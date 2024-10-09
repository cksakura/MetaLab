/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math.curveFitting;

/**
 * @author Kai Cheng
 *
 */
public interface IFunction {
	
	public double fx(double x);

	public double[] getPara();

	public void setPara(double[] para);

	public int getParaNum();

	public double[] getInitialValue(double[] x, double[] xData);
}
