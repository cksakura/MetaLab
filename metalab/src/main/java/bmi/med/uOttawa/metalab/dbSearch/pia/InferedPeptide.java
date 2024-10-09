/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pia;

/**
 * @author Kai Cheng
 *
 */
public class InferedPeptide implements Comparable<InferedPeptide> {

	private String name;
	private String accession;
	private int spcount;
	private int psmcount;
	private boolean unique;
	private double best_psm_combined_fdr_score;
	private double hyperscore;
	private double expect;
	private double percolator_score;
	private double q_value;
	private double best_psm_fdr_score;

	public InferedPeptide(String name, String accession, int spcount, int psmcount, boolean unique,
			double best_psm_combined_fdr_score, double hyperscore, double expect, double percolator_score,
			double q_value, double best_psm_fdr_score) {
		this.name = name;
		this.accession = accession;
		this.unique = unique;
		this.spcount = spcount;
		this.psmcount = psmcount;
		this.best_psm_combined_fdr_score = best_psm_combined_fdr_score;
		this.hyperscore = hyperscore;
		this.expect = expect;
		this.percolator_score = percolator_score;
		this.q_value = q_value;
		this.best_psm_fdr_score = best_psm_fdr_score;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getAccession() {
		return accession;
	}
	
	public void setAccession(String accession) {
		this.accession = accession;
	}
	
	public boolean isUnique() {
		return unique;
	}

	public int getSpcount() {
		return spcount;
	}

	public int getPsmcount() {
		return psmcount;
	}

	public double getBest_psm_combined_fdr_score() {
		return best_psm_combined_fdr_score;
	}

	public double getHyperscore() {
		return hyperscore;
	}

	public double getExpect() {
		return expect;
	}

	public double getPercolator_score() {
		return percolator_score;
	}

	public double getQ_value() {
		return q_value;
	}

	public double getBest_psm_fdr_score() {
		return best_psm_fdr_score;
	}

	@Override
	public int compareTo(InferedPeptide o) {
		// TODO Auto-generated method stub
		if (this.best_psm_combined_fdr_score > o.best_psm_combined_fdr_score) {
			return 1;
		} else if (this.best_psm_combined_fdr_score < o.best_psm_combined_fdr_score) {
			return -1;
		}
		return 0;
	}

	public String[] getContent() {
		String[] content = new String[11];
		content[0] = name;
		content[1] = accession;
		content[2] = String.valueOf(spcount);
		content[3] = String.valueOf(psmcount);
		content[4] = String.valueOf(unique);
		content[5] = String.valueOf(best_psm_combined_fdr_score);
		content[6] = String.valueOf(hyperscore);
		content[7] = String.valueOf(expect);
		content[8] = String.valueOf(percolator_score);
		content[9] = String.valueOf(q_value);
		content[10] = String.valueOf(best_psm_fdr_score);
		return content;
	}
	
	public Object[] getContentObject() {
		Object[] content = new Object[11];
		content[0] = name;
		content[1] = accession;
		content[2] = spcount;
		content[3] = psmcount;
		content[4] = unique;
		content[5] = best_psm_combined_fdr_score;
		content[6] = hyperscore;
		content[7] = expect;
		content[8] = percolator_score;
		content[9] = q_value;
		content[10] = best_psm_fdr_score;
		return content;
	}
}
