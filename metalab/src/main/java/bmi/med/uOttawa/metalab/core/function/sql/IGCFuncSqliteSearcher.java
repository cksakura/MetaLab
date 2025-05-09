package bmi.med.uOttawa.metalab.core.function.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.MGYG.db.ProDbConfig;
import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLWriter2;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class IGCFuncSqliteSearcher {

	protected String dbPath;
	private String[] dbNames = new String[] { "mouse", "human", "dog", "rat", "pig", "chicken" };

	protected String defDbPath;
	protected String cogDbName = "cog_20_def_tab";
	protected String nogDbName = "nog_e5_og_annotations";
	protected String ecDbName = "enzyme";
	protected String goDbName = "go_obo";
	protected String keggDbName = "kegg_pathway";

	protected HashMap<String, String[]> usedCogMap;
	protected HashMap<String, String[]> usedNogMap;
	protected HashMap<String, String> usedKeggMap;
	protected HashMap<String, GoObo> usedGoMap;
	protected HashMap<String, EnzymeCommission> usedEcMap;

	public IGCFuncSqliteSearcher(String dbpath, String defDbPath) throws SQLException, NumberFormatException, IOException {

		this.dbPath = dbpath;
		this.defDbPath = defDbPath;

		this.usedCogMap = new HashMap<String, String[]>();
		this.usedNogMap = new HashMap<String, String[]>();
		this.usedKeggMap = new HashMap<String, String>();
		this.usedGoMap = new HashMap<String, GoObo>();
		this.usedEcMap = new HashMap<String, EnzymeCommission>();
	}

	public MetaProteinAnnoEggNog[] match(MetaProtein[] metapros) throws SQLException, IOException {

		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		Statement dbStmt = dbConn.createStatement();

		String dbname = null;
		L: for (int i = 0; i < metapros.length; i++) {
			for (int j = 0; j < dbNames.length; j++) {
				String sql = "select * from " + dbNames[j] + " where query_name=" + "\"" + metapros[i].getName() + "\"";
				ResultSet rs = dbStmt.executeQuery(sql);
				if (rs.next()) {
					dbname = dbNames[j];
					break L;
				}
			}
		}

		ArrayList<MetaProteinAnnoEggNog> list = new ArrayList<MetaProteinAnnoEggNog>();

		if (dbname != null) {

			HashSet<String> proNameSet = new HashSet<String>();
			for (int i = 0; i < metapros.length; i++) {
				proNameSet.add(metapros[i].getName());
			}

			HashSet<String> cogSet = new HashSet<String>();
			HashSet<String> nogSet = new HashSet<String>();
			HashSet<String> keggSet = new HashSet<String>();
			HashSet<String> goSet = new HashSet<String>();
			HashSet<String> ecSet = new HashSet<String>();

			HashMap<String, String[]> proMap = new HashMap<String, String[]>();
			StringBuilder prosb = new StringBuilder();
			prosb.append("(");
			for (String pro : proNameSet) {
				prosb.append("\"").append(pro).append("\"").append(",");
			}
			prosb.deleteCharAt(prosb.length() - 1);
			prosb.append(")");

			StringBuilder sb = new StringBuilder();
			sb.append("select * from ").append(dbname).append(" where query_name IN ").append(prosb);

			ResultSet rs = dbStmt.executeQuery(sb.toString());
			while (rs.next()) {
				String[] content = new String[ProDbConfig.PRO2FUNTITLE_STRINGS.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = rs.getString(ProDbConfig.PRO2FUNTITLE_STRINGS[i]);
				}
				proMap.put(content[0], content);

				String[] gos = content[5].split(",");
				for (int i = 0; i < gos.length; i++) {
					goSet.add(gos[i]);
				}

				String[] ecs = content[6].split(",");
				for (int i = 0; i < ecs.length; i++) {
					ecSet.add(ecs[i]);
				}

				String[] keggs = content[8].split(",");
				for (int i = 0; i < keggs.length; i++) {
					keggSet.add(keggs[i]);
				}

				String[] cogs = content[16].split(",");
				for (int i = 0; i < cogs.length; i++) {
					cogSet.add(cogs[i]);
				}

				String[] nogs = content[17].split(",");
				for (int i = 0; i < nogs.length; i++) {
					nogSet.add(nogs[i] + "@" + content[2]);
				}
			}

			for (int i = 0; i < metapros.length; i++) {
				String proName = metapros[i].getName();
				if (proMap.containsKey(proName)) {
					String[] content = proMap.get(proName);
					MetaProteinAnnoEggNog mpa = new MetaProteinAnnoEggNog(metapros[i], content[2], content[3], content[4],
							content[5], content[6], content[7], content[8], content[9], content[10], content[11],
							content[12], content[13], content[14], content[15], content[16], content[17]);
					list.add(mpa);
				}
			}

			getReference(cogSet, nogSet, goSet, ecSet, keggSet);
		}

		dbStmt.close();
		dbConn.close();

		MetaProteinAnnoEggNog[] mpas = list.toArray(new MetaProteinAnnoEggNog[list.size()]);

		return mpas;
	}
	
	protected void getReference(HashSet<String> cogSet, HashSet<String> nogSet, HashSet<String> goSet,
			HashSet<String> ecSet, HashSet<String> keggSet) throws SQLException {

		Connection defDbConn = DriverManager.getConnection("jdbc:sqlite:" + this.defDbPath);
		Statement defDbStmt = defDbConn.createStatement();

		if (cogSet.size() > 0) {
			StringBuilder cogsb = new StringBuilder();
			cogsb.append("(");
			for (String cog : cogSet) {
				cogsb.append("\"").append(cog).append("\"").append(",");
			}
			cogsb.deleteCharAt(cogsb.length() - 1);
			cogsb.append(")");

			StringBuilder sqlsb = new StringBuilder();
			sqlsb.append("select * from ").append(cogDbName).append(" where id IN ").append(cogsb);

			ResultSet cogRs = defDbStmt.executeQuery(sqlsb.toString());
			while (cogRs.next()) {
				String id = cogRs.getString("id");
				String category = cogRs.getString("category");
				String des = cogRs.getString("description");

				usedCogMap.put(id, new String[] { category, des });
			}
		}

		if (nogSet.size() > 0) {
			StringBuilder nogsb = new StringBuilder();
			nogsb.append("(");
			for (String nog : nogSet) {
				nogsb.append("\"").append(nog).append("\"").append(",");
			}
			nogsb.deleteCharAt(nogsb.length() - 1);
			nogsb.append(")");

			StringBuilder sqlsb = new StringBuilder();
			sqlsb.append("select * from ").append(nogDbName).append(" where id IN ").append(nogsb);

			ResultSet nogRs = defDbStmt.executeQuery(sqlsb.toString());
			while (nogRs.next()) {
				String id = nogRs.getString("id");
				String category = nogRs.getString("category");
				String des = nogRs.getString("description");

				usedNogMap.put(id, new String[] { category, des });
			}
		}

		if (ecSet.size() > 0) {
			StringBuilder ecsb = new StringBuilder();
			ecsb.append("(");
			for (String ec : ecSet) {
				ecsb.append("\"").append(ec).append("\"").append(",");
			}
			ecsb.deleteCharAt(ecsb.length() - 1);
			ecsb.append(")");

			StringBuilder sqlsb = new StringBuilder();
			sqlsb.append("select * from ").append(ecDbName).append(" where id IN ").append(ecsb);

			ResultSet ecRs = defDbStmt.executeQuery(sqlsb.toString());
			while (ecRs.next()) {
				String id = ecRs.getString("id");
				String de = ecRs.getString("de");
				String ca = ecRs.getString("ca");
				String[] ans = ecRs.getString("an").split(";");

				ArrayList<String> anList = new ArrayList<String>();
				for (String an : ans) {
					anList.add(an);
				}
				EnzymeCommission ec = new EnzymeCommission(id, de, ca, anList);

				usedEcMap.put(id, ec);
			}
		}

		if (goSet.size() > 0) {
			StringBuilder gosb = new StringBuilder();
			gosb.append("(");
			for (String go : goSet) {
				gosb.append("\"").append(go).append("\"").append(",");
			}
			gosb.deleteCharAt(gosb.length() - 1);
			gosb.append(")");

			StringBuilder sqlsb = new StringBuilder();
			sqlsb.append("select * from ").append(goDbName).append(" where id IN ").append(gosb);

			ResultSet goRs = defDbStmt.executeQuery(sqlsb.toString());
			while (goRs.next()) {
				String id = goRs.getString("id");
				String name = goRs.getString("name");
				String namespace = goRs.getString("namespace");

				GoObo goObo = new GoObo(id, name, namespace);

				usedGoMap.put(id, goObo);
			}
		}

		if (keggSet.size() > 0) {
			StringBuilder keggsb = new StringBuilder();
			keggsb.append("(");
			for (String kegg : keggSet) {
				keggsb.append("\"").append(kegg).append("\"").append(",");
			}
			keggsb.deleteCharAt(keggsb.length() - 1);
			keggsb.append(")");

			StringBuilder sqlsb = new StringBuilder();
			sqlsb.append("select * from ").append(keggDbName).append(" where name IN ").append(keggsb);

			ResultSet keggRs = defDbStmt.executeQuery(sqlsb.toString());
			while (keggRs.next()) {
				String name = keggRs.getString("name");
				String des = keggRs.getString("description");

				usedKeggMap.put(name, des);
			}
		}

		defDbStmt.close();
		defDbConn.close();
	}
	
	public HashMap<String, String[]> getUsedCogMap() {
		return usedCogMap;
	}

	public HashMap<String, String[]> getUsedNogMap() {
		return usedNogMap;
	}

	public HashMap<String, String> getUsedKeggMap() {
		return usedKeggMap;
	}

	public HashMap<String, GoObo> getUsedGoMap() {
		return usedGoMap;
	}

	public HashMap<String, EnzymeCommission> getUsedEcMap() {
		return usedEcMap;
	}

	@SuppressWarnings("unused")
	private void test(String in)
			throws IOException, NumberFormatException, SQLException, DocumentException {
		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		String[] title = line.split("\t");
		String[] fileNames = new String[title.length - 10];
		System.arraycopy(title, 6, fileNames, 0, fileNames.length);

		int count = 1;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (!cs[1].contains("ECOLI")) {
				double[] intensity = new double[cs.length - 10];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[i + 6]);
				}
				MetaProtein mp = new MetaProtein(count++, 1, cs[1], Integer.parseInt(cs[2]), Integer.parseInt(cs[3]),
						Double.parseDouble(cs[4]), intensity);
				list.add(mp);
			}
		}
		reader.close();

		MetaProtein[] mps = list.toArray(new MetaProtein[list.size()]);
		MetaProteinAnnoEggNog[] metapros = this.match(mps);

		File folder = (new File(in)).getParentFile();

		MetaProteinXMLWriter2 writer = new MetaProteinXMLWriter2(new File(folder, "function.xml"), MetaConstants.pFind,
				MetaConstants.labelFree, fileNames);

		writer.addProteins(metapros, this.getUsedCogMap(), this.getUsedNogMap(), this.getUsedKeggMap(),
				this.getUsedGoMap(), this.getUsedEcMap());

		writer.close();

		MetaProteinXMLReader2 funReader = new MetaProteinXMLReader2(new File(folder, "function.xml"));
		funReader.exportTsv(new File(folder, "function.tsv"));
	}
}
