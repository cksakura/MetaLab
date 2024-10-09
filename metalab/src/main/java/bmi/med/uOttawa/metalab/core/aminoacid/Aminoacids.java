/**
 * 
 */
package bmi.med.uOttawa.metalab.core.aminoacid;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;


/**
 * @author Kai Cheng
 *
 */
public class Aminoacids {

	private final static DecimalFormat DA = new DecimalFormat("+0.#### Da;-0.#### Da");

	/*
	 * All the legal aminoacids
	 */
	private static final Aminoacid AMINOACIDS[] = new Aminoacid[128];

	/**
	 * pk value table for amino acids at different conditions. # Bjellqvist,
	 * B.,Hughes, G.J., Pasquali, Ch., Paquet, N., Ravier, F., Sanchez, J.-Ch.,
	 * Frutiger, S. & Hochstrasser, D.F. The focusing positions of polypeptides
	 * in immobilized pH gradients can be predicted from their amino acid
	 * sequences. Electrophoresis 1993, 14, 1023-1031.
	 * 
	 * MEDLINE: 8125050
	 * 
	 * <p>
	 * <p>
	 * Ct Nt Sm Sc Sn
	 */
	public static final double[][] AAPK = new double[][] {
	/* A */{ 3.55, 7.59, 0.0, 0.0, 0.0 },
	/* B */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* C */{ 3.55, 7.50, 9.00, 9.00, 9.00 },
	/* D */{ 4.55, 7.50, 4.05, 4.05, 4.05 },
	/* E */{ 4.75, 7.70, 4.45, 4.45, 4.45 },
	/* F */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* G */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* H */{ 3.55, 7.50, 5.98, 5.98, 5.98 },
	/* I */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* J */{ 0.00, 0.00, 0.0, 0.0, 0.0 },
	/* K */{ 3.55, 7.50, 10.00, 10.00, 10.00 },
	/* L */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* M */{ 3.55, 7.00, 0.0, 0.0, 0.0 },
	/* N */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* O */{ 0.00, 0.00, 0.0, 0.0, 0.0 },
	/* P */{ 3.55, 8.36, 0.0, 0.0, 0.0 },
	/* Q */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* R */{ 3.55, 7.50, 12.0, 12.0, 12.0 },
	/* S */{ 3.55, 6.93, 0.0, 0.0, 0.0 },
	/* T */{ 3.55, 6.82, 0.0, 0.0, 0.0 },
	/* U */{ 0.00, 0.00, 0.0, 0.0, 0.0 },
	/* V */{ 3.55, 7.44, 0.0, 0.0, 0.0 },
	/* W */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* X */{ 3.55, 7.50, 0.0, 0.0, 0.0 },
	/* Y */{ 3.55, 7.50, 10.00, 10.00, 10.00 },
	/* Z */{ 3.55, 7.50, 0.0, 0.0, 0.0 } };

	/**
	 * Amino Acid Hydropathy Scores. These scores are based on 
	 * the values given by the original Kyte-Doolittle paper, 
	 * "Kyte, J. and Doolittle, R. 1982. A simple method for displaying 
	 * the hydropathic character of a protein. J. Mol. Biol. 157: 105-132."
	 * 
	 * http://gcat.davidson.edu/rakarnik/aminoacidscores.htm
	 */
	public static final double[] HydroScore = new double[] {
		/* A */ 1.8,
		/* B */ 0,
		/* C */ 2.5,
		/* D */ -3.5,
		/* E */ -3.5,
		/* F */ 2.8,
		/* G */ -0.4,
		/* H */ -3.2,
		/* I */ 4.5,
		/* J */ 0,
		/* K */ -3.9,
		/* L */ 3.8,
		/* M */ 1.9,
		/* N */ -3.5,
		/* O */ 0,
		/* P */ -1.6,
		/* Q */ -3.5,
		/* R */ -4.5,
		/* S */ -0.8,
		/* T */ -0.7,
		/* U */ 0,
		/* V */ 4.2,
		/* W */ -0.9,
		/* X */ 0,
		/* Y */ -1.3,
		/* Z */ 0
	};
	
	static {
		// Initial 20 aminoacids
		AMINOACIDS[71] = new Aminoacid('G', "Gly", 57.02146000000005D, 57.05135999999999D, -0.40000000000000002D,
				AAPK['G' - 'A'], "glycine", "C2H3NO", true);
		AMINOACIDS[65] = new Aminoacid('A', "Ala", 71.03710000000007D, 71.07793999999996D, 1.8D, AAPK[0], "alanine",
				"C3H5NO", true);
		AMINOACIDS[83] = new Aminoacid('S', "Ser", 87.03200000000004D, 87.07733999999994D, -0.80000000000000004D,
				AAPK['S' - 'A'], "serine", "C3H5NO2", true);
		AMINOACIDS[80] = new Aminoacid('P', "Pro", 97.05274000000003D, 97.11522000000002D, -1.6000000000000001D,
				AAPK['P' - 'A'], "proline", "C5H7NO", true);
		AMINOACIDS[86] = new Aminoacid('V', "Val", 99.06838000000005D, 99.1311D, 4.2000000000000002D, AAPK['V' - 'A'],
				"valine", "C5H9NO", true);
		AMINOACIDS[84] = new Aminoacid('T', "Thr", 101.04764D, 101.10392000000002D, -0.69999999999999996D,
				AAPK['T' - 'A'], "threonine", "C4H7NO2", true);
		AMINOACIDS[67] = new Aminoacid('C', "Cys", 103.00920000000008D, 103.14393999999993D, 2.5D, AAPK['C' - 'A'],
				"cysteine", "C3H5NOS", true);
		AMINOACIDS[73] = new Aminoacid('I', "Ile", 113.08402000000007D, 113.15767999999997D, 4.5D, AAPK['I' - 'A'],
				"isoleucine", "C6H11NO", true);
		AMINOACIDS[76] = new Aminoacid('L', "Leu", 113.08402000000007D, 113.15767999999997D, 3.7999999999999998D,
				AAPK['L' - 'A'], "leucine", "C6H11NO", true);
		AMINOACIDS[78] = new Aminoacid('N', "Asn", 114.04292000000004D, 114.10272000000009D, -3.5D, AAPK['N' - 'A'],
				"asparagine", "C4H6N2O2", true);
		AMINOACIDS[68] = new Aminoacid('D', "Asp", 115.02690000000007D, 115.08744000000002D, -3.5D, AAPK['D' - 'A'],
				"aspartic_acid", "C4H5NO3", true);
		AMINOACIDS[81] = new Aminoacid('Q', "Gln", 128.05856000000006D, 128.12930000000006D, -3.5D, AAPK['Q' - 'A'],
				"glutamine", "C5H8N2O2", true);
		AMINOACIDS[75] = new Aminoacid('K', "Lys", 128.09494000000007D, 128.17236000000003D, -3.8999999999999999D,
				AAPK['K' - 'A'], "lysine", "C6H12N2O", true);
		AMINOACIDS[69] = new Aminoacid('E', "Glu", 129.04254000000003D, 129.11402000000004D, -3.5D, AAPK['E' - 'A'],
				"glutamic_acid", "C5H7NO3", true);
		AMINOACIDS[77] = new Aminoacid('M', "Met", 131.04048000000006D, 131.19709999999998D, 1.8999999999999999D,
				AAPK['M' - 'A'], "methionine", "C5H9NOS", true);
		AMINOACIDS[72] = new Aminoacid('H', "His", 137.05894000000006D, 137.13940000000002D, -3.2000000000000002D,
				AAPK['H' - 'A'], "histidine", "C6H7N3O", true);
		AMINOACIDS[70] = new Aminoacid('F', "Phe", 147.06838000000005D, 147.1739D, 2.7999999999999998D, AAPK['F' - 'A'],
				"phenylalanine", "C9H9NO", true);
		AMINOACIDS[82] = new Aminoacid('R', "Arg", 156.10114000000004D, 156.18583999999998D, -4.5D, AAPK['R' - 'A'],
				"arginine", "C6H12N4O", true);
		AMINOACIDS[89] = new Aminoacid('Y', "Tyr", 163.06328000000002D, 163.17330000000004D, -1.3D, AAPK['Y' - 'A'],
				"tyrosine", "C9H9NO2", true);
		AMINOACIDS[87] = new Aminoacid('W', "Trp", 186.0793D, 186.20997999999997D, -0.90000000000000002D,
				AAPK['W' - 'A'], "tryptophan", "C11H10N2O", true);
		/*
		 * AMINOACIDS[79] = new Aminoacid('O', "Orn", 114.079313D, 114.146240D, 0.0D,
		 * AAPK['O' - 'A'], "Ornithine C5H12N2O2", true);
		 */
		AMINOACIDS[79] = new Aminoacid('O', "Orn", 12.0000D, 12.0107D, 0.0D, AAPK['O' - 'A'], "Ornithine", "C5H12N2O2",
				true);

		// Z was the average of glu and gln
		AMINOACIDS[90] = new Aminoacid('Z', "Glx", (AMINOACIDS[69].getMonoMass() + AMINOACIDS[81].getMonoMass()) / 2D,
				(AMINOACIDS[69].getAverageMass() + AMINOACIDS[81].getAverageMass()) / 2D,
				(AMINOACIDS[69].getHydropathicity() + AMINOACIDS[81].getHydropathicity()) / 2D, AAPK['Z' - 'A'],
				"Glutamine or Glutamic acid", "", true);

		// B is the average of asp and asn
		AMINOACIDS[66] = new Aminoacid('B', "Asx", (AMINOACIDS[78].getMonoMass() + AMINOACIDS[68].getMonoMass()) / 2D,
				(AMINOACIDS[78].getAverageMass() + AMINOACIDS[68].getAverageMass()) / 2D,
				(AMINOACIDS[78].getHydropathicity() + AMINOACIDS[68].getHydropathicity()) / 2D, AAPK['B' - 'A'],
				"Aspartic acid or Asparagine", "", true);

		AMINOACIDS[74] = new Aminoacid('J', "Jaa", 12.0D, 12.0107D, 0.0D, AAPK['J' - 'A'], "User defined amino J", "",
				true);

		AMINOACIDS[85] = new Aminoacid('U', "Uaa", 12.0D, 12.0107D, 0.0D, AAPK['U' - 'A'], "User defined amino U", "",
				true);

		// Intial Xaa
		double totalMonoMass = 0.0D;
		double totalAverageMass = 0.0D;
		double totalAverageHydropathicity = 0.0D;
		int icount = 0;
		for (int i = 0; i < 128; i++) {
			Aminoacid taa = AMINOACIDS[i];
			if (taa != null && i != 88) {
				icount++;
				totalMonoMass += taa.getMonoMass();
				totalAverageMass += taa.getAverageMass();
				totalAverageHydropathicity += taa.getHydropathicity();
			}
		}

		double averageMonoMass = totalMonoMass / icount;
		double averageAverageMass = totalAverageMass / icount;
		double averageHydrophthicity = totalAverageHydropathicity / icount;
		AMINOACIDS[88] = new Aminoacid('X', "Xaa", averageMonoMass, averageAverageMass, averageHydrophthicity,
				AAPK['X' - 'A'], "Any amino acid", "", true);
	}

	/**
	 * Test whether this character is a legal aminoacid. 
	 * 
	 * @param c
	 * @return true if this is an aminoacid.
	 */
	public static final boolean isAminoacid(char c) {
		if (getAminoacid(c) == null)
			return false;
		return true;
	}

	/**
	 * Get the Aminoacid for the specific character. If this char is not an
	 * aminoacid, null will be returned. 
	 * 
	 * @param char
	 * @return Aminoacid.
	 */
	public static final Aminoacid getAminoacid(char c) {
		int index = c;
		if (index < 0 || index >= 128)
			return null;
		return AMINOACIDS[index];
	}
	
	public Aminoacid getAAInstance(char c) {
		int index = c;
		if (index < 0 || index >= 128)
			return null;
		return aas[index];
	}

	/**
	 * Get an instance of Aminoacids. This method is identical to the
	 * constructor of Aminoacids, but supply a static solution.
	 * 
	 * @return Aminoacids instance.
	 */
	public static final Aminoacids getInstance() {
		return new Aminoacids();
	}

	private Aminoacid[] aas;

	private HashSet<Character> modifiedAAs;

	// The modification of n terminal of peptide
	private double ntmodif = 0d;

	// The modification of c terminal of peptide
	private double ctmodif = 0d;

	/**
	 * Construct a new instance of Aminoacids.
	 */
	public Aminoacids() {
		aas = new Aminoacid[AMINOACIDS.length];
		modifiedAAs = new HashSet<Character>();
		this.reset();
	}

	/**
	 * Reset to the initial value; All the modification informations will be
	 * lost.
	 */
	public void reset() {
		int len = aas.length;
		for (int i = 0; i < len; i++) {
			Aminoacid aa = AMINOACIDS[i];
			if (aa != null)
				aas[i] = aa.clone();
		}
		modifiedAAs = new HashSet<Character>();
		this.ctmodif = 0.0;
		this.ntmodif = 0.0;
	}

	/**
	 * A commonly used modification in proteomic research; Normally cystein is
	 * carboxyamidomethylated by IAA, add the mass to it
	 */
	public void setCysCarboxyamidomethylation() {
		Aminoacid cys = this.aas[67];

		cys.setAverageMass(cys.getAverageMass() + 57.0513D);
		cys.setMonoMass(cys.getMonoMass() + 57.02146D);

		this.modifiedAAs.add(cys.getOneLetter());

		this.reInitXmass();
	}

	// Intial Xaa for the molecular
	private void reInitXmass() {
		double totalMonoMass = 0.0D;
		double totalAverageMass = 0.0D;
		int icount = 0;
		for (int i = 0; i < 128; i++) {
			Aminoacid taa = this.aas[i];
			if (taa != null) {
				icount++;
				totalMonoMass += taa.getMonoMass();
				totalAverageMass += taa.getAverageMass();
			}
		}

		double averageMonoMass = totalMonoMass / icount;
		double averageAverageMass = totalAverageMass / icount;

		aas[88].setAverageMass(averageAverageMass);
		aas[88].setMonoMass(averageMonoMass);
	}

	// B is the average of asp and asn, re- initial for molecular
	private void reInitBmass() {
		AMINOACIDS[66]
		        .setMonoMass((AMINOACIDS[78].getMonoMass() + AMINOACIDS[68]
		                .getMonoMass()) / 2D);
		AMINOACIDS[66]
		        .setAverageMass((AMINOACIDS[78].getAverageMass() + AMINOACIDS[68]
		                .getAverageMass()) / 2D);
	}

	private void reInitZmass() {
		// Z was the average of gly and gln
		AMINOACIDS[90]
		        .setMonoMass((AMINOACIDS[69].getMonoMass() + AMINOACIDS[81]
		                .getMonoMass()) / 2D);
		AMINOACIDS[90]
		        .setAverageMass((AMINOACIDS[69].getAverageMass() + AMINOACIDS[81]
		                .getAverageMass()) / 2D);
	}

	/**
	 * While a amino acid is modified statically, (e.g. cystin modified with
	 * IAA) the mass of this amino acid must be changed.
	 * <p>
	 * <b>Cannot use this method and </b>
	 * {@link #setModifiedMassForAA(char, double)} or
	 * {@link #setModifiedMassForAA(ModSite, double)}.
	 * 
	 * @param aa
	 *            one character name of the amino acid (c or n for c terminal
	 *            and n terminal)
	 * @param add
	 *            the mass add to this amino acid (if minus, the value of add
	 *            should be less than 0)
	 */
	public void setModification(char aa, double add) {
		int index = aa;

		if (aa == 'n') {
			this.setNterminalStaticModif(add);
			return;
		} else if (aa == 'c') {
			this.setCterminalStaticModif(add);
			return;
		}

		Aminoacid aacid = this.get(index);
		if (aacid == null)
			throw new NullPointerException(
			        "\""
			                + aa
			                + "\" is not a legal aminoacid. Please use the upper "
			                + "cased characters (e.g. Y, S but not y and s) for aminoacids");

		aacid.setAverageMass(aacid.getAverageMass() + add);
		aacid.setMonoMass(aacid.getMonoMass() + add);

		this.modifiedAAs.add(aa);

		if (index == 69 || index == 81)// glx
			this.reInitZmass();
		else if (index == 78 || index == 68)
			this.reInitBmass();
	}

	/**
	 * While a amino acid is modified statically, (e.g. cystin modified with
	 * IAA) the mass of this amino acid must be changed. More than one fixed
	 * modifications can be set and the modifications are reflected by the added
	 * mass.
	 * <p>
	 * <b>Cannot use this method and </b>
	 * {@link #setModifiedMassForAA(char, double)} or
	 * {@link #setModifiedMassForAA(ModSite, double)}.
	 * 
	 * <p>
	 * <b>We assume that all the fixed modification should occur at specific
	 * aminoacid or c/n terminus of peptides. Protein terminal modifications and
	 * specific aminoaicd at terminus should be variable modification</b>
	 * 
	 * 
	 * @since 0.4
	 * 
	 * @param aa
	 *            string of aminoacids with one character name (c or n for c
	 *            terminal and n terminal)
	 * @param add
	 *            the mass add to this amino acid (if minus, the value of add
	 *            should be less than 0)
	 */
	/*public void setModification(ModSite site, double add) {

		if (site == null) {
			System.out.println("Null fixed modification site, no modification "
			        + "will be add for mass " + add);
		}

		ModType type = site.getModType();

		if (type.isMust_var_mod()) {
			throw new IllegalArgumentException(
			        "The mofiication type for this site is \"" + type
			                + "\". Cannot used as fix modification.");
		}

		switch (type) {
		case modaa: {
			this.setModification(site.getSymbol().charAt(0), add);
			break;
		}
		case modcp: {
			this.setCterminalStaticModif(add);
			break;
		}
		case modnp: {
			this.setNterminalStaticModif(add);
			break;
		}
		default:
			throw new IllegalArgumentException(
			        "The mofiication type for this site is \"" + type
			                + "\". Cannot used as fix modification.");
		}
	}*/

	/**
	 * While a amino acid is modified statically, (e.g. cystin modified with
	 * IAA) the mass of this amino acid must be changed. Use this method to set
	 * the actual mass for the modified aminoacid.
	 * 
	 * <p>
	 * <b>Cannot use this method and </b>{@link #setModification(char, double)}
	 * or {@link #setModification(ModSite, double)}.
	 * 
	 * @since 0.4
	 * @param aa
	 *            one character name of the amino acid (c or n for c terminal
	 *            and n terminal)
	 * @param add
	 *            the mass add to this amino acid (if minus, the value of add
	 *            should be less than 0)
	 */
	public void setModifiedMassForAA(char aa, double newmass) {
		int index = aa;

		if (aa == 'n') {
			this.setNterminalStaticModif(newmass);
			return;
		} else if (aa == 'c') {
			this.setCterminalStaticModif(newmass);
			return;
		}

		Aminoacid aacid = this.get(index);
		if (aacid == null)
			throw new NullPointerException(
			        "\""
			                + aa
			                + "\" is not a legal aminoacid. Please use the upper "
			                + "cased characters (e.g. Y, S but not y and s) for aminoacids");

		aacid.setAverageMass(newmass);
		aacid.setMonoMass(newmass);

		this.modifiedAAs.add(aa);

		if (index == 69 || index == 81)// glx
			this.reInitZmass();
		else if (index == 78 || index == 68)
			this.reInitBmass();
	}

	/**
	 * While a amino acid is modified statically, (e.g. cystin modified with
	 * IAA) the mass of this amino acid must be changed. Use this method to set
	 * the actual mass for the modified aminoacid.
	 * 
	 * <p>
	 * <b>Cannot use this method and </b>{@link #setModification(char, double)}
	 * or {@link #setModification(ModSite, double)}.
	 * 
	 * <p>
	 * <b>We assume that all the fixed modification should occur at specific
	 * aminoacid or c/n terminus of peptides. Protein terminal modifications and
	 * specific aminoaicd at terminus should be variable modification</b>
	 * 
	 * 
	 * @since 0.4.1
	 * 
	 * @param aa
	 *            string of aminoacids with one character name (c or n for c
	 *            terminal and n terminal)
	 * @param add
	 *            the mass add to this amino acid (if minus, the value of add
	 *            should be less than 0)
	 */
	/*public void setModifiedMassForAA(ModSite site, double newmass) {

		if (site == null) {
			System.out.println("Null fixed modification site, no modification "
			        + "will be set for mass " + newmass);
		}

		ModType type = site.getModType();

		if (type.isMust_var_mod()) {
			throw new IllegalArgumentException(
			        "The mofiication type for this site is \"" + type
			                + "\". Cannot used as fix modification.");
		}

		switch (type) {
		case modaa: {
			this.setModifiedMassForAA(site.getSymbol().charAt(0), newmass);
			break;
		}
		case modcp: {
			this.setCterminalStaticModif(newmass);
			break;
		}
		case modnp: {
			this.setNterminalStaticModif(newmass);
			break;
		}
		default:
			throw new IllegalArgumentException(
			        "The mofiication type for this site is \"" + type
			                + "\". Cannot used as fix modification.");
		}
	}
*/
	/**
	 * @return the modified amioacids (c or n for the c terminal and n terminal)
	 */
	public char[] getModifiedAAs() {
		int size = this.modifiedAAs.size();
		char[] maa = new char[size];
		int i = 0;
		for (Iterator<Character> iterator = this.modifiedAAs.iterator(); iterator
		        .hasNext(); i++) {
			maa[i] = iterator.next().charValue();
		}
		return maa;
	}

	/**
	 * Average molecular weight. This is the raw weight (+1Da will be the MH+)
	 * <p>
	 * <b>Note: please make sure the sequence only contains legal aminoacids all
	 * other characters will be ignored, including the lower case character</b>
	 * 
	 * @param sequence
	 * @param molecular
	 *            weight.
	 */
	public double getAveragePeptideMass(String sequence) {
		return 18.01528D + getAverageResiduesMass(sequence)
		        + this.getNterminalStaticModif()
		        + this.getCterminalStaticModif();
	}

	/**
	 * Peptide weight can be generated by +18Da
	 * <p>
	 * <b>Note: please make sure the sequence only contains legal aminoacids all
	 * other characters will be ignored, including the lower case character</b>
	 * 
	 * @param sequence
	 * @param residue
	 *            molecular weight.
	 */
	public double getAverageResiduesMass(String sequence) {
		double result = 0.0D;
		for (int i = 0, n = sequence.length(); i < n; i++) {
			char c = sequence.charAt(i);
			Aminoacid aa = this.get(c);
			if (aa == null) {
				System.out.println("\"" + aa + "\" in " + sequence
				        + "is not a legal aminoacid; "
				        + "skipped for calcualtion.");
				continue;
			}
			result += aa.getAverageMass();
		}

		return result;
	}

	/**
	 * Get the aminoacid information for this index; The index is the char value
	 * of the aminoacid;
	 * <p>
	 * <b>Note: The lower cased character will not be considered, e.g. 'C' is
	 * cystein while 'c' is null.</b>
	 * 
	 * @param index
	 * @return instance of aminoacid, if it is not an aminoacid, return null.
	 */
	public Aminoacid get(int index) {
		if (index < 0 || index >= 128) {
			System.out.println("The index: " + index
			        + "doesn't point to a legal aminoacid. Return null.");
			return null;
		}

		return this.aas[index];
	}

	/**
	 * Get the aminoacid information for this index; The index is the char value
	 * of the aminoacid;
	 * <p>
	 * <b>Note: The lower cased character will not be considered, e.g. 'C' is
	 * cystein while 'c' is null.</b>
	 * 
	 * @param aaSym
	 *            one character symbol of aminoacid
	 * @return instance of aminoacid; if the aaSym is not a legal aminoacid,
	 *         null will be returned.
	 */
	public Aminoacid get(char aaSym) {
		int idx = aaSym;
		return this.get(idx);
	}

	/**
	 * How many amino acids existing.
	 * 
	 * @return
	 */
	public int length() {
		return this.aas.length;
	}

	/**
	 * Mono isotoptic molecular weight. This is the raw weight (+1Da will be the
	 * MH+)
	 * <p>
	 * <b>Note: please make sure the sequence only contains legal aminoacids all
	 * other characters will be ignored, including the lower case character</b>
	 * 
	 * @param sequence
	 * @param molecular
	 *            weight.
	 */
	public double getMonoPeptideMass(String sequence) {
		return 18.01054D + getMonoResiduesMass(sequence)
		        + this.getNterminalStaticModif()
		        + this.getCterminalStaticModif();
	}

	/**
	 * Peptide weight can be generated by +18Da
	 * <p>
	 * <b>Note: please make sure the sequence only contains legal aminoacids all
	 * other characters will be ignored, including the lower case character</b>
	 * 
	 * @param sequence
	 * @param residue
	 *            molecular weight.
	 */
	public double getMonoResiduesMass(String sequence) {
		double dRes = 0.0D;
		for (int i = 0; i < sequence.length(); i++) {
			char c = sequence.charAt(i);
			Aminoacid aa = this.get(c);
			if (aa == null) {
				System.out.println("\"" + aa + "\" in " + sequence
				        + "is not a legal aminoacid; "
				        + "skipped for calcualtion.");
				continue;
			}
			dRes += aa.getMonoMass();
		}

		return dRes;
	}

	/**
	 * Set static modification for cterm of peptide
	 * 
	 * @param mass
	 */
	public void setCterminalStaticModif(double mass) {
		this.ctmodif += mass;
		this.modifiedAAs.add('c');
	}

	/**
	 * Set static modification for nterm of peptide
	 * 
	 * @param mass
	 */
	public void setNterminalStaticModif(double mass) {
		this.ntmodif += mass;
		this.modifiedAAs.add('n');
	}

	/**
	 * Return the static modification for cterm of peptide
	 */
	public double getCterminalStaticModif() {
		return this.ctmodif;
	}

	/**
	 * Return the static modification for nterm of peptide
	 */
	public double getNterminalStaticModif() {
		return this.ntmodif;
	}

	/**
	 * Get the description of the modified aminoacids with the format of
	 * "C = 156 Da" or "C terminal = -18 Da"
	 * 
	 * @since 0.5.1
	 * @param isMono
	 *            is the search performed using Monoisotope mass
	 * @return
	 */
	public String getModfiedAADescription(boolean isMono) {
		StringBuilder sb = new StringBuilder(30);
		char[] aas = this.getModifiedAAs();
		for (char aa : aas) {
			if (aa == 'n') {
				sb.append("N terminal [").append(DA.format(this.ntmodif))
				        .append(']');
			} else if (aa == 'c') {
				sb.append("C terminal [").append(DA.format(this.ctmodif))
				        .append(']');
			} else {
				Aminoacid aacid = this.get(aa);
				Aminoacid oaacid = getAminoacid(aa);
				double mmass = isMono ? aacid.getMonoMass() : aacid
				        .getAverageMass();
				double omass = isMono ? oaacid.getMonoMass() : oaacid
				        .getAverageMass();

				sb.append(aa).append(" [").append(DA.format(mmass - omass));
				sb.append("] = ").append(DA.format(mmass));
			}

			sb.append("\n");
		}

		return aas.length==0 ? "No modification." : sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < 128; i++) {
			Aminoacid aa = aas[i];
			if (aa != null)
				res.append(aa.toString()).append("\n");
		}

		return res.toString();
	}

	/**
	 * Deep clone
	 */
	public Aminoacids deepClone() {
		Aminoacids copy = null;
		try {
			copy = (Aminoacids) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		Aminoacid[] copyed = copy.aas;

		for (int i = 0; i < copyed.length; i++) {
			Aminoacid aa = copyed[i];
			if (aa != null)
				copyed[i] = aa.clone();
		}

		return copy;
	}
	
	/**
	 * Clone
	 */
	@Override
	public Aminoacids clone() {
		try {
	        return (Aminoacids) super.clone();
        } catch (CloneNotSupportedException e) {
	        throw new RuntimeException(e);
        }
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Aminoacids) {
			Aminoacids aaobj = (Aminoacids) obj;

			if (aaobj.ctmodif != this.ctmodif)
				return false;

			if (aaobj.ntmodif != this.ntmodif)
				return false;

			Aminoacid[] aacids = aaobj.aas;
			for (int i = 0; i < aacids.length; i++) {
				Aminoacid aa2 = aacids[i];
				Aminoacid aa1 = aas[i];

				if (aa1 == null && aa2 == null)
					continue;

				if (aa1 != null && aa2 != null) {
					if (aa1.equals(aa2))
						continue;
				}

				return false;
			}

			return true;
		} else
			return false;
	}

}
