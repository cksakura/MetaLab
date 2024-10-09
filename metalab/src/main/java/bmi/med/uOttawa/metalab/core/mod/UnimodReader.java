/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Kai Cheng
 *
 */
public class UnimodReader {

	private static final String unimod = "Resources//unimod.xml";

	/**
	 * Multiple<br />
	 * Non-standard residue <br />
	 * O-linked glycosylation <br />
	 * Post-translational<br />
	 * Artefact <br />
	 * Chemical derivative <br />
	 * Other glycosylation <br />
	 * Isotopic label <br />
	 * AA substitution <br />
	 * N-linked glycosylation <br />
	 * Pre-translational Other<br />
	 * Co-translational <br />
	 * Synth. pep. protect. gp.
	 */
	private boolean onlyPTMAndAASub;

	private HashMap<String, UmElement> elementMap;
	private HashMap<String, UmModification> modMap;
	private HashMap<String, UmAminoacid> aaMap;
	private HashMap<String, UmBrick> brickMap;

	private UmModification[] umMods;

	public UnimodReader() throws DocumentException {
		this.parse(unimod);
	}

	public UnimodReader(boolean onlyPTMAndAASub) throws DocumentException {
		this.onlyPTMAndAASub = onlyPTMAndAASub;
		this.parse(unimod);
	}

	public UnimodReader(String unimod) throws DocumentException {
		this.parse(unimod);
	}

	public UnimodReader(String unimod, boolean onlyPTMAndAASub) throws DocumentException {
		this.onlyPTMAndAASub = onlyPTMAndAASub;
		this.parse(unimod);
	}

	@SuppressWarnings("unchecked")
	private void parse(String unimod) throws DocumentException {

		this.elementMap = new HashMap<String, UmElement>();
		this.modMap = new HashMap<String, UmModification>();
		this.aaMap = new HashMap<String, UmAminoacid>();
		this.brickMap = new HashMap<String, UmBrick>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(unimod);
		Element root = document.getRootElement();

		Element elementRoot = root.element("elements");
		Element modRoot = root.element("modifications");
		Element aaRoot = root.element("amino_acids");
		Element brickRoot = root.element("mod_bricks");

		Iterator<Element> elementIt = elementRoot.elementIterator();
		while (elementIt.hasNext()) {

			Element ei = elementIt.next();
			String title = ei.attributeValue("title");
			String name = ei.attributeValue("full_name");
			double avge_mass = Double.parseDouble(ei.attributeValue("avge_mass"));
			double mono_mass = Double.parseDouble(ei.attributeValue("mono_mass"));

			UmElement ue = new UmElement(title, name, avge_mass, mono_mass);
			this.elementMap.put(title, ue);
		}

		Iterator<Element> modIt = modRoot.elementIterator();
		while (modIt.hasNext()) {
			Element modElement = modIt.next();
			String title = modElement.attributeValue("title");
			String name = modElement.attributeValue("full_name");
			List<Element> splist = modElement.elements("specificity");

			String[] specificity;
			if (onlyPTMAndAASub) {
				ArrayList<String> specificityList = new ArrayList<String>();
				for (int i = 0; i < splist.size(); i++) {
					Element spi = splist.get(i);
					String site = spi.attributeValue("site");
					String position = spi.attributeValue("position");
					String classification = spi.attributeValue("classification");
					if (classification.equals("Post-translational") || classification.endsWith("AA substitution")) {
						specificityList.add(UmSpecTranslator.translate(site, position));
					}
				}

				if (specificityList.size() == 0) {
					continue;
				} else {
					specificity = specificityList.toArray(new String[specificityList.size()]);
				}
			} else {
				specificity = new String[splist.size()];
				for (int i = 0; i < splist.size(); i++) {
					Element spi = splist.get(i);
					String site = spi.attributeValue("site");
					String position = spi.attributeValue("position");
					specificity[i] = UmSpecTranslator.translate(site, position);
				}
			}

			Element delta = modElement.element("delta");
			double mono_mass = Double.parseDouble(delta.attributeValue("mono_mass"));
			double avge_mass = Double.parseDouble(delta.attributeValue("avge_mass"));
			List<Element> complist = delta.elements("element");
			UmElement[] comp_element = new UmElement[complist.size()];
			int[] comp_count = new int[complist.size()];

			for (int i = 0; i < complist.size(); i++) {
				Element compi = complist.get(i);
				comp_element[i] = this.elementMap.get(compi.attributeValue("symbol"));
				comp_count[i] = Integer.parseInt(compi.attributeValue("number"));
			}

			UmModification um = new UmModification(title, name, specificity, mono_mass, avge_mass, comp_element,
					comp_count);
			this.modMap.put(title, um);
		}
		this.umMods = this.modMap.values().toArray(new UmModification[this.modMap.size()]);
		Arrays.sort(this.umMods, new UmModification.UmModMassComparator());

		Iterator<Element> aaIt = aaRoot.elementIterator();
		while (aaIt.hasNext()) {
			Element aaElement = aaIt.next();
			String title = aaElement.attributeValue("title");
			String threeLet = aaElement.attributeValue("three_letter");
			String name = aaElement.attributeValue("full_name");
			double mono_mass = Double.parseDouble(aaElement.attributeValue("mono_mass"));
			double avge_mass = Double.parseDouble(aaElement.attributeValue("avge_mass"));

			List<Element> complist = aaElement.elements("element");
			UmElement[] comp_element = new UmElement[complist.size()];
			int[] comp_count = new int[complist.size()];

			for (int i = 0; i < complist.size(); i++) {
				Element compi = complist.get(i);
				comp_element[i] = this.elementMap.get(compi.attributeValue("symbol"));
				comp_count[i] = Integer.parseInt(compi.attributeValue("number"));
			}

			UmAminoacid ua = new UmAminoacid(title, threeLet, name, mono_mass, avge_mass, comp_element, comp_count);
			this.aaMap.put(title, ua);
		}

		Iterator<Element> brickIt = brickRoot.elementIterator();
		while (brickIt.hasNext()) {
			Element brickElement = brickIt.next();
			String title = brickElement.attributeValue("title");
			String name = brickElement.attributeValue("full_name");
			double mono_mass = Double.parseDouble(brickElement.attributeValue("mono_mass"));
			double avge_mass = Double.parseDouble(brickElement.attributeValue("avge_mass"));

			List<Element> complist = brickElement.elements("element");
			UmElement[] comp_element = new UmElement[complist.size()];
			int[] comp_count = new int[complist.size()];

			for (int i = 0; i < complist.size(); i++) {
				Element compi = complist.get(i);
				comp_element[i] = this.elementMap.get(compi.attributeValue("symbol"));
				comp_count[i] = Integer.parseInt(compi.attributeValue("number"));
			}

			UmBrick ub = new UmBrick(title, name, mono_mass, avge_mass, comp_element, comp_count);
			this.brickMap.put(title, ub);
		}
	}

	public UmElement[] getUmElements() {
		UmElement[] elements = this.elementMap.values().toArray(new UmElement[this.elementMap.size()]);
		return elements;
	}

	public UmModification[] getUmModifications() {
		return umMods;
	}

	public UmAminoacid[] getUmAminoacids() {
		UmAminoacid[] aas = this.aaMap.values().toArray(new UmAminoacid[this.aaMap.size()]);
		return aas;
	}

	public UmBrick[] getUmBricks() {
		UmBrick[] bricks = this.brickMap.values().toArray(new UmBrick[this.brickMap.size()]);
		return bricks;
	}

	public UmElement getElementFromTitle(String title) {
		return this.elementMap.get(title);
	}

	public UmModification getModFromTitle(String title) {
		return this.modMap.get(title);
	}

	public UmAminoacid getAminoacidFromTitle(String title) {
		return this.aaMap.get(title);
	}

	public UmBrick getBrickFromTitle(String title) {
		return this.brickMap.get(title);
	}

}
