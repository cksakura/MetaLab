/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.psm;

/**
 * @author Kai Cheng
 *
 */
public class SearchedReference {
	
	private String ref;
	private int begin;
	private int end;
	private char preAA;
	private char nexAA;
	
	public SearchedReference(String ref){
		this.ref = ref;
	}
	
	public SearchedReference(String ref, int begin, int end){
		this.ref = ref;
		this.begin = begin;
		this.end = end;
	}
	
	public SearchedReference(String ref, int begin, int end, char preAA, char nexAA){
		this.ref = ref;
		this.begin = begin;
		this.end = end;
		this.preAA = preAA;
		this.nexAA = nexAA;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public char getPreAA() {
		return preAA;
	}

	public void setPreAA(char preAA) {
		this.preAA = preAA;
	}

	public char getNexAA() {
		return nexAA;
	}

	public void setNexAA(char nexAA) {
		this.nexAA = nexAA;
	}
	
	public static SearchedReference parse(String refinfo){
		String[] cs = refinfo.split("\\$");
		String ref = cs[0];
		int begin = Integer.parseInt(cs[1]);
		int end = Integer.parseInt(cs[2]);
		char preAA = cs[3].charAt(0);
		char nexAA = cs[4].charAt(0);
		SearchedReference sr = new SearchedReference(ref, begin, end, preAA, nexAA);
		return sr;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(ref).append("$");
		sb.append(begin).append("$");
		sb.append(end).append("$");
		sb.append(preAA).append("$");
		sb.append(nexAA);
		return sb.toString();
	}
}
