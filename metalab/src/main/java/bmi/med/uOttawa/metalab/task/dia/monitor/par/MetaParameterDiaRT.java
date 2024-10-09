package bmi.med.uOttawa.metalab.task.dia.monitor.par;

import java.io.File;

import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;

public class MetaParameterDiaRT extends MetaParameterDia {
	
	private boolean combineResult;
	private boolean stopNow;
	private boolean stopComplete;
	private int taskCount;
	private File monitorFolder;
	private File resultFolder;

	public MetaParameterDiaRT(MetaParameter metaPar, String magDb, String magDbVersion, boolean librarySearch,
			boolean selfModelSearch, String[] library, boolean useModel, String[] models) {
		super(metaPar, magDb, magDbVersion, librarySearch, selfModelSearch, library, useModel, models);
		// TODO Auto-generated constructor stub
	}

	public boolean isCombineResult() {
		return combineResult;
	}

	public void setCombineResult(boolean combineResult) {
		this.combineResult = combineResult;
	}

	public boolean isStopNow() {
		return stopNow;
	}

	public void setStopNow(boolean stopNow) {
		this.stopNow = stopNow;
	}

	public boolean isStopComplete() {
		return stopComplete;
	}

	public void setStopComplete(boolean stopComplete) {
		this.stopComplete = stopComplete;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public File getMonitorFolder() {
		return monitorFolder;
	}

	public void setMonitorFolder(File monitorFolder) {
		this.monitorFolder = monitorFolder;
	}

	public File getResultFolder() {
		return resultFolder;
	}

	public void setResultFolder(File resultFolder) {
		this.resultFolder = resultFolder;
	}

	
}
