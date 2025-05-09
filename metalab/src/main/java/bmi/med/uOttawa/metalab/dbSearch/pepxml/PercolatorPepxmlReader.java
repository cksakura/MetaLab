/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pepxml;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

/**
 * @author Kai Cheng
 *
 */
public class PercolatorPepxmlReader {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(PercolatorPepxmlReader.class);

	private File[] files;
	private int currentFileId;

	private Iterator<Element> it;
	private Element query;

	private String baseName;
	private HashMap<Double, PostTransModification> ptmMap;

	private PercolatorPSM[] psms;

	private String rev_symbol = "rev_";
	
	public PercolatorPepxmlReader(String xml) {
		this(new File(xml));
	}

	public PercolatorPepxmlReader(File xml) {
		this.files = new File[] { xml };
		this.initial(xml);
	}

	public PercolatorPepxmlReader(File[] xmls) {
		this.files = xmls;
		this.initial(xmls[0]);
	}

	@SuppressWarnings("unchecked")
	private void initial(File xml) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading PSMs from " + xml, e);
			return;
		}
		Element root = document.getRootElement();

		Element summary = root.element("msms_run_summary");
		this.baseName = summary.attributeValue("base_name");
		
		Element anaSummary = root.element("analysis_summary");
		Element workflow = anaSummary.element("summary").element("WorkflowMessages");
		Iterator<Element> workflowIt = workflow.elementIterator("Message");
		while (workflowIt.hasNext()) {
			Element message = workflowIt.next();
			String text = message.attributeValue("text");
			int id = text.indexOf("high confident target/decoy peptides");
			if (id > 0) {
				String sub = text.substring(0, id).trim();
				int subid = sub.indexOf("/");
				if (subid > 0) {
					int target = Integer.parseInt(sub.substring(0, subid));
					int decoy = Integer.parseInt(sub.substring(subid + 1));
					System.out.println(baseName+"\t"+target+"\t"+decoy);
				}
			}
		}

		if (this.ptmMap == null) {
			Element e_search_summary = summary.element("search_summary");
			this.ptmMap = new HashMap<Double, PostTransModification>();

			if (e_search_summary != null) {
				Iterator<Element> modit = e_search_summary.elementIterator("aminoacid_modification");
				while (modit.hasNext()) {
					Element emod = modit.next();
					String site = emod.attributeValue("aminoacid");
					boolean vari = emod.attributeValue("variable").equals("Y");

					double massdiff = Double.parseDouble(emod.attributeValue("massdiff"));
					double mass = Double.parseDouble(emod.attributeValue("mass"));
					PostTransModification ptm = new PostTransModification(massdiff, "", site, vari);
					this.ptmMap.put(mass, ptm);
				}

				modit = e_search_summary.elementIterator("terminal_modification");
				while (modit.hasNext()) {
					Element emod = modit.next();

					boolean pro_term = emod.attributeValue("protein_terminus").equals("Y");
					boolean n_term = emod.attributeValue("terminus").equals("N");
					String site = "";
					if (pro_term) {
						if (n_term) {
							site = "[*";
						} else {
							site = "]*";
						}
					} else {
						if (n_term) {
							site = "n*";
						} else {
							site = "c*";
						}
					}
					double massdiff = Double.parseDouble(emod.attributeValue("massdiff"));
					double mass = Double.parseDouble(emod.attributeValue("mass"));
					boolean vari = emod.attributeValue("variable").equals("Y");

					PostTransModification ptm = new PostTransModification(massdiff, "", site, vari);
					this.ptmMap.put(mass, ptm);
				}
			}
		}
		this.it = summary.elementIterator("spectrum_query");
	}

	public boolean hasNext() {

		if (it.hasNext()) {
			this.query = it.next();
			return true;

		} else {
			this.currentFileId++;
			if (currentFileId < this.files.length) {
				this.initial(this.files[currentFileId]);
				return hasNext();
			} else {
				return false;

			}
		}
	}

	@SuppressWarnings("unchecked")
	public PercolatorPSM next() {

		int scan = Integer.parseInt(query.attributeValue("start_scan"));
		int charge = Integer.parseInt(query.attributeValue("assumed_charge"));
		double precursorMr = Double.parseDouble(query.attributeValue("precursor_neutral_mass"));
		double rt = Double.parseDouble(query.attributeValue("retention_time_sec"));

		Element result = query.element("search_result");
		Iterator<Element> resultIt = result.elementIterator();

		if (resultIt.hasNext()) {
			Element peptide = resultIt.next();
			String sequence = peptide.attributeValue("peptide");
			double massdiff = Double.parseDouble(peptide.attributeValue("massdiff"));
			double pepmass = Double.parseDouble(peptide.attributeValue("calc_neutral_pep_mass"));
			int hit_rank = Integer.parseInt(peptide.attributeValue("hit_rank"));
			int num_matched_ions = Integer.parseInt(peptide.attributeValue("num_matched_ions"));
			String pro_name = peptide.attributeValue("protein");
			String pro_des = peptide.attributeValue("protein_descr");
			boolean isTarget = !pro_name.startsWith(rev_symbol);

			double per_q_value = -1;
			double per_PEP = -1;
			double per_svm = -1;

			Iterator<Element> scoreIt = peptide.elementIterator("search_score");

			while (scoreIt.hasNext()) {
				Element ss = scoreIt.next();
				String scorename = ss.attributeValue("name");
				double value = Double.parseDouble(ss.attributeValue("value"));
				if (scorename.equals("Percolator q-Value")) {
					per_q_value = value;
				} else if (scorename.equals("Percolator PEP")) {
					per_PEP = value;
				} else if (scorename.equals("Percolator SVMScore")) {
					per_svm = value;
				}
			}

			PercolatorPSM psm = new PercolatorPSM(baseName, scan, charge, precursorMr, rt, sequence, pepmass, massdiff,
					num_matched_ions, hit_rank, pro_name + " " + pro_des, per_q_value, per_PEP, per_svm, isTarget);

			return psm;
		}
		return null;
	}

	@SuppressWarnings("unused")
	private static void batchRead(String dir) throws IOException, DocumentException {
		File[] pepxmlFiles = (new File(dir)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("pep.xml"))
					return true;
				return false;
			}
		});
		for (int i = 0; i < pepxmlFiles.length; i++) {
			PercolatorPepxmlReader reader = new PercolatorPepxmlReader(pepxmlFiles[i]);
			PercolatorPSM[] psms = reader.getPSMs();
			LOGGER.info("Reading pepXML file " + pepxmlFiles[i] + ", " + psms.length + " PSMs obtained");
		}

	}

	public HashMap<Double, PostTransModification> getPTMMap() {
		return ptmMap;
	}

	public HashMap<Integer, PercolatorPSM> getPsmMap() {
		HashMap<Integer, PercolatorPSM> map = new HashMap<Integer, PercolatorPSM>();
		while (hasNext()) {
			PercolatorPSM psm = this.next();
			map.put(psm.getScan(), psm);
		}
		return map;
	}

	public PercolatorPSM[] getPSMs() {
		if (this.psms == null) {
			ArrayList<PercolatorPSM> list = new ArrayList<PercolatorPSM>();
			while (hasNext()) {
				PercolatorPSM psm = this.next();
				list.add(psm);
			}

			this.psms = list.toArray(new PercolatorPSM[list.size()]);
		}
		return psms;
	}

	@SuppressWarnings("unchecked")
	public PercolatorPSM[][] getPSMArrays() {

		ArrayList<PercolatorPSM>[] list = new ArrayList[this.files.length];
		for (int i = 0; i < list.length; i++) {
			list[i] = new ArrayList<PercolatorPSM>();
		}
		String basename = null;
		int fileid = -1;

		while (hasNext()) {
			PercolatorPSM psm = this.next();
			if (psm.getFileName().equals(basename)) {
				list[fileid].add(psm);
			} else {
				basename = psm.getFileName();
				fileid++;
				list[fileid].add(psm);
			}
		}

		PercolatorPSM[][] psmarrays = new PercolatorPSM[this.files.length][];
		for (int i = 0; i < psmarrays.length; i++) {
			psmarrays[i] = list[i].toArray(new PercolatorPSM[list[i].size()]);
		}

		return psmarrays;
	}
	
	@SuppressWarnings("unused")
	private static void biomassTest(String fasta, String pepxml) throws IOException {
		/*
		 * HashSet<String> proset = new HashSet<String>(); BufferedReader freader = new
		 * BufferedReader(new FileReader(fasta)); String line = null; while ((line =
		 * freader.readLine()) != null) { if (line.startsWith(">")) { String name =
		 * line.split("\\s+")[0]; proset.add(name.substring(1)); } } freader.close();
		 * 
		 * System.out.println(proset.size());
		 */
		File[] pepxmlFiles = (new File(pepxml)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("pep.xml"))
					return true;
				return false;
			}
		});

		int psmcount = 0;
		HashSet<String> idenProSet = new HashSet<String>();
		HashSet<String> idenPepSet = new HashSet<String>();
		for (int i = 0; i < pepxmlFiles.length; i++) {
			PercolatorPepxmlReader reader = new PercolatorPepxmlReader(pepxmlFiles[i]);
			PercolatorPSM[] psms = reader.getPSMs();
			for (int j = 0; j < psms.length; j++) {

				if (psms[j].getPer_q_value() >= 0.01) {
					continue;
				}
				String pro = psms[j].getProtein();
				String name = pro.split("\\s+")[0];
				int id = name.indexOf("&#x9");
				if (id > 0) {
					name = name.substring(0, id);
				}
				psmcount++;
				idenProSet.add(name);
				idenPepSet.add(psms[j].getSequence());
			}
		}
		System.out.println("psm count\t" + psmcount);
		System.out.println("pro count\t" + idenProSet.size());
		System.out.println("pep count\t" + idenPepSet.size());
	}
}
