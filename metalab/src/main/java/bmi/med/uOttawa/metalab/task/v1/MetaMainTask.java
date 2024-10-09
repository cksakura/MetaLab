/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import bmi.med.uOttawa.metalab.core.tools.FileHandler;
import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.task.v1.par.JsonParaHandler;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MetaMainTask extends SwingWorker<Void, Void> {

	private MetaParameterV1 parameter;
	private JProgressBar progressBar1;
	private JProgressBar progressBar2;
	
	private boolean finished = false;
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	
	public MetaMainTask(MetaParameterV1 parameter) {
		this.parameter = parameter;
	}

	public MetaMainTask(MetaParameterV1 parameter, JProgressBar progressBar1, JProgressBar progressBar2) {
		this.parameter = parameter;
		this.progressBar1 = progressBar1;
		this.progressBar2 = progressBar2;
	}

	@Override
	protected Void doInBackground() {
		// TODO Auto-generated method stub

		boolean[] steps = parameter.getWorkflow();
		int total = 0;
		for (int i = 0; i < steps.length; i++) {
			if (steps[i]) {
				total++;
			}
		}

		if (total == 0)
			return null;

		int range = (int) ((double) 1.0 / (double) total * 100);
		int begin = 0;

		/**
		 * pre-processing
		 */
		if (steps[0]) {
			int[] progress = new int[] { begin, begin + range };
			System.out.println(MetaDbCreateTaskV1.taskName + ": started");

			MetaDbCreateTaskV1 task = new MetaDbCreateTaskV1(parameter, progress, progressBar1, progressBar2);
			task.doInBackground();

			begin += range;

			if (!task.isFinished()) {
				return null;
			}

			System.out.println(MetaDbCreateTaskV1.taskName + ": finished");

			int dbProCount;
			try {
				dbProCount = task.getDBProteinCount();
				if (dbProCount == -1) {
					System.out.println("Sample specific database is not generated properly, task failed.");
					return null;
				} else if (dbProCount == 0) {
					System.out.println("No protein sequence is found in the sample specific database, task failed.");
					return null;
				} else {
					System.out.println(dbProCount + " protein sequence are included in the sample specific database.");
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * peptide identification
		 */
		if (steps[1]) {

			int[] progress = new int[] { begin, begin + range };
			System.out.println(MetaPepIdenTask.taskName + ": started");

			MetaPepIdenTask task = new MetaPepIdenTask(parameter, progress, progressBar1, progressBar2);
			task.doInBackground();

			begin += range;

			if (!task.isFinished()) {
				return null;
			}

		}

		/**
		 * taxonomy analysis
		 */
		if (steps[2]) {
			int[] progress = new int[] { begin, begin + range };
			System.out.println(MetaLCATask.taskName + ": started");

			File[] results = parameter.getPepIdenResults();
			if (results == null) {
				String[] fileNames = parameter.getRawFiles()[0];
				for (int i = 0; i < fileNames.length; i++) {
					if (fileNames[i].endsWith("txt")) {
						parameter.setPepIdenResults(new File[] { new File(fileNames[i]) });
						parameter.setPepIdenResultType(MetaSearchEngine.maxquant.getId());

						MetaLCATask task = new MetaLCATask(parameter, progress, progressBar1, progressBar2);
						task.doInBackground();

						if (!task.isFinished()) {
							return null;
						}

					} else if (fileNames[i].endsWith("xml")) {
						parameter.setPepIdenResults(new File[] { new File(fileNames[i]) });
						parameter.setPepIdenResultType(MetaSearchEngine.xtandem.getId());

						MetaLCATask task = new MetaLCATask(parameter, progress, progressBar1, progressBar2);
						task.doInBackground();

						if (!task.isFinished()) {
							return null;
						}
					}
				}
			} else {
				MetaLCATask task = new MetaLCATask(parameter, progress, progressBar1, progressBar2);
				task.doInBackground();

				if (!task.isFinished()) {
					return null;
				}
			}

			System.out.println(MetaLCATask.taskName + ": finished");
		}

		/**
		 * functional analysis
		 */
		if (steps[3]) {

			int[] progress = new int[] { begin, begin + range };
			System.out.println(MetaFunctionTask.taskName + ": started");

			File results = parameter.getProteinIdenResult();
			if (results == null) {
				String[] fileNames = parameter.getRawFiles()[0];
				for (int i = 0; i < fileNames.length; i++) {
					if (fileNames[i].endsWith("txt")) {
						parameter.setProteinIdenResult(new File(fileNames[i]));
						MetaFunctionTask task = new MetaFunctionTask(parameter, progress, progressBar1, progressBar2);
						task.doInBackground();

						if (!task.isFinished()) {
							return null;
						}
					}
				}
			} else {
				MetaFunctionTask task = new MetaFunctionTask(parameter, progress, progressBar1, progressBar2);
				task.doInBackground();

				if (!task.isFinished()) {
					return null;
				}
			}
			begin += range;

			System.out.println(MetaFunctionTask.taskName + ": finished");
		}

		finished = true;

		return null;
	}
	
	public void taskDone() {
		try {

			if (finished) {
				HashMap<Date, String> map = new HashMap<Date, String>();
				File[] files = parameter.getParameterDir().listFiles();

				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().endsWith("log")) {

						BufferedReader reader = new BufferedReader(new FileReader(files[i]));
						String line = null;
						while ((line = reader.readLine()) != null) {
							String[] cs = line.split("\t");
							if (cs.length == 2) {
								ParsePosition pp = new ParsePosition(0);
								Date date = format.parse(cs[0], pp);
								map.put(date, cs[1]);
							}
						}
						reader.close();

						boolean isFileUnlocked = false;
						try {
							org.apache.commons.io.FileUtils.touch(files[i]);
							isFileUnlocked = true;
						} catch (IOException e) {
							isFileUnlocked = false;
						}

						if (isFileUnlocked) {
							FileHandler.deleteFileOrFolder(files[i]);
						}
					}
				}

				Date[] dates = map.keySet().toArray(new Date[map.size()]);
				Arrays.sort(dates);

				PrintWriter logWriter = new PrintWriter(new File(parameter.getParameterDir(), "log.txt"));
				logWriter.println(format.format(dates[0]) + "\t" + "Task created");

				for (Date date : dates) {
					logWriter.println(format.format(date) + "\t" + map.get(date));
				}

				logWriter.println(format.format(new Date()) + "\t" + "Task finished");
				logWriter.close();

				progressBar1.setString("");
				progressBar2.setValue(100);
				progressBar1.setIndeterminate(false);
				progressBar2.setString("Task finished");
				
			} else {
				progressBar1.setString("");
				progressBar2.setValue(100);
				progressBar1.setIndeterminate(false);
				progressBar2.setString("Task failed");
			}
			
			JsonParaHandler.export(parameter, new File(parameter.getParameterDir(), "MetaLab.json"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isFinish() {
		return finished;
	}
}
