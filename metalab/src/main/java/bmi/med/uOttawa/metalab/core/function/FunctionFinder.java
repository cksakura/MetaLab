/**
 * 
 */
package bmi.med.uOttawa.metalab.core.function;

import java.io.File;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;

/**
 * @author Kai Cheng
 *
 */
public abstract class FunctionFinder {

	protected HashMap<String, String> functionMap;
	protected String db;
	protected String fullName;
	protected String abbreviation;

	public FunctionFinder(String db, String fullName, String abbreviation) {
		this.db = db;
		this.fullName = fullName;
		this.abbreviation = abbreviation;

		this.functionMap = new HashMap<String, String>();
	}

	public HashMap<String, String> getFunctionMap() {
		return functionMap;
	}

	public boolean isUsable() {
		File dbfile = new File(db);
		return dbfile.exists();
	}

	public abstract void match(MetaProteinAnno1[] proteins);

	public String getFullName() {
		return fullName;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}
