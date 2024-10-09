/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;

/**
 * @author Kai Cheng
 *
 */
public class PixelList {

	private HashMap<Integer, Pixel> pixMap;
	private double mz;
	private double mr;
	private int charge;

	public PixelList() {
		this.pixMap = new HashMap<Integer, Pixel>();
	}

	public PixelList(Pixel pix) {
		this.pixMap = new HashMap<Integer, Pixel>();
		this.pixMap.put(pix.getScanNum(), pix);

		this.mz = pix.getMz();
		this.charge = pix.getCharge();
		this.mr = (mz - AminoAcidProperty.PROTON_W) * charge;
	}

	public void addPixel(Pixel pix) {

		this.pixMap.put(pix.getScanNum(), pix);
	}

	public int getLength() {
		return pixMap.size();
	}

	public double getMz() {
		return mz;
	}

	public double getMr() {
		return mr;
	}

	public double getAveMz() {

		ArrayList<Double> mzlist = new ArrayList<Double>();
		Iterator<Integer> it = this.pixMap.keySet().iterator();
		while (it.hasNext()) {
			Integer scannum = it.next();
			mzlist.add(pixMap.get(scannum).getMz());
		}

		return MathTool.getAve(mzlist);
	}

	public double getInten() {

		double totalInten = 0;
		Iterator<Integer> it = this.pixMap.keySet().iterator();
		while (it.hasNext()) {
			Integer scannum = it.next();
			totalInten += pixMap.get(scannum).getInten();
		}

		return totalInten;
	}

	/*
	 * public double getCutoffInten(){
	 * 
	 * double cutoff = 0.3;
	 * 
	 * double totalInten = 0; double maxInten = 0; Iterator <Integer> it =
	 * this.pixMap.keySet().iterator(); while(it.hasNext()){ Integer scannum =
	 * it.next(); double inten = pixMap.get(scannum).getInten(); if(inten>maxInten){
	 * maxInten = inten; } }
	 * 
	 * Iterator <Integer> it2 = this.pixMap.keySet().iterator();
	 * while(it2.hasNext()){ Integer scannum = it2.next(); double inten =
	 * pixMap.get(scannum).getInten(); if(inten>maxInten*cutoff){ totalInten +=
	 * inten; } }
	 * 
	 * return totalInten; }
	 */
	public double intenCompare(PixelList list0) {

		HashSet<Integer> set = new HashSet<Integer>();
		set.addAll(this.pixMap.keySet());
		set.addAll(list0.pixMap.keySet());

		ArrayList<Double> list = new ArrayList<Double>();

		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {

			Integer scannum = it.next();
			Pixel p0, p1;
			if ((p0 = this.pixMap.get(scannum)) != null && (p1 = list0.pixMap.get(scannum)) != null) {

				double inten0 = p0.getInten();
				double inten1 = p1.getInten();

				if (inten0 != 0 && inten1 != 0) {
					double intenR = inten0 / inten1;
					list.add(intenR);
				}
			}
		}

		double ratio = MathTool.getAve(list);
		return ratio;
	}

	public String getListString() {

		StringBuilder sb = new StringBuilder();
		Integer[] scans = this.pixMap.keySet().toArray(new Integer[pixMap.size()]);
		Arrays.sort(scans);
		for (int i = 0; i < scans.length; i++) {
			sb.append(pixMap.get(scans[i])).append("\n");
		}

		return sb.toString();
	}

}
