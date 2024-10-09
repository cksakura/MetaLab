/**
 * 
 */
package bmi.med.uOttawa.metalab.quant.labelfree;

/**
 * @author Kai Cheng
 *
 */
public class FreeFeature {

	private int scannum;
	private int charge;
	private double mz;
	private double rt;
	private double intensity;

	/**
	 * @param scanNum
	 * @param value
	 */
	public FreeFeature(int scannum, int charge, double mz, double intensity) {
		// TODO Auto-generated constructor stub
		this.scannum = scannum;
		this.charge = charge;
		this.mz = mz;
		this.intensity = intensity;
	}
	
	/**
	 * @param scanNum
	 * @param value
	 */
	public FreeFeature(int scannum, int charge, double mz, double intensity, double rt) {
		// TODO Auto-generated constructor stub
		this.scannum = scannum;
		this.charge = charge;
		this.mz = mz;
		this.intensity = intensity;
		this.rt = rt;
	}

	public int getScannum() {
		return scannum;
	}

	public int getCharge() {
		return charge;
	}

	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}

	/**
	 * @return
	 */
	public double getRT() {
		// TODO Auto-generated method stub
		return rt;
	}

	/**
	 * @return
	 */
	public double getIntensity() {
		// TODO Auto-generated method stub
		return intensity;
	}

	/**
	 * @param rt
	 */
	public void setRT(double rt) {
		// TODO Auto-generated method stub
		this.rt = rt;
	}

	/**
	 * 
	 */
	public void validate() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return
	 */
	public int getScanNum() {
		// TODO Auto-generated method stub
		return scannum;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
