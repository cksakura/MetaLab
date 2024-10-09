/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.tpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Kai Cheng
 *
 */
public class PepProphetReader {

	private Iterator<Element> it;
	private Element query;
	private PeptideProphehPSM[] psms;

	public PepProphetReader(String pepxml) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(pepxml);
		
		parse(document);
	}
	
	public PepProphetReader(File pepxml) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(pepxml);
		
		parse(document);
	}
	
	private void parse(Document document) {

		ArrayList<PeptideProphehPSM> psmlist = new ArrayList<PeptideProphehPSM>();
		Element rootElement = document.getRootElement();
		Iterator<Element> summaryIt = rootElement.elementIterator("msms_run_summary");
		while (summaryIt.hasNext()) {
			Element summaryElement = summaryIt.next();
			Iterator<Element> queryIt = summaryElement.elementIterator("spectrum_query");
			while (queryIt.hasNext()) {
				this.query = queryIt.next();
				PeptideProphehPSM psm = next();
				psmlist.add(psm);
			}
		}
		this.psms = psmlist.toArray(PeptideProphehPSM[]::new);
	}

	public PeptideProphehPSM next() {

		String spectrum = query.attributeValue("spectrum");
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

		PeptideProphehPSM pppsm = new PeptideProphehPSM(spectrum, start_scan, assumed_charge, precursor_neutral_mass,
				retention_time_sec, peptide, massdiff, num_missed_cleavages, protein, hyperscore, nextscore, expect,
				probability);
		pppsm.setQvalue(qvalue);

		return pppsm;
	}

	public PeptideProphehPSM[] getAllPSMs() throws IOException {
		return this.psms;
	}
	
	public static void main(String[] args) {
		
		
		try {
			HashMap<String, HashSet<String>> spectrumMap = new HashMap<String, HashSet<String>>();
			PepProphetReader reader = new PepProphetReader("Z:\\Kai\\Raw_files\\PXD008738\\paper_data\\12mix.peps.xml");
			PeptideProphehPSM[] psms = reader.getAllPSMs();
			int[] count = new int[4];

			for (int i = 0; i < psms.length; i++) {
				

				count[0]++;
				if(psms[i].getProtein().startsWith("DECOY_")) {
					count[1]++;
				}
//				if (psms[i].getProbability() > 0.99) {
				if (psms[i].getExpect() < 0.01) {
					count[2]++;
					if (psms[i].getProtein().startsWith("DECOY_")) {
						count[3]++;
						continue;
					}

					String spectrum = psms[i].getSpectrum();
					String fileString = spectrum.substring(0, spectrum.indexOf("."));
					if (spectrumMap.containsKey(fileString)) {
						spectrumMap.get(fileString).add(psms[i].getPeptide());
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(psms[i].getPeptide());
						spectrumMap.put(fileString, set);
					}

				}
			}
			System.out.println(psms.length + "\t" + count[0] + "\t" + count[1] + "\t" + count[2] + "\t" + count[3]);
			HashSet<String> totalSet = new HashSet<String>();
			for (String file : spectrumMap.keySet()) {
				System.out.println(file + "\t" + spectrumMap.get(file).size());
				totalSet.addAll( spectrumMap.get(file));
			}
			System.out.println("total\t"+totalSet.size());

		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		PeptideProphehPSM[] psms = reader.getAllPSMs();
//		System.out.println(psms.length);
//		int count = 0;
//		while(reader.hasNext()) {
//			PeptideProphehPSM psm = reader.next();
//			count++;
//		}
//		System.out.println(count);

	}
}
