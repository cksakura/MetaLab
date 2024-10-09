package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvWriter;

public class PFindResultReader {

	public static final String REV = "REV_";
	private static final int confPepLength = 8;
	
	private double psmFDR = 0.01;
	private double proFDR = 0.01;

	private HashMap<String, PostTransModification> modMap;

	private File resultFolder;
	private File spectraFile;
	private File proteinFile;
	private File ms2SpecDir;
	
	private boolean filter;

	private HashSet<String> modSet;
	private HashMap<String, Integer> ms2CountMap;
	private HashMap<String, HashMap<Integer, Double>> rtmap;

	private PFindPSM[] psms;
	private PFindProtein[] proteins;
	private HashMap<String, PFindProtein> proteinMap;
	private HashMap<String, PeptideResult> peptideMap;
	private PeptideResult[] peptideResults;
	private HashMap<String, ArrayList<PeptideResult>> basePeptideMap;
	
	private HashMap<String, HashSet<String>> originalPepProMap;
	private HashMap<String, HashSet<String>> originalProPepMap;

	private HashMap<String, HashSet<String>> razorProPepMap;
	private HashMap<String, String> razorPepProMap;

	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private Logger LOGGER = LogManager.getLogger(PFindResultReader.class);

	public PFindResultReader(String resultFolder, String ms2SpecDir) {
		this(new File(resultFolder), new File(ms2SpecDir));
	}

	public PFindResultReader(File resultFolder, File ms2SpecDir) {
		this(resultFolder, ms2SpecDir, new HashMap<String, PostTransModification>());
	}

	public PFindResultReader(File resultFolder, File ms2SpecDir, HashMap<String, PostTransModification> modMap) {
		this(resultFolder, ms2SpecDir, modMap, true);
	}
	
	public PFindResultReader(File resultFolder, File ms2SpecDir, HashMap<String, PostTransModification> modMap, boolean filter) {
		this.resultFolder = resultFolder;
		this.spectraFile = new File(resultFolder, "pFind.spectra");
		this.proteinFile = new File(resultFolder, "pFind.protein");
		this.ms2SpecDir = ms2SpecDir;
		this.modMap = modMap;
		this.filter = filter;

		this.ms2CountMap = new HashMap<String, Integer>();
		this.rtmap = new HashMap<String, HashMap<Integer, Double>>();
	}

	private static class ProteinGroup {
		@SuppressWarnings("unused")
		String proName;
		HashSet<String> sameSet;

		private ProteinGroup(String proName) {
			this.proName = proName;
			this.sameSet = new HashSet<String>();
		}

		private void addSameGroup(String pro) {
			this.sameSet.add(pro);
		}
	}

	public class PeptideResult {

		int id;
		String sequence;
		String baseSeq;
		int length;
		int miss;
		double mass;
		boolean target;
		String[] proteins;
		int[] charge;
		double rawScore;
		double finalScore;
		double qValue;
		int ms2Count;

		public PeptideResult(int id, String sequence, String baseSeq, int length, int miss, double mass, boolean target,
				String[] proteins, int[] charge, double rawScore, double finalScore, double qValue, int ms2Count) {
			this.id = id;
			this.sequence = sequence;
			this.baseSeq = baseSeq;
			this.length = length;
			this.miss = miss;
			this.mass = mass;
			this.target = target;
			this.proteins = proteins;
			this.charge = charge;
			this.rawScore = rawScore;
			this.finalScore = finalScore;
			this.qValue = qValue;
			this.ms2Count = ms2Count;
		}

		public int getId() {
			return id;
		}

		public String getSequence() {
			return sequence;
		}

		public String getBaseSeq() {
			return baseSeq;
		}

		public int getLength() {
			return length;
		}

		public int getMiss() {
			return miss;
		}

		public double getMass() {
			return mass;
		}

		public boolean isTarget() {
			return target;
		}

		public String[] getProteins() {
			return proteins;
		}

		public int[] getCharge() {
			return charge;
		}

		public double getRawScore() {
			return rawScore;
		}

		public double getFinalScore() {
			return finalScore;
		}

		public double getQValue() {
			return qValue;
		}

		public int getMs2Count() {
			return ms2Count;
		}
	}

	public boolean hasResult() {
		if (!this.spectraFile.exists() || this.spectraFile.length() == 0) {
			this.spectraFile = new File(resultFolder, "pFind-Filtered.spectra");
			this.filter = false;
		} else {
			this.filter = true;
		}
		if (!this.spectraFile.exists() || this.spectraFile.length() == 0) {
			return false;
		}
		if (!this.proteinFile.exists() || this.proteinFile.length() == 0) {
			return false;
		}
		return true;
	}

	public void read() throws NumberFormatException, IOException {

		if (psms != null && psms.length > 0 && proteins != null && proteins.length > 0 && peptideResults != null
				&& peptideResults.length > 0) {

			int targetPsmCount = 0;
			int targetPepCount = 0;

			for (PFindPSM psm : this.psms) {
				if (psm.isTarget()) {
					targetPsmCount++;
				}
			}

			for (PeptideResult pep : this.peptideResults) {
				if (pep.isTarget()) {
					targetPepCount++;
				}
			}

			LOGGER.info("Reading pFind result from " + resultFolder + " finished, " + targetPsmCount + " PSMs, "
					+ targetPepCount + " peptide sequences and " + this.proteins.length + " protein groups were obtained.");
			
			System.out.println(format.format(new Date()) + "\t" + "Reading pFind result from " + resultFolder
					+ " finished, " + targetPsmCount + " PSMs, " + targetPepCount + " peptide sequences and "
					+ this.proteins.length + " protein groups were obtained.");

			return;
		}

		if (!hasResult()) {
			LOGGER.info("Database search result file was not found in " + resultFolder);
			throw new IOException("Database search result file was not found in " + resultFolder);
		}

		LOGGER.info("parsing retention time...");
		
		if (this.ms2SpecDir != null && this.ms2SpecDir.exists()) {
			getRtInfo();
		}

		LOGGER.info("parsing modifications...");

		parseModInfo();

		LOGGER.info("parsing PSMs...");

		readPSMs();

		LOGGER.info("parsing proteins...");

		readProteins();

		LOGGER.info("mapping peptides and proteins...");

		getMapInfo();

		int targetPsmCount = 0;
		int targetPepCount = 0;

		for (PFindPSM psm : this.psms) {
			if (psm.isTarget()) {
				targetPsmCount++;
			}
		}

		for (PeptideResult pep : this.peptideResults) {
			if (pep.isTarget()) {
				targetPepCount++;
			}
		}

		LOGGER.info("Reading pFind result from " + resultFolder + " finished, " + targetPsmCount + " PSMs, "
				+ targetPepCount + " peptide sequences and " + this.proteins.length + " protein groups were obtained.");

		System.out.println(format.format(new Date()) + "\t" + "Reading pFind result from " + resultFolder
				+ " finished, " + targetPsmCount + " PSMs, " + targetPepCount + " peptide sequences and "
				+ this.proteins.length + " protein groups were obtained.");
	}
	
	private void parseModInfo() throws IOException {
		this.modSet = new HashSet<String>();
		File parFile = new File(resultFolder, "pFind.cfg");
		if (!parFile.exists()) {
			parFile = new File(resultFolder, "backup_pFind.cfg");
		}

		if (!parFile.exists()) {
			return;
		}

		BufferedReader reader = new BufferedReader(new FileReader(parFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("=");
			if (cs.length == 2 && (cs[0].equals("fixmod") || cs[0].equals("selectmod"))) {
				String[] mods = cs[1].split(";");
				for (int i = 0; i < mods.length; i++) {
					this.modSet.add(mods[i]);
				}
			}
		}
		reader.close();
	}

	private void getRtInfo() throws NumberFormatException, IOException {

		File ms2CountFile = new File(ms2SpecDir, "ms2Count.txt");
		File rtFile = new File(ms2SpecDir, "rt.txt");

		if (ms2CountFile.exists() && rtFile.exists() && ms2CountFile.length() > 0 && rtFile.length() > 0) {

			BufferedReader ms2reader = new BufferedReader(new FileReader(ms2CountFile));
			String line = ms2reader.readLine();
			while ((line = ms2reader.readLine()) != null) {
				String[] cs = line.split("\t");
				ms2CountMap.put(cs[0], Integer.parseInt(cs[1]));
			}
			ms2reader.close();

			BufferedReader rtreader = new BufferedReader(new FileReader(rtFile));
			line = rtreader.readLine();

			while ((line = rtreader.readLine()) != null) {
				String[] cs = line.split("\t");
				HashMap<Integer, Double> rtmapi;

				if (this.rtmap.containsKey(cs[0])) {
					rtmapi = this.rtmap.get(cs[0]);
				} else {
					rtmapi = new HashMap<Integer, Double>();
					this.rtmap.put(cs[0], rtmapi);
				}
				rtmapi.put(Integer.parseInt(cs[1]), Double.parseDouble(cs[2]));
			}
			rtreader.close();

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

				this.rtmap.put(name, map);
				this.ms2CountMap.put(name, ms2Count);

				for (Integer scannumi : map.keySet()) {
					rtWriter.println(name + "\t" + scannumi + "\t" + map.get(scannumi));
				}
				ms2CountWriter.println(name + "\t" + ms2Count);
			}

			rtWriter.close();
			ms2CountWriter.close();
		}
	}

	private void readPSMs() throws IOException {

		ArrayList<PFindPSM> list = new ArrayList<PFindPSM>(2000000);
		BufferedReader reader = new BufferedReader(new FileReader(spectraFile));
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
			} else if (title[i].equals("Miss.Clv.Sites")) {
				missID = i;
			} else if (title[i].equals("Avg.Frag.Mass.Shift")) {
				fragMassShiftID = i;
			}
		}

		this.originalPepProMap = new HashMap<String, HashSet<String>>();
		this.originalProPepMap = new HashMap<String, HashSet<String>>();

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
				double calMH = Double.parseDouble(cs[calMHID]);
				double massShift = Double.parseDouble(cs[massShiftID]);
				double rawScore = Double.parseDouble(cs[rawScoreID]);
				double finalScore = Double.parseDouble(cs[finalScoreID]);
				String mod = cs[modID];
				int specificity = Integer.parseInt(cs[specificitID]);
				boolean isTarget = false;
				String[] proteins = cs[proteinID].split("/");
				for (int i = 0; i < proteins.length; i++) {
					if (!proteins[i].startsWith(REV)) {
						isTarget = true;
					}
				}

				if (qValue > psmFDR) {
					break;
				}

				int miss = Integer.parseInt(cs[missID]);
				double fragMassShift = Double.parseDouble(cs[fragMassShiftID]);

				PFindPSM psm = new PFindPSM(fileName, scan, expMH, charge, qValue, sequence, calMH, massShift, rawScore,
						finalScore, mod, modMap.get(mod), specificity, proteins, isTarget, miss, fragMassShift);

				if (this.rtmap.containsKey(fileName)) {
					HashMap<Integer, Double> map = this.rtmap.get(fileName);
					if (map.containsKey(scan)) {
						psm.setRt(map.get(scan));
					}
				}

				list.add(psm);

				HashSet<String> proSet;
				if (this.originalPepProMap.containsKey(sequence)) {
					proSet = this.originalPepProMap.get(sequence);
				} else {
					proSet = new HashSet<String>();
					this.originalPepProMap.put(sequence, proSet);
				}
				for (String pro : proteins) {
					proSet.add(pro);

					if (this.originalProPepMap.containsKey(pro)) {
						this.originalProPepMap.get(pro).add(sequence);
					} else {
						HashSet<String> pepSet = new HashSet<String>();
						pepSet.add(sequence);
						this.originalProPepMap.put(pro, pepSet);
					}
				}
			}
		}
		reader.close();

		this.psms = list.toArray(new PFindPSM[list.size()]);
	}

	private void readProteins() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(this.proteinFile));
		String line = reader.readLine();
		String[] title = line.split("\t");

		int nameID = -1;
		int scoreID = -1;
		int qValueID = -1;
		int coverageID = -1;
		int pepCountID = -1;
		int desID = -1;

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("AC")) {
				nameID = i;
			} else if (title[i].equals("Score")) {
				scoreID = i;
			} else if (title[i].equals("Q-Value")) {
				qValueID = i;
			} else if (title[i].equals("Coverage")) {
				coverageID = i;
			} else if (title[i].equals("No.Peptide")) {
				pepCountID = i;
			} else if (title[i].equals("Description")) {
				desID = i;
			}
		}

		String[] title2 = reader.readLine().split("\t");
		int spCountID = -1;
		int seqID = -1;
		int modId = -1;
		int pepScoreID = -1;

		for (int i = 0; i < title2.length; i++) {
			if (title2[i].equals("Spec_Num")) {
				spCountID = i;
			} else if (title2[i].equals("Sequence")) {
				seqID = i;
			} else if (title2[i].equals("Modification")) {
				modId = i;
			} else if (title2[i].equals("Final_Score")) {
				pepScoreID = i;
			}
		}

		HashMap<String, Boolean> longPepMap = new HashMap<String, Boolean>();
		for (int i = 0; i < this.psms.length; i++) {
			if (psms[i].getSequence().length() >= confPepLength) {
				longPepMap.put(psms[i].getSequence(), false);
			}
		}

		boolean strict = false;
		int spCount = 0;
		String name = "";
		int[] tdCount = new int[2];
		boolean passPeptide = false;
		boolean absPassPeptide = false;

		this.proteinMap = new HashMap<String, PFindProtein>();

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				if (proteinMap.containsKey(name)) {
					PFindProtein lastProtein = proteinMap.get(name);
					if (filter) {
						if (absPassPeptide) {
							lastProtein.setSpCount(spCount);
							if (name.startsWith(REV)) {
								tdCount[1]++;
							} else {
								tdCount[0]++;
							}
						} else {
							if (passPeptide) {
								if (strict) {
									if (lastProtein.getPepCount() > 1) {
										lastProtein.setSpCount(spCount);
										if (name.startsWith(REV)) {
											tdCount[1]++;
										} else {
											tdCount[0]++;
										}
									} else {
										proteinMap.remove(name);
									}
								} else {
									lastProtein.setSpCount(spCount);
									if (name.startsWith(REV)) {
										tdCount[1]++;
									} else {
										tdCount[0]++;
									}
								}
							} else {
								proteinMap.remove(name);
							}
						}

						if ((double) tdCount[1] / (double) tdCount[0] > proFDR) {
							break;
						}
					} else {
						lastProtein.setSpCount(spCount);
						if (name.startsWith(REV)) {
							tdCount[1]++;
						} else {
							tdCount[0]++;
						}
					}
				}

				name = cs[nameID];

				double score = Double.parseDouble(cs[scoreID]);
				double qValue = Double.parseDouble(cs[qValueID]);
				double coverage = Double.parseDouble(cs[coverageID]);
				int pepCount = Integer.parseInt(cs[pepCountID]);
				String des = cs[desID];
				spCount = 0;

				PFindProtein protein = new PFindProtein(proteinMap.size(), name, score, qValue, coverage, pepCount,
						des, new HashSet<String>());

				this.proteinMap.put(name, protein);
				passPeptide = false;
				absPassPeptide = false;

			} else if (cs.length == title2.length) {
				spCount += Integer.parseInt(cs[spCountID]);
				double finalScore = Double.parseDouble(cs[pepScoreID]);
				if (passPeptide) {
					if (longPepMap.containsKey(cs[seqID])) {
						if (!longPepMap.get(cs[seqID])) {
							if (!name.startsWith(REV)) {
								longPepMap.put(cs[seqID], true);
							}
						}
					}
				} else {
					if (longPepMap.containsKey(cs[seqID])) {
						if (name.startsWith(REV) && longPepMap.get(cs[seqID])) {
							continue;
						}

						if (finalScore < psmFDR * 2.0) {
							passPeptide = true;
							absPassPeptide = true;
						} else {
							if (strict && cs[modId].length() > 0) {
								String[] mods = cs[modId].split(";");
								boolean pass = true;
								for (int i = 0; i < mods.length; i++) {
									String mod = mods[i].substring(mods[i].indexOf(",") + 1);
									if (!this.modSet.contains(mod)) {
										pass = false;
									}
								}
								passPeptide = pass;
							} else {
								passPeptide = true;
							}
						}

						if (passPeptide) {
							if (!longPepMap.get(cs[seqID])) {
								if (!name.startsWith(REV)) {
									longPepMap.put(cs[seqID], true);
								}
							}
						}
					}
				}

			} else if (cs.length >= 4) {
				if (cs[1].equals("SameSet") && !cs[2].startsWith(REV)) {
					proteinMap.get(name).getSameSet().add(cs[2]);
				}
			} else if (cs.length == 1 && line.contains("------")) {
				strict = true;
			} else if (cs.length == 1 && line.contains("Summary")) {
				if (proteinMap.containsKey(name)) {
					PFindProtein lastProtein = proteinMap.get(name);
					if (filter) {
						if (absPassPeptide) {
							lastProtein.setSpCount(spCount);
							if (name.startsWith(REV)) {
								tdCount[1]++;
							} else {
								tdCount[0]++;
							}
						} else {
							if (passPeptide) {
								if (strict) {
									if (lastProtein.getPepCount() > 1) {
										lastProtein.setSpCount(spCount);
										if (name.startsWith(REV)) {
											tdCount[1]++;
										} else {
											tdCount[0]++;
										}
									} else {
										proteinMap.remove(name);
									}
								} else {
									lastProtein.setSpCount(spCount);
									if (name.startsWith(REV)) {
										tdCount[1]++;
									} else {
										tdCount[0]++;
									}
								}
							} else {
								proteinMap.remove(name);
							}
						}
					} else {
						lastProtein.setSpCount(spCount);
						if (name.startsWith(REV)) {
							tdCount[1]++;
						} else {
							tdCount[0]++;
						}
					}
				}
				break;
			}
		}
		reader.close();

		this.proteins = proteinMap.values().toArray(new PFindProtein[proteinMap.size()]);
		Arrays.sort(this.proteins, new Comparator<PFindProtein>() {
			@Override
			public int compare(PFindProtein o1, PFindProtein o2) {
				// TODO Auto-generated method stub
				return o1.getId() - o2.getId();
			}
		});

		this.razorPepProMap = new HashMap<String, String>();
		this.razorProPepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < this.proteins.length; i++) {
			String proName = this.proteins[i].getName();
			HashSet<String> pepSet = this.originalProPepMap.get(proName);
			this.razorProPepMap.put(proName, new HashSet<String>());
			for (String pep : pepSet) {
				if (!this.razorPepProMap.containsKey(pep)) {
					this.razorPepProMap.put(pep, proName);
					this.razorProPepMap.get(proName).add(pep);
				} else {
					if (this.razorPepProMap.get(pep).startsWith(REV)) {
						String revPro = this.razorPepProMap.get(pep);
						this.razorProPepMap.get(revPro).remove(pep);
						this.razorPepProMap.put(pep, proName);
						this.razorProPepMap.get(proName).add(pep);
					}
				}
			}
		}

		if (filter) {
			ArrayList<String> proNameList = new ArrayList<String>();
			for (int i = 0; i < this.proteins.length; i++) {
				String proName = this.proteins[i].getName();
				if (this.razorProPepMap.get(proName).size() == 0) {
					this.razorProPepMap.remove(proName);
				} else {
					proNameList.add(proName);
				}
			}

			this.proteins = new PFindProtein[proNameList.size()];
			for (int i = 0; i < proteins.length; i++) {
				String proName = proNameList.get(i);
				this.proteins[i] = this.proteinMap.get(proName);
				this.proteins[i].setId(i);
			}
		} 
	}
	
	private void getMapInfo() {

		ArrayList<PFindPSM> psmList = new ArrayList<PFindPSM>();
		for (PFindPSM psm : this.psms) {
			String pep = psm.getSequence();
			if (!this.razorPepProMap.containsKey(pep)) {
				continue;
			}

			String razorProName = this.razorPepProMap.get(pep);
			HashSet<String> proSet = new HashSet<String>();
			String[] pros = psm.getProteins();
			if (razorProName.startsWith(REV)) {
				for (int i = 0; i < pros.length; i++) {
					if (pros[i].startsWith(REV)) {
						proSet.add(pros[i]);
					}
				}
			} else {
				for (int i = 0; i < pros.length; i++) {
					if (!pros[i].startsWith(REV)) {
						proSet.add(pros[i]);
					}
				}
			}
			psm.setProteins(proSet.toArray(new String[proSet.size()]));
			psmList.add(psm);
		}

		this.psms = psmList.toArray(new PFindPSM[psmList.size()]);
		
		HashSet<String> subProSet = new HashSet<String>();
		for (int i = 0; i < proteins.length; i++) {
			String proName = proteins[i].getName();
			if (proName.startsWith(REV)) {
				continue;
			}

			HashSet<String> sameSet = proteins[i].getSameSet();
			HashSet<String> pepSet = this.originalProPepMap.get(proName);
			
			for (String pep : pepSet) {
				HashSet<String> proSet = this.originalPepProMap.get(pep);
				
				for (String pro : proSet) {
					if (pro.startsWith(REV)) {
						continue;
					}

					if (subProSet.contains(pro)) {
						continue;
					}

					if (proName.equals(pro) || sameSet.contains(pro)) {
						continue;
					}

					boolean sub = true;
					HashSet<String> pepISet = this.originalProPepMap.get(pro);
					
					for (String pepI : pepISet) {
						if (!pepSet.contains(pepI)) {
							sub = false;
						}
					}

					if (sub) {
						proteins[i].addSubProtein(pro);
						subProSet.add(pro);
					}
				}
			}
		}

		HashMap<String, Integer> countmap = new HashMap<String, Integer>();
		HashMap<String, PFindPSM> psmmap = new HashMap<String, PFindPSM>();
		HashMap<String, int[]> chargemap = new HashMap<String, int[]>();

		for (int i = 0; i < psms.length; i++) {
			String sequence = psms[i].getModseq();
			if (psmmap.containsKey(sequence)) {
				countmap.put(sequence, countmap.get(sequence) + 1);
				if (psms[i].getFinalScore() > psmmap.get(sequence).getFinalScore()) {
					psmmap.put(sequence, psms[i]);
				}
				int[] charge = chargemap.get(sequence);
				if (psms[i].getCharge() > 0 && psms[i].getCharge() < 10) {
					charge[psms[i].getCharge() - 1] = 1;
				}
				chargemap.put(sequence, charge);
			} else {
				psmmap.put(sequence, psms[i]);
				countmap.put(sequence, 1);

				int[] charge = new int[9];
				Arrays.fill(charge, 0);

				if (psms[i].getCharge() > 0 && psms[i].getCharge() < 10) {
					charge[psms[i].getCharge() - 1] = 1;
				}

				chargemap.put(sequence, charge);
			}
		}

		String[] modPeps = psmmap.keySet().toArray(new String[psmmap.size()]);
		Arrays.sort(modPeps);

		this.peptideResults = new PeptideResult[modPeps.length];
		this.peptideMap = new HashMap<String, PeptideResult>();
		this.basePeptideMap = new HashMap<String, ArrayList<PeptideResult>>();
		for (int i = 0; i < modPeps.length; i++) {

			PFindPSM psm = psmmap.get(modPeps[i]);
			String baseSeq = psm.getSequence();
			int length = baseSeq.length();
			int miss = psm.getMiss();
			double mass = psm.getCalMH() - AminoAcidProperty.PROTON_W;

			boolean target = !this.razorPepProMap.get(baseSeq).startsWith(REV);
			String[] proteins = psm.getProteins();

			int[] charge = chargemap.get(modPeps[i]);
			double rawScore = psm.getRawScore();
			double finalScore = psm.getFinalScore();
			double qValue = psm.getqValue();
			int ms2Count = countmap.get(modPeps[i]);

			peptideResults[i] = new PeptideResult(i, modPeps[i], baseSeq, length, miss, mass, target, proteins, charge,
					rawScore, finalScore, qValue, ms2Count);
			this.peptideMap.put(modPeps[i], peptideResults[i]);

			for (int j = 0; j < proteins.length; j++) {
				if (this.proteinMap.containsKey(proteins[j])) {
					proteinMap.get(proteins[j]).addPeptideId(i);
				}
			}

			if (this.basePeptideMap.containsKey(baseSeq)) {
				basePeptideMap.get(baseSeq).add(peptideResults[i]);
			} else {
				ArrayList<PeptideResult> list = new ArrayList<PeptideResult>();
				list.add(peptideResults[i]);
				basePeptideMap.put(baseSeq, list);
			}
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void filter() {

		HashMap<String, HashSet<String>> totalPepProMap = new HashMap<String, HashSet<String>>();
		HashSet<String> usedProSet = new HashSet<String>();
		for (PFindProtein pf : this.proteins) {
			String name = pf.getName();
			usedProSet.add(name);

			HashSet<String> pepSet = originalProPepMap.get(name);
			for (String pep : pepSet) {
				if (totalPepProMap.containsKey(pep)) {
					totalPepProMap.get(pep).add(name);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(name);
					totalPepProMap.put(pep, set);
				}
			}
		}

		HashMap<String, ProteinGroup> proGroupMap = getProteinGroups(totalPepProMap, usedProSet);

		this.originalProPepMap = new HashMap<String, HashSet<String>>();
		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			for (String pro : proSet) {
				if (originalProPepMap.containsKey(pro)) {
					originalProPepMap.get(pro).add(pep);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(pep);
					originalProPepMap.put(pro, set);
				}
			}
		}

		this.originalPepProMap = totalPepProMap;

		for (PFindPSM psm : this.psms) {
			String seq = psm.getSequence();
			if (totalPepProMap.containsKey(seq)) {
				HashSet<String> leadProSet = totalPepProMap.get(seq);
				HashSet<String> proSet = new HashSet<String>();
				String[] pros = psm.getProteins();
				for (String pro : pros) {
					if (leadProSet.contains(pro)) {
						proSet.add(pro);
					} else {
						if (proGroupMap.containsKey(pro)) {
							HashSet<String> sameSet = proGroupMap.get(pro).sameSet;
							if (sameSet.contains(pro)) {
								proSet.add(pro);
							}
						}
					}
				}
				psm.setProteins(proSet.toArray(new String[proSet.size()]));
			}
		}

		HashSet<String> tempUniPepSet = new HashSet<String>();
//		tempUniPepSet.addAll(this.uniquePepProMap.keySet());
//		this.uniqueProPepMap = new HashMap<String, HashSet<String>>();
//		this.uniquePepProMap = new HashMap<String, String>();
		this.proteinMap = new HashMap<String, PFindProtein>();

		ArrayList<PFindProtein> proList = new ArrayList<PFindProtein>();
		for (int i = 0; i < proteins.length; i++) {
			String proName = proteins[i].getName();
			if (!proteinMap.containsKey(proName) && proGroupMap.containsKey(proName)) {

				proteins[i].setId(proList.size());
				proteins[i].setSameSet(proGroupMap.get(proName).sameSet);
				proList.add(proteins[i]);

				HashSet<String> pepSet = originalProPepMap.get(proName);
				HashSet<String> uniquePepSet = new HashSet<String>();

				for (String pep : pepSet) {
					if (originalPepProMap.get(pep).size() == 1) {
						uniquePepSet.add(pep);
//						this.uniquePepProMap.put(pep, proName);
					}
				}

//				this.uniqueProPepMap.put(proName, uniquePepSet);
				this.proteinMap.put(proName, proteins[i]);
			}
		}

		for (String upep : tempUniPepSet) {
//			if (!this.uniquePepProMap.containsKey(upep)) {
//				break;
//			}
		}

		this.proteins = proList.toArray(new PFindProtein[proList.size()]);

		this.proteinMap = new HashMap<String, PFindProtein>();
		this.razorProPepMap = new HashMap<String, HashSet<String>>();
		this.razorPepProMap = new HashMap<String, String>();

		for (int i = 0; i < proteins.length; i++) {
			proteins[i].setId(i);
			String proName = proteins[i].getName();
			this.proteinMap.put(proName, proteins[i]);

			HashSet<String> pepSet = originalProPepMap.get(proName);
			HashSet<String> razorPepSet = new HashSet<String>();
			for (String pep : pepSet) {
				if (!this.razorPepProMap.containsKey(pep)) {
					this.razorPepProMap.put(pep, proName);
					razorPepSet.add(pep);
				}
			}
			this.razorProPepMap.put(proName, razorPepSet);
		}
	}

	public PeptideResult[] getPeptideResults() {
		return peptideResults;
	}

	public HashMap<String, PFindProtein> getProteinMap() {
		return proteinMap;
	}

	public HashMap<String, ArrayList<PeptideResult>> getBasePeptideMap() {
		return basePeptideMap;
	}

	private static void combineProteinGroups(HashMap<String, ProteinGroup> pgMap,
			HashMap<String, HashSet<String>> tempProPepMap, HashMap<String, HashSet<String>> tempPepProMap,
			HashMap<String, ProteinGroup> uniquePgMap) {

		// combine protein groups
		for (String pep : tempPepProMap.keySet()) {

			HashSet<String> proSet = tempPepProMap.get(pep);

			String[] pros = proSet.toArray(new String[proSet.size()]);

			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					if (tempProPepMap.get(o1).size() > tempProPepMap.get(o2).size()) {
						return -1;
					}
					if (tempProPepMap.get(o1).size() < tempProPepMap.get(o2).size()) {
						return 1;
					}
					return o1.compareTo(o2);
				}
			});

			for (int i = 0; i < pros.length; i++) {

				if (pgMap.containsKey(pros[i])) {
					ProteinGroup pg = pgMap.get(pros[i]);

					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);

					for (int j = i + 1; j < pros.length; j++) {

						if (pgMap.containsKey(pros[j])) {
							HashSet<String> pepSetJ = tempProPepMap.get(pros[j]);

							HashSet<String> pepAll = new HashSet<String>(pepSetI.size() + pepSetJ.size());
							pepAll.addAll(pepSetI);
							pepAll.addAll(pepSetJ);

							if (pepAll.size() == pepSetI.size()) {
								if (pepSetI.size() == pepSetJ.size()) {
									pg.addSameGroup(pros[j]);
								}
								pgMap.remove(pros[j]);
							}
						}
					}
				}
			}
		}

		if (pgMap.size() == tempProPepMap.size()) {

			String[] pros = pgMap.keySet().toArray(new String[pgMap.size()]);
			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					int size1 = tempProPepMap.get(o1).size();
					int size2 = tempProPepMap.get(o2).size();

					if (size1 < size2) {
						return -1;
					}
					if (size1 > size2) {
						return 1;
					}
					return 0;
				}
			});

			// remove subset proteins
			for (int i = 0; i < pros.length; i++) {
				boolean subset = true;
				HashSet<String> pepSet = tempProPepMap.get(pros[i]);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						subset = false;
					}
				}
				if (subset) {
					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);
					for (String pep : pepSetI) {
						HashSet<String> proSet = tempPepProMap.get(pep);
						proSet.remove(pros[i]);
					}
					tempProPepMap.remove(pros[i]);
					pgMap.remove(pros[i]);
				}
			}

			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}

		} else {

			// remove redundant proteins
			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			// find unique proteins
			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}
		}
	}

	/**
	 * get razor protein groups
	 * 
	 * @param originalProPepMap
	 * @param finalPepProMap
	 * @return
	 */
	private static HashMap<String, ProteinGroup> getProteinGroups(HashMap<String, HashSet<String>> totalPepProMap,
			HashSet<String> razorProSet) {

		HashMap<String, HashSet<String>> tempPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			HashSet<String> tempProSet = new HashSet<String>();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!razorProSet.contains(pro)) {
					proIt.remove();
				} else {
					if (tempProPepMap.containsKey(pro)) {
						tempProPepMap.get(pro).add(pep);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pep);
						tempProPepMap.put(pro, set);
					}
					tempProSet.add(pro);
				}
			}
			tempPepProMap.put(pep, tempProSet);
		}

		HashSet<String> uniqueProSet = new HashSet<String>();
		HashSet<String> uniquePepSet = new HashSet<String>();
		for (String pro : tempProPepMap.keySet()) {
			HashSet<String> pepSet = tempProPepMap.get(pro);
			for (String pep : pepSet) {
				HashSet<String> proSet = tempPepProMap.get(pep);
				if (proSet.size() == 1) {
					uniqueProSet.add(pro);
					uniquePepSet.addAll(pepSet);
					break;
				}
			}
		}

		HashMap<String, ProteinGroup> uniquePgMap = new HashMap<String, ProteinGroup>(uniqueProSet.size());
		HashMap<String, ProteinGroup> pgMap = new HashMap<String, ProteinGroup>(tempProPepMap.size());
		HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
		for (String pro : tempProPepMap.keySet()) {
			if (uniqueProSet.contains(pro)) {
				uniquePgMap.put(pro, new ProteinGroup(pro));
			} else {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				HashSet<String> tempPepSet = new HashSet<String>();
				for (String pep : pepSet) {
					if (!uniquePepSet.contains(pep)) {
						tempPepSet.add(pep);
					}
				}

				if (tempPepSet.size() > 0) {
					tempProPepMap2.put(pro, tempPepSet);
					for (String pep : tempPepSet) {
						if (tempPepProMap2.containsKey(pep)) {
							tempPepProMap2.get(pep).add(pro);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pro);
							tempPepProMap2.put(pep, set);
						}
					}
					pgMap.put(pro, new ProteinGroup(pro));
				}
			}
		}

		combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!uniquePgMap.containsKey(pro)) {
					proIt.remove();
				}
			}
		}

		return uniquePgMap;
	}

	public PFindPSM[] getPsms() {
		return psms;
	}

	public PFindProtein[] getProteins() {
		return proteins;
	}

	public HashMap<String, HashSet<String>> getPepProMap() {
		return originalPepProMap;
	}

	public HashMap<String, HashSet<String>> getProPepMap() {
		return originalProPepMap;
	}

	public HashMap<String, HashSet<String>> getRazorProPepMap() {
		return razorProPepMap;
	}

	public HashMap<String, String> getRazorPepProMap() {
		return razorPepProMap;
	}
	
	public File extractPSMs() {
		File msms = new File(this.resultFolder, "psms.tsv");
		return extractPSMs(msms);
	}

	public File extractPSMs(File msms) {

		if (msms.exists() && msms.length() > 0) {
			return msms;
		}

		OpenPsmTsvWriter writer = new OpenPsmTsvWriter(msms, OpenPsmTsvWriter.pFindFormat);
		PrintWriter idWriter = null;
		try {
			idWriter = new PrintWriter(new File(resultFolder, "id.tsv"));
			idWriter.println("Raw file\tUnique peptide count\tPSM count");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<String, HashSet<Integer>> filePsmCountMap = new HashMap<String, HashSet<Integer>>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < psms.length; i++) {

			String fileName = psms[i].getFileName();
//			fileName = fileName.substring(0, fileName.indexOf("."));
			String sequence = psms[i].getSequence();

			if (filePsmCountMap.containsKey(fileName)) {

				filePsmCountMap.get(fileName).add(psms[i].getScan());
				fileUnipepMap.get(fileName).add(sequence);
			} else {

				HashSet<Integer> scanSet = new HashSet<Integer>();
				scanSet.add(psms[i].getScan());
				filePsmCountMap.put(fileName, scanSet);

				HashSet<String> seqSet = new HashSet<String>();
				seqSet.add(sequence);
				fileUnipepMap.put(fileName, seqSet);
			}

			if (!sequence.contains("X")) {
				writer.addPFindPSM(psms[i]);
			}
		}
		writer.close();

		String[] rawNames = filePsmCountMap.keySet().toArray(new String[filePsmCountMap.size()]);
		Arrays.sort(rawNames);

		for (String rawNameString : rawNames) {
			idWriter.println(rawNameString + "\t" + fileUnipepMap.get(rawNameString).size() + "\t"
					+ filePsmCountMap.get(rawNameString).size());
		}

		idWriter.close();

		return msms;
	}
	
	void exportFinalPeptide(String resultFile) throws IOException {

		File quan_pep_file = new File(resultFile, "QuantifiedPeptides.tsv");
		BufferedReader quanPepReader = new BufferedReader(new FileReader(quan_pep_file));

		File final_pep_txt = new File(resultFile, "final_peptides.tsv");
		PrintWriter pepWriter = new PrintWriter(final_pep_txt);

		String[] quanExpNames = null;
		String[] idenNames = null;

		String line = quanPepReader.readLine();
		String[] title = line.split("\t");
		int seqId = -1;
		ArrayList<String> intenList = new ArrayList<String>();
		ArrayList<String> detectList = new ArrayList<String>();
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				seqId = i;
			} else if (title[i].startsWith("Intensity_")) {
				intenList.add(title[i]);
			} else if (title[i].startsWith("Detection Type_")) {
				detectList.add(title[i]);
			}
		}

		quanExpNames = intenList.toArray(new String[intenList.size()]);
		idenNames = detectList.toArray(new String[detectList.size()]);

		int[] intenId = new int[intenList.size()];
		int[] idenId = new int[detectList.size()];

		Arrays.sort(quanExpNames);

		for (int i = 0; i < quanExpNames.length; i++) {
			for (int j = 0; j < title.length; j++) {
				if (quanExpNames[i].equals(title[j])) {
					intenId[i] = j;
				}
			}
		}

		for (int i = 0; i < idenNames.length; i++) {
			for (int j = 0; j < title.length; j++) {
				if (idenNames[i].equals(title[j])) {
					idenId[i] = j;
				}
			}
		}

		HashMap<String, double[]> pepIntensityMap = new HashMap<String, double[]>();
		HashMap<String, boolean[]> idenTypeMap = new HashMap<String, boolean[]>();

		while ((line = quanPepReader.readLine()) != null) {
			String[] cs = line.split("\t");
			double[] intensity = new double[quanExpNames.length];
			for (int i = 0; i < intensity.length; i++) {
				intensity[i] = Double.parseDouble(cs[intenId[i]]);
			}

			if (pepIntensityMap.containsKey(cs[seqId])) {
				double[] total = pepIntensityMap.get(cs[seqId]);
				for (int i = 0; i < intensity.length; i++) {
					total[i] += intensity[i];
				}
				pepIntensityMap.put(cs[seqId], total);
			} else {
				pepIntensityMap.put(cs[seqId], intensity);
			}

			boolean[] idenType = new boolean[idenNames.length];
			for (int i = 0; i < idenType.length; i++) {
				if (cs[idenId[i]].equals("MSMS")) {
					idenType[i] = true;
				} else {
					idenType[i] = false;
				}
			}
			idenTypeMap.put(cs[seqId], idenType);
		}
		quanPepReader.close();

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Sequence").append("\t");
		titlesb.append("Base sequence").append("\t");
		titlesb.append("Length").append("\t");
		titlesb.append("Missed cleavages").append("\t");
		titlesb.append("Mass").append("\t");
		titlesb.append("Proteins").append("\t");
		titlesb.append("Charges").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Final score").append("\t");
		titlesb.append("Q-value").append("\t");

		for (int i = 0; i < idenNames.length; i++) {
			String name = idenNames[i].substring(idenNames[i].indexOf("_") + 1);
			titlesb.append("Identification type " + name).append("\t");
		}

		titlesb.append("Intensity").append("\t");

		for (int i = 0; i < quanExpNames.length; i++) {
			String name = quanExpNames[i].substring(quanExpNames[i].indexOf("_") + 1);
			titlesb.append("Intensity " + name).append("\t");
		}

		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id").append("\t");
		titlesb.append("Protein group IDs").append("\t");
		titlesb.append("MS/MS Count");

		pepWriter.println(titlesb);

		PeptideResult[] pepresult = this.getPeptideResults();
		HashMap<String, PFindProtein> proteinMap = this.getProteinMap();

		for (int i = 0; i < pepresult.length; i++) {

			String sequence = pepresult[i].getSequence();
			StringBuilder sb = new StringBuilder();
			sb.append(sequence).append("\t");
			sb.append(pepresult[i].getBaseSeq()).append("\t");
			sb.append(pepresult[i].getLength()).append("\t");
			sb.append(pepresult[i].getMiss()).append("\t");
			sb.append(pepresult[i].getMass()).append("\t");

			String[] proteins = pepresult[i].getProteins();
			if (proteins.length > 0) {
				for (String pro : proteins) {
					sb.append(pro).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("\t");

			int[] charge = pepresult[i].getCharge();
			StringBuilder chargesb = new StringBuilder();
			for (int j = 0; j < charge.length; j++) {
				if (charge[j] == 1) {
					chargesb.append(j + 1).append(";");
				}
			}
			if (chargesb.length() > 1) {
				sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
			} else {
				sb.append("2").append("\t");
			}

			sb.append(pepresult[i].getRawScore()).append("\t");
			sb.append(pepresult[i].getFinalScore()).append("\t");
			sb.append(pepresult[i].getQValue()).append("\t");

			if (idenTypeMap.containsKey(sequence)) {
				boolean[] idenType = idenTypeMap.get(sequence);
				for (int j = 0; j < idenType.length; j++) {
					if (idenType[j]) {
						sb.append("By MS/MS").append("\t");
					} else {
						sb.append("\t");
					}
				}
			} else {
				for (int j = 0; j < idenNames.length; j++) {
					sb.append("\t");
				}
			}

			double totalIntensity = 0;

			if (pepIntensityMap.containsKey(sequence)) {
				double[] intensity = pepIntensityMap.get(sequence);
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append((int) totalIntensity).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append((int) intensity[j]).append("\t");
				}
			} else {
				sb.append("0\t");

				for (int j = 0; j < quanExpNames.length; j++) {
					sb.append("0\t");
				}
			}

			if (pepresult[i].isTarget()) {
				sb.append("").append("\t");
			} else {
				sb.append("+").append("\t");
			}

			sb.append("").append("\t");

			sb.append(i).append("\t");

			if (proteins.length > 0) {
				for (String pro : proteins) {
					if (proteinMap.containsKey(pro)) {
						sb.append(proteinMap.get(pro).getId()).append(";");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("\t");

			sb.append(pepresult[i].getMs2Count());

			pepWriter.println(sb);
		}

		pepWriter.close();

		File quan_pro_file = new File(resultFile, "QuantifiedProteins.tsv");
		BufferedReader quanProReader = new BufferedReader(new FileReader(quan_pro_file));

		File final_pro_txt = new File(resultFile, "final_proteins.tsv");
		PrintWriter proWriter = new PrintWriter(final_pro_txt);

		line = quanProReader.readLine();
		title = line.split("\t");
		int proId = -1;
		ArrayList<String> tlist = new ArrayList<String>();
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Protein Groups")) {
				proId = i;
			} else if (title[i].startsWith("Intensity")) {
				tlist.add(title[i]);
			}
		}

		String[] expNames = tlist.toArray(new String[tlist.size()]);

		titlesb = new StringBuilder();
		titlesb.append("Protein IDs").append("\t");
		titlesb.append("Majority protein IDs").append("\t");
		titlesb.append("Peptide counts (all)").append("\t");
		titlesb.append("Peptide counts (razor)").append("\t");
		titlesb.append("Number of proteins").append("\t");
		titlesb.append("Peptides").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Intensity").append("\t");

		int[] fileIds = new int[expNames.length];

		Arrays.sort(expNames);

		for (int i = 0; i < expNames.length; i++) {
			for (int j = 0; j < title.length; j++) {
				if (expNames[i].equals(title[j])) {
					fileIds[i] = j;
				}
			}
		}

		for (int i = 0; i < expNames.length; i++) {
			String name = expNames[i].substring(expNames[i].indexOf("_") + 1);
			titlesb.append("Intensity " + name).append("\t");
		}

		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id").append("\t");
		titlesb.append("Peptide IDs").append("\t");
		titlesb.append("Peptide is razor");

		proWriter.println(titlesb);

		PFindProtein[] proteins = this.getProteins();
		HashMap<String, HashSet<String>> razorProPepMap = this.getRazorProPepMap();
		HashMap<String, HashSet<String>> allProPepMap = this.getProPepMap();
		PeptideResult[] peptideResults = this.getPeptideResults();

		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		for (int i = 0; i < proteins.length; i++) {
			proIntensityMap.put(proteins[i].getName(), null);
		}

		while ((line = quanProReader.readLine()) != null) {

			String[] cs = line.split("\t");

			if (proIntensityMap.containsKey(cs[proId])) {
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < fileIds.length; i++) {
					intensity[i] = Double.parseDouble(cs[fileIds[i]]);
				}
				proIntensityMap.put(cs[proId], intensity);
			}
		}

		quanProReader.close();

		DecimalFormat df = FormatTool.getDFE4();
		for (int i = 0; i < proteins.length; i++) {

			StringBuilder sb = new StringBuilder();
			String pro = proteins[i].getName();
			double[] intensity = proIntensityMap.get(pro);

			if (intensity == null) {
				intensity = new double[expNames.length];
				Arrays.fill(intensity, 0.0);
			}

			HashSet<String> sameSet = proteins[i].getSameSet();
			HashSet<String> subSet = proteins[i].getSubSet();
			HashSet<Integer> pepIdSet = proteins[i].getPepIdSet();

			Integer[] pepIds = pepIdSet.toArray(new Integer[pepIdSet.size()]);
			Arrays.sort(pepIds);
			boolean[] isRazorPep = new boolean[pepIds.length];

			int proCount = 1 + sameSet.size() + subSet.size();
			int[] pepCountAll = new int[proCount];
			int[] pepCountRazor = new int[proCount];

			for (int j = 0; j < pepIds.length; j++) {
				String sequence = peptideResults[pepIds[j]].getBaseSeq();
				pepCountAll[0]++;
				if (razorProPepMap.get(pro).contains(sequence)) {
					pepCountRazor[0]++;
					isRazorPep[j] = true;
				}
			}

			int proNameId = 1;
			StringBuilder sameSb = new StringBuilder();
			for (String p : sameSet) {
				sameSb.append(";").append(p);
				for (int j = 0; j < pepIds.length; j++) {
					String sequence = peptideResults[pepIds[j]].getBaseSeq();
					if (allProPepMap.get(p).contains(sequence)) {
						pepCountAll[proNameId]++;
						if (isRazorPep[j]) {
							pepCountRazor[proNameId]++;
						}
					}
				}
				proNameId++;
			}

			StringBuilder subSb = new StringBuilder();
			for (String p : subSet) {
				subSb.append(";").append(p);
				for (int j = 0; j < pepIds.length; j++) {
					String sequence = peptideResults[pepIds[j]].getBaseSeq();
					if (allProPepMap.get(p).contains(sequence)) {
						pepCountAll[proNameId]++;
						if (isRazorPep[j]) {
							pepCountRazor[proNameId]++;
						}
					}
				}
				proNameId++;
			}

			sb.append(pro).append(sameSb).append(subSb).append("\t");
			sb.append(pro).append(sameSb).append("\t");
			for (int j = 0; j < pepCountAll.length; j++) {
				sb.append(pepCountAll[j]).append(";");
			}
			sb.append("\t");

			for (int j = 0; j < pepCountRazor.length; j++) {
				sb.append(pepCountRazor[j]).append(";");
			}
			sb.append("\t");

			sb.append(proCount).append("\t");
			sb.append(pepIds.length).append("\t");

			sb.append(FormatTool.getDF2().format(proteins[i].getScore())).append("\t");

			double totalIntensity = 0;

			for (int j = 0; j < intensity.length; j++) {
				totalIntensity += intensity[j];
			}

			sb.append(df.format(totalIntensity)).append("\t");
			for (int j = 0; j < intensity.length; j++) {
				sb.append(df.format(intensity[j])).append("\t");
			}

			if (pro.startsWith(PFindResultReader.REV)) {
				sb.append("+").append("\t");
			} else {
				sb.append("\t");
			}
			sb.append("\t");
			sb.append(proteins[i].getId()).append("\t");

			for (int j = 0; j < pepIds.length; j++) {
				sb.append(pepIds[j]).append(";");
			}
			sb.append("\t");

			for (int j = 0; j < isRazorPep.length; j++) {
				sb.append(isRazorPep[j]).append(";");
			}

			// currently no reverse proteins are exported
			if (!pro.startsWith(PFindResultReader.REV)) {
				proWriter.println(sb);
			}
		}

		proWriter.close();

	}
	
	protected static void batchPrintId(String in) throws IOException {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File summaryFile = new File(files[i], "pFind.summary");
				if (summaryFile.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(summaryFile));
					String line = null;
					boolean write = false;
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("ID Rate:")) {
							write = true;
							continue;
						}
						if (line.startsWith("Overall")) {
							write = false;
						}
						if (write) {

							String[] cs = line.split("[\\s+]");

							StringBuilder sb = new StringBuilder();
							sb.append(cs[0].trim()).append("\t");
							sb.append(cs[3].trim()).append("\t");
							sb.append(cs[1].trim()).append("\t");
							sb.append(cs[5].trim()).append("\t");

							System.out.println(sb);

							continue;
						}
					}
					reader.close();
				}
			} else {
				if (files[i].getName().equals("pFind.summary")) {
					BufferedReader reader = new BufferedReader(new FileReader(files[i]));
					String line = null;
					boolean write = false;
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("ID Rate:")) {
							write = true;
							continue;
						}
						if (line.startsWith("Overall")) {
							write = false;
						}
						if (write) {

							String[] cs = line.split("[\\s+]");

							StringBuilder sb = new StringBuilder();
							sb.append(cs[0].trim()).append("\t");
							sb.append(cs[3].trim()).append("\t");
							sb.append(cs[1].trim()).append("\t");
							sb.append(cs[5].trim()).append("\t");

							System.out.println(sb);

							continue;
						}
					}
					reader.close();
				}
			}
		}
	}
	
	protected static void batchPrintHapId(String in) throws IOException {
		File[] files = (new File(in)).listFiles();
		int iden = 0;
		int total = 0;
		System.out.println("Raw file\tIdentifed MS/MS\tTotal MS/MS\tIdentification rate");
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File hapFile = new File(files[i], "hap");
				if (hapFile.exists() && hapFile.isDirectory()) {
					File summaryFile = new File(hapFile, "pFind.summary");
					if (summaryFile.exists()) {
						BufferedReader reader = new BufferedReader(new FileReader(summaryFile));
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith("ID Rate:")) {
								String infoLine = reader.readLine();
								String[] cs = infoLine.split("[=/\t]");

								int idenI = Integer.parseInt(cs[1].trim());
								iden += idenI;

								int totalI = Integer.parseInt(cs[2].trim());
								total += totalI;

								System.out.println(cs[0] + "\t" + idenI + "\t" + totalI + "\t" + cs[3].trim());
							}
						}
						reader.close();
					}
				}
			}
		}

		if (total == 0) {
			System.out.println("Total\t0\t0\t0.00%");
		} else {
			System.out.println("Total\t" + iden + "\t" + total + "\t"
					+ FormatTool.getDFP2().format((double) iden / (double) total));
		}
	}
	
	public static void main(String[] args) {
		PFindResultReader reader = new PFindResultReader(new File("Z:\\Kai\\Raw_files\\PXD008738\\8mix\\MetaLab_dda\\mag_result\\160822_8Mix_DDA_6ul_01"), 
				new File("Z:\\Kai\\Raw_files\\PXD008738\\8mix\\MetaLab_dda\\spectra"));
		try {
			reader.read();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PFindPSM[] psms = reader.getPsms();
		System.out.println(psms.length);
		
		reader.extractPSMs();
	}
}
