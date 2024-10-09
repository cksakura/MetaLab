/**
 * 
 */
package bmi.med.uOttawa.metalab.core.taxonomy;

import java.util.HashMap;

/**
 * @author Kai Cheng
 *
 */
public enum TaxonomyRanks {

	Root(-1, "Root", true, -1),

	Superkingdom(0, "Superkingdom", true, 0),

	Kingdom(1, "Kingdom", true, 1),

	Subkingdom(2, "Subkingdom", false, -1),

	Superphylum(3, "Superphylum", false, -1),

	Phylum(4, "Phylum", true, 2),

	Subphylum(5, "Subphylum", false, -1),

	Superclass(6, "Superclass", false, -1),

	Class(7, "Class", true, 3),

	Subclass(8, "Subclass", false, -1),

	Infraclass(9, "Infraclass", false, -1),

	Superorder(10, "Superorder", false, -1),

	Order(11, "Order", true, 4),

	Suborder(12, "Suborder", false, -1),

	Infraorder(13, "Infraorder", false, -1),

	Parvorder(14, "Parvorder", true, -1),

	Superfamily(15, "Superfamily", false, -1),

	Family(16, "Family", true, 5),

	Subfamily(17, "Subfamily", false, -1),

	Tribe(18, "Tribe", false, -1),

	Subtribe(19, "Subtribe", false, -1),

	Genus(20, "Genus", true, 6),

	Subgenus(21, "Subgenus", false, -1),

	Species_group(22, "Species group", false, -1),

	Species(23, "Species", true, 7),

	Species_subgroup(24, "Species subgroup", false, -1),

	Subspecies(25, "Subspecies", false, -1),

	Varietas(26, "Varietas", false, -1),

	Forma(27, "Forma", false, -1),

	;

	protected static final String[] ranks = new String[] { "Superkingdom", "Kingdom", "Subkingdom", "Superphylum",
			"Phylum", "Subphylum", "Superclass", "Class", "Subclass", "Infraclass", "Superorder", "Order", "Suborder",
			"Infraorder", "Parvorder", "Superfamily", "Family", "Subfamily", "Tribe", "Subtribe", "Genus", "Subgenus",
			"Species group", "Species", "Species subgroup", "Subspecies", "Varietas", "Forma" };

	private int id;
	private String name;
	private boolean mainRank;
	private int mainId;

	TaxonomyRanks(int id, String name, boolean mainRank, int mainId) {
		this.id = id;
		this.name = name;
		this.mainRank = mainRank;
		this.mainId = mainId;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isMainRank() {
		return mainRank;
	}

	public int getMainId() {
		return mainId;
	}

	public static HashMap<String, Integer> getRankIdMap() {
		HashMap<String, Integer> rankIdMap = new HashMap<String, Integer>();
		for (TaxonomyRanks tr : TaxonomyRanks.values()) {
			rankIdMap.put(tr.getName(), tr.getId());
		}
		return rankIdMap;
	}

	public static TaxonomyRanks getRankFromId(int id) {
		TaxonomyRanks[] values = TaxonomyRanks.values();
		if (id >= -1 && id <= 28) {
			return values[id + 1];
		} else {
			return null;
		}
	}

	public static TaxonomyRanks getRankFromName(String name) {
		TaxonomyRanks[] values = TaxonomyRanks.values();
		for (TaxonomyRanks tr : values) {
			if (tr.getName().equalsIgnoreCase(name)) {
				return tr;
			}
		}
		return null;
	}

	public static String getRankNameFromId(int id) {
		TaxonomyRanks[] values = TaxonomyRanks.values();
		if (id >= -1 && id <= 28) {
			return values[id + 1].getName();
		} else {
			return null;
		}
	}

	public static TaxonomyRanks[] getMainRanks() {
		TaxonomyRanks[] values = new TaxonomyRanks[] { Superkingdom, Kingdom, Phylum, Class, Order, Family, Genus,
				Species };
		return values;
	}
	
	public static TaxonomyRanks[] getMainRanks7() {
		TaxonomyRanks[] values = new TaxonomyRanks[] { Superkingdom, Phylum, Class, Order, Family, Genus, Species };
		return values;
	}

	public static int[] getMainRankIds() {
		TaxonomyRanks[] mainRanks = getMainRanks();
		int[] mainRankIds = new int[mainRanks.length];
		for (int i = 0; i < mainRankIds.length; i++) {
			mainRankIds[i] = mainRanks[i].getId();
		}
		return mainRankIds;
	}
}
