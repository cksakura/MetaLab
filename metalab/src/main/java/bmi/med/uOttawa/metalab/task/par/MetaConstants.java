package bmi.med.uOttawa.metalab.task.par;

public class MetaConstants {

	/**
	 * MS2 in high resolution
	 */
	public static String FTMS = "FTMS";

	/**
	 * MS2 in low resolution
	 */
	public static String ITMS = "ITMS";

	/**
	 * The description in the GUI of FTMS
	 */
	public static String FTDescription = "MS2 scan in Fourier transform mass spectrometry, high resolution";

	/**
	 * The description in the GUI of ITMS
	 */
	public static String ITDescription = "MS2 scan in ion trap mass spectrometry, low resolution";

	/**
	 * MS instrument in pFind
	 */
	public static String HCD_FTMS = "HCD-FTMS";

	/**
	 * MS instrument in pFind
	 */
	public static String HCD_ITMS = "HCD-ITMS";

	/**
	 * MS instrument in pFind
	 */
	public static String CID_FTMS = "CID-FTMS";

	/**
	 * MS instrument in pFind
	 */
	public static String CID_ITMS = "CID-ITMS";

	/**
	 * Database search engine: MaxQuant
	 */
	public static String maxQuant = "MaxQuant";

	/**
	 * Database search engine: XTandem
	 */
	public static String xTandem = "XTandem";

	/**
	 * Database search engine: MSFragger
	 */
	public static String msfragger = "MSFragger";

	/**
	 * Database search engine: MSFragger
	 * <p>
	 * Post-analysis by MetaLab
	 */
	public static String openSearch = "openSearch";

	/**
	 * Database search engine: pFind
	 */
	public static String pFind = "pFind";
	
	/**
	 * Database search engine: pFind
	 * Searching high abundant protein database
	 */
	public static String pFindHap = "pFindHap";
	
	/**
	 * Database search engine: pFind
	 * Searching high abundant protein database
	 * Metagenome-assembled genomes
	 */
	public static String pFindMag = "pFindMag";
	
	/**
	 * Database search engine: Fragpipe
	 * Searching high abundant protein database
	 * Metagenome-assembled genomes
	 */
	public static String fragpipeIGC = "FragPipeIGC";

	/**
	 * Database search engine: Fragpipe
	 * Searching high abundant protein database
	 * Metagenome-assembled genomes
	 */
	public static String fragpipeMAG = "FragPipeMAG";
	
	/**
	 * Database search engine: Fragpipe
	 * Searching high abundant protein database
	 * Metagenome-assembled genomes
	 */
	public static String alphapeptMAG = "AlphapeptMAG";
	
	/**
	 * Quantification: flashLFQ
	 */
	public static String flashLFQ = "flashLFQ";
	
	/**
	 * Quantification: flashLFQ
	 */
	public static String diaNN = "Dia-NN";

	/**
	 * Quantification mode: label free
	 */
	public static String labelFree = "Label free";

	/**
	 * Quantification mode: isotopic labeling
	 */
	public static String isotopicLabel = "Isotopic labeling";

	/**
	 * Quantification mode: isobaric labeling
	 */
	public static String isobaricLabel = "Isobaric labeling";

	/**
	 * Peptide-to-taxon database: Built-in
	 */
	public static String builtin = "Built-in";

	/**
	 * Peptide-to-taxon database: Built-in
	 */
	public static String unipept = "Unipept";

	public static String humanGutProDb = "Human gut microbiome protein sequence database";

	public static String humanGutProDbLink = "ftp://ftp.cngb.org/pub/SciRAID/Microbiome/humanGut_9.9M/GeneCatalog/IGC.pep.gz";

	public static long humanGutProDbLength = 1717986920L;

	public static String mouseGutProDb = "Mouse gut microbiome protein sequence database";

	public static String mouseGutProDbLink = "ftp://parrot.genomics.cn/gigadb/pub/10.5524/100001_101000/100114/Genecatalog/184sample_2.6M.GeneSet.pep.gz";

	public static long mouseGutProDbLength = 440401920L;

	public static String pep2TaxDb = "Taxonomy analysis database";

	public static String pep2TaxDbLink = "http://account.imetalab.ca/samples/pep2taxa.gz";

	public static long pep2TaxDbLength = 35092336640L;

	public static String pro2FuncDb = "Functional annotation database";

	public static String pro2FuncDbLink = "http://account.imetalab.ca/samples/func.db";

	public static long pro2FuncDbLength = 8801812480L;
}
