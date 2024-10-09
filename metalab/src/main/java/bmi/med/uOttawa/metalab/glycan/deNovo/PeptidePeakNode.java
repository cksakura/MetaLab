package bmi.med.uOttawa.metalab.glycan.deNovo;

import bmi.med.uOttawa.metalab.spectra.Peak;

public class PeptidePeakNode {

	private int peakId;
	private Peak peak;
	private PeptidePeakNode parent;
	private PeptidePeakNode child;

	private int parentPeakId;
	private int childPeakId;

	public PeptidePeakNode(Peak peak, int peakId) {
		this.peak = peak;
		this.peakId = peakId;
	}

	public int getPeakId() {
		return peakId;
	}

	public Peak getPeak() {
		return peak;
	}

	public PeptidePeakNode getParent() {
		return parent;
	}

	public PeptidePeakNode getChild() {
		return child;
	}

	public int getParentPeakId() {
		return parentPeakId;
	}

	public int getChildPeakId() {
		return childPeakId;
	}

	public void setParent(PeptidePeakNode parent) {
		this.parent = parent;
		this.parentPeakId = parent.getPeakId();

		parent.child = this;
		parent.childPeakId = this.peakId;
	}

	public boolean isRoot() {
		boolean root = false;
		if (this.parent == null && this.child != null) {
			root = true;
		}
		return root;
	}

	public boolean isSingle() {
		boolean single = false;
		if (this.parent == null && this.child == null) {
			single = true;
		}
		return single;
	}
	
	public double getRankScore() {
		return peak.getRankScore();
	}
}
