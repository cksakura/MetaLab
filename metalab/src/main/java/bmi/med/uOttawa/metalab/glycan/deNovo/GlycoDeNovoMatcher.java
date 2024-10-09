package bmi.med.uOttawa.metalab.glycan.deNovo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.RegionTopNIntensityFilter;
import bmi.med.uOttawa.metalab.spectra.SpectraList;
import bmi.med.uOttawa.metalab.spectra.Spectrum;

public class GlycoDeNovoMatcher {

	private static double fragTolerance = 1.0;
	private static double precursorTolerance = 20;
	private static String[] glycanNames = new String[] { Glycosyl.Fuc.getTitle(), Glycosyl.Hex.getTitle(),
			Glycosyl.HexNAc.getTitle(), Glycosyl.NeuAc.getTitle() };
	private static double[] glycanMasses = new double[] { Glycosyl.Fuc.getMonoMass(), Glycosyl.Hex.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass(), Glycosyl.NeuAc.getMonoMass() };
	private static double[] nCore = new double[] { Glycosyl.HexNAc.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass() * 2, Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass(),
			Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass() * 2,
			Glycosyl.HexNAc.getMonoMass() * 2 + Glycosyl.Hex.getMonoMass() * 3 };

	public int[] getRootTreeComp(Spectrum spectrum, int preCharge, double backboneMass) {
		
		Peak[] peaks = spectrum.getPeaks();
		double upLimit = spectrum.getPrecursorMass();
		
		for (int charge = preCharge; charge >= 1; charge--) {

			DenovoGlycoTree mainTree = getMainTree(peaks, backboneMass, charge, upLimit);
			if(mainTree==null)
				continue;
			
			int[] glycanCount = mainTree.getGlycanCount();
			return glycanCount;
		}
		return null;
	}
	
	public GlycoCompositionMatch[] match(Spectrum spectrum, double preMz, int preCharge, double backboneMass) {

		Peak[] peaks = spectrum.getPeaks();
		double upLimit = preMz * preCharge;
		
		for (int charge = preCharge; charge >= 1; charge--) {

			DenovoGlycoTree mainTree = getMainTree(peaks, backboneMass, charge, upLimit);
			if(mainTree==null)
				continue;

			int[] glycanCount = mainTree.getGlycanCount();
System.out.println(Arrays.toString(glycanCount)+"\n"+mainTree.getMap().keySet());			

			double treeMass = 0;
			for(int i=0;i<glycanCount.length;i++){
				treeMass += glycanCount[i] * glycanMasses[i];
			}
			double diff = (preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - treeMass;
			System.out.println(treeMass+"\t"+diff+"\t"+Arrays.toString(glycanCount));
			
			int[][] subcomp = getComposition(diff);
			if (subcomp.length > 0) {
				GlycoCompositionMatch[] matches = new GlycoCompositionMatch[subcomp.length];
				for (int i = 0; i < subcomp.length; i++) {
					int[] totalComp = new int[subcomp[i].length];
					double glycoMass = 0;
					for (int j = 0; j < totalComp.length; j++) {
						totalComp[j] = glycanCount[j] + subcomp[i][j];
						glycoMass += glycanMasses[j] * totalComp[j];
					}
					StringBuilder sb = new StringBuilder();
					sb.append(glycanNames[2]);
					sb.append("(").append(totalComp[2]).append(")");
					sb.append(glycanNames[1]);
					sb.append("(").append(totalComp[1]).append(")");
					if (totalComp[3] > 0) {
						sb.append(glycanNames[3]);
						sb.append("(").append(totalComp[3]).append(")");
					}
					if (totalComp[0] > 0) {
						sb.append(glycanNames[0]);
						sb.append("(").append(totalComp[0]).append(")");
					}
					double deltaMass = (preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - glycoMass;
					matches[i] = new GlycoCompositionMatch(totalComp, sb.toString(), glycoMass, deltaMass);
System.out.println(sb);
				}
				return matches;
			}
			/*for (int i = 0; i < pns.length; i++) {
				if (pns[i].isRoot() && pns[i]!=root) {
					DenovoGlycoTree tree = new DenovoGlycoTree();
					add(tree, pns[i]);
					if(tree.getSize()>2){
//						System.out.println("mainTree\t" + charge + "\t" + mainTree.getSize() + "\t"
//						+ mainTree.getMap().keySet());
					}
				}
			}*/
		}
		return null;
	}
	
	private DenovoGlycoTree getMainTree(Peak[] topPeaks, double backboneMass, int charge, double uplimit){

		GlycoPeakNode root = null;
		boolean[] find = new boolean[5];
		GlycoPeakNode[] pns = new GlycoPeakNode[topPeaks.length];
		for (int i = 0; i < pns.length; i++) {
			pns[i] = new GlycoPeakNode(topPeaks[i]);
			for (int j = 0; j < nCore.length; j++) {
				double deltaMz = Math.abs((backboneMass + nCore[j]) / charge + AminoAcidProperty.PROTON_W
						- topPeaks[i].getMz());
				if (deltaMz < fragTolerance) {

					find[j] = true;
					if (j == 0) {
						if (root == null) {
							root = pns[i];
							root.setDeltaMz(deltaMz);
							root.setMainTree(true);
							root.addGlycan(2);
						} else {
							if (deltaMz < root.getDeltaMz()) {
								root.setMainTree(false);
								root = pns[i];
								root.setDeltaMz(deltaMz);
								root.setMainTree(true);
								root.addGlycan(2);
							}
						}
					}
				}
			}
		}
System.out.println("root\t"+charge+"\t"+find[0]+"\t"+find[1]+"\t"+find[2]+"\t"+find[3]+"\t"+((backboneMass + nCore[0]) / charge + AminoAcidProperty.PROTON_W));

		if (!find[0] || !find[2] || !(find[3] || find[4])) {
			return null;
		}
//System.out.println("pass");		
		for (int i = 0; i < pns.length; i++) {
			if(!pns[i].isMainTree()) {
				continue;
			}
			System.out.println("caocaocao\t"+pns[i].getPeakMz());
			for (int j = i + 1; j < pns.length; j++) {
				double delta = topPeaks[j].getMz() - topPeaks[i].getMz();
				System.out.println("caocaocao\t"+pns[i].getPeakMz()+"\t"+delta);
				if (delta - glycanMasses[glycanMasses.length - 1]/(double)charge > fragTolerance) {
					break;
				}
				
				if (topPeaks[j].getMz() * charge > uplimit - glycanMasses[0])
					break;
				System.out.println("caocaocao\t"+pns[i].getPeakMz());
				for (int k = 0; k < glycanMasses.length; k++) {
					double deltaMz = Math.abs(delta - glycanMasses[k]/(double)charge);
					if(pns[i].getPeakMz()>1500){
						System.out.println(glycanMasses[k]+"\t"+topPeaks[j].getMz()+"\t"+deltaMz);
					}
					if (deltaMz < fragTolerance) {
						if(pns[j].getParent()==null){
							if(pns[i].getChildren()[k]==null){
								pns[j].setParent(pns[i]);
								if(pns[i]==root && k==1){
									pns[j].setMainTree(false);
								}
								pns[i].addChild(pns[j], k);
								pns[j].setDeltaMz(deltaMz);
								pns[j].setSubComposition(pns[i].getSubComposition().clone());
								pns[j].addGlycan(k);
								System.out.println("a\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());									
							}else{
								if(deltaMz<pns[i].getChildren()[k].getDeltaMz()){
									pns[i].getChildren()[k].setMainTree(false);
									pns[j].setParent(pns[i]);
									if(pns[i]==root && k==1){
										pns[j].setMainTree(false);
									}
									pns[i].addChild(pns[j], k);
									pns[j].setDeltaMz(deltaMz);
									pns[j].setSubComposition(pns[i].getSubComposition().clone());
									pns[j].addGlycan(k);
								}
//								System.out.println("b\t"+charge+"\t"+pns[i].getPeakMz()+"\t"+pns[j].getPeakMz());									
							}
						}
					}
				}
			}
		}

		if(root.getChildren()[3]!=null){
			if(!root.getChildren()[3].isTerminal())
				return null;
		}
		
		DenovoGlycoTree mainTree = new DenovoGlycoTree();
		add(mainTree, root);

		int[] glycanCount = mainTree.getGlycanCount();
System.out.println("211\t"+charge+"\t"+Arrays.toString(glycanCount));		
		if(glycanCount[1]<3 || glycanCount[2]<2)
			return null;
		
		return mainTree;
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
					
					double delta = peaks[j].getMz() - peaks[i].getMz();
					if (delta - glycanMasses[glycanMasses.length - 1] / (double) charge > fragTolerance) {
						break;
					}

					if (peaks[j].getMz() * charge > upLimit - glycanMasses[0])
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
	
	public GlycoCompositionMatch[] match(GlycoPeakNode[] nodes, double preMz, int preCharge, double backboneMass,
			double expPreMz) {

		double[] treeMws = new double[nodes.length];
		for (int i = 0; i < treeMws.length; i++) {
			treeMws[i] = nodes[i].getMw();
		}

		GlycoPeakNode root = null;
		int[] find = new int[nCore.length];
		ArrayList<Peak> peaklist = new ArrayList<Peak>();
		
		for (int i = 0; i < nCore.length; i++) {
			double gp = backboneMass + nCore[i];
			double begin = gp - fragTolerance * 10;
			int id = Arrays.binarySearch(treeMws, begin);
			if (id < 0) {
				id = -id - 1;
			}

			for (int k = id; k < treeMws.length; k++) {
				if (Math.abs((treeMws[k] - gp) / (double) nodes[k].getCharge()) < fragTolerance) {
					find[i] = 1;
					if (i == 0) {
						if (root == null) {
							root = nodes[k];
						} else {
							if (nodes[k].getPeakIntensity() > root.getPeakIntensity()) {
								root = nodes[k];
							}
						}
					}

					peaklist.add(nodes[k].getPeak());
				}

				if ((treeMws[k] - gp) / (double) nodes[k].getCharge() > fragTolerance) {
					break;
				}
			}
		}

		if (find[0] == 0 || MathTool.getTotal(find) <= 3)
			return null;

		setMainTree(root);

		double maxMass = 0;
		int[] glycanCount = null;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].isMainTree()) {
				if (nodes[i].getMw() > maxMass) {
					maxMass = nodes[i].getMw();
					glycanCount = nodes[i].getSubComposition();
				}
			}
		}

		Peak[] corePeaks = peaklist.toArray(new Peak[peaklist.size()]);
		Arrays.sort(corePeaks);

		return match(glycanCount, preMz, preCharge, backboneMass, expPreMz, corePeaks);
	}
	
	private void setMainTree(GlycoPeakNode node) {
		node.setMainTree(true);
		GlycoPeakNode[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null) {
				children[i].setMainTree(true);
			}
		}
	}
	
	/**
	 * the Some compositions were filtered out with the following
		rules (13): (a) the number of deoxyhexoses plus 1 must be
		less than or equal to the sum of the number of hexoses and Nacetylhexosamines
		and (b) if there are no N-acetylhexosamines,
		except in N-glycan core, then the number of sialic acids is zero.
		
		N-Glycoproteomics � An automated workflow approach, Glycobiology vol. 18 no. 4 pp. 339�349, 2008
		doi:10.1093/glycob/cwn013
		
	 * @param glycanCount
	 * @param preMz
	 * @param preCharge
	 * @param backboneMass
	 * @return
	 */
	public GlycoCompositionMatch[] match(int[] glycanCount, double preMz, int preCharge, double backboneMass,
			double expPreMz, Peak[] matchedCorePeaks) {
		if ((preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass > 4000)
			return null;
		double treeMass = 0;
		for (int i = 0; i < glycanCount.length; i++) {
			treeMass += glycanCount[i] * glycanMasses[i];
		}
		double subMass = (preMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - treeMass;
		// System.out.println(treeMass+"\t"+diff+"\t"+Arrays.toString(glycanCount));

		int[][] subcomp = getComposition(subMass);
		if (subcomp.length > 0) {

			ArrayList<GlycoCompositionMatch> matchList = new ArrayList<GlycoCompositionMatch>();
			for (int i = 0; i < subcomp.length; i++) {
				int[] totalComp = new int[subcomp[i].length];
				double glycoMass = 0;
				for (int j = 0; j < totalComp.length; j++) {
					totalComp[j] = glycanCount[j] + subcomp[i][j];
					glycoMass += glycanMasses[j] * totalComp[j];
				}
				if (totalComp[2] < 2 || totalComp[1] < 3)
					continue;
				if (totalComp[0] > 2 || totalComp[3] > 3)
					continue;
				if (totalComp[0] + 1 > totalComp[1] + totalComp[2])
					continue;
				if (totalComp[2] == 2 && totalComp[3] > 0)
					continue;

				double diff = preMz - AminoAcidProperty.PROTON_W - (backboneMass + glycoMass) / (double) preCharge;
				if (Math.abs(diff) > preMz * precursorTolerance * 1E-6) {
					continue;
				}

				StringBuilder sb = new StringBuilder();
				sb.append(glycanNames[2]);
				sb.append("(").append(totalComp[2]).append(")");
				sb.append(glycanNames[1]);
				sb.append("(").append(totalComp[1]).append(")");
				if (totalComp[3] > 0) {
					sb.append(glycanNames[3]);
					sb.append("(").append(totalComp[3]).append(")");
				}
				if (totalComp[0] > 0) {
					sb.append(glycanNames[0]);
					sb.append("(").append(totalComp[0]).append(")");
				}

				int[] comp = new int[]{totalComp[2], totalComp[1], totalComp[3], totalComp[0]};
				double deltaMass = (expPreMz - AminoAcidProperty.PROTON_W) * preCharge - backboneMass - glycoMass;
				matchList.add(new GlycoCompositionMatch(comp, sb.toString(), glycoMass, deltaMass, 0.0, matchedCorePeaks));
			}
			if (matchList.size() > 0) {
				GlycoCompositionMatch[] matches = matchList.toArray(new GlycoCompositionMatch[matchList.size()]);
				return matches;
			} else {
				return null;
			}
		}
		return null;
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
	
	private int[][] getComposition(double mass) {
		ArrayList<int[]> list = new ArrayList<int[]>();
		for (int i1 = 0;; i1++) {
			double gm = glycanMasses[0] * i1;
			if (gm - mass > fragTolerance) {
				break;
			}
			for (int i2 = 0;; i2++) {
				gm = glycanMasses[0] * i1 + glycanMasses[1] * i2;
				if (gm - mass > fragTolerance) {
					break;
				}
				for (int i3 = 0;; i3++) {
					gm = glycanMasses[0] * i1 + glycanMasses[1] * i2 + glycanMasses[2] * i3;
					if (gm - mass > fragTolerance) {
						break;
					}
					for (int i4 = 0;; i4++) {
						gm = glycanMasses[0] * i1 + glycanMasses[1] * i2 + glycanMasses[2] * i3 + glycanMasses[3] * i4;
						if (gm - mass > fragTolerance) {
							break;
						} else if (Math.abs(gm - mass) < fragTolerance) {
							list.add(new int[] { i1, i2, i3, i4 });
						}
					}
				}
			}
		}
		int[][] compositons = list.toArray(new int[list.size()][]);
		return compositons;
	}

	private static void test(String file, int scannum, double preMz, int preCharge, double backboneMass) throws IOException, XMLStreamException, DataFormatException{

		SpectraList splist = SpectraList.parseSpectraListFromMZXML(file, 2);
		Spectrum spectrum = splist.getScan(scannum);
		GlycoDeNovoMatcher matcher = new GlycoDeNovoMatcher();
		GlycoCompositionMatch[] matches = matcher.match(spectrum, preMz, preCharge, backboneMass);
		System.out.println(matches==null);
		if(matches!=null){
			for(int i=0;i<matches.length;i++){
				System.out.println(matches[i].getMass());
			}
		}
	}
	
	private static void test2(String file, int scannum, double preMz, int preCharge, double backboneMass)
			throws IOException, XMLStreamException, DataFormatException {

		SpectraList splist = SpectraList.parseSpectraListFromMZXML(file, 2);
		Spectrum spectrum = splist.getScan(scannum);
		RegionTopNIntensityFilter peakFilter = new RegionTopNIntensityFilter(4, 100, 0.01);
		spectrum.setPeaks(peakFilter.filter(spectrum.getPeaks()));
		GlycoDeNovoMatcher matcher = new GlycoDeNovoMatcher();
		GlycoPeakNode[] nodes = matcher.getGlycoNodeList(spectrum, preCharge);
		for (int i = 0; i < nCore.length; i++) {
			System.out.println("core\t" + (nCore[i] + backboneMass));
		}
		for (int i = 0; i < nodes.length; i++) {
			System.out.println("nodes\t" + i + "\t" + nodes[i].getPeakMz() + "\t" + nodes[i].getMw());
		}
		GlycoCompositionMatch[] matches = matcher.match(nodes, preMz, preCharge, backboneMass, preMz);
		System.out.println(matches == null);
		if (matches != null) {
			for (int i = 0; i < matches.length; i++) {
				System.out.println(matches[i].getComString() + "\t" + matches[i].getMass());
			}
		}
	}
	
	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub

		long begin = System.currentTimeMillis();
//		GlycoDeNovoMatcher.test2("D:\\Data\\Rui\\N_glyco\\20150930\\Rui_20150712_Huh7_media_intact_HILIC_1.mzXML", 
//				51073, 1309.8568, 3, 2710.2008);
//		int[][] compositions = GlycoDeNovoMatcher.getComposition(892);
//		for(int i=0;i<compositions.length;i++)
//			System.out.println(compositions.length+"\t"+Arrays.toString(compositions[i]));
		System.out.println(glycanMasses[1]*2+glycanMasses[2]*2);
		System.out.println(AminoAcidProperty.MONOW_C*6+AminoAcidProperty.MONOW_H*10+AminoAcidProperty.MONOW_O*5);
		long end = System.currentTimeMillis();
		System.out.println((end-begin)/10000.0);
	}

}
