/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

/**
 * @author Kai Cheng
 *
 */
public class GlycoJudgeParameter {

	private float intenThres;
	// PPM
	private float mzThresPPM;
	// AMU
	private float mzThresAMU;
	private float mzLowLimit;
	private float isoIntenThres;
	private float rtTole;

	private boolean deGlycoLabel;
	private int topnStructure;

	public GlycoJudgeParameter(float intenThres, float mzThresPPM, float mzThresAMU, float mzLowLimit,
			float isoIntenThres, float rtTole, int topnStructure) {
		this.intenThres = intenThres;
		this.mzThresPPM = mzThresPPM;
		this.mzThresAMU = mzThresAMU;
		this.mzLowLimit = mzLowLimit;
		this.isoIntenThres = isoIntenThres;
		this.rtTole = rtTole;
		this.topnStructure = topnStructure;
	}

	public float getIntenThres() {
		return intenThres;
	}

	public float getMzThresPPM() {
		return mzThresPPM;
	}

	public float getMzThresAMU() {
		return mzThresAMU;
	}

	public float getMzLowLimit() {
		return mzLowLimit;
	}

	public float getIsoIntenThres() {
		return isoIntenThres;
	}

	public float getRtTole() {
		return rtTole;
	}

	public int getTopnStructure() {
		return topnStructure;
	}

	public void setDeGlycoLabel(boolean label) {
		this.deGlycoLabel = label;
	}

	/**
	 * If the identified peptide is labeled.
	 * 
	 * @return
	 */
	public boolean getDeGlycoLabel() {
		return deGlycoLabel;
	}

	public static GlycoJudgeParameter defaultParameter() {
		GlycoJudgeParameter para = new GlycoJudgeParameter(0.001f, 30f, 0.01f, 500, 0.3f, 30.0f, 1);
		para.setDeGlycoLabel(true);
		return para;
	}

}
