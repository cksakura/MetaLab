/**
 * 
 */
package bmi.med.uOttawa.metalab.core.taxonomy;

/**
 * @author Kai Cheng
 *
 */
public enum RootType {

	cellular_organisms(131567, "cellular organisms", true, false),

	Archaea(2157, "Archaea", true, false),

	Bacteria(2, "Bacteria", true, false),

	Eukaryota(2759, "Eukaryota", true, false),

	Viroids(12884, "Viroids", false, true),

	Viruses(10239, "Viruses", false, true),

	other(28384, "other sequences", false, false),

	unclassified(12908, "unclassified sequences", false, false);

	private int id;
	private String name;
	private boolean multicellular;
	private boolean unicellular;

	RootType(int id, String name, boolean multicellular, boolean unicellular) {
		this.id = id;
		this.name = name;
		this.multicellular = multicellular;
		this.unicellular = unicellular;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isMulticellular() {
		return multicellular;
	}

	public boolean isUnicellular() {
		return unicellular;
	}

	public static RootType getRootType(int taxId) {
		for (RootType rootType : RootType.values()) {
			if (rootType.getId() == taxId) {
				return rootType;
			}
		}
		return null;
	}
}
