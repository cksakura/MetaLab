package bmi.med.uOttawa.metalab.task.dia;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNPrecursor;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNResultReader;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;

public class DiaSerialTask {

	private DiaNNTask diaNNTask;
	private DeepDetectTask deepDetectTask;
	private DiannParameter diannPar;
	private DeepDetectParameter deepDetectPar;

	private int threadCount;
	private String[] dias;
	private String output;
	private String dbfolder;

	protected Logger LOGGER = LogManager.getLogger(DiaSerialTask.class);
	protected String taskName = "DIA library search";
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public DiaSerialTask(DiaNNTask diaNNTask, DeepDetectTask deepDetectTask, DiannParameter diannPar,
			DeepDetectParameter deepDetectPar, String[] dias, String output, String dbfolder) {
		this.diaNNTask = diaNNTask;
		this.deepDetectTask = deepDetectTask;
		this.diannPar = diannPar;
		this.dias = dias;
		this.output = output;
		this.dbfolder = dbfolder;
	}

	public void run(String[] genomes, boolean terminateCheck, double ratio) {
		run(genomes, terminateCheck, ratio, 1);
	}

	public void run(String[] genomes, boolean terminateCheck, double ratio, int threadCount) {
		if (terminateCheck) {
			HashSet<String> totalPepset = new HashSet<String>();
			for (int i = 0; i < genomes.length; i++) {
				HashMap<String, Double> genomeIntenMap = searchGenome(genomes[i], threadCount);
				double intensity = 0;
				double totalIntensity = 0;
				for (String pep : genomeIntenMap.keySet()) {
					if (!totalPepset.contains(pep)) {
						intensity += genomeIntenMap.get(pep);
					}
					totalIntensity += genomeIntenMap.get(pep);
				}

				if (intensity > totalIntensity * ratio) {
					totalPepset.addAll(genomeIntenMap.keySet());
				} else {
					break;
				}
			}
		} else {
			for (int i = 0; i < genomes.length; i++) {
				searchGenome(genomes[i], threadCount);
			}
		}
	}

	private void prepareDB(String[] genomes) {

		LOGGER.info(taskName + ": checking spectral library start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": checking spectral library start");

		int deepDetectCount = 0;
		for (int i = 0; i < genomes.length; i++) {

			File deepDetectFile = new File(dbfolder, genomes[i] + ".deepDetect.tsv");
			if (!deepDetectFile.exists()) {
				File fastaFile = new File(dbfolder, genomes[i] + ".faa");
				deepDetectPar.setInput(fastaFile);
				deepDetectPar.setOutput(deepDetectFile);
				deepDetectTask.addTask(deepDetectPar);

				deepDetectCount++;
			}
		}

		LOGGER.info(taskName + ": predicting high-reponsing peptides for " + deepDetectCount + " genomes");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": predicting high-reponsing peptides for "
				+ deepDetectCount + " genomes");

		deepDetectTask.run(threadCount, deepDetectCount);

		LOGGER.info(taskName + ": predicting high-reponsing peptides finished");
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": predicting high-reponsing peptides finished");

		for (int i = 0; i < genomes.length; i++) {
			File libFile = new File(dbfolder, genomes[i] + ".predicted.speclib");
			File fastaFile = new File(dbfolder, genomes[i] + ".deepDetect.fasta");
			File libOutFile = new File(dbfolder, genomes[i] + ".tsv");
			File dlibFile = new File(dbfolder, genomes[i] + ".speclib");
			File deepDetectFile = new File(dbfolder, genomes[i] + ".deepDetect.tsv");

			if (!libFile.exists()) {

				deepDetectPar.setInput(fastaFile);
				deepDetectPar.setOutput(deepDetectFile);
				deepDetectTask.addTask(deepDetectPar);

				deepDetectCount++;
			}

		}
	}

	private HashMap<String, Double> searchGenome(String genome, int threadCount) {

		File genomeFile = new File(output, genome);
		if (!genomeFile.exists()) {
			genomeFile.mkdir();
		}

		File outFile = new File(genomeFile, genome + ".tsv");

		File libFile = new File(dbfolder, genome + ".predicted.speclib");
		File fastaFile = new File(dbfolder, genome + ".faa");
		File libOutFile = new File(dbfolder, genome + ".tsv");
		File dlibFile = new File(dbfolder, genome + ".speclib");
		File deepDetectFile = new File(dbfolder, genome + ".deepDetect.tsv");

		if (!deepDetectFile.exists()) {

		}

		DiaNNResultReader reader = new DiaNNResultReader(genomeFile, genome, diannPar.getCut());
		if (reader.hasResult()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": DIA-NN search result already exists in "
					+ genomeFile);
			LOGGER.info(taskName + ": DIA-NN search result already exists in " + genomeFile);
		} else {
			diannPar.setRelaxed_prot_inf(false);

			if (libFile.exists()) {
				diaNNTask.addTask(diannPar, dias, outFile.getAbsolutePath(), libFile.getAbsolutePath());
			} else {

				System.out.println(format.format(new Date()) + "\t" + taskName + ": the spectra library for "
						+ genomeFile + " is not found, generating a spectra library will take several hours");

				LOGGER.info(taskName + ": the spectra library for " + genomeFile
						+ " is not found, generating a spectra library will take several hours");

				diaNNTask.addTask(diannPar, dias, outFile.getAbsolutePath(), fastaFile.getAbsolutePath(),
						libOutFile.getAbsolutePath(), false);
			}

			diaNNTask.run(threadCount, dias.length * 24);
		}

		if (libOutFile.exists()) {
			FileUtils.deleteQuietly(libOutFile);
		}

		if (dlibFile.exists()) {
			FileUtils.deleteQuietly(dlibFile);
		}

		reader = new DiaNNResultReader(genomeFile, genome, diannPar.getCut());

		double totalIntensity = 0;

		HashMap<String, Double> pepIntenMap = new HashMap<String, Double>();

		if (reader.hasResult()) {

			DiaNNPrecursor[] diaNNPrecursors = reader.getDiaNNPrecursors();
			for (int i = 0; i < diaNNPrecursors.length; i++) {
				double intensity = diaNNPrecursors[i].getPreTrans();
				String pep = diaNNPrecursors[i].getModSeqString();
				if (pepIntenMap.containsKey(pep)) {
					pepIntenMap.put(pep, pepIntenMap.get(pep) + intensity);
				} else {
					pepIntenMap.put(pep, intensity);
				}
			}

			System.out.println(format.format(new Date()) + "\t" + taskName + ": DIA-NN search in " + genome
					+ " finished, the total intensity is " + totalIntensity);
			LOGGER.info(
					taskName + ": DIA-NN search in " + genome + " finished, the total intensity is " + totalIntensity);
		} else {
			System.out
					.println(format.format(new Date()) + "\t" + taskName + ": DIA-NN search in " + genome + " failed");
			LOGGER.info(taskName + ": DIA-NN search in " + genome + " failed");
		}

		return pepIntenMap;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
