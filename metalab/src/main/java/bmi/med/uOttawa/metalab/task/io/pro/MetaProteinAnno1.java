package bmi.med.uOttawa.metalab.task.io.pro;

public class MetaProteinAnno1 {

	private MetaProtein pro;
	private String COG;
	private String NOG;
	private String KEGG;
	private String KO;
	private String[] GOBP;
	private String[] GOCC;
	private String[] GOMF;
	private int taxId;

	public MetaProteinAnno1(MetaProtein pro) {
		this(pro, "", "", "", "", new String[] {}, new String[] {}, new String[] {});
	}

	public MetaProteinAnno1(MetaProtein pro, String cOG, String nOG, String kEGG, String kO, String[] gOBP,
			String[] gOCC, String[] gOMF) {
		this.pro = pro;
		COG = cOG;
		NOG = nOG;
		KEGG = kEGG;
		KO = kO;
		GOBP = gOBP;
		GOCC = gOCC;
		GOMF = gOMF;
	}

	public String getCOG() {
		return COG;
	}

	public void setCOG(String cOG) {
		COG = cOG;
	}

	public String getNOG() {
		return NOG;
	}

	public void setNOG(String nOG) {
		NOG = nOG;
	}

	public String getKEGG() {
		return KEGG;
	}

	public void setKEGG(String kEGG) {
		KEGG = kEGG;
	}

	public String getKO() {
		return KO;
	}

	public void setKO(String kO) {
		KO = kO;
	}

	public String[] getGOBP() {
		return GOBP;
	}

	public void setGOBP(String[] gOBP) {
		GOBP = gOBP;
	}

	public String[] getGOCC() {
		return GOCC;
	}

	public void setGOCC(String[] gOCC) {
		GOCC = gOCC;
	}

	public String[] getGOMF() {
		return GOMF;
	}

	public void setGOMF(String[] gOMF) {
		GOMF = gOMF;
	}

	public int getTaxId() {
		return taxId;
	}

	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}

	public MetaProtein getPro() {
		return pro;
	}

}
