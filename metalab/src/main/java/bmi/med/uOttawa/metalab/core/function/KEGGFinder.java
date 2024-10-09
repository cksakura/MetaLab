/**
 * 
 */
package bmi.med.uOttawa.metalab.core.function;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
public class KEGGFinder extends FunctionFinder {

	private static final String fullName = "Kyoto Encyclopedia of Genes and Genomes";
	private static final String abbreviation = "KEGG";
	
	private static final String prefix = "map";
	private static final String link = "http://www.genome.jp/dbget-bin/www_bget?";

	private static Logger LOGGER = LogManager.getLogger(KEGGFinder.class);
	
	public KEGGFinder(String dbPath) {
		super(dbPath, fullName, abbreviation);
	}
	
	public KEGGFinder(MetaParameterV1 par) {
		super(par.getKEGG(), fullName, abbreviation);
		// TODO Auto-generated constructor stub
	}

	public void match(MetaProteinAnno1[] proteins) {

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (MetaProteinAnno1 pro : proteins) {
			map.put(pro.getPro().getName(), new ArrayList<String>());
		}
		if (isUsable()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(db));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading " + abbreviation + " database in " + db, e);
			}

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] dbpros = cs[2].split(";");
					for (String dbp : dbpros) {
						if (map.containsKey(dbp)) {
							map.get(dbp).add(cs[0]);
							this.functionMap.put(cs[0], cs[1]);
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
					pro.setKEGG(funs[0]);
				}
			}

		} else {
			LOGGER.error("Functional annotation database was not found in" + db);
			return;
		}
	}
	
	public static String getLinks(String name) {
		return link + prefix + name;
	}

}
