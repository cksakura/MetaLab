package bmi.med.uOttawa.metalab.task.hgm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.MGYG.db.HGMSqliteSearcher;
import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvWriter;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindProtein;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader.PeptideResult;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindTask;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParaIOHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParameterHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLWriter2;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.pfind.MetaIdenPFindTask;

/**
 * Peptide/protein identification and quantification of human gut microbiome
 * samples in pFind+HAP(high abundant protein) workflow
 * 
 * @author Kai Cheng
 *
 */
public class HgmHapPFindTask extends MetaIdenPFindTask {

	private String originalDbString = "original_db";
	private String hapDbString = "rib_elon.fasta";
	private String hostDbString = "uniprot.Homo_sapiens.fasta";

	private File originalDbFile;
	private File hapDbFile;
	private File hostDbFile;

	private File[] separateResultFolders;

	private double iteratorThreshold = 0.01;
	private int iteratorCountLimit = 200;

	private Logger LOGGER = LogManager.getLogger(HgmHapPFindTask.class);
	private String taskName = "Peptide identification and quantification using high abundant protein database";

	public HgmHapPFindTask(MetaParameterHGM metaPar, MetaSourcesHGM msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv, bar1, bar2, nextWork);

		String dbFolder = metaPar.getCurrentDb();
		this.originalDbFile = new File(dbFolder, originalDbString);
		this.hapDbFile = new File(dbFolder, hapDbString);
		this.hostDbFile = new File(dbFolder, hostDbString);

		MetaData metadata = this.metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();

		this.separateResultFolders = new File[rawFiles.length];

		for (int i = 0; i < rawFiles.length; i++) {
			String name = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1, rawFiles[i].lastIndexOf("."));
			this.separateResultFolders[i] = new File(pFindResultFile, name);
			this.separateResultFolders[i].mkdir();
		}
		this.rawDir = (new File(rawFiles[0])).getParentFile();
		super.taskName = this.taskName;
	}

	private void separateSearchHap() throws IOException {

		int taskCount = 0;
		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			if (!hapFile.exists()) {
				hapFile.mkdirs();
			}

			PFindResultReader reader = new PFindResultReader(hapFile, null);
			if (reader.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": pFind search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(taskName + ": pFind search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			} else {
				taskCount++;
			}
		}
		
		if (taskCount > 0) {

			int totalThreadCount = metaPar.getThreadCount();
			int threadCount = totalThreadCount > taskCount ? totalThreadCount / taskCount : 1;
			int threadPoolCount = totalThreadCount > taskCount ? taskCount : totalThreadCount;
			pFindTask.setTotalThread(threadCount);

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File hapFile = new File(separateResultFolders[i], "hap");
				File pf2 = new File(spectraDirFile, separateResultFolders[i].getName() + suffix);

				if (pf2.exists()) {
					pFindTask.searchPF2(new String[] { pf2.getAbsolutePath() }, hapFile.getAbsolutePath(),
							hapDbFile.getAbsolutePath(), ms2Type, isOpenSearch, true,
							mosp.getOpenPsmFDR(), ((MetaParameterHGM) this.metaPar).getFixMods(),
							((MetaParameterHGM) this.metaPar).getVariMods(),
							((MetaParameterHGM) this.metaPar).getEnzyme(),
							((MetaParameterHGM) this.metaPar).getMissCleavages(),
							((MetaParameterHGM) this.metaPar).getDigestMode(),
							((MetaParameterHGM) this.metaPar).getIsobaricTag());
				} else {

					File mgFile = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
					if (mgFile.exists()) {
						pFindTask.searchMgf(new String[] { mgFile.getAbsolutePath() }, hapFile.getAbsolutePath(),
								hapDbFile.getAbsolutePath(), ms2Type, isOpenSearch, true,
								mosp.getOpenPsmFDR(), ((MetaParameterHGM) this.metaPar).getFixMods(),
								((MetaParameterHGM) this.metaPar).getVariMods(),
								((MetaParameterHGM) this.metaPar).getEnzyme(),
								((MetaParameterHGM) this.metaPar).getMissCleavages(),
								((MetaParameterHGM) this.metaPar).getDigestMode(),
								((MetaParameterHGM) this.metaPar).getIsobaricTag(), false);
					} else {
						System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra file " + pf2
								+ " was not found");

						LOGGER.error(taskName + ": spectra file " + pf2 + " was not found");

						throw new IOException("Spectra file " + pf2 + " was not found ");
					}
				}
			}

			pFindTask.run(threadPoolCount, separateResultFolders.length * 3);
		}
		
		
		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			PFindTask.cleanPFindResultFolder(hapFile, true);
		}
	}

	private void pfindFastaExtract4Iterator() throws IOException {

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File dbFile = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".fasta");

			if (dbFile.exists() && dbFile.length() > 0) {

				System.out.println(format.format(new Date()) + "\t" + taskName + ": sample-specific database for "
						+ separateResultFolders[i].getName() + " already exists in " + dbFile.getAbsolutePath());

				LOGGER.info(taskName + ": sample-specific database for " + separateResultFolders[i].getName()
						+ " already exists in " + dbFile.getAbsolutePath());

				continue;
			}

			File hapFile = new File(separateResultFolders[i], "hap");
			File spectraFile = new File(hapFile, "pFind-Filtered.spectra");
			if (!spectraFile.exists()) {

				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": high abundant database search result file " + spectraFile + " was not found");

				LOGGER.error(
						taskName + ": high abundant database search result file " + spectraFile + " was not found");

				throw new IOException("High abundant database search result file " + spectraFile + " was not found");
			}

			HashMap<String, HashSet<String>> hapGenomeMap = new HashMap<String, HashSet<String>>();
			int psmCount = 0;

			BufferedReader hapReader = new BufferedReader(new FileReader(spectraFile));
			String line = hapReader.readLine();

			while ((line = hapReader.readLine()) != null) {
				psmCount++;
				String[] cs = line.split("\t");
				String[] pros = cs[12].split("/");
				for (int j = 0; j < pros.length; j++) {
					if (!pros[j].startsWith("REV_")) {
						String genome = pros[j].split("_")[0];
						if (hapGenomeMap.containsKey(genome)) {
							hapGenomeMap.get(genome).add(cs[0]);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(cs[0]);
							hapGenomeMap.put(genome, set);
						}
					}
				}
			}
			hapReader.close();

			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": start generating sample-specific database for " + separateResultFolders[i].getName());

			LOGGER.info(
					taskName + ": start generating sample-specific database for " + separateResultFolders[i].getName());

			HashSet<String> allGenomeSet = new HashSet<String>();
			allGenomeSet.addAll(hapGenomeMap.keySet());

			refineGenomes(hapGenomeMap, psmCount, 0);

			PrintWriter writer = new PrintWriter(dbFile);

			File[] files = originalDbFile.listFiles();
			for (int j = 0; j < files.length; j++) {
				if (files[j].isFile() && files[j].getName().endsWith(".faa")) {

					String geneJ = files[j].getName();
					geneJ = geneJ.substring(0, geneJ.length() - ".faa".length());

					if ((allGenomeSet.contains(geneJ) && !hapGenomeMap.containsKey(geneJ))) {
						BufferedReader readeri = new BufferedReader(new FileReader(files[j]));
						String linei = null;
						while ((linei = readeri.readLine()) != null) {
							writer.println(linei);
						}
						readeri.close();
					}
				}
			}

			if (metaPar.isAppendHostDb()) {
				BufferedReader hostReader = new BufferedReader(new FileReader(this.hostDbFile));
				String linei = null;
				while ((linei = hostReader.readLine()) != null) {
					writer.println(linei);
				}
				hostReader.close();
			}

			writer.close();

			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": generating sample-specific database for " + separateResultFolders[i].getName() + " finished");

			LOGGER.info(taskName + ": generating sample-specific database for " + separateResultFolders[i].getName()
					+ " finished");
		}
	}

	private void refineGenomes(HashMap<String, HashSet<String>> genomeMap, int totalCount, int iteratorCount) {

		if (genomeMap.size() == 0) {
			return;
		}

		if (iteratorCount++ == this.iteratorCountLimit) {
			return;
		}

		String genome = "";
		int psmCount = 0;
		for (String key : genomeMap.keySet()) {
			int size = genomeMap.get(key).size();
			if (size > psmCount) {
				genome = key;
				psmCount = size;
			}
		}

		HashSet<String> psmSet = genomeMap.get(genome);
		genomeMap.remove(genome);

		HashSet<String> remainSet = new HashSet<String>();
		for (String key : genomeMap.keySet()) {
			HashSet<String> set = genomeMap.get(key);
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String psm = it.next();
				if (psmSet.contains(psm)) {
					it.remove();
				} else {
					remainSet.add(psm);
				}
			}
		}

		if (remainSet.size() < totalCount * this.iteratorThreshold) {
			return;
		} else {
			refineGenomes(genomeMap, totalCount, iteratorCount);
		}
	}

	private void separateSearch() throws IOException {

		File spectraDir = metaPar.getSpectraFile();
		int taskCount = 0;
		for (int i = 0; i < this.separateResultFolders.length; i++) {

			PFindResultReader reader = new PFindResultReader(separateResultFolders[i], null);
			if (reader.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": pFind search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(taskName + ": pFind search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			} else {
				File db = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".fasta");
				if (!db.exists()) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": sample-specific database" + db
							+ " was not found");

					LOGGER.error(taskName + ": sample-specific database" + db + " was not found");
					throw new IOException("Sample-specific database" + db + " was not found");
				}

				taskCount++;
			}
		}

		if (taskCount > 0) {
			int totalThreadCount = metaPar.getThreadCount();
			int threadCount = totalThreadCount > taskCount ? totalThreadCount / taskCount : 1;
			int threadPoolCount = totalThreadCount > taskCount ? taskCount : totalThreadCount;
			pFindTask.setTotalThread(threadCount);

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File db = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".fasta");
				File pf2 = new File(spectraDir, separateResultFolders[i].getName() + suffix);
				if (pf2.exists()) {

					pFindTask.searchPF2(new String[] { pf2.getAbsolutePath() },
							separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type, isOpenSearch,
							true, mosp.getOpenPsmFDR(), ((MetaParameterHGM) this.metaPar).getFixMods(),
							((MetaParameterHGM) this.metaPar).getVariMods(),
							((MetaParameterHGM) this.metaPar).getEnzyme(),
							((MetaParameterHGM) this.metaPar).getMissCleavages(),
							((MetaParameterHGM) this.metaPar).getDigestMode(),
							((MetaParameterHGM) this.metaPar).getIsobaricTag());

				} else {

					File mgfFile = new File(spectraDir, separateResultFolders[i].getName() + ".mgf");

					if (mgfFile.exists()) {
						pFindTask.searchMgf(new String[] { mgfFile.getAbsolutePath() },
								separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type, isOpenSearch,
								true, mosp.getOpenPsmFDR(), ((MetaParameterHGM) this.metaPar).getFixMods(),
								((MetaParameterHGM) this.metaPar).getVariMods(),
								((MetaParameterHGM) this.metaPar).getEnzyme(),
								((MetaParameterHGM) this.metaPar).getMissCleavages(),
								((MetaParameterHGM) this.metaPar).getDigestMode(),
								((MetaParameterHGM) this.metaPar).getIsobaricTag(), false);
					} else {
						System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra file " + pf2
								+ " was not found");

						LOGGER.error(taskName + ": spectra file " + pf2 + " was not found");

						throw new IOException("Spectra file " + pf2 + " was not found");
					}
				}
			}

			pFindTask.run(threadPoolCount, this.separateResultFolders.length * 10);
		}

		for (int i = 0; i < this.separateResultFolders.length; i++) {
			PFindTask.cleanPFindResultFolder(separateResultFolders[i], true);
		}
	}

	private void combineResult() throws NumberFormatException, IOException {

		System.out.println(format.format(new Date()) + "\t" + taskName + ": result combination started");
		LOGGER.info(taskName + ": result combination started");

		File msms = new File(pFindResultFile, "psms.tsv");

		HashMap<String, HashSet<String>> originalPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> originalProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> totalPepProMap = new HashMap<String, HashSet<String>>();

		HashMap<String, HashSet<String>> tempPepGenomeMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempPepProMap = new HashMap<String, HashSet<String>>();

		HashMap<String, double[]> proScoreMap = new HashMap<String, double[]>();
		HashMap<String, String> proNameMap = new HashMap<String, String>();

		PFindPSM[] psms = null;
		String proTitle1 = null;
		String proTitle2 = null;

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			PFindResultReader readeri = new PFindResultReader(separateResultFolders[i], spectraDirFile, this.ptmMap);
			readeri.read();

			PFindPSM[] psmsi = readeri.getPsms();
			if (psms == null) {
				psms = new PFindPSM[psmsi.length];
				System.arraycopy(psmsi, 0, psms, 0, psmsi.length);
			} else {
				PFindPSM[] tempPsms = new PFindPSM[psms.length + psmsi.length];
				System.arraycopy(psms, 0, tempPsms, 0, psms.length);
				System.arraycopy(psmsi, 0, tempPsms, psms.length, psmsi.length);
				psms = tempPsms;
			}

			PeptideResult[] prs = readeri.getPeptideResults();
			for (PeptideResult pr : prs) {
				String sequence = pr.getBaseSeq();
				String[] pros = pr.getProteins();
				HashSet<String> genomeSet = new HashSet<String>();
				HashSet<String> proSet = new HashSet<String>();

				for (int j = 0; j < pros.length; j++) {
					if (pros[j].startsWith("MGYG")) {
						String genome = pros[j].split("_")[0];
						genomeSet.add(genome);
					}

					proSet.add(pros[j]);

					if (totalPepProMap.containsKey(sequence)) {
						totalPepProMap.get(sequence).add(pros[j]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pros[j]);
						totalPepProMap.put(sequence, set);
					}

					if (originalPepProMap.containsKey(sequence)) {
						originalPepProMap.get(sequence).add(pros[j]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pros[j]);
						originalPepProMap.put(sequence, set);
					}

					if (originalProPepMap.containsKey(pros[j])) {
						originalProPepMap.get(pros[j]).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						originalProPepMap.put(pros[j], set);
					}
				}

				if (genomeSet.size() > 0) {
					if (tempPepGenomeMap.containsKey(sequence)) {
						tempPepGenomeMap.get(sequence).addAll(genomeSet);
					} else {
						tempPepGenomeMap.put(sequence, genomeSet);
					}
				}
				if (proSet.size() > 0) {
					if (tempPepProMap.containsKey(sequence)) {
						tempPepProMap.get(sequence).addAll(proSet);
					} else {
						tempPepProMap.put(sequence, proSet);
					}
				}
			}

			File proFile = new File(separateResultFolders[i], "pFind.protein");
			BufferedReader proReader = new BufferedReader(new FileReader(proFile));
			proTitle1 = proReader.readLine();
			int proTitleLength = proTitle1.split("\t").length;
			proTitle2 = proReader.readLine();
			int psmTitleLenght = proTitle2.split("\t").length;

			double leadProScore = 0;
			double leadQValue = 0;
			double leadProCoverage = 0;
			String line = null;
			while ((line = proReader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == 1) {
					if (line.contains("Summary")) {
						break;
					}
				} else if (cs.length == proTitleLength) {
					double proScore = Double.parseDouble(cs[2]);
					double proQValue = Double.parseDouble(cs[3]);
					double proCoverage = Double.parseDouble(cs[4]);

					if (proScoreMap.containsKey(cs[1])) {
						if (proScore > proScoreMap.get(cs[1])[0]) {
							proScoreMap.put(cs[1], new double[] { proScore, proQValue, proCoverage });
						}
					} else {
						proScoreMap.put(cs[1], new double[] { proScore, proQValue, proCoverage });
					}
					
					leadProScore = proScore;
					leadQValue = proQValue;
					leadProCoverage = proCoverage;

				} else if (cs.length == psmTitleLenght) {

				} else {
					if (cs[1].equals("SameSet") || cs[1].equals("SubSet")) {
						double proCoverage = Double.parseDouble(cs[3]);
						double proScore = leadProScore * proCoverage / leadProCoverage;
						double proQValue = leadQValue * leadProCoverage / proCoverage;
						if (proScoreMap.containsKey(cs[2])) {
							if (proScore > proScoreMap.get(cs[2])[0]) {
								proScoreMap.put(cs[2], new double[] { proScore, proQValue, proCoverage });
							}
						} else {
							proScoreMap.put(cs[2], new double[] { proScore, proQValue, proCoverage });
						}
					}
				}
			}
			proReader.close();

			File dbFile = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".fasta");
			if (dbFile.exists()) {
				BufferedReader dbReader = new BufferedReader(new FileReader(dbFile));
				String dbLine = null;
				while ((dbLine = dbReader.readLine()) != null) {
					if (dbLine.startsWith(">")) {
						int sp = dbLine.indexOf(" ");
						if (sp > 1) {
							proNameMap.put(dbLine.substring(1, sp), dbLine.substring(sp + 1));
						}
					}
				}
				dbReader.close();
			}

			File resFile = new File(separateResultFolders[i], "pFindTask.F1.L1.qry.res");
			if (resFile.exists()) {
				FileUtils.deleteQuietly(resFile);
			}
		}

		HashSet<String> razorGenomeSet = new HashSet<String>();

		razor(tempPepGenomeMap, razorGenomeSet, 1);

		for (String pep : tempPepProMap.keySet()) {
			HashSet<String> proSet = tempPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (pro.startsWith("MGYG")) {
					String genome = pro.split("_")[0];
					if (!razorGenomeSet.contains(genome)) {
						proIt.remove();
					}
				}
			}
		}

		HashSet<String> razorProSet = new HashSet<String>();

		razor(tempPepProMap, razorProSet, 1);

		HashMap<String, ProteinGroup> proGroupMap = this.getProteinGroups(totalPepProMap, razorProSet);

		HashMap<String, HashSet<String>> proPepMap = new HashMap<String, HashSet<String>>();
		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			for (String pro : proSet) {
				if (proPepMap.containsKey(pro)) {
					proPepMap.get(pro).add(pep);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(pep);
					proPepMap.put(pro, set);
				}
			}
		}

		String[] allPros = proGroupMap.keySet().toArray(new String[proGroupMap.size()]);

		Arrays.sort(allPros, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				boolean target1 = !o1.startsWith(PFindResultReader.REV);
				boolean target2 = !o2.startsWith(PFindResultReader.REV);

				if (target1) {
					if (!target2) {
						return -1;
					}
				} else {
					if (target2) {
						return 1;
					}
				}

				HashSet<String> pepSet1 = proPepMap.get(o1);
				boolean unique1 = false;
				for (String pep : pepSet1) {
					HashSet<String> proSet = totalPepProMap.get(pep);
					if (proSet.size() == 1 && proSet.contains(o1)) {
						unique1 = true;
						break;
					}
				}

				HashSet<String> pepSet2 = proPepMap.get(o2);
				boolean unique2 = false;
				for (String pep : pepSet2) {
					HashSet<String> proSet = totalPepProMap.get(pep);
					if (proSet.size() == 1 && proSet.contains(o2)) {
						unique2 = true;
						break;
					}
				}

				if (unique1) {
					if (!unique2) {
						return -1;
					}
				} else {
					if (unique2) {
						return 1;
					}
				}

				if (proScoreMap.containsKey(o1)) {
					if (proScoreMap.containsKey(o2)) {
						double[] score1 = proScoreMap.get(o1);
						double[] score2 = proScoreMap.get(o2);

						if (score1[0] > score2[0]) {
							return -1;
						} else if (score1[0] < score2[0]) {
							return 1;
						} else {
							if (score1[2] > score2[2]) {
								return -1;
							} else if (score1[2] < score2[2]) {
								return 1;
							} else {
								return 0;
							}
						}

					} else {
						return -1;
					}
				} else {
					if (proScoreMap.containsKey(o2)) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});

		HashMap<String, String> razorPepProMap = new HashMap<String, String>();
		HashMap<String, HashSet<String>> razorProPepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < allPros.length; i++) {

			String proName = allPros[i];
			HashSet<String> pepSet = originalProPepMap.get(proName);
			razorProPepMap.put(proName, new HashSet<String>());
			for (String pep : pepSet) {
				if (!razorPepProMap.containsKey(pep)) {
					razorPepProMap.put(pep, proName);
					razorProPepMap.get(proName).add(pep);
				} else {
					if (razorPepProMap.get(pep).startsWith(PFindResultReader.REV)) {
						String revPro = razorPepProMap.get(pep);
						razorProPepMap.get(revPro).remove(pep);
						razorPepProMap.put(pep, proName);
						razorProPepMap.get(proName).add(pep);
					}
				}
			}
		}

		HashSet<String> subProSet = new HashSet<String>();
		HashSet<String> sameProSet = new HashSet<String>();
		for (int i = 0; i < allPros.length; i++) {
			String proName = allPros[i];
			
			if(proName.equals("sp|P35573|GDE_HUMAN") || proName.equals("REV_MGYG000002737_00861") || proName.equals("REV_MGYG000000042_02246")) {
				System.out.println("proname\t"+proName+"\t"+razorProPepMap.get(proName));
			}
			
			if (razorProPepMap.get(proName).size() == 0) {
				razorProPepMap.remove(proName);
			}

			if (proName.startsWith(PFindResultReader.REV)) {
				continue;
			}

			ProteinGroup pg = proGroupMap.get(proName);
			sameProSet.addAll(pg.sameSet);

			HashSet<String> pepSet = originalProPepMap.get(proName);
			for (String pep : pepSet) {
				HashSet<String> proSet = originalPepProMap.get(pep);
				for (String pro : proSet) {
					if (pro.startsWith(PFindResultReader.REV)) {
						continue;
					}

					if (subProSet.contains(pro)) {
						continue;
					}

					if (proName.equals(pro) || pg.sameSet.contains(pro)) {
						continue;
					}

					boolean sub = true;
					HashSet<String> pepISet = originalProPepMap.get(pro);
					for (String pepI : pepISet) {
						if (!pepSet.contains(pepI)) {
							sub = false;
						}
					}

					if (sub) {
						pg.subSet.add(pro);
						subProSet.add(pro);
					}
				}
			}
		}

		OpenPsmTsvWriter msmsWriter = new OpenPsmTsvWriter(msms, OpenPsmTsvWriter.pFindFormat);
		PrintWriter idWriter = new PrintWriter(new File(pFindResultFile, "id.tsv"));
		idWriter.println("Raw file\tUnique peptide count\tPSM count");

		HashMap<String, HashSet<Integer>> filePsmCountMap = new HashMap<String, HashSet<Integer>>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, String[]> scanProMap = new HashMap<String, String[]>();
		for (int i = 0; i < psms.length; i++) {

			String pep = psms[i].getSequence();
			if (!razorPepProMap.containsKey(pep)) {
				continue;
			}

			String razorProName = razorPepProMap.get(pep);

			HashSet<String> originalProSet = originalPepProMap.get(pep);

			HashSet<String> proSet = new HashSet<String>();
			proSet.add(razorProName);

			if (razorProName.startsWith(PFindResultReader.REV)) {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			} else {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (!pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			}
			String[] pros = proSet.toArray(new String[proSet.size()]);
			psms[i].setProteins(pros);

			scanProMap.put(psms[i].getFileName(), pros);

			String fileName = psms[i].getFileName();
			fileName = fileName.substring(0, fileName.indexOf("."));

			if (filePsmCountMap.containsKey(fileName)) {

				filePsmCountMap.get(fileName).add(psms[i].getScan());
				fileUnipepMap.get(fileName).add(pep);
			} else {

				HashSet<Integer> scanSet = new HashSet<Integer>();
				scanSet.add(psms[i].getScan());
				filePsmCountMap.put(fileName, scanSet);

				HashSet<String> seqSet = new HashSet<String>();
				seqSet.add(pep);
				fileUnipepMap.put(fileName, seqSet);
			}

			if (!pep.contains("X")) {
				msmsWriter.addPSM(psms[i]);
			}
		}
		msmsWriter.close();

		String[] rawNames = filePsmCountMap.keySet().toArray(new String[filePsmCountMap.size()]);
		Arrays.sort(rawNames);

		for (String rawNameString : rawNames) {
			idWriter.println(rawNameString + "\t" + fileUnipepMap.get(rawNameString).size() + "\t"
					+ filePsmCountMap.get(rawNameString).size());
		}
		idWriter.close();

		HashMap<String, HashMap<String, String[]>> psmInfoMap = new HashMap<String, HashMap<String, String[]>>();
		HashMap<String, HashMap<String, Integer>> psmCountMap = new HashMap<String, HashMap<String, Integer>>();
		File pFindSpectra = new File(pFindResultFile, "pFind-Filtered.spectra");
		PrintWriter combinePsmWriter = new PrintWriter(pFindSpectra);

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File specFile = new File(separateResultFolders[i], "pFind.spectra");

			BufferedReader reader = new BufferedReader(new FileReader(specFile));
			String line = reader.readLine();
			if (i == 0) {
				combinePsmWriter.println(line);
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (scanProMap.containsKey(cs[0])) {
					String[] pros = scanProMap.get(cs[0]);
					StringBuilder prosb = new StringBuilder();
					for (int j = 0; j < pros.length; j++) {
						prosb.append(pros[j]).append("/");
					}
					cs[12] = prosb.toString();

					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < cs.length; j++) {
						sb.append(cs[j]).append("\t");
					}
					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}
					combinePsmWriter.println(sb);

					HashMap<String, String[]> modInfoMap;
					HashMap<String, Integer> modCountMap;
					if (psmInfoMap.containsKey(cs[5])) {
						modInfoMap = psmInfoMap.get(cs[5]);
						modCountMap = psmCountMap.get(cs[5]);
					} else {
						modInfoMap = new HashMap<String, String[]>();
						modCountMap = new HashMap<String, Integer>();
						psmInfoMap.put(cs[5], modInfoMap);
						psmCountMap.put(cs[5], modCountMap);
					}

					if (modInfoMap.containsKey(cs[10])) {
						if (Double.parseDouble(cs[9]) < Double.parseDouble(modInfoMap.get(cs[10])[9])) {
							modInfoMap.put(cs[10], cs);
						}
						modCountMap.put(cs[10], modCountMap.get(cs[10]) + 1);
					} else {
						modInfoMap.put(cs[10], cs);
						modCountMap.put(cs[10], 1);
					}
				}
			}

			reader.close();
		}
		combinePsmWriter.close();

		File pFindProtein = new File(pFindResultFile, "pFind.protein");
		PrintWriter combineProWriter = new PrintWriter(pFindProtein);
		combineProWriter.println(proTitle1);
		combineProWriter.println(proTitle2);
		int[] proTDCount = new int[4];
		double coverage = 0.0;

		int proId = 1;
		for (int i = 0; i < allPros.length; i++) {

			StringBuilder proSb = new StringBuilder();
			proSb.append(proId++).append("\t");
			proSb.append(allPros[i]).append("\t");
			if (proScoreMap.containsKey(allPros[i])) {
				double[] score = proScoreMap.get(allPros[i]);
				proSb.append(score[0]).append("\t");
				proSb.append(score[1]).append("\t");
				proSb.append(score[2]).append("\t");

				coverage += score[2];
			} else {
				proSb.append("0.0\t");
				proSb.append("1.0\t");
				proSb.append("0.0\t");
			}

			ProteinGroup pg = proGroupMap.get(allPros[i]);
			HashSet<String> sameSet = pg.sameSet;
			HashSet<String> subSet = pg.subSet;
			HashSet<String> pepSet = proPepMap.get(allPros[i]);

			StringBuilder psmSb = new StringBuilder();
			int psmId = 1;
			boolean hasUnique = false;

			String[] peps = pepSet.toArray(new String[pepSet.size()]);
			Arrays.sort(peps);

			HashMap<String, Integer> proCountMap = new HashMap<String, Integer>();

			for (int j = 0; j < peps.length; j++) {
				if (totalPepProMap.containsKey(peps[j]) && totalPepProMap.get(peps[j]).size() == 1) {
					hasUnique = true;
				}
				HashMap<String, String[]> psmInfoMapJ = psmInfoMap.get(peps[j]);
				if (psmInfoMapJ == null) {
					continue;
				}
				for (String mod : psmInfoMapJ.keySet()) {

					String[] cs = psmInfoMapJ.get(mod);

					psmSb.append("\t\t").append(psmId++);
					psmSb.append("\t").append(peps[j]);

					for (int k = 6; k < 18; k++) {
						psmSb.append("\t").append(cs[k]);
					}
					psmSb.append("\t").append(cs[0]);
					psmSb.append("\t").append(cs[3]);
					psmSb.append("\t").append(psmCountMap.get(peps[j]).get(mod));

					String[] pros = cs[12].split("/");
					for (int k = 0; k < pros.length; k++) {
						if (proCountMap.containsKey(pros[k])) {
							proCountMap.put(pros[k], proCountMap.get(pros[k]) + 1);
						} else {
							proCountMap.put(pros[k], 1);
						}
					}

					psmSb.append("\n");
				}
			}

			if (psmSb.length() == 0) {
				continue;
			}

			proSb.append(psmId - 1).append("\t");
			// sameSet
			proSb.append(sameSet.size() + "\t");
			// subSet
			proSb.append(subSet.size() + "\t");

			if (hasUnique) {
				proSb.append("1\t");
			} else {
				proSb.append("0\t");
			}

			if (proNameMap.containsKey(allPros[i])) {
				proSb.append(proNameMap.get(allPros[i])).append("\t\n");
			} else {
				if (allPros[i].startsWith(PFindResultReader.REV)) {
					proSb.append("decoy protein\t\n");
				} else {
					proSb.append("\t\n");
				}
			}

			if (allPros[i].startsWith(PFindResultReader.REV)) {
				proTDCount[1]++;
			} else {
				proTDCount[0]++;
			}

			for (String samePro : sameSet) {
				proSb.append("\tSameSet");
				proSb.append("\t").append(samePro);

				if (proScoreMap.containsKey(samePro)) {
					double[] score = proScoreMap.get(samePro);
					proSb.append("\t").append(score[2]);
				} else {
					proSb.append("\t0.0");
				}
				if (proCountMap.containsKey(samePro)) {
					proSb.append("\t").append(proCountMap.get(samePro)).append("\n");
				} else {
					proSb.append("\t0\n");
				}

				if (samePro.startsWith(PFindResultReader.REV)) {
					proTDCount[3]++;
				} else {
					proTDCount[2]++;
				}
			}

			for (String subPro : subSet) {
				proSb.append("\tSubSet");
				proSb.append("\t").append(subPro);

				if (proScoreMap.containsKey(subPro)) {
					double[] score = proScoreMap.get(subPro);
					proSb.append("\t").append(score[2]);
				} else {
					proSb.append("\t0.0");
				}
				if (proCountMap.containsKey(subPro)) {
					proSb.append("\t").append(proCountMap.get(subPro)).append("\n");
				} else {
					proSb.append("\t0\n");
				}

				if (subPro.startsWith(PFindResultReader.REV)) {
					proTDCount[3]++;
				} else {
					proTDCount[2]++;
				}
			}

			combineProWriter.print(proSb);
			combineProWriter.print(psmSb);
		}

		combineProWriter.println("----Summary----");
		combineProWriter.print("Proteins: ");
		combineProWriter.print(proTDCount[2]);
		combineProWriter.print(" / ");
		combineProWriter.println(proTDCount[2] + proTDCount[3]);

		combineProWriter.print("Protein_Groups: ");
		combineProWriter.print(proTDCount[0]);
		combineProWriter.print(" / ");
		combineProWriter.println(proTDCount[0] + proTDCount[1]);

		combineProWriter.print("Filtered_Protein_Groups: ");
		combineProWriter.print(proTDCount[0]);
		combineProWriter.print(" / ");
		combineProWriter.println(proTDCount[0] + proTDCount[1]);

		combineProWriter.print("Filtered_Protein_Groups_Cov: ");
		combineProWriter.print(FormatTool.getDFP2().format(coverage / (double) allPros.length));

		combineProWriter.close();

		System.out.println(format.format(new Date()) + "\t" + taskName + ": result combination finished");
		LOGGER.info(taskName + ": result combination finished");
	}

	private class ProteinGroup {
		@SuppressWarnings("unused")
		String proName;
		HashSet<String> sameSet;
		HashSet<String> subSet;
		
		private ProteinGroup(String proName) {
			this.proName = proName;
			this.sameSet = new HashSet<String>();
			this.subSet = new HashSet<String>();
		}

		private void addSameGroup(String pro) {
			this.sameSet.add(pro);
		}
	}

	private void combineProteinGroups(HashMap<String, ProteinGroup> pgMap,
			HashMap<String, HashSet<String>> tempProPepMap, HashMap<String, HashSet<String>> tempPepProMap,
			HashMap<String, ProteinGroup> uniquePgMap) {

		// combine protein groups
		for (String pep : tempPepProMap.keySet()) {

			HashSet<String> proSet = tempPepProMap.get(pep);

			String[] pros = proSet.toArray(new String[proSet.size()]);

			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					if (tempProPepMap.get(o1).size() > tempProPepMap.get(o2).size()) {
						return -1;
					}
					if (tempProPepMap.get(o1).size() < tempProPepMap.get(o2).size()) {
						return 1;
					}

					if (o1.startsWith(PFindResultReader.REV)) {
						if (o2.startsWith(PFindResultReader.REV)) {
							return o1.compareTo(o2);
						} else {
							return 1;
						}
					} else {
						if (o2.startsWith(PFindResultReader.REV)) {
							return -1;
						} else {
							return o1.compareTo(o2);
						}
					}
				}
			});

			for (int i = 0; i < pros.length; i++) {

				if (pgMap.containsKey(pros[i])) {
					ProteinGroup pg = pgMap.get(pros[i]);

					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);

					for (int j = i + 1; j < pros.length; j++) {

						if (pgMap.containsKey(pros[j])) {
							HashSet<String> pepSetJ = tempProPepMap.get(pros[j]);

							HashSet<String> pepAll = new HashSet<String>(pepSetI.size() + pepSetJ.size());
							pepAll.addAll(pepSetI);
							pepAll.addAll(pepSetJ);

							if (pepAll.size() == pepSetI.size()) {
								if (pepSetI.size() == pepSetJ.size()) {
									pg.addSameGroup(pros[j]);
								}
								pgMap.remove(pros[j]);
							}
						}
					}
				}
			}
		}

		if (pgMap.size() == tempProPepMap.size()) {

			String[] pros = pgMap.keySet().toArray(new String[pgMap.size()]);
			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					int size1 = tempProPepMap.get(o1).size();
					int size2 = tempProPepMap.get(o2).size();

					if (size1 < size2) {
						return -1;
					}
					if (size1 > size2) {
						return 1;
					}
					return 0;
				}
			});

			// remove subset proteins
			for (int i = 0; i < pros.length; i++) {
				boolean subset = true;
				HashSet<String> pepSet = tempProPepMap.get(pros[i]);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						subset = false;
					}
				}
				if (subset) {
					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);
					for (String pep : pepSetI) {
						HashSet<String> proSet = tempPepProMap.get(pep);
						proSet.remove(pros[i]);
					}
					tempProPepMap.remove(pros[i]);
					pgMap.remove(pros[i]);
				}
			}

			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}

		} else {

			// remove redundant proteins
			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			// find unique proteins
			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}
		}
	}

	/**
	 * get razor protein groups
	 * 
	 * @param proPepMap
	 * @param finalPepProMap
	 * @return
	 */
	private HashMap<String, ProteinGroup> getProteinGroups(HashMap<String, HashSet<String>> totalPepProMap,
			HashSet<String> razorProSet) {

		HashMap<String, HashSet<String>> tempPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			HashSet<String> tempProSet = new HashSet<String>();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!razorProSet.contains(pro)) {
					proIt.remove();
				} else {
					if (tempProPepMap.containsKey(pro)) {
						tempProPepMap.get(pro).add(pep);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pep);
						tempProPepMap.put(pro, set);
					}
					tempProSet.add(pro);
				}
			}
			tempPepProMap.put(pep, tempProSet);
		}

		HashSet<String> uniqueProSet = new HashSet<String>();
		HashSet<String> uniquePepSet = new HashSet<String>();
		for (String pro : tempProPepMap.keySet()) {
			HashSet<String> pepSet = tempProPepMap.get(pro);
			for (String pep : pepSet) {
				HashSet<String> proSet = tempPepProMap.get(pep);
				if (proSet.size() == 1) {
					uniqueProSet.add(pro);
					uniquePepSet.addAll(pepSet);
					break;
				}
			}
		}

		HashMap<String, ProteinGroup> uniquePgMap = new HashMap<String, ProteinGroup>(uniqueProSet.size());
		HashMap<String, ProteinGroup> pgMap = new HashMap<String, ProteinGroup>(tempProPepMap.size());
		HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
		for (String pro : tempProPepMap.keySet()) {
			if (uniqueProSet.contains(pro)) {
				uniquePgMap.put(pro, new ProteinGroup(pro));
			} else {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				HashSet<String> tempPepSet = new HashSet<String>();
				for (String pep : pepSet) {
					if (!uniquePepSet.contains(pep)) {
						tempPepSet.add(pep);
					}
				}

				if (tempPepSet.size() > 0) {
					tempProPepMap2.put(pro, tempPepSet);
					for (String pep : tempPepSet) {
						if (tempPepProMap2.containsKey(pep)) {
							tempPepProMap2.get(pep).add(pro);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pro);
							tempPepProMap2.put(pep, set);
						}
					}
					pgMap.put(pro, new ProteinGroup(pro));
				}
			}
		}

		combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!uniquePgMap.containsKey(pro)) {
					proIt.remove();
				}
			}
		}

		return uniquePgMap;
	}

	private void razor(HashMap<String, HashSet<String>> pepGenomeMap, HashSet<String> totalGenomeSet,
			int iteratorCount) {

		boolean find = false;
		for (String pep : pepGenomeMap.keySet()) {
			HashSet<String> genomeSet = pepGenomeMap.get(pep);
			if (genomeSet.size() == iteratorCount) {
				totalGenomeSet.addAll(genomeSet);
				find = true;
			}
		}

		if (!find) {
			razor(pepGenomeMap, totalGenomeSet, ++iteratorCount);
		} else {
			Iterator<String> pepit = pepGenomeMap.keySet().iterator();
			while ((pepit.hasNext())) {
				String pep = pepit.next();
				boolean has = false;

				HashSet<String> genomeSet = pepGenomeMap.get(pep);
				Iterator<String> genomeIt = genomeSet.iterator();
				while (genomeIt.hasNext()) {
					String genome = genomeIt.next();
					if (totalGenomeSet.contains(genome)) {
						has = true;
						break;
					}
				}
				if (has) {
					pepit.remove();
				}
			}

			if (pepGenomeMap.size() == 0) {
				return;
			} else {
				razor(pepGenomeMap, totalGenomeSet, ++iteratorCount);
			}
		}
	}
	
	protected boolean createSSDB() {
		return true;
	}

	protected boolean iden() {

		bar2.setString(taskName + ": peptide and protein identification");

		LOGGER.info(taskName + ": peptide and protein identification started");
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": peptide and protein identification started");

		this.pFindResultReader = new PFindResultReader(pFindResultFile, spectraDirFile, ptmMap, false);
		
		if (pFindResultReader.hasResult()) {

			LOGGER.info(taskName + ": peptide and protein identification results have been found in " + pFindResultFile
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": peptide and protein identification results have been found in " + pFindResultFile
					+ ", go to next step");

			setProgress(55);

		} else {

			try {
				this.separateSearchHap();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in searching high abundant database", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in searching high abundant database");

				return false;
			}

			setProgress(40);

			try {
				this.pfindFastaExtract4Iterator();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in generating sample-specific database", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in generating sample-specific database");

				return false;
			}

			setProgress(45);

			try {
				separateSearch();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in pFind database search", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in pFind database search");

				return false;
			}

			setProgress(60);

			try {
				combineResult();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!pFindResultReader.hasResult()) {
			LOGGER.error(taskName + ": peptide and protein identification failed");
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": peptide and protein identification failed");
			return false;
		}
		
		try {
			pFindResultReader.read();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the database search result files");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the database search result files");
			return false;
		}

		LOGGER.info(taskName + ": peptide and protein identification finished");
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": peptide and protein identification finished");

		return true;
	}

	/**
	 * The version is different from the parent class
	 */
	protected boolean exportReport() {

		bar2.setString(taskName + ": exporting report");

		LOGGER.info(taskName + ": exporting report started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting report started");

		File reportDir = new File(this.pFindResultFile, "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, MetaParaIOHGM.version);

		metaPar.setReportDir(reportDir);

		this.reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());
		
		this.summaryMetaFile = new File(this.pFindResultFile, "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean summayrFinished = this.exportSummary(task);
		
		setProgress(82);
		
		this.final_pep_txt = new File(this.pFindResultFile, "final_peptides.tsv");

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			this.exportPeptideTxt();
		}

		boolean pepReportOk = false;
		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		} else {
			File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
			if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					pepReportOk = task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary,
							summaryMetaFile);
				} else {
					pepReportOk = task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary);
				}
			}
		}

		setProgress(84);
		
		this.final_pro_txt = new File(this.pFindResultFile, "final_proteins.tsv");

		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			this.exportProteinsTxt();
		}

		boolean proReportOk = false;
		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			return false;
		} else {
			File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
			if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					proReportOk = task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary,
							summaryMetaFile);
				} else {
					proReportOk = task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary);
				}
			}
		}
		
		setProgress(86);

		boolean taxFuncReportOk = false;
		if (metaPar.isMetaWorkflow()) {
			taxFuncReportOk = exportTaxaFunc(task);
		} else {
			taxFuncReportOk = true;
		}

		if (!summayrFinished || !pepReportOk || !proReportOk || !taxFuncReportOk) {
			setProgress(99);

			LOGGER.info(taskName + ": error in generating the reports");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in generating the reports");

			return false;
		}
		
		task.run();

		try {
			boolean finish = task.get();
			if (finish) {
				
				LOGGER.info(taskName + ": report generation successes");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": report generation successes");
				
				setProgress(100);
				return true;
			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	protected boolean exportTaxaFunc(MetaReportTask task) {

		LOGGER.info(taskName + ": taxonomy analysis and functional annotation started");
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": taxonomy analysis and functional annotation started");

		HashMap<String, String> hostProNameMap = new HashMap<String, String>();
		if (metaPar.isAppendHostDb()) {
			String hostLine = null;
			try {
				BufferedReader hostDBufferedReader = new BufferedReader(new FileReader(this.hostDbFile));
				while ((hostLine = hostDBufferedReader.readLine()) != null) {
					if (hostLine.startsWith(">")) {
						String[] cs = hostLine.split("[\\s+]");
						String name = cs[0].substring(1);
						if (cs.length > 1) {
							StringBuilder sb = new StringBuilder();
							for (int i = 1; i < cs.length; i++) {
								if (cs[i].equals("OS=Homo")) {
									break;
								} else {
									sb.append(cs[i]).append(" ");
								}
							}
							hostProNameMap.put(name, sb.substring(0, sb.length() - 1));
						}
					}
				}
				hostDBufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading host database from " + this.hostDbFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading host database from " + this.hostDbFile);
			}
		}

		if (this.pFindResultReader == null) {
			this.pFindResultReader = new PFindResultReader(pFindResultFile, spectraDirFile, ptmMap);
			try {
				pFindResultReader.read();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading the database search result files");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading the database search result files");
				return false;
			}
		}

		// functional annotation

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();

		LOGGER.info(taskName + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": functional annotation started");

		File funcFile = new File(metaPar.getDbSearchResultFile(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		final_pro_xml_file = new File(funcFile, "functions.xml");

		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {

			BufferedReader quanProReader = null;
			try {
				quanProReader = new BufferedReader(new FileReader(this.final_pro_txt));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading quantified proteins from " + this.quan_pro_file.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading quantified proteins from " + this.quan_pro_file.getName());
			}

			try {

				String line = quanProReader.readLine();
				String[] title = line.split("\t");
				int proId = -1;
				
				ArrayList<String> tlist = new ArrayList<String>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].startsWith("Intensity ")) {
						tlist.add(title[i]);
					} else if (title[i].equals("Majority protein IDs")) {
						proId = i;
					}
				}

				String[] fileNames = tlist.toArray(new String[tlist.size()]);

				int[] fileIds = new int[fileNames.length];
				for (int i = 0; i < fileNames.length; i++) {
					for (int j = 0; j < title.length; j++) {
						if (fileNames[i].equals(title[j])) {
							fileIds[i] = j;
						}
					}
				}

				while ((line = quanProReader.readLine()) != null) {

					String[] cs = line.split("\t");
					double[] intensity = new double[fileIds.length];
					for (int i = 0; i < fileIds.length; i++) {
						intensity[i] = Double.parseDouble(cs[fileIds[i]]);
					}

					String[] pros = cs[proId].split(";");
					for (int i = 0; i < pros.length; i++) {
						proIntensityMap.put(pros[i], intensity);

						if (pros[i].startsWith("MGYG")) {
							String genome = pros[i].split("_")[0];
							double[] genomeIntensity;
							if (genomeIntensityMap.containsKey(genome)) {
								genomeIntensity = genomeIntensityMap.get(genome);
							} else {
								genomeIntensity = new double[intensity.length];
							}

							for (int j = 0; j < genomeIntensity.length; j++) {
								genomeIntensity[j] += intensity[j] / (double) pros.length;
							}
							genomeIntensityMap.put(genome, genomeIntensity);
						}
					}
				}
				quanProReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading quantified proteins from " + this.quan_pro_file.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading quantified proteins from " + this.quan_pro_file.getName());
			}

			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {

			PFindProtein[] proteins = this.pFindResultReader.getProteins();
			HashMap<String, PFindProtein> promap = new HashMap<String, PFindProtein>();
			for (int i = 0; i < proteins.length; i++) {
				String pro = proteins[i].getName();
				promap.put(pro, proteins[i]);
			}

			BufferedReader quanProReader = null;
			try {
				quanProReader = new BufferedReader(new FileReader(this.final_pro_txt));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading quantified proteins from " + this.quan_pro_file.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading quantified proteins from " + this.quan_pro_file.getName());
			}

			ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
			String[] fileNames = null;
			try {
				String line = quanProReader.readLine();
				String[] title = line.split("\t");
				int id = -1;
				int proId = -1;

				ArrayList<String> tlist = new ArrayList<String>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].startsWith("Intensity ")) {
						tlist.add(title[i]);
					} else if (title[i].equals("id")) {
						id = i;
					} else if (title[i].equals("Majority protein IDs")) {
						proId = i;
					}
				}

				fileNames = tlist.toArray(new String[tlist.size()]);

				int[] fileIds = new int[fileNames.length];
				for (int i = 0; i < fileNames.length; i++) {
					for (int j = 0; j < title.length; j++) {
						if (fileNames[i].equals(title[j])) {
							fileIds[i] = j;
						}
					}
				}

				while ((line = quanProReader.readLine()) != null) {

					String[] cs = line.split("\t");
					String[] pros = cs[proId].split(";");

					if (promap.containsKey(pros[0]) && pros[0].startsWith("MGYG")) {

						int groupId = Integer.parseInt(cs[id]);
						PFindProtein protein = promap.get(pros[0]);

						double[] intensity = new double[fileIds.length];
						for (int i = 0; i < fileIds.length; i++) {
							intensity[i] = Double.parseDouble(cs[fileIds[i]]);
						}

						for (int i = 0; i < pros.length; i++) {
							MetaProtein mp = new MetaProtein(groupId, i + 1, pros[i], protein.getPepCount(),
									protein.getSpCount(), protein.getScore(), intensity);
							list.add(mp);

							if (pros[i].startsWith("MGYG")) {
								String genome = pros[i].split("_")[0];

								double[] genomeIntensity;
								if (genomeIntensityMap.containsKey(genome)) {
									genomeIntensity = genomeIntensityMap.get(genome);
								} else {
									genomeIntensity = new double[intensity.length];
								}

								for (int j = 0; j < genomeIntensity.length; j++) {
									genomeIntensity[j] += intensity[j] / (double) pros.length;
								}
								genomeIntensityMap.put(genome, genomeIntensity);
							}
						}
					}
				}
				quanProReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading quantified proteins from " + this.quan_pro_file.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading quantified proteins from " + this.quan_pro_file.getName());
			}

			setProgress(80);

			MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
			MetaProteinAnnoHgm[] mpas = funcAnnoSqlite(metapros, fileNames);
			for (int i = 0; i < mpas.length; i++) {
				String name = mpas[i].getPro().getName();
				if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
					proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
				} else {
					proTaxIdMap.put(name, 131567);
				}
			}
		}

		File funcResultFile = final_pro_xml_file.getParentFile();
		File funTsv = new File(funcResultFile, "functions.tsv");
		File funReportTsv = new File(funcResultFile, "functions_report.tsv");

		MetaProteinXMLReader2 funReader = null;
		try {
			funReader = new MetaProteinXMLReader2(final_pro_xml_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading protein function information from "
					+ this.final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading protein function information from " + this.quan_pro_file.getName());
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
			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv.getName()
					+ " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing protein function information to " + funTsv.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv.getName()
					+ " finished");
		}

		File html = new File(funcResultFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html.getName()
					+ " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html.getName()
					+ " finished");
		}

		File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");
		if (!report_function_summary.exists() || report_function_summary.length() == 0) {

			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary, summaryMetaFile);
			} else {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
			}
		}

		LOGGER.info(taskName + ": functional annotation finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": functional annotation finished");

		setProgress(92);
		
		// taxonomy analysis
		
		LOGGER.info(taskName + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": taxonomy analysis started");

		File taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}
		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.final_pep_txt));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		HashMap<String, double[]> pepIntenMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].startsWith("Intensity ")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				pepIntenMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> pepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> uniquePepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashMap<String, ArrayList<PFindPSM>>> pepModInfoMap = new HashMap<String, HashMap<String, ArrayList<PFindPSM>>>();

		PFindPSM[] psms = pFindResultReader.getPsms();
		HashMap<String, HashSet<String>> genomeProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < psms.length; i++) {

			String baseSequence = psms[i].getSequence();
			String[] pros = psms[i].getProteins();

			for (int j = 0; j < pros.length; j++) {

				if (psmCountMap.containsKey(pros[j])) {
					psmCountMap.put(pros[j], psmCountMap.get(pros[j]) + 1);

					pepCountMap.get(pros[j]).add(baseSequence);
					if (pros.length == 1) {
						uniquePepCountMap.get(pros[j]).add(baseSequence);
					}
				} else {
					psmCountMap.put(pros[j], 1);

					HashSet<String> pepSet = new HashSet<String>();
					pepSet.add(baseSequence);
					pepCountMap.put(pros[j], pepSet);

					HashSet<String> unipepSet = new HashSet<String>();
					if (pros.length == 1) {
						unipepSet.add(baseSequence);
					}
					uniquePepCountMap.put(pros[j], unipepSet);
				}

				if (pros[j].startsWith("MGYG")) {
					String genome = pros[j].split("_")[0];

					if (psmCountMap.containsKey(genome)) {
						psmCountMap.put(genome, psmCountMap.get(genome) + 1);

						pepCountMap.get(genome).add(baseSequence);
						if (pros.length == 1) {
							uniquePepCountMap.get(genome).add(baseSequence);
						}
					} else {
						psmCountMap.put(genome, 1);

						HashSet<String> pepSet = new HashSet<String>();
						pepSet.add(baseSequence);
						pepCountMap.put(genome, pepSet);

						HashSet<String> unipepSet = new HashSet<String>();
						if (pros.length == 1) {
							unipepSet.add(baseSequence);
						}
						uniquePepCountMap.put(genome, unipepSet);
					}

					if (genomeProMap.containsKey(genome)) {
						genomeProMap.get(genome).add(pros[j]);
					} else {
						HashSet<String> proSet = new HashSet<String>();
						proSet.add(pros[j]);
						genomeProMap.put(genome, proSet);
					}
				}
			}

			String modseq = psms[i].getModseq();
			if (pepModInfoMap.containsKey(baseSequence)) {
				HashMap<String, ArrayList<PFindPSM>> m = pepModInfoMap.get(baseSequence);
				if (m.containsKey(modseq)) {
					m.get(modseq).add(psms[i]);
				} else {
					ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
					list.add(psms[i]);
					m.put(modseq, list);
				}
			} else {
				HashMap<String, ArrayList<PFindPSM>> m = new HashMap<String, ArrayList<PFindPSM>>();
				ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
				list.add(psms[i]);
				m.put(modseq, list);
				pepModInfoMap.put(baseSequence, m);
			}
		}

		return exportGenome(genomeIntensityMap, psmCountMap, pepCountMap, uniquePepCountMap, taxFile, expNames, task);
	}

	@SuppressWarnings("unchecked")
	private boolean exportGenome(HashMap<String, double[]> genomeIntensityMap, HashMap<String, Integer> psmCountMap,
			HashMap<String, HashSet<String>> pepCountMap, HashMap<String, HashSet<String>> uniquePepCountMap,
			File taxFile, String[] expNames, MetaReportTask task) {

		HGMSqliteSearcher searcher = null;
		try {
			searcher = new HGMSqliteSearcher(msv.getFunction(), msv.getFuncDef());

		} catch (NumberFormatException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in genome analysis, task failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in genome analysis, task failed");

			return false;
		}

		String[] genomes = genomeIntensityMap.keySet().toArray(new String[genomeIntensityMap.size()]);
		HashMap<String, String[]> genomeTaxaMap = null;
		try {
			genomeTaxaMap = searcher.matchGenome(genomes);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": Error in searching the genome2taxa database", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in searching the genome2taxa database");
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

		File genomeFile = new File(taxFile, "Genome.tsv");
		PrintWriter genomeWriter = null;
		try {
			genomeWriter = new PrintWriter(genomeFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeFile, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeFile);
			return false;
		}

		StringBuilder genomeTitlesb = new StringBuilder();
		genomeTitlesb.append("Genome\t");
		genomeTitlesb.append("PSM count\t");
		genomeTitlesb.append("Peptide count\t");
		genomeTitlesb.append("Unique peptide count\t");
		genomeTitlesb.append("GTDB_Name\t");
		genomeTitlesb.append("GTDB_Rank\t");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			genomeTitlesb.append("GTDB_").append(mainRanks[i].getName()).append("\t");
		}

		genomeTitlesb.append("NCBI_Name\t");
		genomeTitlesb.append("NCBI_Rank");

		for (int i = 0; i < mainRanks.length; i++) {
			genomeTitlesb.append("\t").append("NCBI_").append(mainRanks[i].getName());
		}

		for (String exp : expNames) {
			genomeTitlesb.append("\t").append(exp);
		}

		genomeWriter.println(genomeTitlesb);
		
		setProgress(94);

		double[] cellularIntensity = new double[expNames.length];
		HashMap<String, String[]>[] taxRankMaps = new HashMap[mainRanks.length];
		HashMap<String, double[]>[] taxIntensityMaps = new HashMap[mainRanks.length];
		for (int i = 0; i < taxRankMaps.length; i++) {
			taxRankMaps[i] = new HashMap<String, String[]>();
			taxIntensityMaps[i] = new HashMap<String, double[]>();
		}

		for (String genomeKey : genomeTaxaMap.keySet()) {
			String[] taxaContent = genomeTaxaMap.get(genomeKey);
			if (genomeIntensityMap.containsKey(genomeKey)) {
				double[] genomeIntensity = genomeIntensityMap.get(genomeKey);
				for (int i = 0; i < genomeIntensity.length; i++) {
					cellularIntensity[i] += genomeIntensity[i];
				}
				
				StringBuilder genomeTaxaSb = new StringBuilder();
				genomeTaxaSb.append(genomeKey).append("\t");

				if (psmCountMap.containsKey(genomeKey)) {
					genomeTaxaSb.append(psmCountMap.get(genomeKey)).append("\t");
				} else {
					genomeTaxaSb.append("0").append("\t");
				}
				if (pepCountMap.containsKey(genomeKey)) {
					genomeTaxaSb.append(pepCountMap.get(genomeKey).size()).append("\t");
				} else {
					genomeTaxaSb.append("0").append("\t");
				}
				if (uniquePepCountMap.containsKey(genomeKey)) {
					genomeTaxaSb.append(uniquePepCountMap.get(genomeKey).size());
				} else {
					genomeTaxaSb.append("0");
				}

				for (int i = 1; i < taxaContent.length; i++) {
					genomeTaxaSb.append("\t").append(taxaContent[i]);
				}

				for (int i = 0; i < genomeIntensity.length; i++) {
					genomeTaxaSb.append("\t").append(genomeIntensity[i]);
				}
				genomeWriter.println(genomeTaxaSb);

				for (int i = 0; i < taxRankMaps.length; i++) {
					String taxon = taxaContent[i + 3];
					if (taxon == null || taxon.length() == 0) {
						continue;
					}
					if (!taxRankMaps[i].containsKey(taxon)) {
						String[] lineage = new String[i + 1];
						System.arraycopy(taxaContent, 3, lineage, 0, lineage.length);
						taxRankMaps[i].put(taxon, lineage);
					}

					double[] taxIntensity;
					if (taxIntensityMaps[i].containsKey(taxon)) {
						taxIntensity = taxIntensityMaps[i].get(taxon);
					} else {
						taxIntensity = new double[genomeIntensity.length];
					}

					for (int j = 0; j < taxIntensity.length; j++) {
						taxIntensity[j] += genomeIntensity[j];
					}
					taxIntensityMaps[i].put(taxon, taxIntensity);
				}
			}
		}
		genomeWriter.close();

		File taxaFile = new File(taxFile, "Taxa.tsv");
		PrintWriter taxaWriter = null;
		try {
			taxaWriter = new PrintWriter(taxaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxa information to " + taxaFile, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing taxa information to " + taxaFile);
		}

		StringBuilder taxaTitlesb = new StringBuilder();
		taxaTitlesb.append("Name\t");
		taxaTitlesb.append("Rank");

		for (int i = 0; i < mainRanks.length; i++) {
			taxaTitlesb.append("\t").append(mainRanks[i].getName());
		}

		for (String exp : expNames) {
			taxaTitlesb.append("\t").append(exp);
		}
		taxaWriter.println(taxaTitlesb);

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

				taxaWriter.println(taxaSb);
			}
		}

		taxaWriter.close();

		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}

		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(taxRankMaps, taxIntensityMaps, cellularIntensity, expNames, jsOutput);
		}

		boolean finished = false;

		File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
		if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				finished = task.addTask(MetaReportTask.taxon, taxaFile, report_taxonomy_summary,
						summaryMetaFile);
			} else {
				finished = task.addTask(MetaReportTask.taxon, taxaFile, report_taxonomy_summary);
			}
		}
		
		LOGGER.info(taskName + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": taxonomy analysis started");
		
		setProgress(96);

		return finished;
	}

	protected MetaProteinAnnoHgm[] funcAnnoSqlite(MetaProtein[] pros, String[] fileNames) {

		HashMap<String, ArrayList<MetaProtein>> genomeProMap = new HashMap<String, ArrayList<MetaProtein>>();
		for (int i = 0; i < pros.length; i++) {
			String name = pros[i].getName();
			String genome = name.split("_")[0];
			if (genome.endsWith(".1")) {
				genome = genome.substring(0, genome.length() - 2);
			}

			if (genomeProMap.containsKey(genome)) {
				genomeProMap.get(genome).add(pros[i]);
			} else {
				ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
				list.add(pros[i]);
				genomeProMap.put(genome, list);
			}
		}

		HashMap<String, String[]> usedCogMap = new HashMap<String, String[]>();
		HashMap<String, String[]> usedNogMap = new HashMap<String, String[]>();
		HashMap<String, String> usedKeggMap = new HashMap<String, String>();
		HashMap<String, GoObo> usedGoMap = new HashMap<String, GoObo>();
		HashMap<String, EnzymeCommission> usedEcMap = new HashMap<String, EnzymeCommission>();

		ArrayList<MetaProteinAnnoHgm> list = new ArrayList<MetaProteinAnnoHgm>();
		for (String genome : genomeProMap.keySet()) {

			ArrayList<MetaProtein> mplist = genomeProMap.get(genome);
			MetaProtein[] mps = mplist.toArray(new MetaProtein[mplist.size()]);

			HGMSqliteSearcher searcher = null;
			try {
				searcher = new HGMSqliteSearcher(msv.getFunction(), msv.getFuncDef(), genome);

			} catch (NumberFormatException | SQLException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in proteins functional annotations, task failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in proteins functional annotations, task failed");

				return null;
			}

			MetaProteinAnnoHgm[] mpas = null;
			try {
				mpas = searcher.match(mps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in proteins functional annotations, task failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in proteins functional annotations, task failed");

				return null;
			}

			if (mpas != null) {
				for (int i = 0; i < mpas.length; i++) {
					list.add(mpas[i]);
				}

				usedCogMap.putAll(searcher.getUsedCogMap());
				usedNogMap.putAll(searcher.getUsedNogMap());
				usedKeggMap.putAll(searcher.getUsedKeggMap());
				usedEcMap.putAll(searcher.getUsedEcMap());
				usedGoMap.putAll(searcher.getUsedGoMap());
			}
		}

		MetaProteinAnnoHgm[] metapros = list.toArray(new MetaProteinAnnoHgm[list.size()]);

		MetaProteinXMLWriter2 writer = new MetaProteinXMLWriter2(final_pro_xml_file.getAbsolutePath(),
				MetaConstants.pFindHap, ((MetaParameterHGM) this.metaPar).getQuanMode(), fileNames);

		writer.addProteins(metapros, usedCogMap, usedNogMap, usedKeggMap, usedGoMap, usedEcMap);

		writer.close();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {
			LOGGER.error(taskName + ": error in proteins functional annotations, task failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in proteins functional annotations, task failed");
		}

		return metapros;
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
