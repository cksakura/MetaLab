package bmi.med.uOttawa.metalab.task.dia.par;

import java.io.File;

import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;

public class MetaSourcesDia extends MetaSourcesMag {

	private String diann;
	private String deepDetect;
	private String python;

	public MetaSourcesDia(String version, String resource, String funcDef, String diann, String deepDetect,
			String python) {
		super(version, resource, "", funcDef, "", "", "");
		// TODO Auto-generated constructor stub
		this.diann = diann;
		this.deepDetect = deepDetect;
		this.python = python;
	}

	public void setDiann(String diann) {
		this.diann = diann;
	}

	public String getDiann() {
		return diann;
	}

	public boolean findDiaNN() {
		if (this.diann == null) {
			return false;
		} else {
			return (new File(this.diann)).exists();
		}
	}

	public String getDeepDetect() {
		return deepDetect;
	}

	public void setDeepDetect(String deepDetect) {
		this.deepDetect = deepDetect;
	}

	public boolean findDeepDetect() {
		if (this.deepDetect == null) {
			return false;
		} else {
			return (new File(this.deepDetect)).exists();
		}
	}

	public String getPython() {
		return python;
	}

	public void setPython(String python) {
		this.python = python;
	}

	public boolean findPython() {
		if (this.python == null) {
			return false;
		} else {
			return (new File(this.python)).exists();
		}
	}
}
