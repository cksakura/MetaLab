/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.io.IOException;
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
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.GlycosylDBManager;
import bmi.med.uOttawa.metalab.glycan.NewGlycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoTree;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

/**
 * discover mode: identify the possible monosaccharide from the oxonium ions
 * @author Kai Cheng <ckkazuma@gmail.com>
 */
public class GlycoDiscoverParser extends AbstractGlycoParser {
	
	private static final Logger LOGGER = LogManager.getLogger(GlycoDiscoverParser.class);

	private File spectraInput;
	
	private Glycosyl[] containGlycosyls;
	private Glycosyl[] findGlycosyls;
	private Glycosyl[] totalGlycosyls;
	private HashMap<String, Glycosyl> glycosylMap;
	private HashMap<String, int[]> modMap;

	public GlycoDiscoverParser(String file, Glycosyl[] containGlycosyls, double precursorPPM, double fragmentPPM) {
		this(new File(file), containGlycosyls, precursorPPM, fragmentPPM);
	}

	public GlycoDiscoverParser(File file, Glycosyl[] containGlycosyls, double precursorPPM, double fragmentPPM) {

		this.containGlycosyls = containGlycosyls;

		for (int i = 0; i < this.containGlycosyls.length; i++) {
			this.containGlycosyls[i].setExpId(i);
		}
		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;
		
		this.preprocess();
	}
	
	public GlycoDiscoverParser(SpectraList ms1SpectraList, SpectraList ms2SpectraList, Glycosyl[] containGlycosyls,
			Glycosyl[] findGlycosyls, double precursorPPM, double fragmentPPM) {

		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;

		this.containGlycosyls = containGlycosyls;
		this.findGlycosyls = findGlycosyls;
		
		this.totalGlycosyls = new Glycosyl[this.containGlycosyls.length + this.findGlycosyls.length];
		System.arraycopy(this.containGlycosyls, 0, this.totalGlycosyls, 0, this.containGlycosyls.length);
		System.arraycopy(this.findGlycosyls, 0, this.totalGlycosyls, this.containGlycosyls.length,
				this.findGlycosyls.length);
		for (int i = 0; i < this.totalGlycosyls.length; i++) {
			totalGlycosyls[i].setExpId(i);
		}

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

	/**
	 * find validate oxonium ions<p>
	 * three-dimensional array mzlist is used for the calculation of intensity weighted mz value
	 */
	private void preprocess() {
		
		LOGGER.info("Pre-processing start");

		this.glycosylMap = GlycosylDBManager.getAllMono();
		this.modMap = GlycosylDBManager.getModMap();
		
		this.pepSpectraMap = new HashMap<Integer, Peak[]>();
		this.pepSpectraPPMap = new HashMap<Integer, Double>();

		this.ms1SpectraList = new SpectraList();
		this.ms2SpectraList = new SpectraList();

		double[][][] mzlist = new double[200][500][2];
		int[][] countlist = new int[200][500];
		double[][] intensityList = new double[200][500];

		if (spectraInput.getName().endsWith("mgf") || spectraInput.getName().endsWith("MGF")) {

			this.ms2SpectraList = new SpectraList();
			Spectrum spectrum;

			try {
				MgfReader reader = new MgfReader(spectraInput);
				while ((spectrum = reader.getNextSpectrum()) != null) {
					pepSpectraMap.put(spectrum.getScannum(), spectrum.getPeaks());
					pepSpectraPPMap.put(spectrum.getScannum(), spectrum.getPrecursorMz());

					peakFilter.parseRank(spectrum.getPeaks());
					Peak[] peaks = peakFilter.filter(spectrum.getPeaks());
					spectrum.setPeaks(peaks);
					ms2SpectraList.addSpectrum(spectrum);
					parseOxonium(peaks, mzlist, countlist, intensityList);
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing spectra from " + spectraInput, e);
			}
			ms2SpectraList.complete();

		} else if (spectraInput.getName().endsWith("mzXML")) {

			this.ms1SpectraList = new SpectraList();
			this.ms2SpectraList = new SpectraList();

			Spectrum spectrum;
			try {
				MzxmlReader reader = new MzxmlReader(spectraInput);
				while ((spectrum = reader.getNextSpectrum()) != null) {
					int level = spectrum.getMslevel();
					if (level == 1) {
						ms1SpectraList.addSpectrum(spectrum);
					} else if (level == 2) {

						pepSpectraMap.put(spectrum.getScannum(), spectrum.getPeaks());
						pepSpectraPPMap.put(spectrum.getScannum(), spectrum.getPrecursorMz());

						peakFilter.parseRank(spectrum.getPeaks());
						Peak[] peaks = peakFilter.filter(spectrum.getPeaks());
						spectrum.setPeaks(peaks);
						ms2SpectraList.addSpectrum(spectrum);
						parseOxonium(peaks, mzlist, countlist, intensityList);
					}
				}
				reader.close();

			} catch (XMLStreamException | DataFormatException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing spectra from " + spectraInput, e);
			}

			ms1SpectraList.complete();
			ms2SpectraList.complete();

			this.featuresList = new ArrayList<Features>();
			this.feasIdMap = new HashMap<Integer, Integer>();
		}

		HashMap<Double, int[]> intensityMap = new HashMap<Double, int[]>();
		for (int i = 0; i < mzlist.length; i++) {
			for (int j = 0; j < mzlist[i].length; j++) {
				if (intensityList[i][j] > 0) {
					intensityMap.put(intensityList[i][j], new int[] { i, j });
				}
			}
		}

		Double[] intensities = intensityMap.keySet().toArray(new Double[intensityMap.size()]);
		Arrays.sort(intensities);
		double stdev = MathTool.getStdDev(intensities);
		int thresId = 1;
		for (; thresId < intensities.length; thresId++) {
			Double[] intensityI = new Double[intensities.length - thresId];
			System.arraycopy(intensities, 0, intensityI, 0, intensityI.length);
			double stdevi = MathTool.getStdDev(intensityI);

			if (stdevi / stdev >= 0.98)
				break;

			stdev = stdevi;
		}

		for (int i = 0; i < intensities.length - thresId + 1; i++) {
			intensityMap.remove(intensities[i]);
		}

		ms1SpectraList.complete();
		ms2SpectraList.complete();

		this.ms2scans = ms2SpectraList.getScanList();
		this.featuresList = new ArrayList<Features>();
		this.feasIdMap = new HashMap<Integer, Integer>();
		this.determinedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.undeterminedMatchList = new ArrayList<NovoGlycoCompMatch>();
		this.usedRank2Scans = new HashSet<Integer>();
		
		

		boolean continuous = false;
		OxoniumFinder finder = null;
		ArrayList<Double> oxoniumMassList = new ArrayList<Double>();
		ArrayList<Double> oxoniumIntensityList = new ArrayList<Double>();

		for (int i = 0; i < intensityList.length; i++) {
			for (int j = 0; j < intensityList[i].length; j++) {
				if (intensityMap.containsKey(intensityList[i][j])) {
					if (continuous) {
						finder.add(mzlist[i][j], countlist[i][j]);
					} else {
						continuous = true;
						finder = new OxoniumFinder();
						finder.add(mzlist[i][j], countlist[i][j]);
					}
				} else {
					if (continuous) {
						oxoniumMassList.add(finder.getMonoMz());
						oxoniumIntensityList.add(finder.getTotalIntensity());
						continuous = false;
						finder = null;
					}
				}
			}
		}

		HashSet<Integer> removeIdSet = new HashSet<Integer>();

		L: for (int i = 0; i < oxoniumMassList.size(); i++) {

			if (removeIdSet.contains(i)) {
				continue;
			}

			double massi = oxoniumMassList.get(i);
			double intensityi = oxoniumIntensityList.get(i);

			for (int j = i + 1; j < oxoniumMassList.size(); j++) {
				double massj = oxoniumMassList.get(j);
				double intensityj = oxoniumIntensityList.get(j);

				if (massj - massi < 3) {

					if (intensityi >= intensityj) {
						removeIdSet.add(j);
					} else {
						removeIdSet.add(i);
						continue L;
					}
				} else if (massj - massi > 3) {
					break;
				}
			}
			for (int j = i + 1; j < oxoniumMassList.size(); j++) {
				double massj = oxoniumMassList.get(j);
				if (Math.abs(massj - massi - h2o) / massi * 1E6 < fragmentPPM) {
					removeIdSet.add(i);
					continue L;
				} else if ((massj - massi - h2o) / massi * 1E6 > fragmentPPM) {
					break;
				}
			}
			for (int j = 0; j < containGlycosyls.length; j++) {
				if (Math.abs(containGlycosyls[j].getMonoMass() - massi - h2o) / massi * 1E6 < fragmentPPM) {
					removeIdSet.add(i);
					continue L;
				} else if (Math.abs(massi - containGlycosyls[j].getMonoMass() - AminoAcidProperty.isotope_deltaM)
						/ massi * 1E6 < fragmentPPM) {
					continue L;
				}
			}
		}

		ArrayList<Double> deltaList = new ArrayList<Double>();
		L: for (int i = 0; i < oxoniumMassList.size(); i++) {

			if (removeIdSet.contains(i))
				continue;

			double massi = oxoniumMassList.get(i);

			for (int k1 = 0; k1 < containGlycosyls.length; k1++) {

				double delta = Math.abs(containGlycosyls[k1].getMonoMass() - massi) / massi * 1E6;
				if (delta <= fragmentPPM) {
					removeIdSet.add(i);
					deltaList.add(massi - containGlycosyls[k1].getMonoMass());
					continue L;
				}
				for (int k2 = 0; k2 <= k1; k2++) {
					delta = Math.abs(containGlycosyls[k1].getMonoMass() + containGlycosyls[k2].getMonoMass() - massi)
							/ massi * 1E6;
					if (delta <= fragmentPPM) {
						removeIdSet.add(i);
						deltaList.add(massi - containGlycosyls[k1].getMonoMass() - containGlycosyls[k2].getMonoMass());
						continue L;
					}
				}
			}

			for (int j = 0; j < i; j++) {

				if (removeIdSet.contains(j))
					continue;

				double massj = oxoniumMassList.get(j);
				for (int k = 0; k < containGlycosyls.length; k++) {
					if (Math.abs(massi - containGlycosyls[k].getMonoMass() - massj) / massi * 1E6 < fragmentPPM) {
						removeIdSet.add(i);
						continue L;
					}
				}
				for (int k = 0; k <= j; k++) {

					if (removeIdSet.contains(k))
						continue;

					double massk = oxoniumMassList.get(k);
					if (Math.abs(massi - massk - massj) / massi * 1E6 < fragmentPPM) {
						removeIdSet.add(i);
						continue L;
					}
				}
			}
		}

		double aveDelta = MathTool.getAve(deltaList);

		ArrayList<Glycosyl> tempList = new ArrayList<Glycosyl>();
		int id = 0;
		for (int i = 0; i < oxoniumMassList.size(); i++) {
			if (!removeIdSet.contains(i)) {
				double monomz = oxoniumMassList.get(i);
				NewGlycosyl ng = new NewGlycosyl("G" + (id + 1), "Glycosyl " + (id + 1), new int[] {},
						monomz - aveDelta, monomz - aveDelta, "", 20);				
				ng.parsePossibleComposition();
				if (ng.getPossibleComposition().length == 0)
					continue;
				ng.parsePossibleGlycosyl(glycosylMap, modMap);
				tempList.add(ng);
				id++;
			}
		}

		this.findGlycosyls = tempList.toArray(new Glycosyl[tempList.size()]);
		for (int i = 0; i < findGlycosyls.length; i++) {
			this.findGlycosyls[i].setExpId(this.containGlycosyls.length + i);
		}

		this.totalGlycosyls = new Glycosyl[this.containGlycosyls.length + this.findGlycosyls.length];
		System.arraycopy(this.containGlycosyls, 0, this.totalGlycosyls, 0, this.containGlycosyls.length);
		System.arraycopy(this.findGlycosyls, 0, this.totalGlycosyls, this.containGlycosyls.length,
				this.findGlycosyls.length);
		
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

	/**
	 * detect oxonium ions in the mz range 200~400
	 * @param peaks
	 * @param mzlist
	 * @param countlist
	 * @param intensityList
	 */
	private void parseOxonium(Peak[] peaks, double[][][] mzlist, int[][] countlist, double[][] intensityList) {
		for (int i = 0; i < peaks.length; i++) {
			double mz = peaks[i].getMz();
			double intensity = peaks[i].getIntensity();
			if (mz < 200) {
				continue;
			} else if (mz >= 400) {
				break;
			} else {
				int id1 = (int) (mz - 200);
				int id2 = (int) ((mz - (int) mz) / 2E-3);
				mzlist[id1][id2][0] += mz * intensity;
				mzlist[id1][id2][1] += intensity;
				countlist[id1][id2]++;
				intensityList[id1][id2] += intensity;
			}
		}
	}

	private class OxoniumFinder {

		private int totalCount;
		private ArrayList<double[]> mzlist;
		private ArrayList<Integer> countlist;

		private OxoniumFinder() {
			this.mzlist = new ArrayList<double[]>();
			this.countlist = new ArrayList<Integer>();
		}

		private void add(double[] mz, int count) {
			this.mzlist.add(mz);
			this.countlist.add(count);
			totalCount += count;
		}

		private double getMonoMz() {

			double mz = 0;
			double intensity = 0;
			for (int i = 0; i < mzlist.size(); i++) {
				double[] mzs = mzlist.get(i);
				mz += mzs[0];
				intensity += mzs[1];
			}
			return mz / intensity - AminoAcidProperty.PROTON_W;
		}
		
		private double getTotalIntensity() {
			double intensity = 0;
			for (int i = 0; i < mzlist.size(); i++) {
				double[] mzs = mzlist.get(i);
				intensity += mzs[1];
			}
			return intensity;
		}

		private int getTotalCount() {
			return totalCount;
		}
	}

	public Glycosyl[] getAllGlycosyls() {
		return totalGlycosyls;
	}

	public Glycosyl[] getContainGlycosyls() {
		return containGlycosyls;
	}

	public Glycosyl[] getFindGlycosyls() {
		return findGlycosyls;
	}

	NovoGlycoCompMatch[] parseGSM(Spectrum spectrum, double monoMz, int charge) {

		Peak[] peaks = spectrum.getPeaks();

		ArrayList<Glycosyl> glycolist = new ArrayList<Glycosyl>();
		for (int i = 0; i < peaks.length; i++) {
			double mzi = peaks[i].getMz();
			double oxoniumTolerance = mzi * fragmentPPM * 1E-6;
			for (int j = 0; j < this.findGlycosyls.length; j++) {
				if (Math.abs(
						mzi - this.findGlycosyls[j].getMonoMass() - AminoAcidProperty.PROTON_W) < oxoniumTolerance) {
					glycolist.add(this.findGlycosyls[j]);
				}
			}
		}

		Glycosyl[] usedGlycosyls = new Glycosyl[this.containGlycosyls.length + glycolist.size()];
		System.arraycopy(this.containGlycosyls, 0, usedGlycosyls, 0, this.containGlycosyls.length);
		for (int i = containGlycosyls.length; i < usedGlycosyls.length; i++) {
			usedGlycosyls[i] = glycolist.get(i - containGlycosyls.length);
		}

		MicroMonosaccharides glycans = new MicroMonosaccharides(usedGlycosyls);
		double[] glycanMasses = glycans.getSortedMasses();
		double[] oxoniumMasses = glycans.getOxoniumMasses();

		HashMap<Integer, GlycoPeakNode> oxoniumIonMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap = new HashMap<Integer, ArrayList<Integer>>();

		boolean hasNeuAc = false;
		int oxoId = 0;
		for (int i = 0; i < peaks.length; i++) {

			double oxoniumTolerance = peaks[i].getMz() * fragmentPPM * 1E-6 * 5;
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
					int[] comp = new int[glycanMasses.length];

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
					gpn.setComposition(getOriginalComposition(glycans, comp, usedGlycosyls));
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

			HashMap<Integer, GlycoPeakNode> nodemap = matches[i].getNodeMap();
			Iterator<Integer> it = nodemap.keySet().iterator();
			while (it.hasNext()) {
				Integer id = it.next();
				GlycoPeakNode gpn = nodemap.get(id);
				gpn.setChildren(getOriginalPeakNodes(gpn.getChildren(), usedGlycosyls));
			}
		}

		if (matches.length <= maxN) {
			return matches;
		} else {
			NovoGlycoCompMatch[] partMatches = new NovoGlycoCompMatch[maxN];
			System.arraycopy(matches, 0, partMatches, 0, maxN);
			return partMatches;
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
		Glycosyl[] glycosyls = glycans.getGlycosyls();
		double[] scores = calScore(nodemap, basicComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] originalComposition = getOriginalComposition(glycans, basicComposition, glycosyls);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, originalComposition,
				glycans.getGlycanName(glycans.getOriginalComposition(basicComposition)),
				glycans.getGlycanMass(basicComposition), delta, scores[0], peaks, nodemap, oxoniumMap, missOxoniumMap, 1,
				tree.getSize() - nodemap.size(), determined);
		match.setTreeScore(scores[1]);
		match.setOxoniumScore(scores[2]);
		
		return match;
	}
	
	NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, double combineGlycanMass) {

		HashMap<Double, int[]> combineGlycans = glycans.getCombineCompositions();

		int basicTotal = 0;
		int addTotal = 0;
		int[] addComposition = combineGlycans.get(combineGlycanMass);
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
//System.out.println("oxo miss\t"+Arrays.toString(totalComposition)+"\t"+oxoniumIonMap.get(oxoid).getPeakMz());
			} else {
				oxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
//System.out.println("oxo have\t"+Arrays.toString(totalComposition)+"\t"+oxoniumIonMap.get(oxoid).getPeakMz());				
			}
		}

		Glycosyl[] glycosyls = glycans.getGlycosyls();
		double factor = (double) (basicTotal) / (double) (basicTotal + addTotal - 1);
		double[] scores = calScore(nodemap, totalComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] originalComposition = getOriginalComposition(glycans, totalComposition, glycosyls);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, originalComposition,
				glycans.getGlycanName(glycans.getOriginalComposition(totalComposition)),
				glycans.getGlycanMass(totalComposition), delta - combineGlycanMass, scores[0], peaks, nodemap, oxoniumMap,
				missOxoniumMap, factor, tree.getSize() - nodemap.size(), true);
		match.setTreeScore(scores[1]);
		match.setOxoniumScore(scores[2]);
		
		return match;
	}

	private int[] getOriginalComposition(MicroMonosaccharides glycans, int[] composition, Glycosyl[] usedGlycosyls) {
		int[] sortComposition = glycans.getOriginalComposition(composition);
		int[] originalComposition = new int[this.totalGlycosyls.length];
		for (int i = 0; i < usedGlycosyls.length; i++) {
			int expid = usedGlycosyls[i].getExpId();
			originalComposition[expid] = sortComposition[i];
		}
		return originalComposition;
	}
	
	private GlycoPeakNode[] getOriginalPeakNodes(GlycoPeakNode[] nodes, Glycosyl[] usedGlycosyls) {
		GlycoPeakNode[] originalNodes = new GlycoPeakNode[this.totalGlycosyls.length];
		for (int i = 0; i < usedGlycosyls.length; i++) {
			int expid = usedGlycosyls[i].getExpId();
			originalNodes[expid] = nodes[i];
		}
		return originalNodes;
	}

	private static void singleTest2(String in, int scannum)
			throws IOException, XMLStreamException, DataFormatException {

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid, Glycosyl.Pseudaminic_acid};
		
		for (int i = 0; i < glycans.length; i++) {
			System.out.println("oxo\t" + i + "\t" + glycans[i].getCompositionString() + "\t"
					+ (glycans[i].getMonoMass() + AminoAcidProperty.isotope_deltaM));
		}

		GlycoDiscoverParser parser = new GlycoDiscoverParser(in, glycans, 10, 10);
		Spectrum ms2spectrumi = parser.ms2SpectraList.getScan(scannum);
		Features features = parser.ms1SpectraList.getFeatures(ms2spectrumi.getPrecursorMz(),
				ms2spectrumi.getPrecursorCharge(), scannum, parser.precursorPPM);
		NovoGlycoCompMatch[] matchesi = parser.parseGSM(ms2spectrumi, features.getMonoMz(), features.getCharge());
		if (matchesi != null) {
			for (int i = 0; i < matchesi.length; i++) {
				System.out.println(i + "\t" + matchesi[i].isDetermined() + "\t" + matchesi[i].getComString() + "\t"
						+ matchesi[i].getScore() + "\t" + matchesi[i].getDeltaMass());
				
//				parser.instanceValue(matchesi[i]);
			}
		}
	}
	
	private void testSpectrum(int scannum) {
		
		for(int i=0;i<this.findGlycosyls.length;i++) {
			System.out.println(i+" glyco \t"+findGlycosyls[i].getMonoMass());
		}

		Spectrum ms2spectrumi = ms2SpectraList.getScan(scannum);
		Features features = ms1SpectraList.getFeatures(ms2spectrumi.getPrecursorMz(), ms2spectrumi.getPrecursorCharge(),
				scannum, precursorPPM);

		NovoGlycoCompMatch[] matchesi = this.parseGSM(ms2spectrumi, features.getMonoMz(), features.getCharge());
		if (matchesi != null) {
			for (int i = 0; i < matchesi.length; i++) {
				System.out.println(i + "\t" + matchesi[i].isDetermined() + "\t" + matchesi[i].getComString() + "\t"
						+ matchesi[i].getScore() + "\t" + matchesi[i].getDeltaMass());
			}
		}
	}
	
	private static void modeTest(String in) throws IOException, XMLStreamException, DataFormatException {
		Glycosyl[] containList = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.NeuAc, Glycosyl.Hex, Glycosyl.dHex };
		GlycoDiscoverParser parser = new GlycoDiscoverParser(new File(in), containList, 10, 10);
		parser.preprocess();
	}
	
	private static void modeTestFile(String in) throws IOException, XMLStreamException, DataFormatException {
		Glycosyl[] containList = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.NeuAc, Glycosyl.Hex, Glycosyl.dHex };
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("mzXML")) {
				GlycoDiscoverParser parser = new GlycoDiscoverParser(files[i], containList, 10, 10);
				parser.preprocess();
			}
		}
	}

	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub

		long begin = System.currentTimeMillis();
		
//		GlycoDiscoverParser.modeTest("\\\\137.122.238.141\\Figeys_lab\\Kai\\data\\Xu_20160719_HM502PCN_S2.mzXML");
//		GlycoDiscoverParser.modeTestFile("D:\\Data\\Rui\\New");
//		GlycoDiscoverParser.modeTest("D:\\Data\\Rui\\mouse_diet study\\pooled\\Rui_20151222_mouse_microbiota_LFD_step3_151224035620.mzXML");
//		GlycoDiscoverParser.modeTest("D:\\Data\\Rui\\mouse_diet study\\separate\\Rui_20160209_mouse_microbiota_HFD_#6_step3.mzXML");
		GlycoDiscoverParser.singleTest2("D:\\Data\\Rui\\microbiota\\20180827\\0911\\Rui_20140530_microbiota_HILIC_step3.mzXML", 10731);
		
		long end = System.currentTimeMillis();
		System.out.println((end-begin)/60000);
	}

}
