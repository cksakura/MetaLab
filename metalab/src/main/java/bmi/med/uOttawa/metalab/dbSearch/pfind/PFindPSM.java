/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

/**
 * @author Kai Cheng
 *
 */
public class PFindPSM {

	private String fileName;
	private int scan;
	private double expMH;
	private int charge;
	private double qValue;
	private String sequence;
	private double calMH;
	private double massShift;
	private double rawScore;
	private double finalScore;
	private String mod;
	private PostTransModification ptm;
	private int specificity;
	private String[] proteins;
	private boolean isTarget;
	private int miss;
	private double fragMassShift;
	private double rt;

	public PFindPSM(String fileName, int scan, double expMH, int charge, double qValue, String sequence, double calMH,
			double massShift, double rawScore, double finalScore, String mod, PostTransModification ptm,
			int specificity, String[] proteins, boolean isTarget, int miss, double fragMassShift) {
		this.fileName = fileName;
		this.scan = scan;
		this.expMH = expMH;
		this.charge = charge;
		this.qValue = qValue;
		this.sequence = sequence;
		this.calMH = calMH;
		this.massShift = massShift;
		this.rawScore = rawScore;
		this.finalScore = finalScore;
		this.mod = mod;
		this.ptm = ptm;
		this.specificity = specificity;
		this.proteins = proteins;
		this.isTarget = isTarget;
		this.miss = miss;
		this.fragMassShift = fragMassShift;
	}

	public String getFileName() {
		return fileName;
	}

	public int getScan() {
		return scan;
	}

	public double getExpMH() {
		return expMH;
	}

	public int getCharge() {
		return charge;
	}

	public double getqValue() {
		return qValue;
	}

	public String getSequence() {
		return sequence;
	}

	public double getCalMH() {
		return calMH;
	}

	public double getMassShift() {
		return massShift;
	}

	public double getRawScore() {
		return rawScore;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public String getMod() {
		return mod;
	}

	public PostTransModification getPtm() {
		return ptm;
	}

	public int getSpecificity() {
		return specificity;
	}
	
	public void setProteins(String[] proteins) {
		this.proteins = proteins;
	}

	public String[] getProteins() {
		return proteins;
	}

	public String getProtein() {
		StringBuilder sb = new StringBuilder();
		for (String pro : proteins) {
			sb.append(pro).append(";");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return "";
		}
	}

	public boolean isTarget() {
		return isTarget;
	}

	public int getMiss() {
		return miss;
	}

	public double getFragMassShift() {
		return fragMassShift;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public String getModseq() {

		if (mod.trim().length() == 0) {
			return sequence;
		}

		HashMap<Integer, String> modmap = new HashMap<Integer, String>();
		StringBuilder sb = new StringBuilder();
		String[] mods = mod.split(";");
		for (int i = 0; i < mods.length; i++) {
			int id = mods[i].indexOf(",");
			if (id > 0) {
				int loc = Integer.parseInt(mods[i].substring(0, id));
				String name = mods[i].substring(id + 1, mods[i].lastIndexOf("["));
				modmap.put(loc, name);
			}
		}

		for (int i = 0; i < sequence.length(); i++) {
			if (modmap.containsKey(i)) {
				sb.append("(").append(modmap.get(i)).append(")");
			}
			sb.append(sequence.charAt(i));
		}
		if (modmap.containsKey(sequence.length())) {
			sb.append("(").append(modmap.get(sequence.length())).append(")");
		}
		if (modmap.containsKey(sequence.length() + 1)) {
			sb.append("(").append(modmap.get(sequence.length() + 1)).append(")");
		}
		return sb.toString();
	}

	/**
	 * No isobaric tags
	 * 
	 * @return
	 */
	public String getModseqNoIsoTags() {

		if (mod.trim().length() == 0) {
			return sequence;
		}

		HashMap<Integer, String> modmap = new HashMap<Integer, String>();
		StringBuilder sb = new StringBuilder();
		String[] mods = mod.split(";");
		for (int i = 0; i < mods.length; i++) {
			int id = mods[i].indexOf(",");
			if (id > 0) {
				int loc = Integer.parseInt(mods[i].substring(0, id));
				String name = mods[i].substring(id + 1, mods[i].indexOf("["));

				if (!name.contains("plex")) {
					modmap.put(loc, name);
				}
			}
		}

		for (int i = 0; i < sequence.length(); i++) {
			if (modmap.containsKey(i)) {
				sb.append("[").append(modmap.get(i)).append("]");
			}
			sb.append(sequence.charAt(i));
		}
		if (modmap.containsKey(sequence.length())) {
			sb.append("[").append(modmap.get(sequence.length())).append("]");
		}
		if (modmap.containsKey(sequence.length() + 1)) {
			sb.append("[").append(modmap.get(sequence.length() + 1)).append("]");
		}
		return sb.toString();
	}
}
