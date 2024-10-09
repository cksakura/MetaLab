package bmi.med.uOttawa.metalab.core.function.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.alibaba.innodb.java.reader.TableReader;
import com.alibaba.innodb.java.reader.TableReaderImpl;
import com.alibaba.innodb.java.reader.page.index.GenericRecord;
import com.google.common.collect.ImmutableList;

public class SqlTest {

	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:33062/func_anno";

	static final String USER = "root";
	static final String PASS = "CK@mysql2016";

	private static void toCsv(String in, String out, String usedTitles) throws IOException {

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		BufferedReader titlereader = new BufferedReader(new FileReader(usedTitles));
		String uline = null;
		while ((uline = titlereader.readLine()) != null) {
			int id = uline.indexOf(".");
			int number = Integer.parseInt(uline.substring(0, id));

			map.put(number - 1, uline.substring(id + 1).trim());
		}
		titlereader.close();

		PrintWriter writer = new PrintWriter(out);
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		boolean begin = false;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == 22) {
				StringBuilder sb = new StringBuilder();
				if (begin) {
					for (int i = 0; i < cs.length; i++) {
						if (map.containsKey(i)) {
							sb.append("\"").append(cs[i]).append("\"").append(",");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
				} else {
					for (int i = 0; i < cs.length; i++) {
						if (map.containsKey(i)) {
							sb.append("\"").append(map.get(i)).append("\"").append(",");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					begin = true;
				}

				writer.print(sb+"\n");
			}
		}
		reader.close();
		writer.close();
	}
	
	private static void deleteTable(String table) {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);

			System.out.println("连接数据库...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println(" 实例化Statement对象...");
			stmt = conn.createStatement();

			String sql = "use func_anno";
			stmt.execute(sql);

			sql = "drop table " + table;
			stmt.execute(sql);

			stmt.close();
			conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		System.out.println("Goodbye!");
	}

	private static void createTable(String in, String usedTitles) throws IOException {

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

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);

			System.out.println("连接数据库...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println(" 实例化Statement对象...");
			stmt = conn.createStatement();

			BufferedReader reader = new BufferedReader(new FileReader(in));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == 22) {

					String sql = "CREATE TABLE IF NOT EXISTS mouse (\n" + " query_name VARCHAR(100),\n";

					for (int i = 1; i < cs.length; i++) {

						if (map.containsKey(i + 1)) {

							sql += new String(" " + map.get(i + 1) + " text NOT NULL,\n");
						}
					}

					sql += "PRIMARY KEY (query_name));";

					System.out.println(sql);

					stmt.execute(sql);

					break;
				}

			}
			reader.close();

			stmt.close();
			conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		System.out.println("Goodbye!");

	}

	private static void sqlTest() {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);

			System.out.println("连接数据库...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println(" 实例化Statement对象...");
			stmt = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS GOs (\n" + "	name VARCHAR(100) PRIMARY KEY,\n"
					+ "	gos text NOT NULL\n" + ");";

			System.out.println(sql);
//			String sql = "DROP TABLE GOs";

//			stmt.execute(sql);

			boolean begin = false;
			int id = -1;

			BufferedReader reader = new BufferedReader(
					new FileReader("Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation"
							+ "\\dogs_eggnog_mapper.emapper.annotations"));
			int count = 0;
			String line = null;
			L: while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs[0].equals("#query_name")) {
					begin = true;

					for (int i = 1; i < cs.length; i++) {
						if (cs[i].equals("GOs")) {
							id = i;
							continue L;
						}
					}
				}

				if (begin && id < cs.length) {
					if (cs[id].length() == 0) {
						cs[id] = " ";
					}
					sql = "INSERT INTO gos VALUES " + "(" + count + ",'" + cs[0] + "','" + cs[id] + "')";

//					System.out.println(sql);

//					stmt.executeUpdate(sql);
					count++;

					if (count % 1000 == 0) {
						System.out.println(new Date() + "\t" + count);
					}
				}
			}
			reader.close();

			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		System.out.println("Goodbye!");
	}

	private static void search(String table, String in) {
		ArrayList<ResultSet> list = new ArrayList<ResultSet>();
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);

			System.out.println("连接数据库...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println(" 实例化Statement对象...");
			stmt = conn.createStatement();

			String sql = "use func_anno";

			System.out.println(sql);
//			String sql = "DROP TABLE GOs";

			stmt.execute(sql);

			BufferedReader reader = new BufferedReader(new FileReader(in));
			int count = 0;
			String line = reader.readLine();

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				sql = "select * from " + table + " where query_name=" + "\"" + cs[0] + "\"";

				ResultSet rs = stmt.executeQuery(sql);

				list.add(rs);

				count++;

				if (count % 10000 == 0) {
					System.out.println(new Date() + "\t" + count);
				}
			}
			reader.close();

			stmt.close();
			conn.close();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		System.out.println(list.size());
		System.out.println("Goodbye!");
	}
	
	private static void readTest(String in) {

		String sql = "CREATE TABLE `mouse` (\r\n" + "  `query_name` varchar(100) NOT NULL,\r\n"
				+ "  `Gene_Ontology` text NOT NULL,\r\n" + "  `EC` text NOT NULL,\r\n"
				+ "  `KEGG_ko` text NOT NULL,\r\n" + "  `KEGG_Pathway` text NOT NULL,\r\n"
				+ "  `KEGG_Module` text NOT NULL,\r\n" + "  `KEGG_Reaction` text NOT NULL,\r\n"
				+ "  `KEGG_rclass` text NOT NULL,\r\n" + "  `BRITE` text NOT NULL,\r\n"
				+ "  `KEGG_TC` text NOT NULL,\r\n" + "  `CAZy` text NOT NULL,\r\n"
				+ "  `BiGG_Reaction` text NOT NULL,\r\n" + "  `COG_Functional_Category` text NOT NULL,\r\n"
				+ "  `eggNOG_description` text NOT NULL,\r\n" + "  PRIMARY KEY (`query_name`)\r\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

		try (TableReader reader = new TableReaderImpl(in, sql)) {
			reader.open();
			GenericRecord record = reader.queryByPrimaryKey(ImmutableList.of(0));
			Object[] values = record.getValues();
			System.out.println(Arrays.asList(values));
		}
	}
	
	private static void count(String in) throws IOException {
		int count = 0;
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			String nameString = files[i].getName();
			if (nameString.endsWith("emapper.annotations")) {
				BufferedReader reader = new BufferedReader(new FileReader(files[i]));
				String lineString = null;
				while((lineString=reader.readLine())!=null) {
					count++;
				}
				reader.close();
			}
		}
		System.out.println("total\t"+count);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
/*
		SqlTest.toCsv(
				"Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\mouse_eggnog_mapper.emapper.annotations",
				"Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\mouse.csv",
				"Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\used.txt");
*/
//		SqlTest.createTable("Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\mouse_eggnog_mapper.emapper.annotations",
//				"Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation\\used.txt");
		
//		SqlTest.deleteTable("mouse");
		
//		SqlTest.search("mouse", "Z:\\Kai\\Raw_files\\open_search_project\\used_in_paper\\Dataset7_mouse_micro\\result"
//				+ "\\filter3_psms_FlashLFQ_QuantifiedProteins.tsv");
		
//		SqlTest.readTest("C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Data\\func_anno"
//				+ "\\mouse.ibd");
		
		SqlTest.count("Z:\\Microbiome\\eggNOG5.0_database_organize\\eggnogmapper_annotation");
	}

}
