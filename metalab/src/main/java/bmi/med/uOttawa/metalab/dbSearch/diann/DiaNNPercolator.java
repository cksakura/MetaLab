package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.ml.SemiSuperClassifier;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @deprecated
 * @author Kai Cheng
 *
 */
public class DiaNNPercolator {

	private int rawFileCound;
	private ArrayList<Attribute> attributeList;
	private int scoreAttriId;

	public DiaNNPercolator(int rawFileCound) {
		this.rawFileCound = rawFileCound;
		this.createAttributeList();
	}

	private void createAttributeList() {

		attributeList = new ArrayList<Attribute>();
		attributeList.add(new Attribute("PG.Quantity"));
		attributeList.add(new Attribute("PG.Normalised"));
		attributeList.add(new Attribute("PG.MaxLFQ"));

		attributeList.add(new Attribute("Genes.Quantity"));
		attributeList.add(new Attribute("Genes.Normalised"));
		attributeList.add(new Attribute("Genes.MaxLFQ"));
		attributeList.add(new Attribute("Genes.MaxLFQ.Unique"));

		attributeList.add(new Attribute("Unimod"));
		attributeList.add(new Attribute("Length"));
		attributeList.add(new Attribute("Precursor.Charge"));
		attributeList.add(new Attribute("Q.Value"));
		attributeList.add(new Attribute("PEP"));

		attributeList.add(new Attribute("Global.Q.Value"));
		attributeList.add(new Attribute("PG.Q.Value"));
		attributeList.add(new Attribute("Global.PG.Q.Value"));

		attributeList.add(new Attribute("Precursor.Quantity"));
		attributeList.add(new Attribute("Precursor.Normalised"));
		attributeList.add(new Attribute("Precursor.Translated"));
		attributeList.add(new Attribute("Ms1.Translated"));
		attributeList.add(new Attribute("Quantity.Quality"));

		attributeList.add(new Attribute("RT"));
		attributeList.add(new Attribute("RT.Start"));
		attributeList.add(new Attribute("RT.Stop"));
		attributeList.add(new Attribute("RT.Width"));
		attributeList.add(new Attribute("iRT"));
		attributeList.add(new Attribute("Predicted.RT"));
		attributeList.add(new Attribute("Predicted.iRT"));
		attributeList.add(new Attribute("Predicted.RT.Diff"));
		attributeList.add(new Attribute("Predicted.iRT.Diff"));

		attributeList.add(new Attribute("Ms1.Profile.Corr"));
		attributeList.add(new Attribute("Ms1.Area"));
		attributeList.add(new Attribute("Evidence"));
		attributeList.add(new Attribute("Spectrum.Similarity"));
		attributeList.add(new Attribute("Mass.Evidence"));
		attributeList.add(new Attribute("CScore"));
		attributeList.add(new Attribute("Decoy.CScore"));
		attributeList.add(new Attribute("Decoy.Evidence"));

		attributeList.add(new Attribute("Fragment.Quant.Raw"));
		attributeList.add(new Attribute("Fragment.Quant.Corrected"));
		attributeList.add(new Attribute("Fragment.Correlations"));

		ArrayList<String> classList = new ArrayList<String>();
		classList.add("P");
		classList.add("N");
		Attribute classAattr = new Attribute("class", classList);
		attributeList.add(classAattr);

		for (int i = 0; i < attributeList.size(); i++) {
			if (attributeList.get(i).name().equals("CScore")) {
				this.scoreAttriId = i;
			}
		}
	}

	public void calculate(String file, String cut) {
		try {
			calculate(new File(file), cut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DiaNNPrecursor[] calculate(File file, String cut) throws IOException {

		Instances instances = new Instances("Precursor Instances", attributeList, 50000 * rawFileCound);
		ArrayList<Double> scorelist = new ArrayList<Double>(50000 * rawFileCound);
		ArrayList<Double> decoyList = new ArrayList<Double>();

		File ppFile = new File(file, file.getName() + ".tsv");
		BufferedReader reader;
		try {

			reader = new BufferedReader(new FileReader(ppFile));
			String line = reader.readLine();
			String[] title = line.split("\t");
			int targetid = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Decoy.Evidence")) {
					targetid = i;
				}
			}

			HashMap<Integer, Integer> idMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < attributeList.size(); i++) {
				String name = attributeList.get(i).name();
				for (int j = 0; j < title.length; j++) {
					if (title[j].equals(name)) {
						idMap.put(j, i);
					}
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				boolean isTarget = cs[targetid].equals("0");
				decoyList.add(Double.parseDouble(cs[targetid]));

				Instance instance = new DenseInstance(attributeList.size());
				for (int i = 0; i < cs.length; i++) {
					if (idMap.containsKey(i)) {
						int attrId = idMap.get(i);
						Attribute attr = attributeList.get(attrId);

						if (title[i].startsWith("Fragment")) {
							String[] fragment = cs[i].split(";");
							double totalFragment = 0;
							for (int j = 0; j < fragment.length; j++) {
								totalFragment += Double.parseDouble(fragment[j]);
							}
							instance.setValue(attr, totalFragment / (double) fragment.length);
						} else {
							instance.setValue(attr, cs[i].length() > 0 ? Double.parseDouble(cs[i]) : 0.0);
						}

						if (attrId == this.scoreAttriId) {
							scorelist.add(Double.parseDouble(cs[i]));
						}

					} else {
						if (cs[i].startsWith("Modified.Sequence")) {
							Attribute attr = attributeList.get(7);
							Pattern pattern = Pattern.compile("UniMod:(d+)");
							Matcher matcher = pattern.matcher(cs[i]);
							if (matcher.matches()) {
								instance.setValue(attr, Double.parseDouble(matcher.group(1)));
							} else {
								instance.setValue(attr, 0);
							}

						} else if (cs[i].startsWith("Stripped.Sequence")) {
							Attribute attr = attributeList.get(8);
							instance.setValue(attr, cs[i].length());
						}
					}
				}

				if (isTarget) {
					instance.setValue(attributeList.get(attributeList.size() - 1), "P");
				} else {
					instance.setValue(attributeList.get(attributeList.size() - 1), "N");
				}
				instances.add(instance);
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		instances.setClassIndex(instances.numAttributes() - 1);

		Collections.sort(scorelist);
		int thresId = (int) (scorelist.size() * 0.75);
		double threshold = scorelist.get(thresId);

		SemiSuperClassifier classifier = new SemiSuperClassifier(attributeList);
		double[] scores = classifier.classify(instances, scoreAttriId, threshold);

		DiaNNResultReader precursorReader = new DiaNNResultReader(file, file.getName(), cut);

		if (!precursorReader.hasResult()) {
			return null;
		}

		DiaNNPrecursor[] pps = precursorReader.getDiaNNPrecursors();

		if (pps.length == scores.length) {
			for (int i = 0; i < pps.length; i++) {
				pps[i].setPercolatorScore(scores[i]);
			}
		}

		Arrays.sort(pps, new Comparator<DiaNNPrecursor>() {

			@Override
			public int compare(DiaNNPrecursor o1, DiaNNPrecursor o2) {
				// TODO Auto-generated method stub

				if (o1.getPercolatorScore() < o2.getPercolatorScore()) {
					return -1;
				} else if (o1.getPercolatorScore() > o2.getPercolatorScore()) {
					return 1;
				}

				return 0;
			}
		});

		PrintWriter perWriter = new PrintWriter(new File(file, file.getName() + ".percolator.tsv"));
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Run").append("\t");
		titlesb.append("Protein.Group").append("\t");
		titlesb.append("Modified.Sequence").append("\t");
		titlesb.append("Stripped.Sequence").append("\t");
		titlesb.append("Precursor.Charge").append("\t");
		titlesb.append("Miss cleavages").append("\t");
		titlesb.append("Mass").append("\t");
		titlesb.append("Q.Value").append("\t");
		titlesb.append("PEP").append("\t");
		titlesb.append("Protein.Q.Value").append("\t");
		titlesb.append("Precursor.Translated").append("\t");
		titlesb.append("CScore").append("\t");
		titlesb.append("Decoy.Evidence").append("\t");
		titlesb.append("Percolator score").append("\t");
		titlesb.append("Percolator local FDR").append("\t");
		perWriter.println(titlesb);

		int[] tdCount = new int[2];
		for (int i = 0; i < pps.length; i++) {

			StringBuilder sb = new StringBuilder();
			sb.append(pps[i].getRunString()).append("\t");
			String[] pros = pps[i].getProGroups();
			for (int j = 0; j < pros.length; j++) {
				sb.append(pros[j]).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\t");
			sb.append(pps[i].getModSeqString()).append("\t");
			sb.append(pps[i].getSeqString()).append("\t");
			sb.append(pps[i].getCharge()).append("\t");
			sb.append(pps[i].getMissCleavage()).append("\t");
			sb.append(pps[i].getMass()).append("\t");
			sb.append(pps[i].getQvalue()).append("\t");
			sb.append(pps[i].getPEP()).append("\t");
			sb.append(pps[i].getProGroupQvalue()).append("\t");
			sb.append(pps[i].getPreTrans()).append("\t");
			sb.append(pps[i].getCscore()).append("\t");
			sb.append(pps[i].getDecoyEvi()).append("\t");
			sb.append(pps[i].getPercolatorScore()).append("\t");

			if (pps[i].getDecoyEvi() == 0) {
				tdCount[0]++;
			} else {
				tdCount[1]++;
			}
			double fdr = (double) tdCount[1] / (double) tdCount[0];
			pps[i].setPercolatorFdr(fdr);
			sb.append(fdr);
			perWriter.println(sb);
		}
		perWriter.close();

		return pps;
	}
	
	@SuppressWarnings("unused")
	private static void test(String fasta, String perTsv) throws IOException {
		HashSet<String> pepSet = new HashSet<String>();
		FastaReader fr = new FastaReader(fasta);
		ProteinItem[] pis = fr.getItems();
		for (int j = 0; j < pis.length; j++) {
			String seq = pis[j].getSequence();
			String[] peps = Enzyme.TrypsinP.digest(seq, 3);
			for (int k = 0; k < peps.length; k++) {
				String ilseq = peps[k].replaceAll("L", "I");
				pepSet.add(ilseq);
				pepSet.add(ilseq.substring(1));
				pepSet.add(ilseq.substring(2));
				pepSet.add(ilseq.substring(3));
			}
		}

		int[] tdPer = new int[2];
		int[] tdNoPer = new int[2];
		BufferedReader reader = new BufferedReader(new FileReader(perTsv));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			double per = Double.parseDouble(cs[cs.length - 2]);
			String seq = cs[3].replaceAll("L", "I");
			if (per < 0.001) {
				if (pepSet.contains(seq)) {
					tdPer[0]++;
				} else {
					tdPer[1]++;
				}

			} else {
				if (pepSet.contains(seq)) {
					tdNoPer[0]++;
				} else {
					tdNoPer[1]++;
				}
			}
		}
		reader.close();
		System.out.println(tdPer[0] + "\t" + tdPer[1] + "\t" + (double) tdPer[1] / (double) tdPer[0]);
		System.out.println(tdNoPer[0] + "\t" + tdNoPer[1] + "\t" + (double) tdNoPer[1] / (double) tdNoPer[0]);
	}
}
