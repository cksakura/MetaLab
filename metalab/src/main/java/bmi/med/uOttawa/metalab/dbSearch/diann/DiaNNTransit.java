package bmi.med.uOttawa.metalab.dbSearch.diann;

public class DiaNNTransit {

	private String[] content;
	private int transitId;
	private int seqId;
	private int proGroupId;
	private int uniprotId;

	public DiaNNTransit(String[] content, int transitId, int seqId, int proGroupId, int uniprotId) {
		this.content = content;
		this.transitId = transitId;
		this.seqId = seqId;
		this.proGroupId = proGroupId;
		this.uniprotId = uniprotId;
	}

	public String[] getContent() {
		return content;
	}

	public String getTransition() {
		return content[transitId];
	}

	public String getSequence() {
		return content[seqId];
	}

	public String getProtein() {
		return content[proGroupId];
	}

	public void setProtein(String protein) {
		this.content[proGroupId] = protein;
	}
	
	public void combine(DiaNNTransit transit) {
		this.content[proGroupId] += ";";
		this.content[proGroupId] += transit.content[proGroupId];
		
		this.content[uniprotId] += ";";
		this.content[uniprotId] += transit.content[uniprotId];
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < content.length; i++) {
			sb.append(content[i]).append("\t");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);

		}
		return sb.toString();
	}
}
