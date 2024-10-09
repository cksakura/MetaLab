package bmi.med.uOttawa.metalab.dbSearch.deepDetect;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.management.OperatingSystemMXBean;

import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;

public class DeepDetectTask extends MetaLabTask {

	private static String taskName = "DeepDetect task";
	private static final Logger LOGGER = LogManager.getLogger(DeepDetectTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File deepDetectFile;
	private String drive;
	private ArrayList<File> batFileList;
	private HashSet<String> genomeSet;

	private DeepDetectParameter par;
	private ExecutorService executor;
	private int threads;
	private JTextField timeLeft;
	
	private MagDbItem magdb;

	public DeepDetectTask(String deepDetect) {
		this(new File(deepDetect));
	}

	public DeepDetectTask(File deepDetectFile) {
		super(new JProgressBar(), deepDetectFile.getParentFile());
		this.deepDetectFile = deepDetectFile;
		String path = deepDetectFile.getAbsolutePath();
		int id = path.indexOf(":");
		if (id < 0) {
			Exception e = new IOException(
					"The absolute path of the DeepDetect executable file (DeepDetect.exe) was not found.");
			LOGGER.error(taskName + ": can't find DeepDetect.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": can't find DeepDetect.exe");
		} else {
			drive = path.substring(0, id);
		}
		this.batFileList = new ArrayList<File>();
	}

	public DeepDetectTask(File deepDetectFile, HashSet<String> genomeSet) {
		super(new JProgressBar(), deepDetectFile.getParentFile());
		this.deepDetectFile = deepDetectFile;
		String path = deepDetectFile.getAbsolutePath();
		int id = path.indexOf(":");
		if (id < 0) {
			Exception e = new IOException(
					"The absolute path of the DeepDetect executable file (DeepDetect.exe) was not found.");
			LOGGER.error(taskName + ": can't find DeepDetect.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": can't find DeepDetect.exe");
		} else {
			drive = path.substring(0, id);
		}
		this.batFileList = new ArrayList<File>();
		this.genomeSet = genomeSet;
	}

	public DeepDetectTask(File deepDetectFile, DeepDetectParameter par, MagDbItem magdb, int threads,
			JProgressBar bar, JTextField timeLeft) {
		super(bar, magdb.getCurrentFile());
		this.deepDetectFile = deepDetectFile;
		String path = deepDetectFile.getAbsolutePath();
		int id = path.indexOf(":");
		if (id < 0) {
			Exception e = new IOException(
					"The absolute path of the DeepDetect executable file (DeepDetect.exe) was not found.");
			LOGGER.error(taskName + ": can't find DeepDetect.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": can't find DeepDetect.exe");
		} else {
			drive = path.substring(0, id);
		}
		this.batFileList = new ArrayList<File>();

		this.par = par;
		this.magdb = magdb;
		this.threads = threads;
		this.executor = Executors.newFixedThreadPool(threads);
		this.timeLeft = timeLeft;
	}
	
	public DeepDetectTask(File deepDetectFile, DeepDetectParameter par, MagDbItem magdb,
			JProgressBar bar, JTextField timeLeft) {
		super(bar, magdb.getCurrentFile());
		this.deepDetectFile = deepDetectFile;
		String path = deepDetectFile.getAbsolutePath();
		int id = path.indexOf(":");
		if (id < 0) {
			Exception e = new IOException(
					"The absolute path of the DeepDetect executable file (DeepDetect.exe) was not found.");
			LOGGER.error(taskName + ": can't find DeepDetect.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": can't find DeepDetect.exe");
		} else {
			drive = path.substring(0, id);
		}
		this.batFileList = new ArrayList<File>();

		this.par = par;
		this.magdb = magdb;

		long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
				.getTotalMemorySize() / 1024 / 1024 / 1024;

		this.threads = (int) (totalMemorySize / 16);
		this.executor = Executors.newFixedThreadPool(threads);
		this.timeLeft = timeLeft;
	}

	public DeepDetectTask(File deepDetectFile, DeepDetectParameter par, MagDbItem magdb, int threads,
			JProgressBar bar, JTextField timeLeft, HashSet<String> genomeSet) {
		super(bar, magdb.getCurrentFile());

		this.deepDetectFile = deepDetectFile;
		String path = deepDetectFile.getAbsolutePath();
		int id = path.indexOf(":");
		if (id < 0) {
			Exception e = new IOException(
					"The absolute path of the DeepDetect executable file (DeepDetect.exe) was not found.");
			LOGGER.error(taskName + ": can't find DeepDetect.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": can't find DeepDetect.exe");
		} else {
			drive = path.substring(0, id);
		}
		this.batFileList = new ArrayList<File>();

		this.par = par;
		this.magdb = magdb;
		this.threads = threads;
		this.executor = Executors.newFixedThreadPool(threads);
		this.timeLeft = timeLeft;
		this.genomeSet = genomeSet;
	}

	/**
	 * Predict all the genomes in a MAG database
	 */
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				timeLeft.setText("Calculating...");
				System.out.println(format.format(new Date()) + "\t" + "Peptide detectability predection started");
			}
		});

		File dbfile = magdb.getOriginalDbFolder();
		File[] files = dbfile.listFiles();

		File predictedFile = new File(dbfile.getParentFile(), "predicted");
		if (!predictedFile.exists()) {
			predictedFile.mkdir();
		}

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.endsWith(".faa") || name.endsWith(".fasta")) {
				name = name.substring(0, name.lastIndexOf("."));
				if (genomeSet == null || (genomeSet != null && genomeSet.contains(name))) {
					File predictedPepFile = new File(predictedFile, name + ".predicted.tsv");
					if (!predictedPepFile.exists()) {

						par.setInput(files[i]);
						par.setOutput(predictedPepFile);

						File batFile = new File(predictedFile, name + ".deepDetect.bat");
						try {
							PrintWriter batWriter = new PrintWriter(batFile);
							batWriter.println("@echo off");
							batWriter.println("cd /d \"" + deepDetectFile.getParent() + "\"");
							batWriter.println("start /B " + deepDetectFile.getName() + " " + par);
							batWriter.print("exit");
							batWriter.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							LOGGER.error(taskName + ": error in writing the .bat file", e);
							return false;
						}
						this.batFileList.add(batFile);
					}
				}
			}
		}

		int totalGenomeCount = magdb.getSpeciesCount();

		AtomicInteger completedTasks = new AtomicInteger(magdb.getSpeciesCount() - batFileList.size());
		
		LOGGER.info(taskName + ": " + batFileList.size() + " files will be processed");
		System.out.println(format.format(new Date()) + "\t" + batFileList.size() + " files will be processed");

		for (int i = 0; i < batFileList.size(); i++) {
			File batFile = batFileList.get(i);

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {

						long start = System.currentTimeMillis();

						String[] args = { "cmd.exe", "/c", "start", "/min", batFile.getAbsolutePath() };
						ProcessBuilder pb = new ProcessBuilder(args);
						Process p = pb.start();

						BufferedInputStream in = new BufferedInputStream(p.getInputStream());
						BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
						String[] lines = new String[1];
						while ((lines[0] = inBr.readLine()) != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									System.out.println(format.format(new Date()) + "\t" + lines[0]);
								}
							});
						}

						if (p.waitFor() != 0) {
							if (p.exitValue() == 1)
								LOGGER.error("false");
						}
						inBr.close();
						in.close();

						int completed = completedTasks.incrementAndGet();
						double progress = (double) completed / (double) totalGenomeCount;
						int value = (int) (progress * 100);
						// update the value of the progress bar
						bar1.setValue(value);
						bar1.setString(completed + "/" + totalGenomeCount);

						long end = System.currentTimeMillis();
						int runOneTask = (int) ((end - start) / 60000);
						int restTime = (int) ((double) (totalGenomeCount - completed) * runOneTask / (double) threads);
						if (restTime == 0) {
							timeLeft.setText("About <1 minutes left");
						} else {
							timeLeft.setText("About " + restTime + " minutes left");
						}
						String batName = batFile.getName();

						LOGGER.info(
								taskName + ": predicting " + batName.substring(0, batName.length() - 4) + " finished");
						System.out.println(format.format(new Date()) + "\t" + "predicting "
								+ batName.substring(0, batName.length() - 4) + " finished");

					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						LOGGER.error(taskName + ": error in predicting peptide detectability", e);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in predicting peptide detectability");
					}
				}
			});
		}

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(totalGenomeCount, TimeUnit.HOURS);

			for (int i = 0; i < batFileList.size(); i++) {
				File batFile = batFileList.get(i);
				FileUtils.deleteQuietly(batFile);
			}

			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				if (name.endsWith(".bat")) {
					FileUtils.deleteQuietly(files[i]);
				}
			}
			
			batFileList = new ArrayList<File>();

			for (int i = 0; i < files.length; i++) {
				String name = files[i].getName();
				if (name.endsWith(".faa") || name.endsWith(".fasta")) {
					name = name.substring(0, name.lastIndexOf("."));

					File predictedPepFile = new File(predictedFile, name + ".predicted.tsv");
					if (!predictedPepFile.exists()) {

						File tempFastaFile = new File(predictedFile, name + ".faa");

						try {

							BufferedReader reader = new BufferedReader(new FileReader(files[i]));
							PrintWriter tempFastaWriter = new PrintWriter(tempFastaFile);
							StringBuilder sb = new StringBuilder();
							String line = null;
							boolean use = true;
							while ((line = reader.readLine()) != null) {
								if (line.startsWith(">")) {
									if (use && sb.length() > 0) {
										tempFastaWriter.print(sb);
									}

									use = true;
									sb = new StringBuilder();
									sb.append(line).append("\n");
								} else {
									if (line.contains("X")) {
										use = false;
									} else {
										sb.append(line).append("\n");
									}
								}
							}

							if (use && sb.length() > 0) {
								tempFastaWriter.print(sb);
							}

							reader.close();
							tempFastaWriter.close();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							LOGGER.error(taskName + ": error in writing the temp fasta file", e);
							return false;
						}

						par.setInput(tempFastaFile);
						par.setOutput(predictedPepFile);

						File batFile = new File(predictedFile, name + ".deepDetect.bat");
						try {
							PrintWriter batWriter = new PrintWriter(batFile);
							batWriter.println("@echo off");
							batWriter.println("cd /d \"" + deepDetectFile.getParent() + "\"");
							batWriter.println("start /B " + deepDetectFile.getName() + " " + par);
							batWriter.print("exit");
							batWriter.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							LOGGER.error(taskName + ": error in writing the .bat file", e);
							return false;
						}
						this.batFileList.add(batFile);
					}
				}
			}

			if (batFileList.size() > 0) {

				this.executor = Executors.newFixedThreadPool(threads);

				for (int i = 0; i < batFileList.size(); i++) {
					File batFile = batFileList.get(i);
					if (batFile.exists()) {
						executor.submit(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								try {

									long start = System.currentTimeMillis();

									String[] args = { "cmd.exe", "/c", "start", "/min", batFile.getAbsolutePath() };
									ProcessBuilder pb = new ProcessBuilder(args);
									Process p = pb.start();

									BufferedInputStream in = new BufferedInputStream(p.getInputStream());
									BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
									String[] lines = new String[1];
									while ((lines[0] = inBr.readLine()) != null) {
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												System.out.println(format.format(new Date()) + "\t" + lines[0]);
											}
										});
									}

									if (p.waitFor() != 0) {
										if (p.exitValue() == 1)
											LOGGER.error("false");
									}
									inBr.close();
									in.close();

									int completed = completedTasks.incrementAndGet();
									double progress = (double) completed / (double) totalGenomeCount;
									int value = (int) (progress * 100);
									// update the value of the progress bar
									bar1.setValue(value);
									bar1.setString(completed + "/" + totalGenomeCount);

									long end = System.currentTimeMillis();
									int runOneTask = (int) ((end - start) / 60000);
									int restTime = (int) ((double) (totalGenomeCount - completed) * runOneTask
											/ (double) threads);
									if (restTime == 0) {
										timeLeft.setText("About <1 minutes left");
									} else {
										timeLeft.setText("About " + restTime + " minutes left");
									}

								} catch (IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									LOGGER.error(taskName + ": error in predicting peptide detectability", e);
									System.err.println(format.format(new Date()) + "\t" + taskName
											+ ": error in predicting peptide detectability");
								}
							}
						});
					}
				}

				executor.shutdown();

				finish = executor.awaitTermination(totalGenomeCount, TimeUnit.HOURS);

				for (int i = 0; i < batFileList.size(); i++) {
					File batFile = batFileList.get(i);
					FileUtils.deleteQuietly(batFile);
				}

				for (int i = 0; i < files.length; i++) {
					String name = files[i].getName();
					if (name.endsWith(".bat")) {
						FileUtils.deleteQuietly(files[i]);
					}
				}
			}

			bar1.setValue(100);
			bar1.setString(totalGenomeCount + "/" + totalGenomeCount);

			timeLeft.setText("Task finished");

			this.batFileList = new ArrayList<File>();

			if (finish) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the DeepDetect task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the DeepDetect task finish");

				return false;
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}

		int predictedFileCount = predictedFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith(".predicted.tsv"))
					return true;
				return false;
			}
		}).length;

		if (predictedFileCount == magdb.getSpeciesCount()) {
			return true;
		} else {
			return false;
		}
	}

	public void forceStop() {
		if (executor != null) {
			this.executor.shutdown();
			this.cancel(true);

			Runtime run = Runtime.getRuntime();
			try {
				Process p = run.exec(new String[] { "taskkill", "/F", "/IM", "DeepDetect.exe" });
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					if (lineStr.startsWith("SUCCESS")) {
						System.out.println(lineStr);
					}
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addTask(DeepDetectParameter par) {
		File inputFile = par.getInput();
		File outputFile = par.getOutput();
		String name = inputFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File batFile = new File(outputFile.getParent(), name + ".deepDetect.bat");
		try {
			PrintWriter writer = new PrintWriter(batFile);
			writer.println(drive + ":");
			writer.println("cd " + deepDetectFile.getParent());
			writer.println(deepDetectFile.getName() + " " + par);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error(taskName + ": error in writing the .bat file", e);
		}
		this.batFileList.add(batFile);
	}

	public void run(int waitHours) {
		run(1, waitHours);
	}

	public void run(int threadPool, int waitHours) {

		if (this.batFileList.size() == 0) {
			return;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadPool);
		for (int i = 0; i < batFileList.size(); i++) {
			File batFile = batFileList.get(i);

			executor.submit(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					try {
						Process p = Runtime.getRuntime().exec(new String[] { batFile.getAbsolutePath() });
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

			this.batFileList = new ArrayList<File>();

			if (finish) {
				LOGGER.info(taskName + ": finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");
			} else {
				LOGGER.info(taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the DeepDetect task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the DeepDetect task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}
	
	public static void main(String[] args) {

		/*
		DeepDetectParameter parameter = new DeepDetectParameter();
		parameter.setInput(new File("Z:\\Kai\\Raw_files\\core_pep_db\\hap.fasta"));
		parameter.setOutput(new File("Z:\\Kai\\Raw_files\\core_pep_db\\hap.deepdetect.tsv"));
		parameter.setMissCleavage(2);

		DeepDetectTask task = new DeepDetectTask("E:\\Exported\\Resources\\DeepDetect\\DeepDetect.exe");
		task.addTask(parameter);
		task.run(1);
		*/
		String nameString = "Z:\\Kai\\Raw_files\\core_pep_db\\hap.fasta";
		System.out.println(nameString.substring(0, nameString.lastIndexOf(".")));
	}

}
