/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class ProteinInferenceReader {
	
	/** logger for this class */
	private static final Logger LOGGER = Logger.getLogger(ProteinInferenceReader.class);
	
	private static String[] proteinTitle = new String[] { "accessions", "score", "#peptides", "#PSMs", "#spectra" };

	private static String[] peptideTitle = new String[] { "sequence", "accessions", "#spectra", "#PSMs sets", "unique",
			"best_psm_combined_fdr_score", "best_xtandem_hyperscore", "best_xtandem_expect", "best_percolator_score",
			"best_percolator_q_value", "best_psm_fdr_score" };

	private static String[] proteinTitle1360 = new String[] { "COLS_PROTEIN", "Proteins", "Score", "Coverages",
			"nrPeptides", "nrPSM", "nrSpectra", "ClusterID", "Description" };

	private static String[] peptideTitle1360 = new String[] { "COLS_PEPTIDE", "Sequence", "Accessions", "nrSpectra",
			"nrPSMSets", "Missed Cleavages", "best scores", "score names", "score shorts" };

	public static InferedProtein[] getAllProteins(String input) {
		return getAllProteins(new File(input));
	}

	public static InferedProtein[] getAllProteins(File input) {
		ArrayList<InferedProtein> list = new ArrayList<InferedProtein>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from "+input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(proteinTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from "+input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String acc = record.get(proteinTitle[0]);
			String sss = record.get(proteinTitle[1]);

			double score = 0;
			if (sss.equalsIgnoreCase("Infinity")) {
				score = Double.MAX_VALUE;
			} else {
				score = Double.parseDouble(sss);
			}
			int pepcount = Integer.parseInt(record.get(proteinTitle[2]));
			int psmcount = Integer.parseInt(record.get(proteinTitle[3]));
			int spcount = Integer.parseInt(record.get(proteinTitle[4]));
			list.add(new InferedProtein(acc, score, pepcount, psmcount, spcount));
		}

		InferedProtein[] proteins = list.toArray(new InferedProtein[list.size()]);
		return proteins;
	}
	
	public static InferedProtein[] getAllProteins1360(String input) throws IOException {
		return getAllProteins1360(new File(input));
	}

	public static InferedProtein[] getAllProteins1360(File input) throws IOException {
		ArrayList<InferedProtein> list = new ArrayList<InferedProtein>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(proteinTitle1360).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();

			String att = record.get(proteinTitle1360[0]);
			if (!att.equals("PROTEIN")) {
				continue;
			}

			String acc = record.get(proteinTitle1360[1]);
			if (acc.indexOf(";") > 0) {
				acc = acc.substring(0, acc.indexOf(";"));
			}
			String sss = record.get(proteinTitle1360[2]);

			double score = 0;
			if (sss.equalsIgnoreCase("Infinity")) {
				score = Double.MAX_VALUE;
			} else {
				score = Double.parseDouble(sss);
			}
			int pepcount = Integer.parseInt(record.get(proteinTitle1360[4]));
			int psmcount = Integer.parseInt(record.get(proteinTitle1360[5]));
			int spcount = Integer.parseInt(record.get(proteinTitle1360[6]));
			list.add(new InferedProtein(acc, score, pepcount, psmcount, spcount));
		}

		InferedProtein[] proteins = list.toArray(new InferedProtein[list.size()]);
		return proteins;
	}

	public static InferedProtein[] getProteins(String input, double fdrThres) {
		return getProteins(new File(input), fdrThres);
	}

	public static InferedProtein[] getProteins(File input, double fdrThres) {
		ArrayList<InferedProtein> list = new ArrayList<InferedProtein>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from "+input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(proteinTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred proteins from "+input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String acc = record.get(proteinTitle[0]);
			String sss = record.get(proteinTitle[1]);

			double score = 0;
			if (sss.equalsIgnoreCase("Infinity")) {
				score = Double.MAX_VALUE;
			} else {
				score = Double.parseDouble(sss);
			}
			int pepcount = Integer.parseInt(record.get(proteinTitle[2]));
			int psmcount = Integer.parseInt(record.get(proteinTitle[3]));
			int spcount = Integer.parseInt(record.get(proteinTitle[4]));
			list.add(new InferedProtein(acc, score, pepcount, psmcount, spcount));
		}

		InferedProtein[] proteins = list.toArray(new InferedProtein[list.size()]);
		Arrays.sort(proteins);

		ArrayList<InferedProtein> prolist = new ArrayList<InferedProtein>();
		int target = 0;
		int decoy = 0;
		for (int i = 0; i < proteins.length; i++) {
			if (proteins[i].getName().startsWith("REV_")) {
				decoy++;
			} else {
				target++;
			}
			double fdr = (double) decoy / (double) target;
			if (fdr > fdrThres) {
				break;
			}
			prolist.add(proteins[i]);
		}

		InferedProtein[] filteredProteins = prolist.toArray(new InferedProtein[prolist.size()]);
		return filteredProteins;
	}

	public static InferedPeptide[] getAllPeptides(String input) {
		return getAllPeptides(new File(input));
	}

	public static InferedPeptide[] getAllPeptides(File input) {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(peptideTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String seq = record.get(peptideTitle[0]);
			String acc = record.get(peptideTitle[1]);

			int spcount = Integer.parseInt(record.get(peptideTitle[2]));
			int psmcount = Integer.parseInt(record.get(peptideTitle[3]));

			boolean unique = Boolean.parseBoolean(record.get(peptideTitle[4]));
			double best_score = Double.parseDouble(record.get(peptideTitle[5]));
			double hyper_score = Double.parseDouble(record.get(peptideTitle[6]));
			double expect = Double.parseDouble(record.get(peptideTitle[7]));
			double percolator_score = Double.parseDouble(record.get(peptideTitle[8]));
			double percolator_qvalue = Double.parseDouble(record.get(peptideTitle[9]));
			double fdr_score = Double.parseDouble(record.get(peptideTitle[10]));

			list.add(new InferedPeptide(seq, acc, spcount, psmcount, unique, best_score, hyper_score, expect,
					percolator_score, percolator_qvalue, fdr_score));
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		return peptides;
	}
	
	public static InferedPeptide[] getAllPeptides1360(String input) throws IOException {
		return getAllPeptides1360(new File(input));
	}

	public static InferedPeptide[] getAllPeptides1360(File input) throws IOException {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(peptideTitle1360).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();

			String att = record.get(peptideTitle1360[0]);
			if (!att.equals("PEPTIDE")) {
				continue;
			}

			String seq = record.get(peptideTitle1360[1]);
			String acc = record.get(peptideTitle1360[2]);

			int spcount = Integer.parseInt(record.get(peptideTitle1360[3]));
			int psmcount = Integer.parseInt(record.get(peptideTitle1360[4]));

			String[] scores = record.get(peptideTitle1360[6]).split(";");
			if (scores.length == 6) {
				list.add(new InferedPeptide(seq, acc, spcount, psmcount, true, Double.parseDouble(scores[0]),
						Double.parseDouble(scores[1]), Double.parseDouble(scores[2]), Double.parseDouble(scores[3]),
						Double.parseDouble(scores[4]), Double.parseDouble(scores[5])));
			} else {
				double best_score = 0;
				double hyper_score = 0;
				double expect = 0;
				double percolator_score = 0;
				double percolator_qvalue = 0;
				double fdr_score = 0;

				String[] scoreNames = record.get(peptideTitle1360[7]).split(";");
				for (int i = 0; i < scoreNames.length; i++) {
					if (scoreNames[i].equals("PSM Combined FDR Score")) {
						best_score = Double.parseDouble(scores[i]);
					} else if (scoreNames[i].equals("X!Tandem Hyperscore")) {
						hyper_score = Double.parseDouble(scores[i]);
					} else if (scoreNames[i].equals("X!Tandem Expect")) {
						expect = Double.parseDouble(scores[i]);
					} else if (scoreNames[i].equals("percolator:score")) {
						percolator_score = Double.parseDouble(scores[i]);
					} else if (scoreNames[i].equals("percolator:Q value")) {
						percolator_qvalue = Double.parseDouble(scores[i]);
					} else if (scoreNames[i].equals("PSM FDRScore")) {
						fdr_score = Double.parseDouble(scores[i]);
					}
				}

				list.add(new InferedPeptide(seq, acc, spcount, psmcount, true, best_score, hyper_score, expect,
						percolator_score, percolator_qvalue, fdr_score));
			}
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		return peptides;
	}
	
	public static InferedPeptide[] getAllPeptidesNoPercolator(File input) {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(peptideTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String seq = record.get(peptideTitle[0]);
			String acc = record.get(peptideTitle[1]);

			int spcount = Integer.parseInt(record.get(peptideTitle[2]));
			int psmcount = Integer.parseInt(record.get(peptideTitle[3]));

			boolean unique = Boolean.parseBoolean(record.get(peptideTitle[4]));
			double best_score = Double.parseDouble(record.get(peptideTitle[5]));
			double hyper_score = Double.parseDouble(record.get(peptideTitle[6]));
			double expect = Double.parseDouble(record.get(peptideTitle[7]));
			double fdr_score = Double.parseDouble(record.get(peptideTitle[8]));

			list.add(new InferedPeptide(seq, acc, spcount, psmcount, unique, best_score, hyper_score, expect, 0, 0,
					fdr_score));
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		return peptides;
	}
	
	public static InferedPeptide[] getAllPeptidesNoPercolator1360(File input) {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(proteinTitle1360).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String seq = record.get(peptideTitle[0]);
			String acc = record.get(peptideTitle[1]);

			int spcount = Integer.parseInt(record.get(peptideTitle[2]));
			int psmcount = Integer.parseInt(record.get(peptideTitle[3]));

			boolean unique = Boolean.parseBoolean(record.get(peptideTitle[4]));
			double best_score = Double.parseDouble(record.get(peptideTitle[5]));
			double hyper_score = Double.parseDouble(record.get(peptideTitle[6]));
			double expect = Double.parseDouble(record.get(peptideTitle[7]));
			double fdr_score = Double.parseDouble(record.get(peptideTitle[8]));

			list.add(new InferedPeptide(seq, acc, spcount, psmcount, unique, best_score, hyper_score, expect, 0, 0,
					fdr_score));
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		return peptides;
	}
	
	public static InferedPeptide[] getAllPeptidesWithMod(String input) {
		return getAllPeptidesWithMod(new File(input));
	}

	public static InferedPeptide[] getAllPeptidesWithMod(File input) {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(peptideTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String seq = record.get(peptideTitle[0]);
			String acc = record.get(peptideTitle[1]);

			int spcount = Integer.parseInt(record.get(peptideTitle[2]));
			int psmcount = Integer.parseInt(record.get(peptideTitle[3]));

			boolean unique = Boolean.parseBoolean(record.get(peptideTitle[4]));
			double best_score = Double.parseDouble(record.get(peptideTitle[5]));
			double hyper_score = Double.parseDouble(record.get(peptideTitle[6]));
			double expect = Double.parseDouble(record.get(peptideTitle[7]));
			double percolator_score = Double.parseDouble(record.get(peptideTitle[8]));
			double percolator_qvalue = Double.parseDouble(record.get(peptideTitle[9]));
			double fdr_score = Double.parseDouble(record.get(peptideTitle[10]));

			list.add(new InferedPeptide(seq, acc, spcount, psmcount, unique, best_score, hyper_score, expect,
					percolator_score, percolator_qvalue, fdr_score));
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		return peptides;
	}

	public static InferedPeptide[] getPeptides(String input, double fdrThres) {
		return getPeptides(new File(input), fdrThres);
	}

	public static InferedPeptide[] getPeptides(File input, double fdrThres) {

		ArrayList<InferedPeptide> list = new ArrayList<InferedPeptide>();
		FileReader reader = null;
		try {
			reader = new FileReader(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.RFC4180.withHeader(peptideTitle).parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading inferred peptides from " + input, e);
		}
		Iterator<CSVRecord> it = records.iterator();
		it.next();
		while (it.hasNext()) {
			CSVRecord record = it.next();
			String seq = record.get(peptideTitle[0]);
			String acc = record.get(peptideTitle[1]);

			int spcount = Integer.parseInt(record.get(peptideTitle[2]));
			int psmcount = Integer.parseInt(record.get(peptideTitle[3]));

			boolean unique = Boolean.parseBoolean(record.get(peptideTitle[4]));
			double best_score = Double.parseDouble(record.get(peptideTitle[5]));
			double hyper_score = Double.parseDouble(record.get(peptideTitle[6]));
			double expect = Double.parseDouble(record.get(peptideTitle[7]));
			double percolator_score = Double.parseDouble(record.get(peptideTitle[8]));
			double percolator_qvalue = Double.parseDouble(record.get(peptideTitle[9]));
			double fdr_score = Double.parseDouble(record.get(peptideTitle[10]));

			list.add(new InferedPeptide(seq, acc, spcount, psmcount, unique, best_score, hyper_score, expect,
					percolator_score, percolator_qvalue, fdr_score));
		}

		InferedPeptide[] peptides = list.toArray(new InferedPeptide[list.size()]);
		Arrays.sort(peptides);

		ArrayList<InferedPeptide> peplist = new ArrayList<InferedPeptide>();
		int target = 0;
		int decoy = 0;
		for (int i = 0; i < peptides.length; i++) {
			if (peptides[i].getAccession().startsWith("REV_")) {
				decoy++;
			} else {
				target++;
			}
			double fdr = (double) decoy / (double) target;
			if (fdr > 0.01) {
				break;
			}
			peplist.add(peptides[i]);
		}

		InferedPeptide[] filteredPeptides = peplist.toArray(new InferedPeptide[peplist.size()]);
		return filteredPeptides;
	}

	public static String[] getProteinTitle() {
		return proteinTitle;
	}

	public static String[] getPeptideTitle() {
		return peptideTitle;
	}
	
	public static String[] getProteinTitle1360() {
		return proteinTitle;
	}

	public static String[] getPeptideTitle1360() {
		return peptideTitle;
	}
}
