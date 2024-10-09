/**
 * 
 */
package bmi.med.uOttawa.metalab.core.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Kai Cheng
 *
 */
public class SemiSuperClassifier {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(SemiSuperClassifier.class);

	private int[] totalPepCount;
	private int[] totalProCount;
	
	/**
	 * no iteration
	 */
	private static int iterativeMax = 5;
	private static int partialCount = 5;
	private ArrayList<Attribute> attrList;

	private int iterativeCount;

	public SemiSuperClassifier(ArrayList<Attribute> attrList) {
		this.attrList = attrList;
	}

	public double[] classify(Instances instances, int scoreId, double scoreThreshold, int[] modIds, String[] peps,
			String[] pros, int[] totalPepCount, int[] totalProCount) {
		
		this.totalPepCount = totalPepCount;
		this.totalProCount = totalProCount;

		this.iterativeCount = 1;

		Instances[] trainInstances = new Instances[partialCount];
		for (int i = 0; i < partialCount; i++) {
			trainInstances[i] = new Instances("Train" + i, attrList, 0);
		}

		int partialId = 0;

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (instance.stringValue(instances.numAttributes() - 1).equals("P")) {
				if (instance.value(scoreId) > scoreThreshold) {
					trainInstances[partialId++].add(instance);
					if (partialId == partialCount) {
						partialId = 0;
					}
				}
			} else if (instance.stringValue(instances.numAttributes() - 1).equals("N")) {
				for (Instances iis : trainInstances) {
					iis.add(instance);
				}
			}
		}

		Classifier[] classifiers = new Classifier[partialCount];

		for (int i = 0; i < partialCount; i++) {
			trainInstances[i].setClassIndex(trainInstances[i].numAttributes() - 1);
			classifiers[i] = new NaiveBayesUpdateable();
			try {
				classifiers[i].buildClassifier(trainInstances[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in building classifier", e);
			}
			LOGGER.info("train set " + (i + 1) + " size: " + trainInstances[i].numInstances());
		}

		double[] scores = new double[instances.numInstances()];
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			double disScore = 0;
			for (int j = 0; j < partialCount; j++) {
				double[] distribution = null;
				try {
					distribution = classifiers[j].distributionForInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in classifying PTM", e);
				}
				disScore += distribution[1];
			}
			scores[i] = disScore / (double) partialCount;
		}
		
		for (int i = 0; i < partialCount; i++) {
			String evaluation = this.evaluation(classifiers[i], instances);
			LOGGER.info(evaluation);
		}

		double[] sortScores = new double[scores.length];
		System.arraycopy(scores, 0, sortScores, 0, scores.length);
		Arrays.sort(sortScores);
		
		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.9)];
		
		if (scoreThres1 < 0.05) {
			scoreThres1 = 0.05;
		} else if (scoreThres1 > 0.1) {
			scoreThres1 = 0.1;
		}

		scoreThres2 = 0.5;
		
		LOGGER.info("Iterative " + iterativeCount + " finished");
		
		double[] finalScores = null;
		try {
			finalScores = iterate(instances, scores, scoreThres1, scoreThres2, modIds, peps, pros);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in classifying PSMs", e);
		}

		return finalScores;
	}

	private double[] iterate(Instances instances, double[] scores, double scoreThreshold1, double scoreThreshold2,
			int[] modIds, String[] peps, String[] pros) {

		if (iterativeCount == iterativeMax) {
			return scores;
		}

		Instances[] trainInstances = new Instances[partialCount];
		for (int i = 0; i < partialCount; i++) {
			trainInstances[i] = new Instances("Train" + i, attrList, 0);
		}

		HashMap<Integer, Integer> modmap = new HashMap<Integer, Integer>();
		HashMap<String, Integer> pepmap = new HashMap<String, Integer>();
		HashMap<String, Integer> promap = new HashMap<String, Integer>();

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (instance.stringValue(instances.numAttributes() - 1).equals("P")) {
				if (scores[i] <= scoreThreshold1) {
					int partialId = (int) (Math.random() * 100) % partialCount;
					trainInstances[partialId].add(instance);
				}
			} else if (instance.stringValue(instances.numAttributes() - 1).equals("N")) {
				for (Instances iis : trainInstances) {
					iis.add(instance);
				}
			}

			if (scores[i] < scoreThreshold2) {
				if (modmap.containsKey(modIds[i])) {
					modmap.put(modIds[i], modmap.get(modIds[i]) + 1);
				} else {
					modmap.put(modIds[i], 1);
				}
			} else {
				if (pepmap.containsKey(peps[i])) {
					pepmap.put(peps[i], pepmap.get(peps[i]) + 1);
				} else {
					pepmap.put(peps[i], 1);
				}
				if (promap.containsKey(pros[i])) {
					promap.put(pros[i], promap.get(pros[i]) + 1);
				} else {
					promap.put(pros[i], 1);
				}
			}
		}

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (modmap.containsKey(modIds[i])) {
				instance.setValue(instances.numAttributes() - 4, Math.log(modmap.get(modIds[i])));
			} else {
				instance.setValue(instances.numAttributes() - 4, 0);
			}

			if (pepmap.containsKey(peps[i])) {
				int pepcount = this.totalPepCount[i] - pepmap.get(peps[i]);
				if (pepcount == 0) {
					instance.setValue(instances.numAttributes() - 3, 0);
				} else {
					instance.setValue(instances.numAttributes() - 3, Math.log((double) pepcount));
				}

			} else {
				instance.setValue(instances.numAttributes() - 3, this.totalPepCount[i]);
			}

			if (promap.containsKey(pros[i])) {
				int procount = this.totalProCount[i] - promap.get(pros[i]);
				if (procount == 0) {
					instance.setValue(instances.numAttributes() - 2, 0);
				} else {
					instance.setValue(instances.numAttributes() - 2, Math.log((double) procount));
				}

			} else {
				instance.setValue(instances.numAttributes() - 2, this.totalProCount[i]);
			}
		}

		Classifier[] classifiers = new Classifier[partialCount];

		for (int i = 0; i < partialCount; i++) {
			trainInstances[i].setClassIndex(trainInstances[i].numAttributes() - 1);
			classifiers[i] = new NaiveBayesUpdateable();
			try {
				classifiers[i].buildClassifier(trainInstances[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in building classfier", e);
			}
			LOGGER.info("train set size: " + trainInstances[i].numInstances());
		}

		double[] newscores = new double[instances.numInstances()];
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			double disScore = 0;
			for (int j = 0; j < partialCount; j++) {
				double[] distribution = null;
				try {
					distribution = classifiers[j].distributionForInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in classifying PSMs", e);
				}
				disScore += distribution[1];
			}
			newscores[i] = disScore / (double) partialCount;
		}
		
		for (int i = 0; i < partialCount; i++) {
			String evaluation = this.evaluation(classifiers[i], instances);
			LOGGER.info(evaluation);
		}
		
		double[] sortScores = new double[newscores.length];
		System.arraycopy(newscores, 0, sortScores, 0, newscores.length);
		Arrays.sort(sortScores);
		
		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.9)];
		
		if (scoreThres1 < 0.05) {
			scoreThres1 = 0.05;
		} else if (scoreThres1 > 0.1) {
			scoreThres1 = 0.1;
		}

		scoreThres2 = 0.5;
		
		iterativeCount++;

		LOGGER.info("Iterative " + iterativeCount + " finished");

		return iterate(instances, newscores, scoreThres1, scoreThres2, modIds, peps, pros);
	}
	
	public double[] classify(Instances instances, int scoreId, double scoreThreshold) {

		this.iterativeCount = 1;

		Instances[] trainInstances = new Instances[partialCount];
		for (int i = 0; i < partialCount; i++) {
			trainInstances[i] = new Instances("Train" + i, attrList, 0);
		}

		int partialId = 0;

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (instance.stringValue(instances.numAttributes() - 1).equals("P")) {
				if (instance.value(scoreId) > scoreThreshold) {
					trainInstances[partialId++].add(instance);
					if (partialId == partialCount) {
						partialId = 0;
					}
				}
			} else if (instance.stringValue(instances.numAttributes() - 1).equals("N")) {
				for (Instances iis : trainInstances) {
					iis.add(instance);
				}
			}
		}

		Classifier[] classifiers = new Classifier[partialCount];

		for (int i = 0; i < partialCount; i++) {
			trainInstances[i].setClassIndex(trainInstances[i].numAttributes() - 1);
			classifiers[i] = new NaiveBayesUpdateable();
			try {
				classifiers[i].buildClassifier(trainInstances[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in building classifier", e);
			}
			LOGGER.info("train set " + (i + 1) + " size: " + trainInstances[i].numInstances());
		}

		double[] scores = new double[instances.numInstances()];
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			double disScore = 0;
			for (int j = 0; j < partialCount; j++) {
				double[] distribution = null;
				try {
					distribution = classifiers[j].distributionForInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in classifying PTM", e);
				}
				disScore += distribution[1];
			}
			scores[i] = disScore / (double) partialCount;
		}

		for (int i = 0; i < partialCount; i++) {
			String evaluation = this.evaluation(classifiers[i], instances);
			LOGGER.info(evaluation);
		}

		double[] sortScores = new double[scores.length];
		System.arraycopy(scores, 0, sortScores, 0, scores.length);
		Arrays.sort(sortScores);

		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.9)];

		if (scoreThres1 < 0.05) {
			scoreThres1 = 0.05;
		} else if (scoreThres1 > 0.1) {
			scoreThres1 = 0.1;
		}

		scoreThres2 = 0.5;

		LOGGER.info("Iterative " + iterativeCount + " finished");

		double[] finalScores = null;
		try {
			finalScores = iterate(instances, scores, scoreThres1, scoreThres2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in classifying PSMs", e);
		}

		return finalScores;
	}

	private double[] iterate(Instances instances, double[] scores, double scoreThreshold1, double scoreThreshold2) {

		if (iterativeCount == iterativeMax) {
			return scores;
		}

		Instances[] trainInstances = new Instances[partialCount];
		for (int i = 0; i < partialCount; i++) {
			trainInstances[i] = new Instances("Train" + i, attrList, 0);
		}

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			if (instance.stringValue(instances.numAttributes() - 1).equals("P")) {
				if (scores[i] <= scoreThreshold1) {
					int partialId = (int) (Math.random() * 100) % partialCount;
					trainInstances[partialId].add(instance);
				}
			} else if (instance.stringValue(instances.numAttributes() - 1).equals("N")) {
				for (Instances iis : trainInstances) {
					iis.add(instance);
				}
			}
		}

		Classifier[] classifiers = new Classifier[partialCount];

		for (int i = 0; i < partialCount; i++) {
			trainInstances[i].setClassIndex(trainInstances[i].numAttributes() - 1);
			classifiers[i] = new NaiveBayesUpdateable();
			try {
				classifiers[i].buildClassifier(trainInstances[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in building classfier", e);
			}
			LOGGER.info("train set size: " + trainInstances[i].numInstances());
		}

		double[] newscores = new double[instances.numInstances()];
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			double disScore = 0;
			for (int j = 0; j < partialCount; j++) {
				double[] distribution = null;
				try {
					distribution = classifiers[j].distributionForInstance(instance);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in classifying PSMs", e);
				}
				disScore += distribution[1];
			}
			newscores[i] = disScore / (double) partialCount;
		}

		for (int i = 0; i < partialCount; i++) {
			String evaluation = this.evaluation(classifiers[i], instances);
			LOGGER.info(evaluation);
		}

		double[] sortScores = new double[newscores.length];
		System.arraycopy(newscores, 0, sortScores, 0, newscores.length);
		Arrays.sort(sortScores);

		double scoreThres1 = sortScores[(int) (sortScores.length * 0.1)];
		double scoreThres2 = sortScores[(int) (sortScores.length * 0.9)];

		if (scoreThres1 < 0.05) {
			scoreThres1 = 0.05;
		} else if (scoreThres1 > 0.1) {
			scoreThres1 = 0.1;
		}

		scoreThres2 = 0.5;

		iterativeCount++;

		LOGGER.info("Iterative " + iterativeCount + " finished");

		return iterate(instances, newscores, scoreThres1, scoreThres2);
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
	
}
