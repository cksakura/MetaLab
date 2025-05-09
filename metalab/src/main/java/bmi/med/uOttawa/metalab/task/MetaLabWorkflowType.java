package bmi.med.uOttawa.metalab.task;

public enum MetaLabWorkflowType {

	MaxQuantWorkflow(0, "MaxQuant workflow"),

	MSFraggerWorkflow(1, "MSFragger workflow"),

	pFindWorkflow(2, "pFind workflow"),

	TaxonomyAnalysis(3, "Taxonomy analysis"),

	FunctionalAnnotation(4, "Functional annotation"),

	pFindHGM(5, "pFind human gut microbiome (HGM) workflow"),

	pFindMAG(6, "pFind metagenome-assembled genomes (MAG) workflow"),

	DiaNNMAG(7, "DiaNN metagenome-assembled genomes (MAG) workflow"),

	DdaDiaMAG(8, "DDA Dia workflow"),

	FragpipeIMMag(9, "Fragpipe metagenome-assembled genomes (MAG) workflow"),

	AlphapeptIMMag(10, "Alphapept metagenome-assembled genomes (MAG) workflow"),
	
	DiaNNMAGRT(11, "DiaNN metagenome-assembled genomes (MAG) realtime workflow"),
	
	DiaNNMAGTG(12, "DiaNN metagenome-assembled genomes (MAG) target workflow"),
	
	SageMAG(13, "Sage metagenome-assembled genomes (MAG) workflow");

	int id;
	String name;

	MetaLabWorkflowType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return name;
	}

	public static MetaLabWorkflowType parse(int id) {
		for (MetaLabWorkflowType tt : MetaLabWorkflowType.values()) {
			if (tt.getId() == id) {
				return tt;
			}
		}
		return null;
	}

}
