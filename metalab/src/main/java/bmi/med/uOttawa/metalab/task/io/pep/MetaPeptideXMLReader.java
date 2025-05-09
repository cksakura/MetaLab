/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.fragpipe.FragpipePeptide;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPep4Meta;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPeptide;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPeptide;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemPep4Meta;
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
			return;
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
		} else if (searchEnigne.equals(MetaConstants.fragpipeIGC) || searchEnigne.equals(MetaConstants.fragpipeMAG)) {
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

		try (PrintWriter writer = new PrintWriter(out)) {
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + out, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to " + out);
		}
	}

	public void exportPeptideTaxaAll(String output) {
		exportPeptideTaxaAll(new File(output));
	}

	public void exportPeptideTaxaAll(File output) {

		try (PrintWriter writer = new PrintWriter(output)) {
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + output.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to "
					+ output.getName());
		}
	}

	public void exportPerseus(String output, String rank, int dataType, boolean proportion) {
		exportPerseus(new File(output), rank, dataType, proportion);
	}

	public void exportPerseus(File output, String rank, int dataType, boolean proportion) {

		try (PrintWriter writer = new PrintWriter(output)) {
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxonomy information to " + output, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptide taxonomy information to "
					+ output.getName());
		}
	}

	public void exportAll(File taxFile, File taxResultFile, String[] expNames, int leastPepCount) {
		MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

//		File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
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
}
