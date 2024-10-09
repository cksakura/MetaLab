/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.io.Serializable;

/**
 * @author Kai Cheng
 *
 */
public class MassUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3316981794445839568L;

	private int id;
	private double mono;
	private double average;
	private double[][] fragments;
	private double[][] rev_fragments;
	private int[] composition;

	private int matchPeakId;

	public MassUnit(double mono, double average, double[][] fragments) {

		this.mono = mono;
		this.average = average;
		this.fragments = fragments;
	}

	public MassUnit(int id, double mono, double average, double[][] fragments) {

		this.id = id;
		this.mono = mono;
		this.average = average;
		this.fragments = fragments;
	}

	public MassUnit(int id, double mono, double average, double[][] fragments, double[][] rev_fragments) {

		this.id = id;
		this.mono = mono;
		this.average = average;
		this.fragments = fragments;
		this.rev_fragments = rev_fragments;
	}

	public int getId() {
		return id;
	}

	public double getMono() {
		return mono;
	}

	public double getAverage() {
		return average;
	}

	public int[] getComposition() {
		return composition;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double[][] getFragments() {
		return fragments;
	}

	public double[][] getRevFragments() {
		return rev_fragments;
	}

	public int getMatchPeakId() {
		return matchPeakId;
	}

	public void setMatchPeakId(int matchPeakId) {
		this.matchPeakId = matchPeakId;
	}

}
