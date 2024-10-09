/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

/**
 * @author Kai Cheng
 *
 */
public enum MetaLabVersions {

	v1_2_0("1.2.0", "");

	String version;
	String description;

	MetaLabVersions(String version, String description) {
		this.version = version;
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	public static MetaLabVersions getCurrentVersion() {
		return v1_2_0;
	}
}
