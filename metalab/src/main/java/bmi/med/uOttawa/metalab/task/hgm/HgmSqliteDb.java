package bmi.med.uOttawa.metalab.task.hgm;

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
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;

/**
 * @deprecated
 * @author Kai Cheng
 *
 */
public class HgmSqliteDb extends IGCFuncSqliteSearcher {

	private HashMap<String, String> genomeNameMap;

	public HgmSqliteDb(String dbpath, String defDbPath, HashMap<String, String> genomeNameMap)
			throws SQLException, NumberFormatException, IOException {
		super(dbpath, defDbPath);
		// TODO Auto-generated constructor stub
		this.genomeNameMap = genomeNameMap;
	}

	public MetaProteinAnnoHgm[] match(MetaProtein[] metapros) throws SQLException {

		HashMap<String, HashMap<String, MetaProtein>> genomeProMap = new HashMap<String, HashMap<String, MetaProtein>>();

		for (int i = 0; i < metapros.length; i++) {
			String name = metapros[i].getName();
			String genome = name.split("_")[1];
			String genomeIdName = genomeNameMap.get(genome);
			if (genomeProMap.containsKey(genomeIdName)) {
				genomeProMap.get(genomeIdName).put(name, metapros[i]);
			} else {
				HashMap<String, MetaProtein> map = new HashMap<String, MetaProtein>();
				map.put(name, metapros[i]);
				genomeProMap.put(genomeIdName, map);
			}
		}

		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		Statement dbStmt = dbConn.createStatement();

		ArrayList<MetaProteinAnnoEggNog> list = new ArrayList<MetaProteinAnnoEggNog>();
		HashSet<String> cogSet = new HashSet<String>();
		HashSet<String> nogSet = new HashSet<String>();
		HashSet<String> keggSet = new HashSet<String>();
		HashSet<String> goSet = new HashSet<String>();
		HashSet<String> ecSet = new HashSet<String>();

		for (String genomeIdName : genomeProMap.keySet()) {
			HashMap<String, MetaProtein> proMap = genomeProMap.get(genomeIdName);
			StringBuilder prosb = new StringBuilder();
			prosb.append("(");
			for (String pro : proMap.keySet()) {
				prosb.append("\"").append(pro).append("\"").append(",");
			}
			prosb.deleteCharAt(prosb.length() - 1);
			prosb.append(")");

			StringBuilder sb = new StringBuilder();
			sb.append("select * from pro2fun_").append(genomeIdName).append(" where query_name IN ").append(prosb);

			ResultSet rs = dbStmt.executeQuery(sb.toString());
			while (rs.next()) {
				String[] content = new String[MetaProteinAnnoHgm.funcNames.length];
				for (int i = 0; i < content.length; i++) {
					content[i] = rs.getString(MetaProteinAnnoHgm.funcNames[i]);
				}

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

				String[] cogs = content[MetaProteinAnnoHgm.cogID].split(",");
				for (int i = 0; i < cogs.length; i++) {
					cogSet.add(cogs[i]);
				}

				String[] nogs = content[MetaProteinAnnoHgm.nogID].split(",");
				for (int i = 0; i < nogs.length; i++) {
					nogSet.add(nogs[i]);
				}

				MetaProtein mp = proMap.get(content[0]);

				MetaProteinAnnoHgm mpa = new MetaProteinAnnoHgm(mp, content);

				list.add(mpa);
			}
		}

		getReference(cogSet, nogSet, goSet, ecSet, keggSet);

		MetaProteinAnnoHgm[] mpas = list.toArray(new MetaProteinAnnoHgm[list.size()]);

		return mpas;
	}
}
