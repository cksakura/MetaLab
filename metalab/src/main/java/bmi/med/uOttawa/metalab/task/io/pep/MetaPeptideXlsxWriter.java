/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPep4Meta;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenPeptide;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPeptide;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemPep4Meta;
import bmi.med.uOttawa.metalab.task.io.MetaAlgorithm;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaPeptideXlsxWriter {

	private File[] pepfile;
	private File taxfile;
	private String searchEngine;
	private int pepCountThres;
	private String[] expNames;

	private MetaPeptide[] peps;
	private Taxon[] taxons;
	private HashMap<Integer, Taxon> taxonMap;

	private XSSFWorkbook pepWorkbook;
	private XSSFCellStyle pepStyle;
	private XSSFFont pepTitleFont;
	private XSSFFont pepContentFont;
	private XSSFSheet pepSheet;
	private int pepCurrentRowCount;
	private String[] pepTitles;

	private XSSFWorkbook taxWorkbook;
	private XSSFCellStyle taxStyle;
	private XSSFFont taxTitleFont;
	private XSSFFont taxContentFont;
	private XSSFSheet[] taxSheets;
	private int[] taxCurrentRowCounts;
	private String[] taxTitle;

	private int currentFileId = 0;
	private int taxAllSheetId = 0;
	private int taxHighSheetId = 1;

	private int maxLine;

	private static DecimalFormat df2 = FormatTool.getDF2();
	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private static final Logger LOGGER = LogManager.getLogger(MetaPeptideXlsxWriter.class);

	public MetaPeptideXlsxWriter(String resultDir, String pepFileName, String taxFileName, String searchType,
			String quanType, String[] expNames, int pepCountThres, MetaPeptide[] peps, Taxon[] taxons) {

		this(new File(resultDir), pepFileName, taxFileName, searchType, quanType, expNames, pepCountThres, peps,
				taxons);
	}

	public MetaPeptideXlsxWriter(File resultDir, String pepFileName, String taxFileName, String searchType,
			String quanType, String[] expNames, int pepCountThres, MetaPeptide[] peps, Taxon[] taxons) {

		if (expNames.length > 200) {
			maxLine = 10000;
		} else if (expNames.length > 100 && expNames.length <= 200) {
			maxLine = 20000;
		} else if (expNames.length > 60 && expNames.length <= 100) {
			maxLine = 30000;
		} else if (expNames.length > 40 && expNames.length <= 60) {
			maxLine = 50000;
		} else {
			maxLine = 100000;
		}

		if (peps.length > maxLine) {
			int fileCount = peps.length / maxLine + 1;
			this.pepfile = new File[fileCount];
			for (int i = 0; i < fileCount; i++) {
				this.pepfile[i] = new File(resultDir, pepFileName + "_" + (i + 1) + ".xlsx");
			}
		} else {
			this.pepfile = new File[] { new File(resultDir, pepFileName + ".xlsx") };
		}
		this.taxfile = new File(resultDir, taxFileName + ".xlsx");

		this.peps = peps;
		this.taxons = taxons;

		this.searchEngine = searchType;
		this.pepCountThres = pepCountThres;
		this.expNames = expNames;

		this.pepWorkbook = new XSSFWorkbook();
		this.pepStyle = pepWorkbook.createCellStyle();
		this.pepTitleFont = pepWorkbook.createFont();
		this.pepContentFont = pepWorkbook.createFont();
		this.pepTitleFont.setBold(true);
		this.pepContentFont.setBold(false);
		this.pepSheet = pepWorkbook.createSheet("peptide list");
		this.pepCurrentRowCount = 0;

		this.taxWorkbook = new XSSFWorkbook();
		this.taxStyle = taxWorkbook.createCellStyle();
		this.taxTitleFont = taxWorkbook.createFont();
		this.taxContentFont = taxWorkbook.createFont();
		this.taxTitleFont.setBold(true);
		this.taxContentFont.setBold(false);
		this.taxSheets = new XSSFSheet[2];
		this.taxSheets[0] = taxWorkbook.createSheet("taxa list all");
		this.taxSheets[1] = taxWorkbook.createSheet("taxa list pep>=3");
		this.taxCurrentRowCounts = new int[2];

		this.addPepTitle();
		this.addTaxonTitle();
	}

	public MetaPeptideXlsxWriter(String resultDir, String searchType, String quanType, String[] expNames,
			int pepCountThres, MetaAlgorithm ma, MetaPeptide[] peps, Taxon[] taxons) {

		this(new File(resultDir), searchType, quanType, expNames, pepCountThres, ma, peps, taxons);
	}

	public MetaPeptideXlsxWriter(File resultDir, String searchType, String quanType, String[] expNames,
			int pepCountThres, MetaAlgorithm ma, MetaPeptide[] peps, Taxon[] taxons) {

		if (expNames.length > 200) {
			maxLine = 10000;
		} else if (expNames.length > 100 && expNames.length <= 200) {
			maxLine = 20000;
		} else if (expNames.length > 60 && expNames.length <= 100) {
			maxLine = 30000;
		} else if (expNames.length > 40 && expNames.length <= 60) {
			maxLine = 50000;
		} else {
			maxLine = 100000;
		}

		if (peps.length > maxLine) {
			int fileCount = peps.length / maxLine + 1;
			this.pepfile = new File[fileCount];
			for (int i = 0; i < fileCount; i++) {
				this.pepfile[i] = new File(resultDir, ma.getName() + "_peptide_" + (i + 1) + ".xlsx");
			}
		} else {
			this.pepfile = new File[] { new File(resultDir, ma.getName() + "_peptide.xlsx") };
		}
		this.taxfile = new File(resultDir, ma.getName() + ".taxonomy.xlsx");

		this.peps = peps;
		this.taxons = taxons;

		this.searchEngine = searchType;
		this.pepCountThres = pepCountThres;
		this.expNames = expNames;

		this.pepWorkbook = new XSSFWorkbook();
		this.pepStyle = pepWorkbook.createCellStyle();
		this.pepTitleFont = pepWorkbook.createFont();
		this.pepContentFont = pepWorkbook.createFont();
		this.pepTitleFont.setBold(true);
		this.pepContentFont.setBold(false);
		this.pepSheet = pepWorkbook.createSheet("peptide list");
		this.pepCurrentRowCount = 0;

		this.taxWorkbook = new XSSFWorkbook();
		this.taxStyle = taxWorkbook.createCellStyle();
		this.taxTitleFont = taxWorkbook.createFont();
		this.taxContentFont = taxWorkbook.createFont();
		this.taxTitleFont.setBold(true);
		this.taxContentFont.setBold(false);
		this.taxSheets = new XSSFSheet[2];
		this.taxSheets[0] = taxWorkbook.createSheet("taxa list all");
		this.taxSheets[1] = taxWorkbook.createSheet("taxa list pep>=3");
		this.taxCurrentRowCounts = new int[2];

		this.addPepTitle();
		this.addTaxonTitle();
	}

	public void setTaxonMap(HashMap<Integer, Taxon> taxonMap) {
		this.taxonMap = taxonMap;
	}

	private void addPepTitle() {

		this.pepStyle.setFont(pepTitleFont);

		ArrayList<String> list0 = new ArrayList<String>();

		if (searchEngine.equals(MetaConstants.maxQuant)) {
			list0.add("Peptide id");
			list0.add("Sequence");
			list0.add("Total MS2 count");
			list0.add("Missed cleavages");
			list0.add("Score");
			list0.add("PEP");
		} else if (searchEngine.equals(MetaConstants.xTandem)) {
			list0.add("Peptide id");
			list0.add("Sequence");
			list0.add("Total MS2 count");
			list0.add("Missed cleavages");
			list0.add("Score");
			list0.add("Expect");
		} else if (searchEngine.equals(MetaConstants.openSearch)) {
			list0.add("Unique sequence id");
			list0.add("Sequence");
			list0.add("Variable mod");
			list0.add("Open mod mass");
			list0.add("Open mod name");
			list0.add("Open mod site");
			list0.add("Best Q-value");
			list0.add("Total MS2 count");
		}

		list0.add("LCA");
		list0.add("Rank");

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			list0.add(mainRanks[i].getName());
		}

		for (String exp : expNames) {
			list0.add(exp);
		}

		XSSFRow row0 = this.pepSheet.createRow(this.pepCurrentRowCount++);
		for (int i = 0; i < list0.size(); i++) {
			XSSFCell cell = row0.createCell(i);
			cell.setCellValue(list0.get(i));
			cell.setCellStyle(pepStyle);
		}

		this.pepTitles = list0.toArray(new String[list0.size()]);
		this.pepStyle.setFont(pepContentFont);
	}

	private void addTaxonTitle() {
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add("Name");
		list1.add("Rank");

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			list1.add(mainRanks[i].getName());
		}

		for (String exp : expNames) {
			list1.add(exp);
		}

		XSSFRow row1 = taxSheets[taxAllSheetId].createRow(taxCurrentRowCounts[taxAllSheetId]++);
		XSSFRow row2 = taxSheets[taxHighSheetId].createRow(taxCurrentRowCounts[taxHighSheetId]++);
		for (int i = 0; i < list1.size(); i++) {
			XSSFCell cell1 = row1.createCell(i);
			cell1.setCellValue(list1.get(i));
			cell1.setCellStyle(taxStyle);

			XSSFCell cell2 = row2.createCell(i);
			cell2.setCellValue(list1.get(i));
			cell2.setCellStyle(taxStyle);
		}

		this.taxTitle = list1.toArray(new String[list1.size()]);
		this.taxStyle.setFont(taxContentFont);
	}

	public void write() {

		if (this.taxonMap == null) {
			taxonMap = new HashMap<Integer, Taxon>();
			taxonMap.put(Taxon.root.getId(), Taxon.root);
			taxonMap.put(Taxon.cellular_organisms.getId(), Taxon.cellular_organisms);
			taxonMap.put(Taxon.unclassified.getId(), Taxon.unclassified);
			taxonMap.put(Taxon.other.getId(), Taxon.other);

			for (int i = 0; i < taxons.length; i++) {
				taxonMap.put(taxons[i].getId(), taxons[i]);
			}
		}

		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();
		HashMap<Integer, Integer> pepCountMap = new HashMap<Integer, Integer>();

		int pepid = 1;
		for (MetaPeptide peptide : peps) {

			int lcaId = peptide.getLcaId();
			if (!this.taxonMap.containsKey(lcaId)) {
				continue;
			}

			try {
				write(peptide, pepid++);
			} catch (Exception e) {
				LOGGER.error("Error in writing peptides to " + pepfile[currentFileId], e);
				System.err.println(
						format.format(new Date()) + "\t" + "Error in writing peptides to " + pepfile[currentFileId]);
			}

			double[] intensities = peptide.getIntensity();
			int[] parentIds = this.taxonMap.get(lcaId).getMainParentIds();

			if (parentIds != null) {
				for (int parentId : parentIds) {
					if (intensityMap.containsKey(parentId)) {
						double[] intens = intensityMap.get(parentId);
						for (int i = 0; i < intens.length; i++) {
							intens[i] += intensities[i];
						}
						pepCountMap.put(parentId, pepCountMap.get(parentId) + 1);
					} else {
						double[] intens = new double[intensities.length];
						System.arraycopy(intensities, 0, intens, 0, intens.length);

						intensityMap.put(parentId, intens);
						pepCountMap.put(parentId, 1);
					}
				}
			}
		}

		for (Integer taxId : intensityMap.keySet()) {
			if (taxonMap.containsKey(taxId) && pepCountMap.containsKey(taxId)) {
				write(taxAllSheetId, taxonMap.get(taxId), intensityMap.get(taxId));
				if (pepCountMap.get(taxId) >= pepCountThres) {
					write(taxHighSheetId, taxonMap.get(taxId), intensityMap.get(taxId));
				}
			}
		}
	}

	private void write(MetaPeptide peptide, int peptideId) {

		if (peptideId / maxLine > this.currentFileId) {
			FileOutputStream pepout;
			try {
				pepout = new FileOutputStream(pepfile[currentFileId]);
				pepWorkbook.write(pepout);
				pepout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in writing peptide result file to " + pepfile[currentFileId], e);
				System.err.println(
						format.format(new Date()) + "\t" + "Error in writing peptides to " + pepfile[currentFileId]);
			}

			currentFileId++;

			this.pepWorkbook = new XSSFWorkbook();
			this.pepStyle = pepWorkbook.createCellStyle();
			this.pepTitleFont = pepWorkbook.createFont();
			this.pepContentFont = pepWorkbook.createFont();
			this.pepTitleFont.setBold(true);
			this.pepContentFont.setBold(false);
			this.pepSheet = pepWorkbook.createSheet("peptide list");
			this.pepCurrentRowCount = 0;

			this.addPepTitle();
		}

		if (searchEngine.equals(MetaConstants.openSearch)) {
			OpenPeptide op = (OpenPeptide) peptide;
			String[] modseqs = op.getModseqs();
			double[] scores = op.getScores();
			double[][] intensities = op.getIntensities();
			int[] ms2Counts = op.getTotalMS2Counts();
			String[] variMods = op.getVariMods();
			double[] opModMass = op.getOpModMass();
			String[] opModName = op.getOpModName();
			String[] opModSite = op.getOpModSite();

			for (int i = 0; i < modseqs.length; i++) {
				XSSFRow row = pepSheet.createRow(this.pepCurrentRowCount++);
				XSSFCell[] cells = new XSSFCell[pepTitles.length];
				int id = 0;
				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptideId);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(op.getSequence());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(variMods[i]);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(opModMass[i]);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(opModName[i]);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(opModSite[i]);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(scores[i]);
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(ms2Counts[i]);
				id++;

				Taxon lca = this.taxonMap.get(peptide.getLcaId());
				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getRank());
				id++;

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
						cells[id] = row.createCell(id);
						id++;
					}
				} else {
					for (int taxId : taxIds) {
						cells[id] = row.createCell(id);
						if (this.taxonMap.containsKey(taxId)) {
							cells[id].setCellValue(this.taxonMap.get(taxId).getName());
						}
						id++;
					}
				}

				for (double intensity : intensities[i]) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(intensity);
					id++;
				}
			}
		} else {

			XSSFRow row = pepSheet.createRow(this.pepCurrentRowCount++);
			XSSFCell[] cells = new XSSFCell[pepTitles.length];
			int id = 0;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(peptideId);
			id++;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(peptide.getSequence());
			id++;

			if (searchEngine.equals(MetaConstants.maxQuant)) {

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getTotalMS2Count());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getMissCleave());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getScore());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(((MaxquantPep4Meta) peptide).getPEP());
				id++;

				Taxon lca = this.taxonMap.get(peptide.getLcaId());
				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getRank());
				id++;

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int i = 0; i < 8; i++) {
						cells[id] = row.createCell(id);
						id++;
					}
				} else {
					for (int taxId : taxIds) {
						cells[id] = row.createCell(id);
						if (this.taxonMap.containsKey(taxId)) {
							cells[id].setCellValue(this.taxonMap.get(taxId).getName());
						}
						id++;
					}
				}

				double[] intensities = peptide.getIntensity();
				for (double intensity : intensities) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(intensity);
					id++;
				}

			} else if (searchEngine.equals(MetaConstants.xTandem)) {

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getTotalMS2Count());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getMissCleave());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(peptide.getScore());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(((XTandemPep4Meta) peptide).getEvalue());
				id++;

				Taxon lca = this.taxonMap.get(peptide.getLcaId());
				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(lca.getRank());
				id++;

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int i = 0; i < 8; i++) {
						cells[id] = row.createCell(id);
						id++;
					}
				} else {
					for (int taxId : taxIds) {
						cells[id] = row.createCell(id);
						if (this.taxonMap.containsKey(taxId)) {
							cells[id].setCellValue(this.taxonMap.get(taxId).getName());
						}
						id++;
					}
				}

				double[] intensities = peptide.getIntensity();
				for (double intensity : intensities) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(intensity);
					id++;
				}
			}
		}
	}

	private void write(int sheetNum, Taxon taxon, double[] intensities) {
		XSSFRow row = this.taxSheets[sheetNum].createRow(this.taxCurrentRowCounts[sheetNum]++);
		XSSFCell[] cells = new XSSFCell[this.taxTitle.length];

		int id = 0;
		cells[id] = row.createCell(id);
		cells[id].setCellValue(taxon.getName());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(taxon.getRank());
		id++;

		int[] mainParentIds = taxon.getMainParentIds();
		if (mainParentIds == null) {
			for (int i = 0; i < 8; i++) {
				cells[id] = row.createCell(id);
				id++;
			}
		} else {
			for (int mainParentId : mainParentIds) {
				cells[id] = row.createCell(id);
				if (this.taxonMap.containsKey(mainParentId)) {
					cells[id].setCellValue(this.taxonMap.get(mainParentId).getName());
				}
				id++;
			}
		}

		for (double intensity : intensities) {
			cells[id] = row.createCell(id);
			cells[id].setCellValue(intensity);
			id++;
		}
	}

	public void close() {

		FileOutputStream pepout;
		try {
			pepout = new FileOutputStream(pepfile[currentFileId]);
			pepWorkbook.write(pepout);
			pepout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide result file to " + pepfile[currentFileId], e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing peptides to " + pepfile[currentFileId]);
		}

		FileOutputStream taxout;
		try {
			taxout = new FileOutputStream(taxfile);
			taxWorkbook.write(taxout);
			taxout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxon result file to " + taxfile, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing peptides to " + pepfile[currentFileId]);
		}
	}
	
	public MetaPeptideXlsxWriter(File resultDir, String searchType, String quanType, String[] expNames,
			int pepCountThres, MetaPeptide[] peps, Taxon[] taxons) {

		this.peps = peps;
		this.taxons = taxons;

		this.searchEngine = searchType;
		this.pepCountThres = pepCountThres;
		this.expNames = expNames;
	}
	
	public void writeCsv(File resultDir, int pepCountThres) {

		if (this.taxonMap == null) {
			taxonMap = new HashMap<Integer, Taxon>();
			taxonMap.put(Taxon.root.getId(), Taxon.root);
			taxonMap.put(Taxon.cellular_organisms.getId(), Taxon.cellular_organisms);
			taxonMap.put(Taxon.unclassified.getId(), Taxon.unclassified);
			taxonMap.put(Taxon.other.getId(), Taxon.other);

			for (int i = 0; i < taxons.length; i++) {
				taxonMap.put(taxons[i].getId(), taxons[i]);
			}
		}

		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();
		HashMap<Integer, Integer> pepCountMap = new HashMap<Integer, Integer>();

		File peptaxon = new File(resultDir, "PepTaxa.csv");
		File taxfileAll = new File(resultDir, "Taxa.all.csv");
		File taxfileHigh = new File(resultDir, "Taxa.refine.csv");

		try {
			this.writePeptideCsv(peptaxon, intensityMap, pepCountMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptides and taxa to " + peptaxon, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptides and taxa to " + peptaxon);
		}

		try {
			this.writeTaxonCsv(taxfileAll, taxfileHigh, intensityMap, pepCountMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptides and taxa to " + taxfileAll, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing peptides and taxa to " + taxfileAll);
		}
	}

	public void writeCsv(File resultDir, MetaAlgorithm ma, int pepCountThres) {

		if (this.taxonMap == null) {
			taxonMap = new HashMap<Integer, Taxon>();
			taxonMap.put(Taxon.root.getId(), Taxon.root);
			taxonMap.put(Taxon.cellular_organisms.getId(), Taxon.cellular_organisms);
			taxonMap.put(Taxon.unclassified.getId(), Taxon.unclassified);
			taxonMap.put(Taxon.other.getId(), Taxon.other);

			for (int i = 0; i < taxons.length; i++) {
				taxonMap.put(taxons[i].getId(), taxons[i]);
			}
		}

		HashMap<Integer, double[]> intensityMap = new HashMap<Integer, double[]>();
		HashMap<Integer, Integer> pepCountMap = new HashMap<Integer, Integer>();

		File peptaxon = new File(resultDir, ma.getName() + ".pepTaxa.csv");
		File taxfileAll = new File(resultDir, ma.getName() + ".taxa.all.csv");
		File taxfileHigh = new File(resultDir, ma.getName() + ".taxa.refine.csv");

		try {
			this.writePeptideCsv(peptaxon, intensityMap, pepCountMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptides and taxa to " + peptaxon, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing peptides and taxa to " + peptaxon);
		}

		try {
			this.writeTaxonCsv(taxfileAll, taxfileHigh, intensityMap, pepCountMap);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptides and taxa to " + taxfileAll, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing peptides and taxa to " + taxfileAll);
		}
	}

	private void writePeptideCsv(File output, HashMap<Integer, double[]> intensityMap,
			HashMap<Integer, Integer> pepCountMap) throws FileNotFoundException {

		PrintWriter writer = new PrintWriter(output);
		StringBuilder title = new StringBuilder();

		if (searchEngine.equals(MetaConstants.maxQuant)) {
			title.append("\"Peptide id\",");
			title.append("\"Sequence\",");
			title.append("\"Total MS2 count\",");
			title.append("\"Missed cleavages\",");
			title.append("\"Score\",");
			title.append("\"PEP\",");
		} else if (searchEngine.equals(MetaConstants.xTandem)) {
			title.append("\"Peptide id\",");
			title.append("\"Sequence\",");
			title.append("\"Total MS2 count\",");
			title.append("\"Missed cleavages\",");
			title.append("\"Score\",");
			title.append("\"Expect\",");
		} else if (searchEngine.equals(MetaConstants.openSearch)) {
			title.append("\"Unique sequence id\",");
			title.append("\"Sequence\",");
			title.append("\"Variable mod\",");
			title.append("\"Open mod mass\",");
			title.append("\"Open mod name\",");
			title.append("\"Open mod site\",");
			title.append("\"Best Q-value\",");
			title.append("\"Total MS2 count\",");
		} else if (searchEngine.equals(MetaConstants.pFind)) {
			title.append("\"Unique sequence id\",");
			title.append("\"Sequence\",");
			title.append("\"Mod sequence\",");
			title.append("\"Missed cleavages\",");
			title.append("\"Total MS2 count\",");
			title.append("\"Score\",");
		} else {
			title.append("\"Peptide id\",");
			title.append("\"Sequence\",");
		}

		title.append("\"LCA\",");
		title.append("\"Rank\",");

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			title.append("\"").append(mainRanks[i].getName()).append("\",");
		}

		for (String exp : expNames) {
			title.append("\"").append(exp).append("\",");
		}
		if (title.length() > 0) {
			title.deleteCharAt(title.length() - 1);
		}

		writer.println(title);

		int pepid = 1;
		for (MetaPeptide peptide : peps) {

			int lcaId = peptide.getLcaId();
			if (!this.taxonMap.containsKey(lcaId)) {
				continue;
			}

			StringBuilder sb = new StringBuilder();

			if (searchEngine.equals(MetaConstants.openSearch)) {

				OpenPeptide op = (OpenPeptide) peptide;
				String[] modseqs = op.getModseqs();
				double[] scores = op.getScores();
				double[][] intensities = op.getIntensities();
				int[] ms2Counts = op.getTotalMS2Counts();
				String[] variMods = op.getVariMods();
				double[] opModMass = op.getOpModMass();
				String[] opModName = op.getOpModName();
				String[] opModSite = op.getOpModSite();

				Taxon lca = this.taxonMap.get(peptide.getLcaId());

				for (int i = 0; i < modseqs.length; i++) {

					sb.append("\"").append(pepid).append("\",");
					sb.append("\"").append(op.getSequence()).append("\",");
					sb.append("\"").append(variMods[i]).append("\",");
					sb.append("\"").append(opModMass[i]).append("\",");
					sb.append("\"").append(opModName[i]).append("\",");
					sb.append("\"").append(opModSite[i]).append("\",");
					sb.append("\"").append(scores[i]).append("\",");
					sb.append("\"").append(ms2Counts[i]).append("\",");
					sb.append("\"").append(lca.getName()).append("\",");
					sb.append("\"").append(lca.getRank()).append("\",");

					int[] taxIds = lca.getMainParentIds();
					if (taxIds == null) {
						for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
							sb.append("\"\",");
						}
					} else {
						for (int taxId : taxIds) {
							if (this.taxonMap.containsKey(taxId)) {
								sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
							} else {
								sb.append("\"\",");
							}
						}
					}

					for (double intensity : intensities[i]) {
						sb.append("\"").append(intensity).append("\",");
					}

					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}

					sb.append("\n");
				}
				writer.print(sb);
				pepid++;

			} else if (searchEngine.equals(MetaConstants.pFind)) {

				PFindPeptide op = (PFindPeptide) peptide;
				int miss = op.getMissCleave();
				String[] modseqs = op.getModseqs();
				double[] scores = op.getScores();
				double[][] intensities = op.getIntensities();
				int[] ms2Counts = op.getTotalMS2Counts();

				Taxon lca = this.taxonMap.get(peptide.getLcaId());

				for (int i = 0; i < modseqs.length; i++) {

					sb.append("\"").append(pepid).append("\",");
					sb.append("\"").append(op.getSequence()).append("\",");
					sb.append("\"").append(modseqs[i]).append("\",");
					sb.append("\"").append(miss).append("\",");
					sb.append("\"").append(ms2Counts[i]).append("\",");
					sb.append("\"").append(df2.format(scores[i])).append("\",");

					sb.append("\"").append(lca.getName()).append("\",");
					sb.append("\"").append(lca.getRank()).append("\",");

					int[] taxIds = lca.getMainParentIds();
					if (taxIds == null) {
						for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
							sb.append("\"\",");
						}
					} else {
						for (int taxId : taxIds) {
							if (this.taxonMap.containsKey(taxId)) {
								sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
							} else {
								sb.append("\"\",");
							}
						}
					}

					for (double intensity : intensities[i]) {
						sb.append("\"").append(intensity).append("\",");
					}

					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}

					sb.append("\n");
				}

				writer.print(sb);

				pepid++;

			} else if (searchEngine.equals(MetaConstants.maxQuant)) {

				sb.append("\"").append(pepid).append("\",");
				sb.append("\"").append(peptide.getSequence()).append("\",");
				sb.append("\"").append(peptide.getTotalMS2Count()).append("\",");
				sb.append("\"").append(peptide.getMissCleave()).append("\",");
				sb.append("\"").append(peptide.getScore()).append("\",");
				sb.append("\"").append(((MaxquantPep4Meta) peptide).getPEP()).append("\",");

				Taxon lca = this.taxonMap.get(peptide.getLcaId());

				sb.append("\"").append(lca.getName()).append("\",");
				sb.append("\"").append(lca.getRank()).append("\",");

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
						sb.append("\"\",");
					}
				} else {
					for (int taxId : taxIds) {
						if (this.taxonMap.containsKey(taxId)) {
							sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
						} else {
							sb.append("\"\",");
						}
					}
				}

				double[] intensities = peptide.getIntensity();
				for (double intensity : intensities) {
					sb.append("\"").append(intensity).append("\",");
				}

				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}

				writer.println(sb);
				pepid++;

			} else if (searchEngine.equals(MetaConstants.xTandem)) {

				sb.append("\"").append(pepid).append("\",");
				sb.append("\"").append(peptide.getSequence()).append("\",");
				sb.append("\"").append(peptide.getTotalMS2Count()).append("\",");
				sb.append("\"").append(peptide.getMissCleave()).append("\",");
				sb.append("\"").append(peptide.getScore()).append("\",");
				sb.append("\"").append(((XTandemPep4Meta) peptide).getEvalue()).append("\",");

				Taxon lca = this.taxonMap.get(peptide.getLcaId());

				sb.append("\"").append(lca.getName()).append("\",");
				sb.append("\"").append(lca.getRank()).append("\",");

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
						sb.append("\"\",");
					}
				} else {
					for (int taxId : taxIds) {
						if (this.taxonMap.containsKey(taxId)) {
							sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
						} else {
							sb.append("\"\",");
						}
					}
				}

				double[] intensities = peptide.getIntensity();
				for (double intensity : intensities) {
					sb.append("\"").append(intensity).append("\",");
				}

				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}

				writer.println(sb);
				pepid++;
			} else {
				sb.append("\"").append(pepid).append("\",");
				sb.append("\"").append(peptide.getSequence()).append("\",");

				Taxon lca = this.taxonMap.get(peptide.getLcaId());

				sb.append("\"").append(lca.getName()).append("\",");
				sb.append("\"").append(lca.getRank()).append("\",");

				int[] taxIds = lca.getMainParentIds();
				if (taxIds == null) {
					for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
						sb.append("\"\",");
					}
				} else {
					for (int taxId : taxIds) {
						if (this.taxonMap.containsKey(taxId)) {
							sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
						} else {
							sb.append("\"\",");
						}
					}
				}

				double[] intensities = peptide.getIntensity();
				for (double intensity : intensities) {
					sb.append("\"").append(intensity).append("\",");
				}

				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}

				writer.println(sb);
				pepid++;
			}

			double[] intensities = peptide.getIntensity();
			int[] parentIds = this.taxonMap.get(lcaId).getMainParentIds();

			if (parentIds != null) {
				for (int parentId : parentIds) {
					if (intensityMap.containsKey(parentId)) {
						double[] intens = intensityMap.get(parentId);
						for (int i = 0; i < intens.length; i++) {
							intens[i] += intensities[i];
						}
						pepCountMap.put(parentId, pepCountMap.get(parentId) + 1);
					} else {
						double[] intens = new double[intensities.length];
						System.arraycopy(intensities, 0, intens, 0, intens.length);

						intensityMap.put(parentId, intens);
						pepCountMap.put(parentId, 1);
					}
				}
			}
		}

		writer.close();
	}

	private void writeTaxonCsv(File outputAll, File outputHigh, HashMap<Integer, double[]> intensityMap,
			HashMap<Integer, Integer> pepCountMap) throws FileNotFoundException {

		PrintWriter writerAll = new PrintWriter(outputAll);
		PrintWriter writerHigh = new PrintWriter(outputHigh);

		StringBuilder title = new StringBuilder();
		title.append("\"Name\"").append(",");
		title.append("\"Rank\"").append(",");

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			title.append("\"").append(mainRanks[i].getName()).append("\",");
		}

		for (String exp : expNames) {
			title.append("\"").append(exp).append("\",");
		}

		if (title.length() > 0) {
			title.deleteCharAt(title.length() - 1);
		}

		writerAll.println(title);
		writerHigh.println(title);

		for (Integer lcaId : intensityMap.keySet()) {
			if (taxonMap.containsKey(lcaId) && pepCountMap.containsKey(lcaId)) {

				Taxon taxon = taxonMap.get(lcaId);

				StringBuilder sb = new StringBuilder();
				sb.append("\"").append(taxon.getName()).append("\",");
				sb.append("\"").append(taxon.getRank()).append("\",");

				int[] taxIds = taxon.getMainParentIds();
				if (taxIds == null) {
					for (int j = 0; j < TaxonomyRanks.getMainRanks().length; j++) {
						sb.append("\"\",");
					}
				} else {
					for (int taxId : taxIds) {
						if (this.taxonMap.containsKey(taxId)) {
							sb.append("\"").append(this.taxonMap.get(taxId).getName()).append("\",");
						} else {
							sb.append("\"\",");
						}
					}
				}

				double[] intensities = intensityMap.get(lcaId);
				for (double intensity : intensities) {
					sb.append("\"").append(intensity).append("\",");
				}

				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}

				writerAll.println(sb);

				if (pepCountMap.get(lcaId) >= pepCountThres) {
					writerHigh.println(sb);
				}
			}
		}

		writerAll.close();
		writerHigh.close();
	}

}
