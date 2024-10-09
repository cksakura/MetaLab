package bmi.med.uOttawa.metalab.task.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParaIOPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParaIOMQ;

public class MetaParameterIO {

	private static Logger LOGGER = LogManager.getLogger(MetaParameterIO.class);
	
	public static MetaLabWorkflowType getWorkflow(String json) {
		return getWorkflow(new File(json));
	}
	
	public static MetaLabWorkflowType getWorkflow(File json) {
		if (!json.exists() || json.length() == 0) {
			LOGGER.error("Error in reading MetaLab parameter file " + json);
			return null;
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

		if (obj.has("workflowType")) {
			MetaLabWorkflowType workflow = MetaLabWorkflowType.valueOf(obj.getString("workflowType"));
			if (workflow == null) {
				LOGGER.error("Unknown workflow " + obj.getString("workflowType"));
				return null;
			} else {
				return workflow;
			}
		} else {
			LOGGER.error("There is no workflowType information in " + json);
			return null;
		}
	}

	public static MetaParameter parse(String json) {
		return parse(new File(json));
	}

	public static MetaParameter parse(File json) {

		if (!json.exists() || json.length() == 0) {
			MetaParaIOMQ.exportBlank(json);
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

	public static MetaParameter parseMain(JSONObject obj) {
		String workflowType = obj.has("workflowType") ? obj.getString("workflowType") : "MaxQuantWorkflow";
		JSONArray rawsExpArray = obj.getJSONArray("rawExpName");
		String[] rawFiles = new String[rawsExpArray.length()];
		String[] expNames = new String[rawsExpArray.length()];
		int[] fraction = new int[rawsExpArray.length()];
		int[] replicate = new int[rawsExpArray.length()];

		for (int i = 0; i < rawFiles.length; i++) {
			JSONObject obji = rawsExpArray.getJSONObject(i);
			rawFiles[i] = obji.getString("path");
			expNames[i] = obji.getString("experiment");
			if (obji.has("fraction")) {
				fraction[i] = obji.getInt("fraction");
			}
			if (obji.has("replicate")) {
				replicate[i] = obji.getInt("replicate");
			}
		}

		MetaData metadata;

		int metaTypeCount = obj.getInt("metaCount");
		
		String[] labelTitle = new String[] {};
		if(obj.has("labelTitle")) {
			JSONArray labelTitleArray = obj.getJSONArray("labelTitle");
			labelTitle = new String[labelTitleArray.length()];
			for (int i = 0; i < labelTitle.length; i++) {
				labelTitle[i] = labelTitleArray.getJSONObject(i).getString("label");
			}
		}
		
		int labelCount = labelTitle.length;

		String[][] metaInfo;
		if (labelCount == 0) {
			if (metaTypeCount == 0) {
				metadata = new MetaData(rawFiles, expNames, fraction, replicate);
			} else {
				metaInfo = new String[rawFiles.length][metaTypeCount];
				JSONArray metainfoArray = obj.getJSONArray("metainfo");
				for (int i = 0; i < metaInfo.length; i++) {
					JSONObject obji = metainfoArray.getJSONObject(i);
					for (int j = 0; j < metaTypeCount; j++) {
						metaInfo[i][j] = obji.getString("meta " + (j + 1));
					}
				}
				metadata = new MetaData(rawFiles, expNames, fraction, replicate, metaTypeCount, metaInfo);
			}

		} else {
			String[] labelNames = new String[expNames.length * labelCount];
			JSONArray labelArray = obj.getJSONArray("labelExpName");
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelCount; j++) {
					int id = i * labelCount + j;
					JSONObject obji = labelArray.getJSONObject(id);
					labelNames[id] = obji.getString(expNames[i] + " " + labelTitle[j]);
				}
			}
			if (metaTypeCount == 0) {
				metadata = new MetaData(rawFiles, expNames, fraction, replicate, metaTypeCount, labelTitle, labelNames);
			} else {
				metaInfo = new String[rawFiles.length * labelCount][metaTypeCount];
				JSONArray metainfoArray = obj.getJSONArray("metainfo");
				for (int i = 0; i < metaInfo.length; i++) {
					JSONObject obji = metainfoArray.getJSONObject(i);
					for (int j = 0; j < metaTypeCount; j++) {
						metaInfo[i][j] = obji.getString("meta " + (j + 1));
					}
				}
				metadata = new MetaData(rawFiles, expNames, fraction, replicate, metaTypeCount, metaInfo, labelTitle,
						labelNames);
			}

			JSONArray isobaricRefArray = obj.getJSONArray("isobaricRefs");
			String[] isobaricRefs = new String[isobaricRefArray.length()];
			for (int i = 0; i < isobaricRefArray.length(); i++) {
				isobaricRefs[i] = isobaricRefArray.getJSONObject(i).getString("refName");
			}

			JSONArray selectRefArray = obj.getJSONArray("selectRefs");
			boolean[] selectRefs = new boolean[selectRefArray.length()];
			for (int i = 0; i < selectRefArray.length(); i++) {
				selectRefs[i] = selectRefArray.getJSONObject(i).getBoolean("select");
			}

			metadata.setIsobaricReference(isobaricRefs);
			metadata.setSelectRef(selectRefs);

			int refChannelId = obj.has("refChannelId") ? obj.optInt("refChannelId") : -1;
			metadata.setRefChannelId(refChannelId);
		}

		String result = obj.getString("result");

		String microDb = obj.getString("microDb");

		boolean appendHostDb = obj.getBoolean("appendHostDb");

		String hostDb = obj.getString("hostDb");

		String ms2ScanMode = obj.has("MS2ScanMode")?  obj.getString("MS2ScanMode"): "";

		int coreCount = obj.optInt("coreCount") == 0 ? 1 : obj.optInt("coreCount");

		int threadCount = obj.getInt("threadCount");

		boolean isMetaWorkflow = obj.getBoolean("isMetaWorkflow");

		MetaSsdbCreatePar mscp;
		if (obj.has("ssdb")) {
			boolean ssdb = obj.getBoolean("ssdb");
			boolean ssopen = obj.getBoolean("ssOpen");
			boolean cluster = obj.getBoolean("cluster");
			double xtandemFDR = obj.getDouble("xtandemFDR");
			double xtandemEvalue = obj.getDouble("xtandemEvalue");
			mscp = new MetaSsdbCreatePar(ssdb, cluster, ssopen, xtandemFDR, xtandemEvalue);
		} else {
			mscp = MetaSsdbCreatePar.getDefault();
		}

		MetaParameter metaPar = new MetaParameter(workflowType, metadata, result, microDb, appendHostDb, hostDb,
				ms2ScanMode, coreCount, threadCount, isMetaWorkflow, mscp);

		return metaPar;
	}

	public static MetaParameter parse(JSONObject obj) {

		MetaParameter metaPar;

		String workflowType = obj.has("workflowType") ? obj.getString("workflowType") : "MaxQuantWorkflow";

		MetaLabWorkflowType mwf = MetaLabWorkflowType.valueOf(workflowType);
		if (mwf == MetaLabWorkflowType.pFindWorkflow) {
			metaPar = MetaParaIOPFind.parse(obj);
		} else {
			metaPar = MetaParaIOMQ.parse(obj);
		}

		return metaPar;
	}
}
