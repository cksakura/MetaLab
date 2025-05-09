package bmi.med.uOttawa.metalab.dbSearch.sage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SageTask {

	private static String taskName = "Sage task";
	private static final Logger LOGGER = LogManager.getLogger(SageTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File sageFile;
	private ArrayList<String> parList;
	private StringBuilder headsb;
	private StringBuilder tailsb;

	public SageTask(String sage) {
		this(new File(sage));
	}

	public SageTask(File sage) {
		this.sageFile = sage;
		this.parList = new ArrayList<String>();
		this.headsb = new StringBuilder();
		this.headsb.append("@echo off\n");
		this.headsb.append("cd /d \"" + sageFile.getParent() + "\"\n");

		this.tailsb = new StringBuilder();
		this.tailsb.append("if errorlevel 1 (\n");
		this.tailsb.append("\techo Error: Failed to start the application.\n");
		this.tailsb.append(") else (\n");
		this.tailsb.append("\techo Application started successfully.\n");
		this.tailsb.append(")\n");
		this.tailsb.append("exit");
	}

	public void addTask(File configFile) {
		File batFile = new File(configFile.getParentFile(), "sage.bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			writer.println("start /B " + sageFile.getName() + " " + configFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		this.parList.add(batFile.getAbsolutePath());
	}

	public File getSageFile() {
		return sageFile;
	}

	public void run(int waitHours) {
		run(1, waitHours);
	}

	public void run(int threadPool, int waitHours) {

		if (this.parList.size() == 0) {
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadPool);
		for (int i = 0; i < parList.size(); i++) {

			String pari = parList.get(i);

			LOGGER.info(pari);

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {

						ArrayList<String> commands = new ArrayList<String>();
						commands.add("cmd.exe");
						commands.add("/c");
						commands.add("start");
						commands.add("/min");

						String[] cs = pari.split("\t");
						for (String parString : cs) {
							commands.add(parString);
						}
						ProcessBuilder pb = new ProcessBuilder(commands);
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

			this.parList = new ArrayList<String>();

			if (finish) {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the Sage task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the Sage task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}

	public boolean runCMD(String cmd, JProgressBar bar) {

		try {

			int id = sageFile.getAbsolutePath().indexOf(":");
			if (id < 0) {
				if (id < 0) {
					LOGGER.error(taskName + ": cann't find the directory of sage.exe");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": cann't find the directory of sage.exe");

					return false;
				}
			}

			PrintWriter writer = new PrintWriter(cmd);
			writer.println(sageFile.getAbsolutePath().substring(0, id) + ":");
			writer.println("cd " + sageFile.getParent());

			for (int i = 0; i < parList.size(); i++) {
				writer.println(sageFile.getName() + " " + parList.get(i));
			}

			writer.print("exit");
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the executable command for sage.exe to " + cmd, e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the executable command for sage.exe to " + cmd);

			return false;
		}

		try {
			ArrayList<String> cmdList = new ArrayList<String>();
			cmdList.add("cmd.exe");
			cmdList.add("/c");
			cmdList.add("start");
			cmdList.add(cmd);

			ProcessBuilder pb = new ProcessBuilder(cmdList);
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				LOGGER.info(lineStr);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");

				bar.setValue(100);
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in running the executable command by sage.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in running the executable command by sage.exe");

			return false;
		}

		return true;
	}

}
