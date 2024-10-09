/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.function.COGFinder;
import bmi.med.uOttawa.metalab.core.function.CategoryFinder;
import bmi.med.uOttawa.metalab.core.function.GOBPFinder;
import bmi.med.uOttawa.metalab.core.function.GOCCFinder;
import bmi.med.uOttawa.metalab.core.function.GOMFFinder;
import bmi.med.uOttawa.metalab.core.function.KEGGFinder;
import bmi.med.uOttawa.metalab.core.function.KOFinder;
import bmi.med.uOttawa.metalab.core.function.NOGFinder;
import bmi.med.uOttawa.metalab.core.function.sql.IGCFuncSqliteSearcher;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.mod.UmModification;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPeptide;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlReader;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlWriter;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvReader;
import bmi.med.uOttawa.metalab.dbSearch.psm.PeptideModification;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipLCAResult;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipeptRequester;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.io.MetaAlgorithm;
import bmi.med.uOttawa.metalab.task.io.MetaBiomJsonHandler;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLReader;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLWriter;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinXMLReader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinXMLWriter;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLWriter2;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class OpenExportTask extends MetaAbstractTask {

	private String taskName = "Open search result export";
	private Logger LOGGER = LogManager.getLogger(OpenExportTask.class);

	private DecimalFormat df2 = FormatTool.getDF2();
	private DecimalFormat df4 = FormatTool.getDF4();

	private MetaData metadata;
	private File result;
	private File ms2_count_file;
	private File filtered_3_peps_file;
	private File filtered_3_pros_file;
	private File filtered_3_psms_file;
	private File filtered_3_mods_file;

	private File quan_peak_file;
	private File quan_pep_file;
	private File quan_pro_file;

	private File final_psm_file;
	private File final_pro_xml_file;
	private File final_pro_file;

	private File final_pep_file;
	private File final_pep_taxa_xml_file;
	private File final_mod_xml_file;
	private File final_mod_file;

	private File final_summary_txt;
	private File final_pep_txt;
	private File final_pro_txt;
	protected File summaryMetaFile;

	private File database;
	private File aa_around_file;

	private HashMap<String, HashMap<String, String[]>> aaWindowMap;
	private HashMap<String, Double> psmIntenMap;
	private OpenMod[] openMods;
	private HashMap<Double, PostTransModification> setModMap;
	private HashMap<Integer, double[][]> openModMatrixMap;
	private HashMap<Double, double[][]> setModMatrixMap;

	private HashMap<String, Integer> pepPsmCountMap;
	private HashMap<String, Double> pepBestScoreMap;
	private HashMap<String, HashMap<String, Object[]>> pepModInfoMap;
	private HashMap<String, String> pepSeqMap;
	private HashMap<String, String> pepProMap;
	private HashMap<String, String> proAccNameMap;
	private HashMap<String, String> expNameMap;

	private HashMap<String, Integer> proPsmCountMap;
	private HashMap<String, Integer> proPepCountMap;

	private int pepCountThres = 3;
	private static String rev = "rev_";
	private static int window = 7;

	private MetaPep2TaxaPar mptp;
	private File reportHtmlDir;

	public OpenExportTask(MetaParameterMQ metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
		this.metadata = metaPar.getMetadata();
		this.mptp = metaPar.getPtp();
	}

	public OpenExportTask(String result, String database) {
		this(new File(result), new File(database));
	}

	public OpenExportTask(File result, File database) {
		this.result = result;
		this.database = database;

		this.filtered_3_peps_file = new File(this.result, "filter3_peps.csv");
		this.filtered_3_pros_file = new File(this.result, "filter3_pros.csv");
		this.filtered_3_psms_file = new File(this.result, "filter3_psms.tsv");
		this.filtered_3_mods_file = new File(this.result, "filter3_mods.xml");

		this.quan_peak_file = new File(this.result, "QuantifiedPeaks.tsv");
		this.quan_pep_file = new File(this.result, "QuantifiedPeptides.tsv");
		this.quan_pro_file = new File(this.result, "QuantifiedProteins.tsv");

		this.final_psm_file = new File(this.result, "final_psms.csv");
		this.final_pro_xml_file = new File(this.result, "final_proteins.xml");
		this.final_pro_file = new File(this.result, "final_proteins.csv");

		this.final_pep_taxa_xml_file = new File(this.result, "final_peptide_taxa.xml");

		this.final_mod_xml_file = new File(this.result, "final_mods.xml");
		this.final_mod_file = new File(this.result, "final_mods.xlsx");

		this.aa_around_file = new File(this.result, "aminoacid_around.tsv");
	}

	private void exportMods() {

		LOGGER.info(taskName + ": export open mod information started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export open mod information started");

		BufferedReader quanPeakReader = null;
		try {
			quanPeakReader = new BufferedReader(new FileReader(quan_peak_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading quantified peaks from " + this.quan_peak_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading quantified peaks from "
					+ this.quan_peak_file.getName());
		}

		int quanPeakFileId = -1;
		int quanPeakSeqId = -1;
		int quanPeakModSeqId = -1;
		int quanPeakProId = -1;
		int quanPeakIntenId = -1;
		String line = null;
		try {
			line = quanPeakReader.readLine();
			String[] cs = line.split("\t");
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].equals("File Name")) {
					quanPeakFileId = i;
				} else if (cs[i].equals("Peak intensity")) {
					quanPeakIntenId = i;
				} else if (cs[i].equals("Full Sequence")) {
					quanPeakModSeqId = i;
				} else if (cs[i].equals("Base Sequence")) {
					quanPeakSeqId = i;
				} else if (cs[i].equals("Protein Group")) {
					quanPeakProId = i;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading quantified peaks from " + this.quan_peak_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading quantified peaks from "
					+ this.quan_peak_file.getName());
		}

		this.proAccNameMap = new HashMap<String, String>();
		this.psmIntenMap = new HashMap<String, Double>();
		this.aaWindowMap = new HashMap<String, HashMap<String, String[]>>();

		if (quanPeakFileId == -1 || quanPeakSeqId == -1 || quanPeakModSeqId == -1 || quanPeakProId == -1
				|| quanPeakIntenId == -1) {
			try {
				quanPeakReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading quantified peaks from " + this.quan_peak_file.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading quantified peaks from " + this.quan_peak_file.getName());
			}
			LOGGER.error(taskName + ": error in reading quantified peaks from " + this.quan_peak_file.getName());
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading quantified peaks from "
					+ this.quan_peak_file.getName());
		}

		try {
			while ((line = quanPeakReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] spSeqs = cs[quanPeakSeqId].split("\\|");
				String[] spModSeqs = cs[quanPeakModSeqId].split("\\|");
				String[] spPros = cs[quanPeakProId].split(";");

				double value = Double.parseDouble(cs[quanPeakIntenId]);
				if (value == 0) {
					continue;
				}

				for (int i = 0; i < spModSeqs.length; i++) {

					String key = cs[quanPeakFileId] + "_" + spModSeqs[i];

					if (psmIntenMap.containsKey(key)) {
						psmIntenMap.put(key, psmIntenMap.get(key) + value);
					} else {
						psmIntenMap.put(key, value / (double) spSeqs.length);
					}

					String prokey;
					if (i < spPros.length) {
						prokey = spPros[i].split("\\s+")[0];
					} else {
						prokey = spPros[0].split("\\s+")[0];
					}
					HashMap<String, String[]> m = aaWindowMap.get(prokey);
					if (m == null) {
						m = new HashMap<String, String[]>();
					}
					m.put(spSeqs[0], new String[] { "", "" });
					aaWindowMap.put(prokey, m);
					proAccNameMap.put(prokey, cs[quanPeakProId]);
				}
			}
			quanPeakReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading quantified peaks from " + this.quan_peak_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading quantified peaks from "
					+ this.quan_peak_file.getName());
		}

		FastaReader fr = null;
		try {
			fr = new FastaReader(database);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading protein sequences from " + this.database.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in reading protein sequences from " + this.database.getName());
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

						int nex_loc = id + sequence.length() + window;
						String nex_seq = "";
						if (nex_loc >= items[i].length()) {
							nex_seq = items[i].getPeptide(id + sequence.length(), items[i].length());
							for (int j = items[i].length(); j <= nex_loc; j++) {
								nex_seq += '_';
							}
						} else {
							nex_seq = items[i].getPeptide(id + sequence.length(), nex_loc);
						}

						m.put(sequence, new String[] { pre_seq, nex_seq });
					}
				}
			}
		}

		PrintWriter aroundWriter = null;
		try {
			aroundWriter = new PrintWriter(aa_around_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in writing previous and next aminoacids to " + this.aa_around_file.getName(),
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing previous and next aminoacids to " + this.aa_around_file.getName());
		}

		String title = "Protein,Peptide,Previous AAs,Next AAs";
		aroundWriter.println(title);

		for (String prokey : aaWindowMap.keySet()) {
			HashMap<String, String[]> m = aaWindowMap.get(prokey);
			for (String pep : m.keySet()) {
				String[] seq = m.get(pep);
				aroundWriter.println(proAccNameMap.get(prokey) + "\t" + pep + "\t" + seq[0] + "\t" + seq[1]);
			}
		}
		aroundWriter.close();

		OpenModXmlReader mreader = null;
		try {
			mreader = new OpenModXmlReader(this.filtered_3_mods_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading potential PTMs from " + this.filtered_3_mods_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading potential PTMs from "
					+ this.filtered_3_mods_file.getName());
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

		this.pepProMap = new HashMap<String, String>();

		BufferedReader filter3PsmReader = null;
		try {
			filter3PsmReader = new BufferedReader(new FileReader(filtered_3_psms_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading PSMs from " + filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
					+ filtered_3_psms_file.getName());
		}

		int quanPsmFileId = -1;
		int quanPsmSeqId = -1;
		int quanPsmModId = -1;
		int quanPsmLocId = -1;
		int quanPsmProId = -1;

		line = null;
		try {
			line = filter3PsmReader.readLine();
			String[] cs = line.split("\t");
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].equals("Raw file")) {
					quanPsmFileId = i;
				} else if (cs[i].equals("Modified sequence")) {
					quanPsmSeqId = i;
				} else if (cs[i].equals("mod_id")) {
					quanPsmModId = i;
				} else if (cs[i].equals("possible_loc")) {
					quanPsmLocId = i;
				} else if (cs[i].equals("Proteins")) {
					quanPsmProId = i;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading PSMs from " + filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
					+ filtered_3_psms_file.getName());
		}

		if (quanPsmFileId == -1 || quanPsmSeqId == -1 || quanPsmModId == -1 || quanPsmLocId == -1
				|| quanPsmProId == -1) {
			LOGGER.error(
					taskName + ": error in reading PSMs from " + filtered_3_psms_file.getName() + ", unknown format");
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
					+ filtered_3_psms_file.getName() + ", unknown format");
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

		HashMap<Integer, Integer> modPsmCountMap = new HashMap<Integer, Integer>();

		int[] quanCount = new int[2];
		HashSet<String> tempset = new HashSet<String>();
		try {
			while ((line = filter3PsmReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String fileName = cs[quanPsmFileId];
				String modseq = cs[quanPsmSeqId];
				String key = fileName + "_" + modseq;
				int modIden = Integer.parseInt(cs[quanPsmModId]);

				String prokey = cs[quanPsmProId].split("\\s+")[0];
				this.pepProMap.put(modseq, prokey);

				if (modIden != 0) {
					if (modPsmCountMap.containsKey(modIden)) {
						modPsmCountMap.put(modIden, modPsmCountMap.get(modIden) + 1);
					} else {
						modPsmCountMap.put(modIden, 1);
					}
				}

				double intensity = 0;
				if (psmIntenMap.containsKey(key)) {
					intensity = psmIntenMap.get(key);
					quanCount[0]++;
				} else {
					quanCount[1]++;
					tempset.add(modseq);
					continue;
				}

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

						if (modsb.toString().equals(cs[quanPsmModId])) {

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
				if (around == null || around[0].length() == 0 || around[1].length() == 0) {
					continue;
				}

				String fullSequence = around[0] + seqsb + around[1];

				if (modIden != 0) {
					String[] loccs = cs[quanPsmLocId].split("_");
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
					}
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
			filter3PsmReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading PSMs from " + filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
					+ filtered_3_psms_file.getName());
		}

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

		ArrayList<OpenMod> list = new ArrayList<OpenMod>();
		for (OpenMod om : openMods) {
			if (modPsmCountMap.containsKey(om.getId())) {
				list.add(om);
			}
		}
		OpenMod[] rankedMods = list.toArray(new OpenMod[list.size()]);

		Arrays.sort(rankedMods, new Comparator<OpenMod>() {
			@Override
			public int compare(OpenMod o1, OpenMod o2) {
				// TODO Auto-generated method stub

				int count1 = modPsmCountMap.containsKey(o1.getId()) ? modPsmCountMap.get(o1.getId()) : 0;
				int count2 = modPsmCountMap.containsKey(o2.getId()) ? modPsmCountMap.get(o2.getId()) : 0;

				if (count1 > count2) {
					return -1;
				} else if (count1 < count2) {
					return 1;
				}
				return 0;
			}
		});

		OpenModXmlWriter writer = null;
		try {
			writer = new OpenModXmlWriter(this.final_mod_xml_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing potential PTMs to " + final_mod_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing potential PTMs to "
					+ final_mod_xml_file.getName());
		}

		writer.addMods(rankedMods, openModMatrixMap);
		writer.addSetMods(setModMap, openMods[0], setModMatrixMap);

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing potential PTMs to " + final_mod_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing potential PTMs to "
					+ final_mod_xml_file.getName());
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont titleFont = workbook.createFont();
		titleFont.setBold(true);

		XSSFFont contentFont = workbook.createFont();
		contentFont.setBold(false);

		XSSFSheet sheet = workbook.createSheet("Potential PTM list");
		style.setFont(titleFont);

		ArrayList<String> titleList1 = new ArrayList<String>();
		titleList1.add("Id");
		titleList1.add(taskName + " experiment mod mass");
		titleList1.add("Number of PSMs");
		titleList1.add("Matched in Unimod");

		int rowCount = 0;

		XSSFRow row0 = sheet.createRow(rowCount++);
		for (int i = 0; i < titleList1.size(); i++) {
			XSSFCell cell = row0.createCell(i);
			cell.setCellValue(titleList1.get(i));
			cell.setCellStyle(style);
		}

		ArrayList<String> titleList2 = new ArrayList<String>();
		titleList2.add("");
		titleList2.add("Title");
		titleList2.add("Name");
		titleList2.add("Mono mass");
		titleList2.add("Sites");
		titleList2.add("Composition");

		XSSFRow row1 = sheet.createRow(rowCount++);
		for (int i = 0; i < titleList2.size(); i++) {
			XSSFCell cell = row1.createCell(i);
			cell.setCellValue(titleList2.get(i));
			cell.setCellStyle(style);
		}

		style.setFont(contentFont);

		for (int i = 0; i < rankedMods.length; i++) {

			int openModid = rankedMods[i].getId();

			XSSFRow row = sheet.createRow(rowCount++);
			XSSFCell[] cells = new XSSFCell[titleList1.size()];
			int id = 0;
			cells[id] = row.createCell(id);
			cells[id].setCellValue(i + 1);
			id++;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(rankedMods[i].getExpModMass());
			id++;

			int count = modPsmCountMap.get(openModid);
			cells[id] = row.createCell(id);
			cells[id].setCellValue(count);
			id++;

			UmModification[] ums = rankedMods[i].getUmmod();
			if (ums == null) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue("No");
				id++;
			} else {
				cells[id] = row.createCell(id);
				cells[id].setCellValue("Yes");
				id++;
			}

			if (ums != null) {
				for (int j = 0; j < ums.length; j++) {
					row = sheet.createRow(rowCount++);
					cells = new XSSFCell[titleList2.size()];
					id = 0;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(ums[j].getTitle());
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(ums[j].getName());
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(ums[j].getMono_mass());
					id++;

					String[] sites = ums[j].getSpecificity();
					StringBuilder sb = new StringBuilder();
					for (int k = 0; k < sites.length; k++) {
						sb.append(sites[k]).append(";");
					}
					sb.deleteCharAt(sb.length() - 1);

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb.toString());
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(ums[j].getComposition());
					id++;
				}
			}

			if (openModMatrixMap.containsKey(openModid)) {

				double[][] matrix = openModMatrixMap.get(openModid);

				row = sheet.createRow(rowCount++);
				cells = new XSSFCell[window * 2 + 3];
				id = 0;

				cells[id] = row.createCell(id);
				cells[id].setCellValue("");
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue("Motif information");
				id++;

				for (int j = -window; j <= window; j++) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(j);
					id++;
				}

				for (char j = 'A'; j <= 'Z'; j++) {
					row = sheet.createRow(rowCount++);
					cells = new XSSFCell[window * 2 + 3];
					id = 0;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(String.valueOf(j));
					id++;

					for (int k = 0; k < matrix[j - 'A'].length; k++) {
						cells[id] = row.createCell(id);
						cells[id].setCellValue(matrix[j - 'A'][k]);
						id++;
					}
				}
			}
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(final_mod_file);
			workbook.write(out);
			workbook.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing potential PTMs to " + final_mod_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing potential PTMs to "
					+ final_mod_file.getName());
		}

		LOGGER.info(taskName + ": open mod information has been exported to " + final_mod_file);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": open mod information has been exported to "
				+ final_mod_file);
	}

	private void exportOpenPSMs() {

		LOGGER.info(taskName + ": export PSMs started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export PSMs started");

		UmModification[] possiUms = new UmModification[this.openMods.length];
		String[] possiSite = new String[this.openMods.length];
		L: for (Integer key : this.openModMatrixMap.keySet()) {
			UmModification[] ums = openMods[key].getUmmod();
			if (ums != null) {
				double[][] matrix = this.openModMatrixMap.get(key);
				double max = 0;
				int maxId = -1;
				for (int i = 0; i < matrix.length; i++) {
					if (matrix[i][window] > max) {
						max = matrix[i][window];
						maxId = i;
					}
				}

				String site = String.valueOf((char) (maxId + 'A'));
				for (int i = 0; i < ums.length; i++) {
					String[] sites = ums[i].getSpecificity();
					for (int j = 0; j < sites.length; j++) {
						if (site.equals(sites[j])) {
							possiUms[key] = ums[i];
							possiSite[key] = site;
							continue L;
						}
					}
				}
				possiUms[key] = ums[0];
			}
		}

		OpenPsmTsvReader reader = null;
		try {
			reader = new OpenPsmTsvReader(this.filtered_3_psms_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading psms from " + filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading psms from "
					+ filtered_3_psms_file.getName());
		}

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter psmCsvPrinter = null;
		Object[] title = new Object[15];
		try {
			psmCsvPrinter = new CSVPrinter(new FileWriter(this.final_psm_file), csvFileFormat);
			title[0] = "Raw file";
			title[1] = "Scan number";
			title[2] = "Sequence";
			title[3] = "Variable mods";
			title[4] = "Open mod mass";
			title[5] = "Possible open mod name";
			title[6] = "Possible open mod site";
			title[7] = "Precursor mass";
			title[8] = "Retention time";
			title[9] = "Charge";
			title[10] = "Proteins";
			title[11] = "Peptide mass";
			title[12] = "Mass difference";
			title[13] = "Missed cleavages";
			title[14] = "Q value";
			psmCsvPrinter.printRecord(title);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		this.proPsmCountMap = new HashMap<String, Integer>();
		this.pepPsmCountMap = new HashMap<String, Integer>();
		this.pepBestScoreMap = new HashMap<String, Double>();
		this.pepModInfoMap = new HashMap<String, HashMap<String, Object[]>>();
		this.pepSeqMap = new HashMap<String, String>();

		HashMap<String, Integer> filePsmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();

		OpenPSM[] psms = reader.getAllOpenPSMs();

		for (OpenPSM psm : psms) {

			Object[] content = new Object[title.length];

			String fileName = psm.getFileName();

			content[0] = fileName;
			content[1] = psm.getScan();
			content[2] = psm.getSequence();

			String sequence = psm.getSequence();

			if (filePsmCountMap.containsKey(fileName)) {
				filePsmCountMap.put(fileName, filePsmCountMap.get(fileName) + 1);
				HashSet<String> set = fileUnipepMap.get(fileName);
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			} else {
				filePsmCountMap.put(fileName, 1);
				HashSet<String> set = new HashSet<String>();
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			}

			PeptideModification[] ptms = psm.getPTMs();
			if (ptms == null || ptms.length == 0) {
				content[3] = "";
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < ptms.length; i++) {
					int loc = ptms[i].getLoc();
					double mass = ptms[i].getMass();

					if (loc == 0) {
						sb.append(mass).append("@n-term").append(";");
					} else {
						sb.append(mass).append("@").append(sequence.charAt(loc - 1)).append(loc).append(";");
					}
				}
				content[3] = sb.deleteCharAt(sb.length() - 1).toString();
			}

			int modId = psm.getOpenModId();
			int[] possiLoc = psm.getPossibleLoc();
			if (modId == 0) {
				content[4] = "";
				content[5] = "";
				content[6] = "";
			} else {
				content[4] = openMods[modId].getExpModMass();
				if (possiUms[modId] == null) {
					content[5] = "Unknown";
				} else {
					content[5] = possiUms[modId].getName();
					String site = null;
					for (int i = 0; i < possiLoc.length; i++) {
						String sitei = String.valueOf(sequence.charAt(possiLoc[i]));
						if (sitei.equals(possiSite[modId])) {
							site = sitei + (possiLoc[i] + 1);
							break;
						}
					}
					if (site == null) {
						content[6] = "Unknown";
					} else {
						content[6] = site;
					}
				}
			}

			content[7] = psm.getPrecursorMr();
			content[8] = psm.getRt();
			content[9] = psm.getCharge();
			content[10] = psm.getProtein();
			content[11] = psm.getPepMass();
			content[12] = psm.getMassDiff();
			content[13] = psm.getMiss();
			content[14] = psm.getQValue();

			try {
				psmCsvPrinter.printRecord(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in writing PSMs to " + this.final_psm_file.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSMs to "
						+ this.final_psm_file.getName());
			}

			String pro = psm.getProtein();
			pro = pro.split("[\\s+]")[0];

			if (proPsmCountMap.containsKey(pro)) {
				proPsmCountMap.put(pro, proPsmCountMap.get(pro) + 1);
			} else {
				proPsmCountMap.put(pro, 1);
			}

			String modseq = psm.getModSequence();
			if (pepPsmCountMap.containsKey(modseq)) {
				pepPsmCountMap.put(modseq, pepPsmCountMap.get(modseq) + 1);
			} else {
				pepPsmCountMap.put(modseq, 1);
			}

			if (pepBestScoreMap.containsKey(modseq)) {
				if (psm.getQValue() < pepBestScoreMap.get(modseq)) {
					pepBestScoreMap.put(modseq, psm.getQValue());
				}
			} else {
				pepBestScoreMap.put(modseq, psm.getQValue());
			}

			pepSeqMap.put(modseq, sequence);

			if (pepModInfoMap.containsKey(sequence)) {
				HashMap<String, Object[]> map = pepModInfoMap.get(sequence);
				if (!map.containsKey(modseq)) {
					map.put(modseq, new Object[] { content[3], content[4], content[5], content[6] });
				}
			} else {
				HashMap<String, Object[]> map = new HashMap<String, Object[]>();
				map.put(modseq, new Object[] { content[3], content[4], content[5], content[6] });
				pepModInfoMap.put(sequence, map);
			}
		}

		try {
			psmCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		this.final_summary_txt = new File(this.result, "final_summary.txt");

		BufferedReader ms2CountReader = null;
		try {
			ms2CountReader = new BufferedReader(new FileReader(this.ms2_count_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PSM information to " + ms2_count_file, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSM information to "
					+ ms2_count_file);
		}

		try {

			PrintWriter countWriter = new PrintWriter(this.final_summary_txt);
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Raw file").append("\t");
			titlesb.append(taskName + " experiment").append("\t");
			titlesb.append("MS/MS").append("\t");
			titlesb.append("MS/MS Identified").append("\t");
			titlesb.append("MS/MS Identified [%]").append("\t");
			titlesb.append("Peptide Sequences Identified");

			countWriter.println(titlesb);

			int totalms2count = 0;
			int totalpsmCount = 0;
			int totalUnipepCount = 0;

			String line = ms2CountReader.readLine();

			while ((line = ms2CountReader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (filePsmCountMap.containsKey(cs[0]) && fileUnipepMap.containsKey(cs[0])) {

					StringBuilder sb = new StringBuilder();
					sb.append(cs[0]).append("\t");

					if (this.expNameMap.containsKey(cs[0])) {
						sb.append(expNameMap.get(cs[0])).append("\t");
					} else {
						sb.append(cs[0]).append("\t");
					}

					int ms2count = Integer.parseInt(cs[1]);
					int psmCount = filePsmCountMap.get(cs[0]);

					sb.append(ms2count).append("\t");
					sb.append(psmCount).append("\t");

					double ratio = 0;
					if (ms2count > 0) {
						ratio = (double) psmCount / (double) ms2count * 100.0;
					}

					sb.append(df2.format(ratio)).append("\t");

					int unipepCount = fileUnipepMap.get(cs[0]).size();
					sb.append(unipepCount);

					countWriter.println(sb);

					totalms2count += ms2count;
					totalpsmCount += psmCount;
					totalUnipepCount += unipepCount;
				}
			}

			ms2CountReader.close();

			StringBuilder sb = new StringBuilder();
			sb.append("Total").append("\t");
			sb.append("\t");
			sb.append(totalms2count).append("\t");
			sb.append(totalpsmCount).append("\t");

			double ratio = 0;
			if (totalms2count > 0) {
				ratio = (double) totalpsmCount / (double) totalms2count * 100.0;
			}

			sb.append(df2.format(ratio)).append("\t");

			sb.append(totalUnipepCount);

			countWriter.print(sb);

			countWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PSM information to " + final_summary_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSM information to "
					+ final_summary_txt.getName());
		}

		LOGGER.info(taskName + ": PSMs has been exported to " + final_psm_file);
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": PSMs has been exported to " + final_psm_file);
	}

	private void exportClosedPSMs() {

		LOGGER.info(taskName + ": export PSMs started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export PSMs started");

		OpenPsmTsvReader reader = null;
		try {
			reader = new OpenPsmTsvReader(this.filtered_3_psms_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading psms from " + filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading psms from "
					+ filtered_3_psms_file.getName());
		}

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter psmCsvPrinter = null;
		Object[] title = new Object[15];
		try {
			psmCsvPrinter = new CSVPrinter(new FileWriter(this.final_psm_file), csvFileFormat);
			title[0] = "Raw file";
			title[1] = "Scan number";
			title[2] = "Sequence";
			title[3] = "Variable mods";
			title[4] = "Precursor mass";
			title[5] = "Retention time";
			title[6] = "Charge";
			title[7] = "Proteins";
			title[8] = "Peptide mass";
			title[9] = "Mass difference";
			title[10] = "Missed cleavages";
			title[11] = "Hyper score";
			psmCsvPrinter.printRecord(title);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading psms from "
					+ filtered_3_psms_file.getName());
		}

		this.pepProMap = new HashMap<String, String>();
		this.proAccNameMap = new HashMap<String, String>();
		this.proPsmCountMap = new HashMap<String, Integer>();
		this.pepPsmCountMap = new HashMap<String, Integer>();
		this.pepBestScoreMap = new HashMap<String, Double>();
		this.pepModInfoMap = new HashMap<String, HashMap<String, Object[]>>();
		this.pepSeqMap = new HashMap<String, String>();

		HashMap<String, Integer> filePsmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();

		OpenPSM[] psms = reader.getAllOpenPSMs();

		for (OpenPSM psm : psms) {

			Object[] content = new Object[title.length];

			String fileName = psm.getFileName();

			content[0] = fileName;
			content[1] = psm.getScan();
			content[2] = psm.getSequence();

			String sequence = psm.getSequence();

			if (filePsmCountMap.containsKey(fileName)) {
				filePsmCountMap.put(fileName, filePsmCountMap.get(fileName) + 1);
				HashSet<String> set = fileUnipepMap.get(fileName);
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			} else {
				filePsmCountMap.put(fileName, 1);
				HashSet<String> set = new HashSet<String>();
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			}

			PeptideModification[] ptms = psm.getPTMs();
			if (ptms == null || ptms.length == 0) {
				content[3] = "";
			} else {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < ptms.length; i++) {
					int loc = ptms[i].getLoc();
					double mass = ptms[i].getMass();

					if (loc == 0) {
						sb.append(mass).append("@n-term").append(";");
					} else {
						sb.append(mass).append("@").append(sequence.charAt(loc - 1)).append(loc).append(";");
					}
				}
				content[3] = sb.deleteCharAt(sb.length() - 1).toString();
			}

			content[4] = psm.getPrecursorMr();
			content[5] = psm.getRt();
			content[6] = psm.getCharge();
			content[7] = psm.getProtein();
			content[8] = psm.getPepMass();
			content[9] = psm.getMassDiff();
			content[10] = psm.getMiss();
			content[11] = psm.getHyperscore();

			try {
				psmCsvPrinter.printRecord(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + " error in writing PSMs to " + this.final_psm_file.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
						+ this.final_psm_file.getName());
			}

			String pro = psm.getProtein();
			pro = pro.split("[\\s+]")[0];

			if (proPsmCountMap.containsKey(pro)) {
				proPsmCountMap.put(pro, proPsmCountMap.get(pro) + 1);
			} else {
				proPsmCountMap.put(pro, 1);
			}

			proAccNameMap.put(pro, psm.getProtein());

			String modseq = psm.getModSequence();
			if (pepPsmCountMap.containsKey(modseq)) {
				pepPsmCountMap.put(modseq, pepPsmCountMap.get(modseq) + 1);
			} else {
				pepPsmCountMap.put(modseq, 1);
			}

			pepProMap.put(modseq, pro);

			if (pepBestScoreMap.containsKey(modseq)) {
				if (psm.getQValue() < pepBestScoreMap.get(modseq)) {
					pepBestScoreMap.put(modseq, psm.getQValue());
				}
			} else {
				pepBestScoreMap.put(modseq, psm.getQValue());
			}

			pepSeqMap.put(modseq, sequence);

			if (pepModInfoMap.containsKey(sequence)) {
				HashMap<String, Object[]> map = pepModInfoMap.get(sequence);
				if (!map.containsKey(modseq)) {
					map.put(modseq, new Object[] { content[3] });
				}
			} else {
				HashMap<String, Object[]> map = new HashMap<String, Object[]>();
				map.put(modseq, new Object[] { content[3] });
				pepModInfoMap.put(sequence, map);
			}
		}

		try {
			psmCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		this.final_summary_txt = new File(this.result, "final_summary.txt");

		BufferedReader ms2CountReader = null;
		try {
			ms2CountReader = new BufferedReader(new FileReader(this.ms2_count_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSM information to " + ms2_count_file, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSM information to "
					+ ms2_count_file);
		}

		try {

			PrintWriter countWriter = new PrintWriter(this.final_summary_txt);
			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Raw file").append("\t");
			titlesb.append(taskName + " experiment").append("\t");
			titlesb.append("MS/MS").append("\t");
			titlesb.append("MS/MS Identified").append("\t");
			titlesb.append("MS/MS Identified [%]").append("\t");
			titlesb.append("Peptide Sequences Identified");

			countWriter.println(titlesb);

			int totalms2count = 0;
			int totalpsmCount = 0;
			int totalUnipepCount = 0;

			String line = ms2CountReader.readLine();

			while ((line = ms2CountReader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (filePsmCountMap.containsKey(cs[0]) && fileUnipepMap.containsKey(cs[0])) {

					StringBuilder sb = new StringBuilder();
					sb.append(cs[0]).append("\t");

					if (this.expNameMap.containsKey(cs[0])) {
						sb.append(expNameMap.get(cs[0])).append("\t");
					} else {
						sb.append(cs[0]).append("\t");
					}

					int ms2count = Integer.parseInt(cs[1]);
					int psmCount = filePsmCountMap.get(cs[0]);

					sb.append(ms2count).append("\t");
					sb.append(psmCount).append("\t");

					double ratio = 0;
					if (ms2count > 0) {
						ratio = (double) psmCount / (double) ms2count * 100.0;
					}

					sb.append(df2.format(ratio)).append("\t");

					int unipepCount = fileUnipepMap.get(cs[0]).size();
					sb.append(unipepCount);

					countWriter.println(sb);

					totalms2count += ms2count;
					totalpsmCount += psmCount;
					totalUnipepCount += unipepCount;
				}
			}

			ms2CountReader.close();

			StringBuilder sb = new StringBuilder();
			sb.append("Total").append("\t");
			sb.append("\t");
			sb.append(totalms2count).append("\t");
			sb.append(totalpsmCount).append("\t");

			double ratio = 0;
			if (totalms2count > 0) {
				ratio = (double) totalpsmCount / (double) totalms2count * 100.0;
			}

			sb.append(df2.format(ratio)).append("\t");

			sb.append(totalUnipepCount);

			countWriter.print(sb);

			countWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSM information to " + final_summary_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSM information to "
					+ final_summary_txt.getName());
		}

		if (!final_summary_txt.exists() || final_summary_txt.length() == 0) {
			LOGGER.error(taskName + " error in writing PSM information to " + final_summary_txt.getName());
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSM information to "
					+ final_summary_txt.getName());
			return;

		} else {
			File report_ID_summary = new File(reportHtmlDir, "report_ID_summary.html");
			if (!report_ID_summary.exists() || report_ID_summary.length() == 0) {

				MetaReportTask task = new MetaReportTask(1);
				task.addTask(MetaReportTask.summary, final_summary_txt, report_ID_summary);
				task.run();
			}
		}

		LOGGER.info(taskName + ": PSMs has been exported to " + final_psm_file);
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": PSMs has been exported to " + final_psm_file);
	}

	private void exportOpenPeptideCsv() {

		LOGGER.info(taskName + ": export peptides started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export peptides started");

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
		Arrays.sort(sequences);

		this.final_pep_file = new File(this.result, "final_peptides.csv");

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter pepCsvPrinter = null;
		Object[] title = new Object[expNames.length + 9];
		try {
			pepCsvPrinter = new CSVPrinter(new FileWriter(this.final_pep_file), csvFileFormat);
			title[0] = "Id";
			title[1] = "Sequence";
			title[2] = "Variable mods";
			title[3] = "Open mod mass";
			title[4] = "Possible open mod name";
			title[5] = "Possible open mod site";
			title[6] = "PSM count";
			title[7] = "P value";
			title[8] = "Protein";

			for (int i = 0; i < expNames.length; i++) {
				title[i + 9] = expNames[i];
			}

			pepCsvPrinter.printRecord(title);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		this.proPepCountMap = new HashMap<String, Integer>();

		for (int i = 0; i < sequences.length; i++) {

			HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

			String[] modseqs = map.keySet().toArray(new String[map.size()]);
			Arrays.sort(modseqs);

			double[] scores = new double[modseqs.length];
			int[] psmCounts = new int[modseqs.length];
			double[][] intensities = new double[modseqs.length][];

			String[] variMods = new String[modseqs.length];
			double[] opModMass = new double[modseqs.length];
			String[] opModName = new String[modseqs.length];
			String[] opModSite = new String[modseqs.length];

			for (int j = 0; j < modseqs.length; j++) {

				String proAcc = this.pepProMap.get(modseqs[j]);
				if (proAcc == null) {
					LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
							+ this.filtered_3_psms_file);
					continue;
				}

				String pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

				Object[] objs = map.get(modseqs[j]);
				variMods[j] = (String) objs[0];
				opModMass[j] = 0;
				opModName[j] = (String) objs[2];
				opModSite[j] = (String) objs[3];

				if (opModName[j].length() > 0) {
					opModMass[j] = (double) objs[1];
				}

				if (proPepCountMap.containsKey(proAcc)) {
					proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
				} else {
					proPepCountMap.put(proAcc, 1);
				}

				Object[] contents = new Object[title.length];

				if (j == 0) {
					contents[0] = (i + 1);
				} else {
					contents[0] = "";
				}

				contents[1] = sequences[i];
				contents[2] = variMods[j];

				if (opModName[j].length() > 0) {
					contents[3] = opModMass[j];
				} else {
					contents[3] = "";
				}

				contents[4] = opModName[j];
				contents[5] = opModSite[j];
				contents[6] = pepPsmCountMap.get(modseqs[j]);
				contents[7] = pepBestScoreMap.get(modseqs[j]);
				contents[8] = pro;

				intensities[j] = valueMap.get(modseqs[j]);
				for (int k = 0; k < intensities[j].length; k++) {
					contents[k + 9] = intensities[j][k];
				}

				scores[j] = pepBestScoreMap.get(modseqs[j]);
				psmCounts[j] = pepPsmCountMap.get(modseqs[j]);

				try {
					pepCsvPrinter.printRecord(contents);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + " error in writing peptides to " + this.final_psm_file.getName(), e);
					System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing peptides to "
							+ this.final_psm_file.getName());
				}
			}
		}

		try {
			pepCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing peptides to " + this.final_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing peptides to "
					+ this.final_psm_file.getName());
		}

		LOGGER.info(taskName + ": peptides has been exported to " + final_pep_file);
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": peptides has been exported to " + final_pep_file);
	}

	private void exportClosedPeptideCsv() {

		LOGGER.info(taskName + ": export peptides started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export peptides started");

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
		Arrays.sort(sequences);

		this.final_pep_file = new File(this.result, "final_peptides.csv");

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter pepCsvPrinter = null;
		Object[] title = new Object[expNames.length + 9];
		try {
			pepCsvPrinter = new CSVPrinter(new FileWriter(this.final_pep_file), csvFileFormat);
			title[0] = "Id";
			title[1] = "Sequence";
			title[2] = "Variable mods";
			title[3] = "PSM count";
			title[4] = "P value";
			title[5] = "Protein";

			for (int i = 0; i < expNames.length; i++) {
				title[i + 6] = expNames[i];
			}

			pepCsvPrinter.printRecord(title);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing PSMs to " + this.final_psm_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		this.proPepCountMap = new HashMap<String, Integer>();

		for (int i = 0; i < sequences.length; i++) {

			HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

			String[] modseqs = map.keySet().toArray(new String[map.size()]);
			Arrays.sort(modseqs);

			double[] scores = new double[modseqs.length];
			int[] psmCounts = new int[modseqs.length];
			double[][] intensities = new double[modseqs.length][];

			String[] variMods = new String[modseqs.length];

			for (int j = 0; j < modseqs.length; j++) {

				String proAcc = this.pepProMap.get(modseqs[j]);
				if (proAcc == null) {
					LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
							+ this.filtered_3_psms_file);
					continue;
				}

				String pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

				Object[] objs = map.get(modseqs[j]);
				variMods[j] = (String) objs[0];

				if (proPepCountMap.containsKey(proAcc)) {
					proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
				} else {
					proPepCountMap.put(proAcc, 1);
				}

				Object[] contents = new Object[title.length];

				if (j == 0) {
					contents[0] = (i + 1);
				} else {
					contents[0] = "";
				}

				contents[1] = sequences[i];
				contents[2] = variMods[j];

				contents[3] = pepPsmCountMap.get(modseqs[j]);
				contents[4] = pepBestScoreMap.get(modseqs[j]);
				contents[5] = pro;

				intensities[j] = valueMap.get(modseqs[j]);
				for (int k = 0; k < intensities[j].length; k++) {
					contents[k + 6] = intensities[j][k];
				}

				scores[j] = pepBestScoreMap.get(modseqs[j]);
				psmCounts[j] = pepPsmCountMap.get(modseqs[j]);

				try {
					pepCsvPrinter.printRecord(contents);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + " error in writing peptides to " + this.final_psm_file.getName(), e);
					System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
							+ this.final_psm_file.getName());
				}
			}
		}

		try {
			pepCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing peptides to " + this.final_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing PSMs to "
					+ this.final_psm_file.getName());
		}

		LOGGER.info(taskName + ": peptides has been exported to " + final_pep_file);
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": peptides has been exported to " + final_pep_file);
	}

	private void exportPeptideTxt() {

		LOGGER.info(taskName + ": export peptide report started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export peptide report started");

		OpenPsmTsvReader psmReader = null;
		try {
			psmReader = new OpenPsmTsvReader(this.filtered_3_psms_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading PSMs from " + this.filtered_3_psms_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading PSMs from "
					+ this.filtered_3_psms_file.getName());
		}

		HashMap<String, Integer> countmap = new HashMap<String, Integer>();
		HashMap<String, OpenPSM> psmmap = new HashMap<String, OpenPSM>();
		HashMap<String, int[]> chargemap = new HashMap<String, int[]>();
		OpenPSM[] finalPSMs = psmReader.getAllOpenPSMs();
		for (int i = 0; i < finalPSMs.length; i++) {
			String sequence = finalPSMs[i].getSequence();
			if (psmmap.containsKey(sequence)) {
				countmap.put(sequence, countmap.get(sequence) + 1);
				if (finalPSMs[i].getHyperscore() > psmmap.get(sequence).getHyperscore()) {
					psmmap.put(sequence, finalPSMs[i]);
				}
				int[] charge = chargemap.get(sequence);
				if (finalPSMs[i].getCharge() > 0 && finalPSMs[i].getCharge() < 10) {
					charge[finalPSMs[i].getCharge() - 1] = 1;
				}
				chargemap.put(sequence, charge);
			} else {
				psmmap.put(sequence, finalPSMs[i]);
				countmap.put(sequence, 1);

				int[] charge = new int[9];
				Arrays.fill(charge, 0);

				if (finalPSMs[i].getCharge() > 0 && finalPSMs[i].getCharge() < 10) {
					charge[finalPSMs[i].getCharge() - 1] = 1;
				}

				chargemap.put(sequence, charge);
			}
		}

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading PSMs from "
					+ this.quan_pep_file.getName());
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(this.final_pep_txt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing peptides to " + this.final_pep_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing peptides to "
					+ this.final_pep_txt.getName());
		}

		String[] expNames = null;
		try {

			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Base Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			HashMap<String, double[]> intensityMap = new HashMap<String, double[]>();
			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}

				if (intensityMap.containsKey(cs[seqId])) {
					double[] total = intensityMap.get(cs[seqId]);
					for (int i = 0; i < intensity.length; i++) {
						total[i] += intensity[i];
					}
					intensityMap.put(cs[seqId], total);
				} else {
					intensityMap.put(cs[seqId], intensity);
				}
			}
			quanPepReader.close();

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Sequence").append("\t");
			titlesb.append("Length").append("\t");
			titlesb.append("Missed cleavages").append("\t");
			titlesb.append("Mass").append("\t");
			titlesb.append("Proteins").append("\t");
			titlesb.append("Charges").append("\t");
			titlesb.append("Score").append("\t");
			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < expNames.length; i++) {
				String name = expNames[i].substring(expNames[i].indexOf("_") + 1);
				if (this.expNameMap.containsKey(name)) {
					titlesb.append("Intensity " + expNameMap.get(name)).append("\t");
				} else {
					titlesb.append("Intensity " + name).append("\t");
				}
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("MS/MS Count").append("\t");
			titlesb.append("id");

			writer.println(titlesb);

			String[] seqs = intensityMap.keySet().toArray(new String[intensityMap.size()]);
			Arrays.sort(seqs);

			int id = 0;
			for (int i = 0; i < seqs.length; i++) {

				if (!psmmap.containsKey(seqs[i])) {
					continue;
				}

				OpenPSM psm = psmmap.get(seqs[i]);

				StringBuilder sb = new StringBuilder();
				sb.append(seqs[i]).append("\t");
				sb.append(seqs[i].length()).append("\t");

				sb.append(psm.getMiss()).append("\t");
				sb.append(psm.getPepMass()).append("\t");
				sb.append(psm.getProtein()).append("\t");

				int[] charge = chargemap.get(seqs[i]);
				StringBuilder chargesb = new StringBuilder();
				for (int j = 0; j < charge.length; j++) {
					if (charge[j] == 1) {
						chargesb.append(j + 1).append(";");
					}
				}
				if (chargesb.length() > 0) {
					sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
				} else {
					sb.append("2").append("\t");
				}

				sb.append(psm.getHyperscore()).append("\t");

				double totalIntensity = 0;
				double[] intensity = intensityMap.get(seqs[i]);
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append((int) totalIntensity).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append((int) intensity[j]).append("\t");
				}

				sb.append("").append("\t");
				sb.append("").append("\t");

				sb.append(countmap.get(seqs[i])).append("\t");
				sb.append(id++);

				writer.println(sb);
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing peptides from " + this.final_pep_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing peptides to "
					+ this.final_pep_txt.getName());
		}

		LOGGER.info(taskName + ": peptide report has been exported to " + final_pep_txt);
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": peptide report has been exported to " + final_pep_txt);
	}

	/**
	 * not used now
	 */
	@SuppressWarnings("unused")
	private void exportPeptideXlsx() {

		LOGGER.info(taskName + ": exporting peptides...");

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file, e);
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file, e);
		}

		String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
		Arrays.sort(sequences);

		this.final_pep_file = new File(this.result, "final_peptides.csv");
		this.proPepCountMap = new HashMap<String, Integer>();

		String[] title = new String[expNames.length + 9];
		title[0] = "Id";
		title[1] = "Sequence";
		title[2] = "Variable mods";
		title[3] = "Open mod mass";
		title[4] = "Possible open mod name";
		title[5] = "Possible open mod site";
		title[6] = "PSM count";
		title[7] = "P value";
		title[8] = "Protein";

		System.arraycopy(expNames, 0, title, 9, expNames.length);

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont titleFont = workbook.createFont();
		titleFont.setBold(true);

		XSSFFont contentFont = workbook.createFont();
		contentFont.setBold(false);

		XSSFSheet sheet = workbook.createSheet("Peptide list");
		style.setFont(titleFont);

		int rowCount = 0;

		XSSFRow row0 = sheet.createRow(rowCount++);
		for (int i = 0; i < title.length; i++) {
			XSSFCell cell = row0.createCell(i);
			cell.setCellValue(title[i]);
			cell.setCellStyle(style);
		}
		style.setFont(contentFont);

		for (int i = 0; i < sequences.length; i++) {

			HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

			String[] modseqs = map.keySet().toArray(new String[map.size()]);
			Arrays.sort(modseqs);

			double[] scores = new double[modseqs.length];
			int[] psmCounts = new int[modseqs.length];
			double[][] intensities = new double[modseqs.length][];

			String[] variMods = new String[modseqs.length];
			double[] opModMass = new double[modseqs.length];
			String[] opModName = new String[modseqs.length];
			String[] opModSite = new String[modseqs.length];

			for (int j = 0; j < modseqs.length; j++) {

				String proAcc = this.pepProMap.get(modseqs[j]);
				if (proAcc == null) {
					LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
							+ this.filtered_3_psms_file);
					continue;
				}

				String pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

				scores[j] = pepBestScoreMap.get(modseqs[j]);
				psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
				intensities[j] = valueMap.get(modseqs[j]);

				Object[] objs = map.get(modseqs[j]);
				variMods[j] = (String) objs[0];
				opModMass[j] = 0;
				opModName[j] = (String) objs[2];
				opModSite[j] = (String) objs[3];

				if (opModName[j].length() > 0) {
					opModMass[j] = (double) objs[1];
				}

				if (proPepCountMap.containsKey(proAcc)) {
					proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
				} else {
					proPepCountMap.put(proAcc, 1);
				}

				XSSFRow row = sheet.createRow(rowCount++);
				XSSFCell[] cells = new XSSFCell[title.length];
				int cellId = 0;
				if (j == 0) {
					cells[cellId] = row.createCell(cellId);
					cells[cellId].setCellValue((i + 1));
					cellId++;
				} else {
					cells[cellId] = row.createCell(cellId);
					cells[cellId].setCellValue("");
					cellId++;
				}

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(sequences[i]);
				cellId++;

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(variMods[j]);
				cellId++;

				if (opModName[j].length() > 0) {
					cells[cellId] = row.createCell(cellId);
					cells[cellId].setCellValue(opModMass[j]);
					cellId++;
				} else {
					cells[cellId] = row.createCell(cellId);
					cells[cellId].setCellValue("");
					cellId++;
				}

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(opModName[j]);
				cellId++;

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(opModSite[j]);
				cellId++;

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(psmCounts[j]);
				cellId++;

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(scores[j]);
				cellId++;

				cells[cellId] = row.createCell(cellId);
				cells[cellId].setCellValue(pro);
				cellId++;

				for (int k = 0; k < intensities[j].length; k++) {
					cells[cellId] = row.createCell(cellId);
					cells[cellId].setCellValue(intensities[j][k]);
					cellId++;
				}
			}
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(final_pep_file);
			workbook.write(out);
			workbook.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing peptide result file to " + final_pep_file, e);
		}

		LOGGER.info(taskName + ": peptides has been exported to " + final_pep_file);
	}

	/**
	 * @deprecated not used now
	 */
	@SuppressWarnings("unused")
	private void exportPeptideTaxa2() {

		LOGGER.info(taskName + ": exporting peptides...");

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file, e);
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file, e);
		}

		TaxonomyDatabase td = new TaxonomyDatabase();
		HashSet<String> sequenceSet = new HashSet<String>();
		sequenceSet.addAll(pepModInfoMap.keySet());

		if (mptp.isBuildIn()) {

			String pep2tax = ((MetaSourcesV2) msv).getPep2tax();

			if ((new File(pep2tax)).exists()) {

				Pep2TaxaDatabase pep2taxDb = new Pep2TaxaDatabase(pep2tax, td);

				HashSet<String> exclude = new HashSet<String>();
				exclude.add("environmental samples");

				HashSet<RootType> rootSet = new HashSet<RootType>();
				rootSet.add(RootType.Bacteria);
				rootSet.add(RootType.Archaea);
				rootSet.add(RootType.cellular_organisms);
				rootSet.add(RootType.Eukaryota);
				rootSet.add(RootType.Viroids);
				rootSet.add(RootType.Viruses);

				HashMap<String, ArrayList<Taxon>> taxaMap = pep2taxDb.pept2TaxaList(sequenceSet);
				HashMap<String, Integer> lcaIdMap = pep2taxDb.taxa2Lca(taxaMap, TaxonomyRanks.Family, rootSet, exclude);

				String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
				Arrays.sort(sequences);

				this.final_pep_file = new File(this.result, "final_peptides.xlsx");
				this.proPepCountMap = new HashMap<String, Integer>();

				String[] title = new String[expNames.length + 9];
				title[0] = "Id";
				title[1] = "Sequence";
				title[2] = "Variable mods";
				title[3] = "Open mod mass";
				title[4] = "Possible open mod name";
				title[5] = "Possible open mod site";
				title[6] = "PSM count";
				title[7] = "P value";
				title[8] = "Protein";

				System.arraycopy(expNames, 0, title, 9, expNames.length);

				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFCellStyle style = workbook.createCellStyle();
				XSSFFont titleFont = workbook.createFont();
				titleFont.setBold(true);

				XSSFFont contentFont = workbook.createFont();
				contentFont.setBold(false);

				XSSFSheet sheet = workbook.createSheet("Peptide list");
				style.setFont(titleFont);

				int rowCount = 0;

				XSSFRow row0 = sheet.createRow(rowCount++);
				for (int i = 0; i < title.length; i++) {
					XSSFCell cell = row0.createCell(i);
					cell.setCellValue(title[i]);
					cell.setCellStyle(style);
				}
				style.setFont(contentFont);

				File oepnSearch = new File(metaPar.getResult(), "open_search");
				File taxFile = new File(oepnSearch, "taxonomy_analysis");
				if (!taxFile.exists()) {
					taxFile.mkdir();
				}
				File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");

				MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, MetaConstants.openSearch,
						MetaConstants.labelFree, expNames);

				for (int i = 0; i < sequences.length; i++) {
					// String proAcc = pepProMap.get(sequences[i]);
					// String pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) :
					// proAcc;

					HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

					String[] modseqs = map.keySet().toArray(new String[map.size()]);
					Arrays.sort(modseqs);

					double[] scores = new double[modseqs.length];
					int[] psmCounts = new int[modseqs.length];
					double[][] intensities = new double[modseqs.length][];

					String[] variMods = new String[modseqs.length];
					double[] opModMass = new double[modseqs.length];
					String[] opModName = new String[modseqs.length];
					String[] opModSite = new String[modseqs.length];
					String pro = null;

					for (int j = 0; j < modseqs.length; j++) {

						String proAcc = this.pepProMap.get(modseqs[j]);
						if (proAcc == null) {
							LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
									+ this.filtered_3_psms_file);
							continue;
						}

						pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

						scores[j] = pepBestScoreMap.get(modseqs[j]);
						psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
						intensities[j] = valueMap.get(modseqs[j]);

						Object[] objs = map.get(modseqs[j]);
						variMods[j] = (String) objs[0];
						opModMass[j] = 0;
						opModName[j] = (String) objs[2];
						opModSite[j] = (String) objs[3];

						if (opModName[j].length() > 0) {
							opModMass[j] = (double) objs[1];
						}

						if (proPepCountMap.containsKey(proAcc)) {
							proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
						} else {
							proPepCountMap.put(proAcc, 1);
						}

						XSSFRow row = sheet.createRow(rowCount++);
						XSSFCell[] cells = new XSSFCell[title.length];
						int cellId = 0;
						if (j == 0) {
							cells[cellId] = row.createCell(cellId);
							cells[cellId].setCellValue((i + 1));
							cellId++;
						} else {
							cells[cellId] = row.createCell(cellId);
							cells[cellId].setCellValue("");
							cellId++;
						}

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(sequences[i]);
						cellId++;

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(variMods[j]);
						cellId++;

						if (opModName[j].length() > 0) {
							cells[cellId] = row.createCell(cellId);
							cells[cellId].setCellValue(opModMass[j]);
							cellId++;
						} else {
							cells[cellId] = row.createCell(cellId);
							cells[cellId].setCellValue("");
							cellId++;
						}

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(opModName[j]);
						cellId++;

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(opModSite[j]);
						cellId++;

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(psmCounts[j]);
						cellId++;

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(scores[j]);
						cellId++;

						cells[cellId] = row.createCell(cellId);
						cells[cellId].setCellValue(pro);
						cellId++;

						for (int k = 0; k < intensities[j].length; k++) {
							cells[cellId] = row.createCell(cellId);
							cells[cellId].setCellValue(intensities[j][k]);
							cellId++;
						}
					}

					if (lcaIdMap.containsKey(sequences[i])) {
						int taxId = lcaIdMap.get(sequences[i]);
						Taxon lca = td.getTaxonFromId(taxId);

						if (lca != null) {

							OpenPeptide op = new OpenPeptide(sequences[i], pro, modseqs, scores, psmCounts, intensities,
									variMods, opModMass, opModName, opModSite);

							op.setLca(lca);

							ArrayList<Taxon> taxonList = taxaMap.get(sequences[i]);
							int[] taxonIds = new int[taxonList.size()];
							for (int j = 0; j < taxonList.size(); j++) {
								Taxon taxonj = taxonList.get(j);
								taxonIds[j] = taxonj.getId();
								if (taxonj.getMainParentIds() == null) {
									taxonj.setMainParentIds(td.getMainParentTaxonIds(taxonj));
								}

								int[] mainTaxonIds = taxonj.getMainParentIds();

								for (int parentId : mainTaxonIds) {
									Taxon parentTaxon = td.getTaxonFromId(parentId);
									if (parentTaxon != null) {
										if (parentTaxon.getMainParentIds() == null) {
											parentTaxon.setMainParentIds(td.getMainParentTaxonIds(parentId));
										}
										writer.addTaxon(parentTaxon);
									}
								}
							}
							op.setTaxonIds(taxonIds);
							writer.addPeptide(op);
						}
					}
				}
				writer.close();

				FileOutputStream out;
				try {
					out = new FileOutputStream(final_pep_file);
					workbook.write(out);
					workbook.close();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + " error in writing peptide result file to " + final_pep_file, e);
				}

				MetaPeptideXMLReader xmlReader = new MetaPeptideXMLReader(this.final_pep_taxa_xml_file);
				xmlReader.export(this.result, "final_peptide_taxa", "taxon_file", pepCountThres);

				LOGGER.info(taskName + ": peptides has been exported to " + final_pep_file);
			}
		}

		if (mptp.isUnipept()) {

		}
	}

	private boolean exportOpenPeptideTaxa() {

		LOGGER.info(taskName + ": exporting peptides and taxonomy started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": export peptides and taxonomy started");

		boolean finish = false;

		File taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		boolean executeBuiltin = false;
		if (mptp.isBuildIn()) {
			File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");
			if (!taxResultFile.exists() || taxResultFile.length() == 0) {
				executeBuiltin = true;
			} else {
				finish = true;
			}
		}

		boolean executeUnipept = false;
		if (mptp.isUnipept()) {
			File taxResultFile = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxonomy.xml");
			if (!taxResultFile.exists() || taxResultFile.length() == 0) {
				executeUnipept = true;
			} else {
				finish = true;
			}
		}

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		TaxonomyDatabase td = new TaxonomyDatabase(((MetaSourcesV2) msv).getTaxonAll());
		HashSet<String> sequenceSet = new HashSet<String>();
		sequenceSet.addAll(pepModInfoMap.keySet());

		if (mptp.isBuildIn() && executeBuiltin) {

			String pep2tax = ((MetaSourcesV2) msv).getPep2tax();

			if ((new File(pep2tax)).exists()) {

				Pep2TaxaDatabase pep2taxDb = new Pep2TaxaDatabase(pep2tax, td);

				TaxonomyRanks ignoreRank = mptp.getIgnoreBlankRank();
				HashSet<String> exclude = mptp.getExcludeTaxa();
				HashSet<RootType> rootSet = mptp.getUsedRootTypes();

				HashMap<String, ArrayList<Taxon>> taxaMap = pep2taxDb.pept2TaxaList(sequenceSet);
				HashMap<String, Integer> lcaIdMap = pep2taxDb.taxa2Lca(taxaMap, ignoreRank, rootSet, exclude);

				String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
				Arrays.sort(sequences);

				this.proPepCountMap = new HashMap<String, Integer>();

				File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");

				MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, MetaConstants.openSearch,
						MetaConstants.labelFree, expNames);

				for (int i = 0; i < sequences.length; i++) {

					HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

					String[] modseqs = map.keySet().toArray(new String[map.size()]);
					Arrays.sort(modseqs);

					double[] scores = new double[modseqs.length];
					int[] psmCounts = new int[modseqs.length];
					double[][] intensities = new double[modseqs.length][];

					String[] variMods = new String[modseqs.length];
					double[] opModMass = new double[modseqs.length];
					String[] opModName = new String[modseqs.length];
					String[] opModSite = new String[modseqs.length];
					String pro = null;

					for (int j = 0; j < modseqs.length; j++) {

						String proAcc = this.pepProMap.get(modseqs[j]);
						if (proAcc == null) {
							LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
									+ this.filtered_3_psms_file.getName());
							continue;
						}

						pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

						scores[j] = pepBestScoreMap.get(modseqs[j]);
						psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
						intensities[j] = valueMap.get(modseqs[j]);

						Object[] objs = map.get(modseqs[j]);
						variMods[j] = (String) objs[0];
						opModMass[j] = 0;
						opModName[j] = (String) objs[2];
						opModSite[j] = (String) objs[3];

						if (opModName[j].length() > 0) {
							opModMass[j] = (double) objs[1];
						}

						if (proPepCountMap.containsKey(proAcc)) {
							proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
						} else {
							proPepCountMap.put(proAcc, 1);
						}
					}

					if (lcaIdMap.containsKey(sequences[i])) {
						int taxId = lcaIdMap.get(sequences[i]);
						Taxon lca = td.getTaxonFromId(taxId);

						if (lca != null) {

							OpenPeptide op = new OpenPeptide(sequences[i], pro, modseqs, scores, psmCounts, intensities,
									variMods, opModMass, opModName, opModSite);

							op.setLca(lca);

							ArrayList<Taxon> taxonList = taxaMap.get(sequences[i]);
							int[] taxonIds = new int[taxonList.size()];
							for (int j = 0; j < taxonList.size(); j++) {
								Taxon taxonj = taxonList.get(j);
								taxonIds[j] = taxonj.getId();
								if (taxonj.getMainParentIds() == null) {
									taxonj.setMainParentIds(td.getMainParentTaxonIds(taxonj));
								}

								int[] mainTaxonIds = taxonj.getMainParentIds();

								for (int parentId : mainTaxonIds) {
									Taxon parentTaxon = td.getTaxonFromId(parentId);
									if (parentTaxon != null) {
										if (parentTaxon.getMainParentIds() == null) {
											parentTaxon.setMainParentIds(td.getMainParentTaxonIds(parentId));
										}
										writer.addTaxon(parentTaxon);
									}
								}
							}
							op.setTaxonIds(taxonIds);
							writer.addPeptide(op);
						}
					}
				}
				writer.close();

				if (taxResultFile.exists() && taxResultFile.length() > 0) {

					MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

					File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
					reader.exportCsv(taxFile, MetaAlgorithm.Builtin, mptp.getLeastPepCount());

					MetaPeptide[] peps = reader.getPeptides();
					Taxon[] taxons = reader.getTaxons();

					File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
					if (!megan.exists() || megan.length() == 0) {
						MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					}

					File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
					if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
						reader.exportPeptideTaxaAll(allPepTaxa);
					}

					File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
					if (!iMetaLab.exists() || iMetaLab.length() == 0) {
						MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					}

					File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
					if (!jsOutput.exists() || jsOutput.length() == 0) {
						MetaTreeHandler.exportJS(peps, taxons, expNames, iMetaLab);
					} else {
						jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree2.js");
						MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
					}

					if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
						finish = true;
					}

					File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
					if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
						if (summaryMetaFile.exists()) {

							MetaReportTask task = new MetaReportTask(1);
							task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, summaryMetaFile);
							task.run();

						} else {

							MetaReportTask task = new MetaReportTask(1);
							task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);

							task.run();
						}
					}
					finish = true;
				}
			}
		}

		if (mptp.isUnipept() && executeUnipept) {

			String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
			Arrays.sort(sequences);

			HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);
			this.proPepCountMap = new HashMap<String, Integer>();

			File taxResultFile = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxonomy.xml");

			MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, MetaConstants.openSearch,
					MetaConstants.labelFree, expNames);

			for (int i = 0; i < sequences.length; i++) {

				HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

				String[] modseqs = map.keySet().toArray(new String[map.size()]);
				Arrays.sort(modseqs);

				double[] scores = new double[modseqs.length];
				int[] psmCounts = new int[modseqs.length];
				double[][] intensities = new double[modseqs.length][];

				String[] variMods = new String[modseqs.length];
				double[] opModMass = new double[modseqs.length];
				String[] opModName = new String[modseqs.length];
				String[] opModSite = new String[modseqs.length];
				String pro = null;

				for (int j = 0; j < modseqs.length; j++) {

					String proAcc = this.pepProMap.get(modseqs[j]);
					if (proAcc == null) {
						LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
								+ this.filtered_3_psms_file.getName());
						continue;
					}

					pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

					scores[j] = pepBestScoreMap.get(modseqs[j]);
					psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
					intensities[j] = valueMap.get(modseqs[j]);

					Object[] objs = map.get(modseqs[j]);
					variMods[j] = (String) objs[0];
					opModMass[j] = 0;
					opModName[j] = (String) objs[2];
					opModSite[j] = (String) objs[3];

					if (opModName[j].length() > 0) {
						opModMass[j] = (double) objs[1];
					}

					if (proPepCountMap.containsKey(proAcc)) {
						proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
					} else {
						proPepCountMap.put(proAcc, 1);
					}
				}

				if (lcaResultMap.containsKey(sequences[i])) {
					UnipLCAResult lcaResult = lcaResultMap.get(sequences[i]);
					int taxId = lcaResult.getTaxon().getId();
					Taxon lca = td.getTaxonFromId(taxId);

					if (lca != null) {

						OpenPeptide op = new OpenPeptide(sequences[i], pro, modseqs, scores, psmCounts, intensities,
								variMods, opModMass, opModName, opModSite);

						op.setLca(lca);

						if (lca.getMainParentIds() == null) {
							lca.setMainParentIds(td.getMainParentTaxonIds(taxId));
						}

						int[] mainTaxonIds = lca.getMainParentIds();

						for (int parentId : mainTaxonIds) {
							Taxon parentTaxon = td.getTaxonFromId(parentId);
							if (parentTaxon != null) {
								if (parentTaxon.getMainParentIds() == null) {
									parentTaxon.setMainParentIds(td.getMainParentTaxonIds(parentId));
								}
								writer.addTaxon(parentTaxon);
							}
						}

						op.setLca(lca);
						writer.addPeptide(op);
					}
				}
			}
			writer.close();

			if (taxResultFile.exists() && taxResultFile.length() > 0) {

				MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

				File refinedTaxon = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxa.refine.csv");
				reader.exportCsv(taxFile, MetaAlgorithm.Unipept, mptp.getLeastPepCount());

				MetaPeptide[] peps = reader.getPeptides();
				Taxon[] taxons = reader.getTaxons();

				File megan = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".biom");
				if (!megan.exists() || megan.length() == 0) {
					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
				}

				File allPepTaxa = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".allPepTaxa.csv");
				if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
					reader.exportPeptideTaxaAll(allPepTaxa);
				}

				File iMetaLab = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
				if (!iMetaLab.exists() || iMetaLab.length() == 0) {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
				}

				File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
				if (!jsOutput.exists() || jsOutput.length() == 0) {
					MetaTreeHandler.exportJS(peps, taxons, expNames, iMetaLab);
				} else {
					jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree2.js");
					MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
				}

				if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
					finish = true;
				}

				File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
				if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
					if (summaryMetaFile.exists()) {

						MetaReportTask task = new MetaReportTask(1);
						task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, summaryMetaFile);

						task.run();

					} else {
						MetaReportTask task = new MetaReportTask(1);
						task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);

						task.run();
					}
				}
				finish = true;
			}
		}

		if (finish) {
			LOGGER.info(taskName + ": export peptides and taxonomy finished");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": exporting peptides and taxonomy finished");
			return true;
		} else {
			LOGGER.error(taskName + ": export peptides and taxonomy failed");
			System.err
					.println(format.format(new Date()) + "\t" + taskName + ": exporting peptides and taxonomy failed");
			return false;
		}
	}

	private boolean exportClosedPeptideTaxa() {

		LOGGER.info(taskName + ": exporting peptides with taxonomy started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting peptides with taxonomy started");

		boolean finish = false;

		File taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		boolean executeBuiltin = false;
		if (mptp.isBuildIn()) {
			File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");
			if (!taxResultFile.exists() || taxResultFile.length() == 0) {
				executeBuiltin = true;
			} else {
				finish = true;
			}
		}

		boolean executeUnipept = false;
		if (mptp.isBuildIn()) {
			File taxResultFile = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxonomy.xml");
			if (!taxResultFile.exists() || taxResultFile.length() == 0) {
				executeUnipept = true;
			} else {
				finish = true;
			}
		}

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		HashMap<String, double[]> valueMap = new HashMap<String, double[]>();
		String[] expNames = null;
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int proId = -1;
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Protein Groups")) {
					proId = i;
				} else if (title[i].startsWith("Intensity_")) {
					list.add(title[i]);
				}
			}

			expNames = list.toArray(new String[list.size()]);
			Arrays.sort(expNames);

			int[] intenId = new int[list.size()];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						intenId[i] = j;
					}
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double[] intensity = new double[expNames.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
				valueMap.put(cs[seqId], intensity);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading peptides from "
					+ this.quan_pep_file.getName());
		}

		TaxonomyDatabase td = new TaxonomyDatabase(msv.getTaxonAll());
		HashSet<String> sequenceSet = new HashSet<String>();
		sequenceSet.addAll(pepModInfoMap.keySet());

		if (mptp.isBuildIn() && executeBuiltin) {

			String pep2tax = msv.getPep2tax();

			if ((new File(pep2tax)).exists()) {

				Pep2TaxaDatabase pep2taxDb = new Pep2TaxaDatabase(pep2tax, td);

				TaxonomyRanks ignoreRank = mptp.getIgnoreBlankRank();
				HashSet<String> exclude = mptp.getExcludeTaxa();
				HashSet<RootType> rootSet = mptp.getUsedRootTypes();

				HashMap<String, ArrayList<Taxon>> taxaMap = pep2taxDb.pept2TaxaList(sequenceSet);
				HashMap<String, Integer> lcaIdMap = pep2taxDb.taxa2Lca(taxaMap, ignoreRank, rootSet, exclude);

				String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
				Arrays.sort(sequences);

				this.proPepCountMap = new HashMap<String, Integer>();

				File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");

				MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, MetaConstants.openSearch,
						MetaConstants.labelFree, expNames);

				for (int i = 0; i < sequences.length; i++) {

					HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

					String[] modseqs = map.keySet().toArray(new String[map.size()]);
					Arrays.sort(modseqs);

					double[] scores = new double[modseqs.length];
					int[] psmCounts = new int[modseqs.length];
					double[][] intensities = new double[modseqs.length][];

					String[] variMods = new String[modseqs.length];
					double[] opModMass = new double[modseqs.length];
					Arrays.fill(opModMass, 0.0);
					String[] opModName = new String[modseqs.length];
					Arrays.fill(opModName, "");
					String[] opModSite = new String[modseqs.length];
					Arrays.fill(opModSite, "");

					String pro = null;

					for (int j = 0; j < modseqs.length; j++) {

						String proAcc = this.pepProMap.get(modseqs[j]);
						if (proAcc == null) {
							LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
									+ this.filtered_3_psms_file.getName());
							continue;
						}

						pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

						scores[j] = pepBestScoreMap.get(modseqs[j]);
						psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
						intensities[j] = valueMap.get(modseqs[j]);

						Object[] objs = map.get(modseqs[j]);
						variMods[j] = (String) objs[0];

						if (proPepCountMap.containsKey(proAcc)) {
							proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
						} else {
							proPepCountMap.put(proAcc, 1);
						}
					}

					if (lcaIdMap.containsKey(sequences[i])) {
						int taxId = lcaIdMap.get(sequences[i]);
						Taxon lca = td.getTaxonFromId(taxId);

						if (lca != null) {

							OpenPeptide op = new OpenPeptide(sequences[i], pro, modseqs, scores, psmCounts, intensities,
									variMods, opModMass, opModName, opModSite);

							op.setLca(lca);

							ArrayList<Taxon> taxonList = taxaMap.get(sequences[i]);
							int[] taxonIds = new int[taxonList.size()];
							for (int j = 0; j < taxonList.size(); j++) {
								Taxon taxonj = taxonList.get(j);
								taxonIds[j] = taxonj.getId();
								if (taxonj.getMainParentIds() == null) {
									taxonj.setMainParentIds(td.getMainParentTaxonIds(taxonj));
								}

								int[] mainTaxonIds = taxonj.getMainParentIds();

								for (int parentId : mainTaxonIds) {
									Taxon parentTaxon = td.getTaxonFromId(parentId);
									if (parentTaxon != null) {
										if (parentTaxon.getMainParentIds() == null) {
											parentTaxon.setMainParentIds(td.getMainParentTaxonIds(parentId));
										}
										writer.addTaxon(parentTaxon);
									}
								}
							}
							op.setTaxonIds(taxonIds);
							writer.addPeptide(op);
						}
					}
				}
				writer.close();

				if (taxResultFile.exists() && taxResultFile.length() > 0) {				

					MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

					File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
					reader.exportCsv(taxFile, MetaAlgorithm.Builtin, mptp.getLeastPepCount());

					MetaPeptide[] peps = reader.getPeptides();
					Taxon[] taxons = reader.getTaxons();

					File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
					if (!megan.exists() || megan.length() == 0) {
						MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					}

					File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
					if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
						reader.exportPeptideTaxaAll(allPepTaxa);
					}

					File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
					if (!iMetaLab.exists() || iMetaLab.length() == 0) {
						MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					}

					File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
					if (!jsOutput.getParentFile().exists()) {
						jsOutput.getParentFile().mkdirs();
					}

					if (!jsOutput.exists() || jsOutput.length() == 0) {
						MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
					}

					if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
						finish = true;
					}

					File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
					if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

						if (summaryMetaFile.exists()) {
							MetaReportTask task = new MetaReportTask(1);
							task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, summaryMetaFile);

							task.run();

						} else {
							MetaReportTask task = new MetaReportTask(1);
							task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);

							task.run();
						}
					}

					finish = true;
				}
			}
		}

		if (mptp.isUnipept() && executeUnipept) {

			String[] sequences = this.pepModInfoMap.keySet().toArray(new String[this.pepModInfoMap.size()]);
			Arrays.sort(sequences);

			HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);
			this.proPepCountMap = new HashMap<String, Integer>();

			File taxResultFile = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxonomy.xml");

			MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, MetaConstants.openSearch,
					MetaConstants.labelFree, expNames);

			for (int i = 0; i < sequences.length; i++) {

				HashMap<String, Object[]> map = this.pepModInfoMap.get(sequences[i]);

				String[] modseqs = map.keySet().toArray(new String[map.size()]);
				Arrays.sort(modseqs);

				double[] scores = new double[modseqs.length];
				int[] psmCounts = new int[modseqs.length];
				double[][] intensities = new double[modseqs.length][];

				String[] variMods = new String[modseqs.length];
				double[] opModMass = new double[modseqs.length];
				Arrays.fill(opModMass, 0.0);
				String[] opModName = new String[modseqs.length];
				Arrays.fill(opModName, "");
				String[] opModSite = new String[modseqs.length];
				Arrays.fill(opModSite, "");

				String pro = null;

				for (int j = 0; j < modseqs.length; j++) {

					String proAcc = this.pepProMap.get(modseqs[j]);
					if (proAcc == null) {
						LOGGER.error("Protein information for peptide " + modseqs[j] + " was not found in "
								+ this.filtered_3_psms_file.getName());
						continue;
					}

					pro = proAccNameMap.containsKey(proAcc) ? proAccNameMap.get(proAcc) : proAcc;

					scores[j] = pepBestScoreMap.get(modseqs[j]);
					psmCounts[j] = pepPsmCountMap.get(modseqs[j]);
					intensities[j] = valueMap.get(modseqs[j]);

					Object[] objs = map.get(modseqs[j]);
					variMods[j] = (String) objs[0];

					if (proPepCountMap.containsKey(proAcc)) {
						proPepCountMap.put(proAcc, proPepCountMap.get(proAcc) + 1);
					} else {
						proPepCountMap.put(proAcc, 1);
					}
				}

				if (lcaResultMap.containsKey(sequences[i])) {
					UnipLCAResult lcaResult = lcaResultMap.get(sequences[i]);
					int taxId = lcaResult.getTaxon().getId();
					Taxon lca = td.getTaxonFromId(taxId);

					if (lca != null) {

						OpenPeptide op = new OpenPeptide(sequences[i], pro, modseqs, scores, psmCounts, intensities,
								variMods, opModMass, opModName, opModSite);

						op.setLca(lca);

						if (lca.getMainParentIds() == null) {
							lca.setMainParentIds(td.getMainParentTaxonIds(taxId));
						}

						int[] mainTaxonIds = lca.getMainParentIds();

						for (int parentId : mainTaxonIds) {
							Taxon parentTaxon = td.getTaxonFromId(parentId);
							if (parentTaxon != null) {
								if (parentTaxon.getMainParentIds() == null) {
									parentTaxon.setMainParentIds(td.getMainParentTaxonIds(parentId));
								}
								writer.addTaxon(parentTaxon);
							}
						}

						op.setLca(lca);
						writer.addPeptide(op);
					}
				}
			}
			writer.close();

			if (taxResultFile.exists() && taxResultFile.length() > 0) {

				MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

				File refinedTaxon = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxa.refine.csv");
				reader.exportCsv(taxFile, MetaAlgorithm.Unipept, mptp.getLeastPepCount());

				MetaPeptide[] peps = reader.getPeptides();
				Taxon[] taxons = reader.getTaxons();

				File megan = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".biom");
				if (!megan.exists() || megan.length() == 0) {
					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
				}

				File allPepTaxa = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".allPepTaxa.csv");
				if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
					reader.exportPeptideTaxaAll(allPepTaxa);
				}

				File iMetaLab = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
				if (!iMetaLab.exists() || iMetaLab.length() == 0) {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
				}

				File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
				if (!jsOutput.getParentFile().exists()) {
					jsOutput.getParentFile().mkdirs();
				}

				if (!jsOutput.exists() || jsOutput.length() == 0) {
					MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
				}

				if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
					finish = true;
				}

				File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
				if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

					if (summaryMetaFile.exists()) {
						MetaReportTask task = new MetaReportTask(1);
						task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, summaryMetaFile);

						task.run();

					} else {
						MetaReportTask task = new MetaReportTask(1);
						task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);

						task.run();
					}
				}

				finish = true;
			}
		}

		if (finish) {
			LOGGER.info(taskName + ": export peptides and taxonomy finished");
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": exporting peptides and taxonomy finished");
			return true;
		} else {
			LOGGER.error(taskName + ": export peptides and taxonomy failed");
			System.err
					.println(format.format(new Date()) + "\t" + taskName + ": exporting peptides and taxonomy failed");
			return false;
		}
	}

	private void exportProteinsTxt() {

		LOGGER.info(taskName + ": exporting protein report started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting protein report started");

		Reader filter3Reader = null;
		Iterable<CSVRecord> records = null;

		try {
			filter3Reader = new FileReader(this.filtered_3_pros_file);
			records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(filter3Reader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading proteins from " + this.filtered_3_pros_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading proteins from "
					+ this.filtered_3_pros_file.getName());
		}

		HashMap<String, Double> scoreMap = new HashMap<String, Double>();
		HashMap<String, Integer> pepCountMap = new HashMap<String, Integer>();

		for (CSVRecord record : records) {
			String pro = record.get(0);
			pro = pro.split("[\\s+]")[0];
			double score = Double.parseDouble(record.get(1));
			scoreMap.put(pro, score);
			int pepCount = Integer.parseInt(record.get(2));
			pepCountMap.put(pro, pepCount);
		}

		BufferedReader quanProReader = null;
		try {
			quanProReader = new BufferedReader(new FileReader(this.quan_pro_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(this.final_pro_txt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		try {

			String line = quanProReader.readLine();
			String[] title = line.split("\t");
			ArrayList<String> tlist = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity")) {
					tlist.add(title[i]);
				}
			}

			String[] expNames = tlist.toArray(new String[tlist.size()]);
			Arrays.sort(expNames);

			int[] fileIds = new int[expNames.length];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (expNames[i].equals(title[j])) {
						fileIds[i] = j;
					}
				}
			}

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Protein IDs").append("\t");
			titlesb.append("Peptides").append("\t");
			titlesb.append("Unique peptides").append("\t");
			titlesb.append("Score").append("\t");
			titlesb.append("Intensity").append("\t");

			for (int i = 0; i < expNames.length; i++) {
				String name = expNames[i].substring(expNames[i].indexOf("_") + 1);
				if (this.expNameMap.containsKey(name)) {
					titlesb.append("Intensity " + expNameMap.get(name)).append("\t");
				} else {
					titlesb.append("Intensity " + name).append("\t");
				}
			}

			titlesb.append("Only identified by site").append("\t");
			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id");

			writer.println(titlesb);

			int id = 0;

			while ((line = quanProReader.readLine()) != null) {

				String[] cs = line.split("\t");
				String pro = cs[0].split("[\\s+]")[0];

				if (pepCountMap.containsKey(pro)) {
					StringBuilder sb = new StringBuilder();
					sb.append(cs[0]).append("\t");
					sb.append(pepCountMap.get(pro)).append("\t");
					sb.append(pepCountMap.get(pro)).append("\t");
					sb.append(df4.format(scoreMap.get(pro))).append("\t");

					double totalIntensity = 0;
					double[] intensity = new double[expNames.length];
					for (int i = 0; i < fileIds.length; i++) {
						intensity[i] = Double.parseDouble(cs[fileIds[i]]);
						totalIntensity += intensity[i];
					}

					sb.append((int) totalIntensity).append("\t");
					for (int i = 0; i < intensity.length; i++) {
						sb.append((int) intensity[i]).append("\t");
					}

					sb.append("").append("\t");
					sb.append("").append("\t");
					sb.append("").append("\t");
					sb.append(id++);

					writer.println(sb);
				}
			}

			quanProReader.close();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		LOGGER.info(taskName + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + taskName + ": protein report has been exported to "
				+ final_pro_txt.getName());
	}

	private void exportProteins() {

		LOGGER.info(taskName + ": exporting proteins started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting proteins started");

		Reader filter3Reader = null;
		Iterable<CSVRecord> records = null;

		try {
			filter3Reader = new FileReader(this.filtered_3_pros_file);
			records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(filter3Reader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading proteins from " + this.filtered_3_pros_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading proteins from "
					+ this.filtered_3_pros_file.getName());
		}

		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		for (CSVRecord record : records) {
			String pro = record.get(0);
			pro = pro.split("[\\s+]")[0];
			double score = Double.parseDouble(record.get(1));
			scoreMap.put(pro, score);
		}

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter proCsvPrinter = null;

		BufferedReader quanProReader = null;
		try {
			quanProReader = new BufferedReader(new FileReader(this.quan_pro_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		try {

			String line = quanProReader.readLine();
			String[] title = line.split("\t");
			ArrayList<String> tlist = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity")) {
					tlist.add(title[i]);
				}
			}

			String[] fileNames = tlist.toArray(new String[tlist.size()]);
			Arrays.sort(fileNames);

			Object[] csvTitle = new Object[fileNames.length + 4];
			try {
				proCsvPrinter = new CSVPrinter(new FileWriter(this.final_pro_file), csvFileFormat);
				csvTitle[0] = "Protein";
				csvTitle[1] = "Score";
				csvTitle[2] = "Number of peptides";
				csvTitle[3] = "Number of PSMs";

				for (int i = 0; i < fileNames.length; i++) {
					csvTitle[i + 4] = fileNames[i];
				}

				proCsvPrinter.printRecord(csvTitle);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + " error in writing proteins to " + this.final_pro_file.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing proteins to "
						+ this.final_pro_file.getName());
			}

			int[] fileIds = new int[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (fileNames[i].equals(title[j])) {
						fileIds[i] = j;
					}
				}
			}

			while ((line = quanProReader.readLine()) != null) {

				String[] cs = line.split("\t");
				String pro = cs[0].split("[\\s+]")[0];

				if (!proPepCountMap.containsKey(pro) || !proPsmCountMap.containsKey(pro)) {
					continue;
				}

				Object[] contents = new Object[csvTitle.length];
				contents[0] = proAccNameMap.get(pro);

				if (scoreMap.containsKey(pro)) {
					contents[1] = scoreMap.get(pro);
				} else {
					contents[1] = 0.0;
				}

				contents[2] = proPepCountMap.get(pro);

				contents[3] = proPsmCountMap.get(pro);

				for (int i = 0; i < fileIds.length; i++) {
					contents[i + 4] = Double.parseDouble(cs[fileIds[i]]);
				}

				try {
					proCsvPrinter.printRecord(contents);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + " error in writing proteins to " + this.final_pro_file.getName(), e);
					System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing proteins to "
							+ this.final_pro_file.getName());
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file);
		}

		try {
			proCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing proteins to " + this.final_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in writing proteins to "
					+ this.final_pro_file.getName());
		}

		LOGGER.info(taskName + ": proteins has been exported to " + final_pro_file);
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": proteins has been exported to " + final_pro_file);
	}

	/**
	 * not used now
	 */
	@SuppressWarnings("unused")
	private void exportProteinsXlsx() {

		LOGGER.info(taskName + ": exporting proteins...");

		Reader filter3Reader = null;
		Iterable<CSVRecord> records = null;

		try {
			filter3Reader = new FileReader(this.filtered_3_pros_file);
			records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(filter3Reader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading proteins from " + this.filtered_3_pros_file, e);
		}

		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		for (CSVRecord record : records) {
			String pro = record.get(0);
			pro = pro.split("[\\s+]")[0];
			double score = Double.parseDouble(record.get(1));
			scoreMap.put(pro, score);
		}

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont titleFont = workbook.createFont();
		titleFont.setBold(true);

		XSSFFont contentFont = workbook.createFont();
		contentFont.setBold(false);

		XSSFSheet sheet = workbook.createSheet("Protein list");
		style.setFont(titleFont);

		ArrayList<String> titleList = new ArrayList<String>();
		titleList.add("Protein");
		titleList.add("Score");
		titleList.add("Number of peptides");
		titleList.add("Number of PSMs");

		BufferedReader quanProReader = null;
		try {
			quanProReader = new BufferedReader(new FileReader(this.quan_pro_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file, e);
		}

		try {

			String line = quanProReader.readLine();
			String[] title = line.split("\t");
			ArrayList<String> tlist = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity")) {
					tlist.add(title[i]);
				}
			}
			String[] fileNames = tlist.toArray(new String[tlist.size()]);
			Arrays.sort(fileNames);

			for (String fn : fileNames) {
				titleList.add(fn);
			}

			int rowCount = 0;

			XSSFRow row0 = sheet.createRow(rowCount++);
			for (int i = 0; i < titleList.size(); i++) {
				XSSFCell cell = row0.createCell(i);
				cell.setCellValue(titleList.get(i));
				cell.setCellStyle(style);
			}

			int[] fileIds = new int[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (fileNames[i].equals(title[j])) {
						fileIds[i] = j;
					}
				}
			}

			while ((line = quanProReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String pro = cs[0].split("[\\s+]")[0];

				if (!proPepCountMap.containsKey(pro) || !proPsmCountMap.containsKey(pro)) {
					continue;
				}

				XSSFRow row = sheet.createRow(rowCount++);
				XSSFCell[] cells = new XSSFCell[titleList.size()];
				int id = 0;
				cells[id] = row.createCell(id);
				cells[id].setCellValue(proAccNameMap.get(pro));
				id++;

				if (scoreMap.containsKey(pro)) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(scoreMap.get(pro));
					id++;
				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(0.0);
					id++;
				}

				cells[id] = row.createCell(id);
				cells[id].setCellValue(proPepCountMap.get(pro));
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(proPsmCountMap.get(pro));
				id++;

				for (int i = 0; i < fileIds.length; i++) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(Double.parseDouble(cs[fileIds[i]]));
					id++;
				}
			}
			quanProReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file, e);
		}

		FileOutputStream out;
		try {
			out = new FileOutputStream(final_pro_file);
			workbook.write(out);
			workbook.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in writing result file to " + final_pro_file, e);
		}

		LOGGER.info(taskName + ": proteins has been exported to " + final_pro_file);
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void exportProteinFunction2() {

		LOGGER.info(taskName + ": exporting proteins...");

		Reader filter3Reader = null;
		Iterable<CSVRecord> records = null;

		try {
			filter3Reader = new FileReader(this.filtered_3_pros_file);
			records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(filter3Reader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading proteins from " + this.filtered_3_pros_file, e);
		}

		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		for (CSVRecord record : records) {
			String pro = record.get(0);
			pro = pro.split("[\\s+]")[0];
			pro = pro.split("|")[0];
			double score = Double.parseDouble(record.get(1));
			scoreMap.put(pro, score);
		}

		BufferedReader quanProReader = null;
		try {
			quanProReader = new BufferedReader(new FileReader(this.quan_pro_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file, e);
		}

		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		String[] fileNames = null;
		try {
			String line = quanProReader.readLine();
			String[] title = line.split("\t");

			ArrayList<String> tlist = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity")) {
					tlist.add(title[i]);
				}
			}

			fileNames = tlist.toArray(new String[tlist.size()]);
			Arrays.sort(fileNames);

			int[] fileIds = new int[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (fileNames[i].equals(title[j])) {
						fileIds[i] = j;
					}
				}
			}

			int groupId = 1;
			while ((line = quanProReader.readLine()) != null) {

				String[] cs = line.split("\t");
				String pro = cs[0].split("[\\s+]")[0];

				if (!proPepCountMap.containsKey(pro) || !proPsmCountMap.containsKey(pro)) {
					continue;
				}

				double score = 0;
				if (scoreMap.containsKey(pro)) {
					score = scoreMap.get(pro);
				}

				int pepCount = proPepCountMap.get(pro);
				int psmCount = proPsmCountMap.get(pro);

				double[] intensity = new double[fileIds.length];
				for (int i = 0; i < fileIds.length; i++) {
					intensity[i] = Double.parseDouble(cs[fileIds[i]]);
				}

				MetaProtein mp = new MetaProtein(groupId, 1, pro, pepCount, psmCount, score, intensity);
				list.add(mp);

				groupId++;
			}
			quanProReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file, e);
		}

		if (msv.funcAnnoSqlite()) {
			MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
			boolean match = this.funcAnnoSqlite(metapros, fileNames, final_pro_xml_file);
			if (!match) {
				LOGGER.error(taskName + ": error in searching functional annotation database");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in searching functional annotation database");
				return;
			}
		} else {
			MetaProteinAnno1[] metapros = new MetaProteinAnno1[list.size()];
			for (int i = 0; i < metapros.length; i++) {
				metapros[i] = new MetaProteinAnno1(list.get(i));
			}
			boolean match = this.funcAnnoTxt(metapros, fileNames, final_pro_xml_file);
			if (!match) {
				LOGGER.error(taskName + ": error in searching functional annotation database");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in searching functional annotation database");
				return;
			}
		}
		LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
				+ final_pro_xml_file.getName());
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());

		setProgress(99);

		File funcResultFile = final_pro_xml_file.getParentFile();
		File funCsv = new File(funcResultFile, "functions.csv");

		if (msv.funcAnnoSqlite()) {
			MetaProteinXMLReader2 funReader = null;
			try {
				funReader = new MetaProteinXMLReader2(final_pro_xml_file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!funCsv.exists()) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ funCsv.getName() + " started");
				try {
					funReader.exportCsv(funCsv);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ funCsv.getName() + " finished");
			}

			File html = new File(funcResultFile, "functions.html");
			if (!html.exists()) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ html.getName() + " started");
				try {
					funReader.exportHtml(html);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ html.getName() + " finished");
			}
		} else {
			MetaProteinXMLReader funReader = null;
			try {
				funReader = new MetaProteinXMLReader(final_pro_xml_file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!funCsv.exists()) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ funCsv.getName() + " started");
				try {
					funReader.exportCsv(funCsv);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ funCsv.getName() + " finished");
			}

			File html = new File(funcResultFile, "functions.html");
			if (!html.exists()) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ html.getName() + " started");
				try {
					funReader.exportHtml(html);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
						+ html.getName() + " finished");
			}
		}

		LOGGER.info(taskName + ": proteins has been exported to " + final_pro_file);
	}

	private boolean exportProteinFunction() throws DocumentException, IOException {

		LOGGER.info(taskName + ": exporting proteins with functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": exporting proteins with functional annotation started");

		File funcFile = new File(metaPar.getDbSearchResultFile(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		final_pro_xml_file = new File(funcFile, "functions.xml");

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
			return true;
		}

		Reader filter3Reader = null;
		Iterable<CSVRecord> records = null;

		try {
			filter3Reader = new FileReader(this.filtered_3_pros_file);
			records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(filter3Reader);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading proteins from " + this.filtered_3_pros_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + " error in reading proteins from "
					+ this.filtered_3_pros_file.getName());
		}

		HashMap<String, Double> scoreMap = new HashMap<String, Double>();

		for (CSVRecord record : records) {
			String pro = record.get(0);
			pro = pro.split("[\\s+]")[0];
			double score = Double.parseDouble(record.get(1));
			scoreMap.put(pro, score);
		}

		BufferedReader quanProReader = null;
		try {
			quanProReader = new BufferedReader(new FileReader(this.quan_pro_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		String[] fileNames = null;
		try {
			String line = quanProReader.readLine();
			String[] title = line.split("\t");

			ArrayList<String> tlist = new ArrayList<String>();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity")) {
					tlist.add(title[i]);
				}
			}

			fileNames = tlist.toArray(new String[tlist.size()]);
			Arrays.sort(fileNames);

			int[] fileIds = new int[fileNames.length];
			for (int i = 0; i < fileNames.length; i++) {
				for (int j = 0; j < title.length; j++) {
					if (fileNames[i].equals(title[j])) {
						fileIds[i] = j;
					}
				}
			}

			int groupId = 1;
			while ((line = quanProReader.readLine()) != null) {

				String[] cs = line.split("\t");
				String pro = cs[0].split("[\\s+]")[0];

				if (!proPepCountMap.containsKey(pro) || !proPsmCountMap.containsKey(pro)) {
					continue;
				}

				double score = 0;
				if (scoreMap.containsKey(pro)) {
					score = scoreMap.get(pro);
				}

				int pepCount = proPepCountMap.get(pro);
				int psmCount = proPsmCountMap.get(pro);

				double[] intensity = new double[fileIds.length];
				for (int i = 0; i < fileIds.length; i++) {
					intensity[i] = Double.parseDouble(cs[fileIds[i]]);
				}

				MetaProtein mp = new MetaProtein(groupId, 1, pro, pepCount, psmCount, score, intensity);
				list.add(mp);

				groupId++;
			}
			quanProReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + " error in reading quantified proteins from " + this.quan_pro_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ " error in reading quantified proteins from " + this.quan_pro_file.getName());
		}

		if (msv.funcAnnoSqlite()) {
			MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
			boolean match = this.funcAnnoSqlite(metapros, fileNames, final_pro_xml_file);
			if (!match) {
				LOGGER.error(taskName + ": error in searching functional annotation database");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in searching functional annotation database");
				return false;
			}
		} else {
			MetaProteinAnno1[] metapros = new MetaProteinAnno1[list.size()];
			for (int i = 0; i < metapros.length; i++) {
				metapros[i] = new MetaProteinAnno1(list.get(i));
			}
			boolean match = this.funcAnnoTxt(metapros, fileNames, final_pro_xml_file);
			if (!match) {
				LOGGER.error(taskName + ": error in searching functional annotation database");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in searching functional annotation database");
				return false;
			}
		}
		LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
				+ final_pro_xml_file.getName());
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());

		setProgress(99);

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());

			File funcResultFile = final_pro_xml_file.getParentFile();
			File funCsv = new File(funcResultFile, "functions.csv");

			if (msv.funcAnnoSqlite()) {
				MetaProteinXMLReader2 funReader = new MetaProteinXMLReader2(final_pro_xml_file);
				if (!funCsv.exists()) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ funCsv.getName() + " started");
					funReader.exportCsv(funCsv);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ funCsv.getName() + " finished");
				}

				File html = new File(funcResultFile, "functions.html");
				if (!html.exists()) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ html.getName() + " started");
					funReader.exportHtml(html);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ html.getName() + " finished");
				}
			} else {
				MetaProteinXMLReader funReader = new MetaProteinXMLReader(final_pro_xml_file);

				if (!funCsv.exists()) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ funCsv.getName() + " started");
					funReader.exportCsv(funCsv);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ funCsv.getName() + " finished");
				}

				File html = new File(funcResultFile, "functions.html");
				if (!html.exists()) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ html.getName() + " started");
					funReader.exportHtml(html);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
							+ html.getName() + " finished");
				}
			}

			File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");
			if (!report_function_summary.exists() || report_function_summary.length() == 0) {

				if (summaryMetaFile.exists()) {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.function, funCsv, report_function_summary, summaryMetaFile);

					task.runCLI();

				} else {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.function, funCsv, report_function_summary);

					task.runCLI();
				}
			}

			LOGGER.info(taskName + ": functional annotation finished");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": functional annotation finished");

			setProgress(100);

			return true;

		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean funcAnnoTxt(MetaProteinAnno1[] metapros, String[] fileNames, File final_pro_xml_file) {

		LOGGER.info(taskName + ": searching COG database");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": searching COG database");

		HashMap<String, String> cogMap = new HashMap<String, String>();
		String cogName = "";

		String funcDir = msv.getFunction();
		File[] funcDbFiles = (new File(funcDir)).listFiles();
		for (int i = 0; i < funcDbFiles.length; i++) {
			String name = funcDbFiles[i].getName();
			if (name.startsWith("COG")) {

				COGFinder cogFinder = new COGFinder(funcDbFiles[i].getAbsolutePath());

				cogFinder.match(metapros);
				HashMap<String, String> cogMapi = cogFinder.getFunctionMap();
				if (cogMapi.size() > cogMap.size()) {
					cogMap = cogMapi;
					cogName = name;
				}

			}
		}

		if (cogMap.size() == 0) {

			LOGGER.info(taskName + ": species unknown");
			LOGGER.info(taskName + ": task failed");

			System.out.println(format.format(new Date()) + "\t" + taskName + ": species unknown");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": task failed");

			return false;
		}

		setProgress(88);

		ArrayList<String> funcDbNameList = new ArrayList<String>();
		funcDbNameList.add("COG");

		ArrayList<HashMap<String, String>> funcResultList = new ArrayList<HashMap<String, String>>();
		funcResultList.add(cogMap);

		String species = cogName.substring(cogName.indexOf("_") + 1, cogName.indexOf("."));

		System.out.println(format.format(new Date()) + "\t" + taskName + ": the sample is identified as " + species
				+ " gut microbiome");

		File nogDb = new File(funcDir, "NOG_" + species + ".gc");
		if (nogDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching NOG database");

			NOGFinder nogfinder = new NOGFinder(nogDb.getAbsolutePath());
			funcDbNameList.add(nogfinder.getAbbreviation());

			nogfinder.match(metapros);
			HashMap<String, String> nogMap = nogfinder.getFunctionMap();
			funcResultList.add(nogMap);
		}

		setProgress(90);

		File keggDb = new File(funcDir, "KEGG_" + species + ".gc");
		if (keggDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching KEGG database");

			KEGGFinder keggfinder = new KEGGFinder(keggDb.getAbsolutePath());
			funcDbNameList.add(keggfinder.getAbbreviation());

			keggfinder.match(metapros);
			HashMap<String, String> keggMap = keggfinder.getFunctionMap();
			funcResultList.add(keggMap);
		}

		setProgress(92);

		File gobpDb = new File(funcDir, "GOBP_" + species + ".gc");
		if (gobpDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching GOBP database");

			GOBPFinder gobpfinder = new GOBPFinder(gobpDb.getAbsolutePath());
			funcDbNameList.add(gobpfinder.getAbbreviation());

			gobpfinder.match(metapros);
			HashMap<String, String> gobpMap = gobpfinder.getFunctionMap();
			funcResultList.add(gobpMap);
		}

		setProgress(94);

		File goccDb = new File(funcDir, "GOCC_" + species + ".gc");
		if (goccDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching GOCC database");

			GOCCFinder goccfinder = new GOCCFinder(goccDb.getAbsolutePath());
			funcDbNameList.add(goccfinder.getAbbreviation());

			goccfinder.match(metapros);
			HashMap<String, String> goccMap = goccfinder.getFunctionMap();
			funcResultList.add(goccMap);
		}

		setProgress(96);

		File gomfDb = new File(funcDir, "GOMF_" + species + ".gc");
		if (gomfDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching GOMF database");

			GOMFFinder gomffinder = new GOMFFinder(gomfDb.getAbsolutePath());
			funcDbNameList.add(gomffinder.getAbbreviation());

			gomffinder.match(metapros);
			HashMap<String, String> gomfMap = gomffinder.getFunctionMap();
			funcResultList.add(gomfMap);
		}

		setProgress(98);

		File koDb = new File(funcDir, "KO_" + species + ".gc");
		if (koDb.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": searching KO database");

			KOFinder kofinder = new KOFinder(koDb.getAbsolutePath());
			funcDbNameList.add(kofinder.getAbbreviation());

			kofinder.match(metapros);
			HashMap<String, String> koMap = kofinder.getFunctionMap();
			funcResultList.add(koMap);
		}

		HashMap<String, String>[] maps = funcResultList.toArray(new HashMap[funcResultList.size()]);
		String[] funcDbNames = funcDbNameList.toArray(new String[funcDbNameList.size()]);

		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = "Intensity " + fileNames[i].substring(fileNames[i].indexOf("_") + 1);
		}

		MetaProteinXMLWriter writer = new MetaProteinXMLWriter(final_pro_xml_file.getAbsolutePath(),
				MetaConstants.pFind, MetaConstants.labelFree, fileNames);

		writer.addProteins(metapros, maps, funcDbNames, new CategoryFinder(msv));
		writer.close();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {
			LOGGER.error(taskName + ": error in proteins functional annotations, task failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in proteins functional annotations, task failed");

			return false;
		}

		return true;
	}

	private boolean funcAnnoSqlite(MetaProtein[] proteins, String[] fileNames, File final_pro_xml_file) {

		MetaProteinAnnoEggNog[] metapros = null;
		IGCFuncSqliteSearcher funcDb = null;
		try {
			funcDb = new IGCFuncSqliteSearcher(msv.getFunction(), msv.getFuncDef());
			metapros = funcDb.match(proteins);
		} catch (NumberFormatException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in proteins functional annotations, task failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in proteins functional annotations, task failed");
			return false;
		}

		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = "Intensity " + fileNames[i].substring(fileNames[i].indexOf("_") + 1);
		}

		MetaProteinXMLWriter2 writer = new MetaProteinXMLWriter2(final_pro_xml_file.getAbsolutePath(),
				MetaConstants.pFind, MetaConstants.labelFree, fileNames);

		writer.addProteins(metapros, funcDb.getUsedCogMap(), funcDb.getUsedNogMap(), funcDb.getUsedKeggMap(),
				funcDb.getUsedGoMap(), funcDb.getUsedEcMap());
		writer.close();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(taskName + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {
			LOGGER.error(taskName + ": error in proteins functional annotations, task failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in proteins functional annotations, task failed");

			return false;
		}

		return true;
	}

	private boolean exportReport() {

		bar2.setString(taskName + ": exporting report");

		LOGGER.info(taskName + ": exporting report started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting report started");

		File reportDir = new File(this.result, "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, "2.0.0");

		metaPar.setReportDir(reportDir);

		this.reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportHtmlDir.exists()) {
			reportHtmlDir.mkdirs();
		}

		this.summaryMetaFile = new File(this.result, "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!final_summary_txt.exists() || final_summary_txt.length() == 0) {

			LOGGER.error(taskName + ": error in writing PSM identification summary to " + final_summary_txt.getName());
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing PSM identification summary to " + final_summary_txt.getName());

			return false;

		} else {
			File report_ID_summary = new File(reportHtmlDir, "report_ID_summary.html");
			if (!report_ID_summary.exists() || report_ID_summary.length() == 0) {
				MetaReportTask task = new MetaReportTask(1);
				task.addTask(MetaReportTask.summary, final_summary_txt, report_ID_summary);
				task.run();
			}
		}

		this.final_pep_txt = new File(this.result, "final_peptides.txt");
		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			this.exportPeptideTxt();
		}

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		} else {
			File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
			if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {

				if (summaryMetaFile.exists()) {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary, summaryMetaFile);

					task.run();

				} else {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary);

					task.run();
				}
			}
		}

		this.final_pro_txt = new File(this.result, "final_proteins.txt");
		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			this.exportProteinsTxt();
		}
		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			return false;
		} else {
			File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
			if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary, summaryMetaFile);

					task.run();

				} else {
					MetaReportTask task = new MetaReportTask(1);
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary);

					task.run();
				}
			}
		}

		return true;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		this.database = new File(metaPar.getCurrentDb());
		this.result = new File(metaPar.getDbSearchResultFile(), "result");
		this.ms2_count_file = new File(this.result, "ms2Count.tsv");
		this.filtered_3_peps_file = new File(this.result, "filter3_peps.csv");
		this.filtered_3_pros_file = new File(this.result, "filter3_pros.csv");
		this.filtered_3_psms_file = new File(this.result, "filter3_psms.tsv");
		this.filtered_3_mods_file = new File(this.result, "filter3_mods.xml");

		this.quan_peak_file = new File(this.result, "QuantifiedPeaks.tsv");
		this.quan_pep_file = new File(this.result, "QuantifiedPeptides.tsv");
		this.quan_pro_file = new File(this.result, "QuantifiedProteins.tsv");

		this.final_psm_file = new File(this.result, "final_psms.csv");
		this.final_pep_file = new File(this.result, "final_peptides.csv");
		this.final_pro_file = new File(this.result, "final_proteins.csv");

		this.final_mod_xml_file = new File(this.result, "final_mods.xml");
		this.final_mod_file = new File(this.result, "final_mods.xlsx");

		this.aa_around_file = new File(this.result, "aminoacid_around.tsv");

		this.expNameMap = new HashMap<String, String>();
		MetaData metadata = this.metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		for (int i = 0; i < rawFiles.length; i++) {
			String name = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1, rawFiles[i].lastIndexOf("."));
			this.expNameMap.put(name, expNames[i]);
		}

		MetaOpenPar mop = ((MetaParameterMQ) metaPar).getOsp();
		if (mop.isOpenSearch()) {

			this.exportMods();
			if (!final_mod_xml_file.exists() || final_mod_xml_file.length() == 0) {
				return false;
			}

			this.exportOpenPSMs();
			if (!final_psm_file.exists() || final_psm_file.length() == 0) {
				return false;
			}

			this.exportOpenPeptideCsv();
			if (!final_pep_file.exists() || final_pep_file.length() == 0) {
				return false;
			}

		} else {

			this.exportClosedPSMs();
			if (!final_psm_file.exists() || final_psm_file.length() == 0) {
				return false;
			}

			this.exportClosedPeptideCsv();
			if (!final_pep_file.exists() || final_pep_file.length() == 0) {
				return false;
			}
		}

		this.exportProteins();
		if (!final_pro_file.exists() || final_pro_file.length() == 0) {
			return false;
		}

		exportReport();

		if (metaPar.isMetaWorkflow()) {
			if (mop.isOpenSearch()) {

				boolean taxonomy = this.exportOpenPeptideTaxa();
				if (!taxonomy) {
					return false;
				}

				boolean function = this.exportProteinFunction();

				if (!function) {
					return false;
				}
			} else {

				boolean taxonomy = this.exportClosedPeptideTaxa();
				if (!taxonomy) {
					return false;
				}

				boolean function = this.exportProteinFunction();

				if (!function) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
}
