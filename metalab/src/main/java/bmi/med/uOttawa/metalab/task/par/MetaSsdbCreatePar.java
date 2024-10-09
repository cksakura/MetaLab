package bmi.med.uOttawa.metalab.task.par;

/**
 * parameter for database generation
 * 
 * @author Kai Cheng
 *
 */
public class MetaSsdbCreatePar {

	private boolean ssdb;
	private boolean cluster;
	private boolean isOpen;
	private double xtandemFDR;
	private double xtandemEvalue;

	/**
	 * Used in pFind
	 * 
	 * @param ssdb
	 * @param cluster
	 * @param fdr
	 */
	public MetaSsdbCreatePar(boolean ssdb, boolean cluster, double fdr) {
		this.ssdb = ssdb;
		this.cluster = cluster;
		this.isOpen = false;
		this.xtandemFDR = fdr;
	}

	/**
	 * Used in MSFragger based strategy
	 * 
	 * @param ssdb
	 * @param cluster
	 * @param xtandemFDR
	 * @param xtandemEvalue
	 */
	public MetaSsdbCreatePar(boolean ssdb, boolean cluster, double xtandemFDR, double xtandemEvalue) {
		this.ssdb = ssdb;
		this.cluster = cluster;
		this.isOpen = false;
		this.xtandemFDR = xtandemFDR;
		this.xtandemEvalue = xtandemEvalue;
	}

	/**
	 * Used in pFind
	 * 
	 * @param ssdb
	 * @param cluster
	 * @param fdr
	 */
	public MetaSsdbCreatePar(boolean ssdb, boolean cluster, boolean isOpen, double fdr) {
		this.ssdb = ssdb;
		this.cluster = cluster;
		this.isOpen = isOpen;
		this.xtandemFDR = fdr;
	}

	/**
	 * Used in MSFragger based strategy
	 * 
	 * @param ssdb
	 * @param cluster
	 * @param xtandemFDR
	 * @param xtandemEvalue
	 */
	public MetaSsdbCreatePar(boolean ssdb, boolean cluster, boolean isOpen, double xtandemFDR, double xtandemEvalue) {
		this.ssdb = ssdb;
		this.cluster = cluster;
		this.isOpen = isOpen;
		this.xtandemFDR = xtandemFDR;
		this.xtandemEvalue = xtandemEvalue;
	}

	public boolean isSsdb() {
		return ssdb;
	}

	public void setSsdb(boolean ssdb) {
		this.ssdb = ssdb;
	}

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public double getXtandemFDR() {
		return xtandemFDR;
	}

	public void setXtandemFDR(double xtandemFDR) {
		this.xtandemFDR = xtandemFDR;
	}

	public double getXtandemEvalue() {
		return xtandemEvalue;
	}

	public void setXtandemEvalue(double xtandemEvalue) {
		this.xtandemEvalue = xtandemEvalue;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public static MetaSsdbCreatePar getDefault() {
		MetaSsdbCreatePar mscp = new MetaSsdbCreatePar(true, true, false, 0.01, 0.05);
		return mscp;
	}
}
