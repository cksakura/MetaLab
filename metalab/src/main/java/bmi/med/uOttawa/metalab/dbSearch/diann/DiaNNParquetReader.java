package bmi.med.uOttawa.metalab.dbSearch.diann;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;

public class DiaNNParquetReader {

	private DiaNNPrecursor[] diaNNPrecursors;

	public static HashSet<String> getPeptideSet(String parquet) throws IOException {
		System.out.println("Reading parquet file " + parquet + "......");
		HashSet<String> set = new HashSet<String>();
		Path path = new Path(parquet);

		HadoopInputFile inputFile = HadoopInputFile.fromPath(path, new Configuration());
		ParquetReadOptions readOptions = ParquetReadOptions.builder().build();
		ParquetFileReader fileReader = ParquetFileReader.open(inputFile, readOptions);

		// Get file schema
		ParquetMetadata metadata = fileReader.getFooter();
		MessageType schema = metadata.getFileMetaData().getSchema();

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
				String sequence = group.getBinary("Stripped.Sequence", 0).toStringUsingUTF8();
				set.add(sequence);
			}
		}

		fileReader.close();

		return set;
	}

	public static HashMap<String, Double> getPeptideMap(String parquet) throws IOException {
		HashMap<String, Double> map = new HashMap<String, Double>();
		System.out.println("Reading parquet file " + parquet + "......");
		Path path = new Path(parquet);

		HadoopInputFile inputFile = HadoopInputFile.fromPath(path, new Configuration());
		ParquetReadOptions readOptions = ParquetReadOptions.builder().build();
		ParquetFileReader fileReader = ParquetFileReader.open(inputFile, readOptions);

		// Get file schema
		ParquetMetadata metadata = fileReader.getFooter();
		MessageType schema = metadata.getFileMetaData().getSchema();

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
				String sequence = group.getBinary("Stripped.Sequence", 0).toStringUsingUTF8();
				double preNormal = group.getFloat("Precursor.Normalised", 0);
				if (map.containsKey(sequence)) {
					map.put(sequence, map.get(sequence) + preNormal);
				} else {
					map.put(sequence, preNormal);
				}
			}
		}

		fileReader.close();

		return map;
	}

	public DiaNNParquetReader(String parquet, String cut) {
		ArrayList<DiaNNPrecursor> list = new ArrayList<DiaNNPrecursor>();
		HashSet<Character> cutSet = new HashSet<Character>();
		HashSet<Character> noCutSet = new HashSet<Character>();
		String[] cuts = cut.split(",");
		for (int i = 0; i < cuts.length; i++) {
			if (cuts[i].startsWith("!*")) {
				noCutSet.add(cuts[i].charAt(2));
			} else {
				cutSet.add(cuts[i].charAt(0));
			}
		}

		try {
			Path path = new Path(parquet);
			System.out.println("Reading parquet file " + parquet + "......");
			HadoopInputFile inputFile = HadoopInputFile.fromPath(path, new Configuration());
			ParquetReadOptions readOptions = ParquetReadOptions.builder().build();
			ParquetFileReader fileReader = ParquetFileReader.open(inputFile, readOptions);

			// Get file schema
			ParquetMetadata metadata = fileReader.getFooter();
			MessageType schema = metadata.getFileMetaData().getSchema();

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
					String run = group.getBinary("Run", 0).toStringUsingUTF8();
					if (run.lastIndexOf(".") > 0) {
						run = run.substring(0, run.lastIndexOf("."));
					}
					String[] proteins = group.getBinary("Protein.Group", 0).toStringUsingUTF8().split(";");
					String modSeq = group.getBinary("Modified.Sequence", 0).toStringUsingUTF8();
					String sequence = group.getBinary("Stripped.Sequence", 0).toStringUsingUTF8();
					int missCleavage = 0;
					for (int j = 0; j < sequence.length() - 1; j++) {
						char aa = sequence.charAt(j);
						char bb = sequence.charAt(j + 1);
						if (cutSet.contains(aa) && !noCutSet.contains(bb)) {
							missCleavage++;
						}
					}
					double precursorMz = group.getFloat("Precursor.Mz", 0);
					int charge = (int) group.getLong("Precursor.Charge", 0);
//					float rt = group.getFloat("RT", 0);
//					float rtStart = group.getFloat("RT.Start", 0);
//					float rtStop = group.getFloat("RT.Stop", 0);
//					float area = group.getFloat("Ms1.Area", 0);
					double preNormal = group.getFloat("Precursor.Normalised", 0);
					double preQuan = group.getFloat("Precursor.Quantity", 0);
					double ms1Normal = group.getFloat("Ms1.Normalised", 0);
					double qvalue = group.getFloat("Q.Value", 0);
					double PEP = group.getFloat("PEP", 0);
					double globalQ = group.getFloat("Global.Q.Value", 0);
					double pgQvalue = group.getFloat("PG.Q.Value", 0);
					double globalPGQvalue = group.getFloat("Global.PG.Q.Value", 0);
					boolean isDecoy = ((int) group.getLong("Decoy", 0)) == 1;
					DiaNNPrecursor diaNNPrecursor = new DiaNNPrecursor(run, proteins, modSeq, sequence, charge,
							missCleavage, precursorMz, ms1Normal, qvalue, PEP, globalQ, pgQvalue, globalPGQvalue,
							preNormal, preQuan, isDecoy);

					list.add(diaNNPrecursor);
				}
			}

			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.diaNNPrecursors = list.toArray(new DiaNNPrecursor[list.size()]);
	}

	public DiaNNPrecursor[] getDiaNNPrecursors() {
		return diaNNPrecursors;
	}

	public DiaNNPrecursor[] getDiaNNPrecursors(double globalQValue) {
		ArrayList<DiaNNPrecursor> list = new ArrayList<DiaNNPrecursor>();
		for (DiaNNPrecursor pp : diaNNPrecursors) {
			if (pp.getGlobalQvalue() < globalQValue) {
				list.add(pp);
			}
		}
		return list.toArray(DiaNNPrecursor[]::new);
	}

	@SuppressWarnings("unused")
	private static void compare(File folder) {
		HashSet<String> prSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(
				new FileReader(new File(folder, folder.getName() + ".pr_matrix.tsv")))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				prSet.add(cs[6]);
			}
			reader.close();
		} catch (IOException e) {

		}

		File parquetFile = new File(folder, folder.getName() + ".parquet");
		DiaNNParquetReader reader = new DiaNNParquetReader(parquetFile.getAbsolutePath(), "K*R*");
		DiaNNPrecursor[] pps = reader.getDiaNNPrecursors();
		HashSet<String> ppSet = new HashSet<String>();
		HashSet<String> trSet = new HashSet<String>();
		for (DiaNNPrecursor pp : pps) {
			ppSet.add(pp.getSeqString());
			if (!pp.getIsDecoy()) {
				trSet.add(pp.getSeqString());
			}
		}

		HashSet<String> allSet = new HashSet<String>();
		allSet.addAll(trSet);
		allSet.addAll(prSet);
		int common = trSet.size() + prSet.size() - allSet.size();
		System.out.println(ppSet.size() + "\t" + prSet.size() + "\t" + common + "\t" + trSet.size());
	}

	@SuppressWarnings("unused")
	private static void matchGenome(String dbFile, String parquetFile) {

		DiaNNParquetReader ppreader = new DiaNNParquetReader(parquetFile, "K*R*");
		DiaNNPrecursor[] pps = ppreader.getDiaNNPrecursors();
		HashSet<String> allPepSet = new HashSet<String>();
		for (DiaNNPrecursor pp : pps) {
			if (pp.getIsDecoy()) {
				allPepSet.add(pp.getSeqString());
			}
		}
		System.out.println("decoy\t" + pps.length + "\t" + allPepSet.size());

		ConcurrentHashMap<String, HashMap<String, HashSet<String>>> genomePepMap = new ConcurrentHashMap<String, HashMap<String, HashSet<String>>>();
		HashSet<String> matchedPepSet = new HashSet<String>();
		ExecutorService executor = Executors.newFixedThreadPool(12);
		File currentFile = new File(dbFile);
		File predictPepFile = new File(currentFile, "predicted");
		File[] files = predictPepFile.listFiles();
		for (File file : files) {
			executor.execute(() -> {
				if (file.getName().endsWith(".predicted.tsv")) {
					try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
						HashMap<String, HashSet<String>> proPepMap = new HashMap<String, HashSet<String>>();
						String pline = reader.readLine();
						while ((pline = reader.readLine()) != null) {
							String[] cs = pline.split("\t");
							if (allPepSet.contains(cs[1])) {
								matchedPepSet.add(cs[1]);
								if (proPepMap.containsKey(cs[0])) {
									proPepMap.get(cs[0]).add(cs[1]);
								} else {
									HashSet<String> pepSet = new HashSet<String>();
									pepSet.add(cs[1]);
									proPepMap.put(cs[0], pepSet);
								}
							}
						}
						if (proPepMap.size() > 0) {
							String preFileName = file.getName();
							String genome = preFileName.substring(0, preFileName.length() - ".predicted.tsv".length());
							genomePepMap.put(genome, proPepMap);
						}
					} catch (IOException e) {

					}
				}
			});
		}

		executor.shutdown();

		try {
			if (executor.awaitTermination(1, TimeUnit.HOURS)) {

				allPepSet.clear();
				allPepSet.addAll(matchedPepSet);

			} else {
			}
		} catch (InterruptedException e) {
		}

		System.out.println("decoy\t" + genomePepMap.size() + "\t" + matchedPepSet.size());
	}
	
	public static void main(String[] args) throws IOException {
		DiaNNParquetReader.matchGenome("Z:\\Kai\\Database\\human-gut\\v2.0.2", 
				"E:\\20250423\\20250422_MetaLAB_DIA_search\\selfModel\\mag_result\\combined.parquet");
	}

}
