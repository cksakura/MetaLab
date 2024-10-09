package bmi.med.uOttawa.metalab.task.dia.par;

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

public class MetaSourcesIoDia {

	private static Logger LOGGER = LogManager.getLogger(MetaSourcesIoDia.class);

	public static MetaSourcesDia parse(String json) {
		return parse(new File(json));
	}

	public static MetaSourcesDia parse(File json) {
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

	public static MetaSourcesDia parse(JSONObject obj) {

		String version = obj.getString("version");
		String resource = obj.getString("resource");
		String diann = obj.getString("DiaNN");
		String deepDetect = obj.getString("DeepDetect");
		String funcDef = obj.getString("funcDef");
		String python = obj.getString("python");

		MetaSourcesDia advancedParV2 = new MetaSourcesDia(version, resource, funcDef, diann, deepDetect, python);

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

		jw.key("version").value(MetaParaIoDia.version);
		jw.key("resource").value("Resources\\");
		jw.key("DiaNN").value("Resources\\DIA-NN\\1.8\\DiaNN.exe");
		jw.key("DeepDetect").value("Resources\\DeepDetect\\DeepDetect.exe");
		jw.key("python").value("Resources\\tf\\python.exe");
		jw.key("funcDef").value("Resources\\function\\func_def.db");

		jw.endObject();

		writer.close();
	}

	public static void export(MetaSourcesDia par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaSourcesDia par, File out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}

		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		jw.key("version").value(MetaParaIoDia.version);
		jw.key("resource").value(par.getResource());
		jw.key("DiaNN").value(par.getDiann());
		jw.key("DeepDetect").value(par.getDeepDetect());
		jw.key("python").value(par.getPython());
		jw.key("funcDef").value(par.getFuncDef());

		jw.endObject();

		writer.close();
	}
}
