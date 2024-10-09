package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.Aminoacids;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoTree;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

public class GlycoRestrictedParser extends AbstractGlycoParser {

	private static final Logger LOGGER = LogManager.getLogger(GlycoRestrictedParser.class);
	
	private File spectraInput;
	
	private Glycosyl[] restrictedGlycosyls;
	private HashMap<String, Glycosyl> glycosylMap;
	private HashMap<String, int[]> modMap;
	
	public GlycoRestrictedParser(SpectraList ms1SpectraList, SpectraList ms2SpectraList, Glycosyl[] glycosyls,
			double precursorPPM, double fragmentPPM) {

		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;
/*
		this.restrictedGlycosyls = glycosyls;
		this.glycans = new MicroMonosaccharides(glycosyls);
		this.glycanMasses = glycans.getSortedMasses();
		this.oxoniumMasses = glycans.getOxoniumMasses();
		this.combineGlycanMasses = glycans.getCombineGlycosylMasses();
		this.combineGlycans = glycans.getCombineCompositions();
*/
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
	
	@Override
	public Glycosyl[] getAllGlycosyls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	NovoGlycoCompMatch[] parseGSM(Spectrum ms2spectrumi, double monoMz, int charge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, boolean determined) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	NovoGlycoCompMatch judge(double delta, double tolerance, int[] basicComposition, int scannum, DenovoGlycoTree tree,
			double precursorMz, int precursorCharge, Peak[] peaks, HashMap<Integer, ArrayList<Integer>> oxoniumGlycoMap,
			HashMap<Integer, GlycoPeakNode> oxoniumIonMap, MicroMonosaccharides glycans, double combineGlycanMass) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
