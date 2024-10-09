/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

/**
 * @author Kai Cheng
 *
 */
public class MS2Spectrum extends Spectrum {

	private double precursorMass;
	private double precursorMz;
	private double precursorIntensity;
	private int precursorCharge;
	
	public MS2Spectrum(){
		
	}
	
	public MS2Spectrum(double precursorMz, double precursorIntensity, int precursorCharge){
		this.precursorMz = precursorMz;
		this.precursorIntensity = precursorIntensity;
		this.precursorCharge = precursorCharge;
	}

	public double getPrecursorMass() {
		return precursorMass;
	}

	public void setPrecursorMass(double precursorMass) {
		this.precursorMass = precursorMass;
	}

	public double getPrecursorMz() {
		return precursorMz;
	}

	public void setPrecursorMz(double precursorMz) {
		this.precursorMz = precursorMz;
	}

	public double getPrecursorIntensity() {
		return precursorIntensity;
	}

	public void setPrecursorIntensity(double precursorIntensity) {
		this.precursorIntensity = precursorIntensity;
	}

	public int getPrecursorCharge() {
		return precursorCharge;
	}

	public void setPrecursorCharge(int precursorCharge) {
		this.precursorCharge = precursorCharge;
	}

}
