package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.File;

public abstract class AbstractMetaProteinReader {

	private File file;

	public AbstractMetaProteinReader(String in) {
		this(new File(in));
	}

	public AbstractMetaProteinReader(File in) {
		this.file = in;
	}

	public File getFile() {
		return file;
	}

	public abstract String getQuanMode();

	public abstract MetaProtein[] getMetaProteins();

	public abstract Object[] getTitleObjs();
	
	public abstract String[] getIntensityTitle();
}
