package bmi.med.uOttawa.metalab.task.v2.par;

public class MetaOpenPar {

	private boolean isOpenSearch;
	private double openPsmFDR;
	private double openPepFDR;
	private double openProFDR;
	private double gaussianR2;
	private boolean matchBetweenRuns;

	/**
	 * Used in pFind
	 * 
	 * @param openPsmFDR
	 * @param openProFDR
	 */
	public MetaOpenPar(boolean isOpenSearch, double openPsmFDR, double openProFDR, boolean matchBetweenRuns) {
		this.isOpenSearch = isOpenSearch;
		this.openPsmFDR = openPsmFDR;
		this.openProFDR = openProFDR;
		this.matchBetweenRuns = matchBetweenRuns;
	}

	/**
	 * Used in MSFragger based strategy
	 * 
	 * @param openPsmFDR
	 * @param openPepFDR
	 * @param openProFDR
	 * @param gaussianR2
	 */
	public MetaOpenPar(boolean isOpenSearch, double openPsmFDR, double openPepFDR, double openProFDR, double gaussianR2,
			boolean matchBetweenRuns) {
		this.isOpenSearch = isOpenSearch;
		this.openPsmFDR = openPsmFDR;
		this.openPepFDR = openPepFDR;
		this.openProFDR = openProFDR;
		this.gaussianR2 = gaussianR2;
		this.matchBetweenRuns = matchBetweenRuns;
	}

	public double getOpenPsmFDR() {
		return openPsmFDR;
	}

	public void setOpenPsmFDR(double openPsmFDR) {
		this.openPsmFDR = openPsmFDR;
	}

	public double getOpenPepFDR() {
		return openPepFDR;
	}

	public void setOpenPepFDR(double openPepFDR) {
		this.openPepFDR = openPepFDR;
	}

	public double getOpenProFDR() {
		return openProFDR;
	}

	public void setOpenProFDR(double openProFDR) {
		this.openProFDR = openProFDR;
	}

	public double getGaussianR2() {
		return gaussianR2;
	}

	public void setGaussianR2(double gaussianR2) {
		this.gaussianR2 = gaussianR2;
	}

	public static MetaOpenPar getDefault() {
		MetaOpenPar mop = new MetaOpenPar(true, 0.01, 0.01, 0.01, 0.9, true);
		return mop;
	}

	public boolean isOpenSearch() {
		return isOpenSearch;
	}

	public void setOpenSearch(boolean isOpenSearch) {
		this.isOpenSearch = isOpenSearch;
	}

	public boolean isMatchBetweenRuns() {
		return matchBetweenRuns;
	}

	public void setMatchBetweenRuns(boolean matchBetweenRuns) {
		this.matchBetweenRuns = matchBetweenRuns;
	}

}
