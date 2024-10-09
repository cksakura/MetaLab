/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.File;

/**
 * @author Kai Cheng
 *
 */
public abstract class AbstractMetaPeptideReader {

	private File file;

	public AbstractMetaPeptideReader(String in) {
		this(new File(in));
	}

	public AbstractMetaPeptideReader(File in) {
		this.file = in;
	}

	public File getFile() {
		return file;
	}

	public abstract String getQuanMode();
	
	public abstract MetaPeptide[] getMetaPeptides();
	
	public abstract Object[] getTitleObjs();
	
	public abstract String[] getIntensityTitle();
}
