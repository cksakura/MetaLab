package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.util.Comparator;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.spectra.Peak;

public class GlycoPeakNode {

	private int peakId;
	// Yn
	private int sid;
	private Peak peak;
	private GlycoPeakNode parent;
	private GlycoPeakNode[] children;
	private int[] composition;
	private boolean mainTree;
	private boolean terminal;
	private boolean oxonium;

	private double usedMz = 0;
	/**
	 * previous isotope id
	 */
	private int monoId = 0;

	/**
	 * next isotope id
	 */
	private int isoId = 0;

	private int charge;
	private double deltaMz;
	private int glycanId;
	private double glycanMass;

	private int parentPeakId;
	private int[] childrenIds;

	public GlycoPeakNode(Peak peak, int peakId, int typeCount) {
		this.peak = peak;
		this.peakId = peakId;
		this.children = new GlycoPeakNode[typeCount];
		this.composition = new int[typeCount];
		this.terminal = true;
		this.childrenIds = new int[typeCount];
	}

	public GlycoPeakNode(Peak peak, int peakId, int parentPeakId, int[] childrenIds) {
		this.peak = peak;
		this.peakId = peakId;
		this.children = new GlycoPeakNode[childrenIds.length];
		this.terminal = true;
		this.parentPeakId = parentPeakId;
		this.childrenIds = childrenIds;
	}

	public GlycoPeakNode(Peak peak, int peakId, int sid, int parentPeakId, int[] childrenIds) {
		this.peak = peak;
		this.peakId = peakId;
		this.sid = sid;
		this.children = new GlycoPeakNode[childrenIds.length];
		this.terminal = true;
		this.parentPeakId = parentPeakId;
		this.childrenIds = childrenIds;
	}

	public int getPeakId() {
		return peakId;
	}

	public void setPeakId(int peakId) {
		this.peakId = peakId;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public GlycoPeakNode getParent() {
		return parent;
	}

	public void setParent(GlycoPeakNode parent) {
		this.parent = parent;
		this.parentPeakId = parent.peakId;

		for (int i = 0; i < parent.childrenIds.length; i++) {
			if (parent.childrenIds[i] == this.peakId) {
				this.glycanId = i;
				parent.children[i] = this;
				parent.terminal = false;
			}
		}
	}

	public void setParent(GlycoPeakNode parent, int glycanId) {
		this.parent = parent;
		this.parentPeakId = parent.peakId;
		this.glycanId = glycanId;
		System.arraycopy(parent.composition, 0, this.composition, 0, this.composition.length);
		this.composition[glycanId]++;

		parent.children[glycanId] = this;
		parent.childrenIds[glycanId] = this.peakId;
		parent.terminal = false;
	}

	public void setComposition(int[] composition) {
		this.composition = composition;
	}

	public int[] getComposition() {
		return composition;
	}

	public Peak getPeak() {
		return peak;
	}

	public GlycoPeakNode[] getChildren() {
		return children;
	}

	/*
	 * public void addChild(GlycoPeakNode node, int glycanId) {
	 * this.children[glycanId] = node; this.terminal = false; }
	 */

	public int getGlycanId() {
		return glycanId;
	}

	public void setGlycanId(int glycanId) {
		this.glycanId = glycanId;
	}

	public double getGlycanMass() {
		return glycanMass;
	}

	public void setGlycanMass(double glycanMass) {
		this.glycanMass = glycanMass;
	}

	public boolean isMainTree() {
		return mainTree;
	}

	public void setMainTree(boolean mainTree) {
		this.mainTree = mainTree;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public double getDeltaMz() {
		return deltaMz;
	}

	public void setDeltaMz(double deltaMz) {
		this.deltaMz = deltaMz;
	}

	public int getParentPeakId() {
		return parentPeakId;
	}

	public int[] getChildrenIds() {
		return childrenIds;
	}

	public void setChildren(GlycoPeakNode[] children) {
		this.children = children;
	}

	public void setChildrenIds(int[] childrenIds) {
		this.childrenIds = childrenIds;
	}

	public double getPeakMz() {
		return peak.getMz();
	}

	public double getPeakIntensity() {
		return peak.getIntensity();
	}

	public double getSingleScore() {
		return peak.getRankScore();
//		return peak.getLocalRankScore() * peak.getRankScore();
	}

	public boolean isRoot() {
		boolean root = false;
		if (this.parent == null && this.children.length > 0) {
			root = true;
		}
		return root;
	}

	public boolean isSingle() {
		boolean single = false;
		if (this.parent == null && this.children.length == 0) {
			single = true;
		}
		return single;
	}

	public boolean isTerminal() {
		return terminal;
	}

	/*
	 * public boolean hasChild() { for (int i = 0; i < children.length; i++) { if
	 * (children[i] != null) { return true; } } return false; }
	 */

	public boolean isOxonium() {
		return oxonium;
	}

	/**
	 * 
	 * @return previous isotope id
	 */
	public int getMonoId() {
		return monoId;
	}

	/**
	 * set previous isotope id
	 * 
	 * @param monoId
	 */
	public void setMonoId(int monoId) {
		this.monoId = monoId;
	}

	/**
	 * 
	 * @return next isotope id
	 */
	public int getIsoId() {
		return isoId;
	}

	/**
	 * set next isotope id
	 * 
	 * @param isoId
	 */
	public void setIsoId(int isoId) {
		this.isoId = isoId;
	}

	/**
	 * 
	 * @return mz for the use of find GlycoPeakNode
	 */
	/*
	 * public double getUsedMz() { if (usedMz == 0) { return getPeakMz(); } return
	 * usedMz; }
	 */

	/**
	 * set mz for the use of find GlycoPeakNode
	 * 
	 * @param usedMz
	 */
	/*
	 * public void setUsedMz(double usedMz) { this.usedMz = usedMz; }
	 */

	public void setOxonium(boolean oxonium) {
		this.oxonium = oxonium;
	}

	public double getMw() {
//		double mw = (getUsedMz() - AminoAcidProperty.PROTON_W) * charge;
		double mw = (this.peak.getMz() - AminoAcidProperty.PROTON_W) * charge;
		return mw;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(peakId).append("$");
		sb.append(charge).append("$");
		if (oxonium) {
			sb.append("1$");
		} else {
			sb.append("0$");
		}
		sb.append(sid).append("$");
		if (sid == 0) {
			sb.append("-1$");
		} else {
			sb.append(parent.peakId).append("$");
		}
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null) {
				sb.append(children[i].peakId).append("_");
			} else {
				sb.append("-1").append("_");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("$");
		for (int i = 0; i < composition.length; i++) {
			sb.append(composition[i]).append("_");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static GlycoPeakNode parse(String line, Peak[] peaks) {
		String[] cs = line.split("\\$");
		int peakId = Integer.parseInt(cs[0]);
		int charge = Integer.parseInt(cs[1]);
		boolean oxonium = cs[2].equals("1");
		int sid = Integer.parseInt(cs[3]);
		int parentId = Integer.parseInt(cs[4]);

		String[] cs1 = cs[5].split("_");
		int[] childrenId = new int[cs1.length];
		for (int i = 0; i < childrenId.length; i++) {
			childrenId[i] = Integer.parseInt(cs1[i]);
		}
		String[] cs2 = cs[6].split("_");
		int[] composition = new int[cs2.length];
		for (int i = 0; i < composition.length; i++) {
			composition[i] = Integer.parseInt(cs2[i]);
		}
		GlycoPeakNode node = new GlycoPeakNode(peaks[peakId], peakId, sid, parentId, childrenId);
		node.oxonium = oxonium;
		node.charge = charge;
		node.composition = composition;

		return node;
	}

	public static class MwComparator implements Comparator<GlycoPeakNode> {

		@Override
		public int compare(GlycoPeakNode arg0, GlycoPeakNode arg1) {
			// TODO Auto-generated method stub
			if (arg0.getMw() < arg1.getMw())
				return -1;
			if (arg0.getMw() > arg1.getMw())
				return 1;
			return 0;
		}

	}
}
