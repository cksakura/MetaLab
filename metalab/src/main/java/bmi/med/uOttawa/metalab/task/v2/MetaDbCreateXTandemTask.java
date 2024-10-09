/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import bmi.med.uOttawa.metalab.dbSearch.xtandem.TandemFastaGetter;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemTask;
import bmi.med.uOttawa.metalab.spectra.cluster.ClusterTask;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.MetaDbCreateTask;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

public class MetaDbCreateXTandemTask extends MetaDbCreateTask {

	public MetaDbCreateXTandemTask(MetaParameterMQ metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
	}
	
	/**
	 * Clustering strategy
	 * 
	 * @throws Exception
	 */
	@Override
	protected void cluster() {

		bar2.setString(taskName + ": spectra clustering...");

		LOGGER.info(taskName + ": spectra cluster started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra cluster started");

		File clusterDir = new File(dbcFile, "clustered_spectra");
		File clusterMgf = new File(clusterDir, "cluster.mgf");
		clusterDir.mkdir();

		File spectraDir = metaPar.getSpectraFile();
		File[] mgfs = spectraDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("mgf") || arg0.getName().endsWith("MGF"))
					return true;

				return false;
			}
		});

		String[] fileNames = new String[mgfs.length];
		for (int i = 0; i < mgfs.length; i++) {
			fileNames[i] = mgfs[i].getAbsolutePath();
		}

		if (clusterMgf.exists()) {
			LOGGER.info(taskName + ": clustered MS/MS spectra file already existed in " + clusterMgf);
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": clustered MS/MS spectra file already existed in " + clusterMgf);
		} else {

			LOGGER.info(taskName + ": clustering MS/MS spectra to " + clusterMgf + " started");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": clustering MS/MS spectra to "
					+ clusterMgf + " started");

			ClusterTask clusterTask = new ClusterTask(metaPar.getThreadCount(), true,
					((MetaParameterMQ) metaPar).getIsobaricTag(), MetaLabWorkflowType.pFindWorkflow.name());

			clusterTask.runCli(fileNames, clusterDir, clusterMgf);

			LOGGER.info(taskName + ": clustering MS/MS spectra to " + clusterMgf + " finished");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": clustering MS/MS spectra to "
					+ clusterMgf + " finished");
		}

		LOGGER.info(taskName + ": spectra cluster finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": spectra cluster finished");
	}

	/**
	 * Clustering strategy
	 * 
	 * 
	 */
	@Override
	protected File[] clusterSearch() {

		bar2.setString(taskName + ": searching clustered spectra in original database");

		LOGGER.info(taskName + ": searching clustered spectra in original database started");
		System.out.println(format.format(new Date()) + "\t" + taskName
				+ ": searching clustered spectra in original database started");

		File clusterDir = new File(dbcFile, "clustered_spectra");

		File[] mgfs = clusterDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("mgf") || arg0.getName().endsWith("MGF"))
					return true;

				return false;
			}
		});

		File xmlResultDir = new File(dbcFile, "search_result");
		if (!xmlResultDir.exists()) {
			xmlResultDir.mkdir();
		}

		File[] xmls = xmlResultDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("xml") || arg0.getName().endsWith("XML"))
					return true;

				return false;
			}
		});

		if (mgfs.length > 0 && mgfs.length == xmls.length) {

			LOGGER.info(taskName + ": search results already existed in " + xmlResultDir);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": search results already existed in "
					+ xmlResultDir);

		} else {

			LOGGER.info(taskName + ": searching clustered spectra in original database started");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": searching clustered spectra in original database started");

			XTandemTask.run((MetaParameterMQ) metaPar, (MetaSourcesV2) msv, xmlResultDir, mgfs);

			xmls = xmlResultDir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					// TODO Auto-generated method stub
					if (arg0.getName().endsWith("xml") || arg0.getName().endsWith("XML"))
						return true;

					return false;
				}
			});
		}

		if (mgfs.length > 0 && mgfs.length == xmls.length) {
			LOGGER.info(taskName + ": searching clustered spectra in original database finished");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": searching clustered spectra in original database finished");
		}

		setProgress(30);

		return xmls;
	}

	@Override
	protected File[] metaProIQ() {

		bar2.setString(taskName + ": searching clustered spectra in original database");

		LOGGER.info(taskName + ": MetaProIQ started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ started");

		File spectraDir = metaPar.getSpectraFile();
		File[] mgfs = spectraDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("mgf") || arg0.getName().endsWith("MGF"))
					return true;

				return false;
			}
		});

		File firstSearch = new File(dbcFile, "first_search");
		if (!firstSearch.exists()) {
			firstSearch.mkdir();
		}

		File secondSearch = new File(dbcFile, "second_search");
		if (!secondSearch.exists()) {
			secondSearch.mkdir();
		}

		LOGGER.info(taskName + ": MetaProIQ first search started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ first search started");

		for (int i = 0; i < mgfs.length; i++) {

			String name = mgfs[i].getName();
			name = name.substring(0, name.lastIndexOf("."));

			File firstFasta = new File(firstSearch, "first." + name + ".fasta");

			if (firstFasta.exists() && firstFasta.length() > 0) {
				continue;
			} else {
				XTandemTask.run((MetaParameterMQ) metaPar, (MetaSourcesV2) msv, firstSearch, new File[] { mgfs[i] });
			}
		}

		LOGGER.info(taskName + ": MetaProIQ first search finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ first search finished");

		File[] firstResults = firstSearch.listFiles();
		for (int i = 0; i < firstResults.length; i++) {
			if (firstResults[i].getName().endsWith("xml")) {
				String name = firstResults[i].getName();
				name = name.substring(0, name.length() - 26);
				File output = new File(firstSearch, "first." + name + ".fasta");

				if (!output.exists() || output.length() == 0) {
					TandemFastaGetter getter = new TandemFastaGetter(firstResults[i]);
					TandemFastaGetter.writeWithDecoy(getter.parseProtein(0, 0), metaPar.getCurrentDb(),
							output.getAbsolutePath());
				}
			}
		}

		LOGGER.info(taskName + ": MetaProIQ first search database generated");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ first search database generated");

		LOGGER.info(taskName + ": MetaProIQ second search started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ second search started");

		for (int i = 0; i < mgfs.length; i++) {

			String name = mgfs[i].getName();
			name = name.substring(0, name.lastIndexOf("."));

			File secondFasta = new File(secondSearch, name + ".fasta");

			if (secondFasta.exists() && secondFasta.length() > 0) {
				continue;
			} else {

				File firstFasta = new File(firstSearch, "first." + name + ".fasta");
				metaPar.setCurrentDb(firstFasta.getAbsolutePath());

				XTandemTask.run((MetaParameterMQ) metaPar, (MetaSourcesV2) msv, secondSearch, new File[] { mgfs[i] });
			}
		}

		File[] secondResults = secondSearch.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("xml") || arg0.getName().endsWith("XML"))
					return true;

				return false;
			}
		});

		LOGGER.info(taskName + ": MetaProIQ second search finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ second search finished");

		return secondResults;
	}

	@Override
	protected int writeDatabase(boolean cluster) {
		// TODO Auto-generated method stub
		if (cluster) {
			return TandemFastaGetter.batchWrite(clusterResults, originalFasta, ssdbFile.getAbsolutePath(), 0,
					mscp.getXtandemEvalue());
		} else {
			return TandemFastaGetter.batchWrite(clusterResults, originalFasta, ssdbFile.getAbsolutePath(),
					mscp.getXtandemFDR(), 0);
		}
	}


}
