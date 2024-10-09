/**
 * 
 */
package bmi.med.uOttawa.metalab.task.pfind.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Kai Cheng
 *
 */
public class PFindModIO {

	private static String path = "";

	public static String[] getMods() {
		return getMods(new File(path));
	}

	public static String[] getMods(String in) {
		return getMods(new File(in));
	}

	public static String[] getMods(File in) {
		String[] mods = new String[0];
		if (in.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(in));
				String line = null;
				int count = 0;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("@NUMBER_MODIFICATION")) {
						int number = Integer.parseInt(line.substring(line.indexOf("=") + 1));
						mods = new String[number];
					} else if (line.startsWith("name")) {
						int id = line.indexOf("=");
						if (id > 0) {
							String mod = line.substring(id + 1);
							if (count < mods.length) {
								mods[count] = mod;
								count++;
							} else {
								String[] newMods = new String[mods.length + 1];
								System.arraycopy(mods, 0, newMods, 0, mods.length);
								newMods[count] = mod;
								count++;
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return mods;
	}
}
