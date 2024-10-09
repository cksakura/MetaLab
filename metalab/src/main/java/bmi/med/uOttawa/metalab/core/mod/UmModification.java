/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

import java.util.Comparator;

/**
 * @author Kai Cheng
 *
 */
public class UmModification {

	private String title;
	private String name;
	private String[] specificity;

	private double mono_mass;
	private double avge_mass;
	private UmElement[] comp_element;
	private int[] comp_count;

	public UmModification(String title, String name, String[] specificity, double mono_mass, double avge_mass,
			UmElement[] comp_element, int[] comp_count) {
		this.title = title;
		this.name = name;
		this.specificity = specificity;
		this.mono_mass = mono_mass;
		this.avge_mass = avge_mass;
		this.comp_element = comp_element;
		this.comp_count = comp_count;
	}

	public String getTitle() {
		return title;
	}

	public String getName() {
		return name;
	}

	public String[] getSpecificity() {
		return specificity;
	}

	public double getMono_mass() {
		return mono_mass;
	}

	public double getAvge_mass() {
		return avge_mass;
	}

	public UmElement[] getComp_element() {
		return comp_element;
	}

	public int[] getComp_count() {
		return comp_count;
	}

	public String getComposition() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < comp_element.length; i++) {
			sb.append(comp_element[i].getTitle());
			sb.append("(").append(comp_count[i]).append(")");
		}
		return sb.toString();
	}

	public static UmModification newInstance(double mono_mass) {
		UmModification um = new UmModification("Mod_" + (int) mono_mass, "Mod_" + (int) mono_mass, new String[] {},
				mono_mass, mono_mass, new UmElement[] {}, new int[] {});
		return um;
	}

	public static class UmModMassComparator implements Comparator<UmModification> {

		public int compare(UmModification o1, UmModification o2) {
			// TODO Auto-generated method stub
			if (o1.mono_mass < o2.mono_mass)
				return -1;
			if (o1.mono_mass > o2.mono_mass)
				return 1;
			return 0;
		}
	}
}
