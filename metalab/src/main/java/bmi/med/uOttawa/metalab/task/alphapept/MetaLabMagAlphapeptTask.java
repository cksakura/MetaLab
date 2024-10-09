package bmi.med.uOttawa.metalab.task.alphapept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptPepReader;
import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptPeptide;
import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptProReader;
import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptTask;
import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptWorkflow;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.mag.MagHapPFindTask;
import bmi.med.uOttawa.metalab.task.mag.MetaProteinAnnoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;

public class MetaLabMagAlphapeptTask extends MagHapPFindTask {

	private String alphapept;
	private AlphapeptTask alphaPeptTask;
	private static final Logger LOGGER = LogManager.getLogger(MetaLabMagAlphapeptTask.class);
	private static final String taskName = "Alphapept workflow";

	public MetaLabMagAlphapeptTask(MetaParameterMag metaPar, MetaSourcesMag msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv, bar1, bar2, nextWork);
		// TODO Auto-generated constructor stub
	}

	protected void initial() {
		this.alphapept = ((MetaSourcesMag) msv).getAlphapept();
		this.threadCount = metaPar.getThreadCount();
		this.alphaPeptTask = new AlphapeptTask(this.alphapept);

		this.resultFolderFile = new File(metaPar.getResult());

		this.metadata = metaPar.getMetadata();
		this.magDb = ((MetaParameterMag) metaPar).getUsedMagDbItem();

		this.threadCount = metaPar.getThreadCount();
		this.quanMode = ((MetaParameterMag) metaPar).getQuanMode();
		this.resultFolderFile = metaPar.getDbSearchResultFile();
		this.expNameMap = new HashMap<String, String>();

		if (metadata != null) {
			this.rawFiles = metadata.getRawFiles();
			this.expNames = metadata.getExpNames();
			this.quanExpNames = new String[rawFiles.length];
			this.fractions = metadata.getFractions();

			if (rawFiles != null && rawFiles.length > 0) {
				for (int i = 0; i < rawFiles.length; i++) {
					this.quanExpNames[i] = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1,
							rawFiles[i].lastIndexOf("."));
					this.expNameMap.put(quanExpNames[i], expNames[i]);
				}
			}
		}

		setTaskCount();
	}

	protected boolean separateSearchHap() {
		this.separateResultFolders = new File[rawFiles.length];
		for (int i = 0; i < rawFiles.length; i++) {
			String name = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1, rawFiles[i].lastIndexOf("."));

			this.separateResultFolders[i] = new File(metaPar.getResult(), name);
			this.separateResultFolders[i].mkdir();

			File hapFile = new File(separateResultFolders[i], "hap");
			if (!hapFile.exists()) {
				hapFile.mkdirs();
			}

			File hdfFile = new File(hapFile, "hap.hdf");
			File pepResultFile = new File(hapFile, "hap_peptides.csv");
			File proResultFile = new File(hapFile, "hap_proteins.csv");
			if (!pepResultFile.exists() || !proResultFile.exists()) {

				AlphapeptWorkflow workflow = null;
				try {
					workflow = AlphapeptWorkflow.parse();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": can't parse the templete workflow", e);
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": can't parse the templete workflow");
				}

				if (workflow == null) {
					LOGGER.error(getTaskName() + ": templete workflow was not found");
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": templete workflow was not found");
					return false;
				}

				File hapFastaFile = this.magDb.getHapFastaFile();
				metaPar.setCurrentDb(hapFastaFile.getAbsolutePath());
				metaPar.setThreadCount(1);

				File workflowFile = new File(hapFile, "hap.yaml");
				try {
					workflow.config((MetaParameterMag) metaPar, new File(rawFiles[i]), expNames[i], fractions[i],
							workflowFile, hdfFile);
					alphaPeptTask.addTask(workflowFile);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": writing workflow file to " + workflowFile + " failed");
					System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": writing workflow file to "
							+ workflowFile + " failed");
					return false;
				}

				LOGGER.info(getTaskName() + ": searching high-abundance database started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": searching high-abundance database started");
			} else {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": AlphaPept search result already exists in " + separateResultFolders[i].getAbsolutePath());
				LOGGER.info(getTaskName() + ": AlphaPept search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			}
		}
		alphaPeptTask.run(threadCount, rawFiles.length * 4);

		for (int i = 0; i < separateResultFolders.length; i++) {
			File hapFile = new File(separateResultFolders[i], "hap");
			File pepResultFile = new File(hapFile, "hap_peptides.csv");
			File proResultFile = new File(hapFile, "hap_proteins.csv");
			if (!pepResultFile.exists() || !proResultFile.exists()) {

				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": AlphaPept search result was not found from " + hapFile.getAbsolutePath());

				LOGGER.info(
						getTaskName() + ": AlphaPept search result was not found from " + hapFile.getAbsolutePath());

				return false;
			}
		}

		return true;
	}

	protected boolean separateSearch() {

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hdfFile = new File(separateResultFolders[i], "mag.hdf");
			File pepResultFile = new File(separateResultFolders[i], "mag_peptides.csv");
			File proResultFile = new File(separateResultFolders[i], "mag_proteins.csv");
			if (!pepResultFile.exists() || !proResultFile.exists()) {

				AlphapeptWorkflow workflow = null;
				try {
					workflow = AlphapeptWorkflow.parse();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": can't parse the templete workflow", e);
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": can't parse the templete workflow");
				}

				if (workflow == null) {
					LOGGER.error(getTaskName() + ": templete workflow was not found");
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": templete workflow was not found");
					return false;
				}

				File hapFile = new File(separateResultFolders[i], "hap");
				File db = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
				if (!db.exists()) {
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": sample-specific database"
							+ db + " was not found");

					LOGGER.error(getTaskName() + ": sample-specific database" + db + " was not found");

					return false;
				}

				metaPar.setCurrentDb(db.getAbsolutePath());
				metaPar.setThreadCount(1);

				File workflowFile = new File(separateResultFolders[i], "mag.yaml");
				try {
					workflow.config((MetaParameterMag) metaPar, new File(rawFiles[i]), expNames[i], fractions[i],
							workflowFile, hdfFile);
					alphaPeptTask.addTask(workflowFile);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": writing workflow file to " + workflowFile + " failed");
					System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": writing workflow file to "
							+ workflowFile + " failed");
					return false;
				}

				LOGGER.info(getTaskName() + ": searching high-abundance database started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": searching high-abundance database started");
			} else {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": AlphaPept search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": AlphaPept search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			}
		}
		alphaPeptTask.run(threadCount, rawFiles.length * 4);

		for (int i = 0; i < separateResultFolders.length; i++) {
			File pepResultFile = new File(separateResultFolders[i], "mag_peptides.csv");
			File proResultFile = new File(separateResultFolders[i], "mag_proteins.csv");
			if (!pepResultFile.exists() || !proResultFile.exists()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": AlphaPept search result was not found from " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": AlphaPept search result was not found from "
						+ separateResultFolders[i].getAbsolutePath());
				return false;
			}
		}

		return true;
	}

	protected boolean fastaExtract4Iterator() {

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
			File pepResultFile = new File(hapFile, "hap_peptides.csv");

			if (dbFile.exists() && dbFile.length() > 0) {

				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": sample-specific database for "
						+ separateResultFolders[i].getName() + " already exists in " + dbFile.getAbsolutePath());

				LOGGER.info(getTaskName() + ": sample-specific database for " + separateResultFolders[i].getName()
						+ " already exists in " + dbFile.getAbsolutePath());

				continue;
			}

			if (pepResultFile.exists()) {

				LOGGER.info(getTaskName() + ": parsing high-abundance database search result from " + pepResultFile);
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": parsing high-abundance database search result from " + pepResultFile);

				HashSet<String> totalPsmSet = new HashSet<String>();
				HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();

				try {

					BufferedReader combineTsvReader = new BufferedReader(new FileReader(pepResultFile));
					String line = combineTsvReader.readLine();
					String[] title = line.split(AlphapeptProReader.delimiter);
					int proid = -1;
					int pepid = -1;
					for (int j = 0; j < title.length; j++) {
						if (title[j].equals("protein")) {
							proid = j;
						} else if (title[j].equals("sequence_naked")) {
							pepid = j;
						}
					}
					while ((line = combineTsvReader.readLine()) != null) {
						String[] cs = line.split(AlphapeptProReader.delimiter);
						String[] pros;
						if (cs[proid].startsWith("\"")) {
							pros = cs[proid].substring(1, cs[proid].length() - 1).split(",");
						} else {
							pros = cs[proid].split(",");
						}

						for (int j = 0; j < pros.length; j++) {
							if (pros[j].startsWith(magDb.getIdentifier())) {
								String genome = pros[j].substring(0, pros[j].indexOf("_"));
								if (genomeMap.containsKey(genome)) {
									genomeMap.get(genome).add(cs[pepid]);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(cs[pepid]);
									genomeMap.put(genome, set);
								}
								totalPsmSet.add(cs[pepid]);
							}
						}
					}
					combineTsvReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": error in reading database search result file " + pepResultFile, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in reading database search result file " + pepResultFile);
				}

				if (totalPsmSet.size() == 0) {
					LOGGER.info(getTaskName() + ": no PSM identifications in " + pepResultFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": no PSM identifications in "
							+ pepResultFile);

					continue;
				}

				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": start generating sample-specific database for " + separateResultFolders[i].getName());

				LOGGER.info(getTaskName() + ": start generating sample-specific database for "
						+ separateResultFolders[i].getName());

				File hapGenomeFile = new File(hapFile, "hap_genomes.tsv");
				HashSet<String> genomeSet = refineGenomes(genomeMap, totalPsmSet.size(), psmCoverThreshold,
						hapGenomeFile);

				try {

					PrintWriter writer = new PrintWriter(dbFile);
					File originalDbFile = this.magDb.getOriginalDbFolder();
					for (String genome : genomeSet) {
						File fastafile = new File(originalDbFile, genome + ".faa");
						if (!fastafile.exists()) {
							fastafile = new File(originalDbFile, genome + ".fasta");
						}

						if (fastafile.exists()) {
							BufferedReader reader = new BufferedReader(new FileReader(fastafile));
							String line = null;
							while ((line = reader.readLine()) != null) {
								writer.println(line);
							}
							reader.close();
						}
					}

					File hostDbFile = this.magDb.getHostDbFile();
					if (metaPar.isAppendHostDb() && (hostDbFile != null) && hostDbFile.exists()) {
						BufferedReader reader = new BufferedReader(new FileReader(hostDbFile));
						String line = null;
						while ((line = reader.readLine()) != null) {
							writer.println(line);
						}
						reader.close();
					}

					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": error in writing protein sequences to the sample-specific database "
							+ dbFile, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in writing protein sequences to the sample-specific database " + dbFile);
					return false;
				}
			} else {

				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": high abundant database search result file " + pepResultFile + " was not found");

				LOGGER.error(getTaskName() + ": high abundant database search result file " + pepResultFile
						+ " was not found");

				return false;
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": generating sample-specific database for " + separateResultFolders[i].getName() + " finished");

			LOGGER.info(getTaskName() + ": generating sample-specific database for "
					+ separateResultFolders[i].getName() + " finished");
		}

		return true;
	}

	protected boolean combineResult() {
		HashSet<String> totalPsmSet = new HashSet<String>();
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File pepResultFile = new File(separateResultFolders[i], "mag_peptides.csv");
			try {
				BufferedReader combineTsvReader = new BufferedReader(new FileReader(pepResultFile));
				String line = combineTsvReader.readLine();
				String[] title = line.split(AlphapeptProReader.delimiter);
				int proid = -1;
				int pepid = -1;
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals("protein")) {
						proid = j;
					} else if (title[j].equals("sequence_naked")) {
						pepid = j;
					}
				}
				while ((line = combineTsvReader.readLine()) != null) {
					String[] cs = line.split(AlphapeptProReader.delimiter);
					String[] pros;
					if (cs[proid].startsWith("\"")) {
						pros = cs[proid].substring(1, cs[proid].length() - 1).split(",");
					} else {
						pros = cs[proid].split(",");
					}

					for (int j = 0; j < pros.length; j++) {
						if (genomeMap.containsKey(pros[j])) {
							genomeMap.get(pros[j]).add(cs[pepid]);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(cs[pepid]);
							genomeMap.put(pros[j], set);
						}
						totalPsmSet.add(cs[pepid]);
					}
				}
				combineTsvReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading database search result file " + pepResultFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading database search result file " + pepResultFile);
			}
		}

		File combinedFastaFile = new File(metaPar.getResult(), "combined.fasta");
		if (!combinedFastaFile.exists() || combinedFastaFile.length() == 0) {
			File hapGenomeFile = new File(metaPar.getResult(), "razorProteins.tsv");
			HashSet<String> proteinSet = refineGenomes(genomeMap, totalPsmSet.size(), 1.0, hapGenomeFile);

			try (PrintWriter writer = new PrintWriter(combinedFastaFile)) {
				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File hapFile = new File(separateResultFolders[i], "hap");
					File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
					try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
						String line = null;
						boolean use = false;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith(">")) {
								String proName = line.substring(1, line.indexOf(" "));
								if (proteinSet.contains(proName)) {
									use = true;
									writer.println(line);
								} else {
									use = false;
								}
							} else {
								if (use) {
									writer.println(line);
								}
							}
						}
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error(getTaskName() + ": error in reading database file " + dbFile, e);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in reading database file " + dbFile);
					}
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing the combined database file to " + combinedFastaFile,
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing the combined database file to " + combinedFastaFile);
			}
		}

		File hdfFile = new File(metaPar.getResult(), "combined.hdf");
		File pepResultFile = new File(metaPar.getResult(), "combined_peptides.csv");
		File proResultFile = new File(metaPar.getResult(), "combined_proteins.csv");

		if (!pepResultFile.exists() && !proResultFile.exists()) {
			AlphapeptWorkflow workflow = null;
			try {
				workflow = AlphapeptWorkflow.parse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": can't parse the templete workflow", e);
				System.err.println(
						format.format(new Date()) + "\t" + getTaskName() + ": can't parse the templete workflow");
			}

			if (workflow == null) {
				LOGGER.error(getTaskName() + ": templete workflow was not found");
				System.err.println(
						format.format(new Date()) + "\t" + getTaskName() + ": templete workflow was not found");
				return false;
			}

			metaPar.setCurrentDb(combinedFastaFile.getAbsolutePath());
			metaPar.setThreadCount(1);

			File workflowFile = new File(metaPar.getResult(), "combined.yaml");
			try {
				workflow.config((MetaParameterMag) metaPar, workflowFile, hdfFile);
				alphaPeptTask.addTask(workflowFile);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": writing workflow file to " + workflowFile + " failed");
				System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": writing workflow file to "
						+ workflowFile + " failed");
				return false;
			}

			LOGGER.info(getTaskName() + ": searching high-abundance database started");
			System.out.println(
					format.format(new Date()) + "\t" + getTaskName() + ": searching high-abundance database started");
		} else {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": AlphaPept search result already exists in " + metaPar.getResult());

			LOGGER.info(getTaskName() + ": AlphaPept search result already exists in " + metaPar.getResult());
		}

		if (pepResultFile.exists() && pepResultFile.length() > 0 && proResultFile.exists()
				&& proResultFile.length() > 0) {

			boolean exportPepPro = this.exportPepProTxt(pepResultFile, proResultFile);

			if (exportPepPro) {
				LOGGER.info(getTaskName() + ": exporting the final peptide and protein identification finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": exporting the final peptide and protein identification finished");
			} else {
				LOGGER.info(getTaskName()
						+ ": peptide and protein identification finished but exporting the final result failed");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": peptide and protein identification finished but exporting the final result failed");
			}

		} else {
			LOGGER.info(getTaskName() + ": peptide and protein identification failed");
			System.out.println(
					format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein identification failed");
		}

		return true;
	}

	protected boolean lfQuant() {
		return true;
	}

	protected boolean isobaricQuant() {
		return true;
	}

	protected boolean exportPepProTxt(File magPepResultFile, File magProResultFile) {

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		HashMap<String, HashSet<String>> razorProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> allProPepMap = new HashMap<String, HashSet<String>>();
		AlphapeptPepReader pepReader = new AlphapeptPepReader(magPepResultFile, expNames);
		AlphapeptPeptide[] peps = pepReader.getMetaPeptides();
		HashMap<String, Integer> pepIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < peps.length; i++) {
			String seq = peps[i].getModSeq();
			pepIdMap.put(seq, i);
			String[] pros = peps[i].getProteins();
			if (razorProPepMap.containsKey(pros[0])) {
				razorProPepMap.get(pros[0]).add(seq);
				allProPepMap.get(pros[0]).add(seq);
			} else {
				HashSet<String> razorSet = new HashSet<String>();
				razorSet.add(seq);
				razorProPepMap.put(pros[0], razorSet);

				HashSet<String> allSet = new HashSet<String>();
				allSet.add(seq);
				allProPepMap.put(pros[0], allSet);
			}

			for (int j = 1; j < pros.length; j++) {
				if (allProPepMap.containsKey(pros[j])) {
					allProPepMap.get(pros[j]).add(seq);
				} else {
					HashSet<String> allSet = new HashSet<String>();
					allSet.add(seq);
					allProPepMap.put(pros[j], allSet);
				}
			}
		}

		AlphapeptProReader proReader = new AlphapeptProReader(magProResultFile, expNames);
		MetaProtein[] pros = proReader.getMetaProteins();
		HashMap<String, Integer> proIdMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> proSameSetMap = new HashMap<String, HashSet<String>>();
		String leadProName = "";
		int groupId = -1;
		for (int i = 0; i < pros.length; i++) {
			if (pros[i].getGroupId() == groupId) {
				proSameSetMap.get(leadProName).add(pros[i].getName());
			} else {
				groupId = pros[i].getGroupId();
				leadProName = pros[i].getName();
				proSameSetMap.put(leadProName, new HashSet<String>());
			}
			proIdMap.put(pros[i].getName(), pros[i].getGroupId());
		}

		this.final_pep_txt = new File(metaPar.getResult(), "final_peptides.tsv");
		this.final_pro_txt = new File(metaPar.getResult(), "final_proteins.tsv");

		PrintWriter pepWriter = null;
		try {
			pepWriter = new PrintWriter(this.final_pep_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Sequence").append("\t");
			titlesb.append("Base sequence").append("\t");
			titlesb.append("Length").append("\t");
			titlesb.append("Proteins").append("\t");
			titlesb.append("Charges").append("\t");
			titlesb.append("Score").append("\t");

			for (int i = 0; i < this.expNames.length; i++) {
				titlesb.append("Identification type " + expNames[i]).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < expNames.length; i++) {
				titlesb.append("Intensity " + expNames[i]).append("\t");
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			pepWriter.println(titlesb);

			for (int i = 0; i < peps.length; i++) {

				String sequence = peps[i].getSequence();
				StringBuilder sb = new StringBuilder();
				sb.append(peps[i].getModSeq()).append("\t");
				sb.append(sequence).append("\t");
				sb.append(peps[i].getLength()).append("\t");

				String[] proteins = peps[i].getProteins();
				if (proteins.length > 0) {
					for (String pro : proteins) {
						sb.append(pro).append(";");
					}
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append("\t");

				int[] charge = peps[i].getCharges();
				StringBuilder chargesb = new StringBuilder();
				for (int j = 0; j < charge.length; j++) {
					chargesb.append(charge[j]).append(";");
				}
				if (chargesb.length() > 1) {
					sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
				} else {
					sb.append("2").append("\t");
				}

				sb.append(peps[i].getScore()).append("\t");

				int[] spCount = peps[i].getMs2Counts();

				for (int j = 0; j < spCount.length; j++) {
					if (spCount[j] > 0) {
						sb.append(MetaPeptide.idenTypeStrings[0]).append("\t");
					} else {
						sb.append(MetaPeptide.idenTypeStrings[2]).append("\t");
					}
				}

				double totalIntensity = 0;

				double[] intensity = peps[i].getIntensity();
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append((int) totalIntensity).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append((int) intensity[j]).append("\t");
				}

				sb.append("\t").append("\t");

				sb.append(i).append("\t");

				HashSet<Integer> proIdSet = new HashSet<Integer>();
				if (proteins.length > 0) {
					for (String pro : proteins) {
						if (proIdMap.containsKey(pro)) {
							proIdSet.add(proIdMap.get(pro));
						}
					}
				}
				if (proIdSet.size() > 0) {
					for (Integer proId : proIdSet) {
						sb.append(proId).append(";");
					}
					sb.deleteCharAt(sb.length() - 1);
				}

				sb.append("\t");

				int totalSpCount = 0;

				for (int j = 0; j < spCount.length; j++) {
					totalSpCount += spCount[j];
				}
				sb.append(totalSpCount);

				pepWriter.println(sb);
			}

			pepWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing peptides to " + this.final_pep_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing peptides to "
					+ this.final_pep_txt.getName());
		}

		LOGGER.info(getTaskName() + ": peptide report has been exported to " + final_pep_txt);
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": peptide report has been exported to "
				+ final_pep_txt);

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		}

		LOGGER.info(getTaskName() + ": exporting protein report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting protein report started");

		PrintWriter proWriter = null;
		try {
			proWriter = new PrintWriter(this.final_pro_txt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing final protein result to " + this.final_pro_txt.getName(),
					e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing final protein result to " + this.final_pro_txt.getName());
		}

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Protein IDs").append("\t");
		titlesb.append("Majority protein IDs").append("\t");
		titlesb.append("Peptide counts (all)").append("\t");
		titlesb.append("Peptide counts (razor)").append("\t");
		titlesb.append("Number of proteins").append("\t");
		titlesb.append("Peptides").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Intensity").append("\t");

		for (int i = 0; i < expNames.length; i++) {
			titlesb.append("Intensity " + expNames[i]).append("\t");
		}

		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id").append("\t");
		titlesb.append("Peptide IDs").append("\t");
		titlesb.append("Peptide is razor");

		proWriter.println(titlesb);

		for (int i = 0; i < pros.length; i++) {
			String pro = pros[i].getName();
			if (proSameSetMap.containsKey(pro)) {

				HashSet<String> allPepSet = allProPepMap.get(pro);
				HashSet<String> razorPepSet = razorProPepMap.get(pro);

				if (allPepSet == null || razorPepSet == null) {
					continue;
				}

				HashSet<String> sameSet = proSameSetMap.get(pro);
				int proCount = 1 + sameSet.size();
				int[] pepCountAll = new int[proCount];
				int[] pepCountRazor = new int[proCount];

				pepCountAll[0] = allProPepMap.get(pro).size();
				pepCountRazor[0] = razorProPepMap.get(pro).size();

				String[] samePros = sameSet.toArray(new String[sameSet.size()]);
				for (int j = 0; j < samePros.length; j++) {
					if (razorProPepMap.containsKey(samePros[j])) {
						pepCountRazor[j + 1] = razorProPepMap.get(samePros[j]).size();
					} else {
						pepCountRazor[j + 1] = 0;
					}
					if (allProPepMap.containsKey(samePros[j])) {
						pepCountAll[j + 1] = allProPepMap.get(samePros[j]).size();
					} else {
						pepCountAll[j + 1] = 0;
					}
				}
				StringBuilder sameSb = new StringBuilder();
				StringBuilder pepCountAllSb = new StringBuilder();
				StringBuilder pepCountRazorSb = new StringBuilder();

				sameSb.append(pro);
				pepCountAllSb.append(pepCountAll[0]);
				pepCountRazorSb.append(pepCountRazor[0]);
				int usedProCount = 1;

				for (int j = 0; j < samePros.length; j++) {
					if (pepCountAll[j + 1] > 0) {
						sameSb.append(";").append(samePros[j]);
						pepCountAllSb.append(";").append(pepCountAll[j + 1]);
						pepCountRazorSb.append(";").append(pepCountRazor[j + 1]);
						usedProCount++;
					}
				}

				StringBuilder sb = new StringBuilder();
				sb.append(sameSb).append("\t");
				sb.append(pro).append("\t");
				sb.append(pepCountAllSb).append("\t");
				sb.append(pepCountRazorSb).append("\t");
				sb.append(usedProCount).append("\t");
				sb.append(allPepSet.size()).append("\t");
				sb.append(pros[i].getScore()).append("\t");

				double totalIntensity = 0;
				double[] intensity = pros[i].getIntensities();
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append(totalIntensity).append("\t");
				for (int j = 0; j < intensity.length; j++) {
					sb.append(intensity[j]).append("\t");
				}
				sb.append("\t").append("\t");
				sb.append(pros[i].getGroupId()).append("\t");

				StringBuilder pepIdSb = new StringBuilder();
				StringBuilder pepRazorSb = new StringBuilder();

				String[] allPeps = allPepSet.toArray(new String[allPepSet.size()]);
				Arrays.sort(allPeps, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						// TODO Auto-generated method stub

						return pepIdMap.get(o1) - pepIdMap.get(o2);
					}
				});
				for (String pep : allPeps) {
					pepIdSb.append(pepIdMap.get(pep)).append(";");
					if (razorPepSet.contains(pep)) {
						pepRazorSb.append("true;");
					} else {
						pepRazorSb.append("false;");
					}
				}
				sb.append(pepIdSb).append("\t");
				sb.append(pepRazorSb);

				proWriter.println(sb);
			}
		}

		proWriter.close();

		LOGGER.info(getTaskName() + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein report has been exported to "
				+ final_pro_txt.getName());

		return true;
	}

	protected boolean exportSummary(MetaReportTask task) {

		this.final_summary_txt = new File(metaPar.getResult(), "final_summary.tsv");
		try {
			HashMap<String, int[]> idMap = new HashMap<String, int[]>();
			MetaData metaData = metaPar.getMetadata();

			PrintWriter countWriter = new PrintWriter(this.final_summary_txt);
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Raw file").append("\t");
			titlesb.append("Experiment").append("\t");
			titlesb.append("MS/MS").append("\t");
			titlesb.append("MS/MS Identified").append("\t");
			titlesb.append("MS/MS Identified [%]").append("\t");
			titlesb.append("Peptide Sequences Identified");

			countWriter.println(titlesb);

			int totalms2count = 0;
			int totalpsmCount = 0;
			int totalUnipepCount = 0;

			String[] rawFileStrings = metaData.getRawFiles();
			String[] expStrings = metaData.getExpNames();

			for (int i = 0; i < rawFileStrings.length; i++) {
				File rawFile = new File(rawFileStrings[i]);
				String rawNameString = rawFile.getName();
				rawNameString = rawNameString.substring(0, rawNameString.length() - 2);

				if (idMap.containsKey(rawNameString)) {

					StringBuilder sb = new StringBuilder();
					sb.append(rawNameString).append("\t");
					sb.append(expStrings[i]).append("\t");
					int[] idCounts = idMap.get(rawNameString);

					sb.append(idCounts[0]).append("\t");
					sb.append(idCounts[1]).append("\t");

					double ratio = 0;
					if (idCounts[0] > 0 && idCounts[1] > 0) {
						ratio = (double) idCounts[1] / (double) idCounts[0] * 100.0;
					}

					sb.append(FormatTool.getDF2().format(ratio)).append("\t");
					sb.append(idCounts[2]);

					countWriter.println(sb);

					totalms2count += idCounts[0];
					totalpsmCount += idCounts[1];
					totalUnipepCount += idCounts[2];

				} else {
					StringBuilder sb = new StringBuilder();
					sb.append(rawNameString).append("\t");
					sb.append(expStrings[i]).append("\t");
					sb.append("0\t0\t0%\t0");

					countWriter.println(sb);
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Total").append("\t");
			sb.append("\t");
			sb.append(totalms2count).append("\t");
			sb.append(totalpsmCount).append("\t");

			double ratio = 0;
			if (totalms2count > 0) {
				ratio = (double) totalpsmCount / (double) totalms2count * 100.0;
			}

			sb.append(FormatTool.getDF2().format(ratio)).append("\t");

			sb.append(totalUnipepCount);

			countWriter.print(sb);

			countWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing PSM information to " + final_summary_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing PSM information to " + final_summary_txt.getName());
		}

		if (!final_summary_txt.exists() || final_summary_txt.length() == 0) {
			return false;

		} else {

			boolean finish = false;
			File report_ID_summary = new File(reportHtmlDir, "report_ID_summary.html");
			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				finish = task.addTask(MetaReportTask.summary, final_summary_txt, report_ID_summary, summaryMetaFile);
			} else {
				finish = task.addTask(MetaReportTask.summary, final_summary_txt, report_ID_summary);
			}

			return finish;
		}
	}

	protected boolean exportTaxaFunc(MetaReportTask task) {

		LOGGER.info(getTaskName() + ": taxonomy analysis and functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName()
				+ ": taxonomy analysis and functional annotation started");

		// functional annotation

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();

		LOGGER.info(getTaskName() + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation started");

		File funcFile = new File(metaPar.getResult(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		this.final_pro_xml_file = new File(funcFile, "functions.xml");

		File pepResultCsv = new File(metaPar.getDbSearchResultFile(), "mag_peptides.csv");
		File proResultCsv = new File(metaPar.getDbSearchResultFile(), "mag_proteins.csv");

		AlphapeptProReader proReader = new AlphapeptProReader(proResultCsv, this.expNames);
		MetaProtein[] proteins = proReader.getMetaProteins();

		if (!final_pro_xml_file.exists() || final_pro_xml_file.length() == 0) {

			MetaProteinAnnoMag[] mpas = funcAnnoSqlite(proteins, proReader.getIntensityTitle());
			for (int i = 0; i < mpas.length; i++) {
				String name = mpas[i].getPro().getName();
				if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
					proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
				} else {
					proTaxIdMap.put(name, 131567);
				}
			}
		}

		setProgress(88);

		File funcResultFile = final_pro_xml_file.getParentFile();
		File funTsv = new File(funcResultFile, "functions.tsv");
		File funReportTsv = new File(funcResultFile, "functions_report.tsv");

		MetaProteinXMLReader2 funReader = null;
		try {
			funReader = new MetaProteinXMLReader2(final_pro_xml_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading protein function information from "
					+ final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading protein function information from " + final_pro_xml_file.getName());
		}

		if (proTaxIdMap.size() == 0) {
			MetaProteinAnnoEggNog[] mpas = funReader.getProteins();
			for (int i = 0; i < mpas.length; i++) {
				String name = mpas[i].getPro().getName();
				if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
					proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
				} else {
					proTaxIdMap.put(name, 131567);
				}
			}
		}

		if (!funTsv.exists() || !funReportTsv.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to "
					+ funTsv.getName() + " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + funTsv.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to "
					+ funTsv.getName() + " finished");
		}

		File html = new File(funcResultFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to "
					+ html.getName() + " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to "
					+ html.getName() + " finished");
		}

		File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");

		if (!report_function_summary.exists() || report_function_summary.length() == 0) {

			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary, summaryMetaFile);
			} else {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
			}
		}

		LOGGER.info(getTaskName() + ": functional annotation finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation finished");

		setProgress(92);

		// taxonomy analysis

		LOGGER.info(getTaskName() + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis started");

		File taxFile = new File(metaPar.getResult(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		AlphapeptPepReader pepReader = new AlphapeptPepReader(pepResultCsv, this.expNames);
		AlphapeptPeptide[] peptides = pepReader.getMetaPeptides();
		HashMap<String, double[]> pepIntenMap = new HashMap<String, double[]>();
		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> pepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> uniquePepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, ArrayList<Double>> genomeQvalueMap = new HashMap<String, ArrayList<Double>>();

		for (int i = 0; i < peptides.length; i++) {
			String sequence = peptides[i].getSequence();
			double[] pepIntensity = peptides[i].getIntensity();
			pepIntenMap.put(sequence, pepIntensity);

			int psmCount = 0;
			int[] ms2Count = peptides[i].getMs2Counts();
			for (int j = 0; j < expNames.length; j++) {
				psmCount += ms2Count[j];
			}

			String[] pros = peptides[i].getProteins();
			String razorPro = pros[0];

			if (razorPro.startsWith(this.magDb.getIdentifier())) {
				String genome = razorPro.split("_")[0];
				double score = peptides[i].getScore();

				if (psmCountMap.containsKey(genome)) {
					psmCountMap.put(genome, psmCountMap.get(genome) + psmCount);
					pepCountMap.get(genome).add(sequence);

					if (pros.length == 1) {
						uniquePepCountMap.get(genome).add(sequence);
					}
				} else {
					psmCountMap.put(genome, psmCount);
					pepCountMap.put(genome, new HashSet<String>());
					pepCountMap.get(genome).add(sequence);
					uniquePepCountMap.put(genome, new HashSet<String>());
					if (pros.length == 1) {
						uniquePepCountMap.get(genome).add(sequence);
					}
				}

				ArrayList<Double> scoreList;
				double[] totalIntensity;
				if (genomeIntensityMap.containsKey(genome)) {
					totalIntensity = genomeIntensityMap.get(genome);
					scoreList = genomeQvalueMap.get(genome);
				} else {
					totalIntensity = new double[pepIntensity.length];
					scoreList = new ArrayList<Double>();
				}
				for (int j = 0; j < totalIntensity.length; j++) {
					totalIntensity[j] += pepIntensity[j];
				}
				genomeIntensityMap.put(genome, totalIntensity);

				scoreList.add(score);
				genomeQvalueMap.put(genome, scoreList);
			}
		}

		return exportGenome(genomeIntensityMap, psmCountMap, pepCountMap, uniquePepCountMap, genomeQvalueMap, taxFile,
				expNames, task);
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}

}
