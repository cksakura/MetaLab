/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantMSMSReader {
	
	private MaxquantMSMS[] msms;

	public MaxquantMSMSReader(String in) {
		this(new File(in));
	}

	public MaxquantMSMSReader(File in) {
		read(in);
	}

	private void read(File in) {
		if (this.msms == null) {

			try {
				ArrayList<MaxquantMSMS> list = new ArrayList<MaxquantMSMS>();

				BufferedReader reader = new BufferedReader(new FileReader(in));
				String line = reader.readLine();
				String[] title = line.split("\t");

				int rawID = -1;
				int scanID = -1;
				int sequenceID = -1;
				int missID = -1;
				int modseqID = -1;
				int proteinID = -1;
				int chargeID = -1;
				int mzID = -1;
				int massID = -1;
				int massErrorID = -1;
				int rtID = -1;
				int PEPID = -1;
				int scoreID = -1;
				int deltaScoreID = -1;
				int reverseID = -1;
				int idID = -1;
				int proidID = -1;
				int pepidID = -1;
				int modpepidID = -1;

				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("Raw file")) {
						rawID = i;
					} else if (title[i].equals("Scan number")) {
						scanID = i;
					} else if (title[i].equals("Sequence")) {
						sequenceID = i;
					} else if (title[i].equals("Missed cleavages")) {
						missID = i;
					} else if (title[i].equals("Modified sequence")) {
						modseqID = i;
					} else if (title[i].equals("Proteins")) {
						proteinID = i;
					} else if (title[i].equals("Charge")) {
						chargeID = i;
					} else if (title[i].equals("m/z")) {
						mzID = i;
					} else if (title[i].equals("Mass")) {
						massID = i;
					} else if (title[i].equals("Mass error [ppm]")) {
						massErrorID = i;
					} else if (title[i].equals("Retention time")) {
						rtID = i;
					} else if (title[i].equals("PEP")) {
						PEPID = i;
					} else if (title[i].equals("Score")) {
						scoreID = i;
					} else if (title[i].equals("Delta score")) {
						deltaScoreID = i;
					} else if (title[i].equals("Reverse")) {
						reverseID = i;
					} else if (title[i].equals("id")) {
						idID = i;
					} else if (title[i].equals("Protein group IDs")) {
						proidID = i;
					} else if (title[i].equals("Peptide ID")) {
						pepidID = i;
					} else if (title[i].equals("Mod. peptide ID")) {
						modpepidID = i;
					}
				}

				while ((line = reader.readLine()) != null) {

					String[] cs = line.split("\t");

					String raw = "";
					if (rawID >= 0) {
						raw = cs[rawID];
					}
					int scan = 0;
					if (scanID >= 0 && cs[scanID].length() > 0) {
						scan = Integer.parseInt(cs[scanID]);
					}
					String sequence = "";
					if (sequenceID >= 0) {
						sequence = cs[sequenceID];
					}
					int miss = 0;
					if (missID >= 0 && cs[missID].length() > 0) {
						miss = Integer.parseInt(cs[missID]);
					}
					String modseq = "";
					if (modseqID >= 0) {
						modseq = cs[modseqID];
					}
					String protein = "";
					if (proteinID >= 0) {
						protein = cs[proteinID];
					}
					int charge = 0;
					if (chargeID >= 0 && cs[chargeID].length() > 0) {
						charge = Integer.parseInt(cs[chargeID]);
					}
					double mz = 0.0;
					if (mzID >= 0 && cs[mzID].length() > 0) {
						mz = Double.parseDouble(cs[mzID]);
					}
					double mass = 0.0;
					if (massID >= 0 && cs[massID].length() > 0) {
						mass = Double.parseDouble(cs[massID]);
					}
					double massError = 0.0;
					if (massErrorID >= 0 && cs[massErrorID].length() > 0 && !cs[massErrorID].equals("NaN")) {
						massError = Double.parseDouble(cs[massErrorID]);
					}
					double rt = 0.0;
					if (rtID >= 0 && cs[rtID].length() > 0 && !cs[rtID].equals("NaN")) {
						rt = Double.parseDouble(cs[rtID]);
					}
					double PEP = 0.0;
					if (PEPID >= 0 && cs[PEPID].length() > 0 && !cs[PEPID].equals("NaN")) {
						PEP = Double.parseDouble(cs[PEPID]);
					}
					double score = 0.0;
					if (scoreID >= 0 && cs[scoreID].length() > 0 && !cs[scoreID].equals("NaN")) {
						score = Double.parseDouble(cs[scoreID]);
					}
					double deltaScore = 0.0;
					if (deltaScoreID >= 0 && cs[deltaScoreID].length() > 0 && !cs[deltaScoreID].equals("NaN")) {
						deltaScore = Double.parseDouble(cs[deltaScoreID]);
					}
					boolean isTarget = false;
					if (reverseID >= 0) {
						isTarget = !cs[reverseID].equals("+");
					}
					int id = 0;
					if (idID >= 0 && cs[idID].length() > 0) {
						id = Integer.parseInt(cs[idID]);
					}
					int[] proid = new int[] {};
					if (proidID >= 0 && cs[proidID].length() > 0) {
						String[] proids = cs[proidID].split(";");
						proid = new int[proids.length];
						for (int i = 0; i < proid.length; i++) {
							proid[i] = Integer.parseInt(proids[i]);
						}
					}
					int pepid = 0;
					if (pepidID >= 0 && cs[pepidID].length() > 0) {
						pepid = Integer.parseInt(cs[pepidID]);
					}
					int modpepid = 0;
					if (modpepidID >= 0 && cs[modpepidID].length() > 0) {
						modpepid = Integer.parseInt(cs[modpepidID]);
					}

					MaxquantMSMS msms = new MaxquantMSMS(raw, scan, sequence, miss, modseq, protein, charge, mz, mass,
							massError, rt, PEP, score, deltaScore, isTarget, id, proid, pepid, modpepid);
					list.add(msms);
				}

				reader.close();

				this.msms = list.toArray(new MaxquantMSMS[list.size()]);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public MaxquantMSMS[] getMsms() {
		return msms;
	}
	
}
