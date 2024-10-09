/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;

/**
 * @author Kai Cheng
 *
 */
public class GlycosylDBManager {

	private static final Logger LOGGER = LogManager.getLogger(GlycosylDBManager.class);

	private static final File filepath = new File("src/bmi/med/uOttawa/glyco/Monosaccharide.info");
	private static final File allMono = new File("src/bmi/med/uOttawa/glyco/Monosaccharide.All.info");

	public static HashMap<String, Glycosyl> loadGlycoMap() {
		HashMap<String, Glycosyl> map = new HashMap<String, Glycosyl>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				Glycosyl glycosyl = Glycosyl.parse(line);
				map.put(glycosyl.getTitle(), glycosyl);
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}

		return map;
	}

	public static Glycosyl[] load() {
		ArrayList<Glycosyl> list = new ArrayList<Glycosyl>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				list.add(Glycosyl.parse(line));
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}

		Glycosyl[] glycosyls = list.toArray(new Glycosyl[list.size()]);
		return glycosyls;
	}

	public static void remove(Glycosyl... glycosyl) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String sname = line.substring(0, line.indexOf(";"));
				boolean remove = false;
				for (Glycosyl gl : glycosyl) {
					if (sname.equals(gl.getTitle())) {
						remove = true;
						break;
					}
				}
				if (!remove) {
					sb.append(line).append(FormatTool.CR);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filepath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing glycosyl to file " + filepath, e);
		}
		PrintWriter pw = new PrintWriter(fos);
		pw.write(sb.toString());
		pw.flush();
		pw.close();
	}

	public static void add(Glycosyl glycosyl) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				Glycosyl g0 = Glycosyl.parse(line);
				if (g0.getTitle().equals(glycosyl.getTitle())) {
					reader.close();
					LOGGER.error("Short name \"" + glycosyl.getTitle() + "\" already exist in the list.");

				} else if (g0.getFullname().equals(glycosyl.getFullname())) {
					reader.close();
					LOGGER.error("Short name \"" + glycosyl.getTitle() + "\" already exist in the list.");

				} else {
					sb.append(line).append(FormatTool.CR);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		sb.append(glycosyl.getFullString());

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filepath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing glycosyl to file " + filepath, e);
		}
		PrintWriter pw = new PrintWriter(fos);
		pw.write(sb.toString());
		pw.flush();
		pw.close();
	}

	public static HashMap<String, Glycosyl> getAllMono() {
		HashMap<String, Glycosyl> map = new HashMap<String, Glycosyl>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(allMono));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				int[] composition = new int[6];
				for (int i = 0; i < composition.length; i++) {
					composition[i] = Integer.parseInt(cs[5 + i]);
				}
				Glycosyl glycosyl = new Glycosyl(cs[2], cs[1], composition, Double.parseDouble(cs[3]),
						Double.parseDouble(cs[4]), cs[0], 20);
				if (!map.containsKey(cs[2])) {
					map.put(cs[2], glycosyl);
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading glycosyl from file " + filepath, e);
		}

		return map;
	}

	/**
	 * modifications on glycans
	 * 
	 * @return
	 */
	public static HashMap<String, int[]> getModMap() {
		HashMap<String, int[]> modmap = new HashMap<String, int[]>();
		modmap.put("Methyl", new int[] { 1, 2, 0, 0, 0, 0 });
		modmap.put("Acetyl", new int[] { 2, 2, 1, 0, 0, 0 });
		return modmap;
	}

}
