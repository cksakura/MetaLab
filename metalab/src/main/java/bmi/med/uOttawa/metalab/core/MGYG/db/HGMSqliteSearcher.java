package bmi.med.uOttawa.metalab.core.MGYG.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.function.sql.IGCFuncSqliteSearcher;
import bmi.med.uOttawa.metalab.task.hgm.MetaProteinAnnoHgm;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;

public class HGMSqliteSearcher extends IGCFuncSqliteSearcher {

	/**
	 * https://gtdb.ecogenomic.org/
	 */
	private String genome;

	public HGMSqliteSearcher(String dbpath, String defDbPath) throws NumberFormatException, SQLException, IOException {
		super(dbpath, defDbPath);
	}

	public HGMSqliteSearcher(String dbpath, String defDbPath, String genome)
			throws NumberFormatException, SQLException, IOException {
		super(dbpath, defDbPath);
		this.genome = genome;
	}

	public HashMap<String, String[]> matchGenome(String[] genomes) throws SQLException {

		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		Statement stmt = dbConn.createStatement();

		HashMap<String, String[]> genomeTaxaMap = new HashMap<String, String[]>();

		StringBuilder genomeSb = new StringBuilder();
		genomeSb.append("(");
		for (int i = 0; i < genomes.length; i++) {
			genomeSb.append("\"").append(genomes[i]).append("\"").append(",");
		}
		genomeSb.deleteCharAt(genomeSb.length() - 1);
		if (genomeSb.length() == 0) {
			return genomeTaxaMap;
		}

		genomeSb.append(")");

		StringBuilder sb = new StringBuilder();
		sb.append("select * from genome2taxa where Genome IN ").append(genomeSb);

		ResultSet rs = stmt.executeQuery(sb.toString());

		while (rs.next()) {
			String[] content = new String[ProDbConfig.genome2TaxaGTDBNCBITitle.length];
			for (int i = 0; i < content.length; i++) {
				content[i] = rs.getString(ProDbConfig.genome2TaxaGTDBNCBITitle[i]);
			}
			genomeTaxaMap.put(content[0], content);
		}

		return genomeTaxaMap;
	}

	protected HashMap<String, String[]> matchPro(String tableName, String[] pros) throws SQLException {

		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		Statement stmt = dbConn.createStatement();

		HashMap<String, String[]> proMap = new HashMap<String, String[]>();
		StringBuilder prosb = new StringBuilder();
		prosb.append("(");
		for (String pro : pros) {
			prosb.append("\"").append(pro).append("\"").append(",");
		}
		prosb.deleteCharAt(prosb.length() - 1);
		prosb.append(")");

		StringBuilder sb = new StringBuilder();
		sb.append("select * from pro2fun_").append(tableName).append(" where query_name IN ").append(prosb);

		ResultSet rs = stmt.executeQuery(sb.toString());
		while (rs.next()) {
			String[] content = new String[ProDbConfig.PRO2FUNTITLE_STRINGS.length];
			for (int i = 0; i < content.length; i++) {
				content[i] = rs.getString(ProDbConfig.PRO2FUNTITLE_STRINGS[i]);
			}
			proMap.put(content[0], content);
		}

		return proMap;
	}

	public MetaProteinAnnoHgm[] match(MetaProtein[] metapros) throws SQLException {

		if (genome == null) {
			return null;
		}

		String genomeId = null;
		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		Statement dbStmt = dbConn.createStatement();

		ResultSet accRs = dbStmt.executeQuery("select Genome from genome2taxa where Genome=" + "\"" + genome + "\"");
		if (accRs.next()) {
			genomeId = accRs.getString("Genome");
		}

		if (genomeId == null) {
			return null;
		}

		String proDbName = genomeId.replace("MGYG", "pro2fun_");

		ArrayList<MetaProteinAnnoEggNog> list = new ArrayList<MetaProteinAnnoEggNog>();

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
		sb.append("select * from ").append(proDbName).append(" where query_name IN ").append(prosb);

		ResultSet rs = dbStmt.executeQuery(sb.toString());
		while (rs.next()) {
			String[] content = new String[MetaProteinAnnoHgm.funcNames.length];
			for (int i = 0; i < content.length; i++) {
				content[i] = rs.getString(MetaProteinAnnoHgm.funcNames[i]);
			}
			proMap.put(content[0], content);

			String[] gos = content[MetaProteinAnnoHgm.goID].split(",");
			for (int i = 0; i < gos.length; i++) {
				goSet.add(gos[i]);
			}

			String[] ecs = content[MetaProteinAnnoHgm.ecID].split(",");
			for (int i = 0; i < ecs.length; i++) {
				ecSet.add(ecs[i]);
			}

			String[] keggs = content[MetaProteinAnnoHgm.pathwayID].split(",");
			for (int i = 0; i < keggs.length; i++) {
				keggSet.add(keggs[i]);
			}

			cogSet.add(content[MetaProteinAnnoHgm.cogID]);

			String[] nogs = content[MetaProteinAnnoHgm.nogID].split(",");
			for (int i = 0; i < nogs.length; i++) {
				String nog = nogs[i].substring(0, nogs[i].indexOf("|"));
				nogSet.add(nog);
			}
		}

		for (int i = 0; i < metapros.length; i++) {
			String proName = metapros[i].getName();
			if (proMap.containsKey(proName)) {
				String[] content = proMap.get(proName);
				MetaProteinAnnoHgm mpa = new MetaProteinAnnoHgm(metapros[i], content);
				list.add(mpa);
			}
		}

		getReference(cogSet, nogSet, goSet, ecSet, keggSet);

		dbStmt.close();
		dbConn.close();

		MetaProteinAnnoHgm[] mpas = list.toArray(new MetaProteinAnnoHgm[list.size()]);

		return mpas;
	}

}
