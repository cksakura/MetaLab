package bmi.med.uOttawa.metalab.dbSearch.alphapept;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
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

public class AlphapeptTask {

	private static String taskName = "Alphapept task";
	private static final Logger LOGGER = LogManager.getLogger(AlphapeptTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File alphapeptExe;
	private ArrayList<String> parList;
	
	public AlphapeptTask(String alphapeptExe) {
		this.alphapeptExe = new File(alphapeptExe);
		this.parList = new ArrayList<String>();
	}
	
	public void addTask(File workflowFile) {
		this.parList.add(workflowFile.getAbsolutePath());
	}

	public boolean runSingle(File workflowFile) {

		try {

			StringBuilder sb = new StringBuilder();
			sb.append(alphapeptExe.getName()).append(" ");
			sb.append("workflow ");
			sb.append("\"").append(workflowFile.getAbsolutePath()).append("\"");

			int id = alphapeptExe.getAbsolutePath().indexOf(":");
			if (id < 0) {
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": cann't find the directory of Alphapept.exe");
				LOGGER.error(taskName + ": cann't find the directory of Alphapept.exe");
				return false;
			}

			File batFile = new File(workflowFile.getParent(), workflowFile.getName() + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			batWriter.println("@echo off");
			batWriter.println("cd /d \"" + alphapeptExe.getParent() + "\"");
			batWriter.println("start /B " + sb);

			batWriter.println("if errorlevel 1 (");
			batWriter.println("\techo Error: Failed to start the application.");
			batWriter.println(") else (");
			batWriter.println("\techo Application started successfully.");
			batWriter.println(")");
			batWriter.println("exit");
			batWriter.close();

			System.out.println(format.format(new Date()) + "\t" + sb);
			LOGGER.info(taskName + ": " + sb);

			String[] args = { "cmd.exe", "/c", "start", "/min", batFile.getAbsolutePath() };

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String line1 = "";
			while ((line1 = inBr.readLine()) != null) {
				System.out.println(format.format(new Date()) + "\t" + line1);
				LOGGER.info(taskName + ": " + line1);
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

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");

			return false;
		}

		return true;
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
			File workflowFile = new File(parList.get(i));
			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {

						StringBuilder sb = new StringBuilder();
						sb.append(alphapeptExe.getName()).append(" ");
						sb.append("workflow ");
						sb.append("\"").append(workflowFile.getAbsolutePath()).append("\"");

						int id = alphapeptExe.getAbsolutePath().indexOf(":");
						if (id < 0) {
							System.out.println(format.format(new Date()) + "\t" + taskName
									+ ": cann't find the directory of Alphapept.exe");
							LOGGER.error(taskName + ": cann't find the directory of Alphapept.exe");
							return;
						}

						File batFile = new File(workflowFile.getParent(), workflowFile.getName() + ".bat");
						PrintWriter batWriter = new PrintWriter(batFile);

						batWriter.println("@echo off");
						batWriter.println("cd /d \"" + alphapeptExe.getParent() + "\"");
						batWriter.println("start /B " + sb);

						batWriter.println("if errorlevel 1 (");
						batWriter.println("\techo Error: Failed to start the application.");
						batWriter.println(") else (");
						batWriter.println("\techo Application started successfully.");
						batWriter.println(")");
						batWriter.println("exit");
						batWriter.close();

						System.out.println(format.format(new Date()) + "\t" + sb);
						LOGGER.info(taskName + ": " + sb);

						String[] args = { "cmd.exe", "/c", "start", "/min", batFile.getAbsolutePath() };
//						String[] args = { "cmd.exe", "call", "/min", batFile.getAbsolutePath() };
						
						ProcessBuilder pb = new ProcessBuilder(args);
						Process p = pb.start();

						BufferedInputStream in = new BufferedInputStream(p.getInputStream());
						BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
						String line1 = "";
						while ((line1 = inBr.readLine()) != null) {
							System.out.println(format.format(new Date()) + "\t" + line1);
							LOGGER.info(taskName + ": " + line1);
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
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the AlphaPept task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the AlphaPept task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}

	public void run(File workflowFile, int waitHours) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {

					String[] args = { "cmd.exe", "/c", "call", alphapeptExe.getAbsolutePath(), "workflow",
							workflowFile.getAbsolutePath() };

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
						+ ": task dosen't finish in a long time, please restart MetaLab after the AlphaPept task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the AlphaPept task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}

	public static void main(String[] args) {

		try {
			ProcessBuilder pb = new ProcessBuilder(new String[] { "cmd.exe", "/c", "call",
					"Z:\\Kai\\Raw_files\\single_species\\101114\\MetaLab_alphapept\\mag_result\\Zhibin_20221021_singleStrain_E6\\hap\\hap.yaml.bat" });

			Process p = pb.start();
			BufferedReader inBr = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line1 = "";
			while ((line1 = inBr.readLine()) != null) {
				System.out.println(line1);
				if (line1.startsWith("An exception occured")) {
					break;
				}
			}
			inBr.close();
			// ...

			// Destroy the process
			p.destroy();
			System.out.println("432");
			/*
			 * // Wait for the process to finish int exitCode = p.waitFor();
			 * 
			 * // Check if the process terminated successfully if (exitCode == 0) {
			 * System.out.println("Process terminated successfully."); } else {
			 * System.out.println("Process did not terminate successfully. Exit code: " +
			 * exitCode); }
			 */
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Ensure to destroy the process in case of an exception
			System.out.println("finallly 447");
		}

	}
}
