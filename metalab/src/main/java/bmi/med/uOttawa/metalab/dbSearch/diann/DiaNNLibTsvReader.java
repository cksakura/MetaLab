package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DiaNNLibTsvReader {

	private File input;
	private DiaNNTransit[] transitions;
	private String titleString;

	public DiaNNLibTsvReader(String in) {
		this.input = new File(in);
	}

	public DiaNNTransit[] getTransitions() {
		if (this.transitions == null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(input));
				String line = reader.readLine();
				this.titleString = line;

				int transitId = -1;
				int seqId = -1;
				int proId = -1;
				int uniprotId = -1;
				String[] title = line.split("\t");
				for (int i = 0; i < title.length; i++) {
					if (title[i].equals("transition_name")) {
						transitId = i;
					} else if (title[i].equals("PeptideSequence")) {
						seqId = i;
					} else if (title[i].equals("ProteinGroup")) {
						proId = i;
					} else if (title[i].equals("UniprotID")) {
						uniprotId = i;
					}
				}

				ArrayList<DiaNNTransit> list = new ArrayList<DiaNNTransit>();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					DiaNNTransit transition = new DiaNNTransit(cs, transitId, seqId, proId, uniprotId);
					list.add(transition);
				}
				reader.close();

				this.transitions = list.toArray(new DiaNNTransit[list.size()]);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return transitions;
	}

	public String getTitleString() {
		return titleString;
	}
}
