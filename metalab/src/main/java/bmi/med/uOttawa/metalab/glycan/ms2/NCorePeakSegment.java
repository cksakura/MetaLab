/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.PeakSegment;

/**
 * @author Kai Cheng
 *
 */
public class NCorePeakSegment extends PeakSegment {

	/**
	 * 0=pep+NexNAc; 1=pep+NexNAc*2...
	 */
	private int type;
	private double originalMz;

	public NCorePeakSegment(Peak peak, int charge, double originalMz, int type) {
		super(peak, charge, originalMz);
		this.type = type;
	}

	public NCorePeakSegment(double mz, double inten, int charge, double originalMz, int type) {
		super(mz, inten, charge);
		this.originalMz = originalMz;
		this.type = type;
	}

	public NCorePeakSegment(Peak peak, int charge, double ppm, double originalMz, int type) {
		super(peak, charge, ppm);
		this.originalMz = originalMz;
		this.type = type;
	}

	public NCorePeakSegment(double mz, double inten, int charge, double ppm, double originalMz, int type) {
		super(mz, inten, charge, ppm);
		this.originalMz = originalMz;
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public double getOriginalMz() {
		return originalMz;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append(originalMz).append("-").append(super.getPeakMz()).append("-").append(super.getMass()).append("-")
				.append(super.getCharge()).append("-").append(super.getPeakInten());

		return sb.toString();
	}

}
