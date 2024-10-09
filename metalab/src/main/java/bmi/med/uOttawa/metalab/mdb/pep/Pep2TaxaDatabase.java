/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.pep;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

/**
 * @author Kai Cheng
 *
 */
public class Pep2TaxaDatabase {

//	private String pep2Taxa = "Resources//db//pep2taxa.gz";
	private String pep2Taxa = "Resources//db//pep2taxa.tab";
	private TaxonomyDatabase tdb;

	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private static Logger LOGGER = LogManager.getLogger(Pep2TaxaDatabase.class);

	public Pep2TaxaDatabase() {
		this.tdb = new TaxonomyDatabase();
	}

	public Pep2TaxaDatabase(String pep2Taxa) {
		this.pep2Taxa = pep2Taxa;
		this.tdb = new TaxonomyDatabase();
	}

	public Pep2TaxaDatabase(TaxonomyDatabase tdb) {
		this.tdb = tdb;
	}

	public Pep2TaxaDatabase(String pep2Taxa, TaxonomyDatabase tdb) {
		this.pep2Taxa = pep2Taxa;
		this.tdb = tdb;
	}
	
	private void pept2LcaSingle(String sequence) {
		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date()) + "\t" + "Error in reading pep2tax database " + pep2Taxa);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date()) + "\t" + "Error in reading pep2tax database " + pep2Taxa);
			}
		}

		String line = null;
		char s0 = sequence.charAt(0);

		try {
			while ((line = reader.readLine()) != null) {
				char l0 = line.charAt(0);
				if (s0 > l0) {
					continue;
				} else if (s0 == l0) {
					String[] cs = line.split("\t");
					if (sequence.equals(cs[0])) {
						System.out.println(line);
					}
				} else {
					break;
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in reading pep2tax database " + pep2Taxa);
		}
	}

	public Taxon pept2LcaSignle(String sequence, TaxonomyRanks rank, HashSet<RootType> rootTypes,
			HashSet<String> exclude) {

		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date()) + "\t" + "Error in reading pep2tax database " + pep2Taxa);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date()) + "\t" + "Error in reading pep2tax database " + pep2Taxa);
			}
		} else {
			return null;
		}

		String line = null;
		char s0 = sequence.charAt(0);

		try {
			while ((line = reader.readLine()) != null) {
				char l0 = line.charAt(0);
				if (s0 > l0) {
					continue;
				} else if (s0 == l0) {
					String[] cs = line.split("\t");
					if (sequence.equals(cs[0])) {
						int[] taxIds = new int[cs.length - 1];
						for (int i = 0; i < taxIds.length; i++) {
							taxIds[i] = Integer.parseInt(cs[i + 1]);
						}

						Taxon taxon = tdb.getLCA4PepTaxDb(taxIds, rank, rootTypes, exclude);
						reader.close();
						return taxon;
					}
				} else {
					break;
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
		}

		return null;
	}

	public HashMap<String, Integer> pept2Lca(HashSet<String> set, TaxonomyRanks rank, HashSet<RootType> rootTypes,
			HashSet<String> exclude) {

		HashMap<String, Integer> map = new HashMap<String, Integer>();

		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else {
			return map;
		}

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (set.contains(cs[0])) {
					int[] taxIds = new int[cs.length - 1];
					for (int i = 0; i < taxIds.length; i++) {
						taxIds[i] = Integer.parseInt(cs[i + 1]);
					}

					Taxon taxon = tdb.getLCA4PepTaxDb(taxIds, rank, rootTypes, exclude);
					if (taxon != null) {
						map.put(cs[0], taxon.getId());
					}
					set.remove(cs[0]);
				}
				if (set.size() == 0) {
					break;
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
		}

		return map;
	}

	public HashMap<String, Integer> taxa2Lca(HashMap<String, ArrayList<Taxon>> taxamap, TaxonomyRanks rank,
			HashSet<RootType> rootTypes, HashSet<String> exclude) {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String key : taxamap.keySet()) {
			ArrayList<Taxon> list = taxamap.get(key);
			Taxon[] taxa = list.toArray(new Taxon[list.size()]);
			Taxon taxon = tdb.getLCA4PepTaxDb(taxa, rank, rootTypes, exclude);
			if (taxon != null) {
				map.put(key, taxon.getId());
			}
		}

		return map;
	}

	public HashMap<String, ArrayList<Taxon>> pept2TaxaList(HashSet<String> set) {

		HashMap<String, ArrayList<Taxon>> taxaListMap = new HashMap<String, ArrayList<Taxon>>();
		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else {
			return taxaListMap;
		}

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (set.contains(cs[0])) {

					ArrayList<Taxon> taxaList = new ArrayList<Taxon>();
					L: for (int i = 1; i < cs.length; i++) {
						int taxId = Integer.parseInt(cs[i]);
						Taxon taxon = tdb.getTaxonFromId(taxId);
						if (taxon != null) {

							if (taxon.getRankId() == -1) {
								while (true) {
									taxon = tdb.getTaxonFromId(taxon.getParentId());
									if (taxon == null) {
										continue L;
									}
									if (taxon.getRankId() > -1) {
										break;
									}
								}
							}

							while (taxon.getRankId() > 23) {
								taxon = tdb.getTaxonFromId(taxon.getParentId());
							}

							if (taxon != null) {
								taxaList.add(taxon);
							}
						}
					}

					if (taxaList.size() > 0) {
						taxaListMap.put(cs[0], taxaList);
					}
					set.remove(cs[0]);
				}
				if (set.size() == 0) {
					break;
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
		}

		return taxaListMap;
	}

	public void pept2lca(HashSet<String> set, TaxonomyRanks rank, HashSet<RootType> rootTypes, HashSet<String> exclude,
			Appendable appendable) {

		try {

			appendable.append("Sequence,");
			appendable.append("LCA,");
			appendable.append("LCA Taxonomy Id,");
			appendable.append("Rank,");

			appendable.append("(Super)Kingdom,");
			appendable.append("Phylum,");
			appendable.append("Class,");
			appendable.append("Order,");
			appendable.append("Family,");
			appendable.append("Genus,");
			appendable.append("Species,");
			appendable.append("\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
		}

		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
				System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
			}
		} else {
			return;
		}

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (set.contains(cs[0])) {
					int[] taxIds = new int[cs.length - 1];
					for (int i = 0; i < taxIds.length; i++) {
						taxIds[i] = Integer.parseInt(cs[i + 1]);
					}

					Taxon taxon = tdb.getLCA4PepTaxDb(taxIds, rank, rootTypes, exclude);
					set.remove(cs[0]);

					if (taxon == null) {
						// appendable.append(cs[0] + "\t" + "cannot match" + line +
						// "\n");
						// appendable.append(cs[0] + "," + "cannot match" + "\n");
					} else {
						// appendable.append(cs[0] + "\t" + taxon.getName() + "\t" +
						// taxon.getId() + "\t" + taxon.getRank()
						// + "\t" + line + "\n");
						appendable.append(
								cs[0] + "," + taxon.getName() + "," + taxon.getId() + "," + taxon.getRank() + ",");

						String[] lineage = tdb.getTaxonArrays(taxon);

						for (int i = 0; i < lineage.length; i++) {
							if (lineage[i] != null) {
								appendable.append(lineage[i]).append(",");
							} else {
								appendable.append(",");
							}
						}
						appendable.append("\n");
					}
				}
				if (set.size() == 0) {
					break;
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			System.err.println(format.format(new Date())+"\t"+"Error in reading pep2tax database " + pep2Taxa);
		}

//		for (String sequence : set) {
//			appendable.append(sequence + "," + "not found" + "\n");
//		}
	}

	public static void performTxtFormat(String in, String titleCellName, String output, String pep2tax, String taxdb) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] title = null;
		try {
			title = reader.readLine().split("\t");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int seqId = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals(titleCellName)) {
				seqId = i;
			}
		}

		HashSet<String> set = new HashSet<String>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] content = line.split("\t");
				set.add(content[seqId]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing result to " + output, e);
		}
		Pep2TaxaDatabase db = new Pep2TaxaDatabase(pep2tax, new TaxonomyDatabase(taxdb));
		db.pept2lca(set, TaxonomyRanks.Family, rootSet, exclude, writer);
		writer.close();
	}

	public static void performTxtFormat2(String in, String titleCellName, String output, String pep2tax, String taxdb) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] title = null;
		try {
			title = reader.readLine().split("\t");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int seqId = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals(titleCellName)) {
				seqId = i;
			}
		}

		HashSet<String> set = new HashSet<String>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] content = line.split("\t");
				set.add(content[seqId]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing result to " + output, e);
		}
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Peptide id,");
		titlesb.append("Sequence,");
		titlesb.append("Species id,");
		titlesb.append("Taxon identifier,");
		titlesb.append("Superkingdom,");
		titlesb.append("kingdom,");
		titlesb.append("Phylum,");
		titlesb.append("Class,");
		titlesb.append("Order,");
		titlesb.append("Family,");
		titlesb.append("Genus,");
		titlesb.append("Species");
		writer.println(titlesb.toString());

		Pep2TaxaDatabase db = new Pep2TaxaDatabase(pep2tax, new TaxonomyDatabase(taxdb));

		HashMap<String, ArrayList<Taxon>> taxaListMap = db.pept2TaxaList(set);
		String[] sequences = taxaListMap.keySet().toArray(new String[taxaListMap.size()]);
		Arrays.sort(sequences);

		for (int i = 0; i < sequences.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i + 1).append(",").append(sequences[i]).append(",");
			ArrayList<Taxon> taxa = taxaListMap.get(sequences[i]);
			for (int j = 0; j < taxa.size(); j++) {
				if (j > 0) {
					sb.append(",,");
				}
				Taxon taxon = taxa.get(j);
				int taxId = taxon.getId();
				String[] lineage = taxon.getLineage();
				sb.append(j + 1).append(",");
				sb.append(taxId).append(",");
				for (String node : lineage) {
					sb.append(node).append(",");
				}
				sb.append("\n");
			}
			writer.print(sb.toString());
		}
		writer.close();
	}

	public void taxonContainTest(HashSet<String> pepset, int taxid, Appendable appendable) {

		BufferedReader reader = null;
		if (pep2Taxa.endsWith("tab")) {
			try {
				reader = new BufferedReader(new FileReader(pep2Taxa));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			}
		} else if (pep2Taxa.endsWith("gz")) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(pep2Taxa))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
			}
		} else {
			return;
		}

		String line = null;
		try {
			L: while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (pepset.contains(cs[0])) {
					int[] taxIds = new int[cs.length - 1];
					for (int i = 0; i < taxIds.length; i++) {
						taxIds[i] = Integer.parseInt(cs[i + 1]);
					}

					for (int i = 0; i < taxIds.length; i++) {
						Taxon taxon = tdb.getTaxonFromId(taxIds[i]);
						if (taxon != null) {
							int[] lineage = tdb.getMainParentTaxonIds(taxon);
							for (int j = 0; j < lineage.length; j++) {
								if (lineage[j] == taxid) {
									appendable.append("Match\t").append(cs[0]).append("\t").append(taxon.getName())
											.append("\t").append(String.valueOf(taxon.getId())).append("\n");
									continue L;
								}
							}
						}
					}

					appendable.append("Not match\t").append(cs[0]).append("\t");
				}
				if (pepset.size() == 0) {
					break;
				}
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading pep2tax database " + pep2Taxa, e);
		}
	}

}
