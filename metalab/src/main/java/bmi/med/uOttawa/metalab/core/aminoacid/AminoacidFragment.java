/**
 * 
 */
package bmi.med.uOttawa.metalab.core.aminoacid;

import java.util.HashMap;

import bmi.med.uOttawa.metalab.spectra.Ion;

/**
 * @author Kai Cheng
 *
 */
public class AminoacidFragment {

	private static final double B_ION_ADD_MONO = 1.00782;

	private static final double Y_ION_ADD_MONO = 19.01872;

	private static final double C_ION_ADD_MONO = 18.03438;

	private static final double Z_ION_ADD_MONO = 2.99962;

	private static final double B_ION_ADD_AVG = 1.00794;

	private static final double Y_ION_ADD_AVG = 19.02322;

	private static final double C_ION_ADD_AVG = 18.0385;

	private static final double Z_ION_ADD_AVG = 3.0006;

	private Aminoacids aminoacids = null;

	public AminoacidFragment() {
		this.aminoacids = Aminoacids.getInstance();
	}

	/**
	 * 
	 * @param _aminoacids contains aminoacids informations
	 * @param _aamodif    the infor of modification
	 * 
	 */
	public AminoacidFragment(Aminoacids _aminoacids) {
		this.aminoacids = _aminoacids;
	}

	/**
	 * The static information for this fragment pattern.
	 * 
	 * @return
	 */
	public Aminoacids getStaticInfo() {
		return this.aminoacids;
	}

	/**
	 * Fragment peptide theoretically into ions of specific types.
	 * 
	 * @see Ion
	 * @param sequence raw peptide from sequest output
	 * @param types    the types of ions to be generated
	 * @return ions of specified types
	 */
	public HashMap<Integer, Ion[]> fragment(String sequence, double[] mods, int[] types, boolean isMono) {

		HashMap<Integer, Ion[]> ionMap = new HashMap<Integer, Ion[]>();

		for (int type : types) {

			Ion[] ins = null;

			switch (type) {
			case Ion.TYPE_B:
				ins = this.fragment_b(sequence, mods, isMono);
				break;
			case Ion.TYPE_Y:
				ins = this.fragment_y(sequence, mods, isMono);
				break;
			case Ion.TYPE_C:
				ins = this.fragment_c(sequence, mods, isMono);
				break;
			case Ion.TYPE_Z:
				ins = this.fragment_z(sequence, mods, isMono);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + type);
			}

			ionMap.put(type, ins);
		}

		return ionMap;
	}

	/**
	 * Fragment and get the b theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_b(String sequence, double[] mods, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? B_ION_ADD_MONO : B_ION_ADD_AVG;
		mass += this.aminoacids.getNterminalStaticModif();
		mass += mods[0];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			mass += mods[i + 1];
			ions[i] = new Ion(mass, Ion.TYPE_B, "b", i + 1);
			ions[i].setFragseq(sb.toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the y theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_y(String sequence, double[] mods, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? Y_ION_ADD_MONO : Y_ION_ADD_AVG;
		mass += this.aminoacids.getCterminalStaticModif();
		mass += mods[mods.length - 1];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(sequence.length() - i - 1);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			mass += mods[mods.length - i - 2];
			ions[i] = new Ion(mass, Ion.TYPE_Y, "y", i + 1);
			ions[i].setFragseq(sb.reverse().toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the c theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_c(String sequence, double[] mods, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? C_ION_ADD_MONO : C_ION_ADD_AVG;
		mass += this.aminoacids.getNterminalStaticModif();
		mass += mods[0];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			mass += mods[i + 1];
			ions[i] = new Ion(mass, Ion.TYPE_C, "c", i + 1);
			ions[i].setFragseq(sb.toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the z theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_z(String sequence, double[] mods, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? Z_ION_ADD_MONO : Z_ION_ADD_AVG;
		mass += this.aminoacids.getCterminalStaticModif();
		mass += mods[mods.length - 1];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(sequence.length() - i - 1);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			mass += mods[mods.length - i - 2];
			ions[i] = new Ion(mass, Ion.TYPE_Z, "z", i + 1);
			ions[i].setFragseq(sb.reverse().toString());
		}

		return ions;
	}

	/**
	 * Fragment peptide theoretically into ions of specific types.
	 * 
	 * @see Ion
	 * @param sequence raw peptide from sequest output
	 * @param types    the types of ions to be generated
	 * @return ions of specified types
	 */
	public HashMap<Integer, Ion[]> fragment(String sequence, int[] types, boolean isMono) {

		HashMap<Integer, Ion[]> ionMap = new HashMap<Integer, Ion[]>();

		for (int type : types) {

			Ion[] ins = null;

			switch (type) {
			case Ion.TYPE_B:
				ins = this.fragment_b(sequence, isMono);
				break;
			case Ion.TYPE_Y:
				ins = this.fragment_y(sequence, isMono);
				break;
			case Ion.TYPE_C:
				ins = this.fragment_c(sequence, isMono);
				break;
			case Ion.TYPE_Z:
				ins = this.fragment_z(sequence, isMono);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + type);
			}

			ionMap.put(type, ins);
		}

		return ionMap;
	}

	/**
	 * Fragment and get the b theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_b(String sequence, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? B_ION_ADD_MONO : B_ION_ADD_AVG;
		mass += this.aminoacids.getNterminalStaticModif();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			ions[i] = new Ion(mass, Ion.TYPE_B, "b", i + 1);
			ions[i].setFragseq(sb.toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the y theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_y(String sequence, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? Y_ION_ADD_MONO : Y_ION_ADD_AVG;
		mass += this.aminoacids.getCterminalStaticModif();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(sequence.length() - i - 1);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			ions[i] = new Ion(mass, Ion.TYPE_Y, "y", i + 1);
			ions[i].setFragseq(sb.reverse().toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the c theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_c(String sequence, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? C_ION_ADD_MONO : C_ION_ADD_AVG;
		mass += this.aminoacids.getNterminalStaticModif();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(i);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			ions[i] = new Ion(mass, Ion.TYPE_C, "c", i + 1);
			ions[i].setFragseq(sb.toString());
		}

		return ions;
	}

	/**
	 * Fragment and get the z theoretical ion
	 * 
	 * @param mseq
	 * @return
	 */
	public Ion[] fragment_z(String sequence, boolean isMono) {

		Ion[] ions = new Ion[sequence.length() - 1];
		double mass = isMono ? Z_ION_ADD_MONO : Z_ION_ADD_AVG;
		mass += this.aminoacids.getCterminalStaticModif();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sequence.length() - 1; i++) {
			char aa = sequence.charAt(sequence.length() - i - 1);
			Aminoacid aac = this.aminoacids.get(aa);
			sb.append(aac.getOneLetter());
			mass += isMono ? aac.getMonoMass() : aac.getAverageMass();
			ions[i] = new Ion(mass, Ion.TYPE_Z, "z", i + 1);
			ions[i].setFragseq(sb.reverse().toString());
		}

		return ions;
	}

}
