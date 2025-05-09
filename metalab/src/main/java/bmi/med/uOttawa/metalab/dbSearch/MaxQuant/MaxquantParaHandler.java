/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantParaHandler {

	public static final String path = "Resources//mq_bin//mqpar.xml";

	private static Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	public static void setThreadCount(int threadCount) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threadCount));
			}
		}
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static void setThreadCount(String path, int threadCount) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threadCount));
			}
		}
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

	public static void config(MetaParameterV1 par) {
		config(par, par.getMaxQuantPar());
	}

	@SuppressWarnings("unchecked")
	public static void config(MetaParameterV1 par, String parOutput) {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(parOutput);
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element element = it.next();
				String name = element.getName();
				if (name.equals("maxQuantVersion")) {
					String version = element.getText();
					String[] cs = version.split("\\.");

					Integer v1 = Integer.parseInt(cs[1]);
					Integer v2 = Integer.parseInt(cs[2]);
					Integer v3 = Integer.parseInt(cs[3]);

					if (cs.length == 4) {
						if (v1 == 5) {
							config_1_5_3_30(par, parOutput);
						} else if (v1 == 6) {
							if (v2 >= 4) {
								config_1_6_4_0(par, parOutput);
							} else if (v2 == 3) {
								if (v3 >= 4) {
									config_1_6_4_0(par, parOutput);
								} else {
									config_1_6_2_3(par, parOutput);
								}
							} else {
								config_1_6_2_3(par, parOutput);
							}
						} else {
							LOGGER.error("Error in reading MaxQuant parameter in " + parOutput);
						}
					} else {
						LOGGER.error("Error in reading MaxQuant parameter in " + parOutput);
					}
				}
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + parOutput, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void config_1_5_3_30(MetaParameterV1 par, String parOutput) {

		int threads = par.getThreadCount();
		String[] variMods = par.getVariMods();
		String[] fixMods = par.getFixMods();
		String[] enzyme = par.getEnzymes();
		int digestMode = par.getDigestMode();
		String[][] labels = par.getLabels();
		String[] isobaric = par.getIsobaric();
//		double[][] isoCorFactor = par.getIsoCorFactor();

		File ssDatabase = par.getSSDatabase();
		String fasta = ssDatabase.exists() ? ssDatabase.getAbsolutePath() : par.getDatabase();
		String[][] rawFiles = par.getRawFiles();

		double psmFdr = par.getPsmFdr();
		double proFdr = par.getProFdr();
		int minRatioCount = par.getMinRatioCount();
		int lfqMinRatioCount = par.getLfqMinRatioCount();
		String quanMode = par.getQuanMode();

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(parOutput);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + parOutput, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threads));
			} else if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles[0]) {
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

				for (String fn : rawFiles[1]) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText("0");
					element.add(eFra);
				}
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
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

						for (int i = 0; i < enzyme.length; i++) {
							Element eEn = DocumentFactory.getInstance().createElement("string");
							eEn.setText(enzyme[i]);
							ePg.add(eEn);
						}
					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i]);
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
										sb.append(labels[i][j]).append(";");
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
							int count = 0;
							for (int i = 0; i < isobaric.length; i++) {
								if (isobaric[i].length() > 0) {
									Element eLb = DocumentFactory.getInstance().createElement("string");
									eLb.setText(isobaric[i]);
									ePg.add(eLb);

									count++;
								}
							}
							par.setIsobaricLabelCount(count);
						} else {
							ePg.setText("");
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
					} else if (ePg.getName().equals("lcmsRunType")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("Reporter ion MS2");
						} else {
							ePg.setText("Standard");
						}
					} else if (ePg.getName().equals("lfqMinRatioCount")) {
						ePg.setText(String.valueOf(lfqMinRatioCount));
					} else if (ePg.getName().equals("lfqMode")) {
						if (quanMode.equals(MetaConstants.labelFree) && rawFiles[0].length > 1) {
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
							eFa.setText(getDatabaseIdentifier(fasta));
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
					eFm.setText(fixMods[i]);
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
					eVm.setText(variMods[i]);
					element.add(eVm);
				}
			} else if (name.equals("peptideFdr")) {
				element.setText(String.valueOf(psmFdr));
			} else if (name.equals("proteinFdr")) {
				element.setText(String.valueOf(proFdr));
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

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(parOutput), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + parOutput, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void config_1_6_2_3(MetaParameterV1 par, String parOutput) {

		int threads = par.getThreadCount();
		String[] variMods = par.getVariMods();
		String[] fixMods = par.getFixMods();
		String[] enzyme = par.getEnzymes();
		int digestMode = par.getDigestMode();
		String[][] labels = par.getLabels();
		String[] isobaric = par.getIsobaric();
//		double[][] isoCorFactor = par.getIsoCorFactor();

		File ssDatabase = par.getSSDatabase();
		String fasta = ssDatabase.exists() ? ssDatabase.getAbsolutePath() : par.getDatabase();
		String[][] rawFiles = par.getRawFiles();

		double psmFdr = par.getPsmFdr();
		double proFdr = par.getProFdr();
		int minRatioCount = par.getMinRatioCount();
		int lfqMinRatioCount = par.getLfqMinRatioCount();
		String quanMode = par.getQuanMode();

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(parOutput);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + parOutput, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threads));
			} else if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles[0]) {
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

				for (String fn : rawFiles[1]) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText("0");
					element.add(eFra);
				}
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
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

						for (int i = 0; i < enzyme.length; i++) {
							Element eEn = DocumentFactory.getInstance().createElement("string");
							eEn.setText(enzyme[i]);
							ePg.add(eEn);
						}
					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i]);
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
										sb.append(labels[i][j]).append(";");
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
							int count = 0;
							for (int i = 0; i < isobaric.length; i++) {
								if (isobaric[i].length() > 0) {
									Element eLb = DocumentFactory.getInstance().createElement("string");
									eLb.setText(isobaric[i]);
									ePg.add(eLb);

									count++;
								}
							}
							par.setIsobaricLabelCount(count);
						} else {
							ePg.setText("");
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
					} else if (ePg.getName().equals("lcmsRunType")) {
						if (quanMode.equals(MetaConstants.isobaricLabel)) {
							ePg.setText("Reporter ion MS2");
						} else {
							ePg.setText("Standard");
						}
					} else if (ePg.getName().equals("lfqMinRatioCount")) {
						ePg.setText(String.valueOf(lfqMinRatioCount));
					} else if (ePg.getName().equals("lfqMode")) {
						if (quanMode.equals(MetaConstants.labelFree) && rawFiles[0].length > 1) {
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
							eFa.setText(getDatabaseIdentifier(fasta));
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
					eFm.setText(fixMods[i]);
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
					eVm.setText(variMods[i]);
					element.add(eVm);
				}
			} else if (name.equals("peptideFdr")) {
				element.setText(String.valueOf(psmFdr));
			} else if (name.equals("proteinFdr")) {
				element.setText(String.valueOf(proFdr));
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

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(parOutput), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + parOutput, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void config_1_6_4_0(MetaParameterV1 par, String parOutput) {

		int threads = par.getThreadCount();
		String[] variMods = par.getVariMods();
		String[] fixMods = par.getFixMods();
		String[] enzyme = par.getEnzymes();
		int digestMode = par.getDigestMode();
		String[][] labels = par.getLabels();
		String[] isobaric = par.getIsobaric();
		double[][] isoCorFactor = par.getIsoCorFactor();

		File ssDatabase = par.getSSDatabase();
		String fasta = ssDatabase.exists() ? ssDatabase.getAbsolutePath() : par.getDatabase();
		String[][] rawFiles = par.getRawFiles();

		double psmFdr = par.getPsmFdr();
		double proFdr = par.getProFdr();
		int minRatioCount = par.getMinRatioCount();
		int lfqMinRatioCount = par.getLfqMinRatioCount();
		String quanMode = par.getQuanMode();

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(parOutput);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + parOutput, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("numThreads")) {
				element.setText(String.valueOf(threads));
			} else if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles[0]) {
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

				for (String fn : rawFiles[1]) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText("0");
					element.add(eFra);
				}
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
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

						for (int i = 0; i < enzyme.length; i++) {
							Element eEn = DocumentFactory.getInstance().createElement("string");
							eEn.setText(enzyme[i]);
							ePg.add(eEn);
						}
					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i]);
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
										sb.append(labels[i][j]).append(";");
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
								if (isobaric[i].length() > 0) {

									String[] cs = isobaric[i].split("-");
									String key = null;
									boolean addCorFactor = false;
									Element eIsoInfo;

									if (cs[1].startsWith("Lys")) {
										key = cs[0] + cs[1].substring(3);
										if (elementMap.containsKey(key)) {
											eIsoInfo = elementMap.get(key);
											eIsoInfo.element("internalLabel").setText(isobaric[i]);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText(isobaric[i]);
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
											eIsoInfo.element("terminalLabel").setText(isobaric[i]);
										} else {
											eIsoInfo = DocumentFactory.getInstance().createElement("IsobaricLabelInfo");

											Element eInternal = DocumentFactory.getInstance()
													.createElement("internalLabel");
											eInternal.setText("");
											eIsoInfo.add(eInternal);

											Element eTerminal = DocumentFactory.getInstance()
													.createElement("terminalLabel");
											eTerminal.setText(isobaric[i]);
											eIsoInfo.add(eTerminal);

											elementMap.put(key, eIsoInfo);
											addCorFactor = true;
										}
									} else {
										key = isobaric[i];

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
											eTerminal.setText(isobaric[i]);
											eIsoInfo.add(eTerminal);

											elementMap.put(isobaric[i], eIsoInfo);
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
							par.setIsobaricLabelCount(keys.length);
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
					} else if (ePg.getName().equals("lfqMinRatioCount")) {
						ePg.setText(String.valueOf(lfqMinRatioCount));
					} else if (ePg.getName().equals("lfqMode")) {
						if (quanMode.equals(MetaConstants.labelFree) && rawFiles[0].length > 1) {
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
							eFa.setText(getDatabaseIdentifier(fasta));
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
					eFm.setText(fixMods[i]);
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
					eVm.setText(variMods[i]);
					element.add(eVm);
				}
			} else if (name.equals("peptideFdr")) {
				element.setText(String.valueOf(psmFdr));
			} else if (name.equals("proteinFdr")) {
				element.setText(String.valueOf(proFdr));
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

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(parOutput), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + parOutput, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static String getDatabaseIdentifier(String db) {

		String[] identifiers = new String[4];
		identifiers[0] = ">.*\\|(.*)\\|";
		identifiers[1] = ">(gi\\|[0-9]*)";
		identifiers[2] = ">([^ ]*)";
		identifiers[3] = ">([^\\t]*)";

		Pattern[] patterns = new Pattern[identifiers.length];
		HashSet<String>[] sets = new HashSet[identifiers.length];

		for (int i = 0; i < sets.length; i++) {
			patterns[i] = Pattern.compile(identifiers[i]);
			sets[i] = new HashSet<String>();
		}

		int procount = 0;
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(db));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					procount++;
					for (int i = 0; i < patterns.length; i++) {
						Matcher matcher = patterns[i].matcher(line);
						if (matcher.find()) {
							sets[i].add(matcher.group());
						}
					}
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < sets.length; i++) {
			if (sets[i].size() == procount) {
				return identifiers[i];
			}
		}
		return ">(.*)";
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static void config(String[] variMods, String[] fixMods, String[] enzyme, String[][] labels,
			String[] isobaric, String fasta, String[][] rawFiles) {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles[0]) {
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

				for (String fn : rawFiles[1]) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText("0");
					element.add(eFra);
				}
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
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

						for (int i = 0; i < enzyme.length; i++) {
							Element eEn = DocumentFactory.getInstance().createElement("string");
							eEn.setText(enzyme[i]);
							ePg.add(eEn);
						}
					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i]);
							ePg.add(eVm);
						}
					} else if (ePg.getName().equals("labelMods")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}

						for (int i = 0; i < labels.length; i++) {
							if (labels[i].length > 0) {
								StringBuilder sb = new StringBuilder();
								for (int j = 0; j < labels[i].length; j++) {
									sb.append(labels[i][j]).append(";");
								}
								sb.deleteCharAt(sb.length() - 1);
								Element eLb = DocumentFactory.getInstance().createElement("string");
								eLb.setText(sb.toString());
								ePg.add(eLb);
							}
						}
					} else if (ePg.getName().equals("isobaricLabels")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}
						for (int i = 0; i < isobaric.length; i++) {
							if (isobaric[i].length() > 0) {
								Element eIso = DocumentFactory.getInstance().createElement("string");
								eIso.setText(isobaric[i]);
								ePg.add(eIso);
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
							eFa.setText(eFa.getText());
						} else if (eFa.getName().equals("descriptionParseRule")) {
							eFa.setText(eFa.getText());
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
					eFm.setText(fixMods[i]);
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
					eVm.setText(variMods[i]);
					element.add(eVm);
				}
			}
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private static void config(String[] variMods, String[] fixMods, String[] enzyme, String[][] labels,
			String[] isobaric, String[][] rawFiles) {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("filePaths")) {
				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : rawFiles[0]) {
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

				for (String fn : rawFiles[1]) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element eFra = DocumentFactory.getInstance().createElement("short");
					eFra.setText("0");
					element.add(eFra);
				}
			} else if (name.equals("ptms")) {
				Iterator<Element> pgIt = element.elementIterator();
				while (pgIt.hasNext()) {
					Element ePg = pgIt.next();
					element.remove(ePg);
				}

				for (int i = 0; i < rawFiles[0].length; i++) {
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

				for (int i = 0; i < rawFiles[0].length; i++) {
					Element ePg = DocumentFactory.getInstance().createElement("int");
					ePg.setText("0");
					element.add(ePg);
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

						for (int i = 0; i < enzyme.length; i++) {
							Element eEn = DocumentFactory.getInstance().createElement("string");
							eEn.setText(enzyme[i]);
							ePg.add(eEn);
						}
					} else if (ePg.getName().equals("variableModifications")) {
						Iterator<Element> vmIt = ePg.elementIterator();
						while (vmIt.hasNext()) {
							Element eVm = vmIt.next();
							ePg.remove(eVm);
						}

						for (int i = 0; i < variMods.length; i++) {
							Element eVm = DocumentFactory.getInstance().createElement("string");
							eVm.setText(variMods[i]);
							ePg.add(eVm);
						}
					} else if (ePg.getName().equals("labelMods")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}

						for (int i = 0; i < labels.length; i++) {
							if (labels[i].length > 0) {
								StringBuilder sb = new StringBuilder();
								for (int j = 0; j < labels[i].length; j++) {
									sb.append(labels[i][j]).append(";");
								}
								sb.deleteCharAt(sb.length() - 1);
								Element eLb = DocumentFactory.getInstance().createElement("string");
								eLb.setText(sb.toString());
								ePg.add(eLb);
							}
						}
					} else if (ePg.getName().equals("isobaricLabels")) {
						Iterator<Element> lbIt = ePg.elementIterator();
						while (lbIt.hasNext()) {
							Element eLb = lbIt.next();
							ePg.remove(eLb);
						}
						for (int i = 0; i < isobaric.length; i++) {
							if (isobaric[i].length() > 0) {
								Element eIso = DocumentFactory.getInstance().createElement("string");
								eIso.setText(isobaric[i]);
								ePg.add(eIso);
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
					eFm.setText(fixMods[i]);
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
					eVm.setText(variMods[i]);
					element.add(eVm);
				}
			}
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void addDatabase(String fasta) {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("fastaFiles")) {
				Iterator<Element> faIt = element.elementIterator();
				while (faIt.hasNext()) {
					Element eFa = faIt.next();
					element.remove(eFa);
				}

				Element eFa = DocumentFactory.getInstance().createElement("string");
				eFa.setText(fasta);
				element.add(eFa);
			}
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void addFirstSearchDB(String fasta) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(path);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MaxQuant parameter in " + path, e);
			return;
		}
		Element root = document.getRootElement();

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("fastaFilesFirstSearch")) {
				Iterator<Element> faIt = element.elementIterator();
				while (faIt.hasNext()) {
					Element eFa = faIt.next();
					element.remove(eFa);
				}

				Element eFa = DocumentFactory.getInstance().createElement("string");
				eFa.setText(fasta);
				element.add(eFa);
			}
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(path), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + path, e);
		}
	}

}
