package bmi.med.uOttawa.metalab.dbSearch.diann;

public class DiaNNPrecursor {

	private String runString;
	private String[] proGroups;
	private String modSeqString;
	private String seqString;
	private int charge;
	private int missCleavage;
	private double mass;
	private double qvalue;
	private double PEP;
	private double proGroupQvalue;
	private double preTrans;
	private double cscore;
	private double decoyEvi;
	private double percolatorScore;
	private double percolatorFdr;

	public DiaNNPrecursor(String runString, String[] proGroups, String modSeqString, String seqString, int charge,
			int missCleavage, double mass, double qvalue, double pEP, double proGroupQvalue, double preTrans,
			double cscore) {
		this.runString = runString;
		this.proGroups = proGroups;
		this.modSeqString = modSeqString;
		this.seqString = seqString;
		this.charge = charge;
		this.missCleavage = missCleavage;
		this.mass = mass;
		this.qvalue = qvalue;
		this.PEP = pEP;
		this.proGroupQvalue = proGroupQvalue;
		this.preTrans = preTrans;
		this.cscore = cscore;
	}
	
	public DiaNNPrecursor(String runString, String[] proGroups, String modSeqString, String seqString, int charge,
			int missCleavage, double mass, double qvalue, double pEP, double proGroupQvalue, double preTrans,
			double cscore, double decoyEvi) {
		this.runString = runString;
		this.proGroups = proGroups;
		this.modSeqString = modSeqString;
		this.seqString = seqString;
		this.charge = charge;
		this.missCleavage = missCleavage;
		this.mass = mass;
		this.qvalue = qvalue;
		this.PEP = pEP;
		this.proGroupQvalue = proGroupQvalue;
		this.preTrans = preTrans;
		this.cscore = cscore;
		this.decoyEvi = decoyEvi;
	}

	public String getRunString() {
		return runString;
	}

	public void setProGroups(String[] proGroups) {
		this.proGroups = proGroups;
	}

	public String[] getProGroups() {
		return proGroups;
	}

	public String getModSeqString() {
		return modSeqString;
	}

	public String getSeqString() {
		return seqString;
	}

	public int getCharge() {
		return charge;
	}

	public double getQvalue() {
		return qvalue;
	}

	public double getPEP() {
		return PEP;
	}

	public double getProGroupQvalue() {
		return proGroupQvalue;
	}

	public double getPreTrans() {
		return preTrans;
	}

	public double getCscore() {
		return cscore;
	}

	public int getMissCleavage() {
		return missCleavage;
	}

	public double getMass() {
		return mass;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public double getDecoyEvi() {
		return decoyEvi;
	}
	
	public double getPercolatorScore() {
		return percolatorScore;
	}

	public void setPercolatorScore(double percolatorScore) {
		this.percolatorScore = percolatorScore;
	}

	public double getPercolatorFdr() {
		return percolatorFdr;
	}

	public void setPercolatorFdr(double percolatorFdr) {
		this.percolatorFdr = percolatorFdr;
	}

}
