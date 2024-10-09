/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.philosopher;

/**
 * @author Kai Cheng
 *
 */
public class PhilosopherPsm {

	private String fileName;
	private int scan;
	private String sequence;
	private int charge;
	private double rt;
	private double calMz;
	private double obsMz;
	private double oriDeltaMass;
	private double adjDeltaMass;
	private double expMass;
	private double pepMass;
	private double eValue;
	private double hyperScore;
	private double nextScore;
	private double probability;
	private String variMods;
	private String openMods;
	private String protein;
	
	public PhilosopherPsm(String fileName, int scan, String sequence, int charge, double rt, double calMz, double obsMz,
			double oriDeltaMass, double adjDeltaMass, double expMass, double pepMass, double eValue, double hyperScore,
			double nextScore, double probability, String variMods, String openMods, String protein) {
		this.fileName = fileName;
		this.scan = scan;
		this.sequence = sequence;
		this.charge = charge;
		this.rt = rt;
		this.calMz = calMz;
		this.obsMz = obsMz;
		this.oriDeltaMass = oriDeltaMass;
		this.adjDeltaMass = adjDeltaMass;
		this.expMass = expMass;
		this.pepMass = pepMass;
		this.eValue = eValue;
		this.hyperScore = hyperScore;
		this.nextScore = nextScore;
		this.probability = probability;
		this.variMods = variMods;
		this.openMods = openMods;
		this.protein = protein;
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

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public double getCalMz() {
		return calMz;
	}

	public void setCalMz(double calMz) {
		this.calMz = calMz;
	}

	public double getObsMz() {
		return obsMz;
	}

	public void setObsMz(double obsMz) {
		this.obsMz = obsMz;
	}

	public double getOriDeltaMass() {
		return oriDeltaMass;
	}

	public void setOriDeltaMass(double oriDeltaMass) {
		this.oriDeltaMass = oriDeltaMass;
	}

	public double getAdjDeltaMass() {
		return adjDeltaMass;
	}

	public void setAdjDeltaMass(double adjDeltaMass) {
		this.adjDeltaMass = adjDeltaMass;
	}

	public double getExpMass() {
		return expMass;
	}

	public void setExpMass(double expMass) {
		this.expMass = expMass;
	}

	public double getPepMass() {
		return pepMass;
	}

	public void setPepMass(double pepMass) {
		this.pepMass = pepMass;
	}

	public double geteValue() {
		return eValue;
	}

	public void seteValue(double eValue) {
		this.eValue = eValue;
	}

	public double getHyperScore() {
		return hyperScore;
	}

	public void setHyperScore(double hyperScore) {
		this.hyperScore = hyperScore;
	}

	public double getNextScore() {
		return nextScore;
	}

	public void setNextScore(double nextScore) {
		this.nextScore = nextScore;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public String getVariMods() {
		return variMods;
	}

	public void setVariMods(String variMods) {
		this.variMods = variMods;
	}

	public String getOpenMods() {
		return openMods;
	}

	public void setOpenMods(String openMods) {
		this.openMods = openMods;
	}

	public String getProtein() {
		return protein;
	}

	public void setProtein(String protein) {
		this.protein = protein;
	}
	
	
}
