/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

import java.util.Arrays;

import bmi.med.uOttawa.metalab.spectra.Peak;

/**
 * @author Kai Cheng
 *
 */
public class Feature implements Comparable<Feature> {

	/**
	 * nbt.1511-S1, p9
	 */
	public final static double dm = 1.00286864;
	public static int isotopeCount = 4;
	public static int isotopeLength = isotopeCount * 2 + 1;

	private int scanNum;
	private int charge;
	private double rt;

	private double monoMass;
	private double totalIntensity;
	private double[] masses;
	private double[] intens;
	private boolean miss;
	private int missCount;

	public Feature(int scanNum, int charge, double monoMass) {
		this.scanNum = scanNum;
		this.charge = charge;
		this.monoMass = monoMass;

		this.masses = new double[isotopeLength];
		this.intens = new double[isotopeLength];
		for (int i = 0; i < isotopeLength; i++) {
			this.masses[i] = this.monoMass + dm / (double) charge * (i - isotopeCount);
		}
	}

	public Feature(int scanNum, int charge, double monoMass, double rt) {
		this.scanNum = scanNum;
		this.charge = charge;
		this.monoMass = monoMass;
		this.rt = rt;

		this.masses = new double[isotopeLength];
		this.intens = new double[isotopeLength];
		for (int i = 0; i < isotopeLength; i++) {
			this.masses[i] = this.monoMass + dm / (double) charge * (i - isotopeCount);
		}
	}

	public void match(Peak[] peaks, double tolerance) {

		double max = this.masses[isotopeLength - 1];
		Peak findpeak = new Peak(masses[0] - tolerance, 0d);

		int index = Arrays.binarySearch(peaks, findpeak);
		if (index < 0) {
			index = -index - 1;
		} else if (index >= peaks.length) {
			return;
		}

		for (int i = index; i < peaks.length; i++) {
			double mass = peaks[i].getMz();
			double inten = peaks[i].getIntensity();
			for (int j = 0; j < masses.length; j++) {
				if (Math.abs(masses[j] - mass) < tolerance && inten > intens[j]) {
					masses[j] = mass;
					intens[j] = inten;
				}
			}
			if (mass - max > tolerance)
				break;
		}
		for (int i = isotopeCount; i < isotopeLength; i++) {
			this.totalIntensity += this.intens[i];
			if (i < isotopeCount + 3 && intens[i] == 0) {
				this.miss = true;
			}
		}
	}

	/*
	 * public void match(Peak[] peaks, double tolerance, ArrayList<Double>
	 * taillist, int tailNum, Double tailLimit) {
	 * 
	 * double max = this.masses[isotopeCount - 1]; Peak findpeak = new
	 * Peak(masses[0] - tolerance, 0d); //
	 * System.out.println(findpeak+"\t"+peaks.length); int index =
	 * Arrays.binarySearch(peaks, findpeak); if (index < 0) { index = -index -
	 * 1; } else if (index >= peaks.length) { return; }
	 * 
	 * for (int i = index; i < peaks.length; i++) { double mass =
	 * peaks[i].getMz(); double inten = peaks[i].getIntensity(); if (inten < 0)
	 * inten = -inten; for (int j = 0; j < masses.length; j++) { if
	 * (Math.abs(masses[j] - mass) < tolerance && inten > intens[j]) { intens[j]
	 * = inten; } } if (mass - max > tolerance) break; }
	 * 
	 * for (int i = 0; i < isotopeCount; i++) { this.totalIntensity +=
	 * this.intens[i]; if (i < isotopeCount - 1) { if (this.intens[i] == 0) {
	 * this.miss = true; } } }
	 * 
	 * if (miss) return;
	 * 
	 * int totalTail = 0;
	 * 
	 * if (taillist.size() == 6) { double oldInten = MathTool.getAve(taillist);
	 * taillist.remove(0); taillist.add(this.totalIntensity); double newInten =
	 * MathTool.getAve(taillist); //
	 * System.out.print(MathTool.getRSD(taillist[i])+"\t"); if
	 * (MathTool.getRSD(taillist) < tailLimit && newInten * 0.9 < oldInten) {
	 * tailNum++; } else { if (tailNum > 0 && tailNum < 10) tailNum--; } if
	 * (tailNum >= 15) { miss = true; } else if (tailNum >= 12 && tailNum < 15)
	 * { totalTail++; } } else { taillist.add(this.totalIntensity); } }
	 */

	public String getKey() {
		return scanNum + "\t" + charge;
	}

	public double[] getMasses() {
		return masses;
	}

	public double[] getIntens() {
		return intens;
	}

	public boolean isMiss() {
		return miss;
	}

	public int getMissCount() {
		return missCount;
	}

	public int getScanNum() {
		return scanNum;
	}

	public void setRT(double rt) {
		this.rt = rt;
	}

	public double getRT() {
		return rt;
	}

	public double getTotalIntensity() {
		return totalIntensity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Feature f1) {
		// TODO Auto-generated method stub
		double d = this.masses[0];
		double d1 = f1.masses[0];

		if (d > d1)
			return 1;
		else if (d < d1)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		return sb.toString();
	}

}
