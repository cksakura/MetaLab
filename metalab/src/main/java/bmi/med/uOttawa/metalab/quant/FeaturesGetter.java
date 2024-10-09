/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import org.apache.xmlbeans.xml.stream.XMLStreamException;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.quant.labelfree.FreeFeature;
import bmi.med.uOttawa.metalab.quant.labelfree.FreeFeatures;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

/**
 * @author Kai Cheng
 *
 */
public class FeaturesGetter {

	/**
	 * nbt.1511-S1, p9
	 */
	private final static double dm = 1.00286864;
	private double ppm = 0f;
	private int missNum = 0;
	private int leastINum = 0;

	private double ms1TotalCurrent;
	private SpectraList scanlist;

	public FeaturesGetter(String file) throws IOException, XMLStreamException, DataFormatException {
		this(file, QParameter.default_parameter());
	}

	public FeaturesGetter(String file, QParameter parameter)
			throws IOException, XMLStreamException, DataFormatException {

		this(new File(file), parameter);
	}

	public FeaturesGetter(File file) throws IOException, XMLStreamException, DataFormatException {
		this(file, QParameter.default_parameter());
	}

	public FeaturesGetter(File file, QParameter parameter) throws IOException, XMLStreamException, DataFormatException {

		this.createReader(file);

		this.ppm = parameter.getMzTole() / 1000000f;
		this.missNum = parameter.getMissNum();
		this.leastINum = parameter.getLeastINum();

	}

	public FeaturesGetter(SpectraList scanlist, double ms1TotalCurrent) {

		this(scanlist, ms1TotalCurrent, QParameter.default_parameter());
	}

	public FeaturesGetter(SpectraList scanlist, double ms1TotalCurrent, QParameter parameter) {

		this.scanlist = scanlist;
		this.ms1TotalCurrent = ms1TotalCurrent;

		this.ppm = parameter.getMzTole() / 1000000f;
		this.missNum = parameter.getMissNum();
		this.leastINum = parameter.getLeastINum();
	}

	protected void createReader(File file) throws IOException, XMLStreamException, DataFormatException {

		MzxmlReader reader = new MzxmlReader(file);
		this.scanlist = reader.getMS1Spectralist();
		this.ms1TotalCurrent = reader.getMS1TotalCurrent();
	}

	/**
	 * Find the monoisotope peak.
	 * 
	 * @param scanNum
	 * @param preMz
	 * @param charge
	 * @return
	 */
	public double findMono(int scanNum, double preMz, short charge) {

		Spectrum scan = this.scanlist.getScan(scanNum);
		Peak findpeak = new Peak(preMz, 0d);

		Peak[] peaks = scan.getPeaks();
		double tola = preMz * ppm;

		int index = Arrays.binarySearch(peaks, findpeak);

		if (index < 0) {

			index = -index - 1;

			double d1 = peaks[index].getMz() - preMz;
			double d2 = preMz - peaks[index - 1].getMz();

			index = d1 < d2 ? index : index - 1;

		} else if (index >= peaks.length - 1) {
			index = peaks.length - 1;
		}

		int loc = findPreIsotope(charge, peaks, index, tola);

		return peaks[loc].getMz();
	}

	private int findPreIsotope(short charge, Peak[] peaks, int loc, double tola) {

		double premz = peaks[loc].getMz() - dm / (double) charge;
		double preInten = peaks[loc].getIntensity();

		for (int i = loc - 1; i >= 0; i--) {

			double listmz = peaks[i].getMz();
			double listInten = peaks[i].getIntensity();

			if (Math.abs(listmz - premz) <= tola) {
				if ((listInten / preInten) >= 0.3) {

					// if((listInten/peaks[i].getIntensity())>=1.1){
					// return loc;
					// }

					loc = i;
					return findPreIsotope(charge, peaks, loc, tola);
				}
			} else if ((premz - listmz) > tola) {
				return loc;
			}
		}
		return loc;
	}

	public double findMono2(int scanNum, double preMz, short charge) {

		PixelList list0 = this.getPixelList(new Pixel(scanNum, preMz));
		preMz = list0.getAveMz();
		int length = list0.getLength();
		int limit = (length / 2) > 3 ? length / 2 : 3;

		double finMz = preMz - dm / (double) charge;
		Pixel pix = new Pixel(scanNum, finMz);
		PixelList list = this.getIsotopePixelList(pix);

		if (list.getLength() < limit || list.getInten() * 3 < list0.getInten())
			return preMz;
		else
			return findMono2(scanNum, finMz, charge);
	}

	public double findMono2(int scanNum, PixelList list0, short charge) {

		double preMz = list0.getAveMz();
		int length = list0.getLength();
		int limit = (length / 2) > 3 ? length / 2 : 3;

		double finMz = preMz - dm / (double) charge;
		Pixel pix = new Pixel(scanNum, finMz);
		PixelList list = this.getIsotopePixelList(pix);

		if (list.getLength() <= limit || list0.intenCompare(list) > 3 || list.intenCompare(list0) > 1.2) {

			return preMz;

		} else {
			return findMono2(scanNum, list, charge);
		}
	}

	public Pixel findMonoPixel(int scannum, double mz) {
		Spectrum scan = this.scanlist.getScan(scannum);
		return findMonoPixel(scan, mz);
	}

	public Pixel findMonoPixel(Spectrum scan, double mz) {

		int scanNum = scan.getScannum();
		Peak[] peaks = scan.getPeaks();
		int peakCount = peaks.length;
		double tola = mz * ppm;

		Peak findpeak = new Peak(mz - tola, 0d);

		int index = Arrays.binarySearch(peaks, findpeak);
		if (index < 0) {
			index = -index - 1;
		} else if (index >= peaks.length) {
			return new Pixel(scanNum, mz, 0);
		}
		// System.out.println("mp392\t"+scan.getScanNum()+"\t"+mz+"\t"+peaks[index].getMz()+"\t"+peaks[index-1].getMz());

		Pixel pix = new Pixel(scanNum, mz, 0);
		double inten1 = 0;

		for (int i = index; i < peakCount; i++) {
			double mass = peaks[i].getMz();
			double inten = peaks[i].getIntensity();
			if (Math.abs(mz - mass) <= tola) {

				if (inten > inten1) {

					pix = new Pixel(scanNum, mass, inten);
					inten1 = inten;
				}

			} else if (mass - mz > tola)
				break;
		}

		return pix;
	}

	public Pixel getPixel(Spectrum scan, double mz, double ppm) {

		int scanNum = scan.getScannum();
		Peak[] peaks = scan.getPeaks();
		int peakCount = peaks.length;
		double tola = mz * ppm * 1E-6;

		Peak findpeak = new Peak(mz - tola, 0d);

		int index = Arrays.binarySearch(peaks, findpeak);
		if (index < 0) {
			index = -index - 1;
		} else if (index >= peaks.length) {
			return new Pixel(scanNum, mz, 0);
		}
		// System.out.println("mp392\t"+scan.getScanNum()+"\t"+mz+"\t"+peaks[index].getMz()+"\t"+peaks[index-1].getMz());

		Pixel pix = new Pixel(scanNum, mz, 0);
		double inten1 = 0;

		for (int i = index; i < peakCount; i++) {
			double mass = peaks[i].getMz();
			double inten = peaks[i].getIntensity();
			if (Math.abs(mz - mass) <= tola) {

				if (inten > inten1) {

					pix = new Pixel(scanNum, mass, inten);
					inten1 = inten;
				}

			} else if (mass - mz > tola)
				break;
		}

		return pix;
	}

	public PixelList getIsotopePixelList(Pixel pix) {

		int scanNum = pix.getScanNum();
		PixelList pixList = new PixelList(pix);

		double mz = pix.getMz();

		Spectrum next;
		int nextscan = scanNum;
		while ((next = scanlist.getNextScan(nextscan)) != null) {

			nextscan = next.getScannum();
			Pixel nexPix = getPixel(next, mz, 10);

			if (nexPix != null) {

				pixList.addPixel(nexPix);

			} else {
				break;
			}
		}

		Spectrum prev;
		int prevScan = scanNum;
		while ((prev = scanlist.getPreviousScan(prevScan)) != null) {

			prevScan = prev.getScannum();
			Pixel prePix = getPixel(prev, mz, 10);

			if (prePix != null) {

				pixList.addPixel(prePix);

			} else {
				break;
			}
		}

		return pixList;
	}

	public PixelList getPixelList(Pixel pix) {

		int scanNum = pix.getScanNum();
		double mz = pix.getMz();

		PixelList pixList;
		boolean miss1;
		boolean miss2;
		int missCount1;
		int missCount2;

		if (pix.getInten() > 0) {

			pixList = new PixelList(pix);
			miss1 = false;
			miss2 = false;
			missCount1 = 0;
			missCount2 = 0;

		} else {

			pixList = new PixelList();
			miss1 = true;
			miss2 = true;
			missCount1 = 1;
			missCount2 = 1;
		}

		Spectrum next;
		int nextscan = scanNum;
		while ((next = scanlist.getNextScan(nextscan)) != null) {

			nextscan = next.getScannum();
			Pixel nexPix = findMonoPixel(next, mz);

			if (nexPix != null) {

				pixList.addPixel(nexPix);

				if (miss1) {
					missCount1 = 0;
					miss1 = false;
				}
			} else {
				if (miss1)
					missCount1++;
				else
					miss1 = true;
			}
			if (missCount1 >= 2)
				break;
		}

		Spectrum prev;
		int prevScan = scanNum;
		while ((prev = scanlist.getPreviousScan(prevScan)) != null) {

			prevScan = prev.getScannum();
			Pixel prePix = findMonoPixel(prev, mz);

			if (prePix != null) {

				pixList.addPixel(prePix);
				if (miss2) {
					missCount2 = 0;
					miss2 = false;
				}
			} else {
				if (miss2)
					missCount2++;
				else
					miss2 = true;
			}
			if (missCount2 >= 2)
				break;
		}

		return pixList;
	}

	public FreeFeature getFeature(Pixel pix, int scanNum) {
		return this.getFreeFeature(pix, scanlist.getScan(scanNum));
	}

	public FreeFeature getFreeFeature(Pixel pix, Spectrum scan) {

		Peak[] peaks = scan.getPeaks();
		int peakCount = peaks.length;
		int value = pix.getCharge();

		double rt = (double) scan.getRt();

		double mz1 = pix.getMz();
		double mz2 = mz1 + dm / (double) value;
		double mz3 = mz2 + dm / (double) value;

		double tola = mz1 * ppm;
		Pixel pix1 = null;
		Pixel pix2 = null;
		Pixel pix3 = null;

		double inten1 = 0;
		double inten2 = 0;
		double inten3 = 0;

		int scanNum = scan.getScannum();
		Peak findpeak = new Peak(mz1 - tola, 0d);

		int index = Arrays.binarySearch(peaks, findpeak);
		if (index < 0) {
			index = -index - 1;
		} else if (index >= peaks.length) {
			return null;
		}

		for (int i = index; i < peakCount; i++) {
			double mass = peaks[i].getMz();
			double inten = peaks[i].getIntensity();
			if (Math.abs(mz1 - mass) < tola) {

				// System.out.println("1\t"+scanNum+"\t"+mass);

				if (inten > inten1) {
					pix1 = new Pixel(scanNum, mass, inten);
					inten1 = inten;
				}
			} else if (Math.abs(mz2 - mass) < tola) {

				// System.out.println("2\t"+scanNum+"\t"+mass);

				if (inten > inten2) {
					pix2 = new Pixel(scanNum, mass, inten);
					inten2 = inten;
				}
			} else if (Math.abs(mz3 - mass) < tola) {

				// System.out.println("3\t"+scanNum+"\t"+mass);

				if (inten > inten3) {
					pix3 = new Pixel(scanNum, mass, inten);
					inten3 = inten;
				}
			} else if (mass - mz3 > 0.1)
				break;
		}

		if (pix1 == null || pix2 == null || pix3 == null) {

			return null;

		} else {

			double d1 = pix2.getMz() - pix1.getMz();
			double d2 = pix3.getMz() - pix2.getMz();
			if (Math.abs(d1 - d2) > tola)
				return null;

			double totalInten = inten1 + inten2 + inten3;
			FreeFeature f = new FreeFeature(scanNum, value, mz1, totalInten);
			f.setRT(rt);
			f.validate();

			return f;
		}
	}

	/**
	 * @param p
	 * @return
	 */
	public FreeFeatures getFreeFeatures(Pixel pix) {
		// TODO Auto-generated method stub

		int scanNum = pix.getScanNum();

		while (true) {
			if (this.scanlist.getScan(scanNum) != null) {
				break;
			} else {
				scanNum--;
			}
		}

		FreeFeature f = getFeature(pix, scanNum);
		FreeFeatures fs;
		boolean miss1 = false;
		boolean miss2 = false;
		int missCount1 = 0;
		int missCount2 = 0;
		if (f == null) {
			fs = new FreeFeatures(pix.getCharge());
			miss1 = true;
			miss2 = true;
			missCount1 = 1;
			missCount2 = 1;
		} else {
			fs = new FreeFeatures(f);
			// System.out.println("255\t"+f);
		}

		int down1 = 0;
		ArrayList<Double> tailInten1 = new ArrayList<Double>();

		Spectrum next;
		int nextscan = scanNum;
		while ((next = scanlist.getNextScan(nextscan)) != null) {
			nextscan = next.getScannum();
			FreeFeature fi = getFreeFeature(pix, next);
			if (fi != null) {
				// System.out.println("267\t"+fi);
				// remove the tail

				if (tailInten1.size() == 5) {

					double oldInten = MathTool.getAve(tailInten1);
					tailInten1.remove(0);
					tailInten1.add(fi.getIntensity());
					double newInten = MathTool.getAve(tailInten1);

					if (newInten < oldInten * 1.1) {
						down1++;
					}
					if (down1 >= 10)
						break;

				} else {
					tailInten1.add(fi.getIntensity());
				}

				fs.addFeature(fi);
				if (miss1) {
					missCount1 = 0;
					miss1 = false;
				}
			} else {
				if (miss1)
					missCount1++;
				else
					miss1 = true;
			}
			if (missCount1 >= missNum)
				break;
		}

		Spectrum prev;
		int prevScan = scanNum;
		while ((prev = scanlist.getPreviousScan(prevScan)) != null) {

			prevScan = prev.getScannum();
			FreeFeature fi = getFreeFeature(pix, prev);
			if (fi != null) {
				// System.out.println("309\t"+fi);
				fs.addFeature(fi);
				if (miss2) {
					missCount2 = 0;
					miss2 = false;
				}
			} else {
				if (miss2)
					missCount2++;
				else
					miss2 = true;
			}
			if (missCount2 >= missNum)
				break;
		}

		if (fs.getLength() > 0) {

			fs.setInfo();

		} else {

			double mono = pix.getMz();
			double value = pix.getCharge();
			double mr = (mono - AminoAcidProperty.PROTON_W) * value;

			fs.setPepMass(mr);
		}

		return fs;
	}

	public void setPPM(double ppm) {
		this.ppm = ppm;
	}

	public void setLeastIdenNum(int num) {
		this.leastINum = num;
	}

	public int getLeastIdenNum() {
		return this.leastINum;
	}

	public double getMS1TotalCurrent() {
		return ms1TotalCurrent;
	}

	public void close() {
		this.scanlist = null;
		System.gc();
	}

}
