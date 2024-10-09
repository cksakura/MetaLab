/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bmi.med.uOttawa.metalab.core.model.RowObject;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;

/**
 * @author Kai Cheng
 *
 */
public class Glycosyl {

	public static final Glycosyl deoxy_eryHexNN = new Glycosyl("deoxy_eryHexNN", "deoxy_eryHexNN",
			new int[] { 6, 12, 2, 1 }, 128.094963, 128.17228, "[abox]-[dlx]xyl-PEN-[0-9x]:[0-9x]", 20);

	public static final Glycosyl Pen = new Glycosyl("Pen", "Pentose", new int[] { 5, 8, 4, 0 }, 132.04226, 132.11462,
			"[abox]-[dlx]xyl-PEN-[0-9x]:[0-9x]", 17);

	public static final Glycosyl Xylose = new Glycosyl("Xyl", "Xylose", new int[] { 5, 8, 4, 0 }, 132.04226, 132.11462,
			"[abox]-[dlx]xyl-PEN-[0-9x]:[0-9x]", 17);

	public static final Glycosyl dHex = new Glycosyl("dHex", "Deoxyhexose", new int[] { 6, 10, 4, 0 }, 146.057909,
			146.1412, "[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|[0-9x]:d$", 18);

	public static final Glycosyl Fuc = new Glycosyl("Fuc", "Fucose", new int[] { 6, 10, 4, 0 }, 146.057909, 146.1412,
			"[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|[0-9x]:d$", 18);

	public static final Glycosyl Hex = new Glycosyl("Hex", "Hexose", new int[] { 6, 10, 5, 0 }, 162.052825, 162.1406,
			"[abox]-[dlx](gal|man|glu)-HEX-[0-9x]:[0-9x]$", 6);

	public static final Glycosyl Gal = new Glycosyl("Gal", "Galcose", new int[] { 6, 10, 5, 0 }, 162.052825, 162.1406,
			"[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]$", 7);

	public static final Glycosyl Glc = new Glycosyl("Glc", "Glucose", new int[] { 6, 10, 5, 0 }, 162.052825, 162.1406,
			"[abox]-[dlx]glc-HEX-[0-9x]:[0-9x]$", 9);

	public static final Glycosyl Man = new Glycosyl("Man", "Mannose", new int[] { 6, 10, 5, 0 }, 162.052825, 162.1406,
			"[abox]-[dlx]man-HEX-[0-9x]:[0-9x]$", 8);

	public static final Glycosyl HexNAc = new Glycosyl("HexNAc", "N-Acetylhexosamine", new int[] { 8, 13, 5, 1 },
			203.079373, 203.1925, "[abox]-[dlx](gal|man|glu)-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-acetyl$",
			1);

	public static final Glycosyl ManNAc = new Glycosyl("ManNAc", "N-Acetylhexosamine", new int[] { 8, 13, 5, 1 },
			203.079373, 203.1925, "[abox]-[dlx]man-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-acetyl$", 1);

	public static final Glycosyl GalNAc = new Glycosyl("GalNAc", "N-Acetylhexosamine", new int[] { 8, 13, 5, 1 },
			203.079373, 203.1925, "[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-acetyl$", 2);

	public static final Glycosyl GlcNAc = new Glycosyl("HexNAc", "N-Acetylhexosamine", new int[] { 8, 13, 5, 1 },
			203.079373, 203.1925, "[abox]-[dlx]glc-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-acetyl$", 3);

	public static final Glycosyl NeuAc_H2O = new Glycosyl("NeuAc_H2O", "N-Acetyl Neuraminic Acid Loss Water",
			new int[] { 11, 15, 7, 1 }, 273.080136635, 273.2393, "", 0);

	public static final Glycosyl NeuAc = new Glycosyl("NeuAc", "N-Acetyl neuraminic Acid", new int[] { 11, 17, 8, 1 },
			291.095416, 291.25462,
			"[abox]-[dlx]gro-[dlx]gal-NON-[0-9x]:[0-9x]\\|[0-9x]:a\\|[0-9x]:keto\\|[0-9x]:d\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-acetyl$",
			12);

	public static final Glycosyl NeuGc = new Glycosyl("NeuGc", "N-glycoyl neuraminic acid", new int[] { 11, 17, 9, 1 },
			307.09033058, 307.25402,
			"[abox]-[dlx]gro-[dlx]gal-NON-[0-9x]:[0-9x]\\|[0-9x]:a\\|[0-9x]:keto\\|[0-9x]:d\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)n-glycolyl$",
			10);

	public static final Glycosyl FucMe = new Glycosyl("FucMe", "Galactosamine", new int[] { 7, 12, 0, 4 },
			160.07356000000001, 160.16778, "[abox]-[dlx]gal-HEX-1:5\\|6:d\\|\\|\\(-?[0-9x]o:1\\)methyl$", 20);

	public static final Glycosyl Galactosamine = new Glycosyl("GalN", "Galactosamine", new int[] { 6, 11, 4, 1 },
			161.0688078475, 161.15583999999998, "[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)amino$",
			5);

	public static final Glycosyl Glucosamine = new Glycosyl("GlcN", "Glucosamine", new int[] { 6, 11, 4, 1 },
			161.0688078475, 161.15583999999998, "[abox]-[dlx]glc-HEX-[0-9x]:[0-9x]\\|\\|\\(-?[0-9x]d:-?[0-9x]\\)amino$",
			4);

	public static final Glycosyl Hexose_acid = new Glycosyl("HexA", "Hexuronic acid", new int[] { 6, 8, 6, 0 },
			176.03209, 176.12412, "[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|[0-9x]:a$", 15);

	public static final Glycosyl Galacturonic_acid = new Glycosyl("GalA", "Galacturonic acid", new int[] { 6, 8, 6, 0 },
			176.03209, 176.12412, "[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|[0-9x]:a$", 15);

	public static final Glycosyl Glucuronic_acid = new Glycosyl("GlcA", "Glucuronic acid", new int[] { 6, 8, 6, 0 },
			176.03209, 176.12412, "[abox]-[dlx]Glc-HEX-[0-9x]:[0-9x]\\|[0-9x]:a$", 14);

	public static final Glycosyl Iduronic_acid = new Glycosyl("IdoA", "Iduronic acid", new int[] { 6, 8, 6, 0 },
			176.03209, 176.12412, "[abox]-[dlx]ido-HEX-[0-9x]:[0-9x]\\|[0-9x]:a$", 13);

	public static final Glycosyl Mannuronic_acid = new Glycosyl("ManA", "Mannuronic acid", new int[] { 6, 8, 6, 0 },
			176.03209, 176.12412, "[abox]-[dlx]Man-HEX-[0-9x]:[0-9x]\\|[0-9x]:a$", 16);

	public static final Glycosyl KDN = new Glycosyl("KDN", "KDN", new int[] { 9, 14, 8, 0 }, 250.068867425,
			250.20265999999998, "[abox]-[dlx]gro-[dlx]gal-NON-[0-9x]:[0-9x]\\|[0-9x]:a\\|[0-9x]:keto\\|[0-9x]:d$", 11);

	public static final Glycosyl KDO = new Glycosyl("KDO", "KDO", new int[] { 8, 12, 7, 0 }, 220.05830500000002,
			220.17668, "[abox]-[dlx]man-OCT-[0-9x]:[0-9x]\\|[0-9x]:a\\|[0-9x]:keto\\|[0-9x]:d$", 20);

	public static final Glycosyl Pseudaminic_acid = new Glycosyl("Pse", "Pseudaminic acid", new int[] { 13, 20, 7, 2 },
			316.127053, 316.30717999999996, "", 20);

	public static final Glycosyl GlcNAcAcAc = new Glycosyl("GlcNAcAcAc", "GlcNAcAcAc", new int[] { 12, 17, 7, 1 },
			287.1005019, 287.26588,
			"[abox]-[dlx]glc-HEX-1:5\\|\\|\\(2d:1\\)n-acetyl\\|\\(3o:1\\)acetyl\\|\\([0-9x]o:1\\)acetyl$", 20);

	public static final Glycosyl GlcNAcGc = new Glycosyl("GlcNAcGc", "GlcNAcGc", new int[] { 10, 15, 7, 1 },
			261.0848518, 261.2286, "[abox]-[dlx]glc-HEX-[0-9x]:[0-9x]\\|\\|\\(2d:1\\)n-acetyl\\|\\(6o:1\\)glycolyl$",
			20);

	public static final Glycosyl Unknown = new Glycosyl("Unknown", "Unknown", new int[] { 6, 10, 5, 0 }, 0.0, 0.0, "",
			20);

	public static final Glycosyl[] commonGlycosyls = new Glycosyl[] { Glycosyl.deoxy_eryHexNN, Glycosyl.dHex,
			Glycosyl.Fuc, Glycosyl.Gal, Glycosyl.Galactosamine, Glycosyl.Galacturonic_acid, Glycosyl.GalNAc,
			Glycosyl.Glc, Glycosyl.GlcNAc, Glycosyl.GlcNAcAcAc, Glycosyl.GlcNAcGc, Glycosyl.Glucosamine,
			Glycosyl.Glucuronic_acid, Glycosyl.Hex, Glycosyl.HexNAc, Glycosyl.Iduronic_acid, Glycosyl.KDN, Glycosyl.Man,
			Glycosyl.ManNAc, Glycosyl.Mannuronic_acid, Glycosyl.NeuAc, Glycosyl.NeuAc_H2O, Glycosyl.NeuGc,
			Glycosyl.Pseudaminic_acid, Glycosyl.Unknown, Glycosyl.Xylose };

	private DecimalFormat df4 = FormatTool.getDF4();
	
	String title;
	String fullname;
	/**
	 * C,H,O
	 */
	int[] CHO;
	double mono_mass;
	double avge_mass;
	String pattern;
	int graphicsId;

	private int expId;

	public Glycosyl(String title, String fullname, int[] CHO, double mono_mass, double avge_mass, String pattern,
			int graphicsId) {
		this.title = title;
		this.fullname = fullname;
		this.CHO = CHO;
		this.mono_mass = mono_mass;
		this.avge_mass = avge_mass;
		this.pattern = pattern;
		this.graphicsId = graphicsId;
	}

	public int getGraphicsId() {
		return graphicsId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public double getMonoMass() {
		return mono_mass;
	}

	public void setMonoMass(double mono_mass) {
		this.mono_mass = mono_mass;
	}

	public double getAvgeMass() {
		return avge_mass;
	}

	public void setAvgeMass(double avge_mass) {
		this.avge_mass = avge_mass;
	}

	public String getPattern() {
		return pattern;
	}

	public int[] getCHON() {
		return this.CHO;
	}

	public int getExpId() {
		return expId;
	}

	public void setExpId(int expId) {
		this.expId = expId;
	}

	public static Glycosyl judgeType(String fullname) {

		Glycosyl g = null;
		int id = -1;

		for (int i = 0; i < commonGlycosyls.length; i++) {

			Pattern pattern = Pattern.compile(commonGlycosyls[i].getPattern());
			Matcher m = pattern.matcher(fullname);
			if (m.matches()) {
				if (commonGlycosyls[i].graphicsId > id) {
					g = commonGlycosyls[i];
					id = g.graphicsId;
				}
			}
		}

		if (g != null)
			return g;

		return Glycosyl.Unknown;
	}

	public RowObject getRowObject() {
		Object[] objects = new Object[4];
		objects[0] = title;
		StringBuilder sb = new StringBuilder();
		String[] element = new String[] { "C", "H", "O", "N", "P", "S" };
		for (int i = 0; i < CHO.length; i++) {
			if (CHO[i] > 0) {
				sb.append(element[i]).append("(").append(CHO[i]).append(")");
			}
		}
		objects[1] = sb.toString();
		objects[2] = df4.format(mono_mass);
		objects[3] = df4.format(avge_mass);
		RowObject ro = new RowObject(objects);
		return ro;
	}

	public String toString() {
		return title;
	}

	public String getCompositionString() {
		StringBuilder sb = new StringBuilder();
		String[] element = new String[] { "C", "H", "O", "N", "P", "S" };
		for (int i = 0; i < CHO.length; i++) {
			if (CHO[i] > 0) {
				sb.append(element[i]).append("(").append(CHO[i]).append(")");
			}
		}
		return sb.toString();
	}

	public String getFullString() {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append(";");
		sb.append(fullname).append(";");

		for (int i = 0; i < CHO.length; i++) {
			sb.append(CHO[i]).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);

		sb.append(";");
		sb.append(mono_mass).append(";");
		sb.append(avge_mass).append(";");
		sb.append(pattern).append(";");
		sb.append(graphicsId);
		return sb.toString();
	}

	public static Glycosyl parse(String[] cs) {
		if (cs.length == 7) {
			String[] cs2 = cs[2].split("[,_]");
			int[] cho = new int[cs2.length];
			for (int i = 0; i < cho.length; i++) {
				cho[i] = Integer.parseInt(cs2[i]);
			}
			double monoMass = Double.parseDouble(cs[3]);
			double aveMass = Double.parseDouble(cs[4]);
			int graphicsId = Integer.parseInt(cs[6]);
			Glycosyl glycosyl = new Glycosyl(cs[0], cs[1], cho, monoMass, aveMass, cs[5], graphicsId);
			return glycosyl;
		}

		return null;
	}

	public static Glycosyl parse(String line) {
		String[] cs = line.split(";");
		if (cs.length == 6) {
			double monoMass = Double.parseDouble(cs[2]);
			double aveMass = Double.parseDouble(cs[3]);
			int graphicsId = Integer.parseInt(cs[5]);
			Glycosyl glycosyl = new Glycosyl(cs[0], cs[1], new int[] {}, monoMass, aveMass, cs[5], graphicsId);
			return glycosyl;
		} else if (cs.length == 7) {
			String[] cs2 = cs[2].split(",");
			int[] cho = new int[cs2.length];
			for (int i = 0; i < cho.length; i++) {
				cho[i] = Integer.parseInt(cs2[i]);
			}
			double monoMass = Double.parseDouble(cs[3]);
			double aveMass = Double.parseDouble(cs[4]);
			int graphicsId = Integer.parseInt(cs[6]);
			Glycosyl glycosyl = new Glycosyl(cs[0], cs[1], cho, monoMass, aveMass, cs[5], graphicsId);
			return glycosyl;
		} else if (cs.length >= 8) {
			double monoMass = Double.parseDouble(cs[3]);
			double aveMass = Double.parseDouble(cs[4]);
			int graphicsId = Integer.parseInt(cs[6]);

			int[][] possibleComposition = new int[cs.length - 7][];
			Glycosyl[] glycosyls = new Glycosyl[cs.length - 7];
			String[] modName = new String[cs.length - 7];
			int[][] modComposition = new int[cs.length - 7][];
			for (int i = 0; i < possibleComposition.length; i++) {
				String[] pcsi = cs[i + 7].split(",");

				String[] pcomi = pcsi[0].split("_");
				possibleComposition[i] = new int[pcomi.length];
				for (int j = 0; j < pcomi.length; j++) {
					possibleComposition[i][j] = Integer.parseInt(pcomi[j]);
				}
				if (pcsi.length == 2) {
					String[] pcsi1 = pcsi[1].split("@");
					glycosyls[i] = Glycosyl.parse(pcsi1);
				} else if (pcsi.length == 4) {
					String[] pcsi1 = pcsi[1].split("@");
					glycosyls[i] = Glycosyl.parse(pcsi1);
					modName[i] = pcsi[2];
					if (modName[i].length() > 0) {
						String[] pcsi3 = pcsi[3].split("_");
						modComposition[i] = new int[pcsi3.length];
						for (int j = 0; j < pcsi3.length; j++) {
							modComposition[i][j] = Integer.parseInt(pcsi3[j]);
						}
					}
				}
			}
			NewGlycosyl glycosyl = new NewGlycosyl(cs[0], cs[1], new int[] {}, monoMass, aveMass, cs[5], graphicsId);
			glycosyl.setPossibleComposition(possibleComposition);
			glycosyl.setGlycosyls(glycosyls);
			glycosyl.setModName(modName);
			glycosyl.setModComposition(modComposition);
			return glycosyl;
		}

		return null;
	}

	public static class GlycosylMassComparator implements Comparator<Glycosyl> {

		@Override
		public int compare(Glycosyl o1, Glycosyl o2) {
			// TODO Auto-generated method stub
			if (o1.getMonoMass() < o2.getMonoMass()) {
				return -1;
			}
			if (o1.getMonoMass() > o2.getMonoMass()) {
				return 1;
			}
			return 0;
		}

	}

	public static class GlycosylCompComparator implements Comparator<Glycosyl> {

		@Override
		public int compare(Glycosyl o1, Glycosyl o2) {
			// TODO Auto-generated method stub

			int[] comp1 = o1.CHO;
			int[] comp2 = o2.CHO;

			for (int i = 0; i < comp1.length; i++) {
				if (comp1[i] < comp2[i]) {
					return -1;
				} else if (comp1[i] > comp2[i]) {
					return 1;
				}
			}

			return 0;
		}

	}

	public static class GlycosylExpidComparator implements Comparator<Glycosyl> {

		@Override
		public int compare(Glycosyl o1, Glycosyl o2) {
			// TODO Auto-generated method stub
			if (o1.getExpId() < o2.getExpId()) {
				return -1;
			}
			if (o1.getExpId() > o2.getExpId()) {
				return 1;
			}
			return 0;
		}
	}

}
