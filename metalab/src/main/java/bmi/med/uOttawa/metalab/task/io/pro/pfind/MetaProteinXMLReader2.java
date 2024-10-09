package bmi.med.uOttawa.metalab.task.io.pro.pfind;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.task.hgm.MetaProteinAnnoHgm;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinHtmlWriter;
import bmi.med.uOttawa.metalab.task.mag.MetaProteinAnnoMag;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class MetaProteinXMLReader2 {

	private Element root;
	private String searchType;
	private String quanMode;
	private MetaProteinAnnoEggNog[] proteins;
	private String[] fileNames;
	private DecimalFormat df2 = FormatTool.getDF2();

	private HashMap<String, String[]> usedCogMap;
	private HashMap<String, String[]> usedNogMap;
	private HashMap<String, String> usedKeggMap;
	private HashMap<String, GoObo> usedGoMap;
	private HashMap<String, EnzymeCommission> usedEcMap;

	public MetaProteinXMLReader2(String file) throws DocumentException {
		this(new File(file));
	}

	public MetaProteinXMLReader2(File file) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		this.root = document.getRootElement();
		read();
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected void read() {

		this.fileNames = root.attributeValue("fileNames").split(";");

		this.searchType = root.attributeValue("searchType");
		this.quanMode = root.attributeValue("quanMode");

		this.usedCogMap = new HashMap<String, String[]>();
		Iterator<Element> cogIt = root.element("COGs").elementIterator("COG");
		while (cogIt.hasNext()) {
			Element eCog = cogIt.next();
			this.usedCogMap.put(eCog.attributeValue("id"),
					new String[] { eCog.attributeValue("category"), eCog.attributeValue("function") });
		}

		this.usedNogMap = new HashMap<String, String[]>();
		Iterator<Element> nogIt = root.element("NOGs").elementIterator("NOG");
		while (nogIt.hasNext()) {
			Element eNog = nogIt.next();
			this.usedNogMap.put(eNog.attributeValue("id"),
					new String[] { eNog.attributeValue("category"), eNog.attributeValue("function") });
		}

		this.usedKeggMap = new HashMap<String, String>();
		Iterator<Element> keggIt = root.element("KEGGs").elementIterator("KEGG");
		while (keggIt.hasNext()) {
			Element eKegg = keggIt.next();
			this.usedKeggMap.put(eKegg.attributeValue("id"), eKegg.attributeValue("function"));
		}

		this.usedGoMap = new HashMap<String, GoObo>();
		Iterator<Element> goIt = root.element("GOs").elementIterator("GO");
		while (goIt.hasNext()) {
			Element eGo = goIt.next();
			String id = eGo.attributeValue("id");
			String name = eGo.attributeValue("name");
			String namespace = eGo.attributeValue("namespace");
			this.usedGoMap.put(id, new GoObo(id, name, namespace));
		}

		this.usedEcMap = new HashMap<String, EnzymeCommission>();
		Iterator<Element> ecIt = root.element("ECs").elementIterator("EC");
		while (ecIt.hasNext()) {
			Element eEc = ecIt.next();
			String id = eEc.attributeValue("id");
			String de = eEc.attributeValue("de");
			String ca = eEc.attributeValue("ca");
			String anString = eEc.attributeValue("an");

			ArrayList<String> anlist = new ArrayList<String>();
			for (String an : anString.split(";")) {
				anlist.add(an);
			}

			this.usedEcMap.put(id, new EnzymeCommission(id, de, ca, anlist));
		}

		int proteinCount = Integer.parseInt(root.attributeValue("Protein_count"));

		if (this.searchType.equals(MetaConstants.pFindHap)) {
			this.proteins = new MetaProteinAnnoHgm[proteinCount];
		} else if (this.searchType.equals(MetaConstants.pFindMag) || this.searchType.equals(MetaConstants.fragpipeMAG)
				|| this.searchType.equals(MetaConstants.alphapeptMAG)) {
			this.proteins = new MetaProteinAnnoMag[proteinCount];
		} else {
			this.proteins = new MetaProteinAnnoEggNog[proteinCount];
		}
		int proId = 0;
		Iterator<Element> proIt = root.element("Proteins").elementIterator("Protein");

		while (proIt.hasNext()) {

			Element ePro = proIt.next();
			String name = ePro.attributeValue("Name");

			if (this.searchType.equals(MetaConstants.maxQuant)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int uniPepCount = Integer.parseInt(ePro.attributeValue("Unique_peptide_count"));
				int razorCount = Integer.parseInt(ePro.attributeValue("Razor_unique_peptide_count"));
				double evalue = Double.parseDouble(ePro.attributeValue("E_value"));

				int[] ms2Count = new int[fileNames.length];
				if (quanMode.equals(MetaConstants.labelFree)) {
					String[] ms2CountSs = ePro.attributeValue("ms2Counts").split("_");
					for (int i = 0; i < ms2CountSs.length; i++) {
						ms2Count[i] = Integer.parseInt(ms2CountSs[i]);
					}
				}

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, razorCount, uniPepCount, evalue,
						ms2Count, intensity);

				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Tax_name");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");

				proteins[proId++] = new MetaProteinAnnoEggNog(pro, tax_id, best_tax_name, preferred_name, Gene_Ontology,
						EC, KEGG_ko, KEGG_Pathway, KEGG_Module, KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy,
						BiGG_Reaction, cog, nog);

			} else if (this.searchType.equals(MetaConstants.openSearch)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, psmCount, score, intensity);

				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Best_tax_name");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");

				proteins[proId++] = new MetaProteinAnnoEggNog(pro, tax_id, best_tax_name, preferred_name, Gene_Ontology,
						EC, KEGG_ko, KEGG_Pathway, KEGG_Module, KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy,
						BiGG_Reaction, cog, nog);

			} else if (this.searchType.equals(MetaConstants.pFind) || this.searchType.equals(MetaConstants.fragpipeIGC)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, psmCount, score, intensity);

				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Best_tax_name");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");

				proteins[proId++] = new MetaProteinAnnoEggNog(pro, tax_id, best_tax_name, preferred_name, Gene_Ontology,
						EC, KEGG_ko, KEGG_Pathway, KEGG_Module, KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy,
						BiGG_Reaction, cog, nog);

			} else if (this.searchType.equals(MetaConstants.pFindHap)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, psmCount, score, intensity);

				String pro_name = ePro.attributeValue("Pro_name");
				String seed = ePro.attributeValue("seed");
				String KofamKOALA_KO = ePro.attributeValue("KofamKOALA_KO");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");
				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Best_tax_name");
				String description = ePro.attributeValue("description");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String pfam = ePro.attributeValue("pfam");

				proteins[proId++] = new MetaProteinAnnoHgm(pro, pro_name, seed, KofamKOALA_KO, cog, nog, tax_id,
						best_tax_name, description, preferred_name, Gene_Ontology, EC, KEGG_ko, KEGG_Pathway,
						KEGG_Module, KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy, BiGG_Reaction, pfam);

			} else if (this.searchType.equals(MetaConstants.pFindMag)
					|| this.searchType.equals(MetaConstants.fragpipeMAG)
					|| this.searchType.equals(MetaConstants.alphapeptMAG)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, psmCount, score, intensity);

				String pro_name = ePro.attributeValue("Pro_name");
				String seed = ePro.attributeValue("seed");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");
				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Best_tax_name");
				String description = ePro.attributeValue("description");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String pfam = ePro.attributeValue("pfam");

				proteins[proId++] = new MetaProteinAnnoMag(pro, pro_name, seed, cog, nog, tax_id, best_tax_name,
						description, preferred_name, Gene_Ontology, EC, KEGG_ko, KEGG_Pathway, KEGG_Module,
						KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy, BiGG_Reaction, pfam);

			} else if (this.searchType.equals(MetaConstants.flashLFQ) || this.searchType.equals(MetaConstants.diaNN)) {

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(name, intensity);
				pro.setGroupId(proId);

				String tax_id = ePro.attributeValue("Tax_id");
				String best_tax_name = ePro.attributeValue("Best_tax_name");
				String preferred_name = ePro.attributeValue("Preferred_name");
				String Gene_Ontology = ePro.attributeValue("Gene_Ontology");
				String EC = ePro.attributeValue("EC");
				String KEGG_ko = ePro.attributeValue("KEGG_ko");
				String KEGG_Pathway = ePro.attributeValue("KEGG_Pathway");
				String KEGG_Module = ePro.attributeValue("KEGG_Module");
				String KEGG_Reaction = ePro.attributeValue("KEGG_Reaction");
				String KEGG_rclass = ePro.attributeValue("KEGG_rclass");
				String BRITE = ePro.attributeValue("BRITE");
				String KEGG_TC = ePro.attributeValue("KEGG_TC");
				String CAZy = ePro.attributeValue("CAZy");
				String BiGG_Reaction = ePro.attributeValue("BiGG_Reaction");
				String cog = ePro.attributeValue("COG");
				String nog = ePro.attributeValue("NOG");

				proteins[proId++] = new MetaProteinAnnoEggNog(pro, tax_id, best_tax_name, preferred_name, Gene_Ontology,
						EC, KEGG_ko, KEGG_Pathway, KEGG_Module, KEGG_Reaction, KEGG_rclass, BRITE, KEGG_TC, CAZy,
						BiGG_Reaction, cog, nog);
			}
		}
	}

	public MetaProteinAnnoEggNog[] getProteins() {
		return proteins;
	}

	public void exportCog(String csv) throws IOException {
		exportCog(new File(csv));
	}

	public void exportCog(File csv) throws IOException {
		PrintWriter writer = new PrintWriter(csv);
		StringBuilder title = new StringBuilder();
		title.append("\"COG accession\"").append(",");
		title.append("\"COG name\"").append(",");
		title.append("\"COG category\"").append(",");

		if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
			for (String exp : fileNames) {
				title.append("\"").append(exp.replace("LFQ intensity", "Intensity")).append("\",");
			}
		} else {
			for (String exp : fileNames) {
				title.append("\"" + exp).append("\",");
			}
		}

		writer.println(title);

		HashMap<String, double[]> cogMap = new HashMap<String, double[]>();

		for (MetaProteinAnnoEggNog pro : proteins) {
			String cog = pro.getCog();
			if (cog.length() == 0 || !cogMap.containsKey(cog)) {
				continue;
			}

			double[] intensity = pro.getPro().getIntensities();

			double[] totalIntensity;
			if (cogMap.containsKey(cog)) {
				totalIntensity = cogMap.get(cog);
			} else {
				totalIntensity = new double[intensity.length];
			}
			for (int i = 0; i < totalIntensity.length; i++) {
				totalIntensity[i] += intensity[i];
			}
			cogMap.put(cog, totalIntensity);
		}

		for (String cog : cogMap.keySet()) {
			double[] intensity = cogMap.get(cog);
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(cog).append("\",");
			sb.append("\"").append(cogMap.get(cog)[0]).append("\",");
			sb.append("\"").append(cogMap.get(cog)[1]).append("\",");

			for (int i = 0; i < intensity.length; i++) {
				sb.append("\"").append(intensity[i]).append("\",");
			}
			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();
	}

	public void exportCsv(String csv) throws IOException {
		exportCsv(new File(csv));
	}

	public void exportCsv(File csv) throws IOException {

		PrintWriter writer = new PrintWriter(csv);
		StringBuilder title = new StringBuilder();

		if (this.searchType.equals(MetaConstants.maxQuant)) {
			title.append("\"Group_ID\"").append(",");
			title.append("\"Name\"").append(",");
			title.append("\"Peptide count\"").append(",");
			title.append("\"Unique peptide count\"").append(",");
			title.append("\"E value\"").append(",");

		} else if (this.searchType.equals(MetaConstants.openSearch)) {
			title.append("\"Group_ID\"").append(",");
			title.append("\"Name\"").append(",");
			title.append("\"Peptide count\"").append(",");
			title.append("\"PSM count\"").append(",");
			title.append("\"Score\"").append(",");
		} else if (this.searchType.equals(MetaConstants.pFind)) {
			title.append("\"Group_ID\"").append(",");
			title.append("\"Name\"").append(",");
			title.append("\"Peptide count\"").append(",");
			title.append("\"PSM count\"").append(",");
			title.append("\"Score\"").append(",");
		} else if (this.searchType.equals(MetaConstants.pFindHap) || this.searchType.equals(MetaConstants.fragpipeMAG)) {
			title.append("\"Group_ID\"").append(",");
			title.append("\"Name\"").append(",");
			title.append("\"Peptide count\"").append(",");
			title.append("\"PSM count\"").append(",");
			title.append("\"Score\"").append(",");
			title.append("\"Protein name\"").append(",");
		} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("\"Group_ID\"").append(",");
			title.append("\"Name\"").append(",");
			title.append("\"Protein name\"").append(",");
		}

		title.append("\"Taxonomy Id\"").append(",");
		title.append("\"Taxonomy name\"").append(",");
		title.append("\"Preferred name\"").append(",");

		title.append("\"Gene_Ontology_id\"").append(",");
		title.append("\"Gene_Ontology_name\"").append(",");
		title.append("\"Gene_Ontology_namespace\"").append(",");

		title.append("\"EC_id\"").append(",");
		title.append("\"EC_de\"").append(",");
		title.append("\"EC_an\"").append(",");
		title.append("\"EC_ca\"").append(",");

		title.append("\"KEGG_ko\"").append(",");
		title.append("\"KEGG_Pathway\"").append(",");
		title.append("\"KEGG_Module\"").append(",");
		title.append("\"KEGG_Reaction\"").append(",");
		title.append("\"KEGG_rclass\"").append(",");
		title.append("\"BRITE\"").append(",");
		title.append("\"KEGG_TC\"").append(",");
		title.append("\"CAZy\"").append(",");
		title.append("\"BiGG_Reaction\"").append(",");

		title.append("\"COG accession\"").append(",");
		title.append("\"COG category\"").append(",");
		title.append("\"COG name\"").append(",");

		title.append("\"NOG accession\"").append(",");
		title.append("\"NOG category\"").append(",");
		title.append("\"NOG name\"").append(",");

		if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append("\"").append(exp.replace("LFQ intensity", "MS2 count")).append("\",");
				} else {
					title.append("\"").append("MS2 count ").append(exp).append("\",");
				}
			}

			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append("\"").append(exp.replace("LFQ intensity", "Intensity")).append("\",");
				} else {
					title.append("\"").append("Intensity").append(exp).append("\",");
				}
			}
		} else {

			for (String exp : fileNames) {
				title.append("\"" + exp).append("\",");
			}
		}

		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		for (MetaProteinAnnoEggNog mpa : proteins) {

			MetaProtein protein = mpa.getPro();
			StringBuilder sb = new StringBuilder();
			String[] funcs = mpa.getFuncArrays(usedCogMap, usedNogMap, usedKeggMap, usedGoMap, usedEcMap);

			if (this.searchType.equals(MetaConstants.maxQuant)) {
				if (protein.getProteinId() > 1) {
					continue;
				}
				sb.append("\"").append(protein.getGroupId()).append("\",");
				sb.append("\"").append(protein.getName()).append("\",");
				sb.append("\"").append(protein.getPepCount()).append("\",");
				sb.append("\"").append(protein.getUniPepCount()).append("\",");
				sb.append("\"").append(protein.getEvalue()).append("\",");
			} else if (this.searchType.equals(MetaConstants.openSearch)) {
				sb.append("\"").append(protein.getGroupId()).append("\",");
				sb.append("\"").append(protein.getName()).append("\",");
				sb.append("\"").append(protein.getPepCount()).append("\",");
				sb.append("\"").append(protein.getPsmCount()).append("\",");
				sb.append("\"").append(df2.format(protein.getScore())).append("\",");
			} else if (this.searchType.equals(MetaConstants.pFind)) {
				sb.append("\"").append(protein.getGroupId()).append("\",");
				sb.append("\"").append(protein.getName()).append("\",");
				sb.append("\"").append(protein.getPepCount()).append("\",");
				sb.append("\"").append(protein.getPsmCount()).append("\",");
				sb.append("\"").append(df2.format(protein.getScore())).append("\",");
			} else if (this.searchType.equals(MetaConstants.pFindHap)
					|| this.searchType.equals(MetaConstants.fragpipeMAG)) {
				sb.append("\"").append(protein.getGroupId()).append("\",");
				sb.append("\"").append(protein.getName()).append("\",");
				sb.append("\"").append(protein.getPepCount()).append("\",");
				sb.append("\"").append(protein.getPsmCount()).append("\",");
				sb.append("\"").append(df2.format(protein.getScore())).append("\",");
			} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
				sb.append("\"").append(protein.getGroupId()).append("\",");
				sb.append("\"").append(protein.getName()).append("\",");
			}

			for (int i = 0; i < funcs.length; i++) {
				sb.append("\"").append(funcs[i]).append("\"").append(",");
			}

			if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
				int[] ms2Counts = protein.getMs2Counts();
				for (int ms2Count : ms2Counts) {
					sb.append("\"").append(ms2Count).append("\",");
				}
			}

			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				sb.append("\"").append(intensity).append("\",");
			}
			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();
	}

	public void exportTsvReport(String tsv) throws IOException {
		exportTsvReport(new File(tsv));
	}

	public void exportTsvReport(File tsv) throws IOException {

		PrintWriter writer = new PrintWriter(tsv);
		StringBuilder title = new StringBuilder();

		if (this.searchType.equals(MetaConstants.maxQuant)) {
			title.append("Group_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("Unique peptide count").append("\t");
			title.append("E value").append("\t");

		} else if (this.searchType.equals(MetaConstants.openSearch)) {
			title.append("Group_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFind)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFindHap)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFindMag) || this.searchType.equals(MetaConstants.fragpipeMAG)
				|| this.searchType.equals(MetaConstants.fragpipeIGC)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
		} 

		title.append("Gene_Ontology_id").append("\t");
		title.append("Gene_Ontology_name").append("\t");
		title.append("Gene_Ontology_namespace").append("\t");

		title.append("EC_id").append("\t");
		title.append("EC_de").append("\t");
		title.append("EC_an").append("\t");
		title.append("EC_ca").append("\t");

		if (this.searchType.equals(MetaConstants.pFindHap)) {
			title.append("KofamKOALA_KO").append("\t");
		}

		title.append("KEGG_ko").append("\t");
		title.append("KEGG_Pathway_Entry").append("\t");
		title.append("KEGG_Pathway_Name").append("\t");
		title.append("KEGG_Module").append("\t");
		title.append("KEGG_Reaction").append("\t");
		title.append("KEGG_rclass").append("\t");
		title.append("BRITE").append("\t");
		title.append("KEGG_TC").append("\t");
		title.append("CAZy").append("\t");
		title.append("BiGG_Reaction").append("\t");

		if (this.searchType.equals(MetaConstants.pFindHap) || this.searchType.equals(MetaConstants.pFindMag)
				|| this.searchType.equals(MetaConstants.fragpipeMAG)
				|| this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("PFAMs").append("\t");
		}

		title.append("COG accession").append("\t");
		title.append("COG category").append("\t");
		title.append("COG name").append("\t");

		title.append("NOG accession").append("\t");
		title.append("NOG category").append("\t");
		title.append("NOG name").append("\t");

		if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append(exp.replace("LFQ intensity", "MS2 count")).append("\t");
				} else {
					title.append("MS2 count ").append(exp).append("\t");
				}
			}

			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append(exp.replace("LFQ intensity", "Intensity")).append("\t");
				} else {
					title.append("Intensity").append(exp).append("\t");
				}
			}
		} else {
			for (String exp : fileNames) {
				if (!exp.startsWith("Intensity")) {
					title.append("Intensity ").append(exp).append("\t");
				} else {
					title.append(exp).append("\t");
				}
			}
		}

		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		Arrays.sort(proteins, new Comparator<MetaProteinAnnoEggNog>() {

			@Override
			public int compare(MetaProteinAnnoEggNog o1, MetaProteinAnnoEggNog o2) {
				// TODO Auto-generated method stub

				if (o1.getPro().getGroupId() < o2.getPro().getGroupId()) {
					return -1;
				}
				if (o1.getPro().getGroupId() > o2.getPro().getGroupId()) {
					return 1;
				}
				return 0;
			}

		});

		for (int i = 0; i < proteins.length; i++) {

			MetaProtein protein = proteins[i].getPro();
			StringBuilder sb = new StringBuilder();
			String[] funcs = proteins[i].getFuncReportArrays(usedCogMap, usedNogMap, usedKeggMap, usedGoMap, usedEcMap);

			if (this.searchType.equals(MetaConstants.maxQuant)) {
				if (protein.getProteinId() > 1) {
					continue;
				}
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getUniPepCount()).append("\t");
				sb.append(protein.getEvalue()).append("\t");
			} else if (this.searchType.equals(MetaConstants.openSearch)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFind)
					|| this.searchType.equals(MetaConstants.fragpipeIGC)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFindHap)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFindMag)
					|| this.searchType.equals(MetaConstants.fragpipeMAG)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
			}

			for (int j = 0; j < funcs.length; j++) {
				sb.append(funcs[j]).append("\t");
			}

			if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
				int[] ms2Counts = protein.getMs2Counts();
				for (int ms2Count : ms2Counts) {
					sb.append(ms2Count).append("\t");
				}
			}

			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				sb.append(intensity).append("\t");
			}

			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();
	}

	public void exportTsv(String tsv) throws IOException {
		exportTsv(new File(tsv));
	}

	public void exportTsv(File tsv) throws IOException {

		PrintWriter writer = new PrintWriter(tsv);
		StringBuilder title = new StringBuilder();

		if (this.searchType.equals(MetaConstants.maxQuant)) {
			title.append("Group_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("Unique peptide count").append("\t");
			title.append("E value").append("\t");

		} else if (this.searchType.equals(MetaConstants.openSearch)) {
			title.append("Group_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFind)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFindHap)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		} else if (this.searchType.equals(MetaConstants.pFindMag)
				|| this.searchType.equals(MetaConstants.fragpipeMAG)) {
			title.append("Group_ID").append("\t");
//			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("PEP").append("\t");
		} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
		} else if (this.searchType.equals(MetaConstants.diaNN)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
		} else if (this.searchType.equals(MetaConstants.fragpipeIGC)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		}

		if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append(exp.replace("LFQ intensity", "MS2 count")).append("\t");
				} else {
					title.append("MS2 count ").append(exp).append("\t");
				}
			}

			for (String exp : fileNames) {
				if (exp.startsWith("LFQ intensity")) {
					title.append(exp.replace("LFQ intensity", "Intensity")).append("\t");
				} else {
					title.append("Intensity ").append(exp).append("\t");
				}
			}
		} else {
			for (String exp : fileNames) {
				if (!exp.startsWith("Intensity")) {
					title.append("Intensity ").append(exp).append("\t");
				} else {
					title.append(exp).append("\t");
				}
			}
		}

		if (this.searchType.equals(MetaConstants.pFindHap) || this.searchType.equals(MetaConstants.pFindMag)
				|| this.searchType.equals(MetaConstants.fragpipeMAG)
				|| this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("Protein name").append("\t");
			title.append("Seed ortholog").append("\t");
			title.append("Description").append("\t");
		}

		title.append("Taxonomy Id").append("\t");
		title.append("Taxonomy name").append("\t");
		title.append("Preferred name").append("\t");

		title.append("Gene_Ontology_id").append("\t");
		title.append("Gene_Ontology_name").append("\t");
		title.append("Gene_Ontology_namespace").append("\t");

		title.append("EC_id").append("\t");
		title.append("EC_de").append("\t");
		title.append("EC_an").append("\t");
		title.append("EC_ca").append("\t");

		if (this.searchType.equals(MetaConstants.pFindHap)) {
			title.append("KofamKOALA_KO").append("\t");
		}

		title.append("KEGG_ko").append("\t");
		title.append("KEGG_Pathway_Entry").append("\t");
		title.append("KEGG_Pathway_Name").append("\t");
		title.append("KEGG_Module").append("\t");
		title.append("KEGG_Reaction").append("\t");
		title.append("KEGG_rclass").append("\t");
		title.append("BRITE").append("\t");
		title.append("KEGG_TC").append("\t");
		title.append("CAZy").append("\t");
		title.append("BiGG_Reaction").append("\t");

		if (this.searchType.equals(MetaConstants.pFindHap) || this.searchType.equals(MetaConstants.pFindMag)
				|| this.searchType.equals(MetaConstants.fragpipeMAG)
				|| this.searchType.equals(MetaConstants.alphapeptMAG)) {
			title.append("PFAMs").append("\t");
		}

		title.append("COG accession").append("\t");
		title.append("COG category").append("\t");
		title.append("COG name").append("\t");

		title.append("NOG accession").append("\t");
		title.append("NOG category").append("\t");
		title.append("NOG name").append("\t");

		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		Arrays.sort(proteins, new Comparator<MetaProteinAnnoEggNog>() {

			@Override
			public int compare(MetaProteinAnnoEggNog o1, MetaProteinAnnoEggNog o2) {
				// TODO Auto-generated method stub

				if (o1.getPro().getGroupId() < o2.getPro().getGroupId()) {
					return -1;
				}
				if (o1.getPro().getGroupId() > o2.getPro().getGroupId()) {
					return 1;
				}
				if (o1.getPro().getProteinId() < o2.getPro().getProteinId()) {
					return -1;
				}
				if (o1.getPro().getProteinId() > o2.getPro().getProteinId()) {
					return 1;
				}
				return 0;
			}

		});

		for (int i = 0; i < proteins.length; i++) {

			MetaProtein protein = proteins[i].getPro();
			StringBuilder sb = new StringBuilder();
			String[] funcs = proteins[i].getFuncArrays(usedCogMap, usedNogMap, usedKeggMap, usedGoMap, usedEcMap);

			if (this.searchType.equals(MetaConstants.maxQuant)) {
				if (protein.getProteinId() > 1) {
					continue;
				}
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getUniPepCount()).append("\t");
				sb.append(protein.getEvalue()).append("\t");
			} else if (this.searchType.equals(MetaConstants.openSearch)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFind)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFindHap)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.pFindMag)
					|| this.searchType.equals(MetaConstants.fragpipeMAG)
					|| this.searchType.equals(MetaConstants.fragpipeIGC)) {
				sb.append(protein.getGroupId()).append("\t");
//				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(df2.format(protein.getScore())).append("\t");
			} else if (this.searchType.equals(MetaConstants.alphapeptMAG)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
			} else if (this.searchType.equals(MetaConstants.diaNN)) {
				sb.append(i).append("\t");
				sb.append(protein.getName()).append("\t");
			}

			if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
				int[] ms2Counts = protein.getMs2Counts();
				for (int ms2Count : ms2Counts) {
					sb.append(ms2Count).append("\t");
				}
			}

			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				sb.append(intensity).append("\t");
			}

			for (int j = 0; j < funcs.length; j++) {
				sb.append(funcs[j]).append("\t");
			}

			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();
	}

	public void exportHtml(String html) throws IOException {
		MetaProteinHtmlWriter htmlWriter = new MetaProteinHtmlWriter(html);
		htmlWriter.write(proteins, usedCogMap, usedNogMap);
		htmlWriter.close();
	}

	public void exportHtml(File html) throws IOException {
		MetaProteinHtmlWriter htmlWriter = new MetaProteinHtmlWriter(html);
		htmlWriter.write(proteins, usedCogMap, usedNogMap);
		htmlWriter.close();
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public HashMap<String, String[]> getUsedCogMap() {
		return usedCogMap;
	}

	public HashMap<String, String[]> getUsedNogMap() {
		return usedNogMap;
	}

	public HashMap<String, String> getUsedKeggMap() {
		return usedKeggMap;
	}

	public HashMap<String, GoObo> getUsedGoMap() {
		return usedGoMap;
	}

	public HashMap<String, EnzymeCommission> getUsedEcMap() {
		return usedEcMap;
	}

	public static void main(String[] args) throws DocumentException, IOException {
		MetaProteinXMLReader2 reader2 = new MetaProteinXMLReader2(
				"Z:\\Kai\\20230702\\functional_annotation\\functions.xml");
		reader2.exportTsv("Z:\\Kai\\20230702\\functional_annotation\\new_report.tsv");
		reader2.exportTsvReport("Z:\\Kai\\20230702\\functional_annotation\\new_report_failed.tsv");
//		reader2.exportHtml("Z:\\Kai\\20220913_david\\functional_annotation\\functions.html");
	}

}
