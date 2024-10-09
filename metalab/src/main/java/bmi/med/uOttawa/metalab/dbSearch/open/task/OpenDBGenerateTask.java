/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerPsm;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerReader;

/**
 * @author Kai Cheng
 *
 */
public class OpenDBGenerateTask {

	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenDBGenerateTask.class);

	private File[] pepxmlFiles;
	private File fasta;
	private File result;

	public OpenDBGenerateTask(String pepxml_dir, String fasta, String result) {
		this(new File(pepxml_dir), new File(fasta), new File(result));
	}

	public OpenDBGenerateTask(File pepxml_dir, File fasta, File result) {
		LOGGER.info("Parsing pepxml files started");

		ArrayList<File> templist = new ArrayList<File>();
		File[] files = pepxml_dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith("pepXML")) {
				File refinedPepxml;
				try {
					refinedPepxml = refine(files[i]);
					templist.add(refinedPepxml);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in parsing pepxml file " + files[i].getAbsolutePath(), e);
				}

			}
		}
		this.pepxmlFiles = templist.toArray(new File[templist.size()]);

		LOGGER.info("Parsing pepxml files finished");

		this.fasta = fasta;
		this.result = result;
	}

	private File refine(File in) throws IOException {
		File out = new File(in.getParentFile(), in.getName() + ".temp");
		if (out.exists()) {
			return out;
		}

		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("<spectrum_query")) {

				int assumed_charge = line.indexOf("assumed_charge=");
				int spectrum = line.indexOf("spectrum=");
				int end = line.indexOf("end_scan=");

				String spstring = line.substring(spectrum, end);
				int file = spstring.indexOf("File:");

				if (file > 0) {
					String[] cs = spstring.substring(0, file).split("\\.");
					StringBuilder sb = new StringBuilder();

					sb.append("<spectrum_query start_scan=\"").append(cs[cs.length - 2]).append("\" ");
					sb.append(line.substring(assumed_charge, spectrum + file - 1)).append("\" ");
					sb.append(line.substring(end));

					writer.println(sb);
				} else {
					writer.println(line);
				}

			} else if (line.startsWith("<search_hit")) {
				String reline = line.substring(1, line.length() - 1);
				if (reline.indexOf("<") > -1) {
					reline = reline.replaceAll("<", "_");
					reline = reline.replaceAll(">", "_");

				} else if (reline.indexOf("&") > -1) {
					reline = reline.replaceAll("&", "_");
				}

				writer.println("<" + reline + ">");

			} else {
				writer.println(line);
			}
		}
		reader.close();
		writer.close();

		return out;
	}

	private void generate(double zero) {

		FastaReader fr = null;
		try {
			fr = new FastaReader(fasta);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading protein sequences from " + this.fasta, e);
		}

		HashMap<String, ProteinItem> promap = new HashMap<String, ProteinItem>();
		ProteinItem[] items = fr.getItems();
		for (ProteinItem pi : items) {
			promap.put(pi.getRef(), pi);
		}

		MSFraggerReader psmReader = new MSFraggerReader(this.pepxmlFiles);
		MSFraggerPsm[] original_psms = psmReader.getPSMs();

		if (original_psms == null) {
			LOGGER.error("Error in parsing PSMs from MSFragger search results");
			return;
		}

		HashMap<String, ProteinItem> selectMap = new HashMap<String, ProteinItem>();
		for (MSFraggerPsm psm : original_psms) {
			if (psm.isTarget()) {
				double massdiff = psm.getMassDiff();
				double charge = psm.getCharge();
				double premr = psm.getPrecursorMr();
				double premz = premr / (double) charge + AminoAcidProperty.PROTON_W;
				double ppm = (massdiff - zero) / premz * 1E6;
				if (Math.abs(ppm) <= 10.0) {
					if (promap.containsKey(psm.getProtein())) {
						selectMap.put(psm.getProtein(), promap.get(psm.getProtein()));
					}
				}
			}
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(result);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String key : selectMap.keySet()) {
			ProteinItem pi = selectMap.get(key);
			String sequence = pi.getSequence();
			String revsequence = (new StringBuilder(sequence)).reverse().toString();
			int lengthCount = sequence.length() / 80;

			writer.println(">" + key);
			for (int i = 0; i < lengthCount; i++) {
				writer.println(sequence.substring(i * 80, (i + 1) * 80));
			}
			writer.println(sequence.substring(lengthCount * 80, sequence.length()));

			writer.println(">REV_" + key);
			for (int i = 0; i < lengthCount; i++) {
				writer.println(revsequence.substring(i * 80, (i + 1) * 80));
			}
			writer.println(revsequence.substring(lengthCount * 80, revsequence.length()));
		}

		writer.close();
	}
}
