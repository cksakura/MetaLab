/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

/**
 * @author Kai Cheng
 *
 */
public class MgfWriter {

	private static final DecimalFormat DF4 = new DecimalFormat("#.####");

	private static Logger LOGGER = LogManager.getLogger();

	private PrintWriter writer;

	public MgfWriter(String mgffile) throws IOException {
		this.writer = new PrintWriter(mgffile);
	}

	public MgfWriter(File mgffile) {
		try {
			this.writer = new PrintWriter(mgffile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing spectra list to " + mgffile, e);
		}
	}

	public void write(Spectrum spectrum) {

		StringBuilder sb = new StringBuilder(1000);

		sb.append("BEGIN IONS").append("\n");
		sb.append("PEPMASS=").append(DF4.format(spectrum.getPrecursorMz())).append("\n");
		sb.append("CHARGE=").append(spectrum.getPrecursorCharge()).append('+').append("\n");
		sb.append("SCANS=").append(spectrum.getScannum()).append("\n");

		Peak[] peaks = spectrum.getPeaks();
		for (int i = 0; i < peaks.length; i++) {
			sb.append(DF4.format(peaks[i].getMz())).append(' ').append(DF4.format(peaks[i].getIntensity()))
					.append("\n");
		}

		sb.append("END IONS");

		this.writer.println(sb);
	}

	public void writeMascotFormat(Spectrum spectrum, String rawfile) {

		int charge = spectrum.getPrecursorCharge();
		double premz = spectrum.getPrecursorMz();
		double intensity = spectrum.getPrecursorIntensity();
		double rt = spectrum.getRt();
		int scannum = spectrum.getScannum();
		StringBuilder sb = new StringBuilder();

		sb.append("BEGIN IONS").append("\n");
		sb.append("PEPMASS=").append(DF4.format(premz)).append("\n");
		sb.append("CHARGE=").append(charge).append('+').append("\n");
		sb.append("TITLE=");
		sb.append("Elution from: ").append(DF4.format(rt)).append(" to ").append(DF4.format(rt));
		sb.append(" period: 0 experiment: 1 cycles: 1 precIntensity: ").append(DF4.format(intensity));
		sb.append(" FinneganScanNumber: ").append(scannum);
		sb.append(" MStype: enumIsNormalMS rawFile: ").append(rawfile).append(" \n");

		Peak[] peaks = spectrum.getPeaks();
		for (int i = 0; i < peaks.length; i++) {
			sb.append(DF4.format(peaks[i].getMz())).append(' ').append(DF4.format(peaks[i].getIntensity()))
					.append("\n");
		}
		sb.append("END IONS");

		this.writer.println(sb);
	}

	public void writeMSCFormat(Spectrum spectrum, String rawfile) {

		int charge = spectrum.getPrecursorCharge();
		double premz = spectrum.getPrecursorMz();
		double intensity = spectrum.getPrecursorIntensity();
		double rt = spectrum.getRt();
		int scannum = spectrum.getScannum();
		StringBuilder sb = new StringBuilder();

		sb.append("BEGIN IONS").append("\n");
		sb.append("TITLE=").append(rawfile).append(".").append(scannum).append(".").append(scannum).append(".")
				.append(charge).append("\n");
		sb.append("RTINSECONDS=").append(rt).append("\n");
		sb.append("PEPMASS=").append(DF4.format(premz)).append(" ").append(intensity).append("\n");
		sb.append("CHARGE=").append(charge).append('+').append("\n");

		Peak[] peaks = spectrum.getPeaks();
		for (int i = 0; i < peaks.length; i++) {
			sb.append(DF4.format(peaks[i].getMz())).append(' ').append(DF4.format(peaks[i].getIntensity()))
					.append("\n");
		}
		sb.append("END IONS");

		this.writer.println(sb);
	}

	private Peak findIsotope(Peak precursorPeak, int charge, Peak[] ms1Peaks) {

		double mzThresPPM = 1E-5;
		double dm = 1.00286864;

		int isoloc = Arrays.binarySearch(ms1Peaks, precursorPeak);
		if (isoloc < 0)
			isoloc = -isoloc - 1;
		double mz = precursorPeak.getMz();
		double intensity = precursorPeak.getIntensity();
		Peak peak = null;
		int i = isoloc;

		for (; i >= 0; i--) {
			double delta = mz - ms1Peaks[i].getMz() - dm / (double) charge;
			double loginten = Math.log10(intensity / ms1Peaks[i].getIntensity());
			if (Math.abs(delta) <= mz * mzThresPPM) {
				if (Math.abs(loginten) < 1) {
					mz = ms1Peaks[i].getMz();
					peak = ms1Peaks[i];
					if (intensity < ms1Peaks[i].getIntensity())
						intensity = ms1Peaks[i].getIntensity();
				}
			} else if (delta > mz * mzThresPPM * 1E-6) {
				break;
			}
		}

		return peak;
	}

	private int findCharge(Peak precursorPeak, Peak[] ms1Peaks) {

		double dm = 1.00286864;
		int isoloc = Arrays.binarySearch(ms1Peaks, precursorPeak);
		if (isoloc < 0)
			isoloc = -isoloc - 1;

		double premz = precursorPeak.getMz();
		int[] charges = new int[] { 2, 3, 4, 5 };
		double[][] intensities = new double[charges.length][2];

		for (int i = isoloc; i < ms1Peaks.length; i++) {

			double mzi = ms1Peaks[i].getMz();
			double inten = ms1Peaks[i].getIntensity();

			for (int j = 0; j < charges.length; j++) {
				for (int k = 1; k <= 2; k++) {
					if (Math.abs(mzi - premz - dm / (double) charges[j] * k) < 0.01) {
						intensities[j][k - 1] += inten;
					}
				}
			}
		}

		int charge = 0;
		double max = 0;
		for (int i = 0; i < charges.length; i++) {
			double totali = intensities[i][0] + intensities[i][1];
			if (totali > max) {
				max = totali;
				charge = charges[i];
			}
		}

		return charge;
	}

	public void plusTen(String in) {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading spectra from " + in, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("PEPMASS")) {
					double mass = Double.parseDouble(line.substring(line.indexOf("=") + 1));
					this.writer.write("PEPMASS=" + (mass + 10.0) + "\n");
				} else {
					this.writer.write(line + "\n");
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading spectra from " + in, e);
		}

		this.close();
	}

	public void correction(String in, double ppm) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading spectra from " + in, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("PEPMASS")) {
					double mass = Double.parseDouble(line.substring(line.indexOf("=") + 1));
					mass = mass - mass * ppm * 1E-6;
					this.writer.write("PEPMASS=" + DF4.format(mass) + "\n");
				} else {
					this.writer.write(line + "\n");
				}
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading spectra from " + in, e);
		}

		this.close();
	}

	public void close() {
		this.writer.close();
	}

}
