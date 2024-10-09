/**
 * 
 */
package bmi.med.uOttawa.metalab.task;

/**
 * The complete workflow in MetaLab.
 * 
 * @author Kai Cheng
 * @deprecated
 */
public enum MetaLabTaskType {

	StandardClosedSearch(0, "Proteomics closed search"),

	StandardOpenSearch(1, "Proteomics open search"),

	MetaClosedSearch(2, "Metaproteomics closed search"),

	MetaOpenSearch(3, "Metaproteomics open search"),

	TaxonomyAnalysis(4, "Taxonomy analysis"),

	FunctionalAnnotation(5, "Functional annotation"),

	MetaGlycoSearch(6, "Metaproteomics glycopeptide search");

	int id;
	String name;

	MetaLabTaskType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public static MetaLabTaskType parse(int id) {
		for (MetaLabTaskType tt : MetaLabTaskType.values()) {
			if (tt.getId() == id) {
				return tt;
			}
		}
		return null;
	}
}
