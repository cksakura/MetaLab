/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.psm;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

/**
 * @author Kai Cheng
 *
 */
public class PeptideSpectraMatch {
	
	protected int scannum;
	protected int missCleavage;
	protected double pepmr;
	protected double backboneMass;
	protected double deltaMass;
	protected String sequence;
	protected String modSequence;
	protected PeptideModification[] pms;
	protected boolean isTargetMatch;
	protected SearchedReference[] ref;
	protected Spectrum spectrum;
	
	public PeptideSpectraMatch(int missCleavage, double pepmr, double deltaMass, String sequence){
		this.missCleavage = missCleavage;
		this.pepmr = pepmr;
		this.deltaMass = deltaMass;
		this.sequence = sequence;
	}

	public int getScannum() {
		return scannum;
	}

	public void setScannum(int scannum) {
		this.scannum = scannum;
	}

	public int getMissCleavage() {
		return missCleavage;
	}

	public void setMissCleavage(int missCleavage) {
		this.missCleavage = missCleavage;
	}

	public double getPepmr() {
		return pepmr;
	}

	public void setPepmr(double pepmr) {
		this.pepmr = pepmr;
	}

	public double getBackboneMass() {
		return backboneMass;
	}

	public void setBackboneMass(double backboneMass) {
		this.backboneMass = backboneMass;
	}

	public double getDeltaMass() {
		return deltaMass;
	}

	public void setDeltaMass(double deltaMass) {
		this.deltaMass = deltaMass;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public PeptideModification[] getPeptideMods() {
		return pms;
	}

	public void setPeptideMods(PeptideModification[] pms) {
		this.pms = pms;
	}
	
	public String getModSequence() {
		if (modSequence == null) {
			if (pms == null) {
				modSequence = "";
			} else {
				HashMap<Integer, String> map = new HashMap<Integer, String>();
				for (PeptideModification pm : pms) {
					map.put(pm.getLoc(), pm.getName());
				}
				StringBuilder sb = new StringBuilder();
				sb.append(".");
				if (map.containsKey(0)) {
					sb.append("(").append(map.get(0)).append(")");
				}
				for (int i = 0; i < sequence.length(); i++) {
					sb.append(sequence.charAt(i));
					if (map.containsKey(i + 1)) {
						sb.append("(").append(map.get(i + 1)).append(")");
					}
				}
				sb.append(".");
				if (map.containsKey(sequence.length())) {
					sb.append("(").append(map.get(sequence.length())).append(")");
				}
				modSequence = sb.toString();
			}
		}
		return modSequence;
	}

	public void setModSequence(String modSequence) {
		this.modSequence = modSequence;
	}

	public boolean isTargetMatch() {
		return isTargetMatch;
	}

	public void setTargetMatch(boolean isTargetMatch) {
		this.isTargetMatch = isTargetMatch;
	}

	public SearchedReference[] getRef() {
		return ref;
	}

	public void setRef(SearchedReference[] ref) {
		this.ref = ref;
	}

	public Spectrum getSpectrum() {
		return spectrum;
	}

	public void setSpectrum(Spectrum spectrum) {
		this.spectrum = spectrum;
	}

	public int getCharge() {
		return spectrum.getPrecursorCharge();
	}
	
	public double getPrecursorMz() {
		return spectrum.getPrecursorMz();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(scannum).append(";");
		sb.append(pepmr).append(";");
		sb.append(deltaMass).append(";");
		sb.append(missCleavage).append(";");
		sb.append(sequence).append(";");
		if(isTargetMatch)
			sb.append("1");
		else
			sb.append("0");
		for(SearchedReference sr : ref){
			sb.append(sr).append(";");
		}
		sb.append(spectrum.getPrecursorMz()).append(" ");
		sb.append(spectrum.getPrecursorIntensity()).append(" ");
		sb.append(spectrum.getPrecursorCharge()).append(",");
		Peak[] peaks = spectrum.getPeaks();
		for(Peak peak : peaks){
			sb.append(peak.getMz()).append(" ");
			sb.append(peak.getIntensity()).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
}
