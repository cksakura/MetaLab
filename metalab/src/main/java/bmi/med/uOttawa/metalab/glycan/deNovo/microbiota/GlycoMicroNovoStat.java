package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.util.HashSet;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;

public class GlycoMicroNovoStat {

	private int dataCount = 50;
	private int[] rank1DeterminedScore;
	private int[] rank1UndeterminedScore;
	private int[] rank2DeterminedScore;
	private int[] rank2UndeterminedScore;

	private int determinedRank1Count;
	private int determinedRank2Count;
	private int undeterminedRank1Count;
	private int undeterminedRank2Count;

	private int[] scoreTP;
	private int[] scoreTN;
	private int[] scoreFP;
	private int[] scoreFN;

	private int classTP;
	private int classTN;
	private int classFP;
	private int classFN;

	private int qValue1;
	private int qValue5;

	private int[] determinedGlycoCount;
	private int[] undeterminedGlycoCount;
	private Glycosyl[] glycosyls;

	public GlycoMicroNovoStat(Glycosyl[] glycosyls) {
		this.rank1DeterminedScore = new int[dataCount];
		this.rank1UndeterminedScore = new int[dataCount];
		this.rank2DeterminedScore = new int[dataCount];
		this.rank2UndeterminedScore = new int[dataCount];

		this.scoreTP = new int[dataCount];
		this.scoreTN = new int[dataCount];
		this.scoreFP = new int[dataCount];
		this.scoreFN = new int[dataCount];

		this.glycosyls = glycosyls;
		this.determinedGlycoCount = new int[glycosyls.length];
		this.undeterminedGlycoCount = new int[glycosyls.length];
	}

	public void addItem(NovoGlycoCompMatch match) {
		int rank = match.getRank();
		boolean determined = match.isDetermined();

		double score = match.getScore();
		int scoreId = (int) (score >= dataCount * 0.2 ? dataCount - 1 : score / 0.2);
		if (scoreId < 0)
			scoreId = 0;

		double classScore = match.getClassScore();
		double qvalue = match.getqValue();

		int[] composition = match.getComposition();
		if (rank == 1) {
			if (determined) {
				determinedRank1Count++;
				rank1DeterminedScore[scoreId]++;
				for (int i = 0; i < composition.length; i++) {
					determinedGlycoCount[i] += composition[i];
				}
				for (int i = 0; i < scoreId; i++) {
					scoreTP[i]++;
				}
				for (int i = scoreId; i < dataCount; i++) {
					scoreFN[i]++;
				}
				if (qvalue < 0.01) {
					qValue1++;
				}

				if (qvalue < 0.05) {
					qValue5++;
				}

				if (classScore < 0.5) {
					classTP++;
				} else {
					classTN++;
				}

			} else {
				undeterminedRank1Count++;
				rank1UndeterminedScore[scoreId]++;
				for (int i = 0; i < composition.length; i++) {
					undeterminedGlycoCount[i] += composition[i];
				}
			}
		} else {
			if (determined) {
				determinedRank2Count++;
				rank2DeterminedScore[scoreId]++;
				for (int i = 0; i < composition.length; i++) {
					determinedGlycoCount[i] += composition[i];
				}
				for (int i = 0; i < scoreId; i++) {
					scoreFP[i]++;
				}
				for (int i = scoreId; i < dataCount; i++) {
					scoreTN[i]++;
				}
				if (classScore < 0.5) {
					classFP++;
				} else {
					classFN++;
				}
			} else {
				undeterminedRank2Count++;
				rank2UndeterminedScore[scoreId]++;
				for (int i = 0; i < composition.length; i++) {
					undeterminedGlycoCount[i] += composition[i];
				}
			}
		}
	}

	public int getDeterminedRank1Count() {
		return determinedRank1Count;
	}

	public int getDeterminedRank2Count() {
		return determinedRank2Count;
	}

	public int getUndeterminedRank1Count() {
		return undeterminedRank1Count;
	}

	public int getUndeterminedRank2Count() {
		return undeterminedRank2Count;
	}

	public int[] getRank1DeterminedScore() {
		return rank1DeterminedScore;
	}

	public int[] getRank1UndeterminedScore() {
		return rank1UndeterminedScore;
	}

	public int[] getDeterminedGlycoCount() {
		return determinedGlycoCount;
	}

	public int[] getUndeterminedGlycoCount() {
		return undeterminedGlycoCount;
	}

	public int[] getScoreTP() {
		return scoreTP;
	}

	public int[] getScoreTN() {
		return scoreTN;
	}

	public int[] getScoreFP() {
		return scoreFP;
	}

	public int[] getScoreFN() {
		return scoreFN;
	}

	public int getClassTP() {
		return classTP;
	}

	public int getClassTN() {
		return classTN;
	}

	public int getClassFP() {
		return classFP;
	}

	public int getClassFN() {
		return classFN;
	}

	public double getClassFDR() {
		double fdr = (classTP + classFP) == 0 ? 0 : (double) classFP / (double) (classTP + classFP);
		return fdr;
	}

	public double getClassSensitivity() {
		double fdr = (classTP + classFN) == 0 ? 0 : (double) classTP / (double) (classTP + classFN);
		return fdr;
	}

	public double getClassSpecificity() {
		double fdr = (classTN + classFP) == 0 ? 0 : (double) classTN / (double) (classTN + classFP);
		return fdr;
	}

	public int getQValue1() {
		return qValue1;
	}

	public int getQValue5() {
		return qValue5;
	}

	/*
	 * public double[] getSingleROC(){ double[] roc = new double[2]; roc[0] = 1.0 -
	 * (double) this.tnCount / (double) (this.fpCount + this.tnCount); roc[1] =
	 * (double) this.tpCount / (double) (this.tpCount + this.fnCount); return roc; }
	 */
	public double[][] getScoreROC() {
		double[][] scoreROC = new double[2][dataCount];
		for (int i = 0; i < dataCount; i++) {
			scoreROC[0][i] = 1.0 - (double) this.scoreTN[i] / (double) (this.scoreFP[i] + this.scoreTN[i]);
			scoreROC[1][i] = (double) this.scoreTP[i] / (double) (this.scoreTP[i] + this.scoreFN[i]);
		}
		return scoreROC;
	}

	public double[] getScoreFDR() {
		double[] fdr = new double[dataCount];
		for (int i = 0; i < dataCount; i++) {
			if (this.scoreTP[i] > 0) {
				fdr[i] = (double) this.scoreFP[i] / (double) this.scoreTP[i];
			} else {
				fdr[i] = 0;
			}
		}
		return fdr;
	}

	public void stat(NovoGlycoCompMatch[] matches) {
		for (NovoGlycoCompMatch match : matches) {
			int scan = match.getScannum();
			int rank = match.getRank();
			boolean determined = match.isDetermined();

			double score = match.getScore();
			int scoreId = (int) (score >= dataCount * 0.2 ? dataCount - 1 : score / 0.2);
			if (scoreId < 0)
				scoreId = 0;

			double classScore = match.getClassScore();
			double qvalue = match.getqValue();

			int[] composition = match.getComposition();
			if (rank == 1) {
				if (determined) {
					determinedRank1Count++;
					rank1DeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						determinedGlycoCount[i] += composition[i];
					}
					for (int i = 0; i < scoreId; i++) {
						scoreTP[i]++;
					}
					for (int i = scoreId; i < dataCount; i++) {
						scoreFN[i]++;
					}
					if (qvalue < 0.01) {
						qValue1++;
					}

					if (qvalue < 0.05) {
						qValue5++;
					}

					if (classScore < 0.5) {
						classTP++;
					} else {
						classTN++;
					}

				} else {
					undeterminedRank1Count++;
					rank1UndeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						undeterminedGlycoCount[i] += composition[i];
					}
				}
			} else {
				if (determined) {

					determinedRank2Count++;
					rank2DeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						determinedGlycoCount[i] += composition[i];
					}
					for (int i = 0; i < scoreId; i++) {
						scoreFP[i]++;
					}
					for (int i = scoreId; i < dataCount; i++) {
						scoreTN[i]++;
					}
					if (classScore < 0.5) {
						classFP++;
					} else {
						classFN++;
					}

				} else {
					undeterminedRank2Count++;
					rank2UndeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						undeterminedGlycoCount[i] += composition[i];
					}
				}
			}
		}
	}

	public void stat(NovoGlycoCompMatch[] matches, HashSet<Integer> usedRank2Scans) {
		for (NovoGlycoCompMatch match : matches) {
			int scan = match.getScannum();
			int rank = match.getRank();
			boolean determined = match.isDetermined();

			double score = match.getScore();
			int scoreId = (int) (score >= dataCount * 0.2 ? dataCount - 1 : score / 0.2);
			if (scoreId < 0)
				scoreId = 0;

			double classScore = match.getClassScore();
			double qvalue = match.getqValue();

			int[] composition = match.getComposition();
			if (rank == 1) {
				if (determined) {
					determinedRank1Count++;
					rank1DeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						determinedGlycoCount[i] += composition[i];
					}
					for (int i = 0; i < scoreId; i++) {
						scoreTP[i]++;
					}
					for (int i = scoreId; i < dataCount; i++) {
						scoreFN[i]++;
					}
					if (qvalue < 0.01) {
						qValue1++;
					}

					if (qvalue < 0.05) {
						qValue5++;
					}

					if (classScore < 0.5) {
						classTP++;
					} else {
						classTN++;
					}

				} else {
					undeterminedRank1Count++;
					rank1UndeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						undeterminedGlycoCount[i] += composition[i];
					}
				}
			} else {
				if (determined) {

					if (usedRank2Scans.contains(scan)) {

					} else {
						determinedRank2Count++;
						rank2DeterminedScore[scoreId]++;
						for (int i = 0; i < composition.length; i++) {
							determinedGlycoCount[i] += composition[i];
						}
						for (int i = 0; i < scoreId; i++) {
							scoreFP[i]++;
						}
						for (int i = scoreId; i < dataCount; i++) {
							scoreTN[i]++;
						}
						if (classScore < 0.5) {
							classFP++;
						} else {
							classFN++;
						}
					}

				} else {
					undeterminedRank2Count++;
					rank2UndeterminedScore[scoreId]++;
					for (int i = 0; i < composition.length; i++) {
						undeterminedGlycoCount[i] += composition[i];
					}
				}
			}
		}
	}

	public void tempExport(MicroMonosaccharides monosaccharides) {
	}
}
