/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import uk.ac.ebi.pride.spectracluster.binning.BinningSpectrumConverter;
import uk.ac.ebi.pride.spectracluster.cli.SpectraClusterCliMain;
import uk.ac.ebi.pride.spectracluster.clustering.BinaryClusterFileReference;
import uk.ac.ebi.pride.spectracluster.clustering.BinaryFileClusterer;
import uk.ac.ebi.pride.spectracluster.conversion.MergingCGFConverter;
import uk.ac.ebi.pride.spectracluster.implementation.ClusteringSettings;
import uk.ac.ebi.pride.spectracluster.implementation.SpectraClusterStandalone;
import uk.ac.ebi.pride.spectracluster.merging.BinaryFileMergingClusterer;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.function.peak.BinnedHighestNPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.function.peak.HighestNPeakFunction;
import uk.ac.ebi.pride.spectracluster.util.function.spectrum.RemoveReporterIonPeaksFunction;

/**
 * @author Kai Cheng
 * @version 2025.03.20
 */
public class ClusterTask {

	private int threadCount;
	private boolean fastMode;
	private IsobaricTag isobaricTag;

	private float startThreshold = 0.999F;
	private float endThreshold = 0.99f;
	private int rounds = 4;
	private int nHighestPeaks = 70;

	private static Logger LOGGER = LogManager.getLogger(ClusterTask.class);
	private static final String taskName = "Spectra clustering";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public ClusterTask(int threadCount, boolean fastMode) {
		this.threadCount = threadCount;
		this.fastMode = fastMode;
		this.isobaricTag = null;
	}

	public ClusterTask(int threadCount, boolean fastMode, IsobaricTag isobaricTag) {
		this.threadCount = threadCount;
		this.fastMode = fastMode;
		this.isobaricTag = isobaricTag;
		
		this.startThreshold = 0.999F;
		this.endThreshold = 0.99f;
	}

	public ClusterTask(int threadCount, boolean fastMode, IsobaricTag isobaricTag, String workflow) {
		this.threadCount = threadCount;
		this.fastMode = fastMode;
		this.isobaricTag = isobaricTag;

		this.startThreshold = 0.999F;
		this.endThreshold = 0.99f;
	}
	
	public void runCli(String[] fileNames, File clusterDir, File resultMgf) {
		runCli(fileNames, clusterDir, resultMgf, false);
	}

	public void runCli(String[] fileNames, File clusterDir, File resultMgf, boolean singleFile) {

		if (!clusterDir.exists()) {
			clusterDir.mkdirs();
		}

		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < fileNames.length; i++) {
			list.add(fileNames[i]);
		}

		if (fastMode) {
			list.add("-fast_mode");
		}

		list.add("-filter");
		list.add("mz_150");

		list.add("-rounds");
		list.add("4");

		list.add("-binary_directory");
		list.add(clusterDir.getAbsolutePath());

		// FILTER PEAKS PER MZ new BinnedHighestNPeakFunction(10, 100, 30)
		list.add("-x_filter_peaks_mz");

		list.add("-threshold_start");
		list.add(String.valueOf(startThreshold));

		list.add("-threshold_end");
		list.add(String.valueOf(endThreshold));

		File cfgFile = new File(resultMgf.getParent(), "cluster.cfg");
		list.add("-output_path");
		list.add(cfgFile.getAbsolutePath());

		list.add("-major_peak_jobs");
		list.add(String.valueOf(threadCount));

		if (isobaricTag != null) {
			String name = isobaricTag.getName();
			if (name.startsWith("iTRAQ")) {
				list.add("-remove_reporters");
				list.add("itraq");

			} else {
				list.add("-remove_reporters");
				list.add("all");
			}
		}

		String[] args = list.toArray(new String[list.size()]);
		SpectraClusterCliMain.main(args);

		System.gc();

		File[] generatedFiles = clusterDir.listFiles();
		File cgfFile = null;
		for (int i = 0; i < generatedFiles.length; i++) {
			if (generatedFiles[i].isDirectory()) {
				File[] subFiles = generatedFiles[i].listFiles();
				for (int j = 0; j < subFiles.length; j++) {
					FileUtils.deleteQuietly(subFiles[j]);
				}
				FileUtils.deleteQuietly(generatedFiles[i]);

			} else if (generatedFiles[i].isFile()) {
				if (generatedFiles[i].getName().endsWith("cgf")) {
					cgfFile = generatedFiles[i];
				} else {
					FileUtils.deleteQuietly(generatedFiles[i]);
				}
			}
		}

		int clusterCount = 0;

		try {
			clusterCount = CGF2MGF.convert2Mgf(fileNames, cgfFile, resultMgf, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in converting the CGF file to MGF file.");
			LOGGER.error(taskName + ": error in converting the CGF file to MGF file.", e);
		}

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": clustered MS/MS spectra count: " + clusterCount);
		LOGGER.info(taskName + ": clustered MS/MS spectra count: " + clusterCount);
	}

	/**
	 * @deprecated
	 * @param fileNames
	 * @param clusterDir
	 * @param log
	 * @param resultName
	 */
	public void clustering(String[] fileNames, File clusterDir, Appendable log, String resultName) {

		try {
			log.append(format.format(new Date()) + "\tclustering started\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
		if (!clusterDir.exists()) {
			clusterDir.mkdirs();
		}

		int totalCount = 0;
		for (int i = 0; i < fileNames.length; i++) {
			File filei = new File(fileNames[i]);
			int count = 0;

			String line = null;
			try (BufferedReader reader = new BufferedReader(new FileReader(filei))) {
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("BEGIN IONS")) {
						count++;
					}
				}
				reader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading mgf file " + filei.getName(), e);
			}

			totalCount += count;

			try {
				log.append(format.format(new Date()) + "\t" + filei.getName() + " MS/MS count: " + count + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in writing log file", e);
			}
		}
		try {
			log.append(format.format(new Date()) + "\tTotal MS/MS count: " + totalCount + "\n");
			log.append(format.format(new Date()) + "\tGenerating binning spectrum started\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		BinningSpectrumConverter binningSpectrumConverter = new BinningSpectrumConverter(clusterDir, threadCount,
				fastMode);

		try {
			binningSpectrumConverter.processPeaklistFiles(fileNames);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}
		List<BinaryClusterFileReference> binaryFiles = binningSpectrumConverter.getWrittenFiles();

		File clusteringResultDirectory = createTemporaryDirectory("clustering_results", clusterDir);
		File tmpClusteringResultDirectory = createTemporaryDirectory("clustering_results", clusterDir);
		List<Float> thresholds = generateThreshold(0.999f, 0.99f, 4);

		try {
			log.append(format.format(new Date()) + "\tThresholds: ");
			for (Float f : thresholds) {
				log.append(String.valueOf(f)).append("\t");
			}
			log.append("\n");

			log.append(format.format(new Date()) + "\tClustering MS/MS spectra started\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

//		BinaryFileClusterer binaryFileClusterer = new BinaryFileClusterer(threadCount, clusteringResultDirectory,
//				thresholds, fastMode, tmpClusteringResultDirectory, rtRestrict, null);

		BinaryFileClusterer binaryFileClusterer = new BinaryFileClusterer(threadCount, clusteringResultDirectory,
				thresholds, fastMode, tmpClusteringResultDirectory, null);

		try {
			binaryFileClusterer.clusterFiles(binaryFiles);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}

		List<BinaryClusterFileReference> clusteredFiles = binaryFileClusterer.getResultFiles();
		clusteredFiles = new ArrayList<BinaryClusterFileReference>(clusteredFiles);
		Collections.sort(clusteredFiles);

		File mergedResultsDirectory = createTemporaryDirectory("merged_results", clusterDir);
		BinaryFileMergingClusterer mergingClusterer = new BinaryFileMergingClusterer(threadCount,
				mergedResultsDirectory, thresholds, fastMode, Defaults.getDefaultPrecursorIonTolerance(), true,
				mergedResultsDirectory);

		try {
			log.append(format.format(new Date()) + "\tMerging clusters started\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		File combinedResultFile = null;
		try {
			combinedResultFile = File.createTempFile("combined_clustering_results", ".cgf", clusterDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in creating temp file", e);
			return;
		}
		MergingCGFConverter mergingCGFConverter = new MergingCGFConverter(combinedResultFile, true);
		mergingClusterer.addListener(mergingCGFConverter);

		try {
			mergingClusterer.clusterFiles(clusteredFiles);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}

		System.gc();

		File[] generatedFiles = clusterDir.listFiles();
		File cgfFile = null;
		for (int i = 0; i < generatedFiles.length; i++) {
			if (generatedFiles[i].isDirectory()) {
				File[] subFiles = generatedFiles[i].listFiles();
				for (int j = 0; j < subFiles.length; j++) {
//					FileUtils.deleteQuietly(subFiles[j]);
				}
//				FileUtils.deleteQuietly(generatedFiles[i]);

			} else if (generatedFiles[i].isFile()) {
				if (generatedFiles[i].getName().endsWith("cgf")) {
					cgfFile = new File(clusterDir, "cluster.cgf");
					generatedFiles[i].renameTo(cgfFile);
				} else {
//					FileUtils.deleteQuietly(generatedFiles[i]);
				}
			}
		}

		File mgfFile = new File(clusterDir, resultName);
		int clusterCount = CGF2MGF.convertMultiFile(cgfFile, mgfFile);

		try {
			log.append(format.format(new Date()) + "\tClustered MS/MS count: " + clusterCount + "\n");
			log.append(format.format(new Date()) + "\tClustering MS/MS spectra finished\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	/**
	 * @deprecated
	 * @param fileNames
	 * @param clusterDir
	 * @param resultMgf
	 */
	public void clusterStandAlone(String[] fileNames, File clusterDir, File resultMgf) {

		if (!clusterDir.exists()) {
			clusterDir.mkdirs();
		}

		SpectraClusterStandalone spectraClusterStandalone = null;
		try {
			spectraClusterStandalone = new SpectraClusterStandalone(clusterDir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in clutering spectra");
			return;
		}

		ClusteringSettings
				.addIntitalSpectrumFilter(ClusteringSettings.SPECTRUM_FILTER.getFilterForName("immonium_ions").filter);

		ClusteringSettings
				.addIntitalSpectrumFilter(ClusteringSettings.SPECTRUM_FILTER.getFilterForName("mz_200").filter);

		ClusteringSettings.setLoadingSpectrumFilter(new HighestNPeakFunction(nHighestPeaks));

		ClusteringSettings.setLoadingSpectrumFilter(new BinnedHighestNPeakFunction(10, 100, 30));

		spectraClusterStandalone.setParallelJobs(threadCount);

		List<Float> thresholds = spectraClusterStandalone.generateClusteringThresholds(startThreshold, endThreshold,
				rounds);

		spectraClusterStandalone.setUseFastMode(fastMode);

		if (isobaricTag == null) {

		} else {
			String name = isobaricTag.getName();
			if (name.startsWith("iTRAQ")) {
				spectraClusterStandalone.setReporterType(RemoveReporterIonPeaksFunction.REPORTER_TYPE.ITRAQ);
			} else {
				spectraClusterStandalone.setReporterType(RemoveReporterIonPeaksFunction.REPORTER_TYPE.ALL);
			}
		}

		ArrayList<File> peaklistFiles = new ArrayList<File>();
		for (int i = 0; i < fileNames.length; i++) {
			peaklistFiles.add(new File(fileNames[i]));
		}
		System.out.println(ClusteringSettings.getLoadingSpectrumFilter().getClass() == HighestNPeakFunction.class);
		File finalResultFile = new File(clusterDir, "cluster.cgf");
		try {
			spectraClusterStandalone.clusterPeaklistFiles(peaklistFiles, thresholds, finalResultFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in clutering spectra");
		}

		System.gc();

		File[] generatedFiles = clusterDir.listFiles();
		File cgfFile = null;
		for (int i = 0; i < generatedFiles.length; i++) {
			if (generatedFiles[i].isDirectory()) {
				File[] subFiles = generatedFiles[i].listFiles();
				for (int j = 0; j < subFiles.length; j++) {
					FileUtils.deleteQuietly(subFiles[j]);
				}
				FileUtils.deleteQuietly(generatedFiles[i]);

			} else if (generatedFiles[i].isFile()) {
				if (generatedFiles[i].getName().endsWith("cgf")) {
					cgfFile = generatedFiles[i];
				} else {
					FileUtils.deleteQuietly(generatedFiles[i]);
				}
			}
		}

		int clusterCount = CGF2MGF.convertMultiFile(cgfFile, resultMgf);

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": clustered MS/MS spectra count: " + clusterCount);
		LOGGER.info(taskName + ": clustered MS/MS spectra count: " + clusterCount);
	}

	/**
	 * Can't remove report ions from isobaric labeling quantification, not used
	 * 
	 * @deprecated
	 * @param fileNames
	 * @param clusterDir
	 * @param resultMgf
	 */
	public void clustering(String[] fileNames, File clusterDir, File resultMgf) {

		if (!clusterDir.exists()) {
			clusterDir.mkdirs();
		}

		BinningSpectrumConverter binningSpectrumConverter = new BinningSpectrumConverter(clusterDir, threadCount,
				fastMode);

		try {
			binningSpectrumConverter.processPeaklistFiles(fileNames);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}
		List<BinaryClusterFileReference> binaryFiles = binningSpectrumConverter.getWrittenFiles();

		File clusteringResultDirectory = createTemporaryDirectory("clustering_results", clusterDir);
		File tmpClusteringResultDirectory = createTemporaryDirectory("clustering_results", clusterDir);
		List<Float> thresholds = generateThreshold(startThreshold, endThreshold, rounds);

		BinaryFileClusterer binaryFileClusterer = new BinaryFileClusterer(threadCount, clusteringResultDirectory,
				thresholds, fastMode, tmpClusteringResultDirectory, null);

		try {
			binaryFileClusterer.clusterFiles(binaryFiles);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}

		List<BinaryClusterFileReference> clusteredFiles = binaryFileClusterer.getResultFiles();
		clusteredFiles = new ArrayList<BinaryClusterFileReference>(clusteredFiles);
		Collections.sort(clusteredFiles);

		File mergedResultsDirectory = createTemporaryDirectory("merged_results", clusterDir);
		BinaryFileMergingClusterer mergingClusterer = new BinaryFileMergingClusterer(threadCount,
				mergedResultsDirectory, thresholds, fastMode, Defaults.getDefaultPrecursorIonTolerance(), true,
				mergedResultsDirectory);

		File combinedResultFile = null;
		try {
			combinedResultFile = File.createTempFile("combined_clustering_results", ".cgf", clusterDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in creating temp file", e);
		}
		MergingCGFConverter mergingCGFConverter = new MergingCGFConverter(combinedResultFile, true);
		mergingClusterer.addListener(mergingCGFConverter);

		try {
			mergingClusterer.clusterFiles(clusteredFiles);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in clutering spectra", e);
		}

		System.gc();

		File[] generatedFiles = clusterDir.listFiles();
		File cgfFile = null;
		for (int i = 0; i < generatedFiles.length; i++) {
			if (generatedFiles[i].isDirectory()) {
				File[] subFiles = generatedFiles[i].listFiles();
				for (int j = 0; j < subFiles.length; j++) {
					FileUtils.deleteQuietly(subFiles[j]);
				}
				FileUtils.deleteQuietly(generatedFiles[i]);

			} else if (generatedFiles[i].isFile()) {
				if (generatedFiles[i].getName().endsWith("cgf")) {
					cgfFile = new File(clusterDir, "cluster.cgf");
					generatedFiles[i].renameTo(cgfFile);
				} else {
					FileUtils.deleteQuietly(generatedFiles[i]);
				}
			}
		}

		int clusterCount = CGF2MGF.convertMultiFile(cgfFile, resultMgf);

		LOGGER.info(taskName + ": Clustered MS/MS spectra count: " + clusterCount);
	}

	private File createTemporaryDirectory(String prefix, File tmpDirectory) {
		File tmpFile = null;

		if (tmpDirectory != null && tmpDirectory.isDirectory()) {
			try {
				tmpFile = File.createTempFile(prefix, "", tmpDirectory);
			} catch (IOException e) {
				// TODO Auto-generated catch block

				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating temp file.");
				LOGGER.error(taskName + ": error in creating temp file.", e);
			}
		} else {
			try {
				tmpFile = File.createTempFile(prefix, "");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating temp file.");
				LOGGER.error(taskName + ": error in creating temp file.", e);
			}
		}

		if (tmpFile != null && !tmpFile.delete()) {
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in deleting temp file.");
			LOGGER.error(taskName + ": error in deleting temp file.");
			return tmpFile;
		}

		if (tmpFile != null && !tmpFile.mkdir()) {
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating temp file.");
			LOGGER.error(taskName + ": error in creating temp file.");
			return tmpFile;
		}

		return tmpFile;
	}

	private List<Float> generateThreshold(float startThreshold, float endThreshold, int rounds) {
		List<Float> thresholds = new ArrayList<Float>(rounds);
		float stepSize = (startThreshold - endThreshold) / (rounds - 1);

		for (int i = 0; i < rounds; i++) {
			thresholds.add(startThreshold - (stepSize * i));
		}

		return thresholds;
	}

}
