/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.xtandem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;

/**
 * @author Kai Cheng
 *
 */
public class XTandemPepReader extends AbstractMetaPeptideReader {

	private Iterator<Element> it;

	private static Logger LOGGER = LogManager.getLogger();

	public XTandemPepReader(String xml) {
		this(new File(xml));
	}

	@SuppressWarnings("unchecked")
	public XTandemPepReader(File xml) {
		super(xml);
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(xml);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading X!Tandem search result file " + xml, e);
		}
		Element root = document.getRootElement();
		this.it = root.elementIterator();
	}

	public boolean hasNext() {
		if (it.hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public XTandemPep4Meta next() {

		int scan = -1;
		String sequence = "";
		double mass = 0;
		int missCleave = -1;
		double hyperscore = -1;
		double expect = -1;
		String protein = "";

		Element group = it.next();

		Iterator<Element> groupIt = group.elementIterator();
		while (groupIt.hasNext()) {
			Element element = groupIt.next();
			String name = element.getName();
			if (name.equals("protein")) {
				Element ePeptide = element.element("peptide");
				Element domain = ePeptide.element("domain");

				protein = element.attributeValue("label");
				sequence = domain.attributeValue("seq");
				mass = Double.parseDouble(domain.attributeValue("mh"));
				missCleave = Integer.parseInt(domain.attributeValue("missed_cleavages"));
				hyperscore = Double.parseDouble(domain.attributeValue("hyperscore"));
				expect = Double.parseDouble(domain.attributeValue("expect"));

			} else if (name.equals("group")) {
				if (element.attributeValue("label").equals("fragment ion mass spectrum")) {
					Element eNote = element.element("note");
					String[] cs = eNote.getStringValue().split("\\.");
					scan = Integer.parseInt(cs[1]);
				}
			}
		}

		XTandemPep4Meta pep = new XTandemPep4Meta(sequence, missCleave, mass, hyperscore, expect, protein);

		return pep;
	}

	public XTandemPep4Meta[] getPSMs() {
		ArrayList<XTandemPep4Meta> list = new ArrayList<XTandemPep4Meta>();
		while (hasNext()) {
			XTandemPep4Meta psm = this.next();
			list.add(psm);
		}

		XTandemPep4Meta[] psms = list.toArray(new XTandemPep4Meta[list.size()]);
		return psms;
	}

	public XTandemPep4Meta[] getPSMs(double fdr) {

		XTandemPep4Meta[] psms = getPSMs();
		int[][] count = new int[50][2];

		for (XTandemPep4Meta psm : psms) {
			double score = psm.getScore();
			boolean decoy = psm.getProtein().startsWith("REV");
			if (score > 50) {
				if (decoy) {
					for (int i = 0; i < count.length; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < count.length; i++) {
						count[i][0]++;
					}
				}
			} else {
				if (decoy) {
					for (int i = 0; i < score; i++) {
						count[i][1]++;
					}
				} else {
					for (int i = 0; i < score; i++) {
						count[i][0]++;
					}
				}
			}
		}

		int threshold = -1;
		for (int i = 0; i < count.length; i++) {
			if ((double) count[i][1] / (double) count[i][0] < fdr) {
				threshold = i;
				break;
			}
		}

		if (threshold == -1) {
			return null;
		}

		ArrayList<XTandemPep4Meta> list = new ArrayList<XTandemPep4Meta>();
		for (XTandemPep4Meta psm : psms) {
			if (psm.getScore() > threshold) {
				list.add(psm);
			}
		}
		XTandemPep4Meta[] selectedPsms = list.toArray(new XTandemPep4Meta[list.size()]);

		return selectedPsms;
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return new String[] {};
	}

}
