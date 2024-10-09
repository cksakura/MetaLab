/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * default_input.xml
 * <p><b>currently not used</b></p>
 * @author Kai Cheng
 *
 */
public class XTandemParameter {
	
	private String[] nodes;
	private HashMap<String, ArrayList<TandemElement>> elementMap;
	private ArrayList<TandemElement> currentList;
	
	private static Logger LOGGER = LogManager.getLogger();
	
	public XTandemParameter() {
		this.nodes = new String[] { "list path parameters", "spectrum parameters", "spectrum conditioning parameters",
				"residue modification parameters", "protein parameters", "model refinement parameters",
				"scoring parameters", "output parameters", "ADDITIONAL EXPLANATIONS", };
		this.elementMap = new HashMap<String, ArrayList<TandemElement>>();
		for (int i = 0; i < nodes.length; i++) {
			this.elementMap.put(nodes[i], new ArrayList<TandemElement>());
		}
	}
	
	public void addElement(String value) {
		TandemElement te = new TandemElement(value);
		if (this.elementMap.containsKey(value)) {
			this.currentList = this.elementMap.get(value);
			this.currentList.add(te);
		} else {
			if (this.currentList != null) {
				this.currentList.add(te);
			}
		}
	}
	
	public void addElement(String type, String value) {
		TandemElement te = new TandemElement(type, value);
		if (this.elementMap.containsKey(value)) {
			this.currentList = this.elementMap.get(value);
			this.currentList.add(te);
		} else {
			if (this.currentList != null) {
				this.currentList.add(te);
			}
		}
	}
	
	public void addElement(String type, String label, String value) {
		TandemElement te = new TandemElement(type, label, value);
		if (this.currentList != null) {
			this.currentList.add(te);
		}
	}
	
	public void export(String out) {

		Document document = DocumentHelper.createDocument();
		HashMap<String, String> inMap = new HashMap<String, String>();
		inMap.put("type", "text/xsl");
		inMap.put("href", "tandem-input-style.xsl");
		document.addProcessingInstruction("xml-stylesheet", inMap);

		Element root = DocumentFactory.getInstance().createElement("bioml");

		for(int i=0;i<nodes.length;i++){
			ArrayList<TandemElement> list = this.elementMap.get(nodes[i]);
			for(int j=0;j<list.size();j++){
				Element element = DocumentFactory.getInstance().createElement("note");
				TandemElement te = list.get(j);
				te.addToRoot(root, element);
			}
		}

		document.setRootElement(root);
		
		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(out), "UTF8");
			XMLWriter writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing MaxQuant parameter to " + out, e);
		}
	}

	private class TandemElement {

		private String type;
		private String label;
		private String value;

		public TandemElement(String value) {
			this.value = value;
		}

		public TandemElement(String type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public TandemElement(String type, String label, String value) {
			this.type = type;
			this.label = label;
			this.value = value;
		}

		private void addToRoot(Element root, Element node) {
			if (this.type != null) {
				node.addAttribute("type", type);
			}
			if (this.label != null) {
				node.addAttribute("label", label);
			}
			if (this.value != null) {
				node.setText(value);
			}
			root.add(node);
		}
	}
}
