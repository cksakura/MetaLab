package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.spectra.Peak;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class GlycoMatchClassifier {
	
	private static Attribute preChargeAtt = new Attribute("Precursor charge");
	private static Attribute scoreAtt = new Attribute("Score");
	private static Attribute factorAtt = new Attribute("Factor");
	private static Attribute peakCountAtt = new Attribute("Peak count");
	private static Attribute nodeCountAtt = new Attribute("Node count");
	private static Attribute nodeCountRatioAtt = new Attribute("Node count ratio");
	private static Attribute nodeIntensityRatioAtt = new Attribute("Node intensity ratio");
	private static Attribute missNodeCountAtt = new Attribute("Miss node count");
	
	private static Attribute fragmentChargeAtt = new Attribute("Fragment charge");
	private static Attribute deltaMzMaxAtt = new Attribute("Max delta mz");
	private static Attribute deltaAverageAtt = new Attribute("Average delta mz");
	private static Attribute deltaMedianAtt = new Attribute("Median delta mz");
	
	private static Attribute intensityMaxAtt = new Attribute("Max intensity");
	private static Attribute intensityMinAtt = new Attribute("Min intensity");
	private static Attribute intensityMedianAtt = new Attribute("Median intensity");
	private static Attribute intensityAverageAtt = new Attribute("Average intensity");
	private static Attribute intensityStdevAtt = new Attribute("Stdev intensity");
	
	private static Attribute oxoniumMaxAtt = new Attribute("Max oxonium");
	private static Attribute oxoniumMinAtt = new Attribute("Min oxonium");
	private static Attribute oxoniumCountAtt = new Attribute("Oxonium count");
	private static Attribute missOxoniumMaxAtt = new Attribute("Max miss oxonium");
	private static Attribute missOxoniumMinAtt = new Attribute("Min miss oxonium");
	private static Attribute missOxoniumCountAtt = new Attribute("Miss oxonium count");
	private static Attribute oxoniumCountRatio = new Attribute("Oxonium count ratio");
	private static Attribute oxoniumIntensityRatio = new Attribute("Oxonium intensity ratio");
	private static Attribute oxoniumScoreRatio = new Attribute("Oxonium score ratio");
	
	private static Attribute glycanCountAtt = new Attribute("Glycan count");
	private static Attribute classAtt;

	private ArrayList<Attribute> attList;

	private boolean useOxoniumInfo;
	private static int iterativeMax = 5;
	private int iterativeCount;
	
	public static final int leastClassifyCount = 200;
	
	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(GlycoMatchClassifier.class);

	public GlycoMatchClassifier(boolean useOxoniumInfo) {
		this.useOxoniumInfo = useOxoniumInfo;
		this.initialAttList();
	}
	
	private void initialAttList() {
		this.attList = new ArrayList<Attribute>();
		if (this.useOxoniumInfo) {
			attList.add(preChargeAtt);
			attList.add(scoreAtt);
			attList.add(factorAtt);

			attList.add(peakCountAtt);
			attList.add(nodeCountAtt);
			attList.add(nodeCountRatioAtt);
			attList.add(nodeIntensityRatioAtt);
			attList.add(missNodeCountAtt);

			attList.add(fragmentChargeAtt);
			attList.add(deltaMzMaxAtt);
			attList.add(deltaAverageAtt);
			attList.add(deltaMedianAtt);

			attList.add(intensityMaxAtt);
			attList.add(intensityMinAtt);
			attList.add(intensityMedianAtt);
			attList.add(intensityAverageAtt);
			attList.add(intensityStdevAtt);

			attList.add(oxoniumMaxAtt);
			attList.add(oxoniumMinAtt);
			attList.add(oxoniumCountAtt);
			attList.add(missOxoniumMaxAtt);
			attList.add(missOxoniumMinAtt);
			attList.add(missOxoniumCountAtt);
			attList.add(oxoniumCountRatio);
			attList.add(oxoniumIntensityRatio);
			attList.add(oxoniumScoreRatio);

			attList.add(glycanCountAtt);

			ArrayList<String> classList = new ArrayList<String>();
			classList.add("P");
			classList.add("N");

			classAtt = new Attribute("class", classList);
			attList.add(classAtt);
			
		} else {
			
			attList.add(preChargeAtt);
			attList.add(scoreAtt);
			attList.add(factorAtt);

			attList.add(peakCountAtt);
			attList.add(nodeCountAtt);
			attList.add(nodeCountRatioAtt);
			attList.add(nodeIntensityRatioAtt);
			attList.add(missNodeCountAtt);

			attList.add(fragmentChargeAtt);
			attList.add(deltaMzMaxAtt);
			attList.add(deltaAverageAtt);
			attList.add(deltaMedianAtt);

			attList.add(intensityMaxAtt);
			attList.add(intensityMinAtt);
			attList.add(intensityMedianAtt);
			attList.add(intensityAverageAtt);
			attList.add(intensityStdevAtt);

			attList.add(glycanCountAtt);

			ArrayList<String> classList = new ArrayList<String>();
			classList.add("P");
			classList.add("N");

			classAtt = new Attribute("class", classList);
			attList.add(classAtt);
		}
	}
	
	public Instance getMatchInstance(NovoGlycoCompMatch match) {

		Instance instance = new DenseInstance(attList.size());

		instance.setValue(scoreAtt, match.getScore());
		instance.setValue(factorAtt, match.getFactor());
		instance.setValue(preChargeAtt, match.getPreCharge());

		Peak[] peaks = match.getPeaks();
		HashMap<Integer, GlycoPeakNode> nodemap = match.getNodeMap();

		instance.setValue(peakCountAtt, peaks.length);
		instance.setValue(nodeCountAtt, nodemap.size());

		int fragmentCharge = -1;
		double totalIntensity = 0;
		double max = 0;
		double min = 1;
		double maxDeltaMz = 0;
		double[] scoreList = new double[nodemap.size()];
		double[] massDifference = new double[nodemap.size()];
		int scoreid = 0;

		for (Integer id : nodemap.keySet()) {
			GlycoPeakNode node = nodemap.get(id);
			totalIntensity += node.getPeakIntensity();
			fragmentCharge = node.getCharge();
			double nodeScore = node.getSingleScore();
			double nodeDeltaMz = node.getDeltaMz();
			if (nodeScore > max) {
				max = nodeScore;
			}
			if (nodeScore < min) {
				min = nodeScore;
			}
			if (nodeDeltaMz > maxDeltaMz) {
				maxDeltaMz = nodeDeltaMz;
			}
			scoreList[scoreid] = nodeScore;
			massDifference[scoreid] = node.getDeltaMz();
			scoreid++;
		}

		double totalPeakIntensity = 0;
		for (int i = 0; i < peaks.length; i++) {
			totalPeakIntensity += peaks[i].getIntensity();
		}

		instance.setValue(nodeCountRatioAtt, (double) nodemap.size() / (double) peaks.length);
		instance.setValue(nodeIntensityRatioAtt, totalIntensity / totalPeakIntensity);
		instance.setValue(missNodeCountAtt, match.getMissNodeCount());

		double medianDelta = MathTool.getMedian(massDifference);
		double aveDelta = MathTool.getAve(massDifference);

		instance.setValue(fragmentChargeAtt, fragmentCharge);
		instance.setValue(deltaMzMaxAtt, maxDeltaMz);
		instance.setValue(deltaAverageAtt, aveDelta);
		instance.setValue(deltaMedianAtt, medianDelta);

		double median = MathTool.getMedian(scoreList);
		double average = MathTool.getAve(scoreList);
		double stdev = MathTool.getStdDev(scoreList);

		instance.setValue(intensityMaxAtt, max);
		instance.setValue(intensityMinAtt, min);
		instance.setValue(intensityMedianAtt, median);
		instance.setValue(intensityAverageAtt, average);
		instance.setValue(intensityStdevAtt, stdev);

		if (this.useOxoniumInfo) {
			HashMap<Integer, GlycoPeakNode> oxoniumMap = match.getOxoniumMap();
			double maxOxonium = 0;
			double minOxonium = 1;
			double oxoIntensity = 0;
			double oxoScore = 0;
			for (Integer id : oxoniumMap.keySet()) {
				GlycoPeakNode node = oxoniumMap.get(id);
				double nodeScore = node.getSingleScore();
				if (nodeScore > maxOxonium) {
					maxOxonium = nodeScore;
				}
				if (nodeScore < minOxonium) {
					minOxonium = nodeScore;
				}
				oxoIntensity += node.getPeakIntensity();
				oxoScore += node.getSingleScore();
			}
			instance.setValue(oxoniumMaxAtt, maxOxonium);
			instance.setValue(oxoniumMinAtt, minOxonium);
			instance.setValue(oxoniumCountAtt, oxoniumMap.size());

			HashMap<Integer, GlycoPeakNode> missOxoniumMap = match.getMissOxoniumMap();
			double missMaxOxonium = 0;
			double missMinOxonium = 1;
			double missOxoIntensity = 0;
			double missOxoScore = 0;
			for (Integer id : missOxoniumMap.keySet()) {
				GlycoPeakNode node = missOxoniumMap.get(id);
				double nodeScore = node.getSingleScore();
				if (nodeScore > missMaxOxonium) {
					missMaxOxonium = nodeScore;
				}
				if (nodeScore < missMinOxonium) {
					missMinOxonium = nodeScore;
				}
				missOxoIntensity += node.getPeakIntensity();
				missOxoScore += node.getSingleScore();
			}
			instance.setValue(missOxoniumMaxAtt, missMaxOxonium);
			instance.setValue(missOxoniumMinAtt, missMinOxonium);
			instance.setValue(missOxoniumCountAtt, missOxoniumMap.size());

			instance.setValue(oxoniumCountRatio, (double) oxoniumMap.size() - (double) missOxoniumMap.size());
			instance.setValue(oxoniumIntensityRatio, oxoIntensity - missOxoIntensity);
			instance.setValue(oxoniumScoreRatio, oxoScore - missOxoScore);
		}

		int glycoCount = 0;
		int[] composition = match.getComposition();
		for (int i = 0; i < composition.length; i++) {
			glycoCount += composition[i];
		}
		instance.setValue(glycanCountAtt, glycoCount);

		return instance;
	}

	private String evaluation(Classifier classifier, Instances instances) {
		Evaluation eva = null;
		try {
			eva = new Evaluation(instances);
			eva.evaluateModel(classifier, instances);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in evaluting the instances", e);
		}

		return eva.toSummaryString();
	}

	public double[] classify(ArrayList<NovoGlycoCompMatch> matchlist, HashSet<Integer> usedRank2Scans) {

		this.iterativeCount = 1;
		Instances trainInstances = new Instances("Train", this.attList, matchlist.size());
		Instances instances = new Instances("All", this.attList, matchlist.size());
		
		double[] sortScores = new double[matchlist.size()];
		for (int i = 0; i < matchlist.size(); i++) {
			sortScores[i] = matchlist.get(i).getScore();
		}
		Arrays.sort(sortScores);
		int thresId1 = (int) (matchlist.size() * 0.9);
		double thresScore1 = sortScores[thresId1];
		
		int thresId2 = (int) (matchlist.size() * 0.5);
		double thresScore2 = sortScores[thresId2];
		
		int[] trainCount = new int[2];

		for (int i = 0; i < matchlist.size(); i++) {
			NovoGlycoCompMatch matchi = matchlist.get(i);
			Instance instance = this.getMatchInstance(matchi);

			if (matchi.getRank() == 1) {
				// if (matchi.getScore() > thresScore && matchi.getOxoniumFactor() > 0) {

				if (matchi.getScore() > thresScore1) {
					instance.setValue(classAtt, "P");
					trainInstances.add(instance);
					trainCount[0]++;
				}
			} else {
				if (usedRank2Scans.contains(matchi.getScannum())) {
					if (matchi.getScore() > thresScore1) {
						instance.setValue(classAtt, "P");
						trainInstances.add(instance);
						trainCount[0]++;
					}
				} else {
					instance.setValue(classAtt, "N");
					trainInstances.add(instance);
					trainCount[1]++;
				}
			}
			instances.add(instance);
		}
		instances.setClass(classAtt);
		trainInstances.setClass(classAtt);

		LOGGER.info("Total number of instances: " + instances.numInstances());
		LOGGER.info("train set size: " + trainInstances.numInstances()+"; Target: "+trainCount[0]+"; Decoy: "+trainCount[1]);

		Classifier classifier = new NaiveBayesUpdateable();
		
		try {
			classifier.buildClassifier(trainInstances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in building classifier", e);
		}

		double[] scores = new double[instances.numInstances()];
		for (int i = 0; i < scores.length; i++) {
			Instance instance = instances.instance(i);
			double[] distribution = null;
			try {
				distribution = classifier.distributionForInstance(instance);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in classifying instances", e);
			}
			scores[i] = distribution[1];
		}
		
		LOGGER.info("Classification finished");
		String evaluation = this.evaluation(classifier, instances);
		LOGGER.info(evaluation);

		/*
//		Iteration was not needed
		
		sortScores = new double[scores.length];
		System.arraycopy(scores, 0, sortScores, 0, scores.length);
		Arrays.sort(sortScores);

		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.8)];

		LOGGER.info("Iterative " + iterativeCount + " finished");
		String evaluation = this.evaluation(classifier, instances);
		LOGGER.info(evaluation);

		double[] finalScores = null;
		try {
			finalScores = iterate(instances, scores, scoreThres1, scoreThres2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in classifying PSMs", e);
		}
*/
		return scores;
	}
	
	private double[] iterate(Instances instances, double[] scores, double scoreThreshold1,
			double scoreThreshold2) {

		if (iterativeCount == iterativeMax) {
			return scores;
		}

		Instances trainInstances = new Instances("Train", this.attList, 0);

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (instance.stringValue(classAtt).equals("P")) {
				if (scores[i] <= scoreThreshold1) {
					trainInstances.add(instance);
				}
			} else if (instance.stringValue(classAtt).equals("N")) {
				if (scores[i] >= scoreThreshold2) {
					trainInstances.add(instance);
				}
			}
		}
		trainInstances.setClass(classAtt);

		Classifier classifier = new NaiveBayesUpdateable();
		try {
			classifier.buildClassifier(trainInstances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in building classifier", e);
		}

		double[] newscores = new double[instances.numInstances()];
		for (int i = 0; i < newscores.length; i++) {
			Instance instance = instances.instance(i);
			double[] distribution = null;
			try {
				distribution = classifier.distributionForInstance(instance);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in classifying PSMs", e);
			}
			newscores[i] = distribution[1];
		}

		double[] sortScores = new double[newscores.length];
		System.arraycopy(newscores, 0, sortScores, 0, newscores.length);
		Arrays.sort(sortScores);

		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.8)];

		iterativeCount++;

		LOGGER.info("Iterative " + iterativeCount + " finished");

		String evaluation = this.evaluation(classifier, instances);
		LOGGER.info(evaluation);

		return iterate(instances, newscores, scoreThres1, scoreThres2);
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//		GlycoMatchClassifier.test("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g6.txt");
//		GlycoMatchClassifier.test("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g7.txt");
//		GlycoMatchClassifier.test("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover.txt");
//		GlycoMatchClassifier.test("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_discover2.txt");
//		GlycoMatchClassifier.test2("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g6.txt");
//		GlycoMatchClassifier.loopTest("D:\\Data\\Rui\\microbiota\\Rui_20140530_microbiota_HILIC_step3_standard_g6.txt");
	}

}
