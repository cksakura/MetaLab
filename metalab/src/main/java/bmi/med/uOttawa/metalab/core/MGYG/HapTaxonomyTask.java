package bmi.med.uOttawa.metalab.core.MGYG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;

public class HapTaxonomyTask {
	
	private static void getTaxonomyInfo(String in) throws IOException {

		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase("D:\\Exported\\Resources\\pep2taxa\\pep2taxa.tab", td);

		HashSet<String> excludeSet = new HashSet<String>();
		excludeSet.add("environmental samples");

		TaxonomyRanks rank = TaxonomyRanks.Family;

		HashSet<RootType> rootTypeSet = new HashSet<RootType>();
		rootTypeSet.add(RootType.Bacteria);
		rootTypeSet.add(RootType.Archaea);
		rootTypeSet.add(RootType.cellular_organisms);
		rootTypeSet.add(RootType.Eukaryota);
		rootTypeSet.add(RootType.Viroids);
		rootTypeSet.add(RootType.Viruses);

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			File peptides = new File(files[i], "peptide.txt");
			if (peptides.exists()) {
				HashSet<String> set = new HashSet<String>();
				BufferedReader reader = new BufferedReader(new FileReader(peptides));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					set.add(cs[0]);
				}
				reader.close();

				System.out.println(files[i] + "\t" + set.size());

				HashMap<String, Integer> lcaMap = pep2tax.pept2Lca(set, rank, rootTypeSet, excludeSet);

				System.out.println(files[i] + "\t" + set.size() + "\t" + lcaMap.size());

				PrintWriter writer = new PrintWriter(new File(files[i], "LCA.tsv"));

				StringBuilder tsb = new StringBuilder();
				tsb.append("Sequence").append("\t");
				tsb.append("LCA id");
				TaxonomyRanks[] ranks = TaxonomyRanks.getMainRanks();
				for (int j = 0; j < ranks.length; j++) {
					tsb.append("\t").append(ranks[j]);
				}
				writer.println(tsb);

				for (String pep : lcaMap.keySet()) {

					Integer id = lcaMap.get(pep);
					Taxon taxon = td.getTaxonFromId(id);
					int[] links = td.getMainParentTaxonIds(id);

					if (taxon != null && links != null) {

						if (links != null) {

							StringBuilder sb = new StringBuilder();
							sb.append(pep).append("\t");
							sb.append(id);

							for (int j = 0; j < links.length; j++) {
								Taxon taxoni = td.getTaxonFromId(links[j]);
								if (taxoni != null) {
									sb.append("\t").append(taxoni.getName());
								} else {
									sb.append("\t");
								}
							}

							writer.println(sb);
						}

					}
				}

				writer.close();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		HapTaxonomyTask.getTaxonomyInfo("D:\\Data\\new_db_4\\original_db");
	}

}
