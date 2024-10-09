/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantParaHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPep4Meta;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantProReader;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaMaxQuantPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParaIOMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesIOV2;

/**
 * @author Kai Cheng
 *
 */
public class MetaMQSearchTask extends MetaAbstractTask {

	private MetaMaxQuantPar mcsp;

	private Logger LOGGER = LogManager.getLogger(MetaMQSearchTask.class);
	private String taskName = "Closed search";

	public MetaMQSearchTask(MetaParameterMQ metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
	}

	protected void initial() {
		this.mcsp = ((MetaParameterMQ) metaPar).getCsp();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": started");

		File closedSearchFile = new File(metaPar.getResult(), "maxquant_search");
		if (!closedSearchFile.exists()) {
			closedSearchFile.mkdir();
		}

		metaPar.setDbSearchResultFile(closedSearchFile);

		File maxquantCmd = new File(((MetaSourcesV2) msv).getMaxquant());

		if (maxquantCmd.exists()) {

			File peptides = maxquantSearch();

			if (peptides != null && peptides.exists()) {

				File combined = peptides.getParentFile();

				File summary = new File(combined, "summary.txt");

				File proteins = new File(combined, "proteinGroups.txt");

				File reportDir = new File(closedSearchFile, "report");
				if (!reportDir.exists()) {
					reportDir.mkdirs();
				}

				MetaReportCopyTask.copy(reportDir, MetaParaIOMQ.version);

				metaPar.setReportDir(reportDir);

				File reportFolder = new File(reportDir + "\\reports\\rmd_html");

				if (!reportFolder.exists()) {
					reportFolder.mkdirs();
				}
				
				MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

				File report_ID_summary = new File(reportFolder, "report_ID_summary.html");

				if (!report_ID_summary.exists() || report_ID_summary.length() == 0) {
					task.addTask(MetaReportTask.summary, summary, report_ID_summary);
				}

				File summaryMetaFile = new File(combined, "metainfo.tsv");
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

				File report_peptides_summary = new File(reportFolder, "report_peptides_summary.html");
				if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {

					File pepReport = new File(peptides.getParentFile(),
							peptides.getName().replace(".txt", "_report.txt"));
					convertPep2Report(peptides, pepReport);

					if (summaryMetaFile.exists()) {
						task.addTask(MetaReportTask.peptide, pepReport, report_peptides_summary, summaryMetaFile);
					} else {
						task.addTask(MetaReportTask.peptide, pepReport, report_peptides_summary);
					}
				}

				File report_proteinGroups_summary = new File(reportFolder, "report_proteinGroups_summary.html");
				if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {

					File proReport = new File(proteins.getParentFile(),
							proteins.getName().replace(".txt", "_report.txt"));
					convertPro2Report(proteins, proReport);

					if (summaryMetaFile.exists()) {
						task.addTask(MetaReportTask.protein, proReport, report_proteinGroups_summary, summaryMetaFile);

					} else {
						task.addTask(MetaReportTask.protein, proReport, report_proteinGroups_summary);
					}
				}

				task.run();
				
				setProgress(60);

				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");

				return task.get();

			} else {
				LOGGER.info(taskName + ": MaxQuant search result was not found");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": MaxQuant search result was not found");

				return false;
			}

		} else {

			LOGGER.info(taskName + ": MaxQuant was not found, please set it in Setting->Resource");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": MaxQuant was not found, please set it in Setting->Resource");

			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public File maxquantSearch() {

		File closedSearchFile = metaPar.getDbSearchResultFile();
		File peptides = new File(closedSearchFile.getAbsoluteFile() + "\\combined\\txt\\peptides.txt");
		if (peptides.exists() && peptides.length() > 0) {
			LOGGER.info(taskName + ": peptide search result was found in " + peptides + ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": peptide search result was found in "
					+ peptides + ", go to next step");

			return peptides;
		}

		MaxquantModification[] variMods = ((MetaParameterMQ) metaPar).getVariMods();
		MaxquantModification[] fixMods = ((MetaParameterMQ) metaPar).getFixMods();
		Enzyme enzyme = ((MetaParameterMQ) metaPar).getEnzyme();
		int digestMode = ((MetaParameterMQ) metaPar).getDigestMode();
		int missCleavage = ((MetaParameterMQ) metaPar).getMissCleavages();

		MaxquantModification[][] labels = ((MetaParameterMQ) metaPar).getLabels();
		MaxquantModification[] isobaric = ((MetaParameterMQ) metaPar).getIsobaric();
		double[][] isoCorFactor = ((MetaParameterMQ) metaPar).getIsoCorFactor();
		String quanMode = ((MetaParameterMQ) metaPar).getQuanMode();

		String fasta = metaPar.getCurrentDb();
		MetaData metadata = metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		int[] fractions = metadata.getFractions();

		int threads = metaPar.getThreadCount();
		double closedPsmFdr = mcsp.getClosedPsmFdr();
		double closedProFdr = mcsp.getClosedProFdr();
		boolean mbr = mcsp.isMbr();
		double matchTimeWin = mcsp.getMatchTimeWin();
		double matchIonWin = mcsp.getMatchIonWin();
		double alignTimeWin = mcsp.getAlignTimeWin();
		double alignIonMob = mcsp.getAlignIonMobility();
		boolean matchUnidenFea = mcsp.isMatchUnidenFeature();
		int minRatioCount = mcsp.getMinRatioCount();
		int lfqMinRatioCount = mcsp.getLfqMinRatioCount();

		String maxquantCmd = ((MetaSourcesV2) msv).getMaxquant();
		File maxquantCmdFile = new File(maxquantCmd);
		File mqpar = new File(maxquantCmdFile.getParent(), "mqpar.xml");

		if (!mqpar.exists()) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(maxquantCmdFile.getName());
				sb.append(" -c ");
				sb.append(mqpar.getAbsolutePath());

				int id = maxquantCmdFile.getAbsolutePath().indexOf(":");
				if (id < 0) {
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": cann't find the directory of MaxQuantCmd.exe");
					LOGGER.error(taskName + ": cann't find the directory of MaxQuantCmd.exe");
				}

				File batFile = new File(mqpar.getParent(), mqpar.getName() + ".bat");
				PrintWriter batWriter = new PrintWriter(batFile);

				batWriter.println(maxquantCmdFile.getAbsolutePath().substring(0, id) + ":");
				batWriter.println("cd " + maxquantCmdFile.getParent());
				batWriter.println(sb);
				batWriter.println("exit");
				batWriter.close();

				System.out.println(format.format(new Date()) + "\t" + sb);
				LOGGER.info(taskName + ": " + sb);

				String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };

				ProcessBuilder pb = new ProcessBuilder(args);
				Process p = pb.start();

				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
				}

				if (p.waitFor() != 0) {
					if (p.exitValue() == 1) {
						LOGGER.error(taskName + ": error in creating MaxQuant parameter file");
						System.err.println(
								format.format(new Date()) + "\t" + taskName + ": error in creating MaxQuant parameter");
					}
				}
				inBr.close();
				in.close();

			} catch (Exception e) {
				LOGGER.error(taskName + ": error in creating MaxQuant parameter file", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in creating MaxQuant parameter");
			}
		}

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(mqpar);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading MaxQuant parameter in " + mqpar, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading MaxQuant parameter in " + mqpar);
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threads));
			} else if (name.equals("fixedCombinedFolder")) {
				element.setText(closedSearchFile.getAbsolutePath());
			} else if (name.equals("tempFolder")) {
				element.setText(closedSearchFile.getAbsolutePath());
			} else if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles) {
					Element eFile = DocumentFactory.getInstance().createElement("string");
					eFile.setText(raw);
					element.add(eFile);
				}
			} else if (name.equals("experiments")) {
				Iterator<Element> expIt = element.elementIterator();
				while (expIt.hasNext()) {
					Element eExp = expIt.next();
					element.remove(eExp);
				}

				for (String fn : expNames) {
					Element eExp = DocumentFactory.getInstance().createElement("string");
					eExp.setText(fn);
					element.add(eExp);
				}
			} else if (name.equals("fractions")) {
				Iterator<Element> fraIt = element.elementIterator();
				while (fraIt.hasNext()) {
					Element eFra = fraIt.next();
					element.remove(eFra);
				}

				for (int i = 0; i < fractions.length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText(String.valueOf(fractions[i]));
					element.add(eFra);
				}
			} else if (name.equals("writeMsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writePasefMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeAccumulatedPasefMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeMs3ScansTable")) {
				element.setText("false");
			} else if (name.equals("writeAllPeptidesTable")) {
				element.setText("false");
			} else if (name.equals("writeMzRangeTable")) {
				element.setText("false");
			} else if (name.equals("writeMzTab")) {
				element.setText("false");
			} else if (name.equals("matchBetweenRuns")) {
				element.setText(String.valueOf(mbr));
			} else if (name.equals("matchUnidentifiedFeatures")) {
				element.setText(String.valueOf(matchUnidenFea));
			} else if (name.equals("matchingTimeWindow")) {
				element.setText(String.valueOf(matchTimeWin));
			} else if (name.equals("matchingIonMobilityWindow")) {
				element.setText(String.valueOf(matchIonWin));
			} else if (name.equals("alignmentTimeWindow")) {
				element.setText(String.valueOf(alignTimeWin));
			} else if (name.equals("alignmentIonMobilityWindow")) {
				element.setText(String.valueOf(alignIonMob));
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("boolean");
					ePg.setText("false");
					element.add(ePg);
				}
			} else if (name.equals("paramGroupIndices")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
				}
			} else if (name.equals("referenceChannel")) {
				Iterator<Element> rcIt = element.elementIterator();
				while (rcIt.hasNext()) {
					Element eRc = rcIt.next();
					element.remove(eRc);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element eRc = DocumentFactory.getInstance().createElement("string");
					element.add(eRc);
				}
			} else if (name.equals("parameterGroups")) {
				Element para = element.element("parameterGroup");
				Iterator<Element> pgIt = para.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					if (ePg.getName().equals("enzymes")) {
						Iterator<Element> enIt = ePg.elementIterator();
						while (enIt.hasNext()) {
							Element eEn = enIt.next();
							ePg.remove(eEn);
						}

						Element eEn = DocumentFactory.getInstance().createElement("string");
						eEn.setText(enzyme.getName());
						ePg.add(eEn);

					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i].getTitle());
							ePg.add(eVm);
						}

					} else if (ePg.getName().equals("labelMods")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}

						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							for (int i = 0; i < labels.length; i++) {
								if (labels[i].length > 0) {
									StringBuilder sb = new StringBuilder();
									for (int j = 0; j < labels[i].length; j++) {
										sb.append(labels[i][j].getTitle()).append(";");
									}
									sb.deleteCharAt(sb.length() - 1);
									Element eLb = DocumentFactory.getInstance().createElement("string");
									eLb.setText(sb.toString());
									ePg.add(eLb);
								}
							}
						} else {
							Element eLb = DocumentFactory.getInstance().createElement("string");
							ePg.add(eLb);
						}

					} else if (ePg.getName().equals("maxLabeledAa")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							ePg.setText(String.valueOf(labels.length));
						} else {
							ePg.setText("0");
						}
					} else if (ePg.getName().equals("multiplicity")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							ePg.setText(String.valueOf(labels.length));
						} else {
							ePg.setText("1");
						}
					} else if (ePg.getName().equals("isobaricLabels")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							HashMap<String, Element> elementMap = new HashMap<String, Element>();
							for (int i = 0; i < isobaric.length; i++) {
								String isobaricTitle = isobaric[i].getTitle();

								if (isobaricTitle.length() > 0) {

									String[] cs = isobaricTitle.split("-");
									String key = null;
									boolean addCorFactor = false;
									Element eIsoInfo;

									if (cs[1].startsWith("Lys")) {
										key = cs[0] + cs[1].substring(3);
										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);
											eIsoInfo.element("internalLabel").setText(isobaricTitle);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText(isobaricTitle);
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText("");
											eIsoInfo.add(eTerminal);

											elementMap.put(key, eIsoInfo);
											addCorFactor = true;
										}
									} else if (cs[1].startsWith("Nter")) {
										key = cs[0] + cs[1].substring(4);
										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);
											eIsoInfo.element("terminalLabel").setText(isobaricTitle);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText("");
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText(isobaricTitle);
											eIsoInfo.add(eTerminal);

											elementMap.put(key, eIsoInfo);
											addCorFactor = true;
										}
									} else {
										key = isobaricTitle;

										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);

										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText("");
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText(isobaricTitle);
											eIsoInfo.add(eTerminal);

											elementMap.put(isobaricTitle, eIsoInfo);
											addCorFactor = true;
										}
									}

									if (addCorFactor) {
										Element eM2 = DocumentFactory.getInstance().createElement("correctionFactorM2");
										eM2.setText(String.valueOf(isoCorFactor[i][0]));
										eIsoInfo.add(eM2);

										Element eM1 = DocumentFactory.getInstance().createElement("correctionFactorM1");
										eM1.setText(String.valueOf(isoCorFactor[i][1]));
										eIsoInfo.add(eM1);

										Element eP1 = DocumentFactory.getInstance().createElement("correctionFactorP1");
										eP1.setText(String.valueOf(isoCorFactor[i][2]));
										eIsoInfo.add(eP1);

										Element eP2 = DocumentFactory.getInstance().createElement("correctionFactorP2");
										eP2.setText(String.valueOf(isoCorFactor[i][3]));
										eIsoInfo.add(eP2);

										Element eTmt = DocumentFactory.getInstance().createElement("tmtLike");
										if (cs[0].startsWith("TMT")) {
											eTmt.setText("True");
										} else {
											eTmt.setText("False");
										}
										eIsoInfo.add(eTmt);
									}
								}
							}

							String[] keys = elementMap.keySet().toArray(new String[elementMap.size()]);
							Arrays.sort(keys);
							for (String key : keys) {
								Element eIsoInfo = elementMap.get(key);
								ePg.add(eIsoInfo);
							}
						} else {
							ePg.setText("");
						}
					} else if (ePg.getName().equals("lcmsRunType")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("Reporter ion MS2");
						} else {
							ePg.setText("Standard");
						}
					} else if (ePg.getName().equals("reporterMassTolerance")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0.003");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterPif")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterFraction")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterBasePeakRatio")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("fastLfq")) {
						ePg.setText("True");
					} else if (ePg.getName().equals("lfqMinRatioCount")) {
						ePg.setText(String.valueOf(lfqMinRatioCount));
					} else if (ePg.getName().equals("maxMissedCleavages")) {
						ePg.setText(String.valueOf(missCleavage));
					} else if (ePg.getName().equals("lfqMode")) {
						if (quanMode.equals(MetaConstants.labelFree) && rawFiles.length > 1) {
							ePg.setText(String.valueOf(1));
						} else {
							ePg.setText(String.valueOf(0));
						}
					} else if (ePg.getName().equals("enzymeMode")) {
						ePg.setText(String.valueOf(digestMode));
					} else {
						List<Element> list = ePg.elements();
						if (list == null || list.size() == 0) {
							ePg.setText(ePg.getText());
						} else {
							for (int i = 0; i < list.size(); i++) {
								Element ei = list.get(i);
								if (ei.getName().equals("string")) {
									ei.setText(ei.getText());
								}
							}
						}
					}
				}
			} else if (name.equals("fastaFiles")) {
				Element eFileInfo = element.element("FastaFileInfo");
				if (eFileInfo == null) {
					Iterator<Element> faIt = element.elementIterator();
					while (faIt.hasNext()) {
						Element eFa = faIt.next();
						element.remove(eFa);
					}

					Element eFa = DocumentFactory.getInstance().createElement("string");
					eFa.setText(fasta);
					element.add(eFa);
				} else {
					Iterator<Element> faIt = eFileInfo.elementIterator();
					while (faIt.hasNext()) {
						Element eFa = faIt.next();
						if (eFa.getName().equals("fastaFilePath")) {
							eFa.setText(fasta);
						} else if (eFa.getName().equals("identifierParseRule")) {
							eFa.setText(MaxquantParaHandler.getDatabaseIdentifier(fasta));
						} else if (eFa.getName().equals("descriptionParseRule")) {
							eFa.setText(">(.*)");
						} else {
							List<Element> list = eFa.elements();
							if (list == null || list.size() == 0) {
								element.setText(element.getText());
							} else {
								for (int i = 0; i < list.size(); i++) {
									Element ei = list.get(i);
									if (ei.getName().equals("string")) {
										ei.setText(ei.getText());
									}
								}
							}
						}
					}
				}
			} else if (name.equals("fixedModifications")) {
				Iterator<Element> fmIt = element.elementIterator();
				while (fmIt.hasNext()) {
					Element eFm = fmIt.next();
					element.remove(eFm);
				}

				for (int i = 0; i < fixMods.length; i++) {
					Element eFm = DocumentFactory.getInstance().createElement("string");
					eFm.setText(fixMods[i].getTitle());
					element.add(eFm);
				}
			} else if (name.equals("restrictMods")) {
				Iterator<Element> vmIt = element.elementIterator();
				while (vmIt.hasNext()) {
					Element eVm = vmIt.next();
					element.remove(eVm);
				}

				for (int i = 0; i < variMods.length; i++) {
					Element eVm = DocumentFactory.getInstance().createElement("string");
					eVm.setText(variMods[i].getTitle());
					element.add(eVm);
				}
			} else if (name.equals("peptideFdr")) {
				element.setText(String.valueOf(closedPsmFdr));
			} else if (name.equals("proteinFdr")) {
				element.setText(String.valueOf(closedProFdr));
			} else if (name.equals("minRatioCount")) {
				element.setText(String.valueOf(minRatioCount));
			} else {
				List<Element> list = element.elements();
				if (list == null || list.size() == 0) {
					element.setText(element.getText());
				} else {
					for (int i = 0; i < list.size(); i++) {
						Element ei = list.get(i);
						if (ei.getName().equals("string")) {
							ei.setText(ei.getText());
						}
					}
				}
			}
		}

		File parFile = new File(closedSearchFile, "mqpar.xml");
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(parFile), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing MaxQuant parameter to " + parFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing MaxQuant parameter to "
					+ parFile);
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append(maxquantCmdFile.getName());
			sb.append(" ");
			sb.append(parFile.getAbsolutePath());

			int id = maxquantCmdFile.getAbsolutePath().indexOf(":");
			if (id < 0) {
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": cann't find the directory of MaxQuantCmd.exe");
				LOGGER.error(taskName + ": cann't find the directory of MaxQuantCmd.exe");
			}

			File batFile = new File(parFile.getParent(), parFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			batWriter.println(maxquantCmdFile.getAbsolutePath().substring(0, id) + ":");
			batWriter.println("cd " + maxquantCmdFile.getParent());
			batWriter.println(sb);
			batWriter.println("exit");
			batWriter.close();

			System.out.println(format.format(new Date()) + "\t" + sb);
			LOGGER.info(taskName + ": " + sb);

			String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error(taskName + ": error in database searching by MaxQuant");
					System.err.println(
							format.format(new Date()) + "\t" + taskName + ": error in database searching by MaxQuant");
				}
			}
			inBr.close();
			in.close();

		} catch (Exception e) {
			LOGGER.error(taskName + ": error in database searching by MaxQuant", e);
			System.err
					.println(format.format(new Date()) + "\t" + taskName + ": error in database searching by MaxQuant");
		}

		for (int i = 0; i < rawFiles.length; i++) {
			File file = new File(rawFiles[i]);
			if (file.exists()) {
				String name = file.getName();
				if (name.endsWith("raw")) {
					name = name.substring(0, name.lastIndexOf("."));
				}
				File folder = new File(file.getParentFile(), name);
				if (folder.exists() && folder.isDirectory()) {
					FileUtils.deleteQuietly(folder);
				}
			}
		}

		File combine = new File(closedSearchFile, "combined");
		File[] results = combine.listFiles();
		for (int i = 0; i < results.length; i++) {
			if (!results[i].getName().equals("txt")) {
				FileUtils.deleteQuietly(results[i]);
			}
		}

		return peptides;
	}

	@SuppressWarnings("unchecked")
	protected void maxquantParTest(String mqpar, String intput, String output) {

		MaxquantModification[] variMods = ((MetaParameterMQ) metaPar).getVariMods();
		MaxquantModification[] fixMods = ((MetaParameterMQ) metaPar).getFixMods();
		Enzyme enzyme = ((MetaParameterMQ) metaPar).getEnzyme();
		int digestMode = ((MetaParameterMQ) metaPar).getDigestMode();
		int missCleavage = ((MetaParameterMQ) metaPar).getMissCleavages();

		MaxquantModification[][] labels = ((MetaParameterMQ) metaPar).getLabels();
		MaxquantModification[] isobaric = ((MetaParameterMQ) metaPar).getIsobaric();
		double[][] isoCorFactor = ((MetaParameterMQ) metaPar).getIsoCorFactor();
		String quanMode = ((MetaParameterMQ) metaPar).getQuanMode();

		String fasta = metaPar.getCurrentDb();
		MetaData metadata = metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		int[] fractions = metadata.getFractions();

		int threads = metaPar.getThreadCount();
		double closedPsmFdr = mcsp.getClosedPsmFdr();
		double closedProFdr = mcsp.getClosedProFdr();
		boolean mbr = mcsp.isMbr();
		double matchTimeWin = mcsp.getMatchTimeWin();
		double matchIonWin = mcsp.getMatchIonWin();
		double alignTimeWin = mcsp.getAlignTimeWin();
		double alignIonMob = mcsp.getAlignIonMobility();
		boolean matchUnidenFea = mcsp.isMatchUnidenFeature();
		int minRatioCount = mcsp.getMinRatioCount();
		int lfqMinRatioCount = mcsp.getLfqMinRatioCount();

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(mqpar);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in reading MaxQuant parameter in " + mqpar, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in reading MaxQuant parameter in " + mqpar);
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threads));
			} else if (name.equals("fixedCombinedFolder")) {
				element.setText(intput);
			} else if (name.equals("tempFolder")) {
				element.setText(intput);
			} else if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles) {
					Element eFile = DocumentFactory.getInstance().createElement("string");
					eFile.setText(raw);
					element.add(eFile);
				}
			} else if (name.equals("experiments")) {
				Iterator<Element> expIt = element.elementIterator();
				while (expIt.hasNext()) {
					Element eExp = expIt.next();
					element.remove(eExp);
				}

				for (String fn : expNames) {
					Element eExp = DocumentFactory.getInstance().createElement("string");
					eExp.setText(fn);
					element.add(eExp);
				}
			} else if (name.equals("fractions")) {
				Iterator<Element> fraIt = element.elementIterator();
				while (fraIt.hasNext()) {
					Element eFra = fraIt.next();
					element.remove(eFra);
				}

				for (int i = 0; i < fractions.length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText(String.valueOf(fractions[i]));
					element.add(eFra);
				}
			} else if (name.equals("writeMsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writePasefMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeAccumulatedPasefMsmsScansTable")) {
				element.setText("false");
			} else if (name.equals("writeMs3ScansTable")) {
				element.setText("false");
			} else if (name.equals("writeAllPeptidesTable")) {
				element.setText("false");
			} else if (name.equals("writeMzRangeTable")) {
				element.setText("false");
			} else if (name.equals("writeMzTab")) {
				element.setText("false");
			} else if (name.equals("matchBetweenRuns")) {
				element.setText(String.valueOf(mbr));
			} else if (name.equals("matchUnidentifiedFeatures")) {
				element.setText(String.valueOf(matchUnidenFea));
			} else if (name.equals("matchingTimeWindow")) {
				element.setText(String.valueOf(matchTimeWin));
			} else if (name.equals("matchingIonMobilityWindow")) {
				element.setText(String.valueOf(matchIonWin));
			} else if (name.equals("alignmentTimeWindow")) {
				element.setText(String.valueOf(alignTimeWin));
			} else if (name.equals("alignmentIonMobilityWindow")) {
				element.setText(String.valueOf(alignIonMob));
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("boolean");
					ePg.setText("false");
					element.add(ePg);
				}
			} else if (name.equals("paramGroupIndices")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
				}
			} else if (name.equals("referenceChannel")) {
				Iterator<Element> rcIt = element.elementIterator();
				while (rcIt.hasNext()) {
					Element eRc = rcIt.next();
					element.remove(eRc);
				}

				for (int i = 0; i < rawFiles.length; i++) {
					Element eRc = DocumentFactory.getInstance().createElement("string");
					element.add(eRc);
				}
			} else if (name.equals("parameterGroups")) {
				Element para = element.element("parameterGroup");
				Iterator<Element> pgIt = para.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					if (ePg.getName().equals("enzymes")) {
						Iterator<Element> enIt = ePg.elementIterator();
						while (enIt.hasNext()) {
							Element eEn = enIt.next();
							ePg.remove(eEn);
						}

						Element eEn = DocumentFactory.getInstance().createElement("string");
						eEn.setText(enzyme.getName());
						ePg.add(eEn);

					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i].getTitle());
							ePg.add(eVm);
						}

					} else if (ePg.getName().equals("labelMods")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}

						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							for (int i = 0; i < labels.length; i++) {
								if (labels[i].length > 0) {
									StringBuilder sb = new StringBuilder();
									for (int j = 0; j < labels[i].length; j++) {
										sb.append(labels[i][j].getTitle()).append(";");
									}
									sb.deleteCharAt(sb.length() - 1);
									Element eLb = DocumentFactory.getInstance().createElement("string");
									eLb.setText(sb.toString());
									ePg.add(eLb);
								}
							}
						} else {
							Element eLb = DocumentFactory.getInstance().createElement("string");
							ePg.add(eLb);
						}

					} else if (ePg.getName().equals("maxLabeledAa")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							ePg.setText(String.valueOf(labels.length));
						} else {
							ePg.setText("0");
						}
					} else if (ePg.getName().equals("multiplicity")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							ePg.setText(String.valueOf(labels.length));
						} else {
							ePg.setText("1");
						}
					} else if (ePg.getName().equals("isobaricLabels")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							HashMap<String, Element> elementMap = new HashMap<String, Element>();
							for (int i = 0; i < isobaric.length; i++) {
								String isobaricTitle = isobaric[i].getTitle();

								if (isobaricTitle.length() > 0) {

									String[] cs = isobaricTitle.split("-");
									String key = null;
									boolean addCorFactor = false;
									Element eIsoInfo;

									if (cs[1].startsWith("Lys")) {
										key = cs[0] + cs[1].substring(3);
										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);
											eIsoInfo.element("internalLabel").setText(isobaricTitle);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText(isobaricTitle);
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText("");
											eIsoInfo.add(eTerminal);

											elementMap.put(key, eIsoInfo);
											addCorFactor = true;
										}
									} else if (cs[1].startsWith("Nter")) {
										key = cs[0] + cs[1].substring(4);
										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);
											eIsoInfo.element("terminalLabel").setText(isobaricTitle);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText("");
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText(isobaricTitle);
											eIsoInfo.add(eTerminal);

											elementMap.put(key, eIsoInfo);
											addCorFactor = true;
										}
									} else {
										key = isobaricTitle;

										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);

										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText("");
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText(isobaricTitle);
											eIsoInfo.add(eTerminal);

											elementMap.put(isobaricTitle, eIsoInfo);
											addCorFactor = true;
										}
									}

									if (addCorFactor) {
										Element eM2 = DocumentFactory.getInstance().createElement("correctionFactorM2");
										eM2.setText(String.valueOf(isoCorFactor[i][0]));
										eIsoInfo.add(eM2);

										Element eM1 = DocumentFactory.getInstance().createElement("correctionFactorM1");
										eM1.setText(String.valueOf(isoCorFactor[i][1]));
										eIsoInfo.add(eM1);

										Element eP1 = DocumentFactory.getInstance().createElement("correctionFactorP1");
										eP1.setText(String.valueOf(isoCorFactor[i][2]));
										eIsoInfo.add(eP1);

										Element eP2 = DocumentFactory.getInstance().createElement("correctionFactorP2");
										eP2.setText(String.valueOf(isoCorFactor[i][3]));
										eIsoInfo.add(eP2);

										Element eTmt = DocumentFactory.getInstance().createElement("tmtLike");
										if (cs[0].startsWith("TMT")) {
											eTmt.setText("True");
										} else {
											eTmt.setText("False");
										}
										eIsoInfo.add(eTmt);
									}
								}
							}

							String[] keys = elementMap.keySet().toArray(new String[elementMap.size()]);
							Arrays.sort(keys);
							for (String key : keys) {
								Element eIsoInfo = elementMap.get(key);
								ePg.add(eIsoInfo);
							}
						} else {
							ePg.setText("");
						}
					} else if (ePg.getName().equals("lcmsRunType")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("Reporter ion MS2");
						} else {
							ePg.setText("Standard");
						}
					} else if (ePg.getName().equals("reporterMassTolerance")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0.003");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterPif")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterFraction")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("reporterBasePeakRatio")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("0");
						} else {
							ePg.setText("NaN");
						}
					} else if (ePg.getName().equals("fastLfq")) {
						ePg.setText("True");
					} else if (ePg.getName().equals("lfqMinRatioCount")) {
						ePg.setText(String.valueOf(lfqMinRatioCount));
					} else if (ePg.getName().equals("maxMissedCleavages")) {
						ePg.setText(String.valueOf(missCleavage));
					} else if (ePg.getName().equals("lfqMode")) {
						if (quanMode.equals(MetaConstants.labelFree) && rawFiles.length > 1) {
							ePg.setText(String.valueOf(1));
						} else {
							ePg.setText(String.valueOf(0));
						}
					} else if (ePg.getName().equals("enzymeMode")) {
						ePg.setText(String.valueOf(digestMode));
					} else {
						List<Element> list = ePg.elements();
						if (list == null || list.size() == 0) {
							ePg.setText(ePg.getText());
						} else {
							for (int i = 0; i < list.size(); i++) {
								Element ei = list.get(i);
								if (ei.getName().equals("string")) {
									ei.setText(ei.getText());
								}
							}
						}
					}
				}
			} else if (name.equals("fastaFiles")) {
				Element eFileInfo = element.element("FastaFileInfo");
				if (eFileInfo == null) {
					Iterator<Element> faIt = element.elementIterator();
					while (faIt.hasNext()) {
						Element eFa = faIt.next();
						element.remove(eFa);
					}

					Element eFa = DocumentFactory.getInstance().createElement("string");
					eFa.setText(fasta);
					element.add(eFa);
				} else {
					Iterator<Element> faIt = eFileInfo.elementIterator();
					while (faIt.hasNext()) {
						Element eFa = faIt.next();
						if (eFa.getName().equals("fastaFilePath")) {
							eFa.setText(fasta);
						} else if (eFa.getName().equals("identifierParseRule")) {
							eFa.setText(MaxquantParaHandler.getDatabaseIdentifier(fasta));
						} else if (eFa.getName().equals("descriptionParseRule")) {
							eFa.setText(">(.*)");
						} else {
							List<Element> list = eFa.elements();
							if (list == null || list.size() == 0) {
								element.setText(element.getText());
							} else {
								for (int i = 0; i < list.size(); i++) {
									Element ei = list.get(i);
									if (ei.getName().equals("string")) {
										ei.setText(ei.getText());
									}
								}
							}
						}
					}
				}
			} else if (name.equals("fixedModifications")) {
				Iterator<Element> fmIt = element.elementIterator();
				while (fmIt.hasNext()) {
					Element eFm = fmIt.next();
					element.remove(eFm);
				}

				for (int i = 0; i < fixMods.length; i++) {
					Element eFm = DocumentFactory.getInstance().createElement("string");
					eFm.setText(fixMods[i].getTitle());
					element.add(eFm);
				}
			} else if (name.equals("restrictMods")) {
				Iterator<Element> vmIt = element.elementIterator();
				while (vmIt.hasNext()) {
					Element eVm = vmIt.next();
					element.remove(eVm);
				}

				for (int i = 0; i < variMods.length; i++) {
					Element eVm = DocumentFactory.getInstance().createElement("string");
					eVm.setText(variMods[i].getTitle());
					element.add(eVm);
				}
			} else if (name.equals("peptideFdr")) {
				element.setText(String.valueOf(closedPsmFdr));
			} else if (name.equals("proteinFdr")) {
				element.setText(String.valueOf(closedProFdr));
			} else if (name.equals("minRatioCount")) {
				element.setText(String.valueOf(minRatioCount));
			} else {
				List<Element> list = element.elements();
				if (list == null || list.size() == 0) {
					element.setText(element.getText());
				} else {
					for (int i = 0; i < list.size(); i++) {
						Element ei = list.get(i);
						if (ei.getName().equals("string")) {
							ei.setText(ei.getText());
						}
					}
				}
			}
		}

		File parFile = new File(output, "mqpar.xml");
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(parFile), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing MaxQuant parameter to " + parFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in writing MaxQuant parameter to "
					+ parFile);
		}
	}
	
	private void convertPep2Report(File peptides, File peptideReport) throws IOException {

		MaxquantPepReader pepReader = new MaxquantPepReader(peptides, (MetaParameterMQ) metaPar);
		PrintWriter writer = new PrintWriter(peptideReport);

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Sequence").append("\t");
		titlesb.append("Length").append("\t");
		titlesb.append("Missed cleavages").append("\t");
		titlesb.append("Proteins").append("\t");
		titlesb.append("Charges").append("\t");
		titlesb.append("PEP").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Intensity").append("\t");

		String[] intensityTitle = pepReader.getIntensityTitle();

		for (int i = 0; i < intensityTitle.length; i++) {
			titlesb.append(intensityTitle[i]).append("\t");
		}

		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id").append("\t");
		titlesb.append("MS/MS Count");

		writer.println(titlesb);

		MetaPeptide[] peps = pepReader.getMetaPeptides();
		int id = 1;
		for (int i = 0; i < peps.length; i++) {
			if (!peps[i].isUse()) {
				continue;
			}
			MaxquantPep4Meta pep = (MaxquantPep4Meta) peps[i];
			StringBuilder sb = new StringBuilder();

			sb.append(pep.getSequence()).append("\t");
			sb.append(pep.getSequence().length()).append("\t");
			sb.append(pep.getMissCleave()).append("\t");
			sb.append(pep.getProtein()).append("\t");
			sb.append(pep.getChargeString()).append("\t");
			sb.append(pep.getPEP()).append("\t");
			sb.append(pep.getScore()).append("\t");

			double[] intensity = pep.getIntensity();
			sb.append((int) MathTool.getTotal(intensity)).append("\t");
			for (int j = 0; j < intensity.length; j++) {
				sb.append((int) intensity[j]).append("\t");
			}

			if (pep.isReverse()) {
				sb.append("+").append("\t");
			} else {
				sb.append("").append("\t");
			}

			if (pep.isContaminant()) {
				sb.append("+").append("\t");
			} else {
				sb.append("").append("\t");
			}

			sb.append(id++).append("\t");
			sb.append(pep.getTotalMS2Count());

			writer.println(sb);
		}

		pepReader.close();
		writer.close();
	}

	public void convertPro2Report(File proteinGroups, File proteinReport) throws IOException {
		MaxquantProReader proReader = new MaxquantProReader(proteinGroups, (MetaParameterMQ) metaPar);
		MetaProtein[] pros = proReader.getMetaProteins();
		PrintWriter writer = new PrintWriter(proteinReport);

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Protein IDs").append("\t");
		titlesb.append("Peptides").append("\t");
		titlesb.append("Razor + unique peptides").append("\t");
		titlesb.append("Unique peptides").append("\t");
		titlesb.append("Score").append("\t");
		titlesb.append("Intensity").append("\t");

		MetaData metadata = metaPar.getMetadata();
		String[] expNames = metadata.getExpNames();
		String[] labelTitle = metadata.getLabelTitle();
		String[] labelExpNames = metadata.getLabelExpNames();

		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < expNames.length; i++) {
			for (int j = 0; j < labelTitle.length; j++) {
				map.put("Reporter intensity corrected " + (j + 1) + " " + expNames[i],
						labelExpNames[i * labelTitle.length + j]);
			}
		}

		String[] intensityTitle = proReader.getIntensityTitle();
		for (int i = 0; i < intensityTitle.length; i++) {
			if (map.containsKey(intensityTitle[i])) {
				titlesb.append(map.get(intensityTitle[i])).append("\t");
			} else {
				titlesb.append(intensityTitle[i]).append("\t");
			}
		}

		titlesb.append("Only identified by site").append("\t");
		titlesb.append("Reverse").append("\t");
		titlesb.append("Potential contaminant").append("\t");
		titlesb.append("id");

		writer.println(titlesb);

		MetaProtein protein = null;
		StringBuilder namesb = new StringBuilder();

		for (int i = 0; i < pros.length; i++) {

			if (protein == null) {
				protein = pros[i];
				namesb.append(pros[i].getName()).append(";");
				continue;
			}

			StringBuilder sb = new StringBuilder();
			int groupIdI = pros[i].getGroupId();

			if (protein.getGroupId() != groupIdI) {
				if (namesb != null && namesb.length() > 0) {
					String proName;
					if (namesb.charAt(namesb.length() - 1) == ';') {
						proName = namesb.substring(0, namesb.length() - 1);
					} else {
						proName = namesb.toString();
					}

					sb.append(proName).append("\t");
					sb.append(protein.getPepCount()).append("\t");
					sb.append(protein.getRazorUniPepCount()).append("\t");
					sb.append(protein.getUniPepCount()).append("\t");
					sb.append(protein.getScore()).append("\t");

					double[] intensity = protein.getIntensities();

					sb.append((long) MathTool.getTotal(intensity)).append("\t");
					for (int j = 0; j < intensity.length; j++) {
						sb.append((long) intensity[j]).append("\t");
					}

					sb.append("").append("\t");
					sb.append("").append("\t");
					sb.append("").append("\t");
					sb.append(protein.getGroupId());

					writer.println(sb);

					namesb = new StringBuilder();
					namesb.append(pros[i].getName()).append(";");
					protein = pros[i];
				}
			} else {
				namesb.append(pros[i].getName()).append(";");
			}
		}

		StringBuilder sb = new StringBuilder();
		String proName;
		if (namesb.charAt(namesb.length() - 1) == ';') {
			proName = namesb.substring(0, namesb.length() - 1);
		} else {
			proName = namesb.toString();
		}

		sb.append(proName).append("\t");
		sb.append(protein.getPepCount()).append("\t");
		sb.append(protein.getRazorUniPepCount()).append("\t");
		sb.append(protein.getUniPepCount()).append("\t");
		sb.append(protein.getScore()).append("\t");

		double[] intensity = protein.getIntensities();

		sb.append((int) MathTool.getTotal(intensity)).append("\t");
		for (int j = 0; j < intensity.length; j++) {
			sb.append((int) intensity[j]).append("\t");
		}

		sb.append("").append("\t");
		sb.append("").append("\t");
		sb.append("").append("\t");
		sb.append(protein.getGroupId());

		writer.println(sb);

		proReader.close();
		writer.close();
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

	public static void main(String[] args) {
		MetaParameterMQ metaPar = MetaParaIOMQ.parse(args[0]);
		MetaSourcesV2 msv = MetaSourcesIOV2.parse(args[1]);

		MetaMQSearchTask task = new MetaMQSearchTask(metaPar, msv, new JProgressBar(), new JProgressBar(), null);
		task.maxquantParTest(args[2], args[3], args[3]);
	}
}
