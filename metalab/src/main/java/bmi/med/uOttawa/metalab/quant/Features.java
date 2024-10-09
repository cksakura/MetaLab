/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.math.MathTool;


/**
 * @author Kai Cheng
 *
 */
public class Features implements Comparable <Features> {

	private HashMap<Integer, Feature> feaMap;
	private double monoMz;
	private int[] scanList;
	private double[][] intenList;
	private double[] rtList;
	private double[] idenRtList;

	private int presentFeaNum;
	private double pepMass;

	/**
	 * The rt of begin, max, end
	 */
	private double[] rtRange;

	private int charge;
	private boolean validate;
	private boolean use;

	protected boolean normal;
	protected int[] select;
	protected double[] normalFactor;

//	protected DecimalFormat df4 = DecimalFormats.DF0_4;
//	protected DecimalFormat dfE4 = DecimalFormats.DF_E4;

	public Features(double monoMz, int charge){
		this.charge = charge;
		this.monoMz = monoMz;
		this.feaMap = new HashMap <Integer, Feature> ();
		this.setInfo();
	}

	public Features(double monoMz, int charge, double [] idenRtList){
		this.charge = charge;
		this.monoMz = monoMz;
		this.idenRtList = idenRtList;
		this.feaMap = new HashMap <Integer, Feature> ();
		this.setInfo();
	}

	public Features(double monoMz, int[] scanList, double[][] intenList, double[] rtList, int charge) {
		this.charge = charge;
		this.feaMap = new HashMap<Integer, Feature>();
		this.monoMz = monoMz;
		this.scanList = scanList;
		this.intenList = intenList;
		this.rtList = rtList;
	}

	public void addFeature(Feature fea) {
		feaMap.put(fea.getScanNum(), fea);
	}

	public int getCharge() {
		return charge;
	}

	public double getPepMass() {
		return this.pepMass;
	}

	public void setPepMass(double pepMass) {
		this.pepMass = pepMass;
	}

	public double getMonoMz() {
		return monoMz;
	}

	public void setRtRange(double[] rtRange) {
		this.rtRange = rtRange;
	}

	public double[] getRtRange() {
		return rtRange;
	}

	public int getPresentFeasNum() {
		return presentFeaNum;
	}

	public void setPresentFeasNum(int presentFeaNum) {
		this.presentFeaNum = presentFeaNum;
	}

	/**
	 * The inherited class should overload this method.
	 * 
	 * @param fea
	 * @return
	 */
//	public boolean contain(Feature fea) {
//		return false;
//	}

	public int getLength() {
		return this.scanList.length;
	}

	public Feature getFeature(int scanNum) {
		return feaMap.get(scanNum);
	}

	public int getLastScan() {
		return scanList[scanList.length - 1];
	}

	public int[] getScanList() {
		return scanList;
	}

	public HashMap<Integer, Feature> getFeaMap() {
		return feaMap;
	}

	public double[][] getIntenList() {
		return this.intenList;
	}

	public double[] getPartIntenList(double rtBeg, double rtEnd) {

		int idbeg = Arrays.binarySearch(rtList, rtBeg);
		int idend = Arrays.binarySearch(rtList, rtEnd);

		if (idbeg < 0) {
			idbeg = -idbeg - 1;
		}

		if (idend < 0) {
			idend = -idend - 1;
		}

		double[] pIntenList = new double[idend - idbeg];

		if (pIntenList.length == 0)
			return null;

		System.arraycopy(intenList, idbeg, pIntenList, 0, pIntenList.length - 1);

		return pIntenList;
	}

	public double[] getRTList() {
		return this.rtList;
	}

	public double[] getPartRTList(double rtBeg, double rtEnd) {

		int idbeg = Arrays.binarySearch(rtList, rtBeg);
		int idend = Arrays.binarySearch(rtList, rtEnd);

		if (idbeg < 0) {
			idbeg = -idbeg - 1;
		}

		if (idend < 0) {
			idend = -idend - 1;
		}

		double[] prtlist = new double[idend - idbeg];

		// System.out.println(rtBeg+"\t"+rtEnd+"\t"+idbeg+"\t"+idend+"\t"+rtList.length+"\t"+prtlist.length);
		if (prtlist.length == 0)
			return null;

		System.arraycopy(rtList, idbeg, prtlist, 0, prtlist.length - 1);

		return prtlist;
	}
	
	public double getTotalIntensity() {
		double intensity = 0;
		for (int i = 0; i < this.intenList.length; i++) {
			for (int j = 0; j < this.intenList[i].length; j++) {
				intensity += this.intenList[i][j];
			}
		}
		return intensity;
	}
	
	public void setInfo() {

		if (feaMap.size() == 0) {
			this.scanList = new int[0];
			this.rtList = new double[0];
			this.intenList = new double[0][];
			return;
		}

		Integer[] scans = feaMap.keySet().toArray(new Integer[feaMap.size()]);
		Arrays.sort(scans);

		int[] isotopeCount = new int[Feature.isotopeLength];
		boolean[] begin = new boolean[Feature.isotopeLength];
		boolean[] miss = new boolean[Feature.isotopeLength];
		boolean[] totalMiss = new boolean[Feature.isotopeLength];
		double[] isotopeIntensity = new double[Feature.isotopeLength];

		for (int i = 0; i < scans.length; i++) {
			double[] intenList = feaMap.get(scans[i]).getIntens();
			for (int j = 0; j < intenList.length; j++) {
				if (intenList[j] > 0) {
					begin[j] = true;
					isotopeCount[j]++;
					isotopeIntensity[j] += intenList[j];
				} else {
					if (begin[j]) {
						if (miss[j]) {
							totalMiss[j] = true;
						} else {
							miss[j] = true;
						}
					}
				}
			}
//			System.out.println(scans[i]+"\t"+Arrays.toString(intenList));
		}

		int mono = Feature.isotopeCount;
		for (int i = Feature.isotopeCount - 1; i >= 0; i--) {
//			if (totalMiss[i])
//				break;
//			System.out.println(isotopeCount[i]+"\t"+isotopeCount[Feature.isotopeCount]);
			if (isotopeCount[i] >= isotopeCount[Feature.isotopeCount] * 0.8) {
				double intensityRatio = isotopeIntensity[i] / isotopeIntensity[Feature.isotopeCount];
				if (Math.abs(Math.log10(intensityRatio)) < 1.2) {
					mono = i;
				}
			}else if(isotopeCount[i] < isotopeCount[Feature.isotopeCount] * 0.8 && isotopeCount[i] > isotopeCount[Feature.isotopeCount] * 0.6){
//				System.out.println(i+"\t"+Feature.isotopeCount+"\t"+isotopeCount[i]+"\t"+isotopeCount[Feature.isotopeCount]);
				this.scanList = new int[0];
				this.rtList = new double[0];
				this.intenList = new double[0][];
				return;
			}else if(isotopeCount[i] < isotopeCount[Feature.isotopeCount] * 0.6){
				break;
			}
		}

		int firstScanId = 0;
		int lastScanId = scans.length - 1;
		for (int i = 0; i < scans.length; i++) {
			double[] intenList = feaMap.get(scans[i]).getIntens();
			if (intenList[mono] > 0) {
				firstScanId = i;
				break;
			}
		}
		for (int i = scans.length - 1; i >= 0; i--) {
			double[] intenList = feaMap.get(scans[i]).getIntens();
			if (intenList[mono] > 0) {
				lastScanId = i;
				break;
			}
		}

		this.scanList = new int[lastScanId - firstScanId + 1];
		this.rtList = new double[lastScanId - firstScanId + 1];
		this.intenList = new double[lastScanId - firstScanId + 1][];
		double[] mzs = new double[lastScanId - firstScanId + 1];
		double[] mzs2 = new double[lastScanId - firstScanId + 1];
		double[] mzs3 = new double[lastScanId - firstScanId + 1];
		
		for (int i = firstScanId; i <= lastScanId; i++) {
			Feature fea = feaMap.get(scans[i]);
			double[] intenList = fea.getIntens();
			double rt = fea.getRT();
			this.intenList[i - firstScanId] = new double[3];
			System.arraycopy(intenList, mono, this.intenList[i - firstScanId], 0, this.intenList[i - firstScanId].length);
			scanList[i - firstScanId] = scans[i];
			rtList[i - firstScanId] = rt;
			mzs[i - firstScanId] = fea.getMasses()[mono];
			mzs2[i - firstScanId] = fea.getMasses()[mono+1];
			mzs3[i - firstScanId] = fea.getMasses()[mono+2];
//			System.out.println(scanList[i - firstScanId]+"\t"+mzs[i - firstScanId]+"\t"+Arrays.toString(this.intenList[i - firstScanId]));
//			System.out.println(scanList[i - firstScanId]+"\t"+mzs[i - firstScanId]+"\t"+MathTool.getTotal(this.intenList[i - firstScanId]));
		}

		double mono1 = MathTool.getMedian(mzs);
		double mono2 = MathTool.getMedian(mzs2) - Feature.dm / (double) charge;
		double mono3 = MathTool.getMedian(mzs3) - Feature.dm / (double) charge * 2.0;

//		this.monoMz = MathTool.getMedian(mzs);
		this.monoMz = (mono1 + mono2 + mono3) / 3.0;
//		System.out.println(MathTool.getMedian(mzs)+"\t"+MathTool.getMedian(mzs2)+"\t"+MathTool.getMedian(mzs3) +"\t"+ monoMz);
		this.rtRange = new double[2];
		this.rtRange[0] = rtList[0];
		this.rtRange[1] = rtList[rtList.length-1];
//		this.monoMz = MathTool.getAve(mzs);
	}
	
	public boolean contain(int scannum, double mz, double tolerance) {
		if (this.scanList.length == 0)
			return false;
		if (scannum > this.scanList[this.scanList.length - 1] || scannum < this.scanList[0])
			return false;
		boolean contain = false;
		for (int i = 0; i < 6; i++) {
			double mzi = this.monoMz + Feature.dm * (double) i / (double) this.charge;
			if (Math.abs(mzi - mz) < tolerance) {
				contain = true;
				break;
			}
		}
		return contain;
	}
	
	public boolean containMono(int scannum, double mz, double tolerance) {
		if (this.scanList.length == 0)
			return false;
		if (scannum > this.scanList[this.scanList.length - 1] || scannum < this.scanList[0])
			return false;
		boolean contain = false;
		if (Math.abs(this.monoMz - mz) < tolerance) {
			contain = true;
		}
		return contain;
	}
	
	public boolean containWithRtTolerance(double mz, double tolerance, double rt, double rtTolerance) {
		if (this.scanList.length == 0)
			return false;
		if (this.rtRange[0] - rt > rtTolerance || rt - this.rtRange[1] > rtTolerance)
			return false;
		boolean contain = false;
		for (int i = 0; i < 6; i++) {
			double mzi = this.monoMz + Feature.dm * (double) i / (double) this.charge;
			if (Math.abs(mzi - mz) < tolerance) {
				contain = true;
				break;
			}
		}
		return contain;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Features fs) {
		// TODO Auto-generated method stub
		double d = this.getPepMass();
		double d1 = fs.getPepMass();
		return d > d1 ? 1 : -1;
	}
	
	public String toString() {
		if (this.scanList.length == 0)
			return "null";
		StringBuilder sb = new StringBuilder();
		sb.append("mz=").append(this.monoMz).append("\n");
		sb.append("charge=").append(this.charge).append("\n");
		sb.append("form scan ").append(this.scanList[0]).append(" to scan ").append(this.scanList[scanList.length - 1])
				.append("\n");
		sb.append("form rt ").append(this.rtList[0]).append(" to rt ").append(this.rtList[rtList.length - 1])
				.append("\n");
		return sb.toString();
	}
	
	public String toStringOneLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(monoMz).append(";");
		sb.append(charge).append(";");
		for (int i = 0; i < scanList.length; i++) {
			sb.append(scanList[i]).append("_");
			sb.append(rtList[i]).append("_");
			for (int j = 0; j < intenList[i].length - 1; j++) {
				sb.append(intenList[i][j]).append("_");
			}
			sb.append(intenList[i][intenList[i].length - 1]).append(",");
		}
		return sb.toString();
	}
	
	public static Features parse(String line) {
		String[] cs = line.split(";");
		if (cs.length != 3)
			return null;

		double monoMz = Double.parseDouble(cs[0]);
		int charge = Integer.parseInt(cs[1]);

		String[] ss = cs[2].split(",");
		int[] scanList = new int[ss.length];
		double[] rtList = new double[ss.length];
		double[][] intenList = new double[ss.length][];

		for (int i = 0; i < ss.length; i++) {
			String[] sss = ss[i].split("_");
			scanList[i] = Integer.parseInt(sss[0]);
			rtList[i] = Double.parseDouble(sss[1]);
			intenList[i] = new double[sss.length - 2];
			for (int j = 0; j < intenList[i].length; j++) {
				intenList[i][j] = Double.parseDouble(sss[j + 2]);
			}
		}

		Features features = new Features(monoMz, scanList, intenList, rtList, charge);
		return features;
	}

	private class PixelList {

		private double[] allRtList;
		private double[] intenlist;
		private ArrayList<Integer> maxIdList;
		private HashSet<Integer> maxIdSet;
		private HashSet<Integer> minIdSet;
		private int maxscore;

		private int begid = -1;
		private int endid = -1;
		private int maxid = -1;
		private int leftZero = -1;
		private int rightZero = -1;
		private double[] trueIntenList;
		private double[] newIntenList;
		private boolean use;

		private PixelList(double[] intens, double[] allRtList) {

			this.intenlist = intens;
			this.trueIntenList = new double[intenlist.length];
			this.allRtList = allRtList;
			this.maxIdList = new ArrayList<Integer>();
			this.maxIdSet = new HashSet<Integer>();
			this.minIdSet = new HashSet<Integer>();

			int noZero = 0;
			for (int i = 0; i < intenlist.length; i++) {
				if (intenlist[i] > 0) {
					if (leftZero == -1) {
						leftZero = i;
					}
					noZero++;
				}
			}

			for (int i = intenlist.length - 1; i >= 0; i--) {
				if (intenlist[i] > 0) {
					if (rightZero == -1) {
						rightZero = i;
					}
				}
			}

			if (noZero < 5) {
				this.use = false;
			} else {
				this.use = true;
				findMaxId();
			}
		}

		private void findMaxId() {

			// int combineCount = intenlist.length/30;

			// if(combineCount<=1){

			// * for(int i=0;i<combinelist.length;i++){
			// * combinelist[i] = intenlist[i];
			// * }

			// }else{

			/*
			 * for(int i=0;i<combinelist.length;i++){
			 * 
			 * if(intenlist[i]>0){ int beg = i-combineCount>=0 ? i-combineCount
			 * : 0; int end = i+combineCount<=combinelist.length ?
			 * i+combineCount : combinelist.length; for(int j=beg;j<end;j++){
			 * combinelist[i] += intenlist[j]; } }
			 * System.out.println("combine\t"+combinelist[i]+"\t"+intenlist[i]);
			 * }
			 */
			// }

			double max = -1;
			int totalmaxid = -1;

			for (int i = 2; i < intenlist.length - 2; i++) {

				if (intenlist[i] > max) {
					max = intenlist[i];
					totalmaxid = i;
				}

				if (intenlist[i] > intenlist[i - 1] && intenlist[i - 1] > 0 && intenlist[i] > intenlist[i - 2]
						&& intenlist[i - 2] > 0 && intenlist[i] > intenlist[i + 1] && intenlist[i + 1] > 0
						&& intenlist[i] > intenlist[i + 2] && intenlist[i + 2] > 0) {

					if (intenlist[i] / intenlist[i - 1] < 10 && intenlist[i] / intenlist[i + 1] < 10) {

						if (maxIdList.size() == 0) {
							maxIdList.add(i);
							maxIdSet.add(i);
						} else {
							Integer lastId = maxIdList.get(maxIdList.size() - 1);
							if (i - lastId > 3) {
								maxIdList.add(i);
								maxIdSet.add(i);
							} else {
								if (intenlist[i] > intenlist[lastId]) {
									maxIdList.remove(lastId);
									maxIdSet.remove(lastId);
									maxIdList.add(i);
									maxIdSet.add(i);
								}
							}
						}
					}

				} else if (intenlist[i] > 0) {

					boolean locmin = true;
					if (intenlist[i - 1] > 0 && intenlist[i - 1] < intenlist[i]) {
						locmin = false;
					}
					if (intenlist[i - 2] > 0 && intenlist[i - 2] < intenlist[i]) {
						locmin = false;
					}
					if (intenlist[i + 1] > 0 && intenlist[i + 1] < intenlist[i]) {
						locmin = false;
					}
					if (intenlist[i + 2] > 0 && intenlist[i + 2] < intenlist[i]) {
						locmin = false;
					}

					if (locmin)
						minIdSet.add(i);
				}
			}

			// System.out.println("max\t"+maxIdList+"\tmin"+minIdSet);

			if (maxIdList.size() == 0) {

				this.maxid = totalmaxid;

			} else if (maxIdList.size() == 1) {

				this.maxid = maxIdList.get(0);

			} else {

				/*
				 * Double minDiff = Double.MAX_VALUE; ArrayList <Integer> less30
				 * = new ArrayList <Integer>();
				 * 
				 * for(int i=0;i<maxIdList.size();i++){ double totalRtDiff = 0;
				 * for(int j=0;j<idenRtList.length;j++){ totalRtDiff +=
				 * Math.abs(idenRtList[j]-allRtList[maxIdList.get(i)]); }
				 * totalRtDiff = totalRtDiff/(double)idenRtList.length;
				 * if(totalRtDiff<0.5){ less30.add(maxIdList.get(i)); }
				 * if(minDiff>totalRtDiff){ minDiff = totalRtDiff; this.maxid =
				 * maxIdList.get(i); }
				 * //System.out.println("diff\t"+maxIdList.get(i)+"\t"+
				 * totalRtDiff); }
				 * 
				 * if(less30.size()>1){ double less30Max = 0; for(int
				 * i=0;i<less30.size();i++){
				 * if(combinelist[less30.get(i)]>less30Max){ this.maxid =
				 * less30.get(i); } } }
				 */
				/*
				 * for(int i=0;i<maxIdList.size()-1;i++){
				 * 
				 * int s1 = 0; int s2 = 0;
				 * 
				 * for(int j=0;j<idenRtList.length;j++){
				 * 
				 * if(idenRtList[j]<=allRtList[maxIdList.get(i)]){
				 * 
				 * s1++;
				 * 
				 * }else if(idenRtList[j]>allRtList[maxIdList.get(i)] &&
				 * idenRtList[j]<allRtList[maxIdList.get(i+1)]){
				 * 
				 * double min = combinelist[maxIdList.get(i)]; double minrt =
				 * -1; int minid = -1;
				 * 
				 * for(int k=maxIdList.get(i)+3;k<maxIdList.get(i+1)-2;k++){
				 * if(combinelist[k]<min){ min = combinelist[k]; minrt =
				 * allRtList[k]; minid = k; } }
				 * 
				 * if(minrt>0){
				 * 
				 * if(idenRtList[j]<minrt){ s1++; }else
				 * if(idenRtList[j]==minrt){ s1++; s2++; }else{ s2++; }
				 * 
				 * minIdSet.add(minid);
				 * 
				 * }else{
				 * 
				 * if(combinelist[this.maxIdList.get(i)]>combinelist[this.
				 * maxIdList.get(i+1)]){ s1++; }else{ s2++; } } }else{ s2++; } }
				 * 
				 * if(s1>s2){
				 * 
				 * this.maxid = maxIdList.get(i);
				 * 
				 * }else if(s1==s2){
				 * 
				 * if(combinelist[this.maxIdList.get(i)]>combinelist[this.
				 * maxIdList.get(i+1)]){
				 * 
				 * this.maxid = maxIdList.get(i);
				 * 
				 * }else{
				 * 
				 * this.maxid = maxIdList.get(i+1); }
				 * 
				 * }else{
				 * 
				 * this.maxid = maxIdList.get(i+1); } }
				 */

				// better than above 2

				int idenId = 0;
				int[] score = new int[maxIdList.size()];

				for (int i = 0; i < maxIdList.size() - 1; i++) {

					if (idenRtList[idenId] <= allRtList[maxIdList.get(i)]) {

						score[i]++;
						idenId++;
						i--;

					} else if (idenRtList[idenId] > allRtList[maxIdList.get(i)]
							&& idenRtList[idenId] < allRtList[maxIdList.get(i + 1)]) {

						double min = intenlist[maxIdList.get(i)];
						double minrt = -1;
						int minid = -1;

						for (int j = maxIdList.get(i) + 3; j < maxIdList.get(i + 1) - 2; j++) {
							if (intenlist[j] < min) {
								min = intenlist[j];
								minrt = allRtList[j];
								minid = j;
							}
						}

						if (minrt > 0) {

							if (idenRtList[idenId] < minrt) {
								score[i]++;
							} else if (idenRtList[idenId] == minrt) {
								score[i]++;
								score[i + 1]++;
							} else {
								score[i + 1]++;
							}

							idenId++;
							i--;
							minIdSet.add(minid);

						} else {

							if (intenlist[this.maxIdList.get(i)] > intenlist[this.maxIdList.get(i + 1)]) {
								score[i]++;
							} else {
								score[i + 1]++;
							}

							idenId++;
							i--;
						}
					}

					if (idenId == idenRtList.length)
						break;
				}

				score[score.length - 1] += (idenRtList.length - idenId);

				int maxScore = -1;
				int maxScoreId = -1;
				for (int i = 0; i < score.length; i++) {
					if (score[i] > maxScore) {
						maxScore = score[i];
						maxScoreId = i;
					} else if (score[i] == maxScore) {
						if (intenlist[maxIdList.get(i)] > intenlist[maxIdList.get(maxScoreId)]) {
							maxScore = score[i];
							maxScoreId = i;
						}
					}
				}

				// System.out.println(Arrays.toString(score));

				this.maxid = maxIdList.get(maxScoreId);

			}
		}

		private void findRange() {

			for (int i = this.maxid - 1; i >= 0; i--) {

				if (minIdSet.contains(i) && this.maxid - i >= 3) {
					begid = i;
					break;
				} else {
					if (allRtList[this.maxid] - allRtList[i] > 1.0 || i == leftZero) {
						begid = i;
						break;
					}
				}
			}

			if (begid == -1)
				begid = 0;

			for (int i = begid; i < this.intenlist.length; i++) {
				if (intenlist[i] == 0) {
					begid = i + 1;
				} else {
					break;
				}
			}

			for (int i = this.maxid + 1; i < intenlist.length; i++) {

				if (minIdSet.contains(i) && i - maxid >= 3) {
					endid = i;
					break;
				} else {
					if (allRtList[i] - allRtList[this.maxid] > 1.0 || i == rightZero) {
						endid = i;
						break;
					}
				}
			}

			if (endid == -1)
				endid = intenlist.length - 1;

			for (int i = endid; i >= 0; i--) {
				if (intenlist[i] == 0) {
					endid = i - 1;
				} else {
					break;
				}
			}

			for (int i = begid; i <= endid; i++) {
				if (intenlist[i] <= intenlist[maxid])
					this.trueIntenList[i] = intenlist[i];
			}
		}

		private void reFindRange(HashSet<Integer> possiMaxId, int allBeginId, int allEndId) {

			double maxInten = 0;
			int reMaxId = 0;

			for (Integer mi : possiMaxId) {
				if (this.intenlist[mi] > maxInten) {
					maxInten = this.intenlist[mi];
					reMaxId = mi;
				}
			}

			this.maxid = reMaxId;

			for (int i = this.maxid - 1; i >= 0; i--) {

				if (minIdSet.contains(i) && this.maxid - i >= 3) {
					begid = i;
					break;
				} else {
					if (allRtList[this.maxid] - allRtList[i] > 0.6 || i == leftZero || i <= allBeginId) {
						begid = i;
						break;
					}
				}
			}

			if (begid == -1)
				begid = 0;

			for (int i = begid; i < this.intenlist.length; i++) {
				if (intenlist[i] == 0) {
					begid = i + 1;
				} else {
					break;
				}
			}

			for (int i = this.maxid + 1; i < intenlist.length; i++) {

				if (minIdSet.contains(i) && i - maxid >= 3) {
					endid = i;
					break;
				} else {
					if (allRtList[i] - allRtList[this.maxid] > 0.6 || i == rightZero || i > allEndId) {
						endid = i;
						break;
					}
				}
			}

			if (endid == -1)
				endid = intenlist.length - 1;

			for (int i = endid; i >= 0; i--) {
				if (intenlist[i] == 0) {
					endid = i - 1;
				} else {
					break;
				}
			}

			for (int i = begid; i <= endid; i++) {
				if (intenlist[i] <= intenlist[maxid])
					this.trueIntenList[i] = intenlist[i];
			}
		}

		private void setNewIntenlist(int beginId, int endId) {

			if (this.maxid <= beginId || this.maxid >= endId) {
				this.use = false;
			}

			this.newIntenList = new double[endId - beginId + 1];

			System.arraycopy(trueIntenList, beginId, newIntenList, 0, newIntenList.length);

			this.maxid = maxid - beginId;
		}

		private double getRatio(PixelList p1) {

			if (this.use && p1.use) {

				ArrayList<Double> list0 = new ArrayList<Double>();
				ArrayList<Double> list1 = new ArrayList<Double>();

				list0.add(this.newIntenList[this.maxid]);
				list1.add(p1.newIntenList[p1.maxid]);

				for (int i = 1;; i++) {
					if (this.maxid - i >= 0 && p1.maxid - i >= 0 && this.newIntenList[this.maxid - i] != 0
							&& p1.newIntenList[p1.maxid - i] != 0) {

						list0.add(this.newIntenList[this.maxid - i]);
						list1.add(p1.newIntenList[p1.maxid - i]);

					} else {
						break;
					}
				}
				for (int i = 1;; i++) {
					if (this.maxid + i < this.newIntenList.length && p1.maxid + i < p1.newIntenList.length
							&& this.newIntenList[this.maxid + i] != 0 && p1.newIntenList[p1.maxid + i] != 0) {

						list0.add(this.newIntenList[this.maxid + i]);
						list1.add(p1.newIntenList[p1.maxid + i]);

					} else {
						break;
					}
				}

				// use this will be better
				if ((double) Math.abs(this.maxid - p1.maxid) / (double) list1.size() >= 0.75) {

					if (this.maxscore > p1.maxscore) {

						ArrayList<Double> newlist0 = new ArrayList<Double>();
						ArrayList<Double> newlist1 = new ArrayList<Double>();

						for (int i = 0; i < this.newIntenList.length; i++) {
							if (this.newIntenList[i] > 0 && p1.newIntenList[i] > 0
									&& p1.newIntenList[i] <= p1.newIntenList[this.maxid]) {

								newlist0.add(this.newIntenList[i]);
								newlist1.add(p1.newIntenList[i]);
							}
						}
						if (newlist0.size() >= 5)
							return MathTool.getTotal(newlist1) / MathTool.getTotal(newlist0);
						else
							return 0;

					} else if (this.maxscore < p1.maxscore) {

						ArrayList<Double> newlist0 = new ArrayList<Double>();
						ArrayList<Double> newlist1 = new ArrayList<Double>();

						for (int i = 0; i < this.newIntenList.length; i++) {
							if (this.newIntenList[i] > 0 && p1.newIntenList[i] > 0
									&& this.newIntenList[i] <= this.newIntenList[p1.maxid]) {

								newlist0.add(this.newIntenList[i]);
								newlist1.add(p1.newIntenList[i]);
							}
						}

						if (newlist0.size() >= 5)
							return MathTool.getTotal(newlist1) / MathTool.getTotal(newlist0);
						else
							return 0;

					} else {
						return 0;
					}

					// return 0;

				}

				if (MathTool.getTotal(list0) == 0 || MathTool.getTotal(list1) == 0) {
					return 0;
				}
				return MathTool.getTotal(list1) / MathTool.getTotal(list0);

			} else {

				return 0;
			}
		}

	}

}
