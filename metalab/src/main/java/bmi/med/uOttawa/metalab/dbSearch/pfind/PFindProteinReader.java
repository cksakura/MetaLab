/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Kai Cheng
 *
 */
public class PFindProteinReader {

	private File in;
	private PFindProtein[] proteins;

	private String rev = "REV_";
	private double threshold = 0.01;

	public PFindProteinReader(String in) {
		this(new File(in));
	}

	public PFindProteinReader(File in) {
		this.in = in;
	}
	
	public PFindProteinReader(File in, double threshold) {
		this.threshold = threshold;
		this.in = in;
	}

	public void filterRead(HashMap<String, HashSet<String>> pepProMap) {

		ArrayList<PFindProtein> list = new ArrayList<PFindProtein>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
			String line = reader.readLine();
			String[] title = line.split("\t");

			int nameID = -1;
			int scoreID = -1;
			int qValueID = -1;
			int coverageID = -1;
			int pepCountID = -1;
			int desID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("AC")) {
					nameID = i;
				} else if (title[i].equals("Score")) {
					scoreID = i;
				} else if (title[i].equals("Q-Value")) {
					qValueID = i;
				} else if (title[i].equals("Coverage")) {
					coverageID = i;
				} else if (title[i].equals("No.Peptide")) {
					pepCountID = i;
				} else if (title[i].equals("Description")) {
					desID = i;
				}
			}

			String[] title2 = reader.readLine().split("\t");
			int spCountID = -1;
			int seqID = -1;
			for (int i = 0; i < title2.length; i++) {
				if (title2[i].equals("Spec_Num")) {
					spCountID = i;
				} else if (title2[i].equals("Sequence")) {
					seqID = i;
				} 
			}

			int spCount = 0;
			ArrayList<String> peplist = new ArrayList<String>();
			String name = "";
			boolean use = false;

			HashSet<String> findPepSet = new HashSet<String>();

			while ((line = reader.readLine()) != null) {

				String[] cs = line.split("\t");

				if (cs.length == title.length) {

					if (list.size() > 0) {

						if (use) {

							boolean add = false;
							PFindProtein lastProtein = list.get(list.size() - 1);
							lastProtein.setSpCount(spCount);

							for (String pep : peplist) {
								if (!findPepSet.contains(pep)) {
									findPepSet.add(pep);
									add = true;
								}
							}

							if (!add) {
								list.remove(list.size() - 1);
							}

						} else {
							list.remove(list.size() - 1);
						}
					}

					name = cs[nameID];

					double score = Double.parseDouble(cs[scoreID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					double coverage = Double.parseDouble(cs[coverageID]);
					int pepCount = Integer.parseInt(cs[pepCountID]);
					String des = cs[desID];

					if (name.startsWith(rev)) {
						use = false;
					} else {
						use = true;
					}

					PFindProtein protein = new PFindProtein(list.size() + 1, name, score, qValue, coverage, pepCount,
							des, new HashSet<String>());

					list.add(protein);

					spCount = 0;
					peplist = new ArrayList<String>();

				} else if (cs.length == title2.length) {
					spCount += Integer.parseInt(cs[spCountID]);
					peplist.add(cs[seqID]);
				} else if (cs.length >= 4) {
					if (cs[1].equals("SameSet") && !cs[2].startsWith(rev)) {
						if (use) {
							list.get(list.size() - 1).getSameSet().add(cs[2]);
						} else {
							list.get(list.size() - 1).setName(cs[2]);
							use = true;
						}
					}
				} else if (cs.length == 1 && line.contains("Summary")) {
					break;
				}
			}
			reader.close();

			if (list.size() > 0 && peplist.size() > 0) {

				if (use) {

					boolean add = false;
					PFindProtein lastProtein = list.get(list.size() - 1);
					lastProtein.setSpCount(spCount);

					for (String pep : peplist) {
						if (!findPepSet.contains(pep)) {
							findPepSet.add(pep);
							add = true;
						}
					}

					if (!add) {
						list.remove(list.size() - 1);
					}

				} else {
					list.remove(list.size() - 1);
				}
			}

			System.out.println(findPepSet.size() + "\t" + pepProMap.size() + "\t" + list.size());

			HashSet<String> missProSet = new HashSet<String>();
			for (String pep : pepProMap.keySet()) {
				if (!findPepSet.contains(pep)) {
					missProSet.addAll(pepProMap.get(pep));
				}
			}

			reader = new BufferedReader(new FileReader(in));
			line = reader.readLine();
			line = reader.readLine();

			HashMap<String, PFindProtein> tempProMap = new HashMap<String, PFindProtein>();

			while ((line = reader.readLine()) != null) {

				String[] cs = line.split("\t");

				double score = 0;
				double qValue = 0;

				if (cs.length == title.length) {

					if (findPepSet.size() == pepProMap.size()) {
						break;
					}

					tempProMap = new HashMap<String, PFindProtein>();
					score = Double.parseDouble(cs[scoreID]);
					qValue = Double.parseDouble(cs[qValueID]);

				} else if (cs.length == title2.length) {
					if (!findPepSet.contains(cs[seqID])) {
						PFindProtein protein = null;

						HashSet<String> pros = pepProMap.get(cs[seqID]);
						if (pros != null && pros.size() > 0) {
							for (String pro : pros) {
								if (tempProMap.containsKey(pro)) {
									if (protein == null) {
										protein = tempProMap.get(pro);
									} else {
										protein.getSameSet().add(pro);
									}
								}
							}
						}

						if (protein != null) {
							protein.setSpCount(Integer.parseInt(cs[spCountID]));
							findPepSet.add(cs[seqID]);
							list.add(protein);
							protein.setId(list.size());
						}
					}
				} else if (cs.length >= 4) {
					if (missProSet.contains(cs[2])) {

						double coverage = Double.parseDouble(cs[3]);
						int pepCount = Integer.parseInt(cs[4]);

						PFindProtein protein = new PFindProtein(0, cs[2], score, qValue, coverage, pepCount, "",
								new HashSet<String>());

						tempProMap.put(cs[2], protein);
					}
				} else if (cs.length == 1 && line.contains("Summary")) {
					break;
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.proteins = list.toArray(new PFindProtein[list.size()]);
	}
/*	
	public void read(File in) {

		this.pepProMap = new HashMap<String, HashSet<String>>();
		ArrayList<PFindProtein> list = new ArrayList<PFindProtein>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
			String line = reader.readLine();
			String[] title = line.split("\t");

			int idID = -1;
			int nameID = -1;
			int scoreID = -1;
			int qValueID = -1;
			int coverageID = -1;
			int pepCountID = -1;
			int desID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("ID")) {
					idID = i;
				} else if (title[i].equals("AC")) {
					nameID = i;
				} else if (title[i].equals("Score")) {
					scoreID = i;
				} else if (title[i].equals("Q-Value")) {
					qValueID = i;
				} else if (title[i].equals("Coverage")) {
					coverageID = i;
				} else if (title[i].equals("No.Peptide")) {
					pepCountID = i;
				} else if (title[i].equals("Description")) {
					desID = i;
				}
			}

			String[] title2 = reader.readLine().split("\t");
			int spCountID = -1;
			int seqID = -1;
			for (int i = 0; i < title2.length; i++) {
				if (title2[i].equals("Spec_Num")) {
					spCountID = i;
				} else if (title2[i].equals("Sequence")) {
					seqID = i;
				}
			}

			int spCount = 0;
			ArrayList<String> peplist = new ArrayList<String>();
			String name = "";
			boolean use = false;

			while ((line = reader.readLine()) != null) {

				String[] cs = line.split("\t");

				if (cs.length == title.length) {

					if (cs[nameID].startsWith(rev)) {
						use = false;
						continue;
					}

					if (list.size() > 0) {
						boolean targetPro = true;
						PFindProtein lastProtein = list.get(list.size() - 1);
						HashSet<String> pset = lastProtein.getSameSet();
						for (String subp : pset) {
							if (subp.startsWith(rev)) {
								targetPro = false;
								break;
							}
						}
						if (targetPro) {
							lastProtein.setSpCount(spCount);

							for (String pep : peplist) {
								if (this.pepProMap.containsKey(pep)) {
									this.pepProMap.get(pep).add(name);
									this.pepProMap.get(pep).addAll(pset);
								} else {
									HashSet<String> ps = new HashSet<String>();
									ps.add(name);
									ps.addAll(pset);
									this.pepProMap.put(pep, ps);
								}
							}

						} else {
							list.remove(list.size() - 1);
						}
					}

					int id = Integer.parseInt(cs[idID]);

					name = cs[nameID];

					double score = Double.parseDouble(cs[scoreID]);
					double qValue = Double.parseDouble(cs[qValueID]);
					double coverage = Double.parseDouble(cs[coverageID]);
					int pepCount = Integer.parseInt(cs[pepCountID]);
					String des = cs[desID];

					if (name.startsWith(rev)) {
						use = false;
					} else {
						use = true;
						PFindProtein protein = new PFindProtein(id, name, score, qValue, coverage, pepCount, des,
								new HashSet<String>());

						list.add(protein);
					}

					spCount = 0;
					peplist = new ArrayList<String>();

				} else if (cs.length == title2.length) {
					if (use) {
						spCount += Integer.parseInt(cs[spCountID]);
						peplist.add(cs[seqID]);
					}

				} else if (cs.length >= 4) {
					if (use && cs[1].equals("SameSet")) {
						list.get(list.size() - 1).getSameSet().add(cs[2]);
					}
				} else if (cs.length == 1) {
					break;
				}
			}
			reader.close();

			if (list.size() > 0 && peplist.size() > 0) {
				boolean targetPro = true;
				PFindProtein lastProtein = list.get(list.size() - 1);
				HashSet<String> pset = lastProtein.getSameSet();
				for (String subp : pset) {
					if (subp.startsWith(rev)) {
						targetPro = false;
						break;
					}
				}
				if (targetPro) {
					lastProtein.setSpCount(spCount);
					for (String pep : peplist) {
						if (this.pepProMap.containsKey(pep)) {
							this.pepProMap.get(pep).add(name);
							this.pepProMap.get(pep).addAll(pset);
						} else {
							HashSet<String> ps = new HashSet<String>();
							ps.add(name);
							ps.addAll(pset);
							this.pepProMap.put(pep, ps);
						}
					}
				} else {
					list.remove(list.size() - 1);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<PFindProtein> usedList = new ArrayList<PFindProtein>();
		for (PFindProtein pro : list) {
			if (pro.getqValue() <= threshold) {
				usedList.add(pro);
			} else {
				System.out.println(pro.getName()+"\t"+pro.getqValue()+"\t"+threshold);
			}
		}
		this.proteins = usedList.toArray(new PFindProtein[usedList.size()]);
	}
*/
	public PFindProtein[] getProteins() {
		return proteins;
	}

	public static void main(String[] args) throws IOException {

		PFindPsmReader psmReader = new PFindPsmReader(
				"Z:\\Kai\\tmt\\TMT20201020\\MetaLab_hap\\pfind_open_search\\Xu_20201007_LSARP_HM0860_TMT_rowB_20201011230407"
						+ "\\pFind-Filtered.spectra");
		PFindPSM[] psms = psmReader.getPsms();
		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < psms.length; i++) {
			String pep = psms[i].getSequence();
			String[] pros = psms[i].getProteins();

			if (pepProMap.containsKey(pep)) {
				HashSet<String> set = pepProMap.get(pep);
				for (int j = 0; j < pros.length; j++) {
					set.add(pros[j]);
				}
			} else {
				HashSet<String> set = new HashSet<String>();
				for (int j = 0; j < pros.length; j++) {
					set.add(pros[j]);
				}
				pepProMap.put(pep, set);
			}
		}

		PFindProteinReader pr1 = new PFindProteinReader(
				"Z:\\Kai\\tmt\\TMT20201020\\MetaLab_hap\\pfind_open_search\\Xu_20201007_LSARP_HM0860_TMT_rowB_20201011230407\\pFind.protein");
		pr1.filterRead(pepProMap);
	}
}
