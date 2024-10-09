package bmi.med.uOttawa.metalab.task.dia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import com.sun.management.OperatingSystemMXBean;

import bmi.med.uOttawa.metalab.core.prodb.FastaManager;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNPrecursor;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNResultReader;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannPrMatrixReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPeptide;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParaIoDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesIoDia;
import bmi.med.uOttawa.metalab.task.hgm.HMGenomeHtmlWriter;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.mag.MagDbConfigIO;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagFuncSearcher;
import bmi.med.uOttawa.metalab.task.mag.MagHapPFindTask;
import bmi.med.uOttawa.metalab.task.mag.MetaProteinAnnoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;

public class MetaDiaTask extends MagHapPFindTask {

	protected String[] diaFiles;
	protected DiaNNTask diaNNTask;
	protected DiaLibSearchPar diaLibSearchPar;
	protected DiannParameter diannPar;
	protected DiaNNPrecursor[] diaPp;
	private boolean libSearch;
	private boolean selfModelSearch;
	private int[] selfModelIds;
	
	private static final Logger LOGGER = LogManager.getLogger(MetaDiaTask.class);
	private static final String taskName = "DIA data analysis";

	public MetaDiaTask(MetaParameterDia metaPar, MetaSourcesDia msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv, bar1, bar2, nextWork);
	}
	
	protected void initial() {
		
		this.metadata = metaPar.getMetadata();
		this.magDb = ((MetaParameterMag) metaPar).getUsedMagDbItem();
		this.threadCount = metaPar.getThreadCount();

		this.diaNNTask = new DiaNNTask(((MetaSourcesDia) msv).getDiann());
		this.diaLibSearchPar = new DiaLibSearchPar();
		this.diaLibSearchPar.setThreadCount(metaPar.getThreadCount());
		
		this.diannPar = new DiannParameter();
		this.diannPar.setThreads(metaPar.getThreadCount());
		this.libSearch = ((MetaParameterDia) metaPar).isLibrarySearch();
		this.selfModelSearch = ((MetaParameterDia) metaPar).isSelfModelSearch();

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

		LOGGER.info(getTaskName() + ": converting raw files started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw files started");

		ArrayList<String> list = new ArrayList<String>();
		this.diaFiles = new String[rawFiles.length];
		for (int i = 0; i < diaFiles.length; i++) {
			if (rawFiles[i].endsWith(".dia") || rawFiles[i].endsWith(".DIA")) {
				diaFiles[i] = rawFiles[i];
			} else if (rawFiles[i].endsWith(".d") || rawFiles[i].endsWith(".D")) {
				diaFiles[i] = rawFiles[i];
			} else if (rawFiles[i].endsWith(".mzml") || rawFiles[i].endsWith(".mzML")) {
				diaFiles[i] = rawFiles[i];
			} else if (rawFiles[i].endsWith(".raw") || rawFiles[i].endsWith(".RAW")) {
				diaFiles[i] = rawFiles[i] + ".dia";
				File diaFile = new File(diaFiles[i]);
				if (!diaFile.exists()) {
					list.add(rawFiles[i]);
				}
			} else {
				LOGGER.error(getTaskName() + ": unknown spectra format");
				System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": unknown spectra format");

				return false;
			}
		}

		if (list.size() > 0) {
			diaNNTask.addTask(diannPar, list.toArray(String[]::new));
		}

		diaNNTask.run(1, diaFiles.length);

		for (int i = 0; i < diaFiles.length; i++) {
			File file = new File(diaFiles[i]);
			if (!file.exists()) {
				LOGGER.error(getTaskName() + ": converting raw file to " + diaFiles[i] + " failed");
				System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw file to "
						+ diaFiles[i] + " failed");

				return false;
			}
		}

		LOGGER.info(getTaskName() + ": converting raw files finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw files finished");

		return true;
	}

	protected boolean createSSDB() {
		
		setProgress(0);
		
		if (libSearch) {
			return true;
		}

		LOGGER.info(getTaskName() + ": iterative search started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": iterative search started");

		boolean searchHap = this.separateSearchHap();

		if (searchHap) {
			LOGGER.info(getTaskName() + ": iterative search finished");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": iterative search finished");

			return true;
		} else {
			LOGGER.error(getTaskName() + ": iterative search failed");
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": iterative search failed");

			return false;
		}
	}
	
	protected boolean iden() {

		bar2.setString(getTaskName() + ": peptide and protein identification");
		LOGGER.info(getTaskName() + ": peptide and protein identification started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein identification started");

		diaLibSearchPar.setMbr(true);
		diaLibSearchPar.setDiaFiles(diaFiles);
		diaLibSearchPar.setThreadCount(threadCount);

		diannPar.setThreads(threadCount);
		diannPar.setMatrices(true);
		diannPar.setRelaxed_prot_inf(true);
		diannPar.setReanalyse(true);

		this.quan_pep_file = new File(resultFolderFile, "combined.pr_matrix.tsv");
		File combineFile = new File(resultFolderFile, "combined.tsv");

		if (!quan_pep_file.exists() || !combineFile.exists()) {

			LOGGER.info(getTaskName() + ": library search started");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library search started");

			if (libSearch) {

				setProgress(10);

				File magDbFile = this.magDb.getCurrentFile();
				File libFolderFile = new File(magDbFile, "libraries");

				String[] usedLibs = ((MetaParameterDia) metaPar).getLibrary();
				String[] libPath = new String[usedLibs.length];
				for (int i = 0; i < usedLibs.length; i++) {
					File libFile = new File(new File(libFolderFile, usedLibs[i]), usedLibs[i] + ".speclib");
					File libTsvFile = new File(new File(libFolderFile, usedLibs[i]), usedLibs[i] + ".tsv");
					if (libPath.length == 1) {
						libPath[i] = libFile.getAbsolutePath();
					} else {
						libPath[i] = libTsvFile.getAbsolutePath();
					}
				}
				diaNNTask.addTask(diannPar, diaLibSearchPar, combineFile.getAbsolutePath(), libPath);
				diaNNTask.run(diaFiles.length * 12);

				HashSet<String> pepSet = new HashSet<String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(combineFile))) {
					String line = reader.readLine();
					String[] title = line.split("\t");
					int seqid = -1;
					for (int i = 0; i < title.length; i++) {
						if (title[i].equals("Stripped.Sequence")) {
							seqid = i;
						}
					}

					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						pepSet.add(cs[seqid]);
					}
					reader.close();

				} catch (IOException e) {
					LOGGER.error(getTaskName() + ": error in reading the combined result file " + combineFile, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in reading the combined result file " + combineFile);

					return false;
				}

				File shufFastaFile = new File(resultFolderFile, "shuffled.fasta");
				File shufPepFile = new File(resultFolderFile, "shuffledPeptide.tsv");

				if (!shufFastaFile.exists() || shufFastaFile.length() == 0) {
					Random random = new Random();

					try (PrintWriter shufFastaWriter = new PrintWriter(shufFastaFile)) {

						PrintWriter shufPepWriter = new PrintWriter(shufPepFile);
						shufPepWriter.println("Shuffled peptide\tOriginal peptide");

						int id = 0;
						for (String pep : pepSet) {
							List<Character> sequenceChars = new ArrayList<Character>();
							for (int i = 0; i < pep.length() - 1; i++) {
								sequenceChars.add(pep.charAt(i));
							}
							Collections.shuffle(sequenceChars, random);

							StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size() + 1);
							for (Character c : sequenceChars) {
								shuffledSequence.append(c);
							}
							shuffledSequence.append(pep.charAt(pep.length() - 1));
							String shufPep = shuffledSequence.toString();

							shufFastaWriter.println(">pro" + id + " pro" + id);
							shufFastaWriter.println(pep);

							shufFastaWriter.println(">shuf_pro" + id + " shuf_pro" + id);
							shufFastaWriter.println(shufPep);

							shufPepWriter.println(shufPep + "\t" + pep);
							id++;
						}
						shufFastaWriter.close();
						shufPepWriter.close();

					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in writing the shuffled database file " + shufFastaFile,
								e);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in writing the shuffled database file " + shufFastaFile);

						return false;
					}
				} else {
					LOGGER.info(getTaskName() + ": find the shuffled database file in " + shufFastaFile
							+ ", go to the next step");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": find the shuffled database file in " + shufFastaFile + ", go to the next step");
				}

				File shufLibFile = new File(resultFolderFile, "shuffled.speclib");

				if (!shufLibFile.exists() || shufLibFile.length() == 0) {

					File tempLibFile = new File(resultFolderFile, "shuffled.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(shufLibFile);
					} else {
						diaNNTask.addTask(diannPar, shufFastaFile.getAbsolutePath(), shufLibFile.getAbsolutePath(),
								false);
						diaNNTask.run(2);

						if (tempLibFile.exists()) {
							tempLibFile.renameTo(shufLibFile);
						} else {

							LOGGER.error(getTaskName() + ": error in writing the shuffled library file " + shufLibFile);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in writing the shuffled library file " + shufLibFile);

							return false;
						}
					}

				} else {

					LOGGER.info(getTaskName() + ": find the shuffled library file in " + shufLibFile
							+ ", go to the next step");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": find the shuffled library file in " + shufLibFile + ", go to the next step");
				}

				File shufPsmFile = new File(resultFolderFile, "shuffled.tsv");
				if (!shufLibFile.exists() || shufLibFile.length() == 0) {
					diaNNTask.addTask(diannPar, diaFiles, shufPsmFile.getAbsolutePath(), shufLibFile.getAbsolutePath());
					diaNNTask.run(diaFiles.length * 4);
				}

			} else {

				setProgress(75);

				HashSet<String> pepSet = new HashSet<String>();
				for (int i = 0; i < separateResultFolders.length; i++) {

					File firstFile = new File(new File(separateResultFolders[i], "firstSearch"), "firstSearch.tsv");
					if (firstFile.exists()) {
						try (BufferedReader reader = new BufferedReader(new FileReader(firstFile))) {
							String pline = reader.readLine();
							String[] title = pline.split("\t");
							int seqid = -1;
							for (int j = 0; j < title.length; j++) {
								if (title[j].equals("Stripped.Sequence")) {
									seqid = j;
								}
							}
							while ((pline = reader.readLine()) != null) {
								String[] cs = pline.split("\t");
								pepSet.add(cs[seqid]);
							}
							reader.close();
						} catch (IOException e) {
							LOGGER.error(
									getTaskName() + ": error in reading the library search result file " + firstFile,
									e);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in reading the library search result file " + firstFile);

							return false;
						}
					}

					File secondFile = new File(new File(separateResultFolders[i], "secondSearch"), "secondSearch.tsv");
					if (secondFile.exists()) {
						try (BufferedReader reader = new BufferedReader(new FileReader(secondFile))) {
							String pline = reader.readLine();
							String[] title = pline.split("\t");
							int seqid = -1;
							for (int j = 0; j < title.length; j++) {
								if (title[j].equals("Stripped.Sequence")) {
									seqid = j;
								}
							}
							while ((pline = reader.readLine()) != null) {
								String[] cs = pline.split("\t");
								pepSet.add(cs[seqid]);
							}
							reader.close();
						} catch (IOException e) {
							LOGGER.error(
									getTaskName() + ": error in reading the library search result file " + secondFile,
									e);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in reading the library search result file " + secondFile);

							return false;
						}
					}
					
					File thirdFile = new File(new File(separateResultFolders[i], "thirdSearch"), "thirdSearch.tsv");
					if (thirdFile.exists()) {
						try (BufferedReader reader = new BufferedReader(new FileReader(thirdFile))) {
							String pline = reader.readLine();
							String[] title = pline.split("\t");
							int seqid = -1;
							for (int j = 0; j < title.length; j++) {
								if (title[j].equals("Stripped.Sequence")) {
									seqid = j;
								}
							}
							while ((pline = reader.readLine()) != null) {
								String[] cs = pline.split("\t");
								pepSet.add(cs[seqid]);
							}
							reader.close();
						} catch (IOException e) {
							LOGGER.error(
									getTaskName() + ": error in reading the library search result file " + thirdFile,
									e);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in reading the library search result file " + thirdFile);

							return false;
						}
					}
					
					File lastFile = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".tsv");
					if (!lastFile.exists()) {
						lastFile = new File(separateResultFolders[i], "lastSearch.tsv");
					}
					if (lastFile.exists()) {
						try (BufferedReader reader = new BufferedReader(new FileReader(lastFile))) {
							String pline = reader.readLine();
							String[] title = pline.split("\t");
							int seqid = -1;
							for (int j = 0; j < title.length; j++) {
								if (title[j].equals("Stripped.Sequence")) {
									seqid = j;
								}
							}
							while ((pline = reader.readLine()) != null) {
								String[] cs = pline.split("\t");
								pepSet.add(cs[seqid]);
							}
							reader.close();
						} catch (IOException e) {
							LOGGER.error(
									getTaskName() + ": error in reading the library search result file " + lastFile, e);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in reading the library search result file " + lastFile);

							return false;
						}
					}
				}
				
				File hostFile = new File(metaPar.getResult(), "host");
				File hostTsvFile = new File(hostFile, "host.tsv");
				if (hostTsvFile.exists()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(hostTsvFile))) {
						String pline = reader.readLine();
						String[] title = pline.split("\t");
						int seqid = -1;
						for (int j = 0; j < title.length; j++) {
							if (title[j].equals("Stripped.Sequence")) {
								seqid = j;
							}
						}
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							pepSet.add(cs[seqid]);
						}
						reader.close();
					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in reading the library search result file " + hostTsvFile,
								e);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in reading the library search result file " + hostTsvFile);

						return false;
					}
				}

				File fastaFile = new File(resultFolderFile, "combined.fasta");
				if (!fastaFile.exists() || fastaFile.length() == 0) {
					try (PrintWriter fastaWriter = new PrintWriter(fastaFile)) {

						int id = 0;
						for (String pep : pepSet) {

							fastaWriter.println(">pro" + id + " pro" + id);
							fastaWriter.println(pep);
							id++;
						}
						fastaWriter.close();
					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in writing the protein database file " + fastaFile, e);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in writing the protein database file " + fastaFile);

						return false;
					}
				}

				File combineLibFile = new File(resultFolderFile, "combined.speclib");

				if (!combineLibFile.exists() || combineLibFile.length() == 0) {

					File tempLibFile = new File(resultFolderFile, "combined.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(combineLibFile);
					} else {
						diaNNTask.addTask(diannPar, fastaFile.getAbsolutePath(), combineLibFile.getAbsolutePath(),
								false);
						diaNNTask.run(2);

						if (tempLibFile.exists()) {
							tempLibFile.renameTo(combineLibFile);
						} else {

							LOGGER.error(
									getTaskName() + ": error in writing the combined library file " + combineLibFile);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in writing the combined library file " + combineLibFile);

							return false;
						}
					}

				} else {

					LOGGER.info(getTaskName() + ": find the combined library file in " + combineLibFile
							+ ", go to the next step");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": find the combined library file in " + combineLibFile + ", go to the next step");
				}

				diaNNTask.addTask(diannPar, diaFiles, combineFile.getAbsolutePath(), combineLibFile.getAbsolutePath());
				diaNNTask.run(diaFiles.length * 4);

				File shufFastaFile = new File(resultFolderFile, "shuffled.fasta");
				File shufPepFile = new File(resultFolderFile, "shuffledPeptide.tsv");

				if (!shufFastaFile.exists() || shufFastaFile.length() == 0) {
					Random random = new Random();

					try (PrintWriter shufFastaWriter = new PrintWriter(shufFastaFile)) {

						PrintWriter shufPepWriter = new PrintWriter(shufPepFile);
						shufPepWriter.println("Shuffled peptide\tOriginal peptide");

						int id = 0;
						for (String pep : pepSet) {
							List<Character> sequenceChars = new ArrayList<Character>();
							for (int i = 0; i < pep.length() - 1; i++) {
								sequenceChars.add(pep.charAt(i));
							}
							Collections.shuffle(sequenceChars, random);

							StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size() + 1);
							for (Character c : sequenceChars) {
								shuffledSequence.append(c);
							}
							shuffledSequence.append(pep.charAt(pep.length() - 1));
							String shufPep = shuffledSequence.toString();

							shufFastaWriter.println(">pro" + id + " pro" + id);
							shufFastaWriter.println(pep);

							shufFastaWriter.println(">shuf_pro" + id + " shuf_pro" + id);
							shufFastaWriter.println(shufPep);

							shufPepWriter.println(shufPep + "\t" + pep);
							id++;
						}
						shufFastaWriter.close();
						shufPepWriter.close();

					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in writing the shuffled database file " + shufFastaFile,
								e);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in writing the shuffled database file " + shufFastaFile);

						return false;
					}
				} else {
					LOGGER.info(getTaskName() + ": find the shuffled database file in " + shufFastaFile
							+ ", go to the next step");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": find the shuffled database file in " + shufFastaFile + ", go to the next step");
				}

				File shufLibFile = new File(resultFolderFile, "shuffled.speclib");

				if (!shufLibFile.exists() || shufLibFile.length() == 0) {

					File tempLibFile = new File(resultFolderFile, "shuffled.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(shufLibFile);
					} else {
						diaNNTask.addTask(diannPar, shufFastaFile.getAbsolutePath(), shufLibFile.getAbsolutePath(),
								false);
						diaNNTask.run(2);

						if (tempLibFile.exists()) {
							tempLibFile.renameTo(shufLibFile);
						} else {

							LOGGER.error(getTaskName() + ": error in writing the shuffled library file " + shufLibFile);
							System.err.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": error in writing the shuffled library file " + shufLibFile);

							return false;
						}
					}

				} else {

					LOGGER.info(getTaskName() + ": find the shuffled library file in " + shufLibFile
							+ ", go to the next step");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": find the shuffled library file in " + shufLibFile + ", go to the next step");
				}

				File shufPsmFile = new File(resultFolderFile, "shuffled.tsv");
				if (!shufPsmFile.exists() || shufPsmFile.length() == 0) {
					diaNNTask.addTask(diannPar, diaFiles, shufPsmFile.getAbsolutePath(), shufLibFile.getAbsolutePath());
					diaNNTask.run(diaFiles.length * 4);
				}
			}

			LOGGER.info(getTaskName() + ": library search finished");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library search finished");

		} else {

			HashSet<String> pepSet = new HashSet<String>();
			try (BufferedReader reader = new BufferedReader(new FileReader(combineFile))) {
				String line = reader.readLine();
				String[] title = line.split("\t");
				int seqid = -1;
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Stripped.Sequence")) {
						seqid = i;
					}
				}

				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					pepSet.add(cs[seqid]);
				}
				reader.close();

			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in reading the combined result file " + combineFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the combined result file " + combineFile);

				return false;
			}

			File shufFastaFile = new File(resultFolderFile, "shuffled.fasta");
			File shufPepFile = new File(resultFolderFile, "shuffledPeptide.tsv");

			if (!shufFastaFile.exists() || shufFastaFile.length() == 0) {
				Random random = new Random();

				try (PrintWriter shufFastaWriter = new PrintWriter(shufFastaFile)) {

					PrintWriter shufPepWriter = new PrintWriter(shufPepFile);
					shufPepWriter.println("Shuffled peptide\tOriginal peptide");

					int id = 0;
					for (String pep : pepSet) {
						List<Character> sequenceChars = new ArrayList<Character>();
						for (int i = 0; i < pep.length() - 1; i++) {
							sequenceChars.add(pep.charAt(i));
						}
						Collections.shuffle(sequenceChars, random);

						StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size() + 1);
						for (Character c : sequenceChars) {
							shuffledSequence.append(c);
						}
						shuffledSequence.append(pep.charAt(pep.length() - 1));
						String shufPep = shuffledSequence.toString();

						shufFastaWriter.println(">pro" + id + " pro" + id);
						shufFastaWriter.println(pep);

						shufFastaWriter.println(">shuf_pro" + id + " shuf_pro" + id);
						shufFastaWriter.println(shufPep);

						shufPepWriter.println(shufPep + "\t" + pep);
						id++;
					}
					shufFastaWriter.close();
					shufPepWriter.close();

				} catch (IOException e) {
					LOGGER.error(getTaskName() + ": error in writing the shuffled database file " + shufFastaFile, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in writing the shuffled database file " + shufFastaFile);

					return false;
				}
			} else {
				LOGGER.info(getTaskName() + ": find the shuffled database file in " + shufFastaFile
						+ ", go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": find the shuffled database file in " + shufFastaFile + ", go to the next step");
			}

			File shufLibFile = new File(resultFolderFile, "shuffled.speclib");

			if (!shufLibFile.exists() || shufLibFile.length() == 0) {

				File tempLibFile = new File(resultFolderFile, "shuffled.predicted.speclib");
				if (tempLibFile.exists()) {
					tempLibFile.renameTo(shufLibFile);
				} else {
					diaNNTask.addTask(diannPar, shufFastaFile.getAbsolutePath(), shufLibFile.getAbsolutePath(), false);
					diaNNTask.run(2);

					if (tempLibFile.exists()) {
						tempLibFile.renameTo(shufLibFile);
					} else {

						LOGGER.error(getTaskName() + ": error in writing the shuffled library file " + shufLibFile);
						System.err.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in writing the shuffled library file " + shufLibFile);

						return false;
					}
				}

			} else {

				LOGGER.info(
						getTaskName() + ": find the shuffled library file in " + shufLibFile + ", go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": find the shuffled library file in " + shufLibFile + ", go to the next step");
			}

			File shufPsmFile = new File(resultFolderFile, "shuffled.tsv");
			if (!shufPsmFile.exists() || shufPsmFile.length() == 0) {
				diaNNTask.addTask(diannPar, diaFiles, shufPsmFile.getAbsolutePath(), shufLibFile.getAbsolutePath());
				diaNNTask.run(diaFiles.length * 4);
			}
		}

		cleanTempFiles();
		
		setProgress(85);

		return true;
	}
	
	private HashMap<String, HashSet<Integer>> exportPeptideTxt(FlashLfqQuanPeptide[] peps, HashMap<String, HashSet<String>> pepProMap,
			HashMap<String, DiaNNPrecursor> pepPpMap, HashMap<String, Integer> proIdMap) {

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		this.final_pep_txt = new File(metaPar.getResult(), "final_peptides.tsv");

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(this.final_pep_txt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing peptides to " + this.final_pep_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing peptides to "
					+ this.final_pep_txt.getName());
		}

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
		titlesb.append("Q-value").append("\t");
		titlesb.append("Intensity").append("\t");

		for (int i = 0; i < quanExpNames.length; i++) {
			if (this.expNameMap.containsKey(quanExpNames[i])) {
				titlesb.append("Intensity " + expNameMap.get(quanExpNames[i])).append("\t");
			} else {
				titlesb.append("Intensity " + quanExpNames[i]).append("\t");
			}
		}

		titlesb.append("id").append("\t");
		titlesb.append("Protein group IDs").append("\t");

		writer.println(titlesb);

		HashMap<String, HashSet<Integer>> proPepIdMap = new HashMap<String, HashSet<Integer>>();
		int pepid = 0;
		for (int i = 0; i < peps.length; i++) {

			String sequence = peps[i].getSequence();
			String modSeq = peps[i].getModSeq();

			if (pepPpMap.containsKey(modSeq)) {
				DiaNNPrecursor diannPp = pepPpMap.get(modSeq);
				StringBuilder sb = new StringBuilder();
				sb.append(modSeq).append("\t");
				sb.append(sequence).append("\t");
				sb.append(sequence.length()).append("\t");
				sb.append(diannPp.getMissCleavage()).append("\t");
				sb.append(diannPp.getMass()).append("\t");

				HashSet<String> proSet = pepProMap.get(sequence);
				StringBuilder pidSb = new StringBuilder();
				if (proSet != null && proSet.size() > 0) {
					for (String pro : proSet) {
						sb.append(pro).append(";");
						if (proIdMap.containsKey(pro)) {
							pidSb.append(proIdMap.get(pro)).append(";");

							if (proPepIdMap.containsKey(pro)) {
								proPepIdMap.get(pro).add(i);
							} else {
								HashSet<Integer> pset = new HashSet<Integer>();
								pset.add(i);
								proPepIdMap.put(pro, pset);
							}
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					if (pidSb.length() > 1) {
						pidSb.deleteCharAt(pidSb.length() - 1);
					}
				} else {
					continue;
				}

				sb.append("\t").append(diannPp.getCharge());
				sb.append("\t").append(diannPp.getCscore());
				sb.append("\t").append(diannPp.getPEP());
				sb.append("\t").append(diannPp.getQvalue());

				peps[i].setScore(diannPp.getCscore());

				double totalIntensity = 0;
				double[] intensity = peps[i].getIntensity();
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append("\t").append(totalIntensity);
				for (int j = 0; j < intensity.length; j++) {
					sb.append("\t").append(intensity[j]);
				}

				sb.append("\t").append(pepid++);
				sb.append("\t").append(pidSb);
				writer.println(sb);
			}
		}

		writer.close();

		LOGGER.info(getTaskName() + ": peptide report has been exported to " + final_pep_txt);
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide report has been exported to " + final_pep_txt);

		return proPepIdMap;
	}

	/**
	 * 
	 */
	private void exportProteinsTxt(FlashLfqQuanPeptide[] peps, String[] allPros, HashMap<String, ProteinGroup> proGroupMap,
			HashMap<String, HashSet<String>> razorProPepMap, HashMap<String, HashSet<String>> allProPepMap,
			HashMap<String, double[]> proIntensityMap, HashMap<String, HashSet<Integer>> proPepIdMap) {

		LOGGER.info(getTaskName() + ": exporting protein report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting protein report started");

		this.final_pro_txt = new File(metaPar.getResult(), "final_proteins.tsv");

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(this.final_pro_txt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing final protein result to " + this.final_pro_txt.getName(), e);
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

		for (int i = 0; i < quanExpNames.length; i++) {
			if (this.expNameMap.containsKey(quanExpNames[i])) {
				titlesb.append("Intensity " + expNameMap.get(quanExpNames[i])).append("\t");
			} else {
				titlesb.append("Intensity " + quanExpNames[i]).append("\t");
			}
		}

		titlesb.append("id").append("\t");
		titlesb.append("Peptide IDs").append("\t");
		titlesb.append("Peptide is razor");

		writer.println(titlesb);

		DecimalFormat df = FormatTool.getDFE4();
		for (int i = 0; i < allPros.length; i++) {

			StringBuilder sb = new StringBuilder();
			double[] intensity = proIntensityMap.get(allPros[i]);

			if (intensity == null) {
				intensity = new double[quanExpNames.length];
				Arrays.fill(intensity, 0.0);
			}

			ProteinGroup pg = proGroupMap.get(allPros[i]);
			HashSet<String> sameSet = pg.getSameSet();
			HashSet<Integer> pepIdSet = proPepIdMap.get(allPros[i]);

			Integer[] pepIds = pepIdSet.toArray(new Integer[pepIdSet.size()]);
			Arrays.sort(pepIds);
			boolean[] isRazorPep = new boolean[pepIds.length];

			int proCount = 1 + sameSet.size();
			int[] pepCountAll = new int[proCount];
			int[] pepCountRazor = new int[proCount];
			double proScore = 0;

			for (int j = 0; j < pepIds.length; j++) {
				String sequence = peps[pepIds[j]].getSequence();
				pepCountAll[0]++;
				proScore += peps[pepIds[j]].getScore();
				if (razorProPepMap.get(allPros[i]).contains(sequence)) {
					pepCountRazor[0]++;
					isRazorPep[j] = true;
				}
			}

			int proNameId = 1;
			StringBuilder sameSb = new StringBuilder();
			for (String p : sameSet) {
				sameSb.append(";").append(p);
				for (int j = 0; j < pepIds.length; j++) {
					String sequence = peps[pepIds[j]].getSequence();
					if (allProPepMap.get(p).contains(sequence)) {
						pepCountAll[proNameId]++;
						if (isRazorPep[j]) {
							pepCountRazor[proNameId]++;
						}
					}
				}
				proNameId++;
			}

			if (sameSb.length() > 0) {
				sameSb.deleteCharAt(sameSb.length() - 1);
			}

			sb.append(allPros[i]).append(sameSb).append("\t");
			sb.append(allPros[i]).append(sameSb).append("\t");

			for (int j = 0; j < pepCountAll.length; j++) {
				sb.append(pepCountAll[j]).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\t");

			for (int j = 0; j < pepCountRazor.length; j++) {
				sb.append(pepCountRazor[j]).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\t");

			sb.append(proCount).append("\t");
			sb.append(pepIds.length).append("\t");

			sb.append(FormatTool.getDF2().format(proScore)).append("\t");

			double totalIntensity = 0;

			for (int j = 0; j < intensity.length; j++) {
				totalIntensity += intensity[j];
			}

			sb.append(df.format(totalIntensity)).append("\t");
			for (int j = 0; j < intensity.length; j++) {
				sb.append(df.format(intensity[j])).append("\t");
			}

			sb.append(i).append("\t");

			for (int j = 0; j < pepIds.length; j++) {
				sb.append(pepIds[j]).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\t");

			for (int j = 0; j < isRazorPep.length; j++) {
				sb.append(isRazorPep[j]).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();

		LOGGER.info(getTaskName() + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein report has been exported to "
				+ final_pro_txt.getName());
	}
	
	private int[] buildSelfModel() {

		LOGGER.info(getTaskName() + ": building self-model started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": building self-model started");

		File selfModelFolder = new File(metaPar.getResult(), "models");
		if (!selfModelFolder.exists()) {
			selfModelFolder.mkdir();
		}

		int[] typeIds = new int[this.separateResultFolders.length];

		File modelInfoFile = new File(selfModelFolder, "modelInfo.tsv");
		if (!modelInfoFile.exists() || modelInfoFile.length() == 0) {

			ArrayList<HashMap<String, Double>> totaList = new ArrayList<HashMap<String, Double>>();

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File firstFile = new File(separateResultFolders[i], "firstSearch");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				if (round1TsvFile.exists()) {
					HashMap<String, Double> map = new HashMap<String, Double>();
					try (BufferedReader reader = new BufferedReader(new FileReader(round1TsvFile))) {
						String line = reader.readLine();
						String[] title = line.split("\t");
						int id = -1;
						for (int j = 0; j < title.length; j++) {
							if (title[j].equals("Precursor.Normalised")) {
								id = j;
							}
						}
						while ((line = reader.readLine()) != null) {
							String[] cs = line.split("\t");
							if (map.containsKey(cs[14])) {
								map.put(cs[14], map.get(cs[14]) + Double.parseDouble(cs[id]));
							} else {
								map.put(cs[14], Double.parseDouble(cs[id]));
							}
						}
						reader.close();
						totaList.add(map);
					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in reading the round1 search result", e);
						System.out.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in reading the round1 search result");
					}
				}
			}

			PearsonsCorrelation pc = new PearsonsCorrelation();
			double[][] correlation = new double[totaList.size()][totaList.size()];
			for (int i = 0; i < totaList.size(); i++) {
				for (int j = i; j < totaList.size(); j++) {
					if (i == j) {
						correlation[i][j] = 1.0;
					} else {
						HashMap<String, Double> mapi = totaList.get(i);
						HashMap<String, Double> mapj = totaList.get(j);
						HashSet<String> totalSet = new HashSet<String>();
						totalSet.addAll(mapi.keySet());
						totalSet.addAll(mapj.keySet());
						double[] x = new double[totalSet.size()];
						double[] y = new double[totalSet.size()];

						int id = 0;
						for (String pep : totalSet) {
							if (mapi.containsKey(pep)) {
								double intensity = mapi.get(pep);
								if (intensity > 0) {
									x[id] = Math.log(intensity);
								} else {
									x[id] = 0.0;
								}
							} else {
								x[id] = 0.0;
							}

							if (mapj.containsKey(pep)) {
								double intensity = mapj.get(pep);
								if (intensity > 0) {
									y[id] = Math.log(intensity);
								} else {
									y[id] = 0.0;
								}
							} else {
								y[id] = 0.0;
							}
							id++;
						}
						double corr = pc.correlation(x, y);
						correlation[i][j] = corr;
						correlation[j][i] = corr;
					}
				}
			}

			HashMap<String, Integer> typeIdMap = new HashMap<String, Integer>();

			String[] fileIds = new String[this.separateResultFolders.length];
			for (int i = 0; i < typeIds.length; i++) {
				StringBuilder idsb = new StringBuilder();
				for (int j = 0; j < correlation[i].length; j++) {
					if (correlation[i][j] > 0) {
						idsb.append(j).append("_");
					}
				}
				fileIds[i] = idsb.toString();
				if (typeIdMap.containsKey(fileIds[i])) {
					typeIds[i] = typeIdMap.get(fileIds[i]);
				} else {
					typeIdMap.put(fileIds[i], typeIdMap.size());
					typeIds[i] = typeIdMap.get(fileIds[i]);
				}
			}

			try (PrintWriter writer = new PrintWriter(modelInfoFile)) {
				StringBuilder titlesb = new StringBuilder();
				titlesb.append("Correlation");

				for (File file : this.separateResultFolders) {
					titlesb.append("\t").append(file.getName());
				}
				titlesb.append("\tModelId");
				titlesb.append("\tFileIds");
				writer.println(titlesb);

				for (int i = 0; i < separateResultFolders.length; i++) {
					StringBuilder sb = new StringBuilder();
					sb.append(separateResultFolders[i].getName());
					for (int j = 0; j < correlation[i].length; j++) {
						sb.append("\t").append(correlation[i][j]);
					}
					sb.append("\t").append(typeIds[i]);
					sb.append("\t").append(fileIds[i]);
					writer.println(sb);
				}

				writer.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in writing the grouped model information", e);
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing the grouped model information");
			}
		}

		HashMap<Integer, int[]> modelFileIdMap = new HashMap<Integer, int[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(modelInfoFile))) {
			String line = reader.readLine();
			int fileId = 0;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				Integer modelId = Integer.parseInt(cs[cs.length - 2]);
				if (!modelFileIdMap.containsKey(modelId)) {
					String[] fileIdStrings = cs[cs.length - 1].split("_");
					int[] fileIds = new int[fileIdStrings.length];
					for (int i = 0; i < fileIds.length; i++) {
						fileIds[i] = Integer.parseInt(fileIdStrings[i]);
					}
					modelFileIdMap.put(modelId, fileIds);
				}
				typeIds[fileId++] = modelId;
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(getTaskName() + ": error in reading the grouped model information", e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading the grouped model information");
		}

		LOGGER.info(getTaskName() + ": reading round1 information finished, model generation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName()
				+ ": reading round1 information finished, model generation started");

		long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
				.getTotalMemorySize() / 1024 / 1024 / 1024;
		final int maxBathCount = (int) (totalMemorySize / 31);

		LOGGER.info(getTaskName() + ": the memory size is " + totalMemorySize + "GB, the maximum batch count is set as "
				+ maxBathCount);
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": the memory size is " + totalMemorySize
				+ "GB, the maximum batch count is set as " + maxBathCount);

		MetaParameterDia diaPar = (MetaParameterDia) metaPar;
		int modelNumber = modelFileIdMap.size();
		ExecutorService executor;

		if (modelNumber < maxBathCount) {
			executor = Executors.newFixedThreadPool(modelNumber);
		} else {
			if (maxBathCount == 0) {
				executor = Executors.newFixedThreadPool(1);
			} else {
				executor = Executors.newFixedThreadPool(maxBathCount);
			}
		}
		
		for (Integer modelId : modelFileIdMap.keySet()) {

			int[] fileIds = modelFileIdMap.get(modelId);
			File modelFolder = new File(selfModelFolder, "model" + modelId);
			if (!modelFolder.exists()) {
				modelFolder.mkdir();
			}

			LOGGER.info(getTaskName() + ": create " + modelFolder.getName() + " started");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": create " + modelFolder.getName()
					+ " started");

			HashMap<String, Double> pepIntenMap = new HashMap<String, Double>();
			for (int i = 0; i < fileIds.length; i++) {

				File firstFile = new File(separateResultFolders[fileIds[i]], "firstSearch");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				if (round1TsvFile.exists()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(round1TsvFile))) {
						String line = reader.readLine();
						String[] title = line.split("\t");
						int seqId = -1;
						int intensityId = -1;
						for (int j = 0; j < title.length; j++) {
							if (title[j].equals("Stripped.Sequence")) {
								seqId = j;
							} else if (title[j].equals("Precursor.Normalised")) {
								intensityId = j;
							}
						}
						while ((line = reader.readLine()) != null) {
							String[] cs = line.split("\t");
							if (pepIntenMap.containsKey(cs[seqId])) {
								pepIntenMap.put(cs[seqId],
										pepIntenMap.get(cs[seqId]) + Double.parseDouble(cs[intensityId]));
							} else {
								pepIntenMap.put(cs[seqId], Double.parseDouble(cs[intensityId]));
							}
						}
						reader.close();
					} catch (IOException e) {
						LOGGER.error(getTaskName() + ": error in combining the grouped quantitative information", e);
						System.out.println(format.format(new Date()) + "\t" + getTaskName()
								+ ": error in combining the grouped quantitative information");
					}
				}
			}

			int batchThreadCount = threadCount / maxBathCount + 1;

			DiaModelTask diaModelTask = new DiaModelTask(diaPar, (MetaSourcesDia) msv, bar1, pepIntenMap,
					modelFolder.getName(), DiaModelTask.RNN, modelFolder, batchThreadCount);

			executor.submit(diaModelTask);
		}

		executor.shutdown();

		try {
			executor.awaitTermination(modelFileIdMap.size() * 12, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			LOGGER.error(getTaskName() + ": building models failed", e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": building models failed");
		}

		return typeIds;
	}
	
	protected boolean separateSearchHap() {

		LOGGER.info(getTaskName() + ": parsing peptide from the libraries started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": parsing peptide from the libraries started");

		MetaParameterDia diaPar = (MetaParameterDia) metaPar;
		String[] usedLibs = diaPar.getLibrary();

		File magDbFile = this.magDb.getCurrentFile();
		File predictedFolder = new File(magDbFile, "predicted");
		File libFolderFile = new File(magDbFile, "libraries");
		File modelFolderFile = new File(magDbFile, "models");

		ArrayList<String> libPathList = new ArrayList<String>();

		for (int i = 0; i < usedLibs.length; i++) {
			if (usedLibs[i].equals("host")) {

				LOGGER.info(getTaskName() + ": host library search started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": host library search started");

				File libFile = new File(new File(libFolderFile, "host"), "host.speclib");

				File hostFile = new File(metaPar.getResult(), "host");
				if (!hostFile.exists()) {
					hostFile.mkdirs();
				}

				File hostTsvFile = new File(hostFile, "host.tsv");
				if (!hostTsvFile.exists()) {

					DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
					diaLibSearchPar.setDiaFiles(diaFiles);
					diaLibSearchPar.setThreadCount(threadCount);
					diaLibSearchPar.setMbr(false);

					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(threadCount);
					diannPar.setMatrices(false);
					diannPar.setRelaxed_prot_inf(true);
					
					diaNNTask.addTask(diannPar, diaLibSearchPar, hostTsvFile.getAbsolutePath(),
							libFile.getAbsolutePath());
					diaNNTask.run(1, diaFiles.length * 12);
				} else {
					LOGGER.info(getTaskName() + ": host library search result already existed in " + hostFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": host library search result already existed in " + hostFile);
				}

				setProgress(15);

				LOGGER.info(getTaskName() + ": host library search finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": host library search finished");

			} else {
				libPathList.add(usedLibs[i]);
			}
		}

		@SuppressWarnings("unchecked")
		HashSet<String>[] initialLibPepSet = new HashSet[diaFiles.length];
		for (int i = 0; i < initialLibPepSet.length; i++) {
			initialLibPepSet[i] = new HashSet<String>();
		}

		String[] libPath = new String[libPathList.size()];
		for (int i = 0; i < libPathList.size(); i++) {
			String usedLibIString = libPathList.get(i);
			File libTsvFile = new File(new File(libFolderFile, usedLibIString), usedLibIString + ".tsv");
			File libFile = new File(new File(libFolderFile, usedLibIString), usedLibIString + ".speclib");
			if (libPath.length == 1) {
				libPath[i] = libFile.getAbsolutePath();
			} else {
				libPath[i] = libTsvFile.getAbsolutePath();
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(libTsvFile))) {
				String pline = reader.readLine();
				String[] title = pline.split("\t");
				int seqid = -1;
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals("PeptideSequence")) {
						seqid = j;
					}
				}
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					for (int j = 0; j < initialLibPepSet.length; j++) {
						initialLibPepSet[j].add(cs[seqid]);
					}
				}
				reader.close();
			} catch (Exception e) {
				LOGGER.error(getTaskName() + ": error in reading " + libTsvFile, e);
				System.err
						.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + libTsvFile);

				return false;
			}
		}

		LOGGER.info(getTaskName() + ": parsing peptide from the libraries finished");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": parsing peptide from the libraries finished");

		LOGGER.info(getTaskName() + ": round 1 library search started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 1 library search started");

		int firstTaskCount = 0;
		if (diaFiles != null && diaFiles.length > 0) {
			this.separateResultFolders = new File[diaFiles.length];
			for (int i = 0; i < diaFiles.length; i++) {
				String name = diaFiles[i].substring(diaFiles[i].lastIndexOf("\\") + 1, diaFiles[i].lastIndexOf("."));
				if (name.endsWith(".d")) {
					name = name.substring(0, name.length() - 2);
				} else if (name.endsWith(".raw")) {
					name = name.substring(0, name.length() - 4);
				}
				this.separateResultFolders[i] = new File(metaPar.getResult(), name);

				if (!this.separateResultFolders[i].exists()) {
					this.separateResultFolders[i].mkdir();
				}

				File firstFile = new File(separateResultFolders[i], "firstSearch");
				if (!firstFile.exists()) {
					firstFile.mkdirs();
				}

				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				if (!round1TsvFile.exists()) {
					firstTaskCount++;
				}
			}
		}

		if (firstTaskCount > 0) {
			int eachThreadCount = firstTaskCount < maxTaskCount ? threadCount / firstTaskCount + 1
					: threadCount / maxTaskCount + 1;

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File firstFile = new File(separateResultFolders[i], "firstSearch");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				if (!round1TsvFile.exists()) {

					DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
					diaLibSearchPar.setDiaFiles(new String[] { diaFiles[i] });
					diaLibSearchPar.setThreadCount(eachThreadCount);
					diaLibSearchPar.setMbr(false);

					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(eachThreadCount);
					diannPar.setMatrices(false);
					diannPar.setRelaxed_prot_inf(true);
					diaNNTask.addTask(diannPar, diaLibSearchPar, round1TsvFile.getAbsolutePath(), libPath);
				}
			}
			if (firstTaskCount < maxTaskCount) {
				diaNNTask.run(firstTaskCount, firstTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, firstTaskCount * 12);
			}
		}

		setProgress(20);

		LOGGER.info(getTaskName() + ": round 1 library search finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 1 library search finished");

		String[] usedModels = diaPar.getModels();
		ArrayList<String> modelPathList = new ArrayList<String>();
		for (int i = 0; i < usedModels.length; i++) {
			File modelFile = new File(new File(modelFolderFile, usedModels[i]), usedModels[i] + "_pep_rank.tsv");
			modelPathList.add(modelFile.getAbsolutePath());
		}

		if (this.selfModelSearch) {
			this.selfModelIds = this.buildSelfModel();
		}
		
		setProgress(35);

		firstTaskCount = 0;
		for (int i = 0; i < separateResultFolders.length; i++) {
			File firstFile = new File(separateResultFolders[i], "firstSearch");
			File round1DbFile = new File(firstFile, "firstSearch.fasta");
			if (!round1DbFile.exists() || round1DbFile.length() == 0) {
				firstTaskCount++;
			}
		}
		
		if (firstTaskCount > 0) {
			int eachThreadCount = firstTaskCount < maxTaskCount ? threadCount / firstTaskCount + 1
					: threadCount / maxTaskCount + 1;			
			ExecutorService executor = threadCount > eachThreadCount
					? Executors.newFixedThreadPool(threadCount / eachThreadCount)
					: Executors.newSingleThreadExecutor();
			
			for (int i = 0; i < separateResultFolders.length; i++) {
				final int index = i;
				File firstFile = new File(separateResultFolders[index], "firstSearch");
				File round1DbFile = new File(firstFile, "firstSearch.fasta");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				if (!round1DbFile.exists() || round1DbFile.length() == 0) {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							HashSet<String> pepSet1 = new HashSet<String>();
							try (BufferedReader reader = new BufferedReader(new FileReader(round1TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet1.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round1TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading "
										+ round1TsvFile);
							}

							HashSet<String> genomeSet1 = getGenomeSet(firstFile, 0.85, pepSet1, eachThreadCount);

							LOGGER.info(getTaskName() + ": generating database for " + separateResultFolders[index] + ", "
									+ pepSet1.size() + " peptide sequences were identified, " + genomeSet1.size()
									+ " MAGs were used in this step");
							System.out.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": generating database for " + separateResultFolders[index] + ", " + pepSet1.size()
									+ " peptide sequences were identified, " + genomeSet1.size()
									+ " MAGs were used in this step");
							
							ArrayList<String> modelListI = new ArrayList<String>(modelPathList);
							if (selfModelIds != null) {
								File selfModelFolder = new File(metaPar.getResult(), "models");
								File modelIFile = new File(selfModelFolder, "model" + selfModelIds[index]);
								File pepRankFile = new File(modelIFile, "model" + selfModelIds[index] + "_pep_rank.tsv");
								modelListI.add(pepRankFile.getAbsolutePath());
							}
							String[] modelPath = modelListI.toArray(String[]::new);
							if (modelPath.length > 0) {
								dbFromModelPeptide(modelPath, genomeSet1, pepSet1, round1DbFile, 1500000);
							} else {
								File[] predictedFiles = predictedFolder.listFiles(new FilenameFilter() {

									@Override
									public boolean accept(File dir, String name) {
										// TODO Auto-generated method stub
										name = name.substring(0, name.indexOf("."));
										return genomeSet1.contains(name);
									}
								});
								dbFromPredictedPeptide(predictedFiles, pepSet1, round1DbFile, 1500000);
							}
						}
					});
				} else {

					LOGGER.info(getTaskName() + ": the refined database already exists in " + round1DbFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": the refined database already exists in " + round1DbFile);
				}
			}

			try {
				executor.shutdown();
				boolean finish = executor.awaitTermination(diaFiles.length, TimeUnit.HOURS);
				if (finish) {
					LOGGER.info(getTaskName() + ": analyzing round 1 search result finished");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": analyzing round 1 search result finished");
				} else {
					LOGGER.info(getTaskName() + ": analyzing round 1 search result failed");
					System.out.println(
							format.format(new Date()) + "\t" + getTaskName() + ": analyzing round 1 search result failed");
					return false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": analyzing round 1 search result failed", e);
				System.out.println(
						format.format(new Date()) + "\t" + getTaskName() + ": analyzing round 1 search result failed");
				return false;
			}
		}

		LOGGER.info(getTaskName() + ": round 2 library search started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 2 library search started");

		int secondTaskCount = 0;
		for (int i = 0; i < separateResultFolders.length; i++) {
			File secondFile = new File(separateResultFolders[i], "secondSearch");
			if (!secondFile.exists()) {
				secondFile.mkdirs();
			}

			File round2TsvFile = new File(secondFile, "secondSearch.tsv");
			if (!round2TsvFile.exists()) {
				secondTaskCount++;
			}
		}

		if (secondTaskCount > 0) {
			int eachThreadCount = secondTaskCount < maxTaskCount ? threadCount / secondTaskCount + 1
					: threadCount / maxTaskCount + 1;
			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File firstFile = new File(separateResultFolders[i], "firstSearch");
				File round1DbFile = new File(firstFile, "firstSearch.fasta");

				File secondFile = new File(separateResultFolders[i], "secondSearch");
				File round2LibFile = new File(secondFile, "secondSearch.speclib");
				if (!round2LibFile.exists()) {
					File tempLibFile = new File(secondFile, "secondSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round2LibFile);
					} else {
						diannPar.setThreads(eachThreadCount);
						diaNNTask.addTask(diannPar, round1DbFile.getAbsolutePath(), round2LibFile.getAbsolutePath(),
								false);
					}
				}
			}
			if (secondTaskCount < maxTaskCount) {
				diaNNTask.run(secondTaskCount, secondTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, secondTaskCount * 12);
			}

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File secondFile = new File(separateResultFolders[i], "secondSearch");
				File round2TsvFile = new File(secondFile, "secondSearch.tsv");
				File round2LibFile = new File(secondFile, "secondSearch.speclib");
				if (!round2LibFile.exists()) {
					File tempLibFile = new File(secondFile, "secondSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round2LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round2LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round2LibFile + " is not found");
						return false;
					}
				}
				if (!round2TsvFile.exists()) {
					DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
					diaLibSearchPar.setDiaFiles(new String[] { diaFiles[i] });
					diaLibSearchPar.setThreadCount(eachThreadCount);
					diaLibSearchPar.setMbr(false);

					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(eachThreadCount);
					diannPar.setMatrices(false);
					diannPar.setRelaxed_prot_inf(true);
					diaNNTask.addTask(diannPar, new String[] { diaFiles[i] }, round2TsvFile.getAbsolutePath(),
							round2LibFile.getAbsolutePath());
				}
			}
			if (secondTaskCount < maxTaskCount) {
				diaNNTask.run(secondTaskCount, secondTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, secondTaskCount * 12);
			}
		}

		setProgress(50);

		LOGGER.info(getTaskName() + ": round 2 library search finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 2 library search finished");

		secondTaskCount = 0;
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File secondFile = new File(separateResultFolders[i], "secondSearch");
			File round2DbFile = new File(secondFile, "secondSearch.fasta");
			if (!round2DbFile.exists() || round2DbFile.length() == 0) {
				secondTaskCount++;
			}
		}
		
		if (secondTaskCount > 0) {

			int eachThreadCount = secondTaskCount < maxTaskCount ? threadCount / secondTaskCount + 1
					: threadCount / maxTaskCount + 1;
			ExecutorService executor = threadCount > eachThreadCount
					? Executors.newFixedThreadPool(threadCount / eachThreadCount)
					: Executors.newSingleThreadExecutor();

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				final int index = i;
				File firstFile = new File(separateResultFolders[index], "firstSearch");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				File secondFile = new File(separateResultFolders[index], "secondSearch");
				File round2DbFile = new File(secondFile, "secondSearch.fasta");
				File round2TsvFile = new File(secondFile, "secondSearch.tsv");
				if (!round2DbFile.exists() || round2DbFile.length() == 0) {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							HashSet<String> pepSet2 = new HashSet<String>();
							try (BufferedReader reader = new BufferedReader(new FileReader(round1TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet2.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round1TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading " + round1TsvFile);
							}
							try (BufferedReader reader = new BufferedReader(new FileReader(round2TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet2.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round2TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading " + round2TsvFile);
							}

							HashSet<String> genomeSet2 = getGenomeSet(secondFile, 0.9, pepSet2, eachThreadCount);

							HashSet<String> excludeSet = new HashSet<String>();
							try (BufferedReader firstLibReader = new BufferedReader(
									new FileReader(new File(firstFile, "firstSearch.fasta")))) {

								String pline = null;
								while ((pline = firstLibReader.readLine()) != null) {
									if (!pline.startsWith(">")) {
										excludeSet.add(pline);
									}
								}
								firstLibReader.close();

							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading fasta file from " + firstFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading fasta file from " + firstFile);
							}

							excludeSet.retainAll(initialLibPepSet[index]);
							Iterator<String> it = excludeSet.iterator();
							while (it.hasNext()) {
								String pep = it.next();
								if (pepSet2.contains(pep)) {
									it.remove();
								}
							}

							LOGGER.info(getTaskName() + ": generating database for " + separateResultFolders[index]
									+ ", " + pepSet2.size() + " peptide sequences were identified, " + genomeSet2.size()
									+ " MAGs were used in this step, the size of the excluded set is "
									+ excludeSet.size());
							System.out.println(format.format(new Date()) + "\t" + getTaskName()
									+ ": generating database for " + separateResultFolders[index] + ", "
									+ pepSet2.size() + " peptide sequences were identified, " + genomeSet2.size()
									+ " MAGs were used in this step, the size of the excluded set is "
									+ excludeSet.size());

							ArrayList<String> modelListI = new ArrayList<String>(modelPathList);
							if (selfModelIds != null) {
								File selfModelFolder = new File(metaPar.getResult(), "models");
								File modelIFile = new File(selfModelFolder, "model" + selfModelIds[index]);
								File pepRankFile = new File(modelIFile,
										"model" + selfModelIds[index] + "_pep_rank.tsv");
								modelListI.add(pepRankFile.getAbsolutePath());
							}

							String[] modelPath = modelListI.toArray(String[]::new);
							if (modelPath.length > 0) {
								dbFromModelPeptide(modelPath, genomeSet2, pepSet2, excludeSet, round2DbFile, 2000000);
							} else {
								File[] predictedFiles = predictedFolder.listFiles(new FilenameFilter() {

									@Override
									public boolean accept(File dir, String name) {
										// TODO Auto-generated method stub
										name = name.substring(0, name.indexOf("."));
										return genomeSet2.contains(name);
									}
								});

								dbFromPredictedPeptide(predictedFiles, pepSet2, excludeSet, round2DbFile, 2000000);
							}
						}
					});
				} else {
					LOGGER.info(getTaskName() + ": refined database already exists in " + round2DbFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": refined database already exists in " + round2DbFile);
				}
			}

			try {
				executor.shutdown();
				boolean finish = executor.awaitTermination(diaFiles.length, TimeUnit.HOURS);
				if (finish) {
					LOGGER.info(getTaskName() + ": analyzing round 2 search result finished");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": analyzing round 2 search result finished");
				} else {
					LOGGER.info(getTaskName() + ": analyzing round 2 search result failed");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": analyzing round 2 search result failed");
					return false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": analyzing round 2 search result failed", e);
				System.out.println(
						format.format(new Date()) + "\t" + getTaskName() + ": analyzing round 2 search result failed");
				return false;
			}
		}

		LOGGER.info(getTaskName() + ": round 3 library search started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 3 library search started");

		int thirdTaskCount = 0;
		for (int i = 0; i < separateResultFolders.length; i++) {
			File thirdFile = new File(separateResultFolders[i], "thirdSearch");
			if (!thirdFile.exists()) {
				thirdFile.mkdirs();
			}

			File round3TsvFile = new File(thirdFile, "thirdSearch.tsv");
			if (!round3TsvFile.exists()) {
				thirdTaskCount++;
			}
		}

		if (thirdTaskCount > 0) {
			int eachThreadCount = thirdTaskCount < maxTaskCount ? threadCount / thirdTaskCount + 1
					: threadCount / maxTaskCount + 1;
			for (int i = 0; i < this.separateResultFolders.length; i++) {

				File secondFile = new File(separateResultFolders[i], "secondSearch");
				File round2DbFile = new File(secondFile, "secondSearch.fasta");

				File thirdFile = new File(separateResultFolders[i], "thirdSearch");
				File round3LibFile = new File(thirdFile, "thirdSearch.speclib");
				if (!round3LibFile.exists()) {
					File tempLibFile = new File(thirdFile, "thirdSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round3LibFile);
					} else {
						diannPar.setThreads(eachThreadCount);
						diaNNTask.addTask(diannPar, round2DbFile.getAbsolutePath(), round3LibFile.getAbsolutePath(),
								false);
					}
				}
			}
			if (thirdTaskCount < maxTaskCount) {
				diaNNTask.run(thirdTaskCount, thirdTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, thirdTaskCount * 12);
			}

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File thirdFile = new File(separateResultFolders[i], "thirdSearch");
				File round3TsvFile = new File(thirdFile, "thirdSearch.tsv");
				File round3LibFile = new File(thirdFile, "thirdSearch.speclib");
				if (!round3LibFile.exists()) {
					File tempLibFile = new File(thirdFile, "thirdSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round3LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round3LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round3LibFile + " is not found");
						return false;
					}
				}

				if (!round3TsvFile.exists()) {

					DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
					diaLibSearchPar.setThreadCount(eachThreadCount);
					diaLibSearchPar.setMbr(false);

					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(eachThreadCount);
					diannPar.setMatrices(false);
					diannPar.setRelaxed_prot_inf(true);
					diaNNTask.addTask(diannPar, new String[] { diaFiles[i] }, round3TsvFile.getAbsolutePath(),
							round3LibFile.getAbsolutePath());
				}
			}
			if (thirdTaskCount < maxTaskCount) {
				diaNNTask.run(thirdTaskCount, thirdTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, thirdTaskCount * 12);
			}
		}

		LOGGER.info(getTaskName() + ": round 3 library search finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 3 library search finished");

		thirdTaskCount = 0;
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File thirdFile = new File(separateResultFolders[i], "thirdSearch");
			File round3DbFile = new File(thirdFile, "thirdSearch.fasta");
			if (!round3DbFile.exists() || round3DbFile.length() == 0) {
				thirdTaskCount++;
			}
		}
		
		if (thirdTaskCount > 0) {
			int eachThreadCount = thirdTaskCount < maxTaskCount ? threadCount / thirdTaskCount + 1
					: threadCount / maxTaskCount + 1;
			ExecutorService executor = threadCount > eachThreadCount
					? Executors.newFixedThreadPool(threadCount / eachThreadCount)
					: Executors.newSingleThreadExecutor();
			for (int i = 0; i < this.separateResultFolders.length; i++) {
				final int index = i;
				File firstFile = new File(separateResultFolders[index], "firstSearch");
				File secondFile = new File(separateResultFolders[index], "secondSearch");
				File thirdFile = new File(separateResultFolders[index], "thirdSearch");
				File round1TsvFile = new File(firstFile, "firstSearch.tsv");
				File round2TsvFile = new File(secondFile, "secondSearch.tsv");
				File round3TsvFile = new File(thirdFile, "thirdSearch.tsv");
				File round3DbFile = new File(thirdFile, "thirdSearch.fasta");
				if (!round3DbFile.exists() || round3DbFile.length() == 0) {
					executor.submit(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							HashSet<String> pepSet3 = new HashSet<String>();
							try (BufferedReader reader = new BufferedReader(new FileReader(round1TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet3.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round1TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading " + round1TsvFile);
							}
							try (BufferedReader reader = new BufferedReader(new FileReader(round2TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet3.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round2TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading " + round2TsvFile);
							}
							try (BufferedReader reader = new BufferedReader(new FileReader(round3TsvFile))) {
								String pline = reader.readLine();
								String[] title = pline.split("\t");
								int seqId = -1;
								for (int j = 0; j < title.length; j++) {
									if (title[j].equals("Stripped.Sequence")) {
										seqId = j;
									}
								}
								while ((pline = reader.readLine()) != null) {
									String[] cs = pline.split("\t");
									pepSet3.add(cs[seqId]);
								}
								reader.close();
							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading " + round3TsvFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading " + round3TsvFile);
							}

							HashSet<String> genomeSet3 = getGenomeSet(thirdFile, 0.95, pepSet3, eachThreadCount);

							HashSet<String> excludeSet = new HashSet<String>();
							HashSet<String> firstSet = new HashSet<String>();
							try (BufferedReader firstLibReader = new BufferedReader(
									new FileReader(new File(firstFile, "firstSearch.fasta")))) {

								String pline = null;
								while ((pline = firstLibReader.readLine()) != null) {
									if (!pline.startsWith(">")) {
										excludeSet.add(pline);
										firstSet.add(pline);
									}
								}
								firstLibReader.close();

							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading fasta file from " + firstFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading fasta file from " + firstFile);
							}

							HashSet<String> secondSet = new HashSet<String>();
							try (BufferedReader secondLibReader = new BufferedReader(
									new FileReader(new File(secondFile, "secondSearch.fasta")))) {

								String pline = null;
								while ((pline = secondLibReader.readLine()) != null) {
									if (!pline.startsWith(">")) {
										excludeSet.add(pline);
										secondSet.add(pline);
									}
								}
								secondLibReader.close();

							} catch (Exception e) {
								LOGGER.error(getTaskName() + ": error in reading fasta file from " + firstFile, e);
								System.err.println(format.format(new Date()) + "\t" + getTaskName()
										+ ": error in reading fasta file from " + firstFile);
							}

							firstSet.retainAll(secondSet);
							excludeSet.retainAll(initialLibPepSet[index]);
							excludeSet.addAll(firstSet);
							Iterator<String> it = excludeSet.iterator();
							while (it.hasNext()) {
								String pep = it.next();
								if (pepSet3.contains(pep)) {
									it.remove();
								}
							}

							LOGGER.info(getTaskName() + ": generating database for " + separateResultFolders[index]
									+ ", " + genomeSet3.size()
									+ " MAGs were used in this step, the size of the excluded set is "
									+ excludeSet.size());
							System.out.println(
									format.format(new Date()) + "\t" + getTaskName() + ": generating database for "
											+ separateResultFolders[index] + ", " + genomeSet3.size()
											+ " MAGs were used in this step, the size of the excluded set is "
											+ excludeSet.size());

							ArrayList<String> modelListI = new ArrayList<String>(modelPathList);
							if (selfModelIds != null) {
								File selfModelFolder = new File(metaPar.getResult(), "models");
								File modelIFile = new File(selfModelFolder, "model" + selfModelIds[index]);
								File pepRankFile = new File(modelIFile,
										"model" + selfModelIds[index] + "_pep_rank.tsv");
								modelListI.add(pepRankFile.getAbsolutePath());
							}

							String[] modelPath = modelListI.toArray(String[]::new);
							if (modelPath.length > 0) {
								dbFromModelPeptide(modelPath, genomeSet3, pepSet3, excludeSet, round3DbFile, 2500000);
							} else {
								File[] predictedFiles = predictedFolder.listFiles(new FilenameFilter() {

									@Override
									public boolean accept(File dir, String name) {
										// TODO Auto-generated method stub
										name = name.substring(0, name.indexOf("."));
										return genomeSet3.contains(name);
									}
								});

								dbFromPredictedPeptide(predictedFiles, pepSet3, excludeSet, round3DbFile, 2500000);
							}
						}
					});
				} else {
					LOGGER.info(getTaskName() + ": refined database already exists in " + round3DbFile);
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": refined database already exists in " + round3DbFile);
				}
			}

			try {
				executor.shutdown();
				boolean finish = executor.awaitTermination(diaFiles.length, TimeUnit.HOURS);
				if (finish) {
					LOGGER.info(getTaskName() + ": analyzing round 3 search result finished");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": analyzing round 3 search result finished");
				} else {
					LOGGER.info(getTaskName() + ": analyzing round 3 search result failed");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": analyzing round 3 search result failed");
					return false;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": analyzing round 3 search result failed", e);
				System.out.println(
						format.format(new Date()) + "\t" + getTaskName() + ": analyzing round 3 search result failed");
				return false;
			}
		}
		setProgress(65);
		
		LOGGER.info(getTaskName() + ": round 4 library search started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 4 library search started");

		int lastTaskCount = 0;
		for (int i = 0; i < separateResultFolders.length; i++) {
			File lastTsvFile = new File(separateResultFolders[i], "lastSearch.tsv");
			if (!lastTsvFile.exists()) {
				lastTsvFile = new File(separateResultFolders[i], separateResultFolders[i].getName() + ".tsv");
				if (!lastTsvFile.exists()) {
					lastTaskCount++;
				}
			}
		}

		if (lastTaskCount > 0) {
			int eachThreadCount = lastTaskCount < maxTaskCount ? threadCount / lastTaskCount + 1
					: threadCount / maxTaskCount + 1;

			for (int i = 0; i < this.separateResultFolders.length; i++) {

				File thirdFile = new File(separateResultFolders[i], "thirdSearch");
				File round3DbFile = new File(thirdFile, "thirdSearch.fasta");

				File round4LibFile = new File(separateResultFolders[i], "lastSearch.speclib");
				if (!round4LibFile.exists()) {
					File tempLibFile = new File(separateResultFolders[i], "lastSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round4LibFile);
					} else {
						diannPar.setThreads(eachThreadCount);
						diaNNTask.addTask(diannPar, round3DbFile.getAbsolutePath(), round4LibFile.getAbsolutePath(),
								false);
					}
				}
			}
			if (lastTaskCount < maxTaskCount) {
				diaNNTask.run(lastTaskCount, lastTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, lastTaskCount * 12);
			}

			for (int i = 0; i < this.separateResultFolders.length; i++) {
				File round4TsvFile = new File(separateResultFolders[i], "lastSearch.tsv");
				File round4LibFile = new File(separateResultFolders[i], "lastSearch.speclib");
				if (!round4LibFile.exists()) {
					File tempLibFile = new File(separateResultFolders[i], "lastSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round4LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round4LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round4LibFile + " is not found");
						return false;
					}
				}

				if (!round4TsvFile.exists()) {

					DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
					diaLibSearchPar.setThreadCount(eachThreadCount);
					diaLibSearchPar.setMbr(false);

					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(eachThreadCount);
					diannPar.setMatrices(false);
					diannPar.setRelaxed_prot_inf(true);
					diaNNTask.addTask(diannPar, new String[] { diaFiles[i] }, round4TsvFile.getAbsolutePath(),
							round4LibFile.getAbsolutePath());
				}
			}
			if (lastTaskCount < maxTaskCount) {
				diaNNTask.run(lastTaskCount, lastTaskCount * 12);
			} else {
				diaNNTask.run(maxTaskCount, lastTaskCount * 12);
			}
		}

		LOGGER.info(getTaskName() + ": round 4 library search finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 4 library search finished");

		setProgress(80);

		return true;
	}
	
	protected boolean lfQuant() {
		return true;
	}
	
	protected boolean isobaricQuant() {
		return true;
	}
	
	protected String getVersion() {
		return MetaParaIoDia.version;
	}
	
	protected boolean exportReport() {
		
		LOGGER.info(getTaskName() + ": protein inference started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein inference started");

		HashMap<String, HashSet<String>> hostPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> hostProPepMap = new HashMap<String, HashSet<String>>();
		File hostFile = new File(metaPar.getResult(), "host");
		File hostTsvFile = new File(hostFile, "host.tsv");
		if (hostTsvFile.exists()) {
			File magDbFile = this.magDb.getCurrentFile();
			File libFolderFile = new File(magDbFile, "libraries");
			File hostFastaFile = new File(new File(libFolderFile, "host"), "host.predicted.tsv");

			try (BufferedReader reader = new BufferedReader(new FileReader(hostFastaFile))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");

					if (hostPepProMap.containsKey(cs[1])) {
						hostPepProMap.get(cs[1]).add(cs[0]);
					} else {
						HashSet<String> pSet = new HashSet<String>();
						pSet.add(cs[0]);
						hostPepProMap.put(cs[1], pSet);
					}
					if (hostProPepMap.containsKey(cs[0])) {
						hostProPepMap.get(cs[0]).add(cs[1]);
					} else {
						HashSet<String> pSet = new HashSet<String>();
						pSet.add(pline);
						hostProPepMap.put(cs[0], pSet);
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in reading the host database from " + hostFastaFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the host database from " + hostFastaFile);

				return false;
			}
		}
		
		DiaNNResultReader reader = new DiaNNResultReader(resultFolderFile, "combined", this.diannPar.getCut());
		this.diaPp = reader.getDiaNNPrecursors();
		HashSet<String> pepSet = new HashSet<String>();
		if (diaPp != null && diaPp.length > 0) {
			for (int i = 0; i < diaPp.length; i++) {
				pepSet.add(diaPp[i].getSeqString());
			}
		}
		
		DiannPrMatrixReader prReader = new DiannPrMatrixReader(this.quan_pep_file);
		FlashLfqQuanPeptide[] peps = prReader.getMetaPeptides(this.diaPp);
		this.quanExpNames = prReader.getIntensityTitle();

		HashMap<String, FlashLfqQuanPeptide> modseqPepMap = new HashMap<String, FlashLfqQuanPeptide>();
		for (FlashLfqQuanPeptide pep : peps) {
			modseqPepMap.put(pep.getModSeq(), pep);
		}

		DiaNNResultReader shufReader = new DiaNNResultReader(resultFolderFile, "shuffled", this.diannPar.getCut());
		DiaNNPrecursor[] shufDiaPp = shufReader.getDiaNNPrecursors();
		if (shufDiaPp != null && shufDiaPp.length > 0) {
			for (int i = 0; i < shufDiaPp.length; i++) {
				pepSet.add(diaPp[i].getSeqString());
			}
		}
		
		ConcurrentHashMap<String, HashMap<String, HashSet<String>>> matchedMap = getGenomeFromPep(pepSet, threadCount);
		if (matchedMap == null) {
			return false;
		}
		
		File shufPrFile = new File(resultFolderFile, "shuffled.pr_matrix.tsv");
		DiannPrMatrixReader shufPrReader = new DiannPrMatrixReader(shufPrFile);
		FlashLfqQuanPeptide[] shufPeps = shufPrReader.getMetaPeptides(shufDiaPp);
		for (FlashLfqQuanPeptide shufPep : shufPeps) {
			String seq = shufPep.getSequence();
			if (pepSet.contains(seq)) {
				String modseq = shufPep.getModSeq();
				if (modseqPepMap.containsKey(modseq)) {
					FlashLfqQuanPeptide targetPep = modseqPepMap.get(modseq);
					double[] intensity = targetPep.getIntensity();
					int[] idenType = targetPep.getIdenType();
					int[] ms2Count = targetPep.getMs2Counts();
					double[] intensity2 = shufPep.getIntensity();
					int[] ms2Count2 = shufPep.getMs2Counts();
					for (int i = 0; i < intensity.length; i++) {
						if (intensity2[i] > 0) {
							if (intensity[i] == 0) {
								intensity[i] = intensity2[i];
							} else {
								intensity[i] = (intensity[i] + intensity2[i]) * 0.5;
							}
							if (ms2Count[i] < ms2Count2[i]) {
								ms2Count[i] = ms2Count2[i];
								idenType[i] = 0;
							}
						}
					}
					targetPep.setIntensity(intensity);
					targetPep.setMs2Counts(ms2Count);
					targetPep.setIdenType(idenType);
				} else {
					modseqPepMap.put(modseq, shufPep);
				}
			}
		}

		HashMap<String, String> shufPepMap = new HashMap<String, String>();
		File shufPeptideFile = new File(resultFolderFile, "shuffledPeptide.tsv");
		try (BufferedReader shufPepReader = new BufferedReader(new FileReader(shufPeptideFile))) {
			String line = shufPepReader.readLine();
			while ((line = shufPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				shufPepMap.put(cs[0], cs[1]);
			}
			shufPepReader.close();
		} catch (IOException e) {
			LOGGER.error(getTaskName() + ": error in reading the shuffled peptides from " + shufPeptideFile, e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading the shuffled peptides from " + shufPeptideFile);
		}
		
		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		for (String genome : matchedMap.keySet()) {
			HashMap<String, HashSet<String>> proPepMap = matchedMap.get(genome);
			for (String pro : proPepMap.keySet()) {
				HashSet<String> pset = proPepMap.get(pro);
				for (String pep : pset) {
					if (pepProMap.containsKey(pep)) {
						pepProMap.get(pep).add(pro);
					} else {
						HashSet<String> proSet = new HashSet<String>();
						proSet.add(pro);
						pepProMap.put(pep, proSet);
					}
				}
			}
		}
		
		ArrayList<FlashLfqQuanPeptide> peplist = new ArrayList<FlashLfqQuanPeptide>();
		for (FlashLfqQuanPeptide peptide : modseqPepMap.values()) {
			String sequence = peptide.getSequence();
			if (pepProMap.containsKey(sequence)) {
				String[] pros = pepProMap.get(sequence).toArray(String[]::new);
				peptide.setProteins(pros);
				peplist.add(peptide);
			} else if (hostPepProMap.containsKey(sequence)) {
				String[] pros = hostPepProMap.get(sequence).toArray(String[]::new);
				peptide.setProteins(pros);
				peplist.add(peptide);
			}
		}
		
		L: for (FlashLfqQuanPeptide peptide : shufPeps) {
			String[] shufPros = peptide.getProteins();
			for (String pro : shufPros) {
				if (!pro.startsWith(FastaManager.shuffle)) {
					continue L;
				}
			}

			String shufsequence = peptide.getSequence();
			if (shufPepMap.containsKey(shufsequence)) {
				String sequence = shufPepMap.get(shufsequence);
				if (pepProMap.containsKey(sequence)) {
					String[] pros = pepProMap.get(sequence).toArray(String[]::new);
					for (int i = 0; i < pros.length; i++) {
						pros[i] = FastaManager.shuffle + pros[i];
					}
					peptide.setProteins(pros);
					peplist.add(peptide);
				}
			}
		}
		
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		
		for (int i = 0; i < peplist.size(); i++) {
			FlashLfqQuanPeptide quanPep = peplist.get(i);
			String sequence = quanPep.getSequence();
			String[] pros = quanPep.getProteins();
			for (int j = 0; j < pros.length; j++) {
				if (pros[j].startsWith(magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					if (genomeMap.containsKey(genome)) {
						genomeMap.get(genome).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						genomeMap.put(genome, set);
					}
				} else if (pros[j].startsWith(FastaManager.shuffle)) {
					String genome = FastaManager.shuffle
							+ pros[j].substring(FastaManager.shuffle.length()).split("_")[0];
					if (genomeMap.containsKey(genome)) {
						genomeMap.get(genome).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						genomeMap.put(genome, set);
					}
				}
			}
		}
		
		File hapGenomeFile = new File(resultFolderFile, "hap_genomes.tsv");
		HashSet<String> genomeSet = refineGenomes(genomeMap, pepSet.size(), 1.0, 0.0001, 500, true, hapGenomeFile);
		
		ArrayList<FlashLfqQuanPeptide> filteredList = new ArrayList<FlashLfqQuanPeptide>();
		for (int i = 0; i < peplist.size(); i++) {
			FlashLfqQuanPeptide quanPep = peplist.get(i);
			String[] pros = quanPep.getProteins();
			ArrayList<String> proList = new ArrayList<>();
			for (int j = 0; j < pros.length; j++) {
				if (pros[j].startsWith(magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					if (genomeSet.contains(genome)) {
						proList.add(pros[j]);
					}
				} else if (pros[j].startsWith(FastaManager.shuffle)) {
					String genome = FastaManager.shuffle
							+ pros[j].substring(FastaManager.shuffle.length()).split("_")[0];
					if (genomeSet.contains(genome)) {
						proList.add(pros[j]);
					}
				} else if (hostProPepMap.containsKey(pros[j])) {
					proList.add(pros[j]);
				}
			}

			if (proList.size() > 0) {
				String[] proteins = proList.toArray(String[]::new);
				quanPep.setProteins(proteins);
				filteredList.add(quanPep);
			}
		}
		
		FlashLfqQuanPeptide[] quanPeps = filteredList.toArray(FlashLfqQuanPeptide[]::new);
		
		LOGGER.info(getTaskName() + ": protein inference finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein inference finished");

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		HashMap<String, Double> genomeScoreMap = this.refineGenomes(quanPeps, FastaManager.shuffle);

		String[] razorProteins = this.refineProteins(quanPeps, genomeScoreMap, FastaManager.shuffle);

		setProgress(82);

		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> proteinIntensityMap = new HashMap<String, double[]>();
		HashMap<String, Double> proteinTotalIntenMap = new HashMap<String, Double>();

		HashSet<String> pepSequenceSet = new HashSet<String>();
		HashMap<String, HashSet<String>> totalPepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> razorPepCountMap = new HashMap<String, HashSet<String>>();

		ArrayList<FlashLfqQuanPeptide> peptideList = new ArrayList<FlashLfqQuanPeptide>();

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

		int[] filePsmCount = new int[quanExpNames.length];
		HashSet<String>[] fileSequenceSets = new HashSet[quanExpNames.length];
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

		try (PrintWriter idWriter = new PrintWriter(new File(resultFolderFile, "id.tsv"))) {
			idWriter.println("Raw file\tUnique peptide count\tPSM count");
			for (int i = 0; i < quanExpNames.length; i++) {
				idWriter.println(quanExpNames[i] + "\t" + fileSequenceSets[i].size() + "\t" + filePsmCount[i]);
			}
			idWriter.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing identification information to " + new File(resultFolderFile, "id.tsv"));
			LOGGER.error(getTaskName() + ": error in writing identification information to "
					+ new File(resultFolderFile, "id.tsv"), e);
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

			for (int i = 0; i < this.quanExpNames.length; i++) {
				titlesb.append("Identification type " + this.quanExpNames[i]).append("\t");
			}

			for (int i = 0; i < this.quanExpNames.length; i++) {
				titlesb.append("MS/MS count " + this.quanExpNames[i]).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("Intensity " + this.quanExpNames[i]).append("\t");
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			writer.println(titlesb);

			int id = 0;
			for (FlashLfqQuanPeptide peptide : peptideList) {

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
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide report has been exported to " + final_pep_txt);

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
			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("Intensity " + quanExpNames[i]).append("\t");
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
			LOGGER.error(getTaskName() + ": error in writing final protein result to " + this.final_pro_txt.getName(), e);
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
		MetaProteinAnnoMag[] mpas = funcAnnoSqlite(metapros, quanExpNames);
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
		}

		if (!funTsv.exists() || !funReportTsv.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + funTsv.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " finished");
		}

		File html = new File(funcFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " finished");
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
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in genome analysis, task failed");

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
		double[] cellularIntensity = new double[quanExpNames.length];
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
							taxScoreMaps[j].put(taxon, taxScoreMaps[j].get(taxon) -Math.log10(score));
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

		this.summaryMetaFile = new File(this.resultFolderFile, "metainfo.tsv");
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
	/**
	 * The version is different from the parent class
	 */
	protected boolean exportReportDiaNN() {

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

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());
		
		this.summaryMetaFile = new File(metaPar.getResult(), "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean summayrFinished = this.exportSummary(task);
		
		setProgress(82);
		
		this.final_pep_txt = new File(metaPar.getResult(), "final_peptides.tsv");

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
			} else {
				pepReportOk = true;
			}
		}

		setProgress(84);
		
		this.final_pro_txt = new File(metaPar.getResult(), "final_proteins.tsv");

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
			} else {
				proReportOk = true;
			}
		}
		
		setProgress(86);

		boolean taxFuncReportOk = exportTaxaFunc(task);

		if (!summayrFinished || !pepReportOk || !proReportOk || !taxFuncReportOk) {
			setProgress(99);

			LOGGER.info(getTaskName() + ": error in generating the reports");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in generating the reports");

			return false;
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
			e.printStackTrace();
		}

		return false;
	}
	
	protected boolean exportSummary(MetaReportTask task) {

		this.final_summary_txt = new File(metaPar.getResult(), "final_summary.tsv");
		try {

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

			HashMap<String, Integer> totalms2Map = new HashMap<String, Integer>();
			HashMap<String, HashSet<String>> totalUnipepMap = new HashMap<String, HashSet<String>>();
			HashSet<String> totalPepSet = new HashSet<String>();
			BufferedReader idReader = new BufferedReader(
					new FileReader(new File(this.resultFolderFile, "combined.tsv")));
			String line = idReader.readLine();
			while ((line = idReader.readLine()) != null) {
				String[] cs = line.split("\t");

				if (totalms2Map.containsKey(cs[0])) {
					totalms2Map.put(cs[0], totalms2Map.get(cs[0]) + 1);
					totalUnipepMap.get(cs[0]).add(cs[14]);
				} else {
					totalms2Map.put(cs[0], 1);
					HashSet<String> set = new HashSet<String>();
					set.add(cs[14]);
					totalUnipepMap.put(cs[0], set);
				}
				totalPepSet.add(cs[14]);
			}
			idReader.close();

			for (String file : totalUnipepMap.keySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(file).append("\t");

				if (this.expNameMap.containsKey(file)) {
					sb.append(expNameMap.get(file)).append("\t");
				} else {
					sb.append(file).append("\t");
				}
				int idCounts = totalms2Map.get(file);
				int pepCount = totalUnipepMap.get(file).size();
				sb.append(idCounts).append("\t");
				sb.append(idCounts).append("\t");
				sb.append("1.0").append("\t");
				sb.append(pepCount);

				countWriter.println(sb);

				totalms2count += idCounts;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Total").append("\t");
			sb.append("\t");
			sb.append(totalms2count).append("\t");
			sb.append(totalms2count).append("\t");
			sb.append("1.0").append("\t");
			sb.append(totalPepSet.size());

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
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis and functional annotation started");

		// functional annotation

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();

		LOGGER.info(getTaskName() + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation started");

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.final_pep_txt));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		HashMap<String, ArrayList<Double>> genomeQValueMap = new HashMap<String, ArrayList<Double>>();
		try {

			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int proId = -1;
			int qvalueId = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Proteins")) {
					proId = i;
				} else if (title[i].equals("PEP")) {
					qvalueId = i;
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double qvalue = Double.parseDouble(cs[qvalueId]);
				String[] pros = cs[proId].split(";");
				for (int i = 0; i < pros.length; i++) {
					if (pros[i].startsWith(magDb.getIdentifier())) {
						String genome = pros[i].split("_")[0];
						ArrayList<Double> qList;
						if (genomeQValueMap.containsKey(genome)) {
							qList = genomeQValueMap.get(genome);
						} else {
							qList = new ArrayList<Double>();
							genomeQValueMap.put(genome, qList);
						}
						qList.add(qvalue);
					}
				}
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		File funcFile = new File(metaPar.getResult(), "functional_annotation");
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
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
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

						if (pros[i].startsWith(magDb.getIdentifier())) {
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
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			LOGGER.info(getTaskName() + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {

			BufferedReader quanProReader = null;
			try {
				quanProReader = new BufferedReader(new FileReader(this.final_pro_txt));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
			String[] fileNames = null;
			try {
				String line = quanProReader.readLine();
				String[] title = line.split("\t");
				int id = -1;
				int proId = -1;
				int pepCountId = -1;
				int rzPepCountId = -1;
				int scoreId = -1;

				ArrayList<String> tlist = new ArrayList<String>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].startsWith("Intensity ")) {
						tlist.add(title[i]);
					} else if (title[i].equals("id")) {
						id = i;
					} else if (title[i].equals("Majority protein IDs")) {
						proId = i;
					} else if (title[i].equals("Peptide counts (all)")) {
						pepCountId = i;
					} else if (title[i].equals("Peptide counts (razor)")) {
						rzPepCountId = i;
					} else if (title[i].equals("Score")) {
						scoreId = i;
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

					if (pros[0].startsWith(magDb.getIdentifier())) {

						int groupId = Integer.parseInt(cs[id]);

						double[] intensity = new double[fileIds.length];
						for (int i = 0; i < fileIds.length; i++) {
							intensity[i] = Double.parseDouble(cs[fileIds[i]]);
						}

						String[] pepCountSs = cs[pepCountId].split(";");
						String[] rzPepCountSs = cs[rzPepCountId].split(";");

						for (int i = 0; i < pros.length; i++) {

							int pepCount = i < pepCountSs.length ? Integer.parseInt(pepCountSs[i])
									: Integer.parseInt(pepCountSs[0]);
							int rzPepCount = i < rzPepCountSs.length ? Integer.parseInt(rzPepCountSs[i])
									: Integer.parseInt(rzPepCountSs[0]);

							MetaProtein mp = new MetaProtein(groupId, i + 1, pros[i], pepCount, pepCount,
									Double.parseDouble(cs[scoreId]), intensity);
							list.add(mp);

							if (pros[i].startsWith(magDb.getIdentifier())) {
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
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			setProgress(88);

			MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
			MetaProteinAnnoMag[] mpas = funcAnnoSqlite(metapros, fileNames);
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
			LOGGER.error(getTaskName() + ": error in reading protein function information from "
					+ this.final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
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
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + funTsv.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " finished");
		}

		File html = new File(funcResultFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
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

		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> pepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> uniquePepCountMap = new HashMap<String, HashSet<String>>();

		HashMap<String, HashSet<String>> genomeProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < diaPp.length; i++) {

			String baseSequence = diaPp[i].getSeqString();
			String[] pros = diaPp[i].getProGroups();

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

				if (pros[j].startsWith(magDb.getIdentifier())) {
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
		}

		return exportGenome(genomeIntensityMap, psmCountMap, pepCountMap, uniquePepCountMap, genomeQValueMap, taxFile,
				expNames, task);
	}
	
	protected void cleanTempFiles() {
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File secondFile = new File(separateResultFolders[i], "secondSearch");
			File secondLibFile = new File(secondFile, "secondSearch.speclib");
			if (secondLibFile.exists()) {
				FileUtils.deleteQuietly(secondLibFile);
			}

			File thirdFile = new File(separateResultFolders[i], "thirdSearch");
			File thirdLibFile = new File(thirdFile, "thirdSearch.speclib");
			if (thirdLibFile.exists()) {
				FileUtils.deleteQuietly(thirdLibFile);
			}

			File lashLibFile = new File(separateResultFolders[i], "lastSearch.speclib");
			if (lashLibFile.exists()) {
				FileUtils.deleteQuietly(lashLibFile);
			}
		}

		File combineLibFile = new File(resultFolderFile, "combined.speclib");
		if (combineLibFile.exists()) {
			FileUtils.deleteQuietly(combineLibFile);
		}

		File shuffledLibFile = new File(resultFolderFile, "shuffled.speclib");
		if (shuffledLibFile.exists()) {
			FileUtils.deleteQuietly(shuffledLibFile);
		}
	}
	
	private void dbFromPredictedPeptide(File[] predictedFiles, HashSet<String> pepSet,
			File outputDbFile, int upperLimit) {

		HashMap<String, HashMap<String, Double>> pepGenomeMap = new HashMap<String, HashMap<String, Double>>();

		for (int i = 0; i < predictedFiles.length; i++) {
			try (BufferedReader reader = new BufferedReader(new FileReader(predictedFiles[i]))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					String genome = cs[0].substring(0, cs[0].indexOf("_"));
					double score = Double.parseDouble(cs[2]);
					if (pepGenomeMap.containsKey(cs[1])) {
						pepGenomeMap.get(cs[1]).put(genome, score);
					} else {
						HashMap<String, Double> gMap = new HashMap<String, Double>();
						gMap.put(genome, score);
						pepGenomeMap.put(cs[1], gMap);
					}
				}
				reader.close();
			} catch (IOException e) {

				LOGGER.error(getTaskName() + ": error in reading " + predictedFiles[i], e);
				System.err.println(
						format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + predictedFiles[i]);
				return;
			}
		}

		HashMap<String, Double> genomeCountMap = new HashMap<String, Double>();
		for (String pep : pepGenomeMap.keySet()) {
			HashMap<String, Double> map = pepGenomeMap.get(pep);
			double score = 1.0 / (double) map.size();
			for (String genome : map.keySet()) {
				if (genomeCountMap.containsKey(genome)) {
					genomeCountMap.put(genome, genomeCountMap.get(genome) + score);
				} else {
					genomeCountMap.put(genome, score);
				}
			}
		}

		HashMap<String, Double> pepScoreMap = new HashMap<String, Double>();
		for (String pep : pepGenomeMap.keySet()) {
			if (pepGenomeMap.containsKey(pep)) {
				HashMap<String, Double> map = pepGenomeMap.get(pep);
				double score = 0;

				for (String genome : map.keySet()) {
					score += genomeCountMap.get(genome) * map.get(genome);
				}

				score = score / (double) map.size();

				pepScoreMap.put(pep, score);
			}
		}

		String[] peps = pepScoreMap.keySet().toArray(String[]::new);
		Arrays.sort(peps, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				if (pepScoreMap.get(o1) < pepScoreMap.get(o2)) {
					return 1;
				}
				if (pepScoreMap.get(o1) > pepScoreMap.get(o2)) {
					return -1;
				}

				return 0;
			}
		});

		HashSet<String> totalPepSet = new HashSet<String>(pepSet);
		int end = peps.length > upperLimit ? upperLimit : peps.length;
		for (int j = 0; j < end; j++) {
			totalPepSet.add(peps[j]);
		}

		try (PrintWriter writer = new PrintWriter(outputDbFile)) {
			int id = 0;
			for (String pep : totalPepSet) {
				writer.println(">pro" + id + " pro" + id);
				writer.println(pep);
				id++;
			}
			writer.close();
		} catch (IOException e) {

			LOGGER.error(getTaskName() + ": error in writing to " + outputDbFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in writing to " + outputDbFile);
		}
	}
	
	private void dbFromPredictedPeptide(File[] predictedFiles, HashSet<String> pepSet, HashSet<String> notUsedSet,
			File outputDbFile, int upperLimit) {

		HashMap<String, HashMap<String, Double>> pepGenomeMap = new HashMap<String, HashMap<String, Double>>();

		for (int i = 0; i < predictedFiles.length; i++) {
			try (BufferedReader reader = new BufferedReader(new FileReader(predictedFiles[i]))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					String genome = cs[0].substring(0, cs[0].indexOf("_"));
					double score = Double.parseDouble(cs[2]);
					if (pepGenomeMap.containsKey(cs[1])) {
						pepGenomeMap.get(cs[1]).put(genome, score);
					} else {
						HashMap<String, Double> gMap = new HashMap<String, Double>();
						gMap.put(genome, score);
						pepGenomeMap.put(cs[1], gMap);
					}
				}
				reader.close();
			} catch (IOException e) {

				LOGGER.error(getTaskName() + ": error in reading " + predictedFiles[i], e);
				System.err.println(
						format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + predictedFiles[i]);
				return;
			}
		}

		HashMap<String, Double> genomeCountMap = new HashMap<String, Double>();
		for (String pep : pepGenomeMap.keySet()) {
			HashMap<String, Double> map = pepGenomeMap.get(pep);
			double score = 1.0 / (double) map.size();
			for (String genome : map.keySet()) {
				if (genomeCountMap.containsKey(genome)) {
					genomeCountMap.put(genome, genomeCountMap.get(genome) + score);
				} else {
					genomeCountMap.put(genome, score);
				}
			}
		}

		HashMap<String, Double> pepScoreMap = new HashMap<String, Double>();
		for (String pep : pepGenomeMap.keySet()) {
			if (pepGenomeMap.containsKey(pep)) {
				HashMap<String, Double> map = pepGenomeMap.get(pep);
				double score = 0;

				for (String genome : map.keySet()) {
					score += genomeCountMap.get(genome) * map.get(genome);
				}

				score = score / (double) map.size();

				pepScoreMap.put(pep, score);
			}
		}

		String[] peps = pepScoreMap.keySet().toArray(String[]::new);
		Arrays.sort(peps, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				if (pepScoreMap.get(o1) < pepScoreMap.get(o2)) {
					return 1;
				}
				if (pepScoreMap.get(o1) > pepScoreMap.get(o2)) {
					return -1;
				}

				return 0;
			}
		});

		HashSet<String> totalPepSet = new HashSet<String>(pepSet);
		int end = peps.length > upperLimit ? upperLimit : peps.length;
		for (int j = 0; j < end; j++) {
			totalPepSet.add(peps[j]);
		}

		try (PrintWriter writer = new PrintWriter(outputDbFile)) {
			int id = 0;
			for (String pep : totalPepSet) {
				if (!notUsedSet.contains(pep)) {
					writer.println(">pro" + id + " pro" + id);
					writer.println(pep);
					id++;
				}
			}
			writer.close();
		} catch (IOException e) {

			LOGGER.error(getTaskName() + ": error in writing to " + outputDbFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in writing to " + outputDbFile);
		}
	}
	
	protected void dbFromModelPeptide(String[] modelRankFiles, HashSet<String> genomeSet, HashSet<String> pepSet,
			File outputDbFile, int upperLimit) {

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile
				+ " started");

		HashSet<String> totalPepSet = new HashSet<String>();
		totalPepSet.addAll(pepSet);

		for (int i = 0; i < modelRankFiles.length; i++) {

			File modelRankFile = new File(modelRankFiles[i]);
			File modelIntensityFile = new File(modelRankFiles[i].replace("_pep_rank.tsv", "_predicted_allData.tsv"));

			HashMap<String, Double> pepIntensityMap = new HashMap<String, Double>();
			HashMap<String, HashSet<String>> pepGenomeMap = new HashMap<String, HashSet<String>>();

			try (BufferedReader reader = new BufferedReader(new FileReader(modelRankFile))) {
				String pline = reader.readLine();
				String genome = "";
				boolean have = false;
				while ((pline = reader.readLine()) != null) {
					if (pline.startsWith("pro")) {
						continue;
					} else if (genome.length() > 0 && pline.startsWith(genome)) {
						if (have) {
							String[] cs = pline.split("\t");

							pepIntensityMap.put(cs[1], 0.0);

							if (pepGenomeMap.containsKey(cs[1])) {
								pepGenomeMap.get(cs[1]).add(genome);
							} else {
								HashSet<String> gset = new HashSet<String>();
								gset.add(genome);
								pepGenomeMap.put(cs[1], gset);
							}
						}
					} else {
						String[] cs = pline.split("\t");
						genome = cs[0].substring(0, cs[0].indexOf("_"));
						if (genomeSet.contains(genome)) {
							have = true;
						} else {
							have = false;
						}
						if (have) {
							pepIntensityMap.put(cs[1], 0.0);

							if (pepGenomeMap.containsKey(cs[1])) {
								pepGenomeMap.get(cs[1]).add(genome);
							} else {
								HashSet<String> gset = new HashSet<String>();
								gset.add(genome);
								pepGenomeMap.put(cs[1], gset);
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {

				LOGGER.error(getTaskName() + ": error in reading model information from " + modelRankFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading model information from  " + modelRankFile);
				return;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(modelIntensityFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split(",");
					if (pepIntensityMap.containsKey(cs[1])) {
						double intensity = Double.parseDouble(cs[2]);
						if (intensity > pepIntensityMap.get(cs[1])) {
							pepIntensityMap.put(cs[1], intensity);
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in reading model information from  " + modelIntensityFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading model information from  " + modelIntensityFile);
				return;
			}

			HashMap<String, Double> genomeCountMap = new HashMap<String, Double>();
			for (String pep : pepGenomeMap.keySet()) {
				if (pepGenomeMap.containsKey(pep)) {
					HashSet<String> set = pepGenomeMap.get(pep);
					double score = 1.0 / (double) set.size();
					for (String genome : set) {
						if (genomeCountMap.containsKey(genome)) {
							genomeCountMap.put(genome, genomeCountMap.get(genome) + score);
						} else {
							genomeCountMap.put(genome, score);
						}
					}
				}
			}

			HashMap<String, Double> pepScoreMap = new HashMap<String, Double>();
			for (String pep : pepIntensityMap.keySet()) {
				if (pepGenomeMap.containsKey(pep)) {
					HashSet<String> set = pepGenomeMap.get(pep);
					double score = 0;

					for (String genome : set) {
						score += genomeCountMap.get(genome);
					}

					score = score / (double) set.size() * pepIntensityMap.get(pep);

					pepScoreMap.put(pep, score);
				}
			}

			String[] peps = pepScoreMap.keySet().toArray(String[]::new);
			Arrays.sort(peps, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					if (pepScoreMap.get(o1) < pepScoreMap.get(o2)) {
						return 1;
					}
					if (pepScoreMap.get(o1) > pepScoreMap.get(o2)) {
						return -1;
					}

					return 0;
				}
			});

			int end = peps.length > upperLimit ? upperLimit : peps.length;
			for (int j = 0; j < end; j++) {
				totalPepSet.add(peps[j]);
			}
		}

		try (PrintWriter writer = new PrintWriter(outputDbFile)) {
			int id = 0;
			for (String pep : totalPepSet) {
				writer.println(">pro" + id + " pro" + id);
				writer.println(pep);
				id++;
			}
			writer.close();
		} catch (IOException e) {

			LOGGER.error(getTaskName() + ": error in writing to " + outputDbFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in writing to " + outputDbFile);
		}

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile
				+ " finished");
	}
	
	protected void dbFromModelPeptide(String[] modelRankFiles, HashSet<String> genomeSet, HashSet<String> pepSet,
			HashSet<String> notUsedSet, File outputDbFile, int upperLimit) {

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile
				+ " started");

		HashSet<String> totalPepSet = new HashSet<String>();
		totalPepSet.addAll(pepSet);

		for (int i = 0; i < modelRankFiles.length; i++) {
			File modelRankFile = new File(modelRankFiles[i]);
			File modelIntensityFile = new File(modelRankFiles[i].replace("_pep_rank.tsv", "_predicted_allData.tsv"));

			HashMap<String, Double> pepIntensityMap = new HashMap<String, Double>();
			HashMap<String, HashSet<String>> pepGenomeMap = new HashMap<String, HashSet<String>>();

			try (BufferedReader reader = new BufferedReader(new FileReader(modelRankFile))) {
				String pline = reader.readLine();
				String genome = "";
				boolean have = false;
				while ((pline = reader.readLine()) != null) {
					if (pline.startsWith("pro")) {
						continue;
					} else if (genome.length() > 0 && pline.startsWith(genome)) {
						if (have) {
							String[] cs = pline.split("\t");

							pepIntensityMap.put(cs[1], 0.0);

							if (pepGenomeMap.containsKey(cs[1])) {
								pepGenomeMap.get(cs[1]).add(genome);
							} else {
								HashSet<String> gset = new HashSet<String>();
								gset.add(genome);
								pepGenomeMap.put(cs[1], gset);
							}
						}
					} else {
						String[] cs = pline.split("\t");
						genome = cs[0].substring(0, cs[0].indexOf("_"));
						if (genomeSet.contains(genome)) {
							have = true;
						} else {
							have = false;
						}
						if (have) {
							pepIntensityMap.put(cs[1], 0.0);

							if (pepGenomeMap.containsKey(cs[1])) {
								pepGenomeMap.get(cs[1]).add(genome);
							} else {
								HashSet<String> gset = new HashSet<String>();
								gset.add(genome);
								pepGenomeMap.put(cs[1], gset);
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {

				LOGGER.error(getTaskName() + ": error in reading model information from  " + modelRankFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading model information from  " + modelRankFile);
				return;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(modelIntensityFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split(",");
					if (pepIntensityMap.containsKey(cs[1])) {
						double intensity = Double.parseDouble(cs[2]);
						if (intensity > pepIntensityMap.get(cs[1])) {
							pepIntensityMap.put(cs[1], intensity);
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in reading model information from  " + modelIntensityFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading model information from  " + modelIntensityFile);
				return;
			}

			HashMap<String, Double> genomeCountMap = new HashMap<String, Double>();
			for (String pep : pepGenomeMap.keySet()) {
				if (pepGenomeMap.containsKey(pep)) {
					HashSet<String> set = pepGenomeMap.get(pep);
					double score = 1.0 / (double) set.size();
					for (String genome : set) {
						if (genomeCountMap.containsKey(genome)) {
							genomeCountMap.put(genome, genomeCountMap.get(genome) + score);
						} else {
							genomeCountMap.put(genome, score);
						}
					}
				}
			}

			HashMap<String, Double> pepScoreMap = new HashMap<String, Double>();
			for (String pep : pepIntensityMap.keySet()) {
				if (pepGenomeMap.containsKey(pep)) {
					HashSet<String> set = pepGenomeMap.get(pep);
					double score = 0;

					for (String genome : set) {
						score += genomeCountMap.get(genome);
					}

					score = score / (double) set.size() * pepIntensityMap.get(pep);

					pepScoreMap.put(pep, score);
				}
			}

			String[] peps = pepScoreMap.keySet().toArray(String[]::new);
			Arrays.sort(peps, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					if (pepScoreMap.get(o1) < pepScoreMap.get(o2)) {
						return 1;
					}
					if (pepScoreMap.get(o1) > pepScoreMap.get(o2)) {
						return -1;
					}

					return 0;
				}
			});

			int end = peps.length > upperLimit ? upperLimit : peps.length;
			for (int j = 0; j < end; j++) {
				totalPepSet.add(peps[j]);
			}
		}

		try (PrintWriter writer = new PrintWriter(outputDbFile)) {
			int id = 0;
			for (String pep : totalPepSet) {
				if (!notUsedSet.contains(pep)) {
					writer.println(">pro" + id + " pro" + id);
					writer.println(pep);
					id++;
				}
			}
			writer.close();
		} catch (IOException e) {

			LOGGER.error(getTaskName() + ": error in writing to " + outputDbFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in writing to " + outputDbFile);
		}

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile
				+ " finished");
	}

	private void dbFromModelPeptideNotUsed(String[] modelPath, HashSet<String> genomeSet, HashSet<String> pepSet,
			HashSet<String> notUsedSet, File outputDbFile) {

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile + " started");

		HashSet<String>[] addSets = new HashSet[10];
		for (int i = 0; i < addSets.length; i++) {
			addSets[i] = new HashSet<String>();
		}

		for (int i = 0; i < modelPath.length; i++) {
			try (BufferedReader reader = new BufferedReader(new FileReader(modelPath[i]))) {
				String pline = reader.readLine();
				String genome = "";
				boolean have = false;
				while ((pline = reader.readLine()) != null) {
					if (genome.length() > 0 && pline.startsWith(genome)) {
						if (have) {
							String[] cs = pline.split("\t");
							if (!notUsedSet.contains(cs[1])) {
								double score = Double.parseDouble(cs[2]);

								int id = (int) (score * 10.0);

								if (id < addSets.length) {
									addSets[id].add(cs[1]);
								} else {
									addSets[addSets.length - 1].add(cs[1]);
								}
							}
						}
					} else {
						String[] cs = pline.split("\t");
						genome = cs[0].substring(0, cs[0].indexOf("_"));
						if (genomeSet.contains(genome)) {
							have = true;
						} else {
							have = false;
						}
						if (have) {
							if (!notUsedSet.contains(cs[1])) {
								double score = Double.parseDouble(cs[2]);
								int id = (int) (score * 10.0);

								if (id < addSets.length) {
									addSets[id].add(cs[1]);
								} else {
									addSets[addSets.length - 1].add(cs[1]);
								}
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {

				LOGGER.error(getTaskName() + ": error in reading " + modelPath[i], e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + modelPath[i]);
			}
		}

		HashSet<String> addSet = new HashSet<String>();
		for (int i = 1; i <= addSets.length; i++) {
			addSet.addAll(addSets[addSets.length - i]);
			if (addSet.size() > 1000000) {
				break;
			}
		}

		notUsedSet.addAll(addSet);
		try (PrintWriter writer = new PrintWriter(outputDbFile)) {
			int id = 0;
			for (String pep : pepSet) {
				writer.println(">pro" + id + " pro" + id);
				writer.println(pep);
				id++;
			}
			for (String pep : addSet) {
				writer.println(">pro" + id + " pro" + id);
				writer.println(pep);
				id++;
			}
			writer.close();
		} catch (IOException e) {

			LOGGER.error(getTaskName() + ": error in writing to " + outputDbFile, e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing to " + outputDbFile);
		}

		LOGGER.info(getTaskName() + ": generating database to " + outputDbFile + " finished");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": generating database to " + outputDbFile + " finished");
	}

	protected HashSet<String> getGenomeSet(File resultFile, double filterThreshold, HashSet<String> pepSet, int threadCount) {
		ConcurrentHashMap<String, HashMap<String, HashSet<String>>> matchedMap = getGenomeFromPep(pepSet, threadCount);
		if (matchedMap == null) {
			return null;
		} else {
			HashMap<String, HashSet<String>> genomePepMap = new HashMap<String, HashSet<String>>();
			for (String genome : matchedMap.keySet()) {
				HashSet<String> genomePepSet = new HashSet<String>();
				HashMap<String, HashSet<String>> proPepMap = matchedMap.get(genome);
				for (String pro : proPepMap.keySet()) {
					genomePepSet.addAll(proPepMap.get(pro));
				}
				genomePepMap.put(genome, genomePepSet);
			}

			File magsFile = new File(resultFile, "mags.tsv");
			HashSet<String> genomeSet = refineGenomes(genomePepMap, pepSet.size(), filterThreshold, magsFile);

			File usedMagsFile = new File(resultFile, "usedMags.tsv");
			try (PrintWriter writer = new PrintWriter(usedMagsFile)) {
				writer.println("Genome");
				for (String genome : genomeSet) {
					writer.println(genome);
				}
				writer.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in writing genomes to " + usedMagsFile, e);
				System.out.println(
						format.format(new Date()) + "\t" + getTaskName() + ": error in writing genomes to " + usedMagsFile);
			}

			return genomeSet;
		}
	}
	
	protected HashSet<String> refineGenomes(HashMap<String, HashSet<String>> genomePepMap, int totalPepCount, double threshold,
			File out) {

		String[] genomes = genomePepMap.keySet().toArray(new String[genomePepMap.size()]);
		Arrays.sort(genomes, (g1, g2) -> {
			int s1 = genomePepMap.get(g1).size();
			int s2 = genomePepMap.get(g2).size();
			return s2 - s1;
		});

		for (int iteratorCount = 0; iteratorCount < 5; iteratorCount++) {
			HashSet<String> totalSet = new HashSet<String>();
			HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
			for (int i = 0; i < genomes.length; i++) {
				int currentSize = totalSet.size();
				totalSet.addAll(genomePepMap.get(genomes[i]));
				int increaseSize = totalSet.size() - currentSize;
				orderMap.put(genomes[i], increaseSize);
			}

			Arrays.sort(genomes, (g1, g2) -> {
				return orderMap.get(g2) - orderMap.get(g1);
			});
		}

		double[] percentage = new double[genomes.length];
		HashSet<String> genomeSet = new HashSet<String>();
		HashSet<String> totalSet = new HashSet<String>();
		int breakPoint1 = -1;
		int breakPoint2 = -1;

		try (PrintWriter writer = new PrintWriter(out)) {
			writer.println("Rank\tGenome\tTotal_covered_PSM_count\tPercentage\tIncrement\tIncrement ratio");
			for (int i = 0; i < genomes.length; i++) {

				int currentSize = totalSet.size();
				totalSet.addAll(genomePepMap.get(genomes[i]));
				int add = totalSet.size() - currentSize;
				double additional = ((double) add / (double) totalPepCount);

				percentage[i] = ((double) totalSet.size() / (double) totalPepCount);

				writer.println(i + "\t" + genomes[i] + "\t" + totalSet.size() + "\t" + percentage[i] + "\t" + add + "\t"
						+ additional);

				if (add > 1) {
					breakPoint1 = i;
				}
				if (additional > 0.0002) {
					breakPoint2 = i;
				}
			}
			writer.close();
		} catch (IOException e) {
			LOGGER.error(getTaskName() + ": error in writing genomes to " + out, e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing genomes to " + out);
		}

		for (int i = 0; i < genomes.length; i++) {
			genomeSet.add(genomes[i]);
			if (percentage[i] >= threshold) {
				break;
			}
			if (i == breakPoint1 || i == breakPoint2) {
				break;
			}
			if (genomeSet.size() > 240) {
				break;
			}
		}

		return genomeSet;
	}

	protected HashSet<String> refineProtein(HashMap<String, HashSet<String>> proPepMap, int totalPepCount,
			File out) {

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
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing genomes to " + out);
		}

		for (int i = 0; i < proteins.length; i++) {
			proSet.add(proteins[i]);
			if (i == breakPoint1) {
				break;
			}
		}

		return proSet;
	}
	
	private ConcurrentHashMap<String, HashMap<String, HashSet<String>>> getGenomeFromPep(HashSet<String> allPepSet,
			int threadCount) {
		ConcurrentHashMap<String, HashMap<String, HashSet<String>>> genomePepMap = new ConcurrentHashMap<String, HashMap<String, HashSet<String>>>();
		HashSet<String> matchedPepSet = new HashSet<String>();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File currentFile = super.magDb.getCurrentFile();
		File predictPepFile = new File(currentFile, "predicted");
		File[] files = predictPepFile.listFiles();
		for (File file : files) {
			executor.execute(() -> {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					HashMap<String, HashSet<String>> proPepMap = new HashMap<String, HashSet<String>>();
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (allPepSet.contains(cs[1])) {
							matchedPepSet.add(cs[1]);
							if (proPepMap.containsKey(cs[0])) {
								proPepMap.get(cs[0]).add(cs[1]);
							} else {
								HashSet<String> pepSet = new HashSet<String>();
								pepSet.add(cs[1]);
								proPepMap.put(cs[0], pepSet);
							}
						}
					}
					if (proPepMap.size() > 0) {
						String preFileName = file.getName();
						String genome = preFileName.substring(0, preFileName.length() - ".predicted.tsv".length());
						genomePepMap.put(genome, proPepMap);
					}
				} catch (IOException e) {

					LOGGER.error(getTaskName() + ": error in reading " + file, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + file);
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {

				allPepSet.clear();
				allPepSet.addAll(matchedPepSet);

				LOGGER.info(getTaskName() + ": matching peptides finished, " + allPepSet.size() + " peptides from "
						+ genomePepMap.size() + " genomes were matched");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": matching peptides finished, "
						+ allPepSet.size() + " peptides from " + genomePepMap.size() + " genomes were matched");

			} else {
				LOGGER.error(getTaskName()
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");

				return null;
			}
		} catch (InterruptedException e) {

			LOGGER.error(getTaskName() + ": error in reading predicted peptides", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in reading predicted peptides");

			return null;
		}

		return genomePepMap;
	}
	
	private ConcurrentHashMap<String, HashSet<String>> getGenomeFromPep(HashSet<String> allPepSet,
			HashSet<String> genomeSet, int threadCount) {
		ConcurrentHashMap<String, HashSet<String>> genomePepMap = new ConcurrentHashMap<String, HashSet<String>>();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File currentFile = super.magDb.getCurrentFile();
		File predictPepFile = new File(currentFile, "predicted");
		File[] files = predictPepFile.listFiles(f -> {
			String name = f.getName();
			name = name.substring(0, name.indexOf("."));
			return genomeSet.contains(name);
		});
		for (File file : files) {
			executor.execute(() -> {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					HashSet<String> pepSet = new HashSet<String>();
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (allPepSet.contains(cs[1])) {
							pepSet.add(cs[1]);
						}
					}
					if (pepSet.size() > 0) {
						String preFileName = file.getName();
						String genome = preFileName.substring(0, preFileName.length() - ".predicted.tsv".length());
						genomePepMap.put(genome, pepSet);
					}
				} catch (IOException e) {

					LOGGER.error(getTaskName() + ": error in reading " + file, e);
					System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + file);
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {
				LOGGER.info(getTaskName() + ": matching peptides finished, " + allPepSet.size() + " peptides from "
						+ genomePepMap.size() + " genomes were matched");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": matching peptides finished, "
						+ allPepSet.size() + " peptides from " + genomePepMap.size() + " genomes were matched");

			} else {
				LOGGER.error(getTaskName()
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");

				return null;
			}
		} catch (InterruptedException e) {

			LOGGER.error(getTaskName() + ": error in reading predicted peptides", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in reading predicted peptides");

			return null;
		}

		return genomePepMap;
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
	
	private static void compareTest(String in, String combined) {
		File firstFolder = new File(in, "firstSearch");
		File secondFolder = new File(in, "secondSearch");
		File thirdFolder = new File(in, "thirdSearch");

		HashSet<String> firstPepSet = new HashSet<String>();
		HashSet<String> firstFindSet = new HashSet<String>();

		compare(firstFolder, firstPepSet, firstFindSet);

		HashSet<String> secondPepSet = new HashSet<String>();
		HashSet<String> secondFindSet = new HashSet<String>();
		compare(secondFolder, secondPepSet, secondFindSet);

		HashSet<String> all12Set = new HashSet<String>();
		all12Set.addAll(firstPepSet);
		all12Set.addAll(secondPepSet);
		int common12 = firstPepSet.size() + secondPepSet.size() - all12Set.size();
		System.out.println("first second\t" + firstPepSet.size() + "\t" + common12 + "\t" + secondPepSet.size());

		HashSet<String> thirdPepSet = new HashSet<String>();
		HashSet<String> thirdFindSet = new HashSet<String>();
		compare(thirdFolder, thirdPepSet, thirdFindSet);

		HashSet<String> all23Set = new HashSet<String>();
		all23Set.addAll(secondPepSet);
		all23Set.addAll(thirdFindSet);
		int common23 = secondPepSet.size() + thirdFindSet.size() - all23Set.size();
		System.out.println("first second\t" + secondPepSet.size() + "\t" + common23 + "\t" + thirdFindSet.size());
		
		HashSet<String> combinedSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(combined))) {
			String pline;
			while ((pline = reader.readLine()) != null) {
				if (!pline.startsWith(">")) {
					combinedSet.add(pline);
				}
			}
			reader.close();
		} catch (IOException e) {

		}
		
		HashSet<String> combined1Set = new HashSet<String>();
		combined1Set.addAll(firstPepSet);
		combined1Set.addAll(combinedSet);
		int common1 = firstPepSet.size() + combinedSet.size() - combined1Set.size();
		System.out.println("first\t" + firstPepSet.size() + "\t" + common1 + "\t" + combined1Set.size());
		
		HashSet<String> combined2Set = new HashSet<String>();
		combined2Set.addAll(secondPepSet);
		combined2Set.addAll(combinedSet);
		int common2 = secondPepSet.size() + combined1Set.size() - combined2Set.size();
		System.out.println("second\t" + secondPepSet.size() + "\t" + common2 + "\t" + combined2Set.size());
		
		HashSet<String> combined3Set = new HashSet<String>();
		combined3Set.addAll(thirdPepSet);
		combined3Set.addAll(combinedSet);
		int common3 = thirdPepSet.size() + combined1Set.size() - combined1Set.size();
		System.out.println("third\t" + thirdPepSet.size() + "\t" + common3 + "\t" + combined3Set.size());
		
	}
	
	private static void compare(File firstFolder, HashSet<String> firstPepSet, HashSet<String> firstFindSet) {
		DiaNNResultReader firstReader = new DiaNNResultReader(firstFolder.getAbsolutePath(), firstFolder.getName(),
				"K*,R*");
		DiaNNPrecursor[] firstPPs = firstReader.getDiaNNPrecursors();

		for (DiaNNPrecursor pp : firstPPs) {
			firstPepSet.add(pp.getSeqString());
		}

		File firstFastaFile = new File(firstFolder, firstFolder.getName() + ".fasta");

		try (BufferedReader reader = new BufferedReader(new FileReader(firstFastaFile))) {
			String pline;
			while ((pline = reader.readLine()) != null) {
				if (!pline.startsWith(">")) {
					firstFindSet.add(pline);
				}
			}
			reader.close();
		} catch (IOException e) {

		}

		HashSet<String> allSet = new HashSet<String>();
		allSet.addAll(firstPepSet);
		allSet.addAll(firstFindSet);
		int common = firstPepSet.size() + firstFindSet.size() - allSet.size();
		System.out.println(
				firstFolder.getName() + "\t" + firstPepSet.size() + "\t" + common + "\t" + firstFindSet.size());
	}
	
	private static void compare2(String pep1, String pep2) {
		DiaNNResultReader diannReader1 = new DiaNNResultReader(pep1, (new File(pep1)).getName(), "K*,R*");
		DiaNNPrecursor[] pps1 = diannReader1.getDiaNNPrecursors();
		HashSet<String> pepSet1 = new HashSet<String>();
		for (DiaNNPrecursor pp : pps1) {
			pepSet1.add(pp.getSeqString());
		}

		DiaNNResultReader diannReader2 = new DiaNNResultReader(pep2, (new File(pep2)).getName(), "K*,R*");
		DiaNNPrecursor[] pps2 = diannReader2.getDiaNNPrecursors();
		HashSet<String> pepSet2 = new HashSet<String>();
		for (DiaNNPrecursor pp : pps2) {
			pepSet2.add(pp.getSeqString());
		}

		HashSet<String> allSet = new HashSet<String>();
		allSet.addAll(pepSet1);
		allSet.addAll(pepSet2);
		int common = pepSet1.size() + pepSet2.size() - allSet.size();
		System.out.println(pepSet1.size() + "\t" + common + "\t" + pepSet2.size());

	}
	
	private static void compare3(String fasta1, String fasta2) {
		HashSet<String> proSet1 = new HashSet<String>();
		try (BufferedReader frReader = new BufferedReader(new FileReader(fasta1))) {
			String pline;
			while ((pline = frReader.readLine()) != null) {
				if (!pline.startsWith(">")) {
					proSet1.add(pline);
				}
			}
			frReader.close();
		} catch (IOException e) {

		}
		
		HashSet<String> proSet2 = new HashSet<String>();
		try (BufferedReader frReader = new BufferedReader(new FileReader(fasta2))) {
			String pline;
			while ((pline = frReader.readLine()) != null) {
				if (!pline.startsWith(">")) {
					proSet2.add(pline);
				}
			}
			frReader.close();
		} catch (IOException e) {

		}

		HashSet<String> allSet = new HashSet<String>();
		allSet.addAll(proSet1);
		allSet.addAll(proSet2);
		int common = proSet1.size() + proSet2.size() - allSet.size();
		System.out.println(proSet1.size() + "\t" + common + "\t" + proSet2.size());
		
		
		try (PrintWriter writer = new PrintWriter(new File(fasta2+".test.fasta"))) {
			int count = 0;
			for(String seq: proSet2) {
				if(!proSet1.contains(seq)) {
					writer.println(">pro"+count+" pro"+count);
					writer.println(seq);
					count++;
				}
			}
			
			System.out.println(count);
			writer.close();
		} catch (IOException e) {

		}
	}
	
	private static void test(File firstFile) {
		MetaParameterDia metaPar = MetaParaIoDia
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\parameters_DIA_1_0.json");
		MetaSourcesDia msv = MetaSourcesIoDia
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\resources_DIA_1_0.json");

		MagDbItem[][] magDbItems = MagDbConfigIO
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\MAGDB_1_1.json");
		metaPar.setUsedMagDbItem(magDbItems[0][3]);
		magDbItems[0][3].setCurrentFile(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2"));

		MetaDiaTask task = new MetaDiaTask(metaPar, msv, new JProgressBar(), new JProgressBar(), null);
		HashSet<String> pepSet1 = new HashSet<String>();
		HashSet<String> genomeSet1 = task.getGenomeSet(firstFile, 0.9, pepSet1, 12);
		System.out.println("pepSet1\t" + pepSet1.size());
		System.out.println("genomeSet1\t" + genomeSet1.size());

		File round1DbFile = new File(firstFile, "test.fasta");
		String[] modelPath = new String[] {
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD012725\\PXD012725_pep_rank.tsv",
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD005780\\PXD005780_pep_rank.tsv",
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD027172\\PXD027172_pep_rank.tsv",
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD008870\\PXD008870_pep_rank.tsv",
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD017390\\PXD017390_pep_rank.tsv",
				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD007819\\PXD007819_pep_rank.tsv" };
		HashSet<String> initialSet = new HashSet<String>();

		try (BufferedReader reader = new BufferedReader(
				new FileReader("Z:\\Kai\\Database\\human-gut\\v2.0.2\\libraries\\core15\\core15.tsv"))) {
			String pline = reader.readLine();
			String[] title = pline.split("\t");
			int seqid = -1;
			for (int j = 0; j < title.length; j++) {
				if (title[j].equals("PeptideSequence")) {
					seqid = j;
				}
			}
			while ((pline = reader.readLine()) != null) {
				String[] cs = pline.split("\t");
				initialSet.add(cs[seqid]);
			}
			reader.close();
		} catch (Exception e) {

		}
		System.out.println("initialSet\t"+initialSet.size());
		task.dbFromModelPeptide(modelPath, genomeSet1, pepSet1, initialSet, round1DbFile, 1000000);
	}

	private static void testFDR(String in) {
		MetaParameterDia metaPar = MetaParaIoDia.parse("E:\\Exported\\exe\\parameters_DIA_1_0.json");
		MetaSourcesDia msv = MetaSourcesIoDia.parse("E:\\Exported\\exe\\resources_DIA_1_0.json");

		MagDbItem[][] magDbItems = MagDbConfigIO.parse("E:\\Exported\\exe\\MAGDB_1_1.json");
//		metaPar.setUsedMagDbItem(magDbItems[0][3]);
		metaPar.setUsedMagDbItem(magDbItems[0][7]);
//		magDbItems[0][3].setCurrentFile(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2"));
		magDbItems[0][7].setCurrentFile(new File("Z:\\Kai\\Database\\mouse-gut\\v1.0"));

		MetaDiaTask task = new MetaDiaTask(metaPar, msv, new JProgressBar(), new JProgressBar(), null);

		DiaNNResultReader reader = new DiaNNResultReader(new File(in), "combined", task.diannPar.getCut());
		DiaNNPrecursor[] diaPp = reader.getDiaNNPrecursors();
		HashSet<String> pepSet = new HashSet<String>();
		HashSet<String> highPepSet = new HashSet<String>();
		if (diaPp != null && diaPp.length > 0) {

			Arrays.sort(diaPp, new Comparator<DiaNNPrecursor>() {

				@Override
				public int compare(DiaNNPrecursor o1, DiaNNPrecursor o2) {
					// TODO Auto-generated method stub
					if (o1.getPEP() > o2.getPEP()) {
						return -1;
					}
					if (o1.getPEP() < o2.getPEP()) {
						return 1;
					}
					return 0;
				}
			});
			for (int i = 0; i < diaPp.length; i++) {
				if ((double) i / (double) diaPp.length > 0.05) {
					highPepSet.add(diaPp[i].getSeqString());
				} else {
					pepSet.add(diaPp[i].getSeqString());
				}
			}
		}
		System.out.println(pepSet.size()+"\t"+highPepSet.size()+"\t"+diaPp.length);

		try(PrintWriter writer = new PrintWriter(new File(in, "fdrtest_low.tsv"))){
			ConcurrentHashMap<String, HashMap<String, HashSet<String>>> matchedMap = task.getGenomeFromPep(pepSet, 12);
			Iterator<Entry<String, HashMap<String, HashSet<String>>>> iterator = matchedMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, HashMap<String, HashSet<String>>> entry = iterator.next();
				String genome =  entry.getKey();
				HashMap<String, HashSet<String>> proPepMap = entry.getValue();
				int[] count = new int[2];
				for(String pro: proPepMap.keySet()) {
					HashSet<String> ppepSet = proPepMap.get(pro);
					for(String pep: ppepSet) {
						count[0]++;
						if(highPepSet.contains(pep)) {
							count[1]++;
						}
					}
				}
				
				writer.println(genome+"\t"+count[0]+"\t"+count[1]+"\t"+(double)count[1]/(double)count[0]);
			}
			writer.close();
			
//			HashSet<String> genomeSet =task. refineGenomes(genomeMap, pepSet.size(), 1.0, 0.0001, 500, true, hapGenomeFile);
			
			
		}catch(IOException e) {
			
		}
	}
	
	private static void combine(String in, String[] diaFiles) {
		HashSet<String> pepSet = new HashSet<String>();
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			File thirdFile = new File(files[i], "thirdSearch");
			if (thirdFile.exists()) {
				File resultFile = new File(thirdFile, "thirdSearch.tsv");
				if (resultFile.exists()) {
					try (BufferedReader reader = new BufferedReader(new FileReader(resultFile))) {
						String pline = reader.readLine();
						String[] title = pline.split("\t");
						int seqid = -1;
						for (int j = 0; j < title.length; j++) {
							if (title[j].equals("Stripped.Sequence")) {
								seqid = j;
							}
						}
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							pepSet.add(cs[seqid]);
						}
						reader.close();
					} catch (IOException e) {

					}
				}
			}
		}
		try (PrintWriter writer = new PrintWriter(new File(in, "test.fasta"))) {

			int id = 0;
			for (String pep : pepSet) {
				writer.println(">pro" + id + " pro" + id);
				writer.println(pep);
				id++;
			}
			writer.close();

		} catch (IOException e) {

		}

		MetaParameterDia metaPar = MetaParaIoDia
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\parameters_DIA_1_0.json");
		MetaSourcesDia msv = MetaSourcesIoDia
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\resources_DIA_1_0.json");

		MagDbItem[][] magDbItems = MagDbConfigIO
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\MAGDB_1_1.json");
		metaPar.setUsedMagDbItem(magDbItems[0][3]);
		magDbItems[0][3].setCurrentFile(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2"));

		MetaDiaTask task = new MetaDiaTask(metaPar, msv, new JProgressBar(), new JProgressBar(), null);

		File testTsvFile = new File(in, "test.tsv");
		File testLibFile = new File(in, "test.lib");
		if (!testTsvFile.exists()) {

			DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
			diaLibSearchPar.setDiaFiles(diaFiles);
			diaLibSearchPar.setThreadCount(3);
			diaLibSearchPar.setMbr(true);

			DiannParameter diannPar = new DiannParameter();
			diannPar.setThreads(3);
			diannPar.setMatrices(true);
			diannPar.setRelaxed_prot_inf(true);
			diannPar.setReanalyse(true);

			task.diaNNTask.addTask(diannPar, diaFiles, testTsvFile.getAbsolutePath(),
					(new File(in, "test.fasta")).getAbsolutePath(), testLibFile.getAbsolutePath(), false);
		}
		task.diaNNTask.run(1);
	}
	
	private static void exportTest(String resultFolderFile, String resultOutputFolder) {

		MetaParameterDia metaPar = MetaParaIoDia.parse("E:\\Exported\\exe\\parameters_DIA_1_0.json");
		MetaSourcesDia msv = MetaSourcesIoDia.parse("E:\\Exported\\exe\\resources_DIA_1_0.json");
		MagDbItem[][] magDbItems = MagDbConfigIO.parse("E:\\Exported\\exe\\MAGDB_1_1.json");
		metaPar.setUsedMagDbItem(magDbItems[0][3]);
		magDbItems[0][3].setCurrentFile(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2"));
		metaPar.setMicroDb("Z:\\Kai\\Database\\human-gut\\v2.0.2");
		
//		metaPar.setUsedMagDbItem(magDbItems[0][6]);
//		magDbItems[0][6].setCurrentFile(new File("Z:\\Kai\\Database\\mouse-gut\\v1.0"));
//		metaPar.setMicroDb("Z:\\Kai\\Database\\mouse-gut\\v1.0");
		
		MetaDiaTask task = new MetaDiaTask(metaPar, msv, new JProgressBar(), new JProgressBar(), null);
		DiaNNResultReader reader = new DiaNNResultReader(resultFolderFile, "combined", task.diannPar.getCut());
		DiaNNPrecursor[] diaPp = reader.getDiaNNPrecursors();
		HashSet<String> pepSet = new HashSet<String>();
		if (diaPp != null && diaPp.length > 0) {
			for (int i = 0; i < diaPp.length; i++) {
				pepSet.add(diaPp[i].getSeqString());
			}
		}
		
		System.out.println("PEPSET\t"+pepSet.size());
		
		DiannPrMatrixReader prReader = new DiannPrMatrixReader(new File(resultFolderFile, "combined.pr_matrix.tsv"));
		FlashLfqQuanPeptide[] peps = prReader.getMetaPeptides(diaPp);
		String[] quanExpNames = prReader.getIntensityTitle();
		
		try {

			PrintWriter countWriter = new PrintWriter(new File(resultOutputFolder, "final_summary.tsv"));
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Raw file").append("\t");
			titlesb.append("Experiment").append("\t");
			titlesb.append("MS/MS").append("\t");
			titlesb.append("MS/MS Identified").append("\t");
			titlesb.append("MS/MS Identified [%]").append("\t");
			titlesb.append("Peptide Sequences Identified");

			countWriter.println(titlesb);

			int totalms2count = 0;

			HashMap<String, Integer> totalms2Map = new HashMap<String, Integer>();
			HashMap<String, HashSet<String>> totalUnipepMap = new HashMap<String, HashSet<String>>();
			HashSet<String> totalPepSet = new HashSet<String>();
			BufferedReader idReader = new BufferedReader(
					new FileReader(new File(resultFolderFile, "combined.tsv")));
			String line = idReader.readLine();
			while ((line = idReader.readLine()) != null) {
				String[] cs = line.split("\t");

				if (totalms2Map.containsKey(cs[0])) {
					totalms2Map.put(cs[0], totalms2Map.get(cs[0]) + 1);
					totalUnipepMap.get(cs[0]).add(cs[14]);
				} else {
					totalms2Map.put(cs[0], 1);
					HashSet<String> set = new HashSet<String>();
					set.add(cs[14]);
					totalUnipepMap.put(cs[0], set);
				}
				totalPepSet.add(cs[14]);
			}
			idReader.close();

			for (String file : totalUnipepMap.keySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(file).append("\t");

				sb.append(file).append("\t");
				int idCounts = totalms2Map.get(file);
				int pepCount = totalUnipepMap.get(file).size();
				sb.append(idCounts).append("\t");
				sb.append(idCounts).append("\t");
				sb.append("1.0").append("\t");
				sb.append(pepCount);

				countWriter.println(sb);

				totalms2count += idCounts;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Total").append("\t");
			sb.append("\t");
			sb.append(totalms2count).append("\t");
			sb.append(totalms2count).append("\t");
			sb.append("1.0").append("\t");
			sb.append(totalPepSet.size());

			countWriter.print(sb);

			countWriter.close();

		} catch (IOException e) {

		}
		
		

		HashMap<String, FlashLfqQuanPeptide> modseqPepMap = new HashMap<String, FlashLfqQuanPeptide>();
		for (FlashLfqQuanPeptide pep : peps) {
			modseqPepMap.put(pep.getModSeq(), pep);
		}
		DiaNNResultReader shufReader = new DiaNNResultReader(resultFolderFile, "shuffled", task.diannPar.getCut());
		DiaNNPrecursor[] shufDiaPp = shufReader.getDiaNNPrecursors();
		
		ConcurrentHashMap<String, HashMap<String, HashSet<String>>> matchedMap = task.getGenomeFromPep(pepSet, 12);

		File shufPrFile = new File(resultFolderFile, "shuffled.pr_matrix.tsv");
		DiannPrMatrixReader shufPrReader = new DiannPrMatrixReader(shufPrFile);
		FlashLfqQuanPeptide[] shufPeps = shufPrReader.getMetaPeptides(shufDiaPp);

		HashMap<String, String> shufPepMap = new HashMap<String, String>();
		File shufPeptideFile = new File(resultFolderFile, "shuffledPeptide.tsv");
		try (BufferedReader shufPepReader = new BufferedReader(new FileReader(shufPeptideFile))) {
			String line = shufPepReader.readLine();
			while ((line = shufPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				shufPepMap.put(cs[0], cs[1]);
			}
			shufPepReader.close();
		} catch (IOException e) {

		}
		
		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		for (String genome : matchedMap.keySet()) {
			HashMap<String, HashSet<String>> proPepMap = matchedMap.get(genome);
			for (String pro : proPepMap.keySet()) {
				HashSet<String> pset = proPepMap.get(pro);
				for (String pep : pset) {
					if (pepProMap.containsKey(pep)) {
						pepProMap.get(pep).add(pro);
					} else {
						HashSet<String> proSet = new HashSet<String>();
						proSet.add(pro);
						pepProMap.put(pep, proSet);
					}
				}
			}
		}
		System.out.println("pepProMap\t"+pepProMap.size());
		ArrayList<FlashLfqQuanPeptide> peplist = new ArrayList<FlashLfqQuanPeptide>();
		for (FlashLfqQuanPeptide peptide : modseqPepMap.values()) {
			String sequence = peptide.getSequence();
			if (pepProMap.containsKey(sequence)) {
				String[] pros = pepProMap.get(sequence).toArray(String[]::new);
				peptide.setProteins(pros);
				peplist.add(peptide);
			}
		}
		System.out.println("peplist\t"+peplist.size());
		L: for (FlashLfqQuanPeptide peptide : shufPeps) {
			String[] shufPros = peptide.getProteins();
			for (String pro : shufPros) {
				if (!pro.startsWith(FastaManager.shuffle)) {
					continue L;
				}
			}

			String shufsequence = peptide.getSequence();
			if (shufPepMap.containsKey(shufsequence)) {
				String sequence = shufPepMap.get(shufsequence);
				if (pepProMap.containsKey(sequence)) {
					String[] pros = pepProMap.get(sequence).toArray(String[]::new);
					for (int i = 0; i < pros.length; i++) {
						pros[i] = FastaManager.shuffle + pros[i];
					}
					peptide.setProteins(pros);
					peplist.add(peptide);
				}
			}
		}
		
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		
		for (int i = 0; i < peplist.size(); i++) {
			FlashLfqQuanPeptide quanPep = peplist.get(i);
			String sequence = quanPep.getSequence();
			String[] pros = quanPep.getProteins();
			for (int j = 0; j < pros.length; j++) {
				if (pros[j].startsWith(task. magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					if (genomeMap.containsKey(genome)) {
						genomeMap.get(genome).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						genomeMap.put(genome, set);
					}
				} else if (pros[j].startsWith(FastaManager.shuffle)) {
					String genome = FastaManager.shuffle
							+ pros[j].substring(FastaManager.shuffle.length()).split("_")[0];
					if (genomeMap.containsKey(genome)) {
						genomeMap.get(genome).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						genomeMap.put(genome, set);
					}
				}
			}
		}
		
		File hapGenomeFile = new File(resultFolderFile, "hap_genomes.tsv");
		HashSet<String> genomeSet = task. refineGenomes(genomeMap, pepSet.size(), 1.0, 0.0001, 500, true, hapGenomeFile);
		
		ArrayList<FlashLfqQuanPeptide> filteredList = new ArrayList<FlashLfqQuanPeptide>();
		for (int i = 0; i < peplist.size(); i++) {
			FlashLfqQuanPeptide quanPep = peplist.get(i);
			String[] pros = quanPep.getProteins();
			ArrayList<String> proList = new ArrayList<>();
			for (int j = 0; j < pros.length; j++) {
				if (pros[j].startsWith(task. magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					if (genomeSet.contains(genome)) {
						proList.add(pros[j]);
					}
				} else if (pros[j].startsWith(FastaManager.shuffle)) {
					String genome = FastaManager.shuffle
							+ pros[j].substring(FastaManager.shuffle.length()).split("_")[0];
					if (genomeSet.contains(genome)) {
						proList.add(pros[j]);
					}
				}
			}

			if (proList.size() > 0) {
				String[] proteins = proList.toArray(String[]::new);
				quanPep.setProteins(proteins);
				filteredList.add(quanPep);
			}
		}
		System.out.println("filteredList\t"+filteredList.size());
		FlashLfqQuanPeptide[] quanPeps = filteredList.toArray(FlashLfqQuanPeptide[]::new);
		HashMap<String, Double> genomeScoreMap = task.refineGenomes(quanPeps, FastaManager.shuffle);
		String[] razorProteins = task.refineProteins(quanPeps, genomeScoreMap, FastaManager.shuffle);

		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> proteinIntensityMap = new HashMap<String, double[]>();
		HashMap<String, Double> proteinTotalIntenMap = new HashMap<String, Double>();

		HashSet<String> pepSequenceSet = new HashSet<String>();
		HashMap<String, HashSet<String>> totalPepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> razorPepCountMap = new HashMap<String, HashSet<String>>();

		ArrayList<FlashLfqQuanPeptide> peptideList = new ArrayList<FlashLfqQuanPeptide>();

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

				if (razorProteins[i].startsWith(task.magDb.getIdentifier())) {
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

		int[] filePsmCount = new int[quanExpNames.length];
		HashSet<String>[] fileSequenceSets = new HashSet[quanExpNames.length];
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

						if (proteins[j].startsWith(task. magDb.getIdentifier())) {
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

		try (PrintWriter idWriter = new PrintWriter(new File(resultFolderFile, "id.tsv"))) {
			idWriter.println("Raw file\tUnique peptide count\tPSM count");
			for (int i = 0; i < quanExpNames.length; i++) {
				idWriter.println(quanExpNames[i] + "\t" + fileSequenceSets[i].size() + "\t" + filePsmCount[i]);
			}
			idWriter.close();
		} catch (IOException e) {

		}

		HashMap<String, ArrayList<Integer>> proPepIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> proPepRazorIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, Double> proScoreMap = new HashMap<String, Double>();
		try {
			File final_pep_txt = new File(resultOutputFolder, "final_peptides.tsv");

			PrintWriter writer = new PrintWriter(final_pep_txt);

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

			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("Identification type " + quanExpNames[i]).append("\t");
			}

			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("MS/MS count " + quanExpNames[i]).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("Intensity " + quanExpNames[i]).append("\t");
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			writer.println(titlesb);

			int id = 0;
			for (FlashLfqQuanPeptide peptide : peptideList) {

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

		}

		ArrayList<MetaProtein> proteinList = new ArrayList<MetaProtein>();
		try {

			File final_pro_txt = new File(resultOutputFolder, "final_proteins.tsv");

			PrintWriter writer = new PrintWriter(final_pro_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Protein IDs").append("\t");
			titlesb.append("Peptide counts (all)").append("\t");
			titlesb.append("Peptide counts (razor)").append("\t");
			titlesb.append("Number of PSMs").append("\t");
			titlesb.append("PEP").append("\t");
			titlesb.append("Intensity").append("\t");
			for (int i = 0; i < quanExpNames.length; i++) {
				titlesb.append("Intensity " + quanExpNames[i]).append("\t");
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

		}

		File funcFile = new File(resultOutputFolder, "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		File funTsv = new File(funcFile, "functions.tsv");
		File funReportTsv = new File(funcFile, "functions_report.tsv");
		task.final_pro_xml_file = new File(funcFile, "functions.xml");

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();
		MetaProtein[] metapros = proteinList.toArray(new MetaProtein[proteinList.size()]);
		MetaProteinAnnoMag[] mpas =task.funcAnnoSqlite(metapros, quanExpNames);
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
			funReader = new MetaProteinXMLReader2(task.final_pro_xml_file);
		} catch (DocumentException e) {

		}

		if (!funTsv.exists() || !funReportTsv.exists()) {
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {

			}
		}

		File html = new File(funcFile, "functions.html");
		if (!html.exists()) {
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
			}
		}

		File taxFile = new File(resultOutputFolder, "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		MagFuncSearcher searcher = null;
		try {
			searcher = new MagFuncSearcher(metaPar.getMicroDb(), msv.getFuncDef());

		} catch (NumberFormatException | SQLException | IOException e) {

		}

		String[] genomes = genomeIntensityMap.keySet().toArray(new String[genomeIntensityMap.size()]);
		HashMap<String, String[]> genomeTaxaMap = null;
		try {
			genomeTaxaMap = searcher.matchGenome(genomes);
		} catch (IOException e) {

		}

		File genomeHtmlFile = new File(taxFile, "Genome.html");
		try {
			HMGenomeHtmlWriter htmlWriter = new HMGenomeHtmlWriter(genomeHtmlFile);
			htmlWriter.write(genomeTaxaMap);
			htmlWriter.close();
		} catch (IOException e) {

		}

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		double[] cellularIntensity = new double[quanExpNames.length];
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

			for (String exp : quanExpNames) {
				if (exp.startsWith("Intensity ")) {
					genomeTitlesb.append("\t").append(exp);
				} else {
					genomeTitlesb.append("\tIntensity ").append(exp);
				}
			}

			genomeWriter.println(genomeTitlesb);

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
							taxScoreMaps[j].put(taxon, taxScoreMaps[j].get(taxon) -Math.log10(score));
						} else {
							taxScoreMaps[j].put(taxon, -Math.log10(score));
						}
					}
				}
			}
			genomeWriter.close();

		} catch (FileNotFoundException e) {

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
			for (String exp : quanExpNames) {
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
			for (String exp : quanExpNames) {
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

		}


		File reportDir = new File(resultOutputFolder, "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, "1.0.0");

		metaPar.setReportDir(reportDir);

		File reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportHtmlDir.exists()) {
			reportHtmlDir.mkdirs();
		}
		
		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}

		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(taxRankMaps, taxIntensityMaps, cellularIntensity, quanExpNames, jsOutput);
		}

		MetaReportTask reportTask = new MetaReportTask(metaPar.getThreadCount());

		File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
		if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {
			reportTask.addTask(MetaReportTask.peptide, new File(resultOutputFolder, "final_peptides.tsv"),
					report_peptides_summary);
		}

		File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
		if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
			reportTask.addTask(MetaReportTask.protein, new File(resultOutputFolder, "final_peptides.tsv"),
					report_proteinGroups_summary);
		}

		File report_taxonomy_summary = new File(reportHtmlDir, "report_taxonomy_summary.html");
		if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
			reportTask.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary);
		}

		File report_function_summary = new File(reportHtmlDir, "report_function_summary.html");
		if (!report_function_summary.exists() || report_function_summary.length() == 0) {
			reportTask.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		MetaDiaTask.compareTest("Z:\\Kai\\Raw_files\\Western_DIA\\Plate16\\MetaLab\\20230504_uOttawa_AD_Plate16_DIA_H12_1_422",
//				"Z:\\Kai\\Raw_files\\Western_DIA\\Plate16\\MetaLab\\mag_result\\combined.fasta");
		
		long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
				.getTotalMemorySize() / 1024 / 1024 / 1024;
		System.out.println(totalMemorySize);
		
		System.out.println(Runtime.getRuntime().maxMemory() / 1024 / 1024 / 1024);
		
		java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
	    int cores = osBean.getAvailableProcessors();

	    System.out.println("Available Cores: " + cores);
//	    System.out.println("Logical Processors: " + logicalProcessors);
//	    MetaDiaTask.testFDR("Z:\\Kai\\Raw_files\\For_Kai_MouseGut\\MetaLab_selfmodel\\mag_result");
	    MetaDiaTask.exportTest("Z:\\Kai\\paper_data\\DIA\\westlake\\westlake\\mag_result", 
	    		"Z:\\Kai\\paper_data\\DIA\\westlake\\test");
	}

}
