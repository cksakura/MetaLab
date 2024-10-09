/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.par;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;

/**
 * MetaLab running parameter for the MetaLab 2.0 version
 * 
 * @author Kai Cheng
 *
 */
public class MetaParameterMQ extends MetaParameter {

	private MaxquantModification[] fixMods;
	private MaxquantModification[] variMods;

	private Enzyme enzyme;
	private int missCleavages;
	private int digestMode;

	private String quanMode;
	private MaxquantModification[][] labels;
	private MaxquantModification[] isobaric;
	private IsobaricTag isobaricTag;
	private boolean combineLabel;
	private double[][] isoCorFactor;

	private MetaMaxQuantPar csp;
	private MetaOpenPar osp;
	private MetaPep2TaxaPar ptp;

	private MetaPeptide[] peps;
	private MetaProtein[] pros;
	private String[] intensityTitles;
	private String quanResultType;
	private String quickOutput;

	public MetaParameterMQ() {
		super(MetaLabWorkflowType.MaxQuantWorkflow.name(), new MetaData(), "", "", false, "", MetaConstants.FTMS, 1, 1,
				true);
		this.fixMods = new MaxquantModification[] {};
		this.variMods = new MaxquantModification[] {};
		this.enzyme = Enzyme.Trypsin;
		this.missCleavages = 2;
		this.digestMode = 0;
		this.quanMode = MetaConstants.labelFree;
		this.labels = new MaxquantModification[][] { {}, {}, {} };
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};
		this.csp = MetaMaxQuantPar.getDefault();
		this.osp = MetaOpenPar.getDefault();
		this.ptp = MetaPep2TaxaPar.getDefault();
	}

	public MetaParameterMQ(MetaParameter metaPar) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata().getNoMetaInfo(), metaPar.getResult(),
				metaPar.getMicroDb(), metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(),
				metaPar.getCoreCount(), metaPar.getThreadCount(), metaPar.isMetaWorkflow(), metaPar.getMscp());

		this.fixMods = new MaxquantModification[] {};
		this.variMods = new MaxquantModification[] {};
		this.enzyme = Enzyme.Trypsin;
		this.missCleavages = 2;
		this.digestMode = 0;
		this.quanMode = MetaConstants.labelFree;
		this.labels = new MaxquantModification[][] { {}, {}, {} };
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};
		this.csp = MetaMaxQuantPar.getDefault();
		this.osp = MetaOpenPar.getDefault();
		this.ptp = MetaPep2TaxaPar.getDefault();
	}

	public MetaParameterMQ(MetaParameter metaPar, MaxquantModification[] fixMods, MaxquantModification[] variMods,
			Enzyme enzyme, int missCleavages, int digestMode, String quanMode, MaxquantModification[][] labels,
			MaxquantModification[] isobaric, IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor,
			boolean openSearch, MetaMaxQuantPar csp, MetaOpenPar osp, MetaPep2TaxaPar ptp) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata(), metaPar.getResult(), metaPar.getMicroDb(),
				metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(), metaPar.getCoreCount(),
				metaPar.getThreadCount(), metaPar.isMetaWorkflow());

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = quanMode;
		this.labels = labels;
		this.isobaric = isobaric;
		this.isobaricTag = isobaricTag;
		this.combineLabel = combineLabel;
		this.isoCorFactor = isoCorFactor;
		this.csp = csp;
		this.osp = osp;
		this.ptp = ptp;
	}

	public MetaParameterMQ(String workflowType, MetaData meta, String result, String microDb, boolean appendHostDb,
			String hostDB, String ms2ScanMode, int coreCount, int threadCount, boolean isOpenSearch,
			boolean isMetaWorkflow, MaxquantModification[] fixMods, MaxquantModification[] variMods, Enzyme enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[][] labels,
			MaxquantModification[] isobaric, IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor,
			boolean openSearch, MetaSsdbCreatePar mscp, MetaMaxQuantPar csp, MetaOpenPar osp, MetaPep2TaxaPar ptp) {

		super(workflowType, meta, result, microDb, appendHostDb, hostDB, ms2ScanMode, coreCount, threadCount,
				isMetaWorkflow, mscp);
		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = quanMode;
		this.labels = labels;
		this.isobaric = isobaric;
		this.isobaricTag = isobaricTag;
		this.combineLabel = combineLabel;
		this.isoCorFactor = isoCorFactor;
		this.csp = csp;
		this.osp = osp;
		this.ptp = ptp;
	}

	public IsobaricTag getIsobaricTag() {
		return isobaricTag;
	}

	public void setIsobaricTag(IsobaricTag isobaricTag) {
		this.isobaricTag = isobaricTag;
	}

	public int getNPlex() {
		if (this.isobaricTag != null) {
			return this.isobaricTag.getnPlex();
		} else {
			return 0;
		}
	}

	public String[] getIsobaricReportTitle() {
		if (this.isobaricTag != null) {
			return this.isobaricTag.getReportTitle();
		} else {
			return new String[] {};
		}
	}

	public boolean isCombineLabel() {
		return combineLabel;
	}

	public void setCombineLabel(boolean combineLabel) {
		this.combineLabel = combineLabel;
	}

	public boolean isOpenSearch() {
		return this.osp.isOpenSearch();
	}

	public MaxquantModification[] getVariMods() {
		return variMods;
	}

	public void setVariMods(MaxquantModification[] variMods) {
		this.variMods = variMods;
	}

	public MaxquantModification[] getFixMods() {
		return fixMods;
	}

	public void setFixMods(MaxquantModification[] fixMods) {
		this.fixMods = fixMods;
	}

	public Enzyme getEnzyme() {
		return enzyme;
	}

	public void setEnzyme(Enzyme enzyme) {
		this.enzyme = enzyme;
	}

	public int getMissCleavages() {
		return missCleavages;
	}

	public void setMissCleavages(int missCleavages) {
		this.missCleavages = missCleavages;
	}

	public int getDigestMode() {
		return digestMode;
	}

	public void setDigestMode(int digestMode) {
		this.digestMode = digestMode;
	}

	public String getQuanMode() {
		return quanMode;
	}

	public void setQuanMode(String quanMode) {
		if (this.quanMode == null || !this.quanMode.equals(quanMode)) {
			this.quanMode = quanMode;
			this.labels = new MaxquantModification[0][0];
			this.isobaric = new MaxquantModification[0];
			this.getMetadata().setLabelTitle(new String[0]);
		}
	}

	public MaxquantModification[][] getLabels() {
		return labels;
	}

	public void setLabels(MaxquantModification[][] labels) {
		this.labels = labels;
	}

	public MaxquantModification[] getIsobaric() {
		return isobaric;
	}

	public void setIsobaric(MaxquantModification[] isobaric) {
		this.isobaric = isobaric;
	}

	public double[][] getIsoCorFactor() {
		return isoCorFactor;
	}

	public void setIsoCorFactor(double[][] isoCorFactor) {
		this.isoCorFactor = isoCorFactor;
	}

	public MetaMaxQuantPar getCsp() {
		return csp;
	}

	public MetaOpenPar getOsp() {
		return osp;
	}

	public MetaPep2TaxaPar getPtp() {
		return ptp;
	}

	public void setCsp(MetaMaxQuantPar csp) {
		this.csp = csp;
	}

	public void setOsp(MetaOpenPar osp) {
		this.osp = osp;
	}

	public void setPtp(MetaPep2TaxaPar ptp) {
		this.ptp = ptp;
	}

	public MetaPeptide[] getPeps() {
		return peps;
	}

	public void setPeps(MetaPeptide[] peps) {
		this.peps = peps;
	}

	public MetaProtein[] getPros() {
		return pros;
	}

	public void setPros(MetaProtein[] pros) {
		this.pros = pros;
	}

	public String[] getIntensityTitles() {
		return intensityTitles;
	}

	public void setIntensityTitles(String[] intensityTitles) {
		this.intensityTitles = intensityTitles;
	}

	public String getQuanResultType() {
		return quanResultType;
	}

	public void setQuanResultType(String quanResultType) {
		this.quanResultType = quanResultType;
	}

	public String getQuickOutput() {
		return quickOutput;
	}

	public void setQuickOutput(String quickOutput) {
		this.quickOutput = quickOutput;
	}
}
