/**
 * 
 */
package bmi.med.uOttawa.metalab.task.pfind.par;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;

/**
 * @author Kai Cheng
 *
 */
public class MetaParameterPFind extends MetaParameter {

	private String[] fixMods;
	private String[] variMods;

	private String enzyme;
	private int missCleavages;
	private int digestMode;

	private String quanMode;
	private MaxquantModification[] isobaric;
	private IsobaricTag isobaricTag;
	private boolean combineLabel;
	private double[][] isoCorFactor;

	protected MetaOpenPar osp;
	protected MetaPep2TaxaPar ptp;

	public MetaParameterPFind() {
		super(MetaLabWorkflowType.pFindWorkflow.name(), new MetaData(), "", "", false, "", MetaConstants.HCD_FTMS, 1, 1,
				true);

		this.fixMods = new String[] { "Carbamidomethyl[C] 0" };
		this.variMods = new String[] { "Acetyl[ProteinN-term] 0", "Oxidation[M] 0" };
		this.enzyme = "Trypsin_P";
		this.missCleavages = 2;
		this.digestMode = 0;
		this.quanMode = MetaConstants.labelFree;
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};

		this.osp = MetaOpenPar.getDefault();
		this.ptp = MetaPep2TaxaPar.getDefault();
	}

	public MetaParameterPFind(MetaParameter metaPar) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata().getNoMetaInfo(), metaPar.getResult(),
				metaPar.getMicroDb(), metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(),
				metaPar.getCoreCount(), metaPar.getThreadCount(), metaPar.isMetaWorkflow(), metaPar.getMscp());

		this.fixMods = new String[] { "Carbamidomethyl[C] 0" };
		this.variMods = new String[] { "Acetyl[ProteinN-term] 0", "Oxidation[M] 0" };
		this.enzyme = "Trypsin_P";
		this.missCleavages = 2;
		this.digestMode = 0;
		this.quanMode = MetaConstants.labelFree;
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};

		this.osp = MetaOpenPar.getDefault();
		this.ptp = MetaPep2TaxaPar.getDefault();
	}

	/**
	 * Extended by MetaParameterHGM
	 * 
	 * @param metaPar
	 * @param fixMods
	 * @param variMods
	 * @param enzyme
	 * @param missCleavages
	 * @param digestMode
	 * @param quanMode
	 * @param isobaric
	 * @param isobaricTag
	 * @param combineLabel
	 * @param isoCorFactor
	 */
	public MetaParameterPFind(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[] isobaric,
			IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata(), metaPar.getResult(), metaPar.getMicroDb(),
				metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(), metaPar.getCoreCount(),
				metaPar.getThreadCount(), metaPar.isMetaWorkflow(), metaPar.getMscp());

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = quanMode;
		this.isobaric = isobaric;
		this.isobaricTag = isobaricTag;
		this.combineLabel = combineLabel;
		this.isoCorFactor = isoCorFactor;
	}

	public MetaParameterPFind(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, boolean openSearch, MetaSsdbCreatePar mscp, MetaOpenPar osp,
			MetaPep2TaxaPar ptp) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata(), metaPar.getResult(), metaPar.getMicroDb(),
				metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(), metaPar.getCoreCount(),
				metaPar.getThreadCount(), metaPar.isMetaWorkflow(), mscp);

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = MetaConstants.labelFree;
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};
		this.osp = osp;
		this.ptp = ptp;
	}

	public MetaParameterPFind(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[] isobaric,
			IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor, boolean openSearch, MetaOpenPar osp,
			MetaPep2TaxaPar ptp) {

		super(metaPar.getWorkflowType().name(), metaPar.getMetadata(), metaPar.getResult(), metaPar.getMicroDb(),
				metaPar.isAppendHostDb(), metaPar.getHostDB(), metaPar.getMs2ScanMode(), metaPar.getCoreCount(),
				metaPar.getThreadCount(), metaPar.isMetaWorkflow());

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = quanMode;
		this.isobaric = isobaric;
		this.isobaricTag = isobaricTag;
		this.combineLabel = combineLabel;
		this.isoCorFactor = isoCorFactor;
		this.osp = osp;
		this.ptp = ptp;
	}

	public MetaParameterPFind(String taskType, MetaData meta, String result, String microDb, boolean appendHostDb,
			String hostDB, String ms2ScanMode, int coreCount, int threadCount, boolean isOpenSearch,
			boolean isMetaWorkflow, String[] fixMods, String[] variMods, String enzyme, int missCleavages,
			int digestMode, boolean openSearch, MetaSsdbCreatePar mscp, MetaOpenPar osp, MetaPep2TaxaPar ptp) {

		super(taskType, meta, result, microDb, appendHostDb, hostDB, ms2ScanMode, coreCount, threadCount,
				isMetaWorkflow, mscp);

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = MetaConstants.labelFree;
		this.isobaric = new MaxquantModification[] {};
		this.isobaricTag = null;
		this.combineLabel = false;
		this.isoCorFactor = new double[][] {};
		this.osp = osp;
		this.ptp = ptp;
	}

	public MetaParameterPFind(String taskType, MetaData meta, String result, String microDb, boolean appendHostDb,
			String hostDB, String ms2ScanMode, int coreCount, int threadCount, boolean isOpenSearch,
			boolean isMetaWorkflow, String[] fixMods, String[] variMods, String enzyme, int missCleavages,
			int digestMode, String quanMode, MaxquantModification[] isobaric, IsobaricTag isobaricTag,
			boolean combineLabel, double[][] isoCorFactor, boolean openSearch, MetaSsdbCreatePar mscp, MetaOpenPar osp,
			MetaPep2TaxaPar ptp) {

		super(taskType, meta, result, microDb, appendHostDb, hostDB, ms2ScanMode, coreCount, threadCount,
				isMetaWorkflow, mscp);

		this.fixMods = fixMods;
		this.variMods = variMods;
		this.enzyme = enzyme;
		this.missCleavages = missCleavages;
		this.digestMode = digestMode;
		this.quanMode = quanMode;
		this.isobaric = isobaric;
		this.isobaricTag = isobaricTag;
		this.combineLabel = combineLabel;
		this.isoCorFactor = isoCorFactor;
		this.osp = osp;
		this.ptp = ptp;
	}

	public void setFixMods(String[] fixMods) {
		this.fixMods = fixMods;
	}

	public void setVariMods(String[] variMods) {
		this.variMods = variMods;
	}

	public void setEnzyme(String enzyme) {
		this.enzyme = enzyme;
	}

	public void setMissCleavages(int missCleavages) {
		this.missCleavages = missCleavages;
	}

	public void setDigestMode(int digestMode) {
		this.digestMode = digestMode;
	}

	public boolean isOpenSearch() {
		return osp.isOpenSearch();
	}

	public String[] getFixMods() {
		return fixMods;
	}

	public String[] getVariMods() {
		return variMods;
	}

	public String getEnzyme() {
		return enzyme;
	}

	public int getMissCleavages() {
		return missCleavages;
	}

	public int getDigestMode() {
		return digestMode;
	}

	public String getQuanMode() {
		return quanMode;
	}

	public void setQuanMode(String quanMode) {
		this.quanMode = quanMode;
	}

	public MaxquantModification[] getIsobaric() {
		return isobaric;
	}

	public void setIsobaric(MaxquantModification[] isobaric) {
		this.isobaric = isobaric;
	}

	public IsobaricTag getIsobaricTag() {
		return isobaricTag;
	}

	public void setIsobaricTag(IsobaricTag isobaricTag) {
		this.isobaricTag = isobaricTag;
	}

	public boolean isCombineLabel() {
		return combineLabel;
	}

	public void setCombineLabel(boolean combineLabel) {
		this.combineLabel = combineLabel;
	}

	public double[][] getIsoCorFactor() {
		return isoCorFactor;
	}

	public void setIsoCorFactor(double[][] isoCorFactor) {
		this.isoCorFactor = isoCorFactor;
	}

	public MetaOpenPar getOsp() {
		return osp;
	}

	public void setOsp(MetaOpenPar osp) {
		this.osp = osp;
	}

	public void setPtp(MetaPep2TaxaPar ptp) {
		this.ptp = ptp;
	}

	public MetaPep2TaxaPar getPtp() {
		return ptp;
	}

	public double getOpenPsmFDR() {
		return osp.getOpenPsmFDR();
	}

	public double getOpenProFDR() {
		return osp.getOpenProFDR();
	}

	public MetaOpenPar getMop() {
		return osp;
	}

	public void setMop(MetaOpenPar mop) {
		this.osp = mop;
	}

}
