package bmi.med.uOttawa.metalab.task.dia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;

public class DiaPredictedLibTask {

	private DiaNNTask diaNNTask;
	private DeepDetectTask deepDetectTask;
	private DiannParameter diannPar;
	private DeepDetectParameter deepDetectPar;
	private double pepDetectThreshold;

	protected Logger LOGGER = LogManager.getLogger(DiaPredictedLibTask.class);
	protected String taskName = "Spectra library generation";
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public DiaPredictedLibTask(DiaNNTask diaNNTask, DeepDetectTask deepDetectTask, DiannParameter diannPar) {
		this.diaNNTask = diaNNTask;
		this.deepDetectTask = deepDetectTask;
		this.diannPar = diannPar;
		this.deepDetectPar = diannPar.getDeepPredictPar();
	}

	public DiaPredictedLibTask(DiaNNTask diaNNTask, DiannParameter diannPar) {
		this.diaNNTask = diaNNTask;
		this.diannPar = diannPar;
	}

	public void refineLibrary(String predictPep, String libTsv, double threshold) throws IOException {
		HashSet<String> pepset = new HashSet<String>();
		BufferedReader predictPepReader = new BufferedReader(new FileReader(predictPep));
		String line = predictPepReader.readLine();
		while ((line = predictPepReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (Double.parseDouble(cs[2]) > threshold) {
				pepset.add(cs[1]);
			}
		}
		predictPepReader.close();

		BufferedReader libTsvReader = new BufferedReader(new FileReader(libTsv));
		PrintWriter tempWriter = new PrintWriter(libTsv + ".temp.tsv");
		line = libTsvReader.readLine();
		tempWriter.println(line);

		while ((line = libTsvReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (pepset.contains(cs[9])) {
				tempWriter.println(line);
			}
		}
		libTsvReader.close();
		tempWriter.close();
	}

	public void generateLibrary(String fasta, String lib) {
		generateLibrary(new File(fasta), new File(lib), true);
	}

	public void generateLibrary(File fasta, File lib, boolean keepTsv) {

		if (lib.exists() && lib.length() > 0) {

			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": spectra library already exists in " + lib);
			LOGGER.info(taskName + ": spectra library already exists in " + lib);

			return;
		}

		File parentFile = fasta.getParentFile();
		String name = fasta.getName();
		int id = name.lastIndexOf(".");
		name = name.substring(0, id);

		LOGGER.info(taskName + ": generating library for " + name + " started");
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": generating library for " + name + " started");

		File predictedPepTsv = new File(parentFile, name + ".predicted.tsv");

		if (!predictedPepTsv.exists()) {

			LOGGER.info(taskName + ": high-responding peptide prediction started");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": high-responding peptide prediction started");

			DeepDetectParameter ddPar = new DeepDetectParameter(deepDetectPar);
			ddPar.setInput(fasta);
			ddPar.setOutput(predictedPepTsv);

			deepDetectTask.addTask(ddPar);
			deepDetectTask.run((int) (fasta.length() * 1E-7) > 1 ? (int) (fasta.length() * 1E-7) : 1);

			LOGGER.info(taskName + ": high-responding peptide prediction finished");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": high-responding peptide prediction finished");
		}

		if (!predictedPepTsv.exists() || predictedPepTsv.length() == 0) {
			LOGGER.error(taskName + ": high-responding peptide prediction failed");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": high-responding peptide prediction failed");

			return;
		}

		HashMap<String, ArrayList<String>> pepProMap = new HashMap<String, ArrayList<String>>(100000);

		File tempFasta = new File(parentFile, name + ".temp.fasta");
		if (!tempFasta.exists()) {
			BufferedReader predictPepReader;
			try {
				predictPepReader = new BufferedReader(new FileReader(predictedPepTsv));
				PrintWriter writer = new PrintWriter(tempFasta);
				String line = predictPepReader.readLine();
				while ((line = predictPepReader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (Double.parseDouble(cs[2]) > this.deepDetectPar.getThreshold()) {
						if (pepProMap.containsKey(cs[1])) {
							pepProMap.get(cs[1]).add(cs[0]);
						} else {
							ArrayList<String> list = new ArrayList<String>();
							list.add(cs[0]);
							pepProMap.put(cs[1], list);

							writer.println(">" + cs[0]);
							writer.println(cs[1]);
						}
					}
				}
				writer.close();
				predictPepReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing high-responding peptides to temp database", e);
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing high-responding peptides to temp database");
			}
		} else {
			BufferedReader predictPepReader;
			try {
				predictPepReader = new BufferedReader(new FileReader(predictedPepTsv));
				String line = predictPepReader.readLine();
				while ((line = predictPepReader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (Double.parseDouble(cs[2]) > this.deepDetectPar.getThreshold()) {
						if (pepProMap.containsKey(cs[1])) {
							pepProMap.get(cs[1]).add(cs[0]);
						} else {
							ArrayList<String> list = new ArrayList<String>();
							list.add(cs[0]);
							pepProMap.put(cs[1], list);
						}
					}
				}
				predictPepReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing high-responding peptides to temp database", e);
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": error in writing high-responding peptides to temp database");
			}
		}

		if (!tempFasta.exists()) {
			LOGGER.error(taskName + ": writing high-responding peptides to temp database failed");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": writing high-responding peptides to temp database failed");
			return;
		}

		File tempLib1 = new File(parentFile, name + ".temp.speclib");
		File tempLib2 = new File(parentFile, name + ".temp.predicted.speclib");

		if (!tempLib2.exists()) {
			diaNNTask.addTask(diannPar, tempFasta.getAbsolutePath(), tempLib1.getAbsolutePath(), false);
			diaNNTask.run((int) (tempFasta.length() * 1E-7) > 1 ? (int) (tempFasta.length() * 1E-7) : 1);
		}

		if (!tempLib2.exists() || tempLib2.length() == 0) {
			LOGGER.error(taskName + ": predicting spectra library from temp fasta file " + tempFasta + " failed");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": predicting spectra library from temp fasta file " + tempFasta + " failed");
			return;
		}

		File tempLibTsv = new File(parentFile, name + ".temp.speclib.tsv");
		if (!tempLibTsv.exists()) {
			diaNNTask.addTask(diannPar, tempLib2.getAbsolutePath(), tempLibTsv.getAbsolutePath());
			diaNNTask.run((int) (tempFasta.length() * 1E-7) > 1 ? (int) (tempFasta.length() * 1E-7) : 1);
		}

		if (!tempLibTsv.exists() || tempLibTsv.length() == 0) {
			LOGGER.error(
					taskName + ": converting spectra library from speclib file " + tempLib2 + " to tsv file failed");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": converting spectra library from speclib file " + tempLib2 + " to tsv file failed");
			return;
		}

		File preLib = new File(parentFile, name + ".speclib.tsv");
		PrintWriter preLibWriter = null;
		BufferedReader tempTsvReader = null;

		try {
			tempTsvReader = new BufferedReader(new FileReader(tempLibTsv));

			preLibWriter = new PrintWriter(preLib);

			String tempTsvLine = tempTsvReader.readLine();
			preLibWriter.println(tempTsvLine);

			String[] title = tempTsvLine.split("\t");
			int seqId = -1;
			int proId = -1;
			int NTermId = -1;
			int CTermId = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("PeptideSequence")) {
					seqId = i;
				} else if (title[i].equals("ProteinGroup")) {
					proId = i;
				} else if (title[i].equals("NTerm")) {
					NTermId = i;
				} else if (title[i].equals("CTerm")) {
					CTermId = i;
				}
			}

			L: while ((tempTsvLine = tempTsvReader.readLine()) != null) {
				String[] cs = tempTsvLine.split("\t");
				StringBuilder sb = new StringBuilder();

				ArrayList<String> prolist = null;
				for (int i = 0; i < cs.length; i++) {
					if (i == seqId) {
						prolist = pepProMap.get(cs[i]);
						if (prolist == null) {
							continue L;
						}
						sb.append(cs[i]).append("\t");
					} else if (i == NTermId) {
						sb.append("0\t");
					} else if (i == CTermId) {
						sb.append("0\t");
					} else if (i == proId) {
						String pro = "";
						if (prolist != null) {
							for (int j = 0; j < prolist.size(); j++) {
								String pj = prolist.get(j);
								pro += pj;
								pro += ";";
							}
						}

						sb.append(pro.length() > 1 ? pro.substring(0, pro.length() - 1) : pro).append("\t");
					} else {
						sb.append(cs[i]).append("\t");
					}
				}
				sb.deleteCharAt(sb.length() - 1);

				preLibWriter.println(tempTsvLine);
			}

			preLibWriter.close();
			tempTsvReader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": generating spectra library for " + fasta.getName() + " failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": generating spectra library for "
					+ fasta.getName() + " failed");
		}

		if (!preLib.exists() || preLib.length() == 0) {
			LOGGER.error(taskName + ": generating spectra library for " + fasta.getName() + " failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": generating spectra library for "
					+ fasta.getName() + " failed");
			return;
		}

		File finalLib = new File(parentFile, name + ".speclib.tsv.speclib");

		if (!keepTsv) {
			if (!finalLib.exists()) {
				diaNNTask.addTask(diannPar, preLib.getAbsolutePath(), new String[] { fasta.getAbsolutePath() });
				diaNNTask.run((int) (tempFasta.length() * 1E-7) > 1 ? (int) (tempFasta.length() * 1E-7) : 1);
			}
		} else {
			if (!finalLib.exists()) {
				diaNNTask.addTask(diannPar, preLib.getAbsolutePath());
				diaNNTask.run((int) (tempFasta.length() * 1E-7) > 1 ? (int) (tempFasta.length() * 1E-7) : 1);
			}
		}

		if (!finalLib.exists() || finalLib.length() == 0) {
			LOGGER.error(taskName + ": converting spectra library from " + preLib.getName() + " failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": converting spectra library from "
					+ preLib.getName() + " failed");
			return;
		}

		finalLib.renameTo(lib);

		FileUtils.deleteQuietly(tempFasta);
		FileUtils.deleteQuietly(tempLib2);
		FileUtils.deleteQuietly(tempLibTsv);
		FileUtils.deleteQuietly(new File(parentFile, name + ".temp.log.txt"));
		FileUtils.deleteQuietly(new File(parentFile, name + ".temp.speclib.log.txt"));

		if (!keepTsv) {
			FileUtils.deleteQuietly(preLib);
		}

		File diaNNFile = this.diaNNTask.getDiaNNFile();
		File tempLibFile = new File(diaNNFile.getParentFile(), "lib.tsv");
		File tempLibLogFile = new File(diaNNFile.getParentFile(), "lib.log.txt");

		FileUtils.deleteQuietly(tempLibFile);
		FileUtils.deleteQuietly(tempLibLogFile);

		LOGGER.info(taskName + ": generating library for " + name + " finished");
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": generating library for " + name + " finished");
	}

	public void combine(String[] genomes, String libDir, String output, double threshold) {
		for (int i = 0; i < genomes.length; i++) {
			File libTsvFile = new File(libDir, genomes[i] + ".speclib.tsv");
			if (!libTsvFile.exists()) {
				File libFile = new File(libDir, genomes[i] + ".predicted.speclib");
				if (libFile.exists()) {
					diaNNTask.addTask(diannPar, libFile.getAbsolutePath(), libTsvFile.getAbsolutePath());
				} else {
					LOGGER.info(taskName + ": spectra library for " + genomes[i] + " not found");
					System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra library for "
							+ genomes[i] + " not found");
					return;
				}
			}
		}

		diaNNTask.run(this.diannPar.getThreads(), genomes.length);

		LOGGER.info(taskName + ": combining spectra library started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": combining spectra library started");

		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < genomes.length; i++) {
			File predictedPepFile = new File(libDir, genomes[i] + ".predicted.tsv");
			if (predictedPepFile.exists()) {
				BufferedReader reader;
				String line;
				try {
					reader = new BufferedReader(new FileReader(predictedPepFile));
					line = reader.readLine();
					String[] title = line.split("\t");
					int pepId = -1;
					int proId = -1;
					int scoreId = -1;
					for (int j = 0; j < title.length; j++) {
						if (title[j].equals("Protein id")) {
							proId = i;
						} else if (title[j].equals("Peptide sequence")) {
							pepId = i;
						} else if (title[j].equals("Peptide detectability")) {
							scoreId = i;
						}
					}
					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (Double.parseDouble(cs[scoreId]) > pepDetectThreshold) {
							if (pepProMap.containsKey(cs[pepId])) {
								pepProMap.get(cs[pepId]).add(cs[proId]);
							} else {
								HashSet<String> proset = new HashSet<String>();
								proset.add(cs[proId]);
								pepProMap.put(cs[pepId], proset);
							}
						}
					}
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				LOGGER.info(taskName + ": predicted peptide for " + genomes[i] + " not found");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": predicted peptide for " + genomes[i]
						+ " not found");
				return;
			}
		}

		File combineLibTsv = new File(output + ".tsv");

		try (PrintWriter writer = new PrintWriter(combineLibTsv)) {
			HashSet<String> transitSet = new HashSet<String>();
			for (int i = 0; i < genomes.length; i++) {
				File libFile = new File(libDir, genomes[i] + ".speclib.tsv");
				if (!libFile.exists()) {
					LOGGER.info(taskName + ": spectra library for " + genomes[i] + " not found");
					System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra library for "
							+ genomes[i] + " not found");
					writer.close();
					return;
				}

				try (BufferedReader reader = new BufferedReader(new FileReader(libFile))) {
					String line = reader.readLine();
					String[] title = line.split("\t");
					int transitId = -1;
					int seqId = -1;
					int proId = -1;
					for (int j = 0; j < title.length; j++) {
						if (title[j].equals("transition_name")) {
							transitId = j;
						} else if (title[j].equals("PeptideSequence")) {
							seqId = j;
						} else if (title[j].equals("ProteinGroup")) {
							proId = j;
						}
					}

					L: while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");

						if (!transitSet.contains(cs[transitId])) {
							transitSet.add(cs[transitId]);
							StringBuilder sb = new StringBuilder();
							HashSet<String> proset = null;
							for (int j = 0; j < cs.length; j++) {
								if (j == seqId) {
									proset = pepProMap.get(cs[j]);
									if (proset == null) {
										continue L;
									}
									sb.append(cs[j]).append("\t");
								} else if (j == proId) {
									String pro = "";
									if (proset != null) {
										for (String proRef : proset) {
											pro += proRef;
											pro += ";";
										}
									}

									sb.append(pro.length() > 1 ? pro.substring(0, pro.length() - 1) : pro).append("\t");
								} else {
									sb.append(cs[j]).append("\t");
								}
							}

							writer.println(sb);
						}
					}

					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.info(taskName + ": error in reading spectra library files", e);
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": error in reading spectra library files");
				}
			}

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing spectra library file " + output, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing spectra library file " + output);
		}

		File finalLib = new File(combineLibTsv.getAbsolutePath() + ".speclib");

		String[] fastas = new String[genomes.length];
		for (int i = 0; i < fastas.length; i++) {
			fastas[i] = (new File(libDir, genomes[i] + ".faa")).getAbsolutePath();
		}

		diaNNTask.addTask(diannPar, combineLibTsv.getAbsolutePath(), fastas);
		diaNNTask.run((int) (combineLibTsv.length() * 1E-7) > 1 ? (int) (combineLibTsv.length() * 1E-7) : 1);

		if (finalLib.exists()) {
			finalLib.renameTo(new File(output));
			FileUtils.deleteQuietly(combineLibTsv);
		} else {
			LOGGER.info(taskName + ": converting spectra library from " + combineLibTsv + " failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": converting spectra library from "
					+ combineLibTsv + " failed");
			return;
		}

		File diaNNFile = this.diaNNTask.getDiaNNFile();
		File tempLibFile = new File(diaNNFile.getParentFile(), "lib.tsv");
		File tempLibLogFile = new File(diaNNFile.getParentFile(), "lib.log.txt");

		FileUtils.deleteQuietly(tempLibFile);
		FileUtils.deleteQuietly(tempLibLogFile);

		LOGGER.info(taskName + ": combining spectra library finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": combining spectra library finished");
	}

	public void combine(String[] genomes, String libDir, String output) {
		File combineLibTsv = new File(output + ".tsv");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(combineLibTsv);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing spectra library file " + output, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing spectra library file " + output);
		}
		if (writer == null) {
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(diannPar.getThreads());

		for (int i = 0; i < genomes.length; i++) {
			File libTsvFile = new File(libDir, genomes[i] + ".speclib.tsv");
			if (!libTsvFile.exists()) {
				File libFile = new File(libDir, genomes[i] + ".predicted.speclib");
				if (!libFile.exists()) {
					File fastaFile = new File(libDir, genomes[i] + ".faa");
					executor.submit(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							generateLibrary(fastaFile, libFile, true);
						}
					});
				}
			}
		}

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(genomes.length * 3, TimeUnit.HOURS);

			if (finish) {
				LOGGER.info(taskName + ": generating spectra library for genomes finished");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": generating spectra library for genomes finished");
			} else {
				LOGGER.info(taskName + ": generating spectra library for genomes failed");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": generating spectra library for genomes failed");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}

		for (int i = 0; i < genomes.length; i++) {
			File libTsvFile = new File(libDir, genomes[i] + ".speclib.tsv");
			if (!libTsvFile.exists()) {
				File libFile = new File(libDir, genomes[i] + ".speclib");
				if (libFile.exists()) {
					diaNNTask.addTask(diannPar, libFile.getAbsolutePath(), libTsvFile.getAbsolutePath());
				} else {
					LOGGER.info(taskName + ": spectra library for " + genomes[i] + " not found");
					System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra library for "
							+ genomes[i] + " not found");
					return;
				}
			}
		}

		diaNNTask.run(genomes.length);

		LOGGER.info(taskName + ": combining spectra library started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": combining spectra library started");

		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		boolean printTitle = false;
		for (int i = 0; i < genomes.length; i++) {
			File libTsvFile = new File(libDir, genomes[i] + ".speclib.tsv");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(libTsvFile));
				String line = reader.readLine();
				if (!printTitle) {
					writer.println(line);
					printTitle = true;
				}

				String[] title = line.split("\t");
				int seqId = -1;
				int proId = -1;
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals("PeptideSequence")) {
						seqId = j;
					} else if (title[j].equals("ProteinGroup")) {
						proId = j;
					}
				}

				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] pros = cs[proId].split(";");
					for (int j = 0; j < pros.length; j++) {
						if (pepProMap.containsKey(cs[seqId])) {
							pepProMap.get(cs[seqId]).add(pros[j]);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pros[j]);
							pepProMap.put(cs[seqId], set);
						}
					}
				}

				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.info(taskName + ": error in reading spectra library files", e);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in reading spectra library files");
			}
		}

		HashSet<String> transitSet = new HashSet<String>();
		for (int i = 0; i < genomes.length; i++) {
			File libFile = new File(libDir, genomes[i] + ".speclib.tsv");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(libFile));
				String line = reader.readLine();

				String[] title = line.split("\t");
				int transitId = -1;
				int seqId = -1;
				int proId = -1;
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals("transition_name")) {
						transitId = j;
					} else if (title[j].equals("PeptideSequence")) {
						seqId = j;
					} else if (title[j].equals("ProteinGroup")) {
						proId = j;
					}
				}

				L: while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");

					if (!transitSet.contains(cs[transitId])) {
						transitSet.add(cs[transitId]);
						StringBuilder sb = new StringBuilder();
						HashSet<String> proset = null;
						for (int j = 0; j < cs.length; j++) {
							if (j == seqId) {
								proset = pepProMap.get(cs[j]);
								if (proset == null) {
									continue L;
								}
								sb.append(cs[j]).append("\t");
							} else if (j == proId) {
								String pro = "";
								if (proset != null) {
									for (String proRef : proset) {
										pro += proRef;
										pro += ";";
									}
								}

								sb.append(pro.length() > 1 ? pro.substring(0, pro.length() - 1) : pro).append("\t");
							} else {
								sb.append(cs[j]).append("\t");
							}
						}

						writer.println(sb);
					}
				}

				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.info(taskName + ": error in reading spectra library files", e);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in reading spectra library files");
			}
		}

		writer.close();

		File finalLib = new File(combineLibTsv.getAbsolutePath() + ".speclib");

		String[] fastas = new String[genomes.length];
		for (int i = 0; i < fastas.length; i++) {
			fastas[i] = (new File(libDir, genomes[i] + ".faa")).getAbsolutePath();
		}

		diaNNTask.addTask(diannPar, combineLibTsv.getAbsolutePath(), fastas);
		diaNNTask.run((int) (combineLibTsv.length() * 1E-7) > 1 ? (int) (combineLibTsv.length() * 1E-7) : 1);

		if (finalLib.exists()) {
			finalLib.renameTo(new File(output));
			FileUtils.deleteQuietly(combineLibTsv);
		} else {
			LOGGER.info(taskName + ": converting spectra library from " + combineLibTsv + " failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": converting spectra library from "
					+ combineLibTsv + " failed");
			return;
		}

		File diaNNFile = this.diaNNTask.getDiaNNFile();
		File tempLibFile = new File(diaNNFile.getParentFile(), "lib.tsv");
		File tempLibLogFile = new File(diaNNFile.getParentFile(), "lib.log.txt");

		FileUtils.deleteQuietly(tempLibFile);
		FileUtils.deleteQuietly(tempLibLogFile);

		LOGGER.info(taskName + ": combining spectra library finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": combining spectra library finished");
	}

	public void addTask(String fasta) {
		addTask(new File(fasta));
	}

	public void addTask(File fasta) {
		File parentFile = fasta.getParentFile();
		String name = fasta.getName();
		int id = name.lastIndexOf(".");
		name = name.substring(0, id);

		File predictedPepTsv = new File(parentFile, name + ".predicted.tsv");
		if (!predictedPepTsv.exists()) {

			DeepDetectParameter par = new DeepDetectParameter(this.deepDetectPar);
			par.setInput(fasta);
			par.setOutput(predictedPepTsv);
			deepDetectTask.addTask(par);
		}
	}
}
