/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.function.COGFinder;
import bmi.med.uOttawa.metalab.core.function.CategoryFinder;
import bmi.med.uOttawa.metalab.core.function.GOBPFinder;
import bmi.med.uOttawa.metalab.core.function.GOCCFinder;
import bmi.med.uOttawa.metalab.core.function.GOMFFinder;
import bmi.med.uOttawa.metalab.core.function.KEGGFinder;
import bmi.med.uOttawa.metalab.core.function.KOFinder;
import bmi.med.uOttawa.metalab.core.function.NOGFinder;
import bmi.med.uOttawa.metalab.core.function.sql.IGCFuncSqliteSearcher;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantProReader;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinXMLWriter;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLWriter2;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class MetaFuncTaskV2 extends MetaAbstractTask {

	private static Logger LOGGER = LogManager.getLogger(MetaFuncTaskV2.class);
	private static final String taskName = "Functional annotation";
	private String searchType;
	private String quanType;

	public MetaFuncTaskV2(MetaParameterMQ metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
	}

	@Override
	protected void initial() {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		boolean finished = false;

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		File funcResultFile = null;

		if (((MetaParameterMQ) metaPar).getQuickOutput() != null) {
			funcResultFile = new File(((MetaParameterMQ) metaPar).getQuickOutput());
			if (!funcResultFile.exists()) {
				funcResultFile.mkdir();
			}

		} else {
			File summaryMetaFile;
			if (metaPar.getDbSearchResultFile() != null) {
				funcResultFile = new File(metaPar.getDbSearchResultFile(), "functional_annotation");
				summaryMetaFile = new File(metaPar.getDbSearchResultFile(), "metainfo.tsv");
			} else {
				funcResultFile = new File(metaPar.getResult(), "functional_annotation");
				summaryMetaFile = new File(metaPar.getResult(), "metainfo.tsv");
			}

			if (!funcResultFile.exists()) {
				funcResultFile.mkdir();
			}

			try {
				metaPar.getMetadata().exportMetadata(summaryMetaFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block

				LOGGER.error(taskName + ": error in exporting metadata information", e);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in exporting metadata information");

				return false;
			}

			metaPar.setMetafile(summaryMetaFile);
		}

		File funcResultXml = new File(funcResultFile, "functions.xml");

		if (funcResultXml.exists() && funcResultXml.length() > 0) {

			LOGGER.info(taskName + ": functional annotation result file " + funcResultXml + " was found");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": functional annotation result file "
					+ funcResultXml + " was found");

			setProgress(95);

			File funTsv = new File(funcResultFile, "functions.tsv");
			File html = new File(funcResultFile, "functions.html");

			SAXReader reader = new SAXReader();
			Document document = reader.read(funcResultXml);
			Element root = document.getRootElement();

			if (root.element("COGs") != null && root.element("NOGs") != null && root.element("KEGGs") != null
					&& root.element("ECs") != null && root.element("GOs") != null) {

				MetaProteinXMLReader2 funReader = new MetaProteinXMLReader2(funcResultXml);
				if (!funTsv.exists() || funTsv.length() == 0) {
					LOGGER.info(taskName + ": writing results to " + funTsv);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv);

					try {
						funReader.exportTsv(funTsv);
					} catch (IOException e) {
						LOGGER.error(taskName + ": writing results to " + funTsv + " failed", e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": writing results to "
								+ funTsv + " failed");

						return false;
					}

					LOGGER.info(taskName + ": writing results to " + funTsv + " finished");
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv
							+ " finished");
				} else {
					LOGGER.info(taskName + ": result file " + funTsv + " already exist");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": result file " + funTsv + " already exist");
				}

				setProgress(96);

				if (!html.exists() || html.length() == 0) {
					LOGGER.info(taskName + ": writing results to " + html);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html);

					try {
						funReader.exportHtml(html);
					} catch (IOException e) {
						LOGGER.error(taskName + ": writing results to " + html + " failed", e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html
								+ " failed");

						return false;
					}

					LOGGER.info(taskName + ": writing results to " + html + " finished");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": writing results to " + html + " finished");

				} else {
					LOGGER.info(taskName + ": result file " + funTsv + " already exist");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": result file " + funTsv + " already exist");
				}
			}

			setProgress(97);

			if (funTsv.exists() && html.exists()) {
				finished = true;
			}

			File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

			if (!reportDir.exists()) {
				reportDir.mkdirs();
			}

			MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

			File report_function_summary = new File(reportDir, "report_function_summary.html");
			if (!report_function_summary.exists() || report_function_summary.length() == 0) {

				if (this.metaPar.getMetafile() != null && this.metaPar.getMetafile().exists()) {

					try {
						task.addTask(MetaReportTask.function, funTsv, report_function_summary, metaPar.getMetafile());
					} catch (Exception e) {
						LOGGER.error(taskName + ": writing report to " + report_function_summary + " failed", e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": writing report to "
								+ report_function_summary + " failed");

						return false;
					}

				} else {
					try {
						task.addTask(MetaReportTask.function, funTsv, report_function_summary);
					} catch (Exception e) {
						LOGGER.error(taskName + ": writing report to " + report_function_summary + " failed", e);
						System.err.println(format.format(new Date()) + "\t" + taskName + ": writing report to "
								+ report_function_summary + " failed");

						return false;
					}
				}
			} else {
				LOGGER.info(taskName + ": report file " + report_function_summary + " already exist");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": report file "
						+ report_function_summary + " already exist");
			}

			task.run();

			finished = task.get();

			if (finished) {
				setProgress(100);
			}

			return finished;
		}

		MetaProtein[] metapros = null;
		String[] expTitles = null;

		switch (metaPar.getWorkflowType()) {
		case FunctionalAnnotation: {
			metapros = ((MetaParameterMQ) metaPar).getPros();
			expTitles = ((MetaParameterMQ) metaPar).getIntensityTitles();
			searchType = ((MetaParameterMQ) metaPar).getQuanResultType();
			quanType = ((MetaParameterMQ) metaPar).getQuanMode();
			break;
		}
		case MaxQuantWorkflow: {

			File maxquantTxt = new File(metaPar.getDbSearchResultFile() + "\\combined\\txt\\proteinGroups.txt");
			if (maxquantTxt.exists()) {
				MaxquantProReader reader = new MaxquantProReader(maxquantTxt, (MetaParameterMQ) metaPar);
				metapros = reader.getMetaProteins();
				expTitles = reader.getIntensityTitle();
				searchType = MetaConstants.maxQuant;
				quanType = reader.getQuanMode();

			} else {
				LOGGER.error(taskName + ": protein identification result was not found, task failed");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": protein identification result was not found, task failed");

				return false;
			}

			break;
		}

		case MSFraggerWorkflow: {

			File funTsv = new File(funcResultFile, "functions.tsv");
			File html = new File(funcResultFile, "functions.html");

			try {

				MetaProteinXMLReader2 funReader = new MetaProteinXMLReader2(funcResultXml);

				LOGGER.info(taskName + ": writing results to " + funTsv);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv);

				try {
					funReader.exportTsv(funTsv);
				} catch (IOException e) {
					LOGGER.error(taskName + ": writing results to " + funTsv + " failed", e);
					System.err.println(
							format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv + " failed");

					return false;
				}

				LOGGER.info(taskName + ": writing results to " + funTsv + " finished");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv + " finished");

				LOGGER.info(taskName + ": writing results to " + html);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html);

				try {
					funReader.exportHtml(html);
				} catch (IOException e) {
					LOGGER.error(taskName + ": writing results to " + html + " failed", e);
					System.err.println(
							format.format(new Date()) + "\t" + taskName + ": writing results to " + html + " failed");

					return false;
				}

				LOGGER.info(taskName + ": writing results to " + html + " finished");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": writing results to " + html + " finished");

			} catch (DocumentException e) {
				LOGGER.error(taskName + ": error in reading function result file " + funcResultXml, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in reading function result file " + funcResultXml);

				return false;
			}

			setProgress(100);

			return finished;
		}

		default:
			break;
		}

		if (metapros == null) {
			LOGGER.error(taskName + ": no proteins were found");
			LOGGER.error(taskName + ": task failed");

			System.err.println(format.format(new Date()) + "\t" + taskName + ": no proteins were found");
			System.err.println(format.format(new Date()) + "\t" + taskName + ": task failed");

			return false;
		}

		setProgress(86);

		File funTsv = new File(funcResultFile, "functions.tsv");

		if (msv.funcAnnoSqlite()) {
			boolean match = this.funcAnnoSqlite(metapros, expTitles, funcResultXml);
			if (!match) {
				LOGGER.error(taskName + ": error in searching functional annotation database");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in searching functional annotation database");
				return false;
			}

			MetaProteinXMLReader2 funReader = new MetaProteinXMLReader2(funcResultXml);

			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + funTsv);

			funReader.exportTsv(funTsv);

			File html = new File(funcResultFile, "functions.html");

			System.out.println(format.format(new Date()) + "\t" + taskName + ": writing results to " + html);

			funReader.exportHtml(html);

			if (funTsv.exists() && html.exists()) {
				finished = true;
			}
		}

		File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

		File report_function_summary = new File(reportDir, "report_function_summary.html");
		if (!report_function_summary.exists() || report_function_summary.length() == 0) {

			if (this.metaPar.getMetadata() == null) {
				task.addTask(MetaReportTask.function, funTsv, report_function_summary);
			} else {
				task.addTask(MetaReportTask.function, funTsv, report_function_summary, metaPar.getMetafile());
			}
		}

		task.run();

		finished = task.get();

		if (finished) {
			setProgress(100);
		}

		return finished;
	}

	/**
	 * @deprecated
	 * @param metapros
	 * @param fileNames
	 * @param final_pro_xml_file
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "unused" })
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

		MetaProteinXMLWriter writer = new MetaProteinXMLWriter(final_pro_xml_file.getAbsolutePath(),
				searchType, quanType, fileNames);

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

		MetaProteinXMLWriter2 writer = new MetaProteinXMLWriter2(final_pro_xml_file.getAbsolutePath(), searchType,
				quanType, fileNames);

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
}
