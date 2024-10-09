/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.par;

import java.io.File;

import bmi.med.uOttawa.metalab.task.par.MetaSources;

/**
 * @author Kai Cheng
 *
 */
public class MetaSourcesV2 extends MetaSources {

	protected String maxquant;
	protected String xtandem;
	protected String ProteomicsTools;
	protected String msfragger;
	protected String flashlfq;
	protected String unimod;
	protected String pFind;
	protected String msconvert;
	protected File pFindBin;
	protected File pFindDefault;
	protected String dbReducer;

	/**
	 * MetaSourcesHGM inherit this constructor
	 * 
	 * @param resource
	 * @param pep2tax
	 * @param taxonAll
	 * @param function
	 * @param funcDef
	 */
	public MetaSourcesV2(String verion, String resource, String pep2tax, String taxonAll, String function,
			String funcDef) {
		super(verion, resource, pep2tax, taxonAll, function, funcDef);
	}

	/**
	 * Used for the construction of MetaSourcesV2
	 * 
	 * @param resource
	 * @param maxquant
	 * @param xtandem
	 * @param proteomicsTools
	 * @param msfragger
	 * @param pFind
	 * @param flashlfq
	 * @param pep2tax
	 * @param function
	 * @param funcDef
	 * @param taxonAll
	 * @param unimod
	 */
	public MetaSourcesV2(String verion, String resource, String maxquant, String xtandem, String proteomicsTools,
			String msfragger, String pFind, String dbReducer, String flashlfq, String pep2tax, String function, String funcDef,
			String taxonAll, String unimod) {

		super(verion, resource, pep2tax, taxonAll, function, funcDef);

		this.maxquant = maxquant;
		this.xtandem = xtandem;
		this.ProteomicsTools = proteomicsTools;
		this.msfragger = msfragger;
		this.flashlfq = flashlfq;
		this.pFind = pFind;
		this.unimod = unimod;

		File pFindFile = new File(pFind);
		this.pFindBin = pFindFile.getParentFile();
		this.pFindDefault = new File(pFindBin, "default");
		this.dbReducer = dbReducer;
	}

	public String getMaxquant() {
		return maxquant;
	}

	public void setMaxquant(String maxquant) {
		this.maxquant = maxquant;
	}

	public String getXtandem() {
		return xtandem;
	}

	public void setXtandem(String xtandem) {
		this.xtandem = xtandem;
	}

	public String getProteomicsTools() {
		return ProteomicsTools;
	}

	public void setProteomicsTools(String proteomicsTools) {
		ProteomicsTools = proteomicsTools;
	}

	public String getMsconvert() {
		return msconvert;
	}

	public void setMsconvert(String msconvert) {
		this.msconvert = msconvert;
	}

	public String getMsfragger() {
		return msfragger;
	}

	public void setMsfragger(String msfragger) {
		this.msfragger = msfragger;
	}

	public String getpFind() {
		return pFind;
	}

	public void setpFind(String pFind) {
		this.pFind = pFind;
	}

	public String getFlashlfq() {
		return flashlfq;
	}

	public void setFlashlfq(String flashlfq) {
		this.flashlfq = flashlfq;
	}

	public String getUnimod() {
		return unimod;
	}

	public void setUnimod(String unimod) {
		this.unimod = unimod;
	}

	public String getDbReducer() {
		return dbReducer;
	}

	public void setDbReducer(String dbReducer) {
		this.dbReducer = dbReducer;
	}

	public boolean findMSConvert() {
		if (this.msconvert == null) {
			return false;
		} else {
			return (new File(this.msconvert)).exists();
		}
	}

	public boolean findMaxQuant() {
		if (this.maxquant == null) {
			return false;
		} else {
			return (new File(this.maxquant)).exists();
		}
	}

	public boolean findXTandem() {
		if (this.xtandem == null) {
			return false;
		} else {
			return (new File(this.xtandem)).exists();
		}
	}

	public boolean findPT() {
		if (this.ProteomicsTools == null) {
			return false;
		} else {
			return (new File(this.ProteomicsTools)).exists();
		}
	}

	public boolean findMSFragger() {
		if (this.msfragger == null) {
			return false;
		} else {
			return (new File(this.msfragger)).exists();
		}
	}

	public boolean findPFind() {
		if (this.pFind == null) {
			return false;
		} else {
			return (new File(this.pFind)).exists();
		}
	}

	public boolean findFlashLFQ() {
		if (this.flashlfq == null) {
			return false;
		} else {
			return (new File(this.flashlfq)).exists();
		}
	}

	public boolean findUnimod() {
		if (this.unimod == null) {
			return false;
		} else {
			return (new File(this.unimod)).exists();
		}
	}

	public File getEnzyme() {
		return new File(this.pFindDefault, "enzyme_default.ini");
	}

	public File getMod() {
		return new File(this.pFindDefault, "modification_default.ini");
	}
}
