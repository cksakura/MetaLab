/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.pep;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemPepReader;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLWriter;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class LCAGetter {

	private AbstractMetaPeptideReader reader;
	private MetaPeptideXMLWriter writer;
	private Appendable log;
	private File outFile;
	private MetaPeptide[] peps;

	private TaxonomyDatabase taxDb;

	private String pep2taxa = "Resources//db//pep2taxa.tab";
	private TaxonomyRanks rank;
	private HashSet<String> excludeSet;
	private HashSet<RootType> rootTypeSet;

	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	private String taskName = "Unipept LCA calculation task";
	private static final Logger LOGGER = LogManager.getLogger(LCAGetter.class);

	LCAGetter(String input, String output, String type, TaxonomyDatabase taxDb, Appendable log) {

		this.taxDb = taxDb;
		this.outFile = new File(output);
		this.excludeSet = new HashSet<String>();
		this.excludeSet.add("environmental samples");

		this.rank = TaxonomyRanks.Family;

		this.rootTypeSet = new HashSet<RootType>();
		this.rootTypeSet.add(RootType.Bacteria);
		this.rootTypeSet.add(RootType.Archaea);
		this.rootTypeSet.add(RootType.cellular_organisms);
		this.rootTypeSet.add(RootType.Eukaryota);
		this.rootTypeSet.add(RootType.Viroids);
		this.rootTypeSet.add(RootType.Viruses);

		this.log = log;
		String[] fileNames = null;

		if (type.equals(MetaConstants.maxQuant)) {
			this.reader = new MaxquantPepReader(input);
			fileNames = ((MaxquantPepReader) reader).getIntensityTitle();
		} else if (type.equals(MetaConstants.xTandem)) {
			this.reader = new XTandemPepReader(input);
			String name = (new File(input)).getName();
			name = name.substring(0, name.lastIndexOf("."));
			fileNames = new String[] { name };
		}

		this.writer = new MetaPeptideXMLWriter(output, type, reader.getQuanMode(), fileNames);
		this.peps = reader.getMetaPeptides();

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by built-in database started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public LCAGetter(String pep2taxa, TaxonomyDatabase taxDb, HashSet<RootType> rootTypeSet, TaxonomyRanks rank,
			HashSet<String> excludeSet, Appendable log) {
		this.pep2taxa = pep2taxa;
		this.taxDb = taxDb;
		this.rootTypeSet = rootTypeSet;
		this.rank = rank;
		this.excludeSet = excludeSet;
		this.log = log;
	}

	public LCAGetter(String input, String output, String type, String pep2taxa, TaxonomyDatabase taxDb,
			HashSet<RootType> rootTypeSet, TaxonomyRanks rank, HashSet<String> excludeSet, Appendable log) {

		this.outFile = new File(output);
		this.pep2taxa = pep2taxa;
		this.taxDb = taxDb;
		this.rootTypeSet = rootTypeSet;
		this.rank = rank;
		this.excludeSet = excludeSet;
		this.log = log;

		String[] fileNames = null;

		if (type.equals(MetaConstants.maxQuant)) {
			this.reader = new MaxquantPepReader(input);
			fileNames = ((MaxquantPepReader) reader).getIntensityTitle();
		} else if (type.equals(MetaConstants.xTandem)) {
			this.reader = new XTandemPepReader(input);
			String name = (new File(input)).getName();
			name = name.substring(0, name.lastIndexOf("."));
			fileNames = new String[] { name };
		}

		this.writer = new MetaPeptideXMLWriter(output, type, reader.getQuanMode(), fileNames);
		this.peps = reader.getMetaPeptides();

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by built-in database started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public LCAGetter(File input, File output, String type, String pep2taxa, TaxonomyDatabase taxDb,
			HashSet<RootType> rootTypeSet, TaxonomyRanks rank, HashSet<String> excludeSet, Appendable log) {

		this.outFile = output;
		this.pep2taxa = pep2taxa;
		this.taxDb = taxDb;
		this.rootTypeSet = rootTypeSet;
		this.rank = rank;
		this.excludeSet = excludeSet;
		this.log = log;
		String[] fileNames = null;

		if (type.equals(MetaConstants.maxQuant)) {
			this.reader = new MaxquantPepReader(input);
			fileNames = ((MaxquantPepReader) reader).getIntensityTitle();
		} else if (type.equals(MetaConstants.xTandem)) {
			this.reader = new XTandemPepReader(input);
			String name = input.getName();
			name = name.substring(0, name.lastIndexOf("."));
			fileNames = new String[] { name };
		}

		this.writer = new MetaPeptideXMLWriter(output, type, reader.getQuanMode(), fileNames);
		this.peps = reader.getMetaPeptides();
		this.rank = rank;

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by built-in database started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public LCAGetter(File input, File output, MetaParameterV1 parameter, Appendable log) {
		this.outFile = output;
		this.pep2taxa = parameter.getPep2taxa();
		this.taxDb = new TaxonomyDatabase(parameter.getTaxDb());
		this.rootTypeSet = parameter.getUsedRootTypes();
		this.rank = parameter.getIgnoreBlankRank();
		this.excludeSet = parameter.getExcludeTaxa();
		this.log = log;
		String[] fileNames = null;

		int type = parameter.getPepIdenResultType();
		if (type == MetaSearchEngine.maxquant.getId()) {
			this.reader = new MaxquantPepReader(input, parameter);
			fileNames = ((MaxquantPepReader) reader).getIntensityTitle();
			this.writer = new MetaPeptideXMLWriter(output, MetaConstants.maxQuant, reader.getQuanMode(), fileNames);
		} else if (type == MetaSearchEngine.xtandem.getId()) {
			this.reader = new XTandemPepReader(input);
			String name = input.getName();
			name = name.substring(0, name.lastIndexOf("."));
			fileNames = new String[] { name };
			this.writer = new MetaPeptideXMLWriter(output, MetaConstants.xTandem, reader.getQuanMode(), fileNames);
		}

		this.peps = reader.getMetaPeptides();

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by built-in database started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public LCAGetter(MetaParameterMQ metaPar, MetaSourcesV2 advPar) {

		File taxFile = new File(metaPar.getResult(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}
		this.outFile = new File(taxFile, "builtin.taxonomy.xml");

		this.pep2taxa = advPar.getPep2tax();
		this.taxDb = new TaxonomyDatabase(advPar.getTaxonAll());
		MetaPep2TaxaPar mptp = metaPar.getPtp();
		this.rootTypeSet = mptp.getUsedRootTypes();
		this.rank = mptp.getIgnoreBlankRank();
		this.excludeSet = mptp.getExcludeTaxa();
		this.log = new StringBuilder();

		if (metaPar.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow) {
			String[] fileNames = null;

			String searchType = "";
			File maxquantTxt = new File(metaPar.getDbSearchResultFile() + "\\combined\\txt\\peptides.txt");

			if (maxquantTxt.exists()) {

				searchType = MetaConstants.maxQuant;
				this.reader = new MaxquantPepReader(maxquantTxt, metaPar);
				fileNames = ((MaxquantPepReader) reader).getIntensityTitle();

			} else {
				LOGGER.info(taskName + ": maxquant search result file " + maxquantTxt + " was not found");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": maxquant search result file "
						+ maxquantTxt + " was not found");
			}

			this.writer = new MetaPeptideXMLWriter(outFile, searchType, metaPar.getQuanMode(), fileNames);
			this.peps = reader.getMetaPeptides();
		}

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by built-in database started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public void getLCA() {

		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < this.peps.length; i++) {
			if (peps[i].isUse()) {
				set.add(peps[i].getSequence());
			}
		}

		LOGGER.info("Unique peptide counts:" + set.size());

		Pep2TaxaDatabase pep2TaxDb = new Pep2TaxaDatabase(pep2taxa, taxDb);

		HashMap<String, ArrayList<Taxon>> taxaMap = pep2TaxDb.pept2TaxaList(set);
		HashMap<String, Integer> lcaIdMap = pep2TaxDb.taxa2Lca(taxaMap, rank, rootTypeSet, excludeSet);

		LOGGER.info(lcaIdMap.size() + " found in pep2tax database");

		for (MetaPeptide metapep : this.peps) {
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
						Taxon parentTaxon = this.taxDb.getTaxonFromId(parentId);
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

		try {
			log.append(format.format(new Date()) + "\t" + "writing result to " + outFile + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		writer.close();
	}

}
