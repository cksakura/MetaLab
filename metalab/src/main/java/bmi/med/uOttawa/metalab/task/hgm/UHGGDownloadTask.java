package bmi.med.uOttawa.metalab.task.hgm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.MGYG.HapExtractor;
import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;
import bmi.med.uOttawa.metalab.core.tools.ResumableDownloadTask;
import bmi.med.uOttawa.metalab.task.mag.MagDatabase;

public class UHGGDownloadTask extends SwingWorker<String, Void> {
	
	private static String taskName = "UHGG database download task";
	private static final Logger LOGGER = LogManager.getLogger(UHGGDownloadTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private int waitHours = 4;
	private int thread;
	private File outputDirFile;
	private boolean finish = false;
	
	public UHGGDownloadTask(String output) {
		this.outputDirFile = new File(output);
		this.thread = Runtime.getRuntime().availableProcessors();;
	}
	
	public UHGGDownloadTask(String output, int thread) {
		this.outputDirFile = new File(output);
		this.thread = thread;
	}
	
	@Override
	protected String doInBackground() throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info(taskName + ": started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": started");

		if (!outputDirFile.exists()) {
			outputDirFile.mkdirs();
		}

		File originalDbFile = new File(outputDirFile, "original_db");
		if (!originalDbFile.exists()) {
			originalDbFile.mkdirs();
		}

		ExecutorService executor = Executors.newFixedThreadPool(thread);

		ArrayList<Future<?>> flist = new ArrayList<Future<?>>();
		BufferedReader reader = null;
		String line = null;
		try {

			reader = new BufferedReader(new InputStreamReader(ResourceLoader.load(MagDatabase.human_gut.name())));

			line = reader.readLine();

			File hostDesFile = new File(outputDirFile, MagDatabase.human_gut.getHostNameString());

			if (!hostDesFile.exists() || hostDesFile.length() == 0) {
				ResumableDownloadTask task = new ResumableDownloadTask(line, hostDesFile.getAbsolutePath());
				Future<?> f = executor.submit(task);
				flist.add(f);
			}

			// all_metadata.tsv, not used here
			line = reader.readLine();

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				if (cs.length == 2) {
					String name = cs[0].substring(cs[0].lastIndexOf("/") + 1, cs[0].length());
					long fileLength = Long.parseLong(cs[1]);

					File desFile = new File(originalDbFile, name);

					if (!desFile.exists() || desFile.length() < fileLength) {
						ResumableDownloadTask task = new ResumableDownloadTask(cs[0], desFile.getAbsolutePath());
						Future<?> f = executor.submit(task);
						flist.add(f);
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.info(taskName + ": unable to download the UHGG database");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": unable to download the UHGG database");
		}

		for (int i = 0; i < flist.size(); i++) {
			Future<?> f = flist.get(i);
			f.get();
			setProgress((int) (i * 0.02));
		}

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(waitHours, TimeUnit.HOURS);

			setProgress(98);

			if (finish) {

				LOGGER.info(taskName + ": extracting high abundant proteins...");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": extracting high abundant proteins...");

				File hapFile = new File(outputDirFile, "rib_elon.fasta");
				HapExtractor extractor = new HapExtractor(true);
				extractor.extract(originalDbFile, hapFile);

				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");

				setProgress(100);

				this.finish = true;

				return "Task finished";

			} else {
				LOGGER.info(taskName + ": task dosen't finish in a long time, please try again");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please try again");

				return "Task dosen't finish in a long time, please try again";
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}

		return "Task failed";
	}

	public boolean isFinish() {
		return finish;
	}

	
}
