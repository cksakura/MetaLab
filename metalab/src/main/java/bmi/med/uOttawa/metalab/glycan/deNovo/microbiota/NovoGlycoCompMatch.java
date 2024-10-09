package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.util.Comparator;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.model.RowObject;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoCompositionMatch;
import bmi.med.uOttawa.metalab.glycan.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.metalab.spectra.Peak;

public class NovoGlycoCompMatch extends GlycoCompositionMatch {

	private HashMap<Integer, GlycoPeakNode> nodeMap;
	private HashMap<Integer, GlycoPeakNode> oxoniumMap;
	private HashMap<Integer, GlycoPeakNode> missOxoniumMap;
	private double factor;
	private boolean determined;
	private int missNodeCount;
	private double qValue;
//	private int[] usedGlycosylIds;
	private boolean matchOxonium;
	
	private double treeScore;
	private double oxoniumScore;

	public NovoGlycoCompMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass, double factor, boolean determined) {
		super(scannum, preMz, preCharge, composition, comString, mass, deltaMass);
		// TODO Auto-generated constructor stub
		this.factor = factor;
		this.determined = determined;
	}

	public NovoGlycoCompMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass, double score, Peak[] matchedPeaks, double factor, boolean determined) {
		super(scannum, preMz, preCharge, composition, comString, mass, deltaMass, score, matchedPeaks);
		// TODO Auto-generated constructor stub
		this.factor = factor;
		this.determined = determined;
	}

	public NovoGlycoCompMatch(int scannum, double preMz, int preCharge, int[] composition, String comString,
			double mass, double deltaMass, double score, Peak[] peaks, HashMap<Integer, GlycoPeakNode> nodeMap,
			HashMap<Integer, GlycoPeakNode> oxoniumMap, HashMap<Integer, GlycoPeakNode> missOxoniumMap, double factor, 
			int missNodeCount, boolean determined) {
		super(scannum, preMz, preCharge, composition, comString, mass, deltaMass, score, peaks);
		// TODO Auto-generated constructor stub
		this.nodeMap = nodeMap;
		this.oxoniumMap = oxoniumMap;
		this.missOxoniumMap = missOxoniumMap;
		this.factor = factor;
		this.missNodeCount = missNodeCount;
		this.determined = determined;
		this.addNodeSid();
	}
	
	/**
	 * Set the id to each node
	 */
	private void addNodeSid() {
		for (GlycoPeakNode node : nodeMap.values()) {
			if (node.isRoot() && !node.isOxonium()) {
				node.setSid(0);
				setNodeSid(node);
				break;
			}
		}
	}
	
	private void setNodeSid(GlycoPeakNode node) {
		GlycoPeakNode[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null) {
				children[i].setSid(node.getSid() + 1);
				setNodeSid(children[i]);
			}
		}
	}

	public double getFactor() {
		return factor;
	}

	public boolean isDetermined() {
		return determined;
	}

	public HashMap<Integer, GlycoPeakNode> getNodeMap() {
		return nodeMap;
	}

	public HashMap<Integer, GlycoPeakNode> getOxoniumMap() {
		return oxoniumMap;
	}

	public HashMap<Integer, GlycoPeakNode> getMissOxoniumMap() {
		return missOxoniumMap;
	}

	public int getMissNodeCount() {
		return missNodeCount;
	}

	public void setMissNodeCount(int missNodeCount) {
		this.missNodeCount = missNodeCount;
	}

	public double getqValue() {
		return qValue;
	}

	public void setqValue(double qValue) {
		this.qValue = qValue;
	}

	/**
	 * For undetermined matches, if the unknown part matched an oxonium ion
	 * @return
	 */
	public boolean isMatchOxonium() {
		return matchOxonium;
	}

	public void setMatchOxonium(boolean matchOxonium) {
		this.matchOxonium = matchOxonium;
	}

	public double getTreeScore() {
		return treeScore;
	}

	public void setTreeScore(double treeScore) {
		this.treeScore = treeScore;
	}

	public double getOxoniumScore() {
		return oxoniumScore;
	}

	public void setOxoniumScore(double oxoniumScore) {
		this.oxoniumScore = oxoniumScore;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(scannum).append(";");
		sb.append(df4.format(preMz)).append(";");
		sb.append(preCharge).append(";");
		sb.append(comString).append(";");
		for (int i : composition) {
			sb.append(i).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");
		sb.append(df4.format(glycanMass)).append(";");
		sb.append(df4.format(deltaMass)).append(";");
		sb.append(df2.format(score)).append(";");
		sb.append(rank).append(";");

		for (Peak p : peaks) {
			sb.append(p).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");

		for (GlycoPeakNode node : nodeMap.values()) {
			sb.append(node).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");

		if (oxoniumMap.size() > 0) {
			for (GlycoPeakNode node : oxoniumMap.values()) {
				sb.append(node).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(";");

		if (missOxoniumMap.size() > 0) {
			for (GlycoPeakNode node : missOxoniumMap.values()) {
				sb.append(node).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(";");
		sb.append(factor).append(";");
		sb.append(missNodeCount).append(";");
		sb.append(determined).append(";");
		sb.append(classScore).append(";");
		sb.append(FormatTool.getDFE2().format(qValue));

		return sb.toString();
	}

	public static NovoGlycoCompMatch parse(String line) {

		String[] cs = line.split(";");
		int scannum = Integer.parseInt(cs[0]);
		double premz = Double.parseDouble(cs[1]);
		int precharge = Integer.parseInt(cs[2]);
		String comstring = cs[3];
		String[] coms = cs[4].split(",");
		int[] composition = new int[coms.length];
		for (int i = 0; i < coms.length; i++) {
			composition[i] = Integer.parseInt(coms[i]);
		}
		double mass = Double.parseDouble(cs[5]);
		double deltaMass = Double.parseDouble(cs[6]);
		double score = Double.parseDouble(cs[7]);
		int rank = Integer.parseInt(cs[8]);
		String[] mps = cs[9].split(",");
		Peak[] peaks = new Peak[mps.length];
		for (int i = 0; i < mps.length; i++) {
			peaks[i] = Peak.parse(mps[i]);
		}

		String[] ns = cs[10].split(",");
		GlycoPeakNode[] nodes = new GlycoPeakNode[ns.length];
		HashMap<Integer, GlycoPeakNode> nodemap = new HashMap<Integer, GlycoPeakNode>();

		for (int i = 0; i < ns.length; i++) {
			nodes[i] = GlycoPeakNode.parse(ns[i], peaks);
			nodemap.put(nodes[i].getPeakId(), nodes[i]);
		}
		for (int i = 0; i < nodes.length; i++) {
			int parentId = nodes[i].getParentPeakId();
			if (parentId != -1) {
				nodes[i].setParent(nodemap.get(parentId));
			}
		}

		HashMap<Integer, GlycoPeakNode> oxoniumMap = new HashMap<Integer, GlycoPeakNode>();
		if (cs[11].length() > 0) {
			String[] oxons = cs[11].split(",");
			for (int i = 0; i < oxons.length; i++) {
				GlycoPeakNode node = GlycoPeakNode.parse(oxons[i], peaks);
				oxoniumMap.put(node.getPeakId(), node);
			}
		}

		HashMap<Integer, GlycoPeakNode> missOxoniumMap = new HashMap<Integer, GlycoPeakNode>();
		if (cs[12].length() > 0) {
			String[] missOxons = cs[12].split(",");
			for (int i = 0; i < missOxons.length; i++) {
				GlycoPeakNode node = GlycoPeakNode.parse(missOxons[i], peaks);
				missOxoniumMap.put(node.getPeakId(), node);
			}
		}

		double factor = Double.parseDouble(cs[13]);
		int missNodeCount = Integer.parseInt(cs[14]);
		boolean determined = Boolean.parseBoolean(cs[15]);
		double classScore = Double.parseDouble(cs[16]);
		double qvalue = Double.parseDouble(cs[17]);

		NovoGlycoCompMatch match = new NovoGlycoCompMatch(scannum, premz, precharge, composition, comstring, mass,
				deltaMass, score, peaks, nodemap, oxoniumMap, missOxoniumMap, factor, missNodeCount, determined);

		match.setRank(rank);
		match.setClassScore(classScore);
		match.setqValue(qvalue);

		return match;
	}

	public double getOxoniumFactor() {
		double s1 = 0;
		for (Integer id : this.oxoniumMap.keySet()) {
			GlycoPeakNode node = this.oxoniumMap.get(id);
			s1 += node.getSingleScore();
		}
		double s2 = 0;
		for (Integer id : this.missOxoniumMap.keySet()) {
			GlycoPeakNode node = this.missOxoniumMap.get(id);
			s2 += node.getSingleScore();
		}
		return s1 - s2;
	}
	
	public RowObject getRowObject() {
		Object[] objects = new Object[9];
		objects[0] = scannum;
		objects[1] = preMz;
		objects[2] = preCharge;
		objects[3] = comString;
		objects[4] = glycanMass;
		objects[5] = deltaMass;
		objects[6] = score;
		objects[7] = qValue;

		RowObject ro = new RowObject(objects);
		return ro;
	}

	public static class ScoreComparator implements Comparator<NovoGlycoCompMatch> {

		@Override
		public int compare(NovoGlycoCompMatch arg0, NovoGlycoCompMatch arg1) {
			// TODO Auto-generated method stub

			if (arg0.determined && !arg1.determined) {
				return -1;
			}

			if (!arg0.determined && arg1.determined) {
				return 1;
			}

			if (arg0.getScore() < arg1.getScore())
				return 1;
			if (arg0.getScore() > arg1.getScore())
				return -1;
			return 0;
		}

	}
	
	public static void main(String[] args) {
		String ss = "978;887.6465;3;Hex(3)HexNAc(2)NeuAc(2);0,3,0,2,2,0;1474.508;-0.0086;4.5;1;186.0761_9602.209_.8462_.8571,187.0791_1127.3997_.1667_.625,197.0444_892.3806_.0769_.375,204.0866_25744.3164_.9487_1.0,205.0899_4034.979_.6667_.75,207.0062_1026.2395_.1154_.5,274.0921_12803.5273_.8846_1.0,288.0378_1271.6042_.3205_.625,292.1029_6040.5195_.7564_.875,306.0486_3431.3481_.5769_.75,330.0586_1065.9263_.141_.375,331.0835_766.3928_.0385_.125,366.1392_13775.666_.9103_1.0,367.1433_1687.2706_.4744_.7143,401.1785_1329.6392_.3846_.4,404.086_1816.7844_.5128_.8,450.9927_904.153_.0897_.25,500.0467_2116.9158_.5385_.875,501.2415_8372.5752_.8077_1.0,536.3553_1058.8204_.1282_.25,539.1881_1242.3076_.2564_.5,546.235_1264.1156_.3077_.6,547.1669_1372.104_.4103_.7,566.1348_1179.6002_.2436_.375,593.7124_3142.2959_.5641_.8571,594.2133_1134.9015_.1795_.1429,611.0906_1518.278_.4615_.6,621.2829_6698.6772_.7692_1.0,674.7377_5381.6533_.7308_.75,675.2396_6900.1577_.7821_1.0,675.7393_3649.2654_.6154_.6667,693.921_1732.3358_.5_.5556,695.1807_5384.6685_.7436_.8889,695.2468_1152.0922_.1923_.2222,695.7467_1288.6211_.359_.3333,696.254_1288.926_.3718_.4444,767.2696_1261.1937_.2949_.3333,776.2779_43483.7773_.9744_1.0,776.7792_34972.0234_.9615_.8333,777.2799_10614.8018_.859_.6667,777.7809_3622.1042_.6026_.5,824.3674_1693.274_.4872_.2667,848.3016_3753.1453_.641_.4545,848.7992_5027.7983_.6795_.5455,849.2994_5115.8135_.7051_.6364,849.8282_1283.2906_.3333_.0909,857.3047_88300.9609_1.0_1.0,857.8057_68261.3047_.9872_.9091,858.2358_1470.4828_.4359_.2727,858.3069_19483.6855_.9359_.8182,858.8042_8482.4414_.8205_.7273,859.3071_1330.0148_.3974_.1818,950.3309_1155.5265_.2051_.2,958.8438_13902.1602_.9231_1.0,959.3456_11710.3838_.8718_.8,959.8455_5185.5049_.7179_.6,986.4172_3744.6787_.6282_.4,1039.875_7048.604_.7949_.6667,1040.3723_13084.2832_.8974_1.0,1040.8699_5087.3901_.6923_.3333,1116.7273_1176.8315_.2308_.25,1148.4639_8943.7197_.8333_1.0,1149.4712_3527.4277_.5897_.75,1153.3436_2124.856_.5513_.5,1208.4806_1493.26_.4487_1.0,1310.5217_3854.915_.6538_1.0,1311.5214_2072.4399_.5256_.5,1513.5743_1432.7075_.4231_1.0,1552.3936_1261.0616_.2821_.6667,1581.283_1258.0215_.2692_.5,1908.1978_1176.3909_.2179_1.0,2302.436_1286.9614_.3462_1.0;33$2$0$1$24$-1_-1_-1_-1_-1_-1$0_0_0_1_0_0,37$2$0$2$28$-1_46_-1_-1_-1_-1$0_1_0_1_0_0,53$2$0$4$46$-1_57_-1_-1_-1_-1$0_2_0_2_0_0,24$2$0$0$-1$-1_28_-1_33_-1_-1$0_0_0_0_0_0,57$2$0$5$53$-1_-1_-1_-1_-1_-1$0_3_0_2_0_0,28$2$0$1$24$-1_-1_-1_37_-1_-1$0_1_0_0_0_0,46$2$0$3$37$-1_-1_-1_53_-1_-1$0_2_0_1_0_0;3$1$1$0$-1$-1_-1_-1_-1_-1_-1$0_0_0_0_0_0,8$1$1$0$-1$-1_-1_-1_-1_-1_-1$0_0_0_0_0_0,12$1$1$0$-1$-1_-1_-1_-1_-1_-1$0_0_0_0_0_0;;0.8333333333333334;0;true;0;1.51E-2";
		NovoGlycoCompMatch mm = NovoGlycoCompMatch.parse(ss);
		Peak[] peaks = mm.getPeaks();
		for(int i=0;i<peaks.length;i++)
			System.out.println("aa\t"+peaks[i].getMz()+"\t"+peaks[i].getIntensity()+"\t"+peaks[i].getRankScore());
	}
}
