/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * @author Kai Cheng
 *
 */
public class PFindPeptide extends MetaPeptide {

	private String protein;
	private String[] modseqs;
	private double[] scores;
	private int[] totalMS2Counts;
	private double[][] intensities;

	public PFindPeptide(String sequence, int missCleave, double mass, String protein, String[] modseqs, double[] scores,
			int[] totalMS2Counts, double[][] intensities) {
		super(sequence, sequence.length(), missCleave, mass, scores[0]);
		this.protein = protein;
		this.modseqs = modseqs;
		this.scores = scores;
		this.totalMS2Counts = totalMS2Counts;
		this.intensities = intensities;
	}

	public double getScore() {
		double score = 0;
		for (int i = 0; i < this.scores.length; i++) {
			score += this.scores[i];
		}
		return score;
	}

	public int getTotalMS2Count() {
		int totalMS2Count = 0;
		for (int i = 0; i < this.totalMS2Counts.length; i++) {
			totalMS2Count += totalMS2Counts[i];
		}
		return totalMS2Count;
	}

	public String getProtein() {
		return protein;
	}

	public String[] getModseqs() {
		return modseqs;
	}

	public double[] getScores() {
		return scores;
	}

	public int[] getTotalMS2Counts() {
		return totalMS2Counts;
	}

	public double[] getIntensity() {
		double[] intensity = new double[this.intensities[0].length];
		for (int i = 0; i < intensity.length; i++) {
			for (int j = 0; j < this.intensities.length; j++) {
				intensity[i] += this.intensities[j][i];
			}
		}
		return intensity;
	}

	public double[][] getIntensities() {
		return intensities;
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
		ePep.addAttribute("protein", protein);
		ePep.addAttribute("missCleave", String.valueOf(missCleave));

		for (int i = 0; i < modseqs.length; i++) {
			Element eModpep = DocumentFactory.getInstance().createElement("Mod_form");
			eModpep.addAttribute("mod_seq", modseqs[i]);
			eModpep.addAttribute("score", String.valueOf(scores[i]));
			eModpep.addAttribute("ms2_count", String.valueOf(totalMS2Counts[i]));

			StringBuilder intensitySb = new StringBuilder();
			for (int j = 0; j < intensities[i].length; j++) {
				intensitySb.append(intensities[i][j]).append("_");
			}
			if (intensitySb.length() > 0)
				intensitySb.deleteCharAt(intensitySb.length() - 1);

			eModpep.addAttribute("intensities", intensitySb.toString());

			ePep.add(eModpep);
		}

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

	public static class XmlPFindPepElementParser extends XmlPepElementParser {

		@SuppressWarnings("unchecked")
		public MetaPeptide parse(Element ePep) {
			// TODO Auto-generated method stub
			String sequence = ePep.attributeValue("sequence");
			double mass = Double.parseDouble(ePep.attributeValue("mass"));
			int missCleave = Integer.parseInt(ePep.attributeValue("missCleave"));
			String protein = ePep.attributeValue("protein");
			List<Element> eModForms = ePep.elements("Mod_form");
			Element[] elements = eModForms.toArray(new Element[eModForms.size()]);

			String[] modseqs = new String[elements.length];
			double[] scores = new double[elements.length];
			double[][] intensities = new double[elements.length][];
			int[] ms2Counts = new int[elements.length];

			for (int i = 0; i < elements.length; i++) {
				modseqs[i] = elements[i].attributeValue("mod_seq");
				scores[i] = Double.parseDouble(elements[i].attributeValue("score"));
				String[] intensitySs = elements[i].attributeValue("intensities").split("_");
				intensities[i] = new double[intensitySs.length];
				for (int j = 0; j < intensitySs.length; j++) {
					intensities[i][j] = Double.parseDouble(intensitySs[j]);
				}

				ms2Counts[i] = Integer.parseInt(elements[i].attributeValue("ms2_count"));
			}

			PFindPeptide peptide = new PFindPeptide(sequence, missCleave, mass, protein, modseqs, scores, ms2Counts,
					intensities);
			return peptide;
		}

	}

}
