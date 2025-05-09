/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantPepReader extends AbstractMetaPeptideReader {

	private BufferedReader reader;
	private int sequenceId = -1;
	private int chargeId = -1;
	private int lengthId = -1;
	private int missCleaveId = -1;
	private int massId = -1;
	private int pepId = -1;
	private int scoreId = -1;
	private int proteinId = -1;
	private int ms2CountId = -1;
	private int revId = -1;
	private int contamId = -1;

	private boolean hasIntensity = false;
	private String[] title;

	private String quanMode;
	private String[] intensityTitles;
	private String[] isoIntensityTitles;

	private HashMap<String, Integer> intensityTitleIdMap;

	private String line;

	private static Logger LOGGER = LogManager.getLogger(MaxquantPepReader.class);

	public MaxquantPepReader(String in, MetaParameterV1 parameter) {
		this(new File(in), parameter);
	}

	public MaxquantPepReader(File in, MetaParameterV1 parameter) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + in, e);
		}
		this.parseTitle(parameter);
	}

	public MaxquantPepReader(String in, MetaParameterMQ parameter) {
		this(new File(in), parameter);
	}

	public MaxquantPepReader(File in, MetaParameterMQ parameter) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + in, e);
		}
		this.parseTitle(parameter);
	}

	public MaxquantPepReader(String in) {
		this(new File(in));
	}

	public MaxquantPepReader(File in) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + in, e);
		}
		this.parseTitle();
	}

	public MaxquantPepReader(File in, File meta) {
		super(in);
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + in, e);
		}
		this.parseTitle();
	}

	private void parseTitle(MetaParameterV1 parameter) {
		String[] paraExps = parameter.getRawFiles()[1];
		String[][] isotopeLabels = parameter.getLabels();
		int isobaricLabelCount = parameter.getIsobaricLabelCount();
		int quanResultType = parameter.getQuanResultType();
		this.quanMode = parameter.getQuanMode();

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

		HashMap<String, Integer> lfqIntensityMap = new HashMap<String, Integer>();
		ArrayList<String> lfqIntensityList = new ArrayList<String>();
		if (quanMode.equals(MetaConstants.labelFree)) {
			intensityTitleIdMap = new HashMap<String, Integer>();
			for (int i = 0; i < paraExps.length; i++) {
				intensityTitleList.add("Intensity " + paraExps[i]);
				intensityTitleIdMap.put("Intensity " + paraExps[i], -1);

				lfqIntensityList.add("LFQ intensity " + paraExps[i]);
				lfqIntensityMap.put("LFQ intensity " + paraExps[i], -1);
			}
		} else {
			intensityTitleIdMap = new HashMap<String, Integer>();
			for (String intensityTitle : intensityTitleList) {
				intensityTitleIdMap.put(intensityTitle, -1);
			}
		}

		boolean hasLFQ = false;
		String line = null;
		try {
			line = reader.readLine();
			title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence") || title[i].substring(1).equals("Sequence")) {
					sequenceId = i;
				} else if (title[i].equals("Charges")) {
					chargeId = i;
				} else if (title[i].equals("Length")) {
					lengthId = i;
				} else if (title[i].equals("Missed cleavages")) {
					if (parameter.getDigestMode() == MaxquantEnzyme.specific) {
						missCleaveId = i;
					}
				} else if (title[i].equals("Mass")) {
					massId = i;
				} else if (title[i].equals("Proteins")) {
					proteinId = i;
				} else if (title[i].equals("PEP")) {
					pepId = i;
				} else if (title[i].equals("Score")) {
					scoreId = i;
				} else if (title[i].equals("MS/MS Count")) {
					ms2CountId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else if (title[i].equals("T: Sequence")) {
					if (sequenceId == -1) {
						sequenceId = i;
					}
				} else {
					if (intensityTitleIdMap.containsKey(title[i])) {
						intensityTitleIdMap.put(title[i], i);
					}
					if (lfqIntensityMap.containsKey(title[i])) {
						hasLFQ = true;
						lfqIntensityMap.put(title[i], i);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + super.getFile(), e);
		}

		if (quanMode.equals(MetaConstants.labelFree) && hasLFQ) {
			this.intensityTitles = lfqIntensityList.toArray(new String[intensityTitleList.size()]);
			this.intensityTitleIdMap = lfqIntensityMap;
		} else {
			this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
		}

		if (intensityTitles.length > 0) {
			hasIntensity = true;
		}
	}

	private void parseTitle(MetaParameterMQ parameter) {
		MetaData metadata = parameter.getMetadata();
		String[] paraExps = metadata.getExpNames();
		MaxquantModification[][] isotopeLabels = parameter.getLabels();
		int isobaricLabelCount = metadata.getLabelTitle().length;
		int quanResultType = parameter.isCombineLabel() ? MetaParameter.quanResultCombine
				: MetaParameter.quanResultSeparate;
		this.quanMode = parameter.getQuanMode();

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

		HashMap<String, Integer> lfqIntensityMap = new HashMap<String, Integer>();
		ArrayList<String> lfqIntensityList = new ArrayList<String>();
		if (quanMode.equals(MetaConstants.labelFree)) {
			intensityTitleIdMap = new HashMap<String, Integer>();
			for (int i = 0; i < paraExps.length; i++) {
				intensityTitleList.add("Intensity " + paraExps[i]);
				intensityTitleIdMap.put("Intensity " + paraExps[i], -1);

				lfqIntensityList.add("LFQ intensity " + paraExps[i]);
				lfqIntensityMap.put("LFQ intensity " + paraExps[i], -1);
			}
		} else {
			intensityTitleIdMap = new HashMap<String, Integer>();
			for (String intensityTitle : intensityTitleList) {
				intensityTitleIdMap.put(intensityTitle, -1);
			}
		}

		boolean hasLFQ = false;
		String line = null;
		try {
			line = reader.readLine();
			title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence") || title[i].substring(1).equals("Sequence")) {
					sequenceId = i;
				} else if (title[i].equals("Charges")) {
					chargeId = i;
				} else if (title[i].equals("Length")) {
					lengthId = i;
				} else if (title[i].equals("Missed cleavages")) {
					if (parameter.getDigestMode() == MaxquantEnzyme.specific) {
						missCleaveId = i;
					}
				} else if (title[i].equals("Mass")) {
					massId = i;
				} else if (title[i].equals("Proteins")) {
					proteinId = i;
				} else if (title[i].equals("PEP")) {
					pepId = i;
				} else if (title[i].equals("Score")) {
					scoreId = i;
				} else if (title[i].equals("MS/MS Count")) {
					ms2CountId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else if (title[i].equals("T: Sequence")) {
					if (sequenceId == -1) {
						sequenceId = i;
					}
				} else {
					if (intensityTitleIdMap.containsKey(title[i])) {
						intensityTitleIdMap.put(title[i], i);
					}
					if (lfqIntensityMap.containsKey(title[i])) {
						hasLFQ = true;
						lfqIntensityMap.put(title[i], i);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + super.getFile(), e);
		}

		if (quanMode.equals(MetaConstants.labelFree) && hasLFQ) {
			this.intensityTitles = lfqIntensityList.toArray(new String[intensityTitleList.size()]);
			this.intensityTitleIdMap = lfqIntensityMap;

		} else if (quanMode.equals(MetaConstants.isobaricLabel) && parameter.getIsobaricTag() != null) {

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

		if (intensityTitles.length > 0) {
			hasIntensity = true;
		}
	}

	private void parseTitle() {

		ArrayList<String> explist = new ArrayList<String>();
		intensityTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> intensityTitleList = new ArrayList<String>();
		quanMode = MetaConstants.labelFree;

		String line = null;
		try {
			line = reader.readLine();
			this.title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence") || title[i].substring(1).equals("Sequence")) {
					sequenceId = i;
				} else if (title[i].equals("Charges")) {
					chargeId = i;
				} else if (title[i].equals("Length")) {
					lengthId = i;
				} else if (title[i].equals("Missed cleavages")) {
					missCleaveId = i;
				} else if (title[i].equals("Mass")) {
					massId = i;
				} else if (title[i].equals("Proteins")) {
					proteinId = i;
				} else if (title[i].equals("PEP")) {
					pepId = i;
				} else if (title[i].equals("Score")) {
					scoreId = i;
				} else if (title[i].equals("MS/MS Count")) {
					ms2CountId = i;
				} else if (title[i].equals("Reverse")) {
					revId = i;
				} else if (title[i].equals("Potential contaminant")) {
					contamId = i;
				} else if (title[i].startsWith("LFQ intensity ")) {
					quanMode = MetaConstants.labelFree;
				} else if (title[i].startsWith("Reporter intensity corrected ")) {
					quanMode = MetaConstants.isobaricLabel;
				} else if (title[i].startsWith("Experiment ")) {
					int loc = title[i].indexOf(" ");
					if (loc < title[i].length()) {
						explist.add(title[i].substring(loc + 1, title[i].length()));
					} else {
						explist.add("");
					}
				} else if (title[i].equals("T: Sequence")) {
					if (sequenceId == -1) {
						sequenceId = i;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + super.getFile(), e);
		}

		if (quanMode.equals(MetaConstants.labelFree)) {
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("LFQ intensity ")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
				}
			}
		} else if (quanMode.equals(MetaConstants.isobaricLabel)) {
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Reporter intensity corrected ")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
				}
			}
		} else {
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Intensity L")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
					quanMode = MetaConstants.isotopicLabel;
				} else if (title[i].equals("Intensity M")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
					quanMode = MetaConstants.isotopicLabel;
				} else if (title[i].equals("Intensity H")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
					quanMode = MetaConstants.isotopicLabel;
				}
			}
		}

		if (intensityTitleList.size() == 0) {
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity ")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i]);
				}
			}
		}

		this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);

		if (intensityTitles.length > 0) {
			hasIntensity = true;
		} else {
			hasIntensity = false;
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Experiment ")) {
					intensityTitleIdMap.put(title[i], i);
					intensityTitleList.add(title[i].substring(title[i].indexOf(" ") + 1));
				}
			}
			this.intensityTitles = intensityTitleList.toArray(new String[intensityTitleList.size()]);
		}
	}

	@Override
	public MetaPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub

		ArrayList<MaxquantPep4Meta> list = new ArrayList<MaxquantPep4Meta>();
		try {
			while ((line = reader.readLine()) != null) {
				MaxquantPep4Meta mp = parse();
				if (mp != null && mp.isUse()) {
					list.add(mp);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MaxquantPep4Meta[] peps = list.toArray(new MaxquantPep4Meta[list.size()]);
		return peps;
	}

	private MaxquantPep4Meta parse() {
		String[] cs = line.split("\t");
		if (cs.length != title.length) {
			return null;
		}
		String[] ps = proteinId == -1 ? new String[] {} : cs[proteinId].split(";");
		String[] proteins = new String[ps.length];
		for (int i = 0; i < proteins.length; i++) {
			proteins[i] = ps[i];
		}

		int[] charge = new int[] {};
		if (chargeId > -1 && cs[chargeId].trim().length() > 0) {
			String[] ccs = cs[chargeId].split(";");
			if (ccs.length > 0) {
				charge = new int[ccs.length];
				for (int i = 0; i < charge.length; i++) {
					charge[i] = Integer.parseInt(ccs[i]);
				}
			}
		}

		double pep = pepId == -1 ? -1 : Double.parseDouble(cs[pepId]);
		double score = scoreId == -1 ? -1 : Double.parseDouble(cs[scoreId]);
		int length = lengthId == -1 ? -1 : Integer.parseInt(cs[lengthId]);
		int missCleave = missCleaveId == -1 ? -1 : Integer.parseInt(cs[missCleaveId]);
		double mass = massId == -1 ? -1 : Double.parseDouble(cs[massId]);
		int count = ms2CountId == -1 ? -1 : Integer.parseInt(cs[ms2CountId]);
		boolean reverse = false;
		boolean contaminant = false;

		if (revId > -1 && revId < cs.length) {
			reverse = cs[revId].equals("+");
		}

		if (contamId > -1 && contamId < cs.length) {
			contaminant = cs[contamId].equals("+");
		}

		double[] intensity = new double[intensityTitles.length];

		if (hasIntensity) {

			for (int i = 0; i < intensity.length; i++) {
				int id = this.intensityTitleIdMap.get(intensityTitles[i]);
				if (id > -1 && id < cs.length) {
					intensity[i] = Double.parseDouble(cs[id]);
				}
			}
		} else {
			Arrays.fill(intensity, 0.0);
		}

		MaxquantPep4Meta mpep = new MaxquantPep4Meta(cs[sequenceId], length, charge, missCleave, mass, score, count,
				pep, proteins, reverse, contaminant, intensity);

		return mpep;
	}

	public String[] getIntensityTitle() {
		if (this.isoIntensityTitles != null) {
			return isoIntensityTitles;
		}
		return intensityTitles;
	}

	public String[] getIsoIntensityTitles() {
		return isoIntensityTitles;
	}

	public String getQuanMode() {
		// TODO Auto-generated method stub
		return quanMode;
	}

	public boolean hasIntensity() {
		return hasIntensity;
	}

	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant search result file " + super.getFile(), e);
		}
	}

	public Object[] getTitleObjs() {

		Object[] titleObjs = new Object[10 + intensityTitles.length];
		titleObjs[0] = this.title[this.sequenceId];
		titleObjs[1] = this.title[this.lengthId];
		titleObjs[2] = this.title[this.missCleaveId];
		titleObjs[3] = this.title[this.massId];
		titleObjs[4] = this.title[this.proteinId];
		titleObjs[5] = this.title[this.chargeId];
		titleObjs[6] = this.title[this.scoreId];
		titleObjs[7] = this.title[this.pepId];
		titleObjs[8] = "Intensity";
		for (int i = 0; i < intensityTitles.length; i++) {
			titleObjs[i + 9] = intensityTitles[i];
		}
		titleObjs[intensityTitles.length + 9] = this.title[this.ms2CountId];

		return titleObjs;
	}

	@SuppressWarnings("unused")
	private static void convertPep2Report(File peptides, File peptideReport, MetaParameterMQ metaPar)
			throws IOException {
		MaxquantPepReader pepReader = new MaxquantPepReader(peptides, metaPar);
		PrintWriter writer = new PrintWriter(peptideReport);

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Sequence").append("\t");
		titlesb.append("Length").append("\t");
		titlesb.append("Missed cleavages").append("\t");
		titlesb.append("Proteins").append("\t");
		titlesb.append("Charges").append("\t");
		titlesb.append("PEP").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Intensity").append("\t");

		String[] expNames = pepReader.getIsoIntensityTitles();
		for (int i = 0; i < expNames.length; i++) {
			titlesb.append(expNames[i]).append("\t");
		}

		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id");
		titlesb.append("MS/MS Count").append("\t");

		writer.println(titlesb);

		MetaPeptide[] peps = pepReader.getMetaPeptides();
		for (int i = 0; i < peps.length; i++) {
			MaxquantPep4Meta pep = (MaxquantPep4Meta) peps[i];
			StringBuilder sb = new StringBuilder();

			sb.append(pep.getSequence()).append("\t");
			sb.append(pep.getSequence().length()).append("\t");
			sb.append(pep.getMissCleave()).append("\t");
			sb.append(pep.getProtein()).append("\t");
			sb.append(pep.getChargeString()).append("\t");
			sb.append(pep.getPEP()).append("\t");
			sb.append(pep.getScore()).append("\t");

			double[] intensity = pep.getIntensity();
			sb.append((int) MathTool.getTotal(intensity)).append("\t");
			for (int j = 0; j < intensity.length; j++) {
				sb.append((int) intensity[j]).append("\t");
			}

			if (pep.isReverse()) {
				sb.append("+").append("\t");
			} else {
				sb.append("").append("\t");
			}

			if (pep.isContaminant()) {
				sb.append("+").append("\t");
			} else {
				sb.append("").append("\t");
			}

			sb.append(i + 1).append("\t");
			sb.append(pep.getTotalMS2Count());

			writer.println(sb);
		}

		pepReader.close();
		writer.close();
	}

	public void filter() {

		HashMap<String, HashSet<String>> totalPepProMap = new HashMap<String, HashSet<String>>();
		HashSet<String> proSet = new HashSet<String>();

		try {
			while ((line = reader.readLine()) != null) {
				MaxquantPep4Meta mp = parse();
				if (mp.isUse()) {
					String sequence = mp.getSequence();
					String[] pros = mp.getProteins();

					for (String pro : pros) {
						proSet.add(pro);

						if (totalPepProMap.containsKey(sequence)) {
							totalPepProMap.get(sequence).add(pro);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pro);
							totalPepProMap.put(sequence, set);
						}
					}

				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(proSet.size() + "\t" + totalPepProMap.size());

		HashMap<String, ProteinGroup> proGroupMap = this.getProteinGroups(totalPepProMap, proSet);

		System.out.println(proGroupMap.size());
	}

	/**
	 * get razor protein groups
	 * 
	 * @param proPepMap
	 * @param finalPepProMap
	 * @return
	 */
	private HashMap<String, ProteinGroup> getProteinGroups(HashMap<String, HashSet<String>> totalPepProMap,
			HashSet<String> razorProSet) {

		HashMap<String, HashSet<String>> tempPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			HashSet<String> tempProSet = new HashSet<String>();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!razorProSet.contains(pro)) {
					proIt.remove();
				} else {
					if (tempProPepMap.containsKey(pro)) {
						tempProPepMap.get(pro).add(pep);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pep);
						tempProPepMap.put(pro, set);
					}
					tempProSet.add(pro);
				}
			}
			tempPepProMap.put(pep, tempProSet);
		}

		HashSet<String> uniqueProSet = new HashSet<String>();
		HashSet<String> uniquePepSet = new HashSet<String>();
		for (String pro : tempProPepMap.keySet()) {
			HashSet<String> pepSet = tempProPepMap.get(pro);
			for (String pep : pepSet) {
				HashSet<String> proSet = tempPepProMap.get(pep);
				if (proSet.size() == 1) {
					uniqueProSet.add(pro);
					uniquePepSet.addAll(pepSet);
					break;
				}
			}
		}

		HashMap<String, ProteinGroup> uniquePgMap = new HashMap<String, ProteinGroup>(uniqueProSet.size());
		HashMap<String, ProteinGroup> pgMap = new HashMap<String, ProteinGroup>(tempProPepMap.size());
		HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
		for (String pro : tempProPepMap.keySet()) {
			if (uniqueProSet.contains(pro)) {
				uniquePgMap.put(pro, new ProteinGroup(pro));
			} else {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				HashSet<String> tempPepSet = new HashSet<String>();
				for (String pep : pepSet) {
					if (!uniquePepSet.contains(pep)) {
						tempPepSet.add(pep);
					}
				}

				if (tempPepSet.size() > 0) {
					tempProPepMap2.put(pro, tempPepSet);
					for (String pep : tempPepSet) {
						if (tempPepProMap2.containsKey(pep)) {
							tempPepProMap2.get(pep).add(pro);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pro);
							tempPepProMap2.put(pep, set);
						}
					}
					pgMap.put(pro, new ProteinGroup(pro));
				}
			}
		}

		combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!uniquePgMap.containsKey(pro)) {
					proIt.remove();
				}
			}
		}

		return uniquePgMap;
	}

	private void combineProteinGroups(HashMap<String, ProteinGroup> pgMap,
			HashMap<String, HashSet<String>> tempProPepMap, HashMap<String, HashSet<String>> tempPepProMap,
			HashMap<String, ProteinGroup> uniquePgMap) {

		// combine protein groups
		for (String pep : tempPepProMap.keySet()) {

			HashSet<String> proSet = tempPepProMap.get(pep);

			String[] pros = proSet.toArray(new String[proSet.size()]);

			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					if (tempProPepMap.get(o1).size() > tempProPepMap.get(o2).size()) {
						return -1;
					}
					if (tempProPepMap.get(o1).size() < tempProPepMap.get(o2).size()) {
						return 1;
					}
					return o1.compareTo(o2);
				}
			});

			for (int i = 0; i < pros.length; i++) {

				if (pgMap.containsKey(pros[i])) {
					ProteinGroup pg = pgMap.get(pros[i]);

					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);

					for (int j = i + 1; j < pros.length; j++) {

						if (pgMap.containsKey(pros[j])) {
							HashSet<String> pepSetJ = tempProPepMap.get(pros[j]);

							HashSet<String> pepAll = new HashSet<String>(pepSetI.size() + pepSetJ.size());
							pepAll.addAll(pepSetI);
							pepAll.addAll(pepSetJ);

							if (pepAll.size() == pepSetI.size()) {
								if (pepSetI.size() == pepSetJ.size()) {
									pg.addSameGroup(pros[j]);
								}
								pgMap.remove(pros[j]);
							}
						}
					}
				}
			}
		}

		if (pgMap.size() == tempProPepMap.size()) {

			String[] pros = pgMap.keySet().toArray(new String[pgMap.size()]);
			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					int size1 = tempProPepMap.get(o1).size();
					int size2 = tempProPepMap.get(o2).size();

					if (size1 < size2) {
						return -1;
					}
					if (size1 > size2) {
						return 1;
					}
					return 0;
				}
			});

			// remove subset proteins
			for (int i = 0; i < pros.length; i++) {
				boolean subset = true;
				HashSet<String> pepSet = tempProPepMap.get(pros[i]);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						subset = false;
					}
				}
				if (subset) {
					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);
					for (String pep : pepSetI) {
						HashSet<String> proSet = tempPepProMap.get(pep);
						proSet.remove(pros[i]);
					}
					tempProPepMap.remove(pros[i]);
					pgMap.remove(pros[i]);
				}
			}

			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}

		} else {

			// remove redundant proteins
			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			// find unique proteins
			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}
		}
	}

	private class ProteinGroup {
		@SuppressWarnings("unused")
		String proName;
		HashSet<String> sameSet;

		private ProteinGroup(String proName) {
			this.proName = proName;
			this.sameSet = new HashSet<String>();
		}

		private void addSameGroup(String pro) {
			this.sameSet.add(pro);
		}
	}
}
