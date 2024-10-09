/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.util.HashSet;

/**
 * @author Kai Cheng
 *
 */
public class PFindProtein {

	private int id;
	private String name;
	private double score;
	private double qValue;
	private double coverage;
	private int pepCount;
	private int spCount;
	private String des;
	private HashSet<String> sameSet;
	private HashSet<String> subSet;
	private HashSet<Integer> pepIdSet;

	public PFindProtein(int id, String name, double score, double qValue, double coverage, int pepCount, String des,
			HashSet<String> sameSet) {
		this.id = id;
		this.name = name;
		this.score = score;
		this.qValue = qValue;
		this.coverage = coverage;
		this.pepCount = pepCount;
		this.des = des;
		this.sameSet = sameSet;
		this.subSet = new HashSet<String>();
		this.pepIdSet = new HashSet<Integer>();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public double getScore() {
		return score;
	}

	public double getqValue() {
		return qValue;
	}

	public double getCoverage() {
		return coverage;
	}

	public int getPepCount() {
		return pepCount;
	}

	public String getDes() {
		return des;
	}

	public int getSpCount() {
		return spCount;
	}

	public void setSpCount(int spCount) {
		this.spCount = spCount;
	}

	public HashSet<String> getSameSet() {
		return sameSet;
	}

	public void setSameSet(HashSet<String> sameSet) {
		this.sameSet = sameSet;
	}
	
	public void addSubProtein(String pro) {
		this.subSet.add(pro);
	}
	
	public HashSet<String> getSubSet() {
		return subSet;
	}

	public void addPeptideId(int pepid) {
		this.pepIdSet.add(pepid);
	}

	public HashSet<Integer> getPepIdSet() {
		return pepIdSet;
	}
	
	
}
