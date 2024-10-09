package bmi.med.uOttawa.metalab.spectra.predict;

import java.util.HashMap;

public class PredictSpectra {

	private String pepinfo;
	private int charge;
	private HashMap<String, double[]> ionmap;

	public PredictSpectra(String pepinfo, int charge) {
		super();
		this.pepinfo = pepinfo;
		this.charge = charge;
		this.ionmap = new HashMap<String, double[]>();
	}

	public PredictSpectra(String pepinfo, int charge, HashMap<String, double[]> ionmap) {
		super();
		this.pepinfo = pepinfo;
		this.charge = charge;
		this.ionmap = ionmap;
	}

	void addIon(String name, double mz, double intensity) {
		this.ionmap.put(name, new double[] { mz, intensity });
	}

	public String getPepinfo() {
		return pepinfo;
	}

	public int getCharge() {
		return charge;
	}

	public HashMap<String, double[]> getIonmap() {
		return ionmap;
	}

}
