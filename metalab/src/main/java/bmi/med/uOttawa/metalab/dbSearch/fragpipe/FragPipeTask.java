package bmi.med.uOttawa.metalab.dbSearch.fragpipe;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FragPipeTask {

	private static String taskName = "Fragpipe task";
	private static final Logger LOGGER = LogManager.getLogger(FragPipeTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File fragpipeBat;
	private File workflowFolder;
	private ArrayList<String> parList;
	
	public FragPipeTask(String fragpipeBat) {
		this.fragpipeBat = new File(fragpipeBat);
		File binFile = this.fragpipeBat.getParentFile();
		this.workflowFolder = new File(binFile.getParent(), "workflows");
		this.parList = new ArrayList<String>();
	}

	public FragpipeWorkflow getFragpipeWorkflow(int workflowType) {

		if (workflowType < 0 || workflowType > FragpipeWorkflow.workflowTypes.length) {
			
			LOGGER.error(taskName + ": unknown workflow type " + workflowType);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": unknown workflow type " + workflowType);
			
			return null;
		}

		File workflowFile = new File(workflowFolder, FragpipeWorkflow.workflowTypes[workflowType]);
		try {
			FragpipeWorkflow fragpipeWorkflow = FragpipeWorkflow.parse(workflowFile);
			fragpipeWorkflow.setWorkflowId(workflowType);

			return fragpipeWorkflow;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block

			LOGGER.error(taskName + ": can't parse workflow file from " + workflowFile.getAbsolutePath(), e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": can't parse workflow file from "
					+ workflowFile.getAbsolutePath());

		}
		return null;
	}
	
	public void addTask(File workflowFile, File manifestFile, File workDir, int thread) {

		StringBuilder sb = new StringBuilder();
		sb.append(this.fragpipeBat.getName()).append(" ");
		sb.append("--headless ");
		sb.append("--workflow ");
		sb.append("\"").append(workflowFile.getAbsolutePath()).append("\" ");
		sb.append("--manifest ");
		sb.append("\"").append(manifestFile.getAbsolutePath()).append("\" ");
		sb.append("--workdir ");
		sb.append("\"").append(workDir.getAbsolutePath()).append("\" ");
		sb.append("--threads ").append(thread);

		int id = fragpipeBat.getAbsolutePath().indexOf(":");
		if (id < 0) {
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": cann't find the directory of fragpipe.bat");
			LOGGER.error(taskName + ": cann't find the directory of fragpipe.bat");
			return;
		}

		File batFile = new File(workflowFile.getParent(), workflowFile.getName() + ".bat");

		try (PrintWriter batWriter = new PrintWriter(batFile)) {
			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + fragpipeBat.getParent() + "\"");
			batWriter.println("start /B /wait " + sb);
			batWriter.print("exit");
			batWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.parList.add(batFile.getAbsolutePath());
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
			String batFilePath = parList.get(i);
			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {

						String[] args = { "cmd.exe", "/c", "call", batFilePath };

						ProcessBuilder pb = new ProcessBuilder(args);
						Process p = pb.start();

						String line = "";
						BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						// Read and log the standard output
						while ((line = stdInput.readLine()) != null) {
							System.out.println(format.format(new Date()) + "\t" + line);
							LOGGER.info(taskName + ": " + line);

							if (line.startsWith("Cancelling") && line.endsWith("remaining tasks")) {
								LOGGER.error(taskName + ": failed, " + line);
								System.out.println(format.format(new Date()) + "\t" + taskName + ": failed, " + line);
								break;
							} else if (line.contains("ALL JOBS DONE")) {
								LOGGER.info(taskName + ": processing " + batFilePath + " finished");
								System.out.println(format.format(new Date()) + "\t" + taskName + ": processing "
										+ batFilePath + " finished");
								break;
							}
						}
						stdInput.close();
						p.destroy();

					} catch (IOException e) {
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
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the FragPipe task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the FragPipe task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}
	
	public boolean runSingle(File workflowFile, File manifestFile, File workDir, int ram, int thread) {
		try {

			StringBuilder sb = new StringBuilder();
			sb.append(this.fragpipeBat.getName()).append(" ");
			sb.append("--headless ");
			sb.append("--workflow ");
			sb.append("\"").append(workflowFile.getAbsolutePath()).append("\" ");
			sb.append("--manifest ");
			sb.append("\"").append(manifestFile.getAbsolutePath()).append("\" ");
			sb.append("--workdir ");
			sb.append("\"").append(workDir.getAbsolutePath()).append("\" ");
			sb.append("--threads ").append(thread);

			int id = fragpipeBat.getAbsolutePath().indexOf(":");
			if (id < 0) {
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": cann't find the directory of fragpipe.bat");
				LOGGER.error(taskName + ": cann't find the directory of fragpipe.bat");
				return false;
			}

			File batFile = new File(workflowFile.getParent(), workflowFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + fragpipeBat.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
//			batWriter.print("exit");
			batWriter.close();

			System.out.println(format.format(new Date()) + "\t" + sb);
			LOGGER.info(taskName + ": " + sb);

			String[] args = { "cmd.exe", "/c", "call", batFile.getAbsolutePath() };

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			String line = "";
			String lastLine1 = "";
			String lastLine2 = "";

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// Read and log the standard output
			while ((line = stdInput.readLine()) != null) {
				System.out.println(format.format(new Date()) + "\t" + line);
				lastLine1 = line;
				LOGGER.info(taskName + ": " + line);
			}

			// Read and log the standard error
			while ((line = stdError.readLine()) != null) {
				System.out.println(format.format(new Date()) + "\t" + line);
				lastLine2 = line;
				LOGGER.error(taskName + ": " + line);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error(taskName + ": the exit value of the task is not zero, task not finished");
					System.out.println(format.format(new Date()) + "\t" + taskName
							+ ": the exit value of the task is not zero, task not finished");
				}
			}
			stdInput.close();
			stdError.close();

			if (lastLine1.startsWith("Cancelling") && lastLine1.endsWith("remaining tasks")) {
				LOGGER.error(taskName + ": failed, " + lastLine1);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": failed, " + lastLine1);
				p.destroy();
				return false;
			} else if (lastLine1.contains("ALL JOBS DONE")) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
				p.destroy();
				return true;
			}

			if (lastLine2.startsWith("Cancelling") && lastLine2.endsWith("remaining tasks")) {
				LOGGER.error(taskName + ": failed, " + lastLine2);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": failed, " + lastLine2);
				p.destroy();
				return false;
			} else if (lastLine2.contains("ALL JOBS DONE")) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
				p.destroy();
				return true;
			}

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");

			return false;
		}

		return true;
	}

	public void run(File workflowFile, File manifestFile, File workDir, int ram, int thread, int waitHours) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {

					String[] args = new String[] { "cmd.exe", "/c", "call", fragpipeBat.getAbsolutePath(), "--headless",
							"--workflow", workflowFile.getAbsolutePath(), "--manifest", manifestFile.getAbsolutePath(),
							"--workdir", workDir.getAbsolutePath(), "--threads", String.valueOf(thread) };

					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < args.length; i++) {
						sb.append(args[i]).append(" ");
					}

					System.out.println(format.format(new Date()) + "\t" + sb);
					LOGGER.info(taskName + ": " + sb);

					ProcessBuilder pb = new ProcessBuilder(args);
					pb.inheritIO();
					Process p = pb.start();

					BufferedInputStream in = new BufferedInputStream(p.getInputStream());
					BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
					String line1 = "";
					while ((line1 = inBr.readLine()) != null) {
						System.out.println(format.format(new Date()) + "\t" + line1);
					}

					if (p.waitFor() != 0) {
						if (p.exitValue() == 1) {
							LOGGER.error(taskName + ": the exit value of the task is not zero, task not finished");
							System.out.println(format.format(new Date()) + "\t" + taskName
									+ ": the exit value of the task is not zero, task not finished");
						}
					}
					inBr.close();
					in.close();

					p.destroy();
					
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					LOGGER.error(taskName + ": failed", e);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
				}
			}
		});

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(waitHours, TimeUnit.HOURS);

			if (finish) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the FragPipe task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the FragPipe task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}
}
