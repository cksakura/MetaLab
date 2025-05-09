/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class CGF2MGF {

	private static Logger LOGGER = LogManager.getLogger();

	private static final int maxSpCount = 100000;

	public static int convert2Mgf(String[] fileNames, String cgf, String clusteredMgf, boolean singleFile)
			throws IOException {
		return convert2Mgf(fileNames, new File(cgf), new File(clusteredMgf), singleFile);
	}

	public static int convert2Mgf(String[] fileNames, File cgf, File clusteredMgf, boolean singleFile)
			throws IOException {

		int totalCount = 0;

		HashMap<String, HashSet<String>> specMap = new HashMap<String, HashSet<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(cgf));

		Pattern patternTitle = Pattern.compile(".*file=(.+).*\\#id=index=([\\w]+).*title=(.+)");

		boolean begin = false;
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("BEGIN CLUSTER")) {
				begin = true;
			} else {
				if (begin) {
					Matcher matcher = patternTitle.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 3) {
							String file = matcher.group(1);
							String title = matcher.group(3);
							if (specMap.containsKey(file)) {
								specMap.get(file).add(title);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(title);
								specMap.put(file, set);
							}

							totalCount++;
							begin = false;
						}
					}
				}
			}
		}
		reader.close();

		if (singleFile || totalCount < maxSpCount) {

			PrintWriter writer = new PrintWriter(clusteredMgf);

			for (int i = 0; i < fileNames.length; i++) {
				BufferedReader mgfReader = new BufferedReader(new FileReader(fileNames[i]));
				HashSet<String> scanSet = specMap.get(fileNames[i]);
				if (scanSet == null) {
					continue;
				}

				boolean start = false;
				String mgfLine = null;
				while ((mgfLine = mgfReader.readLine()) != null) {
					if (mgfLine.startsWith("TITLE=")) {
						String title = mgfLine.substring("TITLE=".length());
						if (scanSet.contains(title)) {
							writer.println("BEGIN IONS");
							writer.println(mgfLine);
							start = true;
						}
					} else {
						if (start) {
							writer.println(mgfLine);
							if (mgfLine.startsWith("END IONS")) {
								start = false;
							}
						}
					}
				}
				mgfReader.close();
			}

			writer.close();

		} else {
			int mgfCount = (int) ((double) totalCount / (double) maxSpCount) + 1;
			PrintWriter[] writers = new PrintWriter[mgfCount];
			writers[0] = new PrintWriter(clusteredMgf);

			for (int i = 1; i < writers.length; i++) {
				File newMgf = new File(clusteredMgf.getParentFile(),
						clusteredMgf.getName().substring(0, clusteredMgf.getName().length() - 4) + "_" + i + ".mgf");
				writers[i] = new PrintWriter(newMgf);
			}

			for (int i = 0; i < fileNames.length; i++) {
				BufferedReader mgfReader = new BufferedReader(new FileReader(fileNames[i]));
				HashSet<String> scanSet = specMap.get(fileNames[i]);

				int currentFileId = i % writers.length;

				boolean start = false;
				String mgfLine = null;
				while ((mgfLine = mgfReader.readLine()) != null) {
					if (mgfLine.startsWith("TITLE=")) {
						String title = mgfLine.substring("TITLE=".length());
						if (scanSet.contains(title)) {
							writers[currentFileId].println("BEGIN IONS");
							writers[currentFileId].println(mgfLine);
							start = true;
						}
					} else {
						if (start) {
							writers[currentFileId].println(mgfLine);
							if (mgfLine.startsWith("END IONS")) {
								start = false;
							}
						}
					}
				}
				mgfReader.close();
			}

			for (int i = 0; i < writers.length; i++) {
				writers[i].close();
			}
		}

		return totalCount;
	}

	/**
	 * 
	 * @param cgf
	 * @return
	 */
	public static HashMap<String, HashSet<String>> getSpectraMap(String cgf) {
		HashMap<String, HashSet<String>> specMap = new HashMap<String, HashSet<String>>();
		Pattern patternTitle = Pattern.compile(".*file=(.+).*\\#id=index=([\\w]+).*title=(.+)");
		boolean begin = false;
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(cgf))) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("BEGIN CLUSTER")) {
					begin = true;
				} else {
					if (begin) {
						Matcher matcher = patternTitle.matcher(line);
						if (matcher.matches()) {
							int groupcount = matcher.groupCount();
							if (groupcount == 3) {
								String file = matcher.group(1);
								String title = matcher.group(3);
								if (specMap.containsKey(file)) {
									specMap.get(file).add(title);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(title);
									specMap.put(file, set);
								}
							}

							begin = false;
						}
					}
				}
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading cluster file " + cgf, e);
		}

		return specMap;
	}

	public static int convertSingleFile(String cgf, String mgf) {
		return convertSingleFile(new File(cgf), new File(mgf));
	}

	public static int convertSingleFile(File cgf, File mgf) {
		Pattern patternCluster = Pattern.compile(".*Id=([\\w-]+).*Charge=([\\d]+).*");
		Pattern patternConsensus = Pattern.compile(".*nSpec=([\\d]+).*SumCharge=([\\d]+).*SumMz=([E\\d\\.]+)");

		StringBuilder sb = null;
		String scanname = null;
		int charge = -1;
		double premz = -1;

		int totalSpectraCount = 0;
		boolean begin = false;
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(cgf));
				PrintWriter writer = new PrintWriter(mgf)) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("BEGIN CLUSTER")) {
					Matcher matcher = patternCluster.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 2) {
							scanname = matcher.group(1);
							charge = Integer.parseInt(matcher.group(2));
						}
					}
				} else if (line.startsWith("BEGIN CONSENSUS")) {
					Matcher matcher = patternConsensus.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 3) {
							begin = true;

							int count = Integer.parseInt(matcher.group(1));
							double totalMz = Double.parseDouble(matcher.group(3));

							premz = totalMz / (double) count;

							sb = new StringBuilder();
							sb.append("BEGIN IONS\n");
							sb.append("TITLE=" + scanname + "\n");
							sb.append("PEPMASS=" + premz + "\n");
							sb.append("CHARGE=" + charge + "+\n");

							totalSpectraCount++;
						}
					}
				} else if (line.startsWith("END CONSENSUS")) {
					begin = false;
					if (sb != null) {
						sb.append("END IONS\n");
						writer.print(sb);
					}
				} else {
					if (begin && sb != null) {
						sb.append(line).append("\n");
					}
				}
			}

			reader.close();
			writer.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing mgf spectra file " + mgf, e);
		}

		return totalSpectraCount;
	}

	/**
	 * Used for X!Tandem search, because the result file of X!Tandem is xml format,
	 * don't want it boo big
	 * 
	 * @param cgf
	 * @param mgf
	 */
	public static int convertMultiFile(String cgf, String mgf) {
		return convertMultiFile(new File(cgf), new File(mgf));
	}

	public static int convertMultiFile(File cgf, File mgf) {
		Pattern patternCluster = Pattern.compile(".*Id=([\\w-]+).*Charge=([\\d]+).*");
		Pattern patternConsensus = Pattern.compile(".*nSpec=([\\d]+).*SumCharge=([\\d]+).*SumMz=([E\\d\\.]+)");

		StringBuilder sb = null;
		String scanname = null;
		int charge = -1;
		double premz = -1;

		int totalSpectraCount = 0;
		boolean begin = false;
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(cgf))) {
			PrintWriter writer = new PrintWriter(mgf);
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("BEGIN CLUSTER")) {
					Matcher matcher = patternCluster.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 2) {
							scanname = matcher.group(1);
							charge = Integer.parseInt(matcher.group(2));
						}
					}
				} else if (line.startsWith("BEGIN CONSENSUS")) {
					Matcher matcher = patternConsensus.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 3) {
							begin = true;

							int count = Integer.parseInt(matcher.group(1));
							double totalMz = Double.parseDouble(matcher.group(3));

							premz = totalMz / (double) count;

							sb = new StringBuilder();
							sb.append("BEGIN IONS\n");
							sb.append("TITLE=" + scanname + "\n");
							sb.append("PEPMASS=" + premz + "\n");
							sb.append("CHARGE=" + charge + "+\n");

							totalSpectraCount++;

							if (totalSpectraCount % 100000 == 0) {
								writer.close();

								int id = totalSpectraCount / 100000;
								File newMgf = new File(mgf.getParentFile(),
										mgf.getName().substring(0, mgf.getName().length() - 4) + "_" + id + ".mgf");
								writer = new PrintWriter(newMgf);
							}
						}
					}
				} else if (line.startsWith("END CONSENSUS")) {
					begin = false;
					if (sb != null) {
						sb.append("END IONS\n");
						writer.print(sb);
					}

				} else {
					if (begin && sb != null) {
						sb.append(line).append("\n");
					}
				}
			}

			reader.close();
			writer.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing mgf spectra file " + mgf, e);
		}

		return totalSpectraCount;
	}

	public static int convertMultiCharge(String cgf, String mgf) {
		Pattern patternCluster = Pattern.compile(".*Id=([\\w-]+).*Charge=([\\d]+).*");
		Pattern patternConsensus = Pattern.compile(".*nSpec=([\\d]+).*SumCharge=([\\d]+).*SumMz=([E\\d\\.]+)");

		StringBuilder sb = null;
		String scanname = null;
		int charge = -1;
		double premz = -1;

		int totalSpectraCount = 0;
		boolean begin = false;
		String line = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(cgf));
				PrintWriter writer = new PrintWriter(mgf)) {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("BEGIN CLUSTER")) {
					Matcher matcher = patternCluster.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 2) {
							scanname = matcher.group(1);
							charge = Integer.parseInt(matcher.group(2));
						}
					}
				} else if (line.startsWith("BEGIN CONSENSUS")) {
					Matcher matcher = patternConsensus.matcher(line);
					if (matcher.matches()) {
						int groupcount = matcher.groupCount();
						if (groupcount == 3) {
							begin = true;

							int count = Integer.parseInt(matcher.group(1));
							double totalMz = Double.parseDouble(matcher.group(3));

							premz = totalMz / (double) count;

							sb = new StringBuilder();
							sb.append("BEGIN IONS\n");
							sb.append("TITLE=" + scanname + "\n");
							sb.append("PEPMASS=" + premz + "\n");
							sb.append("CHARGE=" + charge + "+\n");

							totalSpectraCount++;
						}
					}
				} else if (line.startsWith("END CONSENSUS")) {
					begin = false;
					if (sb != null) {
						sb.append("END IONS\n");
						writer.print(sb);
					}
				} else {
					if (begin && sb != null) {
						sb.append(line).append("\n");
					}
				}
			}

			reader.close();
			writer.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing mgf spectra file " + mgf, e);
		}

		return totalSpectraCount;
	}
}
