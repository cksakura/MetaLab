/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math.curveFitting;

/**
 * @author Kai Cheng
 *
 */
public class SLineFunction implements IFunction {

	private double [] para;
	
	public SLineFunction(){
		this.para = new double[]{1.0, 1.0};
	}
	
	public SLineFunction(double [] para){
		this.para = para;
	}
	
	/* (non-Javadoc)
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#fx(double)
	 */
	@Override
	public double fx(double x) {
		// TODO Auto-generated method stub
		double y = para[0]*x+para[1];
		return y;
	}

	/* (non-Javadoc)
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#getInitialValue(double[], double[])
	 */
	@Override
	public double[] getInitialValue(double[] x, double[] y) {
		// TODO Auto-generated method stub
		int num = x.length;
		double firstx = x[0];
        double firsty = y[0];
        double lastx = x[num-1];
        double lasty = y[num-1];
        double slope;
        if ((lastx - firstx) != 0.0)
            slope = (lasty - firsty)/(lastx - firstx);
        else
            slope = 1.0;
        double yintercept = firsty - slope * firstx;
        
        double [] para = new double[2];
        para[0] = slope;
        para[1] = yintercept;

		return para;
	}

	/* (non-Javadoc)
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#getPara()
	 */
	@Override
	public double[] getPara() {
		// TODO Auto-generated method stub
		return para;
	}

	/* (non-Javadoc)
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#getParaNum()
	 */
	@Override
	public int getParaNum() {
		// TODO Auto-generated method stub
		return para.length;
	}

	/* (non-Javadoc)
	 * @see cn.ac.dicp.gp1809.util.math.curvefit.IFunction#setPara(double[])
	 */
	@Override
	public void setPara(double[] para) {
		// TODO Auto-generated method stub
		this.para = para;
	}
}
