package bmi.med.uOttawa.metalab.dbSearch.skyline;

import java.util.ArrayList;
import java.util.List;

public class SkylinePeptide {
	private String sequence;
	private String modseq;
	private short missCleavage;
	private List<SkylinePrecursor> precursors;

	public SkylinePeptide(String sequence, String modseq, short missCleavage) {
		this.sequence = sequence;
		this.modseq = modseq;
		this.missCleavage = missCleavage;
		this.precursors = new ArrayList<>();
	}

	public void addPrecursor(SkylinePrecursor precursor) {
		this.precursors.add(precursor);
	}

	public String getSequence() {
		return sequence;
	}

	public String getModseq() {
		return modseq;
	}

	public short getMissCleavage() {
		return missCleavage;
	}

	public List<SkylinePrecursor> getPrecursors() {
		return precursors;
	}
	
}
