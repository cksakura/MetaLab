/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

/**
 * @author Kai Cheng
 *
 */
public class MetaProteinXlsxWriter {

	private File file;
	private String[] expNames;
	private String searchType;
	private String quanType;

	private XSSFWorkbook workbook;
	private XSSFCellStyle style;
	private XSSFFont titleFont;
	private XSSFFont contentFont;
	private XSSFSheet[] sheets;
	private int[] currentRowCounts;
	private String[][] titles;

	public MetaProteinXlsxWriter(String file, String[] expNames, String searchType, String quanType) {
		this(new File(file), expNames, searchType, quanType);
	}

	public MetaProteinXlsxWriter(File file, String[] expNames, String searchType, String quanType) {

		this.file = file;
		this.expNames = expNames;
		this.searchType = searchType;
		this.quanType = quanType;

		this.workbook = new XSSFWorkbook();
		this.style = workbook.createCellStyle();
		this.titleFont = workbook.createFont();
		this.contentFont = workbook.createFont();
		this.titleFont.setBold(true);
		this.contentFont.setBold(false);
		this.sheets = new XSSFSheet[1];
		this.sheets[0] = this.workbook.createSheet("Protein function");
		this.currentRowCounts = new int[1];
	}

	public void write(MetaProteinAnno1[] proteins, HashMap<String, HashMap<String, String>> functionMap,
			HashMap<Integer, Taxon> taxaMap) {

		HashMap<String, String> cogMap = functionMap.containsKey("COG") ? functionMap.get("COG") : null;
		HashMap<String, String> cogCateMap = functionMap.containsKey("COG category") ? functionMap.get("COG category")
				: null;
		HashMap<String, String> nogMap = functionMap.containsKey("NOG") ? functionMap.get("NOG") : null;
		HashMap<String, String> nogCateMap = functionMap.containsKey("NOG category") ? functionMap.get("NOG category")
				: null;
		HashMap<String, String> keggMap = functionMap.containsKey("KEGG") ? functionMap.get("KEGG") : null;
		HashMap<String, String> koMap = functionMap.containsKey("KO") ? functionMap.get("KO") : null;
		HashMap<String, String> goMap = functionMap.containsKey("GO") ? functionMap.get("GO") : null;

		this.style.setFont(titleFont);

		this.titles = new String[1][];
		ArrayList<String> list0 = new ArrayList<String>();

		if (searchType.equals(MetaConstants.maxQuant)) {
			list0.add("Group_ID");
			list0.add("Protein_ID");
			list0.add("Name");
			list0.add("Peptide count");
			list0.add("Unique peptide count");
			list0.add("E value");
		} else if (searchType.equals(MetaConstants.openSearch)) {
			list0.add("Name");
			list0.add("Peptide count");
			list0.add("PSM count");
			list0.add("Score");
			list0.add("LCA");
			list0.add("Rank");
		}

		if (cogMap != null && cogCateMap != null) {
			list0.add("COG accession");
			list0.add("COG name");
			list0.add("COG category");
		}

		if (nogMap != null && nogCateMap != null) {
			list0.add("NOG accession");
			list0.add("NOG name");
			list0.add("NOG category");
		}

		if (keggMap != null) {
			list0.add("KEGG accession");
			list0.add("KEGG name");
		}

		if (koMap != null) {
			list0.add("KO accession");
			list0.add("KO name");
		}

		if (goMap != null) {
			list0.add("GOBP accession");
			list0.add("GOBP name");

			list0.add("GOCC accession");
			list0.add("GOCC name");

			list0.add("GOMF accession");
			list0.add("GOMF name");
		}

		if (searchType.equals(MetaConstants.maxQuant) && quanType.equals(MetaConstants.labelFree)) {
			for (String exp : expNames) {
				list0.add(exp.replace("LFQ intensity", "MS2 count"));
			}
		}

		for (String exp : expNames) {
			list0.add(exp);
		}

		XSSFRow row0 = sheets[0].createRow(currentRowCounts[0]++);
		for (int i = 0; i < list0.size(); i++) {
			XSSFCell cell = row0.createCell(i);
			cell.setCellValue(list0.get(i));
			cell.setCellStyle(style);
		}

		this.titles[0] = list0.toArray(new String[list0.size()]);
		this.style.setFont(contentFont);

		for (MetaProteinAnno1 mpa : proteins) {
			MetaProtein protein = mpa.getPro();
			XSSFRow row = this.sheets[0].createRow(currentRowCounts[0]++);
			XSSFCell[] cells = new XSSFCell[titles[0].length];
			Taxon taxon = taxaMap.get(mpa.getTaxId());

			int id = 0;

			if (searchType.equals(MetaConstants.maxQuant)) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getGroupId());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getProteinId());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getUniPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getEvalue());
				id++;
			} else if (searchType.equals(MetaConstants.openSearch)) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPsmCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getScore());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(taxon == null ? "" : taxon.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(taxon == null ? "" : taxon.getRank());
				id++;
			}

			if (cogMap != null && cogCateMap != null) {
				String cog = mpa.getCOG() == null ? "" : mpa.getCOG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cog);
				id++;

				String cogName = cogMap.containsKey(cog) ? cogMap.get(cog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cogName);
				id++;

				String cogCate = cogCateMap.containsKey(cog) ? cogCateMap.get(cog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cogCate);
				id++;
			}

			if (nogMap != null && nogCateMap != null) {
				String nog = mpa.getNOG() == null ? "" : mpa.getNOG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nog);
				id++;

				String nogName = nogMap.containsKey(nog) ? nogMap.get(nog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nogName);
				id++;

				String nogCate = nogCateMap.containsKey(nog) ? nogCateMap.get(nog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nogCate);
				id++;
			}

			if (keggMap != null) {
				String kegg = mpa.getKEGG() == null ? "" : mpa.getKEGG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(kegg);
				id++;

				String keggName = keggMap.containsKey(kegg) ? keggMap.get(kegg) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(keggName);
				id++;
			}

			if (koMap != null) {
				String ko = mpa.getKO() == null ? "" : mpa.getKO();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(ko);
				id++;

				String koName = koMap.containsKey(ko) ? koMap.get(ko) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(koName);
				id++;
			}

			if (goMap != null) {
				String[] gobp = mpa.getGOBP();
				if (gobp != null && gobp.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gobp) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}

				String[] gocc = mpa.getGOCC();
				if (gocc != null && gocc.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gocc) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}

				String[] gomf = mpa.getGOMF();
				if (gomf != null && gomf.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gomf) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}
			}

			if (searchType.equals(MetaConstants.maxQuant) && quanType.equals(MetaConstants.labelFree)) {
				int[] ms2Counts = protein.getMs2Counts();
				for (int ms2Count : ms2Counts) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(ms2Count);
					id++;
				}
			}

			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(intensity);
				id++;
			}
		}
	}

	public void write(MetaProteinAnno1[] proteins, HashMap<String, HashMap<String, String>> functionMap) {

		HashMap<String, String> cogMap = functionMap.containsKey("COG") ? functionMap.get("COG") : null;
		HashMap<String, String> cogCateMap = functionMap.containsKey("COG category") ? functionMap.get("COG category")
				: null;
		HashMap<String, String> nogMap = functionMap.containsKey("NOG") ? functionMap.get("NOG") : null;
		HashMap<String, String> nogCateMap = functionMap.containsKey("NOG category") ? functionMap.get("NOG category")
				: null;
		HashMap<String, String> keggMap = functionMap.containsKey("KEGG") ? functionMap.get("KEGG") : null;
		HashMap<String, String> koMap = functionMap.containsKey("KO") ? functionMap.get("KO") : null;
		HashMap<String, String> goMap = functionMap.containsKey("GO") ? functionMap.get("GO") : null;

		this.style.setFont(titleFont);

		this.titles = new String[1][];
		ArrayList<String> list0 = new ArrayList<String>();

		if (searchType.equals(MetaConstants.maxQuant)) {
			list0.add("Group_ID");
			list0.add("Protein_ID");
			list0.add("Name");
			list0.add("Peptide count");
			list0.add("Unique peptide count");
			list0.add("E value");
		} else if (searchType.equals(MetaConstants.openSearch)) {
			list0.add("Name");
			list0.add("Peptide count");
			list0.add("PSM count");
			list0.add("Score");
			list0.add("LCA");
			list0.add("Rank");
		}

		if (cogMap != null && cogCateMap != null) {
			list0.add("COG accession");
			list0.add("COG name");
			list0.add("COG category");
		}

		if (nogMap != null && nogCateMap != null) {
			list0.add("NOG accession");
			list0.add("NOG name");
			list0.add("NOG category");
		}

		if (keggMap != null) {
			list0.add("KEGG accession");
			list0.add("KEGG name");
		}

		if (koMap != null) {
			list0.add("KO accession");
			list0.add("KO name");
		}

		if (goMap != null) {
			list0.add("GOBP accession");
			list0.add("GOBP name");

			list0.add("GOCC accession");
			list0.add("GOCC name");

			list0.add("GOMF accession");
			list0.add("GOMF name");
		}

		if (searchType.equals(MetaConstants.maxQuant) && quanType.equals(MetaConstants.labelFree)) {
			for (String exp : expNames) {
				list0.add(exp.replace("LFQ intensity", "MS2 count"));
			}
		}

		for (String exp : expNames) {
			list0.add(exp);
		}

		XSSFRow row0 = sheets[0].createRow(currentRowCounts[0]++);
		for (int i = 0; i < list0.size(); i++) {
			XSSFCell cell = row0.createCell(i);
			cell.setCellValue(list0.get(i));
			cell.setCellStyle(style);
		}

		this.titles[0] = list0.toArray(new String[list0.size()]);
		this.style.setFont(contentFont);

		TaxonomyDatabase td = new TaxonomyDatabase();

		for (MetaProteinAnno1 mpa : proteins) {
			MetaProtein protein = mpa.getPro();
			XSSFRow row = this.sheets[0].createRow(currentRowCounts[0]++);
			XSSFCell[] cells = new XSSFCell[titles[0].length];

			int id = 0;

			if (searchType.equals(MetaConstants.maxQuant)) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getGroupId());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getProteinId());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getUniPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getEvalue());
				id++;
			} else if (searchType.equals(MetaConstants.openSearch)) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getName());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPepCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getPsmCount());
				id++;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(protein.getScore());
				id++;

				int taxId = mpa.getTaxId();
				Taxon taxon = td.getTaxonFromId(taxId);

				if (taxon != null) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(taxon.getName());
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(taxon.getRank());
					id++;
				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}
			}

			if (cogMap != null && cogCateMap != null) {
				String cog = mpa.getCOG() == null ? "" : mpa.getCOG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cog);
				id++;

				String cogName = cogMap.containsKey(cog) ? cogMap.get(cog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cogName);
				id++;

				String cogCate = cogCateMap.containsKey(cog) ? cogCateMap.get(cog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(cogCate);
				id++;
			}

			if (nogMap != null && nogCateMap != null) {
				String nog = mpa.getNOG() == null ? "" : mpa.getNOG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nog);
				id++;

				String nogName = nogMap.containsKey(nog) ? nogMap.get(nog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nogName);
				id++;

				String nogCate = nogCateMap.containsKey(nog) ? nogCateMap.get(nog) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(nogCate);
				id++;
			}

			if (keggMap != null) {
				String kegg = mpa.getKEGG() == null ? "" : mpa.getKEGG();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(kegg);
				id++;

				String keggName = keggMap.containsKey(kegg) ? keggMap.get(kegg) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(keggName);
				id++;
			}

			if (koMap != null) {
				String ko = mpa.getKO() == null ? "" : mpa.getKO();
				cells[id] = row.createCell(id);
				cells[id].setCellValue(ko);
				id++;

				String koName = koMap.containsKey(ko) ? koMap.get(ko) : "";
				cells[id] = row.createCell(id);
				cells[id].setCellValue(koName);
				id++;
			}

			if (goMap != null) {
				String[] gobp = mpa.getGOBP();
				if (gobp != null && gobp.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gobp) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}

				String[] gocc = mpa.getGOCC();
				if (gocc != null && gocc.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gocc) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}

				String[] gomf = mpa.getGOMF();
				if (gomf != null && gomf.length > 0) {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (String s : gomf) {
						sb1.append(s).append("; ");
						if (goMap.containsKey(s)) {
							sb2.append(goMap.get(s)).append("; ");
						}
					}

					sb1.deleteCharAt(sb1.length() - 2);
					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb1.toString());
					id++;

					if (sb2.length() > 2) {
						sb2.deleteCharAt(sb2.length() - 2);
					}

					cells[id] = row.createCell(id);
					cells[id].setCellValue(sb2.toString());
					id++;

				} else {
					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue("");
					id++;
				}
			}

			if (searchType.equals(MetaConstants.maxQuant) && quanType.equals(MetaConstants.labelFree)) {
				int[] ms2Counts = protein.getMs2Counts();
				for (int ms2Count : ms2Counts) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(ms2Count);
					id++;
				}
			}

			double[] intensities = protein.getIntensities();
			for (double intensity : intensities) {
				cells[id] = row.createCell(id);
				cells[id].setCellValue(intensity);
				id++;
			}
		}
	}

	public void close() throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		this.workbook.write(out);
		out.close();
	}
}
