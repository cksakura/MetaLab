/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

/**
 * @author Kai Cheng
 *
 */
public class UnipProResult {

	private String sequence;
	private String[] uniprotIds;
	private String[] proNames;
	private int[] taxonIds;

	public UnipProResult(String sequence, String[] uniprotIds, String[] proNames, int[] taxonIds) {
		this.sequence = sequence;
		this.uniprotIds = uniprotIds;
		this.proNames = proNames;
		this.taxonIds = taxonIds;
	}

	public String getSequence() {
		return sequence;
	}

	public String[] getUniprotIds() {
		return uniprotIds;
	}

	public String[] getProNames() {
		return proNames;
	}

	public int[] getTaxonIds() {
		return taxonIds;
	}

	
}
