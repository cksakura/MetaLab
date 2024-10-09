package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqTask;
import bmi.med.uOttawa.metalab.task.par.MetaData;

public class PFindLabelFreeTask {

	private PFindResultReader reader;
	private FlashLfqTask lfqTask;

	private File resultFolder;
	private File rawSpectraFolder;
	private boolean mbr = true;
	private boolean nor = true;

	private MetaData metaData;

	private File quan_pep_file;
	private File quan_pro_file;
	private File quan_pro_file_razor;

	protected Logger LOGGER = LogManager.getLogger(PFindLabelFreeTask.class);
	protected String taskName = "PFind label-free quantification";
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public PFindLabelFreeTask(FlashLfqTask lfqTask, MetaData metaData, File resultFolder, File rawSpectraFolder,
			File ms2SpectraFolder) {
		this.reader = new PFindResultReader(resultFolder, ms2SpectraFolder);
		this.lfqTask = lfqTask;
		this.metaData = metaData;

		this.resultFolder = resultFolder;
		this.rawSpectraFolder = rawSpectraFolder;
	}

	public PFindLabelFreeTask(PFindResultReader reader, FlashLfqTask lfqTask, MetaData metaData, File resultFolder,
			File rawSpectraFolder) {
		this.reader = reader;
		this.lfqTask = lfqTask;
		this.metaData = metaData;

		this.resultFolder = resultFolder;
		this.rawSpectraFolder = rawSpectraFolder;
	}

	public PFindLabelFreeTask(PFindResultReader reader, FlashLfqTask lfqTask, MetaData metaData, File resultFolder,
			File rawSpectraFolder, boolean mbr, boolean nor) {
		this.reader = reader;
		this.lfqTask = lfqTask;
		this.metaData = metaData;

		this.resultFolder = resultFolder;
		this.rawSpectraFolder = rawSpectraFolder;
		this.mbr = mbr;
		this.nor = nor;
	}

	public boolean run() {

		if (this.reader.getPsms() == null) {
			try {
				this.reader.read();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		File msmsFile = this.reader.extractPSMs();
		if (msmsFile == null || !msmsFile.exists()) {

			LOGGER.info(taskName + ": extracting PSMs failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": extracting PSMs failed");

			return false;
		} else {
			LOGGER.info(taskName + ": PSMs have been extracted in " + msmsFile);
			System.out
					.println(format.format(new Date()) + "\t" + taskName + ": PSMs have been extracted in " + msmsFile);
		}

		this.quan_pep_file = new File(this.resultFolder, "QuantifiedPeptides.tsv");
		if (!quan_pep_file.exists() || quan_pep_file.length() == 0) {
			boolean lfqQuan = lfqTask.run(metaData, msmsFile.getAbsolutePath(), rawSpectraFolder.getAbsolutePath(), mbr,
					nor);
			if (!lfqQuan) {
				return false;
			} else {
				if (!this.quan_pep_file.exists()) {
					return false;
				}
			}
		} else {
			LOGGER.info(taskName + ": peptide quantification result has been found in " + resultFolder
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": peptide quantification result has been found in " + resultFolder + ", go to next step");
		}

		this.quan_pro_file = new File(this.resultFolder, "QuantifiedProteinsTotal.tsv");
		this.quan_pro_file_razor = new File(this.resultFolder, "QuantifiedProteinsRazor.tsv");

		if (!quan_pro_file.exists() || quan_pro_file.length() == 0 || !quan_pro_file_razor.exists()
				|| quan_pro_file_razor.length() == 0) {

			boolean quanpro = false;
			try {
				quanpro = quanPro();
			} catch (IOException e) {
				// TODO Auto-generated catch block

				LOGGER.error(taskName + ": error in protein quantification", e);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": error in protein quantification");

				return false;
			}

			return quanpro;

		} else {
			LOGGER.info(taskName + ": protein quantification result has been found in " + resultFolder
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": protein quantification result has been found in " + resultFolder + ", go to next step");
		}

		return true;
	}

	private boolean quanPro() throws IOException {

		HashMap<String, Integer> fileIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < this.metaData.getRawFileCount(); i++) {
			String rawName = this.metaData.getRawFiles()[i];
			rawName = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.lastIndexOf("."));
			fileIdMap.put(rawName, i);
		}

		BufferedReader quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
		String line = quanPepReader.readLine();
		String[] title = line.split("\t");

		int seqId = -1;
		int[] intenId = new int[fileIdMap.size()];
		Arrays.fill(intenId, -1);
		int[] idenId = new int[fileIdMap.size()];
		Arrays.fill(idenId, -1);

		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				seqId = i;
			} else if (title[i].equals("Base Sequence")) {
			} else if (title[i].startsWith("Intensity_")) {
				String fileName = title[i].substring("Intensity_".length());
				if (fileIdMap.containsKey(fileName)) {
					intenId[fileIdMap.get(fileName)] = i;
				}
			} else if (title[i].startsWith("Detection Type_")) {
				String fileName = title[i].substring("Detection Type_".length());
				if (fileIdMap.containsKey(fileName)) {
					idenId[fileIdMap.get(fileName)] = i;
				}
			}
		}

		boolean miss = false;
		for (int i = 0; i < intenId.length; i++) {
			if (intenId[i] == -1) {
				miss = true;
			}
		}

		if (miss) {
			File tempFile = new File(this.quan_pep_file.getParent(), "QuantifiedPeptides.temp");
			PrintWriter writer = new PrintWriter(tempFile);
			StringBuilder titleSb = new StringBuilder();
			for (int i = 0; i < title.length; i++) {
				if (title[i].startsWith("Intensity_")) {
					break;
				}
				titleSb.append(title[i]).append("\t");
			}

			for (int i = 0; i < this.metaData.getRawFileCount(); i++) {
				String rawName = this.metaData.getRawFiles()[i];
				rawName = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.length() - ".raw".length());
				titleSb.append("Intensity_").append(rawName).append("\t");
			}

			for (int i = 0; i < this.metaData.getRawFileCount(); i++) {
				String rawName = this.metaData.getRawFiles()[i];
				rawName = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.length() - ".raw".length());
				titleSb.append("Detection Type_").append(rawName).append("\t");
			}

			writer.println(titleSb.substring(0, titleSb.length() - 1));

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < cs.length; i++) {
					if (title[i].startsWith("Intensity_")) {
						break;
					}
					sb.append(cs[i]).append("\t");
				}

				for (int i = 0; i < intenId.length; i++) {
					if (intenId[i] == -1) {
						sb.append("0").append("\t");
					} else {
						sb.append(cs[intenId[i]]).append("\t");
					}
				}

				for (int i = 0; i < idenId.length; i++) {
					if (idenId[i] == -1) {
						sb.append("NotDetected").append("\t");
					} else {
						sb.append(cs[idenId[i]]).append("\t");
					}
				}

				writer.println(sb.substring(0, sb.length() - 1));
			}
			writer.close();
			quanPepReader.close();

			Files.move(tempFile.toPath(), this.quan_pep_file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			quanPepReader = new BufferedReader(new FileReader(this.quan_pep_file));
			title = quanPepReader.readLine().split("\t");

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				} else if (title[i].equals("Base Sequence")) {

				} else if (title[i].startsWith("Intensity_")) {
					String fileName = title[i].substring("Intensity_".length());
					if (fileIdMap.containsKey(fileName)) {
						intenId[fileIdMap.get(fileName)] = i;
					}
				} else if (title[i].startsWith("Detection Type_")) {
					String fileName = title[i].substring("Detection Type_".length());
					if (fileIdMap.containsKey(fileName)) {
						idenId[fileIdMap.get(fileName)] = i;
					}
				}
			}
		}

		HashMap<String, String> razorPepProMap = reader.getRazorPepProMap();
		HashMap<String, HashSet<String>> totalPepProMap = reader.getPepProMap();
		HashMap<String, String> modSeqMap = new HashMap<String, String>();
		PFindPSM[] psms = reader.getPsms();
		for (int i = 0; i < psms.length; i++) {
			modSeqMap.put(psms[i].getModseq(), psms[i].getSequence());
		}

		HashMap<String, double[]> razorProIntenMap = new HashMap<String, double[]>();
		HashMap<String, double[]> totalProIntenMap = new HashMap<String, double[]>();

		while ((line = quanPepReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs[2].startsWith("REV_"))
				continue;
			
			double[] intensity = new double[intenId.length];
			for (int i = 0; i < intensity.length; i++) {
				if (intenId[i] == -1) {
					intensity[i] = 0;
				} else {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
			}

			String baseSeq = "";
			if (modSeqMap.containsKey(cs[seqId])) {
				baseSeq = modSeqMap.get(cs[seqId]);
			}

			if (razorPepProMap.containsKey(baseSeq)) {
				String pro = razorPepProMap.get(baseSeq);
				if (razorProIntenMap.containsKey(pro)) {
					double[] total = razorProIntenMap.get(pro);
					for (int i = 0; i < intensity.length; i++) {
						total[i] += intensity[i];
					}
					razorProIntenMap.put(pro, total);
				} else {
					double[] total = new double[intensity.length];
					System.arraycopy(intensity, 0, total, 0, total.length);
					razorProIntenMap.put(pro, total);
				}
			}

			if (totalPepProMap.containsKey(baseSeq)) {
				HashSet<String> proSet = totalPepProMap.get(baseSeq);
				if (proSet.size() > 0) {
					for (String pro : proSet) {
						if (totalProIntenMap.containsKey(pro)) {
							double[] total = totalProIntenMap.get(pro);
							for (int i = 0; i < intensity.length; i++) {
								total[i] += intensity[i] / (double) proSet.size();
							}
							totalProIntenMap.put(pro, total);
						} else {
							double[] total = new double[intensity.length];
							System.arraycopy(intensity, 0, total, 0, total.length);
							totalProIntenMap.put(pro, total);
						}
					}
				}
			}

		}
		quanPepReader.close();

		StringBuilder titleSb = new StringBuilder();
		titleSb.append("Protein Groups").append("\t");
		titleSb.append("Gene Name").append("\t");
		titleSb.append("Organism");
		for (int i = 0; i < this.metaData.getRawFileCount(); i++) {
			String rawName = this.metaData.getRawFiles()[i];
			rawName = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.lastIndexOf("."));
			titleSb.append("\t").append("Intensity_").append(rawName);
		}

		PrintWriter totalProWriter = new PrintWriter(quan_pro_file);
		totalProWriter.println(titleSb);

		for (String pro : totalProIntenMap.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(pro).append("\t").append("\t");
			double[] intensity = totalProIntenMap.get(pro);
			for (int i = 0; i < intensity.length; i++) {
				sb.append("\t").append(intensity[i]);
			}
			totalProWriter.println(sb);
		}
		totalProWriter.close();

		PrintWriter razorProWriter = new PrintWriter(quan_pro_file_razor);
		razorProWriter.println(titleSb);

		double[] totalProIntensity = new double[intenId.length];
		for (String pro : razorProIntenMap.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(pro).append("\t").append("\t");
			double[] intensity = razorProIntenMap.get(pro);
			for (int i = 0; i < intensity.length; i++) {
				sb.append("\t").append(intensity[i]);
				totalProIntensity[i] += intensity[i];
			}
			razorProWriter.println(sb);
		}
		razorProWriter.close();

		if (quan_pro_file.exists() && quan_pro_file_razor.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public PFindResultReader getReader() {
		return reader;
	}

	public File getQuan_pep_file() {
		return quan_pep_file;
	}

	public File getQuan_pro_file_razor() {
		return quan_pro_file_razor;
	}

}
