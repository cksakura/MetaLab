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
public class GOCCFinder extends FunctionFinder {

	private static final String fullName = "Gene Ontology Cellular Component";
	private static final String abbreviation = "GOCC";

	private static final String QuickGO = "https://www.ebi.ac.uk/QuickGO/term/";
	private static final String AmiGO = "http://amigo.geneontology.org/amigo/term/";

	private static Logger LOGGER = LogManager.getLogger(GOCCFinder.class);

	public GOCCFinder(String dbPath) {
		super(dbPath, fullName, abbreviation);
		// TODO Auto-generated constructor stub
	}

	public GOCCFinder(MetaParameterV1 par) {
		super(par.getGOCC(), fullName, abbreviation);
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
					pro.setGOCC(funs);
				}
			}

		} else {
			LOGGER.error("Functional annotation database was not found in" + db);
			return;
		}
	}

	public static String getLinks(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(QuickGO).append(name);
		sb.append(" ;");
		sb.append(AmiGO).append(name);

		return sb.toString();
	}

	public static String getQuickGo(String name) {
		return QuickGO + name;
	}

	public static String getAmiGO(String name) {
		return AmiGO + name;
	}
}
