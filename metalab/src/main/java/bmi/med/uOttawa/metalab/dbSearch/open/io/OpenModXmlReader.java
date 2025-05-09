/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.math.DataListClusterable;
import bmi.med.uOttawa.metalab.core.ml.SemiSuperClassifier;
import bmi.med.uOttawa.metalab.core.ml.UnsuperCluster;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.mod.UmElement;
import bmi.med.uOttawa.metalab.core.mod.UmModification;
import bmi.med.uOttawa.metalab.core.mod.UnimodReader;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * @author Kai Cheng
 *
 */
public class OpenModXmlReader {
	
	private Element root;
	private OpenMod[] openMods;
	private OpenPSM[] openPSMs;
	private HashMap<Double, PostTransModification> setModMap;
	
	private HashMap<Integer, double[][]> openModMatrixMap;
	private HashMap<Double, double[][]> setModMatrixMap;
	
	public OpenModXmlReader(String in) throws DocumentException{
		this(new File(in));
	}
	
	public OpenModXmlReader(File in) throws DocumentException{
		SAXReader reader = new SAXReader();
		Document document = reader.read(in);
		this.root = document.getRootElement();
		this.read();
	}

	@SuppressWarnings("unchecked")
	private void read() {

		this.openMods = new OpenMod[Integer.parseInt(root.attributeValue("open_mod_count"))];
		this.openPSMs = new OpenPSM[Integer.parseInt(root.attributeValue("psm_count"))];
		this.setModMap = new HashMap<Double, PostTransModification>();
		
		this.openModMatrixMap = new HashMap<Integer, double[][]>();
		this.setModMatrixMap = new HashMap<Double, double[][]>();

		Element eSetModSummary = root.element("set_mod_summary");
		Iterator<Element> setModit = eSetModSummary.elementIterator();
		while (setModit.hasNext()) {

			Element emod = setModit.next();
			double modMass = Double.parseDouble(emod.attributeValue("ExpModMass"));
			String site = emod.attributeValue("site");
			boolean isVariable = Boolean.parseBoolean(emod.attributeValue("is_variable"));

			int foundUm = Integer.parseInt(emod.attributeValue("found_in_unimod"));

			if (foundUm == 0) {
				PostTransModification ptm = new PostTransModification(modMass, "", site, isVariable);
				this.setModMap.put(modMass, ptm);

			} else if (foundUm == 1) {
				List<Element> umlist = emod.elements("unimod");
				UmModification[] ums = new UmModification[umlist.size()];
				for (int i = 0; i < ums.length; i++) {
					Element eum = umlist.get(i);
					String title = eum.attributeValue("title");
					String name = eum.attributeValue("name");
					double monomass = Double.parseDouble(eum.attributeValue("mono_mass"));
					double avgmass = Double.parseDouble(eum.attributeValue("avg_mass"));

					List<Element> splist = eum.elements("specificity");
					String[] sites = new String[splist.size()];
					for (int j = 0; j < sites.length; j++) {
						sites[j] = splist.get(j).attributeValue("site");
					}

					List<Element> elist = eum.elements("Element");
					UmElement[] umes = new UmElement[elist.size()];
					int[] umecount = new int[elist.size()];
					for (int j = 0; j < umes.length; j++) {

						Element ee = elist.get(j);

						String etitle = ee.attributeValue("title");
						String ename = ee.attributeValue("name");
						double emonomass = Double.parseDouble(ee.attributeValue("mono_mass"));
						double eavgmass = Double.parseDouble(ee.attributeValue("avg_mass"));

						umecount[j] = Integer.parseInt(ee.attributeValue("count"));
						umes[j] = new UmElement(etitle, ename, emonomass, eavgmass);
					}

					ums[i] = new UmModification(title, name, sites, monomass, avgmass, umes, umecount);
				}

				PostTransModification ptm = new PostTransModification(ums, site, isVariable);
				this.setModMap.put(modMass, ptm);
			}

			Element eMotif = emod.element("motif");
			if (eMotif != null) {
				double[][] matrix = new double[26][15];
				for (int i = 0; i < matrix.length; i++) {
					Element eAA = eMotif.element("aa_" + ((char) (i + 'A')));
					for (int j = 0; j < matrix[i].length; j++) {
						matrix[i][j] = Double.parseDouble(eAA.attributeValue("p_" + (j - 7)));
					}
				}
				this.setModMatrixMap.put(modMass, matrix);
			}
		}

		Element eModSummary = root.element("mod_summary");

		Iterator<Element> modit = eModSummary.elementIterator();
		int openModId = 0;
		while (modit.hasNext()) {

			Element emod = modit.next();
			int id = Integer.parseInt(emod.attributeValue("id"));
			int monoId = Integer.parseInt(emod.attributeValue("monoId"));
			boolean isMonoIsotope = Boolean.parseBoolean(emod.attributeValue("isMonoIsotope"));

			double[] parameter = new double[5];
			parameter[0] = Double.parseDouble(emod.attributeValue("gaussian_a"));
			parameter[1] = Double.parseDouble(emod.attributeValue("gaussian_b"));
			parameter[2] = Double.parseDouble(emod.attributeValue("gaussian_c"));
			parameter[3] = Double.parseDouble(emod.attributeValue("ave_intensity"));
			parameter[4] = Double.parseDouble(emod.attributeValue("gaussian_r2"));
			double expModMass = Double.parseDouble(emod.attributeValue("ExpModMass"));

			int foundUm = Integer.parseInt(emod.attributeValue("found_in_unimod"));

			if (foundUm == 0) {
				openMods[openModId] = new OpenMod(id, parameter, expModMass, monoId);
				openMods[openModId].setMonoIsotope(isMonoIsotope);

			} else if (foundUm == 1) {
				List<Element> umlist = emod.elements("unimod");
				UmModification[] ums = new UmModification[umlist.size()];
				for (int i = 0; i < ums.length; i++) {
					Element eum = umlist.get(i);
					String title = eum.attributeValue("title");
					String name = eum.attributeValue("name");
					double monomass = Double.parseDouble(eum.attributeValue("mono_mass"));
					double avgmass = Double.parseDouble(eum.attributeValue("avg_mass"));

					List<Element> splist = eum.elements("specificity");
					String[] sites = new String[splist.size()];
					for (int j = 0; j < sites.length; j++) {
						sites[j] = splist.get(j).attributeValue("site");
					}

					List<Element> elist = eum.elements("Element");
					UmElement[] umes = new UmElement[elist.size()];
					int[] umecount = new int[elist.size()];
					for (int j = 0; j < umes.length; j++) {

						Element ee = elist.get(j);

						String etitle = ee.attributeValue("title");
						String ename = ee.attributeValue("name");
						double emonomass = Double.parseDouble(ee.attributeValue("mono_mass"));
						double eavgmass = Double.parseDouble(ee.attributeValue("avg_mass"));

						umecount[j] = Integer.parseInt(ee.attributeValue("count"));
						umes[j] = new UmElement(etitle, ename, emonomass, eavgmass);
					}

					ums[i] = new UmModification(title, name, sites, monomass, avgmass, umes, umecount);
				}

				openMods[openModId] = new OpenMod(id, parameter, expModMass, monoId, ums);
				openMods[openModId].setMonoIsotope(isMonoIsotope);
			}

			if (emod.attributeValue("bScore") != null) {
				openMods[openModId].setBscore(Double.parseDouble(emod.attributeValue("bScore")));
			}
			if (emod.attributeValue("yScore") != null) {
				openMods[openModId].setYscore(Double.parseDouble(emod.attributeValue("yScore")));
			}
			if (emod.attributeValue("bModScore") != null) {
				openMods[openModId].setbModScore(Double.parseDouble(emod.attributeValue("bModScore")));
			}
			if (emod.attributeValue("yModScore") != null) {
				openMods[openModId].setyModScore(Double.parseDouble(emod.attributeValue("yModScore")));
			}
			if (emod.attributeValue("nlMass") != null) {
				openMods[openModId].setNeutralLossMass(Double.parseDouble(emod.attributeValue("nlMass")));
			}
			if (emod.attributeValue("nlScore") != null) {
				openMods[openModId].setNeutralLossScore(Double.parseDouble(emod.attributeValue("nlScore")));
			}
			if (emod.attributeValue("nlRatio") != null) {
				openMods[openModId].setNeutralLossRatio(Double.parseDouble(emod.attributeValue("nlRatio")));
			}
			openModId++;

			Element eMotif = emod.element("motif");
			if (eMotif != null) {
				double[][] matrix = new double[26][15];
				for (int i = 0; i < matrix.length; i++) {
					Element eAA = eMotif.element("aa_" + ((char) (i + 'A')));
					for (int j = 0; j < matrix[i].length; j++) {
						matrix[i][j] = Double.parseDouble(eAA.attributeValue("p_" + (j - 7)));
					}
				}
				this.openModMatrixMap.put(id, matrix);
			}
		}

		/*Element ePSMSummary = root.element("peptide_spectra_matches");
		Iterator<Element> psmit = ePSMSummary.elementIterator();
		int psmId = 0;
		while (psmit.hasNext()) {

			Element epsm = psmit.next();
			String fileName = epsm.attributeValue("fileName");
			int scan = Integer.parseInt(epsm.attributeValue("scan"));
			int charge = Integer.parseInt(epsm.attributeValue("charge"));
			double precursorMr = Double.parseDouble(epsm.attributeValue("precursorMr"));
			double rt = Double.parseDouble(epsm.attributeValue("rt"));

			String sequence = epsm.attributeValue("sequence");
			double pepMass = Double.parseDouble(epsm.attributeValue("pepMass"));
			double massDiff = Double.parseDouble(epsm.attributeValue("massDiff"));
			int miss = Integer.parseInt(epsm.attributeValue("miss"));
			int num_tol_term = Integer.parseInt(epsm.attributeValue("num_tol_term"));
			int tot_num_ions = Integer.parseInt(epsm.attributeValue("tot_num_ions"));
			int num_matched_ions = Integer.parseInt(epsm.attributeValue("num_matched_ions"));
			String protein = epsm.attributeValue("protein");

			double hyperscore = Double.parseDouble(epsm.attributeValue("hyperscore"));
			double nextscore = Double.parseDouble(epsm.attributeValue("nextscore"));
			double expect = Double.parseDouble(epsm.attributeValue("expect"));

			double open_mod_mass = Double.parseDouble(epsm.attributeValue("open_mod_mass"));
			double delta_open_mod_mass = Double.parseDouble(epsm.attributeValue("delta_open_mod_mass"));

			double b_intensity = Double.parseDouble(epsm.attributeValue("b_intensity"));
			double y_intensity = Double.parseDouble(epsm.attributeValue("y_intensity"));
			double b_rankscore = Double.parseDouble(epsm.attributeValue("b_rankscore"));
			double y_rankscore = Double.parseDouble(epsm.attributeValue("y_rankscore"));
			int b_ion_count = Integer.parseInt(epsm.attributeValue("b_ion_count"));
			int y_ion_count = Integer.parseInt(epsm.attributeValue("y_ion_count"));
			double fragment_delta_mass = Double.parseDouble(epsm.attributeValue("fragment_delta_mass"));

			double b_mod_intensity = Double.parseDouble(epsm.attributeValue("b_mod_intensity"));
			double y_mod_intensity = Double.parseDouble(epsm.attributeValue("y_mod_intensity"));
			double b_mod_rankscore = Double.parseDouble(epsm.attributeValue("b_mod_rankscore"));
			double y_mod_rankscore = Double.parseDouble(epsm.attributeValue("y_mod_rankscore"));
			double mod_fragment_delta_mass = Double.parseDouble(epsm.attributeValue("mod_fragment_delta_mass"));

			int mod_id = Integer.parseInt(epsm.attributeValue("mod_id"));
			int[] possible_loc = null;
			if (mod_id >= 0) {
				String[] locstr = epsm.attributeValue("possible_loc").split("_");
				possible_loc = new int[locstr.length];
				for (int i = 0; i < possible_loc.length; i++) {
					possible_loc[i] = Integer.parseInt(locstr[i]);
				}
			}
			boolean is_target = Boolean.parseBoolean(epsm.attributeValue("is_target"));
			double classScore = Double.parseDouble(epsm.attributeValue("classify_score"));

			openPSMs[psmId] = new OpenPSM(fileName, scan, charge, precursorMr, rt, sequence, pepMass, massDiff, miss,
					num_tol_term, tot_num_ions, num_matched_ions, protein, hyperscore, nextscore, expect, open_mod_mass,
					delta_open_mod_mass, b_intensity, y_intensity, b_rankscore, y_rankscore, b_ion_count, y_ion_count,
					fragment_delta_mass, b_mod_intensity, y_mod_intensity, b_mod_rankscore, y_mod_rankscore,
					mod_fragment_delta_mass, mod_id, possible_loc, is_target, classScore);

			psmId++;
		}*/
	}

	public OpenMod[] getOpenMods() {
		return openMods;
	}

	public OpenPSM[] getOpenPSMs() {
		return openPSMs;
	}
	
	public HashMap<Double, PostTransModification> getSetModMap() {
		return setModMap;
	}

	public HashMap<Integer, double[][]> getOpenModMatrixMap() {
		return openModMatrixMap;
	}

	public HashMap<Double, double[][]> getSetModMatrixMap() {
		return setModMatrixMap;
	}
	
	public static void updateUnimod(String in, String out) throws DocumentException, IOException {
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] mods = reader.getOpenMods();
		HashMap<Integer, double[][]> matrixMap = reader.getOpenModMatrixMap();
		HashMap<Double, PostTransModification> setModMap = reader.getSetModMap();
		HashMap<Double, double[][]> setModMatrixMap = reader.getSetModMatrixMap();

		UnimodReader umreader = new UnimodReader();
		UmModification[] ummods = umreader.getUmModifications();

		double modMassTolerance = 0.01;
		OpenMod mod0 = null;
		for (int i = 0; i < mods.length; i++) {
			if (Math.abs(mods[i].getExpModMass()) < modMassTolerance) {
				mod0 = mods[i];
			} else {
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(mods[i].getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					mods[i].setUmmod(ums);
				}
			}
		}

		OpenModXmlWriter writer = new OpenModXmlWriter(out);
		writer.addMods(mods, matrixMap);
		writer.addSetMods(setModMap, mod0, setModMatrixMap);
		writer.close();
	}
	
	public static void updateUnimod(String in, String out, String unimod) throws DocumentException, IOException {
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] mods = reader.getOpenMods();
		HashMap<Integer, double[][]> matrixMap = reader.getOpenModMatrixMap();
		HashMap<Double, PostTransModification> setModMap = reader.getSetModMap();
		HashMap<Double, double[][]> setModMatrixMap = reader.getSetModMatrixMap();

		UnimodReader umreader = new UnimodReader(unimod);
		UmModification[] ummods = umreader.getUmModifications();

		double modMassTolerance = 0.01;
		OpenMod mod0 = null;
		for (int i = 0; i < mods.length; i++) {
			if (Math.abs(mods[i].getExpModMass()) < modMassTolerance) {
				mod0 = mods[i];
			} else {
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(mods[i].getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					mods[i].setUmmod(ums);
				}
			}
		}

		OpenModXmlWriter writer = new OpenModXmlWriter(out);
		writer.addMods(mods, matrixMap);
		writer.addSetMods(setModMap, mod0, setModMatrixMap);
		writer.close();
	}
	
	private void getModMotif(int id) {
		double[][] matrix = this.openModMatrixMap.get(id);
		if (matrix != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = -7; i <= 7; i++) {
				if (i < 0) {
					sb.append("\t").append("_").append(String.valueOf(Math.abs(i)));
				} else {
					sb.append("\t").append(String.valueOf(i));
				}
			}
			sb.append("\n");

			for (int i = 0; i < matrix.length; i++) {
				sb.append((char) (i + 'A')).append("\t");
				for (int j = 0; j < matrix[i].length; j++) {
					sb.append(matrix[i][j]).append("\t");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
			}
			System.out.print(sb);
		}
	}

	private static void test(String in) throws DocumentException {

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenPSM[] psms = reader.getOpenPSMs();
		OpenMod[] mods = reader.getOpenMods();
		System.out.println(psms.length + "\t" + mods.length);

		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());

		HashSet<String> set = new HashSet<String>();
		int target = 0;
		int decoy = 0;
		boolean thres = true;
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}
set.add(psms[i].getSequence());
			/*if ((target + decoy) % 10000 == 0) {
				System.out.println(target + decoy);
			}

			double fdr = (double) decoy / (double) target;

			if (thres) {
				if (fdr > 0.01) {
					System.out.println(i + "\t" + target + "\t" + decoy + "\t" + fdr);
					thres = false;
				}
			} else {
				if (fdr < 0.01) {
					thres = true;
				}
			}*/
		}
		
		System.out.println(set.size());
	}
	
	private static void partTest(String in) throws DocumentException {

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] mods = reader.getOpenMods();
		OpenPSM[] psms = reader.getOpenPSMs();

		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
		
		HashMap<Integer, int[]> countmap = new HashMap<Integer, int[]>();

		int target = 0;
		int decoy = 0;
		boolean thres = true;
		for (int i = 0; i < psms.length; i++) {

			int modid = psms[i].getOpenModId();

			if (countmap.containsKey(modid)) {
				if (psms[i].isTarget()) {
					countmap.get(modid)[0]++;
				} else {
					countmap.get(modid)[1]++;
				}
			} else {
				int[] ccc = new int[2];
				if (psms[i].isTarget()) {
					ccc[0]++;
				} else {
					ccc[1]++;
				}
				countmap.put(modid, ccc);
			}

			if (psms[i].getOpenModId() == -2) {
				continue;
			}

			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}

			if ((target + decoy) % 10000 == 0) {
				System.out.println(target + decoy);
			}

			double fdr = (double) decoy / (double) target;

			if (thres) {
				if (fdr > 0.01) {
					System.out.println(i + "\t" + target + "\t" + decoy + "\t" + fdr);
					thres = false;
				}
			} else {
				if (fdr < 0.01) {
					thres = true;
				}
			}
		}
		
		System.out.println("count\t"+target+"\t"+decoy);
		
		for (Integer modid : countmap.keySet()) {
			int[] count = countmap.get(modid);
			if (modid >= 0) {
				System.out.println(modid + "\t" + mods[modid].getExpModMass() + "\t" + (count[0] + count[1]) + "\t"
						+ count[0] + "\t" + count[1] + "\t" + (double) count[1] / (double) count[0]);
			} else {
				System.out.println(modid + "\t" + "\t" + (count[0] + count[1]) + "\t" + count[0] + "\t" + count[1]
						+ "\t" + (double) count[1] / (double) count[0]);
			}
		}
	}
	
	private static void test(String in, String out) throws DocumentException, IOException {
		
		OpenModXmlWriter writer = new OpenModXmlWriter(out);
		OpenModXmlReader reader = new OpenModXmlReader(in);
		writer.addMods(reader.getOpenMods());

		OpenPSM[] psms = reader.getOpenPSMs();

		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());

		int target = 0;
		int decoy = 0;
		boolean thres = true;
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}

			if ((target + decoy) % 10000 == 0) {
				System.out.println(target + decoy);
			}

			double fdr = (double) decoy / (double) target;

			if (thres) {
				if (fdr > 0.01) {
					System.out.println(i + "\t" + target + "\t" + decoy + "\t" + fdr);
					thres = false;
					
					
					break;
				}
			} else {
				if (fdr < 0.01) {
					thres = true;
				}
			}
			
			writer.addPSM(psms[i]);
		}
		
		writer.close();
	}
	
	private static void learnSemi(String in) throws Exception {

		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		attributeList.add(new Attribute("charge"));
		attributeList.add(new Attribute("pepMass"));
		attributeList.add(new Attribute("pepLen"));

		attributeList.add(new Attribute("premr"));
		attributeList.add(new Attribute("massDiff"));
		attributeList.add(new Attribute("massDiffPPM"));

		attributeList.add(new Attribute("miss"));
		attributeList.add(new Attribute("num_tol_term"));
		attributeList.add(new Attribute("tot_num_ions"));
		attributeList.add(new Attribute("num_matched_ions"));

		attributeList.add(new Attribute("hyperscore"));
		attributeList.add(new Attribute("nextscore"));
		attributeList.add(new Attribute("deltascore"));
		attributeList.add(new Attribute("log10expect"));

		attributeList.add(new Attribute("bintensity"));
		attributeList.add(new Attribute("yintensity"));
		attributeList.add(new Attribute("brankscore"));
		attributeList.add(new Attribute("yrankscore"));
		attributeList.add(new Attribute("bcount"));
		attributeList.add(new Attribute("ycount"));
		attributeList.add(new Attribute("fragDeltaMass"));

		attributeList.add(new Attribute("modMass"));
		attributeList.add(new Attribute("bmodIntensity"));
		attributeList.add(new Attribute("ymodIntensity"));
		attributeList.add(new Attribute("bmodrankscore"));
		attributeList.add(new Attribute("ymodrankscore"));
		attributeList.add(new Attribute("fragModDeltaMass"));
		
		attributeList.add(new Attribute("sameModCount"));
		attributeList.add(new Attribute("samePepCount"));
		attributeList.add(new Attribute("sameProCount"));

		ArrayList<String> classList = new ArrayList<String>();
		classList.add("P");
		classList.add("N");
		Attribute classAattr = new Attribute("class", classList);
		attributeList.add(classAattr);

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] mods = reader.getOpenMods();
		OpenPSM[] psms = reader.getOpenPSMs();
		double[] hyperscores = new double[psms.length];
		int[] modIds = new int[psms.length];
		String[] peps = new String[psms.length];
		String[] pros = new String[psms.length];
		int[] totalPepCount = new int[psms.length];
		int[] totalProCount = new int[psms.length];
		
		HashMap<Integer, Integer> modcountmap = new HashMap<Integer, Integer>();
		HashMap<String, Integer> pepcountmap = new HashMap<String, Integer>();
		HashMap<String, Integer> procountmap = new HashMap<String, Integer>();

		Instances instances = new Instances("PSM Instances", attributeList, psms.length);
		for (int i = 0; i < psms.length; i++) {

			hyperscores[i] = psms[i].getHyperscore();

			modIds[i] = psms[i].getOpenModId();
			if (modcountmap.containsKey(modIds[i])) {
				modcountmap.put(modIds[i], modcountmap.get(modIds[i]) + 1);
			} else {
				modcountmap.put(modIds[i], 1);
			}

			peps[i] = psms[i].getSequence();
			if (pepcountmap.containsKey(peps[i])) {
				pepcountmap.put(peps[i], pepcountmap.get(peps[i]) + 1);
			} else {
				pepcountmap.put(peps[i], 1);
			}

			pros[i] = psms[i].getProtein();
			if (procountmap.containsKey(pros[i])) {
				procountmap.put(pros[i], procountmap.get(pros[i]) + 1);
			} else {
				procountmap.put(pros[i], 1);
			}
		}
		
		for (int i = 0; i < psms.length; i++) {
			totalPepCount[i] = pepcountmap.get(psms[i].getSequence());
			totalProCount[i] = procountmap.get(psms[i].getProtein());
			psms[i].setSameModCount(modcountmap.get(psms[i].getOpenModId()));
			psms[i].setSamePepCount(totalPepCount[i]);
			psms[i].setSameProCount(totalProCount[i]);

			instances.add(psms[i].createSemisuperInstance(attributeList, mods[psms[i].getOpenModId()]));
		}
		
		instances.setClassIndex(instances.numAttributes() - 1);

		StringBuilder sb = new StringBuilder();

		Arrays.sort(hyperscores);
		int thresId = (int) (hyperscores.length * 0.75);
		double threshold = hyperscores[thresId];

		SemiSuperClassifier classifer = new SemiSuperClassifier(attributeList);

		double[] scores = classifer.classify(instances, 10, threshold, modIds, peps, pros, totalPepCount, totalProCount);

		for (int i = 0; i < psms.length; i++) {
			psms[i].setClassScore(scores[i]);
		}

		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());

		int target = 0;
		int decoy = 0;
		boolean thres = true;
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}

			if ((target + decoy) % 10000 == 0) {
				System.out.println(target + decoy);
			}

			double fdr = (double) decoy / (double) target;

			if (thres) {
				if (fdr > 0.01) {
					System.out.println(i + "\t" + target + "\t" + decoy + "\t" + fdr);
					thres = false;
				}
			} else {
				if (fdr < 0.01) {
					thres = true;
				}
			}
		}
	}
	
	private static void learnTest(String in) throws Exception {

		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		attributeList.add(new Attribute("charge"));
		attributeList.add(new Attribute("pepMass"));
		attributeList.add(new Attribute("pepLen"));

		attributeList.add(new Attribute("premr"));
		attributeList.add(new Attribute("massDiff"));
		attributeList.add(new Attribute("massDiffPPM"));

		attributeList.add(new Attribute("miss"));
		attributeList.add(new Attribute("num_tol_term"));
		attributeList.add(new Attribute("tot_num_ions"));
		attributeList.add(new Attribute("num_matched_ions"));

		attributeList.add(new Attribute("hyperscore"));
		attributeList.add(new Attribute("nextscore"));
		attributeList.add(new Attribute("deltascore"));
		attributeList.add(new Attribute("log10expect"));

		attributeList.add(new Attribute("bintensity"));
		attributeList.add(new Attribute("yintensity"));
		attributeList.add(new Attribute("brankscore"));
		attributeList.add(new Attribute("yrankscore"));
		attributeList.add(new Attribute("bcount"));
		attributeList.add(new Attribute("ycount"));
		attributeList.add(new Attribute("fragDeltaMass"));

		attributeList.add(new Attribute("modMass"));
		attributeList.add(new Attribute("bmodIntensity"));
		attributeList.add(new Attribute("ymodIntensity"));
		attributeList.add(new Attribute("bmodrankscore"));
		attributeList.add(new Attribute("ymodrankscore"));
		attributeList.add(new Attribute("fragModDeltaMass"));
		attributeList.add(new Attribute("sameModCount"));

		/*ArrayList<String> classList = new ArrayList<String>();
		classList.add("P");
		classList.add("N");
		Attribute classAattr = new Attribute("class", classList);
		attributeList.add(classAattr);
*/
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenPSM[] psms = reader.getOpenPSMs();
		double[] hyperscores = new double[psms.length];
		HashMap<Integer, Integer> modcountmap = new HashMap<Integer, Integer>();
		
		ArrayList<Boolean> blist = new ArrayList<Boolean>();
		Instances instances = new Instances("PSM Instances", attributeList, psms.length);
		for (int i = 0; i < psms.length; i++) {
			blist.add(psms[i].isTarget());
			hyperscores[i] = psms[i].getHyperscore();
			int modid = psms[i].getOpenModId();
			if (modcountmap.containsKey(modid)) {
				modcountmap.put(modid, modcountmap.get(modid) + 1);
			} else {
				modcountmap.put(modid, 1);
			}
		}
		
		for (int i = 0; i < psms.length; i++) {
//			psms[i].setSameModCount(modcountmap.get(psms[i].getOpenModId()));
//			instances.add(psms[i].createSemisuperInstance(attributeList));
			instances.add(psms[i].createUnsuperInstance(attributeList));
		}
		
//		instances.setClassIndex(instances.numAttributes() - 1);

		StringBuilder sb = new StringBuilder();

		UnsuperCluster cluster = new UnsuperCluster(attributeList);
		cluster.cluster2(instances, blist);

		/*Arrays.sort(hyperscores);
		int thresId = (int) (hyperscores.length * 0.75);
		double threshold = hyperscores[thresId];

		SemiSuperClassifier classifer = new SemiSuperClassifier(attributeList, sb);
		double[] scores = classifer.classify(instances, 10, threshold);
*/
		double[] scores = cluster.getScores();

		for (int i = 0; i < psms.length; i++) {
			psms[i].setClassScore(scores[i]);
		}

		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());

		int target = 0;
		int decoy = 0;
		boolean thres = true;
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}

			if ((target + decoy) % 10000 == 0) {
				System.out.println(target + decoy);
			}

			double fdr = (double) decoy / (double) target;

			if (thres) {
				if (fdr > 0.01) {
					System.out.println(i + "\t" + target + "\t" + decoy + "\t" + fdr);
					thres = false;
				}
			} else {
				if (fdr < 0.01) {
					thres = true;
				}
			}
		}
	}

	private static void statistic(String in) throws DocumentException {

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] mods = reader.getOpenMods();
		OpenPSM[] psms = reader.getOpenPSMs();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		// for(int i=0;i<mods.length;i++){
		// System.out.println(i+"\t"+mods[i].getId()+"\t"+mods[i].getExpModMass());
		// }

		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < psms.length; i++) {
			int modid = psms[i].getOpenModId();
			if (map.containsKey(modid)) {
				map.put(modid, map.get(modid) + 1);
			} else {
				map.put(modid, 1);
			}
			set.add(psms[i].getSequence());
		}

		System.out.println(set.size());

		Integer[] keys = map.keySet().toArray(new Integer[map.size()]);
		Arrays.sort(keys);

		HashMap<Double, Integer> massmap = new HashMap<Double, Integer>();

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == -1) {
				System.out.println(i + "\t" + keys[i] + "\t" + map.get(keys[i]));
				
				massmap.put(0.0, map.get(keys[i]));
				
			} else {
				System.out.println(i + "\t" + keys[i] + "\t" + mods[keys[i]].getExpModMass() + "\t"
						+ (mods[keys[i]].getUmmod() != null) + "\t" + map.get(keys[i]));
				
				massmap.put(mods[keys[i]].getExpModMass(), map.get(keys[i]));
			}
		}
		
		int[] ppmcount = new int[100];
		int t = 0;
		int d = 0;
		for (int i = 0; i < psms.length; i++) {
			
//			int modid = psms[i].getOpenModId();
//			if(modid==0){
//				continue;
//			}
			
			if(psms[i].getMassDiffPPM()>20){
				continue;
			}
			
			if(psms[i].isTarget()){
				t++;
				
				double ppm = psms[i].getMassDiffPPM();
				int id = (int) ppm;
				if (id < 0) {
					ppmcount[0]++;
				} else if (id >= 100) {
					ppmcount[99]++;
				} else {
					ppmcount[id]++;
				}
				
			}else{
				d++;
			}
			
			double fdr = (double)d/(double)t;
			
//			System.out.println(t+"\t"+d+"\t"+fdr);
			
			if(fdr>0.01){
				break;
			}
			
			
		}
		
		for (int i = 0; i < ppmcount.length; i++) {
//			System.out.println((i+1) + "\t" + ppmcount[i]);
		}
	}
	
	private static void nomodTest(String in, String guassian) throws DocumentException {

		OpenModXmlReader guassianreader = new OpenModXmlReader(guassian);
		OpenMod[] mods = guassianreader.getOpenMods();
		OpenPSM[] psms = guassianreader.getOpenPSMs();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < psms.length; i++) {
			int modid = psms[i].getOpenModId();
			if (map.containsKey(modid)) {
				map.put(modid, map.get(modid) + 1);
			} else {
				map.put(modid, 1);
			}
			set.add(psms[i].getSequence());
		}

		System.out.println(set.size());

		Integer[] keys = map.keySet().toArray(new Integer[map.size()]);
		Arrays.sort(keys);

		HashMap<Double, Integer> massmap = new HashMap<Double, Integer>();
		double[] massdiff = new double[keys.length];

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == -1) {
				System.out.println(i + "\t" + keys[i] + "\t" + map.get(keys[i]));
				massdiff[i] = 0.0;
				massmap.put(0.0, map.get(keys[i]));

			} else if (keys[i] == -2) {
//				System.out.println(i + "\t" + keys[i] + "\t" + map.get(keys[i]));
//				massdiff[i] = 0.0;
//				massmap.put(0.0, map.get(keys[i]));

			} else {
				System.out.println(i + "\t" + keys[i] + "\t" + mods[keys[i]].getExpModMass() + "\t"
						+ (mods[keys[i]].getUmmod() != null) + "\t" + map.get(keys[i]));
				massdiff[i] = mods[keys[i]].getExpModMass();
				massmap.put(mods[keys[i]].getExpModMass(), map.get(keys[i]));
			}
		}

		SAXReader reader = new SAXReader();
		Document document = reader.read(in);
		Element root = document.getRootElement();

		OpenPSM[] openPSMs = new OpenPSM[Integer.parseInt(root.attributeValue("psm_count"))];

		HashMap<Double, Integer> massmap2 = new HashMap<Double, Integer>();
		Arrays.sort(massdiff);

		Element ePSMSummary = root.element("peptide_spectra_matches");
		Iterator<Element> psmit = ePSMSummary.elementIterator();
		int psmId = 0;
		while (psmit.hasNext()) {

			Element epsm = psmit.next();
			String fileName = epsm.attributeValue("fileName");
			int scan = Integer.parseInt(epsm.attributeValue("scan"));
			int charge = Integer.parseInt(epsm.attributeValue("charge"));
			double precursorMr = Double.parseDouble(epsm.attributeValue("precursorMr"));
			double rt = Double.parseDouble(epsm.attributeValue("rt"));

			String sequence = epsm.attributeValue("sequence");
			double pepMass = Double.parseDouble(epsm.attributeValue("pepMass"));
			double massDiff = Double.parseDouble(epsm.attributeValue("massDiff"));
			int miss = Integer.parseInt(epsm.attributeValue("miss"));
			int num_tol_term = Integer.parseInt(epsm.attributeValue("num_tol_term"));
			int tot_num_ions = Integer.parseInt(epsm.attributeValue("tot_num_ions"));
			int num_matched_ions = Integer.parseInt(epsm.attributeValue("num_matched_ions"));
			String protein = epsm.attributeValue("protein");

			double hyperscore = Double.parseDouble(epsm.attributeValue("hyperscore"));
			double nextscore = Double.parseDouble(epsm.attributeValue("nextscore"));
			double expect = Double.parseDouble(epsm.attributeValue("expect"));

			double open_mod_mass = Double.parseDouble(epsm.attributeValue("open_mod_mass"));
			double delta_open_mod_mass = Double.parseDouble(epsm.attributeValue("delta_open_mod_mass"));

			double b_intensity = Double.parseDouble(epsm.attributeValue("b_intensity"));
			double y_intensity = Double.parseDouble(epsm.attributeValue("y_intensity"));
			double b_rankscore = Double.parseDouble(epsm.attributeValue("b_rankscore"));
			double y_rankscore = Double.parseDouble(epsm.attributeValue("y_rankscore"));
			int b_ion_count = Integer.parseInt(epsm.attributeValue("b_ion_count"));
			int y_ion_count = Integer.parseInt(epsm.attributeValue("y_ion_count"));
			double fragment_delta_mass = Double.parseDouble(epsm.attributeValue("fragment_delta_mass"));

			double b_mod_intensity = Double.parseDouble(epsm.attributeValue("b_mod_intensity"));
			double y_mod_intensity = Double.parseDouble(epsm.attributeValue("y_mod_intensity"));
			double b_mod_rankscore = Double.parseDouble(epsm.attributeValue("b_mod_rankscore"));
			double y_mod_rankscore = Double.parseDouble(epsm.attributeValue("y_mod_rankscore"));
			double mod_fragment_delta_mass = Double.parseDouble(epsm.attributeValue("mod_fragment_delta_mass"));

			int mod_id = Integer.parseInt(epsm.attributeValue("mod_id"));
			int[] possible_loc = null;
			if (mod_id >= 0) {
				String[] locstr = epsm.attributeValue("possible_loc").split("_");
				possible_loc = new int[locstr.length];
				for (int i = 0; i < possible_loc.length; i++) {
					possible_loc[i] = Integer.parseInt(locstr[i]);
				}
			}
			boolean is_target = Boolean.parseBoolean(epsm.attributeValue("is_target"));
			double classScore = Double.parseDouble(epsm.attributeValue("classify_score"));

			openPSMs[psmId] = new OpenPSM(fileName, scan, charge, precursorMr, rt, sequence, pepMass, massDiff, miss,
					num_tol_term, tot_num_ions, num_matched_ions, protein, hyperscore, nextscore, expect, open_mod_mass,
					delta_open_mod_mass, b_intensity, y_intensity, b_rankscore, y_rankscore, b_ion_count, y_ion_count,
					fragment_delta_mass, b_mod_intensity, y_mod_intensity, b_mod_rankscore, y_mod_rankscore,
					mod_fragment_delta_mass, mod_id, possible_loc, 0, 0, is_target, classScore);

			psmId++;

			int id = Arrays.binarySearch(massdiff, open_mod_mass);
			if (id < 0) {
				id = -id - 1;
			}

			double tolerance = 0.02;

			if (id == 0) {
				if (Math.abs(open_mod_mass - massdiff[id]) < tolerance) {
					if (massmap2.containsKey(massdiff[id])) {
						massmap2.put(massdiff[id], massmap2.get(massdiff[id]) + 1);
					} else {
						massmap2.put(massdiff[id], 1);
					}
				}
			} else if (id == massdiff.length) {
				if (Math.abs(open_mod_mass - massdiff[id - 1]) < tolerance) {
					if (massmap2.containsKey(massdiff[id - 1])) {
						massmap2.put(massdiff[id - 1], massmap2.get(massdiff[id - 1]) + 1);
					} else {
						massmap2.put(massdiff[id - 1], 1);
					}
				}
			} else {
				if (Math.abs(open_mod_mass - massdiff[id]) < tolerance) {
					if (massmap2.containsKey(massdiff[id])) {
						massmap2.put(massdiff[id], massmap2.get(massdiff[id]) + 1);
					} else {
						massmap2.put(massdiff[id], 1);
					}
				} else {
					if (Math.abs(open_mod_mass - massdiff[id - 1]) < tolerance) {
						if (massmap2.containsKey(massdiff[id - 1])) {
							massmap2.put(massdiff[id - 1], massmap2.get(massdiff[id - 1]) + 1);
						} else {
							massmap2.put(massdiff[id - 1], 1);
						}
					}
				}
			}
		}

		for (double modmass : massdiff) {
			int count2 = massmap2.containsKey(modmass) ? massmap2.get(modmass) : 0;
			System.out.println(modmass + "\t" + massmap.get(modmass) + "\t" + count2);
		}

	}
	
	private static void count(String in) throws DocumentException {
		HashSet<String> set = new HashSet<String>();
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenPSM[] psms = reader.getOpenPSMs();
		for (int i = 0; i < psms.length; i++) {
			set.add(psms[i].getSequence());
		}
		System.out.println(psms.length + "\t" + set.size());
		
		OpenMod[] mods = reader.getOpenMods();
		for (int i = 0; i < mods.length; i++) {
			if(mods[i].getUmmod()!=null){
				System.out.println(mods[i].getExpModMass()+"\t"+mods[i].getUmmod().length);
			}
		}
		System.out.println(mods.length);
	}
	
	private static void countNoMod(String in) throws DocumentException{
		SAXReader reader = new SAXReader();
		Document document = reader.read(in);
		Element root = document.getRootElement();

		OpenPSM[] openPSMs = new OpenPSM[Integer.parseInt(root.attributeValue("psm_count"))];
		System.out.println(openPSMs.length);
	}
	
	private static void compare(String s1, String s2) throws DocumentException {
		HashMap<Double, OpenMod> modmap = new HashMap<Double, OpenMod>();
		OpenModXmlReader reader1 = new OpenModXmlReader(s1);
		OpenPSM[] psms1 = reader1.getOpenPSMs();
		OpenMod[] mods1 = reader1.getOpenMods();
		HashMap<String, OpenPSM> map1 = new HashMap<String, OpenPSM>();
		HashMap<Double, Integer> modmap1 = new HashMap<Double, Integer>();
		int zcount1 = 0;
		for (OpenPSM op : psms1) {
			map1.put(op.getFileName() + "_" + op.getScan(), op);
			modmap.put(op.getOpenModmass(), mods1[op.getOpenModId()]);

			double modmass = op.getOpenModmass();
			if (modmap1.containsKey(modmass)) {
				modmap1.put(modmass, modmap1.get(modmass) + 1);
			} else {
				modmap1.put(modmass, 1);
			}
		}

		OpenModXmlReader reader2 = new OpenModXmlReader(s2);
		OpenPSM[] psms2 = reader2.getOpenPSMs();
		HashMap<String, OpenPSM> map2 = new HashMap<String, OpenPSM>();
		HashMap<Double, Integer> modmap2 = new HashMap<Double, Integer>();
		int zcount2 = 0;
		for (OpenPSM op : psms2) {
			map2.put(op.getFileName() + "_" + op.getScan(), op);
			double modmass = op.getOpenModmass();
			if (modmap2.containsKey(modmass)) {
				modmap2.put(modmass, modmap2.get(modmass) + 1);
			} else {
				modmap2.put(modmass, 1);
			}
		}

		HashSet<String> set = new HashSet<String>();
		set.addAll(map1.keySet());
		set.addAll(map2.keySet());

		System.out.println(psms1.length + "\t" + psms2.length + "\t" + map1.size() + "\t" + map2.size() + "\t"
				+ set.size() + "\t" + zcount1 + "\t" + zcount2);

		HashSet<Double> modkey = new HashSet<Double>();
		modkey.addAll(modmap1.keySet());
		modkey.addAll(modmap2.keySet());
		
		for (Double modmass : modkey) {
			if (modmap1.containsKey(modmass) && modmap2.containsKey(modmass)) {
				if (modmap.containsKey(modmass)) {
					OpenMod om = modmap.get(modmass);
					UmModification[] ums = om.getUmmod();
					if (ums != null && ums.length > 0) {
						System.out.print(modmass + "\t" + modmap1.get(modmass) + "\t" + modmap2.get(modmass) + "\t");
						for (int i = 0; i < ums.length; i++) {
							System.out.print(ums[i].getName() + ";");
						}
						System.out.println();
					} else {
						System.out.println(
								modmass + "\t" + modmap1.get(modmass) + "\t" + modmap2.get(modmass) + "\t" + "Unknown");
					}
				}
			}
		}
	}
	
	private static void modCompare(String s1, String s2) throws DocumentException {
		HashMap<Double, OpenMod> modmap = new HashMap<Double, OpenMod>();
		OpenModXmlReader reader1 = new OpenModXmlReader(s1);
		OpenPSM[] psms1 = reader1.getOpenPSMs();
		OpenMod[] mods1 = reader1.getOpenMods();
		double[][] parameters = new double[mods1.length][];
		for(int i=0;i<mods1.length;i++){
			parameters[i] = mods1[i].getGaussianParameters();
		}
		
		HashMap<String, OpenPSM> map1 = new HashMap<String, OpenPSM>();
		HashMap<Double, Integer> modmap1 = new HashMap<Double, Integer>();
		for (OpenPSM op : psms1) {
			if(op.isTarget()){
				map1.put(op.getFileName() + "_" + op.getScan(), op);
				modmap.put(op.getOpenModmass(), mods1[op.getOpenModId()]);

				double modmass = op.getOpenModmass();
				if (modmap1.containsKey(modmass)) {
					modmap1.put(modmass, modmap1.get(modmass) + 1);
				} else {
					modmap1.put(modmass, 1);
				}
			}
		}

		OpenModXmlReader reader2 = new OpenModXmlReader(s2);
		OpenPSM[] psms2 = reader2.getOpenPSMs();
		HashMap<String, OpenPSM> map2 = new HashMap<String, OpenPSM>();
		HashMap<Double, Integer> modmap2 = new HashMap<Double, Integer>();
		for (OpenPSM op : psms2) {
			map2.put(op.getFileName() + "_" + op.getScan(), op);
			double modmass = op.getOpenModmass();
			if (modmap2.containsKey(modmass)) {
				modmap2.put(modmass, modmap2.get(modmass) + 1);
			} else {
				modmap2.put(modmass, 1);
			}
		}
		System.out.println(modmap1.size()+"\t"+modmap2.size());
		
		HashSet<String> set = new HashSet<String>();
		set.addAll(map1.keySet());
		set.addAll(map2.keySet());
		System.out.println(
				psms1.length + "\t" + psms2.length + "\t" + map1.size() + "\t" + map2.size() + "\t" + set.size());
		
		int [] mcount = new int[2];
		int[][] m1pcount = new int[mods1.length][2];
		for (String key : set) {
			if (map1.containsKey(key)) {
				OpenPSM op1 = map1.get(key);
				double md1 = op1.getOpenModmass();
				int id1 = (int) ((md1 + 500) / (500 * 2) * 1000000);

				for (int i = 0; i < parameters.length; i++) {
					if (id1 >= parameters[i][3] && id1 < parameters[i][4]) {
						m1pcount[i][0]++;
					}
				}
			}
			if (map2.containsKey(key)) {
				OpenPSM op2 = map2.get(key);
				double md2 = op2.getOpenModmass();
				int id2 = (int) ((md2 + 500) / (500 * 2) * 1000000);

				for (int i = 0; i < parameters.length; i++) {

					if (id2 >= parameters[i][3] && id2 < parameters[i][4]) {
						m1pcount[i][1]++;
					}
				}
			}
		}
		System.out.println(mcount[0]+"\t"+mcount[1]);
		
		for(int i=0;i<m1pcount.length;i++){
			System.out.println(parameters[i][1]+"\t"+m1pcount[i][0]+"\t"+m1pcount[i][1]);
		}
		
		/*

		System.out.println(psms1.length + "\t" + psms2.length + "\t" + map1.size() + "\t" + map2.size() + "\t"
				+ set.size() + "\t" + zcount1 + "\t" + zcount2);

		HashSet<Double> modkey = new HashSet<Double>();
		modkey.addAll(modmap1.keySet());
		modkey.addAll(modmap2.keySet());
		
		for (Double modmass : modkey) {
			if (modmap1.containsKey(modmass) && modmap2.containsKey(modmass)) {
				if (modmap.containsKey(modmass)) {
					OpenMod om = modmap.get(modmass);
					UmModification[] ums = om.getUmmod();
					if (ums != null && ums.length > 0) {
						System.out.print(modmass + "\t" + modmap1.get(modmass) + "\t" + modmap2.get(modmass) + "\t");
						for (int i = 0; i < ums.length; i++) {
							System.out.print(ums[i].getName() + ";");
						}
						System.out.println();
					} else {
						System.out.println(
								modmass + "\t" + modmap1.get(modmass) + "\t" + modmap2.get(modmass) + "\t" + "Unknown");
					}
				}
			}
		}*/
	}
	
	private static void modTest(String in) throws DocumentException {
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenPSM[] psms = reader.getOpenPSMs();
		OpenMod[] mods = reader.getOpenMods();

		System.out.println(mods.length + "\t" + psms.length);

		HashMap<Integer, Integer> bincountmap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> r2countmap = new HashMap<Integer, Integer>();
		for (int i = 0; i < mods.length; i++) {
			double[] parameter = mods[i].getGaussianParameters();
			int bin = (int) (parameter[4] - parameter[3]);
//			System.out.println("mod para\t"+parameter[1]+"\t"+bin);
			if (bincountmap.containsKey(bin)) {
				bincountmap.put(bin, bincountmap.get(bin) + 1);
			} else {
				bincountmap.put(bin, 1);
			}
			
			int r2id = (int)(parameter[5]*10);
			if(r2countmap.containsKey(r2id)){
				r2countmap.put(r2id, r2countmap.get(r2id)+1);
			}else{
				r2countmap.put(r2id, 1);
			}
		}

		for (Integer r2key : r2countmap.keySet()) {
			System.out.println("r2\t" + r2key + "\t" + r2countmap.get(r2key));
		}
		
		for (Integer bin : bincountmap.keySet()) {
			System.out.println(bin + "\t" + bincountmap.get(bin));
		}
		
		int t = 0;
		int d = 0;
		for(OpenPSM psm : psms){
			if(psm.isTarget()){
				t++;
			}else{
				d++;
			}
			double fdr = (double)d/(double)t;
			if(fdr>0.01){
				System.out.println(t+"\t"+d+"\t"+fdr);
				break;
			}
		}
	}
	
	private static void fdrtest(String in) throws DocumentException {
		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenPSM[] psms = reader.getOpenPSMs();
		OpenMod[] mods = reader.getOpenMods();

		int[] count0 = new int[2];
		Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				count0[0]++;
			} else {
				count0[1]++;
			}
			double fdr = count0[1] / (double) count0[0];
			if (fdr > 0.01) {
				System.out.println(count0[0] + "\t" + count0[1] + "\t" + fdr);
				break;
			}
		}

		Arrays.sort(psms, new OpenPSM.InverseHyperScoreComparator());

		HashMap<Integer, Double> totalp = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> countp = new HashMap<Integer, Integer>();
		int[] count1 = new int[2];
		double[] localp = new double[psms.length];
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				count1[0]++;
			} else {
				count1[1]++;
			}
			localp[i] = (double) count1[1] / (double) count1[0];
			
			if (localp[i] > 0.01) {
				System.out.println(count1[0] + "\t" + count1[1] + "\t" + localp[i]);
				break;
			}

			int modid = psms[i].getOpenModId();
			if (totalp.containsKey(modid)) {
				totalp.put(modid, totalp.get(modid) + localp[i]);
				countp.put(modid, countp.get(modid) + 1);
			} else {
				totalp.put(modid, localp[i]);
				countp.put(modid, 1);
			}
		}

		System.out.println(count1[0] + "\t" + count1[1] + "\t" + (double) count1[1] / (double) count1[0]);

		double[] avep = new double[mods.length];
		for (int i = 0; i < mods.length; i++) {
			double totali = totalp.get(mods[i].getId());
			int counti = countp.get(mods[i].getId());
			avep[i] = totali / (double) counti;

			System.out.println(i + "\t" + mods[i].getExpModMass() + "\t" + counti + "\t" + avep[i]);
		}

		int[] count2 = new int[2];
		for (int i = 0; i < psms.length; i++) {
			
			if(psms[i].getMassDiffPPM()>20){
				continue;
			}
			
			int modid = psms[i].getOpenModId();
			if (avep[modid] > 0.005) {
				continue;
			}

			if (psms[i].isTarget()) {
				count2[0]++;
			} else {
				count2[1]++;
			}
		}

		System.out.println(count2[0] + "\t" + count2[1] + "\t" + (double) count2[1] / (double) count2[0]);
	}
	
	private static void tdTTest(String target, String decoy) throws DocumentException {
		OpenModXmlReader treader = new OpenModXmlReader(target);
		OpenMod[] tmods = treader.getOpenMods();
		OpenPSM[] tpsms = treader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> tmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : tpsms) {
			int modid = op.getOpenModId();
			if (tmap.containsKey(modid)) {
				tmap.get(modid).add(op.getHyperscore());
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(op.getHyperscore());
				tmap.put(modid, list);
			}
		}

		OpenModXmlReader dreader = new OpenModXmlReader(decoy);
		OpenMod[] dmods = dreader.getOpenMods();
		OpenPSM[] dpsms = dreader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> dmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : dpsms) {
			int modid = op.getOpenModId();
			if (dmap.containsKey(modid)) {
				dmap.get(modid).add(op.getHyperscore());
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(op.getHyperscore());
				dmap.put(modid, list);
			}
		}
		
		SummaryStatistics[] decoyss = new SummaryStatistics[dmap.size()];
		Integer[] decoykeys = dmap.keySet().toArray(new Integer[dmap.size()]);
		for(int i=0;i<decoyss.length;i++){
			decoyss[i] = new SummaryStatistics();
			ArrayList<Double> list = dmap.get(decoykeys[i]);
			for(Double score: list){
				decoyss[i].addValue(score);
			}
		}
		
		Integer[] targetkeys = tmap.keySet().toArray(new Integer[tmap.size()]);
		Arrays.sort(targetkeys);
		for (Integer key : targetkeys) {
			SummaryStatistics targetss = new SummaryStatistics();
			ArrayList<Double> list = tmap.get(key);
			for (Double score : list) {
				targetss.addValue(score);
			}

			int[] count = new int[2];
			for (SummaryStatistics dss : decoyss) {
				boolean pass = TestUtils.tTest(targetss, dss, 0.05);
				if (pass) {
					count[0]++;
				} else {
					count[1]++;
				}
			}
			System.out.println(key+"\t"+count[0]+"\t"+count[1]);
		}
	}

	private static void tdTTest(String target_decoy, String target, String decoy) throws DocumentException {

		OpenModXmlReader tdreader = new OpenModXmlReader(target_decoy);
		OpenPSM[] tdpsms = tdreader.getOpenPSMs();
		HashMap<String, HashMap<Integer, Double>> tdmap = new HashMap<String, HashMap<Integer, Double>>();
		for (OpenPSM psm : tdpsms) {
			String name = psm.getFileName();
			int scan = psm.getScan();
			if (tdmap.containsKey(name)) {
				tdmap.get(name).put(scan, psm.getClassScore());
			} else {
				HashMap<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(scan, psm.getClassScore());
				tdmap.put(name, map);
			}
		}

		OpenModXmlReader treader = new OpenModXmlReader(target);
		OpenPSM[] tpsms = treader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> tmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : tpsms) {
			int modid = op.getOpenModId();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (tmap.containsKey(modid)) {
				tmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				tmap.put(modid, list);
			}
		}

		OpenModXmlReader dreader = new OpenModXmlReader(decoy);
		OpenPSM[] dpsms = dreader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> dmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : dpsms) {
			int modid = op.getOpenModId();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (dmap.containsKey(modid)) {
				dmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				dmap.put(modid, list);
			}
		}

		SummaryStatistics[] decoyss = new SummaryStatistics[dmap.size()];
		Integer[] decoykeys = dmap.keySet().toArray(new Integer[dmap.size()]);
		for (int i = 0; i < decoyss.length; i++) {
			decoyss[i] = new SummaryStatistics();
			ArrayList<Double> list = dmap.get(decoykeys[i]);
			for (Double score : list) {
				decoyss[i].addValue(score);
			}
		}
		
		for(int i=0;i<decoyss.length;i++){
			for(int j=i+1;j<decoyss.length;j++){
				
			}
		}

		Integer[] targetkeys = tmap.keySet().toArray(new Integer[tmap.size()]);
		int[][] decoyCount = new int[decoyss.length][2];
		Arrays.sort(targetkeys);
		for (Integer key : targetkeys) {
			SummaryStatistics targetss = new SummaryStatistics();
			ArrayList<Double> list = tmap.get(key);
			for (Double score : list) {
				targetss.addValue(score);
			}

			int[] count = new int[2];
			boolean[] passes = new boolean[decoyss.length];
			for (int i = 0; i < decoyss.length; i++) {
				passes[i] = TestUtils.tTest(targetss, decoyss[i], 0.01);
				if (passes[i]) {
					count[0]++;
				} else {
					count[1]++;
				}
			}
			
			double ratio = count[0] / (double) (count[0] + count[1]);
			System.out.println(key + "\t" + count[0] + "\t" + count[1] + "\t" + ratio);

			if (ratio < 0.9) {
				for (int i = 0; i < passes.length; i++) {
					if (passes[i]) {
						decoyCount[i][1]++;
					} else {
						decoyCount[i][0]++;
					}
				}
			}
		}

		for (int i = 0; i < decoyCount.length; i++) {
			System.out.println("decoy " + i + "\t" + decoyCount[i][0] + "\t" + decoyCount[i][1] + "\t"
					+ decoyCount[i][0] / (double) (decoyCount[i][0] + decoyCount[i][1]));
		}
	}
	
	private static void tdTTestTogether(String target_decoy, String target, String decoy) throws DocumentException {

		OpenModXmlReader tdreader = new OpenModXmlReader(target_decoy);
		OpenPSM[] tdpsms = tdreader.getOpenPSMs();
		HashMap<String, HashMap<Integer, Double>> tdmap = new HashMap<String, HashMap<Integer, Double>>();
		for (OpenPSM psm : tdpsms) {
			String name = psm.getFileName();
			int scan = psm.getScan();
			if (tdmap.containsKey(name)) {
				tdmap.get(name).put(scan, psm.getClassScore());
			} else {
				HashMap<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(scan, psm.getClassScore());
				tdmap.put(name, map);
			}
		}

		OpenModXmlReader treader = new OpenModXmlReader(target);
		OpenPSM[] tpsms = treader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> tmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : tpsms) {
			int modid = op.getOpenModId();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (tmap.containsKey(modid)) {
				tmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				tmap.put(modid, list);
			}
		}

		OpenModXmlReader dreader = new OpenModXmlReader(decoy);
		OpenPSM[] dpsms = dreader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> dmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : dpsms) {
			int modid = op.getOpenModId() + tmap.size();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (dmap.containsKey(modid)) {
				dmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				dmap.put(modid, list);
			}
		}
		
		Integer[] targetkeys = tmap.keySet().toArray(new Integer[tmap.size()]);
		Integer[] decoykeys = dmap.keySet().toArray(new Integer[dmap.size()]);
		
		Integer[] totalKeys = new Integer[targetkeys.length+decoykeys.length];
		System.arraycopy(targetkeys, 0, totalKeys, 0, targetkeys.length);
		System.arraycopy(decoykeys, 0, totalKeys, targetkeys.length, decoykeys.length);
		Arrays.sort(totalKeys);
		
		SummaryStatistics[] sss = new SummaryStatistics[totalKeys.length];
		for (int i = 0; i < totalKeys.length; i++) {
			sss[i] = new SummaryStatistics();
			if (tmap.containsKey(totalKeys[i])) {
				ArrayList<Double> list = tmap.get(totalKeys[i]);
				for (Double score : list) {
					sss[i].addValue(score);
				}
			} else if (dmap.containsKey(totalKeys[i])) {
				ArrayList<Double> list = dmap.get(totalKeys[i]);
				for (Double score : list) {
					sss[i].addValue(score);
				}
			}
		}
		
		for (int i = 0; i < sss.length; i++) {
			for (int j = i + 1; j < sss.length; j++) {
				boolean pass = TestUtils.tTest(sss[i], sss[j], 0.05);
				if (pass) {
					if (i < tmap.size()) {

					} else {
						
					}
					if (j < tmap.size()) {

					}
				} else {
					if (i < tmap.size()) {

					}
					if (j < tmap.size()) {

					}
				}
			}
		}

		SummaryStatistics[] decoyss = new SummaryStatistics[dmap.size()];
		
		for (int i = 0; i < decoyss.length; i++) {
			decoyss[i] = new SummaryStatistics();
			ArrayList<Double> list = dmap.get(decoykeys[i]);
			for (Double score : list) {
				decoyss[i].addValue(score);
			}
		}

		
		int[][] decoyCount = new int[decoyss.length][2];
		Arrays.sort(targetkeys);
		for (Integer key : targetkeys) {
			SummaryStatistics targetss = new SummaryStatistics();
			ArrayList<Double> list = tmap.get(key);
			for (Double score : list) {
				targetss.addValue(score);
			}

			int[] count = new int[2];
			for (int i = 0; i < decoyss.length; i++) {
				boolean pass = TestUtils.tTest(targetss, decoyss[i], 0.05);
				if (pass) {
					count[0]++;
					decoyCount[i][1]++;
				} else {
					count[1]++;
					decoyCount[i][0]++;
				}
			}
			System.out.println(
					key + "\t" + count[0] + "\t" + count[1] + "\t" + count[0] / (double) (count[0] + count[1]));
		}

		for (int i = 0; i < decoyCount.length; i++) {
			System.out.println("decoy " + i + "\t" + decoyCount[i][0] + "\t" + decoyCount[i][1] + "\t"
					+ decoyCount[i][0] / (double) (decoyCount[i][0] + decoyCount[i][1]));
		}
	}
	
	/**
	 * not work
	 * @param target_decoy
	 * @param target
	 * @param decoy
	 * @throws DocumentException
	 */
	private static void tdCluster(String target_decoy, String target, String decoy) throws DocumentException {

		OpenModXmlReader tdreader = new OpenModXmlReader(target_decoy);
		OpenPSM[] tdpsms = tdreader.getOpenPSMs();
		HashMap<String, HashMap<Integer, Double>> tdmap = new HashMap<String, HashMap<Integer, Double>>();
		for (OpenPSM psm : tdpsms) {
			String name = psm.getFileName();
			int scan = psm.getScan();
			if (tdmap.containsKey(name)) {
				tdmap.get(name).put(scan, psm.getClassScore());
			} else {
				HashMap<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(scan, psm.getClassScore());
				tdmap.put(name, map);
			}
		}

		OpenModXmlReader treader = new OpenModXmlReader(target);
		OpenPSM[] tpsms = treader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> tmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : tpsms) {
			int modid = op.getOpenModId();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (tmap.containsKey(modid)) {
				tmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				tmap.put(modid, list);
			}
		}

		OpenModXmlReader dreader = new OpenModXmlReader(decoy);
		OpenPSM[] dpsms = dreader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> dmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : dpsms) {
			int modid = op.getOpenModId() + tmap.size();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (dmap.containsKey(modid)) {
				dmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				dmap.put(modid, list);
			}
		}

		ArrayList<DataListClusterable> dclist = new ArrayList<DataListClusterable>();

		for (Integer id : tmap.keySet()) {
			ArrayList<Double> list = tmap.get(id);
			Double[] data = list.toArray(new Double[list.size()]);
			Arrays.sort(data);

			double[] feature = new double[4];
			feature[0] = data[0];
			feature[1] = data.length % 2 == 0 ? (data[data.length / 2 - 1] + data[data.length / 2]) / 2.0
					: data[(data.length - 1) / 2];
			feature[2] = data[data.length - 1];

			DataListClusterable dc = new DataListClusterable(id, feature);
			dclist.add(dc);
		}

		for (Integer id : dmap.keySet()) {
			
			ArrayList<Double> list = dmap.get(id);
			Double[] data = list.toArray(new Double[list.size()]);
			Arrays.sort(data);

			double[] feature = new double[4];
			feature[0] = data[0];
			feature[1] = data.length % 2 == 0 ? (data[data.length / 2 - 1] + data[data.length / 2]) / 2.0
					: data[(data.length - 1) / 2];
			feature[2] = data[data.length - 1];

			DataListClusterable dc = new DataListClusterable(id, feature);
			dclist.add(dc);
		}
		System.out.println("DC\t" + dclist.size());

		KMeansPlusPlusClusterer<DataListClusterable> clusterer = new KMeansPlusPlusClusterer<DataListClusterable>(2,
				10000);
		List<CentroidCluster<DataListClusterable>> clusterResults = clusterer.cluster(dclist);

		int ccid = 0;
		int[][] counts = new int[2][2];
		for (CentroidCluster<DataListClusterable> cc : clusterResults) {
			List<DataListClusterable> dcs = cc.getPoints();
			System.out.println("cluster\t"+ccid+"\t"+dcs.size());
			for (DataListClusterable dc : dcs) {
				int id = dc.getId();
				if (tmap.containsKey(id)) {
					counts[ccid][0]++;
				} 
				if (dmap.containsKey(id)) {
					counts[ccid][1]++;
				}
			}
			ccid++;
		}

		System.out.println(counts[0][0] + "\t" + counts[0][1]);
		System.out.println(counts[1][0] + "\t" + counts[1][1]);
	}

	private static void tdGaussian(String target_decoy, String target, String decoy) throws DocumentException{
		
		OpenModXmlReader tdreader = new OpenModXmlReader(target_decoy);
		OpenPSM[] tdpsms = tdreader.getOpenPSMs();
		HashMap<String, HashMap<Integer, Double>> tdmap = new HashMap<String, HashMap<Integer, Double>>();
		for (OpenPSM psm : tdpsms) {
			String name = psm.getFileName();
			int scan = psm.getScan();
			if (tdmap.containsKey(name)) {
				tdmap.get(name).put(scan, psm.getClassScore());
			} else {
				HashMap<Integer, Double> map = new HashMap<Integer, Double>();
				map.put(scan, psm.getClassScore());
				tdmap.put(name, map);
			}
		}

		OpenModXmlReader treader = new OpenModXmlReader(target);
		OpenPSM[] tpsms = treader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> tmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : tpsms) {
			int modid = op.getOpenModId();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (score > 0.01) {
				continue;
			}
			if (tmap.containsKey(modid)) {
				tmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				tmap.put(modid, list);
			}
		}

		OpenModXmlReader dreader = new OpenModXmlReader(decoy);
		OpenPSM[] dpsms = dreader.getOpenPSMs();
		HashMap<Integer, ArrayList<Double>> dmap = new HashMap<Integer, ArrayList<Double>>();
		for (OpenPSM op : dpsms) {
			int modid = op.getOpenModId() + tmap.size();
			String name = op.getFileName();
			int scan = op.getScan();
			double score = tdmap.get(name).get(scan);
			if (dmap.containsKey(modid)) {
				dmap.get(modid).add(score);
			} else {
				ArrayList<Double> list = new ArrayList<Double>();
				list.add(score);
				dmap.put(modid, list);
			}
		}
	}
	
	private static void statisTest(String in) throws DocumentException {

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] openMods = reader.getOpenMods();
		OpenPSM[] validatedPSMs = reader.getOpenPSMs();

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < validatedPSMs.length; i++) {
			int modid = validatedPSMs[i].getOpenModId();
			double classScore = validatedPSMs[i].getClassScore();
			boolean isTarget = validatedPSMs[i].isTarget();
			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}
		}
System.out.println(modmap.size()+"\t"+openMods.length+"\t"+validatedPSMs.length);
		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int[] useModIds = new int[modmap.size()];
		ArrayList<OpenMod> modlist = new ArrayList<OpenMod>();
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		for (int i = 0; i < modkey.length; i++) {
			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.05)) {
						pass = true;
						openMods[modkey[i]].setId(modlist.size());
						modlist.add(openMods[modkey[i]]);
						useModIds[modkey[i]] = openMods[modkey[i]].getId();
					}
				} else {
					pass = true;
					openMods[modkey[i]].setId(modlist.size());
					modlist.add(openMods[modkey[i]]);
					useModIds[modkey[i]] = openMods[modkey[i]].getId();
				}
			}

			if (pass) {
				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = validatedPSMs[psmIdList.get(j)];
				}

				Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
				int target = 0;
				int decoy = 0;

				for (int j = 0; j < psms.length; j++) {
					if (psms[j].isTarget()) {
						target++;
					} else {
						decoy++;
					}
					double locfdr = (double) decoy / (double) target;
					if (target != 0 && locfdr < 0.01) {
						filteredList.add(psms[j]);
						if (psms[j].isTarget()) {
							totaltarget++;
						} else {
							totaldecoy++;
						}
					} else {
						break;
					}
				}
				
				System.out.println("mod\t"+modlist.size()+"\t"+target+"\t"+decoy);
			}
		}

		OpenMod[] usedMods = modlist.toArray(new OpenMod[modlist.size()]);
		
		int pept = 0;
		int pepd = 0;
		HashSet<String> seqset = new HashSet<String>();
		for(OpenPSM psm : filteredList){
			if(seqset.contains(psm.getSequence())){
				continue;
			}
			
			seqset.add(psm.getSequence());
			if(psm.isTarget()){
				pept++;
			}else{
				pepd++;
			}
		}

		System.out.println("psm count\t" + validatedPSMs.length + "\t" + filteredList.size() + "\t" + totaltarget + "\t"
				+ totaldecoy+"\t"+pept+"\t"+pepd);
		System.out.println("mod count\t" + openMods.length + "\t" + usedMods.length);
	}
	
	private static void exportPIA(String in, String out) throws DocumentException, IOException {

		OpenModXmlReader reader = new OpenModXmlReader(in);
		OpenMod[] openMods = reader.getOpenMods();

		for (int i = 0; i < openMods.length; i++) {
			openMods[i].setExpModMass(openMods[i].getGaussianParameters()[1] - openMods[0].getGaussianParameters()[1]);
		}

		OpenModXmlWriter writer = new OpenModXmlWriter(out);
		writer.addMods(openMods);
		writer.close();
	}
}
