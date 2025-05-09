package bmi.med.uOttawa.metalab.task.dia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNParquetReader;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;

public class DiaLibCreateTask extends MetaLabTask {

	private static String taskName = "MAG spectra libarary create task";
	private static final Logger LOGGER = LogManager.getLogger(DiaLibCreateTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private int taskType;
	private MagDbItem magDbItem;
	private String filePath;
	private String enzyme;
	private String libName;
	private int miss;

	public static final int fromLibrary = 0;
	public static final int fromProtein = 1;
	public static final int fromPeptide = 2;

	public DiaLibCreateTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, String filePath,
			String enzyme, int miss, String libName) {
		super(metaPar, advPar, bar);
		this.magDbItem = metaPar.getUsedMagDbItem();
		this.filePath = filePath;
		this.enzyme = enzyme;
		this.libName = libName;
		this.miss = miss;

		if (filePath.endsWith(".speclib")) {
			this.taskType = fromLibrary;
		} else if (filePath.endsWith(".faa") || filePath.endsWith(".fasta")) {
			this.taskType = fromProtein;
		} else if (filePath.endsWith(".tsv") || filePath.endsWith(".csv")) {
			this.taskType = fromPeptide;
		}
	}

	public DiaLibCreateTask(MetaParameterDia metaPar, MetaSourcesDia advPar, JProgressBar bar, String filePath,
			String enzyme, int miss, String libName, int taskType) {
		super(metaPar, advPar, bar);

		this.magDbItem = metaPar.getUsedMagDbItem();
		this.filePath = filePath;
		this.enzyme = enzyme;
		this.libName = libName;
		this.miss = miss;
		this.taskType = taskType;
	}

	@Override
	protected Boolean doInBackground() throws Exception {

		File libFolder = new File(magDbItem.getCurrentFile(), "libraries");
		if (!libFolder.exists()) {
			libFolder.mkdir();
		}

		File libFile = new File(libFolder, libName);
		if (!libFile.exists()) {
			libFile.mkdir();
		}

		DiaNNTask diaNNTask = new DiaNNTask(((MetaSourcesDia) msv).getDiann());
		DiannParameter diannPar = new DiannParameter();
		diannPar.setThreads(metaPar.getThreadCount());
		diannPar.setEnzyme(enzyme);
		diannPar.setMissed_cleavages(miss);

		File destLibFile = new File(libFile, libName + ".speclib");
		File parquetFile = new File(libFile, libName + ".parquet");

		if (taskType == fromLibrary) {
			if (!destLibFile.exists()) {
				FileUtils.copyFile(new File(filePath), destLibFile);
			}
		} else if (taskType == fromProtein) {

			File pepFile = new File(libFile, libName + ".predicted.tsv");
			if (!pepFile.exists()) {
				DeepDetectTask deepDetectTask = new DeepDetectTask(((MetaSourcesDia) msv).getDeepDetect());
				DeepDetectParameter deepDetectPar = new DeepDetectParameter();
				deepDetectPar.setMissCleavage(miss);
				if (enzyme.equals("Lys-C")) {
					deepDetectPar.setProtease("LysC");
				} else if (enzyme.equals("Chymotrypsin")) {
					deepDetectPar.setProtease("Chymotrypsin");
				} else if (enzyme.equals("AspN")) {
					deepDetectPar.setProtease("AspN");
				} else if (enzyme.equals("GluC")) {
					deepDetectPar.setProtease("GluC");
				} else {
					deepDetectPar.setProtease("Trypsin");
				}

				deepDetectPar.setInput(new File(filePath));
				deepDetectPar.setOutput(pepFile);

				deepDetectTask.addTask(deepDetectPar);
				deepDetectTask.run(2);
			}

			HashMap<String, Double> pepScoreMap = new HashMap<String, Double>();
			HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
			try (BufferedReader reader = new BufferedReader(new FileReader(pepFile))) {
				String pline = reader.readLine();
				while ((pline = reader.readLine()) != null) {
					String[] cs = pline.split("\t");
					double score = Double.parseDouble(cs[2]);
					if (pepScoreMap.containsKey(cs[1])) {
						if (score > pepScoreMap.get(cs[1])) {
							pepScoreMap.put(cs[1], score);
						}
					} else {
						pepScoreMap.put(cs[1], score);
					}
					if (pepProMap.containsKey(cs[1])) {
						pepProMap.get(cs[1]).add(cs[0]);
					} else {
						HashSet<String> proSet = new HashSet<String>();
						proSet.add(cs[0]);
						pepProMap.put(cs[1], proSet);
					}
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + pepFile, e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + pepFile);
			}

			File pepDbFile = new File(libFile, libName + ".fasta");
			if (!pepDbFile.exists()) {
				List<Map.Entry<String, Double>> pepList = new ArrayList<>(pepScoreMap.entrySet());
				pepList.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));
				String[] sortedPeps = pepList.stream().map(Map.Entry::getKey).toArray(String[]::new);

				try (PrintWriter writer = new PrintWriter(pepDbFile)) {
					for (int i = 0; i < sortedPeps.length; i++) {
						double score = pepScoreMap.get(sortedPeps[i]);
						if (score < 0.85) {
							if (i > 2000000) {
								break;
							}
						} else {
							if (i > 3000000) {
								break;
							}
						}

						HashSet<String> proSet = pepProMap.get(sortedPeps[i]);
						if (proSet != null && proSet.size() > 0) {
							for (String pro : proSet) {
								writer.println(">" + pro);
								writer.println(sortedPeps[i]);
							}
						}
					}

					writer.close();
				} catch (IOException e) {
					LOGGER.error(taskName + ": error in writing " + pepDbFile, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + pepDbFile);
				}
			}

			if (!destLibFile.exists()) {
				File tempLibFile = new File(libFile, libName + ".predicted.speclib");
				if (tempLibFile.exists()) {
					tempLibFile.renameTo(destLibFile);
				} else {
					diaNNTask.addTask(diannPar, pepDbFile.getAbsolutePath(), destLibFile.getAbsolutePath(), false);
					diaNNTask.run(2);

					if (tempLibFile.exists()) {
						tempLibFile.renameTo(destLibFile);
					} else {
						return false;
					}
				}
			}
		} else if (taskType == fromPeptide) {

			File pepDbFile = new File(libFile, libName + ".fasta");
			if (!pepDbFile.exists()) {
				HashSet<String> pepSet = new HashSet<String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
					String pline = reader.readLine();
					String[] title = pline.split("\t");
					int seqid = -1;
					for (int i = 0; i < title.length; i++) {
						if (title[i].equals("Sequence")) {
							seqid = i;
						} else if (title[i].equals("Base sequence")) {
							seqid = i;
						}
					}

					if (seqid == -1) {

						LOGGER.error(taskName + ": there is no \"Sequence\" column in the file " + filePath);
						System.out.println(format.format(new Date()) + "\t" + taskName
								+ ": there is no \"Sequence\" column in the file " + filePath);

						return false;
					}

					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
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
							pepSet.add(seq);
						}
					}
					reader.close();
				} catch (IOException e) {

					LOGGER.error(taskName + ": error in reading " + filePath, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + filePath);

					return false;
				}

				String[] sortedPeps = pepSet.toArray(String[]::new);

				try (PrintWriter writer = new PrintWriter(pepDbFile)) {
					for (int i = 0; i < sortedPeps.length; i++) {
						writer.println(">pro" + i + " pro" + i);
						writer.println(sortedPeps[i]);
					}
					writer.close();
				} catch (IOException e) {
					LOGGER.error(taskName + ": error in writing " + pepDbFile, e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + pepDbFile);
				}
			}

			if (!destLibFile.exists()) {
				File tempLibFile = new File(libFile, libName + ".predicted.speclib");
				if (tempLibFile.exists()) {
					tempLibFile.renameTo(destLibFile);
				} else {
					diaNNTask.addTask(diannPar, pepDbFile.getAbsolutePath(), destLibFile.getAbsolutePath(), false);
					diaNNTask.run(2);

					if (tempLibFile.exists()) {
						tempLibFile.renameTo(destLibFile);
					} else {
						return false;
					}
				}
			}
		} else {
			return false;
		}

		if (!parquetFile.exists()) {
			diaNNTask.addTask(diannPar, destLibFile.getAbsolutePath(), parquetFile.getAbsolutePath());
			diaNNTask.run(1);
		}

		if (!parquetFile.exists()) {
			return false;
		}

		HashSet<String> pepSet = DiaNNParquetReader.getPeptideSet(parquetFile.getAbsolutePath());
		File infoFile = new File(libFile, libName + "_info.txt");
		try (PrintWriter writer = new PrintWriter(infoFile)) {
			writer.println("Data set size:\t" + pepSet.size());
			writer.println("Enzyme:\t" + enzyme);
			writer.println("Miss cleavages:\t" + miss);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the information to " + infoFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the information to " + infoFile);
		}

		magDbItem.addPepLib(libName, pepSet.size(), enzyme, miss);

		return true;
	}

	@Override
	public void forceStop() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
