package bmi.med.uOttawa.metalab.glycan.deNovo;

import bmi.med.uOttawa.metalab.quant.Features;

public class DenovoGlycoPeptide {

	private double preMz;
	private int preCharge;
	private int[] scans;
	private int[] composition;
	private String comString;
	private double peptideMass;
	private double glycanMass;
	private double score;
	private Features features;

	public DenovoGlycoPeptide(double preMz, int preCharge, int[] scans, int[] composition, String comString,
			double peptideMass, double glycanMass, double score, Features features) {
		this.preMz = preMz;
		this.preCharge = preCharge;
		this.scans = scans;
		this.composition = composition;
		this.comString = comString;
		this.peptideMass = peptideMass;
		this.glycanMass = glycanMass;
		this.score = score;
		this.features = features;
	}

	public double getPreMz() {
		return preMz;
	}

	public int getPreCharge() {
		return preCharge;
	}

	public int[] getScans() {
		return scans;
	}

	public int[] getComposition() {
		return composition;
	}

	public String getComString() {
		return comString;
	}

	public double getPeptideMass() {
		return peptideMass;
	}

	public double getGlycanMass() {
		return glycanMass;
	}

	public double getScore() {
		return score;
	}

	public Features getFeatures() {
		return features;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
