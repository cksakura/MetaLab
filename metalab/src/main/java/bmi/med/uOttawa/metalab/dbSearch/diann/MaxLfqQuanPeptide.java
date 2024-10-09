package bmi.med.uOttawa.metalab.dbSearch.diann;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

public class MaxLfqQuanPeptide extends MetaPeptide {

	public MaxLfqQuanPeptide(String sequence, double[] intensity) {
		super(sequence);
		// TODO Auto-generated constructor stub
		this.intensity = intensity;
	}

	@Override
	public Object[] getTableObjects() {
		// TODO Auto-generated method stub

		Object[] objs = new Object[2 + intensity.length];
		objs[0] = sequence;
		double total = 0;
		for (int i = 0; i < intensity.length; i++) {
			objs[i + 2] = intensity[i];
			total += intensity[i];
		}
		objs[1] = total;
		return objs;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub

		Element ePep = DocumentFactory.getInstance().createElement("Peptide");

		ePep.addAttribute("sequence", sequence);

		StringBuilder intensitySb = new StringBuilder();
		for (double inten : intensity) {
			intensitySb.append(inten).append("_");
		}
		if (intensitySb.length() > 0)
			intensitySb.deleteCharAt(intensitySb.length() - 1);

		ePep.addAttribute("intensities", intensitySb.toString());
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

	public static class XmlFLPepElementParser extends XmlPepElementParser {

		@Override
		public MetaPeptide parse(Element ePep) {
			// TODO Auto-generated method stub
			String sequence = ePep.attributeValue("sequence");

			String[] intensitySs = ePep.attributeValue("intensities").split("_");
			double[] intensity = new double[intensitySs.length];
			for (int i = 0; i < intensitySs.length; i++) {
				intensity[i] = Double.parseDouble(intensitySs[i]);
			}

			MaxLfqQuanPeptide peptide = new MaxLfqQuanPeptide(sequence, intensity);
			return peptide;
		}
	}

	public String getSequence() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length(); i++) {
			char aa = sequence.charAt(i);
			if (aa >= 'A' && aa <= 'Z') {
				sb.append(aa);
			}
		}
		return sb.toString();
	}
}
