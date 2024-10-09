package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoPeptide;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoDiscoverParser;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoMatchClassifier;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoMicroNovoStat;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoOnlyGlycanParser;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GlycoStandardParser;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IGlycoParser;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.NovoGlycoCompMatch;
import bmi.med.uOttawa.metalab.quant.Features;


public class GlycoMicroNovoTask extends SwingWorker<File, Void> {
	
	private static String taskName = "Microbiome glycan identification task";
	private static final Logger LOGGER = LogManager.getLogger(GlycoMicroNovoTask.class);

	private File in;
	private File out;
	private String fileName;
	private double precursorPPM;
	private double fragmentPPM;
	private String parserType;
	private Glycosyl[] glycosyls;

	private double glycosylThreshold = 0.005;
	
	public GlycoMicroNovoTask(String in, String out, String para) {
		this(in, out, GlycoMicroNovoPara.read(para));
	}
	
	public GlycoMicroNovoTask(String in, String out, GlycoMicroNovoPara para) {
		this(in, out, para.getGlycosyls(), para.getPrecursorPPM(), para.getFragmentPPM(), para.getParserType());
	}

	public GlycoMicroNovoTask(String in, String out, Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM,
			String parserType) {

		this.in = new File(in);
		this.out = new File(out);
		this.glycosyls = glycosyls;
		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;
		this.parserType = parserType;
	}
	
	@Override
	protected File doInBackground() throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info(taskName + ": parsing " + in + " started");

		IGlycoParser parser = null;
		if (parserType.equals(MetaGlycanConstants.standardMode)) {

			LOGGER.info("Parsing mode: standard");
			parser = new GlycoStandardParser(in, glycosyls, precursorPPM, fragmentPPM);

			String name = in.getName();
			name = name.substring(0, name.length()-".mzXML".length());
					
			File outputFile = new File(out, name);
			if (!outputFile.exists()) {
				outputFile.mkdir();
			}
			parser.exportPepSpectra(name, outputFile.getAbsolutePath());

		} else if (parserType.equals(MetaGlycanConstants.discoverMode)) {

			LOGGER.info("Parsing mode: discovery");
			parser = new GlycoDiscoverParser(in, glycosyls, precursorPPM, fragmentPPM);

			String name = in.getName();
			File outputFile = new File(out, name);
			if (!outputFile.exists()) {
				outputFile.mkdir();
			}
			parser.exportPepSpectra(name, outputFile.getAbsolutePath());

		} else if (parserType.equals(MetaGlycanConstants.onlyGlycanMode)) {

			LOGGER.info("Parsing mode: only glycan");
			parser = new GlycoOnlyGlycanParser(in, glycosyls, precursorPPM, fragmentPPM);

		} else {
			LOGGER.error("Unknown parser type, should be 0 for standard mode or 1 for discovery mode");
			return null;
		}

		Integer[] ms2scans = parser.getMs2scans();
		for (int i = 0; i < ms2scans.length; i++) {
			parser.parseSpectrum(i);
		}

		HashMap<Integer, Integer> feasIdMap = parser.getFeasIdMap();
		ArrayList<Features> featuresList = parser.getFeaturesList();
		Glycosyl[] glycosyls = parser.getAllGlycosyls();

		ArrayList<NovoGlycoCompMatch> determinedList = parser.getDeterminedMatchList();
		ArrayList<NovoGlycoCompMatch> undeterminedList = parser.getUndeterminedMatchList();
		
		HashSet<Integer> usedRank2Scans = parser.getUsedRank2Scans();
		HashMap<Integer, Integer> feaIdMap = parser.getFeasIdMap();
		ArrayList<Features> feasList = parser.getFeaturesList();

		NovoGlycoCompMatch[] determinedMatches = null;
		NovoGlycoCompMatch[] undeterminedMatches = null;
		DenovoGlycoPeptide[] glycoPeptides = null;

		int[] totalComposition = new int[glycosyls.length];

		boolean useClassifier = false;

		if (!parserType.equals(MetaGlycanConstants.onlyGlycanMode)) {
			if (determinedList.size() > GlycoMatchClassifier.leastClassifyCount) {

				LOGGER.info(determinedList.size()
						+ " determined GSMs are found, machine learning based classification started");

				GlycoMatchClassifier classifier = new GlycoMatchClassifier(false);
				double[] scores = classifier.classify(determinedList, usedRank2Scans);
				determinedMatches = this.setQValue(determinedList, scores, usedRank2Scans);
				glycoPeptides = this.getGlycopeptides(determinedMatches, feasIdMap, feasList, true);

				for (int i = 0; i < determinedMatches.length; i++) {
					if (determinedMatches[i].getRank() == 1 && determinedMatches[i].getClassScore() < 0.5) {
						int[] comp = determinedMatches[i].getComposition();
						for (int j = 0; j < comp.length; j++) {
							if (comp[j] > 0) {
								totalComposition[j]++;
							}
						}
					}
				}
				useClassifier = true;

			} else {
				determinedMatches = this.setQValue(determinedList, usedRank2Scans);
				glycoPeptides = this.getGlycopeptides(determinedMatches, feasIdMap, feasList, false);

				for (int i = 0; i < determinedMatches.length; i++) {
					if (determinedMatches[i].getRank() == 1 && determinedMatches[i].getqValue() <= 0.01) {
						int[] comp = determinedMatches[i].getComposition();
						for (int j = 0; j < comp.length; j++) {
							if (comp[j] > 0) {
								totalComposition[j]++;
							}
						}
					}
				}
			}
		}
		
		if (this.parserType.equals(MetaGlycanConstants.discoverMode)) {

			Glycosyl[] contianGlycosyls = ((GlycoDiscoverParser) parser).getContainGlycosyls();
			Glycosyl[] findGlycosyls = ((GlycoDiscoverParser) parser).getFindGlycosyls();

			ArrayList<Glycosyl> glist = new ArrayList<Glycosyl>();

			for (int i = contianGlycosyls.length; i < totalComposition.length; i++) {
				if (totalComposition[i] > determinedMatches.length * this.glycosylThreshold) {
					glist.add(glycosyls[i]);
				}
			}

			if (glist.size() < findGlycosyls.length) {
				Glycosyl[] newGlycosyls = glist.toArray(new Glycosyl[glist.size()]);
				for (int i = 0; i < newGlycosyls.length; i++) {
					newGlycosyls[i].setTitle("G" + (i + 1));
					newGlycosyls[i].setFullname("Glycosyl " + (i + 1));
				}
				IGlycoParser newParser = new GlycoDiscoverParser(parser.getMs1SpectraList(), parser.getMs2SpectraList(),
						contianGlycosyls, newGlycosyls, precursorPPM, fragmentPPM);

				Integer[] newMs2scans = newParser.getMs2scans();
				for (int i = 0; i < newMs2scans.length; i++) {
					newParser.parseSpectrum(i);
				}
				feasIdMap = newParser.getFeasIdMap();
				featuresList = newParser.getFeaturesList();

				determinedList = newParser.getDeterminedMatchList();
				undeterminedList = newParser.getUndeterminedMatchList();
				usedRank2Scans = newParser.getUsedRank2Scans();

				if (determinedList.size() > 200) {

					GlycoMatchClassifier classifier = new GlycoMatchClassifier(false);
					double[] scores = classifier.classify(determinedList, usedRank2Scans);
					determinedMatches = this.setQValue(determinedList, scores, usedRank2Scans);
					glycoPeptides = this.getGlycopeptides(determinedMatches, feasIdMap, feasList, true);
					useClassifier = true;

				} else {
					determinedMatches = this.setQValue(determinedList, usedRank2Scans);
					glycoPeptides = this.getGlycopeptides(determinedMatches, feasIdMap, feasList, false);
					useClassifier = false;
				}
				undeterminedMatches = this.setQValue(undeterminedList, usedRank2Scans);
				glycosyls = newParser.getAllGlycosyls();

			} else {
				undeterminedMatches = this.setQValue(undeterminedList, usedRank2Scans);
			}

		} else if (this.parserType.equals(MetaGlycanConstants.standardMode)) {
			undeterminedMatches = this.setQValue(undeterminedList, usedRank2Scans);

		} else if (this.parserType.equals(MetaGlycanConstants.onlyGlycanMode)) {
			undeterminedMatches = this.setQValue(undeterminedList, usedRank2Scans);
		}

		Arrays.sort(glycosyls, new Glycosyl.GlycosylExpidComparator());

		GlycoMicroNovoStat stat = new GlycoMicroNovoStat(glycosyls);
		stat.stat(determinedMatches);
		stat.stat(undeterminedMatches);

		File outputFile = new File(out, fileName + ".tsv");
		GlycoMicroNovoWriter writer = new GlycoMicroNovoWriter(outputFile);
		writer.write(determinedMatches, undeterminedMatches, feasIdMap, featuresList, glycosyls);

		File xlsx = new File(out, fileName + ".xlsx");
		LOGGER.info(taskName + ": writing results to " + xlsx);

		GlycoMicroNovoXlsWriter xlsWriter = new GlycoMicroNovoXlsWriter(xlsx, glycosyls, useClassifier);

		try {
			xlsWriter.write(determinedMatches, undeterminedMatches, usedRank2Scans, glycoPeptides);
			xlsWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing glycopeptide spectra matches to " + xlsx, e);
		}

		LOGGER.info(taskName + ": parsing " + in + " finished");

		return xlsx;
	}

	/**
	 * Combining the glycan spectra matches with the extracted ion chromatograms to glycopeptide information
	 * @param determinedMatches all of the identified glycan spectra matches
	 * @param feasIdMap key: scan number; value: the extracted ion chromatogram containing this MS2 spectra
	 * @param feasList the extracted ion chromatogram list
	 * @param classify
	 * @return
	 */
	private DenovoGlycoPeptide[] getGlycopeptides(NovoGlycoCompMatch[] determinedMatches,
			HashMap<Integer, Integer> feasIdMap, ArrayList<Features> feasList, boolean classify) {

		/*
		 * key: feature ID; value: a list of glycan spectra matches from the same
		 * glycopeptide
		 */
		HashMap<Integer, HashMap<String, ArrayList<NovoGlycoCompMatch>>> gsmMap = new HashMap<Integer, HashMap<String, ArrayList<NovoGlycoCompMatch>>>();

		for (NovoGlycoCompMatch match : determinedMatches) {
			if (classify) {
				if (match.getRank() > 1 || match.getClassScore() >= 0.5) {
					continue;
				}
			} else {
				if (match.getqValue() > 0.05) {
					continue;
				}
			}

			int scan = match.getScannum();
			String glycan = match.getComString();
			if (feasIdMap.containsKey(scan)) {
				int feaId = feasIdMap.get(scan);

				HashMap<String, ArrayList<NovoGlycoCompMatch>> glycanMap;
				if (gsmMap.containsKey(feaId)) {
					glycanMap = gsmMap.get(feaId);
				} else {
					glycanMap = new HashMap<String, ArrayList<NovoGlycoCompMatch>>();
					gsmMap.put(feaId, glycanMap);
				}

				ArrayList<NovoGlycoCompMatch> list;
				if (glycanMap.containsKey(glycan)) {
					list = glycanMap.get(glycan);
				} else {
					list = new ArrayList<NovoGlycoCompMatch>();
					glycanMap.put(glycan, list);
				}
				list.add(match);
			}
		}

		ArrayList<DenovoGlycoPeptide> list = new ArrayList<DenovoGlycoPeptide>();
		for (Integer feaId : gsmMap.keySet()) {

			Features feas = feasList.get(feaId);
			feas.setInfo();

			double premz = feas.getMonoMz();
			int charge = feas.getCharge();

			HashMap<String, ArrayList<NovoGlycoCompMatch>> glycanMap = gsmMap.get(feaId);
			for (String glycan : glycanMap.keySet()) {
				ArrayList<NovoGlycoCompMatch> gsms = glycanMap.get(glycan);

				double score = 0;
				double pepMass = 0;
				double glycanMass = 0;
				int[] composition = null;
				int[] scans = new int[gsms.size()];
				for (int i = 0; i < scans.length; i++) {
					NovoGlycoCompMatch gm = gsms.get(i);
					scans[i] = gm.getScannum();
					if (gm.getScore() > score) {
						score = gm.getScore();
						pepMass = gm.getPeptideMass();
						glycanMass = gm.getGlycanMass();
						composition = gm.getComposition();
					}
				}

				DenovoGlycoPeptide dgp = new DenovoGlycoPeptide(premz, charge, scans, composition, glycan, pepMass,
						glycanMass, score, feas);
				list.add(dgp);
			}
		}

		DenovoGlycoPeptide[] dgps = list.toArray(new DenovoGlycoPeptide[list.size()]);
		
		return dgps;
	}

	private NovoGlycoCompMatch[] setQValue(ArrayList<NovoGlycoCompMatch> matchlist, HashSet<Integer> usedRank2Scans) {

		NovoGlycoCompMatch[] matches = new NovoGlycoCompMatch[matchlist.size()];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = matchlist.get(i);
		}

		Arrays.sort(matches, new Comparator<NovoGlycoCompMatch>() {

			@Override
			public int compare(NovoGlycoCompMatch arg0, NovoGlycoCompMatch arg1) {
				// TODO Auto-generated method stub
				if (arg0.getScore() < arg1.getScore()) {
					return 1;
				} else if (arg0.getScore() > arg1.getScore()) {
					return -1;
				}
				return 0;
			}

		});

		int target = 0;
		int decoy = 0;

		for (int i = 0; i < matches.length; i++) {
			if (matches[i].getRank() == 1) {
				target++;
			} else {
				if (!usedRank2Scans.contains(matches[i].getScannum())) {
					decoy++;
				}
			}
			double qValue = target == 0 ? 1 : (double) decoy / (double) target;
			matches[i].setqValue(qValue);
		}

		return matches;
	}
	
	private NovoGlycoCompMatch[] setQValue(ArrayList<NovoGlycoCompMatch> matchlist, double[] scores, HashSet<Integer> usedRank2Scans) {

		NovoGlycoCompMatch[] matches = new NovoGlycoCompMatch[matchlist.size()];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = matchlist.get(i);
			matches[i].setClassScore(scores[i]);
		}

		Arrays.sort(matches, new Comparator<NovoGlycoCompMatch>() {

			@Override
			public int compare(NovoGlycoCompMatch arg0, NovoGlycoCompMatch arg1) {
				// TODO Auto-generated method stub
				if (arg0.getScore() < arg1.getScore()) {
					return 1;
				} else if (arg0.getScore() > arg1.getScore()) {
					return -1;
				}
				return 0;
			}

		});

		int target = 0;
		int decoy = 0;

		for (int i = 0; i < matches.length; i++) {
			if (matches[i].getRank() == 1) {
				target++;
			} else {
				if (!usedRank2Scans.contains(matches[i].getScannum())) {
					decoy++;
				}
			}
			double qValue = target == 0 ? 1 : (double) decoy / (double) target;
			matches[i].setqValue(qValue);
		}

		return matches;
	}

	public String toString() {
		return out.getAbsolutePath();
	}

	private static void batchTest(String in) {

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Fuc, Glycosyl.Pen,
				Glycosyl.Glucuronic_acid};
		
		/*Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Fuc,
				Glycosyl.Pen};*/
				
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("mzXML")) {
				String mzxml = files[i].getAbsolutePath();
				GlycoMicroNovoTask task = new GlycoMicroNovoTask(mzxml, in, glycans, 10, 20, MetaGlycanConstants.standardMode);
				task.run();
				break;
			}
		}
	}
	
	private static void onlyTest(String mzxml, String folder) throws IOException {
		
		HashSet<Integer> setA = new HashSet<Integer>();
		BufferedReader readerA = new BufferedReader(new FileReader(folder + "\\A.txt"));
		String lineA = null;
		while ((lineA = readerA.readLine()) != null) {
			setA.add(Integer.parseInt(lineA));
		}
		readerA.close();

		HashSet<Integer> setB = new HashSet<Integer>();
		BufferedReader readerB = new BufferedReader(new FileReader(folder + "\\B.txt"));
		String lineB = null;
		while ((lineB = readerB.readLine()) != null) {
			setB.add(Integer.parseInt(lineB));
		}
		readerB.close();

		HashSet<Integer> setC = new HashSet<Integer>();
		BufferedReader readerC = new BufferedReader(new FileReader(folder + "\\C.txt"));
		String lineC = null;
		while ((lineC = readerC.readLine()) != null) {
			setC.add(Integer.parseInt(lineC));
		}
		readerC.close();

		System.out.println(setA.size() + "\t" + setB.size() + "\t" + setC.size());

		Glycosyl[] glycans = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Fuc, Glycosyl.Pen,
				Glycosyl.Glucuronic_acid, Glycosyl.Pseudaminic_acid };
		String out = mzxml.substring(0, mzxml.lastIndexOf(".")) + ".txt";
		System.out.println(mzxml);

		GlycoMicroNovoTask task = new GlycoMicroNovoTask(mzxml, out, glycans, 10, 10,
				MetaGlycanConstants.onlyGlycanMode);
		IGlycoParser parser = new GlycoDiscoverParser(mzxml, glycans, 10, 10);

		Integer[] ms2scans = parser.getMs2scans();
		for (int i = 0; i < ms2scans.length; i++) {
			parser.parseSpectrum(i);
		}

		HashMap<Integer, Integer> feasIdMap = parser.getFeasIdMap();
		ArrayList<Features> featuresList = parser.getFeaturesList();
		Glycosyl[] glycosyls = parser.getAllGlycosyls();

		ArrayList<NovoGlycoCompMatch> determinedList = parser.getDeterminedMatchList();
		ArrayList<NovoGlycoCompMatch> undeterminedList = parser.getUndeterminedMatchList();
		HashSet<Integer> usedRank2Scans = parser.getUsedRank2Scans();

		NovoGlycoCompMatch[] determinedMatches = null;
		NovoGlycoCompMatch[] undeterminedMatches = null;
		DenovoGlycoPeptide[] glycoPeptides = null;

		int[] totalComposition = new int[glycosyls.length];

		if (determinedList.size() > 200) {

			LOGGER.info(determinedList.size()
					+ " determined GSMs are found, machine learning based classification started");
			GlycoMatchClassifier classifier = new GlycoMatchClassifier(true);
			double[] scores = classifier.classify(determinedList, parser.getUsedRank2Scans());
			determinedMatches = task.setQValue(determinedList, scores, usedRank2Scans);

			for (int i = 0; i < determinedMatches.length; i++) {
				if (determinedMatches[i].getRank() == 1 && determinedMatches[i].getClassScore() < 0.5) {
					int[] comp = determinedMatches[i].getComposition();
					for (int j = 0; j < comp.length; j++) {
						if (comp[j] > 0) {
							totalComposition[j]++;
						}
					}
				}
			}

		} else {
			determinedMatches = task.setQValue(determinedList, usedRank2Scans);
			for (int i = 0; i < determinedMatches.length; i++) {
				if (determinedMatches[i].getRank() == 1 && determinedMatches[i].getqValue() <= 0.01) {
					int[] comp = determinedMatches[i].getComposition();
					for (int j = 0; j < comp.length; j++) {
						if (comp[j] > 0) {
							totalComposition[j]++;
						}
					}
				}
			}
		}

		Glycosyl[] contianGlycosyls = ((GlycoDiscoverParser) parser).getContainGlycosyls();
		Glycosyl[] findGlycosyls = ((GlycoDiscoverParser) parser).getFindGlycosyls();

		ArrayList<Glycosyl> glist = new ArrayList<Glycosyl>();

		for (int i = contianGlycosyls.length; i < totalComposition.length; i++) {
//System.out.println("140\t"+glycosyls[i].getMonoMass()+"\t"+totalComposition[i]+"\t"+determinedMatches.length+"\t"+(determinedMatches.length * this.glycosylThreshold));				
			if (totalComposition[i] > determinedMatches.length * task.glycosylThreshold) {
				glist.add(glycosyls[i]);
			}
		}

		if (glist.size() == 0) {

			IGlycoParser newParser = new GlycoStandardParser(parser.getMs1SpectraList(), parser.getMs2SpectraList(),
					contianGlycosyls, 10, 10);

			Integer[] newMs2scans = newParser.getMs2scans();
			for (int i = 0; i < newMs2scans.length; i++) {
				newParser.parseSpectrum(i);
			}
			feasIdMap = newParser.getFeasIdMap();
			featuresList = newParser.getFeaturesList();

			determinedList = newParser.getDeterminedMatchList();
			undeterminedList = newParser.getUndeterminedMatchList();
			usedRank2Scans = newParser.getUsedRank2Scans();

			if (determinedList.size() > 200) {
				GlycoMatchClassifier classifier = new GlycoMatchClassifier(true);
				double[] scores = classifier.classify(determinedList, newParser.getUsedRank2Scans());
				determinedMatches = task.setQValue(determinedList, scores, usedRank2Scans);

			} else {
				determinedMatches = task.setQValue(determinedList, usedRank2Scans);
			}
			undeterminedMatches = task.setQValue(undeterminedList, usedRank2Scans);
			glycosyls = contianGlycosyls;

		} else if (glist.size() == findGlycosyls.length) {
			undeterminedMatches = task.setQValue(undeterminedList, usedRank2Scans);

		} else {
			Glycosyl[] newGlycosyls = glist.toArray(new Glycosyl[glist.size()]);
			for (int i = 0; i < newGlycosyls.length; i++) {
				newGlycosyls[i].setTitle("G" + (i + 1));
				newGlycosyls[i].setFullname("Glycosyl " + (i + 1));
			}
			IGlycoParser newParser = new GlycoDiscoverParser(parser.getMs1SpectraList(), parser.getMs2SpectraList(),
					contianGlycosyls, newGlycosyls, 10, 10);

			Integer[] newMs2scans = newParser.getMs2scans();
			for (int i = 0; i < newMs2scans.length; i++) {
				newParser.parseSpectrum(i);
			}
			feasIdMap = newParser.getFeasIdMap();
			featuresList = newParser.getFeaturesList();

			determinedList = newParser.getDeterminedMatchList();
			undeterminedList = newParser.getUndeterminedMatchList();
			usedRank2Scans = newParser.getUsedRank2Scans();

			if (determinedList.size() > 200) {
				GlycoMatchClassifier classifier = new GlycoMatchClassifier(true);
				double[] scores = classifier.classify(determinedList, newParser.getUsedRank2Scans());
				determinedMatches = task.setQValue(determinedList, scores, usedRank2Scans);

			} else {
				determinedMatches = task.setQValue(determinedList, usedRank2Scans);
			}
			undeterminedMatches = task.setQValue(undeterminedList, usedRank2Scans);
			glycosyls = newParser.getAllGlycosyls();
		}

		Arrays.sort(glycosyls, new Glycosyl.GlycosylExpidComparator());

		GlycoMicroNovoStat stat = new GlycoMicroNovoStat(glycosyls);
		stat.stat(determinedMatches);
		stat.stat(undeterminedMatches);

		System.out.println(stat.getClassTP() + "\t" + stat.getClassTN() + "\t" + stat.getClassFP() + "\t"
				+ stat.getClassFN() + "\t" + stat.getClassFDR());
		System.out.println((1 - stat.getClassSpecificity()) + "\t" + stat.getClassSensitivity());
		System.out.println(stat.getClassFDR() + "\t" + stat.getQValue1() + "\t" + stat.getQValue5());
		double[][] roc = stat.getScoreROC();
		for (int i = 0; i < roc[0].length; i++) {
			System.out.println(roc[0][i] + "\t" + roc[1][i]);
		}

		int[] counts = new int[3];
		int[] tier1 = new int[4];
		int[] tier2 = new int[4];
		int[] tier3 = new int[4];
		for (int i = 0; i < determinedMatches.length; i++) {
			if (determinedMatches[i].getRank() == 1) {
				int scan = determinedMatches[i].getScannum();
				if (determinedMatches[i].getClassScore() < 0.5) {
					counts[0]++;
					if (setA.contains(scan)) {
						tier1[0]++;
					} else if (setB.contains(scan)) {
						tier1[1]++;
					} else if (setC.contains(scan)) {
						tier1[2]++;
					} else {
						tier1[3]++;

					}
				} else {
					counts[1]++;
					if (setA.contains(scan)) {
						tier2[0]++;
					} else if (setB.contains(scan)) {
						tier2[1]++;
					} else if (setC.contains(scan)) {
						tier2[2]++;
					} else {
						tier2[3]++;
					}
				}
			} else {
				if (determinedMatches[i].getScore() > 2
						&& !usedRank2Scans.contains(determinedMatches[i].getScannum())) {
					System.out.println(determinedMatches[i].getScannum() + "\t" + determinedMatches[i].getComString()
							+ "\t" + determinedMatches[i].getScore());
				}
			}
		}

		System.out.println("Tier 1\t" + Arrays.toString(tier1));
		System.out.println("Tier 2\t" + Arrays.toString(tier2));

		for (int i = 0; i < undeterminedMatches.length; i++) {
			if (undeterminedMatches[i].getRank() == 1) {
				counts[2]++;
				int scan = undeterminedMatches[i].getScannum();
				if (setA.contains(scan)) {
					System.out.println("miss\t" + scan);
					tier3[0]++;
				} else if (setB.contains(scan)) {
					tier3[1]++;
				} else if (setC.contains(scan)) {
					tier3[2]++;
				} else {
					tier3[3]++;
				}
			}
		}
		System.out.println("Tier 3\t" + Arrays.toString(tier3));
		System.out.println("counts\t" + Arrays.toString(counts));

		GlycoMicroNovoWriter writer = new GlycoMicroNovoWriter(out);
		writer.write(determinedMatches, undeterminedMatches, feasIdMap, featuresList, glycosyls);

		String xlsx = out.substring(0, out.lastIndexOf(".")) + ".xlsx";
		GlycoMicroNovoXlsWriter xlsWriter = new GlycoMicroNovoXlsWriter(xlsx, glycosyls, true);
		xlsWriter.write(determinedMatches, undeterminedMatches, usedRank2Scans, glycoPeptides);
		xlsWriter.close();
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
//		GlycoMicroNovoTask.onlyTest(
//				"D:\\Data\\Rui\\microbiota\\20180827\\0911\\Rui_20140530_microbiota_HILIC_step3.mzXML",
//				"D:\\Data\\Rui\\microbiota\\20180827\\0911");
		
		GlycoMicroNovoTask.batchTest("D:\\Data\\Dalian_glyco\\20210218\\N-Glyco");
		
//		GlycoMicroNovoTask.batchTest("D:\\Data\\Rui\\microbiota\\20180827\\0911");
		
//		GlycoMicroNovoTask.test("\\\\137.122.238.141\\Figeys_lab\\Kai\\data\\Xu_20160719_HM556PCA_S3.mzXML",
//				"\\\\137.122.238.141\\Figeys_lab\\Kai\\data\\Xu_20160719_HM556PCA_S3.txt");
//		GlycoMicroNovoTask.test("D:\\Data\\Rui\\mouse_diet study\\pooled\\Rui_20160314_mouse_microbiota_LFD_step3.mzXML",
//				"D:\\Data\\Rui\\mouse_diet study\\pooled\\Rui_20160314_mouse_microbiota_LFD_step3.txt");
//		GlycoMicroNovoTask.test2("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.mzXML",
//				"D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover.txt");

//		GlycoMicroNovoTask.test("D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_151224035620.mzXML",
//				"D:\\Data\\Rui\\microbiota\\Rui_20151222_mouse_microbiota_LFD_step3_151224035620.txt");
//		GlycoMicroNovoTask.test2("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.mzXML ",
//				"D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.txt");
//		GlycoMicroNovoTask.testN("D:\\Data\\Rui\\microbiota\\N_glycan\\Rui_20150712_Huh7_media_intact_HILIC_1_150715052142.mzXML", 
//				"D:\\Data\\Rui\\microbiota\\N_glycan\\Rui_20150712_Huh7_media_intact_HILIC_1_150715052142.txt");
	}

	

}
