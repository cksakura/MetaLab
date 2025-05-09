/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.AbstractMetaProteinReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantProReader extends AbstractMetaProteinReader {

	private BufferedReader reader;
	private String[] title;
	private int proteinId = -1;
	private int pepCountId = -1;
	private int pepCountRazorUniqueId = -1;
	private int pepCountUniqueId = -1;
	private int evalueId = -1;

	private int revId = -1;
	private int contamId = -1;
//	private int pepIdId = -1;

	private String quanMode;

	private String[] intensityTitles;
	private String[] isoIntensityTitles;
	private String[] ms2CountTitles;
	private HashMap<String, Integer> intensityTitleIdMap;
	private HashMap<String, Integer> ms2CountTitleIdMap;

	private String line;

	private MetaProtein[] metaPros;

	private static String spSymbol = "[";

	public MaxquantProReader(String in) {
		this(new File(in));
	}

	public MaxquantProReader(File in) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parseTitle();
	}

	public MaxquantProReader(String in, MetaParameterV1 parameter) {
		this(new File(in), parameter);
	}

	public MaxquantProReader(File in, MetaParameterV1 parameter) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parseTitle(parameter);
	}

	public MaxquantProReader(String in, MetaParameterMQ metaPar) {
		this(new File(in), metaPar);
	}

	public MaxquantProReader(File in, MetaParameterMQ metaPar) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parseTitle(metaPar);
	}

	private void parseTitle() {

		ArrayList<String> intensityTitleList = new ArrayList<String>();
		ArrayList<String> ms2CountTitleList = new ArrayList<String>();

		intensityTitleIdMap = new HashMap<String, Integer>();
		ms2CountTitleIdMap = new HashMap<String, Integer>();

		quanMode = MetaConstants.labelFree;

		String line = null;
		try {
			line = reader.readLine();
			this.title = line.split("\t");

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Protein IDs")) {
					proteinId = i;
				} else if (title[i].equals("Peptide counts (all)")) {
					pepCountId = i;
				} else if (title[i].equals("Peptide counts (razor+unique)")) {
					pepCountRazorUniqueId = i;
				} else if (title[i].equals("Peptide counts (unique)")) {
					pepCountUniqueId = i;
				} else if (title[i].equals("Q-value")) {
					evalueId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else if (title[i].equals("Peptide IDs")) {
//					pepIdId = i;
				} else if (title[i].startsWith("LFQ intensity ")) {
					quanMode = MetaConstants.labelFree;
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				} else if (title[i].equals("Intensity L")) {
					quanMode = MetaConstants.isotopicLabel;
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				} else if (title[i].equals("Intensity M")) {
					quanMode = MetaConstants.isotopicLabel;
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				} else if (title[i].equals("Intensity H")) {
					quanMode = MetaConstants.isotopicLabel;
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				} else if (title[i].startsWith("Reporter intensity corrected ")) {
					quanMode = MetaConstants.isobaricLabel;
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				} else if (title[i].startsWith("MS/MS count ")) {
					ms2CountTitleList.add(title[i]);
					ms2CountTitleIdMap.put(title[i], i);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (intensityTitleList.size() == 0) {
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity ")) {
					intensityTitleList.add(title[i]);
					intensityTitleIdMap.put(title[i], i);
				}
			}
		}

		this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
		this.ms2CountTitles = ms2CountTitleList.toArray(new String[ms2CountTitleList.size()]);
	}

	private void parseTitle(MetaParameterV1 parameter) {

		String[] paraExps = parameter.getRawFiles()[1];
		this.quanMode = parameter.getQuanMode();
		String[][] isotopeLabels = parameter.getLabels();
		int isobaricLabelCount = parameter.getIsobaricLabelCount();
		int quanResultType = parameter.getQuanResultType();

		ArrayList<String> intensityTitleList = new ArrayList<String>();

		switch (quanResultType) {
		case MetaParameterV1.quanResultCombine: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0].length() > 0) {
					intensityTitleList.add("Intensity L");
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0].length() > 0) {
					intensityTitleList.add("Intensity M");
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0].length() > 0) {
					intensityTitleList.add("Intensity H");
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < isobaricLabelCount; i++) {
					intensityTitleList.add("Reporter intensity corrected " + (i + 1));
				}
			}

			break;
		}
		case MetaParameterV1.quanResultSeparate: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0].length() > 0) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity L " + paraExps[i]);
					}
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0].length() > 0) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity M " + paraExps[i]);
					}
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0].length() > 0) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity H " + paraExps[i]);
					}
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < isobaricLabelCount; i++) {
					for (int j = 0; j < paraExps.length; j++) {
						intensityTitleList.add("Reporter intensity corrected " + (i + 1) + " " + paraExps[j]);
					}
				}
			}

			break;
		}
		case MetaParameterV1.quanResultAll: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0].length() > 0) {
					intensityTitleList.add("Intensity L");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity L " + paraExps[i]);
					}
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0].length() > 0) {
					intensityTitleList.add("Intensity M");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity M " + paraExps[i]);
					}
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0].length() > 0) {
					intensityTitleList.add("Intensity H");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity H " + paraExps[i]);
					}
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < isobaricLabelCount; i++) {
					intensityTitleList.add("Reporter intensity corrected " + (i + 1));
					for (int j = 0; j < paraExps.length; j++) {
						intensityTitleList.add("Reporter intensity corrected " + (i + 1) + " " + paraExps[j]);
					}
				}
			}

			break;
		}
		}

		intensityTitleIdMap = new HashMap<String, Integer>();
		ms2CountTitleIdMap = new HashMap<String, Integer>();

		ArrayList<String> ms2CountTitleList = new ArrayList<String>();
		if (quanMode.equals(MetaConstants.labelFree)) {
			for (int i = 0; i < paraExps.length; i++) {
				intensityTitleList.add("LFQ intensity " + paraExps[i]);
				intensityTitleIdMap.put("LFQ intensity " + paraExps[i], -1);
				ms2CountTitleList.add("MS/MS count " + paraExps[i]);
				ms2CountTitleIdMap.put("MS/MS count " + paraExps[i], -1);
			}
		} else {
			for (String intensityTitle : intensityTitleList) {
				intensityTitleIdMap.put(intensityTitle, -1);
			}
		}

		String line = null;
		try {
			line = reader.readLine();
			String[] title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Protein IDs")) {
					proteinId = i;
				} else if (title[i].equals("Peptide counts (all)")) {
					pepCountId = i;
				} else if (title[i].equals("Peptide counts (razor+unique)")) {
					pepCountRazorUniqueId = i;
				} else if (title[i].equals("Peptide counts (unique)")) {
					pepCountUniqueId = i;
				} else if (title[i].equals("Q-value")) {
					evalueId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else {
					if (intensityTitleIdMap.containsKey(title[i])) {
						intensityTitleIdMap.put(title[i], i);
					}
					if (ms2CountTitleIdMap.containsKey(title[i])) {
						ms2CountTitleIdMap.put(title[i], i);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
		this.ms2CountTitles = ms2CountTitleList.toArray(new String[ms2CountTitleList.size()]);
	}

	private void parseTitle(MetaParameterMQ metaPar) {

		MetaData metadata = metaPar.getMetadata();
		String[] paraExps = metadata.getExpNames();
		this.quanMode = metaPar.getQuanMode();
		MaxquantModification[][] isotopeLabels = metaPar.getLabels();
		int isobaricLabelCount = metadata.getLabelTitle().length;
		int quanResultType = metaPar.isCombineLabel() ? MetaParameterV1.quanResultCombine
				: MetaParameterV1.quanResultSeparate;

		ArrayList<String> intensityTitleList = new ArrayList<String>();

		switch (quanResultType) {
		case MetaParameter.quanResultCombine: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0] != null) {
					intensityTitleList.add("Intensity L");
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0] != null) {
					intensityTitleList.add("Intensity M");
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0] != null) {
					intensityTitleList.add("Intensity H");
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < isobaricLabelCount; i++) {
					intensityTitleList.add("Reporter intensity corrected " + (i + 1));
				}
			}

			break;
		}
		case MetaParameter.quanResultSeparate: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0] != null) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity L " + paraExps[i]);
					}
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0] != null) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity M " + paraExps[i]);
					}
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0] != null) {
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity H " + paraExps[i]);
					}
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < paraExps.length; i++) {
					for (int j = 0; j < isobaricLabelCount; j++) {
						intensityTitleList.add("Reporter intensity corrected " + (j + 1) + " " + paraExps[i]);
					}
				}
			}

			break;
		}
		case MetaParameter.quanResultAll: {

			if (quanMode.equals(MetaConstants.isotopicLabel)) {
				if (isotopeLabels[0].length > 0 && isotopeLabels[0][0] != null) {
					intensityTitleList.add("Intensity L");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity L " + paraExps[i]);
					}
				}
				if (isotopeLabels[1].length > 0 && isotopeLabels[1][0] != null) {
					intensityTitleList.add("Intensity M");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity M " + paraExps[i]);
					}
				}
				if (isotopeLabels[2].length > 0 && isotopeLabels[2][0] != null) {
					intensityTitleList.add("Intensity H");
					for (int i = 0; i < paraExps.length; i++) {
						intensityTitleList.add("Intensity H " + paraExps[i]);
					}
				}
			}

			if (quanMode.equals(MetaConstants.isobaricLabel)) {
				for (int i = 0; i < isobaricLabelCount; i++) {
					intensityTitleList.add("Reporter intensity corrected " + (i + 1));
					for (int j = 0; j < paraExps.length; j++) {
						intensityTitleList.add("Reporter intensity corrected " + (i + 1) + " " + paraExps[j]);
					}
				}
			}

			break;
		}
		}

		intensityTitleIdMap = new HashMap<String, Integer>();
		ms2CountTitleIdMap = new HashMap<String, Integer>();

		ArrayList<String> ms2CountTitleList = new ArrayList<String>();
		if (quanMode.equals(MetaConstants.labelFree)) {
			for (int i = 0; i < paraExps.length; i++) {
				intensityTitleList.add("LFQ intensity " + paraExps[i]);
				intensityTitleIdMap.put("LFQ intensity " + paraExps[i], -1);
				ms2CountTitleList.add("MS/MS count " + paraExps[i]);
				ms2CountTitleIdMap.put("MS/MS count " + paraExps[i], -1);
			}
		} else {
			for (String intensityTitle : intensityTitleList) {
				intensityTitleIdMap.put(intensityTitle, -1);
			}
		}

		String line = null;
		try {
			line = reader.readLine();
			String[] title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Protein IDs")) {
					proteinId = i;
				} else if (title[i].equals("Peptide counts (all)")) {
					pepCountId = i;
				} else if (title[i].equals("Peptide counts (razor+unique)")) {
					pepCountRazorUniqueId = i;
				} else if (title[i].equals("Peptide counts (unique)")) {
					pepCountUniqueId = i;
				} else if (title[i].equals("Q-value")) {
					evalueId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else {
					if (intensityTitleIdMap.containsKey(title[i])) {
						intensityTitleIdMap.put(title[i], i);
					}
					if (ms2CountTitleIdMap.containsKey(title[i])) {
						ms2CountTitleIdMap.put(title[i], i);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (quanMode.equals(MetaConstants.labelFree)) {
			this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);

		} else if (quanMode.equals(MetaConstants.isobaricLabel) && metaPar.getIsobaricTag() != null) {

			String[] expNames = metadata.getExpNames();
			String[] labelTitle = metadata.getLabelTitle();
			String[] labelExpNames = metadata.getLabelExpNames();

			HashMap<String, String> map = new HashMap<String, String>();

			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					map.put("Reporter intensity corrected " + (j + 1) + " " + expNames[i],
							labelExpNames[i * labelTitle.length + j]);
				}
			}

			ArrayList<String> templist = new ArrayList<String>();
			ArrayList<String> isolist = new ArrayList<String>();
			for (int i = 0; i < intensityTitleList.size(); i++) {
				String titlei = intensityTitleList.get(i);
				if (intensityTitleIdMap.get(titlei) > -1) {
					if (map.containsKey(titlei)) {
						templist.add(titlei);
						isolist.add("Intensity " + map.get(titlei));
					} else {
						templist.add(titlei);
						isolist.add(titlei);
					}
				}
			}
			this.intensityTitles = templist.toArray(new String[templist.size()]);
			this.isoIntensityTitles = isolist.toArray(new String[isolist.size()]);

		} else {
			ArrayList<String> templist = new ArrayList<String>();
			for (int i = 0; i < intensityTitleList.size(); i++) {
				String titlei = intensityTitleList.get(i);
				if (intensityTitleIdMap.get(titlei) > -1) {
					templist.add(titlei);
				}
			}
			this.intensityTitles = templist.toArray(new String[templist.size()]);
		}

		this.ms2CountTitles = ms2CountTitleList.toArray(new String[ms2CountTitleList.size()]);
	}

	private void parse() throws NumberFormatException, IOException {

		int id = 1;
		ArrayList<MetaProtein> prolist = new ArrayList<MetaProtein>();
		while ((line = reader.readLine()) != null) {

			String[] cs = line.split("\t");
			boolean reverse = false;
			boolean contaminant = false;

			if (revId > -1 && revId < cs.length) {
				reverse = cs[revId].equals("+");
			}

			if (contamId > -1 && contamId < cs.length) {
				contaminant = cs[contamId].equals("+");
			}

			if (reverse || contaminant) {
				continue;
			}

			String[] proids = cs[proteinId].split(";");
			String[] pepCounts = cs[pepCountId].split(";");
			String[] razorUniPepCounts = cs[pepCountRazorUniqueId].split(";");
			String[] uniPepCounts = cs[pepCountUniqueId].split(";");
			double evalue = evalueId > -1 ? Double.parseDouble(cs[evalueId]) : -1;

			double[] intensity = new double[intensityTitles.length];
			int[] ms2Count = new int[ms2CountTitles.length];

			for (int i = 0; i < intensity.length; i++) {
				int titleid = this.intensityTitleIdMap.get(intensityTitles[i]);
				if (titleid > -1 && titleid < cs.length) {
					intensity[i] = Double.parseDouble(cs[titleid]);
				}
			}

			for (int i = 0; i < ms2Count.length; i++) {
				int titleid = this.ms2CountTitleIdMap.get(ms2CountTitles[i]);
				if (titleid > -1 && titleid < cs.length) {
					ms2Count[i] = Integer.parseInt(cs[titleid]);
				}
			}

			if (ms2Count.length == 0) {
				ms2Count = new int[intensity.length];
				Arrays.fill(ms2Count, 0);
			}

			for (int i = 0; i < proids.length; i++) {
				String proname = proids[i];
				int spid = proname.indexOf(spSymbol);
				if (spid > 0) {
					proname = proname.substring(0, spid);
				}
				MetaProtein metaprotein = new MetaProtein(id, (i + 1), proname, Integer.parseInt(pepCounts[i]),
						Integer.parseInt(razorUniPepCounts[i]), Integer.parseInt(uniPepCounts[i]), evalue, ms2Count,
						intensity);
				prolist.add(metaprotein);
			}

			id++;
		}
		this.metaPros = prolist.toArray(new MetaProtein[prolist.size()]);
	}

	public MetaProtein[] getMetaProteins() {
		if (this.metaPros == null) {
			try {
				parse();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.metaPros;
	}

	/*
	 * public String[] getExperiments() { return experiments; }
	 */

	public String[] getIntensityTitle() {
		if (this.isoIntensityTitles != null) {
			return isoIntensityTitles;
		}
		return intensityTitles;
	}

	public String[] getMs2CountTitles() {
		return ms2CountTitles;
	}

	public void close() throws IOException {
		this.reader.close();
	}

	public String getQuanMode() {
		// TODO Auto-generated method stub
		return quanMode;
	}

	public Object[] getTitleObjs() {
		Object[] titleObjs = new Object[5 + intensityTitles.length];
		titleObjs[0] = "Name";
		titleObjs[1] = this.title[this.pepCountId];
		titleObjs[2] = this.title[this.pepCountRazorUniqueId];
		titleObjs[3] = this.title[this.pepCountRazorUniqueId];
		titleObjs[4] = this.title[this.evalueId];
		for (int i = 5; i < titleObjs.length; i++) {
			titleObjs[i] = intensityTitles[i - 5];
		}
		return titleObjs;
	}

}
