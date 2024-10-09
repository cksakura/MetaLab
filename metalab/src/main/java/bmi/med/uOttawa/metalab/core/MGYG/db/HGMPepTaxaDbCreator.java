package bmi.med.uOttawa.metalab.core.MGYG.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipProResult;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipeptRequester;

public class HGMPepTaxaDbCreator {
	
	private static void create(String in, String out, String pep2taxDb, String taxAll) throws IOException {

		TaxonomyDatabase td = new TaxonomyDatabase(taxAll);

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {

			File dbFile = new File(files[i], "MGYG" + files[i].getName() + ".faa");
			File eggnogFile = new File(files[i], "MGYG" + files[i].getName() + "_eggNOG.tsv");

			if (dbFile.exists() && eggnogFile.exists()) {

				HashMap<String, String> proTaxMap = new HashMap<String, String>();
				BufferedReader eggnogReader = new BufferedReader(new FileReader(eggnogFile));
				String line = eggnogReader.readLine();
				while ((line = eggnogReader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] nogs = cs[4].split(",");
					String taxon = nogs[nogs.length - 1].split("[\\|@]")[1];
					proTaxMap.put(cs[0], taxon);
				}
				eggnogReader.close();

				HashMap<String, HashSet<String>> pepMap = new HashMap<String, HashSet<String>>();
				FastaReader reader = new FastaReader(dbFile);
				ProteinItem[] items = reader.getItems();
				for (int j = 0; j < items.length; j++) {
					String ref = items[j].getRef();
					ref = ref.substring(0, ref.indexOf(" "));
					String[] peps = Enzyme.Trypsin.digest(items[j].getSequence(), 2);
					if (proTaxMap.containsKey(ref)) {
						String tax = proTaxMap.get(ref);
						for (int k = 0; k < peps.length; k++) {
							if (pepMap.containsKey(peps[k])) {
								pepMap.get(peps[k]).add(tax);
							} else {
								HashSet<String> taxSet = new HashSet<String>();
								taxSet.add(tax);
								pepMap.put(peps[k], taxSet);
							}
						}
					} else {
						for (int k = 0; k < peps.length; k++) {
							if (!pepMap.containsKey(peps[k])) {
								pepMap.put(peps[k], new HashSet<String>());
							}
						}
					}
				}
				String[] peps = pepMap.keySet().toArray(new String[pepMap.size()]);
				String[] subPepStrings = new String[100];
				System.arraycopy(peps, 0, subPepStrings, 0, subPepStrings.length);
//				HashMap<String, UnipProResult> proMap = UnipeptRequester.pept2prot(subPepStrings);
//				System.out.println("pro\t"+proMap.size());

				HashMap<String, HashSet<String>> subMap = new HashMap<String, HashSet<String>>();
				for(String pep: subPepStrings) {
					subMap.put(pep, pepMap.get(pep));
				}
				
				PrintWriter writer = new PrintWriter(new File(out, "MGYG" + files[i].getName() + "_pep2tax_100.tsv"));

				BufferedReader pep2taxReader = new BufferedReader(new FileReader(pep2taxDb));
				String pep2taxLine = null;
				while ((pep2taxLine = pep2taxReader.readLine()) != null) {
					String[] cs = pep2taxLine.split("\t");

					if (subMap.containsKey(cs[0])) {
						HashSet<String> taxSet = subMap.get(cs[0]);
						if (taxSet.size() == 0) {
							writer.println(pep2taxLine);
						} else {
							StringBuilder sb = new StringBuilder();
							for (int j = 1; j < cs.length; j++) {
								int taxId = Integer.parseInt(cs[j]);
								Taxon taxon = td.getTaxonFromId(taxId);
								if (taxon != null) {
									int[] lineage = td.getMainParentTaxonIds(taxon);
									boolean use = false;
									for (int k = 0; k < lineage.length; k++) {
										if (taxSet.contains(String.valueOf(lineage[k]))) {
											use = true;
											break;
										}
									}
									if (use) {
										sb.append("\t").append(taxId);
									}
								}
							}

							if (sb.length() == 0) {
								for (String tax : taxSet) {
									sb.append("\t").append(tax);
								}
							}
							writer.println(cs[0] + sb);
						}
					}
				}
				pep2taxReader.close();
				writer.close();
				
				break;
			}
		}
	}
	
	
	class Pep2taxCall implements Callable<Object> {

		@Override
		public Object call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		HGMPepTaxaDbCreator.create("Z:\\Kai\\20211220_newdb", "Z:\\Kai\\HGM_DB",
				"D:\\Exported\\Resources\\pep2taxa\\pep2taxa.tab", "D:\\Exported\\Resources\\taxonomy-all.tab");
	}

}
