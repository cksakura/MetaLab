package bmi.med.uOttawa.metalab.core.MGYG.db;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
//import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.function.v2.ECReader;
import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.core.function.v2.GoOboReader;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;

public class ProDbConfig {

	public static final String[] PRO2FUNTITLE_STRINGS = new String[] { "query_name", "pro_name", "tax_id", "best_tax_level",
			"Preferred_name", "GOs", "EC", "KEGG_ko", "KEGG_Pathway", "KEGG_Module", "KEGG_Reaction", "KEGG_rclass",
			"BRITE", "KEGG_TC", "CAZy", "BiGG_Reaction", "COG_Id", "NOG_Id"};
	
	public static final String[] genome2TaxaTitle = new String[] { "Genome", "Name", "Rank", "Superkingdom", "Kingdom_",
			"Phylum_", "Class_", "Order_", "Family_", "Genus_", "Species_"};

	public static final String[] genome2TaxaGTDBNCBITitle = new String[] { "Genome", "GTDB_Name", "GTDB_Rank",
			"GTDB_Superkingdom", "GTDB_Kingdom", "GTDB_Phylum", "GTDB_Class", "GTDB_Order", "GTDB_Family", "GTDB_Genus",
			"GTDB_Species", "NCBI_Name", "NCBI_Rank", "NCBI_Superkingdom", "NCBI_Kingdom", "NCBI_Phylum", "NCBI_Class",
			"NCBI_Order", "NCBI_Family", "NCBI_Genus", "NCBI_Species" };

	/**
	 * http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-gut/v1.0/uhgg_catalogue/
	 * 
	 * @param out
	 * @throws IOException
	 */
	private static void download(String out) throws IOException {

		DecimalFormat df1 = new DecimalFormat("0000000");
		DecimalFormat df2 = new DecimalFormat("000000000");

		for (int i = 0; i <= 49; i++) {

			for (int j = 0; j <= 99; j++) {

				int idj = i * 100 + j;
				if (idj == 0) {
					continue;
				} else if (idj == 4906) {
					break;
				}

				String lineString = "http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-gut/v2.0/species_catalogue/";
				lineString += "MGYG";
				lineString += df1.format(i);
				lineString += "/";

				String geneId = df2.format(idj);

				lineString += "MGYG";
				lineString += df2.format(idj);
				lineString += "/";

				System.out.println(geneId);

				File outputFile = new File(out, geneId);
				if (outputFile.exists()) {
					File downloadFile = new File(outputFile, "download.txt");
					if (!downloadFile.exists()) {
						PrintWriter writer = new PrintWriter(downloadFile);

						lineString += "genome/";

						writer.println(lineString + "MGYG" + geneId + ".faa");
						writer.println(lineString + "MGYG" + geneId + "_InterProScan.tsv");
						writer.println(lineString + "MGYG" + geneId + "_eggNOG.tsv");
						writer.println(lineString + "MGYG" + geneId + "_annotation_coverage.tsv");
						writer.println(lineString + "MGYG" + geneId + "_cog_summary.tsv");
						writer.println(lineString + "MGYG" + geneId + "_kegg_classes.tsv");
						writer.print(lineString + "MGYG" + geneId + "_kegg_modules.tsv");

						writer.close();
					}
					File batFile = new File(outputFile, "download.bat");
					if (!batFile.exists()) {
						PrintWriter batWriter = new PrintWriter(batFile);

						batWriter.println("cd " + outputFile);
						batWriter.println("wget -i " + downloadFile);
						batWriter.close();
					}
				} else {
					outputFile.mkdirs();

					File downloadFile = new File(outputFile, "download.txt");
					PrintWriter writer = new PrintWriter(downloadFile);

					lineString += "genome/";

					writer.println(lineString + "MGYG" + geneId + ".faa");
					writer.println(lineString + "MGYG" + geneId + "_InterProScan.tsv");
					writer.println(lineString + "MGYG" + geneId + "_eggNOG.tsv");
					writer.println(lineString + "MGYG" + geneId + "_annotation_coverage.tsv");
					writer.println(lineString + "MGYG" + geneId + "_cog_summary.tsv");
					writer.println(lineString + "MGYG" + geneId + "_kegg_classes.tsv");
					writer.print(lineString + "MGYG" + geneId + "_kegg_modules.tsv");

					writer.close();

					File batFile = new File(outputFile, "download.bat");
					PrintWriter batWriter = new PrintWriter(batFile);

					batWriter.println("cd " + outputFile);
					batWriter.println("wget -i " + downloadFile);
					batWriter.close();
				}
			}
		}

		String downloadBatString = out + "\\download.bat";
		PrintWriter writer = new PrintWriter(downloadBatString);
		writer.println("E:");

		File[] files = (new File(out)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {

				File downloadFile = new File(files[i], "download.txt");
				if (downloadFile.exists()) {
					if (files[i].listFiles().length != 9) {
						writer.println("cd " + files[i]);
						writer.println("wget -i " + downloadFile.getAbsolutePath());
					}
				}
			}
		}
		writer.close();
	}

	private static void digest(String in) throws IOException {
		Enzyme trypsin = Enzyme.Trypsin;
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {

			File pepFile = new File(files[i], "peptide.txt");
			if (pepFile.exists()) {
				continue;
			}

			File fastaFile = new File(files[i], "MGYG-HGUT-" + files[i].getName() + ".faa");
			FastaReader fr = new FastaReader(fastaFile);
			ProteinItem[] pros = fr.getItems();

			HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
			for (int j = 0; j < pros.length; j++) {
				String pronameString = pros[j].getRef();
				pronameString = pronameString.split("[ _]")[2];
				String[] pepStrings = trypsin.digest(pros[j].getSequence(), 2);
				for (String pep : pepStrings) {
					if (pep.length() > 40) {
						continue;
					}
					if (map.containsKey(pep)) {
						map.get(pep).add(pronameString);
					} else {
						HashSet<String> set = new HashSet<>();
						set.add(pronameString);
						map.put(pep, set);
					}
				}
			}

			System.out.println(fastaFile + "\t" + pros.length + "\t" + map.size());

			PrintWriter writer = new PrintWriter(pepFile);
			for (String keyString : map.keySet()) {
				StringBuilder sBuilder = new StringBuilder(keyString);
				HashSet<String> set = map.get(keyString);

				for (String pro : set) {
					sBuilder.append("\t").append(pro);
				}
				writer.println(sBuilder);
			}
			writer.close();
		}
	}

	private static void generatePepDb(String in, String out) throws IOException {
		HashSet<String> set = new HashSet<String>();
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < 500; i++) {

			File pepFile = new File(files[i], "peptide.txt");
			if (pepFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(pepFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					set.add(cs[0]);
				}
				reader.close();
			}
		}

		int id = 1;
		PrintWriter writer = new PrintWriter(out);
		for (String pepString : set) {
			writer.println(">Protein_" + (id++));
			writer.println(pepString);
		}
		writer.close();
	}

	private static void createSqliteDb(String in) {
		String url = "jdbc:sqlite:Z:/Kai/20211220_newdb/" + in;

		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
//				DatabaseMetaData meta = conn.getMetaData();
//				System.out.println("The driver name is " + meta.getDriverName());
//				System.out.println("A new database has been created.");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void createPep2ProTable(String in) throws NumberFormatException, IOException {
		// SQLite connection string
		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {

			String name = "pep2pro_" + files[i].getName();
			File fileiFile = new File(files[i], "peptide.txt");

			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n pep VARCHAR(100),\n");
			sb.append(" " + "pro" + " text NOT NULL,\n");
			sb.append("PRIMARY KEY (pep));\n");

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (pep,pro)\n").append("VALUES\n");

			BufferedReader reader = new BufferedReader(new FileReader(fileiFile));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				sb2.append("(\"").append(cs[0]).append("\"").append(",").append("\"").append(cs[1]);
				for (int j = 2; j < cs.length; j++) {
					sb2.append("_").append(cs[j]);
				}
				sb2.append("\"),");
			}

			sb2.deleteCharAt(sb2.length() - 1);

			reader.close();

			if (i % 100 == 0) {
				System.out.println(name);
			}

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb.toString());
				stmt.execute(sb2.toString());
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static void dropTable(String in) {
		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		DecimalFormat df5 = new DecimalFormat("00000");
		for (int i = 1; i <= 436; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS \"").append("pro2fun_").append(df5.format(i)).append("\";\n");

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb.toString());
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static void createPro2FuncTable(String in, String usedTitles) throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		BufferedReader titlereader = new BufferedReader(new FileReader(usedTitles));
		String uline = null;
		while ((uline = titlereader.readLine()) != null) {
			int id = uline.indexOf(".");
			int number = Integer.parseInt(uline.substring(0, id));

			map.put(number, uline.substring(id + 1).trim());
		}
		titlereader.close();

		System.out.println(map);

		int titleLenght = 22;

		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {

			HashMap<String, String> fastaMap = new HashMap<String, String>();
			BufferedReader freader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG-HGUT-" + files[i].getName() + ".faa")));

			String frline = null;
			while ((frline = freader.readLine()) != null) {
				if (frline.startsWith(">")) {
					String[] cs = frline.split("_");
					int id = cs[2].indexOf(" ");
					fastaMap.put(cs[2].substring(0, id), cs[2].substring(id + 1));
				}
			}
			freader.close();

			String name = "pro2fun_" + files[i].getName();

			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG-HGUT-" + files[i].getName() + "_eggNOG.tsv")));
			String line = reader.readLine();

			StringBuilder sb1 = new StringBuilder();
			StringBuilder titlesb = new StringBuilder("query_name,pro_name");

			sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n query_name VARCHAR(100),\n");
			sb1.append(" pro_name text NOT NULL,\n");

			for (int j = 1; j <= titleLenght; j++) {

				if (map.containsKey(j + 1)) {
					sb1.append(" " + map.get(j + 1) + " text NOT NULL,\n");
					titlesb.append(",").append(map.get(j + 1));
				}
			}
			sb1.append("PRIMARY KEY (query_name));");

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
			sb2.append("VALUES\n");

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == titleLenght) {

					sb2.append("(");

					String[] proname = cs[0].split("_");
					sb2.append("\"").append(proname[2]).append("\"").append(",");

					if (fastaMap.containsKey(proname[2])) {
						sb2.append("\"").append(fastaMap.get(proname[2])).append("\"");
					} else {
						sb2.append("\"").append("").append("\"");
					}

					for (int j = 1; j < cs.length; j++) {
						if (map.containsKey(j + 1)) {
							sb2.append(",").append("\"").append(cs[j]).append("\"");
						}
					}
					sb2.append(")").append(",");
				}
			}
			reader.close();

			sb2.deleteCharAt(sb2.length() - 1);
			sb2.append(";");

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb1.toString());
				stmt.execute(sb2.toString());
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

			if (i % 100 == 0) {
				System.out.println(name);
			}
		}
	}
	
	/**
	 * 2022.03.08
	 * @param in
	 * @param taxdb
	 * @param cogDb
	 * @param nogDb
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void createPro2FuncTable20220308(String in, String cogInfo, String KofamKOALA)
			throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:Z:/kai/HGM_DB/MGYG/UHGG_func_anno.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		HashMap<String, String> cogMap = new HashMap<String, String>();
		BufferedReader cogReader = new BufferedReader(new FileReader(cogInfo));
		String coglineString = null;
		while ((coglineString = cogReader.readLine()) != null) {
			String[] cs = coglineString.split("\t");
			cogMap.put(cs[0], cs[1]);
		}
		cogReader.close();

		System.out.println("cog\t" + cogMap.size());

		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {
			
			String fileName = files[i].getName();
			
			if (!files[i].isDirectory()) {
				continue;
			}

			File fastaFile = new File(files[i], "MGYG" + fileName + ".faa");

			if (!fastaFile.exists()) {
				continue;
			}

			File kofamFile = new File(KofamKOALA, "MGYG" + fileName + ".txt");
			if (!kofamFile.exists()) {
				continue;
			}

			HashMap<String, String> kofamMap = new HashMap<String, String>();
			BufferedReader kReader = new BufferedReader(new FileReader(kofamFile));
			String kline = null;
			while ((kline = kReader.readLine()) != null) {
				String[] cs = kline.split("\t");
				if (cs.length == 2) {
					kofamMap.put(cs[0], cs[1]);
				}
			}
			kReader.close();

			HashMap<String, String> fastaMap = new HashMap<String, String>();
			BufferedReader freader = new BufferedReader(new FileReader(fastaFile));

			String frline = null;
			while ((frline = freader.readLine()) != null) {
				if (frline.startsWith(">")) {
					int id = frline.indexOf(" ");
					fastaMap.put(frline.substring(1, id), frline.substring(id + 1));
				}
			}
			freader.close();

			String name = "pro2fun_" + fileName;
			if (name.endsWith(".1")) {
				name = name.substring(0, name.length() - 2);
			}

			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG" + fileName + "_eggNOG.tsv")));
			String line = reader.readLine();

			StringBuilder sb1 = new StringBuilder();
			StringBuilder titlesb = new StringBuilder();

			sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n query_name VARCHAR(100),\n");
			titlesb.append("query_name");

			sb1.append(" pro_name text NOT NULL,\n");
			titlesb.append(",").append("pro_name");
			
			String[] title = line.split("\t");
			sb1.append(" " + title[1] + " text NOT NULL,\n");
			titlesb.append(",").append(title[1]);

			sb1.append(" KofamKOALA_ko text NOT NULL,\n");
			titlesb.append(",").append("KofamKOALA_ko");
			
			sb1.append(" COG text NOT NULL,\n");
			titlesb.append(",").append("COG");
			
			for (int j = 4; j < title.length; j++) {
				sb1.append(" " + title[j] + " text NOT NULL,\n");
				titlesb.append(",").append(title[j]);
			}

			sb1.append("PRIMARY KEY (query_name));");

//			System.out.println(titlesb);
//			System.out.println(sb1);

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
			sb2.append("VALUES\n");

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");

				if (cs.length == title.length) {

					sb2.append("(");

					sb2.append("\"").append(cs[0]).append("\"").append(",");

					if (fastaMap.containsKey(cs[0])) {
						sb2.append("\"").append(fastaMap.get(cs[0])).append("\"");
					} else {
						sb2.append("\"").append("-").append("\"");
					}

					String seed = cs[1];
					int spid = seed.indexOf(".");
					if (spid > 0) {
						seed = seed.substring(spid + 1);
					}
					sb2.append(",").append("\"").append(seed).append("\"");
					
					if (kofamMap.containsKey(cs[0])) {
						sb2.append(",").append("\"").append(kofamMap.get(cs[0])).append("\"");
					} else {
						sb2.append(",").append("\"").append("-").append("\"");
					}
					
					if (cogMap.containsKey(cs[0])) {
						sb2.append(",").append("\"").append(cogMap.get(cs[0])).append("\"");
					} else {
						sb2.append(",").append("\"").append("-").append("\"");
					}

					for (int j = 4; j < cs.length; j++) {
						sb2.append(",").append("\"").append(cs[j]).append("\"");
					}

					sb2.append(")");

					sb2.append(",");
				}
			}

			reader.close();

			sb2.deleteCharAt(sb2.length() - 1);
			
			sb2.append(";");

//			System.out.println(sb2);
			
			boolean one = false;

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb1.toString());
				stmt.execute(sb2.toString());
				
				one = true;
				
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

//			if(one) {
//				break;
//			}
			
			if (i % 100 == 0) {
				System.out.println(name);
			}
		}
	}
	
	/**
	 * 2022.01.04
	 * @param in
	 * @param taxdb
	 * @param cogDb
	 * @param nogDb
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void createPro2FuncTable20220104(String in, String cogInfo)
			throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:Z:/kai/HGM_DB/MGYG/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		HashMap<String, String> cogMap = new HashMap<String, String>();
		BufferedReader cogReader = new BufferedReader(new FileReader(cogInfo));
		String coglineString = null;
		while ((coglineString = cogReader.readLine()) != null) {
			String[] cs = coglineString.split("\t");
			cogMap.put(cs[0], cs[1]);
		}
		cogReader.close();

		System.out.println("cog\t" + cogMap.size());

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			
			String fileName = files[i].getName();
			
			if (!files[i].isDirectory()) {
				continue;
			}

			File fastaFile = new File(files[i], "MGYG" + fileName + ".faa");

			if (!fastaFile.exists()) {
				continue;
			}

			HashMap<String, String> fastaMap = new HashMap<String, String>();
			BufferedReader freader = new BufferedReader(new FileReader(fastaFile));

			String frline = null;
			while ((frline = freader.readLine()) != null) {
				if (frline.startsWith(">")) {
					int id = frline.indexOf(" ");
					fastaMap.put(frline.substring(1, id), frline.substring(id + 1));
				}
			}
			freader.close();

			String name = "pro2fun_" + fileName;
			if (name.endsWith(".1")) {
				name = name.substring(0, name.length() - 2);
			}

			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG" + fileName + "_eggNOG.tsv")));
			String line = reader.readLine();

			StringBuilder sb1 = new StringBuilder();
			StringBuilder titlesb = new StringBuilder();

			sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n query_name VARCHAR(100),\n");
			titlesb.append("query_name");

			sb1.append(" pro_name text NOT NULL,\n");
			titlesb.append(",").append("pro_name");
			
			String[] title = line.split("\t");
			sb1.append(" " + title[1] + " text NOT NULL,\n");
			titlesb.append(",").append(title[1]);

			sb1.append(" COG text NOT NULL,\n");
			titlesb.append(",").append("COG");
			
			for (int j = 4; j < title.length; j++) {
				sb1.append(" " + title[j] + " text NOT NULL,\n");
				titlesb.append(",").append(title[j]);
			}

			sb1.append("PRIMARY KEY (query_name));");

//			System.out.println(titlesb);
//			System.out.println(sb1);

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
			sb2.append("VALUES\n");

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");

				if (cs.length == title.length) {

					sb2.append("(");

					sb2.append("\"").append(cs[0]).append("\"").append(",");

					if (fastaMap.containsKey(cs[0])) {
						sb2.append("\"").append(fastaMap.get(cs[0])).append("\"");
					} else {
						sb2.append("\"").append("-").append("\"");
					}

					String seed = cs[1];
					int spid = seed.indexOf(".");
					if (spid > 0) {
						seed = seed.substring(spid + 1);
					}
					sb2.append(",").append("\"").append(seed).append("\"");
					
					if (cogMap.containsKey(cs[0])) {
						sb2.append(",").append("\"").append(cogMap.get(cs[0])).append("\"");
					} else {
						sb2.append(",").append("\"").append("-").append("\"");
					}

					for (int j = 4; j < cs.length; j++) {
						sb2.append(",").append("\"").append(cs[j]).append("\"");
					}

					sb2.append(")");

					sb2.append(",");
				}
			}

			reader.close();

			sb2.deleteCharAt(sb2.length() - 1);
			
			sb2.append(";");

//			System.out.println(sb2);
			
			boolean one = false;

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb1.toString());
				stmt.execute(sb2.toString());
				
				one = true;
				
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

//			if(one) {
//				break;
//			}
			
//			if (i % 100 == 0) {
				System.out.println(name);
//			}
		}
	}
	
	/**
	 * 2021.9.29
	 * @param in
	 * @param usedTitles
	 * @param taxdb
	 * @param cogDb
	 * @param nogDb
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void createPro2FuncTable(String in, String usedTitles, String taxdb, String cogDb, String nogDb)
			throws NumberFormatException, IOException {

		HashMap<String, String[]> cogInfoMap = new HashMap<String, String[]>(4877);
		BufferedReader cogReader = new BufferedReader(new FileReader(cogDb));
		String cogLine = null;
		while ((cogLine = cogReader.readLine()) != null) {
			String[] cs = cogLine.split("\t");
			cogInfoMap.put(cs[0], new String[] { cs[1], cs[2] });
		}
		cogReader.close();

		HashMap<String, String[]> nogInfoMap = new HashMap<String, String[]>(4400438);
		BufferedReader nogReader = new BufferedReader(new FileReader(nogDb));
		String nogLine = null;
		while ((nogLine = nogReader.readLine()) != null) {
			String[] cs = nogLine.split("\t");
			if (cs.length > 3) {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], cs[3] });
			} else {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], "" });
			}
		}
		nogReader.close();

		TaxonomyDatabase td = new TaxonomyDatabase(taxdb);

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		BufferedReader titlereader = new BufferedReader(new FileReader(usedTitles));
		String uline = null;
		while ((uline = titlereader.readLine()) != null) {
			int id = uline.indexOf(".");
			int number = Integer.parseInt(uline.substring(0, id));

			map.put(number, uline.substring(id + 1).trim());
		}
		titlereader.close();

		System.out.println(map);

		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		int titleLenght = 22;

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {

			if (!files[i].isDirectory()) {
				continue;
			}

			HashMap<String, String> fastaMap = new HashMap<String, String>();
			BufferedReader freader = new BufferedReader(
					new FileReader(new File(in, "MGYG-HGUT-" + files[i].getName() + ".faa")));

			String frline = null;
			while ((frline = freader.readLine()) != null) {
				if (frline.startsWith(">")) {
					String[] cs = frline.split("_");
					int id = cs[2].indexOf(" ");
					fastaMap.put(cs[2].substring(0, id), cs[2].substring(id + 1));
				}
			}
			freader.close();

			String name = "pro2fun_" + files[i].getName();

			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG-HGUT-" + files[i].getName() + "_eggNOG.tsv")));
			String line = reader.readLine();

			StringBuilder sb1 = new StringBuilder();
			StringBuilder titlesb = new StringBuilder();

			sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n query_name VARCHAR(100),\n");
			titlesb.append("query_name");

			for (int j = 1; j < PRO2FUNTITLE_STRINGS.length; j++) {
				sb1.append(" " + PRO2FUNTITLE_STRINGS[j] + " text NOT NULL,\n");
				titlesb.append(",").append(PRO2FUNTITLE_STRINGS[j]);
			}

			sb1.append("PRIMARY KEY (query_name));");

//			System.out.println(titlesb);
//			System.out.println(sb1);

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
			sb2.append("VALUES\n");

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == titleLenght) {

					String[] cogs = cs[18].split(",");

					ArrayList<String> cogList = new ArrayList<String>();
					ArrayList<String> nogList = new ArrayList<String>();

					String rootTaxon = cs[17];
					int taxId = -1;
					int currentNogRankId = -1;
					String funId = null;

					for (int j = 0; j < cogs.length; j++) {
						int spid = cogs[j].indexOf("@");
						funId = cogs[j].substring(0, spid);

						if (nogInfoMap.containsKey(cogs[j])) {
							int nogTaxId = Integer.parseInt(cogs[j].substring(spid + 1));
							Taxon nogTaxon = td.getTaxonFromId(nogTaxId);
							if (nogTaxon != null) {
								int nogRankId = nogTaxon.getRankId();
								if (nogRankId > currentNogRankId) {
									nogList = new ArrayList<String>();
									nogList.add(funId);
									currentNogRankId = nogRankId;
									taxId = nogTaxId;
								}
							}

							if (cogInfoMap.containsKey(funId)) {
								int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
								Taxon taxonj = td.getTaxonFromId(taxIdJ);
								if (taxonj != null) {
									if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
										cogList.add(funId);
									}
								}
							}

						} else {
							if (cogInfoMap.containsKey(funId)) {
								int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
								Taxon taxonj = td.getTaxonFromId(taxIdJ);
								if (taxonj != null) {
									if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
										cogList.add(funId);
										taxId = taxonj.getId();
									}
								}
							}
						}
					}

					if (taxId == -1 || td.getTaxonFromId(taxId) == null) {
						System.out.println("---cao571\t" + line);
						continue;
					}

					sb2.append("(");

					String[] proname = cs[0].split("_");
					sb2.append("\"").append(proname[2]).append("\"").append(",");

					if (fastaMap.containsKey(proname[2])) {
						sb2.append("\"").append(fastaMap.get(proname[2])).append("\"");
					} else {
						sb2.append("\"").append("").append("\"");
					}

					sb2.append(",").append("\"").append(taxId).append("\"");
					sb2.append(",").append("\"").append(td.getTaxonFromId(taxId).getName()).append("\"");

					for (int j = 5; j < 17; j++) {
						sb2.append(",").append("\"").append(cs[j]).append("\"");
					}

					sb2.append(",").append("\"");
					for (int j = 0; j < cogList.size(); j++) {
						sb2.append(cogList.get(j)).append(",");
					}
					if (cogList.size() > 0) {
						sb2.deleteCharAt(sb2.length() - 1);
					}
					sb2.append("\"");
					
					sb2.append(",").append("\"");
					for (int j = 0; j < nogList.size(); j++) {
						sb2.append(nogList.get(j)).append(",");
					}
					if (nogList.size() > 0) {
						sb2.deleteCharAt(sb2.length() - 1);
					}
					sb2.append("\"");
					
					sb2.append(")");
					
					sb2.append(",");
				}
			}

			reader.close();

			sb2.deleteCharAt(sb2.length() - 1);
			
			sb2.append(";");

//			System.out.println(sb2);

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sb1.toString());
				stmt.execute(sb2.toString());
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

			if (i % 100 == 0) {
				System.out.println(name);
			}
		}
	}
	
	/**
	 * 2021.9.29
	 * @param in
	 * @param usedTitles
	 * @param taxdb
	 * @param cogDb
	 * @param nogDb
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void createPro2FuncTable(String url, String in, String microbeName, String taxdb, String cogDb, String nogDb)
			throws NumberFormatException, IOException {

		HashMap<String, String[]> cogInfoMap = new HashMap<String, String[]>(4877);
		BufferedReader cogReader = new BufferedReader(new FileReader(cogDb));
		String cogLine = null;
		while ((cogLine = cogReader.readLine()) != null) {
			String[] cs = cogLine.split("\t");
			cogInfoMap.put(cs[0], new String[] { cs[1], cs[2] });
		}
		cogReader.close();

		HashMap<String, String[]> nogInfoMap = new HashMap<String, String[]>(4400438);
		BufferedReader nogReader = new BufferedReader(new FileReader(nogDb));
		String nogLine = null;
		while ((nogLine = nogReader.readLine()) != null) {
			String[] cs = nogLine.split("\t");
			if (cs.length > 3) {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], cs[3] });
			} else {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], "" });
			}
		}
		nogReader.close();

		TaxonomyDatabase td = new TaxonomyDatabase(taxdb);

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		int titleLenght = 22;

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.startsWith("#query")) {
				break;
			}
		}
		
		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(microbeName).append(" (\n query_name VARCHAR(100),\n");
		titlesb.append("query_name");

		for (int j = 1; j < PRO2FUNTITLE_STRINGS.length; j++) {
			sb1.append(" " + PRO2FUNTITLE_STRINGS[j] + " text NOT NULL,\n");
			titlesb.append(",").append(PRO2FUNTITLE_STRINGS[j]);
		}

		sb1.append("PRIMARY KEY (query_name));");

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
		} catch (SQLException e) {
			System.out.println("716\t" + e.getMessage());
		}

//		System.out.println(titlesb);
//		System.out.println(sb1);

		StringBuilder sb2 = new StringBuilder();
		sb2.append("INSERT INTO ").append(microbeName).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		int count = 0;
		
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == titleLenght) {

				String[] cogs = cs[18].split(",");

				ArrayList<String> cogList = new ArrayList<String>();
				ArrayList<String> nogList = new ArrayList<String>();

				String rootTaxon = cs[17];
				int taxId = -1;
				int currentNogRankId = -1;
				String funId = null;

				for (int j = 0; j < cogs.length; j++) {
					int spid = cogs[j].indexOf("@");
//					if(spid==-1) {
//						System.out.println(cogs[j]);
//					}
					
					funId = cogs[j].substring(0, spid);
					
					

					if (nogInfoMap.containsKey(cogs[j])) {
						int nogTaxId = Integer.parseInt(cogs[j].substring(spid + 1));
						Taxon nogTaxon = td.getTaxonFromId(nogTaxId);
						if (nogTaxon != null) {
							int nogRankId = nogTaxon.getRankId();
							if (nogRankId > currentNogRankId) {
								nogList = new ArrayList<String>();
								nogList.add(funId);
								currentNogRankId = nogRankId;
								taxId = nogTaxId;
							}
						}

						if (cogInfoMap.containsKey(funId)) {
							int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
							Taxon taxonj = td.getTaxonFromId(taxIdJ);
							if (taxonj != null) {
								if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
									cogList.add(funId);
								}
							}
						}

					} else {
						if (cogInfoMap.containsKey(funId)) {
							int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
							Taxon taxonj = td.getTaxonFromId(taxIdJ);
							if (taxonj != null) {
								if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
									cogList.add(funId);
									taxId = taxonj.getId();
								}
							}
						}
					}
				}

				if (taxId == -1 || td.getTaxonFromId(taxId) == null) {
					System.out.println("---cao571\t" + line);
					continue;
				}

				sb2.append("(");

				sb2.append("\"").append(cs[0]).append("\"");
				sb2.append(",").append("\"").append("\"");
				sb2.append(",").append("\"").append(taxId).append("\"");
				sb2.append(",").append("\"").append(td.getTaxonFromId(taxId).getName()).append("\"");

				for (int j = 5; j < 17; j++) {
					
					sb2.append(",").append("\"").append(cs[j]).append("\"");
/*					
					sb2.append(",").append("\"");
					for (int k = 0; k < cs[j].length(); k++) {
						char aa = cs[j].charAt(k);
						if (aa == '(' || aa == ')' || aa == '\'') {
							sb2.append("\"").append(aa);
						} else {
							sb2.append(aa);
						}
					}
					sb2.append("\"");
*/					
				}

				sb2.append(",").append("\"");
				for (int j = 0; j < cogList.size(); j++) {
					sb2.append(cogList.get(j)).append(",");
				}
				if (cogList.size() > 0) {
					sb2.deleteCharAt(sb2.length() - 1);
				}
				sb2.append("\"");

				sb2.append(",").append("\"");
				for (int j = 0; j < nogList.size(); j++) {
					sb2.append(nogList.get(j)).append(",");
				}
				if (nogList.size() > 0) {
					sb2.deleteCharAt(sb2.length() - 1);
				}
				sb2.append("\"");

				sb2.append(")");

				sb2.append(",");
				
				
				count++;
//				System.out.println(count);
				if (count % 500000 == 0) {
					
					System.out.println(count);
					
					sb2.deleteCharAt(sb2.length() - 1);
					sb2.append(";");

					try (Statement stmt = conn.createStatement()) {
						stmt.execute(sb2.toString());
					} catch (SQLException e) {
						System.out.println("845\t" + e.getMessage());
					}

					sb2 = new StringBuilder();
					sb2.append("INSERT INTO ").append(microbeName).append(" (").append(titlesb).append(")\n");
					sb2.append("VALUES\n");
				}
			}
			
			
		}

		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

//		System.out.println(sb2);

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println("864\t" + e.getMessage());
		}

	}
	
	/**
	 * 2021.9.29
	 * @param in
	 * @param usedTitles
	 * @param taxdb
	 * @param cogDb
	 * @param nogDb
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void addPro2FuncTable(String in, String usedTitles, String taxdb, String cogDb, String nogDb, String miss)
			throws NumberFormatException, IOException {

		HashMap<String, String[]> missMap = new HashMap<String, String[]>();
		BufferedReader missReader = new BufferedReader(new FileReader(miss));
		String missLine = null;
		while ((missLine = missReader.readLine()) != null) {
			String[] cs = missLine.split("\t");
			missMap.put(cs[1], cs);
		}
		missReader.close();

		System.out.println(missMap);
		
		HashMap<String, String[]> cogInfoMap = new HashMap<String, String[]>(4877);
		BufferedReader cogReader = new BufferedReader(new FileReader(cogDb));
		String cogLine = null;
		while ((cogLine = cogReader.readLine()) != null) {
			String[] cs = cogLine.split("\t");
			cogInfoMap.put(cs[0], new String[] { cs[1], cs[2] });
		}
		cogReader.close();

		HashMap<String, String[]> nogInfoMap = new HashMap<String, String[]>(4400438);
		BufferedReader nogReader = new BufferedReader(new FileReader(nogDb));
		String nogLine = null;
		while ((nogLine = nogReader.readLine()) != null) {
			String[] cs = nogLine.split("\t");

			if (cs.length > 3) {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], cs[3] });
			} else {
				nogInfoMap.put(cs[1] + "@" + cs[0], new String[] { cs[2], "" });
			}
		}
		nogReader.close();

		TaxonomyDatabase td = new TaxonomyDatabase(taxdb);

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		BufferedReader titlereader = new BufferedReader(new FileReader(usedTitles));
		String uline = null;
		while ((uline = titlereader.readLine()) != null) {
			int id = uline.indexOf(".");
			int number = Integer.parseInt(uline.substring(0, id));

			map.put(number, uline.substring(id + 1).trim());
		}
		titlereader.close();

		System.out.println(map);

		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		int titleLenght = 22;

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {

			if (!files[i].isDirectory()) {
				continue;
			}

			HashMap<String, String> fastaMap = new HashMap<String, String>();
			BufferedReader freader = new BufferedReader(
					new FileReader(new File(in, "MGYG-HGUT-" + files[i].getName() + ".faa")));

			String frline = null;
			while ((frline = freader.readLine()) != null) {
				if (frline.startsWith(">")) {
					String[] cs = frline.split("_");
					int id = cs[2].indexOf(" ");
					fastaMap.put(cs[2].substring(0, id), cs[2].substring(id + 1));
				}
			}
			freader.close();

			String name = "pro2fun_" + files[i].getName();

			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG-HGUT-" + files[i].getName() + "_eggNOG.tsv")));
			String line = reader.readLine();

			StringBuilder sb1 = new StringBuilder();
			StringBuilder titlesb = new StringBuilder();

			sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n query_name VARCHAR(100),\n");
			titlesb.append("query_name");

			for (int j = 1; j < PRO2FUNTITLE_STRINGS.length; j++) {
				sb1.append(" " + PRO2FUNTITLE_STRINGS[j] + " text NOT NULL,\n");
				titlesb.append(",").append(PRO2FUNTITLE_STRINGS[j]);
			}

			sb1.append("PRIMARY KEY (query_name));");

//			System.out.println(titlesb);
//			System.out.println(sb1);

			StringBuilder sb2 = new StringBuilder();
			sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
			sb2.append("VALUES\n");

			boolean has = false;
			
			while ((line = reader.readLine()) != null) {
				
				String[] cs = line.split("\t");
				
				if (cs.length == titleLenght && missMap.containsKey(cs[0])) {
					
					has = true;

					String[] cogs = cs[18].split(",");

					ArrayList<String> cogList = new ArrayList<String>();
					ArrayList<String> nogList = new ArrayList<String>();

					String rootTaxon = cs[17];
					int taxId = -1;
					int currentNogRankId = -1;
					String funId = null;

					for (int j = 0; j < cogs.length; j++) {
						
						cogs[j] = cogs[j].replace("", "");
						
						int spid = cogs[j].indexOf("@");
						funId = cogs[j].substring(0, spid);

						if (nogInfoMap.containsKey(cogs[j])) {
							int nogTaxId = Integer.parseInt(cogs[j].substring(spid + 1));
							Taxon nogTaxon = td.getTaxonFromId(nogTaxId);
							if (nogTaxon != null) {
								int nogRankId = nogTaxon.getRankId();
								if (nogRankId > currentNogRankId) {
									nogList = new ArrayList<String>();
									nogList.add(funId);
									currentNogRankId = nogRankId;
									taxId = nogTaxId;
								}
							}

							if (cogInfoMap.containsKey(funId)) {
								int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
								Taxon taxonj = td.getTaxonFromId(taxIdJ);
								if (taxonj != null) {
									if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
										cogList.add(funId);
									}
								}
							}

						} else {
							if (cogInfoMap.containsKey(funId)) {
								int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));
								Taxon taxonj = td.getTaxonFromId(taxIdJ);
								if (taxonj != null) {
									if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
										cogList.add(funId);
										taxId = taxonj.getId();
									}
								}
							}
						}
					}

					if (taxId == -1 || td.getTaxonFromId(taxId) == null) {
						System.out.println("---cao571\t" + line);
						continue;
					}

					sb2.append("(");

					String[] proname = cs[0].split("_");
					sb2.append("\"").append(proname[2]).append("\"").append(",");

					if (fastaMap.containsKey(proname[2])) {
						sb2.append("\"").append(fastaMap.get(proname[2])).append("\"");
					} else {
						sb2.append("\"").append("").append("\"");
					}

					sb2.append(",").append("\"").append(taxId).append("\"");
					sb2.append(",").append("\"").append(td.getTaxonFromId(taxId).getName()).append("\"");

					for (int j = 5; j < 17; j++) {
						sb2.append(",").append("\"").append(cs[j]).append("\"");
					}

					sb2.append(",").append("\"");
					for (int j = 0; j < cogList.size(); j++) {
						sb2.append(cogList.get(j)).append(",");
					}
					if (cogList.size() > 0) {
						sb2.deleteCharAt(sb2.length() - 1);
					}
					sb2.append("\"");
					
					sb2.append(",").append("\"");
					for (int j = 0; j < nogList.size(); j++) {
						sb2.append(nogList.get(j)).append(",");
					}
					if (nogList.size() > 0) {
						sb2.deleteCharAt(sb2.length() - 1);
					}
					sb2.append("\"");
					
					sb2.append(")");
					
					sb2.append(",");
				}
			}

			reader.close();

			sb2.deleteCharAt(sb2.length() - 1);
			
			sb2.append(";");

			if (has) {
				System.out.println(sb2);
				
				/*			
			
				try (Statement stmt = conn.createStatement()) {
//					stmt.execute(sb1.toString());
					stmt.execute(sb2.toString());
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
*/					
				
			}
		

			if (i % 100 == 0) {
//				System.out.println(name);
			}
		}

	}
	
	private static void getCOG(String in, String taxdb) throws IOException {
		
		TaxonomyDatabase td = new TaxonomyDatabase(taxdb);
		
		int titleLenght = 22;
		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {
			BufferedReader reader = new BufferedReader(
					new FileReader(new File(files[i], "MGYG-HGUT-" + files[i].getName() + "_eggNOG.tsv")));
			String line = reader.readLine();

			while ((line = reader.readLine()) != null) {
				
				String[] cs = line.split("\t");
				
				if (cs.length == titleLenght) {

					String[] cogs = cs[18].split(",");

					ArrayList<String> cogList = new ArrayList<String>();
					ArrayList<String> nogList = new ArrayList<String>();

					String taxon = cs[4];
					String rootTaxon = cs[17];
					int taxId = -1;
					int currentId = -1;

					HashMap<Integer, ArrayList<String>> nogMap = new HashMap<Integer, ArrayList<String>>();

					for (int j = 0; j < cogs.length; j++) {
						int spid = cogs[j].indexOf("@");
						String funId = cogs[j].substring(0, spid);
						int taxIdJ = Integer.parseInt(cogs[j].substring(spid + 1));

						if (taxIdJ != 1) {

							Taxon taxonj = td.getTaxonFromId(taxIdJ);

							if (taxonj != null) {

								if (taxonj.getName().equalsIgnoreCase(taxon)) {
									taxId = taxIdJ;
								}
								if (taxonj.getName().equalsIgnoreCase(rootTaxon)) {
									cogList.add(funId);
								} else {
									currentId = taxIdJ;

									if (nogMap.containsKey(taxIdJ)) {
										nogMap.get(taxIdJ).add(funId);
									} else {
										ArrayList<String> nogListJ = new ArrayList<String>();
										nogListJ.add(funId);
										nogMap.put(taxIdJ, nogListJ);
									}
								}
							}
						}
					}

					if (taxId != -1) {
						nogList = nogMap.get(taxId);
					} else {
						if (currentId != -1) {
							nogList = nogMap.get(currentId);
						}
					}
				}
			}

			reader.close();
		}
	}
	
	public static void addColumn(String in) {
		String url = "jdbc:sqlite:D:/Data/new_db_4/catalog.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {

			if (files[i].isDirectory()) {
				String name = "pro2fun_" + files[i].getName();
System.out.println(name);

				StringBuilder sb = new StringBuilder();
				sb.append("ALTER TABLE ").append(name).append(";\n");
				sb.append("ADD COLUMN COG").append(";\n");
				sb.append("ADD COLUMN NOG;");

				try (Statement stmt = conn.createStatement()) {
					stmt.execute(sb.toString());
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	private static void createGenomeGTDBNCBITaxaTable(String in) throws IOException {
		String url = "jdbc:sqlite:Z:/kai/HGM_DB/MGYG/UHGG_func_anno.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "genome2taxa";

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		String[] title = line.split("\t");
		for (int i = 0; i < 4; i++) {

			sb1.append(" " + title[i] + " text NOT NULL,\n");
			titlesb.append(title[i]).append(",");
		}
		for (int i = 4; i < title.length; i++) {

			sb1.append(" " + title[i] + " text,\n");
			titlesb.append(title[i]).append(",");
		}
		sb1.append("PRIMARY KEY (Genome));");

		titlesb.deleteCharAt(titlesb.length() - 1);

		StringBuilder sb2 = new StringBuilder();
		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		System.out.println(sb2);

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {

				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				sb2.deleteCharAt(sb2.length() - 1);
				sb2.append(")").append(",");

			} else if (cs.length == title.length - 1) {

				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				sb2.append("\"\"");
				sb2.append(")").append(",");

			} else {
				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				for (int j = cs.length; j < title.length; j++) {
					sb2.append("\"\"").append(",");
				}
				sb2.deleteCharAt(sb2.length() - 1);
				sb2.append(")").append(",");
			}
		}
		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void createGenomeTaxaTable(String in) throws IOException {
		String url = "jdbc:sqlite:Z:/kai/HGM_DB/MGYG/UHGG_func_anno.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "genome2taxa";

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		String[] title = line.split("\t");
		for (int i = 0; i < 4; i++) {

			sb1.append(" " + title[i] + " text NOT NULL,\n");
			titlesb.append(title[i]).append(",");
		}
		for (int i = 4; i < title.length; i++) {

			sb1.append(" " + title[i] + "_ text,\n");
			titlesb.append(title[i]).append("_,");
		}
		sb1.append("PRIMARY KEY (Genome));");

		titlesb.deleteCharAt(titlesb.length() - 1);

		StringBuilder sb2 = new StringBuilder();
		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		System.out.println(sb2);

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {

				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				sb2.deleteCharAt(sb2.length() - 1);
				sb2.append(")").append(",");

			} else if (cs.length == title.length - 1) {

				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				sb2.append("\"\"");
				sb2.append(")").append(",");

			} else {
				sb2.append("(");
				for (int j = 0; j < cs.length; j++) {
					sb2.append("\"").append(cs[j]).append("\"").append(",");
				}
				for (int j = cs.length; j < title.length; j++) {
					sb2.append("\"\"").append(",");
				}
				sb2.deleteCharAt(sb2.length() - 1);
				sb2.append(")").append(",");
			}
		}
		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void createCogTable(String in) throws IOException {
		String url = "jdbc:sqlite:D:/Exported/exe/Resources/function/func_def.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "cog_20_def_tab";

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		sb1.append(" id text NOT NULL,\n");
		titlesb.append("id,");

		sb1.append(" category text NOT NULL,\n");
		titlesb.append("category,");

		sb1.append(" description text NOT NULL,\n");
		titlesb.append("description");

		sb1.append("PRIMARY KEY (id));");

		StringBuilder sb2 = new StringBuilder();
		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			sb2.append("(");
			for (int j = 0; j < 3; j++) {
				sb2.append("\"").append(cs[j]).append("\"").append(",");
			}
			sb2.deleteCharAt(sb2.length() - 1);
			sb2.append(")").append(",");
		}
		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");
		
//		System.out.println(sb1);
//		System.out.println(titlesb);
//		System.out.println(sb2);

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	private static void createNogTable(String in) throws IOException {
		String url = "jdbc:sqlite:D:/Exported/exe/Resources/function/func_def.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "nog_e5_og_annotations";

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		sb1.append(" id text NOT NULL,\n");
		titlesb.append("id,");

		sb1.append(" category text NOT NULL,\n");
		titlesb.append("category,");

		sb1.append(" description text NOT NULL,\n");
		titlesb.append("description");

		sb1.append("PRIMARY KEY (id));");

		StringBuilder sb2 = new StringBuilder();
		
		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");
		
		int count = 0;

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");

			

			sb2.append("(");
			sb2.append("\"").append(cs[1]).append("@").append(cs[0]).append("\"").append(",");

			if (cs.length == 4) {
				sb2.append("\"").append(cs[2]).append("\"").append(",");
				String des = cs[3].replaceAll("\"", "\"\"");
//				System.out.println("cao\t"+cs[3]+"\t"+des);			
//				des = des.replaceAll("insert", "\"insert\"");

				sb2.append("\"").append(des).append("\"");

			} else {
				sb2.append("\"").append(cs[2]).append("\"").append(",");
				sb2.append("\"").append("").append("\"");
			}

			sb2.append(")").append(",");
		
		}
		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");
		
		System.out.println(sb1);
		System.out.println(titlesb);
//		System.out.println(sb2);

		
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	private static void createGOTable(String in) throws IOException {
		String url = "jdbc:sqlite:D:/Exported/exe/Resources/function/func_def.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "go_obo";

		GoOboReader reader = new GoOboReader(in);
		GoObo[] gos = reader.getGoObos();

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		sb1.append(" id text NOT NULL,\n");
		titlesb.append("id,");

		sb1.append(" name text NOT NULL,\n");
		titlesb.append("name,");

		sb1.append(" namespace text NOT NULL,\n");
		titlesb.append("namespace");

		sb1.append("PRIMARY KEY (id));");

		StringBuilder sb2 = new StringBuilder();

		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		for (int i = 0; i < gos.length; i++) {
			sb2.append("(");
			sb2.append("\"").append(gos[i].getId()).append("\"").append(",");
			sb2.append("\"").append(gos[i].getName()).append("\"").append(",");
			sb2.append("\"").append(gos[i].getNamespace()).append("\"");
			sb2.append(")").append(",");
		}

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

		System.out.println(sb1);
		System.out.println(titlesb);
//		System.out.println(sb2);

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	private static void createKEGGTable(String in) throws IOException {
		
		String url = "jdbc:sqlite:D:/Exported/exe/Resources/function/func_def.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "kegg_pathway";

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		sb1.append(" id text NOT NULL,\n");
		titlesb.append("id,");

		sb1.append(" name text NOT NULL,\n");
		titlesb.append("name,");

		sb1.append(" description text NOT NULL,\n");
		titlesb.append("description");

		sb1.append("PRIMARY KEY (id));");

		StringBuilder sb2 = new StringBuilder();

		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("[<>]");
			if (cs.length > 1 && cs[1].equals("dt")) {
				String id = cs[2];
				String pathwayname = "";
				String des = "";
				for (int i = 2; i < cs.length; i++) {
					if (cs[i].startsWith("a href=\"/pathway")) {
						int sp = cs[i].lastIndexOf('/');
						pathwayname = cs[i].substring(sp + 1, cs[i].length() - 1);
						des = cs[i + 1];

						sb2.append("(");
						sb2.append("\"").append(id).append("\"").append(",");
						sb2.append("\"").append(pathwayname).append("\"").append(",");
						sb2.append("\"").append(des).append("\"");
						sb2.append(")").append(",");

						break;
					} else if (cs[i].startsWith("a href=\"/kegg-bin")) {

						int sp1 = cs[i].lastIndexOf('?');
						int sp2 = cs[i].lastIndexOf('&');
						
						pathwayname = cs[i].substring(sp1 + 1, sp2);
						pathwayname = pathwayname.replace("=", "");
						
						
						System.out.println(pathwayname);
						
						
						des = cs[i + 1];

						sb2.append("(");
						sb2.append("\"").append(id).append("\"").append(",");
						sb2.append("\"").append(pathwayname).append("\"").append(",");
						sb2.append("\"").append(des).append("\"");
						sb2.append(")").append(",");

						break;
					}
				}
			}
		}
		reader.close();

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

		System.out.println(sb1);
		System.out.println(titlesb);
//		System.out.println(sb2);

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	private static void createECTable(String in) throws IOException {
		String url = "jdbc:sqlite:D:/Exported/exe/Resources/function/func_def.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String name = "enzyme";

		ECReader reader = new ECReader(in);
		EnzymeCommission[] ecs = reader.getECs();

		StringBuilder sb1 = new StringBuilder();
		StringBuilder titlesb = new StringBuilder();

		sb1.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (\n");

		sb1.append(" id text NOT NULL,\n");
		titlesb.append("id,");

		sb1.append(" de text NOT NULL,\n");
		titlesb.append("de,");

		sb1.append(" ca text NOT NULL,\n");
		titlesb.append("ca,");
		
		sb1.append(" an text NOT NULL,\n");
		titlesb.append("an");

		sb1.append("PRIMARY KEY (id));");

		StringBuilder sb2 = new StringBuilder();

		sb2.append("INSERT INTO ").append(name).append(" (").append(titlesb).append(")\n");
		sb2.append("VALUES\n");

		for (int i = 0; i < ecs.length; i++) {
			sb2.append("(");
			sb2.append("\"").append(ecs[i].getId()).append("\"").append(",");
			sb2.append("\"").append(ecs[i].getDe()).append("\"").append(",");
			sb2.append("\"").append(ecs[i].getCa()).append("\"").append(",");
			sb2.append("\"").append(ecs[i].getAnString()).append("\"");
			sb2.append(")").append(",");
		}

		sb2.deleteCharAt(sb2.length() - 1);
		sb2.append(";");

		System.out.println(sb1);
		System.out.println(titlesb);
//		System.out.println(sb2);

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb1.toString());
			stmt.execute(sb2.toString());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	private static void peptideTest(String in) throws IOException {

		File[] files = (new File(in)).listFiles();
		for (int i = 1; i < files.length; i++) {

			BufferedReader freader = new BufferedReader(new FileReader(new File(files[i], "peptide.txt")));

			int count = 0;
			int total = 0;
			String frline = null;
			while ((frline = freader.readLine()) != null) {

				String[] cs = frline.split("\t");
				if (cs.length > 2) {
					count++;
				}
				total++;
			}
			freader.close();

			System.out.println(count + "\t" + total + "\t" + (double) count / (double) total);
		}
	}
	
	private static void split(String in) throws IOException {
		File downloadFile = new File(in, "download.bat");

		BufferedReader reader = new BufferedReader(new FileReader(downloadFile));
		String line = reader.readLine();

		PrintWriter[] writers = new PrintWriter[10];
		for (int i = 0; i < writers.length; i++) {
			writers[i] = new PrintWriter(new File(in, "download" + (i + 1) + ".bat"));
			writers[i].println(line);
		}

		int count = 0;
		while ((line = reader.readLine()) != null) {
			int fileId = count++ % writers.length;
			writers[fileId].println(line);
			writers[fileId].println(reader.readLine());
		}

		reader.close();
		for (int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
	}

	private static void combine(String in) throws IOException {
		int count = 0;
		PrintWriter writer = new PrintWriter(new File(in, "MGYG.combine.fasta"));
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File dbFile = new File(files[i], "MGYG" + files[i].getName() + ".faa");
				if (files[i].listFiles().length == 9) {

					BufferedReader reader = new BufferedReader(new FileReader(dbFile));
					String line = null;
					while ((line = reader.readLine()) != null) {
						writer.println(line);
					}
					reader.close();
					count++;

				} else {
					System.out.println(dbFile);

					File tempFile = new File(in, "temp.bat");
					PrintWriter temPrintWriter = new PrintWriter(tempFile);
					temPrintWriter.println("cd " + files[i]);
					temPrintWriter.print("C:\\Windows\\System32\\wget.exe -i " + files[i] + "\\download.txt");
					temPrintWriter.close();

					Runtime runtime = Runtime.getRuntime();
					try {
						Process process = runtime.exec(tempFile.getAbsolutePath());

						BufferedInputStream bin = new BufferedInputStream(process.getInputStream());
						BufferedReader inBr = new BufferedReader(new InputStreamReader(bin));

						String line1 = "";
						while ((line1 = inBr.readLine()) != null) {
							System.out.println(line1);
						}

						if (process.waitFor() != 0) {
							if (process.exitValue() == 1)
								System.err.println("false");
						}
						inBr.close();
						bin.close();

						Thread.sleep(1000);

					} catch (Exception e) {
						System.err.println(e);
					}
				}
			}
		}
		writer.close();
		System.out.println(count);

	}
	
	private static void removeDuplicate(String in) {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File batFile = new File(files[i], "download.bat");
				File downloadFile = new File(files[i], "download.txt");
				if (batFile.exists() && downloadFile.exists()) {
					File[] filesiFiles = files[i].listFiles();
					for (int j = 0; j < filesiFiles.length; j++) {
						if (filesiFiles[j].getName().endsWith(".1")) {
							FileUtils.deleteQuietly(filesiFiles[j]);
						}
					}
				}
			}
		}
	}
	
	private static void copy(String in, String out) throws IOException {
		PrintWriter writer = new PrintWriter(new File(out, "download.bat"));
		writer.println("E:");
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File batFile = new File(files[i], "download.bat");
				File downloadFile = new File(files[i], "download.txt");
				if (batFile.exists() && downloadFile.exists() && files[i].listFiles().length != 9) {
					File newFile = new File(out, files[i].getName());
					newFile.mkdir();

					FileUtils.copyFile(batFile, new File(newFile, "download.bat"));
					FileUtils.copyFile(downloadFile, new File(newFile, "download.txt"));

					writer.println("cd E:\\searches\\20211220_newdb\\" + files[i].getName());
					writer.println("wget -i E:\\searches\\20211220_newdb\\" + files[i].getName() + "\\download.txt");
				}
			}
		}
		writer.close();
	}
	
	private static void copy1(String in, String out) throws IOException {
		PrintWriter writer = new PrintWriter(new File(out, "download.bat"));
		writer.println("E:");
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File batFile = new File(files[i], "download.bat");
				File downloadFile = new File(files[i], "download.txt");

				if (batFile.exists() && downloadFile.exists() && files[i].listFiles().length != 9) {
					String oldNameString = files[i].getName();
					String newNameString = oldNameString + ".1";

					File newFile = new File(out, newNameString);
					newFile.mkdir();

					BufferedReader reader = new BufferedReader(new FileReader(downloadFile));
					PrintWriter writer1 = new PrintWriter(new File(newFile, "download.txt"));

					String line = null;
					while ((line = reader.readLine()) != null) {
						String line1 = line.replaceAll(oldNameString, newNameString);
						writer1.println(line1);
					}
					reader.close();
					writer1.close();

					PrintWriter writer2 = new PrintWriter(new File(newFile, "download.bat"));
					writer2.println("cd E:\\searches\\20211220_newdb\\" + newNameString);
					writer2.println("wget -i E:\\searches\\20211220_newdb\\" + newNameString + "\\download.txt");
					writer2.close();

					writer.println("cd E:\\searches\\20211220_newdb\\" + newNameString);
					writer.println("wget -i E:\\searches\\20211220_newdb\\" + newNameString + "\\download.txt");
				}
			}
		}
		writer.close();
	}

	private static void testExist(String in) throws IOException {

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File dlFile = new File(files[i], "download.txt");
				if (dlFile.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(dlFile));
					String line = reader.readLine();
					reader.close();
					URL url = new URL(line);
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();

					int responseCode = huc.getResponseCode();
					if (responseCode == 404) {
						System.out.println(responseCode+"\t"+url);
					}
				}
			}
		}
	}
	
	private static void extractCOG(String in, String cogInfo, String out) throws IOException {

		HashMap<String, String> cogMap = new HashMap<String, String>();
		BufferedReader cogReader = new BufferedReader(new FileReader(cogInfo));
		String coglineString = null;
		while ((coglineString = cogReader.readLine()) != null) {
			String[] cs = coglineString.split(",");
			String name = cs[2].replace('.', '_');
			cogMap.put(name, cs[6]);
		}
		cogReader.close();

		System.out.println("cog\t" + cogMap.size());

		String lastNameString = "";
		int count = 0;
		PrintWriter writer = new PrintWriter(out);
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {

			String[] cs = line.split("\t");
			if (cs[0].equals(lastNameString)) {
				continue;
			}

			lastNameString = cs[0];

			if (cogMap.containsKey(cs[1])) {
				writer.println(cs[0] + "\t" + cogMap.get(cs[1]));
			}
			if (count++ % 50000 == 0) {
				System.out.println(count);
			}
		}
		reader.close();
		writer.close();
	}
	
	private static void findFasta(String in) {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File dlFile = new File(files[i], "download.txt");
				if (dlFile.exists() && files[i].listFiles().length > 2) {
					System.out.println(files[i].getName());
				} else {
					FileUtils.deleteQuietly(files[i]);
				}
			}
		}
	}
	
	private static void copyFasta(String in, String out) throws IOException {

		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File fastaFile = new File(files[i], "MGYG"+files[i].getName() + ".faa");
				if (fastaFile.exists()) {
					File newFile = new File(out, fastaFile.getName());
					FileUtils.copyFile(fastaFile, newFile);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

//		ProDbConfig.findFasta("Z:\\Kai\\miss_db");
		
//		ProDbConfig.extractCOG("Z:\\Kai\\miss_db\\genomes_16.M8", "D:\\Database\\COG\\cog-20.cog.csv",
//				"Z:\\Kai\\miss_db\\extract_cog.tsv");

//		ProDbConfig.removeDuplicate("Z:\\Kai\\20211220_newdb");
//		ProDbConfig.copy1("Z:\\Kai\\20211220_newdb", "Z:\\Kai\\miss_db");
//		ProDbConfig.combine("Z:\\Kai\\miss_db");
//		ProDbConfig.testExist("Z:\\Kai\\20211220_newdb");

//		ProDbConfig.split("Z:\\Kai");
//		ProDbConfig.download(args[0]);
		
//		ProDbConfig.generatePepDb("D:\\Data\\new_db_4\\original_db", "D:\\Data\\new_db_4\\tryptic_peptide.fasta");

//		ProDbConfig.copyFasta("Z:\\Kai\\miss_db", "Z:\\Kai\\HGM_DB\\MGYG\\original_db");

//		ProDbConfig.createPep2ProTable("D:\\Data\\new_db_4\\original_db");

//		ProDbConfig.createPro2FuncTable("D:\\Data\\new_db_4\\original_db", "D:\\Data\\new_db_4\\columnNames.txt");

//		ProDbConfig.dropTable("D:\\Data\\new_db_4\\original_db");
		
//		ProDbConfig.peptideTest("D:\\Data\\new_db_4\\original_db");

		ProDbConfig.createPro2FuncTable20220308("Z:\\Kai\\20211220_newdb\\All_db", "Z:\\Kai\\20211220_newdb\\extract_cog.tsv",
				"Z:\\Kai\\20211220_newdb\\All_UHGP_KofamKOALA");
		
//		ProDbConfig.getCOG("D:\\Data\\new_db_4\\original_db", 
//				"D:\\Exported\\exe\\Resources\\taxonomy-all.tab");
		
//		ProDbConfig.createSqliteDb("UHGG_func_anno.db");
		
//		ProDbConfig.createGenomeGTDBNCBITaxaTable("Z:\\Kai\\20211220_newdb\\GTDB_NCBI_taxa.tsv");
		
//		ProDbConfig.createPro2FuncTable20220104("Z:\\Kai\\miss_db", "Z:\\Kai\\miss_db\\extract_cog.tsv");
		
//		ProDbConfig.addPro2FuncTable("D:\\Data\\new_db_4\\original_db", "D:\\Data\\new_db_4\\columnNames.txt",
//				"D:\\Exported\\exe\\Resources\\taxonomy-all.tab", "D:\\Database\\COG\\cog-20.def.tab",
//				"D:\\Database\\NOG\\e5.og_annotations.tsv", "D:\\Data\\new_db_4\\miss.txt");
		
//		ProDbConfig.createCogTable("D:\\Database\\COG\\cog-20.def.tab");
		
//		ProDbConfig.createNogTable("D:\\Database\\NOG\\e5.og_annotations.tsv");
		
//		ProDbConfig.createGOTable("D:\\Exported\\exe\\Resources\\function\\go.obo");
		
//		ProDbConfig.createECTable("D:\\Exported\\exe\\Resources\\function\\enzyme.dat");
		
//		ProDbConfig.createKEGGTable("D:\\Exported\\exe\\Resources\\function\\KeggMap20211018.txt");
		
//		ProDbConfig.createPro2FuncTable("jdbc:sqlite:D:/Data/new_db_4/func.db",
//				"Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\mouse.emapper.annotations", "mouse",
//				"D:\\Exported\\exe\\Resources\\taxonomy-all.tab", "D:\\Database\\COG\\cog-20.def.tab",
//				"D:\\Database\\NOG\\e5.og_annotations.tsv");
				
	}

}
