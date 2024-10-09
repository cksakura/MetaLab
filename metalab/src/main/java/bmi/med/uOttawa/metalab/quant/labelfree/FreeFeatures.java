/**
 * 
 */
package bmi.med.uOttawa.metalab.quant.labelfree;

import java.util.HashMap;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * @author Kai Cheng
 *
 */
public class FreeFeatures {

	protected MetaPeptide peptide;
	private HashMap<Integer, FreeFeature> feaMap;
	private double pepmass;
	private int charge;
	private double[] intenlist;
	private double intensity;

	public FreeFeatures(int charge) {
		// TODO Auto-generated constructor stub
		this.charge = charge;
		this.feaMap = new HashMap<Integer, FreeFeature>();
	}

	public FreeFeatures(FreeFeatures feas) {
		// TODO Auto-generated constructor stub
		this.charge = feas.charge;
		this.feaMap = feas.feaMap;
		this.intenlist = feas.intenlist;
		this.intensity = feas.intensity;
	}

	public FreeFeatures(int charge, HashMap<Integer, FreeFeature> feaMap) {
		// TODO Auto-generated constructor stub
		this.charge = charge;
		this.feaMap = feaMap;
		this.setInfo();
	}

	/**
	 * @param f
	 */
	public FreeFeatures(FreeFeature fea) {
		// TODO Auto-generated constructor stub
		this.charge = fea.getCharge();
		this.feaMap = new HashMap<Integer, FreeFeature>();
		this.feaMap.put(fea.getScanNum(), fea);
	}

	/**
	 * @param f
	 */
	public void addFeature(FreeFeature fea) {
		// TODO Auto-generated method stub
		feaMap.put(fea.getScanNum(), fea);
	}

	/**
	 * 
	 */
	public void setInfo() {
		// TODO Auto-generated method stub
		this.intenlist = new double[feaMap.size()];
		Iterator<Integer> it = feaMap.keySet().iterator();
		int id = 0;
		while (it.hasNext()) {
			Integer sn = it.next();
			FreeFeature ff = feaMap.get(sn);
			this.intenlist[id++] = ff.getIntensity();
			this.intensity += ff.getIntensity();
		}
	}

	/**
	 * @return
	 */
	public HashMap<Integer, FreeFeature> getFeaMap() {
		// TODO Auto-generated method stub
		return feaMap;
	}

	/**
	 * @return
	 */
	public double getPepMass() {
		// TODO Auto-generated method stub
		return pepmass;
	}

	/**
	 * @param mr
	 */
	public void setPepMass(double mr) {
		// TODO Auto-generated method stub
		this.pepmass = mr;
	}

	public int getCharge() {
		return charge;
	}

	/**
	 * @return
	 */
	public double[] getIntenList() {
		// TODO Auto-generated method stub
		return intenlist;
	}

	public int getLength() {
		return this.feaMap.size();
	}

	public double getInten() {
		return intensity;
	}

	public void setPeptide(MetaPeptide peptide) {
		this.peptide = peptide;
	}

	public MetaPeptide getPeptide() {
		return peptide;
	}

	public String getSequence() {
		return peptide.getSequence();
	}

	/**
	 * @param length
	 * @return
	 */
	public double getTopNInten(int length) {
		// TODO Auto-generated method stub
		return MathTool.getTopnTotal(intenlist, length);
	}

	/**
	 * @param normal
	 */
	public void setNormalRatio(double[] normal) {
		// TODO Auto-generated method stub

	}

}
