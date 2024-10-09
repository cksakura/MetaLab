/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.core.function.CategoryFinder;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaProteinXMLWriter {

	protected String searchType;
	protected String quanType;
	protected File output;
	protected Document document;
	protected XMLWriter writer;
	protected Element root;
	protected Element proRoot;
	protected Element taxonRoot;

	private HashSet<Integer> taxonIdSet;

	protected static final Logger LOGGER = LogManager.getLogger(MetaProteinXMLWriter.class);

	public MetaProteinXMLWriter(String output, String searchType, String quanType, String... fileNames) {
		this(new File(output), searchType, quanType, fileNames);
	}

	public MetaProteinXMLWriter(File output, String searchType, String quanType, String... fileNames) {
		// TODO Auto-generated constructor stub

		this.searchType = searchType;
		this.quanType = quanType;
		this.output = output;
		this.document = DocumentHelper.createDocument();
		OutputStreamWriter bufferedWriter = null;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(output), "UTF8");
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing proteins to " + output, e);
		}
		this.writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
		this.root = DocumentFactory.getInstance().createElement("Protein_function");
		this.document.setRootElement(root);

		StringBuilder sb = new StringBuilder();
		for (String name : fileNames) {
			sb.append(name).append(";");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		this.taxonIdSet = new HashSet<Integer>();

		this.root.addAttribute("fileNames", sb.toString());
		this.root.addAttribute("searchType", searchType);
		this.root.addAttribute("quanMode", quanType);

		this.proRoot = DocumentFactory.getInstance().createElement("Proteins");
		this.root.add(proRoot);

		this.taxonRoot = DocumentFactory.getInstance().createElement("Taxa");
		this.root.add(taxonRoot);
	}

	public void addProteins(MetaProteinAnno1[] proteins, HashMap<String, String>[] maps, String[] funNames,
			CategoryFinder cf) {

		this.root.addAttribute("Protein_count", String.valueOf(proteins.length));

		for (MetaProteinAnno1 mp : proteins) {
			this.addProtein(mp);
		}

		HashMap<String, String> cogCatMap = cf.getCOGMap();
		HashMap<String, String> nogCatMap = cf.getNOGMap();

		HashMap<String, String> gomap = new HashMap<String, String>();
		for (int i = 0; i < funNames.length; i++) {
			if (funNames[i].startsWith("GO")) {
				gomap.putAll(maps[i]);
			} else if (funNames[i].startsWith("KEGG")) {
				Element rooti = DocumentFactory.getInstance().createElement("KEGGs");
				for (String key : maps[i].keySet()) {
					Element e = DocumentFactory.getInstance().createElement("KEGG");
					e.addAttribute("Accession", key);
					e.addAttribute("Function", maps[i].get(key));
					rooti.add(e);
				}
				this.root.add(rooti);
				this.root.addAttribute("KEGG_count", String.valueOf(maps[i].size()));

			} else if (funNames[i].startsWith("KO")) {
				Element rooti = DocumentFactory.getInstance().createElement("KOs");
				for (String key : maps[i].keySet()) {
					Element e = DocumentFactory.getInstance().createElement("KO");
					e.addAttribute("Accession", key);
					e.addAttribute("Function", maps[i].get(key));
					rooti.add(e);
				}
				this.root.add(rooti);
				this.root.addAttribute("KO_count", String.valueOf(maps[i].size()));

			} else if (funNames[i].startsWith("COG")) {
				Element rooti = DocumentFactory.getInstance().createElement("COGs");
				for (String key : maps[i].keySet()) {
					Element e = DocumentFactory.getInstance().createElement("COG");
					e.addAttribute("Accession", key);
					e.addAttribute("Function", maps[i].get(key));
					if (cogCatMap.containsKey(key)) {
						e.addAttribute("Category", cogCatMap.get(key));
					} else {
						e.addAttribute("Category", "");
					}
					rooti.add(e);
				}
				this.root.add(rooti);
				this.root.addAttribute("COG_count", String.valueOf(maps[i].size()));

			} else if (funNames[i].startsWith("NOG")) {
				Element rooti = DocumentFactory.getInstance().createElement("NOGs");
				for (String key : maps[i].keySet()) {
					Element e = DocumentFactory.getInstance().createElement("NOG");
					e.addAttribute("Accession", key);
					e.addAttribute("Function", maps[i].get(key));
					if (nogCatMap.containsKey(key)) {
						e.addAttribute("Category", nogCatMap.get(key));
					} else {
						e.addAttribute("Category", "");
					}
					rooti.add(e);
				}
				this.root.add(rooti);
				this.root.addAttribute("NOG_count", String.valueOf(maps[i].size()));
			}
		}

		Element goRoot = DocumentFactory.getInstance().createElement("GOs");
		for (String key : gomap.keySet()) {
			Element e = DocumentFactory.getInstance().createElement("GO");
			e.addAttribute("Accession", key);
			e.addAttribute("Function", gomap.get(key));
			goRoot.add(e);
		}
		this.root.add(goRoot);
		this.root.addAttribute("GO_count", String.valueOf(gomap.size()));
	}

	private void addProtein(MetaProteinAnno1 mpa) {

		Element ePro = DocumentFactory.getInstance().createElement("Protein");
		MetaProtein protein = mpa.getPro();

		ePro.addAttribute("Name", protein.getName());

		if (this.searchType.equals(MetaConstants.maxQuant)) {

			ePro.addAttribute("Group_ID", String.valueOf(protein.getGroupId()));
			ePro.addAttribute("Protein_ID", String.valueOf(protein.getProteinId()));
			ePro.addAttribute("Peptide_count", String.valueOf(protein.getPepCount()));
			ePro.addAttribute("Razor_unique_peptide_count", String.valueOf(protein.getRazorUniPepCount()));
			ePro.addAttribute("Unique_peptide_count", String.valueOf(protein.getUniPepCount()));
			ePro.addAttribute("E_value", String.valueOf(protein.getEvalue()));

			int[] ms2Counts = protein.getMs2Counts();
			StringBuilder ms2CountSb = new StringBuilder();
			for (int ms2c : ms2Counts) {
				ms2CountSb.append(ms2c).append("_");
			}
			if (ms2CountSb.length() > 0) {
				ms2CountSb.deleteCharAt(ms2CountSb.length() - 1);
			}
			ePro.addAttribute("ms2Counts", ms2CountSb.toString());

		} else if (this.searchType.equals(MetaConstants.openSearch)) {

			ePro.addAttribute("Group_ID", String.valueOf(protein.getGroupId()));
			ePro.addAttribute("Peptide_count", String.valueOf(protein.getPepCount()));
			ePro.addAttribute("PSM_count", String.valueOf(protein.getPsmCount()));
			ePro.addAttribute("Score", String.valueOf(protein.getScore()));

		} else if (this.searchType.equals(MetaConstants.pFind)) {

			ePro.addAttribute("Group_ID", String.valueOf(protein.getGroupId()));
			ePro.addAttribute("Peptide_count", String.valueOf(protein.getPepCount()));
			ePro.addAttribute("PSM_count", String.valueOf(protein.getPsmCount()));
			ePro.addAttribute("Score", String.valueOf(protein.getScore()));
		}

		double[] intensities = protein.getIntensities();
		StringBuilder intensitySb = new StringBuilder();
		for (double inten : intensities) {
			intensitySb.append(inten).append("_");
		}
		if (intensitySb.length() > 0) {
			intensitySb.deleteCharAt(intensitySb.length() - 1);
		}

		ePro.addAttribute("intensities", intensitySb.toString());

		ePro.addAttribute("COG", String.valueOf(mpa.getCOG()));
		ePro.addAttribute("NOG", String.valueOf(mpa.getNOG()));
		ePro.addAttribute("KEGG", String.valueOf(mpa.getKEGG()));
		ePro.addAttribute("KO", String.valueOf(mpa.getKO()));

		String[] GOBPs = mpa.getGOBP();
		StringBuilder GOBPsb = new StringBuilder();
		if (GOBPs != null) {
			for (String gobp : GOBPs) {
				GOBPsb.append(gobp).append("_");
			}
			if (GOBPsb.length() > 0) {
				GOBPsb.deleteCharAt(GOBPsb.length() - 1);
			}
		}
		ePro.addAttribute("GOBP", GOBPsb.toString());

		String[] GOCCs = mpa.getGOCC();
		StringBuilder GOCCsb = new StringBuilder();
		if (GOCCs != null) {
			for (String gocc : GOCCs) {
				GOCCsb.append(gocc).append("_");
			}
			if (GOCCsb.length() > 0) {
				GOCCsb.deleteCharAt(GOCCsb.length() - 1);
			}
		}

		ePro.addAttribute("GOCC", GOCCsb.toString());

		String[] GOMFs = mpa.getGOMF();
		StringBuilder GOMFsb = new StringBuilder();
		if (GOMFs != null) {
			for (String gomf : GOMFs) {
				GOMFsb.append(gomf).append("_");
			}
			if (GOMFsb.length() > 0) {
				GOMFsb.deleteCharAt(GOMFsb.length() - 1);
			}
		}
		ePro.addAttribute("GOMF", GOMFsb.toString());

		int lcaid = mpa.getTaxId();
		ePro.addAttribute("lcaId", String.valueOf(lcaid));

		this.proRoot.add(ePro);
	}

	public boolean addTaxon(Taxon taxon) {
		int id = taxon.getId();
		if (taxonIdSet.contains(id)) {
			return false;
		} else {
			taxonIdSet.add(id);

			String name = taxon.getName();
			int parentId = taxon.getParentId();
			int rankId = taxon.getRankId();
			int rootTypeId = taxon.getRootType().getId();

			Element eTaxon = DocumentFactory.getInstance().createElement("Taxon");
			eTaxon.addAttribute("id", String.valueOf(id));
			eTaxon.addAttribute("name", name);
			eTaxon.addAttribute("parentId", String.valueOf(parentId));
			eTaxon.addAttribute("rankId", String.valueOf(rankId));
			eTaxon.addAttribute("rootTypeId", String.valueOf(rootTypeId));

			int[] mainParentIds = taxon.getMainParentIds();

			if (mainParentIds != null) {
				StringBuilder mpsb = new StringBuilder();
				for (int mpid : mainParentIds) {
					mpsb.append(mpid).append(",");
				}
				if (mpsb.length() > 0) {
					mpsb.deleteCharAt(mpsb.length() - 1);
				}
				eTaxon.addAttribute("mainParentIds", mpsb.toString());
			}

			taxonRoot.add(eTaxon);
			return true;
		}
	}

	public void close() {
		try {
			this.writer.write(document);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing proteins to " + output, e);
		}
		try {
			this.writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing proteins to " + output, e);
		}
	}
}
