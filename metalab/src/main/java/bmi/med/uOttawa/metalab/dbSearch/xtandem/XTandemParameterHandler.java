/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzymeHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.AbstractParameter;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class XTandemParameterHandler {

	private MaxquantModHandler modHandler;
	private MaxquantEnzymeHandler enzymeHandler;
	private File preProcessParFile;
	private File tandemInputFile;
	private File tandemTaxaFile;
	private File tandemDefaultFile;

	private static final int high_high = 0;
	private static final int high_low = 1;

	private static Logger LOGGER = LogManager.getLogger();

	public XTandemParameterHandler(AbstractParameter parameter) {

		File paraDir = parameter.getParameterDir();
		this.preProcessParFile = new File(paraDir, "pre_processing");
		if (!preProcessParFile.exists()) {
			preProcessParFile.mkdirs();
		}

		tandemInputFile = new File(preProcessParFile, "input.xml");
		Path sourceInput = Paths.get(parameter.getXTandemInput());
		Path targetInput = Paths.get(tandemInputFile.getAbsolutePath());

		try {
			Files.copy(sourceInput, targetInput, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying parameter file from " + sourceInput + " to " + targetInput, e);
		}

		tandemTaxaFile = new File(preProcessParFile, "taxonomy.xml");
		Path sourceTaxa = Paths.get(parameter.getXTandemTaxonomy());
		Path targetTaxa = Paths.get(tandemTaxaFile.getAbsolutePath());

		try {
			Files.copy(sourceTaxa, targetTaxa, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying parameter file from " + sourceTaxa + " to " + targetTaxa, e);
		}

		tandemDefaultFile = new File(preProcessParFile, "default_input.xml");
		Path sourceDefault = Paths.get(parameter.getXTandemDefaultInput());
		Path targetDefault = Paths.get(tandemDefaultFile.getAbsolutePath());

		try {
			Files.copy(sourceDefault, targetDefault, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying parameter file from " + sourceDefault + " to " + targetDefault, e);
		}

		if (parameter instanceof MetaParameterV1) {
			MetaParameterV1 metaPara = (MetaParameterV1) parameter;
			this.modHandler = new MaxquantModHandler(metaPara.getMaxQuantMod());
			this.enzymeHandler = new MaxquantEnzymeHandler(metaPara.getMaxQuantEnzyme());
		}
	}

	@SuppressWarnings("unchecked")
	public void config(MetaParameterV1 parameter) {

		String[] variMods = parameter.getVariMods();
		String[] fixMods = parameter.getFixMods();
		String[] enzyme = parameter.getEnzymes();
		int digestMode = parameter.getDigestMode();
		String quanMode = parameter.getQuanMode();

		String[][] labels = parameter.getLabels();
		String[] isobaric = parameter.getIsobaric();
		String fasta = parameter.getDatabase();
		int thread = parameter.getThreadCount();
		int instruType = parameter.getInstruType();

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
			Element element = inputIt.next();
			String label = element.attributeValue("label");
			if (label != null) {
				if (label.equals("protein, taxon")) {
					String name = (new File(fasta)).getName();
					name = name.substring(0, name.length() - 6);
					addFasta(name, fasta, tandemTaxaFile.getAbsolutePath());
					element.setText(name);
				} else if (label.equals("list path, default parameters")) {
					element.setText(tandemDefaultFile.getAbsolutePath());
				} else if (label.equals("list path, taxonomy information")) {
					element.setText(tandemTaxaFile.getAbsolutePath());
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
							element.setText(modHandler.getMod4Tandem(fixMods, isobaric));
						} else {
							element.setText(modHandler.getMod4Tandem(fixMods));
						}
					} else if (label.equals("residue, potential modification mass")) {
						if (quanMode.equals(MetaConstants.isotopicLabel)) {
							element.setText(modHandler.getMod4Tandem(variMods, labels));
						} else {
							element.setText(modHandler.getMod4Tandem(variMods));
						}
					} else if (label.equals("protein, cleavage site")) {
						if (enzyme.length == 0) {
							element.setText("");
						} else {
							element.setText(enzymeHandler.getEnzyme4Tandem(enzyme[0]));
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
						if (instruType == high_high) {
							element.setText("20");
						} else if (instruType == high_low) {
							element.setText("0.5");
						}
					} else if (label.equals("spectrum, fragment monoisotopic mass error units")) {
						if (instruType == high_high) {
							element.setText("ppm");
						} else if (instruType == high_low) {
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
			defaultWriter = new OutputStreamWriter(new FileOutputStream(tandemDefaultFile), "UTF8");
			XMLWriter writer = new XMLWriter(defaultWriter, OutputFormat.createPrettyPrint());
			writer.write(defaultDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + tandemDefaultFile, e);
		}
	}

	public File setSpectrum(String mgf) {

		String filename = (new File(mgf)).getName();
		filename = filename.substring(0, filename.length() - 3) + "_input.xml";
		File input = new File(this.preProcessParFile, filename);

		return setSpectrum(mgf, input);
	}

	@SuppressWarnings("unchecked")
	public File setSpectrum(String mgf, File inputPath) {

		if (!inputPath.exists()) {

			Path sourceInput = Paths.get(tandemInputFile.getAbsolutePath());
			Path targetInput = Paths.get(inputPath.getAbsolutePath());

			try {
				Files.copy(sourceInput, targetInput, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in copying parameter file from " + sourceInput + " to " + targetInput, e);
			}
		}

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(inputPath);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + inputPath, e);
		}
		Element root = document.getRootElement();

		String output = mgf.substring(0, mgf.lastIndexOf(".")) + ".xml";
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String label = element.attributeValue("label");
			if (label != null) {
				if (label.equals("spectrum, path")) {
					element.setText(mgf);
				}
				if (label.equals("output, path")) {
					element.setText(output);
				}
			}
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(inputPath), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + inputPath, e);
		}

		return inputPath;
	}

	@SuppressWarnings("unchecked")
	private static void addFasta(String name, String path, String taxonomyPath) {

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(taxonomyPath);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in adding database information to X!Tandem parameter in " + taxonomyPath, e);
		}
		Element root = document.getRootElement();

		boolean found = false;

		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String taxon = element.attributeValue("label");
			if (taxon.equals(name)) {

				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				Element file = DocumentFactory.getInstance().createElement("file");
				file.addAttribute("format", "peptide");
				file.addAttribute("URL", path);

				element.add(file);

				found = true;
			}
		}

		if (!found) {
			Element taxon = DocumentFactory.getInstance().createElement("taxon");
			taxon.addAttribute("label", name);

			Element file = DocumentFactory.getInstance().createElement("file");
			file.addAttribute("format", "peptide");
			file.addAttribute("URL", path);

			taxon.add(file);
			root.add(taxon);
		}

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(taxonomyPath), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + taxonomyPath, e);
		}
	}

	@SuppressWarnings("unchecked")
	public void setDB2Input(File input, File tempFasta) {
		// TODO Auto-generated method stub

		if (!input.exists()) {

			Path sourceInput = Paths.get(tandemInputFile.getAbsolutePath());
			Path targetInput = Paths.get(input.getAbsolutePath());

			try {
				Files.copy(sourceInput, targetInput, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in copying parameter file from " + sourceInput + " to " + targetInput, e);
			}
		}

		SAXReader inputReader = new SAXReader();
		Document inputDocument = null;
		try {
			inputDocument = inputReader.read(input);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem parameter in " + input, e);
		}
		Element inputRoot = inputDocument.getRootElement();

		Iterator<Element> inputIt = inputRoot.elementIterator();
		while (inputIt.hasNext()) {
			Element element = inputIt.next();
			String label = element.attributeValue("label");
			if (label != null) {
				if (label.equals("protein, taxon")) {
					String name = tempFasta.getName();
					name = name.substring(0, name.length() - 6);
					addFasta(name, tempFasta.getAbsolutePath(), tandemTaxaFile.getAbsolutePath());
					element.setText(name);
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
	}

	public String getDefaultPath() {
		return this.tandemDefaultFile.getAbsolutePath();
	}

	public String getInputPath() {
		return this.tandemInputFile.getAbsolutePath();
	}

	public String getTaxonomyPath() {
		return this.tandemTaxaFile.getAbsolutePath();
	}

	public File getPreProcessParFile() {
		return preProcessParFile;
	}

	@SuppressWarnings("unchecked")
	public static void config(MetaParameterMQ metaPar, MetaSourcesV2 advPar) {

		MaxquantModification[] variMods = metaPar.getVariMods();
		MaxquantModification[] fixMods = metaPar.getFixMods();
		Enzyme enzyme = metaPar.getEnzyme();
		int digestMode = metaPar.getDigestMode();
		String quanMode = metaPar.getQuanMode();

		MaxquantModification[][] labels = metaPar.getLabels();
		MaxquantModification[] isobaric = metaPar.getIsobaric();
		String fasta = metaPar.getCurrentDb();

		int thread = metaPar.getThreadCount();
		String ms2ScanMode = metaPar.getMs2ScanMode();

		File xtandemFile = new File(advPar.getXtandem());
		File tandemInputFile = new File(xtandemFile.getParent(), "input.xml");
		File tandemDefaultFile = new File(xtandemFile.getParent(), "default_input.xml");
		File tandemTaxaFile = new File(xtandemFile.getParent(), "taxonomy.xml");

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
			Element element = inputIt.next();
			String label = element.attributeValue("label");
			if (label != null) {
				if (label.equals("protein, taxon")) {
					String name = (new File(fasta)).getName();
					name = name.substring(0, name.length() - 6);
					addFasta(name, fasta, tandemTaxaFile.getAbsolutePath());
					element.setText(name);
				} else if (label.equals("list path, default parameters")) {
					element.setText(tandemDefaultFile.getAbsolutePath());
				} else if (label.equals("list path, taxonomy information")) {
					element.setText(tandemTaxaFile.getAbsolutePath());
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
			defaultWriter = new OutputStreamWriter(new FileOutputStream(tandemDefaultFile), "UTF8");
			XMLWriter writer = new XMLWriter(defaultWriter, OutputFormat.createPrettyPrint());
			writer.write(defaultDocument);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing X!Tandem parameter to " + tandemDefaultFile, e);
		}

	}
}
