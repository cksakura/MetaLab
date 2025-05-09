package bmi.med.uOttawa.metalab.dbSearch.diann;

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

import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;

public class DiaNNTask {

	private static String taskName = "DiaNN task";
	private static final Logger LOGGER = LogManager.getLogger(DiaNNTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private File diaNNFile;
	private ArrayList<String> parList;
	private StringBuilder headsb;
	private StringBuilder tailsb;

	public DiaNNTask(String diaNN) {
		this(new File(diaNN));
	}

	public DiaNNTask(File diaNN) {
		this.diaNNFile = diaNN;
		this.parList = new ArrayList<String>();
		this.headsb = new StringBuilder();
		this.headsb.append("@echo off\n");
		this.headsb.append("cd /d \"" + diaNNFile.getParent() + "\"\n");

		this.tailsb = new StringBuilder();
		this.tailsb.append("if errorlevel 1 (\n");
		this.tailsb.append("\techo Error: Failed to start the application.\n");
		this.tailsb.append(") else (\n");
		this.tailsb.append("\techo Application started successfully.\n");
		this.tailsb.append(")\n");
		this.tailsb.append("exit");
	}

	public File getDiaNNFile() {
		return diaNNFile;
	}

	/**
	 * convert raw files to dia files
	 * 
	 * @param par
	 * @param raws
	 */
	public void addTask(DiannParameter par, String[] raws) {
		String cmd = par.generateConvert();
		File rawFile = (new File(raws[0])).getParentFile();
		File batFile = new File(rawFile, "convert.bat");
		File cfgFile = new File(rawFile, "convert.cfg");

		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!");
			 * 
			 * 
			 * writer.println(); writer.println("endlocal");
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * StringBuilder sb = new StringBuilder(); sb.append(batFile.getAbsolutePath());
		 * for (String raw : raws) { sb.append("\t").append(raw); }
		 * this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param raws
	 * @param out
	 * @param lib
	 */
	public void addTask(DiannParameter par, String[] raws, String out, String... libs) {
		String cmd = par.generateLibSearch(raws, out, libs);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.print("exit");
			writer.close();

			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();

		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}

		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param raws
	 * @param out
	 * @param lib
	 */
	public void addTask(DiannParameter par, String[] raws, String out, boolean fastSearch, String... lib) {
		String cmd;
		if (fastSearch) {
			cmd = par.generateLibSearchFast(raws, out, lib);
		} else {
			cmd = par.generateLibSearch(raws, out, lib);
		}
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");

		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + "batFile" + batFile);
		}
		/*
		 * StringBuilder sb = new StringBuilder(); sb.append(batFile.getAbsolutePath());
		 * for (String raw : raws) { sb.append("\t").append(raw); }
		 * this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param raws
	 * @param out
	 * @param lib
	 */
	public void addTask(DiannParameter par, String[] raws, String out, String lib, boolean genSpecLib,
			boolean autoInfer) {
		String cmd = par.generateLibSearch(raws, out, lib, genSpecLib, autoInfer);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * StringBuilder sb = new StringBuilder(); sb.append(batFile.getAbsolutePath());
		 * for (String raw : raws) { sb.append("\t").append(raw); }
		 * this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param libSearchPar
	 * @param out
	 * @param lib
	 */
	public void addTask(DiannParameter par, DiaLibSearchPar libSearchPar, String out, String... lib) {
		String cmd = par.generateLibSearch(libSearchPar, out, lib);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			String[] raws = libSearchPar.getDiaFiles();
			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * String[] raws = libSearchPar.getDiaFiles(); StringBuilder sb = new
		 * StringBuilder(); sb.append(batFile.getAbsolutePath()); for (String raw :
		 * raws) { sb.append("\t").append(raw); } this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param libSearchPar
	 * @param out
	 * @param lib
	 */
	public void addFastTask(DiannParameter par, DiaLibSearchPar libSearchPar, String out, String... lib) {
		String cmd = par.generateLibSearchFast(libSearchPar, out, lib);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			String[] raws = libSearchPar.getDiaFiles();
			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * String[] raws = libSearchPar.getDiaFiles(); StringBuilder sb = new
		 * StringBuilder(); sb.append(batFile.getAbsolutePath()); for (String raw :
		 * raws) { sb.append("\t").append(raw); } this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * 
	 * @param par
	 * @param libSearchPar
	 * @param output
	 * @param fast
	 * @param genLib
	 * @param multiQvaule
	 * @param libs
	 */
	public void addTask(DiannParameter par, DiaLibSearchPar libSearchPar, String output, boolean fast, boolean genLib,
			boolean qvalueCutoff, String... libs) {
		String cmd = par.generateLibSearch(libSearchPar, output, fast, genLib, qvalueCutoff, libs);
		File batFile = new File(output + ".bat");
		File cfgFile = new File(output + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			String[] raws = libSearchPar.getDiaFiles();
			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * String[] raws = libSearchPar.getDiaFiles(); StringBuilder sb = new
		 * StringBuilder(); sb.append(batFile.getAbsolutePath()); for (String raw :
		 * raws) { sb.append("\t").append(raw); } this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search library
	 * 
	 * @param par
	 * @param libSearchPar
	 * @param out
	 * @param lib
	 */
	public void addFastLibTask(DiannParameter par, DiaLibSearchPar libSearchPar, String out, String... lib) {
		String cmd = par.generateLibSearchFastLib(libSearchPar, out, lib);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			String[] raws = libSearchPar.getDiaFiles();
			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * String[] raws = libSearchPar.getDiaFiles(); StringBuilder sb = new
		 * StringBuilder(); sb.append(batFile.getAbsolutePath()); for (String raw :
		 * raws) { sb.append("\t").append(raw); } this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * search fasta
	 * 
	 * @param par
	 * @param raws
	 * @param out
	 * @param fasta
	 * @param libOut
	 * @param fast
	 */
	public void addTask(DiannParameter par, String[] raws, String out, String fasta, String libOut, boolean fast) {
		String cmd = par.generateFastaSearch(raws, out, fasta, libOut, fast);
		File batFile = new File(out + ".bat");
		File cfgFile = new File(out + ".cfg");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.println(headsb);
			/*
			 * writer.println("setlocal enabledelayedexpansion"); writer.println(); writer.
			 * println("REM Get all the file paths passed as parameters and store them in a variable"
			 * ); writer.println("set files="); writer.println(); writer.println(
			 * "REM Loop through each file path and append it to the variable with a space and a --f option"
			 * ); writer.println("for %%f in (%*) do (");
			 * writer.println("\tset files=!files! --f \"%%f\""); writer.println(")");
			 * writer.println();
			 * writer.println("REM Print the content of the start command");
			 * writer.println("echo !files!"); writer.println(); writer.println("start /B "
			 * + diaNNFile.getName() + " " + cmd + "!files!"); writer.println();
			 * writer.println("endlocal"); writer.println();
			 */
			writer.println("start /B " + diaNNFile.getName() + " --cfg " + cfgFile.getAbsolutePath());
			writer.println();
			writer.print("exit");
			writer.close();

			PrintWriter cfgWriter = new PrintWriter(cfgFile);
			cfgWriter.print(cmd);
			for (int i = 0; i < raws.length; i++) {
				cfgWriter.print(" --f \"" + raws[i] + "\"");
			}
			cfgWriter.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
		}
		/*
		 * StringBuilder sb = new StringBuilder(); sb.append(batFile.getAbsolutePath());
		 * for (String raw : raws) { sb.append("\t").append(raw); }
		 * this.parList.add(sb.toString());
		 */
		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * generate library from fasta
	 * 
	 * @param par
	 * @param raws
	 * @param out
	 * @param fasta
	 * @param libOut
	 * @param fast
	 */
	public void addTask(DiannParameter par, String fasta, String libOut, boolean fast) {
		String cmd = par.generateFasta(fasta, libOut, fast);
		File batFile = new File(libOut + ".bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.print(headsb);
			writer.println("start /B " + diaNNFile.getName() + " " + cmd);
			writer.print(tailsb);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + "batFile" + batFile);
		}

		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * library from .tsv to .speclib
	 * 
	 * @param par
	 * @param tsv
	 */
	public void addTask(DiannParameter par, String tsv) {
		String cmd = par.generateTsv2Lib(tsv);
		File batFile = new File(tsv + ".bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.print(headsb);
			writer.println("start /B " + diaNNFile.getName() + " " + cmd);
			writer.print(tailsb);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + "batFile" + batFile);
		}

		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * library from .tsv to .speclib, extracting protein names from the fasta files
	 * 
	 * @param par
	 * @param tsv
	 */
	public void addTask(DiannParameter par, String tsv, String[] fasta) {
		String cmd = par.generateTsv2Lib(tsv, fasta);
		File batFile = new File(tsv + ".bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.print(headsb);
			writer.println("start /B " + diaNNFile.getName() + " " + cmd);
			writer.print(tailsb);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + "batFile" + batFile);
		}

		this.parList.add(batFile.getAbsolutePath());
	}

	/**
	 * library from .speclib to .tsv
	 * 
	 * @param par
	 * @param tsv
	 */
	public void addTask(DiannParameter par, String lib, String tsv) {
		String cmd = par.generateLib2Tsv(lib, tsv);
		File batFile = new File(tsv + ".bat");
		try (PrintWriter writer = new PrintWriter(batFile)) {
			writer.print(headsb);
			writer.println("start /B " + diaNNFile.getName() + " " + cmd);
			writer.print(tailsb);
			writer.close();
		} catch (IOException e) {
			LOGGER.error(taskName + ": error in writing the .bat file to " + batFile, e);
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": error in writing the .bat file to " + batFile);
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
						+ ": task dosen't finish in a long time, please restart MetaLab after the DiaNN task finish");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please restart MetaLab after the DiaNN task finish");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}
	}

	public boolean runCMD(String cmd, JProgressBar bar) {

		try {

			int id = diaNNFile.getAbsolutePath().indexOf(":");
			if (id < 0) {
				if (id < 0) {
					LOGGER.error(taskName + ": cann't find the directory of DiaNN.exe");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": cann't find the directory of DiaNN.exe");

					return false;
				}
			}

			PrintWriter writer = new PrintWriter(cmd);
			writer.println(diaNNFile.getAbsolutePath().substring(0, id) + ":");
			writer.println("cd " + diaNNFile.getParent());

			for (int i = 0; i < parList.size(); i++) {
				writer.println(diaNNFile.getName() + " " + parList.get(i));
			}

			writer.print("exit");
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing the executable command for DiaNN.exe to " + cmd, e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in writing the executable command for DiaNN.exe to " + cmd);

			return false;
		}

		try {
			int finishCount = 0;

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

				if (lineStr.startsWith("DIA-NN 1.8 (Data-Independent Acquisition by Neural Networks)")) {

					double percent = (double) finishCount / (double) parList.size() * 100.0;
					bar.setValue((int) percent);

					finishCount++;
				}
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");

				bar.setValue(100);
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(taskName + ": error in running the executable command by DiaNN.exe", e);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": error in running the executable command by DiaNN.exe");

			return false;
		}

		return true;
	}
}
