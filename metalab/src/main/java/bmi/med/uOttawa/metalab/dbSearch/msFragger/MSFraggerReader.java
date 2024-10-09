/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.msFragger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;

/**
 * @author Kai Cheng
 *
 */
public class MSFraggerReader {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(MSFraggerReader.class);
	
	private File[] files;
	private int currentFileId;
	
	private Iterator<Element> it;
	private Element query;
	
	private String baseName;
	private HashMap<Double, PostTransModification> ptmMap;
	
	private MSFraggerPsm[] psms;
	private int[] targetMassDiffBin;
	
	private static int openSearchWindow = 500;
	private static int binCount = 1000000;
	
	private String rev_symbol = "REV_";

	public MSFraggerReader(String xml){
		this(new File(xml));
	}

	public MSFraggerReader(File xml) {
		this.files = new File[] { xml };
		this.initial(xml);
	}
	
	public MSFraggerReader(File[] xmls) {
		this.files = xmls;
		this.initial(xmls[0]);
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

		Element summary = root.element("msms_run_summary");
		this.baseName = summary.attributeValue("base_name");
		int lastId = this.baseName.lastIndexOf("\\");
		if (lastId > 0 && lastId < this.baseName.length()) {
			this.baseName = this.baseName.substring(lastId + 1);
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

	public MSFraggerPsm next() {

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
			int miss = Integer.parseInt(peptide.attributeValue("num_missed_cleavages"));
			int num_tol_term = Integer.parseInt(peptide.attributeValue("num_tol_term"));
			int tot_num_ions = Integer.parseInt(peptide.attributeValue("tot_num_ions"));
			int hit_rank = Integer.parseInt(peptide.attributeValue("hit_rank"));
			int num_matched_ions = Integer.parseInt(peptide.attributeValue("num_matched_ions"));
			String protein = peptide.attributeValue("protein");
			boolean isTarget = !protein.startsWith(rev_symbol);

			double hyperscore = -1;
			double nextscore = -1;
			double expect = -1;

			Iterator<Element> scoreIt = peptide.elementIterator("search_score");

			while (scoreIt.hasNext()) {
				Element ss = scoreIt.next();
				String scorename = ss.attributeValue("name");
				double value = Double.parseDouble(ss.attributeValue("value"));
				if (scorename.equals("hyperscore")) {
					hyperscore = value;
				} else if (scorename.equals("nextscore")) {
					nextscore = value;
				} else if (scorename.equals("expect")) {
					expect = value;
				}
			}

			MSFraggerPsm psm;

			Element modElement = peptide.element("modification_info");

			if (modElement != null) {

				ArrayList<PeptideModification> mlist = new ArrayList<PeptideModification>();
				if (modElement.attribute("mod_nterm_mass") != null) {
					double modMass = Double.parseDouble(modElement.attributeValue("mod_nterm_mass"));
					if (this.ptmMap.containsKey(modMass)) {
						PeptideModification mod = new PeptideModification(this.ptmMap.get(modMass), 0);
						mlist.add(mod);
					}
				}
				if (modElement.attribute("mod_cterm_mass") != null) {
					double modMass = Double.parseDouble(modElement.attributeValue("mod_cterm_mass"));
					if (this.ptmMap.containsKey(modMass)) {
						PeptideModification mod = new PeptideModification(this.ptmMap.get(modMass),
								sequence.length() + 1);
						mlist.add(mod);
					}
				}

				List<Element> modElements = modElement.elements("mod_aminoacid_mass");
				for (int i = 0; i < modElements.size(); i++) {
					Element ei = modElements.get(i);
					double modMass = Double.parseDouble(ei.attributeValue("mass"));
					int positions = Integer.parseInt(ei.attributeValue("position"));
					if (this.ptmMap.containsKey(modMass)) {
						PeptideModification mod = new PeptideModification(this.ptmMap.get(modMass), positions);
						mlist.add(mod);
					}
				}

				if (mlist.size() == 0) {
					psm = new MSFraggerPsm(scan, charge, precursorMr, rt, sequence, pepmass, massdiff, miss,
							num_tol_term, tot_num_ions, num_matched_ions, hit_rank, protein, hyperscore, nextscore,
							expect, isTarget);
				} else {
					PeptideModification[] pms = mlist.toArray(new PeptideModification[mlist.size()]);

					psm = new MSFraggerPsm(scan, charge, precursorMr, rt, sequence, pepmass, massdiff, miss,
							num_tol_term, tot_num_ions, num_matched_ions, hit_rank, protein, hyperscore, nextscore,
							expect, pms, isTarget);
				}

			} else {
				psm = new MSFraggerPsm(scan, charge, precursorMr, rt, sequence, pepmass, massdiff, miss, num_tol_term,
						tot_num_ions, num_matched_ions, hit_rank, protein, hyperscore, nextscore, expect, isTarget);
			}

			psm.setFileName(baseName);

			return psm;
		}
		return null;
	}
	
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
		for (int i = 0; i < pepxmlFiles.length; i++) {
			MSFraggerReader reader = new MSFraggerReader(pepxmlFiles[i]);
			MSFraggerPsm[] psms = reader.getPSMs();

			for (MSFraggerPsm psm : psms) {
				PeptideModification[] ptms = psm.getPTMs();
				if (ptms != null) {
					for (PeptideModification pmf : ptms) {
						LOGGER.error(psm.getFileName() + "\t" + psm.getScan());
						LOGGER.error(psm.getFileName() + "\t" + psm.getScan() + "\t" + psm.getSequence()+"\t"+pmf.getMass());
					}
				}
			}

			LOGGER.info("Reading pepXML file " + pepxmlFiles[i] + ", " + psms.length + " PSMs obtained");
		}

	}
	
	private static MSFraggerPsm[] batchRead(String[] pepxmls) throws IOException, DocumentException {
		MSFraggerPsm[] totalPSMs = null;
		for (int i = 0; i < pepxmls.length; i++) {
			if (pepxmls[i].endsWith("pepXML")) {
				MSFraggerReader reader = new MSFraggerReader(pepxmls[i]);
				MSFraggerPsm[] psms = reader.getPSMs();

				if (totalPSMs == null) {
					totalPSMs = new MSFraggerPsm[psms.length];
					System.arraycopy(psms, 0, totalPSMs, 0, psms.length);
				} else {
					MSFraggerPsm[] combinePSMs = new MSFraggerPsm[psms.length + totalPSMs.length];
					System.arraycopy(totalPSMs, 0, combinePSMs, 0, totalPSMs.length);
					System.arraycopy(psms, 0, combinePSMs, totalPSMs.length, psms.length);

					totalPSMs = combinePSMs;
				}

				LOGGER.info("Reading pepXML file " + pepxmls[i] + ", " + psms.length + " PSMs obtained");
			}
		}

		return totalPSMs;
	}
	
	private static MSFraggerPsm[] batchRead(File[] pepxmls) throws IOException, DocumentException {
		MSFraggerPsm[] totalPSMs = null;

		for (int i = 0; i < pepxmls.length; i++) {
			if (pepxmls[i].getName().endsWith("pepXML")) {
				MSFraggerReader reader = new MSFraggerReader(pepxmls[i]);
				MSFraggerPsm[] psms = reader.getPSMs();
				if (totalPSMs == null) {
					totalPSMs = new MSFraggerPsm[psms.length];
					System.arraycopy(psms, 0, totalPSMs, 0, psms.length);
				} else {
					MSFraggerPsm[] combinePSMs = new MSFraggerPsm[psms.length + totalPSMs.length];
					System.arraycopy(totalPSMs, 0, combinePSMs, 0, totalPSMs.length);
					System.arraycopy(psms, 0, combinePSMs, totalPSMs.length, psms.length);

					totalPSMs = combinePSMs;
				}

				LOGGER.info("Reading pepXML file " + pepxmls[i] + ", " + psms.length + " PSMs obtained");
			}
		}

		return totalPSMs;
	}

	public HashMap<Double, PostTransModification> getPTMMap() {
		return ptmMap;
	}

	public HashMap<Integer, MSFraggerPsm> getPsmMap() {
		HashMap<Integer, MSFraggerPsm> map = new HashMap<Integer, MSFraggerPsm>();
		while (hasNext()) {
			MSFraggerPsm psm = this.next();
			map.put(psm.getScan(), psm);
		}
		return map;
	}
	
	public MSFraggerPsm[] getPSMs() {
		if (this.psms == null) {
			ArrayList<MSFraggerPsm> list = new ArrayList<MSFraggerPsm>();
			while (hasNext()) {
				MSFraggerPsm psm = this.next();
				list.add(psm);
			}

			this.psms = list.toArray(new MSFraggerPsm[list.size()]);
		}
		return psms;
	}
	
	@SuppressWarnings("unchecked")
	public MSFraggerPsm[][] getPSMArrays() {

		ArrayList<MSFraggerPsm>[] list = new ArrayList[this.files.length];
		for (int i = 0; i < list.length; i++) {
			list[i] = new ArrayList<MSFraggerPsm>();
		}
		String basename = null;
		int fileid = -1;

		while (hasNext()) {
			MSFraggerPsm psm = this.next();
			if (psm.getFileName().equals(basename)) {
				list[fileid].add(psm);
			} else {
				basename = psm.getFileName();
				fileid++;
				list[fileid].add(psm);
			}
		}

		MSFraggerPsm[][] psmarrays = new MSFraggerPsm[this.files.length][];
		for (int i = 0; i < psmarrays.length; i++) {
			psmarrays[i] = list[i].toArray(new MSFraggerPsm[list[i].size()]);
		}

		return psmarrays;
	} 
	
	public MSFraggerPsm[] getPSMs(double fdr) throws IOException {

		MSFraggerPsm[] psms = getPSMs();
		int[][] count = new int[50][2];

		for (MSFraggerPsm psm : psms) {
			double score = psm.getHyperscore();
			boolean decoy = psm.getProtein().startsWith("REV");
			if (score > 50) {
				if (decoy) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				}
			} else {
				if (decoy) {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			}
		}

		int threshold = -1;
		for (int i = 0; i < count.length; i++) {
			if ((double) count[i][1] / (double) count[i][0] < fdr) {
				threshold = i;
				break;
			}
		}

		if (threshold == -1) {
			return null;
		}

		ArrayList<MSFraggerPsm> list = new ArrayList<MSFraggerPsm>();
		for (MSFraggerPsm psm : psms) {
			if (psm.getProtein().startsWith("REV")) {
				continue;
			}
			if (psm.getHyperscore() > threshold) {
				list.add(psm);
			}
		}
		MSFraggerPsm[] selectedPsms = list.toArray(new MSFraggerPsm[list.size()]);

		return selectedPsms;
	}
	
	public MSFraggerPsm[] getPSMs(double fdr, double expect) throws IOException {

		MSFraggerPsm[] psms = getPSMs();
		int[][] count = new int[50][2];

		for (MSFraggerPsm psm : psms) {

			if (psm.getExpect() > expect) {
				continue;
			}

			double score = psm.getHyperscore();
			boolean decoy = psm.getProtein().startsWith("REV");
			if (score > 50) {
				if (decoy) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				}
			} else {
				if (decoy) {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			}
		}

		int threshold = -1;
		for (int i = 0; i < count.length; i++) {
			if ((double) count[i][1] / (double) count[i][0] < fdr) {
				threshold = i;
				break;
			}
		}

		if (threshold == -1) {
			return null;
		}

		ArrayList<MSFraggerPsm> list = new ArrayList<MSFraggerPsm>();
		for (MSFraggerPsm psm : psms) {
			if (psm.getProtein().startsWith("REV")) {
				continue;
			}
			if (psm.getHyperscore() > threshold) {
				list.add(psm);
			}
		}
		MSFraggerPsm[] selectedPsms = list.toArray(new MSFraggerPsm[list.size()]);

		return selectedPsms;
	}
	
	public int[] getTargetMassDiffBin() throws IOException {

		if (targetMassDiffBin == null) {
			MSFraggerPsm[] psms = this.getPSMs();
			targetMassDiffBin = new int[binCount];

			for (int i = 0; i < psms.length; i++) {
				double massDiff = psms[i].getMassDiff();
				int id = (int) ((massDiff + openSearchWindow) / (openSearchWindow * 2) * binCount);
				if (id >= 0 && id < binCount) {
					if (psms[i].isTarget()) {
						targetMassDiffBin[id]++;
					}
				}
			}
		}

		return targetMassDiffBin;
	}
	
	public void getPossibleModList() throws IOException {
		int[][] positiveCounts = new int[500][500];
		int[][] negativeCounts = new int[500][500];
		int totalCount = 0;
		MSFraggerPsm psm = null;
		while (hasNext()) {
			psm = this.next();
			double massDiff = psm.getMassDiff();
			if (massDiff > 2) {
				int intMassDiff = (int) massDiff;
				int id1 = intMassDiff;
				int id2 = (int) ((massDiff - intMassDiff) * 500);
				positiveCounts[id1][id2]++;
			} else if (massDiff < -2) {
				int intMassDiff = (int) -massDiff;
				int id1 = intMassDiff;
				int id2 = (int) (-(massDiff + intMassDiff) * 500);
				negativeCounts[id1][id2]++;
			} else {
				continue;
			}

			totalCount++;
		}

		int pBeginI = -1;
		int pBeginJ = -1;
		boolean pContinuous = false;
		ArrayList<Integer> pCountList = new ArrayList<Integer>();
		for (int i = 0; i < positiveCounts.length; i++) {
			for (int j = 0; j < positiveCounts[i].length; j++) {

				if (positiveCounts[i][j] > 0) {
					if (pContinuous) {
						pCountList.add(positiveCounts[i][j]);
					} else {
						pContinuous = true;
						pBeginI = i;
						pBeginJ = j;
						pCountList = new ArrayList<Integer>();
						pCountList.add(positiveCounts[i][j]);
					}
				} else {
					if (pContinuous) {
						pContinuous = false;
						double totalMassK = 0;
						double totalCountK = 0;
						for (int k = 0; k < pCountList.size(); k++) {
							int countk = pCountList.get(k);
							double mass = pBeginI + (pBeginJ + k) / 500.0;
							totalMassK += mass * countk;
							totalCountK += countk;
						}

						if (totalCountK > 100)
							System.out.println(totalMassK / (double) totalCountK + "\t" + totalCountK + "\t"
									+ (totalCountK / totalCount));
					}
				}
			}
		}

		int nBeginI = -1;
		int nBeginJ = -1;
		boolean nContinuous = false;
		ArrayList<Integer> nCountList = new ArrayList<Integer>();
		for (int i = 0; i < negativeCounts.length; i++) {
			for (int j = 0; j < negativeCounts[i].length; j++) {

				if (positiveCounts[i][j] > 0) {
					if (nContinuous) {
						nCountList.add(negativeCounts[i][j]);
					} else {
						nContinuous = true;
						nBeginI = i;
						nBeginJ = j;
						nCountList = new ArrayList<Integer>();
						nCountList.add(negativeCounts[i][j]);
					}
				} else {
					if (nContinuous) {
						nContinuous = false;
						double totalMassK = 0;
						double totalCountK = 0;
						for (int k = 0; k < nCountList.size(); k++) {
							int countk = nCountList.get(k);
							double mass = nBeginI + (nBeginJ + k) / 500.0;
							totalMassK += mass * countk;
							totalCountK += countk;
						}
						if (totalCountK > 100)
							System.out.println(-(totalMassK / (double) totalCountK) + "\t" + totalCountK + "\t"
									+ (totalCountK / totalCount));
					}
				}
			}
		}
	}
	
	private static int[] massDiffTest(String in) throws IOException, DocumentException {
		int[] count = new int[1000];
		MSFraggerReader reader = new MSFraggerReader(in);
		while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			String sequence = psm.getSequence();
			double massDiff = psm.getMassDiff();
			double expect = psm.getExpect();
			if (expect < 0.01) {
				int diffId = (int) (massDiff + 500);
				if (diffId >= 0 && diffId < 1000) {
					count[diffId]++;
				}
			}
		}
		
		return count;
	}
	
	private static int[][] massDiffTest2(String in) throws IOException, DocumentException {
		int[][] count = new int[1000][500];
		MSFraggerReader reader = new MSFraggerReader(in);
		while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			double massDiff = psm.getMassDiff();
			double expect = psm.getExpect();
			if (expect < 0.01) {
				int diffId = (int) (massDiff + 500);
				if (diffId >= 0 && diffId < 1000) {
					double decimalPart = massDiff - (int) massDiff;
					if (decimalPart < 0)
						decimalPart = -decimalPart;
					int decimalId = (int) (decimalPart * 500);
					count[diffId][decimalId]++;
				}
			}
		}

		return count;
	}
	
	private static void batchTest(String in) throws IOException, DocumentException {
		HashMap<String, int[]> map = new HashMap<String, int[]>();
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			map.put(files[i].getName(), massDiffTest(files[i].getAbsolutePath()));
		}

		String[] names = map.keySet().toArray(new String[map.size()]);
		Arrays.sort(names);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			sb.append(names[i]).append("\t");
		}

		System.out.println(sb);

		for (int i = 0; i < 1000; i++) {
			if (i >= 497 && i <= 502) {
				continue;
			}
			sb = new StringBuilder((i - 500) + "\t");
			for (int j = 0; j < names.length; j++) {
				sb.append(map.get(names[j])[i]).append("\t");
			}
			System.out.println(sb);
		}
	}
	
	private static void batchTest2(String in) throws IOException, DocumentException {

		File[] pepxmls = (new File(in)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("pepXML"))
					return true;
				return false;
			}
		});

		MSFraggerPsm[] psms = MSFraggerReader.batchRead(pepxmls);

		int[] count = new int[2];
		for (int i = 0; i < psms.length; i++) {
			if (Math.abs(psms[i].getMassDiff()) < 0.2) {
				count[0]++;
			} else {
				count[1]++;
			}
		}
		System.out.println(count[0] + "\t" + count[1]);
	}
	
	private static void testModOnAA(String in, double mass, char aa) throws IOException, DocumentException {
		int[] counts = new int[2];
		MSFraggerReader reader = new MSFraggerReader(in);
		L: while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			String sequence = psm.getSequence();
			double massDiff = psm.getMassDiff();

			double expect = psm.getExpect();
			if (expect < 0.01) {
				if (Math.abs(mass - massDiff) < 1.0) {
					for (int i = 0; i < sequence.length(); i++) {
						if (sequence.charAt(i) == aa) {
							counts[0]++;
							continue L;
						}
					}
					counts[1]++;
				}
			}
		}

		System.out.println(counts[0] + "\t" + counts[1] + "\t" + (double) counts[0] / (double) (counts[0] + counts[1]));
	}
	
	private static void batchTestModOnAA(String in, double mass, char aa) throws IOException, DocumentException {

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			testModOnAA(files[i].getAbsolutePath(), mass, aa);
		}
	}
	
	private static void batchFdrTestWithDecoy(String in) throws IOException, DocumentException {
		File[] files = (new File(in)).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				if (arg1.endsWith("pepXML"))
					return true;
				return false;
			}

		});

		int[][] count = new int[200][2];
		int expcount = 0;
		MSFraggerReader reader = new MSFraggerReader(files);
		while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			String sequence = psm.getSequence();
			double massDiff = psm.getMassDiff();
			String protein = psm.getProtein();
//			double score = psm.getHyperscore() * 4.0;
			double score = -Math.log10(psm.getExpect()) * 20;
			if (protein.startsWith("REV")) {
				if (score > count.length) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				}
			} else {
				if (score > count.length) {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			}

			double expect = psm.getExpect();
			if (expect < 0.01) {
				expcount++;
			}
		}

		for (int i = 0; i < count.length; i++) {
			System.out.println(i + "\t" + count[i][0] + "\t" + count[i][1] + "\t"
					+ (double) count[i][1] / (double) count[i][0] + "\t" + expcount);
		}
	}
	
	private static void fdrTestWithDecoy(String in) throws IOException, DocumentException {
		HashSet<Integer> set = new HashSet<Integer>();
		int[][] count = new int[50][2];
		int expcount = 0;
		MSFraggerReader reader = new MSFraggerReader(in);
		while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			String sequence = psm.getSequence();
			double massDiff = psm.getMassDiff();
			String protein = psm.getProtein();
			double score = psm.getHyperscore();
			if (protein.startsWith("REV")) {
				if (score > 50) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				}
			} else {
				if (score > 50) {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			}
			set.add(psm.getScan());
			
			double expect = psm.getExpect();
			if (expect < 0.01) {
				expcount++;
			}
		}

		for (int i = 0; i < count.length; i++) {
			System.out.println(i + "\t" + count[i][0] + "\t" + count[i][1] + "\t"
					+ (double) count[i][1] / (double) count[i][0] + "\t" + expcount + "\t" + set.size());
		}
	}
	
	private static void fdrTestNoDecoy(String in) throws IOException, DocumentException {
		HashSet<Integer> set = new HashSet<Integer>();
		int[][] count = new int[50][2];
		int expcount = 0;
		MSFraggerReader reader = new MSFraggerReader(in);
		while (reader.hasNext()) {
			MSFraggerPsm psm = reader.next();
			String sequence = psm.getSequence();
			double massDiff = psm.getMassDiff();
			String protein = psm.getProtein();
			double score = psm.getHyperscore();
			double score2 = psm.getNextscore();
			
			if (score > 50) {
				for (int i = 0; i < count.length; i++) {
					count[i][0]++;
				}
			} else {
				for (int i = 0; i < score; i++) {
					count[i][0]++;
				}
			}
			

			if (score2 > 50) {
				for (int i = 0; i < count.length; i++) {
					count[i][1]++;
				}
			} else {
				for (int i = 0; i < score2; i++) {
					count[i][1]++;
				}
			}
		
			set.add(psm.getScan());
			
			double expect = psm.getExpect();
			if (expect < 0.01) {
				expcount++;
			}
		}

		for (int i = 0; i < count.length; i++) {
			System.out.println(i + "\t" + count[i][0] + "\t" + count[i][1] + "\t"
					+ (double) count[i][1] / (double) count[i][0] + "\t" + expcount + "\t" + set.size());
		}
	}
	
	private static void batchPsmCount(String dir) throws DocumentException, IOException {

		File[] pepxmls = (new File(dir)).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("pepXML"))
					return true;
				return false;
			}

		});
		MSFraggerPsm[] psms = MSFraggerReader.batchRead(pepxmls);

		int zero = 0;
		int[][] count = new int[50][2];
		int[][] count2 = new int[50][2];
		int[] massdCount = new int[2];
		for (MSFraggerPsm psm : psms) {

			double massdiff = psm.getMassDiff();
			if (Math.abs(massdiff) < 0.02) {
				massdCount[0]++;
			}
			if (Math.abs(massdiff) < 0.002) {
				massdCount[1]++;
			}
			if (massdiff > -0.0022 && massdiff < 0.0018) {
				zero++;
			}

			double score = psm.getHyperscore();
			if (psm.isTarget()) {
				if (score > 50) {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			} else {
				if (score > 50) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				}
			}

			double escore = -Math.log10(psm.getExpect())*10;
			if (psm.isTarget()) {
				if (escore > 50) {
					for (int i = 0; i < count2.length; i++) {
						count2[i][0]++;
					}
				} else {
					for (int i = 0; i < escore; i++) {
						count2[i][0]++;
					}
				}
			} else {
				if (escore > 50) {
					for (int i = 0; i < count2.length; i++) {
						count2[i][1]++;
					}
				} else {
					for (int i = 0; i < escore; i++) {
						count2[i][1]++;
					}
				}
			}
		}
		System.out.println("zero\t"+zero);
		System.out.println(massdCount[0]+"\t"+massdCount[1]);
		for (int i = 0; i < count.length; i++) {
			double fdr = (double) count[i][1] / (double) count[i][0];
			System.out.println(count[i][0] + "\t" + count[i][1] + "\t" + fdr);
		}
		System.out.println();
		
		for (int i = 0; i < count2.length; i++) {
			double fdr = (double) count2[i][1] / (double) count2[i][0];
			System.out.println(count2[i][0] + "\t" + count2[i][1] + "\t" + fdr);
		}
	}
	
	private static void batchPsmCountTD(String dir) throws DocumentException, IOException {

		int[] zeroCount = new int[2];
		int[] count = new int[2];

		File[] files = (new File(dir)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("pepXML")) {
				MSFraggerReader reader = new MSFraggerReader(files[i]);
				MSFraggerPsm[] psms = reader.getPSMs();

				for (MSFraggerPsm psm : psms) {
					double massdiff = psm.getMassDiff();
					if (psm.isTarget()) {
						count[0]++;
						if (Math.abs(massdiff) < 0.02) {
							zeroCount[0]++;
						}
					} else {
						count[1]++;
						if (Math.abs(massdiff) < 0.02) {
							zeroCount[1]++;
						}
					}
				}

			}
		}

		System.out.println(zeroCount[0] + "\t" + zeroCount[1]);
		System.out.println(count[0] + "\t" + count[1]);
	}
	
	/**
	 * X=False positive/Condition negative
	 * Y=True positive/Condition positive
	 * @param dir
	 * @throws IOException
	 * @throws DocumentException
	 */
	private static void ROCTestHyperscore(String dir) throws IOException, DocumentException {
		File[] pepxmls = (new File(dir)).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("pepXML"))
					return true;
				return false;
			}

		});

		int target = 0;
		int decoy = 0;
		int[][] counts = new int[50][2];
		MSFraggerPsm[] psms = MSFraggerReader.batchRead(pepxmls);
		for (MSFraggerPsm mp : psms) {
			if (mp.isTarget()) {
				target++;
			} else {
				decoy++;
			}
//			double score = mp.getHyperscore();
			double score = -Math.log(mp.getExpect()) * 10;
			int id = (int) score;
			if (id > 49) {
				for (int i = 0; i < counts.length; i++) {
					if (mp.isTarget()) {
						counts[i][0]++;
					} else {
						counts[i][1]++;
					}
				}
			} else {
				for (int i = 0; i <= id; i++) {
					if (mp.isTarget()) {
						counts[i][0]++;
					} else {
						counts[i][1]++;
					}
				}
			}
		}

		for (int i = 0; i < counts.length; i++) {
			double X = (double) counts[i][1] / (double) decoy;
			double Y = (double) counts[i][0] / (double) target;
			double fdr = (double) counts[i][1] / (double) counts[i][0];
			System.out.println(i + "\t" + counts[i][0] + "\t" + counts[i][1] + "\t" + fdr + "\t" + X + "\t" + Y);
		}
	}
	
	private static void getScore(String in, int scannum) {
		MSFraggerReader reader = new MSFraggerReader(in);
		HashMap<Integer, MSFraggerPsm> map = reader.getPsmMap();
		MSFraggerPsm psm = map.get(scannum);
		System.out.println(psm.getSequence()+"\t"+psm.getMassDiff()+"\t"+psm.getHyperscore()+"\t"+psm.getExpect());
	}

	public static int batchFastaWrite(String[] xmls, String fasta, String output, double evalue) {
		HashSet<String> totalSet = new HashSet<String>();
		for (int i = 0; i < xmls.length; i++) {
			MSFraggerReader reader = new MSFraggerReader(xmls[i]);
			MSFraggerPsm[] psms = reader.getPSMs();
			for (int j = 0; j < psms.length; j++) {
				double expect = psms[j].getExpect();
//				if (expect <= evalue) {
					totalSet.add(psms[j].getProtein());
//				}
			}
		}

		int count = 0;
		boolean write = false;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing protein database to " + output, e);
		}
		BufferedReader fastaReader = null;
		try {
			fastaReader = new BufferedReader(new FileReader(fasta));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading protein sequence from " + fasta, e);
		}
		String line = null;
		try {
			while ((line = fastaReader.readLine()) != null) {
				if (line.startsWith(">")) {
					String ref = line.substring(1);
					if (totalSet.contains(ref)) {
						write = true;
						writer.println(line);
						count++;
					} else {
						write = false;
					}
				} else {
					if (write) {
						writer.println(line);
					}
				}
			}
			fastaReader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing protein database to " + output, e);
		}

		return count;
	}
}
