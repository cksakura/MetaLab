package bmi.med.uOttawa.metalab.task.mag;

import java.io.File;
@Deprecated
public enum MagDatabase {

	human_gut("human-gut-v2-0-1", "Unified Human Gastrointestinal Genome (UHGG) v2.0.1", "2.0.1", 4744, "12/15/2021",
			true, "uniprot.Homo_sapiens.fasta", 0, 49,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-gut/v2.0.1/"),

	human_oral("human-oral-v1-0", "Human Oral v1.0", "1.0", 452, "12/17/2021", true, "uniprot.Homo_sapiens.fasta", 2980,
			2992, "http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-oral/v1.0/"),

	cow_rumen("cow-rumen-v1-0", "Cow Rumen v1.0", "1.0", 2729, "12/17/2021", true, "uniprot.bovine.fasta", 2900, 2955,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/cow-rumen/v1.0/"),

	marine("marine-v1-0", "Marine v1.0", "1.0", 1496, "12/17/2021", false, "", 2960, 2975,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/marine/v1.0/"),

	nm_fish_gut("non-model-fish-gut-v2-0", "Non-model Fish Gut v2.0", "2.0", 178, "4/26/2023", false, "", 2995, 2996,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/non-model-fish-gut/v2.0/"),

	pig_gut("pig-gut-v1-0", "Pig Gut v1.0", "1.0", 1376, "11/29/2022", true, "uniprot.sus_scrofa.fasta", 2997, 3036,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/pig-gut/v1.0/"),

	zebrafish_fecal("zebrafish-fecal-v1-0", "Zebrafish Fecal v1.0", "1.0", 79, "11/28/2022", true,
			"uniprot.zebrafish.fasta", 2993, 2993,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/zebrafish-fecal/v1.0/"),
	
	chicken_gut("chicken-gut-v1-0", "Chicken Gut v1.0", "1.0", 1322, "4/26/2023", true,
			"uniprot.chicken.fasta", 3076, 3209,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/chicken-gut/v1.0/"),

	honeybee_gut("honeybee-gut-v1-0", "Honeybee gut v1.0", "1.0", 154, "7/7/2023", true,
			"uniprot.apis.fasta", 3210, 3216,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/honeybee-gut/v1.0/"),
	
	human_vaginal("human-vaginal-v1-0", "Human Vaginal v1.0", "1.0", 280, "8/15/2023", true,
			"uniprot.Homo_sapiens.fasta", 3037, 3043,
			"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-vaginal/v1.0/"),
	
	local("local MAG database", "local MAG database", "0.0", 0, "01/01/2022", true, "", 0, 0, "");

	String idString;
	String nameString;
	String versionString;
	int genomeCount;
	String updateString;
	boolean hasHost;
	String hostNameString;
	int startId;
	int endId;
	String baseLinkString;

	String identifier;
	String fileDirString;
	String taxDbString;
	String funcDbString;
	int predictedFileCount;
	int hapDistinctLibCount;
	int libFileCount;

	MagDatabase(String idString, String nameString, String versionString, int genomeCount, String updateString,
			boolean hasHost, String hostNameString, int startId, int endId, String baseLinkString) {
		this.idString = idString;
		this.nameString = nameString;
		this.versionString = versionString;
		this.genomeCount = genomeCount;
		this.updateString = updateString;
		this.hasHost = hasHost;
		this.hostNameString = hostNameString;
		this.startId = startId;
		this.endId = endId;
		this.baseLinkString = baseLinkString;
	}

	public String getIdString() {
		return idString;
	}

	public String getNameString() {
		return nameString;
	}

	public String getVersionString() {
		return versionString;
	}

	public int getGenomeCount() {
		return genomeCount;
	}

	public String getUpdateString() {
		return updateString;
	}

	public boolean hasHost() {
		return hasHost;
	}

	public String getHostNameString() {
		return hostNameString;
	}

	public int getStartId() {
		return startId;
	}

	public int getEndId() {
		return endId;
	}

	public String getBaseLinkString() {
		return baseLinkString;
	}

	public String getFileDirString() {
		if (fileDirString == null) {
			return "";
		}
		return fileDirString;
	}

	public void setFileDirString(String fileDirString) {
		this.fileDirString = fileDirString;
		this.taxDbString = fileDirString + "\\genomes-all_metadata.tsv";
		this.funcDbString = fileDirString + "\\eggNOG";
	}

	public String getTaxDbString() {
		return taxDbString;
	}

	public void setTaxDbString(String taxDbString) {
		this.taxDbString = taxDbString;
	}

	public String getFuncDbString() {
		return funcDbString;
	}

	public void setFuncDbString(String funcDbString) {
		this.funcDbString = funcDbString;
	}

	public int getPredictedPepCount() {
		if (fileDirString == null) {
			return 0;
		}
		File dbFile = new File(fileDirString, "original_db");
		if (!dbFile.exists() || !dbFile.isDirectory()) {
			return 0;
		}
		int count = 0;
		File[] files = dbFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.endsWith(".faa")) {
				name = name.substring(0, name.length() - 3);
				File predictedPepFile = new File(dbFile, name + "predicted.tsv");
				if (predictedPepFile.exists()) {
					count++;
				}
			}
		}

		return count;
	}

	public boolean isLibReady() {
		return false;
	}

	public File getSpeclibFolder() {
		return new File(fileDirString, "speclib");
	}
	
	public File getPredictedFolder() {
		return new File(fileDirString, "predicted");
	}

	public File getOriginalDbFolder() {
		return new File(fileDirString, "original_db");
	}

	public File getHapFastaFile() {
		return new File(fileDirString, "rib_elon.fasta");
	}
	
	public File getHapFastaTDFile() {
		return new File(fileDirString, "rib_elon_td.fasta");
	}

	public File getHapNondisLibFile() {
		return new File(fileDirString, "rib_elon_nondistinct.predicted.speclib");
	}
	
	public File getHapNondisFastaFile() {
		return new File(fileDirString, "rib_elon_nondistinct.fasta");
	}

	public File getHapDisLibFolder() {
		return new File(fileDirString, "rib_elon_distinct_speclib");
	}

	public File getHostFile() {
		return new File(fileDirString, hostNameString);
	}

	public File getHostLibFile() {
		return new File(fileDirString, hostNameString.replace("fasta", "predicted.speclib"));
	}

	public int getPredictedFileCount() {
		return predictedFileCount;
	}

	public void setPredictedFileCount(int predictedFileCount) {
		this.predictedFileCount = predictedFileCount;
	}

	public int getHapDistinctLibCount() {
		return hapDistinctLibCount;
	}

	public void setHapDistinctLibCount(int hapDistinctLibCount) {
		this.hapDistinctLibCount = hapDistinctLibCount;
	}

	public int getLibFileCount() {
		return libFileCount;
	}

	public void setLibFileCount(int libFileCount) {
		this.libFileCount = libFileCount;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	
}
