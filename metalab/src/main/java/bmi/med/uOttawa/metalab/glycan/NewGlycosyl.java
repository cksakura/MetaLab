/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;

/**
 * @author Kai Cheng
 *
 */
public class NewGlycosyl extends Glycosyl {

	private int[][] possibleComposition;
	private Glycosyl[] glycosyls;
	private String[] modName;
	private int[][] modComposition;

	public NewGlycosyl(String title, String fullname, int[] CHO, double mono_mass, double avge_mass, String pattern,
			int graphicsId) {
		super(title, fullname, CHO, mono_mass, avge_mass, pattern, graphicsId);
		// TODO Auto-generated constructor stub
	}

	public void parsePossibleComposition() {
		ArrayList<int[]> compositionList = new ArrayList<int[]>();
		double delta = mono_mass * 20 * 1E-6;
		L: for (int i1 = 5;; i1++) {
			double m = i1 * AminoAcidProperty.MONOW_C;
			if (m - mono_mass > delta) {
				break;
			}
			for (int i2 = i1 + 3; i2 <= i1 * 2; i2++) {
				m = i1 * AminoAcidProperty.MONOW_C + i2 * AminoAcidProperty.MONOW_H;
				if (m - mono_mass > delta) {
					break;
				}
				for (int i3 = 1;; i3++) {
					m = i1 * AminoAcidProperty.MONOW_C + i2 * AminoAcidProperty.MONOW_H
							+ i3 * AminoAcidProperty.MONOW_O;
					if (m - mono_mass > delta) {
						break;
					} else if (Math.abs(m - mono_mass) < delta) {
						if (i3 > i1)
							continue;
						if (i3 < 3)
							continue;
						if (i3 * 2 + 1 < i1)
							continue;
						compositionList.add(new int[] { i1, i2, i3, 0, 0, 0 });
						continue L;
					}
					for (int i4 = 0; i4 < 5; i4++) {
						if (i3 + i4 < 3)
							continue;
						if ((i3 + i4) * 2 + 1 < i1)
							continue;
						m = i1 * AminoAcidProperty.MONOW_C + i2 * AminoAcidProperty.MONOW_H
								+ i3 * AminoAcidProperty.MONOW_O + i4 * AminoAcidProperty.MONOW_N;
						if (m - mono_mass > delta) {
							break;
						} else if (Math.abs(m - mono_mass) < delta) {
							if (i3 > i1)
								continue;
							compositionList.add(new int[] { i1, i2, i3, i4, 0, 0 });
							break;
						}

						for (int i5 = 0; i5 < 5; i5++) {
							if (i5 * 4 > i3)
								continue;
							m = i1 * AminoAcidProperty.MONOW_C + i2 * AminoAcidProperty.MONOW_H
									+ i3 * AminoAcidProperty.MONOW_O + i4 * AminoAcidProperty.MONOW_N
									+ i5 * AminoAcidProperty.MONOW_P;
							if (m - mono_mass > delta) {
								break;
							} else if (Math.abs(m - mono_mass) < delta) {
								if (i5 + (i3 - i5 * 4) + i4 < 5)
									continue;
								compositionList.add(new int[] { i1, i2, i3, i4, i5, 0 });
								break;
							}

							for (int i6 = 0; i6 < 5; i6++) {
								if (i6 * 4 + i5 * 4 > i3)
									continue;
								m = i1 * AminoAcidProperty.MONOW_C + i2 * AminoAcidProperty.MONOW_H
										+ i3 * AminoAcidProperty.MONOW_O + i4 * AminoAcidProperty.MONOW_N
										+ i5 * AminoAcidProperty.MONOW_P + i6 * AminoAcidProperty.MONOW_S32;
								if (m - mono_mass > delta) {
									break;
								} else if (Math.abs(m - mono_mass) < delta) {
									if (i5 + i6 + (i3 - i5 * 4 - i6 * 4) + i4 < 5)
										continue;
									compositionList.add(new int[] { i1, i2, i3, i4, i5, i6 });
									break;
								}
							}
						}
					}
				}
			}
		}
		this.possibleComposition = compositionList.toArray(new int[compositionList.size()][]);
		this.glycosyls = new Glycosyl[possibleComposition.length];
		this.modName = new String[possibleComposition.length];
		this.modComposition = new int[possibleComposition.length][];
	}

	public void parsePossibleGlycosyl(HashMap<String, Glycosyl> glycosylMap, HashMap<String, int[]> modMap) {
		Glycosyl[] glycosyls = glycosylMap.values().toArray(new Glycosyl[glycosylMap.size()]);
		Arrays.sort(glycosyls, new Glycosyl.GlycosylCompComparator());
		for (int i = 0; i < possibleComposition.length; i++) {
			for (int j = 0; j < glycosyls.length; j++) {
				int[] compositionj = glycosyls[j].CHO;
				int isEqual = 0;
				for (int k = 0; k < compositionj.length; k++) {
					if (possibleComposition[i][k] < compositionj[k]) {
						isEqual = -1;
						break;
					} else if (possibleComposition[i][k] > compositionj[k]) {
						isEqual = 1;
						break;
					}
				}
				if (isEqual == -1) {
					Iterator<String> it = modMap.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						int[] modcomp = modMap.get(key);
						boolean equal = true;
						for (int k = 0; k < compositionj.length; k++) {
							if (possibleComposition[i][k] != compositionj[k] + modcomp[k]) {
								equal = false;
								break;
							}
						}
						if (equal) {
							this.glycosyls[i] = glycosyls[j];
							this.modName[i] = key;
							this.modComposition[i] = modcomp;
						}
					}
				} else if (isEqual == 0) {
					this.glycosyls[i] = glycosyls[j];
				}
			}
		}
	}

	public int[][] getPossibleComposition() {
		return possibleComposition;
	}

	public void setPossibleComposition(int[][] possibleComposition) {
		this.possibleComposition = possibleComposition;
	}

	public void setGlycosyls(Glycosyl[] glycosyls) {
		this.glycosyls = glycosyls;
	}

	public void setModName(String[] modName) {
		this.modName = modName;
	}

	public void setModComposition(int[][] modComposition) {
		this.modComposition = modComposition;
	}

	public Glycosyl[] getGlycosyls() {
		return glycosyls;
	}

	public String[] getModName() {
		return modName;
	}

	public int[][] getModComposition() {
		return modComposition;
	}

	public String getFullString() {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append(";");
		sb.append(fullname).append(";");
		sb.append(";");
		sb.append(mono_mass).append(";");
		sb.append(avge_mass).append(";");
		sb.append(pattern).append(";");
		sb.append(graphicsId).append(";");

		for (int i = 0; i < possibleComposition.length; i++) {
			for (int j = 0; j < possibleComposition[i].length; j++) {
				sb.append(possibleComposition[i][j]).append("_");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(",");
			if (glycosyls[i] != null) {
				String fullString = glycosyls[i].getFullString();
				fullString = fullString.replaceAll(";", "@");
				fullString = fullString.replaceAll(",", "_");
				sb.append(fullString).append(",");
			} else {
				sb.append(",");
			}
			if (modName[i] != null) {
				sb.append(modName[i]).append(",");
				for (int j = 0; j < modComposition.length; j++) {
					sb.append(modComposition[j]).append("_");
				}
				sb.deleteCharAt(sb.length() - 1);
			} else {
				sb.append(",");
			}
			sb.append(";");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String[] getCompositionStrings() {
		String[] element = new String[] { "C", "H", "O", "N", "P", "S" };
		String[] cs = new String[this.possibleComposition.length];
		for (int i = 0; i < cs.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < possibleComposition[i].length; j++) {
				if (possibleComposition[i][j] > 0) {
					sb.append(element[j]).append("(").append(possibleComposition[i][j]).append(")");
				}
			}
			cs[i] = sb.toString();
		}

		return cs;
	}
}
