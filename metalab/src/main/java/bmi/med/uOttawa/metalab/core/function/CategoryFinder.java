/**
 * 
 */
package bmi.med.uOttawa.metalab.core.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.par.MetaSources;
import bmi.med.uOttawa.metalab.task.v1.par.AbstractParameter;

/**
 * @author Kai Cheng
 *
 */
public class CategoryFinder {

	private static final String Category_COG = "Resources//function//Category_COG.gc";
	private static final String Category_NOG = "Resources//function//Category_NOG.gc";

	private HashMap<String, String> COGMap;
	private HashMap<String, String> NOGMap;
	
	private static Logger LOGGER = LogManager.getLogger(CategoryFinder.class);
	
	public CategoryFinder() {
		initial();
	}
	
	public CategoryFinder(AbstractParameter par) {
		initial(par);
	}

	public CategoryFinder(MetaSources advPar) {
		initial(advPar);
	}

	private void initial(AbstractParameter par) {
		this.COGMap = new HashMap<String, String>();
		BufferedReader cogReader = null;
		try {
			cogReader = new BufferedReader(new FileReader(par.getCogCategory()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_COG, e);
		}
		String line = null;
		try {
			while ((line = cogReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] cogs = cs[2].split(";");
				for (String cog : cogs) {
					this.COGMap.put(cog, cs[1]);
				}
			}
			cogReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_COG, e);
		}

		this.NOGMap = new HashMap<String, String>();
		BufferedReader nogReader = null;
		try {
			nogReader = new BufferedReader(new FileReader(par.getNogCategory()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
		}
		try {
			while ((line = nogReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] cogs = cs[2].split(";");
				for (String cog : cogs) {
					this.NOGMap.put(cog, cs[1]);
				}
			}
			nogReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
		}
	}

	private void initial() {
		this.COGMap = new HashMap<String, String>();
		BufferedReader cogReader = null;
		try {
			cogReader = new BufferedReader(new FileReader(Category_COG));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_COG, e);
		}
		String line = null;
		try {
			while ((line = cogReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] cogs = cs[2].split(";");
				for (String cog : cogs) {
					this.COGMap.put(cog, cs[1]);
				}
			}
			cogReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_COG, e);
		}

		this.NOGMap = new HashMap<String, String>();
		BufferedReader nogReader = null;
		try {
			nogReader = new BufferedReader(new FileReader(Category_NOG));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
		}
		try {
			while ((line = nogReader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] cogs = cs[2].split(";");
				for (String cog : cogs) {
					this.NOGMap.put(cog, cs[1]);
				}
			}
			nogReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
		}
	}

	private void initial(MetaSources par) {

		String funcDir = par.getFunction();

		File cogCategory = new File(funcDir, "Category_COG.gc");
		this.COGMap = new HashMap<String, String>();

		if (cogCategory.exists()) {
			BufferedReader cogReader = null;
			try {
				cogReader = new BufferedReader(new FileReader(cogCategory));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading category COG database in " + Category_COG, e);
			}
			String line = null;
			try {
				while ((line = cogReader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] cogs = cs[2].split(";");
					for (String cog : cogs) {
						this.COGMap.put(cog, cs[1]);
					}
				}
				cogReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading category COG database in " + Category_COG, e);
			}
		}

		this.NOGMap = new HashMap<String, String>();
		File nogCategory = new File(funcDir, "Category_NOG.gc");
		if (nogCategory.exists()) {
			BufferedReader nogReader = null;
			try {
				nogReader = new BufferedReader(new FileReader(nogCategory));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
			}
			String line = null;
			try {
				while ((line = nogReader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] cogs = cs[2].split(";");
					for (String cog : cogs) {
						this.NOGMap.put(cog, cs[1]);
					}
				}
				nogReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in reading category COG database in " + Category_NOG, e);
			}
		}
	}
	
	public HashMap<String, String> getCOGMap() {
		return COGMap;
	}

	public HashMap<String, String> getNOGMap() {
		return NOGMap;
	}

	public HashMap<String, String> getCOGMap(String[] cogs) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String cog : cogs) {
			if (COGMap.containsKey(cog)) {
				map.put(cog, COGMap.get(cog));
			}
		}
		return map;
	}

	public HashMap<String, String> getNOGMap(String[] nogs) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String nog : nogs) {
			if (NOGMap.containsKey(nog)) {
				map.put(nog, NOGMap.get(nog));
			}
		}
		return map;
	}
}
