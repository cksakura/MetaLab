/**
 * 
 */
package bmi.med.uOttawa.metalab.core.ml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Kai Cheng
 *
 */
public class UnsuperCluster {

	private int clusterCount = 10;
	private int[] clusterCounts = new int[] { 5, 8, 10 };
	private double fdrThres = 0.01;
	private ArrayList<Attribute> attrList;

	private int iterativeMax = 5;
	private int iterativeCount = 0;
	private double[] scores;

	public UnsuperCluster(ArrayList<Attribute> attrList) {
		this.attrList = attrList;
	}

	public void cluster(Instances instances, ArrayList<Boolean> blist) throws Exception {
		
		System.out.println("Iterative count: " + iterativeCount);
		
//		MakeDensityBasedClusterer cluster = new MakeDensityBasedClusterer();
		HierarchicalClusterer cluster = new HierarchicalClusterer();
//		SingleClustererEnhancer Enhancer = new SingleClustererEnhancer();
		cluster.setNumClusters(clusterCount);
		cluster.buildClusterer(instances);

		System.out.println("number of instance: "+instances.numInstances());
		
		double[][] tdDistribution = new double[clusterCount][2];
		double[] totalCount = new double[2];
		double[][] totalDistribution = new double[instances.numInstances()][];

		int[] maxDistribute = new int[instances.numInstances()];
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			totalDistribution[i] = cluster.distributionForInstance(instance);
			if (blist.get(i)) {
				for (int j = 0; j < clusterCount; j++) {
					tdDistribution[j][0] += totalDistribution[i][j];
					totalCount[0] += totalDistribution[i][j];
				}
			} else {
				for (int j = 0; j < clusterCount; j++) {
					tdDistribution[j][1] += totalDistribution[i][j];
					totalCount[1] += totalDistribution[i][j];
				}
			}

			double maxj = 0;
			for (int j = 0; j < totalDistribution[i].length; j++) {
				if (totalDistribution[i][j] > maxj) {
					maxj = totalDistribution[i][j];
					maxDistribute[i] = j;
				}
			}
		}
		
		double[] TvsD = new double[clusterCount];
		for (int i = 0; i < TvsD.length; i++) {
			if (tdDistribution[i][0] == 0) {
				TvsD[i] = 0;
			} else {
				TvsD[i] = 1.0 - tdDistribution[i][1] / tdDistribution[i][0];
			}
		}
		
		this.scores = new double[totalDistribution.length];
		for (int i = 0; i < totalDistribution.length; i++) {
			double d = 0;
			for (int j = 0; j < totalDistribution[i].length; j++) {
				d += totalDistribution[i][j] * TvsD[j];
			}
			scores[i] = d;
		}
/*
//		ArrayList<Integer> list = new ArrayList<Integer>();
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < clusterCount; i++) {
			double fdr = tdDistribution[i][1] / tdDistribution[i][0];
			if (fdr > fdrThres) {
				set.add(i);
			}
			System.out.println(tdDistribution[i][0] + "\t" + tdDistribution[i][1] + "\t"
					+ tdDistribution[i][1] / tdDistribution[i][0]);
		}
//		System.out.println(totalCount[0] + "\t" + totalCount[1]);
*/ 
		
		iterativeCount++;
//		iterate(instances, blist, maxDistribute, set);
				
	}
	
	public void cluster2(Instances instances, ArrayList<Boolean> blist) throws Exception {

		this.scores = new double[instances.numInstances()];

		MakeDensityBasedClusterer cluster = new MakeDensityBasedClusterer();

		for (int ci = 0; ci < clusterCounts.length; ci++) {

			cluster.setNumClusters(clusterCounts[ci]);
			cluster.buildClusterer(instances);

			System.out.println("cluster count: " + clusterCounts[ci]);

			double[][] tdDistribution = new double[clusterCounts[ci]][2];
			double[] totalCount = new double[2];
			double[][] totalDistribution = new double[instances.numInstances()][];

			int[] maxDistribute = new int[instances.numInstances()];
			for (int i = 0; i < instances.numInstances(); i++) {
				Instance instance = instances.instance(i);
				totalDistribution[i] = cluster.distributionForInstance(instance);
				if (blist.get(i)) {
					for (int j = 0; j < clusterCounts[ci]; j++) {
						tdDistribution[j][0] += totalDistribution[i][j];
						totalCount[0] += totalDistribution[i][j];
					}
				} else {
					for (int j = 0; j < clusterCounts[ci]; j++) {
						tdDistribution[j][1] += totalDistribution[i][j];
						totalCount[1] += totalDistribution[i][j];
					}
				}

				double maxj = 0;
				for (int j = 0; j < totalDistribution[i].length; j++) {
					if (totalDistribution[i][j] > maxj) {
						maxj = totalDistribution[i][j];
						maxDistribute[i] = j;
					}
				}
			}

			double[] TvsD = new double[clusterCounts[ci]];
			for (int i = 0; i < TvsD.length; i++) {
				if (tdDistribution[i][0] == 0) {
					TvsD[i] = 0;
				} else {
					TvsD[i] = 1.0 - tdDistribution[i][1] / tdDistribution[i][0];
				}
			}

			for (int i = 0; i < totalDistribution.length; i++) {

				double d = 0;

				if (TvsD[maxDistribute[i]] > 0.6) {
					for (int j = 0; j < totalDistribution[i].length; j++) {
						d += totalDistribution[i][j] * TvsD[j];
					}
				}

				scores[i] += d;
			}
		}
	}
	
	public double[] getScores() {
		return this.scores;
	}
	
	private void iterate(Instances instances, ArrayList<Boolean> blist, int[] maxDistribute,
			HashSet<Integer> set) throws Exception {

		System.out.println(set);
		System.out.println("Iterative count: " + iterativeCount);
		
		Instances iterativeInstances = new Instances("PSM Instances", attrList, 10000);

		for (int i = 0; i < instances.numInstances(); i++) {
			if (set.contains(maxDistribute[i])) {
				iterativeInstances.add(instances.instance(i));
			}
		}

		MakeDensityBasedClusterer cluster = new MakeDensityBasedClusterer();
		cluster.setNumClusters(clusterCount);
		cluster.buildClusterer(iterativeInstances);

		System.out.println("number of instance: " + iterativeInstances.numInstances());

		double[][] tdDistribution = new double[clusterCount][2];
		double[] totalCount = new double[2];

		int[] newMaxDistribute = new int[instances.numInstances()];
		Arrays.fill(newMaxDistribute, -1);

		for (int i = 0; i < instances.numInstances(); i++) {
			if (set.contains(maxDistribute[i])) {
				Instance instance = instances.instance(i);
				double[] distribution = cluster.distributionForInstance(instance);
				if (blist.get(i)) {
					for (int j = 0; j < clusterCount; j++) {
						tdDistribution[j][0] += distribution[j];
						totalCount[0] += distribution[j];
					}
				} else {
					for (int j = 0; j < clusterCount; j++) {
						tdDistribution[j][1] += distribution[j];
						totalCount[1] += distribution[j];
					}
				}

				double maxj = 0;
				for (int j = 0; j < distribution.length; j++) {
					if (distribution[j] > maxj) {
						maxj = distribution[j];
						newMaxDistribute[i] = j;
					}
				}
			}
		}
		iterativeCount++;

		HashSet<Integer> newset = new HashSet<Integer>();
		for (int i = 0; i < clusterCount; i++) {
			double fdr = tdDistribution[i][1] / tdDistribution[i][0];
			if (fdr > fdrThres) {
				newset.add(i);
			}
			System.out.println(tdDistribution[i][0] + "\t" + tdDistribution[i][1] + "\t"
					+ tdDistribution[i][1] / tdDistribution[i][0]);
		}

		if (newset.size() == 0 || iterativeCount == iterativeMax) {
			return;
		}

		iterate(instances, blist, newMaxDistribute, newset);
	}

	private static void test(String feature) throws Exception {

		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		attributeList.add(new Attribute("charge"));
		attributeList.add(new Attribute("pepMass"));
		attributeList.add(new Attribute("pepLen"));

		attributeList.add(new Attribute("premr"));
		attributeList.add(new Attribute("massDiff"));
		attributeList.add(new Attribute("massDiffPPM"));

		attributeList.add(new Attribute("miss"));
		attributeList.add(new Attribute("num_tol_term"));
		attributeList.add(new Attribute("tot_num_ions"));
		attributeList.add(new Attribute("num_matched_ions"));

		attributeList.add(new Attribute("hyperscore"));
		attributeList.add(new Attribute("nextscore"));
		attributeList.add(new Attribute("deltascore"));
		attributeList.add(new Attribute("log10expect"));

		attributeList.add(new Attribute("bintensity"));
		attributeList.add(new Attribute("yintensity"));
		attributeList.add(new Attribute("brankscore"));
		attributeList.add(new Attribute("yrankscore"));
		attributeList.add(new Attribute("bcount"));
		attributeList.add(new Attribute("ycount"));
		attributeList.add(new Attribute("fragDeltaMass"));

		attributeList.add(new Attribute("modMass"));
		attributeList.add(new Attribute("bmodIntensity"));
		attributeList.add(new Attribute("ymodIntensity"));
		attributeList.add(new Attribute("bmodrankscore"));
		attributeList.add(new Attribute("ymodrankscore"));
		attributeList.add(new Attribute("fragModDeltaMass"));

		Instances instances = new Instances("PSM Instances", attributeList, 10000);

		ArrayList<Boolean> blist = new ArrayList<Boolean>();
		BufferedReader reader = new BufferedReader(new FileReader(feature));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			Instance inst = new DenseInstance(cs.length - 1);
			for (int i = 0; i < cs.length - 1; i++) {
				double value = Double.parseDouble(cs[i]);
				inst.setValue(attributeList.get(i), value);
			}
			if (Boolean.parseBoolean(cs[cs.length - 1])) {
				blist.add(true);
			} else {
				blist.add(false);
			}
			instances.add(inst);
		}
		reader.close();

		StringBuilder sb = new StringBuilder();
		UnsuperCluster cluster = new UnsuperCluster(attributeList);
		cluster.cluster(instances, blist);
	}
}
