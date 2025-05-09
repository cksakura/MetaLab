/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pepxml;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SequestHTPepxmlReader {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(SequestHTPepxmlReader.class);

	private File[] files;
	private int currentFileId;

	private Iterator<Element> it;
	private Element query;

	private String baseName;
	private HashMap<Double, PostTransModification> ptmMap;

	private SequestHTPSM[] psms;
	private String rev_symbol = "rev_";
	
	public SequestHTPepxmlReader(String xml) {
		this(new File(xml));
	}

	public SequestHTPepxmlReader(File xml) {
		this.files = new File[] { xml };
		this.initial(xml);
	}

	public SequestHTPepxmlReader(File[] xmls) {
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
	public SequestHTPSM next() {

		int scan = Integer.parseInt(query.attributeValue("start_scan"));
		int charge = Integer.parseInt(query.attributeValue("assumed_charge"));
		double precursorMr = Double.parseDouble(query.attributeValue("precursor_neutral_mass"));
		double rt = Double.parseDouble(query.attributeValue("retention_time_sec"));

		Element result = query.element("search_result");
		Iterator<Element> resultIt = result.elementIterator("search_hit");

		if (resultIt.hasNext()) {
			Element peptide = resultIt.next();
			String sequence = peptide.attributeValue("peptide");
			double massdiff = Double.parseDouble(peptide.attributeValue("massdiff"));
			double pepmass = Double.parseDouble(peptide.attributeValue("calc_neutral_pep_mass"));
			int hit_rank = Integer.parseInt(peptide.attributeValue("hit_rank"));
			int num_matched_ions = Integer.parseInt(peptide.attributeValue("num_matched_ions"));
			String pro_name = peptide.attributeValue("protein");
			boolean isTarget = !pro_name.startsWith(rev_symbol);
			
			double xcorr = -1;

			Iterator<Element> scoreIt = peptide.elementIterator("search_score");

			while (scoreIt.hasNext()) {
				Element ss = scoreIt.next();
				String scorename = ss.attributeValue("name");
				double value = Double.parseDouble(ss.attributeValue("value"));
				if (scorename.equals("xcorr")) {
					xcorr = value;
				} 
			}

			SequestHTPSM psm = new SequestHTPSM(baseName, scan, charge, precursorMr, rt, sequence, pepmass, massdiff,
					num_matched_ions, hit_rank, pro_name, xcorr, isTarget);

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
				if (name.endsWith("pepXML"))
					return true;
				return false;
			}
		});

		int[] count = new int[2];

		for (int i = 0; i < pepxmlFiles.length; i++) {
			SequestHTPepxmlReader reader = new SequestHTPepxmlReader(pepxmlFiles[i]);
			SequestHTPSM[] psms = reader.getPSMs();

			for (int j = 0; j < psms.length; j++) {
				if(psms[j].getHit_rank()>1) {
					continue;
				}
				if (psms[j].getProtein().startsWith("rev_")) {
					count[1]++;
				} else {
					count[0]++;
				}
			}

			LOGGER.info("Reading pepXML file " + pepxmlFiles[i] + ", " + psms.length + " PSMs obtained" + "\t"
					+ count[0] + "\t" + count[1] + "\t" + (double) count[1] / (double) count[0]);
		}
	}

	public HashMap<Double, PostTransModification> getPTMMap() {
		return ptmMap;
	}

	public HashMap<Integer, SequestHTPSM> getPsmMap() {
		HashMap<Integer, SequestHTPSM> map = new HashMap<Integer, SequestHTPSM>();
		while (hasNext()) {
			SequestHTPSM psm = this.next();
			map.put(psm.getScan(), psm);
		}
		return map;
	}

	public SequestHTPSM[] getPSMs() {
		if (this.psms == null) {
			ArrayList<SequestHTPSM> list = new ArrayList<SequestHTPSM>();
			while (hasNext()) {
				SequestHTPSM psm = this.next();
				if(psm!=null) {
					list.add(psm);
				}
			}

			this.psms = list.toArray(new SequestHTPSM[list.size()]);
		}
		return psms;
	}
}
