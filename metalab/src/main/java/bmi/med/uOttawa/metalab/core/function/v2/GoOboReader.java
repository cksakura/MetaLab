package bmi.med.uOttawa.metalab.core.function.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GoOboReader {

	private GoObo[] goObos;
	private BufferedReader reader;

	public GoOboReader(String file) {
		this(new File(file));
	}

	public GoOboReader(File file) {
		try {
			this.reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GoOboReader(BufferedReader reader) {
		this.reader = reader;
	}

	public GoObo[] getGoObos() throws IOException {
		if (goObos != null) {
			return goObos;
		} else {
			ArrayList<GoObo> list = new ArrayList<GoObo>();
			String line = null;
			String id = null;
			String name = null;
			String namespace = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("id:")) {
					id = line.substring("id:".length()).trim();
				} else if (line.startsWith("name:")) {
					name = line.substring("name:".length()).trim();
				} else if (line.startsWith("namespace:")) {
					namespace = line.substring("namespace:".length()).trim();
					GoObo goobo = new GoObo(id, name, namespace);
					list.add(goobo);
				}
			}
			reader.close();
			this.goObos = list.toArray(new GoObo[list.size()]);
			return goObos;
		}
	}

	public HashMap<String, GoObo> getGoMap() throws IOException {
		HashMap<String, GoObo> map = new HashMap<String, GoObo>();
		GoObo[] goobos = this.getGoObos();
		for (int i = 0; i < goobos.length; i++) {
			map.put(goobos[i].getId(), goobos[i]);
		}
		return map;
	}
}
