/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.philosopher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Kai Cheng
 *
 */
public class PhilosopherPsmReader {
	
	private PhilosopherPsm[] psms;

	public PhilosopherPsmReader(String in) {
		this(new File(in));
	}

	public PhilosopherPsmReader(File in) {
		this.read(in);
	}

	private void read(File in) {

		ArrayList<PhilosopherPsm> list = new ArrayList<PhilosopherPsm>();
		BufferedReader reader = null;
		try {

			reader = new BufferedReader(new FileReader(in));
			String line = reader.readLine();
			String[] title = line.split("\t");

			int fileNameID = -1;
			int sequenceID = -1;
			int chargeID = -1;
			int rtID = -1;
			int calMzID = -1;
			int obsMzID = -1;
			int oriDeltaMassID = -1;
			int adjDeltaMassID = -1;
			int expMassID = -1;
			int pepMassID = -1;
			int eValueID = -1;
			int hyperScoreID = -1;
			int nextScoreID = -1;
			int probabilityID = -1;
			int variModsID = -1;
			int openModsID = -1;
			int proteinID = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Spectrum")) {
					fileNameID = i;
				} else if (title[i].equals("Peptide")) {
					sequenceID = i;
				} else if (title[i].equals("Charge")) {
					chargeID = i;
				} else if (title[i].equals("Retention")) {
					rtID = i;
				} else if (title[i].equals("Calculated M/Z")) {
					calMzID = i;
				} else if (title[i].equals("Observed M/Z")) {
					obsMzID = i;
				} else if (title[i].equals("Original Delta Mass")) {
					oriDeltaMassID = i;
				} else if (title[i].equals("Adjusted Delta Mass")) {
					adjDeltaMassID = i;
				} else if (title[i].equals("Experimental Mass")) {
					expMassID = i;
				} else if (title[i].equals("Peptide Mass")) {
					pepMassID = i;
				} else if (title[i].equals("Expectation")) {
					eValueID = i;
				} else if (title[i].equals("Hyperscore")) {
					hyperScoreID = i;
				} else if (title[i].equals("Nextscore")) {
					nextScoreID = i;
				} else if (title[i].equals("PeptideProphet Probability")) {
					probabilityID = i;
				} else if (title[i].equals("Assigned Modifications")) {
					variModsID = i;
				} else if (title[i].equals("Observed Modifications")) {
					openModsID = i;
				} else if (title[i].equals("Protein")) {
					proteinID = i;
				}
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				String fileName = "";
				int scan = 0;
				if (fileNameID > -1) {
					int id1 = cs[fileNameID].indexOf("\\.");
					if (id1 > 0) {
						fileName = cs[fileNameID].substring(0, id1);
						int id2 = cs[fileNameID].indexOf("\\.", id1 + 1);
						if (id2 > 0) {
							scan = Integer.parseInt(cs[fileNameID].substring(id1 + 1, id2));
						}
					} else {
						fileName = cs[fileNameID];
					}
				}

				String sequence = "";
				if (sequenceID > -1) {
					sequence = cs[sequenceID];
				}

				int charge = 0;
				if (chargeID > -1) {
					charge = Integer.parseInt(cs[chargeID]);
				}

				double rt = 0.0;
				if (rtID > -1) {
					rt = Double.parseDouble(cs[rtID]);
				}

				double calMz = 0.0;
				if (calMzID > -1) {
					calMz = Double.parseDouble(cs[calMzID]);
				}

				double obsMz = 0.0;
				if (obsMzID > -1) {
					obsMz = Double.parseDouble(cs[obsMzID]);
				}

				double oriDeltaMass = 0.0;
				if (oriDeltaMassID > -1) {
					oriDeltaMass = Double.parseDouble(cs[oriDeltaMassID]);
				}

				double adjDeltaMass = 0.0;
				if (adjDeltaMassID > -1) {
					adjDeltaMass = Double.parseDouble(cs[adjDeltaMassID]);
				}

				double expMass = 0.0;
				if (expMassID > -1) {
					expMass = Double.parseDouble(cs[expMassID]);
				}

				double pepMass = 0.0;
				if (pepMassID > -1) {
					pepMass = Double.parseDouble(cs[pepMassID]);
				}

				double eValue = 0.0;
				if (eValueID > -1) {
					eValue = Double.parseDouble(cs[eValueID]);
				}

				double hyperScore = 0.0;
				if (hyperScoreID > -1) {
					hyperScore = Double.parseDouble(cs[hyperScoreID]);
				}

				double nextScore = 0.0;
				if (nextScoreID > -1) {
					nextScore = Double.parseDouble(cs[nextScoreID]);
				}

				double probability = 0.0;
				if (probabilityID > -1) {
					probability = Double.parseDouble(cs[probabilityID]);
				}

				String variMods = "";
				if (variModsID > -1) {
					variMods = cs[variModsID];
				}

				String openMods = "";
				if (openModsID > -1) {
					openMods = cs[openModsID];
				}

				String protein = "";
				if (proteinID > -1) {
					protein = cs[proteinID];
				}

				PhilosopherPsm psm = new PhilosopherPsm(fileName, scan, sequence, charge, rt, calMz, obsMz,
						oriDeltaMass, adjDeltaMass, expMass, pepMass, eValue, hyperScore, nextScore, probability,
						variMods, openMods, protein);
				list.add(psm);
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.psms = list.toArray(new PhilosopherPsm[list.size()]);
	}

	
	public PhilosopherPsm[] getPsms() {
		return psms;
	}
}
