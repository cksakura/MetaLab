package bmi.med.uOttawa.metalab.quant.flashLFQ;

import java.util.Arrays;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

public class FlashLfqQuanPeptide extends MetaPeptide {

	private boolean isDecoy;

	public FlashLfqQuanPeptide(String sequence, String modSeq, String[] proteins, double[] intensity, int[] idenType, boolean isDecoy) {
		super(sequence, modSeq, proteins, intensity, idenType);
		// TODO Auto-generated constructor stub
		this.isDecoy = isDecoy;
	}

	public String getProteinString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < proteins.length; i++) {
			sb.append(proteins[i]).append(";");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}
	}

	@Override
	public Object[] getTableObjects() {
		// TODO Auto-generated method stub

		Object[] objs = new Object[3 + intensity.length];
		if (proteins != null && proteins.length > 0) {
			objs = new Object[3 + intensity.length];
			objs[0] = modSeq;
			objs[1] = getProteinString();
			double total = 0;
			for (int i = 0; i < intensity.length; i++) {
				objs[i + 3] = intensity[i];
				total += intensity[i];
			}
			objs[2] = total;
		} else {
			objs = new Object[2 + intensity.length];
			objs[0] = modSeq;
			double total = 0;
			for (int i = 0; i < intensity.length; i++) {
				objs[i + 2] = intensity[i];
				total += intensity[i];
			}
			objs[1] = total;
		}

		return objs;
	}

	public boolean isDecoy() {
		return isDecoy;
	}

	public void setDecoy(boolean isDecoy) {
		this.isDecoy = isDecoy;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub

		Element ePep = DocumentFactory.getInstance().createElement("Peptide");

		ePep.addAttribute("sequence", sequence);
		ePep.addAttribute("modSeq", modSeq);

		StringBuilder prosb = new StringBuilder();
		if (proteins != null && proteins.length > 0) {
			for (String protein : proteins) {
				prosb.append(protein).append(">");
			}
			if (prosb.length() > 0)
				prosb.deleteCharAt(prosb.length() - 1);
		}

		ePep.addAttribute("proteins", prosb.toString());

		StringBuilder intensitySb = new StringBuilder();
		for (double inten : intensity) {
			intensitySb.append(inten).append("_");
		}
		if (intensitySb.length() > 0)
			intensitySb.deleteCharAt(intensitySb.length() - 1);
		ePep.addAttribute("intensities", intensitySb.toString());

		StringBuilder idenTypeSb = new StringBuilder();
		for (double iden : idenType) {
			idenTypeSb.append(iden).append("_");
		}
		if (idenTypeSb.length() > 0)
			idenTypeSb.deleteCharAt(idenTypeSb.length() - 1);
		ePep.addAttribute("idenTypes", idenTypeSb.toString());

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
			String modSeq = ePep.attributeValue("modSeq");
			String[] proteins = new String[] {};
			if (ePep.attributeValue("proteins") != null) {
				proteins = ePep.attributeValue("proteins").split(">");
			}

			String[] intensitySs = ePep.attributeValue("intensities").split("_");
			double[] intensity = new double[intensitySs.length];
			for (int i = 0; i < intensitySs.length; i++) {
				intensity[i] = Double.parseDouble(intensitySs[i]);
			}

			int[] idenType = new int[intensity.length];
			String[] idenTypeSs = ePep.attributeValue("idenTypes").split("_");
			if (idenType.length == idenTypeSs.length) {
				for (int i = 0; i < idenTypeSs.length; i++) {
					idenType[i] = Integer.parseInt(idenTypeSs[i]);
				}
			} else {
				Arrays.fill(idenType, 0);
			}

			FlashLfqQuanPeptide peptide = new FlashLfqQuanPeptide(sequence, modSeq, proteins, intensity, idenType,
					false);
			return peptide;
		}
	}

}
