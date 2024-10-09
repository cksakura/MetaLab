/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kai Cheng
 *
 */
public class FastaReader {
	
	private File in;
	private ProteinItem[] items;
	
	public FastaReader(String in) throws IOException{
		this(new File(in));
	}
	
	public FastaReader(File in) throws IOException{
		this.in = in;
		this.read();
	}
	
	private void read() throws IOException {
		ArrayList<ProteinItem> list = new ArrayList<ProteinItem>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		String ref = null;
		StringBuilder sb = new StringBuilder(1024);
		Pattern pattern1 = Pattern.compile(".*\\[(.*)\\].*");
		Pattern pattern2 = Pattern.compile(".*From (.*) At.*");
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
				if (ref != null) {
					Matcher matcher1 = pattern1.matcher(ref);
					if (matcher1.matches()) {
						String genome = matcher1.group(1);
						ProteinItem proitem = new ProteinItem(ref, sb.toString(), genome);
						list.add(proitem);
					} else {
						Matcher matcher2 = pattern2.matcher(ref);
						if (matcher2.matches()) {
							String genome = matcher2.group(1);
							ProteinItem proitem = new ProteinItem(ref, sb.toString(), genome);
							list.add(proitem);
						} else {
							ProteinItem proitem = new ProteinItem(ref, sb.toString());
							list.add(proitem);
						}
					}
				}

				ref = line.substring(1);
				sb = new StringBuilder();

			} else {
				if (line.trim().length() > 0) {
					sb.append(line);
				}
			}
		}

		if (ref != null) {
			Matcher matcher1 = pattern1.matcher(ref);
			if (matcher1.matches()) {
				String genome = matcher1.group(1);
				ProteinItem proitem = new ProteinItem(ref, sb.toString(), genome);
				list.add(proitem);
			} else {
				Matcher matcher2 = pattern2.matcher(ref);
				if (matcher2.matches()) {
					String genome = matcher2.group(1);
					ProteinItem proitem = new ProteinItem(ref, sb.toString(), genome);
					list.add(proitem);
				} else {
					ProteinItem proitem = new ProteinItem(ref, sb.toString());
					list.add(proitem);
				}
			}
		}

		reader.close();
		this.items = list.toArray(new ProteinItem[list.size()]);
	}

	public ProteinItem[] getItems() {
		return items;
	}
	
	public ProteinItem[] getTargetItems(String decoy_symbol) {
		ArrayList<ProteinItem> list = new ArrayList<ProteinItem>();
		for (int i = 0; i < items.length; i++) {
			String ref = items[i].getRef();
			if (!ref.startsWith(decoy_symbol)) {
				list.add(items[i]);
			}
		}
		ProteinItem[] targets = list.toArray(new ProteinItem[list.size()]);
		return targets;
	}
	
	private static void getCount(String in) throws IOException{
		int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=reader.readLine())!=null){
			if(line.startsWith(">")){
				count++;
			}
		}
		reader.close();
		System.out.println(count);
	}
	
	private static void compare(String s1, String s2) throws IOException {
		HashSet<String> set1 = new HashSet<String>();
		BufferedReader reader1 = new BufferedReader(new FileReader(s1));
		String line = null;
		while ((line = reader1.readLine()) != null) {
			if (line.startsWith(">")) {
				String[] cs = line.split("\\s");
				set1.add(cs[0]);
			}
		}
		reader1.close();
		
		HashSet<String> set2 = new HashSet<String>();
		BufferedReader reader2 = new BufferedReader(new FileReader(s2));
		while ((line = reader2.readLine()) != null) {
			if (line.startsWith(">")) {
				String[] cs = line.split("\\s");
				set2.add(cs[0]);
			}
		}
		reader2.close();
		
		HashSet<String> total = new HashSet<String>();
		total.addAll(set1);
		total.addAll(set2);
		
		System.out.println(set1.size()+"\t"+set2.size()+"\t"+total.size()+"\t"+(set1.size()+set2.size()-total.size()));
	}
	
	private static void getTotalAACount(String in) throws IOException {
		long total = 0;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith(">")) {
				total += line.length();
			}
		}
		reader.close();
		System.out.println(total);
	}

	private static void getGenomeProCount(String in) throws IOException {
		HashSet<String> set = new HashSet<String>();
		int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
				String[] cs = line.split("_");
				set.add(cs[0].substring(1));
				count++;
			}
		}
		reader.close();
		System.out.println(set.size()+"\t"+count);
	}
	
	public static void main(String[] args) throws IOException {
		FastaReader.getGenomeProCount("Z:\\Kai\\Raw_files\\single_species\\8483\\pfind_open_search_MAG\\"
				+ "Zhibin_20221021_singleStrain_F1\\Zhibin_20221021_singleStrain_F1.fasta");
	}
}
