/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;

/**
 * @author Kai Cheng
 *
 */
public class Ion {

	/**
	 * A b ion
	 */
	public static final int TYPE_B = 0;

	/**
	 * A y ion
	 */
	public static final int TYPE_Y = 1;

	/**
	 * A c ion
	 */
	public static final int TYPE_C = 2;

	/**
	 * A z ion
	 */
	public static final int TYPE_Z = 3;

	/**
	 * A b ion with neutral loss, such as in phosphorylated peptides. Symbol of b*
	 */
	public static final int TYPE_B_NEU = 4;

	/**
	 * A y ion with neutral loss, such as in phosphorylated peptides. Symbol of y*
	 */
	public static final int TYPE_Y_NEU = 5;

	/**
	 * A c ion with neutral loss, such as in phosphorylated peptides. Symbol of c*
	 */
	public static final int TYPE_C_NEU = 6;

	/**
	 * A z ion with neutral loss, such as in phosphorylated peptides. Symbol of z*
	 */
	public static final int TYPE_Z_NEU = 7;

	/**
	 * A ion after neutral loss. The symbol depends on the setting
	 */
	public static final int TYPE_NEU = 8;

	/**
	 * Other type ions
	 */
	public static final int TYPE_OTHER = 9;

	// mass of 1+
	private double mz_charge_1;
	// b y or others
	private int type;

	// The symbol
	private String symbol;

	// what count is this ion lay, e,g, the value of b-12 ion is 12
	private int series;

	private String fragseq;

	private static double h2o = AminoAcidProperty.MONOW_H * 2 + AminoAcidProperty.MONOW_O;

	/**
	 * This constructor prefer the by cz or neutral lost by and cz ions. For neutral
	 * loss ions and other ions you want to set the symbol for the ion, use
	 * {@link #Ion(double, int, String, int)}
	 * 
	 * @param mz_charge_1 mass of this ion (1+)
	 * @param type        type
	 * @param series      the series number, e.g. b series ion of b-7, this value
	 *                    should be 7. if the series is not used for this ion (e.g.
	 *                    MH-H2O), then assign this value as 0.
	 */
	public Ion(double mz_charge_1, int type, int series) {
		this(mz_charge_1, type, null, series);
	}

	/**
	 * @param mass1  mass of this ion (1+)
	 * @param type   type
	 * @param symbol e.g. "b" "y". if more than one char, preferred to be
	 *               "[stirng]", e,g, [b-H3PO4] or may add other symbols, e.g. b*
	 *               and so on.
	 * @param series the series number, e.g. b series ion of b-7, this value should
	 *               be 7. if the series is not used for this ion (e.g. MH-H2O),
	 *               then assign this value as 0.
	 */
	public Ion(double mz_charge_1, int type, String symbol, int series) {
		this.mz_charge_1 = mz_charge_1;
		this.type = type;
		this.series = series;

		// Only set the null symbol automatically
		if (symbol == null) {
			switch (type) {
			case TYPE_B:
				this.symbol = "b";
				break;
			case TYPE_Y:
				this.symbol = "y";
				break;
			case TYPE_B_NEU:
				this.symbol = "b*";
				break;
			case TYPE_Y_NEU:
				this.symbol = "y*";
				break;
			case TYPE_C:
				this.symbol = "c";
				break;
			case TYPE_Z:
				this.symbol = "z";
				break;
			case TYPE_C_NEU:
				this.symbol = "c*";
				break;
			case TYPE_Z_NEU:
				this.symbol = "z*";
				break;
			case TYPE_NEU:
				this.symbol = "n";
				break;
			default:
				this.symbol = "u";
			}
		} else
			this.symbol = symbol;
	}

	/**
	 * @see static final field
	 * 
	 * @return ion type
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * @return ion symbol, e.g. "b" "y" for b y ions and "b*" "y*" for b y ions with
	 *         neutral loss.if more than one char, preferred to be "[string]", e,g,
	 *         [b-H3PO4] or may add other symbols, e.g. b* and so on.
	 */
	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * 
	 * From 1 - n. If the series is 0, this may be not a b or y type ion (may be
	 * neutral loss ion)
	 * 
	 * @return what count is this ion lay, e,g, the value of b-12 ion is 12
	 */
	public int getSeries() {
		return this.series;
	}

	/**
	 * @return the mass/z of this ion with charge state 1+
	 */
	public double getMz() {
		return this.getMzVsCharge(1);
	}

	/**
	 * Get the M/Z value of this ion with the specific charge state. If the charge
	 * state less than 1, return 0.
	 * 
	 * @param charge
	 * @return the M/Z value;
	 */
	public double getMzVsCharge(int charge) {

		if (charge < 1)
			return 0;

		if (charge == 1)
			return this.mz_charge_1;

		int diff = charge - 1;
		return (this.mz_charge_1 + 1.00782f * diff) / charge;
	}

	public double getMzVsChargeLossH2O(int charge) {

		if (charge < 1)
			return 0;

		if (charge == 1)
			return this.mz_charge_1 - h2o;

		int diff = charge - 1;
		return (this.mz_charge_1 + 1.00782f * diff - h2o) / charge;

	}

	/**
	 * Get the name of the ion for the charge state of 1 (or the raw state). The raw
	 * state is for the ion which has no charge correlation, e.g. MH+ (or MH++).
	 * Then the name of this ion will be no related to the charge state (the charge
	 * state will not be added to the type).
	 * 
	 * The name will be returned as (type)(series)(+[*1]). If the series equals to
	 * 0, the series no. will be not added to the end. e.g. series == 3, b3+ (b3+);
	 * series == 0, MH+;
	 */
	public String getName() {
		StringBuilder sb = new StringBuilder(5);
		sb.append(this.getSymbol());
		if (this.series > 0) {
			sb.append(this.series);
		}
		sb.append('+');
		return sb.toString();
	}

	/**
	 * Get the name of the ion for this charge state. The name will be returned as
	 * (type)(series)(+[*charge]). If the series equals to 0, the series no. will be
	 * not added to the end. e.g. series == 3, b3++ (b3+); series == 0, MH+;
	 * 
	 * @param charge
	 */
	public String getName(int charge) {
		StringBuilder sb = new StringBuilder(5);
		sb.append(this.getSymbol());
		if (this.series > 0)
			sb.append(this.series);

		for (int i = 0; i < charge; i++)
			sb.append("+");
		return sb.toString();
	}

	public void setFragseq(String fragseq) {
		this.fragseq = fragseq;
	}

	public String getFragseq() {
		return fragseq;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("\t").append(this.getMz());
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof Ion) {

			Ion ion = (Ion) o;

			int t1 = this.type;
			int t2 = ion.type;
			if (t1 != t2)
				return false;

			int s1 = this.series;
			int s2 = ion.series;
			if (s1 != s2)
				return false;

			double m1 = this.mz_charge_1;
			double m2 = ion.mz_charge_1;
			if (m1 != m2)
				return false;

			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		String s = this.mz_charge_1 + " " + this.series + " " + this.type;
		return s.hashCode();
	}

}
