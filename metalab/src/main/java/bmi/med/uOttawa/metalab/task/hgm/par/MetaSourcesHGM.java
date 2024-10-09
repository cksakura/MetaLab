package bmi.med.uOttawa.metalab.task.hgm.par;

import java.io.File;

import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

public class MetaSourcesHGM extends MetaSourcesV2 {

	public MetaSourcesHGM(String version, String resource, String func, String funcDef, String flashlfq, String pFind) {
		super(version, resource, "", "", func, funcDef);
		// TODO Auto-generated constructor stub
		this.flashlfq = flashlfq;
		this.pFind = pFind;
		File pFindFile = new File(pFind);
		this.pFindBin = pFindFile.getParentFile();
		this.pFindDefault = new File(pFindBin, "default");
	}

	public MetaSourcesHGM(String version, String resource, String func, String funcDef, String flashlfq, String pFind, String dbReducer) {
		super(version, resource, "", "", func, funcDef);
		// TODO Auto-generated constructor stub
		this.flashlfq = flashlfq;
		this.pFind = pFind;
		File pFindFile = new File(pFind);
		this.pFindBin = pFindFile.getParentFile();
		this.pFindDefault = new File(pFindBin, "default");
		this.dbReducer = dbReducer;
	}
}
