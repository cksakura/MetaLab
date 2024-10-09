package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoTree;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.glycan.deNovo.PeptidePeakNode;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfWriter;

/**
 * <p>
 * the abstract class of the IGlycoParser
 * 
 * @author Kai Cheng <ckkazuma@gmail.com>
 *
 */
public abstract class AbstractGlycoParser implements IGlycoParser {

	final static double h2o = AminoAcidProperty.MONOW_H * 2 + AminoAcidProperty.MONOW_O;
	final static double nh3 = AminoAcidProperty.MONOW_H * 3 + AminoAcidProperty.MONOW_N;

	double precursorPPM;
	double fragmentPPM;

	SpectraList ms1SpectraList;
	SpectraList ms2SpectraList;

	ArrayList<Features> featuresList;
	HashMap<Integer, Integer> feasIdMap;
	ArrayList<NovoGlycoCompMatch> determinedMatchList;
	ArrayList<NovoGlycoCompMatch> undeterminedMatchList;
	HashSet<Integer> usedRank2Scans;
	Integer[] ms2scans;
	int[] glycanIds;
	
	HashMap<Double, int[]> combineGlycans;

	double[] aminoacidMzs;

	// all the MS2 spectra are divided into four types, type 2-4 contain oxonium
	// ions which are glycopeptide/glycan spectra
	
	// 1. spectra without oxonium ions
	HashSet<Integer> noOxoniumScanSet;

	// 2. glycan trees are detected in the spectra, despite of peptide ions
	HashSet<Integer> glycanTreeScanSet;

	// 3. glycan trees are not detected in the spectra, peptide ions are detected
	HashSet<Integer> pepScanSet;

	// 4. neither glycan trees nor peptide ions are detected
	HashSet<Integer> noPepGlycanScanSet;

	HashMap<Integer, Peak[]> pepSpectraMap;
	HashMap<Integer, double[]> pepSpectraPPMap;

	protected static Appendable testsb;
	protected static int[] testCounts;

	public Integer[] getMs2scans() {
		return ms2scans;
	}

	public SpectraList getMs1SpectraList() {
		return ms1SpectraList;
	}

	public SpectraList getMs2SpectraList() {
		return ms2SpectraList;
	}

	public ArrayList<Features> getFeaturesList() {
		return featuresList;
	}

	public HashMap<Integer, Integer> getFeasIdMap() {
		return feasIdMap;
	}

	public ArrayList<NovoGlycoCompMatch> getDeterminedMatchList() {
		return determinedMatchList;
	}

	public ArrayList<NovoGlycoCompMatch> getUndeterminedMatchList() {
		return undeterminedMatchList;
	}

	public HashSet<Integer> getUsedRank2Scans() {
		return usedRank2Scans;
	}

	/**
	 * <p>
	 * Get glycopeptide spectra matches (GSMs) from a spectrum and add them to the
	 * GSM list
	 * <p>
	 * If the spectra file is mgf, using only the MS2 spectrum, the precursor mz and
	 * charge are read from the spectrum directly; if the spectra file is mzXML, the
	 * MS1 scans will be parsed and the precursor mz and charge are calculated by
	 * the MS1 feature
	 */
	public void parseSpectrum(int currentId, boolean getMS1Feature) {

		if (getMS1Feature) {

			Spectrum ms2spectrumi = ms2SpectraList.getScan(ms2scans[currentId]);
			Features features;

			if (feasIdMap.containsKey(ms2scans[currentId])) {
				features = featuresList.get(feasIdMap.get(ms2scans[currentId]));
			} else {
				features = ms1SpectraList.getFeatures(ms2spectrumi.getPrecursorCharge(), ms2spectrumi.getPrecursorMz(),
						ms2scans[currentId], precursorPPM);

				if (features.getScanList().length < scansLengthLimit) {
					return;
				}
			}

			NovoGlycoCompMatch[] matchesi = this.parseGSM(ms2spectrumi, features.getMonoMz(), features.getCharge());

			if (matchesi == null) {

				return;

			} else {

				double rank1score = 0;
				double rank2score = 0;

				double maxGlycoMass = 0;

				for (NovoGlycoCompMatch match : matchesi) {
					if (match.isDetermined()) {
						this.determinedMatchList.add(match);
						if (match.getRank() == 1) {
							rank1score = match.getTreeScore();
						} else if (match.getRank() == 2) {
							rank2score = match.getTreeScore();
						}
					} else {
						this.undeterminedMatchList.add(match);
					}

					double glycoMass = match.getGlycanMass();
					if (glycoMass > maxGlycoMass) {
						maxGlycoMass = glycoMass;
					}
				}

				double ppMz = ms2spectrumi.getPrecursorMz();
				int ppCharge = ms2spectrumi.getPrecursorCharge();

				double subpp = ppMz - maxGlycoMass / (double) ppCharge;
				if (subpp < 0) {
					subpp = ppMz;
				}

				this.pepSpectraPPMap.put(ms2scans[currentId], new double[] { ppMz, subpp });

				if (rank1score == rank2score) {
					this.usedRank2Scans.add(ms2scans[currentId]);
				}

				feasIdMap.put(ms2scans[currentId], featuresList.size());
				featuresList.add(features);
			}

			int featuresId = feasIdMap.get(ms2scans[currentId]);

			int lastScan = features.getLastScan();

			for (int j = currentId + 1; j < ms2scans.length; j++) {

				if (feasIdMap.containsKey(ms2scans[j]))
					continue;

				if (ms2scans[j] > lastScan)
					break;

				Spectrum ms2spectrumj = ms2SpectraList.getScan(ms2scans[j]);

				double tolerance = ms2spectrumj.getPrecursorMz() * precursorPPM * 1E-6;
				boolean contain = features.contain(ms2scans[j], ms2spectrumj.getPrecursorMz(), tolerance);

				if (contain) {
					feasIdMap.put(ms2scans[j], featuresId);
				}
			}
		} else {

			Spectrum ms2spectrumi = ms2SpectraList.getScan(ms2scans[currentId]);

			NovoGlycoCompMatch[] matchesi = this.parseGSM(ms2spectrumi, ms2spectrumi.getPrecursorMz(),
					ms2spectrumi.getPrecursorCharge());

			if (matchesi == null) {

				return;

			} else {

				double rank1score = 0;
				double rank2score = 0;

				double maxGlycoMass = 0;

				for (NovoGlycoCompMatch match : matchesi) {
					if (match.isDetermined()) {
						this.determinedMatchList.add(match);
						if (match.getRank() == 1) {
							rank1score = match.getTreeScore();
						} else if (match.getRank() == 2) {
							rank2score = match.getTreeScore();
						}
					} else {
						this.undeterminedMatchList.add(match);
					}

					double glycoMass = match.getGlycanMass();
					if (glycoMass > maxGlycoMass) {
						maxGlycoMass = glycoMass;
					}
				}

				double ppMz = ms2spectrumi.getPrecursorMz();
				int ppCharge = ms2spectrumi.getPrecursorCharge();

				double subpp = ppMz - maxGlycoMass / (double) ppCharge;
				if (subpp < 0) {
					subpp = ppMz;
				}

				this.pepSpectraPPMap.put(ms2scans[currentId], new double[] { ppMz, subpp });

				if (rank1score == rank2score) {
					this.usedRank2Scans.add(ms2scans[currentId]);
				}
			}
		}
	}

	public void parseSpectrum(int currentId) {
		parseSpectrum(currentId, false);
	}

	double[] calScore(HashMap<Integer, GlycoPeakNode> nodemap, int[] totalComposition, GlycoPeakNode[] pns,
			HashMap<Integer, GlycoPeakNode> oxoniumMap, HashMap<Integer, GlycoPeakNode> missOxoniumMap) {

		int charge = 1;
		double nodeScore = 0;
		Integer[] ids = nodemap.keySet().toArray(new Integer[nodemap.size()]);
		ArrayList<Double> usedlist = new ArrayList<Double>();
		L: for (int j = 0; j < ids.length; j++) {
			GlycoPeakNode node = nodemap.get(ids[j]);

			int[] nodeComp = node.getComposition();

			for (int k = 0; k < nodeComp.length; k++) {
				if (nodeComp[k] > totalComposition[k]) {
					nodemap.remove(ids[j]);
					continue L;
				}
			}

			charge = node.getCharge();
			nodeScore += node.getSingleScore();
			usedlist.add(node.getMw());
		}

		Double[] usedMws = usedlist.toArray(new Double[usedlist.size()]);
		Arrays.sort(usedMws);

		double totalScore = 0;
		L: for (int i = 0; i < pns.length; i++) {
			if (pns[i] != null && pns[i].getMonoId() == 0) {

				if (pns[i].getPeakMz() < usedMws[0] / (double) charge) {
					continue;
				}

				boolean hasLink = false;
				if (pns[i].getParent() == null) {
					GlycoPeakNode[] children = pns[i].getChildren();
					for (GlycoPeakNode child : children) {
						if (child != null) {
							hasLink = true;
							break;
						}
					}
				} else {
					hasLink = true;
				}

				if (!hasLink) {
					continue;
				}

				for (int j = 0; j < usedMws.length; j++) {
					if (Math.abs(usedMws[j] - pns[i].getMw() - h2o) < 0.01) {
						continue L;
					}
					if (Math.abs(usedMws[j] - pns[i].getMw() - nh3) < 0.01) {
						continue L;
					}
				}
				totalScore += pns[i].getSingleScore();
			}
		}

		double haveOxo = 0;
		for (Integer id : oxoniumMap.keySet()) {
			haveOxo += oxoniumMap.get(id).getSingleScore();
		}

		double missOxo = 0;
		for (Integer id : missOxoniumMap.keySet()) {
			GlycoPeakNode node = missOxoniumMap.get(id);
			missOxo += node.getSingleScore();
		}

		double treeScore = totalScore == 0 ? 0 : nodeScore * (nodeScore / totalScore);
		double oxoniumScore;
		if (haveOxo > 0) {
			oxoniumScore = haveOxo / (haveOxo + missOxo);
		} else {
			if (missOxo > 0) {
				oxoniumScore = missOxo / (double) (missOxoniumMap.size());
			} else {
				oxoniumScore = 1;
			}
		}
		double score = oxoniumScore * treeScore;
		return new double[] { score, treeScore, oxoniumScore };
	}

	/**
	 * judge a spectrum if it have peptide y ions
	 * 
	 * @param peaks
	 * @param precursorMz
	 * @param preCharge
	 * @param aaMap
	 * @return
	 */
	boolean parsePeptide(Peak[] peaks, double precursorMz, int preCharge, HashMap<Integer, HashSet<Integer>> aaMap) {

		boolean peptide = false;

		for (int charge = 1; charge <= preCharge; charge++) {
			aaMap.put(charge, new HashSet<Integer>());
		}

		ArrayList<Peak> list = new ArrayList<Peak>();
		Peak[] intenPeaks = new Peak[peaks.length];
		System.arraycopy(peaks, 0, intenPeaks, 0, peaks.length);
		Arrays.sort(intenPeaks, new Peak.RevIntensityComparator());

		L: for (int i = 0; i < intenPeaks.length; i++) {
			double mzi = intenPeaks[i].getMz();
			if (mzi < 500) {
				continue;
			}
			for (int j = 0; j < list.size(); j++) {
				double mzj = list.get(j).getMz();
				if (Math.abs(mzi - mzj) < AminoAcidProperty.isotope_deltaM * preCharge) {
					continue L;
				}
			}
			list.add(intenPeaks[i]);

			if (list.size() == 10) {
				break;
			}
		}

		if (list.size() < 6) {
			return false;
		}

		Peak[] topPeaks = list.toArray(new Peak[list.size()]);
		Arrays.sort(topPeaks);

		for (int charge = 1; charge <= preCharge; charge++) {
			HashSet<Integer> set = aaMap.get(charge);
			for (int i = 0; i < topPeaks.length; i++) {
				double mzi = topPeaks[i].getMz();
				double fragTolerance = charge > 2 ? mzi * fragmentPPM * 1E-6 * 1.5 : mzi * fragmentPPM * 1E-6;

				for (int j = i + 1; j < topPeaks.length; j++) {
					double mzj = topPeaks[j].getMz();
					double delta = mzj - mzi;

					for (int k = 0; k < this.aminoacidMzs.length; k++) {
						if (Math.abs(delta - this.aminoacidMzs[k] / (double) charge) < fragTolerance) {

							int loci = Arrays.binarySearch(peaks, topPeaks[i]);
							int locj = Arrays.binarySearch(peaks, topPeaks[j]);

							set.add(loci);
							set.add(locj);

							break;
						}
					}
				}
			}

			if (set.size() > 3) {
				peptide = true;
			}
		}

		return peptide;
	}

	/**
	 * match {@linkplain DenovoGlycoTree} from the MS2 peaks
	 * 
	 * @param peaks
	 * @param precursorMz
	 * @param preCharge
	 * @param glycanMasses
	 * @param oxoniumMap
	 * @return
	 */
	DenovoGlycoTree[] getGlycoTreeList(Peak[] peaks, double precursorMz, int preCharge, double[] glycanMasses,
			HashMap<Integer, GlycoPeakNode> oxoniumMap) {

		ArrayList<DenovoGlycoTree> treeList = new ArrayList<DenovoGlycoTree>();

		Integer[] oxoIds = oxoniumMap.keySet().toArray(new Integer[oxoniumMap.size()]);
		Arrays.sort(oxoIds);
		int oxoUpLimit = oxoIds.length > 0 ? oxoIds[oxoIds.length - 1] : 0;

		double upLimit = precursorMz * preCharge;
		double maxPeptideScore = 0;

		for (int charge = preCharge; charge >= 1; charge--) {

			GlycoPeakNode[] pns = new GlycoPeakNode[peaks.length];

			HashMap<Integer, PeptidePeakNode> pepNodeMap = new HashMap<Integer, PeptidePeakNode>();

			for (int i = 0; i < peaks.length; i++) {

				if (oxoniumMap.containsKey(i))
					continue;

				if (pns[i] != null && pns[i].getMonoId() > 0) {
					continue;
				}

				double mzi = peaks[i].getMz();

				double fragTolerance;

				if (charge == 1) {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 1.5;
				} else if (charge == 2) {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 2.0;
				} else {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 2.5;
				}

				boolean findTheChargeI = false;
				boolean findOtherChargeI = false;

				for (int ik = i + 1; ik < peaks.length; ik++) {

					if (oxoniumMap.containsKey(ik))
						continue;

					if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
						break;
					}

					for (int chargei = 1; chargei <= preCharge; chargei++) {
						if (Math.abs(peaks[ik].getMz() - mzi
								- AminoAcidProperty.isotope_deltaM / (double) chargei) < fragTolerance) {

							pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
							pns[ik].setMonoId(i);

							if (chargei == charge) {
								findTheChargeI = true;
							} else {
								findOtherChargeI = true;
							}
						}
					}
				}

				if (findTheChargeI) {
					for (int ik = i + 1; ik < peaks.length; ik++) {

						if (oxoniumMap.containsKey(ik))
							continue;

						if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
							break;
						}

						for (int isoi = 1; isoi <= 5; isoi++) {
							if (Math.abs(peaks[ik].getMz() - mzi
									- AminoAcidProperty.isotope_deltaM * isoi / (double) charge) < fragTolerance) {

								pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
								pns[ik].setMonoId(i);
							}
						}
					}
				} else {
					if (findOtherChargeI) {
						continue;
					}
				}

				for (int j = i + 1; j < peaks.length; j++) {

					if (oxoniumMap.containsKey(j))
						continue;

					if (pns[j] != null && pns[j].getMonoId() > 0) {
						continue;
					}

					double mzj = peaks[j].getMz();
					double delta = mzj - mzi;

					if (delta - glycanMasses[glycanMasses.length - 1] / (double) charge > fragTolerance) {
						break;
					}

					if ((mzj - fragTolerance) * charge > upLimit)
						break;

					boolean findTheChargeJ = false;
					boolean findOtherChargeJ = false;

					for (int jk = j + 1; jk < peaks.length; jk++) {
						if (oxoniumMap.containsKey(jk))
							continue;

						if (peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * preCharge > fragTolerance) {
							break;
						}

						for (int chargej = 1; chargej <= preCharge; chargej++) {
							if (Math.abs(peaks[jk].getMz() - mzj
									- AminoAcidProperty.isotope_deltaM / (double) chargej) < fragTolerance) {

								pns[jk] = new GlycoPeakNode(peaks[jk], jk, glycanMasses.length);
								pns[jk].setMonoId(j);

								if (chargej == charge) {
									findTheChargeJ = true;
								} else {
									findOtherChargeJ = true;
								}
							}
						}
					}
					// System.out.println("~~~~374\t"+charge+"\t"+mzi+"\t"+peaks[j].getMz()+"\t"+findTheChargeJ+"\t"+findOtherChargeJ);
					if (findTheChargeJ) {
						for (int jk = j + 1; jk < peaks.length; jk++) {

							if (oxoniumMap.containsKey(jk))
								continue;

							if (peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
								break;
							}

							for (int isoj = 1; isoj <= 5; isoj++) {
								if (Math.abs(peaks[jk].getMz() - mzj
										- AminoAcidProperty.isotope_deltaM * isoj / (double) charge) < fragTolerance) {

									pns[jk] = new GlycoPeakNode(peaks[jk], jk, glycanMasses.length);
									pns[jk].setMonoId(j);
								}
							}
						}
					} else {
						if (findOtherChargeJ) {
							continue;
						}
					}

					if (j > oxoUpLimit) {
						for (int k = 0; k < this.aminoacidMzs.length; k++) {
							double deltaMz = Math.abs(delta - aminoacidMzs[k] / (double) charge);
							if (deltaMz < fragTolerance) {

								PeptidePeakNode ppni = null;
								if (pepNodeMap.containsKey(i)) {
									ppni = pepNodeMap.get(i);
								} else {
									ppni = new PeptidePeakNode(peaks[i], i);
									pepNodeMap.put(i, ppni);
								}

								PeptidePeakNode ppnj = null;
								if (pepNodeMap.containsKey(j)) {
									ppnj = pepNodeMap.get(j);
								} else {
									ppnj = new PeptidePeakNode(peaks[j], j);
									pepNodeMap.put(j, ppnj);
								}

								if (ppni.getChild() != null) {
									PeptidePeakNode child = ppni.getChild();
									if (ppnj.getParent() == null) {
										if (ppnj.getRankScore() > child.getRankScore()) {
											ppnj.setParent(ppni);
										}
									}
								} else {
									if (ppnj.getParent() != null) {
										PeptidePeakNode parent = ppnj.getParent();
										if (ppni.getRankScore() > parent.getRankScore()) {
											ppnj.setParent(ppni);
										}
									} else {
										ppnj.setParent(ppni);
									}
								}
							}
						}
					}

					LK: for (int k = 0; k < glycanMasses.length; k++) {

						double deltaMz = Math.abs(delta - glycanMasses[k] / (double) charge);
						boolean findK = false;
//						System.out.println("490\t"+charge+"\t"+mzi+"\t"+mzj+"\t"+deltaMz+"\t"+glycanMasses[k]+"\t"+fragTolerance);
						if (deltaMz < fragTolerance) {

							if (pns[i] == null)
								pns[i] = new GlycoPeakNode(peaks[i], i, glycanMasses.length);
							if (pns[j] == null)
								pns[j] = new GlycoPeakNode(peaks[j], j, glycanMasses.length);

							if (pns[j].getParent() == null) {
								if (pns[i].getChildren()[k] == null) {
									pns[j].setParent(pns[i], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setGlycanId(k);
									pns[j].setGlycanMass(glycanMasses[k]);

									pns[i].setCharge(charge);
									pns[j].setCharge(charge);

								} else {
									if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
										pns[j].setParent(pns[i], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setGlycanId(k);
										pns[j].setGlycanMass(glycanMasses[k]);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									}
								}
							} else {
								if (pns[i].getPeakIntensity() > pns[j].getParent().getPeakIntensity()) {
									GlycoPeakNode[] childrenj = pns[j].getParent().getChildren();
									for (int l = 0; l < childrenj.length; l++) {
										if (childrenj[l] == pns[j]) {
											childrenj[l] = null;
										}
									}
									if (pns[i].getChildren()[k] == null) {
										pns[j].setParent(pns[i], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setGlycanId(k);
										pns[j].setGlycanMass(glycanMasses[k]);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									} else {
										if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
											pns[j].setParent(pns[i], k);
											pns[j].setDeltaMz(deltaMz);
											pns[j].setGlycanId(k);
											pns[j].setGlycanMass(glycanMasses[k]);

											pns[i].setCharge(charge);
											pns[j].setCharge(charge);
										}
									}
								}
							}

							findK = true;
						}

						if (findK) {
							break;
						}
					}
				}
			}

			PeptidePeakNode[] pepNodes = pepNodeMap.values().toArray(new PeptidePeakNode[pepNodeMap.size()]);

			for (int i = 0; i < pepNodes.length; i++) {
				PeptidePeakNode nodei = pepNodes[i];
				if (nodei.isRoot() && !nodei.isSingle()) {
					double score = nodei.getPeak().getRankScore();
					while (true) {
						if (nodei.getChild() == null) {
							break;
						} else {
							nodei = nodei.getChild();
							score += nodei.getPeak().getRankScore();
						}
					}

					if (score > maxPeptideScore) {
						maxPeptideScore = score;
					}
				}
			}

			L: for (int i = 0; i < pns.length; i++) {

				if (pns[i] == null)
					continue;

				if (pns[i].isRoot() && pns[i].getMonoId() == 0) {

					DenovoGlycoTree tree = new DenovoGlycoTree(glycanMasses.length, charge, pns);
					add(tree, pns[i]);
					if (tree.getSize() >= 3) {

						tree.setMainStem();

						GlycoPeakNode[] nodes = tree.getMap().values().toArray(new GlycoPeakNode[tree.getMap().size()]);
						for (int j = 0; j < nodes.length; j++) {
							if (nodes[j].isMainTree()) {
								if (nodes[j].getMonoId() != 0) {
									continue L;
								}
							}
						}
						treeList.add(tree);
					}
				}
			}
		}

		Iterator<DenovoGlycoTree> it = treeList.iterator();
		while (it.hasNext()) {
			DenovoGlycoTree tree = it.next();
			// System.out.println("654\t"+tree.getTotalScore()+"\t"+maxPeptideScore+"\t"+Arrays.toString(tree.getGlycanComposition()));
			// if (tree.getTotalScore() < maxPeptideScore) {
			// it.remove();
			// }
		}
		DenovoGlycoTree[] trees = treeList.toArray(new DenovoGlycoTree[treeList.size()]);
		if (trees.length > 0) {
			Arrays.sort(trees, new DenovoGlycoTree.RevTreeScoreComparator());
		}
		/*
		 * System.out.println("tree\t" + trees.length + "\t"); for (int i = 0; i <
		 * trees.length; i++) { HashMap<Integer, GlycoPeakNode> nodemap =
		 * trees[i].getMap(); for (Integer iii : nodemap.keySet()) { GlycoPeakNode nnn =
		 * nodemap.get(iii); System.out.println("tree:" + i + "\t" +
		 * trees[i].getCharge() + "\t" + trees[i].getTotalScore() + "\t" + iii + "\t" +
		 * nnn.getPeakMz() + "\t" + nnn.getSingleScore() + "\t" + nnn.getDeltaMz() +
		 * "\t" + Arrays.toString(nnn.getComposition()) + "\t" +
		 * nnn.isMainTree()+"\t"+peaks[iii]); } }
		 */
		return trees;
	}

	/**
	 * now this method is used, before this method is executed, the
	 * removeFromPepSpectrum only contains oxonium ions
	 * 
	 * @param peaks
	 * @param precursorMz
	 * @param preCharge
	 * @param glycanMasses
	 * @param isGlycanPeak now 'true' represents oxonium ion
	 * @param oxoUpLimit
	 * @return
	 */
	DenovoGlycoTree[] getGlycoTreeList(int scannum, Peak[] peaks, double precursorMz, int preCharge,
			double[] glycanMasses, boolean[] removeFromPepSpectrum) {

		ArrayList<DenovoGlycoTree> treeList = new ArrayList<DenovoGlycoTree>();

		double upLimit = precursorMz * preCharge;
		double maxPeptideScore = 0;

		int maxPepIonsCount = 0;

		for (int charge = preCharge; charge >= 1; charge--) {

			GlycoPeakNode[] pns = new GlycoPeakNode[peaks.length];

			HashMap<Integer, PeptidePeakNode> pepNodeMap = new HashMap<Integer, PeptidePeakNode>();

			for (int i = 0; i < peaks.length; i++) {

				if (removeFromPepSpectrum[i])
					continue;

				if (pns[i] != null && pns[i].getMonoId() > 0) {
					continue;
				}

				double mzi = peaks[i].getMz();

				double fragTolerance;

				if (charge == 1) {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 1.5;
				} else if (charge == 2) {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 2.0;
				} else {
					fragTolerance = mzi * fragmentPPM * 1E-6 * 2.5;
				}

				boolean findTheChargeI = false;
				boolean findOtherChargeI = false;

				for (int ik = i + 1; ik < peaks.length; ik++) {

					if (removeFromPepSpectrum[ik])
						continue;

					if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
						break;
					}

					for (int chargei = 1; chargei <= preCharge; chargei++) {
						if (Math.abs(peaks[ik].getMz() - mzi
								- AminoAcidProperty.isotope_deltaM / (double) chargei) < fragTolerance
								&& peaks[ik].getIntensity() < peaks[i].getIntensity() * (chargei + 1.0)) {

							pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
							pns[ik].setMonoId(i);

							if (chargei == charge) {
								findTheChargeI = true;
							} else {
								findOtherChargeI = true;
							}
						}
					}
				}

				if (findTheChargeI) {
					for (int ik = i + 1; ik < peaks.length; ik++) {

						if (removeFromPepSpectrum[ik])
							continue;

						if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
							break;
						}

						for (int isoi = 1; isoi <= 5; isoi++) {
							if (Math.abs(peaks[ik].getMz() - mzi
									- AminoAcidProperty.isotope_deltaM * isoi / (double) charge) < fragTolerance
									&& peaks[ik].getIntensity() < peaks[i].getIntensity() * (charge + 1.0)) {

								pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
								pns[ik].setMonoId(i);
							}
						}
					}
				} else {
					if (findOtherChargeI) {
						continue;
					}
				}

				for (int j = i + 1; j < peaks.length; j++) {

					if (removeFromPepSpectrum[j])
						continue;

					if (pns[j] != null && pns[j].getMonoId() > 0) {
						continue;
					}

					double mzj = peaks[j].getMz();
					double delta = mzj - mzi;

					if (delta - glycanMasses[glycanMasses.length - 1] / (double) charge > fragTolerance) {
						break;
					}

					if ((mzj - fragTolerance) * charge > upLimit)
						break;

					boolean findTheChargeJ = false;
					boolean findOtherChargeJ = false;

					for (int jk = j + 1; jk < peaks.length; jk++) {
						if (removeFromPepSpectrum[jk])
							continue;

						if (peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * preCharge > fragTolerance) {
							break;
						}

						for (int chargej = 1; chargej <= preCharge; chargej++) {
							if (Math.abs(peaks[jk].getMz() - mzj
									- AminoAcidProperty.isotope_deltaM / (double) chargej) < fragTolerance
									&& peaks[jk].getIntensity() < peaks[j].getIntensity() * (chargej + 1.0)) {

								pns[jk] = new GlycoPeakNode(peaks[jk], jk, glycanMasses.length);
								pns[jk].setMonoId(j);

								if (chargej == charge) {
									findTheChargeJ = true;
								} else {
									findOtherChargeJ = true;
								}
							}
						}
					}

					if (findTheChargeJ) {
						for (int jk = j + 1; jk < peaks.length; jk++) {

							if (removeFromPepSpectrum[jk])
								continue;

							if (peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
								break;
							}

							for (int isoj = 1; isoj <= 5; isoj++) {
								if (Math.abs(peaks[jk].getMz() - mzj
										- AminoAcidProperty.isotope_deltaM * isoj / (double) charge) < fragTolerance
										&& peaks[jk].getIntensity() < peaks[j].getIntensity() * (charge + 1.0)) {

									pns[jk] = new GlycoPeakNode(peaks[jk], jk, glycanMasses.length);
									pns[jk].setMonoId(j);
								}
							}
						}
					} else {
						if (findOtherChargeJ) {
							continue;
						}
					}

					for (int k = 0; k < this.aminoacidMzs.length; k++) {
						double deltaMz = Math.abs(delta - aminoacidMzs[k] / (double) charge);
						if (deltaMz < fragTolerance) {

							PeptidePeakNode ppni = null;
							if (pepNodeMap.containsKey(i)) {
								ppni = pepNodeMap.get(i);
							} else {
								ppni = new PeptidePeakNode(peaks[i], i);
								pepNodeMap.put(i, ppni);
							}

							PeptidePeakNode ppnj = null;
							if (pepNodeMap.containsKey(j)) {
								ppnj = pepNodeMap.get(j);
							} else {
								ppnj = new PeptidePeakNode(peaks[j], j);
								pepNodeMap.put(j, ppnj);
							}

							if (ppni.getChild() != null) {
								PeptidePeakNode child = ppni.getChild();
								if (ppnj.getParent() == null) {
									if (ppnj.getRankScore() > child.getRankScore()) {
										ppnj.setParent(ppni);
									}
								}
							} else {
								if (ppnj.getParent() != null) {
									PeptidePeakNode parent = ppnj.getParent();
									if (ppni.getRankScore() > parent.getRankScore()) {
										ppnj.setParent(ppni);
									}
								} else {
									ppnj.setParent(ppni);
								}
							}
						}
					}

					for (int k = 0; k < glycanMasses.length; k++) {

						double deltaMz = Math.abs(delta - glycanMasses[k] / (double) charge);
						boolean findK = false;

						if (deltaMz < fragTolerance) {

							if (pns[i] == null)
								pns[i] = new GlycoPeakNode(peaks[i], i, glycanMasses.length);
							if (pns[j] == null)
								pns[j] = new GlycoPeakNode(peaks[j], j, glycanMasses.length);

							if (pns[j].getParent() == null) {
								if (pns[i].getChildren()[k] == null) {
									pns[j].setParent(pns[i], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setGlycanId(k);
									pns[j].setGlycanMass(glycanMasses[k]);

									pns[i].setCharge(charge);
									pns[j].setCharge(charge);

								} else {
									if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
										pns[j].setParent(pns[i], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setGlycanId(k);
										pns[j].setGlycanMass(glycanMasses[k]);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									}
								}
							} else {
								if (pns[i].getPeakIntensity() > pns[j].getParent().getPeakIntensity()) {
									GlycoPeakNode[] childrenj = pns[j].getParent().getChildren();
									for (int l = 0; l < childrenj.length; l++) {
										if (childrenj[l] == pns[j]) {
											childrenj[l] = null;
										}
									}
									if (pns[i].getChildren()[k] == null) {
										pns[j].setParent(pns[i], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setGlycanId(k);
										pns[j].setGlycanMass(glycanMasses[k]);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									} else {
										if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
											pns[j].setParent(pns[i], k);
											pns[j].setDeltaMz(deltaMz);
											pns[j].setGlycanId(k);
											pns[j].setGlycanMass(glycanMasses[k]);

											pns[i].setCharge(charge);
											pns[j].setCharge(charge);
										}
									}
								}
							}

							findK = true;
						}

						if (findK) {
							break;
						}
					}
				}
			}

			if (pepNodeMap.size() > maxPepIonsCount) {
				maxPepIonsCount = pepNodeMap.size();
			}

			PeptidePeakNode[] pepNodes = pepNodeMap.values().toArray(new PeptidePeakNode[pepNodeMap.size()]);

			for (int i = 0; i < pepNodes.length; i++) {
				PeptidePeakNode nodei = pepNodes[i];
				if (nodei.isRoot() && !nodei.isSingle()) {
					double score = nodei.getPeak().getRankScore();
					while (true) {
						if (nodei.getChild() == null) {
							break;
						} else {
							nodei = nodei.getChild();
							score += nodei.getPeak().getRankScore();
						}
					}

					if (score > maxPeptideScore) {
						maxPeptideScore = score;
					}
				}
			}

			for (int i = 0; i < pns.length; i++) {

				if (pns[i] == null)
					continue;

				if (pns[i].getMonoId() >= 0 && pns[pns[i].getMonoId()] != null) {
					if (pns[pns[i].getMonoId()].isRoot() || pns[pns[i].getMonoId()].getParent() != null) {
						removeFromPepSpectrum[i] = true;
						continue;
					}
				}

				if (pns[i].isRoot()) {

					DenovoGlycoTree tree = new DenovoGlycoTree(glycanMasses.length, charge, pns);
					add(tree, pns[i]);
					if (tree.getSize() >= 3) {

						tree.setMainStem();

						boolean use = true;
						GlycoPeakNode[] nodes = tree.getMap().values().toArray(new GlycoPeakNode[tree.getMap().size()]);
						for (int j = 0; j < nodes.length; j++) {
							if (nodes[j].isMainTree()) {
								if (nodes[j].getMonoId() != 0) {
									use = false;
									break;
								}
							}
						}

						if (use) {
							for (int j = 0; j < nodes.length; j++) {
								removeFromPepSpectrum[nodes[j].getPeakId()] = true;
							}
							treeList.add(tree);
						}
						
						System.out.println("tree 1146\t"+pns[i].getPeakMz()+"\t"+tree.getSize()+"\t"+use+"\t"+tree.getTotalScore());
						
					}
					
					
				}
			}
		}

		DenovoGlycoTree[] trees = treeList.toArray(new DenovoGlycoTree[treeList.size()]);
		if (trees.length > 0) {
			Arrays.sort(trees, new DenovoGlycoTree.RevTreeScoreComparator());
			this.glycanTreeScanSet.add(scannum);
		} else {
			if (maxPepIonsCount > 4) {
				this.pepScanSet.add(scannum);
			} else {
				this.noPepGlycanScanSet.add(scannum);
			}
		}

		return trees;
	}

	/**
	 * get the GSM list from the detected glyco trees
	 * 
	 * @param scannum
	 * @param tree
	 * @param precursorMz
	 * @param precursorCharge
	 * @param peaks
	 * @param oxoniumGlycoMap
	 * @param oxoniumIonMap
	 * @param glycans
	 * @param hasNeuAc
	 * @return
	 */
	ArrayList<NovoGlycoCompMatch> match(int scannum, DenovoGlycoTree tree, double precursorMz, int precursorCharge,
			Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, boolean hasNeuAc) {

		double[] glycanMasses = glycans.getSortedMasses();
		double[] combineGlycanMasses = glycans.getCombineGlycosylMasses();
		ArrayList<NovoGlycoCompMatch> matchList = new ArrayList<NovoGlycoCompMatch>();

		double tolerance = precursorMz * precursorCharge * precursorPPM * 1E-6;

		double difference = (precursorMz - AminoAcidProperty.PROTON_W) * precursorCharge
				- tree.getLastGPMass(glycanMasses);
		int[] basicComposition = tree.getLastComposition();
		if (difference > -tolerance) {
			if (difference < tolerance) {
				NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition, scannum, tree,
						precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, true);
				String comp = match.getComString();
				if (!hasNeuAc && comp.contains(Glycosyl.NeuAc.getTitle())) {

				} else {
					matchList.add(match);
				}

			} else {
				if (difference < combineGlycanMasses[combineGlycanMasses.length - 1]) {
					for (int i = 0; i < combineGlycanMasses.length; i++) {
						// System.out.println("773\t"+difference+"\t"+combineGlycanMasses[i]+"\t"+Arrays.toString(basicComposition)+"\t"+
						// tree.getLastGPMass(glycanMasses)+"\t"+(precursorMz -
						// AminoAcidProperty.PROTON_W) * precursorCharge);
						if (Math.abs(difference - combineGlycanMasses[i]) < tolerance) {

							NovoGlycoCompMatch match = this.judge(difference - combineGlycanMasses[i], tolerance,
									basicComposition, scannum, tree, precursorMz, precursorCharge, peaks,
									oxoniumGlycoMap, oxoniumIonMap, glycans, combineGlycanMasses[i]);

							matchList.add(match);
						}
					}
				}
			}
		}

		if (matchList.size() == 0) {
			HashMap<String, NovoGlycoCompMatch> matchMap = new HashMap<String, NovoGlycoCompMatch>();
			HashMap<Integer, Double> idmap = new HashMap<Integer, Double>();
			HashMap<Integer, GlycoPeakNode> nodemap = tree.getMap();

			L: for (Integer id : nodemap.keySet()) {

				GlycoPeakNode node = nodemap.get(id);
				if (node.isRoot())
					continue;

				int[] childrenIds = node.getChildrenIds();
				for (int i = 0; i < childrenIds.length; i++) {
					if (idmap.containsKey(childrenIds[i])) {
						continue L;
					}
				}
				double mw = node.getMw();
				double delta = (precursorMz - AminoAcidProperty.PROTON_W) * precursorCharge - mw;

				if (delta > -tolerance) {
					if (delta < tolerance) {
						NovoGlycoCompMatch match = this.judge(delta, tolerance, node.getComposition(), scannum, tree,
								precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, true);
						String comp = match.getComString();
						if (!hasNeuAc && comp.contains(Glycosyl.NeuAc.getTitle())) {
							continue;
						}

						matchMap.put(match.getComString(), match);

					} else {
						if (delta < combineGlycanMasses[combineGlycanMasses.length - 1]) {
							for (int i = 0; i < combineGlycanMasses.length; i++) {

								// System.out.println("788\t"+mw+"\t"+delta+"\t"+combineGlycanMasses[i]);

								if (Math.abs(delta - combineGlycanMasses[i]) < tolerance) {
									idmap.put(id, combineGlycanMasses[i]);
									if (idmap.containsKey(node.getParentPeakId())) {
										idmap.remove(node.getParentPeakId());
									}
									break;
								}
							}
						}
					}
				}
			}

			Iterator<Integer> it = idmap.keySet().iterator();
			while (it.hasNext()) {
				Integer id = it.next();
				GlycoPeakNode node = nodemap.get(id);
				double mw = node.getMw();
				double delta = (precursorMz - AminoAcidProperty.PROTON_W) * precursorCharge - mw;

				NovoGlycoCompMatch match = this.judge(delta, tolerance, node.getComposition(), scannum, tree,
						precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, idmap.get(id));

				String key = match.getComString();
				if (!hasNeuAc && key.contains(Glycosyl.NeuAc.getTitle())) {
					continue;
				}

				if (matchMap.containsKey(key)) {
					if (match.getScore() > matchMap.get(key).getScore()) {
						matchMap.put(key, match);
					} else if (match.getScore() == matchMap.get(key).getScore()) {
						if (match.getDeltaMass() < matchMap.get(key).getDeltaMass()) {
							matchMap.put(key, match);
						}
					}
				} else {
					matchMap.put(key, match);
				}
			}
			matchList.addAll(matchMap.values());
		}

		if (matchList.size() == 0) {

			if (difference > tolerance) {
				// HashMap<Integer, GlycoPeakNode> nodemap2 = new HashMap<Integer,
				// GlycoPeakNode>();
				// nodemap2.putAll(tree.getMap());
				// nodemap2.putAll(oxoniumIonMap);
				NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition, scannum, tree,
						precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, false);
				matchList.add(match);

				// NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum,
				// precursorMz, precursorCharge,
				// basicComposition, glycans.getGlycanName(basicComposition),
				// glycans.getGlycanMass(basicComposition), difference, score,
				// peaks, nodemap2, false);
				// System.out.println("cao3\t"+score);
				// unknownMatchList.add(match);
			}

			return matchList;

		} else {
			return matchList;
		}
	}

	/**
	 * 
	 * @param ms2spectrumi
	 * @param monoMz
	 * @param charge
	 * @return
	 */
	abstract NovoGlycoCompMatch[] parseGSM(Spectrum ms2spectrumi, double monoMz, int charge);

	/**
	 * 
	 * @param delta
	 * @param tolerance
	 * @param basicComposition
	 * @param scannum
	 * @param tree
	 * @param precursorMz
	 * @param precursorCharge
	 * @param peaks
	 * @param oxoniumGlycoMap
	 * @param oxoniumIonMap
	 * @param glycans
	 * @param determined
	 * @return
	 */
	abstract NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum,
			DenovoGlycoTree tree, double precursorMz, int precursorCharge, Peak[] peaks,
			HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap, HashMap<Integer, GlycoPeakNode> oxoniumIonMap,
			MicroMonosaccharides glycans, boolean determined);

	/**
	 * 
	 * @param delta
	 * @param tolerance
	 * @param basicComposition
	 * @param scannum
	 * @param tree
	 * @param precursorMz
	 * @param precursorCharge
	 * @param peaks
	 * @param oxoniumGlycoMap
	 * @param oxoniumIonMap
	 * @param glycans
	 * @param combineGlycanMass
	 * @return
	 */
	abstract NovoGlycoCompMatch judge(double deltaMass, double tolerance, int[] basicComposition, int scannum,
			DenovoGlycoTree tree, double precursorMz, int precursorCharge, Peak[] peaks,
			HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap, HashMap<Integer, GlycoPeakNode> oxoniumIonMap,
			MicroMonosaccharides glycans, double combineGlycanMass);

	void findIsotope(Peak[] peaks, int id, int charge, HashSet<Integer> isotopeSet) {
		double fragTolerance = charge > 2 ? peaks[id].getMz() * fragmentPPM * 1E-6 * 1.5
				: peaks[id].getMz() * fragmentPPM * 1E-6;
		for (int i = id + 1; i < peaks.length; i++) {
			double delta = peaks[i].getMz() - peaks[id].getMz();
			if (Math.abs(delta * charge - AminoAcidProperty.isotope_deltaM) < fragTolerance) {
				isotopeSet.add(i);
				findIsotope(peaks, i, charge, isotopeSet);
			} else if (delta * charge - AminoAcidProperty.isotope_deltaM > fragTolerance) {
				break;
			}
		}
	}

	void add(DenovoGlycoTree tree, GlycoPeakNode root) {
		tree.addNode(root);
		GlycoPeakNode[] nodes = root.getChildren();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				add(tree, nodes[i]);
			}
		}
	}

	public void instanceValue(NovoGlycoCompMatch match) {

		Peak[] peaks = match.getPeaks();
		HashMap<Integer, GlycoPeakNode> nodemap = match.getNodeMap();

		System.out.println("Score\t" + "Factor\t" + "Charge\t" + "peaks.length\tnodemap.size()");
		System.out.println(match.getScore() + "\t" + match.getFactor() + "\t" + match.getPreCharge() + "\t"
				+ peaks.length + "\t" + nodemap.size());

		int fragmentCharge = -1;
		double totalIntensity = 0;
		double max = 0;
		double min = 1;
		double maxDeltaMz = 0;
		double[] scoreList = new double[nodemap.size()];
		double[] massDifference = new double[nodemap.size()];
		int scoreid = 0;

		for (Integer id : nodemap.keySet()) {
			GlycoPeakNode node = nodemap.get(id);
			totalIntensity += node.getPeakIntensity();
			fragmentCharge = node.getCharge();
			double nodeScore = node.getSingleScore();
			double nodeDeltaMz = node.getDeltaMz();
			if (nodeScore > max) {
				max = nodeScore;
			}
			if (nodeScore < min) {
				min = nodeScore;
			}
			if (nodeDeltaMz > maxDeltaMz) {
				maxDeltaMz = nodeDeltaMz;
			}
			scoreList[scoreid] = nodeScore;
			massDifference[scoreid] = node.getDeltaMz();
			scoreid++;
		}

		double totalPeakIntensity = 0;
		for (int i = 0; i < peaks.length; i++) {
			totalPeakIntensity += peaks[i].getIntensity();
		}

		System.out.println("nodeCountRatioAtt\tnodeIntensityRatioAtt\tmissNodeCountAtt\t");
		System.out.println((double) nodemap.size() / (double) peaks.length + "\t"
				+ (totalIntensity / totalPeakIntensity) + "\t" + match.getMissNodeCount());

		double medianDelta = MathTool.getMedian(massDifference);
		double aveDelta = MathTool.getAve(massDifference);

		System.out.println("fragmentChargeAtt\tdeltaMzMaxAtt\tdeltaAverageAtt\tdeltaMedianAtt");
		System.out.println(fragmentCharge + "\t" + maxDeltaMz + "\t" + aveDelta + "\t" + medianDelta);

		double median = MathTool.getMedian(scoreList);
		double average = MathTool.getAve(scoreList);
		double stdev = MathTool.getStdDev(scoreList);

		System.out.println(
				"intensityMaxAtt\tintensityMinAtt\tintensityMedianAtt\tintensityAverageAtt\tintensityStdevAtt");
		System.out.println(max + "\t" + min + "\t" + median + "\t" + average + "\t" + stdev);

		HashMap<Integer, GlycoPeakNode> oxoniumMap = match.getOxoniumMap();
		double maxOxonium = 0;
		double minOxonium = 1;
		double oxoIntensity = 0;
		double oxoScore = 0;
		for (Integer id : oxoniumMap.keySet()) {
			GlycoPeakNode node = oxoniumMap.get(id);
			double nodeScore = node.getSingleScore();
			if (nodeScore > maxOxonium) {
				maxOxonium = nodeScore;
			}
			if (nodeScore < minOxonium) {
				minOxonium = nodeScore;
			}
			oxoIntensity += node.getPeakIntensity();
			oxoScore += node.getSingleScore();
		}

		System.out.println("oxoniumMaxAtt\toxoniumMinAtt\toxoniumCountAtt");
		System.out.println(maxOxonium + "\t" + minOxonium + "\t" + oxoniumMap.size());

		HashMap<Integer, GlycoPeakNode> missOxoniumMap = match.getMissOxoniumMap();
		double missMaxOxonium = 0;
		double missMinOxonium = 1;
		double missOxoIntensity = 0;
		double missOxoScore = 0;
		for (Integer id : missOxoniumMap.keySet()) {
			GlycoPeakNode node = missOxoniumMap.get(id);
			double nodeScore = node.getSingleScore();
			if (nodeScore > missMaxOxonium) {
				missMaxOxonium = nodeScore;
			}
			if (nodeScore < missMinOxonium) {
				missMinOxonium = nodeScore;
			}
			missOxoIntensity += node.getPeakIntensity();
			missOxoScore += node.getSingleScore();
		}

		System.out.println("missOxoniumMaxAtt\tmissOxoniumMinAtt\tmissOxoniumCountAtt");
		System.out.println(missMaxOxonium + "\t" + missMinOxonium + "\t" + missOxoniumMap.size());

		System.out.println("oxoniumCountRatio\toxoniumIntensityRatio\toxoniumScoreRatio");
		System.out.println(((double) oxoniumMap.size() - (double) missOxoniumMap.size()) + "\t"
				+ (oxoIntensity - missOxoIntensity) + "\t" + (oxoScore - missOxoScore));

		int glycoCount = 0;
		int[] composition = match.getComposition();
		for (int i = 0; i < composition.length; i++) {
			glycoCount += composition[i];
		}

		System.out.println("glycanCountAtt");
		System.out.println(glycoCount);
	}

	public File[] exportPepSpectra(String rawfile, String output) throws IOException {

		File[] files = new File[] { new File(output, rawfile + "_1.mgf"), new File(output, rawfile + "_2.mgf"),
				new File(output, rawfile + "_3.mgf"), new File(output, rawfile + "_4.mgf") };

		MgfWriter noOxoniumWriter = new MgfWriter(files[0]);
		MgfWriter glycanTreeWriter = new MgfWriter(files[1]);
		MgfWriter pepWriter = new MgfWriter(files[2]);
		MgfWriter noPepGlycanWriter = new MgfWriter(files[3]);

		
		System.out.println("abstract1531\t"+this.noOxoniumScanSet.size()+"\t"+this.glycanTreeScanSet.size()+"\t"+this.pepScanSet.size());
		/*
		 * PrintWriter noOxoniumWriter = new PrintWriter(files[0]); PrintWriter
		 * glycanTreeWriter = new PrintWriter(files[1]); PrintWriter pepWriter = new
		 * PrintWriter(files[2]); PrintWriter noPepGlycanWriter = new
		 * PrintWriter(files[3]);
		 */
		for (Integer scannum : this.pepSpectraMap.keySet()) {

			double[] premz = this.pepSpectraPPMap.get(scannum);
			Peak[] peaks = this.pepSpectraMap.get(scannum);
			Spectrum spectrum = ms2SpectraList.getScan(scannum);
			spectrum.setPrecursorMz(premz[1]);
			spectrum.setPeaks(peaks);

			if (this.noOxoniumScanSet.contains(scannum)) {
				noOxoniumWriter.writeMSCFormat(spectrum, rawfile);
//				noOxoniumWriter.println(scannum);
			}

			if (this.glycanTreeScanSet.contains(scannum)) {
				glycanTreeWriter.writeMSCFormat(spectrum, rawfile);
//				glycanTreeWriter.println(scannum);
			}

			if (this.pepScanSet.contains(scannum)) {
				pepWriter.writeMSCFormat(spectrum, rawfile);
//				pepWriter.println(scannum);
			}

			if (this.noOxoniumScanSet.contains(scannum)) {
				noPepGlycanWriter.writeMSCFormat(spectrum, rawfile);
//				noPepGlycanWriter.println(scannum);
			}
		}

		noOxoniumWriter.close();
		glycanTreeWriter.close();
		pepWriter.close();
		noPepGlycanWriter.close();

		return files;
	}

	public HashMap<Integer, double[]> getPepSpectraPPMap() {
		return pepSpectraPPMap;
	}

}
