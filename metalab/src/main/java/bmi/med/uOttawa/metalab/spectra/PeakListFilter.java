/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import java.util.Arrays;

/**
 * @author Kai Cheng
 *
 */
public class PeakListFilter {

	private static double isotopeTolerance = 1.2;

	public static Peak[] TopNFilter(Peak[] peaks, double range, int topn) {

		int begin = 0;
		double beginMz = peaks[0].getMz();
		Peak[] filteredPeaks = new Peak[0];
		Peak.RevIntensityComparator comparator = new Peak.RevIntensityComparator();

		for (int i = begin; i < peaks.length; i++) {
			double mz = peaks[i].getMz();
			if (mz - beginMz > range) {
				int rangeLength = i - begin;
				if (rangeLength > topn) {
					Peak[] rangePeaks = new Peak[rangeLength];
					System.arraycopy(peaks, begin, rangePeaks, 0, rangeLength);
					Arrays.parallelSort(rangePeaks, comparator);

					int deep = 0;
					L: for (int j = 0; j < rangePeaks.length; j++) {
						for (int k = 0; k < j; k++) {
							if (Math.abs(rangePeaks[k].getMz() - rangePeaks[j].getMz()) < isotopeTolerance) {
								continue L;
							}
						}
						deep++;
						if (deep == topn)
							break;
					}
					Peak[] tempPeaks = new Peak[filteredPeaks.length + deep];
					System.arraycopy(filteredPeaks, 0, tempPeaks, 0, filteredPeaks.length);
					System.arraycopy(rangePeaks, 0, tempPeaks, filteredPeaks.length + 1, deep);
					filteredPeaks = tempPeaks;
				} else {
					Peak[] tempPeaks = new Peak[filteredPeaks.length + rangeLength];
					System.arraycopy(filteredPeaks, 0, tempPeaks, 0, filteredPeaks.length);
					System.arraycopy(peaks, begin, tempPeaks, filteredPeaks.length + 1, rangeLength);
					filteredPeaks = tempPeaks;
				}

				begin = i;
				beginMz = mz;
			}
		}

		int end = peaks.length;
		int rangeLength = end - begin;
		if (rangeLength > topn) {
			Peak[] rangePeaks = new Peak[rangeLength];
			System.arraycopy(peaks, begin, rangePeaks, 0, rangeLength);
			Arrays.parallelSort(rangePeaks, comparator);

			int deep = 0;
			L: for (int j = 0; j < rangePeaks.length; j++) {
				for (int k = 0; k < j; k++) {
					if (Math.abs(rangePeaks[k].getMz() - rangePeaks[j].getMz()) < isotopeTolerance) {
						continue L;
					}
				}
				deep++;
				if (deep == topn)
					break;
			}
			Peak[] tempPeaks = new Peak[filteredPeaks.length + deep];
			System.arraycopy(filteredPeaks, 0, tempPeaks, 0, filteredPeaks.length);
			System.arraycopy(rangePeaks, 0, tempPeaks, filteredPeaks.length + 1, deep);
			filteredPeaks = tempPeaks;
		} else {
			Peak[] tempPeaks = new Peak[filteredPeaks.length + rangeLength];
			System.arraycopy(filteredPeaks, 0, tempPeaks, 0, filteredPeaks.length);
			System.arraycopy(peaks, begin, tempPeaks, filteredPeaks.length + 1, rangeLength);
			filteredPeaks = tempPeaks;
		}

		Arrays.parallelSort(filteredPeaks);
		return filteredPeaks;
	}

}
