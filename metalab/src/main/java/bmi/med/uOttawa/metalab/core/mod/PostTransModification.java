/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

/**
 * @author Kai Cheng
 *
 */
public class PostTransModification {

	private double mass;
	private String name;
	private String site;
	private double neutralLoss;
	private boolean variable;
	
	private UmModification[] ums;
	
	public PostTransModification(UmModification[] ums, String site, boolean variable) {
		this.ums = ums;
		this.mass = ums[0].getMono_mass();
		this.name = ums[0].getName();
		this.site = site;
		this.variable = variable;
	}
	
	public PostTransModification(double mass, String name, String site, boolean variable) {
		this.mass = mass;
		this.name = name;
		this.site = site;
		this.variable = variable;
	}
	
	public PostTransModification(double mass, double neutralLoss, String name, String site, boolean variable) {
		this.mass = mass;
		this.neutralLoss = neutralLoss;
		this.name = name;
		this.site = site;
		this.variable = variable;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String location) {
		this.site = location;
	}

	public double getNeutralLoss() {
		return neutralLoss;
	}

	public void setNeutralLoss(double neutralLoss) {
		this.neutralLoss = neutralLoss;
	}

	public boolean isVariable() {
		return variable;
	}

	public void setVariable(boolean variable) {
		this.variable = variable;
	}

	public UmModification[] getUms() {
		return ums;
	}

	public void setUms(UmModification[] ums) {
		this.ums = ums;
	}
	
}
