/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.spectra.Peak;

/**
 * @author Kai Cheng
 *
 */
public class NCoreMatcher {

	private static final double Hex = Glycosyl.Hex.getMonoMass();
	private static final double HexNAc = Glycosyl.HexNAc.getMonoMass();
	private static final double dHex = Glycosyl.Fuc.getMonoMass();

//	private static final double[] core1 = new double[] { dHex, HexNAc, HexNAc + Hex, HexNAc + Hex * 2,
//			HexNAc + Hex * 3 };
//	private static final double[] core2 = new double[] { HexNAc + dHex, HexNAc * 2 + dHex, HexNAc * 2 + Hex + dHex,
//			HexNAc * 2 + Hex * 2 + dHex, HexNAc * 2 + Hex * 3 + dHex };

	private static final int[][] ms2Charge = new int[][] { { 1 }, { 1, 2 }, { 1, 2 }, { 2, 3 }, { 2, 3, 4 } };

	/**
	 * nbt.1511-S1, p9
	 */
	private static final double dm = 1.00286864;
	private double ppm;

	public NCoreMatcher(double ppm) {
		this.ppm = ppm;
	}

	public NCoreInfo match(Peak[] peaks, int preCharge) {

		ArrayList<NCorePeakSegment> segmentList = new ArrayList<NCorePeakSegment>();
		double[] intensityList = new double[peaks.length];

		for (int i = 0; i < peaks.length; i++) {
			this.addNCorePeak(peaks[i], segmentList, preCharge);
			intensityList[i] = peaks[i].getIntensity();
		}

		NCorePeakSegment[] segments = segmentList.toArray(new NCorePeakSegment[segmentList.size()]);
		Arrays.parallelSort(segments);

		NCoreInfo info = this.match(segments, intensityList);

		return info;
	}

	private NCoreInfo match(NCorePeakSegment[] pslist, double[] intensityList) {

		NCoreOverlapSegment overlapSeg = new NCoreOverlapSegment();
		overlapSeg.add(pslist[0]);

		ArrayList<NCoreOverlapSegment> overlist = new ArrayList<NCoreOverlapSegment>();

		for (int i = 1; i < pslist.length; i++) {

			if (pslist[i].isOverlap(pslist[i - 1])) {

				overlapSeg.add(pslist[i]);

			} else {

				overlist.add(overlapSeg);

				overlapSeg = new NCoreOverlapSegment();
				overlapSeg.add(pslist[i]);

			}
		}

		overlist.add(overlapSeg);

		if (overlist.size() == 0) {
			return null;
		}

		ArrayList<NCoreOverlapSegment> overlist2 = new ArrayList<NCoreOverlapSegment>();
		HashSet<Integer> usedset = new HashSet<Integer>();

		for (int i = 0; i < overlist.size(); i++) {

			NCoreOverlapSegment olsi = overlist.get(i);
			double massi = olsi.getMass();
			double inteni = olsi.getInten();
			if (massi < 350 || inteni == 0) {
				usedset.add(i);
				continue;
			}

			if (usedset.contains(i))
				continue;

			int counti = olsi.getMassList().size();

			int k = 1;

			for (int j = i + 1; j < overlist.size(); j++) {

				if (usedset.contains(j))
					continue;

				NCoreOverlapSegment olsj = overlist.get(j);
				double massj = olsj.getMass();
				double intenj = olsj.getInten();

				if (massj < 1000 && k == 4)
					continue;

				if (massj >= 1000 && k == 5)
					continue;

				if (intenj == 0) {
					usedset.add(j);
					continue;
				}

				if (counti == 1 && olsj.getMassList().size() > 2 && k == 1) {
					continue;
				}

				double deltamass = massj - massi;
				double deltaIntensity = Math.abs(Math.log10(olsi.getCurrentIntensity() / olsj.getCurrentIntensity()));

				if (Math.abs(deltaIntensity) > 0.8)
					continue;

				if (deltamass > k + 1)
					break;

				if (Math.abs(deltamass - dm * k) <= massj * ppm * 1E-6) {

					usedset.add(j);

					if (!usedset.contains(i)) {

						olsi.combineIsotope(olsj, k);
					}
					k++;
				}
			}
			olsi.validate();
		}

		for (int i = 0; i < overlist.size(); i++) {
			if (overlist.get(i).isUse() && !usedset.contains(i)) {
				overlist2.add(overlist.get(i));
			}
		}

		NCoreOverlapSegment[] olslist = overlist2.toArray(new NCoreOverlapSegment[overlist2.size()]);

		if (olslist.length == 0)
			return null;

		Arrays.parallelSort(olslist);

		double[] pepMasses;
		Integer[][] chargeList;
		NCoreOverlapSegment olsMax = olslist[olslist.length - 1];
		if (olsMax.isFull()) {
			ArrayList<NCorePeakSegment> templisti = olsMax.getPeakSegList();
			for (int j = 0; j < templisti.size(); j++) {
				if (templisti.get(j).getType() == 0
						&& templisti.get(j).getPeakInten() == intensityList[intensityList.length - 1]) {
					pepMasses = new double[] { olsMax.getMass() };
					chargeList = new Integer[][] { olsMax.getChargeList() };

					NCoreInfo info = new NCoreInfo(pepMasses, chargeList);
					return info;
				}
			}
		}

		ArrayList<Double> templist = new ArrayList<Double>();
		ArrayList<Integer[]> tempChargelist = new ArrayList<Integer[]>();
		double intenthres1 = 0;
		double intenthres2 = 0;
		double massthres = 0;

		for (int i = olslist.length - 1; i >= 0; i--) {
			if (massthres == 0) {
				massthres = olslist[i].getMass();
			}
			if (intenthres1 == 0 && olslist[i].isFull()) {
				intenthres1 = olslist[i].getInten() / 5.0;
			}
			if (intenthres2 == 0) {
				intenthres2 = olslist[i].getInten() / 8.0;
			}
			if (olslist[i].getMass() > massthres) {
				if (olslist[i].getInten() > intenthres1 && olslist[i].getInten() > intenthres2) {
					templist.add(olslist[i].getMass());
					tempChargelist.add(olslist[i].getChargeList());
				}
			} else {
				if (olslist[i].getInten() > intenthres2) {
					templist.add(olslist[i].getMass());
					tempChargelist.add(olslist[i].getChargeList());
				}
			}
		}

		pepMasses = new double[templist.size()];
		chargeList = new Integer[templist.size()][];
		for (int i = 0; i < pepMasses.length; i++) {
			pepMasses[i] = templist.get(i);
			chargeList[i] = tempChargelist.get(i);
		}

		NCoreInfo info = new NCoreInfo(pepMasses, chargeList);
		return info;
	}

	private void addNCorePeak(Peak peak, ArrayList<NCorePeakSegment> segmentList, int preCharge) {

		int[] charge;
		if (preCharge < 6) {
			charge = ms2Charge[preCharge - 1];
		} else {
			charge = new int[] { (preCharge - 3), (preCharge - 2), (preCharge - 1) };
		}

		double ppm = this.ppm / 2.0;
		for (int i = 0; i < charge.length; i++) {

			double mz = peak.getMz();
			double inten = peak.getIntensity();

			double mz0 = mz - HexNAc / charge[i];
			NCorePeakSegment ps0 = new NCorePeakSegment(mz0, inten, charge[i], ppm, mz, 0);
			segmentList.add(ps0);

			double mz1 = mz0 - HexNAc / charge[i];
			NCorePeakSegment ps1 = new NCorePeakSegment(mz1, inten, charge[i], ppm, mz, 1);
			segmentList.add(ps1);

			double mz2 = mz1 - Hex / charge[i];
			NCorePeakSegment ps2 = new NCorePeakSegment(mz2, inten, charge[i], ppm, mz, 2);
			segmentList.add(ps2);

			double mz3 = mz2 - Hex / charge[i];
			NCorePeakSegment ps3 = new NCorePeakSegment(mz3, inten, charge[i], ppm, mz, 3);
			segmentList.add(ps3);

			double mz4 = mz3 - Hex / charge[i];
			NCorePeakSegment ps4 = new NCorePeakSegment(mz4, inten, charge[i], ppm, mz, 4);
			segmentList.add(ps4);

			double mz01 = mz - (HexNAc + dHex) / charge[i];
			NCorePeakSegment ps01 = new NCorePeakSegment(mz01, inten, charge[i], ppm, mz, 5);
			segmentList.add(ps01);

			double mz11 = mz01 - HexNAc / charge[i];
			NCorePeakSegment ps11 = new NCorePeakSegment(mz11, inten, charge[i], ppm, mz, 6);
			segmentList.add(ps11);

			double mz21 = mz11 - Hex / charge[i];
			NCorePeakSegment ps21 = new NCorePeakSegment(mz21, inten, charge[i], ppm, mz, 7);
			segmentList.add(ps21);

			double mz31 = mz21 - Hex / charge[i];
			NCorePeakSegment ps31 = new NCorePeakSegment(mz31, inten, charge[i], ppm, mz, 8);
			segmentList.add(ps31);

			double mz41 = mz31 - Hex / charge[i];
			NCorePeakSegment ps41 = new NCorePeakSegment(mz41, inten, charge[i], ppm, mz, 9);
			segmentList.add(ps41);

		}
	}

	public class NCoreInfo {

		private double[] pepMasses;
		private Integer[][] chargeList;

		public NCoreInfo(double[] pepMasses, Integer[][] chargeList) {
			this.pepMasses = pepMasses;
			this.chargeList = chargeList;
		}

		public double[] getPepMasses() {
			return pepMasses;
		}

		public Integer[][] getChargeList() {
			return chargeList;
		}

	}

}
