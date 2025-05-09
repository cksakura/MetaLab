package bmi.med.uOttawa.metalab.dbSearch.dbReducer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindTask;

/**
 * Using DBReducer to get a refined database before search
 * 
 * @see https://github.com/Wang-kaifei/DBReducer
 * @author Kai Cheng
 * @version 2025.03.20
 */
public class DBReducerTask extends PFindTask {

	protected static String taskName = "DBReducer task";
	protected static Logger LOGGER = LogManager.getLogger(DBReducerTask.class);
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File dbreducerFile;
	private String dbreducerExe = "DBReducer.exe";

	public DBReducerTask(File dbreducerFile) {
		super(dbreducerFile);
		// TODO Auto-generated constructor stub
		this.dbreducerFile = dbreducerFile.getParentFile();
	}

	public void searchMgf(String[] raws, String result, String db, String ms2Type, double psmFdr) {

		File rawFile = new File(raws[0]);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".DBReducer.bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {

			writer.println(dbreducerFile.getAbsolutePath().split("\\\\")[0]);
			writer.println("cd " + dbreducerFile.getAbsolutePath());

			File dbReducerCfg = new File(dbreducerFile.getAbsolutePath() + ".cfg");
			if (!dbReducerCfg.exists()) {
				LOGGER.error(taskName + ": DBReducer parameter file DBReducer.cfg was not found in "
						+ dbreducerFile.getParentFile().getParent());
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": DBReducer parameter file DBReducer.cfg was not found in "
						+ dbreducerFile.getParentFile().getParent());

				return;
			}
			DBReducerConfig config = new DBReducerConfig(dbReducerCfg);
			int multipCount = totalThread > raws.length ? raws.length : totalThread;
			int threadCount = totalThread > raws.length ? totalThread / raws.length : 1;

			config.setCoreThread(multipCount, threadCount);

			String[] fixedMods = new String[] { "Carbamidomethyl[C] 0" };
			String[] variMods = new String[] { "Oxidation[M] 0", "Acetyl[ProteinN-term] 0" };

			File conf = config.searchMgf(raws, result, db, ms2Type, psmFdr, fixedMods, variMods);

			if (conf == null) {
				LOGGER.error(taskName + ": error in writing a cfg file to " + conf);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing a cfg file to " + conf);
				return;
			}

			writer.println(dbreducerExe + " " + conf);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		this.pfindTaskMap.put(batFile.getAbsolutePath(), false);
	}

	public void searchMgf(String[] raws, String result, String db, String ms2Type, double psmFdr, String[] fixedMods,
			String[] variMods) {

		File rawFile = new File(raws[0]);
		String name = rawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(result, name + ".DBReducer.bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(dbreducerFile.getAbsolutePath().split("\\\\")[0]);
			writer.println("cd " + dbreducerFile.getAbsolutePath());

			File dbReducerCfg = new File(dbreducerFile.getAbsolutePath() + ".cfg");
			if (!dbReducerCfg.exists()) {
				LOGGER.error(taskName + ": DBReducer parameter file DBReducer.cfg was not found in "
						+ dbreducerFile.getParentFile().getParent());
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": DBReducer parameter file DBReducer.cfg was not found in "
						+ dbreducerFile.getParentFile().getParent());

				return;
			}
			DBReducerConfig config = new DBReducerConfig(dbReducerCfg);
			int multipCount = totalThread > raws.length ? raws.length : totalThread;
			int threadCount = totalThread > raws.length ? totalThread / raws.length : 1;

			config.setCoreThread(multipCount, threadCount);

			File conf = config.searchMgf(raws, result, db, ms2Type, psmFdr, fixedMods, variMods);

			if (conf == null) {
				LOGGER.error(taskName + ": error in writing a cfg file to " + conf);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in writing a cfg file to " + conf);
				return;
			}

			writer.println(dbreducerExe + " " + conf);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing a bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing a bat file to " + batFile);
		}

		this.pfindTaskMap.put(batFile.getAbsolutePath(), false);
	}
}
