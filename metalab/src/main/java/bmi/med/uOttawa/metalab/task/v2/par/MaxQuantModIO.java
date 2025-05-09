/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.par;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;

/**
 * @author Kai Cheng
 *
 */
public class MaxQuantModIO {

	private static Logger LOGGER = LogManager.getLogger(MaxQuantModIO.class);

	public static HashMap<String, MaxquantModification> getModMap() {

		HashMap<String, MaxquantModification> map = new HashMap<String, MaxquantModification>();

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/mods.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			for (int i = 0; i < modArray.length(); i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				MaxquantModification mod = new MaxquantModification(name, description, position, composition, sites,
						mass);
				map.put(name, mod);
			}
		}
		return map;
	}

	public static HashMap<String, MaxquantModification> getIsotopicMap() {

		HashMap<String, MaxquantModification> map = new HashMap<String, MaxquantModification>();

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/isotopic.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			for (int i = 0; i < modArray.length(); i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				MaxquantModification mod = new MaxquantModification(name, description, position, composition, sites,
						mass);
				map.put(name, mod);
			}
		}
		return map;
	}

	public static HashMap<String, MaxquantModification> getIsobaricMap() {

		HashMap<String, MaxquantModification> map = new HashMap<String, MaxquantModification>();

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/isobaric.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			for (int i = 0; i < modArray.length(); i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				double tmtMass = obji.getDouble("tmtMass");
				String tmtCompsition = obji.getString("tmtComposition");

				MaxquantModification mod = new MaxquantModification(name, description, position, composition, sites,
						mass, tmtMass, tmtCompsition);

				map.put(name, mod);
			}
		}
		return map;
	}

	public static MaxquantModification[] getMods() {

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/mods.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		MaxquantModification[] mods = null;
		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			mods = new MaxquantModification[modArray.length()];

			for (int i = 0; i < mods.length; i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				mods[i] = new MaxquantModification(name, description, position, composition, sites, mass);
			}
		}

		return mods;
	}

	public static MaxquantModification[] getIsotopicLabels() {

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/isotopic.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		MaxquantModification[] mods = null;
		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			mods = new MaxquantModification[modArray.length()];

			for (int i = 0; i < mods.length; i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				mods[i] = new MaxquantModification(name, description, position, composition, sites, mass);
			}
		}

		return mods;
	}

	public static MaxquantModification[] getIsobaricLabels() {

		InputStream is = null;

		URL url = MaxQuantModIO.class.getResource("/bmi/med/uOttawa/metalab/task/v2/par/isobaric.json");
		try {
			is = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		StringBuilder sb = new StringBuilder();
		String line = null;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		JSONObject obj = null;
		try {
			obj = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LOGGER.error("Error in reading MetaLab modification file.", e);
			return null;
		}

		MaxquantModification[] mods = null;
		JSONArray modArray = obj.getJSONArray("modifications");

		if (modArray != null) {
			mods = new MaxquantModification[modArray.length()];

			for (int i = 0; i < mods.length; i++) {
				JSONObject obji = modArray.getJSONObject(i);
				String name = obji.getString("name");
				String description = obji.getString("description");
				double mass = obji.getDouble("mass");
				String position = obji.getString("position");
				String composition = obji.getString("composition");

				JSONArray siteArray = obji.getJSONArray("sites");
				String[] sites = new String[siteArray.length()];
				for (int j = 0; j < sites.length; j++) {
					JSONObject objj = siteArray.getJSONObject(j);
					sites[j] = objj.getString("site");
				}

				double tmtMass = obji.getDouble("tmtMass");
				String tmtCompsition = obji.getString("tmtComposition");

				mods[i] = new MaxquantModification(name, description, position, composition, sites, mass, tmtMass,
						tmtCompsition);
			}
		}

		return mods;
	}

	public static void convertMaxquant(String in, String out, int type) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);

			JSONWriter jw = new JSONWriter(writer);
			jw.object().key("modifications").array();

			MaxquantModHandler maxhandler = new MaxquantModHandler(in);
			MaxquantModification[][] mmods = maxhandler.getModifications();

			for (int i = 0; i < mmods[type].length; i++) {
				String name = mmods[type][i].getTitle();
				String description = mmods[type][i].getDescription();
				String position = mmods[type][i].getPosition();
				String[] sites = mmods[type][i].getSites();
				String composition = mmods[type][i].getComposition();
				double mass = mmods[type][i].getMonomass();

				jw.object();
				jw.key("name").value(name);
				jw.key("description").value(description);
				jw.key("mass").value(mass);
				jw.key("position").value(position);
				jw.key("composition").value(composition);

				if (type == 2) {
					double tmtMass = mmods[type][i].getTmtMass();
					String tmtComp = mmods[type][i].getTmtComp();
					jw.key("tmtMass").value(tmtMass);
					jw.key("tmtComposition").value(tmtComp);
				}

				jw.key("sites").array();
				for (int j = 0; j < sites.length; j++) {
					jw.object().key("site").value(sites[j]).endObject();
				}
				jw.endArray();
				jw.endObject();
			}

			jw.endArray();
			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting MetaLab parameter to " + out, e);
		}
	}
}
