/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.tpp;

import java.util.Comparator;

/**
 * @author Kai Cheng
 *
 */
public class PeptideProphehPSM {
	
	private String spectrum;
	private int start_scan;
	private int assumed_charge;
	private double precursor_neutral_mass;
	private double retention_time_sec;
	private String peptide;
	private double massdiff;
	private int num_missed_cleavages;
	private String protein;
	
	private double hyperscore;
	private double nextscore;
	private double expect;
	
	private double qvalue;
	private double probability;

	public PeptideProphehPSM(String spectrum, int start_scan, int assumed_charge, double precursor_neutral_mass,
			double retention_time_sec, String peptide, double massdiff, int num_missed_cleavages, String protein,
			double hyperscore, double nextscore, double expect, double probability) {
		this.spectrum = spectrum;
		this.start_scan = start_scan;
		this.assumed_charge = assumed_charge;
		this.precursor_neutral_mass = precursor_neutral_mass;
		this.retention_time_sec = retention_time_sec;
		this.peptide = peptide;
		this.massdiff = massdiff;
		this.num_missed_cleavages = num_missed_cleavages;
		this.protein = protein;
		this.hyperscore = hyperscore;
		this.nextscore = nextscore;
		this.expect = expect;
		this.probability = probability;
	}

	public String getSpectrum() {
		return spectrum;
	}

	public int getStart_scan() {
		return start_scan;
	}

	public int getAssumed_charge() {
		return assumed_charge;
	}

	public double getPrecursor_neutral_mass() {
		return precursor_neutral_mass;
	}

	public double getRetention_time_sec() {
		return retention_time_sec;
	}

	public String getPeptide() {
		return peptide;
	}

	public double getMassdiff() {
		return massdiff;
	}

	public int getNum_missed_cleavages() {
		return num_missed_cleavages;
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

	public double getProbability() {
		return probability;
	}
	
	public boolean isTarget() {
		return !protein.startsWith("REV");
	}

	public void setQvalue(double qvalue) {
		this.qvalue = qvalue;
	}
	
	public double getQvalue() {
		return qvalue;
	}
	
	public static class InverseProbabilityComparator implements Comparator<PeptideProphehPSM> {

		@Override
		public int compare(PeptideProphehPSM o1, PeptideProphehPSM o2) {
			// TODO Auto-generated method stub
			if (o1.probability > o2.probability)
				return -1;

			return 0;
		}

	} 
}
