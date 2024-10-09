package bmi.med.uOttawa.metalab.dbSearch.dbReducer;

import java.io.File;
import java.io.IOException;

import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindConfig;

public class DBReducerConfig extends PFindConfig {

	public DBReducerConfig(File cfgFile) {
		super(cfgFile);
		// TODO Auto-generated constructor stub
	}

	protected void parse(File cfgFile) {
		try {
			findMap = this.parseMap(cfgFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File searchMgf(String[] raws, String result, String db, String ms2Type, double psmFdr, String[] fixedMods,
			String[] variMods) {

		StringBuilder fixsb = new StringBuilder();
		for (int i = 0; i < fixedMods.length; i++) {
			fixsb.append(fixedMods[i].substring(0, fixedMods[i].indexOf(" "))).append(";");
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
		this.findMap.get("ProteinDatabase").setValue(db);
		this.findMap.get("SearchRes").setValue(result);
		this.findMap.get("threshold").setValue(String.valueOf(psmFdr));

		this.findMap.get("activation_type").setValue(ms2Type);
		this.findMap.get("msmsnum").setValue(String.valueOf(raws.length));

		this.findMap.get("msmstype").setValue("MGF");

		StringBuilder findRawSb = new StringBuilder(raws[0]);

		for (int i = 1; i < raws.length; i++) {
			findRawSb.append("\n");
			findRawSb.append("msmspath").append(i + 1).append("=").append(raws[i]);
		}

		this.findMap.get("msmspath1").setValue(findRawSb.toString());

		try {
			this.pFindCfg = new File(result, "DBReducer.cfg");
			this.export(pFindCfg, findMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pFindCfg;
	}

}
