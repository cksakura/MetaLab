/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pro;

/**
 * @author Kai Cheng
 *
 */
public class MetaProtein {

	protected int groupId;
	protected int proteinId;
	protected String name;
	protected int pepCount;
	protected int razorUniPepCount;
	protected int uniPepCount;
	protected double evalue;

	protected double[] intensities;
	protected int[] ms2Counts;

	protected int psmCount;
	protected double score;

	public MetaProtein(String name, double[] intensities) {
		this.groupId = -1;
		this.name = name;
		this.intensities = intensities;
	}

	/**
	 * MSFragger/pFind
	 * 
	 * @param groupId
	 * @param proteinId
	 * @param name
	 * @param pepCount
	 * @param psmCount
	 * @param score
	 * @param intensities
	 */
	public MetaProtein(int groupId, int proteinId, String name, int pepCount, int psmCount, double score,
			double[] intensities) {
		this.groupId = groupId;
		this.proteinId = proteinId;
		this.name = name;
		this.pepCount = pepCount;
		this.psmCount = psmCount;
		this.score = score;
		this.intensities = intensities;
	}

	public MetaProtein(String name, int pepCount, int psmCount, double score, double[] intensities) {
		this.name = name;
		this.pepCount = pepCount;
		this.psmCount = psmCount;
		this.score = score;
		this.intensities = intensities;
	}

	public MetaProtein(int groupId, int proteinId, String name, int pepCount, int razorUniPepCount, int uniPepCount,
			double evalue) {
		this.groupId = groupId;
		this.proteinId = proteinId;
		this.name = name;
		this.pepCount = pepCount;
		this.razorUniPepCount = razorUniPepCount;
		this.uniPepCount = uniPepCount;
		this.evalue = evalue;
	}

	/**
	 * MaxQuant
	 * 
	 * @param groupId
	 * @param proteinId
	 * @param name
	 * @param pepCount
	 * @param razorUniPepCount
	 * @param uniPepCount
	 * @param evalue
	 * @param ms2Counts
	 * @param intensities
	 */
	public MetaProtein(int groupId, int proteinId, String name, int pepCount, int razorUniPepCount, int uniPepCount,
			double evalue, int[] ms2Counts, double[] intensities) {
		this.groupId = groupId;
		this.proteinId = proteinId;
		this.name = name;
		this.pepCount = pepCount;
		this.razorUniPepCount = razorUniPepCount;
		this.uniPepCount = uniPepCount;
		this.evalue = evalue;
		this.ms2Counts = ms2Counts;
		this.intensities = intensities;
	}

	public MetaProtein(int groupId, int proteinId, String name, int pepCount, int uniPepCount, double evalue,
			double[] intensities, int[] ms2Counts) {

		this.groupId = groupId;
		this.proteinId = proteinId;
		this.name = name;
		this.pepCount = pepCount;
		this.uniPepCount = uniPepCount;
		this.evalue = evalue;
		this.intensities = intensities;
		this.ms2Counts = ms2Counts;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getProteinId() {
		return proteinId;
	}

	public String getName() {
		return name;
	}

	public int getPepCount() {
		return pepCount;
	}

	public int getRazorUniPepCount() {
		return razorUniPepCount;
	}

	public int getUniPepCount() {
		return uniPepCount;
	}

	public double getEvalue() {
		return evalue;
	}

	public double[] getIntensities() {
		return intensities;
	}

	public int[] getMs2Counts() {
		return ms2Counts;
	}

	public void setPepCount(int pepCount) {
		this.pepCount = pepCount;
	}

	public void setUniPepCount(int uniPepCount) {
		this.uniPepCount = uniPepCount;
	}

	public int getPsmCount() {
		return psmCount;
	}

	public void setPsmCount(int psmCount) {
		this.psmCount = psmCount;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Object[] getTableObjects() {

		Object[] objs = new Object[5 + intensities.length];
		objs[0] = name;
		objs[1] = pepCount;
		objs[2] = razorUniPepCount;
		objs[3] = uniPepCount;
		objs[4] = evalue;
		for (int i = 0; i < intensities.length; i++) {
			objs[i + 5] = intensities[i];
		}

		return objs;
	}
	
	public Object[] getFragpipeTableObjects() {

		Object[] objs = new Object[7 + intensities.length];
		objs[0] = groupId;
		objs[1] = proteinId;
		objs[2] = name;
		objs[3] = pepCount;
		objs[4] = psmCount;
		objs[5] = evalue;
		double totalIntensity = 0;
		for (int i = 0; i < intensities.length; i++) {
			totalIntensity += intensities[i];
			objs[i + 7] = intensities[i];
		}
		objs[6] = totalIntensity;
		return objs;
	}
}
