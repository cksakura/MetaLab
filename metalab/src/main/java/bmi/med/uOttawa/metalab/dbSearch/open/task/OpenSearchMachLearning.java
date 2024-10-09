/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.aminoacid.AminoacidFragment;
import bmi.med.uOttawa.metalab.core.ml.SemiSuperClassifier;
import bmi.med.uOttawa.metalab.core.ml.UnsuperCluster;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerPsm;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;
import bmi.med.uOttawa.metalab.spectra.Ion;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * @author Kai Cheng
 *
 */
public class OpenSearchMachLearning {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenSearchMachLearning.class);

	private double ms2Tolerance;
	private static double ms2ToleranceFTMS = 0.02;
	private static double ms2ToleranceITMS = 0.8;
	private static double[] neutralLossRange = new double[] { 50, 10 };

	private int semiSupervised = 0;
	private int unsupervised = 1;

	/**
	 * expect value
	 */
	private int scoreId = 13;

	private HashMap<String, HashMap<Integer, MSFraggerPsm>> psmmap;
	private HashMap<String, Integer> ms2CountMap;
	private OpenMod[] openmods;
	private int learnMethod;
	private AminoacidFragment aaf;

	private ArrayList<Attribute> attributeList;

	public OpenSearchMachLearning(MSFraggerPsm[] psms, OpenMod[] openmods, int learnMethod, String ms2ScanMode) {
		this.learnMethod = learnMethod;
		this.openmods = openmods;
		if (ms2ScanMode.equals(MetaConstants.FTMS)) {
			ms2Tolerance = ms2ToleranceFTMS;
		} else {
			ms2Tolerance = ms2ToleranceITMS;
		}
		this.initial(psms);
	}

	private void initial(MSFraggerPsm[] psms) {
		this.aaf = new AminoacidFragment();
		this.psmmap = new HashMap<String, HashMap<Integer, MSFraggerPsm>>();
		this.ms2CountMap = new HashMap<String, Integer>();

		for (MSFraggerPsm psm : psms) {
			String basename = psm.getFileName();
			int scannum = psm.getScan();
			if (this.psmmap.containsKey(basename)) {
				this.psmmap.get(basename).put(scannum, psm);
			} else {
				HashMap<Integer, MSFraggerPsm> map = new HashMap<Integer, MSFraggerPsm>();
				map.put(scannum, psm);
				this.psmmap.put(basename, map);
			}
		}

		this.createAttributeList();
	}

	private void createAttributeList() {
		if (learnMethod == semiSupervised) {
			attributeList = new ArrayList<Attribute>();

			attributeList.add(new Attribute("charge"));
			attributeList.add(new Attribute("pepMass"));
			attributeList.add(new Attribute("pepLen"));

			attributeList.add(new Attribute("premr"));
			attributeList.add(new Attribute("massDiff"));
			attributeList.add(new Attribute("massDiffPPM"));

			attributeList.add(new Attribute("miss"));
			attributeList.add(new Attribute("num_tol_term"));
			attributeList.add(new Attribute("tot_num_ions"));
			attributeList.add(new Attribute("num_matched_ions"));

			attributeList.add(new Attribute("hyperscore"));
			attributeList.add(new Attribute("nextscore"));
			attributeList.add(new Attribute("deltascore"));
			attributeList.add(new Attribute("_logExpect"));

			attributeList.add(new Attribute("bintensity"));
			attributeList.add(new Attribute("yintensity"));
			attributeList.add(new Attribute("brankscore"));
			attributeList.add(new Attribute("yrankscore"));
			attributeList.add(new Attribute("bcount"));
			attributeList.add(new Attribute("ycount"));
			attributeList.add(new Attribute("fragDeltaMass"));

			attributeList.add(new Attribute("modMass"));
			attributeList.add(new Attribute("bmodIntensity"));
			attributeList.add(new Attribute("ymodIntensity"));
			attributeList.add(new Attribute("bmodrankscore"));
			attributeList.add(new Attribute("ymodrankscore"));
			attributeList.add(new Attribute("fragModDeltaMass"));

			attributeList.add(new Attribute("gaussian_a"));
			attributeList.add(new Attribute("gaussian_b"));
			attributeList.add(new Attribute("gaussian_r2"));

			attributeList.add(new Attribute("sameModCount"));
			attributeList.add(new Attribute("samePepCount"));
			attributeList.add(new Attribute("sameProCount"));

			ArrayList<String> classList = new ArrayList<String>();
			classList.add("P");
			classList.add("N");
			Attribute classAattr = new Attribute("class", classList);
			attributeList.add(classAattr);

		} else if (learnMethod == unsupervised) {

			attributeList = new ArrayList<Attribute>();

			attributeList.add(new Attribute("charge"));
			attributeList.add(new Attribute("pepMass"));
			attributeList.add(new Attribute("pepLen"));

			attributeList.add(new Attribute("premr"));
			attributeList.add(new Attribute("massDiff"));
			attributeList.add(new Attribute("massDiffPPM"));

			attributeList.add(new Attribute("miss"));
			attributeList.add(new Attribute("num_tol_term"));
			attributeList.add(new Attribute("tot_num_ions"));
			attributeList.add(new Attribute("num_matched_ions"));

			attributeList.add(new Attribute("hyperscore"));
			attributeList.add(new Attribute("nextscore"));
			attributeList.add(new Attribute("deltascore"));
			attributeList.add(new Attribute("_logExpect"));

			attributeList.add(new Attribute("bintensity"));
			attributeList.add(new Attribute("yintensity"));
			attributeList.add(new Attribute("brankscore"));
			attributeList.add(new Attribute("yrankscore"));
			attributeList.add(new Attribute("bcount"));
			attributeList.add(new Attribute("ycount"));
			attributeList.add(new Attribute("fragDeltaMass"));

			attributeList.add(new Attribute("modMass"));
			attributeList.add(new Attribute("bmodIntensity"));
			attributeList.add(new Attribute("ymodIntensity"));
			attributeList.add(new Attribute("bmodrankscore"));
			attributeList.add(new Attribute("ymodrankscore"));
			attributeList.add(new Attribute("fragModDeltaMass"));

			attributeList.add(new Attribute("gaussian_a"));
			attributeList.add(new Attribute("gaussian_b"));
			attributeList.add(new Attribute("gaussian_r2"));

			attributeList.add(new Attribute("sameModCount"));
			attributeList.add(new Attribute("samePepCount"));
			attributeList.add(new Attribute("sameProCount"));
		}
	}

	public HashMap<String, Integer> getMs2CountMap() {
		return ms2CountMap;
	}

	public OpenPSM[] assignScores(File[] mgfs) {

		Instances instances = new Instances("PSM Instances", attributeList, 20000 * mgfs.length);

		ArrayList<OpenPSM> noModPsmList = new ArrayList<OpenPSM>();

		ArrayList<OpenPSM> psmlist = new ArrayList<OpenPSM>();
		ArrayList<Double> scorelist = new ArrayList<Double>(10000);

		HashMap<Integer, Integer> modcountmap = new HashMap<Integer, Integer>();
		HashMap<String, Integer> pepcountmap = new HashMap<String, Integer>();
		HashMap<String, Integer> procountmap = new HashMap<String, Integer>();

		for (File mgffile : mgfs) {
			String fileName = mgffile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			int ms2Count = 0;

			if (this.psmmap.containsKey(fileName)) {
				HashMap<Integer, MSFraggerPsm> map = this.psmmap.get(fileName);

				MgfReader mr = new MgfReader(mgffile);
				Spectrum sp = null;
				try {
					while ((sp = mr.getNextSpectrum()) != null) {
						int scan = sp.getScannum();
						ms2Count++;

						if (map.containsKey(scan)) {
							MSFraggerPsm psm = map.get(scan);
							OpenPSM openPsm = getOpenPSM(psm, sp);
							int modid = openPsm.getOpenModId();
							double modMass = openPsm.getOpenModmass();

							if (modMass == 0) {
								noModPsmList.add(openPsm);
							} else {
								scorelist.add(-Math.log(psm.getExpect()));
								psmlist.add(openPsm);

								if (modcountmap.containsKey(modid)) {
									modcountmap.put(modid, modcountmap.get(modid) + 1);
								} else {
									modcountmap.put(modid, 1);
								}
							}

							String pep = openPsm.getSequence();
							if (pepcountmap.containsKey(pep)) {
								pepcountmap.put(pep, pepcountmap.get(pep) + 1);
							} else {
								pepcountmap.put(pep, 1);
							}

							String pro = openPsm.getProtein();
							if (procountmap.containsKey(pro)) {
								procountmap.put(pro, procountmap.get(pro) + 1);
							} else {
								procountmap.put(pro, 1);
							}
						}
					}
					mr.close();

				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in reading spectra information from " + mgffile, e);
				}
			}

			this.ms2CountMap.put(fileName, ms2Count);
		}

		int[] modids = new int[psmlist.size()];
		String[] peps = new String[psmlist.size()];
		String[] pros = new String[psmlist.size()];

		int[] totalPepCount = new int[psmlist.size()];
		int[] totalProCount = new int[psmlist.size()];

		for (int i = 0; i < psmlist.size(); i++) {

			OpenPSM openpsm = psmlist.get(i);
			modids[i] = openpsm.getOpenModId();
			peps[i] = openpsm.getSequence();
			pros[i] = openpsm.getProtein();

			totalPepCount[i] = pepcountmap.get(openpsm.getSequence());
			totalProCount[i] = procountmap.get(openpsm.getProtein());

			openpsm.setSameModCount(modcountmap.get(openpsm.getOpenModId()));
			openpsm.setSamePepCount(totalPepCount[i]);
			openpsm.setSameProCount(totalProCount[i]);

			if (this.learnMethod == semiSupervised) {
				instances.add(openpsm.createSemisuperInstance(getAttributeList(), this.openmods[modids[i]]));
			} else if (this.learnMethod == unsupervised) {
				instances.add(openpsm.createUnsuperInstance(getAttributeList()));
			}
		}

		double[] scores = null;
		if (this.learnMethod == semiSupervised) {

			instances.setClassIndex(instances.numAttributes() - 1);

			Collections.sort(scorelist);
			int thresId = (int) (scorelist.size() * 0.75);
			double threshold = scorelist.get(thresId);

			SemiSuperClassifier classifier = new SemiSuperClassifier(getAttributeList());
			scores = classifier.classify(instances, scoreId, threshold, modids, peps, pros, totalPepCount,
					totalProCount);

		} else if (this.learnMethod == unsupervised) {
			// UnsuperCluster cluster = new UnsuperCluster(getAttributeList());
			// cluster.cluster(instances, blist);
			// scores = cluster.getScores();
		}

		OpenPSM[] validatedPSMs = new OpenPSM[psmlist.size() + noModPsmList.size()];
		for (int i = 0; i < psmlist.size(); i++) {
			validatedPSMs[i] = psmlist.get(i);
			double score = Math.pow(100, (1 - scores[i]));
			validatedPSMs[i].setClassScore(score);
		}

		int noModTarget = 0;
		int noModDecoy = 0;
		OpenPSM[] nomodPSMs = noModPsmList.toArray(new OpenPSM[noModPsmList.size()]);
		Arrays.sort(nomodPSMs, new OpenPSM.InverseExpectScoreComparator());
		for (int i = 0; i < nomodPSMs.length; i++) {
			if (nomodPSMs[i].isTarget()) {
				noModTarget++;
			} else {
				noModDecoy++;
			}

			double score = noModTarget == 0 ? 1 : Math.pow(100, (1 - (double) noModDecoy / (double) noModTarget));
			nomodPSMs[i].setClassScore(score);
			validatedPSMs[i + psmlist.size()] = nomodPSMs[i];
		}

		Arrays.sort(validatedPSMs, new OpenPSM.InverseClassScoreComparator());

		int target = 0;
		int decoy = 0;
		for (int i = 0; i < validatedPSMs.length; i++) {
			boolean isTarget = validatedPSMs[i].isTarget();
			if (isTarget) {
				target++;
			} else {
				decoy++;
			}
			double fdr = (double) decoy / (double) target;
			validatedPSMs[i].setQValue(fdr);
		}

		this.getModStat(validatedPSMs);

		return validatedPSMs;
	}

	private void getModStat(OpenPSM[] validatedPSMs) {
		HashMap<Integer, double[]> scoreMap = new HashMap<Integer, double[]>();
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		HashMap<Integer, ArrayList<Double>> nlMassMap = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, ArrayList<Double>> nlScoreMap = new HashMap<Integer, ArrayList<Double>>();
		for (int i = 0; i < openmods.length; i++) {
			int id = openmods[i].getId();
			scoreMap.put(id, new double[4]);
			countMap.put(id, 0);
			nlMassMap.put(id, new ArrayList<Double>());
			nlScoreMap.put(id, new ArrayList<Double>());
		}
		for (OpenPSM psm : validatedPSMs) {
			int modid = psm.getOpenModId();
			double bscore = psm.getBrankscore();
			double yscore = psm.getYrankscore();
			double bModScore = psm.getBmodrankscore();
			double yModScore = psm.getYmodrankscore();
			double neutralLossMass = psm.getNeutralLossMass();
			double nuetralLossScore = psm.getNlPeakRankScore();

			double[] scores = scoreMap.get(modid);
			scores[0] += bscore;
			scores[1] += yscore;
			scores[2] += bModScore;
			scores[3] += yModScore;
			scoreMap.put(modid, scores);

			countMap.put(modid, countMap.get(modid) + 1);

			nlMassMap.get(modid).add(neutralLossMass);
			nlScoreMap.get(modid).add(nuetralLossScore);
		}

		for (int i = 0; i < openmods.length; i++) {
			int id = openmods[i].getId();

			double[] scores = scoreMap.get(id);
			double count = (double) countMap.get(id);
			if (count > 0) {
				openmods[i].setBscore(scores[0] / count);
				openmods[i].setYscore(scores[1] / count);
				openmods[i].setbModScore(scores[2] / count);
				openmods[i].setyModScore(scores[3] / count);
			}

			ArrayList<Double> nlMassList = nlMassMap.get(id);

			Double[] nlMasses = nlMassList.toArray(new Double[nlMassList.size()]);
			Arrays.sort(nlMasses);

			int start = 0;
			int rangeCount = 0;

			int j = 0;
			int k = 1;
			for (; j < nlMasses.length; j++) {
				if (nlMasses[j] == 0) {
					k++;
					continue;
				}
				for (; k < nlMasses.length;) {
					if (nlMasses[k] - nlMasses[j] > ms2Tolerance) {
						int range = k - j;
						if (range > rangeCount) {
							rangeCount = range;
							start = j;
						}
						k++;
						break;
					} else {
						k++;
					}
				}
			}

			double ratio = (double) rangeCount / (double) nlMasses.length;
			if (ratio > 0.05) {

				double totalMass = 0;
				double totalScore = 0;

				for (int l = 0; l < rangeCount; l++) {
					totalMass += nlMasses[start + l];
				}
				double aveMass = totalMass / (double) rangeCount;

				ArrayList<Double> nlScoreList = nlScoreMap.get(id);
				for (int l = 0; l < nlMassList.size(); l++) {
					double massl = nlMassList.get(l);
					double scorel = nlScoreList.get(l);
					if (massl >= nlMasses[start] && massl < nlMasses[start + rangeCount]) {
						totalScore += scorel;
					}
				}
				double aveScore = totalScore / (double) rangeCount;

				openmods[i].setNeutralLossMass(aveMass);
				openmods[i].setNeutralLossRatio(ratio);
				openmods[i].setNeutralLossScore(aveScore);
			}
		}
	}

	private OpenPSM getOpenPSM(MSFraggerPsm psm, Spectrum sp) {

		String sequence = psm.getSequence();
		double modMass = psm.getOpenModMass();
		int openModId = psm.getOpenModId();

		Ion[] bion = null;
		Ion[] yion = null;

		PeptideModification[] pms = psm.getPTMs();
		if (pms == null) {
			bion = aaf.fragment_b(sequence, true);
			yion = aaf.fragment_y(sequence, true);
		} else {
			double[] addMass = new double[sequence.length() + 2];
			Arrays.fill(addMass, 0.0);
			for (int i = 0; i < pms.length; i++) {
				PostTransModification ptm = pms[i].getPtm();
				int loc = pms[i].getLoc();
				double mass = ptm.getMass();
				addMass[loc] += mass;
			}

			bion = aaf.fragment_b(sequence, addMass, true);
			yion = aaf.fragment_y(sequence, addMass, true);
		}

		Peak[] peaks = sp.getPeaks();
		double[] peakmzs = new double[peaks.length];
		double[] peakRankScore = new double[peaks.length];
		for (int i = 0; i < peakmzs.length; i++) {
			peakmzs[i] = peaks[i].getMz();
			peakRankScore[i] = peaks[i].getRankScore();
		}

		double[] modscore = new double[sequence.length()];
		double fragDeltaMass = 0;
		int matchedCount = 0;
		double fragModDeltaMass = 0;
		int matchedModCount = 0;

		double[] modscoreMono = new double[sequence.length()];
		double fragDeltaMassMono = 0;
		int matchedCountMono = 0;
		double fragModDeltaMassMono = 0;
		int matchedModCountMono = 0;

		int[] ycount = new int[yion.length];

		double yintensity = 0;
		double ymodIntensity = 0;
		double yrankscore = 0;
		double ymodrankscore = 0;
		int[] yModCount = new int[yion.length];

		double yintensityMono = 0;
		double ymodIntensityMono = 0;
		double yrankscoreMono = 0;
		double ymodrankscoreMono = 0;
		int[] ycountMono = new int[yion.length];

		boolean hasMono = !this.openmods[openModId].isMonoIsotope();
		boolean hasMonoNoMod = false;
		if (hasMono) {
			hasMonoNoMod = this.openmods[openModId].getMonoId() == 0;
		}

		for (int i = 0; i < yion.length; i++) {
			double mzi = yion[i].getMz();
			int id = Arrays.binarySearch(peakmzs, mzi);
			if (id < 0) {
				id = -id - 1;
			}

			if (id == 0) {
				if (Math.abs(mzi - peakmzs[id]) < ms2Tolerance) {
					yintensity += peaks[id].getIntensity();
					yrankscore += peaks[id].getRankScore();
					ycount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id]);
					matchedCount++;
				}
			} else if (id == peakmzs.length) {
				if (Math.abs(mzi - peakmzs[id - 1]) < ms2Tolerance) {
					yintensity += peaks[id - 1].getIntensity();
					yrankscore += peaks[id - 1].getRankScore();
					ycount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id - 1]);
					matchedCount++;
				}
			} else {
				if (Math.abs(mzi - peakmzs[id]) < ms2Tolerance) {
					yintensity += peaks[id].getIntensity();
					yrankscore += peaks[id].getRankScore();
					ycount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id]);
					matchedCount++;
				}
				if (Math.abs(mzi - peakmzs[id - 1]) < ms2Tolerance) {
					yintensity += peaks[id - 1].getIntensity();
					yrankscore += peaks[id - 1].getRankScore();
					ycount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id - 1]);
					matchedCount++;
				}
			}

			if (modMass != 0) {

				double modmzi = mzi + modMass;
				int modid = Arrays.binarySearch(peakmzs, modmzi);
				if (modid < 0) {
					modid = -modid - 1;
				}

				if (modid == 0) {
					if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
						ymodIntensity += peaks[modid].getIntensity();
						ymodrankscore += peaks[modid].getRankScore();
						ycount[i] = 1;
						yModCount[i] = 1;

						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modid].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modid].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid]);
						matchedModCount++;
					}
				} else if (modid == peakmzs.length) {
					if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
						ymodIntensity += peaks[modid - 1].getIntensity();
						ymodrankscore += peaks[modid - 1].getRankScore();
						ycount[i] = 1;
						yModCount[i] = 1;

						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modid - 1].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modid - 1].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid - 1]);
						matchedModCount++;
					}
				} else {
					if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
						ymodIntensity += peaks[modid].getIntensity();
						ymodrankscore += peaks[modid].getRankScore();
						ycount[i] = 1;
						yModCount[i] = 1;

						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modid].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modid].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid]);
						matchedModCount++;
					}
					if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
						ymodIntensity += peaks[modid - 1].getIntensity();
						ymodrankscore += peaks[modid - 1].getRankScore();
						ycount[i] = 1;
						yModCount[i] = 1;

						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modid - 1].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modid - 1].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid - 1]);
						matchedModCount++;
					}
				}

				if (hasMono) {

					double monoModMass = this.openmods[this.openmods[openModId].getMonoId()].getExpModMass();

					if (hasMonoNoMod) {

						modmzi = mzi + monoModMass;
						modid = Arrays.binarySearch(peakmzs, modmzi);
						if (modid < 0) {
							modid = -modid - 1;
						}

						if (modid == 0) {
							if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
								ymodIntensityMono += peaks[modid].getIntensity();
								ymodrankscoreMono += peaks[modid].getRankScore();
								ycountMono[i] = 1;

								for (int j = modscoreMono.length - i - 1; j < modscoreMono.length; j++) {
									modscoreMono[j] += peaks[modid].getRankScore();
								}
								for (int j = 0; j < modscoreMono.length - i - 1; j++) {
									modscoreMono[j] -= peaks[modid].getRankScore();
								}
								fragModDeltaMassMono += Math.abs(modmzi - peakmzs[modid]);
								matchedModCountMono++;
							}
						} else if (modid == peakmzs.length) {
							if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
								ymodIntensityMono += peaks[modid - 1].getIntensity();
								ymodrankscoreMono += peaks[modid - 1].getRankScore();
								ycountMono[i] = 1;

								for (int j = modscoreMono.length - i - 1; j < modscoreMono.length; j++) {
									modscoreMono[j] += peaks[modid - 1].getRankScore();
								}
								for (int j = 0; j < modscoreMono.length - i - 1; j++) {
									modscoreMono[j] -= peaks[modid - 1].getRankScore();
								}
								fragModDeltaMassMono += Math.abs(modmzi - peakmzs[modid - 1]);
								matchedModCountMono++;
							}
						} else {
							if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
								ymodIntensityMono += peaks[modid].getIntensity();
								ymodrankscoreMono += peaks[modid].getRankScore();
								ycountMono[i] = 1;

								for (int j = modscoreMono.length - i - 1; j < modscoreMono.length; j++) {
									modscoreMono[j] += peaks[modid].getRankScore();
								}
								for (int j = 0; j < modscoreMono.length - i - 1; j++) {
									modscoreMono[j] -= peaks[modid].getRankScore();
								}
								fragModDeltaMassMono += Math.abs(modmzi - peakmzs[modid]);
								matchedModCountMono++;
							}
							if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
								ymodIntensityMono += peaks[modid - 1].getIntensity();
								ymodrankscoreMono += peaks[modid - 1].getRankScore();
								ycountMono[i] = 1;

								for (int j = modscoreMono.length - i - 1; j < modscoreMono.length; j++) {
									modscoreMono[j] += peaks[modid - 1].getRankScore();
								}
								for (int j = 0; j < modscoreMono.length - i - 1; j++) {
									modscoreMono[j] -= peaks[modid - 1].getRankScore();
								}
								fragModDeltaMassMono += Math.abs(modmzi - peakmzs[modid - 1]);
								matchedModCountMono++;
							}
						}
					}
				}
			}
		}

		boolean isIsotope = true;
		if (hasMono) {
			if (hasMonoNoMod) {
				int isoCount = 0;
				for (int i = 0; i < ycount.length; i++) {
					if (yModCount[i] == 1 && ycount[i] == 0) {
						isoCount++;
					}
				}
				if (isoCount == ycount.length) {
					isIsotope = false;
				}
			} else {
				int isoCount = 0;
				for (int i = 0; i < ycount.length; i++) {
					if (yModCount[i] == 1 && ycountMono[i] == 0) {
						isoCount++;
					}
				}
				if (isoCount == ycount.length) {
					isIsotope = false;
				}
			}
		} else {
			isIsotope = false;
		}

		int realModId = openModId;
		double realModMass = modMass;
		if (isIsotope) {
			realModId = this.openmods[openModId].getMonoId();
			realModMass = this.openmods[realModId].getExpModMass();

			modscore = modscoreMono;
			fragDeltaMass = fragDeltaMassMono;
			matchedCount = matchedCountMono;
			fragModDeltaMass = fragModDeltaMassMono;
			matchedModCount = matchedModCountMono;

			yintensity = yintensityMono;
			ymodIntensity = ymodIntensityMono;
			yrankscore = yrankscoreMono;
			ymodrankscore = ymodrankscoreMono;
			yModCount = ycountMono;
		}

		double nlPeakMz = 0;
		double nlPeakIntensity = 0;
		double nlPeakRankscore = 0;
		double neutralLossMass = 0;
		double isoDeltaM = AminoAcidProperty.isotope_deltaM / 2.0;

		if (psm.getCharge() == 2) {
			double pepmz = (psm.getPrecursorMr() - modMass + realModMass) / 2.0 + AminoAcidProperty.PROTON_W;
			for (int i = 0; i < peaks.length; i++) {
				double mzi = peaks[i].getMz();
				double inteni = peaks[i].getIntensity();
				double deltaMass = pepmz - mzi;
				if (deltaMass > neutralLossRange[0]) {
					continue;
				}
				if (deltaMass < neutralLossRange[1]) {
					break;
				}

				for (int j = i + 1; j < peaks.length; j++) {
					double mzj = peaks[j].getMz();
					if (Math.abs(mzj - mzi - isoDeltaM) < ms2Tolerance) {
						if (inteni > nlPeakIntensity) {
							nlPeakMz = mzi;
							nlPeakIntensity = inteni;
							nlPeakRankscore = peaks[i].getRankScore();
						}
					}
					if (mzj - mzi - isoDeltaM > ms2Tolerance) {
						break;
					}
				}
			}

			if (nlPeakMz > 0) {
				neutralLossMass = (pepmz - nlPeakMz) * 2;
			}
		}

		if (neutralLossMass != 0 && Math.abs(neutralLossMass - realModMass) > ms2Tolerance) {
			for (int i = 0; i < yion.length; i++) {
				double mzi = yion[i].getMz();
				double modNLmzi = mzi + realModMass - neutralLossMass;
				int modNLid = Arrays.binarySearch(peakmzs, modNLmzi);
				if (modNLid < 0) {
					modNLid = -modNLid - 1;
				}

				if (modNLid == 0) {
					if (Math.abs(modNLmzi - peakmzs[modNLid]) < ms2Tolerance) {
						ymodIntensity += peaks[modNLid].getIntensity();
						ymodrankscore += peaks[modNLid].getRankScore();
						ycount[i] = 1;
						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modNLid].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modNLid].getRankScore();
						}
						fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid]);
						matchedModCount++;
					}
				} else if (modNLid == peakmzs.length) {
					if (Math.abs(modNLmzi - peakmzs[modNLid - 1]) < ms2Tolerance) {
						ymodIntensity += peaks[modNLid - 1].getIntensity();
						ymodrankscore += peaks[modNLid - 1].getRankScore();
						ycount[i] = 1;
						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modNLid - 1].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modNLid - 1].getRankScore();
						}
						if (ycount[i] == 0) {
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid - 1]);
							matchedModCount++;
						}
					}
				} else {
					if (Math.abs(modNLmzi - peakmzs[modNLid]) < ms2Tolerance) {
						ymodIntensity += peaks[modNLid].getIntensity();
						ymodrankscore += peaks[modNLid].getRankScore();
						ycount[i] = 1;
						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modNLid].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modNLid].getRankScore();
						}
						if (ycount[i] == 0) {
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid]);
							matchedModCount++;
						}
					}
					if (Math.abs(modNLmzi - peakmzs[modNLid - 1]) < ms2Tolerance) {
						ymodIntensity += peaks[modNLid - 1].getIntensity();
						ymodrankscore += peaks[modNLid - 1].getRankScore();
						ycount[i] = 1;
						for (int j = modscore.length - i - 1; j < modscore.length; j++) {
							modscore[j] += peaks[modNLid - 1].getRankScore();
						}
						for (int j = 0; j < modscore.length - i - 1; j++) {
							modscore[j] -= peaks[modNLid - 1].getRankScore();
						}
						if (ycount[i] == 0) {
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid - 1]);
							matchedModCount++;
						}
					}
				}
			}
		}

		double bintensity = 0;
		double bmodIntensity = 0;
		double brankscore = 0;
		double bmodrankscore = 0;
		int[] bcount = new int[bion.length];

		for (int i = 0; i < bion.length; i++) {

			double mzi = bion[i].getMz();
			int id = Arrays.binarySearch(peakmzs, mzi);
			if (id < 0) {
				id = -id - 1;
			}

			if (id == 0) {
				if (Math.abs(mzi - peakmzs[id]) < ms2Tolerance) {
					bintensity += peaks[id].getIntensity();
					brankscore += peaks[id].getRankScore();
					bcount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id]);
					matchedCount++;
				}
			} else if (id == peakmzs.length) {
				if (Math.abs(mzi - peakmzs[id - 1]) < ms2Tolerance) {
					bintensity += peaks[id - 1].getIntensity();
					brankscore += peaks[id - 1].getRankScore();
					bcount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id - 1]);
					matchedCount++;
				}
			} else {
				if (Math.abs(mzi - peakmzs[id]) < ms2Tolerance) {
					bintensity += peaks[id].getIntensity();
					brankscore += peaks[id].getRankScore();
					bcount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id]);
					matchedCount++;
				}
				if (Math.abs(mzi - peakmzs[id - 1]) < ms2Tolerance) {
					bintensity += peaks[id - 1].getIntensity();
					brankscore += peaks[id - 1].getRankScore();
					bcount[i] = 1;
					fragDeltaMass += Math.abs(mzi - peakmzs[id - 1]);
					matchedCount++;
				}
			}

			if (realModMass != 0) {

				double modmzi = mzi + realModMass;
				int modid = Arrays.binarySearch(peakmzs, modmzi);
				if (modid < 0) {
					modid = -modid - 1;
				}

				if (modid == 0) {
					if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
						bmodIntensity += peaks[modid].getIntensity();
						bmodrankscore += peaks[modid].getRankScore();
						bcount[i] = 1;
						for (int j = 0; j <= i; j++) {
							modscore[j] += peaks[modid].getRankScore();
						}
						for (int j = i + 1; j < modscore.length; j++) {
							modscore[j] -= peaks[modid].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid]);
						matchedModCount++;
					}
				} else if (modid == peakmzs.length) {
					if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
						bmodIntensity += peaks[modid - 1].getIntensity();
						bmodrankscore += peaks[modid - 1].getRankScore();
						bcount[i] = 1;
						for (int j = 0; j <= i; j++) {
							modscore[j] += peaks[modid - 1].getRankScore();
						}
						for (int j = i + 1; j < modscore.length; j++) {
							modscore[j] -= peaks[modid - 1].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid - 1]);
						matchedModCount++;
					}
				} else {
					if (Math.abs(modmzi - peakmzs[modid]) < ms2Tolerance) {
						bmodIntensity += peaks[modid].getIntensity();
						bmodrankscore += peaks[modid].getRankScore();
						bcount[i] = 1;
						for (int j = 0; j <= i; j++) {
							modscore[j] += peaks[modid].getRankScore();
						}
						for (int j = i + 1; j < modscore.length; j++) {
							modscore[j] -= peaks[modid].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid]);
						matchedModCount++;
					}
					if (Math.abs(modmzi - peakmzs[modid - 1]) < ms2Tolerance) {
						bmodIntensity += peaks[modid - 1].getIntensity();
						bmodrankscore += peaks[modid - 1].getRankScore();
						bcount[i] = 1;
						for (int j = 0; j <= i; j++) {
							modscore[j] += peaks[modid - 1].getRankScore();
						}
						for (int j = i + 1; j < modscore.length; j++) {
							modscore[j] -= peaks[modid - 1].getRankScore();
						}
						fragModDeltaMass += Math.abs(modmzi - peakmzs[modid - 1]);
						matchedModCount++;
					}
				}

				if (neutralLossMass != 0 && Math.abs(neutralLossMass - realModMass) > ms2Tolerance) {

					double modNLmzi = mzi + realModMass - neutralLossMass;
					int modNLid = Arrays.binarySearch(peakmzs, modNLmzi);
					if (modNLid < 0) {
						modNLid = -modNLid - 1;
					}

					if (modNLid == 0) {
						if (Math.abs(modNLmzi - peakmzs[modNLid]) < ms2Tolerance) {
							bmodIntensity += peaks[modNLid].getIntensity();
							bmodrankscore += peaks[modNLid].getRankScore();
							bcount[i] = 1;
							for (int j = 0; j <= i; j++) {
								modscore[j] += peaks[modNLid].getRankScore();
							}
							for (int j = i + 1; j < modscore.length; j++) {
								modscore[j] -= peaks[modNLid].getRankScore();
							}
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid]);
							matchedModCount++;
						}
					} else if (modNLid == peakmzs.length) {
						if (Math.abs(modNLmzi - peakmzs[modNLid - 1]) < ms2Tolerance) {
							bmodIntensity += peaks[modNLid - 1].getIntensity();
							bmodrankscore += peaks[modNLid - 1].getRankScore();
							bcount[i] = 1;
							for (int j = 0; j <= i; j++) {
								modscore[j] += peaks[modNLid - 1].getRankScore();
							}
							for (int j = i + 1; j < modscore.length; j++) {
								modscore[j] -= peaks[modNLid - 1].getRankScore();
							}
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid - 1]);
							matchedModCount++;
						}
					} else {
						if (Math.abs(modNLmzi - peakmzs[modNLid]) < ms2Tolerance) {
							bmodIntensity += peaks[modNLid].getIntensity();
							bmodrankscore += peaks[modNLid].getRankScore();
							bcount[i] = 1;
							for (int j = 0; j <= i; j++) {
								modscore[j] += peaks[modNLid].getRankScore();
							}
							for (int j = i + 1; j < modscore.length; j++) {
								modscore[j] -= peaks[modNLid].getRankScore();
							}
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid]);
							matchedModCount++;
						}
						if (Math.abs(modNLmzi - peakmzs[modNLid - 1]) < ms2Tolerance) {
							bmodIntensity += peaks[modNLid - 1].getIntensity();
							bmodrankscore += peaks[modNLid - 1].getRankScore();
							bcount[i] = 1;
							for (int j = 0; j <= i; j++) {
								modscore[j] += peaks[modNLid - 1].getRankScore();
							}
							for (int j = i + 1; j < modscore.length; j++) {
								modscore[j] -= peaks[modNLid - 1].getRankScore();
							}
							fragModDeltaMass += Math.abs(modNLmzi - peakmzs[modNLid - 1]);
							matchedModCount++;
						}
					}
				}
			}
		}

		fragDeltaMass = matchedCount == 0 ? 0 : fragDeltaMass / (double) matchedCount;
		fragModDeltaMass = matchedModCount == 0 ? 0 : fragModDeltaMass / (double) matchedModCount;

		int totalBCount = 0;
		for (int i = 0; i < bcount.length; i++) {
			totalBCount += bcount[i];
		}

		int totalYCount = 0;
		for (int i = 0; i < ycount.length; i++) {
			totalYCount += ycount[i];
		}

		double maxvalue = -1000;
		for (int i = 0; i < modscore.length; i++) {
			if (modscore[i] > maxvalue) {
				maxvalue = modscore[i];
			}
		}
		ArrayList<Integer> maxModList = new ArrayList<Integer>();
		for (int i = 0; i < modscore.length; i++) {
			if (modscore[i] == maxvalue) {
				maxModList.add(i);
			}
		}
		int[] possibleLoc = new int[maxModList.size()];
		for (int i = 0; i < possibleLoc.length; i++) {
			possibleLoc[i] = maxModList.get(i);
		}

		OpenPSM openpsm = new OpenPSM(psm.getFileName(), psm.getScan(), psm.getCharge(),
				psm.getPrecursorMr() + realModMass - modMass, sp.getRt(), sequence, psm.getPepMass(), psm.getMassDiff(),
				psm.getMiss(), psm.getNum_tol_term(), psm.getTot_num_ions(), psm.getNum_matched_ions(),
				psm.getProtein(), psm.getHyperscore(), psm.getNextscore(), psm.getExpect(), realModMass,
				psm.getModMassDiff(), bintensity, yintensity, brankscore, yrankscore, totalBCount, totalYCount,
				fragDeltaMass, bmodIntensity, ymodIntensity, bmodrankscore, ymodrankscore, fragModDeltaMass, realModId,
				possibleLoc, neutralLossMass, nlPeakRankscore, psm.isTarget(), 0.0);

		openpsm.setPTMs(psm.getPTMs());

		return openpsm;
	}

	private OpenPSM[] match(File[] mgfs) throws IOException {

		ArrayList<OpenPSM> psmlist = new ArrayList<OpenPSM>();

		StringBuilder sb = new StringBuilder();
		for (File mgffile : mgfs) {
			String fileName = mgffile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			if (this.psmmap.containsKey(fileName)) {
				HashMap<Integer, MSFraggerPsm> map = this.psmmap.get(fileName);

				MgfReader mr = new MgfReader(mgffile);
				Spectrum sp = null;
				while ((sp = mr.getNextSpectrum()) != null) {
					int scan = sp.getScannum();
					if (map.containsKey(scan)) {
						MSFraggerPsm psm = map.get(scan);
						OpenPSM openPsm = getOpenPSM(psm, sp);
						psmlist.add(openPsm);
					}
				}
				mr.close();
			}
		}

		OpenPSM[] validatedPSMs = psmlist.toArray(new OpenPSM[psmlist.size()]);

		return validatedPSMs;
	}

	private OpenPSM[] validateSeparate(File[] mgfs) throws Exception {

		ArrayList<OpenPSM> psmlist0 = new ArrayList<OpenPSM>();
		ArrayList<OpenPSM> psmlist1 = new ArrayList<OpenPSM>();

		HashMap<Integer, Integer> modcountmap = new HashMap<Integer, Integer>();
		HashMap<String, Integer> pepcountmap = new HashMap<String, Integer>();
		HashMap<String, Integer> procountmap = new HashMap<String, Integer>();

		StringBuilder sb = new StringBuilder();
		for (File mgffile : mgfs) {
			String fileName = mgffile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
			if (this.psmmap.containsKey(fileName)) {
				HashMap<Integer, MSFraggerPsm> map = this.psmmap.get(fileName);

				MgfReader mr = new MgfReader(mgffile);
				Spectrum sp = null;
				while ((sp = mr.getNextSpectrum()) != null) {
					int scan = sp.getScannum();
					if (map.containsKey(scan)) {

						MSFraggerPsm psm = map.get(scan);
						OpenPSM openPsm = getOpenPSM(psm, sp);

						int modid = openPsm.getOpenModId();
						if (modcountmap.containsKey(modid)) {
							modcountmap.put(modid, modcountmap.get(modid) + 1);
						} else {
							modcountmap.put(modid, 1);
						}

						String pep = openPsm.getSequence();
						if (pepcountmap.containsKey(pep)) {
							pepcountmap.put(pep, pepcountmap.get(pep) + 1);
						} else {
							pepcountmap.put(pep, 1);
						}

						String pro = openPsm.getProtein();
						if (procountmap.containsKey(pro)) {
							procountmap.put(pro, procountmap.get(pro) + 1);
						} else {
							procountmap.put(pro, 1);
						}

						if (modid == -1) {
							psmlist0.add(openPsm);
						} else {
							psmlist1.add(openPsm);
						}
					}
				}
				mr.close();
			}
		}

		modcountmap.put(-2, 1);

		Instances instances0 = new Instances("PSM Instances 0", attributeList, psmlist0.size());

		int[] modids0 = new int[psmlist0.size()];
		String[] peps0 = new String[psmlist0.size()];
		String[] pros0 = new String[psmlist0.size()];

		int[] totalPepCount0 = new int[psmlist0.size()];
		int[] totalProCount0 = new int[psmlist0.size()];

		ArrayList<Double> scorelist0 = new ArrayList<Double>(psmlist0.size());
		ArrayList<Boolean> blist0 = new ArrayList<Boolean>(psmlist0.size());

		for (int i = 0; i < psmlist0.size(); i++) {

			OpenPSM openpsm = psmlist0.get(i);

			scorelist0.add(openpsm.getHyperscore());
			blist0.add(openpsm.isTarget());

			modids0[i] = openpsm.getOpenModId();
			peps0[i] = openpsm.getSequence();
			pros0[i] = openpsm.getProtein();

			totalPepCount0[i] = pepcountmap.get(openpsm.getSequence());
			totalProCount0[i] = procountmap.get(openpsm.getProtein());

			openpsm.setSameModCount(modcountmap.get(openpsm.getOpenModId()));
			openpsm.setSamePepCount(totalPepCount0[i]);
			openpsm.setSameProCount(totalProCount0[i]);

			if (this.learnMethod == semiSupervised) {
				instances0.add(openpsm.createSemisuperInstance(getAttributeList(), this.openmods[modids0[i]]));
			} else if (this.learnMethod == unsupervised) {
				instances0.add(openpsm.createUnsuperInstance(getAttributeList()));
			}
		}

		double[] scores0 = null;

		if (this.learnMethod == semiSupervised) {
			instances0.setClassIndex(instances0.numAttributes() - 1);

			Collections.sort(scorelist0);
			int thresId = (int) (scorelist0.size() * 0.75);
			double threshold = scorelist0.get(thresId);
			SemiSuperClassifier classifier = new SemiSuperClassifier(getAttributeList());
			scores0 = classifier.classify(instances0, 10, threshold, modids0, peps0, pros0, totalPepCount0,
					totalProCount0);

		} else if (this.learnMethod == unsupervised) {
			UnsuperCluster cluster = new UnsuperCluster(getAttributeList());
			cluster.cluster(instances0, blist0);
			scores0 = cluster.getScores();
		}

		Instances instances1 = new Instances("PSM Instances 1", attributeList, psmlist1.size());
		int[] modids1 = new int[psmlist1.size()];
		String[] peps1 = new String[psmlist1.size()];
		String[] pros1 = new String[psmlist1.size()];

		int[] totalPepCount1 = new int[psmlist1.size()];
		int[] totalProCount1 = new int[psmlist1.size()];

		ArrayList<Double> scorelist1 = new ArrayList<Double>(psmlist1.size());
		ArrayList<Boolean> blist1 = new ArrayList<Boolean>(psmlist1.size());

		for (int i = 0; i < psmlist1.size(); i++) {

			OpenPSM openpsm = psmlist1.get(i);

			scorelist1.add(openpsm.getHyperscore());
			blist1.add(openpsm.isTarget());

			modids1[i] = openpsm.getOpenModId();
			peps1[i] = openpsm.getSequence();
			pros1[i] = openpsm.getProtein();

			totalPepCount1[i] = pepcountmap.get(openpsm.getSequence());
			totalProCount1[i] = procountmap.get(openpsm.getProtein());

			openpsm.setSameModCount(modcountmap.get(openpsm.getOpenModId()));
			openpsm.setSamePepCount(totalPepCount1[i]);
			openpsm.setSameProCount(totalProCount1[i]);

			if (this.learnMethod == semiSupervised) {
				instances1.add(openpsm.createSemisuperInstance(getAttributeList(), this.openmods[modids1[i]]));
			} else if (this.learnMethod == unsupervised) {
				instances1.add(openpsm.createUnsuperInstance(getAttributeList()));
			}
		}

		double[] scores1 = null;

		if (this.learnMethod == semiSupervised) {

			instances1.setClassIndex(instances1.numAttributes() - 1);

			Collections.sort(scorelist1);
			int thresId = (int) (scorelist1.size() * 0.75);
			double threshold = scorelist1.get(thresId);
			SemiSuperClassifier classifier = new SemiSuperClassifier(getAttributeList());
			scores1 = classifier.classify(instances1, 10, threshold, modids1, peps1, pros1, totalPepCount1,
					totalProCount1);

		} else if (this.learnMethod == unsupervised) {
			UnsuperCluster cluster = new UnsuperCluster(getAttributeList());
			cluster.cluster(instances1, blist1);
			scores1 = cluster.getScores();
		}
		OpenPSM[] validatedPSMs = new OpenPSM[psmlist0.size() + psmlist1.size()];
		for (int i = 0; i < psmlist0.size(); i++) {
			validatedPSMs[i] = psmlist0.get(i);
			validatedPSMs[i].setClassScore(scores0[i]);
		}

		for (int i = 0; i < psmlist1.size(); i++) {
			validatedPSMs[i + psmlist0.size()] = psmlist1.get(i);
			validatedPSMs[i + psmlist0.size()].setClassScore(scores1[i]);
		}

		return validatedPSMs;
	}

	private ArrayList<Attribute> getAttributeList() {
		return attributeList;
	}

}
