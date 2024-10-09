/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.par;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaParameterV1 extends AbstractParameter {

	private String[] variMods;
	private String[] fixMods;
	private String[] enzymes;
	private int digestMode;
	private String database;
	private boolean appendHostDb;
	private String[] hostDBs;
	private String[][] labels;
	private String[] isobaric;
	private double[][] isoCorFactor;
	private int isobaricLabelCount;
	private int quanResultType = 0;
	private int instruType;
	private int threadCount;
	private boolean buildIn;
	private boolean unipept;
	private String[][] rawFiles;

	private String quanMode;
	private int minRatioCount;
	private int lfqMinRatioCount;
	private double psmFdr;
	private double proFdr;
	private boolean cluster;

	private boolean[] workflow;

	private File[] pepIdenResults;
	private int pepIdenResultType;

	private HashSet<RootType> usedRootTypes;
	private TaxonomyRanks ignoreBlankRank;
	private HashSet<String> excludeTaxa;

	private int functionSampleId;
	private String sampleName;
	private File proteinIdenResult;

	public static final int quanResultCombine = 0;
	public static final int quanResultSeparate = 1;
	public static final int quanResultAll = 2;

	public MetaParameterV1(String[] variMods, String[] fixMods, String[] enzymes, String database, String[] hostDBs,
			boolean appendHostDb, String[][] labels, String[] isobaric, double[][] isoCorFactor, int instruType,
			int threadCount, boolean buildIn, boolean unipept, String[][] rawFiles, String result, String resource,
			int minRatioCount, int lfqMinRatioCount, double psmFdr, double proFdr, boolean cluster, boolean[] workflow,
			HashSet<RootType> usedRootTypes, TaxonomyRanks ignoreBlankRank, HashSet<String> excludeTaxa) {

		super(resource, result);

		this.variMods = variMods;
		this.fixMods = fixMods;
		this.enzymes = enzymes;
		this.database = database;
		this.hostDBs = hostDBs;
		this.appendHostDb = appendHostDb;
		this.labels = labels;
		this.isobaric = isobaric;
		this.isoCorFactor = isoCorFactor;
		this.instruType = instruType;
		this.threadCount = threadCount;
		this.buildIn = buildIn;
		this.unipept = unipept;
		this.rawFiles = rawFiles;
		this.minRatioCount = minRatioCount;
		this.lfqMinRatioCount = lfqMinRatioCount;
		this.psmFdr = psmFdr;
		this.proFdr = proFdr;
		this.cluster = cluster;
		this.workflow = workflow;
		this.usedRootTypes = usedRootTypes;
		this.ignoreBlankRank = ignoreBlankRank;
		this.excludeTaxa = excludeTaxa;
	}

	public MetaParameterV1(String[] variMods, String[] fixMods, String[] enzymes, int digestMode, String database,
			String[] hostDBs, boolean appendHostDb, String[][] labels, String[] isobaric, double[][] isoCorFactor,
			int instruType, int threadCount, boolean buildIn, boolean unipept, String[][] rawFiles, String result,
			String resource, String quanMode, int minRatioCount, int lfqMinRatioCount, double psmFdr, double proFdr,
			boolean cluster, boolean[] workflow, HashSet<RootType> usedRootTypes, TaxonomyRanks ignoreBlankRank,
			HashSet<String> excludeTaxa, int functionSampleId) {

		super(resource, result);

		this.variMods = variMods;
		this.fixMods = fixMods;
		this.enzymes = enzymes;
		this.digestMode = digestMode;
		this.database = database;
		this.hostDBs = hostDBs;
		this.appendHostDb = appendHostDb;
		this.labels = labels;
		this.isobaric = isobaric;
		this.isoCorFactor = isoCorFactor;
		this.instruType = instruType;
		this.threadCount = threadCount;
		this.buildIn = buildIn;
		this.unipept = unipept;
		this.rawFiles = rawFiles;
		this.quanMode = quanMode;
		this.minRatioCount = minRatioCount;
		this.lfqMinRatioCount = lfqMinRatioCount;
		this.psmFdr = psmFdr;
		this.proFdr = proFdr;
		this.cluster = cluster;
		this.workflow = workflow;
		this.usedRootTypes = usedRootTypes;
		this.ignoreBlankRank = ignoreBlankRank;
		this.excludeTaxa = excludeTaxa;
		this.functionSampleId = functionSampleId;
	}

	public MetaParameterV1() {
		this(new String[] {}, new String[] {}, new String[] {}, 0, "", new String[] {}, false,
				new String[][] { {}, {}, {} }, new String[] {}, new double[][] {}, 0, 1, false, false,
				new String[][] { {}, {} }, "", "", MetaConstants.labelFree, 1, 1, 0.01, 0.01, true,
				new boolean[] { true, true, true, true }, new HashSet<RootType>(), TaxonomyRanks.Family,
				new HashSet<String>(), 0);

		this.usedRootTypes = new HashSet<RootType>();
		this.usedRootTypes.add(RootType.Bacteria);
		this.usedRootTypes.add(RootType.Eukaryota);
		this.usedRootTypes.add(RootType.Archaea);
		this.usedRootTypes.add(RootType.Viruses);
		this.ignoreBlankRank = TaxonomyRanks.Family;
		this.excludeTaxa = new HashSet<String>();
		this.excludeTaxa.add("environmental samples");
	}

	public String[] getVariMods() {
		return variMods;
	}

	public String[] getFixMods() {
		return fixMods;
	}

	public String[] getEnzymes() {
		return enzymes;
	}

	public int getDigestMode() {
		return digestMode;
	}

	public String getDatabase() {
		return database;
	}

	public String[] getHostDBs() {
		return hostDBs;
	}

	public boolean isAppendHostDb() {
		return appendHostDb;
	}

	public String[][] getLabels() {
		return labels;
	}

	public String[] getIsobaric() {
		return isobaric;
	}

	public double[][] getIsoCorFactor() {
		return isoCorFactor;
	}

	public int getQuanResultType() {
		return quanResultType;
	}

	public int getInstruType() {
		return instruType;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public boolean isBuildIn() {
		return buildIn;
	}

	public boolean isUnipept() {
		return unipept;
	}

	public String[][] getRawFiles() {
		return rawFiles;
	}

	public void setDigestMode(int digestMode) {
		this.digestMode = digestMode;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setHostDBs(String[] hostDBs) {
		this.hostDBs = hostDBs;
	}

	public void setAppendHostDb(boolean appendHostDb) {
		this.appendHostDb = appendHostDb;
	}

	public void setInstruType(int instruType) {
		this.instruType = instruType;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public void setBuildIn(boolean buildIn) {
		this.buildIn = buildIn;
	}

	public void setUnipept(boolean unipept) {
		this.unipept = unipept;
	}

	public void setVariMods(String[] variMods) {
		this.variMods = variMods;
	}

	public void setFixMods(String[] fixMods) {
		this.fixMods = fixMods;
	}

	public void setEnzymes(String[] enzymes) {
		this.enzymes = enzymes;
	}

	public void setLabels(String[][] labels) {
		this.labels = labels;
	}

	public void setIsobaric(String[] isobaric) {
		this.isobaric = isobaric;
	}

	public void setIsoCorFactor(double[][] isoCorFactor) {
		this.isoCorFactor = isoCorFactor;
	}

	public int getIsobaricLabelCount() {
		if (isobaricLabelCount > 0) {

		} else {
			HashSet<String> set = new HashSet<String>();
			for (String isolabel : isobaric) {
				String[] cs = isolabel.split("-");
				if (cs.length == 2) {
					if (cs[1].startsWith("Lys")) {
						set.add(cs[1].substring(3));
					} else if (cs[1].startsWith("Nter")) {
						set.add(cs[1].substring(4));
					}
				}
			}
			this.isobaricLabelCount = set.size();
			return set.size();
		}

		return isobaricLabelCount;
	}

	public void setIsobaricLabelCount(int isobaricLabelCount) {
		this.isobaricLabelCount = isobaricLabelCount;
	}

	public void setQuanResultType(int quanResultType) {
		this.quanResultType = quanResultType;
	}

	public void setRawFiles(String[][] rawFiles) {
		this.rawFiles = rawFiles;
	}

	public String getQuanMode() {
		return quanMode;
	}

	public int getMinRatioCount() {
		return minRatioCount;
	}

	public int getLfqMinRatioCount() {
		return lfqMinRatioCount;
	}

	public double getPsmFdr() {
		return psmFdr;
	}

	public double getProFdr() {
		return proFdr;
	}

	public void setQuanMode(String quanMode) {
		this.quanMode = quanMode;
	}

	public void setMinRatioCount(int minRatioCount) {
		this.minRatioCount = minRatioCount;
	}

	public void setLfqMinRatioCount(int lfqMinRatioCount) {
		this.lfqMinRatioCount = lfqMinRatioCount;
	}

	public void setPsmFdr(double psmFdr) {
		this.psmFdr = psmFdr;
	}

	public void setProFdr(double proFdr) {
		this.proFdr = proFdr;
	}

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public boolean[] getWorkflow() {
		return workflow;
	}

	public void setWorkflow(boolean[] workflow) {
		this.workflow = workflow;
	}

	public File[] getPepIdenResults() {
		return pepIdenResults;
	}

	public void setPepIdenResults(File[] pepIdenResults) {
		this.pepIdenResults = pepIdenResults;
	}

	public int getPepIdenResultType() {
		return pepIdenResultType;
	}

	public void setPepIdenResultType(int pepIdenResultType) {
		this.pepIdenResultType = pepIdenResultType;
	}

	public HashSet<RootType> getUsedRootTypes() {
		return usedRootTypes;
	}

	public void setUsedRootTypes(HashSet<RootType> usedRootTypes) {
		this.usedRootTypes = usedRootTypes;
	}

	public TaxonomyRanks getIgnoreBlankRank() {
		return ignoreBlankRank;
	}

	public void setIgnoreBlankRank(TaxonomyRanks ignoreBlankRank) {
		this.ignoreBlankRank = ignoreBlankRank;
	}

	public HashSet<String> getExcludeTaxa() {
		return excludeTaxa;
	}

	public void setExcludeTaxa(HashSet<String> excludeTaxa) {
		this.excludeTaxa = excludeTaxa;
	}

	public int getFunctionSampleId() {
		return functionSampleId;
	}

	public void setFunctionSampleId(int functionSampleId) {
		this.functionSampleId = functionSampleId;
	}

	public File getSSDatabase() {
		if (database.length() > 0) {
			File ssDatabase = new File(getFastaDir(),
					database.substring(database.lastIndexOf("\\") + 1, database.lastIndexOf("."))
							+ ".sample_specific.fasta");

			return ssDatabase;
		} else {
			return new File("");
		}
	}

	public File getProteinIdenResult() {
		return proteinIdenResult;
	}

	public void setProteinIdenResult(File proteinIdenResult) {
		this.proteinIdenResult = proteinIdenResult;
	}

	public String getMaxQuantCmd() {
		return getResource() + "\\mq_bin\\MaxQuantCmd.exe";
	}

	public String getMaxQuantPar() {
		return getResource() + "\\mq_bin\\mqpar.xml";
	}

	public String getMaxQuantMod() {
		return getResource() + "\\mq_bin\\conf\\modifications.xml";
	}

	public String getMaxQuantEnzyme() {
		return getResource() + "\\mq_bin\\conf\\enzymes.xml";
	}

	public String getSampleName() {
		if (sampleName == null) {

			if (functionSampleId == 0) {
				sampleName = "Unknown";
			} else {
				File[] funcDbs = (new File(getResource() + "\\function")).listFiles();
				HashSet<String> sampleSet = new HashSet<String>();
				for (int i = 0; i < funcDbs.length; i++) {
					String name = funcDbs[i].getName();
					if (name.endsWith("gc")) {
						int sampleBegin = name.indexOf("_");
						int sampleEnd = name.indexOf(".");
						if (sampleBegin > 0 && sampleEnd > sampleBegin) {
							String sample = name.substring(sampleBegin + 1, sampleEnd);
							if (!sample.endsWith("COG") && !sample.endsWith("NOG")) {
								sampleSet.add(sample);
							}
						}
					}
				}
				String[] samples = sampleSet.toArray(new String[sampleSet.size()]);
				Arrays.sort(samples);

				if (this.functionSampleId <= samples.length) {
					sampleName = samples[functionSampleId - 1];
				}
			}
		}
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getCog() {
		return getResource() + "\\function\\COG_" + sampleName + ".gc";
	}

	public String getNOG() {
		return getResource() + "\\function\\NOG_" + sampleName + ".gc";
	}

	public String getGOBP() {
		return getResource() + "\\function\\GOBP_" + sampleName + ".gc";
	}

	public String getGOCC() {
		return getResource() + "\\function\\GOCC_" + sampleName + ".gc";
	}

	public String getGOMF() {
		return getResource() + "\\function\\GOMF_" + sampleName + ".gc";
	}

	public String getKEGG() {
		return getResource() + "\\function\\KEGG_" + sampleName + ".gc";
	}

	public String getKO() {
		return getResource() + "\\function\\KO_" + sampleName + ".gc";
	}
}
