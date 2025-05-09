package bmi.med.uOttawa.metalab.spectra.io;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;

import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;

import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

public class MZMLReader {

	public MZMLReader() {

	}

	@SuppressWarnings("unused")
	private void ParseAllScans(String filename) {

		MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(new File(filename));
		MzMLObjectIterator<Spectrum> itr = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum",
				Spectrum.class);
//        ArrayList<mzMLSpecConverter> ScanList=new ArrayList<>();        
		ExecutorService executorPool = null;
		executorPool = Executors.newFixedThreadPool(10);

		int count = 0;
		while (itr.hasNext()) {
			Spectrum jmzSpec = itr.next();
//            mzMLSpecConverter converter=new mzMLSpecConverter(jmzSpec,parameter);
//            executorPool.execute(converter);    
			// ScanList.add(converter);
			count++;
		}
		executorPool.shutdown();
		try {
			executorPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

		}
		System.out.println(count);

		/*
		 * for (mzMLSpecConverter converter : ScanList) { ScanData spec =
		 * converter.spec; scanCollection.AddScan(spec);
		 * ElutionTimeToScanNoMap.put(spec.RetentionTime, spec.ScanNum);
		 * ScanToElutionTime.put(spec.ScanNum, spec.RetentionTime);
		 * MsLevelList.put(spec.ScanNum, spec.MsLevel);
		 * 
		 * if(spec.MsLevel==1){ NoMS1Scans++; }
		 * 
		 * if (datatype != SpectralDataType.DataType.DDA && spec.MsLevel == 2){ if
		 * (datatype == SpectralDataType.DataType.DIA_V_Window) { for (XYData window :
		 * dIA_Setting.DIAWindows.keySet()) { if (window.getX() <=
		 * spec.isolationWindowTargetMz && window.getY() >=
		 * spec.isolationWindowTargetMz) {
		 * dIA_Setting.DIAWindows.get(window).add(spec.ScanNum); break; } } } else { if
		 * (spec.isolationWindowLoffset > 0f && spec.isolationWindowRoffset > 0f) { if
		 * (!dIA_Setting.DIAWindows.containsKey(new XYData(spec.isolationWindowTargetMz
		 * - spec.isolationWindowLoffset, spec.isolationWindowTargetMz +
		 * spec.isolationWindowRoffset))) { ArrayList<Integer> scanList2 = new
		 * ArrayList<>(); dIA_Setting.DIAWindows.put(new
		 * XYData(spec.isolationWindowTargetMz - spec.isolationWindowLoffset,
		 * spec.isolationWindowTargetMz + spec.isolationWindowRoffset), scanList2); }
		 * dIA_Setting.DIAWindows.get(new XYData(spec.isolationWindowTargetMz -
		 * spec.isolationWindowLoffset, spec.isolationWindowTargetMz +
		 * spec.isolationWindowRoffset)).add(spec.ScanNum); } else { if (datatype ==
		 * SpectralDataType.DataType.DIA_F_Window) { spec.isolationWindowLoffset =
		 * (dIA_Setting.F_DIA_WindowSize + 1) * 0.2f; spec.isolationWindowRoffset =
		 * (dIA_Setting.F_DIA_WindowSize + 1) * 0.8f; if
		 * (!dIA_Setting.DIAWindows.containsKey(new XYData(spec.isolationWindowTargetMz
		 * - spec.isolationWindowLoffset, spec.isolationWindowTargetMz +
		 * spec.isolationWindowRoffset))) { ArrayList<Integer> scanList2 = new
		 * ArrayList<>(); dIA_Setting.DIAWindows.put(new
		 * XYData(spec.isolationWindowTargetMz - spec.isolationWindowLoffset,
		 * spec.isolationWindowTargetMz + spec.isolationWindowRoffset), scanList2); }
		 * dIA_Setting.DIAWindows.get(new XYData(spec.isolationWindowTargetMz -
		 * spec.isolationWindowLoffset, spec.isolationWindowTargetMz +
		 * spec.isolationWindowRoffset)).add(spec.ScanNum); } } } } } try {
		 * FSElutionIndexWrite(); } catch (IOException ex) { }
		 */

	}

	public void parse(File file) {

		try {
			MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(file);
			@SuppressWarnings("unused")
			MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller
					.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class);
			int spCount = unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum");
			System.out.println(unmarshaller.getChromatogramIDs().size());
			System.out.println(spCount);
			System.out.println(unmarshaller.getMzMLId());
/*			
			int ms1Count = 0;
			int[] mzrange = new int[] {500, 500};
			while (spectrumIterator.hasNext()) {
				Spectrum spectrum = spectrumIterator.next();

				if (spectrum.getPrecursorList() == null) {
					List<BinaryDataArray> bdlist = spectrum.getBinaryDataArrayList().getBinaryDataArray();
					Number[] mzArray = bdlist.get(0).getBinaryDataAsNumberArray();
					Number[] intensityArray = bdlist.get(1).getBinaryDataAsNumberArray();
					if (mzArray.length == intensityArray.length) {
						if (mzArray[0].floatValue() < mzrange[0]) {
							mzrange[0] = (int) mzArray[0].floatValue();
						}
						if (mzArray[mzArray.length-1].floatValue() > mzrange[1]) {
							mzrange[1] = (int) mzArray[mzArray.length-1].floatValue();
						}
						ms1Count++;
					}
				}
			}
			System.out.println(ms1Count+"\t"+mzrange[0]+"\t"+mzrange[1]);
*/		
/*
			double[][] data = new double[620000][1823];
			double[] rtArray = new double[1823];
			int scanId = 0;

			while (spectrumIterator.hasNext()) {
				Spectrum spectrum = spectrumIterator.next();

				if (spectrum.getPrecursorList() == null) {
					List<BinaryDataArray> bdlist = spectrum.getBinaryDataArrayList().getBinaryDataArray();
					Number[] mzArray = bdlist.get(0).getBinaryDataAsNumberArray();
					Number[] intensityArray = bdlist.get(1).getBinaryDataAsNumberArray();
					if (mzArray.length == intensityArray.length) {
						for (int i = 0; i < mzArray.length; i++) {
							int mzId = (int) ((mzArray[i].floatValue() - 394.0) * 1000.0);
							data[mzId][scanId] += intensityArray[i].floatValue();
						}
					}

					for (CVParam cvParam : spectrum.getScanList().getScan().get(0).getCvParam()) {
						if ("MS:1000016".equals(cvParam.getAccession())) {
							rtArray[scanId] = Double.parseDouble(cvParam.getValue());
						}
					}

					scanId++;
				}
			}

			System.out.println("Total scan\t"+scanId);
			
			PrintWriter writer1 = new PrintWriter(new File(file.getParent(), file.getName()+".precursor.txt"));
			for (int i = 0; i < data.length; i++) {
				float totalI = 0;
				StringBuilder sb = new StringBuilder();
				sb.append(i);
				for (int j = 0; j < data[i].length; j++) {
					sb.append("\t").append(data[i][j]);
					totalI += data[i][j];
				}
				if (totalI > 0) {
					writer1.println(sb);
				}
			}
			writer1.close();

			PrintWriter writer2 = new PrintWriter(new File(file.getParent(), file.getName()+".features.txt"));
			for (int i = 0; i < data.length; i++) {
				List<int[]> rangeList = detectAndMergeFeatures(data[i], 10);
				for (int[] range : rangeList) {
					StringBuilder sb = new StringBuilder();
					sb.append((double) i / 100.0).append("\t");
					sb.append(range[0]).append("\t");
					sb.append(rtArray[range[0]]).append("\t");
					sb.append(range[1]).append("\t");
					sb.append(rtArray[range[1]]).append("\t");
					double totalIntesntiy = 0;
					for (int j = range[0]; j <= range[1]; j++) {
						sb.append(data[i][j]).append("\t");
						totalIntesntiy += data[i][j];
					}
					sb.append(totalIntesntiy);
					writer2.println(sb);
				}
			}
			writer2.close();
*/	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	public List<int[]> detectAndMergeFeatures(double[] data, int windowSize) {
		List<int[]> featureRanges = new ArrayList<>();
		List<Integer> featureIndices = detectFeatures(data, windowSize);

		if (featureIndices.isEmpty()) {
			return featureRanges;
		}

		// Merge overlapping features
		int currentStart = featureIndices.get(0);
		int currentEnd = currentStart + windowSize - 1;

		for (int i = 1; i < featureIndices.size(); i++) {
			int newStart = featureIndices.get(i);
			int newEnd = newStart + windowSize - 1;

			// If the new feature overlaps with the current one, extend the current range
			if (newStart <= currentEnd) {
				currentEnd = Math.max(currentEnd, newEnd);
			} else {
				// No overlap, add the current range and start a new one
				featureRanges.add(new int[] { currentStart, currentEnd });
				currentStart = newStart;
				currentEnd = newEnd;
			}
		}

		// Add the last feature range
		featureRanges.add(new int[] { currentStart, currentEnd });

		return featureRanges;
	}

	public List<Integer> detectFeatures(double[] data, int windowSize) {
		List<Integer> featureIndices = new ArrayList<>();
		KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();

		// Loop through the array with a sliding window of 'windowSize'
		L: for (int i = 0; i <= data.length - windowSize; i++) {
			// Collect 10 consecutive non-zero numbers
			if (data[i] == 0) {
				continue;
			}
			List<Double> window = new ArrayList<>();
			for (int j = 0; j < windowSize; j++) {
				if (data[i + j] != 0) {
					window.add(data[i + j]);
				} else {
					continue L;
				}
			}

			// Only process if we have exactly 'windowSize' non-zero numbers
			if (window.size() == windowSize) {
				double[] windowArray = window.stream().mapToDouble(Double::doubleValue).toArray();

				// Check if the window conforms to a normal distribution
				if (isNormalDistribution(windowArray, ksTest)) {
					// If it does, add the starting index of the window
					featureIndices.add(i);
				}
			}
		}
		return featureIndices;
	}
	
	public List<Integer> detectFeatures(double[] data, double[] previousData, double[] nextData, int windowSize) {
		List<Integer> featureIndices = new ArrayList<>();
		KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();

		// Loop through the array with a sliding window of 'windowSize'
		L: for (int i = 0; i <= data.length - windowSize; i++) {
			// Collect 10 consecutive non-zero numbers
			if (data[i] == 0) {
				continue;
			}
			List<Double> window = new ArrayList<>();
			for (int j = 0; j < windowSize; j++) {
				if (data[i + j] != 0) {
					window.add(data[i + j]);
				} else {
					continue L;
				}
			}

			// Only process if we have exactly 'windowSize' non-zero numbers
			if (window.size() == windowSize) {
				double[] windowArray = window.stream().mapToDouble(Double::doubleValue).toArray();

				// Check if the window conforms to a normal distribution
				if (isNormalDistribution(windowArray, ksTest)) {
					// If it does, add the starting index of the window
					featureIndices.add(i);
				}
			}
		}
		return featureIndices;
	}

	// Function to check if the data follows a normal distribution
	public boolean isNormalDistribution(double[] data, KolmogorovSmirnovTest ksTest) {
		// Calculate mean and standard deviation
		Mean mean = new Mean();
		StandardDeviation stdDev = new StandardDeviation();
		double meanValue = mean.evaluate(data);
		double stdValue = stdDev.evaluate(data);

		// Generate a normal distribution sample with the same mean and std
		double[] normalSample = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			normalSample[i] = meanValue + stdValue * Math.random();
		}

		// Perform Kolmogorov-Smirnov test
		return ksTest.kolmogorovSmirnovTest(data, normalSample) > 0.05; // 0.05 is a typical significance level
	}

	public void compare(String feature, String parquet) {

		ArrayList<DiaNNPrecursor> list = new ArrayList<DiaNNPrecursor>();
		try {
			Path path = new Path(parquet);

			HadoopInputFile inputFile = HadoopInputFile.fromPath(path, new Configuration());
			ParquetReadOptions readOptions = ParquetReadOptions.builder().build();
			ParquetFileReader fileReader = ParquetFileReader.open(inputFile, readOptions);

			// Get file schema
			ParquetMetadata metadata = fileReader.getFooter();
			MessageType schema = metadata.getFileMetaData().getSchema();
			System.out.println("Schema: " + schema);

			// Create a factory for SimpleGroup to read the data
			@SuppressWarnings("unused")
			SimpleGroupFactory groupFactory = new SimpleGroupFactory(schema);

			// Loop through row groups in the Parquet file
			PageReadStore pages;
			while ((pages = fileReader.readNextRowGroup()) != null) {
				long rowCount = pages.getRowCount();
				System.out.println("Row group has " + rowCount + " rows.");

				MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
				GroupRecordConverter recordConverter = new GroupRecordConverter(schema);
				RecordReader<Group> recordReader = columnIO.getRecordReader(pages, recordConverter);

				for (int i = 0; i < rowCount; i++) {
					Group group = recordReader.read();
					String modifiedSequence = group.getBinary("Modified.Sequence", 0).toStringUsingUTF8();
					float precursorMz = group.getFloat("Precursor.Mz", 0);
					float rt = group.getFloat("RT", 0);
					list.add(new DiaNNPrecursor(precursorMz, rt, modifiedSequence));
				}
			}

			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("PP\t" + list.size());
		DiaNNPrecursor[] pps = list.toArray(new DiaNNPrecursor[list.size()]);
		Arrays.sort(pps, new Comparator<DiaNNPrecursor>() {
			@Override
			public int compare(DiaNNPrecursor o1, DiaNNPrecursor o2) {
				// TODO Auto-generated method stub
				if (o1.mz < o2.mz)
					return -1;
				if (o1.mz > o2.mz)
					return 1;
				return 0;
			}
		});

		ArrayList<DiaNNPrecursor> matchList = new ArrayList<DiaNNPrecursor>();
		int id = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(feature))) {
			String line = null;
			L: while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				float mz = Float.parseFloat(cs[0]);
				double tolerance = mz * 1E-5;
				for (; id < pps.length; id++) {
					if (mz + tolerance < pps[id].mz) {
						continue L;
					} else if (mz + tolerance >= pps[id].mz && mz - tolerance <= pps[id].mz) {
//						float startRt = Float.parseFloat(cs[2]);
//						float endRt = Float.parseFloat(cs[4]);

//						if (pps[id].rt >= startRt && pps[id].rt <= endRt) {
							matchList.add(pps[id]);
//						}

					} else {
						continue;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("match PP\t" + matchList.size());
	}

	class DiaNNPrecursor {
		float mz;
		float rt;
		String seq;

		DiaNNPrecursor(float mz, float rt, String seq) {
			this.mz = mz;
			this.rt = rt;
			this.seq = seq;
		}
	}
	
	public static void main(String[] args) {

		long begin = System.currentTimeMillis();

		MZMLReader reader = new MZMLReader();
		reader.parse(new File("Z:\\Kai\\Raw_files\\DIA_validate\\QE480_human\\Dora_20240323_Neo_240_HeLa_8mz_staggered_27NCE_01.mzML"));
//		reader.compare("Z:\\Kai\\Raw_files\\run21\\raw\\Haonan_20220809_DIA_S48_11.features.txt",
//				"Z:\\Kai\\Raw_files\\run21\\DIA\\MetaLab20240625DDA\\Haonan_20220809_DIA_S48_11\\lastSearch.parquet");

		long end = System.currentTimeMillis();
		System.out.println((end - begin) / 60000.0);
	}
}
