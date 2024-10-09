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
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

/**
 * @author Kai Cheng
 *
 */
public class MgfReader {

	private static final Pattern D_PATTERN = Pattern.compile("(\\d+)");

	// TITLE=20110203_HZ_HB_888TC_AD_01, Cmpd 9, +MSn(405.07), 10.09 min
	private static final Pattern title1 = Pattern.compile(".+Cmpd (\\d+),.+");

	// TITLE=Kerwin_20150114_ARPE19_phosphoproteomics_15min_rep#1.38.38.3
	// File:"Kerwin_20150114_ARPE19_phosphoproteomics_15min_rep#1.raw",
	// NativeID:"controllerType=0 controllerNumber=1 scan=38"
	private static final Pattern title2 = Pattern.compile(".*scan=(\\d+).*");

	// TITLE=wash_long_column_for_test1.5.5.2
	private static final Pattern title3 = Pattern.compile(".*\\.(\\d+)\\.(\\d+)\\.(\\d+)?(.*)?");

	// TITLE=wash_long_column_for_test1.5.5.2.0.dta
	private static final Pattern title4 = Pattern.compile(".*\\.(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\.dta");
	
	// TITLE=2558_10A0_5-17-2022.2.2.1 File:"2558_10A0_5-17-2022.d", NativeID:"merged=1 frame=2 scanStart=439 scanEnd=463", IonMobility:"1.1411916731289999"
	private static final Pattern title5 = Pattern.compile(".*\\.(\\d+)\\.(\\d+)\\.(\\d+)");

	private static final Pattern[] patterns = new Pattern[] { title1, title2, title3, title4, title5 };

	private BufferedReader reader;
	private File file;

	private String baseName;

	private static Logger LOGGER = LogManager.getLogger();
	
	private DecimalFormat format;
	
	public MgfReader(String input) {
		this(new File(input));
	}

	public MgfReader(File input) {
		this.file = input;
		try {
			this.reader = new BufferedReader(new FileReader(input));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading mgf file " + input, e);
		}
	}

	public String getFileName() {
		return this.baseName;
	}

	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading mgf file " + file, e);
		}
	}

	public File getFile() {
		return file;
	}

	public Spectrum getNextSpectrum() {

		String line;
		Spectrum spectrum = null;
		ArrayList<Peak> peaklist = new ArrayList<Peak>();
		try {
			while ((line = reader.readLine()) != null) {

				if (line.trim().length() == 0)
					continue;

				if (line.startsWith("#"))
					continue;

				if (line.equalsIgnoreCase("BEGIN IONS")) {
					spectrum = new Spectrum();
					continue;
				}

				if (line.equalsIgnoreCase("END IONS")) {
					Peak[] peaks = peaklist.toArray(new Peak[peaklist.size()]);

					Arrays.sort(peaks, new Peak.IntensityComparator());
					for (int i = 0; i < peaks.length; i++) {
						peaks[i].setRankScore((double) (i + 1) / (double) peaks.length);
					}
					Arrays.sort(peaks);

					spectrum.setPeaks(peaks);
					return spectrum;
				}

				int eid = line.indexOf("=");
				if (eid > 0) {
					String key = line.substring(0, eid);
					String value = line.substring(eid + 1);
					if (key.equalsIgnoreCase("PEPMASS")) {
						String[] cs = value.split("\\s");

						if (format == null) {
							format = new DecimalFormat();
							format.applyPattern("#,##0.00");
							if (cs[0].contains(",")) {
								format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.GERMANY));
							} else {
								format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
							}
						}

						double mz = format.parse(cs[0]).doubleValue();

						spectrum.setPrecursorMz(mz);

						if (cs.length > 1) {
							double intensity = format.parse(cs[1]).doubleValue();
							spectrum.setPrecursorIntensity(intensity);
						}
					} else if (key.equalsIgnoreCase("CHARGE")) {
						int charge = Integer.parseInt(value.substring(0, value.length() - 1));
						spectrum.setPrecursorCharge(charge);
					} else if (key.equalsIgnoreCase("RTINSECONDS")) {
						if (format == null) {
							format = new DecimalFormat();
							if (value.contains(",")) {
								format.applyPattern("#,##0.00");
								format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.GERMANY));
							} else {
								format.applyPattern("#,##0.00");
								format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
							}
						}
						double rt = format.parse(value).doubleValue();
						spectrum.setRt(rt / 60.0);
					} else if (key.equalsIgnoreCase("SCANS")) {
						Matcher matcher = D_PATTERN.matcher(value);
						if (matcher.matches()) {
							int scan = Integer.parseInt(value);
							spectrum.setScannum(scan);
						}
					} else if (key.equalsIgnoreCase("TITLE")) {

						for (Pattern pp : patterns) {
							Matcher matcher = pp.matcher(value);
							if (matcher.matches()) {
								int scan = Integer.parseInt(matcher.group(1));
								spectrum.setScannum(scan);

								break;
							}
						}

						if (spectrum.getScannum() <= 0) {
							spectrum.setTitle(value);

							System.out.println(line);
							break;
						}
					}
				} else {
					String[] cs = line.split("\\s");
					double mz = format.parse(cs[0]).doubleValue();
					double intensity = format.parse(cs[1]).doubleValue();
					Peak peak = new Peak(mz, intensity);
					peaklist.add(peak);
				}
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading mgf file " + file, e);
		}

		return null;
	}

	public void exportRt(String output) throws IOException {

		PrintWriter rtWriter = new PrintWriter(output);
		rtWriter.println("FileName\tScannum\tRt");
		String fileName = this.file.getName().substring(0, this.file.getName().lastIndexOf("."));
		Spectrum sp = null;
		while ((sp = this.getNextSpectrum()) != null) {
			int scannum = sp.getScannum();
			double rtsecond = sp.getRt();
			rtWriter.println(fileName + "\t" + scannum + "\t" + rtsecond);
		}
		reader.close();
		rtWriter.close();
	}

}
