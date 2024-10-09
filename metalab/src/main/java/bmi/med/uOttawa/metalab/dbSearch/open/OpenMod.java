/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open;

import java.util.ArrayList;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.mod.UmModification;

/**
 * @author Kai Cheng
 *
 */
public class OpenMod {

	private int id;
	private UmModification[] ummod;
	private double[] gaussianParameters;
	private double expModMass;
	private int monoId;
	private boolean isMonoIsotope;
	
	private double bscore;
	private double yscore;
	private double bModScore;
	private double yModScore;
	private double neutralLossMass;
	private double neutralLossScore;
	private double neutralLossRatio;

	public OpenMod(int id, double[] gaussianParameters, double expModMass, int monoId) {
		this.id = id;
		this.gaussianParameters = gaussianParameters;
		this.expModMass = expModMass;
		this.monoId = monoId;
	}

	public OpenMod(int id, double[] gaussianParameters, double expModMass, int monoId, UmModification... ummod) {
		this.id = id;
		this.gaussianParameters = gaussianParameters;
		this.expModMass = expModMass;
		this.monoId = monoId;
		this.ummod = ummod;
	}

	public OpenMod(int id, double[] gaussianParameters, double expModMass, int monoId, double bscore, double yscore,
			double bModScore, double yModScore, double neutralLossMass, double neutralLossScore,
			double neutralLossRatio) {

		this.id = id;
		this.gaussianParameters = gaussianParameters;
		this.expModMass = expModMass;
		this.monoId = monoId;
		this.bscore = bscore;
		this.yscore = yscore;
		this.bModScore = bModScore;
		this.yModScore = yModScore;
		this.neutralLossMass = neutralLossMass;
		this.neutralLossScore = neutralLossScore;
	}

	public OpenMod(int id, double[] gaussianParameters, double expModMass, int monoId, double bscore, double yscore,
			double bModScore, double yModScore, double neutralLossMass, double neutralLossScore,
			double neutralLossRatio, UmModification... ummod) {

		this.id = id;
		this.gaussianParameters = gaussianParameters;
		this.expModMass = expModMass;
		this.monoId = monoId;
		this.bscore = bscore;
		this.yscore = yscore;
		this.bModScore = bModScore;
		this.yModScore = yModScore;
		this.neutralLossMass = neutralLossMass;
		this.neutralLossScore = neutralLossScore;
		this.ummod = ummod;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setExpModMass(double expModMass) {
		this.expModMass = expModMass;
	}

	public double getExpModMass() {
		return expModMass;
	}

	public double[] getGaussianParameters() {
		return gaussianParameters;
	}

	public void setGaussianParameters(double[] gaussianParameters) {
		this.gaussianParameters = gaussianParameters;
	}

	public UmModification[] getUmmod() {
		return ummod;
	}

	public void setUmmod(UmModification... ummod) {
		this.ummod = ummod;
	}

	public HashMap<String, ArrayList<String>> getSiteNameMap() {
		if (ummod == null) {
			return null;
		}

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (UmModification um : ummod) {
			String[] sps = um.getSpecificity();
			for (String sp : sps) {
				if (map.containsKey(sp)) {
					map.get(sp).add(um.getName());
				} else {
					ArrayList<String> list = new ArrayList<String>();
					list.add(um.getName());
					map.put(sp, list);
				}
			}
		}
		return map;
	}

	public int getMonoId() {
		return monoId;
	}

	public void setMonoId(int monoId) {
		this.monoId = monoId;
	}

	public boolean isMonoIsotope() {
		return isMonoIsotope;
	}

	public void setMonoIsotope(boolean isUse) {
		this.isMonoIsotope = isUse;
	}

	public double getBscore() {
		return bscore;
	}

	public void setBscore(double bscore) {
		this.bscore = bscore;
	}

	public double getYscore() {
		return yscore;
	}

	public void setYscore(double yscore) {
		this.yscore = yscore;
	}

	public double getbModScore() {
		return bModScore;
	}

	public void setbModScore(double bModScore) {
		this.bModScore = bModScore;
	}

	public double getyModScore() {
		return yModScore;
	}

	public void setyModScore(double yModScore) {
		this.yModScore = yModScore;
	}

	public double getNeutralLossMass() {
		return neutralLossMass;
	}

	public void setNeutralLossMass(double neutralLossMass) {
		this.neutralLossMass = neutralLossMass;
	}

	public double getNeutralLossScore() {
		return neutralLossScore;
	}

	public void setNeutralLossScore(double neutralLossScore) {
		this.neutralLossScore = neutralLossScore;
	}

	public double getNeutralLossRatio() {
		return neutralLossRatio;
	}

	public void setNeutralLossRatio(double neutralLossRatio) {
		this.neutralLossRatio = neutralLossRatio;
	}

	public String getName() {
		if (this.ummod == null) {
			return "Unknown";
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ummod.length; i++) {
				sb.append(ummod[i].getName()).append(";");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			return sb.toString();
		}
	}

	/*
	 * public double getDeltaMass() { if (ummod != null) { return expModMass -
	 * ummod.getMono_mass(); } else { return -1; } }
	 */
}
