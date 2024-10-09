/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.pia.InferedPeptide;
import bmi.med.uOttawa.metalab.dbSearch.pia.InferedProtein;
import bmi.med.uOttawa.metalab.dbSearch.pia.ProteinInferenceReader;
import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;

/**
 * @author Kai Cheng
 *
 */
public class OpenPsmTsvReader {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenPsmTsvReader.class);
	
	private OpenPSM[] openPsms;
	
	private String rev = "rev";
	
	public OpenPsmTsvReader(String file) throws IOException {
		this(new File(file));
	}

	public OpenPsmTsvReader(File file) throws IOException {
		this.read(file);
	}
	
	private PeptideModification[] parseMod(String modSequence, int modid) {
		String sequence = new String(modSequence);
		if (modid > 0) {
			sequence = modSequence.substring(0, modSequence.lastIndexOf("("));
		}

		ArrayList<PeptideModification> mlist = new ArrayList<PeptideModification>();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		boolean modBegin = false;
		for (int i = 0; i < sequence.length(); i++) {
			char ci = sequence.charAt(i);
			if (ci == '(') {
				sb2 = new StringBuilder();
				modBegin = true;
			} else if (ci == ')') {
				double mass = Double.parseDouble(sb2.toString());
				PostTransModification ptm = new PostTransModification(mass, "", "", true);
				PeptideModification pm = new PeptideModification(ptm, sb1.length());
				mlist.add(pm);
				modBegin = false;
			} else {
				if (modBegin) {
					sb2.append(ci);
				} else {
					sb1.append(ci);
				}
			}
		}
		if (mlist.size() == 0) {
			return null;
		} else {
			PeptideModification[] pms = mlist.toArray(new PeptideModification[mlist.size()]);
			return pms;
		}
	}

	private void read(File file) {
		String[] title = OpenPsmTsvWriter.title;
		ArrayList<OpenPSM> list = new ArrayList<OpenPSM>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading psms from " + file, e);
		}
		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading psms from " + file, e);
		}
		HashMap<String, Integer> titlemap = new HashMap<String, Integer>();
		String[] titleNames = line.split("\t");
		int type = -1;
		if (titleNames.length == OpenPsmTsvWriter.getTitle(OpenPsmTsvWriter.full_information).length) {
			type = OpenPsmTsvWriter.full_information;
		} else if (titleNames.length == OpenPsmTsvWriter.getTitle(OpenPsmTsvWriter.simple_information).length) {
			type = OpenPsmTsvWriter.simple_information;
		} else {
			try {
				IOException e = new IOException("Error in parsing title from " + file);
				LOGGER.error("Error in reading psms from " + file, e);
				throw e;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading psms from " + file, e);
			}
		}

		for (int i = 0; i < titleNames.length; i++) {
			for (int j = 0; j < title.length; j++) {
				if (titleNames[i].equals(title[j])) {
					titlemap.put(title[j], i);
				}
			}
		}

		int lineNum = 1;

		try {

			while ((line = reader.readLine()) != null) {
				lineNum++;

				String[] cs = line.split("\t");

				if (type == OpenPsmTsvWriter.full_information) {

					String fileName = cs[titlemap.get(title[0])];
					int scan = Integer.parseInt(cs[titlemap.get(title[7])]);
					int charge = Integer.parseInt(cs[titlemap.get(title[5])]);
					double precursorMr = Double.parseDouble(cs[titlemap.get(title[3])]);
					double rt = Double.parseDouble(cs[titlemap.get(title[4])]);

					String protein = cs[titlemap.get(title[6])];

					String sequence = cs[titlemap.get(title[1])];
					double pepMass = Double.parseDouble(cs[titlemap.get(title[8])]);
					double massDiff = Double.parseDouble(cs[titlemap.get(title[9])]);
					int miss = Integer.parseInt(cs[titlemap.get(title[10])]);

					double hyperscore = Double.parseDouble(cs[titlemap.get(title[11])]);
					double nextscore = Double.parseDouble(cs[titlemap.get(title[12])]);
					double expect = Double.parseDouble(cs[titlemap.get(title[13])]);

					int num_tol_term = Integer.parseInt(cs[titlemap.get(title[14])]);
					int tot_num_ions = Integer.parseInt(cs[titlemap.get(title[15])]);
					int num_matched_ions = Integer.parseInt(cs[titlemap.get(title[16])]);

					double open_mod_mass = Double.parseDouble(cs[titlemap.get(title[17])]);
					double delta_open_mod_mass = Double.parseDouble(cs[titlemap.get(title[18])]);

					double b_intensity = Double.parseDouble(cs[titlemap.get(title[19])]);
					double y_intensity = Double.parseDouble(cs[titlemap.get(title[20])]);
					double b_rankscore = Double.parseDouble(cs[titlemap.get(title[21])]);
					double y_rankscore = Double.parseDouble(cs[titlemap.get(title[22])]);
					int b_ion_count = Integer.parseInt(cs[titlemap.get(title[23])]);
					int y_ion_count = Integer.parseInt(cs[titlemap.get(title[24])]);
					double fragment_delta_mass = Double.parseDouble(cs[titlemap.get(title[25])]);

					double b_mod_intensity = Double.parseDouble(cs[titlemap.get(title[26])]);
					double y_mod_intensity = Double.parseDouble(cs[titlemap.get(title[27])]);
					double b_mod_rankscore = Double.parseDouble(cs[titlemap.get(title[28])]);
					double y_mod_rankscore = Double.parseDouble(cs[titlemap.get(title[29])]);
					double mod_fragment_delta_mass = Double.parseDouble(cs[titlemap.get(title[30])]);

					double neutralLossMass = Double.parseDouble(cs[titlemap.get(title[31])]);
					double nlPeakRankScore = Double.parseDouble(cs[titlemap.get(title[32])]);

					int mod_id = Integer.parseInt(cs[titlemap.get(title[33])]);
					int[] possible_loc = null;
					if (mod_id > 0) {
						String[] locstr = cs[titlemap.get(title[34])].split("_");
						possible_loc = new int[locstr.length];
						for (int i = 0; i < possible_loc.length; i++) {
							possible_loc[i] = Integer.parseInt(locstr[i]);
						}
					}

					boolean is_target = !protein.startsWith(rev);
					double classScore = Double.parseDouble(cs[titlemap.get(title[35])]);
					double q_value = Double.parseDouble(cs[titlemap.get(title[36])]);

					OpenPSM openPsm = new OpenPSM(fileName, scan, charge, precursorMr, rt, sequence, pepMass, massDiff,
							miss, num_tol_term, tot_num_ions, num_matched_ions, protein, hyperscore, nextscore, expect,
							open_mod_mass, delta_open_mod_mass, b_intensity, y_intensity, b_rankscore, y_rankscore,
							b_ion_count, y_ion_count, fragment_delta_mass, b_mod_intensity, y_mod_intensity,
							b_mod_rankscore, y_mod_rankscore, mod_fragment_delta_mass, mod_id, possible_loc,
							neutralLossMass, nlPeakRankScore, is_target, classScore);

					openPsm.setQValue(q_value);
					openPsm.setPTMs(parseMod(cs[titlemap.get(title[2])], mod_id));

					list.add(openPsm);

				} else if (type == OpenPsmTsvWriter.simple_information) {

					String fileName = cs[titlemap.get(title[0])];
					int scan = Integer.parseInt(cs[titlemap.get(title[7])]);
					int charge = Integer.parseInt(cs[titlemap.get(title[5])]);
					double precursorMr = Double.parseDouble(cs[titlemap.get(title[3])]);
					double rt = Double.parseDouble(cs[titlemap.get(title[4])]);

					String sequence = cs[titlemap.get(title[1])];
					double pepMass = Double.parseDouble(cs[titlemap.get(title[8])]);
					double massDiff = Double.parseDouble(cs[titlemap.get(title[9])]);
					int miss = Integer.parseInt(cs[titlemap.get(title[10])]);
					String protein = cs[titlemap.get(title[6])];

					double hyperscore = Double.parseDouble(cs[titlemap.get(title[11])]);
					double nextscore = Double.parseDouble(cs[titlemap.get(title[12])]);
					double expect = Double.parseDouble(cs[titlemap.get(title[13])]);

					int mod_id = Integer.parseInt(cs[titlemap.get(title[33])]);
					int[] possible_loc = null;
					if (mod_id > 0) {
						String[] locstr = cs[titlemap.get(title[34])].split("_");
						possible_loc = new int[locstr.length];
						for (int i = 0; i < possible_loc.length; i++) {
							possible_loc[i] = Integer.parseInt(locstr[i]);
						}
					}

					double classScore = Double.parseDouble(cs[titlemap.get(title[35])]);
					double q_value = Double.parseDouble(cs[titlemap.get(title[36])]);

					OpenPSM openPsm = new OpenPSM(fileName, scan, charge, precursorMr, rt, sequence, pepMass, massDiff,
							miss, protein, hyperscore, nextscore, expect, mod_id, possible_loc, classScore);
					openPsm.setQValue(q_value);
					openPsm.setPTMs(parseMod(cs[titlemap.get(title[2])], mod_id));

					list.add(openPsm);
				}
			}
			reader.close();

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading psms from " + file + " in line " + lineNum, e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading psms from " + file + " in line " + lineNum, e);
		}
		this.openPsms = list.toArray(new OpenPSM[list.size()]);
	}
	
	public OpenPSM[] getAllOpenPSMs() {
		return openPsms;
	}
	
	public OpenPSM[] getFilteredPSMs() {
		
		Arrays.sort(openPsms, new OpenPSM.InverseClassScoreComparator());
		int t = 0;
		int d = 0;
		for (int i = 0; i < openPsms.length; i++) {
			if (openPsms[i].isTarget()) {
				t++;
			} else {
				d++;
			}
			double fdr = (double) d / (double) t;
			openPsms[i].setQValue(fdr);
		}

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < openPsms.length; i++) {
			int modid = openPsms[i].getOpenModId();
			double classScore = openPsms[i].getQValue();
			boolean isTarget = openPsms[i].isTarget();
			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}
		}

		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int modcount = 0;
		int[] useModIds = new int[modmap.size()];
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		for (int i = 0; i < modkey.length; i++) {
			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.01)) {
						pass = true;
					}
				} else {
					pass = true;
				}
			}

			if (pass) {
				modcount++;
				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = openPsms[psmIdList.get(j)];
				}

				Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
				int target = 0;
				int decoy = 0;

				for (int j = 0; j < psms.length; j++) {
					if (psms[j].isTarget()) {
						target++;
					} else {
						decoy++;
					}
					double locfdr = (double) decoy / (double) target;
					if (target != 0 && locfdr < 0.01) {
						filteredList.add(psms[j]);
						if (psms[j].isTarget()) {
							totaltarget++;
						} else {
							totaldecoy++;
						}
					} else {
						break;
					}
				}
			}
		}

		OpenPSM[] psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		
		System.out.println(modmap.size()+"\t"+modcount);
		System.out.println(openPsms.length+"\t"+psms.length);
		System.out.println(totaltarget+"\t"+totaldecoy);
		
		return psms;
	}
	
	public OpenPSM[] getFilteredPSMs(InferedProtein[] proteins) {
		
		HashSet<String> proset = new HashSet<String>();
		for (InferedProtein ip : proteins) {
			proset.add(ip.getName());
		}
		
		Arrays.sort(openPsms, new OpenPSM.InverseClassScoreComparator());
		int t = 0;
		int d = 0;
		for (int i = 0; i < openPsms.length; i++) {
			if (openPsms[i].isTarget()) {
				t++;
			} else {
				d++;
			}
			double fdr = (double) d / (double) t;
			openPsms[i].setQValue(fdr);
			// if(fdr>0.01){
			// System.out.println(t+"\t"+d+"\t"+fdr);
			// break;
			// }
		}

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < openPsms.length; i++) {
			int modid = openPsms[i].getOpenModId();
//			double classScore = openPsms[i].getClassScore();
//			double classScore = openPsms[i].getHyperscore();
			double classScore = openPsms[i].getQValue();
			boolean isTarget = openPsms[i].isTarget();
			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}
		}

		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int modcount = 0;
		int[] useModIds = new int[modmap.size()];
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		for (int i = 0; i < modkey.length; i++) {
			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.01)) {
						pass = true;
					}
				} else {
					pass = true;
				}
			}

			if (pass) {
				modcount++;
				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = openPsms[psmIdList.get(j)];
				}

				Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
				int target = 0;
				int decoy = 0;

				for (int j = 0; j < psms.length; j++) {
					if (psms[j].isTarget()) {
						target++;
					} else {
						decoy++;
					}
					double locfdr = (double) decoy / (double) target;
					if (target != 0 && locfdr < 0.01) {
						filteredList.add(psms[j]);
						if (psms[j].isTarget()) {
							totaltarget++;
						} else {
							totaldecoy++;
						}
					} else {
						break;
					}
				}
			}
		}

		OpenPSM[] psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		
		System.out.println(modmap.size()+"\t"+modcount);
		System.out.println(openPsms.length+"\t"+psms.length);
		
		int ft = 0;
		int fd = 0;
		for(OpenPSM op : psms){
			String pro = op.getProtein();
			if(proset.contains(pro)){
				if(op.isTarget()){
					ft++;
				}else{
					fd++;
				}
			}
		}
		System.out.println("final\t"+ft+"\t"+fd);
		return psms;
	}

	public OpenPSM[] getFilteredPSMsByProtein(InferedProtein[] proteins) {
		
		HashSet<String> proset = new HashSet<String>();
		for (InferedProtein ip : proteins) {
			proset.add(ip.getName());
		}
		
		Arrays.sort(openPsms, new OpenPSM.InverseClassScoreComparator());
		int t = 0;
		int d = 0;
		for (int i = 0; i < openPsms.length; i++) {
			if (openPsms[i].isTarget()) {
				t++;
			} else {
				d++;
			}
			double fdr = (double) d / (double) t;
			openPsms[i].setQValue(fdr);
			// if(fdr>0.01){
			// System.out.println(t+"\t"+d+"\t"+fdr);
			// break;
			// }
		}

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < openPsms.length; i++) {
			int modid = openPsms[i].getOpenModId();
//			double classScore = openPsms[i].getClassScore();
//			double classScore = openPsms[i].getHyperscore();
			double classScore = openPsms[i].getQValue();
			boolean isTarget = openPsms[i].isTarget();
			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}
		}

		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int modcount = 0;
		int[] useModIds = new int[modmap.size()];
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		for (int i = 0; i < modkey.length; i++) {
			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.01)) {
						pass = true;
					}
				} else {
					pass = true;
				}
			}

			if (pass) {
				modcount++;
				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = openPsms[psmIdList.get(j)];
				}

				Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
				int target = 0;
				int decoy = 0;

				for (int j = 0; j < psms.length; j++) {
					if (psms[j].isTarget()) {
						target++;
					} else {
						decoy++;
					}
					double locfdr = (double) decoy / (double) target;
					if (target != 0 && locfdr < 0.01) {
						filteredList.add(psms[j]);
						if (psms[j].isTarget()) {
							totaltarget++;
						} else {
							totaldecoy++;
						}
					} else {
						break;
					}
				}
			}
		}

		OpenPSM[] psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		
		System.out.println(modmap.size()+"\t"+modcount);
		System.out.println(openPsms.length+"\t"+psms.length);
		
		int ft = 0;
		int fd = 0;
		for(OpenPSM op : psms){
			String pro = op.getProtein();
			if(proset.contains(pro)){
				if(op.isTarget()){
					ft++;
				}else{
					fd++;
				}
			}
		}
		System.out.println("final\t"+ft+"\t"+fd);
		return psms;
	}

	private static void testAll(String in, String mod) throws IOException, DocumentException {

		OpenModXmlReader modreader = new OpenModXmlReader(mod);
		OpenMod[] mods = modreader.getOpenMods();

		int certainCount = 0;
		HashMap<Integer, int[]> map = new HashMap<Integer, int[]>();
		OpenPsmTsvReader reader = new OpenPsmTsvReader(in);
		OpenPSM[] openPsms = reader.getAllOpenPSMs();
		for (int i = 0; i < openPsms.length; i++) {
			int modid = openPsms[i].getOpenModId();
			String pro = openPsms[i].getProtein();
			int[] count = null;
			if (map.containsKey(modid)) {
				count = map.get(modid);
			} else {
				count = new int[2];
			}

			double massdiff = openPsms[i].getMassDiff();
			if (Math.abs(massdiff) < 0.02) {
				count[0]++;
			}
			if (Math.abs(massdiff) < 0.002) {
				count[1]++;
			}
			map.put(modid, count);

			if (massdiff > -0.0022 && massdiff < 0.0018) {
				certainCount++;
				if(modid!=0) {
					System.out.println("caocaocao\t"+modid);
				}
			}
		}
		Integer[] keys = map.keySet().toArray(new Integer[map.size()]);
		Arrays.sort(keys);
		System.out.println("certain\t"+certainCount);
		
		for (Integer id : keys) {
			System.out.println(id + "\t" + mods[id].getExpModMass() + "\t" + map.get(id)[0] + "\t" + map.get(id)[1]);
		}
	}

	private static void modtest(String psmin, String mod) throws IOException, DocumentException {
		OpenPsmTsvReader reader = new OpenPsmTsvReader(psmin);
		OpenPSM[] psms = reader.getAllOpenPSMs();
		HashSet<Integer> modset = new HashSet<Integer>();
		for (OpenPSM psm : psms) {
			modset.add(psm.getOpenModId());
		}
		System.out.println(modset.size());

		int find = 0;
		int notfind = 0;
		OpenModXmlReader modreader = new OpenModXmlReader(mod);
		OpenMod[] mods = modreader.getOpenMods();
		for (OpenMod m : mods) {
			int id = m.getId();
			if (!modset.contains(id)) {
				continue;
			}
			if (id != 0) {
				if (m.getUmmod() != null) {
					find++;
				} else {
					notfind++;
				}
			}
		}

		System.out.println(find + "\t" + notfind);
	}
	
	private static void pepProTaxTest(String psmin, String pro_taxid, String tax) throws IOException {
		HashMap<String, Boolean> promodmap = new HashMap<String, Boolean>();
		OpenPsmTsvReader psmreader = new OpenPsmTsvReader(psmin);
		OpenPSM[] psms = psmreader.getAllOpenPSMs();
		for (OpenPSM op : psms) {
			String proname = op.getProtein();
			int loc = proname.indexOf("[");
			if (loc > 1) {
				proname = proname.substring(0, loc - 2);
			}
			if (!promodmap.containsKey(proname)) {
				promodmap.put(proname, false);
			}
			if (op.getOpenModId() != 0) {
				promodmap.put(proname, true);
			}
		}
		System.out.println(promodmap.size());
		
		HashMap<String, Boolean> taxmodmap = new HashMap<String, Boolean>();
		BufferedReader proidreader = new BufferedReader(new FileReader(pro_taxid));
		String proidline = null;
		while ((proidline = proidreader.readLine()) != null) {
			String[] cs = proidline.split("\t");
			if(!promodmap.containsKey(cs[0])){
				continue;
			}
			if (!taxmodmap.containsKey(cs[1])) {
				taxmodmap.put(cs[1], false);
			}
			if (promodmap.get(cs[0])) {
				taxmodmap.put(cs[1], true);
			}
		}
		proidreader.close();

		int mod = 0;
		int unmod = 0;
		BufferedReader taxreader = new BufferedReader(new FileReader(tax));
		String taxline = null;
		while ((taxline = taxreader.readLine()) != null) {
			String[] cs = taxline.split("\t");
			if (cs[1].equals("Species")) {
				if (taxmodmap.get(cs[0])) {
					mod++;
				} else {
					unmod++;
				}
			}
		}
		taxreader.close();
		System.out.println(mod + "\t" + unmod);
	}
	
	private static void pep2taxTest(String psmin, String out) throws IOException {

		HashMap<String, Boolean> pepmodmap = new HashMap<String, Boolean>();
		OpenPsmTsvReader psmreader = new OpenPsmTsvReader(psmin);
		OpenPSM[] psms = psmreader.getAllOpenPSMs();
		for (OpenPSM op : psms) {
			String seq = op.getSequence();
			if (!pepmodmap.containsKey(seq)) {
				pepmodmap.put(seq, false);
			}
			if (op.getOpenModId() != 0) {
				pepmodmap.put(seq, true);
			}
		}
		System.out.println(pepmodmap.size());

		String pep2Taxafile = "Resources//db//pep2taxa.tab";

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		Pep2TaxaDatabase pep2Taxa = new Pep2TaxaDatabase(pep2Taxafile);
		HashSet<String> pepset = new HashSet<String>();
		pepset.addAll(pepmodmap.keySet());
		HashMap<String, Integer> map = pep2Taxa.pept2Lca(pepset, TaxonomyRanks.Family, rootSet, exclude);

		PrintWriter writer = new PrintWriter(out);
		String[] keys = map.keySet().toArray(new String[map.size()]);
		Arrays.sort(keys);

		int mod = 0;
		int unmod = 0;
		TaxonomyDatabase td = new TaxonomyDatabase();
		for (String key : keys) {
			Integer id = map.get(key);
			Taxon taxon = td.getTaxonFromId(id);
			if(taxon==null){
				continue;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(key).append("\t");
			sb.append(pepmodmap.get(key)).append("\t");
			sb.append(taxon.getRank()).append("\t");
			sb.append(taxon.getName()).append("\t");
			String[] lineage = td.getTaxonArrays(id);
			for (int i = 0; i < lineage.length; i++) {
				sb.append(lineage[i]).append("\t");
			}
			writer.println(sb);

			if (taxon.getRank().equals("Species")) {
				if (pepmodmap.get(key)) {
					mod++;
				} else {
					unmod++;
				}
			}
		}
		writer.close();
		System.out.println(mod + "\t" + unmod);
	}
	
	private static void readPepTax(String in) throws IOException {
		HashMap<String, int[]> map = new HashMap<String, int[]>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs[2].equals("Species")) {
				int[] count;
				if (map.containsKey(cs[3])) {
					count = map.get(cs[3]);
				} else {
					count = new int[2];
				}
				if (Boolean.parseBoolean(cs[1])) {
					count[0] = 1;
				} else {
					count[1] = 1;
				}
				map.put(cs[3], count);
			}
		}
		reader.close();
		System.out.println(map.size());

		int[] result = new int[3];
		for (String key : map.keySet()) {
			int[] count = map.get(key);
			if (count[0] == 1) {
				if (count[1] == 1) {
					result[0]++;
				} else {
					result[1]++;
				}
			} else {
				result[2]++;
			}
		}

		System.out.println(result[0] + "\t" + result[1] + "\t" + result[2]);
	}

	private static void readPepProTest(String psmin, String proin, String pepin) throws IOException {

		OpenPsmTsvReader reader = new OpenPsmTsvReader(psmin);
		OpenPSM[] openPsms = reader.getAllOpenPSMs();
		Arrays.sort(openPsms, new OpenPSM.InverseClassScoreComparator());
		int t = 0;
		int d = 0;
		for (int i = 0; i < openPsms.length; i++) {
			if (openPsms[i].isTarget()) {
				t++;
			} else {
				d++;
			}
			double fdr = (double) d / (double) t;
			openPsms[i].setQValue(fdr);
		}

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < openPsms.length; i++) {
			int modid = openPsms[i].getOpenModId();
			double classScore = openPsms[i].getQValue();
			boolean isTarget = openPsms[i].isTarget();
			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(classScore);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(classScore);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			}
			if (psmmap.containsKey(modid)) {
				psmmap.get(modid).add(i);
			} else {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				psmmap.put(modid, list);
			}
		}

		ArrayList<OpenPSM> filteredList = new ArrayList<OpenPSM>();
		int totaltarget = 0;
		int totaldecoy = 0;

		int modcount = 0;
		int[] useModIds = new int[modmap.size()];
		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		for (int i = 0; i < modkey.length; i++) {
			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];
			double[] targetScore = new double[tlist.size()];
			SummaryStatistics sstarget = new SummaryStatistics();
			for (int j = 0; j < targetScore.length; j++) {
				targetScore[j] = tlist.get(j);
				sstarget.addValue(tlist.get(j));
			}

			SummaryStatistics ssdecoy = new SummaryStatistics();
			double[] decoyScore = new double[dlist.size()];
			for (int j = 0; j < decoyScore.length; j++) {
				decoyScore[j] = dlist.get(j);
				ssdecoy.addValue(dlist.get(j));
			}

			boolean pass = false;
			useModIds[modkey[i]] = -1;
			if (targetScore.length > 1) {
				if (decoyScore.length > 1) {
					if (TestUtils.tTest(sstarget, ssdecoy, 0.01)) {
						pass = true;
					}
				} else {
					pass = true;
				}
			}

			if (pass) {
				modcount++;
				ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
				OpenPSM[] psms = new OpenPSM[psmIdList.size()];
				for (int j = 0; j < psms.length; j++) {
					psms[j] = openPsms[psmIdList.get(j)];
				}

				Arrays.sort(psms, new OpenPSM.InverseClassScoreComparator());
				int target = 0;
				int decoy = 0;

				for (int j = 0; j < psms.length; j++) {
					if (psms[j].isTarget()) {
						target++;
					} else {
						decoy++;
					}
					double locfdr = (double) decoy / (double) target;
					if (target != 0 && locfdr < 0.01) {
						filteredList.add(psms[j]);
						if (psms[j].isTarget()) {
							totaltarget++;
						} else {
							totaldecoy++;
						}
					} else {
						break;
					}
				}
			}
		}

		OpenPSM[] psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		InferedProtein[] proteins = ProteinInferenceReader.getAllProteins(proin);
		Arrays.sort(proteins);
		InferedPeptide[] peptides = ProteinInferenceReader.getAllPeptides(pepin);
		Arrays.sort(peptides);

		System.out.println("all psm\t" + psms.length);
		System.out.println("all pro\t" + proteins.length);
		System.out.println("all pep\t" + peptides.length);

		HashSet<String> proset = new HashSet<String>();
		int prot = 0;
		int prod = 0;
		for (InferedProtein ip : proteins) {
			if (ip.getName().startsWith("REV_")) {
				prod++;
			} else {
				prot++;
			}
			double fdr = (double) prod / (double) prot;
			if (fdr > 0.01) {
				System.out.println("protein\t" + prot + "\t" + prod);
				break;
			}
			proset.add(ip.getName());
		}

		HashMap<Integer, int[]> filteredModmap = new HashMap<Integer, int[]>();
		HashSet<String> psmset = new HashSet<String>();
		for (OpenPSM op : psms) {
			String pro = op.getProtein();
			if (proset.contains(pro)) {
				psmset.add(op.getSequence());
				int modid = op.getOpenModId();
				if(filteredModmap.containsKey(modid)){
					int[] tdc = filteredModmap.get(modid);
					if(op.isTarget()){
						tdc[0]++;
					}else{
						tdc[1]++;
					}
					filteredModmap.put(modid, tdc);
				}else{
					int[] tdc = new int[2];
					if(op.isTarget()){
						tdc[0]++;
					}else{
						tdc[1]++;
					}
					filteredModmap.put(modid, tdc);
				}
			}
		}
		System.out.println("psm\t" + psmset.size());
		
		for(Integer id : filteredModmap.keySet()){
			int[] tdc = filteredModmap.get(id);
			System.out.println(id+"\t"+tdc[0]+"\t"+tdc[1]+"\t"+(double)tdc[1]/(double)tdc[0]);
		}

		int pept = 0;
		int pepd = 0;
		for (InferedPeptide ip : peptides) {
			String sequence = ip.getName();
			String acc = ip.getAccession();
			if (psmset.contains(sequence)) {
				if (acc.startsWith("REV_")) {
					pepd++;
				} else {
					pept++;
				}
				double fdr = (double) pepd / (double) pept;
				if (fdr > 0.01) {
					System.out.println("peptide\t" + pept + "\t" + pepd);
					break;
				}
			}
		}
		System.out.println("peptide\t" + pept + "\t" + pepd);
		System.out.println();
	}
	
	/**
	 * X=False positive/Condition negative
	 * Y=True positive/Condition positive
	 * @param dir
	 * @throws IOException
	 * @throws DocumentException
	 */
	private static void ROCTestHyperscore(String psmin) throws IOException, DocumentException {

		OpenPsmTsvReader reader = new OpenPsmTsvReader(psmin);
		OpenPSM[] psms = reader.getAllOpenPSMs();

		int target = 0;
		int decoy = 0;
		int[][] counts = new int[50][2];

		for (OpenPSM mp : psms) {
			if (mp.isTarget()) {
				target++;
			} else {
				decoy++;
			}
			double score = mp.getClassScore();
			// double score = -Math.log(mp.getExpect()) * 10;
			int id = (int) score;
			if (id > 49) {
				for (int i = 0; i < counts.length; i++) {
					if (mp.isTarget()) {
						counts[i][0]++;
					} else {
						counts[i][1]++;
					}
				}
			} else {
				for (int i = 0; i <= id; i++) {
					if (mp.isTarget()) {
						counts[i][0]++;
					} else {
						counts[i][1]++;
					}
				}
			}
		}

		for (int i = 0; i < counts.length; i++) {
			double X = (double) counts[i][1] / (double) decoy;
			double Y = (double) counts[i][0] / (double) target;
			double fdr = (double) counts[i][1] / (double) counts[i][0];
			System.out.println(i + "\t" + counts[i][0] + "\t" + counts[i][1] + "\t" + fdr + "\t" + X + "\t" + Y);
		}
	}
	
	private static void ioTest(String in, String out) throws IOException {
		OpenPsmTsvReader reader = new OpenPsmTsvReader(in);
		OpenPsmTsvWriter writer = new OpenPsmTsvWriter(out, OpenPsmTsvWriter.full_information);
		OpenPSM[] psms = reader.getAllOpenPSMs();
		for(OpenPSM op : psms) {
			writer.addPSM(op);
		}
		writer.close();
	}
	
	private static void modtest(String in, double deltaMass) throws IOException {
		
		int nomodcount = 0;
		HashMap<Double, HashMap<String, Integer>> modmap = new HashMap<Double, HashMap<String, Integer>>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split(",");
			if(!cs[9].equals("2")) {
//				continue;
			}
			if (map.containsKey(cs[2])) {
				HashSet<String> set = map.get(cs[2]);
				set.add(cs[4]);
				map.put(cs[2], set);
			} else {
				HashSet<String> set = new HashSet<String>();
				set.add(cs[4]);
				map.put(cs[2], set);
			}
			if (cs[4].length() > 0) {
				double mass = Double.parseDouble(cs[4]);
				HashMap<String, Integer> seqmap;
				if (modmap.containsKey(mass)) {
					seqmap = modmap.get(mass);
				} else {
					seqmap = new HashMap<String, Integer>();
				}

				if (seqmap.containsKey(cs[2])) {
					seqmap.put(cs[2], seqmap.get(cs[2]) + 1);
				} else {
					seqmap.put(cs[2], 1);
				}
				modmap.put(mass, seqmap);
			}else {
				nomodcount++;
			}
		}
		reader.close();
System.out.println("nomodcount\t"+nomodcount);
		for (String key : map.keySet()) {
			HashSet<String> set = map.get(key);
//			if(key.contains("S") || key.contains("T")) {
//				if (map.get(key).size() > 3 && set.contains("79.9662") && set.contains("212.0088") && set.contains("242.0191")) {
//				if (set.contains("167.9831") && set.contains("151.9878") && !key.contains("K")) {
//				if (set.contains("248.0087")) {
//					System.out.println(key + "\t" + set);
//				}
//			}
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~");
		
		double mass = deltaMass;
		if (modmap.containsKey(mass)) {
			HashMap<String, Integer> seqmap = modmap.get(mass);
			String seq = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
			int count = 0;
			int ccc = 0;
			
			System.out.println("unique sequence count: "+seqmap.size());
			
			for (String key : seqmap.keySet()) {
//				if (seqmap.get(key) > count) {
//				if(key.contains("W")) {
//					continue;
//				}
				
				int number = 0;
				for(int i=0;i<key.length();i++) {
					if(key.charAt(i)=='S' || key.charAt(i)=='T') {
						number++;
					}
				}

				if(key.substring(0, key.length()-1).contains("K")) {
//				if(key.contains("W")) {
//				if(key.contains("N") || key.contains("Q")) {
//				if(number>=2) {
//				if(key.startsWith("Q")) {
					ccc++;
				}
//				if(key.contains("S") || key.contains("T"))
				System.out.println(key+"\t"+seqmap.get(key));
//				}
				if (key.length() < seq.length() && seqmap.get(key)>5) {
					count = seqmap.get(key);
					seq = key;
				}
			}

			System.out.println(mass + "\t" + seq + "\t" + count);
			System.out.println(ccc + "\t" + seqmap.size() + "\t" + (double) ccc / (double) seqmap.size());
		}
	}
	
	private static void moddbtest(String in, String database, double mass) throws IOException {
		
		HashMap<String, ProteinItem> promap = new HashMap<String, ProteinItem>();
		FastaReader proreader = new FastaReader(database);
		ProteinItem[] items = proreader.getItems();
		for(ProteinItem item : items) {
			promap.put(item.getRef(), item);
		}
		
		HashMap<Double, HashMap<String, Integer>> modmap = new HashMap<Double, HashMap<String, Integer>>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		HashMap<String, String> seqpromap = new HashMap<String, String>();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split(",");

			if (map.containsKey(cs[2])) {
				HashSet<String> set = map.get(cs[2]);
				set.add(cs[4]);
				map.put(cs[2], set);
			} else {
				HashSet<String> set = new HashSet<String>();
				set.add(cs[4]);
				map.put(cs[2], set);
				
				seqpromap.put(cs[2], cs[10]);
			}
			if (cs[4].length() > 0) {
				double modmass = Double.parseDouble(cs[4]);
				HashMap<String, Integer> seqmap;
				if (modmap.containsKey(modmass)) {
					seqmap = modmap.get(modmass);
				} else {
					seqmap = new HashMap<String, Integer>();
				}

				if (seqmap.containsKey(cs[2])) {
					seqmap.put(cs[2], seqmap.get(cs[2]) + 1);
				} else {
					seqmap.put(cs[2], 1);
				}
				modmap.put(modmass, seqmap);
			}
		}
		reader.close();

		for (String key : map.keySet()) {
			HashSet<String> set = map.get(key);
//			if(key.contains("S") || key.contains("T")) {
//				if (map.get(key).size() > 3 && set.contains("79.9662") && set.contains("212.0088") && set.contains("242.0191")) {
//				if (set.contains("167.9831") && set.contains("151.9878") && !key.contains("K")) {
//				if (set.contains("248.0087")) {
//					System.out.println(key + "\t" + set);
//				}
//			}
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~");
		
		Pattern pattern = Pattern.compile("N[A-Z][ST]");
		
		if (modmap.containsKey(mass)) {
			HashMap<String, Integer> seqmap = modmap.get(mass);
			String seq = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
			int count = 0;
			int ccc = 0;
			
			System.out.println("unique sequence count: "+seqmap.size());
			
			for (String key : seqmap.keySet()) {
//				if (seqmap.get(key) > count) {
				
				int number = 0;
				for (int i = 0; i < key.length(); i++) {
					if (key.charAt(i) == 'S' || key.charAt(i) == 'T') {
						number++;
					}
				}
				
//				if(key.contains("N") || key.contains("Q")) {
//					continue;
//				}
//				if(key.substring(0, key.length()-1).contains("K")) {
//				if(key.startsWith("L") || key.startsWith("I")) {
//				if(key.contains("N") || key.contains("Q")) {
				if(key.contains("W")) {
//				if(key.startsWith("R")) {
//				if (number >= 2) {
//					ccc++;
					// continue;
				}
//				if(key.contains("W"))
//					continue;
				
				Matcher matcher = null;
				
				String pro = seqpromap.get(key);
				int begin = -1;
				if (promap.containsKey(pro)) {
					
					ProteinItem item = promap.get(pro);
					begin = item.indexOf(key);

					int nextid = begin + key.length(); 
					if (nextid >= 0 && nextid < item.length()) {
						char nextaa = item.getAminoacid(begin + key.length());
						matcher = pattern.matcher(key+nextaa);
					}else {
						matcher = pattern.matcher(key);
					}
						
					/*
					 * int nextid = begin + key.length(); if (nextid >= 0 && nextid < item.length())
					 * { char nextaa = item.getAminoacid(begin + key.length()); // if (begin == 1 ||
					 * begin == 0) { if (nextaa == 'A') { ccc++; } }
					 */
					if (begin >= 2) {
						// System.out.println(item.getPeptide(begin-2, begin));
					}
				} else {
					matcher = pattern.matcher(key);
				}
				
				if(matcher.find()) {
					ccc++;
				} else {
					System.out.println(key+"\t"+seqmap.get(key)+"\t"+begin);
				}

//				if(key.startsWith("K"))
				
//				}
				if (key.length() < seq.length() && seqmap.get(key)>5) {
					count = seqmap.get(key);
					seq = key;
				}
			}

			System.out.println(mass + "\t" + seq + "\t" + count);
			System.out.println(ccc + "\t" + seqmap.size() + "\t" + (double) ccc / (double) seqmap.size());
		}
	}

	private static void modScoreTest(String in, int modid) throws IOException {
		OpenPsmTsvReader reader = new OpenPsmTsvReader(in);
		OpenPSM[] openPsms = reader.getAllOpenPSMs();
		for(OpenPSM op : openPsms) {
			int mid = op.getOpenModId();
			if(mid==modid) {
				if(op.getYmodIntensity()>0) {
					System.out.println(op.getScan()+"\t"+op.getFileName()+"\t"+op.getYrankscore()+"\t"+op.getYmodrankscore());
				}
			}
		}
	}
	
	private static void pepLengthStat(String in, String out, int count) throws NumberFormatException, IOException {

		HashMap<Double, ArrayList<Integer>> lengthMap = new HashMap<Double, ArrayList<Integer>>();
		HashMap<Double, ArrayList<Integer>> chargeMap = new HashMap<Double, ArrayList<Integer>>();

		BufferedReader reader = new BufferedReader(new FileReader(in));
		reader.readLine();
		
		CSVParser parser = CSVParser.parse(reader, CSVFormat.RFC4180);
		for (CSVRecord csvRecord : parser) {
			
			ArrayList<Integer> lengthList = null;
			ArrayList<Integer> chargeList = null;

			if (csvRecord.get(4).length() > 0) {
				double mass = Double.parseDouble(csvRecord.get(4));
				if (lengthMap.containsKey(mass)) {
					lengthList = lengthMap.get(mass);
				} else {
					lengthList = new ArrayList<Integer>();
					lengthMap.put(mass, lengthList);
				}
				lengthList.add(csvRecord.get(2).length());

				if (chargeMap.containsKey(mass)) {
					chargeList = chargeMap.get(mass);
				} else {
					chargeList = new ArrayList<Integer>();
					chargeMap.put(mass, chargeList);
				}
				chargeList.add(Integer.parseInt(csvRecord.get(9)));
			} else {
				double mass = 0.0;
				if (lengthMap.containsKey(mass)) {
					lengthList = lengthMap.get(mass);
				} else {
					lengthList = new ArrayList<Integer>();
					lengthMap.put(mass, lengthList);
				}
				lengthList.add(csvRecord.get(2).length());

				if (chargeMap.containsKey(mass)) {
					chargeList = chargeMap.get(mass);
				} else {
					chargeList = new ArrayList<Integer>();
					chargeMap.put(mass, chargeList);
				}
				chargeList.add(Integer.parseInt(csvRecord.get(9)));
			}
		}
		reader.close();

		Double[] keys = lengthMap.keySet().toArray(new Double[lengthMap.size()]);
		Arrays.sort(keys, new Comparator<Double>() {

			@Override
			public int compare(Double arg0, Double arg1) {
				// TODO Auto-generated method stub

				if (lengthMap.get(arg0).size() < lengthMap.get(arg1).size())
					return 1;
				if (lengthMap.get(arg0).size() > lengthMap.get(arg1).size())
					return -1;
				return 0;
			}
		});

		PrintWriter writer1 = new PrintWriter(out+"pep_length.txt");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append("dM").append(keys[i]).append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		writer1.println(sb);

		for (int i = 0;; i++) {
			sb = new StringBuilder();
			boolean have = false;
			for (int j = 0; j < count; j++) {
				ArrayList<Integer> lengthList = lengthMap.get(keys[j]);
				if (lengthList.size() > i) {
					have = true;
					sb.append(lengthList.get(i)).append(".0\t");
				} else {
					sb.append("NA\t");
				}
			}
			if (have) {
				sb.deleteCharAt(sb.length() - 1);
				writer1.println(sb);
			} else {
				break;
			}
		}
		writer1.close();
		
		PrintWriter writer2 = new PrintWriter(out+"charge.txt");
		sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append("dM").append(keys[i]).append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		writer2.println(sb);

		for (int i = 0;; i++) {
			sb = new StringBuilder();
			boolean have = false;
			for (int j = 0; j < count; j++) {
				ArrayList<Integer> chargeList = chargeMap.get(keys[j]);
				if (chargeList.size() > i) {
					have = true;
					sb.append(chargeList.get(i)).append(".0\t");
				} else {
					sb.append("NA\t");
				}
			}
			if (have) {
				sb.deleteCharAt(sb.length() - 1);
				writer2.println(sb);
			} else {
				break;
			}
		}
		writer2.close();

		PrintWriter writer3 = new PrintWriter(out+"count.txt");
		sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append("dM").append(keys[i]).append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		for (int i = 0; i < count; i++) {
			sb.append(chargeMap.get(keys[i]).size()).append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		writer3.print(sb);
		writer3.close();
	}
	
	private static void copy(String in, String out) throws IOException {
		File[] mgfs = (new File(in)).listFiles();
		for (int i = 0; i < mgfs.length; i++) {
			String name = mgfs[i].getName();
			if(name.endsWith("mgf")) {
				String raw = name.replace("mgf", "raw");
				PrintWriter writer = new PrintWriter(out + "\\" + raw);
				writer.close();
			}
		}
	}
	
	private static void compareAll(String metaPsm, String metaMods, String fragPsm, String fragMods)
			throws IOException, DocumentException {

		HashMap<String, OpenPSM> metaPsmMap = new HashMap<String, OpenPSM>();
		OpenPsmTsvReader metaPsmReader = new OpenPsmTsvReader(metaPsm);
		OpenPSM[] psms = metaPsmReader.getAllOpenPSMs();
		for (OpenPSM psm : psms) {
//			if (psm.getOpenModId() == 0) {
			metaPsmMap.put(psm.getFileName() + "." + psm.getScan() + "." + psm.getScan(), psm);
//			}
		}

		OpenModXmlReader metaModsReader = new OpenModXmlReader(metaMods);
		OpenMod[] mods = metaModsReader.getOpenMods();

		BufferedReader fragPsmReader = new BufferedReader(new FileReader(fragPsm));
		String line = fragPsmReader.readLine();
		String[] title = line.split("\t");
		int dmid2 = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Adjusted Delta Mass")) {
				dmid2 = i;
			}
		}
		HashMap<Integer, Integer> deltamap = new HashMap<Integer, Integer>();
		HashMap<String, String> fragPsmMap = new HashMap<String, String>();
		while ((line = fragPsmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			double dm = Double.parseDouble(cs[dmid2]);
			double mass = Double.parseDouble(cs[dmid2 + 1]);
			double ppm = dm / mass * 1E6;

			String scan = cs[0].substring(0, cs[0].lastIndexOf("."));
			fragPsmMap.put(scan, line);
			/*
			 * if (dm >= -0.01 && dm <= 0.01) { fragPsmMap.put(scan, cs[1]); } else if (dm
			 * >= 0.9935 && dm <= 1.0128) { fragPsmMap.put(scan, cs[1]); } else if (dm >=
			 * 1.9962 && dm <= 2.015) { fragPsmMap.put(scan, cs[1]); } else if (dm >= 2.9991
			 * && dm <= 3.0154) { fragPsmMap.put(scan, cs[1]); }
			 */
		}
		fragPsmReader.close();

		HashMap<Integer, double[]> modRangeMap = new HashMap<Integer, double[]>();
		ArrayList<double[]> modRangeList = new ArrayList<double[]>();
		BufferedReader fragModsReader = new BufferedReader(new FileReader(fragMods));
		line = fragModsReader.readLine();
		String[] modtitle = line.split("\t");

		while ((line = fragModsReader.readLine()) != null) {
			String[] cs = line.split("\t");
			double lower = Double.parseDouble(cs[1]);
			double upper = Double.parseDouble(cs[2]);
			for (int i = 0; i < mods.length; i++) {
				double modi = mods[i].getExpModMass();
				if (modi >= lower && modi <= upper) {
					modRangeMap.put(mods[i].getId(), new double[] { lower, upper });
				}
			}

			modRangeList.add(new double[] { lower, upper });
		}
		fragModsReader.close();

		HashSet<String> setall = new HashSet<String>();
		setall.addAll(metaPsmMap.keySet());
		setall.addAll(fragPsmMap.keySet());

		System.out.println(metaPsmMap.size() + "\t" + fragPsmMap.size() + "\t" + setall.size());

		System.out.println(mods.length + "\t" + modRangeMap.size());

		HashMap<Integer, HashMap<Double, Double>> modIdMap = new HashMap<Integer, HashMap<Double, Double>>();
		for (String key : setall) {
			if (metaPsmMap.containsKey(key) && fragPsmMap.containsKey(key)) {
				int modid = metaPsmMap.get(key).getOpenModId();
				String[] cs = fragPsmMap.get(key).split("\t");
				double lower = Double.parseDouble(cs[cs.length - 2]);
				double upper = Double.parseDouble(cs[cs.length - 1]);

				if (modIdMap.containsKey(modid)) {
					modIdMap.get(modid).put(new Double(lower), new Double(upper));
				} else {
					HashMap<Double, Double> map = new HashMap<Double, Double>();
					map.put(new Double(lower), new Double(upper));
					modIdMap.put(modid, map);
				}
			}
		}
		
		if (modIdMap.containsKey(0)) {
			HashMap<Double, Double> map = modIdMap.get(0);
			for (Double key : map.keySet()) {
				System.out.println("0\t0.0" + "\t" + key + "\t" + map.get(key));
			}
		}

		for (OpenMod mod : mods) {
			if (modIdMap.containsKey(mod.getId())) {
				HashMap<Double, Double> map = modIdMap.get(mod.getId());
				for (Double key : map.keySet()) {
					System.out.println(mod.getId() + "\t" + mod.getExpModMass() + "\t" + key + "\t" + map.get(key));
				}
			}
		}
	}
	
}
