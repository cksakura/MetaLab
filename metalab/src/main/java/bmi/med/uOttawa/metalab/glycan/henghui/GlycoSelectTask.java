package bmi.med.uOttawa.metalab.glycan.henghui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;

public class GlycoSelectTask extends SwingWorker<Object, Object> {

	private String in;
	private String out;
	private String fasta;
	private Pattern pattern;

	private static final String[] glycanNames = new String[] { "GalNAzPC", "GalNAzPC-Fuc" };

	public GlycoSelectTask(String in, String out, String fasta, Pattern pattern) {
		super();
		this.in = in;
		this.out = out;
		this.fasta = fasta;
		this.pattern = pattern;
	}

	@Override
	protected Object doInBackground() throws Exception {
		// TODO Auto-generated method stub

		HashMap<String, ProteinItem> promap = new HashMap<String, ProteinItem>();
		FastaReader fr = new FastaReader(fasta);
		ProteinItem[] proItems = fr.getItems();
		for (ProteinItem pro : proItems) {
			String ref = pro.getRef();
			String name = ref.split("\\|")[1];
			promap.put(name, pro);
		}

		BufferedReader reader = new BufferedReader(new FileReader(in));

		String line = reader.readLine();
		String[] title = line.split("\t");

		int proid = -1;
		int seqid = -1;
		HashMap<Integer, String> modidmap = new HashMap<Integer, String>();

		int revid = -1;
		int conid = -1;

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				seqid = i;
			} else if (title[i].equals("Proteins")) {
				proid = i;
			} else if (title[i].equals("Reverse")) {
				revid = i;
			} else if (title[i].equals("Potential contaminant")) {
				conid = i;
			} else {
				for (String glycan : glycanNames) {
					if (title[i].equals(glycan)) {
						modidmap.put(i, title[i]);
					}
				}
			}
		}

		if (seqid == -1 || proid == -1 || revid == -1 || conid == -1 || modidmap.size() == 0) {
			reader.close();
			return null;
		}

		PrintWriter writer = new PrintWriter(out);
		StringBuilder titlesb = new StringBuilder();
		for (int i = 0; i <= seqid; i++) {
			titlesb.append(title[i]).append("\t");
		}
		titlesb.append("Localization in peptide").append("\t");
		titlesb.append("Sequence window").append("\t");
		for (int i = seqid; i < title.length; i++) {
			titlesb.append(title[i]).append("\t");
		}
		titlesb.deleteCharAt(titlesb.length() - 1);
		writer.println(titlesb);

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs[revid].equals("+") || cs[conid].equals("+")) {
				continue;
			}

			boolean use = false;
			for (int i = 0; i < cs.length; i++) {
				if (modidmap.containsKey(i)) {
					if (!cs[i].equals("0")) {
						use = true;
						break;
					}
				}
			}

			if (!use) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			String[] pros = cs[proid].split(";");
			for (int i = 0; i < pros.length; i++) {
				if (promap.containsKey(pros[i])) {
					ProteinItem proitem = promap.get(pros[i]);
					int start = proitem.indexOf(cs[seqid]);
					if (start >= 0) {
						String pre = "";
						if (start > 7) {
							pre = proitem.getPeptide(start - 7, start);
						} else {
							for (int j = 0; j < 7 - start; j++) {
								pre += "_";
							}
							pre += proitem.getPeptide(0, start);
						}

						String next = "";
						if (start + cs[seqid].length() + 7 > proitem.length()) {
							next = proitem.getPeptide(start + cs[seqid].length(), proitem.length());
							for (int j = 0; j < 7 - next.length(); j++) {
								next += "_";
							}
						} else {
							next = proitem.getPeptide(start + cs[seqid].length(), start + cs[seqid].length() + 7);
						}

						String fullSequence = pre + cs[seqid] + next;

						Matcher matcher = pattern.matcher(fullSequence);

						if (matcher.find()) {
							int loc = matcher.start();
							if (loc >= 7 && loc < fullSequence.length() - 7) {
								for (int j = 0; j <= seqid; j++) {
									sb.append(cs[j]).append("\t");
								}
								sb.append(loc - 6).append("\t");
								sb.append(fullSequence.substring(loc - 7, loc + 8)).append("\t");

								for (int j = seqid + 1; j < cs.length; j++) {
									sb.append(cs[j]).append("\t");
								}

								sb.deleteCharAt(sb.length() - 1);
								writer.println(sb);

								break;
							}
						}
					}
				}
			}
		}

		reader.close();
		writer.close();

		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		GlycoSelectTask task = new GlycoSelectTask("D:\\Data\\henghui\\modificationSpecificPeptides.txt",
				"D:\\Data\\henghui\\modificationSpecificPeptides_glycopeptide.txt", "D:\\Data\\henghui\\HUMAN.fasta",
				Pattern.compile("N[A-Z][ST]"));
		task.run();
	}

}
