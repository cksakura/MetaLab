package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPepReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPeptide;

public class DiannPrMatrixReader extends FlashLfqQuanPepReader {

	private static int chargeId = -1;
	private boolean isCsv;
	private static Logger LOGGER = LogManager.getLogger(DiannPrMatrixReader.class);
	
	private HashMap<String, Integer> runIdMap;

	public DiannPrMatrixReader(String quanPep) {
		this(new File(quanPep));
		// TODO Auto-generated constructor stub
	}

	public DiannPrMatrixReader(File quanPep) {
		super(quanPep);
		// TODO Auto-generated constructor stub
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> intensityTitleList = new ArrayList<String>();

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}

		String fileName = this.getFile().getName();
		if (fileName.endsWith("tsv") || fileName.endsWith("TSV")) {
			this.title = line.split("\t");
			this.isCsv = false;
		} else if (fileName.endsWith("csv") || fileName.endsWith("CSV")) {
			this.title = line.split(",");
			this.isCsv = true;
		} else {

		}
		
		this.runIdMap = new HashMap<String, Integer>();

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Stripped.Sequence") || title[i].equals("StrippedSequence")) {
				sequenceId = i;
			} else if (title[i].equals("Modified.Sequence") || title[i].equals("ModifiedSequence")) {
				modSeqId = i;
			} else if (title[i].equals("Precursor.Charge") || title[i].equals("PrecursorCharge")) {
				chargeId = i;
			} else if (title[i].equals("Protein.Group")) {
				proteinId = i;
			} else if (title[i].endsWith(".d") || title[i].endsWith(".dia") || title[i].endsWith(".raw")) {
				int id = title[i].lastIndexOf("\\");
				if (id >= 0 && id < title[i].length() - 1) {
					String fileNameI = null;
					if (title[i].endsWith(".d")) {
						fileNameI = title[i].substring(id + 1, title[i].length() - 2);
					} else if (title[i].endsWith(".d.dia")) {
						fileNameI = title[i].substring(id + 1, title[i].length() - 6);
					} else if (title[i].endsWith(".raw")) {
						fileNameI = title[i].substring(id + 1, title[i].length() - 4);
					} else if (title[i].endsWith(".raw.dia")) {
						fileNameI = title[i].substring(id + 1, title[i].length() - 8);
					} else if (title[i].endsWith(".dia")) {
						fileNameI = title[i].substring(id + 1, title[i].length() - 4);
					} else {
						fileNameI = title[i].substring(id + 1);
					}

					intensityTitleIdMap.put(fileNameI, i);
					intensityTitleList.add(fileNameI);
					
					String run = title[i].substring(id + 1, title[i].lastIndexOf("."));
					runIdMap.put(run, runIdMap.size());
					
				} else {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
				}
			}
		}

		this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
	}

	protected FlashLfqQuanPeptide parse() {
		if (line.trim().length() == 0) {
			return null;
		}

		String[] cs;
		if (this.isCsv) {
			cs = line.split(",");
		} else {
			cs = line.split("\t");
		}

		String[] ps = proteinId == -1 ? new String[] {} : cs[proteinId].split(";");
		String[] proteins = new String[ps.length];
		for (int i = 0; i < proteins.length; i++) {
			proteins[i] = ps[i];
		}

		int[] idenType = new int[intensityTitles.length];
		double[] intensity = new double[intensityTitles.length];

		for (int i = 0; i < intensity.length; i++) {
			int id = this.intensityTitleIdMap.get(intensityTitles[i]);
			if (id > -1 && id < cs.length) {
				if (cs[id].trim().length() > 0) {
					intensity[i] = Double.parseDouble(cs[id]);
				} else {
					intensity[i] = 0;
				}
				idenType[i] = 0;
			} else {
				intensity[i] = 0;
			}
		}

		int charge = chargeId > -1 ? Integer.parseInt(cs[chargeId]) : 1;
		FlashLfqQuanPeptide peptide = new FlashLfqQuanPeptide(cs[sequenceId], cs[modSeqId], proteins, intensity,
				idenType, false);
		peptide.setCharges(new int[] { charge });

		return peptide;
	}

	@Override
	public FlashLfqQuanPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		HashMap<String, FlashLfqQuanPeptide> map = new HashMap<String, FlashLfqQuanPeptide>();
		ArrayList<FlashLfqQuanPeptide> list = new ArrayList<FlashLfqQuanPeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				FlashLfqQuanPeptide pep = parse();
				if (pep != null) {

					String modSeq = pep.getModSeq();
					if (map.containsKey(modSeq)) {
						FlashLfqQuanPeptide pep0 = map.get(modSeq);
						int[] charge0 = pep0.getCharges();
						int[] charge = pep.getCharges();

						int[] charge1 = new int[charge0.length + 1];
						System.arraycopy(charge0, 0, charge1, 0, charge0.length);
						charge1[charge0.length] = charge[0];
						pep0.setCharges(charge1);

						double[] intensity0 = pep0.getIntensity();
						double[] intensity = pep.getIntensity();
						for (int i = 0; i < intensity0.length; i++) {
							intensity0[i] += intensity[i];
						}

					} else {
						list.add(pep);
						map.put(modSeq, pep);
					}
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		FlashLfqQuanPeptide[] peps = list.toArray(new FlashLfqQuanPeptide[list.size()]);
		return peps;
	}

	public FlashLfqQuanPeptide[] getMetaPeptides(HashSet<String> usedPepSet) {
		// TODO Auto-generated method stub
		HashMap<String, FlashLfqQuanPeptide> map = new HashMap<String, FlashLfqQuanPeptide>();
		ArrayList<FlashLfqQuanPeptide> list = new ArrayList<FlashLfqQuanPeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				FlashLfqQuanPeptide pep = parse();
				if (pep != null) {
					String stripeSeq = pep.getSequence();
					String modSeq = pep.getModSeq();
					if (usedPepSet.contains(stripeSeq)) {
						if (map.containsKey(modSeq)) {
							FlashLfqQuanPeptide pep0 = map.get(modSeq);
							int[] charge0 = pep0.getCharges();
							int[] charge = pep.getCharges();

							int[] charge1 = new int[charge0.length + 1];
							System.arraycopy(charge0, 0, charge1, 0, charge0.length);
							charge1[charge0.length] = charge[0];
							pep0.setCharges(charge1);

							double[] intensity0 = pep0.getIntensity();
							double[] intensity = pep.getIntensity();
							for (int i = 0; i < intensity0.length; i++) {
								intensity0[i] += intensity[i];
							}

						} else {
							list.add(pep);
							map.put(modSeq, pep);
						}
					}
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		FlashLfqQuanPeptide[] peps = list.toArray(new FlashLfqQuanPeptide[list.size()]);		
		return peps;
	}
	
	public FlashLfqQuanPeptide[] getMetaPeptides(DiaNNPrecursor[] diaNNPrecursors) {
		// TODO Auto-generated method stub

		HashMap<String, ArrayList<DiaNNPrecursor>> ppMap = new HashMap<String, ArrayList<DiaNNPrecursor>>();
		for (int i = 0; i < diaNNPrecursors.length; i++) {
			String modseq = diaNNPrecursors[i].getModSeqString();
			if (ppMap.containsKey(modseq)) {
				ppMap.get(modseq).add(diaNNPrecursors[i]);
			} else {
				ArrayList<DiaNNPrecursor> list = new ArrayList<DiaNNPrecursor>();
				list.add(diaNNPrecursors[i]);
				ppMap.put(modseq, list);
			}
		}

		HashMap<String, FlashLfqQuanPeptide> map = new HashMap<String, FlashLfqQuanPeptide>();
		ArrayList<FlashLfqQuanPeptide> list = new ArrayList<FlashLfqQuanPeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				FlashLfqQuanPeptide peptide = parse();
				if (peptide != null) {
					String modSeq = peptide.getModSeq();
					if (ppMap.containsKey(modSeq)) {
						if (map.containsKey(modSeq)) {
							FlashLfqQuanPeptide pep0 = map.get(modSeq);
							int[] charge0 = pep0.getCharges();
							int[] charge = peptide.getCharges();

							int[] charge1 = new int[charge0.length + 1];
							System.arraycopy(charge0, 0, charge1, 0, charge0.length);
							charge1[charge0.length] = charge[0];
							pep0.setCharges(charge1);

							double[] intensity0 = pep0.getIntensity();
							double[] intensity = peptide.getIntensity();
							for (int i = 0; i < intensity0.length; i++) {
								intensity0[i] += intensity[i];
							}

							int[] spCount = pep0.getMs2Counts();
							int[] idenType = pep0.getIdenType();
							for (int i = 0; i < spCount.length; i++) {
								if (spCount[i] > 0) {
									idenType[i] = 0;
								} else {
									if (intensity0[i] > 0) {
										idenType[i] = 1;
									} else {
										idenType[i] = 2;
									}
								}
							}
							peptide.setIdenType(idenType);

						} else {
							list.add(peptide);
							map.put(modSeq, peptide);

							ArrayList<DiaNNPrecursor> pplist = ppMap.get(modSeq);

							int[] spCount = new int[intensityTitleIdMap.size()];
							int[] idenType = new int[intensityTitleIdMap.size()];
							double[] indiPEP = new double[intensityTitleIdMap.size()];
							Arrays.fill(indiPEP, 1.0);
							double[] intensity = peptide.getIntensity();

							double PEP = 1;
							double score = 0;
							int miss = 0;
							double mass = 0;
							for (DiaNNPrecursor pp : pplist) {
								String runString = pp.getRunString();
								if (runIdMap.containsKey(runString)) {
									spCount[runIdMap.get(runString)]++;

									if (pp.getPEP() < indiPEP[runIdMap.get(runString)]) {
										indiPEP[runIdMap.get(runString)] = pp.getPEP();
									}
								}

								if (pp.getPEP() < PEP) {
									PEP = pp.getPEP();
								}

								if (pp.getCscore() > score) {
									score = pp.getCscore();
								}

								miss = pp.getMissCleavage();
								mass = pp.getMass();
							}

							peptide.setMissCleave(miss);
							peptide.setMass(mass);
							peptide.setPEP(PEP);
							peptide.setScore(score);

							peptide.setMs2Counts(spCount);
							peptide.setIndividualPEP(indiPEP);
							
							for (int i = 0; i < spCount.length; i++) {
								if (spCount[i] > 0) {
									idenType[i] = 0;
								} else {
									if (intensity[i] > 0) {
										idenType[i] = 1;
									} else {
										idenType[i] = 2;
									}
								}
							}
							peptide.setIdenType(idenType);
						}
					}
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		FlashLfqQuanPeptide[] peps = list.toArray(new FlashLfqQuanPeptide[list.size()]);
		return peps;
	}
	
	public static void main(String[] args) {
		DiannPrMatrixReader reader = new DiannPrMatrixReader("Z:\\Kai\\Raw_files\\single_strain_dia\\8482\\MetaLab_dia\\mag_result"
				+ "\\combined.pr_matrix.tsv");
		FlashLfqQuanPeptide[] peps = reader.getMetaPeptides();
		System.out.println(peps.length);
	}

}
