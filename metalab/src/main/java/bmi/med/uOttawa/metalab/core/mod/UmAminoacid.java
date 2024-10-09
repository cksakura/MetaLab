/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

/**
 * @author Kai Cheng
 *
 */
public class UmAminoacid {

	private String title;
	private String threeLet;
	private String name;
	private double mono_mass;
	private double avge_mass;
	private UmElement[] comp_element;
	private int[] comp_count;
	
	public UmAminoacid(String title, String threeLet, String name, double mono_mass, double avge_mass,
			UmElement[] comp_element, int[] comp_count) {
		this.title = title;
		this.threeLet = threeLet;
		this.name = name;
		this.mono_mass = mono_mass;
		this.avge_mass = avge_mass;
		this.comp_element = comp_element;
		this.comp_count = comp_count;
	}

	public String getTitle() {
		return title;
	}

	public String getThreeLet() {
		return threeLet;
	}

	public String getName() {
		return name;
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
	
	
}
