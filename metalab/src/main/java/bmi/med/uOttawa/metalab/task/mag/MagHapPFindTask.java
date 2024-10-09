package bmi.med.uOttawa.metalab.task.mag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.dbReducer.DBReducerTask;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPsmTsvWriter;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindIsobaricTask;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindProtein;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPsmReader;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindTask;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader.PeptideResult;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqPsmReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqPsmReader.FlashLfqPsm;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPepReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPeptide;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqTask;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.hgm.HMGenomeHtmlWriter;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLReader2;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinXMLWriter2;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParaIOMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesIoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.pfind.MetaIdenPFindTask;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

public class MagHapPFindTask extends MetaIdenPFindTask {

	protected MagDbItem magDb;
	protected File[] separateResultFolders;
	protected int threadCount;

	protected double psmCoverThreshold = 0.95;

	protected static final Logger LOGGER = LogManager.getLogger(MagHapPFindTask.class);
	protected static final String taskName = "pFind HAP workflow";

	public MagHapPFindTask(MetaParameterMag metaPar, MetaSourcesMag msv, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv, bar1, bar2, nextWork);
	}
	
	protected void initial() {
		this.metadata = metaPar.getMetadata();
		this.magDb = ((MetaParameterMag) metaPar).getUsedMagDbItem();
		this.threadCount = metaPar.getThreadCount();

		this.mosp = ((MetaParameterPFind) metaPar).getMop();
		this.ms2Type = ((MetaParameterPFind) metaPar).getMs2ScanMode();
		this.quanMode = ((MetaParameterPFind) metaPar).getQuanMode();

		File pFindBin = (new File(((MetaSourcesV2) msv).getpFind())).getParentFile();
		this.pFindTask = new PFindTask(pFindBin, metaPar.getThreadCount());
		this.ptmMap = pFindTask.getPtmMap();

		this.dbReducerExe = new File(((MetaSourcesV2) msv).getDbReducer());
		if (dbReducerExe.exists() && ms2Type.equals(MetaConstants.HCD_FTMS)
				&& ((MetaParameterPFind) metaPar).getQuanMode().equals(MetaConstants.labelFree)) {
			useDBReducer = true;
			dbReducerTask = new DBReducerTask(dbReducerExe);
		} else {
			useDBReducer = false;
		}

		this.resultFolderFile = metaPar.getDbSearchResultFile();
		this.expNameMap = new HashMap<String, String>();

		if (metadata != null) {
			this.rawFiles = metadata.getRawFiles();
			this.expNames = metadata.getExpNames();
			this.quanExpNames = new String[rawFiles.length];
			this.fractions = metadata.getFractions();

			if (rawFiles != null && rawFiles.length > 0) {
				spConvert = true;
				for (int i = 0; i < rawFiles.length; i++) {
					this.quanExpNames[i] = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1,
							rawFiles[i].lastIndexOf("."));
					this.expNameMap.put(quanExpNames[i], expNames[i]);

					if (rawFiles[i].endsWith(".mzML") || rawFiles[i].endsWith(".mzml")) {
						this.isMzML = true;
					} else {
						this.isMzML = false;
					}

					if (rawFiles[i].endsWith(".d") || rawFiles[i].endsWith(".D")) {
						this.isBrukD = true;
					} else {
						this.isBrukD = false;
					}
				}
				this.rawDir = (new File(rawFiles[0])).getParentFile();

				if (this.ms2Type.equals(MetaConstants.HCD_FTMS)) {
					this.suffix = "_HCDFT.pf2";
					this.isOpenSearch = true;
				} else if (this.ms2Type.equals(MetaConstants.HCD_ITMS)) {
					this.suffix = "_HCDIT.pf2";
					this.isOpenSearch = false;
				} else if (this.ms2Type.equals(MetaConstants.CID_FTMS)) {
					this.suffix = "_CIDFT.pf2";
					this.isOpenSearch = false;
				} else if (this.ms2Type.equals(MetaConstants.CID_ITMS)) {
					this.suffix = "_CIDIT.pf2";
					this.isOpenSearch = false;
				}
			} else {
				spConvert = false;
			}
		}

		setTaskCount();
	}
	
	public MetaParameter getMetaParameter() {
		return super.getMetaParameter();
	}
	
	protected boolean separateSearchHap() {

		int taskCount = 0;
		this.separateResultFolders = new File[rawFiles.length];
		for (int i = 0; i < rawFiles.length; i++) {
			String name = rawFiles[i].substring(rawFiles[i].lastIndexOf("\\") + 1, rawFiles[i].lastIndexOf("."));

			this.separateResultFolders[i] = new File(resultFolderFile, name);
			if (!this.separateResultFolders[i].exists()) {
				this.separateResultFolders[i].mkdir();
			}

			File hapFile = new File(separateResultFolders[i], "hap");
			if (!hapFile.exists()) {
				hapFile.mkdirs();
			}

			PFindResultReader reader = new PFindResultReader(hapFile, null);
			if (reader.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": pFind search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": pFind search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			} else {
				taskCount++;
			}
		}

		if (taskCount > 0) {
			int totalThreadCount = metaPar.getThreadCount();
			int threadCount = totalThreadCount > taskCount ? totalThreadCount / taskCount : 1;
			int threadPoolCount = totalThreadCount > taskCount ? taskCount : totalThreadCount;
			String mgfsuffix = suffix.replace("pf2", "mgf");

			if (this.useDBReducer) {
				dbReducerTask.setTotalThread(threadCount);
				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File hapFile = new File(separateResultFolders[i], "hap");
					File mgf = new File(spectraDirFile, separateResultFolders[i].getName() + mgfsuffix);

					if (!mgf.exists()) {
						mgf = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
					}

					if (mgf.exists()) {
						dbReducerTask.searchMgf(new String[] { mgf.getAbsolutePath() }, hapFile.getAbsolutePath(),
								this.magDb.getHapFastaFile().getAbsolutePath(), ms2Type, mosp.getOpenPsmFDR(),
								((MetaParameterMag) this.metaPar).getFixMods(),
								((MetaParameterMag) this.metaPar).getVariMods());
					} else {

						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra file " + mgf
								+ " was not found");

						LOGGER.error(getTaskName() + ": spectra file " + mgf + " was not found");

						return false;
					}
				}
				dbReducerTask.run(threadPoolCount, separateResultFolders.length * 3);

			} else {
				pFindTask.setTotalThread(threadCount);

				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File hapFile = new File(separateResultFolders[i], "hap");
					File pf2 = new File(spectraDirFile, separateResultFolders[i].getName() + suffix);
					if (pf2.exists()) {
						pFindTask.searchPF2(new String[] { pf2.getAbsolutePath() }, hapFile.getAbsolutePath(),
								this.magDb.getHapFastaFile().getAbsolutePath(), ms2Type, isOpenSearch, true,
								mosp.getOpenPsmFDR(), ((MetaParameterMag) this.metaPar).getFixMods(),
								((MetaParameterMag) this.metaPar).getVariMods(),
								((MetaParameterMag) this.metaPar).getEnzyme(),
								((MetaParameterMag) this.metaPar).getMissCleavages(),
								((MetaParameterMag) this.metaPar).getDigestMode(),
								((MetaParameterMag) this.metaPar).getIsobaricTag());
					} else {

						File mgFile = new File(spectraDirFile, separateResultFolders[i].getName() + mgfsuffix);
						if (!mgFile.exists()) {
							mgFile = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
						}

						if (mgFile.exists()) {
							pFindTask.searchMgf(new String[] { mgFile.getAbsolutePath() }, hapFile.getAbsolutePath(),
									this.magDb.getHapFastaFile().getAbsolutePath(), ms2Type, isOpenSearch, true,
									mosp.getOpenPsmFDR(), ((MetaParameterMag) this.metaPar).getFixMods(),
									((MetaParameterMag) this.metaPar).getVariMods(),
									((MetaParameterMag) this.metaPar).getEnzyme(),
									((MetaParameterMag) this.metaPar).getMissCleavages(),
									((MetaParameterMag) this.metaPar).getDigestMode(),
									((MetaParameterMag) this.metaPar).getIsobaricTag(), false);
						} else {
							System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra file " + pf2
									+ " or " + mgFile + " was not found");

							LOGGER.error(getTaskName() + ": spectra file " + pf2 + " or " + mgFile + " was not found");

							return false;
						}
					}
				}
				pFindTask.run(threadPoolCount, separateResultFolders.length * 3);
			}
		}
		
		FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().equals("pFind-Filtered.spectra");
            }
        };

		// The WatchService for monitoring the folders
		WatchService watchService = null;
		// Create the WatchService that monitors the folders
		try {
			watchService = FileSystems.getDefault().newWatchService();
			// Register each folder with the watch service
			for (File folder : separateResultFolders) {
				File hapFile = new File(folder, "hap");
				Path path = hapFile.toPath();
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
			}
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in create a WatchService for separate search");

			LOGGER.error(getTaskName() + ": error in create a WatchService for separate search", e);
		}
		
		new Thread(new WatchServiceLoop(watchService, filter, taskCount, separateResultFolders.length, 10, 38)).start();

		for (int i = 0; i < this.separateResultFolders.length; i++) {
			File hapFile = new File(separateResultFolders[i], "hap");
			PFindTask.cleanPFindResultFolder(hapFile, true);

			PFindResultReader reader = new PFindResultReader(hapFile, null);
			if (!reader.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": pFind search result was not found from " + hapFile.getAbsolutePath());

				LOGGER.error(getTaskName() + ": pFind search result was not found from " + hapFile.getAbsolutePath());

				return false;
			}
		}

		return true;
	}

	/**
	 * host database is combined in this step
	 * @throws IOException
	 */
	protected boolean fastaExtract4Iterator() {

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			File hapFile = new File(separateResultFolders[i], "hap");
			File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");

			if (dbFile.exists() && dbFile.length() > 0) {

				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": sample-specific database for "
						+ separateResultFolders[i].getName() + " already exists in " + dbFile.getAbsolutePath());

				LOGGER.info(getTaskName() + ": sample-specific database for " + separateResultFolders[i].getName()
						+ " already exists in " + dbFile.getAbsolutePath());

				continue;
			}

			File spectraFile = new File(hapFile, "pFind-Filtered.spectra");

			HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
			HashSet<String> pepSet = new HashSet<String>();

			try (BufferedReader hapReader = new BufferedReader(new FileReader(spectraFile))) {
				String line = hapReader.readLine();
				while ((line = hapReader.readLine()) != null) {
					String[] cs = line.split("\t");
					String[] pros = cs[12].split("/");
					for (int j = 0; j < pros.length; j++) {
						if (pros[j].startsWith(magDb.getIdentifier())) {
							String genome = pros[j].split("_")[0];
							if (genomeMap.containsKey(genome)) {
								genomeMap.get(genome).add(cs[0]);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(cs[0]);
								genomeMap.put(genome, set);
							}
						}
					}
					pepSet.add(cs[0]);
				}
				hapReader.close();
			} catch (IOException e) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the pFind search result file from " + spectraFile.getAbsolutePath());

				LOGGER.error(getTaskName() + ": error in reading the pFind search result file from "
						+ spectraFile.getAbsolutePath());

				return false;
			}

			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": start generating sample-specific database for " + separateResultFolders[i].getName());

			LOGGER.info(
					getTaskName() + ": start generating sample-specific database for " + separateResultFolders[i].getName());

			File hapGenomeFile = new File(hapFile, "hap_genomes.tsv");
			HashSet<String> genomeSet = refineGenomes(genomeMap, pepSet.size(), psmCoverThreshold, hapGenomeFile);

			try (PrintWriter writer = new PrintWriter(dbFile)) {
				File dbFolder = this.magDb.getOriginalDbFolder();
				for (String genome : genomeSet) {
					File genomeDbFile = new File(dbFolder, genome + ".faa");
					if (!genomeDbFile.exists()) {
						genomeDbFile = new File(dbFolder, genome + ".fasta");
					}
					if (genomeDbFile.exists()) {
						BufferedReader readeri = new BufferedReader(new FileReader(genomeDbFile));
						String linei = null;
						while ((linei = readeri.readLine()) != null) {
							writer.println(linei);
						}
						readeri.close();
					}
				}

				if (metaPar.isAppendHostDb()) {
					BufferedReader hostReader = new BufferedReader(new FileReader(this.metaPar.getHostDB()));
					String linei = null;
					while ((linei = hostReader.readLine()) != null) {
						writer.println(linei);
					}
					hostReader.close();
				}

				writer.close();
			} catch (IOException e) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing the sample-specific database to " + dbFile.getAbsolutePath());

				LOGGER.error(
						getTaskName() + ": error in writing the sample-specific database to " + dbFile.getAbsolutePath());

				return false;
			}

			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": generating sample-specific database for " + separateResultFolders[i].getName() + " finished");

			LOGGER.info(getTaskName() + ": generating sample-specific database for " + separateResultFolders[i].getName()
					+ " finished");
		}

		return true;
	}

	
	protected boolean separateSearch() {

		int taskCount = 0;
		for (int i = 0; i < this.separateResultFolders.length; i++) {

			PFindResultReader reader = new PFindResultReader(separateResultFolders[i], null);
			if (reader.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": pFind search result already exists in " + separateResultFolders[i].getAbsolutePath());

				LOGGER.info(getTaskName() + ": pFind search result already exists in "
						+ separateResultFolders[i].getAbsolutePath());
			} else {
				File hapFile = new File(separateResultFolders[i], "hap");
				File db = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
				if (!db.exists()) {
					System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": sample-specific database" + db
							+ " was not found");

					LOGGER.error(getTaskName() + ": sample-specific database" + db + " was not found");

					return false;
				}
				taskCount++;
			}
		}

		if (taskCount > 0) {
			int totalThreadCount = metaPar.getThreadCount();
			int threadCount = totalThreadCount > taskCount ? totalThreadCount / taskCount : 1;
			int threadPoolCount = totalThreadCount > taskCount ? taskCount : totalThreadCount;

			if (this.useDBReducer) {
				dbReducerTask.setTotalThread(threadCount);
				String mgfsuffix = suffix.replace("pf2", "mgf");

				for (int i = 0; i < this.separateResultFolders.length; i++) {

					File mgf = new File(spectraDirFile, separateResultFolders[i].getName() + mgfsuffix);

					if (!mgf.exists()) {
						mgf = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
					}

					File dbReducerFile = new File(separateResultFolders[i], "DBReducer");
					if (!dbReducerFile.exists()) {
						dbReducerFile.mkdir();
					}

					if (mgf.exists()) {
						File hapFile = new File(separateResultFolders[i], "hap");
						File db = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
						if (db.length() > 0) {
							dbReducerTask.searchMgf(new String[] { mgf.getAbsolutePath() },
									dbReducerFile.getAbsolutePath(), db.getAbsolutePath(), ms2Type,
									mosp.getOpenPsmFDR(), ((MetaParameterMag) this.metaPar).getFixMods(),
									((MetaParameterMag) this.metaPar).getVariMods());
						} else {
							System.out.println(
									format.format(new Date()) + "\t" + getTaskName() + ": no protein sequences were in " + db
											+ ", which means no peptides/proteins can be identifed from "
											+ separateResultFolders[i].getName());

							LOGGER.error(getTaskName() + ": no protein sequences were in " + db
									+ ", which means no peptides/proteins can be identifed from "
									+ separateResultFolders[i].getName());
						}
					} else {

						System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra file " + mgf
								+ " was not found");

						LOGGER.error(getTaskName() + ": spectra file " + mgf + " was not found");

						return false;
					}
				}
				dbReducerTask.run(threadPoolCount, separateResultFolders.length * 3);

				pFindTask.setTotalThread(threadCount);

				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File pf2 = new File(spectraDirFile, separateResultFolders[i].getName() + suffix);
					File dbReducerFile = new File(separateResultFolders[i], "DBReducer");
					File db = new File(dbReducerFile, "DBReducer.fasta");

					if (pf2.exists()) {

						if (db.exists() && db.length() > 0) {
							pFindTask.searchPF2(new String[] { pf2.getAbsolutePath() },
									separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type,
									isOpenSearch, true, mosp.getOpenPsmFDR(),
									((MetaParameterMag) this.metaPar).getFixMods(),
									((MetaParameterMag) this.metaPar).getVariMods(),
									((MetaParameterMag) this.metaPar).getEnzyme(),
									((MetaParameterMag) this.metaPar).getMissCleavages(),
									((MetaParameterMag) this.metaPar).getDigestMode(),
									((MetaParameterMag) this.metaPar).getIsobaricTag());

						} else {

							System.out.println(
									format.format(new Date()) + "\t" + getTaskName() + ": no protein sequences were in " + db
											+ ", which means no peptides/proteins can be identifed from "
											+ separateResultFolders[i].getName());

							LOGGER.error(getTaskName() + ": no protein sequences were in " + db
									+ ", which means no peptides/proteins can be identifed from "
									+ separateResultFolders[i].getName());
						}

					} else {

						File mgf = new File(spectraDirFile, separateResultFolders[i].getName() + mgfsuffix);

						if (!mgf.exists()) {
							mgf = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
						}

						if (mgf.exists() && db.exists() && db.length() > 0) {

							if (db.exists() && db.length() > 0) {
								pFindTask.searchMgf(new String[] { mgf.getAbsolutePath() },
										separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type,
										isOpenSearch, true, mosp.getOpenPsmFDR(),
										((MetaParameterMag) this.metaPar).getFixMods(),
										((MetaParameterMag) this.metaPar).getVariMods(),
										((MetaParameterMag) this.metaPar).getEnzyme(),
										((MetaParameterMag) this.metaPar).getMissCleavages(),
										((MetaParameterMag) this.metaPar).getDigestMode(),
										((MetaParameterMag) this.metaPar).getIsobaricTag(), false);
							} else {

								System.out.println(
										format.format(new Date()) + "\t" + getTaskName() + ": no protein sequences were in "
												+ db + ", which means no peptides/proteins can be identifed from "
												+ separateResultFolders[i].getName());

								LOGGER.error(getTaskName() + ": no protein sequences were in " + db
										+ ", which means no peptides/proteins can be identifed from "
										+ separateResultFolders[i].getName());
							}

						} else {
							System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra file " + pf2
									+ " or " + mgf + " was not found");

							LOGGER.error(getTaskName() + ": spectra file " + pf2 + " or " + mgf + " was not found");

							return false;
						}
					}
				}

				pFindTask.run(threadPoolCount, this.separateResultFolders.length * 10);

			} else {

				pFindTask.setTotalThread(threadCount);
				String mgfsuffix = suffix.replace("pf2", "mgf");

				for (int i = 0; i < this.separateResultFolders.length; i++) {
					File pf2 = new File(spectraDirFile, separateResultFolders[i].getName() + suffix);
					File hapFile = new File(separateResultFolders[i], "hap");
					File db = new File(hapFile, separateResultFolders[i].getName() + ".fasta");
					if (pf2.exists()) {

						if (db.exists() && db.length() > 0) {
							pFindTask.searchPF2(new String[] { pf2.getAbsolutePath() },
									separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type,
									isOpenSearch, true, mosp.getOpenPsmFDR(),
									((MetaParameterMag) this.metaPar).getFixMods(),
									((MetaParameterMag) this.metaPar).getVariMods(),
									((MetaParameterMag) this.metaPar).getEnzyme(),
									((MetaParameterMag) this.metaPar).getMissCleavages(),
									((MetaParameterMag) this.metaPar).getDigestMode(),
									((MetaParameterMag) this.metaPar).getIsobaricTag());
						} else {

							System.out.println(
									format.format(new Date()) + "\t" + getTaskName() + ": no protein sequences were in " + db
											+ ", which means no peptides/proteins can be identifed from "
											+ separateResultFolders[i].getName());

							LOGGER.error(getTaskName() + ": no protein sequences were in " + db
									+ ", which means no peptides/proteins can be identifed from "
									+ separateResultFolders[i].getName());
						}

					} else {

						File mgf = new File(spectraDirFile, separateResultFolders[i].getName() + mgfsuffix);

						if (!mgf.exists()) {
							mgf = new File(spectraDirFile, separateResultFolders[i].getName() + ".mgf");
						}

						if (mgf.exists() && db.exists() && db.length() > 0) {

							if (db.exists() && db.length() > 0) {
								pFindTask.searchMgf(new String[] { mgf.getAbsolutePath() },
										separateResultFolders[i].getAbsolutePath(), db.getAbsolutePath(), ms2Type,
										isOpenSearch, true, mosp.getOpenPsmFDR(),
										((MetaParameterMag) this.metaPar).getFixMods(),
										((MetaParameterMag) this.metaPar).getVariMods(),
										((MetaParameterMag) this.metaPar).getEnzyme(),
										((MetaParameterMag) this.metaPar).getMissCleavages(),
										((MetaParameterMag) this.metaPar).getDigestMode(),
										((MetaParameterMag) this.metaPar).getIsobaricTag(), false);
							} else {

								System.out.println(
										format.format(new Date()) + "\t" + getTaskName() + ": no protein sequences were in "
												+ db + ", which means no peptides/proteins can be identifed from "
												+ separateResultFolders[i].getName());

								LOGGER.error(getTaskName() + ": no protein sequences were in " + db
										+ ", which means no peptides/proteins can be identifed from "
										+ separateResultFolders[i].getName());
							}

						} else {
							System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": spectra file " + pf2
									+ " or " + mgf + " was not found");

							LOGGER.error(getTaskName() + ": spectra file " + pf2 + " or " + mgf + " was not found");

							return false;
						}
					}
				}

				pFindTask.run(threadPoolCount, this.separateResultFolders.length * 10);
			}
		}

		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().equals("pFind-Filtered.spectra");
			}
		};

		// The WatchService for monitoring the folders
		WatchService watchService = null;
		// Create the WatchService that monitors the folders
		try {
			watchService = FileSystems.getDefault().newWatchService();
			// Register each folder with the watch service
			for (File folder : separateResultFolders) {
				Path path = folder.toPath();
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Thread(new WatchServiceLoop(watchService, filter, taskCount, separateResultFolders.length, 40, 68)).start();

		for (int i = 0; i < this.separateResultFolders.length; i++) {
			PFindTask.cleanPFindResultFolder(separateResultFolders[i], true);

			File hapFile = new File(separateResultFolders[i], "hap");
			if (hapFile.exists()) {
				PFindTask.cleanPFindResultFolder(hapFile, true);
			}

			File dbReduceFile = new File(separateResultFolders[i], "DBReducer");
			if (dbReduceFile.exists()) {
				PFindTask.cleanPFindResultFolder(dbReduceFile, true);
			}
		}

		return true;
	}

	protected boolean combineResult() {
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination started");
		LOGGER.info(getTaskName() + ": result combination started");

		File msms = new File(resultFolderFile, "psms.tsv");

		if (msms.exists() && msms.length() > 200) {

			LOGGER.info(getTaskName() + ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");
			
			return true;
		}

		File rtFile = new File(metaPar.getSpectraFile(), "rt.txt");
		HashMap<String, Double> rtMap = new HashMap<String, Double>();
		try (BufferedReader reader = new BufferedReader(new FileReader(rtFile))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				rtMap.put(cs[0] + "_" + cs[1], Double.parseDouble(cs[2]));
			}
			reader.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading the retention time information from " + rtFile);
			LOGGER.error(getTaskName() + ": error in reading the retention time information from " + rtFile);
		}

		ArrayList<PFindPSM[]> psmList = new ArrayList<PFindPSM[]>();
		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < this.separateResultFolders.length; i++) {
			PFindPsmReader psmReader = new PFindPsmReader(
					new File(this.separateResultFolders[i], "pFind.spectra"));

			PFindPSM[] psms = psmReader.getPsms();

			for (int j = 0; j < psms.length; j++) {
				String sequence = psms[j].getSequence();

				if (sequence.contains("X")) {
					continue;
				}

				String[] proteins = psms[j].getProteins();
				HashSet<String> proSet;
				if (pepProMap.containsKey(sequence)) {
					proSet = pepProMap.get(sequence);
				} else {
					proSet = new HashSet<String>();
					pepProMap.put(sequence, proSet);
				}
				for (int k = 0; k < proteins.length; k++) {
					proSet.add(proteins[k]);
				}
			}

			psmList.add(psms);
		}

		OpenPsmTsvWriter msmsWriter = new OpenPsmTsvWriter(msms, OpenPsmTsvWriter.pFindFormat);
		for (int i = 0; i < psmList.size(); i++) {
			PFindPSM[] psms = psmList.get(i);
			for (int j = 0; j < psms.length; j++) {
				String sequence = psms[j].getSequence();

				if (pepProMap.containsKey(sequence)) {
					HashSet<String> proSet = pepProMap.get(sequence);
					String[] proteins = proSet.toArray(new String[proSet.size()]);
					psms[j].setProteins(proteins);

					String scanKey = psms[j].getFileName() + "_" + psms[j].getScan();

					if (rtMap.containsKey(scanKey)) {
						psms[j].setRt(rtMap.get(scanKey));
						msmsWriter.addPFindPSM(psms[j]);
					}
				}
			}
		}

		msmsWriter.close();

		if (msms.exists() && msms.length() > 200) {
			
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination finished");
			LOGGER.info(getTaskName() + ": result combination finished");

			return true;
		} else {

			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination failed");
			LOGGER.info(getTaskName() + ": result combination failed");

			return false;
		}
	}
		
	/*
	protected boolean exportCombinedPSMs() {
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination started");
		LOGGER.info(getTaskName() + ": result combination started");

		File msms = new File(resultFolderFile, "psms.tsv");
		
		if (msms.exists() && msms.length() > 0) {

			LOGGER.info(getTaskName() + ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");

			setProgress(55);

			return true;
		}

		HashMap<String, HashSet<String>> tempGenomePepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();
		HashSet<String> magPepSet = new HashSet<String>();
		ArrayList<PFindPSM[]> psmList = new ArrayList<PFindPSM[]>();
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			PFindPsmReader psmReader = new PFindPsmReader(
					new File(this.separateResultFolders[i], "pFind-Filtered.spectra"));

			PFindPSM[] psms = psmReader.getPsms();
			psmList.add(psms);
			
			for (int j = 0; j < psms.length; j++) {
				String sequence = psms[j].getSequence();
				String[] pros = psms[j].getProteins();

				if (pros[j].startsWith(magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];
					magPepSet.add(sequence);

					if (tempGenomePepMap.containsKey(genome)) {
						tempGenomePepMap.get(genome).add(sequence);
					} else {
						HashSet<String> tempPepSet = new HashSet<String>();
						tempPepSet.add(sequence);
						tempGenomePepMap.put(genome, tempPepSet);
					}

					if (tempProPepMap.containsKey(pros[j])) {
						tempProPepMap.get(pros[j]).add(sequence);
					} else {
						HashSet<String> tempPepSet = new HashSet<String>();
						tempPepSet.add(sequence);
						tempProPepMap.put(pros[j], tempPepSet);
					}
				}
			}
		}

		if (magPepSet.size() == 0) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": no peptide from microbiome was identified from this dataset, "
					+ "the task will be terminated, please try another dataset");
			LOGGER.info(getTaskName() + ": no peptide from microbiome was identified from this dataset, "
					+ "the task will be terminated, please try another dataset");

			return false;
		}

		File genomeFile = new File(resultFolderFile, "filter1_mags.tsv");
		HashSet<String> razorGenomeSet = refineGenomes(tempGenomePepMap, magPepSet.size(), 1.0, genomeFile);
		
		LOGGER.info(getTaskName() + ": filtered genome count " + razorGenomeSet.size());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": filtered genome count " + razorGenomeSet.size());
		
		Iterator<String> tempProIt = tempProPepMap.keySet().iterator();
		while (tempProIt.hasNext()) {
			String pro = tempProIt.next();
			String genome = pro.split("_")[0];
			if (!razorGenomeSet.contains(genome)) {
				tempProIt.remove();
			}
		}

		File genomeProFile = new File(resultFolderFile, "filter1_pros.tsv");
		HashSet<String> razorProSet = refineGenomes(tempProPepMap, magPepSet.size(), 1.0, genomeProFile);
		
		
		OpenPsmTsvWriter msmsWriter = new OpenPsmTsvWriter(msms, OpenPsmTsvWriter.pFindFormat);

		HashMap<String, HashSet<Integer>> filePsmCountMap = new HashMap<String, HashSet<Integer>>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, String[]> scanProMap = new HashMap<String, String[]>();

		for (int i = 0; i < psmList.size(); i++) {
			PFindPSM[] psms = psmList.get(i);
			
			for(int j=0;j<psms.length;j++) {
				String pep = psms[j].getSequence();
				if (pep.contains("X")) {
					continue;
				}
				
				ArrayList<String> proList = new ArrayList<String>();
				String[] proteins = psms[j].getProteins();
				for (int k = 0; k < proteins.length; k++) {
					if (!proteins[i].startsWith(PFindResultReader.REV)) {
						if (proteins[k].startsWith(magDb.getIdentifier())) {
							if(razorProSet.contains(proteins[k])) {
								proList.add(proteins[k]);
							}
						} else {
							proList.add(proteins[k]);
						}
					}
				}
				if(proList.size()>0) {
					String[] pros = proList.toArray(new String[proList.size()]);
				}
			}
			

			
			String razorProName = razorPepProMap.get(pep);

			HashSet<String> originalProSet = originalPepProMap.get(pep);

			HashSet<String> proSet = new HashSet<String>();
			proSet.add(razorProName);

			if (razorProName.startsWith(PFindResultReader.REV)) {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			} else {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (!pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			}
			String[] pros = proSet.toArray(new String[proSet.size()]);
			psms[i].setProteins(pros);

			scanProMap.put(psms[i].getFileName(), pros);

			String fileName = psms[i].getFileName();
			fileName = fileName.substring(0, fileName.indexOf("."));

			if (filePsmCountMap.containsKey(fileName)) {

				filePsmCountMap.get(fileName).add(psms[i].getScan());
				fileUnipepMap.get(fileName).add(pep);
			} else {

				HashSet<Integer> scanSet = new HashSet<Integer>();
				scanSet.add(psms[i].getScan());
				filePsmCountMap.put(fileName, scanSet);

				HashSet<String> seqSet = new HashSet<String>();
				seqSet.add(pep);
				fileUnipepMap.put(fileName, seqSet);
			}

			
		}
		msmsWriter.close();
		
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination finished");
		LOGGER.info(getTaskName() + ": result combination finished");

		return true;
	}
	*/
	
	protected boolean combineResultPFind() {

		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination started");
		LOGGER.info(getTaskName() + ": result combination started");

		File msms = new File(resultFolderFile, "psms.tsv");

		this.pFindResultReader = new PFindResultReader(resultFolderFile, spectraDirFile, ptmMap, false);

		if (pFindResultReader.hasResult() && msms.exists()) {

			LOGGER.info(getTaskName() + ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": peptide and protein identification results have been found in " + resultFolderFile
					+ ", go to next step");

			setProgress(55);

			return true;
		}

		HashMap<String, HashSet<String>> originalPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> originalProPepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> totalPepProMap = new HashMap<String, HashSet<String>>();

		HashMap<String, HashSet<String>> tempGenomePepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();
		HashSet<String> magPepSet = new HashSet<String>();

		HashMap<String, double[]> proScoreMap = new HashMap<String, double[]>();
		HashMap<String, String> proNameMap = new HashMap<String, String>();

		PFindPSM[] psms = null;
		String proTitle1 = null;
		String proTitle2 = null;

		for (int i = 0; i < this.separateResultFolders.length; i++) {

			PFindResultReader readeri = new PFindResultReader(separateResultFolders[i], spectraDirFile, this.ptmMap);
			if (!readeri.hasResult()) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": no peptide/protein identifications in " + separateResultFolders[i]);
				LOGGER.info(getTaskName() + ": no peptide/protein identifications in " + separateResultFolders[i]);
				continue;
			}

			try {
				readeri.read();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading peptide identification result from " + separateResultFolders[i]);
				LOGGER.error(
						getTaskName() + ": error in reading peptide identification result from " + separateResultFolders[i],
						e);
				return false;
			}

			PFindPSM[] psmsi = readeri.getPsms();
			if (psms == null) {
				psms = new PFindPSM[psmsi.length];
				System.arraycopy(psmsi, 0, psms, 0, psmsi.length);
			} else {
				PFindPSM[] tempPsms = new PFindPSM[psms.length + psmsi.length];
				System.arraycopy(psms, 0, tempPsms, 0, psms.length);
				System.arraycopy(psmsi, 0, tempPsms, psms.length, psmsi.length);
				psms = tempPsms;
			}

			PeptideResult[] prs = readeri.getPeptideResults();
			for (PeptideResult pr : prs) {
				String sequence = pr.getBaseSeq();
				String[] pros = pr.getProteins();
				HashSet<String> genomeSet = new HashSet<String>();
				HashSet<String> proSet = new HashSet<String>();

				for (int j = 0; j < pros.length; j++) {
					if (pros[j].startsWith(magDb.getIdentifier())) {
						String genome = pros[j].split("_")[0];
						genomeSet.add(genome);
						magPepSet.add(sequence);

						if (tempGenomePepMap.containsKey(genome)) {
							tempGenomePepMap.get(genome).add(sequence);
						} else {
							HashSet<String> tempPepSet = new HashSet<String>();
							tempPepSet.add(sequence);
							tempGenomePepMap.put(genome, tempPepSet);
						}

						if (tempProPepMap.containsKey(pros[j])) {
							tempProPepMap.get(pros[j]).add(sequence);
						} else {
							HashSet<String> tempPepSet = new HashSet<String>();
							tempPepSet.add(sequence);
							tempProPepMap.put(pros[j], tempPepSet);
						}
					}

					proSet.add(pros[j]);

					if (totalPepProMap.containsKey(sequence)) {
						totalPepProMap.get(sequence).add(pros[j]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pros[j]);
						totalPepProMap.put(sequence, set);
					}

					if (originalPepProMap.containsKey(sequence)) {
						originalPepProMap.get(sequence).add(pros[j]);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pros[j]);
						originalPepProMap.put(sequence, set);
					}

					if (originalProPepMap.containsKey(pros[j])) {
						originalProPepMap.get(pros[j]).add(sequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(sequence);
						originalProPepMap.put(pros[j], set);
					}
				}
			}

			File proFile = new File(separateResultFolders[i], "pFind.protein");
			try (BufferedReader proReader = new BufferedReader(new FileReader(proFile))) {
				proTitle1 = proReader.readLine();
				int proTitleLength = proTitle1.split("\t").length;
				proTitle2 = proReader.readLine();
				int psmTitleLenght = proTitle2.split("\t").length;

				double leadProScore = 0;
				double leadQValue = 0;
				double leadProCoverage = 0;
				String line = null;
				while ((line = proReader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (cs.length == 1) {
						if (line.contains("Summary")) {
							break;
						}
					} else if (cs.length == proTitleLength) {
						double proScore = Double.parseDouble(cs[2]);
						double proQValue = Double.parseDouble(cs[3]);
						double proCoverage = Double.parseDouble(cs[4]);

						if (proScoreMap.containsKey(cs[1])) {
							if (proScore > proScoreMap.get(cs[1])[0]) {
								proScoreMap.put(cs[1], new double[] { proScore, proQValue, proCoverage });
							}
						} else {
							proScoreMap.put(cs[1], new double[] { proScore, proQValue, proCoverage });
						}

						leadProScore = proScore;
						leadQValue = proQValue;
						leadProCoverage = proCoverage;

					} else if (cs.length == psmTitleLenght) {

					} else {
						if (cs[1].equals("SameSet") || cs[1].equals("SubSet")) {
							double proCoverage = Double.parseDouble(cs[3]);
							double proScore = leadProScore * proCoverage / leadProCoverage;
							double proQValue = leadQValue * leadProCoverage / proCoverage;
							if (proScoreMap.containsKey(cs[2])) {
								if (proScore > proScoreMap.get(cs[2])[0]) {
									proScoreMap.put(cs[2], new double[] { proScore, proQValue, proCoverage });
								}
							} else {
								proScoreMap.put(cs[2], new double[] { proScore, proQValue, proCoverage });
							}
						}
					}
				}
				proReader.close();
			} catch (IOException e) {
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading protein identification result from " + proFile);
				LOGGER.error(getTaskName() + ": error in reading protein identification result from " + proFile, e);
			}

			File hapFile = new File(separateResultFolders[i], "hap");
			File dbFile = new File(hapFile, separateResultFolders[i].getName() + ".fasta");

			if (dbFile.exists()) {
				try (BufferedReader dbReader = new BufferedReader(new FileReader(dbFile))) {
					String dbLine = null;
					while ((dbLine = dbReader.readLine()) != null) {
						if (dbLine.startsWith(">")) {
							int sp = dbLine.indexOf(" ");
							if (sp > 1) {
								proNameMap.put(dbLine.substring(1, sp), dbLine.substring(sp + 1));
							}
						}
					}
					dbReader.close();
				} catch (IOException e) {
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in reading database information from " + dbFile);
					LOGGER.error(getTaskName() + ": error in reading database information from " + dbFile, e);
				}
			}

			File resFile = new File(separateResultFolders[i], "pFindTask.F1.L1.qry.res");
			if (resFile.exists()) {
				FileUtils.deleteQuietly(resFile);
			}
		}

		if (magPepSet.size() == 0) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": no peptide from microbiome was identified from this dataset, "
					+ "the task will be terminated, please try another dataset");
			LOGGER.info(getTaskName() + ": no peptide from microbiome was identified from this dataset, "
					+ "the task will be terminated, please try another dataset");

			return false;
		}

		File genomeFile = new File(resultFolderFile, "mags.tsv");
		HashSet<String> razorGenomeSet = refineGenomes(tempGenomePepMap, magPepSet.size(), 1.0, genomeFile);
		
		LOGGER.info(getTaskName() + ": filtered genome count " + razorGenomeSet.size());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": filtered genome count " + razorGenomeSet.size());

		Iterator<String> tempProIt = tempProPepMap.keySet().iterator();
		while (tempProIt.hasNext()) {
			String pro = tempProIt.next();
			String genome = pro.split("_")[0];
			if (!razorGenomeSet.contains(genome)) {
				tempProIt.remove();
			}
		}

		File genomeProFile = new File(resultFolderFile, "razorPro.tsv");
		HashSet<String> razorProSet = refineGenomes(tempProPepMap, magPepSet.size(), 1.0, genomeProFile);

		HashMap<String, ProteinGroup> proGroupMap = this.getProteinGroups(totalPepProMap, razorProSet);

		HashMap<String, HashSet<String>> proPepMap = new HashMap<String, HashSet<String>>();
		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			for (String pro : proSet) {
				if (proPepMap.containsKey(pro)) {
					proPepMap.get(pro).add(pep);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(pep);
					proPepMap.put(pro, set);
				}
			}
		}

		String[] allPros = proGroupMap.keySet().toArray(new String[proGroupMap.size()]);

		Arrays.sort(allPros, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub

				boolean target1 = !o1.startsWith(PFindResultReader.REV);
				boolean target2 = !o2.startsWith(PFindResultReader.REV);

				if (target1) {
					if (!target2) {
						return -1;
					}
				} else {
					if (target2) {
						return 1;
					}
				}

				HashSet<String> pepSet1 = proPepMap.get(o1);
				boolean unique1 = false;
				for (String pep : pepSet1) {
					HashSet<String> proSet = totalPepProMap.get(pep);
					if (proSet.size() == 1 && proSet.contains(o1)) {
						unique1 = true;
						break;
					}
				}

				HashSet<String> pepSet2 = proPepMap.get(o2);
				boolean unique2 = false;
				for (String pep : pepSet2) {
					HashSet<String> proSet = totalPepProMap.get(pep);
					if (proSet.size() == 1 && proSet.contains(o2)) {
						unique2 = true;
						break;
					}
				}

				if (unique1) {
					if (!unique2) {
						return -1;
					}
				} else {
					if (unique2) {
						return 1;
					}
				}

				if (proScoreMap.containsKey(o1)) {
					if (proScoreMap.containsKey(o2)) {
						double[] score1 = proScoreMap.get(o1);
						double[] score2 = proScoreMap.get(o2);

						if (score1[0] > score2[0]) {
							return -1;
						} else if (score1[0] < score2[0]) {
							return 1;
						} else {
							if (score1[2] > score2[2]) {
								return -1;
							} else if (score1[2] < score2[2]) {
								return 1;
							} else {
								return 0;
							}
						}

					} else {
						return -1;
					}
				} else {
					if (proScoreMap.containsKey(o2)) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});

		HashMap<String, String> razorPepProMap = new HashMap<String, String>();
		HashMap<String, HashSet<String>> razorProPepMap = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < allPros.length; i++) {

			String proName = allPros[i];
			HashSet<String> pepSet = originalProPepMap.get(proName);
			razorProPepMap.put(proName, new HashSet<String>());
			for (String pep : pepSet) {
				if (!razorPepProMap.containsKey(pep)) {
					razorPepProMap.put(pep, proName);
					razorProPepMap.get(proName).add(pep);
				} else {
					if (razorPepProMap.get(pep).startsWith(PFindResultReader.REV)) {
						String revPro = razorPepProMap.get(pep);
						razorProPepMap.get(revPro).remove(pep);
						razorPepProMap.put(pep, proName);
						razorProPepMap.get(proName).add(pep);
					}
				}
			}
		}

		HashSet<String> subProSet = new HashSet<String>();
		HashSet<String> sameProSet = new HashSet<String>();
		for (int i = 0; i < allPros.length; i++) {
			String proName = allPros[i];

			if (razorProPepMap.get(proName).size() == 0) {
				razorProPepMap.remove(proName);
			}

			if (proName.startsWith(PFindResultReader.REV)) {
				continue;
			}

			ProteinGroup pg = proGroupMap.get(proName);
			sameProSet.addAll(pg.sameSet);

			HashSet<String> pepSet = originalProPepMap.get(proName);
			for (String pep : pepSet) {
				HashSet<String> proSet = originalPepProMap.get(pep);
				for (String pro : proSet) {
					if (pro.startsWith(PFindResultReader.REV)) {
						continue;
					}

					if (subProSet.contains(pro)) {
						continue;
					}

					if (proName.equals(pro) || pg.sameSet.contains(pro)) {
						continue;
					}

					boolean sub = true;
					HashSet<String> pepISet = originalProPepMap.get(pro);
					for (String pepI : pepISet) {
						if (!pepSet.contains(pepI)) {
							sub = false;
						}
					}

					if (sub) {
						pg.subSet.add(pro);
						subProSet.add(pro);
					}
				}
			}
		}

		OpenPsmTsvWriter msmsWriter = new OpenPsmTsvWriter(msms, OpenPsmTsvWriter.pFindFormat);

		HashMap<String, HashSet<Integer>> filePsmCountMap = new HashMap<String, HashSet<Integer>>();
		HashMap<String, HashSet<String>> fileUnipepMap = new HashMap<String, HashSet<String>>();
		HashMap<String, String[]> scanProMap = new HashMap<String, String[]>();

		for (int i = 0; i < psms.length; i++) {

			String pep = psms[i].getSequence();
			if (!razorPepProMap.containsKey(pep)) {
				continue;
			}

			String razorProName = razorPepProMap.get(pep);

			HashSet<String> originalProSet = originalPepProMap.get(pep);

			HashSet<String> proSet = new HashSet<String>();
			proSet.add(razorProName);

			if (razorProName.startsWith(PFindResultReader.REV)) {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			} else {
				for (String pro : originalProSet) {
					if (proGroupMap.containsKey(pro) || sameProSet.contains(pro) || subProSet.contains(pro)) {
						if (!pro.startsWith(PFindResultReader.REV)) {
							proSet.add(pro);
						}
					}
				}
			}
			String[] pros = proSet.toArray(new String[proSet.size()]);
			psms[i].setProteins(pros);

			scanProMap.put(psms[i].getFileName(), pros);

			String fileName = psms[i].getFileName();
			fileName = fileName.substring(0, fileName.indexOf("."));

			if (filePsmCountMap.containsKey(fileName)) {

				filePsmCountMap.get(fileName).add(psms[i].getScan());
				fileUnipepMap.get(fileName).add(pep);
			} else {

				HashSet<Integer> scanSet = new HashSet<Integer>();
				scanSet.add(psms[i].getScan());
				filePsmCountMap.put(fileName, scanSet);

				HashSet<String> seqSet = new HashSet<String>();
				seqSet.add(pep);
				fileUnipepMap.put(fileName, seqSet);
			}

			if (!pep.contains("X")) {
				msmsWriter.addPFindPSM(psms[i]);
			}
		}
		msmsWriter.close();

		try (PrintWriter idWriter = new PrintWriter(new File(resultFolderFile, "id.tsv"))) {
			idWriter.println("Raw file\tUnique peptide count\tPSM count");
			String[] rawNames = filePsmCountMap.keySet().toArray(new String[filePsmCountMap.size()]);
			Arrays.sort(rawNames);

			for (String rawNameString : rawNames) {
				idWriter.println(rawNameString + "\t" + fileUnipepMap.get(rawNameString).size() + "\t"
						+ filePsmCountMap.get(rawNameString).size());
			}
			idWriter.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing identification information to " + new File(resultFolderFile, "id.tsv"));
			LOGGER.error(getTaskName() + ": error in writing identification information to "
					+ new File(resultFolderFile, "id.tsv"), e);
		}

		HashMap<String, HashMap<String, String[]>> psmInfoMap = new HashMap<String, HashMap<String, String[]>>();
		HashMap<String, HashMap<String, Integer>> psmCountMap = new HashMap<String, HashMap<String, Integer>>();
		File pFindSpectra = new File(resultFolderFile, "pFind-Filtered.spectra");

		try (PrintWriter combinePsmWriter = new PrintWriter(pFindSpectra)) {
			for (int i = 0; i < this.separateResultFolders.length; i++) {

				File specFile = new File(separateResultFolders[i], "pFind.spectra");

				BufferedReader reader = new BufferedReader(new FileReader(specFile));
				String line = reader.readLine();
				if (i == 0) {
					combinePsmWriter.println(line);
				}

				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					if (scanProMap.containsKey(cs[0])) {
						String[] pros = scanProMap.get(cs[0]);
						StringBuilder prosb = new StringBuilder();
						for (int j = 0; j < pros.length; j++) {
							prosb.append(pros[j]).append("/");
						}
						cs[12] = prosb.toString();

						StringBuilder sb = new StringBuilder();
						for (int j = 0; j < cs.length; j++) {
							sb.append(cs[j]).append("\t");
						}
						if (sb.length() > 0) {
							sb.deleteCharAt(sb.length() - 1);
						}
						combinePsmWriter.println(sb);

						HashMap<String, String[]> modInfoMap;
						HashMap<String, Integer> modCountMap;
						if (psmInfoMap.containsKey(cs[5])) {
							modInfoMap = psmInfoMap.get(cs[5]);
							modCountMap = psmCountMap.get(cs[5]);
						} else {
							modInfoMap = new HashMap<String, String[]>();
							modCountMap = new HashMap<String, Integer>();
							psmInfoMap.put(cs[5], modInfoMap);
							psmCountMap.put(cs[5], modCountMap);
						}

						if (modInfoMap.containsKey(cs[10])) {
							if (Double.parseDouble(cs[9]) < Double.parseDouble(modInfoMap.get(cs[10])[9])) {
								modInfoMap.put(cs[10], cs);
							}
							modCountMap.put(cs[10], modCountMap.get(cs[10]) + 1);
						} else {
							modInfoMap.put(cs[10], cs);
							modCountMap.put(cs[10], 1);
						}
					}
				}

				reader.close();
			}
			combinePsmWriter.close();
		} catch (IOException e) {

			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing identified PSMs to "
					+ pFindSpectra);
			LOGGER.error(getTaskName() + ": error in writing identified PSMs to " + pFindSpectra, e);
		}

		File pFindProtein = new File(resultFolderFile, "pFind.protein");
		try (PrintWriter combineProWriter = new PrintWriter(pFindProtein)) {
			combineProWriter.println(proTitle1);
			combineProWriter.println(proTitle2);
			int[] proTDCount = new int[4];
			double coverage = 0.0;

			int proId = 1;
			for (int i = 0; i < allPros.length; i++) {

				StringBuilder proSb = new StringBuilder();
				proSb.append(proId++).append("\t");
				proSb.append(allPros[i]).append("\t");
				if (proScoreMap.containsKey(allPros[i])) {
					double[] score = proScoreMap.get(allPros[i]);
					proSb.append(score[0]).append("\t");
					proSb.append(score[1]).append("\t");
					proSb.append(score[2]).append("\t");

					coverage += score[2];
				} else {
					proSb.append("0.0\t");
					proSb.append("1.0\t");
					proSb.append("0.0\t");
				}

				ProteinGroup pg = proGroupMap.get(allPros[i]);
				HashSet<String> sameSet = pg.sameSet;
				HashSet<String> subSet = pg.subSet;
				HashSet<String> pepSet = proPepMap.get(allPros[i]);

				StringBuilder psmSb = new StringBuilder();
				int psmId = 1;
				boolean hasUnique = false;

				String[] peps = pepSet.toArray(new String[pepSet.size()]);
				Arrays.sort(peps);

				HashMap<String, Integer> proCountMap = new HashMap<String, Integer>();

				for (int j = 0; j < peps.length; j++) {
					if (totalPepProMap.containsKey(peps[j]) && totalPepProMap.get(peps[j]).size() == 1) {
						hasUnique = true;
					}
					HashMap<String, String[]> psmInfoMapJ = psmInfoMap.get(peps[j]);
					if (psmInfoMapJ == null) {
						continue;
					}
					for (String mod : psmInfoMapJ.keySet()) {

						String[] cs = psmInfoMapJ.get(mod);

						psmSb.append("\t\t").append(psmId++);
						psmSb.append("\t").append(peps[j]);

						for (int k = 6; k < 18; k++) {
							psmSb.append("\t").append(cs[k]);
						}
						psmSb.append("\t").append(cs[0]);
						psmSb.append("\t").append(cs[3]);
						psmSb.append("\t").append(psmCountMap.get(peps[j]).get(mod));

						String[] pros = cs[12].split("/");
						for (int k = 0; k < pros.length; k++) {
							if (proCountMap.containsKey(pros[k])) {
								proCountMap.put(pros[k], proCountMap.get(pros[k]) + 1);
							} else {
								proCountMap.put(pros[k], 1);
							}
						}

						psmSb.append("\n");
					}
				}

				if (psmSb.length() == 0) {
					continue;
				}

				proSb.append(psmId - 1).append("\t");
				// sameSet
				proSb.append(sameSet.size() + "\t");
				// subSet
				proSb.append(subSet.size() + "\t");

				if (hasUnique) {
					proSb.append("1\t");
				} else {
					proSb.append("0\t");
				}

				if (proNameMap.containsKey(allPros[i])) {
					proSb.append(proNameMap.get(allPros[i])).append("\t\n");
				} else {
					if (allPros[i].startsWith(PFindResultReader.REV)) {
						proSb.append("decoy protein\t\n");
					} else {
						proSb.append("\t\n");
					}
				}

				if (allPros[i].startsWith(PFindResultReader.REV)) {
					proTDCount[1]++;
				} else {
					proTDCount[0]++;
				}

				for (String samePro : sameSet) {
					proSb.append("\tSameSet");
					proSb.append("\t").append(samePro);

					if (proScoreMap.containsKey(samePro)) {
						double[] score = proScoreMap.get(samePro);
						proSb.append("\t").append(score[2]);
					} else {
						proSb.append("\t0.0");
					}
					if (proCountMap.containsKey(samePro)) {
						proSb.append("\t").append(proCountMap.get(samePro)).append("\n");
					} else {
						proSb.append("\t0\n");
					}

					if (samePro.startsWith(PFindResultReader.REV)) {
						proTDCount[3]++;
					} else {
						proTDCount[2]++;
					}
				}

				for (String subPro : subSet) {
					proSb.append("\tSubSet");
					proSb.append("\t").append(subPro);

					if (proScoreMap.containsKey(subPro)) {
						double[] score = proScoreMap.get(subPro);
						proSb.append("\t").append(score[2]);
					} else {
						proSb.append("\t0.0");
					}
					if (proCountMap.containsKey(subPro)) {
						proSb.append("\t").append(proCountMap.get(subPro)).append("\n");
					} else {
						proSb.append("\t0\n");
					}

					if (subPro.startsWith(PFindResultReader.REV)) {
						proTDCount[3]++;
					} else {
						proTDCount[2]++;
					}
				}

				combineProWriter.print(proSb);
				combineProWriter.print(psmSb);
			}

			combineProWriter.println("----Summary----");
			combineProWriter.print("Proteins: ");
			combineProWriter.print(proTDCount[2]);
			combineProWriter.print(" / ");
			combineProWriter.println(proTDCount[2] + proTDCount[3]);

			combineProWriter.print("Protein_Groups: ");
			combineProWriter.print(proTDCount[0]);
			combineProWriter.print(" / ");
			combineProWriter.println(proTDCount[0] + proTDCount[1]);

			combineProWriter.print("Filtered_Protein_Groups: ");
			combineProWriter.print(proTDCount[0]);
			combineProWriter.print(" / ");
			combineProWriter.println(proTDCount[0] + proTDCount[1]);

			combineProWriter.print("Filtered_Protein_Groups_Cov: ");
			combineProWriter.print(FormatTool.getDFP2().format(coverage / (double) allPros.length));

			combineProWriter.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing identified proteins to " + pFindProtein);
			LOGGER.error(getTaskName() + ": error in writing identified proteins to " + pFindProtein, e);
		}

		if (pFindResultReader.hasResult()) {

			try {
				pFindResultReader.read();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading the database search result files");
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the database search result files");
				return false;
			}

			setProgress(55);

		} else {

			LOGGER.error(getTaskName() + ": peptide and protein identification failed");
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein identification failed");
			return false;
		}

		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": result combination finished");
		LOGGER.info(getTaskName() + ": result combination finished");

		return true;
	}

	protected String[] refineProteins(MetaPeptide[] quanPeps, HashMap<String, Double> genomeScoreMap) {
		return refineProteins(quanPeps, genomeScoreMap, PFindResultReader.REV);
	}

	protected String[] refineProteins(MetaPeptide[] quanPeps, HashMap<String, Double> genomeScoreMap, String decoySymble) {

		HashMap<String, Double> proteinIntensityMap = new HashMap<String, Double>();
		for (int i = 0; i < quanPeps.length; i++) {
			double[] pepIntensity = quanPeps[i].getIntensity();
			String[] proteins = quanPeps[i].getProteins();

			int count = 0;
			for (int j = 0; j < proteins.length; j++) {
				if (!proteins[j].startsWith(decoySymble)) {
					if (proteins[j].startsWith(this.magDb.getIdentifier())) {
						String genome = proteins[j].split("_")[0];
						if (genomeScoreMap.containsKey(genome)) {
							count++;
						}
					} else {
						count++;
					}
				}
			}

			if (count > 0) {
				double intensity = MathTool.getTotal(pepIntensity) / (double) count;
				for (int j = 0; j < proteins.length; j++) {
					if (!proteins[j].startsWith(decoySymble)) {
						if (proteins[j].startsWith(this.magDb.getIdentifier())) {
							String genome = proteins[j].split("_")[0];
							if (genomeScoreMap.containsKey(genome)) {
								if (proteinIntensityMap.containsKey(proteins[j])) {
									proteinIntensityMap.put(proteins[j],
											proteinIntensityMap.get(proteins[j]) + intensity);
								} else {
									proteinIntensityMap.put(proteins[j], intensity);
								}
							}
						} else {
							if (proteinIntensityMap.containsKey(proteins[j])) {
								proteinIntensityMap.put(proteins[j], proteinIntensityMap.get(proteins[j]) + intensity);
							} else {
								proteinIntensityMap.put(proteins[j], intensity);
							}
						}
					}
				}
			}
		}

		String[] razorProteins = new String[quanPeps.length];

		for (int i = 0; i < quanPeps.length; i++) {
			String[] proteins = quanPeps[i].getProteins();
			double genomeScore = Double.MAX_VALUE;
			double proteinIntensity = 0;

			for (int j = 0; j < proteins.length; j++) {

				if (!proteins[j].startsWith(decoySymble)) {
					if (proteins[j].startsWith(this.magDb.getIdentifier())) {
						String genome = proteins[j].split("_")[0];
						if (genomeScoreMap.containsKey(genome)) {
							if (genomeScoreMap.get(genome) < genomeScore) {
								genomeScore = genomeScoreMap.get(genome);
								razorProteins[i] = proteins[j];
								proteinIntensity = proteinIntensityMap.get(proteins[j]);
							} else if (genomeScoreMap.get(genome) == genomeScore) {
								if (proteinIntensityMap.get(proteins[j]) > proteinIntensity) {
									proteinIntensity = proteinIntensityMap.get(proteins[j]);
									razorProteins[i] = proteins[j];
								}
							}
						}
					} else {
						if (proteinIntensityMap.get(proteins[j]) > proteinIntensity) {
							proteinIntensity = proteinIntensityMap.get(proteins[j]);
							razorProteins[i] = proteins[j];
						}
					}
				}
			}
		}

		return razorProteins;
	}
	
	/**
	 * Reverser proteins are also included to calculate the FDR of genomes
	 * @param quanPeps
	 * @return
	 */
	protected HashMap<String, Double> refineGenomes(MetaPeptide[] quanPeps) {
		return refineGenomes(quanPeps, PFindResultReader.REV);
	}
	
	/**
	 * Reverser proteins are also included to calculate the FDR of genomes
	 * @param quanPeps
	 * @return
	 */
	protected HashMap<String, Double> refineGenomes(MetaPeptide[] quanPeps, String decoySymble) {

		String revIdentifier = decoySymble + this.magDb.getIdentifier();
		HashMap<String, Double> genomeIntensityMap = new HashMap<String, Double>();
		double[] peptideIntensity = new double[quanPeps.length];

		for (int i = 0; i < quanPeps.length; i++) {
			double[] pepIntensity = quanPeps[i].getIntensity();
			double totalIntensity = MathTool.getTotal(pepIntensity);
			int[] idenType = quanPeps[i].getIdenType();
			int idenCount = 0;
			for (int j = 0; j < idenType.length; j++) {
				if (idenType[j] == 0) {
					idenCount++;
				}
			}

			if (totalIntensity == 0 || idenCount == 0) {
				continue;
			}

//			double logIntensity = Math.log10(totalIntensity) * (double) idenCount / (double) idenType.length;
//			peptideIntensity[i] = Math.pow(10.0, logIntensity);
			
			double[] indiPEP = quanPeps[i].getIndividualPEP();
//			peptideIntensity[i] = 1.0;
			for (int j = 0; j < pepIntensity.length; j++) {
				if (idenType[j] == 0) {
					if (indiPEP[j] == 0) {
//						peptideIntensity[i] = peptideIntensity[i] * 1.0E7;
						peptideIntensity[i] += pepIntensity[j] * 1.0E7;
					} else {
//						peptideIntensity[i] = peptideIntensity[i] / indiPEP[j];
						peptideIntensity[i] += pepIntensity[j] / indiPEP[j];
					}
				} 
			}

			String[] proteins = quanPeps[i].getProteins();
			double intensity = peptideIntensity[i] / (double) proteins.length;

			for (int j = 0; j < proteins.length; j++) {
				if (proteins[j].startsWith(this.magDb.getIdentifier())) {
					String genome = proteins[j].split("_")[0];
					if (genomeIntensityMap.containsKey(genome)) {
						genomeIntensityMap.put(genome, genomeIntensityMap.get(genome) + intensity);
					} else {
						genomeIntensityMap.put(genome, intensity);
					}
				} else if (proteins[j].startsWith(revIdentifier)) {
					String pro = this.magDb.getIdentifier() + proteins[j].substring(revIdentifier.length());
					String genome = decoySymble + pro.split("_")[0];

					if (genomeIntensityMap.containsKey(genome)) {
						genomeIntensityMap.put(genome, genomeIntensityMap.get(genome) + intensity);
					} else {
						genomeIntensityMap.put(genome, intensity);
					}
				}
			}
		}

		LOGGER.info(getTaskName() + ": genome before filtering: " + genomeIntensityMap.size());

		HashMap<String, Double> razorGenomeIntenMap = new HashMap<String, Double>();
		for (int i = 0; i < quanPeps.length; i++) {
			if (peptideIntensity[i] > 0) {
				String[] proteins = quanPeps[i].getProteins();
				String razorGenome = "";
				double razorGenomeIntensity = 0;
				for (int j = 0; j < proteins.length; j++) {
					if (proteins[j].startsWith(this.magDb.getIdentifier())) {
						String genome = proteins[j].split("_")[0];
						if (genomeIntensityMap.containsKey(genome)) {
							double genomeIntensity = genomeIntensityMap.get(genome);
							if (genomeIntensity > razorGenomeIntensity) {
								razorGenome = genome;
								razorGenomeIntensity = genomeIntensity;
							}
						}
					} else if (proteins[j].startsWith(revIdentifier)) {
						String pro = this.magDb.getIdentifier() + proteins[j].substring(revIdentifier.length());
						String genome = decoySymble + pro.split("_")[0];
	
						if (genomeIntensityMap.containsKey(genome)) {
							double genomeIntensity = genomeIntensityMap.get(genome);
							if (genomeIntensity > razorGenomeIntensity) {
								razorGenome = genome;
								razorGenomeIntensity = genomeIntensity;
							}
						}
					}
				}

				if (razorGenome.startsWith(this.magDb.getIdentifier()) || razorGenome.startsWith(revIdentifier)) {
					razorGenomeIntenMap.put(razorGenome, 0.0);
				}
			}
		}

		LOGGER.info(getTaskName() + ": razor genome before filtering: " + razorGenomeIntenMap.size());

		for (int i = 0; i < quanPeps.length; i++) {
			if (peptideIntensity[i] > 0) {
				String[] proteins = quanPeps[i].getProteins();

				int count = 0;
				for (int j = 0; j < proteins.length; j++) {
					if (proteins[j].startsWith(this.magDb.getIdentifier())) {
						String genome = proteins[j].split("_")[0];
						if (razorGenomeIntenMap.containsKey(genome)) {
							count++;
						}
					} else if (proteins[j].startsWith(revIdentifier)) {
						String pro = this.magDb.getIdentifier() + proteins[j].substring(revIdentifier.length());
						String genome = decoySymble + pro.split("_")[0];
						if (razorGenomeIntenMap.containsKey(genome)) {
							count++;
						}
					}
				}

				if (count > 0) {
					double pepIntensity = peptideIntensity[i] / (double) count;
					for (int j = 0; j < proteins.length; j++) {

						if (proteins[j].startsWith(this.magDb.getIdentifier())) {
							String genome = proteins[j].split("_")[0];
							if (razorGenomeIntenMap.containsKey(genome)) {
								razorGenomeIntenMap.put(genome, razorGenomeIntenMap.get(genome) + pepIntensity);
							}
						} else if (proteins[j].startsWith(revIdentifier)) {
							String pro = this.magDb.getIdentifier() + proteins[j].substring(revIdentifier.length());
							String genome = decoySymble + pro.split("_")[0];
							if (razorGenomeIntenMap.containsKey(genome)) {
								razorGenomeIntenMap.put(genome, razorGenomeIntenMap.get(genome) + pepIntensity);
							}
						}
					}
				}
			}
		}

		List<Map.Entry<String, Double>> sortedRazorGenomeList = razorGenomeIntenMap.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList());
		String[] razorGenomes = sortedRazorGenomeList.stream().map(Map.Entry::getKey).toArray(String[]::new);

		ArrayList<Double> revLogIntenList = new ArrayList<Double>();
		for (int i = 0; i < razorGenomes.length; i++) {
			if (razorGenomes[i].startsWith(revIdentifier)) {
				double intensity = razorGenomeIntenMap.get(razorGenomes[i]);
				if (intensity > 0) {
					revLogIntenList.add(Math.log10(intensity));
				}
			}
		}

		LOGGER.info(getTaskName() + ": decoy genome count: " + revLogIntenList.size());

		HashMap<String, Double> genomeScoreMap = new HashMap<String, Double>();

		if (revLogIntenList.size() == 0) {
			LOGGER.info(getTaskName()
					+ ": no decoy genome were found, the p-Value for the genome identifications will be set as 0");

			for (int i = 0; i < razorGenomes.length; i++) {
				double intensity = razorGenomeIntenMap.get(razorGenomes[i]);
				if (intensity == 0) {
					break;
				}
				genomeScoreMap.put(razorGenomes[i], intensity);
			}
		} else {
			TTest tTest = new TTest();
			double[] revLogIntensities = new double[revLogIntenList.size()];
			for (int i = 2; i < revLogIntenList.size(); i++) {
				revLogIntensities = new double[i];
				for (int j = 0; j < revLogIntensities.length; j++) {
					revLogIntensities[j] = revLogIntenList.get(j);
				}

				double pValue = tTest.tTest(revLogIntensities[0], revLogIntensities);
				if (pValue < 0.01) {
					break;
				}
			}

			if (revLogIntensities.length == 0) {
				LOGGER.info(getTaskName() + ": the number of decoy genome was not sufficient for the t-test, "
						+ "the p-Value for the genome identifications will be set as 0");
				for (int i = 0; i < razorGenomes.length; i++) {
					double intensity = razorGenomeIntenMap.get(razorGenomes[i]);
					if (razorGenomes[i].startsWith(revIdentifier) || intensity == 0) {
						break;
					}
					genomeScoreMap.put(razorGenomes[i], 0.0);
				}
			} else if (revLogIntensities.length < 6) {
				double[] extended = new double[6];
				for (int i = 0; i < extended.length; i++) {
					extended[i] = revLogIntensities[i % revLogIntensities.length];
				}
				
				LOGGER.info(getTaskName() + ": decoy genome used for scoring: " + extended.length);
				
				for (int i = 0; i < razorGenomes.length; i++) {
					double intensity = razorGenomeIntenMap.get(razorGenomes[i]);

					if (razorGenomes[i].startsWith(revIdentifier) || intensity == 0) {
						break;
					}

					double pvalue = tTest.tTest(Math.log10(intensity), extended);
					LOGGER.info(getTaskName() + ": pvalue for genome "+razorGenomes[i]+": " + pvalue);
					
					if (pvalue < 0.01) {
						genomeScoreMap.put(razorGenomes[i], pvalue);
					} else {
//						break;
					}
				}
			} else {
				LOGGER.info(getTaskName() + ": decoy genome used for scoring: " + revLogIntensities.length);

				for (int i = 0; i < razorGenomes.length; i++) {
					double intensity = razorGenomeIntenMap.get(razorGenomes[i]);

					if (razorGenomes[i].startsWith(revIdentifier) || intensity == 0) {
						break;
					}

					double pvalue = tTest.tTest(Math.log10(intensity), revLogIntensities);
					LOGGER.info(getTaskName() + ": pvalue for genome "+razorGenomes[i]+": " + pvalue);
					
					if (pvalue < 0.01) {
						genomeScoreMap.put(razorGenomes[i], pvalue);
					} else {
//						break;
					}
				}
			}
		}

		LOGGER.info(getTaskName() + ": genome after filtering: " + genomeScoreMap.size());

		return genomeScoreMap;
	}

	protected class ProteinGroup {
		@SuppressWarnings("unused")
		String proName;
		HashSet<String> sameSet;
		HashSet<String> subSet;
		
		ProteinGroup(String proName) {
			this.proName = proName;
			this.sameSet = new HashSet<String>();
			this.subSet = new HashSet<String>();
		}

		public void addSameGroup(String pro) {
			this.sameSet.add(pro);
		}
		
		public HashSet<String> getSameSet(){
			return sameSet;
		}
	}

	protected void combineProteinGroups(HashMap<String, ProteinGroup> pgMap,
			HashMap<String, HashSet<String>> tempProPepMap, HashMap<String, HashSet<String>> tempPepProMap,
			HashMap<String, ProteinGroup> uniquePgMap) {

		// combine protein groups
		for (String pep : tempPepProMap.keySet()) {

			HashSet<String> proSet = tempPepProMap.get(pep);

			String[] pros = proSet.toArray(new String[proSet.size()]);

			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					if (tempProPepMap.get(o1).size() > tempProPepMap.get(o2).size()) {
						return -1;
					}
					if (tempProPepMap.get(o1).size() < tempProPepMap.get(o2).size()) {
						return 1;
					}

					if (o1.startsWith(PFindResultReader.REV)) {
						if (o2.startsWith(PFindResultReader.REV)) {
							return o1.compareTo(o2);
						} else {
							return 1;
						}
					} else {
						if (o2.startsWith(PFindResultReader.REV)) {
							return -1;
						} else {
							return o1.compareTo(o2);
						}
					}
				}
			});

			for (int i = 0; i < pros.length; i++) {

				if (pgMap.containsKey(pros[i])) {
					ProteinGroup pg = pgMap.get(pros[i]);
					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);

					for (int j = i + 1; j < pros.length; j++) {

						if (pgMap.containsKey(pros[j])) {
							HashSet<String> pepSetJ = tempProPepMap.get(pros[j]);

							HashSet<String> pepAll = new HashSet<String>(pepSetI.size() + pepSetJ.size());
							pepAll.addAll(pepSetI);
							pepAll.addAll(pepSetJ);

							if (pepAll.size() == pepSetI.size()) {
								if (pepSetI.size() == pepSetJ.size()) {
									pg.addSameGroup(pros[j]);
								}
								pgMap.remove(pros[j]);
							}
						}
					}
				}
			}
		}

		if (pgMap.size() == tempProPepMap.size()) {

			String[] pros = pgMap.keySet().toArray(new String[pgMap.size()]);
			Arrays.sort(pros, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub

					int size1 = tempProPepMap.get(o1).size();
					int size2 = tempProPepMap.get(o2).size();

					if (size1 < size2) {
						return -1;
					}
					if (size1 > size2) {
						return 1;
					}
					return 0;
				}
			});

			// remove subset proteins
			for (int i = 0; i < pros.length; i++) {
				boolean subset = true;
				HashSet<String> pepSet = tempProPepMap.get(pros[i]);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						subset = false;
					}
				}
				if (subset) {
					HashSet<String> pepSetI = tempProPepMap.get(pros[i]);
					for (String pep : pepSetI) {
						HashSet<String> proSet = tempPepProMap.get(pep);
						proSet.remove(pros[i]);
					}
					tempProPepMap.remove(pros[i]);
					pgMap.remove(pros[i]);
				}
			}

			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}

		} else {

			// remove redundant proteins
			Iterator<String> pepIt = tempPepProMap.keySet().iterator();
			while (pepIt.hasNext()) {
				String pep = pepIt.next();
				HashSet<String> proSet = tempPepProMap.get(pep);

				Iterator<String> proIt = proSet.iterator();
				while (proIt.hasNext()) {
					String pro = proIt.next();
					if (!pgMap.containsKey(pro)) {
						proIt.remove();
					}
				}
			}

			Iterator<String> proIt = tempProPepMap.keySet().iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!pgMap.containsKey(pro)) {
					proIt.remove();
				}
			}

			// find unique proteins
			HashSet<String> uniqueProSet = new HashSet<String>();
			HashSet<String> uniquePepSet = new HashSet<String>();
			for (String pro : pgMap.keySet()) {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				for (String pep : pepSet) {
					HashSet<String> proSet = tempPepProMap.get(pep);
					if (proSet.size() == 1) {
						uniqueProSet.addAll(proSet);
						uniquePepSet.addAll(pepSet);
						break;
					}
				}
			}

			HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
			HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
			for (String pro : tempProPepMap.keySet()) {
				if (uniqueProSet.contains(pro)) {
					uniquePgMap.put(pro, pgMap.get(pro));
					pgMap.remove(pro);
				} else {
					HashSet<String> pepSet = tempProPepMap.get(pro);
					HashSet<String> tempPepSet = new HashSet<String>();
					for (String pep : pepSet) {
						if (!uniquePepSet.contains(pep)) {
							tempPepSet.add(pep);
						}
					}

					if (tempPepSet.size() > 0) {
						tempProPepMap2.put(pro, tempPepSet);
						for (String pep : tempPepSet) {
							if (tempPepProMap2.containsKey(pep)) {
								tempPepProMap2.get(pep).add(pro);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(pro);
								tempPepProMap2.put(pep, set);
							}
						}
					} else {
						pgMap.remove(pro);
					}
				}
			}

			if (pgMap.size() > 0) {
				combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);
			}
		}
	}

	/**
	 * get razor protein groups
	 * 
	 * @param proPepMap
	 * @param finalPepProMap
	 * @return
	 */
	protected HashMap<String, ProteinGroup> getProteinGroups(HashMap<String, HashSet<String>> totalPepProMap,
			HashSet<String> razorProSet) {

		HashMap<String, HashSet<String>> tempPepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap = new HashMap<String, HashSet<String>>();

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			HashSet<String> tempProSet = new HashSet<String>();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!razorProSet.contains(pro)) {
					proIt.remove();
				} else {
					if (tempProPepMap.containsKey(pro)) {
						tempProPepMap.get(pro).add(pep);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(pep);
						tempProPepMap.put(pro, set);
					}
					tempProSet.add(pro);
				}
			}
			tempPepProMap.put(pep, tempProSet);
		}

		HashSet<String> uniqueProSet = new HashSet<String>();
		HashSet<String> uniquePepSet = new HashSet<String>();
		for (String pro : tempProPepMap.keySet()) {
			HashSet<String> pepSet = tempProPepMap.get(pro);
			for (String pep : pepSet) {
				HashSet<String> proSet = tempPepProMap.get(pep);
				if (proSet.size() == 1) {
					uniqueProSet.add(pro);
					uniquePepSet.addAll(pepSet);
					break;
				}
			}
		}

		HashMap<String, ProteinGroup> uniquePgMap = new HashMap<String, ProteinGroup>(uniqueProSet.size());
		HashMap<String, ProteinGroup> pgMap = new HashMap<String, ProteinGroup>(tempProPepMap.size());
		HashMap<String, HashSet<String>> tempPepProMap2 = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> tempProPepMap2 = new HashMap<String, HashSet<String>>();
		for (String pro : tempProPepMap.keySet()) {
			if (uniqueProSet.contains(pro)) {
				uniquePgMap.put(pro, new ProteinGroup(pro));
			} else {
				HashSet<String> pepSet = tempProPepMap.get(pro);
				HashSet<String> tempPepSet = new HashSet<String>();
				for (String pep : pepSet) {
					if (!uniquePepSet.contains(pep)) {
						tempPepSet.add(pep);
					}
				}

				if (tempPepSet.size() > 0) {
					tempProPepMap2.put(pro, tempPepSet);
					for (String pep : tempPepSet) {
						if (tempPepProMap2.containsKey(pep)) {
							tempPepProMap2.get(pep).add(pro);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(pro);
							tempPepProMap2.put(pep, set);
						}
					}
					pgMap.put(pro, new ProteinGroup(pro));
				}
			}
		}

		combineProteinGroups(pgMap, tempProPepMap2, tempPepProMap2, uniquePgMap);

		for (String pep : totalPepProMap.keySet()) {
			HashSet<String> proSet = totalPepProMap.get(pep);
			Iterator<String> proIt = proSet.iterator();
			while (proIt.hasNext()) {
				String pro = proIt.next();
				if (!uniquePgMap.containsKey(pro)) {
					proIt.remove();
				}
			}
		}

		return uniquePgMap;
	}

	protected boolean createSSDB() {

		LOGGER.info(getTaskName() + ": searching high abundant database started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": searching high abundant database started");

		boolean searchHap = this.separateSearchHap();

		if (searchHap) {
			LOGGER.info(getTaskName() + ": searching high abundant database finished");
			System.out.println(
					format.format(new Date()) + "\t" + getTaskName() + ": searching high abundant database finished");
		} else {
			LOGGER.error(getTaskName() + ": error in searching high abundant database");
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in searching high abundant database");

			return false;
		}

		LOGGER.info(getTaskName() + ": extracting proteins from the HAP result started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": extracting proteins from the HAP result started");

		boolean extract = this.fastaExtract4Iterator();

		if (extract) {
			LOGGER.info(getTaskName() + ": extracting proteins from the HAP result finished");
			System.out.println(
					format.format(new Date()) + "\t" + getTaskName() + ": extracting proteins from the HAP result finished");
		} else {
			LOGGER.error(getTaskName() + ": error in extracting proteins from the HAP result");
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in extracting proteins from the HAP result");

			return false;
		}

		setProgress(40);

		return true;
	}
	
	protected boolean iden() {

		bar2.setString(getTaskName() + ": peptide and protein identification");

		LOGGER.info(getTaskName() + ": peptide and protein identification started");
		System.out
				.println(format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein identification started");

		boolean search = separateSearch();

		if (search) {
			LOGGER.info(getTaskName() + ": database search through the sample-specific database finished");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": database search through the sample-specific database finished");
		} else {
			LOGGER.error(getTaskName() + ": error in database search");
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in database search");

			return false;
		}

		boolean combine = combineResult();

		if (combine) {
			LOGGER.info(getTaskName() + ": combining of the separate search results finished");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": combining of the separate search results finished");
		} else {
			LOGGER.error(getTaskName() + ": error in the combination of the separate search results");
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in the combination of the separate search results");
			return false;
		}

		LOGGER.info(getTaskName() + ": peptide and protein identification finished");
		System.out
				.println(format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein identification finished");

		setProgress(70);
		
		return true;
	}
	

	protected boolean lfQuant() {

		if (isBrukD || isMzML) {
			return true;
		}

		bar2.setString(getTaskName() + ": peptide and protein quantification");

		LOGGER.info(getTaskName() + ": peptide and protein label-free quantification started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName()
				+ ": peptide and protein label-free quantification started");

		String flashLFQ = ((MetaSourcesV2) msv).getFlashlfq();

		FlashLfqTask flashLfqTask = new FlashLfqTask(flashLFQ, metaPar.getThreadCount());

		this.quan_pep_file = new File(resultFolderFile, "QuantifiedPeptides.tsv");
		if (!quan_pep_file.exists() || quan_pep_file.length() == 0) {

			File msms = new File(resultFolderFile, "psms.tsv");
			boolean lfqQuan = flashLfqTask.run(metadata, msms.getAbsolutePath(), rawDir.getAbsolutePath(), true, true);
			if (!lfqQuan) {

				LOGGER.info(getTaskName() + ": error in peptide and protein label-free quantification");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in peptide and protein label-free quantification");

				LOGGER.info(getTaskName()
						+ ": try to fix this problem by install the dotNET framework, please download from "
						+ "https://aka.ms/dotnet-core-applaunch?missing_runtime=true&arch=x64&rid=win10-x64&apphost_version=5.0.14");
				System.out.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": try to fix this problem by install the dotNET framework, please download from "
						+ "https://aka.ms/dotnet-core-applaunch?missing_runtime=true&arch=x64&rid=win10-x64&apphost_version=5.0.14");

				return false;

			} else {
				if (!this.quan_pep_file.exists()) {

					LOGGER.info(getTaskName() + ": error in peptide and protein label-free quantification");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": error in peptide and protein label-free quantification");

					LOGGER.info(getTaskName()
							+ ": try to fix this problem by install the dotNET framework, please download from "
							+ "https://aka.ms/dotnet-core-applaunch?missing_runtime=true&arch=x64&rid=win10-x64&apphost_version=5.0.14");
					System.out.println(format.format(new Date()) + "\t" + getTaskName()
							+ ": try to fix this problem by install the dotNET framework, please download from "
							+ "https://aka.ms/dotnet-core-applaunch?missing_runtime=true&arch=x64&rid=win10-x64&apphost_version=5.0.14");

					return false;

				}
			}
		} else {
			LOGGER.info(getTaskName() + ": peptide quantification result has been found in " + quan_pep_file
					+ ", go to next step");
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": peptide quantification result has been found in " + quan_pep_file + ", go to next step");
		}

		LOGGER.info(getTaskName() + ": peptide and protein quantification label-free finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName()
				+ ": peptide and protein label-free quantification finished");

		return true;
	}

	protected boolean isobaricQuant() {

		if (isBrukD || isMzML) {
			return true;
		}

		bar2.setString(getTaskName() + ": peptide and protein quantification");

		LOGGER.info(getTaskName() + ": peptide and protein isobaric quantification started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein isobaric quantification started");
		
		ArrayList<PFindPSM> psmList = new ArrayList<PFindPSM>();
		for (int i = 0; i < this.separateResultFolders.length; i++) {
			PFindPsmReader psmReader = new PFindPsmReader(new File(this.separateResultFolders[i], "pFind.spectra"));
			PFindPSM[] psms = psmReader.getPsms();
			for (int j = 0; j < psms.length; j++) {
				psmList.add(psms[j]);
			}
		}
		PFindPSM[] psms = psmList.toArray(new PFindPSM[psmList.size()]);
		PFindIsobaricTask pfindIsobaricTask = new PFindIsobaricTask(psms, this.metaPar.getSpectraFile(),
				resultFolderFile, ((MetaParameterPFind) this.metaPar).getIsobaricTag(),
				((MetaParameterPFind) this.metaPar).getIsobaric(),
				((MetaParameterPFind) this.metaPar).getIsoCorFactor(),
				((MetaParameterPFind) this.metaPar).isCombineLabel(), metadata);

		try {
			pfindIsobaricTask.runOnlyPeptide();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in quantifying PSMs", e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in quantifying PSMs");
		}

		this.quan_pep_file = pfindIsobaricTask.getQuanPepFile();

		if (quan_pep_file.exists()) {
			LOGGER.info(getTaskName() + ": quantification results have been fount in " + resultFolderFile);
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": quantification results have been fount in " + resultFolderFile);
			return true;
		}

		if (!this.quan_pep_file.exists()) {
			return false;
		}

		LOGGER.info(getTaskName() + ": peptide and protein isobaric quantification finished");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide and protein isobaric quantification finished");

		return true;
	}
	
	protected String getVersion() {
		return MetaParaIOMag.version;
	}
	
	/**
	 * The version is different from the parent class
	 */
	protected boolean exportReportPFind() {

		bar2.setString(getTaskName() + ": exporting report");

		LOGGER.info(getTaskName() + ": exporting report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting report started");

		File reportDir = new File(this.resultFolderFile, "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, getVersion());

		metaPar.setReportDir(reportDir);

		this.reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportHtmlDir.exists()) {
			reportHtmlDir.mkdirs();
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());
		
		this.summaryMetaFile = new File(this.resultFolderFile, "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean summayrFinished = this.exportSummary(task);
		
		setProgress(82);
		
		this.final_pep_txt = new File(this.resultFolderFile, "final_peptides.tsv");

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			this.exportPeptideTxt();
		}

		boolean pepReportOk = false;
		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		} else {
			File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
			if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					pepReportOk = task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary,
							summaryMetaFile);
				} else {
					pepReportOk = task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary);
				}
			} else {
				pepReportOk = true;
			}
		}

		setProgress(84);
		
		this.final_pro_txt = new File(this.resultFolderFile, "final_proteins.tsv");

		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			this.exportProteinsTxt();
		}

		boolean proReportOk = false;
		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			return false;
		} else {
			File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
			if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					proReportOk = task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary,
							summaryMetaFile);
				} else {
					proReportOk = task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary);
				}
			} else {
				proReportOk = true;
			}
		}
		
		setProgress(86);

		boolean taxFuncReportOk = exportTaxaFunc(task);

		if (!summayrFinished || !pepReportOk || !proReportOk || !taxFuncReportOk) {
			setProgress(99);

			LOGGER.info(getTaskName() + ": error in generating the reports");
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in generating the reports");

			return false;
		}
		
		task.run();

		try {
			boolean finish = task.get();
			if (finish) {
				
				LOGGER.info(getTaskName() + ": report generation successes");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": report generation successes");
				
				setProgress(100);
				return true;
			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	protected boolean exportReport() {

		LOGGER.info(getTaskName() + ": exporting peptide report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting peptide report started");

		ArrayList<String> fileList = new ArrayList<String>();
		HashMap<String, Integer> fileIdMap = new HashMap<String, Integer>();
		File expDesignFile = new File(rawDir, "ExperimentalDesign.tsv");
		
		if (expDesignFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(expDesignFile))) {
				String line = reader.readLine();
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					fileIdMap.put(cs[0], fileIdMap.size());
					fileList.add(cs[0]);
				}
				reader.close();
			} catch (IOException e) {
				LOGGER.error(getTaskName() + ": error in reading the experimental design from " + expDesignFile, e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the experimental design from " + expDesignFile);
			}
		} else {
			File[] files = resultFolderFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory() && (new File(files[i], "pFind-Filtered.spectra")).exists()) {
					fileIdMap.put(files[i].getName(), fileIdMap.size());
					fileList.add(files[i].getName());
				}
			}
		}

		HashMap<String, FlashLfqPsm> flashLfqPsmMap = new HashMap<String, FlashLfqPsm>();
		HashMap<String, int[]> pepPsmCountMap = new HashMap<String, int[]>();
		HashMap<String, int[]> pepChargeMap = new HashMap<String, int[]>();
		HashMap<String, double[]> pepExpectMap = new HashMap<String, double[]>();
		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();

		int[] filePsmCount = new int[fileIdMap.size()];
		HashSet<String>[] fileSequenceSets = new HashSet[fileIdMap.size()];
		for (int i = 0; i < fileSequenceSets.length; i++) {
			fileSequenceSets[i] = new HashSet<String>();
		}

		try {
			FlashLfqPsmReader flashLfqPsmReader = new FlashLfqPsmReader(new File(resultFolderFile, "psms.tsv"));
			FlashLfqPsm[] psms = flashLfqPsmReader.getPSMs();
			for (FlashLfqPsm psm : psms) {
				String sequence = psm.getBaseSequence();
				String fileName = psm.getFileName();
				String modseq = psm.getFullSequence();
				int charge = psm.getCharge();
				double PEP = psm.getPEP();
				int fileId = fileIdMap.get(fileName);

				int[] psmCount;
				if (pepPsmCountMap.containsKey(modseq)) {
					psmCount = pepPsmCountMap.get(modseq);
				} else {
					psmCount = new int[fileIdMap.size()];
					pepPsmCountMap.put(modseq, psmCount);
				}
				if (fileIdMap.containsKey(fileName)) {
					psmCount[fileId]++;
				}

				filePsmCount[fileId]++;
				fileSequenceSets[fileId].add(sequence);

				int[] charges;
				if (pepChargeMap.containsKey(modseq)) {
					charges = pepChargeMap.get(modseq);
				} else {
					charges = new int[7];
					pepChargeMap.put(modseq, charges);
				}
				if (charge < charges.length) {
					charges[charge] = 1;
				}
				double[] expect;
				if (pepExpectMap.containsKey(modseq)) {
					expect = pepExpectMap.get(modseq);
				} else {
					expect = new double[fileIdMap.size()];
					Arrays.fill(expect, 1.0);
					pepExpectMap.put(modseq, expect);
				}
				if (PEP < expect[fileId]) {
					expect[fileId] = PEP;
				}

				if (flashLfqPsmMap.containsKey(modseq)) {
					if (PEP < flashLfqPsmMap.get(modseq).getPEP()) {
						flashLfqPsmMap.put(modseq, psm);
					}
				} else {
					flashLfqPsmMap.put(modseq, psm);
				}

				String[] pros = psm.getProteins();
				for (String pro : pros) {
					if (psmCountMap.containsKey(pro)) {
						psmCountMap.put(pro, psmCountMap.get(pro) + 1);
					} else {
						psmCountMap.put(pro, 1);
					}

					if (pro.startsWith(this.magDb.getIdentifier())) {
						String genome = pro.split("_")[0];
						if (psmCountMap.containsKey(genome)) {
							psmCountMap.put(genome, psmCountMap.get(genome) + 1);
						} else {
							psmCountMap.put(genome, 1);
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading the PSMs from  " + (new File(resultFolderFile, "psms.tsv")), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in reading the PSMs from  "
					+ (new File(resultFolderFile, "psms.tsv")));
		}
		
		FlashLfqQuanPepReader quanPepReader;

		if (this.quanMode.equals(MetaConstants.labelFree)) {
			quanPepReader = new FlashLfqQuanPepReader(this.quan_pep_file, fileList);
		} else if (this.quanMode.equals(MetaConstants.isobaricLabel)) {
			IsobaricTag isoTag = ((MetaParameterMag) this.metaPar).getIsobaricTag();
			quanPepReader = new FlashLfqQuanPepReader(this.quan_pep_file, fileList, isoTag.getReportTitle());
		} else {
			LOGGER.error(getTaskName() + ": unknown quantitative type");
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": unknown quantitative type");
			return false;
		}

		FlashLfqQuanPeptide[] quanPeps = quanPepReader.getMetaPeptides();
		for (int i = 0; i < quanPeps.length; i++) {
			String seq = quanPeps[i].getModSeq();
			if (pepExpectMap.containsKey(seq)) {
				quanPeps[i].setIndividualPEP(pepExpectMap.get(seq));
			} else {
				double[] expect = new double[fileIdMap.size()];
				Arrays.fill(expect, 1.0);
				quanPeps[i].setIndividualPEP(expect);
			}
		}

		HashMap<String, Double> genomeScoreMap = this.refineGenomes(quanPeps);

		String[] razorProteins = this.refineProteins(quanPeps, genomeScoreMap);
		
		setProgress(82);

		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> proteinIntensityMap = new HashMap<String, double[]>();
		HashMap<String, Double> proteinTotalIntenMap = new HashMap<String, Double>();

		HashSet<String> pepSequenceSet = new HashSet<String>();
		HashMap<String, HashSet<String>> totalPepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> razorPepCountMap = new HashMap<String, HashSet<String>>();

		ArrayList<FlashLfqQuanPeptide> peptideList = new ArrayList<FlashLfqQuanPeptide>();

		for (int i = 0; i < quanPeps.length; i++) {
			if (razorProteins[i] != null) {
				String stripeSequence = quanPeps[i].getSequence();

				pepSequenceSet.add(stripeSequence);
				peptideList.add(quanPeps[i]);

				if (razorPepCountMap.containsKey(razorProteins[i])) {
					razorPepCountMap.get(razorProteins[i]).add(stripeSequence);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(stripeSequence);
					razorPepCountMap.put(razorProteins[i], set);
				}

				if (razorProteins[i].startsWith(magDb.getIdentifier())) {
					String genome = razorProteins[i].split("_")[0];
					if (razorPepCountMap.containsKey(genome)) {
						razorPepCountMap.get(genome).add(stripeSequence);
					} else {
						HashSet<String> set = new HashSet<String>();
						set.add(stripeSequence);
						razorPepCountMap.put(genome, set);
					}
				}
			}
		}
		
		for (int i = 0; i < quanPeps.length; i++) {
			String[] proteins = quanPeps[i].getProteins();
			int count = 0;
			for (int j = 0; j < proteins.length; j++) {
				if (razorPepCountMap.containsKey(proteins[j])) {
					count++;
				}
			}

			if (count > 0) {
				String stripeSequence = quanPeps[i].getSequence();
				double[] pepIntensity = new double[quanPeps[i].getIntensity().length];
				for (int j = 0; j < pepIntensity.length; j++) {
					pepIntensity[j] = quanPeps[i].getIntensity()[j] / (double) count;
				}

				for (int j = 0; j < proteins.length; j++) {
					if (razorPepCountMap.containsKey(proteins[j])) {
						if (proteins[j].startsWith(magDb.getIdentifier())) {
							String genome = proteins[j].split("_")[0];
							if (genomeScoreMap.containsKey(genome)) {
								double[] genomeIntensity;
								if (genomeIntensityMap.containsKey(genome)) {
									genomeIntensity = genomeIntensityMap.get(genome);
								} else {
									genomeIntensity = new double[pepIntensity.length];
									genomeIntensityMap.put(genome, genomeIntensity);
								}
								for (int k = 0; k < pepIntensity.length; k++) {
									genomeIntensity[k] += pepIntensity[k];
								}

								double proteinTotalInten;
								double[] proteinIntensity;
								if (proteinIntensityMap.containsKey(proteins[j])) {
									proteinIntensity = proteinIntensityMap.get(proteins[j]);
									proteinTotalInten = proteinTotalIntenMap.get(proteins[j]);
								} else {
									proteinIntensity = new double[pepIntensity.length];
									proteinIntensityMap.put(proteins[j], proteinIntensity);
									proteinTotalInten = 0.0;
								}
								for (int k = 0; k < pepIntensity.length; k++) {
									proteinIntensity[k] += pepIntensity[k];
									proteinTotalInten += pepIntensity[k];
								}
								proteinTotalIntenMap.put(proteins[j], proteinTotalInten);

								if (totalPepCountMap.containsKey(genome)) {
									totalPepCountMap.get(genome).add(stripeSequence);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(stripeSequence);
									totalPepCountMap.put(genome, set);
								}

								if (totalPepCountMap.containsKey(proteins[j])) {
									totalPepCountMap.get(proteins[j]).add(stripeSequence);
								} else {
									HashSet<String> set = new HashSet<String>();
									set.add(stripeSequence);
									totalPepCountMap.put(proteins[j], set);
								}
							}
						} else {
							double proteinTotalInten;
							double[] proteinIntensity;
							if (proteinIntensityMap.containsKey(proteins[j])) {
								proteinIntensity = proteinIntensityMap.get(proteins[j]);
								proteinTotalInten = proteinTotalIntenMap.get(proteins[j]);
							} else {
								proteinIntensity = new double[pepIntensity.length];
								proteinIntensityMap.put(proteins[j], proteinIntensity);
								proteinTotalInten = 0.0;
							}
							for (int k = 0; k < pepIntensity.length; k++) {
								proteinIntensity[k] += pepIntensity[k];
								proteinTotalInten += pepIntensity[k];
							}
							proteinTotalIntenMap.put(proteins[j], proteinTotalInten);

							if (totalPepCountMap.containsKey(proteins[j])) {
								totalPepCountMap.get(proteins[j]).add(stripeSequence);
							} else {
								HashSet<String> set = new HashSet<String>();
								set.add(stripeSequence);
								totalPepCountMap.put(proteins[j], set);
							}
						}
					}
				}
			}
		}

		String[] proteins = proteinTotalIntenMap.keySet().toArray(String[]::new);
		Arrays.sort(proteins, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				if (proteinTotalIntenMap.get(o1) > proteinTotalIntenMap.get(o2))
					return -1;
				if (proteinTotalIntenMap.get(o1) < proteinTotalIntenMap.get(o2))
					return 1;
				return 0;
			}
		});

		HashMap<String, Integer> proteinIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < proteins.length; i++) {
			proteinIdMap.put(proteins[i], i);
		}

		try (PrintWriter idWriter = new PrintWriter(new File(resultFolderFile, "id.tsv"))) {
			idWriter.println("Raw file\tUnique peptide count\tPSM count");

			String[] rawNames = fileIdMap.keySet().toArray(new String[fileIdMap.size()]);
			Arrays.sort(rawNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					return fileIdMap.get(o1) - fileIdMap.get(o2);
				}
			});

			for (int i = 0; i < rawNames.length; i++) {
				idWriter.println(rawNames[i] + "\t" + fileSequenceSets[i].size() + "\t" + filePsmCount[i]);
			}
			idWriter.close();
		} catch (IOException e) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing identification information to " + new File(resultFolderFile, "id.tsv"));
			LOGGER.error(getTaskName() + ": error in writing identification information to "
					+ new File(resultFolderFile, "id.tsv"), e);
		}

		setProgress(85);

		HashMap<String, ArrayList<Integer>> proPepIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> proPepRazorIdMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, Double> proScoreMap = new HashMap<String, Double>();
		try {
			this.final_pep_txt = new File(this.resultFolderFile, "final_peptides.tsv");

			PrintWriter writer = new PrintWriter(this.final_pep_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Sequence").append("\t");
			titlesb.append("Base sequence").append("\t");
			titlesb.append("Length").append("\t");
			titlesb.append("Missed cleavages").append("\t");
			titlesb.append("Mass").append("\t");
			titlesb.append("Proteins").append("\t");
			titlesb.append("Charges").append("\t");
			titlesb.append("Score").append("\t");
			titlesb.append("PEP").append("\t");

			for (int i = 0; i < fileList.size(); i++) {
				titlesb.append("Identification type " + fileList.get(i)).append("\t");
			}

			for (int i = 0; i < fileList.size(); i++) {
				titlesb.append("MS/MS count " + fileList.get(i)).append("\t");
			}

			titlesb.append("Intensity").append("\t");

			if (this.quanMode.equals(MetaConstants.labelFree)) {
				for (int i = 0; i < fileList.size(); i++) {
					titlesb.append("Intensity " + fileList.get(i)).append("\t");
				}
			} else if (this.quanMode.equals(MetaConstants.isobaricLabel)) {
				IsobaricTag isoTag = ((MetaParameterMag) this.metaPar).getIsobaricTag();
				String[] repoTitle = isoTag.getReportTitle();
				for (int i = 0; i < fileList.size(); i++) {
					for (int j = 0; j < repoTitle.length; j++) {
						titlesb.append("Intensity " + fileList.get(i) + "_" + repoTitle[j]).append("\t");
					}
				}
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Protein group IDs").append("\t");
			titlesb.append("MS/MS Count");

			writer.println(titlesb);

			int id = 0;
			for (FlashLfqQuanPeptide peptide : peptideList) {

				String fullSeq = peptide.getModSeq();
				String sequence = peptide.getSequence();

				FlashLfqPsm psm = flashLfqPsmMap.get(fullSeq);
				if (psm == null) {
					continue;
				}

				StringBuilder sb = new StringBuilder();
				sb.append(fullSeq).append("\t");
				sb.append(sequence).append("\t");
				sb.append(sequence.length()).append("\t");
				sb.append(psm.getMiss()).append("\t");
				sb.append(psm.getCalMass()).append("\t");

				String[] pros = peptide.getProteins();

				for (String pro : pros) {
					if (proteinIdMap.containsKey(pro)) {
						sb.append(pro).append(";");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				int[] charge = pepChargeMap.get(fullSeq);
				StringBuilder chargesb = new StringBuilder();
				for (int j = 0; j < charge.length; j++) {
					if (charge[j] == 1) {
						chargesb.append(j).append(";");
					}
				}
				if (chargesb.length() > 1) {
					sb.append(chargesb.subSequence(0, chargesb.length() - 1)).append("\t");
				} else {
					sb.append("2").append("\t");
				}

				sb.append(psm.getScore()).append("\t");
				sb.append(psm.getPEP()).append("\t");

				int totalMsCount = 0;
				int[] psmCount = pepPsmCountMap.get(fullSeq);
				double[] intensity = peptide.getIntensity();
				for (int j = 0; j < psmCount.length; j++) {
					if (psmCount[j] > 0) {
						totalMsCount += psmCount[j];
						sb.append("By MS/MS").append("\t");
					} else {
						if (intensity[j] > 0) {
							sb.append("MBR\t");
						} else {
							sb.append("NotDetected\t");
						}
					}
				}

				for (int j = 0; j < psmCount.length; j++) {
					sb.append(psmCount[j]).append("\t");
				}

				double totalIntensity = 0;
				for (int j = 0; j < intensity.length; j++) {
					totalIntensity += intensity[j];
				}

				sb.append(BigInteger.valueOf((long) Math.floor(totalIntensity))).append("\t");

				for (int j = 0; j < intensity.length; j++) {
					sb.append(BigInteger.valueOf((long) Math.floor(intensity[j]))).append("\t");
				}

				sb.append("").append("\t");
				sb.append("").append("\t");

				sb.append(id).append("\t");

				for (String pro : pros) {
					if (proteinIdMap.containsKey(pro)) {
						sb.append(proteinIdMap.get(pro)).append(";");

						if (proPepIdMap.containsKey(pro)) {
							proPepIdMap.get(pro).add(id);
						} else {
							ArrayList<Integer> list = new ArrayList<Integer>();
							list.add(id);
							proPepIdMap.put(pro, list);
						}

						if (proScoreMap.containsKey(pro)) {
							proScoreMap.put(pro, proScoreMap.get(pro) * psm.getPEP());
						} else {
							proScoreMap.put(pro, psm.getPEP());
						}

						HashSet<String> rSet = razorPepCountMap.get(pro);
						if (proPepRazorIdMap.containsKey(pro)) {
							if (rSet.contains(sequence)) {
								proPepRazorIdMap.get(pro).add(1);
							} else {
								proPepRazorIdMap.get(pro).add(0);
							}
						} else {
							ArrayList<Integer> list = new ArrayList<Integer>();
							if (rSet.contains(sequence)) {
								list.add(1);
							} else {
								list.add(0);
							}
							proPepRazorIdMap.put(pro, list);
						}
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				sb.append(totalMsCount);

				writer.println(sb);
				id++;
			}

			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing the peptides to  " + final_pep_txt, e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in writing the peptides to  "
					+ final_pep_txt);
		}

		LOGGER.info(getTaskName() + ": peptide report has been exported to " + final_pep_txt);
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": peptide report has been exported to " + final_pep_txt);

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		}

		setProgress(87);

		LOGGER.info(getTaskName() + ": exporting protein report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting protein report started");

		ArrayList<MetaProtein> proteinList = new ArrayList<MetaProtein>();
		try {

			this.final_pro_txt = new File(this.resultFolderFile, "final_proteins.tsv");

			PrintWriter writer = new PrintWriter(this.final_pro_txt);

			StringBuilder titlesb = new StringBuilder();
			titlesb.append("Protein IDs").append("\t");
//			titlesb.append("Majority protein IDs").append("\t");
			titlesb.append("Peptide counts (all)").append("\t");
			titlesb.append("Peptide counts (razor)").append("\t");
			titlesb.append("Number of PSMs").append("\t");
			titlesb.append("PEP").append("\t");
			titlesb.append("Intensity").append("\t");
			
			if (this.quanMode.equals(MetaConstants.labelFree)) {
				for (int i = 0; i < fileList.size(); i++) {
					titlesb.append("Intensity " + fileList.get(i)).append("\t");
				}
			} else if (this.quanMode.equals(MetaConstants.isobaricLabel)) {
				IsobaricTag isoTag = ((MetaParameterMag) this.metaPar).getIsobaricTag();
				String[] repoTitle = isoTag.getReportTitle();
				for (int i = 0; i < fileList.size(); i++) {
					for (int j = 0; j < repoTitle.length; j++) {
						titlesb.append("Intensity " + fileList.get(i) + "_" + repoTitle[j]).append("\t");
					}
				}
			}

			titlesb.append("Reverse").append("\t");
			titlesb.append("Potential contaminant").append("\t");
			titlesb.append("id").append("\t");
			titlesb.append("Peptide IDs").append("\t");
			titlesb.append("Peptide is razor");

			writer.println(titlesb);

			DecimalFormat df = FormatTool.getDFE4();
			for (int i = 0; i < proteins.length; i++) {

				StringBuilder sb = new StringBuilder();
				sb.append(proteins[i]).append("\t");
				sb.append(totalPepCountMap.get(proteins[i]).size()).append("\t");
				sb.append(razorPepCountMap.get(proteins[i]).size()).append("\t");
				sb.append(psmCountMap.get(proteins[i])).append("\t");
				sb.append(proScoreMap.get(proteins[i])).append("\t");

				double totalIntensity = proteinTotalIntenMap.get(proteins[i]);
				sb.append(df.format(totalIntensity)).append("\t");

				double[] intensity = proteinIntensityMap.get(proteins[i]);
				for (int j = 0; j < intensity.length; j++) {
					sb.append(df.format(intensity[j])).append("\t");
				}
				sb.append("\t");
				sb.append("\t");
				sb.append(i).append("\t");

				ArrayList<Integer> pepIdList = proPepIdMap.get(proteins[i]);
				ArrayList<Integer> pepRazorList = proPepRazorIdMap.get(proteins[i]);

				for (int j = 0; j < pepIdList.size(); j++) {
					sb.append(pepIdList.get(j)).append(";");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\t");

				for (int j = 0; j < pepRazorList.size(); j++) {
					if (pepRazorList.get(j) == 1) {
						sb.append("true;");
					} else {
						sb.append("false;");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				writer.println(sb);

				MetaProtein mp = new MetaProtein(i, 0, proteins[i], totalPepCountMap.get(proteins[i]).size(),
						psmCountMap.get(proteins[i]), proScoreMap.get(proteins[i]), intensity);
				proteinList.add(mp);
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in writing final protein result to " + this.final_pro_txt.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in writing final protein result to " + this.final_pro_txt.getName());
		}

		LOGGER.info(getTaskName() + ": protein report has been exported to " + final_pro_txt.getName());
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": protein report has been exported to "
				+ final_pro_txt.getName());

		setProgress(89);

		LOGGER.info(getTaskName() + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation started");

		File funcFile = new File(metaPar.getDbSearchResultFile(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		File funTsv = new File(funcFile, "functions.tsv");
		File funReportTsv = new File(funcFile, "functions_report.tsv");
		final_pro_xml_file = new File(funcFile, "functions.xml");

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();
		MetaProtein[] metapros = proteinList.toArray(new MetaProtein[proteinList.size()]);

		String[] intensityTitle;
		if (this.quanMode.equals(MetaConstants.labelFree)) {
			intensityTitle = fileList.toArray(String[]::new);
		} else if (this.quanMode.equals(MetaConstants.isobaricLabel)) {
			IsobaricTag isoTag = ((MetaParameterMag) this.metaPar).getIsobaricTag();
			String[] repoTitle = isoTag.getReportTitle();
			intensityTitle = new String[fileList.size() * repoTitle.length];

			for (int i = 0; i < fileList.size(); i++) {
				for (int j = 0; j < repoTitle.length; j++) {
					intensityTitle[i * repoTitle.length + j] = fileList.get(i) + "_" + repoTitle[j];
				}
			}
		} else {
			return false;
		}

		MetaProteinAnnoMag[] mpas = funcAnnoSqlite(metapros, intensityTitle);
		for (int i = 0; i < mpas.length; i++) {
			String name = mpas[i].getPro().getName();
			if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
				proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
			} else {
				proTaxIdMap.put(name, 131567);
			}
		}

		MetaProteinXMLReader2 funReader = null;
		try {
			funReader = new MetaProteinXMLReader2(final_pro_xml_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading protein function information from "
					+ this.final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading protein function information from " + this.quan_pro_file.getName());
		}

		if (!funTsv.exists() || !funReportTsv.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + funTsv.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " finished");
		}

		File html = new File(funcFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " finished");
		}

		setProgress(91);

		// taxonomy analysis

		LOGGER.info(getTaskName() + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis started");

		File taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		MagFuncSearcher searcher = null;
		try {
			searcher = new MagFuncSearcher(metaPar.getMicroDb(), msv.getFuncDef());

		} catch (NumberFormatException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in genome analysis, task failed", e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in genome analysis, task failed");

			return false;
		}

		String[] genomes = genomeIntensityMap.keySet().toArray(new String[genomeIntensityMap.size()]);
		HashMap<String, String[]> genomeTaxaMap = null;
		try {
			genomeTaxaMap = searcher.matchGenome(genomes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": Error in searching the genome2taxa database", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in searching the genome2taxa database");
			return false;
		}

		File genomeHtmlFile = new File(taxFile, "Genome.html");
		try {
			HMGenomeHtmlWriter htmlWriter = new HMGenomeHtmlWriter(genomeHtmlFile);
			htmlWriter.write(genomeTaxaMap);
			htmlWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeHtmlFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeHtmlFile);
			return false;
		}

		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		double[] cellularIntensity = new double[intensityTitle.length];
		HashMap<String, String[]>[] taxRankMaps = new HashMap[mainRanks.length];
		HashMap<String, double[]>[] taxIntensityMaps = new HashMap[mainRanks.length];
		HashMap<String, Double>[] taxScoreMaps = new HashMap[mainRanks.length];
		for (int i = 0; i < taxRankMaps.length; i++) {
			taxRankMaps[i] = new HashMap<String, String[]>();
			taxIntensityMaps[i] = new HashMap<String, double[]>();
			taxScoreMaps[i] = new HashMap<String, Double>();
		}

		File genomeFile = new File(taxFile, "Genome.tsv");
		PrintWriter genomeWriter = null;
		try {
			genomeWriter = new PrintWriter(genomeFile);
			StringBuilder genomeTitlesb = new StringBuilder();
			genomeTitlesb.append("Genome\t");
			genomeTitlesb.append("Minus_log(p-value)\t");
			genomeTitlesb.append("PSM count\t");
			genomeTitlesb.append("Peptide count\t");
			genomeTitlesb.append("Razor peptide count\t");
			genomeTitlesb.append("GTDB_Name\t");
			genomeTitlesb.append("GTDB_Rank\t");

			for (int i = 0; i < mainRanks.length; i++) {
				genomeTitlesb.append("GTDB_").append(mainRanks[i].getName()).append("\t");
			}

			genomeTitlesb.append("NCBI_Name\t");
			genomeTitlesb.append("NCBI_Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				genomeTitlesb.append("\t").append("NCBI_").append(mainRanks[i].getName());
			}

			for (String exp : intensityTitle) {
				if (exp.startsWith("Intensity ")) {
					genomeTitlesb.append("\t").append(exp);
				} else {
					genomeTitlesb.append("\tIntensity ").append(exp);
				}
			}

			genomeWriter.println(genomeTitlesb);

			setProgress(94);

			for (int i = 0; i < genomes.length; i++) {
				String[] taxaContent = genomeTaxaMap.get(genomes[i]);
				if (genomeIntensityMap.containsKey(genomes[i])) {
					double[] genomeIntensity = genomeIntensityMap.get(genomes[i]);
					for (int j = 0; j < genomeIntensity.length; j++) {
						cellularIntensity[j] += genomeIntensity[j];
					}

					double score = genomeScoreMap.get(genomes[i]);
					StringBuilder genomeTaxaSb = new StringBuilder();
					genomeTaxaSb.append(genomes[i]).append("\t");

					genomeTaxaSb.append(FormatTool.getDF2().format(-Math.log10(score))).append("\t");

					if (psmCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(psmCountMap.get(genomes[i])).append("\t");
					} else {
						genomeTaxaSb.append("0").append("\t");
					}
					if (totalPepCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(totalPepCountMap.get(genomes[i]).size()).append("\t");
					} else {
						genomeTaxaSb.append("0").append("\t");
					}
					if (razorPepCountMap.containsKey(genomes[i])) {
						genomeTaxaSb.append(razorPepCountMap.get(genomes[i]).size());
					} else {
						genomeTaxaSb.append("0");
					}

					for (int j = 1; j < taxaContent.length; j++) {
						genomeTaxaSb.append("\t").append(taxaContent[j]);
					}

					for (int j = 0; j < genomeIntensity.length; j++) {
						genomeTaxaSb.append("\t").append(genomeIntensity[j]);
					}
					genomeWriter.println(genomeTaxaSb);

					for (int j = 0; j < taxRankMaps.length; j++) {
						String taxon = taxaContent[j + 3];
						if (taxon == null || taxon.length() == 0) {
							continue;
						}
						if (!taxRankMaps[j].containsKey(taxon)) {
							String[] lineage = new String[j + 1];
							System.arraycopy(taxaContent, 3, lineage, 0, lineage.length);
							taxRankMaps[j].put(taxon, lineage);
						}

						double[] taxIntensity;
						if (taxIntensityMaps[j].containsKey(taxon)) {
							taxIntensity = taxIntensityMaps[j].get(taxon);
						} else {
							taxIntensity = new double[genomeIntensity.length];
						}

						for (int k = 0; k < taxIntensity.length; k++) {
							taxIntensity[k] += genomeIntensity[k];
						}
						taxIntensityMaps[j].put(taxon, taxIntensity);

						if (taxScoreMaps[j].containsKey(taxon)) {
							taxScoreMaps[j].put(taxon, taxScoreMaps[j].get(taxon) -Math.log10(score));
						} else {
							taxScoreMaps[j].put(taxon, -Math.log10(score));
						}
					}
				}
			}
			genomeWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeFile, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeFile);
			return false;
		}

		File taxaFile = new File(taxFile, "Taxa.tsv");
		File taxaReportFile = new File(taxFile, "Taxa_report.tsv");
		PrintWriter taxaWriter = null;
		PrintWriter taxaReportWriter = null;
		try {
			taxaWriter = new PrintWriter(taxaFile);
			taxaReportWriter = new PrintWriter(taxaReportFile);

			StringBuilder taxaTitlesb1 = new StringBuilder();
			taxaTitlesb1.append("Name\t");
			taxaTitlesb1.append("Minus_log(p-value)\t");
			taxaTitlesb1.append("Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				taxaTitlesb1.append("\t").append(mainRanks[i].getName());
			}
			for (String exp : intensityTitle) {
				if (exp.startsWith("Intensity ")) {
					taxaTitlesb1.append("\t").append(exp);
				} else {
					taxaTitlesb1.append("\tIntensity ").append(exp);
				}
			}
			StringBuilder taxaTitlesb2 = new StringBuilder();
			taxaTitlesb2.append("Name\t");
			taxaTitlesb2.append("Rank");

			for (int i = 0; i < mainRanks.length; i++) {
				taxaTitlesb2.append("\t").append(mainRanks[i].getName());
			}
			for (String exp : intensityTitle) {
				if (exp.startsWith("Intensity ")) {
					taxaTitlesb2.append("\t").append(exp);
				} else {
					taxaTitlesb2.append("\tIntensity ").append(exp);
				}
			}
			taxaWriter.println(taxaTitlesb1);
			taxaReportWriter.println(taxaTitlesb2);

			for (int i = 0; i < taxRankMaps.length; i++) {
				for (String taxon : taxRankMaps[i].keySet()) {
					String[] taxaContent = taxRankMaps[i].get(taxon);
					double[] intensity = taxIntensityMaps[i].get(taxon);

					StringBuilder taxaSb = new StringBuilder();
					taxaSb.append(taxon).append("\t");
					
					double taxonScore = taxScoreMaps[i].get(taxon);
					taxaSb.append(FormatTool.getDF2().format(taxonScore)).append("\t");

					taxaSb.append(mainRanks[i].getName());

					for (int j = 0; j < mainRanks.length; j++) {
						if (j < taxaContent.length) {
							taxaSb.append("\t").append(taxaContent[j]);
						} else {
							taxaSb.append("\t");
						}
					}

					for (int j = 0; j < intensity.length; j++) {
						taxaSb.append("\t").append(intensity[j]);
					}

					taxaWriter.println(taxaSb);
				}
			}

			taxaWriter.close();

			for (int i = 0; i < taxRankMaps.length; i++) {
				for (String taxon : taxRankMaps[i].keySet()) {
					String[] taxaContent = taxRankMaps[i].get(taxon);
					double[] intensity = taxIntensityMaps[i].get(taxon);

					StringBuilder taxaSb = new StringBuilder();
					taxaSb.append(taxon).append("\t");
					taxaSb.append(mainRanks[i].getName());

					for (int j = 0; j < mainRanks.length; j++) {
						if (j < taxaContent.length) {
							taxaSb.append("\t").append(taxaContent[j]);
						} else {
							taxaSb.append("\t");
						}
					}
					for (int j = 0; j < intensity.length; j++) {
						taxaSb.append("\t").append(intensity[j]);
					}

					taxaReportWriter.println(taxaSb);
				}
			}

			taxaReportWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxa information to " + taxaFile, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing taxa information to " + taxaFile);
		}

		LOGGER.info(getTaskName() + ": taxonomy analysis finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis finished");

		setProgress(95);

		bar2.setString(getTaskName() + ": exporting report");

		LOGGER.info(getTaskName() + ": exporting report started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": exporting report started");

		File reportDir = new File(this.resultFolderFile, "report");
		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportCopyTask.copy(reportDir, getVersion());

		metaPar.setReportDir(reportDir);

		this.reportHtmlDir = new File(reportDir + "\\reports\\rmd_html");

		if (!reportHtmlDir.exists()) {
			reportHtmlDir.mkdirs();
		}
		
		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}

		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(taxRankMaps, taxIntensityMaps, cellularIntensity, expNames, jsOutput);
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

		this.summaryMetaFile = new File(this.resultFolderFile, "metainfo.tsv");
		try {
			metadata.exportMetadata(summaryMetaFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.exportSummary(task);

		if (!final_pep_txt.exists() || final_pep_txt.length() == 0) {
			return false;
		} else {
			File report_peptides_summary = new File(reportHtmlDir, "report_peptides_summary.html");
			if (!report_peptides_summary.exists() || report_peptides_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.peptide, final_pep_txt, report_peptides_summary);
				}
			}
		}

		if (!final_pro_txt.exists() || final_pro_txt.length() == 0) {
			return false;
		} else {
			File report_proteinGroups_summary = new File(reportHtmlDir, "report_proteinGroups_summary.html");
			if (!report_proteinGroups_summary.exists() || report_proteinGroups_summary.length() == 0) {
				if (summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.protein, final_pro_txt, report_proteinGroups_summary);
				}
			}
		}

		if (!taxaReportFile.exists() || taxaReportFile.length() == 0) {
			return false;
		} else {
			File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
			if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

				if (summaryMetaFile != null && summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary);
				}
			}
		}

		if (!funReportTsv.exists() || funReportTsv.length() == 0) {
			return false;
		} else {
			File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");
			if (!report_function_summary.exists() || report_function_summary.length() == 0) {

				if (summaryMetaFile != null && summaryMetaFile.exists()) {
					task.addTask(MetaReportTask.function, funReportTsv, report_function_summary, summaryMetaFile);
				} else {
					task.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
				}
			}
		}

		task.run();

		try {
			boolean finish = task.get();
			if (finish) {

				LOGGER.info(getTaskName() + ": report generation successes");
				System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": report generation successes");

				setProgress(100);
				return true;
			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in the report generation", e);
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": error in the report generation");
		}

		return true;
	}

	protected boolean exportTaxaFunc(MetaReportTask task) {

		LOGGER.info(getTaskName() + ": taxonomy analysis and functional annotation started");
		System.out.println(
				format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis and functional annotation started");

		HashMap<String, String> hostProNameMap = new HashMap<String, String>();
		if (metaPar.isAppendHostDb()) {
			String hostLine = null;
			try {
				BufferedReader hostDBufferedReader = new BufferedReader(new FileReader(this.metaPar.getHostDB()));
				while ((hostLine = hostDBufferedReader.readLine()) != null) {
					if (hostLine.startsWith(">")) {
						String[] cs = hostLine.split("[\\s+]");
						String name = cs[0].substring(1);
						if (cs.length > 1) {
							StringBuilder sb = new StringBuilder();
							for (int i = 1; i < cs.length; i++) {
								if (cs[i].equals("OS=Homo")) {
									break;
								} else {
									sb.append(cs[i]).append(" ");
								}
							}
							hostProNameMap.put(name, sb.substring(0, sb.length() - 1));
						}
					}
				}
				hostDBufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading host database from " + this.metaPar.getHostDB(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading host database from " + this.metaPar.getHostDB());
			}
		}

		if (this.pFindResultReader == null) {
			this.pFindResultReader = new PFindResultReader(resultFolderFile, spectraDirFile, ptmMap);
			try {
				pFindResultReader.read();
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading the database search result files");
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading the database search result files");
				return false;
			}
		}

		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.final_pep_txt));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		HashMap<String, ArrayList<Double>> genomeQValueMap = new HashMap<String, ArrayList<Double>>();
		try {

			String line = quanPepReader.readLine();
			String[] title = line.split("\t");
			int proId = -1;
			int qvalueId = -1;

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Proteins")) {
					proId = i;
				} else if (title[i].equals("Q-value")) {
					qvalueId = i;
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				double qvalue = Double.parseDouble(cs[qvalueId]);
				String[] pros = cs[proId].split(";");
				for (int i = 0; i < pros.length; i++) {
					if (pros[i].startsWith(magDb.getIdentifier())) {
						String genome = pros[i].split("_")[0];
						ArrayList<Double> qList;
						if (genomeQValueMap.containsKey(genome)) {
							qList = genomeQValueMap.get(genome);
						} else {
							qList = new ArrayList<Double>();
							genomeQValueMap.put(genome, qList);
						}
						qList.add(qvalue);
					}
				}
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading quantified peptides from " + this.quan_pep_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading quantified peptides from " + this.quan_pep_file.getName());
		}

		// functional annotation

		HashMap<String, Integer> proTaxIdMap = new HashMap<String, Integer>();

		LOGGER.info(getTaskName() + ": functional annotation started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation started");

		File funcFile = new File(metaPar.getDbSearchResultFile(), "functional_annotation");
		if (!funcFile.exists()) {
			funcFile.mkdir();
		}

		final_pro_xml_file = new File(funcFile, "functions.xml");

		HashMap<String, double[]> proIntensityMap = new HashMap<String, double[]>();
		HashMap<String, double[]> genomeIntensityMap = new HashMap<String, double[]>();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {

			BufferedReader quanProReader = null;
			try {
				quanProReader = new BufferedReader(new FileReader(this.final_pro_txt));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			try {

				String line = quanProReader.readLine();
				String[] title = line.split("\t");
				int proId = -1;

				ArrayList<String> tlist = new ArrayList<String>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].startsWith("Intensity ")) {
						tlist.add(title[i]);
					} else if (title[i].equals("Majority protein IDs")) {
						proId = i;
					}
				}

				String[] fileNames = tlist.toArray(new String[tlist.size()]);

				int[] fileIds = new int[fileNames.length];
				for (int i = 0; i < fileNames.length; i++) {
					for (int j = 0; j < title.length; j++) {
						if (fileNames[i].equals(title[j])) {
							fileIds[i] = j;
						}
					}
				}

				while ((line = quanProReader.readLine()) != null) {

					String[] cs = line.split("\t");
					double[] intensity = new double[fileIds.length];
					for (int i = 0; i < fileIds.length; i++) {
						intensity[i] = Double.parseDouble(cs[fileIds[i]]);
					}

					String[] pros = cs[proId].split(";");
					for (int i = 0; i < pros.length; i++) {
						proIntensityMap.put(pros[i], intensity);

						if (pros[i].startsWith(magDb.getIdentifier())) {
							String genome = pros[i].split("_")[0];
							double[] genomeIntensity;
							if (genomeIntensityMap.containsKey(genome)) {
								genomeIntensity = genomeIntensityMap.get(genome);
							} else {
								genomeIntensity = new double[intensity.length];
							}

							for (int j = 0; j < genomeIntensity.length; j++) {
								genomeIntensity[j] += intensity[j] / (double) pros.length;
							}
							genomeIntensityMap.put(genome, genomeIntensity);
						}
					}
				}
				quanProReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			LOGGER.info(getTaskName() + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {

			PFindProtein[] proteins = this.pFindResultReader.getProteins();
			HashMap<String, PFindProtein> promap = new HashMap<String, PFindProtein>();
			for (int i = 0; i < proteins.length; i++) {
				String pro = proteins[i].getName();
				promap.put(pro, proteins[i]);
			}

			BufferedReader quanProReader = null;
			try {
				quanProReader = new BufferedReader(new FileReader(this.final_pro_txt));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
			String[] fileNames = null;
			try {
				String line = quanProReader.readLine();
				String[] title = line.split("\t");
				int id = -1;
				int proId = -1;

				ArrayList<String> tlist = new ArrayList<String>();
				for (int i = 0; i < title.length; i++) {
					if (title[i].startsWith("Intensity ")) {
						tlist.add(title[i]);
					} else if (title[i].equals("id")) {
						id = i;
					} else if (title[i].equals("Majority protein IDs")) {
						proId = i;
					}
				}

				fileNames = tlist.toArray(new String[tlist.size()]);

				int[] fileIds = new int[fileNames.length];
				for (int i = 0; i < fileNames.length; i++) {
					for (int j = 0; j < title.length; j++) {
						if (fileNames[i].equals(title[j])) {
							fileIds[i] = j;
						}
					}
				}

				while ((line = quanProReader.readLine()) != null) {

					String[] cs = line.split("\t");
					String[] pros = cs[proId].split(";");

					if (promap.containsKey(pros[0]) && pros[0].startsWith(magDb.getIdentifier())) {

						int groupId = Integer.parseInt(cs[id]);
						PFindProtein protein = promap.get(pros[0]);

						double[] intensity = new double[fileIds.length];
						for (int i = 0; i < fileIds.length; i++) {
							intensity[i] = Double.parseDouble(cs[fileIds[i]]);
						}

						for (int i = 0; i < pros.length; i++) {

							MetaProtein mp = new MetaProtein(groupId, i + 1, pros[i], protein.getPepCount(),
									protein.getSpCount(), protein.getScore(), intensity);
							list.add(mp);

							if (pros[i].startsWith(magDb.getIdentifier())) {
								String genome = pros[i].split("_")[0];
								double[] genomeIntensity;
								if (genomeIntensityMap.containsKey(genome)) {
									genomeIntensity = genomeIntensityMap.get(genome);
								} else {
									genomeIntensity = new double[intensity.length];
								}

								for (int j = 0; j < genomeIntensity.length; j++) {
									genomeIntensity[j] += intensity[j] / (double) pros.length;
								}
								genomeIntensityMap.put(genome, genomeIntensity);
							}
						}
					}
				}
				quanProReader.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in reading quantified proteins from " + this.final_pro_txt.getName(),
						e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in reading quantified proteins from " + this.final_pro_txt.getName());
			}

			setProgress(88);

			MetaProtein[] metapros = list.toArray(new MetaProtein[list.size()]);
			MetaProteinAnnoMag[] mpas = funcAnnoSqlite(metapros, fileNames);
			for (int i = 0; i < mpas.length; i++) {
				String name = mpas[i].getPro().getName();
				if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
					proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
				} else {
					proTaxIdMap.put(name, 131567);
				}
			}
		}

		File funcResultFile = final_pro_xml_file.getParentFile();
		File funTsv = new File(funcResultFile, "functions.tsv");
		File funReportTsv = new File(funcResultFile, "functions_report.tsv");

		MetaProteinXMLReader2 funReader = null;
		try {
			funReader = new MetaProteinXMLReader2(final_pro_xml_file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in reading protein function information from "
					+ this.final_pro_xml_file.getName(), e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in reading protein function information from " + this.quan_pro_file.getName());
		}

		if (proTaxIdMap.size() == 0) {
			MetaProteinAnnoEggNog[] mpas = funReader.getProteins();
			for (int i = 0; i < mpas.length; i++) {
				String name = mpas[i].getPro().getName();
				if (NumberUtils.isCreatable(mpas[i].getTax_id())) {
					proTaxIdMap.put(name, Integer.parseInt(mpas[i].getTax_id()));
				} else {
					proTaxIdMap.put(name, 131567);
				}
			}
		}

		if (!funTsv.exists() || !funReportTsv.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " started");
			try {
				funReader.exportTsv(funTsv);
				funReader.exportTsvReport(funReportTsv);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + funTsv.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + funTsv.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + funTsv.getName()
					+ " finished");
		}

		File html = new File(funcResultFile, "functions.html");
		if (!html.exists()) {
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " started");
			try {
				funReader.exportHtml(html);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in writing protein function information to " + html.getName(), e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in writing protein function information to " + html.getName());
			}
			System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": writing results to " + html.getName()
					+ " finished");
		}

		File report_function_summary = new File(this.reportHtmlDir, "report_function_summary.html");
		if (!report_function_summary.exists() || report_function_summary.length() == 0) {

			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary, summaryMetaFile);
			} else {
				task.addTask(MetaReportTask.function, funReportTsv, report_function_summary);
			}
		}

		LOGGER.info(getTaskName() + ": functional annotation finished");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": functional annotation finished");

		setProgress(92);

		// taxonomy analysis

		LOGGER.info(getTaskName() + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis started");

		File taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		HashMap<String, Integer> psmCountMap = new HashMap<String, Integer>();
		HashMap<String, HashSet<String>> pepCountMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> uniquePepCountMap = new HashMap<String, HashSet<String>>();

		PFindPSM[] psms = pFindResultReader.getPsms();
		HashMap<String, HashSet<String>> genomeProMap = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < psms.length; i++) {

			String baseSequence = psms[i].getSequence();
			String[] pros = psms[i].getProteins();

			for (int j = 0; j < pros.length; j++) {

				if (psmCountMap.containsKey(pros[j])) {
					psmCountMap.put(pros[j], psmCountMap.get(pros[j]) + 1);

					pepCountMap.get(pros[j]).add(baseSequence);
					if (pros.length == 1) {
						uniquePepCountMap.get(pros[j]).add(baseSequence);
					}
				} else {
					psmCountMap.put(pros[j], 1);

					HashSet<String> pepSet = new HashSet<String>();
					pepSet.add(baseSequence);
					pepCountMap.put(pros[j], pepSet);

					HashSet<String> unipepSet = new HashSet<String>();
					if (pros.length == 1) {
						unipepSet.add(baseSequence);
					}
					uniquePepCountMap.put(pros[j], unipepSet);
				}

				if (pros[j].startsWith(magDb.getIdentifier())) {
					String genome = pros[j].split("_")[0];

					if (psmCountMap.containsKey(genome)) {
						psmCountMap.put(genome, psmCountMap.get(genome) + 1);

						pepCountMap.get(genome).add(baseSequence);
						if (pros.length == 1) {
							uniquePepCountMap.get(genome).add(baseSequence);
						}
					} else {
						psmCountMap.put(genome, 1);

						HashSet<String> pepSet = new HashSet<String>();
						pepSet.add(baseSequence);
						pepCountMap.put(genome, pepSet);

						HashSet<String> unipepSet = new HashSet<String>();
						if (pros.length == 1) {
							unipepSet.add(baseSequence);
						}
						uniquePepCountMap.put(genome, unipepSet);
					}

					if (genomeProMap.containsKey(genome)) {
						genomeProMap.get(genome).add(pros[j]);
					} else {
						HashSet<String> proSet = new HashSet<String>();
						proSet.add(pros[j]);
						genomeProMap.put(genome, proSet);
					}
				}
			}
		}

		return exportGenome(genomeIntensityMap, psmCountMap, pepCountMap, uniquePepCountMap, genomeQValueMap, taxFile,
				expNames, task);
	}

	@SuppressWarnings("unchecked")
	protected boolean exportGenome(HashMap<String, double[]> genomeIntensityMap, HashMap<String, Integer> psmCountMap,
			HashMap<String, HashSet<String>> pepCountMap, HashMap<String, HashSet<String>> uniquePepCountMap,
			HashMap<String, ArrayList<Double>> genomeQvalueMap, File taxFile, String[] expNames, MetaReportTask task) {

		MagFuncSearcher searcher = null;
		try {
			searcher = new MagFuncSearcher(metaPar.getMicroDb(), msv.getFuncDef());

		} catch (NumberFormatException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": error in genome analysis, task failed", e);
			System.err.println(format.format(new Date()) + "\t" + getTaskName() + ": error in genome analysis, task failed");

			return false;
		}

		String[] genomes = genomeIntensityMap.keySet().toArray(new String[genomeIntensityMap.size()]);
		HashMap<String, String[]> genomeTaxaMap = null;
		try {
			genomeTaxaMap = searcher.matchGenome(genomes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(getTaskName() + ": Error in searching the genome2taxa database", e);
			System.err.println(
					format.format(new Date()) + "\t" + getTaskName() + ": error in searching the genome2taxa database");
			return false;
		}

		File genomeHtmlFile = new File(taxFile, "Genome.html");
		try {
			HMGenomeHtmlWriter htmlWriter = new HMGenomeHtmlWriter(genomeHtmlFile);
			htmlWriter.write(genomeTaxaMap);
			htmlWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeHtmlFile, e);
			System.err.println(
					format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeHtmlFile);
			return false;
		}

		File genomeFile = new File(taxFile, "Genome.tsv");
		PrintWriter genomeWriter = null;
		try {
			genomeWriter = new PrintWriter(genomeFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing genome information to " + genomeFile, e);
			System.err
					.println(format.format(new Date()) + "\t" + "Error in writing genome information to " + genomeFile);
			return false;
		}

		StringBuilder genomeTitlesb = new StringBuilder();
		genomeTitlesb.append("Genome\t");
		genomeTitlesb.append("Score\t");
		genomeTitlesb.append("PSM count\t");
		genomeTitlesb.append("Peptide count\t");
		genomeTitlesb.append("Unique peptide count\t");
		genomeTitlesb.append("GTDB_Name\t");
		genomeTitlesb.append("GTDB_Rank\t");
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();
		for (int i = 0; i < mainRanks.length; i++) {
			genomeTitlesb.append("GTDB_").append(mainRanks[i].getName()).append("\t");
		}
		
		genomeTitlesb.append("NCBI_Name\t");
		genomeTitlesb.append("NCBI_Rank");

		for (int i = 0; i < mainRanks.length; i++) {
			genomeTitlesb.append("\t").append("NCBI_").append(mainRanks[i].getName());
		}
		
		for (String exp : expNames) {
			if (exp.startsWith("Intensity ")) {
				genomeTitlesb.append("\t").append(exp);
			} else {
				genomeTitlesb.append("\tIntensity ").append(exp);
			}
		}

		genomeWriter.println(genomeTitlesb);
		
		setProgress(94);

		double[] cellularIntensity = new double[expNames.length];
		HashMap<String, String[]>[] taxRankMaps = new HashMap[mainRanks.length];
		HashMap<String, double[]>[] taxIntensityMaps = new HashMap[mainRanks.length];
		HashMap<String, Double>[] taxScoreMaps = new HashMap[mainRanks.length];
		for (int i = 0; i < taxRankMaps.length; i++) {
			taxRankMaps[i] = new HashMap<String, String[]>();
			taxIntensityMaps[i] = new HashMap<String, double[]>();
			taxScoreMaps[i] = new HashMap<String, Double>();
		}
		
		Arrays.sort(genomes, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				return genomeQvalueMap.get(o2).size() - genomeQvalueMap.get(o1).size();
			}
		});
		
		double[] logGenomePepCount = new double[genomes.length];
		for (int i = 0; i < genomes.length; i++) {
			logGenomePepCount[i] = Math.log(genomeQvalueMap.get(genomes[i]).size());
		}
		double[] genomeScores = new double[genomes.length];
		double mean = MathTool.calculateArithmeticMean(logGenomePepCount);
		if (mean > 0) {
			TTest tTest = new TTest();
			double add = -Math.log10(tTest.tTest(logGenomePepCount[0], logGenomePepCount));
			genomeScores[0] = add * 2;
			for (int i = 1; i < logGenomePepCount.length; i++) {
				if (logGenomePepCount[i] > 0) {
					double pValue = tTest.tTest(logGenomePepCount[i], logGenomePepCount);
					genomeScores[i] = -Math.log10(pValue);
					if (logGenomePepCount[i] > mean) {
						genomeScores[i] = add + genomeScores[i];
					} else {
						genomeScores[i] = add - genomeScores[i];
					}

					if (genomeScores[i] < 0) {
						genomeScores[i] = 0;
					}
				}
			}
		}
		
		for (int i = 0; i < genomes.length; i++) {
			String[] taxaContent = genomeTaxaMap.get(genomes[i]);
			if (genomeIntensityMap.containsKey(genomes[i])) {
				double[] genomeIntensity = genomeIntensityMap.get(genomes[i]);
				for (int j = 0; j < genomeIntensity.length; j++) {
					cellularIntensity[j] += genomeIntensity[j];
				}

				StringBuilder genomeTaxaSb = new StringBuilder();
				genomeTaxaSb.append(genomes[i]).append("\t");

				genomeTaxaSb.append(FormatTool.getDF2().format(genomeScores[i])).append("\t");

				if (psmCountMap.containsKey(genomes[i])) {
					genomeTaxaSb.append(psmCountMap.get(genomes[i])).append("\t");
				} else {
					genomeTaxaSb.append("0").append("\t");
				}
				if (pepCountMap.containsKey(genomes[i])) {
					genomeTaxaSb.append(pepCountMap.get(genomes[i]).size()).append("\t");
				} else {
					genomeTaxaSb.append("0").append("\t");
				}
				if (uniquePepCountMap.containsKey(genomes[i])) {
					genomeTaxaSb.append(uniquePepCountMap.get(genomes[i]).size());
				} else {
					genomeTaxaSb.append("0");
				}

				for (int j = 1; j < taxaContent.length; j++) {
					genomeTaxaSb.append("\t").append(taxaContent[j]);
				}

				for (int j = 0; j < genomeIntensity.length; j++) {
					genomeTaxaSb.append("\t").append(genomeIntensity[j]);
				}
				genomeWriter.println(genomeTaxaSb);

				for (int j = 0; j < taxRankMaps.length; j++) {
					String taxon = taxaContent[j + 3];
					if (taxon == null || taxon.length() == 0) {
						continue;
					}
					if (!taxRankMaps[j].containsKey(taxon)) {
						String[] lineage = new String[j + 1];
						System.arraycopy(taxaContent, 3, lineage, 0, lineage.length);
						taxRankMaps[j].put(taxon, lineage);
					}

					double[] taxIntensity;
					if (taxIntensityMaps[j].containsKey(taxon)) {
						taxIntensity = taxIntensityMaps[j].get(taxon);
					} else {
						taxIntensity = new double[genomeIntensity.length];
					}

					for (int k = 0; k < taxIntensity.length; k++) {
						taxIntensity[k] += genomeIntensity[k];
					}
					taxIntensityMaps[j].put(taxon, taxIntensity);

					if (taxScoreMaps[j].containsKey(taxon)) {
						taxScoreMaps[j].put(taxon, taxScoreMaps[j].get(taxon) + genomeScores[i]);
					} else {
						taxScoreMaps[j].put(taxon, genomeScores[i]);
					}
				}
			}
		}
		genomeWriter.close();

		File taxaFile = new File(taxFile, "Taxa.tsv");
		File taxaReportFile = new File(taxFile, "Taxa_report.tsv");
		PrintWriter taxaWriter = null;
		PrintWriter taxaReportWriter = null;
		try {
			taxaWriter = new PrintWriter(taxaFile);
			taxaReportWriter = new PrintWriter(taxaReportFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxa information to " + taxaFile, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing taxa information to " + taxaFile);
		}

		StringBuilder taxaTitlesb = new StringBuilder();
		taxaTitlesb.append("Name\t");
		taxaTitlesb.append("Rank");

		for (int i = 0; i < mainRanks.length; i++) {
			taxaTitlesb.append("\t").append(mainRanks[i].getName());
		}
		
		taxaTitlesb.append("\tScore");

		for (String exp : expNames) {
			if (exp.startsWith("Intensity ")) {
				taxaTitlesb.append("\t").append(exp);
			} else {
				taxaTitlesb.append("\tIntensity ").append(exp);
			}
		}
		taxaWriter.println(taxaTitlesb);
		taxaReportWriter.println(taxaTitlesb);
		
		double maxTaxonScore = 0;
		for (int i = 0; i < taxRankMaps.length; i++) {
			for (String taxon : taxRankMaps[i].keySet()) {
				String[] taxaContent = taxRankMaps[i].get(taxon);
				double[] intensity = taxIntensityMaps[i].get(taxon);

				StringBuilder taxaSb = new StringBuilder();
				taxaSb.append(taxon).append("\t");
				taxaSb.append(mainRanks[i].getName());

				for (int j = 0; j < mainRanks.length; j++) {
					if (j < taxaContent.length) {
						taxaSb.append("\t").append(taxaContent[j]);
					} else {
						taxaSb.append("\t");
					}
				}

				double taxonScore = taxScoreMaps[i].get(taxon);
				taxaSb.append("\t").append(FormatTool.getDF2().format(taxonScore));

				for (int j = 0; j < intensity.length; j++) {
					taxaSb.append("\t").append(intensity[j]);
				}

				taxaWriter.println(taxaSb);

				if (taxonScore > maxTaxonScore) {
					maxTaxonScore = taxonScore;
				}
			}
		}

		taxaWriter.close();
		
		for (int i = 0; i < taxRankMaps.length; i++) {
			for (String taxon : taxRankMaps[i].keySet()) {
				double taxonScore = taxScoreMaps[i].get(taxon);
				if (taxonScore < maxTaxonScore * 0.005) {
					continue;
				}
				String[] taxaContent = taxRankMaps[i].get(taxon);
				double[] intensity = taxIntensityMaps[i].get(taxon);

				StringBuilder taxaSb = new StringBuilder();
				taxaSb.append(taxon).append("\t");
				taxaSb.append(mainRanks[i].getName());

				for (int j = 0; j < mainRanks.length; j++) {
					if (j < taxaContent.length) {
						taxaSb.append("\t").append(taxaContent[j]);
					} else {
						taxaSb.append("\t");
					}
				}

				taxaSb.append("\t").append(FormatTool.getDF2().format(taxonScore));

				for (int j = 0; j < intensity.length; j++) {
					taxaSb.append("\t").append(intensity[j]);
				}

				taxaReportWriter.println(taxaSb);
			}
		}

		taxaReportWriter.close();

		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}

		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(taxRankMaps, taxIntensityMaps, cellularIntensity, expNames, jsOutput);
		}

		boolean finished = false;

		File report_taxonomy_summary = new File(this.reportHtmlDir, "report_taxonomy_summary.html");
		if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {

			if (summaryMetaFile != null && summaryMetaFile.exists()) {
				finished = task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary,
						summaryMetaFile);
			} else {
				finished = task.addTask(MetaReportTask.taxon, taxaReportFile, report_taxonomy_summary);
			}
		} else {
			finished = true;
		}
		
		LOGGER.info(getTaskName() + ": taxonomy analysis started");
		System.out.println(format.format(new Date()) + "\t" + getTaskName() + ": taxonomy analysis started");
		
		setProgress(96);

		return finished;
	}

	protected MetaProteinAnnoMag[] funcAnnoSqlite(MetaProtein[] pros, String[] fileNames) {
		HashMap<String, ArrayList<MetaProtein>> genomeProMap = new HashMap<String, ArrayList<MetaProtein>>();
		for (int i = 0; i < pros.length; i++) {
			String name = pros[i].getName();
			if (name.startsWith(magDb.getIdentifier())) {
				String genome = name.split("_")[0];
				if (genome.endsWith(".1")) {
					genome = genome.substring(0, genome.length() - 2);
				}

				if (genomeProMap.containsKey(genome)) {
					genomeProMap.get(genome).add(pros[i]);
				} else {
					ArrayList<MetaProtein> list = new ArrayList<MetaProtein>();
					list.add(pros[i]);
					genomeProMap.put(genome, list);
				}
			}
		}

		HashMap<String, String[]> usedCogMap = new HashMap<String, String[]>();
		HashMap<String, String[]> usedNogMap = new HashMap<String, String[]>();
		HashMap<String, String> usedKeggMap = new HashMap<String, String>();
		HashMap<String, GoObo> usedGoMap = new HashMap<String, GoObo>();
		HashMap<String, EnzymeCommission> usedEcMap = new HashMap<String, EnzymeCommission>();

		ArrayList<MetaProteinAnnoMag> list = new ArrayList<MetaProteinAnnoMag>();
		for (String genome : genomeProMap.keySet()) {

			ArrayList<MetaProtein> mplist = genomeProMap.get(genome);
			MetaProtein[] mps = mplist.toArray(new MetaProtein[mplist.size()]);

			MagFuncSearcher searcher = null;
			try {
				searcher = new MagFuncSearcher(metaPar.getMicroDb(), msv.getFuncDef(), genome);

			} catch (NumberFormatException | SQLException | IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in proteins functional annotations, task failed", e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in proteins functional annotations, task failed");

				return null;
			}

			MetaProteinAnnoMag[] mpas = null;
			try {
				mpas = searcher.match(mps);
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				LOGGER.error(getTaskName() + ": error in proteins functional annotations, task failed", e);
				System.err.println(format.format(new Date()) + "\t" + getTaskName()
						+ ": error in proteins functional annotations, task failed");

				return null;
			}

			if (mpas != null) {
				for (int i = 0; i < mpas.length; i++) {
					list.add(mpas[i]);
				}

				usedCogMap.putAll(searcher.getUsedCogMap());
				usedNogMap.putAll(searcher.getUsedNogMap());
				usedKeggMap.putAll(searcher.getUsedKeggMap());
				usedEcMap.putAll(searcher.getUsedEcMap());
				usedGoMap.putAll(searcher.getUsedGoMap());
			}
		}

		MetaProteinAnnoMag[] metapros = list.toArray(new MetaProteinAnnoMag[list.size()]);

		MetaProteinXMLWriter2 writer = new MetaProteinXMLWriter2(final_pro_xml_file.getAbsolutePath(),
				MetaConstants.pFindMag, ((MetaParameterMag) this.metaPar).getQuanMode(), fileNames);

		writer.addProteins(metapros, usedCogMap, usedNogMap, usedKeggMap, usedGoMap, usedEcMap);

		writer.close();

		if (final_pro_xml_file.exists() && final_pro_xml_file.length() > 0) {
			LOGGER.info(getTaskName() + ": proteins with functional annotations have been exported to "
					+ final_pro_xml_file.getName());
			System.out.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": proteins with functional annotations have been exported to " + final_pro_xml_file.getName());
		} else {
			LOGGER.error(getTaskName() + ": error in proteins functional annotations, task failed");
			System.err.println(format.format(new Date()) + "\t" + getTaskName()
					+ ": error in proteins functional annotations, task failed");
		}

		return metapros;
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}
	
	protected static void dbReduceTest(String in, String out) {
		HashMap<String, HashSet<String>> genomePepMap = new HashMap<String, HashSet<String>>();
		HashSet<String> pepSet = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				String[] pros = cs[12].split("/");
				for (int j = 0; j < pros.length; j++) {
					if (pros[j].startsWith("MGYG")) {
						String genome = pros[j].split("_")[0];
						if (genomePepMap.containsKey(genome)) {
							genomePepMap.get(genome).add(cs[0]);
						} else {
							HashSet<String> set = new HashSet<String>();
							set.add(cs[0]);
							genomePepMap.put(genome, set);
						}
					}
				}
				pepSet.add(cs[0]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double threshold = 0.9;
		
		String[] genomes = genomePepMap.keySet().toArray(new String[genomePepMap.size()]);
		Arrays.sort(genomes, (g1, g2) -> {
			int s1 = genomePepMap.get(g1).size();
			int s2 = genomePepMap.get(g2).size();
			return s2 - s1;
		});

		for (int iteratorCount = 0; iteratorCount < 5; iteratorCount++) {
			HashSet<String> totalSet = new HashSet<String>();
			HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
			for (int i = 0; i < genomes.length; i++) {
				int currentSize = totalSet.size();
				totalSet.addAll(genomePepMap.get(genomes[i]));
				int increaseSize = totalSet.size() - currentSize;
				orderMap.put(genomes[i], increaseSize);
			}

			Arrays.sort(genomes, (g1, g2) -> {
				return orderMap.get(g2) - orderMap.get(g1);
			});
		}

		boolean finish = false;
		HashSet<String> genomeSet = new HashSet<String>();
		HashSet<String> totalSet = new HashSet<String>();
		try (PrintWriter writer = new PrintWriter(out)) {
			writer.println("Rank\tGenome\tTotal_covered_PSM_count\tPercentage");
			for (int i = 0; i < genomes.length; i++) {
				totalSet.addAll(genomePepMap.get(genomes[i]));
				double percentage = ((double) totalSet.size() / (double) pepSet.size());
				writer.println(i + "\t" + genomes[i] + "\t" + totalSet.size() + "\t" + percentage);

				if (!finish) {
					genomeSet.add(genomes[i]);

					if (percentage >= threshold) {
						if (percentage == 1.0) {
							finish = true;
						} else {
							if (genomeSet.size() > 200) {
								finish = true;
							} else {
								threshold += 0.01;
							}
						}
					}
				}
			}
			writer.close();
		} catch (IOException e) {

		}
		
		System.out.println(genomeSet.size());
	}

	public static void main(String[] args) {

		MetaParameterMag parameterMag = MetaParaIOMag
				.parse("Z:\\Kai\\Raw_files\\PXD008738\\12mix\\MetaLab_dda\\parameter.json");
		MetaSourcesMag metaSourcesMag = MetaSourcesIoMag
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\resources_MAG_1_1.json");
		MagDbItem[][] magDbItems = MagDbConfigIO
				.parse("C:\\Users\\kchen2\\metalab\\metalab\\target\\classes\\MAGDB_1_1.json");

		for (int i = 0; i < magDbItems[0].length; i++) {
			if (magDbItems[0][i].getCatalogueID().equals("human-gut")) {
				parameterMag.setUsedMagDbItem(magDbItems[0][i]);
			}
		}

		MagHapPFindTask task = new MagHapPFindTask(parameterMag, metaSourcesMag, new JProgressBar(), new JProgressBar(),
				null);

		ArrayList<String> fileList = new ArrayList<String>();
		fileList.add("170412_12mix_DDA_library_stock_2_2");
		fileList.add("170412_12mix_DDA_library_stock_2_3");
		fileList.add("170412_12mix_DDA_library_stock_2_4");
		
		FlashLfqQuanPepReader quanPepReader = new FlashLfqQuanPepReader(
				"Z:\\Kai\\Raw_files\\PXD008738\\12mix\\MetaLab_dda\\mag_result\\QuantifiedPeptides.tsv", fileList);

		FlashLfqQuanPeptide[] quanPeps = quanPepReader.getMetaPeptides();

		HashMap<String, Double> genomeScoreMap = task.refineGenomes(quanPeps);
		
		task.refineProteins(quanPeps, genomeScoreMap);
	}

}
