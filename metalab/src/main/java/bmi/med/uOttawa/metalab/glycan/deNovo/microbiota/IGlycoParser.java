package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.RegionTopNIntensityFilter;
import bmi.med.uOttawa.metalab.spectra.SpectraList;

/**
 * <p>
 * parse a glycan composition from a MS2 spectra
 * <p>
 * the spectra can be from either a glycopeptide or a glycan
 * 
 * @author Kai Cheng <ckkazuma@gmail.com>
 *
 */
public interface IGlycoParser {

	int maxN = 3;
	int scansLengthLimit = 4;
	double ms2Tolerance = 0.1;
	RegionTopNIntensityFilter peakFilter = new RegionTopNIntensityFilter(5, 100);

	/**
	 * 
	 * @return
	 */
	public Integer[] getMs2scans();

	/**
	 * 
	 * @return
	 */
	public SpectraList getMs1SpectraList();

	/**
	 * 
	 * @return
	 */
	public SpectraList getMs2SpectraList();

	/**
	 * parseSpectrum(i, <b>false</b>)
	 * 
	 * @param i
	 */
	public void parseSpectrum(int i);

	/**
	 * 
	 * @param i             the id of the current MS2 scan (not the MS2 scannum)
	 * @param getMS1Feature if true, detect the extracted-ion chromatogram from the
	 *                      MS1 range for the quantitative analysis. if not, do the
	 *                      quantification by other method
	 */
	public void parseSpectrum(int i, boolean getMS1Feature);

	/**
	 * 
	 * @return
	 */
	public ArrayList<NovoGlycoCompMatch> getDeterminedMatchList();

	/**
	 * 
	 * @return
	 */
	public ArrayList<NovoGlycoCompMatch> getUndeterminedMatchList();

	/**
	 * 
	 * @return
	 */
	public HashSet<Integer> getUsedRank2Scans();

	/**
	 * 
	 * @return
	 */
	public HashMap<Integer, Integer> getFeasIdMap();

	/**
	 * 
	 * @return
	 */
	public ArrayList<Features> getFeaturesList();

	/**
	 * 
	 * @return
	 */
	public Glycosyl[] getAllGlycosyls();

	/**
	 * 
	 * @param rawfile the name of the raw file, which is used to complete the
	 *                information in "TITLE="
	 * @param output  should be a folder because multiple spectra files will be
	 *                generated
	 * @return the generated spectra files
	 * @throws IOException
	 */
	public File[] exportPepSpectra(String rawfile, String output) throws IOException;
}
