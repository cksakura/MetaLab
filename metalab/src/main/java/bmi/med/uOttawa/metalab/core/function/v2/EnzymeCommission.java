package bmi.med.uOttawa.metalab.core.function.v2;

import java.util.ArrayList;

public class EnzymeCommission {

	private String id;
	private String de;
	private String ca;
	private ArrayList<String> an;

	public EnzymeCommission(String id) {
		super();
		this.id = id;
		this.ca = "";
		this.an = new ArrayList<String>();
	}

	public EnzymeCommission(String id, String de, String ca, ArrayList<String> an) {
		super();
		this.id = id;
		this.de = de;
		this.ca = ca;
		this.an = an;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de;
	}

	public String getCa() {
		return ca;
	}

	public void setCa(String ca) {
		this.ca = ca;
	}

	public ArrayList<String> getAn() {
		return an;
	}
	
	public String getAnString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < an.size(); i++) {
			sb.append(an.get(i)).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public void setAn(ArrayList<String> an) {
		this.an = an;
	}

	public void addCa(String caString) {
		this.ca += caString;
	}

	public void addAn(String anString) {
		this.an.add(anString);
	}
}
