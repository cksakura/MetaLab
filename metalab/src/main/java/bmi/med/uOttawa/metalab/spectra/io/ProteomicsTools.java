/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v1.par.AbstractParameter;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesIOV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

/**
 * @author Kai Cheng
 *
 */
public class ProteomicsTools {

	private String convertCmd = "Resources//ProteomicsTools//ProteomicsTools.exe";
//	private String convertMGF = "Resources//ProteomicsTools//config//mgf_convert.param";
//	private String convertMzxml = "Resources//ProteomicsTools//config//mzxml_convert.param";

	private String mgfParString;
	private String mzxmlParString;

	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private static final Logger LOGGER = LogManager.getLogger(ProteomicsTools.class);

	private File spectraDir;
	private File confMgf;
	private File confMzxml;

	private static final int mgfId = 0;
	private static final int mzxmlId = 1;

	private ProteomicsTools(MetaSourcesV2 advPar, File spectraDir) {
		this.convertCmd = advPar.getProteomicsTools();
		File parent = (new File(convertCmd)).getParentFile();
		File config = new File(parent, "config");
		File mgfPara = new File(config, "mgf_convert.param");
		if (!mgfPara.exists()) {
			this.createConf(mgfPara.getAbsolutePath());
		}

		File mzxmlPara = new File(config, "mzxml_convert.param");
		if (!mzxmlPara.exists()) {
			this.createConf(mzxmlPara.getAbsolutePath());
		}
		this.spectraDir = spectraDir;

		try {
			this.initialParString(mgfPara, mzxmlPara);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ProteomicsTools(AbstractParameter parameter) {
		this.convertCmd = parameter.getResource() + "\\ProteomicsTools\\ProteomicsTools.exe";
		try {
			this.initialParString(new File(parameter.getResource() + "\\ProteomicsTools\\config\\mgf_convert.param"),
					new File(parameter.getResource() + "\\ProteomicsTools\\config\\mzxml_convert.param"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		this.convertMGF = parameter.getResource() + "\\ProteomicsTools\\config\\mgf_convert.param";
//		this.convertMzxml = parameter.getResource() + "\\ProteomicsTools\\config\\mzxml_convert.param";
	}

	public ProteomicsTools(MetaParameter metaPar, MetaSourcesV2 advPar) {
		this.convertCmd = advPar.getProteomicsTools();
		File parent = (new File(convertCmd)).getParentFile();
		File config = new File(parent, "config");
		File mgfPara = new File(config, "mgf_convert.param");
		if (!mgfPara.exists()) {
			this.createConf(mgfPara.getAbsolutePath());
		}

		File mzxmlPara = new File(config, "mzxml_convert.param");
		if (!mzxmlPara.exists()) {
			this.createConf(mzxmlPara.getAbsolutePath());
		}
		this.spectraDir = metaPar.getSpectraFile();

		try {
			this.initialParString(mgfPara, mzxmlPara);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in config the parameter file", e);
			System.err.println(format.format(new Date()) + "\t" + "Error in config the parameter file");
		}
	}

	/**
	 * If parsing the .xml parameter file from a string an Exception will be throw because the XML file is in UTF-8-BOM format, a BOM character is
	 * at the start of the file (can't be seen) which is not allowed
	 * Use BOMInputStream can solve this problem
	 * @param mgfParaFile
	 * @param mzxmlParaFile
	 * @throws IOException
	 */
	private void initialParString(File mgfParaFile, File mzxmlParaFile) throws IOException {

		StringBuilder mgfsb = new StringBuilder();

		FileInputStream mgfFis = new FileInputStream(mgfParaFile);
		BOMInputStream mgfBomInputStream = new BOMInputStream(mgfFis);

		BufferedReader reader = new BufferedReader(new InputStreamReader(mgfBomInputStream));
		String line = null;
		while ((line = reader.readLine()) != null) {
			mgfsb.append(line);
		}
		reader.close();
		this.mgfParString = mgfsb.toString();

		StringBuilder mzxmlsb = new StringBuilder();

		FileInputStream xmlfis = new FileInputStream(mzxmlParaFile);
		BOMInputStream xmlBomInputStream = new BOMInputStream(xmlfis);

		reader = new BufferedReader(new InputStreamReader(xmlBomInputStream));
		while ((line = reader.readLine()) != null) {
			mzxmlsb.append(line);
		}
		reader.close();
		this.mzxmlParString = mzxmlsb.toString();
	}

	public void convert(String[] raws) throws DocumentException, IOException {

		this.confMgf = new File(this.spectraDir, "spectra_convert_mgf.params");
		this.createConf(confMgf.getAbsolutePath());

		this.confMzxml = new File(this.spectraDir, "spectra_convert_mzxml.params");
		this.createConf(confMzxml.getAbsolutePath());

		this.configMgf(raws);
		this.configMzxml(raws);

		try {
			String cmd = convertCmd + " raw2mgf -c " + this.confMgf;

			Runtime run = Runtime.getRuntime();

			Process p = run.exec(cmd);
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
			e.printStackTrace();
		}

		try {
			String cmd = convertCmd + " raw2mgf -c " + this.confMzxml;

			Runtime run = Runtime.getRuntime();

			Process p = run.exec(cmd);
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
			e.printStackTrace();
		}
	}

	private void createConf(String conf) {

		String cmd = convertCmd + " raw2mgf -c " + conf + " --create";

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			LOGGER.error("Error in creating conf file", e);
		}
	}

	public String raw2Mgf(String raw) {

		File file = new File(raw);
		String conf = null;

		try {
			conf = config(mgfId, file.getParentFile(), file);
		} catch (IOException | DocumentException e) {
			LOGGER.error("Cann't find raw file(s) " + raw, e);
		}

		String cmd = convertCmd + " raw2mgf -c " + conf;

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			LOGGER.error("Error in converting file " + raw, e);
		}

		String name = file.getName();
		name = name.substring(0, name.lastIndexOf(".")) + ".mgf";
		return file.getParent() + "\\" + name;
	}

	public String raw2Mgf(String raw, String spectraDir) {

		File file = new File(raw);
		String name = file.getName();
		String spectraName = name.substring(0, name.lastIndexOf(".")) + ".mgf";

		File result = new File(spectraDir, spectraName);
		if (result.exists()) {
			return result.getAbsolutePath();
		}

		String conf = null;

		try {
			conf = config(mgfId, new File(spectraDir), file);
		} catch (IOException | DocumentException e) {
			LOGGER.error("Cann't find raw file(s) " + raw, e);
		}

		String cmd = convertCmd + " raw2mgf -c " + conf;

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				if (!lineStr.startsWith("Find ")) {
					int id = lineStr.lastIndexOf("\\");
					if (id > 0) {
						System.out.println(format.format(new Date()) + "\t" + lineStr.substring(id + 1));
					} else {
						System.out.println(format.format(new Date()) + "\t" + lineStr);
					}
				}
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in converting file " + raw, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in converting file " + raw);
		}

		if (!result.exists()) {
			LOGGER.error("Error in converting file " + raw);
			System.err.println(format.format(new Date()) + "\t" + "Error in converting file " + raw);
		}

		return result.getAbsolutePath();
	}

	public void raws2Mgfs(String[] raw, String spectraDir) {

		if (raw.length == 0) {
			return;
		}

		File[] files = new File[raw.length];
		for (int i = 0; i < files.length; i++) {
			files[i] = new File(raw[i]);
			if (!files[i].exists()) {
				LOGGER.error("Error in converting file " + files[i] + ", file dosen't exist");
				System.err.println(format.format(new Date()) + "\t" + "Error in converting file " + files[i]
						+ ", file dosen't exist");
				return;
			}
		}

		String conf = null;

		try {
			conf = config(mgfId, new File(spectraDir), files);
		} catch (IOException | DocumentException e) {
			LOGGER.error("Cann't find raw file(s) " + raw, e);
		}

		try {
			ProcessBuilder pb = new ProcessBuilder(convertCmd, "raw2mgf", "-c", conf);
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				if (!lineStr.startsWith("Find ")) {
					int id = lineStr.lastIndexOf("\\");
					if (id > 0) {
						System.out.println(format.format(new Date()) + "\t" + lineStr.substring(id + 1));
					} else {
						System.out.println(format.format(new Date()) + "\t" + lineStr);
					}
				}
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in converting file " + Arrays.toString(raw), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in converting file " + Arrays.toString(raw));
		}
	}

	public File raw2Mgf(File... raws) {

		String conf = null;

		try {
			conf = config(mgfId, raws[0].getParentFile(), raws);
		} catch (IOException | DocumentException e) {
			for (File raw : raws) {
				LOGGER.error("Cann't find raw file(s) " + raw, e);
			}
		}

		try {
			ProcessBuilder pb = new ProcessBuilder(convertCmd, "raw2mgf", "-c", conf);
			Process p = pb.start();
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
			LOGGER.error("Error in converting raw file", e);
		}

		String name = raws[0].getName();
		name = name.substring(0, name.lastIndexOf(".")) + ".mgf";
		return new File(raws[0].getParent(), name);
	}

	public String raw2Mzxml(String raw) {

		File file = new File(raw);
		String conf = null;

		try {
			conf = config(mzxmlId, file);
		} catch (IOException | DocumentException e) {
			System.err.println("Cann't find raw file(s) " + raw);
		}

		String cmd = convertCmd + " raw2mgf -c " + conf;

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			LOGGER.error("Error in converting file " + raw, e);
		}

		String name = file.getName();
		name = name.substring(0, name.lastIndexOf(".")) + ".mzXML";
		return file.getParent() + "\\" + name;
	}

	public String raw2Mzxml(String raw, String spectraDir) {

		File file = new File(raw);
		String name = file.getName();
		String spectraName = name.substring(0, name.lastIndexOf(".")) + ".mzXML";

		File result = new File(spectraDir, spectraName);
		if (result.exists()) {
			result.getAbsolutePath();
		}

		String conf = null;

		try {
			conf = config(mzxmlId, new File(spectraDir), file);
		} catch (IOException | DocumentException e) {
			System.err.println("Cann't find raw file(s) " + raw);
		}

		String cmd = convertCmd + " raw2mgf -c " + conf;

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			LOGGER.error("Error in converting file " + raw, e);
		}

		return result.getAbsolutePath();
	}

	public void raw2Mzxml(File resultDir, File... raw) {

		String conf = null;

		try {
			conf = config(mzxmlId, resultDir, raw);
		} catch (IOException | DocumentException e) {
			System.err.println("Cann't find raw file(s) " + raw);
		}

		String cmd = convertCmd + " raw2mgf -c " + conf;

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			LOGGER.error("Error in converting file " + raw, e);
		}
	}

	/*
	 * public File raw2Mzxml(File... raws) {
	 * 
	 * String conf = null;
	 * 
	 * try { conf = config(mzxmlId, raws[0].getParentFile(), raws); } catch
	 * (IOException | DocumentException e) { for (File raw : raws) {
	 * System.err.println("Cann't find raw file(s) " + raw); } }
	 * 
	 * String cmd = convertCmd + " raw2mgf -c " + conf; Runtime run =
	 * Runtime.getRuntime(); try { Process p = run.exec(cmd); BufferedInputStream in
	 * = new BufferedInputStream(p.getInputStream()); BufferedReader inBr = new
	 * BufferedReader(new InputStreamReader(in)); String lineStr; while ((lineStr =
	 * inBr.readLine()) != null) System.out.println(lineStr); if (p.waitFor() != 0)
	 * { if (p.exitValue() == 1) System.err.println("false"); } inBr.close();
	 * in.close(); } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * String name = raws[0].getName(); name = name.substring(0,
	 * name.lastIndexOf(".")) + ".mzXML"; return new File(raws[0].getParent(),
	 * name); }
	 */

	/*
	 * private String config(int type, File... raws) throws DocumentException,
	 * IOException {
	 * 
	 * if (raws.length == 0) { throw new IOException() {
	 *//**
		* 
		*/

	/*
	 * private static final long serialVersionUID = 1L; }; }
	 * 
	 * SAXReader reader = new SAXReader(); File parent = raws[0].getParentFile();
	 * 
	 * if (type == mgfId) {
	 * 
	 * Document document = reader.read(convertMGF); Element root =
	 * document.getRootElement(); Iterator<Element> it = root.elementIterator();
	 * while (it.hasNext()) { Element element = it.next(); String name =
	 * element.getName(); if (name.equals("TargetDirectory")) {
	 * element.setText(String.valueOf(parent.getAbsolutePath()));
	 * 
	 * } else if (name.equals("RawFiles")) {
	 * 
	 * Iterator<Element> fileIt = element.elementIterator(); while
	 * (fileIt.hasNext()) { Element eFile = fileIt.next(); element.remove(eFile); }
	 * 
	 * for (File raw : raws) { Element eFile =
	 * DocumentFactory.getInstance().createElement("File");
	 * eFile.setText(raw.getAbsolutePath()); element.add(eFile); } } } XMLWriter
	 * writer = new XMLWriter(new FileWriter(convertMGF),
	 * OutputFormat.createPrettyPrint()); writer.write(document); writer.close();
	 * 
	 * return convertMGF;
	 * 
	 * } else if (type == mzxmlId) {
	 * 
	 * Document document = reader.read(convertMzxml); Element root =
	 * document.getRootElement(); Iterator<Element> it = root.elementIterator();
	 * while (it.hasNext()) { Element element = it.next(); String name =
	 * element.getName(); if (name.equals("TargetDirectory")) {
	 * element.setText(String.valueOf(parent.getAbsolutePath()));
	 * 
	 * } else if (name.equals("RawFiles")) {
	 * 
	 * Iterator<Element> fileIt = element.elementIterator(); while
	 * (fileIt.hasNext()) { Element eFile = fileIt.next(); element.remove(eFile); }
	 * 
	 * for (File raw : raws) { Element eFile =
	 * DocumentFactory.getInstance().createElement("File");
	 * eFile.setText(raw.getAbsolutePath()); element.add(eFile); } } } XMLWriter
	 * writer = new XMLWriter(new FileWriter(convertMzxml),
	 * OutputFormat.createPrettyPrint()); writer.write(document); writer.close();
	 * 
	 * return convertMzxml;
	 * 
	 * } else { throw new IOException() {
	 *//**
		 * @throws DocumentException
		 * @throws IOException
		 * 
		 *//*
			 * private static final long serialVersionUID = 1L; }; } }
			 */

	@SuppressWarnings("unchecked")
	private void configMgf(String... raws) throws DocumentException, IOException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(confMgf);
		Element root = document.getRootElement();
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("TargetDirectory")) {
				element.setText(spectraDir.getAbsolutePath());

			} else if (name.equals("RawFiles")) {

				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : raws) {
					Element eFile = DocumentFactory.getInstance().createElement("File");
					eFile.setText(raw);
					element.add(eFile);
				}
			} else if (name.equals("OutputMzXmlFormat")) {
				element.setText("false");
			}
		}
		XMLWriter writer = new XMLWriter(new FileWriter(confMgf), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	@SuppressWarnings("unchecked")
	private void configMzxml(String... raws) throws DocumentException, IOException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(confMzxml);
		Element root = document.getRootElement();
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (name.equals("TargetDirectory")) {
				element.setText(spectraDir.getAbsolutePath());

			} else if (name.equals("RawFiles")) {

				Iterator<Element> fileIt = element.elementIterator();
				while (fileIt.hasNext()) {
					Element eFile = fileIt.next();
					element.remove(eFile);
				}

				for (String raw : raws) {
					Element eFile = DocumentFactory.getInstance().createElement("File");
					eFile.setText(raw);
					element.add(eFile);
				}
			} else if (name.equals("OutputMzXmlFormat")) {
				element.setText("true");
			}
		}
		XMLWriter writer = new XMLWriter(new FileWriter(confMzxml), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	@SuppressWarnings("unchecked")
	private String config(int type, File resultDir, File... raws) throws DocumentException, IOException {

		if (type == mgfId) {

			Document document = DocumentHelper.parseText(this.mgfParString);

			Element root = document.getRootElement();
			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element element = it.next();
				String name = element.getName();
				if (name.equals("TargetDirectory")) {
					element.setText(resultDir.getAbsolutePath());

				} else if (name.equals("RawFiles")) {

					Iterator<Element> fileIt = element.elementIterator();
					while (fileIt.hasNext()) {
						Element eFile = fileIt.next();
						element.remove(eFile);
					}

					for (File raw : raws) {
						Element eFile = DocumentFactory.getInstance().createElement("File");
						eFile.setText(raw.getAbsolutePath());
						element.add(eFile);
					}
				} else if (name.equals("KeepTopX")) {
					element.setText("true");
				} else if (name.equals("TopX")) {
					element.setText("20");
				} else if (name.equals("ParallelMode")) {
					element.setText("true");
				}
			}

			File paraFile = new File(resultDir, "mgf_convert.param");
			XMLWriter writer = new XMLWriter(new FileWriter(paraFile), OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

			return paraFile.getAbsolutePath();

		} else if (type == mzxmlId) {

			Document document = DocumentHelper.parseText(this.mzxmlParString);
			Element root = document.getRootElement();
			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element element = it.next();
				String name = element.getName();
				if (name.equals("TargetDirectory")) {
					element.setText(resultDir.getAbsolutePath());

				} else if (name.equals("RawFiles")) {

					Iterator<Element> fileIt = element.elementIterator();
					while (fileIt.hasNext()) {
						Element eFile = fileIt.next();
						element.remove(eFile);
					}

					for (File raw : raws) {
						Element eFile = DocumentFactory.getInstance().createElement("File");
						eFile.setText(raw.getAbsolutePath());
						element.add(eFile);
					}
				} else if (name.equals("KeepTopX")) {
					element.setText("true");
				} else if (name.equals("TopX")) {
					element.setText("20");
				} else if (name.equals("ParallelMode")) {
					element.setText("true");
				}
			}
			File paraFile = new File(resultDir, "mzxml_convert.param");
			XMLWriter writer = new XMLWriter(new FileWriter(paraFile), OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

			return paraFile.getAbsolutePath();

		} else {
			LOGGER.error("Unknown spectra type");
			throw new IOException() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 382315935628329060L;

			};
		}
	}

	@SuppressWarnings("unused")
	private static void create(String conf) {
		ProteomicsTools pt = new ProteomicsTools(MetaSourcesIOV2.parse(""), new File(""));
		String cmd = pt.convertCmd + " raw2mgf -c " + conf + " --create";

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
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
			e.printStackTrace();
		}
	}
	
	private static void test(String in, String out, String par) {
		File[] files = (new File(in)).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if(pathname.getName().endsWith("raw"))
					return true;
				return false;
			}
		});
		String[] raws = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			raws[i] = files[i].getAbsolutePath();
		}
		ProteomicsTools pt = new ProteomicsTools(MetaSourcesIOV2.parse(par), new File(out));
		pt.raws2Mgfs(raws, out);
	}

	public static void main(String[] args) throws DocumentException, IOException {

		String name = "ProteomicsTools.exe";
		String cmd = "taskkill /F /IM " + name;
		System.out.println(cmd);
		Thread thread = new Thread() {
			public void run() {
				Runtime run = Runtime.getRuntime();
				try {
					Process p = run.exec(cmd);
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
					e.printStackTrace();
				}
			}
		};
		System.out.println(System.currentTimeMillis());

		ProteomicsTools.test("D:\\sp_tst\\raw", "D:\\sp_tst\\cao", "D:\\Exported\\MetaLab_3\\resources_2_1_0.json");

		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println(System.currentTimeMillis());

		thread.start();

	}
}
