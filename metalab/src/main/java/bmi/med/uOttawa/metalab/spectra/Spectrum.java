/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import java.text.DecimalFormat;

/**
 * @author Kai Cheng
 *
 */
public class Spectrum {
	
	private DecimalFormat df2 = new DecimalFormat("#.##");
	
	private String title;
	private int scannum;
	private double rt;
	private int mslevel;
	private Peak[] peaks;
	private double basePeakMz;
	private double basePeakIntensity;
	private double totalCurrent;
	
	// these field belongs to MS2 spectrum
	private int precursorScan;
	private double precursorMass;
	private double precursorMz;
	private double precursorIntensity;
	private int precursorCharge;
	
	// these field belongs to MSn spectrum
	private int ms1PrecursorScan;
	private double ms1PrecursorMass;
	private double ms1PrecursorMz;
	private double ms1PrecursorIntensity;
	private int ms1PrecursorCharge;
	
	public Spectrum(){
		
	}
	
	public Spectrum(int scannum){
		this.scannum = scannum;
	}
	
	public Spectrum(int scannum, double rt){
		this.scannum = scannum;
		this.rt = rt;
	}
	
	public Spectrum(int scannum, double rt, int mslevel, double basePeakMz, double basePeakIntensity, double totalCurrent){
		this.scannum = scannum;
		this.rt = rt;
		this.mslevel = mslevel;
		this.basePeakMz = basePeakMz;
		this.basePeakIntensity = basePeakIntensity;
		this.totalCurrent = totalCurrent;
	}
	
	public Spectrum(int scannum, double rt, Peak[] peaks){
		this.scannum = scannum;
		this.rt = rt;
		this.peaks = peaks;
	}
	
	public int getScannum() {
		return scannum;
	}

	public void setScannum(int scannum) {
		this.scannum = scannum;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public Peak[] getPeaks() {
		return peaks;
	}

	public void setPeaks(Peak[] peaks) {
		this.peaks = peaks;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMslevel() {
		return mslevel;
	}

	public void setMslevel(int mslevel) {
		this.mslevel = mslevel;
	}

	public double getBasePeakMz() {
		return basePeakMz;
	}

	public void setBasePeakMz(double basePeakMz) {
		this.basePeakMz = basePeakMz;
	}

	public double getBasePeakIntensity() {
		return basePeakIntensity;
	}

	public void setBasePeakIntensity(double basePeakIntensity) {
		this.basePeakIntensity = basePeakIntensity;
	}

	public double getTotalCurrent() {
		return totalCurrent;
	}

	public void setTotalCurrent(double totalCurrent) {
		this.totalCurrent = totalCurrent;
	}

	public double getPrecursorMass() {
		return precursorMass;
	}

	public void setPrecursorMass(double precursorMass) {
		this.precursorMass = precursorMass;
	}

	public int getPrecursorScan() {
		return precursorScan;
	}

	public void setPrecursorScan(int precursorScan) {
		this.precursorScan = precursorScan;
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
	
	public int getMs1PrecursorScan() {
		return ms1PrecursorScan;
	}

	public void setMs1PrecursorScan(int ms1PrecursorScan) {
		this.ms1PrecursorScan = ms1PrecursorScan;
	}

	public double getMs1PrecursorMass() {
		return ms1PrecursorMass;
	}

	public void setMs1PrecursorMass(double ms1PrecursorMass) {
		this.ms1PrecursorMass = ms1PrecursorMass;
	}

	public double getMs1PrecursorMz() {
		return ms1PrecursorMz;
	}

	public void setMs1PrecursorMz(double ms1PrecursorMz) {
		this.ms1PrecursorMz = ms1PrecursorMz;
	}

	public double getMs1PrecursorIntensity() {
		return ms1PrecursorIntensity;
	}

	public void setMs1PrecursorIntensity(double ms1PrecursorIntensity) {
		this.ms1PrecursorIntensity = ms1PrecursorIntensity;
	}

	public int getMs1PrecursorCharge() {
		return ms1PrecursorCharge;
	}

	public void setMs1PrecursorCharge(int ms1PrecursorCharge) {
		this.ms1PrecursorCharge = ms1PrecursorCharge;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(scannum).append(" ");
		sb.append(precursorMz).append(" ");
		sb.append(precursorIntensity).append(" ");
		sb.append(precursorCharge).append(" ");
		sb.append(df2.format(rt)).append(",");
		Peak[] peaks = this.getPeaks();
		for (Peak peak : peaks) {
			sb.append(peak.getMz()).append(" ");
			sb.append(peak.getIntensity()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public static Spectrum parse(String line) {
		String[] cs = line.split(",");
		String[] ppcs = cs[0].split(" ");
		int scannum = Integer.parseInt(ppcs[0]);
		double premz = Double.parseDouble(ppcs[1]);
		double preintensity = Double.parseDouble(ppcs[2]);
		int precharge = Integer.parseInt(ppcs[3]);
		double rt = Double.parseDouble(ppcs[4]);
		Peak[] peaks = new Peak[cs.length - 1];
		for (int i = 0; i < peaks.length; i++) {
			String[] pics = cs[i + 1].split(" ");
			peaks[i] = new Peak(Double.parseDouble(pics[0]), Double.parseDouble(pics[1]));
		}
		Spectrum spectrum = new Spectrum();
		spectrum.scannum = scannum;
		spectrum.precursorMz = premz;
		spectrum.precursorIntensity = preintensity;
		spectrum.precursorCharge = precharge;
		spectrum.rt = rt;
		spectrum.peaks = peaks;
		return spectrum;
	}
	
}
