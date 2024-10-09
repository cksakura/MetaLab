package bmi.med.uOttawa.metalab.core.function.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CogCsvReader {

	private Cog[] cogs;
	private File file;

	public CogCsvReader(String file) {
		this(new File(file));
	}

	public CogCsvReader(File file) {
		this.file = file;
	}

	public Cog[] getCogs() throws IOException {
		if (cogs != null) {
			return cogs;
		} else {
			ArrayList<Cog> list = new ArrayList<Cog>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split(",");
				if (cs.length == 3) {
					Cog cog = new Cog(cs[0], cs[1].charAt(0), cs[2].trim());
					list.add(cog);
				}
			}
			reader.close();
			this.cogs = list.toArray(new Cog[list.size()]);
			return cogs;
		}
	}

	public HashMap<String, Cog> getGoMap() throws IOException {
		HashMap<String, Cog> map = new HashMap<String, Cog>();
		Cog[] cogs = this.getCogs();
		for (int i = 0; i < cogs.length; i++) {
			map.put(cogs[i].getId(), cogs[i]);
		}
		return map;
	}
}
