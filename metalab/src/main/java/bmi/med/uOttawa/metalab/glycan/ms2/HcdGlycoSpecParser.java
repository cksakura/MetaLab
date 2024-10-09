/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.glycoCT.GlycoDatabaseMatcher;
import bmi.med.uOttawa.metalab.glycan.ms2.NCoreMatcher.NCoreInfo;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.PeakListFilter;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

/**
 * @author Kai Cheng
 *
 */
public class HcdGlycoSpecParser {

	private MzxmlReader reader;
	private GlycoJudgeParameter jpara;
	private NCoreMatcher ncMatcher;
	private GlycoDatabaseMatcher gdMatcher;

	private HashMap<Integer, Integer> isotopeMap;
	
	private double intenThres;
	private double  mzThresPPM;
	private double  mzThresAMU;
	private double filterMassRange = 100;
	private int filterTopN = 8;
	
	private int topn;
	private int ms2count;
	private int glycoSpectraCount;
	private NGlycoSSM [] ssms;

	/**
	 * nbt.1511-S1, p9
	 */
	protected static final double dm = 1.00286864;

	private static final double nGlycanCoreFuc = 1038.375127;
	protected static final double Hex = Glycosyl.Hex.getMonoMass();
	protected static final double HexNAc = Glycosyl.HexNAc.getMonoMass();
		
	public HcdGlycoSpecParser(String peakfile) throws IOException, XMLStreamException, DataFormatException {
		
		this.reader = new MzxmlReader(peakfile);
		this.jpara = GlycoJudgeParameter.defaultParameter();
		this.ncMatcher = new NCoreMatcher(jpara.getMzThresPPM());
		this.gdMatcher = new GlycoDatabaseMatcher(jpara.getMzThresPPM());
		this.isotopeMap = new HashMap<Integer, Integer>();
		this.parseNGlyco();
	}
	
	public HcdGlycoSpecParser(String peakfile, GlycoJudgeParameter jpara) throws XMLStreamException, IOException, DataFormatException{
		
		this.reader = new MzxmlReader(peakfile);
		this.jpara = jpara;
		this.ncMatcher = new NCoreMatcher(jpara.getMzThresPPM());
		this.gdMatcher = new GlycoDatabaseMatcher(jpara.getMzThresPPM());
		this.isotopeMap = new HashMap<Integer, Integer>();
		this.parseNGlyco();
	}

	private void parseNGlyco() throws IOException, XMLStreamException, DataFormatException{

		HashMap <Integer, Spectrum> ms2ScanMap = new HashMap <Integer, Spectrum>();
		HashMap <Integer, Double> neuAcMap = new HashMap <Integer, Double>();
//		double ms1TotalCurrent = reader.getMS1TotalCurrent();
		
		double [] mzs = new double [5];
		
		// 162.052824; 204.086649; 274.08741263499996; 292.102692635; 366.139472; 657.2348890000001
		mzs[0] = Glycosyl.Hex.getMonoMass()+AminoAcidProperty.PROTON_W;
		mzs[1] = Glycosyl.HexNAc.getMonoMass()+AminoAcidProperty.PROTON_W;
		mzs[2] = Glycosyl.NeuAc_H2O.getMonoMass()+AminoAcidProperty.PROTON_W;
		mzs[3] = Glycosyl.NeuAc.getMonoMass()+AminoAcidProperty.PROTON_W;
		mzs[4] = Glycosyl.Hex.getMonoMass()+Glycosyl.HexNAc.getMonoMass()+AminoAcidProperty.PROTON_W;
		
		intenThres = jpara.getIntenThres();
		mzThresPPM = jpara.getMzThresPPM();
		mzThresAMU = jpara.getMzThresAMU();
		topn = jpara.getTopnStructure();

		ArrayList <NGlycoSSM> list = new ArrayList <NGlycoSSM>();
		Spectrum spectrum;
		Peak [] ms1Peaks = null;

		while((spectrum=reader.getNextSpectrum())!=null){

			int msLevel = spectrum.getMslevel();
			double totIonCurrent = spectrum.getTotalCurrent();
			
			if(msLevel>1){
				ms2count++;
				double preMz = spectrum.getPrecursorMz();
				int preCharge = spectrum.getPrecursorCharge();
				int snum = spectrum.getScannum();

				if(preMz*preCharge <= nGlycanCoreFuc)
					continue;

				int count = 0;
				
				Peak [] peaks = spectrum.getPeaks();
				double [] symbolPeakIntensity = new double [5];

L:				for(int i=0;i<peaks.length;i++){

					double mz = peaks[i].getMz();
					double inten = peaks[i].getIntensity();
					if(inten/totIonCurrent < intenThres)
						continue;

					if((mz-mzs[4])> mzThresAMU){
						break;
					}
					
					for(int j=0;j<mzs.length;j++){
						
						if((mzs[j]-mz)> mzThresAMU)
							continue L;
						
						if(Math.abs(mz-mzs[j]) <= mzThresAMU){
							if(symbolPeakIntensity[j]==0){
								symbolPeakIntensity[j] = inten;
								count++;
							}else{
								if(inten>symbolPeakIntensity[j]){
									symbolPeakIntensity[j] = inten;
								}
							}
						}
					}
				}

				if(count>=2){
					this.glycoSpectraCount++;
//if(snum==4357) System.out.println(snum+"\t"+ms2.getPrecursorMZ());
					
					this.findIsotope(spectrum, ms1Peaks);
					ms2ScanMap.put(snum, spectrum);
					double neuAcScore = 0;
					if(symbolPeakIntensity[1]>0){
						neuAcScore += symbolPeakIntensity[2]>0 ? (symbolPeakIntensity[2]/symbolPeakIntensity[1]+1) : 0;
						neuAcScore += symbolPeakIntensity[3]>0 ? (symbolPeakIntensity[3]/symbolPeakIntensity[1]+1) : 0;
					}else{
						neuAcScore += symbolPeakIntensity[2]>0 ? 1 : 0;
						neuAcScore += symbolPeakIntensity[3]>0 ? 1 : 0;
					}
					neuAcMap.put(snum, neuAcScore);
//if(snum==8195) System.out.println("NGlycoSpecStrucGetter205\t"+snum+"\t"+Arrays.toString(symbolPeakIntensity)+"\t"+neuAcScore);					
				}
	
			}else if(msLevel==1){
//				this.scanlist.add(spectrum);
				ms1Peaks = spectrum.getPeaks();
			}
		}

		Integer [] scans = ms2ScanMap.keySet().toArray(new Integer[ms2ScanMap.size()]);
		Arrays.sort(scans);
		
		for(int scanid=0;scanid<scans.length;scanid++){

			int scannum = scans[scanid];
			Spectrum ms2 = ms2ScanMap.get(scannum);
			
//if(scannum==8195){			
			double preMz = ms2.getPrecursorMz();
			int preCharge = ms2.getPrecursorCharge();
			int preScanNum0 = ms2.getPrecursorScan();
			double rt = ms2.getRt();
			
			Peak [] peaks = ms2.getPeaks();
			Peak [] filteredPeaks = PeakListFilter.TopNFilter(peaks, filterMassRange, filterTopN);
			
			NCoreInfo ncinfo = this.ncMatcher.match(filteredPeaks, preCharge);
			if(ncinfo==null) continue;
			
			NGlycoSSM [] ssm = this.gdMatcher.match(filteredPeaks, scannum, preMz, preCharge, 
					ncinfo, isotopeMap.get(scannum), neuAcMap.get(scannum));

			if(ssm!=null){

				for(int i=0;i<ssm.length;i++){
					
//					if(ssm[i].getRank()==1)System.out.println(scannum+"\t"+ssm[i].getPepMass()+"\t"+(ssm[i].getPepMass()+203)+"\t"+(ssm[i].getPepMass()+203)/2.0+"\t"
//							+ssm[i].getPreMz()+"\t"+ssm[i].getScore()+"\t"+ssm[i].getName());
//						System.out.println("NGlycoSpecStrucgetter259\t"+ssm[i].getPepMass()+"\t"+ssm[i].getPreMz()+"\t"+ssm[i].getScore()+"\t"+ssm[i].getGlycoMass()
//						+"\t"+neuAcMap.get(scannum)+"\t"+ssm[i].getName()+"\t"+ssm.length+"\t"+isotopeMap.get(scannum));

					if(ssm[i].getRank()>topn){
						break;
					}

					ssm[i].setMS1Scannum(preScanNum0);
					ssm[i].setRT(rt);

					list.add(ssm[i]);
				}
			}else{
//				System.out.println("null");
			}
//break;}	
		}

		this.ssms = list.toArray(new NGlycoSSM[list.size()]);
	}
	
	private void findIsotope(Spectrum scan, Peak [] ms1Peaks){

		double mz = scan.getPrecursorMz();
		double intensity = scan.getPrecursorIntensity();
		
		Peak precursorPeak = new Peak(mz, intensity);
		int isoloc = Arrays.binarySearch(ms1Peaks, precursorPeak);
		if(isoloc<0) isoloc = -isoloc-1;
		if(isoloc>=ms1Peaks.length){
			this.isotopeMap.put(scan.getScannum(), 0);
			return;
		}
		int charge = scan.getPrecursorCharge();
		
		int k=1;
		int i=isoloc;

		for(;i>=0;i--){
			
			double delta = mz-ms1Peaks[i].getMz()-k*dm/(double)charge;
			double loginten = Math.log10(intensity/ms1Peaks[i].getIntensity());
			if(Math.abs(delta)<=mz*mzThresPPM*1E-6){
				if(Math.abs(loginten)<1){
					k++;
					if(intensity<ms1Peaks[i].getIntensity())
						intensity = ms1Peaks[i].getIntensity();
					
				}
			}else if(delta>mz*mzThresPPM*1E-6){
				break;
			}
			
			if(k>7) break;
		}
		this.isotopeMap.put(scan.getScannum(), k-1);
	}
	
	/**
	 * not validated
	 * @deprecated
	 * @param peaks
	 * @return
	 */
	private Peak [] filterPeakList(Peak [] peaks){
		
		double intenthres1 = 0;
		double intenthres2 = 0;
		
		if(peaks.length>=300){
			intenthres1 = 0.4;
		}else if(peaks.length<300 && peaks.length>=200){
			intenthres1 = 0.35;
		}else if(peaks.length<200 && peaks.length>=140){
			intenthres1 = 0.28;
		}else if(peaks.length<140 && peaks.length>=70){
			intenthres1 = 0.2;
		}else{
			intenthres1 = 0.1;
		}
		
		ArrayList <Double> tempintenlist = new ArrayList <Double>();
		double [] rsdlist = new double[peaks.length];
		double percent = 0;
		for(int i=peaks.length-1;i>=0;i--){
			tempintenlist.add(peaks[i].getIntensity());
			rsdlist[tempintenlist.size()-1] = MathTool.getRSD(tempintenlist);
		}

		for(int i=1;i<=rsdlist.length;i++){
			
			if(rsdlist[rsdlist.length-i]<intenthres1){
				intenthres2 = peaks[i-1].getIntensity();
				percent = ((double)peaks.length-i+1)/((double)peaks.length);
				break;
			}
		}
		
		ArrayList <Peak> filteredList = new ArrayList <Peak>();
		for(int i=0;i<peaks.length;i++){
			if(peaks[i].getIntensity()>intenthres2){
				filteredList.add(peaks[i]);
			}
		}
		
		return filteredList.toArray(new Peak[filteredList.size()]);
	}

	public NGlycoSSM[] getGlycoSSMs() {
		return ssms;
	}

	public int getMS2Count() {
		return this.ms2count;
	}

	public int getGlycoSpectraCount() {
		return this.glycoSpectraCount;
	}

}
