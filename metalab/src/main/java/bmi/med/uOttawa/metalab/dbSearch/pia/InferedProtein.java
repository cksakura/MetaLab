/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pia;

/**
 * @author Kai Cheng
 *
 */
public class InferedProtein implements Comparable<InferedProtein> {

	private String name;
	private double score;
	private int pepcount;
	private int psmcount;
	private int spcount;

	public InferedProtein(String name, double score, int pepcount, int psmcount, int spcount) {
		this.name = name;
		this.score = score;
		this.pepcount = pepcount;
		this.psmcount = psmcount;
		this.spcount = spcount;
	}

	public String getName() {
		return name;
	}

	public double getScore() {
		return score;
	}

	public int getPepcount() {
		return pepcount;
	}

	public int getPsmcount() {
		return psmcount;
	}

	public int getSpcount() {
		return spcount;
	}

	@Override
	public int compareTo(InferedProtein o) {
		// TODO Auto-generated method stub
		if (this.score < o.score) {
			return 1;
		} else if (this.score > o.score) {
			return -1;
		}
		return 0;
	}
	
	public String[] getContent() {
		String[] content = new String[5];
		content[0] = name;
		content[1] = String.valueOf(score);
		content[2] = String.valueOf(pepcount);
		content[3] = String.valueOf(psmcount);
		content[4] = String.valueOf(spcount);

		return content;
	}
	
	public Object[] getContentObject() {
		Object[] content = new Object[5];
		content[0] = name;
		content[1] = score;
		content[2] = pepcount;
		content[3] = psmcount;
		content[4] = spcount;

		return content;
	}

	public static String[] getTitle() {
		String[] content = new String[5];
		content[0] = "accessions";
		content[1] = "score";
		content[2] = "#peptides";
		content[3] = "#PSMs";
		content[4] = "#spectra";

		return content;
	}
}
