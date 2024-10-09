/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.quant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class OpenQuanReader {

	/** logger for this class */
	private static final Logger LOGGER = Logger.getLogger(OpenQuanReader.class);

	private int fileCount;
	private String[] fileNames;
	private OpenQuanPeptide[] peptides;
	private OpenQuanProtein[] proteins;

	public OpenQuanReader(String peptide, String protein) {
		this(new File(peptide), new File(protein));
	}

	public OpenQuanReader(File peptide, File protein) {
		this.read(peptide, protein);
	}

	private void read(File peptide, File protein) {
		BufferedReader pepreader = null;
		try {
			pepreader = new BufferedReader(new FileReader(peptide));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
			return;
		}
		String line = null;
		try {
			line = pepreader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
		}
		String[] title = line.split("\t");
		if (fileCount == 0) {
			fileCount = title.length / 2 - 1;
		}
		if (fileCount <= 0) {
			IOException e = new IOException("Error in reading quantitative result file " + peptide);
			LOGGER.error("", e);
		}
		this.fileNames = new String[fileCount];
		for (int i = 0; i < fileCount; i++) {
			fileNames[i] = title[i + 2].substring(title[i + 2].indexOf("_") + 1);
		}

		ArrayList<OpenQuanPeptide> peplist = new ArrayList<OpenQuanPeptide>();
		try {
			while ((line = pepreader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[fileCount];
				for (int i = 0; i < fileCount; i++) {
					intensity[i] = Double.parseDouble(cs[i + 2]);
				}

				OpenQuanPeptide oqp = new OpenQuanPeptide(cs[0], cs[1], 0, intensity);
				peplist.add(oqp);
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
		}
		try {
			pepreader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
			return;
		}
		this.peptides = peplist.toArray(new OpenQuanPeptide[peplist.size()]);

		ArrayList<OpenQuanProtein> prolist = new ArrayList<OpenQuanProtein>();
		BufferedReader proreader = null;
		try {
			proreader = new BufferedReader(new FileReader(protein));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative protein information from " + protein, e);
			return;
		}
		try {
			line = proreader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
		}
		try {
			while ((line = proreader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[fileCount];
				for (int i = 0; i < fileCount; i++) {
					intensity[i] = Double.parseDouble(cs[i + 1]);
				}
				OpenQuanProtein oqp = new OpenQuanProtein(cs[0], intensity);
				prolist.add(oqp);
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
		}
		try {
			proreader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading quantitative peptide information from " + peptide, e);
			return;
		}
		this.proteins = prolist.toArray(new OpenQuanProtein[prolist.size()]);
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public OpenQuanPeptide[] getPeptides() {
		return peptides;
	}

	public OpenQuanProtein[] getProteins() {
		return proteins;
	}

	public static HashMap<String, Integer> getQuanPSMCount(String quanPeaks) {
		return getQuanPSMCount(new File(quanPeaks));
	}

	public static HashMap<String, Integer> getQuanPSMCount(File quanPeaks) {

		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		BufferedReader peakReader = null;
		try {
			peakReader = new BufferedReader(new FileReader(quanPeaks));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading file " + quanPeaks, e);
		}

		try {
			String line = peakReader.readLine();
			while ((line = peakReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] seqs = cs[1].split("\\|");
				for (String seq : seqs) {
					if (countMap.containsKey(seq)) {
						countMap.put(seq, countMap.get(seq) + 1);
					} else {
						countMap.put(seq, 1);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading file " + quanPeaks, e);
		}
		try {
			peakReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading file " + quanPeaks, e);
		}
		return countMap;
	}

}
