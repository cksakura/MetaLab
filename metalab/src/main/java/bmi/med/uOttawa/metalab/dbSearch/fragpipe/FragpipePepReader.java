package bmi.med.uOttawa.metalab.dbSearch.fragpipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class FragpipePepReader extends AbstractMetaPeptideReader {

	protected BufferedReader reader;
	protected int sequenceId = -1;
	protected int modSeqId = -1;
	protected int proteinId = -1;
//	protected int subProGroupId = -1;
	protected int chargeId = -1;
	protected int lengthId = -1;

	protected String[] fileNames;
	protected String[] title;
	protected String line;

	protected int[] intensityId;
	protected int[] spCountId;
	protected int[] idenTypeId;

	private HashMap<String, Integer> missMap;
	private HashMap<String, Double> massMap;
	private HashMap<String, Double> scoreMap;
	private HashMap<String, ArrayList<Double>> pepProbMap;
	private HashMap<String, double[]> pepProbListMap;

	private static Logger LOGGER = LogManager.getLogger(FragpipePepReader.class);

	public FragpipePepReader(String in) {
		this(new File(in));
		// TODO Auto-generated constructor stub
	}

	public FragpipePepReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + in, e);
		}
		this.parseTitle();
	}

	public FragpipePepReader(String in, String[] fileNames) {
		this(new File(in), fileNames);
		// TODO Auto-generated constructor stub
	}

	public FragpipePepReader(File in, String[] fileNames) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + in, e);
		}
		this.fileNames = fileNames;

		this.parseTitle();
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		String line = null;
		try {
			line = reader.readLine();

			this.title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Peptide Sequence")) {
					sequenceId = i;
				} else if (title[i].equals("Modified Sequence")) {
					modSeqId = i;
				} else if (title[i].equals("Protein")) {
					proteinId = i;
				} else if (title[i].equals("Mapped Proteins")) {
//					subProGroupId = i;
				} else if (title[i].equals("Charges")) {
					chargeId = i;
				} else if (title[i].equals("Peptide Length")) {
					lengthId = i;
				}
			}

			if (fileNames == null) {
				ArrayList<Integer> intenIdList = new ArrayList<Integer>();
				ArrayList<Integer> spCountIdList = new ArrayList<Integer>();
				ArrayList<Integer> idenTypeIdList = new ArrayList<Integer>();

				for (int i = 0; i < title.length; i++) {
					if (title[i].endsWith(" Intensity")) {
						if (!title[i].endsWith(" MaxLFQ Intensity")) {
							intenIdList.add(i);
						}
					} else if (title[i].endsWith(" Spectral Count")) {
						spCountIdList.add(i);
					} else if (title[i].endsWith(" Match Type")) {
						idenTypeIdList.add(i);
					}
				}

				this.fileNames = new String[intenIdList.size()];
				this.intensityId = new int[intenIdList.size()];
				this.spCountId = new int[intenIdList.size()];
				this.idenTypeId = new int[intenIdList.size()];
				for (int i = 0; i < fileNames.length; i++) {
					intensityId[i] = intenIdList.get(i);
					fileNames[i] = title[intensityId[i]].substring(0,
							title[intensityId[i]].length() - " Intensity".length());

					spCountId[i] = spCountIdList.get(i);
					idenTypeId[i] = idenTypeIdList.get(i);
				}
			} else {
				this.intensityId = new int[fileNames.length];
				this.spCountId = new int[fileNames.length];
				this.idenTypeId = new int[fileNames.length];

				int findCount = 0;

				for (int i = 0; i < title.length; i++) {
					if (title[i].endsWith(" Intensity")) {
						for (int j = 0; j < fileNames.length; j++) {
							if (title[i].equals(fileNames[j] + " Intensity")) {
								intensityId[j] = i;
								findCount++;
							}
						}
					} else if (title[i].endsWith(" Spectral Count")) {
						for (int j = 0; j < fileNames.length; j++) {
							if (title[i].equals(fileNames[j] + " Spectral Count")) {
								spCountId[j] = i;
								findCount++;
							}
						}
					} else if (title[i].endsWith(" Match Type")) {
						for (int j = 0; j < fileNames.length; j++) {
							if (title[i].equals(fileNames[j] + " Match Type")) {
								idenTypeId[j] = i;
								findCount++;
							}
						}
					}
				}

				if (findCount != (this.intensityId.length + this.spCountId.length + this.idenTypeId.length)) {
					ArrayList<Integer> intenIdList = new ArrayList<Integer>();
					ArrayList<Integer> spCountIdList = new ArrayList<Integer>();
					ArrayList<Integer> idenTypeIdList = new ArrayList<Integer>();

					for (int i = 0; i < title.length; i++) {
						if (title[i].endsWith(" Intensity")) {
							if (!title[i].endsWith(" MaxLFQ Intensity")) {
								intenIdList.add(i);
							}
						} else if (title[i].endsWith(" Spectral Count")) {
							spCountIdList.add(i);
						} else if (title[i].endsWith(" Match Type")) {
							idenTypeIdList.add(i);
						}
					}

					this.fileNames = new String[intenIdList.size()];
					this.intensityId = new int[intenIdList.size()];
					this.spCountId = new int[intenIdList.size()];
					this.idenTypeId = new int[intenIdList.size()];
					for (int i = 0; i < fileNames.length; i++) {
						intensityId[i] = intenIdList.get(i);
						fileNames[i] = title[intensityId[i]].substring(0,
								title[intensityId[i]].length() - " Intensity".length());

						spCountId[i] = spCountIdList.get(i);
						idenTypeId[i] = idenTypeIdList.get(i);
					}
				}
			}

			this.missMap = new HashMap<String, Integer>();
			this.scoreMap = new HashMap<String, Double>();
			this.massMap = new HashMap<String, Double>();
			this.pepProbMap = new HashMap<String, ArrayList<Double>>();
			this.pepProbListMap = new HashMap<String, double[]>();

			File parentFile = this.getFile().getParentFile();
			for (int i = 0; i < fileNames.length; i++) {
				File filei = new File(parentFile, fileNames[i]);
				if (filei.exists() && filei.isDirectory()) {
					File peptideFile = new File(filei, "psm.tsv");

					if (peptideFile.exists() && peptideFile.length() > 0) {
						try (BufferedReader readeri = new BufferedReader(new FileReader(peptideFile))) {
							String pline = readeri.readLine();
							String[] title = pline.split("\t");
							int seqId = -1;
							int modId = -1;
							int missId = -1;
							int scoreId = -1;
							int probId = -1;
							int pepMassId = -1;

							for (int j = 0; j < title.length; j++) {
								if (title[j].equals("Peptide")) {
									seqId = j;
								} else if (title[j].equals("Assigned Modifications")) {
									modId = j;
								} else if (title[j].equals("Hyperscore")) {
									scoreId = j;
								} else if (title[j].equals("PeptideProphet Probability")) {
									probId = j;
								} else if (title[j].equals("Calculated Peptide Mass")) {
									pepMassId = j;
								} else if (title[j].equals("Number of Missed Cleavages")) {
									missId = j;
								}
							}

							while ((line = readeri.readLine()) != null) {
								String[] cs = line.split("\t");
								String sequence = cs[seqId];
								if (modId > -1 && cs[modId].length() > 0) {
									String[] mods = cs[modId].split(",");
									HashMap<Integer, String> modsMap = new HashMap<Integer, String>();
									for (int j = 0; j < mods.length; j++) {
										int loc = mods[j].lastIndexOf("(");

										String modMass = mods[j].substring(loc + 1, mods[j].lastIndexOf(")"));
										if (mods[j].startsWith("N-term") || mods[j].startsWith("n-term")) {
											modsMap.put(0, modMass);
										} else if (mods[j].startsWith("C-term") || mods[j].startsWith("c-term")) {
											modsMap.put(sequence.length() + 1, modMass);
										} else {
											modsMap.put(Integer.parseInt(mods[j].substring(0, loc - 1)), modMass);
										}
									}

									StringBuilder sb = new StringBuilder();
									if (modsMap.containsKey(0)) {
										sb.append("n[").append(modsMap.get(0)).append("]");
									}
									for (int j = 0; j < sequence.length(); j++) {
										sb.append(sequence.charAt(j));
										if (modsMap.containsKey(j + 1)) {
											sb.append("[").append(modsMap.get(j + 1)).append("]");
										}
									}
									if (modsMap.containsKey(sequence.length() + 1)) {
										sb.append("c[").append(modsMap.get(0)).append("]");
									}

									sequence = sb.toString();
								}

								double score = scoreId > -1 ? Double.parseDouble(cs[scoreId]) : 0.0;
								double prob = probId > -1 ? Double.parseDouble(cs[probId]) : 0.0;
								double pepMass = pepMassId > -1 ? Double.parseDouble(cs[pepMassId]) : 0.0;
								int miss = missId > -1 ? Integer.parseInt(cs[missId]) : 0;
								ArrayList<Double> qList;
								double[] expList;
								if (pepProbMap.containsKey(sequence)) {
									qList = pepProbMap.get(sequence);
									expList = pepProbListMap.get(sequence);
								} else {
									qList = new ArrayList<Double>();
									pepProbMap.put(sequence, qList);
									expList = new double[fileNames.length];
									Arrays.fill(expList, 1.0);
									pepProbListMap.put(sequence, expList);
								}
								qList.add(prob);
								if (prob < expList[i]) {
									expList[i] = prob;
								}

								this.scoreMap.put(sequence, score);
								this.massMap.put(sequence, pepMass);
								this.missMap.put(sequence, miss);
							}
							readeri.close();
						} catch (Exception e) {
							LOGGER.error("Error in reading Fragpipe search result file " + peptideFile, e);
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + super.getFile(), e);
		}
	}

	protected FragpipePeptide parse() {
		String[] cs = line.split("\t");
		String seqString = cs[sequenceId];
		String modSeq = modSeqId > -1 ? cs[modSeqId] : seqString;

		String[] proteins = new String[] { cs[proteinId] };
		int length = Integer.parseInt(cs[lengthId]);

		String[] chargeSs = cs[chargeId].split(",");
		int[] charge = new int[chargeSs.length];
		for (int i = 0; i < charge.length; i++) {
			charge[i] = Integer.parseInt(chargeSs[i]);
		}

		double[] intensity = new double[intensityId.length];
		for (int i = 0; i < intensity.length; i++) {
			int id = intensityId[i];
			if (id > -1 && id < cs.length) {
				intensity[i] = Double.parseDouble(cs[id]);
			}
		}

		int[] spCount = new int[spCountId.length];
		for (int i = 0; i < spCount.length; i++) {
			int id = spCountId[i];
			if (id > -1 && id < cs.length) {
				spCount[i] = Integer.parseInt(cs[id]);
			}
		}

		int[] idenType = new int[idenTypeId.length];
		for (int i = 0; i < idenType.length; i++) {
			int id = idenTypeId[i];
			if (id > -1 && id < cs.length) {
				for (int j = 0; j < MetaPeptide.idenTypeStrings.length; j++) {
					if (cs[id].equals(MetaPeptide.idenTypeStrings[j])) {
						idenType[i] = j;
					}
				}
			} else {
				if (spCount[i] == 0) {
					idenType[i] = 2;
				} else {
					idenType[i] = 0;
				}
			}
		}

		double PEP = 1;
		if (this.pepProbMap.containsKey(modSeq)) {
			ArrayList<Double> list = this.pepProbMap.get(modSeq);
			for (int i = 0; i < list.size(); i++) {
				PEP = PEP * (1.0 - list.get(i));
			}
		}
		double score = 0;
		if (this.scoreMap.containsKey(modSeq)) {
			score = scoreMap.get(modSeq);
		}

		double pepMass = 0;
		if (this.massMap.containsKey(modSeq)) {
			pepMass = massMap.get(modSeq);
		}

		int miss = 0;
		if (this.missMap.containsKey(modSeq)) {
			miss = this.missMap.get(modSeq);
		}
		double[] pepList = new double[intensityId.length];
		if (this.pepProbListMap.containsKey(modSeq)) {
			pepList = this.pepProbListMap.get(modSeq);
		} else {
			Arrays.fill(pepList, 1.0);
		}
		FragpipePeptide peptide = new FragpipePeptide(seqString, modSeq, length, charge, miss, pepMass, score, PEP,
				proteins, intensity, spCount, idenType);
		peptide.setIndividualPEP(pepList);

		return peptide;
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return MetaConstants.labelFree;
	}

	@Override
	public FragpipePeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		ArrayList<FragpipePeptide> list = new ArrayList<FragpipePeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				FragpipePeptide pep = parse();
				if (pep != null) {
					list.add(pep);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		FragpipePeptide[] peps = list.toArray(new FragpipePeptide[list.size()]);
		return peps;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		Object[] titleObjs = new Object[3 + fileNames.length + fileNames.length];
		titleObjs[0] = this.title[sequenceId];
		titleObjs[1] = this.title[proteinId];
		titleObjs[2] = "Intensity";
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + 3] = "Intensity " + fileNames[i];
		}
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + fileNames.length + 3] = "Spectral Count" + fileNames[i];
		}
		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		String[] intensityTitles = new String[this.fileNames.length];
		for (int i = 0; i < intensityTitles.length; i++) {
			intensityTitles[i] = "Intensity " + fileNames[i];
		}
		return intensityTitles;
	}
}
