package bmi.med.uOttawa.metalab.core.MGYG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HapExtractor {
	
	private final static String ribosomal1 = "ribosomal";
	private final static String ribosomal2 = "Ribosomal";
	private final static String elongation1 = "elongation";
	private final static String elongation2 = "Elongation";
	
	private final static String ribosomal0 = " ribosomal protein ";
	private final static String elongation0 = " Elongation factor ";
	
	private ArrayList<String> hapList;
	
	public HapExtractor() {
		this(true);
	}
	
	public HapExtractor(boolean strict) {
		this.hapList = new ArrayList<String>();
		if (strict) {
			addHap(ribosomal0);
			addHap(elongation0);
		} else {
			addHap(ribosomal1);
			addHap(ribosomal2);
			addHap(elongation1);
			addHap(elongation2);
		}
	}
	
	public void addHap(String hap) {
		this.hapList.add(hap);
	}
	
	public void extract(String folder, String output) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		File[] files = (new File(folder)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("faa")) {
				BufferedReader reader = new BufferedReader(new FileReader(files[i]));
				String lineString = null;
				boolean write = false;
				while ((lineString = reader.readLine()) != null) {
					if (lineString.startsWith(">")) {

						boolean find = false;
						for (int j = 0; j < hapList.size(); j++) {
							if (lineString.contains(hapList.get(j))) {
								find = true;
								break;
							}
						}

						if (find) {
							write = true;
							writer.println(lineString);
						} else {
							write = false;
						}

					} else {
						if (write) {
							writer.println(lineString);
						}
					}
				}
				reader.close();
			}
		}
		writer.close();
	}
	
	public void extract(File folder, File output) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".faa") || files[i].getName().endsWith(".fasta")) {
				int[] proCounts = new int[2];
				BufferedReader reader = new BufferedReader(new FileReader(files[i]));
				String lineString = null;
				boolean write = false;
				while ((lineString = reader.readLine()) != null) {
					if (lineString.startsWith(">")) {
						proCounts[0]++;

						boolean find = false;
						for (int j = 0; j < hapList.size(); j++) {
							if (lineString.contains(hapList.get(j))) {
								find = true;
								break;
							}
						}

						if (find) {
							proCounts[1]++;
							write = true;
							writer.println(lineString);
						} else {
							write = false;
						}

					} else {
						if (write) {
							writer.println(lineString);
						}
					}
				}
				reader.close();

				if (proCounts[1] == 0 && proCounts[0] < 1000) {
					reader = new BufferedReader(new FileReader(files[i]));
					while ((lineString = reader.readLine()) != null) {
						writer.println(lineString);
					}
					reader.close();
				}
			}
		}
		writer.close();
	}
}
