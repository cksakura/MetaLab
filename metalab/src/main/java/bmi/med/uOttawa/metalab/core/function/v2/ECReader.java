package bmi.med.uOttawa.metalab.core.function.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ECReader {

	private EnzymeCommission[] ecs;
	private BufferedReader reader;

	public ECReader(String file) {
		this(new File(file));
	}

	public ECReader(File file) {
		try {
			this.reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ECReader(BufferedReader reader) {
		this.reader = reader;
	}

	public EnzymeCommission[] getECs() throws IOException {
		if (ecs != null) {
			return ecs;
		} else {
			ArrayList<EnzymeCommission> list = new ArrayList<EnzymeCommission>();
			String line = null;
			EnzymeCommission enzymeCommission = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("ID")) {

					if (enzymeCommission != null) {
						list.add(enzymeCommission);
					}

					String id = line.substring(2).trim();
					enzymeCommission = new EnzymeCommission(id);

				} else if (line.startsWith("DE")) {
					String de = line.substring(2).trim();
					enzymeCommission.setDe(de);
				} else if (line.startsWith("CA")) {
					String ca = line.substring(2).trim();
					enzymeCommission.addCa(ca);
				} else if (line.startsWith("AN")) {
					String an = line.substring(2).trim();
					enzymeCommission.addAn(an);
				}
			}
			reader.close();

			if (enzymeCommission != null) {
				list.add(enzymeCommission);
			}

			this.ecs = list.toArray(new EnzymeCommission[list.size()]);
			return ecs;
		}
	}

	public HashMap<String, EnzymeCommission> getECMap() throws IOException {
		HashMap<String, EnzymeCommission> map = new HashMap<String, EnzymeCommission>();
		EnzymeCommission[] ecs = this.getECs();
		for (int i = 0; i < ecs.length; i++) {
			map.put(ecs[i].getId(), ecs[i]);
		}
		return map;
	}
}
