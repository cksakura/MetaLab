package bmi.med.uOttawa.metalab.task.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import bmi.med.uOttawa.metalab.core.prodb.FastaReader;
import bmi.med.uOttawa.metalab.core.prodb.ProteinItem;
import bmi.med.uOttawa.metalab.dbSearch.dbReducer.DBReducerTask;
import bmi.med.uOttawa.metalab.spectra.cluster.ClusterTask;
import bmi.med.uOttawa.metalab.task.MetaDbCreateTask;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

/**
 * Only for the MaxQuant workflow, only used for HCD-FTMS data
 * @author MEDTECHU0188
 *
 */
public class MetaDbCreateReduceTask extends MetaDbCreateTask {

	private File dbReducerExe;

	public MetaDbCreateReduceTask(MetaParameter metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initial() {
		// TODO Auto-generated method stub
		this.mscp = metaPar.getMscp();
		File dbReducer = new File(((MetaSourcesV2) msv).getDbReducer());
		if (dbReducer.exists()) {
			dbReducerExe = new File(((MetaSourcesV2) msv).getDbReducer());
		}
	}

	@Override
	protected void cluster() {
		// TODO Auto-generated method stub

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

	@Override
	protected File[] clusterSearch() {
		// TODO Auto-generated method stub

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

		String[] mgfPath = new String[mgfs.length];
		for (int i = 0; i < mgfs.length; i++) {
			mgfPath[i] = mgfs[i].getAbsolutePath();
		}

		File dbReducerFasta = new File(dbcFile, "DBReducer.fasta");

		if (dbReducerFasta.exists() && dbReducerFasta.length() > 0) {

			LOGGER.info(taskName + ": refined database already existed in " + dbReducerFasta);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": refined database already existed in "
					+ dbReducerFasta);

		} else {

			LOGGER.info(taskName + ": searching clustered spectra in original database started");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": searching clustered spectra in original database started");

			int totalThreadCount = metaPar.getThreadCount();
			int coreCount = totalThreadCount > mgfs.length ? mgfs.length : totalThreadCount;

			DBReducerTask task = new DBReducerTask(dbReducerExe);
			task.setTotalThread(coreCount);

			task.searchMgf(mgfPath, dbcFile.getAbsolutePath(), originalFasta, MetaConstants.HCD_FTMS,
					((MetaParameterMQ) metaPar).getCsp().getClosedPsmFdr());

			task.run(1, mgfs.length * 48);
		}

		if (dbReducerFasta.exists() && dbReducerFasta.length() > 0) {
			LOGGER.info(taskName + ": searching clustered spectra in original database finished");
			System.out.println(format.format(new Date()) + "\t" + taskName
					+ ": searching clustered spectra in original database finished");
		}

		setProgress(30);

		return new File[] { dbReducerFasta };
	}

	@Override
	protected File[] metaProIQ() {
		// TODO Auto-generated method stub

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

		int totalThreadCount = metaPar.getThreadCount();
		int coreCount = totalThreadCount > mgfs.length ? mgfs.length : totalThreadCount;

		DBReducerTask task = new DBReducerTask(dbReducerExe);
		task.setTotalThread(coreCount);

		for (int i = 0; i < mgfs.length; i++) {

			String name = mgfs[i].getName();
			name = name.substring(0, name.lastIndexOf("."));

			File firstFileI = new File(firstSearch, name);
			if (!firstFileI.exists()) {
				firstFileI.mkdir();
			}

			File firstFasta = new File(firstFileI, "DBReducer.fasta");

			if (firstFasta.exists() && firstFasta.length() > 0) {
				continue;
			} else {
				task.searchMgf(new String[] { mgfs[i].getAbsolutePath() }, firstFileI.getAbsolutePath(), originalFasta,
						MetaConstants.HCD_FTMS, ((MetaParameterMQ) metaPar).getCsp().getClosedPsmFdr());
			}
		}
		task.run(1, mgfs.length * 24);

		LOGGER.info(taskName + ": MetaProIQ first search finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ first search finished");

		LOGGER.info(taskName + ": MetaProIQ first search database generated");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ first search database generated");

		LOGGER.info(taskName + ": MetaProIQ second search started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ second search started");

		File[] secondResults = new File[mgfs.length];
		for (int i = 0; i < mgfs.length; i++) {

			String name = mgfs[i].getName();
			name = name.substring(0, name.lastIndexOf("."));

			File firstFileI = new File(firstSearch, name);
			File secondFileI = new File(secondSearch, name);
			if (!secondFileI.exists()) {
				secondFileI.mkdir();
			}

			File secondFasta = new File(secondFileI, "DBReducer.fasta");

			if (!secondFasta.exists() || secondFasta.length() == 0) {
				File firstFasta = new File(firstFileI, "DBReducer.fasta");
				task.searchMgf(new String[] { mgfs[i].getAbsolutePath() }, secondFileI.getAbsolutePath(),
						firstFasta.getAbsolutePath(), MetaConstants.HCD_FTMS,
						((MetaParameterMQ) metaPar).getCsp().getClosedPsmFdr());
			}

			secondResults[i] = secondFasta;
		}
		task.run(1, mgfs.length * 12);

		LOGGER.info(taskName + ": MetaProIQ second search finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": MetaProIQ second search finished");

		return secondResults;
	}

	@Override
	protected int writeDatabase(boolean cluster) {
		// TODO Auto-generated method stub

		if (cluster) {

			BufferedReader reader = null;
			PrintWriter writer = null;
			int count = 0;
			try {
				reader = new BufferedReader(new FileReader(clusterResults[0]));
				writer = new PrintWriter(ssdbFile);
				String line = null;
				while ((line = reader.readLine()) != null) {
					writer.println(line);
					if (line.startsWith(">")) {
						count++;
					}
				}
				reader.close();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return count;

		} else {
			HashMap<String, ProteinItem> proMap = new HashMap<String, ProteinItem>();
			for (File reduceDb : clusterResults) {
				try {
					FastaReader fr = new FastaReader(reduceDb);
					ProteinItem[] pis = fr.getItems();
					for (int i = 0; i < pis.length; i++) {
						String ref = pis[i].getRef();
						proMap.put(ref, pis[i]);
					}

					PrintWriter writer = new PrintWriter(ssdbFile);
					for (String ref : proMap.keySet()) {
						writer.println(">" + ref);
						writer.println(proMap.get(ref).getSequence());
					}
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return proMap.size();
		}
	}

}
