/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.par;

import java.io.File;

/**
 * @author Kai Cheng
 */
public abstract class AbstractParameter {

	private String resource;
	private String metalabOutput;

	private File resultDir;
	private File ParameterDir;
	private File fastaDir;

	public AbstractParameter(String resource, String metalabOutput) {
		this.resource = resource;
		this.metalabOutput = metalabOutput;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getMetalabOutput() {
		return metalabOutput;
	}

	public void setResult(String metalabOutput) {
		this.metalabOutput = metalabOutput;
		this.resultDir = new File(metalabOutput + "\\result");
		if (!this.resultDir.exists()) {
			this.resultDir.mkdir();
		}
		this.ParameterDir = new File(metalabOutput + "\\parameter");
		if (!this.ParameterDir.exists()) {
			this.ParameterDir.mkdir();
		}
		this.fastaDir = new File(metalabOutput + "\\database");
		if (!this.fastaDir.exists()) {
			this.fastaDir.mkdir();
		}
	}

	public File getResultDir() {
		this.resultDir = new File(metalabOutput + "\\result");
		if (!this.resultDir.exists()) {
			this.resultDir.mkdir();
		}
		return resultDir;
	}

	public File getParameterDir() {
		this.ParameterDir = new File(metalabOutput + "\\parameter");
		if (!this.ParameterDir.exists()) {
			this.ParameterDir.mkdir();
		}
		return ParameterDir;
	}

	public File getFastaDir() {
		this.fastaDir = new File(metalabOutput + "\\database");
		if (!this.fastaDir.exists()) {
			this.fastaDir.mkdir();
		}
		return fastaDir;
	}

	public String getPep2taxa() {
		File pep2taxGz = new File(resource + "\\db\\pep2taxa.gz");
		File pep2taxTab = new File(resource + "\\db\\pep2taxa.tab");
		if (pep2taxTab.exists()) {
			return pep2taxTab.getAbsolutePath();
		} else {
			if (pep2taxGz.exists()) {
				return pep2taxGz.getAbsolutePath();
			} else {
				return "";
			}
		}
	}

	public String getTaxDb() {
		return resource + "\\taxonomy-all.tab";
	}

	public String getXTandemCmd() {
		return resource + "\\tandem\\bin\\tandem.exe";
	}

	public String getXTandemDefaultInput() {
		return resource + "\\tandem\\bin\\default_input.xml";
	}

	public String getXTandemInput() {
		return resource + "\\tandem\\bin\\input.xml";
	}

	public String getXTandemTaxonomy() {
		return resource + "\\tandem\\bin\\taxonomy.xml";
	}

	public String getCogCategory() {
		return resource + "\\function\\Category_COG.gc";
	}

	public String getNogCategory() {
		return resource + "\\function\\Category_NOG.gc";
	}

}
