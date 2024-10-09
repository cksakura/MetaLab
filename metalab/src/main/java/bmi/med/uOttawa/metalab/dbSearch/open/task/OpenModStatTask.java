/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.mod.UmModification;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlReader;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlWriter;

/**
 * @author Kai Cheng
 *
 */
public class OpenModStatTask {

	private static String taskName = "Open mod analysis";
	private static final Logger LOGGER = LogManager.getLogger(OpenModStatTask.class);

	private File result;
	private File psms_file;
	private File quan_mods_file;
	private File quan_peps_file;
	private File quan_peaks_file;

	private File database;
	private File aa_around_file;

	private File final_mods_file;

	private HashMap<String, HashMap<String, String[]>> aaWindowMap;

	private OpenMod[] openMods;
	private HashMap<Double, PostTransModification> setModMap;

	private HashMap<Integer, double[][]> openModMatrixMap;
	private HashMap<Double, double[][]> setModMatrixMap;

	private static String rev = "REV_";
	private static int window = 7;

	public OpenModStatTask(String result, String database) {
		this(new File(result), new File(database));
	}

	public OpenModStatTask(File result, File database) {
		this.result = result;
		this.psms_file = new File(this.result, "quant_psms.tsv");
		this.quan_mods_file = new File(this.result, "quant_mods.xml");
		this.quan_peps_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedBaseSequences.tsv");
		this.quan_peaks_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedPeaks.tsv");

		this.final_mods_file = new File(this.result, "final_mods.xml");

		this.database = database;
		this.aa_around_file = new File(this.result, "aa_around.tsv");
	}

	private HashMap<String, Double> parseIntensityMap() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(quan_peaks_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int fileId = -1;
		int seqId = -1;
		int modSeqId = -1;
		int proId = -1;
		int intenId = -1;
		String line = null;
		try {
			line = reader.readLine();
			String[] cs = line.split("\t");
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].equals("File Name")) {
					fileId = i;
				} else if (cs[i].equals("Peak intensity")) {
					intenId = i;
				} else if (cs[i].equals("Full Sequence")) {
					modSeqId = i;
				} else if (cs[i].equals("Base Sequence")) {
					seqId = i;
				} else if (cs[i].equals("Protein Group")) {
					proId = i;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<String, Double> intenMap = new HashMap<String, Double>();
		this.aaWindowMap = new HashMap<String, HashMap<String, String[]>>();
		HashMap<String, String> proNameMap = new HashMap<String, String>();

		if (fileId == -1 || seqId == -1 || modSeqId == -1 || intenId == -1 || proId == -1) {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return intenMap;
		}

		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				String key = cs[fileId] + "_" + cs[modSeqId];
				double value = Double.parseDouble(cs[intenId]);
				if (intenMap.containsKey(key)) {
					intenMap.put(key, intenMap.get(key) + value);
				} else {
					intenMap.put(key, value);
				}

				String prokey = cs[proId].split("\\s+")[0];

				HashMap<String, String[]> m = aaWindowMap.get(prokey);
				if (m == null) {
					m = new HashMap<String, String[]>();
				}
				m.put(cs[seqId], new String[] { "", "" });
				aaWindowMap.put(prokey, m);
				proNameMap.put(prokey, cs[proId]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FastaReader fr = null;
		try {
			fr = new FastaReader(database);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading protein sequences from " + this.database, e);
		}

		ProteinItem[] items = fr.getTargetItems(rev);
		for (int i = 0; i < items.length; i++) {

			String ref = items[i].getRef();
			String refkey = ref.split("\\s+")[0];

			if (aaWindowMap.containsKey(refkey)) {

				HashMap<String, String[]> m = aaWindowMap.get(refkey);
				for (String sequence : m.keySet()) {
					int id = items[i].indexOf(sequence);
					if (id >= 0) {
						int pre_loc = id - window;
						String pre_seq = "";
						if (pre_loc < 0) {
							for (int j = pre_loc; j < 0; j++) {
								pre_seq += '_';
							}
							pre_seq += items[i].getPeptide(0, id);
						} else {
							pre_seq += items[i].getPeptide(pre_loc, id);
						}

						int nex_loc = id + window;
						String nex_seq = "";
						if (nex_loc >= items[i].length()) {
							nex_seq = items[i].getPeptide(id + 1, items[i].length());
							for (int j = items[i].length(); j <= nex_loc; j++) {
								nex_seq += '_';
							}
						} else {
							nex_seq = items[i].getPeptide(id + 1, nex_loc + 1);
						}

						m.put(sequence, new String[] { pre_seq, nex_seq });
					}
				}
			}
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(aa_around_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String title = "Protein\tPeptide\tPrevious AAs\tNext AAs";
		writer.println(title);

		for (String prokey : aaWindowMap.keySet()) {
			HashMap<String, String[]> m = aaWindowMap.get(prokey);
			for (String pep : m.keySet()) {
				String[] seq = m.get(pep);
				writer.println(proNameMap.get(prokey) + "\t" + pep + "\t" + seq[0] + "\t" + seq[1]);
			}
		}
		writer.close();

		return intenMap;
	}

	private void parseMods() {
		OpenModXmlReader mreader = null;
		try {
			mreader = new OpenModXmlReader(this.quan_mods_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.openMods = mreader.getOpenMods();
		this.openModMatrixMap = new HashMap<Integer, double[][]>();

		for (OpenMod om : openMods) {
			int id = om.getId();
			if (id != 0) {
				double[][] matrix = new double[26][15];
				this.openModMatrixMap.put(id, matrix);
			}
		}

		this.setModMap = mreader.getSetModMap();
		this.setModMatrixMap = new HashMap<Double, double[][]>();

		for (Double key : setModMap.keySet()) {
			double[][] matrix = new double[26][15];
			this.setModMatrixMap.put(key, matrix);
		}
	}

	private void match(HashMap<String, Double> intenMap) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(psms_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int fileId = -1;
		int seqId = -1;
		int modId = -1;
		int locId = -1;
		int proId = -1;

		String line = null;
		try {
			line = reader.readLine();
			String[] cs = line.split("\t");
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].equals("Raw file")) {
					fileId = i;
				} else if (cs[i].equals("Modified sequence")) {
					seqId = i;
				} else if (cs[i].equals("mod_id")) {
					modId = i;
				} else if (cs[i].equals("possible_loc")) {
					locId = i;
				} else if (cs[i].equals("Proteins")) {
					proId = i;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fileId == -1 || seqId == -1 || modId == -1 || locId == -1 || proId == -1) {
			return;
		}

		HashMap<Integer, HashSet<String>> siteMap = new HashMap<Integer, HashSet<String>>();
		for (OpenMod om : openMods) {
			UmModification[] ums = om.getUmmod();
			if (ums != null) {
				HashSet<String> set = new HashSet<String>();
				for (UmModification um : ums) {
					String[] sites = um.getSpecificity();
					for (String site : sites) {
						set.add(site);
					}
				}
				siteMap.put(om.getId(), set);
			}
		}

		HashMap<Integer, double[]> caomap = new HashMap<Integer, double[]>();

		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				String fileName = cs[fileId];
				String modseq = cs[seqId];
				String key = fileName + "_" + modseq;

				double intensity = 0;
				if (intenMap.containsKey(key)) {
					intensity = intenMap.get(key);
				} else {
					continue;
				}

				String prokey = cs[proId].split("\\s+")[0];
				HashMap<String, String[]> aroundMap = this.aaWindowMap.get(prokey);
				if (aroundMap == null) {
					continue;
				}

				ArrayList<Double> setModMassList = new ArrayList<Double>();
				ArrayList<Integer> setModLocList = new ArrayList<Integer>();
				boolean isAA = true;
				String seqsb = "";
				String modsb = "";
				for (int i = 0; i < modseq.length(); i++) {
					char aa = modseq.charAt(i);
					if (aa == '(') {
						isAA = false;
						modsb = "";
					} else if (aa == ')') {
						isAA = true;

						if (modsb.toString().equals(cs[modId])) {

						} else {
							double modmass = Double.parseDouble(modsb);
							if (this.setModMap.containsKey(modmass)) {
								setModMassList.add(modmass);
								setModLocList.add(seqsb.length());
							}
						}

					} else {
						if (isAA) {
							seqsb += aa;
						} else {
							modsb += aa;
						}
					}
				}

				String[] around = aroundMap.get(seqsb);
				if (around == null) {
					continue;
				}

				String fullSequence = around[0] + seqsb + around[1];
				int modIden = Integer.parseInt(cs[modId]);
				if (modIden != 0) {
					String[] loccs = cs[locId].split("_");
					ArrayList<Integer> realLoc = new ArrayList<Integer>();

					for (int i = 0; i < loccs.length; i++) {
						int loc = Integer.parseInt(loccs[i]);

						if (siteMap.containsKey(modIden)) {
							HashSet<String> sites = siteMap.get(modIden);
							char aa = seqsb.charAt(loc);
							if (!sites.contains(String.valueOf(aa))) {
								if (loc == 0) {
									if (!sites.contains("[" + aa) && !sites.contains("n" + aa) && !sites.contains("[*")
											&& !sites.contains("n*")) {
										continue;
									}
								} else if (loc == seqsb.length() - 1) {
									if (!sites.contains("]" + aa) && !sites.contains("c" + aa) && !sites.contains("]*")
											&& !sites.contains("c*")) {
										continue;
									}
								} else {
									continue;
								}
							}
						}
						realLoc.add(loc);
					}

					double[] cao = caomap.get(modIden);
					if (cao == null) {
						cao = new double[4];
					}
					cao[1]++;
					cao[3] += intensity;

					if (realLoc.size() > 0) {
						double[][] matrix = this.openModMatrixMap.get(modIden);
						double inteni = intensity / (double) realLoc.size();
						for (int i = 0; i < realLoc.size(); i++) {
							int loc = realLoc.get(i);

							String sub = fullSequence.substring(loc, loc + 15);

							for (int j = 0; j < sub.length(); j++) {
								char aa = sub.charAt(j);
								if (aa >= 'A' && aa <= 'Z') {
									matrix[aa - 'A'][j] += inteni;
								}
							}
						}

						this.openModMatrixMap.put(modIden, matrix);

						cao[0]++;
						cao[2] += intensity;
					}
					caomap.put(modIden, cao);
				}

				for (int i = 0; i < setModMassList.size(); i++) {
					double modmass = setModMassList.get(i);
					int loc = setModLocList.get(i);
					double[][] matrix = this.setModMatrixMap.get(modmass);

					String sub = null;
					if (loc == 0) {
						sub = fullSequence.substring(loc, loc + 15);
					} else {
						sub = fullSequence.substring(loc - 1, loc + 14);
					}

					for (int j = 0; j < sub.length(); j++) {
						char aa = sub.charAt(j);
						if (aa >= 'A' && aa <= 'Z') {
							matrix[aa - 'A'][j] += intensity;
						}
					}
					this.setModMatrixMap.put(modmass, matrix);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Integer key : caomap.keySet()) {
			double[] cao = caomap.get(key);
			System.out.println(key + "\t" + cao[0] + "\t" + cao[1] + "\t" + cao[2] + "\t" + cao[3] + "\t"
					+ (cao[0] / cao[1]) + "\t" + (cao[2] / cao[3]));
		}
	}

	private void writeModInfo() {
		for (Integer key : this.openModMatrixMap.keySet()) {
			double[][] matrix = this.openModMatrixMap.get(key);
			double[] total = new double[window * 2 + 1];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					total[j] += matrix[i][j];
				}
			}
			double[][] norMatrix = new double[matrix.length][total.length];
			for (int i = 0; i < norMatrix.length; i++) {
				for (int j = 0; j < norMatrix[i].length; j++) {
					if (total[j] > 0) {
						norMatrix[i][j] = matrix[i][j] / total[j];
					}
				}
			}
			this.openModMatrixMap.put(key, norMatrix);
		}

		for (Double key : this.setModMatrixMap.keySet()) {
			double[][] matrix = this.setModMatrixMap.get(key);
			double[] total = new double[window * 2 + 1];
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					total[j] += matrix[i][j];
				}
			}
			double[][] norMatrix = new double[matrix.length][total.length];
			for (int i = 0; i < norMatrix.length; i++) {
				for (int j = 0; j < norMatrix[i].length; j++) {
					if (total[j] > 0) {
						norMatrix[i][j] = matrix[i][j] / total[j];
					}
				}
			}
			this.setModMatrixMap.put(key, norMatrix);
		}

		OpenModXmlWriter writer = null;
		try {
			writer = new OpenModXmlWriter(this.final_mods_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writer.addMods(openMods, openModMatrixMap);

		writer.addSetMods(setModMap, openMods[0], setModMatrixMap);

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		parseMods();

		HashMap<String, Double> intenMap = parseIntensityMap();

		match(intenMap);

		writeModInfo();
	}
}
