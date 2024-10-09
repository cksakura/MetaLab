/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.swing.JProgressBar;

import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerTask;
import bmi.med.uOttawa.metalab.dbSearch.open.task.OpenExportTask;
import bmi.med.uOttawa.metalab.dbSearch.open.task.OpenQuanTask;
import bmi.med.uOttawa.metalab.dbSearch.open.task.OpenSearchValidationTask;
import bmi.med.uOttawa.metalab.task.MetaDbCreateTask;
import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParaIOMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * Four types of tasks were supported: 1. MaxQuant search 2. MSFragger search 3.
 * taxonomy analysis 4. functional annotation
 * 
 * @author Kai Cheng
 *
 */
public class MetaLabTaskV2 extends MetaLabTask {

	private MetaParameterMQ metaPar;
	private MetaSourcesV2 advPar;

	private MetaSpectraConvertTaskV2 spcTask1;
	private MetaDbCreateTask dbcTask1;
	private MetaTaxaTaskV2 taxaTask;
	private MetaFuncTaskV2 funcTask;
	private MetaMQSearchTask closedTask;

	private OpenExportTask exportTask;
	private OpenQuanTask quanTask;
	private OpenSearchValidationTask openValidTask;
	private MSFraggerTask openTask;
	private MetaDbCreateXTandemTask dbcTask2;
	private MetaSpectraConvertTaskV2 spcTask2;

	public MetaLabTaskV2(MetaParameterMQ metaPar, MetaSourcesV2 advPar, JProgressBar bar1, JProgressBar bar2) {
		super(metaPar, advPar, bar1, bar2);
		this.metaPar = metaPar;
		this.advPar = advPar;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		this.bar1.setStringPainted(true);
		this.bar2.setStringPainted(true);
		this.bar2.setIndeterminate(true);

		File result = new File(metaPar.getResult());
		if (!result.exists()) {
			result.mkdirs();
		}
		MetaParaIOMQ.export(metaPar, new File(result, "parameter.json"));

		MetaLabWorkflowType tt = metaPar.getWorkflowType();

		if (tt != null) {

			switch (tt) {

			case MaxQuantWorkflow: {

				File closedResult = new File(result, "maxquant_search");

				metaPar.setDbSearchResultFile(closedResult);

				boolean useDBReducer = false;
				File dbReducer = new File(advPar.getDbReducer());
				if (dbReducer.exists() && metaPar.getMs2ScanMode().equals(MetaConstants.FTMS)) {
					useDBReducer = true;
				}

				if (metaPar.isMetaWorkflow()) {

					funcTask = new MetaFuncTaskV2(metaPar, advPar, bar1, bar2, null);

					taxaTask = new MetaTaxaTaskV2(metaPar, advPar, bar1, bar2, funcTask);

					closedTask = new MetaMQSearchTask(metaPar, advPar, bar1, bar2, taxaTask);

					if (metaPar.getMscp().isSsdb()) {

						if (useDBReducer) {
							dbcTask1 = new MetaDbCreateReduceTask(metaPar, advPar, bar1, bar2, closedTask);
						} else {
							dbcTask1 = new MetaDbCreateXTandemTask(metaPar, advPar, bar1, bar2, closedTask);
						}

						spcTask1 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, dbcTask1);

						spcTask1.execute();

						if (!spcTask1.get()) {
							return false;
						}

						if (!dbcTask1.get()) {
							return false;
						}

					} else {
						spcTask1 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, closedTask);

						spcTask1.execute();

						if (!spcTask1.get()) {
							return false;
						}
					}

					if (!closedTask.get()) {
						return false;
					}

					if (!taxaTask.get()) {
						return false;
					}

					return funcTask.get();

				} else {
					closedTask = new MetaMQSearchTask(metaPar, advPar, bar1, bar2, null);

					if (metaPar.getMscp().isSsdb()) {
						if (useDBReducer) {
							dbcTask1 = new MetaDbCreateReduceTask(metaPar, advPar, bar1, bar2, closedTask);
						} else {
							dbcTask1 = new MetaDbCreateXTandemTask(metaPar, advPar, bar1, bar2, closedTask);
						}
						spcTask1 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, dbcTask1);
						spcTask1.execute();

						if (!spcTask1.get()) {
							return false;
						}

						if (!dbcTask1.get()) {
							return false;
						}

					} else {
						spcTask1 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, closedTask);

						spcTask1.execute();

						if (!spcTask1.get()) {
							return false;
						}
					}

					return closedTask.get();
				}
			}

			case MSFraggerWorkflow: {

				File resultFile = null;
				if (metaPar.isOpenSearch()) {
					resultFile = new File(result, "msfragger_open_search");
				} else {
					resultFile = new File(result, "msfragger_closed_search");
				}

				if (!resultFile.exists()) {
					resultFile.mkdirs();
				}
				metaPar.setDbSearchResultFile(resultFile);

				exportTask = new OpenExportTask(metaPar, advPar, bar1, bar2, null);

				quanTask = new OpenQuanTask(metaPar, advPar, bar1, bar2, exportTask);

				openValidTask = new OpenSearchValidationTask(metaPar, advPar, bar1, bar2, quanTask);

				openTask = new MSFraggerTask(metaPar, advPar, bar1, bar2, openValidTask);

				if (metaPar.getMscp().isSsdb()) {

					dbcTask2 = new MetaDbCreateXTandemTask(metaPar, advPar, bar1, bar2, openTask);

					spcTask2 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, dbcTask2);

					spcTask2.execute();

					if (!spcTask2.get()) {
						return false;
					}

					if (!dbcTask2.get()) {
						return false;
					}

				} else {

					spcTask2 = new MetaSpectraConvertTaskV2(metaPar, advPar, bar1, bar2, openTask);

					spcTask2.execute();

					if (!spcTask2.get()) {
						return false;
					}
				}

				if (!openTask.get()) {
					return false;
				}

				if (!openValidTask.get()) {
					return false;
				}

				if (!quanTask.get()) {
					return false;
				}

				return exportTask.get();
			}

			case TaxonomyAnalysis: {

				File reportDir = new File(result, "report");
				if (!reportDir.exists()) {
					reportDir.mkdirs();
				}

				MetaReportCopyTask.copy(reportDir, MetaParaIOMQ.version);
				
				metaPar.setReportDir(reportDir);
				
				taxaTask = new MetaTaxaTaskV2(metaPar, advPar, bar1, bar2, null);
				taxaTask.execute();

				return taxaTask.get();

			}

			case FunctionalAnnotation: {

				File reportDir = new File(result, "report");
				if (!reportDir.exists()) {
					reportDir.mkdirs();
				}

				MetaReportCopyTask.copy(reportDir, MetaParaIOMQ.version);
				
				metaPar.setReportDir(reportDir);
				
				funcTask = new MetaFuncTaskV2(metaPar, advPar, bar1, bar2, null);
				funcTask.execute();

				return funcTask.get();
			}
			default: {
				return false;
			}
			}
		} else {
			return false;
		}

	}

	@Override
	public void forceStop() {
		// TODO Auto-generated method stub

		this.cancel(true);

		if (this.spcTask1 != null && !this.spcTask1.isDone()) {
			this.spcTask1.cancel(true);
		}

		if (this.dbcTask1 != null && !this.dbcTask1.isDone()) {
			this.dbcTask1.cancel(true);
		}

		if (this.closedTask != null && !this.closedTask.isDone()) {
			this.closedTask.cancel(true);
		}

		if (this.exportTask != null && !this.exportTask.isDone()) {
			this.exportTask.cancel(true);
		}

		if (this.quanTask != null && !this.quanTask.isDone()) {
			this.quanTask.cancel(true);
		}

		if (this.spcTask2 != null && !this.spcTask2.isDone()) {
			this.spcTask2.cancel(true);
		}

		if (this.dbcTask2 != null && !this.dbcTask2.isDone()) {
			this.dbcTask2.cancel(true);
		}

		if (this.openTask != null && !this.openTask.isDone()) {
			this.openTask.cancel(true);
		}

		if (this.openValidTask != null && !this.openValidTask.isDone()) {
			this.openValidTask.cancel(true);
		}

		if (this.taxaTask != null && !this.taxaTask.isDone()) {
			this.taxaTask.cancel(true);
		}

		if (this.funcTask != null && !this.funcTask.isDone()) {
			this.funcTask.cancel(true);
		}

		MetaData metadata = metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();

		HashSet<String> rawSet = new HashSet<String>();
		for (int i = 0; i < rawFiles.length; i++) {
			rawSet.add((new File(rawFiles[i])).getName());
		}

		String resultDir = metaPar.getResult();
		File spectraFile = new File(resultDir, "spectra");
		File[] mgfFiles = spectraFile.listFiles();
		for (int i = 0; i < mgfFiles.length; i++) {
			String name = mgfFiles[i].getName();
			if (name.endsWith("mgf")) {
				name = name.substring(0, name.length() - "mgf".length()) + "raw";
				if (rawSet.contains(name)) {
					rawSet.remove(name);
				}
			}
		}

		if (rawSet.size() > 0) {
			try {
				ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "ProteomicsTools.exe");
				Process p = pb.start();
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

			return;
		}

		File dbcFile = null;
		if (metaPar.getMscp().isCluster()) {
			dbcFile = new File(metaPar.getResult(), "db_generation_msCluster");
		} else {
			dbcFile = new File(metaPar.getResult(), "db_generation_metaProIq");
		}
		if (dbcFile == null || !dbcFile.exists()) {
			return;
		} else {
			File ssdbFile = new File(dbcFile, "sample_specific_db.fasta");
			if (!ssdbFile.exists()) {
				try {
					ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "tandem.exe");
					Process p = pb.start();
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

				return;
			}
		}

		File closedSearchFile = new File(resultDir, "maxquant_search");
		File peptides = new File(closedSearchFile.getAbsoluteFile() + "\\combined\\txt\\peptides.txt");
		if (!peptides.exists()) {
			try {
				ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "MaxQuantTask.exe");
				Process p = pb.start();
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

			try {
				ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/IM", "MaxQuantTask.exe");
				Process p = pb.start();
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

			return;
		}
	}

}
