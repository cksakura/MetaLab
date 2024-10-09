/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantModHandler {

	private String modifications = "Resources//mq_bin//conf//modifications.xml";

	private static final String[] elements = new String[] { "H", "Hx", "C", "Cx", "N", "Nx", "O", "Ox", "S", "P",
			"Na" };
	private static final double[] monoMassList = new double[] { 1.007825, 2.014101, 12, 13.003354, 14.003074, 15.000108,
			15.994915, 17.9991603, 31.972071, 30.973762, 22.989770 };

	private static Logger LOGGER = LogManager.getLogger();

	public MaxquantModHandler() {

	}

	public MaxquantModHandler(String modifications) {
		this.modifications = modifications;
	}

	@SuppressWarnings("unchecked")
	public MaxquantModification[][] getModifications() {

		ArrayList<MaxquantModification> list0 = new ArrayList<MaxquantModification>();
		ArrayList<MaxquantModification> list1 = new ArrayList<MaxquantModification>();
		ArrayList<MaxquantModification> list2 = new ArrayList<MaxquantModification>();

		File file = new File(modifications);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + modifications, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element modification = it.next();
				String title = modification.attributeValue("title");
				String des = modification.attributeValue("description");
				String comp = modification.attributeValue("composition");
				String position = modification.element("position").getText();
				String type = modification.element("type").getText();
				double mass = this.getMass4Comp(comp);

				List<Element> siteList = modification.elements("modification_site");
				String[] sites = new String[siteList.size()];
				for (int i = 0; i < sites.length; i++) {
					sites[i] = siteList.get(i).attributeValue("site");
				}

				MaxquantModification mm = new MaxquantModification(title, des, position, comp, sites, mass);
				list0.add(mm);
				if (type.equals("Label")) {
					list1.add(mm);
				} else if (type.equals("IsobaricLabel")) {

					Element tmtElement = siteList.get(0);
					Element diagnostics = tmtElement.element("diagnostic_collection");
					Element tagElement = diagnostics.element("diagnostic");
					String tmtComp = tagElement.attributeValue("composition");
					double tmtMass = this.getMass4Comp(tmtComp);

					mm.setTmtComp(tmtComp);
					mm.setTmtMass(tmtMass);

					list2.add(mm);
				}
			}
		}

		MaxquantModification[][] mods = new MaxquantModification[3][];
		mods[0] = list0.toArray(new MaxquantModification[list0.size()]);
		mods[1] = list1.toArray(new MaxquantModification[list1.size()]);
		mods[2] = list2.toArray(new MaxquantModification[list2.size()]);

		return mods;
	}

	@SuppressWarnings("unchecked")
	public String[] getTitles() {

		ArrayList<String> list = new ArrayList<String>();
		File file = new File(modifications);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + modifications, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element modification = it.next();
				String title = modification.attributeValue("title");
				list.add(title);
			}
		}

		String[] titles = list.toArray(new String[list.size()]);
		return titles;
	}

	@SuppressWarnings("unchecked")
	public String getMod4Tandem(String[] mods) {

		StringBuilder sb = new StringBuilder();

		File file = new File(modifications);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + modifications, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element modification = it.next();
				String title = modification.attributeValue("title");
				for (String mod : mods) {
					if (mod.equals(title)) {
						String comp = modification.attributeValue("composition");
						String position = modification.element("position").getText();
						double mass = getMass4Comp(comp);
						sb.append(mass).append("@");
						List<Element> siteList = modification.elements("modification_site");
						for (int i = 0; i < siteList.size(); i++) {
							String site = siteList.get(i).attributeValue("site");
							if (site.equals("-")) {
								if (position.endsWith("Nterm")) {
									sb.append("[,");
								} else if (position.endsWith("Cterm")) {
									sb.append("],");
								} else {

								}
							} else {
								sb.append(site).append(",");
							}
						}
						break;
					}
				}
			}
			if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
		}

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public String getMod4Tandem(String[] mods, String[] isobaric) {

		StringBuilder sb = new StringBuilder();

		File file = new File(modifications);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + modifications, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element modification = it.next();
				String title = modification.attributeValue("title");
				for (String mod : mods) {
					if (mod.equals(title)) {
						String comp = modification.attributeValue("composition");
						String position = modification.element("position").getText();
						double mass = getMass4Comp(comp);
						sb.append(mass).append("@");
						List<Element> siteList = modification.elements("modification_site");
						for (int i = 0; i < siteList.size(); i++) {
							String site = siteList.get(i).attributeValue("site");
							if (site.equals("-")) {
								if (position.endsWith("Nterm")) {
									sb.append("[,");
								} else if (position.endsWith("Cterm")) {
									sb.append("],");
								} else {

								}
							} else {
								sb.append(site).append(",");
							}
						}
						break;
					}
				}
			}

			IsobaricTag[] tags = IsobaricTag.values();
			IsobaricTag tag = null;
			boolean nterm = false;
			boolean lys = false;
			for (String iso : isobaric) {
				int sp = iso.indexOf("-");
				String name = iso.substring(0, sp);

				if (tag == null) {
					for (int i = 0; i < tags.length; i++) {
						if (tags[i].getName().equals(name)) {
							if (tag == null) {
								tag = tags[i];
							}
						}
					}
					if (tag == null) {
						LOGGER.info("Isobaric tags " + "\"" + name + "\""
								+ " is unknown, isobaric quantification will not be performed.");
					}
				} else {
					if (!tag.getName().equals(name)) {
						LOGGER.info("Multiple isobaric tags " + "\"" + tag.getName() + "\"" + "\"" + name + "\""
								+ " are found, isobaric quantification will not be performed.");
					}
				}

				String loc = iso.substring(sp + 1);
				if (loc.startsWith("Nter")) {
					nterm = true;
				}
				if (loc.startsWith("Lys")) {
					lys = true;
				}
			}

			if (tag != null) {
				double isomass = tag.getMass();
				if (nterm) {
					sb.append(isomass).append("@[,");
				}
				if (lys) {
					sb.append(isomass).append("@K,");
				}
			}

			if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
		}

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public String getMod4Tandem(String[] mods, String[][] labels) {

		StringBuilder sb = new StringBuilder();

		File file = new File(modifications);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(file);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + modifications, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element modification = it.next();
				String title = modification.attributeValue("title");
				for (String mod : mods) {
					if (mod.equals(title)) {
						String comp = modification.attributeValue("composition");
						String position = modification.element("position").getText();
						double mass = getMass4Comp(comp);
						sb.append(mass).append("@");
						List<Element> siteList = modification.elements("modification_site");
						for (int i = 0; i < siteList.size(); i++) {
							String site = siteList.get(i).attributeValue("site");
							if (site.equals("-")) {
								if (position.endsWith("Nterm")) {
									sb.append("[,");
								} else if (position.endsWith("Cterm")) {
									sb.append("],");
								} else {

								}
							} else {
								sb.append(site).append(",");
							}
						}
						break;
					}
				}
				for (String label : labels[0]) {
					if (label.equals(title)) {
						String type = modification.element("type").getText();
						if (type.equals("Label")) {
							String comp = modification.attributeValue("composition");
							String position = modification.element("position").getText();
							double mass = getMass4Comp(comp);
							sb.append(mass).append("@");
							List<Element> siteList = modification.elements("modification_site");
							for (int i = 0; i < siteList.size(); i++) {
								String site = siteList.get(i).attributeValue("site");
								if (site.equals("-")) {
									if (position.endsWith("Nterm")) {
										sb.append("[,");
									} else if (position.endsWith("Cterm")) {
										sb.append("],");
									} else {

									}
								} else {
									sb.append(site).append(",");
								}
							}
							break;
						}
					}
				}
			}

			if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
		}

		return sb.toString();
	}

	public double getMass4Comp(String composition) {
		double mass = 0;
		String[] comp = composition.split("\\s");
		for (int i = 0; i < comp.length; i++) {
			String[] compi = comp[i].split("[\\(\\)]");
			if (compi.length == 1) {
				for (int j = 0; j < elements.length; j++) {
					if (compi[0].equals(elements[j])) {
						mass += monoMassList[j];
					}
				}
			} else if (compi.length == 2) {
				for (int j = 0; j < elements.length; j++) {
					if (compi[0].equals(elements[j])) {
						mass += monoMassList[j] * Integer.parseInt(compi[1]);
					}
				}
			}
		}
		return mass;
	}

	public void add(MaxquantModification mod) {

	}

	private void export(String out) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(out);
		MaxquantModification[][] mods = this.getModifications();
		StringBuilder sb = new StringBuilder();
		sb.append("const isobaricData = [").append("\n");
		for (int i = 0; i < mods[2].length; i++) {
			sb.append("{ \"isobaric\": \"").append(mods[2][i].getTitle()).append("\"},");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("\n").append("]");
		writer.println(sb);
		writer.println("export default isobaricData;");
		writer.close();
	}

	public static void main(String[] args) throws DocumentException, FileNotFoundException {
		// TODO Auto-generated method stub

		MaxquantModHandler handler = new MaxquantModHandler(
				"D:\\MetaLab\\Project\\Resources\\mq_bin\\conf\\modifications.xml");
		handler.export("Z:\\Kai\\isobaric.js");
	}

}
