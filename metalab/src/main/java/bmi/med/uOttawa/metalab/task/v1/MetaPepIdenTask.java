/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantParaHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantTask;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MetaPepIdenTask extends SwingWorker<File, Void> {

	private MetaParameterV1 parameter;
	private int[] progress;
	private int methodId = 0;

	private JProgressBar progressBar1;
	private JProgressBar progressBar2;

	private File logFile;
	private PrintWriter logWriter;
	
	private boolean finished;
	
	protected static String taskName = "Peptide identification";
	
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	
	private static Logger LOGGER = LogManager.getLogger();
	
	public MetaPepIdenTask(MetaParameterV1 parameter) {
		this.parameter = parameter;
	}
	
	public MetaPepIdenTask(MetaParameterV1 parameter, int[] progress, JProgressBar progressBar1, JProgressBar progressBar2) {
		this.parameter = parameter;
		this.progress = progress;
		this.progressBar1 = progressBar1;
		this.progressBar2 = progressBar2;
	}

	public File runCLI() {
		
		this.logFile = new File(parameter.getParameterDir(), "pep_iden.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logFile, e);
		}

		File pepIdenPar = new File(parameter.getParameterDir(), "pep_iden_par.xml");
		Path source = Paths.get(parameter.getMaxQuantPar());
		Path target = Paths.get(pepIdenPar.getAbsolutePath());
		boolean parFinish = false;
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			MaxquantParaHandler.config(parameter, pepIdenPar.getAbsolutePath());
			parFinish = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying parameter file from " + source + " to " + target, e);
		} finally {
			if (!parFinish) {
				logWriter.println(format.format(new Date()) + "\t" + "Coping " + source + " to " + target + " failed");
				logWriter.close();
			}
		}

		LOGGER.info(taskName + ": searching specific database started...");
		logWriter.println(format.format(new Date()) + "\t" + "Searching specific database started");

		File mq_tex_dir = new File(parameter.getResultDir(), "pep_iden");
		if (!mq_tex_dir.exists()) {
			mq_tex_dir.mkdir();
		}

		File pepIdenResult = new File(mq_tex_dir, "peptides.txt");
		if (pepIdenResult.exists()) {
			LOGGER.info("peptide search result already existed in " + pepIdenResult + ", skip");
			logWriter.println(format.format(new Date()) + "\t" + "peptide search result already existed in "
					+ pepIdenResult + ", skip");
		} else {

			MaxquantTask maxquantTask = new MaxquantTask(logWriter);
			boolean maxFinish = false;
			try {
				maxquantTask.run(parameter.getMaxQuantCmd(), pepIdenPar.getAbsolutePath());
				maxFinish = true;
			} finally {
				if (!maxFinish) {
					LOGGER.error("MaxQuant searching failed");
					logWriter.println(format.format(new Date()) + "\t" + "MaxQuant searching failed");
					logWriter.close();
				}
			}

			String[] raws = parameter.getRawFiles()[0];
			File txtDir = new File((new File(raws[0])).getParent() + "\\combined\\txt");
			File[] txts = txtDir.listFiles();

			for (int i = 0; i < txts.length; i++) {
				Path sourcei = Paths.get(txts[i].getAbsolutePath());
				Path targeti = Paths.get(mq_tex_dir.getAbsolutePath() + "\\" + txts[i].getName());
				boolean moveFinish = false;
				try {
					Files.move(sourcei, targeti, StandardCopyOption.REPLACE_EXISTING);
					moveFinish = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in copying result file from " + sourcei + " to " + targeti, e);
				} finally {
					if (!moveFinish) {
						logWriter.println(
								format.format(new Date()) + "\t" + "Moving " + sourcei + " to " + targeti + " failed");
						logWriter.close();
					}
				}
			}
			System.gc();

			logWriter.println(format.format(new Date()) + "\t" + "Search result moved to " + mq_tex_dir);

			File rawDir = (new File(raws[0])).getParentFile();
			File combined = new File(rawDir, "combined");
			if (combined.exists()) {
				FileUtils.deleteQuietly(combined);
			}

			for (String raw : raws) {
				String name = (new File(raw)).getName();
				name = name.substring(0, name.lastIndexOf("."));
				File mqfile = new File(rawDir, name);
				if (mqfile.exists()) {
					FileUtils.deleteQuietly(mqfile);
				}

				File indexFile = new File(rawDir, name + ".index");
				if (indexFile.exists()) {
					FileUtils.deleteQuietly(indexFile);
				}
			}
		}

		File[] pepIdenResults = new File[] { pepIdenResult };
		parameter.setPepIdenResults(pepIdenResults);
		parameter.setPepIdenResultType(MetaSearchEngine.maxquant.getId());
		
		File proIdenResult = new File(mq_tex_dir, "proteinGroups.txt");
		parameter.setProteinIdenResult(proIdenResult);

		LOGGER.info(taskName + ": searching specific database finished...");

		logWriter.println(format.format(new Date()) + "\t" + "Searching specific database finished");
		logWriter.close();

		if (pepIdenResult.exists()) {
			finished = true;
		}

		return logFile;
	}

	@Override
	protected File doInBackground() {
		// TODO Auto-generated method stub

		progressBar1.setIndeterminate(true);
		progressBar1.setString("Searching sample-specific database...");
		progressBar2.setString(taskName + " processing...");
		
		this.logFile = new File(parameter.getParameterDir(), "pep_iden.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logFile, e);
		}

		File pepIdenPar = new File(parameter.getParameterDir(), "pep_iden_par.xml");
		Path source = Paths.get(parameter.getMaxQuantPar());
		Path target = Paths.get(pepIdenPar.getAbsolutePath());
		boolean parFinish = false;
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			MaxquantParaHandler.config(parameter, pepIdenPar.getAbsolutePath());
			parFinish = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in copying parameter file from " + source + " to " + target, e);
		} finally {
			if (!parFinish) {
				logWriter.println(format.format(new Date()) + "\t" + "Coping " + source + " to " + target + " failed");
				logWriter.close();
			}
		}

		logWriter.println(format.format(new Date()) + "\t" + "Search parameter copied to " + pepIdenPar);

		LOGGER.info(taskName + ": searching specific database started...");
		logWriter.println(format.format(new Date()) + "\t" + "Searching specific database started");

		File mq_tex_dir = new File(parameter.getResultDir(), "pep_iden");
		if (!mq_tex_dir.exists()) {
			mq_tex_dir.mkdir();
		}

		File pepIdenResult = new File(mq_tex_dir, "peptides.txt");
		if (pepIdenResult.exists()) {

			LOGGER.info("peptide search result already existed in " + pepIdenResult + ", skip");
			logWriter.println(format.format(new Date()) + "\t" + "peptide search result already existed in "
					+ pepIdenResult + ", skip");

		} else {

			MaxquantTask maxquantTask = new MaxquantTask(logWriter);
			boolean maxFinish = false;
			try {
				maxquantTask.run(parameter.getMaxQuantCmd(), pepIdenPar.getAbsolutePath());
				maxFinish = true;
			} finally {
				if (!maxFinish) {
					LOGGER.error("MaxQuant searching failed");
					logWriter.println(format.format(new Date()) + "\t" + "MaxQuant searching " + " failed");
					logWriter.close();
				}
			}

			String[] raws = parameter.getRawFiles()[0];
			File txtDir = new File((new File(raws[0])).getParent() + "\\combined\\txt");
			File[] txts = txtDir.listFiles();

			for (int i = 0; i < txts.length; i++) {
				Path sourcei = Paths.get(txts[i].getAbsolutePath());
				Path targeti = Paths.get(mq_tex_dir.getAbsolutePath() + "\\" + txts[i].getName());
				boolean movefinish = false;
				try {
					Files.move(sourcei, targeti, StandardCopyOption.REPLACE_EXISTING);
					movefinish = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.error("Error in moving result files from " + sourcei + " to " + targeti, e);
				} finally {
					if (!movefinish) {
						logWriter.println(
								format.format(new Date()) + "\t" + "Moving " + sourcei + " to " + targeti + " failed");
						logWriter.close();
					}
				}
			}
			System.gc();

			logWriter.println(format.format(new Date()) + "\t" + "Search result moved to " + mq_tex_dir);

			File rawDir = (new File(raws[0])).getParentFile();
			File combined = new File(rawDir, "combined");
			if (combined.exists()) {
				FileUtils.deleteQuietly(combined);
			}

			for (String raw : raws) {
				String name = (new File(raw)).getName();
				name = name.substring(0, name.lastIndexOf("."));
				File mqfile = new File(rawDir, name);
				if (mqfile.exists()) {
					FileUtils.deleteQuietly(mqfile);
				}

				File indexFile = new File(rawDir, name + ".index");
				if (indexFile.exists()) {
					FileUtils.deleteQuietly(indexFile);
				}
			}
		}

		File[] pepIdenResults = new File[] { pepIdenResult };
		parameter.setPepIdenResults(pepIdenResults);
		parameter.setPepIdenResultType(MetaSearchEngine.maxquant.getId());

		File proIdenResult = new File(mq_tex_dir, "proteinGroups.txt");
		parameter.setProteinIdenResult(proIdenResult);

		LOGGER.info(taskName + ": searching specific database finished...");
		logWriter.println(format.format(new Date()) + "\t" + "Searching specific database finished");

		logWriter.close();

		progressBar1.setIndeterminate(false);
		progressBar2.setValue(progress[1]);

		if (pepIdenResult.exists()) {
			finished = true;
		}

		return logFile;
	}
	
	public void done() {
		this.logWriter.close();
	}

	public boolean isFinished() {
		return finished;
	}
}
