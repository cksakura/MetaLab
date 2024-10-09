/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.msFragger;

import java.util.ArrayList;
import java.util.Comparator;

import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

/**
 * @author Kai Cheng
 *
 */
public class MSFraggerPsm {

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
	private int rank;
	
	private String protein;
	private double hyperscore;
	private double nextscore;
	private double expect;
	private PeptideModification[] ptms;
	
	private double modMassDiff;
	private int modBinId;
	
	private String fileName;
	private int openModId;
	private double openModMass;
	private double classScore;
	
	private boolean isTarget;
	
	public MSFraggerPsm(int scan, int charge, double precursorMr, double rt, String sequence, double pepMass,
			double massDiff, int miss, int num_tol_term, int tot_num_ions, int num_matched_ions, int rank,
			String protein, double hyperscore, double nextscore, double expect, boolean isTarget) {
		
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
		this.rank = rank;
		this.protein = protein;
		this.hyperscore = hyperscore;
		this.nextscore = nextscore;
		this.expect = expect;
		this.isTarget = isTarget;
	}
	
	public MSFraggerPsm(int scan, int charge, double precursorMr, double rt, String sequence, double pepMass,
			double massDiff, int miss, int num_tol_term, int tot_num_ions, int num_matched_ions, int rank,
			String protein, double hyperscore, double nextscore, double expect, PeptideModification[] ptms, boolean isTarget) {

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
		this.rank = rank;
		this.protein = protein;
		this.hyperscore = hyperscore;
		this.nextscore = nextscore;
		this.expect = expect;
		this.ptms = ptms;
		this.isTarget = isTarget;
	}

	public int getScan() {
		return scan;
	}

	public double getRt() {
		return rt;
	}

	public double getPrecursorMr() {
		return precursorMr;
	}

	public String getSequence() {
		return sequence;
	}

	public double getPepMass() {
		return pepMass;
	}

	public double getMassDiff() {
		return massDiff;
	}
	
	public void setMassDiff(double massDiff) {
		this.massDiff = massDiff;
	}
	
	public double getMassDiffPPM() {
		double ppm = Math.abs(massDiff) / precursorMr * 1E6;
		return ppm;
	}

	public int getMiss() {
		return miss;
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

	public PeptideModification[] getPTMs() {
		return ptms;
	}

	public double getModMassDiff() {
		return modMassDiff;
	}

	public void setModMassDiff(double modMassDiff) {
		this.modMassDiff = modMassDiff;
	}

	public int getModBinId() {
		return modBinId;
	}

	public void setModBinId(int modBinId) {
		this.modBinId = modBinId;
	}

	public boolean isTarget() {
		return isTarget;
	}
	
	public int getCharge() {
		return charge;
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

	public int getRank() {
		return rank;
	}

	public double getClassScore() {
		return classScore;
	}

	public void setClassScore(double classScore) {
		this.classScore = classScore;
	}

	public int getOpenModId() {
		return openModId;
	}

	public void setOpenModId(int openModId) {
		this.openModId = openModId;
	}

	public double getOpenModMass() {
		return openModMass;
	}

	public void setOpenModMass(double openModMass) {
		this.openModMass = openModMass;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public static class MassDiffComparator implements Comparator<MSFraggerPsm> {

		@Override
		public int compare(MSFraggerPsm o1, MSFraggerPsm o2) {
			// TODO Auto-generated method stub
			if (o1.massDiff < o2.massDiff)
				return -1;
			if (o1.massDiff > o2.massDiff)
				return 1;
			return 0;
		}

	}
	
	public static class HyperscoreComparator implements Comparator<MSFraggerPsm> {

		@Override
		public int compare(MSFraggerPsm o1, MSFraggerPsm o2) {
			// TODO Auto-generated method stub
			if (o1.hyperscore < o2.hyperscore)
				return -1;
			if (o1.hyperscore > o2.hyperscore)
				return 1;
			return 0;
		}

	} 
	
	public static class InverseHyperscoreComparator implements Comparator<MSFraggerPsm> {

		@Override
		public int compare(MSFraggerPsm o1, MSFraggerPsm o2) {
			// TODO Auto-generated method stub
			if (o1.hyperscore > o2.hyperscore)
				return -1;
			if (o1.hyperscore < o2.hyperscore)
				return 1;
			return 0;
		}

	} 

	public Instance createInstance(ArrayList<Attribute> attrList) {
		Instance inst = new DenseInstance(15);
		inst.setValue(attrList.get(0), charge);
		inst.setValue(attrList.get(1), precursorMr);
		inst.setValue(attrList.get(2), rt);
		inst.setValue(attrList.get(3), sequence.length());
		inst.setValue(attrList.get(4), pepMass);
		inst.setValue(attrList.get(5), massDiff);
		inst.setValue(attrList.get(6), miss);
		inst.setValue(attrList.get(7), num_tol_term);
		inst.setValue(attrList.get(8), tot_num_ions);
		inst.setValue(attrList.get(9), num_matched_ions);
		inst.setValue(attrList.get(10), rank);
		inst.setValue(attrList.get(11), hyperscore);
		inst.setValue(attrList.get(12), nextscore);
		inst.setValue(attrList.get(13), expect);
		return inst;
	}
	
	public Instance createClusterInstance(ArrayList<Attribute> attrList) {
		Instance inst = new DenseInstance(14);
		inst.setValue(attrList.get(0), charge);
		inst.setValue(attrList.get(1), precursorMr);
		inst.setValue(attrList.get(2), rt);
		inst.setValue(attrList.get(3), sequence.length());
		inst.setValue(attrList.get(4), pepMass);
		inst.setValue(attrList.get(5), massDiff);
		inst.setValue(attrList.get(6), miss);
		inst.setValue(attrList.get(7), num_tol_term);
		inst.setValue(attrList.get(8), tot_num_ions);
		inst.setValue(attrList.get(9), num_matched_ions);
		inst.setValue(attrList.get(10), rank);
		inst.setValue(attrList.get(11), hyperscore);
		inst.setValue(attrList.get(12), nextscore);
		inst.setValue(attrList.get(13), expect);
		return inst;
	}
	
	
}
