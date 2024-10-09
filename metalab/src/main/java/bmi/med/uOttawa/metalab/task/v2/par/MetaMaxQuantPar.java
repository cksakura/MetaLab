package bmi.med.uOttawa.metalab.task.v2.par;

public class MetaMaxQuantPar {

	private double closedPsmFdr;
	private double closedProFdr;
	private boolean mbr;

	private double matchTimeWin;
	private double matchIonWin;
	private double alignTimeWin;
	private double alignIonMobility;
	private boolean matchUnidenFeature;

	private int minRatioCount;
	private int lfqMinRatioCount;

	public MetaMaxQuantPar(double closedPsmFdr, double closedProFdr, boolean mbr, double matchTimeWin,
			double matchIonWin, double alignTimeWin, double alignIonMobility, boolean matchUnidenFeature,
			int minRatioCount, int lfqMinRatioCount) {
		this.closedPsmFdr = closedPsmFdr;
		this.closedProFdr = closedProFdr;
		this.mbr = mbr;
		this.matchTimeWin = matchTimeWin;
		this.matchIonWin = matchIonWin;
		this.alignTimeWin = alignTimeWin;
		this.alignIonMobility = alignIonMobility;
		this.matchUnidenFeature = matchUnidenFeature;
		this.minRatioCount = minRatioCount;
		this.lfqMinRatioCount = lfqMinRatioCount;
	}

	public double getClosedPsmFdr() {
		return closedPsmFdr;
	}

	public void setClosedPsmFdr(double closedPsmFdr) {
		this.closedPsmFdr = closedPsmFdr;
	}

	public double getClosedProFdr() {
		return closedProFdr;
	}

	public void setClosedProFdr(double closedProFdr) {
		this.closedProFdr = closedProFdr;
	}

	public boolean isMbr() {
		return mbr;
	}

	public void setMbr(boolean mbr) {
		this.mbr = mbr;
	}

	public double getMatchTimeWin() {
		return matchTimeWin;
	}

	public void setMatchTimeWin(double matchTimeWin) {
		this.matchTimeWin = matchTimeWin;
	}

	public double getMatchIonWin() {
		return matchIonWin;
	}

	public void setMatchIonWin(double matchIonWin) {
		this.matchIonWin = matchIonWin;
	}

	public double getAlignTimeWin() {
		return alignTimeWin;
	}

	public void setAlignTimeWin(double alignTimeWin) {
		this.alignTimeWin = alignTimeWin;
	}

	public double getAlignIonMobility() {
		return alignIonMobility;
	}

	public void setAlignIonMobility(double alignIonMobility) {
		this.alignIonMobility = alignIonMobility;
	}

	public boolean isMatchUnidenFeature() {
		return matchUnidenFeature;
	}

	public void setMatchUnidenFeature(boolean matchUnidenFeature) {
		this.matchUnidenFeature = matchUnidenFeature;
	}

	public int getMinRatioCount() {
		return minRatioCount;
	}

	public void setMinRatioCount(int minRatioCount) {
		this.minRatioCount = minRatioCount;
	}

	public int getLfqMinRatioCount() {
		return lfqMinRatioCount;
	}

	public void setLfqMinRatioCount(int lfqMinRatioCount) {
		this.lfqMinRatioCount = lfqMinRatioCount;
	}

	public static MetaMaxQuantPar getDefault() {
		MetaMaxQuantPar mcp = new MetaMaxQuantPar(0.01, 0.01, true, 0.7, 0.05, 20, 1, false, 1, 1);
		return mcp;
	}
}
