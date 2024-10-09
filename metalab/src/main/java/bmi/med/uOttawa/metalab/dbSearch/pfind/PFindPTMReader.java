package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

public class PFindPTMReader {

	private HashMap<String, PostTransModification> modMap;

	public PFindPTMReader(String modification) {
		this(new File(modification));
	}

	public PFindPTMReader(File modification) {
		try {
			this.parse(modification);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parse(File modification) throws IOException {
		this.modMap = new HashMap<String, PostTransModification>();
		BufferedReader reader = new BufferedReader(new FileReader(modification));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("name")) {
				line = reader.readLine();
				String[] cs0 = line.split("=");
				String[] cs1 = cs0[1].split("\\s");
				PostTransModification ptm = new PostTransModification(Double.parseDouble(cs1[2]), cs0[0], cs1[0], true);
				modMap.put(cs0[0], ptm);
			}
		}
		reader.close();
	}

	public HashMap<String, PostTransModification> getModMap() {
		return modMap;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		PFindPTMReader reader = new PFindPTMReader("C:\\pFindStudio\\pFind3\\bin\\default\\modification_default.ini");
		System.out.println(reader.getModMap().size());
	}

}
