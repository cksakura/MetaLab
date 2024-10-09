/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Kai Cheng
 *
 */
public class RegionTopNIntensityFilter {

	/**
	 * Within this tolerance, the peaks will be considered as the isotope mass and
	 * will not be selected twice.
	 */
	private static final double ISOTOPE_TOL = 1.2;

	private int topn = 0;
	private double window = 100;
	private double intensityThres;

	/**
	 * Within a specific m/z window, how many peaks are retained
	 * 
	 * @param topn
	 * @param mzRegion
	 */
	public RegionTopNIntensityFilter(int topn, double mzRegion) {
		this.topn = topn;
		this.window = mzRegion;

		if (this.topn <= 0) {
			throw new IllegalArgumentException("The retained n top peaks must bigger than 0");
		}

		if (this.window <= this.topn) {
			throw new IllegalArgumentException("The selected top peaks must less than the total mz region.");
		}
	}

	public RegionTopNIntensityFilter(int topn, double mzRegion, double intensityThres) {
		this(topn, mzRegion);
		this.intensityThres = intensityThres;
	}

	/**
	 * The probability for a single peak match in the prediction that they are
	 * randomly matches. Equals topn/100.
	 * 
	 * @return topn/100;
	 */
	public double singleP() {
		return (double) topn / this.window;
	}

	/**
	 * @return the topn peaks to be retained per 100 amu window.
	 */
	public int getTopn() {
		return this.topn;
	}

	public Peak[] filter(Peak[] peaks) {
		if (peaks.length == 0)
			return peaks;

		double intensity = 0;
		double threshold = 0;
		for (int i = 0; i < peaks.length; i++) {
			if (peaks[i].getIntensity() > intensity) {
				intensity = peaks[i].getIntensity();
			}
		}
		threshold = intensity * intensityThres;

		ArrayList<Peak> peaklist = new ArrayList<Peak>();
		int beginId = 0;
		double beginMz = peaks[0].getMz();
		while (true) {

			if (beginId == peaks.length)
				break;

			double endMz = beginMz + window;

			Peak endPeak = new Peak(endMz, 0);
			int endId = Arrays.binarySearch(peaks, endPeak);
			if (endId < 0)
				endId = -endId - 1;

			Peak[] rangePeak = new Peak[endId - beginId];
//System.out.println(beginMz+"\t"+endMz+"\t"+rangePeak.length);
			if (rangePeak.length == 0) {
				beginId = endId;
				beginMz = endMz;
				continue;
			}

			System.arraycopy(peaks, beginId, rangePeak, 0, rangePeak.length);
			Arrays.sort(rangePeak, new Peak.RevIntensityComparator());
			if (rangePeak[0].getIntensity() < threshold) {
				beginId = endId;
				beginMz = endMz;
				continue;
			}

			if (endId - beginId <= topn) {
				for (int i = beginId; i < endId; i++) {
					peaklist.add(peaks[i]);
				}
			} else {
				int deep = 0;
				Peak[] topIsotopes = new Peak[topn];
				L: for (int i = 0; i < rangePeak.length; i++) {

					for (int j = 0; j < deep; j++) {
						if (Math.abs(rangePeak[i].getMz() - topIsotopes[j].getMz()) < ISOTOPE_TOL) {
							peaklist.add(rangePeak[i]);
							continue L;
						}
					}
					if (deep < topn) {
						peaklist.add(rangePeak[i]);
						topIsotopes[deep++] = rangePeak[i];
					}

					/*
					 * for (int j = 0; j < i; j++) { if (Math.abs(rangePeak[i].getMz() -
					 * rangePeak[j].getMz()) < ISOTOPE_TOL) { continue L; } } deep++; if (deep >
					 * topn) { for (int j = 0; j < i; j++) { peaklist.add(rangePeak[j]); } break; }
					 */
				}
				/*
				 * if (deep <= topn) { for (int i = beginId; i < endId; i++) {
				 * peaklist.add(peaks[i]); } }
				 */
			}
			beginId = endId;
			beginMz = endMz;
		}

		Peak[] filteredPeaks = peaklist.toArray(new Peak[peaklist.size()]);
		Arrays.parallelSort(filteredPeaks);

		return filteredPeaks;
	}

	/**
	 * assign rankscore and localrankscore
	 * 
	 * @see bmi.med.uOttawa.spectra.Peak
	 * @param peaks
	 * 
	 */
	public void parseRank(Peak[] peaks) {

		Arrays.sort(peaks, new Peak.RevIntensityComparator());
		for (int i = 0; i < peaks.length; i++) {
			peaks[i].setRankScore(1.0 - (double) i / (double) peaks.length);
		}

		Arrays.sort(peaks);

		int initialCount = 0;
		for (int i = 1; i < peaks.length; i++) {
			if (peaks[i].getMz() - peaks[0].getMz() > 50) {
				initialCount = i;
				break;
			}
		}

		int localBegin = 0;
		int localEnd = initialCount - 1;
		int[] tempRank = new int[initialCount];

		for (int i = 0; i < tempRank.length; i++) {
			for (int j = i + 1; j < tempRank.length; j++) {
				if (peaks[i].getIntensity() >= peaks[j].getIntensity()) {
					tempRank[j]++;
				} else {
					tempRank[i]++;
				}
			}
		}

		for (int i = 0; i < peaks.length; i++) {
			int newLocalBegin = localBegin;
			int newLocalEnd = localEnd;
			for (int j = localBegin; j < peaks.length; j++) {
				if (peaks[i].getMz() - peaks[j].getMz() > 50) {
					newLocalBegin++;
				} else {
					break;
				}
			}
			for (int j = localEnd + 1; j < peaks.length; j++) {
				if (peaks[j].getMz() - peaks[i].getMz() > 50) {
					break;
				} else {
					newLocalEnd++;
				}
			}

			if (newLocalBegin == localBegin && newLocalEnd == localEnd) {
				peaks[i].setLocalRankScore(1.0 - (double) tempRank[i - localBegin] / (double) tempRank.length);
				continue;
			}

			int[] newTempRank = new int[newLocalEnd - newLocalBegin + 1];
			int removeCount = newLocalBegin - localBegin;
			int addCount = newLocalEnd - localEnd;
			System.arraycopy(tempRank, removeCount, newTempRank, 0, tempRank.length - removeCount);

			if (removeCount > 0) {
				for (int j = 0; j < tempRank.length - removeCount; j++) {
					int tempi = newTempRank[j];
					for (int k = 0; k < removeCount; k++) {
						if (tempi > tempRank[k]) {
							newTempRank[j]--;
						}
					}
				}
			}

			if (addCount > 0) {
				for (int j = localEnd + 1; j <= newLocalEnd; j++) {
					for (int k = newLocalBegin; k < j; k++) {
						if (peaks[j].getIntensity() >= peaks[k].getIntensity()) {
							newTempRank[k - newLocalBegin]++;
						} else {
							newTempRank[j - newLocalBegin]++;
						}
					}
				}
			}

			tempRank = newTempRank;
			localBegin = newLocalBegin;
			localEnd = newLocalEnd;
			peaks[i].setLocalRankScore(1.0 - (double) tempRank[i - localBegin] / (double) tempRank.length);
		}
	}

}
