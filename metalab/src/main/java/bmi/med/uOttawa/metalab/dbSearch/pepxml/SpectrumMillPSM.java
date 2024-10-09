/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pepxml;

import bmi.med.uOttawa.metalab.dbSearch.AbstractPSM;

/**
 * @author Kai Cheng
 *
 */
public class SpectrumMillPSM extends AbstractPSM {
	
	private double smScore;
	private double deltaForwardReverseScore;
	private double deltaRank12Score;

	public SpectrumMillPSM(String fileName, int scan, int charge, double precorsorMr, double rt, String sequence,
			double pepmass, double massdiff, int num_matched_ions, int hit_rank, String protein, double smScore,
			double deltaForwardReverseScore, double deltaRank12Score, boolean isTarget) {
		super(fileName, scan, charge, precorsorMr, rt, sequence, pepmass, massdiff, num_matched_ions, hit_rank,
				protein, isTarget);
		// TODO Auto-generated constructor stub
		this.smScore = smScore;
		this.deltaForwardReverseScore = deltaForwardReverseScore;
		this.deltaRank12Score = deltaRank12Score;
	}

	public double getSmScore() {
		return smScore;
	}

	public void setSmScore(double smScore) {
		this.smScore = smScore;
	}

	public double getDeltaForwardReverseScore() {
		return deltaForwardReverseScore;
	}

	public void setDeltaForwardReverseScore(double deltaForwardReverseScore) {
		this.deltaForwardReverseScore = deltaForwardReverseScore;
	}

	public double getDeltaRank12Score() {
		return deltaRank12Score;
	}

	public void setDeltaRank12Score(double deltaRank12Score) {
		this.deltaRank12Score = deltaRank12Score;
	}

	
	

}
