/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * @author Kai Cheng
 *
 */
public class XTandemPep4Meta extends MetaPeptide {

	private String protein;
	private double evalue;

	public XTandemPep4Meta(String sequence, int missCleave, double mass, double score, double evalue, String protein) {
		super(sequence, sequence.length(), missCleave, mass, score);
		this.evalue = evalue;
		this.protein = protein;
	}

	public String getProtein() {
		return protein;
	}

	public double getEvalue() {
		return evalue;
	}

	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}

	@Override
	public Object[] getTableObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub
		Element ePep = DocumentFactory.getInstance().createElement("Peptide");

		ePep.addAttribute("sequence", sequence);
		ePep.addAttribute("mass", String.valueOf(mass));
		ePep.addAttribute("score", String.valueOf(score));
		ePep.addAttribute("missCleave", String.valueOf(missCleave));
		ePep.addAttribute("proteins", protein);
		ePep.addAttribute("evalue", String.valueOf(evalue));

		ePep.addAttribute("lcaId", String.valueOf(lcaId));
		if (taxonIds != null) {
			StringBuilder taxonIdSb = new StringBuilder();
			for (int i = 0; i < taxonIds.length; i++) {
				taxonIdSb.append(taxonIds[i]).append("_");
			}
			if (taxonIdSb.length() > 0)
				taxonIdSb.deleteCharAt(taxonIdSb.length() - 1);

			ePep.addAttribute("taxonIds", taxonIdSb.toString());
		}

		return ePep;
	}

	public static class XmlTandemPepElementParser extends XmlPepElementParser {

		@Override
		public XTandemPep4Meta parse(Element ePep) {
			// TODO Auto-generated method stub
			String sequence = ePep.attributeValue("sequence");
			double mass = Double.parseDouble(ePep.attributeValue("mass"));
			double score = Double.parseDouble(ePep.attributeValue("score"));
			String protein = ePep.attributeValue("protein");
			double evalue = Double.parseDouble(ePep.attributeValue("evalue"));
			int missCleave = Integer.parseInt(ePep.attributeValue("missCleave"));
			XTandemPep4Meta peptide = new XTandemPep4Meta(sequence, missCleave, mass, score, evalue, protein);

			return peptide;
		}

	}

}
