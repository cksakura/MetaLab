/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math.curveFitting;

import java.util.ArrayList;
import java.util.Arrays;

import bmi.med.uOttawa.metalab.core.math.TwoDimArrayComparator;


/**
 * @author Kai Cheng
 *
 */
public class CurveFitting {

	private static final double alpha = -1.0; // reflection coefficient
	private static final double beta = 0.5; // contraction coefficient
	private static final double gamma = 2.0; // expansion coefficient
	private static final double root2 = 1.414214; // square root of 2
	public static final int IterFactor = 500;

	private int numPoints; // number of data points
	private int numParams; // number of parametres
	private int numVertices; // numParams+1 (includes sumLocalResiduaalsSqrd)
	private int worst; // worst current parametre estimates
	private int nextWorst; // 2nd worst current parametre estimates
	private int best; // best current parametre estimates
	private double[][] simp; // the simplex (the last element of the array at
								// each vertice is the sum of the square of the
								// residuals)
	private double[] next; // new vertex to be tested
	private int numIter; // number of iterations so far
	private int maxIter; // maximum number of iterations per restart
	private int restarts; // number of times to restart simplex after first
							// soln.
	private double maxError; // maximum error tolerance

	private double[] x;
	private double[] y;
	private IFunction function;

	public CurveFitting(double[] x, double[] y, IFunction function) {
		this.x = x;
		this.y = y;
		this.function = function;
		this.initial();
	}

	public CurveFitting(ArrayList<Double> x, ArrayList<Double> y, IFunction function) {
		double[][] data = new double[2][x.size()];
		for (int i = 0; i < x.size(); i++) {
			data[0][i] = x.get(i);
			data[1][i] = y.get(i);
		}
		Arrays.sort(data, new TwoDimArrayComparator());
		this.x = data[0];
		this.y = data[1];
		this.function = function;
		this.initial();
	}

	private void initial() {

		this.numPoints = x.length;
		this.numParams = function.getParaNum();
		this.numVertices = numParams + 1;

		this.simp = new double[numVertices][numVertices];
		this.next = new double[numVertices];
		/*
		 * double firstx = x[0]; double firsty = y[0]; double lastx =
		 * x[numPoints-1]; double lasty = y[numPoints-1]; double xmean =
		 * (firstx+lastx)/2.0; double ymean = (firsty+lasty)/2.0; double slope;
		 * if ((lastx - firstx) != 0.0) slope = (lasty - firsty)/(lastx -
		 * firstx); else slope = 1.0;
		 * 
		 * double yintercept = firsty - slope * firstx;
		 */
		maxIter = IterFactor * numParams * numParams;
		restarts = 1;
		maxError = 1e-9;

		double[] iPara = function.getInitialValue(x, y);
		for (int i = 0; i < iPara.length; i++) {
			this.simp[0][i] = iPara[i];
		}
	}

	public void fit() {

		restart(0);

		numIter = 0;
		boolean done = false;
		double[] center = new double[numParams]; // mean of simplex vertices
		while (!done) {
			numIter++;
			for (int i = 0; i < numParams; i++)
				center[i] = 0.0;
			// get mean "center" of vertices, excluding worst
			for (int i = 0; i < numVertices; i++) {
				if (i != worst)
					for (int j = 0; j < numParams; j++)
						center[j] += simp[i][j];
			}

			// Reflect worst vertex through centre
			for (int i = 0; i < numParams; i++) {
				center[i] /= numParams;
				next[i] = center[i] + alpha * (simp[worst][i] - center[i]);
			}
			calSumResiduals(next);
			// if it's better than the best...
			if (next[numParams] <= simp[best][numParams]) {
				newVertex();
				// try expanding it
				for (int i = 0; i < numParams; i++)
					next[i] = center[i] + gamma * (simp[worst][i] - center[i]);
				calSumResiduals(next);
				// if this is even better, keep it
				if (next[numParams] <= simp[worst][numParams])
					newVertex();
			}
			// else if better than the 2nd worst keep it...
			else if (next[numParams] <= simp[nextWorst][numParams]) {
				newVertex();
			}
			// else try to make positive contraction of the worst
			else {
				for (int i = 0; i < numParams; i++)
					next[i] = center[i] + beta * (simp[worst][i] - center[i]);
				calSumResiduals(next);
				// if this is better than the second worst, keep it.
				if (next[numParams] <= simp[nextWorst][numParams]) {
					newVertex();
				}
				// if all else fails, contract simplex in on best
				else {
					for (int i = 0; i < numVertices; i++) {
						if (i != best) {
							for (int j = 0; j < numVertices; j++)
								simp[i][j] = beta * (simp[i][j] + simp[best][j]);
							calSumResiduals(simp[i]);
						}
					}
				}
			}
			order();

			double rtol = 2 * Math.abs(simp[best][numParams] - simp[worst][numParams])
					/ (Math.abs(simp[best][numParams]) + Math.abs(simp[worst][numParams]) + 0.0000000001);

			if (numIter >= maxIter) {
				done = true;
			} else if (rtol < maxError) {

				restarts--;
				if (restarts < 0) {
					done = true;
				} else {
					restart(best);
				}
			}
		}
	}

	/** Restart the simplex at the nth vertex */
	private void restart(int n) {
		// Copy nth vertice of simplex to first vertice
		for (int i = 0; i < numParams; i++) {
			simp[0][i] = simp[n][i];
		}
		calSumResiduals(simp[0]); // Get sum of residuals^2 for first vertex
		double[] step = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			step[i] = simp[0][i] / 2.0; // Step half the parametre value
			if (step[i] == 0.0) // We can't have them all the same or we're
								// going nowhere
				step[i] = 0.01;
		}
		// Some kind of factor for generating new vertices
		double[] p = new double[numParams];
		double[] q = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			p[i] = step[i] * (Math.sqrt(numVertices) + numParams - 1.0) / (numParams * root2);
			q[i] = step[i] * (Math.sqrt(numVertices) - 1.0) / (numParams * root2);
		}
		// Create the other simplex vertices by modifing previous one.
		for (int i = 1; i < numVertices; i++) {
			for (int j = 0; j < numParams; j++) {
				simp[i][j] = simp[i - 1][j] + q[j];
			}
			simp[i][i - 1] = simp[i][i - 1] + p[i - 1];
			calSumResiduals(simp[i]);
		}
		// Initialise current lowest/highest parametre estimates to simplex 1
		best = 0;
		worst = 0;
		nextWorst = 0;
		order();
	}

	/**
	 * Get a measure of "goodness of fit" where 1.0 is best.
	 *
	 */
	public double getFitGoodness() {
		double sumY = 0.0;
		for (int i = 0; i < numPoints; i++)
			sumY += y[i];

		double mean = sumY / numPoints;
		double sumMeanDiffSqr = 0.0;
		int degreesOfFreedom = numPoints - numParams;
		double fitGoodness = 0.0;
		for (int i = 0; i < numPoints; i++) {
			sumMeanDiffSqr += Math.pow(y[i] - mean, 2);
		}
		if (sumMeanDiffSqr > 0.0 && degreesOfFreedom != 0)
			fitGoodness = 1.0 - (getSumResidualSqr() / degreesOfFreedom) * ((numParams) / sumMeanDiffSqr);

//		 System.out.println("ssss\t"+sumMeanDiffSqr+"\t"+getSumResidualSqr());

		return fitGoodness;
	}

	public double getR2() {
		double sumY = 0.0;
		for (int i = 0; i < numPoints; i++)
			sumY += y[i];

		double mean = sumY / numPoints;
		double sumMeanDiffSqr = 0.0;
		for (int i = 0; i < numPoints; i++) {
			sumMeanDiffSqr += Math.pow(y[i] - mean, 2);
		}

		double sumResidualSqr = getSumResidualSqr();
		double r2;
		if (sumMeanDiffSqr == 0.0) {
			r2 = 1.0;
		} else {
			r2 = (sumMeanDiffSqr - sumResidualSqr) / sumMeanDiffSqr;
		}

		return r2;
	}

	/*
	 * Last "parametre" at each vertex of simplex is sum of residuals for the
	 * curve described by that vertex
	 */
	public double getSumResidualSqr() {
		order();
		double sumResidualSqr = simp[best][numParams];
		return sumResidualSqr;
	}

	/**
	 * SD = sqrt(sum of residuals squared / number of params+1)
	 */
	public double getSD() {
		double sd = Math.sqrt(getSumResidualSqr() / numVertices);
		return sd;
	}

	/** Get the set of parameter values from the best corner of the simplex */
	public double[] getBestParams() {
		order();
		double[] para = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			para[i] = simp[best][i];
		}
		return para;
	}

	/** Returns residuals array , ie. differences between data and curve */
	public double[] getResiduals() {
		double[] params = getBestParams();
		double[] residuals = new double[numPoints];
		this.function.setPara(params);
		for (int i = 0; i < numPoints; i++) {
			residuals[i] = y[i] - function.fx(x[i]);
		}

		return residuals;
	}

	private double calSumResiduals(double[] vertice) {
		vertice[numParams] = 0.0;
		double[] para = new double[numParams];
		for (int i = 0; i < numParams; i++) {
			para[i] = vertice[i];
		}
		function.setPara(para);
		for (int i = 0; i < numPoints; i++) {
			vertice[numParams] = vertice[numParams] + Math.pow(y[i] - function.fx(x[i]), 2);
		}
//		 System.out.println("267\t"+vertice[numParams]);
		return vertice[numParams];
	}

	/** Keep the "next" vertex */
	private void newVertex() {
		for (int i = 0; i < numVertices; i++)
			simp[worst][i] = next[i];
	}

	/** Find the worst, nextWorst and best current set of parameter estimates */
	private void order() {
		for (int i = 0; i < numVertices; i++) {
			if (simp[i][numParams] < simp[best][numParams])
				best = i;
			if (simp[i][numParams] > simp[worst][numParams])
				worst = i;
		}
		nextWorst = best;
		for (int i = 0; i < numVertices; i++) {
			if (i != worst) {
				if (simp[i][numParams] > simp[nextWorst][numParams])
					nextWorst = i;
			}
		}
	}

	/** Get number of iterations performed */
	public int getIterations() {
		return numIter;
	}

	/** Get maximum number of iterations allowed */
	public int getMaxIterations() {
		return maxIter;
	}

	/** Set maximum number of iterations allowed */
	public void setMaxIterations(int x) {
		maxIter = x;
	}

	/** Get number of simplex restarts to do */
	public int getRestarts() {
		return restarts;
	}

	/** Set number of simplex restarts to do */
	public void setRestarts(int restarts) {
		this.restarts = restarts;
	}
}
