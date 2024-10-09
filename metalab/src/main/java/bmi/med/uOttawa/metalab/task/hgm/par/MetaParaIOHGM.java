package bmi.med.uOttawa.metalab.task.hgm.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaParameterIO;
import bmi.med.uOttawa.metalab.task.v2.par.MaxQuantModIO;

public class MetaParaIOHGM {

	public static final String version = "HGM 1.0";
	public static final String versionFile = "_HGM_1_0.json";
	private static Logger LOGGER = LogManager.getLogger(MetaParaIOHGM.class);

	public static MetaParameterHGM parse(String json) {
		return parse(new File(json));
	}

	public static MetaParameterHGM parse(File json) {

		if (!json.exists() || json.length() == 0) {
			exportBlank(json);
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(json));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab parameter file " + json, e);
		}

		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab parameter file " + json, e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab parameter file " + json, e);
			return null;
		}

		return parse(obj);
	}

	public static MetaParameterHGM parse(JSONObject obj) {

		MetaParameter metaPar = MetaParameterIO.parseMain(obj);

		ArrayList<String> fixModList = new ArrayList<String>();
		JSONArray fixArray = obj.getJSONArray("fixMods");
		if (fixArray != null) {
			for (int i = 0; i < fixArray.length(); i++) {
				String name = fixArray.getJSONObject(i).getString("name");
				fixModList.add(name);
			}
		}
		String[] fixMods = fixModList.toArray(new String[fixModList.size()]);

		ArrayList<String> variModList = new ArrayList<String>();
		JSONArray variArray = obj.getJSONArray("variMods");
		if (variArray != null) {
			for (int i = 0; i < variArray.length(); i++) {
				String name = variArray.getJSONObject(i).getString("name");
				variModList.add(name);
			}
		}
		String[] variMods = variModList.toArray(new String[variModList.size()]);

		String enzymeString = obj.has("enzyme") ? obj.getString("enzyme") : "Trypsin";

		int missCleavages = obj.has("missCleavages") ? obj.getInt("missCleavages") : 2;

		int digestMode = obj.has("digestMode") ? obj.getInt("digestMode") : 0;

		String quanMode = obj.has("quanMode") ? obj.getString("quanMode") : MetaConstants.labelFree;

		boolean combineLabel = obj.has("combineLabel") ? obj.getBoolean("combineLabel") : false;

		HashMap<String, MaxquantModification> isobaricMap = MaxQuantModIO.getIsobaricMap();

		ArrayList<MaxquantModification> isobaricList = new ArrayList<MaxquantModification>();

		IsobaricTag isobaricTag = null;
		if (obj.has("isobaricTag")) {
			String tag = obj.getString("isobaricTag");
			if (tag.length() > 0) {
				isobaricTag = IsobaricTag.valueOf(tag);
			}
		}

		double[][] isoCorFactor = new double[][] {};
		JSONArray isobaricArray = obj.getJSONArray("isobaric");
		if (isobaricArray != null) {
			isoCorFactor = new double[isobaricArray.length()][4];
			for (int i = 0; i < isobaricArray.length(); i++) {
				String[] values = isobaricArray.getJSONObject(i).getString("name").split("_");
				if (isobaricMap.containsKey(values[0])) {
					isobaricList.add(isobaricMap.get(values[0]));
					for (int j = 0; j < 4; j++) {
						isoCorFactor[i][j] = Double.parseDouble(values[j + 1]);
					}
				}
			}
		}
		MaxquantModification[] isobaric = isobaricList.toArray(new MaxquantModification[isobaricList.size()]);

		MetaParameterHGM parHGM = new MetaParameterHGM(metaPar, fixMods, variMods, enzymeString, missCleavages,
				digestMode, quanMode, isobaric, isobaricTag, combineLabel, isoCorFactor);

		return parHGM;
	}

	public static void exportBlank(String out) {
		exportBlank(new File(out));
	}

	public static void exportBlank(File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		jw.key("version").value(version);

		jw.key("workflowType").value(MetaLabWorkflowType.pFindHGM.name());

		MetaData metadata = new MetaData();

		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();

		jw.key("rawExpName").array();
		for (int i = 0; i < rawFiles.length; i++) {
			jw.object().key("path").value(rawFiles[i]).key("experiment").value(expNames[i]).endObject();
		}
		jw.endArray();

		int metaTypeCount = metadata.getMetaTypeCount();
		jw.key("metaCount").value(metaTypeCount);

		jw.key("metainfo").array();
		String[][] metainfo = metadata.getMetaInfo();
		for (int i = 0; i < metainfo.length; i++) {
			jw.object();
			for (int j = 0; j < metainfo[i].length; j++) {
				jw.key("meta " + (j + 1)).value(metainfo[i][j]);
			}
			jw.endObject();
		}
		jw.endArray();

		String[] labelTitle = metadata.getLabelTitle();
		jw.key("labelTitle").array();
		for (int i = 0; i < labelTitle.length; i++) {
			jw.object().key("label").value(labelTitle[i]).endObject();
		}
		jw.endArray();

		jw.key("labelExpName").array();
		String[] labelExpName = metadata.getLabelExpNames();
		for (int i = 0; i < expNames.length; i++) {
			for (int j = 0; j < labelTitle.length; j++) {
				jw.object().key(expNames[i] + " " + labelTitle[j]).value(labelExpName[i * labelTitle.length + j])
						.endObject();
			}
		}
		jw.endArray();

		String[] isobaricRefs = metadata.getIsobaricReference();
		jw.key("isobaricRefs").array();
		for (int i = 0; i < isobaricRefs.length; i++) {
			jw.object().key("refName").value(isobaricRefs[i]).endObject();
		}
		jw.endArray();

		boolean[] selectRefs = metadata.getSelectRef();
		jw.key("selectRefs").array();
		for (int i = 0; i < selectRefs.length; i++) {
			jw.object().key("select").value(selectRefs[i]).endObject();
		}
		jw.endArray();

		jw.key("result").value("");

		jw.key("microDb").value("");

		jw.key("hostDb").value("");

		jw.key("appendHostDb").value(false);

		jw.key("MS2ScanMode").value(MetaConstants.FTMS);

		jw.key("coreCount").value(1);

		jw.key("threadCount").value(1);

		jw.key("isMetaWorkflow").value(true);

		jw.key("fixMods").array();
		jw.object().key("name").value("Carbamidomethyl[C] 0").endObject();
		jw.endArray();

		jw.key("variMods").array();
		jw.object().key("name").value("Oxidation[M] 0").endObject();
		jw.object().key("name").value("Acetyl[ProteinN-term] 0").endObject();
		jw.endArray();

		jw.key("enzyme").value("Trypsin");

		jw.key("missCleavages").value(2);

		jw.key("digestMode").value(0);

		jw.key("quanMode").value(MetaConstants.labelFree);

		jw.key("combineLabel").value(false);

		jw.key("isobaricTag").value("");

		jw.key("isobaric").array();
		jw.endArray();

		jw.key("refChannelId").value(-1);

		jw.endObject();

		writer.close();
	}

	public static void export(MetaParameterHGM par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaParameterHGM par, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		jw.key("version").value(version);

		String workflowType = par.getWorkflowType().name();
		jw.key("workflowType").value(workflowType);

		MetaData metadata = par.getMetadata();

		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		int[] fractions = metadata.getFractions();
		int[] replicates = metadata.getReplicates();

		jw.key("rawExpName").array();
		for (int i = 0; i < rawFiles.length; i++) {
			jw.object().key("path").value(rawFiles[i]).key("experiment").value(expNames[i]).key("fraction")
					.value(fractions[i]).key("replicate").value(replicates[i]).endObject();
		}
		jw.endArray();

		int metaTypeCount = metadata.getMetaTypeCount();
		jw.key("metaCount").value(metaTypeCount);

		jw.key("metainfo").array();
		String[][] metainfo = metadata.getMetaInfo();
		for (int i = 0; i < metainfo.length; i++) {
			jw.object();
			for (int j = 0; j < metainfo[i].length; j++) {
				if (metainfo[i][j] == null) {
					metainfo[i][j] = "";
				}
				jw.key("meta " + (j + 1)).value(metainfo[i][j]);
			}
			jw.endObject();
		}
		jw.endArray();

		String[] labelTitle = metadata.getLabelTitle();
		jw.key("labelTitle").array();
		for (int i = 0; i < labelTitle.length; i++) {
			jw.object().key("label").value(labelTitle[i]).endObject();
		}
		jw.endArray();

		jw.key("labelExpName").array();
		String[] labelExpName = metadata.getLabelExpNames();
		for (int i = 0; i < expNames.length; i++) {
			for (int j = 0; j < labelTitle.length; j++) {
				if (labelExpName[i * labelTitle.length + j] == null) {
					labelExpName[i * labelTitle.length + j] = "";
				}
				jw.object().key(expNames[i] + " " + labelTitle[j]).value(labelExpName[i * labelTitle.length + j])
						.endObject();
			}
		}
		jw.endArray();

		String[] isobaricRefs = metadata.getIsobaricReference();
		jw.key("isobaricRefs").array();
		for (int i = 0; i < isobaricRefs.length; i++) {
			jw.object().key("refName").value(isobaricRefs[i]).endObject();
		}
		jw.endArray();

		boolean[] selectRefs = metadata.getSelectRef();
		jw.key("selectRefs").array();
		for (int i = 0; i < selectRefs.length; i++) {
			jw.object().key("select").value(selectRefs[i]).endObject();
		}
		jw.endArray();

		jw.key("refChannelId").value(metadata.getRefChannelId());

		String result = par.getResult();
		jw.key("result").value(result);

		String microDb = par.getMicroDb();
		jw.key("microDb").value(microDb);

		String hostDb = par.getHostDB();
		jw.key("hostDb").value(hostDb);

		boolean appendHostDb = par.isAppendHostDb();
		jw.key("appendHostDb").value(appendHostDb);

		String ms2ScanMode = par.getMs2ScanMode();
		jw.key("MS2ScanMode").value(ms2ScanMode);

		int coreCount = par.getCoreCount();
		jw.key("coreCount").value(coreCount);

		int threadCount = par.getThreadCount();
		jw.key("threadCount").value(threadCount);

		jw.key("isMetaWorkflow").value(true);

		String[] fixMods = par.getFixMods();
		jw.key("fixMods").array();
		for (String fm : fixMods) {
			jw.object().key("name").value(fm).endObject();
		}
		jw.endArray();

		String[] variMods = par.getVariMods();
		jw.key("variMods").array();
		for (String vm : variMods) {
			jw.object().key("name").value(vm).endObject();
		}
		jw.endArray();

		String enzyme = par.getEnzyme();
		jw.key("enzyme").value(enzyme);

		int missCleavages = par.getMissCleavages();
		jw.key("missCleavages").value(missCleavages);

		int digestMode = par.getDigestMode();
		jw.key("digestMode").value(digestMode);

		String quantMode = par.getQuanMode();
		jw.key("quanMode").value(quantMode);

		boolean combineLabel = par.isCombineLabel();
		jw.key("combineLabel").value(combineLabel);

		IsobaricTag isobaricTag = par.getIsobaricTag();
		if (isobaricTag != null) {
			jw.key("isobaricTag").value(isobaricTag.toString());
		} else {
			jw.key("isobaricTag").value("");
		}

		MaxquantModification[] isobaric = par.getIsobaric();
		double[][] isoCorFactor = par.getIsoCorFactor();
		jw.key("isobaric").array();
		for (int i = 0; i < isobaric.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(isobaric[i].getTitle());
			for (int j = 0; j < 4; j++) {
				sb.append("_").append(isoCorFactor[i][j]);
			}
			jw.object().key("name").value(sb.toString()).endObject();
		}
		jw.endArray();

		jw.endObject();

		writer.close();
	}

}
