/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * @author Kai Cheng
 *
 */
public class DataListClusterable implements Clusterable {

	private int id;
	private double[] point;

	public DataListClusterable(int id, double[] point) {
		this.id = id;
		this.point = point;
	}

	public DataListClusterable(int id, Double[] point) {
		this.id = id;
		this.point = new double[point.length];
		for (int i = 0; i < point.length; i++) {
			this.point[i] = point[i];
		}
	}

	public int getId() {
		return id;
	}

	public double[] getPoint() {
		// TODO Auto-generated method stub
		return point;
	}
}
