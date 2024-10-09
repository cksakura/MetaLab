/**
 * 
 */
package bmi.med.uOttawa.metalab.quant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;

/**
 * @author Kai Cheng
 *
 */
public class Pixel implements Comparable<Pixel>, Cloneable {

	private int id;
	private int scanNum;
	private double mz;
	private double rt;
	private double intensity;
	private double relaInten;
	private double mbgInten;
	private int charge;
	private double pepMr;

	private static DecimalFormat df2 = FormatTool.getDF2();
	private static DecimalFormat df4 = FormatTool.getDF4();
	private static DecimalFormat df6 = FormatTool.getDF6();

	private String labelInfo;

	/**
	 * A pixel is one peak in a scan, in 2D profile it can be treated as a pixel.
	 * 
	 * @param id
	 * @param scanNum
	 * @param mz
	 * @param rt
	 * @param intensity
	 */

	public Pixel(Pixel p) {
		this(p.scanNum, p.mz, p.rt, p.intensity);
	}

	public Pixel(int scanNum, double mz) {
		this.scanNum = scanNum;
		this.mz = mz;
	}

	public Pixel(int scanNum, double mz, int charge) {
		this.scanNum = scanNum;
		this.mz = mz;
		this.charge = charge;
	}

	public Pixel(int scanNum, double mz, double intensity) {
		this.scanNum = scanNum;
		this.mz = mz;
		this.intensity = intensity;
	}

	public Pixel(int scanNum, double mz, double rt, double intensity) {
		this(scanNum, mz, intensity);
		this.rt = rt;
	}

	public Pixel(int id, int scanNum, double mz, double rt, double intensity) {
		this(scanNum, mz, rt, intensity);
		this.id = id;
	}

	public Pixel(int id, int scanNum, double mz, double rt, double intensity, double relaInten) {
		this(id, scanNum, mz, rt, intensity);
		this.relaInten = relaInten;
	}

	public Pixel(int id, int scanNum, double mz, double rt, double intensity, double relaInten, double mbgInten) {
		this(id, scanNum, mz, rt, intensity, relaInten);
		this.mbgInten = mbgInten;
	}

	/**
	 * Get pixels from sql table use relative intensity filter
	 * 
	 * @param thresLow
	 * @param thresHigh
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public double[][] getPixel(double thresLow, double thresHigh, Statement statement) throws SQLException {

		ArrayList<Float> rtList = new ArrayList<Float>();
		ArrayList<Float> mzList = new ArrayList<Float>();
		ArrayList<Float> intenList = new ArrayList<Float>();

		String str = "SELECT * FROM Pixel WHERE relaInten<" + thresHigh + "and relaInten>" + thresLow + ";";
		ResultSet rset = statement.executeQuery(str);
		while (rset.next()) {
			rtList.add(rset.getFloat(4));
			mzList.add(rset.getFloat(3));
			intenList.add(rset.getFloat(5));
		}

		double[][] data = new double[2][rtList.size()];

		for (int i = 0; i < rtList.size(); i++) {
			data[0][i] = rtList.get(i);
			data[1][i] = mzList.get(i);
		}

		return data;

	}

	/**
	 * Decide if two pixel belong to the same feature
	 * 
	 * @param Pixel a
	 * @param Pixel b
	 * @return A double arrays contain two floats: first double is value charge and
	 *         second double is delta mass.
	 */
	public static double[] monoValue(Pixel a, Pixel b, double multi) {
		double c = Float.parseFloat(df4.format(a.getMz() - b.getMz()));
		// double d =
		// Float.parseFloat(df4.format(a.getMbgInten()/b.getMbgInten()));
		double d = Float.parseFloat(df4.format(a.getInten() / b.getInten()));
		if (d > multi || d < Float.parseFloat(df2.format(1 / multi)))
			return new double[] { 0.0f, 0.0f };

		for (int i = 1; i < 6; i++) {
			if (Math.abs((double) 1 / i - c) < 0.01) {
				return new double[] { i,
						Float.parseFloat(df6.format(Math.abs(c - Float.parseFloat(df4.format(1 / i))))) };
			}
		}

		return new double[] { 0.0f, 0.0f };
	}

	public boolean relaWith(Pixel pix) {

		double d1 = this.getMz();
		double d2 = pix.getMz();
		if (Float.parseFloat(df6.format(Math.abs(d1 - d2))) < 0.01) {
			return true;
		}
		return false;
	}

	public int getID() {
		return id;
	}

	public int getScanNum() {
		return scanNum;
	}

	public void setScanNum(int scanNum) {
		this.scanNum = scanNum;
	}

	public double getMz() {
		return mz;
	}

	public void setMz(double mz) {
		this.mz = mz;
	}

	public double getRt() {
		return rt;
	}

	public double getInten() {
		return intensity;
	}

	public void setInten(double inten) {
		this.intensity = inten;
	}

	public double getRelaInten() {
		return relaInten;
	}

	public double getMbgInten() {
		return mbgInten;
	}

	public void setRelaInten(double basePeak) {
		this.relaInten = this.intensity / basePeak;
	}

	public void setMbgInten(double mbgInten) {
		this.mbgInten = mbgInten;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public int getCharge() {
		return charge;
	}

	public void setLabelInfo(String labelInfo) {
		this.labelInfo = labelInfo;
	}

	public String getLabelInfo() {
		return labelInfo;
	}

	public void setPepMr(double pepMr) {
		this.pepMr = pepMr;
	}

	public double getPepMr() {
		return this.pepMr;
	}

	@Override
	public Object clone() {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			oos.writeObject(this);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			return (Pixel) ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(scanNum).append("\t");
		sb.append(rt).append("\t");
		sb.append(mz).append("\t");
		sb.append(intensity).append("\t");
		// sb.append(mbgInten).append("\t");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Pixel p1) {
		// TODO Auto-generated method stub
		double mz = this.mz;
		double mz1 = p1.getMz();
		return mz > mz1 ? 1 : -1;
	}

}
