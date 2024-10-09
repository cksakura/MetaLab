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
public class PFindEnzymeIO {

	private static String path = "";

	public static String[][] getEnzymes() {
		return getEnzymes(new File(path));
	}

	public static String[][] getEnzymes(String in) {
		return getEnzymes(new File(in));
	}

	public static String[][] getEnzymes(File in) {
		String[][] enzymes = new String[0][0];
		if (in.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(in));
				String line = null;
				int count = 0;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("@NUMBER_ENZYME")) {
						int number = Integer.parseInt(line.substring(line.indexOf("=") + 1));
						enzymes = new String[number][];
					} else {
						int id = line.indexOf("=");
						if (id > 0) {
							String enzyme = line.substring(id + 1);
							if (count < enzymes.length) {
								enzymes[count] = enzyme.split("\\s");
								count++;
							} else {
								String[] newEnzymes = new String[enzymes.length + 1];
								System.arraycopy(enzyme, 0, newEnzymes, 0, enzymes.length);
								newEnzymes[count] = enzyme;
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

		return enzymes;
	}
}
