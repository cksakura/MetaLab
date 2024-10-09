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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaParameterIO;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;

/**
 * @author Kai Cheng
 *
 */
public class MetaParaIOMQ extends MetaParameterIO {

	public static final String version = "2.3.1";
	public static final String versionFile = "_2_3_1.json";

	private static Logger LOGGER = LogManager.getLogger(MetaParaIOMQ.class);

	public static MetaParameterMQ parse(String json) {
		return parse(new File(json));
	}

	public static MetaParameterMQ parse(File json) {

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

	public static MetaParameterMQ parse(JSONObject obj) {

		MetaParameter metaPar = MetaParameterIO.parseMain(obj);

		MetaLabWorkflowType workflowType = metaPar.getWorkflowType();

		MaxquantModification[] fixMods = new MaxquantModification[0];
		MaxquantModification[] variMods = new MaxquantModification[0];

		String enzymeString = "";
		Enzyme enzyme = Enzyme.TrypsinP;
		int missCleavages = 0;
		int digestMode = 0;
		String quanMode = "";
		boolean combineLabel = false;

		if (workflowType != MetaLabWorkflowType.TaxonomyAnalysis
				&& workflowType != MetaLabWorkflowType.FunctionalAnnotation) {

			HashMap<String, MaxquantModification> modMap = MaxQuantModIO.getModMap();

			ArrayList<MaxquantModification> fixModList = new ArrayList<MaxquantModification>();
			JSONArray fixArray = obj.getJSONArray("fixMods");
			if (fixArray != null) {
				for (int i = 0; i < fixArray.length(); i++) {
					String name = fixArray.getJSONObject(i).getString("name");
					if (modMap.containsKey(name)) {
						fixModList.add(modMap.get(name));
					}
				}
			}
			fixMods = fixModList.toArray(new MaxquantModification[fixModList.size()]);

			ArrayList<MaxquantModification> variModList = new ArrayList<MaxquantModification>();
			JSONArray variArray = obj.getJSONArray("variMods");
			if (variArray != null) {
				for (int i = 0; i < variArray.length(); i++) {
					String name = variArray.getJSONObject(i).getString("name");
					if (modMap.containsKey(name)) {
						variModList.add(modMap.get(name));
					}
				}
			}
			variMods = variModList.toArray(new MaxquantModification[variModList.size()]);

			enzymeString = obj.getString("enzyme");
			
			if (enzymeString.equals("Trypsin/P")) {
				enzyme = Enzyme.TrypsinP;
			} else if (enzymeString.equals("Lys/P")) {
				enzyme = Enzyme.LysCP;
			} else if (enzymeString.equals("ChymoTrypsin+")) {
				enzyme = Enzyme.ChymoTrypsinPlus;
			} else {
				enzyme = Enzyme.valueOf(enzymeString);
			}

			missCleavages = obj.has("missCleavages") ? obj.getInt("missCleavages") : 2;

			digestMode = obj.has("digestMode") ? obj.getInt("digestMode") : 0;

			quanMode = obj.has("quanMode") ? obj.getString("quanMode") : MetaConstants.labelFree;

			combineLabel = obj.has("combineLabel") ? obj.getBoolean("combineLabel") : false;
		}

		HashMap<String, MaxquantModification> isotopicMap = MaxQuantModIO.getIsotopicMap();

		ArrayList<MaxquantModification> lightList = new ArrayList<MaxquantModification>();
		JSONArray lightArray = obj.getJSONArray("light");
		if (lightArray != null) {
			for (int i = 0; i < lightArray.length(); i++) {
				String name = lightArray.getJSONObject(i).getString("name");
				if (isotopicMap.containsKey(name)) {
					lightList.add(isotopicMap.get(name));
				}
			}
		}
		MaxquantModification[] light = lightList.toArray(new MaxquantModification[lightList.size()]);

		ArrayList<MaxquantModification> mediumList = new ArrayList<MaxquantModification>();
		JSONArray mediumArray = obj.getJSONArray("medium");
		if (mediumArray != null) {
			for (int i = 0; i < mediumArray.length(); i++) {
				String name = mediumArray.getJSONObject(i).getString("name");
				if (isotopicMap.containsKey(name)) {
					mediumList.add(isotopicMap.get(name));
				}
			}
		}
		MaxquantModification[] medium = mediumList.toArray(new MaxquantModification[mediumList.size()]);

		ArrayList<MaxquantModification> heavyList = new ArrayList<MaxquantModification>();
		JSONArray heavyArray = obj.getJSONArray("heavy");
		if (heavyArray != null) {
			for (int i = 0; i < heavyArray.length(); i++) {
				String name = heavyArray.getJSONObject(i).getString("name");
				if (isotopicMap.containsKey(name)) {
					heavyList.add(isotopicMap.get(name));
				}
			}
		}
		MaxquantModification[] heavy = heavyList.toArray(new MaxquantModification[heavyList.size()]);

		MaxquantModification[][] labels = new MaxquantModification[][] { light, medium, heavy };

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

		double closedPsmFdr = obj.getDouble("closedPsmFdr");
		double closedProFdr = obj.getDouble("closedProFdr");
		boolean mbr = obj.getBoolean("matchBetweenRuns");
		double matchTimeWin = obj.getDouble("matchTimeWindow");
		double matchIonWin = obj.getDouble("matchIonWindow");
		double alignTimeWin = obj.getDouble("alignTimeWindow");
		double alignIonMobility = obj.getDouble("alignIonMobility");
		boolean matchUnidenFeature = obj.getBoolean("matchUnidenFeatures");
		int minRatioCount = obj.getInt("minRatioCount");
		int lfqMinRatioCount = obj.getInt("lfqMinRatioCount");

		MetaMaxQuantPar mcsp = new MetaMaxQuantPar(closedPsmFdr, closedProFdr, mbr, matchTimeWin, matchIonWin,
				alignTimeWin, alignIonMobility, matchUnidenFeature, minRatioCount, lfqMinRatioCount);

		boolean openSearch = obj.has("openSearch") ? obj.getBoolean("openSearch") : true;
		double openPsmFDR = obj.getDouble("openPsmFDR");
		double openPepFDR = obj.getDouble("openPepFDR");
		double openProFDR = obj.getDouble("openProFDR");
		double gaussianR2 = obj.getDouble("gaussianR2");
		boolean matchBetweenRuns = obj.has("mbr") ? obj.getBoolean("mbr") : true;

		MetaOpenPar mosp = new MetaOpenPar(openSearch, openPsmFDR, openPepFDR, openProFDR, gaussianR2,
				matchBetweenRuns);

		boolean buildIn = obj.getBoolean("buildIn");
		boolean unipept = obj.getBoolean("unipept");

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
		int leastPepCount = obj.getInt("leastPepCount");

		MetaPep2TaxaPar mptp = new MetaPep2TaxaPar(buildIn, unipept, usedRootSet, ignoreBlankRank, ignoreTaxaSet,
				leastPepCount);

		MetaParameterMQ parV2 = new MetaParameterMQ(metaPar, fixMods, variMods, enzyme, missCleavages, digestMode,
				quanMode, labels, isobaric, isobaricTag, combineLabel, isoCorFactor, openSearch, mcsp, mosp, mptp);

		return parV2;
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

		jw.key("workflowType").value(MetaLabWorkflowType.MaxQuantWorkflow.name());

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

		jw.key("threadCount").value(1);

		jw.key("isMetaWorkflow").value(true);

		jw.key("fixMods").array();
		jw.object().key("name").value("Carbamidomethyl (C)").endObject();
		jw.endArray();

		jw.key("variMods").array();
		jw.object().key("name").value("Oxidation (M)").endObject();
		jw.object().key("name").value("Acetyl (Protein N-term)").endObject();
		jw.endArray();

		jw.key("enzyme").value("Trypsin");

		jw.key("missCleavages").value(2);

		jw.key("digestMode").value(0);

		jw.key("quanMode").value(MetaConstants.labelFree);

		jw.key("combineLabel").value(false);

		jw.key("isobaricTag").value("");

		jw.key("light").array();
		jw.endArray();

		jw.key("medium").array();
		jw.endArray();

		jw.key("heavy").array();
		jw.endArray();

		jw.key("isobaric").array();
		jw.endArray();

		MetaSsdbCreatePar mscp = MetaSsdbCreatePar.getDefault();
		jw.key("ssdb").value(mscp.isSsdb());
		jw.key("cluster").value(mscp.isCluster());
		jw.key("ssOpen").value(mscp.isOpen());
		jw.key("xtandemFDR").value(mscp.getXtandemFDR());
		jw.key("xtandemEvalue").value(mscp.getXtandemEvalue());

		MetaMaxQuantPar mcp = MetaMaxQuantPar.getDefault();
		jw.key("closedPsmFdr").value(mcp.getClosedPsmFdr());
		jw.key("closedProFdr").value(mcp.getClosedProFdr());
		jw.key("matchBetweenRuns").value(mcp.isMbr());
		jw.key("matchTimeWindow").value(mcp.getMatchTimeWin());
		jw.key("matchIonWindow").value(mcp.getMatchIonWin());
		jw.key("alignTimeWindow").value(mcp.getAlignTimeWin());
		jw.key("alignIonMobility").value(mcp.getAlignIonMobility());
		jw.key("matchUnidenFeatures").value(mcp.isMatchUnidenFeature());
		jw.key("minRatioCount").value(mcp.getMinRatioCount());
		jw.key("lfqMinRatioCount").value(mcp.getLfqMinRatioCount());

		MetaOpenPar mop = MetaOpenPar.getDefault();
		jw.key("isOpenSearch").value(mop.isOpenSearch());
		jw.key("openPsmFDR").value(mop.getOpenPsmFDR());
		jw.key("openPepFDR").value(mop.getOpenPepFDR());
		jw.key("openProFDR").value(mop.getOpenProFDR());
		jw.key("gaussianR2").value(mop.getGaussianR2());
		jw.key("mbr").value(mop.isMatchBetweenRuns());

		MetaPep2TaxaPar mptp = MetaPep2TaxaPar.getDefault();
		jw.key("buildIn").value(mptp.isBuildIn());
		jw.key("unipept").value(mptp.isUnipept());

		HashSet<RootType> usedRootTypes = mptp.getUsedRootTypes();
		jw.key("usedRoots").array();
		for (RootType rt : usedRootTypes) {
			jw.object().key("root").value(rt).endObject();
		}
		jw.endArray();

		TaxonomyRanks ignoreBlankRank = mptp.getIgnoreBlankRank();
		jw.key("ignoreBlankRank").value(ignoreBlankRank);

		HashSet<String> excludeTaxa = mptp.getExcludeTaxa();
		jw.key("excludeTaxa").array();
		for (String et : excludeTaxa) {
			jw.object().key("excludeTaxon").value(et).endObject();
		}
		jw.endArray();

		jw.key("leastPepCount").value(mptp.getLeastPepCount());

		jw.endObject();

		writer.close();
	}

	public static void export(MetaParameterMQ par, String out) {
		export(par, new File(out));
	}

	public static void export(MetaParameterMQ par, File out) {

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

		int threadCount = par.getThreadCount();
		jw.key("threadCount").value(threadCount);

		boolean isMetaWorkflow = par.isMetaWorkflow();
		jw.key("isMetaWorkflow").value(isMetaWorkflow);

		MaxquantModification[] fixMods = par.getFixMods();
		jw.key("fixMods").array();
		for (MaxquantModification fm : fixMods) {
			jw.object().key("name").value(fm.getTitle()).endObject();
		}
		jw.endArray();

		MaxquantModification[] variMods = par.getVariMods();
		jw.key("variMods").array();
		for (MaxquantModification vm : variMods) {
			jw.object().key("name").value(vm.getTitle()).endObject();
		}
		jw.endArray();

		Enzyme enzyme = par.getEnzyme();
		jw.key("enzyme").value(enzyme.getName());

		int missCleavages = par.getMissCleavages();
		jw.key("missCleavages").value(missCleavages);

		int digestMode = par.getDigestMode();
		jw.key("digestMode").value(digestMode);

		String quantMode = par.getQuanMode();
		jw.key("quanMode").value(quantMode);

		boolean combineLabel = par.isCombineLabel();
		jw.key("combineLabel").value(combineLabel);

		MaxquantModification[][] labels = par.getLabels();
		jw.key("light").array();
		if (labels.length > 0) {
			for (MaxquantModification light : labels[0]) {
				jw.object().key("name").value(light.getTitle()).endObject();
			}
		}
		jw.endArray();

		jw.key("medium").array();
		if (labels.length > 1) {
			for (MaxquantModification medium : labels[1]) {
				jw.object().key("name").value(medium.getTitle()).endObject();
			}
		}
		jw.endArray();

		jw.key("heavy").array();
		if (labels.length > 2) {
			for (MaxquantModification heavy : labels[2]) {
				jw.object().key("name").value(heavy.getTitle()).endObject();
			}
		}
		jw.endArray();

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

		MetaSsdbCreatePar scp = par.getMscp() == null ? MetaSsdbCreatePar.getDefault() : par.getMscp();
		jw.key("ssdb").value(scp.isSsdb());
		jw.key("cluster").value(scp.isCluster());
		jw.key("ssOpen").value(scp.isOpen());
		jw.key("xtandemFDR").value(scp.getXtandemFDR());
		jw.key("xtandemEvalue").value(scp.getXtandemEvalue());

		MetaMaxQuantPar csp = par.getCsp() == null ? MetaMaxQuantPar.getDefault() : par.getCsp();
		jw.key("closedPsmFdr").value(csp.getClosedPsmFdr());
		jw.key("closedProFdr").value(csp.getClosedProFdr());
		jw.key("matchBetweenRuns").value(csp.isMbr());
		jw.key("matchTimeWindow").value(csp.getMatchTimeWin());
		jw.key("matchIonWindow").value(csp.getMatchIonWin());
		jw.key("alignTimeWindow").value(csp.getAlignTimeWin());
		jw.key("alignIonMobility").value(csp.getAlignIonMobility());
		jw.key("matchUnidenFeatures").value(csp.isMatchUnidenFeature());
		jw.key("minRatioCount").value(csp.getMinRatioCount());
		jw.key("lfqMinRatioCount").value(csp.getLfqMinRatioCount());

		MetaOpenPar mop = par.getOsp() == null ? MetaOpenPar.getDefault() : par.getOsp();
		jw.key("isOpenSearch").value(mop.isOpenSearch());
		jw.key("openPsmFDR").value(mop.getOpenPsmFDR());
		jw.key("openPepFDR").value(mop.getOpenPepFDR());
		jw.key("openProFDR").value(mop.getOpenProFDR());
		jw.key("gaussianR2").value(mop.getGaussianR2());
		jw.key("mbr").value(mop.isMatchBetweenRuns());

		MetaPep2TaxaPar mptp = par.getPtp() == null ? MetaPep2TaxaPar.getDefault() : par.getPtp();
		jw.key("buildIn").value(mptp.isBuildIn());
		jw.key("unipept").value(mptp.isUnipept());

		HashSet<RootType> usedRootTypes = mptp.getUsedRootTypes();
		jw.key("usedRoots").array();
		for (RootType rt : usedRootTypes) {
			jw.object().key("root").value(rt).endObject();
		}
		jw.endArray();

		TaxonomyRanks ignoreBlankRank = mptp.getIgnoreBlankRank();
		jw.key("ignoreBlankRank").value(ignoreBlankRank);

		HashSet<String> excludeTaxa = mptp.getExcludeTaxa();
		jw.key("excludeTaxa").array();
		for (String et : excludeTaxa) {
			jw.object().key("excludeTaxon").value(et).endObject();
		}
		jw.endArray();

		jw.key("leastPepCount").value(mptp.getLeastPepCount());

		jw.endObject();

		writer.close();
	}
}
