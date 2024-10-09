/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.msFragger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.prodb.FastaManager;
import bmi.med.uOttawa.metalab.dbSearch.open.task.OpenSearchValidationTask;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class MSFraggerTask extends MetaAbstractTask {

	private String jar = "Resources\\MSFragger\\MSFragger.jar";

	private File params;
	private int memory;
	private Appendable log;

	private File input;
	private File output;

	private static String taskName = "Database search by MSFragger";
	private static final Logger LOGGER = LogManager.getLogger(MSFraggerTask.class);

	public MSFraggerTask(File params, Appendable log) {
		this.params = params;
		this.memory = 8;
		this.log = log;
	}

	public MSFraggerTask(File params, File input, File output, Appendable log) {
		this.params = params;
		this.memory = 8;
		this.input = input;
		this.output = output;
		this.log = log;
	}

	public MSFraggerTask(File params, int memory, Appendable log) {
		this.params = params;
		this.memory = memory;
		this.log = log;
	}

	private MetaParameterMQ metaPar;
	private MetaSourcesV2 advPar;

	/**
	 * Constructor for the swingworker
	 * 
	 * @param parameter
	 */
	public MSFraggerTask(MetaParameterMQ metaPar, MetaSourcesV2 advPar) {
		this.metaPar = metaPar;
		this.advPar = advPar;
	}

	/**
	 * Constructor for the swingworker
	 * 
	 * @param parameter
	 */
	public MSFraggerTask(MetaParameterMQ metaPar, MetaSourcesV2 advPar, JProgressBar bar1, JProgressBar bar2,
			OpenSearchValidationTask validTask) {
		super(metaPar, advPar, bar1, bar2, validTask);
		this.metaPar = metaPar;
		this.advPar = advPar;
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	public void setInput(File input) {
		this.input = input;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		File msfraggerJar = new File(advPar.getMsfragger());
		if (metaPar.isOpenSearch()) {
			this.params = new File(msfraggerJar.getParent(), "open_fragger.params");
		} else {
			this.params = new File(msfraggerJar.getParent(), "closed_fragger.params");
		}

		if (!this.params.exists()) {

			File paramBatFile = new File(msfraggerJar.getParent(), "generateParams.bat");
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(paramBatFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": generate parameter file failed", e);
				System.err.println(format.format(new Date()) + "\t" + taskName + ": generate parameter file failed");
			}

			writer.println(msfraggerJar.getAbsolutePath().split("\\\\")[0]);
			writer.println("cd " + msfraggerJar.getParent());

			String cmd;

			if (metaPar.isOpenSearch()) {
				cmd = "java -jar " + msfraggerJar.getName() + " --config open";
			} else {
				cmd = "java -jar " + msfraggerJar.getName() + " --config closed";
			}

			writer.println(cmd);

			writer.close();

			Runtime run = Runtime.getRuntime();
			try {

				Process p = run.exec(paramBatFile.getAbsolutePath());

				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;

				while ((lineStr = inBr.readLine()) != null) {
					System.out.println(lineStr);
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				LOGGER.error(taskName + ": error in generation of parameter file", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in generation of parameter file");
			}
		}

		if (!this.params.exists()) {
			LOGGER.error(taskName + ": error in generation of parameter file");
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in generation of parameter file");

			return false;
		}

		File resultFile = metaPar.getDbSearchResultFile();
		File pepxmlDir = new File(resultFile, "pepxml");

		if (!pepxmlDir.exists()) {
			pepxmlDir.mkdir();
		}

		StringBuilder msmssb = new StringBuilder();

		boolean search = false;

		String[] rawNames = metaPar.getMetadata().getRawFiles();

		for (int i = 0; i < rawNames.length; i++) {
			String name = rawNames[i].substring(rawNames[i].lastIndexOf("\\"), rawNames[i].lastIndexOf("."));
			File pepxmli = new File(pepxmlDir, name + ".pepXML");
			
			if (pepxmli.exists()) {
				LOGGER.info(taskName + ": open search result for " + rawNames[i] + " already existed in " + pepxmlDir
						+ ", go to next step");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": open search result for "
						+ rawNames[i] + " already existed in " + pepxmlDir + ", go to next step");
			} else {

				msmssb.append(rawNames[i]).append(" ");
				search = true;
			}
		}

		if (search && msmssb.length() > 0) {

			msmssb.deleteCharAt(msmssb.length() - 1);

			if (metaPar.getCurrentDb().equals(metaPar.getMicroDb())) {

				File currentDbFile = new File(metaPar.getCurrentDb());
				File parent = new File(metaPar.getResult());
				String dbname = currentDbFile.getName();

				File finalDb = new File(parent, "final." + dbname);
				if (!finalDb.exists()) {
					try {
						FastaManager.addDecoy(metaPar.getCurrentDb(), finalDb.getAbsolutePath());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in creating target-decoy database in " + finalDb);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in creating target-decoy database in " + finalDb);
					}
				}

				metaPar.setCurrentDb(finalDb.getAbsolutePath());

			} else {
				File currentDbFile = new File(metaPar.getCurrentDb());
				File parent = currentDbFile.getParentFile();
				String dbname = currentDbFile.getName();

				File finalDb = new File(parent, "final." + dbname);
				if (!finalDb.exists()) {
					try {
						FastaManager.addDecoy(metaPar.getCurrentDb(), finalDb.getAbsolutePath());
					} catch (IOException e) {
						LOGGER.error(taskName + ": error in creating target-decoy database in " + finalDb);
						System.err.println(format.format(new Date()) + "\t" + taskName
								+ ": error in creating target-decoy database in " + finalDb);
					}
				}

				metaPar.setCurrentDb(finalDb.getAbsolutePath());
			}

			int memory = 8;

			int maxMemory = (int) (Runtime.getRuntime().maxMemory() * 1E-9);

			if (maxMemory >= 15 && maxMemory < 30) {
				memory = 12;
			} else if (maxMemory >= 30 && maxMemory < 60) {
				memory = 20;
			} else if (maxMemory >= 60) {
				memory = 32;
			}

			MSFraggerParameter parameter = MSFraggerParameter.parse(params);

			File para = parameter.config(metaPar, advPar);
			if (para == null || !para.exists()) {

				LOGGER.error(taskName + ": error in configuring the MSFragger parameter file");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": error in configuring the MSFragger parameter file");

				return false;
			}

			Runtime run = Runtime.getRuntime();
			try {
				String cmd = "java -Xmx" + memory + "G -jar " + msfraggerJar.getAbsolutePath() + " "
						+ para.getAbsolutePath() + " " + msmssb;

				Process p = run.exec(cmd);
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;

				while ((lineStr = inBr.readLine()) != null) {
					System.out.println(lineStr);
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				LOGGER.error(taskName + ": error in database search by MSFragger", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in database search by MSFragger");
			}

			for (int i = 0; i < rawNames.length; i++) {

				File pepxml = new File(rawNames[i].substring(0, rawNames[i].lastIndexOf(".")) + ".pepXML");

				BufferedReader reader = new BufferedReader(new FileReader(pepxml));
				PrintWriter writer = new PrintWriter(new File(pepxmlDir, pepxml.getName()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("<spectrum_query")) {

						int assumed_charge = line.indexOf("assumed_charge=");
						int spectrum = line.indexOf("spectrum=");
						int end = line.indexOf("end_scan=");

						String spstring = line.substring(spectrum, end);
						int file = spstring.indexOf("File:");

						if (file > 0) {
							String[] cs = spstring.substring(0, file).split("\\.");
							StringBuilder sb = new StringBuilder();

							sb.append("<spectrum_query start_scan=\"").append(cs[cs.length - 2]).append("\" ");
							sb.append(line.substring(assumed_charge, spectrum + file - 1)).append("\" ");
							sb.append(line.substring(end));

							writer.println(sb);
						} else {
							writer.println(line);
						}

					} else if (line.startsWith("<search_hit")) {
						String reline = line.substring(1, line.length() - 1);
						if (reline.indexOf("<") > -1) {
							reline = reline.replaceAll("<", "_");
							reline = reline.replaceAll(">", "_");

						} else if (reline.indexOf("&") > -1) {
							reline = reline.replaceAll("&", "_");
						}

						writer.println("<" + reline + ">");

					} else {
						writer.println(line);
					}
				}
				reader.close();
				writer.close();

//				FileUtils.deleteQuietly(pepxml);
			}
		}

		LOGGER.info(taskName + ": MSFragger searching is finished");

		File[] finalPepxmls = pepxmlDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				if (arg1.endsWith("pepXML"))
					return true;
				return false;
			}

		});

		setProgress(50);

		if (finalPepxmls.length == rawNames.length) {
			return true;
		} else {
			return false;
		}
	}

	public void runSingleTask(String params) throws IOException {

		// TODO Auto-generated method stub

		if (!input.exists()) {
			LOGGER.error(taskName + ": Input spectra file was not found in " + input);
			log.append(format.format(new Date()) + "\t" + taskName + ": Input spectra file was not found in " + input);
		}

		if (output.exists()) {
			LOGGER.info(taskName + ": open search result already existed in " + output + ", skip");
			log.append(format.format(new Date()) + "\t" + taskName + ": peptide search result already existed in "
					+ output + ", skip\n");
		} else {
			String name = input.getName();

			if (name.endsWith("mzXML") || name.endsWith("MZXML") || name.endsWith("mgf") || name.endsWith("MGF")) {

				LOGGER.info(taskName + ": searching file " + name);
				Runtime run = Runtime.getRuntime();
				try {
					String cmd = "java -Xmx" + memory + "G -jar " + jar + " " + params + " " + input;
					Process p = run.exec(cmd);
					BufferedInputStream in = new BufferedInputStream(p.getInputStream());
					BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
					String lineStr;
					while ((lineStr = inBr.readLine()) != null)
						System.out.println(lineStr);
					if (p.waitFor() != 0) {
						if (p.exitValue() == 1)
							System.err.println("false");
					}
					inBr.close();
					in.close();
				} catch (Exception e) {
					LOGGER.error(e);
				}

				String pepxml = name.substring(0, name.lastIndexOf(".")) + "pepXML";
				File result = new File(input.getParentFile(), pepxml);

				if (result.exists()) {

					if (!result.equals(output)) {

						BufferedReader reader = new BufferedReader(new FileReader(result));
						PrintWriter writer = new PrintWriter(output);
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith("<spectrum_query")) {

								int assumed_charge = line.indexOf("assumed_charge=");
								int spectrum = line.indexOf("spectrum=");
								int end = line.indexOf("end_scan=");

								String spstring = line.substring(spectrum, end);
								int file = spstring.indexOf("File:");

								if (file > 0) {
									String[] cs = spstring.substring(0, file).split("\\.");
									StringBuilder sb = new StringBuilder();

									sb.append("<spectrum_query start_scan=\"").append(cs[cs.length - 2]).append("\" ");
									sb.append(line.substring(assumed_charge, spectrum + file - 1)).append("\" ");
									sb.append(line.substring(end));

									writer.println(sb);
								} else {
									writer.println(line);
								}

							} else if (line.startsWith("<search_hit")) {
								String reline = line.substring(1, line.length() - 1);
								if (reline.indexOf("<") > -1) {
									reline = reline.replaceAll("<", "_");
									reline = reline.replaceAll(">", "_");

								} else if (reline.indexOf("&") > -1) {
									reline = reline.replaceAll("&", "_");
								}

								writer.println("<" + reline + ">");

							} else {
								writer.println(line);
							}
						}
						reader.close();
						writer.close();
					}

					LOGGER.info("MSFragger searching for " + output + " finished");
					log.append(format.format(new Date()) + "\t" + "MSFragger searching for " + output + " finished"
							+ "\n");

					FileUtils.deleteQuietly(result);

				} else {
					LOGGER.info("MSFragger searching for " + output + " failed");
					log.append(
							format.format(new Date()) + "\t" + "MSFragger searching for " + output + " failed" + "\n");
				}

			} else {
				LOGGER.error(taskName + ": spectra file format was not supported");
				log.append(format.format(new Date()) + "\t" + taskName + ": spectra file format was not supported");
			}
		}
	}

	@SuppressWarnings("unused")
	private static void runSingleMSMS(String input, String para, int spcount, String jar) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String file = input + "." + spcount + ".mgf";
		PrintWriter writer = new PrintWriter(file);
		String line = null;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0) {
				writer.print(line);
				writer.print("\n");
			}

			if (line.startsWith("END")) {
				count++;
				if (count == spcount) {
					break;
				}
			}
		}
		reader.close();
		writer.close();

		Runtime run = Runtime.getRuntime();
		try {
			String cmd = "java -Xmx" + 10 + "G -jar " + jar + " " + para + " " + file;
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stu-b
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}

}
