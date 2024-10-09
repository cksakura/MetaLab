/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;

/**
 * @author Kai Cheng
 *
 */
public class GlycoTreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1505308741664460483L;

	private String selfid;
	private String parentid;

	private String stemType;
	private GlycoTreeNode parentNode;
	private ArrayList<GlycoTreeNode> childNodeList;
	private LinkedList<String> substituentList;

	/**
	 * e.g. 2-1, x-x
	 */
	private String parentLinkPosition;

	/**
	 * a, b, o, x
	 */
	private char anomeric = '\u0000';

	/**
	 * d, l, x
	 */
	private char stereo = '\u0000';

	private double monoMass;
	private double aveMass;

	private Glycosyl glyco;

	private int XPosition;
	private int YPosition;

	private GlycoTreeNode up;
	private GlycoTreeNode down;
	private GlycoTreeNode left;
	private GlycoTreeNode leftUp;
	private GlycoTreeNode leftDown;

	private boolean lock = false;
	/**
	 * 0==up, 1== left up, 2==left, 3==left down, 4==down
	 */
	private int orient;

	public GlycoTreeNode(String selfid, String stemType) {
		this.selfid = selfid;
		this.stemType = stemType;
		this.anomeric = stemType.charAt(0);
		this.childNodeList = new ArrayList<GlycoTreeNode>();
		this.substituentList = new LinkedList<String>();
	}

	public String getSelfid() {
		return selfid;
	}

	public char getAnomeric() {
		return anomeric;
	}

	public void setParentId(String parentid) {
		this.parentid = parentid;
	}

	public String getParentId() {
		return parentid;
	}

	public void setParentNode(GlycoTreeNode parentNode, String parentLinkPosition) {
		this.parentNode = parentNode;
		this.parentid = parentNode.selfid;
		this.parentLinkPosition = parentLinkPosition;
	}

	public GlycoTreeNode getParentNode() {
		return parentNode;
	}

	public String getParentLink() {
		return this.parentLinkPosition;
	}

	public ArrayList<GlycoTreeNode> getChildNodeList() {
		return childNodeList;
	}

	public void addChildTreeNode(GlycoTreeNode childNode, String linkPosition) {
		this.childNodeList.add(childNode);
		linkPosition = linkPosition.replaceAll("\\d\\|\\d", "x");
		childNode.setParentNode(this, linkPosition);
	}

	public boolean isLeaf() {
		return this.childNodeList.size() == 0;
	}

	public void addSubstituents(String sub, String linkPosition) {

		linkPosition = linkPosition.replaceAll("\\d\\|\\d", "x");
		if (sub.equals("n-acetyl")) {
			this.substituentList.addFirst("(" + linkPosition + ")" + sub);
		} else {
			this.substituentList.add("(" + linkPosition + ")" + sub);
		}
	}

	public LinkedList<String> getSubList() {
		return substituentList;
	}

	public String getGlycoCTName() {

		StringBuilder sb = new StringBuilder();
		sb.append(stemType);

		if (this.substituentList.size() > 0) {

			sb.append("||");

			String[] list = substituentList.toArray(new String[substituentList.size()]);

			for (int i = 0; i < list.length; i++) {
				sb.append(list[i]).append("|");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public void traverse() {

		int childNumber = childNodeList.size();
		for (int i = 0; i < childNumber; i++) {
			GlycoTreeNode child = childNodeList.get(i);
			child.traverse();
		}
	}

	public void traverse(double mass) {

		int childNumber = childNodeList.size();
		for (int i = 0; i < childNumber; i++) {
			GlycoTreeNode child = childNodeList.get(i);
			mass += child.monoMass;
			child.traverse(mass);
		}
	}

	public String getId() {
		// return "<"+this.selfid+">";
		return this.selfid;
	}

	public String getIUPACname(HashMap<String, String> namemap) {
		String ctname = this.getGlycoCTName();
		if (namemap.containsKey(ctname)) {
			return namemap.get(ctname);
		} else {
			return "Unknown";
		}
	}

	public void setX(int x) {
		this.XPosition = x;
	}

	public void setY(int y) {
		this.YPosition = y;
	}

	public int getX() {
		return XPosition;
	}

	public int getY() {
		return YPosition;
	}

	public void setUp(GlycoTreeNode child, int disY) {
		this.up = child;
		child.setX(this.XPosition);
		child.setY(this.YPosition - (int) (disY * 0.8));
		child.setOrient(0);
	}

	public GlycoTreeNode getUp() {
		return up;
	}

	public void setDown(GlycoTreeNode child, int disY) {
		this.down = child;
		child.setX(this.XPosition);
		child.setY(this.YPosition + (int) (disY * 0.8));
		child.setOrient(4);
	}

	public GlycoTreeNode getDown() {
		return down;
	}

	public void setLeft(GlycoTreeNode child, int disX) {
		this.left = child;
		child.setX(this.XPosition - disX);
		child.setY(this.YPosition);
		child.setOrient(2);
	}

	public GlycoTreeNode getLeft() {
		return left;
	}

	public void setLeftUp(GlycoTreeNode child, int disX, int disY) {
		this.leftUp = child;
		child.setX(this.XPosition - disX);
		child.setY(this.YPosition - disY);
		child.setOrient(1);
	}

	public GlycoTreeNode getLeftUp() {
		return leftUp;
	}

	public void setLeftDown(GlycoTreeNode child, int disX, int disY) {
		this.leftDown = child;
		child.setX(this.XPosition - disX);
		child.setY(this.YPosition + disY);
		child.setOrient(3);
	}

	public GlycoTreeNode getLeftDown() {
		return leftDown;
	}

	public Glycosyl getGlycosyl() {

		if (glyco == null) {

			String des = this.getGlycoCTName();
			this.glyco = Glycosyl.judgeType(des);
		}

		return glyco;
	}

	public void setOrient(int orient) {
		this.orient = orient;
	}

	public int getOrient() {
		return orient;
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

}
