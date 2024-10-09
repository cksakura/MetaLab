/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.mdb.pep.LCAGetter;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipeptLCAGetter;
import bmi.med.uOttawa.metalab.task.io.MetaAlgorithm;
import bmi.med.uOttawa.metalab.task.io.MetaBiomJsonHandler;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLReader;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class MetaLCATask extends SwingWorker<File, Void> {

	private MetaParameterV1 parameter;
	private int leastPepCount = 3;
	private File logFile;
	private PrintWriter logWriter;

	private int[] progress;
	private JProgressBar progressBar1;
	private JProgressBar progressBar2;

	private boolean finished;

	protected static String taskName = "Taxonomy analysis";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static Logger LOGGER = LogManager.getLogger();

	public MetaLCATask(MetaParameterV1 parameter) {
		this.parameter = parameter;
	}

	public MetaLCATask(MetaParameterV1 parameter, int[] progress, JProgressBar progressBar1,
			JProgressBar progressBar2) {
		this.parameter = parameter;
		this.progress = progress;
		this.progressBar1 = progressBar1;
		this.progressBar2 = progressBar2;
	}

	public File runCLI() {

		this.logFile = new File(parameter.getParameterDir(), "taxon.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file to " + logFile, e);
		}

		logWriter.println(format.format(new Date()) + "\t" + taskName + " started");
		LOGGER.info(taskName + " started");

		File lcaDir = new File(parameter.getResultDir(), "taxonomy");
		if (!lcaDir.exists()) {
			lcaDir.mkdir();
		}

		File[] pepIdenResults = parameter.getPepIdenResults();
		if (pepIdenResults == null) {

			ArrayList<File> txtlist = new ArrayList<File>();
			String[] fileNames = parameter.getRawFiles()[0];
			for (int i = 0; i < fileNames.length; i++) {
				if (fileNames[i].endsWith("txt")) {
					txtlist.add(new File(fileNames[i]));
				}
			}

			if (txtlist.size() == 0) {

				for (int i = 0; i < fileNames.length; i++) {
					if (fileNames[i].endsWith("xml")) {
						txtlist.add(new File(fileNames[i]));
					}
				}

				if (txtlist.size() > 0) {
					parameter.setPepIdenResultType(MetaSearchEngine.xtandem.getId());
					pepIdenResults = txtlist.toArray(new File[txtlist.size()]);
				}

			} else {
				parameter.setPepIdenResultType(MetaSearchEngine.maxquant.getId());
				pepIdenResults = txtlist.toArray(new File[txtlist.size()]);
			}
		}

		if (pepIdenResults == null || pepIdenResults.length == 0) {

			LOGGER.info(taskName + " no input file found");
			LOGGER.info(taskName + " finished");

			logWriter.println(format.format(new Date()) + "\t" + taskName + " no input file found");
			logWriter.println(format.format(new Date()) + "\t" + taskName + " finished");
			logWriter.close();

			finished = true;

			return logFile;
		}

		for (File pepIdenResult : pepIdenResults) {

			String name = pepIdenResult.getName();
			name = name.substring(0, name.lastIndexOf("."));

			if (parameter.isBuildIn()) {

				File taxResult = new File(lcaDir, name + ".builtin.xml");
				if (taxResult.exists()) {
					LOGGER.info("taxonomy analysis result already existed in " + taxResult + ", skip");
					logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis result already existed in "
							+ taxResult + ", skip");
				} else {
					LCAGetter getter;
					boolean finish = false;
					try {
						getter = new LCAGetter(pepIdenResult, taxResult, parameter, logWriter);
						getter.getLCA();
						finish = true;

					} finally {
						if (!finish) {
							logWriter.println(
									format.format(new Date()) + "\t" + "taxonomy analysis by built-in database failed");
							logWriter.close();
						}
					}
				}

				MetaPeptideXMLReader reader = null;
				MetaPeptide[] peps = null;
				Taxon[] taxons = null;
				String[] expNames = null;

				boolean readFinish = false;
				try {
					reader = new MetaPeptideXMLReader(taxResult);
					reader.export(lcaDir, MetaAlgorithm.Builtin, leastPepCount);

					peps = reader.getPeptides();
					taxons = reader.getTaxons();
					expNames = reader.getFileNames();

					readFinish = true;

				} finally {
					if (!readFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing final result in .xlsx format failed");
						logWriter.close();
					}
				}

				File megan = new File(lcaDir, "Biom_" + MetaAlgorithm.Builtin.getName() + ".biom");
				boolean biomFinish = false;
				try {
					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					biomFinish = true;
				} finally {
					if (!biomFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ megan.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File allPepTaxa = new File(lcaDir, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
				boolean allFinish = false;
				try {
					reader.exportPeptideTaxaAll(allPepTaxa);
					allFinish = true;
				} finally {
					if (!allFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ allPepTaxa.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File iMetaLab = new File(lcaDir, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
				boolean treeFinish = false;
				try {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					treeFinish = true;
				} finally {
					if (!treeFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ iMetaLab.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}
			}

			if (parameter.isUnipept()) {
				File taxResult = new File(lcaDir, name + ".unipept.xml");
				if (taxResult.exists()) {
					logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis result already existed in "
							+ taxResult + ", skip");
				} else {
					boolean finish = false;
					UnipeptLCAGetter getter;
					try {
						getter = new UnipeptLCAGetter(pepIdenResult, taxResult, parameter, logWriter);
						getter.getLCA();
						finish = true;
					} finally {
						if (!finish) {
							logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis by Unipept failed");
							logWriter.close();
						}
					}
				}

				boolean readFinish = false;
				MetaPeptideXMLReader reader = null;
				MetaPeptide[] peps = null;
				Taxon[] taxons = null;
				String[] expNames = null;

				try {
					reader = new MetaPeptideXMLReader(taxResult);
					reader.export(lcaDir, MetaAlgorithm.Unipept, leastPepCount);

					peps = reader.getPeptides();
					taxons = reader.getTaxons();
					expNames = reader.getFileNames();

					readFinish = true;
				} finally {
					if (!readFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing final result in .xlsx format failed");
						logWriter.close();
					}
				}

				File megan = new File(lcaDir, "Biom_" + MetaAlgorithm.Unipept.getName() + ".biom");
				boolean biomFinish = false;
				try {

					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					biomFinish = true;

				} finally {
					if (!biomFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ megan.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File iMetaLab = new File(lcaDir, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
				boolean treeFinish = false;
				try {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					treeFinish = true;

				} finally {
					if (!treeFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ iMetaLab.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}
			}
		}

		LOGGER.info(taskName + " finished");

		logWriter.println(format.format(new Date()) + "\t" + taskName + " finished");
		logWriter.close();

		finished = true;

		return logFile;
	}

	@Override
	protected File doInBackground() {
		// TODO Auto-generated method stub

		this.logFile = new File(parameter.getParameterDir(), "taxon.log");
		try {
			this.logWriter = new PrintWriter(logFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		progressBar1.setIndeterminate(true);
		progressBar2.setString(taskName + " processing...");

		logWriter.println(format.format(new Date()) + "\t" + taskName + " started");
		LOGGER.info(taskName + ": started");

		File lcaDir = new File(parameter.getResultDir(), "taxonomy");
		if (!lcaDir.exists()) {
			lcaDir.mkdir();
		}

		File[] pepIdenResults = parameter.getPepIdenResults();
		for (File pepIdenResult : pepIdenResults) {

			String name = pepIdenResult.getName();
			name = name.substring(0, name.lastIndexOf("."));

			if (parameter.isBuildIn()) {

				progressBar1.setString("Taxonomy analysis by built-in database...");

				File taxResult = new File(lcaDir, name + ".builtin.xml");
				if (taxResult.exists()) {
					logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis result already existed in "
							+ taxResult + ", skip");
				} else {
					boolean finish = false;
					LCAGetter getter;
					try {
						getter = new LCAGetter(pepIdenResult, taxResult, parameter, logWriter);
						getter.getLCA();
						finish = true;

					} finally {
						if (!finish) {
							logWriter.println(
									format.format(new Date()) + "\t" + "taxonomy analysis by built-in database failed");
							logWriter.close();
						}
					}
				}

				MetaPeptideXMLReader reader = null;
				MetaPeptide[] peps = null;
				Taxon[] taxons = null;
				String[] expNames = null;

				boolean readFinish = false;
				try {
					reader = new MetaPeptideXMLReader(taxResult);
					reader.export(lcaDir, MetaAlgorithm.Builtin, leastPepCount);

					peps = reader.getPeptides();
					taxons = reader.getTaxons();
					expNames = reader.getFileNames();

					readFinish = true;

				} finally {
					if (!readFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing final result in .xlsx format failed");
						logWriter.close();
					}
				}

				File megan = new File(lcaDir, "Biom_" + MetaAlgorithm.Builtin.getName() + ".biom");
				boolean biomFinish = false;
				try {
					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					biomFinish = true;

				} finally {
					if (!biomFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ megan.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File allPepTaxa = new File(lcaDir, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
				boolean allFinish = false;
				try {
					reader.exportPeptideTaxaAll(allPepTaxa);
					allFinish = true;

				} finally {
					if (!allFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ allPepTaxa.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File iMetaLab = new File(lcaDir, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
				boolean treeFinish = false;
				try {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					treeFinish = true;

				} finally {
					if (!treeFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ iMetaLab.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}
			}

			if (parameter.isUnipept()) {

				progressBar1.setString("Taxonomy analysis by Unipept...");
				progressBar2.setValue((progress[0] + progress[1] / 2));

				File taxResult = new File(lcaDir, name + ".unipept.xml");

				if (taxResult.exists()) {
					logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis result already existed in "
							+ taxResult + ", skip");
				} else {
					boolean finish = false;
					UnipeptLCAGetter getter;
					try {
						getter = new UnipeptLCAGetter(pepIdenResult, taxResult, parameter, logWriter);
						getter.getLCA();
						finish = true;

					} finally {
						if (!finish) {
							logWriter.println(format.format(new Date()) + "\t" + "taxonomy analysis by Unipept failed");
							logWriter.close();
						}
					}
				}

				MetaPeptideXMLReader reader = null;
				MetaPeptide[] peps = null;
				Taxon[] taxons = null;
				String[] expNames = null;
				boolean readFinish = false;
				try {
					reader = new MetaPeptideXMLReader(taxResult);
					reader.export(lcaDir, MetaAlgorithm.Unipept, leastPepCount);

					peps = reader.getPeptides();
					taxons = reader.getTaxons();
					expNames = reader.getFileNames();

					readFinish = true;

				} finally {
					if (!readFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing final result in .xlsx format failed");
						logWriter.close();
					}
				}

				File megan = new File(lcaDir, "Biom_" + MetaAlgorithm.Unipept.getName() + ".biom");
				boolean biomFinish = false;
				try {
					MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
					biomFinish = true;

				} finally {
					if (!biomFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ megan.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}

				File iMetaLab = new File(lcaDir, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
				boolean treeFinish = false;
				try {
					MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
					treeFinish = true;

				} finally {
					if (!treeFinish) {
						logWriter.println(format.format(new Date()) + "\t" + "Writing result to "
								+ iMetaLab.getAbsolutePath() + " failed");
						logWriter.close();
					}
				}
			}
		}

		LOGGER.info(taskName + ": finished");

		progressBar1.setString("");
		progressBar2.setValue(progress[1]);
		progressBar1.setIndeterminate(false);
		progressBar2.setString(taskName + " finished");

		finished = true;

		logWriter.println(format.format(new Date()) + "\t" + taskName + " finished");
		logWriter.close();

		return logFile;
	}

	public void done() {
		logWriter.close();
	}

	public boolean isFinished() {
		return finished;
	}
}
