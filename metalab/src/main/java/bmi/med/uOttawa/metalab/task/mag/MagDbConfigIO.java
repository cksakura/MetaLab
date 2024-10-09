package bmi.med.uOttawa.metalab.task.mag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagModel;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagPepLib;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagVersion;

public class MagDbConfigIO {

	public static final String version = "MAGDB 1.1";
	public static final String versionFile = "MAGDB_1_1.json";

	private static String MGnify = "MGnify";
	private static String localMAGs = "localMAGs";
	private static SimpleDateFormat sdf = FormatTool.getDateFormat();
	private static Logger LOGGER = LogManager.getLogger(MagDbConfigIO.class);

	public static MagDbItem[][] parse(String json) {
		return parse(new File(json));
	}

	public static MagDbItem[][] parse(File json) {
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

	public static MagDbItem[][] parse(JSONObject obj) {

		JSONArray mgnifyArray = obj.getJSONArray(MGnify);
		ArrayList<MagDbItem> mgnifyList = new ArrayList<MagDbItem>();
		if (mgnifyArray != null) {
			for (int i = 0; i < mgnifyArray.length(); i++) {
				JSONObject obji = mgnifyArray.getJSONObject(i);

				String catalogueID = obji.getString("catalogueID");
				String identifier = obji.getString("identifier");
				File repoFile = obji.isNull("repository") ? null : new File(obji.getString("repository"));

				MagDbItem magDbItem = new MagDbItem(catalogueID, identifier, repoFile);

				JSONArray versionArray = obji.getJSONArray("versionlist");
				for (int j = 0; j < versionArray.length(); j++) {
					JSONObject objj = versionArray.getJSONObject(j);

					String catalogueVersion = objj.getString("version");
					int speciesCount = objj.getInt("speciesCount");
					Date date = null;
					try {
						date = sdf.parse(objj.getString("date"));
					} catch (JSONException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean localAvailable = objj.getBoolean("localAvailable");
					String dbName = objj.getString("dbName");
					String funcName = objj.getString("funcName");
					String taxaName = objj.getString("taxaName");
					magDbItem.addVersion(catalogueVersion, speciesCount, date, localAvailable, dbName, funcName,
							taxaName);
				}

				JSONArray mgygArray = obji.getJSONArray("MGYG");
				for (int j = 0; j < mgygArray.length(); j++) {
					JSONObject objj = mgygArray.getJSONObject(j);
					String parent = objj.getString("parent");

					JSONArray childrenArray = objj.getJSONArray("children");
					for (int k = 0; k < childrenArray.length(); k++) {
						JSONObject objk = childrenArray.getJSONObject(k);
						String child = objk.getString("child");
						magDbItem.addMgygName(parent, child);
					}
				}

				if (obji.has("models")) {
					JSONArray modelArray = obji.getJSONArray("models");
					for (int j = 0; j < modelArray.length(); j++) {
						JSONObject objj = modelArray.getJSONObject(j);
						String modelName = objj.getString("modelName");
						int trainSize = objj.getInt("trainSize");
						int modelSize = objj.getInt("modelSize");
						double corr = objj.getDouble("corr");
						double med = objj.getDouble("med");
						magDbItem.addModel(modelName, trainSize, modelSize, corr, med);
					}
				}

				if (obji.has("pepLibs")) {
					JSONArray pepLibArray = obji.getJSONArray("pepLibs");
					for (int j = 0; j < pepLibArray.length(); j++) {
						JSONObject objj = pepLibArray.getJSONObject(j);
						String pepLibName = objj.getString("pepLibName");
						int pepCount = objj.getInt("pepCount");
						String protease = objj.getString("protease");
						int miss = objj.getInt("miss");
						magDbItem.addPepLib(pepLibName, pepCount, protease, miss);
					}
				}

				mgnifyList.add(magDbItem);
			}
		}

		JSONArray localArray = obj.getJSONArray(localMAGs);
		ArrayList<MagDbItem> localList = new ArrayList<MagDbItem>();
		if (localArray != null) {
			for (int i = 0; i < localArray.length(); i++) {
				JSONObject obji = localArray.getJSONObject(i);

				String catalogueID = obji.getString("catalogueID");
				String identifier = obji.getString("identifier");
				File repoFile = obji.isNull("repository") ? null : new File(obji.getString("repository"));
				MagDbItem magDbItem = new MagDbItem(catalogueID, identifier, repoFile);

				JSONArray versionArray = obji.getJSONArray("versionlist");
				for (int j = 0; j < versionArray.length(); j++) {
					JSONObject objj = versionArray.getJSONObject(j);

					String catalogueVersion = objj.getString("version");
					int speciesCount = objj.getInt("speciesCount");
					Date date = null;
					try {
						date = sdf.parse(objj.getString("date"));
					} catch (JSONException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean localAvailable = objj.getBoolean("localAvailable");
					String dbName = objj.getString("dbName");
					String funcName = objj.getString("funcName");
					String taxaName = objj.getString("taxaName");
					magDbItem.addVersion(catalogueVersion, speciesCount, date, localAvailable, dbName, funcName,
							taxaName);
				}
				
				if (obji.has("models")) {
					JSONArray modelArray = obji.getJSONArray("models");
					for (int j = 0; j < modelArray.length(); j++) {
						JSONObject objj = modelArray.getJSONObject(j);
						String modelName = objj.getString("modelName");
						int trainSize = objj.getInt("trainSize");
						int modelSize = objj.getInt("modelSize");
						double corr = objj.getDouble("corr");
						double med = objj.getDouble("med");
						magDbItem.addModel(modelName, trainSize, modelSize, corr, med);
					}
				}

				if (obji.has("pepLibs")) {
					JSONArray pepLibArray = obji.getJSONArray("pepLibs");
					for (int j = 0; j < pepLibArray.length(); j++) {
						JSONObject objj = pepLibArray.getJSONObject(j);
						String pepLibName = objj.getString("pepLibName");
						int pepCount = objj.getInt("pepCount");
						String protease = objj.getString("protease");
						int miss = objj.getInt("miss");
						magDbItem.addPepLib(pepLibName, pepCount, protease, miss);
					}
				}

				localList.add(magDbItem);
			}
		}

		MagDbItem[][] magDbItems = new MagDbItem[2][];
		magDbItems[0] = mgnifyList.toArray(new MagDbItem[mgnifyList.size()]);
		magDbItems[1] = localList.toArray(new MagDbItem[localList.size()]);

		return magDbItems;
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

		jw.key(MGnify).array();

		jw.endArray();

		jw.key(localMAGs).array();
		jw.endArray();

		jw.endObject();

		writer.close();
	}

	public static void export(MagDbItem[][] magDbItems, String out) {
		export(magDbItems, new File(out));
	}

	public static void export(MagDbItem[][] magDbItems, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		jw.key(MGnify).array();
		for (int i = 0; i < magDbItems[0].length; i++) {
			jw.object();
			jw.key("catalogueID").value(magDbItems[0][i].getCatalogueID());
			jw.key("identifier").value(magDbItems[0][i].getIdentifier());
			jw.key("repository").value(magDbItems[0][i].getRepositoryFile());

			jw.key("versionlist").array();
			ArrayList<MagVersion> versionList = magDbItems[0][i].getVersionList();
			for (int j = 0; j < versionList.size(); j++) {
				MagVersion ver = versionList.get(j);
				jw.object();
				jw.key("version").value(ver.getCatalogueVersion());
				jw.key("speciesCount").value(ver.getSpeciesCount());
				jw.key("date").value(sdf.format(ver.getDate()));
				jw.key("localAvailable").value(ver.isLocalAvailable());
				jw.key("dbName").value(ver.getDbName());
				jw.key("funcName").value(ver.getFuncName());
				jw.key("taxaName").value(ver.getTaxName());
				jw.endObject();
			}
			jw.endArray();

			jw.key("MGYG").array();
			HashMap<String, ArrayList<String>> mgygMap = magDbItems[0][i].getMgygNameMap();
			for (String parent : mgygMap.keySet()) {
				ArrayList<String> childList = mgygMap.get(parent);
				jw.object();
				jw.key("parent").value(parent);

				jw.key("children").array();
				for (String child : childList) {
					jw.object();
					jw.key("child").value(child);
					jw.endObject();
				}
				jw.endArray();
				jw.endObject();
			}
			jw.endArray();

			MagModel[] magModels = magDbItems[0][i].getMagModels();
			jw.key("models").array();
			for (MagModel magModel : magModels) {
				jw.object();
				jw.key("modelName").value(magModel.getModelName());
				jw.key("trainSize").value(magModel.getTrainSize());
				jw.key("modelSize").value(magModel.getModelSize());
				jw.key("corr").value(magModel.getCorr());
				jw.key("med").value(magModel.getMed());
				jw.endObject();
			}
			jw.endArray();

			MagPepLib[] magPepLibs = magDbItems[0][i].getMagPepLibs();
			jw.key("pepLibs").array();
			for (MagPepLib magPepLib : magPepLibs) {
				jw.object();
				jw.key("pepLibName").value(magPepLib.getLibName());
				jw.key("pepCount").value(magPepLib.getPepCount());
				jw.key("protease").value(magPepLib.getProtease());
				jw.key("miss").value(magPepLib.getMiss());
				jw.endObject();
			}
			jw.endArray();

			jw.endObject();
		}
		jw.endArray();

		jw.key(localMAGs).array();
		for (int i = 0; i < magDbItems[1].length; i++) {
			jw.object();
			jw.key("catalogueID").value(magDbItems[1][i].getCatalogueID());
			jw.key("identifier").value(magDbItems[1][i].getIdentifier());
			jw.key("repository").value(magDbItems[1][i].getRepositoryFile());

			jw.key("versionlist").array();
			ArrayList<MagVersion> versionList = magDbItems[1][i].getVersionList();
			for (int j = 0; j < versionList.size(); j++) {
				MagVersion ver = versionList.get(j);
				jw.object();
				jw.key("version").value(ver.getCatalogueVersion());
				jw.key("speciesCount").value(ver.getSpeciesCount());
				jw.key("date").value(sdf.format(ver.getDate()));
				jw.key("localAvailable").value(ver.isLocalAvailable());
				jw.key("dbName").value(ver.getDbName());
				jw.key("funcName").value(ver.getFuncName());
				jw.key("taxaName").value(ver.getTaxName());
				jw.endObject();
			}
			jw.endArray();
			
			MagModel[] magModels = magDbItems[1][i].getMagModels();
			jw.key("models").array();
			for (MagModel magModel : magModels) {
				jw.object();
				jw.key("modelName").value(magModel.getModelName());
				jw.key("trainSize").value(magModel.getTrainSize());
				jw.key("modelSize").value(magModel.getModelSize());
				jw.key("corr").value(magModel.getCorr());
				jw.key("med").value(magModel.getMed());
				jw.endObject();
			}
			jw.endArray();

			MagPepLib[] magPepLibs = magDbItems[1][i].getMagPepLibs();
			jw.key("pepLibs").array();
			for (MagPepLib magPepLib : magPepLibs) {
				jw.object();
				jw.key("pepLibName").value(magPepLib.getLibName());
				jw.key("pepCount").value(magPepLib.getPepCount());
				jw.key("protease").value(magPepLib.getProtease());
				jw.key("miss").value(magPepLib.getMiss());
				jw.endObject();
			}
			jw.endArray();
			
			jw.endObject();
		}
		jw.endArray();

		jw.endObject();

		writer.close();
	}
	
	public static void main(String[] args) {
		/*
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("C:\\Users\\kchen2\\Downloads\\MAGDB_1_1.json"), StandardCharsets.UTF_8))) {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
	                new FileOutputStream("C:\\Users\\kchen2\\Downloads\\test.json"), StandardCharsets.UTF_8));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				writer.println(sCurrentLine);
			}
			br.close();
			writer.close();
		} catch (IOException e) {

		}
		*/
		
		MagDbItem[][] items =  MagDbConfigIO.parse("C:\\Users\\kchen2\\Downloads\\MAGDB_1_1.json");
//		System.out.println(items[0].length+"\t"+items[1].length);
		
		for(int i=0;i<items[0].length;i++) {
//			System.out.println(items[0][i].getCatalogueID());
			if (items[0][i].getCatalogueID().equals("human-gut")) {
				
				MagPepLib[] libs = items[0][i].getMagPepLibs();
				for (int j = 0; j < libs.length; j++) {
					String libName = libs[j].getLibName();
					
					File infoFile = new File("Z:\\Kai\\Database\\human-gut\\v2.0.2\\libraries\\" + libName,
							libName + "_info.txt");
					System.out.println(libName);
	
				}
			}
		}
		
		
	}
}
