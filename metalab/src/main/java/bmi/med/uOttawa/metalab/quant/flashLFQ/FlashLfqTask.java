package bmi.med.uOttawa.metalab.quant.flashLFQ;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.par.MetaData;

/**
 * Before using FlashLFQ, please make sure to modify the file
 * LicenceAgreements.toml with HasAcceptedThermoLicence = true
 * 
 * @author Kai Cheng
 *
 */
public class FlashLfqTask {

	private String flashLFQ;
	private MetaData metadata;
	private String idt;
	private String rep;
	private boolean mbr;
	private boolean nor;
	private int thr;

	protected Logger LOGGER = LogManager.getLogger(FlashLfqTask.class);
	protected String taskName = "Label free quantification";
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static final String DOTNET_STRING = "https://dotnet.microsoft.com/en-us/download/dotnet/8.0/runtime?cid=getdotnetcore&os=windows&arch=x64";

	public FlashLfqTask(String flashLFQ, int thr) {
		this.flashLFQ = flashLFQ;
		this.thr = thr;
		this.thermoLicence();
	}

	private void thermoLicence() {
		File flashLFQFile = new File(flashLFQ);
		File rawLicenceFile = new File(flashLFQFile.getParent(), "LicenceAgreements.toml");
		try (PrintWriter writer = new PrintWriter(rawLicenceFile)) {
			writer.println("HasAcceptedThermoLicence = true");
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in creating the license file", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in creating the license file");
		}
	}

	public boolean run(MetaData metadata, String idt, String rep, boolean mbr, boolean nor) {

		this.metadata = metadata;
		this.idt = idt;
		this.rep = rep;
		this.mbr = mbr;
		this.nor = nor;

		if (!this.exportExpDesign()) {
			return false;
		}

		if (!this.flashLFQ()) {
			return false;
		}

		return true;
	}

	/**
	 * FlashLFQ CMD version has a bug that if the raw file folder contains other raw
	 * files which are not shown in the ExperimentalDesign.tsv, an Exception will
	 * thrown
	 * 
	 * @param rawDir
	 * @throws FileNotFoundException
	 */
	private boolean exportExpDesign() {

		File output = new File(this.rep, "ExperimentalDesign.tsv");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing experimental design file", e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing experimental design file");
			return false;
		}
		StringBuilder title = new StringBuilder();
		title.append("FileName").append("\t");
		title.append("Condition").append("\t");
		title.append("Biorep").append("\t");
		title.append("Fraction").append("\t");
		title.append("Techrep");

		writer.println(title);

		String[] raws = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		int[] fractions = metadata.getFractions();
		int[] replicates = metadata.getReplicates();

		HashSet<String> rawSet = new HashSet<String>();
		for (int i = 0; i < raws.length; i++) {
			StringBuilder sb = new StringBuilder();
			String name = raws[i].substring(raws[i].lastIndexOf("\\") + 1, raws[i].lastIndexOf("."));
			sb.append(name).append("\t");
			sb.append(expNames[i]).append("\t");

			// biorep
			sb.append("1").append("\t");

			sb.append(fractions[i]).append("\t");
			sb.append(replicates[i]);

			writer.println(sb);

			rawSet.add(raws[i].substring(raws[i].lastIndexOf("\\") + 1));
		}

		writer.close();

		File rawDir = new File(this.rep);
		File[] listFiles = rawDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub

				if (name.endsWith(".raw") || name.endsWith(".RAW") || name.endsWith(".mzML") || name.endsWith(".MZML")
						|| name.endsWith(".mzml"))
					return true;

				return false;
			}
		});

		if (listFiles.length != raws.length) {
			LOGGER.info(taskName + ": the number of the raw files in " + rawDir
					+ " didn't exactly matched to the expected raw file number in this task, " + raws.length
					+ " raw files were added in this task but " + listFiles.length + " raw files in this folder "
					+ rawDir);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": the number of the raw files in "
					+ rawDir + " didn't exactly matched to the expected raw file number in this task, " + raws.length
					+ " raw files were added in this task but " + listFiles.length + " raw files in this folder "
					+ rawDir);

			return false;
		}

		for (int i = 0; i < listFiles.length; i++) {
			if (!rawSet.contains(listFiles[i].getName())) {
				LOGGER.info(taskName + ": this raw file " + listFiles[i].getName()
						+ " was not in this task, please move it out of this folder");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": this raw file "
						+ listFiles[i].getName() + " was not in this task, please move it out of this folder");

				return false;
			}
		}

		LOGGER.info(taskName + ": the experimental design has been exported to " + output);
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": the experimental design has been exported to " + output);

		return true;
	}

	private boolean flashLFQ() {
		File outFile = (new File(idt)).getParentFile();

		File batFile = new File(outFile, "LFQ.bat");

		try {

			String cmd = flashLFQ + " --idt " + "\"" + idt + "\"" + " --rep " + "\"" + rep + "\"" + " --out " + "\""
					+ outFile.getAbsolutePath() + "\"" + " --mbr " + mbr + " --nor " + nor + " --thr " + thr;

			File flashLFQFile = new File(flashLFQ);
			int id = flashLFQ.indexOf(":");
			if (id < 0) {

				id = flashLFQFile.getAbsolutePath().indexOf(":");

				if (id < 0) {
					LOGGER.error(taskName + ": cann't find the directory of FlashLFQ ");
					return false;
				}
			}

			PrintWriter writer = new PrintWriter(batFile);
			writer.println(flashLFQ.substring(0, id) + ":");
			writer.println("cd " + flashLFQFile.getParent());
			writer.println("CMD.exe --idt " + "\"" + idt + "\"" + " --rep " + "\"" + rep + "\"" + " --out " + "\""
					+ outFile.getAbsolutePath() + "\"" + " --mbr " + mbr + " --nor " + nor + " --thr " + thr);
			writer.print("exit");
			writer.close();

			LOGGER.info(cmd);

		} catch (FileNotFoundException e) { // TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the command file " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the command file " + batFile);
		}

		try {

			ArrayList<String> list = new ArrayList<String>();
			list.add("cmd.exe");
			list.add("/c");
			list.add("call");

			list.add(flashLFQ);
			list.add("--idt");
			list.add("\"" + idt + "\"");
			list.add("--rep");
			list.add("\"" + rep + "\"");
			list.add("--out");
			list.add("\"" + outFile.getAbsolutePath() + "\"");
			list.add("--mbr");
			list.add(String.valueOf(mbr));
			list.add("--nor");
			list.add(String.valueOf(nor));
			list.add("--thr");
			list.add(String.valueOf(thr));

			String[] args = list.toArray(new String[list.size()]);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < args.length; i++) {
				sb.append(args[i]).append(" ");
			}

			System.out.println(format.format(new Date()) + "\t" + sb);
			LOGGER.info(taskName + ": " + sb);

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				LOGGER.info(lineStr);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.info(taskName
							+ ": try to fix this problem by install the dotNET framework, please download from "
							+ FlashLfqTask.DOTNET_STRING);
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": try to fix this problem by install the dotNET framework, please download from "
							+ FlashLfqTask.DOTNET_STRING);
				}
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in quantifying PSMs from " + idt, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in quantifying PSMs from " + idt);

			return false;
		}

		LOGGER.info(taskName + ": quantifying PSMs from " + idt + " finished");
		System.out
				.println(format.format(new Date()) + "\t" + taskName + ": quantifying PSMs from " + idt + " finished");

		return true;
	}

	protected static void exportExpDesign(File repFile) throws FileNotFoundException {

		File output = new File(repFile, "ExperimentalDesign.tsv");
		PrintWriter writer = new PrintWriter(output);

		StringBuilder title = new StringBuilder();
		title.append("FileName").append("\t");
		title.append("Condition").append("\t");
		title.append("Biorep").append("\t");
		title.append("Fraction").append("\t");
		title.append("Techrep");

		writer.println(title);

		HashSet<String> rawSet = new HashSet<String>();

		File[] rawFiles = repFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("raw"))
					return true;
				return false;
			}
		});
		for (int i = 0; i < rawFiles.length; i++) {
			StringBuilder sb = new StringBuilder();
			String name = rawFiles[i].getName().substring(0, rawFiles[i].getName().lastIndexOf("."));

			sb.append(name).append("\t");
			sb.append(name).append("\t");

			// biorep
			sb.append("1").append("\t");

			sb.append("1").append("\t");
			sb.append("1");

			writer.println(sb);

			rawSet.add(name + ".raw");
		}

		writer.close();
	}

}
