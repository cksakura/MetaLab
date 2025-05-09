/**
 * 
 */
package bmi.med.uOttawa.metalab.core.function;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class COGFinder extends FunctionFinder {

	private static final String fullName = "Clusters of Orthologous Groups";
	private static final String abbreviation = "COG";

	private static final String link = "ftp://ftp.ncbi.nih.gov/pub/COG/COG2014/static/byCOG/";
	private static Logger LOGGER = LogManager.getLogger(COGFinder.class);
	
	public COGFinder(String dbPath) {
		super(dbPath, fullName, abbreviation);
		// TODO Auto-generated constructor stub
	}
	
	public COGFinder(MetaParameterV1 par) {
		super(par.getCog(), fullName, abbreviation);
		// TODO Auto-generated constructor stub
	}

	public void match(MetaProteinAnno1[] proteins) {

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (MetaProteinAnno1 pro : proteins) {
			map.put(pro.getPro().getName(), new ArrayList<String>());
		}

		if (isUsable()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(db))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (cs.length == 3) {
						String[] dbpros = cs[2].split(";");
						for (String dbp : dbpros) {
							if (map.containsKey(dbp)) {
								map.get(dbp).add(cs[0]);
								this.functionMap.put(cs[0], cs[1]);
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading " + abbreviation + " information", e);
			}

			for (MetaProteinAnno1 pro : proteins) {
				String name = pro.getPro().getName();
				if (map.containsKey(name) && map.get(name).size() > 0) {
					String[] funs = map.get(name).toArray(new String[map.get(name).size()]);
					pro.setCOG(funs[0]);
				}
			}

		} else {
			LOGGER.error("Functional annotation database was not found in" + db);
			return;
		}
	}
	
	public static String getLinks(String name) {
		return link + name + ".html";
	}
}
