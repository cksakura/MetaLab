/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.function.COGFinder;
import bmi.med.uOttawa.metalab.core.function.CategoryFinder;
import bmi.med.uOttawa.metalab.core.function.FunctionFinder;
import bmi.med.uOttawa.metalab.core.function.GOBPFinder;
import bmi.med.uOttawa.metalab.core.function.GOCCFinder;
import bmi.med.uOttawa.metalab.core.function.GOMFFinder;
import bmi.med.uOttawa.metalab.core.function.KEGGFinder;
import bmi.med.uOttawa.metalab.core.function.NOGFinder;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantProReader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinXMLReader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinXMLWriter;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 * @deprecated
 */
public class MetaFunctionTask extends SwingWorker<File, Void> {

	private MetaParameterV1 parameter;
	private File logFile;
	private PrintWriter logWriter;

	private int[] progress;
	private JProgressBar progressBar1;
	private JProgressBar progressBar2;

	private boolean finished;

	protected static String taskName = "Functional analysis";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static Logger LOGGER = LogManager.getLogger(MetaFunctionTask.class);

	public MetaFunctionTask(MetaParameterV1 parameter) {
		this.parameter = parameter;
	}

	public MetaFunctionTask(MetaParameterV1 parameter, int[] progress, JProgressBar progressBar1,
			JProgressBar progressBar2) {
		this.parameter = parameter;
		this.progress = progress;
		this.progressBar1 = progressBar1;
		this.progressBar2 = progressBar2;
	}

	@Override
	protected File doInBackground() {
		// TODO Auto-generated method stub
		this.logFile = new File(parameter.getParameterDir(), "function.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logFile, e);
		}

		progressBar1.setIndeterminate(true);
		progressBar2.setString(taskName + " processing...");

		logWriter.println(format.format(new Date()) + "\t" + taskName + " started");
		LOGGER.info(taskName + " started");

		if (parameter.getFunctionSampleId() == 0) {
			logWriter.println(format.format(new Date()) + "\t" + "Unknown sample type");
			LOGGER.info("Unknown sample type");
			logWriter.close();
			progressBar1.setString("");
			progressBar2.setValue(progress[1]);
			progressBar1.setIndeterminate(false);
			progressBar2.setString(taskName + " finished");
			finished = true;
			return logFile;
		}

		File proIdenResult = parameter.getProteinIdenResult();
		if (proIdenResult == null || !proIdenResult.exists()) {
			logWriter.println(format.format(new Date()) + "\t" + "Cann't find protein identification result.");
			LOGGER.info("Cann't find protein identification result.");
			logWriter.close();
			progressBar1.setString("");
			progressBar2.setValue(progress[1]);
			progressBar1.setIndeterminate(false);
			progressBar2.setString(taskName + " finished");
			finished = true;
			return logFile;
		}

		File funcDir = new File(parameter.getResultDir(), "function");
		if (!funcDir.exists()) {
			funcDir.mkdir();
		}

		File functionFile = new File(funcDir, "function.xml");

		if (functionFile.exists()) {
			LOGGER.info("functional annotation result already existed in " + functionFile + ", skip");
			logWriter.println(format.format(new Date()) + "\t" + "functional annotation result already existed in "
					+ functionFile + ", skip");
		} else {
			try {

				MaxquantProReader reader = new MaxquantProReader(proIdenResult, parameter);
				if (proIdenResult.getAbsolutePath().equals(parameter.getRawFiles()[0][0])) {
					reader = new MaxquantProReader(proIdenResult);
				} else {
					reader = new MaxquantProReader(proIdenResult, parameter);
				}
				MetaProtein[] metapros = reader.getMetaProteins();

				FunctionFinder[] ffs = getFunctionFinders(parameter);
				String[] funNames = new String[ffs.length];
				for (int i = 0; i < ffs.length; i++) {
					funNames[i] = ffs[i].getAbbreviation();
				}
				HashMap<String, String>[] maps = this.analysis(ffs, metapros);

				MetaProteinXMLWriter writer = new MetaProteinXMLWriter(functionFile, MetaConstants.maxQuant,
						reader.getQuanMode(), reader.getIntensityTitle());
//				writer.addProteins(metapros, maps, funNames, new CategoryFinder(parameter));
				writer.close();

				MetaProteinXMLReader funReader = new MetaProteinXMLReader(functionFile);
				if (metapros.length > 30000) {
					File output = new File(funcDir, "function.csv");
					funReader.exportCsv(output);
				} else {
					File output = new File(funcDir, "function.xlsx");
					funReader.export(output);
				}

			} catch (IOException | DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in analyzing protein function", e);
			}
		}

		progressBar1.setString("");
		progressBar2.setValue(progress[1]);
		progressBar1.setIndeterminate(false);
		progressBar2.setString(taskName + " finished");

		LOGGER.info(taskName + " finished");

		logWriter.println(format.format(new Date()) + "\t" + taskName + " finished");
		logWriter.close();

		finished = true;

		return logFile;
	}

	public File runCLI() {

		this.logFile = new File(parameter.getParameterDir(), "function.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		logWriter.println(format.format(new Date()) + "\t" + taskName + " started");
		LOGGER.info(taskName + " started");

		if (parameter.getFunctionSampleId() == 0) {
			LOGGER.info("Unknown sample type");
			logWriter.println(format.format(new Date()) + "\t" + "Unknown sample type");
			logWriter.close();
			finished = true;
			return logFile;
		}

		File proIdenResult = parameter.getProteinIdenResult();
		if (proIdenResult == null || !proIdenResult.exists()) {

			String[] fileNames = parameter.getRawFiles()[0];
			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].endsWith("txt")) {

					File funcDir = new File(parameter.getResultDir(), "function");
					if (!funcDir.exists()) {
						funcDir.mkdir();
					}

					File functionFile = new File(funcDir,
							fileNames[i].substring(0, fileNames[i].lastIndexOf(".")) + "function.xml");
					if (functionFile.exists()) {
						LOGGER.info("functional annotation result already existed in " + functionFile + ", skip");
						logWriter.println(format.format(new Date()) + "\t"
								+ "functional annotation result already existed in " + functionFile + ", skip");
					} else {
						try {
							MaxquantProReader reader = new MaxquantProReader(proIdenResult, parameter);
							MetaProtein[] metapros = reader.getMetaProteins();

							FunctionFinder[] ffs = getFunctionFinders(parameter);
							String[] funNames = new String[ffs.length];
							for (int j = 0; j < ffs.length; j++) {
								funNames[j] = ffs[j].getAbbreviation();
							}
							HashMap<String, String>[] maps = this.analysis(ffs, metapros);

							MetaProteinXMLWriter writer = new MetaProteinXMLWriter(functionFile, MetaConstants.maxQuant,
									reader.getQuanMode(), reader.getIntensityTitle());
//							writer.addProteins(metapros, maps, funNames, new CategoryFinder(parameter));
							writer.close();

							MetaProteinXMLReader funReader = new MetaProteinXMLReader(functionFile);
							if (metapros.length > 30000) {
								File output = new File(funcDir,
										fileNames[i].substring(0, fileNames[i].lastIndexOf(".")) + "function.csv");
								funReader.exportCsv(output);
							} else {
								File output = new File(funcDir,
										fileNames[i].substring(0, fileNames[i].lastIndexOf(".")) + "function.xlsx");
								funReader.export(output);
							}

						} catch (IOException | DocumentException e) {
							// TODO Auto-generated catch block
							LOGGER.error("Error in analyzing protein function", e);
						}
					}
				}
			}

		} else {

			File funcDir = new File(parameter.getResultDir(), "function");
			if (!funcDir.exists()) {
				funcDir.mkdir();
			}

			File functionFile = new File(funcDir, "function.xml");

			if (functionFile.exists()) {
				LOGGER.info("functional annotation result already existed in " + functionFile + ", skip");
				logWriter.println(format.format(new Date()) + "\t" + "functional annotation result already existed in "
						+ functionFile + ", skip");
			} else {
				try {
					MaxquantProReader reader = new MaxquantProReader(proIdenResult, parameter);
					MetaProtein[] metapros = reader.getMetaProteins();

					FunctionFinder[] ffs = getFunctionFinders(parameter);
					String[] funNames = new String[ffs.length];
					for (int i = 0; i < ffs.length; i++) {
						funNames[i] = ffs[i].getAbbreviation();
					}
					HashMap<String, String>[] maps = this.analysis(ffs, metapros);

					MetaProteinXMLWriter writer = new MetaProteinXMLWriter(functionFile, MetaConstants.maxQuant,
							reader.getQuanMode(), reader.getIntensityTitle());
//					writer.addProteins(metapros, maps, funNames, new CategoryFinder(parameter));
					writer.close();

					MetaProteinXMLReader funReader = new MetaProteinXMLReader(functionFile);

					if (metapros.length > 30000) {
						File output = new File(funcDir, "function.csv");
						funReader.exportCsv(output);
					} else {
						File output = new File(funcDir, "function.xlsx");
						funReader.export(output);
					}

				} catch (IOException | DocumentException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in analyzing protein function", e);
				}
			}
		}

		LOGGER.info(taskName + " finished");

		logWriter.println(format.format(new Date()) + "\t" + taskName + " finished");
		logWriter.close();

		finished = true;

		return logFile;
	}

	/**
	 * 
	 * @throws IOException
	 */
	private FunctionFinder[] getFunctionFinders(MetaParameterV1 parameter) throws IOException {
		ArrayList<FunctionFinder> list = new ArrayList<FunctionFinder>();
		COGFinder cogfinder = new COGFinder(parameter);
		if (cogfinder.isUsable()) {
			list.add(cogfinder);
		}

		NOGFinder nogfinder = new NOGFinder(parameter);
		if (nogfinder.isUsable()) {
			list.add(nogfinder);
		}

		KEGGFinder keggfinder = new KEGGFinder(parameter);
		if (keggfinder.isUsable()) {
			list.add(keggfinder);
		}

		GOBPFinder gobpfinder = new GOBPFinder(parameter);
		if (gobpfinder.isUsable()) {
			list.add(gobpfinder);
		}

		GOCCFinder goccfinder = new GOCCFinder(parameter);
		if (goccfinder.isUsable()) {
			list.add(goccfinder);
		}

		GOMFFinder gomffinder = new GOMFFinder(parameter);
		if (gomffinder.isUsable()) {
			list.add(gomffinder);
		}
		FunctionFinder[] ffs = list.toArray(new FunctionFinder[list.size()]);

		return ffs;
	}

	private HashMap<String, String>[] analysis(FunctionFinder[] ffs, MetaProtein[] metapros) {
		HashMap<String, String>[] maps = new HashMap[ffs.length];
		for (int i = 0; i < ffs.length; i++) {
			LOGGER.info("Functional analysis: " + ffs[i].getFullName() + " analysis start...");
//			ffs[i].match(metapros);
			maps[i] = ffs[i].getFunctionMap();
			LOGGER.info("Functional analysis: " + ffs[i].getFullName() + " analysis finish...");
		}

		return maps;
	}

	public boolean isFinished() {
		return finished;
	}
	/*
	 * private static void testTxt(String proIdenResult, String out, int sampleID)
	 * throws IOException, DocumentException {
	 * 
	 * BufferedReader reader = new BufferedReader(new FileReader(proIdenResult));
	 * String line = reader.readLine(); String[] title = line.split(","); String[]
	 * fileNames = new String[title.length - 2]; for (int i = 0; i <
	 * fileNames.length; i++) { fileNames[i] = title[i + 2].substring(1, title[i +
	 * 2].length() - 1); }
	 * 
	 * ArrayList<MetaProtein> list = new ArrayList<MetaProtein>(); int groupId = 1;
	 * while ((line = reader.readLine()) != null) { String[] cs0 =
	 * line.split("\","); if (cs0.length != 3) { continue; } String[] cs2 =
	 * cs0[2].split(","); double[] intensity = new double[cs2.length]; for (int i =
	 * 0; i < intensity.length; i++) { intensity[i] = Double.parseDouble(cs2[i]); }
	 * 
	 * String[] pros = cs0[1].substring(1).split(";"); for (int i = 0; i <
	 * pros.length; i++) { String name = pros[i].substring(0, pros[i].indexOf("["));
	 * MetaProtein mp = new MetaProtein(name, 0, 0, 0, intensity);
	 * mp.setGroupId(groupId); list.add(mp); } groupId++; } reader.close();
	 * 
	 * MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
	 * System.out.println("protein number: "+metapros.length); FunctionFinder[] ffs
	 * = new FunctionFinder[] { new COGFinder(sampleID), new NOGFinder(sampleID),
	 * new KEGGFinder(sampleID), new GOBPFinder(sampleID), new GOCCFinder(sampleID),
	 * new GOMFFinder(sampleID) };
	 * 
	 * String[] funNames = new String[ffs.length]; for (int i = 0; i < ffs.length;
	 * i++) { funNames[i] = ffs[i].getAbbreviation(); } HashMap<String, String>[]
	 * maps = new HashMap[ffs.length]; for (int i = 0; i < ffs.length; i++) {
	 * LOGGER.info("Functional analysis: " + ffs[i].getFullName() +
	 * " analysis start..."); ffs[i].match(metapros); maps[i] =
	 * ffs[i].getFunctionMap(); LOGGER.info("Functional analysis: " +
	 * ffs[i].getFullName() + " analysis finish..."); }
	 * 
	 * MetaProteinXMLWriter writer = new MetaProteinXMLWriter(out, 2, 0, fileNames);
	 * writer.addProteins(metapros, maps, funNames); writer.close();
	 * 
	 * MetaProteinXMLReader funReader = new MetaProteinXMLReader(out); File output =
	 * new File(out + ".xlsx"); funReader.export(output); }
	 * 
	 * private static void testMaxQuant(String proIdenResult, String out, String
	 * parameterFile) throws IOException, DocumentException { MetaProParameter
	 * parameter = JsonParaHandler.parse(parameterFile);
	 * parameter.setQuanResultType(1);
	 * 
	 * int sampleID = parameter.getFunctionSampleId(); MaxquantProReader reader =
	 * new MaxquantProReader(proIdenResult, parameter); MetaProtein[] metapros =
	 * reader.getMetaProteins();
	 * 
	 * FunctionFinder[] ffs = new FunctionFinder[] { new COGFinder(sampleID), new
	 * NOGFinder(sampleID), new KEGGFinder(sampleID), new GOBPFinder(sampleID), new
	 * GOCCFinder(sampleID), new GOMFFinder(sampleID) };
	 * 
	 * String[] funNames = new String[ffs.length]; for (int i = 0; i < ffs.length;
	 * i++) { funNames[i] = ffs[i].getAbbreviation(); } HashMap<String, String>[]
	 * maps = new HashMap[ffs.length]; for (int i = 0; i < ffs.length; i++) {
	 * LOGGER.info("Functional analysis: " + ffs[i].getFullName() +
	 * " analysis start..."); ffs[i].match(metapros); maps[i] =
	 * ffs[i].getFunctionMap(); LOGGER.info("Functional analysis: " +
	 * ffs[i].getFullName() + " analysis finish..."); }
	 * 
	 * MetaProteinXMLWriter writer = new MetaProteinXMLWriter(out, 0, 0,
	 * reader.getIntensityTitles()); writer.addProteins(metapros, maps, funNames,
	 * parameter); writer.close();
	 * 
	 * MetaProteinXMLReader funReader = new MetaProteinXMLReader(out); File output =
	 * new File(out + ".xlsx"); funReader.export(output); }
	 */

	private static void transfer(String in, String out) throws IOException {
		File[] filein = (new File(in)).listFiles();
		for (int i = 0; i < filein.length; i++) {
			if (filein[i].getName().endsWith("gc") && !filein[i].getName().startsWith("Category")) {
				if (!filein[i].getName().contains("human") && !filein[i].getName().contains("mouse")) {
					System.out.println(filein[i].getName());
					BufferedReader reader = new BufferedReader(new FileReader(filein[i]));
					PrintWriter writer = new PrintWriter(new File(out, filein[i].getName()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						String newline = line.replaceAll(",\\s?", ";");
						writer.println(newline);
					}
					reader.close();
					writer.close();
				}
			}
		}
	}
}
