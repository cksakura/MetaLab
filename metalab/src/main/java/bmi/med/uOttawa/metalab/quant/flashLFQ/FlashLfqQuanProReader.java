package bmi.med.uOttawa.metalab.quant.flashLFQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.AbstractMetaProteinReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class FlashLfqQuanProReader extends AbstractMetaProteinReader {

	protected BufferedReader reader;
	protected int proteinId = -1;
	protected String[] intensityTitles;
	protected HashMap<String, Integer> intensityTitleIdMap;
	protected String[] title;
	protected String line;

	private static Logger LOGGER = LogManager.getLogger(FlashLfqQuanProReader.class);

	public FlashLfqQuanProReader(String in) {
		this(new File(in));
		// TODO Auto-generated constructor stub
	}

	public FlashLfqQuanProReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + in, e);
		}
		this.parseTitle();
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> intensityTitleList = new ArrayList<String>();

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}

		this.title = line.split("\t");
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Protein Groups")) {
				proteinId = i;
			} else if (title[i].startsWith("Intensity_")) {
				intensityTitleIdMap.put(title[i], i);
				intensityTitleList.add(title[i]);
			}
		}

		this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
	}

	protected FlashLfqQuanProtein parse() {
		String[] cs = line.split("\t");
		double[] intensity = new double[intensityTitles.length];

		for (int i = 0; i < intensity.length; i++) {
			int id = this.intensityTitleIdMap.get(intensityTitles[i]);
			if (id > -1 && id < cs.length) {
				intensity[i] = Double.parseDouble(cs[id]);
			} else {
				intensity[i] = 0;
			}
		}

		String proname = cs[proteinId].split("[\\s]")[0];
		FlashLfqQuanProtein protein = new FlashLfqQuanProtein(proname, intensity);

		return protein;
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return MetaConstants.labelFree;
	}

	@Override
	public MetaProtein[] getMetaProteins() {
		// TODO Auto-generated method stub
		ArrayList<FlashLfqQuanProtein> list = new ArrayList<FlashLfqQuanProtein>();
		try {
			while ((line = reader.readLine()) != null) {
				FlashLfqQuanProtein pro = parse();
				if (pro != null) {
					list.add(pro);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading protein from " + this.getFile(), e);
		}

		FlashLfqQuanProtein[] pros = list.toArray(new FlashLfqQuanProtein[list.size()]);
		return pros;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		Object[] titleObjs = new Object[2 + intensityTitles.length];
		titleObjs[0] = this.title[this.proteinId];
		titleObjs[1] = "Intensity";
		for (int i = 0; i < intensityTitles.length; i++) {
			titleObjs[i + 2] = intensityTitles[i];
		}

		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return intensityTitles;
	}

}
