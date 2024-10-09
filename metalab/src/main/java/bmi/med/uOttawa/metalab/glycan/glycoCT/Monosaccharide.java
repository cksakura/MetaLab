/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.util.regex.Pattern;

/**
 * @author Kai Cheng
 *
 */
public class Monosaccharide {

	private String glycoCT_Name;
	private Pattern pattern;
	private String carbBank_Name;
	private String IUPAC_Name;
	private int[] composition;
	private double mono_mass;
	private double avg_mass;

	public Monosaccharide() {

	}

	public Monosaccharide(String glycoCT_Name, String carbBank_Name, String IUPAC_Name, String composition,
			double mono_mass, double avg_mass) {

		this.glycoCT_Name = glycoCT_Name;
		this.carbBank_Name = carbBank_Name;
		this.IUPAC_Name = IUPAC_Name;
		this.mono_mass = mono_mass;
		this.avg_mass = avg_mass;
	}

	/**
	 * @return the glycoCT_Name
	 */
	public String getGlycoCT_Name() {
		return glycoCT_Name;
	}

	/**
	 * @param glycoCT_Name the glycoCT_Name to set
	 */
	public void setGlycoCT_Name(String glycoCT_Name) {
		this.glycoCT_Name = glycoCT_Name;
	}

	/**
	 * @return the pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the carbBank_Name
	 */
	public String getCarbBank_Name() {
		return carbBank_Name;
	}

	/**
	 * @param carbBank_Name the carbBank_Name to set
	 */
	public void setCarbBank_Name(String carbBank_Name) {
		this.carbBank_Name = carbBank_Name;
	}

	/**
	 * @return the iUPAC_Name
	 */
	public String getIUPAC_Name() {
		return IUPAC_Name;
	}

	/**
	 * @param iUPAC_Name the iUPAC_Name to set
	 */
	public void setIUPAC_Name(String iUPAC_Name) {
		IUPAC_Name = iUPAC_Name;
	}

	/**
	 * @return the composition
	 */
	public int[] getComposition() {
		return composition;
	}

	/**
	 * @param composition the composition to set
	 */
	public void setComposition(int[] composition) {
		this.composition = composition;
	}

	/**
	 * @return the mono_mass
	 */
	public double getMono_mass() {
		return mono_mass;
	}

	/**
	 * @param mono_mass the mono_mass to set
	 */
	public void setMono_mass(double mono_mass) {
		this.mono_mass = mono_mass;
	}

	/**
	 * @return the avg_mass
	 */
	public double getAvg_mass() {
		return avg_mass;
	}

	/**
	 * @param avg_mass the avg_mass to set
	 */
	public void setAvg_mass(double avg_mass) {
		this.avg_mass = avg_mass;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("Name:").append(this.glycoCT_Name).append("\n");
		sb.append("CarbBank:").append(this.carbBank_Name).append("\n");
		sb.append("IUPAC:").append(this.IUPAC_Name).append("\n");
		sb.append("Composition:").append(this.composition).append("\n");
		sb.append("Mono:").append(this.mono_mass).append("\n");
		sb.append("Avg:").append(this.avg_mass).append("\n");

		return sb.toString();
	}

}
