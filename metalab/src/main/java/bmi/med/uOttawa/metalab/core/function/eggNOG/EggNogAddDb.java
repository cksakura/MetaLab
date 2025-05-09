package bmi.med.uOttawa.metalab.core.function.eggNOG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.function.v2.ECReader;
import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.core.function.v2.GoOboReader;
import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;

public class EggNogAddDb {

	private final String cogName = "cognames2003-2014.tab";
	private HashMap<String, String> cogNameMap;

	private final String nogName = "1_annotations.tsv";
	private HashMap<String, String[]> nogNameMap;

	private final String keggMapNameString = "KeggMap.txt";
	private HashMap<String, String> keggMap;

	private final String goObo = "go.obo";
	private HashMap<String, GoObo> goOboMap;

	private final String ECs = "enzyme.dat";
	private HashMap<String, EnzymeCommission> ecMap;

	public EggNogAddDb() {
		this.initial();
	}

	private void initial() {
		this.cogNameMap = new HashMap<String, String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceLoader.load(cogName)))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cStrings = line.split("\t");
				cogNameMap.put(cStrings[0], cStrings[2]);
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.nogNameMap = new HashMap<String, String[]>();
		try (BufferedReader nogReader = new BufferedReader(new InputStreamReader(ResourceLoader.load(nogName)))) {
			String line = nogReader.readLine();
			while ((line = nogReader.readLine()) != null) {
				String[] cStrings = line.split("\t");
				if (cStrings.length == 3) {
					nogNameMap.put(cStrings[1], new String[] { cStrings[2], "" });
				} else if (cStrings.length == 4) {
					nogNameMap.put(cStrings[1], new String[] { cStrings[2], cStrings[3] });
				}
			}

			nogReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.keggMap = new HashMap<String, String>();
		try (BufferedReader keggReader = new BufferedReader(
				new InputStreamReader(ResourceLoader.load(keggMapNameString)))) {
			String line = keggReader.readLine();
			while ((line = keggReader.readLine()) != null) {
				String[] cStrings = line.split("\t");
				keggMap.put(cStrings[0], cStrings[1]);
			}

			keggReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GoOboReader goReader = null;
		try {
			goReader = new GoOboReader(new BufferedReader(new InputStreamReader(ResourceLoader.load(goObo))));
			this.goOboMap = goReader.getGoMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ECReader ecReader = null;
		try {
			ecReader = new ECReader(new BufferedReader(new InputStreamReader(ResourceLoader.load(ECs))));
			this.ecMap = ecReader.getECMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String findCOG(String name) {
		return cogNameMap.get(name);
	}

	public String[] findNOG(String name) {
		return nogNameMap.get(name);
	}

	public String findKEGG(String name) {
		return keggMap.get(name);
	}

	public GoObo findGoObo(String name) {
		return goOboMap.get(name);
	}

	public EnzymeCommission findEC(String name) {
		return ecMap.get(name);
	}

}
