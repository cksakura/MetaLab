/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.TandemFastaGetter;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemParameterHandler;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemTask;
import bmi.med.uOttawa.metalab.spectra.cluster.ClusterTask;
import bmi.med.uOttawa.metalab.spectra.io.ProteomicsTools;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MetaDbCreateTaskV1 extends SwingWorker<File, Void> {

	private MetaParameterV1 parameter;
	private XTandemParameterHandler tandemParHandler;
	private ProteomicsTools pt;
	private int[] progress;

	private JProgressBar progressBar1;
	private JProgressBar progressBar2;

	private File combineLogFile;
	private File preProcessFile;
	private ArrayList<File> tempLogFileList;
	private ArrayList<PrintWriter> tempLogWriterList;

	protected static String taskName = "Sample specific database generation";

	public static final double tandemFdr = 0.01;
	public static final double tandemEValue = 0.05;

	private boolean fastMode = true;

	private boolean finished = false;

	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * used in cli mode, progress value was not needed
	 * 
	 * @param parameter
	 */
	public MetaDbCreateTaskV1(MetaParameterV1 parameter) {
		this.parameter = parameter;
		this.tandemParHandler = new XTandemParameterHandler(parameter);
		this.pt = new ProteomicsTools(parameter);
	}

	/**
	 * 
	 * @param parameter
	 * @param progress
	 *            the positions of begin and end of progressBar2
	 * @param methodId
	 * @param progressBar1
	 * @param progressBar2
	 */
	public MetaDbCreateTaskV1(MetaParameterV1 parameter, int[] progress, JProgressBar progressBar1,
			JProgressBar progressBar2) {
		this.parameter = parameter;
		this.tandemParHandler = new XTandemParameterHandler(parameter);
		this.pt = new ProteomicsTools(parameter);
		this.progress = progress;
		this.progressBar1 = progressBar1;
		this.progressBar2 = progressBar2;
	}

	private void initial() {
		this.preProcessFile = new File(parameter.getResultDir(), "pre_processing");
		if (!preProcessFile.exists()) {
			this.preProcessFile.mkdirs();
		}

		this.combineLogFile = new File(parameter.getParameterDir(), "pre_processing.log");
		this.tempLogFileList = new ArrayList<File>();
		this.tempLogWriterList = new ArrayList<PrintWriter>();

		this.tandemParHandler.config(parameter);
	}

	/**
	 * MetaProIQ strategy
	 * 
	 * @param raw
	 * @param id
	 * @throws IOException
	 * @throws DocumentException
	 */
	private void firstSearch(String raw, int id) {

		File logfile = new File(parameter.getParameterDir(), "1_" + id + ".log");
		this.tempLogFileList.add(logfile);

		PrintWriter logWriter = null;
		try {
			logWriter = new PrintWriter(logfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logfile, e);
		}

		this.tempLogWriterList.add(logWriter);

		File file = new File(raw);
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File resultFile = new File(preProcessFile, "first_search." + name + ".xml");
		String fileName = resultFile.getAbsolutePath();

		if (resultFile.exists()) {

			LOGGER.info(taskName + ": first search result " + fileName + " already existed, skip...");
			logWriter.println(format.format(new Date()) + "\t" + "First search result for " + raw
					+ " already existed in " + fileName + ", skip");

		} else {

			LOGGER.info(taskName + ": searching " + raw + " in original database started...");
			logWriter.println(format.format(new Date()) + "\t" + "Searching " + raw + " in original database started");

			File tandemInputFile = new File(parameter.getParameterDir(), "1_" + id + "_input.xml");

			boolean finish = false;
			XTandemTask task = new XTandemTask(parameter, logWriter);
			try {
				task.run(raw, tandemInputFile);
				finish = true;
			} finally {
				if (!finish) {
					logWriter.println(
							format.format(new Date()) + "\t" + "Searching " + raw + " in original database failed");
					logWriter.close();
				}
			}

			File[] files = file.getParentFile().listFiles();

			for (int i = 0; i < files.length; i++) {
				String namei = files[i].getName();
				if (namei.endsWith("xml")) {
					namei = namei.substring(0, namei.length() - 26);
					if (name.equals(namei)) {

						Path sourcei = Paths.get(files[i].getAbsolutePath());
						Path targeti = Paths.get(fileName);
						boolean moveFinish = false;
						try {
							Files.move(sourcei, targeti, StandardCopyOption.REPLACE_EXISTING);
							moveFinish = true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in moving X!Tandem result file from " + sourcei + " to " + targeti, e);
						} finally {
							if (!moveFinish) {
								logWriter.println(format.format(new Date()) + "\t" + "Moving " + sourcei + " to "
										+ targeti + " failed");
								logWriter.close();
							}
						}

						logWriter.println(format.format(new Date()) + "\t" + "Result file " + files[i].getName()
								+ " moved to " + fileName);
						break;
					}
				}
			}

			LOGGER.info(taskName + ": searching " + raw + " in original database finished...");
			logWriter.println(format.format(new Date()) + "\t" + "Peptide identification for " + raw
					+ "in original database finished");
		}

		TandemFastaGetter getter;
		int[] counts = null;
		boolean getFinish = false;
		try {
			getter = new TandemFastaGetter(fileName);
			counts = getter.getPSMCount(tandemEValue);
			getFinish = true;
		} finally {
			if (!getFinish) {
				logWriter.println(format.format(new Date()) + "\t" + "Getting the number of peptide spectra matches in "
						+ fileName + " failed");
				logWriter.close();
			}
		}

		logWriter.println(format.format(new Date()) + "\t" + counts[0] + " peptide spectra matches found in " + raw
				+ ", " + counts[1] + " with expect < " + tandemEValue);

		boolean copyFinish = false;
		Path source = Paths.get(parameter.getXTandemDefaultInput());
		Path target = Paths
				.get(tandemParHandler.getPreProcessParFile() + "\\first_search." + name + ".default_input.xml");
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			copyFinish = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying X!Tandem parameter file from " + source + " to " + target, e);
		} finally {
			if (!copyFinish) {
				logWriter.println(format.format(new Date()) + "\t" + "Copying " + source + " to " + target + " failed");
				logWriter.close();
			}
		}

		logWriter.close();
	}

	/**
	 * MetaProIq strategy
	 * 
	 * @param raw
	 * @param fasta
	 * @param id
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void secondSearch(String raw, String fasta, int id) {

		File logfile = new File(parameter.getParameterDir(), "2_" + id + ".log");
		this.tempLogFileList.add(logfile);

		boolean writeFinish = false;
		PrintWriter logWriter = null;
		try {
			logWriter = new PrintWriter(logfile);
			writeFinish = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logfile, e);
		} finally {
			if (!writeFinish) {
				logWriter.close();
			}
		}

		this.tempLogWriterList.add(logWriter);

		File file = new File(raw);
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File resultFile = new File(preProcessFile, "second_search." + name + ".xml");
		String fileName = resultFile.getAbsolutePath();

		if (resultFile.exists()) {

			LOGGER.info(taskName + ": Second search result " + fileName + " already existed, skip...");
			logWriter.println(format.format(new Date()) + "\t" + "Second search result for " + raw
					+ " already existed in " + fileName + ", skip");

		} else {

			int proCount = 0;
			TandemFastaGetter getter;
			String tempFasta = parameter.getFastaDir() + "\\ss." + name + ".fasta";

			boolean getFinish = false;
			try {

				getter = new TandemFastaGetter(preProcessFile + "\\first_search." + name + ".xml");
				proCount = TandemFastaGetter.writeWithDecoy(getter.parseProtein(0, 0), fasta, tempFasta);
				getFinish = true;

			} finally {

				if (!getFinish) {
					logWriter.println(format.format(new Date()) + "\t" + "Getting the number of proteins in "
							+ preProcessFile + "\\first_search." + name + ".xml" + " failed");
					logWriter.close();
				}
			}

			LOGGER.info(taskName + ": sample specific database " + tempFasta + "created...");
			logWriter.println(format.format(new Date()) + "\t" + "Sample specific protein database " + tempFasta
					+ " created, " + proCount + " target protein sequences included");

			LOGGER.info(taskName + ": searching " + raw + " in original database started...");
			logWriter.println(format.format(new Date()) + "\t" + "Searching " + raw + " in original database started");

			File tandemInputFile = new File(parameter.getParameterDir(), "2_" + id + "_input.xml");
			tandemParHandler.setDB2Input(tandemInputFile, new File(tempFasta));

			boolean taskFinish = false;
			XTandemTask task = new XTandemTask(parameter, logWriter);
			try {
				task.run(raw, tandemInputFile);
				taskFinish = true;

			} finally {
				if (!taskFinish) {
					logWriter.println(
							format.format(new Date()) + "\t" + "Searching " + raw + " in original database failed");
					logWriter.close();
				}
			}

			File[] files = file.getParentFile().listFiles();

			for (int i = 0; i < files.length; i++) {
				String namei = files[i].getName();
				if (namei.endsWith("xml")) {
					namei = namei.substring(0, namei.length() - 26);
					if (name.equals(namei)) {

						Path sourcei = Paths.get(files[i].getAbsolutePath());
						Path targeti = Paths.get(fileName);
						try {
							Files.move(sourcei, targeti, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in moving X!Tandem result file from " + sourcei + " to " + targeti, e);
						} finally {
							logWriter.println(format.format(new Date()) + "\t" + "Moving " + sourcei + " to " + targeti
									+ " failed");
							logWriter.close();
						}

						logWriter.println(format.format(new Date()) + "\t" + "Result file " + files[i].getName()
								+ "\" moved to " + fileName);
						break;
					}
				}
			}
			LOGGER.info(taskName + ": searching " + raw + " in sample specific database finished...");
			logWriter.println(format.format(new Date()) + "\t" + "Peptide identification for " + raw
					+ "in sample specific database finished");
		}

		int[] counts = new int[2];
		TandemFastaGetter getter = null;
		try {
			getter = new TandemFastaGetter(fileName);
			counts = getter.getPSMCount(tandemEValue);
		} finally {
			logWriter.println(format.format(new Date()) + "\t" + "Getting the number of peptide spectra matches in "
					+ fileName + " failed");
			logWriter.close();
		}

		logWriter.println(format.format(new Date()) + "\t" + counts[0] + " peptide spectra matches found in \"" + raw
				+ ", " + counts[1] + " with expect < " + tandemEValue);

		Path source = Paths.get(parameter.getXTandemDefaultInput());
		Path target = Paths
				.get(tandemParHandler.getPreProcessParFile() + "\\second_search." + name + ".default_input.xml");
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying X!Tandem parameter file from " + source + " to " + target, e);
		} finally {
			logWriter.println(format.format(new Date()) + "\t" + "Copying " + source + " to " + target + " failed");
			logWriter.close();
		}

		logWriter.close();
	}

	/**
	 * Clustering strategy
	 * 
	 * @throws Exception
	 */
	private void cluster() {

		File logfile1 = new File(parameter.getParameterDir(), "\\1.log");
		this.tempLogFileList.add(logfile1);
		PrintWriter logWriter = null;
		boolean writeFinish = false;

		try {
			logWriter = new PrintWriter(logfile1);
			writeFinish = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logfile1, e);
		} finally {
			if (!writeFinish) {
				logWriter.close();
			}
		}
		this.tempLogWriterList.add(logWriter);

		File clusterDir = new File(preProcessFile, "cluster");
		File clusterMgf = new File(clusterDir, "cluster.mgf");
		clusterDir.mkdir();

		if (clusterMgf.exists()) {
			LOGGER.info(taskName + ": clustered MS/MS spectra file " + clusterMgf + " already existed, skip...");
			logWriter
					.println(format.format(new Date()) + "\t" + "Cluster result was found in " + clusterMgf + ", skip");
		} else {

			LOGGER.info(taskName + ": converting raw files started...");
			logWriter.println(format.format(new Date()) + "\t" + "Converting raw files to mgf files started");

			String[] mgfs = convert2Mgf();

			if (mgfs == null) {
				LOGGER.error("Error in converting mgf files, no mgf files found, task failed");
				logWriter.println(format.format(new Date()) + "\t" + "Clustering MS/MS spectra failed");
				logWriter.close();
				return;
			}

			LOGGER.info(taskName + ": converting raw files finished...");

			logWriter.println(format.format(new Date()) + "\t" + "Converting raw files to mgf files finished");
			logWriter.println(format.format(new Date()) + "\t" + "Clustering MS/MS spectra started");

			LOGGER.info(taskName + ": clustering MS/MS spectra started...");

			ClusterTask clusterTask = new ClusterTask(parameter.getThreadCount(), fastMode);
			boolean clusterFinish = false;
			try {
				clusterTask.clustering(mgfs, clusterDir, logWriter, clusterMgf.getName());
				clusterFinish = true;
			} finally {
				if (!clusterFinish) {
					logWriter.println(format.format(new Date()) + "\t" + "Clustering MS/MS spectra failed");
					logWriter.close();
				}
			}

			LOGGER.info(taskName + ": clustering MS/MS spectra finished...");
			logWriter.println(format.format(new Date()) + "\t" + "Clustering MS/MS spectra finished");
		}
		logWriter.close();
	}

	/**
	 * Clustering strategy
	 * 
	 * 
	 */
	private void clusterSearch() {

		File clusterDir = new File(preProcessFile, "cluster");
		File clusterMgf = new File(clusterDir, "cluster.mgf");
		File tandemResult = new File(clusterDir, "cluster.xml");

		File logfile = new File(parameter.getParameterDir(), "2.log");
		this.tempLogFileList.add(logfile);
		PrintWriter logWriter = null;
		boolean writeFinish = false;

		try {
			logWriter = new PrintWriter(logfile);
			writeFinish = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logfile, e);
		} finally {
			if (!writeFinish) {
				logWriter.close();
			}
		}
		this.tempLogWriterList.add(logWriter);

		if (tandemResult.exists()) {
			LOGGER.info(taskName + ": search result for " + clusterMgf + " was found in " + tandemResult + ", skip");
			logWriter.println(format.format(new Date()) + "\t" + "Search result for " + clusterMgf + " was found in "
					+ tandemResult + ", skip");
		} else {

			LOGGER.info(taskName + ": searching " + clusterMgf + " in original database started...");
			logWriter.println(format.format(new Date()) + "\t" + "Searching clustered MS/MS spectra " + clusterMgf
					+ " in original database started");

			File tandemInputFile0 = new File(tandemParHandler.getPreProcessParFile(), "0_input.xml");

			XTandemTask task = new XTandemTask(parameter, tandemParHandler, logWriter);
			boolean taskFinish = false;
			try {
				task.run(clusterMgf, tandemInputFile0);
				taskFinish = false;
			} finally {
				if (!taskFinish) {
					logWriter.println(format.format(new Date()) + "\t" + "Searching clustered MS/MS spectra "
							+ clusterMgf + " in original database failed");
					logWriter.close();
				}
			}

			File[] clusterFiles = clusterDir.listFiles();
			for (int i = 0; i < clusterFiles.length; i++) {
				String namei = clusterFiles[i].getName();
				if (namei.endsWith("xml")) {
					Path xmlSource = Paths.get(clusterFiles[i].getAbsolutePath());
					Path xmlTarget = Paths.get((new File(preProcessFile, "cluster.xml")).getAbsolutePath());
					boolean moveFinish = false;
					try {
						Files.move(xmlSource, xmlTarget, StandardCopyOption.REPLACE_EXISTING);
						moveFinish = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in moving result file from " + xmlSource + " to " + xmlTarget, e);
					} finally {
						if (!moveFinish) {
							logWriter.println(format.format(new Date()) + "\t" + "Moving " + xmlSource + " to "
									+ xmlTarget + " failed");
							logWriter.close();
						}
					}
					break;
				}
			}

			LOGGER.info(taskName + ": searching " + clusterMgf + " in original database finished...");
			logWriter.println(format.format(new Date()) + "\tsearching clustered MS/MS spectra " + clusterMgf
					+ " in original database finished");

			int fileId = 1;
			for (int i = 1;; i++) {
				File clusterMgfi = new File(clusterDir, "cluster_" + i + ".mgf");
				if (clusterMgfi.exists()) {
					fileId = i + 1;
				} else {
					break;
				}
			}

			if (fileId > 1) {
				for (int i = 1; i < fileId; i++) {

					File clusterMgfi = new File(clusterDir, "cluster_" + i + ".mgf");

					File tandemInputFilei = new File(tandemParHandler.getPreProcessParFile(), i + "_input.xml");

					LOGGER.info(taskName + ": searching " + clusterMgfi + " in original database started...");
					logWriter.println(format.format(new Date()) + "\t" + "Searching clustered MS/MS spectra "
							+ clusterMgfi + " in original database started");

					boolean taskFinishi = false;
					try {
						task.run(clusterMgfi, tandemInputFilei);
						taskFinishi = true;
					} finally {
						if (!taskFinishi) {
							logWriter.println(format.format(new Date()) + "\t" + "Searching clustered MS/MS spectra "
									+ clusterMgfi + " in original database failed");
							logWriter.close();
						}
					}

					File[] clusterFilesi = clusterDir.listFiles();
					for (int j = 0; j < clusterFilesi.length; j++) {
						String namei = clusterFilesi[j].getName();
						if (namei.endsWith("xml")) {
							Path xmlSource = Paths.get(clusterFilesi[j].getAbsolutePath());
							Path xmlTarget = Paths
									.get((new File(preProcessFile, "cluster_" + i + ".xml")).getAbsolutePath());
							boolean moveFinish = false;
							try {
								Files.move(xmlSource, xmlTarget, StandardCopyOption.REPLACE_EXISTING);
								moveFinish = true;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in moving result file from " + xmlSource + " to " + xmlTarget, e);
							} finally {
								if (!moveFinish) {
									logWriter.println(format.format(new Date()) + "\t" + "Moving " + xmlSource + " to "
											+ xmlTarget + " failed");
									logWriter.close();
								}
							}
							break;
						}
					}

					LOGGER.info(taskName + ": searching " + clusterMgfi + " in original database finished...");
					logWriter.println(format.format(new Date()) + "\tsearching clustered MS/MS spectra " + clusterMgfi
							+ " in original database finished");
				}
			}
		}
		logWriter.close();
	}

	private String[] convert2Mgf() {
		String[] raws = parameter.getRawFiles()[0];
		String[] mgfs = new String[raws.length];
		for (int i = 0; i < mgfs.length; i++) {
			File rawi = new File(raws[i]);
			String rawname = rawi.getName();
			String mgfname = rawname.substring(0, rawname.lastIndexOf(".")) + ".mgf";
			File mgfi = new File(rawi.getParentFile(), mgfname);
			if (!mgfi.exists()) {
				if (rawname.endsWith("raw") || rawname.endsWith("RAW")) {
					pt.raw2Mgf(raws[i]);

				} else {
					LOGGER.error(
							"Cann't convert raw file " + rawi + " to mgf file, only .raw are supported");
					return null;
				}

			}
			mgfs[i] = mgfi.getAbsolutePath();
		}
		return mgfs;
	}

	public File runCLI() {

		initial();

		File logfile = new File(parameter.getParameterDir(), "3.log");
		PrintWriter logWriter3 = null;
		try {
			logWriter3 = new PrintWriter(logfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logfile, e);
		}

		String database = parameter.getDatabase();
		String[] raws = parameter.getRawFiles()[0];

		String originalFasta = parameter.getDatabase();
		String ssDatabase = parameter.getFastaDir() + "\\"
				+ originalFasta.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.lastIndexOf("."))
				+ ".sample_specific.fasta";

		File ssDatabaseFile = new File(ssDatabase);
		if (ssDatabaseFile.exists()) {

			LOGGER.info("sample specific database already existed in " + ssDatabase);
			logWriter3.println(
					format.format(new Date()) + "\t" + "sample specific database already existed in " + ssDatabase);

		} else {

			if (parameter.isCluster()) {

				cluster();

				clusterSearch();

				int fileId = 1;
				for (int i = 1;; i++) {
					File clusterMgfi = new File(preProcessFile, "cluster_" + i + ".xml");
					if (clusterMgfi.exists()) {
						fileId = i + 1;
					} else {
						break;
					}
				}

				File[] clusterResults = new File[fileId];
				clusterResults[0] = new File(preProcessFile, "cluster.xml");
				for (int i = 1; i < fileId; i++) {
					clusterResults[i] = new File(preProcessFile, "cluster_" + i + ".xml");
				}

				logWriter3.println(format.format(new Date()) + "\t"
						+ "Writing protein sequences to sample specific database " + ssDatabase);
				boolean finish = false;

				try {
					if (parameter.isAppendHostDb()) {

						String temp = parameter.getFastaDir() + "\\" + originalFasta
								.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.length() - 5) + "temp";

						int proCount = TandemFastaGetter.batchWrite(clusterResults, originalFasta, temp, tandemFdr,
								tandemEValue);

						String[] hostDbs = parameter.getHostDBs();

						PrintWriter writer = null;
						try {
							writer = new PrintWriter(ssDatabase);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in writing protein sequence to sample-specific database " + ssDatabase,
									e);
						}
						BufferedReader r0 = null;
						try {
							r0 = new BufferedReader(new FileReader(temp));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}
						String l0 = null;
						try {
							while ((l0 = r0.readLine()) != null) {
								writer.println(l0);
							}
							r0.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}

						int hostProCount = 0;
						for (String hdb : hostDbs) {
							BufferedReader r1 = null;
							try {
								r1 = new BufferedReader(new FileReader(hdb));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}
							String l1 = null;
							try {
								while ((l1 = r1.readLine()) != null) {
									writer.println(l1);
									if (l1.startsWith(">")) {
										hostProCount++;
									}
								}
								r1.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}

						}
						writer.close();

						FileUtils.deleteQuietly(new File(temp));

						logWriter3.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + (proCount + hostProCount)
								+ " target protein sequences were included (" + proCount
								+ " protein sequences were identified from the microbiome database, " + hostProCount
								+ " protein sequences were from the host database");

						parameter.setPepIdenResults(clusterResults);

					} else {
						int proCount = TandemFastaGetter.batchWrite(clusterResults, originalFasta, ssDatabase,
								tandemFdr, tandemEValue);
						logWriter3.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + proCount + " target protein sequences were included");

						parameter.setPepIdenResults(clusterResults);
					}
					finish = true;

				} finally {
					if (!finish) {
						logWriter3.println(format.format(new Date()) + "\t" + "Generating sample-specific database "
								+ ssDatabase + " failed");
						logWriter3.close();
					}
				}

			} else {

				for (int i = 0; i < raws.length; i++) {
					firstSearch(raws[i], i + 1);
				}

				for (int i = 0; i < raws.length; i++) {
					secondSearch(raws[i], database, i + 1);
				}

				LOGGER.info(taskName + ": writing protein sequences to sample specific database " + ssDatabase);
				logWriter3.println(format.format(new Date()) + "\t" + "Writing protein sequences to combined database "
						+ ssDatabase);

				String[] step2Results = new String[raws.length];
				File[] pepIdenResults = new File[step2Results.length];
				for (int i = 0; i < step2Results.length; i++) {
					String name = raws[i].substring(raws[i].lastIndexOf("\\") + 1, raws[i].lastIndexOf("."));
					step2Results[i] = preProcessFile + "\\second_search." + name + ".xml";
					pepIdenResults[i] = new File(step2Results[i]);
				}
				boolean finish = false;

				try {
					if (parameter.isAppendHostDb()) {

						String temp = parameter.getFastaDir() + "\\" + originalFasta
								.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.length() - 5) + "temp";

						int proCount = TandemFastaGetter.batchWrite(step2Results, originalFasta, temp, tandemFdr,
								tandemEValue);

						String[] hostDbs = parameter.getHostDBs();

						PrintWriter writer = null;
						try {
							writer = new PrintWriter(ssDatabase);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in writing protein sequence to sample-specific database " + ssDatabase,
									e);
						}
						BufferedReader r0 = null;
						try {
							r0 = new BufferedReader(new FileReader(temp));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}
						String l0 = null;
						try {
							while ((l0 = r0.readLine()) != null) {
								writer.println(l0);
							}
							r0.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}

						int hostProCount = 0;
						for (String hdb : hostDbs) {
							BufferedReader r1 = null;
							try {
								r1 = new BufferedReader(new FileReader(hdb));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}
							String l1 = null;
							try {
								while ((l1 = r1.readLine()) != null) {
									writer.println(l1);
									if (l1.startsWith(">")) {
										hostProCount++;
									}
								}
								r1.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}

						}
						writer.close();

						FileUtils.deleteQuietly(new File(temp));

						logWriter3.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + (proCount + hostProCount)
								+ " target protein sequences were included (" + proCount
								+ " protein sequences were identified from the microbiome database, " + hostProCount
								+ " protein sequences were from the host database");

						parameter.setPepIdenResults(pepIdenResults);

					} else {
						int proCount = TandemFastaGetter.batchWrite(step2Results, originalFasta, ssDatabase, tandemFdr,
								tandemEValue);
						logWriter3.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + proCount + " target protein sequences were included");

						parameter.setPepIdenResults(pepIdenResults);
					}
					finish = true;

				} finally {
					if (!finish) {
						logWriter3.println(format.format(new Date()) + "\t" + "Generating sample-specific database "
								+ ssDatabase + " failed");
						logWriter3.close();
					}
				}
			}
		}

		parameter.setPepIdenResultType(MetaSearchEngine.xtandem.getId());

		this.tempLogFileList.add(logfile);
		this.tempLogWriterList.add(logWriter3);

		File dir = (new File(raws[0])).getParentFile();

		PrintWriter logWriter = null;
		try {
			logWriter = new PrintWriter(combineLogFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + combineLogFile, e);
		}
		logWriter.println(taskName + " task created");

		for (String raw : raws) {
			String name = (new File(raw)).getName();
			name = name.substring(0, name.lastIndexOf("."));
			File mqfile = new File(dir, name);
			if (mqfile.exists()) {
				FileUtils.deleteQuietly(mqfile);
			}

			File indexFile = new File(dir, name + ".index");
			if (indexFile.exists()) {
				FileUtils.deleteQuietly(indexFile);
			}
		}

		for (PrintWriter writer : this.tempLogWriterList) {
			writer.close();
		}

		for (File file : this.tempLogFileList) {
			if (file.exists()) {
				boolean tempFinish = false;
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					while ((line = reader.readLine()) != null) {
						logWriter.println(line);
					}
					reader.close();
					tempFinish = false;

				} catch (IOException e) {
					LOGGER.error("Error in writing log file to " + combineLogFile, e);
				} finally {
					if (!tempFinish) {
						logWriter.println(
								format.format(new Date()) + "\t" + "Writing log file " + combineLogFile + " failed");
						logWriter.close();
					}
				}
			}
		}

		for (File file : this.tempLogFileList) {
			if (file.exists()) {
				FileUtils.deleteQuietly(file);
			}
		}

		logWriter.println(taskName + " task finished");
		logWriter.close();

		if (ssDatabaseFile.exists()) {
			finished = true;
		}

		return combineLogFile;
	}

	@Override
	protected File doInBackground() {
		// TODO Auto-generated method stub

		try {

			initial();

			progressBar1.setValue(0);
			progressBar2.setValue(0);
			progressBar1.setStringPainted(true);
			progressBar2.setStringPainted(true);

			File logfile = new File(parameter.getParameterDir(), "3.log");
			PrintWriter logWriter = null;
			try {
				logWriter = new PrintWriter(logfile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in writing log file to " + logfile, e);
			}

			String database = parameter.getDatabase();
			String[] raws = parameter.getRawFiles()[0];

			progressBar2.setString(taskName + " processing...");

			String originalFasta = parameter.getDatabase();
			String ssDatabase = parameter.getFastaDir() + "\\"
					+ originalFasta.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.length() - 5)
					+ "sample_specific.fasta";

			File ssDatabaseFile = new File(ssDatabase);
			if (ssDatabaseFile.exists()) {

				progressBar2.setValue(progress[1]);
				logWriter.println(format.format(new Date()) + "\t" + "sample specific database already existed in "
						+ ssDatabase + ", skip");

			} else {

				if (parameter.isCluster()) {

					progressBar1.setString("Clustering MS/MS spectra...");
					progressBar1.setIndeterminate(true);

					cluster();

					progressBar2.setValue((int) ((progress[1] - progress[0]) * 0.2 + progress[0]));
					progressBar1.setString("Searching clustered MS/MS spectra in original database...");

					clusterSearch();

					progressBar2.setValue(progress[1]);
					progressBar1.setIndeterminate(false);

					int fileId = 1;
					for (int i = 1;; i++) {
						File clusterMgfi = new File(preProcessFile, "cluster_" + i + ".xml");
						if (clusterMgfi.exists()) {
							fileId = i + 1;
						} else {
							break;
						}
					}

					File[] clusterResults = new File[fileId];
					clusterResults[0] = new File(preProcessFile, "cluster.xml");
					for (int i = 1; i < fileId; i++) {
						clusterResults[i] = new File(preProcessFile, "cluster_" + i + ".xml");
					}

					logWriter.println(format.format(new Date()) + "\t"
							+ "Writing protein sequences to sample specific database " + ssDatabase);

					if (parameter.isAppendHostDb()) {

						String temp = parameter.getFastaDir() + "\\" + originalFasta
								.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.length() - 5) + "temp";

						int proCount = TandemFastaGetter.batchWrite(clusterResults, originalFasta, temp, tandemFdr,
								tandemEValue);

						String[] hostDbs = parameter.getHostDBs();

						PrintWriter writer = null;
						try {
							writer = new PrintWriter(ssDatabase);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in writing protein sequence to sample-specific database " + ssDatabase,
									e);
						}
						BufferedReader r0 = null;
						try {
							r0 = new BufferedReader(new FileReader(temp));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}
						String l0 = null;
						try {
							while ((l0 = r0.readLine()) != null) {
								writer.println(l0);
							}
							r0.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}

						int hostProCount = 0;
						for (String hdb : hostDbs) {
							BufferedReader r1 = null;
							try {
								r1 = new BufferedReader(new FileReader(hdb));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}
							String l1 = null;
							try {
								while ((l1 = r1.readLine()) != null) {
									writer.println(l1);
									if (l1.startsWith(">")) {
										hostProCount++;
									}
								}
								r1.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}

						}
						writer.close();

						FileUtils.deleteQuietly(new File(temp));
						
						logWriter.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + (proCount + hostProCount)
								+ " target protein sequences were included (" + proCount
								+ " protein sequences were identified from the microbiome database, " + hostProCount
								+ " protein sequences were from the host database");

						parameter.setPepIdenResults(clusterResults);

					} else {
						int proCount = TandemFastaGetter.batchWrite(clusterResults, originalFasta, ssDatabase,
								tandemFdr, tandemEValue);
						logWriter.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + proCount + " target protein sequences were included");

						parameter.setPepIdenResults(clusterResults);
					}

				} else {

					progressBar1.setString("Searching original database: " + "0/" + raws.length);

					for (int i = 0; i < raws.length; i++) {
						firstSearch(raws[i], i + 1);
						progressBar1.setValue((int) ((double) (i + 1) / (double) raws.length * 100.0));
						progressBar2.setValue(
								(int) (((double) (i + 1) / (double) raws.length / 2.0) * (progress[1] - progress[0])
										+ progress[0]));
						progressBar1.setString("Searching original database: " + (i + 1) + "/" + raws.length);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in first search of raw file " + raws[i], e);
						}
					}

					progressBar1.setString("Searching sample-specific database: " + "0/" + raws.length);

					for (int i = 0; i < raws.length; i++) {
						secondSearch(raws[i], database, i + 1);
						progressBar1.setValue((int) ((double) (i + 1) / (double) raws.length * 100.0));
						progressBar2.setValue((int) (((double) (i + 1) / (double) raws.length / 2.0 + 0.5)
								* (progress[1] - progress[0]) + progress[0]));
						progressBar1.setString("Searching sample-specific database: " + (i + 1) + "/" + raws.length);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in second search of raw file " + raws[i], e);
						}
					}

					LOGGER.info(taskName + ": writing protein sequences to sample specific database " + ssDatabase);
					logWriter.println(format.format(new Date()) + "\t"
							+ "Writing protein sequences to combined database " + ssDatabase);

					String[] step2Results = new String[raws.length];
					File[] pepIdenResults = new File[step2Results.length];
					for (int i = 0; i < step2Results.length; i++) {
						String name = raws[i].substring(raws[i].lastIndexOf("\\") + 1, raws[i].lastIndexOf("."));
						step2Results[i] = preProcessFile + "\\second_search." + name + ".xml";
						pepIdenResults[i] = new File(step2Results[i]);
					}

					if (parameter.isAppendHostDb()) {

						String temp = parameter.getFastaDir() + "\\" + originalFasta
								.substring(originalFasta.lastIndexOf("\\") + 1, originalFasta.length() - 5) + "temp";

						int proCount = TandemFastaGetter.batchWrite(step2Results, originalFasta, temp, tandemFdr,
								tandemEValue);

						String[] hostDbs = parameter.getHostDBs();

						PrintWriter writer = null;
						try {
							writer = new PrintWriter(ssDatabase);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in writing protein sequence to sample-specific database " + ssDatabase,
									e);
						}
						BufferedReader r0 = null;
						try {
							r0 = new BufferedReader(new FileReader(temp));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}
						String l0 = null;
						try {
							while ((l0 = r0.readLine()) != null) {
								writer.println(l0);
							}
							r0.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in reading file " + temp, e);
						}

						int hostProCount = 0;
						for (String hdb : hostDbs) {
							BufferedReader r1 = null;
							try {
								r1 = new BufferedReader(new FileReader(hdb));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}
							String l1 = null;
							try {
								while ((l1 = r1.readLine()) != null) {
									writer.println(l1);
									if (l1.startsWith(">")) {
										hostProCount++;
									}
								}
								r1.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in reading file " + hdb, e);
							}

						}
						writer.close();
						FileUtils.deleteQuietly(new File(temp));

						logWriter.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + (proCount + hostProCount)
								+ " target protein sequences were included (" + proCount
								+ " protein sequences were identified from the microbiome database, " + hostProCount
								+ " protein sequences were from the host database");

						parameter.setPepIdenResults(pepIdenResults);

					} else {
						int proCount = TandemFastaGetter.batchWrite(step2Results, originalFasta, ssDatabase, tandemFdr,
								tandemEValue);
						logWriter.println(format.format(new Date()) + "\t" + "Sample specific protein database "
								+ ssDatabase + " was created, " + proCount + " target protein sequences were included");

						parameter.setPepIdenResults(pepIdenResults);
					}
				}
			}

			parameter.setPepIdenResultType(MetaSearchEngine.xtandem.getId());

			this.tempLogFileList.add(logfile);
			this.tempLogWriterList.add(logWriter);

			progressBar2.setString(taskName + " finished...");

			if (ssDatabaseFile.exists()) {
				finished = true;
			}

		} finally {
			done();
		}

		return combineLogFile;
	}

	public void done() {

		String[] raws = parameter.getRawFiles()[0];
		File dir = (new File(raws[0])).getParentFile();
		PrintWriter logWriter = null;

		try {
			for (String raw : raws) {
				String name = (new File(raw)).getName();
				name = name.substring(0, name.lastIndexOf("."));
				File mqfile = new File(dir, name);
				if (mqfile.exists()) {
					FileUtils.deleteQuietly(mqfile);
				}
				
				File indexFile = new File(dir, name + ".index");
				if (indexFile.exists()) {
					FileUtils.deleteQuietly(indexFile);
				}
			}

			for (PrintWriter writer : this.tempLogWriterList) {
				writer.close();
			}

			logWriter = new PrintWriter(combineLogFile);
			for (File file : this.tempLogFileList) {
				if (file.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					while ((line = reader.readLine()) != null) {
						logWriter.println(line);
					}
					reader.close();
				}
			}
			logWriter.close();

			for (File file : this.tempLogFileList) {
				if (file.exists()) {
					boolean isFileUnlocked = false;
					try {
						FileUtils.touch(file);
						isFileUnlocked = true;
					} catch (IOException e) {
						isFileUnlocked = false;
					}

					if (isFileUnlocked) {
						FileUtils.deleteQuietly(file);
					}
				}
			}
			
			File tandemInput = new File(this.tandemParHandler.getInputPath());
			if (tandemInput.exists()) {
				boolean isFileUnlocked = false;
				try {
					FileUtils.touch(tandemInput);
					isFileUnlocked = true;
				} catch (IOException e) {
					isFileUnlocked = false;
				}

				if (isFileUnlocked) {
					FileUtils.deleteQuietly(tandemInput);
				}
			}

		} catch (IOException e) {
			LOGGER.error("Error in writing log file to " + combineLogFile, e);
		} finally {
			logWriter.close();
		}
	}

	public boolean isFinished() {
		return finished;
	}

	public int getDBProteinCount() throws IOException {
		File ssDatabase = parameter.getSSDatabase();
		if (ssDatabase.exists()) {

			BufferedReader freader = null;
			try {
				freader = new BufferedReader(new FileReader(ssDatabase));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int proCount = 0;
			String line = null;
			while ((line = freader.readLine()) != null) {
				if (line.startsWith(">")) {
					proCount++;
				}
			}
			freader.close();
			return proCount;
		} else {
			return -1;
		}
	}
}
