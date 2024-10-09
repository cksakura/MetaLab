package bmi.med.uOttawa.metalab.core.function.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.function.FunctionFinder;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProteinAnno1;

public class FunctionFinderV2 extends FunctionFinder {

	private static final String fullName = "Kyoto Encyclopedia of Genes and Genomes";
	private static final String abbreviation = "KEGG";

	private static Logger LOGGER = LogManager.getLogger(FunctionFinderV2.class);

	private String proNameDb;

	public FunctionFinderV2(String db, String fullName, String abbreviation, String proNameDb) {
		super(db, fullName, abbreviation);
		// TODO Auto-generated constructor stub
		this.proNameDb = proNameDb;
	}

	private void getProteinMap(MetaProtein[] proteins) throws IOException {
		HashMap<String, MetaProtein> map = new HashMap<String, MetaProtein>();
		for (int i = 0; i < proteins.length; i++) {
			map.put(proteins[i].getName(), proteins[i]);
		}

		File[] files = (new File(proNameDb)).listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.equals("")) {
				BufferedReader reader = new BufferedReader(new FileReader(files[i]));
				String line = null;
				while ((line = reader.readLine()) != null) {

				}
			}
		}
	}

	@Override
	public void match(MetaProteinAnno1[] proteins) {
		// TODO Auto-generated method stub

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

}
