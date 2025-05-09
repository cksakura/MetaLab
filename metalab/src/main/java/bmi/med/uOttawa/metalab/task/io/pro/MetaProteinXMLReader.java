/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaProteinXMLReader {

	protected Element root;
	protected String searchType;
	protected String quanMode;
	protected MetaProteinAnno1[] proteins;
	/*
	 * private HashMap<String, String> cogMap; private HashMap<String, String>
	 * cogCateMap; private HashMap<String, String> nogMap; private HashMap<String,
	 * String> nogCateMap; private HashMap<String, String> keggMap; private
	 * HashMap<String, String> koMap; private HashMap<String, String> goMap;
	 */
	protected HashMap<String, HashMap<String, String>> functionMap;
	protected HashMap<Integer, Taxon> taxaMap;
	protected String[] fileNames;

	protected DecimalFormat df2 = FormatTool.getDF2();
	protected DecimalFormat dfe2 = FormatTool.getDFE2();

	public MetaProteinXMLReader(String file) throws DocumentException {
		this(new File(file));
	}

	public MetaProteinXMLReader(File file) throws DocumentException {
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
		this.functionMap = new HashMap<String, HashMap<String, String>>();

		if (root.element("COGs") != null) {
			Iterator<Element> cogIt = root.element("COGs").elementIterator("COG");
			int cogCount = Integer.parseInt(root.attributeValue("COG_count"));
			HashMap<String, String> cogMap = new HashMap<String, String>(cogCount);
			HashMap<String, String> cogCateMap = new HashMap<String, String>(cogCount);

			while (cogIt.hasNext()) {
				Element eCOG = cogIt.next();
				String accession = eCOG.attributeValue("Accession");
				String name = eCOG.attributeValue("Function");
				String category = eCOG.attributeValue("Category");
				cogMap.put(accession, name);
				cogCateMap.put(accession, category);
			}

			this.functionMap.put("COG", cogMap);
			this.functionMap.put("COG category", cogCateMap);
		}

		if (root.element("NOGs") != null) {
			int nogCount = Integer.parseInt(root.attributeValue("NOG_count"));
			HashMap<String, String> nogMap = new HashMap<String, String>(nogCount);
			HashMap<String, String> nogCateMap = new HashMap<String, String>(nogCount);
			Iterator<Element> nogIt = root.element("NOGs").elementIterator("NOG");
			while (nogIt.hasNext()) {
				Element eNOG = nogIt.next();
				String accession = eNOG.attributeValue("Accession");
				String name = eNOG.attributeValue("Function");
				String category = eNOG.attributeValue("Category");
				nogMap.put(accession, name);
				nogCateMap.put(accession, category);
			}

			this.functionMap.put("NOG", nogMap);
			this.functionMap.put("NOG category", nogCateMap);
		}

		if (root.element("KEGGs") != null) {
			int keggCount = Integer.parseInt(root.attributeValue("KEGG_count"));
			HashMap<String, String> keggMap = new HashMap<String, String>(keggCount);
			Iterator<Element> keggIt = root.element("KEGGs").elementIterator("KEGG");
			while (keggIt.hasNext()) {
				Element eKEGG = keggIt.next();
				String accession = eKEGG.attributeValue("Accession");
				String name = eKEGG.attributeValue("Function");
				keggMap.put(accession, name);
			}
			this.functionMap.put("KEGG", keggMap);
		}

		if (root.element("KOs") != null) {
			int koCount = Integer.parseInt(root.attributeValue("KO_count"));
			HashMap<String, String> koMap = new HashMap<String, String>(koCount);
			Iterator<Element> koIt = root.element("KOs").elementIterator("KO");
			while (koIt.hasNext()) {
				Element eKO = koIt.next();
				String accession = eKO.attributeValue("Accession");
				String name = eKO.attributeValue("Function");
				koMap.put(accession, name);
			}
			this.functionMap.put("KO", koMap);
		}

		if (root.element("GOs") != null) {
			int goCount = Integer.parseInt(root.attributeValue("GO_count"));
			HashMap<String, String> goMap = new HashMap<String, String>(goCount);
			Iterator<Element> goIt = root.element("GOs").elementIterator("GO");
			while (goIt.hasNext()) {
				Element eGO = goIt.next();
				String accession = eGO.attributeValue("Accession");
				String name = eGO.attributeValue("Function");
				goMap.put(accession, name);
			}
			this.functionMap.put("GO", goMap);
		}

		int proteinCount = Integer.parseInt(root.attributeValue("Protein_count"));
		this.proteins = new MetaProteinAnno1[proteinCount];

		this.taxaMap = new HashMap<Integer, Taxon>();
		Element eTaxa = root.element("Taxa");
		if (eTaxa != null) {
			Iterator<Element> taxIt = eTaxa.elementIterator("Taxon");
			while (taxIt.hasNext()) {
				Element eTaxon = taxIt.next();
				Integer id = Integer.parseInt(eTaxon.attributeValue("id"));
				String name = eTaxon.attributeValue("name");
				Integer parentId = Integer.parseInt(eTaxon.attributeValue("parentId"));
				Integer rankId = Integer.parseInt(eTaxon.attributeValue("rankId"));
				Integer rootTypeId = Integer.parseInt(eTaxon.attributeValue("rootTypeId"));
				String[] mps = eTaxon.attributeValue("mainParentIds").split(",");
				int[] mainParents = new int[mps.length];
				for (int i = 0; i < mainParents.length; i++) {
					mainParents[i] = Integer.parseInt(mps[i]);
				}

				Taxon taxon = new Taxon(id, parentId, rankId, name, RootType.getRootType(rootTypeId));
				taxon.setMainParentIds(mainParents);
				taxaMap.put(id, taxon);
			}
		}

		int proId = 0;
		Iterator<Element> proIt = root.element("Proteins").elementIterator("Protein");

		while (proIt.hasNext()) {

			Element ePro = proIt.next();

			if (this.searchType.equals(MetaConstants.maxQuant)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				int proteinId = Integer.parseInt(ePro.attributeValue("Protein_ID"));
				String name = ePro.attributeValue("Name");

				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int uniPepCount = Integer.parseInt(ePro.attributeValue("Unique_peptide_count"));
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

				MetaProtein pro = new MetaProtein(groupId, proteinId, name, pepCount, uniPepCount, evalue, intensity,
						ms2Count);

				String COG = ePro.attributeValue("COG");
				if (COG.equals("null")) {
					COG = null;
				}
				String NOG = ePro.attributeValue("NOG");
				if (NOG.equals("null")) {
					NOG = null;
				}
				String KEGG = ePro.attributeValue("KEGG");
				if (KEGG.equals("null")) {
					KEGG = null;
				}

				String KO = ePro.attributeValue("KO");
				if (KO.equals("null")) {
					KO = null;
				}

				String[] GOBP = ePro.attributeValue("GOBP").length() > 0 ? ePro.attributeValue("GOBP").split("_")
						: null;
				String[] GOCC = ePro.attributeValue("GOCC").length() > 0 ? ePro.attributeValue("GOCC").split("_")
						: null;
				String[] GOMF = ePro.attributeValue("GOMF").length() > 0 ? ePro.attributeValue("GOMF").split("_")
						: null;

				proteins[proId++] = new MetaProteinAnno1(pro, COG, NOG, KEGG, KO, GOBP, GOCC, GOMF);

			} else if (this.searchType.equals(MetaConstants.openSearch)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				String name = ePro.attributeValue("Name");

				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(name, pepCount, psmCount, score, intensity);
				pro.setGroupId(groupId);

				String COG = ePro.attributeValue("COG");
				if (COG.equals("null")) {
					COG = null;
				}
				String NOG = ePro.attributeValue("NOG");
				if (NOG.equals("null")) {
					NOG = null;
				}
				String KEGG = ePro.attributeValue("KEGG");
				if (KEGG.equals("null")) {
					KEGG = null;
				}
				String KO = ePro.attributeValue("KO");
				if (KO.equals("null")) {
					KO = null;
				}
				String[] GOBP = ePro.attributeValue("GOBP").length() > 0 ? ePro.attributeValue("GOBP").split("_")
						: null;
				String[] GOCC = ePro.attributeValue("GOCC").length() > 0 ? ePro.attributeValue("GOCC").split("_")
						: null;
				String[] GOMF = ePro.attributeValue("GOMF").length() > 0 ? ePro.attributeValue("GOMF").split("_")
						: null;

				proteins[proId] = new MetaProteinAnno1(pro, COG, NOG, KEGG, KO, GOBP, GOCC, GOMF);

				String taxString = ePro.attributeValue("lcaId");
				if (taxString != null && taxString.length() > 0) {
					int taxId = Integer.parseInt(taxString);
					proteins[proId].setTaxId(taxId);
				}

				proId++;

			} else if (this.searchType.equals(MetaConstants.pFind)) {

				int groupId = Integer.parseInt(ePro.attributeValue("Group_ID"));
				String name = ePro.attributeValue("Name");

				int pepCount = Integer.parseInt(ePro.attributeValue("Peptide_count"));
				int psmCount = Integer.parseInt(ePro.attributeValue("PSM_count"));
				double score = Double.parseDouble(ePro.attributeValue("Score"));

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(name, pepCount, psmCount, score, intensity);
				pro.setGroupId(groupId);

				String COG = ePro.attributeValue("COG");
				if (COG == null || COG.equals("null")) {
					COG = null;
				}
				String NOG = ePro.attributeValue("NOG");
				if (NOG == null || NOG.equals("null")) {
					NOG = null;
				}
				String KEGG = ePro.attributeValue("KEGG");
				if (KEGG == null || KEGG.equals("null")) {
					KEGG = null;
				}
				String KO = ePro.attributeValue("KO");
				if (KO == null || KO.equals("null")) {
					KO = null;
				}
				String[] GOBP = ePro.attributeValue("GOBP").length() > 0 ? ePro.attributeValue("GOBP").split("_")
						: null;
				String[] GOCC = ePro.attributeValue("GOCC").length() > 0 ? ePro.attributeValue("GOCC").split("_")
						: null;
				String[] GOMF = ePro.attributeValue("GOMF").length() > 0 ? ePro.attributeValue("GOMF").split("_")
						: null;

				proteins[proId] = new MetaProteinAnno1(pro, COG, NOG, KEGG, KO, GOBP, GOCC, GOMF);

				String taxString = ePro.attributeValue("lcaId");
				if (taxString != null && taxString.length() > 0) {
					int taxId = Integer.parseInt(taxString);
					proteins[proId].setTaxId(taxId);
				}

				proId++;

			} else if (this.searchType.equals(MetaConstants.flashLFQ) || this.searchType.equals(MetaConstants.diaNN)) {

				String name = ePro.attributeValue("Name");

				String[] intensitySs = ePro.attributeValue("intensities").split("_");
				double[] intensity = new double[intensitySs.length];
				for (int i = 0; i < intensitySs.length; i++) {
					intensity[i] = Double.parseDouble(intensitySs[i]);
				}

				MetaProtein pro = new MetaProtein(name, intensity);

				String COG = ePro.attributeValue("COG");
				if (COG.equals("null")) {
					COG = null;
				}
				String NOG = ePro.attributeValue("NOG");
				if (NOG.equals("null")) {
					NOG = null;
				}
				String KEGG = ePro.attributeValue("KEGG");
				if (KEGG.equals("null")) {
					KEGG = null;
				}
				String KO = ePro.attributeValue("KO");
				if (KO.equals("null")) {
					KO = null;
				}
				String[] GOBP = ePro.attributeValue("GOBP").length() > 0 ? ePro.attributeValue("GOBP").split("_")
						: null;
				String[] GOCC = ePro.attributeValue("GOCC").length() > 0 ? ePro.attributeValue("GOCC").split("_")
						: null;
				String[] GOMF = ePro.attributeValue("GOMF").length() > 0 ? ePro.attributeValue("GOMF").split("_")
						: null;

				proteins[proId] = new MetaProteinAnno1(pro, COG, NOG, KEGG, KO, GOBP, GOCC, GOMF);

				String taxString = ePro.attributeValue("lcaId");
				if (taxString != null && taxString.length() > 0) {
					int taxId = Integer.parseInt(taxString);
					proteins[proId].setTaxId(taxId);
				}

				proId++;
			}
		}
	}

	public MetaProteinAnno1[] getProteins() {
		return proteins;
	}

	/*
	 * public HashMap<String, String> getCogMap() { return cogMap; }
	 * 
	 * public HashMap<String, String> getCogCateMap() { return cogCateMap; }
	 * 
	 * public HashMap<String, String> getNogMap() { return nogMap; }
	 * 
	 * public HashMap<String, String> getNogCateMap() { return nogCateMap; }
	 * 
	 * public HashMap<String, String> getKeggMap() { return keggMap; }
	 * 
	 * public HashMap<String, String> getKoMap() { return koMap; }
	 * 
	 * public HashMap<String, String> getGoMap() { return goMap; }
	 */
	public HashMap<Integer, Taxon> getTaxaMap() {
		return taxaMap;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public void exportSpecies(File xlsx) throws IOException {
		MetaProteinXlsxWriter xlsxWriter = new MetaProteinXlsxWriter(xlsx, fileNames, searchType, quanMode);
		xlsxWriter.write(proteins, functionMap, taxaMap);
		xlsxWriter.close();
	}

	public void exportSpecies(String xlsx) throws IOException {
		MetaProteinXlsxWriter xlsxWriter = new MetaProteinXlsxWriter(xlsx, fileNames, searchType, quanMode);
		xlsxWriter.write(proteins, functionMap, taxaMap);
		xlsxWriter.close();
	}

	public void export(File xlsx) throws IOException {
		MetaProteinXlsxWriter xlsxWriter = new MetaProteinXlsxWriter(xlsx, fileNames, searchType, quanMode);
		xlsxWriter.write(proteins, functionMap);
		xlsxWriter.close();
	}

	public void export(String xlsx) throws IOException {
		MetaProteinXlsxWriter xlsxWriter = new MetaProteinXlsxWriter(xlsx, fileNames, searchType, quanMode);
		xlsxWriter.write(proteins, functionMap);
		xlsxWriter.close();
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
		}

		HashMap<String, String> cogMap = null;
		if (functionMap.containsKey("COG")) {
			cogMap = functionMap.get("COG");
			title.append("\"COG accession\"").append(",");
			title.append("\"COG name\"").append(",");
			title.append("\"COG category\"").append(",");
		}

		HashMap<String, String> cogCateMap = null;
		if (functionMap.containsKey("COG category")) {
			cogCateMap = functionMap.get("COG category");
		}

		HashMap<String, String> nogMap = null;
		if (functionMap.containsKey("NOG")) {
			nogMap = functionMap.get("NOG");
			title.append("\"NOG accession\"").append(",");
			title.append("\"NOG name\"").append(",");
			title.append("\"NOG category\"").append(",");
		}

		HashMap<String, String> nogCateMap = null;
		if (functionMap.containsKey("NOG category")) {
			nogCateMap = functionMap.get("NOG category");
		}

		HashMap<String, String> keggMap = null;
		if (functionMap.containsKey("KEGG")) {
			keggMap = functionMap.get("KEGG");
			title.append("\"KEGG accession\"").append(",");
			title.append("\"KEGG name\"").append(",");
		}

		HashMap<String, String> koMap = null;
		if (functionMap.containsKey("KO")) {
			koMap = functionMap.get("KO");
			title.append("\"KO accession\"").append(",");
			title.append("\"KO name\"").append(",");
		}

		HashMap<String, String> goMap = null;
		if (functionMap.containsKey("GO")) {
			goMap = functionMap.get("GO");

			title.append("\"GOBP accession\"").append(",");
			title.append("\"GOBP name\"").append(",");

			title.append("\"GOCC accession\"").append(",");
			title.append("\"GOCC name\"").append(",");

			title.append("\"GOMF accession\"").append(",");
			title.append("\"GOMF name\"").append(",");
		}

		if (this.searchType.equals(MetaConstants.maxQuant) && this.quanMode.equals(MetaConstants.labelFree)) {
			for (String exp : fileNames) {
				title.append("\"").append(exp.replace("LFQ intensity", "MS2 count")).append("\",");
			}
		}

		for (String exp : fileNames) {
			title.append("\"" + exp).append("\",");
		}
		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		for (MetaProteinAnno1 mpa : proteins) {

			MetaProtein protein = mpa.getPro();

			StringBuilder sb = new StringBuilder();

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
			}

			if (cogMap != null && cogCateMap != null) {
				String cog = mpa.getCOG() == null ? "" : mpa.getCOG();
				sb.append("\"").append(cog).append("\"").append(",");

				String cogName = cogMap.containsKey(cog) ? cogMap.get(cog) : "";
				sb.append("\"").append(cogName).append("\"").append(",");

				String cogCate = cogCateMap.containsKey(cog) ? cogCateMap.get(cog) : "";
				sb.append("\"").append(cogCate).append("\"").append(",");
			}

			if (nogMap != null && nogCateMap != null) {
				String nog = mpa.getNOG() == null ? "" : mpa.getNOG();
				sb.append("\"").append(nog).append("\"").append(",");

				String nogName = nogMap.containsKey(nog) ? nogMap.get(nog) : "";
				sb.append("\"").append(nogName).append("\"").append(",");

				String nogCate = nogCateMap.containsKey(nog) ? nogCateMap.get(nog) : "";
				sb.append("\"").append(nogCate).append("\"").append(",");
			}

			if (keggMap != null) {
				String kegg = mpa.getKEGG() == null ? "" : mpa.getKEGG();
				sb.append("\"").append(kegg).append("\"").append(",");

				String keggName = keggMap.containsKey(kegg) ? keggMap.get(kegg) : "";
				sb.append("\"").append(keggName).append("\"").append(",");
			}

			if (koMap != null) {
				String ko = mpa.getKO() == null ? "" : mpa.getKO();
				sb.append("\"").append(ko).append("\"").append(",");

				String koName = koMap.containsKey(ko) ? koMap.get(ko) : "";
				sb.append("\"").append(koName).append("\"").append(",");
			}

			if (goMap != null) {
				String[] gobp = mpa.getGOBP();
				if (gobp != null && gobp.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gobp) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append("\"").append(sb1).append("\",");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append("\"").append(sb2).append("\",");

				} else {
					sb.append("\"\",\"\",");
				}

				String[] gocc = mpa.getGOCC();
				if (gocc != null && gocc.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gocc) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append("\"").append(sb1).append("\",");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append("\"").append(sb2).append("\",");

				} else {
					sb.append("\"\",\"\",");
				}

				String[] gomf = mpa.getGOMF();
				if (gomf != null && gomf.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gomf) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append("\"").append(sb1).append("\",");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append("\"").append(sb2).append("\",");

				} else {
					sb.append("\"\",\"\",");
				}
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

	public void exportTxt(String txt) throws IOException {
		PrintWriter writer = new PrintWriter(txt);
		StringBuilder title = new StringBuilder();

		if (this.searchType.equals(MetaConstants.maxQuant)) {
			title.append("Group_ID").append("\t");
			title.append("Protein_ID").append("\t");
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("Unique peptide count").append("\t");
			title.append("E value").append("\t");
		} else if (this.searchType.equals(MetaConstants.openSearch)) {
			title.append("Name").append("\t");
			title.append("Peptide count").append("\t");
			title.append("PSM count").append("\t");
			title.append("Score").append("\t");
		}

		HashMap<String, String> cogMap = null;
		if (functionMap.containsKey("COG")) {
			cogMap = functionMap.get("COG");
			title.append("COG accession").append("\t");
			title.append("COG name").append("\t");
			title.append("COG category").append("\t");
		}

		HashMap<String, String> cogCateMap = null;
		if (functionMap.containsKey("COG category")) {
			cogCateMap = functionMap.get("COG category");
		}

		HashMap<String, String> nogMap = null;
		if (functionMap.containsKey("NOG")) {
			nogMap = functionMap.get("NOG");
			title.append("NOG accession").append("\t");
			title.append("NOG name").append("\t");
			title.append("NOG category").append("\t");
		}

		HashMap<String, String> nogCateMap = null;
		if (functionMap.containsKey("NOG category")) {
			nogCateMap = functionMap.get("NOG category");
		}

		HashMap<String, String> keggMap = null;
		if (functionMap.containsKey("KEGG")) {
			keggMap = functionMap.get("KEGG");
			title.append("KEGG accession").append("\t");
			title.append("KEGG name").append("\t");
		}

		HashMap<String, String> koMap = null;
		if (functionMap.containsKey("KO")) {
			koMap = functionMap.get("KO");
			title.append("KO accession").append("\t");
			title.append("KO name").append("\t");
		}

		HashMap<String, String> goMap = null;
		if (functionMap.containsKey("GO")) {
			goMap = functionMap.get("GO");

			title.append("GOBP accession").append("\t");
			title.append("GOBP name").append("\t");

			title.append("GOCC accession").append("\t");
			title.append("GOCC name").append("\t");

			title.append("GOMF accession").append("\t");
			title.append("GOMF name").append("\t");
		}

		if (this.searchType.equals(MetaConstants.maxQuant)) {
			for (String exp : fileNames) {
				title.append("MS2 count " + exp).append("\t");
			}
		}

		for (String exp : fileNames) {
			title.append("Intensity " + exp).append("\t");
		}
		writer.println(title);

		for (MetaProteinAnno1 mpa : proteins) {

			MetaProtein protein = mpa.getPro();
			StringBuilder sb = new StringBuilder();
			if (this.searchType.equals(MetaConstants.maxQuant)) {
				sb.append(protein.getGroupId()).append("\t");
				sb.append(protein.getProteinId()).append("\t");
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getUniPepCount()).append("\t");
				sb.append(protein.getEvalue()).append("\t");
			} else if (this.searchType.equals(MetaConstants.openSearch)) {
				sb.append(protein.getName()).append("\t");
				sb.append(protein.getPepCount()).append("\t");
				sb.append(protein.getPsmCount()).append("\t");
				sb.append(protein.getScore()).append("\t");
			}

			if (cogMap != null && cogCateMap != null) {
				String cog = mpa.getCOG() == null ? "" : mpa.getCOG();
				sb.append(cog).append("\t");

				String cogName = cogMap.containsKey(cog) ? cogMap.get(cog) : "";
				sb.append(cogName).append("\t");

				String cogCate = cogCateMap.containsKey(cog) ? cogCateMap.get(cog) : "";
				sb.append(cogCate).append("\t");
			}

			if (nogMap != null && nogCateMap != null) {
				String nog = mpa.getNOG() == null ? "" : mpa.getNOG();
				sb.append(nog).append("\t");

				String nogName = nogMap.containsKey(nog) ? nogMap.get(nog) : "";
				sb.append(nogName).append("\t");

				String nogCate = nogCateMap.containsKey(nog) ? nogCateMap.get(nog) : "";
				sb.append(nogCate).append("\t");
			}

			if (keggMap != null) {
				String kegg = mpa.getKEGG() == null ? "" : mpa.getKEGG();
				sb.append(kegg).append("\t");

				String keggName = keggMap.containsKey(kegg) ? keggMap.get(kegg) : "";
				sb.append(keggName).append("\t");
			}

			if (koMap != null) {
				String ko = mpa.getKO() == null ? "" : mpa.getKO();
				sb.append(ko).append("\t");

				String koName = koMap.containsKey(ko) ? koMap.get(ko) : "";
				sb.append(koName).append("\t");
			}

			if (goMap != null) {
				String[] gobp = mpa.getGOBP();
				if (gobp != null && gobp.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gobp) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append(sb1).append("\t");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append(sb2).append("\t");

				} else {
					sb.append("\t\t");
				}

				String[] gocc = mpa.getGOCC();
				if (gocc != null && gocc.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gocc) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append(sb1).append("\t");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append(sb2).append("\t");

				} else {
					sb.append("\t\t");
				}

				String[] gomf = mpa.getGOMF();
				if (gomf != null && gomf.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gomf) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					sb.append(sb1).append("\t");

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					sb.append(sb2).append(",");

				} else {
					sb.append("\t\t");
				}
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
			writer.println(sb);
		}

		writer.close();
	}

	public void exportIntensity(String out) throws FileNotFoundException {

		PrintWriter writer = new PrintWriter(out);
		StringBuilder title = new StringBuilder();
		title.append("Name");
		for (String exp : fileNames) {
			String name = exp.replace("Intensity_Xu_20151106_HFDmice_", "");
			name = name.replace("NCD", "LFD");
			title.append("\t").append(name);
		}
		writer.println(title);

		for (MetaProteinAnno1 mpa : proteins) {

			MetaProtein protein = mpa.getPro();

			StringBuilder sb = new StringBuilder();
			sb.append(protein.getName());
			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				if (intensity == 0) {
					sb.append("\tNA");
				} else {
					sb.append("\t").append(dfe2.format(intensity));
				}
			}
			writer.println(sb);
		}

		writer.close();
	}

	public void exportHtml(String html) throws IOException {
		MetaProteinHtmlWriter htmlWriter = new MetaProteinHtmlWriter(html);
		htmlWriter.write(proteins, functionMap);
		htmlWriter.close();
	}

	public void exportHtml(File html) throws IOException {
		MetaProteinHtmlWriter htmlWriter = new MetaProteinHtmlWriter(html);
		htmlWriter.write(proteins, functionMap);
		htmlWriter.close();
	}

}
