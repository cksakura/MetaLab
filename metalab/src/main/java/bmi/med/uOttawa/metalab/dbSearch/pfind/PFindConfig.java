/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class PFindConfig {

	private File pFindBin;

	protected HashMap<String, PFindItem> findMap;
	private HashMap<String, PFindItem> parseMap;
	private HashMap<String, PFindItem> quantMap;

	protected File pFindCfg;
	private File pParseCfg;
	private File pQuantCfg;

	public PFindConfig(File pFindBin) {
		this.pFindBin = pFindBin;
		if (pFindBin.isDirectory()) {
			File searcher = new File(pFindBin, "Searcher.exe");
			parse(searcher);
		} else if (pFindBin.getName().endsWith("cfg")) {
			parse(pFindBin);
		}
	}
	
	protected void parse(File searcher) {
		Runtime runtime = Runtime.getRuntime();
		try {

			String path = "";
			Process p = runtime.exec(new String[] { searcher.getAbsolutePath() });
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				if (lineStr.startsWith("An example of param file is generated")) {
					path = inBr.readLine();
				}
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();

			File pFindCfg = new File(path);

			if (pFindCfg.exists()) {
				try {
					this.findMap = this.parseMap(pFindCfg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				FileUtils.deleteQuietly(pFindCfg);

			} else {
				pFindCfg = new File(pFindBin + "\\template\\param\\pFind.cfg");
				if (pFindCfg.exists()) {
					try {
						this.findMap = this.parseMap(pFindCfg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.err.println("No parameter file found. Please run Searcher.exe in " + pFindBin
							+ " to generate a parameter file.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		File pParseCfg = new File(pFindBin + "\\template\\param\\pParse.cfg");
		if (pParseCfg.exists()) {
			try {
				this.parseMap = this.parseMap(pParseCfg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File pQuantCfg = new File(pFindBin + "\\template\\param\\pQuant.cfg");
		if (pParseCfg.exists()) {
			try {
				this.quantMap = this.parseMap(pQuantCfg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public File getpFindCfg() {
		return pFindCfg;
	}

	public File getpParseCfg() {
		return pParseCfg;
	}

	public File getpQuantCfg() {
		return pQuantCfg;
	}

	public void setCoreThread(int cores, int thread) {
		this.findMap.get("multip").setValue(String.valueOf(cores));
		this.findMap.get("thread").setValue(String.valueOf(thread));
	}

	public File config(String result, String[] raws, String db, String ms2Type, boolean isMgf, boolean isOpenSearch) {

		File pFindTask = new File(result, "pFindTask");
		if (!pFindTask.exists()) {
			pFindTask.mkdirs();
		}

		File pFindPara = new File(pFindTask, "param");
		if (!pFindPara.exists()) {
			pFindPara.mkdir();
		}

		File pFindResult = new File(pFindTask, "result");
		if (!pFindResult.exists()) {
			pFindResult.mkdir();
		}

		this.writeTask(new File(pFindTask, "pFindTask.tsk"));

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
			this.findMap.get("fixmod").setValue("");
			this.findMap.get("selectmod").setValue("");
		} else {
			this.findMap.get("open").setValue("0");
			this.findMap.get("fixmod").setValue("Carbamidomethyl[C];");
			this.findMap.get("selectmod").setValue("Acetyl[ProteinN-term];Oxidation[M];");
		}

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(pFindResult.getAbsolutePath());
		this.findMap.get("outputname").setValue("pFindTask");

		this.quantMap.get("PATH_INI_ELEMENT").setValue(new File(pFindBin, "element.ini").getAbsolutePath());
		this.quantMap.get("PATH_INI_MODIFICATION").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.quantMap.get("PATH_INI_RESIDUE").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.quantMap.get("PATH_BIN").setValue(pFindBin.getAbsolutePath());
		this.quantMap.get("PATH_FASTA").setValue(db + ";");
		this.quantMap.get("DIR_EXPORT").setValue(pFindResult.getAbsolutePath() + ";");

		this.parseMap.get("datanum").setValue(String.valueOf(raws.length));
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		if (isMgf) {

			StringBuilder findRawSb = new StringBuilder(raws[0]);
			for (int i = 1; i < raws.length; i++) {
				findRawSb.append("\n");
				findRawSb.append("msmspath").append(i + 1).append("=").append(raws[i]);
			}
			this.findMap.get("msmspath1").setValue(findRawSb.toString());
			try {
				this.pFindCfg = new File(pFindPara, "pFind.cfg");
				this.export(pFindCfg, findMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			String suffix = "";

			if (ms2Type.equals(MetaConstants.HCD_FTMS)) {
				suffix = "_HCDFT.pf2";
				this.findMap.get("msmstol").setValue("10");
				this.findMap.get("msmstolppm").setValue("1");
			} else if (ms2Type.equals(MetaConstants.HCD_ITMS)) {
				suffix = "_HCDIT.pf2";
				this.findMap.get("msmstol").setValue("1");
				this.findMap.get("msmstolppm").setValue("0");
			} else if (ms2Type.equals(MetaConstants.CID_FTMS)) {
				suffix = "_CIDFT.pf2";
				this.findMap.get("msmstol").setValue("10");
				this.findMap.get("msmstolppm").setValue("1");
			} else if (ms2Type.equals(MetaConstants.CID_ITMS)) {
				suffix = "_CIDIT.pf2";
				this.findMap.get("msmstol").setValue("1");
				this.findMap.get("msmstolppm").setValue("0");
			}

			StringBuilder parseRawSb = new StringBuilder(raws[0]);
			StringBuilder findRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + suffix);
			StringBuilder quantRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + "pf1");

			for (int i = 1; i < raws.length; i++) {
				parseRawSb.append("\n");
				parseRawSb.append("datapath").append(i + 1).append("=").append(raws[i]);

				findRawSb.append("\n");
				findRawSb.append("msmspath").append(i + 1).append("=")
						.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + suffix);

				quantRawSb.append("|");
				quantRawSb.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + "pf1");
			}

			this.parseMap.get("datapath1").setValue(parseRawSb.toString());
			this.findMap.get("msmspath1").setValue(findRawSb.toString());
			this.quantMap.get("PATH_MS1").setValue(quantRawSb.toString() + "|;");

			try {
				this.pFindCfg = new File(pFindPara, "pFind.cfg");
				this.export(pFindCfg, findMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				this.pParseCfg = new File(pFindPara, "pParse.cfg");
				this.export(pParseCfg, parseMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				this.pQuantCfg = new File(pFindPara, "pQuant.cfg");
				this.export(pQuantCfg, quantMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		File cmd = new File(pFindTask, "pfind.cmd");

		return cmd;
	}

	/**
	 * @deprecated
	 * @param raw
	 * @return
	 */
	public File extract(String raw) {

		File rawFile = new File(raw);
		File parent = rawFile.getParentFile();
		String name = rawFile.getName();

		this.parseMap.get("co-elute").setValue("1");
		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());
		this.parseMap.get("datanum").setValue("1");
		this.parseMap.get("datapath1").setValue(raw);
		this.parseMap.get("input_format").setValue("raw");

		try {
			this.pParseCfg = new File(parent, name.substring(0, name.lastIndexOf(".")) + ".pParse.cfg");
			this.export(pParseCfg, parseMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pParseCfg;
	}

	/**
	 * @deprecated
	 * @param co_elute
	 * @param extractRaws
	 * @return
	 * @throws IOException
	 */
	public File extract(int co_elute, String... extractRaws) throws IOException {

		File rawFile = new File(extractRaws[0]);
		File parent = rawFile.getParentFile();

		this.parseMap.get("co-elute").setValue(String.valueOf(co_elute));
		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());
		this.parseMap.get("datanum").setValue(String.valueOf(extractRaws.length));

		if (extractRaws.length > 0) {
			StringBuilder sb = new StringBuilder(extractRaws[0]);
			for (int i = 1; i < extractRaws.length; i++) {
				sb.append("\n");
				sb.append("datapath" + (i + 1));
				sb.append("=");
				sb.append(extractRaws[i]);
			}

			this.parseMap.get("datapath1").setValue(sb.toString());
		}

		this.parseMap.get("input_format").setValue("raw");

		this.pParseCfg = new File(parent, "ms2Spctra.pParse.cfg");
		this.export(pParseCfg, parseMap);

		return pParseCfg;
	}

	/**
	 * @deprecated
	 * @param co_elute
	 * @param spectraDir
	 * @param extractRaws
	 * @return
	 * @throws IOException
	 */
	public File extract(int co_elute, String spectraDir, String... extractRaws) throws IOException {

		this.parseMap.get("co-elute").setValue(String.valueOf(co_elute));
		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());
		this.parseMap.get("datanum").setValue(String.valueOf(extractRaws.length));

		if (extractRaws.length > 0) {
			StringBuilder sb = new StringBuilder(extractRaws[0]);
			for (int i = 1; i < extractRaws.length; i++) {
				sb.append("\n");
				sb.append("datapath" + (i + 1));
				sb.append("=");
				sb.append(extractRaws[i]);
			}

			this.parseMap.get("datapath1").setValue(sb.toString());
		}

		this.parseMap.get("input_format").setValue("raw");

		this.pParseCfg = new File(spectraDir, "ms2Spctra.pParse.cfg");
		this.export(pParseCfg, parseMap);

		return pParseCfg;
	}

	/**
	 * Set "output_mgf" as "0" because the mgf files should be converted from ms2 files but not use the automatic output here
	 * @param co_elute
	 * @param spectraDir
	 * @param extractRaw
	 * @param rawName
	 * @return
	 * @throws IOException
	 */
	public File extractSingle(int co_elute, String spectraDir, String extractRaw, String rawName) throws IOException {

		this.parseMap.get("co-elute").setValue(String.valueOf(co_elute));
		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());
		this.parseMap.get("datanum").setValue("1");
		this.parseMap.get("datapath1").setValue(extractRaw);

		this.parseMap.get("outputpath").setValue(spectraDir);

		this.parseMap.get("input_format").setValue("raw");
		this.parseMap.get("output_mgf").setValue("0");

		this.pParseCfg = new File(spectraDir, rawName + ".pParse.cfg");
		this.export(pParseCfg, parseMap);

		return pParseCfg;
	}

	public File searchRaw(String raw, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev) {

		File rawFile = new File(raw);
		File parent = rawFile.getParentFile();
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char a = name.charAt(i);
			if (Character.isLetterOrDigit(a) || a == '_' || a == '-') {
				sb.append(a);
			}
		}

		File resultFile = new File(result, sb.toString());
		if (!resultFile.exists()) {
			resultFile.mkdirs();
		}

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
			this.findMap.get("fixmod").setValue("");
			this.findMap.get("selectmod").setValue("");
			this.findMap.get("digest").setValue("3");
		} else {
			this.findMap.get("open").setValue("0");
			this.findMap.get("fixmod").setValue("Carbamidomethyl[C];");
			this.findMap.get("selectmod").setValue("Acetyl[ProteinN-term];Oxidation[M];");
			this.findMap.get("digest").setValue("3");
		}

		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(resultFile.getAbsolutePath());
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("PF2");
		this.findMap.get("msmsnum").setValue("1");

		String suffix = "";

		if (ms2Type.equals("HCD-FTMS")) {
			suffix = "_HCDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals("HCD-ITMS")) {
			suffix = "_HCDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		} else if (ms2Type.equals("CID-ITMS")) {
			suffix = "_CIDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals("CID-ITMS")) {
			suffix = "_CIDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		}

		this.findMap.get("msmspath1").setValue(name + suffix);

		try {
			this.pFindCfg = new File(parent, name + ".pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchRaw(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr) {

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
			this.findMap.get("fixmod").setValue("");
			this.findMap.get("selectmod").setValue("");
			this.findMap.get("digest").setValue("3");
		} else {
			this.findMap.get("open").setValue("0");
			this.findMap.get("fixmod").setValue("Carbamidomethyl[C];");
			this.findMap.get("selectmod").setValue("Acetyl[ProteinN-term];Oxidation[M];");
			this.findMap.get("digest").setValue("3");
		}

		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("psm_fdr").setValue(String.valueOf(psmFdr));

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);

		String name = (new File(db)).getName();
		name = name.substring(0, name.length() - ".fasta".length());
		File resultFile = new File(result);
		resultFile.mkdir();

		this.findMap.get("outputpath").setValue(result);
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("PF2");
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		String suffix = "";

		if (ms2Type.equals("HCD-FTMS")) {
			suffix = "_HCDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals("HCD-ITMS")) {
			suffix = "_HCDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		} else if (ms2Type.equals("CID-ITMS")) {
			suffix = "_CIDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals("CID-ITMS")) {
			suffix = "_CIDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		}

		StringBuilder findRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + suffix);

		for (int i = 1; i < raws.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=")
					.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + suffix);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(resultFile, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchRaw(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestType, IsobaricTag tag) {

		StringBuilder fixsb = new StringBuilder();
		for (int i = 0; i < fixedMods.length; i++) {
			fixsb.append(fixedMods[i].substring(0, fixedMods[i].indexOf(" "))).append(";");
		}

		if (tag == null) {
			if (fixsb.length() > 0) {
				fixsb.deleteCharAt(fixsb.length() - 1);
			}
		} else {
			if (tag.getName().startsWith("TMT2")) {
				fixsb.append("TMT2plex[AnyN-term]").append(";");
				fixsb.append("TMT2plex[K]");
			} else if (tag.getName().startsWith("iodoTMT6")) {
				fixsb.append("iodoTMT6plex[K]");
			} else if (tag.getName().startsWith("iTRAQ4")) {
				fixsb.append("iTRAQ4plex114[AnyN-term]").append(";");
				fixsb.append("iTRAQ4plex114[K]");
			} else if (tag.getName().startsWith("iTRAQ8")) {
				fixsb.append("iTRAQ8plex[AnyN-term]").append(";");
				fixsb.append("iTRAQ8plex[K]");
			} else {
				fixsb.append("TMT6plex[AnyN-term]").append(";");
				fixsb.append("TMT6plex[K]");
			}
		}

		StringBuilder varisb = new StringBuilder();
		for (int i = 0; i < variMods.length; i++) {
			varisb.append(variMods[i].substring(0, variMods[i].indexOf(" "))).append(";");
		}
		if (varisb.length() > 0) {
			varisb.deleteCharAt(varisb.length() - 1);
		}

		this.findMap.get("fixmod").setValue(fixsb.toString());
		this.findMap.get("selectmod").setValue(varisb.toString());

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
		} else {
			this.findMap.get("open").setValue("0");
		}

		this.findMap.get("max_clv_sites").setValue(String.valueOf(missCleavages));

		switch (digestType) {
		case 0:
			this.findMap.get("digest").setValue("3");
			break;
		case 3:
			this.findMap.get("digest").setValue("1");
			break;
		case 4:
			this.findMap.get("digest").setValue("0");
			break;
		default:
			this.findMap.get("digest").setValue("3");
			break;
		}

		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("psm_fdr").setValue(String.valueOf(psmFdr));

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(result);
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("PF2");
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		String suffix = "";

		if (ms2Type.equals(MetaConstants.HCD_FTMS)) {
			suffix = "_HCDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.HCD_ITMS)) {
			suffix = "_HCDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		} else if (ms2Type.equals(MetaConstants.CID_FTMS)) {
			suffix = "_CIDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.CID_ITMS)) {
			suffix = "_CIDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		}

		StringBuilder findRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + suffix);

		for (int i = 1; i < raws.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=")
					.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + suffix);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(result, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchPF2(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestType, IsobaricTag tag) {

		StringBuilder fixsb = new StringBuilder();
		for (int i = 0; i < fixedMods.length; i++) {
			fixsb.append(fixedMods[i].substring(0, fixedMods[i].indexOf(" "))).append(";");
		}
		if (tag == null) {
			if (fixsb.length() > 0) {
				fixsb.deleteCharAt(fixsb.length() - 1);
			}
		} else {
			if (tag.getName().startsWith("TMT2")) {
				fixsb.append("TMT2plex[AnyN-term]").append(";");
				fixsb.append("TMT2plex[K]");
			} else if (tag.getName().startsWith("TMTpro")) {
				fixsb.append("TMTpro[AnyN-term]").append(";");
				fixsb.append("TMTpro[K]");
			} else if (tag.getName().startsWith("iodoTMT6")) {
				fixsb.append("iodoTMT6plex[K]");
			} else if (tag.getName().startsWith("iTRAQ4")) {
				fixsb.append("iTRAQ4plex114[AnyN-term]").append(";");
				fixsb.append("iTRAQ4plex114[K]");
			} else if (tag.getName().startsWith("iTRAQ8")) {
				fixsb.append("iTRAQ8plex[AnyN-term]").append(";");
				fixsb.append("iTRAQ8plex[K]");
			} else {
				fixsb.append("TMT6plex[AnyN-term]").append(";");
				fixsb.append("TMT6plex[K]");
			}
		}

		StringBuilder varisb = new StringBuilder();
		for (int i = 0; i < variMods.length; i++) {
			varisb.append(variMods[i].substring(0, variMods[i].indexOf(" "))).append(";");
		}
		if (varisb.length() > 0) {
			varisb.deleteCharAt(varisb.length() - 1);
		}

		this.findMap.get("fixmod").setValue(fixsb.toString());
		this.findMap.get("selectmod").setValue(varisb.toString());

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
		} else {
			this.findMap.get("open").setValue("0");
		}
		this.findMap.get("len_lower").setValue("6");

		this.findMap.get("max_clv_sites").setValue(String.valueOf(missCleavages));

		switch (digestType) {
		case 0:
			this.findMap.get("digest").setValue("3");
			break;
		case 3:
			this.findMap.get("digest").setValue("1");
			break;
		case 4:
			this.findMap.get("digest").setValue("0");
			break;
		default:
			this.findMap.get("digest").setValue("3");
			break;
		}

		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("psm_fdr").setValue(String.valueOf(psmFdr));

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(result);
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("PF2");
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		if (ms2Type.equals(MetaConstants.HCD_FTMS)) {
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.HCD_ITMS)) {
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		} else if (ms2Type.equals(MetaConstants.CID_FTMS)) {
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.CID_ITMS)) {
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		}

		StringBuilder findRawSb = new StringBuilder(raws[0]);

		for (int i = 1; i < raws.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=").append(raws[i]);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(result, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchMgf(String mgf, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev) {

		File rawFile = new File(mgf);
		File parent = rawFile.getParentFile();
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char a = name.charAt(i);
			if (Character.isLetterOrDigit(a) || a == '_' || a == '-') {
				sb.append(a);
			}
		}

		File resultFile = new File(result, sb.toString());
		if (!resultFile.exists()) {
			resultFile.mkdirs();
		}

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
			this.findMap.get("fixmod").setValue("");
			this.findMap.get("selectmod").setValue("");
			this.findMap.get("digest").setValue("3");
		} else {
			this.findMap.get("open").setValue("0");
			this.findMap.get("fixmod").setValue("Carbamidomethyl[C];");
			this.findMap.get("selectmod").setValue("Acetyl[ProteinN-term];Oxidation[M];");
			this.findMap.get("digest").setValue("3");
		}
		this.findMap.get("len_lower").setValue("6");
		
		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(resultFile.getAbsolutePath());
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("MGF");
		this.findMap.get("msmsnum").setValue("1");
		this.findMap.get("msmspath1").setValue(mgf);

		try {
			this.pFindCfg = new File(parent, name + ".pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchMgf(String[] mgfs, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr) {

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
			this.findMap.get("fixmod").setValue("");
			this.findMap.get("selectmod").setValue("");
			this.findMap.get("digest").setValue("3");
		} else {
			this.findMap.get("open").setValue("0");
			this.findMap.get("fixmod").setValue("Carbamidomethyl[C];");
			this.findMap.get("selectmod").setValue("Acetyl[ProteinN-term];Oxidation[M];");
			this.findMap.get("digest").setValue("3");
		}
		this.findMap.get("len_lower").setValue("6");
		
		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("psm_fdr").setValue(String.valueOf(psmFdr));

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(result);
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("MGF");
		this.findMap.get("msmsnum").setValue(String.valueOf(mgfs.length));

		StringBuilder findRawSb = new StringBuilder(mgfs[0]);

		for (int i = 1; i < mgfs.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=").append(mgfs[i]);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(result, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public File searchMgf(String[] mgfs, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestType, IsobaricTag tag) {

		StringBuilder fixsb = new StringBuilder();
		for (int i = 0; i < fixedMods.length; i++) {
			fixsb.append(fixedMods[i].substring(0, fixedMods[i].indexOf(" "))).append(";");
		}
		if (tag == null) {
			if (fixsb.length() > 0) {
				fixsb.deleteCharAt(fixsb.length() - 1);
			}
		} else {
			if (tag.getName().startsWith("TMT2")) {
				fixsb.append("TMT2plex[AnyN-term]").append(";");
				fixsb.append("TMT2plex[K]");
			} else if (tag.getName().startsWith("TMTpro")) {
				fixsb.append("TMT2plex[AnyN-term]").append(";");
				fixsb.append("TMT2plex[K]");
			} else if (tag.getName().startsWith("iodoTMT6")) {
				fixsb.append("iodoTMT6plex[K]");
			} else if (tag.getName().startsWith("iTRAQ4")) {
				fixsb.append("iTRAQ4plex114[AnyN-term]").append(";");
				fixsb.append("iTRAQ4plex114[K]");
			} else if (tag.getName().startsWith("iTRAQ8")) {
				fixsb.append("iTRAQ8plex[AnyN-term]").append(";");
				fixsb.append("iTRAQ8plex[K]");
			} else {
				fixsb.append("TMT6plex[AnyN-term]").append(";");
				fixsb.append("TMT6plex[K]");
			}
		}

		StringBuilder varisb = new StringBuilder();
		for (int i = 0; i < variMods.length; i++) {
			varisb.append(variMods[i].substring(0, variMods[i].indexOf(" "))).append(";");
		}
		if (varisb.length() > 0) {
			varisb.deleteCharAt(varisb.length() - 1);
		}

		this.findMap.get("fixmod").setValue(fixsb.toString());
		this.findMap.get("selectmod").setValue(varisb.toString());

		if (isOpenSearch) {
			this.findMap.get("open").setValue("1");
		} else {
			this.findMap.get("open").setValue("0");
		}
		this.findMap.get("len_lower").setValue("6");
		this.findMap.get("max_clv_sites").setValue(String.valueOf(missCleavages));

		switch (digestType) {
		case 0:
			this.findMap.get("digest").setValue("3");
			break;
		case 3:
			this.findMap.get("digest").setValue("1");
			break;
		case 4:
			this.findMap.get("digest").setValue("0");
			break;
		default:
			this.findMap.get("digest").setValue("3");
			break;
		}

		if (isAutoRev) {
			this.findMap.get("auto_rev").setValue("1");
		} else {
			this.findMap.get("auto_rev").setValue("0");
		}

		this.findMap.get("psm_fdr").setValue(String.valueOf(psmFdr));

		this.findMap.get("mstol").setValue("10");
		this.findMap.get("mstolppm").setValue("1");

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(db);
		this.findMap.get("outputpath").setValue(result);
		this.findMap.get("outputname").setValue("pFindTask");

		this.findMap.get("msmstype").setValue("MGF");
		this.findMap.get("msmsnum").setValue(String.valueOf(mgfs.length));

		StringBuilder findRawSb = new StringBuilder(mgfs[0]);

		for (int i = 1; i < mgfs.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=").append(mgfs[i]);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(result, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

	public void config(File result, String[] raws, String fasta, String ms2Type) {

		File pFindTask = new File(result, "pFindTask");
		if (!pFindTask.exists()) {
			pFindTask.mkdirs();
		}

		File pFindPara = new File(pFindTask, "param");
		if (!pFindPara.exists()) {
			pFindPara.mkdir();
		}

		File pFindResult = new File(pFindTask, "result");
		if (!pFindResult.exists()) {
			pFindResult.mkdir();
		}

		this.writeTask(new File(pFindTask, "pFindTask.tsk"));

		this.parseMap.get("ipv_file").setValue(new File(pFindBin, "IPV.txt").getAbsolutePath());
		this.parseMap.get("trainingset").setValue(new File(pFindBin, "TrainingSet.txt").getAbsolutePath());

		this.findMap.get("len_lower").setValue("6");
		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("modpath").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.findMap.get("aapath").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.findMap.get("fastapath").setValue(fasta);
		this.findMap.get("outputpath").setValue(pFindResult.getAbsolutePath());
		this.findMap.get("outputname").setValue("pFindTask");

		this.quantMap.get("PATH_INI_ELEMENT").setValue(new File(pFindBin, "element.ini").getAbsolutePath());
		this.quantMap.get("PATH_INI_MODIFICATION").setValue(new File(pFindBin, "modification.ini").getAbsolutePath());
		this.quantMap.get("PATH_INI_RESIDUE").setValue(new File(pFindBin, "aa.ini").getAbsolutePath());
		this.quantMap.get("PATH_BIN").setValue(pFindBin.getAbsolutePath());
		this.quantMap.get("PATH_FASTA").setValue(fasta + ";");
		this.quantMap.get("DIR_EXPORT").setValue(pFindResult.getAbsolutePath() + ";");

		this.parseMap.get("datanum").setValue(String.valueOf(raws.length));
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		String suffix = "";

		if (ms2Type.equals(MetaConstants.HCD_FTMS)) {
			suffix = "_HCDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.HCD_ITMS)) {
			suffix = "_HCDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		} else if (ms2Type.equals(MetaConstants.CID_FTMS)) {
			suffix = "_CIDFT.pf2";
			this.findMap.get("msmstol").setValue("10");
			this.findMap.get("msmstolppm").setValue("1");
		} else if (ms2Type.equals(MetaConstants.CID_ITMS)) {
			suffix = "_CIDIT.pf2";
			this.findMap.get("msmstol").setValue("1");
			this.findMap.get("msmstolppm").setValue("0");
		}
		StringBuilder parseRawSb = new StringBuilder(raws[0]);
		StringBuilder findRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + suffix);
		StringBuilder quantRawSb = new StringBuilder(raws[0].substring(0, raws[0].lastIndexOf(".")) + "pf1");

		for (int i = 1; i < raws.length; i++) {
			parseRawSb.append("\n");
			parseRawSb.append("datapath").append(i + 1).append("=").append(raws[i]);

			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=")
					.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + suffix);

			quantRawSb.append("|");
			quantRawSb.append(raws[i].substring(0, raws[i].lastIndexOf(".")) + "pf1");
		}

		this.parseMap.get("datapath1").setValue(parseRawSb.toString());
		this.findMap.get("msmspath1").setValue(findRawSb.toString());
		this.quantMap.get("PATH_MS1").setValue(quantRawSb.toString() + "|;");

		try {
			this.pFindCfg = new File(pFindPara, "pFind.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.pParseCfg = new File(pFindPara, "pParse.cfg");
			this.export(pParseCfg, parseMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.pQuantCfg = new File(pFindPara, "pQuant.cfg");
			this.export(pQuantCfg, quantMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeTask(File taskOutput) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(taskOutput);
			writer.println("pFind Studio Task File, Format Version 3.0");
			writer.println("# pFind 3.0");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, PFindItem> parseMap(String params) throws IOException {
		return parseMap(new File(params));
	}

	public HashMap<String, PFindItem> parseMap(File params) throws IOException {

		HashMap<String, PFindItem> valueMap = new HashMap<String, PFindItem>();

		int id = 1;
		String pre_anno = "";
		boolean pre_blank = false;
		BufferedReader reader = new BufferedReader(new FileReader(params));
		String line = null;
		while ((line = reader.readLine()) != null) {

			if (line.startsWith("[")) {

				if (line.startsWith("[About")) {
					if (!valueMap.containsKey("outputpath")) {
						PFindItem item = new PFindItem(id++, false, "outputpath", "", "", false);
						valueMap.put("outputpath", item);
					}
				}

				PFindItem item = new PFindItem(id++, true, line, "", pre_anno, pre_blank);
				valueMap.put(line, item);

			} else {

				if (line.startsWith("#")) {
					pre_anno += line + "\n";
					continue;
				}

				if (line.trim().length() == 0) {
					pre_blank = true;
					continue;
				}

				int l1 = line.indexOf("=");
				if (l1 < 0) {
					continue;
				}

				String key = line.substring(0, l1).trim();
				String value = line.substring(l1 + 1).trim();

				PFindItem item = new PFindItem(id++, false, key, value, pre_anno, pre_blank);
				valueMap.put(key, item);
			}

			pre_anno = "";
			pre_blank = false;
		}
		reader.close();

		return valueMap;
	}

	public void setRawFiles(String[] raws) {

	}

	protected static class PFindItem implements Comparable<PFindItem> {

		private int id;
		private boolean title;
		private String key;
		private String value;
		private String pre_annotation;
		private boolean pre_blank;

		PFindItem(int id, boolean title, String key, String value, String pre_annotation, boolean pre_blank) {
			this.id = id;
			this.title = title;
			this.key = key;
			this.value = value;
			this.pre_annotation = pre_annotation;
			this.pre_blank = pre_blank;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (title) {
				if (pre_blank) {
					sb.append("\n");
				}
				sb.append(key).append("\n");
			} else {
				if (pre_blank) {
					sb.append("\n");
				}
				if (pre_annotation != null) {
					sb.append(pre_annotation);
				}

				sb.append(key).append("=").append(value).append("\n");
			}

			return sb.toString();
		}

		@Override
		public int compareTo(PFindItem o) {
			// TODO Auto-generated method stub
			return this.id - o.id;
		}
	}

	public void export(String file, HashMap<String, PFindItem> valueMap) throws IOException {
		export(new File(file), valueMap);
	}

	public void export(File file, HashMap<String, PFindItem> valueMap) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		PFindItem[] items = valueMap.values().toArray(new PFindItem[valueMap.size()]);
		Arrays.sort(items);

		for (int id = 0; id < items.length; id++) {
			writer.print(items[id]);
		}
		writer.close();
	}
}
