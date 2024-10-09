/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantParameter {

	private ArrayList<String> keyList;
	private HashMap<String, String> attributeMap;
	private HashMap<String, String> totalMap1;
	private HashMap<String, ArrayList<String>> totalMap2;
	private ArrayList<ParameterGroup> paraGroupList;
	private ParameterGroup pg;
	private ArrayList<MsmsParams> msmsList;
	private MsmsParams mp;

	public static String dbPath = "fastaFiles";

	public MaxquantParameter() {
		this.keyList = new ArrayList<String>();
		this.attributeMap = new HashMap<String, String>();
		this.totalMap1 = new HashMap<String, String>();
		this.totalMap2 = new HashMap<String, ArrayList<String>>();
		this.paraGroupList = new ArrayList<ParameterGroup>();
		this.msmsList = new ArrayList<MsmsParams>();
	}

	public void addName(String name) {
		this.keyList.add(name);
	}

	public void addAttribute(String key, String value) {
		this.keyList.add(key);
		this.attributeMap.put(key, value);
	}

	public void addElement(String key, String value) {
		this.keyList.add(key);
		this.totalMap1.put(key, value);
	}

	public void addElement(String key, ArrayList<String> list) {
		this.keyList.add(key);
		this.totalMap2.put(key, list);
	}

	public void addParameterGroup() {
		ParameterGroup pg = new ParameterGroup();
		this.paraGroupList.add(pg);
		this.pg = pg;
	}

	public void addMsmsParams() {
		MsmsParams mp = new MsmsParams();
		this.msmsList.add(mp);
		this.mp = mp;
	}

	public void addParameterItem(String key, String value) {
		this.pg.add(key, value);
	}

	public void addParameterItem(String key, ArrayList<String> value) {
		this.pg.add(key, value);
	}

	public void addMsmsItem(String key, String value) {
		this.mp.addAttribute(key, value);
	}

	public void addMsmsItem2(String key, String value) {
		this.mp.addElement(key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {
		this.totalMap1.put(key, value);
	}

	/**
	 * Set files, modifications, fastas
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, ArrayList<String> value) {
		this.totalMap2.put(key, value);
	}

	public void export(String out) throws IOException {

		Document document = DocumentHelper.createDocument();
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(out), format);

		Element root = DocumentFactory.getInstance().createElement("MaxQuantParams");
		Namespace namespace1 = new Namespace("xsd", "http://www.w3.org/2001/XMLSchema");
		Namespace namespace2 = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.add(namespace1);
		root.add(namespace2);

		for (String key : this.keyList) {
			if (attributeMap.containsKey(key)) {
				root.addAttribute(key, attributeMap.get(key));
			} else if (totalMap1.containsKey(key)) {
				Element element = DocumentFactory.getInstance().createElement(key);
				element.setText(totalMap1.get(key));
				root.add(element);
			} else if (totalMap2.containsKey(key)) {
				if (key.equals("paramGroupIndices")) {
					Element element = DocumentFactory.getInstance().createElement(key);
					ArrayList<String> list = totalMap2.get(key);
					for (String ss : list) {
						Element e = DocumentFactory.getInstance().createElement("int");
						e.setText(ss);
						element.add(e);
					}
					root.add(element);
				} else if (key.equals("fractions")) {
					Element element = DocumentFactory.getInstance().createElement(key);
					ArrayList<String> list = totalMap2.get(key);
					for (String ss : list) {
						Element e = DocumentFactory.getInstance().createElement("short");
						e.setText(ss);
						element.add(e);
					}
					root.add(element);
				} else {

					Element element = DocumentFactory.getInstance().createElement(key);
					ArrayList<String> list = totalMap2.get(key);
					for (String ss : list) {
						Element e = DocumentFactory.getInstance().createElement("string");
						e.setText(ss);
						element.add(e);
					}
					root.add(element);
				}
			} else {
				if (key.equals("parameterGroups")) {
					Element element = DocumentFactory.getInstance().createElement(key);
					for (ParameterGroup pmg : paraGroupList) {
						pmg.addToRoot(element);
					}
					root.add(element);
				} else if (key.equals("msmsParamsArray")) {
					Element element = DocumentFactory.getInstance().createElement(key);
					for (MsmsParams msp : msmsList) {
						msp.addToRoot(element);
					}
					root.add(element);
				}
			}
		}
		document.setRootElement(root);
		writer.write(document);
		writer.close();
	}

	class ParameterGroup {

		private ArrayList<String> paraItemNameList;
		private HashMap<String, String> paraMap1;
		private HashMap<String, ArrayList<String>> paraMap2;

		private ParameterGroup() {
			this.paraItemNameList = new ArrayList<String>();
			this.paraMap1 = new HashMap<String, String>();
			this.paraMap2 = new HashMap<String, ArrayList<String>>();
		}

		private void add(String key, String value) {
			paraItemNameList.add(key);
			paraMap1.put(key, value);
		}

		private void add(String key, ArrayList<String> list) {
			paraItemNameList.add(key);
			paraMap2.put(key, list);
		}

		private void addToRoot(Element root) {
			Element parent = DocumentFactory.getInstance().createElement("parameterGroup");
			for (String key : paraItemNameList) {
				Element leaf = DocumentFactory.getInstance().createElement(key);
				if (paraMap1.containsKey(key)) {
					leaf.setText(paraMap1.get(key));
				} else if (paraMap2.containsKey(key)) {
					ArrayList<String> list = paraMap2.get(key);
					for (String value : list) {
						Element e = DocumentFactory.getInstance().createElement("string");
						e.setText(value);
						leaf.add(e);
					}
				}
				parent.add(leaf);
			}
			root.add(parent);
		}
	}

	class MsmsParams {

		private ArrayList<String> msmsNameList;
		private HashMap<String, String> attributeMap;
		private HashMap<String, String> elementMap;

		private MsmsParams() {
			this.msmsNameList = new ArrayList<String>();
			this.attributeMap = new HashMap<String, String>();
			this.elementMap = new HashMap<String, String>();
		}

		private void addAttribute(String key, String value) {
			this.msmsNameList.add(key);
			this.attributeMap.put(key, value);
		}

		private void addElement(String key, String value) {
			this.msmsNameList.add(key);
			this.elementMap.put(key, value);
		}

		private void addToRoot(Element root) {
			Element parent = DocumentFactory.getInstance().createElement("msmsParams");
			for (String key : msmsNameList) {
				if (attributeMap.containsKey(key)) {
					parent.addAttribute(key, attributeMap.get(key));
				} else if (elementMap.containsKey(key)) {
					Element leaf = DocumentFactory.getInstance().createElement(key);
					leaf.setText(elementMap.get(key));
					parent.add(leaf);
				}
			}
			root.add(parent);
		}
	}
}
