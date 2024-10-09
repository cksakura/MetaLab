package bmi.med.uOttawa.metalab.spectra.predict;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;

public class PredictSpectraMatch {

	private static void test(String xml, String mgf, String predict) {
		PredictSpectraReader prereader = new PredictSpectraReader(predict);
		HashMap<String, PredictSpectra> spmap = prereader.getSpmap();

		HashMap<String, Spectrum> ms2map = new HashMap<String, Spectrum>();
		MgfReader mgfreader = new MgfReader(mgf);
		Spectrum sp = null;
		while ((sp = mgfreader.getNextSpectrum()) != null) {
			ms2map.put(sp.getTitle(), sp);
		}
		mgfreader.close();

		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		PredictSpectraReader reader = new PredictSpectraReader(
				"D:\\software\\Bio\\pDeep-master\\pDeep2" + "\\predict.txt");
	}

}
