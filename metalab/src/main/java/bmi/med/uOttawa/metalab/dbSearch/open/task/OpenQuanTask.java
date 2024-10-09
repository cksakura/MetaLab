/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class OpenQuanTask extends MetaAbstractTask {

	private String taskName = "Open search quantification";
	private Logger LOGGER = LogManager.getLogger(OpenQuanTask.class);

	public OpenQuanTask(MetaParameterMQ metaPar, MetaSourcesV2 advPar, JProgressBar bar1, JProgressBar bar2,
			OpenExportTask exportTask) {
		super(metaPar, advPar, bar1, bar2, exportTask);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		String flashLFQ = ((MetaSourcesV2) msv).getFlashlfq();

		File resultDir = new File(metaPar.getDbSearchResultFile(), "result");
		File msms = new File(resultDir, "filter3_psms.tsv");

		File quanPeak = new File(resultDir, "QuantifiedPeaks.tsv");
		File quanPep = new File(resultDir, "QuantifiedPeptides.tsv");
		File quanPro = new File(resultDir, "QuantifiedProteins.tsv");

		if (quanPep.exists() && quanPeak.exists() && quanPro.exists() && quanPep.length() > 0 && quanPeak.length() > 0
				&& quanPro.length() > 0) {

			LOGGER.info(taskName + ": quantitative analysis results already exist in " + resultDir);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": quantitative analysis results already exist in " + resultDir);

			return true;
		}

		if (msms == null || !msms.exists() || msms.length() == 0) {

			LOGGER.error(taskName + ": peptide identification result was not found in " + msms);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": peptide identification result was not found in " + msms);

			return false;
		}

		File rawDir = (new File(metaPar.getMetadata().getRawFiles()[0])).getParentFile();
		exportExpDesign(rawDir);

		String cmd;
		if (((MetaParameterMQ) metaPar).getOsp().isMatchBetweenRuns()) {
			cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"" + " --mbr true";
		} else {
			cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"" + " --mbr false";
		}

		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				LOGGER.info(lineStr);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in quantifying PSMs from " + msms, e);
			System.err
					.println(format.format(new Date()) + "\t" + taskName + ": error in quantifying PSMs from " + msms);
		}

		if (!quanPep.exists() || quanPep.length() == 0) {
			LOGGER.error(taskName + ": quantitative result was not found in " + quanPep);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": quantitative result was not found in " + quanPep);
			return false;
		}

		if (!quanPeak.exists() || quanPeak.length() == 0) {
			LOGGER.error(taskName + ": quantitative result was not found in " + quanPeak);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": quantitative result was not found in " + quanPeak);
			return false;
		}

		if (!quanPro.exists() || quanPro.length() == 0) {
			LOGGER.error(taskName + ": quantitative result was not found in " + quanPro);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": quantitative result was not found in " + quanPro);
			return false;
		}

		return true;
	}

	private void exportExpDesign(File rawDir) throws FileNotFoundException {

		File output = new File(rawDir, "ExperimentalDesign.tsv");
		PrintWriter writer = new PrintWriter(output);
		StringBuilder title = new StringBuilder();
		title.append("FileName").append("\t");
		title.append("Condition").append("\t");
		title.append("Biorep").append("\t");
		title.append("Fraction").append("\t");
		title.append("Techrep");

		writer.println(title);

		MetaData metadata = this.metaPar.getMetadata();
		String[] raws = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < raws.length; i++) {
			if (map.containsKey(raws[i])) {
				map.put(raws[i], map.get(raws[i]) + 1);
			} else {
				map.put(raws[i], 1);
			}
		}

		for (int i = 0; i < raws.length; i++) {
			StringBuilder sb = new StringBuilder();
			String name = raws[i].substring(raws[i].lastIndexOf("\\") + 1, raws[i].lastIndexOf("."));
			sb.append(name).append("\t");
			sb.append(expNames[i]).append("\t");
			sb.append(map.get(raws[i])).append("\t");
			sb.append("1").append("\t");
			sb.append("1");

			writer.println(sb);
		}

		writer.close();
	}

	/**
	 * Old version of FlashLFQ
	 * 
	 * @return
	 * @throws Exception
	 */
	protected Boolean doInBackground099() throws Exception {
		// TODO Auto-generated method stub

		bar1.setIndeterminate(true);
		bar2.setString(taskName);

		LOGGER.info(taskName + ": started");

		String flashLFQ = ((MetaSourcesV2) msv).getFlashlfq();

		File resultDir = new File(metaPar.getDbSearchResultFile(), "result");
		File msms = new File(resultDir, "filter3_psms.tsv");

		File quanBaseSeq = new File(resultDir, "filter3_psms_FlashLFQ_QuantifiedBaseSequences.tsv");
		File quanModSeq = new File(resultDir, "filter3_psms_FlashLFQ_QuantifiedModifiedSequences.tsv");
		File quanPeak = new File(resultDir, "filter3_psms_FlashLFQ_QuantifiedPeaks.tsv");
		File quanPro = new File(resultDir, "filter3_psms_FlashLFQ_QuantifiedProteins.tsv");

		if (quanBaseSeq.exists() && quanModSeq.exists() && quanPeak.exists() && quanPro.exists()) {
			return true;
		}

		if (msms == null || !msms.exists()) {
			return false;
		}

		File rawDir = (new File(metaPar.getMetadata().getRawFiles()[0])).getParentFile();
		String cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"";
		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				LOGGER.info(lineStr);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in quantifying PSMs from " + msms, e);
		}

		if (!quanBaseSeq.exists()) {
			return false;
		}

		if (!quanModSeq.exists()) {
			return false;
		}

		if (!quanPeak.exists()) {
			return false;
		}

		if (!quanPro.exists()) {
			return false;
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
