package bmi.med.uOttawa.metalab.core.function.sql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SqliteManager {

	@SuppressWarnings("unused")
	private static void create(String in) {
		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/" + in;

		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created.");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void createNewTable(String in, String usedTitles, String name)
			throws NumberFormatException, IOException {
		// SQLite connection string
		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/func.db";

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

		BufferedReader reader = new BufferedReader(new FileReader(in));

		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == 22) {

				String sql = "CREATE TABLE IF NOT EXISTS " + name + " (\n query_name VARCHAR(100),\n";

				for (int i = 1; i < cs.length; i++) {

					if (map.containsKey(i + 1)) {

						sql += new String(" " + map.get(i + 1) + " text NOT NULL,\n");
					}
				}

				sql += new String(" " + "COG" + " text NOT NULL,\n");
				sql += new String(" " + "NOG" + " text NOT NULL,\n");

				sql += "PRIMARY KEY (query_name));";

				System.out.println(sql);

				try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
					// create a new table
					stmt.execute(sql);
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}

				break;
			}

		}
		reader.close();
	}

	@SuppressWarnings("unused")
	private static void insert(String in, String usedTitles, String name) throws NumberFormatException, IOException {
		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/func.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return;
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

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		StringBuilder titlesb = new StringBuilder("query_name");

		while ((line = reader.readLine()) != null) {
			String[] title = line.split("\t");
			if (title.length == 22) {

				for (int i = 1; i < title.length; i++) {
					if (map.containsKey(i + 1)) {
						titlesb.append(",").append(map.get(i + 1));
					}
				}
				System.out.println(titlesb + "\t" + title.length);

				break;
			}
		}

		int count = 0;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == 22) {

				sb.append("(");
				for (int i = 0; i < cs.length; i++) {
					if (map.containsKey(i + 1)) {
						sb.append("\"").append(cs[i]).append("\"").append(",");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				sb.append(",");

				count++;

				if (count % 2000 == 0 && sb.length() > 1) {

					String sql = "INSERT INTO " + name + " (" + titlesb + ")" + "\n" + "VALUES" + "\n"
							+ sb.deleteCharAt(sb.length() - 1) + ";";

//					System.out.println(sql);

					try (Statement stmt = conn.createStatement()) {
						stmt.execute(sql);
					} catch (SQLException e) {
						System.out.println(e.getMessage());
					}

					sb = new StringBuilder();

					System.out.println(count);

//					break;
				}
			}
		}
		reader.close();

		if (sb.length() > 1) {
			String sql = "INSERT INTO " + name + " (" + titlesb + ")" + "\n" + "VALUES" + "\n"
					+ sb.deleteCharAt(sb.length() - 1) + ";";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@SuppressWarnings("unused")
	private static void insert(String in, String usedTitles, String name, String cog, String nog)
			throws NumberFormatException, IOException {

		HashMap<String, String> cogMap = new HashMap<String, String>();
		BufferedReader cogReader = new BufferedReader(new FileReader(cog));
		String coglineString = null;
		while ((coglineString = cogReader.readLine()) != null) {
			String[] cStrings = coglineString.split("\t");
			String[] proStrings = cStrings[2].split(";");
			for (int i = 0; i < proStrings.length; i++) {
				cogMap.put(proStrings[i], cStrings[0]);
			}
		}
		cogReader.close();

		HashMap<String, String> nogMap = new HashMap<String, String>();
		BufferedReader nogReader = new BufferedReader(new FileReader(nog));
		String noglineString = null;
		while ((noglineString = nogReader.readLine()) != null) {
			String[] cStrings = noglineString.split("\t");
			String[] proStrings = cStrings[2].split(";");
			for (int i = 0; i < proStrings.length; i++) {
				nogMap.put(proStrings[i], cStrings[0]);
			}
		}
		nogReader.close();

		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/func.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return;
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

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;

		StringBuilder titlesb = new StringBuilder("query_name");

		while ((line = reader.readLine()) != null) {
			String[] title = line.split("\t");
			if (title.length == 22) {

				for (int i = 1; i < title.length; i++) {
					if (map.containsKey(i + 1)) {
						titlesb.append(",").append(map.get(i + 1));
					}
				}

				titlesb.append(",").append("COG");
				titlesb.append(",").append("NOG");

				System.out.println(titlesb + "\t" + title.length);

				break;
			}
		}

		int count = 0;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == 22) {

				sb.append("(");
				for (int i = 0; i < cs.length; i++) {
					if (map.containsKey(i + 1)) {
						sb.append("\"").append(cs[i]).append("\"").append(",");
					}
				}

				if (cogMap.containsKey(cs[0])) {
					sb.append("\"").append(cogMap.get(cs[0])).append("\"").append(",");
				} else {
					sb.append("\"").append("").append("\"").append(",");
				}

				if (nogMap.containsKey(cs[0])) {
					sb.append("\"").append(nogMap.get(cs[0])).append("\"");
				} else {
					sb.append("\"").append("").append("\"");
				}

				sb.append(")");
				sb.append(",");

				count++;

				if (count % 200000 == 0 && sb.length() > 1) {

					String sql = "INSERT INTO " + name + " (" + titlesb + ")" + "\n" + "VALUES" + "\n"
							+ sb.deleteCharAt(sb.length() - 1) + ";";

//					System.out.println(sql);

					try (Statement stmt = conn.createStatement()) {
						stmt.execute(sql);
					} catch (SQLException e) {
						System.out.println(e.getMessage());
					}

					sb = new StringBuilder();

					System.out.println(count);

//					break;
				}
			}
		}
		reader.close();

		if (sb.length() > 1) {
			String sql = "INSERT INTO " + name + " (" + titlesb + ")" + "\n" + "VALUES" + "\n"
					+ sb.deleteCharAt(sb.length() - 1) + ";";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@SuppressWarnings("unused")
	private static void select(String table, String in) {

		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/func.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return;
		}

		ArrayList<ResultSet> list = new ArrayList<ResultSet>();
		ArrayList<ResultSet> findList = new ArrayList<ResultSet>();

		try {
			Statement stmt = conn.createStatement();

			BufferedReader reader = new BufferedReader(new FileReader(in));
			int count = 0;
			String line = reader.readLine();

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
//				String pro = cs[0].substring(0, cs[0].indexOf("|"));
//				String pro = cs[0];
				for (String pro : cs[0].split(";")) {
					String sql = "select * from " + table + " where query_name=" + "\"" + pro + "\"";

					ResultSet rs = stmt.executeQuery(sql);

					list.add(rs);

					count++;

					if (count % 10000 == 0) {
						System.out.println(new Date() + "\t" + count);
					}

					if (rs.next()) {
						String ss = rs.getString("Gene_Ontology");
						if (ss != null) {
							findList.add(rs);
						}
					}
				}

			}
			reader.close();

			stmt.close();
			conn.close();
		} catch (SQLException | IOException se) {
			se.printStackTrace();
		}

		System.out.println(list.size());
		System.out.println(findList.size());
		System.out.println("Goodbye!");
	}

	@SuppressWarnings("unused")
	private static void deleteTable(String name) {

		String url = "jdbc:sqlite:D:/MetaLab/Project/Resources/function2/func.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return;
		}

		String sql = "CREATE TABLE IF NOT EXISTS " + name + " (\n query_name VARCHAR(100),\n";

		sql += "PRIMARY KEY (query_name));";

		System.out.println(sql);

		try (Statement stmt = conn.createStatement()) {
			// create a new table
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	private static void selectOne(String name) {
		String url = "jdbc:sqlite:D:/Exported/Resources/function/func.db";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			return;
		}

		String sql = "select * from " + "human" + " where query_name=" + "\"" + name + "\"";

		System.out.println(sql);

		try (Statement stmt = conn.createStatement()) {
			// create a new table
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println(rs.next());
			String ss = rs.getString("");
			System.out.println(ss == null);

		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub

//		SqliteManager.create("func.db");
		/*
		 * SqliteManager.createNewTable(
		 * "Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\chicken.emapper.annotations",
		 * "Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\columnNames.txt",
		 * "chicken");
		 * 
		 * SqliteManager.insert(
		 * "Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\chicken.emapper.annotations",
		 * "Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\columnNames.txt",
		 * "chicken", "D:\\MetaLab\\Project\\Resources\\function\\COG_chicken.gc",
		 * "D:\\MetaLab\\Project\\Resources\\function\\NOG_chicken.gc");
		 */
//		SqliteManager.select("mouse", "D:\\Data\\MetaLab_test\\MetaLab\\maxquant_search\\combined\\txt"
//				+ "\\proteinGroups.txt");

//		SqliteTest.select("human", "Z:\\Kai\\Raw_files\\open_search_project\\used_in_paper\\Dataset8_human_micro_PXD8870\\open_result"
//				+ "\\filter3_psms_FlashLFQ_QuantifiedProteins.tsv");

		SqliteManager.selectOne("1069533.Sinf_0283");
	}

}
