/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

/**
 * Use this for the reading of pFind.spectra, use PFindResultReader for the reading of pFind-filtered.spectra
 * 
 * @see bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader
 * @author Kai Cheng
 */
public class PFindPsmReader {

	private PFindPSM[] psms;

	private static final String REV = "REV_";

	private HashMap<String, Integer> ms2CountMap;
	private HashMap<String, PostTransModification> modMap;

	public PFindPsmReader(String in) {
		this(new File(in));
	}

	public PFindPsmReader(File in) {
		read(in, new HashMap<String, PostTransModification>());
	}

	public PFindPsmReader(String in, HashMap<String, PostTransModification> modMap) {
		this(new File(in), modMap);
	}

	public PFindPsmReader(File in, HashMap<String, PostTransModification> modMap) {
		read(in, modMap);
	}

	public PFindPsmReader(String in, String ms2SpecDir) {
		this(new File(in), new File(ms2SpecDir));
	}

	public PFindPsmReader(File in, File ms2SpecDir) {
		read(in, ms2SpecDir, new HashMap<String, PostTransModification>());
	}

	public PFindPsmReader(File in, File ms2SpecDir, HashMap<String, PostTransModification> modMap) {
		read(in, ms2SpecDir, modMap);
	}

	public PFindPsmReader(String in, String ms2SpecDir, HashMap<String, HashSet<String>> promap) {
		this(new File(in), new File(ms2SpecDir), promap, new HashMap<String, PostTransModification>());
	}

	public PFindPsmReader(File in, File ms2SpecDir, HashMap<String, HashSet<String>> promap,
			HashMap<String, PostTransModification> modMap) {
		try {
			read(in, ms2SpecDir, promap, modMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The spectra count in each raw file is unknown
	 * 
	 * @param in
	 */
	private void read(File in, HashMap<String, PostTransModification> modMap) {
		this.ms2CountMap = new HashMap<String, Integer>();
		ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
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
			int massShiftID = -1;
			int rawScoreID = -1;
			int finalScoreID = -1;
			int modID = -1;
			int specificitID = -1;
			int proteinID = -1;
			int isTargetID = -1;
			int missID = -1;
			int fragMassShiftID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("File_Name")) {
					fileNameID = i;
				} else if (title[i].equals("Scan_No")) {
					scanID = i;
				} else if (title[i].equals("Exp.MH+")) {
					expMHID = i;
				} else if (title[i].equals("Charge")) {
					chargeID = i;
				} else if (title[i].equals("Q-value")) {
					qValueID = i;
				} else if (title[i].equals("Sequence")) {
					sequenceID = i;
				} else if (title[i].equals("Calc.MH+")) {
					calMHID = i;
				} else if (title[i].equals("Mass_Shift(Exp.-Calc.)")) {
					massShiftID = i;
				} else if (title[i].equals("Raw_Score")) {
					rawScoreID = i;
				} else if (title[i].equals("Final_Score")) {
					finalScoreID = i;
				} else if (title[i].equals("Modification")) {
					modID = i;
				} else if (title[i].equals("Specificity")) {
					specificitID = i;
				} else if (title[i].equals("Proteins")) {
					proteinID = i;
				} else if (title[i].equals("Target/Decoy")) {
					isTargetID = i;
				} else if (title[i].equals("Miss.Clv.Sites")) {
					missID = i;
				} else if (title[i].equals("Avg.Frag.Mass.Shift")) {
					fragMassShiftID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == title.length) {

					String fileName = cs[fileNameID];
					if (fileName.indexOf(".") >= 0) {
						fileName = fileName.substring(0, fileName.indexOf("."));
					}

					int scan = Integer.parseInt(cs[scanID]);
					double expMH = Double.parseDouble(cs[expMHID]);
					int charge = Integer.parseInt(cs[chargeID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					String sequence = cs[sequenceID];
					if (sequence.length() < 7) {
						continue;
					}
					if (qValue > 0.01) {
						break;
					}

					double calMH = Double.parseDouble(cs[calMHID]);
					double massShift = Double.parseDouble(cs[massShiftID]);
					double rawScore = Double.parseDouble(cs[rawScoreID]);
					double finalScore = -Math.log10(Double.parseDouble(cs[finalScoreID]));
					String mod = cs[modID];
					int specificity = Integer.parseInt(cs[specificitID]);
					boolean isTarget = cs[isTargetID].equals("target");
					String[] proteins = cs[proteinID].split("/");

					// for short peptides (generally 6 aminoacids), they can belong to both target and decoy proteins
					if (isTarget) {
						ArrayList<String> proList = new ArrayList<String>();
						for (int i = 0; i < proteins.length; i++) {
							if (!proteins[i].startsWith(REV)) {
								proList.add(proteins[i]);
							}
						}
						if (proList.size() == 0) {
							continue;
						}
						proteins = proList.toArray(new String[proList.size()]);
					}

					int miss = Integer.parseInt(cs[missID]);
					double fragMassShift = Double.parseDouble(cs[fragMassShiftID]);

					PFindPSM psm = new PFindPSM(fileName, scan, expMH, charge, qValue, sequence, calMH, massShift,
							rawScore, finalScore, mod, modMap.get(mod), specificity, proteins, isTarget, miss,
							fragMassShift);
					list.add(psm);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new PFindPSM[list.size()]);
	}

	/**
	 * Get the information of retention time from the mgf files and add it to the
	 * psm
	 * 
	 * @param in
	 * @param rawDir
	 */
	private void read(File in, File rawDir, HashMap<String, PostTransModification> modMap) {

		this.ms2CountMap = new HashMap<String, Integer>();
		HashMap<String, HashMap<Integer, Double>> rtmap = new HashMap<String, HashMap<Integer, Double>>();

		File rtFile = new File(rawDir, "rt.txt");
		if (rtFile.exists() && rtFile.length() > 0) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(rtFile));
				String line = reader.readLine();
				String[] title = line.split("\t");
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (cs.length == title.length) {
						if (rtmap.containsKey(cs[0])) {
							rtmap.get(cs[0]).put(Integer.parseInt(cs[1]), Double.parseDouble(cs[2]));
						} else {
							HashMap<Integer, Double> map = new HashMap<Integer, Double>();
							map.put(Integer.parseInt(cs[1]), Double.parseDouble(cs[2]));
							rtmap.put(cs[0], map);
						}
					}
				}
				reader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			File[] ms2Files = rawDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					// TODO Auto-generated method stub
					if (arg0.getName().endsWith("ms2")) {
						return true;
					}
					return false;
				}

			});

			for (int i = 0; i < ms2Files.length; i++) {

				String line = null;
				int scan = -1;
				double rt = -1;
				try {
					int ms2Count = 0;
					HashMap<Integer, Double> map = new HashMap<Integer, Double>();
					BufferedReader reader = new BufferedReader(new FileReader(ms2Files[i]));
					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (cs.length == 3) {

							if (cs[1].equals("RetTime")) {
								rt = Double.parseDouble(cs[2]);
								map.put(scan, rt / 60.0);
							}
						} else if (cs.length == 4) {
							if (cs[0].equals("S")) {
								scan = Integer.parseInt(cs[1]);
								ms2Count++;
							}
						}
					}
					reader.close();

					String name = ms2Files[i].getName();
					name = name.substring(0, name.lastIndexOf("."));

					rtmap.put(name, map);
					this.ms2CountMap.put(name, ms2Count);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
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
			int massShiftID = -1;
			int rawScoreID = -1;
			int finalScoreID = -1;
			int modID = -1;
			int specificitID = -1;
			int proteinID = -1;
			int isTargetID = -1;
			int missID = -1;
			int fragMassShiftID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("File_Name")) {
					fileNameID = i;
				} else if (title[i].equals("Scan_No")) {
					scanID = i;
				} else if (title[i].equals("Exp.MH+")) {
					expMHID = i;
				} else if (title[i].equals("Charge")) {
					chargeID = i;
				} else if (title[i].equals("Q-value")) {
					qValueID = i;
				} else if (title[i].equals("Sequence")) {
					sequenceID = i;
				} else if (title[i].equals("Calc.MH+")) {
					calMHID = i;
				} else if (title[i].equals("Mass_Shift(Exp.-Calc.)")) {
					massShiftID = i;
				} else if (title[i].equals("Raw_Score")) {
					rawScoreID = i;
				} else if (title[i].equals("Final_Score")) {
					finalScoreID = i;
				} else if (title[i].equals("Modification")) {
					modID = i;
				} else if (title[i].equals("Specificity")) {
					specificitID = i;
				} else if (title[i].equals("Proteins")) {
					proteinID = i;
				} else if (title[i].equals("Target/Decoy")) {
					isTargetID = i;
				} else if (title[i].equals("Miss.Clv.Sites")) {
					missID = i;
				} else if (title[i].equals("Avg.Frag.Mass.Shift")) {
					fragMassShiftID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == title.length) {

					String fileName = cs[fileNameID];
					fileName = fileName.substring(0, fileName.indexOf("."));
					int scan = Integer.parseInt(cs[scanID]);
					double expMH = Double.parseDouble(cs[expMHID]);
					int charge = Integer.parseInt(cs[chargeID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					String sequence = cs[sequenceID];
					double calMH = Double.parseDouble(cs[calMHID]);
					double massShift = Double.parseDouble(cs[massShiftID]);
					double rawScore = Double.parseDouble(cs[rawScoreID]);
					double finalScore = Double.parseDouble(cs[finalScoreID]);
					String mod = cs[modID];
					int specificity = Integer.parseInt(cs[specificitID]);
					boolean isTarget = cs[isTargetID].equals("target");
					String[] proteins = cs[proteinID].split("/");

					// for short peptides (generally 6 aminoacids), they can belong to both target
					// and decoy proteins
					if (isTarget) {
						ArrayList<String> proList = new ArrayList<String>();
						for (int i = 0; i < proteins.length; i++) {
							if (!proteins[i].startsWith(REV)) {
								proList.add(proteins[i]);
							}
						}
						if (proList.size() == 0) {
							continue;
						}
						proteins = proList.toArray(new String[proList.size()]);
					}

					int miss = Integer.parseInt(cs[missID]);
					double fragMassShift = Double.parseDouble(cs[fragMassShiftID]);

					PFindPSM psm = new PFindPSM(fileName, scan, expMH, charge, qValue, sequence, calMH, massShift,
							rawScore, finalScore, mod, modMap.get(mod), specificity, proteins, isTarget, miss,
							fragMassShift);

					if (rtmap.containsKey(fileName)) {
						HashMap<Integer, Double> map = rtmap.get(fileName);
						if (map.containsKey(scan)) {
							psm.setRt(map.get(scan));
						}
					}

					list.add(psm);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new PFindPSM[list.size()]);
	}

	/**
	 * The protein information is not read from the "pFind-Filtered.spectra" file
	 * directly, but from the "pFind.protein" file.
	 * 
	 * @param in
	 * @param rawDir
	 * @param promap
	 * @throws IOException 
	 */
	private void read(File in, File ms2SpecDir, HashMap<String, HashSet<String>> promap,
			HashMap<String, PostTransModification> modMap) throws IOException {

		File ms2CountFile = new File(ms2SpecDir, "ms2Count.txt");
		this.ms2CountMap = new HashMap<String, Integer>();
		if (ms2CountFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(ms2CountFile));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				ms2CountMap.put(cs[0], Integer.parseInt(cs[1]));
			}
			reader.close();
		}

		File rtFile = new File(ms2SpecDir, "rt.txt");
		HashMap<String, HashMap<Integer, Double>> rtmap = new HashMap<String, HashMap<Integer, Double>>();
		if (rtFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(rtFile));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				HashMap<Integer, Double> rtmapi;
				if (rtmap.containsKey(cs[0])) {
					rtmapi = rtmap.get(cs[0]);
				} else {
					rtmapi = new HashMap<Integer, Double>();
					rtmap.put(cs[0], rtmapi);
				}
				rtmapi.put(Integer.parseInt(cs[1]), Double.parseDouble(cs[2]));
			}
			reader.close();

		} else {

			File[] ms2Files = ms2SpecDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					// TODO Auto-generated method stub
					if (arg0.getName().endsWith("ms2")) {
						return true;
					}
					return false;
				}

			});

			PrintWriter rtWriter = new PrintWriter(rtFile);
			PrintWriter ms2CountWriter = new PrintWriter(ms2CountFile);

			rtWriter.println("FileName\tScannum\tRt");
			ms2CountWriter.println("FileName\tMS2 count");

			for (int i = 0; i < ms2Files.length; i++) {

				String line = null;
				int scan = -1;
				double rt = -1;
				int ms2Count = 0;
				HashMap<Integer, Double> map = new HashMap<Integer, Double>();
				BufferedReader reader = new BufferedReader(new FileReader(ms2Files[i]));
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (cs.length == 3) {

						if (cs[1].equals("RetTime")) {
							rt = Double.parseDouble(cs[2]);
							map.put(scan, rt / 60.0);
						}
					} else if (cs.length == 4) {
						if (cs[0].equals("S")) {
							scan = Integer.parseInt(cs[1]);
							ms2Count++;
						}
					}
				}
				reader.close();

				String name = ms2Files[i].getName();
				name = name.substring(0, name.lastIndexOf("."));

				rtmap.put(name, map);
				this.ms2CountMap.put(name, ms2Count);

				for (Integer scannumi : map.keySet()) {
					rtWriter.println(name + "\t" + scannumi + "\t" + map.get(scannumi));
				}
				ms2CountWriter.println(name + "\t" + ms2Count);
			}

			rtWriter.close();
			ms2CountWriter.close();
		}

		ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
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
			int massShiftID = -1;
			int rawScoreID = -1;
			int finalScoreID = -1;
			int modID = -1;
			int specificitID = -1;
//			int proteinID = -1;
			int isTargetID = -1;
			int missID = -1;
			int fragMassShiftID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("File_Name")) {
					fileNameID = i;
				} else if (title[i].equals("Scan_No")) {
					scanID = i;
				} else if (title[i].equals("Exp.MH+")) {
					expMHID = i;
				} else if (title[i].equals("Charge")) {
					chargeID = i;
				} else if (title[i].equals("Q-value")) {
					qValueID = i;
				} else if (title[i].equals("Sequence")) {
					sequenceID = i;
				} else if (title[i].equals("Calc.MH+")) {
					calMHID = i;
				} else if (title[i].equals("Mass_Shift(Exp.-Calc.)")) {
					massShiftID = i;
				} else if (title[i].equals("Raw_Score")) {
					rawScoreID = i;
				} else if (title[i].equals("Final_Score")) {
					finalScoreID = i;
				} else if (title[i].equals("Modification")) {
					modID = i;
				} else if (title[i].equals("Specificity")) {
					specificitID = i;
				} else if (title[i].equals("Proteins")) {
//					proteinID = i;
				} else if (title[i].equals("Target/Decoy")) {
					isTargetID = i;
				} else if (title[i].equals("Miss.Clv.Sites")) {
					missID = i;
				} else if (title[i].equals("Avg.Frag.Mass.Shift")) {
					fragMassShiftID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == title.length) {

					String fileName = cs[fileNameID];
					fileName = fileName.substring(0, fileName.indexOf("."));
					int scan = Integer.parseInt(cs[scanID]);
					double expMH = Double.parseDouble(cs[expMHID]);
					int charge = Integer.parseInt(cs[chargeID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					String sequence = cs[sequenceID];
					double calMH = Double.parseDouble(cs[calMHID]);
					double massShift = Double.parseDouble(cs[massShiftID]);
					double rawScore = Double.parseDouble(cs[rawScoreID]);
					double finalScore = Double.parseDouble(cs[finalScoreID]);
					String mod = cs[modID];
					int specificity = Integer.parseInt(cs[specificitID]);

					String[] proteins;

					if (promap.containsKey(sequence)) {
						HashSet<String> plist = promap.get(sequence);
						proteins = plist.toArray(new String[plist.size()]);
					} else {
						/*
						 * String[] proteins = cs[proteinID].split("/"); for (int i = 0; i <
						 * proteins.length; i++) { prosb.append(proteins[i]).append(";"); }
						 */

						continue;
					}

					boolean isTarget = cs[isTargetID].equals("target");
					int miss = Integer.parseInt(cs[missID]);
					double fragMassShift = Double.parseDouble(cs[fragMassShiftID]);

					PFindPSM psm = new PFindPSM(fileName, scan, expMH, charge, qValue, sequence, calMH, massShift,
							rawScore, finalScore, mod, modMap.get(mod), specificity, proteins, isTarget, miss,
							fragMassShift);

					if (rtmap.containsKey(fileName)) {
						HashMap<Integer, Double> map = rtmap.get(fileName);
						if (map.containsKey(scan)) {
							psm.setRt(map.get(scan));
						}
					}

					list.add(psm);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new PFindPSM[list.size()]);
	}

	/**
	 * The protein information is not read from the "pFind-Filtered.spectra" file
	 * directly, but from the "pFind.protein" file.
	 * 
	 * @param in
	 * @param rawDir
	 * @param promap
	 */
	@SuppressWarnings("unused")
	private void read(File in, HashMap<String, HashSet<String>> promap,
			HashMap<String, Integer> ms2CountMap, HashMap<String, HashMap<Integer, Double>> rtmap,
			HashMap<String, PostTransModification> modMap) {

		this.ms2CountMap = ms2CountMap;
		this.modMap = modMap;

		ArrayList<PFindPSM> list = new ArrayList<PFindPSM>();
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
			int massShiftID = -1;
			int rawScoreID = -1;
			int finalScoreID = -1;
			int modID = -1;
			int specificitID = -1;
			int proteinID = -1;
			int isTargetID = -1;
			int missID = -1;
			int fragMassShiftID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("File_Name")) {
					fileNameID = i;
				} else if (title[i].equals("Scan_No")) {
					scanID = i;
				} else if (title[i].equals("Exp.MH+")) {
					expMHID = i;
				} else if (title[i].equals("Charge")) {
					chargeID = i;
				} else if (title[i].equals("Q-value")) {
					qValueID = i;
				} else if (title[i].equals("Sequence")) {
					sequenceID = i;
				} else if (title[i].equals("Calc.MH+")) {
					calMHID = i;
				} else if (title[i].equals("Mass_Shift(Exp.-Calc.)")) {
					massShiftID = i;
				} else if (title[i].equals("Raw_Score")) {
					rawScoreID = i;
				} else if (title[i].equals("Final_Score")) {
					finalScoreID = i;
				} else if (title[i].equals("Modification")) {
					modID = i;
				} else if (title[i].equals("Specificity")) {
					specificitID = i;
				} else if (title[i].equals("Proteins")) {
					proteinID = i;
				} else if (title[i].equals("Target/Decoy")) {
					isTargetID = i;
				} else if (title[i].equals("Miss.Clv.Sites")) {
					missID = i;
				} else if (title[i].equals("Avg.Frag.Mass.Shift")) {
					fragMassShiftID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == title.length) {

					String fileName = cs[fileNameID];
					fileName = fileName.substring(0, fileName.indexOf("."));
					int scan = Integer.parseInt(cs[scanID]);
					double expMH = Double.parseDouble(cs[expMHID]);
					int charge = Integer.parseInt(cs[chargeID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					String sequence = cs[sequenceID];
					double calMH = Double.parseDouble(cs[calMHID]);
					double massShift = Double.parseDouble(cs[massShiftID]);
					double rawScore = Double.parseDouble(cs[rawScoreID]);
					double finalScore = Double.parseDouble(cs[finalScoreID]);
					String mod = cs[modID];
					int specificity = Integer.parseInt(cs[specificitID]);

					String[] proteins;

					if (promap.containsKey(sequence)) {
						HashSet<String> plist = promap.get(sequence);
						proteins = plist.toArray(new String[plist.size()]);
					} else {
						/*
						 * String[] proteins = cs[proteinID].split("/"); for (int i = 0; i <
						 * proteins.length; i++) { prosb.append(proteins[i]).append(";"); }
						 */

						continue;
					}

					boolean isTarget = cs[isTargetID].equals("target");
					int miss = Integer.parseInt(cs[missID]);
					double fragMassShift = Double.parseDouble(cs[fragMassShiftID]);

					PFindPSM psm = new PFindPSM(fileName, scan, expMH, charge, qValue, sequence, calMH, massShift,
							rawScore, finalScore, mod, modMap.get(mod), specificity, proteins, isTarget, miss,
							fragMassShift);

					if (rtmap.containsKey(fileName)) {
						HashMap<Integer, Double> map = rtmap.get(fileName);
						if (map.containsKey(scan)) {
							psm.setRt(map.get(scan));
						}
					}

					list.add(psm);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new PFindPSM[list.size()]);
	}

	public PFindPSM[] getPsms() {
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
	
	public HashMap<String, PostTransModification> getModMap() {
		return modMap;
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
	
	@SuppressWarnings("unused")
	private static void compare(String s1, String s2) {
		PFindPsmReader reader1 = new PFindPsmReader(s1);
		PFindPSM[] psms1 = reader1.getPsms();
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < psms1.length; i++) {
			set.add(psms1[i].getScan() + "\t" + psms1[i].getSequence());
		}

		PFindPsmReader reader2 = new PFindPsmReader(s2);
		PFindPSM[] psms2 = reader2.getPsms();

		int count = 0;
		int count2 = 0;
		String name = (new File(s1)).getParentFile().getName();
		System.out.println(name);

		for (int i = 0; i < psms2.length; i++) {
			if (psms2[i].getFileName().equals(name)) {
				count2++;
				if (set.contains(psms2[i].getScan() + "\t" + psms2[i].getSequence()))
					count++;
			}
		}
		System.out.println(psms1.length + "\t" + set.size() + "\t" + count + "\t" + count2);
	}
}
