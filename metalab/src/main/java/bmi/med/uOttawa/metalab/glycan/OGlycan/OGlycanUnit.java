package bmi.med.uOttawa.metalab.glycan.OGlycan;

import java.util.HashSet;

public enum OGlycanUnit {

	core1_1(0, "core1_1", "GalNAc", new int[] { 1, 0, 0, 0, 0 }, 203.079373, 0, 0, 0, 0, 0, new double[] { 203.079373 },
			new int[] { 0 }, null),

	core1_2(1, "core1_2", "Gal-GalNAc", new int[] { 1, 1, 0, 0, 0 }, 365.132198, 0, 1, 0, 0, 0,
			new double[] { 203.079373, 365.132198 }, new int[] { 0, 1 }, null),

	core1_3(2, "core1_3", "NeuAc-GalNAc", new int[] { 1, 0, 1, 0, 0 }, 494.174789635, 1, 0, 0, 0, 0,
			new double[] { 203.079373, 494.174789635 }, new int[] { 0, 4 }, null),

	// core1_4(3, "core1_4", "NeuAc-Gal-GalNAc", new int[]{1, 1, 1, 0, 0},
	// 656.227614635, 1, 1, 0, 0, 0, new double []{203.079373, 365.132198,
	// 656.227614635},
	// new int[]{0, 1, 8}, null),

	core1_4(3, "core1_4", "NeuAc-Gal-GalNAc", new int[] { 1, 1, 1, 0, 0 }, 656.227614635, 1, 1, 0, 0, 0,
			new double[] { 203.079373, 365.132198, 494.174789635, 656.227614635 }, new int[] { 0, 1, 4, 8 }, null),

	core1_4b(4, "core1_4b", "Gal-(NeuAc-)GalNAc", new int[] { 1, 1, 1, 0, 0 }, 656.227614635, 1, 1, 1, 0, 0,
			new double[] { 203.079373, 365.132198, 494.174789635, 656.227614635 }, new int[] { 0, 1, 4, 9 }, null),

	core1_5(5, "core1_5", "NeuAc-Gal-(NeuAc-)GalNAc", new int[] { 1, 1, 2, 0, 0 }, 947.32303127, 1, 1, 0, 0, 0,
			new double[] { 203.079373, 365.132198, 494.174789635, 656.227614635, 947.32303127 },
			new int[] { 0, 1, 4, 8, 17 }, null),

	core1_5b(6, "core1_5b", "NeuAc-NeuAc-Gal-GalNAc", new int[] { 1, 1, 2, 0, 0 }, 947.32303127, 1, 1, 0, 1, 0,
			new double[] { 203.079373, 365.132198, 656.227614635, 947.32303127 }, new int[] { 0, 1, 8, 18 }, null),

	core1_6(7, "core1_6", "", new int[] { 0, 0, 0, 0, 0 }, 1238.418447905, 1, 1, 0, 0, 0,
			new double[] { 203.079373, 365.132198, 494.174789635, 656.227614635, 947.32303127, 1238.418447905 },
			new int[] { 0, 1, 4, 8, 17, 26 }, null),

	/*
	 * core1_6("core1_6", "", new int[]{0, 0, 0, 0}, 1238.418447905, 1, 1, 0, 0,
	 * 0, new double []{203.079373, 365.132198, 494.174789635, 656.227614635,
	 * 947.32303127, 1238.418447905}, new int[]{0, 1, 3, 5, 11, 16}, null),
	 * 
	 * core1_7("core1_7", "", new int[]{0, 0, 0, 0}, 698.238179214, 0, 1, 0, 0,
	 * 0, new double []{203.079373, 365.132198, 698.238179214}, new int[]{0, 1,
	 * 7}, null),
	 * 
	 * core1_8("core1_8", "", new int[]{0, 0, 0, 0}, 989.333595849, 1, 1, 0, 0,
	 * 0, new double []{203.079373, 365.132198, 494.174789635, 656.227614635,
	 * 698.238179214, 989.333595849}, new int[]{0, 1, 3, 5, 7, 13}, null),
	 * 
	 * core1_9("core1_9", "", new int[]{0, 0, 0, 0}, 1280.4290124842, 1, 1, 0,
	 * 0, 0, new double []{203.079373, 365.132198, 494.174789635, 656.227614635,
	 * 698.238179214, 989.333595849, 1280.4290124842}, new int[]{0, 1, 3, 5, 7,
	 * 13, 17}, null),
	 * 
	 * core1_10("core1_10", "", new int[]{0, 0, 0, 0}, 929.313008, 0, 1, 0, 0,
	 * 0, new double []{203.079373, 365.132198, 929.313008}, new int[]{0, 1,
	 * 10}, null),
	 * 
	 * core1_11("core1_11", "", new int[]{0, 0, 0, 0}, 1220.4084246, 1, 1, 0, 0,
	 * 0, new double []{203.079373, 365.132198, 494.174789635, 656.227614635,
	 * 929.313008, 1220.4084246}, new int[]{0, 1, 3, 5, 10, 15}, null),
	 */

	core2_1(8, "core2_1", "Gal-(GlcNAc-)GalNAc", new int[] { 2, 1, 0, 0, 0 }, 568.211571, 0, 1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571 }, new int[] { 0, 1, 2, 6 },
			new OGlycanUnit[][] { { core1_1, core1_2 } }),

	core2_2(9, "core2_2", "NeuAc-Gal-(GlcNAc-)GalNAc", new int[] { 2, 1, 1, 0, 0 }, 859.306987635, 1, 1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 656.227614635, 859.306987635 },
			new int[] { 0, 1, 2, 6, 8, 14 }, new OGlycanUnit[][] { { core1_1, core1_4 }, { core1_2, core1_3 } }),

	core2_3(10, "core2_3", "Gal-(Gal-GlcNAc-)GalNAc", new int[] { 2, 2, 0, 0, 0 }, 730.264396, 0, 1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 730.264396 }, new int[] { 0, 1, 2, 6, 12 },
			new OGlycanUnit[][] { { core1_2, core1_2 } }),

	core2_4(11, "core2_4", "NeuAc-Gal-(Gal-GlcNAc-)GalNAc", new int[] { 2, 2, 1, 0, 0 }, 1021.359812635, 1, 1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 656.227614635, 730.264396, 859.306987635,
					1021.359812635 },
			new int[] { 0, 1, 2, 6, 8, 12, 14, 21 }, new OGlycanUnit[][] { { core1_2, core1_4 } }),

	core2_5(12, "core2_5", "NeuAc-Gal-(NeuAc-Gal-GlcNAc-)GalNAc", new int[] { 2, 2, 2, 0, 0 }, 1312.45522927, 1, 1, 0,
			0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 656.227614635, 730.264396, 859.306987635,
					1021.359812635, 1312.45522927 },
			new int[] { 0, 1, 2, 6, 8, 12, 14, 21, 28 },
			new OGlycanUnit[][] { { core1_4, core1_4 }, { core1_2, core1_5 } }),

	core2_6(13, "core2_6", "Fuc-Gal-GlcNAc-(NeuAc-Gal-)GalNAc", new int[] { 2, 2, 1, 1, 0 }, 1167.417721635, 1, 1, 0, 0,
			1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 656.227614635, 714.26948, 730.264396,
					859.306987635, 876.322305, 1021.359812635, 1167.417721635 },
			new int[] { 0, 1, 2, 6, 8, 11, 12, 14, 15, 21, 24 }, null),

	core2_7(14, "core2_7", "NeuAc-Gal-(Fuc-)GlcNAc-(Gal-)GalNAc", new int[] { 2, 2, 1, 1, 0 }, 1167.417721635, 1, 1, 0,
			0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 552.216655, 568.211571, 714.26948, 730.264396,
					859.306987635, 876.322305, 1005.364896635, 1021.359812635, 1167.417721635 },
			new int[] { 0, 1, 2, 5, 6, 11, 12, 14, 15, 20, 21, 24 }, null),

	core2_8(15, "core2_8", "NeuAc-Gal-(Fuc-)GlcNAc-(Gal-)GalNAc", new int[] { 2, 2, 1, 1, 0 }, 1167.417721635, 1, 1, 0,
			0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 552.216655, 568.211571, 656.227614635, 714.26948,
					730.264396, 859.306987635, 876.322305, 1021.359812635, 1167.417721635 },
			new int[] { 0, 1, 2, 5, 6, 8, 11, 12, 14, 15, 21, 24 }, null),

	core2_9(16, "core2_9", "NeuAc-Gal-(Fuc-)GlcNAc-(NeuAc-Gal-)GalNAc", new int[] { 2, 2, 2, 1, 0 }, 1458.51313827, 1,
			1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 552.216655, 568.211571, 656.227614635, 714.26948,
					730.264396, 859.306987635, 876.322305, 1021.359812635, 1167.417721635, 1312.45522927,
					1458.51313827 },
			new int[] { 0, 1, 2, 5, 6, 8, 11, 12, 14, 15, 21, 24, 28, 29 }, null),

	core2_10(17, "core2_10", "NeuAc-Gal-(NeuAc-NeuAc-Gal-GlcNAc-)GalNAc", new int[] { 2, 2, 3, 0, 0 }, 1603.550645905,
			1, 1, 0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 568.211571, 656.227614635, 730.264396, 859.306987635,
					1021.359812635, 1150.40240427, 1312.45522927, 1603.550645905 },
			new int[] { 0, 1, 2, 6, 8, 12, 14, 21, 23, 28, 30 }, new OGlycanUnit[][] { { core1_4, core1_5 } }),

	core2_11(18, "core2_11", "NeuAc-Gal-(Gal-(HSO3-)GlcNAc-)GalNAc", new int[] { 2, 2, 1, 0, 1 }, 1102.418382635, 1, 1,
			0, 0, 1,
			new double[] { 203.079373, 365.132198, 406.158746, 487.217316, 568.211571, 649.270141, 656.227614635,
					730.264396, 811.322966, 859.306987635, 1021.359812635, 1102.418382635 },
			new int[] { 0, 1, 2, 3, 6, 7, 8, 12, 13, 14, 21, 22 }, null),;

	private int id;
	private String name;
	private String comp;
	private int[] composition;
	private double mass;
	private int y292;
	private int y366;
	private int y495;
	private int y583;
	private int y407;
	private double[] fragment;
	private int[] fragid;
	private OGlycanUnit[][] split;

	/**
	 * 0-4 5-10 11-16 17-21 23-27 28-30
	 */
	private static String[] fragmentNames = new String[] { "GalNAc", "Gal-GalNAc", "GlcNAc-GalNAc",
			"HSO3-GlcNAc-GalNAc", "NeuAc-GalNAc", "Fuc-GlcNAc-GalNAc", "Gal-(GlcNAc-)GalNAc",
			"Gal-(HSO3-)GlcNAc-GalNAc", "NeuAc-Gal-GalNAc", "Gal-(NeuAc-)GalNAc", "", "Fuc-Gal-GlcNAc-GalNAc",
			"Gal-(Gal-GlcNAc-)GalNAc", "Gal-(HSO3-)GlcNAc-(Gal-)GalNAc", "NeuAc-Gal-(GlcNAc-)GalNAc",
			"Fuc-Gal-GlcNAc-(Gal-)GalNAc", "", "NeuAc-Gal-(NeuAc-)GalNAc", "NeuAc-NeuAc-Gal-GalNAc", "",
			"NeuAc-Gal-(Fuc-)GlcNAc-GalNAc", "NeuAc-Gal-(Gal-GlcNAc-)GalNAc", "Gal-(HSO3-)GlcNAc-(Gal-)GalNAc",
			"NeuAc-NeuAc-Gal-GlcNAc-GalNAc", "Fuc-Gal-GlcNAc-(NeuAc-Gal-)GalNAc", "", "", "",
			"NeuAc-Gal-(NeuAc-Glc-GalNAc-)GalNAc", "NeuAc-Gal-(Fuc-)GlcNAc-(NeuAc-Gal-)GalNAc",
			"NeuAc-NeuAc-Gal-(NeuAc-Gal-GlcNAc-)GalNAc" };

	private static double[] fragmentMasses = new double[] { 203.079373, 365.132198, 406.158746, 487.217316,
			494.174789635, 552.216655, 568.211571, 649.270141, 656.227614635, 656.227614635, 698.238179214, 714.26948,
			730.264396, 811.322966, 859.306987635, 876.322305, 929.313008, 947.32303127, 947.32303127, 989.333595849,
			1005.364896635, 1021.359812635, 1102.418382635, 1150.40240427, 1167.417721635, 1220.4084246, 1238.418447905,
			1280.4290124842, 1312.45522927, 1458.51313827, 1603.550645905 };

	OGlycanUnit(int id, String name, String comp, int[] composition, double mass, int y292, int y366, int y495,
			int y583, int y407, double[] fragment, int[] fragid, OGlycanUnit[][] split) {
		this.id = id;
		this.name = name;
		this.comp = comp;
		this.composition = composition;
		this.mass = mass;
		this.y292 = y292;
		this.y366 = y366;
		this.y495 = y495;
		this.y583 = y583;
		this.y407 = y407;
		this.fragment = fragment;
		this.fragid = fragid;
		this.split = split;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getComposition() {
		return comp;
	}

	public int[] getCompCount() {
		return composition;
	}

	public double getMass() {
		return mass;
	}

	public double[] getFragment() {
		return fragment;
	}

	public int[] getFragid() {
		return fragid;
	}

	public static String[] getTotalFragmentNames() {
		return fragmentNames;
	}

	public static double[] getTotalFragmentMasses() {
		return fragmentMasses;
	}

	public HashSet<String> getFragmentNameSet() {
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < this.fragid.length; i++) {
			set.add(fragmentNames[fragid[i]]);
		}
		return set;
	}

	public HashSet<Integer> getFragmentIdSet() {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < this.fragid.length; i++) {
			set.add(fragid[i]);
		}
		return set;
	}

	public OGlycanUnit[][] getSplitUnits() {
		return split;
	}

	public int y292() {
		return y292;
	}

	public int y366() {
		return y366;
	}

	public int y495() {
		return y495;
	}

	public int y583() {
		return y583;
	}

	public int y407() {
		return y407;
	}

	public int[] getMark() {
		return new int[] { y292, y366, y495, y583, y407 };
	}

	public static int[][] getFragmentIdList() {
		OGlycanUnit[] units = OGlycanUnit.values();
		int[][] fragmentId = new int[units.length][];
		for (int i = 0; i < units.length; i++) {
			fragmentId[units[i].id] = units[i].fragid;
		}
		return fragmentId;
	}

	public static OGlycanUnit getUnitFromID(int id) {
		return OGlycanUnit.values()[id];
	}

	/*
	 * public double [] getCore1_292_no366(){ return new double
	 * []{494.174789635, 656.227614635, }; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292n_366n(){ return new OGlycanUnit
	 * []{core1_1, core1_2, core1_7, core1_10}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366n_495n_583n(){ return new
	 * OGlycanUnit []{core1_3, core1_4, core1_5, core1_6, core1_8, core1_9,
	 * core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366n_495y_583y(){ return new
	 * OGlycanUnit []{core1_3, core1_4b, core1_5b, core1_6, core1_8, core1_9,
	 * core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366n_495y_583n(){ return new
	 * OGlycanUnit []{core1_3, core1_4b, core1_5, core1_6, core1_8, core1_9,
	 * core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366n_495n_583y(){ return new
	 * OGlycanUnit []{core1_3, core1_4, core1_5b, core1_6, core1_8, core1_9,
	 * core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292n_366y(){ return new OGlycanUnit
	 * []{core1_2, core1_7, core1_10}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366y_495n_583n(){ return new
	 * OGlycanUnit []{core1_4, core1_5, core1_6, core1_8, core1_9, core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366y_495y_583y(){ return new
	 * OGlycanUnit []{core1_4b, core1_5b, core1_6, core1_8, core1_9, core1_11};
	 * }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366y_495y_583n(){ return new
	 * OGlycanUnit []{core1_4b, core1_5, core1_6, core1_8, core1_9, core1_11}; }
	 * 
	 * public OGlycanUnit [] getCore1Unit_292y_366y_495n_583y(){ return new
	 * OGlycanUnit []{core1_4, core1_5b, core1_6, core1_8, core1_9, core1_11}; }
	 * 
	 * public double [] getCore2(){ return new double []{568.211571,
	 * 859.306987635}; }
	 * 
	 * public double [] getCore2_292(){ return new double []{730.264396,
	 * 1021.359812635, 1312.45522927}; }
	 * 
	 * public OGlycanUnit [] getCore2Unit_292n(){ return new OGlycanUnit
	 * []{OGlycanUnit.core2_1, OGlycanUnit.core2_3,}; }
	 * 
	 * public OGlycanUnit [] getCore2Unit_292y(){ return new OGlycanUnit
	 * []{OGlycanUnit.core2_2, OGlycanUnit.core2_4, OGlycanUnit.core2_5}; }
	 */
}
