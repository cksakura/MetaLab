/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;

/**
 * @author Kai Cheng
 *
 */
public class UnipLCAResult {

	private static HashMap<String, Integer> rankMap = new HashMap<String, Integer>();
	static {
		rankMap.put("superkingdom", 0);
		rankMap.put("kingdom", 1);
		rankMap.put("phylum", 4);
		rankMap.put("class_", 7);
		rankMap.put("class", 7);
		rankMap.put("order", 11);
		rankMap.put("family", 16);
		rankMap.put("genus", 20);
		rankMap.put("species", 23);
	};

	private String sequence;
	private int taxonId;
	private String taxonName;
	private String taxonRank;

	public UnipLCAResult(String sequence, int taxonId, String taxonName, String taxonRank) {
		this.sequence = sequence;
		this.taxonId = taxonId;
		this.taxonName = taxonName;
		this.taxonRank = taxonRank;
	}

	public String getSequence() {
		return sequence;
	}

	public int getTaxonId() {
		return taxonId;
	}

	public String getTaxonName() {
		return taxonName;
	}

	public String getTaxonRank() {
		return taxonRank;
	}

	public Taxon getTaxon() {
		int rankId = -1;
		if (rankMap.containsKey(taxonRank)) {
			rankId = rankMap.get(taxonRank);
		}

		Taxon taxon = new Taxon(taxonId, rankId, taxonName);
		return taxon;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sequence).append(";").append(taxonId).append(";").append(taxonName).append(";").append(taxonRank);
		return sb.toString();
	}
}
