/**
 * 
 */
package bmi.med.uOttawa.metalab.core.tools;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.aminoacid.Aminoacid;
import bmi.med.uOttawa.metalab.core.aminoacid.Aminoacids;

/**
 * @author Kai Cheng
 *
 */
public class MwCalculator {

	/**
	 * The Mono mass (H3O+)
	 */
	private static final double TERMINAL_MASS_MONO = AminoAcidProperty.PROTON_W + AminoAcidProperty.MONOW_H * 2
			+ AminoAcidProperty.MONOW_O;

	/**
	 * The average mass (H3O+)
	 */
	private static final double TERMINAL_MASS_AVG = AminoAcidProperty.PROTON_W + AminoAcidProperty.AVERAGEW_H * 2
			+ AminoAcidProperty.AVERAGEW_O;

	private Aminoacids aacids;

	/**
	 * Construct a calculator using default parameter. No modification will be
	 * included while calculating of molecular weight. To set modifications please
	 * us MwCalculator(Aminoacids, AminoacidModification)
	 */

	public MwCalculator() {
		this.aacids = new Aminoacids();
	}

	/**
	 * @param aacids the aacids to set
	 */
	public final void setAacids(Aminoacids aacids) {
		this.aacids = aacids;
	}

	/**
	 * Calculate the monoisotope MH+ for the specific peptide sequence. Any
	 * modifications (static or variable) can be included when the related
	 * Aminoacids and AminoacidModification instance are transfered into the
	 * calculator through setAacids() and setAamodif();
	 * 
	 * @param sequence can include variable modification symbols such as #* and so
	 *                 on, but the added mass for these symbols must be defined in
	 *                 aamodifcation. Otherwise, the masses added by these variable
	 *                 modification will not be included.
	 * @return the mono isotope mass
	 */
	public final double getMonoIsotopeMh(String sequence) {
		if (sequence == null || sequence.length() == 0)
			return 0d;

		// 19.01836d
		double mw = TERMINAL_MASS_MONO + aacids.getNterminalStaticModif() + aacids.getCterminalStaticModif();
		for (int i = 0, n = sequence.length(); i < n; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aacid = this.aacids.get(aa);
			if (aacid != null)
				mw += aacid.getMonoMass();

		}

		return mw;
	}

	public final double getMonoIsotopeMZ(String sequence) {
		if (sequence == null || sequence.length() == 0)
			return 0d;

		// 19.01836d
		double mw = TERMINAL_MASS_MONO + aacids.getNterminalStaticModif() + aacids.getCterminalStaticModif()
				- AminoAcidProperty.PROTON_W;
		for (int i = 0, n = sequence.length(); i < n; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aacid = this.aacids.get(aa);
			if (aacid != null)
				mw += aacid.getMonoMass();

		}

		return mw;
	}

	/**
	 * Calculate the monoisotope mass for the specific peptide sequence. Any
	 * modifications (static or variable) can be included when the related
	 * Aminoacids and AminoacidModification instance are transfered into the
	 * calculator through setAacids() and setAamodif();
	 * 
	 * @param sequence can include variable modification symbols such as #* and so
	 *                 on, but the added mass for these symbols must be defined in
	 *                 aamodifcation. Otherwise, the masses added by these variable
	 *                 modification will not be included.
	 * @return the mono isotope mass
	 */
	public final double getAverageMh(String sequence) {
		if (sequence == null || sequence.length() == 0)
			return 0d;

		// 19.02322d
		double mw = TERMINAL_MASS_AVG + aacids.getNterminalStaticModif() + aacids.getCterminalStaticModif();
		for (int i = 0, n = sequence.length(); i < n; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aacid = this.aacids.get(aa);
			if (aacid != null)
				mw += this.aacids.get(aa).getAverageMass();

		}

		return mw;
	}

	/**
	 * @return the Aminoacids
	 */
	public Aminoacids getAminoacids() {
		return this.aacids;
	}

}
