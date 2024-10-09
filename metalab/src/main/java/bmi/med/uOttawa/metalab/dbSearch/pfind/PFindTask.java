/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;

/**
 * @author Kai Cheng
 *
 */
public class PFindTask {

	protected static String taskName = "pFind task";
	protected static Logger LOGGER = LogManager.getLogger(PFindTask.class);
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File pFindBin;
	protected int totalThread = 1;
	private int threadPool = 1;

	protected HashMap<String, Boolean> pfindTaskMap;
	private HashMap<String, PostTransModification> ptmMap;

	private static final String xtract = "xtract_raw.exe";
	private static final String xtract_par = "-ms -a -i 1 -m 5 -o";
	private static final String pParser = "pParse.exe";
	private static final String pSearcher = "Searcher.exe";

	public PFindTask(File pFindBin) {
		this.pFindBin = pFindBin;
		this.totalThread = 1;
		this.threadPool = 1;
		this.pfindTaskMap = new HashMap<String, Boolean>();
	}

	public PFindTask(File pFindBin, int threadPool) {
		this.pFindBin = pFindBin;
		this.totalThread = 1;
		this.threadPool = threadPool;
		this.pfindTaskMap = new HashMap<String, Boolean>();
	}

	public void setTotalThread(int totalThread) {
		this.totalThread = totalThread;
	}

	public HashMap<String, PostTransModification> getPtmMap() {
		if (ptmMap == null) {
			ptmMap = new HashMap<String, PostTransModification>();
			File defaultFolder = new File(pFindBin, "default");
			File modification_default = new File(defaultFolder, "modification_default.ini");
			if (modification_default.exists()) {
				PFindPTMReader reader = new PFindPTMReader(modification_default);
				this.ptmMap = reader.getModMap();
			}
		}
		return ptmMap;
	}

	/**
	 * generate mgf spectra from the raw files by xtract_raw.exe and pParse.exe in
	 * pFind
	 * 
	 * @param raw
	 * 
	 */
	public void extract(String... extractRaws) {
		if (extractRaws.length > 0) {
			this.extract(1, (new File(extractRaws[0])).getParent(), extractRaws);
		}
	}

	/**
	 * generate mgf spectra from the raw files by xtract_raw.exe and pParse.exe in
	 * pFind
	 * 
	 * 
	 * @param raw
	 * 
	 */
	public void extract(String spectraDir, String... extractRaws) {
		this.extract(1, spectraDir, extractRaws);
	}

	/**
	 * 
	 * @param co_elute    0, output single precursor for single scan; 1, output all
	 *                    co-eluted precursors
	 * @param spectraDir
	 * @param extractRaws
	 * 
	 */
	public void extract(int co_elute, String spectraDir, String... extractRaws) {

		LOGGER.info(taskName + ": extract spectra from raw file");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file");

		for (String raw : extractRaws) {

			File rawFile = new File(raw);
			String rawName = rawFile.getName().substring(0, rawFile.getName().length() - ".raw".length());

			File batFile = new File(spectraDir, rawName + ".pParse.bat");
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(batFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": extract spectra failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": extract spectra failed");
			}

			writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
			writer.println("cd " + pFindBin);

			String cmd = xtract + " " + xtract_par + " \"" + spectraDir + "\" \"" + raw + "\"";
			writer.println(cmd);

			PFindConfig config = new PFindConfig(pFindBin);

			File conf = null;

			try {
				conf = config.extractSingle(co_elute, spectraDir, raw, rawName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in configuration, extract spectra failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in configuration, extract spectra failed");
			}

			writer.println(pParser + " " + conf);
			writer.close();

			this.pfindTaskMap.put(batFile.getAbsolutePath(), false);
		}
	}

	/**
	 * 
	 * @param spectraDir
	 * @param rawName
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void ms2ToMgf(String spectraDir, String rawName) throws NumberFormatException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(new File(spectraDir, rawName + ".ms2")));
		PrintWriter writer = new PrintWriter(new File(spectraDir, rawName + ".mgf"));

		String line = null;
		int scannum = -1;
		int charge = -1;
		double preMz = -1;
		double rt = -1;

		while ((line = reader.readLine()) != null) {

			char c0 = line.charAt(0);
			String[] cs = line.split("[\\s+]");

			switch (c0) {
			case 'H':
				break;

			case 'S':
				if (scannum != -1) {
					writer.println("END IONS");
				}
				scannum = Integer.parseInt(cs[1]);
				break;

			case 'I':

				if (cs[1].equals("MonoiosotopicMz")) {
					preMz = Double.parseDouble(cs[2]);
				} else if (cs[1].equals("RetTime")) {
					rt = Double.parseDouble(cs[2]);
				}

				break;
			case 'Z':
				charge = Integer.parseInt(cs[1]);

				writer.println("BEGIN IONS");
				writer.println("TITLE=" + rawName + "." + scannum + "." + scannum + "." + charge);
				writer.println("RTINSECONDS=" + rt);
				writer.println("PEPMASS=" + preMz);
				writer.println("CHARGE=" + charge + "+");

				break;

			default:

				if (cs.length == 2) {
					writer.println(line);
				}

				break;
			}

		}

		writer.println("END IONS");

		reader.close();
		writer.close();
	}

	/**
	 * <p>
	 * Generate mgf spectra from the raw files by xtract_raw.exe and pParse.exe in
	 * pFind
	 * <p>
	 * Add all the raw files in one pParse.cfg to execute will be slow because only
	 * one thread was used
	 * <p>
	 * So this method is not used now, please extract each raw file <b>separately<b>
	 * 
	 * @deprecated
	 */
	public void extractCombine(int co_elute, String spectraDir, String... extractRaws) {

		LOGGER.info(taskName + ": extract spectra from raw file");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file");

		File batFile = new File(spectraDir, "ms2Spctra.pParse.bat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(batFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": extract spectra failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": extract spectra failed");
		}

		writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
		writer.println("cd " + pFindBin);

		for (String exRaw : extractRaws) {
			String cmd = xtract + " " + xtract_par + " \"" + spectraDir + "\" \"" + exRaw + "\"";
			writer.println(cmd);
		}

		PFindConfig config = new PFindConfig(pFindBin);

		File conf = null;

		try {
			conf = config.extract(co_elute, spectraDir, extractRaws);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in configuration, extract spectra failed", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in configuration, extract spectra failed");
		}

		writer.println(pParser + " " + conf);
		writer.close();

		this.pfindTaskMap.put(batFile.getAbsolutePath(), false);

		LOGGER.info(taskName + ": extract spectra from raw file finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file finished");
	}

	/**
	 * search the peptides against the protein database by Searcher.exe in pFind a
	 * Batch file (.bat) is needed for the batch process, which use a "cd" command
	 * to jump to the pFind execute directory first
	 * 
	 * @param raw
	 * @param result
	 * @param db
	 * @param ms2Type
	 * @param isOpenSearch
	 */
	public void search(String raw, String result, String db, String ms2Type, boolean isOpenSearch, boolean isAutoRev,
			boolean interrupt) {

		File rawFile = new File(raw);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".pFind.bat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(batFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
		writer.println("cd " + pFindBin);

		PFindConfig config = new PFindConfig(pFindBin);
		config.setCoreThread(1, totalThread);

		File conf = null;

		if (raw.endsWith("raw")) {
			conf = config.searchRaw(raw, result, db, ms2Type, isOpenSearch, isAutoRev);
		} else if (raw.endsWith("mgf")) {
			conf = config.searchMgf(raw, result, db, ms2Type, isOpenSearch, isAutoRev);
		}

		if (conf == null) {
			return;
		}

		writer.println(pSearcher + " " + conf);
		writer.close();

		this.pfindTaskMap.put(batFile.getAbsolutePath(), interrupt);
	}

	/**
	 * search the peptides against the protein database by Searcher.exe in pFind a
	 * Batch file (.bat) is needed for the batch process, which use a "cd" command
	 * to jump to the pFind execute directory first
	 * 
	 * @param raws
	 * @param result
	 * @param db
	 * @param ms2Type
	 * @param isMgf
	 * @param isOpenSearch
	 */
	public void search(String[] raws, String result, String db, String ms2Type, boolean isMgf, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, boolean interrupt) {

		File rawFile = new File(raws[0]);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".pFind.bat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(batFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
		writer.println("cd " + pFindBin);

		PFindConfig config = new PFindConfig(pFindBin);
		config.setCoreThread(totalThread, 1);

		File conf = null;

		if (isMgf) {
			conf = config.searchMgf(raws, result, db, ms2Type, isOpenSearch, isAutoRev, psmFdr);
		} else {
			conf = config.searchRaw(raws, result, db, ms2Type, isOpenSearch, isAutoRev, psmFdr);
		}

		if (conf == null) {
			return;
		}

		writer.println(pSearcher + " " + conf);
		writer.close();

		this.pfindTaskMap.put(batFile.getAbsolutePath(), interrupt);
	}

	/**
	 * search the peptides against the protein database by Searcher.exe in pFind a
	 * Batch file (.bat) is needed for the batch process, which use a "cd" command
	 * to jump to the pFind execute directory first
	 * 
	 * @param raws
	 * @param result
	 * @param db
	 * @param ms2Type
	 * @param isMgf
	 * @param isOpenSearch
	 */
	public void searchMgf(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestMode, IsobaricTag tag, boolean interrupt) {

		File rawFile = new File(raws[0]);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".pFind.bat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(batFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
		writer.println("cd " + pFindBin);

		PFindConfig config = new PFindConfig(pFindBin);

		int multipCount = totalThread > raws.length ? raws.length : totalThread;
		int threadCount = totalThread > raws.length ? totalThread / raws.length : 1;

		config.setCoreThread(multipCount, threadCount);

		File conf = config.searchMgf(raws, result, db, ms2Type, isOpenSearch, isAutoRev, psmFdr, fixedMods, variMods,
				enzyme, missCleavages, digestMode, tag);

		if (conf == null) {
			return;
		}

		writer.println(pSearcher + " " + conf);
		writer.close();

		this.pfindTaskMap.put(batFile.getAbsolutePath(), interrupt);
	}

	/**
	 * search the peptides against the protein database by Searcher.exe in pFind a
	 * Batch file (.bat) is needed for the batch process, which use a "cd" command
	 * to jump to the pFind execute directory first
	 * 
	 * @param raws
	 * @param result
	 * @param db
	 * @param ms2Type
	 * @param isMgf
	 * @param isOpenSearch
	 */
	public void searchPF2(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestMode, IsobaricTag tag) {
		searchPF2(raws, result, db, ms2Type, isOpenSearch, isAutoRev, psmFdr, fixedMods, variMods, enzyme,
				missCleavages, digestMode, tag, false);
	}

	/**
	 * search the peptides against the protein database by Searcher.exe in pFind a
	 * Batch file (.bat) is needed for the batch process, which use a "cd" command
	 * to jump to the pFind execute directory first
	 * 
	 * @param raws
	 * @param result
	 * @param db
	 * @param ms2Type
	 * @param isMgf
	 * @param isOpenSearch
	 * @param interrupt
	 */
	public void searchPF2(String[] raws, String result, String db, String ms2Type, boolean isOpenSearch,
			boolean isAutoRev, double psmFdr, String[] fixedMods, String[] variMods, String enzyme, int missCleavages,
			int digestMode, IsobaricTag tag, boolean interrupt) {

		File rawFile = new File(raws[0]);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".pFind.bat");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(batFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		writer.println(pFindBin.getAbsolutePath().split("\\\\")[0]);
		writer.println("cd " + pFindBin);

		PFindConfig config = new PFindConfig(pFindBin);

		int multipCount = totalThread > raws.length ? raws.length : totalThread;
		int threadCount = totalThread > raws.length ? totalThread / raws.length : 1;

		config.setCoreThread(multipCount, threadCount);

		File conf = config.searchPF2(raws, result, db, ms2Type, isOpenSearch, isAutoRev, psmFdr, fixedMods, variMods,
				enzyme, missCleavages, digestMode, tag);

		if (conf == null) {
			return;
		}

		writer.println(pSearcher + " " + conf);
		writer.close();

		this.pfindTaskMap.put(batFile.getAbsolutePath(), interrupt);
	}

	public static void cleanPFindResultFolder(File in, boolean deleteRes) {
		File[] listFiles = in.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].isFile()) {
				String name = listFiles[i].getName();
				if (name.equals("1.aa")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.equals("1.mod")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.equals("modification.ini")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.endsWith(".sub.fasta")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.equals("backup_pFind.cfg")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.endsWith("pFind.bat")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (deleteRes) {
					if (name.endsWith("qry.res")) {
						FileUtils.deleteQuietly(listFiles[i]);
					}
				}

				if (name.endsWith("_td.fasta")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.endsWith("_td.pac")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}

				if (name.endsWith("_td.pde")) {
					FileUtils.deleteQuietly(listFiles[i]);
				}
			}
		}
	}

	public void run(int waitHours) {
		run(this.threadPool, waitHours);
	}

	public void run(int threadPool, int waitHours) {

		if (this.pfindTaskMap.size() == 0) {
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadPool);
		Iterator<String> it = this.pfindTaskMap.keySet().iterator();

		while (it.hasNext()) {

			String batString = it.next();
			boolean interrupt = this.pfindTaskMap.get(batString);

			it.remove();

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {
						ProcessBuilder pb = new ProcessBuilder(batString);
						Process p = pb.start();
						BufferedInputStream in = new BufferedInputStream(p.getInputStream());
						BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
						String line0 = "";
						String line1 = "";
						while ((line1 = inBr.readLine()) != null) {
							if (!line1.equals(line0)) {
								System.out.println(format.format(new Date()) + "\t" + line1);
								line0 = line1;
							}

							if (interrupt) {
								if (line1.startsWith("[pFind] <Protein Inference>")) {
									p.destroy();

									String group = "Group.exe";
									String groupCmd = "taskkill /F /IM " + group;

									try {
										Process pGroup = Runtime.getRuntime().exec(groupCmd);
										BufferedInputStream inGroup = new BufferedInputStream(pGroup.getInputStream());
										BufferedReader inBrGroup = new BufferedReader(new InputStreamReader(inGroup));
										String lineStr;
										while ((lineStr = inBrGroup.readLine()) != null) {
											if (lineStr.startsWith("SUCCESS")) {
												System.out.println(lineStr);
											}
										}
										if (p.waitFor() != 0) {
											if (p.exitValue() == 1)
												System.err.println("false");
										}
										inBrGroup.close();
										inBrGroup.close();
									} catch (Exception e) {
										e.printStackTrace();
									}

									break;
								}
							}
						}

						if (p.waitFor() != 0) {
							if (p.exitValue() == 1)
								LOGGER.error("false");
						}
						inBr.close();
						in.close();

					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						LOGGER.error(e);
					}
				}
			});
		}

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(waitHours, TimeUnit.HOURS);

			this.pfindTaskMap = new HashMap<String, Boolean>();

			if (finish) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart  MetaLab after the pFind task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the pFind task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}
	
	public static void main(String[] args) {
		PFindTask task = new PFindTask(new File("E:\\Exported\\Resources\\DBReducer\\pFind3\\bin"));
		task.extract(new String[] {"Z:\\Kai\\Raw_files\\20231214\\20230802_164941_OTOT_293T_500ng_FAIMSDDA_76min_gas4_-43V_-63V_7O5k_AGC400_IT27ms_F2_R4.raw"});
	}
}
