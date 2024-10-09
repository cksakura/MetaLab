/**
 * 
 */
package bmi.med.uOttawa.metalab.core.enzyme;

import java.util.ArrayList;

/**
 * @author Kai Cheng
 *
 */
public enum Enzyme {

	Trypsin("Trypsin", true, "KR", "P"),

	TrypsinP("Trypsin/P", true, "KR", ""),

	LysC("LysC", true, "K", "P"),

	LysCP("LysC/P", true, "K", ""),

	LysN("LysN", false, "K", ""),

	ArgC("ArgC", true, "R", ""),

	AspC("AspC", true, "D", ""),

	AspN("AspN", false, "D", ""),

	GluC("GluC", true, "E", ""),

	GlyN("GlyN", false, "K", ""),

	ChymoTrypsin("ChymoTrypsin", false, "FWY", ""),

	ChymoTrypsinPlus("ChymoTrypsin+", false, "LMFWY", "");

	private String name;
	private boolean sense_C;
	private String cleaveAt;
	private String notCleaveAt;
	private char[] cleaves;
	private char[] restricts;

	Enzyme(String enzymeName, boolean sense_C, String cleaveAt, String notCleaveAt) {

		this.name = enzymeName;
		this.sense_C = sense_C;
		this.cleaveAt = cleaveAt;
		this.notCleaveAt = notCleaveAt;
		this.cleaves = cleaveAt.toCharArray();
		this.restricts = notCleaveAt.toCharArray();
	}

	public String getName() {
		return name;
	}

	public boolean isSense_C() {
		return sense_C;
	}

	public String getCleaveAt() {
		return cleaveAt;
	}

	public String getNotCleaveAt() {
		return notCleaveAt;
	}

	public char[] getCleaves() {
		return cleaves;
	}

	public char[] getRestricts() {
		return restricts;
	}

	public Object[] toTableRawObjects() {
		Object[] objs = new Object[5];
		objs[0] = false;
		objs[1] = name;
		objs[2] = cleaveAt;
		objs[3] = notCleaveAt;
		objs[4] = sense_C;
		return objs;
	}

	public String getTandemFormat() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(this.cleaveAt).append("]").append("|").append("{").append(this.notCleaveAt).append("}");
		return sb.toString();
	}

	private String[] getPeptides(String sequence, int[] loc, int miss, int leastLength) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i <= miss; i++) {
			if (i < loc.length && loc[i] + 1 >= leastLength) {
				String pep = sequence.substring(0, loc[i] + 1);
				list.add(pep);
			}
		}

		for (int i = 0; i < loc.length - 1; i++) {
			for (int j = i + 1; j <= i + 1 + miss; j++) {
				if (j == loc.length)
					break;

				if (loc[j] - loc[i] >= leastLength) {
					String pep = sequence.substring(loc[i] + 1, loc[j] + 1);
					list.add(pep);
				}
			}
		}

		String[] peps = list.toArray(new String[list.size()]);
		return peps;
	}

	/**
	 * Get the cleavage sites
	 * 
	 * @param sequence
	 * @return
	 */
	private int[] getLoc(String sequence) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		char[] cs = sequence.toCharArray();
		for (int i = 0; i < cs.length - 1; i++) {
			char ci0 = cs[i];
			char ci1 = cs[i + 1];

			boolean cleave = false;
			for (int j = 0; j < this.cleaves.length; j++) {
				if (ci0 == this.cleaves[j]) {
					cleave = true;
					break;
				}
			}

			boolean res = false;
			for (int j = 0; j < this.restricts.length; j++) {
				if (ci1 == this.restricts[j]) {
					res = true;
					break;
				}
			}

			if (cleave && !res) {
				list.add(i);
			}
		}
		list.add(cs.length - 1);

		int[] arrays = new int[list.size()];
		for (int i = 0; i < arrays.length; i++) {
			arrays[i] = list.get(i);
		}
		return arrays;
	}

	public String[] digest(String sequence, int miss) {
		int[] locs = this.getLoc(sequence);
		return getPeptides(sequence, locs, miss, 6);
	}
	
	public String[] digest(String sequence, int miss, int leastLength) {
		int[] locs = this.getLoc(sequence);
		return getPeptides(sequence, locs, miss, leastLength);
	}
	
	/**
	 * DiaNN format
	 * @return
	 */
	public String getCut() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cleaveAt.length(); i++) {
			char aa = cleaveAt.charAt(i);
			sb.append(aa).append("*,");
		}
		if (notCleaveAt.length() > 0) {
			sb.append("!*").append(notCleaveAt.charAt(0));
		}
		return sb.toString();
	}
}
