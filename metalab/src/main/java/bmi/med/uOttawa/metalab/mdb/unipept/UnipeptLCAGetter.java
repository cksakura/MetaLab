/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.dbSearch.MetaSearchEngine;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.XTandemPepReader;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptideXMLWriter;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

/**
 * @author Kai Cheng
 *
 */
public class UnipeptLCAGetter {

	private AbstractMetaPeptideReader reader;
	private MetaPeptideXMLWriter writer;
	private Appendable log;
	private File outFile;

	private TaxonomyDatabase taxDb;
	private MetaPeptide[] peps;

	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private String taskName = "Unipept LCA calculation task";
	private static Logger LOGGER = LogManager.getLogger(UnipeptLCAGetter.class);

	private UnipeptLCAGetter(String input, String output, String type, Appendable log) {

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
			log.append(format.format(new Date()) + "\t" + "LCA analysis by Unipept started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing log file", e);
			System.err.println(taskName + ": error in writing log file");
		}
	}

	private UnipeptLCAGetter(File input, File output, String type, Appendable log) {

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

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by Unipept started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing log file", e);
			System.err.println(taskName + ": error in writing log file");
		}
	}

	public UnipeptLCAGetter(File input, File output, MetaParameterV1 parameter, Appendable log) {

		this.log = log;
		this.taxDb = new TaxonomyDatabase(parameter.getTaxDb());

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
			log.append(format.format(new Date()) + "\t" + "LCA analysis by Unipept started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in writing log file", e);
			System.err.println(taskName + ": error in writing log file");
		}
	}

	public UnipeptLCAGetter(MetaParameterMQ metaPar, MetaSourcesV2 advPar) {

		File taxFile = new File(metaPar.getResult(), "taxonomy_analysis");
		if (!taxFile.exists()) {
			taxFile.mkdir();
		}
		this.outFile = new File(taxFile, "unipept.taxonomy.xml");
		this.taxDb = new TaxonomyDatabase(advPar.getTaxonAll());
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
			LOGGER.error(taskName + ": error in writing log file", e);
			System.err.println(taskName + ": error in writing log file");
		}

	}

	public void getLCA() {

		ArrayList<String> seqlist = new ArrayList<String>();
		for (int i = 0; i < this.peps.length; i++) {
			if (peps[i].isUse()) {
				seqlist.add(peps[i].getSequence());
			}
		}
		String[] sequences = seqlist.toArray(new String[seqlist.size()]);

		HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);

		for (MetaPeptide metapep : this.peps) {
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
					Taxon parentTaxon = this.taxDb.getTaxonFromId(parentId);
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

		try {
			log.append(format.format(new Date()) + "\t" + "LCA analysis by Unipept started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		writer.close();
	}

}
