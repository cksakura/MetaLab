package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

/**
 * standard mode: using the given monosaccharide to identify the glycans
 * 
 * @author Kai Cheng <ckkazuma@gmail.com>
 *
 */
public class GlycoStandardParser extends AbstractGlycoParser {

	private static final Logger LOGGER = LogManager.getLogger(GlycoStandardParser.class);

	private MicroMonosaccharides glycans;
	private Glycosyl[] glycosyls;
	private double[] glycanMasses;
	private double[] oxoniumMasses;
	private double[] combineGlycanMasses;
	private HashMap<Double, int[]> combineGlycans;

	public GlycoStandardParser(String file, Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM) {
		this(new File(file), glycosyls, precursorPPM, fragmentPPM);
	}

	public GlycoStandardParser(File file, Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM) {

		this.glycosyls = glycosyls;
		this.glycans = new MicroMonosaccharides(glycosyls);
		this.glycanMasses = glycans.getSortedMasses();
		this.oxoniumMasses = glycans.getOxoniumMasses();
		this.combineGlycanMasses = glycans.getCombineGlycosylMasses();
		this.combineGlycans = glycans.getCombineCompositions();

		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;
System.out.println(Arrays.toString(glycanMasses));
System.out.println(Arrays.toString(oxoniumMasses));
		this.preprocess(file);
	}

	public GlycoStandardParser(SpectraList ms1SpectraList, SpectraList ms2SpectraList, Glycosyl[] glycosyls,
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

	private void preprocess(File spectraInput) {

		LOGGER.info("Pre-processing start");

		this.pepSpectraMap = new HashMap<Integer, Peak[]>();
		this.pepSpectraPPMap = new HashMap<Integer, double[]>();

		this.noOxoniumScanSet = new HashSet<Integer>();
		this.glycanTreeScanSet = new HashSet<Integer>();
		this.pepScanSet = new HashSet<Integer>();
		this.noPepGlycanScanSet = new HashSet<Integer>();

		if (spectraInput.getName().endsWith("mgf") || spectraInput.getName().endsWith("MGF")) {

			this.ms2SpectraList = new SpectraList();
			Spectrum spectrum;

			try {
				MgfReader reader = new MgfReader(spectraInput);
				while ((spectrum = reader.getNextSpectrum()) != null) {
					pepSpectraMap.put(spectrum.getScannum(), spectrum.getPeaks());
					pepSpectraPPMap.put(spectrum.getScannum(),
							new double[] { spectrum.getPrecursorMz(), spectrum.getPrecursorMz() });

					peakFilter.parseRank(spectrum.getPeaks());
					spectrum.setPeaks(peakFilter.filter(spectrum.getPeaks()));
					ms2SpectraList.addSpectrum(spectrum);
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
						pepSpectraPPMap.put(spectrum.getScannum(),
								new double[] { spectrum.getPrecursorMz(), spectrum.getPrecursorMz() });

						peakFilter.parseRank(spectrum.getPeaks());
						spectrum.setPeaks(peakFilter.filter(spectrum.getPeaks()));
						ms2SpectraList.addSpectrum(spectrum);
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

		this.ms2scans = ms2SpectraList.getScanList();
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

		LOGGER.info("Pre-processing finished");
	}

	public Glycosyl[] getAllGlycosyls() {
		return this.glycosyls;
	}

	NovoGlycoCompMatch[] parseGSM(Spectrum spectrum, double monoMz, int charge) {

		int scannum = spectrum.getScannum();
		Peak[] peaks = spectrum.getPeaks();
		HashMap<Integer, GlycoPeakNode> oxoniumIonMap = new HashMap<Integer, GlycoPeakNode>();
		HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap = new HashMap<Integer, ArrayList<Integer>>();
		boolean[] removeFromPepSpectrum = new boolean[peaks.length];

		boolean hasNeuAc = false;
		L: for (int i = 0; i < peaks.length; i++) {
			for (int oxoId = 0; oxoId < oxoniumMasses.length; oxoId++) {
				if (Math.abs(peaks[i].getMz() - oxoniumMasses[oxoId]) <= ms2Tolerance) {

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
					removeFromPepSpectrum[i] = true;

					continue L;

				} else if (Math.abs(
						peaks[i].getMz() - oxoniumMasses[oxoId] - AminoAcidProperty.isotope_deltaM) <= ms2Tolerance) {

					removeFromPepSpectrum[i] = true;

					continue L;

				} else if (oxoniumMasses[oxoId] - peaks[i].getMz() > ms2Tolerance) {
					continue L;
				}
			}
			break;
		}

		// no oxonium ions are found
		if (oxoniumIonMap.size() == 0) {
			noOxoniumScanSet.add(scannum);
			return null;
		} else {
			for (Integer id : oxoniumIonMap.keySet()) {
//				System.out.println("oxo\t"+oxoniumIonMap.get(id).getPeakMz());
			}
				
		}

		DenovoGlycoTree[] trees = this.getGlycoTreeList(scannum, peaks, monoMz, charge, glycanMasses,
				removeFromPepSpectrum);

//		DenovoGlycoTree[] trees = this.getGlycoTreeList(peaks, monoMz, charge, glycanMasses, oxoniumIonMap);

		ArrayList<Peak> usedPeakList = new ArrayList<Peak>();
		for (int i = 0; i < peaks.length; i++) {
			if (!removeFromPepSpectrum[i]) {
				usedPeakList.add(peaks[i]);
			}
		}
		this.pepSpectraMap.put(spectrum.getScannum(), usedPeakList.toArray(new Peak[usedPeakList.size()]));

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
			System.out.println("407\t"+i+"\t"+matches[i].getComString()+"\t"+matches[i].getPeptideMass()+"\t"+matches[i].getDeltaMass());
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

		double[] scores = calScore(nodemap, basicComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] composition = glycans.getOriginalComposition(basicComposition);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, composition,
				glycans.getGlycanName(composition), glycans.getGlycanMass(basicComposition), delta, scores[0], peaks,
				nodemap, oxoniumMap, missOxoniumMap, 1, tree.getSize() - nodemap.size(), determined);
		match.setTreeScore(scores[1]);
		match.setOxoniumScore(scores[2]);

		return match;
	}

	NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, double combineGlycanMass) {

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
			} else {
				oxoniumMap.put(oxoid, oxoniumIonMap.get(oxoid));
			}
		}

		double factor = (double) (basicTotal) / (double) (basicTotal + addTotal - 1);
		double[] scores = calScore(nodemap, totalComposition, tree.getAllNodes(), oxoniumMap, missOxoniumMap);
		int[] composition = glycans.getOriginalComposition(totalComposition);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, precursorMz, precursorCharge, composition,
				glycans.getGlycanName(composition), glycans.getGlycanMass(totalComposition), delta - combineGlycanMass,
				scores[0], peaks, nodemap, oxoniumMap, missOxoniumMap, factor, tree.getSize() - nodemap.size(), true);
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

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc };

		testCounts = new int[5];
		GlycoStandardParser parser = new GlycoStandardParser(in, glycans, 20, 20);

		Spectrum ms2spectrumi = parser.ms2SpectraList.getScan(scannum);
//		Features features = parser.ms1SpectraList.getFeatures(ms2spectrumi.getPrecursorMz(),
//				ms2spectrumi.getPrecursorCharge(), scannum, parser.precursorPPM);
		Integer[] ms2scans = parser.getMs2scans();
		for (int i = 0; i < ms2scans.length; i++) {
			if (ms2scans[i] == scannum) {
				parser.parseSpectrum(i);
			}
		}
		
		
		
		/*
		 * NovoGlycoCompMatch[] matchesi = parser.parseGSM(ms2spectrumi,
		 * ms2spectrumi.getPrecursorMz(), ms2spectrumi.getPrecursorCharge()); if
		 * (matchesi != null) { for (int i = 0; i < matchesi.length; i++) {
		 * System.out.println(i + "\t" + matchesi[i].isDetermined() + "\t" +
		 * matchesi[i].getComString() + "\t" + matchesi[i].getPeptideMass() + "\t" +
		 * matchesi[i].getScore() + "\t" + matchesi[i].getGlycanMass() + "\t" +
		 * matchesi[i].getDeltaMass());
		 * 
		 * HashMap<Integer, GlycoPeakNode> nodemap = matchesi[i].getNodeMap(); //
		 * parser.instanceValue(matchesi[i]);
		 * 
		 * for (Integer key : nodemap.keySet()) { System.out.println( key + "\t" +
		 * nodemap.get(key).getPeakMz() + "\t" + nodemap.get(key).getGlycanMass()); } }
		 * }
		 */
		System.out.println("cao 407");
	}

	private static void sptest(String in, String out) throws IOException {
		testsb = new PrintWriter(out);
		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Pen, Glycosyl.Fuc,
				Glycosyl.Glucuronic_acid, Glycosyl.Pseudaminic_acid };
		testCounts = new int[5];
		GlycoStandardParser parser = new GlycoStandardParser(in, glycans, 20, 20);

		Integer[] ms2scans = parser.getMs2scans();
		for (int i = 0; i < ms2scans.length; i++) {
			parser.parseSpectrum(i);
		}
		((PrintWriter) testsb).close();

		System.out.println(
				ms2scans.length + "\t" + parser.noOxoniumScanSet.size() + "\t" + parser.glycanTreeScanSet.size() + "\t"
						+ parser.pepScanSet.size() + "\t" + parser.noPepGlycanScanSet.size());

		System.out.println(Arrays.toString(testCounts));
	}

	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub

		long begin = System.currentTimeMillis();

//		GlycoStandardParser.singleTest2("Z:\\Kai\\henghui_glyco_IBD\\Henghui_20191028_Control_PCN466_HFX.mzXML", 5847);
		GlycoStandardParser.singleTest2(
				"D:\\Data\\Dalian_glyco\\20210218\\N-Glyco\\ACE2_Glyco_TC_193UVPD_3.mzXML", 9562);
//		GlycoStandardParser.singleTest2("D:\\Data\\Rui\\microbiota\\20180827\\0911\\Rui_20140530_microbiota_HILIC_step3.mzXML", 17857);
//		GlycoStandardParser.singleTest2("D:\\Data\\Rui\\microbiota\\decoy\\Bo_20140321_hippo_phospho_#1_pH6.mzXML", 1618);
//		GlycoStandardParser.singleTest2(
//				"D:\\Data\\Rui\\mouse_diet study\\separate\\analysis\\Rui_20160209_mouse_microbiota_HFD_#5_step1.mzXML", 9291);
		// GlycoDenovoParser.modeTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.mzXML");

//		GlycoStandardParser.sptest("D:\\Data\\metaLab3_glyco_test\\Henghui_20190822_HUMANSTOOL_gly_hilic_43_HFX_HCDFT.mgf", 
//				"D:\\Data\\metaLab3_glyco_test\\Henghui_20190822_HUMANSTOOL_gly_hilic_43_HFX_HCDFT_peptide_tree_scans.txt");

		long end = System.currentTimeMillis();
		System.out.println((end - begin) / 60000);
	}

}
