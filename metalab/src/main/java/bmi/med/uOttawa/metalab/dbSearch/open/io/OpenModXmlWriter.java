/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.mod.UmElement;
import bmi.med.uOttawa.metalab.core.mod.UmModification;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;

/**
 * @author Kai Cheng
 *
 */
public class OpenModXmlWriter {

	private static DecimalFormat df4 = FormatTool.getDF4();
	private static DecimalFormat dfE4 = FormatTool.getDFE4();

	private Document document;
	private XMLWriter writer;
	private Element root;
	private Element psms;
	private int psmCount = 0;

	private static Logger LOGGER = LogManager.getLogger();

	public OpenModXmlWriter(String output) throws IOException {
		this(new File(output));
	}

	public OpenModXmlWriter(File output) throws IOException {

		this.document = DocumentHelper.createDocument();

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(output), "UTF8");
			this.writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing PTMs to " + output, e);
		}

		this.root = DocumentFactory.getInstance().createElement("peptide_spectra_matches_summary");
		this.psms = DocumentFactory.getInstance().createElement("peptide_spectra_matches");
		this.root.add(psms);
		this.document.setRootElement(root);
	}

	public void addSetMods(HashMap<Double, PostTransModification> setModMap, OpenMod mod0) {
		Element eModSummary = DocumentFactory.getInstance().createElement("set_mod_summary");
		for (Double mass : setModMap.keySet()) {
			Element eMod = DocumentFactory.getInstance().createElement("mod");
			eMod.addAttribute("id", String.valueOf(mod0.getId()));
			double[] parameter = mod0.getGaussianParameters();
			eMod.addAttribute("gaussian_a", df4.format(parameter[0]));
			eMod.addAttribute("gaussian_b", df4.format(parameter[1]));
			eMod.addAttribute("gaussian_c", df4.format(parameter[2]));
			eMod.addAttribute("ave_intensity", df4.format(parameter[3]));
			eMod.addAttribute("gaussian_r2", df4.format(parameter[4]));

			PostTransModification ptm = setModMap.get(mass);

			eMod.addAttribute("ExpModMass", String.valueOf(ptm.getMass()));
			eMod.addAttribute("site", ptm.getSite());
			eMod.addAttribute("is_variable", String.valueOf(ptm.isVariable()));

			UmModification[] ums = ptm.getUms();
			if (ums == null) {
				eMod.addAttribute("found_in_unimod", "0");
			} else {
				eMod.addAttribute("found_in_unimod", "1");

				for (UmModification um : ums) {
					Element eUM = DocumentFactory.getInstance().createElement("unimod");
					eUM.addAttribute("title", String.valueOf(um.getTitle()));
					eUM.addAttribute("name", String.valueOf(um.getName()));
					eUM.addAttribute("mono_mass", String.valueOf(um.getMono_mass()));
					eUM.addAttribute("avg_mass", String.valueOf(um.getAvge_mass()));

					String[] specificity = um.getSpecificity();
					for (String sp : specificity) {
						Element esp = DocumentFactory.getInstance().createElement("specificity");
						esp.addAttribute("site", sp);
						eUM.add(esp);
					}

					int[] compCount = um.getComp_count();
					UmElement[] umes = um.getComp_element();

					for (int i = 0; i < compCount.length; i++) {
						Element ee = DocumentFactory.getInstance().createElement("Element");
						ee.addAttribute("title", umes[i].getTitle());
						ee.addAttribute("name", umes[i].getName());
						ee.addAttribute("mono_mass", String.valueOf(umes[i].getMono_mass()));
						ee.addAttribute("avg_mass", String.valueOf(umes[i].getAvge_mass()));
						ee.addAttribute("count", String.valueOf(compCount[i]));
						eUM.add(ee);
					}

					eMod.add(eUM);
				}
			}

			eModSummary.add(eMod);

		}

		this.root.addAttribute("set_mod_count", String.valueOf(setModMap.size()));
		this.root.add(eModSummary);
	}

	public void addMods(OpenMod[] openMods) {
		Element eModSummary = DocumentFactory.getInstance().createElement("mod_summary");
		for (OpenMod om : openMods) {
			Element eMod = DocumentFactory.getInstance().createElement("mod");
			eMod.addAttribute("id", String.valueOf(om.getId()));
			double[] parameter = om.getGaussianParameters();
			eMod.addAttribute("gaussian_a", df4.format(parameter[0]));
			eMod.addAttribute("gaussian_b", df4.format(parameter[1]));
			eMod.addAttribute("gaussian_c", df4.format(parameter[2]));
			eMod.addAttribute("ave_intensity", df4.format(parameter[3]));
			eMod.addAttribute("gaussian_r2", df4.format(parameter[4]));
			eMod.addAttribute("ExpModMass", df4.format(om.getExpModMass()));
			eMod.addAttribute("monoId", String.valueOf(om.getMonoId()));
			eMod.addAttribute("bScore", df4.format(om.getBscore()));
			eMod.addAttribute("yScore", df4.format(om.getYscore()));
			eMod.addAttribute("bModScore", df4.format(om.getbModScore()));
			eMod.addAttribute("yModScore", df4.format(om.getyModScore()));
			eMod.addAttribute("nlMass", df4.format(om.getNeutralLossMass()));
			eMod.addAttribute("nlScore", df4.format(om.getNeutralLossScore()));
			eMod.addAttribute("nlRatio", df4.format(om.getNeutralLossRatio()));
			eMod.addAttribute("isMonoIsotope", String.valueOf(om.isMonoIsotope()));

			UmModification[] ums = om.getUmmod();
			if (ums == null) {
				eMod.addAttribute("found_in_unimod", "0");
			} else {
				eMod.addAttribute("found_in_unimod", "1");

				for (UmModification um : ums) {
					Element eUM = DocumentFactory.getInstance().createElement("unimod");
					eUM.addAttribute("title", String.valueOf(um.getTitle()));
					eUM.addAttribute("name", String.valueOf(um.getName()));
					eUM.addAttribute("mono_mass", String.valueOf(um.getMono_mass()));
					eUM.addAttribute("avg_mass", String.valueOf(um.getAvge_mass()));

					String[] specificity = um.getSpecificity();
					for (String sp : specificity) {
						Element esp = DocumentFactory.getInstance().createElement("specificity");
						esp.addAttribute("site", sp);
						eUM.add(esp);
					}

					int[] compCount = um.getComp_count();
					UmElement[] umes = um.getComp_element();

					for (int i = 0; i < compCount.length; i++) {
						Element ee = DocumentFactory.getInstance().createElement("Element");
						ee.addAttribute("title", umes[i].getTitle());
						ee.addAttribute("name", umes[i].getName());
						ee.addAttribute("mono_mass", String.valueOf(umes[i].getMono_mass()));
						ee.addAttribute("avg_mass", String.valueOf(umes[i].getAvge_mass()));
						ee.addAttribute("count", String.valueOf(compCount[i]));
						eUM.add(ee);
					}

					eMod.add(eUM);
				}
			}

			eModSummary.add(eMod);
		}

		this.root.addAttribute("open_mod_count", String.valueOf(openMods.length));
		this.root.add(eModSummary);
	}

	public void addPSM(OpenPSM openpsm) {

		Element epsm = DocumentFactory.getInstance().createElement("psm");

		epsm.addAttribute("fileName", openpsm.getFileName());
		epsm.addAttribute("scan", String.valueOf(openpsm.getScan()));
		epsm.addAttribute("charge", String.valueOf(openpsm.getCharge()));
		epsm.addAttribute("precursorMr", String.valueOf(openpsm.getPrecursorMr()));
		epsm.addAttribute("rt", String.valueOf(openpsm.getRt()));

		epsm.addAttribute("sequence", openpsm.getSequence());
		epsm.addAttribute("pepMass", String.valueOf(openpsm.getPepMass()));
		epsm.addAttribute("massDiff", dfE4.format(openpsm.getMassDiff()));
		epsm.addAttribute("miss", String.valueOf(openpsm.getMiss()));
		epsm.addAttribute("num_tol_term", String.valueOf(openpsm.getNum_tol_term()));
		epsm.addAttribute("tot_num_ions", String.valueOf(openpsm.getTot_num_ions()));
		epsm.addAttribute("num_matched_ions", String.valueOf(openpsm.getNum_matched_ions()));
		epsm.addAttribute("protein", openpsm.getProtein());

		epsm.addAttribute("hyperscore", String.valueOf(openpsm.getNum_matched_ions()));
		epsm.addAttribute("nextscore", String.valueOf(openpsm.getNextscore()));
		epsm.addAttribute("expect", String.valueOf(openpsm.getExpect()));

		epsm.addAttribute("open_mod_mass", df4.format(openpsm.getOpenModmass()));
		epsm.addAttribute("delta_open_mod_mass", df4.format(openpsm.getOpenModMassDiff()));

		epsm.addAttribute("b_intensity", df4.format(openpsm.getBintensity()));
		epsm.addAttribute("y_intensity", df4.format(openpsm.getYintensity()));
		epsm.addAttribute("b_rankscore", df4.format(openpsm.getBrankscore()));
		epsm.addAttribute("y_rankscore", df4.format(openpsm.getYrankscore()));
		epsm.addAttribute("b_ion_count", String.valueOf(openpsm.getBcount()));
		epsm.addAttribute("y_ion_count", String.valueOf(openpsm.getYcount()));
		epsm.addAttribute("fragment_delta_mass", df4.format(openpsm.getFragDeltaMass()));

		epsm.addAttribute("b_mod_intensity", df4.format(openpsm.getBmodIntensity()));
		epsm.addAttribute("y_mod_intensity", df4.format(openpsm.getYmodIntensity()));
		epsm.addAttribute("b_mod_rankscore", df4.format(openpsm.getBmodrankscore()));
		epsm.addAttribute("y_mod_rankscore", df4.format(openpsm.getYmodrankscore()));
		epsm.addAttribute("mod_fragment_delta_mass", df4.format(openpsm.getFragModDeltaMass()));

		epsm.addAttribute("mod_id", String.valueOf(openpsm.getOpenModId()));
		if (openpsm.getOpenModId() >= 0) {
			int[] possible_loc = openpsm.getPossibleLoc();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < possible_loc.length; i++) {
				sb.append(possible_loc[i]).append("_");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			epsm.addAttribute("possible_loc", sb.toString());
		}

		epsm.addAttribute("is_target", String.valueOf(openpsm.isTarget()));
		epsm.addAttribute("classify_score", dfE4.format(openpsm.getClassScore()));

		psms.add(epsm);
		psmCount++;
	}

	public void addMods(OpenMod[] openMods, HashMap<Integer, double[][]> openModMatrixMap) {
		Element eModSummary = DocumentFactory.getInstance().createElement("mod_summary");
		for (OpenMod om : openMods) {
			Element eMod = DocumentFactory.getInstance().createElement("mod");
			eMod.addAttribute("id", String.valueOf(om.getId()));
			double[] parameter = om.getGaussianParameters();
			eMod.addAttribute("gaussian_a", df4.format(parameter[0]));
			eMod.addAttribute("gaussian_b", df4.format(parameter[1]));
			eMod.addAttribute("gaussian_c", df4.format(parameter[2]));
			eMod.addAttribute("ave_intensity", df4.format(parameter[3]));
			eMod.addAttribute("gaussian_r2", df4.format(parameter[4]));
			eMod.addAttribute("ExpModMass", df4.format(om.getExpModMass()));
			eMod.addAttribute("monoId", String.valueOf(om.getMonoId()));
			eMod.addAttribute("bScore", df4.format(om.getBscore()));
			eMod.addAttribute("yScore", df4.format(om.getYscore()));
			eMod.addAttribute("bModScore", df4.format(om.getbModScore()));
			eMod.addAttribute("yModScore", df4.format(om.getyModScore()));
			eMod.addAttribute("nlMass", df4.format(om.getNeutralLossMass()));
			eMod.addAttribute("nlScore", df4.format(om.getNeutralLossScore()));
			eMod.addAttribute("nlRatio", df4.format(om.getNeutralLossRatio()));
			eMod.addAttribute("isMonoIsotope", String.valueOf(om.isMonoIsotope()));

			UmModification[] ums = om.getUmmod();
			if (ums == null) {
				eMod.addAttribute("found_in_unimod", "0");
			} else {
				eMod.addAttribute("found_in_unimod", "1");

				for (UmModification um : ums) {
					Element eUM = DocumentFactory.getInstance().createElement("unimod");
					eUM.addAttribute("title", String.valueOf(um.getTitle()));
					eUM.addAttribute("name", String.valueOf(um.getName()));
					eUM.addAttribute("mono_mass", String.valueOf(um.getMono_mass()));
					eUM.addAttribute("avg_mass", String.valueOf(um.getAvge_mass()));

					String[] specificity = um.getSpecificity();
					for (String sp : specificity) {
						Element esp = DocumentFactory.getInstance().createElement("specificity");
						esp.addAttribute("site", sp);
						eUM.add(esp);
					}

					int[] compCount = um.getComp_count();
					UmElement[] umes = um.getComp_element();

					for (int i = 0; i < compCount.length; i++) {
						Element ee = DocumentFactory.getInstance().createElement("Element");
						ee.addAttribute("title", umes[i].getTitle());
						ee.addAttribute("name", umes[i].getName());
						ee.addAttribute("mono_mass", String.valueOf(umes[i].getMono_mass()));
						ee.addAttribute("avg_mass", String.valueOf(umes[i].getAvge_mass()));
						ee.addAttribute("count", String.valueOf(compCount[i]));
						eUM.add(ee);
					}

					eMod.add(eUM);
				}
			}

			if (openModMatrixMap.containsKey(om.getId())) {
				double[][] matrix = openModMatrixMap.get(om.getId());
				Element eMotif = DocumentFactory.getInstance().createElement("motif");
				for (int i = 0; i < matrix.length; i++) {
					Element eAA = DocumentFactory.getInstance().createElement("aa_" + ((char) (i + 'A')));
					for (int j = 0; j < matrix[i].length; j++) {
						eAA.addAttribute("p_" + (j - 7), df4.format(matrix[i][j]));
					}
					eMotif.add(eAA);
				}
				eMod.add(eMotif);
			}

			eModSummary.add(eMod);
		}

		this.root.addAttribute("open_mod_count", String.valueOf(openMods.length));
		this.root.add(eModSummary);
	}

	public void addSetMods(HashMap<Double, PostTransModification> setModMap, OpenMod mod0,
			HashMap<Double, double[][]> setModMatrixMap) {
		Element eModSummary = DocumentFactory.getInstance().createElement("set_mod_summary");
		for (Double mass : setModMap.keySet()) {
			Element eMod = DocumentFactory.getInstance().createElement("mod");

			double[] parameter = null;
			if (mod0 == null) {
				eMod.addAttribute("id", String.valueOf(-1));
				parameter = new double[5];
			} else {
				eMod.addAttribute("id", String.valueOf(mod0.getId()));
				parameter = mod0.getGaussianParameters();
			}
			eMod.addAttribute("gaussian_a", df4.format(parameter[0]));
			eMod.addAttribute("gaussian_b", df4.format(parameter[1]));
			eMod.addAttribute("gaussian_c", df4.format(parameter[2]));
			eMod.addAttribute("ave_intensity", df4.format(parameter[3]));
			eMod.addAttribute("gaussian_r2", df4.format(parameter[4]));
			eMod.addAttribute("ExpModMass", df4.format(mass));

			PostTransModification ptm = setModMap.get(mass);

			eMod.addAttribute("site", ptm.getSite());
			eMod.addAttribute("is_variable", String.valueOf(ptm.isVariable()));

			UmModification[] ums = ptm.getUms();
			if (ums == null) {
				eMod.addAttribute("found_in_unimod", "0");
			} else {
				eMod.addAttribute("found_in_unimod", "1");

				for (UmModification um : ums) {
					Element eUM = DocumentFactory.getInstance().createElement("unimod");
					eUM.addAttribute("title", String.valueOf(um.getTitle()));
					eUM.addAttribute("name", String.valueOf(um.getName()));
					eUM.addAttribute("mono_mass", String.valueOf(um.getMono_mass()));
					eUM.addAttribute("avg_mass", String.valueOf(um.getAvge_mass()));

					String[] specificity = um.getSpecificity();
					for (String sp : specificity) {
						Element esp = DocumentFactory.getInstance().createElement("specificity");
						esp.addAttribute("site", sp);
						eUM.add(esp);
					}

					int[] compCount = um.getComp_count();
					UmElement[] umes = um.getComp_element();

					for (int i = 0; i < compCount.length; i++) {
						Element ee = DocumentFactory.getInstance().createElement("Element");
						ee.addAttribute("title", umes[i].getTitle());
						ee.addAttribute("name", umes[i].getName());
						ee.addAttribute("mono_mass", String.valueOf(umes[i].getMono_mass()));
						ee.addAttribute("avg_mass", String.valueOf(umes[i].getAvge_mass()));
						ee.addAttribute("count", String.valueOf(compCount[i]));
						eUM.add(ee);
					}

					eMod.add(eUM);
				}
			}

			if (setModMatrixMap.containsKey(mass)) {
				double[][] matrix = setModMatrixMap.get(mass);
				Element eMotif = DocumentFactory.getInstance().createElement("motif");
				for (int i = 0; i < matrix.length; i++) {
					Element eAA = DocumentFactory.getInstance().createElement("aa_" + ((char) (i + 'A')));
					for (int j = 0; j < matrix[i].length; j++) {
						eAA.addAttribute("p_" + (j - 7), df4.format(matrix[i][j]));
					}
					eMotif.add(eAA);
				}
				eMod.add(eMotif);
			}

			eModSummary.add(eMod);
		}

		this.root.addAttribute("set_mod_count", String.valueOf(setModMap.size()));
		this.root.add(eModSummary);
	}

	public void close() throws IOException {
		root.addAttribute("psm_count", String.valueOf(psmCount));
		writer.write(document);
		writer.close();
	}

}
