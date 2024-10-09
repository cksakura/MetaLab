package bmi.med.uOttawa.metalab.task.mag.par;

import java.io.File;

import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;

public class MetaSourcesMag extends MetaSourcesHGM {

	private String fragpipe;
	private String alphapept;

	public MetaSourcesMag(String version, String resource, String func, String funcDef, String flashlfq, String pFind,
			String dbReducer) {
		super(version, resource, func, funcDef, flashlfq, pFind, dbReducer);
		// TODO Auto-generated constructor stub
	}

	public MetaSourcesMag(String version, String resource, String func, String funcDef, String flashlfq, String pFind,
			String dbReducer, String fragpipe, String alphapept) {
		super(version, resource, func, funcDef, flashlfq, pFind, dbReducer);
		// TODO Auto-generated constructor stub
		this.fragpipe = fragpipe;
		this.alphapept = alphapept;
	}

	public void setFragpipe(String fragpipe) {
		this.fragpipe = fragpipe;
	}

	public String getFragpipe() {
		return fragpipe;
	}

	public boolean findFragpipe() {
		if (this.fragpipe == null) {
			return false;
		} else {
			return (new File(this.fragpipe)).exists();
		}
	}

	public String getAlphapept() {
		return alphapept;
	}

	public void setAlphapept(String alphapept) {
		this.alphapept = alphapept;
	}

	public boolean findAlphapept() {
		if (this.alphapept == null) {
			return false;
		} else {
			return (new File(this.alphapept)).exists();
		}
	}

}
