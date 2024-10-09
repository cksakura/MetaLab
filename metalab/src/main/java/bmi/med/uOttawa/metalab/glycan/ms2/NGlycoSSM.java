/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.glycan.glycoCT.GlycoTree;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.Peak;

/**
 * @author Kai Cheng
 *
 */
public class NGlycoSSM {

	private int scannum;
	private int preCharge;
	private int ms1Scannum;
	private double preMz;
	private double preMr;
	private Peak[] peaks;

	private HashSet<Integer> matchedPeaks;
	private GlycoTree tree;
	private double score;
	private double deltaScore;
	private double neuAcScore;
	private int rank;
	private double rt;
	private int peptideid;
	private int[] glycanid;

	private double pepMass;
	private double pepMassExperiment;
	private double monoGlycoMass;
	private double deltaMz;
	private int isotope;

	private boolean use;
	private boolean share;
	private double leastDeltaMass = 20;
	private int bestMatchPepScannum;
	private ArrayList<Integer> matchPepIDs;
	private HashMap<Integer, Integer> pepIDLabelTypeMap;

	private boolean target;
	private String sequence;
	private Features features;

	private static DecimalFormat df4 = new DecimalFormat(".####");

	public NGlycoSSM(int scannum, int preCharge, double preMz, double pepMassExperiment, Peak[] peaks,
			HashSet<Integer> matchedPeaks, GlycoTree tree, double score, double deltaScore) {

		this.scannum = scannum;
		this.preCharge = preCharge;
		this.preMz = preMz;
		this.preMr = Double.parseDouble(df4.format((preMz - AminoAcidProperty.PROTON_W) * (double) preCharge));

		this.peaks = peaks;
		this.matchedPeaks = matchedPeaks;
		this.tree = tree;
		this.score = score;
		this.deltaScore = deltaScore;

		this.monoGlycoMass = tree.getMonoMass();
		this.pepMass = Double
				.parseDouble(df4.format((preMz - AminoAcidProperty.PROTON_W) * (double) preCharge - monoGlycoMass));
		this.pepMassExperiment = pepMassExperiment;
		this.matchPepIDs = new ArrayList<Integer>();
		this.pepIDLabelTypeMap = new HashMap<Integer, Integer>();
	}

	public NGlycoSSM(int scannum, int preCharge, double preMz, double pepMassExperiment, Peak[] peaks, int rank,
			HashSet<Integer> matchedPeaks, GlycoTree tree, double score) {

		this.scannum = scannum;
		this.preCharge = preCharge;
		this.preMz = preMz;
		this.preMr = Double.parseDouble(df4.format((preMz - AminoAcidProperty.PROTON_W) * (double) preCharge));

		this.peaks = peaks;
		this.matchedPeaks = matchedPeaks;
		this.tree = tree;
		this.score = score;
		this.rank = rank;

		this.monoGlycoMass = tree.getMonoMass();
		this.pepMass = Double
				.parseDouble(df4.format((preMz - AminoAcidProperty.PROTON_W) * (double) preCharge - monoGlycoMass));
		this.pepMassExperiment = pepMassExperiment;
		this.matchPepIDs = new ArrayList<Integer>();
		this.pepIDLabelTypeMap = new HashMap<Integer, Integer>();
	}

	public NGlycoSSM(int scannum, int preCharge, double preMz, double pepMass, double pepMassExperiment, Peak[] peaks,
			int rank, HashSet<Integer> matchedPeaks, GlycoTree tree, double score) {

		this.scannum = scannum;
		this.preCharge = preCharge;
		this.preMz = preMz;
		this.preMr = Double.parseDouble(df4.format((preMz - AminoAcidProperty.PROTON_W) * (double) preCharge));

		this.peaks = peaks;
		this.matchedPeaks = matchedPeaks;
		this.tree = tree;
		this.score = score;
		this.rank = rank;

		this.monoGlycoMass = tree.getMonoMass();
		this.pepMass = pepMass;
		this.pepMassExperiment = pepMassExperiment;
		this.matchPepIDs = new ArrayList<Integer>();
		this.pepIDLabelTypeMap = new HashMap<Integer, Integer>();
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public String getName() {
		return tree.getIupacName();
	}
	
	public String getComposition() {
		return tree.getCompositionString();
	}

	public GlycoTree getGlycoTree() {
		return tree;
	}

	public double getPepMass() {
		return pepMass;
	}

	public double getGlycoMass() {
		return monoGlycoMass;
	}

	public HashSet<Integer> getMatchedPeaks() {
		return matchedPeaks;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	/**
	 * @return the neuAcScore
	 */
	public double getNeuAcScore() {
		return neuAcScore;
	}

	/**
	 * @param neuAcScore
	 *            the neuAcScore to set
	 */
	public void setNeuAcScore(double neuAcScore) {
		this.neuAcScore = neuAcScore;
	}

	public void setRT(double rt) {
		this.rt = rt;
	}

	public double getRT() {
		return rt;
	}

	public Peak[] getPeaks() {
		return peaks;
	}

	public int getScanNum() {
		return scannum;
	}

	public int getPreCharge() {
		return preCharge;
	}

	public double getPreMz() {
		return preMz;
	}

	public double getPreMr() {
		return preMr;
	}

	/**
	 * @return the pepMassExperiment
	 */
	public double getPepMassExperiment() {
		return pepMassExperiment;
	}

	/**
	 * @param pepMassExperiment
	 *            the pepMassExperiment to set
	 */
	public void setPepMassExperiment(double pepMassExperiment) {
		this.pepMassExperiment = pepMassExperiment;
	}

	public int getMS1Scannum() {
		return ms1Scannum;
	}

	public void setMS1Scannum(int ms1Scannum) {
		this.ms1Scannum = ms1Scannum;
	}

	public String getPeakOneLine() {

		StringBuilder sb = new StringBuilder();

		sb.append(df4.format(preMz)).append(' ').append(preCharge).append('$');
		for (int i = 0; i < peaks.length; i++) {
			sb.append(peaks[i].getMz()).append(' ').append(peaks[i].getIntensity()).append(',');
		}
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public void setUse() {
		this.use = true;
	}

	public boolean isUse() {
		return use;
	}

	public void setShare() {
		this.share = true;
	}

	public boolean isShare() {
		return share;
	}

	/**
	 * @return the target
	 */
	public boolean isTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(boolean target) {
		this.target = target;
	}

	public boolean setDeltaMass(double deltaMass) {
		if (deltaMass < this.leastDeltaMass) {
			this.leastDeltaMass = deltaMass;
			return true;
		} else {
			return false;
		}
	}

	public void setBestPepScannum(int scannum) {
		this.bestMatchPepScannum = scannum;
	}

	public int getBestPepScannum() {
		return bestMatchPepScannum;
	}

	public ArrayList<Integer> getMatchPepIDs() {
		return matchPepIDs;
	}

	public void addMatchPepID(int pepid) {
		this.matchPepIDs.add(pepid);
	}

	public void addMatchPepID(int pepid, int labelType) {
		this.matchPepIDs.add(pepid);
		this.pepIDLabelTypeMap.put(pepid, labelType);
	}

	public int getPepLabelType(int pepid) {
		if (pepIDLabelTypeMap.containsKey(pepid))
			return this.pepIDLabelTypeMap.get(pepid);
		else
			return -1;
	}

	/**
	 * @return the sequence
	 */
	public String getSequence() {
		return sequence;
	}

	/**
	 * @param sequence
	 *            the sequence to set
	 */
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the peptideid
	 */
	public int getPeptideid() {
		return peptideid;
	}

	/**
	 * @param peptideid
	 *            the peptideid to set
	 */
	public void setPeptideid(int peptideid) {
		this.peptideid = peptideid;
	}

	/**
	 * @return the glycanid
	 */
	public int[] getGlycanid() {
		return glycanid;
	}

	/**
	 * @param glycanid
	 *            the glycanid to set
	 */
	public void setGlycanid(int[] glycanid) {
		this.glycanid = glycanid;
	}

	/**
	 * @return the deltaMz
	 */
	public double getDeltaMz() {
		return deltaMz;
	}

	/**
	 * @param deltaMz
	 *            the deltaMz to set
	 */
	public void setDeltaMz(double deltaMz) {
		this.deltaMz = deltaMz;
	}

	/**
	 * @return the features
	 */
	public Features getFeatures() {
		return features;
	}

	/**
	 * @param features
	 *            the features to set
	 */
	public void setFeatures(Features features) {
		this.features = features;
	}

	/**
	 * @return the isotope
	 */
	public int getIsotope() {
		return isotope;
	}

	/**
	 * @param isotope
	 *            the isotope to set
	 */
	public void setIsotope(int isotope) {
		this.isotope = isotope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(NGlycoSSM ssm) {
		// TODO Auto-generated method stub
		if (this.getScore() > ssm.getScore()) {

			return 1;

		} else if (this.getScore() < ssm.getScore()) {

			return -1;
		}

		return 0;
	}

}
