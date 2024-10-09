/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

/**
 * @author Kai Cheng
 *
 */
public class UnipeptWebResult {

	private String sequence;
	private String[] taxonomy;
	private String rank;
	
	public UnipeptWebResult(String sequence, String[] taxonomy, String rank) {
		this.sequence = sequence;
		this.taxonomy = taxonomy;
		this.rank = rank;
	}

	public String getSequence() {
		return sequence;
	}

	public String[] getTaxonomy() {
		return taxonomy;
	}

	public String getRank() {
		return rank;
	}
	
	
}
