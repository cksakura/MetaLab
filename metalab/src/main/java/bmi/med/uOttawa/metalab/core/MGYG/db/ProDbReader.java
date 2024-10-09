package bmi.med.uOttawa.metalab.core.MGYG.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

public class ProDbReader {
	
	private static void getAllRansk(String genome, String output) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		File[] files = (new File(genome)).listFiles();
		int totalCount = 0;
		for (int i = 0; i < files.length; i++) {
			File[] subFiles = files[i].listFiles();
			for (int j = 0; j < subFiles.length; j++) {
				if (subFiles[j].getName().contains("eggNOG")) {

					BufferedReader genomeReader = new BufferedReader(new FileReader(subFiles[j]));
					String line = genomeReader.readLine();
					while ((line = genomeReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (map.containsKey(cs[4])) {
							map.put(cs[4], map.get(cs[4]) + 1);
						} else {
							map.put(cs[4], 1);
						}
						totalCount++;
					}
					genomeReader.close();
				}
			}
		}
/*
		for (String key : map.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(key).append("\t");
			sb.append(map.get(key)).append("\t");
			sb.append(totalCount).append("\t");
			sb.append((double) map.get(key) / (double) totalCount);
			writer.println(sb);
		}
		writer.close();
*/
		HashMap<String, Integer>[] maps = new HashMap[8];
		for (int i = 0; i < maps.length; i++) {
			maps[i] = new HashMap<String, Integer>();
		}
		
		int gcount = 0;
		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		HashMap<Integer, Taxon> taxonMap = td.getTaxonMap();
		for (Integer id : taxonMap.keySet()) {
			Taxon taxon = taxonMap.get(id);
			String name = taxon.getName();
			if (map.containsKey(name)) {

				int count = map.get(name);

				int[] ids = td.getMainParentTaxonIds(taxon);
				if (ids == null) {
					continue;
				}

				if (taxon.getRank().equals("Genus") || taxon.getRank().equals("Species")) {
					gcount += count;
				}

				for (int k = 0; k < ids.length; k++) {
					Taxon taxoni = taxonMap.get(ids[k]);

					if (taxoni != null) {
						String namei = taxoni.getName();
						if (maps[k].containsKey(namei)) {
							maps[k].put(namei, maps[k].get(namei) + count);
						} else {
							maps[k].put(namei, count);
						}
					}
				}

				map.remove(name);
			}
		}
		
		System.out.println("gcount\t"+gcount);
		
		int gcount2 = 0;
		TaxonomyRanks[] ranks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < ranks.length; i++) {
			if (i == 1) {
				continue;
			}

			for (String key : maps[i].keySet()) {
				StringBuilder sb = new StringBuilder();
				sb.append(ranks[i].getName()).append("\t");

				sb.append(key).append("\t");
				sb.append(maps[i].get(key)).append("\t");
				sb.append(totalCount).append("\t");
				sb.append((double) maps[i].get(key) / (double) totalCount);
				writer.println(sb);
				
				if(i==ranks.length-2) {
					gcount2 += maps[i].get(key);
				}
			}
		}
		writer.close();
		
		System.out.println("gcount\t"+gcount2);
	}
	
	private static void getGenus(String genome, String output) throws IOException {

		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		HashMap<Integer, Taxon> taxonMap = td.getTaxonMap();
		
		PrintWriter writer = new PrintWriter(output);

		File[] files = (new File(genome)).listFiles();
		for (int i = 0; i < files.length; i++) {
			File[] subFiles = files[i].listFiles();
			for (int j = 0; j < subFiles.length; j++) {
				if (subFiles[j].getName().contains("eggNOG")) {
					int proCount = 0;
					HashMap<String, Integer> map = new HashMap<String, Integer>();
					BufferedReader genomeReader = new BufferedReader(new FileReader(subFiles[j]));
					String line = genomeReader.readLine();
					while ((line = genomeReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (map.containsKey(cs[4])) {
							map.put(cs[4], map.get(cs[4]) + 1);
						} else {
							map.put(cs[4], 1);
						}
						proCount++;
					}
					genomeReader.close();

					HashMap<String, Integer> genusIdMap = new HashMap<String, Integer>();
					HashMap<String, HashMap<String, Integer>> countMap = new HashMap<String, HashMap<String, Integer>>();
					for (Integer id : taxonMap.keySet()) {
						Taxon taxon = taxonMap.get(id);
						String name = taxon.getName();

						if (map.containsKey(name)) {
							int count = map.get(name);

							map.remove(name);
							
							int[] ids = td.getMainParentTaxonIds(taxon);
							if (ids == null) {
								continue;
							}

							for (int k = 0; k < ids.length; k++) {
								Taxon taxoni = taxonMap.get(ids[k]);
								if (taxoni != null) {
									String namei = taxoni.getName();
									String ranki = taxoni.getRank();

									if (ranki.equals("Genus")) {
										genusIdMap.put(namei, ids[k]);
									}
									
									if (countMap.containsKey(ranki)) {
										HashMap<String, Integer> cMap = countMap.get(ranki);
										if (cMap.containsKey(namei)) {
											cMap.put(namei, cMap.get(namei) + count);
										} else {
											cMap.put(namei, count);
										}
									} else {
										HashMap<String, Integer> cMap = new HashMap<String, Integer>();
										cMap.put(namei, count);
										countMap.put(ranki, cMap);
									}
								}
							}
						}
					}

					HashMap<String, Integer> genusMap = countMap.get("Genus");
					if (genusMap == null) {
						continue;
					}
					String genus = "";
					int gcount = 0;
					for (String gname : genusMap.keySet()) {
						if (genusMap.get(gname) > gcount) {
							gcount = genusMap.get(gname);
							genus = gname;
						}
					}

					Taxon taxon = taxonMap.get(genusIdMap.get(genus));
//System.out.println(files[i].getName()+"\t"+genus+"\t"+genusIdMap.get(genus)+"\t"+genusIdMap+"\t"+taxonMap.containsKey(genusIdMap.get(genus)));					
					StringBuilder sb = new StringBuilder();
					sb.append(files[i].getName());
					int[] ids = td.getMainParentTaxonIds(taxon);
					if (ids == null) {
						continue;
					}

					for (int k = 0; k < ids.length; k++) {
						Taxon taxoni = taxonMap.get(ids[k]);
						if (taxoni != null) {
							sb.append("\t").append(taxoni.getName());
						} else {
							sb.append("\t");
						}
					}
					sb.append("\t").append(gcount);
					sb.append("\t").append(proCount);
					sb.append("\t").append((double) gcount / (double) proCount);
					
					writer.println(sb);
				}
			}
		}
		writer.close();
	}
	
	private static void compare(String igc, String genome, int id) throws IOException {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(igc));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			map.put(cs[2 + id], 0);
		}
		reader.close();
		System.out.println("Igc geneus\t" + map.size());

		HashMap<String, Integer> genomeMap = new HashMap<String, Integer>();
		BufferedReader genomeReader = new BufferedReader(new FileReader(genome));
		while ((line = genomeReader.readLine()) != null) {
			String[] cs = line.split("\t");
			String genus = cs[id + 3];
			int count = Integer.parseInt(cs[cs.length - 3]);
			if (genomeMap.containsKey(genus)) {
				genomeMap.put(genus, genomeMap.get(genus) + count);
			} else {
				genomeMap.put(genus, count);
			}
		}
		genomeReader.close();

		HashSet<String> total = new HashSet<String>();
		total.addAll(map.keySet());
		total.addAll(genomeMap.keySet());
		System.out.println(map.size() + "\t" + genomeMap.size() + "\t" + total.size());
System.out.println(genomeMap.keySet());
		for (String key : map.keySet()) {
//			System.out.println(key + "\t" + map.get(key));
		}
	}

	private static void getSpecies(String species) throws IOException {

		int proCount = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(species));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (map.containsKey(cs[4])) {
				map.put(cs[4], map.get(cs[4]) + 1);
			} else {
				map.put(cs[4], 1);
			}
			proCount++;
		}
		reader.close();

		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		HashMap<Integer, Taxon> taxonMap = td.getTaxonMap();

		HashMap<String, HashMap<String, Integer>> countMap = new HashMap<String, HashMap<String, Integer>>();
		for (Integer id : taxonMap.keySet()) {
			Taxon taxon = taxonMap.get(id);
			String name = taxon.getName();

			if (map.containsKey(name)) {
				int count = map.get(name);

				int[] ids = td.getMainParentTaxonIds(taxon);
				if (ids == null) {
					continue;
				}

				for (int i = 0; i < ids.length; i++) {
					Taxon taxoni = taxonMap.get(ids[i]);
					if (taxoni != null) {
						String namei = taxoni.getName();
						String ranki = taxoni.getRank();

						if (countMap.containsKey(ranki)) {
							HashMap<String, Integer> cMap = countMap.get(ranki);
							if (cMap.containsKey(namei)) {
								cMap.put(namei, cMap.get(namei) + count);
							} else {
								cMap.put(namei, count);
							}
						} else {
							HashMap<String, Integer> cMap = new HashMap<String, Integer>();
							cMap.put(namei, count);
							countMap.put(ranki, cMap);
						}
					}
				}
			}
		}

		for (String rank : countMap.keySet()) {
			HashMap<String, Integer> cmap = countMap.get(rank);
			for (String spname : cmap.keySet()) {
				System.out.println(rank + "\t" + spname + "\t" + cmap.get(spname) + "\t"
						+ (double) cmap.get(spname) / (double) proCount);
			}
		}
	}
	
	private static void getIGCRank(String igc) throws IOException {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(igc));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			map.put(cs[0], 0);
		}
		reader.close();
		System.out.println("Igc geneus\t" + map.size());

		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		HashMap<Integer, Taxon> taxonMap = td.getTaxonMap();
		for (Integer id : taxonMap.keySet()) {
			Taxon taxon = taxonMap.get(id);
			String name = taxon.getName();
			String rank = taxon.getRank();

			if (rank.equals("Genus") && map.containsKey(name)) {
				StringBuilder sb = new StringBuilder();
				int[] ids = td.getMainParentTaxonIds(taxon);
				if (ids == null) {
					continue;
				}

				for (int k = 0; k < ids.length; k++) {
					Taxon taxoni = taxonMap.get(ids[k]);
					if (taxoni != null) {
						sb.append(taxoni.getName()).append("\t");
					} else {
						sb.append("\t");
					}
				}

				String content = sb.toString();
				if (content.startsWith("Bacteria"))
					System.out.println(sb);
			}
		}
	}
	
	private static void topGenusTest(String in) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("No genus >50%", 0);
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			double ratio = Double.parseDouble(cs[cs.length - 1]);
			if (ratio < 0.5) {
				map.put("No genus >50%", map.get("No genus >50%") + 1);
			} else {
				if (map.containsKey(cs[3])) {
					map.put(cs[3], map.get(cs[3]) + 1);
				} else {
					map.put(cs[3], 1);
				}
			}
		}
		reader.close();

		for (String key : map.keySet()) {
			System.out.println(key + "\t" + map.get(key));
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

//		ProDbReader.getSpecies("D:\\Data\\new_db_4\\original_db\\00002\\MGYG-HGUT-00002_eggNOG.tsv");

		ProDbReader.compare(
				"Z:\\Microbiome\\IGC_Gene_profile_1267\\IGC.genus.normalization.ProfileTable\\genus.tsv",
				"D:\\Data\\new_db_4\\analysis\\top_genus.tsv", 0);
		
//		ProDbReader.getGenus("D:\\Data\\new_db_4\\original_db", "D:\\Data\\new_db_4\\analysis\\genus.tsv");
		
//		ProDbReader.getIGCRank("Z:\\Microbiome\\IGC_Gene_profile_1267\\IGC.genus.normalization.ProfileTable\\genus.normalization.profile");
	
//		ProDbReader.getAllRansk("D:\\Data\\new_db_4\\original_db", 
//				"D:\\Data\\new_db_4\\analysis\\allTaxa2.tsv");
		
		ProDbReader.topGenusTest("D:\\Data\\new_db_4\\analysis\\top_genus.tsv");
	}

}
