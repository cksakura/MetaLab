/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.psm;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

/**
 * @author Kai Cheng
 *
 */
public class PeptideModification implements Comparable<PeptideModification> {

	private PostTransModification ptm;
	private int loc;

	public PeptideModification(PostTransModification ptm, int loc) {
		this.ptm = ptm;
		this.loc = loc;
	}

	public PostTransModification getPtm() {
		return ptm;
	}

	public void setPtm(PostTransModification ptm) {
		this.ptm = ptm;
	}

	public double getMass() {
		return ptm.getMass();
	}

	public String getName() {
		return ptm.getName();
	}

	public int getLoc() {
		return loc;
	}

	public void setLoc(int loc) {
		this.loc = loc;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(loc).append("_");
		sb.append(ptm.getName()).append("_");
		sb.append(ptm.getMass()).append("_");
		sb.append(ptm.getNeutralLoss()).append("_");
		sb.append(ptm.getSite()).append("_");
		sb.append(ptm.isVariable());
		return sb.toString();
	}

	public static PeptideModification parse(String line) {
		String[] cs = line.split("_");
		int loc = Integer.parseInt(cs[0]);
		double mass = Double.parseDouble(cs[2]);
		double neutralLoss = Double.parseDouble(cs[3]);
		boolean variable = Boolean.parseBoolean(cs[5]);
		PeptideModification pm = new PeptideModification(
				new PostTransModification(mass, neutralLoss, cs[1], cs[4], variable), loc);
		return pm;
	}

	@Override
	public int compareTo(PeptideModification pm) {
		// TODO Auto-generated method stub
		if (this.loc < pm.loc) {
			return -1;
		} else if (this.loc > pm.loc) {
			return 1;
		}
		return 0;
	}

}
