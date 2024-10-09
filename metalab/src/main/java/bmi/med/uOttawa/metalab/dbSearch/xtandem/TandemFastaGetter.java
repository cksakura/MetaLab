/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Kai Cheng
 *
 */
public class TandemFastaGetter {

	private Element root;

	private static Logger LOGGER = LogManager.getLogger();

	public TandemFastaGetter(String xml) {
		this(new File(xml));
	}

	public TandemFastaGetter(File xml) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem result file " + xml, e);
		}
		this.root = document.getRootElement();
	}

	@SuppressWarnings("unchecked")
	public int[] getPSMCount(double evalue) {
		int[] counts = new int[2];
		Iterator<Element> it = this.root.elementIterator();
		while (it.hasNext()) {
			Element group = it.next();
			String type = group.attributeValue("type");
			if (type.equals("model")) {
				counts[0]++;
				double expect = Double.parseDouble(group.attributeValue("expect"));
				if (expect <= evalue) {
					counts[1]++;
				}
			}
		}

		return counts;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> filterEValue(double evalue) {

		Iterator<Element> it = this.root.elementIterator();
		HashMap<String, String> idmap = new HashMap<String, String>();
		HashMap<String, Double> scoremap = new HashMap<String, Double>();

		L: while (it.hasNext()) {
			Element group = it.next();
			String id = group.attributeValue("id");

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

		return idmap;
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> filterFDR(double fdr) {
		Iterator<Element> it = this.root.elementIterator();
		HashMap<String, String> idmap = new HashMap<String, String>();
		HashMap<String, Double> scoremap = new HashMap<String, Double>();
		int[][] scoreArrays = new int[50][2];
		while (it.hasNext()) {
			Element group = it.next();
			String id = group.attributeValue("id");
			String label = group.attributeValue("label");
			boolean decoy = false;
			if (label.startsWith("REV")) {
				decoy = true;
			}
			if (label.contains("_REVERSED")) {
				decoy = true;
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
						double hyperscore = Double.parseDouble(domain.attributeValue("hyperscore"));

						int hyperId = hyperscore > 49 ? 49 : (int) hyperscore;
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
					}
				}
			}
		}

		int scoreThres = -1;
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

	@SuppressWarnings("unchecked")
	public HashMap<String, String> filter(double fdr, double evalue) {
		Iterator<Element> it = this.root.elementIterator();
		HashMap<String, String> idmap = new HashMap<String, String>();
		HashMap<String, Double> scoremap = new HashMap<String, Double>();
		int[][] scoreArrays = new int[50][2];
		L: while (it.hasNext()) {
			Element group = it.next();
			String id = group.attributeValue("id");
			String label = group.attributeValue("label");
			boolean decoy = false;
			if (label.startsWith("REV")) {
				decoy = true;
			}
			if (label.contains("_REVERSED")) {
				decoy = true;
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

						if (evalue == 0 || expect <= evalue) {
							int hyperId = hyperscore > 49 ? 49 : (int) hyperscore;
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

		int scoreThres = -1;
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

	@SuppressWarnings("unchecked")
	public HashSet<String> parseProtein(double fdr, double evalue) {
		HashSet<String> refset = new HashSet<String>();
		Iterator<Element> it = root.elementIterator();
		if (fdr == 0) {
			if (evalue == 0) {
				while (it.hasNext()) {
					Element proteinGroup = it.next();
					String id = proteinGroup.attributeValue("id");
					if (id != null) {
						Iterator<Element> pit = proteinGroup.elementIterator("protein");
						while (pit.hasNext()) {
							Element protein = pit.next();
							String ref = protein.attributeValue("label");
							ref = ref.split("[\\s]")[0];
							refset.add(ref);
						}
					}
				}
			} else {
				HashMap<String, String> filterMap = this.filterEValue(evalue);
				while (it.hasNext()) {
					Element proteinGroup = it.next();
					String id = proteinGroup.attributeValue("id");
					if (id != null) {
						if (filterMap.containsValue(id)) {
							Iterator<Element> pit = proteinGroup.elementIterator("protein");
							while (pit.hasNext()) {
								Element protein = pit.next();
								String ref = protein.attributeValue("label");
								ref = ref.split("[\\s]")[0];
								refset.add(ref);
							}
						}
					}
				}
			}

		} else {
			HashMap<String, String> filterMap = this.filter(fdr, evalue);
			while (it.hasNext()) {
				Element proteinGroup = it.next();
				String id = proteinGroup.attributeValue("id");
				if (id != null) {
					if (filterMap.containsValue(id)) {
						Iterator<Element> pit = proteinGroup.elementIterator("protein");
						while (pit.hasNext()) {
							Element protein = pit.next();
							String ref = protein.attributeValue("label");
							ref = ref.split("[\\s]")[0];
							refset.add(ref);
						}
					}
				}
			}
		}
		return refset;
	}

	public static int write(HashSet<String> set, String fasta, String output) {
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
					ref = ref.split("[\\s]")[0];

					if (set.contains(ref)) {
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

	public static int writeWithDecoy(HashSet<String> set, String fasta, String output) {
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
		StringBuilder sb = null;
		String revRef = null;
		try {
			while ((line = fastaReader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (sb != null) {
						writer.println(revRef);
						StringBuilder revsb = sb.reverse();
						revsb.deleteCharAt(0);
						writer.println(revsb);

						sb = null;
						revRef = null;
					}
					String ref = line.substring(1);
					ref = ref.split("[\\s]")[0];

					if (set.contains(ref)) {
						write = true;
						writer.println(line);
						count++;
						sb = new StringBuilder();
						revRef = ">REV_" + ref;
					} else {
						write = false;
					}
				} else {
					if (write) {
						writer.println(line);
						sb.append(line).append("\n");
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

	public static int batchWrite(String[] xmls, String fasta, String output, double fdr, double evalue) {
		HashSet<String> totalSet = new HashSet<String>();
		for (int i = 0; i < xmls.length; i++) {
			TandemFastaGetter getter = new TandemFastaGetter(xmls[i]);
			HashSet<String> refset = getter.parseProtein(fdr, evalue);
			totalSet.addAll(refset);
		}
		System.out.println(totalSet.size());
		return TandemFastaGetter.write(totalSet, fasta, output);
	}

	public static int batchWrite(File[] xmls, String fasta, String output, double fdr, double evalue) {
		HashSet<String> totalSet = new HashSet<String>();
		for (int i = 0; i < xmls.length; i++) {
			TandemFastaGetter getter = new TandemFastaGetter(xmls[i]);
			HashSet<String> refset = getter.parseProtein(fdr, evalue);
			totalSet.addAll(refset);
		}
		return TandemFastaGetter.write(totalSet, fasta, output);
	}

	public static int batchWriteWithDecoy(String[] xmls, String fasta, String output, double fdr, double evalue) {
		HashSet<String> totalSet = new HashSet<String>();
		for (int i = 0; i < xmls.length; i++) {
			TandemFastaGetter getter = new TandemFastaGetter(xmls[i]);
			HashSet<String> refset = getter.parseProtein(fdr, evalue);
			totalSet.addAll(refset);
		}
		return TandemFastaGetter.writeWithDecoy(totalSet, fasta, output);
	}

}
