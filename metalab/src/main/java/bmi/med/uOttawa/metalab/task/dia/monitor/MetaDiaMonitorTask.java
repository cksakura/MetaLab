package bmi.med.uOttawa.metalab.task.dia.monitor;

import javax.swing.DefaultListModel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;
import bmi.med.uOttawa.metalab.task.dia.DiaModelTask;
import bmi.med.uOttawa.metalab.task.dia.MetaDiaTask;
import bmi.med.uOttawa.metalab.task.dia.monitor.par.MetaParameterDiaRT;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MetaDiaMonitorTask extends MetaDiaTask {
	private final Path monitorFolderPath;
	private final Path destinationFolderPath;

	private File monitorFolder;
	private File destinationFolder;
	private ExecutorService executorService;
	private HashSet<String> fileNameSet;
	private DefaultTableModel resultTableModel;
	private DefaultListModel<String> rawListModel;
	
	private DiaLibSearchPar diaLibSearchPar;
	private DiannParameter diannPar;
	private File libFolderFile;
	private ArrayList<String> libPathList;
	private ArrayList<String> modelPathList;
	
	private static final Logger LOGGER = LogManager.getLogger(MetaDiaMonitorTask.class);
	private static final String taskName = "Realtime DIA data analysis";

	public MetaDiaMonitorTask(MetaParameterDiaRT metaPar, MetaSourcesDia msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork, File monitorFolder, File destinationFolder,
			DefaultTableModel resultTableModel, DefaultListModel<String> rawListModel) {

		super(metaPar, msv, bar1, bar2, nextWork);
		this.monitorFolderPath = Paths.get(monitorFolder.getAbsolutePath());
		this.destinationFolderPath = Paths.get(destinationFolder.getAbsolutePath());

		this.monitorFolder = monitorFolder;
		this.destinationFolder = destinationFolder;

		this.resultTableModel = resultTableModel;
		this.rawListModel = rawListModel;
	}

	protected void initial() {
		
		this.magDb = ((MetaParameterMag) metaPar).getUsedMagDbItem();
		this.threadCount = metaPar.getThreadCount();

		this.diaLibSearchPar = new DiaLibSearchPar();
		this.diaLibSearchPar.setThreadCount(metaPar.getThreadCount());
		
		this.diannPar = new DiannParameter();
		this.diannPar.setThreads(metaPar.getThreadCount());

		setTaskCount();
	}

	@Override
	protected Boolean doInBackground() throws IOException {

		File magDbFile = this.magDb.getCurrentFile();
		this.libFolderFile = new File(magDbFile, "libraries");

		MetaParameterDiaRT diaPar = (MetaParameterDiaRT) metaPar;
		String[] usedLibs = diaPar.getLibrary();
		this.libPathList = new ArrayList<String>();

		for (int i = 0; i < usedLibs.length; i++) {
			if (!usedLibs[i].equals("host")) {
				libPathList.add(usedLibs[i]);
			}
		}

		File modelFolderFile = new File(magDbFile, "models");
		String[] usedModels = diaPar.getModels();
		this.modelPathList = new ArrayList<String>();
		for (int i = 0; i < usedModels.length; i++) {
			File modelFile = new File(new File(modelFolderFile, usedModels[i]), usedModels[i] + "_pep_rank.tsv");
			modelPathList.add(modelFile.getAbsolutePath());
		}

		executorService = Executors.newFixedThreadPool(maxTaskCount);

		this.fileNameSet = new HashSet<String>();

		List<String> diaFileList = new ArrayList<>();
		List<Future<String>> futures = new ArrayList<>();

		File[] files = monitorFolder.listFiles();
		if (files.length > 0) {
			LOGGER.info(
					getTaskName() + ": " + files.length + " files already in the monitor folder, processing started");

			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": " + files.length
					+ " files already in the monitor folder, processing started");

			for (int i = 0; i < files.length; i++) {
				final int index = i;
				Future<String> future = executorService.submit(() -> process(files[index]));
				futures.add(future);
			}
		}

		WatchService watchService = FileSystems.getDefault().newWatchService();
		monitorFolderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

		while (!isCancelled()) {

			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path filename = ev.context();
				Path filePath = monitorFolderPath.resolve(filename);

				if (Files.isRegularFile(filePath)) {

					LOGGER.info(getTaskName() + ": found new file " + filePath);

					Future<String> future = executorService.submit(() -> waitForFileCompletionAndProcess(filePath));
					futures.add(future);
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				break;
			}

			MetaParameterDiaRT parDiaRT = ((MetaParameterDiaRT) metaPar);
			boolean combine = parDiaRT.isCombineResult();

			if (parDiaRT.isStopNow()) {
				if (combine) {
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": the monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will be performed based on the combined result.");
					LOGGER.info(getTaskName()
							+ ": the monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will be performed based on the combined result.");
				} else {
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": the monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will NOT be performed.");
					LOGGER.info(getTaskName()
							+ ": the monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will NOT be performed.");
				}

				break;
			}
			if (parDiaRT.isStopComplete()) {
				int totalCount = parDiaRT.getTaskCount();
				if (combine) {
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": the total number of the files was set as " + totalCount
							+ ", the monitoring will stop after all " + totalCount
							+ " files are detected, the processing will stop after all the current tasks are finished."
							+ "The taxonomic and functional analysis will be performed based on the combined result.");
					LOGGER.info(getTaskName() + ": the total number of the files was set as " + totalCount
							+ ", the monitoring will stop after all " + totalCount
							+ " files are detected, the processing will stop after all the current tasks are finished."
							+ "The taxonomic and functional analysis will be performed based on the combined result.");
				} else {

					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": the total number of the files was set as " + totalCount
							+ ", the monitoring will stop after all " + totalCount
							+ " files are detected, the processing will stop after all the current tasks are finished."
							+ "The taxonomic and functional analysis will NOT be performed.");
					LOGGER.info(getTaskName() + ": the total number of the files was set as " + totalCount
							+ ", the monitoring will stop after all " + totalCount
							+ " files are detected, the processing will stop after all the current tasks are finished."
							+ "The taxonomic and functional analysis will NOT be performed.");

				}

				if (this.fileNameSet.size() >= totalCount) {

					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": The file count was set as "
							+ totalCount + " and " + this.fileNameSet.size() + " files have been detected."
							+ " The monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will NOT be performed.");
					LOGGER.info(getTaskName() + ": The file count was set as " + totalCount + " and "
							+ this.fileNameSet.size() + " files have been detected."
							+ " The monitoring is stopped, the processing will stop after all the current tasks are finished. "
							+ "The taxonomic and functional analysis will NOT be performed.");

					break;
				}
			}
		}
		executorService.shutdown();

		for (Future<String> future : futures) {
			try {
				String result = future.get();
				if (result != null) {
					diaFileList.add(result);
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		this.diaFiles = diaFileList.toArray(String[]::new);
		Arrays.sort(diaFiles);

		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": the processing of all the raw files finished");
		LOGGER.info(getTaskName() + ": the processing of all the raw files finished");

		if (diaPar.isCombineResult()) {
			boolean iden = iden();
			if (!iden) {
				LOGGER.error(getTaskName() + ": error in peptide and protein identification");
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in peptide and protein identification");
				return false;
			}

			setProgress(90);

			boolean quant = false;
			if (this.quanMode.equals(MetaConstants.labelFree)) {
				quant = lfQuant();
				isIsobaric = false;
				if (!quant) {
					LOGGER.error(getTaskName() + ": error in peptide and protein quantification");
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in peptide and protein quantification");
					return false;
				}
			} else if (this.quanMode.equals(MetaConstants.isobaricLabel)) {
				quant = lfQuant();
				isIsobaric = true;
				if (!quant) {
					LOGGER.error(getTaskName() + ": error in peptide and protein quantification");
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in peptide and protein quantification");
					return false;
				}

				setProgress(95);

				quant = isobaricQuant();
				if (!quant) {
					LOGGER.error(getTaskName() + ": error in peptide and protein quantification");
					System.err.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in peptide and protein quantification");
					return false;
				}
			}

			exportReport();
		}

		this.separateResultFolders = new File[fileNameSet.size()];
		int fildId = 0;
		for (String fName : fileNameSet) {
			this.separateResultFolders[fildId++] = new File(destinationFolder, fName);
		}
		this.resultFolderFile = new File(destinationFolder, "combined");

		cleanTempFiles();

		setProgress(100);

		return true;
	}
	
	private String process(File file) {
		try {

			String fileAbPath = file.getAbsolutePath();
			String fileName = file.getName();

			LOGGER.info(getTaskName() + ": processing raw files " + fileName + " started");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": processing raw files " + fileName
					+ " started");

			DiaNNTask diaNNTask = new DiaNNTask(((MetaSourcesDia) msv).getDiann());
			diannPar.setThreads(threadCount / maxTaskCount + 1);

			File diaFile = new File(fileAbPath);
			if (fileName.endsWith(".raw") || fileName.endsWith(".RAW")) {

				diaFile = new File(file + ".dia");
				fileName = fileName.substring(0, fileName.length() - 4);
				fileNameSet.add(fileName);

				if (!diaFile.exists()) {
					LOGGER.info(getTaskName() + ": converting raw files " + fileName + " started");
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw files "
							+ fileName + " started");

					diaNNTask.addTask(diannPar, new String[] { fileAbPath });
					diaNNTask.run(1);

					LOGGER.info(getTaskName() + ": converting raw files " + fileName + " finished");
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw files "
							+ fileName + " finished");
				}
				return null;

			} else if (fileName.endsWith(".d") || fileName.endsWith(".D")) {
				fileName = fileName.substring(0, fileName.length() - 2);
				fileNameSet.add(fileName);
			} else if (fileName.endsWith(".dia") || fileName.endsWith(".DIA")) {
				if (fileName.endsWith(".raw.dia") || fileName.endsWith(".RAW.DIA")) {
					fileName = fileName.substring(0, fileName.length() - 8);
				} else if (fileName.endsWith(".raw.d") || fileName.endsWith(".RAW.D")) {
					fileName = fileName.substring(0, fileName.length() - 6);
				} else {
					fileName = fileName.substring(0, fileName.length() - 4);
				}
				fileNameSet.add(fileName);
			} else {
				return null;
			}

			final String finalFileName = fileName;
			SwingUtilities.invokeLater(() -> {
				boolean add1 = true;
				for (int i = 0; i < rawListModel.size(); i++) {
					String fileNameI = rawListModel.get(i);
					if (fileNameI.equals(finalFileName)) {
						add1 = false;
						break;
					}
				}
				if (add1) {
					this.rawListModel.addElement(finalFileName);
				}

				boolean add2 = true;
				for (int i = 0; i < resultTableModel.getRowCount(); i++) {
					String fileNameI = (String) resultTableModel.getValueAt(i, 0);
					if (fileNameI.equals(finalFileName)) {
						add2 = false;
						break;
					}
				}

				if (add2) {
					String[] content = new String[] { finalFileName, "Waiting", "Waiting", "Waiting", "Waiting",
							"Waiting" };
					this.resultTableModel.addRow(content);
				}
			});

			File resultFolderFile = new File(destinationFolderPath.toAbsolutePath().toString(), fileName);
			if (!resultFolderFile.exists()) {
				resultFolderFile.mkdir();
			}

			HashSet<String> initialLibPepSet = new HashSet<String>();
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
						initialLibPepSet.add(cs[seqid]);
					}
					reader.close();
				} catch (Exception e) {
					LOGGER.error(getTaskName() + ": error in reading " + libTsvFile, e);
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + libTsvFile);

					return null;
				}
			}

			File firstFile = new File(resultFolderFile, "firstSearch");
			if (!firstFile.exists()) {
				firstFile.mkdirs();
			}

			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < resultTableModel.getRowCount(); i++) {
					String fileNameI = (String) resultTableModel.getValueAt(i, 0);
					if (fileNameI.equals(finalFileName)) {
						resultTableModel.setValueAt("Working", i, 1);
						break;
					}
				}
			});

			File round1TsvFile = new File(firstFile, "firstSearch.tsv");
			if (!round1TsvFile.exists()) {

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 1 started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " round 1 started");

				int batchThreadCount = threadCount / maxTaskCount + 1;
				DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
				diaLibSearchPar.setDiaFiles(new String[] { diaFile.getAbsolutePath() });
				diaLibSearchPar.setThreadCount(batchThreadCount);
				diaLibSearchPar.setMbr(false);

				DiannParameter diannPar = new DiannParameter();
				diannPar.setThreads(batchThreadCount);
				diannPar.setMatrices(false);
				diannPar.setRelaxed_prot_inf(true);
				diaNNTask.addTask(diannPar, diaLibSearchPar, round1TsvFile.getAbsolutePath(), libPath);
				diaNNTask.run(12);

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 1 finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": converting raw files "
						+ fileName + " finished");
			} else {
				LOGGER.info(
						getTaskName() + ": round 1 search result for " + fileName + " existed, go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 1 search result for "
						+ fileName + " existed, go to the next step");
			}

			if (round1TsvFile.exists()) {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Done", i, 1);
							break;
						}
					}
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Failed", i, 1);
							break;
						}
					}
				});
				return null;
			}

			File modelFolderFile = new File(resultFolderFile, "model");
			if (!modelFolderFile.exists()) {
				modelFolderFile.mkdirs();
			}

			File rankedPepFile = new File(modelFolderFile, "model_pep_rank.tsv");
			if (!rankedPepFile.exists()) {

				LOGGER.info(getTaskName() + ": creating model for " + fileName + " started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": creating model for " + fileName
						+ " started");

				HashMap<String, Double> pepIntenMap = new HashMap<String, Double>();
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
				int batchThreadCount = threadCount / maxTaskCount + 1;
				DiaModelTask diaModelTask = new DiaModelTask((MetaParameterDia) metaPar, (MetaSourcesDia) msv, bar1,
						pepIntenMap, "model", DiaModelTask.RNN, modelFolderFile, batchThreadCount);
				diaModelTask.execute();
				
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Working", i, 2);
							break;
						}
					}
				});

				try {
					if (diaModelTask.get()) {
						LOGGER.info(getTaskName() + ": creating model for " + fileName + " finished");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": creating model for "
								+ fileName + " finished");

					} else {
						LOGGER.info(getTaskName() + ": creating model for " + fileName + " failed");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": creating model for "
								+ fileName + " failed");
					}

				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					LOGGER.error(getTaskName() + ": creating model for " + fileName + " failed", e);
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": creating model for "
							+ fileName + " failed");
				}
			} else {
				LOGGER.info(getTaskName() + ": model for " + fileName + " existed, go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": model for " + fileName
						+ " existed, go to the next step");
			}

			if (rankedPepFile.exists()) {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Done", i, 2);
							break;
						}
					}
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Failed", i, 2);
							break;
						}
					}
				});
				return null;
			}

			ArrayList<String> modelListI = new ArrayList<String>(modelPathList);
			modelListI.add(rankedPepFile.getAbsolutePath());
			String[] modelPath = modelListI.toArray(String[]::new);

			File round1DbFile = new File(firstFile, "firstSearch.fasta");
			if (!round1DbFile.exists() || round1DbFile.length() == 0) {
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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round1TsvFile);
					return null;
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				HashSet<String> genomeSet1 = getGenomeSet(firstFile, 0.85, pepSet1, batchThreadCount);

				LOGGER.info(getTaskName() + ": generating database for " + fileName + ", " + pepSet1.size()
						+ " peptide sequences were identified, " + genomeSet1.size() + " MAGs were used in this step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database for "
						+ fileName + ", " + pepSet1.size() + " peptide sequences were identified, " + genomeSet1.size()
						+ " MAGs were used in this step");

				dbFromModelPeptide(modelPath, genomeSet1, pepSet1, round1DbFile, 1500000);
			}

			File secondFile = new File(resultFolderFile, "secondSearch");
			if (!secondFile.exists()) {
				secondFile.mkdirs();
			}

			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < resultTableModel.getRowCount(); i++) {
					String fileNameI = (String) resultTableModel.getValueAt(i, 0);
					if (fileNameI.equals(finalFileName)) {
						resultTableModel.setValueAt("Working", i, 3);
						break;
					}
				}
			});

			File round2TsvFile = new File(secondFile, "secondSearch.tsv");
			if (!round2TsvFile.exists()) {

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 2 started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " round 2 started");

				File round2LibFile = new File(secondFile, "secondSearch.speclib");
				if (!round2LibFile.exists()) {
					File tempLibFile = new File(secondFile, "secondSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round2LibFile);
					} else {
						int batchThreadCount = threadCount / maxTaskCount + 1;
						diannPar.setThreads(batchThreadCount);
						diaNNTask.addTask(diannPar, round1DbFile.getAbsolutePath(), round2LibFile.getAbsolutePath(),
								false);
						diaNNTask.run(12);
					}
				}

				if (!round2LibFile.exists()) {
					File tempLibFile = new File(secondFile, "secondSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round2LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round2LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round2LibFile + " is not found");

					}
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
				diaLibSearchPar.setDiaFiles(new String[] { diaFile.getAbsolutePath() });
				diaLibSearchPar.setThreadCount(batchThreadCount);
				diaLibSearchPar.setMbr(false);

				DiannParameter diannPar = new DiannParameter();
				diannPar.setThreads(batchThreadCount);
				diannPar.setMatrices(false);
				diannPar.setRelaxed_prot_inf(true);
				diaNNTask.addTask(diannPar, new String[] { diaFile.getAbsolutePath() }, round2TsvFile.getAbsolutePath(),
						round2LibFile.getAbsolutePath());
				diaNNTask.run(12);

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 2 finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " round 2 finished");

			} else {
				LOGGER.info(
						getTaskName() + ": round 2 search result for " + fileName + " existed, go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 2 search result for "
						+ fileName + " existed, go to the next step");
			}

			if (round2TsvFile.exists()) {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Done", i, 3);
							break;
						}
					}
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Failed", i, 3);
							break;
						}
					}
				});
				return null;
			}

			File round2DbFile = new File(secondFile, "secondSearch.fasta");
			if (!round2DbFile.exists() || round2DbFile.length() == 0) {

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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round1TsvFile);
					return null;
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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round2TsvFile);
					return null;
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				HashSet<String> genomeSet2 = getGenomeSet(secondFile, 0.9, pepSet2, batchThreadCount);

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
					return null;
				}

				excludeSet.retainAll(initialLibPepSet);
				Iterator<String> it = excludeSet.iterator();
				while (it.hasNext()) {
					String pep = it.next();
					if (pepSet2.contains(pep)) {
						it.remove();
					}
				}

				LOGGER.info(getTaskName() + ": generating database for " + fileName + ", " + pepSet2.size()
						+ " peptide sequences were identified, " + genomeSet2.size()
						+ " MAGs were used in this step, the size of the excluded set is " + excludeSet.size());
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database for "
						+ fileName + ", " + pepSet2.size() + " peptide sequences were identified, " + genomeSet2.size()
						+ " MAGs were used in this step, the size of the excluded set is " + excludeSet.size());

				dbFromModelPeptide(modelPath, genomeSet2, pepSet2, excludeSet, round2DbFile, 2000000);
			}

			File thirdFile = new File(resultFolderFile, "thirdSearch");
			if (!thirdFile.exists()) {
				thirdFile.mkdirs();
			}

			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < resultTableModel.getRowCount(); i++) {
					String fileNameI = (String) resultTableModel.getValueAt(i, 0);
					if (fileNameI.equals(finalFileName)) {
						resultTableModel.setValueAt("Working", i, 4);
						break;
					}
				}
			});

			File round3TsvFile = new File(thirdFile, "thirdSearch.tsv");
			if (!round3TsvFile.exists()) {
				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 3 started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " round 3 started");
				File round3LibFile = new File(thirdFile, "thirdSearch.speclib");
				if (!round3LibFile.exists()) {
					File tempLibFile = new File(thirdFile, "thirdSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round3LibFile);
					} else {
						int batchThreadCount = threadCount / maxTaskCount + 1;
						diannPar.setThreads(batchThreadCount);
						diaNNTask.addTask(diannPar, round2DbFile.getAbsolutePath(), round3LibFile.getAbsolutePath(),
								false);
						diaNNTask.run(12);
					}
				}
				if (!round3LibFile.exists()) {
					File tempLibFile = new File(thirdFile, "thirdSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round3LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round3LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round3LibFile + " is not found");
					}
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
				diaLibSearchPar.setThreadCount(batchThreadCount);
				diaLibSearchPar.setMbr(false);

				DiannParameter diannPar = new DiannParameter();
				diannPar.setThreads(batchThreadCount);
				diannPar.setMatrices(false);
				diannPar.setRelaxed_prot_inf(true);
				diaNNTask.addTask(diannPar, new String[] { diaFile.getAbsolutePath() }, round3TsvFile.getAbsolutePath(),
						round3LibFile.getAbsolutePath());
				diaNNTask.run(12);

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " round 3 finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " round 3 finished");
			} else {
				LOGGER.info(
						getTaskName() + ": round 3 search result for " + fileName + " existed, go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": round 3 search result for "
						+ fileName + " existed, go to the next step");
			}

			if (round3TsvFile.exists()) {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Done", i, 4);
							break;
						}
					}
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Failed", i, 4);
							break;
						}
					}
				});
				return null;
			}

			File round3DbFile = new File(thirdFile, "thirdSearch.fasta");
			if (!round3DbFile.exists() || round3DbFile.length() == 0) {

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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round1TsvFile);
					return null;
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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round2TsvFile);
					return null;
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
					System.err.println(
							format.format(new Date()) + "\t" + getTaskName() + ": error in reading " + round3TsvFile);
					return null;
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				HashSet<String> genomeSet3 = getGenomeSet(thirdFile, 0.95, pepSet3, batchThreadCount);

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
					return null;
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
					return null;
				}

				firstSet.retainAll(secondSet);
				excludeSet.retainAll(initialLibPepSet);
				excludeSet.addAll(firstSet);
				Iterator<String> it = excludeSet.iterator();
				while (it.hasNext()) {
					String pep = it.next();
					if (pepSet3.contains(pep)) {
						it.remove();
					}
				}

				LOGGER.info(getTaskName() + ": generating database for " + fileName + ", " + genomeSet3.size()
						+ " MAGs were used in this step, the size of the excluded set is " + excludeSet.size());
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": generating database for "
						+ fileName + ", " + genomeSet3.size()
						+ " MAGs were used in this step, the size of the excluded set is " + excludeSet.size());

				dbFromModelPeptide(modelPath, genomeSet3, pepSet3, excludeSet, round3DbFile, 2500000);
			}

			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < resultTableModel.getRowCount(); i++) {
					String fileNameI = (String) resultTableModel.getValueAt(i, 0);
					if (fileNameI.equals(finalFileName)) {
						resultTableModel.setValueAt("Working", i, 5);
						break;
					}
				}
			});

			File lastTsvFile = new File(resultFolderFile, "lastSearch.tsv");
			if (!lastTsvFile.exists()) {

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " the last round started");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " the last round started");

				File round4LibFile = new File(resultFolderFile, "lastSearch.speclib");
				if (!round4LibFile.exists()) {
					File tempLibFile = new File(resultFolderFile, "lastSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round4LibFile);
					} else {
						int batchThreadCount = threadCount / maxTaskCount + 1;
						diannPar.setThreads(batchThreadCount);
						diaNNTask.addTask(diannPar, round3DbFile.getAbsolutePath(), round4LibFile.getAbsolutePath(),
								false);
						diaNNTask.run(12);
					}
				}
				if (!round4LibFile.exists()) {
					File tempLibFile = new File(resultFolderFile, "lastSearch.predicted.speclib");
					if (tempLibFile.exists()) {
						tempLibFile.renameTo(round4LibFile);
					} else {
						LOGGER.error(getTaskName() + ": library file " + round4LibFile + " is not found");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": library file "
								+ round4LibFile + " is not found");
					}
				}
				int batchThreadCount = threadCount / maxTaskCount + 1;
				DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
				diaLibSearchPar.setThreadCount(batchThreadCount);
				diaLibSearchPar.setMbr(false);

				DiannParameter diannPar = new DiannParameter();
				diannPar.setThreads(batchThreadCount);
				diannPar.setMatrices(false);
				diannPar.setRelaxed_prot_inf(true);
				diaNNTask.addTask(diannPar, new String[] { diaFile.getAbsolutePath() }, lastTsvFile.getAbsolutePath(),
						round4LibFile.getAbsolutePath());
				diaNNTask.run(12);

				LOGGER.info(getTaskName() + ": search raw files " + fileName + " the last round finished");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": search raw files " + fileName
						+ " the last round finished");
			} else {
				LOGGER.info(getTaskName() + ": the last round search result for " + fileName
						+ " existed, go to the next step");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": the last round search result for " + fileName + " existed, go to the next step");
			}

			if (lastTsvFile.exists()) {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Done", i, 5);
							break;
						}
					}
				});
			} else {
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < resultTableModel.getRowCount(); i++) {
						String fileNameI = (String) resultTableModel.getValueAt(i, 0);
						if (fileNameI.equals(finalFileName)) {
							resultTableModel.setValueAt("Failed", i, 5);
							break;
						}
					}
				});
				return null;
			}

			int count = 0;
			File[] destFiles = destinationFolder.listFiles();
			for (int i = 0; i < destFiles.length; i++) {
				if (destFiles[i].isDirectory()) {
					File resultFileI = new File(destFiles[i], "lastSearch.tsv");
					if (resultFileI.exists() && resultFileI.length() > 0) {
						count++;
					}
				}
			}

			setProgress((int) ((double) count / (double) this.rawListModel.size() * 70.0));

			return diaFile.getAbsolutePath();

		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return null;
	}

	private String waitForFileCompletionAndProcess(Path filePath) {
		try {
			long previousSize = -1;
			while (true) {
				// Check the file size at regular intervals
				long currentSize = Files.size(filePath);
				if (currentSize == previousSize) {
					// Try to acquire an exclusive lock to see if any other process is still writing
					try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.WRITE)) {
						FileLock lock = fileChannel.tryLock();
						if (lock != null) {
							lock.release(); // Release the lock immediately
							break; // File is not in use
						}
					} catch (Exception e) {
						// The file is still in use or another process is writing to it
						LOGGER.info(getTaskName() + ": detect file " + filePath.toAbsolutePath().toString()
								+ ", waiting for the copy complete");
						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": detect file "
								+ filePath.toAbsolutePath().toString() + ", waiting for the copy complete");
					}
				}
				previousSize = currentSize;
				Thread.sleep(1000); // Wait for 1 second before checking again
			}

//			Path destinationPath = destinationFolder.resolve(file.getFileName());
//			Files.copy(file, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			return process(new File(filePath.toAbsolutePath().toString()));

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		return null;
	}

	@Override
	protected void done() {
		try {
			if (!isCancelled()) {
				get(); // Trigger any exceptions thrown in doInBackground
				// Update GUI with completion status
				System.out.println("Monitoring completed. Total files processed: " + rawListModel.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
