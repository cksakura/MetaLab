package bmi.med.uOttawa.metalab.dbSearch.fragpipe;

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

public class FragpipeProReader extends AbstractMetaProteinReader {

	protected BufferedReader reader;
	protected int proteinId = -1;
	protected int pepCountId = -1;
	protected int psmCountId = -1;
	protected int scoreId = -1;
	protected int groupId = -1;
	protected String[] fileNames;
	protected int[] intensityId;
	protected String[] title;
	protected String line;

	private MetaProtein[] metaProteins;

	private static Logger LOGGER = LogManager.getLogger(FragpipeProReader.class);

	public FragpipeProReader(String in) {
		this(new File(in));
	}

	public FragpipeProReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + in, e);
		}
		this.parseTitle();
	}

	public FragpipeProReader(String in, String[] fileNames) {
		this(new File(in), fileNames);
	}

	public FragpipeProReader(File in, String[] fileNames) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + in, e);
		}
		this.fileNames = fileNames;
		this.parseTitle();
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		String line = null;
		try {
			line = reader.readLine();

			this.title = line.split("\t");
			if (fileNames == null) {
				ArrayList<Integer> idList = new ArrayList<Integer>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Protein")) {
						proteinId = i;
					} else if (title[i].equals("Protein Probability")) {
						scoreId = i;
					} else if (title[i].equals("Combined Total Peptides")) {
						pepCountId = i;
					} else if (title[i].equals("Combined Spectral Count")) {
						psmCountId = i;
					} else if (title[i].equals("Indistinguishable Proteins")) {
						groupId = i;
					} else if (title[i].endsWith(" Intensity")) {
						idList.add(i);
					}
				}
				this.intensityId = new int[idList.size()];
				this.fileNames = new String[idList.size()];
				for (int i = 0; i < fileNames.length; i++) {
					intensityId[i] = idList.get(i);
					fileNames[i] = title[intensityId[i]].substring(0,
							title[intensityId[i]].length() - " Intensity".length());
				}
			} else {
				this.intensityId = new int[fileNames.length];
				int findCount = 0;
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Protein")) {
						proteinId = i;
					} else if (title[i].equals("Protein Probability")) {
						scoreId = i;
					} else if (title[i].equals("Combined Total Peptides")) {
						pepCountId = i;
					} else if (title[i].equals("Combined Spectral Count")) {
						psmCountId = i;
					} else if (title[i].equals("Indistinguishable Proteins")) {
						groupId = i;
					} else if (title[i].endsWith(" Intensity")) {
						for (int j = 0; j < fileNames.length; j++) {
							if (title[i].equals(fileNames[j] + " Intensity")) {
								intensityId[j] = i;
								findCount++;
							}
						}
					}
				}

				if (findCount != this.intensityId.length) {
					ArrayList<Integer> idList = new ArrayList<Integer>();
					for (int i = 0; i < title.length; i++) {
						if (title[i].equals("Protein")) {
							proteinId = i;
						} else if (title[i].equals("Protein Probability")) {
							scoreId = i;
						} else if (title[i].equals("Combined Total Peptides")) {
							pepCountId = i;
						} else if (title[i].equals("Combined Spectral Count")) {
							psmCountId = i;
						} else if (title[i].equals("Indistinguishable Proteins")) {
							groupId = i;
						} else if (title[i].endsWith(" Intensity")) {
							idList.add(i);
						}
					}
					this.intensityId = new int[idList.size()];
					this.fileNames = new String[idList.size()];
					for (int i = 0; i < fileNames.length; i++) {
						intensityId[i] = idList.get(i);
						fileNames[i] = title[intensityId[i]].substring(0,
								title[intensityId[i]].length() - " Intensity".length());
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe protein quantification result file " + super.getFile(), e);
		}
	}

	protected void parse() {

		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		try {

			int proGroupId = 0;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[fileNames.length];

				for (int i = 0; i < intensity.length; i++) {
					int id = intensityId[i];
					if (id > -1 && id < cs.length) {
						intensity[i] = Double.parseDouble(cs[id]);
					} else {
						intensity[i] = 0;
					}
				}

				String proname = cs[proteinId];
				double proScore = scoreId > 0 ? Double.parseDouble(cs[scoreId]) : 0.0;
				int pepCount = pepCountId > 0 ? Integer.parseInt(cs[pepCountId]) : 0;
				int psmCount = psmCountId > 0 ? Integer.parseInt(cs[psmCountId]) : 0;

				MetaProtein protein = new MetaProtein(proGroupId++, 1, proname, pepCount, psmCount, proScore,
						intensity);
				list.add(protein);

				if (this.groupId > 0 && this.groupId < cs.length && cs[this.groupId].trim().length() > 0) {
					String[] pros = cs[this.groupId].split(", ");
					for (int i = 0; i < pros.length; i++) {
						MetaProtein proI = new MetaProtein(protein.getGroupId(), 2 + i, pros[i], pepCount, psmCount,
								proScore, intensity);
						list.add(proI);
					}
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
		Object[] titleObjs = new Object[7 + fileNames.length];
		titleObjs[0] = "Group Id";
		titleObjs[1] = "Protein Id";
		titleObjs[2] = this.title[this.proteinId];
		titleObjs[3] = this.title[this.pepCountId];
		titleObjs[4] = this.title[this.psmCountId];
		titleObjs[5] = this.title[this.scoreId];
		titleObjs[6] = "Intensity";
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + 7] = "Intensity " + fileNames[i];
		}
		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return fileNames;
	}
}
