package bmi.med.uOttawa.metalab.task.sage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import com.sun.management.OperatingSystemMXBean;

import bmi.med.uOttawa.metalab.core.prodb.FastaManager;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.sage.SageParameter;
import bmi.med.uOttawa.metalab.dbSearch.sage.SagePeptide;
import bmi.med.uOttawa.metalab.dbSearch.sage.SageResultReader;
import bmi.med.uOttawa.metalab.dbSearch.sage.SageTask;
import bmi.med.uOttawa.metalab.spectra.io.MSConvertor;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.hgm.HMGenomeHtmlWriter;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideTsvReader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinTsvReader;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagFuncSearcher;
import bmi.med.uOttawa.metalab.task.mag.MagHapPFindTask;
import bmi.med.uOttawa.metalab.task.mag.MetaProteinAnnoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

public class MetaLabMagSageTask extends MagHapPFindTask {

	private String sage;
	private SageTask sageTask;
	private static final Logger LOGGER = LogManager.getLogger(MetaLabMagSageTask.class);
	private static final String taskName = "Sage workflow";

	private String[] mzMLFiles;

	public MetaLabMagSageTask(MetaParameterMag metaPar, MetaSourcesMag msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv, bar1, bar2, nextWork);
		// TODO Auto-generated constructor stub
	}

	protected void initial() {
		this.sage = ((MetaSourcesMag) msv).getSage();
		this.sageTask = new SageTask(this.sage);
		this.spConvert = true;

		this.metadata = metaPar.getMetadata();
		this.magDb = ((MetaParameterMag) metaPar).getUsedMagDbItem();

		this.threadCount = metaPar.getThreadCount();
		this.quanMode = ((MetaParameterPFind) metaPar).getQuanMode();
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

	protected boolean spectraConvert() {
		if (spConvert) {
			try {
				this.mzMLFiles = msconvert(metadata.getRawFiles());
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in extracting spectra", e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in extracting spectra");
			}
		}

		return true;
	}

	protected String[] msconvert(String[] raws) throws NumberFormatException, IOException {

		bar2.setString(getTaskName() + ": extracting spectra");

		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": extracting spectra started");
		LOGGER.info(getTaskName() + ": extracting spectra started");

		this.spectraDirFile = metaPar.getSpectraFile();
		this.rtFile = new File(spectraDirFile, "rt.txt");
		this.ms2CountFile = new File(spectraDirFile, "ms2Count.txt");

		String[] rawFileNames = new String[raws.length];
		String[] mzMLFiles = new String[raws.length];

		ArrayList<String> extractRawList = new ArrayList<String>();
		for (int i = 0; i < raws.length; i++) {

			File rawi = new File(raws[i]);
			String rawName = rawi.getName();

			rawFileNames[i] = rawName.substring(0, rawName.lastIndexOf("."));
			File mzMLFile = new File(rawi.getParent(), rawFileNames[i] + ".mzML");

			if (!mzMLFile.exists() || mzMLFile.length() == 0) {
				extractRawList.add(raws[i]);
			}

			mzMLFiles[i] = mzMLFile.getAbsolutePath();
		}

		if (extractRawList.size() > 0) {

			File msconvertFile;

			if (((MetaSourcesV2) msv).findMSConvert()) {
				msconvertFile = new File(((MetaSourcesV2) msv).getMsconvert());
			} else {
				File resourceFile = (new File(((MetaSourcesV2) msv).getMsconvert())).getParentFile().getParentFile();
				File proteoWizardFile = new File(resourceFile, "ProteoWizard");
				msconvertFile = new File(proteoWizardFile, "msconvert.exe");
			}

			if (msconvertFile.exists()) {

				ExecutorService es = Executors.newFixedThreadPool(metaPar.getThreadCount());

				for (int i = 0; i < extractRawList.size(); i++) {

					String rawi = extractRawList.get(i);
					es.submit(() -> {
						MSConvertor.raw2mzML(msconvertFile.getAbsolutePath(), rawi);
					});
				}

				es.shutdown();

				try {
					boolean finish = es.awaitTermination(extractRawList.size() * 4, TimeUnit.HOURS);
					if (finish) {
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": msconvert finished");
						LOGGER.info(getTaskName() + ": msconvert finished");
					} else {
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": msconvert failed");
						LOGGER.info(getTaskName() + ": msconvert failed");
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": msconvert was not found, please " + "put \"ProteoWizard\" into the resource folder");
				LOGGER.info(getTaskName() + ": msconvert was not found, please "
						+ "put \"ProteoWizard\" into the resource folder");
			}

		} else {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra files have been found in "
					+ spectraDirFile);
			LOGGER.info(getTaskName() + ": spectra files have been found in " + spectraDirFile);
		}

		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": extracting spectra finished");
		LOGGER.info(getTaskName() + ": extracting spectra finished");
		/*
		 * if (!rtFile.exists() || rtFile.length() == 0 || !ms2CountFile.exists() ||
		 * ms2CountFile.length() == 0) {
		 * 
		 * System.out.println( format.format(new Date()) + "\t" + getTaskName() +
		 * ": writing retention time information started"); LOGGER.info(getTaskName() +
		 * ": writing retention time information started");
		 * 
		 * PrintWriter rtWriter = new PrintWriter(this.rtFile); PrintWriter
		 * ms2CountWriter = new PrintWriter(this.ms2CountFile);
		 * 
		 * rtWriter.println("FileName\tScannum\tRt");
		 * ms2CountWriter.println("FileName\tMS2 count");
		 * 
		 * for (int i = 0; i < mgfFileNames.length; i++) {
		 * 
		 * int count = 0; MgfReader reader = new MgfReader(mgfFileNames[i]); Spectrum sp
		 * = null; while ((sp = reader.getNextSpectrum()) != null) { count++; int
		 * scannum = sp.getScannum(); double rtsecond = sp.getRt();
		 * rtWriter.println(rawFileNames[i] + "\t" + scannum + "\t" + rtsecond); }
		 * reader.close();
		 * 
		 * ms2CountWriter.println(rawFileNames[i] + "\t" + count); }
		 * 
		 * rtWriter.close(); ms2CountWriter.close();
		 * 
		 * System.out.println( format.format(new Date()) + "\t" + getTaskName() +
		 * ": writing retention time information finished"); LOGGER.info(getTaskName() +
		 * ": writing retention time information finished"); }
		 */
		return mzMLFiles;
	}

	protected void setTaskCount() {
		long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
				.getTotalMemorySize() / 1024 / 1024 / 1024;
		this.maxTaskCount = (int) (totalMemorySize / 32);
		if (this.maxTaskCount == 0) {
			this.maxTaskCount = 1;
		} else if (this.maxTaskCount > 4) {
			this.maxTaskCount = 4;
		}
		LOGGER.info(getTaskName() + ": the number of max memory is " + totalMemorySize
				+ " GB, the max number of tasks is set as " + maxTaskCount);
	}

	protected boolean separateSearchHap() {

		int taskCount = 0;
		this.separateResultFolders = new File[rawFiles.length];

		for (int i = 0; i < rawFiles.length; i++) {
			String name = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1, rawFiles[i].lastIndexOf("."));

			this.separateResultFolders[i] = new File(metaPar.getResult(), name);
			if (!this.separateResultFolders[i].exists()) {
				this.separateResultFolders[i].mkdir();
			}

			File hapFile = new File(separateResultFolders[i], "hap");
			if (!hapFile.exists()) {
				hapFile.mkdirs();
			}

			File hapResultFile = new File(hapFile, "results.sage.tsv");

			if (hapResultFile.exists()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result already exists in " + hapResultFile.getAbsolutePath());

				LOGGER.info(
						getTaskName() + ": Sage search result already exists in " + hapResultFile.getAbsolutePath());
			} else {
				taskCount++;
			}
		}

		if (taskCount == 0) {
			return true;
		}

		for (int i = 0; i < separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			if (!hapFile.exists()) {
				hapFile.mkdirs();
			}

			File hapResultFile = new File(hapFile, "results.sage.tsv");

			if (!hapResultFile.exists() || hapResultFile.length() < 200) {

				metaPar.setCurrentDb(this.magDb.getHapFastaFile().getAbsolutePath());
				SageParameter sageParameter = new SageParameter();
				File configFile = new File(hapFile, "hap.json");
				try {
					sageParameter.config(((MetaParameterMag) metaPar), false, new String[] { mzMLFiles[i] },
							configFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": error in create the parameter file in " + separateResultFolders[i],
							e);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in create the parameter file in " + separateResultFolders[i]);
				}

				LOGGER.info(getTaskName() + ": searching high-abundance database for " + separateResultFolders[i]
						+ " started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": searching high-abundance database for " + separateResultFolders[i] + " started");

				this.sageTask.addTask(configFile);

			} else {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": Sage search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			}
		}
		sageTask.run(1, rawFiles.length * 4);

		for (int i = 0; i < separateResultFolders.length; i++) {
			File hapFile = new File(separateResultFolders[i], "hap");
			File sageResultFile = new File(hapFile, "results.sage.tsv");

			if (!sageResultFile.exists()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result was not found from " + sageResultFile.getAbsolutePath());

				LOGGER.info(
						getTaskName() + ": Sage search result was not found from " + sageResultFile.getAbsolutePath());

				return false;
			}
		}

		return true;
	}

	protected boolean fastaExtract4Iterator() {

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");

			if (dbFile.exists() && dbFile.length() > 0) {

				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": sample-specific database for "
						+ separateResultFolders[i].getName() + " already exists in " + dbFile.getAbsolutePath());

				LOGGER.info(getTaskName() + ": sample-specific database for " + separateResultFolders[i].getName()
						+ " already exists in " + dbFile.getAbsolutePath());

				continue;
			}

			File hapResultFile = new File(hapFile, "results.sage.tsv");

			LOGGER.info(getTaskName() + ": parsing high-abundance database search result from "
					+ separateResultFolders[i].getName());
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": parsing high-abundance database search result from " + separateResultFolders[i].getName());

			if (hapResultFile.exists()) {

				LOGGER.info(getTaskName() + ": parsing high-abundance database search result " + hapResultFile);
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": parsing high-abundance database search result " + hapResultFile);

				HashSet<String> totalPsmSet = new HashSet<String>();
				HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();

				MagDbItem magDbItem = ((MetaParameterMag) metaPar).getUsedMagDbItem();

				try {
					BufferedReader combineTsvReader = new BufferedReader(new FileReader(hapResultFile));
					String line = combineTsvReader.readLine();
					String[] title = line.split("\t");
					int pepid = -1;
					int proid = -1;
					int qvalueId = -1;
					for (int j = 0; j < title.length; j++) {
						if (title[j].equals("peptide")) {
							pepid = j;
						} else if (title[j].equals("proteins")) {
							proid = j;
						} else if (title[j].equals("spectrum_q")) {
							qvalueId = j;
						}
					}
					while ((line = combineTsvReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (Double.parseDouble(cs[qvalueId]) > 0.1) {
							continue;
						}
						String[] pros = cs[proid].split(";");
						for (int j = 0; j < pros.length; j++) {
							if (pros[j].startsWith(magDbItem.getIdentifier())) {
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
					LOGGER.error(getTaskName() + ": error in reading database search result file " + hapResultFile, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in reading database search result file " + hapResultFile);
				}

				if (totalPsmSet.size() == 0) {
					LOGGER.info(getTaskName() + ": no PSM identifications in " + hapResultFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": no PSM identifications in "
							+ hapResultFile);
				}

				File hapGenomeFile = new File(hapFile, "hap_genomes.tsv");
				HashSet<String> genomeSet = refineGenomes(genomeMap, totalPsmSet.size(), psmCoverThreshold,
						hapGenomeFile);

				try {
					PrintWriter writer = new PrintWriter(dbFile);
					File originalDbFile = this.magDb.getOriginalDbFolder();
					for (String genome : genomeSet) {
						File fastafile = new File(originalDbFile, genome + ".faa");
						BufferedReader reader = new BufferedReader(new FileReader(fastafile));
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
				}
			}

			if (dbFile.exists() && dbFile.length() > 0) {

				LOGGER.info(getTaskName() + ": sample-specific database generation finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": sample-specific database generation finished");

				System.out.println(
						format.format(new Date()) + "\t" + getTaskName() + ": generating sample-specific database for "
								+ separateResultFolders[i].getName() + " finished");

				LOGGER.info(getTaskName() + ": generating sample-specific database for "
						+ separateResultFolders[i].getName() + " finished");
			} else {
				LOGGER.info(getTaskName() + ": sample-specific database generation failed");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": sample-specific database generation failed");

				return false;
			}
		}

		return true;
	}

	protected boolean separateSearch() {

		int taskCount = 0;
		for (int i = 0; i < separateResultFolders.length; i++) {
			File spResultFile = new File(separateResultFolders[i], "results.sage.tsv");
			if (spResultFile.exists() && spResultFile.length() > 200) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result already exists in " + spResultFile.getAbsolutePath());
				LOGGER.info(getTaskName() + ": Sage search result already exists in " + spResultFile.getAbsolutePath());
			} else {
				taskCount++;
			}
		}

		if (taskCount == 0) {
			return true;
		}

		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File hapFile = new File(separateResultFolders[i], "hap");
			File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");

			File spResultFile = new File(separateResultFolders[i], "results.sage.tsv");

			if (!spResultFile.exists() || spResultFile.length() < 200) {
				metaPar.setCurrentDb(dbFile.getAbsolutePath());
				SageParameter sageParameter = new SageParameter();
				File configFile = new File(separateResultFolders[i], "sage.json");
				try {
					sageParameter.config(((MetaParameterMag) metaPar), false, new String[] { mzMLFiles[i] },
							configFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": error in create the parameter file in " + separateResultFolders[i],
							e);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in create the parameter file in " + separateResultFolders[i]);
				}

				LOGGER.info(getTaskName() + ": searching high-abundance database for " + separateResultFolders[i]
						+ " started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": searching high-abundance database for " + separateResultFolders[i] + " started");

				this.sageTask.addTask(configFile);
			} else {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": Sage search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			}
		}
		this.sageTask.run(1, rawFiles.length * 4);

		for (int i = 0; i < separateResultFolders.length; i++) {
			File spResultFile = new File(separateResultFolders[i], "results.sage.tsv");

			if (!spResultFile.exists() || spResultFile.length() < 200) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": Sage search result was not found from " + spResultFile.getAbsolutePath());

				LOGGER.info(
						getTaskName() + ": Sage search result was not found from " + spResultFile.getAbsolutePath());

				return false;
			}
		}

		return true;
	}

	protected boolean combineResult() {
		HashSet<String> totalPsmSet = new HashSet<String>();
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File pepResultFile = new File(separateResultFolders[i], "results.sage.tsv");
			try {
				BufferedReader combineTsvReader = new BufferedReader(new FileReader(pepResultFile));
				String line = combineTsvReader.readLine();
				String[] title = line.split("\t");
				int proid = -1;
				int pepid = -1;
				int qvalueid = -1;
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals("proteins")) {
						proid = j;
					} else if (title[j].equals("peptide")) {
						pepid = j;
					} else if (title[j].equals("spectrum_q")) {
						qvalueid = j;
					}
				}
				while ((line = combineTsvReader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (Double.parseDouble(cs[qvalueid]) > 0.01 || cs[proid].contains("REV_")) {
						continue;
					}
					String[] pros = cs[proid].split(";");

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

		File combinedFastaFile = new File(resultFolderFile, "combined.fasta");
		if (!combinedFastaFile.exists() || combinedFastaFile.length() == 0) {
			File hapGenomeFile = new File(resultFolderFile, "razorProteins.tsv");
			HashSet<String> proteinSet = refineGenomes(genomeMap, totalPsmSet.size(), 1.0, hapGenomeFile);

			try (PrintWriter writer = new PrintWriter(combinedFastaFile)) {
				Random random = new Random();
				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File hapFile = new File(separateResultFolders[i], "hap");
					File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
					try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
						String line = null;
						boolean use = false;
						String shufName = "";
						List<Character> sequenceChars = new ArrayList<Character>();
						while ((line = reader.readLine()) != null) {
							if (line.startsWith(">")) {
								if (sequenceChars.size() > 0) {
									Collections.shuffle(sequenceChars, random);

									StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size());
									for (Character c : sequenceChars) {
										shuffledSequence.append(c);
									}
									writer.println(shufName);
									writer.println(shuffledSequence);

									sequenceChars = new ArrayList<Character>();
								}

								String proName = line.substring(1, line.indexOf(" "));
								if (proteinSet.contains(proName)) {
									use = true;
									writer.println(line);
									proteinSet.remove(proName);

									shufName = ">" + FastaManager.shuffle + line.substring(1);
								} else {
									use = false;
								}
							} else {
								if (use) {
									writer.println(line);
									for (int j = 0; j < line.length(); j++) {
										sequenceChars.add(line.charAt(j));
									}
								}
							}
						}
						reader.close();

						if (sequenceChars.size() > 0) {
							Collections.shuffle(sequenceChars, random);

							StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size());
							for (Character c : sequenceChars) {
								shuffledSequence.append(c);
							}
							writer.println(shufName);
							writer.println(shuffledSequence);
						}

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

		File pepResultFile = new File(resultFolderFile, "results.sage.tsv");
		File quanResultFile = new File(resultFolderFile, "lfq.tsv");

		if (!pepResultFile.exists() && !quanResultFile.exists()) {
			metaPar.setCurrentDb(combinedFastaFile.getAbsolutePath());
			SageParameter sageParameter = new SageParameter();
			File configFile = new File(resultFolderFile, "sage.json");
			try {
				sageParameter.config(((MetaParameterMag) metaPar), true, mzMLFiles, configFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in create the parameter file in " + metaPar.getResult(), e);
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in create the parameter file in " + metaPar.getResult());
			}

			this.sageTask.addTask(configFile);
			this.sageTask.run(mzMLFiles.length);

			LOGGER.info(getTaskName() + ": searching high-abundance database started");
			System.out.println(
					format.format(new Date()) + "\t" + getTaskName() + ": searching high-abundance database started");
		} else {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": Sage search result already exists in " + metaPar.getResult());

			LOGGER.info(getTaskName() + ": Sage search result already exists in " + metaPar.getResult());
		}

		if (pepResultFile.exists() && pepResultFile.length() > 0 && quanResultFile.exists()
				&& quanResultFile.length() > 0) {

			boolean exportPepPro = this.exportPepProTxt();

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

	@SuppressWarnings("unchecked")
	protected boolean exportReport() {

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		HashMap<String, Integer> fileIdMap = new HashMap<String, Integer>();
		File magResultDir = new File(metaPar.getResult(), "mag_result");

		SageResultReader quanPepReader = new SageResultReader(magResultDir);
		String[] intensityTitles = quanPepReader.getIntensityTitle();
		for (int i = 0; i < intensityTitles.length; i++) {
			fileIdMap.put(intensityTitles[i], fileIdMap.size());
		}

		SagePeptide[] quanPeps = quanPepReader.getMetaPeptides();

		HashMap<String, Double> genomeScoreMap = this.refineGenomes(quanPeps, FastaManager.shuffle);

		String[] razorProteins = this.refineProteins(quanPeps, genomeScoreMap, FastaManager.shuffle);

		setProgress(82);

		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> proteinIntensityMap = new HashMap<String, double[]>();
		HashMap<String, Double> proteinTotalIntenMap = new HashMap<String, Double>();

		HashSet<String> pepSequenceSet = new HashSet<String>();
		HashMap<String, HashSet<String>> totalPepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> razorPepCountMap = new HashMap<String, HashSet<String>>();

		ArrayList<SagePeptide> peptideList = new ArrayList<SagePeptide>();

		for (int i = 0; i < quanPeps.length; i++) {
			if (razorProteins[i] != null) {
				String stripeSequence = quanPeps[i].getSequence();

				pepSequenceSet.add(stripeSequence);
				peptideList.add(quanPeps[i]);

				if (razorPepCountMap.containsKey(razorProteins[i])) {
					razorPepCountMap.get(razorProteins[i]).add(stripeSequence);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(stripeSequence);
					razorPepCountMap.put(razorProteins[i], set);
				}

				if (razorProteins[i].startsWith(magDb.getIdentifier())) {
					String genome = razorProteins[i].split("_")[0];
					if (razorPepCountMap.containsKey(genome)) {
						razorPepCountMap.get(genome).add(stripeSequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(stripeSequence);
						razorPepCountMap.put(genome, set);
					}
				}
			}
		}

		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();

		int[] filePsmCount = new int[fileIdMap.size()];
		HashSet<String>[] fileSequenceSets = new HashSet[fileIdMap.size()];
		for (int i = 0; i < fileSequenceSets.length; i++) {
			fileSequenceSets[i] = new HashSet<String>();
		}

		for (int i = 0; i < quanPeps.length; i++) {
			String[] proteins = quanPeps[i].getProteins();
			int count = 0;
			for (int j = 0; j < proteins.length; j++) {
				if (razorPepCountMap.containsKey(proteins[j])) {
					count++;
				}
			}

			if (count > 0) {
				String modSequence = quanPeps[i].getModSeq();
				int[] idenType = quanPeps[i].getIdenType();
				for (int j = 0; j < fileSequenceSets.length; j++) {
					if (idenType[j] == 0) {
						fileSequenceSets[j].add(modSequence);
					}
				}

				String stripeSequence = quanPeps[i].getSequence();
				double[] pepIntensity = new double[quanPeps[i].getIntensity().length];
				for (int j = 0; j < pepIntensity.length; j++) {
					pepIntensity[j] = quanPeps[i].getIntensity()[j] / (double) count;
				}

				int pepMs2Count = 0;
				int[] ms2Count = quanPeps[i].getMs2Counts();
				for (int j = 0; j < ms2Count.length; j++) {
					pepMs2Count += ms2Count[j];
					filePsmCount[j] += ms2Count[j];
				}

				for (int j = 0; j < proteins.length; j++) {
					if (razorPepCountMap.containsKey(proteins[j])) {

						if (psmCountMap.containsKey(proteins[j])) {
							psmCountMap.put(proteins[j], psmCountMap.get(proteins[j]) + pepMs2Count);
						} else {
							psmCountMap.put(proteins[j], pepMs2Count);
						}

						double proteinTotalInten;
						double[] proteinIntensity;
						if (proteinIntensityMap.containsKey(proteins[j])) {
							proteinIntensity = proteinIntensityMap.get(proteins[j]);
							proteinTotalInten = proteinTotalIntenMap.get(proteins[j]);
						} else {
							proteinIntensity = new double[pepIntensity.length];
							proteinIntensityMap.put(proteins[j], proteinIntensity);
							proteinTotalInten = 0.0;
						}
						for (int k = 0; k < pepIntensity.length; k++) {
							proteinIntensity[k] += pepIntensity[k];
							proteinTotalInten += pepIntensity[k];
						}
						proteinTotalIntenMap.put(proteins[j], proteinTotalInten);

						if (totalPepCountMap.containsKey(proteins[j])) {
							totalPepCountMap.get(proteins[j]).add(stripeSequence);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(stripeSequence);
							totalPepCountMap.put(proteins[j], set);
						}

						if (proteins[j].startsWith(magDb.getIdentifier())) {
							String genome = proteins[j].split("_")[0];
							if (genomeScoreMap.containsKey(genome)) {
								double[] genomeIntensity;
								if (genomeIntensityMap.containsKey(genome)) {
									genomeIntensity = genomeIntensityMap.get(genome);
								} else {
									genomeIntensity = new double[pepIntensity.length];
									genomeIntensityMap.put(genome, genomeIntensity);
								}
								for (int k = 0; k < pepIntensity.length; k++) {
									genomeIntensity[k] += pepIntensity[k];
								}

								if (psmCountMap.containsKey(genome)) {
									psmCountMap.put(genome, psmCountMap.get(genome) + pepMs2Count);
								} else {
									psmCountMap.put(genome, pepMs2Count);
								}

								if (totalPepCountMap.containsKey(genome)) {
									totalPepCountMap.get(genome).add(stripeSequence);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(stripeSequence);
									totalPepCountMap.put(genome, set);
								}
							}
						}
					}
				}
			}
		}

		String[] proteins = proteinTotalIntenMap.keySet().toArray(String[]::new);
		Arrays.sort(proteins, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				if (proteinTotalIntenMap.get(o1) > proteinTotalIntenMap.get(o2))
					return -1;
				if (proteinTotalIntenMap.get(o1) < proteinTotalIntenMap.get(o2))
					return 1;
				return 0;
			}
		});

		HashMap<String, Integer> proteinIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < proteins.length; i++) {
			proteinIdMap.put(proteins[i], i);
		}

		try (PrintWriter idWriter = new PrintWriter(new File(metaPar.getResult(), "id.tsv"))) {
			idWriter.println("Raw file\tUnique peptide count\tPSM count");

			String[] rawNames = fileIdMap.keySet().toArray(new String[fileIdMap.size()]);
			Arrays.sort(rawNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					return fileIdMap.get(o1) - fileIdMap.get(o2);
				}
			});

			for (int i = 0; i < rawNames.length; i++) {
				idWriter.println(rawNames[i] + "\t" + fileSequenceSets[i].size() + "\t" + filePsmCount[i]);
			}
			idWriter.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing identification information to " + new File(metaPar.getResult(), "id.tsv"));
			LOGGER.error(getTaskName() + ": error in writing identification information to "
					+ new File(metaPar.getResult(), "id.tsv"), e);
		}

		setProgress(85);

		HashMap<String, ArrayList<Integer>> proPepIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> proPepRazorIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, Double> proScoreMap = new HashMap<String, Double>();
		try {
			this.final_pep_txt = new File(metaPar.getResult(), "final_peptides.tsv");

			PrintWriter writer = new PrintWriter(this.final_pep_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Sequence").append("\t");
			titlesb.append("Base sequence").append("\t");
			titlesb.append("Length").append("\t");
			titlesb.append("Missed cleavages").append("\t");
			titlesb.append("Mass").append("\t");
			titlesb.append("Proteins").append("\t");
			titlesb.append("Charges").append("\t");
			titlesb.append("Score").append("\t");
			titlesb.append("PEP").append("\t");

			String[] idenNames = fileIdMap.keySet().toArray(String[]::new);
			Arrays.sort(idenNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					return fileIdMap.get(o1) - fileIdMap.get(o2);
				}
			});

			for (int i = 0; i < idenNames.length; i++) {
				titlesb.append("Identification type " + idenNames[i]).append("\t");
			}

			for (int i = 0; i < idenNames.length; i++) {
				titlesb.append("MS/MS count " + idenNames[i]).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < intensityTitles.length; i++) {
				if (!intensityTitles[i].startsWith("Intensity ")) {
					titlesb.append("Intensity " + intensityTitles[i]).append("\t");
				} else {
					titlesb.append(intensityTitles[i]).append("\t");
				}
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			writer.println(titlesb);

			int id = 0;
			for (SagePeptide peptide : peptideList) {

				String fullSeq = peptide.getModSeq();
				String sequence = peptide.getSequence();

				StringBuilder sb = new StringBuilder();
				sb.append(fullSeq).append("\t");
				sb.append(sequence).append("\t");
				sb.append(sequence.length()).append("\t");
				sb.append(peptide.getMissCleave()).append("\t");
				sb.append(peptide.getMass()).append("\t");

				String[] pros = peptide.getProteins();

				for (String pro : pros) {
					if (proteinIdMap.containsKey(pro)) {
						sb.append(pro).append(";");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				int[] charge = peptide.getCharges();
				StringBuilder chargesb = new StringBuilder();
				for (int j = 0; j < charge.length; j++) {
					if (charge[j] == 1) {
						chargesb.append(j).append(";");
					}
				}
				if (chargesb.length() > 1) {
					sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
				} else {
					sb.append("2").append("\t");
				}

				sb.append(peptide.getScore()).append("\t");
				sb.append(peptide.getPEP()).append("\t");

				int[] idenType = peptide.getIdenType();
				for (int j = 0; j < idenType.length; j++) {
					sb.append(MetaPeptide.idenTypeStrings[idenType[j]]).append("\t");
				}

				int totalMsCount = 0;
				int[] ms2Count = peptide.getMs2Counts();
				for (int j = 0; j < ms2Count.length; j++) {
					totalMsCount += ms2Count[j];
					sb.append(ms2Count[j]).append("\t");
				}

				double[] intensity = peptide.getIntensity();
				double totalIntensity = 0;
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append(BigInteger.valueOf((long) Math.floor(totalIntensity))).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append(BigInteger.valueOf((long) Math.floor(intensity[j]))).append("\t");
				}

				sb.append("").append("\t");
				sb.append("").append("\t");

				sb.append(id).append("\t");

				for (String pro : pros) {
					if (proteinIdMap.containsKey(pro)) {
						sb.append(proteinIdMap.get(pro)).append(";");

						if (proPepIdMap.containsKey(pro)) {
							proPepIdMap.get(pro).add(id);
						} else {
							ArrayList<Integer> list = new ArrayList<Integer>();
							list.add(id);
							proPepIdMap.put(pro, list);
						}

						if (proScoreMap.containsKey(pro)) {
							proScoreMap.put(pro, proScoreMap.get(pro) * peptide.getPEP());
						} else {
							proScoreMap.put(pro, peptide.getPEP());
						}

						HashSet<String> rSet = razorPepCountMap.get(pro);
						if (proPepRazorIdMap.containsKey(pro)) {
							if (rSet.contains(sequence)) {
								proPepRazorIdMap.get(pro).add(1);
							} else {
								proPepRazorIdMap.get(pro).add(0);
							}
						} else {
							ArrayList<Integer> list = new ArrayList<Integer>();
							if (rSet.contains(sequence)) {
								list.add(1);
							} else {
								list.add(0);
							}
							proPepRazorIdMap.put(pro, list);
						}
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				sb.append(totalMsCount);

				writer.println(sb);
				id++;
			}

			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing the peptides to  " + final_pep_txt, e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing the peptides to  "
					+ final_pep_txt);
		}

		LOGGER.info(getTaskName() + ": peptide report has been exported to " + final_pep_txt);
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": peptide report has been exported to "
				+ final_pep_txt);

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		}

		setProgress(87);

		LOGGER.info(getTaskName() + ": exporting protein report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting protein report started");

		ArrayList<MetaProtein> proteinList = new ArrayList<MetaProtein>();
		try {

			this.final_pro_txt = new File(metaPar.getResult(), "final_proteins.tsv");

			PrintWriter writer = new PrintWriter(this.final_pro_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Protein IDs").append("\t");
			titlesb.append("Peptide counts (all)").append("\t");
			titlesb.append("Peptide counts (razor)").append("\t");
			titlesb.append("Number of PSMs").append("\t");
			titlesb.append("PEP").append("\t");
			titlesb.append("Intensity").append("\t");
			for (int i = 0; i < intensityTitles.length; i++) {
				if (!intensityTitles[i].startsWith("Intensity ")) {
					titlesb.append("Intensity " + intensityTitles[i]).append("\t");
				} else {
					titlesb.append(intensityTitles[i]).append("\t");
				}
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Peptide IDs").append("\t");
			titlesb.append("Peptide is razor");

			writer.println(titlesb);

			DecimalFormat df = FormatTool.getDFE4();
			for (int i = 0; i < proteins.length; i++) {

				StringBuilder sb = new StringBuilder();
				sb.append(proteins[i]).append("\t");
				sb.append(totalPepCountMap.get(proteins[i]).size()).append("\t");
				sb.append(razorPepCountMap.get(proteins[i]).size()).append("\t");
				sb.append(psmCountMap.get(proteins[i])).append("\t");
				sb.append(proScoreMap.get(proteins[i])).append("\t");

				double totalIntensity = proteinTotalIntenMap.get(proteins[i]);
				sb.append(df.format(totalIntensity)).append("\t");

				double[] intensity = proteinIntensityMap.get(proteins[i]);
				for (int j = 0; j < intensity.length; j++) {
					sb.append(df.format(intensity[j])).append("\t");
				}
				sb.append("\t");
				sb.append("\t");
				sb.append(i).append("\t");

				ArrayList<Integer> pepIdList = proPepIdMap.get(proteins[i]);
				ArrayList<Integer> pepRazorList = proPepRazorIdMap.get(proteins[i]);

				for (int j = 0; j < pepIdList.size(); j++) {
					sb.append(pepIdList.get(j)).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				for (int j = 0; j < pepRazorList.size(); j++) {
					if (pepRazorList.get(j) == 1) {
						sb.append("true;");
					} else {
						sb.append("false;");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				writer.println(sb);

				MetaProtein mp = new MetaProtein(i, 0, proteins[i], totalPepCountMap.get(proteins[i]).size(),
						psmCountMap.get(proteins[i]), proScoreMap.get(proteins[i]), intensity);
				proteinList.add(mp);
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing final protein result to " + this.final_pro_txt.getName(),
					e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing final protein result to " + this.final_pro_txt.getName());
		}

		LOGGER.info(getTaskName() + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein report has been exported to "
				+ final_pro_txt.getName());

		setProgress(89);

		LOGGER.info(getTaskName() + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation started");

		File funcFile = new File(metaPar.getResult(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		File funTsv = new File(funcFile, "functions.tsv");
		File funReportTsv = new File(funcFile, "functions_report.tsv");
		final_pro_xml_file = new File(funcFile, "functions.xml");

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();
		MetaProtein[] metapros = proteinList.toArray(new MetaProtein[proteinList.size()]);
		MetaProteinAnnoMag[] mpas = funcAnnoSqlite(metapros, intensityTitles);
		for (int i = 0; i < mpas.length; i++) {
			String name = mpas[i].getPro().getName();
			if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
				proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
			} else {
				proTaxIdMap.put(name, 131567);
			}
		}

		MetaProteinXMLReader2 funReader = null;
		try {
			funReader = new MetaProteinXMLReader2(final_pro_xml_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading protein function information from "
					+ this.final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading protein function information from " + this.quan_pro_file.getName());
			return false;
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

		File html = new File(funcFile, "functions.html");
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

		setProgress(91);

		// taxonomy analysis

		LOGGER.info(getTaskName() + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis started");

		File taxFile = new File(metaPar.getResult(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		MagFuncSearcher searcher = null;
		try {
			searcher = new MagFuncSearcher(metaPar.getMicroDb(), msv.getFuncDef());

		} catch (NumberFormatException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in genome analysis, task failed", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in genome analysis, task failed");

			return false;
		}

		String[] genomes = genomeIntensityMap.keySet().toArray(new String[genomeIntensityMap.size()]);
		HashMap<String, String[]> genomeTaxaMap = null;
		try {
			genomeTaxaMap = searcher.matchGenome(genomes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": Error in searching the genome2taxa database", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in searching the genome2taxa database");
			return false;
		}

		File genomeHtmlFile = new File(taxFile, "Genome.html");
		try {
			HMGenomeHtmlWriter htmlWriter = new HMGenomeHtmlWriter(genomeHtmlFile);
			htmlWriter.write(genomeTaxaMap);
			htmlWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeHtmlFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeHtmlFile);
			return false;
		}

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		double[] cellularIntensity = new double[intensityTitles.length];
		HashMap<String, String[]>[] taxRankMaps = new HashMap[mainRanks.length];
		HashMap<String, double[]>[] taxIntensityMaps = new HashMap[mainRanks.length];
		HashMap<String, Double>[] taxScoreMaps = new HashMap[mainRanks.length];
		for (int i = 0; i < taxRankMaps.length; i++) {
			taxRankMaps[i] = new HashMap<String, String[]>();
			taxIntensityMaps[i] = new HashMap<String, double[]>();
			taxScoreMaps[i] = new HashMap<String, Double>();
		}

		File genomeFile = new File(taxFile, "Genome.tsv");
		PrintWriter genomeWriter = null;
		try {
			genomeWriter = new PrintWriter(genomeFile);
			StringBuilder genomeTitlesb = new StringBuilder();
			genomeTitlesb.append("Genome\t");
			genomeTitlesb.append("Minus_log(p-value)\t");
			genomeTitlesb.append("PSM count\t");
			genomeTitlesb.append("Peptide count\t");
			genomeTitlesb.append("Razor peptide count\t");
			genomeTitlesb.append("GTDB_Name\t");
			genomeTitlesb.append("GTDB_Rank\t");

			for (int i = 0; i < mainRanks.length; i++) {
				genomeTitlesb.append("GTDB_").append(mainRanks[i].getName()).append("\t");
			}

			genomeTitlesb.append("NCBI_Name\t");
			genomeTitlesb.append("NCBI_Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				genomeTitlesb.append("\t").append("NCBI_").append(mainRanks[i].getName());
			}

			for (String exp : expNames) {
				if (exp.startsWith("Intensity ")) {
					genomeTitlesb.append("\t").append(exp);
				} else {
					genomeTitlesb.append("\tIntensity ").append(exp);
				}
			}

			genomeWriter.println(genomeTitlesb);

			setProgress(94);

			for (int i = 0; i < genomes.length; i++) {
				String[] taxaContent = genomeTaxaMap.get(genomes[i]);
				if (genomeIntensityMap.containsKey(genomes[i])) {
					double[] genomeIntensity = genomeIntensityMap.get(genomes[i]);
					for (int j = 0; j < genomeIntensity.length; j++) {
						cellularIntensity[j] += genomeIntensity[j];
					}

					double score = genomeScoreMap.get(genomes[i]);
					StringBuilder genomeTaxaSb = new StringBuilder();
					genomeTaxaSb.append(genomes[i]).append("\t");

					genomeTaxaSb.append(FormatTool.getDF2().format(-Math.log10(score))).append("\t");

					if (psmCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(psmCountMap.get(genomes[i])).append("\t");
					} else {
						genomeTaxaSb.append("0").append("\t");
					}
					if (totalPepCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(totalPepCountMap.get(genomes[i]).size()).append("\t");
					} else {
						genomeTaxaSb.append("0").append("\t");
					}
					if (razorPepCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(razorPepCountMap.get(genomes[i]).size());
					} else {
						genomeTaxaSb.append("0");
					}

					for (int j = 1; j < taxaContent.length; j++) {
						genomeTaxaSb.append("\t").append(taxaContent[j]);
					}

					for (int j = 0; j < genomeIntensity.length; j++) {
						genomeTaxaSb.append("\t").append(genomeIntensity[j]);
					}
					genomeWriter.println(genomeTaxaSb);

					for (int j = 0; j < taxRankMaps.length; j++) {
						String taxon = taxaContent[j + 3];
						if (taxon == null || taxon.length() == 0) {
							continue;
						}
						if (!taxRankMaps[j].containsKey(taxon)) {
							String[] lineage = new String[j + 1];
							System.arraycopy(taxaContent, 3, lineage, 0, lineage.length);
							taxRankMaps[j].put(taxon, lineage);
						}

						double[] taxIntensity;
						if (taxIntensityMaps[j].containsKey(taxon)) {
							taxIntensity = taxIntensityMaps[j].get(taxon);
						} else {
							taxIntensity = new double[genomeIntensity.length];
						}

						for (int k = 0; k < taxIntensity.length; k++) {
							taxIntensity[k] += genomeIntensity[k];
						}
						taxIntensityMaps[j].put(taxon, taxIntensity);

						if (taxScoreMaps[j].containsKey(taxon)) {
							taxScoreMaps[j].put(taxon, taxScoreMaps[j].get(taxon) - Math.log10(score));
						} else {
							taxScoreMaps[j].put(taxon, -Math.log10(score));
						}
					}
				}
			}
			genomeWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeFile, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeFile);
			return false;
		}

		File taxaFile = new File(taxFile, "Taxa.tsv");
		File taxaReportFile = new File(taxFile, "Taxa_report.tsv");
		PrintWriter taxaWriter = null;
		PrintWriter taxaReportWriter = null;
		try {
			taxaWriter = new PrintWriter(taxaFile);
			taxaReportWriter = new PrintWriter(taxaReportFile);

			StringBuilder taxaTitlesb1 = new StringBuilder();
			taxaTitlesb1.append("Name\t");
			taxaTitlesb1.append("Minus_log(p-value)\t");
			taxaTitlesb1.append("Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				taxaTitlesb1.append("\t").append(mainRanks[i].getName());
			}
			for (String exp : expNames) {
				if (exp.startsWith("Intensity ")) {
					taxaTitlesb1.append("\t").append(exp);
				} else {
					taxaTitlesb1.append("\tIntensity ").append(exp);
				}
			}
			StringBuilder taxaTitlesb2 = new StringBuilder();
			taxaTitlesb2.append("Name\t");
			taxaTitlesb2.append("Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				taxaTitlesb2.append("\t").append(mainRanks[i].getName());
			}
			for (String exp : expNames) {
				if (exp.startsWith("Intensity ")) {
					taxaTitlesb2.append("\t").append(exp);
				} else {
					taxaTitlesb2.append("\tIntensity ").append(exp);
				}
			}
			taxaWriter.println(taxaTitlesb1);
			taxaReportWriter.println(taxaTitlesb2);

			for (int i = 0; i < taxRankMaps.length; i++) {
				for (String taxon : taxRankMaps[i].keySet()) {
					String[] taxaContent = taxRankMaps[i].get(taxon);
					double[] intensity = taxIntensityMaps[i].get(taxon);

					StringBuilder taxaSb = new StringBuilder();
					taxaSb.append(taxon).append("\t");

					double taxonScore = taxScoreMaps[i].get(taxon);
					taxaSb.append(FormatTool.getDF2().format(taxonScore)).append("\t");

					taxaSb.append(mainRanks[i].getName());

					for (int j = 0; j < mainRanks.length; j++) {
						if (j < taxaContent.length) {
							taxaSb.append("\t").append(taxaContent[j]);
						} else {
							taxaSb.append("\t");
						}
					}

					for (int j = 0; j < intensity.length; j++) {
						taxaSb.append("\t").append(intensity[j]);
					}

					taxaWriter.println(taxaSb);
				}
			}

			taxaWriter.close();

			for (int i = 0; i < taxRankMaps.length; i++) {
				for (String taxon : taxRankMaps[i].keySet()) {
					String[] taxaContent = taxRankMaps[i].get(taxon);
					double[] intensity = taxIntensityMaps[i].get(taxon);

					StringBuilder taxaSb = new StringBuilder();
					taxaSb.append(taxon).append("\t");
					taxaSb.append(mainRanks[i].getName());

					for (int j = 0; j < mainRanks.length; j++) {
						if (j < taxaContent.length) {
							taxaSb.append("\t").append(taxaContent[j]);
						} else {
							taxaSb.append("\t");
						}
					}
					for (int j = 0; j < intensity.length; j++) {
						taxaSb.append("\t").append(intensity[j]);
					}

					taxaReportWriter.println(taxaSb);
				}
			}

			taxaReportWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxa information to " + taxaFile, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing taxa information to " + taxaFile);
		}

		LOGGER.info(getTaskName() + ": taxonomy analysis finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis finished");

		setProgress(95);

		bar2.setString(getTaskName() + ": exporting report");

		LOGGER.info(getTaskName() + ": exporting report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting report started");

		File reportDir = new File(metaPar.getResult(), "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, getVersion());

		metaPar.setReportDir(reportDir);

		this.reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportHtmlDir.exists()) {
			reportHtmlDir.mkdirs();
		}

		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}

		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(taxRankMaps, taxIntensityMaps, cellularIntensity, expNames, jsOutput);
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

		this.summaryMetaFile = new File(metaPar.getResult(), "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.exportSummary(task);

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		} else {
			File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
			if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary);
				}
			}
		}

		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			return false;
		} else {
			File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
			if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary);
				}
			}
		}

		if (!taxaReportFile.exists() || taxaReportFile.length() == 0) {
			return false;
		} else {
			File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
			if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

				if (summaryMetaFile != null && summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary);
				}
			}
		}

		if (!funReportTsv.exists() || funReportTsv.length() == 0) {
			return false;
		} else {
			File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");
			if (!report_function_summary.exists() || report_function_summary.length() == 0) {

				if (summaryMetaFile != null && summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.function, funReportTsv, report_function_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
				}
			}
		}

		task.run();

		try {
			boolean finish = task.get();
			if (finish) {

				LOGGER.info(getTaskName() + ": report generation successes");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": report generation successes");

				setProgress(100);
				return true;
			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in the report generation", e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in the report generation");
		}

		return true;
	}

	protected boolean exportPepProTxt() {

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		SageResultReader pepReader = new SageResultReader(resultFolderFile);
		SagePeptide[] peps = pepReader.getMetaPeptides();
		HashMap<String, Integer> hostProMap = new HashMap<String, Integer>();

		HashSet<String> magPepSet = new HashSet<String>();
		HashMap<String, HashSet<String>> genomePepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> totalPepProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < peps.length; i++) {
			if (peps[i].getPEP() > 0.01) {
				continue;
			}
			String stripSeq = peps[i].getSequence();
			String[] pros = peps[i].getProteins();
			for (int j = 0; j < pros.length; j++) {
				if (totalPepProMap.containsKey(stripSeq)) {
					totalPepProMap.get(stripSeq).add(pros[j]);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(pros[j]);
					totalPepProMap.put(stripSeq, set);
				}
				if (pros[j].startsWith(magDb.getIdentifier())) {
					magPepSet.add(stripSeq);
					String genome = pros[j].split("_")[0];
					if (genomePepMap.containsKey(genome)) {
						genomePepMap.get(genome).add(stripSeq);
					} else {
						HashSet<String> pSet = new HashSet<String>();
						pSet.add(stripSeq);
						genomePepMap.put(genome, pSet);
					}
				} else {
					if (hostProMap.containsKey(pros[j])) {
						hostProMap.put(pros[j], hostProMap.get(pros[j]) + 1);
					} else {
						hostProMap.put(pros[j], 1);
					}
				}
			}
		}

		File magsFile = new File(resultFolderFile, "mags.tsv");
		HashSet<String> genomeSet = refineGenomes(genomePepMap, magPepSet.size(), 1.0, magsFile);

		LOGGER.info(getTaskName() + ": filtered genome count " + genomeSet.size());
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": filtered genome count " + genomeSet.size());

		HashMap<String, HashSet<String>> magProPepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < peps.length; i++) {
			if (peps[i].getPEP() > 0.01) {
				continue;
			}
			String stripSeq = peps[i].getSequence();
			String[] pros = peps[i].getProteins();
			for (int j = 0; j < pros.length; j++) {
				if (pros[j].startsWith(magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					if (genomeSet.contains(genome)) {
						if (magProPepMap.containsKey(pros[j])) {
							magProPepMap.get(pros[j]).add(stripSeq);
						} else {
							HashSet<String> pSet = new HashSet<String>();
							pSet.add(stripSeq);
							magProPepMap.put(pros[j], pSet);
						}
					}
				}
			}
		}

		File razorProFile = new File(resultFolderFile, "razorPro.tsv");
		HashSet<String> razorProSet = refineProtein(magProPepMap, magPepSet.size(), razorProFile);

		LOGGER.info(getTaskName() + ": filtered microbial protein count " + razorProSet.size());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": filtered microbial protein count "
				+ razorProSet.size());

		razorProSet.addAll(hostProMap.keySet());

		HashMap<String, Integer> proIdMap = new HashMap<String, Integer>();
		try (BufferedReader reader = new BufferedReader(new FileReader(razorProFile))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (razorProSet.contains(cs[1])) {
					proIdMap.put(cs[1], proIdMap.size());
				}
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(getTaskName() + ": error in reading razor protein file " + razorProFile, e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading razor protein file " + razorProFile);
		}

		if (hostProMap.size() > 0) {
			// Convert the HashMap into a list of entries and sort it by values
			List<Map.Entry<String, Integer>> sortedList = hostProMap.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList());
			String[] hostPros = sortedList.stream().map(Map.Entry::getKey).toArray(String[]::new);
			for (String hostPro : hostPros) {
				proIdMap.put(hostPro, proIdMap.size());
			}
		}

		HashMap<String, HashSet<String>> razorProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> allProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, Integer> pepIdMap = new HashMap<String, Integer>();
		HashMap<String, String[]> pepProMap = new HashMap<String, String[]>();
		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		HashMap<String, ArrayList<Double>> proScoreMap = new HashMap<String, ArrayList<Double>>();

		for (int i = 0; i < peps.length; i++) {
			if (peps[i].getPEP() > 0.01) {
				continue;
			}
			String seq = peps[i].getModSeq();
			String[] pros = peps[i].getProteins();

			ArrayList<String> tempList = new ArrayList<String>();
			for (int j = 0; j < pros.length; j++) {
				if (proIdMap.containsKey(pros[j])) {
					tempList.add(pros[j]);
				}
			}

			if (tempList.size() > 0) {

				String[] tempPros = tempList.toArray(String[]::new);
				Arrays.sort(tempPros, new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						return proIdMap.get(s1) - proIdMap.get(s2);
					}
				});

				if (razorProPepMap.containsKey(tempPros[0])) {
					razorProPepMap.get(tempPros[0]).add(seq);
				} else {
					HashSet<String> razorSet = new HashSet<String>();
					razorSet.add(seq);
					razorProPepMap.put(tempPros[0], razorSet);
				}
				double[] pepIntensity = peps[i].getIntensity();

				for (int j = 0; j < tempPros.length; j++) {
					double[] proIntensity;
					if (proIntensityMap.containsKey(tempPros[j])) {
						proIntensity = proIntensityMap.get(tempPros[j]);
					} else {
						proIntensity = new double[pepIntensity.length];
					}

					for (int k = 0; k < proIntensity.length; k++) {
						proIntensity[k] += pepIntensity[k] / (double) tempPros.length;
					}
					proIntensityMap.put(tempPros[j], proIntensity);

					ArrayList<Double> scoreList;
					if (proScoreMap.containsKey(tempPros[j])) {
						scoreList = proScoreMap.get(tempPros[j]);
					} else {
						scoreList = new ArrayList<Double>();
					}
					scoreList.add(peps[i].getScore());
					proScoreMap.put(tempPros[j], scoreList);

					HashSet<String> pepSet;
					if (allProPepMap.containsKey(tempPros[j])) {
						pepSet = allProPepMap.get(tempPros[j]);
					} else {
						pepSet = new HashSet<String>();
						allProPepMap.put(tempPros[j], pepSet);
					}
					pepSet.add(seq);
				}
				pepIdMap.put(seq, pepIdMap.size());
				pepProMap.put(seq, tempPros);
			}
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
			titlesb.append("PEP").append("\t");

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
				if (peps[i].getPEP() > 0.01) {
					continue;
				}
				String sequence = peps[i].getSequence();
				String modSeqString = peps[i].getModSeq();
				if (!pepIdMap.containsKey(modSeqString)) {
					continue;
				}

				StringBuilder sb = new StringBuilder();
				sb.append(modSeqString).append("\t");
				sb.append(sequence).append("\t");
				sb.append(peps[i].getLength()).append("\t");

				String[] proteins = pepProMap.get(modSeqString);
				for (String pro : proteins) {
					sb.append(pro).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
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

				sb.append(FormatTool.getDF4().format(peps[i].getScore())).append("\t");

				int[] idenType = peps[i].getIdenType();

				for (int j = 0; j < idenType.length; j++) {
					sb.append(MetaPeptide.idenTypeStrings[idenType[j]]).append("\t");
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

				sb.append(pepIdMap.get(modSeqString)).append("\t");

				for (String pro : proteins) {
					sb.append(proIdMap.get(pro)).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				int totalSpCount = 0;
				int[] spCount = peps[i].getMs2Counts();
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
			return false;
		}

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Protein IDs").append("\t");
		titlesb.append("Majority protein IDs").append("\t");
		titlesb.append("Peptide counts (all)").append("\t");
		titlesb.append("Peptide counts (razor)").append("\t");
		titlesb.append("Number of proteins").append("\t");
		titlesb.append("Peptides").append("\t");
		titlesb.append("PEP").append("\t");
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

		List<Map.Entry<String, Integer>> sortedList = proIdMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toList());
		String[] pros = sortedList.stream().map(Map.Entry::getKey).toArray(String[]::new);

		for (int i = 0; i < pros.length; i++) {

			HashSet<String> allPepSet = allProPepMap.get(pros[i]);
			HashSet<String> razorPepSet = razorProPepMap.get(pros[i]);

			if (allPepSet == null || razorPepSet == null) {
				continue;
			}

			int pepCountAll = allProPepMap.get(pros[i]).size();
			int pepCountRazor = razorProPepMap.get(pros[i]).size();

			StringBuilder sb = new StringBuilder();
			sb.append(pros[i]).append("\t");
			sb.append(pros[i]).append("\t");
			sb.append(pepCountAll).append("\t");
			sb.append(pepCountRazor).append("\t");
			sb.append("1\t");
			sb.append(allPepSet.size()).append("\t");

			double proScore = 1.0;
			ArrayList<Double> scoreList = proScoreMap.get(pros[i]);
			for (int j = 0; j < scoreList.size(); j++) {
				proScore = proScore * scoreList.get(j);
			}

			sb.append(FormatTool.getDF4().format(proScore)).append("\t");

			double totalIntensity = 0;
			double[] intensity = proIntensityMap.get(pros[i]);
			for (int j = 0; j < intensity.length; j++) {
				totalIntensity += intensity[j];
			}

			sb.append(totalIntensity).append("\t");
			for (int j = 0; j < intensity.length; j++) {
				sb.append(intensity[j]).append("\t");
			}
			sb.append("\t").append("\t");
			sb.append(proIdMap.get(pros[i])).append("\t");

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

		proWriter.close();

		LOGGER.info(getTaskName() + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein report has been exported to "
				+ final_pro_txt.getName());

		return true;
	}

	protected HashSet<String> refineProtein(HashMap<String, HashSet<String>> proPepMap, int totalPepCount, File out) {

		String[] proteins = proPepMap.keySet().toArray(new String[proPepMap.size()]);
		Arrays.sort(proteins, (g1, g2) -> {
			int s1 = proPepMap.get(g1).size();
			int s2 = proPepMap.get(g2).size();
			return s2 - s1;
		});

		for (int iteratorCount = 0; iteratorCount < 5; iteratorCount++) {
			HashSet<String> totalSet = new HashSet<String>();
			HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
			for (int i = 0; i < proteins.length; i++) {
				int currentSize = totalSet.size();
				totalSet.addAll(proPepMap.get(proteins[i]));
				int increaseSize = totalSet.size() - currentSize;
				orderMap.put(proteins[i], increaseSize);
			}

			Arrays.sort(proteins, (g1, g2) -> {
				return orderMap.get(g2) - orderMap.get(g1);
			});
		}

		double[] percentage = new double[proteins.length];
		HashSet<String> totalSet = new HashSet<String>();
		int breakPoint1 = -1;
		HashSet<String> proSet = new HashSet<String>();
		try (PrintWriter writer = new PrintWriter(out)) {
			writer.println("Rank\tProtein\tTotal_covered_PSM_count\tPercentage\tIncrement\tIncrement ratio");
			for (int i = 0; i < proteins.length; i++) {

				int currentSize = totalSet.size();
				totalSet.addAll(proPepMap.get(proteins[i]));
				int add = totalSet.size() - currentSize;
				double additional = ((double) add / (double) totalPepCount);
				if (add > 0) {
					breakPoint1 = i;
				}

				percentage[i] = ((double) totalSet.size() / (double) totalPepCount);

				writer.println(i + "\t" + proteins[i] + "\t" + totalSet.size() + "\t" + percentage[i] + "\t" + add
						+ "\t" + additional);
			}
			writer.close();
		} catch (IOException e) {
			LOGGER.error(getTaskName() + ": error in writing genomes to " + out, e);
			System.out
					.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing genomes to " + out);
		}

		for (int i = 0; i < proteins.length; i++) {
			proSet.add(proteins[i]);
			if (i == breakPoint1) {
				break;
			}
		}

		return proSet;
	}

	protected boolean exportSummary(MetaReportTask task) {

		this.final_summary_txt = new File(metaPar.getResult(), "final_summary.tsv");
		try {
			HashMap<String, int[]> idMap = new HashMap<String, int[]>();
			MetaData metaData = metaPar.getMetadata();
			File rawFolder = (new File(metaData.getRawFiles()[0])).getParentFile();
			File magResultDir = new File(metaPar.getResult(), "mag_result");
			File[] magResultFiles = magResultDir.listFiles();
			for (int i = 0; i < magResultFiles.length; i++) {
				if (magResultFiles[i].isDirectory()) {
					int[] counts = new int[3];
					String resultName = null;
					File[] subFiles = magResultFiles[i].listFiles();
					HashSet<String> pepSet = new HashSet<String>();

					for (int j = 0; j < subFiles.length; j++) {
						String fileNameString = subFiles[j].getName();
						if (fileNameString.equals("psm.tsv")) {
							BufferedReader psmReader = new BufferedReader(new FileReader(subFiles[j]));
							String psmLine = psmReader.readLine();
							while ((psmLine = psmReader.readLine()) != null) {
								counts[1]++;
								String[] cs = psmLine.split("\t");
								pepSet.add(cs[2]);
							}
							psmReader.close();
						}
						if (fileNameString.endsWith(".pepXML")) {
							resultName = fileNameString.substring(0, fileNameString.length() - ".pepXML".length());
						}
					}
					counts[2] = pepSet.size();

					if (resultName != null) {
						File mzmlFile = new File(rawFolder, resultName + "_uncalibrated.mzML");
						if (mzmlFile.exists()) {
							BufferedReader mzmlReader = new BufferedReader(new FileReader(mzmlFile));
							String mzmlLine = null;
							while ((mzmlLine = mzmlReader.readLine()) != null) {
								if (mzmlLine.trim().equals("</spectrum>")) {
									counts[0]++;
								}
							}
							mzmlReader.close();
						}
					}

					idMap.put(resultName, counts);
				}
			}

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

			for (int i = 0; i < this.rawFiles.length; i++) {
				File rawFile = new File(this.rawFiles[i]);
				String rawNameString = rawFile.getName();
				rawNameString = rawNameString.substring(0, rawNameString.lastIndexOf("."));

				if (idMap.containsKey(rawNameString)) {

					StringBuilder sb = new StringBuilder();
					sb.append(rawNameString).append("\t");
					sb.append(this.expNames[i]).append("\t");
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
					sb.append(this.expNames[i]).append("\t");
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

		MetaProteinTsvReader proReader = new MetaProteinTsvReader(this.final_pro_txt);
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
			return false;
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

		MetaPeptideTsvReader pepReader = new MetaPeptideTsvReader(this.final_pep_txt);
		MetaPeptide[] peptides = pepReader.getMetaPeptides();
		HashMap<String, double[]> pepIntenMap = new HashMap<String, double[]>();
		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> pepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> uniquePepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, ArrayList<Double>> genomePEPMap = new HashMap<String, ArrayList<Double>>();

		for (int i = 0; i < peptides.length; i++) {
			String sequence = peptides[i].getSequence();
			double[] pepIntensity = peptides[i].getIntensity();
			pepIntenMap.put(sequence, pepIntensity);

			double PEP = peptides[i].getScore();

			int psmCount = 0;
			int[] ms2Count = peptides[i].getMs2Counts();
			for (int j = 0; j < expNames.length; j++) {
				psmCount += ms2Count[j];
			}

			String[] pros = peptides[i].getProteins();
			String razorPro = pros[0];

			if (razorPro.startsWith(this.magDb.getIdentifier())) {
				String genome = razorPro.split("_")[0];
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

				double[] totalIntensity;
				if (genomeIntensityMap.containsKey(genome)) {
					totalIntensity = genomeIntensityMap.get(genome);
				} else {
					totalIntensity = new double[pepIntensity.length];
				}
				for (int j = 0; j < totalIntensity.length; j++) {
					totalIntensity[j] += pepIntensity[j];
				}
				genomeIntensityMap.put(genome, totalIntensity);

				ArrayList<Double> qList;
				if (genomePEPMap.containsKey(genome)) {
					qList = genomePEPMap.get(genome);
				} else {
					qList = new ArrayList<Double>();
					genomePEPMap.put(genome, qList);
				}
				qList.add(PEP);
			}
		}

		return exportGenome(genomeIntensityMap, psmCountMap, pepCountMap, uniquePepCountMap, genomePEPMap, taxFile,
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
