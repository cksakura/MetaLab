package bmi.med.uOttawa.metalab.task.dia.monitor.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaParameterIO;

public class MetaParIODiaRT {

	public static final String[] oldVersionFiles = new String[] { "_DIA_RT_1_0.json" };
	public static final String version = "DIA Realtime 1.0";
	public static final String versionFile = "_DIA_RT_1_0.json";
	private static Logger LOGGER = LogManager.getLogger(MetaParIODiaRT.class);

	public static MetaParameterDiaRT parse(String json) {
		return parse(new File(json));
	}

	public static MetaParameterDiaRT parse(File json) {

		if (!json.exists() || json.length() == 0) {
			exportBlank(json);
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(json))) {
			String line = null;
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

	public static MetaParameterDiaRT parse(JSONObject obj) {

		MetaParameter metaPar = MetaParameterIO.parseMain(obj);

		String magDbItem = obj.has("MagDb") ? obj.getString("MagDb") : "";
		String magDbVersuib = obj.has("MagDbVersion") ? obj.getString("MagDbVersion") : "";
		String monitorPath = obj.has("monitorFolder") ? obj.getString("monitorFolder") : "";
		String resultPath = obj.has("resultFolder") ? obj.getString("resultFolder") : "";

		ArrayList<String> libList = new ArrayList<String>();
		JSONArray libArray = obj.getJSONArray("libraries");
		if (libArray != null) {
			for (int i = 0; i < libArray.length(); i++) {
				String name = libArray.getJSONObject(i).getString("library");
				libList.add(name);
			}
		}
		String[] libs = libList.toArray(new String[libList.size()]);

		ArrayList<String> modelList = new ArrayList<String>();
		JSONArray modelArray = obj.getJSONArray("models");
		if (modelArray != null) {
			for (int i = 0; i < modelArray.length(); i++) {
				String name = modelArray.getJSONObject(i).getString("model");
				modelList.add(name);
			}
		}
		String[] models = modelList.toArray(new String[modelList.size()]);

		MetaParameterDiaRT parDia = new MetaParameterDiaRT(metaPar, magDbItem, magDbVersuib, false, false, libs, false,
				models);
		parDia.setMonitorFolder(new File(monitorPath));
		parDia.setResultFolder(new File(resultPath));
		return parDia;
	}

	public static void exportBlank(String out) {
		exportBlank(new File(out));
	}

	public static void exportBlank(File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);

			JSONWriter jw = new JSONWriter(writer);
			jw.object();

			jw.key("version").value(version);

			jw.key("workflowType").value(MetaLabWorkflowType.DiaNNMAGRT.name());

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

			jw.key("MagDb").value("");

			jw.key("MagDbVersion").value("");

			jw.key("librarySearch").value(false);

			jw.key("selfModel").value(false);

			jw.key("libraries").array();
			jw.endArray();

			jw.key("useModel").value(false);

			jw.key("models").array();
			jw.endArray();

			jw.key("monitorFolder").value("");

			jw.key("resultFolder").value("");

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}

	public static void export(MetaParameterDiaRT par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaParameterDiaRT par, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);

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

			MagDbItem magDbItem = par.getUsedMagDbItem();
			if (magDbItem != null) {
				jw.key("MagDb").value(magDbItem.getCatalogueID());
				jw.key("MagDbVersion").value(magDbItem.getUsedVersion());
			} else {
				jw.key("MagDb").value("");
				jw.key("MagDbVersion").value("");
			}

			jw.key("librarySearch").value(par.isLibrarySearch());

			jw.key("largeScaleSearch").value(par.isSelfModelSearch());

			String[] libs = par.getLibrary();
			jw.key("libraries").array();
			for (int i = 0; i < libs.length; i++) {
				jw.object().key("library").value(libs[i]).endObject();
			}
			jw.endArray();

			jw.key("useModel").value(par.isUseModel());

			String[] models = par.getModels();
			jw.key("models").array();
			for (int i = 0; i < models.length; i++) {
				jw.object().key("model").value(models[i]).endObject();
			}
			jw.endArray();

			jw.key("monitorFolder").value(par.getMonitorFolder().getAbsolutePath());

			jw.key("resultFolder").value(par.getResultFolder().getAbsolutePath());

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}
}
