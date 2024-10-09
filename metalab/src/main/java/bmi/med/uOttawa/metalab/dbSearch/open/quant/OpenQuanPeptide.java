/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.quant;

/**
 * @author Kai Cheng
 *
 */
public class OpenQuanPeptide {

	private String sequence;
	private String protein;
	private int modid;
	private double[] intensity;

	public OpenQuanPeptide(String sequence, String protein, int modid, double[] intensity) {
		this.sequence = sequence;
		this.protein = protein;
		this.modid = modid;
		this.intensity = intensity;
	}

	public String getSequence() {
		return sequence;
	}

	public String getProtein() {
		return protein;
	}

	public int getModId() {
		return modid;
	}

	public double[] getIntensity() {
		return intensity;
	}

	public boolean isTarget() {
		return !protein.startsWith("REV_");
	}

}
