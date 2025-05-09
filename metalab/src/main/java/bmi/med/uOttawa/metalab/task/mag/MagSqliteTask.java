package bmi.med.uOttawa.metalab.task.mag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

public class MagSqliteTask {

	private static String taskName = "SQL database create task";
	private static final Logger LOGGER = LogManager.getLogger(MagSqliteTask.class);
	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public static void create(File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");

		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				LOGGER.info(taskName + ": a new database has been created in " + sqldb + ", the driver name is "
						+ meta.getDriverName());
				System.out.println(format.format(new Date()) + "\t" + taskName + ": a new database has been created in "
						+ sqldb + ", the driver name is " + meta.getDriverName());
			} else {
				LOGGER.info(taskName + ": a SQL database already exist in " + sqldb);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": a SQL database already exist in " + sqldb);
			}

		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in creating a new SQL database in " + sqldb, e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in creating a new SQL database in " + sqldb);
		}
	}

	public static void createFuncTable(String funcDir, File sqldb) throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		File[] files = (new File(funcDir)).listFiles();
		if (files.length > 0) {

			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in connecting to the SQL database in " + sqldb);
				return;
			}

			StringBuilder titlesb = new StringBuilder("CREATE TABLE IF NOT EXISTS Function (\n");
			StringBuilder fNameSb = new StringBuilder("INSERT INTO FuncName (fName) \nVALUES\n");
			StringBuilder sb = new StringBuilder("INSERT INTO Function (");

			try (BufferedReader reader = new BufferedReader(new FileReader(files[0]))) {
				String[] title = reader.readLine().split("\t");
				titlesb.append(" protein VARCHAR(50),\n");
				sb.append("protein");
				fNameSb.append("(\"protein\")");

				for (int i = 4; i < title.length; i++) {
					titlesb.append(" ").append(title[i]).append(" text,\n");
					sb.append(",").append(title[i]);
					fNameSb.append(",(\"").append(title[i]).append("\")");
				}
				titlesb.append("PRIMARY KEY (protein));");
				reader.close();

			} catch (IOException e) {
				LOGGER.error(taskName + ": error in reading " + files[0], e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + files[0]);

				return;
			}

			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(
						"CREATE TABLE IF NOT EXISTS FuncName (\n fName VARCHAR(100),\n PRIMARY KEY (fName));");
				stmt.executeUpdate(fNameSb.toString());
				stmt.executeUpdate(titlesb.toString());
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in creating the table to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in creating the table to the SQL database in " + sqldb);
			}

			sb.append(") \nVALUES\n");

			StringBuilder sbi = new StringBuilder();
			for (int i = 0; i < files.length; i++) {
				try (BufferedReader reader = new BufferedReader(new FileReader(files[i]))) {
					String line = reader.readLine();
					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						sbi.append("(\"").append(cs[0]).append("\"");
						for (int j = 4; j < cs.length; j++) {
							sbi.append(",\"").append(cs[j]).append("\"");
						}
						sbi.append("),");
					}
					reader.close();

				} catch (IOException e) {
					LOGGER.error(taskName + ": error in reading " + files[i], e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + files[i]);

					return;
				}
				if (i % 100 == 0) {
					sbi.deleteCharAt(sbi.length() - 1);
					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate(sb.toString() + sbi.toString());
						stmt.close();
						sbi = new StringBuilder();
					} catch (SQLException e) {
						LOGGER.error(taskName + ": error in inserting the information to the function table " + sqldb,
								e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in inserting the information to the function table " + sqldb);
					}
				}
			}

			if (sbi.length() > 1) {
				sbi.deleteCharAt(sbi.length() - 1);
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate(sb.toString() + sbi.toString());
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(taskName + ": error in inserting the information to the function table " + sqldb, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in inserting the information to the function table " + sqldb);
				}
			}

			try {
				conn.close();

				LOGGER.info(taskName + ": the function table was created in " + sqldb);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": the function table was created in " + sqldb);

			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in closing the connect to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in closing the connect to the SQL database in " + sqldb);
			}
		}
	}

	public static void createPeptideTable(String pepDir, File sqldb) throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		File[] files = (new File(pepDir)).listFiles();
		if (files.length > 0) {

			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in connecting to the SQL database in " + sqldb);
				return;
			}

			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("DROP TABLE IF EXISTS Peptide");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}

			StringBuilder titlesb = new StringBuilder("CREATE TABLE IF NOT EXISTS Peptide (\n");
			titlesb.append(" pepid INTEGER NOT NULL,\n");
			titlesb.append(" pro TEXT NOT NULL,\n");
			titlesb.append(" pep TEXT NOT NULL,\n");
			titlesb.append(" score TEXT NOT NULL,\n");
			titlesb.append(" PRIMARY KEY (pepid));");

			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(titlesb.toString());
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in creating the peptide table to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in creating the peptide table to the SQL database in " + sqldb);
			}

			StringBuilder sb = new StringBuilder("INSERT INTO Peptide (pepid, pro, pep, score) \nVALUES\n");
			int totalCount = 0;
			for (int i = 0; i < files.length; i++) {
				try (BufferedReader reader = new BufferedReader(new FileReader(files[i]))) {
					String line = reader.readLine();
					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						sb.append("(").append(totalCount++);
						sb.append(",'").append(cs[0]).append("'");
						sb.append(",'").append(cs[1]).append("'");
						if (cs[2].length() > 6) {
							sb.append(",").append(cs[2].subSequence(0, 6));
						} else {
							sb.append(",").append(cs[2]);
						}
						sb.append("),");
					}
					reader.close();

				} catch (IOException e) {
					LOGGER.error(taskName + ": error in reading " + files[0], e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + files[0]);

					return;
				}

				if (i % 100 == 0) {
					sb.deleteCharAt(sb.length() - 1);
					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate(sb.toString());
						stmt.close();
						sb = new StringBuilder("INSERT INTO Peptide (pepid, pro, pep, score) \nVALUES\n");

					} catch (SQLException e) {
						LOGGER.error(taskName + ": error in inserting the information to the peptide table " + sqldb,
								e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in inserting the information to the peptide table " + sqldb);
					}
				}
			}

			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate(sb.toString());
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(taskName + ": error in inserting the information to the peptide table " + sqldb, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in inserting the information to the peptide table " + sqldb);
					return;
				}
			}

			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("CREATE INDEX idx_pep ON Peptide (pep);");
				stmt.executeUpdate("CREATE INDEX idx_pro ON Peptide (pro);");
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in creating the index to the peptide table " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in creating the index to the peptide table " + sqldb);
				return;
			}

			try {
				LOGGER.info(taskName + ": the peptide table was created in " + sqldb);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": the peptide table was created in " + sqldb);
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(taskName + ": error in closing the connect to the SQL database in " + sqldb, e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in closing the connect to the SQL database in " + sqldb);
			}
		}
	}

	private static void createPepProTables(Connection conn, int tableId) throws SQLException {
		String createProteinsTable = "CREATE TABLE IF NOT EXISTS Proteins" + tableId + " ("
				+ "protein_id INTEGER PRIMARY KEY," + "protein_name VARCHAR(255) NOT NULL)";

		String createPeptidesTable = "CREATE TABLE IF NOT EXISTS Peptides" + tableId + " ("
				+ "peptide_id INTEGER PRIMARY KEY," + "peptide_sequence VARCHAR(255) NOT NULL)";

		String createPeptideProteinMatchesTable = "CREATE TABLE IF NOT EXISTS PeptideProteinMatches" + tableId + " ("
				+ "protein_id INTEGER," + "peptide_id INTEGER," + "score TEXT NOT NULL,"
				+ "PRIMARY KEY (protein_id, peptide_id)," + "FOREIGN KEY (protein_id) REFERENCES Proteins" + tableId
				+ "(protein_id)," + "FOREIGN KEY (peptide_id) REFERENCES Peptides" + tableId + "(peptide_id))";

//		String createPeptideProteinMatchesTable = "CREATE TABLE IF NOT EXISTS PeptideProteinMatches" + tableId + " ("
//				+ "peptide_id INTEGER," + "protein_id INTEGER," + "score TEXT NOT NULL,"
//				+ "PRIMARY KEY (peptide_id, protein_id))";

		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(createProteinsTable);
			stmt.executeUpdate(createPeptidesTable);
			stmt.executeUpdate(createPeptideProteinMatchesTable);
			stmt.close();
		}
	}

	private static void insertBatch(Connection conn, HashMap<String, Integer> proteinMap,
			HashMap<String, Integer> peptideMap, HashMap<String, Float> peptideProteinMap, int tableId)
			throws SQLException {
		long start = System.currentTimeMillis();
		// Insert Proteins

		String insertProteinSQL = "INSERT INTO Proteins" + tableId + " (protein_id, protein_name) VALUES (?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(insertProteinSQL)) {
			conn.setAutoCommit(false);
			for (String protein : proteinMap.keySet()) {
				pstmt.setInt(1, proteinMap.get(protein));
				pstmt.setString(2, protein);
				pstmt.addBatch();
			}
			pstmt.executeBatch(); // Insert remaining records
			conn.commit(); // Commit transaction
			conn.setAutoCommit(true);
		}

		long proTime = System.currentTimeMillis();
		System.out.println("Insert protein using " + (proTime - start) / 60000 + " min");

		// Insert Peptides
		String insertPeptideSQL = "INSERT INTO Peptides" + tableId + " (peptide_id, peptide_sequence) VALUES (?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(insertPeptideSQL)) {
			conn.setAutoCommit(false);
			int count = 0;
			for (String peptide : peptideMap.keySet()) {
				pstmt.setInt(1, peptideMap.get(peptide));
				pstmt.setString(2, peptide);
				pstmt.addBatch();
				if (++count % 1000 == 0) {
					pstmt.executeBatch();
				}
			}
			pstmt.executeBatch(); // Insert remaining records
			conn.commit(); // Commit transaction
			conn.setAutoCommit(true);
		}

		long pepTime = System.currentTimeMillis();
		System.out.println("Insert peptide using " + (pepTime - proTime) / 60000 + " min");

		// Insert Peptide-Protein Matches
		String insertPeptideProteinMatchSQL = "INSERT INTO PeptideProteinMatches" + tableId
				+ " (protein_id, peptide_id, score) VALUES (?,?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(insertPeptideProteinMatchSQL)) {
			conn.setAutoCommit(false);
			int count = 0;
			for (String key : peptideProteinMap.keySet()) {
				String[] cs = key.split("\t");
				pstmt.setInt(1, Integer.parseInt(cs[0]));
				pstmt.setInt(2, Integer.parseInt(cs[1]));
				String score = String.valueOf(peptideProteinMap.get(key));
				if (score.length() > 7) {
					score = score.substring(0, 7);
				}
				pstmt.setString(3, score);
				pstmt.addBatch();

				if (++count % 1000 == 0) {
					pstmt.executeBatch();
				}
			}
			pstmt.executeBatch(); // Insert remaining records
			conn.commit(); // Commit transaction
			conn.setAutoCommit(true);
		}

		long end = System.currentTimeMillis();

		System.out.println("Insert peppro using " + (end - start) / 60000 + " min");
	}

	public static void createPeptideSeparateTable(String pepDir, File sqldb) throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		File[] files = (new File(pepDir)).listFiles();

		try (Connection conn = DriverManager.getConnection(url)) {

			HashMap<String, Integer> pepMap = new HashMap<String, Integer>(100000000);
			HashMap<String, Integer> proMap = new HashMap<String, Integer>(10000000);
			HashMap<String, Float> pepProMap = new HashMap<String, Float>(100000000);
			int tableId = 1;

			for (int i = 0; i < files.length; i++) {
				try (BufferedReader reader = new BufferedReader(new FileReader(files[i]))) {
					String pline = reader.readLine();
					while ((pline = reader.readLine()) != null) {
						String[] cs = pline.split("\t");
						if (cs[2].contains("e")) {
							continue;
						}
						int pepId = -1;
						int proId = -1;
						if (!pepMap.containsKey(cs[1])) {
							pepMap.put(cs[1], pepMap.size());
						}
						pepId = pepMap.get(cs[1]);

						if (!proMap.containsKey(cs[0])) {
							proMap.put(cs[0], proMap.size());
						}
						proId = proMap.get(cs[0]);

						float score = Float.parseFloat(cs[2]);

						String key = proId + "\t" + pepId;
						if (pepProMap.containsKey(key)) {
							if (score > pepProMap.get(key)) {
								pepProMap.put(key, score);
							}
						} else {
							pepProMap.put(key, score);
						}
					}
					reader.close();
				} catch (IOException e) {

				}

				if ((i + 1) % 100 == 0) {

					createPepProTables(conn, tableId);

					System.out.println(tableId + "\t" + proMap.size() + "\t" + pepMap.size() + "\t" + pepProMap.size());

					insertBatch(conn, proMap, pepMap, pepProMap, tableId);

					pepMap = new HashMap<String, Integer>(100000000);
					proMap = new HashMap<String, Integer>(10000000);
					pepProMap = new HashMap<String, Float>(100000000);
					tableId++;
				}
			}

			createPepProTables(conn, tableId);

			System.out.println(tableId + "\t" + proMap.size() + "\t" + pepMap.size() + "\t" + pepProMap.size());

			insertBatch(conn, proMap, pepMap, pepProMap, tableId);

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createPeptideShardTable(String pepDir, File sqldb) throws NumberFormatException, IOException {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		File[] files = (new File(pepDir)).listFiles();
		if (files.length > 0) {

			for (File file : files) {
				try (Connection conn = DriverManager.getConnection(url)) {

					// Ensure the connection is not in read-only mode
					if (conn.isReadOnly()) {
						conn.setReadOnly(false);
					}

					// Start a transaction
					conn.setAutoCommit(false);

					// Loop through the list of values and set them to the prepared statement
					String fileName = file.getName();
					String genome = fileName.substring(0, fileName.indexOf("."));
					StringBuilder titlesb = new StringBuilder("CREATE TABLE IF NOT EXISTS " + genome + " (\n");
					titlesb.append(" pro TEXT NOT NULL,\n");
					titlesb.append(" pep TEXT NOT NULL,\n");
					titlesb.append(" score TEXT NOT NULL);");

					try (Statement stmt = conn.createStatement()) {
						stmt.executeUpdate(titlesb.toString());
						stmt.close();
					} catch (SQLException e) {
						LOGGER.error(taskName + ": error in creating the peptide table to the SQL database in " + sqldb,
								e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in creating the peptide table to the SQL database in " + sqldb);
					}

					String sql = "INSERT INTO " + genome + "(pro, pep, score) VALUES(?, ?, ?)";

					PreparedStatement pstmt = conn.prepareStatement(sql);
					try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
						String pline = reader.readLine();
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							for (int i = 0; i < cs.length; i++) {
								pstmt.setString(i + 1, cs[i]);
							}
							pstmt.addBatch();
						}
						reader.close();
					} catch (IOException e) {

					}
					pstmt.executeBatch();
					// Execute the insert statement

					// Commit the transaction
					conn.commit();
					System.out.println("Records inserted successfully!");
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void createTaxaTable(String taxDbFile, File sqldb) throws NumberFormatException, IOException {
		// SQLite connection string
		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");

		HashMap<String, String[]> taxaMap = new HashMap<String, String[]>();
		try (BufferedReader taxaReader = new BufferedReader(new FileReader(taxDbFile))) {
			String line = taxaReader.readLine();
			String[] title = line.split("\t");
			int lineageId = -1;
			int genomeId = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Lineage") || title[i].startsWith("Species_designation")) {
					lineageId = i;
				} else if (title[i].startsWith("Species_rep") || title[i].startsWith("Species_representative")) {
					genomeId = i;
				}
			}

			if (lineageId == -1 || genomeId == -1) {
				LOGGER.error(taskName + ": error in reading " + taxDbFile + ", the title format is unknown");
				System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxDbFile
						+ ", the title format is unknown");
				return;
			}

			while ((line = taxaReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] lineange = cs[lineageId].split(";");
				String[] taxaString = new String[7];
				for (int i = 0; i < taxaString.length; i++) {
					if (i < lineange.length) {
						if (lineange[i].endsWith("__")) {
							taxaString[i] = "";
						} else {
							taxaString[i] = lineange[i];
						}

					} else {
						taxaString[i] = "";
					}
				}
				taxaMap.put(cs[genomeId], taxaString);
			}
			taxaReader.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in reading " + taxDbFile, e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in reading " + taxDbFile);

			return;
		}

		TaxonomyRanks[] ranks = TaxonomyRanks.getMainRanks7();

		String sql = "CREATE TABLE IF NOT EXISTS GenomeTaxon (\n genome VARCHAR(100),\n";
		StringBuilder titleSb = new StringBuilder("(genome,");
		for (int i = 0; i < ranks.length; i++) {
			String rank = ranks[i].getName();
			if (rank.equals("Class") || rank.equals("Order")) {
				sql += new String(" \"" + ranks[i].getName() + "\" text,\n");
				titleSb.append("\"").append(ranks[i].getName()).append("\",");
			} else {
				sql += new String(" " + ranks[i].getName() + " text,\n");
				titleSb.append(ranks[i].getName()).append(",");
			}
		}
		titleSb.deleteCharAt(titleSb.length() - 1);
		titleSb.append(")");

		sql += "PRIMARY KEY (genome));";

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in connecting to the SQL database in " + sqldb);
			return;
		}

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in creating the genome table to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in creating the genome table to the SQL database in " + sqldb);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO GenomeTaxon ").append(titleSb).append(" \nVALUES\n");
		for (String genome : taxaMap.keySet()) {
			sb.append("(\"").append(genome).append("\",");
			String[] taxaStrings = taxaMap.get(genome);
			for (int i = 0; i < taxaStrings.length; i++) {
				if (taxaStrings[i] != null) {
					sb.append("\"").append(taxaStrings[i]).append("\",");
				} else {
					sb.append("\"\",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("),");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");

		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sb.toString());
			stmt.close();
			conn.close();

			LOGGER.info(taskName + ": the genome table was created in " + sqldb);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": the genome table was created in " + sqldb);

		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in closing the connect to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in closing the connect to the SQL database in " + sqldb);
		}
	}

	public static String[][] queryProteinEggnog(HashSet<String> proSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in connecting to the SQL database in " + sqldb);
			return null;
		}

		StringBuilder sql = new StringBuilder("SELECT protein, eggNOG_OGs FROM Function WHERE protein IN (");
		for (int i = 0; i < proSet.size(); i++) {
			sql.append("?");
			if (i < proSet.size() - 1) {
				sql.append(", ");
			}
		}
		sql.append(");");

		ArrayList<String[]> list = new ArrayList<String[]>();
		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			Iterator<String> iterator = proSet.iterator();
			while (iterator.hasNext()) {
				pstmt.setString(index++, iterator.next());
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(new String[] { rs.getString("eggNOG_OGs"), rs.getString("eggNOG_OGs") });
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[][] contents = list.toArray(new String[list.size()][]);

		return contents;
	}

	public static ConcurrentHashMap<String, String> queryProteinEggnog(HashSet<String> proSet, File sqldb,
			int threadCount) {
		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		List<List<String>> chunkList = chunkify(proSet, 10000);

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setMaximumPoolSize(threadCount);
		HikariDataSource dataSource = new HikariDataSource(config);

		ConcurrentHashMap<String, String> proEggNogMap = new ConcurrentHashMap<String, String>();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (List<String> chunkIList : chunkList) {
			executor.execute(() -> {
				try {
					StringBuilder sql = new StringBuilder(
							"SELECT protein, eggNOG_OGs FROM Function WHERE protein IN (");
					for (int j = 0; j < chunkIList.size(); j++) {
						sql.append("?,");
					}
					sql.deleteCharAt(sql.length() - 1);
					sql.append(");");

					Connection conn = dataSource.getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sql.toString());
					for (int j = 0; j < chunkIList.size(); j++) {
						pstmt.setString(j + 1, chunkIList.get(j));
					}
					HashMap<String, String> tempMap = new HashMap<String, String>();
					try (ResultSet rs = pstmt.executeQuery()) {
						while (rs.next()) {
							tempMap.put(rs.getString(1), rs.getString(2));
						}
					}
					synchronized (proEggNogMap) {
						proEggNogMap.putAll(tempMap);
					}

				} catch (SQLException e) {
					LOGGER.error(taskName + ": error in query proteins", e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in query proteins");
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {
				LOGGER.info(taskName + ": query proteins finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": query proteins finished");

				dataSource.close();
			} else {
				LOGGER.error(taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
			}
		} catch (InterruptedException e) {

			LOGGER.error(taskName + ": error in query proteins", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in query proteins");
		}

		return proEggNogMap;
	}

	public static String[][] queryProFunc(HashSet<String> proSet, File sqldb, int threadCount) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in connecting to the SQL database in " + sqldb);
			return null;
		}

		ArrayList<String> nameList = new ArrayList<String>();
		StringBuilder sql = new StringBuilder("SELECT ");
		try (PreparedStatement pstmt = conn.prepareStatement("SELECT fName FROM FuncName")) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				sql.append(name).append(", ");
				nameList.add(name);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql.deleteCharAt(sql.length() - 2);
		sql.append("FROM Function WHERE protein IN (");

		String[] names = nameList.toArray(String[]::new);
		List<List<String>> chunkList = chunkify(proSet, 10000);

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setMaximumPoolSize(threadCount);
		HikariDataSource dataSource = new HikariDataSource(config);

		ConcurrentHashMap<String, String[]> proFuncMap = new ConcurrentHashMap<String, String[]>();
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (List<String> chunkIList : chunkList) {
			executor.execute(() -> {
				try {
					StringBuilder sqlI = new StringBuilder(sql);
					for (int j = 0; j < chunkIList.size(); j++) {
						sqlI.append("?,");
					}
					sqlI.deleteCharAt(sqlI.length() - 1);
					sqlI.append(");");

					Connection connI = dataSource.getConnection();
					PreparedStatement pstmt = connI.prepareStatement(sqlI.toString());
					for (int j = 0; j < chunkIList.size(); j++) {
						pstmt.setString(j + 1, chunkIList.get(j));
					}
					HashMap<String, String[]> tempMap = new HashMap<String, String[]>();
					try (ResultSet rs = pstmt.executeQuery()) {
						while (rs.next()) {

							String[] cs = new String[names.length];
							for (int i = 0; i < cs.length; i++) {
								cs[i] = rs.getString(i + 1);
							}
							tempMap.put(cs[0], cs);
						}
					}
					synchronized (proFuncMap) {
						proFuncMap.putAll(tempMap);
					}

				} catch (SQLException e) {
					LOGGER.error(taskName + ": error in query proteins", e);
					System.err.println(format.format(new Date()) + "\t" + taskName + ": error in query proteins");
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {
				LOGGER.info(taskName + ": query proteins finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": query proteins finished");

				dataSource.close();
			} else {
				LOGGER.error(taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": the program didn't responce in a long time, increase the thread count will be helpful");
			}
		} catch (InterruptedException e) {

			LOGGER.error(taskName + ": error in query proteins", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in query proteins");
		}

		String[][] contents = proFuncMap.values().toArray(new String[proFuncMap.size()][]);
		return contents;
	}

	@SuppressWarnings("unchecked")
	public static String[][] queryProFunc(HashSet<String> proSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			LOGGER.error(taskName + ": error in connecting to the SQL database in " + sqldb, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in connecting to the SQL database in " + sqldb);
			return null;
		}

		ArrayList<String> nameList = new ArrayList<String>();
		StringBuilder sql = new StringBuilder("SELECT ");
		try (PreparedStatement pstmt = conn.prepareStatement("SELECT fName FROM FuncName")) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				sql.append(name).append(", ");
				nameList.add(name);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql.deleteCharAt(sql.length() - 2);
		sql.append("FROM Function WHERE protein IN (");

		ArrayList<String[]> list = new ArrayList<String[]>();
		String[] names = nameList.toArray(String[]::new);

		if (proSet.size() > 30000) {
			int subProSetCount = proSet.size() / 30000;
			HashSet<String>[] proSets = new HashSet[subProSetCount];
			for (int i = 0; i < proSets.length; i++) {
				proSets[i] = new HashSet<String>();
			}

			int id = 0;
			for (String pro : proSet) {
				proSets[id % subProSetCount].add(pro);
				id++;
			}

			StringBuilder subSb = new StringBuilder("");
			for (HashSet<String> subProSet : proSets) {
				for (int i = 0; i < subProSet.size(); i++) {
					subSb.append("?");
					if (i < subProSet.size() - 1) {
						subSb.append(", ");
					}
				}
				subSb.append(");");

				try (PreparedStatement pstmt = conn.prepareStatement(sql.toString() + subSb.toString())) {
					int index = 1;
					Iterator<String> iterator = subProSet.iterator();
					while (iterator.hasNext()) {
						pstmt.setString(index++, iterator.next());
					}

					String[] content = new String[names.length];
					try (ResultSet rs = pstmt.executeQuery()) {
						while (rs.next()) {
							for (int i = 0; i < content.length; i++) {
								content[i] = rs.getString(names[i]);
							}
							list.add(content);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			for (int i = 0; i < proSet.size(); i++) {
				sql.append("?");
				if (i < proSet.size() - 1) {
					sql.append(", ");
				}
			}
			sql.append(");");

			try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
				int index = 1;
				Iterator<String> iterator = proSet.iterator();
				while (iterator.hasNext()) {
					pstmt.setString(index++, iterator.next());
				}

				String[] content = new String[names.length];
				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						for (int i = 0; i < content.length; i++) {
							content[i] = rs.getString(names[i]);
						}
						list.add(content);
					}
				}
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	public static String[][] queryPeptide(HashSet<String> pepSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		/*
		 * try (Statement stmt = conn.createStatement()) {
		 * stmt.executeUpdate("CREATE INDEX idx_pep ON Peptide (pep);"); conn.close(); }
		 * catch (SQLException e) { System.out.println(e.getMessage()); }
		 */
		ArrayList<String[]> list = new ArrayList<String[]>(10000000);

		StringBuilder sql = new StringBuilder("SELECT pro, pep, score FROM Peptide WHERE pep IN (");

		for (int i = 0; i < pepSet.size(); i++) {
			sql.append("?");
			if (i < pepSet.size() - 1) {
				sql.append(", ");
			}
		}
		sql.append(");");

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			Iterator<String> iterator = pepSet.iterator();
			while (iterator.hasNext()) {
				pstmt.setString(index++, iterator.next());
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {

					String proid = rs.getString("pro");
					String pep = rs.getString("pep");
					String score = rs.getString("score");
					String[] content = new String[] { proid, pep, score };
					list.add(content);

				}
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	public static String[][] queryPeptideTempTable(HashSet<String> pepSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}

		ArrayList<String[]> list = new ArrayList<String[]>(10000000);

		StringBuilder sql = new StringBuilder("SELECT pro, pep, score FROM Peptide WHERE pep IN (");

		for (int i = 0; i < pepSet.size(); i++) {
			sql.append("?");
			if (i < pepSet.size() - 1) {
				sql.append(", ");
			}
		}
		sql.append(");");

		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {

			// Step 1: Create a temporary table
			String createTempTableSQL = "CREATE TEMPORARY TABLE temp_sequences (Sequence VARCHAR(255) PRIMARY KEY)";
			pstmt = conn.prepareStatement(createTempTableSQL);
			pstmt.execute();
			pstmt.close();

			// Step 2: Insert sequences into the temporary table
			String insertTempTableSQL = "INSERT INTO temp_sequences (Sequence) VALUES (?)";
			pstmt = conn.prepareStatement(insertTempTableSQL);
			conn.setAutoCommit(false); // Enable transaction for batch insert
			for (String seq : pepSet) {
				pstmt.setString(1, seq);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.close();

			// Step 3: Perform the join and retrieve the matching rows
			String joinSQL = "SELECT * FROM Peptide t INNER JOIN temp_sequences ts ON t.pep = ts.Sequence";
			pstmt = conn.prepareStatement(joinSQL);
			rs = pstmt.executeQuery();

			// Process the results
			while (rs.next()) {
//				String sequence = rs.getString("Sequence");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close resources
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	private static List<List<String>> chunkify(HashSet<String> set, int chunkSize) {
		List<List<String>> chunks = new ArrayList<>();
		List<String> currentChunk = new ArrayList<>(chunkSize);

		for (String item : set) {
			currentChunk.add(item);
			if (currentChunk.size() == chunkSize) {
				chunks.add(new ArrayList<>(currentChunk));
				currentChunk.clear();
			}
		}

		if (!currentChunk.isEmpty()) {
			chunks.add(currentChunk);
		}

		return chunks;
	}

	public static String[][] queryPeptideShard(HashSet<String> pepSet, File sqldb, String outputDir) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");

		ArrayList<String> ppmList = new ArrayList<String>();
		try (Connection conn = DriverManager.getConnection(url);
				PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table'")) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name.startsWith("PeptideProteinMatches")) {
					ppmList.add(name);
				}
			}
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println(ppmList.size());

		StringBuilder pepsql = new StringBuilder("(");
		for (int i = 0; i < pepSet.size(); i++) {
			pepsql.append("?,");
		}
		pepsql.deleteCharAt(pepsql.length() - 1);
		pepsql.append(");");

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setMaximumPoolSize(ppmList.size());
		HikariDataSource dataSource = new HikariDataSource(config);

		ExecutorService executor = Executors.newFixedThreadPool(ppmList.size());
		BufferedWriter[] writer = { null, null };
		try {
			writer[0] = new BufferedWriter(new FileWriter(new File(outputDir, "pep2pro.tsv"), true));
			writer[0].write("pro\tpep\tscore\n");
			writer[1] = new BufferedWriter(new FileWriter(new File(outputDir, "pro2pep.tsv"), true));
			writer[1].write("pro\tpep\tscore\n");
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the peptide rank information to " + outputDir);
		}

		for (String ppmName : ppmList) {
			executor.execute(() -> {
				long start = System.currentTimeMillis();

				String pepName = ppmName.replace("PeptideProteinMatches", "Peptides");
				String proName = ppmName.replace("PeptideProteinMatches", "Proteins");
				StringBuilder pep2ProQuery = new StringBuilder(
						"SELECT p.peptide_sequence, pr.protein_id, pr.protein_name, ppm.score " + "FROM " + ppmName
								+ " ppm " + "JOIN " + pepName + " p ON ppm.peptide_id = p.peptide_id " + "JOIN "
								+ proName + " pr ON ppm.protein_id = pr.protein_id " + "WHERE p.peptide_sequence IN ");
				StringBuilder pro2PepQuery = new StringBuilder("SELECT p.peptide_sequence, pr.protein_name, ppm.score "
						+ "FROM " + ppmName + " ppm " + "JOIN " + pepName + " p ON ppm.peptide_id = p.peptide_id "
						+ "JOIN " + proName + " pr ON ppm.protein_id = pr.protein_id " + "WHERE ppm.protein_id IN ");

				StringBuilder pep2ProSb = new StringBuilder();
				StringBuilder pro2PepSb = new StringBuilder();
				HashSet<String> proIdSet = new HashSet<String>();
				try (Connection conn = dataSource.getConnection()) {

					System.out.println("start\t" + ppmName);
					try (PreparedStatement pstmt = conn.prepareStatement(pep2ProQuery + "" + pepsql)) {
						int index = 1;
						for (String pep : pepSet) {
							pstmt.setString(index++, pep);
						}

						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								pep2ProSb.append(rs.getString(3)).append("\t").append(rs.getString(1)).append("\t")
										.append(rs.getString(4)).append("\n");
								proIdSet.add(rs.getString(2));
							}
						}
						pstmt.close();
					}
					System.out.println(ppmName + " proset\t" + proIdSet.size());

					StringBuilder prosql = new StringBuilder("(");
					for (int i = 0; i < proIdSet.size(); i++) {
						prosql.append("?,");
					}
					prosql.deleteCharAt(prosql.length() - 1);
					prosql.append(");");

					try (PreparedStatement pstmt = conn.prepareStatement(pro2PepQuery + "" + prosql)) {
						int index = 1;
						for (String pro : proIdSet) {
							pstmt.setString(index++, pro);
						}

						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								pro2PepSb.append(rs.getString(2)).append("\t").append(rs.getString(1)).append("\t")
										.append(rs.getString(3)).append("\n");
							}
						}
						pstmt.close();
					}
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();

				synchronized (writer[0]) {
					try {
						writer[0].write(pep2ProSb.toString());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in writing the peptide rank information to " + outputDir);
					}
				}

				synchronized (writer[1]) {
					try {
						writer[1].write(pro2PepSb.toString());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in writing the peptide rank information to " + outputDir);
					}
				}

				System.out.println(ppmName + "\t" + (end - start) / 1000);
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(12, TimeUnit.HOURS)) {
				try {
					writer[0].close();
					writer[1].close();
				} catch (IOException e) {
					LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in writing the peptide rank information to " + outputDir);
				}
			} else {

			}
		} catch (InterruptedException e) {

		}
		dataSource.close();

		return null;
	}

	public static String[][] queryPeptide2(HashSet<String> pepSet, File sqldb) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			// Step 1: Create a temporary table
			String createTempTableSQL = "CREATE TEMPORARY TABLE temp_pep (pep VARCHAR(255) PRIMARY KEY)";
			PreparedStatement pstmt = conn.prepareStatement(createTempTableSQL);
			pstmt.execute();
			pstmt.close();

			// Step 2: Insert sequences into the temporary table
			String insertTempTableSQL = "INSERT INTO temp_pep (pep) VALUES (?)";
			pstmt = conn.prepareStatement(insertTempTableSQL);
			conn.setAutoCommit(false); // Enable transaction for batch insert
			for (String seq : pepSet) {
				pstmt.setString(1, seq);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.close();

			// Step 3: Perform the join and retrieve the matching rows
			String joinSQL = "SELECT * FROM Peptide t INNER JOIN temp_pep ts ON t.pep = ts.pep";
			pstmt = conn.prepareStatement(joinSQL);
			ResultSet rs = pstmt.executeQuery();

			// Process the results
			while (rs.next()) {

			}

			conn.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	public static String[][] queryProtein(HashSet<String> proSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}

		StringBuilder sql = new StringBuilder("SELECT pro, pep, score FROM Peptide WHERE pro IN (");
		for (int i = 0; i < proSet.size(); i++) {
			sql.append("?");
			if (i < proSet.size() - 1) {
				sql.append(", ");
			}
		}
		sql.append(");");

		ArrayList<String[]> list = new ArrayList<String[]>();
		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			Iterator<String> iterator = proSet.iterator();
			while (iterator.hasNext()) {
				pstmt.setString(index++, iterator.next());
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String proid = rs.getString("pro");
					String pep = rs.getString("pep");
					String score = rs.getString("score");
					String[] content = new String[] { proid, pep, score };
					list.add(content);
				}
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	public static String[][] queryGenome(HashSet<String> genomeSet, File sqldb) {

		String url = "jdbc:sqlite:" + sqldb.getAbsolutePath().replaceAll("\\\\", "/");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			LOGGER.info(taskName + ": connecting to database " + sqldb, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": connecting to database " + sqldb);
			return null;
		}

		StringBuilder sql = new StringBuilder("SELECT genome,");

		TaxonomyRanks[] ranks = TaxonomyRanks.getMainRanks7();
		for (int i = 0; i < ranks.length; i++) {
			String rank = ranks[i].getName();
			if (rank.equals("Class") || rank.equals("Order")) {
				sql.append(" \"").append(ranks[i].getName()).append("\",");
			} else {
				sql.append(" ").append(ranks[i].getName()).append(",");
			}
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(" FROM GenomeTaxon WHERE genome IN (");

		for (int i = 0; i < genomeSet.size(); i++) {
			sql.append("?");
			if (i < genomeSet.size() - 1) {
				sql.append(", ");
			}
		}
		sql.append(");");

		ArrayList<String[]> list = new ArrayList<String[]>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			int index = 1;
			Iterator<String> iterator = genomeSet.iterator();
			while (iterator.hasNext()) {
				pstmt.setString(index++, iterator.next());
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					String[] content = new String[ranks.length + 1];
					content[0] = rs.getString("genome");
					for (int i = 1; i < content.length; i++) {
						content[i] = rs.getString(i + 1);
					}
					list.add(content);
				}
			}

			conn.close();

		} catch (SQLException e) {
			LOGGER.error(taskName + ": query genome information failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": query genome information failed");
		}

		String[][] contents = list.toArray(new String[list.size()][]);
		return contents;
	}

	@SuppressWarnings("unused")
	private static void test(String in, File dbFile, String out) {

		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				pepSet.add(cs[14]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pepset\t" + pepSet.size());

		long begin = System.currentTimeMillis();
		MagSqliteTask.queryPeptideShard(pepSet, dbFile, out);

		long peptime = System.currentTimeMillis();
		System.out.println("peptide time\t" + (peptime - begin) / 1000);

		/*
		 * HashSet<String> genomeSet = new HashSet<String>(); HashSet<String> proSet =
		 * new HashSet<String>(); try(BufferedReader reader = new BufferedReader(new
		 * FileReader(out))){
		 * 
		 * String line = null; while((line=reader.readLine())!=null) { String[] cs =
		 * line.split("_"); genomeSet.add(cs[0]); proSet.add(line); } reader.close();
		 * }catch(IOException e) {
		 * 
		 * } long peptime = System.currentTimeMillis();
		 * 
		 * System.out.println("genomeSet\t" + genomeSet.size());
		 * System.out.println("proSet\t" + proSet.size());
		 * 
		 * String[][] genomes = MagSqliteTask.queryGenome(genomeSet, dbFile);
		 * System.out.println("genomes\t" + genomes.length);
		 * 
		 * long genometime = System.currentTimeMillis();
		 * System.out.println("genome time\t" + (genometime - peptime) / 60000);
		 * 
		 * String[][] proteins = MagSqliteTask.queryProFunc(proSet, dbFile);
		 * System.out.println("proteins\t" + proteins.length); long protime =
		 * System.currentTimeMillis(); System.out.println("genome time\t" + (protime -
		 * genometime) / 60000);
		 */
	}

	@SuppressWarnings("unused")
	private static void protest(String proin, String db) {
		HashSet<String> proSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(proin))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				proSet.add(cs[0]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("proSet\t" + proSet.size());

		long start = System.currentTimeMillis();
		ConcurrentHashMap<String, String> map = queryProteinEggnog(proSet, new File(db), 12);
		long end = System.currentTimeMillis();
		System.out.println("map\t" + map.size() + "\t" + (end - start) / 1000);
	}

	@SuppressWarnings("unused")
	private static void noSQLTest(String in, String db, String outputDir) {

		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				pepSet.add(cs[14]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("pepset\t" + pepSet.size());

		long begin = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(50);
		BufferedWriter[] writer = { null, null };
		try {
			writer[0] = new BufferedWriter(new FileWriter(new File(outputDir, "pep2proNoSQL.tsv"), true));
			writer[0].write("pro\tpep\tscore\n");
			writer[1] = new BufferedWriter(new FileWriter(new File(outputDir, "pro2pepNoSQL.tsv"), true));
			writer[1].write("pro\tpep\tscore\n");
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
			System.err.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the peptide rank information to " + outputDir);
		}

		File[] files = (new File(db)).listFiles();
		for (File file : files) {
			executor.execute(() -> {
				HashSet<String> proSet = new HashSet<String>();
				StringBuilder pep2ProSb = new StringBuilder();
				StringBuilder pro2PepSb = new StringBuilder();
				try (BufferedReader pepProReader = new BufferedReader(new FileReader(file))) {
					String line = pepProReader.readLine();
					while ((line = pepProReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (pepSet.contains(cs[1])) {
							proSet.add(cs[0]);
							pep2ProSb.append(line).append("\n");
						}
					}
					pepProReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + ": error in reading the peptide information from " + file, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in reading the peptide information from " + file);
				}

				try (BufferedReader pepProReader = new BufferedReader(new FileReader(file))) {
					String line = pepProReader.readLine();
					while ((line = pepProReader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (proSet.contains(cs[0])) {
							pro2PepSb.append(line).append("\n");
						}
					}
					pepProReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + ": error in reading the peptide information from " + file, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in reading the peptide information from " + file);
				}

				synchronized (writer[0]) {
					try {
						writer[0].write(pep2ProSb.toString());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in writing the peptide rank information to " + outputDir);
					}
				}

				synchronized (writer[1]) {
					try {
						writer[1].write(pro2PepSb.toString());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in writing the peptide rank information to " + outputDir);
					}
				}

			});
		}
		executor.shutdown();

		try {
			if (executor.awaitTermination(10, TimeUnit.HOURS)) {
				try {
					writer[0].close();
					writer[1].close();
				} catch (IOException e) {
					LOGGER.error(taskName + ": error in writing the peptide rank information to " + outputDir, e);
					System.err.println(format.format(new Date()) + "\t" + taskName
							+ ": error in writing the peptide rank information to " + outputDir);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("noSQL\t" + (end - begin) / 1000);
	}
}
