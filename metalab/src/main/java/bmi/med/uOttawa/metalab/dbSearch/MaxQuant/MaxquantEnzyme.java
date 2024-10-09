/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantEnzyme {

	private String title;
	private String description;
	private String[] specificity;
	private static final char[] commonAAs = new char[] {};

	/**
	 * in pFind specific is "3"
	 */
	public static final int specific = 0;

	public static final int semi_Nterm = 1;
	public static final int semi_Cterm = 2;

	/**
	 * in pFind semi_specific is "1"
	 */
	public static final int semi_specific = 3;

	/**
	 * in pFind unspecific is "0"
	 */
	public static final int unspecific = 4;

	public static final int no_digest = 5;

	public static final String[] enzymeModes = new String[] { "Specific", "Semi free N-term", "Semi free C-term",
			"Semi specific", "Unspecific", "None" };

	public static final String[] simpleEnzymeModes = new String[] { "Specific", "Semi specific", "Unspecific" };

	public MaxquantEnzyme(String title, String description, String[] specificity) {
		super();
		this.title = title;
		this.description = description;
		this.specificity = specificity;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String[] getSpecificity() {
		return specificity;
	}

	public String toString() {
		return title;
	}

	public String getXTandemFormat() {
		StringBuilder sb = new StringBuilder();
		for (String sp : this.specificity) {
			char cleave0 = sp.charAt(0);
			char cleave1 = sp.charAt(1);
		}
		return sb.toString();
	}

}
