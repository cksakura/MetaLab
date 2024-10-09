package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.tools.MwCalculator;

public class DiaNNResultReader {

	private File inFile;
	private String nameString;
	private boolean hasResult;
	private HashMap<String, Double> modMassMap;
	private MwCalculator mwc;
	private String cut;

	private DiaNNPrecursor[] diaNNPrecursors;

	public DiaNNResultReader(String in, String name, String cut) {
		this(new File(in), name, cut);
	}

	public DiaNNResultReader(File in, String name, String cut) {
		this.inFile = in;
		this.nameString = name;

		this.modMassMap = new HashMap<String, Double>();
		this.modMassMap.put("UniMod:4", 57.021464);
		this.modMassMap.put("UniMod:35", 15.994915);
		this.modMassMap.put("UniMod:1", 42.010565);
		this.modMassMap.put("UniMod:21", 79.966331);
		this.modMassMap.put("UniMod:121", 114.042927);

		this.mwc = new MwCalculator();
		this.cut = cut;
		this.read();
	}

	public boolean hasResult() {
		return hasResult;
	}

	private void read() {
		File resultCsvFile = new File(inFile, nameString + ".tsv");
		if (resultCsvFile.exists()) {
			try {
				readIdenResult(resultCsvFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hasResult = true;
		} else {
			hasResult = false;
		}
	}

	private void readIdenResult(File resultCsvFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(resultCsvFile));
		String line = reader.readLine();
		String[] title = line.split("\t");

		int runId = -1;
		int pgId = -1;
		int modSeqId = -1;
		int seqId = -1;
		int chargeId = -1;
		int qvalueId = -1;
		int pepId = -1;
		int pgQvalueId = -1;
		int preTransId = -1;
		int scoreId = -1;

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Run")) {
				runId = i;
			} else if (title[i].equals("Protein.Group")) {
				pgId = i;
			} else if (title[i].equals("Modified.Sequence")) {
				modSeqId = i;
			} else if (title[i].equals("Stripped.Sequence")) {
				seqId = i;
			} else if (title[i].equals("Precursor.Charge")) {
				chargeId = i;
			} else if (title[i].equals("Q.Value")) {
				qvalueId = i;
			} else if (title[i].equals("PEP")) {
				pepId = i;
			} else if (title[i].equals("PG.Q.Value")) {
				pgQvalueId = i;
			} else if (title[i].equals("Precursor.Normalised")) {
				preTransId = i;
			} else if (title[i].equals("CScore")) {
				scoreId = i;
			}
		}

		HashSet<Character> cutSet = new HashSet<Character>();
		HashSet<Character> noCutSet = new HashSet<Character>();
		String[] cuts = this.cut.split(",");
		for (int i = 0; i < cuts.length; i++) {
			if (cuts[i].startsWith("!*")) {
				noCutSet.add(cuts[i].charAt(2));
			} else {
				cutSet.add(cuts[i].charAt(0));
			}
		}

		ArrayList<DiaNNPrecursor> list = new ArrayList<DiaNNPrecursor>(10000000);
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			String[] pros = cs[pgId].split(";");

			int missCleavage = 0;
			String sequence = cs[seqId];
			for (int i = 0; i < sequence.length() - 1; i++) {
				char aa = sequence.charAt(i);
				char bb = sequence.charAt(i + 1);
				if (cutSet.contains(aa) && !noCutSet.contains(bb)) {
					missCleavage++;
				}
			}

			double mass = this.mwc.getMonoIsotopeMZ(cs[seqId]);
			String modseq = cs[modSeqId];
			int fromIndex = 0;
			while (true) {
				int id = modseq.indexOf("UniMod", fromIndex);
				if (id < 0) {
					break;
				} else {
					fromIndex = modseq.indexOf(")", id);
					String modName = modseq.substring(id, fromIndex);
					if (this.modMassMap.containsKey(modName)) {
						mass += this.modMassMap.get(modName);
					}
				}
			}

			DiaNNPrecursor diaNNPrecursor = new DiaNNPrecursor(cs[runId], pros, cs[modSeqId], cs[seqId],
					Integer.parseInt(cs[chargeId]), missCleavage, mass, Double.parseDouble(cs[qvalueId]),
					Double.parseDouble(cs[pepId]), Double.parseDouble(cs[pgQvalueId]),
					Double.parseDouble(cs[preTransId]), Double.parseDouble(cs[scoreId]));

			list.add(diaNNPrecursor);
		}

		this.diaNNPrecursors = list.toArray(new DiaNNPrecursor[list.size()]);
		reader.close();
	}

	public DiaNNPrecursor[] getDiaNNPrecursors() {
		return diaNNPrecursors;
	}

	
	public static void main(String[] args) {
		DiaNNResultReader reader = new DiaNNResultReader(
				"Z:\\Kai\\Raw_files\\For_Kai_MouseGut\\MetaLab_dia\\firstSearch", "firstSearch", "K*,R*");
		HashSet<String> set1 = new HashSet<String>();
		for (int i = 0; i < reader.getDiaNNPrecursors().length; i++) {
			set1.add(reader.getDiaNNPrecursors()[i].getSeqString());
		}
		System.out.println(reader.getDiaNNPrecursors().length + "\t" + set1.size());
	}
	
}
