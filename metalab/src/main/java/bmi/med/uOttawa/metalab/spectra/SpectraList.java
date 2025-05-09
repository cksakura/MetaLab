/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.quant.Feature;
import bmi.med.uOttawa.metalab.quant.Features;
import bmi.med.uOttawa.metalab.spectra.io.PFindMS1Reader;

/**
 * @author Kai Cheng
 *
 */
public class SpectraList {

	private HashMap<Integer, Spectrum> specMap;
	private Integer[] scans;

	public SpectraList() {
		this.specMap = new HashMap<Integer, Spectrum>();
	}

	public void addSpectrum(Spectrum spectrum) {
		this.specMap.put(spectrum.getScannum(), spectrum);
	}

	public void setScans(Integer[] scans) {
		this.scans = scans;
	}

	public void complete() {
		this.scans = specMap.keySet().toArray(new Integer[specMap.size()]);
		if (scans.length > 0) {
			Arrays.sort(scans);
		}
	}

	public boolean isComplete() {
		if (this.scans == null)
			return false;
		return this.scans.length == this.specMap.size();
	}

	public static SpectraList parseSpectraListFromMS1(File file) throws IOException {

		SpectraList splist = new SpectraList();

		PFindMS1Reader reader = new PFindMS1Reader(file);
		Spectrum spectrum = null;
		while ((spectrum = reader.getNextSpectrum()) != null) {
			splist.addSpectrum(spectrum);
		}
		reader.close();

		Integer[] scans = splist.specMap.keySet().toArray(new Integer[splist.specMap.size()]);
		Arrays.sort(scans);
		splist.setScans(scans);

		return splist;
	}

	public Integer[] getScanList() {
		return this.scans;
	}

	public Spectrum getScan(int scanNum) {
		// TODO Auto-generated method stub
		if (this.specMap.containsKey(scanNum))
			return this.specMap.get(scanNum);
		else
			return null;
	}

	public Spectrum getNextScan(int currentScanNum) {
		// TODO Auto-generated method stub
		int currentId = Arrays.binarySearch(scans, currentScanNum);
		if (currentId < 0)
			currentId = -currentId - 1;
		int nexId = currentId + 1;
		if (nexId < scans.length)
			return this.specMap.get(scans[nexId]);
		return null;
	}

	public Spectrum getPreviousScan(int currentScanNum) {
		// TODO Auto-generated method stub
		int currentId = Arrays.binarySearch(scans, currentScanNum);
		if (currentId < 0)
			currentId = -currentId - 1;
		int preId = currentId - 1;
		if (preId >= 0)
			return this.specMap.get(scans[preId]);
		return null;
	}

	public Features getFeaturesRecalCharge(double monoMz, int scanNum, double ppm) {
		Features finalFeatures = new Features(monoMz, 0, new double[] {});
		for (int charge = 2; charge <= 5; charge++) {
			Features features = getFeatures(charge, monoMz, scanNum, ppm);
			if (features.getLength() > finalFeatures.getLength() * 0.9) {
				finalFeatures = features;
			}
		}
		return finalFeatures;
	}

	public Features getFeatures(int charge, double monoMz, int scanNum, double ppm) {

		Spectrum ms1Scan = null;
		while (true) {
			if ((ms1Scan = this.getScan(scanNum)) != null) {
				break;
			} else {
				scanNum--;
			}
		}

		if (ms1Scan == null) {
			return null;
		}

		double[] idenRtList = new double[] { ms1Scan.getRt() };
		Features feas = new Features(monoMz, charge, idenRtList);

		boolean leftMiss = false;
		int leftMissNum = 0;
		boolean rightMiss = false;
		int rightMissNum = 0;

		Feature fea = getFeature(charge, monoMz, ms1Scan, ppm);
//System.out.println(Arrays.toString(fea.getMasses()));
		if (!fea.isMiss())
			feas.addFeature(fea);

		Spectrum prev;
		int prevscan = scanNum;
		while ((prev = getPreviousScan(prevscan)) != null) {
			prevscan = prev.getScannum();
			Feature prevfea = getFeature(charge, monoMz, prev, ppm);
			if (prevfea.isMiss()) {
				if (leftMiss) {
					leftMissNum++;
					if (leftMissNum == 3) {
						break;
					}
				} else {
					leftMiss = true;
				}
			} else {
				feas.addFeature(prevfea);
				leftMiss = false;
			}
		}

		Spectrum next;
		int nextscan = scanNum;

		while ((next = getNextScan(nextscan)) != null) {

			nextscan = next.getScannum();
			Feature nextfea = getFeature(charge, monoMz, next, ppm);

			if (nextfea.isMiss()) {
				if (rightMiss) {
					rightMissNum++;
					if (rightMissNum == 3) {
						break;
					}
				} else {
					rightMiss = true;
				}
			} else {
				feas.addFeature(nextfea);
				rightMiss = false;
			}
		}

		scanNum = nextscan;
		feas.setInfo();
		return feas;
	}

	public Features getFeatures(int charge, double monoMz, Integer[] scans, double[] scores, double ppm) {

		int scanNum = scans[0];
		Spectrum ms1Scan = null;
		while (true) {
			if ((ms1Scan = this.getScan(scanNum)) != null) {
				break;
			} else {
				scanNum--;
			}
		}
		if (ms1Scan == null) {
			return null;
		}
		double[] idenRtList = new double[scans.length];
		idenRtList[0] = ms1Scan.getRt();

		if (scans.length > 1) {

			Spectrum ms1Scan00 = ms1Scan;
			int idenRtListId = 1;
			int scanNum00 = scanNum;
			double lastRt = idenRtList[0];

			while ((ms1Scan00 = this.getNextScan(scanNum00)) != null) {
				if (ms1Scan00.getScannum() > scans[idenRtListId]) {
					idenRtList[idenRtListId] = lastRt;
					idenRtListId++;
				}
				if (idenRtListId == idenRtList.length) {
					break;
				}
				lastRt = ms1Scan00.getRt();
				scanNum00++;
			}

			Integer[] subscans = this.validateScans(scans, scores, idenRtList);

			if (subscans.length < scans.length) {
				int begid = -1;
				for (int i = 0; i < scans.length; i++) {
					if (subscans[0] == scans[i]) {
						begid = i;
						break;
					}
				}

				double[] subRtList = new double[subscans.length];
				System.arraycopy(idenRtList, begid, subRtList, 0, subRtList.length);

				scans = subscans;
				idenRtList = subRtList;
				scanNum = scans[0];
				while (true) {
					if ((ms1Scan = this.getScan(scanNum)) != null) {
						break;
					} else {
						scanNum--;
					}
				}
			}
		}

		// if(idenRtList[idenRtList.length-1]-idenRtList[0]>3) {
		// return feas;
		// }
		// System.out.println(scans.length);
		// System.out.println("quanfeaturegetter181\tscores\t"+Arrays.toString(scores));
		// System.out.println("quanfeaturegetter181\tscans\t"+Arrays.toString(scans));
		// System.out.println("quanfeaturegetter181\trts\t"+Arrays.toString(idenRtList));
		// System.out.println(Arrays.toString(monoMasses));

		Features feas = new Features(monoMz, charge, idenRtList);

		boolean leftMiss = false;
		int leftMissNum = 0;
		boolean rightMiss = false;
		int rightMissNum = 0;

		Feature fea = getFeature(charge, monoMz, ms1Scan, ppm);

		if (!fea.isMiss())
			feas.addFeature(fea);
		// System.out.println("221\t"+ms1Scan.getScanNum()+"\t"+feas.getFeaMap().size());
		Spectrum prev;
		int prevscan = scanNum;
		while ((prev = getPreviousScan(prevscan)) != null) {
			prevscan = prev.getScannum();
			Feature prevfea = getFeature(charge, monoMz, prev, ppm);

			if (prevfea.isMiss()) {
				if (leftMiss) {
					leftMissNum++;
					if (leftMissNum == 3) {
						break;
					}
				} else {
					leftMiss = true;
				}
			} else {
				feas.addFeature(prevfea);
				leftMiss = false;
			}

			// if(idenRtList[0]-prev.getRTMinute()>rtHalfWidth) break;
		}
		// System.out.println("233\t"+feas.getFeaMap().size());
		for (int sn = 0; sn < scans.length; sn++) {

			if (scanNum > scans[sn])
				continue;

			if (sn == 0) {

				Spectrum next;
				int nextscan = scanNum;

				while ((next = getNextScan(nextscan)) != null) {

					nextscan = next.getScannum();
					Feature nextfea = getFeature(charge, monoMz, next, ppm);

					if (nextfea.isMiss()) {
						if (rightMiss) {
							rightMissNum++;
							if (rightMissNum == 3) {
								break;
							}
						} else {
							rightMiss = true;
						}
					} else {
						feas.addFeature(nextfea);
						rightMiss = false;
					}

					// if(next.getRTMinute()-idenRtList[idenRtList.length-1]>rtHalfWidth)
					// break L;
				}

				scanNum = nextscan;

			} else {

				Spectrum nexms1Scan = null;
				int reNexscan = scans[sn];
				while (true) {
					if ((nexms1Scan = getScan(reNexscan)) != null) {
						break;
					} else {
						reNexscan--;
					}
				}

				Feature reNexfea = this.getFeature(charge, monoMz, nexms1Scan, ppm);

				if (!reNexfea.isMiss())
					feas.addFeature(reNexfea);

				Spectrum next;
				int nextscan = reNexscan;

				while ((next = getNextScan(nextscan)) != null) {
					nextscan = next.getScannum();

					Feature nextfea = this.getFeature(charge, monoMz, next, ppm);

					if (nextfea.isMiss()) {
						if (rightMiss) {
							rightMissNum++;
							if (rightMissNum == 3) {
								break;
							}
						} else {
							rightMiss = true;
						}
					} else {
						feas.addFeature(nextfea);
						rightMiss = false;
					}

					// if(next.getRTMinute()-idenRtList[idenRtList.length-1]>rtHalfWidth)
					// break L;
				}

				scanNum = nextscan;
			}
		}
		feas.setInfo();
		return feas;
	}

	public Features getFeatures(int charge, double monoMz, Integer[] scans, double ppm) {

		int scanNum = scans[0];
		Spectrum ms1Scan = null;
		while (true) {
			if ((ms1Scan = this.getScan(scanNum)) != null) {
				break;
			} else {
				scanNum--;
			}
		}
		if (ms1Scan == null) {
			return null;
		}
		double[] idenRtList = new double[scans.length];
		idenRtList[0] = ms1Scan.getRt();

		if (scans.length > 1) {

			Spectrum ms1Scan00 = ms1Scan;
			int idenRtListId = 1;
			int scanNum00 = scanNum;
			double lastRt = idenRtList[0];

			while ((ms1Scan00 = this.getNextScan(scanNum00)) != null) {
				if (ms1Scan00.getScannum() > scans[idenRtListId]) {
					idenRtList[idenRtListId] = lastRt;
					idenRtListId++;
				}
				if (idenRtListId == idenRtList.length) {
					break;
				}
				lastRt = ms1Scan00.getRt();
				scanNum00++;
			}
		}

		// if(idenRtList[idenRtList.length-1]-idenRtList[0]>3) {
		// return feas;
		// }
		// System.out.println(scans.length);
		// System.out.println("quanfeaturegetter181\tscores\t"+Arrays.toString(scores));
		// System.out.println("quanfeaturegetter181\tscans\t"+Arrays.toString(scans));
		// System.out.println("quanfeaturegetter181\trts\t"+Arrays.toString(idenRtList));
		// System.out.println(Arrays.toString(monoMasses));

		Features feas = new Features(monoMz, charge, idenRtList);

		boolean leftMiss = false;
		int leftMissNum = 0;
		boolean rightMiss = false;
		int rightMissNum = 0;

		Feature fea = getFeature(charge, monoMz, ms1Scan, ppm);

		if (!fea.isMiss())
			feas.addFeature(fea);
		// System.out.println("221\t"+ms1Scan.getScanNum()+"\t"+feas.getFeaMap().size());
		Spectrum prev;
		int prevscan = scanNum;
		while ((prev = getPreviousScan(prevscan)) != null) {
			prevscan = prev.getScannum();
			Feature prevfea = getFeature(charge, monoMz, prev, ppm);

			if (prevfea.isMiss()) {
				if (leftMiss) {
					leftMissNum++;
					if (leftMissNum == 3) {
						break;
					}
				} else {
					leftMiss = true;
				}
			} else {
				feas.addFeature(prevfea);
				leftMiss = false;
			}

			// if(idenRtList[0]-prev.getRTMinute()>rtHalfWidth) break;
		}
		// System.out.println("233\t"+feas.getFeaMap().size());
		for (int sn = 0; sn < scans.length; sn++) {

			if (scanNum > scans[sn])
				continue;

			if (sn == 0) {

				Spectrum next;
				int nextscan = scanNum;

				while ((next = getNextScan(nextscan)) != null) {

					nextscan = next.getScannum();
					Feature nextfea = getFeature(charge, monoMz, next, ppm);

					if (nextfea.isMiss()) {
						if (rightMiss) {
							rightMissNum++;
							if (rightMissNum == 3) {
								break;
							}
						} else {
							rightMiss = true;
						}
					} else {
						feas.addFeature(nextfea);
						rightMiss = false;
					}

					// if(next.getRTMinute()-idenRtList[idenRtList.length-1]>rtHalfWidth)
					// break L;
				}

				scanNum = nextscan;

			} else {

				Spectrum nexms1Scan = null;
				int reNexscan = scans[sn];
				while (true) {
					if ((nexms1Scan = getScan(reNexscan)) != null) {
						break;
					} else {
						reNexscan--;
					}
				}

				Feature reNexfea = this.getFeature(charge, monoMz, nexms1Scan, ppm);

				if (!reNexfea.isMiss())
					feas.addFeature(reNexfea);

				Spectrum next;
				int nextscan = reNexscan;

				while ((next = getNextScan(nextscan)) != null) {
					nextscan = next.getScannum();

					Feature nextfea = this.getFeature(charge, monoMz, next, ppm);

					if (nextfea.isMiss()) {
						if (rightMiss) {
							rightMissNum++;
							if (rightMissNum == 3) {
								break;
							}
						} else {
							rightMiss = true;
						}
					} else {
						feas.addFeature(nextfea);
						rightMiss = false;
					}

					// if(next.getRTMinute()-idenRtList[idenRtList.length-1]>rtHalfWidth)
					// break L;
				}

				scanNum = nextscan;
			}
		}
		feas.setInfo();
		return feas;
	}

	public Feature getFeature(int charge, double monoMass, Spectrum spectrum, double ppm) {

		Feature fea = new Feature(spectrum.getScannum(), charge, monoMass, spectrum.getRt());

		double tolerance = ppm * monoMass * 1.0E-6;
		tolerance = tolerance < 0.02 ? 0.02 : tolerance;
		Peak[] peaks = spectrum.getPeaks();
		fea.match(peaks, tolerance);

		return fea;
	}

	private Integer[] validateScans(Integer[] scans, double[] scores, double[] rts) {

		if (rts[rts.length - 1] - rts[0] < 3) {

			return scans;

		} else {

			Integer[] newScans = new Integer[scans.length - 1];
			double[] newScores = new double[scores.length - 1];
			double[] newRts = new double[rts.length - 1];

			if (scores[0] > scores[scores.length - 1]) {

				System.arraycopy(scans, 0, newScans, 0, newScans.length);
				System.arraycopy(scores, 0, newScores, 0, newScores.length);
				System.arraycopy(rts, 0, newRts, 0, newRts.length);

			} else {

				System.arraycopy(scans, 1, newScans, 0, newScans.length);
				System.arraycopy(scores, 1, newScores, 0, newScores.length);
				System.arraycopy(rts, 1, newRts, 0, newRts.length);
			}

			scans = newScans;
			scores = newScores;
			rts = newRts;

			return validateScans(scans, scores, rts);
		}
	}

}
