/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;

/**
 * @author Kai Cheng
 *
 */
public class PeakSegment implements Comparable<PeakSegment> {

	private double peakMz;
	private double peakInten;
	private double mr;
	private double ppm;
	private int charge;

	private double beg;
	private double end;

	public PeakSegment(Peak peak, int charge) {
		this.peakMz = peak.getMz();
		this.peakInten = peak.getIntensity();
		this.charge = charge;
		this.ppm = 20;

		this.mr = (peakMz - AminoAcidProperty.PROTON_W) * (double) charge;
		this.beg = mr - mr * ppm * 1.0E-6;
		this.end = mr + mr * ppm * 1.0E-6;
	}

	public PeakSegment(double mz, double inten, int charge) {
		this.peakMz = mz;
		this.peakInten = inten;
		this.charge = charge;
		this.ppm = 20;

		this.mr = (peakMz - AminoAcidProperty.PROTON_W) * (double) charge;
		this.beg = mr - mr * ppm * 1.0E-6;
		this.end = mr + mr * ppm * 1.0E-6;
	}

	public PeakSegment(Peak peak, int charge, double ppm) {
		this.peakMz = peak.getMz();
		this.peakInten = peak.getIntensity();
		this.charge = charge;
		this.ppm = ppm;

		this.mr = (peakMz - AminoAcidProperty.PROTON_W) * (double) charge;
		this.beg = mr - mr * ppm * 1.0E-6;
		this.end = mr + mr * ppm * 1.0E-6;
	}

	public PeakSegment(double mz, double inten, int charge, double ppm) {
		this.peakMz = mz;
		this.peakInten = inten;
		this.charge = charge;
		this.ppm = ppm;

		this.mr = (peakMz - AminoAcidProperty.PROTON_W) * (double) charge;
		this.beg = mr - mr * ppm * 1.0E-6;
		this.end = mr + mr * ppm * 1.0E-6;
	}

	public boolean isOverlap(PeakSegment ps) {

		double b0 = this.beg;
		double e0 = this.end;
		double b1 = ps.beg;
		double e1 = ps.end;

		if (b0 < b1) {
			if (e0 < b1) {
				return false;
			} else {
				return true;
			}
		} else if (b0 == b1) {

			return true;

		} else {
			if (e1 < b0) {
				return false;
			} else {
				return true;
			}
		}
	}

	public double getMass() {
		return mr;
	}

	public double getPeakMz() {
		return peakMz;
	}

	public double getPeakInten() {
		return peakInten;
	}

	public double getBeg() {
		return beg;
	}

	public double getEnd() {
		return end;
	}

	public int getCharge() {
		return charge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PeakSegment [peakMz=" + peakMz + ", peakInten=" + peakInten + ", mass=" + mr + ", ppm=" + ppm
				+ ", charge=" + charge + ", beg=" + beg + ", end=" + end + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PeakSegment o) {
		// TODO Auto-generated method stub
		double beg0 = o.beg;

		if (this.beg < beg0) {

			return -1;

		} else if (this.beg == beg0) {

			return 0;

		} else {

			return 1;
		}
	}

}
