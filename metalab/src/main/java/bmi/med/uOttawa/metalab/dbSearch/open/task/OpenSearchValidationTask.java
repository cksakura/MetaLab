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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JProgressBar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerPsm;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerReader;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPSM;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlReader;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenModXmlWriter;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPIAXMLWriter;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvReader;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvWriter;
import bmi.med.uOttawa.metalab.dbSearch.pia.InferedPeptide;
import bmi.med.uOttawa.metalab.dbSearch.pia.InferedProtein;
import bmi.med.uOttawa.metalab.dbSearch.pia.ProteinInferenceReader;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import de.mpc.pia.modeller.PIAModeller;

/**
 * @author Kai Cheng
 *
 */
public class OpenSearchValidationTask extends MetaAbstractTask {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenSearchValidationTask.class);

	private static String taskName = "Open search result validation";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File[] rawFiles;
	private File[] mgfFiles;
	private File[] pepxmlFiles;
	private File fasta;
	private File result;

	private File peaks;
	private File bins;
	private File ms2count;
	private File piaParameter;
	private File filtered_1_psms_file;
	private File filtered_1_mods_file;

	private File filtered_2_psms_file;
	private File filtered_2_psms_file_backup;
	private File filtered_2_peps_file;
	private File filtered_2_pros_file;

	private File filtered_3_psms_file;
	private File filtered_3_mods_file;
	private File filtered_3_peps_file;
	private File filtered_3_pros_file;

	private OpenPSM[] filtered_1_psms;
	private OpenMod[] filtered_1_mods;
	private HashMap<Double, PostTransModification> filtered_1_setModMap;
	private OpenPSM[] filtered_2_psms;

	private PrintWriter summaryWriter;

	private String ms2ScanMode;
	private double psm_fdr;
	private double pep_fdr;
	private double pro_fdr;

	private double ttest_threshold = 0.01;
	private int ppm_threshold = 10;
	private double mono_threshold = 0.003;
	private int leastTTestN = 15;
	private String rev_symbol = "REV_";

	/**
	 * nbt.1511-S1
	 */
//	private final double delta_m_isotope1 = 1.00286864;

	private DecimalFormat dfP2 = FormatTool.getDFP2();

	private MetaParameterMQ metaPar;
	private MetaSourcesV2 advPar;

	public OpenSearchValidationTask(MetaParameterMQ metaPar, MetaSourcesV2 advPar, JProgressBar bar1, JProgressBar bar2,
			OpenQuanTask quanTask) {
		super(metaPar, advPar, bar1, bar2, quanTask);
		this.metaPar = metaPar;
		this.advPar = advPar;
		this.bar1 = bar1;
		this.bar2 = bar2;
		this.ms2ScanMode = metaPar.getMs2ScanMode();

		MetaOpenPar mop = metaPar.getOsp();
		this.psm_fdr = mop.getOpenPsmFDR();
		this.pep_fdr = mop.getOpenPepFDR();
		this.pro_fdr = mop.getOpenProFDR();
	}

	public OpenSearchValidationTask(String raw_dir, String mgf_dir, String xml_dir, String fasta, String result) {
		this(new File(raw_dir), new File(mgf_dir), new File(xml_dir), new File(fasta), new File(result));
	}

	public OpenSearchValidationTask(File raw_dir, File mgf_dir, File pepxml_dir, File fasta, File result) {

		LOGGER.info(taskName + ": started");

		this.rawFiles = raw_dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("raw") || name.endsWith("RAW"))
					return true;
				return false;
			}
		});

		this.mgfFiles = mgf_dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("mgf") || name.endsWith("MGF"))
					return true;
				return false;
			}
		});

		LOGGER.info("Parsing pepxml files started");

		ArrayList<File> templist = new ArrayList<File>();
		File[] files = pepxml_dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("pepXML")) {
				File refinedPepxml;
				try {
					refinedPepxml = refine(files[i]);
					templist.add(refinedPepxml);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in parsing pepxml file " + files[i].getName(), e);
				}

			}
		}
		this.pepxmlFiles = templist.toArray(new File[templist.size()]);

		LOGGER.info("Parsing pepxml files finished");

		this.fasta = fasta;
		this.result = result;
		if (!result.exists()) {
			this.result.mkdirs();
		}

		this.peaks = new File(this.result, "peaks.tsv");
		this.bins = new File(this.result, "bins.tsv");
		this.filtered_1_psms_file = new File(this.result, "filter1_psms.tsv");
		this.filtered_1_mods_file = new File(this.result, "filter1_mods.xml");

		this.filtered_2_psms_file = new File(this.result, "filter2_psms.xml");
		this.filtered_2_psms_file_backup = new File(this.result, "filter2_psms.tsv");
		this.filtered_2_peps_file = new File(this.result, "filter2_peps.csv");
		this.filtered_2_pros_file = new File(this.result, "filter2_pros.csv");

		this.filtered_3_psms_file = new File(this.result, "filter3_psms.tsv");
		this.filtered_3_mods_file = new File(this.result, "filter3_mods.xml");
		this.filtered_3_peps_file = new File(this.result, "filter3_peps.csv");
		this.filtered_3_pros_file = new File(this.result, "filter3_pros.csv");

		File summary = new File(this.result, "summary_" + format.format(new Date()) + ".txt");
		try {
			this.summaryWriter = new PrintWriter(summary);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in creating summary file", e);
		}
	}

	private File refine(File in) throws IOException {

		File out = new File(in.getParentFile(), in.getName());
		File temp = new File(in.getParentFile(), in.getName() + ".temp");
		in.renameTo(temp);

		BufferedReader reader = new BufferedReader(new FileReader(temp));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("<spectrum_query")) {

				int assumed_charge = line.indexOf("assumed_charge=");
				int spectrum = line.indexOf("spectrum=");
				int end = line.indexOf("end_scan=");

				String spstring = line.substring(spectrum, end);
				int file = spstring.indexOf("File:");

				if (file > 0) {
					String[] cs = spstring.substring(0, file).split("\\.");
					StringBuilder sb = new StringBuilder();

					sb.append("<spectrum_query start_scan=\"").append(cs[cs.length - 2]).append("\" ");
					sb.append(line.substring(assumed_charge, spectrum + file - 1)).append("\" ");
					sb.append(line.substring(end));

					writer.println(sb);
				} else {
					writer.println(line);
				}

			} else if (line.startsWith("<search_hit")) {
				String reline = line.substring(1, line.length() - 1);
				if (reline.indexOf("<") > -1) {
					reline = reline.replaceAll("<", "_");
					reline = reline.replaceAll(">", "_");

				} else if (reline.indexOf("&") > -1) {
					reline = reline.replaceAll("&", "_");
				}

				writer.println("<" + reline + ">");

			} else {
				writer.println(line);
			}
		}
		reader.close();
		writer.close();

		FileUtils.deleteQuietly(temp);

		return out;
	}

	/**
	 * Gaussian fitting
	 */
	private void filtering_1() {

		LOGGER.info(taskName + ": stage 1 started");

		if (filtered_1_psms_file.exists() && filtered_1_mods_file.exists()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_1_psms_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_1_mods_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": go to next step");
			return;
		}

		MSFraggerReader psmReader = new MSFraggerReader(this.pepxmlFiles);
		MSFraggerPsm[][] original_psms = psmReader.getPSMArrays();
		HashMap<Double, PostTransModification> ptmmap = psmReader.getPTMMap();

		if (original_psms == null) {
			LOGGER.error(taskName + ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed");
			return;
		}

		int original_target = 0;
		int original_decoy = 0;

		for (int i = 0; i < original_psms.length; i++) {
			for (int j = 0; j < original_psms[i].length; j++) {
				if (original_psms[i][j].isTarget()) {
					original_target++;
				} else {
					original_decoy++;
				}
			}
		}

		double original_fdr = (double) original_decoy / (double) original_target;
		this.summaryWriter.println("Total PSM count from open search: " + (original_target + original_decoy));
		this.summaryWriter.println("Target PSM count from open search: " + original_target);
		this.summaryWriter.println("Decoy PSM count from open search: " + original_decoy);
		this.summaryWriter.println("FDR: " + dfP2.format(original_fdr));
		this.summaryWriter.println("***");

		System.out.println(format.format(new Date()) + "\t" + taskName + ": Total PSM count from open search: "
				+ (original_target + original_decoy));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Target PSM count from open search: "
				+ original_target);
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Decoy PSM count from open search: " + original_decoy);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(original_fdr));

		OpenSearchGaussianFitting gaussian = null;
		PrintWriter peaksWriter = null;
		try {
			peaksWriter = new PrintWriter(this.peaks);
			gaussian = new OpenSearchGaussianFitting(advPar.getUnimod());
		} catch (DocumentException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed",
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed");
		}

		if (gaussian == null) {
			LOGGER.error(taskName + ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in parsing PSMs from MSFragger search results, filtering stage 1 failed");
			return;
		}

		MSFraggerPsm[] gaussian_psms = null;
		try {
			gaussian_psms = gaussian.fit(original_psms, ptmmap, ppm_threshold, peaksWriter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in Gaussian fitting of PSMs, filtering stage 1 failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in Gaussian fitting of PSMs, filtering stage 1 failed");
		}

		if (gaussian_psms == null) {
			LOGGER.error(taskName + ": error in Gaussian fitting of PSMs, filtering stage 1 failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in Gaussian fitting of PSMs, filtering stage 1 failed");
			return;
		}

		this.filtered_1_mods = gaussian.getModInfo();
		this.filtered_1_setModMap = gaussian.getSetModMap();

		peaksWriter.close();
		try {
			gaussian.outputBins(bins);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in Gaussian fitting of PSMs, filtering stage 1 failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in Gaussian fitting of PSMs, filtering stage 1 failed");
		}

		int gaussian_target = 0;
		int gaussian_decoy = 0;

		for (MSFraggerPsm psm : gaussian_psms) {
			if (psm.isTarget()) {
				gaussian_target++;
			} else {
				gaussian_decoy++;
			}
		}
		double gaussian_fdr = (double) gaussian_decoy / (double) gaussian_target;
		this.summaryWriter.println("Total PSM count after Gaussian fitting: " + gaussian_psms.length);
		this.summaryWriter.println("Target PSM count after Gaussian fitting: " + gaussian_target);
		this.summaryWriter.println("Decoy PSM count after Gaussian fitting: " + gaussian_decoy);
		this.summaryWriter.println("FDR: " + dfP2.format(gaussian_fdr));
		this.summaryWriter.println("Potential modification count: " + filtered_1_mods.length);
		this.summaryWriter.println("***");

		System.out.println(format.format(new Date()) + "\t" + taskName + ": Total PSM count after Gaussian fitting: "
				+ gaussian_psms.length);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Target PSM count after Gaussian fitting: "
				+ gaussian_target);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Decoy PSM count after Gaussian fitting: "
				+ gaussian_decoy);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(gaussian_fdr));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Potential modification count: "
				+ filtered_1_mods.length);

		OpenSearchMachLearning percolator = new OpenSearchMachLearning(gaussian_psms, filtered_1_mods, 0,
				this.ms2ScanMode);
		try {
			this.filtered_1_psms = percolator.assignScores(mgfFiles);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName
					+ ": error in assigning score for PSM by semi-supervised machine learning strategy, filtering stage 1 failed",
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in assigning score for PSM by semi-supervised machine learning strategy, filtering stage 1 failed");
		}

		if (this.filtered_1_psms == null) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName
					+ ": error in assigning score for PSM by semi-supervised machine learning strategy, filtering stage 1 failed");
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in assigning score for PSM by semi-supervised machine learning strategy, filtering stage 1 failed");
			return;
		}

		OpenModXmlWriter modWriter = null;
		try {
			modWriter = new OpenModXmlWriter(filtered_1_mods_file);
			modWriter.addMods(filtered_1_mods);
			modWriter.addSetMods(filtered_1_setModMap, filtered_1_mods[0]);
			modWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing potential modifications to " + filtered_1_mods_file.getName()
					+ ", filtering stage 1 failed", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing potential modifications to "
							+ filtered_1_mods_file.getName() + ", filtering stage 1 failed");
		}

		OpenPsmTsvWriter psmwriter = new OpenPsmTsvWriter(filtered_1_psms_file, OpenPsmTsvWriter.full_information);

		for (OpenPSM op : filtered_1_psms) {
			psmwriter.addPSM(op);
		}

		psmwriter.close();

		HashMap<String, Integer> ms2CountMap = percolator.getMs2CountMap();
		PrintWriter ms2CountWriter = null;
		try {
			ms2CountWriter = new PrintWriter(ms2count);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in writing MS/MS count to " + ms2count.getName() + ", filtering stage 1 failed",
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing MS/MS count to "
					+ ms2count.getName() + ", filtering stage 1 failed");
		}
		ms2CountWriter.println("File\tMS2 count");
		for (String key : ms2CountMap.keySet()) {
			ms2CountWriter.println(key + "\t" + ms2CountMap.get(key));
		}
		ms2CountWriter.close();

		LOGGER.info(taskName + ": stage 1 finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": stage 1 finished");
	}

	/**
	 * protein inference
	 */
	@SuppressWarnings("unchecked")
	private void filtering_2() {

		LOGGER.info(taskName + ": stage 2 started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": stage 2 started");

		if (filtered_2_psms_file.exists() && filtered_2_peps_file.exists() && filtered_2_pros_file.exists()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_psms_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_peps_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_pros_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + "go to next step");

			return;
		}

		if (filtered_1_psms == null) {
			OpenPsmTsvReader reader;
			try {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": Reading PSMs from "
						+ filtered_1_psms_file.getName());

				reader = new OpenPsmTsvReader(this.filtered_1_psms_file);
				filtered_1_psms = reader.getAllOpenPSMs();

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_1_psms.length
						+ " PSMs found in " + filtered_1_psms_file.getName());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading PSMs from " + filtered_1_psms_file.getName()
						+ ", filtering stage 2 failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
						+ filtered_1_psms_file.getName() + ", filtering stage 2 failed");
			}

			OpenModXmlReader modreader;
			try {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": Reading potential PTMs from "
						+ filtered_1_mods_file.getName());

				modreader = new OpenModXmlReader(filtered_1_mods_file);
				filtered_1_mods = modreader.getOpenMods();
				filtered_1_setModMap = modreader.getSetModMap();

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_1_mods.length
						+ " potential PTMs found in " + filtered_1_mods_file.getName());

			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading potential PTMs from " + filtered_1_mods_file.getName()
						+ ", filtering stage 2 failed", e);
				System.err
						.println(format.format(new Date()) + "\t" + taskName + ": error in reading potential PTMs from "
								+ filtered_1_mods_file.getName() + ", filtering stage 2 failed");
			}
		}

		HashMap<Integer, ArrayList<Double>[]> modmap = new HashMap<Integer, ArrayList<Double>[]>();
		HashMap<Integer, ArrayList<Integer>> psmmap = new HashMap<Integer, ArrayList<Integer>>();
		SummaryStatistics totalDecoy = new SummaryStatistics();

		for (int i = 0; i < filtered_1_psms.length; i++) {
			int modid = filtered_1_psms[i].getOpenModId();
			boolean isTarget = filtered_1_psms[i].isTarget();
			double score = filtered_1_psms[i].getClassScore();

			if (isTarget) {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[0].add(score);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					tlist.add(score);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
			} else {
				if (modmap.containsKey(modid)) {
					modmap.get(modid)[1].add(score);
				} else {
					ArrayList<Double> tlist = new ArrayList<Double>();
					ArrayList<Double> dlist = new ArrayList<Double>();
					dlist.add(score);
					modmap.put(modid, new ArrayList[] { tlist, dlist });
				}
				totalDecoy.addValue(score);
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

		Integer[] modkey = modmap.keySet().toArray(new Integer[modmap.size()]);
		Arrays.sort(modkey);
		int filteredModCount = 0;

		for (int i = 0; i < modkey.length; i++) {

			ArrayList<Double> tlist = modmap.get(modkey[i])[0];
			ArrayList<Double> dlist = modmap.get(modkey[i])[1];

			if (tlist.size() < leastTTestN) {
				continue;
			}

			double fdri = (double) dlist.size() / (double) tlist.size();

			if (fdri > psm_fdr * 5) {

				double medianT = MathTool.getMedian(tlist);
				double medianD = MathTool.getMedian(dlist);

				if (medianT < medianD) {
					continue;
				}

				SummaryStatistics sstarget = new SummaryStatistics();
				for (int j = 0; j < tlist.size(); j++) {
					sstarget.addValue(tlist.get(j));
				}

				if (dlist.size() < leastTTestN) {
					if (!TestUtils.tTest(sstarget, totalDecoy, ttest_threshold)) {
						continue;
					}
				} else {
					SummaryStatistics ssdecoy = new SummaryStatistics();
					for (int j = 0; j < dlist.size(); j++) {
						ssdecoy.addValue(dlist.get(j));
					}
					if (!TestUtils.tTest(sstarget, ssdecoy, ttest_threshold)
							|| !TestUtils.tTest(sstarget, totalDecoy, ttest_threshold)) {
						continue;
					}
				}
			}

			ArrayList<Integer> psmIdList = psmmap.get(modkey[i]);
			OpenPSM[] psms = new OpenPSM[psmIdList.size()];
			for (int j = 0; j < psms.length; j++) {
				psms[j] = filtered_1_psms[psmIdList.get(j)];
				filteredList.add(psms[j]);
			}
			filteredModCount++;
		}

		this.filtered_2_psms = filteredList.toArray(new OpenPSM[filteredList.size()]);
		Arrays.sort(this.filtered_2_psms, new OpenPSM.InverseClassScoreComparator());

		int target = 0;
		int decoy = 0;
		for (int i = 0; i < this.filtered_2_psms.length; i++) {
			boolean isTarget = this.filtered_2_psms[i].isTarget();
			if (isTarget) {
				target++;
			} else {
				decoy++;
			}
			double fdr = (double) decoy / (double) target;
			this.filtered_2_psms[i].setQValue(fdr);
		}

		double ttest_fdr = (double) decoy / (double) target;
		this.summaryWriter.println("Total PSM count after t-test: " + this.filtered_2_psms.length);
		this.summaryWriter.println("Target PSM count after t-test: " + target);
		this.summaryWriter.println("Decoy PSM count after t-test: " + decoy);
		this.summaryWriter.println("FDR: " + dfP2.format(ttest_fdr));
		this.summaryWriter.println("Potential modification count after t-test and deisotope: " + filteredModCount);
		this.summaryWriter.println("***");

		System.out.println(format.format(new Date()) + "\t" + taskName + ": Total PSM count after t-test: "
				+ this.filtered_2_psms.length);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Target PSM count after t-test: " + target);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Decoy PSM count after t-test: " + decoy);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(ttest_fdr));
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": Potential modification count after t-test and deisotope: " + filteredModCount);

		OpenPsmTsvWriter psmwriter = new OpenPsmTsvWriter(this.filtered_2_psms_file_backup,
				OpenPsmTsvWriter.full_information);

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(filtered_2_psms_file);
		writer.initialMSFragger(this.fasta, this.rawFiles);

		for (OpenPSM psm : filtered_2_psms) {
			psmwriter.addPSM(psm);
			double modMass = psm.getOpenModmass();
			if (modMass == 0) {
				if (psm.getClassScore() > 90) {
					writer.add(psm);
				}
			} else {
				if (psm.getClassScore() > 10) {
					writer.add(psm);
				}
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PSMs to " + this.filtered_2_psms_file.getName()
					+ ", filtering stage 2 failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PSMs to "
					+ this.filtered_2_psms_file.getName() + ", filtering stage 2 failed");
		}

		psmwriter.close();

//		ProteinInferenceTask.PIA(filtered_2_psms_file.getAbsolutePath(), this.filtered_2_peps_file.getAbsolutePath(),
//				this.filtered_2_pros_file.getAbsolutePath());

//		ProteinInferenceTask.PIAByJAR(filtered_2_psms_file.getAbsolutePath(),
//				this.filtered_2_peps_file.getAbsolutePath(), this.filtered_2_pros_file.getAbsolutePath());

//		URL url = OpenSearchValidationTask.class.getResource("/de/mpc/pia/parameter.xml");

		String[] args = new String[] { "-infile", "\"" + filtered_2_psms_file.getAbsolutePath() + "\"", "-paramFile",
				"\"" + this.piaParameter.getAbsolutePath() + "\"", "-peptideExport",
				"\"" + this.filtered_2_peps_file.getAbsolutePath() + "\"", "csv", "-proteinExport",
				"\"" + this.filtered_2_pros_file.getAbsolutePath() + "\"", "csv" };

		PIAModeller.main(args);

		LOGGER.info(taskName + ": stage 2 finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": stage 1 finished");
	}

	private void filtering_3() {

		LOGGER.info(taskName + ": stage 3 started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": stage 3 started");

		if (filtered_2_psms == null) {
			OpenPsmTsvReader reader;
			try {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": Reading PSMs from "
						+ filtered_2_psms_file_backup.getName());

				reader = new OpenPsmTsvReader(this.filtered_2_psms_file_backup);
				filtered_2_psms = reader.getAllOpenPSMs();
				Arrays.sort(filtered_2_psms, new OpenPSM.InverseClassScoreComparator());

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_psms.length
						+ " PSMs found in " + filtered_2_psms_file_backup.getName());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading PSMs from " + filtered_2_psms_file_backup.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading PSMs from "
						+ filtered_2_psms_file_backup.getName());
			}

			OpenModXmlReader modreader;
			try {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": Reading potential PTMs from "
						+ filtered_1_mods_file.getName());

				modreader = new OpenModXmlReader(filtered_1_mods_file);
				filtered_1_mods = modreader.getOpenMods();
				filtered_1_setModMap = modreader.getSetModMap();

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_1_mods.length
						+ " potential PTMs found in " + filtered_1_mods_file.getName());

			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in reading potential PTMs from " + filtered_1_mods_file.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading potential PTMs from " + filtered_1_mods_file.getName());
			}
		}

		int psmt = 0;
		int psmd = 0;

		HashSet<String> psmpepset = new HashSet<String>();
		HashSet<String> psmproset = new HashSet<String>();

		int breakId = -1;
		for (int i = 0; i < filtered_2_psms.length; i++) {
			if (filtered_2_psms[i].isTarget()) {
				psmt++;
			} else {
				psmd++;
			}
			double fdr = (double) psmd / (double) psmt;
			if (fdr > this.psm_fdr) {
				if (breakId == -1) {
					breakId = i;
				}
			} else {
				if (breakId != -1) {
					breakId = -1;
				}
			}
		}
		if (breakId == -1) {
			breakId = filtered_2_psms.length;
		}

		for (int i = 0; i < breakId; i++) {
			String pro = filtered_2_psms[i].getProtein();
			String seq = filtered_2_psms[i].getSequence();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			psmpepset.add(seq);
			psmproset.add(acc);
		}

//		InferedPeptide[] peptides = ProteinInferenceReader.getAllPeptides(filtered_2_peps_file);
		InferedPeptide[] peptides = null;
		try {
			peptides = ProteinInferenceReader.getAllPeptides1360(filtered_2_peps_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading peptides from " + filtered_2_peps_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading peptides from "
					+ filtered_2_peps_file.getName());
		}
		Arrays.sort(peptides);

		HashSet<String> peppepset = new HashSet<String>();
		HashSet<String> pepproset = new HashSet<String>();

		int pept = 0;
		int pepd = 0;

		breakId = -1;

		for (int i = 0; i < peptides.length; i++) {
			String pro = peptides[i].getAccession();
			String seq = peptides[i].getName();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (!psmpepset.contains(seq) || !psmproset.contains(acc)) {
				continue;
			}
			boolean isTarget = !pro.startsWith(rev_symbol);
			if (isTarget) {
				pept++;
			} else {
				pepd++;
			}
			double fdr = (double) pepd / (double) pept;
			if (fdr > this.pep_fdr) {
				if (breakId == -1) {
					breakId = i;
				}
			} else {
				if (breakId != -1) {
					breakId = -1;
				}
			}
		}

		if (breakId == -1) {
			breakId = peptides.length;
		}

		for (int i = 0; i < breakId; i++) {
			String pro = peptides[i].getAccession();
			String seq = peptides[i].getName();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (!psmpepset.contains(seq) || !psmproset.contains(acc)) {
				continue;
			}

			peppepset.add(seq);
			pepproset.add(acc);
		}

//		InferedProtein[] proteins = ProteinInferenceReader.getAllProteins(filtered_2_pros_file);
		InferedProtein[] proteins = null;
		try {
			proteins = ProteinInferenceReader.getAllProteins1360(filtered_2_pros_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading proteins from " + filtered_2_peps_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading proteins from "
					+ filtered_2_peps_file.getName());
		}
		Arrays.sort(proteins);

		HashSet<String> proset = new HashSet<String>();

		int prot = 0;
		int prod = 0;

		breakId = -1;

		for (int i = 0; i < proteins.length; i++) {
			String pro = proteins[i].getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}
			if (!pepproset.contains(acc)) {
				continue;
			}
			boolean isTarget = !pro.startsWith(rev_symbol);
			if (isTarget) {
				prot++;
			} else {
				prod++;
			}
			double fdr = (double) prod / (double) prot;
			if (fdr > this.pro_fdr) {
				if (breakId == -1) {
					breakId = i;
				}
			} else {
				if (breakId != -1) {
					breakId = -1;
				}
			}
		}

		if (breakId == -1) {
			breakId = proteins.length;
		}

		for (int i = 0; i < breakId; i++) {
			String pro = proteins[i].getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}
			if (!pepproset.contains(acc)) {
				continue;
			}
			proset.add(acc);
		}

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter proCsvPrinter = null;
		try {
			proCsvPrinter = new CSVPrinter(new FileWriter(this.filtered_3_pros_file), csvFileFormat);
			String[] protitle = ProteinInferenceReader.getProteinTitle();
			proCsvPrinter.printRecord(protitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in creating csv writer in " + this.filtered_3_pros_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating csv writer in "
					+ this.filtered_3_pros_file.getName());
		}

		int calProt = 0;
		int calProd = 0;
		for (InferedProtein ip : proteins) {
			String pro = ip.getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (proset.contains(acc)) {
				boolean isTarget = !pro.startsWith(rev_symbol);
				if (isTarget) {
					calProt++;
					try {
						proCsvPrinter.printRecord(ip.getContentObject());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error(taskName + ": error in writing protein \"" + ip.getName() + "\" to "
								+ this.filtered_3_pros_file.getName(), e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing protein \""
								+ ip.getName() + "\" to " + this.filtered_3_pros_file.getName());
					}
				} else {
					calProd++;
				}
			}
		}
		try {
			proCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing proteins to " + this.filtered_3_pros_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing proteins to "
					+ this.filtered_3_pros_file.getName());
		}

		double profdr = (double) calProd / (double) calProt;
		this.summaryWriter.println("Final total protein count: " + (calProd + calProt));
		this.summaryWriter.println("Final target protein count: " + calProt);
		this.summaryWriter.println("Final decoy protein count: " + calProd);
		this.summaryWriter.println("FDR: " + dfP2.format(profdr));
		this.summaryWriter.println("***");

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total protein count: " + (calProd + calProt));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target protein count: " + calProt);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy protein count: " + calProd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(profdr));

		CSVPrinter pepCsvPrinter = null;
		try {
			pepCsvPrinter = new CSVPrinter(new FileWriter(this.filtered_3_peps_file), csvFileFormat);
			pepCsvPrinter.printRecord(ProteinInferenceReader.getPeptideTitle());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in creating csv writer in " + this.filtered_3_peps_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating csv writer in "
					+ this.filtered_3_peps_file.getName());
		}

		int calPept = 0;
		int calPepd = 0;
		for (InferedPeptide ip : peptides) {
			String seq = ip.getName();
			String pro = ip.getAccession();
			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			boolean isTarget = !acc.startsWith(rev_symbol);
			if (peppepset.contains(seq) && proset.contains(acc)) {
				if (isTarget) {
					calPept++;
					try {
						pepCsvPrinter.printRecord(ip.getContentObject());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOGGER.error(taskName + ": error in writing peptide \"" + ip.getName() + "\" to "
								+ this.filtered_3_peps_file.getName(), e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing peptide \""
								+ ip.getName() + "\" to " + this.filtered_3_peps_file.getName());
					}
				} else {
					calPepd++;
				}
			}
		}
		try {
			pepCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing peptides to " + this.filtered_3_peps_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing peptides to "
					+ this.filtered_3_peps_file.getName());
		}

		double pepfdr = (double) calPepd / (double) calPept;

		this.summaryWriter.println("Final total peptide count: " + (calPept + calPepd));
		this.summaryWriter.println("Final target peptide count: " + calPept);
		this.summaryWriter.println("Final decoy peptide count: " + calPepd);
		this.summaryWriter.println("FDR: " + dfP2.format(pepfdr));
		this.summaryWriter.println("***");

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total peptide count: " + (calPept + calPepd));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target peptide count: " + calPept);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy peptide count: " + calPepd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(pepfdr));

		ArrayList<OpenPSM> filteredPsmList = new ArrayList<OpenPSM>();
		HashSet<Integer> filteredModSet = new HashSet<Integer>();

		HashMap<Integer, Integer> tempmap = new HashMap<Integer, Integer>();

		int calPsmt = 0;
		int calPsmd = 0;

		for (OpenPSM op : filtered_2_psms) {

			String pro = op.getProtein();
			String seq = op.getSequence();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (peppepset.contains(seq) && proset.contains(acc)) {

				if (op.isTarget()) {
					calPsmt++;

					filteredPsmList.add(op);
					filteredModSet.add(op.getOpenModId());

					if (filtered_1_mods[op.getOpenModId()].getMonoId() != -1) {
						if (tempmap.containsKey(op.getOpenModId())) {
							tempmap.put(op.getOpenModId(), tempmap.get(op.getOpenModId()) + 1);
						} else {
							tempmap.put(op.getOpenModId(), 1);
						}
					}
				} else {
					calPsmd++;
				}
			}
		}

		double psmfdr = (double) calPsmd / (double) calPsmt;

		this.summaryWriter.println("Final total PSM count: " + (calPsmt + calPsmd));
		this.summaryWriter.println("Final target PSM count: " + calPsmt);
		this.summaryWriter.println("Final decoy PSM count: " + calPsmd);
		this.summaryWriter.println("FDR: " + dfP2.format(psmfdr));

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total PSM count: " + (calPsmt + calPsmd));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target PSM count: " + calPsmt);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy PSM count: " + calPsmd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(psmfdr));

		int usedModCount = filteredModSet.size();

		this.summaryWriter.println("Final potential modification count: " + usedModCount);

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final potential modification count: " + usedModCount);

		OpenModXmlWriter modWriter = null;
		try {
			modWriter = new OpenModXmlWriter(this.filtered_3_mods_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in writing potential modifications to " + this.filtered_3_mods_file.getName(),
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing potential modifications to " + this.filtered_3_mods_file.getName());
		}

		try {
			modWriter.addMods(filtered_1_mods);
			modWriter.addSetMods(filtered_1_setModMap, filtered_1_mods[0]);
			modWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(
					taskName + ": error in writing potential modifications to " + this.filtered_3_mods_file.getName(),
					e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing potential modifications to " + this.filtered_3_mods_file.getName());
		}

		OpenPsmTsvWriter psmwriter = new OpenPsmTsvWriter(this.filtered_3_psms_file,
				OpenPsmTsvWriter.simple_information);

		for (OpenPSM op : filteredPsmList) {
			psmwriter.addPSM(op);
		}
		psmwriter.close();

		LOGGER.info(taskName + ": stage 3 finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": stage 3 finished");
	}

	private void filter_close() {

		HashMap<String, HashMap<Integer, Double>> rawRtMap = new HashMap<String, HashMap<Integer, Double>>();
		PrintWriter ms2CountWriter = null;
		try {
			ms2CountWriter = new PrintWriter(ms2count);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MS/MS count to " + ms2count.getName(), e);
		}
		ms2CountWriter.println("File\tMS2 count");

		for (File mgffile : mgfFiles) {
			HashMap<Integer, Double> rtmap = new HashMap<Integer, Double>();
			String fileName = mgffile.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));

			MgfReader mr = new MgfReader(mgffile);
			Spectrum sp = null;
			try {
				while ((sp = mr.getNextSpectrum()) != null) {
					rtmap.put(sp.getScannum(), sp.getRt());
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading spectra information from " + mgffile, e);
			}

			ms2CountWriter.println(fileName + "\t" + rtmap.size());
			rawRtMap.put(fileName, rtmap);
		}

		ms2CountWriter.close();

		if (filtered_2_psms_file.exists() && filtered_2_peps_file.exists() && filtered_2_pros_file.exists()) {

			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_psms_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_peps_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_pros_file.getName()
					+ " already exist");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": " + "go to next step");

		} else {

			MSFraggerReader psmReader = new MSFraggerReader(this.pepxmlFiles);
			MSFraggerPsm[] original_psms = psmReader.getPSMs();

			if (original_psms == null) {
				LOGGER.error("Error in parsing PSMs from MSFragger search results");
				return;
			}

			int original_target = 0;
			int original_decoy = 0;

			for (int i = 0; i < original_psms.length; i++) {
				if (original_psms[i].isTarget()) {
					original_target++;
				} else {
					original_decoy++;
				}
			}

			double original_fdr = (double) original_decoy / (double) original_target;
			this.summaryWriter.println("Total PSM count from open search: " + (original_target + original_decoy));
			this.summaryWriter.println("Target PSM count from open search: " + original_target);
			this.summaryWriter.println("Decoy PSM count from open search: " + original_decoy);
			this.summaryWriter.println("FDR: " + dfP2.format(original_fdr));
			this.summaryWriter.println("***");

			System.out.println(format.format(new Date()) + "\t" + taskName + ": Total PSM count from closed search: "
					+ (original_target + original_decoy));
			System.out.println(format.format(new Date()) + "\t" + taskName + ": Target PSM count from closed search: "
					+ original_target);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": Decoy PSM count from closed search: "
					+ original_decoy);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(original_fdr));

			filtered_2_psms = new OpenPSM[original_psms.length];
			for (int i = 0; i < original_psms.length; i++) {
				String fileName = original_psms[i].getFileName();
				int scan = original_psms[i].getScan();
				double rt = rawRtMap.get(fileName).get(scan);
				filtered_2_psms[i] = new OpenPSM(fileName, scan, original_psms[i].getCharge(),
						original_psms[i].getPrecursorMr(), rt, original_psms[i].getSequence(),
						original_psms[i].getPepMass(), original_psms[i].getMassDiff(), original_psms[i].getMiss(),
						original_psms[i].getProtein(), original_psms[i].getHyperscore(),
						original_psms[i].getNextscore(), original_psms[i].getExpect(), 0, new int[] {}, 100);
				filtered_2_psms[i].setPTMs(original_psms[i].getPTMs());
				filtered_2_psms[i].setTarget(original_psms[i].isTarget());
			}

			OpenPsmTsvWriter psmwriter = new OpenPsmTsvWriter(this.filtered_2_psms_file_backup,
					OpenPsmTsvWriter.full_information);

			OpenPIAXMLWriter writer = new OpenPIAXMLWriter(filtered_2_psms_file);
			writer.initialMSFragger(this.fasta, this.rawFiles);

			for (OpenPSM psm : filtered_2_psms) {
				psmwriter.addPSM(psm);
				writer.add(psm);
			}

			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in writing PSMs to " + this.filtered_2_psms_file.getName(), e);
			}

			psmwriter.close();

			String[] args = new String[] { "-infile", "\"" + filtered_2_psms_file.getAbsolutePath() + "\"",
					"-paramFile", "\"" + this.piaParameter.getAbsolutePath() + "\"", "-peptideExport",
					"\"" + this.filtered_2_peps_file.getAbsolutePath() + "\"", "csv", "-proteinExport",
					"\"" + this.filtered_2_pros_file.getAbsolutePath() + "\"", "csv" };

			PIAModeller.main(args);
		}

		if (filtered_2_psms == null) {
			OpenPsmTsvReader reader;
			try {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": Reading PSMs from "
						+ filtered_2_psms_file_backup.getName());

				reader = new OpenPsmTsvReader(this.filtered_2_psms_file_backup);
				filtered_2_psms = reader.getAllOpenPSMs();

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + filtered_2_psms.length
						+ " PSMs found in " + filtered_2_psms_file_backup.getName());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading PSMs from " + filtered_2_psms_file_backup.getName(), e);
			}
		}

		HashSet<String> psmpepset = new HashSet<String>();
		HashSet<String> psmproset = new HashSet<String>();

		for (int i = 0; i < filtered_2_psms.length; i++) {
			String pro = filtered_2_psms[i].getProtein();
			String seq = filtered_2_psms[i].getSequence();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			psmpepset.add(seq);
			psmproset.add(acc);
		}

//		InferedPeptide[] peptides = ProteinInferenceReader.getAllPeptides(filtered_2_peps_file);
		InferedPeptide[] peptides = null;
		try {
			peptides = ProteinInferenceReader.getAllPeptides1360(filtered_2_peps_file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Arrays.sort(peptides);

		HashSet<String> peppepset = new HashSet<String>();
		HashSet<String> pepproset = new HashSet<String>();

		int pept = 0;
		int pepd = 0;

		int breakId = -1;

		for (int i = 0; i < peptides.length; i++) {
			String pro = peptides[i].getAccession();
			String seq = peptides[i].getName();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (!psmpepset.contains(seq) || !psmproset.contains(acc)) {
				continue;
			}
			boolean isTarget = !pro.startsWith(rev_symbol);
			if (isTarget) {
				pept++;
			} else {
				pepd++;
			}
			double fdr = (double) pepd / (double) pept;
			if (fdr > this.pep_fdr) {
				if (breakId == -1) {
					breakId = i;
				}
			} else {
				if (breakId != -1) {
					breakId = -1;
				}
			}
		}

		if (breakId == -1) {
			breakId = peptides.length;
		}

		for (int i = 0; i < breakId; i++) {
			String pro = peptides[i].getAccession();
			String seq = peptides[i].getName();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (!psmpepset.contains(seq) || !psmproset.contains(acc)) {
				continue;
			}

			peppepset.add(seq);
			pepproset.add(acc);
		}

//		InferedProtein[] proteins = ProteinInferenceReader.getAllProteins(filtered_2_pros_file);
		InferedProtein[] proteins = null;
		try {
			proteins = ProteinInferenceReader.getAllProteins1360(filtered_2_pros_file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Arrays.sort(proteins);

		HashSet<String> proset = new HashSet<String>();

		int prot = 0;
		int prod = 0;

		breakId = -1;

		for (int i = 0; i < proteins.length; i++) {
			String pro = proteins[i].getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}
			if (!pepproset.contains(acc)) {
				continue;
			}
			boolean isTarget = !pro.startsWith(rev_symbol);
			if (isTarget) {
				prot++;
			} else {
				prod++;
			}
			double fdr = (double) prod / (double) prot;
			if (fdr > this.pro_fdr) {
				if (breakId == -1) {
					breakId = i;
				}
			} else {
				if (breakId != -1) {
					breakId = -1;
				}
			}
		}

		if (breakId == -1) {
			breakId = proteins.length;
		}

		for (int i = 0; i < breakId; i++) {
			String pro = proteins[i].getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}
			if (!pepproset.contains(acc)) {
				continue;
			}
			proset.add(acc);
		}

		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
		CSVPrinter proCsvPrinter = null;
		try {
			proCsvPrinter = new CSVPrinter(new FileWriter(this.filtered_3_pros_file), csvFileFormat);
			String[] protitle = ProteinInferenceReader.getProteinTitle();
			proCsvPrinter.printRecord(protitle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in creating csv writer in " + this.filtered_3_pros_file.getName(), e);
		}

		int calProt = 0;
		int calProd = 0;
		for (InferedProtein ip : proteins) {
			String pro = ip.getName();
			String[] cs = pro.split("\\s+");

			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (proset.contains(acc)) {
				boolean isTarget = !pro.startsWith(rev_symbol);
				if (isTarget) {
					calProt++;
					try {
						proCsvPrinter.printRecord(ip.getContentObject());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing protein \"" + ip.getName() + "\" to "
								+ this.filtered_3_pros_file.getName(), e);
					}
				} else {
					calProd++;
				}
			}
		}
		try {
			proCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing proteins to " + this.filtered_3_pros_file.getName(), e);
		}

		double profdr = (double) calProd / (double) calProt;
		this.summaryWriter.println("Final total protein count: " + (calProd + calProt));
		this.summaryWriter.println("Final target protein count: " + calProt);
		this.summaryWriter.println("Final decoy protein count: " + calProd);
		this.summaryWriter.println("FDR: " + dfP2.format(profdr));
		this.summaryWriter.println("***");

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total protein count: " + (calProd + calProt));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target protein count: " + calProt);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy protein count: " + calProd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(profdr));

		CSVPrinter pepCsvPrinter = null;
		try {
			pepCsvPrinter = new CSVPrinter(new FileWriter(this.filtered_3_peps_file), csvFileFormat);
			pepCsvPrinter.printRecord(ProteinInferenceReader.getPeptideTitle());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in creating csv writer in " + this.filtered_3_peps_file.getName(), e);
		}

		int calPept = 0;
		int calPepd = 0;
		for (InferedPeptide ip : peptides) {
			String seq = ip.getName();
			String pro = ip.getAccession();
			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			boolean isTarget = !acc.startsWith(rev_symbol);
			if (peppepset.contains(seq) && proset.contains(acc)) {
				if (isTarget) {
					calPept++;
					try {
						pepCsvPrinter.printRecord(ip.getContentObject());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing peptide \"" + ip.getName() + "\" to "
								+ this.filtered_3_peps_file.getName(), e);
					}
				} else {
					calPepd++;
				}
			}
		}
		try {
			pepCsvPrinter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptides to " + this.filtered_3_peps_file.getName(), e);
		}

		double pepfdr = (double) calPepd / (double) calPept;

		this.summaryWriter.println("Final total peptide count: " + (calPept + calPepd));
		this.summaryWriter.println("Final target peptide count: " + calPept);
		this.summaryWriter.println("Final decoy peptide count: " + calPepd);
		this.summaryWriter.println("FDR: " + dfP2.format(pepfdr));
		this.summaryWriter.println("***");

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total peptide count: " + (calPept + calPepd));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target peptide count: " + calPept);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy peptide count: " + calPepd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(pepfdr));

		ArrayList<OpenPSM> filteredPsmList = new ArrayList<OpenPSM>();

		int calPsmt = 0;
		int calPsmd = 0;

		for (OpenPSM op : filtered_2_psms) {

			String pro = op.getProtein();
			String seq = op.getSequence();

			String[] cs = pro.split("\\s+");
			String acc;
			if (cs.length > 1) {
				acc = cs[0];
			} else {
				acc = pro;
			}

			if (peppepset.contains(seq) && proset.contains(acc)) {

				if (op.isTarget()) {
					calPsmt++;
					filteredPsmList.add(op);

				} else {
					calPsmd++;
				}
			}
		}

		double psmfdr = (double) calPsmd / (double) calPsmt;

		this.summaryWriter.println("Final total PSM count: " + (calPsmt + calPsmd));
		this.summaryWriter.println("Final target PSM count: " + calPsmt);
		this.summaryWriter.println("Final decoy PSM count: " + calPsmd);
		this.summaryWriter.println("FDR: " + dfP2.format(psmfdr));

		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": Final total PSM count: " + (calPsmt + calPsmd));
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final target PSM count: " + calPsmt);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": Final decoy PSM count: " + calPsmd);
		System.out.println(format.format(new Date()) + "\t" + taskName + ": FDR: " + dfP2.format(psmfdr));

		OpenPsmTsvWriter psmwriter = new OpenPsmTsvWriter(this.filtered_3_psms_file,
				OpenPsmTsvWriter.simple_information);

		for (OpenPSM op : filteredPsmList) {
			psmwriter.addPSM(op);
		}
		psmwriter.close();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		File openSearchFile = metaPar.getDbSearchResultFile();
		File pepxml_dir = new File(openSearchFile, "pepxml");

		String[] raws = metaPar.getMetadata().getRawFiles();
		this.rawFiles = new File[raws.length];
		for (int i = 0; i < this.rawFiles.length; i++) {
			this.rawFiles[i] = new File(raws[i]);
		}

		this.mgfFiles = metaPar.getSpectraFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith("mgf") || name.endsWith("MGF"))
					return true;
				return false;
			}
		});

		File[] pepxmls = pepxml_dir.listFiles();
		ArrayList<File> templist = new ArrayList<File>();
		for (int i = 0; i < pepxmls.length; i++) {
			if (pepxmls[i].getName().endsWith("pepXML")) {
				templist.add(pepxmls[i]);
			}
		}

		this.pepxmlFiles = templist.toArray(new File[templist.size()]);
		this.fasta = new File(metaPar.getCurrentDb());
		this.result = new File(openSearchFile, "result");

		if (!result.exists()) {
			result.mkdir();
		}

		this.peaks = new File(this.result, "peaks.tsv");
		this.bins = new File(this.result, "bins.tsv");
		this.ms2count = new File(this.result, "ms2Count.tsv");
		this.filtered_1_psms_file = new File(this.result, "filter1_psms.tsv");
		this.filtered_1_mods_file = new File(this.result, "filter1_mods.xml");

		this.filtered_2_psms_file = new File(this.result, "filter2_psms.xml");
		this.filtered_2_psms_file_backup = new File(this.result, "filter2_psms.tsv");
		this.filtered_2_peps_file = new File(this.result, "filter2_peps.csv");
		this.filtered_2_pros_file = new File(this.result, "filter2_pros.csv");

		this.filtered_3_psms_file = new File(this.result, "filter3_psms.tsv");
		this.filtered_3_mods_file = new File(this.result, "filter3_mods.xml");
		this.filtered_3_peps_file = new File(this.result, "filter3_peps.csv");
		this.filtered_3_pros_file = new File(this.result, "filter3_pros.csv");
		this.piaParameter = new File(this.result, "pia.parameter.xml");

		byte[] buffer = new byte[1024];
		int readBytes;

		URL fromUrl = getClass().getResource("/bmi/med/uOttawa/metalab/dbSearch/pia/parameter.xml");
		URLConnection urlConnection = fromUrl.openConnection();
		InputStream is = urlConnection.getInputStream();

		OutputStream os = new FileOutputStream(piaParameter);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing PIA parameter file to " + piaParameter, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing PIA parameter file to "
					+ piaParameter);

		} finally {
			os.close();
			is.close();
		}

		File summary = new File(this.result, "summary_" + format.format(new Date()) + ".txt");
		try {
			this.summaryWriter = new PrintWriter(summary);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in creating summary file in " + summary, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in creating summary file in " + summary);
		}

		if (metaPar.isOpenSearch()) {

			setProgress(52);

			this.filtering_1();

			setProgress(55);

			this.filtering_2();

			setProgress(58);

			this.filtering_3();

		} else {

			this.filter_close();

		}

		setProgress(60);

		return filtered_3_psms_file.exists();
	}

	public void done() {

		super.done();
		this.summaryWriter.close();
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
