package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.aminoacid.Aminoacids;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoTree;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

/**
 * the spectra was from the released glycan part, not the intact glycopeptide
 * 
 * @author Kai Cheng <ckkazuma@gmail.com>
 *
 */
public class GlycoOnlyGlycanParser extends AbstractGlycoParser {

	private static final Logger LOGGER = LogManager.getLogger(GlycoOnlyGlycanParser.class);

	private File spectraInput;

	private MicroMonosaccharides glycans;
	private Glycosyl[] glycosyls;
	private double[] glycanMasses;
	private double[] oxoniumMasses;
	private double[] combineGlycanMasses;

	private MzxmlReader reader;

	public GlycoOnlyGlycanParser(String file, Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM) {
		this(new File(file), glycosyls, precursorPPM, fragmentPPM);
	}

	public GlycoOnlyGlycanParser(File file, Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM) {

		try {
			this.reader = new MzxmlReader(file);
		} catch (IOException | XMLStreamException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in parsing spectra from " + spectraInput, e);
		}
		this.glycosyls = glycosyls;
		this.glycans = new MicroMonosaccharides(glycosyls);
		this.glycanMasses = glycans.getSortedMasses();
		this.oxoniumMasses = glycans.getOxoniumMasses();
		this.combineGlycanMasses = glycans.getCombineGlycosylMasses();
		this.combineGlycans = glycans.getCombineCompositions();

		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;

		this.preprocess();
	}

	public GlycoOnlyGlycanParser(SpectraList ms1SpectraList, SpectraList ms2SpectraList, Glycosyl[] glycosyls,
			double precursorPPM, double fragmentPPM) {

		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;

		this.glycosyls = glycosyls;
		this.glycans = new MicroMonosaccharides(glycosyls);
		this.glycanMasses = glycans.getSortedMasses();
		this.oxoniumMasses = glycans.getOxoniumMasses();
		this.combineGlycanMasses = glycans.getCombineGlycosylMasses();
		this.combineGlycans = glycans.getCombineCompositions();

		this.ms1SpectraList = ms1SpectraList;
		this.ms2SpectraList = ms2SpectraList;

		this.ms2scans = ms2SpectraList.getScanList();
		this.featuresList = new ArrayList<Features>();
		this.feasIdMap = new HashMap<Integer, Integer>();
		this.determinedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.undeterminedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.usedRank2Scans = new HashSet<Integer>();

		ArrayList<Double> aaslist = new ArrayList<Double>();
		Aminoacids aas = new Aminoacids();
		for (int i = 'A'; i <= 'Z'; i++) {
			if (i == 'B' || i == 'J' || i == 'O' || i == 'U' || i == 'X' || i == 'Z') {
				continue;
			}
			aaslist.add(aas.get(i).getMonoMass());
		}
		this.aminoacidMzs = new double[aaslist.size()];
		for (int i = 0; i < this.aminoacidMzs.length; i++) {
			this.aminoacidMzs[i] = aaslist.get(i);
		}
	}

	public static String getTitle(String delimiter) {
		StringBuilder sb = new StringBuilder();
		sb.append("Scan number").append(delimiter);
		sb.append("Precursor mz").append(delimiter);
		sb.append("Charge").append(delimiter);
		sb.append("Composition (name)").append(delimiter);
		sb.append("Composition (count)").append(delimiter);
		
		sb.append("Glycan mass").append(delimiter);
		sb.append("Delta mass").append(delimiter);
		sb.append("Score").append(delimiter);
		sb.append("Rank").append(delimiter);
		sb.append("Peaks").append(delimiter);
		sb.append("Matched peaks").append(delimiter);
		sb.append("Oxonium ions").append(delimiter);
		
		return sb.toString();
	}
	
	private void preprocess() {

		LOGGER.info("Pre-processing start");

		this.ms1SpectraList = new SpectraList();
		this.ms2SpectraList = new SpectraList();

		Spectrum spectrum;
		try {
			while ((spectrum = reader.getNextSpectrum()) != null) {
				int level = spectrum.getMslevel();
				if (level == 1) {
					ms1SpectraList.addSpectrum(spectrum);
				} else if (level == 2) {

					peakFilter.parseRank(spectrum.getPeaks());
					spectrum.setPeaks(peakFilter.filter(spectrum.getPeaks()));
					ms2SpectraList.addSpectrum(spectrum);
				}
			}
			reader.close();

		} catch (XMLStreamException | DataFormatException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in parsing spectra from " + spectraInput, e);
		}

		ms1SpectraList.complete();
		ms2SpectraList.complete();

		this.ms2scans = ms2SpectraList.getScanList();
		this.featuresList = new ArrayList<Features>();
		this.feasIdMap = new HashMap<Integer, Integer>();
		this.determinedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.undeterminedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.usedRank2Scans = new HashSet<Integer>();

		this.pepSpectraMap = new HashMap<Integer, Peak[]>();
		this.pepSpectraPPMap = new HashMap<Integer, double[]>();
		
		ArrayList<Double> aaslist = new ArrayList<Double>();
		Aminoacids aas = new Aminoacids();
		for (int i = 'A'; i <= 'Z'; i++) {
			if (i == 'B' || i == 'J' || i == 'O' || i == 'U' || i == 'X' || i == 'Z') {
				continue;
			}
			aaslist.add(aas.get(i).getMonoMass());
		}
		this.aminoacidMzs = new double[aaslist.size()];
		for (int i = 0; i < this.aminoacidMzs.length; i++) {
			this.aminoacidMzs[i] = aaslist.get(i);
		}

		LOGGER.info("Pre-processing finished");
	}

	public Glycosyl[] getAllGlycosyls() {
		return this.glycosyls;
	}
	
	NovoGlycoCompMatch[] parseGSM(Spectrum spectrum, double monoMz, int charge) {

		Peak[] peaks = spectrum.getPeaks();
		HashMap<Integer, GlycoPeakNode> oxoniumIonMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap = new HashMap<Integer, ArrayList<Integer>>();

		DenovoGlycoTree[] trees = this.getGlycoTreeList(peaks, monoMz, charge, glycanMasses, oxoniumIonMap);

		if (trees.length == 0)
			return null;

		int[][] repeatCounts = new int[trees.length][trees.length];

		HashSet<Integer> usedset = new HashSet<Integer>();
		for (int i = 0; i < trees.length; i++) {

			double[] mzsi = trees[i].getPeakMzList();
			int chargei = trees[i].getCharge();
			for (int j = 0; j < trees.length; j++) {

				if (j == i)
					continue;

				double[] mzsj = trees[j].getPeakMzList();
				int chargej = trees[j].getCharge();
				if (chargei == chargej) {
					for (int k1 = 0; k1 < mzsj.length; k1++) {
						for (int k2 = 0; k2 < mzsi.length; k2++) {
							if (Math.abs(mzsi[k2] - mzsj[k1] - h2o / (double) chargei) < mzsj[k1] * fragmentPPM
									* 1E-6) {
								repeatCounts[i][j]++;
								break;
							}
						}
					}
				} else {

					if (usedset.contains(i))
						continue;

					for (int k1 = 0; k1 < mzsj.length; k1++) {
						for (int k2 = 0; k2 < mzsi.length; k2++) {
							if (Math.abs(mzsj[k1] * chargej - mzsi[k2] * chargei
									- (chargej - chargei) * AminoAcidProperty.isotope_deltaM) < mzsj[k1] * fragmentPPM
											* 1E-6 * chargej) {
								repeatCounts[i][j]++;
								break;
							}
						}
					}
				}
			}
		}

		HashMap<String, NovoGlycoCompMatch> matchMap = new HashMap<String, NovoGlycoCompMatch>();
		NovoGlycoCompMatch[][] tempMatches = new NovoGlycoCompMatch[trees.length][];
		boolean[][] removeMatches = new boolean[tempMatches.length][];

		for (int i = 0; i < trees.length; i++) {
			ArrayList<NovoGlycoCompMatch> templist = this.match(spectrum.getScannum(), trees[i], monoMz, charge, peaks,
					oxoniumGlycoMap, oxoniumIonMap, glycans, false);
			if (templist != null) {
				tempMatches[i] = templist.toArray(new NovoGlycoCompMatch[templist.size()]);
				removeMatches[i] = new boolean[tempMatches[i].length];
				Arrays.fill(removeMatches[i], false);
			}
		}

		for (int i = 0; i < tempMatches.length; i++) {
			for (int j = i + 1; j < tempMatches.length; j++) {
				for (int ki = 0; ki < tempMatches[i].length; ki++) {
					for (int kj = 0; kj < tempMatches[j].length; kj++) {
						if (tempMatches[i][ki] != null && tempMatches[j][kj] != null) {
							int[] compi = tempMatches[i][ki].getComposition();
							int[] compj = tempMatches[j][kj].getComposition();
							int maxi = 0;
							int maxj = 0;
							for (int kk = 0; kk < compi.length; kk++) {
								if (compi[kk] > compj[kk]) {
									maxi++;
								} else if (compi[kk] < compj[kk]) {
									maxj++;
								}
							}

							if (maxi == 0) {
								if (repeatCounts[i][j] > 0) {
									removeMatches[i][ki] = true;
								}
							} else {
								if (maxj == 0) {
									if (repeatCounts[i][j] > 0) {
										removeMatches[j][kj] = true;
									}
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < tempMatches.length; i++) {
			for (int j = 0; j < tempMatches[i].length; j++) {
				NovoGlycoCompMatch match = tempMatches[i][j];
				if (match != null) {
					if (!removeMatches[i][j]) {
						String key = match.getComString();
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
				}
			}
		}

		if (matchMap.size() == 0)
			return null;

		NovoGlycoCompMatch[] matches = matchMap.values().toArray(new NovoGlycoCompMatch[matchMap.size()]);
		Arrays.sort(matches, new NovoGlycoCompMatch.ScoreComparator());

		for (int i = 0; i < matches.length; i++) {
			matches[i].setRank(i + 1);
		}

		if (matches.length <= maxN) {
			return matches;
		} else {
			NovoGlycoCompMatch[] partMatches = new NovoGlycoCompMatch[maxN];
			System.arraycopy(matches, 0, partMatches, 0, maxN);
			return partMatches;
		}
	}

	/**
	 * this method is derived from the glycopeptide search, which may be not fit for the only glycan identification
	 */
	NovoGlycoCompMatch[] parseGSMNotUsed(Spectrum spectrum, double monoMz, int charge) {

		Peak[] peaks = spectrum.getPeaks();
		HashMap<Integer, GlycoPeakNode> oxoniumIonMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap = new HashMap<Integer, ArrayList<Integer>>();

		boolean hasNeuAc = false;
		int oxoId = 0;
		for (int i = 0; i < peaks.length; i++) {

			double oxoniumTolerance = peaks[i].getMz() * fragmentPPM * 1E-6;
			for (; oxoId < oxoniumMasses.length;) {
				if (peaks[i].getMz() - oxoniumMasses[oxoId] > oxoniumTolerance) {
					oxoId++;
				} else if (oxoniumMasses[oxoId] - peaks[i].getMz() > oxoniumTolerance) {
					break;
				} else {

					GlycoPeakNode gpn = new GlycoPeakNode(peaks[i], i, glycans.getGlycoTypeCount());
					gpn.setOxonium(true);
					gpn.setCharge(1);
					ArrayList<Integer> glist = new ArrayList<Integer>();
					int[] comp = new int[glycosyls.length];
					oxoniumIonMap.put(i, gpn);
					oxoniumGlycoMap.put(i, glist);
					Glycosyl[] glycosyls = glycans.getGlycoFromOxo(oxoniumMasses[oxoId]);
					for (int j = 0; j < glycosyls.length; j++) {
					
						double gmj = Double.parseDouble(FormatTool.getDF4().format(glycosyls[j].getMonoMass()));
						for (int k = 0; k < glycanMasses.length; k++) {
							if (gmj == glycanMasses[k]) {
								glist.add(k);
								comp[k]++;
							}
						}
						if (glycosyls[j] == Glycosyl.NeuAc || glycosyls[j] == Glycosyl.NeuAc_H2O) {
							hasNeuAc = true;
						}
					}
					gpn.setComposition(comp);
					oxoId++;
					break;
				}
			}
		}

		DenovoGlycoTree[] trees = this.getGlycoTreeList(peaks, monoMz, charge, glycanMasses, oxoniumIonMap);

		if (trees.length == 0)
			return null;

		int[][] repeatCounts = new int[trees.length][trees.length];

		HashSet<Integer> usedset = new HashSet<Integer>();
		for (int i = 0; i < trees.length; i++) {
			double[] mzsi = trees[i].getPeakMzList();
			int chargei = trees[i].getCharge();
			for (int j = 0; j < trees.length; j++) {

				if (j == i)
					continue;

				double[] mzsj = trees[j].getPeakMzList();
				int chargej = trees[j].getCharge();
				if (chargei == chargej) {
					for (int k1 = 0; k1 < mzsj.length; k1++) {
						for (int k2 = 0; k2 < mzsi.length; k2++) {
							if (Math.abs(mzsi[k2] - mzsj[k1] - h2o / (double) chargei) < mzsj[k1] * fragmentPPM
									* 1E-6) {
								repeatCounts[i][j]++;
								break;
							}
						}
					}
				} else {

					if (usedset.contains(i))
						continue;

					for (int k1 = 0; k1 < mzsj.length; k1++) {
						for (int k2 = 0; k2 < mzsi.length; k2++) {
							if (Math.abs(mzsj[k1] * chargej - mzsi[k2] * chargei
									- (chargej - chargei) * AminoAcidProperty.isotope_deltaM) < mzsj[k1] * fragmentPPM
											* 1E-6 * chargej) {
								repeatCounts[i][j]++;
								break;
							}
						}
					}
				}
			}
		}

		HashMap<String, NovoGlycoCompMatch> matchMap = new HashMap<String, NovoGlycoCompMatch>();
		NovoGlycoCompMatch[][] tempMatches = new NovoGlycoCompMatch[trees.length][];
		boolean[][] removeMatches = new boolean[tempMatches.length][];

		for (int i = 0; i < trees.length; i++) {
			ArrayList<NovoGlycoCompMatch> templist = this.match(spectrum.getScannum(), trees[i], monoMz, charge, peaks,
					oxoniumGlycoMap, oxoniumIonMap, glycans, hasNeuAc);
			if (templist != null) {
				tempMatches[i] = templist.toArray(new NovoGlycoCompMatch[templist.size()]);
				removeMatches[i] = new boolean[tempMatches[i].length];
				Arrays.fill(removeMatches[i], false);
			}
		}

		for (int i = 0; i < tempMatches.length; i++) {
			for (int j = i + 1; j < tempMatches.length; j++) {
				for (int ki = 0; ki < tempMatches[i].length; ki++) {
					for (int kj = 0; kj < tempMatches[j].length; kj++) {
						if (tempMatches[i][ki] != null && tempMatches[j][kj] != null) {
							int[] compi = tempMatches[i][ki].getComposition();
							int[] compj = tempMatches[j][kj].getComposition();
							int maxi = 0;
							int maxj = 0;
							for (int kk = 0; kk < compi.length; kk++) {
								if (compi[kk] > compj[kk]) {
									maxi++;
								} else if (compi[kk] < compj[kk]) {
									maxj++;
								}
							}

							if (maxi == 0) {
								if (repeatCounts[i][j] > 0) {
									removeMatches[i][ki] = true;
								}
							} else {
								if (maxj == 0) {
									if (repeatCounts[i][j] > 0) {
										removeMatches[j][kj] = true;
									}
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < tempMatches.length; i++) {
			for (int j = 0; j < tempMatches[i].length; j++) {
				NovoGlycoCompMatch match = tempMatches[i][j];
				if (match != null) {
					if (!removeMatches[i][j]) {
						String key = match.getComString();
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
				}
			}
		}

		if (matchMap.size() == 0)
			return null;

		NovoGlycoCompMatch[] matches = matchMap.values().toArray(new NovoGlycoCompMatch[matchMap.size()]);
		Arrays.sort(matches, new NovoGlycoCompMatch.ScoreComparator());

		for (int i = 0; i < matches.length; i++) {
			matches[i].setRank(i + 1);
		}

		if (matches.length <= maxN) {
			return matches;
		} else {
			NovoGlycoCompMatch[] partMatches = new NovoGlycoCompMatch[maxN];
			System.arraycopy(matches, 0, partMatches, 0, maxN);
			return partMatches;
		}
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

		double upLimit = precursorMz * preCharge;

		GlycoPeakNode[] pns = new GlycoPeakNode[peaks.length];

		for (int i = 0; i < peaks.length; i++) {

			if (pns[i] != null && pns[i].getMonoId() > 0) {
				continue;
			}

			double mzi = peaks[i].getMz();
			double fragTolerance = mzi * fragmentPPM * 1E-6 * 1.5;

			// find the isotope peaks
			for (int ik = i + 1; ik < peaks.length; ik++) {

				if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
					break;
				}

				if (Math.abs(peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM) < fragTolerance) {

					pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
					pns[ik].setMonoId(i);
				}
			}

			// find the isotope peaks
			for (int ik = i + 1; ik < peaks.length; ik++) {

				if (peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
					break;
				}

				for (int isoi = 1; isoi <= 5; isoi++) {
					if (Math.abs(peaks[ik].getMz() - mzi - AminoAcidProperty.isotope_deltaM * isoi) < fragTolerance) {

						pns[ik] = new GlycoPeakNode(peaks[ik], ik, glycanMasses.length);
						pns[ik].setMonoId(i);
					}
				}
			}

			for (int j = i + 1; j < peaks.length; j++) {

				if (pns[j] != null && pns[j].getMonoId() > 0) {
					continue;
				}

				double mzj = peaks[j].getMz();
				double delta = mzj - mzi;

				if (delta - glycanMasses[glycanMasses.length - 1] > fragTolerance) {
					break;
				}

				if (mzj - fragTolerance > upLimit)
					break;

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
						}
					}
				}

				for (int jk = j + 1; jk < peaks.length; jk++) {

					if (peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * 7 > fragTolerance) {
						break;
					}

					for (int isoj = 1; isoj <= 5; isoj++) {
						if (Math.abs(
								peaks[jk].getMz() - mzj - AminoAcidProperty.isotope_deltaM * isoj) < fragTolerance) {

							pns[jk] = new GlycoPeakNode(peaks[jk], jk, glycanMasses.length);
							pns[jk].setMonoId(j);
						}
					}
				}

				for (int k = 0; k < glycanMasses.length; k++) {

					double deltaMz = Math.abs(delta - glycanMasses[k]);
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

								pns[i].setCharge(1);
								pns[j].setCharge(1);

							} else {
								if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
									pns[j].setParent(pns[i], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setGlycanId(k);
									pns[j].setGlycanMass(glycanMasses[k]);

									pns[i].setCharge(1);
									pns[j].setCharge(1);
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

									pns[i].setCharge(1);
									pns[j].setCharge(1);
								} else {
									if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
										pns[j].setParent(pns[i], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setGlycanId(k);
										pns[j].setGlycanMass(glycanMasses[k]);

										pns[i].setCharge(1);
										pns[j].setCharge(1);
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

		L: for (int i = 0; i < pns.length; i++) {

			if (pns[i] == null)
				continue;

			if (pns[i].isRoot() && pns[i].getMonoId() == 0) {

				double oxoniumTolerance = pns[i].getPeakMz() * fragmentPPM * 1E-6;
				int[] comp = null;
				for (int j = 0; j < oxoniumMasses.length; j++) {
					if (Math.abs(pns[i].getPeakMz() - oxoniumMasses[j]) < oxoniumTolerance) {
						comp = new int[glycosyls.length];
						Glycosyl[] glycosyls = glycans.getGlycoFromOxo(oxoniumMasses[j]);
						for (int k = 0; k < glycosyls.length; k++) {

							double gmj = Double.parseDouble(FormatTool.getDF4().format(glycosyls[k].getMonoMass()));
							for (int l = 0; l < glycanMasses.length; l++) {
								if (gmj == glycanMasses[l]) {
									comp[l]++;
								}
							}
						}
						break;
					}
				}

				if (comp == null) {
					continue;
				}

				DenovoGlycoTree tree = new DenovoGlycoTree(glycanMasses.length, 1, pns);
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

		DenovoGlycoTree[] trees = treeList.toArray(new DenovoGlycoTree[treeList.size()]);
		if (trees.length > 0) {
			Arrays.sort(trees, new DenovoGlycoTree.RevTreeScoreComparator());
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

				double difference2 = (precursorMz - AminoAcidProperty.PROTON_W) * precursorCharge - tree.getGlycoMass();

				if (difference2 < combineGlycanMasses[combineGlycanMasses.length - 1]) {
					for (int i = 0; i < combineGlycanMasses.length; i++) {
						if (Math.abs(difference2 - combineGlycanMasses[i]) < tolerance) {

							NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition, scannum,
									tree, precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans,
									combineGlycans.get(combineGlycanMasses[i]));

							matchList.add(match);
						}
					}
				} else {
					NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition, scannum, tree,
							precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, false);
					matchList.add(match);
				}

			} else {
				if (difference < combineGlycanMasses[combineGlycanMasses.length - 1]) {
					for (int i = 0; i < combineGlycanMasses.length; i++) {

						if (Math.abs(difference - combineGlycanMasses[i]) < tolerance) {

							double difference2 = (precursorMz - AminoAcidProperty.PROTON_W) * precursorCharge
									- tree.getGlycoMass() - combineGlycanMasses[i];

							if (difference2 < combineGlycanMasses[combineGlycanMasses.length - 1]) {
								for (int j = 0; j < combineGlycanMasses.length; j++) {

									if (Math.abs(difference2 - combineGlycanMasses[j]) < tolerance) {

										int[] combinei = combineGlycans.get(combineGlycanMasses[i]);
										int[] combinej = combineGlycans.get(combineGlycanMasses[j]);
										int[] combineij = new int[combinei.length];
										for (int k = 0; k < combineij.length; k++) {
											combineij[k] = combinei[k] + combinej[k];
										}

										NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition,
												scannum, tree, precursorMz, precursorCharge, peaks, oxoniumGlycoMap,
												oxoniumIonMap, glycans, combineij);

										String comp = match.getComString();
										if (!hasNeuAc && comp.contains(Glycosyl.NeuAc.getTitle())) {

										} else {
											matchList.add(match);
										}
									}
								}
							} else {
								NovoGlycoCompMatch match = this.judge(difference, tolerance, basicComposition, scannum,
										tree, precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap,
										glycans, false);
								String comp = match.getComString();
								if (!hasNeuAc && comp.contains(Glycosyl.NeuAc.getTitle())) {

								} else {
									matchList.add(match);
								}
							}
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
						precursorMz, precursorCharge, peaks, oxoniumGlycoMap, oxoniumIonMap, glycans, false);

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

	NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, boolean determined) {

		HashMap<Integer, GlycoPeakNode> nodemap = new HashMap<Integer, GlycoPeakNode>();
		nodemap.putAll(tree.getMap());

		HashMap<Integer, GlycoPeakNode> oxoniumMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, GlycoPeakNode> missOxoniumMap = new HashMap<Integer, GlycoPeakNode>();

		for (Integer oxoid : oxoniumGlycoMap.keySet()) {
			ArrayList<Integer> glycoIdList = oxoniumGlycoMap.get(oxoid);
			int missCount = 0;
			for (Integer gid : glycoIdList) {
				if (basicComposition[gid] == 0) {
					missCount++;
				}
			}
			if (missCount > 0) {
				missOxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
			} else {
				oxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
			}
		}

		double[] scores = calScore(nodemap, basicComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] composition = glycans.getOriginalComposition(basicComposition);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, composition,
				glycans.getGlycanName(composition), glycans.getGlycanMass(basicComposition), delta, scores[0], peaks,
				nodemap, oxoniumMap, missOxoniumMap, 1, tree.getSize() - nodemap.size(), determined);
		match.setTreeScore(scores[1]);
		match.setOxoniumScore(scores[2]);

		return match;
	}

	NovoGlycoCompMatch judge(double deltaMass, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, int[] addComposition) {

		int basicTotal = 0;
		int addTotal = 0;
		int[] totalComposition = new int[basicComposition.length];
		for (int j = 0; j < totalComposition.length; j++) {
			totalComposition[j] = basicComposition[j] + addComposition[j];
			basicTotal += basicComposition[j];
			addTotal += addComposition[j];
		}

		HashMap<Integer, GlycoPeakNode> nodemap = new HashMap<Integer, GlycoPeakNode>();
		nodemap.putAll(tree.getMap());

		HashMap<Integer, GlycoPeakNode> oxoniumMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, GlycoPeakNode> missOxoniumMap = new HashMap<Integer, GlycoPeakNode>();

		for (Integer oxoid : oxoniumGlycoMap.keySet()) {
			ArrayList<Integer> glycoIdList = oxoniumGlycoMap.get(oxoid);
			int missCount = 0;
			for (Integer gid : glycoIdList) {
				if (totalComposition[gid] == 0) {
					missCount++;
				}
			}
			if (missCount > 0) {
				missOxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
			} else {
				oxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
			}
		}

		double factor = (double) (basicTotal) / (double) (basicTotal + addTotal - 1);
		double[] scores = calScore(nodemap, totalComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] composition = glycans.getOriginalComposition(totalComposition);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, composition,
				glycans.getGlycanName(composition), glycans.getGlycanMass(totalComposition), deltaMass, scores[0],
				peaks, nodemap, oxoniumMap, missOxoniumMap, factor, tree.getSize() - nodemap.size(), true);
		match.setTreeScore(scores[1]);
		match.setOxoniumScore(scores[2]);

		return match;
	}

	/**
	 * hexnac-hex = 41 357-pse = 41
	 * 
	 * @param in
	 * @param scannum
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws DataFormatException
	 */
	private static void singleTest2(String in, int scannum)
			throws IOException, XMLStreamException, DataFormatException {

		Glycosyl g1 = new Glycosyl("G1", "Glycosyl 1", new int[] {}, 200.116086, 200.116086, "", 20);
		Glycosyl g2 = new Glycosyl("G2", "Glycosyl 2", new int[] {}, 214.1317366, 214.1317366, "", 20);
		Glycosyl g3 = new Glycosyl("G3", "Glycosyl 3", new int[] {}, 227.1270781, 227.1270781, "", 20);
		Glycosyl g4 = new Glycosyl("G4", "Glycosyl 4", new int[] {}, 242.0192296, 242.0192296, "", 20);
		Glycosyl g5 = new Glycosyl("G5", "Glycosyl 5", new int[] {}, 357.0822491, 357.0822491, "", 20);
		Glycosyl g6 = new Glycosyl("G6", "Glycosyl 6", new int[] {}, 249.0862, 249.0862, "", 20);

		double h20 = AminoAcidProperty.MONOW_H * 2 + AminoAcidProperty.MONOW_O;
		Glycosyl g7 = new Glycosyl("G7", "Glycosyl 7", new int[] {}, Glycosyl.HexNAc.getMonoMass() + h20, 249.0862, "",
				20);

		Glycosyl g8 = new Glycosyl("G8", "Glycosyl 8", new int[] {}, 304.12705448, 304.12705448, "", 20);

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid, g8 };

		GlycoOnlyGlycanParser parser = new GlycoOnlyGlycanParser(in, glycans, 10, 20);

		Spectrum ms2spectrumi = parser.ms2SpectraList.getScan(scannum);
		Features features = parser.ms1SpectraList.getFeatures(ms2spectrumi.getPrecursorCharge(),
				ms2spectrumi.getPrecursorMz(), scannum, parser.precursorPPM);
		NovoGlycoCompMatch[] matchesi = parser.parseGSM(ms2spectrumi, features.getMonoMz(), features.getCharge());
		if (matchesi != null) {
			for (int i = 0; i < matchesi.length; i++) {
				System.out.println(i + "\t" + matchesi[i].isDetermined() + "\t" + matchesi[i].getComString() + "\t"
						+ matchesi[i].getPeptideMass() + "\t" + matchesi[i].getScore() + "\t"
						+ matchesi[i].getGlycanMass() + "\t" + matchesi[i].getDeltaMass());

				HashMap<Integer, GlycoPeakNode> nodemap = matchesi[i].getNodeMap();
//				parser.instanceValue(matchesi[i]);

				for (Integer key : nodemap.keySet()) {
					System.out.println(
							key + "\t" + nodemap.get(key).getPeakMz() + "\t" + nodemap.get(key).getGlycanMass());
				}
			}
		}
		System.out.println("cao 407");
	}
	
	private static void testPeak(String in) {
		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid };

		int[] discount = new int[100];
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("mzXML")) {

				GlycoOnlyGlycanParser parser = new GlycoOnlyGlycanParser(files[i], glycans, 10, 20);
				
				System.out.println("length\t"+Arrays.toString(parser.combineGlycanMasses));
				
				Integer[] ms2scans = parser.getMs2scans();
				for (int j = 0; j < ms2scans.length; j++) {

					Spectrum ms2spectrumi = parser.ms2SpectraList.getScan(ms2scans[j]);

					Peak[] peaks = ms2spectrumi.getPeaks();

					int matchCount = 0;
					for (int k = 0; k < peaks.length; k++) {
						double oxoniumTolerance = peaks[k].getMz() * parser.fragmentPPM * 1E-6;
						for (int l = 0; l < parser.oxoniumMasses.length; l++) {
							if (Math.abs(peaks[k].getMz() - parser.oxoniumMasses[l]) < oxoniumTolerance) {
								matchCount++;
							}
						}
					}

					if (matchCount > 1) {
						double premz = ms2spectrumi.getMs1PrecursorMz();
						int before = 0;
						int after = 0;
						for (int k = 0; k < peaks.length; k++) {
							if (peaks[k].getMz() < premz) {
								before++;
							} else {
								after++;
							}
						}

						int id = (int) ((double) after / (double) (peaks.length));
						discount[id]++;
					}
				}
				
				break;
			}
		}

		for (int i = 0; i < discount.length; i++) {
			System.out.println(i + "\t" + discount[i]);
		}
	}
	
	private static void testNew(String in) {
		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid };
		GlycoOnlyGlycanParser parser = new GlycoOnlyGlycanParser(in, glycans, 10, 20);
		Integer[] ms2scans = parser.getMs2scans();
		for (int j = 0; j < ms2scans.length; j++) {

			Spectrum ms2spectrumi = parser.ms2SpectraList.getScan(ms2scans[j]);

			if (ms2spectrumi.getScannum() == 5746) {

				NovoGlycoCompMatch[] matches = parser.parseGSM(ms2spectrumi, ms2spectrumi.getPrecursorMz(),
						ms2spectrumi.getPrecursorCharge());
				if (matches != null) {
					for (int i = 0; i < matches.length; i++) {
						System.out.println(matches[i].isDetermined() + "\t" + matches[i].getComString() + "\t"
								+ matches[i].getGlycanMass()+"\t"+ms2spectrumi.getPrecursorMass());
					}
				}
			}
		}
	}
	
	private static void test(String in) throws FileNotFoundException {

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid };

		int[][] comps = new int[][] { { 2, 1, 0, 0, 1, 0 }, { 2, 2, 0, 0, 0, 0 }, { 2, 2, 0, 0, 1, 0 },
				{ 3, 4, 1, 0, 1, 0 } };

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("mzXML")) {

				GlycoOnlyGlycanParser parser = new GlycoOnlyGlycanParser(files[i], glycans, 10, 20);
				PrintWriter writer = new PrintWriter(new File(files[i].getParent(), files[i].getName() + "determined.tsv"));
				
				writer.println(GlycoOnlyGlycanParser.getTitle("\t"));
				
				Integer[] ms2scans = parser.getMs2scans();
				for (int j = 0; j < ms2scans.length; j++) {
					parser.parseSpectrum(j);
				}

				ArrayList<NovoGlycoCompMatch> list = parser.getDeterminedMatchList();
				ArrayList<NovoGlycoCompMatch> unlist = parser.getUndeterminedMatchList();

				int rank1Deter = 0;
				int rank1Un = 0;

				int[] counts = new int[4];
				
				for (int j = 0; j < list.size(); j++) {
					NovoGlycoCompMatch ngm = list.get(j);
					if (ngm.getRank() == 1) {
						String outputString = ngm.toString();

						writer.println(outputString.replaceAll(";", "\t"));
						rank1Deter++;
					}
					int[] comp = ngm.getComposition();
					for (int k = 0; k < comps.length; k++) {
						boolean find = true;
						for (int l = 0; l < comp.length; l++) {
							if (comp[l] != comps[k][l]) {
								find = false;
								break;
							}
						}

						if (find) {
							counts[k]++;
						}
					}
				}

				for (int j = 0; j < unlist.size(); j++) {
					NovoGlycoCompMatch ngm = unlist.get(j);
					if (ngm.getRank() == 1) {
						String outputString = ngm.toString();

//						writer.println(outputString.replaceAll(";", "\t"));
						rank1Deter++;
					}
					int[] comp = ngm.getComposition();
					for (int k = 0; k < comps.length; k++) {
						boolean find = true;
						for (int l = 0; l < comp.length; l++) {
							if (comp[l] != comps[k][l]) {
								find = false;
								break;
							}
						}

						if (find) {
							counts[k]++;
						}
					}
				}

				for (int j = 0; j < unlist.size(); j++) {
					NovoGlycoCompMatch ngm = unlist.get(j);
					if (ngm.getRank() == 1) {
						rank1Un++;
					}
				}

				System.out.println(files[i].getName() + "\t" + list.size() + "\t" + unlist.size() + "\t"
						+ Arrays.toString(counts) + "\t" + ms2scans.length + "\t" + rank1Deter + "\t" + rank1Un);

				writer.close();
			}
		}
	}

	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub

		long begin = System.currentTimeMillis();

//		GlycoOnlyGlycanParser.singleTest2("D:\\Data\\Xu\\20201027\\Xu_20201027_glycan_test_701sds_S2.mzXML",
//				1583);
		
//		GlycoOnlyGlycanParser.test("D:\\Data\\Xu\\20201027");
		
//		GlycoOnlyGlycanParser.test("D:\\Data\\henghui\\henghui_ibd");
		
		
//		GlycoOnlyGlycanParser.test("Y:\\Xu\\Henghui_Xu_glycan");
		
//		GlycoOnlyGlycanParser.testPeak("D:\\Data\\henghui\\20201013_Glycan");
		
//		GlycoOnlyGlycanParser.testPeak("Y:\\Xu\\Henghui_Xu_glycan");
		
		GlycoOnlyGlycanParser.testNew("Y:\\Xu\\Henghui_Xu_glycan\\Henghui_20191028_Control_PCN590_HFX.mzXML");
		
		long end = System.currentTimeMillis();
		System.out.println((end - begin) / 60000);
	}

}
