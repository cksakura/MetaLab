/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.swing.SwingWorker;

/**
 * @author Kai Cheng
 *
 */
public class FastaExtractTask extends SwingWorker<Void, Void> {

	private String fastain;
	private String fastaout;
	private String protein;
	private int type;

	private static int uniprot = 0;
	private static int dna = 1;

	public FastaExtractTask(String fastain, String fastaout, String protein, int type) {
		this.fastain = fastain;
		this.fastaout = fastaout;
		this.protein = protein;
		this.type = type;
	}

	@Override
	protected Void doInBackground() throws IOException {
		// TODO Auto-generated method stub

		HashSet<String> set = new HashSet<String>();
		BufferedReader proreader = new BufferedReader(new FileReader(protein));
		String line = proreader.readLine();
		while ((line = proreader.readLine()) != null) {
			if (type == dna) {
				String[] cs = line.split("\\s");
				set.add(cs[0]);
			} else {
				set.add(line);
			}
		}
		proreader.close();

		boolean find = false;
		PrintWriter writer = new PrintWriter(fastaout);
		BufferedReader fastaReader = new BufferedReader(new FileReader(fastain));
		while ((line = fastaReader.readLine()) != null) {
			if (line.startsWith(">")) {
				String accession = this.getAccession(line);
				if (set.contains(accession)) {
					set.remove(accession);
					find = true;
					writer.println(line);
				} else {
					find = false;
				}
			} else {
				if (find) {
					writer.println(line);
				}
			}
		}

		fastaReader.close();
		writer.close();

		return null;
	}

	private String getAccession(String line) {

		if (type == uniprot) {
			int id = line.indexOf("|", 4);
			return line.substring(1, id);
		} else if (type == dna) {
			String[] cs = line.split("\\s");
			return cs[0].substring(1);
		}

		return null;
	}

}
