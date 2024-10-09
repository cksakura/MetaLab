package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 
 * @author Kai Cheng
 *
 */
public class MaxquantEnzymeHandler {

	private String enzyme = "Resources//mq_bin//conf//enzymes.xml";

	private static Logger LOGGER = LogManager.getLogger(MaxquantEnzymeHandler.class);
	
	public MaxquantEnzymeHandler() {
		
	}
	
	public MaxquantEnzymeHandler(String enzyme) {
		this.enzyme = enzyme;
	}

	@SuppressWarnings("unchecked")
	public MaxquantEnzyme[] getEnzymes() {

		ArrayList<MaxquantEnzyme> list = new ArrayList<MaxquantEnzyme>();

		File file = new File(enzyme);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(new File(enzyme));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + enzyme, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element enzyme = it.next();
				String title = enzyme.attributeValue("title");
				String des = enzyme.attributeValue("description");
				Element specificity = enzyme.element("specificity");

				List<Element> splist = specificity.elements("string");
				String[] spArrays = new String[splist.size()];
				for (int i = 0; i < spArrays.length; i++) {
					spArrays[i] = splist.get(i).getText();
				}
				list.add(new MaxquantEnzyme(title, des, spArrays));
			}
		}

		MaxquantEnzyme[] enzymes = list.toArray(new MaxquantEnzyme[list.size()]);
		return enzymes;
	}

	@SuppressWarnings("unchecked")
	public String getEnzyme4Tandem(String enzymeName) {
		File file = new File(enzyme);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(new File(enzyme));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + enzyme, e);
			}
			Element root = document.getRootElement();

			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element eEnzyme = it.next();
				String title = eEnzyme.attributeValue("title");
				if (title.equals(enzymeName)) {
					Element specificity = eEnzyme.element("specificity");
					List<Element> splist = specificity.elements("string");
					HashSet<Character> set1 = new HashSet<Character>();
					HashSet<Character> set2 = new HashSet<Character>();
					for (int i = 'A'; i <= 'Z'; i++) {
						if (i != 'B' && i != 'J' && i != 'O' && i != 'U' && i != 'X' && i != 'Z') {
							set2.add((char) i);
						}
					}
					for (int i = 0; i < splist.size(); i++) {
						String si = splist.get(i).getText();
						char c0 = si.charAt(0);
						char c1 = si.charAt(1);
						set1.add(c0);
						set2.remove(c1);
					}

					StringBuilder sb = new StringBuilder();
					sb.append("[");
					for (Character cc : set1) {
						sb.append(cc);
					}
					sb.append("]").append("|").append("{");
					for (Character cc : set2) {
						sb.append(cc);
					}
					sb.append("}");
					return sb.toString();
				}
			}
		}

		return null;
	}

	public String getEnzyme4Tandem(MaxquantEnzyme enzyme) {

		HashSet<Character> set1 = new HashSet<Character>();
		HashSet<Character> set2 = new HashSet<Character>();
		for (int i = 'A'; i <= 'Z'; i++) {
			if (i != 'B' && i != 'J' && i != 'O' && i != 'U' && i != 'X' && i != 'Z') {
				set2.add((char) i);
			}
		}
		String[] specs = enzyme.getSpecificity();
		for (int i = 0; i < specs.length; i++) {
			char c0 = specs[i].charAt(0);
			char c1 = specs[i].charAt(1);
			set1.add(c0);
			set2.remove(c1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Character cc : set1) {
			sb.append(cc);
		}
		sb.append("]").append("|").append("{");
		for (Character cc : set2) {
			sb.append(cc);
		}
		sb.append("}");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public String[] getTitles() {
		ArrayList<String> list = new ArrayList<String>();
		File file = new File(enzyme);
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(new File(enzyme));
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in parsing MaxQuant parameter file " + enzyme, e);
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

	public static void main(String[] args) throws DocumentException {
		// TODO Auto-generated method stub

	}

}
