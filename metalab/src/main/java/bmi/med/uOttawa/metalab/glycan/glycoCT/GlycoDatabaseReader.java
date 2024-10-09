/**
 * 
 */
package bmi.med.uOttawa.metalab.glycan.glycoCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;

/**
 * @author Kai Cheng
 *
 */
public class GlycoDatabaseReader {

	private static String nameDir = "/resources/Monosaccharide.all.txt";
	// private static String nameDir = "/resources/Monosaccharide.txt";
	private static String dir = "Resources/N_GlycoCT.ID.txt";
	private static String massDir = "Resources/N_Mass.Info.txt";

	private GlycoTree[] units;
	// private double[] masses;
	private BufferedReader reader;
	// private HashMap<Double, MassUnit> massUnitMap;

	private ArrayList<MassUnit> massUnitList;
	private MassUnit[] massUnits;

	public GlycoDatabaseReader() throws IOException {

		this.getMassList();

		HashSet<String> fragset = new HashSet<String>();
		ArrayList<GlycoTree> list = new ArrayList<GlycoTree>();

		this.reader = new BufferedReader(new FileReader(dir));

		boolean res = false;
		boolean lin = false;

		GlycoTree gtree = null;
		StringBuilder treeBuilder = new StringBuilder();
		String name = "";

		String line = null;

		while ((line = reader.readLine()) != null) {

			if (line.startsWith("ID")) {

				String[] ss = line.split("\t");
				int massid = Integer.parseInt(ss[1]);
				int fragId = Integer.parseInt(ss[2]);

				MassUnit mu = this.massUnits[massid - 1];
				double[] fragments = mu.getFragments()[fragId - 1];

				gtree.setMonoMass(mu.getMono());
				gtree.setAveMass(mu.getAverage());
				gtree.setIupacName(name);
				gtree.setFragments(fragments);
				gtree.setGlycoCT(treeBuilder.toString());
				fragset.add(Arrays.toString(fragments));
				
				int [] composition = new int[20];
				for(GlycoTreeNode node : gtree.getNodeMap().values()){
					int graphid = node.getGlycosyl().getGraphicsId();
					if(graphid<20) composition[graphid]++;
				}
				gtree.setComposition(composition);
				
				list.add(gtree);

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

					gtree.addLink(parentid, childid, parentLinkType,
							childLinkType, linkPosition1, linkPosition2);
				}
			}
		}

		this.units = list.toArray(new GlycoTree[list.size()]);

//		System.out.println(units.length + "\t" + fragset.size());
		// this.masses = new double[masslist.size()];
		// for (int i = 0; i < masses.length; i++) {
		// masses[i] = masslist.get(i);
		// }

		/*
		 * Arrays.sort(units, new Comparator <GlycoDatabaseUnit>(){
		 * 
		 * @Override public int compare(GlycoDatabaseUnit arg0,
		 * GlycoDatabaseUnit arg1) { // TODO Auto-generated method stub
		 * 
		 * if(arg0.getMonoMass()>arg1.getMonoMass()){
		 * 
		 * return 1;
		 * 
		 * }else if(arg0.getMonoMass()<arg1.getMonoMass()){
		 * 
		 * return -1; }
		 * 
		 * return 0; }
		 * 
		 * });
		 */
		reader.close();
	}

	private void getMassList() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(massDir));
		this.massUnitList = new ArrayList<MassUnit>();
		double[][] peaks = null;
		// double[][] revpeaks = null;
		int id = 0;
		double mono = 0;
		double avg = 0;
		int[] composition = null;
		int num = 0;
		int idx = 0;

		String line = null;
		while ((line = reader.readLine()) != null) {

			if (line.startsWith("ID")) {

				if (mono != 0) {
					// MassUnit unit = new MassUnit(id, mono, avg, peaks,
					// revpeaks);
					MassUnit unit = new MassUnit(id, mono, avg, peaks);
					massUnitList.add(unit);
				}

				String[] ss = line.split("\t");

				id = Integer.parseInt(ss[1]);
				mono = Double.parseDouble(ss[2]);
				avg = Double.parseDouble(ss[3]);
				num = Integer.parseInt(ss[4]);
				peaks = new double[num][];
				// revpeaks = new double[num][];
				composition = new int[ss.length - 5];
				for (int i = 0; i < composition.length; i++) {
					composition[i] = Integer.parseInt(ss[i + 5]);
				}
				idx = 0;

			} else {

				String[] ss = line.split("\t");
				// String [] revss = reader.readLine().split("\t");
				peaks[idx] = new double[ss.length];
				// revpeaks[idx] = new double [ss.length];
				for (int i = 0; i < ss.length; i++) {
					peaks[idx][i] = Double.parseDouble(ss[i]);
					// revpeaks[idx][i] = Double.parseDouble(revss[i]);
				}
				idx++;
			}
		}

		MassUnit unit = new MassUnit(id, mono, avg, peaks);
		massUnitList.add(unit);

		reader.close();

		massUnits = new MassUnit[massUnitList.size()];
		massUnits = massUnitList.toArray(massUnits);

//		System.out.println(massUnitList.size());
	}

	public GlycoTree[] getUnits() {
		return units;
	}

	public static void ddd(String monomass, String name) throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(monomass));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] ss = line.split("\t");
			map.put(ss[0], ss[1]);
		}
		reader.close();
		System.out.println(map.size());

		BufferedReader r2 = new BufferedReader(new FileReader(name));
		String l2 = null;
		while ((l2 = r2.readLine()) != null) {
			String[] ss = l2.split("\t");
			if (map.containsKey(ss[0])) {
				System.out.println(ss[0] + "\t" + ss[1] + "\t" + ss[2] + "\t"
						+ map.get(ss[0]));
			} else {
				System.out.println("AAAAAAA\t" + ss[0]);
			}
		}
		r2.close();
	}

	/**
	 * @param fullname
	 * @return
	 */
	public static String findName(String fullname) {

		String[] name_sub = fullname.split("\\W?\\+\\W?");

		StringBuilder sb = new StringBuilder();

		String[] sss = name_sub[0].split("-");

		switch (sss.length) {

		case 3: {
			if (sss[0].length() == 1) {
				if (sss[1].length() == 1) {
					sb.append(sss[2].substring(0, 3));
					sb.append(sss[2].substring(4).replaceAll("[\\d]", ""));
				} else {
					sb.append(sss[1].replaceAll("[\\d]", ""));
					sb.append("-");
					sb.append(sss[2]);
				}
			} else {
				if (sss[1].length() == 1) {
					sb.append(sss[2]);
				}
			}

			break;
		}
		case 4: {
			
			sb.append(sss[2].substring(0, 3));
			sb.append(sss[2].substring(4).replaceAll("[\\d]", ""));
			sb.append("-");
			sb.append(sss[3]);

			break;
		}
		case 5: {
			
			if (sss[0].length() == 1) {
				if (sss[1].length() == 1) {
					if (sss[4].equals("ol")) {
						sb.append(sss[1]).append("-");
						sb.append(sss[2]).append("-");
						sb.append(sss[3]).append("-");
						sb.append(sss[4]);

					} else {
						String nopf = sss[4].replaceAll("p", "");
						nopf = nopf.replaceAll("f", "");
						nopf = nopf.replaceAll("[\\d]", "");
						sb.append(sss[2]).append("-");
						sb.append(sss[3]).append("-");
						sb.append(nopf);
					}
				} else {
					sb.append(sss[1]).append("-");
					sss[4] = sss[4].replaceAll("Hepp", "Hep");
					sss[4] = sss[4].replaceAll("[\\d]Me", "Me");
					sb.append(sss[4]);
				}
			}

			break;
		}
		case 6: {
			sb.append("keto-3-deoxy-eryHex2ulo-onic");
			break;
		}
		case 7: {
			sb.append("4-en-4-deoxy-thrHexA");
			break;
		}
		case 9: {
			sb.append("gro-3,9-deoxy-manNon2ulo5NAc7N-formyl-onic");
			break;
		}
		}

		if (name_sub.length > 1) {

			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (int i = 1; i < name_sub.length; i++) {
				String mod = name_sub[1]
						.substring(name_sub[i].indexOf(" ") + 1);
				if (map.containsKey(mod)) {
					map.put(mod, map.get(mod) + 1);
				} else {
					map.put(mod, 1);
				}
			}
			Iterator<String> it = map.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Integer count = map.get(key);
				if (count == 1) {
					sb.append("+").append(key);
				} else {
					sb.append("+").append(key).append("*").append(map.get(key));
				}
			}
		}

		return sb.toString();
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, String> getNameMap() throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();
		// BufferedReader reader = new BufferedReader(new FileReader(nameDir));
		File file = new File(System.getProperty("user.dir") + nameDir);
		// System.out.println("DatabaseReader 381\t"+file.getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] ss = line.split("\t");
			// System.out.println(ss[1]+"\t"+ss[2]+"\t"+findName(ss[1]));
			map.put(ss[0], ss[2]);
		}
		reader.close();

		return map;
	}

	public static HashMap<String, Double> getMonoMassMap() throws IOException {

		HashMap<String, Double> map = new HashMap<String, Double>();
		// BufferedReader reader = new BufferedReader(new FileReader(nameDir));
		File file = new File(System.getProperty("user.dir") + nameDir);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] ss = line.split("\t");
			map.put(ss[0], Double.parseDouble(ss[3]));
		}
		reader.close();
		return map;
	}

	private static void test() throws IOException {

		BufferedReader reader = new BufferedReader(
				new FileReader(
						"H:\\Glyco_structure_20130507\\Original.Monosaccharide.Pattern.txt"));

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		HashMap<Integer, String> map3 = new HashMap<Integer, String>();
		HashSet<String> set2 = new HashSet<String>();

		String line = null;
		while ((line = reader.readLine()) != null) {

			String[] ss = line.split("\t");

			String[] name_sub = ss[2].split("\\W?\\+\\W?");
			StringBuilder sb = new StringBuilder();

			if (name_sub.length == 1) {

				String[] sss = name_sub[0].split("-");
				map.put(sss.length, ss[2]);

				if (sss.length == 3) {

					if (sss[0].length() == 1) {
						if (sss[1].length() == 1) {
							sb.append(sss[2].substring(0, 3));
							sb.append(sss[2].substring(4));
						} else {
							sb.append(sss[1]);
							sb.append("-");
							sb.append(sss[2]);
						}
					} else {
						if (sss[1].length() == 1) {
							sb.append(sss[0]);
							sb.append("-");
							sb.append(sss[2]);
						}
					}
					// System.out.println(sb);
				} else if (sss.length == 4) {

					if (sss[0].length() == 1) {
						if (sss[1].length() == 1) {
							sb.append(sss[2].substring(0, 3));
							sb.append(sss[2].substring(4));
							sb.append("-");
							sb.append(sss[3]);
						} else {
							sb.append(sss[2]);
							sb.append("-");
							sb.append(sss[3]);

						}
					} else {
						if (sss[1].length() == 1) {
							sb.append(sss[1]);
							sb.append("-");
							sb.append(sss[3]);

						} else {

						}
					}
				} else if (sss.length == 5) {
					if (sss[0].length() == 1) {
						if (sss[1].length() == 1) {
							if (sss[4].equals("ol")) {
								sb.append(sss[1]).append("-");
								sb.append(sss[2]).append("-");
								sb.append(sss[3]).append("-");
								sb.append(sss[4]);

							} else {
								String nopf = sss[4].replaceAll("p", "");
								nopf = nopf.replaceAll("f", "");
								sb.append(sss[2]).append("-");
								sb.append(sss[3]).append("-");
								sb.append(nopf);
							}
						} else {
							sb.append(sss[1]).append("-");
							sb.append(sss[4].substring(0, 6));
						}
					}
				} else if (sss.length == 9) {
					sb.append("gro-3,9-deoxy-manNon2ulo5NAc7N-formyl-onic");
				} else if (sss.length == 6) {
					sb.append("keto-3-deoxy-eryHex2ulo-onic");

				} else if (sss.length == 7) {
					System.out.println("mipa\t" + name_sub[0]);
					sb.append("4-en-4-deoxy-thrHexA");
				}

			}
		}

		Iterator<Integer> it = map.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			System.out.println(key + "\t" + map.get(key));
		}
		System.out.println("~~~~~~~~~");
		Iterator<Integer> it3 = map3.keySet().iterator();
		while (it3.hasNext()) {
			Integer key = it3.next();
			System.out.println(key + "\t" + map3.get(key));
		}

		reader.close();
		System.out.println(set2);
	}

	private void testMassRange() throws IOException {

		double[] ppms = new double[20];
		for (int i = 0; i < ppms.length; i++) {
			ppms[i] = (i + 1) * 10.0;
		}

		int[] repeatCount = new int[ppms.length];
		for (int i = 0; i < massUnits.length; i++) {
			System.out.println(massUnits[i].getMono());
			for (int j = 0; j < ppms.length; j++) {
				double tolerance = massUnits[i].getMono() * ppms[j] * 1E-6;
				boolean have = false;
				if (i - 1 >= 0) {
					if (massUnits[i].getMono() - massUnits[i - 1].getMono() < tolerance) {
						have = true;
					}
				}
				if (i + 1 < massUnits.length) {
					if (massUnits[i + 1].getMono() - massUnits[i].getMono() < tolerance) {
						have = true;
					}
				}
				if (have) {
					for (int k = j; k < ppms.length; k++) {
						repeatCount[k]++;
					}
					break;
				}
			}
		}
		for (int i = 0; i < ppms.length; i++) {
//			System.out.println(ppms[i] + "\t" + repeatCount[i] + "\t"
//					+ massUnits.length + "\t" + (double) repeatCount[i]
//					/ (double) massUnits.length);
		}
	}
	
	private void testMassRange2() throws IOException {

		double[] ppms = new double[20];
		for (int i = 0; i < ppms.length; i++) {
			ppms[i] = (i + 1) * 10.0;
		}

		int[][] repeatCount = new int[ppms.length][12];
		for (int i = 0; i < massUnits.length; i++) {

			for (int j = 0; j < ppms.length; j++) {

				double tolerance = massUnits[i].getMono() * ppms[j] * 1E-6;

				int count = massUnits[i].getFragments().length;
				for (int k = i - 1; k >= 0; k--) {
					if (massUnits[i].getMono() - massUnits[k].getMono() < tolerance) {
						count += massUnits[k].getFragments().length;
					} else {
						break;
					}
				}
				for (int k = i + 1; k < massUnits.length; k++) {
					if (massUnits[k].getMono() - massUnits[i].getMono() < tolerance) {
						count += massUnits[k].getFragments().length;
					} else {
						break;
					}
				}
				if (count > 0)
					repeatCount[j][count - 1]++;
				// System.out.print(count+"\t");
			}
			// System.out.println();
		}
		for (int i = 0; i < repeatCount.length; i++) {
			System.out.print(ppms[i] + "\t");
			for (int j = 0; j < repeatCount[i].length; j++) {
				System.out.print(repeatCount[i][j] + "\t");
			}
			System.out.println();
			// System.out.println(ppms[i] + "\t" + repeatCount[i] + "\t"
			// + massUnits.length + "\t" + (double) repeatCount[i]
			// / (double) massUnits.length);
		}
	}
	
	/*private static void testOverlap(String in) throws FileDamageException,
			IOException {

		NGlycoPepCriteria filter = new NGlycoPepCriteria(true);
		HashMap<String, Double> map = new HashMap<String, Double>();
		PeptideListReader reader = new PeptideListReader(in);
		IPeptide pep = null;
		while ((pep = reader.getPeptide()) != null) {
			if (!filter.filter(pep))
				continue;
			String sequence = pep.getSequence().substring(2,
					pep.getSequence().length() - 2);
			char[] aas = sequence.toCharArray();
			Arrays.sort(aas);
			String key = new String(aas);
			double mw = pep.getMr();
			map.put(key, mw);
		}
		reader.close();

		Double[] mws = map.values().toArray(new Double[map.size()]);
		Arrays.sort(mws);

		double[] ppms = new double[10];
		for (int i = 0; i < ppms.length; i++) {
			ppms[i] = (i + 1) * 1.0;
		}

		int[] repeatCount = new int[ppms.length];
		for (int i = 0; i < mws.length; i++) {

			for (int j = 0; j < ppms.length; j++) {
				double tolerance = mws[i] * ppms[j] * 1E-6;
				boolean have = false;
				if (i - 1 >= 0) {
					if (mws[i] - mws[i - 1] < tolerance) {
						have = true;
					}
				}
				if (i + 1 < mws.length) {
					if (mws[i + 1] - mws[i] < tolerance) {
						have = true;
					}
				}
				if (have) {
					for (int k = j; k < ppms.length; k++) {
						repeatCount[k]++;
					}
					break;
				}
			}
		}
		for (int i = 0; i < ppms.length; i++) {
			System.out.println(ppms[i] + "\t" + repeatCount[i] + "\t"
					+ mws.length + "\t" + (double) repeatCount[i]
					/ (double) mws.length);
		}
	}
	*/
	private static void target2Decoy(String in, String out) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		
		while((line=reader.readLine())!=null){
			String [] cs = line.split("\t");
			
			if(line.startsWith("ID")){
				writer.write(line+"\n");
			}else{
				StringBuilder sb = new StringBuilder();
				for(int i=0;i<cs.length;i++){
					if(i%2==0){
						sb.append(Double.parseDouble(cs[i])+5.0).append("\t");
					}else{
						sb.append(Double.parseDouble(cs[i])-5.0).append("\t");
					}
				}
				writer.write(sb+"\n");
			}
		}
		reader.close();
		writer.close();
	}
	
	private static void addRearrange(String in, String out) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		while((line=reader.readLine())!=null){
			if(line.startsWith("ID")){
				writer.println(line);
			}else{
				String[] cs = line.split("\t");
				double[] masses = new double[cs.length+1];
				for(int i=0;i<cs.length;i++){
					masses[i] = Double.parseDouble(cs[i]);
				}
				masses[masses.length-1] = Glycosyl.HexNAc.getMonoMass()+Glycosyl.Hex.getMonoMass();
				Arrays.parallelSort(masses);
				StringBuilder sb = new StringBuilder();
				for(int i=0;i<masses.length;i++){
					sb.append(masses[i]).append("\t");
				}
				writer.println(sb);
			}
		}
		reader.close();
		writer.close();
	}

}
