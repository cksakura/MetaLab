package bmi.med.uOttawa.metalab.dbSearch.msFragger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.dbSearch.tpp.IProProtReader;
import bmi.med.uOttawa.metalab.dbSearch.tpp.PepProphetReader;
import bmi.med.uOttawa.metalab.dbSearch.tpp.PeptideProphehPSM;

public class InteractPepxmlReader {



	private Iterator<Element> it;
	private Element query;

	public InteractPepxmlReader(String pepxml) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(pepxml);
		Element msms_run_summary = document.getRootElement().element("msms_run_summary");
		this.it = msms_run_summary.elementIterator("spectrum_query");
	}
	
	public InteractPepxmlReader(File pepxml) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(pepxml);
		Element msms_run_summary = document.getRootElement().element("msms_run_summary");
		this.it = msms_run_summary.elementIterator("spectrum_query");
	}

	public boolean hasNext() throws IOException {

		if (it.hasNext()) {
			this.query = it.next();
			return true;

		} else {
			return false;
		}
	}

	public PeptideProphehPSM next() {

		int start_scan = Integer.parseInt(query.attributeValue("start_scan"));
		int assumed_charge = Integer.parseInt(query.attributeValue("assumed_charge"));
		double precursor_neutral_mass = Double.parseDouble(query.attributeValue("precursor_neutral_mass"));
		double retention_time_sec = Double.parseDouble(query.attributeValue("retention_time_sec"));

		Element result = query.element("search_result");
		Element hit = result.element("search_hit");

		String peptide = hit.attributeValue("peptide");
		double massdiff = Double.parseDouble(hit.attributeValue("massdiff"));
		int num_missed_cleavages = 0;
		String miss_string = hit.attributeValue("num_missed_cleavages");
		if (miss_string != null) {
			num_missed_cleavages = Integer.parseInt(miss_string);
		}
		String protein = hit.attributeValue("protein");

		double xcorr = 0;
		double hyperscore = 0;
		double nextscore = 0;
		double expect = 0;
		double qvalue = 0;

		Iterator<Element> scoreit = hit.elementIterator("search_score");
		while (scoreit.hasNext()) {
			Element escore = scoreit.next();
			String name = escore.attributeValue("name");
			double score = Double.parseDouble(escore.attributeValue("value"));
			if (name.equals("hyperscore")) {
				hyperscore = score;
			} else if (name.equals("nextscore")) {
				nextscore = score;
			} else if (name.equals("expect")) {
				expect = score;
			} else if (name.equals("XCorr")) {
				xcorr = score;
			} else if (name.equals("Percolator q-Value")) {
				qvalue = score;
			}
		}

		double probability = 0;

		Element analysis_result = hit.element("analysis_result");
		if (analysis_result != null) {
			Iterator<Element> it = analysis_result.elementIterator();
			if (it.hasNext()) {
				Element e_method = it.next();
				String pro_string = e_method.attributeValue("probability");
				if (pro_string != null) {
					probability = Double.parseDouble(pro_string);
				}
			}
		}

		PeptideProphehPSM pppsm = new PeptideProphehPSM(start_scan, assumed_charge, precursor_neutral_mass,
				retention_time_sec, peptide, massdiff, num_missed_cleavages, protein, hyperscore, nextscore, expect,
				probability);
		pppsm.setQvalue(qvalue);

		return pppsm;
	}

	public PeptideProphehPSM[] getAllPSMs() throws IOException {
		ArrayList<PeptideProphehPSM> list = new ArrayList<PeptideProphehPSM>();
		while (hasNext()) {
			PeptideProphehPSM psm = next();
			list.add(psm);
		}
		PeptideProphehPSM[] psms = list.toArray(new PeptideProphehPSM[list.size()]);
		return psms;
	}
	
	public PeptideProphehPSM[] getPSMs(double probThres) throws IOException {
		ArrayList<PeptideProphehPSM> list = new ArrayList<PeptideProphehPSM>();
		while (hasNext()) {
			PeptideProphehPSM psm = next();
			if (psm.getProbability() > probThres) {
				list.add(psm);
			}
		}
		PeptideProphehPSM[] psms = list.toArray(new PeptideProphehPSM[list.size()]);
		return psms;
	}
	
	public PeptideProphehPSM[] getPSMs(double probThres, String ipro) throws IOException, DocumentException {
		
		IProProtReader ppr = new IProProtReader(ipro);
		HashSet<String> pepset = ppr.getAllPeptides(0.9);
		
		ArrayList<PeptideProphehPSM> list = new ArrayList<PeptideProphehPSM>();
		while (hasNext()) {
			PeptideProphehPSM psm = next();
			if (pepset.contains(psm.getPeptide()) && psm.getProbability() > probThres) {
				list.add(psm);
			}
		}
		PeptideProphehPSM[] psms = list.toArray(new PeptideProphehPSM[list.size()]);
		return psms;
	}

	private static void test(String pepxml, String proxml) throws DocumentException, IOException {
		PepProphetReader reader = new PepProphetReader(pepxml);
		PeptideProphehPSM[] psms = reader.getPSMs(0.01, proxml);
		Arrays.sort(psms, new PeptideProphehPSM.InverseProbabilityComparator());

		int target = 0;
		int decoy = 0;
		for (int i = 0; i < psms.length; i++) {
			if (psms[i].isTarget()) {
				target++;
			} else {
				decoy++;
			}
		}

		double fdr = (double) decoy / (double) target;
		System.out.println(target + "\t" + decoy + "\t" + fdr);
	}
	
	private static void batchRead(String in) throws DocumentException, IOException {
		int total = 0;
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("xml") && files[i].getName().startsWith("Run1")) {
				PepProphetReader reader = new PepProphetReader(files[i]);
				PeptideProphehPSM psm = null;
				int count = 0;
				while (reader.hasNext()) {
					psm = reader.next();
					double qvalue = psm.getQvalue();
					if (qvalue < 0.01) {
						count++;
					}
				}
				System.out.println(files[i] + "\t" + count);
				total += count;
			}
		}
		System.out.println("total\t" + total);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
