/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * @author Kai Cheng
 *
 */
public class OpenPSM {

	private String fileName;
	private int scan;
	private int charge;
	private double precursorMr;
	private double rt;

	private String sequence;
	private double pepMass;
	private double massDiff;
	private int miss;
	private int num_tol_term;
	private int tot_num_ions;
	private int num_matched_ions;

	private String protein;
	private double hyperscore;
	private double nextscore;
	private double expect;

	private double openModmass;
	private double openModMassDiff;
	private PeptideModification[] ptms;

	// used for machine learning
	private double bintensity;
	private double yintensity;
	private double brankscore;
	private double yrankscore;
	private int bcount;
	private int ycount;
	private double fragDeltaMass;
	private double bmodIntensity;
	private double ymodIntensity;
	private double bmodrankscore;
	private double ymodrankscore;
	private double fragModDeltaMass;
	private double neutralLossMass;
	private double nlPeakRankScore;

	private int sameModCount;
	private int samePepCount;
	private int sameProCount;
	
	private int openModId;
	private int[] possibleLoc;
	private boolean isTarget;
	private double classScore;
	private double qvalue;

	public OpenPSM(String fileName, int scan, int charge, double precursorMr, double rt, String sequence,
			double pepMass, double massDiff, int miss, int num_tol_term, int tot_num_ions, int num_matched_ions,
			String protein, double hyperscore, double nextscore, double expect, double openModmass,
			double openModMassDiff, double bintensity, double yintensity, double brankscore, double yrankscore,
			int bcount, int ycount, double fragDeltaMass, double bmodIntensity, double ymodIntensity,
			double bmodrankscore, double ymodrankscore, double fragModDeltaMass, int openModId, int[] possibleLoc,
			double neutralLossMass, double nlPeakRankScore, boolean isTarget, double classScore) {

		this.fileName = fileName;
		this.scan = scan;
		this.charge = charge;
		this.precursorMr = precursorMr;
		this.rt = rt;
		this.sequence = sequence;
		this.pepMass = pepMass;
		this.massDiff = massDiff;
		this.miss = miss;
		this.num_tol_term = num_tol_term;
		this.tot_num_ions = tot_num_ions;
		this.num_matched_ions = num_matched_ions;
		this.protein = protein;
		this.hyperscore = hyperscore;
		this.nextscore = nextscore;
		this.expect = expect;
		this.openModmass = openModmass;
		this.openModMassDiff = openModMassDiff;
		this.bintensity = bintensity;
		this.yintensity = yintensity;
		this.brankscore = brankscore;
		this.yrankscore = yrankscore;
		this.bcount = bcount;
		this.ycount = ycount;
		this.fragDeltaMass = fragDeltaMass;
		this.bmodIntensity = bmodIntensity;
		this.ymodIntensity = ymodIntensity;
		this.bmodrankscore = bmodrankscore;
		this.ymodrankscore = ymodrankscore;
		this.fragModDeltaMass = fragModDeltaMass;
		this.openModId = openModId;
		this.possibleLoc = possibleLoc;
		this.neutralLossMass = neutralLossMass;
		this.nlPeakRankScore = nlPeakRankScore;
		this.isTarget = isTarget;
		this.classScore = classScore;
	}

	public OpenPSM(String fileName, int scan, int charge, double precursorMr, double rt, String sequence,
			double pepMass, double massDiff, int miss, String protein, double hyperscore, double nextscore,
			double expect, int openModId, int[] possibleLoc, double classScore) {

		this.fileName = fileName;
		this.scan = scan;
		this.charge = charge;
		this.precursorMr = precursorMr;
		this.rt = rt;
		this.sequence = sequence;
		this.pepMass = pepMass;
		this.massDiff = massDiff;
		this.miss = miss;
		this.protein = protein;
		this.hyperscore = hyperscore;
		this.nextscore = nextscore;
		this.expect = expect;
		this.openModId = openModId;
		this.possibleLoc = possibleLoc;
		this.classScore = classScore;
	}
	
	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public String getFileName() {
		return fileName;
	}

	public int getScan() {
		return scan;
	}

	public int getCharge() {
		return charge;
	}

	public double getPrecursorMr() {
		return precursorMr;
	}

	public void setPrecursorMr(double precursorMr) {
		this.precursorMr = precursorMr;
	}
	
	public double getRt() {
		return rt;
	}

	public String getSequence() {
		return sequence;
	}

	public double getPepMass() {
		return pepMass;
	}

	public void setPepMass(double pepMass) {
		this.pepMass = pepMass;
	}

	public double getMassDiff() {
		return massDiff;
	}

	public double getMassDiffPPM() {
		double ppm = Math.abs(massDiff) / precursorMr * 1E6;
		return ppm;
	}
	
	public int getMiss() {
		return miss;
	}

	public int getNum_tol_term() {
		return num_tol_term;
	}

	public int getTot_num_ions() {
		return tot_num_ions;
	}

	public int getNum_matched_ions() {
		return num_matched_ions;
	}

	public String getProtein() {
		return protein;
	}

	public double getHyperscore() {
		return hyperscore;
	}

	public double getNextscore() {
		return nextscore;
	}

	public double getExpect() {
		return expect;
	}

	public double getOpenModmass() {
		return openModmass;
	}

	public double getOpenModMassDiff() {
		return openModMassDiff;
	}

	public PeptideModification[] getPTMs() {
		return ptms;
	}

	public void setPTMs(PeptideModification[] ptms) {
		this.ptms = ptms;
	}

	public double getBintensity() {
		return bintensity;
	}

	public double getYintensity() {
		return yintensity;
	}

	public double getBrankscore() {
		return brankscore;
	}

	public double getYrankscore() {
		return yrankscore;
	}

	public int getBcount() {
		return bcount;
	}

	public int getYcount() {
		return ycount;
	}

	public double getFragDeltaMass() {
		return fragDeltaMass;
	}

	public double getBmodIntensity() {
		return bmodIntensity;
	}

	public double getYmodIntensity() {
		return ymodIntensity;
	}

	public double getBmodrankscore() {
		return bmodrankscore;
	}

	public double getYmodrankscore() {
		return ymodrankscore;
	}

	public double getFragModDeltaMass() {
		return fragModDeltaMass;
	}

	public int getOpenModId() {
		return openModId;
	}
	
	public void setOpenModId(int openModId) {
		this.openModId = openModId;
	}

	public int[] getPossibleLoc() {
		return possibleLoc;
	}

	public double getNeutralLossMass() {
		return neutralLossMass;
	}

	public double getNlPeakRankScore() {
		return nlPeakRankScore;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public double getClassScore() {
		return classScore;
	}

	public void setClassScore(double classScore) {
		this.classScore = classScore;
	}
	
	public double getQValue() {
		return qvalue;
	}

	public void setQValue(double qvalue) {
		this.qvalue = qvalue;
	}

	public int getSameModCount() {
		return sameModCount;
	}

	public void setSameModCount(int sameModCount) {
		this.sameModCount = sameModCount;
	}

	public int getSamePepCount() {
		return samePepCount;
	}

	public void setSamePepCount(int samePepCount) {
		this.samePepCount = samePepCount;
	}

	public int getSameProCount() {
		return sameProCount;
	}

	public void setSameProCount(int sameProCount) {
		this.sameProCount = sameProCount;
	}
	
	public String getModSequence() {
		StringBuilder sb = new StringBuilder();
		if (ptms == null) {
			if (openModId == 0) {
				sb.append(sequence);
			} else {
				sb.append(sequence).append("(").append(openModId).append(")");
			}
		} else {
			Arrays.sort(ptms);
			int lastLoc = 0;
			for (int i = 0; i < ptms.length; i++) {
				if (!ptms[i].getPtm().isVariable()) {
					continue;
				}
				int loc = ptms[i].getLoc();
				double mass = ptms[i].getMass();
				if (loc == 0) {
					sb.append("(").append(mass).append(")");
				} else if (loc == sequence.length() + 1) {
					if (lastLoc == sequence.length()) {
						sb.append("(").append(mass).append(")");
					} else {
						sb.append(sequence.substring(lastLoc)).append("(").append(mass).append(")");
					}
				} else {
					sb.append(sequence.substring(lastLoc, loc)).append("(").append(mass).append(")");
					lastLoc = loc;
				}
			}
			sb.append(sequence.substring(lastLoc));
			if (openModId != 0) {
				sb.append("(").append(openModId).append(")");
			}
		}

		return sb.toString();
	}

	public Instance createSemisuperInstance(ArrayList<Attribute> attributeList, OpenMod om) {

		Instance instance = new DenseInstance(attributeList.size());
		instance.setValue(attributeList.get(0), getCharge());
		instance.setValue(attributeList.get(1), getPepMass());
		instance.setValue(attributeList.get(2), getSequence().length());

		instance.setValue(attributeList.get(3), getPrecursorMr());
		instance.setValue(attributeList.get(4), getMassDiff());
		instance.setValue(attributeList.get(5), getMassDiffPPM());

		instance.setValue(attributeList.get(6), getMiss());
		instance.setValue(attributeList.get(7), getNum_tol_term());
		instance.setValue(attributeList.get(8), getTot_num_ions());
		instance.setValue(attributeList.get(9), getNum_matched_ions());

		instance.setValue(attributeList.get(10), getHyperscore());
		instance.setValue(attributeList.get(11), getNextscore());
		instance.setValue(attributeList.get(12), (getHyperscore() - getNextscore()) / getHyperscore());
		instance.setValue(attributeList.get(13), -Math.log(getExpect()));
		
		if (bintensity == 0) {
			instance.setValue(attributeList.get(14), bintensity);
		} else {
			instance.setValue(attributeList.get(14), Math.log(bintensity));
		}
		if (yintensity == 0) {
			instance.setValue(attributeList.get(15), yintensity);
		} else {
			instance.setValue(attributeList.get(15), Math.log(yintensity));
		}

		instance.setValue(attributeList.get(16), brankscore);
		instance.setValue(attributeList.get(17), yrankscore);
		instance.setValue(attributeList.get(18), (double) bcount / (double) getSequence().length());
		instance.setValue(attributeList.get(19), (double) ycount / (double) getSequence().length());
		instance.setValue(attributeList.get(20), fragDeltaMass);

		instance.setValue(attributeList.get(21), openModmass);
		if (bmodIntensity == 0) {
			instance.setValue(attributeList.get(22), bmodIntensity);
		} else {
			instance.setValue(attributeList.get(22), Math.abs(bmodIntensity));
		}
		if (ymodIntensity == 0) {
			instance.setValue(attributeList.get(23), ymodIntensity);
		} else {
			instance.setValue(attributeList.get(23), Math.abs(ymodIntensity));
		}
		instance.setValue(attributeList.get(24), bmodrankscore);
		instance.setValue(attributeList.get(25), ymodrankscore);
		instance.setValue(attributeList.get(26), fragModDeltaMass);

		double[] gaussianParameters = om.getGaussianParameters();
		instance.setValue(attributeList.get(27), gaussianParameters[0]);
		instance.setValue(attributeList.get(28), gaussianParameters[1]);
		instance.setValue(attributeList.get(29), gaussianParameters[3]);
		
		instance.setValue(attributeList.get(30), Math.log(getSameModCount()));
		instance.setValue(attributeList.get(31), Math.log(getSamePepCount()));
		instance.setValue(attributeList.get(32), Math.log(getSameProCount()));

		if (isTarget()) {
			instance.setValue(attributeList.get(33), "P");
		} else {
			instance.setValue(attributeList.get(33), "N");
		}

		return instance;
	}
	
	public Instance createUnsuperInstance(ArrayList<Attribute> attributeList) {

		Instance instance = new DenseInstance(attributeList.size());
		instance.setValue(attributeList.get(0), getCharge());
		instance.setValue(attributeList.get(1), getPepMass());
		instance.setValue(attributeList.get(2), getSequence().length());

		instance.setValue(attributeList.get(3), getPrecursorMr());
		instance.setValue(attributeList.get(4), getMassDiff());
		instance.setValue(attributeList.get(5), getMassDiffPPM());

		instance.setValue(attributeList.get(6), getMiss());
		instance.setValue(attributeList.get(7), getNum_tol_term());
		instance.setValue(attributeList.get(8), getTot_num_ions());
		instance.setValue(attributeList.get(9), getNum_matched_ions());

		instance.setValue(attributeList.get(10), getHyperscore());
		instance.setValue(attributeList.get(11), getNextscore());
		instance.setValue(attributeList.get(12), (getHyperscore() - getNextscore()) / getHyperscore());
		instance.setValue(attributeList.get(13), -Math.log(getExpect()));

		if (bintensity == 0) {
			instance.setValue(attributeList.get(14), bintensity);
		} else {
			instance.setValue(attributeList.get(14), Math.log(bintensity));
		}
		if (yintensity == 0) {
			instance.setValue(attributeList.get(15), yintensity);
		} else {
			instance.setValue(attributeList.get(15), Math.log(yintensity));
		}
		instance.setValue(attributeList.get(16), brankscore);
		instance.setValue(attributeList.get(17), yrankscore);
		instance.setValue(attributeList.get(18), (double) bcount / (double) getSequence().length());
		instance.setValue(attributeList.get(19), (double) ycount / (double) getSequence().length());
		instance.setValue(attributeList.get(20), fragDeltaMass);

		instance.setValue(attributeList.get(21), openModmass);
		if (bmodIntensity == 0) {
			instance.setValue(attributeList.get(22), bmodIntensity);
		} else {
			instance.setValue(attributeList.get(22), Math.abs(bmodIntensity));
		}
		if (ymodIntensity == 0) {
			instance.setValue(attributeList.get(23), ymodIntensity);
		} else {
			instance.setValue(attributeList.get(23), Math.abs(ymodIntensity));
		}
		instance.setValue(attributeList.get(24), bmodrankscore);
		instance.setValue(attributeList.get(25), ymodrankscore);
		instance.setValue(attributeList.get(26), fragModDeltaMass);

		instance.setValue(attributeList.get(27), Math.log(getSameModCount()));
		instance.setValue(attributeList.get(28), Math.log(getSamePepCount()));
		instance.setValue(attributeList.get(29), Math.log(getSameProCount()));

		return instance;
	}
	
	public static class InverseClassScoreComparator implements Comparator<OpenPSM> {

		@Override
		public int compare(OpenPSM o1, OpenPSM o2) {
			// TODO Auto-generated method stub
			if (o1.classScore > o2.classScore)
				return -1;
			if (o1.classScore < o2.classScore)
				return 1;

			if (o1.hyperscore > o2.hyperscore)
				return -1;
			if (o1.hyperscore < o2.hyperscore)
				return 1;

			return 0;
		}
	} 
	
	public static class InverseExpectScoreComparator implements Comparator<OpenPSM> {

		@Override
		public int compare(OpenPSM o1, OpenPSM o2) {
			// TODO Auto-generated method stub

			if (o1.expect < o2.expect)
				return -1;
			if (o1.expect > o2.expect)
				return 1;

			if (o1.hyperscore > o2.hyperscore)
				return -1;
			if (o1.hyperscore < o2.hyperscore)
				return 1;

			return 0;
		}
	} 
	
	public static class InverseHyperScoreComparator implements Comparator<OpenPSM> {

		@Override
		public int compare(OpenPSM o1, OpenPSM o2) {
			// TODO Auto-generated method stub

			if (o1.hyperscore > o2.hyperscore)
				return -1;
			if (o1.hyperscore < o2.hyperscore)
				return 1;

			return 0;
		}
	} 
	
	public static class InverseQValueComparator implements Comparator<OpenPSM> {

		@Override
		public int compare(OpenPSM o1, OpenPSM o2) {
			// TODO Auto-generated method stub
			if (o1.qvalue < o2.qvalue)
				return -1;
			if (o1.qvalue > o2.qvalue)
				return 1;

			if (o1.hyperscore > o2.hyperscore)
				return -1;
			if (o1.hyperscore < o2.hyperscore)
				return 1;

			return 0;
		}
	} 
}
