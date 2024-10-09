package bmi.med.uOttawa.metalab.core.MGYG.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPsmReader;

public class HapSSDBCreator {

	private Statement stmt;
	private Connection conn;

	private static DecimalFormat df5 = new DecimalFormat("00000");
	private static int genomeCount = 4644;

	public HapSSDBCreator(String dbpath) throws SQLException {
		String url = "jdbc:sqlite:" + dbpath;

		this.conn = DriverManager.getConnection(url);
		this.stmt = conn.createStatement();
	}
	
	public Statement getStatement() {
		return stmt;
	}
	
	private static void deleteTable(String dbpath) throws SQLException {
		HapSSDBCreator creator = new HapSSDBCreator(dbpath);
		for (int i = 3; i <= genomeCount; i++) {
			String name = df5.format(i);

			StringBuilder sb = new StringBuilder();
			sb.append("drop table pep2pro_").append(name);
			creator.stmt.execute(sb.toString());
		}
	}

	private static void pfindTest(String dbin, String in) throws SQLException {
		PFindPsmReader reader = new PFindPsmReader(in);
		PFindPSM[] psms = reader.getPsms();

		StringBuilder pepsb = new StringBuilder();
		pepsb.append("(");

		HashSet<String> pepset = new HashSet<String>();
		for (int i = 0; i < psms.length; i++) {
			String sequence = psms[i].getSequence();
			if (!pepset.contains(sequence)) {
				pepsb.append("\"").append(sequence).append("\"").append(",");
				pepset.add(sequence);
			}
		}

		pepsb.deleteCharAt(pepsb.length() - 1);
		pepsb.append(")");

		System.out.println("peps\t"+psms.length+"\t"+pepset.size());
		
		HashMap<String, HashSet<String>> pepmap = new HashMap<String, HashSet<String>>();

		HapSSDBCreator searcher = new HapSSDBCreator(dbin);
		HashSet<String> set0 = new HashSet<String>();
		for (int i = 1; i <= genomeCount; i++) {
			String name = df5.format(i);

			StringBuilder sb = new StringBuilder();
			sb.append("select pep, pro from pep2pro_").append(name).append(" where pep IN ").append(pepsb);

			ResultSet rs = searcher.stmt.executeQuery(sb.toString());

//			HashSet<String> set = new HashSet<String>();

			while (rs.next()) {
//				set.add(rs.getString("pep"));
			}

//			if (set.size() > 0) {
//				pepmap.put(name, set);
//				set0.addAll(set);
//			}

			if (i % 100 == 0) {
				System.out.println(i);
			}
		}
/*
		String[] keys = pepmap.keySet().toArray(new String[pepmap.size()]);
		Arrays.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				if (pepmap.get(o1).size() > pepmap.get(o2).size()) {
					return -1;
				} else if (pepmap.get(o1).size() < pepmap.get(o2).size()) {
					return 1;
				}

				return 0;
			}
		});

		int id = 0;
		HashSet<String> allSet = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {
			allSet.addAll(pepmap.get(keys[i]));
			if (allSet.size() * 0.8 >= set0.size()) {
				id = i;
				break;
			}
		}

		System.out.println(id + "\t" + keys.length + "\t" + allSet.size() + "\t" + set0.size());

		for (int i = 0; i <= id; i++) {
			System.out.println(keys[i]);
		}
*/		
	}

	private static void pfindCreateDb0(String fileGenomeName, String in, String dbString, String out)
			throws SQLException, IOException {
		
		HashMap<String, String> fileGenomeMap = new HashMap<String, String>();
		BufferedReader fgReader = new BufferedReader(new FileReader(fileGenomeName));
		String line = null;
		while ((line = fgReader.readLine()) != null) {
			String[] cs = line.split("\t");
			fileGenomeMap.put(cs[0], cs[1]);
		}
		fgReader.close();

		PFindPsmReader reader = new PFindPsmReader(in);
		PFindPSM[] psms = reader.getPsms();

		HashSet<String> pepset = new HashSet<String>();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < psms.length; i++) {
			String sequence = psms[i].getSequence();
			pepset.add(sequence);

			String[] proStrings = psms[i].getProteins();
			for (int j = 0; j < proStrings.length; j++) {
				String[] cStrings = proStrings[j].split("_");
				if (map.containsKey(cStrings[1])) {
					map.get(cStrings[1]).add(sequence);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(sequence);
					map.put(cStrings[1], set);
				}
			}
		}

		String[] keys = map.keySet().toArray(new String[map.size()]);
		Arrays.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				if (map.get(o1).size() > map.get(o2).size()) {
					return -1;
				} else if (map.get(o1).size() < map.get(o2).size()) {
					return 1;
				}

				return 0;
			}
		});

		HashSet<String> genomeSet = new HashSet<String>();
		HashSet<String> allSet = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {

			genomeSet.add(keys[i]);
			allSet.addAll(map.get(keys[i]));

			int counti = 0;
			for (int j = 0; j < psms.length; j++) {
				if (allSet.contains(psms[j].getSequence())) {
					counti++;
				}
			}

			System.out.println(genomeSet.size() + "\t" + allSet.size() + "\t" + counti + "\t" + psms.length);
			if (counti >= psms.length * 0.8) {
				break;
			}
		}

		System.out.println(genomeSet.size());

		PrintWriter writer = new PrintWriter(out);
		File[] files = (new File(dbString)).listFiles();
		for (int i = 0; i < files.length; i++) {
			File fastaFilei = new File(files[i], "MGYG-HGUT-" + files[i].getName() + ".faa");
			BufferedReader readeri = new BufferedReader(new FileReader(fastaFilei));
			String linei = readeri.readLine();
			String name = linei.split("_")[1];
			boolean use = false;

			if (genomeSet.contains(name)) {

				if (linei.contains("ribosomal") || linei.contains("Ribosomal") || linei.contains("elongation")
						|| linei.contains("Elongation")) {
					use = false;
				} else {
					use = true;
					writer.println(linei);
				}

				while ((linei = readeri.readLine()) != null) {

					if (linei.startsWith(">")) {
						if (linei.contains("ribosomal") || linei.contains("Ribosomal") || linei.contains("elongation")
								|| linei.contains("Elongation")) {
							use = false;
						} else {
							use = true;
							writer.println(linei);
						}
					} else {
						if (use) {
							writer.println(linei);
						}
					}

				}
			}
			readeri.close();
		}
		writer.close();
	}
	
	/**
	 * extract the remain unidentified spectra
	 * @param in
	 * @param out
	 * @param spectra identified spectra which will be excluded
	 * @throws IOException
	 */
	private static void extractSpectra(String in, String out, String spectra) throws IOException {
		HashMap<String, HashSet<String>> spMap = new HashMap<String, HashSet<String>>();
		BufferedReader spReader = new BufferedReader(new FileReader(spectra));
		String spline = spReader.readLine();
		while ((spline = spReader.readLine()) != null) {
			String[] cs = spline.split("\t")[0].split("[\\.]");
			if (spMap.containsKey(cs[0])) {
				spMap.get(cs[0]).add(cs[1]);
			} else {
				HashSet<String> set = new HashSet<String>();
				set.add(cs[1]);
				spMap.put(cs[0], set);
			}
		}
		spReader.close();

		System.out.println(spMap.size());

		File[] mgfsFiles = (new File(in)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("mgf"))
					return true;

				return false;
			}
		});

		for (int i = 0; i < mgfsFiles.length; i++) {
			String name = mgfsFiles[i].getName();
//			name = name.substring(0, name.length() - "_HCDFT.mgf".length());
			name = name.substring(0, name.length() - ".mgf".length());

			HashSet<String> set = spMap.get(name);
			PrintWriter writer = new PrintWriter(new File(out, name + ".mgf"));
			BufferedReader readeri = new BufferedReader(new FileReader(mgfsFiles[i]));
			boolean use = false;

			String line = null;
			while ((line = readeri.readLine()) != null) {
				if (line.startsWith("TITLE")) {
					String[] cs = line.split("[\\.]");
					if (!set.contains(cs[1])) {
						writer.println("BEGIN IONS");
						writer.println(line);
						use = true;
					}
				} else {
					if (use) {
						writer.println(line);

						if (line.startsWith("END")) {
							use = false;
						}
					}
				}
			}
			readeri.close();
			writer.close();
		}
	}
	
	private static void tandemTest(String in, String original_db, String out) {
		File[] xmlFiles = (new File(in)).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("xml")) {
					return true;
				}
				return false;
			}
		});
		
		for(int i=0;i<xmlFiles.length;i++) {
			
		}
	}
	
	private static void getPfindPepCount(String spectra) throws IOException {
		HashMap<String, HashSet<String>> spMap = new HashMap<String, HashSet<String>>();
		BufferedReader spReader = new BufferedReader(new FileReader(spectra));
		String spline = spReader.readLine();
		while ((spline = spReader.readLine()) != null) {
			String[] cs = spline.split("\t");
			String[] scans = cs[0].split("[\\.]");
			if (spMap.containsKey(scans[0])) {
				spMap.get(scans[0]).add(cs[5]);
			} else {
				HashSet<String> set = new HashSet<String>();
				set.add(cs[5]);
				spMap.put(scans[0], set);
			}
		}
		spReader.close();
		System.out.println(spMap.size());

		for (String key : spMap.keySet()) {
			System.out.println(key + "\t" + spMap.get(key).size());
		}
	}
	
	private static void generateDb(String dbin, String spectra, String fastas, String out, String fileGenomeName)
			throws SQLException, IOException {

		HashMap<String, String> fileGenomeMap = new HashMap<String, String>();
		BufferedReader fgReader = new BufferedReader(new FileReader(fileGenomeName));
		String line = null;
		while ((line = fgReader.readLine()) != null) {
			String[] cs = line.split("\t");
			fileGenomeMap.put(cs[0], cs[1]);
		}
		fgReader.close();

		PFindPsmReader reader = new PFindPsmReader(spectra);
		PFindPSM[] psms = reader.getPsms();

		StringBuilder pepsb = new StringBuilder();
		pepsb.append("(");

		HashSet<String> genomeSet = new HashSet<String>();
		HashMap<String, Integer> pepCountMap = new HashMap<String, Integer>();

		for (int i = 0; i < psms.length; i++) {
			String sequence = psms[i].getSequence();
			if (!pepCountMap.containsKey(sequence)) {
				pepsb.append("\"").append(sequence).append("\"").append(",");
				pepCountMap.put(sequence, 1);

				String[] pros = psms[i].getProteins();
				for (int j = 0; j < pros.length; j++) {
					String[] cs = pros[j].split("_");
					genomeSet.add(fileGenomeMap.get(cs[1]));
				}
			} else {
				pepCountMap.put(sequence, pepCountMap.get(sequence) + 1);
			}
		}

		pepsb.deleteCharAt(pepsb.length() - 1);
		pepsb.append(")");

		System.out.println("peps\t" + psms.length + "\t" + pepCountMap.size() + "\t" + genomeSet.size());

		HashMap<String, HashSet<String>> genomePepMap = new HashMap<String, HashSet<String>>();
		HapSSDBCreator searcher = new HapSSDBCreator(dbin);

		for (int i = 1; i <= genomeCount; i++) {
			String name = df5.format(i);

			if (genomeSet.contains(name)) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("select pep, pro from pep2pro_").append(name).append(" where pep IN ").append(pepsb);

			ResultSet rs = searcher.stmt.executeQuery(sb.toString());

			HashSet<String> pSet = new HashSet<String>();
			while (rs.next()) {
				pSet.add(rs.getString("pep"));
			}

			if (pSet.size() > 0) {
				genomePepMap.put(name, pSet);
			}

			if (i % 100 == 0) {
				System.out.println(i);
			}
		}

		String[] keys = genomePepMap.keySet().toArray(new String[genomePepMap.size()]);
		Arrays.sort(keys, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				if (genomePepMap.get(o1).size() > genomePepMap.get(o2).size()) {
					return -1;
				} else if (genomePepMap.get(o1).size() < genomePepMap.get(o2).size()) {
					return 1;
				}

				return 0;
			}
		});

		PrintWriter writer = new PrintWriter(out);
		HashSet<String> usedGenomeSet = new HashSet<String>();
		HashSet<String> allSet = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {

			usedGenomeSet.add(keys[i]);
			
			HashSet<String> pSet = genomePepMap.get(keys[i]);
			allSet.addAll(pSet);

			int counti = 0;
			for (String pep : allSet) {
				counti += pepCountMap.get(pep);
			}

			writer.println(keys[i] + "\t" + usedGenomeSet.size() + "\t" + allSet.size() + "\t" + counti + "\t"
					+ psms.length + "\t" + (double) counti / (double) psms.length);

//			if (counti >= psms.length * 0.8) {
//				break;
//			}
		}
		writer.close();
		System.out.println(genomeSet.size());

/*		
		PrintWriter writer = new PrintWriter(out);
		File[] files = (new File(fastas)).listFiles();
		for (int i = 0; i < files.length; i++) {
			File fastaFilei = new File(files[i], "MGYG-HGUT-" + files[i].getName() + ".faa");
			BufferedReader readeri = new BufferedReader(new FileReader(fastaFilei));
			String linei = readeri.readLine();
			String name = linei.split("_")[1];
			boolean use = false;

			if (genomeSet.contains(name)) {

				if (linei.contains("ribosomal") || linei.contains("Ribosomal") || linei.contains("elongation")
						|| linei.contains("Elongation")) {
					use = false;
				} else {
					use = true;
					writer.println(linei);
				}

				while ((linei = readeri.readLine()) != null) {

					if (linei.startsWith(">")) {
						if (linei.contains("ribosomal") || linei.contains("Ribosomal") || linei.contains("elongation")
								|| linei.contains("Elongation")) {
							use = false;
						} else {
							use = true;
							writer.println(linei);
						}
					} else {
						if (use) {
							writer.println(linei);
						}
					}

				}
			}
			readeri.close();
		}
		writer.close();
*/		
	}
	
	private static void write(String fastas, String genome, String out) throws NumberFormatException, IOException {

		HashSet<String> genomeSet = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(genome));
		String lineString = null;
		while ((lineString = reader.readLine()) != null) {
			String[] cStrings = lineString.split("\t");
			genomeSet.add(cStrings[0]);
			if (Double.parseDouble(cStrings[cStrings.length - 1]) > 0.6) {
				break;
			}
		}
		reader.close();

		PrintWriter writer = new PrintWriter(out);
		File[] files = (new File(fastas)).listFiles();
		for (int i = 0; i < files.length; i++) {

			if (genomeSet.contains(files[i].getName())) {

				File fastaFilei = new File(files[i], "MGYG-HGUT-" + files[i].getName() + ".faa");
				BufferedReader readeri = new BufferedReader(new FileReader(fastaFilei));
				String linei = null;
				boolean use = false;

				while ((linei = readeri.readLine()) != null) {

					if (linei.startsWith(">")) {
						if (linei.contains("ribosomal") || linei.contains("Ribosomal") || linei.contains("elongation")
								|| linei.contains("Elongation")) {
							use = false;
						} else {
							use = true;
							writer.println(linei);
						}
					} else {
						if (use) {
							writer.println(linei);
						}
					}

				}

				readeri.close();
			}
		}
		writer.close();
	}
	
	private static void exportTaxa(String metadata, String genomeName, String output) throws IOException {

		HashMap<String, String> genomeNameMap = new HashMap<String, String>();
		BufferedReader genomeNameReader = new BufferedReader(new FileReader(genomeName));
		String genomeLine = null;
		while ((genomeLine = genomeNameReader.readLine()) != null) {
			String[] cs = genomeLine.split("\t");
			genomeNameMap.put("GUT_" + cs[0], "MGYG-HGUT-" + cs[1]);
		}
		genomeNameReader.close();

		PrintWriter writer = new PrintWriter(output);
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Genome\t");
		titlesb.append("Accession\t");
		titlesb.append("Name\t");
		titlesb.append("Rank");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			titlesb.append("\t").append(mainRanks[i].getName());
		}
		writer.println(titlesb);

		BufferedReader metaReader = new BufferedReader(new FileReader(metadata));
		String metaLine = metaReader.readLine();
		String[] metaTitle = metaLine.split("\t");
		int lineageId = -1;
		for (int i = 0; i < metaTitle.length; i++) {
			if (metaTitle[i].equals("Lineage")) {
				lineageId = i;
			}
		}

		while ((metaLine = metaReader.readLine()) != null) {
			String[] content = metaLine.split("\t");
			if (content.length == metaTitle.length) {

				if (genomeNameMap.containsKey(content[0])
						&& genomeNameMap.get(content[0]).equals(content[lineageId - 1])) {

					String lieage = content[lineageId];
					String[] cs = lieage.split(";");

					StringBuilder sb = new StringBuilder();
					String genome = content[0];
					String accession = content[lineageId - 1];

					genome = genome.substring(genome.indexOf("_") + 1);
					accession = accession.substring(accession.lastIndexOf("-") + 1);

					sb.append(genome).append("\t");
					sb.append(accession).append("\t");

					String last = "";
					String rank = "";

					StringBuilder taxsb = new StringBuilder();
					for (int i = 0; i < cs.length; i++) {
						if (cs[i].length() > 3) {
							String taxonName = cs[i].substring(3);
/*							
							int lastLine = taxonName.lastIndexOf("_");
							if (lastLine > 0) {
								taxonName = taxonName.substring(0, lastLine);
							}
*/
							last = taxonName;

							if (i == 0) {
								taxsb.append("\t").append(taxonName).append("\t");
								rank = mainRanks[i].getName();
							} else {
								taxsb.append("\t").append(taxonName);
								rank = mainRanks[i + 1].getName();
							}
						} else {
							taxsb.append("\t");
						}
					}
					
					sb.append(last).append("\t");
					sb.append(rank);
					sb.append(taxsb);
					
					writer.println(sb);
				}
			}
		}
		metaReader.close();
		writer.close();
	}
	
	private static void exportTaxa(String metadata, String output) throws IOException {

//		HashMap<String, String> genomeNameMap = new HashMap<String, String>();

		PrintWriter writer = new PrintWriter(output);
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Genome\t");
		titlesb.append("Name\t");
		titlesb.append("Rank");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			titlesb.append("\t").append(mainRanks[i].getName());
		}
		writer.println(titlesb);

		BufferedReader metaReader = new BufferedReader(new FileReader(metadata));
		String metaLine = metaReader.readLine();
		String[] metaTitle = metaLine.split("\t");
		int lineageId = -1;
		for (int i = 0; i < metaTitle.length; i++) {
			if (metaTitle[i].equals("Lineage")) {
				lineageId = i;
			}
		}

		HashMap<String, String> usedMap = new HashMap<String, String>();
		while ((metaLine = metaReader.readLine()) != null) {
			String[] content = metaLine.split("\t");
			if (content.length == metaTitle.length) {

				String genome = content[lineageId - 1];
				String lieage = content[lineageId];

				if (usedMap.containsKey(genome)) {
					if (usedMap.get(genome).length() >= lieage.length()) {
						continue;
					}
				}
				usedMap.put(genome, lieage);
			}
		}

		String[] genomes = usedMap.keySet().toArray(new String[usedMap.size()]);
		Arrays.sort(genomes);

		for (String genome : genomes) {
			String lieage = usedMap.get(genome);

			StringBuilder sb = new StringBuilder();

			sb.append(genome).append("\t");

			String last = "";
			String rank = "";

			String[] cs = lieage.split(";");
			StringBuilder taxsb = new StringBuilder();
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].length() > 3) {
					String taxonName = cs[i].substring(3);
					/*
					 * int lastLine = taxonName.lastIndexOf("_"); if (lastLine > 0) { taxonName =
					 * taxonName.substring(0, lastLine); }
					 */
					last = taxonName;

					if (i == 0) {
						taxsb.append("\t").append(taxonName).append("\t");
						rank = mainRanks[i].getName();
					} else {
						taxsb.append("\t").append(taxonName);
						rank = mainRanks[i + 1].getName();
					}
				} else {
					taxsb.append("\t");
				}
			}

			sb.append(last).append("\t");
			sb.append(rank);
			sb.append(taxsb);

			writer.println(sb);

		}

		metaReader.close();
		writer.close();
	}
	
	private static void exportGTDBNcbiTaxa(String gtdb_a, String gtdb_b, String output)
			throws IOException {

		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader archaeaReader = new BufferedReader(new FileReader(gtdb_a));
		String aLine = archaeaReader.readLine();
		String[] title = aLine.split("\t");
		int gtdb = -1;
		int ncbi = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("gtdb_taxonomy")) {
				gtdb = i;
			} else if (title[i].equals("ncbi_taxonomy")) {
				ncbi = i;
			}
		}
		while ((aLine = archaeaReader.readLine()) != null) {
			String[] cs = aLine.split("\t");
			map.put(cs[gtdb], cs[ncbi]);
		}
		archaeaReader.close();
		System.out.println(map.size());

		BufferedReader bacteriaReader = new BufferedReader(new FileReader(gtdb_b));
		String bLine = bacteriaReader.readLine();
		title = bLine.split("\t");
		gtdb = -1;
		ncbi = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("gtdb_taxonomy")) {
				gtdb = i;
			} else if (title[i].equals("ncbi_taxonomy")) {
				ncbi = i;
			}
		}
		while ((bLine = bacteriaReader.readLine()) != null) {
			String[] cs = bLine.split("\t");
			map.put(cs[gtdb], cs[ncbi]);
		}
		bacteriaReader.close();
		System.out.println(map.size());

		PrintWriter writer = new PrintWriter(output);
		writer.println("gtdb_taxonomy\tncbi_taxonomy");
		for (String key : map.keySet()) {
			writer.println(key + "\t" + map.get(key));
		}
		writer.close();
	}
	
	private static void exportGTDBNcbiTaxa(String metadata, String gtdb_a, String gtdb_b, String output)
			throws IOException {

		PrintWriter writer = new PrintWriter(output);
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Genome\t");
		titlesb.append("GTDB_Name\t");
		titlesb.append("GTDB_Rank");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			titlesb.append("\t").append("GTDB_").append(mainRanks[i].getName());
		}

		titlesb.append("\tNCBI_Name");
		titlesb.append("\tNCBI_Rank");
		for (int i = 0; i < mainRanks.length; i++) {
			titlesb.append("\t").append("NCBI_").append(mainRanks[i].getName());
		}

		writer.println(titlesb);

		HashMap<String, String> archaeaMap = new HashMap<String, String>();
		HashMap<String, String> bacteriaMap = new HashMap<String, String>();
		BufferedReader archaeaReader = new BufferedReader(new FileReader(gtdb_a));
		String aLine = archaeaReader.readLine();
		while ((aLine = archaeaReader.readLine()) != null) {
			String[] cs = aLine.split("\t");
			archaeaMap.put(cs[3].replaceAll("; ", ";"), cs[2].replaceAll("; ", ";"));
		}
		archaeaReader.close();

		BufferedReader bacteriaReader = new BufferedReader(new FileReader(gtdb_b));
		String bLine = bacteriaReader.readLine();
		while ((bLine = bacteriaReader.readLine()) != null) {
			String[] cs = bLine.split("\t");
			bacteriaMap.put(cs[3].replaceAll("; ", ";"), cs[2].replaceAll("; ", ";"));
		}
		bacteriaReader.close();
		System.out.println(archaeaMap.size() + "\t" + bacteriaMap.size());

		BufferedReader metaReader = new BufferedReader(new FileReader(metadata));
		String metaLine = metaReader.readLine();
		String[] metaTitle = metaLine.split("\t");
		int lineageId = -1;
		for (int i = 0; i < metaTitle.length; i++) {
			if (metaTitle[i].equals("Lineage")) {
				lineageId = i;
			}
		}

		HashMap<String, String> usedMap = new HashMap<String, String>();
		while ((metaLine = metaReader.readLine()) != null) {
			String[] content = metaLine.split("\t");
			if (content.length == metaTitle.length) {

				String genome = content[lineageId - 1];
				String lieage = content[lineageId];

				if (usedMap.containsKey(genome)) {
					if (usedMap.get(genome).length() >= lieage.length()) {
						continue;
					}
				}
				usedMap.put(genome, lieage);
			}
		}

		String[] genomes = usedMap.keySet().toArray(new String[usedMap.size()]);
		Arrays.sort(genomes);

		int count = 0;
		for (String genome : genomes) {
			String lieage = usedMap.get(genome);

			StringBuilder sb = new StringBuilder();

			sb.append(genome).append("\t");

			String[] cs = lieage.split(";");

			String gtdblast = "";
			String gtdbrank = "";
			StringBuilder gtdbtaxsb = new StringBuilder();
			for (int i = 0; i < cs.length; i++) {
				if (cs[i].length() > 3) {
					String taxonName = cs[i].substring(3);
					gtdblast = taxonName;
					if (i == 0) {
						gtdbtaxsb.append("\t").append(taxonName).append("\t");
						gtdbrank = mainRanks[i].getName();
					} else {
						gtdbtaxsb.append("\t").append(taxonName);
						gtdbrank = mainRanks[i + 1].getName();
					}
				} else {
					gtdbtaxsb.append("\t");
				}
			}

			sb.append(gtdblast).append("\t");
			sb.append(gtdbrank);
			sb.append(gtdbtaxsb).append("\t");

			String ncbi = null;
			if (archaeaMap.containsKey(lieage)) {
				ncbi = archaeaMap.get(lieage);
			} else if (bacteriaMap.containsKey(lieage)) {
				ncbi = bacteriaMap.get(lieage);
			}

			if (ncbi != null) {

				cs = ncbi.split(";");

				String ncbilast = "";
				String ncbirank = "";
				StringBuilder ncbitaxsb = new StringBuilder();
				for (int i = 0; i < cs.length; i++) {
					if (cs[i].length() > 3) {
						String taxonName = cs[i].substring(3);
						ncbilast = taxonName;
						if (i == 0) {
							ncbitaxsb.append("\t").append(taxonName).append("\t");
							ncbirank = mainRanks[i].getName();
						} else {
							ncbitaxsb.append("\t").append(taxonName);
							ncbirank = mainRanks[i + 1].getName();
						}
					} else {
						ncbitaxsb.append("\t");
					}
				}

				sb.append(ncbilast).append("\t");
				sb.append(ncbirank);
				sb.append(ncbitaxsb);
			} else {
				if (!gtdbrank.equals("Species")) {
					count++;
				}
			}

			writer.println(sb);
		}
		System.out.println(count);
		metaReader.close();
		writer.close();
	}
	
	private static void matchTaxa(String taxon_all, String metadata, String genomeName, String output) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Genome\t");
		titlesb.append("Accession\t");
		titlesb.append("Name\t");
		titlesb.append("Rank");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			titlesb.append("\t").append(mainRanks[i].getName());
		}
		writer.println(titlesb);
		
		HashMap<String, String> genomeNameMap = new HashMap<String, String>();
		BufferedReader genomeNameReader = new BufferedReader(new FileReader(genomeName));
		String genomeLine = null;
		while ((genomeLine = genomeNameReader.readLine()) != null) {
			String[] cs = genomeLine.split("\t");
			genomeNameMap.put("GUT_" + cs[0], "MGYG-HGUT-" + cs[1]);
		}
		genomeNameReader.close();
		System.out.println("namemap\t" + genomeNameMap.size());

		TaxonomyDatabase td = new TaxonomyDatabase(taxon_all);
		HashMap<Integer, Taxon> taxonMap = td.getTaxonMap();
		HashMap<String, Integer>[] rankNameIdMaps = td.getRankNameMap();

		BufferedReader metaReader = new BufferedReader(new FileReader(metadata));
		String metaLine = metaReader.readLine();
		String[] metaTitle = metaLine.split("\t");
		int lineageId = -1;
		for (int i = 0; i < metaTitle.length; i++) {
			if (metaTitle[i].equals("Lineage")) {
				lineageId = i;
			}
		}

		int genomeCount = 0;
		int[] findSpecies = new int[4];
		HashSet<String> set = new HashSet<String>();
		while ((metaLine = metaReader.readLine()) != null) {
			String[] content = metaLine.split("\t");
			if (content.length == metaTitle.length) {

				if (genomeNameMap.containsKey(content[0])) {
					genomeCount++;
					String lieage = content[lineageId];
					String[] cs = lieage.split(";");

					int[] mainIds = new int[8];
					Arrays.fill(mainIds, -1);

					for (int i = 0; i < cs.length; i++) {

						if (cs[i].length() > 3) {
							String taxonName = cs[i].substring(3);
							int lastLine = taxonName.lastIndexOf("_");
							if (lastLine > 0) {
								taxonName = taxonName.substring(0, lastLine);
							}

							if (i == 0) {
								if (rankNameIdMaps[i].containsKey(taxonName)) {
									int taxId = rankNameIdMaps[i].get(taxonName);
									Taxon taxon = taxonMap.get(taxId);
									if (taxon != null) {
										mainIds[0] = taxId;
									}
								}
							} else {
								if (rankNameIdMaps[i + 1].containsKey(taxonName)) {
									int taxId = rankNameIdMaps[i + 1].get(taxonName);
									Taxon taxon = taxonMap.get(taxId);
									if (taxon != null) {

										int[] taxIds = td.getMainParentTaxonIds(taxon);
										if (taxIds != null) {
											boolean miss = false;
											for (int j = 0; j < i; j++) {
												if (mainIds[j] != taxIds[j]) {
													miss = true;
//													System.out.println("wrong\t" + mainIds[j] + "\t" + taxIds[j]);
												}
											}

											if (!miss) {
												mainIds[i + 1] = taxId;
											}
										}
									}
								} else {
									System.out.println((i + 1) + "\t" + taxonName);
								}
							}
						} else {
							break;
						}
					}
					
					System.out.println(Arrays.toString(mainIds));
				}
			}
		}
		metaReader.close();
		System.out.println(Arrays.toString(findSpecies));
		System.out.println(genomeCount + "\t" + set.size());
		
		writer.close();
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
//		HapSSDBCreator.deleteTable("D:\\Data\\new_db_4\\catalog.db");
		
//		HapSSDBCreator.exportTaxa("D:\\Data\\new_db_4\\genomes-nr_metadata.tsv", 
//				"D:\\Data\\new_db_4\\GenomeName.txt", "D:\\Data\\new_db_4\\GenomeTaxa.tsv");

//		HapSSDBCreator.exportGTDBNcbiTaxa("Z:\\Kai\\20211220_newdb\\genomes-all_metadata.tsv",
//				"D:\\Database\\GTDB\\gtdb-archaea.tsv", "D:\\Database\\GTDB\\gtdb-bacteria.tsv",
//				"Z:\\Kai\\20211220_newdb\\GTDB_NCBI_taxa.tsv");
		
		HapSSDBCreator.exportGTDBNcbiTaxa("D:\\Database\\GTDB\\ar53_metadata_r207.tsv", 
				"D:\\Database\\GTDB\\bac120_metadata_r207.tsv", "D:\\Database\\GTDB\\gtdb_ncbi.txt");

//		HapSSDBCreator.exportTaxa("C:\\Users\\MEDTECHU0188\\Downloads\\temp\\genomes-all_metadata.tsv", 
//				"C:\\Users\\MEDTECHU0188\\Downloads\\temp\\GenomeTaxa.tsv");
		
//		HapSSDBCreator.matchTaxa("D:\\Exported\\Resources\\taxonomy-all.tab", 
//				"D:\\Data\\new_db_4\\genomes-nr_metadata.tsv", "D:\\Data\\new_db_4\\GenomeName.txt", "");

//		HapSSDBCreator.pfindCreateDb0("D:\\Data\\new_db_4\\catalog.db",
//				"D:\\Data\\MetaLab_test\\human\\pfind\\pFindTask\\result\\pFind-Filtered.spectra",
//				"D:\\Data\\new_db_4\\original_db",
//				"D:\\Data\\MetaLab_test\\human\\pfind\\pFindTask\\result\\sample_specific.fasta");

//		HapSSDBCreator.extractSpectra("D:\\Data\\MetaLab_test\\human\\iterator_1", 
//				"D:\\Data\\MetaLab_test\\human\\iterator_2", 
//				"D:\\Data\\MetaLab_test\\human\\iterator_1\\pFind-Filtered.spectra");
		
//		HapSSDBCreator.extractSpectra("D:\\Data\\MetaLab_test\\human\\iterator_3", 
//				"D:\\Data\\MetaLab_test\\human\\iterator_4", 
//				"D:\\Data\\MetaLab_test\\human\\iterator_3\\pFind-Filtered.spectra");
		
//		HapSSDBCreator.getPfindPepCount("D:\\Data\\MetaLab_test\\human\\iterator_1\\pFind-Filtered.spectra");
		
//		HapSSDBCreator.getPfindPepCount("D:\\Data\\MetaLab_test\\human\\pfind\\pFindTask\\result\\pFind-Filtered.spectra");
/*
		HapSSDBCreator.generateDb("D:\\Data\\new_db_4\\catalog.db",
				"D:\\Data\\MetaLab_test\\human\\iterator_3\\pFind-Filtered.spectra", "D:\\Data\\new_db_4\\original_db",
				"D:\\Data\\MetaLab_test\\human\\iterator_4\\genomes.txt",
				"D:\\Data\\new_db_4\\fileName_genomeName.txt");

		HapSSDBCreator.write("D:\\Data\\new_db_4\\original_db",
				"D:\\Data\\MetaLab_test\\human\\iterator_4\\genomes.txt",
				"D:\\Data\\MetaLab_test\\human\\iterator_4\\sample_specific.fasta");
*/
	}
}
