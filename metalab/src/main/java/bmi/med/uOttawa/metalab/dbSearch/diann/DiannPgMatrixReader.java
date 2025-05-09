package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanProReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanProtein;

public class DiannPgMatrixReader extends FlashLfqQuanProReader {

	private boolean isCsv;
	private static Logger LOGGER = LogManager.getLogger(DiannPgMatrixReader.class);

	public DiannPgMatrixReader(String in) {
		this(new File(in));
		// TODO Auto-generated constructor stub
	}

	public DiannPgMatrixReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> intensityTitleList = new ArrayList<String>();

		String line = null;
		try {
			line = reader.readLine();
			String fileName = this.getFile().getName();
			if (fileName.endsWith("tsv") || fileName.endsWith("TSV")) {
				this.title = line.split("\t");
				this.isCsv = false;
			} else if (fileName.endsWith("csv") || fileName.endsWith("CSV")) {
				this.title = line.split(",");
				this.isCsv = true;
			} else {

			}

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Protein.Group") || title[i].equals("ProteinGroup")) {
					proteinId = i;
				} else if (title[i].endsWith(".d") || title[i].endsWith(".dia") || title[i].endsWith(".raw")) {
					int id = title[i].lastIndexOf("\\");
					if (id >= 0 && id < title[i].length() - 1) {
						String fileNameI = title[i].substring(id + 1);
						intensityTitleIdMap.put(fileNameI, i);
						intensityTitleList.add(fileNameI);
					} else {
						intensityTitleIdMap.put(title[i], i);
						intensityTitleList.add(title[i]);
					}
				}
			}

			this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}
	}

	protected FlashLfqQuanProtein parse() {
		if (line.trim().length() == 0) {
			return null;
		}

		String[] cs;
		if (this.isCsv) {
			cs = line.split(",");
		} else {
			cs = line.split("\t");
		}

		double[] intensity = new double[intensityTitles.length];

		for (int i = 0; i < intensity.length; i++) {
			int id = this.intensityTitleIdMap.get(intensityTitles[i]);
			if (id > -1 && id < cs.length) {
				if (cs[id].trim().length() > 0) {
					intensity[i] = Double.parseDouble(cs[id]);
				} else {
					intensity[i] = 0;
				}

			} else {
				intensity[i] = 0;
			}
		}

		String proname = cs[proteinId].split(";")[0];
		FlashLfqQuanProtein protein = new FlashLfqQuanProtein(proname, intensity);

		return protein;
	}

}
