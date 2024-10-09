/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import org.dom4j.Element;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;

/**
 * This is a class contains the combined information of peptide and the
 * corresponding taxon
 * 
 * @author Kai Cheng
 *
 */
public abstract class MetaPeptide {

	protected String sequence;
	protected String modSeq;
	protected int length;
	protected int missCleave;
	protected double mass;
	protected double score;
	protected boolean use;
	protected double PEP;
	protected double[] individualPEP;
	
	protected int[] taxonIds;
	protected Taxon lca;
	protected int lcaId;

	protected int totalMS2Count;
	protected int[] ms2Counts;
	protected double[] intensity;
	protected int[] idenType;
	protected String[] proteins;

	protected int[] charges;

	public static String[] idenTypeStrings = { "MS/MS", "MBR", "unmatched" };

	public MetaPeptide(String sequence) {
		this.sequence = sequence;
	}

	public MetaPeptide(String sequence, String modSeq, String[] proteins, double[] intensity, int[] idenType) {
		this.sequence = sequence;
		this.modSeq = modSeq;
		this.proteins = proteins;
		this.intensity = intensity;
		this.idenType = idenType;
	}

	public MetaPeptide(String sequence, int length, int missCleave, double mass, double score) {
		this.sequence = sequence;
		this.length = length;
		this.missCleave = missCleave;
		this.mass = mass;
		this.score = score;
	}

	public MetaPeptide(String sequence, int length, int missCleave, double mass, double score, double[] intensity) {
		this.sequence = sequence;
		this.length = length;
		this.missCleave = missCleave;
		this.mass = mass;
		this.score = score;
		this.intensity = intensity;
	}

	public MetaPeptide(String sequence, int length, int[] charge, int missCleave, double mass, double score,
			double[] intensity) {
		this.sequence = sequence;
		this.length = length;
		this.charges = charge;
		this.missCleave = missCleave;
		this.mass = mass;
		this.score = score;
		this.intensity = intensity;
	}

	public void setProteins(String[] proteins) {
		this.proteins = proteins;
	}

	public String getModSeq() {
		return modSeq;
	}

	public String getSequence() {
		return sequence;
	}

	public String[] getProteins() {
		return proteins;
	}

	public int getLength() {
		return length;
	}

	public int getMissCleave() {
		return missCleave;
	}

	public double getMass() {
		return mass;
	}

	public double getScore() {
		return score;
	}

	public double getPEP() {
		return PEP;
	}

	public void setPEP(double pEP) {
		PEP = pEP;
	}

	public double[] getIndividualPEP() {
		return individualPEP;
	}

	public void setIndividualPEP(double[] individualPEP) {
		this.individualPEP = individualPEP;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public void setMissCleave(int missCleave) {
		this.missCleave = missCleave;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public boolean isUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public int[] getIdenType() {
		return idenType;
	}

	public void setIdenType(int[] idenType) {
		this.idenType = idenType;
	}

	public int[] getTaxonIds() {
		return taxonIds;
	}

	public void setTaxonIds(int[] taxonIds) {
		this.taxonIds = taxonIds;
	}

	public Taxon getLca() {
		return lca;
	}

	public void setLca(Taxon lca) {
		this.lca = lca;
		this.lcaId = lca.getId();
	}

	public int getLcaId() {
		return lcaId;
	}

	public void setLcaId(int lcaId) {
		this.lcaId = lcaId;
	}

	public double[] getIntensity() {
		return intensity;
	}

	public void setIntensity(double[] intensity) {
		this.intensity = intensity;
	}

	public int getTotalMS2Count() {
		return totalMS2Count;
	}

	public void setTotalMS2Count(int totalMS2Count) {
		this.totalMS2Count = totalMS2Count;
	}

	public int[] getMs2Counts() {
		return ms2Counts;
	}

	public void setMs2Counts(int[] ms2Counts) {
		this.ms2Counts = ms2Counts;
	}

	public int[] getCharges() {
		return charges;
	}

	public void setCharges(int[] charges) {
		this.charges = charges;
	}

	public String getChargeString() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < charges.length; i++) {
			sb.append(charges[i]).append(";");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}

	}

	public abstract Object[] getTableObjects();

	/**
	 * Create an Element in xml file
	 * 
	 * @return
	 */
	public abstract Element getXmlPepElement();

	public static abstract class XmlPepElementParser {

		public abstract MetaPeptide parse(Element ePep);
	}
}
