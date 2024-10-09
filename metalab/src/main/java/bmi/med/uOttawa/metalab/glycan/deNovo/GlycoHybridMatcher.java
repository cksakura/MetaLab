package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.glycoCT.GlycoTree;
import bmi.med.uOttawa.metalab.glycan.glycoCT.GlycoTreeNode;
import bmi.med.uOttawa.metalab.glycan.glycoCT.MassUnit;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

public class GlycoHybridMatcher {

	private static String glycoDir = "/resources/N_GlycoCT.ID.txt";
	private static String massesDir = "/resources/N_Mass.Info.txt";

	/**
	 * 
	 */
	private final int size = 1643;

	private double[] monoMasses;
	private MassUnit[] massUnits;
	private GlycoTree[][] glycoUnits;

	private double precursorTolerance = 20;
	private double fragTolerance = 1.0;
	private double probility = 5 * fragTolerance * 2 / 100.0;
	
	private static String[] glycanNames = new String[] { Glycosyl.Fuc.getTitle(), Glycosyl.Hex.getTitle(),
			Glycosyl.HexNAc.getTitle(), Glycosyl.NeuAc.getTitle() };
	private static double[] glycanMasses = new double[] { Glycosyl.Fuc.getMonoMass(), Glycosyl.Hex.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass(), Glycosyl.NeuAc.getMonoMass() };
	private static double[] nCore = new double[] { Glycosyl.HexNAc.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass() * 2, Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass() * 2,
			Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass() * 3 };
	
	public GlycoHybridMatcher() throws IOException {

		this.initialMasses();
		this.initialGlyco();
	}

	private void initialMasses() throws IOException {

		File file = new File(System.getProperty("user.dir") + massesDir);
		BufferedReader masRreader = new BufferedReader(new FileReader(file));

		this.monoMasses = new double[size];
		this.massUnits = new MassUnit[size];
		this.glycoUnits = new GlycoTree[size][];

		double[][] peaks = null;
		int id = 0;
		double mono = 0;
		double avg = 0;
		int[] composition = null;
		int num = 0;
		int idx = 0;

		String line = null;
		while ((line = masRreader.readLine()) != null) {

			if (line.startsWith("ID")) {

				if (mono != 0) {
					MassUnit unit = new MassUnit(id, mono, avg, peaks);
					monoMasses[id - 1] = mono;
					massUnits[id - 1] = unit;
					glycoUnits[id - 1] = new GlycoTree[peaks.length];
				}

				String[] ss = line.split("\t");

				id = Integer.parseInt(ss[1]);
				mono = Double.parseDouble(ss[2]);
				avg = Double.parseDouble(ss[3]);
				num = Integer.parseInt(ss[4]);
				peaks = new double[num][];
				composition = new int[ss.length - 5];
				for (int i = 0; i < composition.length; i++) {
					composition[i] = Integer.parseInt(ss[i + 5]);
				}
				idx = 0;

			} else {

				String[] ss = line.split("\t");
				peaks[idx] = new double[ss.length];
				for (int i = 0; i < ss.length; i++) {
					peaks[idx][i] = Double.parseDouble(ss[i]);
				}
				idx++;
			}
		}

		MassUnit unit = new MassUnit(id, mono, avg, peaks);
		monoMasses[id - 1] = mono;
		massUnits[id - 1] = unit;
		glycoUnits[id - 1] = new GlycoTree[peaks.length];

		masRreader.close();
	}

	private void initialGlyco() throws IOException {

		File file = new File(System.getProperty("user.dir") + glycoDir);
		BufferedReader glycoReader = new BufferedReader(new FileReader(file));

		boolean res = false;
		boolean lin = false;

		GlycoTree gtree = null;
		StringBuilder treeBuilder = new StringBuilder();

		String name = "";

		String line = null;

		while ((line = glycoReader.readLine()) != null) {

			if (line.startsWith("ID")) {

				String[] ss = line.split("\t");
				int massid = Integer.parseInt(ss[1]);
				int fragid = Integer.parseInt(ss[2]);

				MassUnit mu = this.massUnits[massid - 1];
				double[] fragments = mu.getFragments()[fragid - 1];

				gtree.setMonoMass(mu.getMono());
				gtree.setAveMass(mu.getAverage());
				gtree.setIupacName(name);
				gtree.setFragments(fragments);
				gtree.setGlycoCT(treeBuilder.toString());
				gtree.parseInfo();

				if (this.glycoUnits[massid - 1][fragid - 1] == null)
					this.glycoUnits[massid - 1][fragid - 1] = gtree;

			} else if (line.startsWith("IUPAC")) {

				name = line.split("\t")[1];

			} else if (line.startsWith("RES")) {

				gtree = new GlycoTree();
				treeBuilder = new StringBuilder();
				res = true;

				treeBuilder.append(line).append("\n");

			} else if (line.startsWith("LIN")) {

				lin = true;
				res = false;

				treeBuilder.append(line).append("\n");

			} else {

				treeBuilder.append(line).append("\n");

				if (res) {

					int beg = line.indexOf(":");

					String id = line.substring(0, beg - 1);
					String typejudeg = line.substring(0, beg);
					String content = line.substring(beg + 1);

					if (typejudeg.endsWith("b")) {

						GlycoTreeNode node = new GlycoTreeNode(id, content);
						gtree.addNode(id, node);

					} else if (typejudeg.endsWith("s")) {

						gtree.addSub(id, content);

					} else {

						glycoReader.close();
						return;
					}

				} else if (lin) {

					String[] ss = line.split("[:()+]");
					String parentid = ss[1].substring(0, ss[1].length() - 1);
					String childid = ss[4].substring(0, ss[4].length() - 1);
					char parentLinkType = ss[1].charAt(ss[1].length() - 1);
					char childLinkType = ss[4].charAt(ss[3].length() - 1);
					String linkPosition1 = ss[2];
					String linkPosition2 = ss[3];

					gtree.addLink(parentid, childid, parentLinkType, childLinkType, linkPosition1, linkPosition2);
				}
			}
		}

		glycoReader.close();
	}
	
	public ArrayList<DenovoGlycoTree> getGlycoTreeList(Spectrum spectrum, int preCharge) {

		Peak[] peaks = spectrum.getPeaks();
		double upLimit = spectrum.getPrecursorMz() * preCharge;

		ArrayList<DenovoGlycoTree> treeList = new ArrayList<DenovoGlycoTree>();
		
		GlycoPeakNode[] pns = new GlycoPeakNode[peaks.length];
		for (int i = 0; i < pns.length; i++) {
			pns[i] = new GlycoPeakNode(peaks[i]);
		}
		
		for (int charge = preCharge; charge >= 1; charge--) {

			for (int i = 0; i < pns.length; i++) {
				if (pns[i].getCharge() > 0)
					continue;

				for (int j = i + 1; j < pns.length; j++) {
					if (pns[j].getCharge() > 0)
						continue;

					double delta = peaks[j].getMz() - peaks[i].getMz();

					if (delta - glycanMasses[glycanMasses.length - 1] / (double) charge > fragTolerance) {
						break;
					}

					if (peaks[j].getMz() * charge > upLimit - glycanMasses[0])
						break;

					for (int k = 0; k < glycanMasses.length; k++) {
						double deltaMz = Math.abs(delta - glycanMasses[k] / (double) charge);
						if (deltaMz < fragTolerance) {
							if (pns[j].getParent() == null) {
								if (pns[i].getChildren()[k] == null) {
									pns[j].setParent(pns[i]);
									pns[i].addChild(pns[j], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setSubComposition(pns[i].getSubComposition().clone());
									pns[j].addGlycan(k);
									
									pns[i].setCharge(charge);
									pns[j].setCharge(charge);
									
//System.out.println("a\t" + charge + "\t" + pns[i].getPeakMz() + "\t" + pns[j].getPeakMz());
								} else {
									if (deltaMz < pns[i].getChildren()[k].getDeltaMz()) {
										pns[j].setParent(pns[i]);
										pns[i].addChild(pns[j], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setSubComposition(pns[i].getSubComposition().clone());
										pns[j].addGlycan(k);
										
										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									}
									// System.out.println("b\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());
								}
							}
						}
					}
				}
			}
			for (int i = 0; i < pns.length; i++) {
				if (pns[i].isRoot()) {
					DenovoGlycoTree tree = new DenovoGlycoTree();
					add(tree, pns[i]);
					if (tree.getSize() >= 3) {
						treeList.add(tree);
					}
				}
			}
		}

		return treeList;
	}
	
	public GlycoPeakNode[] getGlycoNodeList(Spectrum spectrum, int preCharge) {

		Peak[] peaks = spectrum.getPeaks();
		double upLimit = spectrum.getPrecursorMz() * preCharge;

		GlycoPeakNode[] pns = new GlycoPeakNode[peaks.length];
		for (int i = 0; i < pns.length; i++) {
			pns[i] = new GlycoPeakNode(peaks[i]);
		}

		HashMap<Double, GlycoPeakNode> totalMap = new HashMap<Double, GlycoPeakNode>();
		
		for (int charge = preCharge; charge >= 1; charge--) {

			for (int i = 0; i < pns.length; i++) {
				if (totalMap.containsKey(pns[i].getPeakMz()))
					continue;
				for (int j = i + 1; j < pns.length; j++) {
					if (totalMap.containsKey(pns[j].getPeakMz()))
						continue;

					if(pns[j].getPeakIntensity()>pns[i].getPeakIntensity()*2.5)
						continue;
//System.out.println("i\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz()+"\t"+peaks[j].getMz() * charge+"\t"+ (upLimit - glycanMasses[0]));					
					double delta = peaks[j].getMz() - peaks[i].getMz();
					if (delta - glycanMasses[glycanMasses.length - 1] / (double) charge > fragTolerance) {
						break;
					}


					if ((peaks[j].getMz() - fragTolerance) * charge > upLimit - glycanMasses[0])
						break;

					for (int k = 0; k < glycanMasses.length; k++) {
						double deltaMz = Math.abs(delta - glycanMasses[k] / (double) charge);
						
//System.out.println("match\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz()+"\t"+glycanMasses[k] / (double) charge+"\t"+deltaMz);
					
						if (deltaMz < fragTolerance) {
							if (pns[j].getParent() == null) {
								if (pns[i].getChildren()[k] == null) {
									pns[j].setParent(pns[i]);
									pns[i].addChild(pns[j], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setSubComposition(pns[i].getSubComposition().clone());
									pns[j].addGlycan(k);

									pns[i].setCharge(charge);
									pns[j].setCharge(charge);
//System.out.println("a\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());
								} else {
									if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
										pns[j].setParent(pns[i]);
										pns[i].addChild(pns[j], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setSubComposition(pns[i].getSubComposition().clone());
										pns[j].addGlycan(k);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
//System.out.println("b\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());
									}
								}
							} else {
								if (pns[i].getPeakIntensity() > pns[j].getParent().getPeakIntensity()) {
									GlycoPeakNode[] childrenj = pns[j].getParent().getChildren();
									for (int l = 0; l < childrenj.length; l++) {
										if (childrenj[l] == pns[j]) {
											childrenj[l] = null;
										}
									}
									if (pns[i].getChildren()[k] == null) {
										pns[j].setParent(pns[i]);
										pns[i].addChild(pns[j], k);
										pns[j].setDeltaMz(deltaMz);
										pns[j].setSubComposition(pns[i].getSubComposition().clone());
										pns[j].addGlycan(k);

										pns[i].setCharge(charge);
										pns[j].setCharge(charge);
									} else {
										if (pns[j].getPeakIntensity() > pns[i].getChildren()[k].getPeakIntensity()) {
											pns[j].setParent(pns[i]);
											pns[i].addChild(pns[j], k);
											pns[j].setDeltaMz(deltaMz);
											pns[j].setSubComposition(pns[i].getSubComposition().clone());
											pns[j].addGlycan(k);

											pns[i].setCharge(charge);
											pns[j].setCharge(charge);
										}
									}
//System.out.println("c\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());
								}
							}
						}
					}
				}
			}
			
			for (int i = 0; i < pns.length; i++) {
				if(totalMap.containsKey(pns[i].getPeakMz()))
					continue;
				
				if (pns[i].isRoot()) {
					DenovoGlycoTree tree = new DenovoGlycoTree();
					add(tree, pns[i]);
					if (tree.getSize() >= 3) {
						HashMap<Double, GlycoPeakNode> map = tree.getMap();
						totalMap.putAll(map);
//System.out.println("map\t"+map.keySet()+"\t"+tree.getSize()+"\t"+totalMap.size()+"\t"+tree.getAveIntensity());
					}
				}
			}
		}

//		GlycoPeakNode[] nodes = nodeList.toArray(new GlycoPeakNode[nodeList.size()]);
		GlycoPeakNode[] nodes = totalMap.values().toArray(new GlycoPeakNode[totalMap.size()]);
//		for(int i=0;i<nodes.length;i++){
//			System.out.println("node\t"+i+"\t"+nodes[i].getPeakMz()+"\t"+nodes[i].getCharge()+"\t"+nodes[i].getMw());
//		}
		Arrays.parallelSort(nodes, new GlycoPeakNode.MwComparator());

		return nodes;
	}
	
	private void add(DenovoGlycoTree tree, GlycoPeakNode root){
		tree.addNode(root.getPeakMz(), root);
//System.out.println("tree\t"+Arrays.toString(tree.getGlycanCount())+"\t"+tree.getMap().keySet()+"\t"+root.getPeakMz()+"\t"+
//		Arrays.toString(root.getSubComposition())+"\t"+root.getCharge());		
		GlycoPeakNode[] nodes = root.getChildren();
		for(int i=0;i<nodes.length;i++){
			if(nodes[i]!=null){
				add(tree, nodes[i]);
			}
		}
	}
	
	public GlycoCompositionMatch[] match(Spectrum spectrum, double preMz, int preCharge, double backboneMass) {

		GlycoPeakNode[] nodes = getGlycoNodeList(spectrum, preCharge);
		double[] treeMws = new double[nodes.length];
		for (int i = 0; i < treeMws.length; i++) {
			treeMws[i] = nodes[i].getMw();
//System.out.println("nodes\t"+nodes[i].getPeakMz()+"\t"+nodes[i].getMw());			
		}
//System.out.println("nodes\t"+nodes.length);
		int[] find = new int[nCore.length];
		for (int i = 0; i < nCore.length; i++) {
			double gp = backboneMass + nCore[i];
			double begin = gp - fragTolerance * 10;
			int id = Arrays.binarySearch(treeMws, begin);
//System.out.println(backboneMass+"\t"+gp);
			if (id < 0) {
				id = -id - 1;
			}

			for (int k = id; k < treeMws.length; k++) {
//System.out.println(nodes[k].getPeakMz()+"\t"+treeMws[k]+"\t"+((treeMws[k] - gp) / (double) nodes[k].getCharge()));
				if (Math.abs((treeMws[k] - gp) / (double) nodes[k].getCharge()) < fragTolerance) {
//System.out.println(i+"\t"+treeMws[k]+"\t"+gp+"\t"+nodes[k].getPeakMz());					
					find[i] = 1;
				}

				if ((treeMws[k] - gp) / (double) nodes[k].getCharge() > fragTolerance) {
					break;
				}
			}
		}
//System.out.println("418\t"+Arrays.toString(find)+"\t"+nodes.length);

		if (find[0] == 0 || MathTool.getTotal(find) <= 3)
			return null;
		
		double glycoMass = (preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass;
		double tolerance = glycoMass * precursorTolerance * 1E-6 * preCharge;
		double beg = glycoMass - tolerance * 10;
		int id = Arrays.binarySearch(monoMasses, beg);

		if (id < 0) {
			id = -id - 1;
		}

		ArrayList<GlycoCompositionMatch> matchList = new ArrayList<GlycoCompositionMatch>();
		for (int j = id; j < monoMasses.length; j++) {
//System.out.println(monoMasses[j]+"\t"+glycoMass+"\t"+tolerance+"\t"+(monoMasses[j] - glycoMass));
			if (Math.abs(monoMasses[j] - glycoMass) < tolerance) {
				GlycoCompositionMatch gcmatch = this.match(massUnits[j], treeMws, nodes, backboneMass, monoMasses[j],
						(spectrum.getPrecursorMz() - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - monoMasses[j]);
				if(gcmatch!=null)
					matchList.add(gcmatch);
			} else if(monoMasses[j] - glycoMass>tolerance){
				break;
			}
		}
		
		GlycoCompositionMatch[] matches;
		if (matchList.size() == 0) {
			GlycoDeNovoMatcher deNoveMatcher = new GlycoDeNovoMatcher();
			matches = deNoveMatcher.match(nodes, preMz, preCharge, backboneMass, spectrum.getPrecursorMz());
			if (matches == null) {
				return null;
			} else {
				Arrays.sort(matches, new GlycoCompositionMatch.DeltaMassComparator());
			}
		} else {
			matches = matchList.toArray(new GlycoCompositionMatch[matchList.size()]);
			Arrays.sort(matches, new GlycoCompositionMatch.ScoreComparator());
		}
		
		return matches;
	}
	
	public GlycoCompositionMatch[] match(GlycoPeakNode[] nodes, double preMz, int preCharge, double backboneMass, double expMz) {

		double[] treeMws = new double[nodes.length];
		for (int i = 0; i < treeMws.length; i++) {
			treeMws[i] = nodes[i].getMw();
		}
		int[] find = new int[nCore.length];
		for (int i = 0; i < nCore.length; i++) {
			double gp = backboneMass + nCore[i];
			double begin = gp - fragTolerance * 10;
			int id = Arrays.binarySearch(treeMws, begin);
//System.out.println("core\t"+backboneMass+"\t"+gp);
			if (id < 0) {
				id = -id - 1;
			}

			for (int k = id; k < treeMws.length; k++) {

				if (Math.abs((treeMws[k] - gp) / (double) nodes[k].getCharge()) < fragTolerance) {
					find[i] = 1;
				}

				if ((treeMws[k] - gp) / (double) nodes[k].getCharge() > fragTolerance) {
					break;
				}
			}
		}
//System.out.println("508\t"+Arrays.toString(find)+"\t"+nodes.length+"\t"+backboneMass);
		if (find[0] == 0 || MathTool.getTotal(find) <= 3)
			return null;

		double glycoMass = (preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass;
		double tolerance = glycoMass * precursorTolerance * 1E-6 * preCharge;
		double beg = glycoMass - tolerance * 10;
		int id = Arrays.binarySearch(monoMasses, beg);

		if (id < 0) {
			id = -id - 1;
		}

		ArrayList<GlycoCompositionMatch> matchList = new ArrayList<GlycoCompositionMatch>();
		for (int j = id; j < monoMasses.length; j++) {
//System.out.println(monoMasses[j]+"\t"+glycoMass+"\t"+tolerance+"\t"+Math.abs(monoMasses[j] - glycoMass)+"\t"+preMz);				
			
			if (Math.abs(monoMasses[j] - glycoMass) < tolerance) {
				GlycoCompositionMatch gcmatch = this.match(massUnits[j], treeMws, nodes, backboneMass, monoMasses[j],
						(expMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - monoMasses[j]);
				if (gcmatch != null) {
					if (gcmatch.getComposition()[3] <= 2)
						matchList.add(gcmatch);
				}
			} else if(monoMasses[j] - glycoMass > tolerance){
				break;
			}
		}
		if (matchList.size() == 0)
			return null;
		
		GlycoCompositionMatch[] matches = matchList.toArray(new GlycoCompositionMatch[matchList.size()]);
		Arrays.sort(matches, new GlycoCompositionMatch.ScoreComparator());
		return matches;
	}
	
	private GlycoCompositionMatch match(MassUnit mu, double[] treeMws, GlycoPeakNode[] nodes, double backboneMass,
			double glycoMass, double deltaMass) {

		double[][] theoryGlycoPeaks = mu.getFragments();
		double topScore = -1;
		int[] topMatchedPeaks = null;
		int structureId = -1;
//System.out.println(Arrays.toString(treeMws));
		Peak[] matchedPeaks = null;
		for (int i = 0; i < theoryGlycoPeaks.length; i++) {

			int[] treeMatchList = new int[treeMws.length];
			int[] fragMatchList = new int[theoryGlycoPeaks[i].length];
			ArrayList<Peak> tempPeakList = new ArrayList<Peak>();
			for (int j = 0; j < theoryGlycoPeaks[i].length; j++) {
				double gp = backboneMass + theoryGlycoPeaks[i][j];
				double begin = gp - fragTolerance * 10;
				int id = Arrays.binarySearch(treeMws, begin);
//System.out.println("theory\t"+gp);
				if (id < 0) {
					id = -id - 1;
				}

				for (int k = id; k < treeMws.length; k++) {
//System.out.println((treeMws[k] - gp) / (double) nodes[k].getCharge()+"\t"+fragTolerance +"\t"+nodes[k].getPeakMz());
					if (Math.abs((treeMws[k] - gp) / (double) nodes[k].getCharge()) < fragTolerance) {
						treeMatchList[k] = 1;
						fragMatchList[j] = 1;
						tempPeakList.add(nodes[k].getPeak());
					}

					if ((treeMws[k] - gp) / (double) nodes[k].getCharge() > fragTolerance) {
						break;
					}
				}
			}
			int treeMatched = MathTool.getTotal(treeMatchList);
			int fragMatched = MathTool.getTotal(fragMatchList);
			if(fragMatched==fragMatchList.length) fragMatched--;
			double score = -1.0
					* Math.log10(RandomPCalor.getProbility(theoryGlycoPeaks[i].length - 1, fragMatched, probility));
//			score = ((double) treeMatched / (double) treeMws.length) * score;

			/*System.out.println(
					glycoUnits[mu.getId() - 1][i].getIupacName() + "\t" + glycoUnits[mu.getId() - 1][i].getMonoMass());
			System.out.println(Arrays.toString(treeMatchList));
			System.out.println(Arrays.toString(fragMatchList));
			System.out.println(Arrays.toString(theoryGlycoPeaks[i]));
			System.out.println(treeMatched + "\t" + treeMws.length + "\t" + score);*/
			
			if (score > topScore) {
				topScore = score;
				topMatchedPeaks = fragMatchList;
				structureId = i;
				matchedPeaks = tempPeakList.toArray(new Peak[tempPeakList.size()]);
			}
		}
		GlycoTree glycoTree = this.glycoUnits[mu.getId() - 1][structureId];
		if (!glycoTree.isMammal())
			return null;

		GlycoCompositionMatch gcmatch = new GlycoCompositionMatch(glycoTree.getSimpleComposition(),
				glycoTree.getCompositionString(), glycoTree.getMonoMass(), deltaMass, topScore, matchedPeaks);

		return gcmatch;
	}
	
	private static void test(String file, int scannum, double preMz, int preCharge, double backboneMass)
			throws IOException, XMLStreamException, DataFormatException {

		SpectraList splist = SpectraList.parseSpectraListFromMZXML(file, 2);
		Spectrum spectrum = splist.getScan(scannum);
		Features features = splist.getFeatures(spectrum.getPrecursorCharge(), spectrum.getPrecursorMz(), scannum, 20);
		
		GlycoHybridMatcher matcher = new GlycoHybridMatcher();
		RegionTopNIntensityFilter peakFilter = new RegionTopNIntensityFilter(4, 100, 0.01);
		System.out.println("before\t" + spectrum.getPeaks().length);
		Peak[] filteredPeak = peakFilter.filter(spectrum.getPeaks());
		spectrum.setPeaks(filteredPeak);
		spectrum.setPrecursorCharge(preCharge);
		System.out.println("after\t" + spectrum.getPeaks().length);
		for(int i=0;i<filteredPeak.length;i++){
			System.out.println("peak\t"+i+"\t"+filteredPeak[i].getMz());
		}
		GlycoPeakNode[] nodes = matcher.getGlycoNodeList(spectrum, preCharge);
		for(int i=0;i<nodes.length;i++){
			System.out.println(i+"\t"+nodes[i].getPeakMz()+"\t"+nodes[i].getMw());
		}
		GlycoCompositionMatch[] matches = matcher.match(nodes, features.getMonoMz(), preCharge, backboneMass, preMz);
		System.out.println(matches == null);
		if (matches != null) {
			for (int i = 0; i < matches.length; i++) {
				System.out.println(
						matches[i].getDeltaMass() + "\t" + matches[i].getScore() + "\t" + matches[i].getComString());
			}
		}
	}
	
	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub
		long begin = System.currentTimeMillis();
		GlycoHybridMatcher.test("D:\\Data\\Rui\\N_glyco\\20150930\\Rui_20150712_Huh7_media_intact_HILIC_1.mzXML", 
				51002, 1309.8568, 3, 2710.2008);
//		int[][] compositions = GlycoDeNovoMatcher.getComposition(892);
//		for(int i=0;i<compositions.length;i++)
//			System.out.println(compositions.length+"\t"+Arrays.toString(compositions[i]));
//		System.out.println(Math.log10(0.01));
		long end = System.currentTimeMillis();
		System.out.println((end-begin)/10000.0);
	}

}
