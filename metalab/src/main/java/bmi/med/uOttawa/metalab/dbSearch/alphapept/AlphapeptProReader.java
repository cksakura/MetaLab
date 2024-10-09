package bmi.med.uOttawa.metalab.dbSearch.alphapept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.AbstractMetaProteinReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class AlphapeptProReader extends AbstractMetaProteinReader {

	private static final String REV = "REV__";
	public static final String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	
	protected BufferedReader reader;
	protected int proteinId = 0;
	protected String[] fileNames;
	protected int[] intensityId;
	protected String[] title;
	protected String line;

	private MetaProtein[] metaProteins;

	private static Logger LOGGER = LogManager.getLogger(AlphapeptProReader.class);

	public AlphapeptProReader(String in) {
		this(new File(in));
	}

	public AlphapeptProReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Alphapept search result file " + in, e);
		}
		this.parseTitle();
	}

	public AlphapeptProReader(String in, String[] fileNames) {
		this(new File(in), fileNames);
	}

	public AlphapeptProReader(File in, String[] fileNames) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Alphapept search result file " + in, e);
		}
		this.fileNames = fileNames;
		this.parseTitle();
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe protein quantification result file " + super.getFile(), e);
		}

		this.title = line.split(delimiter);
		if (fileNames == null) {
			ArrayList<Integer> idList = new ArrayList<Integer>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].endsWith("LFQ")) {
					idList.add(i);
				}
			}
			this.intensityId = new int[idList.size()];
			this.fileNames = new String[idList.size()];
			for (int i = 0; i < fileNames.length; i++) {
				intensityId[i] = idList.get(i);
				fileNames[i] = title[intensityId[i]].substring(0, title[intensityId[i]].length() - "_LFQ".length());
			}
		} else {
			this.intensityId = new int[fileNames.length];
			for (int i = 0; i < title.length; i++) {
				if (title[i].endsWith("LFQ")) {
					for (int j = 0; j < fileNames.length; j++) {
						if (title[i].equals(fileNames[j] + "_LFQ")) {
							intensityId[j] = i;
						}
					}
				}
			}
		}
	}

	protected void parse() {

		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		try {

			int proGroupId = 0;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split(delimiter);
				String proname = cs[proteinId];

				if (proname.contains(REV)) {
					continue;
				}

				double[] intensity = new double[fileNames.length];

				for (int i = 0; i < intensity.length; i++) {
					int id = intensityId[i];
					if (id > -1 && id < cs.length && cs[id].length() > 0) {
						intensity[i] = Double.parseDouble(cs[id]);
					} else {
						intensity[i] = 0;
					}
				}

				if (proname.charAt(0) == '"') {
					String[] pros = proname.substring(1, proname.length() - 1).split(",");
					for (int i = 0; i < pros.length; i++) {
						MetaProtein proI = new MetaProtein(proGroupId, 1 + i, pros[i], 0, 0, 0, intensity);
						list.add(proI);
					}
					proGroupId++;
				} else {
					MetaProtein protein = new MetaProtein(proGroupId++, 1, proname, 0, 0, 0, intensity);
					list.add(protein);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading protein from " + this.getFile(), e);
		}

		this.metaProteins = list.toArray(new MetaProtein[list.size()]);
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return MetaConstants.labelFree;
	}

	@Override
	public MetaProtein[] getMetaProteins() {
		// TODO Auto-generated method stub
		if (metaProteins == null) {
			this.parse();
		}
		return this.metaProteins;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		Object[] titleObjs = new Object[4 + fileNames.length];
		titleObjs[0] = "Group Id";
		titleObjs[1] = "Protein Id";
		titleObjs[2] = this.title[this.proteinId];
		titleObjs[3] = "Intensity";
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + 4] = "Intensity " + fileNames[i];
		}
		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return fileNames;
	}

	public static void main(String[] args) {
		AlphapeptProReader reader = new AlphapeptProReader(
				"Z:\\Kai\\Raw_files\\test\\test\\MetaLab_alphapept\\mag_result\\mag_proteins.csv",
				new String[] { "DDA_10507_1", "DDA_10507_2", "DDA_10507_3" });
		MetaProtein[] pros = reader.getMetaProteins();
		System.out.println(pros.length);
		int count = 0;
		for (int i = 0; i < pros.length; i++) {
			if (pros[i].getProteinId() == 1) {
				count++;
			}
		}
		System.out.println(count);
	}
}
