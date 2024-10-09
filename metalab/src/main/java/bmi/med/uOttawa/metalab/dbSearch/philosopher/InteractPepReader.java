/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.philosopher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
public class InteractPepReader {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(InteractPepReader.class);

	private Iterator<Element> summaryIt;
	private Iterator<Element> it;
	private Element query;

	private String baseName;
	private HashMap<Double, PostTransModification> ptmMap;

	private String rev_symbol = "rev_";

	private InteractPsm[] psms;

	public InteractPepReader(String xml) {
		this(new File(xml));
	}

	public InteractPepReader(File xml) {
		this.initial(xml);
	}

	private void initial(File xml) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading PSMs from " + xml, e);
		}
		Element root = document.getRootElement();

		this.summaryIt = root.elementIterator("msms_run_summary");

		this.nextSummary();
	}

	private boolean nextSummary() {
		if (summaryIt.hasNext()) {

			Element summary = summaryIt.next();

			this.baseName = summary.attributeValue("base_name");
			int baseId = baseName.lastIndexOf("/");
			if (baseId > 0) {
				this.baseName = this.baseName.substring(baseId + 1);
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

			return true;

		} else {
			return false;
		}
	}

	public boolean hasNext() {

		if (it.hasNext()) {
			this.query = it.next();
			return true;

		} else {

			if (this.nextSummary()) {
				return hasNext();
			}

			return false;
		}
	}

	public InteractPsm next() {

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

			double hyperScore = 0;
			double nextScore = 0;
			double expect = 0;

			Iterator<Element> scoreIt = peptide.elementIterator("search_score");

			while (scoreIt.hasNext()) {
				Element ss = scoreIt.next();
				String scorename = ss.attributeValue("name");
				double value = Double.parseDouble(ss.attributeValue("value"));
				if (scorename.equals("hyperscore")) {
					hyperScore = value;
				} else if (scorename.equals("nextscore")) {
					nextScore = value;
				} else if (scorename.equals("expect")) {
					expect = value;
				}
			}

			Element ar = peptide.element("analysis_result");
			Element pp = ar.element("peptideprophet_result");

			double probability = Double.parseDouble(pp.attributeValue("probability"));

			InteractPsm psm = new InteractPsm(baseName, scan, charge, precursorMr, rt, sequence, pepmass, massdiff,
					num_matched_ions, hit_rank, pro_name + " " + pro_des, hyperScore, nextScore, expect, probability,
					isTarget);

			return psm;
		}

		return null;
	}

	public HashMap<Double, PostTransModification> getPTMMap() {
		return ptmMap;
	}

	public InteractPsm[] getPSMs() {
		if (this.psms == null) {
			ArrayList<InteractPsm> list = new ArrayList<InteractPsm>();
			while (hasNext()) {
				InteractPsm psm = this.next();
				list.add(psm);
			}

			this.psms = list.toArray(new InteractPsm[list.size()]);
		}
		return psms;
	}

}
