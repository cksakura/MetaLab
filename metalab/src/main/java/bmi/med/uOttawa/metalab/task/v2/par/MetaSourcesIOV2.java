/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * @author Kai Cheng
 *
 */
public class MetaSourcesIOV2 {

	private static Logger LOGGER = LogManager.getLogger(MetaSourcesIOV2.class);

	public static MetaSourcesV2 parse(String json) {
		return parse(new File(json));
	}

	public static MetaSourcesV2 parse(File json) {
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

	public static MetaSourcesV2 parse(JSONObject obj) {

		String version = obj.getString("version");
		String resource = obj.getString("resource");
		String maxquant = obj.getString("maxquant");
		String xtandem = obj.has("xtandem") ? obj.getString("xtandem") : "";
		String ProteomicsTools = obj.has("ProteomicsTools") ? obj.getString("ProteomicsTools") : "";
		String msconvert = obj.has("msconvert") ? obj.getString("msconvert") : "";
		String msfragger = obj.has("msfragger") ? obj.getString("msfragger") : "";
		String pfind = obj.has("pfind") ? obj.getString("pfind") : "";
		String dbReducer = obj.has("DBReducer") ? obj.getString("DBReducer") : "";
		String flashlfq = obj.has("flashlfq") ? obj.getString("flashlfq") : "";

		String pep2tax = obj.getString("pep2tax");
		String function = obj.getString("function");
		String funcDef = obj.getString("funcDef");
		String taxonAll = obj.getString("taxonAll");
		String unimod = obj.getString("unimod");

		MetaSourcesV2 advancedParV2 = new MetaSourcesV2(version, resource, maxquant, xtandem, ProteomicsTools,
				msfragger, pfind, dbReducer, flashlfq, pep2tax, function, funcDef, taxonAll, unimod);
		advancedParV2.setMsconvert(msconvert);

		return advancedParV2;
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

		jw.key("version").value(MetaParaIOMQ.version);

		jw.key("resource").value("Resources\\");
		jw.key("maxquant").value("Resources\\mq_bin\\MaxQuantCmd.exe");
		jw.key("xtandem").value("Resources\\tandem\\bin\\tandem.exe");
		jw.key("ProteomicsTools").value("Resources\\ProteomicsTools\\ProteomicsTools.exe");
		jw.key("msconvert").value("Resources\\ProteoWizard\\msconvert.exe");
		jw.key("pfind").value("Resources\\DBReducer\\pFind3\\bin\\pFind.exe");
		jw.key("DBReducer").value("Resources\\DBReducer\\DBReducer\\DBReducer.exe");
		jw.key("flashlfq").value("Resources\\FlashLFQ\\CMD.exe");

		File taxonFile = new File("Resources\\pep2taxa\\pep2taxa.gz");
		if (taxonFile.exists()) {
			jw.key("pep2tax").value("Resources\\pep2taxa\\pep2taxa.gz");
		} else {
			jw.key("pep2tax").value("Resources\\pep2taxa\\pep2taxa.tab");
		}

		jw.key("function").value("Resources\\function\\func.db");
		jw.key("funcDef").value("Resources\\function\\func_def.db");
		jw.key("taxonAll").value("Resources\\taxonomy-all.tab");
		jw.key("unimod").value("Resources\\unimod.xml");

		jw.endObject();

		writer.close();
	}

	public static void export(MetaSourcesV2 par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaSourcesV2 par, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		jw.key("version").value(MetaParaIOMQ.version);

		jw.key("resource").value(par.getResource());
		jw.key("maxquant").value(par.getMaxquant());
		jw.key("xtandem").value(par.getXtandem());
		jw.key("ProteomicsTools").value(par.getProteomicsTools());
		jw.key("msconvert").value(par.getMsconvert());
		jw.key("pfind").value(par.getpFind());
		jw.key("DBReducer").value(par.getDbReducer());
		jw.key("flashlfq").value(par.getFlashlfq());

		jw.key("pep2tax").value(par.getPep2tax());
		jw.key("function").value(par.getFunction());
		jw.key("funcDef").value(par.getFuncDef());
		jw.key("taxonAll").value(par.getTaxonAll());
		jw.key("unimod").value(par.getUnimod());

		jw.endObject();

		writer.close();
	}
}
