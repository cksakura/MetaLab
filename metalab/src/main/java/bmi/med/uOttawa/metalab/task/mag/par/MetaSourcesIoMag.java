package bmi.med.uOttawa.metalab.task.mag.par;

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

public class MetaSourcesIoMag {

	private static Logger LOGGER = LogManager.getLogger(MetaSourcesIoMag.class);

	public static MetaSourcesMag parse(String json) {
		return parse(new File(json));
	}

	public static MetaSourcesMag parse(File json) {
		if (!json.exists() || json.length() == 0) {
			exportBlank(json);
		}

		StringBuilder sb = new StringBuilder();
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(json))) {
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

	public static MetaSourcesMag parse(JSONObject obj) {

		String version = obj.getString("version");
		String resource = obj.getString("resource");
		String msconvert = obj.has("msconvert") ? obj.getString("msconvert") : "";
		String pfind = obj.has("pfind") ? obj.getString("pfind") : "";
		String dbReducer = obj.has("dbReducer") ? obj.getString("dbReducer") : "";
		String flashlfq = obj.has("flashlfq") ? obj.getString("flashlfq") : "";
		String func = obj.has("function") ? obj.getString("function") : "";
		String funcDef = obj.has("funcDef") ? obj.getString("funcDef") : "";
		String fragpipe = obj.has("fragpipe") ? obj.getString("fragpipe") : "";
		String alphapept = obj.has("alphapept") ? obj.getString("alphapept") : "";
		String sage = obj.has("sage") ? obj.getString("sage") : "";

		MetaSourcesMag advancedParV2 = new MetaSourcesMag(version, resource, msconvert, func, funcDef, flashlfq, pfind,
				dbReducer, fragpipe, alphapept, sage);

		return advancedParV2;
	}

	public static void exportBlank(String out) {
		exportBlank(new File(out));
	}

	public static void exportBlank(File out) {

		try (PrintWriter writer = new PrintWriter(out)) {
			JSONWriter jw = new JSONWriter(writer);
			jw.object();

			jw.key("version").value(MetaParaIOMag.version);

			jw.key("resource").value("Resources\\");
			jw.key("msconvert").value("Resources\\ProteoWizard\\msconvert.exe");
			jw.key("pfind").value("Resources\\DBReducer\\pFind3\\bin\\pFind.exe");
			jw.key("dbReducer").value("Resources\\DBReducer\\DBReducer\\DBReducer.exe");
			jw.key("flashlfq").value("Resources\\FlashLFQ\\CMD.exe");
			jw.key("fragpipe").value("Resources\\fragpipe\\bin\\fragpipe.bat");
			jw.key("alphapept").value("Resources\\alphapept\\python.exe");
			jw.key("sage").value("Resources\\sage\\sage.exe");
			jw.key("function").value("Resources\\function\\UHGG_func_anno.db");
			jw.key("funcDef").value("Resources\\function\\func_def.db");

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

	}

	public static void export(MetaSourcesMag par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaSourcesMag par, File out) {

		try (PrintWriter writer = new PrintWriter(out)) {
			JSONWriter jw = new JSONWriter(writer);
			jw.object();

			jw.key("version").value(MetaParaIOMag.version);
			jw.key("resource").value(par.getResource());
			jw.key("msconvert").value(par.getMsconvert());
			jw.key("pfind").value(par.getpFind());
			jw.key("dbReducer").value(par.getDbReducer());
			jw.key("flashlfq").value(par.getFlashlfq());
			jw.key("fragpipe").value(par.getFragpipe());
			jw.key("alphapept").value(par.getAlphapept());
			jw.key("sage").value(par.getSage());
			jw.key("function").value(par.getFunction());
			jw.key("funcDef").value(par.getFuncDef());

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}
}
