/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.ms2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.math.MathTool;


/**
 * @author Kai Cheng
 *
 */
public class NCoreOverlapSegment implements Comparable<NCoreOverlapSegment> {

	/**
	 * nbt.1511-S1, p9
	 */
	protected static final double dm = 1.00286864;

	private ArrayList<NCorePeakSegment> list;
	private ArrayList<Double> mzList;
	private ArrayList<Double> intenList;
	private ArrayList<Double> peakMzList;
	private ArrayList<Integer> chargeList;
	private double currentIntensity;
	private int currentLength;

	// different charges have different values
	// private boolean [] hasNecePeak;

	// first dimetion is charge, second is 5 NCore
	private double[][] mzs;
	private double[][] intens;

	private boolean use;
	private boolean full;
	private int matchTypeCount;
	private int matchTypeCountTotal;

	public NCoreOverlapSegment() {
		this.list = new ArrayList<NCorePeakSegment>();
		this.mzList = new ArrayList<Double>();
		this.intenList = new ArrayList<Double>();
		this.peakMzList = new ArrayList<Double>();
		this.chargeList = new ArrayList<Integer>();
		this.mzs = new double[8][10];
		this.intens = new double[8][10];
	}

	public NCoreOverlapSegment(ArrayList<NCorePeakSegment> list) {

		this.list = list;
		this.mzList = new ArrayList<Double>();
		this.intenList = new ArrayList<Double>();
		this.peakMzList = new ArrayList<Double>();
		this.chargeList = new ArrayList<Integer>();
		this.mzs = new double[8][10];
		this.intens = new double[8][10];
		
		for (int i = 0; i < list.size(); i++) {
			mzList.add(list.get(i).getMass());
			intenList.add(list.get(i).getPeakInten());
			peakMzList.add(list.get(i).getOriginalMz());
		}
	}

	public void add(NCorePeakSegment ps) {

		if (ps.getCharge() >= 8)
			return;

		this.list.add(ps);
		mzList.add(ps.getMass());
		intenList.add(ps.getPeakInten());
		peakMzList.add(ps.getOriginalMz());

		int charge = ps.getCharge();
		int type = ps.getType();

		this.mzs[charge - 1][type] = ps.getOriginalMz();
		this.intens[charge - 1][type] += ps.getPeakInten();
	}

	public double getScore(double threshold) {
		return Math.log10(getInten() / threshold * (double) intenList.size());
	}

	public double getScore2(double threshold) {

		double d0 = 0;
		double dd = 0;

		for (int i = 0; i < this.intens.length; i++) {
			for (int j = 0; j < this.intens[i].length; j++) {
				if (j == 0) {
					d0 += intens[i][j];
				} else {
					dd += intens[i][j];
				}
			}
		}

		return Math.log10((d0 * 4.0 + dd) / threshold * (double) intenList.size());
	}

	public double getScore3() {

		double total = 0;

		double[] totalInten = new double[5];
		for (int i = 0; i < totalInten.length; i++) {
			for (int j = 0; j < this.intens.length; j++) {
				totalInten[i] += intens[j][i];
			}
		}

		ArrayList<Double> list = new ArrayList<Double>();
		for (int i = 0; i < totalInten.length; i++) {
			if (totalInten[i] != 0) {
				list.add(totalInten[i]);
			}
		}

		int num = 0;
		for (int i = 0; i < list.size(); i++) {

			if (i == 0) {

				total += list.get(0) / list.get(1);
				num++;

			} else if (i == (list.size() - 1)) {

				total += list.get(i) / list.get(i - 1);
				num++;

			} else {

				total += list.get(i) / list.get(i - 1);
				total += list.get(i) / list.get(i + 1);

				num += 2;
			}
		}

		total = total / (double) num;

		return total;
	}

	public double getScore4() {
		return this.getScore2(this.getScore3());
	}

	public double getPercent0() {

		double d0 = 0;
		double dd = 0;

		for (int i = 0; i < this.intens.length; i++) {
			for (int j = 0; j < this.intens[i].length; j++) {
				if (j == 0) {
					d0 += intens[i][j];
				}
				dd += intens[i][j];
			}
		}
		return d0 / dd;
	}

	public void combineIsotope(NCoreOverlapSegment ols, int dmi) {
		double[][] olsintens = ols.intens;
		for (int i = 0; i < this.intens.length; i++) {
			for (int j = 0; j < this.intens[i].length; j++) {
				if (olsintens[i][j] != 0) {
					this.intens[i][j] += olsintens[i][j];
					if (mzs[i][j] == 0) {
						this.mzs[i][j] = ols.mzs[i][j] - dm * dmi / (double) (i + 1);
					}
				}
			}
		}
		this.list.addAll(ols.list);
		this.intenList.addAll(ols.intenList);
		this.peakMzList.addAll(ols.peakMzList);

		for (int i = 0; i < ols.list.size(); i++) {
			this.mzList.add(ols.list.get(i).getMass() - dm * dmi);
		}
		this.currentIntensity = ols.getInten();
		this.currentLength = ols.getCurrentLength();
	}

	public double compareScore(NCoreOverlapSegment ols) {

		double c0 = 0;
		for (int i = 0; i < this.intens.length; i++) {
			c0 += this.intens[i][0];
		}

		double c1 = 0;
		for (int i = 0; i < ols.intens.length; i++) {
			c1 += ols.intens[i][0];
		}

		double comscore = Math.log(c0 / c1);

		return comscore;
	}

	public void validate() {

		boolean use = false;
		int max = 0;
		int maxall = 0;
		int[][] count = new int[8][10];

		for (int i = 0; i < list.size(); i++) {

			NCorePeakSegment ps = list.get(i);
			int charge = ps.getCharge();
			int type = ps.getType();
			count[charge - 1][type] = 1;
		}
		double[] typeinten = new double[5];
		for (int i = 0; i < count.length; i++) {

			int cc1 = 0;
			int cc2 = 0;
			double[] typeinteni = new double[5];

			for (int j = 0; j < count[i].length; j++) {
				if (j < 5) {
					cc1 += count[i][j];
					typeinteni[j] += this.intens[i][j];
				} else {
					cc2 += count[i][j];
					typeinteni[j - 5] += this.intens[i][j];
				}
			}

			if (cc1 == 5 || cc2 == 5) {
				full = true;
			}

			if (cc1 > 1) {
				if (cc1 >= 3 && count[i][0] > 0) {
					this.chargeList.add(i + 1);
					use = true;
				} else {

					if (cc1 + cc2 >= 5 && (count[i][0] + count[i][5]) > 0) {
						this.chargeList.add(i + 1);
						use = true;
					}
				}
				if (cc1 > max) {
					max = cc1;
					typeinten = typeinteni;
				}
				if (cc2 > max) {
					max = cc2;
					typeinten = typeinteni;
				}
				if (cc1 + cc2 > maxall) {
					maxall = cc1 + cc2;
					typeinten = typeinteni;
				}
			}
			// System.out.println(i+"\t"+cc1+"\t"+cc2+"\t"+Arrays.toString(typeinteni));
			// if(cc1+cc2>0)
			// System.out.println(Arrays.toString(this.intens[i])+"\t"+Arrays.toString(typeinteni));
		}

		double maxinten = 0;
		for (int i = 0; i < typeinten.length; i++) {
			if (typeinten[i] > maxinten) {
				maxinten = typeinten[i];
			}
		}

		int tcount = 0;
		for (int i = 0; i < typeinten.length; i++) {
			if (typeinten[i] > 0) {
				if (maxinten == typeinten[0]) {
					if (typeinten[i] * 20.0 >= maxinten) {
						tcount++;
					}
				} else {
					if (typeinten[i] * 10.0 >= maxinten) {
						tcount++;
					}
				}
			}
		}

		if (tcount < 3) {
			use = false;
		}

		this.matchTypeCount = max;
		this.matchTypeCountTotal = maxall;
		this.use = use;
	}

	public boolean isUse() {
		return use;
	}

	public boolean isFull() {
		return full;
	}

	public int size() {
		return list.size();
	}

	public int getMatchTypeCount() {
		return matchTypeCount;
	}

	public int getMatchTypeCountTotal() {
		return matchTypeCountTotal;
	}

	public double getMass() {
		return MathTool.getMedian(mzList);
	}

	public double getInten() {
		return MathTool.getTotal(intenList);
	}

	public double getCurrentIntensity() {
		if (this.currentIntensity == 0) {
			this.currentIntensity = this.getInten();
		}
		return this.currentIntensity;
	}

	public int getCurrentLength() {
		if (this.currentLength == 0) {
			this.currentLength = this.mzList.size();
		}
		return this.currentLength;
	}

	public ArrayList<Double> getMassList() {
		return mzList;
	}

	public ArrayList<Double> getIntensityList() {
		return intenList;
	}

	public Double[] getPeakMassArrays() {
		Double[] masses = peakMzList.toArray(new Double[peakMzList.size()]);
		Arrays.sort(masses);
		return masses;
	}

	public ArrayList<NCorePeakSegment> getPeakSegList() {
		return list;
	}

	public void find() {

		HashSet<Integer> set = new HashSet<Integer>();

		for (int i = 0; i < mzList.size(); i++) {

			if (set.contains(i))
				continue;

		}
	}

	public double[] getMzList(int charge) {
		return this.mzs[charge - 1];
	}

	public Integer[] getChargeList() {
		Integer[] list = this.chargeList.toArray(new Integer[this.chargeList.size()]);
		return list;
	}

	public double getMaxY1() {
		double max = 0;
		for (int i = 0; i < this.intens.length; i++) {
			if (intens[i][0] > max) {
				max = intens[i][0];
			}
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NCoreOverlapSegment o) {
		// TODO Auto-generated method stub

		int count1 = this.matchTypeCountTotal;
		int count2 = o.matchTypeCountTotal;

		if (this.full) {
			if (o.full) {
				if (count1 > count2) {
					return 1;
				} else if (count1 < count2) {
					return -1;
				} else {
					// double inten1 = o1.getScore4();
					// double inten2 = o2.getScore4();

					double inten1 = this.getInten();
					double inten2 = o.getInten();

					if (inten1 > inten2) {
						return 1;
					} else if (inten1 == inten2) {
						return 0;
					} else {
						return -1;
					}
				}
			} else {
				return 1;
			}
		} else {
			if (o.full) {
				return -1;
			} else {
				if (count1 > count2) {
					return 1;
				} else if (count1 < count2) {
					return -1;
				} else {
					// double inten1 = o1.getScore4();
					// double inten2 = o2.getScore4();

					double inten1 = this.getInten();
					double inten2 = o.getInten();

					if (inten1 > inten2) {
						return 1;
					} else if (inten1 == inten2) {
						return 0;
					} else {
						return -1;
					}
				}
			}
		}
	}

}
