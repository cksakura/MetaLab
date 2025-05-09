/**
 * 
 */
package bmi.med.uOttawa.metalab.core.taxonomy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Download from http://www.uniprot.org/taxonomy/
 * 
 * @author Kai Cheng
 *
 */
public class TaxonomyDatabase {

	/**
	 * taxonomy-all.tab.gz is download from http://www.uniprot.org/taxonomy/
	 */
	public static final String filepath = "Resources//taxonomy-all.tab";
	private int taxonColumn;
	private int nameColumn;
	private int rankColumn;
	private int lineageColumn;
	private int parentColumn;

	private static final HashMap<String, Integer> rankIdMap = TaxonomyRanks.getRankIdMap();
	private HashMap<Integer, Taxon> taxonMap;
	
	/**
	 * key = name+"_"+rankId
	 */
	private HashMap<String, Integer> taxonNameMap;
	
	private static Logger LOGGER = LogManager.getLogger(TaxonomyDatabase.class);

	public TaxonomyDatabase() {
		this.taxonMap = new HashMap<Integer, Taxon>();
		this.taxonNameMap = new HashMap<String, Integer>();
		this.read(filepath);
	}
	
	public TaxonomyDatabase(String filepath) {
		this.taxonMap = new HashMap<Integer, Taxon>();
		this.taxonNameMap = new HashMap<String, Integer>();
		this.read(filepath);
	}

	private void read(String in) {
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			String[] title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Taxon")) {
					taxonColumn = i;
				}
				if (title[i].equals("Scientific name")) {
					nameColumn = i;
				}
				if (title[i].equals("Rank")) {
					rankColumn = i;
				}
				if (title[i].equals("Lineage")) {
					lineageColumn = i;
				}
				if (title[i].equals("Parent")) {
					parentColumn = i;
				}
			}
			while ((line = reader.readLine()) != null) {
				Taxon taxon = parse(line);
				if (taxon != null) {
					this.taxonMap.put(taxon.getId(), taxon);
					this.taxonNameMap.put(taxon.getName() + "_" + taxon.getRankId(), taxon.getId());
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading taxonomy and phylogeny information from " + in, e);
			System.err.println("Error in reading taxonomy and phylogeny information from " + in);
		}
	}

	private Taxon parse(String line) {
		String[] cs = line.split("\t");
		int taxonId = Integer.parseInt(cs[taxonColumn]);
		String name = "";
		if (cs.length > nameColumn) {
			name = cs[nameColumn];
		} else {
			return null;
		}
		int rank = -1;
		if (cs.length > rankColumn) {
			if (rankIdMap.containsKey(cs[rankColumn]))
				rank = rankIdMap.get(cs[rankColumn]);
		}
		int parent = -1;
		if (cs.length > parentColumn) {
			parent = Integer.parseInt(cs[parentColumn]);
		}

		RootType rootType = null;
		if (cs.length > lineageColumn) {
			String[] csi = cs[lineageColumn].split("[; ]");

			if (csi[0].trim().length() > 0) {
				rootType = RootType.valueOf(csi[0]);
			} else {
				rootType = RootType.getRootType(taxonId);
			}
		} else {
			rootType = RootType.getRootType(taxonId);
		}

		Taxon taxon = new Taxon(taxonId, parent, rank, name, rootType);
		return taxon;
	}
	
	public HashMap<String, Integer> getRankIdMap() {
		return rankIdMap;
	}

	public HashMap<Integer, Taxon> getTaxonMap() {
		return taxonMap;
	}
	
	public HashMap<String, Integer> getTaxonNameMap() {
		return taxonNameMap;
	}
	
	public static HashMap<String, Integer> parseFullNameMap() {
		HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();
			String[] title = line.split("\t");
			int taxonColumn = -1;
			int namesColumn = -1;
			int rankColumn = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Taxon")) {
					taxonColumn = i;
				}
				if (title[i].equals("Other Names")) {
					namesColumn = i;
				}
				if (title[i].equals("Rank")) {
					rankColumn = i;
				}
			}
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (namesColumn < cs.length && cs[namesColumn].length() > 0) {
					int rank = -1;
					if (rankColumn < cs.length && rankIdMap.containsKey(cs[rankColumn])) {
						rank = rankIdMap.get(cs[rankColumn]);
					}
					int id = Integer.parseInt(cs[taxonColumn]);
					String[] names = cs[namesColumn].split("; ");
					for (String name : names) {
						nameMap.put(name + "_" + rank, id);
					}
				}
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading taxonomy and phylogeny information from " + filepath, e);
			System.err.println("Error in reading taxonomy and phylogeny information from " + filepath);
		}

		return nameMap;
	}
	
	public Taxon getTaxonFromId(int taxonId) {
		return taxonMap.get(taxonId);
	}
	
	public Taxon getTaxonFromName(String name_rankId) {
		if (taxonNameMap.containsKey(name_rankId)) {
			return getTaxonFromId(taxonNameMap.get(name_rankId));
		} else {
			return null;
		}
	}
	
	public Taxon getTaxonFromName(String name, int rankId) {
		return getTaxonFromName(name + "_" + rankId);
	}
	
	public int getMainParentId(int taxonId) {
		Taxon taxon = this.taxonMap.get(taxonId);
		int parentId = taxon.getParentId();
		if (parentId == -1) {
			return -1;
		}
		Taxon parent = this.taxonMap.get(parentId);
		if (TaxonomyRanks.getRankFromId(parent.getRankId()).isMainRank()) {
			return parent.getId();
		} else {
			return getMainParentId(parent.getId());
		}
	}
	
	public int getMainRankId(int taxonId) {
		Taxon taxon = this.taxonMap.get(taxonId);
		int rankId = taxon.getRankId();
		if (taxon.isMainRank()) {
			return rankId;
		} else {
			return getMainRankId(taxon.getParentId());
		}
	}
	
	public int getTaxonId4Rank(int taxonId, int rankId) {
		Taxon taxon = this.taxonMap.get(taxonId);
		if (taxon.getRankId() == rankId) {
			return taxonId;
		} else {
			return getTaxonId4Rank(taxon.getParentId(), rankId);
		}
	}

	/**
	 * 
	 * @param taxonIds
	 * @param blankRank blank above this rank will be seen as distinct value, blank equal or below this rank will be ignored
	 * @param mainTypeSet
	 * @param exclude
	 * @return
	 */
	public Taxon getLCA4PepTaxDb(int[] taxonIds, TaxonomyRanks blankRank, HashSet<RootType> mainTypeSet,
			HashSet<String> exclude) {

		RootType mainType = null;

		HashSet<Integer> set = new HashSet<Integer>();
		for (int taxId : taxonIds) {
			Taxon taxon = this.taxonMap.get(taxId);

			if (taxon != null) {

				RootType rootType = taxon.getRootType();
				if (!mainTypeSet.contains(rootType)) {
					continue;
				}

				if (rootType.isMulticellular()) {

					int add2SetId = taxId;
					int parentId = taxon.getParentId();
					while (true) {
						if (this.taxonMap.containsKey(parentId)) {
							Taxon parent = this.taxonMap.get(parentId);
							String parentName = parent.getName();
							parentId = parent.getParentId();
							if (exclude.contains(parentName) && this.taxonMap.containsKey(parentId)) {
								add2SetId = parentId;
							}
						} else {
							break;
						}
					}

					set.add(add2SetId);

					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								mainType = RootType.cellular_organisms;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else if (rootType.isUnicellular()) {
					set.add(taxId);
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								return Taxon.root;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else {
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {

							} else if (mainType.isUnicellular()) {

							} else {
								return Taxon.root;
							}
						}
					}
				}
			}
		}

		if (mainType == null || !mainTypeSet.contains(mainType)) {
			return null;
		}

		if (set.size() == 0 || mainType == RootType.cellular_organisms) {
			return this.taxonMap.get(mainType.getId());
		}

		Integer[] usedIds = set.toArray(new Integer[set.size()]);

		if (usedIds.length == 1) {
			return this.taxonMap.get(usedIds[0]);
		}

		int[][] taxonArrays = new int[usedIds.length][];
		for (int i = 0; i < usedIds.length; i++) {
			Taxon taxon = this.taxonMap.get(usedIds[i]);
			taxonArrays[i] = this.getMainParentTaxonIds(taxon);
		}

		int blankRankId = blankRank.getMainId();

		int lcaId = 0;
		boolean find = false;
		
		L: for (int i = TaxonomyRanks.Superkingdom.getMainId(); i <= blankRankId; i++) {

			int lcaIdi = taxonArrays[0][i];

			for (int j = 1; j < taxonArrays.length; j++) {
				if (taxonArrays[j][i] != lcaIdi) {
					find = true;
					break L;
				}
			}
			if (lcaIdi != -1) {
				lcaId = lcaIdi;
			}
		}

		if (find) {
			if (lcaId == 0) {
				return this.taxonMap.get(mainType.getId());
			} else {
				return this.taxonMap.get(lcaId);
			}
		} else {
			for (int i = blankRankId + 1; i <= TaxonomyRanks.Species.getMainId(); i++) {
				int lcaIdi = 0;
				for (int j = 0; j < taxonArrays.length; j++) {
					if (taxonArrays[j][i] != -1) {
						lcaIdi = taxonArrays[j][i];
						break;
					}
				}

				if (lcaIdi != 0) {
					for (int j = 0; j < taxonArrays.length; j++) {
						if (taxonArrays[j][i] != -1 && taxonArrays[j][i] != lcaIdi) {
							return this.taxonMap.get(lcaId);
						}
					}
					lcaId = lcaIdi;
				} 
			}
			
			return this.taxonMap.get(lcaId);
		}
	}
	
	/**
	 * 
	 * @param taxonIds
	 * @param blankRank blank above this rank will be seen as distinct value, blank equal or below this rank will be ignored
	 * @param mainTypeSet
	 * @param exclude
	 * @return
	 */
	public Taxon getLCA4PepTaxDb(Taxon[] taxa, TaxonomyRanks blankRank, HashSet<RootType> mainTypeSet,
			HashSet<String> exclude) {

		RootType mainType = null;

		HashSet<Integer> set = new HashSet<Integer>();
		for (Taxon taxon : taxa) {

			if (taxon != null) {

				RootType rootType = taxon.getRootType();
				if (!mainTypeSet.contains(rootType)) {
					continue;
				}

				if (rootType.isMulticellular()) {

					int add2SetId = taxon.getId();
					int parentId = taxon.getParentId();
					while (true) {
						if (this.taxonMap.containsKey(parentId)) {
							Taxon parent = this.taxonMap.get(parentId);
							String parentName = parent.getName();
							parentId = parent.getParentId();
							if (exclude.contains(parentName) && this.taxonMap.containsKey(parentId)) {
								add2SetId = parentId;
							}
						} else {
							break;
						}
					}

					set.add(add2SetId);

					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								mainType = RootType.cellular_organisms;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else if (rootType.isUnicellular()) {
					set.add(taxon.getId());
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								return Taxon.root;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else {
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {

							} else if (mainType.isUnicellular()) {

							} else {
								return Taxon.root;
							}
						}
					}
				}
			}
		}

		if (mainType == null || !mainTypeSet.contains(mainType)) {
			return null;
		}

		if (set.size() == 0 || mainType == RootType.cellular_organisms) {
			return this.taxonMap.get(mainType.getId());
		}

		Integer[] usedIds = set.toArray(new Integer[set.size()]);

		if (usedIds.length == 1) {
			return this.taxonMap.get(usedIds[0]);
		}

		int[][] taxonArrays = new int[usedIds.length][];
		for (int i = 0; i < usedIds.length; i++) {
			Taxon taxon = this.taxonMap.get(usedIds[i]);
			taxonArrays[i] = this.getMainParentTaxonIds(taxon);
		}

		int blankRankId = blankRank.getMainId();

		int lcaId = 0;
		boolean find = false;

		L: for (int i = TaxonomyRanks.Superkingdom.getMainId(); i <= blankRankId; i++) {

			int lcaIdi = taxonArrays[0][i];

			for (int j = 1; j < taxonArrays.length; j++) {
				if (taxonArrays[j][i] != lcaIdi) {
					find = true;
					break L;
				}
			}
			if (lcaIdi != -1) {
				lcaId = lcaIdi;
			}
		}

		if (find) {
			if (lcaId == 0) {
				return this.taxonMap.get(mainType.getId());
			} else {
				return this.taxonMap.get(lcaId);
			}
		} else {
			for (int i = blankRankId + 1; i <= TaxonomyRanks.Species.getMainId(); i++) {
				int lcaIdi = 0;
				for (int j = 0; j < taxonArrays.length; j++) {
					if (taxonArrays[j][i] != -1) {
						lcaIdi = taxonArrays[j][i];
						break;
					}
				}

				if (lcaIdi != 0) {
					for (int j = 0; j < taxonArrays.length; j++) {
						if (taxonArrays[j][i] != -1 && taxonArrays[j][i] != lcaIdi) {
							return this.taxonMap.get(lcaId);
						}
					}
					lcaId = lcaIdi;
				}
			}

			return this.taxonMap.get(lcaId);
		}
	}
	
	/**
	 * used for testing
	 * @param taxonIds
	 * @param strategy
	 * @param sequence
	 * @return
	 */
	public Taxon getLCA4PepTaxDb(int[] taxonIds, TaxonomyRanks blankRank, HashSet<RootType> mainTypeSet,
			HashSet<String> exclude, String sequence) {

		RootType mainType = null;

		HashSet<Integer> set = new HashSet<Integer>();
		for (int taxId : taxonIds) {
			Taxon taxon = this.taxonMap.get(taxId);

			if (taxon != null) {

				RootType rootType = taxon.getRootType();
				if (!mainTypeSet.contains(rootType)) {
					continue;
				}

				if (rootType.isMulticellular()) {

					int add2SetId = taxId;
					int parentId = taxon.getParentId();
					while (true) {
						if (this.taxonMap.containsKey(parentId)) {
							Taxon parent = this.taxonMap.get(parentId);
							String parentName = parent.getName();
							parentId = parent.getParentId();
							if (exclude.contains(parentName) && this.taxonMap.containsKey(parentId)) {
								add2SetId = parentId;
							}
						} else {
							break;
						}
					}

					set.add(add2SetId);

					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								mainType = RootType.cellular_organisms;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else if (rootType.isUnicellular()) {
					set.add(taxId);
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {
								return Taxon.root;
							} else if (mainType.isUnicellular()) {
								return Taxon.root;
							} else {
								mainType = rootType;
							}
						}
					}
				} else {
					if (mainType == null) {
						mainType = rootType;
					} else {
						if (mainType != rootType) {
							if (mainType.isMulticellular()) {

							} else if (mainType.isUnicellular()) {

							} else {
								return Taxon.root;
							}
						}
					}
				}
			}
		}

		if (mainType == null || !mainTypeSet.contains(mainType)) {
			return null;
		}

		if (set.size() == 0 || mainType == RootType.cellular_organisms) {
			return this.taxonMap.get(mainType.getId());
		}

		Integer[] usedIds = set.toArray(new Integer[set.size()]);

		if (usedIds.length == 1) {
			return this.taxonMap.get(usedIds[0]);
		}

		int[][] taxonArrays = new int[usedIds.length][];
		for (int i = 0; i < usedIds.length; i++) {
			Taxon taxon = this.taxonMap.get(usedIds[i]);
			taxonArrays[i] = this.getMainParentTaxonIds(taxon);
		}

		int blankRankId = blankRank.getMainId();

		int lcaId = 0;
		boolean find = false;
		
		L: for (int i = TaxonomyRanks.Superkingdom.getMainId(); i <= blankRankId; i++) {

			int lcaIdi = taxonArrays[0][i];

			for (int j = 1; j < taxonArrays.length; j++) {
				if (taxonArrays[j][i] != lcaIdi) {
					find = true;
					break L;
				}
			}
			if (lcaIdi != -1) {
				lcaId = lcaIdi;
			}
		}

		if (find) {
			if (lcaId == 0) {
				return this.taxonMap.get(mainType.getId());
			} else {
				return this.taxonMap.get(lcaId);
			}
		} else {
			for (int i = blankRankId + 1; i <= TaxonomyRanks.Species.getMainId(); i++) {
				int lcaIdi = 0;
				for (int j = 0; j < taxonArrays.length; j++) {
					if (taxonArrays[j][i] != -1) {
						lcaIdi = taxonArrays[j][i];
						break;
					}
				}

				if (lcaIdi != 0) {
					for (int j = 0; j < taxonArrays.length; j++) {
						if (taxonArrays[j][i] != -1 && taxonArrays[j][i] != lcaIdi) {
							return this.taxonMap.get(lcaId);
						}
					}
					lcaId = lcaIdi;
				}
			}
			
			return this.taxonMap.get(lcaId);
		}
	}
	
	public Taxon getLCA4ProTaxDb(int[] taxonIds) {

		int lineageCount = -1;
		int[][] parents = new int[taxonIds.length][];
		int[] ends = new int[taxonIds.length];

		for (int i = 0; i < taxonIds.length; i++) {
			Taxon taxon = this.taxonMap.get(taxonIds[i]);

			if (taxon != null) {
				parents[i] = this.getMainParentTaxonIds(taxon);
				lineageCount = parents[i].length;

				for (int j = parents[i].length - 1; j >= 0; j--) {
					if (parents[i][j] > 0) {
						ends[i] = j;
						break;
					}
				}
			}
		}

		ArrayList<int[]> list = new ArrayList<int[]>();
		if (lineageCount > 0) {
			for (int i = 0; i < parents.length; i++) {
				if (parents[i] != null) {
					if (ends[i] == lineageCount - 1) {
						list.add(parents[i]);
					} else {
						boolean use = true;
						for (int j = 0; j < parents.length; j++) {
							if (parents[j] != null && i != j) {
								if (parents[i][ends[i]] == parents[j][ends[i]]) {
									use = false;
									break;
								}
							}
						}

						if (use) {
							list.add(parents[i]);
						}
					}
				}
			}
		}

		if (list.size() == 0) {
			return null;
		} else if (list.size() == 1) {
			int[] lineage = list.get(0);
			for (int i = lineage.length - 1; i >= 0; i--) {
				if (lineage[i] > 0) {
					return getTaxonFromId(lineage[i]);
				}
			}
			return null;
		} else {
			int[][] lineages = list.toArray(new int[list.size()][]);
			int taxid = -1;
			for (int i = TaxonomyRanks.Superkingdom.getMainId(); i <= TaxonomyRanks.Species.getMainId(); i++) {
				boolean same = true;
				int taxidi = -1;
				for (int j = 0; j < lineages.length; j++) {
					if (taxidi == -1) {
						taxidi = lineages[j][i];
					} else {
						if (taxidi != lineages[j][i]) {
							same = false;
							break;
						}
					}
				}
				if (same && taxidi != -1) {
					taxid = taxidi;
				}
			}
			return getTaxonFromId(taxid);
		}
	}
	
	public Taxon getLCA4ProTaxDb(Integer[] taxonIds) {

		int lineageCount = -1;
		int[][] parents = new int[taxonIds.length][];
		int[] ends = new int[taxonIds.length];

		for (int i = 0; i < taxonIds.length; i++) {
			Taxon taxon = this.taxonMap.get(taxonIds[i]);

			if (taxon != null) {
				parents[i] = this.getMainParentTaxonIds(taxon);
				lineageCount = parents[i].length;

				for (int j = parents[i].length - 1; j >= 0; j--) {
					if (parents[i][j] > 0) {
						ends[i] = j;
						break;
					}
				}
			}
		}

		ArrayList<int[]> list = new ArrayList<int[]>();
		if (lineageCount > 0) {
			for (int i = 0; i < parents.length; i++) {
				if (parents[i] != null) {
					if (ends[i] == lineageCount - 1) {
						list.add(parents[i]);
					} else {
						boolean use = true;
						for (int j = 0; j < parents.length; j++) {
							if (parents[j] != null && i != j) {
								if (parents[i][ends[i]] == parents[j][ends[i]]) {
									use = false;
									break;
								}
							}
						}

						if (use) {
							list.add(parents[i]);
						}
					}
				}
			}
		}

		if (list.size() == 0) {
			return null;
		} else if (list.size() == 1) {
			int[] lineage = list.get(0);
			for (int i = lineage.length - 1; i >= 0; i--) {
				if (lineage[i] > 0) {
					return getTaxonFromId(lineage[i]);
				}
			}
			return null;
		} else {
			int[][] lineages = list.toArray(new int[list.size()][]);
			int taxid = -1;
			for (int i = TaxonomyRanks.Superkingdom.getMainId(); i <= TaxonomyRanks.Species.getMainId(); i++) {
				boolean same = true;
				int taxidi = -1;
				for (int j = 0; j < lineages.length; j++) {
					if (taxidi == -1) {
						taxidi = lineages[j][i];
					} else {
						if (taxidi != lineages[j][i]) {
							same = false;
							break;
						}
					}
				}
				if (same && taxidi != -1) {
					taxid = taxidi;
				}
			}
			return getTaxonFromId(taxid);
		}
	}
	
	/**
	 * unipept rules
	 * @param taxonIds
	 * @return
	 */
	public Taxon getLCA(Integer[] taxonIds) {

		int[] values = new int[taxonIds.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = taxonIds[i];
		}

		return getLCA(values);
	}
	
	/**
	 * unipept rules
	 * @param taxonIds
	 * @return
	 */
	public Taxon getLCA(int[] taxonIds) {

		HashSet<RootType> rootType = new HashSet<RootType>();
		rootType.add(RootType.Archaea);
		rootType.add(RootType.Bacteria);
		rootType.add(RootType.Eukaryota);
		rootType.add(RootType.Viruses);

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		return getLCA4PepTaxDb(taxonIds, TaxonomyRanks.Family, rootType, exclude);
	}

	public String[] getTaxonArrays(int taxonId) {
		Taxon taxon = this.getTaxonFromId(taxonId);
		if (taxon == null) {
			return null;
		} else {
			return getTaxonArrays(taxon);
		}
	}
	
	public String[] getTaxonArrays(Taxon taxon) {
		String[] taxonomy = new String[8];
		while (true) {
			int rankid = taxon.getRankId();
			switch (rankid) {
			case 0: {
				taxonomy[0] = taxon.getName();
				break;
			}
			case 1: {
				taxonomy[1] = taxon.getName();
				break;
			}
			case 4: {
				taxonomy[2] = taxon.getName();
				break;
			}
			case 7: {
				taxonomy[3] = taxon.getName();
				break;
			}
			case 11: {
				taxonomy[4] = taxon.getName();
				break;
			}
			case 16: {
				taxonomy[5] = taxon.getName();
				break;
			}
			case 20: {
				taxonomy[6] = taxon.getName();
				break;
			}
			case 23: {
				taxonomy[7] = taxon.getName();
				break;
			}
			}
			int parentId = taxon.getParentId();
			if (this.taxonMap.containsKey(parentId)) {
				taxon = this.taxonMap.get(parentId);
			} else {
				break;
			}
		}
		return taxonomy;
	}

	public int[] getMainParentTaxonIds(int taxonId) {
		Taxon taxon = taxonMap.get(taxonId);
		if (taxon == null) {
			return null;
		} else {
			return getMainParentTaxonIds(taxon);
		}
	}
	
	public int[] getMainParentTaxonIds(Taxon taxon) {
		
		int[] taxonomy = new int[8];
		Arrays.fill(taxonomy, -1);

		while (true) {
			int rankid = taxon.getRankId();
			switch (rankid) {
			case -1: {
				break;
			}
			case 0: {
				taxonomy[0] = taxon.getId();
				break;
			}
			case 1: {
				taxonomy[1] = taxon.getId();
				break;
			}
			case 4: {
				taxonomy[2] = taxon.getId();
				break;
			}
			case 7: {
				taxonomy[3] = taxon.getId();
				break;
			}
			case 11: {
				taxonomy[4] = taxon.getId();
				break;
			}
			case 16: {
				taxonomy[5] = taxon.getId();
				break;
			}
			case 20: {
				taxonomy[6] = taxon.getId();
				break;
			}
			case 23: {
				taxonomy[7] = taxon.getId();
				break;
			}
			}
			int parentId = taxon.getParentId();
			if (this.taxonMap.containsKey(parentId)) {
				taxon = this.taxonMap.get(parentId);
			} else {
				break;
			}
		}
		
		return taxonomy;
	}
	
	public Taxon getMainParent(int taxonId) {
		return getMainParent(this.taxonMap.get(taxonId));
	}
	
	public Taxon getMainParent(Taxon taxon) {
		if (taxon == null) {
			return null;
		}
		if (taxon.getId() == 131567) {
			return taxon;
		}
		if (taxon.isMainRank()) {
			return taxon;
		} else {
			return getMainParent(this.taxonMap.get(taxon.getParentId()));
		}
	}
	
	public Taxon getKindom(Taxon taxon) {
		if (taxon == null) {
			return null;
		}

		int rankId = taxon.getRankId();
		if (rankId <= 0) {
			return null;
		} else if (rankId == 1) {
			return taxon;
		} else {
			while (true) {
				int parentId = taxon.getParentId();
				Taxon parent = this.getTaxonFromId(parentId);
				if (parent == null) {
					return null;
				}

				if (parent.getRankId() <= 0) {
					return null;
				} else if (parent.getRankId() == 1) {
					return parent;
				} else {
					taxon = parent;
				}
			}
		}
	}

	public HashMap<String, Integer>[] getRankNameMap() {
		@SuppressWarnings("unchecked")
		HashMap<String, Integer>[] maps = new HashMap[8];
		for (int i = 0; i < maps.length; i++) {
			maps[i] = new HashMap<String, Integer>();
		}

		for (Integer id : taxonMap.keySet()) {
			Taxon taxon = taxonMap.get(id);
			int rankid = taxon.getRankId();

			switch (rankid) {
			case 0: {
				maps[0].put(taxon.getName(), id);
				break;
			}
			case 1: {
				maps[1].put(taxon.getName(), id);
				break;
			}
			case 4: {
				maps[2].put(taxon.getName(), id);
				break;
			}
			case 7: {
				maps[3].put(taxon.getName(), id);
				break;
			}
			case 11: {
				maps[4].put(taxon.getName(), id);
				break;
			}
			case 16: {
				maps[5].put(taxon.getName(), id);
				break;
			}
			case 20: {
				maps[6].put(taxon.getName(), id);
				break;
			}
			case 23: {
				maps[7].put(taxon.getName(), id);
				break;
			}
			}
		}

		return maps;
	}
	
	public HashMap<String, Integer> taxa2Lca(HashMap<String, ArrayList<Taxon>> taxamap, TaxonomyRanks rank,
			HashSet<RootType> rootTypes, HashSet<String> exclude) {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String key : taxamap.keySet()) {
			ArrayList<Taxon> list = taxamap.get(key);
			Taxon[] taxa = list.toArray(new Taxon[list.size()]);
			Taxon taxon = this.getLCA4PepTaxDb(taxa, rank, rootTypes, exclude);
			if (taxon != null) {
				map.put(key, taxon.getId());
			}
		}

		return map;
	}
}
