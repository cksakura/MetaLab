/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

/**
 * @author Kai Cheng
 *
 */
public class UmElement {

	private String title;
	private String name;
	private double avge_mass;
	private double mono_mass;
	
	public UmElement(String title, String name, double avge_mass, double mono_mass) {
		this.title = title;
		this.name = name;
		this.avge_mass = avge_mass;
		this.mono_mass = mono_mass;
	}

	public String getTitle() {
		return title;
	}

	public String getName() {
		return name;
	}

	public double getAvge_mass() {
		return avge_mass;
	}

	public double getMono_mass() {
		return mono_mass;
	}
	
	
}
