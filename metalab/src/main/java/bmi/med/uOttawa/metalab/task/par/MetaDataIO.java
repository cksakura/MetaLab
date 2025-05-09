package bmi.med.uOttawa.metalab.task.par;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MetaDataIO {

	public static void exportBlank(String output) throws IOException {
		exportBlank(new File(output));
	}

	public static void exportBlank(File output) throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		style.getFont().setBold(true);
		XSSFSheet sheet = workbook.createSheet("metadata");
		XSSFRow titleRow = sheet.createRow(0);

		String[] title = new String[4];
		title[0] = "File path";
		title[1] = "Experiment name";
		title[2] = "Fraction";
		title[3] = "Replicate";

		for (int i = 0; i < title.length; i++) {
			XSSFCell cell = titleRow.createCell(i);
			cell.setCellValue(title[i]);
			cell.setCellStyle(style);
		}

		FileOutputStream out = new FileOutputStream(output);
		workbook.write(out);
		out.close();
		workbook.close();
	}

	public static MetaData readRawExpNameTxt(String in) throws IOException {
		return readMetaFileTxt(new File(in));
	}

	public static MetaData readRawExpNameTxt(File in) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));

		String line = reader.readLine();
		String[] title = line.split("[,\t]");

		int fileId = -1;
		int expNameId = -1;
		int fractionId = -1;
		int replicateId = -1;

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("File path")) {
				fileId = i;
			} else if (title[i].equals("Experiment name")) {
				expNameId = i;
			} else if (title[i].equals("Fraction")) {
				fractionId = i;
			} else if (title[i].equals("Replicate")) {
				replicateId = i;
			}
		}

		ArrayList<String> fileList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();
		ArrayList<Integer> fractionList = new ArrayList<Integer>();
		ArrayList<Integer> replicateList = new ArrayList<Integer>();

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("[,\t]");

			String path = "";
			String expName = "";
			int fraction = 0;
			int replicate = 0;

			if (fileId >= 0 && fileId < cs.length) {
				path = cs[fileId];
				if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
					path = path.substring(1, path.length() - 1);
				}
			} else {
				break;
			}

			if (expNameId >= 0 && expNameId < cs.length) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			} else {
				File filei = new File(path);
				expName = filei.getName();
				expName = expName.substring(0, expName.lastIndexOf("."));
			}

			if (fractionId >= 0 && fractionId < cs.length) {
				fraction = NumberUtils.toInt(cs[fractionId], 0);
			}

			if (replicateId >= 0 && replicateId < cs.length) {
				replicate = NumberUtils.toInt(cs[replicateId], 0);
			}

			fileList.add(path);
			expNameList.add(expName);
			fractionList.add(fraction);
			replicateList.add(replicate);
		}
		reader.close();

		String[] rawFiles = fileList.toArray(new String[fileList.size()]);
		String[] expNames = expNameList.toArray(new String[expNameList.size()]);
		int[] fractions = new int[fractionList.size()];
		int[] replicates = new int[replicateList.size()];

		for (int i = 0; i < fractions.length; i++) {
			fractions[i] = fractionList.get(i);
			replicates[i] = replicateList.get(i);
		}

		MetaData md = new MetaData(rawFiles, expNames, fractions, replicates);
		return md;
	}

	public static MetaData readRawExpNameXlsx(String in) throws IOException {
		return readRawExpNameXlsx(new File(in));
	}

	public static MetaData readRawExpNameXlsx(File in) throws IOException {

		int fileId = -1;
		int expNameId = -1;
		int fractionId = -1;
		int replicateId = -1;

		ArrayList<String> fileList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();
		ArrayList<Integer> fractionList = new ArrayList<Integer>();
		ArrayList<Integer> replicateList = new ArrayList<Integer>();

		InputStream inp = new FileInputStream(in);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();

		int headId = 0;

		if (rowIterator.hasNext()) {
			Row headerRow = rowIterator.next();
			Iterator<Cell> headCellIt = headerRow.cellIterator();

			while (headCellIt.hasNext()) {
				Cell cell = headCellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					String name = cell.getRichStringCellValue().getString();
					if (name.trim().length() == 0) {
						break;
					}

					if (name.equals("File path")) {
						fileId = headId;
					} else if (name.equals("Experiment name")) {
						expNameId = headId;
					} else if (name.equals("Fraction")) {
						fractionId = headId;
					} else if (name.equals("Replicate")) {
						replicateId = headId;
					}
				}
				headId++;
			}
		}

		while (rowIterator.hasNext()) {
			Row r = rowIterator.next();
			if (r == null) {
				break;
			}

			ArrayList<String> celllist = new ArrayList<String>();
			Iterator<Cell> cellIt = r.cellIterator();

			while (cellIt.hasNext()) {
				Cell cell = cellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					celllist.add(cell.getRichStringCellValue().getString());
				} else if (cell.getCellType() == CellType.NUMERIC) {
					celllist.add(String.valueOf(cell.getNumericCellValue()));
				}

				if (celllist.size() == headId) {
					break;
				}
			}
			String[] cs = celllist.toArray(new String[celllist.size()]);

			String path = "";
			String expName = "";
			int fraction = 0;
			int replicate = 0;

			if (fileId >= 0 && fileId < cs.length) {
				path = cs[fileId];
				if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
					path = path.substring(1, path.length() - 1);
				}
			} else {
				break;
			}

			if (expNameId >= 0 && expNameId < cs.length) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			} else {
				File filei = new File(path);
				expName = filei.getName();
				expName = expName.substring(0, expName.lastIndexOf("."));
			}

			if (fractionId >= 0 && fractionId < cs.length) {
				fraction = (int) NumberUtils.toDouble(cs[fractionId], 0);
			}

			if (replicateId >= 0 && replicateId < cs.length) {
				replicate = (int) NumberUtils.toDouble(cs[replicateId], 0);
			}

			fileList.add(path);
			expNameList.add(expName);
			fractionList.add(fraction);
			replicateList.add(replicate);
		}

		String[] rawFiles = fileList.toArray(new String[fileList.size()]);
		String[] expNames = expNameList.toArray(new String[expNameList.size()]);
		int[] fractions = new int[fractionList.size()];
		int[] replicates = new int[replicateList.size()];

		for (int i = 0; i < fractions.length; i++) {
			fractions[i] = fractionList.get(i);
			replicates[i] = replicateList.get(i);
		}

		MetaData md = new MetaData(rawFiles, expNames, fractions, replicates);
		return md;
	}

	/**
	 * Txt format is not used now
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static MetaData readMetaFileTxt(String in) throws IOException {
		return readMetaFileTxt(new File(in));
	}

	private static MetaData readMetaFileTxt(File in) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));

		String line = reader.readLine();
		String[] title = line.split("[,\t]");

		int fileId = -1;
		int expNameId = -1;

		for (int i = 0; i < title.length; i++) {
			if (title[i].trim().length() == 0) {
				break;
			}

			if (title[i].equals("File path")) {
				fileId = i;
			} else if (title[i].equals("Experiment name")) {
				expNameId = i;
			}
		}

		ArrayList<String> fileList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("[,\t]");

			String path = "";
			String expName = "";

			if (fileId != -1) {
				path = cs[fileId];
				if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
					path = path.substring(1, path.length() - 1);
				}
			} else {
				break;
			}

			if (expNameId != -1) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			} else {
				File filei = new File(path);
				expName = filei.getName();
				expName = expName.substring(0, expName.lastIndexOf("."));
			}

			fileList.add(path);
			expNameList.add(expName);
		}
		reader.close();

		String[] rawFiles = fileList.toArray(new String[fileList.size()]);
		String[] expNames = expNameList.toArray(new String[expNameList.size()]);

		MetaData md = new MetaData(rawFiles, expNames);
		return md;
	}

	@SuppressWarnings("unused")
	private static void readMetaFileXlsx(String in) throws IOException {
		readMetaFileXlsx(new File(in));
	}

	private static MetaData readMetaFileXlsx(File in) throws IOException {

		int fileId = -1;
		int expNameId = -1;

		ArrayList<String> fileList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();

		InputStream inp = new FileInputStream(in);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();

		int headId = 0;

		if (rowIterator.hasNext()) {
			Row headerRow = rowIterator.next();
			Iterator<Cell> headCellIt = headerRow.cellIterator();

			while (headCellIt.hasNext()) {
				Cell cell = headCellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					String name = cell.getRichStringCellValue().getString();
					if (name.trim().length() == 0) {
						break;
					}

					if (name.equals("File path")) {
						fileId = headId;
					} else if (name.equals("Experiment name")) {
						expNameId = headId;
					}
				}
				headId++;
			}
		}

		while (rowIterator.hasNext()) {
			Row r = rowIterator.next();
			if (r == null) {
				break;
			}

			ArrayList<String> celllist = new ArrayList<String>();
			Iterator<Cell> cellIt = r.cellIterator();

			while (cellIt.hasNext()) {
				Cell cell = cellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					celllist.add(cell.getRichStringCellValue().getString());
				}

				if (celllist.size() == headId) {
					break;
				}
			}
			String[] cs = celllist.toArray(new String[celllist.size()]);

			String path = "";
			String expName = "";

			if (fileId != -1) {
				path = cs[fileId];
				if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
					path = path.substring(1, path.length() - 1);
				}
			} else {
				break;
			}

			if (expNameId != -1) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			} else {
				File filei = new File(path);
				expName = filei.getName();
				expName = expName.substring(0, expName.lastIndexOf("."));
			}

			fileList.add(path);
			expNameList.add(expName);
		}

		String[] rawFiles = fileList.toArray(new String[fileList.size()]);
		String[] expNames = expNameList.toArray(new String[expNameList.size()]);

		MetaData md = new MetaData(rawFiles, expNames);
		return md;
	}

	@SuppressWarnings("unchecked")
	public static void readMetaFileTxt(MetaData md, File in) throws IOException {

		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();
		String[] labelTitle = md.getLabelTitle();
		HashMap<String, Integer> idmap = new HashMap<String, Integer>();

		if (labelTitle.length == 0) {
			for (int i = 0; i < expNames.length; i++) {
				idmap.put(expNames[i] + "_" + fractions[i] + "_" + replicates[i], i);
			}
		} else {
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					idmap.put(expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j],
							i * labelTitle.length + j);
				}
			}
		}

		ArrayList<String> labelList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(in));

		String line = reader.readLine();
		String[] title = line.split("[,\t]");

		int labelId = -1;
		int expNameId = -1;
		int metaStartId = -1;
		int metaCount = 0;

		for (int i = 0; i < title.length; i++) {
			if (title[i].trim().length() == 0) {
				break;
			}

			if (title[i].equals("Label")) {
				labelId = i;
			} else if (title[i].equals("Experiment name")) {
				expNameId = i;
			} else if (title[i].startsWith("Meta ")) {
				if (metaCount == 0) {
					metaStartId = i;
				}
				metaCount++;
			}
		}

		ArrayList<String>[] metaList = new ArrayList[metaCount];
		for (int i = 0; i < metaCount; i++) {
			metaList[i] = new ArrayList<String>();
		}

		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("[,\t]");

			String label = "";
			String expName = "";

			if (labelTitle.length > 0) {
				if (labelId >= 0 && labelId < cs.length) {
					label = cs[labelId];
					if (label.length() > 2 && label.startsWith("\"") && label.endsWith("\"")) {
						label = label.substring(1, label.length() - 1);
					}
				}
			}

			if (expNameId >= 0 && expNameId < cs.length) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			}

			labelList.add(label);
			expNameList.add(expName);

			for (int i = 0; i < metaCount; i++) {
				String meta = "";
				if (metaStartId + i < cs.length) {
					meta = cs[metaStartId + i];
				}

				metaList[i].add(meta);
			}
		}
		reader.close();

		if (labelTitle.length == 0) {
			String[][] metaInfo = new String[expNames.length][metaList.length];
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < metaCount; j++) {
					if (idmap.containsKey(expNameList.get(i)) && idmap.get(expNameList.get(i)) < metaList[j].size()) {
						metaInfo[i][j] = metaList[j].get(idmap.get(expNameList.get(i)));
					} else {
						throw new IOException("Can't find the experiment name " + expNameList.get(i)
								+ ", please ensure the experiment names keep consistent between the table in the panel and the loaded file") {

							/**
							 * 
							 */
							private static final long serialVersionUID = 5620518728523896410L;
						};
					}
				}
			}
			md.setMetaTypeCount(metaCount);
			md.setMetaInfo(metaInfo);
		} else {
			String[] labelExpNames = new String[expNames.length * labelTitle.length];
			String[][] metaInfo = new String[labelExpNames.length][metaList.length];

			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					int id = i * labelTitle.length + j;
					String label = expNames[i] + " " + labelTitle[j];
					if (idmap.containsKey(label) && idmap.get(label) < labelList.size()) {
						labelExpNames[id] = expNameList.get(idmap.get(label));
					} else {
						throw new IOException("Can't find the experiment name " + expNameList.get(i)
								+ ", please ensure the experiment names keep consistent between the table in the panel and the loaded file") {

							/**
							 * 
							 */
							private static final long serialVersionUID = 5620518728523896410L;
						};
					}

					for (int k = 0; k < metaCount; k++) {
						if (idmap.containsKey(label) && idmap.get(label) < metaList[k].size()) {
							metaInfo[id][k] = metaList[k].get(idmap.get(label));
						}
					}
				}
			}

			md.setLabelExpNames(labelExpNames);
			md.setMetaTypeCount(metaCount);
			md.setMetaInfo(metaInfo);
		}
	}

	public static void readMetaFileXlsx(MetaData md, String in) throws IOException {
		readMetaFileXlsx(md, new File(in));
	}

	@SuppressWarnings("unchecked")
	public static void readMetaFileXlsx(MetaData md, File in) throws IOException {

		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();
		String[] labelTitle = md.getLabelTitle();
		HashMap<String, Integer> idmap = new HashMap<String, Integer>();

		if (labelTitle.length == 0) {
			for (int i = 0; i < expNames.length; i++) {
				idmap.put(expNames[i] + "_" + fractions[i] + "_" + replicates[i], i);
			}
		} else {
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					idmap.put(expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j],
							i * labelTitle.length + j);
				}
			}
		}

		int labelId = -1;
		int expNameId = -1;

		ArrayList<String> labelList = new ArrayList<String>();
		ArrayList<String> expNameList = new ArrayList<String>();

		InputStream inp = new FileInputStream(in);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.rowIterator();

		int headId = 0;
		int metaCount = 0;
		int metaStartId = -1;

		if (rowIterator.hasNext()) {
			Row headerRow = rowIterator.next();
			Iterator<Cell> headCellIt = headerRow.cellIterator();

			while (headCellIt.hasNext()) {
				Cell cell = headCellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					String name = cell.getRichStringCellValue().getString();
					if (name.trim().length() == 0) {
						break;
					}

					if (name.equals("Label")) {
						labelId = headId;
					} else if (name.equals("Experiment name")) {
						expNameId = headId;
					} else if (name.startsWith("Meta ")) {
						if (metaCount == 0) {
							metaStartId = headId;
						}
						metaCount++;
					}
				}
				headId++;
			}
		}

		ArrayList<String>[] metaList = new ArrayList[metaCount];
		for (int i = 0; i < metaCount; i++) {
			metaList[i] = new ArrayList<String>();
		}

		while (rowIterator.hasNext()) {
			Row r = rowIterator.next();
			if (r == null) {
				break;
			}

			ArrayList<String> celllist = new ArrayList<String>();
			Iterator<Cell> cellIt = r.cellIterator();

			while (cellIt.hasNext()) {
				Cell cell = cellIt.next();
				if (cell.getCellType() == CellType.STRING) {
					celllist.add(cell.getRichStringCellValue().getString());
				}

				if (celllist.size() == headId) {
					break;
				}
			}
			String[] cs = celllist.toArray(new String[celllist.size()]);

			String label = "";
			String expName = "";

			if (labelTitle.length > 0) {
				if (labelId >= 0 && labelId < cs.length) {
					label = cs[labelId];
					if (label.length() > 2 && label.startsWith("\"") && label.endsWith("\"")) {
						label = label.substring(1, label.length() - 1);
					}
				}
			}

			if (expNameId >= 0 && expNameId < cs.length) {
				expName = cs[expNameId];
				if (expName.length() > 2 && expName.startsWith("\"") && expName.endsWith("\"")) {
					expName = expName.substring(1, expName.length() - 1);
				}
			}

			labelList.add(label);
			expNameList.add(expName);

			for (int i = 0; i < metaCount; i++) {
				String meta = "";
				if (metaStartId + i < cs.length) {
					meta = cs[metaStartId + i];
				}

				metaList[i].add(meta);
			}
		}

		if (labelTitle.length == 0) {
			String[][] metaInfo = new String[expNames.length][metaList.length];
			for (int i = 0; i < expNameList.size(); i++) {
				String namei = expNameList.get(i);
				for (int j = 0; j < metaCount; j++) {
					if (idmap.containsKey(namei) && idmap.get(namei) < metaList[j].size()) {
						int id = idmap.get(namei);
						metaInfo[id][j] = metaList[j].get(i);
					} else {
						throw new IOException("Can't find the experiment name " + expNameList.get(i)
								+ ", please ensure the experiment names keep consistent between the table in the panel and the loaded file") {

							/**
							 * 
							 */
							private static final long serialVersionUID = 5620518728523896410L;
						};
					}
				}
			}
			md.setMetaTypeCount(metaCount);
			md.setMetaInfo(metaInfo);
		} else {
			String[] labelExpNames = new String[expNames.length * labelTitle.length];
			String[][] metaInfo = new String[labelExpNames.length][metaList.length];

			if (labelExpNames.length != expNameList.size()) {
				throw new IOException("The row count is not equal to the expect count, "
						+ "please ensure the count of experiment names keep consistent between the table in the panel and the loaded file") {

					/**
					 * 
					 */
					private static final long serialVersionUID = 5620518728523896410L;
				};
			}

			for (int i = 0; i < labelList.size(); i++) {
				String namei = labelList.get(i);
				if (idmap.containsKey(namei)) {
					int id = idmap.get(namei);
					labelExpNames[id] = expNameList.get(i);
					for (int j = 0; j < metaCount; j++) {
						if (id < metaList[j].size()) {
							metaInfo[id][j] = metaList[j].get(i);
						}
					}
				} else {

					throw new IOException("Can't find the experiment name " + expNameList.get(i)
							+ ", please ensure the experiment names keep consistent between the table in the panel and the loaded file") {

						/**
						 * 
						 */
						private static final long serialVersionUID = 5620518728523896410L;
					};

				}
			}

			md.setLabelExpNames(labelExpNames);
			md.setMetaTypeCount(metaCount);
			md.setMetaInfo(metaInfo);
		}
	}

	public static void exportMetaXlsx(MetaData md, String output) throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		style.getFont().setBold(true);
		XSSFSheet sheet = workbook.createSheet("metadata");
		XSSFRow titleRow = sheet.createRow(0);

		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();
		String[][] metaInfo = md.getMetaInfo();
		String[] labelTitle = md.getLabelTitle();
		String[] labelNames = md.getLabelExpNames();
		int metaTypeCount = md.getMetaTypeCount();
		String[] title;
		if (labelTitle.length == 0) {
			title = new String[1 + metaTypeCount];
			title[0] = "Experiment name";
			for (int i = 0; i < metaTypeCount; i++) {
				title[i + 1] = "Meta " + (i + 1);
			}

		} else {
			title = new String[2 + metaTypeCount];
			title[0] = "Label";
			title[1] = "Experiment name";
			for (int i = 0; i < metaTypeCount; i++) {
				title[i + 2] = "Meta " + (i + 1);
			}
		}

		for (int i = 0; i < title.length; i++) {
			XSSFCell cell = titleRow.createCell(i);
			cell.setCellValue(title[i]);
			cell.setCellStyle(style);
		}

		style.getFont().setBold(false);

		if (labelTitle.length == 0) {
			for (int i = 0; i < expNames.length; i++) {
				XSSFRow row = sheet.createRow(i + 1);
				XSSFCell[] cells = new XSSFCell[title.length];

				int id = 0;

				cells[id] = row.createCell(id);
				cells[id].setCellValue(expNames[i] + "_" + fractions[i] + "_" + replicates[i]);
				cells[id].setCellStyle(style);
				id++;

				for (int j = 0; j < metaInfo[i].length; j++) {
					cells[id] = row.createCell(id);
					cells[id].setCellValue(metaInfo[i][j]);
					cells[id].setCellStyle(style);
					id++;
				}

			}

		} else {
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					int labelExpId = i * labelTitle.length + j;

					XSSFRow row = sheet.createRow(labelExpId + 1);
					XSSFCell[] cells = new XSSFCell[title.length];

					int id = 0;

					cells[id] = row.createCell(id);
					cells[id]
							.setCellValue(expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j]);
					cells[id].setCellStyle(style);
					id++;

					cells[id] = row.createCell(id);
					cells[id].setCellValue(labelNames[labelExpId]);
					cells[id].setCellStyle(style);
					id++;

					for (int k = 0; k < metaInfo[labelExpId].length; k++) {
						cells[id] = row.createCell(id);
						cells[id].setCellValue(metaInfo[labelExpId][k]);
						cells[id].setCellStyle(style);
						id++;
					}
				}
			}
		}

		FileOutputStream out = new FileOutputStream(output);
		workbook.write(out);
		out.close();
		workbook.close();
	}

	public static void exportMetaTsv(MetaData md, String output) throws IOException {

		PrintWriter writer = new PrintWriter(output);

		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();
		String[][] metaInfo = md.getMetaInfo();
		String[] labelTitle = md.getLabelTitle();
		String[] labelNames = md.getLabelExpNames();
		int metaTypeCount = md.getMetaTypeCount();

		StringBuilder titlesb = new StringBuilder();
		if (labelTitle.length == 0) {
			titlesb.append("Experiment name");
			for (int i = 0; i < metaTypeCount; i++) {
				titlesb.append("\t").append("Meta " + (i + 1));
			}
		} else {
			titlesb.append("Label").append("\t");
			titlesb.append("Experiment name");
			for (int i = 0; i < metaTypeCount; i++) {
				titlesb.append("\t").append("Meta " + (i + 1));
			}
		}

		writer.println(titlesb);

		if (labelTitle.length == 0) {
			for (int i = 0; i < expNames.length; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(expNames[i] + "_" + fractions[i] + "_" + replicates[i]);

				for (int j = 0; j < metaInfo[i].length; j++) {
					sb.append("\t").append(metaInfo[i][j]);
				}
				writer.println(sb);
			}

		} else {
			for (int i = 0; i < expNames.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {

					int labelExpId = i * labelTitle.length + j;

					StringBuilder sb = new StringBuilder();
					sb.append(expNames[i] + "_" + fractions[i] + "_" + replicates[i] + " " + labelTitle[j])
							.append("\t");
					sb.append(labelNames[labelExpId]);

					for (int k = 0; k < metaInfo[labelExpId].length; k++) {
						sb.append("\t").append(metaInfo[labelExpId][k]);
					}
					writer.println(sb);
				}
			}
		}

		writer.close();
	}

	public static void exportRawExpNameXlsx(MetaData md, String output) throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle style = workbook.createCellStyle();
		style.getFont().setBold(true);
		XSSFSheet sheet = workbook.createSheet("raw_experiment");
		XSSFRow titleRow = sheet.createRow(0);

		String[] rawfiles = md.getRawFiles();
		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();

		String[] title = new String[4];
		title[0] = "File path";
		title[1] = "Experiment name";
		title[2] = "Fraction";
		title[3] = "Replicate";

		for (int i = 0; i < title.length; i++) {
			XSSFCell cell = titleRow.createCell(i);
			cell.setCellValue(title[i]);
			cell.setCellStyle(style);
		}

		style.getFont().setBold(false);

		for (int i = 0; i < rawfiles.length; i++) {
			XSSFRow row = sheet.createRow(i + 1);
			XSSFCell[] cells = new XSSFCell[title.length];

			int id = 0;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(rawfiles[i]);
			cells[id].setCellStyle(style);
			id++;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(expNames[i]);
			cells[id].setCellStyle(style);
			id++;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(fractions[i]);
			cells[id].setCellStyle(style);
			id++;

			cells[id] = row.createCell(id);
			cells[id].setCellValue(replicates[i]);
			cells[id].setCellStyle(style);
			id++;
		}

		FileOutputStream out = new FileOutputStream(output);
		workbook.write(out);
		out.close();
		workbook.close();
	}

	public static void exportRawExpNameTsv(MetaData md, String output) throws IOException {

		PrintWriter writer = new PrintWriter(output);

		String[] rawfiles = md.getRawFiles();
		String[] expNames = md.getExpNames();
		int[] fractions = md.getFractions();
		int[] replicates = md.getReplicates();

		StringBuilder titlesb = new StringBuilder();
		titlesb.append("File path").append("\t");
		titlesb.append("Experiment name").append("\t");
		titlesb.append("Fraction").append("\t");
		titlesb.append("Replicate");

		writer.println(titlesb);

		for (int i = 0; i < rawfiles.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(rawfiles[i]).append("\t");
			sb.append(expNames[i]).append("\t");
			sb.append(fractions[i]).append("\t");
			sb.append(replicates[i]);
			writer.println(sb);
		}

		writer.close();
	}
}
