/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

/**
 * @author Kai Cheng
 *
 */
public class UnipCsvReader {

	private static final String[] ranks = { "superkingdom", "kingdom", "phylum", "class_", "order", "family", "genus",
			"species" };
	private static final int[] rankIds = TaxonomyRanks.getMainRankIds();
	private static int[] rankColumnIds = new int[8];

	private File input;
	private TaxonomyDatabase taxDb;
	private HashMap<String, Integer> taxNameMap;

	private HashMap<String, Taxon> pepTaxonMap;

	public UnipCsvReader(String in) throws IOException {
		this(new File(in));
	}

	public UnipCsvReader(File in) throws IOException {
		this.input = in;
		this.taxDb = new TaxonomyDatabase();
		this.taxNameMap = TaxonomyDatabase.parseFullNameMap();
		this.read();
	}

	public UnipCsvReader(String in, TaxonomyDatabase taxDb, HashMap<String, Integer> taxNameMap) throws IOException {
		this(new File(in), taxDb, taxNameMap);
	}

	public UnipCsvReader(File in, TaxonomyDatabase taxDb, HashMap<String, Integer> taxNameMap) throws IOException {
		this.input = in;
		this.taxDb = taxDb;
		this.taxNameMap = taxNameMap;
		this.read();
	}

	private void read() throws IOException {
		this.pepTaxonMap = new HashMap<String, Taxon>();

		BufferedReader reader = new BufferedReader(new FileReader(input));
		String title = reader.readLine();
		parseTitle(title);

		String line = null;
		while ((line = reader.readLine()) != null) {
			parse(line);
		}
		reader.close();
	}

	private void parseTitle(String line) {
		String[] cs = line.split(",");
		for (int i = 0; i < cs.length; i++) {
			for (int j = 0; j < ranks.length; j++) {
				if (cs[i].equals(ranks[j])) {
					rankColumnIds[j] = i;
				}
			}
		}
	}

	private void parse(String line) {
		String[] cs = line.split(",");
		String taxName = null;

		for (int i = 0; i < rankColumnIds.length; i++) {

			if (cs[rankColumnIds[i]].length() == 0 || cs[rankColumnIds[i]].charAt(0) == '"') {
				continue;
			}

			taxName = cs[rankColumnIds[i]] + "_" + rankIds[i];
		}

		if (taxName != null) {

			Taxon taxon = this.taxDb.getTaxonFromName(taxName);
			if (taxon != null) {
				pepTaxonMap.put(cs[0], taxon);
			} else {
				Integer taxonId = this.taxNameMap.get(taxName);
				if (taxonId != null) {
					taxon = this.taxDb.getTaxonFromId(taxonId);
					if (taxon != null) {
						pepTaxonMap.put(cs[0], taxon);
					}
				} else {
					System.err.println(taxName + " is not found in taxonomy-all.tab; sequence=" + cs[0]);
				}
			}

		} else {
			pepTaxonMap.put(cs[0], Taxon.root);
		}
	}

	/**
	 * 
	 * @param output
	 * @throws IOException
	 */
	public void exportTree(String output) throws IOException {

	}

	public HashMap<String, Taxon> getPepTaxonMap() throws IOException {
		return pepTaxonMap;
	}

}
