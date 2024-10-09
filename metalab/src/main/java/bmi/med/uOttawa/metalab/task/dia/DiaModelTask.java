package bmi.med.uOttawa.metalab.task.dia;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParaIoDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.MagDbConfigIO;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagSqliteTask;

public class DiaModelTask extends MetaLabTask {

	private static String taskName = "Peptide model predict task";
	private static final Logger LOGGER = LogManager.getLogger(DiaModelTask.class);
	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private MagDbItem magDbItem;
	private String filePath;
	private String modelName;
	private String modelType;
	private HashMap<String, Double> pepIntenMap;
	private int trainPepCount = 0;
	private File modelFolderFile;
	private boolean addModel;
	private int threadCount;
	
	public static String RNN = "RNN";
	public static String DFNN = "DFNN";

	public DiaModelTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, String filePath,
			String modelName, String modelType) {
		super(metaPar, advPar, bar);
		// TODO Auto-generated constructor stub
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.filePath = filePath;
		this.modelName = modelName;
		this.modelType = modelType;
		this.addModel = true;
		this.threadCount = metaPar.getThreadCount();
	}
	
	public DiaModelTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, String filePath,
			String modelName, String modelType, File modelFolderFile) {
		super(metaPar, advPar, bar);
		// TODO Auto-generated constructor stub
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.filePath = filePath;
		this.modelName = modelName;
		this.modelType = modelType;
		this.modelFolderFile = modelFolderFile;
		this.addModel = false;
		this.threadCount = metaPar.getThreadCount();
	}
	
	public DiaModelTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, HashMap<String, Double> pepIntenMap,
			String modelName, String modelType) {
		super(metaPar, advPar, bar);
		// TODO Auto-generated constructor stub
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.pepIntenMap = pepIntenMap;
		this.modelName = modelName;
		this.modelType = modelType;
		this.addModel = true;
		this.threadCount = metaPar.getThreadCount();
	}
	
	public DiaModelTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, HashMap<String, Double> pepIntenMap,
			String modelName, String modelType, File modelFolderFile) {
		super(metaPar, advPar, bar);
		// TODO Auto-generated constructor stub
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.pepIntenMap = pepIntenMap;
		this.modelName = modelName;
		this.modelType = modelType;
		this.modelFolderFile = modelFolderFile;
		this.addModel = false;
		this.threadCount = metaPar.getThreadCount();
	}
	
	public DiaModelTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, HashMap<String, Double> pepIntenMap,
			String modelName, String modelType, File modelFolderFile, int threadCount) {
		super(metaPar, advPar, bar);
		// TODO Auto-generated constructor stub
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.pepIntenMap = pepIntenMap;
		this.modelName = modelName;
		this.modelType = modelType;
		this.modelFolderFile = modelFolderFile;
		this.addModel = false;
		this.threadCount = threadCount;
	}

	@Override
	public void forceStop() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info(taskName + ": peptide model prediction started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": peptide model prediction started");

		File currentFile = magDbItem.getCurrentFile();
		File predictPepFile = new File(currentFile, "predicted");
		if (!predictPepFile.exists()) {
			LOGGER.error(taskName + ": no predicted peptide was found from " + predictPepFile
					+ ", please predict the peptide first");
			System.err.println(format.format(new Date()) + "\t" + taskName + ": no predicted peptide was found from "
					+ predictPepFile + ", please predict the peptide first");

			return false;
		}

		File taxDbFile = new File(currentFile, "genomes-all_metadata.tsv");
		if (!taxDbFile.exists()) {
			LOGGER.error(taskName + ": taxonomic information was not found from " + taxDbFile);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": taxonomic information was not found from " + taxDbFile);
			return false;
		}

		File funcFolder = new File(currentFile, "eggNOG");
		if (!funcFolder.exists()) {
			LOGGER.error(taskName + ": functional annotation information was not found from " + funcFolder);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": functional annotation information was not found from " + funcFolder);
			return false;
		}

		File sqlDbFile = new File(currentFile, currentFile.getParentFile().getName() + ".db");

		File totalModelFile = new File(currentFile, "models");
		if (!totalModelFile.exists()) {
			totalModelFile.mkdir();
		}

		if (this.modelFolderFile == null) {
			this.modelFolderFile = new File(totalModelFile, modelName);
		}
		if (!modelFolderFile.exists()) {
			modelFolderFile.mkdir();
		}

		File trainFile = new File(modelFolderFile, "trainData.tsv");
		File datatableFile = new File(modelFolderFile, "predictionData.tsv");

		if (trainFile.exists() && trainFile.length() > 0 && datatableFile.exists() && datatableFile.length() > 0) {
			try (BufferedReader reader = new BufferedReader(new FileReader(trainFile))) {
				reader.readLine();
				while ((reader.readLine()) != null) {
					trainPepCount++;
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading the peptide table " + trainFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading the peptide table "
						+ trainFile);
			}

			LOGGER.info(taskName + ": the data tables already exist, go to the next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": the data tables already exist, go to the next step");
		} else {
			if (sqlDbFile.exists()) {
				trainPepCount = prepareFilesSQL(sqlDbFile, predictPepFile, modelFolderFile);
			} else {
				trainPepCount = prepareFiles(predictPepFile, taxDbFile, funcFolder, modelFolderFile);
			}

			if (trainPepCount > 0) {
				LOGGER.info(taskName + ": prepare the data tables finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": prepare the data tables finished");
			} else {
				LOGGER.error(taskName + ": prepare the data tables failed");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": prepare the data tables failed");
				return false;
			}
		}

		LOGGER.info(taskName + ": build the peptide intensity model started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": build the peptide intensity model started");

		File modelKerasFile = new File(modelFolderFile, modelName + ".keras");
		File modelInfoFile = new File(modelFolderFile, modelName + "_modelInfo.txt");
		File predictDataFile = new File(modelFolderFile, modelName + "_predicted_allData.tsv");

		if (!modelKerasFile.exists() || !modelInfoFile.exists() || !predictDataFile.exists()) {

			LOGGER.info(taskName + ": peptide model prediction by deep learning started");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": peptide model prediction by deep learning started");

			ExecutorService executor = Executors.newFixedThreadPool(1);
			executor.execute(() -> {
				if (trainPepCount > 15000) {
					buildModel(modelFolderFile, ((MetaSourcesDia) msv).getPython(), true);
				} else {
					buildModel(modelFolderFile, ((MetaSourcesDia) msv).getPython(), false);

				}
			});
			executor.shutdown();

			try {
				if (executor.awaitTermination(48, TimeUnit.HOURS)) {
					if (modelKerasFile.exists() && modelInfoFile.exists() && predictDataFile.exists()) {
						LOGGER.info(taskName + ": peptide model prediction by deep learning finished");
						System.out.println(format.format(new Date()) + "\t" + taskName
								+ ": peptide model prediction by deep learning finished");
					} else {
						LOGGER.info(taskName + ": peptide model prediction by deep learning failed");
						System.out.println(format.format(new Date()) + "\t" + taskName
								+ ": peptide model prediction by deep learning failed");
						return false;
					}
				} else {
					LOGGER.error(taskName + ": peptide model prediction didn't finish in a long time");
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": peptide model prediction didn't finish in a long time");
					return false;
				}
			} catch (Exception e) {
				LOGGER.error(taskName + ": error in peptide model prediction by deep learning", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in peptide model prediction by deep learning");
				return false;
			}
		} else {
			LOGGER.info(taskName + ": peptide model already exists in " + modelKerasFile);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": peptide model already exists in "
					+ modelKerasFile);
		}

		LOGGER.info(taskName + ": predict peptide intensity by deep learning model started");
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": predict peptide intensity by deep learning model started");

		File predictRankFile = new File(modelFolderFile, modelName + "_pep_rank.tsv");
		int predictedCount = 0;
		if (!predictRankFile.exists() || predictRankFile.length() < 30) {
			predictedCount = getPepRank(currentFile, modelFolderFile);
		} else {
			try (BufferedReader reader = new BufferedReader(new FileReader(predictDataFile))) {
				reader.readLine();
				while ((reader.readLine()) != null) {
					predictedCount++;
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in get the predicted peptide information from " + predictDataFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in get the predicted peptide information from " + predictDataFile);
			}
		}

		LOGGER.info(taskName + ": calculate peptide rank finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": calculate peptide rank finished");

		double corr = 0.0;

		try (BufferedReader infoReader = new BufferedReader(new FileReader(modelInfoFile))) {
			String line = infoReader.readLine();
			String[] cs = line.split(" ");
			corr = Double.parseDouble(cs[cs.length - 1]);
			infoReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in get the correlation information from " + modelInfoFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in get the correlation information from " + modelInfoFile);
		}

		double medRankScore = 0;

		File infoFile = new File(modelFolderFile, modelName + "_info.txt");
		if (!infoFile.exists()) {
			medRankScore = evalute(modelFolderFile, ((MetaSourcesDia) msv).getPython());
			try (PrintWriter writer = new PrintWriter(infoFile)) {
				writer.println("Train set size:\t" + trainPepCount);
				writer.println("Data set size:\t" + predictedCount);
				writer.println("Correlation:\t" + corr);
				writer.println("Rank Score median:\t" + medRankScore);
				writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing the information to " + infoFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing the information to " + infoFile);
			}
		} else {
			try (BufferedReader infoReader = new BufferedReader(new FileReader(infoFile))) {
				String line = infoReader.readLine();
				while ((line = infoReader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (cs[0].equals("Rank Score median:")) {
						medRankScore = Double.parseDouble(cs[cs.length - 1]);
					}
					break;
				}
				infoReader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in get the correlation information from " + infoFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in get the correlation information from " + infoFile);
			}
		}

		if (addModel) {
			magDbItem.addModel(modelName, trainPepCount, predictedCount, corr, medRankScore);
		}

		LOGGER.info(taskName + ": peptide model prediction finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": peptide model prediction finished");

		return true;
	}

	private int prepareFiles(File predictPepFolder, File taxDbFile, File funcFolder, File modelFolder) {

		LOGGER.info(taskName + ": prepare the data tables based on the text database started");
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": prepare the data tables based on the text database started");

		ConcurrentHashMap<String, HashSet<String>> pepProMap = new ConcurrentHashMap<String, HashSet<String>>();
		
		if (this.pepIntenMap == null) {

			File pepFile = new File(filePath);
			if (filePath != null && filePath.length() > 0 && !pepFile.exists() || pepFile.length() == 0) {
				LOGGER.error(taskName + ": peptide quantification result was not found from " + filePath);
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": peptide quantification result was not found from " + filePath);

				return -1;
			}

			this.pepIntenMap = new HashMap<String, Double>();
			try (BufferedReader reader = new BufferedReader(new FileReader(pepFile))) {
				String pline = reader.readLine();
				String[] title = pline.split("\t");
				int seqid = -1;
				int intensityId = -1;
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Sequence")) {
						seqid = i;
					} else if (title[i].equals("Base sequence")) {
						seqid = i;
					} else if (title[i].equals("Intensity")) {
						intensityId = i;
					}
				}

				if (seqid == -1) {

					LOGGER.error(taskName + ": there is no \"Sequence\" column in the file " + filePath);
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": there is no \"Sequence\" column in the file " + filePath);

					return 0;
				}

				if (intensityId == -1) {

					LOGGER.error(taskName + ": there is no \"Intensity\" column in the file " + filePath);
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": there is no \"Intensity\" column in the file " + filePath);

					return 0;
				}

				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");

					double intensity = Double.parseDouble(cs[intensityId]);
					if (intensity == 0) {
						continue;
					}

					int c1 = 0;
					int c2 = 0;
					boolean start = true;
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < cs[seqid].length(); i++) {
						char aa = cs[seqid].charAt(i);
						if (aa == '(' || aa == '[') {
							start = false;
							c1++;
						} else if (aa == ')' || aa == ']') {
							start = true;
							c2++;
						} else {
							if (start && aa >= 'A' && aa <= 'Z') {
								sb.append(aa);
							}
						}
					}

					if (c1 == c2) {
						String seq = sb.toString();
						if (pepIntenMap.containsKey(seq)) {
							pepIntenMap.put(seq, pepIntenMap.get(seq) + intensity);
						} else {
							pepIntenMap.put(seq, intensity);
							pepProMap.put(seq, new HashSet<String>());
						}
					}
				}
				reader.close();

			} catch (IOException e) {

				LOGGER.error(taskName + ": error in reading " + filePath, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + filePath);

				return 0;
			}

			LOGGER.info(taskName + ": " + pepIntenMap.size() + " peptides were found from " + filePath);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + pepIntenMap.size()
					+ " peptides were found from " + filePath);
		} else {
			for(String pep: this.pepIntenMap.keySet()) {
				pepProMap.put(pep, new HashSet<String>());
			}
			
			LOGGER.info(taskName + ": " + pepIntenMap.size() + " peptides were found");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + pepIntenMap.size()
					+ " peptides were found");
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File[] files = predictPepFolder.listFiles();

		ConcurrentHashMap<String, HashSet<String>> genomePepMap = new ConcurrentHashMap<String, HashSet<String>>();
		ConcurrentHashMap<String, Double> predictPepMap = new ConcurrentHashMap<String, Double>();

		for (File file : files) {
			executor.execute(() -> {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					HashSet<String> pepSet = new HashSet<String>();
					HashMap<String, Double> tempPepScoreMap = new HashMap<String, Double>();
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (pepProMap.containsKey(cs[1])) {
							pepProMap.get(cs[1]).add(cs[0]);
							pepSet.add(cs[1]);
							double score = Double.parseDouble(cs[2]);
							if (tempPepScoreMap.containsKey(cs[1])) {
								if (score > tempPepScoreMap.get(cs[1])) {
									tempPepScoreMap.put(cs[1], score);
								}
							} else {
								tempPepScoreMap.put(cs[1], score);
							}
						}
					}
					reader.close();

					if (pepSet.size() > 0) {
						String preFileName = file.getName();
						String genome = preFileName.substring(0, preFileName.length() - ".predicted.tsv".length());
						genomePepMap.put(genome, pepSet);

						for (String pep : pepSet) {
							if (predictPepMap.containsKey(pep)) {
								if (tempPepScoreMap.get(pep) > predictPepMap.get(pep)) {
									predictPepMap.put(pep, tempPepScoreMap.get(pep));
								}
							} else {
								predictPepMap.put(pep, tempPepScoreMap.get(pep));
							}
						}
					}
				} catch (IOException e) {

					LOGGER.error(taskName + ": error in reading " + filePath, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + filePath);
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {
				LOGGER.info(taskName + ": matching peptides finished, " + predictPepMap.size() + " peptides from "
						+ genomePepMap.size() + " genomes were matched");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": matching peptides finished, "
						+ predictPepMap.size() + " peptides from " + genomePepMap.size() + " genomes were matched");

			} else {
				LOGGER.error(taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");

				return 0;
			}
		} catch (InterruptedException e) {

			LOGGER.error(taskName + ": error in reading predicted peptides", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading predicted peptides");

			return 0;
		}

		HashMap<String, String[]> taxaMap = new HashMap<String, String[]>();
		try (BufferedReader taxaReader = new BufferedReader(new FileReader(taxDbFile))) {
			String line = taxaReader.readLine();
			String[] title = line.split("\t");
			int lineageId = -1;
			int genomeId = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Lineage") || title[i].startsWith("Species_designation")) {
					lineageId = i;
				} else if (title[i].startsWith("Species_rep") || title[i].startsWith("Species_representative")) {
					genomeId = i;
				}
			}

			if (lineageId == -1 || genomeId == -1) {

				LOGGER.error(taskName + ": error in reading " + taxDbFile + ", the title format is unknown");
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxDbFile
						+ ", the title format is unknown");

				return 0;
			}

			while ((line = taxaReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] lineange = cs[lineageId].split(";");
				String[] taxaString = new String[7];
				for (int i = 0; i < taxaString.length; i++) {
					if (i < lineange.length) {
						if (lineange[i].endsWith("__")) {
							taxaString[i] = "";
						} else {
							taxaString[i] = lineange[i];
						}

					} else {
						taxaString[i] = "";
					}
				}
				taxaMap.put(cs[genomeId], taxaString);
			}
			taxaReader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading " + taxDbFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxDbFile);

			return 0;
		}

		LOGGER.info(taskName + ": " + taxaMap.size() + " genome were matched to taxa from " + taxDbFile);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + taxaMap.size()
				+ " genome were matched to taxa from " + taxDbFile);

		HashMap<String, Double> proIntenMap = new HashMap<String, Double>();
		HashMap<String, Double> genomeIntenMap = new HashMap<String, Double>();
		HashMap<String, Double> taxaIntenMap = new HashMap<String, Double>();
		HashMap<String, HashMap<String, Double>> genomeProMap = new HashMap<String, HashMap<String, Double>>();

		File pepIntenFile = new File(modelFolder, "pep_intensity.tsv");
		File proIntenFile = new File(modelFolder, "pro_intensity.tsv");
		File genomeIntenFile = new File(modelFolder, "genome_intensity.tsv");
		File taxonIntenFile = new File(modelFolder, "taxa_intensity.tsv");

		if (pepIntenFile.exists() && pepIntenFile.length() > 0 && proIntenFile.exists() && proIntenFile.length() > 0
				&& genomeIntenFile.exists() && genomeIntenFile.length() > 0 && taxonIntenFile.exists()
				&& taxonIntenFile.length() > 0) {

			try (BufferedReader reader = new BufferedReader(new FileReader(proIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					proIntenMap.put(cs[0], Double.parseDouble(cs[1]));
					String genome = cs[0].substring(0, cs[0].indexOf("_"));
					if (genomeProMap.containsKey(genome)) {
						genomeProMap.get(genome).put(cs[0], Double.parseDouble(cs[1]));
					} else {
						HashMap<String, Double> proMap = new HashMap<String, Double>();
						proMap.put(cs[0], Double.parseDouble(cs[1]));
						genomeProMap.put(genome, proMap);
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + proIntenFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + proIntenFile);

				return 0;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(genomeIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					genomeIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + genomeIntenFile, e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + genomeIntenFile);

				return 0;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(taxonIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					taxaIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + taxonIntenFile, e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxonIntenFile);

				return 0;
			}

		} else {

			try (PrintWriter writer = new PrintWriter(pepIntenFile)) {
				writer.println("Peptide\tIntensity");
				for (String pep : pepIntenMap.keySet()) {

					double intensity = pepIntenMap.get(pep);
					writer.println(pep + "\t" + intensity);

					HashSet<String> proSet = pepProMap.get(pep);

					if (proSet.size() > 0) {
						double shareIntensity = intensity / (double) proSet.size();

						for (String proi : proSet) {
							if (proIntenMap.containsKey(proi)) {
								proIntenMap.put(proi, proIntenMap.get(proi) + shareIntensity);
							} else {
								proIntenMap.put(proi, shareIntensity);
							}

							String genome = proi.substring(0, proi.indexOf("_"));
							if (genomeIntenMap.containsKey(genome)) {
								genomeIntenMap.put(genome, genomeIntenMap.get(genome) + shareIntensity);
							} else {
								genomeIntenMap.put(genome, shareIntensity);
							}

							String[] taxa = taxaMap.get(genome);
							for (int j = 0; j < taxa.length; j++) {
								if (taxa[j].length() > 0) {
									if (taxaIntenMap.containsKey(taxa[j])) {
										taxaIntenMap.put(taxa[j], taxaIntenMap.get(taxa[j]) + shareIntensity);
									} else {
										taxaIntenMap.put(taxa[j], shareIntensity);
									}
								}
							}

							HashMap<String, Double> gproMap;
							if (genomeProMap.containsKey(genome)) {
								gproMap = genomeProMap.get(genome);
							} else {
								gproMap = new HashMap<String, Double>();
								genomeProMap.put(genome, gproMap);
							}
							if (gproMap.containsKey(proi)) {
								gproMap.put(proi, gproMap.get(proi) + shareIntensity);
							} else {
								gproMap.put(proi, shareIntensity);
							}
						}
					}
				}
				writer.close();
			} catch (Exception e) {
				LOGGER.error(taskName + ": error in writing peptide intensities", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing peptide intensities");
				return 0;
			}

			try (PrintWriter proWriter = new PrintWriter(proIntenFile)) {
				proWriter.println("Protein\tIntensity");
				for (String pro : proIntenMap.keySet()) {
					proWriter.println(pro + "\t" + proIntenMap.get(pro));
				}
				proWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing protein intensities", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing protein intensities");
				return 0;
			}

			try (PrintWriter genomeWriter = new PrintWriter(genomeIntenFile)) {
				genomeWriter.println("Genome\tIntensity");
				for (String genome : genomeIntenMap.keySet()) {
					genomeWriter.println(genome + "\t" + genomeIntenMap.get(genome));
				}
				genomeWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing genome intensities", e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in writing genome intensities");
				return 0;
			}

			try (PrintWriter taxaWriter = new PrintWriter(taxonIntenFile)) {
				taxaWriter.println("Taxa\tIntensity");
				for (String taxon : taxaIntenMap.keySet()) {
					taxaWriter.println(taxon + "\t" + taxaIntenMap.get(taxon));
				}
				taxaWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing taxon intensities", e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in writing taxon intensities");
				return 0;
			}
		}

		ConcurrentHashMap<String, Double> funIntenMap = new ConcurrentHashMap<String, Double>();
		ConcurrentHashMap<String, String> proFuncMap = new ConcurrentHashMap<String, String>();
		executor = Executors.newFixedThreadPool(threadCount);
		for (String genome : genomePepMap.keySet()) {
			File file0 = new File(funcFolder, genome + "_eggNOG.tsv");
			File file1 = new File(funcFolder, genome + ".emapper.annotations");
			File file = file0.exists() ? file0 : file1;
			executor.execute(() -> {
				HashMap<String, Double> funcMap = new HashMap<String, Double>();
				HashMap<String, Double> proMap = genomeProMap.get(genome);

				HashMap<String, String> tempProFuncMap = new HashMap<String, String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String pline = null;
					while ((pline = reader.readLine()) != null) {
						if (pline.startsWith("#query")) {
							break;
						}
					}
					String[] title = pline.split("\t");
					int eggNogId = -1;
					for (int i = 0; i < title.length; i++) {
						if (title[i].equals("eggNOG_OGs")) {
							eggNogId = i;
						}
					}

					if (eggNogId == -1) {
						LOGGER.error(taskName + ": error in reading " + file + ", the title format is unknown");
						System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + file
								+ ", the title format is unknown");
					}

					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (proMap.containsKey(cs[0])) {
							double value = proMap.get(cs[0]);
							String[] funcs = cs[eggNogId].split(",");
							for (int i = 0; i < funcs.length; i++) {
								if (funcMap.containsKey(funcs[i])) {
									funcMap.put(funcs[i], funcMap.get(funcs[i]) + value);
								} else {
									funcMap.put(funcs[i], value);
								}
							}
							tempProFuncMap.put(cs[0], cs[4]);
						}
					}
					reader.close();
				} catch (IOException e) {
					LOGGER.error(taskName + ": error in reading " + file, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + file);
				}

				for (String func : funcMap.keySet()) {
					if (funIntenMap.containsKey(func)) {
						funIntenMap.put(func, funIntenMap.get(func) + funcMap.get(func));
					} else {
						funIntenMap.put(func, funcMap.get(func));
					}
				}
				proFuncMap.putAll(tempProFuncMap);
			});
		}
		executor.shutdown();

		try {
			if (executor.awaitTermination(10, TimeUnit.HOURS)) {
				LOGGER.info(taskName + ": " + funIntenMap.size() + " functions were found");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + funIntenMap.size()
						+ " functions were found");
			} else {
				return 0;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block

			LOGGER.error(taskName + ": error in matching functions", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in matching functions");

			return 0;
		}

		try (PrintWriter funcWriter = new PrintWriter(new File(modelFolder, "func_intensity.tsv"));) {
			funcWriter.println("Function\tIntensity");
			for (String func : funIntenMap.keySet()) {
				funcWriter.println(func + "\t" + funIntenMap.get(func));
			}
			funcWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing function intentisies", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing function intentisies");
			return 0;
		}

		try (PrintWriter genomeTaxaWriter = new PrintWriter(new File(modelFolder, "genome_taxa_intensity.tsv"));) {
			for (String genome : genomePepMap.keySet()) {
				StringBuilder sb = new StringBuilder(genome);
				String[] taxaString = taxaMap.get(genome);
				for (int i = 0; i < taxaString.length; i++) {
					if (taxaString[i].length() > 0 && taxaIntenMap.containsKey(taxaString[i])) {
						sb.append("\t").append(taxaIntenMap.get(taxaString[i]));
					} else {
						sb.append("\t").append(genomeIntenMap.get(genome));
					}
				}
				genomeTaxaWriter.println(sb);
			}
			genomeTaxaWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing genome taxa intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing genome taxa intentisies");
			return 0;
		}

		File proFuncIntenFile = new File(modelFolder, "pro_func_intensity.tsv");
		if (!proFuncIntenFile.exists()) {
			try (PrintWriter proFuncWriter = new PrintWriter(proFuncIntenFile)) {
				for (String pro : proFuncMap.keySet()) {
					StringBuilder sb = new StringBuilder(pro);
					String[] funcStrings = proFuncMap.get(pro).split(",");
					int func3BeginId = -1;
					for (int i = 0; i < funcStrings.length; i++) {
						if (funcStrings[i].endsWith("root")) {

						} else if (funcStrings[i].endsWith("Eukaryota")) {

						} else if (funcStrings[i].endsWith("Viruses")) {

						} else if (funcStrings[i].endsWith("Archaea")) {

						} else if (funcStrings[i].endsWith("Bacteria")) {

						} else {
							if (func3BeginId == -1) {
								func3BeginId = i;
							}
						}
					}

					double[] values = new double[5];

					for (int i = 0; i < funcStrings.length; i++) {

						int funcLevel = -1;
						if (func3BeginId == -1) {
							if (funcStrings[i].endsWith("root")) {
								funcLevel = 0;
							} else {
								funcLevel = 1;
							}
						} else {
							if (i == func3BeginId) {
								funcLevel = 2;
							} else if (i == func3BeginId + 1) {
								funcLevel = 3;
							} else if (i == func3BeginId + 2) {
								funcLevel = 4;
							} else {
								if (funcStrings[i].endsWith("root")) {
									funcLevel = 0;
								} else {
									funcLevel = 1;
								}
							}
						}

						if (funIntenMap.containsKey(funcStrings[i])) {
							values[funcLevel] += funIntenMap.get(funcStrings[i]);
						}
					}

					boolean use = true;
					for (int i = 0; i < values.length; i++) {
						if (values[i] > 0) {
							sb.append("\t").append(values[i]);
						} else {
							if (proIntenMap.containsKey(pro)) {
								sb.append("\t").append(proIntenMap.get(pro));
							} else {
								use = false;
							}
						}
					}
					if (use) {
						proFuncWriter.println(sb);
					}
				}
				proFuncWriter.close();

			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing protein function intentisies", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing protein function intentisies");
				return 0;
			}
		}

		HashMap<String, double[]> profuncIntenMap = new HashMap<String, double[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(proFuncIntenFile))) {
			String pline = null;
			while ((pline = reader.readLine()) != null) {
				String[] cs = pline.split("\t");
				double[] values = new double[cs.length - 1];
				for (int i = 0; i < values.length; i++) {
					values[i] = Double.parseDouble(cs[i + 1]);
				}
				profuncIntenMap.put(cs[0], values);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading protein function intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading protein function intentisies");
			return 0;
		}

		LOGGER.info(taskName + ": " + profuncIntenMap.size() + " proteins and functions were matched");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + profuncIntenMap.size()
				+ " functions were matched");

		HashMap<String, double[]> genomeTaxaIntenMap = new HashMap<String, double[]>();
		try (BufferedReader reader = new BufferedReader(
				new FileReader(new File(modelFolder, "genome_taxa_intensity.tsv")))) {
			String pline = null;
			while ((pline = reader.readLine()) != null) {
				String[] cs = pline.split("\t");
				double[] values = new double[cs.length - 1];
				for (int i = 0; i < values.length; i++) {
					values[i] = Double.parseDouble(cs[i + 1]);
				}
				genomeTaxaIntenMap.put(cs[0], values);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading genome taxon intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading genome taxon intentisies");
			return 0;
		}
		LOGGER.info(taskName + ": " + genomeTaxaIntenMap.size() + " genomes and taxa were matched");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + genomeTaxaIntenMap.size()
				+ " genomes and taxa were matched");

		DecimalFormat df4 = FormatTool.getDF4();
		int peptideCount = 0;
		try (PrintWriter tableWriter = new PrintWriter(new File(modelFolder, "trainData.tsv"))) {
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("peptide\t");
			titlesb.append("intensity\t");
			titlesb.append("detectability\t");
			titlesb.append("protein\t");
			titlesb.append("genome");
			for (int i = 1; i <= 7; i++) {
				titlesb.append("\t").append("taxa").append(i);
			}
			for (int i = 1; i <= 5; i++) {
				titlesb.append("\t").append("func").append(i);
			}
			tableWriter.println(titlesb);

			for (String pep : pepIntenMap.keySet()) {
				StringBuilder sb = new StringBuilder(pep);

				if (predictPepMap.containsKey(pep)) {
					double intensity = pepIntenMap.get(pep);
					if (intensity > 0) {
						sb.append("\t").append(df4.format(Math.log(intensity)));
					} else {
						sb.append("\t0.0");
					}

					double predicted = predictPepMap.get(pep);
					sb.append("\t").append(df4.format(predicted));
				} else {
					continue;
				}

				double[] taxaValues = new double[7];
				double[] funcValues = new double[5];
				double proValue = 0;
				double genomeValue = 0;

				HashSet<String> proSet = pepProMap.get(pep);

				for (String pro : proSet) {
					String genome = pro.substring(0, pro.indexOf("_"));
					if (proIntenMap.containsKey(pro)) {
						proValue += proIntenMap.get(pro);
					}
					if (genomeIntenMap.containsKey(genome)) {
						genomeValue += genomeIntenMap.get(genome);
					}
					if (genomeTaxaIntenMap.containsKey(genome)) {
						double[] taxai = genomeTaxaIntenMap.get(genome);
						for (int j = 0; j < taxaValues.length; j++) {
							taxaValues[j] += taxai[j];
						}
					}
					if (profuncIntenMap.containsKey(pro)) {
						double[] funci = profuncIntenMap.get(pro);
						for (int j = 0; j < funcValues.length; j++) {
							funcValues[j] += funci[j];
						}
					}
				}

				if (proValue > 0) {
					sb.append("\t").append(df4.format(Math.log(proValue)));
				} else {
					continue;
				}

				if (genomeValue > 0) {
					sb.append("\t").append(df4.format(Math.log(genomeValue)));
				} else {
					continue;
				}

				for (int i = 0; i < taxaValues.length; i++) {
					if (taxaValues[i] > 0) {
						sb.append("\t").append(df4.format(Math.log(taxaValues[i])));
					} else {
						sb.append("\t").append(df4.format(Math.log(genomeValue)));
					}
				}

				for (int i = 0; i < funcValues.length; i++) {
					if (funcValues[i] > 0) {
						sb.append("\t").append(df4.format(Math.log(funcValues[i])));
					} else {
						sb.append("\t").append(df4.format(Math.log(proValue)));
					}
				}
				tableWriter.println(sb);
				peptideCount++;
			}
			tableWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide table", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing the peptide table");
			return 0;
		}

		File datatableFile = new File(modelFolder, "predictionData.tsv");
		if (!datatableFile.exists() || datatableFile.length() == 0) {

			LOGGER.info(taskName + ": writing all peptide candidate data table started");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": writing all peptide candidate data table started");

			ConcurrentHashMap<String, double[]> datatableMap = new ConcurrentHashMap<String, double[]>();
			executor = Executors.newFixedThreadPool(threadCount);

			for (String genome : genomeIntenMap.keySet()) {
				File predictedFile = new File(predictPepFolder, genome + ".predicted.tsv");
				executor.execute(() -> {
					try (BufferedReader reader = new BufferedReader(new FileReader(predictedFile))) {
						String pline = reader.readLine();
						HashMap<String, double[]> tempDatatableMap = new HashMap<String, double[]>();
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							double predicted = Double.parseDouble(cs[2]);
							if (tempDatatableMap.containsKey(cs[1])) {

								if (!proIntenMap.containsKey(cs[0]) || !profuncIntenMap.containsKey(cs[0])) {
									continue;
								}
								double genomeInten = genomeIntenMap.get(genome);
								double proInten = proIntenMap.get(cs[0]);

								double[] values = tempDatatableMap.get(cs[1]);
								if (predicted > values[0]) {
									values[0] = predicted;
								}
								values[1] += genomeInten;
								values[2] += proInten;

								double[] taxaIntensity = genomeTaxaIntenMap.get(genome);
								for (int i = 0; i < 7; i++) {
									if (taxaIntensity[i] > 0) {
										values[i + 3] = taxaIntensity[i];
									} else {
										values[i + 3] = genomeInten;
									}
								}

								double[] proFuncIntensity = profuncIntenMap.get(cs[0]);
								for (int i = 0; i < 5; i++) {
									if (proFuncIntensity[i] > 0) {
										values[i + 10] += proFuncIntensity[i];
									} else {
										values[i + 10] += proInten;
									}
								}

								tempDatatableMap.put(cs[1], values);
							} else {

								if (!proIntenMap.containsKey(cs[0]) || !profuncIntenMap.containsKey(cs[0])) {
									continue;
								}

								double genomeInten = genomeIntenMap.get(genome);
								double proInten = proIntenMap.get(cs[0]);

								double[] values = new double[15];
								values[0] = predicted;
								values[1] = genomeInten;
								values[2] = proInten;

								double[] taxaIntensity = genomeTaxaIntenMap.get(genome);
								for (int i = 0; i < 7; i++) {
									if (taxaIntensity[i] > 0) {
										values[i + 3] = taxaIntensity[i];
									} else {
										values[i + 3] = genomeInten;
									}
								}

								double[] proFuncIntensity = profuncIntenMap.get(cs[0]);
								for (int i = 0; i < 5; i++) {
									if (proFuncIntensity[i] > 0) {
										values[i + 10] = proFuncIntensity[i];
									} else {
										values[i + 10] = proInten;
									}
								}

								tempDatatableMap.put(cs[1], values);
							}
						}
						reader.close();

						for (String pep : tempDatatableMap.keySet()) {
							double[] tempValue = tempDatatableMap.get(pep);
							if (datatableMap.containsKey(pep)) {
								double[] values = datatableMap.get(pep);
								if (tempValue[0] > values[0]) {
									values[0] = tempValue[0];
								}
								for (int i = 1; i < values.length; i++) {
									values[i] += tempValue[i];
								}
							} else {
								datatableMap.put(pep, tempValue);
							}
						}

					} catch (IOException e) {
						LOGGER.error(taskName + ": error in reading the peptide detectability information from "
								+ predictedFile, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in reading the peptide detectability information from " + predictedFile);
					}
				});
			}
			executor.shutdown();

			try {
				if (executor.awaitTermination(10, TimeUnit.HOURS)) {
					PrintWriter tableWriter = new PrintWriter(datatableFile);
					StringBuilder titlesb = new StringBuilder();
					titlesb.append("peptide\t");
					titlesb.append("detectability\t");
					titlesb.append("protein\t");
					titlesb.append("genome");

					for (int i = 1; i <= 7; i++) {
						titlesb.append("\t").append("taxa").append(i);
					}
					for (int i = 1; i <= 5; i++) {
						titlesb.append("\t").append("func").append(i);
					}
					tableWriter.println(titlesb);
					for (String pep : datatableMap.keySet()) {
						double[] values = datatableMap.get(pep);
						StringBuilder sb = new StringBuilder(pep);
						sb.append("\t").append(df4.format(values[0]));
						for (int i = 1; i < values.length; i++) {
							if (values[i] > 0) {
								sb.append("\t").append(df4.format(Math.log(values[i])));
							} else {
								sb.append("\t0.0");
							}
						}
						tableWriter.println(sb);
					}
					tableWriter.close();
				} else {

					LOGGER.info(taskName + ": writing all peptide candidate data table didn't finish in a long time");
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": writing all peptide candidate data table didn't finish in a long time");

					return 0;
				}
			} catch (InterruptedException|IOException e) {
				// TODO Auto-generated catch block

				LOGGER.error(taskName + ": error in matching functions", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in matching functions");

				return 0;
			}
		}

		return peptideCount;
	}
	
	private int prepareFilesSQL(File sqlDbFile, File predictPepFolder, File modelFolder) {

		LOGGER.info(taskName + ": prepare the data tables for " + modelFolder.getName()
				+ " based on the SQL database started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": prepare the data tables for "
				+ modelFolder.getName() + " based on the SQL database started");

		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();

		if (this.pepIntenMap == null) {

			File pepFile = new File(filePath);
			if (filePath != null && filePath.length() > 0 && !pepFile.exists() || pepFile.length() == 0) {
				LOGGER.error(taskName + ": peptide quantification result was not found from " + filePath);
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": peptide quantification result was not found from " + filePath);

				return -1;
			}

			this.pepIntenMap = new HashMap<String, Double>();

			try (BufferedReader reader = new BufferedReader(new FileReader(pepFile))) {
				String pline = reader.readLine();
				String[] title = pline.split("\t");
				int seqid = -1;
				int intensityId = -1;
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Sequence")) {
						seqid = i;
					} else if (title[i].equals("Base sequence")) {
						seqid = i;
					} else if (title[i].equals("Intensity")) {
						intensityId = i;
					}
				}

				if (seqid == -1) {

					LOGGER.error(taskName + ": there is no \"Sequence\" column in the file " + filePath);
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": there is no \"Sequence\" column in the file " + filePath);

					return 0;
				}

				if (intensityId == -1) {

					LOGGER.error(taskName + ": there is no \"Intensity\" column in the file " + filePath);
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": there is no \"Intensity\" column in the file " + filePath);

					return 0;
				}

				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");

					double intensity = Double.parseDouble(cs[intensityId]);
					if (intensity == 0) {
						continue;
					}

					int c1 = 0;
					int c2 = 0;
					boolean start = true;
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < cs[seqid].length(); i++) {
						char aa = cs[seqid].charAt(i);
						if (aa == '(' || aa == '[') {
							start = false;
							c1++;
						} else if (aa == ')' || aa == ']') {
							start = true;
							c2++;
						} else {
							if (start && aa >= 'A' && aa <= 'Z') {
								sb.append(aa);
							}
						}
					}

					if (c1 == c2) {
						String seq = sb.toString();
						if (pepIntenMap.containsKey(seq)) {
							pepIntenMap.put(seq, pepIntenMap.get(seq) + intensity);
						} else {
							pepIntenMap.put(seq, intensity);
							pepProMap.put(seq, new HashSet<String>());
						}
					}
				}
				reader.close();

			} catch (IOException e) {

				LOGGER.error(taskName + ": error in reading " + filePath, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + filePath);

				return 0;
			}

			LOGGER.info(taskName + ": " + pepIntenMap.size() + " peptides were found from " + filePath);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + pepIntenMap.size()
					+ " peptides were found from " + filePath);

		} else {
			for (String pep : pepIntenMap.keySet()) {
				pepProMap.put(pep, new HashSet<String>());
			}

			LOGGER.info(taskName + ": " + pepIntenMap.size() + " peptides were found for " + modelFolder.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + pepIntenMap.size()
					+ " peptides were found for " + modelFolder.getName());
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File[] files = predictPepFolder.listFiles();

		ConcurrentHashMap<String, HashSet<String>> genomePepMap = new ConcurrentHashMap<String, HashSet<String>>();
		ConcurrentHashMap<String, Double> predictPepMap = new ConcurrentHashMap<String, Double>();

		for (File file : files) {
			executor.execute(() -> {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					HashSet<String> pepSet = new HashSet<String>();
					HashMap<String, Double> tempPepScoreMap = new HashMap<String, Double>();
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (pepProMap.containsKey(cs[1])) {
							pepProMap.get(cs[1]).add(cs[0]);
							pepSet.add(cs[1]);
							double score = Double.parseDouble(cs[2]);
							if (tempPepScoreMap.containsKey(cs[1])) {
								if (score > tempPepScoreMap.get(cs[1])) {
									tempPepScoreMap.put(cs[1], score);
								}
							} else {
								tempPepScoreMap.put(cs[1], score);
							}
						}
					}
					reader.close();

					if (pepSet.size() > 0) {
						String preFileName = file.getName();
						String genome = preFileName.substring(0, preFileName.length() - ".predicted.tsv".length());
						genomePepMap.put(genome, pepSet);

						for (String pep : pepSet) {
							if (predictPepMap.containsKey(pep)) {
								if (tempPepScoreMap.get(pep) > predictPepMap.get(pep)) {
									predictPepMap.put(pep, tempPepScoreMap.get(pep));
								}
							} else {
								predictPepMap.put(pep, tempPepScoreMap.get(pep));
							}
						}
					}
				} catch (IOException e) {

					LOGGER.error(taskName + ": error in reading " + filePath, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + filePath);
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(12, TimeUnit.HOURS)) {
				LOGGER.info(taskName + ": matching peptides finished, " + predictPepMap.size() + " peptides from "
						+ genomePepMap.size() + " genomes were matched for " + modelFolder.getName());
				System.out.println(format.format(new Date()) + "\t" + taskName + ": matching peptides finished, "
						+ predictPepMap.size() + " peptides from " + genomePepMap.size() + " genomes were matched for "
						+ modelFolder.getName());

			} else {
				LOGGER.error(taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");

				return 0;
			}
		} catch (InterruptedException e) {

			LOGGER.error(taskName + ": error in reading predicted peptides", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading predicted peptides");

			return 0;
		}

		HashMap<String, String[]> taxaMap = new HashMap<String, String[]>();
		HashSet<String> genomeSet = new HashSet<String>(genomePepMap.keySet());
		String[][] genomeContents = MagSqliteTask.queryGenome(genomeSet, sqlDbFile);
		for (int i = 0; i < genomeContents.length; i++) {
			String[] taxaString = new String[7];
			System.arraycopy(genomeContents[i], 1, taxaString, 0, taxaString.length);
			taxaMap.put(genomeContents[i][0], taxaString);
		}

		LOGGER.info(taskName + ": " + taxaMap.size() + " genome were matched to taxa");
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": " + taxaMap.size() + " genome were matched to taxa");

		HashMap<String, Double> proIntenMap = new HashMap<String, Double>();
		HashMap<String, Double> genomeIntenMap = new HashMap<String, Double>();
		HashMap<String, Double> taxaIntenMap = new HashMap<String, Double>();

		File pepIntenFile = new File(modelFolder, "pep_intensity.tsv");
		File proIntenFile = new File(modelFolder, "pro_intensity.tsv");
		File genomeIntenFile = new File(modelFolder, "genome_intensity.tsv");
		File taxonIntenFile = new File(modelFolder, "taxa_intensity.tsv");

		if (pepIntenFile.exists() && pepIntenFile.length() > 0 && proIntenFile.exists() && proIntenFile.length() > 0
				&& genomeIntenFile.exists() && genomeIntenFile.length() > 0 && taxonIntenFile.exists()
				&& taxonIntenFile.length() > 0) {

			try (BufferedReader reader = new BufferedReader(new FileReader(proIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					proIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + proIntenFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + proIntenFile);

				return 0;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(genomeIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					genomeIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + genomeIntenFile, e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + genomeIntenFile);

				return 0;
			}

			try (BufferedReader reader = new BufferedReader(new FileReader(taxonIntenFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					taxaIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + taxonIntenFile, e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxonIntenFile);

				return 0;
			}

		} else {

			try (PrintWriter writer = new PrintWriter(pepIntenFile)) {
				writer.println("Peptide\tIntensity");
				for (String pep : pepIntenMap.keySet()) {

					double intensity = pepIntenMap.get(pep);
					writer.println(pep + "\t" + intensity);

					HashSet<String> proSet = pepProMap.get(pep);

					if (proSet.size() > 0) {
						double shareIntensity = intensity / (double) proSet.size();

						for (String proi : proSet) {
							if (proIntenMap.containsKey(proi)) {
								proIntenMap.put(proi, proIntenMap.get(proi) + shareIntensity);
							} else {
								proIntenMap.put(proi, shareIntensity);
							}

							String genome = proi.substring(0, proi.indexOf("_"));
							if (genomeIntenMap.containsKey(genome)) {
								genomeIntenMap.put(genome, genomeIntenMap.get(genome) + shareIntensity);
							} else {
								genomeIntenMap.put(genome, shareIntensity);
							}

							String[] taxa = taxaMap.get(genome);
							for (int j = 0; j < taxa.length; j++) {
								if (taxa[j].length() > 0) {
									if (taxaIntenMap.containsKey(taxa[j])) {
										taxaIntenMap.put(taxa[j], taxaIntenMap.get(taxa[j]) + shareIntensity);
									} else {
										taxaIntenMap.put(taxa[j], shareIntensity);
									}
								}
							}
						}
					}
				}
				writer.close();
			} catch (Exception e) {
				LOGGER.error(taskName + ": error in writing peptide intensities", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing peptide intensities");
				return 0;
			}

			try (PrintWriter proWriter = new PrintWriter(proIntenFile)) {
				proWriter.println("Protein\tIntensity");
				for (String pro : proIntenMap.keySet()) {
					proWriter.println(pro + "\t" + proIntenMap.get(pro));
				}
				proWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing protein intensities", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing protein intensities");
				return 0;
			}

			try (PrintWriter genomeWriter = new PrintWriter(genomeIntenFile)) {
				genomeWriter.println("Genome\tIntensity");
				for (String genome : genomeIntenMap.keySet()) {
					genomeWriter.println(genome + "\t" + genomeIntenMap.get(genome));
				}
				genomeWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing genome intensities", e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in writing genome intensities");
				return 0;
			}

			try (PrintWriter taxaWriter = new PrintWriter(taxonIntenFile)) {
				taxaWriter.println("Taxa\tIntensity");
				for (String taxon : taxaIntenMap.keySet()) {
					taxaWriter.println(taxon + "\t" + taxaIntenMap.get(taxon));
				}
				taxaWriter.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing taxon intensities", e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in writing taxon intensities");
				return 0;
			}
		}

		HashSet<String> allProSet = new HashSet<String>(proIntenMap.keySet());
		HashMap<String, Double> funIntenMap = new HashMap<String, Double>();
		ConcurrentHashMap<String, String> proFuncMap = MagSqliteTask.queryProteinEggnog(allProSet, sqlDbFile,
				threadCount);
		for (String pro : proFuncMap.keySet()) {
			double intensity = proIntenMap.get(pro);
			String[] funcs = proFuncMap.get(pro).split(",");
			for (int j = 0; j < funcs.length; j++) {
				if (funIntenMap.containsKey(funcs[j])) {
					funIntenMap.put(funcs[j], funIntenMap.get(funcs[j]) + intensity);
				} else {
					funIntenMap.put(funcs[j], intensity);
				}
			}
		}

		try (PrintWriter funcWriter = new PrintWriter(new File(modelFolder, "func_intensity.tsv"));) {
			funcWriter.println("Function\tIntensity");
			for (String func : funIntenMap.keySet()) {
				funcWriter.println(func + "\t" + funIntenMap.get(func));
			}
			funcWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing function intentisies", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing function intentisies");
			return 0;
		}

		try (PrintWriter genomeTaxaWriter = new PrintWriter(new File(modelFolder, "genome_taxa_intensity.tsv"));) {
			for (String genome : genomePepMap.keySet()) {
				StringBuilder sb = new StringBuilder(genome);
				String[] taxaString = taxaMap.get(genome);
				for (int i = 0; i < taxaString.length; i++) {
					if (taxaString[i].length() > 0 && taxaIntenMap.containsKey(taxaString[i])) {
						sb.append("\t").append(taxaIntenMap.get(taxaString[i]));
					} else {
						sb.append("\t").append(genomeIntenMap.get(genome));
					}
				}
				genomeTaxaWriter.println(sb);
			}
			genomeTaxaWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing genome taxa intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing genome taxa intentisies");
			return 0;
		}

		File proFuncIntenFile = new File(modelFolder, "pro_func_intensity.tsv");
		if (!proFuncIntenFile.exists()) {
			try (PrintWriter proFuncWriter = new PrintWriter(proFuncIntenFile)) {
				for (String pro : proFuncMap.keySet()) {
					StringBuilder sb = new StringBuilder(pro);
					String[] funcStrings = proFuncMap.get(pro).split(",");
					int func3BeginId = -1;
					for (int i = 0; i < funcStrings.length; i++) {
						if (funcStrings[i].endsWith("root")) {

						} else if (funcStrings[i].endsWith("Eukaryota")) {

						} else if (funcStrings[i].endsWith("Viruses")) {

						} else if (funcStrings[i].endsWith("Archaea")) {

						} else if (funcStrings[i].endsWith("Bacteria")) {

						} else {
							if (func3BeginId == -1) {
								func3BeginId = i;
							}
						}
					}

					double[] values = new double[5];

					for (int i = 0; i < funcStrings.length; i++) {

						int funcLevel = -1;
						if (func3BeginId == -1) {
							if (funcStrings[i].endsWith("root")) {
								funcLevel = 0;
							} else {
								funcLevel = 1;
							}
						} else {
							if (i == func3BeginId) {
								funcLevel = 2;
							} else if (i == func3BeginId + 1) {
								funcLevel = 3;
							} else if (i == func3BeginId + 2) {
								funcLevel = 4;
							} else {
								if (funcStrings[i].endsWith("root")) {
									funcLevel = 0;
								} else {
									funcLevel = 1;
								}
							}
						}

						if (funIntenMap.containsKey(funcStrings[i])) {
							values[funcLevel] += funIntenMap.get(funcStrings[i]);
						}
					}

					boolean use = true;
					for (int i = 0; i < values.length; i++) {
						if (values[i] > 0) {
							sb.append("\t").append(values[i]);
						} else {
							if (proIntenMap.containsKey(pro)) {
								sb.append("\t").append(proIntenMap.get(pro));
							} else {
								use = false;
							}
						}
					}
					if (use) {
						proFuncWriter.println(sb);
					}
				}
				proFuncWriter.close();

			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing protein function intentisies", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing protein function intentisies");
				return 0;
			}
		}

		HashMap<String, double[]> profuncIntenMap = new HashMap<String, double[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(proFuncIntenFile))) {
			String pline = null;
			while ((pline = reader.readLine()) != null) {
				String[] cs = pline.split("\t");
				double[] values = new double[cs.length - 1];
				for (int i = 0; i < values.length; i++) {
					values[i] = Double.parseDouble(cs[i + 1]);
				}
				profuncIntenMap.put(cs[0], values);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading protein function intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading protein function intentisies");
			return 0;
		}

		LOGGER.info(taskName + ": " + profuncIntenMap.size() + " proteins and functions were matched");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + profuncIntenMap.size()
				+ " functions were matched");

		HashMap<String, double[]> genomeTaxaIntenMap = new HashMap<String, double[]>();
		try (BufferedReader reader = new BufferedReader(
				new FileReader(new File(modelFolder, "genome_taxa_intensity.tsv")))) {
			String pline = null;
			while ((pline = reader.readLine()) != null) {
				String[] cs = pline.split("\t");
				double[] values = new double[cs.length - 1];
				for (int i = 0; i < values.length; i++) {
					values[i] = Double.parseDouble(cs[i + 1]);
				}
				genomeTaxaIntenMap.put(cs[0], values);
			}
			reader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading genome taxon intentisies", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading genome taxon intentisies");
			return 0;
		}
		LOGGER.info(taskName + ": " + genomeTaxaIntenMap.size() + " genomes and taxa were matched");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + genomeTaxaIntenMap.size()
				+ " genomes and taxa were matched");

		DecimalFormat df4 = FormatTool.getDF4();
		int peptideCount = 0;
		try (PrintWriter tableWriter = new PrintWriter(new File(modelFolder, "trainData.tsv"))) {
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("peptide\t");
			titlesb.append("intensity\t");
			titlesb.append("detectability\t");
			titlesb.append("protein\t");
			titlesb.append("genome");
			for (int i = 1; i <= 7; i++) {
				titlesb.append("\t").append("taxa").append(i);
			}
			for (int i = 1; i <= 5; i++) {
				titlesb.append("\t").append("func").append(i);
			}
			tableWriter.println(titlesb);

			for (String pep : pepIntenMap.keySet()) {
				StringBuilder sb = new StringBuilder(pep);

				if (predictPepMap.containsKey(pep)) {
					double intensity = pepIntenMap.get(pep);
					if (intensity > 0) {
						sb.append("\t").append(df4.format(Math.log(intensity)));
					} else {
						sb.append("\t0.0");
					}

					double predicted = predictPepMap.get(pep);
					sb.append("\t").append(df4.format(predicted));
				} else {
					continue;
				}

				double[] taxaValues = new double[7];
				double[] funcValues = new double[5];
				double proValue = 0;
				double genomeValue = 0;

				HashSet<String> proSet = pepProMap.get(pep);

				for (String pro : proSet) {
					String genome = pro.substring(0, pro.indexOf("_"));
					if (proIntenMap.containsKey(pro)) {
						proValue += proIntenMap.get(pro);
					}
					if (genomeIntenMap.containsKey(genome)) {
						genomeValue += genomeIntenMap.get(genome);
					}
					if (genomeTaxaIntenMap.containsKey(genome)) {
						double[] taxai = genomeTaxaIntenMap.get(genome);
						for (int j = 0; j < taxaValues.length; j++) {
							taxaValues[j] += taxai[j];
						}
					}
					if (profuncIntenMap.containsKey(pro)) {
						double[] funci = profuncIntenMap.get(pro);
						for (int j = 0; j < funcValues.length; j++) {
							funcValues[j] += funci[j];
						}
					}
				}

				if (proValue > 0) {
					sb.append("\t").append(df4.format(Math.log(proValue)));
				} else {
					continue;
				}

				if (genomeValue > 0) {
					sb.append("\t").append(df4.format(Math.log(genomeValue)));
				} else {
					continue;
				}

				for (int i = 0; i < taxaValues.length; i++) {
					if (taxaValues[i] > 0) {
						sb.append("\t").append(df4.format(Math.log(taxaValues[i])));
					} else {
						sb.append("\t").append(df4.format(Math.log(genomeValue)));
					}
				}

				for (int i = 0; i < funcValues.length; i++) {
					if (funcValues[i] > 0) {
						sb.append("\t").append(df4.format(Math.log(funcValues[i])));
					} else {
						sb.append("\t").append(df4.format(Math.log(proValue)));
					}
				}
				tableWriter.println(sb);
				peptideCount++;
			}
			tableWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide table", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing the peptide table");
			return 0;
		}

		File datatableFile = new File(modelFolder, "predictionData.tsv");
		if (!datatableFile.exists() || datatableFile.length() == 0) {

			LOGGER.info(taskName + ": writing all peptide candidate data table started");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": writing all peptide candidate data table started");

			ConcurrentHashMap<String, double[]> datatableMap = new ConcurrentHashMap<String, double[]>();
			executor = Executors.newFixedThreadPool(threadCount);

			for (String genome : genomeIntenMap.keySet()) {
				File predictedFile = new File(predictPepFolder, genome + ".predicted.tsv");
				executor.execute(() -> {
					try (BufferedReader reader = new BufferedReader(new FileReader(predictedFile))) {
						String pline = reader.readLine();
						HashMap<String, double[]> tempDatatableMap = new HashMap<String, double[]>();
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							double predicted = Double.parseDouble(cs[2]);
							if (tempDatatableMap.containsKey(cs[1])) {

								if (!proIntenMap.containsKey(cs[0]) || !profuncIntenMap.containsKey(cs[0])) {
									continue;
								}
								double genomeInten = genomeIntenMap.get(genome);
								double proInten = proIntenMap.get(cs[0]);

								double[] values = tempDatatableMap.get(cs[1]);
								if (predicted > values[0]) {
									values[0] = predicted;
								}
								values[1] += genomeInten;
								values[2] += proInten;

								double[] taxaIntensity = genomeTaxaIntenMap.get(genome);
								for (int i = 0; i < 7; i++) {
									if (taxaIntensity[i] > 0) {
										values[i + 3] = taxaIntensity[i];
									} else {
										values[i + 3] = genomeInten;
									}
								}

								double[] proFuncIntensity = profuncIntenMap.get(cs[0]);
								for (int i = 0; i < 5; i++) {
									if (proFuncIntensity[i] > 0) {
										values[i + 10] += proFuncIntensity[i];
									} else {
										values[i + 10] += proInten;
									}
								}

								tempDatatableMap.put(cs[1], values);
							} else {

								if (!proIntenMap.containsKey(cs[0]) || !profuncIntenMap.containsKey(cs[0])) {
									continue;
								}

								double genomeInten = genomeIntenMap.get(genome);
								double proInten = proIntenMap.get(cs[0]);

								double[] values = new double[15];
								values[0] = predicted;
								values[1] = genomeInten;
								values[2] = proInten;

								double[] taxaIntensity = genomeTaxaIntenMap.get(genome);
								for (int i = 0; i < 7; i++) {
									if (taxaIntensity[i] > 0) {
										values[i + 3] = taxaIntensity[i];
									} else {
										values[i + 3] = genomeInten;
									}
								}

								double[] proFuncIntensity = profuncIntenMap.get(cs[0]);
								for (int i = 0; i < 5; i++) {
									if (proFuncIntensity[i] > 0) {
										values[i + 10] = proFuncIntensity[i];
									} else {
										values[i + 10] = proInten;
									}
								}

								tempDatatableMap.put(cs[1], values);
							}
						}
						reader.close();

						for (String pep : tempDatatableMap.keySet()) {
							double[] tempValue = tempDatatableMap.get(pep);
							if (datatableMap.containsKey(pep)) {
								double[] values = datatableMap.get(pep);
								if (tempValue[0] > values[0]) {
									values[0] = tempValue[0];
								}
								for (int i = 1; i < values.length; i++) {
									values[i] += tempValue[i];
								}
							} else {
								datatableMap.put(pep, tempValue);
							}
						}

					} catch (IOException e) {
						LOGGER.error(taskName + ": error in reading the peptide detectability information from "
								+ predictedFile, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in reading the peptide detectability information from " + predictedFile);
					}
				});
			}
			executor.shutdown();

			try {
				if (executor.awaitTermination(10, TimeUnit.HOURS)) {
					PrintWriter tableWriter = new PrintWriter(datatableFile);
					StringBuilder titlesb = new StringBuilder();
					titlesb.append("peptide\t");
					titlesb.append("detectability\t");
					titlesb.append("protein\t");
					titlesb.append("genome");

					for (int i = 1; i <= 7; i++) {
						titlesb.append("\t").append("taxa").append(i);
					}
					for (int i = 1; i <= 5; i++) {
						titlesb.append("\t").append("func").append(i);
					}
					tableWriter.println(titlesb);
					for (String pep : datatableMap.keySet()) {
						double[] values = datatableMap.get(pep);
						StringBuilder sb = new StringBuilder(pep);
						sb.append("\t").append(df4.format(values[0]));
						for (int i = 1; i < values.length; i++) {
							if (values[i] > 0) {
								sb.append("\t").append(df4.format(Math.log(values[i])));
							} else {
								sb.append("\t0.0");
							}
						}
						tableWriter.println(sb);
					}
					tableWriter.close();
				} else {

					LOGGER.info(taskName + ": writing all peptide candidate data table didn't finish in a long time");
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": writing all peptide candidate data table didn't finish in a long time");

					return 0;
				}
			} catch (InterruptedException|IOException e) {
				// TODO Auto-generated catch block

				LOGGER.error(taskName + ": error in matching functions", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in matching functions");

				return 0;
			}
		}

		return peptideCount;
	}
	
	private void buildModel(File modelFolder, String python, boolean largeScale) {

		File pythonFile = new File(modelFolder, modelName + "_predicted.py");
		File trainFile = new File(modelFolder, "trainData.tsv");
		File predictFile = new File(modelFolder, "predictionData.tsv");
		File modelKerasFile = new File(modelFolder, modelName + ".keras");
		File modelInfoFile = new File(modelFolder, modelName + "_modelInfo.txt");
		File predictTrainFile = new File(modelFolder, modelName + "_predicted_trainData.tsv");
		File predictAllFile = new File(modelFolder, modelName + "_predicted_allData.tsv");
		File corFigureFile = new File(modelFolder, modelName + "_correlation.png");
		File disFigureFile = new File(modelFolder, modelName + "_distribution.png");

		if (this.modelType.equals(RNN)) {
			buildRNNModel(pythonFile, trainFile, predictFile, modelKerasFile, modelInfoFile, predictTrainFile,
					predictAllFile, corFigureFile, disFigureFile, python, largeScale);
		} else if (this.modelType.equals(DFNN)) {
			buildDFNNModel(pythonFile, trainFile, predictFile, modelKerasFile, modelInfoFile, predictTrainFile,
					predictAllFile, corFigureFile, disFigureFile, python, largeScale);
		}
	}

	private void buildDFNNModel(File pythonFile, File trainFile,File predictFile, File modelKerasFile, 
			File modelInfoFile, File predictTrainFile, File predictAllFile, File corFigureFile, File disFigureFile,
			String python, boolean largeScale) {
		try (PrintWriter pythonWriter = new PrintWriter(pythonFile)) {
			pythonWriter.println("from tensorflow import keras");
			pythonWriter.println("import pandas as pd");
			pythonWriter.println("import numpy as np");
			pythonWriter.println("import tensorflow as tf");
			pythonWriter.println("from tensorflow.keras import layers");
			pythonWriter.println("from keras_tuner.tuners import RandomSearch");
			pythonWriter.println("from keras.layers import LSTM, Bidirectional, Reshape");
			pythonWriter.println("from sklearn.model_selection import train_test_split");
			pythonWriter.println("from sklearn.metrics import mean_squared_error");
			pythonWriter.println("from sklearn.metrics import mean_absolute_error");
			pythonWriter.println("from sklearn.metrics import r2_score");
			pythonWriter.println();

			String dir = trainFile.getParentFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
			// Load the dataset using pandas
			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(trainFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("train_date_table = data.drop(columns=['peptide', 'intensity'])");
//			pythonWriter.println("normalizer = tf.keras.layers.Normalization(axis=-1)");
//			pythonWriter.println("normalizer.adapt(train_date_table)");
			pythonWriter.println("X_train, X_test, y_train, y_test = train_test_split(train_date_table, "
					+ "data['intensity'], test_size=0.2, random_state=42)");

			if (largeScale) {
				// Define the model building function
				pythonWriter.println("def build_model(hp):\r\n" + "    model = keras.Sequential()\r\n"
//						+ "    model.add(normalizer)\r\n"
						+ "    model.add(layers.Dense(units=hp.Int('units_input', min_value=32, max_value=128, step=32),\r\n"
						+ "                        activation='relu', input_shape=(X_train.shape[1],)))\r\n"
						+ "    for _ in range(hp.Int('num_layers', min_value=1, max_value=3)):\r\n"
						+ "      model.add(layers.Dense(units=hp.Int('units', min_value=16, max_value=64, step=16),\r\n"
						+ "                        activation=hp.Choice('activation', ['relu', 'tanh'])))\r\n"
						+ "    model.add(layers.Dense(1))\r\n"
						+ "    model.compile(optimizer=keras.optimizers.Adam(hp.Choice('learning_rate', values=[1e-3, 1e-4, 1e-5])), loss='mse', metrics=['mse'])\r\n"
						+ "    return model");
			} else {
				// Define the model building function
				pythonWriter.println("def build_model(hp):\r\n" + "    model = keras.Sequential()\r\n"
//						+ "    model.add(normalizer)\r\n"
						+ "    model.add(layers.Dense(units=hp.Int('units_input', min_value=32, max_value=128, step=32),\r\n"
						+ "                        activation='relu', input_shape=(X_train.shape[1],)))\r\n"
						+ "    for _ in range(hp.Int('num_layers', min_value=1, max_value=3)):\r\n"
						+ "      model.add(layers.Dense(units=hp.Int('units', min_value=16, max_value=64, step=16),\r\n"
						+ "                        activation=hp.Choice('activation', ['relu', 'tanh'])))\r\n"
						+ "    model.add(layers.Dense(1))\r\n"
						+ "    model.compile(optimizer=keras.optimizers.Adam(hp.Choice('learning_rate', values=[1e-2, 1e-3, 1e-4, 1e-5])), loss='mse', metrics=['mse'])\r\n"
						+ "    return model");
			}

			if (largeScale) {
				// Initialize the tuner and perform hyperparameter tuning
				pythonWriter.println("tuner = RandomSearch(\r\n" + "    hypermodel=build_model,\r\n"
						+ "    objective='val_mse',\r\n" + "    max_trials=10,\r\n" + "    executions_per_trial=2,\r\n"
						+ "    directory='" + dir + "',\r\n" + "    project_name='" + modelName + "'\r\n" + ")");
			} else {
				// Initialize the tuner and perform hyperparameter tuning
				pythonWriter.println("tuner = RandomSearch(\r\n" + "    hypermodel=build_model,\r\n"
						+ "    objective='val_mse',\r\n" + "    max_trials=15,\r\n" + "    executions_per_trial=3,\r\n"
						+ "    directory='" + dir + "',\r\n" + "    project_name='" + modelName + "'\r\n" + ")");
			}

			pythonWriter.println("early_stopping = keras.callbacks.EarlyStopping(monitor='val_loss', patience=10)");

			// Convert DataFrame and Series to NumPy arrays
			pythonWriter.println("X_test_np = X_test.to_numpy()");
			pythonWriter.println("y_test_np = y_test.to_numpy()");
			if (largeScale) {
				pythonWriter.println(
						"tuner.search(X_test_np, y_test_np, epochs=50, validation_split=0.2, callbacks=[early_stopping])");

			} else {
				pythonWriter.println(
						"tuner.search(X_test_np, y_test_np, epochs=100, validation_split=0.2, callbacks=[early_stopping])");

			}

			// Get the best model and evaluate it on the test set
			pythonWriter.println("best_model = tuner.get_best_models(num_models=1)[0]");
			pythonWriter.println("best_model.evaluate(X_test_np, y_test_np)");
			pythonWriter.println("best_model.summary()");

			pythonWriter.print("best_model.save(\"");
			pythonWriter.print(modelKerasFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\")");
			pythonWriter.println();

			pythonWriter.println("X_pred_np = train_date_table.to_numpy()");
			pythonWriter.println("y_pred = best_model.predict(X_pred_np)");
			pythonWriter.println(
					"y_pred_result = pd.DataFrame({'peptide': data['peptide'], 'predicted_log_intensity': pd.Series(y_pred.ravel())})");

			pythonWriter.print("y_pred_result.to_csv(\"");
			pythonWriter.print(predictTrainFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", index=False, sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("corr = np.corrcoef(data['intensity'], pd.Series(np.squeeze(y_pred)))[0, 1]");
			pythonWriter.println("mse = mean_squared_error(data['intensity'], y_pred)");
			pythonWriter.println("rmse = np.sqrt(mse)");
			pythonWriter.println("mae = mean_absolute_error(data['intensity'], y_pred)");
			pythonWriter.println("r2 = r2_score(data['intensity'], y_pred)");

			pythonWriter.print("with open(\"");
			pythonWriter.print(modelInfoFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.println("\", 'w') as file:");
			pythonWriter.println(
					"\tfile.write(f\"The correlation coefficient between the original and predicted score is {corr:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"MSE: {mse:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"RMSE: {rmse:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"MAE: {mae:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"R2 Score: {r2:.2f}\")");

			pythonWriter.println("import matplotlib.pyplot as plt");
			pythonWriter.println("plt.scatter(data['intensity'], pd.Series(np.squeeze(y_pred)))");
			pythonWriter.println("plt.xlabel(\"Original log(intensity)\")");
			pythonWriter.println("plt.ylabel(\"Predicted log(intensity)\")");
			pythonWriter.println(
					"plt.title(f\"Scatter plot of original and predicted log(intensity) (corr = {corr:.2f})\")");

			pythonWriter.print("plt.savefig(\"");
			pythonWriter.print(corFigureFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", dpi=300)");
			pythonWriter.println();

			// prediction
			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(predictFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("feature_table = data.drop(columns=['peptide'])");

			if (largeScale) {
				pythonWriter.println("chunk_size = 100000");
				pythonWriter.println("all_predictions = []");
				pythonWriter.println("for i in range(0, len(feature_table), chunk_size):");
				pythonWriter.println("\tX_chunk = feature_table.iloc[i:i+chunk_size]");
				pythonWriter.println("\tpredictions = best_model.predict(X_chunk)");
				pythonWriter.println("\tpredicted_log_intensity = predictions.flatten()");
				pythonWriter.println("\tall_predictions.extend(predicted_log_intensity)");
				pythonWriter
						.println("prediction_df = pd.DataFrame(all_predictions, columns=['Predicted_log_intensity'])");
			} else {
				pythonWriter.println("predictions = best_model.predict(feature_table)");
				pythonWriter.println("predicted_log_intensity = predictions.flatten()");
				pythonWriter.println(
						"prediction_df = pd.DataFrame(predicted_log_intensity, columns=['Predicted_log_intensity'])");
			}
			pythonWriter.println("result = pd.concat([data['peptide'], prediction_df], axis=1)");
			pythonWriter.print("result.to_csv(\"");
			pythonWriter.print(predictAllFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\")");
			pythonWriter.println();
			pythonWriter.close();

			pythonWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the python file " + pythonFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the python file " + pythonFile);
		}

		try {
			File batFile = new File(pythonFile.getParent(), pythonFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			File pythonExeFile = new File(python);

			StringBuilder sb = new StringBuilder();
			sb.append(pythonExeFile.getName()).append(" ");
			sb.append(pythonFile.getAbsolutePath());

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + pythonExeFile.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
			batWriter.print("exit");
			batWriter.close();

			String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };
			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			while ((lineStr = inBr.readLine()) != null) {
				System.out.println(lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in building the peptide intensity model by deep learning", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in building the peptide intensity model by deep learning");
		}
	}
	
	private void buildRNNModel(File pythonFile, File trainFile,File predictFile, File modelKerasFile, 
			File modelInfoFile,File predictTrainFile,File predictAllFile, File corFigureFile,File disFigureFile, String python, boolean largeScale) {
		try (PrintWriter pythonWriter = new PrintWriter(pythonFile)) {
			pythonWriter.println("from tensorflow import keras");
			pythonWriter.println("import pandas as pd");
			pythonWriter.println("import numpy as np");
			pythonWriter.println("import tensorflow as tf");
			pythonWriter.println("from tensorflow.keras import layers");
			pythonWriter.println("from keras_tuner.tuners import RandomSearch");
			pythonWriter.println("from keras.layers import LSTM, Bidirectional, Reshape");
			pythonWriter.println("from sklearn.model_selection import train_test_split");
			pythonWriter.println("from sklearn.metrics import mean_squared_error");
			pythonWriter.println("from sklearn.metrics import mean_absolute_error");
			pythonWriter.println("from sklearn.metrics import r2_score");
			pythonWriter.println();
			
			String dir = trainFile.getParentFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");

			// Load the dataset using pandas
			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(trainFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("train_date_table = data.drop(columns=['peptide', 'intensity'])");
			pythonWriter.println("train_date_table_np = train_date_table.to_numpy()");
			pythonWriter.println("train_date_table_np = train_date_table_np.astype('float32')");
//			pythonWriter.println("normalizer = tf.keras.layers.Normalization(axis=-1)");
//			pythonWriter.println("normalizer.adapt(train_date_table_np)");
			pythonWriter.println();
			
			pythonWriter.println("X_train, X_test, y_train, y_test = train_test_split(train_date_table, "
					+ "data['intensity'], test_size=0.2, random_state=42)");
			pythonWriter.println("intensity_weights = data['intensity'] / data['intensity'].max()");
			pythonWriter.println("sample_weights_train = intensity_weights[X_train.index]");
			pythonWriter.println("sample_weights_test = intensity_weights[X_test.index]");
			pythonWriter.println();

			if (largeScale) {
				// Define the model building function
				pythonWriter.println("def build_model(hp):\r\n"
						+ "    from tensorflow.keras.models import Sequential\r\n"
						+ "    from tensorflow.keras.layers import Dense, Dropout, Reshape, Bidirectional, LSTM\r\n"
						+ "    model = Sequential()\r\n"
//						+ "    model.add(normalizer)\r\n"
						+ "    model.add(layers.Reshape((1, train_date_table.shape[1]), input_shape=(train_date_table.shape[1],)))\r\n"
						+ "    model.add(Bidirectional(LSTM(64, return_sequences=True)))\r\n"
						+ "    model.add(Bidirectional(LSTM(32)))\r\n"
						+ "    model.add(Dense(units=hp.Int('units', min_value=32, max_value=512, step=32), activation='relu'))\r\n"
						+ "    model.add(Dropout(0.5))\r\n"
						+ "    model.add(Dense(units=hp.Int('units', min_value=32, max_value=512, step=32), activation='relu'))\r\n"
						+ "    model.add(Dense(1))\r\n"
						+ "    model.compile(optimizer=keras.optimizers.Adam(hp.Choice('learning_rate', values=[1e-3, 1e-4, 1e-5])), loss='mse', metrics=['mse', 'mae'], weighted_metrics=['mse'])\r\n"
						+ "    return model");

			} else {
				// Define the model building function
				pythonWriter.println("def build_model(hp):\r\n"
						+ "    from tensorflow.keras.models import Sequential\r\n"
						+ "    from tensorflow.keras.layers import Dense, Dropout, Reshape, Bidirectional, LSTM\r\n"
						+ "    model = Sequential()\r\n"
//						+ "    model.add(normalizer)\r\n"
						+ "    model.add(layers.Reshape((1, train_date_table.shape[1]), input_shape=(train_date_table.shape[1],)))\r\n"
						+ "    model.add(Bidirectional(LSTM(64, return_sequences=True)))\r\n"
						+ "    model.add(Bidirectional(LSTM(32)))\r\n"
						+ "    model.add(Dense(units=hp.Int('units', min_value=32, max_value=512, step=32), activation='relu'))\r\n"
						+ "    model.add(Dropout(0.5))\r\n"
						+ "    model.add(Dense(units=hp.Int('units', min_value=32, max_value=512, step=32), activation='relu'))\r\n"
						+ "    model.add(Dense(1))\r\n"
						+ "    model.compile(optimizer=keras.optimizers.Adam(hp.Choice('learning_rate', values=[1e-2, 1e-3, 1e-4, 1e-5])), loss='mse', metrics=['mse', 'mae'], weighted_metrics=['mse'])\r\n"
						+ "    return model");

			}

			if (largeScale) {
				// Initialize the tuner and perform hyperparameter tuning
				pythonWriter.println("tuner = RandomSearch(\r\n" + "    build_model,\r\n"
						+ "    objective='val_loss',\r\n" + "    max_trials=10,\r\n" + "    executions_per_trial=2,\r\n"
						+ "    directory='" + dir + "',\r\n" + "    project_name='" + modelName + "'\r\n" + ")");
			} else {
				// Initialize the tuner and perform hyperparameter tuning
				pythonWriter.println("tuner = RandomSearch(\r\n" + "    build_model,\r\n"
						+ "    objective='val_loss',\r\n" + "    max_trials=15,\r\n" + "    executions_per_trial=3,\r\n"
						+ "    directory='" + dir + "',\r\n" + "    project_name='" + modelName + "'\r\n" + ")");
			}

			pythonWriter.println("early_stopping = keras.callbacks.EarlyStopping(monitor='val_loss', patience=10)");

			// Convert DataFrame and Series to NumPy arrays
			pythonWriter.println("X_train_np = X_train.to_numpy().astype('float32')");
			pythonWriter.println("y_train_np = y_train.to_numpy().astype('float32')");
			pythonWriter.println("X_test_np = X_test.to_numpy().astype('float32')");
			pythonWriter.println("y_test_np = y_test.to_numpy().astype('float32')");
			pythonWriter.println("sample_weights_train_np = sample_weights_train.to_numpy().astype('float32')");
			pythonWriter.println("sample_weights_test_np = sample_weights_test.to_numpy().astype('float32')");
			if (largeScale) {
				pythonWriter.println("tuner.search(X_train_np, y_train_np, epochs=50, validation_split=0.2, "
						+ "callbacks=[early_stopping], sample_weight=sample_weights_train_np)");
			} else {
				pythonWriter.println("tuner.search(X_train_np, y_train_np, epochs=100, validation_split=0.2, "
						+ "callbacks=[early_stopping], sample_weight=sample_weights_train_np)");
			}

			// Get the best model and evaluate it on the test set
			pythonWriter.println("best_model = tuner.get_best_models(num_models=1)[0]");
			pythonWriter.println("best_model.evaluate(X_test_np, y_test_np, sample_weight=sample_weights_test_np)");
			pythonWriter.println("best_model.summary()");

			pythonWriter.print("best_model.save(\"");
			pythonWriter.print(modelKerasFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\")");
			pythonWriter.println();

			pythonWriter.println("X_pred_np = train_date_table.to_numpy()");
			pythonWriter.println("y_pred = best_model.predict(X_pred_np)");
			pythonWriter.println(
					"y_pred_result = pd.DataFrame({'peptide': data['peptide'], 'predicted_log_intensity': pd.Series(y_pred.ravel())})");

			pythonWriter.print("y_pred_result.to_csv(\"");
			pythonWriter.print(predictTrainFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", index=False, sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("corr = np.corrcoef(data['intensity'], pd.Series(np.squeeze(y_pred)))[0, 1]");
			pythonWriter.println("mse = mean_squared_error(data['intensity'], y_pred)");
			pythonWriter.println("rmse = np.sqrt(mse)");
			pythonWriter.println("mae = mean_absolute_error(data['intensity'], y_pred)");
			pythonWriter.println("r2 = r2_score(data['intensity'], y_pred)");

			pythonWriter.print("with open(\"");
			pythonWriter.print(modelInfoFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.println("\", 'w') as file:");
			pythonWriter.println(
					"\tfile.write(f\"The correlation coefficient between the original and predicted score is {corr:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"MSE: {mse:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"RMSE: {rmse:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"MAE: {mae:.2f}\\n\")");
			pythonWriter.println("\tfile.write(f\"R2 Score: {r2:.2f}\")");

			pythonWriter.println("import matplotlib.pyplot as plt");
			pythonWriter.println("plt.scatter(data['intensity'], pd.Series(np.squeeze(y_pred)))");
			pythonWriter.println("plt.xlabel(\"Original log(intensity)\")");
			pythonWriter.println("plt.ylabel(\"Predicted log(intensity)\")");
			pythonWriter.println(
					"plt.title(f\"Scatter plot of original and predicted log(intensity) (corr = {corr:.2f})\")");

			pythonWriter.print("plt.savefig(\"");
			pythonWriter.print(corFigureFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", dpi=300)");
			pythonWriter.println();
			// prediction
			
			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(predictFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();
			pythonWriter.println("feature_table = data.drop(columns=['peptide'])");
			
			if (largeScale) {
				pythonWriter.println("chunk_size = 1000000");
				pythonWriter.println("all_predictions = []");
				pythonWriter.println("for i in range(0, len(feature_table), chunk_size):");
				pythonWriter.println("\tX_chunk = feature_table.iloc[i:i+chunk_size]");
				pythonWriter.println("\tpredictions = best_model.predict(X_chunk)");
				pythonWriter.println("\tpredicted_log_intensity = predictions.flatten()");
				pythonWriter.println("\tall_predictions.extend(predicted_log_intensity)");
				pythonWriter
						.println("prediction_df = pd.DataFrame(all_predictions, columns=['Predicted_log_intensity'])");
			} else {
				pythonWriter.println("predictions = best_model.predict(feature_table)");
				pythonWriter.println("predicted_log_intensity = predictions.flatten()");
				pythonWriter.println(
						"prediction_df = pd.DataFrame(predicted_log_intensity, columns=['Predicted_log_intensity'])");
			}
			pythonWriter.println("result = pd.concat([data['peptide'], prediction_df], axis=1)");
			pythonWriter.print("result.to_csv(\"");
			pythonWriter.print(predictAllFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\")");
			pythonWriter.println();
			pythonWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the python file " + pythonFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the python file " + pythonFile);
		}

		try {
			File batFile = new File(pythonFile.getParent(), pythonFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			File pythonExeFile = new File(python);

			StringBuilder sb = new StringBuilder();
			sb.append(pythonExeFile.getName()).append(" ");
			sb.append(pythonFile.getAbsolutePath());

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + pythonExeFile.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
			batWriter.print("exit");
			batWriter.close();

			String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };
			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			while ((lineStr = inBr.readLine()) != null) {
				System.out.println(lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in building the peptide intensity model by deep learning", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in building the peptide intensity model by deep learning");
		}
	}

	private File predictedWithPro(File model, File db, String python) throws IOException, InterruptedException {

		File datatableFile = new File(model, model.getName() + "_dataTable_all.tsv");
		if (!datatableFile.exists() || datatableFile.length() == 0) {

			LOGGER.info(taskName + ": writing all peptide candidate data table started");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": writing all peptide candidate data table started");

			File genomeTaxaFile = new File(model, "genome_taxa_intensity.tsv");
			File genomeFile = new File(model, "genome_intensity.tsv");
			File proFile = new File(model, "pro_intensity.tsv");
			File funcFile = new File(model, "func_intensity.tsv");

			HashMap<String, Double> proIntenMap = new HashMap<String, Double>();
			try (BufferedReader reader = new BufferedReader(new FileReader(proFile))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					proIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading the protein intensity table from " + proFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading the protein intensity table from " + proFile);
				return null;
			}

			HashMap<String, Double> genomeIntenMap = new HashMap<String, Double>();
			try (BufferedReader reader = new BufferedReader(new FileReader(genomeFile))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					genomeIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading the genome intensity table from " + genomeFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading the genome intensity table from " + genomeFile);
				return null;
			}

			HashMap<String, double[]> genomeTaxaIntenMap = new HashMap<String, double[]>();
			try (BufferedReader reader = new BufferedReader(new FileReader(genomeTaxaFile))) {
				String pline = null;
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					double[] intensity = new double[7];
					for (int i = 0; i < intensity.length; i++) {
						intensity[i] = Double.parseDouble(cs[i + 1]);
					}
					genomeTaxaIntenMap.put(cs[0], intensity);
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading the genome taxa intensity table from " + genomeTaxaFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading the genome taxa intensity table from " + genomeTaxaFile);
				return null;
			}

			HashMap<String, Double> funcIntenMap = new HashMap<String, Double>();
			try (BufferedReader reader = new BufferedReader(new FileReader(funcFile))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					funcIntenMap.put(cs[0], Double.parseDouble(cs[1]));
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading the function intensity table from " + funcFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading the function intensity table from " + funcFile);
				return null;
			}

			ConcurrentHashMap<String, double[]> datatableMap = new ConcurrentHashMap<String, double[]>();
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);

			for (String genome : genomeIntenMap.keySet()) {
				File predictedFile = new File(new File(db, "predicted"), genome + ".predicted.tsv");

				File file0 = new File(new File(db, "eggNOG"), genome + "_eggNOG.tsv");
				File file1 = new File(new File(db, "eggNOG"), genome + ".emapper.annotations");
				File eggnogFile = file0.exists() ? file0 : file1;

				HashMap<String, String> pepProMap = new HashMap<String, String>();
				executor.execute(() -> {
					HashMap<String, double[]> proFuncIntenMap = new HashMap<String, double[]>();
					try (BufferedReader reader = new BufferedReader(new FileReader(eggnogFile))) {
						String pline = null;
						while ((pline = reader.readLine()) != null) {
							if (pline.startsWith("#query")) {
								break;
							}
						}
						String[] title = pline.split("\t");
						int eggNogId = -1;
						for (int i = 0; i < title.length; i++) {
							if (title[i].equals("eggNOG_OGs")) {
								eggNogId = i;
							}
						}

						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							if (cs.length <= eggNogId) {
								continue;
							}
							String[] funcStrings = cs[eggNogId].split(",");
							int func3BeginId = -1;
							for (int i = 0; i < funcStrings.length; i++) {
								if (funcStrings[i].endsWith("root")) {

								} else if (funcStrings[i].endsWith("Eukaryota")) {

								} else if (funcStrings[i].endsWith("Viruses")) {

								} else if (funcStrings[i].endsWith("Archaea")) {

								} else if (funcStrings[i].endsWith("Bacteria")) {

								} else {
									if (func3BeginId == -1) {
										func3BeginId = i;
									}
								}
							}

							double[] values = new double[5];

							for (int i = 0; i < funcStrings.length; i++) {

								int funcLevel = -1;
								if (func3BeginId == -1) {
									if (funcStrings[i].endsWith("root")) {
										funcLevel = 0;
									} else {
										funcLevel = 1;
									}
								} else {
									if (i == func3BeginId) {
										funcLevel = 2;
									} else if (i == func3BeginId + 1) {
										funcLevel = 3;
									} else if (i == func3BeginId + 2) {
										funcLevel = 4;
									} else {
										if (funcStrings[i].endsWith("root")) {
											funcLevel = 0;
										} else {
											funcLevel = 1;
										}
									}
								}

								if (funcIntenMap.containsKey(funcStrings[i])) {
									values[funcLevel] += funcIntenMap.get(funcStrings[i]);
								}
							}

							proFuncIntenMap.put(cs[0], values);
						}
						reader.close();
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in reading the functional information from " + eggnogFile, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in reading the functional information from " + eggnogFile);
					}

					try (BufferedReader reader = new BufferedReader(new FileReader(predictedFile))) {
						String pline = reader.readLine();
						HashMap<String, double[]> tempDatatableMap = new HashMap<String, double[]>();
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							double predicted = Double.parseDouble(cs[2]);
							if (tempDatatableMap.containsKey(cs[1])) {

								if (!proIntenMap.containsKey(cs[0])) {
									continue;
								}

								double[] values = tempDatatableMap.get(cs[1]);
								if (predicted > values[0]) {
									values[0] = predicted;
								}

								double proInten = proIntenMap.get(cs[0]);

								if (proFuncIntenMap.containsKey(cs[0])) {
									double[] proFuncIntensity = proFuncIntenMap.get(cs[0]);
									for (int i = 0; i < 5; i++) {
										if (proFuncIntensity[i] > 0) {
											values[i + 10] += proFuncIntensity[i];
										} else {
											values[i + 10] += proInten;
										}
									}
								}

								tempDatatableMap.put(cs[1], values);
							} else {

								if (!proIntenMap.containsKey(cs[0])) {
									continue;
								}

								double genomeInten = genomeIntenMap.get(genome);
								double proInten = proIntenMap.get(cs[0]);

								double[] values = new double[15];
								values[0] = predicted;
								values[1] = genomeInten;
								values[2] = proInten;

								double[] taxaIntensity = genomeTaxaIntenMap.get(genome);
								for (int i = 0; i < 7; i++) {
									if (taxaIntensity[i] > 0) {
										values[i + 3] = taxaIntensity[i];
									} else {
										values[i + 3] = genomeInten;
									}
								}

								if (proFuncIntenMap.containsKey(cs[0])) {
									double[] proFuncIntensity = proFuncIntenMap.get(cs[0]);
									for (int i = 0; i < 5; i++) {
										if (proFuncIntensity[i] > 0) {
											values[i + 10] = proFuncIntensity[i];
										} else {
											values[i + 10] = proInten;
										}
									}
								}

								tempDatatableMap.put(cs[1], values);
							}
							pepProMap.put(cs[1], cs[0]);
						}
						reader.close();

						for (String pep : tempDatatableMap.keySet()) {
							double[] tempValue = tempDatatableMap.get(pep);
							if (datatableMap.containsKey(pep)) {
								double[] values = datatableMap.get(pep);
								if (tempValue[0] > values[0]) {
									values[0] = tempValue[0];
								}
								for (int i = 1; i < values.length; i++) {
									values[i] += tempValue[i];
								}
							} else {
								datatableMap.put(pep, tempValue);
							}
						}

					} catch (IOException e) {
						LOGGER.error(taskName + ": error in reading the peptide detectability information from "
								+ predictedFile, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in reading the peptide detectability information from " + predictedFile);
					}
				});
			}
			executor.shutdown();

			if (executor.awaitTermination(10, TimeUnit.HOURS)) {
				DecimalFormat df4 = FormatTool.getDF4();
				PrintWriter tableWriter = new PrintWriter(datatableFile);
				StringBuilder titlesb = new StringBuilder();
				titlesb.append("peptide\t");
				titlesb.append("detectability\t");
				titlesb.append("protein\t");
				titlesb.append("genome");

				for (int i = 1; i <= 7; i++) {
					titlesb.append("\t").append("taxa").append(i);
				}
				for (int i = 1; i <= 5; i++) {
					titlesb.append("\t").append("func").append(i);
				}
				tableWriter.println(titlesb);
				for (String pep : datatableMap.keySet()) {
					double[] values = datatableMap.get(pep);
					StringBuilder sb = new StringBuilder(pep);
					sb.append("\t").append(df4.format(values[0]));
					for (int i = 1; i < values.length; i++) {
						if (values[i] > 0) {
							sb.append("\t").append(df4.format(Math.log(values[i])));
						} else {
							sb.append("\t0.0");
						}
					}
					tableWriter.println(sb);
				}
				tableWriter.close();
			} else {
				LOGGER.info(taskName + ": writing all peptide candidate data table didn't finish in a long time");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": writing all peptide candidate data table didn't finish in a long time");
				return null;
			}
		}

		if (datatableFile.exists() && datatableFile.length() > 0) {
			File modelFile = new File(model, model.getName() + ".keras");
			File resultFile = new File(model, model.getName() + "_predicted_all.tsv");
			File pythonFile = new File(model, model.getName() + "_predicted_all.py");

			PrintWriter pythonWriter = new PrintWriter(pythonFile);
			pythonWriter.println("from tensorflow import keras");
			pythonWriter.println("import pandas as pd");
			pythonWriter.println();

			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(datatableFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.print("model = keras.models.load_model(\"");
			pythonWriter.print(modelFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.println("\")");

			pythonWriter.println("feature_table = data.drop(columns=['peptide'])");
			pythonWriter.println("predictions = model.predict(feature_table)");
			pythonWriter.println("predicted_log_intensity = predictions.flatten()");
			pythonWriter.println("prediction_df = pd.DataFrame(predicted_log_intensity, columns=['Predicted_log_intensity'])");
			pythonWriter.println("result = pd.concat([data['peptide'], prediction_df], axis=1)");

			pythonWriter.print("result.to_csv(\"");
			pythonWriter.print(resultFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\")");
			pythonWriter.println();
			pythonWriter.close();

			File batFile = new File(pythonFile.getParent(), pythonFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			File pythonExeFile = new File(python);

			StringBuilder sb = new StringBuilder();
			sb.append(pythonExeFile.getName()).append(" ");
			sb.append(pythonFile.getAbsolutePath());

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + pythonExeFile.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
			batWriter.print("exit");
			batWriter.close();

			try {
				String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };
				ProcessBuilder pb = new ProcessBuilder(args);
				Process p = pb.start();
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr = null;
				while ((lineStr = inBr.readLine()) != null) {
					System.out.println(lineStr);
				}

				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {

			}

			return resultFile;

		} else {
			LOGGER.info(taskName + ": writing all peptide candidate data table failed");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": writing all peptide candidate data table failed");
			return null;
		}
	}
	
	private static void evaluteTest(File projectFile) {
		File pepIntenFile = new File(projectFile, "pep_intensity.tsv");
		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(pepIntenFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				pepSet.add(cs[0]);
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the training dataset peptide from " + pepIntenFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the training dataset peptide from " + pepIntenFile);
		}

		HashMap<String, Double> pepRankMap = new HashMap<String, Double>();
		File pepRankFile = new File(projectFile, projectFile.getName() + "_pep_rank.tsv");
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(pepRankFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (pepSet.contains(cs[1])) {
					double rankScore = Double.parseDouble(cs[2]);
					if (pepRankMap.containsKey(cs[1])) {
						if (rankScore > pepRankMap.get(cs[1])) {
							pepRankMap.put(cs[1], rankScore);
						}
					} else {
						pepRankMap.put(cs[1], rankScore);
					}
				}
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the peptide rank information from " + pepRankFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the peptide rank information from " + pepRankFile);
		}

		double[] rankScoreArrays = new double[pepRankMap.size()];
		int rankScoreId = 0;
		for(String pep: pepRankMap.keySet()) {
			double rankScore = pepRankMap.get(pep);
			rankScoreArrays[rankScoreId++] = rankScore;
		}
		
		double averageRankScore = MathTool.getMedian(rankScoreArrays);
		System.out.println(averageRankScore+"\t"+rankScoreArrays.length);
		/*
		File trainRankFile = new File(projectFile, projectFile.getName() + "_train_pep_rank.tsv");
		try (PrintWriter writer = new PrintWriter(trainRankFile)) {
			writer.println("peptide\trank_score");
			for (String pep : pepRankMap.keySet()) {
				writer.println(pep + "\t" + pepRankMap.get(pep));
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the training peptide rank information to " + trainRankFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the training peptide rank information to " + trainRankFile);
		}

		File pythonFile = new File(projectFile, projectFile.getName() + "_train_pep_rank_histogram.py");
		File figureFile = new File(projectFile, projectFile.getName() + "_train_pep_rank_histogram.png");
		try (PrintWriter pythonWriter = new PrintWriter(pythonFile)) {

			pythonWriter.println("import matplotlib.pyplot as plt");
			pythonWriter.println("import pandas as pd");
			pythonWriter.println();

			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(trainRankFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("plt.figure(figsize=(8, 6), dpi=300)");
			pythonWriter.println("plt.hist(data['rank_score'], bins=100, range=(0, 1), edgecolor='black')");
			pythonWriter.println("plt.title('Distribution of peptides from training set')");
			pythonWriter.println("plt.xlabel('Rank score')");
			pythonWriter.println("plt.ylabel('Peptide count')");
			pythonWriter.println();
			pythonWriter.print("plt.savefig(\"");
			pythonWriter.print(figureFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", dpi=300)");
			pythonWriter.println();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the python file to " + pythonFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing the python file to "
					+ pythonFile);
		}

		try {
			ProcessBuilder pb = new ProcessBuilder(python, pythonFile.getAbsolutePath());
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			int lineCount = 0;
			while ((lineStr = inBr.readLine()) != null) {
				if (lineCount++ % 100 == 0) {
					System.out.println(lineStr);
				}
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in drawing the histogram", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in drawing the histogram");
		}
		*/
	}

	private double evalute(File projectFile, String python) {
		File pepIntenFile = new File(projectFile, "pep_intensity.tsv");
		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(pepIntenFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				pepSet.add(cs[0]);
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the training dataset peptide from " + pepIntenFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the training dataset peptide from " + pepIntenFile);
		}

		HashMap<String, Double> pepRankMap = new HashMap<String, Double>();
		File pepRankFile = new File(projectFile, modelName + "_pep_rank.tsv");
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(pepRankFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (pepSet.contains(cs[1])) {
					double rankScore = Double.parseDouble(cs[2]);
					if (pepRankMap.containsKey(cs[1])) {
						if (rankScore > pepRankMap.get(cs[1])) {
							pepRankMap.put(cs[1], rankScore);
						}
					} else {
						pepRankMap.put(cs[1], rankScore);
					}
				}
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the peptide rank information from " + pepRankFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the peptide rank information from " + pepRankFile);
		}

		double[] rankScoreArrays = new double[pepRankMap.size()];
		int rankScoreId = 0;
		for (String pep : pepRankMap.keySet()) {
			double rankScore = pepRankMap.get(pep);
			rankScoreArrays[rankScoreId++] = rankScore;
		}

		double averageRankScore = MathTool.getMedian(rankScoreArrays);

		File trainRankFile = new File(projectFile, modelName + "_train_pep_rank.tsv");
		try (PrintWriter writer = new PrintWriter(trainRankFile)) {
			writer.println("peptide\trank_score");
			for (String pep : pepRankMap.keySet()) {
				writer.println(pep + "\t" + pepRankMap.get(pep));
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the training peptide rank information to " + trainRankFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the training peptide rank information to " + trainRankFile);
		}

		File pythonFile = new File(projectFile, modelName + "_train_pep_rank_histogram.py");
		File figureFile = new File(projectFile, modelName + "_train_pep_rank_histogram.png");
		try (PrintWriter pythonWriter = new PrintWriter(pythonFile)) {

			pythonWriter.println("import matplotlib.pyplot as plt");
			pythonWriter.println("import pandas as pd");
			pythonWriter.println();

			pythonWriter.print("data = pd.read_csv(\"");
			pythonWriter.print(trainRankFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", sep=(\"\\t\"))");
			pythonWriter.println();

			pythonWriter.println("plt.figure(figsize=(8, 6), dpi=300)");
			pythonWriter.println("plt.hist(data['rank_score'], bins=100, range=(0, 1), edgecolor='black')");
			pythonWriter.println("plt.title('Distribution of peptides from training set')");
			pythonWriter.println("plt.xlabel('Rank score')");
			pythonWriter.println("plt.ylabel('Peptide count')");
			pythonWriter.println();
			pythonWriter.print("plt.savefig(\"");
			pythonWriter.print(figureFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			pythonWriter.print("\", dpi=300)");
			pythonWriter.println();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the python file to " + pythonFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing the python file to "
					+ pythonFile);
		}

		try {
			File batFile = new File(pythonFile.getParent(), pythonFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			File pythonExeFile = new File(python);

			StringBuilder sb = new StringBuilder();
			sb.append(pythonExeFile.getName()).append(" ");
			sb.append(pythonFile.getAbsolutePath());

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + pythonExeFile.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
			batWriter.print("exit");
			batWriter.close();

			String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };
			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			while ((lineStr = inBr.readLine()) != null) {
				System.out.println(lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in building the peptide intensity model by deep learning", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in building the peptide intensity model by deep learning");
		}

		return averageRankScore;
	}

	private int getPepRank(File dbFile, File projectFile) throws InterruptedException {
		File predictFile = new File(projectFile, modelName + "_predicted_allData.tsv");
		HashMap<String, Double> pepMap = new HashMap<String, Double>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(predictFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split(",");
				pepMap.put(cs[1], Double.parseDouble(cs[2]));
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in reading the predicted peptide intensity information from " + predictFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the predicted peptide intensity information from " + predictFile);
		}

		HashSet<String> genomeSet = new HashSet<String>();
		File genomeFile = new File(projectFile, "genome_intensity.tsv");
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(genomeFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				genomeSet.add(cs[0]);
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the genome information from " + genomeFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the genome information from " + genomeFile);
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		File outputFile = new File(projectFile, modelName + "_pep_rank.tsv");
		BufferedWriter[] writer = { null };
		try {
			writer[0] = new BufferedWriter(new FileWriter(outputFile, true));
			writer[0].write("pro\tpep\tgenome_rank_score\n");
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the peptide rank information to " + outputFile);
		}

		DecimalFormat df4 = FormatTool.getDF4();
		File predictPepFile = new File(dbFile, "predicted");

		for (String genome : genomeSet) {

			executor.execute(() -> {

				File file = new File(predictPepFile, genome + ".predicted.tsv");
				ArrayList<String> peplist = new ArrayList<String>();
				HashMap<String, String> pepProMap = new HashMap<String, String>();
				HashMap<String, Double> pepDetectMap = new HashMap<String, Double>();
				try (BufferedReader pepProReader = new BufferedReader(new FileReader(file))) {
					String line = pepProReader.readLine();
					while ((line = pepProReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (pepMap.containsKey(cs[1])) {
							double detect = Double.parseDouble(cs[2]);
							if (pepDetectMap.containsKey(cs[1])) {
								if (detect > pepDetectMap.get(cs[1])) {
									pepProMap.put(cs[1], cs[0]);
									pepDetectMap.put(cs[1], detect);
								}
							} else {
								peplist.add(cs[1]);
								pepProMap.put(cs[1], cs[0]);
								pepDetectMap.put(cs[1], detect);
							}
						}
					}
					pepProReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + ": error in reading the peptide information from " + file, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in reading the peptide information from " + file);
				}

				String[] peps = peplist.toArray(new String[peplist.size()]);
				Arrays.sort(peps, (p1, p2) -> {
					double d1 = pepMap.get(p1);
					double d2 = pepMap.get(p2);
					if (d1 > d2) {
						return 1;
					}
					if (d1 < d2) {
						return -1;
					}
					double detect1 = pepDetectMap.get(p1);
					double detect2 = pepDetectMap.get(p2);
					if (detect1 > detect2) {
						return 1;
					}
					if (detect1 < detect2) {
						return -1;
					}

					return 0;
				});

				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < peps.length; i++) {
					double genome_rank_ratio = (double) (i + 1) / (double) peps.length;
					sb.append(pepProMap.get(peps[i])).append("\t").append(peps[i]).append("\t")
							.append(df4.format(genome_rank_ratio)).append("\n");
				}

				synchronized (writer[0]) {
					try {
						writer[0].write(sb.toString());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputFile, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in writing the peptide rank information to " + outputFile);
					}
				}
			});
		}
		executor.shutdown();

		if (executor.awaitTermination(10, TimeUnit.HOURS)) {
			try {
				writer[0].close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing the peptide rank information to " + outputFile);
			}
		}

		return pepMap.size();
	}
	
	private int getPepRankSQL(File sqlDbFile, File projectFile) throws InterruptedException {
		File predictFile = new File(projectFile, modelName + "_predicted_allData.tsv");
		HashMap<String, Double> pepMap = new HashMap<String, Double>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(predictFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split(",");
				pepMap.put(cs[1], Double.parseDouble(cs[2]));
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in reading the predicted peptide intensity information from " + predictFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the predicted peptide intensity information from " + predictFile);
		}

		HashSet<String> genomeSet = new HashSet<String>();
		File genomeFile = new File(projectFile, "genome_intensity.tsv");
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(genomeFile))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				genomeSet.add(cs[0]);
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading the genome information from " + genomeFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading the genome information from " + genomeFile);
		}

		HashSet<String> pepSet = new HashSet<String>(pepMap.keySet());
		String[][] peptideContents = MagSqliteTask.queryPeptide(pepSet, sqlDbFile);
		Arrays.sort(peptideContents, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				String genome1 = o1[0].substring(0, o1[0].indexOf("_"));
				String genome2 = o2[0].substring(0, o2[0].indexOf("_"));

				if (genome1.equals(genome2)) {

					double d1 = pepMap.get(o1[1]);
					double d2 = pepMap.get(o2[1]);
					if (d1 > d2) {
						return 1;
					}
					if (d1 < d2) {
						return -1;
					}
					double detect1 = Double.parseDouble(o1[2]);
					double detect2 = Double.parseDouble(o2[2]);
					if (detect1 > detect2) {
						return 1;
					}
					if (detect1 < detect2) {
						return -1;
					}

					return 0;

				} else {
					return genome1.compareTo(genome2);
				}
			}
		});
		HashMap<String, Integer> genomePepCountMap = new HashMap<String, Integer>();
		for (int i = 0; i < peptideContents.length; i++) {
			String genome = peptideContents[i][0].substring(0, peptideContents[i][0].indexOf("_"));
			if (genomePepCountMap.containsKey(genome)) {
				genomePepCountMap.put(genome, genomePepCountMap.get(genome) + 1);
			} else {
				genomePepCountMap.put(genome, 1);
			}
		}

		DecimalFormat df4 = FormatTool.getDF4();
		File outputFile = new File(projectFile, modelName + "_pep_rank.tsv");
		try {
			PrintWriter writer = new PrintWriter(outputFile);
			for (int i = 0; i < peptideContents.length; i++) {
				StringBuilder sb = new StringBuilder();
				String genome = peptideContents[i][0].substring(0, peptideContents[i][0].indexOf("_"));
				double genome_rank_ratio = (double) (i + 1) / (double) genomePepCountMap.get(genome);
				sb.append(peptideContents[i][0]).append("\t").append(peptideContents[i][1]).append("\t")
						.append(df4.format(genome_rank_ratio));
				writer.println(sb);
			}
			writer.close();

		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the peptide rank information to " + outputFile);
			return -1;
		}

		return pepMap.size();
	}
	
	private static void compare(String s1, String s2, String out) {

		HashMap<String, Double> map1 = new HashMap<String, Double>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(new File(s1)))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split(",");
				map1.put(cs[1], Double.parseDouble(cs[2]));
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
		
		HashMap<String, Double> map2 = new HashMap<String, Double>();
		try (BufferedReader pepProReader = new BufferedReader(new FileReader(new File(s2)))) {
			String line = pepProReader.readLine();
			while ((line = pepProReader.readLine()) != null) {
				String[] cs = line.split(",");
				map2.put(cs[1], Double.parseDouble(cs[2]));
			}
			pepProReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
		System.out.println(map1.size()+"\t"+map2.size());
		try (PrintWriter writer = new PrintWriter(new File(out))) {
			writer.println("Nor\tNonNor");
			for(String pep: map1.keySet()) {
				if(map2.containsKey(pep)) {
					writer.println(map1.get(pep)+"\t"+map2.get(pep));
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}

	}
	
	private static void quickTest(String in) {
		MetaParameterDia metaPar = MetaParaIoDia.parse("E:\\Exported\\exe\\parameters_DIA_1_0.json");
		metaPar.setThreadCount(16);
		MagDbItem[][] mdbs = MagDbConfigIO.parse("E:\\Exported\\exe\\MAGDB_1_1.json");
		for (int i = 0; i < mdbs.length; i++) {
			for (int j = 0; j < mdbs[i].length; j++) {
				if (mdbs[i][j].getCatalogueID().equals("human-gut")) {
					mdbs[i][j].setCurrentFile(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2"));
					metaPar.setUsedMagDbItem(mdbs[i][j]);
					System.out.println(metaPar.getUsedMagDbItem().getCurrentFile());
					System.out.println(metaPar.getUsedMagDbItem().getRepositoryFile());
					System.out.println(metaPar.getUsedMagDbItem().getOriginalDbFolder());
				}
			}
		}
/*
		File firstFile = new File(in, "firstSearch");
		File firstTsvFile = new File(firstFile, "firstSearch.tsv");
		HashMap<String, Double> map = new HashMap<String, Double>();
		try (BufferedReader reader = new BufferedReader(new FileReader(firstTsvFile))) {
			String line = reader.readLine();
			String[] title = line.split("\t");
			int id = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Precursor.Normalised")) {
					id = i;
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
		} catch (IOException e) {

		}
		
		long start = System.currentTimeMillis();

		File modelFile = new File(in, "modelSQL");
		modelFile.mkdir();
		
		MetaSourcesDia msd = MetaSourcesIoDia.parse("E:\\Exported\\exe\\resources_DIA_1_0.json");
		DiaModelTask task = new DiaModelTask(metaPar, msd, new JProgressBar(), map,
				"selfModel", RNN, modelFile) {

			@Override
			protected void done() {
				// TODO Auto-generated method stub
				super.done();
				System.out.println("done");
			}
		};
		task.execute();

		try {
			boolean modelFinished = task.get();
			if (!modelFinished) {
				System.out.println("no");
			} else {
				System.out.println("yes");
			}
		} catch (Exception e) {

		}
		
		long end = System.currentTimeMillis();
		System.out.println((end-start)/60000.0);
*/		
	}

	public static void main(String[] args) {

//		DiaModelTask.quickTest("Z:\\Kai\\Raw_files\\p1\\Adrian_20240606_AD2_Plate1_A03");
		
//		DiaModelTask.evaluteTest(new File("Z:\\Kai\\T640_models\\PXD012725"));
//		DiaModelTask.evaluteTest(new File("Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD012725_RNN_weight"));
		
//		DiaModelTask.compare("Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD005780_RNN\\PXD005780_RNN_predicted_all.tsv", 
//				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD005780\\PXD005780_predicted_all.tsv", 
//				"Z:\\Kai\\Database\\human-gut\\v2.0.2\\models\\PXD005780_RNN\\compare.tsv");
		
//		String[] ss = { "wmic", "cpu", "get", "NumberOfCores,NumberOfLogicalProcessors" };
		String[] ss = { "wmic", "cpu", "get", "NumberOfLogicalProcessors" };
		ProcessBuilder pb = new ProcessBuilder(ss);
		Process p;
		try {
			p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			int total = 0;
			L: while ((lineStr = inBr.readLine()) != null) {
				String trimLine = lineStr.trim();
				if (trimLine.length() > 0) {
					int oneCore = 0;
					for (int i = 0; i < trimLine.length(); i++) {
						char aa = trimLine.charAt(i);
						if (aa >= '0' && aa <= '9') {
							
							oneCore += ((int)(aa-'0'))*Math.pow(10, (trimLine.length()-i-1));
							
							System.out.println(aa+"\t"+(int)(aa-'0')+"\t"+oneCore);
						} else {
							continue L;
						}
					}
					
					total += oneCore;
				}
			}
			System.out.println("Total\t"+total);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
