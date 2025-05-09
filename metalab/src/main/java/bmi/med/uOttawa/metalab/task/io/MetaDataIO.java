package bmi.med.uOttawa.metalab.task.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MetaDataIO {

	private String[] path;
	private String[] expNames;

	public MetaDataIO(String in) {
		this(new File(in));
	}

	public MetaDataIO(File in) {
		this.read(in);
	}

	private void read(File in) {
		String name = in.getName();
		if (name.endsWith("txt") || name.endsWith("TXT")) {
			ArrayList<String> pathList = new ArrayList<String>();
			ArrayList<String> expList = new ArrayList<String>();

			try {
				BufferedReader reader = new BufferedReader(new FileReader(in));
				String line = reader.readLine();
				String[] title = line.split("\t");
				int pathId = -1;
				int expId = -1;

				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("File path")) {
						pathId = i;
					} else if (title[i].equals("Experiment name")) {
						expId = i;
					} else if (title[i].startsWith("Meta ")) {

					}
				}

				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					for (int i = 0; i < cs.length; i++) {
						if (i == pathId) {
							pathList.add(cs[i]);
						} else if (i == expId) {
							expList.add(cs[i]);
						}
					}
				}
				reader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.path = pathList.toArray(new String[pathList.size()]);
			this.expNames = expList.toArray(new String[expList.size()]);

		} else if (name.endsWith("csv") || name.endsWith("CSV")) {

		} else if (name.endsWith("xlsx") || name.endsWith("XLSX")) {

		}
	}

	public String[] getPath() {
		return path;
	}

	public String[] getExpNames() {
		return expNames;
	}
	
	
}
