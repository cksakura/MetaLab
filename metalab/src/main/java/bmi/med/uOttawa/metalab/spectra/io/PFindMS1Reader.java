package bmi.med.uOttawa.metalab.spectra.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

public class PFindMS1Reader {

	private static final String nameRt = "RetTime";

	private BufferedReader reader;
	private Spectrum spectrum;
	private boolean end;

	public PFindMS1Reader(String file) throws FileNotFoundException {
		this(new File(file));
	}

	public PFindMS1Reader(File file) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(file));
	}

	public Spectrum getNextSpectrum() throws IOException {
		if (end) {
			return null;
		}

		Spectrum spectrum = null;
		String line = null;
		ArrayList<Peak> peaklist = new ArrayList<Peak>();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\\s+");
			if (cs[0].equals("H")) {

			} else if (cs[0].equals("S")) {
				int scannum = Integer.parseInt(cs[1]);
				if (this.spectrum != null) {
					spectrum = new Spectrum(this.spectrum.getScannum(), this.spectrum.getRt());
					spectrum.setPeaks(peaklist.toArray(new Peak[peaklist.size()]));

					this.spectrum = new Spectrum(scannum);
					return spectrum;

				} else {
					this.spectrum = new Spectrum(scannum);
				}

			} else if (cs[0].equals("I")) {
				if (cs[1].equals(nameRt)) {
					double rt = Double.parseDouble(cs[2]);
					this.spectrum.setRt(rt);
				}
			} else {
				double mz = Double.parseDouble(cs[0]);
				double intensity = Double.parseDouble(cs[1]);
				peaklist.add(new Peak(mz, intensity));
			}
		}

		end = true;

		spectrum = new Spectrum(this.spectrum.getScannum(), this.spectrum.getRt());
		spectrum.setPeaks(peaklist.toArray(new Peak[peaklist.size()]));

		return spectrum;
	}

	public void close() throws IOException {
		this.reader.close();
	}
}
