/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pepxml;

import bmi.med.uOttawa.metalab.dbSearch.AbstractPSM;

/**
 * @author Kai Cheng
 *
 */
public class PercolatorPSM extends AbstractPSM {

	private double per_q_value;
	private double per_PEP;
	private double per_svm;

	public PercolatorPSM(String filename, int scan, int charge, double precorsorMr, double rt, String sequence,
			double pepmass, double massdiff, int num_matched_ions, int hit_rank, String protein, double per_q_value,
			double per_PEP, double per_svm, boolean isTarget) {
		super(filename, scan, charge, precorsorMr, rt, sequence, pepmass, massdiff, num_matched_ions, hit_rank,
				protein, isTarget);
		this.per_q_value = per_q_value;
		this.per_PEP = per_PEP;
		this.per_svm = per_svm;
	}

	public double getPer_q_value() {
		return per_q_value;
	}

	public void setPer_q_value(double per_q_value) {
		this.per_q_value = per_q_value;
	}

	public double getPer_PEP() {
		return per_PEP;
	}

	public void setPer_PEP(double per_PEP) {
		this.per_PEP = per_PEP;
	}

	public double getPer_svm() {
		return per_svm;
	}

	public void setPer_svm(double per_svm) {
		this.per_svm = per_svm;
	}

}
