/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantMSMS {

	private String raw;
	private int scan;
	private String sequence;
	private int miss;
	private String modseq;
	private String protein;
	private int charge;
	private double mz;
	private double mass;
	private double massError;
	private double rt;
	private double PEP;
	private double score;
	private double deltaScore;
	private boolean isTarget;
	private int id;
	private int[] proid;
	private int pepid;
	private int modpepid;
	
	public MaxquantMSMS(String raw, int scan, String sequence, int miss, String modseq, String protein, int charge,
			double mz, double mass, double massError, double rt, double PEP, double score, double deltaScore, boolean isTarget, int id,
			int[] proid, int pepid, int modpepid) {
		this.raw = raw;
		this.scan = scan;
		this.sequence = sequence;
		this.miss = miss;
		this.modseq = modseq;
		this.protein = protein;
		this.charge = charge;
		this.mz = mz;
		this.mass = mass;
		this.massError = massError;
		this.rt = rt;
		this.PEP = PEP;
		this.score = score;
		this.deltaScore = deltaScore;
		this.isTarget = isTarget;
		this.id = id;
		this.proid = proid;
		this.pepid = pepid;
		this.modpepid = modpepid;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public String getRaw() {
		return raw;
	}

	public int getScan() {
		return scan;
	}

	public String getSequence() {
		return sequence;
	}

	public int getMiss() {
		return miss;
	}

	public String getModseq() {
		return modseq;
	}

	public String getProtein() {
		return protein;
	}

	public int getCharge() {
		return charge;
	}

	public double getMz() {
		return mz;
	}

	public double getMass() {
		return mass;
	}

	public double getMassError() {
		return massError;
	}

	public double getRt() {
		return rt;
	}

	public double getPEP() {
		return PEP;
	}

	public double getScore() {
		return score;
	}

	public double getDeltaScore() {
		return deltaScore;
	}

	public int getId() {
		return id;
	}

	public int[] getProid() {
		return proid;
	}

	public int getPepid() {
		return pepid;
	}

	public int getModpepid() {
		return modpepid;
	}
	
	
}
