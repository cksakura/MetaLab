/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.tpp;

import java.io.IOException;
import java.util.HashSet;
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
public class IProProtReader {

	private Iterator<Element> it;
	private Element protein_group;

	public IProProtReader(String iproProt) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(iproProt);
		Element root = document.getRootElement();
		this.it = root.elementIterator("protein_group");
	}

	public boolean hasNext() throws IOException {

		if (it.hasNext()) {
			this.protein_group = it.next();
			return true;

		} else {
			return false;
		}
	}

	public IProteinGroup next() {

		double probability = -1;
		List<Element> epros = protein_group.elements("protein");
		String[][] pronames = new String[epros.size()][];
		String[][] peptides = new String[epros.size()][];

		for (int i = 0; i < pronames.length; i++) {
			Element eproi = epros.get(i);
			String name = eproi.attributeValue("protein_name");
			int procount = Integer.parseInt(eproi.attributeValue("n_indistinguishable_proteins"));
			if (probability == -1) {
				probability = Double.parseDouble(eproi.attributeValue("probability"));
			}
			peptides[i] = eproi.attributeValue("unique_stripped_peptides").split("\\+");
			pronames[i] = new String[procount];
			pronames[i][0] = name;

			List<Element> eprosj = eproi.elements("indistinguishable_protein");
			for (int j = 0; j < eprosj.size(); j++) {
				pronames[i][j + 1] = eprosj.get(j).attributeValue("protein_name");
			}
		}

		IProteinGroup pg = new IProteinGroup(pronames, peptides, probability);
		return pg;
	}

	public HashSet<String> getAllPeptides(double threshold) throws IOException {
		HashSet<String> set = new HashSet<String>();
		while (hasNext()) {
			IProteinGroup pg = this.next();
			if (pg.probability >= threshold) {
				String[][] peptides = pg.peptides;
				for (int i = 0; i < peptides.length; i++) {
					for (int j = 0; j < peptides[i].length; j++) {
						set.add(peptides[i][j]);
					}
				}
			}
		}
		return set;
	}

	public class IProteinGroup {
		private String[][] proteins;
		private String[][] peptides;
		private double probability;

		public IProteinGroup(String[][] proteins, String[][] peptides, double probability) {
			this.proteins = proteins;
			this.peptides = peptides;
			this.probability = probability;
		}
	}
}
