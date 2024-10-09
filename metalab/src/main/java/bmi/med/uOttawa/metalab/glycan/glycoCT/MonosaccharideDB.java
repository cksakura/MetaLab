/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kai Cheng
 *
 */
public class MonosaccharideDB {

	private static Pattern PatternGlcNac = Pattern
			.compile("[abox]-[dlx]glc-HEX-[0-9x]:[0-9x]\\|\\|\\([0-9x]d:[0-9x]\\)n-acetyl");
	private static Pattern PatternMan = Pattern.compile("[abox]-[dlx]man-HEX-[0-9x]:[0-9x]");
	private static Pattern PatternFuc = Pattern.compile("[abox]-[dlx]gal-HEX-[0-9x]:[0-9x]\\|[0-9x]:d");
	private static Pattern PatternXyl = Pattern.compile("[abox]-[dlx]xyl-PEN-[0-9x]:[0-9x]");

	private static double[] mono = new double[] { 12.0, 1.0078250319, 14.0030740074, 15.9949146223, 31.97207073,
			18.99840320, 34.96885271, 78.9183379, 126.904468, 30.97376149 };

	private static double[] ave = new double[] { 12.0107, 1.00794, 14.0067, 15.9994, 32.065, 18.9984032, 35.453, 79.904,
			126.90447, 30.973761 };

	public MonosaccharideDB() {

	}

	public static String add(String name, String carbBank, String iupac, int[] comp) {

		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		sb.append(carbBank).append("\n");
		sb.append(iupac).append("\n");

		double monomass = 0;
		double avemass = 0;
		for (int i = 0; i < comp.length; i++) {
			sb.append(comp[i]).append("\t");
			monomass += comp[i] * mono[i];
			avemass += comp[i] * ave[i];
		}
		sb.append("\n");
		sb.append(monomass).append("\n");
		sb.append(avemass).append("\n");

		return sb.toString();
	}

	public static void combineTxt(String in, String out) throws IOException {

		PrintWriter pw = new PrintWriter(out);
		File[] files = new File(in).listFiles();
		for (int i = 0; i < files.length; i++) {

			BufferedReader reader = new BufferedReader(new FileReader(files[i]));
			int id = 0;
			String line = null;
			String comp = "";
			while ((line = reader.readLine()) != null) {

				switch (id) {

				case 0: {

					pw.write(line + "\t");
					pw.write(parsePattern(line) + "\t");
					break;
				}

				case 1: {

					pw.write(line + "\t");
					pw.write(GlycoDatabaseReader.findName(line) + "\t");

					break;
				}

				case 2: {

					break;
				}

				case 3: {

					comp = line;
					break;
				}

				case 4: {

					pw.write(line + "\t");
					break;
				}

				case 5: {

					pw.write(line + "\t");
					pw.write(comp);

					break;
				}

				}

				id++;
			}
			reader.close();
			pw.write("\n");
		}

		pw.close();
	}

	private static String parsePattern(String monosaccharide) {

		int id = monosaccharide.indexOf("||");
		String stemtype;
		String sub = "";
		if (id >= 0) {
			stemtype = monosaccharide.substring(0, id);
			sub = monosaccharide.substring(id + 2);
		} else {
			stemtype = monosaccharide;
		}

		StringBuilder sb = new StringBuilder();
		String[] ss = stemtype.split("-");
		for (int i = 0; i < ss.length; i++) {

			if (ss[i].length() == 1) {

				sb.append("[abox]-");

			} else if (ss[i].length() == 4) {

				if (ss[i].startsWith("d")) {

					sb.append("[dlx]").append(ss[i].substring(1)).append("-");

				} else if (ss[i].startsWith("l")) {

					sb.append("[dlx]").append(ss[i].substring(1)).append("-");

				} else {
					System.out.println(ss[i]);
				}

			} else if (ss[i].length() == 3) {

				if (ss[i].contains(":")) {

					char[] cs = ss[i].toCharArray();
					for (int j = 0; j < cs.length; j++) {
						if (cs[j] >= 48 && cs[j] <= 57) {
							sb.append("[0-9x]");
						} else {
							sb.append(cs[j]);
						}
					}

				} else {
					sb.append(ss[i]).append("-");
				}

			} else {

				char[] cs = ss[i].toCharArray();
				for (int j = 0; j < cs.length; j++) {

					if (cs[j] >= 48 && cs[j] <= 57) {

						sb.append("[0-9x]");

					} else if (cs[j] == '|') {
						sb.append("\\|");

					} else {
						sb.append(cs[j]);
					}
				}
			}
		}

		if (sub.length() == 0)
			return sb.toString();

		sb.append("\\|\\|");
		char[] cs = sub.toCharArray();
		for (int j = 0; j < cs.length; j++) {

			if (cs[j] >= 48 && cs[j] <= 57) {
				sb.append("-?[0-9x]");

			} else if (cs[j] == '(') {
				sb.append("\\(");

			} else if (cs[j] == ')') {
				sb.append("\\)");

			} else if (cs[j] == '|') {
				sb.append("\\|");

			} else {
				sb.append(cs[j]);
			}
		}

		return sb.toString();
	}

	public static HashSet<String> getStemSet(String in) throws IOException {

		HashSet<String> set = new HashSet<String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line;
		boolean add = true;
		while ((line = reader.readLine()) != null) {

			if (add) {

				int id = line.indexOf("||");
				if (id >= 0) {
					line = line.substring(0, id);
				}

				StringBuilder sb = new StringBuilder();
				String[] ss = line.split("-");
				for (int i = 0; i < ss.length; i++) {

					if (ss[i].length() == 1) {

						sb.append("[abox]-");

					} else if (ss[i].length() == 4) {

						if (ss[i].startsWith("d")) {

							sb.append("[dlx]").append(ss[i].substring(1)).append("-");

						} else if (ss[i].startsWith("l")) {

							sb.append("[dlx]").append(ss[i].substring(1)).append("-");

						} else {
							System.out.println(ss[i]);
						}

					} else if (ss[i].length() == 3) {

						if (ss[i].contains(":")) {

							char[] cs = ss[i].toCharArray();
							for (int j = 0; j < cs.length; j++) {
								if (cs[j] >= 48 && cs[j] <= 57) {
									sb.append("[0-9x]");
								} else {
									sb.append(cs[j]);
								}
							}

						} else {
							sb.append(ss[i]).append("-");
						}

					} else {

						char[] cs = ss[i].toCharArray();
						for (int j = 0; j < cs.length; j++) {
							if (cs[j] >= 48 && cs[j] <= 57) {
								sb.append("[0-9x]");
							} else {
								sb.append(cs[j]);
							}
						}
					}
				}

				set.add(sb.toString());
				add = false;

			} else {
				if (line.trim().length() == 0) {
					add = true;
				}
			}
		}
		System.out.println(set.size());
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String ss = it.next();
			System.out.println(ss);
		}
		reader.close();
		return set;
	}

	public static boolean isGlcNac(String mono) {
		Matcher m = PatternGlcNac.matcher(mono);
		return m.matches();
	}

	public static boolean isMan(String mono) {
		Matcher m = PatternMan.matcher(mono);
		return m.matches();
	}

	public static boolean isFuc(String mono) {
		Matcher m = PatternFuc.matcher(mono);
		return m.matches();
	}

	public static boolean isXyl(String mono) {
		Matcher m = PatternXyl.matcher(mono);
		return m.matches();
	}

	public static HashSet<String> getMonoInfo(String in) throws IOException {

		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("Pattern")) {
				int beg = line.indexOf(":");
				set.add(line.substring(beg + 1));
				// System.out.println(line+"\t"+line.hashCode());
			}
		}
		reader.close();
		return set;
	}

	public static HashMap<String, String> getMonoMassMap(String in) throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		String name = "";

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("Pattern")) {

				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				name = content;

			} else if (line.startsWith("Mono")) {
				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				map.put(name, content);
			}
		}

		reader.close();

		return map;
	}

	public static HashMap<String, String> getMonoMassMapNew(String in) throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] cs = line.split("\t");
			map.put(cs[1], cs[4]);
		}

		reader.close();

		return map;
	}

	public static HashMap<String, Double> getMonoMassDoubleMap(String in) throws IOException {

		HashMap<String, Double> map = new HashMap<String, Double>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		String name = "";

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("Pattern")) {

				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				name = content;

			} else if (line.startsWith("Mono")) {
				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				map.put(name, Double.parseDouble(content));
			}
		}

		reader.close();

		return map;
	}

	public static HashMap<String, Double> getAvgMassMap(String in) throws IOException {

		HashMap<String, Double> map = new HashMap<String, Double>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		String name = "";

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("Pattern")) {

				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				name = content;

			} else if (line.startsWith("Avg")) {
				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				map.put(name, Double.parseDouble(content));
			}
		}

		reader.close();

		return map;
	}

	public static HashMap<String, String> getCarbBankNameMap(String in) throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		String name = "";

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("Pattern")) {

				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				name = content;

			} else if (line.startsWith("CarbBank")) {
				int beg = line.indexOf(":");
				String content = line.substring(beg + 1);
				map.put(name, content);
			}
		}

		reader.close();

		return map;
	}

	public static HashMap<String, String> getAbbreNameMapNew(String in) throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] cs = line.split("\t");
			map.put(cs[1], cs[3]);
		}

		reader.close();

		return map;
	}

	public static ArrayList<String> getMonoInfoList(String in) throws IOException {

		ArrayList<String> set = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("[")) {
				set.add(line);
				// System.out.println(line+"\t"+line.hashCode());
			}
		}
		reader.close();
		return set;
	}

	/**
	 * Original monosaccharide, 450 items, contains Pattern;
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Monosaccharide> getMonosaccMap(String in) throws IOException {

		HashMap<String, Monosaccharide> map = new HashMap<String, Monosaccharide>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] content = line.split("\t");
			Monosaccharide saccharide = new Monosaccharide();
			saccharide.setGlycoCT_Name(content[0]);
			saccharide.setPattern(Pattern.compile(content[1]));
			saccharide.setCarbBank_Name(content[2]);
			saccharide.setIUPAC_Name(content[3]);
			saccharide.setMono_mass(Double.parseDouble(content[4]));
			saccharide.setAvg_mass(Double.parseDouble(content[5]));

			String[] ss = new String[content.length - 6];
			System.arraycopy(content, 6, ss, 0, ss.length);
			int[] composition = new int[ss.length];
			for (int i = 0; i < ss.length; i++) {
				composition[i] = Integer.parseInt(ss[i]);
			}
			saccharide.setComposition(composition);
			map.put(content[0], saccharide);
		}
		reader.close();

		return map;
	}

	/**
	 * Monosaccharide in glycoCT database, 988 items, without Pattern;
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Monosaccharide> getMonosaccInfoMap(String in) throws IOException {

		HashMap<String, Monosaccharide> map = new HashMap<String, Monosaccharide>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] content = line.split("\t");
			Monosaccharide saccharide = new Monosaccharide();
			saccharide.setGlycoCT_Name(content[0]);
			saccharide.setCarbBank_Name(content[1]);
			saccharide.setIUPAC_Name(content[2]);
			saccharide.setMono_mass(Double.parseDouble(content[3]));
			saccharide.setAvg_mass(Double.parseDouble(content[4]));

			String[] ss = new String[content.length - 5];
			System.arraycopy(content, 5, ss, 0, ss.length);
			int[] composition = new int[ss.length];
			for (int i = 0; i < ss.length; i++) {
				composition[i] = Integer.parseInt(ss[i]);
			}
			saccharide.setComposition(composition);
			map.put(content[0], saccharide);
		}
		reader.close();

		return map;
	}

	/**
	 * Monosaccharide in glycoCT database, 988 items, without Pattern;
	 * 
	 * modified by Hao Wan & Junfeng Huang, the mass of NeuAc was changed because of
	 * enrichment -C2H5O2 + N
	 * 
	 * mono : -47.025881 avg : -47.05316
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Monosaccharide> getMonosaccInfoMapModified1(String in) throws IOException {

		HashMap<String, Monosaccharide> map = new HashMap<String, Monosaccharide>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] content = line.split("\t");
			Monosaccharide saccharide = new Monosaccharide();
			saccharide.setGlycoCT_Name(content[0]);
			saccharide.setCarbBank_Name(content[1]);
			saccharide.setIUPAC_Name(content[2]);
			if (content[2].equals("NeuAc")) {
				saccharide.setMono_mass(Double.parseDouble(content[3]) - 47.025881 - 3 * 1.00286864);
				saccharide.setAvg_mass(Double.parseDouble(content[4]) - 47.05316 - 3 * 1.00286864);
				System.out.println(saccharide.getMono_mass());
			} else {
				saccharide.setMono_mass(Double.parseDouble(content[3]));
				saccharide.setAvg_mass(Double.parseDouble(content[4]));
			}

			String[] ss = new String[content.length - 5];
			System.arraycopy(content, 5, ss, 0, ss.length);
			int[] composition = new int[ss.length];
			for (int i = 0; i < ss.length; i++) {
				composition[i] = Integer.parseInt(ss[i]);
			}
			saccharide.setComposition(composition);
			map.put(content[0], saccharide);
		}
		reader.close();

		return map;
	}

	/**
	 * Monosaccharide in glycoCT database, 988 items, without Pattern;
	 * 
	 * modified by Hao Wan & Junfeng Huang, the mass of NeuAc was changed because of
	 * enrichment -O3+C4HN
	 * 
	 * mono : 15.026153999999996 avg : 15.059280000000003
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, Monosaccharide> getMonosaccInfoMapModified2(String in) throws IOException {

		HashMap<String, Monosaccharide> map = new HashMap<String, Monosaccharide>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line = reader.readLine()) != null) {

			String[] content = line.split("\t");
			Monosaccharide saccharide = new Monosaccharide();
			saccharide.setGlycoCT_Name(content[0]);
			saccharide.setCarbBank_Name(content[1]);
			saccharide.setIUPAC_Name(content[2]);
			if (content[2].equals("NeuAc")) {
				saccharide.setMono_mass(Double.parseDouble(content[3]) + 15.026153999999996);
				saccharide.setAvg_mass(Double.parseDouble(content[4]) + 15.059280000000003);
			} else {
				saccharide.setMono_mass(Double.parseDouble(content[3]));
				saccharide.setAvg_mass(Double.parseDouble(content[4]));
			}

			String[] ss = new String[content.length - 5];
			System.arraycopy(content, 5, ss, 0, ss.length);
			int[] composition = new int[ss.length];
			for (int i = 0; i < ss.length; i++) {
				composition[i] = Integer.parseInt(ss[i]);
			}
			saccharide.setComposition(composition);
			map.put(content[0], saccharide);
		}
		reader.close();

		return map;
	}

	private static void judgeCorrect(String in) throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		int id = 0;
		HashMap<String, Double> map = new HashMap<String, Double>();
		while ((line = reader.readLine()) != null) {

			id++;
			String[] content = line.split("\t");
			Monosaccharide saccharide = new Monosaccharide();
			saccharide.setGlycoCT_Name(content[0]);
			saccharide.setCarbBank_Name(content[1]);
			saccharide.setIUPAC_Name(content[2]);
			saccharide.setMono_mass(Double.parseDouble(content[3]));
			saccharide.setAvg_mass(Double.parseDouble(content[4]));

			String[] ss = new String[content.length - 5];
			System.arraycopy(content, 5, ss, 0, ss.length);
			int[] composition = new int[ss.length];
			for (int i = 0; i < ss.length; i++) {
				composition[i] = Integer.parseInt(ss[i]);
			}
			saccharide.setComposition(composition);
			double calMass = 0;
			for (int i = 0; i < composition.length; i++) {
				calMass += composition[i] * mono[i];
			}

			String key = Arrays.toString(composition);
			if (map.containsKey(key)) {
				// if(map.get(key)!=saccharide.getMono_mass())
				System.out.println(key + "\t" + map.get(key) + "\t" + saccharide.getMono_mass());
			} else {
				map.put(key, saccharide.getMono_mass());
			}
		}
		reader.close();
	}

}
