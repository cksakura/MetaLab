/**
 * 
 */
package bmi.med.uOttawa.metalab.core.aminoacid;

/**
 * @author Kai Cheng
 *
 */
public class AminoAcidProperty {

	/*
	 * name of amino acids
	 */
	public static final String[] AMINOACIDS = new String[] { "Ala(A)", "Asx(B)", "Cys(C)", "Asp(D)", "Glu(E)", "Phe(F)",
			"Gly(G)", "His(H)", "Ile(I)", "Jaa(J)", "Lys(K)", "Leu(L)", "Met(M)", "Asn(N)", "Orn(O)", "Pro(P)",
			"Gln(Q)", "Arg(R)", "Ser(S)", "Thr(T)", "U(Uaa)", "Val(V)", "Trp(W)", "Xaa(X)", "Tyr(Y)", "Glx(Z)" };

	public static final char[] AACHAR = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	/*
	 * weigtht of elements form amino acids
	 */

	public static final double MONOW_C = 12;
	public static final double MONOW_C13 = 13.003354;

	public static final double MONOW_H = 1.007825;
	public static final double MONOW_D = 2.014101;

	public static final double MONOW_N = 14.003074;
	public static final double MONOW_N15 = 15.000108;

	public static final double MONOW_O = 15.99491462;
	public static final double MONOW_O17 = 16.999132;
	public static final double MONOW_O18 = 17.999160;
	
	public static final double MONOW_P = 30.973761998;

	public static final double MONOW_S32 = 31.972071;
	public static final double MONOW_S33 = 32.971458;
	public static final double MONOW_S34 = 33.967867;
	public static final double MONOW_S36 = 35.967081;

	public static final double AVERAGEW_C = 12.0107;
	public static final double AVERAGEW_H = 1.00794;
	public static final double AVERAGEW_N = 14.00674;
	public static final double AVERAGEW_O = 15.9994;
	public static final double AVERAGEW_S = 32.066;

	/**
	 * The proton mass (without electron)
	 */
	public static final double PROTON_W = 1.007276;
	
	/**
	 * Mass difference between isotopic peaks
	 */
	public static final double isotope_deltaM = 1.00286864;

	// fomular of all aminoacid,in order, C,H,N,O,S specifically;
	// B = the oxidation of M;
	// C is the alkylate form and K is the guanidylate form;
	private static final int[] A = new int[] { 3, 5, 1, 1, 0 };
	private static final int[] B = new int[] { 0, 0, 0, 0, 0 };
	private static final int[] C = new int[] { 5, 8, 2, 2, 1 };
	private static final int[] D = new int[] { 4, 5, 1, 3, 0 };
	private static final int[] E = new int[] { 5, 7, 1, 3, 0 };
	private static final int[] F = new int[] { 9, 9, 1, 1, 0 };
	private static final int[] G = new int[] { 2, 3, 1, 1, 0 };
	private static final int[] H = new int[] { 6, 7, 3, 1, 0 };
	private static final int[] I = new int[] { 6, 11, 1, 1, 0 };
	private static final int[] J = new int[] { 0, 0, 0, 0, 0 };
	private static final int[] K = new int[] { 7, 14, 4, 1, 0 };
	private static final int[] L = new int[] { 6, 11, 1, 1, 0 };
	private static final int[] M = new int[] { 5, 9, 1, 1, 1 };
	private static final int[] N = new int[] { 4, 6, 2, 2, 0 };
	private static final int[] O = new int[] { 5, 10, 2, 1, 0 };
	private static final int[] P = new int[] { 5, 7, 1, 1, 0 };
	private static final int[] Q = new int[] { 5, 8, 2, 2, 0 };
	private static final int[] R = new int[] { 6, 12, 4, 1, 0 };
	private static final int[] S = new int[] { 3, 5, 1, 2, 0 };
	private static final int[] T = new int[] { 4, 7, 1, 2, 0 };
	private static final int[] U = new int[] { 0, 0, 0, 0, 0 };
	private static final int[] V = new int[] { 5, 9, 1, 1, 0 };
	private static final int[] W = new int[] { 11, 10, 2, 1, 0 };
	private static final int[] X = new int[] { 0, 0, 0, 0, 0 };
	private static final int[] Y = new int[] { 9, 9, 1, 2, 0 };
	private static final int[] Z = new int[] { 0, 0, 0, 0, 0 };

	// 2D array of amino acid molecular formular,the index of a specific amino
	// acid is its name minus 'A';
	public static final int[][] AMINOACID_FORMULAR = new int[][] { A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R,
			S, T, U, V, W, X, Y, Z };

	/**
	 * GRAVY (Grand Average of Hydropathy) see:
	 * http://us.expasy.org/tools/pscale/Hphob.Doolittle.html
	 * 
	 * Symbol: A-Z
	 */
	public static final double[] AAGRAVY = new double[] { 1.8, -3.5, 2.5, -3.5, -3.5, 2.8, -0.4, -3.2, 4.5, 0, -3.9,
			3.8, 1.9, -3.5, 0, -1.6, -3.5, -4.5, -0.8, -0.7, 0, 4.2, -0.9, 4.15, -1.3, -3.5 };

	public static final double[][] Isotope_Abundance = new double[][] { /* C */ { 0.988944, 0.011056 },
			/* H */ { 0.999844, 0.000155 }, /* O */ { 0.997621, 0.000379, 0.002000 }, /* N */ { 0.996337, 0.003663 },
			/* S */ { 0.950396, 0.007486, 0.0419712, 0.000146 } };

	/**
	 * pk value table for amino acids at different conditions
	 * 
	 * Ct Nt Sm Sc Sn
	 */
	public static final double[][] AAPK = new double[][] { /* A */ { 3.55, 7.59, 0.0, 0.0, 0.0 },
			/* B */ { 3.55, 7.50, 0.0, 0.0, 0.0 }, /* C */ { 3.55, 7.50, 9.00, 9.00, 9.00 },
			/* D */ { 4.55, 7.50, 4.05, 4.05, 4.05 }, /* E */ { 4.75, 7.70, 4.45, 4.45, 4.45 },
			/* F */ { 3.55, 7.50, 0.0, 0.0, 0.0 }, /* G */ { 3.55, 7.50, 0.0, 0.0, 0.0 },
			/* H */ { 3.55, 7.50, 5.98, 5.98, 5.98 }, /* I */ { 3.55, 7.50, 0.0, 0.0, 0.0 },
			/* J */ { 0.00, 0.00, 0.0, 0.0, 0.0 }, /* K */ { 3.55, 7.50, 10.00, 10.00, 10.00 },
			/* L */ { 3.55, 7.50, 0.0, 0.0, 0.0 }, /* M */ { 3.55, 7.00, 0.0, 0.0, 0.0 },
			/* N */ { 3.55, 7.50, 0.0, 0.0, 0.0 }, /* O */ { 0.00, 0.00, 0.0, 0.0, 0.0 },
			/* P */ { 3.55, 8.36, 0.0, 0.0, 0.0 }, /* Q */ { 3.55, 7.50, 0.0, 0.0, 0.0 },
			/* R */ { 3.55, 7.50, 12.0, 12.0, 12.0 }, /* S */ { 3.55, 6.93, 0.0, 0.0, 0.0 },
			/* T */ { 3.55, 6.82, 0.0, 0.0, 0.0 }, /* U */ { 0.00, 0.00, 0.0, 0.0, 0.0 },
			/* V */ { 3.55, 7.44, 0.0, 0.0, 0.0 }, /* W */ { 3.55, 7.50, 0.0, 0.0, 0.0 },
			/* X */ { 3.55, 7.50, 0.0, 0.0, 0.0 }, /* Y */ { 3.55, 7.50, 10.00, 10.00, 10.00 },
			/* Z */ { 3.55, 7.50, 0.0, 0.0, 0.0 } };
			
			
	private static void getPossibleComposition(double mass) {
		System.out.println(mass);
		double delta = mass * 50 * 1E-6;
		for (int i1 = 0;; i1++) {
			double m = i1 * MONOW_C;
			if (m - mass > delta) {
				break;
			} else if (Math.abs(m - mass) < delta) {
				System.out.println("C(" + i1 + ")\t" + Math.abs(m - mass) / mass * 1E6);
			}
			for (int i2 = 0;; i2++) {
				m = i1 * MONOW_C + i2 * MONOW_H;
				if (m - mass > delta) {
					break;
				} else if (Math.abs(m - mass) < delta) {
					System.out.println("C(" + i1 + ")H(" + i2 + ")\t" + Math.abs(m - mass) / mass * 1E6);
				}
				for (int i3 = 0;; i3++) {
					m = i1 * MONOW_C + i2 * MONOW_H + i3 * MONOW_O;
					if (m - mass > delta) {
						break;
					} else if (Math.abs(m - mass) < delta) {
						System.out
								.println("C(" + i1 + ")H(" + i2 + ")O(" + i3 + ")\t" + Math.abs(m - mass) / mass * 1E6);
					}
					for (int i4 = 0; i4 < 5; i4++) {
						m = i1 * MONOW_C + i2 * MONOW_H + i3 * MONOW_O + i4 * MONOW_N;
						if (m - mass > delta) {
							break;
						} else if (Math.abs(m - mass) < delta) {
							System.out.println("C(" + i1 + ")H(" + i2 + ")O(" + i3 + ")N(" + i4 + ")\t"
									+ Math.abs(m - mass) / mass * 1E6);
						}
						for (int i5 = 1;; i5++) {
							m = i1 * MONOW_C + i2 * MONOW_H + i3 * MONOW_O + i4 * MONOW_N + i5 * MONOW_P;
							if (m - mass > delta) {
								break;
							} else if (Math.abs(m - mass) < delta) {
								System.out.println("C(" + i1 + ")H(" + i2 + ")O(" + i3 + ")N(" + i4 + ")P(" + i5 + ")\t"
										+ Math.abs(m - mass) / mass * 1E6);
							}
						}
					}
				}
			}
		}
	}
	
	private static void getDeltaPPM(double ionMz, int[] composition) {
		double mass = MONOW_C * composition[0] + MONOW_H * composition[1] + MONOW_O * composition[2]
				+ MONOW_N * composition[3];
		double ppm = (ionMz - mass)/mass*1E6;
		System.out.println(ppm+"\t"+mass);
	}
	
	private static void getDeltaPPM(double ionMz, double mass) {
		double ppm = (ionMz - PROTON_W - mass) / mass * 1E6;
		System.out.println(ppm);
	}
	
	private static void getDeltaPPM2(double ionMz, double mass) {
		double ppm = (ionMz - mass) / mass * 1E6;
		System.out.println(ppm);
	}
	
	private static void getMass(int[] comp) {
		double mass = MONOW_C * comp[0] + MONOW_H * comp[1] + MONOW_N * comp[2] + MONOW_O * comp[3] + MONOW_P * comp[4];
		double mass2 = AVERAGEW_C * comp[0] + AVERAGEW_H * comp[1] + AVERAGEW_N * comp[2] + AVERAGEW_O * comp[3] + MONOW_P * comp[4];
		System.out.println(mass+"\t"+mass2);
	}
			
	public static void main(String[] args) {

//		AminoAcidProperty.getPossibleComposition(226.10629-AminoAcidProperty.PROTON_W);
//		AminoAcidProperty.getPossibleComposition(358.08881-AminoAcidProperty.PROTON_W);
//		AminoAcidProperty.getPossibleComposition(299.0635);
//		AminoAcidProperty.getPossibleComposition(248.0087);
//		AminoAcidProperty.getDeltaPPM(317.14481, new int[]{13, 20, 7, 2});
//		AminoAcidProperty.getDeltaPPM(204.08588, new int[]{8, 13, 5, 1});
//		AminoAcidProperty.getDeltaPPM(274.08984, new int[]{11, 15, 7, 1});
//		AminoAcidProperty.getDeltaPPM(1706.7155-1546.6384, new int[]{7, 12, 4, 0});
//		AminoAcidProperty.getDeltaPPM2(160.0759809225, 1706.7155-1546.6384);
//		AminoAcidProperty.getDeltaPPM2(160.0759809225, 160.0735589);
//		AminoAcidProperty.getDeltaPPM2(1411.5533-1265.5009, 146.0579088);
//		AminoAcidProperty.getDeltaPPM(274.0910987773355, Glycosyl.NeuAc_H2O.getMonoMass());
//		AminoAcidProperty.getDeltaPPM(292.10199103619544, Glycosyl.NeuAc.getMonoMass());
//		AminoAcidProperty.getDeltaPPM(317.1328925307916, 316.127053);
//		AminoAcidProperty.getDeltaPPM(317.145168195452, 316.127053);
//		AminoAcidProperty.getDeltaPPM2(243.02544-AminoAcidProperty.PROTON_W, 242.0191538);
//		AminoAcidProperty.getMass(new int[]{4, 10, 1, 5, 1});
//		AminoAcidProperty.getMass(new int[]{8, 16, 2, 8, 1});
//		AminoAcidProperty.getMass(new int[]{3, 5, 1, 2, 0});
//		AminoAcidProperty.getMass(new int[]{6, 12, 4, 1, 0});
		AminoAcidProperty.getMass(new int[]{11, 17, 1, 8, 0});
//		AminoAcidProperty.getMass(new int[]{8, 14, 1, 9, 1});
		
//		AminoAcidProperty.getMass(new int[]{8, 13, 1, 5, 0});
//		Aminoacids aas = new Aminoacids();
//		System.out.println(aas.get('K').getMonoMass()+aas.get('E').getMonoMass());
//		AminoAcidProperty.getPossibleComposition(226.10631-AminoAcidProperty.PROTON_W-Glycosyl.Fuc.getMonoMass());
//		AminoAcidProperty.getPossibleComposition(301.9893);
	}

}
