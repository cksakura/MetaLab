/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class JsonParaHandler {

	private static String[] types = new String[] { "high_high", "high_low" };
	private static final String tempParameter = "Resources//temp.json";

	private static Logger LOGGER = LogManager.getLogger(JsonParaHandler.class);

	public static MetaParameterV1 parse() {
		return parse(tempParameter);
	}

	public static MetaParameterV1 parse(String json) {
		return parse(new File(json));
	}

	public static MetaParameterV1 parse(File json) {

		if (!json.exists()) {
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

		String[] variMods = new String[] {};
		JSONArray variArray = obj.getJSONArray("vari mods");
		if (variArray != null) {
			variMods = new String[variArray.length()];
			for (int i = 0; i < variMods.length; i++) {
				variMods[i] = variArray.getJSONObject(i).getString("name");
			}
		}

		String[] fixMods = new String[] {};
		JSONArray fixArray = obj.getJSONArray("fix mods");
		if (fixArray != null) {
			fixMods = new String[fixArray.length()];
			for (int i = 0; i < fixMods.length; i++) {
				fixMods[i] = fixArray.getJSONObject(i).getString("name");
			}
		}

		String[] enzymes = new String[] {};
		JSONArray enzymeArray = obj.getJSONArray("enzyme");
		if (enzymeArray != null) {
			enzymes = new String[enzymeArray.length()];
			for (int i = 0; i < enzymes.length; i++) {
				enzymes[i] = enzymeArray.getJSONObject(i).getString("name");
			}
		}

		int digestMode = obj.getInt("digest mode");

		String[] hostDBs = new String[] {};
		JSONArray hostArray = obj.getJSONArray("host db");
		if (hostArray != null) {
			hostDBs = new String[hostArray.length()];
			for (int i = 0; i < hostDBs.length; i++) {
				hostDBs[i] = hostArray.getJSONObject(i).getString("path");
			}
		}

		boolean appendHostDb = obj.getBoolean("append host db");

		String[] light = new String[] {};
		JSONArray lightArray = obj.getJSONArray("light");
		if (lightArray != null) {
			light = new String[lightArray.length()];
			for (int i = 0; i < light.length; i++) {
				light[i] = lightArray.getJSONObject(i).getString("name");
			}
		}

		String[] medium = new String[] {};
		JSONArray mediumArray = obj.getJSONArray("medium");
		if (mediumArray != null) {
			medium = new String[mediumArray.length()];
			for (int i = 0; i < medium.length; i++) {
				medium[i] = mediumArray.getJSONObject(i).getString("name");
			}
		}

		String[] heavy = new String[] {};
		JSONArray heavyArray = obj.getJSONArray("heavy");
		if (heavyArray != null) {
			heavy = new String[heavyArray.length()];
			for (int i = 0; i < heavy.length; i++) {
				heavy[i] = heavyArray.getJSONObject(i).getString("name");
			}
		}

		String[][] labels = new String[][] { light, medium, heavy };

		String[] isobaric = new String[] {};
		double[][] isoCorFactor = new double[][] {};
		JSONArray isobaricArray = obj.getJSONArray("isobaric");
		if (isobaricArray != null) {
			isobaric = new String[isobaricArray.length()];
			isoCorFactor = new double[isobaricArray.length()][4];
			for (int i = 0; i < isobaric.length; i++) {
				String[] values = isobaricArray.getJSONObject(i).getString("name").split("_");
				isobaric[i] = values[0];
				for (int j = 0; j < 4; j++) {
					isoCorFactor[i][j] = Double.parseDouble(values[j + 1]);
				}
			}
		}

		String database = obj.getString("database");
		String type = obj.getString("instrument resolution");
		int instruType = -1;
		for (int i = 0; i < types.length; i++) {
			if (type.equals(types[i])) {
				instruType = i;
				break;
			}
		}

		int threadCount = obj.getInt("thread count");

		boolean buildin = obj.getBoolean("build-in");
		boolean unipept = obj.getBoolean("unipept");

		String[][] raws = new String[2][];
		JSONArray rawsArray = obj.getJSONArray("raw files");
		raws[0] = new String[rawsArray.length()];
		raws[1] = new String[rawsArray.length()];
		for (int i = 0; i < rawsArray.length(); i++) {
			raws[0][i] = rawsArray.getJSONObject(i).getString("path");
			raws[1][i] = rawsArray.getJSONObject(i).getString("experiment");
		}

		String result = obj.getString("result");
		String resource = obj.getString("resource");

		String quanMode = obj.getString("quantitative mode");
		int minRatioCount = obj.getInt("minRatioCount");
		int lfqMinRatioCount = obj.getInt("lfqMinRatioCount");
		double psmFdr = obj.getDouble("peptideFdr");
		double proFdr = obj.getDouble("proteinFdr");
		boolean cluster = obj.getBoolean("cluster");

		JSONArray workflowArray = obj.getJSONArray("workflow");
		boolean[] workflow = new boolean[workflowArray.length()];
		for (int i = 0; i < workflow.length; i++) {
			workflow[i] = workflowArray.getJSONObject(i).getBoolean("step");
		}

		JSONArray usedRootArray = obj.getJSONArray("usedRoots");
		HashSet<RootType> usedRootSet = new HashSet<RootType>();
		for (int i = 0; i < usedRootArray.length(); i++) {
			String name = usedRootArray.getJSONObject(i).getString("root");
			usedRootSet.add(RootType.valueOf(name));
		}

		TaxonomyRanks ignoreBlankRank = TaxonomyRanks.valueOf(obj.getString("ignoreBlankRank"));

		JSONArray ignoreTaxaArray = obj.getJSONArray("excludeTaxa");
		HashSet<String> ignoreTaxaSet = new HashSet<String>();
		for (int i = 0; i < ignoreTaxaArray.length(); i++) {
			ignoreTaxaSet.add(ignoreTaxaArray.getJSONObject(i).getString("excludeTaxon"));
		}

		int functionSampleId = obj.getInt("sample_id");

		MetaParameterV1 parameter = new MetaParameterV1(variMods, fixMods, enzymes, digestMode, database, hostDBs,
				appendHostDb, labels, isobaric, isoCorFactor, instruType, threadCount, buildin, unipept, raws, result,
				resource, quanMode, minRatioCount, lfqMinRatioCount, psmFdr, proFdr, cluster, workflow, usedRootSet,
				ignoreBlankRank, ignoreTaxaSet, functionSampleId);

		return parameter;
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

		jw.object().key("vari mods").array();
		jw.endArray();

		jw.key("fix mods").array();
		jw.endArray();

		jw.key("enzyme").array();
		jw.endArray();

		jw.key("digest mode").value(0);

		jw.key("host db").array();
		jw.endArray();

		jw.key("append host db").value(false);

		jw.key("light").array();
		jw.endArray();

		jw.key("medium").array();
		jw.endArray();

		jw.key("heavy").array();
		jw.endArray();

		jw.key("isobaric").array();
		jw.endArray();

		jw.key("database").value("");

		jw.key("instrument resolution").value(types[0]);

		jw.key("thread count").value(1);

		jw.key("build-in").value(false);

		jw.key("unipept").value(false);

		jw.key("raw files").array();
		jw.endArray();

		jw.key("result").value("");

		jw.key("resource").value("");

		jw.key("quantitative mode").value(MetaConstants.labelFree);

		jw.key("peptideFdr").value(0.01);

		jw.key("proteinFdr").value(0.01);

		jw.key("quantitative mode").value(0);

		jw.key("minRatioCount").value(1);

		jw.key("lfqMinRatioCount").value(1);

		jw.key("cluster").value(true);

		boolean[] workflow = new boolean[] { true, true, true, true };
		jw.key("workflow").array();
		for (boolean execute : workflow) {
			jw.object().key("step").value(execute).endObject();
		}
		jw.endArray();

		jw.key("usedRoots").array();
		jw.endArray();

		jw.key("ignoreBlankRank").value(TaxonomyRanks.Family);

		jw.key("excludeTaxa").array();
		jw.endArray();

		jw.key("sample_id").value(0);

		jw.endObject();

		writer.close();
	}

	public static void export(MetaParameterV1 parameter, String out) {
		export(parameter, new File(out));
	}

	public static void export(MetaParameterV1 parameter, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);

		String[] vms = parameter.getVariMods();
		jw.object().key("vari mods").array();
		for (String vm : vms) {
			jw.object().key("name").value(vm).endObject();
		}
		jw.endArray();

		String[] fms = parameter.getFixMods();
		jw.key("fix mods").array();
		for (String fm : fms) {
			jw.object().key("name").value(fm).endObject();
		}
		jw.endArray();

		String[] ens = parameter.getEnzymes();
		jw.key("enzyme").array();
		for (String en : ens) {
			jw.object().key("name").value(en).endObject();
		}
		jw.endArray();

		jw.key("digest mode").value(parameter.getDigestMode());

		String[] hostDBs = parameter.getHostDBs();
		jw.key("host db").array();
		for (String hdb : hostDBs) {
			jw.object().key("path").value(hdb).endObject();
		}
		jw.endArray();

		boolean appendHostDb = parameter.isAppendHostDb();
		jw.key("append host db").value(appendHostDb);

		String[][] labels = parameter.getLabels();
		jw.key("light").array();
		for (String light : labels[0]) {
			jw.object().key("name").value(light).endObject();
		}
		jw.endArray();

		jw.key("medium").array();
		for (String mediem : labels[1]) {
			jw.object().key("name").value(mediem).endObject();
		}
		jw.endArray();

		jw.key("heavy").array();
		for (String heavy : labels[2]) {
			jw.object().key("name").value(heavy).endObject();
		}
		jw.endArray();

		String[] isobaric = parameter.getIsobaric();
		double[][] isoCorFactor = parameter.getIsoCorFactor();
		jw.key("isobaric").array();
		for (int i = 0; i < isobaric.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(isobaric[i]);
			for (int j = 0; j < 4; j++) {
				sb.append("_").append(isoCorFactor[i][j]);
			}
			jw.object().key("name").value(sb.toString()).endObject();
		}
		jw.endArray();

		String database = parameter.getDatabase();
		jw.key("database").value(database);

		int instrument = parameter.getInstruType();
		jw.key("instrument resolution").value(types[instrument]);

		int threadCount = parameter.getThreadCount();
		jw.key("thread count").value(threadCount);

		boolean buildin = parameter.isBuildIn();
		jw.key("build-in").value(buildin);

		boolean unipept = parameter.isUnipept();
		jw.key("unipept").value(unipept);

		String[][] raws = parameter.getRawFiles();
		jw.key("raw files").array();
		for (int i = 0; i < raws[0].length; i++) {
			jw.object().key("path").value(raws[0][i]).key("experiment").value(raws[1][i]).endObject();
		}
		jw.endArray();

		String result = parameter.getMetalabOutput();
		jw.key("result").value(result);

		String resource = parameter.getResource();
		jw.key("resource").value(resource);

		String quanMode = parameter.getQuanMode();
		jw.key("quantitative mode").value(quanMode);

		double psmFdr = parameter.getPsmFdr();
		jw.key("peptideFdr").value(psmFdr);

		double proFdr = parameter.getProFdr();
		jw.key("proteinFdr").value(proFdr);

		int minRatioCount = parameter.getMinRatioCount();
		jw.key("minRatioCount").value(minRatioCount);

		int lfqMinRatioCount = parameter.getLfqMinRatioCount();
		jw.key("lfqMinRatioCount").value(lfqMinRatioCount);

		boolean cluster = parameter.isCluster();
		jw.key("cluster").value(cluster);

		boolean[] workflow = parameter.getWorkflow();
		jw.key("workflow").array();
		for (boolean execute : workflow) {
			jw.object().key("step").value(execute).endObject();
		}
		jw.endArray();

		HashSet<RootType> usedRootTypes = parameter.getUsedRootTypes();
		jw.key("usedRoots").array();
		for (RootType rt : usedRootTypes) {
			jw.object().key("root").value(rt).endObject();
		}
		jw.endArray();

		TaxonomyRanks ignoreBlankRank = parameter.getIgnoreBlankRank();
		jw.key("ignoreBlankRank").value(ignoreBlankRank);

		HashSet<String> excludeTaxa = parameter.getExcludeTaxa();
		jw.key("excludeTaxa").array();
		for (String et : excludeTaxa) {
			jw.object().key("excludeTaxon").value(et).endObject();
		}
		jw.endArray();

		int functionSampleId = parameter.getFunctionSampleId();
		jw.key("sample_id").value(functionSampleId);

		jw.endObject();

		writer.close();
	}
}
