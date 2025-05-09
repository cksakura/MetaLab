/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

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
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzyme;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.RegionTopNIntensityFilter;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MgfReader;
import bmi.med.uOttawa.metalab.spectra.io.MgfWriter;
import bmi.med.uOttawa.metalab.spectra.io.ProteomicsTools;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.AbstractParameter;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class XTandemTask {

	private String XTandemCmd = "Resources//tandem//bin//tandem.exe";
	private Appendable log;
	private XTandemParameterHandler tandemParHandler;
	private ProteomicsTools pt;

	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private static Logger LOGGER = LogManager.getLogger(XTandemTask.class);

	public XTandemTask(AbstractParameter parameter, Appendable log) {
		this.log = log;
		this.XTandemCmd = parameter.getXTandemCmd();
		this.tandemParHandler = new XTandemParameterHandler(parameter);
		this.pt = new ProteomicsTools(parameter);
	}

	public XTandemTask(AbstractParameter parameter, XTandemParameterHandler tandemParHandler, Appendable log) {
		this.log = log;
		this.XTandemCmd = parameter.getXTandemCmd();
		this.tandemParHandler = tandemParHandler;
		this.pt = new ProteomicsTools(parameter);
	}

	public void run(String raw) {
		run(new File(raw));
	}

	public void run(File raw) {

		File mgf = null;
		String rawName = raw.getName();
		if (rawName.endsWith("raw") || rawName.endsWith("RAW")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				mgf = pt.raw2Mgf(raw);
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mzXML") || rawName.endsWith("MZXML")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				MgfWriter writer = new MgfWriter(mgf);
//				writer.wiffMzxml2Mgf4Mascot(raw.getAbsolutePath());
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("wiff") || rawName.endsWith("WIFF")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {

				File mzxml = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mzXML");
				if (mzxml.exists()) {
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					MgfWriter writer = new MgfWriter(mgf);
//					writer.wiffMzxml2Mgf4Mascot(mzxml.getAbsolutePath());
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
				} else {
					try {
						log.append(format.format(new Date()) + "\t"
								+ "Cann't convert wiff to mgf directly, mzXML file is needed\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					return;
				}

			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mgf") || rawName.endsWith("MGF")) {
			mgf = new File(raw.getAbsolutePath());
		}

		File input = tandemParHandler.setSpectrum(mgf.getAbsolutePath());

		LOGGER.info("Database searching for " + mgf + " by X!Tandem");
		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(XTandemCmd + " " + input);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by X!Tandem", e);
		}

		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public void run(String raw, File input) {
		run(new File(raw), input);
	}

	public void run(File raw, File input) {

		File mgf = null;
		String rawName = raw.getName();
		if (rawName.endsWith("raw") || rawName.endsWith("RAW")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				mgf = pt.raw2Mgf(raw);
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mzXML") || rawName.endsWith("MZXML")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				MgfWriter writer = new MgfWriter(mgf);
//				writer.wiffMzxml2Mgf4Mascot(raw.getAbsolutePath());
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("wiff") || rawName.endsWith("WIFF")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {

				File mzxml = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mzXML");
				if (mzxml.exists()) {
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					MgfWriter writer = new MgfWriter(mgf);
//					writer.wiffMzxml2Mgf4Mascot(mzxml.getAbsolutePath());
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
				} else {
					try {
						log.append(format.format(new Date()) + "\t"
								+ "Cann't convert wiff to mgf directly, mzXML file is needed\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					return;
				}

			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mgf") || rawName.endsWith("MGF")) {
			mgf = new File(raw.getAbsolutePath());
		}

		tandemParHandler.setSpectrum(mgf.getAbsolutePath(), input);

		LOGGER.info("Database searching for " + mgf + " by X!Tandem");
		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(XTandemCmd + " " + input);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by X!Tandem", e);
		}

		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public void run(File raw, RegionTopNIntensityFilter filter) {

		File mgf = null;
		String rawName = raw.getName();

		if (rawName.endsWith("raw") || rawName.endsWith("RAW")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				mgf = pt.raw2Mgf(raw);
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mzXML") || rawName.endsWith("MZXML")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
				MgfWriter writer = new MgfWriter(mgf);
//				writer.wiffMzxml2Mgf4Mascot(raw.getAbsolutePath());
				try {
					log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("wiff") || rawName.endsWith("WIFF")) {
			mgf = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mgf");
			if (!mgf.exists()) {

				File mzxml = new File(raw.getParentFile(), rawName.substring(0, rawName.lastIndexOf(".")) + ".mzXML");
				if (mzxml.exists()) {
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " started" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					MgfWriter writer = new MgfWriter(mgf);
//					writer.wiffMzxml2Mgf4Mascot(mzxml.getAbsolutePath());
					try {
						log.append(format.format(new Date()) + "\t" + "mgf convert for " + raw + " finished" + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
				} else {
					try {
						log.append(format.format(new Date()) + "\t"
								+ "Cann't convert wiff to mgf directly, mzXML file is needed\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file", e);
					}
					return;
				}

			} else {
				try {
					log.append(format.format(new Date()) + "\t" + "mgf for " + raw + " was found" + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing log file", e);
				}
			}
		} else if (rawName.endsWith("mgf") || rawName.endsWith("MGF")) {
			mgf = raw;
		}

		File filteredMgf = new File(mgf.getParent() + "\\filtered." + mgf.getName());

		MgfReader reader = new MgfReader(mgf);
		MgfWriter writer = new MgfWriter(filteredMgf);
		Spectrum sp = null;
		while ((sp = reader.getNextSpectrum()) != null) {
			Peak[] peaks = sp.getPeaks();
			sp.setPeaks(filter.filter(peaks));
			writer.write(sp);
		}
		reader.close();
		writer.close();

		mgf.delete();
		filteredMgf.renameTo(mgf);

		File input = tandemParHandler.setSpectrum(mgf.getAbsolutePath());

		LOGGER.info("Database searching for " + mgf + " by X!Tandem");
		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(XTandemCmd + " " + input);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by X!Tandem", e);
		}

		try {
			log.append(format.format(new Date()) + "\t" + "X!Tandem searching for " + mgf + " finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void run(MetaParameterMQ metaPar, MetaSourcesV2 advPar, File resultDir, File[] mgfs) {

		MaxquantModification[] variMods = metaPar.getVariMods();
		MaxquantModification[] fixMods = metaPar.getFixMods();
		Enzyme enzyme = metaPar.getEnzyme();
		int missCleavage = metaPar.getMissCleavages();
		int digestMode = metaPar.getDigestMode();
		String quanMode = metaPar.getQuanMode();

		MaxquantModification[][] labels = metaPar.getLabels();
		MaxquantModification[] isobaric = metaPar.getIsobaric();
		String currentDatabase = metaPar.getCurrentDb();

		int thread = metaPar.getThreadCount();
		String ms2ScanMode = metaPar.getMs2ScanMode();

		File xtandemFile = new File(advPar.getXtandem());
		File tandemInputFile = new File(xtandemFile.getParent(), "input.xml");
		File tandemDefaultFile = new File(xtandemFile.getParent(), "default_input.xml");
		File tandemTaxaFile = new File(xtandemFile.getParent(), "taxonomy.xml");

		File resultParaFile = new File(resultDir, "parameter");
		if (!resultParaFile.exists()) {
			resultParaFile.mkdir();
		}

		File copyDefaultFile = new File(resultParaFile, "default_input.xml");
		File copyTaxFile = new File(resultParaFile, "taxonomy.xml");

		SAXReader inputReader = new SAXReader();
		Document inputDocument = null;
		try {
			inputDocument = inputReader.read(tandemInputFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + tandemInputFile.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in reading X!Tandem parameter in "
					+ tandemInputFile.getName());
		}
		Element inputRoot = inputDocument.getRootElement();

		Iterator<Element> inputIt = inputRoot.elementIterator();
		while (inputIt.hasNext()) {
			Element inputElement = inputIt.next();
			String label = inputElement.attributeValue("label");
			if (label != null) {
				if (label.equals("protein, taxon")) {

					String name = (new File(currentDatabase)).getName();
					name = name.substring(0, name.length() - 6);

					inputElement.setText(name);

					SAXReader taxReader = new SAXReader();
					Document taxDocument = null;
					try {
						taxDocument = taxReader.read(tandemTaxaFile);
					} catch (DocumentException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in adding database information to X!Tandem parameter in "
								+ tandemTaxaFile.getName(), e);
						System.err.println(format.format(new Date()) + "\t"
								+ "Error in adding database information to X!Tandem parameter in "
								+ tandemTaxaFile.getName());
					}
					Element taxRoot = taxDocument.getRootElement();

					boolean found = false;

					Iterator<Element> it = taxRoot.elementIterator();
					while (it.hasNext()) {
						Element taxElement = it.next();
						String taxon = taxElement.attributeValue("label");
						if (taxon.equals(name)) {

							Iterator<Element> fileIt = taxElement.elementIterator();
							while (fileIt.hasNext()) {
								Element eFile = fileIt.next();
								taxElement.remove(eFile);
							}

							Element file = DocumentFactory.getInstance().createElement("file");
							file.addAttribute("format", "peptide");
							file.addAttribute("URL", currentDatabase);

							taxElement.add(file);

							found = true;
						}
					}

					if (!found) {
						Element taxon = DocumentFactory.getInstance().createElement("taxon");
						taxon.addAttribute("label", name);

						Element file = DocumentFactory.getInstance().createElement("file");
						file.addAttribute("format", "peptide");
						file.addAttribute("URL", currentDatabase);

						taxon.add(file);
						taxRoot.add(taxon);
					}

					OutputStreamWriter bufferedWriter;
					try {
						bufferedWriter = new OutputStreamWriter(new FileOutputStream(copyTaxFile), "UTF8");
						XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
						writer.write(taxDocument);
						writer.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing X!Tandem parameter to " + copyTaxFile.getName(), e);
						System.err.println(format.format(new Date()) + "\t" + "Error in writing X!Tandem parameter to "
								+ copyTaxFile.getName());
					}
				}
			}
		}

		OutputStreamWriter inputWriter;
		try {
			inputWriter = new OutputStreamWriter(new FileOutputStream(tandemInputFile), "UTF8");
			XMLWriter writer = new XMLWriter(inputWriter, OutputFormat.createPrettyPrint());
			writer.write(inputDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + tandemInputFile.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing X!Tandem parameter to "
					+ tandemInputFile.getName());
		}

		SAXReader defaultReader = new SAXReader();
		Document defaultDocument = null;
		try {
			defaultDocument = defaultReader.read(tandemDefaultFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + tandemDefaultFile.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in reading X!Tandem parameter in "
					+ tandemDefaultFile.getName());
		}
		Element defaultRoot = defaultDocument.getRootElement();

		Iterator<Element> defaultIt = defaultRoot.elementIterator();
		while (defaultIt.hasNext()) {
			Element element = defaultIt.next();
			int acount = element.attributeCount();
			if (acount == 2) {
				String type = element.attributeValue("type");
				String label = element.attributeValue("label");

				if (type.equals("input")) {
					if (label.equals("residue, modification mass")) {

						if (quanMode.equals(MetaConstants.isobaricLabel)) {

							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < fixMods.length; i++) {
								sb.append(fixMods[i].getTandemFormat()).append(",");
							}

							if (isobaric.length > 0) {
								HashSet<String> siteSet = new HashSet<String>();
								for (int i = 0; i < isobaric.length; i++) {
									String tandemFormat = isobaric[i].getTandemFormat();
									String site = tandemFormat.substring(tandemFormat.indexOf("@") + 1);
									if (!siteSet.contains(site)) {
										siteSet.add(site);
										sb.append(isobaric[i].getTandemFormat()).append(",");
									}
								}
							}

							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						} else {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < fixMods.length; i++) {
								sb.append(fixMods[i].getTandemFormat()).append(",");
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						}

					} else if (label.equals("residue, potential modification mass")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < variMods.length; i++) {
								sb.append(variMods[i].getTandemFormat()).append(",");
							}
							for (int i = 0; i < labels.length; i++) {
								for (int j = 0; j < labels[i].length; j++) {
									sb.append(labels[i][j].getTandemFormat()).append(",");
								}
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						} else {

							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < variMods.length; i++) {
								sb.append(variMods[i].getTandemFormat()).append(",");
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						}
					} else if (label.equals("protein, cleavage site")) {
						if (enzyme == null) {
							element.setText("");
						} else {
							element.setText(enzyme.getTandemFormat());
						}
					} else if (label.equals("scoring, maximum missed cleavage sites")) {
						element.setText(String.valueOf(missCleavage));
					} else if (label.equals("protein, cleavage semi")) {
						if (digestMode == MaxquantEnzyme.semi_specific || digestMode == MaxquantEnzyme.semi_Nterm
								|| digestMode == MaxquantEnzyme.semi_Cterm) {
							element.setText("Yes");
						} else {
							element.setText("No");
						}
					} else if (label.equals("spectrum, threads")) {
						element.setText(String.valueOf(thread));
					} else if (label.equals("spectrum, fragment monoisotopic mass error")) {
						if (ms2ScanMode.equals(MetaConstants.FTMS)) {
							element.setText("20");
						} else if (ms2ScanMode.equals(MetaConstants.ITMS)) {
							element.setText("0.5");
						}
					} else if (label.equals("spectrum, fragment monoisotopic mass error units")) {
						if (ms2ScanMode.equals(MetaConstants.FTMS)) {
							element.setText("ppm");
						} else if (ms2ScanMode.equals(MetaConstants.ITMS)) {
							element.setText("Daltons");
						}
					} else if (label.equals("spectrum, threads")) {
						element.setText(String.valueOf(thread));
					}
				}
			}
		}

		OutputStreamWriter defaultWriter;
		try {
			defaultWriter = new OutputStreamWriter(new FileOutputStream(copyDefaultFile), "UTF8");
			XMLWriter writer = new XMLWriter(defaultWriter, OutputFormat.createPrettyPrint());
			writer.write(defaultDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + copyDefaultFile.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing X!Tandem parameter to "
					+ copyDefaultFile.getName());
		}

		for (int i = 0; i < mgfs.length; i++) {

			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(tandemInputFile);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading X!Tandem parameter in " + tandemInputFile.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + "Error in reading X!Tandem parameter in "
						+ tandemInputFile.getName());
			}
			Element root = document.getRootElement();

			String mgfName = mgfs[i].getName();
			File xmlResult = new File(resultDir, mgfName.substring(0, mgfName.lastIndexOf(".")) + ".xml");

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element element = it.next();
				String label = element.attributeValue("label");
				if (label != null) {
					if (label.equals("spectrum, path")) {
						element.setText(mgfs[i].getAbsolutePath());
					}
					if (label.equals("output, path")) {
						element.setText(xmlResult.getAbsolutePath());
					}
					if (label.equals("list path, default parameters")) {
						element.setText(copyDefaultFile.getAbsolutePath());
					}
					if (label.equals("list path, taxonomy information")) {
						element.setText(copyTaxFile.getAbsolutePath());
					}
				}
			}

			File copyInputFile = new File(resultParaFile, "input_" + (i + 1) + ".xml");
			OutputStreamWriter bufferedWriter;
			try {
				bufferedWriter = new OutputStreamWriter(new FileOutputStream(copyInputFile), "UTF8");
				XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
				writer.write(document);
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in writing X!Tandem parameter to " + copyInputFile.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + "Error in writing X!Tandem parameter to "
						+ copyInputFile.getName());
			}

			Runtime run = Runtime.getRuntime();
			try {
				Process p = run.exec(advPar.getXtandem() + " " + copyInputFile);
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null)
					System.out.println(format.format(new Date()) + "\t" + lineStr);
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				LOGGER.error("Error in database searching by X!Tandem", e);
				System.err.println(format.format(new Date()) + "\t" + "Error in database searching by X!Tandem");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void run(MetaParameterMQ metaPar, MetaSourcesV2 advPar, File resultDir, File mgf) {

		MaxquantModification[] variMods = metaPar.getVariMods();
		MaxquantModification[] fixMods = metaPar.getFixMods();
		Enzyme enzyme = metaPar.getEnzyme();
		int digestMode = metaPar.getDigestMode();
		String quanMode = metaPar.getQuanMode();

		MaxquantModification[][] labels = metaPar.getLabels();
		MaxquantModification[] isobaric = metaPar.getIsobaric();
		String currentDatabase = metaPar.getCurrentDb();

		int thread = metaPar.getThreadCount();
		String ms2ScanMode = metaPar.getMs2ScanMode();

		File xtandemFile = new File(advPar.getXtandem());
		File tandemInputFile = new File(xtandemFile.getParent(), "input.xml");
		File tandemDefaultFile = new File(xtandemFile.getParent(), "default_input.xml");
		File tandemTaxaFile = new File(xtandemFile.getParent(), "taxonomy.xml");

		File resultParaFile = new File(resultDir, "parameter");
		if (!resultParaFile.exists()) {
			resultParaFile.mkdir();
		}

		File copyDefaultFile = new File(resultParaFile, "default_input.xml");
		File copyTaxFile = new File(resultParaFile, "taxonomy.xml");

		SAXReader inputReader = new SAXReader();
		Document inputDocument = null;
		try {
			inputDocument = inputReader.read(tandemInputFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + tandemInputFile, e);
		}
		Element inputRoot = inputDocument.getRootElement();

		Iterator<Element> inputIt = inputRoot.elementIterator();
		while (inputIt.hasNext()) {
			Element inputElement = inputIt.next();
			String label = inputElement.attributeValue("label");
			if (label != null) {
				if (label.equals("protein, taxon")) {

					String name = (new File(currentDatabase)).getName();
					name = name.substring(0, name.length() - 6);

					inputElement.setText(name);

					SAXReader taxReader = new SAXReader();
					Document taxDocument = null;
					try {
						taxDocument = taxReader.read(tandemTaxaFile);
					} catch (DocumentException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in adding database information to X!Tandem parameter in " + tandemTaxaFile,
								e);
					}
					Element taxRoot = taxDocument.getRootElement();

					boolean found = false;

					Iterator<Element> it = taxRoot.elementIterator();
					while (it.hasNext()) {
						Element taxElement = it.next();
						String taxon = taxElement.attributeValue("label");
						if (taxon.equals(name)) {

							Iterator<Element> fileIt = taxElement.elementIterator();
							while (fileIt.hasNext()) {
								Element eFile = fileIt.next();
								taxElement.remove(eFile);
							}

							Element file = DocumentFactory.getInstance().createElement("file");
							file.addAttribute("format", "peptide");
							file.addAttribute("URL", currentDatabase);

							taxElement.add(file);

							found = true;
						}
					}

					if (!found) {
						Element taxon = DocumentFactory.getInstance().createElement("taxon");
						taxon.addAttribute("label", name);

						Element file = DocumentFactory.getInstance().createElement("file");
						file.addAttribute("format", "peptide");
						file.addAttribute("URL", currentDatabase);

						taxon.add(file);
						taxRoot.add(taxon);
					}

					OutputStreamWriter bufferedWriter;
					try {
						bufferedWriter = new OutputStreamWriter(new FileOutputStream(copyTaxFile), "UTF8");
						XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
						writer.write(taxDocument);
						writer.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing X!Tandem parameter to " + copyTaxFile, e);
					}
				}
			}
		}

		OutputStreamWriter inputWriter;
		try {
			inputWriter = new OutputStreamWriter(new FileOutputStream(tandemInputFile), "UTF8");
			XMLWriter writer = new XMLWriter(inputWriter, OutputFormat.createPrettyPrint());
			writer.write(inputDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + tandemInputFile, e);
		}

		SAXReader defaultReader = new SAXReader();
		Document defaultDocument = null;
		try {
			defaultDocument = defaultReader.read(tandemDefaultFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + tandemDefaultFile, e);
		}
		Element defaultRoot = defaultDocument.getRootElement();

		Iterator<Element> defaultIt = defaultRoot.elementIterator();
		while (defaultIt.hasNext()) {
			Element element = defaultIt.next();
			int acount = element.attributeCount();
			if (acount == 2) {
				String type = element.attributeValue("type");
				String label = element.attributeValue("label");

				if (type.equals("input")) {
					if (label.equals("residue, modification mass")) {

						if (quanMode.equals(MetaConstants.isobaricLabel)) {

							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < fixMods.length; i++) {
								sb.append(fixMods[i].getTandemFormat()).append(",");
							}
							for (int i = 0; i < isobaric.length; i++) {
								sb.append(isobaric[i].getTandemFormat()).append(",");
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						} else {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < fixMods.length; i++) {
								sb.append(fixMods[i].getTandemFormat()).append(",");
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						}

					} else if (label.equals("residue, potential modification mass")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < variMods.length; i++) {
								sb.append(variMods[i].getTandemFormat()).append(",");
							}
							for (int i = 0; i < labels.length; i++) {
								for (int j = 0; j < labels[i].length; j++) {
									sb.append(labels[i][j].getTandemFormat()).append(",");
								}
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());
						} else {

							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < variMods.length; i++) {
								sb.append(variMods[i].getTandemFormat()).append(",");
							}
							if (sb.length() > 0) {
								sb.deleteCharAt(sb.length() - 1);
							}
							element.setText(sb.toString());

						}
					} else if (label.equals("protein, cleavage site")) {
						if (enzyme == null) {
							element.setText("");
						} else {
							element.setText(enzyme.getTandemFormat());
						}
					} else if (label.equals("protein, cleavage semi")) {
						if (digestMode == MaxquantEnzyme.semi_specific || digestMode == MaxquantEnzyme.semi_Nterm
								|| digestMode == MaxquantEnzyme.semi_Cterm) {
							element.setText("Yes");
						} else {
							element.setText("No");
						}
					} else if (label.equals("spectrum, threads")) {
						element.setText(String.valueOf(thread));
					} else if (label.equals("spectrum, fragment monoisotopic mass error")) {
						if (ms2ScanMode.equals(MetaConstants.FTMS)) {
							element.setText("20");
						} else if (ms2ScanMode.equals(MetaConstants.ITMS)) {
							element.setText("0.5");
						}
					} else if (label.equals("spectrum, fragment monoisotopic mass error units")) {
						if (ms2ScanMode.equals(MetaConstants.FTMS)) {
							element.setText("ppm");
						} else if (ms2ScanMode.equals(MetaConstants.ITMS)) {
							element.setText("Daltons");
						}
					} else if (label.equals("spectrum, threads")) {
						element.setText(String.valueOf(thread));
					}
				}
			}
		}

		OutputStreamWriter defaultWriter;
		try {
			defaultWriter = new OutputStreamWriter(new FileOutputStream(copyDefaultFile), "UTF8");
			XMLWriter writer = new XMLWriter(defaultWriter, OutputFormat.createPrettyPrint());
			writer.write(defaultDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + copyDefaultFile, e);
		}

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(tandemInputFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + tandemInputFile, e);
		}
		Element root = document.getRootElement();

		String mgfName = mgf.getName();
		File xmlResult = new File(resultDir, mgfName.substring(0, mgfName.lastIndexOf(".")) + ".xml");

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String label = element.attributeValue("label");
			if (label != null) {
				if (label.equals("spectrum, path")) {
					element.setText(mgf.getAbsolutePath());
				}
				if (label.equals("output, path")) {
					element.setText(xmlResult.getAbsolutePath());
				}
				if (label.equals("list path, default parameters")) {
					element.setText(copyDefaultFile.getAbsolutePath());
				}
				if (label.equals("list path, taxonomy information")) {
					element.setText(copyTaxFile.getAbsolutePath());
				}
			}
		}

		File copyInputFile = new File(resultParaFile, "input.xml");
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(copyInputFile), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + copyInputFile, e);
		}

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(advPar.getXtandem() + " " + copyInputFile);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by X!Tandem", e);
		}
	}
}
