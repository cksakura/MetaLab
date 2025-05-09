package bmi.med.uOttawa.metalab.core.function.v2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.function.FunctionFinder;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;

public class FunctionFinderV2 extends FunctionFinder {

	private static final String fullName = "Kyoto Encyclopedia of Genes and Genomes";
	private static final String abbreviation = "KEGG";

	private static Logger LOGGER = LogManager.getLogger(FunctionFinderV2.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public FunctionFinderV2(String db, String fullName, String abbreviation, String proNameDb) {
		super(db, fullName, abbreviation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void match(MetaProteinAnno1[] proteins) {
		// TODO Auto-generated method stub

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (MetaProteinAnno1 pro : proteins) {
			map.put(pro.getPro().getName(), new ArrayList<String>());
		}
		if (isUsable()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(db))) {
				String line = null;
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
				System.err.println(format.format(new Date()) + "\t" + fullName + ": error in reading " + abbreviation
						+ " information");
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
}
