/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.philosopher;

import bmi.med.uOttawa.metalab.dbSearch.AbstractPSM;

/**
 * @author Kai Cheng
 *
 */
public class InteractPsm extends AbstractPSM {

	private double hyperScore;
	private double nextScore;
	private double evalue;
	private double probability;

	public InteractPsm(String fileName, int scan, int charge, double precorsorMr, double rt, String sequence,
			double pepmass, double massdiff, int num_matched_ions, int hit_rank, String protein, double hyperScore,
			double nextScore, double evalue, double probability, boolean isTarget) {
		super(fileName, scan, charge, precorsorMr, rt, sequence, pepmass, massdiff, num_matched_ions, hit_rank,
				protein, isTarget);
		// TODO Auto-generated constructor stub
		this.probability = probability;
		this.hyperScore = hyperScore;
		this.nextScore = nextScore;
		this.evalue = evalue;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public double getHyperScore() {
		return hyperScore;
	}

	public void setHyperScore(double hyperScore) {
		this.hyperScore = hyperScore;
	}

	public double getNextScore() {
		return nextScore;
	}

	public void setNextScore(double nextScore) {
		this.nextScore = nextScore;
	}

	public double getEvalue() {
		return evalue;
	}

	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}

}
