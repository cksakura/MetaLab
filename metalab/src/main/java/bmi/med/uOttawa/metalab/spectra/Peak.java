/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * @author Kai Cheng
 *
 */
public class Peak implements Comparable<Peak>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7430383201045985800L;
	private double mz;
	private double intensity;
	
	private double rankScore;
	private double localRankScore;
	
	private static DecimalFormat df4 = new DecimalFormat(".####");

	public Peak() {

	}

	public Peak(double mz, double intensity) {
		this.mz = mz;
		this.intensity = intensity;
	}

	public double getMz() {
		return mz;
	}

	public void setMz(double mz) {
		this.mz = mz;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}
	
	public double getRankScore() {
		return rankScore;
	}

	public void setRankScore(double rankScore) {
		this.rankScore = rankScore;
	}

	public double getLocalRankScore() {
		return localRankScore;
	}

	public void setLocalRankScore(double localRankScore) {
		this.localRankScore = localRankScore;
	}

	public String toString() {
		return df4.format(mz) + "_" + df4.format(intensity) + "_" + df4.format(rankScore) + "_"
				+ df4.format(localRankScore);
	}
	
	public static Peak parse(String line) {

		String[] cs = line.split("_");
		double mz = Double.parseDouble(cs[0]);
		double intensity = Double.parseDouble(cs[1]);
		double rankScore = Double.parseDouble(cs[2]);
		double localRankScore = Double.parseDouble(cs[3]);
		Peak peak = new Peak(mz, intensity);
		peak.setRankScore(rankScore);
		peak.setLocalRankScore(localRankScore);

		return peak;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Peak))
			return false;
		Peak peak = (Peak) obj;
		if (this.mz != peak.mz)
			return false;
		if (this.intensity != peak.intensity)
			return false;
		return true;
	}
	
	public int hashCode() {
		String s = mz + "_" + intensity;
		return s.hashCode();
	}

	public int compareTo(Peak arg0) {
		// TODO Auto-generated method stub
		if (this.mz < arg0.mz)
			return -1;
		else if (this.mz > arg0.mz)
			return 1;
		return 0;
	}
	
	public static class IntensityComparator implements Comparator <Peak> {

		public int compare(Peak peak0, Peak peak1) {
			// TODO Auto-generated method stub

			double i0 = peak0.getIntensity();
			double i1 = peak1.getIntensity();
			if (i0 < i1)
				return -1;
			if (i0 > i1)
				return 1;
			return 0;
		}
	}

	public static class RevIntensityComparator implements Comparator <Peak> {

		public int compare(Peak peak0, Peak peak1) {
			// TODO Auto-generated method stub

			double i0 = peak0.getIntensity();
			double i1 = peak1.getIntensity();
			if (i0 < i1)
				return 1;
			if (i0 > i1)
				return -1;
			return 0;
		}
		
	}

}
