package bmi.med.uOttawa.metalab.dbSearch.diann;

public class DiaNNParquetPrecursor {
	float mz;
	int charge;
	float rt;
	String seq;
	String pro;
	float rtStart;
	float rtStop;
	float area;

	DiaNNParquetPrecursor(float mz, int charge, float rt, String seq, String pro, float rtStart, float rtStop,
			float area) {
		this.mz = mz;
		this.charge = charge;
		this.rt = rt;
		this.seq = seq;
		this.pro = pro;
		this.rtStart = rtStart;
		this.rtStop = rtStop;
		this.area = area;
	}

	public float getMz() {
		return mz;
	}

	public int getCharge() {
		return charge;
	}

	public float getRt() {
		return rt;
	}

	public String getSeq() {
		return seq;
	}

	public String getPro() {
		return pro;
	}

	public float getRtStart() {
		return rtStart;
	}

	public float getRtStop() {
		return rtStop;
	}

	public float getArea() {
		return area;
	}
}
