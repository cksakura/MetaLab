/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pepxml;

import bmi.med.uOttawa.metalab.dbSearch.AbstractPSM;

/**
 * @author Kai Cheng
 *
 */
public class SequestHTPSM extends AbstractPSM {
	
	private double xcorr;

	public SequestHTPSM(String fileName, int scan, int charge, double precorsorMr, double rt, String sequence,
			double pepmass, double massdiff, int num_matched_ions, int hit_rank, String protein, double xcorr, boolean isTarget) {
		super(fileName, scan, charge, precorsorMr, rt, sequence, pepmass, massdiff, num_matched_ions, hit_rank, protein, isTarget);
		// TODO Auto-generated constructor stub
		this.xcorr = xcorr;
	}

	public double getXcorr() {
		return xcorr;
	}

	public void setXcorr(double xcorr) {
		this.xcorr = xcorr;
	}

}
