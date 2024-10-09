/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

/**
 * @author Kai Cheng
 *
 */
public class QParameter {

	private float mzTolerance;
	private int missNum;
	private int leastIdenNum;

	public QParameter(float mzTolerance, int missNum, int leastIdenNum) {
		this.mzTolerance = mzTolerance;
		this.missNum = missNum;
		this.leastIdenNum = leastIdenNum;
	}

	public static QParameter default_parameter() {
		return new QParameter(10f, 4, 1);
	}

	public float getMzTole() {
		return this.mzTolerance;
	}

	public int getMissNum() {
		return this.missNum;
	}

	public int getLeastINum() {
		return this.leastIdenNum;
	}

}
