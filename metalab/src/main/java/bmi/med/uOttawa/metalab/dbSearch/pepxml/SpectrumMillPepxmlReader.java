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
public class SpectrumMillPepxmlReader {
	
	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(SpectrumMillPepxmlReader.class);

	private File[] files;
	private int currentFileId;

	private Iterator<Element> it;
	private Element query;

	private String baseName;
	private HashMap<Double, PostTransModification> ptmMap;

	private SpectrumMillPSM[] psms;
	private String rev_symbol = "rev_";
	
	public SpectrumMillPepxmlReader(String xml) {
		this(new File(xml));
	}

	public SpectrumMillPepxmlReader(File xml) {
		this.files = new File[] { xml };
		this.initial(xml);
	}

	public SpectrumMillPepxmlReader(File[] xmls) {
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
	public SpectrumMillPSM next() {

		int scan = Integer.parseInt(query.attributeValue("start_scan"));
		int charge = Integer.parseInt(query.attributeValue("assumed_charge"));
		double precursorMr = Double.parseDouble(query.attributeValue("precursor_neutral_mass"));

		Element result = query.element("search_result");
		Iterator<Element> resultIt = result.elementIterator();

		if (resultIt.hasNext()) {
			Element peptide = resultIt.next();
			String sequence = peptide.attributeValue("peptide");
			double massdiff = Double.parseDouble(peptide.attributeValue("massdiff"));
			double pepmass = Double.parseDouble(peptide.attributeValue("calc_neutral_pep_mass"));
			int hit_rank = Integer.parseInt(peptide.attributeValue("hit_rank"));
			String pro_name = peptide.attributeValue("protein");
			boolean isTarget = !pro_name.startsWith(rev_symbol);

			double smScore = -1;
			double deltaRank12Score = -1;
			double deltaFRScore = -1;
			
			Iterator<Element> scoreIt = peptide.elementIterator("search_score");

			while (scoreIt.hasNext()) {
				Element ss = scoreIt.next();
				String scorename = ss.attributeValue("name");
				if (scorename.equals("SMscore")) {
					smScore = Double.parseDouble(ss.attributeValue("value"));
				} else if (scorename.equals("deltaRank1Rank2Score")) {
					deltaRank12Score = Double.parseDouble(ss.attributeValue("value"));
				} 
				else if (scorename.equals("deltaForwardReverseScore")) {
					deltaFRScore = Double.parseDouble(ss.attributeValue("value"));
				} 
			}

			SpectrumMillPSM psm = new SpectrumMillPSM(baseName, scan, charge, precursorMr, 0, sequence, pepmass,
					massdiff, 0, hit_rank, pro_name, smScore, deltaFRScore, deltaRank12Score, isTarget);

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


		int[][] count = new int[200][2];
		int[] count2 = new int[200];
		
		for (int i = 0; i < pepxmlFiles.length; i++) {
			SpectrumMillPepxmlReader reader = new SpectrumMillPepxmlReader(pepxmlFiles[i]);
			SpectrumMillPSM[] psms = reader.getPSMs();
			// LOGGER.info("Reading pepXML file " + pepxmlFiles[i] + ", " + psms.length + "
			// PSMs obtained");
			
			int target = 0;
			int decoy = 0;
			
			for (int j = 0; j < psms.length; j++) {
				double score = psms[j].getSmScore();
//				double score = (psms[j].getSmScore() - 4) * 100;
				if (!psms[j].getProtein().startsWith("rev_")) {
					if (score > count.length) {
						for (int k = 0; k < count.length; k++) {
							count[k][0]++;
						}

					} else {
						for (int k = 0; k < score; k++) {
							count[k][0]++;
						}

					}
				} else {
//					System.out.println("sss\t"+psms[i].getScan()+"\t"+psms[i].getSequence()+"\t"+psms[i].getSmScore());

					if (score > count.length) {
						for (int k = 0; k < count.length; k++) {
							count[k][1]++;
						}
						count2[count2.length - 1]++;
					} else {
						for (int k = 0; k < score; k++) {
							count[k][1]++;
						}
						count2[(int) score]++;
					}
				}
			}
		}

		for (int i = 0; i < count.length; i++) {
			double fdr = (double) count[i][1] / (double) count[i][0];
			System.out.println(i+"\t"+count[i][0] + "\t" + count[i][1] + "\t" + fdr);
		}
		
		for (int i = 0; i < count2.length; i++) {
			System.out.println(i+"\t"+count2[i]);
		}
	}

	public HashMap<Double, PostTransModification> getPTMMap() {
		return ptmMap;
	}

	public HashMap<Integer, SpectrumMillPSM> getPsmMap() {
		HashMap<Integer, SpectrumMillPSM> map = new HashMap<Integer, SpectrumMillPSM>();
		while (hasNext()) {
			SpectrumMillPSM psm = this.next();
			map.put(psm.getScan(), psm);
		}
		return map;
	}

	public SpectrumMillPSM[] getPSMs() {
		if (this.psms == null) {
			ArrayList<SpectrumMillPSM> list = new ArrayList<SpectrumMillPSM>();
			while (hasNext()) {
				SpectrumMillPSM psm = this.next();
				list.add(psm);
			}

			this.psms = list.toArray(new SpectrumMillPSM[list.size()]);
		}
		return psms;
	}

	@SuppressWarnings("unchecked")
	public SpectrumMillPSM[][] getPSMArrays() {

		ArrayList<SpectrumMillPSM>[] list = new ArrayList[this.files.length];
		for (int i = 0; i < list.length; i++) {
			list[i] = new ArrayList<SpectrumMillPSM>();
		}
		String basename = null;
		int fileid = -1;

		while (hasNext()) {
			SpectrumMillPSM psm = this.next();
			if (psm.getFileName().equals(basename)) {
				list[fileid].add(psm);
			} else {
				basename = psm.getFileName();
				fileid++;
				list[fileid].add(psm);
			}
		}

		SpectrumMillPSM[][] psmarrays = new SpectrumMillPSM[this.files.length][];
		for (int i = 0; i < psmarrays.length; i++) {
			psmarrays[i] = list[i].toArray(new SpectrumMillPSM[list[i].size()]);
		}

		return psmarrays;
	}
}
