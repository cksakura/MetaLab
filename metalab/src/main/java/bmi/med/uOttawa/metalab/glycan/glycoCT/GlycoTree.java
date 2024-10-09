/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.math.Combinator;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;

/**
 * @author Kai Cheng
 *
 */
public class GlycoTree {

	private HashMap<String, GlycoTreeNode> nodemap;
	private HashMap<String, String> submap;
	private HashMap<String, int[]> coordinateMap;

	private double monoMass;
	private double aveMass;
	private String iupacName;
	private String glycoCT;
	private int[] composition;
	private double[] fragments;
	private int numOfGlcNAc = 0;
	/**
	 * the ring structure is not validate
	 */
	private boolean ring;
	/**
	 * Monosaccharide linked to sub, this type will be discarded
	 */
	private boolean subLink;

	private boolean isMammal;

	private boolean hasNeuAc;

	private boolean hasCoreFuc;

	// private static int[] commonGlycanId = new int[]{1, 2, 3, 7, 8, 9, 12,
	// 18};

	public GlycoTree() {

		this.nodemap = new HashMap<String, GlycoTreeNode>();
		this.submap = new HashMap<String, String>();
		this.coordinateMap = new HashMap<String, int[]>();
	}

	public GlycoTree(String glycoCT) {

		this.nodemap = new HashMap<String, GlycoTreeNode>();
		this.submap = new HashMap<String, String>();
		this.coordinateMap = new HashMap<String, int[]>();
		this.glycoCT = glycoCT;
		this.parseGlycoTree();
	}

	/**
	 * @return the monoMass
	 */
	public double getMonoMass() {
		return monoMass;
	}

	/**
	 * @param monoMass
	 *            the monoMass to set
	 */
	public void setMonoMass(double monoMass) {
		this.monoMass = monoMass;
	}

	/**
	 * @return the aveMass
	 */
	public double getAveMass() {
		return aveMass;
	}

	/**
	 * @param aveMass
	 *            the aveMass to set
	 */
	public void setAveMass(double aveMass) {
		this.aveMass = aveMass;
	}

	/**
	 * @return the iupacName
	 */
	public String getIupacName() {
		return iupacName;
	}

	/**
	 * @param iupacName
	 *            the iupacName to set
	 */
	public void setIupacName(String iupacName) {
		this.iupacName = iupacName;
	}

	/**
	 * @return the glycoCT
	 */
	public String getGlycoCT() {
		return glycoCT;
	}

	/**
	 * @param glycoCT
	 *            the glycoCT to set
	 */
	public void setGlycoCT(String glycoCT) {
		this.glycoCT = glycoCT;
	}

	/**
	 * @param composition
	 *            the composition to set
	 */
	public void setComposition(int[] composition) {
		this.composition = composition;
	}

	public int[] getComposition() {
		return composition;
	}
	
	public int[] getSimpleComposition() {
		int[] simComp = new int[4];
		simComp[0] = composition[1] + composition[2] + composition[3];
		simComp[1] = composition[7] + composition[8] + composition[9];
		simComp[2] = composition[12];
		simComp[3] = composition[18] + composition[19];
		return simComp;
	}

	public String getCompositionString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HexNAc");
		sb.append("(").append(composition[1] + composition[2] + composition[3]).append(")");
		sb.append("Hex");
		sb.append("(").append(composition[7] + composition[8] + composition[9]).append(")");
		if (composition[12] > 0) {
			sb.append("NeuAc");
			sb.append("(").append(composition[12]).append(")");
		}
		if (composition[18] + composition[19] > 0) {
			sb.append("Fuc");
			sb.append("(").append(composition[18] + composition[19]).append(")");
		}

		return sb.toString();
	}

	/**
	 * @return the fragments
	 */
	public double[] getFragments() {
		return fragments;
	}

	/**
	 * @param fragments
	 *            the fragments to set
	 */
	public void setFragments(double[] fragments) {
		this.fragments = fragments;
	}

	public String getType() {

		if (this.numOfGlcNAc == 2) {
			return "High mannose";
		} else {
			return "Complex/Hybrid";
		}
	}

	public void parseInfo() {
		this.composition = new int[20];
		this.isMammal = true;
		for (GlycoTreeNode node : this.nodemap.values()) {
			int graphid = node.getGlycosyl().getGraphicsId();
			if (graphid < 20)
				this.composition[graphid]++;
			if (graphid == 20 || graphid == 10 || graphid == 4 || graphid == 5 || graphid == 11 || graphid == 13
					|| graphid == 14 || graphid == 15 || graphid == 16 || graphid == 17) {
				this.isMammal = false;
			}

			if (graphid == 12) {
				this.hasNeuAc = true;
			}

			if (node.getParentId() == null) {
				ArrayList<GlycoTreeNode> cnodes = node.getChildNodeList();
				for (int i = 0; i < cnodes.size(); i++) {
					int cid = cnodes.get(i).getGlycosyl().getGraphicsId();
					if (cid == 18 || cid == 19) {
						this.hasCoreFuc = true;
					}
				}
			}
		}
	}

	private void parseGlycoTree() {

		String[] lines = glycoCT.split("\n");
		boolean res = false;
		boolean lin = false;

		for (int i = 0; i < lines.length; i++) {

			String line = lines[i];

			if (line.startsWith("RES")) {

				res = true;

			} else if (line.startsWith("LIN")) {

				lin = true;
				res = false;

			} else {

				if (res) {

					int beg = line.indexOf(":");

					String id = line.substring(0, beg - 1);
					String typejudeg = line.substring(0, beg);
					String content = line.substring(beg + 1);

					if (typejudeg.endsWith("b")) {

						GlycoTreeNode node = new GlycoTreeNode(id, content);
						this.addNode(id, node);

					} else if (typejudeg.endsWith("s")) {

						this.addSub(id, content);

					} else {

					}

				} else if (lin) {

					String[] ss = line.split("[:()+]");
					if (ss.length > 4) {
						String parentid = ss[1].substring(0, ss[1].length() - 1);
						String childid = ss[4].substring(0, ss[4].length() - 1);
						char parentLinkType = ss[1].charAt(ss[1].length() - 1);
						char childLinkType = ss[4].charAt(ss[3].length() - 1);
						String linkPosition1 = ss[2];
						String linkPosition2 = ss[3];

						this.addLink(parentid, childid, parentLinkType, childLinkType, linkPosition1, linkPosition2);
					}
				}
			}
		}

		this.composition = new int[20];
		for (GlycoTreeNode node : this.nodemap.values()) {
			int graphid = node.getGlycosyl().getGraphicsId();
			if (graphid < 20)
				this.composition[graphid]++;
			if (MonosaccharideDB.isGlcNac(node.getGlycoCTName())) {
				this.numOfGlcNAc++;
			}
		}
	}

	public void addNode(String id, GlycoTreeNode node) {
		this.nodemap.put(id, node);
	}

	public void addSub(String id, String sub) {
		this.submap.put(id, sub);
	}

	public HashMap<String, GlycoTreeNode> getNodeMap() {
		return this.nodemap;
	}

	public void addLink(String parentid, String childid, char parentLinkType, char childLinkType, String linkPosition1,
			String linkPosition2) {

		if (nodemap.containsKey(parentid)) {

			GlycoTreeNode parentNode = nodemap.get(parentid);

			if (nodemap.containsKey(childid)) {

				GlycoTreeNode childNode = nodemap.get(childid);
				if (childNode.getParentNode() != null || childNode.getId().equals("1")) {
					this.ring = true;
				}
				parentNode.addChildTreeNode(childNode, linkPosition2 + "-" + linkPosition1);

			} else if (submap.containsKey(childid)) {

				parentNode.addSubstituents(submap.get(childid), linkPosition1 + parentLinkType + ":" + linkPosition2);

			}
		} else {
			this.subLink = true;
		}
	}

	public void test() {
		Iterator<String> it = this.nodemap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			System.out.println(this.nodemap.get(key).getGlycoCTName());
		}
	}

	public boolean isNGlycan() {

		GlycoTreeNode n1 = this.nodemap.get("1");
		if (n1 == null)
			return false;

		String n1ss = n1.getGlycoCTName();
		// System.out.println("1\t"+n1ss);

		if (!MonosaccharideDB.isGlcNac(n1ss))
			return false;

		ArrayList<GlycoTreeNode> n1child = n1.getChildNodeList();
		GlycoTreeNode n2 = null;
		for (int i = 0; i < n1child.size(); i++) {

			String n2ss = n1child.get(i).getGlycoCTName();
			// System.out.println("2\t"+n2ss);
			if (MonosaccharideDB.isGlcNac(n2ss)) {
				n2 = n1child.get(i);

			} else if (MonosaccharideDB.isFuc(n2ss)) {

			} else {
				return false;
			}
		}

		if (n2 == null)
			return false;

		ArrayList<GlycoTreeNode> n2child = n2.getChildNodeList();
		GlycoTreeNode n3 = null;
		for (int i = 0; i < n2child.size(); i++) {

			String n3ss = n2child.get(i).getGlycoCTName();
			// System.out.println("3\t"+n3ss);
			if (MonosaccharideDB.isMan(n3ss)) {

				n3 = n2child.get(i);

			} else if (MonosaccharideDB.isFuc(n3ss)) {

			} else {
				return false;
			}
		}

		if (n3 == null)
			return false;

		ArrayList<GlycoTreeNode> n3child = n3.getChildNodeList();
		int mancount = 0;
		int glcnaccount = 0;
		int xylcount = 0;
		for (int i = 0; i < n3child.size(); i++) {

			String n4ss = n3child.get(i).getGlycoCTName();
			// System.out.println("4\t"+n4ss);
			if (MonosaccharideDB.isMan(n4ss)) {

				mancount++;

			} else if (MonosaccharideDB.isGlcNac(n4ss)) {

				glcnaccount++;

			} else if (MonosaccharideDB.isXyl(n4ss)) {

				xylcount++;

			} else {
				return false;
			}
		}

		// if(xylcount>1 || glcnaccount>1)
		// return false;

		if (mancount == 2)
			return true;
		else
			return false;
	}

	private String getIDString() {

		GlycoTreeNode node = this.nodemap.get("1");

		return getSubTreeIDString(node);
	}

	private String getSubTreeIDString(GlycoTreeNode node) {

		StringBuilder sb = new StringBuilder();
		ArrayList<GlycoTreeNode> childlist = node.getChildNodeList();

		if (childlist.size() == 0) {

			sb.append(node.getId()).append("-");
			return sb.toString();

		} else {

			int maxid = -1;
			int max = 0;
			for (int i = 0; i < childlist.size(); i++) {

				GlycoTreeNode childe = childlist.get(i);
				int length = this.getMaxLinkLength(childe);

				if (length > max) {
					max = length;
					maxid = i;
				}
			}

			sb.append(getSubTreeIDString(childlist.get(maxid)));
			for (int i = 0; i < childlist.size(); i++) {
				if (i != maxid) {
					sb.append("(").append(getSubTreeIDString(childlist.get(i))).append(")");
				}
			}

			sb.append(node.getId()).append("-");
		}

		return sb.toString();
	}

	private String getGlycoString(HashMap<String, String> namemap) {

		GlycoTreeNode node = this.nodemap.get("1");

		return getSubTreeString(node, namemap);
	}

	private String getSubTreeString(GlycoTreeNode node, HashMap<String, String> namemap) {

		StringBuilder sb = new StringBuilder();
		ArrayList<GlycoTreeNode> childlist = node.getChildNodeList();

		if (childlist.size() == 0) {

			sb.append(node.getIUPACname(namemap)).append(node.getParentLink());
			return sb.toString();

		} else {

			int maxid = -1;
			int max = 0;
			for (int i = 0; i < childlist.size(); i++) {

				GlycoTreeNode childe = childlist.get(i);
				int length = this.getMaxLinkLength(childe);

				if (length > max) {
					max = length;
					maxid = i;
				}
			}

			sb.append(getSubTreeString(childlist.get(maxid), namemap));
			for (int i = 0; i < childlist.size(); i++) {
				if (i != maxid) {
					sb.append("(").append(getSubTreeString(childlist.get(i), namemap)).append(")");
				}
			}

			if (node.getParentNode() == null) {

				sb.append(node.getIUPACname(namemap)).append("-Asn");

			} else {
				sb.append(node.getIUPACname(namemap)).append(node.getParentLink());
			}
		}

		return sb.toString();
	}

	private int getMaxLinkLength(GlycoTreeNode node) {

		ArrayList<GlycoTreeNode> childlist = node.getChildNodeList();
		int max = 0;
		for (int i = 0; i < childlist.size(); i++) {

			int length = getMaxLinkLength(childlist.get(i));
			if (length > max) {
				max = length;
			}
		}

		return max + 1;
	}

	private HashSet<Double> traverseFragList(GlycoTreeNode[] nodelist, double mass, HashMap<String, Double> massmap,
			HashSet<Double> set) {

		ArrayList<GlycoTreeNode> list = new ArrayList<GlycoTreeNode>();
		for (int i = 0; i < nodelist.length; i++) {
			GlycoTreeNode node = nodelist[i];
			list.addAll(node.getChildNodeList());
		}

		int size = list.size();
		GlycoTreeNode[] nodes = list.toArray(new GlycoTreeNode[size]);
		set.add(mass);

		for (int i = 1; i <= size; i++) {

			Object[][] combines = Combinator.getCombination(nodes, i);

			for (int j = 0; j < combines.length; j++) {

				GlycoTreeNode[] comb = new GlycoTreeNode[combines[j].length];
				double dd = mass;

				for (int k = 0; k < combines[j].length; k++) {

					GlycoTreeNode child = (GlycoTreeNode) combines[j][k];
					double childmass = massmap.get(child.getGlycoCTName());
					comb[k] = child;
					dd += childmass;
				}

				set.add(dd);
				traverseFragList(comb, dd, massmap, set);
			}
		}

		return set;
	}

	public void getCoordinate(int rootX, int rootY, int disX, int disY) {

		GlycoTreeNode root = this.nodemap.get("1");
		root.setX(rootX);
		root.setY(rootY);
		this.coordinateMap.put("1", new int[] { rootX, rootY });
		root.setLock(true);

		GlycoTreeNode child1 = null;

		ArrayList<GlycoTreeNode> list0 = root.getChildNodeList();
		for (int i = 0; i < list0.size(); i++) {

			GlycoTreeNode child0 = list0.get(i);
			Glycosyl glyco0 = child0.getGlycosyl();
			child0.setLock(true);

			if (glyco0 == Glycosyl.GlcNAc) {

				root.setLeft(child0, disX);
				child1 = child0;
				this.coordinateMap.put(child1.getId(), new int[] { child1.getX(), child1.getY() });

			} else {

				if (root.getUp() == null) {

					root.setUp(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });

				} else {

					root.setDown(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });
				}
			}
		}

		GlycoTreeNode child2 = null;
		ArrayList<GlycoTreeNode> list1 = child1.getChildNodeList();
		for (int i = 0; i < list1.size(); i++) {

			GlycoTreeNode child0 = list1.get(i);
			Glycosyl glyco0 = child0.getGlycosyl();
			child0.setLock(true);

			if (glyco0 == Glycosyl.Man) {

				child2 = child0;
				child1.setLeft(child2, disX);
				this.coordinateMap.put(child2.getId(), new int[] { child2.getX(), child2.getY() });
				child2.setOrient(2);

			} else {

				if (child1.getUp() == null) {

					child1.setUp(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });

				} else {

					child1.setDown(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });
				}
			}
		}

		ArrayList<GlycoTreeNode> list2 = child2.getChildNodeList();

		GlycoTreeNode child3 = null;
		GlycoTreeNode child4 = null;

		for (int i = 0; i < list2.size(); i++) {

			GlycoTreeNode child0 = list2.get(i);
			Glycosyl glyco0 = child0.getGlycosyl();

			if (glyco0 == Glycosyl.Man) {

				if (child3 == null) {

					child3 = child0;
					child2.setLeftUp(child3, disX, disY);
					this.coordinateMap.put(child3.getId(), new int[] { child3.getX(), child3.getY() });

				} else {

					child4 = child0;
					child2.setLeftDown(child4, disX, disY);
					this.coordinateMap.put(child4.getId(), new int[] { child4.getX(), child4.getY() });
				}

			} else {

				if (child2.getUp() == null) {

					child2.setUp(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });

				} else {

					child2.setDown(child0, disY);
					this.coordinateMap.put(child0.getId(), new int[] { child0.getX(), child0.getY() });
				}

				ArrayList<GlycoTreeNode> list3 = child0.getChildNodeList();
				if (list3.size() == 1) {
					GlycoTreeNode child00 = list3.get(0);
					child0.setUp(child00, disY);
					this.coordinateMap.put(child00.getId(), new int[] { child00.getX(), child00.getY() });
				}
			}
		}

		addCoordinate(disX, disY, child3, child3.getX(), child3.getY());
		addCoordinate(disX, disY, child4, child4.getX(), child4.getY());
	}

	private void resetCoordinate(String self, int x, int y, int disY) {

		boolean reset = false;
		int orient = nodemap.get(self).getOrient();

		Iterator<String> it = this.coordinateMap.keySet().iterator();
		while (it.hasNext()) {

			String key = it.next();
			if (key.equals(self))
				continue;

			int[] coo = this.coordinateMap.get(key);
			if (x == coo[0]) {
				if (Math.abs(coo[1] - y) < disY / 2) {

					reset = true;
					break;
				}
			}
		}

		if (reset) {

			Iterator<String> it2 = this.coordinateMap.keySet().iterator();
			while (it2.hasNext()) {

				String key = it2.next();

				if (nodemap.get(key).getLock()) {
					continue;
				}

				int[] coo = this.coordinateMap.get(key);
				if (coo[1] > y) {

					coo[1] += disY / 2;

				} else if (coo[1] < y) {

					coo[1] -= disY / 2;

				} else {

					if (orient == 0 || orient == 1) {

						if (key.equals(self)) {
							coo[1] += disY / 2;
						} else {
							coo[1] -= disY / 2;
						}

					} else if (orient == 3 || orient == 4) {

						if (key.equals(self)) {
							coo[1] -= disY / 2;
						} else {
							coo[1] += disY / 2;
						}
					}
				}
				coordinateMap.put(key, coo);
				nodemap.get(key).setY(coo[1]);
			}
		}
	}

	private void addCoordinate(int disX, int disY, GlycoTreeNode node, int x, int y) {

		ArrayList<GlycoTreeNode> list = node.getChildNodeList();
		ArrayList<GlycoTreeNode> temp = new ArrayList<GlycoTreeNode>();
		int neuAcCount = 0;

		for (int i = 0; i < list.size(); i++) {

			GlycoTreeNode child = list.get(i);
			Glycosyl glyco = child.getGlycosyl();

			if (glyco == Glycosyl.Fuc || glyco == Glycosyl.Xylose) {

				if (child.getUp() == null) {

					node.setUp(child, disY);
					coordinateMap.put(child.getId(), new int[] { child.getX(), child.getY() });
					resetCoordinate(child.getId(), child.getX(), child.getY(), disY);

				} else {

					node.setDown(child, disY);
					coordinateMap.put(child.getId(), new int[] { child.getX(), child.getY() });
					resetCoordinate(child.getId(), child.getX(), child.getY(), disY);
				}

			} else {
				temp.add(child);
				if (glyco == Glycosyl.NeuAc) {
					neuAcCount++;
				}
			}
		}

		switch (temp.size()) {
		case 0:
			break;

		case 1: {

			GlycoTreeNode child = temp.get(0);
			node.setLeft(child, disX);
			coordinateMap.put(child.getId(), new int[] { child.getX(), child.getY() });
			resetCoordinate(child.getId(), child.getX(), child.getY(), disY);
			addCoordinate(disX, disY, child, child.getX(), child.getY());

			break;
		}

		case 2: {

			if (neuAcCount == 2) {

				GlycoTreeNode child1 = temp.get(0);
				node.setLeft(child1, disX);
				coordinateMap.put(child1.getId(), new int[] { child1.getX(), child1.getY() });
				resetCoordinate(child1.getId(), child1.getX(), child1.getY(), disY);
				addCoordinate(disX, disY, child1, child1.getX(), child1.getY());

				GlycoTreeNode child2 = temp.get(1);
				node.setUp(child2, disY);
				coordinateMap.put(child2.getId(), new int[] { child2.getX(), child2.getY() });
				resetCoordinate(child2.getId(), child2.getX(), child2.getY(), disY);
				addCoordinate(disX, disY, child2, child2.getX(), child2.getY());

			} else {
				GlycoTreeNode child1 = temp.get(0);
				node.setLeftUp(child1, disX, disY);
				coordinateMap.put(child1.getId(), new int[] { child1.getX(), child1.getY() });
				resetCoordinate(child1.getId(), child1.getX(), child1.getY(), disY);
				addCoordinate(disX, disY, child1, child1.getX(), child1.getY());

				GlycoTreeNode child2 = temp.get(1);
				node.setLeftDown(child2, disX, disY);
				coordinateMap.put(child2.getId(), new int[] { child2.getX(), child2.getY() });
				resetCoordinate(child2.getId(), child2.getX(), child2.getY(), disY);
				addCoordinate(disX, disY, child2, child2.getX(), child2.getY());
			}

			break;
		}

		case 3: {

			int maxid = -1;
			int max = 0;
			for (int i = 0; i < temp.size(); i++) {

				GlycoTreeNode childe = temp.get(i);
				int length = this.getMaxLinkLength(childe);

				if (length > max) {
					max = length;
					maxid = i;
				}
			}

			for (int i = 0; i < temp.size(); i++) {

				GlycoTreeNode childi = temp.get(i);

				if (i == maxid) {

					node.setLeft(childi, disX);
					coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
					resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
					addCoordinate(disX, disY, childi, childi.getX(), childi.getY());

				} else {

					if (node.getLeftUp() == null) {

						node.setLeftUp(childi, disX, disY);
						coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
						resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
						addCoordinate(disX, disY, childi, childi.getX(), childi.getY());

					} else {

						node.setLeftDown(childi, disX, disY);
						coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
						resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
						addCoordinate(disX, disY, childi, childi.getX(), childi.getY());
					}
				}
			}

			break;
		}

		case 4: {

			int maxid = -1;
			int max = 0;
			for (int i = 0; i < temp.size(); i++) {

				GlycoTreeNode childe = temp.get(i);
				int length = this.getMaxLinkLength(childe);

				if (length > max) {
					max = length;
					maxid = i;
				}
			}

			for (int i = 0; i < temp.size(); i++) {

				GlycoTreeNode childi = temp.get(i);

				if (i == maxid) {

					node.setLeftUp(childi, disX, disY);
					coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
					resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
					addCoordinate(disX, disY, childi, childi.getX(), childi.getY());

				} else {

					if (node.getLeftDown() == null) {

						node.setLeftDown(childi, disX, disY);
						coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
						resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
						addCoordinate(disX, disY, childi, childi.getX(), childi.getY());

					} else {

						if (node.getUp() == null) {

							node.setUp(childi, disY);
							coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
							resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
							addCoordinate(disX, disY, childi, childi.getX(), childi.getY());

						} else {

							node.setDown(childi, disY);
							coordinateMap.put(childi.getId(), new int[] { childi.getX(), childi.getY() });
							resetCoordinate(childi.getId(), childi.getX(), childi.getY(), disY);
							addCoordinate(disX, disY, childi, childi.getX(), childi.getY());
						}
					}
				}
			}

			break;
		}
		}
	}

	public HashMap<String, int[]> getCoordinateMap() {
		return this.coordinateMap;
	}

	public boolean isRing() {
		return ring;
	}

	public boolean isSubLind() {
		return subLink;
	}

	public boolean isMammal() {
		return this.isMammal;
	}

	public boolean hasNeuAc() {
		return this.hasNeuAc;
	}

	public boolean hasCoreFuc() {
		return this.hasCoreFuc;
	}

	public int getNumOfFuc() {
		return this.composition[18] + this.composition[19];
	}

}
