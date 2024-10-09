/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.tools.FileHandler;
import bmi.med.uOttawa.metalab.task.v1.gui.MainFrame;
import bmi.med.uOttawa.metalab.task.v1.par.JsonParaHandler;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabCli {

	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static Logger LOGGER = LogManager.getLogger(MetaLabCli.class);

	private void run(String[] args) {

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(RunOption.getOptions(), args);
		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (commandLine.hasOption("help")) {
			printUsage();
			return;
		}

		if (commandLine.hasOption("gui")) {

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Error in creating the GUI", e);
			}

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						MainFrame frame = new MainFrame();
						frame.setVisible(true);
					} catch (Exception e) {
						LOGGER.error("Error in creating the GUI", e);
					}
				}
			});

			return;
		}

		if (commandLine.hasOption("par")) {
			String par = commandLine.getOptionValue("par");
			File parafile = new File(par);
			if (parafile.exists()) {

				MetaParameterV1 parameter = JsonParaHandler.parse(par);

				if (parameter != null) {
					// TODO Auto-generated method stub

					boolean[] steps = parameter.getWorkflow();
					int total = 0;
					for (int i = 0; i < steps.length; i++) {
						if (steps[i]) {
							total++;
						}
					}

					if (total == 0)
						return;

					PrintWriter logWriter = null;
					File logFile = new File(parameter.getParameterDir(), "log.txt");
					try {
						logWriter = new PrintWriter(logFile);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						LOGGER.error("Error in writing log file " + logFile, e);
					}
					logWriter.println(format.format(new Date()) + "\t" + "Task created");

					ArrayList<File> loglist = new ArrayList<File>();
					/**
					 * pre-processing
					 */
					if (steps[0]) {
						MetaDbCreateTaskV1 task = new MetaDbCreateTaskV1(parameter);
						File log = task.runCLI();
						if (!task.isFinished()) {
							logWriter.println(format.format(new Date()) + "\t" + "Task: " + MetaDbCreateTaskV1.taskName
									+ " failed");
							logWriter.close();
							return;
						}
						loglist.add(log);
					}

					/**
					 * peptide identification
					 */
					if (steps[1]) {
						MetaPepIdenTask task = new MetaPepIdenTask(parameter);
						File log = task.runCLI();
						if (!task.isFinished()) {
							logWriter.println(
									format.format(new Date()) + "\t" + "Task: " + MetaPepIdenTask.taskName + " failed");
							logWriter.close();
							return;
						}
						loglist.add(log);
					}

					/**
					 * taxonomy analysis
					 */
					if (steps[2]) {
						MetaLCATask task = new MetaLCATask(parameter);
						File log = task.runCLI();
						if (!task.isFinished()) {
							logWriter.println(
									format.format(new Date()) + "\t" + "Task: " + MetaLCATask.taskName + " failed");
							logWriter.close();
							return;
						}
						loglist.add(log);
					}

					/**
					 * functional analysis
					 */
					if (steps[3]) {

						MetaFunctionTask task = new MetaFunctionTask(parameter);
						File log = task.runCLI();
						if (!task.isFinished()) {
							logWriter.println(format.format(new Date()) + "\t" + "Task: " + MetaFunctionTask.taskName
									+ " failed");
							logWriter.close();
							return;
						}
						loglist.add(log);
					}

					for (File file : loglist) {
						if (file.exists()) {

							boolean finish = false;

							try {

								BufferedReader reader = new BufferedReader(new FileReader(file));
								String line = null;
								while ((line = reader.readLine()) != null) {
									logWriter.println(line);
								}

								reader.close();
								finish = true;

							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in writing log file " + logFile, e);
							} finally {
								if (!finish) {
									logWriter.close();
								}
							}
						}
					}
					logWriter.println(format.format(new Date()) + "\t" + "Task finished");
					logWriter.close();

					for (File file : loglist) {
						if (file.exists()) {
							try {
								FileHandler.deleteFileOrFolder(file);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("Error in deleting temp file " + file, e);
							}
						}
					}

					JsonParaHandler.export(parameter, new File(parameter.getParameterDir(), "MetaLab.json"));

				} else {

					LOGGER.error("Error in parsing parameter file " + par);

					return;
				}
			}
		}
	}

	static class RunOption {

		private static final Options options = new Options();

		static {

			Option help = Option.builder("h").longOpt("help").desc("print this message").build();

			options.addOption(help);

			Option gui = Option.builder("g").longOpt("gui").desc("show the GUI").build();

			options.addOption(gui);

			Option paraPath = Option.builder("p").argName("file").longOpt("par").hasArg()
					.desc("path to the parameter file, which is used to run this program in cmd").build();

			options.addOption(paraPath);

			Option paraPath2 = Option.builder("sep").argName("file").longOpt("sep").hasArg().desc(
					"path to the parameter file, which is used to run this program in cmd, searching raw files separately")
					.build();

			options.addOption(paraPath2);
		};

		public static Options getOptions() {
			return options;
		}

	}

	private void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Metaproteomics data analysis platform\n",
				"\nBoth GUI and cmd running modes are supported, in cmd mode a parameter file is required.\n",
				RunOption.getOptions(), "\nOnline version see http://imetalab.ca/", true);
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		MetaLabCli metalab = new MetaLabCli();
		metalab.run(args);
	}

}
