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

import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaParameterIO;

public class MetaParIODiaSearchLib {

	public static final String version = "DIA 1.0";
	public static final String versionFile = "_DIA_1_0.json";

	private static Logger LOGGER = LogManager.getLogger(MetaParIODiaSearchLib.class);

	public static MetaDiaParSearchLib parse(String json) {
		return parse(new File(json));
	}

	public static MetaDiaParSearchLib parse(File json) {

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

	public static MetaDiaParSearchLib parse(JSONObject obj) {

		MetaParameter metaPar = MetaParameterIO.parseMain(obj);

		String magDbItem = obj.has("MagDb") ? obj.getString("MagDb") : "";
		String magDbVersuib = obj.has("MagDbVersion") ? obj.getString("MagDbVersion") : "";

		DiaLibSearchPar diaLibSearchPar = new DiaLibSearchPar();
		diaLibSearchPar.setMassAccu(obj.getDouble("MassAccu"));
		diaLibSearchPar.setMs1Accu(obj.getDouble("MS1Accu"));
		diaLibSearchPar.setScanWin(obj.getDouble("ScanWin"));
		diaLibSearchPar.setqValue(obj.getDouble("Qvalue"));
		diaLibSearchPar.setMbr(obj.getBoolean("MBR"));
		diaLibSearchPar.setQuanStrategyId(obj.getInt("QuanStrategyId"));
		diaLibSearchPar.setThreadCount(metaPar.getThreadCount());

		MetaDiaParSearchLib parDia = new MetaDiaParSearchLib(metaPar, magDbItem, magDbVersuib, diaLibSearchPar);

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

			jw.key("workflowType").value(MetaLabWorkflowType.DiaNNMAG.name());

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

			jw.key("MS2ScanMode").value("");

			jw.key("threadCount").value(1);

			jw.key("isMetaWorkflow").value(true);

			jw.key("MagDb").value("");

			jw.key("MagDbVersion").value("");

			DiaLibSearchPar diaNNPar = new DiaLibSearchPar();

			jw.key("MassAccu").value(diaNNPar.getMassAccu());
			jw.key("MS1Accu").value(diaNNPar.getMs1Accu());
			jw.key("ScanWin").value(diaNNPar.getScanWin());
			jw.key("Qvalue").value(diaNNPar.getqValue());
			jw.key("MBR").value(diaNNPar.isMbr());
			jw.key("QuanStrategyId").value(diaNNPar.getQuanStrategyId());

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}

	public static void export(MetaDiaParSearchLib par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaDiaParSearchLib par, File out) {

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

			String result = par.getResult();
			jw.key("result").value(result);

			String microDb = par.getMicroDb();
			jw.key("microDb").value(microDb);

			String hostDb = par.getHostDB();
			jw.key("hostDb").value(hostDb);

			boolean appendHostDb = par.isAppendHostDb();
			jw.key("appendHostDb").value(appendHostDb);

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

			DiaLibSearchPar diaNNPar = par.getDiaLibSearchPar();

			jw.key("MassAccu").value(diaNNPar.getMassAccu());
			jw.key("MS1Accu").value(diaNNPar.getMs1Accu());
			jw.key("ScanWin").value(diaNNPar.getScanWin());
			jw.key("Qvalue").value(diaNNPar.getqValue());
			jw.key("MBR").value(diaNNPar.isMbr());
			jw.key("QuanStrategyId").value(diaNNPar.getQuanStrategyId());

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}
}
