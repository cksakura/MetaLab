/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.fragpipe.FragpipePeptide;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPep4Meta;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPeptide;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPeptide;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemPep4Meta;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipCsvReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPeptide;
import bmi.med.uOttawa.metalab.task.io.MetaAlgorithm;
import bmi.med.uOttawa.metalab.task.io.MetaBiomJsonHandler;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide.XmlPepElementParser;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaPeptideXMLReader {

	private Element root;

	private MetaPeptide[] peptides;
	private Taxon[] taxons;
	private String[] fileNames;
	private String quanType;

	private String searchEnigne;
	private XmlPepElementParser parser;
	
	private static DecimalFormat df4 = FormatTool.getDF4();
	private static DecimalFormat dfe4 = FormatTool.getDFE4();

	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private static final Logger LOGGER = LogManager.getLogger(MetaPeptideXMLReader.class);

	public MetaPeptideXMLReader(String file) {
		this(new File(file));
	}

	public MetaPeptideXMLReader(File file) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading result file " + file.getName(), e);
		}
		this.root = document.getRootElement();
		this.read();
	}

	@SuppressWarnings("unchecked")
	private void read() {
		this.quanType = root.attributeValue("quantitativeMethodId");
		this.searchEnigne = root.attributeValue("searchEngine");
		this.fileNames = root.attributeValue("fileNames").split(";");
		
		for (int i = 0; i < fileNames.length; i++) {
			if (!fileNames[i].startsWith("Intensity")) {
				fileNames[i] = "Intensity " + fileNames[i];
			} else {
				if (!fileNames[i].startsWith("Intensity ") && fileNames[i].length() > "Intensity ".length()) {
					fileNames[i] = "Intensity " + fileNames[i].substring("Intensity ".length());
				}
			}
		}
		
		if (this.quanType.equals("Isobaric labeling")) {
			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].startsWith("Reporter intensity corrected")) {
					fileNames[i] = "Intensity" + fileNames[i].substring("Reporter intensity corrected".length());
				}
			}
		}

		if (this.searchEnigne.equals(MetaConstants.maxQuant)) {
			this.parser = new MaxquantPep4Meta.XmlMQPepElementParser();
		} else if (searchEnigne.equals(MetaConstants.xTandem)) {
			this.parser = new XTandemPep4Meta.XmlTandemPepElementParser();
		} else if (searchEnigne.equals(MetaConstants.openSearch)) {
			this.parser = new OpenPeptide.XmlOpenPepElementParser();
		} else if (searchEnigne.equals(MetaConstants.pFind)) {
			this.parser = new PFindPeptide.XmlPFindPepElementParser();
		} else if (searchEnigne.equals(MetaConstants.flashLFQ)) {
			this.parser = new FlashLfqQuanPeptide.XmlFLPepElementParser();
		} else if (searchEnigne.equals(MetaConstants.diaNN)) {
			this.parser = new FlashLfqQuanPeptide.XmlFLPepElementParser();
		}  else if (searchEnigne.equals(MetaConstants.fragpipeIGC) || searchEnigne.equals(MetaConstants.fragpipeMAG)) {
			this.parser = new FragpipePeptide.XmlFPPepElementParser();
		} else {
			LOGGER.error("Unknown search engine" + searchEnigne);
			System.err.println(format.format(new Date()) + "\t" + "Unknown search engine" + searchEnigne);
			return;
		}

		int peptideCount = Integer.parseInt(root.attributeValue("peptideCount"));
		int taxonCount = Integer.parseInt(root.attributeValue("taxonCount"));

		peptides = new MetaPeptide[peptideCount];
		taxons = new Taxon[taxonCount];

		int peptideId = 0;
		Iterator<Element> pepIt = root.element("Peptides").elementIterator("Peptide");

		while (pepIt.hasNext()) {

			Element ePep = pepIt.next();

			MetaPeptide peptide = this.parser.parse(ePep);

			int lcaId = Integer.parseInt(ePep.attributeValue("lcaId"));
			peptide.setLcaId(lcaId);

			String taxIdString = ePep.attributeValue("taxonIds");
			if (taxIdString != null) {
				String[] cs = taxIdString.split("_");
				int[] taxIds = new int[cs.length];
				for (int i = 0; i < taxIds.length; i++) {
					taxIds[i] = Integer.parseInt(cs[i]);
				}
				peptide.setTaxonIds(taxIds);
			}

			peptides[peptideId++] = peptide;
		}

		Iterator<Element> taxonIt = root.element("Taxons").elementIterator("Taxon");
		int taxonArrayId = 0;
		while (taxonIt.hasNext()) {
			Element eTaxon = taxonIt.next();
			int taxonId = Integer.parseInt(eTaxon.attributeValue("id"));
			String name = eTaxon.attributeValue("name");
			int parentId = Integer.parseInt(eTaxon.attributeValue("parentId"));
			int rankId = Integer.parseInt(eTaxon.attributeValue("rankId"));
			int rootId = Integer.parseInt(eTaxon.attributeValue("rootTypeId"));

			Taxon taxon = new Taxon(taxonId, parentId, rankId, name, RootType.getRootType(rootId));
			String[] mps = eTaxon.attributeValue("mainParentIds").split(",");
			int[] mainParentIds = new int[mps.length];
			for (int i = 0; i < mainParentIds.length; i++) {
				mainParentIds[i] = Integer.parseInt(mps[i]);
			}
			taxon.setMainParentIds(mainParentIds);

			taxons[taxonArrayId++] = taxon;
		}
	}

	public MetaPeptide[] getPeptides() {
		return peptides;
	}

	public Taxon[] getTaxons() {
		return taxons;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public HashMap<Integer, Taxon> getTaxonMap() {
		HashMap<Integer, Taxon> map = new HashMap<Integer, Taxon>();
		for (int i = 0; i < taxons.length; i++) {
			map.put(taxons[i].getId(), taxons[i]);
		}

		return map;
	}

	public void export(String resultDir, MetaAlgorithm ma, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, searchEnigne, quanType, fileNames,
				pepCountThres, ma, getPeptides(), getTaxons());

		writer.write();
		writer.close();
	}

	public void export(File resultDir, MetaAlgorithm ma, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, searchEnigne, quanType, fileNames,
				pepCountThres, ma, getPeptides(), getTaxons());

		writer.write();
		writer.close();
	}

	public void export(String resultDir, String pepFileName, String taxFileName, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, pepFileName, taxFileName, searchEnigne,
				quanType, fileNames, pepCountThres, getPeptides(), getTaxons());

		writer.write();
		writer.close();
	}

	public void export(File resultDir, String pepFileName, String taxFileName, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, pepFileName, taxFileName, searchEnigne,
				quanType, fileNames, pepCountThres, getPeptides(), getTaxons());

		writer.write();
		writer.close();
	}

	public void exportCsv(String resultDir, MetaAlgorithm ma, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, searchEnigne, quanType, fileNames,
				pepCountThres, ma, getPeptides(), getTaxons());
		writer.writeCsv(new File(resultDir), ma, pepCountThres);
	}

	public void exportCsv(File resultDir, MetaAlgorithm ma, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, searchEnigne, quanType, fileNames,
				pepCountThres, ma, getPeptides(), getTaxons());
		writer.writeCsv(resultDir, ma, pepCountThres);
	}
	
	public void exportCsv(File resultDir, int pepCountThres) {
		MetaPeptideXlsxWriter writer = new MetaPeptideXlsxWriter(resultDir, searchEnigne, quanType, fileNames,
				pepCountThres, getPeptides(), getTaxons());
		writer.writeCsv(resultDir, pepCountThres);
	}

	public void exportPeptide(String out) {
		HashMap<Integer, Taxon> taxonMap = this.getTaxonMap();
		taxonMap.put(Taxon.root.getId(), Taxon.root);
		taxonMap.put(Taxon.cellular_organisms.getId(), Taxon.cellular_organisms);
		taxonMap.put(Taxon.unclassified.getId(), Taxon.unclassified);
		taxonMap.put(Taxon.other.getId(), Taxon.other);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + out, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to " + out);
		}
		StringBuilder title = new StringBuilder();
		title.append("Sequence").append("\t");
		title.append("Total MS2 count").append("\t");
		title.append("Missed cleavages").append("\t");
		title.append("Score").append("\t");
		if (searchEnigne.equals(MetaConstants.maxQuant)) {
			title.append("PEP").append("\t");
		} else if (searchEnigne.equals(MetaConstants.xTandem)) {
			title.append("Expect").append("\t");
		}

		title.append("LCA").append("\t");
		title.append("Rank").append("\t");

		title.append("Superkingdom").append("\t");
		title.append("kingdom").append("\t");
		title.append("Phylum").append("\t");
		title.append("Class").append("\t");
		title.append("Order").append("\t");
		title.append("Family").append("\t");
		title.append("Genus").append("\t");
		title.append("Species").append("\t");

		for (String exp : fileNames) {
			title.append(exp).append("\t");
		}

		writer.println(title);

		for (MetaPeptide peptide : getPeptides()) {
			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(peptide.getSequence()).append("\t");
			sb.append(peptide.getTotalMS2Count()).append("\t");
			sb.append(peptide.getMissCleave()).append("\t");
			sb.append(peptide.getScore()).append("\t");

			if (searchEnigne.equals(MetaConstants.maxQuant)) {
				sb.append(((MaxquantPep4Meta) peptide).getPEP()).append("\t");
			} else if (searchEnigne.equals(MetaConstants.xTandem)) {
				sb.append(((XTandemPep4Meta) peptide).getEvalue()).append("\t");
			}

			Taxon lca = peptide.getLcaId() == Taxon.cellular_organisms.getId() ? Taxon.cellular_organisms
					: taxonMap.get(peptide.getLcaId());

			sb.append(lca.getName()).append("\t");
			sb.append(lca.getRank()).append("\t");

			int[] taxIds = lca.getMainParentIds();
			if (taxIds == null) {
				for (int i = 0; i < 8; i++) {
					sb.append("\t");
				}
			} else {
				for (int taxId : taxIds) {
					if (taxonMap.containsKey(taxId)) {
						sb.append(taxonMap.get(taxId).getName()).append("\t");
					} else {
						sb.append("\t");
					}
				}
			}

			double[] intensities = peptide.getIntensity();
			for (double intensity : intensities) {
				sb.append(intensity).append("\t");
			}

			writer.println(sb);
		}
		writer.close();
	}

	public void exportPeptideTaxaAll(String output) {
		exportPeptideTaxaAll(new File(output));
	}

	public void exportPeptideTaxaAll(File output) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + output.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to "
					+ output.getName());
		}

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Peptide id,");
		titlesb.append("Sequence,");
		titlesb.append("Species id,");
		titlesb.append("Taxon identifier,");
		titlesb.append("Superkingdom,");
		titlesb.append("Kingdom,");
		titlesb.append("Phylum,");
		titlesb.append("Class,");
		titlesb.append("Order,");
		titlesb.append("Family,");
		titlesb.append("Genus,");
		titlesb.append("Species");
		writer.println(titlesb.toString());

		HashMap<Integer, Taxon> taxonMap = this.getTaxonMap();
		int id = 1;
		for (MetaPeptide peptide : getPeptides()) {
			String sequence = peptide.getSequence();
			StringBuilder sb = new StringBuilder();
			sb.append(id++).append(",").append(sequence).append(",");
			int[] taxa = peptide.getTaxonIds();
			for (int j = 0; j < taxa.length; j++) {
				if (j > 0) {
					sb.append(",,");
				}
				Taxon taxon = taxa[j] == 131567 ? Taxon.cellular_organisms : taxonMap.get(taxa[j]);

				int taxId = taxon.getId();
				sb.append(j + 1).append(",");
				sb.append(taxId).append(",");

				int[] mainParent = taxon.getMainParentIds();
				if (mainParent != null) {
					for (int tid : mainParent) {
						if (taxonMap.containsKey(tid)) {
							sb.append(taxonMap.get(tid).getName()).append(",");
						} else {
							sb.append(",");
						}
					}
				} else {
					sb.append(Taxon.cellular_organisms.getName()).append(",");
					for (int i = 0; i < 7; i++) {
						sb.append(",");
					}
				}

				sb.append("\n");
			}
			writer.print(sb.toString());
		}
		writer.close();
	}

	public void exportPerseus(String output, String rank, int dataType, boolean proportion) {
		exportPerseus(new File(output), rank, dataType, proportion);
	}

	public void exportPerseus(File output, String rank, int dataType, boolean proportion) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + output, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to "
					+ output.getName());
		}

		StringBuilder title = new StringBuilder();

		title.append(rank).append("\t");
		for (String fileName : fileNames) {
			if (fileName.startsWith("NCD")) {
				fileName = fileName.replace("NCD", "LFD");
			}
			title.append(fileName).append("\t");
		}

		writer.println(title);

		TaxonomyRanks taxRank = TaxonomyRanks.getRankFromName(rank);
		if (taxRank == null) {
			writer.close();
			return;
		}

		int mainRankId = taxRank.getMainId();
		HashMap<Integer, Taxon> taxonMap = getTaxonMap();

		HashMap<Integer, int[]> pepCountMap = new HashMap<Integer, int[]>();
		HashMap<Integer, int[]> ms2CountMap = new HashMap<Integer, int[]>();
		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();

		for (MetaPeptide peptide : peptides) {

			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			int[] parentIds = taxonMap.get(lcaId).getMainParentIds();
			if (!taxonMap.containsKey(parentIds[mainRankId])) {
				continue;
			}

			if (parentIds[0] != 2) {
				continue;
			}

			if (pepCountMap.containsKey(parentIds[mainRankId])) {

				int[] ms2Counts = peptide.getMs2Counts();
				double[] intensities = peptide.getIntensity();

				int[] totalPepCounts = pepCountMap.get(parentIds[mainRankId]);
				int[] totalMS2Counts = ms2CountMap.get(parentIds[mainRankId]);
				double[] totalIntensity = intensityMap.get(parentIds[mainRankId]);

				for (int i = 0; i < totalPepCounts.length; i++) {
					if (ms2Counts[i] > 0) {
						totalPepCounts[i] += 1;
						totalMS2Counts[i] += ms2Counts[i];
					}
					totalIntensity[i] += intensities[i];
				}
			} else {

				int[] ms2Counts = peptide.getMs2Counts();
				double[] intensities = peptide.getIntensity();

				int[] totalPepCounts = new int[ms2Counts.length];
				int[] totalMS2Counts = new int[ms2Counts.length];
				double[] totalIntensity = new double[ms2Counts.length];

				for (int i = 0; i < totalPepCounts.length; i++) {
					if (ms2Counts[i] > 0) {
						totalPepCounts[i] += 1;
						totalMS2Counts[i] += ms2Counts[i];
					}
					totalIntensity[i] += intensities[i];
				}

				pepCountMap.put(parentIds[mainRankId], totalPepCounts);
				ms2CountMap.put(parentIds[mainRankId], totalMS2Counts);
				intensityMap.put(parentIds[mainRankId], totalIntensity);
			}
		}

		for (Integer taxId : pepCountMap.keySet()) {

			int[] totalPepCounts = pepCountMap.get(taxId);
			int[] totalMS2Counts = ms2CountMap.get(taxId);
			double[] totalIntensity = intensityMap.get(taxId);

			int validCount = 0;
			for (int i = 0; i < totalPepCounts.length; i++) {
				if (totalPepCounts[i] > 0) {
					validCount++;
				}
			}

			if (validCount < 8) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(taxonMap.get(taxId).getName()).append("\t");

			if (proportion) {

				if (dataType == 0) {
					double total = MathTool.getTotal(totalPepCounts);
					for (int dd : totalPepCounts) {
						if (dd == 0) {
							sb.append(dd).append("\t");
						} else {
							sb.append(df4.format(dd / total)).append("\t");
						}
					}
				} else if (dataType == 1) {
					double total = MathTool.getTotal(totalMS2Counts);
					for (int dd : totalMS2Counts) {
						if (dd == 0) {
							sb.append(dd).append("\t");
						} else {
							sb.append(df4.format(dd / total)).append("\t");
						}
					}
				} else if (dataType == 2) {
					double total = MathTool.getTotal(totalIntensity);
					for (double dd : totalIntensity) {
						if (dd == 0) {
							sb.append(dd).append("\t");
						} else {
							sb.append(df4.format(dd / total)).append("\t");
						}
					}
				}

			} else {

				if (dataType == 0) {
					for (int dd : totalPepCounts) {
						if (dd == 0) {
							sb.append(dd).append("\t");
						} else {
							sb.append(df4.format(Math.log10(dd))).append("\t");
						}
					}
				} else if (dataType == 1) {
					for (int dd : totalMS2Counts) {
						if (dd == 0) {
							sb.append("NaN").append("\t");
						} else {
							sb.append(df4.format(Math.log10(dd))).append("\t");
						}
					}
				} else if (dataType == 2) {
					for (double dd : totalIntensity) {
						if (dd == 0) {
							sb.append("NaN").append("\t");
						} else {
							sb.append(df4.format(Math.log10(dd))).append("\t");
						}
					}
				}
			}

			writer.println(sb);
		}

		writer.close();
	}

	private void exportMetaboanalyst(String output, String rank, int dataType, boolean proportion) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + output, e);
		}

		StringBuilder title = new StringBuilder();
		StringBuilder title2 = new StringBuilder();

		title.append("Sample,");
		title2.append("Label,");

		for (String fileName : fileNames) {
			if (fileName.startsWith("NCD")) {
				fileName = fileName.replace("NCD", "LFD");
			}
			title.append(fileName).append(",");
			if (fileName.endsWith("D0")) {
				title2.append("D0").append(",");
			} else {
				if (fileName.startsWith("HFD")) {
					title2.append("HFD").append(",");
				} else if (fileName.startsWith("LFD")) {
					title2.append("LFD").append(",");
				}
			}
		}

		writer.println(title);
		writer.println(title2);

		TaxonomyRanks taxRank = TaxonomyRanks.getRankFromName(rank);
		if (taxRank == null) {
			writer.close();
			return;
		}

		int mainRankId = taxRank.getMainId();
		HashMap<Integer, Taxon> taxonMap = getTaxonMap();

		HashMap<Integer, int[]> pepCountMap = new HashMap<Integer, int[]>();
		HashMap<Integer, int[]> ms2CountMap = new HashMap<Integer, int[]>();
		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();

		HashSet<Integer> firmicutes = new HashSet<Integer>();
		HashSet<Integer> bacteroides = new HashSet<Integer>();

		TaxonomyDatabase taxDb = new TaxonomyDatabase();

		for (MetaPeptide peptide : peptides) {

			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			int[] parentIds = taxonMap.get(lcaId).getMainParentIds();
			if (!taxonMap.containsKey(parentIds[mainRankId])) {
				continue;
			}

			Taxon taxon = taxDb.getTaxonFromId(lcaId);

			if (parentIds[0] == 2 || parentIds[0] == 2157) {

			} else if (parentIds[0] == 2759) {
				Taxon kindom = taxDb.getKindom(taxon);
				if (kindom == null) {
					if (taxon.getRankId() != 0) {
						continue;
					}
				} else {
					if (kindom.getId() != 4751) {
						continue;
					}
				}
			} else {
				continue;
			}

			if (pepCountMap.containsKey(parentIds[mainRankId])) {

				int[] ms2Counts = peptide.getMs2Counts();
				double[] intensities = peptide.getIntensity();

				int[] totalPepCounts = pepCountMap.get(parentIds[mainRankId]);
				int[] totalMS2Counts = ms2CountMap.get(parentIds[mainRankId]);
				double[] totalIntensity = intensityMap.get(parentIds[mainRankId]);

				for (int i = 0; i < totalPepCounts.length; i++) {
					if (ms2Counts[i] > 0) {
						totalPepCounts[i] += 1;
						totalMS2Counts[i] += ms2Counts[i];
					}
					totalIntensity[i] += intensities[i];
				}
			} else {

				int[] ms2Counts = peptide.getMs2Counts();
				double[] intensities = peptide.getIntensity();

				int[] totalPepCounts = new int[ms2Counts.length];
				int[] totalMS2Counts = new int[ms2Counts.length];
				double[] totalIntensity = new double[ms2Counts.length];

				for (int i = 0; i < totalPepCounts.length; i++) {
					if (ms2Counts[i] > 0) {
						totalPepCounts[i] += 1;
						totalMS2Counts[i] += ms2Counts[i];
					}
					totalIntensity[i] += intensities[i];
				}

				pepCountMap.put(parentIds[mainRankId], totalPepCounts);
				ms2CountMap.put(parentIds[mainRankId], totalMS2Counts);
				intensityMap.put(parentIds[mainRankId], totalIntensity);
			}
		}

		for (Integer taxId : pepCountMap.keySet()) {

			int[] totalPepCounts = pepCountMap.get(taxId);
			int[] totalMS2Counts = ms2CountMap.get(taxId);
			double[] totalIntensity = intensityMap.get(taxId);

			int validCount = 0;
			boolean pep2 = false;

			for (int i = 0; i < totalPepCounts.length; i++) {
				if (totalPepCounts[i] > 1) {
					validCount++;
				}
				if (totalPepCounts[i] > 1) {
					pep2 = true;
				}
			}

			if (!pep2) {
				continue;
			}

			if (validCount < 8) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			if (firmicutes.contains(taxId)) {
				sb.append("f_");
			} else if (bacteroides.contains(taxId)) {
				sb.append("b_");
			}

			sb.append(taxonMap.get(taxId).getName()).append(",");

			if (proportion) {

				if (dataType == 0) {
					double total = MathTool.getTotal(totalPepCounts);
					for (int dd : totalPepCounts) {
						if (dd == 0) {
							sb.append(dd).append(",");
						} else {
							sb.append(df4.format(dd / total)).append(",");
						}
					}
				} else if (dataType == 1) {
					double total = MathTool.getTotal(totalMS2Counts);
					for (int dd : totalMS2Counts) {
						if (dd == 0) {
							sb.append(dd).append(",");
						} else {
							sb.append(df4.format(dd / total)).append(",");
						}
					}
				} else if (dataType == 2) {
					double total = MathTool.getTotal(totalIntensity);
					for (double dd : totalIntensity) {
						if (dd == 0) {
							sb.append(dd).append(",");
						} else {
							sb.append(df4.format(dd / total)).append(",");
						}
					}
				}

			} else {

				if (dataType == 0) {
					for (int dd : totalPepCounts) {
						if (dd == 0) {
							sb.append("Na").append(",");
						} else {
							sb.append(dd).append(",");
						}
					}
				} else if (dataType == 1) {
					for (int dd : totalMS2Counts) {
						if (dd == 0) {
							sb.append("Na").append(",");
						} else {
							sb.append(dd).append(",");
						}
					}
				} else if (dataType == 2) {
					for (double dd : totalIntensity) {
						if (dd == 0) {
							sb.append("Na").append(",");
						} else {
							sb.append(dd).append(",");
						}
					}
				}
			}

			writer.println(sb);
		}

		writer.close();
	}

	private void exportTxt(String out) {

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + out, e);
		}

		StringBuilder title = new StringBuilder();

		title.append("Taxon");

		for (String fileName : fileNames) {
			if (fileName.startsWith("NCD")) {
				fileName = fileName.replace("NCD", "LFD");
			}
			title.append(",").append(fileName);
		}

		writer.println(title);

		TaxonomyDatabase taxDb = new TaxonomyDatabase();

		double[] rootIntensity = new double[fileNames.length];
		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();
		HashMap<Integer, Taxon> taxonMap = getTaxonMap();

		for (MetaPeptide peptide : peptides) {

			int lcaId = peptide.getLcaId();
			if (!taxonMap.containsKey(lcaId)) {
				continue;
			}

			Taxon taxon = taxDb.getTaxonFromId(lcaId);

			int[] parentIds = taxonMap.get(lcaId).getMainParentIds();

			if (parentIds[0] == 2 || parentIds[0] == 2157) {

			} else if (parentIds[0] == 2759) {
				Taxon kindom = taxDb.getKindom(taxon);
				if (kindom == null) {
					if (taxon.getRankId() != 0) {
						continue;
					}
				} else {
					if (kindom.getId() != 4751) {
						continue;
					}
				}
			} else {
				continue;
			}

			for (int i = 0; i < parentIds.length; i++) {

				if (!taxonMap.containsKey(parentIds[i])) {
					continue;
				}

				if (intensityMap.containsKey(parentIds[i])) {

					double[] intensities = peptide.getIntensity();
					double[] totalIntensity = intensityMap.get(parentIds[i]);

					for (int j = 0; j < totalIntensity.length; j++) {
						totalIntensity[j] += intensities[j];
						rootIntensity[j] += intensities[j];
					}
				} else {

					double[] intensities = peptide.getIntensity();
					double[] totalIntensity = new double[intensities.length];

					for (int j = 0; j < totalIntensity.length; j++) {
						totalIntensity[j] += intensities[j];
						rootIntensity[j] += intensities[j];
					}
					intensityMap.put(parentIds[i], totalIntensity);
				}
			}
		}

		double max = 0;
		double k = 1;
		for (int i = 0; i < rootIntensity.length; i++) {
			if (rootIntensity[i] > max) {
				max = rootIntensity[i];
			}
		}

		if (max > Double.MAX_VALUE) {
			k = (Double.MAX_VALUE - 1) / max;
		}

		StringBuilder rootsb = new StringBuilder();
		rootsb.append("cellular organisms");
		for (int i = 0; i < rootIntensity.length; i++) {
			if (rootIntensity[i] > 0) {
				max = rootIntensity[i];
				rootsb.append(",").append(dfe4.format(rootIntensity[i] * k));
			} else {
				rootsb.append(",").append("0.0");
			}
		}

		writer.println(rootsb);

		for (Integer taxId : intensityMap.keySet()) {

			Taxon taxon = taxonMap.get(taxId);
			double[] totalIntensity = intensityMap.get(taxId);

			int[] parentIds = taxon.getMainParentIds();

			StringBuilder sb = new StringBuilder("cellular organisms");
			for (int i = 0; i < parentIds.length; i++) {
				if (taxonMap.containsKey(parentIds[i])) {
					Taxon taxoni = taxonMap.get(parentIds[i]);
					sb.append("@").append(taxoni.getName());
				} else {
					sb.append("@_");
				}
			}

			for (int i = 0; i < totalIntensity.length; i++) {
				if (totalIntensity[i] > 0) {
					sb.append(",").append(dfe4.format(totalIntensity[i] * k));
				} else {
					sb.append(",").append("0.0");
				}
			}

			writer.println(sb);
		}

		writer.close();
	}

	private static void compareWithUnipeptWeb(String builtin, String unipept) throws DocumentException, IOException {
		MetaPeptideXMLReader reader1 = new MetaPeptideXMLReader(builtin);
		Taxon[] taxons = reader1.getTaxons();
		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (Taxon tax : taxons) {
			taxonMap.put(tax.getId(), tax);
		}

		MetaPeptide[] metapeps = reader1.getPeptides();
		HashMap<String, Taxon> taxonMap1 = new HashMap<String, Taxon>();
		for (MetaPeptide pep : metapeps) {
			Taxon lca = taxonMap.get(pep.getLcaId());
			if (lca == null) {
				continue;
			}
			taxonMap1.put(pep.getSequence(), lca);
		}

		UnipCsvReader reader2 = new UnipCsvReader(unipept);
		HashMap<String, Taxon> taxonMap2 = reader2.getPepTaxonMap();
		/*
		 * HashMap<String, Integer> rankmap2 = new HashMap<String, Integer>();
		 * for(String seq : taxonMap2.keySet()){ Taxon taxon = taxonMap2.get(seq);
		 * rankmap2.put(seq, taxon.getRankId()); }
		 */

		HashSet<String> set = new HashSet<String>();
		set.addAll(taxonMap1.keySet());
		set.addAll(taxonMap2.keySet());

		System.out.println(taxonMap1.size() + "\t" + taxonMap2.size() + "\t" + set.size());

		for (String sequence : set) {
			if (taxonMap1.containsKey(sequence) && taxonMap2.containsKey(sequence)) {
				Taxon taxon1 = taxonMap1.get(sequence);
				Taxon taxon2 = taxonMap2.get(sequence);

				if (taxon1.getRankId() == 4 && taxon2.getRankId() != 4) {
					System.out.println(
							sequence + "\t" + taxon1.getRank() + "\t" + taxon2.getRank() + "\t" + taxon1.getName());
				}
			}
		}
	}

	private static void getDistribution(String builtin) throws DocumentException {
		MetaPeptideXMLReader reader1 = new MetaPeptideXMLReader(builtin);
		Taxon[] taxons = reader1.getTaxons();
		HashMap<Integer, Taxon> taxonMap = new HashMap<Integer, Taxon>();
		for (Taxon tax : taxons) {
			taxonMap.put(tax.getId(), tax);
		}

		HashSet<Integer> species = new HashSet<Integer>();
		MetaPeptide[] metapeps = reader1.getPeptides();
		HashMap<String, Taxon> rankmap1 = new HashMap<String, Taxon>();
		for (MetaPeptide pep : metapeps) {
			Taxon lca = taxonMap.get(pep.getLcaId());
			if (lca == null) {
				continue;
			}

			rankmap1.put(pep.getSequence(), lca);
			if (lca.getRankId() == 23) {
				species.add(lca.getId());
			}
		}
		System.out.println(rankmap1.size());

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (String sequence : rankmap1.keySet()) {
			Taxon lca = rankmap1.get(sequence);
			int rankId = lca.getRankId();
			if (map.containsKey(rankId)) {
				map.put(rankId, map.get(rankId) + 1);
			} else {
				map.put(rankId, 1);
			}
		}

		for (Integer rankid : map.keySet()) {
			System.out.println("rank\t" + rankid + "\t" + map.get(rankid));
		}
		System.out.println("species\t" + species.size());
	}

	private static void compare(String s1, String s2) throws DocumentException {
		MetaPeptideXMLReader reader1 = new MetaPeptideXMLReader(s1);
		HashMap<String, Integer> m1 = new HashMap<String, Integer>();
		for (MetaPeptide mp : reader1.peptides) {
			m1.put(mp.getSequence(), mp.getLcaId());
		}

		MetaPeptideXMLReader reader2 = new MetaPeptideXMLReader(s2);
		HashMap<String, Integer> m2 = new HashMap<String, Integer>();
		for (MetaPeptide mp : reader2.peptides) {
			m2.put(mp.getSequence(), mp.getLcaId());
		}

		System.out.println(m1.size() + "\t" + m2.size());

		HashSet<String> set = new HashSet<String>();
		set.addAll(m1.keySet());
		set.addAll(m2.keySet());

		int diff = 0;
		for (String key : set) {
			if (m1.containsKey(key) && m2.containsKey(key)) {
				int i1 = m1.get(key);
				int i2 = m2.get(key);
				if (i1 != i2) {
					System.out.println(key + "\t" + i1 + "\t" + i2);
				}
			}
		}

		System.out.println(diff);
	}

	private static void compareWithUnipept(String s1, String s2) throws DocumentException, IOException {
		MetaPeptideXMLReader reader1 = new MetaPeptideXMLReader(s1);
		HashMap<String, Integer> m1 = new HashMap<String, Integer>();
		for (MetaPeptide mp : reader1.peptides) {
			m1.put(mp.getSequence(), mp.getLcaId());
		}

		BufferedReader reader2 = new BufferedReader(new FileReader(s2));
		HashMap<String, Integer> m2 = new HashMap<String, Integer>();
		String line = reader2.readLine();
		while ((line = reader2.readLine()) != null) {
			String[] cs = line.split(",");
			m2.put(cs[0], 0);
		}

		reader2.close();

		HashSet<String> set = new HashSet<String>();
		set.addAll(m1.keySet());
		set.addAll(m2.keySet());

		System.out.println(
				m1.size() + "\t" + m2.size() + "\t" + set.size() + "\t" + (m1.size() + m2.size() - set.size()));

		int diff = 0;
		for (String key : set) {
			if (m1.containsKey(key) && m2.containsKey(key)) {
				int i1 = m1.get(key);
				int i2 = m2.get(key);
				if (i1 != i2) {
					diff++;
					// System.out.println(key + "\t" + i1 + "\t" + i2);
				}
			}
		}

		System.out.println(diff);
	}

	private static void convert2Log(String in, String out) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = reader.readLine();
		String[] cs = line.split("\t");
		StringBuilder sb = new StringBuilder();
		sb.append(cs[0]).append("\t");
		for (int i = 1; i < cs.length; i++) {
			sb.append(cs[i].substring(cs[i].lastIndexOf(" ") + 1)).append("\t");
		}

		writer.println(sb);

		while ((line = reader.readLine()) != null) {
			sb = new StringBuilder();
			boolean validate = false;
			cs = line.split("\t");
			sb.append(cs[0]).append("\t");
			for (int i = 1; i < cs.length; i++) {
				if (cs[i].equals("0")) {
					sb.append("0").append("\t");
				} else {
					double inten = Double.parseDouble(cs[i]);
					double log = Math.log10(inten);
					sb.append(df4.format(log)).append("\t");
					validate = true;
				}
			}
			if (validate) {
				writer.println(sb);
			}
		}
		reader.close();
		writer.close();
	}

	private void getPepCount() {
		StringBuilder title = new StringBuilder();
		title.append("species").append("\t");
		for (int i = 0; i < fileNames.length; i++) {
			title.append(fileNames[i]).append("\t");
		}
		System.out.println(title);
		HashMap<String, int[]> pepcountmap = new HashMap<String, int[]>();
		HashMap<Integer, Taxon> taxonmap = this.getTaxonMap();
		for (MetaPeptide peptide : peptides) {
			int lcaid = peptide.getLcaId();
			int[] ms2count = peptide.getMs2Counts();
			if (taxonmap.containsKey(lcaid)) {
				int[] taxa = taxonmap.get(lcaid).getMainParentIds();
				if (taxa[5] == 1730 && taxa[6] != -1) {
					String species = taxonmap.get(taxa[6]).getName();
					if (pepcountmap.containsKey(species)) {
						int[] totalPepCount = pepcountmap.get(species);
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i]++;
							}
						}
						pepcountmap.put(species, totalPepCount);
					} else {
						int[] totalPepCount = new int[ms2count.length];
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i] = 1;
							}
						}
						pepcountmap.put(species, totalPepCount);
					}
				}
			}
		}

		String[] phylums = pepcountmap.keySet().toArray(new String[pepcountmap.size()]);
		Arrays.sort(phylums);

		for (String name : phylums) {
			StringBuilder sb = new StringBuilder();
			sb.append(name).append("\t");
			int[] count = pepcountmap.get(name);
			for (int i = 0; i < count.length; i++) {
				sb.append(count[i]).append("\t");
			}
			System.out.println(sb);
		}
	}

	private void getPepCount(int column) throws IOException {

		TaxonomyDatabase taxDb = new TaxonomyDatabase();

		System.out.println(fileNames[column]);

		HashMap<String, int[]> pepcountmap = new HashMap<String, int[]>();
		HashMap<String, int[]> spcountmap = new HashMap<String, int[]>();
		HashMap<String, double[]> intenmap = new HashMap<String, double[]>();

		HashMap<Integer, Taxon> taxonmap = this.getTaxonMap();
		for (MetaPeptide peptide : peptides) {
			int lcaid = peptide.getLcaId();
			Taxon taxon = taxDb.getTaxonFromId(lcaid);
			int[] linage = taxDb.getMainParentTaxonIds(taxon);
			int root = linage[0];

			if (root == 2 || root == 2157) {

			} else if (root == 2759) {
				Taxon kindom = taxDb.getKindom(taxon);
				if (kindom == null) {
					if (taxon.getRankId() != 0) {
						continue;
					}
				} else {
					if (kindom.getId() != 4751) {
						continue;
					}
				}
			} else {
				continue;
			}

			int[] ms2count = peptide.getMs2Counts();
			double[] intensity = peptide.getIntensity();
			if (taxonmap.containsKey(lcaid)) {
				int[] taxa = taxonmap.get(lcaid).getMainParentIds();
				if (taxa[6] != -1) {
					String species = taxonmap.get(taxa[6]).getName();
					if (pepcountmap.containsKey(species)) {
						int[] totalPepCount = pepcountmap.get(species);
						int[] totalMsCount = spcountmap.get(species);
						double[] totalIntensity = intenmap.get(species);
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i]++;
								totalMsCount[i] += ms2count[i];
								totalIntensity[i] += intensity[i];
							}
						}
						pepcountmap.put(species, totalPepCount);
						spcountmap.put(species, totalMsCount);
						intenmap.put(species, totalIntensity);
					} else {
						int[] totalPepCount = new int[ms2count.length];
						int[] totalMsCount = new int[ms2count.length];
						double[] totalIntensity = new double[ms2count.length];
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i] = 1;
								totalMsCount[i] += ms2count[i];
								totalIntensity[i] += intensity[i];
							}
						}
						pepcountmap.put(species, totalPepCount);
						spcountmap.put(species, totalMsCount);
						intenmap.put(species, totalIntensity);
					}
				}
			}
		}

		String[] species = pepcountmap.keySet().toArray(new String[pepcountmap.size()]);
		Arrays.sort(species);

		int[] number = new int[7];
		for (String name : species) {
			int[] pepcount = pepcountmap.get(name);
			int[] mscount = spcountmap.get(name);
			double[] intensity = intenmap.get(name);

			if (pepcount[column] >= 6) {
				number[6]++;
			} else {
				number[pepcount[column]]++;
			}

			if (pepcount[column] > 0) {
				System.out.println(name + "\t" + pepcount[column] + "\t" + mscount[column] + "\t" + intensity[column]);
			}
		}

		System.out.println(Arrays.toString(number) + "\t" + (species.length - number[0]));
	}

	private void rankDistribution() throws IOException {
		TaxonomyDatabase taxDb = new TaxonomyDatabase();
		int[] ranks = new int[] { 0, 4, 7, 11, 16, 20, 23 };
		int[] counts = new int[ranks.length];
		HashMap<Integer, Taxon> taxonmap = this.getTaxonMap();
		for (MetaPeptide peptide : peptides) {
			int lcaid = peptide.getLcaId();
			if (taxonmap.containsKey(lcaid)) {
				Taxon taxon = taxonmap.get(lcaid);
				int[] linage = taxDb.getMainParentTaxonIds(taxon);
				int root = linage[0];

				/*
				 * if (root == 2 || root == 2157) {
				 * 
				 * } else if (root == 2759) { Taxon kindom = taxDb.getKindom(taxon); if (kindom
				 * == null) { if (taxon.getRankId() != 0) { continue; } } else { if
				 * (kindom.getId() != 4751) { continue; } } } else { continue; }
				 */

				int rankId = taxon.getRankId();
				for (int i = 0; i < ranks.length; i++) {
					counts[i]++;
					if (rankId == ranks[i]) {
						break;
					}
				}
			}
		}

		for (int i = 0; i < counts.length; i++) {
			System.out.print(counts[i] + "\t");
		}

		int total = peptides.length;
		System.out.println(total);

		for (int i = 0; i < counts.length; i++) {
			System.out.print((double) counts[i] / (double) total + "\t");
		}
	}

	private void rankStatistics(String rank) {
		TaxonomyRanks taxRank = TaxonomyRanks.getRankFromName(rank);
		if (taxRank == null) {
			return;
		}

		for (int i = 0; i < fileNames.length; i++) {
			System.out.print("\t" + fileNames[i]);
		}
		System.out.println();

		int mainRankid = taxRank.getMainId();

		HashMap<String, int[]> pepcountmap = new HashMap<String, int[]>();
		HashMap<String, int[]> spcountmap = new HashMap<String, int[]>();
		HashMap<String, double[]> intenmap = new HashMap<String, double[]>();

		HashMap<Integer, Taxon> taxonmap = this.getTaxonMap();
		for (MetaPeptide peptide : peptides) {
			int lcaid = peptide.getLcaId();
			int[] ms2count = peptide.getMs2Counts();
			double[] intensity = peptide.getIntensity();
			if (taxonmap.containsKey(lcaid)) {
				int[] taxa = taxonmap.get(lcaid).getMainParentIds();
				if (taxa[mainRankid] != -1) {
					String species = taxonmap.get(taxa[mainRankid]).getName();
					if (pepcountmap.containsKey(species)) {
						int[] totalPepCount = pepcountmap.get(species);
						int[] totalMsCount = spcountmap.get(species);
						double[] totalIntensity = intenmap.get(species);
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i]++;
								totalMsCount[i] += ms2count[i];
								totalIntensity[i] += intensity[i];
							}
						}
						pepcountmap.put(species, totalPepCount);
						spcountmap.put(species, totalMsCount);
						intenmap.put(species, totalIntensity);
					} else {
						int[] totalPepCount = new int[ms2count.length];
						int[] totalMsCount = new int[ms2count.length];
						double[] totalIntensity = new double[ms2count.length];
						for (int i = 0; i < totalPepCount.length; i++) {
							if (ms2count[i] > 0) {
								totalPepCount[i] = 1;
								totalMsCount[i] += ms2count[i];
								totalIntensity[i] += intensity[i];
							}
						}
						pepcountmap.put(species, totalPepCount);
						spcountmap.put(species, totalMsCount);
						intenmap.put(species, totalIntensity);
					}
				}
			}
		}

		String[] species = pepcountmap.keySet().toArray(new String[pepcountmap.size()]);
		Arrays.sort(species);

		int[] total = new int[fileNames.length];
		int[] pep2 = new int[fileNames.length];
		double[] ratio = new double[fileNames.length];

		for (String name : species) {

			int[] pepcount = pepcountmap.get(name);
			int[] mscount = spcountmap.get(name);
			double[] intensity = intenmap.get(name);

			if (name.equals("Firmicutes") || name.equals("Bacteroidetes")) {
				StringBuilder sb1 = new StringBuilder(name);
				StringBuilder sb2 = new StringBuilder(name);
				StringBuilder sb3 = new StringBuilder(name);
				for (int i = 0; i < pepcount.length; i++) {
					sb1.append("\t").append(pepcount[i]);
					sb2.append("\t").append(mscount[i]);
					sb3.append("\t").append(intensity[i]);
				}
				System.out.println(sb1);
				System.out.println(sb2);
				System.out.println(sb3);
			}
		}

		for (int i = 0; i < pep2.length; i++) {
			System.out.print(pep2[i] + "\t");
		}
		System.out.println();

		for (int i = 0; i < total.length; i++) {
			System.out.print(total[i] + "\t");
		}
		System.out.println();

		for (int i = 0; i < total.length; i++) {
			ratio[i] = (double) pep2[i] / (double) total[i];
			System.out.print(ratio[i] + "\t");
		}
		System.out.println();
		System.out.println(MathTool.getAve(ratio));
	}
	
	public void exportAll(File taxFile, File taxResultFile, String[] expNames, int leastPepCount) {
		MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

		File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
		reader.exportCsv(taxFile, MetaAlgorithm.Builtin, leastPepCount);

		MetaPeptide[] peps = reader.getPeptides();
		Taxon[] taxons = reader.getTaxons();

		File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
		if (!megan.exists() || megan.length() == 0) {
			MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
		}

		File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
		if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
			reader.exportPeptideTaxaAll(allPepTaxa);
		}

		File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
		if (!iMetaLab.exists() || iMetaLab.length() == 0) {
			MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
		}

	}
	
	public static void main(String[] args) {
		String taxFile = "Z:\\Kai\\20230526_big_maxquant\\maxquant_txt\\taxonomy_analysis";
		MetaPeptideXMLReader reader = new MetaPeptideXMLReader("Z:\\Kai\\20230526_big_maxquant\\maxquant_txt\\taxonomy_analysis"
				+ "\\BuiltIn.taxonomy.xml");

		reader.exportCsv(taxFile, MetaAlgorithm.Builtin, 3);

		MetaPeptide[] peps = reader.getPeptides();
		Taxon[] taxons = reader.getTaxons();
		String[] expNames = reader.getFileNames();

		File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
		if (!megan.exists() || megan.length() == 0) {
			MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
		}

		File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
		if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
			reader.exportPeptideTaxaAll(allPepTaxa);
		}

		File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
		if (!iMetaLab.exists() || iMetaLab.length() == 0) {
			MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
		}
	}

}
