package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoMatchClassifier;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoMicroNovoStat;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.NovoGlycoCompMatch;
import bmi.med.uOttawa.metalab.quant.Features;

public class GlycoMicroNovoReader {

	private BufferedReader reader;
	private Glycosyl[] glycosyls;
	private NovoGlycoCompMatch[] determinedMatches;
	private NovoGlycoCompMatch[] undeterminedMatches;
	private Features[] features;
	private HashMap<Integer, Integer> feasIdMap;
	private GlycoMicroNovoStat stat;

	public GlycoMicroNovoReader(String in) throws IOException {
		this(new File(in));
	}

	public GlycoMicroNovoReader(File in) throws IOException {
		this.reader = new BufferedReader(new FileReader(in));
	}

	public void read() throws IOException {

		String line = reader.readLine();
		String[] cs = line.split(";");

		this.glycosyls = new Glycosyl[Integer.parseInt(cs[0])];
		this.determinedMatches = new NovoGlycoCompMatch[Integer.parseInt(cs[1])];
		this.undeterminedMatches = new NovoGlycoCompMatch[Integer.parseInt(cs[2])];
		this.features = new Features[Integer.parseInt(cs[3])];
		this.feasIdMap = new HashMap<Integer, Integer>();
		this.stat = new GlycoMicroNovoStat(glycosyls);

		int glyId = 0;
		while ((line = reader.readLine()) != null) {
			this.glycosyls[glyId++] = Glycosyl.parse(line);
			if (glyId == this.glycosyls.length) {
				break;
			}
		}

		int matchId = 0;
		while ((line = reader.readLine()) != null) {
			this.determinedMatches[matchId] = NovoGlycoCompMatch.parse(line);
			this.stat.addItem(this.determinedMatches[matchId]);
			matchId++;

			if (matchId == this.determinedMatches.length) {
				break;
			}
		}

		int matchId2 = 0;
		while ((line = reader.readLine()) != null) {
			this.undeterminedMatches[matchId2] = NovoGlycoCompMatch.parse(line);
			matchId2++;

			if (matchId2 == this.undeterminedMatches.length) {
				break;
			}
		}

		int feaId = 0;
		while ((line = reader.readLine()) != null) {
			this.features[feaId++] = Features.parse(line);
			if (feaId == this.features.length) {
				break;
			}
		}

		while ((line = reader.readLine()) != null) {
			String[] cs1 = line.split(";");
			this.feasIdMap.put(Integer.parseInt(cs1[0]), Integer.parseInt(cs1[1]));
		}

		reader.close();
	}

	public Glycosyl[] getGlycosyls() {
		return glycosyls;
	}

	public NovoGlycoCompMatch[] getDeterminedMatches() {
		return determinedMatches;
	}

	public NovoGlycoCompMatch[] getUndeterminedMatches() {
		return undeterminedMatches;
	}

	public Features[] getFeatures() {
		return features;
	}

	public HashMap<Integer, Integer> getFeasIdMap() {
		return feasIdMap;
	}

	public GlycoMicroNovoStat getStat() {
		return stat;
	}

	public String[] getTitle() {
		String[] title = new String[9];
		title[0] = "Scannum";
		title[1] = "Precursor Mz";
		title[2] = "Precursor Charge";
		title[3] = "Composition";
		title[4] = "Glycan Mass";
		title[5] = "Delta Mass";
		title[6] = "Score";
		title[7] = "Q value";
		title[8] = "Classification";
		return title;
	}

	private static void readTest(String in) throws Exception {

		GlycoMicroNovoReader reader = new GlycoMicroNovoReader(in);
		reader.read();
		System.out.println(reader.determinedMatches.length + "\t" + reader.features.length);

		NovoGlycoCompMatch[] matches = reader.getDeterminedMatches();
		GlycoMicroNovoStat stat = new GlycoMicroNovoStat(reader.getGlycosyls());

		GlycoMatchClassifier classifier = new GlycoMatchClassifier(false);
		for (int i = 0; i < matches.length; i++) {
			if (!matches[i].isDetermined()) {
				continue;
			}
			// System.out.println("score\t"+matches[i].getRank()+"\t"+matches[i].getScore()*matches[i].getFactor());
			int[] composition = matches[i].getComposition();
			int glycanCount = 0;
			for (int j = 0; j < composition.length; j++) {
				glycanCount += composition[j];
			}
			double scoreThreshold1 = glycanCount * 0.8;
			double scoreThreshold2 = glycanCount * 0.3;
			if (matches[i].getRank() == 1) {
				if (matches[i].getScore() > scoreThreshold1 && matches[i].getOxoniumFactor() > 0) {
//					classifier.add(matches[i], matches[i].getScore() * matches[i].getFactor(), true);
//					classifier.add(matches[i], true);
				}
			} else {
//				if (matches[i].getScore() < scoreThreshold2 && matches[i].getOxoniumFactor() < 0) {
//					classifier.add(matches[i], matches[i].getScore() * matches[i].getFactor(), false);
//					classifier.add(matches[i], false);
//				}
			}
		}

//		Instances instances = classifier.buildClassfier();
//		System.out.println(classifier.evaluation(instances));

		int count = 0;
		for (int i = 0; i < matches.length; i++) {
			if (!matches[i].isDetermined()) {
				continue;
			}

//			System.out.println("rank\t"+matches[i].getRank()+"\t"+matches[i].isTarget());
//			classifier.classifyMatch(matches[i]);
			stat.addItem(matches[i]);
			if (matches[i].getqValue() < 0.01) {
				count++;
			}
		}

		System.out.println("count\t" + stat.getDeterminedRank1Count() + "\t" + stat.getDeterminedRank2Count() + "\t"
				+ stat.getUndeterminedRank1Count() + "\t" + stat.getUndeterminedRank2Count() + "\t" + count);
		int[] scores = stat.getRank1DeterminedScore();
		for (int i = 0; i < scores.length; i++) {
			// System.out.println("score\t"+(double)i*0.2+"\t"+scores[i]);
		}

		int[] scoreTP = stat.getScoreTP();
		int[] scoreFP = stat.getScoreFP();
		int[] scoreTN = stat.getScoreTN();
		int[] scoreFN = stat.getScoreFN();
		double[] fdr = stat.getScoreFDR();
		for (int i = 0; i < fdr.length; i++) {
			System.out.println((double) i / 5.0 + "\t" + scoreTP[i] + "\t" + scoreFP[i] + "\t" + fdr[i] + "\t"
					+ scoreTN[i] + "\t" + scoreFN[i]);
		}

		System.out.println("Qvalue\t" + stat.getQValue1() + "\t" + stat.getQValue5());
		System.out.println("class\t" + stat.getClassTP() + "\t" + stat.getClassFP() + "\t" + stat.getClassTN() + "\t"
				+ stat.getClassFN() + "\t" + stat.getClassSensitivity() + "\t" + stat.getClassSpecificity());
		double[][] scoreROC = stat.getScoreROC();

		for (int i = 0; i < scoreROC[0].length; i++) {
			System.out.println(i + "\t" + scoreROC[0][i] + "\t" + scoreROC[1][i]);
		}
	}

	private static void readTest2(String in, int scannum) throws IOException {
		GlycoMicroNovoReader reader = new GlycoMicroNovoReader(in);
		reader.read();
		Glycosyl[] glycosyls = reader.getGlycosyls();
		for (int i = 0; i < glycosyls.length; i++) {
			System.out.println(glycosyls[i]);
		}
		NovoGlycoCompMatch[] matches = reader.getDeterminedMatches();
		for (int i = 0; i < matches.length; i++) {
			if (matches[i].getScannum() == scannum) {
				System.out.println("line534\t" + i + "\t" + matches[i].isDetermined() + "\t"
						+ Arrays.toString(matches[i].getComposition()) + "\t" + matches[i].getComString() + "\t"
						+ matches[i].getScore() + "\t" + matches[i].getDeltaMass() + "\t" + matches[i].getFactor()
						+ "\t" + matches[i].getOxoniumFactor() + "\t" + matches[i].getOxoniumMap().size() + "\t"
						+ matches[i].getMissOxoniumMap().size());

				HashMap<Integer, GlycoPeakNode> nodemap = matches[i].getNodeMap();
				for (Integer id : nodemap.keySet()) {
					GlycoPeakNode node = nodemap.get(id);
					System.out.println(node.getPeakId() + "\t" + node.getCharge() + "\t" + node.getPeakMz() + "\t"
							+ node.getMw() + "\t" + Arrays.toString(node.getComposition()));
				}
			}
		}
	}

	private static void readTest(String s1, String s2) throws Exception {

		GlycoMicroNovoReader reader1 = new GlycoMicroNovoReader(s1);
		reader1.read();
		GlycoMicroNovoStat stat1 = new GlycoMicroNovoStat(reader1.getGlycosyls());

		NovoGlycoCompMatch[] matches1 = reader1.getDeterminedMatches();

		for (int i = 0; i < matches1.length; i++) {
			if (!matches1[i].isDetermined()) {
				continue;
			}
			stat1.addItem(matches1[i]);
		}

		GlycoMicroNovoReader reader2 = new GlycoMicroNovoReader(s2);
		reader2.read();
		GlycoMicroNovoStat stat2 = new GlycoMicroNovoStat(reader2.getGlycosyls());

		NovoGlycoCompMatch[] matches2 = reader2.getDeterminedMatches();

		for (int i = 0; i < matches2.length; i++) {
			if (!matches2[i].isDetermined()) {
				continue;
			}
			stat2.addItem(matches2[i]);
		}

		int[] scoreTP1 = stat1.getScoreTP();
		int[] scoreTP2 = stat2.getScoreTP();
		for (int i = 0; i < scoreTP1.length; i++) {
			System.out.println((double) i * 0.2 + "\t" + scoreTP1[i] + "\t" + scoreTP2[i] + "\t"
					+ (double) scoreTP2[i] / (double) scoreTP1[i]);
		}
	}

	private static void compare(String s1, String s2) throws IOException {
		GlycoMicroNovoReader reader1 = new GlycoMicroNovoReader(s1);
		reader1.read();
		NovoGlycoCompMatch[] matches1 = reader1.getDeterminedMatches();
		HashMap<Integer, NovoGlycoCompMatch> map1 = new HashMap<Integer, NovoGlycoCompMatch>();
		for (NovoGlycoCompMatch match : matches1) {
			map1.put(match.getScannum(), match);
		}

		GlycoMicroNovoReader reader2 = new GlycoMicroNovoReader(s2);
		reader2.read();
		NovoGlycoCompMatch[] matches2 = reader2.getDeterminedMatches();
		HashMap<Integer, NovoGlycoCompMatch> map2 = new HashMap<Integer, NovoGlycoCompMatch>();
		for (NovoGlycoCompMatch match : matches2) {
			map2.put(match.getScannum(), match);
		}

		HashSet<Integer> set = new HashSet<Integer>();
		set.addAll(map1.keySet());
		set.addAll(map2.keySet());

		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer scannum = it.next();
			if (map1.containsKey(scannum) && map2.containsKey(scannum)) {
				NovoGlycoCompMatch m1 = map1.get(scannum);
				NovoGlycoCompMatch m2 = map2.get(scannum);
				String com1 = m1.getComString();
				String com2 = m2.getComString();
				double score1 = m1.getScore();
				double score2 = m2.getScore();
				if (!com1.equals(com2) && score1 != score2)
					System.out.println(scannum + "\t" + com1 + "\t" + m1.getScore() + "\t" + m1.getClassScore() + "\t"
							+ com2 + "\t" + m2.getScore() + "\t" + m2.getClassScore());
			}
		}
	}

	private static void export(String in, String out) throws IOException {
		GlycoMicroNovoReader reader = new GlycoMicroNovoReader(in);
		reader.read();
		GlycoMicroNovoXlsWriter writer = new GlycoMicroNovoXlsWriter(out, reader.getGlycosyls(), true);
		writer.write(reader.getDeterminedMatches(), reader.getUndeterminedMatches());
		writer.close();
	}

	private static void exportAll(String in) throws IOException {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("txt")) {
				String input = files[i].getAbsolutePath();
				String output = input.substring(0, input.length() - 3) + "xlsx";
				export(input, output);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//		GlycoMicroNovoReader.compare("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover2.txt");
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard.txt");
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g6.txt");
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6_standard.txt");
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6_standard.txt");
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6_discover.txt");
//		GlycoMicroNovoReader.readTest2("D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6_standard.txt", 1618);
//		NovoGlycoCompMatch[] matches = reader.matches;
//		for(int i=0;i<matches.length;i++){
//			System.out.println(matches[i].getNodeMap().size()+"\t"+matches[i].getScore());
//		}
//		GlycoMicroNovoReader.readTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g6.txt", 
//				"D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6_standard.txt");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_151223112738_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_151223112738_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_151223141227_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_HFD_step3_151223141227_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_151224035620_discover.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_151224035620_discover.xlsx");
//		GlycoMicroNovoReader.export("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.txt", 
//				"D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.xlsx");
		GlycoMicroNovoReader.exportAll("\\\\137.122.238.141\\Figeys_lab\\Kai\\data");
	}

}
