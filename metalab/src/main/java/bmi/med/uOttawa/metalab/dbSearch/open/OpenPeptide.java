/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open;

import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * @author Kai Cheng
 *
 */
public class OpenPeptide extends MetaPeptide {

	private String protein;
	private String[] modseqs;
	private double[] scores;
	private int[] totalMS2Counts;
	private double[][] intensities;
	private String[] variMods;
	private double[] opModMass;
	private String[] opModName;
	private String[] opModSite;

	public OpenPeptide(String sequence, String protein, String[] modseqs, double[] score, int[] totalMS2Count,
			double[][] intensities, String[] variMods, double[] opModMass, String[] opModName, String[] opModSite) {

		super(sequence);
		this.protein = protein;
		this.modseqs = modseqs;
		this.scores = score;
		this.totalMS2Counts = totalMS2Count;
		this.intensities = intensities;
		this.variMods = variMods;
		this.opModMass = opModMass;
		this.opModName = opModName;
		this.opModSite = opModSite;

		this.setUse(true);
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

	public String getProtein() {
		return protein;
	}

	public String[] getModseqs() {
		return modseqs;
	}

	public String[] getVariMods() {
		return variMods;
	}

	public double[] getOpModMass() {
		return opModMass;
	}

	public String[] getOpModName() {
		return opModName;
	}

	public String[] getOpModSite() {
		return opModSite;
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
		ePep.addAttribute("protein", protein);

		for (int i = 0; i < modseqs.length; i++) {
			Element eModpep = DocumentFactory.getInstance().createElement("Mod_form");
			eModpep.addAttribute("mod_seq", modseqs[i]);
			eModpep.addAttribute("score", String.valueOf(scores[i]));
			eModpep.addAttribute("ms2_count", String.valueOf(totalMS2Counts[i]));
			eModpep.addAttribute("vari_mod", variMods[i]);
			eModpep.addAttribute("open_mod_mass", String.valueOf(opModMass[i]));
			eModpep.addAttribute("open_mod_name", opModName[i]);
			eModpep.addAttribute("open_mod_site", opModSite[i]);

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

	public static class XmlOpenPepElementParser extends XmlPepElementParser {

		@Override
		@SuppressWarnings("unchecked")
		public MetaPeptide parse(Element ePep) {
			// TODO Auto-generated method stub
			String sequence = ePep.attributeValue("sequence");
			String protein = ePep.attributeValue("protein");

			List<Element> eModForms = ePep.elements("Mod_form");
			Element[] elements = eModForms.toArray(new Element[eModForms.size()]);

			String[] modseqs = new String[elements.length];
			double[] scores = new double[elements.length];
			double[][] intensities = new double[elements.length][];
			int[] ms2Counts = new int[elements.length];
			String[] variMods = new String[elements.length];
			double[] opModMass = new double[elements.length];
			String[] opModName = new String[elements.length];
			String[] opModSite = new String[elements.length];

			for (int i = 0; i < elements.length; i++) {
				modseqs[i] = elements[i].attributeValue("mod_seq");
				scores[i] = Double.parseDouble(elements[i].attributeValue("score"));
				String[] intensitySs = elements[i].attributeValue("intensities").split("_");
				intensities[i] = new double[intensitySs.length];
				for (int j = 0; j < intensitySs.length; j++) {
					intensities[i][j] = Double.parseDouble(intensitySs[j]);
				}

				ms2Counts[i] = Integer.parseInt(elements[i].attributeValue("ms2_count"));
				variMods[i] = elements[i].attributeValue("vari_mod");
				opModMass[i] = Double.parseDouble(elements[i].attributeValue("open_mod_mass"));
				opModName[i] = elements[i].attributeValue("open_mod_name");
				opModSite[i] = elements[i].attributeValue("open_mod_site");
			}

			OpenPeptide peptide = new OpenPeptide(sequence, protein, modseqs, scores, ms2Counts, intensities, variMods,
					opModMass, opModName, opModSite);
			return peptide;
		}
	}

}
