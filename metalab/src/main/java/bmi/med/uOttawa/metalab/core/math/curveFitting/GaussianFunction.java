/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math.curveFitting;

import bmi.med.uOttawa.metalab.core.math.MathTool;

/**
 * @author Kai Cheng
 *
 */
public class GaussianFunction implements IFunction {

	private double[] para;

	public GaussianFunction() {
		this.para = new double[] { 1.0, 1.0, 1.0 };
	}

	public GaussianFunction(double a, double b, double c) {
		this.para = new double[3];
		this.para[0] = a;
		this.para[1] = b;
		this.para[2] = c;
	}

	public GaussianFunction(double[] para) {
		this.para = para;
	}

	public double fx(double x) {

		if (para[2] == 0) {
			return 0;
		}

		double y = para[0] * Math.exp(-0.5 * (x - para[1]) * (x - para[1]) / (para[2] * para[2]));

		return y;
	}

	public double[] getPara() {
		return para;
	}

	public void setPara(double[] para) {
		this.para = para;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#getParaNum()
	 */
	@Override
	public int getParaNum() {
		// TODO Auto-generated method stub
		return para.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.ac.dicp.gp1809.util.math.curvefit.IFunction#getInitialValue(double[])
	 */
	@Override
	public double[] getInitialValue(double[] x, double[] y) {
		// TODO Auto-generated method stub
		double[] para = new double[3];

		int maxYIndex = MathTool.getMaxIndex(y);

		para[0] = y[maxYIndex];
		para[1] = x[maxYIndex];
		para[2] = (x[x.length - 1] - x[0]) / 4.0;
		
//		double dx1 = x[maxYIndex] - x[0];
//		double dx2 = x[x.length - 1] - x[maxYIndex];
//		double dx = dx1 > dx2 ? dx1 : dx2;

//		double ldy1 = Math.log(y[maxYIndex]) - Math.log(y[0]);
//		double ldy2 = Math.log(y[maxYIndex]) - Math.log(y[y.length - 1]);
//		double ldy = dx1 > dx2 ? ldy1 : ldy2;

		
//		para[2] = ldy / dx / dx;

//		System.out.println("a:\t" + para[0]);
//		System.out.println("b:\t" + para[1]);
//		System.out.println("c:\t" + para[2]);

		return para;
	}

	/**
	 * The parameter c is related to the full width at half maximum (FWHM) of
	 * the peak according to
	 */
	public double getFWHM() {
		return 2.35482 * para[2];
	}
	
	/**
	 * 
	 * @return
	 */
	public double getFWTM() {
		return 4.29193 * para[2];
	}

	public static double fx(double[] parameter, double x) {
		if (parameter[2] == 0) {
			return 0;
		}

		double y = parameter[0]
				* Math.exp(-0.5 * (x - parameter[1]) * (x - parameter[1]) / (parameter[2] * parameter[2]));

		return y;
	}
}
