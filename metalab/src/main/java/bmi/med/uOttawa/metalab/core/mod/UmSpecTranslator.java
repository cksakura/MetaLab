/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

/**
 * Combine "site" and "position" in specificity in unimod.xml
 * 
 * @author Kai Cheng
 *
 */
public class UmSpecTranslator {

	public static String translate(String site, String position) {
		if (site.length() == 1) {
			if (position.equals("Anywhere")) {
				return site;
			} else if (position.equals("Protein N-term")) {
				return '[' + site;
			} else if (position.equals("Protein C-term")) {
				return ']' + site;
			} else if (position.equals("Any N-term")) {
				return 'n' + site;
			} else if (position.equals("Any C-term")) {
				return 'c' + site;
			} else {
				return "";
			}
		} else {
			if (site.startsWith("C")) {
				if (position.startsWith("A")) {
					return "c*";
				} else if (position.startsWith("P")) {
					return "]*";
				} else {
					return "";
				}
			} else if (site.startsWith("N")) {

				if (position.startsWith("A")) {
					return "n*";
				} else if (position.startsWith("P")) {
					return "[*";
				} else {
					return "";
				}

			} else {
				return "";
			}
		}
	}

}
