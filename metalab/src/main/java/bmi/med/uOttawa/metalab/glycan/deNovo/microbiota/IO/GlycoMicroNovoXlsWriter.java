package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.NewGlycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.DenovoGlycoPeptide;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.NovoGlycoCompMatch;

public class GlycoMicroNovoXlsWriter {

	private File file;
	private boolean useClassifier;

	private XSSFWorkbook workbook;
	private XSSFCellStyle style;
	private XSSFFont titleFont;
	private XSSFFont contentFont;
	private XSSFSheet[] sheets;
	private int[] currentRowCounts;
	private String[][] titles;

	public GlycoMicroNovoXlsWriter(String file, Glycosyl[] glycosyls, boolean useClassifier) {
		this(new File(file), glycosyls, useClassifier);
	}

	public GlycoMicroNovoXlsWriter(File file, Glycosyl[] glycosyls, boolean useClassifier) {
		this.file = file;
		this.useClassifier = useClassifier;

		this.workbook = new XSSFWorkbook();
		this.style = workbook.createCellStyle();
		this.titleFont = workbook.createFont();
		this.contentFont = workbook.createFont();
		this.titleFont.setBold(true);
		this.contentFont.setBold(false);

		if (useClassifier) {
			this.sheets = new XSSFSheet[5];
			this.sheets[0] = this.workbook.createSheet("Monosaccharide list");
			this.sheets[1] = this.workbook.createSheet("Tier 1 GSMs");
			this.sheets[2] = this.workbook.createSheet("Tier 2 GSMs");
			this.sheets[3] = this.workbook.createSheet("Partially determined GSMs");
			this.sheets[4] = this.workbook.createSheet("Glycopeptides");

			this.currentRowCounts = new int[5];
			this.titles = new String[5][];

		} else {
			this.sheets = new XSSFSheet[4];
			this.sheets[0] = this.workbook.createSheet("Monosaccharide list");
			this.sheets[1] = this.workbook.createSheet("Determined GSMs");
			this.sheets[2] = this.workbook.createSheet("Partially determined GSMs");
			this.sheets[3] = this.workbook.createSheet("Glycopeptides");

			this.currentRowCounts = new int[4];
			this.titles = new String[4][];
		}

		this.addTitle(glycosyls);
	}

	private void addTitle(Glycosyl[] glycosyls) {
		// TODO Auto-generated method stub

		this.style.setFont(titleFont);

		ArrayList<String> list0 = new ArrayList<String>();
		list0.add("Short name");
		list0.add("Full name");
		list0.add("Composition");
		list0.add("Mono mass");
		list0.add("Ave mass");
		this.titles[0] = list0.toArray(new String[list0.size()]);

		ArrayList<String> list1 = new ArrayList<String>();
		list1.add("Scan number");
		list1.add("Precursor mz");
		list1.add("Precursor charge");
		list1.add("Composition");
		list1.add("Glycan mass");
		list1.add("Peptide mass");
		list1.add("Delta mass");
		list1.add("Score");
		list1.add("Q value");

		this.titles[1] = new String[list1.size() + glycosyls.length];
		for (int i = 0; i < list1.size(); i++) {
			titles[1][i] = list1.get(i);
		}
		for (int i = 0; i < glycosyls.length; i++) {
			titles[1][list1.size() + i] = glycosyls[i].getTitle();
		}

		ArrayList<String> list2 = new ArrayList<String>();
		list2.add("Scan number");
		list2.add("Precursor mz");
		list2.add("Precursor charge");
		list2.add("Composition");
		list2.add("Glycan mass");
		list2.add("Unknown part mass");
		list2.add("Score");

		ArrayList<String> list3 = new ArrayList<String>();
		list3.add("Precursor mz");
		list3.add("Precursor charge");
		list3.add("Peptide mass");
		list3.add("Glycan mass");
		list3.add("Composition");
		list3.add("Score");
		list3.add("Scans");

		if (this.useClassifier) {
			this.titles[2] = new String[titles[1].length];
			System.arraycopy(titles[1], 0, titles[2], 0, titles[1].length);

			this.titles[3] = new String[list2.size() + glycosyls.length];
			for (int i = 0; i < list2.size(); i++) {
				titles[3][i] = list2.get(i);
			}

			this.titles[4] = new String[list3.size() + glycosyls.length];
			for (int i = 0; i < list3.size(); i++) {
				titles[4][i] = list3.get(i);
			}

			for (int i = 0; i < glycosyls.length; i++) {
				titles[3][list2.size() + i] = glycosyls[i].getTitle();
				titles[4][list3.size() + i] = glycosyls[i].getTitle();
			}
		} else {
			this.titles[2] = new String[list2.size() + glycosyls.length];
			for (int i = 0; i < list2.size(); i++) {
				titles[2][i] = list2.get(i);
			}

			this.titles[3] = new String[list3.size() + glycosyls.length];
			for (int i = 0; i < list3.size(); i++) {
				titles[3][i] = list3.get(i);
			}
			for (int i = 0; i < glycosyls.length; i++) {
				titles[2][list2.size() + i] = glycosyls[i].getTitle();
				titles[3][list3.size() + i] = glycosyls[i].getTitle();
			}
		}

		for (int i = 0; i < titles.length; i++) {
			XSSFRow row = this.sheets[i].createRow(currentRowCounts[i]++);
			for (int j = 0; j < titles[i].length; j++) {
				XSSFCell cell = row.createCell(j);
				cell.setCellValue(titles[i][j]);
				cell.setCellStyle(style);
			}
		}

		for (int i = 0; i < glycosyls.length; i++) {
			writeMonosaccharide(glycosyls[i]);
		}
	}

	private void writeMonosaccharide(Glycosyl glycosyl) {
		XSSFRow row = this.sheets[0].createRow(currentRowCounts[0]++);
		XSSFCell[] cells = new XSSFCell[titles[0].length];
		int id = 0;
		cells[id] = row.createCell(id);
		cells[id].setCellValue(glycosyl.getTitle());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(glycosyl.getFullname());
		id++;

		cells[id] = row.createCell(id);
		if (glycosyl instanceof NewGlycosyl) {
			NewGlycosyl ng = (NewGlycosyl) glycosyl;
			String[] cs = ng.getCompositionStrings();
			Glycosyl[] gs = ng.getGlycosyls();
			String[] modNames = ng.getModName();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cs.length; i++) {
				sb.append(cs[i]);
				if (gs[i] != null) {
					sb.append("[");
					sb.append(gs[i].getTitle());
					if (modNames[i] != null) {
						sb.append("+").append(modNames[i]);
					}
					sb.append("]");
				}
				sb.append(";");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
				cells[id].setCellValue(sb.toString());
			}
		} else {
			cells[id].setCellValue(glycosyl.getCompositionString());
		}
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(glycosyl.getMonoMass());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(glycosyl.getAvgeMass());
		id++;
	}

	public void write(NovoGlycoCompMatch[] determinedMatches, NovoGlycoCompMatch[] undeterminedMatches,
			HashSet<Integer> usedRank2Scans, DenovoGlycoPeptide[] glycoPeptides) {

		Arrays.sort(determinedMatches, new Comparator<NovoGlycoCompMatch>() {
			@Override
			public int compare(NovoGlycoCompMatch gm1, NovoGlycoCompMatch gm2) {
				// TODO Auto-generated method stub
				if (gm1.getScannum() < gm2.getScannum()) {
					return -1;
				} else if (gm1.getScannum() > gm2.getScannum()) {
					return 1;
				} else {
					if (gm1.getRank() < gm2.getRank()) {
						return -1;
					} else {
						return 1;
					}
				}
			}
		});

		Arrays.sort(undeterminedMatches, new Comparator<NovoGlycoCompMatch>() {
			@Override
			public int compare(NovoGlycoCompMatch gm1, NovoGlycoCompMatch gm2) {
				// TODO Auto-generated method stub
				if (gm1.getScannum() < gm2.getScannum()) {
					return -1;
				} else if (gm1.getScannum() > gm2.getScannum()) {
					return 1;
				} else {
					if (gm1.getRank() < gm2.getRank()) {
						return -1;
					} else {
						return 1;
					}
				}
			}
		});

		if (this.useClassifier) {

			for (int i = 0; i < determinedMatches.length; i++) {

				if (determinedMatches[i].getRank() > 1 && !usedRank2Scans.contains(determinedMatches[i].getScannum()))
					continue;

				if (determinedMatches[i].getClassScore() < 0.5) {
					writeDetermined(determinedMatches[i], 1);
				} else {
					writeDetermined(determinedMatches[i], 2);
				}
			}
			for (int i = 0; i < undeterminedMatches.length; i++) {

				if (undeterminedMatches[i].getRank() > 1)
					continue;

				writeUndetermined(undeterminedMatches[i], 3);
			}
			for (int i = 0; i < glycoPeptides.length; i++) {
				writeGlycoPeptide(glycoPeptides[i], 4);
			}

		} else {
			for (int i = 0; i < determinedMatches.length; i++) {

				if (determinedMatches[i].getRank() > 1 && !usedRank2Scans.contains(determinedMatches[i].getScannum()))
					continue;

				writeDetermined(determinedMatches[i], 1);

			}
			for (int i = 0; i < undeterminedMatches.length; i++) {

				if (undeterminedMatches[i].getRank() > 1)
					continue;

				writeUndetermined(undeterminedMatches[i], 2);
			}
			for (int i = 0; i < glycoPeptides.length; i++) {
				writeGlycoPeptide(glycoPeptides[i], 3);
			}
		}
	}

	private void writeDetermined(NovoGlycoCompMatch match, int sheet) {

		XSSFRow row = this.sheets[sheet].createRow(currentRowCounts[sheet]++);
		XSSFCell[] cells = new XSSFCell[titles[sheet].length];
		int id = 0;
		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getScannum());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getPreMz())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getPreCharge());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getComString());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getGlycanMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getPeptideMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getDeltaMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getScore())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getqValue())));
		id++;

		int[] composition = match.getComposition();
		for (int i = 0; i < composition.length; i++) {
			cells[id] = row.createCell(id);
			cells[id].setCellValue(composition[i]);
			id++;
		}
	}

	private void writeUndetermined(NovoGlycoCompMatch match, int sheet) {

		XSSFRow row = this.sheets[sheet].createRow(currentRowCounts[sheet]++);
		XSSFCell[] cells = new XSSFCell[titles[sheet].length];
		int id = 0;
		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getScannum());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getPreMz())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getPreCharge());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(match.getComString());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getGlycanMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getDeltaMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(match.getScore())));
		id++;

		int[] composition = match.getComposition();
		for (int i = 0; i < composition.length; i++) {
			cells[id] = row.createCell(id);
			cells[id].setCellValue(composition[i]);
			id++;
		}
	}

	private void writeGlycoPeptide(DenovoGlycoPeptide gp, int sheet) {
		XSSFRow row = this.sheets[sheet].createRow(currentRowCounts[sheet]++);
		XSSFCell[] cells = new XSSFCell[titles[sheet].length];
		int id = 0;
		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(gp.getPreMz())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(gp.getPreCharge());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(gp.getPeptideMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(gp.getGlycanMass())));
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(gp.getComString());
		id++;

		cells[id] = row.createCell(id);
		cells[id].setCellValue(Double.parseDouble(FormatTool.getDF4().format(gp.getScore())));
		id++;

		StringBuilder scansb = new StringBuilder();
		int[] scans = gp.getScans();
		for (int i = 0; i < scans.length; i++) {
			scansb.append(scans[i]).append(";");
		}
		if (scansb.length() > 0) {
			scansb.deleteCharAt(scansb.length() - 1);
		}
		cells[id] = row.createCell(id);
		cells[id].setCellValue(scansb.toString());
		id++;

		int[] composition = gp.getComposition();
		for (int i = 0; i < composition.length; i++) {
			cells[id] = row.createCell(id);
			cells[id].setCellValue(composition[i]);
			id++;
		}
	}

	public void close() throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		this.workbook.write(out);
		out.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
