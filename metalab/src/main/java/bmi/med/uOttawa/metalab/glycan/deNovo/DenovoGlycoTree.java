package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.spectra.Peak;


public class DenovoGlycoTree {

	private GlycoPeakNode[] pns;
	
	private int[] glycanComposition;
	private HashMap<Integer, GlycoPeakNode> map;
	private double initialYMass;
	private double glycoMass;
	private int charge;
	private double score;

	private int[] mainComposition;
//	private int lastGlycanCount;
	private int lastPeakId;
	
	/**
	 * For the released glycan spectra, the root peak of the glyco tree will be an oxonium ion, this is the composition of the oxonium ion
	 */
	private int[] initialComposition;

	public DenovoGlycoTree(int typeCount, int charge, GlycoPeakNode[] pns) {
		this.pns = pns;
		this.map = new HashMap<Integer, GlycoPeakNode>();
		this.glycanComposition = new int[typeCount];
		this.mainComposition = new int[typeCount];
		this.charge = charge;
	}

	public void addNode(GlycoPeakNode node) {
		this.map.put(node.getPeakId(), node);
		if (node.isRoot()) {
			this.initialYMass = node.getMw();
			this.lastPeakId = node.getPeakId();
			this.mainComposition = new int[node.getComposition().length];
			System.arraycopy(node.getComposition(), 0, this.mainComposition, 0, node.getComposition().length);
		} else {
			int[] nodeComp = node.getComposition();
			for (int i = 0; i < nodeComp.length; i++) {
				if (nodeComp[i] > mainComposition[i]) {
					mainComposition[i] = nodeComp[i];
				}
			}

			this.lastPeakId = node.getPeakId();
		}

		double rankScore = node.getPeak().getRankScore();
		double localRankScore = node.getPeak().getLocalRankScore();
		this.score += rankScore * localRankScore;
	}

	public GlycoPeakNode[] getAllNodes() {
		return pns;
	}
	
	public int getSize() {
		return map.size();
	}

	public HashMap<Integer, GlycoPeakNode> getMap() {
		return map;
	}

	public int[] getGlycanComposition() {
		return glycanComposition;
	}

	public double getGlycoMass() {
		return this.glycoMass;
	}

	public double getInitialYMass() {
		return initialYMass;
	}

	public int[] getInitialComposition() {
		return initialComposition;
	}

	public void setInitialComposition(int[] initialComposition) {
		this.initialComposition = initialComposition;
	}

	public double getGlycopeptideMass() {
		return this.initialYMass + this.glycoMass;
	}

	public double getLastGPMass(double[] glycoMass) {
		double mass = this.initialYMass;
		for (int i = 0; i < glycoMass.length; i++) {
			mass += glycoMass[i] * this.mainComposition[i];
		}
		return mass;
	}

	public void setMainStem() {
		Integer[] ids = this.map.keySet().toArray(new Integer[map.size()]);
		Arrays.sort(ids);
		setMainTree(map.get(ids[ids.length - 1]));
	}

	private void setMainTree(GlycoPeakNode node) {
		node.setMainTree(true);
		if (node.getParent() != null) {
			setMainTree(node.getParent());
		}
	}

	public int[] getLastComposition() {
		return this.mainComposition;
	}

	public int getCharge() {
		return this.charge;
	}

	public double[] getPeakMzList() {
		double[] massList = new double[map.size()];
		int id = 0;
		for (GlycoPeakNode node : map.values()) {
			massList[id++] = node.getPeakMz();
		}
		Arrays.sort(massList);
		return massList;
	}

	public double getAveIntensity() {
		if (map.size() == 0)
			return 0;
		double total = 0;
		for (Integer peakId : map.keySet()) {
			total += map.get(peakId).getPeakIntensity();
		}
		return total / (double) map.size();
	}

	public double getTotalScore() {
		return this.score;
	}

	public Peak[] getPeakList() {
		Peak[] peaks = new Peak[this.map.size()];
		int id = 0;
		for (GlycoPeakNode node : this.map.values()) {
			peaks[id++] = node.getPeak();
		}
		return peaks;
	}

	public static class RevTreeScoreComparator implements Comparator<DenovoGlycoTree> {

		@Override
		public int compare(DenovoGlycoTree arg0, DenovoGlycoTree arg1) {
			// TODO Auto-generated method stub
			if (arg0.score < arg1.score)
				return 1;
			if (arg0.score > arg1.score)
				return -1;
			return 0;
		}

	}

}
