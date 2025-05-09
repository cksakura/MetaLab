/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

/**
 * Export the taxa information in Biom format.
 * 
 * @see http://biom-format.org/
 * @author Kai Cheng
 *
 */
public class MetaBiomJsonHandler {

	private static final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
	private static final String[] rank = new String[] { "k__", "p__", "c__", "o__", "f__", "g__", "s__" };

	private static Logger LOGGER = LogManager.getLogger();

	public static void export(MetaPeptide[] peps, Taxon[] taxons, String[] expNames, String out) {

		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (int i = 0; i < taxons.length; i++) {
			taxonMap.put(taxons[i].getId(), taxons[i]);
		}

		export(peps, taxons, taxonMap, expNames, out);
	}

	public static void export(MetaPeptide[] peps, Taxon[] taxons, HashMap<Integer, Taxon> taxonMap, String[] expNames,
			String out) {

		HashSet<Integer> lcaIdSet = new HashSet<Integer>();
		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();

		for (MetaPeptide peptide : peps) {

			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			lcaIdSet.add(peptide.getLcaId());
			double[] intensities = peptide.getIntensity();

			int[] parentIds = taxonMap.get(lcaId).getMainParentIds();

			for (int parentId : parentIds) {
				if (intensityMap.containsKey(parentId)) {
					double[] intens = intensityMap.get(parentId);
					for (int i = 0; i < intens.length; i++) {
						intens[i] += intensities[i];
					}
				} else {
					double[] intens = new double[intensities.length];
					System.arraycopy(intensities, 0, intens, 0, intens.length);
					intensityMap.put(parentId, intens);
				}
			}
		}

		Date date = new Date();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);

			JSONWriter jw = new JSONWriter(writer);
			jw.object();
			jw.key("id").value("null");
			jw.key("format").value("Biological Observation Matrix 0.9.1-dev");
			jw.key("format_url").value("http://biom-format.org/documentation/format_versions/biom-1.0.html");
			jw.key("type").value("OTU table");
			jw.key("generated_by").value("Metalab 1.0");
			jw.key("date").value(format1.format(date) + "T" + format2.format(date));

			jw.key("rows").array();

			double maxIntensity = 0;
			int id = 0;
			for (Integer taxId : intensityMap.keySet()) {
				if (taxonMap.containsKey(taxId)) {

					id++;

					Taxon taxon = taxonMap.get(taxId);
					int[] lineage = taxon.getMainParentIds();
					String[] lineageNames = new String[lineage.length];

					if (taxonMap.containsKey(lineage[0])) {
						lineageNames[0] = rank[0] + taxonMap.get(lineage[0]).getName();
					} else {
						lineageNames[0] = rank[0];
					}

					for (int i = 1; i < rank.length; i++) {
						if (taxonMap.containsKey(lineage[i + 1])) {
							lineageNames[i] = rank[i] + taxonMap.get(lineage[i + 1]).getName();
						} else {
							lineageNames[i] = rank[i];
						}
					}
					HashMap<String, String[]> map = new HashMap<String, String[]>();
					map.put("taxonomy", lineageNames);

					JSONObject jso = new JSONObject(map);

					jw.object();
					jw.key("id").value("GG_OTU_" + id);
					jw.key("metadata").value(jso);
					jw.endObject();

					double[] intensities = intensityMap.get(taxId);
					for (double inten : intensities) {
						if (inten > maxIntensity) {
							maxIntensity = inten;
						}
					}
				}
			}
			jw.endArray();

			jw.key("columns").array();
			for (String name : expNames) {
				jw.object();
				jw.key("id").value(name);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("BarcodeSequence", "");
				map.put("LinkerPrimerSequence", "");
				map.put("BODY_SITE", "");
				map.put("Description", "");

				JSONObject jsb = new JSONObject(map);

				jw.key("metadata").value(jsb);
				jw.endObject();
			}

			jw.endArray();

			jw.key("matrix_type").value("dense");
			jw.key("matrix_element_type").value("int");

			int[] shapeNum = new int[] { id, expNames.length };
			JSONArray shape = new JSONArray(shapeNum);
			jw.key("shape").value(shape);

			int[][] values = new int[id][expNames.length];
			if (maxIntensity > Integer.MAX_VALUE) {

				double k = (maxIntensity / (double) Integer.MAX_VALUE + 1) * 1000;
				int id2 = 0;
				for (Integer taxId : intensityMap.keySet()) {
					if (taxonMap.containsKey(taxId)) {
						double[] intensities = intensityMap.get(taxId);
						for (int i = 0; i < expNames.length; i++) {
							values[id2][i] = (int) (intensities[i] / k);
						}
						id2++;
					}
				}

			} else {
				int id2 = 0;
				for (Integer taxId : intensityMap.keySet()) {
					if (taxonMap.containsKey(taxId)) {
						double[] intensities = intensityMap.get(taxId);
						for (int i = 0; i < expNames.length; i++) {
							values[id2][i] = (int) intensities[i];
						}
						id2++;
					}
				}
			}

			JSONArray data = new JSONArray(values);
			jw.key("data").value(data);

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing Biom result to " + out, e);
		}
	}

	@SuppressWarnings("unused")
	private static void exportCount(MetaPeptide[] peps, Taxon[] taxons, HashMap<Integer, Taxon> taxonMap,
			String[] expNames, String out) {

		HashSet<Integer> lcaIdSet = new HashSet<Integer>();

		HashMap<Integer, int[]> taxCountMap = new HashMap<Integer, int[]>();
		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();

		for (MetaPeptide peptide : peps) {

			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			lcaIdSet.add(peptide.getLcaId());

			int[] ms2Counts = peptide.getMs2Counts();
			double[] intensities = peptide.getIntensity();

			int[] parentIds = taxonMap.get(lcaId).getMainParentIds();

			for (int parentId : parentIds) {
				if (taxCountMap.containsKey(parentId)) {
					int[] counts = taxCountMap.get(parentId);
					double[] intens = intensityMap.get(parentId);
					for (int i = 0; i < counts.length; i++) {
						counts[i] += ms2Counts[i];
					}
					for (int i = 0; i < intens.length; i++) {
						intens[i] += intensities[i];
					}
				} else {
					int[] counts = new int[ms2Counts.length];
					double[] intens = new double[intensities.length];
					System.arraycopy(ms2Counts, 0, counts, 0, counts.length);
					System.arraycopy(intensities, 0, intens, 0, intens.length);

					taxCountMap.put(parentId, counts);
					intensityMap.put(parentId, intens);
				}
			}
		}

		Date date = new Date();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);

			JSONWriter jw = new JSONWriter(writer);
			jw.object();
			jw.key("id").value("null");
			jw.key("format").value("Biological Observation Matrix 0.9.1-dev");
			jw.key("format_url").value("http://biom-format.org/documentation/format_versions/biom-1.0.html");
			jw.key("type").value("OTU table");
			jw.key("generated_by").value("Metalab 1.0");
			jw.key("date").value(format1.format(date) + "T" + format2.format(date));

			jw.key("rows").array();

			double maxCount = 0;
			int id = 0;
			for (Integer taxId : taxCountMap.keySet()) {
				if (taxonMap.containsKey(taxId)) {

					id++;

					Taxon taxon = taxonMap.get(taxId);
					int[] lineage = taxon.getMainParentIds();
					String[] lineageNames = new String[lineage.length];
					if (taxonMap.containsKey(lineage[0])) {
						lineageNames[0] = rank[0] + taxonMap.get(lineage[0]).getName();
					} else {
						lineageNames[0] = rank[0];
					}

					for (int i = 1; i < rank.length; i++) {
						if (taxonMap.containsKey(lineage[i + 1])) {
							lineageNames[i] = rank[i] + taxonMap.get(lineage[i + 1]).getName();
						} else {
							lineageNames[i] = rank[i];
						}
					}
					HashMap<String, String[]> map = new HashMap<String, String[]>();
					map.put("taxonomy", lineageNames);

					JSONObject jso = new JSONObject(map);

					jw.object();
					jw.key("id").value("GG_OTU_" + id);
					jw.key("metadata").value(jso);
					jw.endObject();

					int[] counts = taxCountMap.get(taxId);
					for (double count : counts) {
						if (count > maxCount) {
							maxCount = count;
						}
					}
				}
			}
			jw.endArray();

			jw.key("columns").array();
			for (String name : expNames) {
				jw.object();
				jw.key("id").value(name);

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("BarcodeSequence", "");
				map.put("LinkerPrimerSequence", "");
				map.put("BODY_SITE", "");
				map.put("Description", "");

				JSONObject jsb = new JSONObject(map);

				jw.key("metadata").value(jsb);
				jw.endObject();
			}

			jw.endArray();

			jw.key("matrix_type").value("dense");
			jw.key("matrix_element_type").value("int");

			int[] shapeNum = new int[] { id, expNames.length };
			JSONArray shape = new JSONArray(shapeNum);
			jw.key("shape").value(shape);

			int[][] values = new int[id][expNames.length];
			if (maxCount > Integer.MAX_VALUE) {

				double k = (maxCount / (double) Integer.MAX_VALUE + 1) * 1000;
				int id2 = 0;
				for (Integer taxId : taxCountMap.keySet()) {
					if (taxonMap.containsKey(taxId)) {
						int[] counts = taxCountMap.get(taxId);
						for (int i = 0; i < expNames.length; i++) {
							values[id2][i] = (int) (counts[i] / k);
						}
						id2++;
					}
				}

			} else {
				int id2 = 0;
				for (Integer taxId : intensityMap.keySet()) {
					if (taxonMap.containsKey(taxId)) {
						int[] counts = taxCountMap.get(taxId);
						for (int i = 0; i < expNames.length; i++) {
							values[id2][i] = (int) counts[i];
						}
						id2++;
					}
				}
			}

			JSONArray data = new JSONArray(values);
			jw.key("data").value(data);

			jw.endObject();

			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing Biom result to " + out, e);
		}
	}
}
