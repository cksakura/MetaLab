package bmi.med.uOttawa.metalab.dbSearch.fragpipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNPrecursor;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNResultReader;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;

public class FragPipeTest {
	
	@SuppressWarnings("unused")
	private static void getDB(String in, String db, String out) throws IOException {
		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			set.add(line);
		}
		reader.close();
		
		PrintWriter writer = new PrintWriter(out);
		for(String genome: set) {
			File fastafile = new File(db, genome+".faa");
			reader = new BufferedReader(new FileReader(fastafile));
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			reader.close();
		}
		writer.close();
	}
	
	@SuppressWarnings("unused")
	private static void test(String in, String out, String db) throws IOException {
		HashSet<String> totalPsmSet = new HashSet<String>();
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> genomeMap2 = new HashMap<String, HashSet<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			String[] pros = cs[6].split(",");
			for (int i = 0; i < pros.length; i++) {
				if (pros[i].startsWith("MGYG")) {
					String genome = pros[i].substring(0, pros[i].indexOf("_"));
					if (genomeMap.containsKey(genome)) {
						genomeMap.get(genome).add(cs[0]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(cs[0]);
						genomeMap.put(genome, set);
					}
					if (genomeMap2.containsKey(genome)) {
						genomeMap2.get(genome).add(cs[0]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(cs[0]);
						genomeMap2.put(genome, set);
					}
					totalPsmSet.add(cs[0]);
				}
			}
		}
		reader.close();

		System.out.println(genomeMap.size() + "\t" + totalPsmSet.size());
		HashSet<String> genomeSet = new HashSet<String>();

		refineGenomesTest(genomeMap, genomeSet);

		System.out.println(genomeMap.size() + "\t" + genomeSet.size());

		String[] cs = genomeSet.toArray(new String[genomeSet.size()]);
		Arrays.sort(cs, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				int i1 = genomeMap2.get(o1).size();
				int i2 = genomeMap2.get(o2).size();
				if (i1 < i2) {
					return 1;
				} else if (i1 > i2) {
					return -1;
				}
				return 0;
			}
		});

		PrintWriter writer = new PrintWriter(out);

		HashSet<String> tempSet = new HashSet<String>();
		for (int i = 0; i < cs.length; i++) {
			HashSet<String> set = genomeMap2.get(cs[i]);
			tempSet.addAll(set);
			System.out.println(i + "\t" + tempSet.size() + "\t" + totalPsmSet.size() + "\t"
					+ (double) tempSet.size() / (double) totalPsmSet.size());

			double ratio = (double) tempSet.size() / (double) totalPsmSet.size();
			if (ratio > 0.95) {
				break;
			}

			File fastafile = new File(db, cs[i] + ".faa");
			reader = new BufferedReader(new FileReader(fastafile));
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			reader.close();
		}
		writer.close();
	}

	private static void refineGenomesTest(HashMap<String, HashSet<String>> genomeMap, HashSet<String> genomeSet) {

		if (genomeMap.size() == 0) {
			return;
		}

		String genome = "";
		int psmCount = 0;

		Iterator<String> genomeIt = genomeMap.keySet().iterator();
		while (genomeIt.hasNext()) {
			String key = genomeIt.next();
			int size = genomeMap.get(key).size();
			if (size == 0) {
				genomeIt.remove();
				continue;
			}

			if (size > psmCount) {
				genome = key;
				psmCount = size;
			}
		}
System.out.println(genome+"\t"+ psmCount);
		if (psmCount <= 1) {
			return;
		}

//System.out.println(genome+"\t"+psmCount+"\tMGYG000000085\t"+genomeMap.get("MGYG000000085").size());
		HashSet<String> psmSet = genomeMap.get(genome);
		genomeMap.remove(genome);
		genomeSet.add(genome);

		for (String key : genomeMap.keySet()) {
			HashSet<String> set = genomeMap.get(key);
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String psm = it.next();
				if (psmSet.contains(psm)) {
					it.remove();
				}
			}
		}

		refineGenomesTest(genomeMap, genomeSet);
	}
	
	@SuppressWarnings({ "unused", "deprecation" })
	private void refineTest(String folder, String fileName, String output) throws IOException {

		HashSet<String> refineGenomeSet = new HashSet<String>();

		HashSet<String> totalPepSet = new HashSet<String>();
		DiaNNResultReader reader = new DiaNNResultReader(folder, fileName, "K*,R*");
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> copyMap = new HashMap<String, HashSet<String>>();
		DiaNNPrecursor[] diapp = reader.getDiaNNPrecursors();
		int count = 0;
		for (int i = 0; i < diapp.length; i++) {
			if (diapp[i].getDecoyEvi() == 0) {
				count++;
				String[] pros = diapp[i].getProGroups();
				for (int j = 0; j < pros.length; j++) {
					String genome = pros[j].split("_")[0];
					if (map.containsKey(genome)) {
						map.get(genome).add(diapp[i].getSeqString());
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(diapp[i].getSeqString());
						map.put(genome, set);
					}

					if (copyMap.containsKey(genome)) {
						copyMap.get(genome).add(diapp[i].getSeqString());
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(diapp[i].getSeqString());
						copyMap.put(genome, set);
					}
				}

				totalPepSet.add(diapp[i].getSeqString());
			}
		}

		HashSet<String> totalSet = new HashSet<String>();
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		for (String key : map.keySet()) {
			countMap.put(key, map.get(key).size());
			totalSet.addAll(map.get(key));
		}

//		System.out.println(map.size() + "\t" + refineGenomeSet.size());
		refineGenomesTest(map, refineGenomeSet);

		PrintWriter writer = new PrintWriter(output);
//		System.out.println(map.size() + "\t" + refineGenomeSet.size());

		HashSet<String> currentSet = new HashSet<String>();
		for (String genome : refineGenomeSet) {
			writer.println(genome + "\t" + countMap.get(genome) + "\tTure");
			currentSet.addAll(copyMap.get(genome));
		}

		System.out.println(currentSet.size() + "\t" + totalSet.size() + "\t"
				+ (double) currentSet.size() / (double) totalSet.size());

		for (String genome : map.keySet()) {
			writer.println(genome + "\t" + countMap.get(genome) + "\tFalse");
		}

		writer.close();

		String[] genomeStrings = copyMap.keySet().toArray(new String[copyMap.size()]);
		Arrays.sort(genomeStrings, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				int c1 = copyMap.get(o1).size();
				int c2 = copyMap.get(o2).size();
				return c2 - c1;
			}
		});

		HashSet<String> pepSet = new HashSet<String>();
		int totalPepSetSize = totalPepSet.size();
		int currentTotalSize = pepSet.size();

		HashSet<String> usedSet = new HashSet<String>();
		HashSet<String> holdSet = new HashSet<String>();

		System.out.println("refine\t"+refineGenomeSet.size());
		
		for (int i = 0; i < genomeStrings.length; i++) {
			
			if(!refineGenomeSet.contains(genomeStrings[i]))
				continue;

			pepSet.addAll(copyMap.get(genomeStrings[i]));

			int addCount = pepSet.size() - currentTotalSize;
			currentTotalSize = pepSet.size();
			double percentage = (double) currentTotalSize / (double) totalPepSetSize;
			double addRatio = (double) addCount / (double) currentTotalSize;

			if (percentage < 0.2) {
				usedSet.add(genomeStrings[i]);
			} else if (percentage > 1) {
				if (addRatio > 0.005) {
					holdSet.add(genomeStrings[i]);
				}
			} else {
				if (addRatio > 0.005) {
					usedSet.add(genomeStrings[i]);
				} else {
					holdSet.add(genomeStrings[i]);
				}
			}
		}

		System.out.println(usedSet.size() + "\t" + holdSet.size());

		HashSet<String> step1Set = new HashSet<String>();
		for (int i = 0; i < genomeStrings.length; i++) {
			if (usedSet.contains(genomeStrings[i]) || holdSet.contains(genomeStrings[i]))
				step1Set.addAll(copyMap.get(genomeStrings[i]));
			
			if(usedSet.contains(genomeStrings[i])) {
				System.out.println(genomeStrings[i]);
			}
		}

		System.out.println(step1Set.size() + "\t" + (double) step1Set.size() / (double) totalPepSetSize);

	}

	protected static boolean exportPepProTxt(File magPepResultFile, File magProResultFile, File final_pep_txt,
			File final_pro_txt, String[] fileNames) {

		HashMap<String, HashSet<String>> razorProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> allProPepMap = new HashMap<String, HashSet<String>>();
		FragpipePepReader pepReader = new FragpipePepReader(magPepResultFile, fileNames);
		FragpipePeptide[] peps = pepReader.getMetaPeptides();
		HashMap<String, Integer> pepIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < peps.length; i++) {
			String seq = peps[i].getModSeq();
			pepIdMap.put(seq, i);
			String[] pros = peps[i].getProteins();
			if (razorProPepMap.containsKey(pros[0])) {
				razorProPepMap.get(pros[0]).add(seq);
				allProPepMap.get(pros[0]).add(seq);
			} else {
				HashSet<String> razorSet = new HashSet<String>();
				razorSet.add(seq);
				razorProPepMap.put(pros[0], razorSet);

				HashSet<String> allSet = new HashSet<String>();
				allSet.add(seq);
				allProPepMap.put(pros[0], allSet);
			}

			for (int j = 1; j < pros.length; j++) {
				if (allProPepMap.containsKey(pros[j])) {
					allProPepMap.get(pros[j]).add(seq);
				} else {
					HashSet<String> allSet = new HashSet<String>();
					allSet.add(seq);
					allProPepMap.put(pros[j], allSet);
				}
			}
		}

		FragpipeProReader proReader = new FragpipeProReader(magProResultFile, fileNames);
		MetaProtein[] pros = proReader.getMetaProteins();
		HashMap<String, Integer> proIdMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> proSameSetMap = new HashMap<String, HashSet<String>>();
		String leadProName = "";
		int groupId = -1;

		for (int i = 0; i < pros.length; i++) {
			if (pros[i].getGroupId() == groupId) {
				proSameSetMap.get(leadProName).add(pros[i].getName());
			} else {
				groupId = pros[i].getGroupId();
				leadProName = pros[i].getName();
				proSameSetMap.put(leadProName, new HashSet<String>());
			}
			proIdMap.put(pros[i].getName(), pros[i].getGroupId());
		}

		int[] counts = new int[2];
		for (String key : proSameSetMap.keySet()) {
			counts[0]++;
			counts[1] += proSameSetMap.get(key).size();
		}
		System.out.println(counts[0] + "\t" + counts[1] + "\t" + (counts[0] + counts[1]) + "\t" + pros.length + "\t"
				+ allProPepMap.size() + "\t" + razorProPepMap.size());
		PrintWriter pepWriter = null;
		try {
			pepWriter = new PrintWriter(final_pep_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Sequence").append("\t");
			titlesb.append("Base sequence").append("\t");
			titlesb.append("Length").append("\t");
			titlesb.append("Proteins").append("\t");
			titlesb.append("Charges").append("\t");
			titlesb.append("Score").append("\t");

			for (int i = 0; i < fileNames.length; i++) {
				titlesb.append("Identification type " + fileNames[i]).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < fileNames.length; i++) {
				titlesb.append("Intensity " + fileNames[i]).append("\t");
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			pepWriter.println(titlesb);

			for (int i = 0; i < peps.length; i++) {

				String sequence = peps[i].getSequence();
				StringBuilder sb = new StringBuilder();
				sb.append(peps[i].getModSeq()).append("\t");
				sb.append(sequence).append("\t");
				sb.append(peps[i].getLength()).append("\t");

				String[] proteins = peps[i].getProteins();
				if (proteins.length > 0) {
					for (String pro : proteins) {
						sb.append(pro).append(";");
					}
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append("\t");

				int[] charge = peps[i].getCharges();
				StringBuilder chargesb = new StringBuilder();
				for (int j = 0; j < charge.length; j++) {
					chargesb.append(charge[j]).append(";");
				}
				if (chargesb.length() > 1) {
					sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
				} else {
					sb.append("2").append("\t");
				}

				sb.append("1.0\t");

				int[] idenType = peps[i].getIdenType();

				for (int j = 0; j < idenType.length; j++) {
					sb.append(MetaPeptide.idenTypeStrings[idenType[j]]).append("\t");
				}

				double totalIntensity = 0;

				double[] intensity = peps[i].getIntensity();
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append((int) totalIntensity).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append((int) intensity[j]).append("\t");
				}

				sb.append("\t").append("\t");

				sb.append(i).append("\t");

				HashSet<Integer> proIdSet = new HashSet<Integer>();
				if (proteins.length > 0) {
					for (String pro : proteins) {
						if (proIdMap.containsKey(pro)) {
							proIdSet.add(proIdMap.get(pro));
						}
					}
				}
				if (proIdSet.size() > 0) {
					for (Integer proId : proIdSet) {
						sb.append(proId).append(";");
					}
					sb.deleteCharAt(sb.length() - 1);
				}

				sb.append("\t");

				int totalSpCount = 0;
				int[] spCount = peps[i].getMs2Counts();
				for (int j = 0; j < spCount.length; j++) {
					totalSpCount += spCount[j];
				}
				sb.append(totalSpCount);

				pepWriter.println(sb);
			}

			pepWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block

		}

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		}

		PrintWriter proWriter = null;
		try {
			proWriter = new PrintWriter(final_pro_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Protein IDs").append("\t");
			titlesb.append("Majority protein IDs").append("\t");
			titlesb.append("Peptide counts (all)").append("\t");
			titlesb.append("Peptide counts (razor)").append("\t");
			titlesb.append("Number of proteins").append("\t");
			titlesb.append("Peptides").append("\t");
			titlesb.append("Score").append("\t");
			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < fileNames.length; i++) {
				titlesb.append("Intensity " + fileNames[i]).append("\t");
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Peptide IDs").append("\t");
			titlesb.append("Peptide is razor");

			proWriter.println(titlesb);

			for (int i = 0; i < pros.length; i++) {
				String pro = pros[i].getName();
				if (proSameSetMap.containsKey(pro)) {

					HashSet<String> allPepSet = allProPepMap.get(pro);
					HashSet<String> razorPepSet = razorProPepMap.get(pro);

					if (allPepSet == null || razorPepSet == null) {
						continue;
					}

					HashSet<String> sameSet = proSameSetMap.get(pro);
					int proCount = 1 + sameSet.size();
					int[] pepCountAll = new int[proCount];
					int[] pepCountRazor = new int[proCount];

					pepCountAll[0] = allProPepMap.get(pro).size();
					pepCountRazor[0] = razorProPepMap.get(pro).size();

					String[] samePros = sameSet.toArray(new String[sameSet.size()]);
					for (int j = 0; j < samePros.length; j++) {
						if (razorProPepMap.containsKey(samePros[j])) {
							pepCountRazor[j + 1] = razorProPepMap.get(samePros[j]).size();
						} else {
							pepCountRazor[j + 1] = 0;
						}
						if (allProPepMap.containsKey(samePros[j])) {
							pepCountAll[j + 1] = allProPepMap.get(samePros[j]).size();
						} else {
							pepCountAll[j + 1] = 0;
						}
					}
					StringBuilder sameSb = new StringBuilder();
					StringBuilder pepCountAllSb = new StringBuilder();
					StringBuilder pepCountRazorSb = new StringBuilder();

					sameSb.append(pro);
					pepCountAllSb.append(pepCountAll[0]);
					pepCountRazorSb.append(pepCountRazor[0]);
					int usedProCount = 1;

					for (int j = 0; j < samePros.length; j++) {
						if (pepCountAll[j + 1] > 0) {
							sameSb.append(";").append(samePros[j]);
							pepCountAllSb.append(";").append(pepCountAll[j + 1]);
							pepCountRazorSb.append(";").append(pepCountRazor[j + 1]);
							usedProCount++;
						}
					}

					StringBuilder sb = new StringBuilder();
					sb.append(sameSb).append("\t");
					sb.append(pro).append("\t");
					sb.append(pepCountAllSb).append("\t");
					sb.append(pepCountRazorSb).append("\t");
					sb.append(usedProCount).append("\t");
					sb.append(allPepSet.size()).append("\t");
					sb.append(pros[i].getScore()).append("\t");

					double totalIntensity = 0;
					double[] intensity = pros[i].getIntensities();
					for (int j = 0; j < intensity.length; j++) {
						totalIntensity += intensity[j];
					}

					sb.append(totalIntensity).append("\t");
					for (int j = 0; j < intensity.length; j++) {
						sb.append(intensity[j]).append("\t");
					}
					sb.append("\t").append("\t");
					sb.append(pros[i].getGroupId()).append("\t");

					StringBuilder pepIdSb = new StringBuilder();
					StringBuilder pepRazorSb = new StringBuilder();

					String[] allPeps = allPepSet.toArray(new String[allPepSet.size()]);
					Arrays.sort(allPeps, new Comparator<String>() {

						@Override
						public int compare(String o1, String o2) {
							// TODO Auto-generated method stub

							return pepIdMap.get(o1) - pepIdMap.get(o2);
						}
					});
					for (String pep : allPeps) {
						pepIdSb.append(pepIdMap.get(pep)).append(";");
						if (razorPepSet.contains(pep)) {
							pepRazorSb.append("true;");
						} else {
							pepRazorSb.append("false;");
						}
					}
					sb.append(pepIdSb).append("\t");
					sb.append(pepRazorSb);

					proWriter.println(sb);
				}
			}

			proWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		}
		return true;
	}
	
	@SuppressWarnings("unused")
	private static void getProCount(String in) {
		HashSet<String> genomeSet = new HashSet<String>();
		HashSet<String> proSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			while((line=reader.readLine())!=null) {
				String[] cs = line.split("\t");
				if(cs[0].startsWith("MGYG")) {
					String genome = cs[0].substring(0, cs[0].indexOf("_"));
					genomeSet.add(genome);
					proSet.add(cs[0]);
				}
			}
			reader.close();
		} catch (IOException e) {

		}
		System.out.println(genomeSet.size()+"\t"+proSet.size());
	}

}
