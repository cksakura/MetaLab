package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.RegionTopNIntensityFilter;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

public class DenovoTest {

	private static void oxoTest(String in, int scanRange, double ppm)
			throws XMLStreamException, DataFormatException, IOException {

		RegionTopNIntensityFilter peakFilter = new RegionTopNIntensityFilter(4, 100, 0.01);
		MzxmlReader reader = new MzxmlReader(in);
		Spectrum spectrum;
		int scanEnd = 0;
		double[] mzs = null;
		HashMap<Double, Integer> count = null;
		while ((spectrum = reader.getNextSpectrum()) != null) {
			int level = spectrum.getMslevel();
			int scannum = spectrum.getScannum();
			if (scannum > scanEnd) {
				if (mzs != null) {
					for (int i = 0; i < mzs.length; i++) {
						System.out.println("from " + (scanEnd - scanRange) + " to " + (scanEnd) + ":\t" + mzs[i] + "\t"
								+ count.get(mzs[i]));
					}
				}
				mzs = null;
				count = new HashMap<Double, Integer>();
				scanEnd = scanEnd + scanRange;
			}
			if (level == 2) {
				Peak[] filteredPeaks = peakFilter.filter(spectrum.getPeaks());

				if (mzs == null) {

					Peak peak0 = new Peak(1000, 0);
					int id = Arrays.binarySearch(filteredPeaks, peak0);
					if (id < 0)
						id = -id - 1;

					mzs = new double[id];
					count = new HashMap<Double, Integer>();
					for (int i = 0; i < id; i++) {
						mzs[i] = filteredPeaks[i].getMz();
						count.put(mzs[i], 1);
					}
				} else {
					for (int i = 0; i < filteredPeaks.length; i++) {
						double mz = filteredPeaks[i].getMz();
						if (mz > 1000)
							continue;
						int id = Arrays.binarySearch(mzs, mz);
						if (id < 0)
							id = -id - 1;

						double tole = mz * ppm * 1E-6;
						if (id > 0) {
							if (mz - mzs[id - 1] < tole) {
								count.put(mzs[id - 1], count.get(mzs[id - 1]) + 1);
								continue;
							}
						}
						if (id < mzs.length - 1) {
							if (mzs[id + 1] - mz < tole) {
								count.put(mzs[id + 1], count.get(mzs[id + 1]) + 1);
								continue;
							}
						}
						double[] newMzs = new double[mzs.length + 1];

						System.arraycopy(mzs, 0, newMzs, 0, id);
						newMzs[id] = mz;

						if (id <= mzs.length)
							System.arraycopy(mzs, id, newMzs, id + 1, mzs.length - id);

						count.put(mz, 1);
					}
				}
			}
		}

		if (mzs != null) {
			for (int i = 0; i < mzs.length; i++) {
				System.out.println("from " + (scanEnd - scanRange) + " to " + (scanEnd) + ":\t" + mzs[i] + "\t"
						+ count.get(mzs[i]));
			}
		}
		mzs = null;
	}

	private static void extractSpectra(String in, String out, String scan) throws IOException {
		String key = scan + "." + scan;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out + "\\" + scan + ".mgf");
		String line = null;
		boolean start = false;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("TITLE")) {
				if (line.contains(key)) {
					writer.println("BEGIN IONS");
					writer.println(line);
					start = true;
				}
			} else if (line.startsWith("END")) {
				if (start) {
					writer.println(line);
					break;
				}
			} else if (line.startsWith("PEPMASS")) {
				if (start) {
					writer.println(line);
				}
			} else {
				if (start) {
					writer.println(line);
				}
			}
		}
		reader.close();
		writer.close();
	}

	public static void main(String[] args) throws XMLStreamException, DataFormatException, IOException {
		// TODO Auto-generated method stub

//		DenovoTest.oxoTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3.mzXML", 10000, 50);

//		DenovoTest.extractSpectra(
//				"D:\\Data\\Rui\\mouse_diet study\\separate\\analysis\\Rui_20160209_mouse_microbiota_HFD_#5_step1.mgf",
//				"D:\\Data\\Rui\\mouse_diet study\\separate\\analysis", "39684");

	}

}
