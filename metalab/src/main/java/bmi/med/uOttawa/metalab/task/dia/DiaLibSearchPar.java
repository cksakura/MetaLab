package bmi.med.uOttawa.metalab.task.dia;

public class DiaLibSearchPar {

	private double ms1Accu;
	private double massAccu;
	private double scanWin;
	private double qValue;
	private boolean mbr;
	private int quanStrategyID;
	private int threadCount;
	private String[] diaFiles;

	public DiaLibSearchPar() {
		this.ms1Accu = 15.0;
		this.massAccu = 15.0;
		this.scanWin = 15.0;
		this.qValue = 0.01;
		this.mbr = true;
		this.quanStrategyID = 0;
		this.threadCount = 4;
	}

	public DiaLibSearchPar(double ms1Accu, double massAccu, double scanWin, double qValue, boolean mbr,
			int quanStrategyID, int threadCount) {
		this.ms1Accu = ms1Accu;
		this.massAccu = massAccu;
		this.scanWin = scanWin;
		this.qValue = qValue;
		this.mbr = mbr;
		this.quanStrategyID = quanStrategyID;
		this.threadCount = threadCount;
	}

	public DiaLibSearchPar(double ms1Accu, double massAccu, double scanWin, boolean mbr, int quanStrategyID,
			int threadCount, String[] diaFiles) {
		this.ms1Accu = ms1Accu;
		this.massAccu = massAccu;
		this.scanWin = scanWin;
		this.mbr = mbr;
		this.quanStrategyID = quanStrategyID;
		this.threadCount = threadCount;
		this.diaFiles = diaFiles;
	}

	public double getMs1Accu() {
		return ms1Accu;
	}

	public void setMs1Accu(double ms1Accu) {
		this.ms1Accu = ms1Accu;
	}

	public double getMassAccu() {
		return massAccu;
	}

	public void setMassAccu(double massAccu) {
		this.massAccu = massAccu;
	}

	public double getScanWin() {
		return scanWin;
	}

	public void setScanWin(double scanWin) {
		this.scanWin = scanWin;
	}

	public boolean isMbr() {
		return mbr;
	}

	public void setMbr(boolean mbr) {
		this.mbr = mbr;
	}

	public int getQuanStrategyId() {
		return quanStrategyID;
	}

	public void setQuanStrategyId(int quanStrategyID) {
		this.quanStrategyID = quanStrategyID;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public String[] getDiaFiles() {
		return diaFiles;
	}

	public void setDiaFiles(String[] diaFiles) {
		this.diaFiles = diaFiles;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

}
