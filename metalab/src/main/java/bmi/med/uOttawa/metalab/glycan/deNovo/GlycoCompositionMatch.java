package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.text.DecimalFormat;
import java.util.Comparator;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.spectra.Peak;

/**
 * Match between a glycan/glycopeptide and a spectrum, like a PSM
 * @author Kai Cheng <ckkazuma@gmail.com>
 *
 */
public class GlycoCompositionMatch implements Comparable<GlycoCompositionMatch> {

	protected DecimalFormat df2 = new DecimalFormat("#.##");
	protected DecimalFormat df4 = new DecimalFormat("#.####");

	protected int scannum;
	protected double preMz;
	protected int preCharge;
	// HexNAc()Hex()NeuAc()Fuc()
	protected int[] composition;
	protected String comString;
	protected double glycanMass;
	protected double deltaMass;
	protected double score;
	protected int rank;
	protected Peak[] peaks;
	protected double classScore;

	public GlycoCompositionMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass) {
		this.scannum = scannum;
		this.preMz = preMz;
		this.preCharge = preCharge;
		this.composition = composition;
		this.comString = comString;
		this.glycanMass = mass;
		this.deltaMass = deltaMass;
	}

	public GlycoCompositionMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass, double score) {
		this.scannum = scannum;
		this.preMz = preMz;
		this.preCharge = preCharge;
		this.composition = composition;
		this.comString = comString;
		this.glycanMass = mass;
		this.deltaMass = deltaMass;
		this.score = score;
	}

	/**
	 * 
	 * @param scannum
	 * @param preMz
	 * @param preCharge
	 * @param composition
	 * @param comString
	 * @param mass
	 * @param deltaMass
	 * @param score
	 * @param peaks matched peaks
	 */
	public GlycoCompositionMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass, double score, Peak[] peaks) {
		this.scannum = scannum;
		this.preMz = preMz;
		this.preCharge = preCharge;
		this.composition = composition;
		this.comString = comString;
		this.glycanMass = mass;
		this.deltaMass = deltaMass;
		this.score = score;
		this.peaks = peaks;
	}

	public int getScannum() {
		return scannum;
	}

	public double getPreMz() {
		return preMz;
	}

	public int[] getComposition() {
		return composition;
	}

	public String getComString() {
		return comString;
	}

	public double getGlycanMass() {
		return glycanMass;
	}

	public double getDeltaMass() {
		return deltaMass;
	}

	public double getPeptideMass() {
		double pepMass = (this.preMz - AminoAcidProperty.PROTON_W) * this.preCharge - this.glycanMass;
		return pepMass;
	}
	
	public double getScore() {
		return score;
	}

	public double getClassScore() {
		return classScore;
	}

	public void setClassScore(double classScore) {
		this.classScore = classScore;
	}

	public Peak[] getPeaks() {
		return peaks;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getPreCharge() {
		return preCharge;
	}

	public void setPreCharge(int preCharge) {
		this.preCharge = preCharge;
	}

	@Override
	public int compareTo(GlycoCompositionMatch o) {
		// TODO Auto-generated method stub
		if (this.glycanMass < o.glycanMass)
			return -1;
		else if (this.glycanMass > o.glycanMass)
			return 1;
		return 0;
	}

	public static GlycoCompositionMatch parse(String line) {
		String[] cs = line.split(";");
		int scannum = Integer.parseInt(cs[0]);
		double premz = Double.parseDouble(cs[1]);
		int precharge = Integer.parseInt(cs[2]);
		String comstring = cs[3];
		String[] coms = cs[4].split(",");
		int[] composition = new int[coms.length];
		for (int i = 0; i < coms.length; i++) {
			composition[i] = Integer.parseInt(coms[i]);
		}
		double mass = Double.parseDouble(cs[5]);
		double deltaMass = Double.parseDouble(cs[6]);
		double score = Double.parseDouble(cs[7]);
		String[] mps = cs[8].split(",");
		Peak[] matchedPeaks = new Peak[mps.length];

		for (int i = 0; i < mps.length; i++) {
			matchedPeaks[i] = Peak.parse(mps[i]);
		}

		GlycoCompositionMatch glycoCompositon = new GlycoCompositionMatch(scannum, premz, precharge, composition,
				comstring, mass, deltaMass, score, matchedPeaks);

		return glycoCompositon;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(scannum).append(";");
		sb.append(preMz).append(";");
		sb.append(preCharge).append(";");
		sb.append(comString).append(";");
		for (int i : composition) {
			sb.append(i).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");
		sb.append(glycanMass).append(";");
		sb.append(df4.format(deltaMass)).append(";");
		sb.append(df2.format(score)).append(";");
		for (Peak peak : peaks) {
			sb.append(peak).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public static class DeltaMassComparator implements Comparator<GlycoCompositionMatch> {

		@Override
		public int compare(GlycoCompositionMatch arg0, GlycoCompositionMatch arg1) {
			// TODO Auto-generated method stub
			double delta0 = Math.abs(arg0.deltaMass);
			double delta1 = Math.abs(arg1.deltaMass);
			if (delta0 < delta1)
				return -1;
			if (delta0 > delta1)
				return 1;
			return 0;
		}

	}

	public static class ScoreComparator implements Comparator<GlycoCompositionMatch> {

		@Override
		public int compare(GlycoCompositionMatch arg0, GlycoCompositionMatch arg1) {
			// TODO Auto-generated method stub			
			
			if (arg0.score < arg1.score)
				return 1;
			if (arg0.score > arg1.score)
				return -1;
			return 0;
		}

	}
}
