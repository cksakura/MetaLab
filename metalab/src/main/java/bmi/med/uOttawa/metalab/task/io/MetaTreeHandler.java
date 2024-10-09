/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * Export the taxon information in a tree format.
 * 
 * @author Kai Cheng
 *
 */
public class MetaTreeHandler {
	
	private static DecimalFormat df2 = FormatTool.getDF2();

	private static Logger LOGGER = LogManager.getLogger();

	public static void export(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, String out) {
		export(peps, taxons, expNames, new File(out));
	}

	/**
	 * Export the result in plain txt format
	 * @param peps
	 * @param taxons
	 * @param expNames
	 * @param out
	 */
	public static void export(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, File out) {

		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxons) {
			taxonMap.put(taxon.getId(), taxon);
		}

		HashMap<Integer, double[]> taxonIntensityMap = new HashMap<Integer, double[]>();
		double[] cellularIntensity = new double[expNames.length];

		for (MetaPeptide mp : peps) {

			double[] intensity = mp.getIntensity();
			int lcaId = mp.getLcaId();
			Taxon lca = taxonMap.get(lcaId);
			if (lca == null) {
				continue;
			}

			int[] mainParentIds = lca.getMainParentIds();

			int lastId = -1;
			for (int i = 0; i < mainParentIds.length; i++) {
				Taxon parentTaxon = taxonMap.get(mainParentIds[i]);
				if (mainParentIds[i] == -1 || parentTaxon == null) {
					if (i != 1) {
						break;
					}
				} else {
					lastId = mainParentIds[i];
				}
			}

			if (lcaId != lastId) {
				continue;
			}

			for (int i = 0; i < intensity.length; i++) {
				cellularIntensity[i] += intensity[i];
			}

			for (int parentId : mainParentIds) {
				if (parentId == -1)
					continue;

				if (taxonIntensityMap.containsKey(parentId)) {
					double[] totalIntensity = taxonIntensityMap.get(parentId);
					for (int i = 0; i < totalIntensity.length; i++) {
						totalIntensity[i] += intensity[i];
					}
					taxonIntensityMap.put(parentId, totalIntensity);

				} else {
					double[] totalIntensity = new double[intensity.length];
					System.arraycopy(intensity, 0, totalIntensity, 0, intensity.length);
					taxonIntensityMap.put(parentId, totalIntensity);
				}
			}
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out.getName(), e);
		}
		StringBuilder title = new StringBuilder();
		title.append("id").append(",").append("value").append(",");
		for (String name : expNames) {
			title.append(name).append(";");
		}
		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}
		StringBuilder cellularSb = new StringBuilder();

		double logTotalCellularIntensity = 0;
		if (totalCellularIntensity > 1) {
			logTotalCellularIntensity = totalCellularIntensity;
		}

		cellularSb.append("cellular organisms").append(",").append(df2.format(logTotalCellularIntensity))
				.append(",");

		for (int i = 0; i < cellularIntensity.length; i++) {
			double logCellularIntensity = 0;
			if (cellularIntensity[i] > 1) {
				logCellularIntensity = cellularIntensity[i];
			}
			cellularSb.append(df2.format(logCellularIntensity)).append(";");
		}
		cellularSb.deleteCharAt(cellularSb.length() - 1);
		writer.println(cellularSb);

		for (Integer id : taxonIntensityMap.keySet()) {

			StringBuilder sb = new StringBuilder("cellular organisms@");

			Taxon lca = taxonMap.get(id);
			int[] mainParentIds = lca.getMainParentIds();
			for (int i = 0; i < mainParentIds.length; i++) {

				if (mainParentIds[i] == -1) {
					if (i == 1) {
						continue;
					} else {
						break;
					}
				}

				String name = taxonMap.get(mainParentIds[i]).getName();
				sb.append(name).append("@");
			}

			sb.deleteCharAt(sb.length() - 1);
			sb.append(",");

			double[] intensity = taxonIntensityMap.get(id);
			double total = 0;
			for (double inten : intensity) {
				total += inten;
			}

			double logTotal = 0;
			if (total > 1) {
				logTotal = total;
			}
			sb.append(df2.format(logTotal)).append(",");

			for (double inten : intensity) {
				double logInten = 0;
				if (inten > 1) {
					logInten = inten;
				}
				sb.append(df2.format(logInten)).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);

			writer.println(sb);
		}
		writer.close();
	}

	public static void exportJS(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, String out)
			throws FileNotFoundException {
		exportJS(peps, taxons, expNames, new File(out));
	}

	/**
	 * Export the result in json format 
	 * @param peps
	 * @param taxons
	 * @param expNames
	 * @param out
	 */
	public static void exportJS(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, File out) {

		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxons) {
			taxonMap.put(taxon.getId(), taxon);
		}

		HashMap<Integer, double[]> taxonIntensityMap = new HashMap<Integer, double[]>();
		double[] cellularIntensity = new double[expNames.length];

		for (MetaPeptide mp : peps) {

			double[] intensity = mp.getIntensity();
			int lcaId = mp.getLcaId();
			Taxon lca = taxonMap.get(lcaId);
			if (lca == null) {
				continue;
			}

			int[] mainParentIds = lca.getMainParentIds();

			int lastId = -1;
			for (int i = 0; i < mainParentIds.length; i++) {
				Taxon parentTaxon = taxonMap.get(mainParentIds[i]);
				if (mainParentIds[i] == -1 || parentTaxon == null) {
					if (i != 1) {
						break;
					}
				} else {
					lastId = mainParentIds[i];
				}
			}

			if (lcaId != lastId) {
				continue;
			}

			for (int i = 0; i < intensity.length; i++) {
				cellularIntensity[i] += intensity[i];
			}

			for (int parentId : mainParentIds) {
				if (parentId == -1)
					continue;

				if (taxonIntensityMap.containsKey(parentId)) {
					double[] totalIntensity = taxonIntensityMap.get(parentId);
					for (int i = 0; i < totalIntensity.length; i++) {
						totalIntensity[i] += intensity[i];
					}
					taxonIntensityMap.put(parentId, totalIntensity);

				} else {
					double[] totalIntensity = new double[intensity.length];
					System.arraycopy(intensity, 0, totalIntensity, 0, intensity.length);
					taxonIntensityMap.put(parentId, totalIntensity);
				}
			}
		}

		String exp = "";
		StringBuilder expsb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			expsb.append(expNames[i]).append(";");
		}
		if (expsb.length() > 0) {
			exp = expsb.substring(0, expsb.length() - 1);
		}

		StringBuilder jsonsb = new StringBuilder();
		JSONWriter jw = new JSONWriter(jsonsb);
		jw.object();

		jw.key("").array();

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}

		jw.object();
		jw.key("id").value("cellular organisms");
		jw.key("value").value(df2.format(totalCellularIntensity));

		String totalInten = "";
		StringBuilder totalIntensb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			totalIntensb.append(df2.format(cellularIntensity[i])).append(";");

		}
		if (totalIntensb.length() > 0) {
			totalInten = totalIntensb.substring(0, totalIntensb.length() - 1);
		}
		jw.key(exp).value(totalInten);
		jw.endObject();

		for (Integer id : taxonIntensityMap.keySet()) {

			StringBuilder sb = new StringBuilder("cellular organisms@");

			Taxon lca = taxonMap.get(id);
			int[] mainParentIds = lca.getMainParentIds();
			for (int i = 0; i < mainParentIds.length; i++) {

				if (mainParentIds[i] == -1) {
					if (i == 1) {
						continue;
					} else {
						break;
					}
				}

				String name = taxonMap.get(mainParentIds[i]).getName();
				sb.append(name).append("@");
			}

			sb.deleteCharAt(sb.length() - 1);

			jw.object();

			jw.key("id").value(sb.toString());

			double[] intensity = taxonIntensityMap.get(id);
			double total = 0;
			for (double inten : intensity) {
				total += inten;
			}

			jw.key("value").value(df2.format(total));

			String inten = "";
			StringBuilder intensb = new StringBuilder();
			for (int i = 0; i < expNames.length; i++) {
				intensb.append(df2.format(intensity[i])).append(";");

			}
			if (intensb.length() > 0) {
				inten = intensb.substring(0, intensb.length() - 1);
			}
			jw.key(exp).value(inten);

			jw.endObject();
		}

		jw.endArray();
		jw.endObject();

		String finalString = "let data = '" + jsonsb.substring(4, jsonsb.length() - 1) + "';";

		PrintWriter writer;
		try {
			writer = new PrintWriter(out);
			writer.print(finalString);
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out.getName(), e);
		}

	}
	
	public static void exportJS(HashMap<String, double[]> taxIntensityMap, double[] cellularIntensity,
			String[] expNames, File out) {

		String exp = "";
		StringBuilder expsb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			expsb.append(expNames[i]).append(";");
		}
		if (expsb.length() > 0) {
			exp = expsb.substring(0, expsb.length() - 1);
		}

		StringBuilder jsonsb = new StringBuilder();
		JSONWriter jw = new JSONWriter(jsonsb);
		jw.object();

		jw.key("").array();

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}

		jw.object();
		jw.key("id").value("cellular organisms");
		jw.key("value").value(df2.format(totalCellularIntensity));

		String totalInten = "";
		StringBuilder totalIntensb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			totalIntensb.append(df2.format(cellularIntensity[i])).append(";");

		}
		if (totalIntensb.length() > 0) {
			totalInten = totalIntensb.substring(0, totalIntensb.length() - 1);
		}
		jw.key(exp).value(totalInten);
		jw.endObject();

		for (String taxon : taxIntensityMap.keySet()) {

			double[] intensity = taxIntensityMap.get(taxon);

			StringBuilder sb = new StringBuilder("cellular organisms@");

			sb.deleteCharAt(sb.length() - 1);

			jw.object();

			jw.key("id").value("cellular organisms@" + taxon);

			double total = 0;
			for (double inten : intensity) {
				total += inten;
			}

			jw.key("value").value(df2.format(total));

			String inten = "";
			StringBuilder intensb = new StringBuilder();
			for (int j = 0; j < expNames.length; j++) {
				intensb.append(df2.format(intensity[j])).append(";");

			}
			if (intensb.length() > 0) {
				inten = intensb.substring(0, intensb.length() - 1);
			}
			jw.key(exp).value(inten);

			jw.endObject();

		}

		jw.endArray();
		jw.endObject();

		String finalString = "let data = '" + jsonsb.substring(4, jsonsb.length() - 1) + "';";

		PrintWriter writer;
		try {
			writer = new PrintWriter(out);
			writer.print(finalString);
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out.getName(), e);
		}
	}
	
	public static void exportJS(HashMap<String, String[]>[] taxRankMaps, HashMap<String, double[]>[] taxIntensityMaps,
			double[] cellularIntensity, String[] expNames, File out) {

		String exp = "";
		StringBuilder expsb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			expsb.append(expNames[i]).append(";");
		}
		if (expsb.length() > 0) {
			exp = expsb.substring(0, expsb.length() - 1);
		}

		StringBuilder jsonsb = new StringBuilder();
		JSONWriter jw = new JSONWriter(jsonsb);
		jw.object();

		jw.key("").array();

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}

		jw.object();
		jw.key("id").value("cellular organisms");
		jw.key("value").value(df2.format(totalCellularIntensity));

		String totalInten = "";
		StringBuilder totalIntensb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			totalIntensb.append(df2.format(cellularIntensity[i])).append(";");

		}
		if (totalIntensb.length() > 0) {
			totalInten = totalIntensb.substring(0, totalIntensb.length() - 1);
		}
		jw.key(exp).value(totalInten);
		jw.endObject();

		for (int i = 0; i < taxRankMaps.length; i++) {
			for (String taxon : taxRankMaps[i].keySet()) {
				if (taxon == null || taxon.length() == 0) {
					continue;
				}

				String[] taxaContent = taxRankMaps[i].get(taxon);
				double[] intensity = taxIntensityMaps[i].get(taxon);

				StringBuilder sb = new StringBuilder("cellular organisms@");

				for (int j = 0; j < taxaContent.length; j++) {
					if (taxaContent[j] != null && taxaContent[j].length() > 0) {
						sb.append(taxaContent[j]).append("@");
					}
				}

				sb.deleteCharAt(sb.length() - 1);

				jw.object();

				jw.key("id").value(sb.toString());

				double total = 0;
				for (double inten : intensity) {
					total += inten;
				}

				jw.key("value").value(df2.format(total));

				String inten = "";
				StringBuilder intensb = new StringBuilder();
				for (int j = 0; j < expNames.length; j++) {
					intensb.append(df2.format(intensity[j])).append(";");

				}
				if (intensb.length() > 0) {
					inten = intensb.substring(0, intensb.length() - 1);
				}
				jw.key(exp).value(inten);

				jw.endObject();
			}
		}

		jw.endArray();
		jw.endObject();

		String finalString = "let data = '" + jsonsb.substring(4, jsonsb.length() - 1) + "';";

		PrintWriter writer;
		try {
			writer = new PrintWriter(out);
			writer.print(finalString);
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out.getName(), e);
		}
	}

	public static void export(MetaPeptide[] peps, Taxon[] taxons, HashMap<Integer, Taxon> taxonMap, String[] expNames,
			String out) {

		HashMap<Integer, double[]> taxonIntensityMap = new HashMap<Integer, double[]>();
		double[] cellularIntensity = new double[expNames.length];

		for (MetaPeptide mp : peps) {

			double[] intensity = mp.getIntensity();
			int lcaId = mp.getLcaId();
			Taxon lca = taxonMap.get(lcaId);
			if (lca == null) {
				continue;
			}

			int[] mainParentIds = lca.getMainParentIds();

			int lastId = -1;
			for (int i = 0; i < mainParentIds.length; i++) {
				Taxon parentTaxon = taxonMap.get(mainParentIds[i]);
				if (mainParentIds[i] == -1 || parentTaxon == null) {
					if (i != 1) {
						break;
					}
				} else {
					lastId = mainParentIds[i];
				}
			}

			if (lcaId != lastId) {
				continue;
			}

			for (int i = 0; i < intensity.length; i++) {
				cellularIntensity[i] += intensity[i];
			}

			for (int parentId : mainParentIds) {
				if (parentId == -1)
					continue;

				if (taxonIntensityMap.containsKey(parentId)) {
					double[] totalIntensity = taxonIntensityMap.get(parentId);
					for (int i = 0; i < totalIntensity.length; i++) {
						totalIntensity[i] += intensity[i];
					}
					taxonIntensityMap.put(parentId, totalIntensity);

				} else {
					double[] totalIntensity = new double[intensity.length];
					System.arraycopy(intensity, 0, totalIntensity, 0, intensity.length);
					taxonIntensityMap.put(parentId, totalIntensity);
				}
			}
		}

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out, e);
		}

		StringBuilder title = new StringBuilder();
		title.append("id").append(",").append("value").append(",");
		for (String name : expNames) {
			title.append(name).append(";");
		}
		title.deleteCharAt(title.length() - 1);
		writer.println(title);

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}
		StringBuilder cellularSb = new StringBuilder();

		double logTotalCellularIntensity = 0;
		if (totalCellularIntensity > 1) {
			logTotalCellularIntensity = 10.0 * Math.log10(totalCellularIntensity);
		}

		cellularSb.append("cellular organisms").append(",").append(df2.format(logTotalCellularIntensity))
				.append(",");

		for (int i = 0; i < cellularIntensity.length; i++) {
			double logCellularIntensity = 0;
			if (cellularIntensity[i] > 1) {
				logCellularIntensity = 10.0 * Math.log10(cellularIntensity[i]);
			}
			cellularSb.append(df2.format(logCellularIntensity)).append(";");
		}
		cellularSb.deleteCharAt(cellularSb.length() - 1);
		writer.println(cellularSb);

		for (Integer id : taxonIntensityMap.keySet()) {

			StringBuilder sb = new StringBuilder("cellular organisms@");

			Taxon lca = taxonMap.get(id);
			int[] mainParentIds = lca.getMainParentIds();
			for (int i = 0; i < mainParentIds.length; i++) {

				if (mainParentIds[i] == -1) {
					if (i == 1) {
						continue;
					} else {
						break;
					}
				}

				String name = taxonMap.get(mainParentIds[i]).getName();
				sb.append(name).append("@");
			}

			sb.deleteCharAt(sb.length() - 1);
			sb.append(",");

			double[] intensity = taxonIntensityMap.get(id);
			double total = 0;
			for (double inten : intensity) {
				total += inten;
			}

			double logTotal = 0;
			if (total > 1) {
				logTotal = 10.0 * Math.log10(total);
			}
			sb.append(df2.format(logTotal)).append(",");

			for (double inten : intensity) {
				double logInten = 0;
				if (inten > 1) {
					logInten = 10.0 * Math.log10(inten);
				}
				sb.append(df2.format(logInten)).append(";");
			}
			sb.deleteCharAt(sb.length() - 1);

			writer.println(sb);
		}
		writer.close();
	}

	/**
	 * 
	 * @param peps
	 * @param taxons
	 * @param expNames
	 * @param out
	 * @throws IOException
	 */
	public static void export4Megan(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, String out)
			throws IOException {

		export4Megan(peps, taxons, expNames, new File(out));
	}

	/**
	 * Export the result in csv format
	 * 
	 * @param peps
	 * @param taxons
	 * @param expNames
	 * @param out
	 * @throws IOException
	 */
	public static void export4Megan(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, File out) {

		if (!out.exists()) {
			out.mkdirs();
		}

		PrintWriter[] writers = new PrintWriter[expNames.length];
		for (int i = 0; i < writers.length; i++) {
			if (expNames[i].equalsIgnoreCase("con")) {
				File outputi = new File(out, expNames[i] + i + ".csv");
				try {
					writers[i] = new PrintWriter(outputi);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing Megan result file to " + outputi, e);
				}
			} else {
				File outputi = new File(out, expNames[i] + ".csv");
				try {
					writers[i] = new PrintWriter(outputi);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in writing Megan result file to " + outputi, e);
				}
			}
		}

		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxons) {
			taxonMap.put(taxon.getId(), taxon);
		}

		HashMap<Integer, double[]> taxonIntensityMap = new HashMap<Integer, double[]>();

		for (MetaPeptide mp : peps) {

			double[] intensity = mp.getIntensity();
			int lcaId = mp.getLcaId();
			Taxon lca = taxonMap.get(lcaId);
			if (lca == null) {
				continue;
			}

			int[] mainParentIds = lca.getMainParentIds();

			int lastId = -1;
			for (int i = 0; i < mainParentIds.length; i++) {
				Taxon parentTaxon = taxonMap.get(mainParentIds[i]);
				if (mainParentIds[i] == -1 || parentTaxon == null) {
					break;
				} else {
					lastId = mainParentIds[i];
				}
			}

			if (lcaId != lastId) {
				continue;
			}

			for (int parentId : mainParentIds) {
				if (parentId == -1)
					continue;

				if (taxonIntensityMap.containsKey(parentId)) {
					double[] totalIntensity = taxonIntensityMap.get(parentId);
					for (int i = 0; i < totalIntensity.length; i++) {
						totalIntensity[i] += intensity[i];
					}
					taxonIntensityMap.put(parentId, totalIntensity);

				} else {
					double[] totalIntensity = new double[intensity.length];
					System.arraycopy(intensity, 0, totalIntensity, 0, intensity.length);
					taxonIntensityMap.put(parentId, totalIntensity);
				}
			}
		}

		for (Integer id : taxonIntensityMap.keySet()) {

			Taxon lca = taxonMap.get(id);
			String name = lca.getName();

			double[] intensity = taxonIntensityMap.get(id);

			for (int i = 0; i < intensity.length; i++) {
				double logInten = 0;
				if (intensity[i] > 1) {
					logInten = 10.0 * Math.log10(intensity[i]);
				}
				writers[i].println(name + "," + ((int) logInten));
			}
		}

		for (PrintWriter writer : writers) {
			writer.close();
		}
	}
	
	private static void exportJS(File in, File out) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = reader.readLine();
		String[] title = line.split("\t");
		String[] expNames = new String[title.length - 14];
		System.arraycopy(title, 14, expNames, 0, expNames.length);
		double[] cellularIntensity = new double[expNames.length];

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		HashMap<String, String[]>[] taxRankMaps = new HashMap[mainRanks.length];
		HashMap<String, double[]>[] taxIntensityMaps = new HashMap[mainRanks.length];
		for (int i = 0; i < taxRankMaps.length; i++) {
			taxRankMaps[i] = new HashMap<String, String[]>();
			taxIntensityMaps[i] = new HashMap<String, double[]>();
		}

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");

			String[] taxaContent = new String[mainRanks.length];
			System.arraycopy(cs, 6, taxaContent, 0, taxaContent.length);

			double[] genomeIntensity = new double[expNames.length];
			for (int i = 0; i < genomeIntensity.length; i++) {
				genomeIntensity[i] = Double.parseDouble(cs[i + 14]);
			}

			for (int i = 0; i < genomeIntensity.length; i++) {
				cellularIntensity[i] += genomeIntensity[i];
			}

			for (int i = 0; i < taxRankMaps.length; i++) {
				String taxon = taxaContent[i];
				if (taxon == null || taxon.length() == 0) {
					continue;
				}
				if (!taxRankMaps[i].containsKey(taxon)) {
					String[] lineage = new String[i + 1];
					System.arraycopy(taxaContent, 0, lineage, 0, lineage.length);
					taxRankMaps[i].put(taxon, lineage);
				}

				double[] taxIntensity;
				if (taxIntensityMaps[i].containsKey(taxon)) {
					taxIntensity = taxIntensityMaps[i].get(taxon);
				} else {
					taxIntensity = new double[genomeIntensity.length];
				}

				for (int j = 0; j < taxIntensity.length; j++) {
					taxIntensity[j] += genomeIntensity[j];
				}
				taxIntensityMaps[i].put(taxon, taxIntensity);
			}

		}
		reader.close();

		String exp = "";
		StringBuilder expsb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			expsb.append(expNames[i]).append(";");
		}
		if (expsb.length() > 0) {
			exp = expsb.substring(0, expsb.length() - 1);
		}

		StringBuilder jsonsb = new StringBuilder();
		JSONWriter jw = new JSONWriter(jsonsb);
		jw.object();

		jw.key("").array();

		double totalCellularIntensity = 0;
		for (int i = 0; i < cellularIntensity.length; i++) {
			totalCellularIntensity += cellularIntensity[i];
		}

		jw.object();
		jw.key("id").value("cellular organisms");
		jw.key("value").value(df2.format(totalCellularIntensity));

		String totalInten = "";
		StringBuilder totalIntensb = new StringBuilder();
		for (int i = 0; i < expNames.length; i++) {
			totalIntensb.append(df2.format(cellularIntensity[i])).append(";");

		}
		if (totalIntensb.length() > 0) {
			totalInten = totalIntensb.substring(0, totalIntensb.length() - 1);
		}
		jw.key(exp).value(totalInten);
		jw.endObject();

		for (int i = 0; i < taxRankMaps.length; i++) {
			for (String taxon : taxRankMaps[i].keySet()) {
				if (taxon == null || taxon.length() == 0) {
					continue;
				}

				String[] taxaContent = taxRankMaps[i].get(taxon);
				double[] intensity = taxIntensityMaps[i].get(taxon);

				StringBuilder sb = new StringBuilder("cellular organisms@");

				for (int j = 0; j < taxaContent.length; j++) {
					if (taxaContent[j] != null && taxaContent[j].length() > 0) {
						sb.append(taxaContent[j]).append("@");
					}
				}

				sb.deleteCharAt(sb.length() - 1);

				jw.object();

				jw.key("id").value(sb.toString());

				double total = 0;
				for (double inten : intensity) {
					total += inten;
				}

				jw.key("value").value(df2.format(total));

				String inten = "";
				StringBuilder intensb = new StringBuilder();
				for (int j = 0; j < expNames.length; j++) {
					intensb.append(df2.format(intensity[j])).append(";");

				}
				if (intensb.length() > 0) {
					inten = intensb.substring(0, intensb.length() - 1);
				}
				jw.key(exp).value(inten);

				jw.endObject();
			}
		}

		jw.endArray();
		jw.endObject();

		String finalString = "let data = '" + jsonsb.substring(4, jsonsb.length() - 1) + "';";

		PrintWriter writer;
		try {
			writer = new PrintWriter(out);
			writer.print(finalString);
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing phylogenetic tree file to " + out.getName(), e);
		}
	}

}
