package bmi.med.uOttawa.metalab.spectra.predict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PredictSpectraReader {

	private HashMap<String, PredictSpectra> spmap;

	public PredictSpectraReader(String file) {
		this(new File(file));
	}

	public PredictSpectraReader(File file) {
		try {
			this.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void read(File file) throws IOException {
		this.spmap = new HashMap<String, PredictSpectra>();

		int charge = 0;
		PredictSpectra ps = null;
		boolean start = false;

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("CHARGE=")) {
				int loc = line.indexOf("=");
				charge = Integer.parseInt(line.substring(loc + 1));
			} else if (line.startsWith("pepinfo=")) {
				int loc = line.indexOf("=");
				String pepinfo = line.substring(loc);
				ps = new PredictSpectra(pepinfo, charge);
				start = true;
			} else if (line.startsWith("END IONS")) {
				if (ps != null) {
					this.spmap.put(ps.getPepinfo(), ps);
				}
				start = false;
			} else {
				if (start) {
					String[] cs = line.split("[\\s]");
					if (cs.length == 3 && ps != null) {
						ps.addIon(cs[2], Double.parseDouble(cs[0]), Double.parseDouble(cs[1]));
					}
				}
			}
		}
		reader.close();

		System.out.println(spmap.size());
	}

	public HashMap<String, PredictSpectra> getSpmap() {
		return spmap;
	}

}
