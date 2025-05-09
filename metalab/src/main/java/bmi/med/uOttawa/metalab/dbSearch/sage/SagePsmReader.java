package bmi.med.uOttawa.metalab.dbSearch.sage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class SagePsmReader {

	private SagePSM[] psms;

	private static final String REV = "REV_";

	private HashMap<String, Integer> ms2CountMap;

	public SagePsmReader(String in) {
		this(new File(in));
	}

	public SagePsmReader(File in) {
		read(in);
	}

	/**
	 * The spectra count in each raw file is unknown
	 * 
	 * @param in
	 */
	private void read(File in) {
		this.ms2CountMap = new HashMap<String, Integer>();
		ArrayList<SagePSM> list = new ArrayList<SagePSM>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
			String line = reader.readLine();

			String[] title = line.split("\t");

			int fileNameID = -1;
			int scanID = -1;
			int expMHID = -1;
			int chargeID = -1;
			int qValueID = -1;
			int sequenceID = -1;
			int calMHID = -1;
			int massPPMID = -1;
			int fragPPMID = -1;
			int hyperScoreID = -1;
			int disScoreID = -1;
			int specificitID = -1;
			int proteinID = -1;
			int missID = -1;
			int rtID = -1;
			int peptideQValueID = -1;
			int proteinQValueID = -1;
			int ms2IntensityID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("filename")) {
					fileNameID = i;
				} else if (title[i].equals("scannr")) {
					scanID = i;
				} else if (title[i].equals("expmass")) {
					expMHID = i;
				} else if (title[i].equals("charge")) {
					chargeID = i;
				} else if (title[i].equals("spectrum_q")) {
					qValueID = i;
				} else if (title[i].equals("peptide")) {
					sequenceID = i;
				} else if (title[i].equals("calcmass")) {
					calMHID = i;
				} else if (title[i].equals("precursor_ppm")) {
					massPPMID = i;
				} else if (title[i].equals("rt")) {
					rtID = i;
				} else if (title[i].equals("hyperscore")) {
					hyperScoreID = i;
				} else if (title[i].equals("sage_discriminant_score")) {
					disScoreID = i;
				} else if (title[i].equals("peptide_q")) {
					peptideQValueID = i;
				} else if (title[i].equals("semi_enzymatic")) {
					specificitID = i;
				} else if (title[i].equals("proteins")) {
					proteinID = i;
				} else if (title[i].equals("protein_q")) {
					proteinQValueID = i;
				} else if (title[i].equals("missed_cleavages")) {
					missID = i;
				} else if (title[i].equals("fragment_ppm")) {
					fragPPMID = i;
				} else if (title[i].equals("ms2_intensity")) {
					ms2IntensityID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == title.length) {
					String fileName = cs[fileNameID];
					if (fileName.endsWith("mzML")) {
						fileName = fileName.substring(0, fileName.length() - ".mzML".length());
					}

					int scan = 0;
					int scanIndex = cs[scanID].indexOf("scan=");
					if (scanIndex > 0) {
						scan = Integer.parseInt(cs[scanID].substring(scanIndex + "scan=".length()));
					}

					String modseq = cs[sequenceID];
					StringBuilder sb = new StringBuilder();
					boolean mod = false;
					for (char aa : modseq.toCharArray()) {
						if (aa == '[') {
							mod = true;
						} else if (aa == ']') {
							mod = false;
						} else {
							if (!mod) {
								sb.append(aa);
							}
						}
					}
					double expMass = Double.parseDouble(cs[expMHID]);
					double calMass = Double.parseDouble(cs[calMHID]);
					double massPPM = Double.parseDouble(cs[massPPMID]);
					double fragPPM = Double.parseDouble(cs[fragPPMID]);
					int charge = Integer.parseInt(cs[chargeID]);
					int miss = Integer.parseInt(cs[missID]);
					int specificity = Integer.parseInt(cs[specificitID]);
					double rt = Double.parseDouble(cs[rtID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					double hyperscore = Double.parseDouble(cs[hyperScoreID]);
					double disScore = Double.parseDouble(cs[disScoreID]);
					boolean isTarget = true;
					String[] proteins = cs[proteinID].split(";");
					for (String pro : proteins) {
						if (pro.startsWith(REV)) {
							isTarget = false;
						}
					}
					double peptideQ = Double.parseDouble(cs[peptideQValueID]);
					double proteinQ = Double.parseDouble(cs[proteinQValueID]);

					double ms2Intensity = Double.parseDouble(cs[ms2IntensityID]);

					SagePSM psm = new SagePSM(fileName, scan, sb.toString(), modseq, expMass, calMass, massPPM, fragPPM,
							charge, miss, specificity, rt, qValue, hyperscore, disScore, proteins, isTarget, peptideQ,
							proteinQ, ms2Intensity);
					list.add(psm);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new SagePSM[list.size()]);
	}

	public SagePSM[] getPsms() {
		return psms;
	}

	public HashMap<String, Integer> getMs2CountMap() {
		return ms2CountMap;
	}

	public static void filterFdr(String in, String out, double fdr) throws IOException {

		HashMap<String, float[]> scanNameMap = new HashMap<String, float[]>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		String[] title = line.split("\t");
		while ((line = reader.readLine()) != null) {

			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				float qvalue = Float.parseFloat(cs[4]);
				if (cs[15].equals("target")) {
					scanNameMap.put(cs[0], new float[] { qvalue, 1.0f });
				} else {
					scanNameMap.put(cs[0], new float[] { qvalue, 0.0f });
				}
			}

		}
		reader.close();

		String[] scanNames = scanNameMap.keySet().toArray(new String[scanNameMap.size()]);
		Arrays.sort(scanNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				float[] f1 = scanNameMap.get(o1);
				float[] f2 = scanNameMap.get(o2);

				if (f1[0] < f2[0]) {
					return -1;
				} else if (f1[0] > f2[0]) {
					return 1;
				}

				return 0;
			}
		});

		int target = 0;
		int decoy = 0;
		boolean begin = false;
		for (int i = 0; i < scanNames.length; i++) {
			if (begin) {
				scanNameMap.remove(scanNames[i]);
			} else {
				float[] score = scanNameMap.get(scanNames[i]);
				if (score[1] == 0.0f) {
					decoy++;
				} else {
					target++;
				}

				if (target * fdr < decoy) {
					scanNameMap.remove(scanNames[i]);
					begin = true;
				}
			}
		}

		PrintWriter writer = new PrintWriter(out);
		reader = new BufferedReader(new FileReader(in));

		writer.println(reader.readLine());

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				if (scanNameMap.containsKey(cs[0]) && cs[15].equals("target")) {
					writer.println(line);
				}
			}
		}
		reader.close();
		writer.close();
	}

	public static HashSet<String> getPepSet(String in) throws IOException {
		HashSet<String> set1 = new HashSet<String>();
		BufferedReader reader1 = new BufferedReader(new FileReader(in));
		String[] title = reader1.readLine().split("\t");
		String line = null;
		while ((line = reader1.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				set1.add(cs[5]);
			}
		}
		reader1.close();
		return set1;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SagePsmReader reader = new SagePsmReader("Z:\\Kai\\Raw_files\\run21\\test2\\sage\\results.sage.tsv");
		System.out.println(reader.psms.length);
	}

}
