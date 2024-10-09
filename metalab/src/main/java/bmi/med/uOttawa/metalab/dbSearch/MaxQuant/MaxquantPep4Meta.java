/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantPep4Meta extends MetaPeptide {

	private double PEP;
	private String[] proteins;
	private boolean reverse;
	private boolean contaminant;

	public MaxquantPep4Meta(String sequence, int length, int missCleave, double mass, double score, double PEP,
			int totalMS2Count, String[] proteins) {

		super(sequence, length, missCleave, mass, score, new double[] {});
		this.PEP = PEP;
		this.totalMS2Count = totalMS2Count;
		this.proteins = proteins;
	}

	public MaxquantPep4Meta(String sequence, int length, int[] charge, int missCleave, double mass, double score,
			int totalMS2Count, double PEP, String[] proteins, boolean reverse, boolean contaminant,
			double[] intensity) {

		super(sequence, length, charge, missCleave, mass, score, intensity);
		this.PEP = PEP;
		this.totalMS2Count = totalMS2Count;
		this.proteins = proteins;
		this.reverse = reverse;
		this.contaminant = contaminant;

		if (reverse || contaminant) {
			this.use = false;
		} else {
			this.use = true;
		}
	}

	public String[] getProteins() {
		return proteins;
	}

	public String getProtein() {
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

	public void setScore(double score) {
		this.score = score;
	}

	public double getPEP() {
		return PEP;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public boolean isContaminant() {
		return contaminant;
	}

	public void setContaminant(boolean contaminant) {
		this.contaminant = contaminant;
	}

	public void setPEP(double PEP) {
		this.PEP = PEP;
	}

	public void setProteins(String[] proteins) {
		this.proteins = proteins;
	}

	public Object[] getTableObjects() {
		// TODO Auto-generated method stub

		Object[] objs = new Object[10 + intensity.length];
		objs[0] = this.getSequence();
		objs[1] = this.getLength();
		objs[2] = this.getMissCleave();
		objs[3] = this.getMass();
		objs[4] = this.getProtein();
		objs[5] = this.getChargeString();
		objs[6] = this.getScore();
		objs[7] = this.getPEP();
		objs[8] = MathTool.getTotal(intensity);

		for (int i = 0; i < intensity.length; i++) {
			objs[i + 9] = intensity[i];
		}

		objs[intensity.length + 9] = this.getTotalMS2Count();

		return objs;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub
		Element ePep = DocumentFactory.getInstance().createElement("Peptide");

		ePep.addAttribute("sequence", sequence);
		ePep.addAttribute("mass", String.valueOf(mass));
		ePep.addAttribute("score", String.valueOf(score));
		ePep.addAttribute("missCleave", String.valueOf(missCleave));
		ePep.addAttribute("PEP", String.valueOf(PEP));
		ePep.addAttribute("totalMS2Count", String.valueOf(totalMS2Count));

		StringBuilder prosb = new StringBuilder();
		for (String protein : proteins) {
			prosb.append(protein).append(">");
		}
		if (prosb.length() > 0)
			prosb.deleteCharAt(prosb.length() - 1);

		StringBuilder intensitySb = new StringBuilder();
		for (double inten : intensity) {
			intensitySb.append(inten).append("_");
		}
		if (intensitySb.length() > 0)
			intensitySb.deleteCharAt(intensitySb.length() - 1);

		ePep.addAttribute("proteins", prosb.toString());
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

	public static class XmlMQPepElementParser extends XmlPepElementParser {

		@Override
		public MetaPeptide parse(Element ePep) {
			// TODO Auto-generated method stub
			String sequence = ePep.attributeValue("sequence");
//			double mass = Double.parseDouble(ePep.attributeValue("mass"));
			double mass = 0;
			double score = Double.parseDouble(ePep.attributeValue("score"));
			int missCleave = Integer.parseInt(ePep.attributeValue("missCleave"));
			int length = sequence.length();

			int totalMS2Count = Integer.parseInt(ePep.attributeValue("totalMS2Count"));
			double PEP = Double.parseDouble(ePep.attributeValue("PEP"));

			String[] proteins = ePep.attributeValue("proteins").split(">");

			String[] intensitySs = ePep.attributeValue("intensities").split("_");
			double[] intensity = new double[intensitySs.length];
			for (int i = 0; i < intensitySs.length; i++) {
				intensity[i] = Double.parseDouble(intensitySs[i]);
			}

			MaxquantPep4Meta peptide = new MaxquantPep4Meta(sequence, length, missCleave, mass, score, PEP,
					totalMS2Count, proteins);
			peptide.setIntensity(intensity);

			return peptide;
		}
	}

}
