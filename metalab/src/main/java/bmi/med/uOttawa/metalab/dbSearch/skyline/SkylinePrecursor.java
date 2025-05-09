package bmi.med.uOttawa.metalab.dbSearch.skyline;

import java.util.ArrayList;
import java.util.List;

public class SkylinePrecursor {

	private short charge;
	private float mw;
	private float mz;
	private List<PrecursorPeak> ppList;

	public SkylinePrecursor(short charge, float mw, float mz) {
		this.charge = charge;
		this.mw = mw;
		this.mz = mz;
		this.ppList = new ArrayList<PrecursorPeak>();
	}

	public void addPrecursorPeak(String replicate, float rt, float rtStart, float rtEnd, float area, float massErrorPPM,
			float qvalue) {
		PrecursorPeak pp = new PrecursorPeak(replicate, rt, rtStart, rtEnd, area, massErrorPPM, qvalue);
		this.ppList.add(pp);
	}

	public short getCharge() {
		return charge;
	}

	public float getMw() {
		return mw;
	}

	public float getMz() {
		return mz;
	}

	public List<PrecursorPeak> getPpList() {
		return ppList;
	}

	class PrecursorPeak {
		String replicate;
		float rt;
		float rtStart;
		float rtEnd;
		float area;
		float massErrorPPM;
		float qvalue;

		public PrecursorPeak(String replicate, float rt, float rtStart, float rtEnd, float area, float massErrorPPM,
				float qvalue) {
			super();
			this.replicate = replicate;
			this.rt = rt;
			this.rtStart = rtStart;
			this.rtEnd = rtEnd;
			this.area = area;
			this.massErrorPPM = massErrorPPM;
			this.qvalue = qvalue;
		}

		public String getReplicate() {
			return replicate;
		}

		public float getRt() {
			return rt;
		}

		public float getRtStart() {
			return rtStart;
		}

		public float getRtEnd() {
			return rtEnd;
		}

		public float getArea() {
			return area;
		}

		public float getMassErrorPPM() {
			return massErrorPPM;
		}

		public float getQvalue() {
			return qvalue;
		}

		
	}
}
