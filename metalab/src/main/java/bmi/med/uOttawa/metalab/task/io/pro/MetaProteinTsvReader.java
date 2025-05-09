package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MetaProteinTsvReader extends AbstractMetaProteinReader {

	private MetaProtein[] proteins;
	private String[] intensityTitle;

	public MetaProteinTsvReader(String in) {
		super(in);
		// TODO Auto-generated constructor stub
		read();
	}

	public MetaProteinTsvReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		read();
	}

	@SuppressWarnings("unused")
	private void read() {
		ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
		try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
			String line = reader.readLine();
			String[] title = line.split("\t");
			int proId = -1;
			int psmId = -1;
			int pepAllId = -1;
			int pepRazorId = -1;
			int scoreId = -1;
			int id = -1;
			HashMap<Integer, String> intenIdMap = new HashMap<Integer, String>();

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Majority protein IDs")) {
					proId = i;
				} else if (title[i].equals("Peptide counts (all)")) {
					pepAllId = i;
				} else if (title[i].equals("Peptide counts (razor)")) {
					pepRazorId = i;
				} else if (title[i].equals("PSM")) {
					psmId = i;
				} else if (title[i].equals("PEP")) {
					scoreId = i;
				} else if (title[i].equals("id")) {
					id = i;
				} else if (title[i].startsWith("Intensity ")) {
					intenIdMap.put(i, title[i]);
				}
			}
			Integer[] ids = intenIdMap.keySet().toArray(Integer[]::new);
			Arrays.sort(ids);

			this.intensityTitle = new String[ids.length];
			for (int i = 0; i < ids.length; i++) {
				intensityTitle[i] = intenIdMap.get(ids[i]);
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");

				int pepAllCount = pepAllId > -1 ? Integer.parseInt(cs[pepAllId]) : 0;
				int pepRazorCount = pepRazorId > -1 ? Integer.parseInt(cs[pepRazorId]) : 0;
				int psmCount = psmId > -1 ? Integer.parseInt(cs[psmId]) : 0;

				double[] intensity = new double[intenIdMap.size()];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[ids[i]]);
				}

				double score = scoreId > -1 ? Double.parseDouble(cs[scoreId]) : 0.0;

				MetaProtein protein = new MetaProtein(cs[proId], pepAllCount, psmCount, score, intensity);
				list.add(protein);
			}
			reader.close();
		} catch (IOException e) {

		}

		proteins = list.toArray(new MetaProtein[list.size()]);
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaProtein[] getMetaProteins() {
		// TODO Auto-generated method stub
		return proteins;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return intensityTitle;
	}
}
