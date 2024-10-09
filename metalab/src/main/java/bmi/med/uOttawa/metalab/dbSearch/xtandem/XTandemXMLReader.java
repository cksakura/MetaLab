/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Kai Cheng
 *
 */
public class XTandemXMLReader {

	private Element root;
	private int[] totalPSM = new int[2];
	private int[] evaluePSM = new int[2];
	private int[][] scoreArrays = new int[100][2];
	private int scoreThres;

	public XTandemXMLReader(String file) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		this.root = document.getRootElement();
	}

	public XTandemXMLReader(File file) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		this.root = document.getRootElement();
	}

	public HashMap<String, String> filter(double fdr, double evalue) {
		Iterator<Element> it = this.root.elementIterator();
		HashMap<String, String> idmap = new HashMap<String, String>();
		HashMap<String, Double> scoremap = new HashMap<String, Double>();

		L: while (it.hasNext()) {
			Element group = it.next();
			String id = group.attributeValue("id");
			String label = group.attributeValue("label");
			// boolean decoy = label.startsWith("REV");
			boolean decoy = label.contains("_REVERSED");

			if (decoy) {
				totalPSM[1]++;
			} else {
				totalPSM[0]++;
			}

			Iterator<Element> pit = group.elementIterator("protein");
			if (pit.hasNext()) {
				Element protein = pit.next();
				Iterator<Element> ppit = protein.elementIterator("peptide");
				if (ppit.hasNext()) {
					Element peptide = ppit.next();
					Iterator<Element> dit = peptide.elementIterator("domain");
					if (dit.hasNext()) {
						Element domain = dit.next();
						String sequence = domain.attributeValue("seq");
						double expect = Double.parseDouble(domain.attributeValue("expect"));
						double hyperscore = Double.parseDouble(domain.attributeValue("hyperscore"));

						if (expect <= evalue) {

							if (decoy) {
								evaluePSM[1]++;
							} else {
								evaluePSM[0]++;
							}

							int hyperId = hyperscore > 99 ? 99 : (int) hyperscore;

							if (decoy) {
								for (int i = 0; i <= hyperId; i++) {
									scoreArrays[i][1]++;
								}
							} else {
								for (int i = 0; i <= hyperId; i++) {
									scoreArrays[i][0]++;
								}
							}

							if (idmap.containsKey(sequence)) {
								if (hyperscore > scoremap.get(sequence)) {
									idmap.put(sequence, id);
									scoremap.put(sequence, hyperscore);
								}
							} else {
								idmap.put(sequence, id);
								scoremap.put(sequence, hyperscore);
							}

						} else {
							continue L;
						}
					}
				}
			}
		}

		scoreThres = -1;
		for (int i = 0; i < scoreArrays.length; i++) {
			double calFDR = (double) scoreArrays[i][0] == 0 ? 1.0
					: (double) scoreArrays[i][1] / (double) scoreArrays[i][0];
			if (calFDR <= fdr) {
				scoreThres = i;
				break;
			}
		}

		if (scoreThres > -1) {
			Iterator<String> pepit = scoremap.keySet().iterator();
			while (pepit.hasNext()) {
				String sequence = pepit.next();
				double score = scoremap.get(sequence);
				if (score < scoreThres) {
					pepit.remove();
					idmap.remove(sequence);
				}
			}
		}

		return idmap;
	}

	private static void test(String in) throws DocumentException {
		XTandemXMLReader reader = new XTandemXMLReader(in);
		HashMap<String, String> map = reader.filter(0.01, 0.05);
		StringBuilder sb = new StringBuilder();
		sb.append(reader.totalPSM[0]).append("\t").append(reader.totalPSM[1]).append("\n");
		sb.append(reader.evaluePSM[0]).append("\t").append(reader.evaluePSM[1]).append("\n");
		int[][] scoreArrays = reader.scoreArrays;
		for (int i = 0; i < scoreArrays.length; i++) {
			sb.append(i + "\t" + scoreArrays[i][0]).append("\t").append(scoreArrays[i][1]).append("\n");
		}
		sb.append(map.size() + "\t" + reader.scoreThres);
		System.out.println(sb);
	}
}
