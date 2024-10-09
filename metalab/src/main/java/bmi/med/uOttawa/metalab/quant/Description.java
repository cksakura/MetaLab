/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

/**
 * @author Kai Cheng
 *
 */
public class Description {

	/**
	 * The ms level of this scan.
	 */
	private int msLevel;
	/**
	 * Inner index counter to evaluate how many scans have been created; This
	 * index is mainly for the reading of peak list;
	 */
	private int index;
	/**
	 * The scan number of this scan
	 */
	private Integer scannum;
	/**
	 * THe number of precursor ions for this scan. If this scan is a MS2 scan,
	 * the number will be 1, if this scan is a MS3 scan, the number of precursor
	 * ions will be 2.
	 */
	private int precursornum = 1;

	/**
	 * For ms2 scan, only one precursor ms is valid; while for ms3 scans,
	 * normally, there are two precursor mses, one from full ms and the other
	 * from ms2. (msn has n-1 precursor ms, currently only ms3 and ms2 is
	 * defined)
	 */
	private double preMs;
	/**
	 * For ms2 scan, only one precursor ms is valide; while for ms3 scans,
	 * normally, there are two precursor mses, one from full ms and the other
	 * from ms2. (msn has n-1 precursor ms, currently only ms3 and ms2 is
	 * defined)
	 */
	private double preMs2;

	/**
	 * The charge state of this scan. For high accuracy mass spectra in MzXML
	 * format, the charge state has been determined. In other conditions, this
	 * value is left as 0
	 * 
	 */
	private int charge;

	private int precursorScanNum;

	private double totIonCurrent;

	private double precursorInten;

	private double precursorInten2;

	private double timeInMinutes;

	public Description() {

	}

	public Description(int scanNum, int msLevel) {
		this.scannum = scanNum;
		this.msLevel = msLevel;
	}

	public Description(int scanNum, int msLevel, double preMs, int charge, double precursorInten) {
		this.scannum = scanNum;
		this.msLevel = msLevel;
		this.preMs = preMs;
		this.charge = charge;
		this.precursorInten = precursorInten;
	}

	public Description(int scanNum, int msLevel, double timeInMinutes, double totIonCurrent) {
		this.scannum = scanNum;
		this.msLevel = msLevel;
		this.timeInMinutes = timeInMinutes;
		this.totIonCurrent = totIonCurrent;
	}

	public Description(int scanNum, int msLevel, double timeInMinutes, int precursornum, int precursorScanNum,
			double preMs, int charge, double precursorInten) {
		this.scannum = scanNum;
		this.msLevel = msLevel;
		this.timeInMinutes = timeInMinutes;
		this.precursornum = precursornum;
		this.precursorScanNum = precursorScanNum;
		this.preMs = preMs;
		this.charge = charge;
		this.precursorInten = precursorInten;
	}

	public void setPreNum(int precursornum) {
		this.precursornum = precursornum;
	}

	public int getPreNum() {
		return precursornum;
	}

	public void setScanNum(int scannum) {
		this.scannum = scannum;
	}

	public int getScanNum() {
		return scannum;
	}

	public void setLevel(int msLevel) {
		this.msLevel = msLevel;
	}

	public int getLevel() {
		return msLevel;
	}

	public double getPreMs() {
		return preMs;
	}

	public int getIndex() {
		return index;
	}

	public void setPreMs(double preMs) {
		this.preMs = preMs;
	}

	public void setPreMs2(double preMs) {
		this.preMs2 = preMs;
	}

	public double getPrecursorInten() {
		return precursorInten;
	}

	public double getPrecursorInten2() {
		return precursorInten2;
	}

	public void setPrecursorInten(double precursorInten) {
		this.precursorInten = precursorInten;
	}

	public void setPrecursorInten2(double precursorInten2) {
		this.precursorInten2 = precursorInten2;
	}

	public void setPrecursorScanNum(int precursorScanNum) {
		this.precursorScanNum = precursorScanNum;
	}

	public int getPrecursorScanNum() {
		return precursorScanNum;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public int getCharge() {
		return charge;
	}

	public double getPreMs2() {
		return preMs2;
	}

	public void setRenTimeMinute(double timeInMinutes) {
		this.timeInMinutes = timeInMinutes;
	}

	public double getRenTimeMinute() {
		return timeInMinutes;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
