package bmi.med.uOttawa.metalab.task.mag.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.MGYG.db.ProDbConfig;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.mag.MagFuncSearcher;
import bmi.med.uOttawa.metalab.task.mag.MetaProteinAnnoMag;

public class MagLocalFuncSearcher extends MagFuncSearcher {

	private File eggnogFile;
	private File genomeTaxaFile;

	public MagLocalFuncSearcher(String dbpath, String defDbPath, String genomeTaxa)
			throws SQLException, NumberFormatException, IOException {
		super(dbpath, defDbPath);
		// TODO Auto-generated constructor stub
		this.eggnogFile = new File(dbpath);
		this.genomeTaxaFile = new File(genomeTaxa);
	}

	public HashMap<String, String[]> matchGenome(String[] genomes) throws IOException {

		HashMap<String, String> gnMap = new HashMap<String, String>();
		BufferedReader gtdbNcbiReader = new BufferedReader(new InputStreamReader(ResourceLoader.load("gtdb_ncbi")));
		String gnLine = gtdbNcbiReader.readLine();
		while ((gnLine = gtdbNcbiReader.readLine()) != null) {
			String[] cs = gnLine.split("\t");
			gnMap.put(cs[0], cs[1]);
		}
		gtdbNcbiReader.close();

		HashMap<String, String[]> genomeTaxaMap = new HashMap<String, String[]>();
		for (int i = 0; i < genomes.length; i++) {
			genomeTaxaMap.put(genomes[i], new String[ProDbConfig.genome2TaxaGTDBNCBITitle.length]);
		}

		if (genomeTaxaFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(genomeTaxaFile));
			String line = reader.readLine();
			int genomeId = -1;
			int lineageId = -1;
			String[] title = line.split("\t");
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("user_genome")) {
					genomeId = i;
				} else if (title[i].equals("classification")) {
					lineageId = i;
				}
			}
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (genomeTaxaMap.containsKey(cs[genomeId])) {
					String gtdb = cs[lineageId];
					ArrayList<String> list = new ArrayList<String>();
					list.add(cs[0]);

					String rank = "";
					String[] lineage = gtdb.split(";");
					for (int i = lineage.length - 1; i >= 0; i--) {
						if (lineage[i].length() > 3) {
							list.add(lineage[i].substring(3));

							char rr = lineage[i].charAt(0);

							switch (rr) {
							case 'd': {
								rank = TaxonomyRanks.Subkingdom.getName();
								break;
							}
							case 'p': {
								rank = TaxonomyRanks.Phylum.getName();
								break;
							}
							case 'c': {
								rank = TaxonomyRanks.Class.getName();
								break;
							}
							case 'o': {
								rank = TaxonomyRanks.Order.getName();
								break;
							}
							case 'f': {
								rank = TaxonomyRanks.Family.getName();
								break;
							}
							case 'g': {
								rank = TaxonomyRanks.Genus.getName();
								break;
							}
							case 's': {
								rank = TaxonomyRanks.Species.getName();
								break;
							}
							default:
								break;
							}

							break;
						}
					}
					list.add(rank);

					for (int i = 0; i < lineage.length; i++) {
						String name = "";
						if (lineage[i].length() > 3) {
							name = lineage[i].substring(3);
						}
						if (i == 0) {
							list.add(name);
							list.add("");
						} else {
							list.add(name);
						}
					}

					if (gnMap.containsKey(gtdb)) {
						String ncbi = gnMap.get(gtdb);
						rank = "";
						lineage = ncbi.split(";");
						for (int i = lineage.length - 1; i >= 0; i--) {
							if (lineage[i].length() > 3) {
								list.add(lineage[i].substring(3));

								char rr = lineage[i].charAt(0);

								switch (rr) {
								case 'd': {
									rank = TaxonomyRanks.Subkingdom.getName();
									break;
								}
								case 'p': {
									rank = TaxonomyRanks.Phylum.getName();
									break;
								}
								case 'c': {
									rank = TaxonomyRanks.Class.getName();
									break;
								}
								case 'o': {
									rank = TaxonomyRanks.Order.getName();
									break;
								}
								case 'f': {
									rank = TaxonomyRanks.Family.getName();
									break;
								}
								case 'g': {
									rank = TaxonomyRanks.Genus.getName();
									break;
								}
								case 's': {
									rank = TaxonomyRanks.Species.getName();
									break;
								}
								default:
									break;
								}

								break;
							}
						}
						list.add(rank);

						for (int i = 0; i < lineage.length; i++) {
							String name = "";
							if (lineage[i].length() > 3) {
								name = lineage[i].substring(3);
							}
							if (i == 0) {
								list.add(name);
								list.add("");
							} else {
								list.add(name);
							}
						}
					} else {
						for (int i = 0; i < 10; i++) {
							list.add("");
						}
					}

					genomeTaxaMap.put(cs[0], list.toArray(new String[list.size()]));
				}
			}
			reader.close();
		} else {
			System.out.println("Taxonomic annotation file " + genomeTaxaFile + " was not found.");
			throw new IOException("Taxonomic annotation file " + genomeTaxaFile + " was not found.");
		}

		return genomeTaxaMap;
	}

	public MetaProteinAnnoMag[] match(MetaProtein[] metapros) throws IOException, SQLException {

		ArrayList<MetaProteinAnnoMag> list = new ArrayList<MetaProteinAnnoMag>();
		HashSet<String> cogSet = new HashSet<String>();
		HashSet<String> nogSet = new HashSet<String>();
		HashSet<String> keggSet = new HashSet<String>();
		HashSet<String> goSet = new HashSet<String>();
		HashSet<String> ecSet = new HashSet<String>();

		HashMap<String, MetaProtein> proMap = new HashMap<String, MetaProtein>();
		for (MetaProtein mp : metapros) {
			proMap.put(mp.getName(), mp);
		}

		if (eggnogFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(eggnogFile));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] content = line.split("\t");
				if (proMap.containsKey(content[0])) {
					MetaProtein mp = proMap.get(content[0]);

					String[] cognogs = content[MetaProteinAnnoMag.nogID].split(",");
					String cogRoot = "";
					String cogBacteria = "";

					StringBuilder nogSb = new StringBuilder();

					for (int i = 0; i < cognogs.length; i++) {
						if (cognogs[i].startsWith("COG")) {

							if (cognogs[i].endsWith("root")) {
								cogRoot = cognogs[i].split("@")[0];
							} else if (cognogs[i].endsWith("Bacteria")) {
								cogBacteria = cognogs[i].split("@")[0];
							}
						} else {
							String nog = cognogs[i].split("\\|")[0];
							nogSb.append(cognogs[i]).append(",");
							nogSet.add(nog);
						}
					}

					String cog = "";
					if (cogRoot.length() > 0) {
						if (cogBacteria.length() > 0) {
							if (cogRoot.equals(cogBacteria)) {
								cog = cogRoot;
								cogSet.add(cogRoot);
							} else {
								cog = cogRoot + "," + cogBacteria;
								cogSet.add(cogRoot);
								cogSet.add(cogBacteria);
							}
						} else {
							cog = cogRoot;
							cogSet.add(cogRoot);
						}
					} else {
						if (cogBacteria.length() > 0) {
							cog = cogBacteria;
							cogSet.add(cogBacteria);
						}
					}

					String nog = "";
					if (nogSb.length() > 0) {
						nog = nogSb.substring(0, nogSb.length() - 1);
					}

					String[] gos = content[MetaProteinAnnoMag.goID].split(",");
					for (int i = 0; i < gos.length; i++) {
						goSet.add(gos[i]);
					}

					String[] ecs = content[MetaProteinAnnoMag.ecID].split(",");
					for (int i = 0; i < ecs.length; i++) {
						ecSet.add(ecs[i]);
					}

					String[] keggs = content[MetaProteinAnnoMag.pathwayID].split(",");
					for (int i = 0; i < keggs.length; i++) {
						keggSet.add(keggs[i]);
					}

					MetaProteinAnnoMag mpa = new MetaProteinAnnoMag(mp, content, "", cog, nog);
					list.add(mpa);
				}

			}
			reader.close();
		} else {
			System.out.println("Functional annotation file " + eggnogFile + " was not found.");
			throw new IOException("Functional annotation file " + eggnogFile + " was not found.");
		}

		getReference(cogSet, nogSet, goSet, ecSet, keggSet);

		MetaProteinAnnoMag[] mpas = list.toArray(new MetaProteinAnnoMag[list.size()]);

		return mpas;
	}

}
