package bmi.med.uOttawa.metalab.task.par;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MetaData {

	private String[] rawFiles;
	private String[] expNames;
	private int[] fractions;
	private int[] replicates;

	private int metaTypeCount;
	private String[][] metaInfo;
	
	private String[] labelTitle;
	private String[] labelExpNames;
	
	private int refChannelId = -1;
	private String[] isobaricReference;
	private boolean[] selectRef;

	public MetaData() {
		this.rawFiles = new String[0];
		this.expNames = new String[0];
		this.fractions = new int[0];
		this.replicates = new int[0];

		this.metaTypeCount = 0;
		this.metaInfo = new String[0][0];
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	public MetaData(String[] rawFiles) {
		this.rawFiles = rawFiles;
		this.expNames = new String[rawFiles.length];
		this.fractions = new int[rawFiles.length];
		Arrays.fill(fractions, 0);
		this.replicates = new int[rawFiles.length];
		Arrays.fill(replicates, 1);

		this.metaTypeCount = 0;
		this.metaInfo = new String[0][0];
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	public MetaData(String[] rawFiles, String[] expNames) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = new int[rawFiles.length];
		Arrays.fill(fractions, 0);
		this.replicates = new int[rawFiles.length];
		Arrays.fill(replicates, 1);

		this.metaTypeCount = 0;
		this.metaInfo = new String[0][0];
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	/**
	 * Label free quantification, no meta information
	 * @param rawFiles
	 * @param expNames
	 * @param fractions
	 * @param replicates
	 */
	public MetaData(String[] rawFiles, String[] expNames, int[] fractions, int[] replicates) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.metaTypeCount = 0;
		this.metaInfo = new String[0][0];
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	public MetaData(String[] rawFiles, String[] expNames, int[] fractions, int[] replicates, String[] labelTitle) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.labelTitle = labelTitle;
		this.metaTypeCount = 0;
		this.metaInfo = new String[0][0];
		this.labelExpNames = new String[0];
	}

	/**
	 * Label free quantification, have meta information
	 * 
	 * @param rawFiles
	 * @param expNames
	 * @param fractions
	 * @param replicates
	 * @param metaTypeCount
	 * @param metaInfo
	 */
	public MetaData(String[] rawFiles, String[] expNames, int[] fractions, int[] replicates, int metaTypeCount,
			String[][] metaInfo) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.metaTypeCount = metaTypeCount;
		this.metaInfo = metaInfo;
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	public MetaData(String[] rawFiles, String[] expNames, int metaTypeCount, String[][] metaInfo, int[] fractions,
			int[] replicates) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.metaTypeCount = metaTypeCount;
		this.metaInfo = metaInfo;
		this.labelTitle = new String[0];
		this.labelExpNames = new String[0];
	}

	/**
	 * Labeling quantification, no meta information
	 * @param rawFiles
	 * @param expNames
	 * @param fractions
	 * @param replicates
	 * @param metaTypeCount
	 * @param labelTitle
	 * @param labelExpNames
	 */
	public MetaData(String[] rawFiles, String[] expNames, int[] fractions, int[] replicates, int metaTypeCount,
			String[] labelTitle, String[] labelExpNames) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.metaTypeCount = metaTypeCount;
		this.labelTitle = labelTitle;
		this.labelExpNames = labelExpNames;
		if (labelTitle.length == 0) {
			this.metaInfo = new String[expNames.length][metaTypeCount];
		} else {
			this.metaInfo = new String[expNames.length * labelTitle.length][metaTypeCount];
		}
	}

	/**
	 * Labeling quantification, have meta information
	 * @param rawFiles
	 * @param expNames
	 * @param fractions
	 * @param replicates
	 * @param metaTypeCount
	 * @param metaInfo
	 * @param labelTitle
	 * @param labelExpNames
	 */
	public MetaData(String[] rawFiles, String[] expNames, int[] fractions, int[] replicates, int metaTypeCount,
			String[][] metaInfo, String[] labelTitle, String[] labelExpNames) {
		this.rawFiles = rawFiles;
		this.expNames = expNames;
		this.fractions = fractions;
		this.replicates = replicates;

		this.metaInfo = metaInfo;
		this.metaTypeCount = metaTypeCount;
		this.labelTitle = labelTitle;
		this.labelExpNames = labelExpNames;
	}

	/**
	 * Remove the meta information
	 * @return
	 */
	public MetaData getNoMetaInfo() {
		MetaData md = new MetaData(this.rawFiles, this.expNames, this.fractions, this.replicates);
		return md;
	}
	
	public boolean isTimsTof() {

		if (rawFiles == null || rawFiles.length == 0) {
			return false;
		}
		for (int i = 0; i < rawFiles.length; i++) {
			if (!rawFiles[i].endsWith(".d")) {
				return false;
			}
		}
		return true;
	}

	public int getRawFileCount() {
		return this.rawFiles.length;
	}

	public int getLabelCount() {
		return this.labelTitle.length;
	}

	public void setMetaTypeCount(int metaTypeCount) {
		this.metaTypeCount = metaTypeCount;
	}

	public void setLabelTitle(String[] labelTitle) {
		if (this.labelTitle.length == labelTitle.length) {
			boolean same = true;
			for (int i = 0; i < labelTitle.length; i++) {
				if (!this.labelTitle[i].equals(labelTitle[i])) {
					same = false;
					break;
				}
			}
			if (same) {
				return;
			}
		}
		this.labelTitle = labelTitle;
		this.labelExpNames = new String[expNames.length * labelTitle.length];
	}

	public String[] getLabelTitle() {
		return labelTitle;
	}

	public String[] getRawFiles() {
		return rawFiles;
	}

	public void setRawFiles(String[] rawFiles) {
		this.rawFiles = rawFiles;
	}

	public String[] getExpNames() {
		return expNames;
	}

	public void setExpNames(String[] expNames) {
		this.expNames = expNames;
	}

	public int[] getFractions() {
		return fractions;
	}

	public void setFractions(int[] fractions) {
		this.fractions = fractions;
	}

	public int[] getReplicates() {
		return replicates;
	}

	public void setReplicates(int[] replicates) {
		this.replicates = replicates;
	}

	public String[][] getMetaInfo() {
		return metaInfo;
	}

	public void setMetaInfo(String[][] metaInfo) {
		this.metaInfo = metaInfo;
	}

	public String[] getLabelExpNames() {
		return labelExpNames;
	}

	public void setLabelExpNames(String[] labelExpNames) {
		this.labelExpNames = labelExpNames;
	}

	public int getSampleCount() {
		if (labelTitle.length == 0) {
			return expNames.length;
		} else {
			return expNames.length * labelTitle.length;
		}
	}

	public int getMetaTypeCount() {
		return metaTypeCount;
	}
	
	public boolean isIsobaricQuan() {
		if (this.labelTitle == null || this.labelTitle.length == 0) {
			return false;
		}

		if (labelTitle[0].startsWith("iTRAQ") || labelTitle[0].startsWith("TMT")
				|| labelTitle[0].startsWith("iodoTMT")) {
			return true;
		}

		return false;
	}

	public Object[] getRawExpTableTitleMaxquant() {
		Object[] titleObj = new Object[5];
		titleObj[0] = "Exist";
		titleObj[1] = "Index";
		titleObj[2] = "File";
		titleObj[3] = "Experiment";
		titleObj[4] = "Fraction";

		return titleObj;
	}

	public Object[] getRawExpTableTitleFlashLFQ() {
		Object[] titleObj = new Object[6];
		titleObj[0] = "Exist";
		titleObj[1] = "Index";
		titleObj[2] = "File";
		titleObj[3] = "Experiment";
		titleObj[4] = "Fraction";
		titleObj[5] = "Replicate";

		return titleObj;
	}

	public Object[] getMetaTableTitleObjects() {
		Object[] titleObj;
		if (labelTitle.length == 0) {
			titleObj = new Object[2 + metaTypeCount];
			titleObj[0] = "Index";
			titleObj[1] = "Exp name";

			for (int i = 0; i < metaTypeCount; i++) {
				titleObj[i + 2] = "Meta " + (i + 1);
			}
		} else {
			titleObj = new Object[3 + metaTypeCount];
			titleObj[0] = "Index";
			titleObj[1] = "Label";
			titleObj[2] = "Exp name";
			for (int i = 0; i < metaTypeCount; i++) {
				titleObj[i + 3] = "Meta " + (i + 1);
			}
		}
		return titleObj;
	}

	public Object[][] getRawExpObjectsMaxquant() {
		Object[][] objs = new Object[rawFiles.length][5];
		for (int i = 0; i < objs.length; i++) {
			File file = new File(rawFiles[i]);
			objs[i][0] = file.exists();
			objs[i][1] = i + 1;
			objs[i][2] = rawFiles[i];
			objs[i][3] = expNames[i];
			objs[i][4] = fractions[i];
		}
		return objs;
	}

	public Object[][] getRawExpObjectsFlashLFQ() {
		Object[][] objs = new Object[rawFiles.length][6];
		for (int i = 0; i < objs.length; i++) {
			File file = new File(rawFiles[i]);
			objs[i][0] = file.exists();
			objs[i][1] = i + 1;
			objs[i][2] = rawFiles[i];
			objs[i][3] = expNames[i];
			objs[i][4] = fractions[i];
			objs[i][5] = replicates[i];
		}
		return objs;
	}

	public Object[][] getMetaObjects() {
		Object[][] objs = new Object[rawFiles.length][];

		if (this.labelTitle.length == 0) {
			objs = new Object[rawFiles.length][2 + metaTypeCount];
			for (int i = 0; i < objs.length; i++) {
				objs[i][0] = i + 1;
				objs[i][1] = expNames[i] + "_" + fractions[i] + "_" + replicates[i];

				for (int j = 0; j < metaTypeCount; j++) {
					objs[i][j + 2] = metaInfo[i][j];
				}
			}
		} else {
			objs = new Object[rawFiles.length * labelTitle.length][3 + metaTypeCount];
			for (int i = 0; i < rawFiles.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					int id = i * labelTitle.length + j;
					objs[id][0] = id + 1;
					objs[id][1] = expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j];
					objs[id][2] = id < labelExpNames.length ? labelExpNames[id] : "";
					for (int k = 0; k < metaTypeCount; k++) {
						objs[id][k + 3] = metaInfo[id][k];
					}
				}
			}
		}

		return objs;
	}
	
	/**
	 * No meta information
	 * @return
	 */
	public Object[][] getBlankMetaObjects() {
		Object[][] objs = new Object[rawFiles.length][];

		if (this.labelTitle.length == 0) {
			objs = new Object[rawFiles.length][2 + metaTypeCount];
			for (int i = 0; i < objs.length; i++) {
				objs[i][0] = i + 1;
				objs[i][1] = expNames[i] + "_" + fractions[i] + "_" + replicates[i];

				for (int j = 0; j < metaTypeCount; j++) {
					objs[i][j + 2] = "";
				}
			}
		} else {
			objs = new Object[rawFiles.length * labelTitle.length][3 + metaTypeCount];
			for (int i = 0; i < rawFiles.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					int id = i * labelTitle.length + j;
					objs[id][0] = id + 1;
					objs[id][1] = expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j];
					objs[id][2] = id < labelExpNames.length ? labelExpNames[id] : "";
					for (int k = 0; k < metaTypeCount; k++) {
						objs[id][k + 3] = "";
					}
				}
			}
		}

		return objs;
	}

	public boolean equalRawExpName(MetaData metadata) {
		if (this.rawFiles.length != metadata.rawFiles.length) {
			return false;
		}
		for (int i = 0; i < this.rawFiles.length; i++) {
			if (!this.rawFiles[i].equals(metadata.rawFiles[i])) {
				return false;
			}
		}
		if (this.expNames.length != metadata.expNames.length) {
			return false;
		}
		for (int i = 0; i < this.expNames.length; i++) {
			if (!this.expNames[i].equals(metadata.expNames[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean equalLabelTitle(MetaData metadata) {
		if (this.labelTitle.length != metadata.labelTitle.length) {
			return false;
		}
		for (int i = 0; i < this.labelTitle.length; i++) {
			if (!this.labelTitle[i].equals(metadata.labelTitle[i])) {
				return false;
			}
		}
		return true;
	}

	public void parseReferenceFromExpname() {

		if (rawFiles.length == 1) {
			this.isobaricReference = new String[] {};
			return;
		}

		HashMap<String, HashSet<Integer>> map = new HashMap<String, HashSet<Integer>>();
		for (int i = 0; i < labelExpNames.length; i++) {
			if (labelExpNames[i] == null || labelExpNames[i].length() == 0) {
				continue;
			}

			int fileId = i / labelTitle.length;
			if (map.containsKey(labelExpNames[i])) {
				map.get(labelExpNames[i]).add(fileId);
			} else {
				HashSet<Integer> set = new HashSet<Integer>();
				set.add(fileId);
				map.put(labelExpNames[i], set);
			}
		}

		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (map.get(key).size() < this.rawFiles.length) {
				it.remove();
			}
		}

		this.isobaricReference = map.keySet().toArray(new String[map.size()]);
		this.selectRef = new boolean[this.isobaricReference.length];
		Arrays.fill(selectRef, true);
	}

	public String[] getIsobaricReference() {
		if (this.isobaricReference == null) {
			this.parseReferenceFromExpname();
		}
		return isobaricReference;
	}

	public void setIsobaricReference(String[] isobaricReference) {
		this.isobaricReference = isobaricReference;
	}

	public Object[][] getRefObjects() {

//		this.parseReferenceFromExpname();

		if (isobaricReference != null && isobaricReference.length > 0) {
			Object[][] objs = new Object[this.isobaricReference.length][2];
			for (int i = 0; i < objs.length; i++) {
				objs[i][0] = this.selectRef[i];
				objs[i][1] = this.isobaricReference[i];
			}
			return objs;
		} else {
			return new Object[0][0];
		}

	}

	public boolean[] getSelectRef() {
		if (this.selectRef == null) {
			selectRef = new boolean[] {};
		}
		return selectRef;
	}

	public void setSelectRef(boolean[] selectRef) {
		this.selectRef = selectRef;
	}
	
	public int getRefChannelId() {
		return refChannelId;
	}

	public void setRefChannelId(int refChannelId) {
		this.refChannelId = refChannelId;
	}

	public void exportMetadata(File result) throws FileNotFoundException {
		int metaTypeCount = this.getMetaTypeCount();
		if (metaTypeCount == 0) {
			return;
		}

		PrintWriter writer = new PrintWriter(result);
		StringBuilder title = new StringBuilder();
		title.append("Raw").append("\t");
		title.append("Experiment");
		for (int i = 0; i < metaTypeCount; i++) {
			title.append("\t").append("meta" + (i + 1));
		}
		writer.println(title);

		if (getLabelTitle().length == 0) {
			String[] raws = getRawFiles();
			String[] expNames = getExpNames();
			String[][] metainfo = getMetaInfo();

			StringBuilder[] sbs = new StringBuilder[expNames.length];

			for (int i = 0; i < sbs.length; i++) {
				String raw = raws[i].substring(raws[i].lastIndexOf("\\") + 1, raws[i].length() - ".raw".length());
				sbs[i] = new StringBuilder();
				sbs[i].append(raw).append("\t");
				sbs[i].append(expNames[i]);
				for (int j = 0; j < metainfo[i].length; j++) {
					sbs[i].append("\t").append(metainfo[i][j]);
				}
				writer.println(sbs[i]);
			}
		} else {
			String[] expNames = getExpNames();
			String[] LabelTitles = getLabelTitle();
			String[] labelExpNames = getLabelExpNames();
			String[][] metainfo = getMetaInfo();

			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < LabelTitles.length; j++) {
					int id = i * LabelTitles.length + j;

					StringBuilder sb = new StringBuilder();
					sb.append(expNames[i] + "_" + LabelTitles[j]).append("\t");
					sb.append(labelExpNames[id]);
					for (int k = 0; k < metainfo[id].length; k++) {
						sb.append("\t").append(metainfo[id][k]);
					}
					writer.println(sb);
				}
			}
		}
		writer.close();
	}

	public String[] checkFlashLFQExpDesign() {

		if (this.rawFiles.length == 0) {
			return new String[] { "No raw files.", "" };
		}

		File rawDir = (new File(rawFiles[0])).getParentFile();
		if (!rawDir.exists()) {
			return new String[] { "Can't find file " + rawDir + ".", "" };
		}

		HashMap<String, HashMap<Integer, HashMap<Integer, String>>> expMap = new HashMap<String, HashMap<Integer, HashMap<Integer, String>>>();

		for (int i = 0; i < rawFiles.length; i++) {
			HashMap<Integer, HashMap<Integer, String>> fracMap;
			if (expMap.containsKey(expNames[i])) {
				fracMap = expMap.get(expNames[i]);
			} else {
				fracMap = new HashMap<Integer, HashMap<Integer, String>>();
				expMap.put(expNames[i], fracMap);
			}

			HashMap<Integer, String> repMap;
			if (fracMap.containsKey(fractions[i])) {
				repMap = fracMap.get(fractions[i]);
			} else {
				repMap = new HashMap<Integer, String>();
				fracMap.put(fractions[i], repMap);
			}

			repMap.put(replicates[i], (new File(rawFiles[i])).getName());
		}

		StringBuilder infoSb = new StringBuilder();

		StringBuilder sb = new StringBuilder();
		for (String expName : expMap.keySet()) {
			HashMap<Integer, HashMap<Integer, String>> fracMap = expMap.get(expName);
			Integer[] fracIds = fracMap.keySet().toArray(new Integer[fracMap.size()]);
			Arrays.sort(fracIds);

			for (int i = 0; i < fracIds.length; i++) {
				if (fracIds[i] != i + 1) {
					infoSb.append("Experiment name " + expName + ": fraction " + (i + 1) + " is missing.\n");
				} else {
					HashMap<Integer, String> repMap = fracMap.get(fracIds[i]);
					Integer[] repIds = repMap.keySet().toArray(new Integer[repMap.size()]);
					Arrays.sort(repIds);

					for (int j = 0; j < repIds.length; j++) {
						if (repIds[j] != j + 1) {
							infoSb.append("Experiment name " + expName + ", fraction " + (i + 1) + ": replicate "
									+ (j + 1) + " is missing.\n");
						} else {
							sb.append(repMap.get(repIds[j])).append("\t");
							sb.append(expName).append("\t");
							sb.append("1").append("\t");
							sb.append(fracIds[i]).append("\t");
							sb.append(repIds[j]).append("\n");
						}
					}
				}
			}
		}

		if (infoSb.length() == 0) {
			return new String[] { "", sb.toString() };
		} else {
			return new String[] { infoSb.toString(), "" };
		}
	}
	
	public void exportFragpipeManifest(File output) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(output);
		StringBuilder[] sbs = new StringBuilder[expNames.length];
		String[] raws = getRawFiles();
		String[] expNames = getExpNames();
		int[] rep = getReplicates();
		for (int i = 0; i < sbs.length; i++) {
			sbs[i] = new StringBuilder();
			sbs[i].append(raws[i]).append("\t");
			sbs[i].append(expNames[i]).append("\t");
			sbs[i].append(rep[i]).append("\t");
			sbs[i].append("DDA");

			writer.println(sbs[i]);
		}
		writer.close();
	}
}
