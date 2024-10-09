package bmi.med.uOttawa.metalab.task.dia.par;

import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;

public class MetaDiaParGenLib {

	private DiannParameter diannPar;
	private double pepDetectThreshold;
	private int thread;
	private MagDbItem magDb;

	public MetaDiaParGenLib(MagDbItem magDb, int thread, double pepDetectThreshold) {
		this.magDb = magDb;
		this.diannPar = new DiannParameter();
		this.thread = thread;
		this.pepDetectThreshold = pepDetectThreshold;
	}

	public MetaDiaParGenLib(MagDbItem magDb, DiannParameter diannPar, int thread, double pepDetectThreshold) {
		this.magDb = magDb;
		this.diannPar = diannPar;
		this.thread = thread;
		this.pepDetectThreshold = pepDetectThreshold;
	}

	public DiannParameter getDiannPar() {
		return diannPar;
	}

	public void setDiannPar(DiannParameter diannPar) {
		this.diannPar = diannPar;
	}

	public double getPepDetectThreshold() {
		return pepDetectThreshold;
	}

	public void setPepDetectThreshold(double pepDetectThreshold) {
		this.pepDetectThreshold = pepDetectThreshold;
	}

	public MagDbItem getMagDb() {
		return magDb;
	}

	public void setMagDb(MagDbItem magDb) {
		this.magDb = magDb;
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}

}
