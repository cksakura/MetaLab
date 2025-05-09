/**
 * 
 */
package bmi.med.uOttawa.metalab.core.math;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

/**
 * @author Kai Cheng
 *
 */
public class MathTool {

	public final static DecimalFormat df4 = new DecimalFormat("0.0###");
	public final static DecimalFormat df6 = new DecimalFormat("0.0#####");
	public final static DecimalFormat dfPer = new DecimalFormat("#0.##%");

	public static double calculateCliffsDelta(double[] x, double[] y) {
		MannWhitneyUTest test = new MannWhitneyUTest();
		double u = test.mannWhitneyU(x, y);

		double n1 = x.length;
		double n2 = y.length;

		double delta = 1 - (2 * u) / (n1 * n2);

		return delta;
	}

	public static double getStdDev(double[] data) {
		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (data.length - 1))));
		}
	}

	public static double getStdDev(Double[] data) {
		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (data.length - 1))));
		}
	}

	public static double getStdDevNoZero(double[] data) {

		double STDEV = 0;
		double ave = getAveNoZero(data);
		int count = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i] != 0) {
				STDEV += (data[i] - ave) * (data[i] - ave);
				count++;
			}
		}
		if (count == 1 || count == 0) {
			return 0;
		} else {
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (count - 1))));
		}
	}

	public static double getStdDev(ArrayList<Double> data) {
		if (data.size() == 0)
			return 0;

		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.size(); i++) {
			ave += data.get(i);
		}
		ave = ave / data.size();
		for (int j = 0; j < data.size(); j++) {
			STDEV += (data.get(j) - ave) * (data.get(j) - ave);
		}
		if (data.size() == 1) {
			return 0;
		} else {
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (data.size() - 1))));
		}
	}

	public static float getStdDev(float[] data) {
		float ave = 0;
		float STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			return Float.parseFloat(df4.format(Math.sqrt(STDEV / (data.length - 1))));
		}
	}

	public static double getRSD(double[] data) {
		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			if (ave == 0)
				return 0;
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (data.length - 1)) / ave));
		}
	}

	public static double getRSD(ArrayList<Double> data) {
		if (data.size() == 0)
			return 0;

		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.size(); i++) {
			ave += data.get(i);
		}
		ave = ave / data.size();
		for (int j = 0; j < data.size(); j++) {
			STDEV += (data.get(j) - ave) * (data.get(j) - ave);
		}
		if (data.size() == 1) {
			return 0;
		} else {
			if (ave == 0)
				return 0;
			return Double.parseDouble(df4.format(Math.sqrt(STDEV / (data.size() - 1)) / ave));
		}
	}

	public static float getRSD(float[] data) {
		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			if (ave == 0)
				return 0;
			return Float.parseFloat(df4.format(Math.sqrt(STDEV / (data.length - 1)) / ave));
		}
	}

	public static float getRSD(int[] data) {
		double ave = 0;
		double STDEV = 0;
		for (int i = 0; i < data.length; i++) {
			ave += data[i];
		}
		ave = ave / data.length;
		for (int j = 0; j < data.length; j++) {
			STDEV += (data[j] - ave) * (data[j] - ave);
		}
		if (data.length == 1) {
			return 0;
		} else {
			if (ave == 0)
				return 0;
			return Float.parseFloat(df4.format(Math.sqrt(STDEV / (data.length - 1)) / ave));
		}
	}

	public static double[] getVar(ArrayList<Double> data) {
		int length = data.size();
		ArrayList<Double> temp = new ArrayList<Double>();
		double[] dataList = new double[length];
		double total = 0;
		dataList[0] = 0;
		for (int i = 1; i < data.size(); i++) {

			double d1 = data.get(i);
			temp.add(d1);
			double root = 0;
			total += d1;

			double ave = total / (i + 1);
			for (Double d2 : temp) {
				root += (d2 - ave) * (d2 - ave);
			}
			root = Math.sqrt(root / (i));
			if (d1 > ave)
				dataList[i] = root;
			else
				dataList[i] = 0 - root;

//			System.out.println(i+"\t"+dataList[i]);
		}
		return dataList;
	}

	public static double[] getVar(Double[] data) {
		ArrayList<Double> temp = new ArrayList<Double>(data.length);
		for (int i = 0; i < data.length; i++) {
			temp.add(data[i]);
		}
		return getVar(temp);
	}

	public static int getMaxIndex(double[] array) {
		double max = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}

	public static int getMaxIndex(float[] array) {
		float max = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}

	public static int getMaxIndex(int[] array) {
		int max = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
				index = i;
			}
		}
		return index;
	}

	/**
	 * Another method to calculate the standard deviation, the result is same as
	 * getVar().
	 * <p>
	 * ±¾·½·¨°´ÕÕ×ÜÌå¼ÆËã¡£
	 * 
	 * @param data
	 * @return
	 */
	public static double[] getVar2(ArrayList<Double> data) {
		int length = data.size();
		ArrayList<Double> temp = new ArrayList<Double>();
		double[] dataList = new double[length];
		double total = 0;

		for (int i = 0; i < data.size(); i++) {
			double d1 = data.get(i);
			temp.add(d1);
			double root = 0;
			total += d1;

			double ave = total / (i + 1);
			for (Double d2 : temp) {
				root += d2 * d2;
			}

			root = Math.sqrt(root / (i + 1) - ave * ave);
			if (d1 > ave)
				dataList[i] = root;
			else
				dataList[i] = 0 - root;
		}

		return dataList;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */

	public static double getVarRatio(ArrayList<Double> data) {
		double[] rmsList = getVar(data);
		double ratio = 0;
		int n = rmsList.length;
		double total = 0;
		for (double r : rmsList) {
			total += Math.abs(r);
		}
		if (total == 0)
			ratio = 0;
		else
			ratio = rmsList[n - 1] * n / total;
		return ratio;
	}

	public static short[] deNoise(Double[] data, int length, double highThres, double lowThres, double lowThMin) {

		ArrayList<Double> temp = new ArrayList<Double>();
		ArrayList<Double> testData = new ArrayList<Double>();
		short[] dataList = new short[data.length];

		temp.add(data[0]);
		testData.add(data[0]);

		for (int i = 1; i < data.length; i++) {

			temp.add(data[i]);
			double ratio = getVarRatio(temp);

			if (ratio < 2.01 && ratio > lowThMin)
				testData.add(data[i]);
			else
				temp.remove(data[i]);

			if (testData.size() == length)
				break;
		}

		for (int i = 0; i < data.length; i++) {
			double di = data[i];
			testData.add(di);
			double ratio = getVarRatio(testData);
//			System.out.println(i+"~"+ratio+"~"+di+"~"+testData);
			if (ratio > highThres) {
				dataList[i] = 1;
				testData.remove(di);
//				System.out.print(di+"~"+ratio+"~");
//				System.out.println(testData);
			} else if (ratio < lowThres && ratio > lowThMin) {
				dataList[i] = 0;
				testData.remove(0);
//				System.out.print(di+"~"+ratio+"~");
//				System.out.println(testData);
			} else {
				dataList[i] = 0;
				testData.remove(di);
//				System.out.print(di+"~"+ratio+"~");
//				System.out.println(testData);
			}
		}

//		System.out.println(dataList);
		return dataList;
	}

	public static double[] getAveList(ArrayList<Double> dataList) {
		double[] ave = new double[dataList.size()];
		double total = 0;
		for (int i = 0; i < ave.length; i++) {
			total += dataList.get(i);
			ave[i] = Double.parseDouble(df4.format(total / (double) (i + 1)));
		}
		return ave;
	}

	public static double getAve(ArrayList<Double> dataList) {
		if (dataList.size() == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.size(); i++) {
			total += dataList.get(i);
		}
		double ave = Double.parseDouble(df4.format(total / (double) dataList.size()));
		return ave;
	}

	public static double getAve(double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}

		double ave = Double.parseDouble(df4.format(total / (double) dataList.length));
		return ave;
	}

	public static double getAve(Double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}

		double ave = Double.parseDouble(df4.format(total / (double) dataList.length));
		return ave;
	}

	public static double getAveNoZero(double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double total = 0;
		int count = 0;
		for (int i = 0; i < dataList.length; i++) {
			if (dataList[i] != 0) {
				total += dataList[i];
				count++;
			}
		}

		double ave = count == 0 ? 0 : Double.parseDouble(df4.format(total / (double) count));
		return ave;
	}

	public static double getAve(int[] dataList) {
		if (dataList.length == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}

		double ave = Double.parseDouble(df4.format(total / (double) dataList.length));
		return ave;
	}

	public static double getWeightAve(double[] weight, double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i] * weight[i];
		}
		double ave = Double.parseDouble(df4.format(total / getTotal(weight)));
		return ave;
	}

	public static double getWeightAve(ArrayList<Double> weight, ArrayList<Double> dataList) {
		if (dataList.size() == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < dataList.size(); i++) {
			total += dataList.get(i) * weight.get(i);
		}
		double ave = Double.parseDouble(df4.format(total / getTotal(weight)));
		return ave;
	}

	public static double[] getPlusAve(double[] d1, double[] d2) {

		if (d1.length != d2.length)
			return null;

		double[] total = new double[d1.length];
		for (int i = 0; i < d1.length; i++) {
			total[i] += d1[i] + d2[i];
		}

		return total;
	}

	public static double getTotal(ArrayList<Double> dataList) {
		double total = 0;
		for (int i = 0; i < dataList.size(); i++) {
			total += dataList.get(i);
		}
		return total;
	}

	public static double getTopnTotal(ArrayList<Double> dataList, int topn) {

		Double[] list = dataList.toArray(new Double[dataList.size()]);
		return getTopnTotal(list, topn);
	}

	public static double getTotal(double[] dataList) {
		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}
		return total;
	}

	public static double getTotal(Double[] dataList) {
		double total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}
		return total;
	}

	public static double getTopnTotal(double[] dataList, int topn) {

		if (topn > dataList.length)
			return getTotal(dataList);

		Arrays.sort(dataList);
		double total = 0;
		for (int i = dataList.length - 1; i >= dataList.length - topn; i--) {
			total += dataList[i];
		}
		return total;
	}

	public static double getTopnTotal(Double[] dataList, int topn) {

		if (topn > dataList.length)
			return getTotal(dataList);

		Double[] templist = new Double[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		double total = 0;
		for (int i = templist.length - 1; i >= templist.length - topn; i--) {
			total += templist[i];
		}
		return total;
	}

	public static double getTopnAve(ArrayList<Double> dataList, int topn) {
		Double[] list = dataList.toArray(new Double[dataList.size()]);
		return getTopnAve(list, topn);
	}

	public static double getTopnAve(double[] dataList, int topn) {

		if (topn > dataList.length)
			return getAve(dataList);

		if (topn == 0)
			return 0;

		Arrays.sort(dataList);
		double total = 0;
		for (int i = dataList.length - 1; i >= dataList.length - topn; i--) {
			total += dataList[i];
		}
		return Double.parseDouble(df4.format(total / (double) topn));
	}

	public static double getTopnAve(Double[] dataList, int topn) {

		if (topn > dataList.length)
			return getAve(dataList);

		if (topn == 0)
			return 0;

		Double[] templist = new Double[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		double total = 0;
		for (int i = templist.length - 1; i >= templist.length - topn; i--) {
			total += templist[i];
		}
		return Double.parseDouble(df4.format(total / (double) topn));
	}

	public static float getTotal(float[] dataList) {
		float total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}
		return total;
	}

	public static int getTotal(int[] dataList) {
		int total = 0;
		for (int i = 0; i < dataList.length; i++) {
			total += dataList[i];
		}
		return total;
	}

	public static double getGeoAve(ArrayList<Double> dataList) {
		if (dataList.size() == 0)
			return 0;

		double product = 1.0;
		double n = 1.0 / dataList.size();
		for (int i = 0; i < dataList.size(); i++) {
			product = product * dataList.get(i);
		}

		double geoAve = Math.pow(product, n);
		geoAve = Double.parseDouble(df4.format(geoAve));
		return geoAve;
	}

	public static double getGeoAve(double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double product = 1.0;
		double n = 1.0d / (double) dataList.length;
		for (int i = 0; i < dataList.length; i++) {
			product = product * dataList[i];
		}
		double geoAve = Math.pow(product, n);
		geoAve = Double.parseDouble(df4.format(geoAve));
		return geoAve;
	}

	public static double getMedianNoZero(ArrayList<Double> dataList) {
		if (dataList.size() == 0)
			return 0;

		double[] dataArrays = new double[dataList.size()];
		for (int i = 0; i < dataArrays.length; i++) {
			dataArrays[i] = dataList.get(i);
		}
		return getMedianNoZero(dataArrays);
	}

	public static double getMedianNoZero(double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double[] templist = new double[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);

		int startId = -1;

		for (int i = 0; i < templist.length; i++) {
			if (templist[i] > 0) {
				startId = i;
				break;
			}
		}

		if (startId == -1) {
			return 0;
		}

		int len = templist.length - startId;
		if (len % 2 == 0) {
			double d1 = templist[len / 2 - 1 + startId];
			double d2 = templist[len / 2 + startId];
			return Double.parseDouble(df4.format((d1 + d2) / 2.0));
		} else {
			return templist[(len - 1) / 2 + startId];
		}
	}

	public static double getMedian(ArrayList<Double> dataList) {
		if (dataList.size() == 0)
			return 0;

		double[] dataArrays = new double[dataList.size()];
		for (int i = 0; i < dataArrays.length; i++) {
			dataArrays[i] = dataList.get(i);
		}
		return getMedian(dataArrays);
	}

	public static int getMedian(int[] dataList) {
		if (dataList.length == 0)
			return 0;

		int[] templist = new int[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		int len = templist.length;
		if (len % 2 == 0) {
			int d1 = templist[len / 2 - 1];
			int d2 = templist[len / 2];
			return (int) ((d1 + d2) / 2.0);
		} else {
			return templist[(len - 1) / 2];
		}
	}

	public static double getMedian(double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double[] templist = new double[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		int len = templist.length;
		if (len % 2 == 0) {
			double d1 = templist[len / 2 - 1];
			double d2 = templist[len / 2];
			return Double.parseDouble(df4.format((d1 + d2) / 2.0));
		} else {
			return templist[(len - 1) / 2];
		}
	}

	public static double getMedian(Double[] dataList) {
		if (dataList.length == 0)
			return 0;

		double[] templist = new double[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		int len = templist.length;
		if (len % 2 == 0) {
			double d1 = templist[len / 2 - 1];
			double d2 = templist[len / 2];
			return Double.parseDouble(df4.format((d1 + d2) / 2.0));
		} else {
			return templist[(len - 1) / 2];
		}
	}

	public static float getMedian(float[] dataList) {
		if (dataList.length == 0)
			return 0;

		float[] templist = new float[dataList.length];
		System.arraycopy(dataList, 0, templist, 0, dataList.length);

		Arrays.sort(templist);
		int len = templist.length;
		if (len % 2 == 0) {
			double d1 = templist[len / 2 - 1];
			double d2 = templist[len / 2];
			return Float.parseFloat(df4.format((d1 + d2) / 2.0f));
		} else {
			return templist[(len - 1) / 2];
		}
	}

	public static double[] minusBG(Double[] data, int length, double thres) {
		double[] backGround = new double[data.length];
		ArrayList<Double> temp = new ArrayList<Double>();
		ArrayList<Double> testData = new ArrayList<Double>();

		temp.add(data[0]);
		testData.add(data[0]);

		for (int i = 1; i < data.length; i++) {

			temp.add(data[i]);
			double ratio = getVarRatio(temp);

			if (ratio < 2.01)
				testData.add(data[i]);
			else
				temp.remove(data[i]);

			if (testData.size() == length)
				break;
		}

		for (int i = 0; i < data.length; i++) {
			double di = data[i];
			double ave = getAve(testData);
			backGround[i] = Double.parseDouble(df4.format(di - ave));
//			System.out.println(di+"~"+ave+"~"+backGround[i]);
			testData.add(di);
			double ratio = getVarRatio(testData);
			if (ratio > thres) {
				testData.remove(di);
			} else {
				testData.remove(0);
			}
		}

		return backGround;
	}

	public static int[] minusBG(int[] data, int bin, double thres) {

		if (data.length < bin) {
			return data;
		}

		int[] window = new int[bin];
		int[] newData = new int[data.length];
		for (int i = 0; i < data.length - bin; i++) {
			System.arraycopy(data, i, window, 0, window.length);
			float rsd = getRSD(window);
			if (rsd > thres) {
				System.arraycopy(data, i, newData, i, window.length);
			}
		}

		return newData;
	}

	/**
	 * For "static" average window.
	 * 
	 * @param data
	 * @param isotopeLength
	 * @param thres
	 */
	public static Double[][] minusBG2(Double[] data) {

		Arrays.sort(data);
		ArrayList<Double> temp = new ArrayList<Double>(data.length);
		temp.add(data[0]);
		temp.add(data[1]);
		temp.add(data[2]);
		double t1 = getVarRatio(temp);
		int pos = 0;

		for (int i = 3; i < data.length; i++) {
			temp.add(data[i]);
			double t2 = getVarRatio(temp);
			if (t2 / t1 > 1.5) {
				pos = i;
				break;
			} else {
				t1 = t2;
			}
		}

		temp.remove(pos);
		double ave = getAve(temp);
		Double[][] sig = new Double[2][data.length];

		for (int j = 0; j < data.length; j++) {
			sig[0][j] = data[j];
			sig[1][j] = Double.parseDouble(df4.format(data[j] - ave));
//			System.out.println(sig[1][j]);
		}
		return sig;
	}

	public static double getX(double a) {
		double x;
		double d = Math.sqrt(a * a - a * 2);
		x = a - d;
		return x;
	}

	public static boolean hasSimilar(ArrayList<Double> douList) {

		for (int i = 0; i < douList.size(); i++) {
			double di = douList.get(i);
			for (int j = i + 1; j < douList.size(); j++) {
				double dj = douList.get(j);
				if (Double.parseDouble(df6.format(Math.abs(di - dj))) < 0.01)
					return true;
			}
		}

		return false;
	}

	public static double getFilterAve(ArrayList<Double> dataList) {
		ArrayList<Double> copylist = new ArrayList<Double>(dataList.size());
		copylist.addAll(dataList);
		ArrayList<Double> data;
		while (true) {
			if (copylist.size() < 4) {
				return getAve(copylist);
			}
			double threshold = getThres(copylist.size());
			data = getFilterData(copylist, threshold);
			if (data.size() == copylist.size()) {
				return (getAve(data) + getMedian(data)) / 2.0;
			} else {
				copylist = data;
			}
		}
	}

	public static double getFilterAve(ArrayList<Double> dataList, double threshold) {
		ArrayList<Double> copylist = new ArrayList<Double>(dataList.size());
		copylist.addAll(dataList);
		ArrayList<Double> data;
		while (true) {
			if (copylist.size() < 4) {
				return getAve(copylist);
			}
			data = getFilterData(copylist, threshold);
			if (data.size() == copylist.size()) {
				return (getAve(data) + getMedian(data)) / 2.0;
			} else {
				copylist = data;
			}
		}
	}

	public static ArrayList<Double> getFilterData(ArrayList<Double> dataList, double threshold) {
		ArrayList<Double> data = new ArrayList<Double>();
		double ave = getAve(dataList);
		for (int i = 0; i < dataList.size(); i++) {
			double d = dataList.get(i);
			if (d / ave < threshold && d / ave > (1.0 / threshold)) {
				data.add(d);
			}
		}
		if (data.size() == 0)
			return dataList;

		return data;
	}

	public static double getThres(int length) {
		return Math.log(length) / 6.0 + 1.5;
	}

	/**
	 * Using binomial distribution to calculate all the combination of the number in
	 * the list.
	 * 
	 * @param list
	 * @return
	 */
	public static HashSet<Double> doubleDiss(ArrayList<Double> list) {

		HashSet<Double> diss = new HashSet<Double>();
		int size = list.size();
		long num = (long) Math.pow(2.0, size) - 1;
		for (int i = 0; i <= num; i++) {
			int n = i;
			double d = 0;
			for (int j = 0; j < size; j++) {
				int k = n % 2;
				if (k == 1)
					d += list.get(j);
				n /= 2;
			}

			diss.add(d);
		}
		return diss;
	}

	public static double getMean2(double[][] data) {

		int len = data.length * data[0].length;
		if (len == 0)
			return 0;

		double total = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				total += data[i][j];
			}
		}
		double mean = Double.parseDouble(df4.format(total / (double) len));
		return mean;
	}

	public static double getCorr2(double[][] a, double[][] b) {

		if (a.length != b.length || a[0].length != b[0].length)
			return 0;

		double meanA = getMean2(a);
		double meanB = getMean2(b);

		double d0 = 0;
		double da = 0;
		double db = 0;

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				d0 += (a[i][j] - meanA) * (b[i][j] - meanB);
				da += (a[i][j] - meanA) * (a[i][j] - meanA);
				db += (b[i][j] - meanB) * (b[i][j] - meanB);
			}
		}

		if (da == 0 || db == 0)
			return 1;

		double r = Double.parseDouble(df4.format(d0 / Math.pow(da + db, 0.5)));
		return r;
	}

	// Function to calculate Arithmetic Mean
	public static double calculateArithmeticMean(double[] values) {
		double sum = Arrays.stream(values).sum();
		return sum / values.length;
	}

	// Function to calculate Geometric Mean
	public static double calculateGeometricMean(double[] values) {
		double[] nonZeroValues = Arrays.stream(values).filter(v -> v != 0).toArray();
		// If all values were zero, return 0
		if (nonZeroValues.length == 0) {
			return 0;
		}
		double product = Arrays.stream(nonZeroValues).reduce(1, (a, b) -> a * b);
		return Math.pow(product, 1.0 / nonZeroValues.length);
	}

	// Function to calculate Harmonic Mean
	public static double calculateHarmonicMean(double[] values) {
		double[] nonZeroValues = Arrays.stream(values).filter(v -> v != 0).toArray();
		// If all values were zero, return 0
		if (nonZeroValues.length == 0) {
			return 0;
		}

		double reciprocalSum = Arrays.stream(nonZeroValues).map(v -> 1.0 / v).sum();
		return nonZeroValues.length / reciprocalSum;
	}

	// Function to calculate Quadratic Mean (Root Mean Square)
	public static double calculateQuadraticMean(double[] values) {
		double squareSum = Arrays.stream(values).map(v -> v * v).sum();
		return Math.sqrt(squareSum / values.length);
	}

	public static double getStrictProteinPEP(double[] peptidePEPs) {
		// initialize the sum of the negative logarithms
		double sum = 0.0;
		// loop through the array of peptide PEPs
		for (double pep : peptidePEPs) {
			// add the negative logarithm of (1 - pep) to the sum
			sum += -Math.log(1 - pep);
		}
		// calculate the protein PEP using the formula
		double proteinPEP = 1 - Math.exp(-sum);
		// return the protein PEP
		return proteinPEP;
	}

	public static double getLooseProteinPEP(double[] peptidePEPs) {
		// initialize the sum of the negative logarithms
		double sum = 0.0;
		// loop through the array of peptide PEPs
		for (double pep : peptidePEPs) {
			// add the negative logarithm of (1 - pep) to the sum
			sum += Math.log(pep);
		}
		// calculate the protein PEP using the formula
		double proteinPEP = Math.exp(sum);
		// return the protein PEP
		return proteinPEP;
	}

	public static double binomialProbability(int m, int n, int x) {
		// Check if the parameters are valid
		if (m < 0 || n < 0 || x < 0 || x > m || x > n) {
			throw new IllegalArgumentException("Invalid parameters");
		}
		// Use the binomial distribution formula
		double p = 1.0 / n; // The probability of success
		double q = 1.0 - p; // The probability of failure
		// Calculate the binomial coefficient using a loop
		double coefficient = 1.0;
		for (int i = 0; i < x; i++) {
			coefficient *= (m - i) / (double) (x - i);
		}
		// Calculate the probability using Math.pow
		double probability = coefficient * Math.pow(p, x) * Math.pow(q, m - x);
		// Return the probability
		return probability;
	}

	// Function to calculate factorial
	public static BigInteger factorial(int n) {
		if (n == 0 || n == 1) {
			return BigInteger.ONE;
		} else {
			return BigInteger.valueOf(n).multiply(factorial(n - 1));
		}
	}

	// Function to calculate multinomial probability
	public static double multinomialProbability(int m, int[] counts) {
		BigInteger numerator = factorial(m);
		BigInteger denominator = BigInteger.ONE;

		for (int count : counts) {
			denominator = denominator.multiply(factorial(count));
		}

		return numerator.doubleValue() / denominator.doubleValue();
	}

	// This method returns the probability of getting a number of balls in box A
	// that is as extreme or more extreme than x, given m balls, n boxes, and x <= m
	public static double binomialTest(int m, int n, int x) {
		// Check if the parameters are valid
		if (m < 0 || n < 0 || x < 0 || x > m || x > n) {
			throw new IllegalArgumentException("Invalid parameters");
		}
		// Use the binomial distribution formula
		double p = 1.0 / n; // The probability of success
		double q = 1.0 - p; // The probability of failure
		// Calculate the two-tailed probability using a loop
		double probability = 0.0;
		for (int k = 0; k <= m; k++) {
			// Only consider the values that are as extreme or more extreme than x
			if (Math.abs(k - m * p) >= Math.abs(x - m * p)) {
				// Calculate the binomial coefficient using a loop
				double coefficient = 1.0;
				for (int i = 0; i < k; i++) {
					coefficient *= (m - i) / (double) (k - i);
				}
				// Add the probability of getting exactly k balls in box A
				probability += coefficient * Math.pow(p, k) * Math.pow(q, m - k);
			}
		}
		// Return the probability
		return probability;
	}

	// This method returns the probability of getting a number of balls in box A
	// that is as extreme or more extreme than x, given m balls, n boxes, and x <= m
	public static double poissonTest(int m, int n, int x) {
		// Check if the parameters are valid
		if (m < 0 || n < 0 || x < 0 || x > m) {
			throw new IllegalArgumentException("Invalid parameters");
		}
		// Use the Poisson distribution formula
		double lambda = (double) m / n; // The average number of balls in box A
		double e = Math.E; // The base of the natural logarithm
		// Calculate the two-tailed probability using a loop
		double probability = 0.0;

		long factorial = 1;
		for (int i = 2; i <= x; i++) {
			factorial *= i;
		}
//		System.out.println("factorial\t"+factorial);
		// Add the probability of getting exactly k balls in box A
		probability += Math.pow(lambda, x) * Math.pow(e, -lambda) / factorial;

		// Return the probability
		return probability;
	}

	public static double calculateStandardError(double standardDeviation, int sampleSize) {
		return standardDeviation / Math.sqrt(sampleSize);
	}

	public static double[] calculateConfidenceInterval(double sampleMean, double standardError,
			double confidenceLevel) {
		NormalDistribution normalDistribution = new NormalDistribution();
		double criticalValue = normalDistribution.inverseCumulativeProbability((1 + confidenceLevel) / 2);

		double marginOfError = criticalValue * standardError;

		double lowerBound = sampleMean - marginOfError;
		double upperBound = sampleMean + marginOfError;

		return new double[] { lowerBound, upperBound };
	}

}
