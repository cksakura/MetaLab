/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipLCAResult;
import bmi.med.uOttawa.metalab.mdb.unipept.UnipeptRequester;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.MetaReportTask;
import bmi.med.uOttawa.metalab.task.io.MetaAlgorithm;
import bmi.med.uOttawa.metalab.task.io.MetaBiomJsonHandler;
import bmi.med.uOttawa.metalab.task.io.MetaTreeHandler;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLReader;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLWriter;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class MetaTaxaTaskV2 extends MetaAbstractTask {

	private MetaPep2TaxaPar mptp;

	private static Logger LOGGER = LogManager.getLogger(MetaTaxaTaskV2.class);
	private static final String taskName = "Taxonomy analysis";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public MetaTaxaTaskV2(MetaParameterMQ metaPar, MetaSourcesV2 msv2, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, msv2, bar1, bar2, nextWork);
	}

	protected void initial() {
		this.mptp = ((MetaParameterMQ) metaPar).getPtp();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		boolean finished1 = false;
		boolean finished2 = false;

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		File summaryMetaFile = metaPar.getMetafile();
		if (summaryMetaFile == null || !summaryMetaFile.exists()) {
			summaryMetaFile = new File(metaPar.getDbSearchResultFile(), "metainfo.tsv");
			try {
				metaPar.getMetadata().exportMetadata(summaryMetaFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block

				LOGGER.error(taskName + ": error in exporting metadata information", e);
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": error in exporting metadata information");

				return false;
			}

			metaPar.setMetafile(summaryMetaFile);
		}

		String pep2tax = ((MetaSourcesV2) msv).getPep2tax();

		if (mptp.isBuildIn()) {
			if ((new File(pep2tax)).exists()) {
				finished1 = builtinCalculate();
			} else {
				LOGGER.error(taskName
						+ ": pep2taxa database was not found, please set it in Setting->Resource, or choose \"Unipept\" in Setting->Taxonomy analysis");
				System.err.println(format.format(new Date()) + "\t" + taskName
						+ ": pep2taxa database was not found, please set it in Setting->Resource, or choose \"Unipept\" in Setting->Taxonomy analysis");
				return false;
			}
		}

		if (mptp.isUnipept()) {
			finished2 = unipeptCalculate();
		}

		setProgress(85);

		if (finished1 || finished2) {

			LOGGER.info(taskName + ": finished");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");

			return true;
		} else {

			LOGGER.info(taskName + ": failed");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");

			return false;
		}
	}

	private boolean builtinCalculate() {

		boolean finished = false;

		File taxFile;
		if (((MetaParameterMQ) metaPar).getQuickOutput() != null) {
			taxFile = new File(((MetaParameterMQ) metaPar).getQuickOutput());
		} else {
			if (metaPar.getDbSearchResultFile() != null) {
				taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
			} else {
				taxFile = new File(metaPar.getResult());
			}
		}

		if (!taxFile.exists()) {
			taxFile.mkdir();
		}

		File taxResultFile = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxonomy.xml");

		if (taxResultFile.exists() && taxResultFile.length() > 0) {

			LOGGER.info(taskName + ": taxonomy result file " + taxResultFile.getName() + " was found, go to next step");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": taxonomy result file "
					+ taxResultFile.getName() + " was found, go to next step");

			MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

			File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
			reader.exportCsv(taxFile, MetaAlgorithm.Builtin, mptp.getLeastPepCount());

			MetaPeptide[] peps = reader.getPeptides();
			Taxon[] taxons = reader.getTaxons();
			String[] expNames = reader.getFileNames();

			File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
			if (!megan.exists() || megan.length() == 0) {
				MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
			}

			File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
			if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
				reader.exportPeptideTaxaAll(allPepTaxa);
			}

			File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
			if (!iMetaLab.exists() || iMetaLab.length() == 0) {
				MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
			}

			File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
			if (!jsOutput.getParentFile().exists()) {
				jsOutput.getParentFile().mkdirs();
			}

			if (!jsOutput.exists() || jsOutput.length() == 0) {
				MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
			}

			if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
				finished = true;
			}

			File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

			if (!reportDir.exists()) {
				reportDir.mkdirs();
			}

			MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

			File report_taxonomy_summary = new File(reportDir, "report_taxonomy_summary.html");
			if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
				if (this.metaPar.getMetadata() == null) {
					task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);
				} else {
					task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, metaPar.getMetafile());
				}
			}

			task.run();

			try {
				finished = task.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return finished;

		} else {

			String pep2taxa = ((MetaSourcesV2) msv).getPep2tax();
			TaxonomyDatabase taxDb = new TaxonomyDatabase(((MetaSourcesV2) msv).getTaxonAll());
			HashSet<RootType> rootTypeSet = mptp.getUsedRootTypes();
			TaxonomyRanks rank = mptp.getIgnoreBlankRank();
			HashSet<String> excludeSet = mptp.getExcludeTaxa();

			String[] fileNames = null;

			String searchType = "";

			MetaPeptide[] peps = null;

			switch (metaPar.getWorkflowType()) {

			case MaxQuantWorkflow: {

				File maxquantTxt = new File(metaPar.getDbSearchResultFile() + "\\combined\\txt\\peptides.txt");

				if (maxquantTxt.exists()) {

					searchType = MetaConstants.maxQuant;
					MaxquantPepReader reader = new MaxquantPepReader(maxquantTxt, (MetaParameterMQ) metaPar);
					fileNames = ((MaxquantPepReader) reader).getIntensityTitle();

					peps = reader.getMetaPeptides();

					MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, searchType,
							((MetaParameterMQ) metaPar).getQuanMode(), fileNames);

					HashSet<String> set = new HashSet<String>();
					for (int i = 0; i < peps.length; i++) {
						if (peps[i].isUse()) {
							set.add(peps[i].getSequence());
						}
					}

					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": Unique peptide counts:" + set.size());

					Pep2TaxaDatabase pep2TaxDb = new Pep2TaxaDatabase(pep2taxa, taxDb);

					HashMap<String, ArrayList<Taxon>> taxaMap = pep2TaxDb.pept2TaxaList(set);
					HashMap<String, Integer> lcaIdMap = pep2TaxDb.taxa2Lca(taxaMap, rank, rootTypeSet, excludeSet);

					System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lcaIdMap.size()
							+ " unique peptide are found in pep2tax database");

					for (MetaPeptide metapep : peps) {
						if (!metapep.isUse()) {
							continue;
						}
						String sequence = metapep.getSequence();
						if (lcaIdMap.containsKey(sequence)) {
							int taxId = lcaIdMap.get(sequence);
							Taxon lca = taxDb.getTaxonFromId(taxId);

							if (lca == null) {
								continue;
							}
							metapep.setLca(lca);

							ArrayList<Taxon> taxonList = taxaMap.get(sequence);
							int[] taxonIds = new int[taxonList.size()];
							for (int i = 0; i < taxonList.size(); i++) {
								Taxon taxoni = taxonList.get(i);
								taxonIds[i] = taxoni.getId();
								if (taxoni.getMainParentIds() == null) {
									taxoni.setMainParentIds(taxDb.getMainParentTaxonIds(taxoni));
								}

								int[] mainTaxonIds = taxoni.getMainParentIds();

								for (int parentId : mainTaxonIds) {
									Taxon parentTaxon = taxDb.getTaxonFromId(parentId);
									if (parentTaxon != null) {
										if (parentTaxon.getMainParentIds() == null) {
											parentTaxon.setMainParentIds(taxDb.getMainParentTaxonIds(parentId));
										}
										writer.addTaxon(parentTaxon);
									}
								}
							}
							metapep.setTaxonIds(taxonIds);

							writer.addPeptide(metapep);
						}
					}
					writer.close();
				}

				break;
			}

			case TaxonomyAnalysis: {

				searchType = ((MetaParameterMQ) metaPar).getQuanResultType();
				fileNames = ((MetaParameterMQ) metaPar).getIntensityTitles();
				peps = ((MetaParameterMQ) metaPar).getPeps();

				MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, searchType,
						((MetaParameterMQ) metaPar).getQuanMode(), fileNames);

				HashSet<String> set = new HashSet<String>();
				for (int i = 0; i < peps.length; i++) {
					if (searchType.equals(MetaConstants.maxQuant) && !peps[i].isUse()) {
						continue;
					}
					set.add(peps[i].getSequence());
				}

				System.out
						.println(format.format(new Date()) + "\t" + taskName + ": Unique peptide counts:" + set.size());

				Pep2TaxaDatabase pep2TaxDb = new Pep2TaxaDatabase(pep2taxa, taxDb);

				HashMap<String, ArrayList<Taxon>> taxaMap = pep2TaxDb.pept2TaxaList(set);
				HashMap<String, Integer> lcaIdMap = pep2TaxDb.taxa2Lca(taxaMap, rank, rootTypeSet, excludeSet);

				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lcaIdMap.size()
						+ " unique peptide are found in pep2tax database");

				for (MetaPeptide metapep : peps) {
					if (searchType.equals(MetaConstants.maxQuant) && !metapep.isUse()) {
						continue;
					}
					String sequence = metapep.getSequence();
					if (lcaIdMap.containsKey(sequence)) {
						int taxId = lcaIdMap.get(sequence);
						Taxon lca = taxDb.getTaxonFromId(taxId);

						if (lca == null) {
							continue;
						}
						metapep.setLca(lca);

						ArrayList<Taxon> taxonList = taxaMap.get(sequence);
						int[] taxonIds = new int[taxonList.size()];
						for (int i = 0; i < taxonList.size(); i++) {
							Taxon taxoni = taxonList.get(i);
							taxonIds[i] = taxoni.getId();
							if (taxoni.getMainParentIds() == null) {
								taxoni.setMainParentIds(taxDb.getMainParentTaxonIds(taxoni));
							}

							int[] mainTaxonIds = taxoni.getMainParentIds();

							for (int parentId : mainTaxonIds) {
								Taxon parentTaxon = taxDb.getTaxonFromId(parentId);
								if (parentTaxon != null) {
									if (parentTaxon.getMainParentIds() == null) {
										parentTaxon.setMainParentIds(taxDb.getMainParentTaxonIds(parentId));
									}
									writer.addTaxon(parentTaxon);
								}
							}
						}
						metapep.setTaxonIds(taxonIds);

						writer.addPeptide(metapep);
					}
				}
				writer.close();

				break;
			}

			default:
				break;
			}
		}

		if (!taxResultFile.exists() || taxResultFile.length() == 0) {
			return false;
		}

		MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);
		File refinedTaxon = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".taxa.refine.csv");
		reader.exportCsv(taxFile, MetaAlgorithm.Builtin, mptp.getLeastPepCount());

		MetaPeptide[] peps = reader.getPeptides();
		Taxon[] taxons = reader.getTaxons();
		String[] expNames = reader.getFileNames();

		File megan = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".biom");
		if (!megan.exists() || megan.length() == 0) {
			MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
		}

		File allPepTaxa = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".allPepTaxa.csv");
		if (!allPepTaxa.exists() || allPepTaxa.length() == 0) {
			reader.exportPeptideTaxaAll(allPepTaxa);
		}

		File iMetaLab = new File(taxFile, MetaAlgorithm.Builtin.getName() + ".iMetaLab.tree.csv");
		if (!iMetaLab.exists() || iMetaLab.length() == 0) {
			MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
		}

		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.getParentFile().exists()) {
			jsOutput.getParentFile().mkdirs();
		}
		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
		}

		if (megan.exists() && allPepTaxa.exists() && iMetaLab.exists() && jsOutput.exists()) {
			finished = true;
		}

		File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());
		
		File report_taxonomy_summary = new File(reportDir, "report_taxonomy_summary.html");
		if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
			if (this.metaPar.getMetafile() != null && this.metaPar.getMetafile().exists()) {
				task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, metaPar.getMetafile());
			} else {
				task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);
			}
		}
		
		task.run();

		try {
			finished = task.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return finished;
	}

	private boolean unipeptCalculate() {

		boolean finished = false;

		File taxFile;
		if (metaPar.getDbSearchResultFile() != null) {
			taxFile = new File(metaPar.getDbSearchResultFile(), "taxonomy_analysis");
		} else {
			taxFile = new File(metaPar.getResult());
		}
		
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}
		
		File taxResultFile = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxonomy.xml");

		if (taxResultFile.exists() && taxResultFile.length() > 0) {

			LOGGER.info(taskName + ": taxonomy result file " + taxResultFile + " was found");

			MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);

			File refinedTaxon = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxa.refine.csv");
			reader.exportCsv(taxFile, MetaAlgorithm.Unipept, mptp.getLeastPepCount());

			MetaPeptide[] peps = reader.getPeptides();
			Taxon[] taxons = reader.getTaxons();
			String[] expNames = reader.getFileNames();

			File megan = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".biom");
			if (!megan.exists() || megan.length() == 0) {
				MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
			}

			File iMetaLab = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
			if (!iMetaLab.exists() || iMetaLab.length() == 0) {
				MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
			}

			File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
			if (!jsOutput.getParentFile().exists()) {
				jsOutput.getParentFile().mkdirs();
			}

			if (!jsOutput.exists() || jsOutput.length() == 0) {
				MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
			}

			if (megan.exists() && iMetaLab.exists() && jsOutput.exists()) {
				finished = true;
			}

			File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

			if (!reportDir.exists()) {
				reportDir.mkdirs();
			}

			MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

			File report_taxonomy_summary = new File(reportDir, "report_taxonomy_summary.html");
			if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
				if (metaPar.getMetafile() != null && metaPar.getMetafile().exists()) {
					task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, metaPar.getMetafile());
				} else {
					task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);
				}
			}

			task.run();

			try {
				finished = task.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return finished;

		} else {

			TaxonomyDatabase taxDb = new TaxonomyDatabase(((MetaSourcesV2) msv).getTaxonAll());
			String[] fileNames = null;

			String searchType = "";
			MetaPeptide[] peps = null;

			switch (metaPar.getWorkflowType()) {

			case MaxQuantWorkflow: {

				File maxquantTxt = new File(metaPar.getDbSearchResultFile() + "\\combined\\txt\\peptides.txt");

				if (maxquantTxt.exists()) {

					searchType = MetaConstants.maxQuant;
					MaxquantPepReader reader = new MaxquantPepReader(maxquantTxt, ((MetaParameterMQ) metaPar));
					fileNames = ((MaxquantPepReader) reader).getIntensityTitle();
					peps = reader.getMetaPeptides();

					MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, searchType,
							((MetaParameterMQ) metaPar).getQuanMode(), fileNames);

					ArrayList<String> seqlist = new ArrayList<String>();
					for (int i = 0; i < peps.length; i++) {
						if (peps[i].isUse()) {
							seqlist.add(peps[i].getSequence());
						}
					}
					String[] sequences = seqlist.toArray(new String[seqlist.size()]);

					HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);

					for (MetaPeptide metapep : peps) {
						if (!metapep.isUse()) {
							continue;
						}
						String sequence = metapep.getSequence();
						if (lcaResultMap.containsKey(sequence)) {
							UnipLCAResult lcaResult = lcaResultMap.get(sequence);
							int taxId = lcaResult.getTaxon().getId();
							Taxon lca = taxDb.getTaxonFromId(taxId);

							if (lca == null) {
								continue;
							}
							if (lca.getMainParentIds() == null) {
								lca.setMainParentIds(taxDb.getMainParentTaxonIds(taxId));
							}

							int[] mainTaxonIds = lca.getMainParentIds();

							for (int parentId : mainTaxonIds) {
								Taxon parentTaxon = taxDb.getTaxonFromId(parentId);
								if (parentTaxon != null) {
									if (parentTaxon.getMainParentIds() == null) {
										parentTaxon.setMainParentIds(taxDb.getMainParentTaxonIds(parentId));
									}
									writer.addTaxon(parentTaxon);
								}
							}

							metapep.setLca(lca);
							writer.addPeptide(metapep);
						}
					}

					writer.close();
				}

				break;
			}

			case TaxonomyAnalysis: {

				searchType = ((MetaParameterMQ) metaPar).getQuanResultType();
				fileNames = ((MetaParameterMQ) metaPar).getIntensityTitles();
				peps = ((MetaParameterMQ) metaPar).getPeps();

				MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter(taxResultFile, searchType,
						((MetaParameterMQ) metaPar).getQuanMode(), fileNames);

				ArrayList<String> seqlist = new ArrayList<String>();
				for (int i = 0; i < peps.length; i++) {
					if (searchType.equals(MetaConstants.maxQuant) && !peps[i].isUse()) {
						continue;
					}
					seqlist.add(peps[i].getSequence());
				}
				String[] sequences = seqlist.toArray(new String[seqlist.size()]);

				HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);

				for (MetaPeptide metapep : peps) {
					if (searchType.equals(MetaConstants.maxQuant) && !metapep.isUse()) {
						continue;
					}
					String sequence = metapep.getSequence();
					if (lcaResultMap.containsKey(sequence)) {
						UnipLCAResult lcaResult = lcaResultMap.get(sequence);
						int taxId = lcaResult.getTaxon().getId();
						Taxon lca = taxDb.getTaxonFromId(taxId);

						if (lca == null) {
							continue;
						}
						if (lca.getMainParentIds() == null) {
							lca.setMainParentIds(taxDb.getMainParentTaxonIds(taxId));
						}

						int[] mainTaxonIds = lca.getMainParentIds();

						for (int parentId : mainTaxonIds) {
							Taxon parentTaxon = taxDb.getTaxonFromId(parentId);
							if (parentTaxon != null) {
								if (parentTaxon.getMainParentIds() == null) {
									parentTaxon.setMainParentIds(taxDb.getMainParentTaxonIds(parentId));
								}
								writer.addTaxon(parentTaxon);
							}
						}

						metapep.setLca(lca);
						writer.addPeptide(metapep);
					}
				}

				writer.close();

				break;
			}

			default:
				break;
			}
		}

		if (!taxResultFile.exists() || taxResultFile.length() == 0) {
			return false;
		}

		MetaPeptideXMLReader reader = new MetaPeptideXMLReader(taxResultFile);
		File refinedTaxon = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".taxa.refine.csv");
		reader.exportCsv(taxFile, MetaAlgorithm.Unipept, mptp.getLeastPepCount());

		MetaPeptide[] peps = reader.getPeptides();
		Taxon[] taxons = reader.getTaxons();
		String[] expNames = reader.getFileNames();

		File megan = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".biom");
		if (!megan.exists() || megan.length() == 0) {
			MetaBiomJsonHandler.export(peps, taxons, expNames, megan.getAbsolutePath());
		}

		File iMetaLab = new File(taxFile, MetaAlgorithm.Unipept.getName() + ".iMetaLab.tree.csv");
		if (!iMetaLab.exists() || iMetaLab.length() == 0) {
			MetaTreeHandler.export(peps, taxons, expNames, iMetaLab);
		}

		File jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree.js");
		if (!jsOutput.exists() || jsOutput.length() == 0) {
			MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
		} else {
			jsOutput = new File(metaPar.getReportDir() + "\\reports\\metamap\\data\\tree2.js");
			MetaTreeHandler.exportJS(peps, taxons, expNames, jsOutput);
		}

		if (megan.exists() && iMetaLab.exists() && jsOutput.exists()) {
			finished = true;
		}

		File reportDir = new File(metaPar.getReportDir() + "\\reports\\rmd_html");

		if (!reportDir.exists()) {
			reportDir.mkdirs();
		}

		MetaReportTask task = new MetaReportTask(metaPar.getThreadCount());

		File report_taxonomy_summary = new File(reportDir, "report_taxonomy_summary.html");
		if (!report_taxonomy_summary.exists() || report_taxonomy_summary.length() == 0) {
			if (this.metaPar.getMetafile() != null && this.metaPar.getMetafile().exists()) {
				task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary, metaPar.getMetafile());
			} else {
				task.addTask(MetaReportTask.taxon, refinedTaxon, report_taxonomy_summary);
			}
		}

		task.run();

		try {
			finished = task.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return finished;
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}
}
