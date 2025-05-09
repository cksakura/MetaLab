package bmi.med.uOttawa.metalab.dbSearch.sage;

public class SagePSM {

	private String fileName;
	private int scan;
	private String sequence;
	private String modSeq;
	private double expMass;
	private double calMass;
	private double massPPM;
	private double fragPPM;
	private int charge;
	private int miss;
	private int specificity;
	private double qValue;
	private double hyperscore;
	private double disScore;
	private double rt;
	private String[] proteins;
	private boolean isTarget;
	private double peptideQ;
	private double proteinQ;
	private double ms2Intensity;

	public SagePSM(String fileName, int scan, String sequence, String modSeq, double expMass, double calMass,
			double massPPM, double fragPPM, int charge, int miss, int specificity, double rt, double qValue,
			double hyperscore, double disScore, String[] proteins, boolean isTarget, double peptideQ, double proteinQ,
			double ms2Intensity) {
		this.fileName = fileName;
		this.scan = scan;
		this.sequence = sequence;
		this.modSeq = modSeq;
		this.expMass = expMass;
		this.calMass = calMass;
		this.massPPM = massPPM;
		this.fragPPM = fragPPM;
		this.charge = charge;
		this.miss = miss;
		this.specificity = specificity;
		this.rt = rt;
		this.qValue = qValue;
		this.hyperscore = hyperscore;
		this.disScore = disScore;
		this.proteins = proteins;
		this.isTarget = isTarget;
		this.peptideQ = peptideQ;
		this.proteinQ = proteinQ;
		this.ms2Intensity = ms2Intensity;
	}

	public String getProtein() {
		StringBuilder sb = new StringBuilder();
		for (String pro : proteins) {
			sb.append(pro).append(";");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return "";
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getScan() {
		return scan;
	}

	public void setScan(int scan) {
		this.scan = scan;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getModSeq() {
		return modSeq;
	}

	public void setModSeq(String modSeq) {
		this.modSeq = modSeq;
	}

	public double getExpMass() {
		return expMass;
	}

	public void setExpMass(double expMass) {
		this.expMass = expMass;
	}

	public double getCalMass() {
		return calMass;
	}

	public void setCalMass(double calMass) {
		this.calMass = calMass;
	}

	public double getMassPPM() {
		return massPPM;
	}

	public void setMassPPM(double massPPM) {
		this.massPPM = massPPM;
	}

	public double getFragPPM() {
		return fragPPM;
	}

	public void setFragPPM(double fragPPM) {
		this.fragPPM = fragPPM;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public int getMiss() {
		return miss;
	}

	public void setMiss(int miss) {
		this.miss = miss;
	}

	public int getSpecificity() {
		return specificity;
	}

	public void setSpecificity(int specificity) {
		this.specificity = specificity;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

	public double getHyperscore() {
		return hyperscore;
	}

	public void setHyperscore(double hyperscore) {
		this.hyperscore = hyperscore;
	}

	public double getDisScore() {
		return disScore;
	}

	public void setDisScore(double disScore) {
		this.disScore = disScore;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public String[] getProteins() {
		return proteins;
	}

	public void setProteins(String[] proteins) {
		this.proteins = proteins;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public double getPeptideQ() {
		return peptideQ;
	}

	public void setPeptideQ(double peptideQ) {
		this.peptideQ = peptideQ;
	}

	public double getProteinQ() {
		return proteinQ;
	}

	public void setProteinQ(double proteinQ) {
		this.proteinQ = proteinQ;
	}

	public double getMs2Intensity() {
		return ms2Intensity;
	}

	public void setMs2Intensity(double ms2Intensity) {
		this.ms2Intensity = ms2Intensity;
	}

}
