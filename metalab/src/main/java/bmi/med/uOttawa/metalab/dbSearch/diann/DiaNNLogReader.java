package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DiaNNLogReader {

	private double rtWindow = -1;
	private double peakWidth = -1;
	private double scanWindow = -1;
	private double ms1Accu = -1;
	private double optiAccu = -1;

	public DiaNNLogReader(String in) {
		this(new File(in));
	}

	public DiaNNLogReader(File in) {
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(in));
			while ((line = reader.readLine()) != null) {
				int id = line.indexOf("]");
				if (id > 0 && id < line.length()) {
					line = line.substring(id + 1).trim();

					if (line.startsWith("RT window set to ")) {
						line = line.substring("RT window set to ".length()).trim();

						try {
							double rtWindow = Double.parseDouble(line);
							if (rtWindow > this.rtWindow) {
								this.rtWindow = rtWindow;
							}
						} catch (NumberFormatException e) {

						}
					} else if (line.startsWith("Peak width: ")) {
						line = line.substring("Peak width: ".length()).trim();

						try {
							double peakWidth = Double.parseDouble(line);
							if (peakWidth > this.peakWidth) {
								this.peakWidth = peakWidth;
							}
						} catch (NumberFormatException e) {

						}
					} else if (line.startsWith("Scan window radius set to ")) {
						line = line.substring("Scan window radius set to ".length()).trim();

						try {
							double scanWindow = Double.parseDouble(line);
							if (scanWindow > this.scanWindow) {
								this.scanWindow = scanWindow;
							}
						} catch (NumberFormatException e) {

						}
					} else if (line.startsWith("Recommended MS1 mass accuracy setting: ")) {
						line = line.substring("Recommended MS1 mass accuracy setting: ".length(),
								line.length() - " ppm".length()).trim();

						try {
							double ms1Accu = Double.parseDouble(line);
							if (ms1Accu > this.ms1Accu) {
								this.ms1Accu = ms1Accu;
							}
						} catch (NumberFormatException e) {

						}
					} else if (line.startsWith("Optimised mass accuracy: ")) {
						line = line.substring("Optimised mass accuracy: ".length(), line.length() - " ppm".length())
								.trim();

						try {
							double optiAccu = Double.parseDouble(line);
							if (optiAccu > this.optiAccu) {
								this.optiAccu = optiAccu;
							}
						} catch (NumberFormatException e) {

						}
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public double getRtWindow() {
		return rtWindow;
	}

	public double getPeakWidth() {
		return peakWidth;
	}

	public double getScanWindow() {
		return scanWindow;
	}

	public double getMs1Accu() {
		return ms1Accu;
	}

	public double getOptiAccu() {
		return optiAccu;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		DiaNNLogReader reader = new DiaNNLogReader(
				"Z:\\Kai\\Raw_files\\PXD008738\\12mix\\hap\\hap.log.txt");
		System.out.println(reader.getMs1Accu() + "\t" + reader.getOptiAccu() + "\t" + reader.getPeakWidth() + "\t"
				+ reader.getRtWindow() + "\t" + reader.getScanWindow());
/*
		File[] files = (new File("Z:\\Kai\\Raw_files\\single_strain_dia\\29098\\hap\\separate_search")).listFiles();
		for (int i = 0; i < files.length; i++) {
			File logFile = new File(files[i], files[i].getName() + ".log.txt");

			DiaNNLogReader readeri = new DiaNNLogReader(logFile);
			System.out.println(logFile.getName()+"\t"+readeri.getMs1Accu() + "\t" + readeri.getOptiAccu() + "\t" + readeri.getPeakWidth()
					+ "\t" + readeri.getRtWindow() + "\t" + readeri.getScanWindow());
		}
*/		
		
	}

}
