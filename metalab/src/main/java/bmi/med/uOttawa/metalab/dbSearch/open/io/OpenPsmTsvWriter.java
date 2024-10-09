/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;

/**
 * @author Kai Cheng
 *
 */
public class OpenPsmTsvWriter {
	
	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenPsmTsvWriter.class);

	protected static final String[] title = new String[] { "File Name", "Base Sequence", "Full Sequence",
			"Peptide Monoisotopic Mass", "Scan Retention Time", "Precursor Charge", "Protein Accession", "Scan number",
			"Peptide Mass", "Mass difference", "Missed cleavages", "hyperscore", "nextscore", "expect", "num_tol_term",
			"tot_num_ions", "num_matched_ions", "open_mod_mass", "delta_open_mod_mass", "b_intensity", "y_intensity",
			"b_rankscore", "y_rankscore", "b_ion_count", "y_ion_count", "fragment_delta_mass", "b_mod_intensity",
			"y_mod_intensity", "b_mod_rankscore", "y_mod_rankscore", "mod_fragment_delta_mass", "neutralLossMass",
			"nlPeakRankScore", "mod_id", "possible_loc", "percolator_score", "q_value" };
	
	private PrintWriter writer;
	private int type;
	
	private DecimalFormat df2 = FormatTool.getDF2();
	private DecimalFormat df4 = FormatTool.getDF4();
	
	public static final int full_information = 0;
	public static final int simple_information = 1;
	public static final int pFindFormat = 2;

	public OpenPsmTsvWriter(String output, int type) {
		this(new File(output), type);
	}

	public OpenPsmTsvWriter(File output, int type) {
		try {
			this.writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing PSMs to " + output, e);
		}
		this.type = type;
		this.addTitle();
	}

	private void addTitle() {
		StringBuilder sb = new StringBuilder();
		if (type == full_information) {
			for (String t : title) {
				sb.append(t).append("\t");
			}
		} else if (type == simple_information) {
			for (int i = 0; i < 14; i++) {
				sb.append(title[i]).append("\t");
			}
			for (int i = title.length - 4; i < title.length; i++) {
				sb.append(title[i]).append("\t");
			}
		} else if (type == pFindFormat) {
			for (int i = 0; i < 7; i++) {
				sb.append(title[i]).append("\t");
			}
			sb.append("Score").append("\t");
			sb.append("PEP").append("\t");
			for (int i = 8; i <= 10; i++) {
				sb.append(title[i]).append("\t");
			}
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		this.writer.println(sb);
	}
	
	public static String[] getTitle(int type) {
		if (type == full_information) {
			return title;
		} else if (type == simple_information) {
			String[] shortTitle = new String[18];
			System.arraycopy(title, 0, shortTitle, 0, 14);
			System.arraycopy(title, title.length - 4, shortTitle, shortTitle.length - 4, 4);
			return shortTitle;
		} else if (type == pFindFormat) {
			String[] shortTitle = new String[8];
			System.arraycopy(title, 0, shortTitle, 0, shortTitle.length - 1);
			shortTitle[7] = "Score";
			return shortTitle;
		}
		return null;
	}

	public void addPSM(OpenPSM openpsm) {

		StringBuilder sb = new StringBuilder();

		if (type == full_information) {

			sb.append(openpsm.getFileName()).append("\t");
			sb.append(openpsm.getSequence()).append("\t");
			sb.append(openpsm.getModSequence()).append("\t");

			sb.append(openpsm.getPrecursorMr()).append("\t");
			sb.append(openpsm.getRt()).append("\t");
			sb.append(openpsm.getCharge()).append("\t");
			sb.append(openpsm.getProtein()).append("\t");

			sb.append(openpsm.getScan()).append("\t");
			sb.append(openpsm.getPepMass()).append("\t");
			sb.append(openpsm.getMassDiff()).append("\t");
			sb.append(openpsm.getMiss()).append("\t");

			sb.append(openpsm.getHyperscore()).append("\t");
			sb.append(openpsm.getNextscore()).append("\t");
			sb.append(openpsm.getExpect()).append("\t");

			sb.append(openpsm.getNum_tol_term()).append("\t");
			sb.append(openpsm.getTot_num_ions()).append("\t");
			sb.append(openpsm.getNum_matched_ions()).append("\t");

			sb.append(openpsm.getOpenModmass()).append("\t");
			sb.append(openpsm.getOpenModMassDiff()).append("\t");

			sb.append(openpsm.getBintensity()).append("\t");
			sb.append(openpsm.getYintensity()).append("\t");
			sb.append(openpsm.getBrankscore()).append("\t");
			sb.append(openpsm.getYrankscore()).append("\t");
			sb.append(openpsm.getBcount()).append("\t");
			sb.append(openpsm.getYcount()).append("\t");
			sb.append(openpsm.getFragDeltaMass()).append("\t");

			sb.append(openpsm.getBmodIntensity()).append("\t");
			sb.append(openpsm.getYmodIntensity()).append("\t");
			sb.append(openpsm.getBmodrankscore()).append("\t");
			sb.append(openpsm.getYmodrankscore()).append("\t");
			sb.append(openpsm.getFragModDeltaMass()).append("\t");

			sb.append(df4.format(openpsm.getNeutralLossMass())).append("\t");
			sb.append(df4.format(openpsm.getNlPeakRankScore())).append("\t");

			sb.append(openpsm.getOpenModId()).append("\t");

			if (openpsm.getOpenModId() > 0) {
				int[] possible_loc = openpsm.getPossibleLoc();
				StringBuilder loc = new StringBuilder();
				for (int i = 0; i < possible_loc.length; i++) {
					loc.append(possible_loc[i]).append("_");
				}
				if (loc.length() > 0) {
					loc.deleteCharAt(loc.length() - 1);
				}
				sb.append(loc).append("\t");
			} else {
				sb.append("\t");
			}

			sb.append(df4.format(openpsm.getClassScore())).append("\t");
			sb.append(df4.format(openpsm.getQValue())).append("\t");

		} else if (type == simple_information) {

			sb.append(openpsm.getFileName()).append("\t");
			sb.append(openpsm.getSequence()).append("\t");
			sb.append(openpsm.getModSequence()).append("\t");

			sb.append(openpsm.getPepMass()).append("\t");
			sb.append(openpsm.getRt()).append("\t");
			sb.append(openpsm.getCharge()).append("\t");
			sb.append(openpsm.getProtein()).append("\t");

			sb.append(openpsm.getScan()).append("\t");
			sb.append(openpsm.getPepMass()).append("\t");
			sb.append(df4.format(openpsm.getMassDiff())).append("\t");
			sb.append(openpsm.getMiss()).append("\t");

			sb.append(openpsm.getHyperscore()).append("\t");
			sb.append(openpsm.getNextscore()).append("\t");
			sb.append(openpsm.getExpect()).append("\t");

			sb.append(openpsm.getOpenModId()).append("\t");

			if (openpsm.getOpenModId() > 0) {
				int[] possible_loc = openpsm.getPossibleLoc();
				StringBuilder loc = new StringBuilder();
				for (int i = 0; i < possible_loc.length; i++) {
					loc.append(possible_loc[i]).append("_");
				}
				if (loc.length() > 0) {
					loc.deleteCharAt(loc.length() - 1);
				}
				sb.append(loc).append("\t");
			} else {
				sb.append("\t");
			}

			sb.append(openpsm.getClassScore()).append("\t");
			sb.append(df4.format(openpsm.getQValue())).append("\t");
		}

		this.writer.println(sb);
	}

	public void addPFindPSM(PFindPSM openpsm) {

		StringBuilder sb = new StringBuilder();

		String fileName = openpsm.getFileName();
		if (fileName.indexOf(".") > 0) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}

		sb.append(fileName).append("\t");
		sb.append(openpsm.getSequence()).append("\t");
		sb.append(openpsm.getModseq()).append("\t");

		sb.append(openpsm.getCalMH() - AminoAcidProperty.PROTON_W).append("\t");
		sb.append(df2.format(openpsm.getRt())).append("\t");
		sb.append(openpsm.getCharge()).append("\t");
		sb.append(openpsm.getProtein()).append("\t");
		sb.append(openpsm.getRawScore()).append("\t");
		sb.append(openpsm.getqValue()).append("\t");
		sb.append(openpsm.getExpMH() - AminoAcidProperty.PROTON_W).append("\t");
		sb.append(openpsm.getMassShift()).append("\t");
		sb.append(openpsm.getMiss());
		this.writer.println(sb);
	}
	
	public void close() {
		this.writer.close();
	}
}
