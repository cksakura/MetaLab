package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.par.MetaData;

public class PFindIsobaricTask {

	private File resultFolder;
	private File spectraFolder;
	private File[] ms2files;
	private File ms2countFile;

	private String[] reportTitle;
	private MetaData metaData;
	private int rawFileCount;
	private int labelCount;
	private double[][] isoCorFactor;
	private boolean combineLabel;
	private HashMap<String, Integer> fileIdMap;
	private String[] rawFileNames;

	private PFindResultReader reader;

	private HashMap<String, HashSet<String>> pepProMap;
	private HashMap<String, HashSet<String>> proPepMap;

	private HashMap<String, String> pepSeqMap;
	private HashMap<String, HashMap<Integer, String>> scanModseqMap;
	private HashMap<String, Integer> filePsmCountMap;
	private HashMap<String, HashSet<String>> fileUnipepMap;

	private double[] labelMzs;

	private File ms2_intensity;
	private File lf_quan_pep_file;
	private File lf_quan_pro_file;
	private File lf_quan_peak_file;

	private File tmt_quan_pep_file;
	private File tmt_quan_pro_file;
	private File tmt_quan_pro_file_razor;

	private double tolerance = 0.0029d;

	protected Logger LOGGER = LogManager.getLogger(PFindIsobaricTask.class);
	protected String taskName = "Isobaric quantification";
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	
	public PFindIsobaricTask(PFindResultReader reader, File spectraFolder, File resultFolder,
			IsobaricTag tag, MaxquantModification[] isobaricLabels, double[][] isoCorFactor, boolean combineLabel,
			MetaData metadata) {

		this.reader = reader;
		this.resultFolder = resultFolder;
		this.spectraFolder = spectraFolder;

		this.metaData = metadata;
		this.isoCorFactor = isoCorFactor;
		this.combineLabel = combineLabel;
		this.rawFileCount = metadata.getRawFileCount();
		this.labelCount = metadata.getLabelCount();

		this.ms2files = spectraFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith(".ms2"))
					return true;
				return false;
			}
		});

		this.reportTitle = tag.getReportTitle();
		this.labelMzs = new double[tag.getnPlex()];
		for (int i = 0; i < labelMzs.length; i++) {
			this.labelMzs[i] = isobaricLabels[i].getTmtMass() + AminoAcidProperty.PROTON_W;
		}

		this.fileIdMap = new HashMap<String, Integer>();
		this.rawFileNames = new String[this.rawFileCount];
		for (int i = 0; i < this.rawFileCount; i++) {
			String rawName = this.metaData.getRawFiles()[i];
			rawFileNames[i] = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.lastIndexOf("."));
			fileIdMap.put(rawFileNames[i], i);
		}

		this.ms2_intensity = new File(resultFolder, "ms2_intensity.tsv");
		this.lf_quan_pep_file = new File(resultFolder, "QuantifiedPeptides.tsv");
		this.lf_quan_pro_file = new File(resultFolder, "QuantifiedProteins.tsv");
		this.lf_quan_peak_file = new File(resultFolder, "QuantifiedPeaks.tsv");

		this.tmt_quan_pep_file = new File(resultFolder, "IsobaricQuantifiedPeptides.tsv");
		this.tmt_quan_pro_file = new File(resultFolder, "IsobaricQuantifiedProteins.tsv");
		this.tmt_quan_pro_file_razor = new File(resultFolder, "IsobaricQuantifiedProteinsRazor.tsv");
	}

	public PFindIsobaricTask(PFindPSM[] psms, File spectraFolder, File resultFolder,
			IsobaricTag tag, MaxquantModification[] isobaricLabels, double[][] isoCorFactor, boolean combineLabel,
			MetaData metadata) {

		this.resultFolder = resultFolder;
		this.spectraFolder = spectraFolder;

		this.metaData = metadata;
		this.isoCorFactor = isoCorFactor;
		this.combineLabel = combineLabel;
		this.rawFileCount = metadata.getRawFileCount();
		this.labelCount = metadata.getLabelCount();

		this.ms2files = spectraFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith(".ms2"))
					return true;
				return false;
			}
		});

		this.reportTitle = tag.getReportTitle();
		this.labelMzs = new double[tag.getnPlex()];
		for (int i = 0; i < labelMzs.length; i++) {
			this.labelMzs[i] = isobaricLabels[i].getTmtMass() + AminoAcidProperty.PROTON_W;
		}

		this.fileIdMap = new HashMap<String, Integer>();
		this.rawFileNames = new String[this.rawFileCount];
		for (int i = 0; i < this.rawFileCount; i++) {
			String rawName = this.metaData.getRawFiles()[i];
			rawFileNames[i] = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.lastIndexOf("."));
			fileIdMap.put(rawFileNames[i], i);
		}

		this.ms2_intensity = new File(resultFolder, "ms2_intensity.tsv");
		this.lf_quan_pep_file = new File(resultFolder, "QuantifiedPeptides.tsv");
		this.lf_quan_pro_file = new File(resultFolder, "QuantifiedProteins.tsv");
		this.lf_quan_peak_file = new File(resultFolder, "QuantifiedPeaks.tsv");

		this.tmt_quan_pep_file = new File(resultFolder, "IsobaricQuantifiedPeptides.tsv");
		this.tmt_quan_pro_file = new File(resultFolder, "IsobaricQuantifiedProteins.tsv");
		this.tmt_quan_pro_file_razor = new File(resultFolder, "IsobaricQuantifiedProteinsRazor.tsv");

		try {
			this.parseIdenInfo(psms);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing peptide and protein idenfication information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in parsing peptide and protein idenfication information");
		}
	}
	
	public PFindIsobaricTask(File spectraFolder, File resultFolder, IsobaricTag tag,
			MaxquantModification[] isobaricLabels, double[][] isoCorFactor, boolean combineLabel, MetaData metadata) {

		this.resultFolder = resultFolder;
		this.spectraFolder = spectraFolder;

		this.metaData = metadata;
		this.isoCorFactor = isoCorFactor;
		this.combineLabel = combineLabel;
		this.rawFileCount = metadata.getRawFileCount();
		this.labelCount = metadata.getLabelCount();

		this.ms2files = spectraFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith(".ms2"))
					return true;
				return false;
			}
		});

		this.reportTitle = tag.getReportTitle();
		this.labelMzs = new double[tag.getnPlex()];
		for (int i = 0; i < labelMzs.length; i++) {
			this.labelMzs[i] = isobaricLabels[i].getTmtMass() + AminoAcidProperty.PROTON_W;
		}

		this.fileIdMap = new HashMap<String, Integer>();
		this.rawFileNames = new String[this.rawFileCount];
		for (int i = 0; i < this.rawFileCount; i++) {
			String rawName = this.metaData.getRawFiles()[i];
			rawFileNames[i] = rawName.substring(rawName.lastIndexOf("\\") + 1, rawName.lastIndexOf("."));
			fileIdMap.put(rawFileNames[i], i);
		}

		this.ms2_intensity = new File(resultFolder, "ms2_intensity.tsv");
		this.lf_quan_pep_file = new File(resultFolder, "QuantifiedPeptides.tsv");
		this.lf_quan_pro_file = new File(resultFolder, "QuantifiedProteins.tsv");
		this.lf_quan_peak_file = new File(resultFolder, "QuantifiedPeaks.tsv");

		this.tmt_quan_pep_file = new File(resultFolder, "IsobaricQuantifiedPeptides.tsv");
		this.tmt_quan_pro_file = new File(resultFolder, "IsobaricQuantifiedProteins.tsv");
		this.tmt_quan_pro_file_razor = new File(resultFolder, "IsobaricQuantifiedProteinsRazor.tsv");
	}

	private void calcuIsobaricPeptide(HashMap<String, HashMap<Integer, double[]>> totalMs2IntenMap) throws IOException {

		if (tmt_quan_pep_file.exists() && tmt_quan_pep_file.length() > 0) {
			LOGGER.info(taskName + ": peptide quantification result has been found in " + tmt_quan_pep_file
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": peptide quantification result has been found in " + tmt_quan_pep_file + ", go to next step");
			return;
		}

		BufferedReader lfqReader = new BufferedReader(new FileReader(this.lf_quan_pep_file));
		String line = lfqReader.readLine();

		String[] title = line.split("\t");
		int seqId = -1;
		int[] intenId = new int[fileIdMap.size()];
		Arrays.fill(intenId, -1);
		
		int[] idenId = new int[fileIdMap.size()];
		Arrays.fill(idenId, -1);
		
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				seqId = i;
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

		HashMap<String, double[]> ms1PepIntensityMap = new HashMap<String, double[]>();
		HashMap<String, int[]> idenTypeMap = new HashMap<String, int[]>();
		
		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			double[] intensity = new double[intenId.length];
			for (int i = 0; i < intensity.length; i++) {
				if (intenId[i] == -1) {
					intensity[i] = 0;
				} else {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}
			}

			if (ms1PepIntensityMap.containsKey(cs[seqId])) {
				double[] total = ms1PepIntensityMap.get(cs[seqId]);
				for (int i = 0; i < intensity.length; i++) {
					total[i] += intensity[i];
				}
				ms1PepIntensityMap.put(cs[seqId], total);
			} else {
				ms1PepIntensityMap.put(cs[seqId], intensity);
			}

			int[] idenType = new int[idenId.length];
			for (int i = 0; i < idenType.length; i++) {
				if (idenId[i] == -1) {
					idenType[i] = 2;
				} else {
					if (cs[idenId[i]].equals("MSMS")) {
						idenType[i] = 0;
					} else if (cs[idenId[i]].equals("MBR")) {
						idenType[i] = 1;
					} else if (cs[idenId[i]].equals("NotDetected")) {
						idenType[i] = 2;
					}
				}
			}
			idenTypeMap.put(cs[seqId], idenType);
		}
		lfqReader.close();

		HashMap<String, double[]> pepIntensityMap = new HashMap<String, double[]>();
		for (String fileName : this.rawFileNames) {

			HashMap<Integer, double[]> ms2IntenMap = totalMs2IntenMap.get(fileName);
			HashMap<Integer, String> scanPepMap = this.scanModseqMap.get(fileName);
			HashMap<String, ArrayList<double[]>> pepRatioIntensityMap = new HashMap<String, ArrayList<double[]>>();

			Integer[] scans = ms2IntenMap.keySet().toArray(new Integer[ms2IntenMap.size()]);

			for (int j = 0; j < scans.length; j++) {

				String modseq = scanPepMap.get(scans[j]);

				if (!ms1PepIntensityMap.containsKey(modseq)) {
					continue;
				}

				double ms1Intensity = ms1PepIntensityMap.get(modseq)[fileIdMap.get(fileName)];
				if (ms1Intensity == 0) {
					continue;
				}

				double[] intensity = ms2IntenMap.get(scans[j]);

				if (pepRatioIntensityMap.containsKey(modseq)) {
					pepRatioIntensityMap.get(modseq).add(intensity);
				} else {
					ArrayList<double[]> list = new ArrayList<double[]>();
					list.add(intensity);
					pepRatioIntensityMap.put(modseq, list);
				}
			}

			for (String modseq : pepRatioIntensityMap.keySet()) {
				ArrayList<double[]> list = pepRatioIntensityMap.get(modseq);
				double[] totalList = new double[list.size()];

				for (int j = 0; j < totalList.length; j++) {
					double[] intensityJ = list.get(j);
					int noZeroCount = 0;
					for (int k = 0; k < intensityJ.length; k++) {
						if (intensityJ[k] > 0) {
							totalList[j] += intensityJ[k];
							noZeroCount++;
						}
					}
					totalList[j] = totalList[j] * (double) noZeroCount / (double) intensityJ.length;
				}

				double[] weightIntensity = new double[this.labelMzs.length];
				double weight = 0;

				for (int j = 0; j < totalList.length; j++) {
					weight += totalList[j];
					double[] intensityJ = list.get(j);
					for (int k = 0; k < weightIntensity.length; k++) {
						weightIntensity[k] += intensityJ[k] * totalList[j];
					}
				}

				double[] pepIntensity;
				if (pepIntensityMap.containsKey(modseq)) {
					pepIntensity = pepIntensityMap.get(modseq);
				} else {
					pepIntensity = new double[this.metaData.getSampleCount()];
				}

				if (weight > 0) {
					double totalIntensity = MathTool.getTotal(weightIntensity);
					double ms1Intensity = ms1PepIntensityMap.get(modseq)[fileIdMap.get(fileName)];

					for (int j = 0; j < weightIntensity.length; j++) {
						weightIntensity[j] = weightIntensity[j] / totalIntensity * ms1Intensity;
					}
				}

				for (int j = 0; j < weightIntensity.length; j++) {
					pepIntensity[fileIdMap.get(fileName) * labelCount + j] += weightIntensity[j];
				}

				pepIntensityMap.put(modseq, pepIntensity);
			}
		}

		HashMap<String, double[]> normalPepIntenMap = this.normalize(pepIntensityMap);

		PrintWriter pepWriter = new PrintWriter(this.tmt_quan_pep_file);
		StringBuilder pepTitlesb = new StringBuilder();
		pepTitlesb.append("Sequence\t");
		pepTitlesb.append("Base Sequence\t");
		pepTitlesb.append("Protein Groups");

		StringBuilder intensityNameSb = new StringBuilder();

		HashMap<String, Integer> idMap = new HashMap<String, Integer>();
		int[] combineIds = new int[this.rawFileCount * this.labelCount];
		String[] combinedNames = null;
		
		if (combineLabel) {

			String[] expLabelNames = this.metaData.getExpNames();
			for (int i = 0; i < expLabelNames.length; i++) {
				if (!idMap.containsKey(expLabelNames[i])) {
					idMap.put(expLabelNames[i], idMap.size());
				}
			}

			combinedNames = idMap.keySet().toArray(new String[idMap.size()]);
			Arrays.sort(combinedNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					Integer id1 = idMap.get(o1);
					Integer id2 = idMap.get(o2);

					return id1.compareTo(id2);
				}

			});

			for (int i = 0; i < combinedNames.length; i++) {
				intensityNameSb.append("\tIntensity_").append(combinedNames[i]);
			}
			pepTitlesb.append(intensityNameSb);
			for (int i = 0; i < expLabelNames.length; i++) {
				combineIds[i] = idMap.get(expLabelNames[i]);
			}
		} else {
			String[] labelNames = metaData.getLabelTitle();
			for (int i = 0; i < this.rawFileNames.length; i++) {
				for (int j = 0; j < this.labelCount; j++) {
					intensityNameSb.append("\tIntensity_").append(this.rawFileNames[i]).append("_")
							.append(labelNames[j]);
				}
			}
			pepTitlesb.append(intensityNameSb);
		}

		for (int i = 0; i < this.rawFileNames.length; i++) {
			pepTitlesb.append("\t").append("Detection Type_").append(this.rawFileNames[i]);
		}
		pepWriter.println(pepTitlesb);
		
		lfqReader = new BufferedReader(new FileReader(this.lf_quan_pep_file));
		line = lfqReader.readLine();

		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (normalPepIntenMap.containsKey(cs[0])) {
				StringBuilder sb = new StringBuilder();
				sb.append(cs[0]).append("\t");
				sb.append(cs[1]).append("\t");
				sb.append(cs[2]);

				double[] intensity = normalPepIntenMap.get(cs[0]);
				for (int i = 0; i < intensity.length; i++) {
					sb.append("\t").append(intensity[i]);
				}
				int[] idenType = idenTypeMap.get(cs[0]);
				for (int i = 0; i < idenType.length; i++) {
					if (idenType[i] == 0) {
						sb.append("\t").append("MSMS");
					} else if (idenType[i] == 1) {
						sb.append("\t").append("MBR");
					} else if (idenType[i] == 2) {
						sb.append("\t").append("NotDetected");
					}
				}
				pepWriter.println(sb);
			}
		}
		lfqReader.close();
		pepWriter.close();
	}
	
	private void calcuIsobaricProtein(HashMap<String, HashMap<Integer, double[]>> totalMs2IntenMap) throws IOException {


		if (tmt_quan_pro_file.exists() && tmt_quan_pro_file.length() > 0) {
			LOGGER.info(taskName + ": protein quantification result has been found in " + tmt_quan_pro_file
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": protein quantification result has been found in " + tmt_quan_pro_file + ", go to next step");

			return;
		}

		BufferedReader lfqReader = new BufferedReader(new FileReader(this.lf_quan_pro_file));
		String line = lfqReader.readLine();

		String[] title = line.split("\t");
		int proId = -1;
		int[] intenId = new int[fileIdMap.size()];
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Protein Groups")) {
				proId = i;
			} else if (title[i].startsWith("Intensity_")) {
				String fileName = title[i].substring("Intensity_".length());
				if (fileIdMap.containsKey(fileName)) {
					intenId[fileIdMap.get(fileName)] = i;
				}
			}
		}

		HashMap<String, double[]> ms1ProIntensityMap = new HashMap<String, double[]>();
		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (this.proPepMap.containsKey(cs[0])) {
				double[] intensity = new double[intenId.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}

				if (ms1ProIntensityMap.containsKey(cs[proId])) {
					double[] total = ms1ProIntensityMap.get(cs[proId]);
					for (int i = 0; i < intensity.length; i++) {
						total[i] += intensity[i];
					}
					ms1ProIntensityMap.put(cs[proId], total);
				} else {
					ms1ProIntensityMap.put(cs[proId], intensity);
				}
			}
		}
		lfqReader.close();

		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		for (String fileName : this.rawFileNames) {

			HashMap<Integer, double[]> ms2IntenMap = totalMs2IntenMap.get(fileName);
			HashMap<Integer, String> scanPepMap = this.scanModseqMap.get(fileName);
			HashMap<String, ArrayList<double[]>> proRatioIntensityMap = new HashMap<String, ArrayList<double[]>>();

			Integer[] scans = ms2IntenMap.keySet().toArray(new Integer[ms2IntenMap.size()]);

			for (int j = 0; j < scans.length; j++) {

				String modseq = scanPepMap.get(scans[j]);
				String sequence = this.pepSeqMap.get(modseq);

				HashSet<String> proSet = this.pepProMap.get(sequence);
				for (String pro : proSet) {
					if (!ms1ProIntensityMap.containsKey(pro)) {
						continue;
					}

					double ms1Intensity = ms1ProIntensityMap.get(pro)[fileIdMap.get(fileName)];
					if (ms1Intensity == 0) {
						continue;
					}

					double[] intensity = ms2IntenMap.get(scans[j]);
					for (int k = 0; k < intensity.length; k++) {
						intensity[k] = intensity[k] / (double) proSet.size();
					}

					if (proRatioIntensityMap.containsKey(pro)) {
						proRatioIntensityMap.get(pro).add(intensity);
					} else {
						ArrayList<double[]> list = new ArrayList<double[]>();
						list.add(intensity);
						proRatioIntensityMap.put(pro, list);
					}
				}
			}

			for (String pro : proRatioIntensityMap.keySet()) {
				ArrayList<double[]> list = proRatioIntensityMap.get(pro);
				double[] totalList = new double[list.size()];
				boolean[] useRatio = new boolean[totalList.length];
				Arrays.fill(useRatio, true);

				for (int j = 0; j < totalList.length; j++) {
					totalList[j] = MathTool.getTotal(list.get(j));
					double[] intensityJ = list.get(j);
					for (int k = 0; k < intensityJ.length; k++) {
						if (intensityJ[k] == 0) {
							useRatio[j] = false;
							break;
						}
					}
				}

				double[] weightIntensity = new double[this.labelMzs.length];
				double weight = 0;

				for (int j = 0; j < totalList.length; j++) {
					if (useRatio[j]) {
						weight += totalList[j];
						double[] intensityJ = list.get(j);
						for (int k = 0; k < weightIntensity.length; k++) {
							weightIntensity[k] += intensityJ[k] * totalList[j];
						}
					}
				}

				double[] proIntensity;
				if (proIntensityMap.containsKey(pro)) {
					proIntensity = proIntensityMap.get(pro);
				} else {
					proIntensity = new double[this.metaData.getSampleCount()];
				}

				if (weight > 0) {
					double totalIntensity = MathTool.getTotal(weightIntensity);
					double ms1Intensity = ms1ProIntensityMap.get(pro)[fileIdMap.get(fileName)];

					for (int j = 0; j < weightIntensity.length; j++) {
						weightIntensity[j] = weightIntensity[j] / totalIntensity * ms1Intensity;
					}
				}

				for (int j = 0; j < weightIntensity.length; j++) {
					proIntensity[fileIdMap.get(fileName) * labelCount + j] += weightIntensity[j];
				}

				proIntensityMap.put(pro, proIntensity);
			}
		}

		HashMap<String, double[]> normalProIntenMap = this.normalize(proIntensityMap);
		PrintWriter proWriter = new PrintWriter(this.tmt_quan_pro_file);
		StringBuilder proTitlesb = new StringBuilder();
		proTitlesb.append("Protein Groups\t");
		proTitlesb.append("Gene Name\t");
		proTitlesb.append("Organism");
		
		StringBuilder intensityNameSb = new StringBuilder();

		HashMap<String, Integer> idMap = new HashMap<String, Integer>();
		int[] combineIds = new int[this.rawFileCount * this.labelCount];
		String[] combinedNames = null;

		if (combineLabel) {

			String[] expLabelNames = this.metaData.getExpNames();
			for (int i = 0; i < expLabelNames.length; i++) {
				if (!idMap.containsKey(expLabelNames[i])) {
					idMap.put(expLabelNames[i], idMap.size());
				}
			}

			combinedNames = idMap.keySet().toArray(new String[idMap.size()]);
			Arrays.sort(combinedNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					Integer id1 = idMap.get(o1);
					Integer id2 = idMap.get(o2);

					return id1.compareTo(id2);
				}

			});

			for (int i = 0; i < combinedNames.length; i++) {
				intensityNameSb.append("\tIntensity_").append(combinedNames[i]);
			}
			proWriter.println(proTitlesb.append(intensityNameSb));

			for (int i = 0; i < expLabelNames.length; i++) {
				combineIds[i] = idMap.get(expLabelNames[i]);
			}
		} else {
			String[] labelNames = metaData.getLabelTitle();
			for (int i = 0; i < this.rawFileNames.length; i++) {
				for (int j = 0; j < this.labelCount; j++) {
					intensityNameSb.append("\tIntensity_").append(this.rawFileNames[i]).append("_")
							.append(labelNames[j]);
				}
			}
			proWriter.println(proTitlesb.append(intensityNameSb));
		}

		lfqReader = new BufferedReader(new FileReader(this.lf_quan_pro_file));
		line = lfqReader.readLine();

		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (normalProIntenMap.containsKey(cs[0])) {
				StringBuilder sb = new StringBuilder();
				sb.append(cs[0]).append("\t");
				sb.append("\t");
				
				double[] intensity = normalProIntenMap.get(cs[0]);
				for (int i = 0; i < intensity.length; i++) {
					sb.append("\t").append(intensity[i]);
				}
				proWriter.println(sb);
			}
		}
		lfqReader.close();
		proWriter.close();
	}

	private void calcuIsobaricProtein(HashMap<String, HashMap<Integer, double[]>> totalMs2IntenMap,
			HashMap<String, String> pepProMap, File output) throws IOException {

		if (output.exists() && output.length() > 0) {
			LOGGER.info(taskName + ": protein quantification result has been found in " + output + ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": protein quantification result has been found in " + output + ", go to next step");

			return;
		}

		BufferedReader lfqReader = new BufferedReader(new FileReader(this.lf_quan_pro_file));
		String line = lfqReader.readLine();

		String[] title = line.split("\t");
		int proId = -1;
		int[] intenId = new int[fileIdMap.size()];
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Protein Groups")) {
				proId = i;
			} else if (title[i].startsWith("Intensity_")) {
				String fileName = title[i].substring("Intensity_".length());
				if (fileIdMap.containsKey(fileName)) {
					intenId[fileIdMap.get(fileName)] = i;
				}
			}
		}

		HashMap<String, double[]> ms1ProIntensityMap = new HashMap<String, double[]>();
		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (this.proPepMap.containsKey(cs[0])) {
				double[] intensity = new double[intenId.length];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[intenId[i]]);
				}

				if (ms1ProIntensityMap.containsKey(cs[proId])) {
					double[] total = ms1ProIntensityMap.get(cs[proId]);
					for (int i = 0; i < intensity.length; i++) {
						total[i] += intensity[i];
					}
					ms1ProIntensityMap.put(cs[proId], total);
				} else {
					ms1ProIntensityMap.put(cs[proId], intensity);
				}
			}
		}
		lfqReader.close();

		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		for (String fileName : this.rawFileNames) {

			HashMap<Integer, double[]> ms2IntenMap = totalMs2IntenMap.get(fileName);
			HashMap<Integer, String> scanPepMap = this.scanModseqMap.get(fileName);
			HashMap<String, ArrayList<double[]>> proRatioIntensityMap = new HashMap<String, ArrayList<double[]>>();

			Integer[] scans = ms2IntenMap.keySet().toArray(new Integer[ms2IntenMap.size()]);

			for (int j = 0; j < scans.length; j++) {

				String modseq = scanPepMap.get(scans[j]);
				String sequence = this.pepSeqMap.get(modseq);

				String pro = pepProMap.get(sequence);

				if (!ms1ProIntensityMap.containsKey(pro)) {
					continue;
				}

				double ms1Intensity = ms1ProIntensityMap.get(pro)[fileIdMap.get(fileName)];
				if (ms1Intensity == 0) {
					continue;
				}

				double[] intensity = ms2IntenMap.get(scans[j]);

				if (proRatioIntensityMap.containsKey(pro)) {
					proRatioIntensityMap.get(pro).add(intensity);
				} else {
					ArrayList<double[]> list = new ArrayList<double[]>();
					list.add(intensity);
					proRatioIntensityMap.put(pro, list);
				}
			}

			for (String pro : proRatioIntensityMap.keySet()) {
				ArrayList<double[]> list = proRatioIntensityMap.get(pro);
				double[] totalList = new double[list.size()];
				boolean[] useRatio = new boolean[totalList.length];
				Arrays.fill(useRatio, true);

				for (int j = 0; j < totalList.length; j++) {
					totalList[j] = MathTool.getTotal(list.get(j));
					double[] intensityJ = list.get(j);
					for (int k = 0; k < intensityJ.length; k++) {
						if (intensityJ[k] == 0) {
							useRatio[j] = false;
							break;
						}
					}
				}

				double[] weightIntensity = new double[this.labelMzs.length];
				double weight = 0;

				for (int j = 0; j < totalList.length; j++) {
					if (useRatio[j]) {
						weight += totalList[j];
						double[] intensityJ = list.get(j);
						for (int k = 0; k < weightIntensity.length; k++) {
							weightIntensity[k] += intensityJ[k] * totalList[j];
						}
					}
				}

				double[] proIntensity;
				if (proIntensityMap.containsKey(pro)) {
					proIntensity = proIntensityMap.get(pro);
				} else {
					proIntensity = new double[this.metaData.getSampleCount()];
				}

				if (weight > 0) {
					double totalIntensity = MathTool.getTotal(weightIntensity);
					double ms1Intensity = ms1ProIntensityMap.get(pro)[fileIdMap.get(fileName)];

					for (int j = 0; j < weightIntensity.length; j++) {
						weightIntensity[j] = weightIntensity[j] / totalIntensity * ms1Intensity;
					}
				}

				for (int j = 0; j < weightIntensity.length; j++) {
					proIntensity[fileIdMap.get(fileName) * labelCount + j] += weightIntensity[j];
				}

				proIntensityMap.put(pro, proIntensity);
			}
		}

		HashMap<String, double[]> normalProIntenMap = this.normalize(proIntensityMap);

		PrintWriter proWriter = new PrintWriter(output);
		StringBuilder proTitlesb = new StringBuilder();
		proTitlesb.append("Protein Groups\t");
		proTitlesb.append("Gene Name\t");
		proTitlesb.append("Organism");

		StringBuilder intensityNameSb = new StringBuilder();

		HashMap<String, Integer> idMap = new HashMap<String, Integer>();
		int[] combineIds = new int[this.rawFileCount * this.labelCount];
		String[] combinedNames = null;

		if (combineLabel) {

			String[] expLabelNames = this.metaData.getExpNames();
			for (int i = 0; i < expLabelNames.length; i++) {
				if (!idMap.containsKey(expLabelNames[i])) {
					idMap.put(expLabelNames[i], idMap.size());
				}
			}

			combinedNames = idMap.keySet().toArray(new String[idMap.size()]);
			Arrays.sort(combinedNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					Integer id1 = idMap.get(o1);
					Integer id2 = idMap.get(o2);

					return id1.compareTo(id2);
				}

			});

			for (int i = 0; i < combinedNames.length; i++) {
				intensityNameSb.append("\tIntensity_").append(combinedNames[i]);
			}
			proWriter.println(proTitlesb.append(intensityNameSb));

			for (int i = 0; i < expLabelNames.length; i++) {
				combineIds[i] = idMap.get(expLabelNames[i]);
			}
		} else {
			String[] labelNames = metaData.getLabelTitle();
			for (int i = 0; i < this.rawFileNames.length; i++) {
				for (int j = 0; j < this.labelCount; j++) {
					intensityNameSb.append("\tIntensity_").append(this.rawFileNames[i]).append("_")
							.append(labelNames[j]);
				}
			}
			proWriter.println(proTitlesb.append(intensityNameSb));
		}

		lfqReader = new BufferedReader(new FileReader(this.lf_quan_pro_file));
		line = lfqReader.readLine();

		while ((line = lfqReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (normalProIntenMap.containsKey(cs[0])) {
				StringBuilder sb = new StringBuilder();
				sb.append(cs[0]).append("\t");
				sb.append("\t");
				
				double[] intensity = normalProIntenMap.get(cs[0]);
				for (int i = 0; i < intensity.length; i++) {
					sb.append("\t").append(intensity[i]);
				}
				proWriter.println(sb);
			}
		}
		lfqReader.close();
		proWriter.close();
	}

	public Boolean run() {

		try {
			this.parseIdenInfo();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing peptide and protein idenfication information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in parsing peptide and protein idenfication information");
			return false;
		}

		HashMap<String, double[]> ms1IntensityMap = null;
		try {
			ms1IntensityMap = getMS1Map();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing MS1 quantitative information", e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in parsing MS1 quantitative information");
			return false;
		}

		HashMap<String, HashMap<Integer, double[]>> ms2Map = null;
		try {
			ms2Map = getMS2IntensityMap(ms1IntensityMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing MS2 quantitative information", e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in parsing MS2 quantitative information");
			return false;
		}

		try {
			calcuIsobaricPeptide(ms2Map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting quantitative peptide information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in exporting quantitative peptide information");
			return false;
		}

		try {
			calcuIsobaricProtein(ms2Map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting quantitative protein information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in exporting quantitative protein information");
			return false;
		}

		try {
			calcuIsobaricProtein(ms2Map, reader.getRazorPepProMap(), tmt_quan_pro_file_razor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting quantitative protein information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in exporting quantitative protein information");
			return false;
		}

		return true;
	}

	public Boolean runOnlyPeptide() {

		HashMap<String, double[]> ms1IntensityMap = null;
		try {
			ms1IntensityMap = getMS1Map();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing MS1 quantitative information", e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in parsing MS1 quantitative information");
			return false;
		}

		HashMap<String, HashMap<Integer, double[]>> ms2Map = null;
		try {
			ms2Map = getMS2IntensityMap(ms1IntensityMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in parsing MS2 quantitative information", e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in parsing MS2 quantitative information");
			return false;
		}

		try {
			calcuIsobaricPeptide(ms2Map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting quantitative peptide information", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in exporting quantitative peptide information");
			return false;
		}

		return true;
	}

	
	private void parseIdenInfo() throws IOException {

		this.scanModseqMap = new HashMap<String, HashMap<Integer, String>>();
		this.pepSeqMap = new HashMap<String, String>();

		this.filePsmCountMap = new HashMap<String, Integer>();
		this.fileUnipepMap = new HashMap<String, HashSet<String>>();

		if (this.reader == null) {
			return;
		}

		PFindPSM[] psms = reader.getPsms();

		this.pepProMap = reader.getPepProMap();
		this.proPepMap = reader.getProPepMap();

		for (int i = 0; i < psms.length; i++) {
			String file = psms[i].getFileName();
			file = file.substring(0, file.indexOf("."));
			
			String modseq = psms[i].getModseq();
			int scannum = psms[i].getScan();

			this.pepSeqMap.put(modseq, psms[i].getSequence());

			if (this.scanModseqMap.containsKey(file)) {
				this.scanModseqMap.get(file).put(scannum, modseq);
			} else {
				HashMap<Integer, String> map = new HashMap<Integer, String>();
				map.put(scannum, modseq);
				this.scanModseqMap.put(file, map);
			}

			String fileName = psms[i].getFileName();
			String sequence = psms[i].getSequence();

			if (filePsmCountMap.containsKey(fileName)) {
				filePsmCountMap.put(fileName, filePsmCountMap.get(fileName) + 1);
				HashSet<String> set = fileUnipepMap.get(fileName);
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			} else {
				filePsmCountMap.put(fileName, 1);
				HashSet<String> set = new HashSet<String>();
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			}
		}
	}
	
	private void parseIdenInfo(PFindPSM[] psms) throws IOException {

		this.scanModseqMap = new HashMap<String, HashMap<Integer, String>>();
		this.pepSeqMap = new HashMap<String, String>();

		this.filePsmCountMap = new HashMap<String, Integer>();
		this.fileUnipepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < psms.length; i++) {
			String file = psms[i].getFileName();
			String modseq = psms[i].getModseq();
			int scannum = psms[i].getScan();

			this.pepSeqMap.put(modseq, psms[i].getSequence());

			if (this.scanModseqMap.containsKey(file)) {
				this.scanModseqMap.get(file).put(scannum, modseq);
			} else {
				HashMap<Integer, String> map = new HashMap<Integer, String>();
				map.put(scannum, modseq);
				this.scanModseqMap.put(file, map);
			}

			String fileName = psms[i].getFileName();
			String sequence = psms[i].getSequence();

			if (filePsmCountMap.containsKey(fileName)) {
				filePsmCountMap.put(fileName, filePsmCountMap.get(fileName) + 1);
				HashSet<String> set = fileUnipepMap.get(fileName);
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			} else {
				filePsmCountMap.put(fileName, 1);
				HashSet<String> set = new HashSet<String>();
				set.add(sequence);
				fileUnipepMap.put(fileName, set);
			}
		}
	}

	private HashMap<String, double[]> getMS1Map() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(lf_quan_peak_file));
		String line = reader.readLine();
		String[] title = line.split("\t");
		int fileId = -1;
		int seqId = -1;
		int intensityId = -1;
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("File Name")) {
				fileId = i;
			} else if (title[i].equals("Full Sequence")) {
				seqId = i;
			} else if (title[i].equals("Peak intensity")) {
				intensityId = i;
			}
		}

		HashMap<String, double[]> ms1IntenMap = new HashMap<String, double[]>();
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			String[] seq = cs[seqId].split("\\|");

			for (int i = 0; i < seq.length; i++) {
				double[] ms1Intensity;
				if (ms1IntenMap.containsKey(seq[i])) {
					ms1Intensity = ms1IntenMap.get(seq[i]);
				} else {
					ms1Intensity = new double[this.rawFileCount];
				}

				double intensity = Double.parseDouble(cs[intensityId]) / (double) seq.length;
				if (intensity > ms1Intensity[this.fileIdMap.get(cs[fileId])]) {
					ms1Intensity[this.fileIdMap.get(cs[fileId])] = intensity;
				}
				ms1IntenMap.put(seq[i], ms1Intensity);
			}
		}
		reader.close();

		return ms1IntenMap;
	}

	private HashMap<String, double[]> normalize(HashMap<String, double[]> pepIntenMap) throws IOException {

		HashSet<Integer> usedRefIdSet = new HashSet<Integer>();
		int firstRefId = -1;

		if (metaData.getRefChannelId() > -1) {
			for (int i = 0; i < this.rawFileCount; i++) {
				int expId = i * this.labelCount + metaData.getRefChannelId();
				usedRefIdSet.add(expId);

				if (firstRefId == -1) {
					firstRefId = expId;
				}
			}
		} else {
			String[] refs = this.metaData.getIsobaricReference();
			boolean[] selectRefs = this.metaData.getSelectRef();
			HashSet<String> refSet = new HashSet<String>();
			for (int i = 0; i < refs.length; i++) {
				if (selectRefs[i])
					refSet.add(refs[i]);
			}

			for (int i = 0; i < this.rawFileCount; i++) {
				for (int j = 0; j < this.labelCount; j++) {
					int expId = i * this.labelCount + j;
					String expName = this.metaData.getLabelExpNames()[expId];
					if (refSet.contains(expName)) {
						usedRefIdSet.add(expId);

						if (firstRefId == -1) {
							firstRefId = expId;
						}
					}
				}
			}
		}

		HashMap<String, double[]> normalMap = new HashMap<String, double[]>();
		for (String modseq : pepIntenMap.keySet()) {
			ArrayList<Double> list = new ArrayList<Double>();
			double[] intensity = pepIntenMap.get(modseq);
			for (int i = 0; i < intensity.length; i++) {
				if (usedRefIdSet.contains(i) && intensity[i] > 0) {
					list.add(intensity[i]);
				}
			}

			double median = MathTool.getMedian(list);

			if (median > 0) {
				double[] normalIntensity = new double[intensity.length];

				for (int i = 0; i < this.rawFileCount; i++) {
					double ratio = 0;
					int refCount = 0;

					for (int j = 0; j < this.labelCount; j++) {
						int expId = i * this.labelCount + j;
						if (usedRefIdSet.contains(expId)) {
							if (intensity[expId] > 0) {
								ratio += intensity[expId] / median;
								refCount++;
							}
						}
					}

					if (refCount > 0) {
						ratio = ratio / (double) refCount;

						for (int j = 0; j < this.labelCount; j++) {
							int expId = i * this.labelCount + j;
							normalIntensity[expId] = intensity[expId] / ratio;
						}
					}
				}

				normalMap.put(modseq, normalIntensity);
			}
		}

		if (normalMap.size() == 0) {
			LOGGER.info(taskName + ": no reference channel was found, using the original intensity instead");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": no reference channel was found, using the original intensity instead");

			return pepIntenMap;
		}

		return normalMap;
	}

	private HashMap<String, HashMap<Integer, double[]>> getMS2IntensityMap(HashMap<String, double[]> ms1IntensityMap)
			throws IOException {

		HashMap<String, HashMap<Integer, double[]>> ms2IntensityMap = new HashMap<String, HashMap<Integer, double[]>>();
		this.ms2countFile = new File(this.spectraFolder, "ms2count.txt");
		this.ms2_intensity = new File(this.resultFolder, "ms2_intensity.tsv");

		if (ms2_intensity.exists() && ms2_intensity.length() > 0 && ms2countFile.exists()
				&& ms2countFile.length() > 0) {

			BufferedReader reader = new BufferedReader(new FileReader(ms2_intensity));
			String line = reader.readLine();

			while ((line = reader.readLine()) != null) {

				String[] cs = line.split("\t");

				HashMap<Integer, double[]> scanIntensityMap;
				if (ms2IntensityMap.containsKey(cs[0])) {
					scanIntensityMap = ms2IntensityMap.get(cs[0]);
				} else {
					scanIntensityMap = new HashMap<Integer, double[]>();
					ms2IntensityMap.put(cs[0], scanIntensityMap);
				}

				double[] intensity = new double[this.labelMzs.length];
				for (int k = 0; k < intensity.length; k++) {
					intensity[k] = Double.parseDouble(cs[k + 3]);
				}

				scanIntensityMap.put(Integer.parseInt(cs[1]), intensity);
			}

			reader.close();

		} else {
			double[] correctFactor = new double[this.isoCorFactor.length];
			Arrays.fill(correctFactor, 100.0);
			for (int i = 0; i < correctFactor.length; i++) {
				for (int j = 0; j < isoCorFactor[i].length; j++) {
					correctFactor[i] = correctFactor[i] - isoCorFactor[i][j];
				}

				correctFactor[i] = correctFactor[i] / 100.0;
			}

			PrintWriter writer = new PrintWriter(ms2_intensity);
			StringBuilder titleBuilder = new StringBuilder();
			titleBuilder.append("File").append("\t");
			titleBuilder.append("Scan").append("\t");
			titleBuilder.append("Modified sequence");
			for (int i = 0; i < this.labelMzs.length; i++) {
				titleBuilder.append("\t").append(this.reportTitle[i]);
			}
			titleBuilder.append("\t").append("Label free intensity");
			writer.println(titleBuilder);

			PrintWriter ms2CountWriter = new PrintWriter(ms2countFile);
			ms2CountWriter.println("File\tMS2 count");

			for (int i = 0; i < ms2files.length; i++) {

				String filename = ms2files[i].getName().substring(0, ms2files[i].getName().length() - ".ms2".length());
				int fileid = this.fileIdMap.get(filename);

				HashMap<Integer, String> modSeqMap = this.scanModseqMap.get(filename);

				HashMap<Integer, double[]> intenMap = new HashMap<Integer, double[]>();

				boolean use = false;
				BufferedReader reader = new BufferedReader(new FileReader(ms2files[i]));
				String line = null;
				int scannum = -1;
				int scanCount = 0;

				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("[\\s+]");
					if (cs.length == 2) {
						if (use) {
							double mz = Double.parseDouble(cs[0]);
							double intensity = Double.parseDouble(cs[1]);
							for (int j = 0; j < this.labelMzs.length; j++) {
								if (Math.abs(mz - this.labelMzs[j]) < this.tolerance) {
									double[] intens = intenMap.get(scannum);
									if (intensity >= intens[j]) {
										intens[j] = intensity;
									}
								}
							}
						}
					} else {
						if (cs[0].equals("S")) {
							scannum = Integer.parseInt(cs[1]);
							if (modSeqMap.containsKey(scannum)) {
								intenMap.put(scannum, new double[this.labelMzs.length]);
								use = true;
							} else {
								use = false;
							}
							scanCount++;
						}
					}
				}
				reader.close();

				Integer[] scans = intenMap.keySet().toArray(new Integer[intenMap.size()]);
				for (int j = 0; j < scans.length; j++) {

					String modseq = modSeqMap.get(scans[j]);

					if (!ms1IntensityMap.containsKey(modseq)) {
						continue;
					}

					double ms1Intensity = ms1IntensityMap.get(modseq)[fileid];
					if (ms1Intensity == 0) {
						continue;
					}

					StringBuilder sb = new StringBuilder();
					sb.append(filename).append("\t");
					sb.append(scans[j]).append("\t");
					sb.append(modseq);

					double[] intensity = intenMap.get(scans[j]);

					boolean hasValue = false;
					for (int k = 0; k < intensity.length; k++) {
						intensity[k] = intensity[k] / correctFactor[k];
						sb.append("\t").append(intensity[k]);
						if (intensity[k] > 0) {
							hasValue = true;
						}
					}
					sb.append("\t").append(ms1Intensity);

					if (hasValue) {
						writer.println(sb);
					}
				}

				ms2CountWriter.println(filename + "\t" + scanCount);
				ms2IntensityMap.put(filename, intenMap);
			}

			writer.close();
			ms2CountWriter.close();
		}

		return ms2IntensityMap;
	}

	public HashMap<String, Integer> getFilePsmCountMap() {
		return filePsmCountMap;
	}

	public HashMap<String, HashSet<String>> getFileUnipepMap() {
		return fileUnipepMap;
	}

	public File getMs2countFile() {
		return ms2countFile;
	}

	public File getQuanPepFile() {
		return tmt_quan_pep_file;
	}

	public File getQuanProFile() {
		return tmt_quan_pro_file;
	}
	
	public File getQuanRazorProFile() {
		return tmt_quan_pro_file_razor;
	}

}
