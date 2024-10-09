/**
 * 
 */
package bmi.med.uOttawa.metalab.task.par;

import java.io.File;

/**
 * The basic settings of MetaLab.
 * 
 * @author Kai Cheng
 *
 */
public class MetaSources {

	private String version;
	private String resource;
	private String pep2tax;
	private String taxonAll;
	private String function;
	private String funcDef;
	
	public MetaSources(String version, String resource, String pep2tax, String taxonAll, String function,
			String funcDef) {
		this.version = version;
		this.resource = resource;
		this.pep2tax = pep2tax;
		this.taxonAll = taxonAll;
		this.function = function;
		this.funcDef = funcDef;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public String getResource() {
		return resource;
	}

	public String getPep2tax() {
		return pep2tax;
	}

	public String getTaxonAll() {
		return taxonAll;
	}

	public String getFunction() {
		return function;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setPep2tax(String pep2tax) {
		this.pep2tax = pep2tax;
	}

	public void setTaxonAll(String taxonAll) {
		this.taxonAll = taxonAll;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getFuncDef() {
		return funcDef;
	}

	public void setFuncDef(String funcDef) {
		this.funcDef = funcDef;
	}

	public boolean findPep2tax() {
		if (this.pep2tax == null) {
			return false;
		} else {
			return (new File(this.pep2tax)).exists();
		}
	}

	public boolean findTaxonAll() {
		if (this.taxonAll == null) {
			return false;
		} else {
			return (new File(this.taxonAll)).exists();
		}
	}

	public boolean findFunction() {
		if (this.function == null) {
			return false;
		} else {
			return (new File(this.function)).exists();
		}
	}

	public boolean funcAnnoSqlite() {
		if (this.function == null) {
			return false;
		} else {
			File file = new File(this.function);
			if (file.exists()) {
				if (file.isDirectory()) {
					return false;
				}

				if (file.isFile() && file.getName().equals("func.db")) {
					return true;
				}
			} else {
				return false;
			}
			return false;
		}
	}

}
