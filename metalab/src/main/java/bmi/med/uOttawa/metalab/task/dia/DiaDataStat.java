package bmi.med.uOttawa.metalab.task.dia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DiaDataStat {
	
	@SuppressWarnings("unused")
	private static void transfer(String in, String project, String db) {
		File oldFile = new File(in, project);
		File outFile = new File(in, "genome_pep_rank");
		File genomePepRankFile = new File(outFile, project);
		if (!genomePepRankFile.exists()) {
			genomePepRankFile.mkdir();
		}

		File modelFile = new File(db, "model");
		HashMap<String, Double> pepMap = new HashMap<String, Double>();
		try (BufferedReader reader = new BufferedReader(
				new FileReader(new File(modelFile, project + "_predicted.tsv")))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split(",");
				pepMap.put(cs[1], Double.parseDouble(cs[2]));
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pep\t" + pepMap.size());

		HashMap<String, double[]> proFuncMap = new HashMap<String, double[]>();
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(oldFile, "pro_func_intensity.tsv")))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[5];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[i + 1]);
				}
				proFuncMap.put(cs[0], intensity);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pro\t" + proFuncMap.size());

		BufferedWriter[] writer = { null };
		try {
			writer[0] = new BufferedWriter(new FileWriter(new File(genomePepRankFile, "test.tsv"), true));
			writer[0].write("Genome\ttrain_pep_count\ttotal_pep_count\tratio\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		ExecutorService executor = Executors.newFixedThreadPool(20);
		File[] predictFiles = (new File(db, "predicted")).listFiles();
		for (File file : predictFiles) {
			executor.execute(() -> {
				String name = file.getName();
				String genome = name.substring(0, name.length() - ".predicted.tsv".length());
				HashSet<String> findSet = new HashSet<String>();
				HashSet<String> totalSet = new HashSet<String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						totalSet.add(cs[1]);
						if (proFuncMap.containsKey(cs[0]) && pepMap.containsKey(cs[1])) {
							findSet.add(cs[1]);
						}
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				synchronized (writer[0]) {
					try {
						writer[0].write(genome + "\t" + findSet.size() + "\t" + totalSet.size() + "\t"
								+ ((double) findSet.size() / (double) totalSet.size()) + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}

		executor.shutdown();
		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {
				writer[0].close();
			}
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void getGenomePepCount(String db, String out) throws IOException, InterruptedException {

		BufferedWriter[] writer = { null };
		try {
			writer[0] = new BufferedWriter(new FileWriter(new File(out), true));
			writer[0].write("Genome\tpep_count\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		ExecutorService executor = Executors.newFixedThreadPool(20);
		File[] predictFiles = (new File(db, "predicted")).listFiles();
		for (File file : predictFiles) {

			executor.execute(() -> {
				String name = file.getName();
				String genome = name.substring(0, name.length() - ".predicted.tsv".length());
				HashSet<String> pSet = new HashSet<String>();
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						pSet.add(cs[1]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				synchronized (writer[0]) {
					try {
						writer[0].write(genome + "\t" + pSet.size() + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}

		executor.shutdown();
		if (executor.awaitTermination(1, TimeUnit.HOURS)) {
			writer[0].close();
		}
	}
	
	private static void test(String dl, String db, String project, String output) {

		File dlFile = new File(dl, "Filtered_spectra");
		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(dlFile, project+ "-pFind-Filtered.spectra")))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				pepSet.add(cs[5]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pep\t" + pepSet.size());

		File modelFile = new File(db, "model");
		try (BufferedReader reader = new BufferedReader(
				new FileReader(new File(modelFile, project + "_predicted_rank.tsv")))) {

			File outputFile = new File(output, project + "_genome_pep_count.tsv");
			PrintWriter writer = new PrintWriter(outputFile);
			writer.println("peptide\tgenome_rank");
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (pepSet.contains(cs[1])) {
					writer.println(cs[1] + "\t" + cs[3]);
				}
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub

//		DiaDataStat.getGenomePepCount("Z:\\Kai\\Database\\human_gut", 
//				"Z:\\Kai\\paper_data\\DIA\\resources_figs\\genome_pep_count.tsv");
		
//		DiaDataStat.transfer("Z:\\Kai\\Raw_files\\core_pep_db", "PXD012724", "Z:\\Kai\\Database\\human_gut");
		
		DiaDataStat.test("Z:\\Zhongzhi\\15Projects_MetaLab_results", "Z:\\Kai\\Database\\human_gut", "PXD012724",
				"Z:\\Kai\\paper_data\\DIA\\resources_figs");
	}

}
