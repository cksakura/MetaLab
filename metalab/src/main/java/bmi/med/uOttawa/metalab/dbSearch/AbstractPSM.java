package bmi.med.uOttawa.metalab.dbSearch;

public abstract class AbstractPSM {

	private String fileName;
	private int scan;
	private int charge;
	private double precorsorMr;
	private double rt;
	private String sequence;
	private double pepmass;
	private double massdiff;
	private int num_matched_ions;
	private int hit_rank;
	private String protein;
	private boolean isTarget;
	
	public AbstractPSM(String fileName, int scan, int charge, double precorsorMr, double rt, String sequence,
			double pepmass, double massdiff, int num_matched_ions, int hit_rank, String protein, boolean isTarget) {
		this.fileName = fileName;
		this.scan = scan;
		this.charge = charge;
		this.precorsorMr = precorsorMr;
		this.rt = rt;
		this.sequence = sequence;
		this.pepmass = pepmass;
		this.massdiff = massdiff;
		this.num_matched_ions = num_matched_ions;
		this.hit_rank = hit_rank;
		this.protein = protein;
		this.isTarget = isTarget;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
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

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public double getPrecorsorMr() {
		return precorsorMr;
	}

	public void setPrecorsorMr(double precorsorMr) {
		this.precorsorMr = precorsorMr;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public double getPepmass() {
		return pepmass;
	}

	public void setPepmass(double pepmass) {
		this.pepmass = pepmass;
	}

	public double getMassdiff() {
		return massdiff;
	}

	public void setMassdiff(double massdiff) {
		this.massdiff = massdiff;
	}

	public int getNum_matched_ions() {
		return num_matched_ions;
	}

	public void setNum_matched_ions(int num_matched_ions) {
		this.num_matched_ions = num_matched_ions;
	}

	public int getHit_rank() {
		return hit_rank;
	}

	public void setHit_rank(int hit_rank) {
		this.hit_rank = hit_rank;
	}

	public String getProtein() {
		return protein;
	}

	public void setProtein(String protein) {
		this.protein = protein;
	}

	

}
