/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;

/**
 * @author Kai Cheng
 *
 */
public class MetaPeptideXMLWriter {

	private File output;
	private Document document;
	private XMLWriter writer;
	private Element root;
	private Element peptideRoot;
	private Element taxonRoot;

	private int peptideCount = 0;
	private int taxonCount = 0;

	private HashSet<Integer> taxonIdSet;

	private static final Logger LOGGER = LogManager.getLogger(MetaPeptideXMLWriter.class);

	public MetaPeptideXMLWriter(String output, String searchEngine, String quanType, String... fileNames) {
		this(new File(output), searchEngine, quanType, fileNames);
	}

	public MetaPeptideXMLWriter(File output, String searchEngine, String quanType, String... fileNames) {

		this.output = output;
		this.document = DocumentHelper.createDocument();

		OutputStreamWriter bufferedWriter;
		try {
			bufferedWriter = new OutputStreamWriter(new FileOutputStream(output), "UTF8");
			this.writer = new XMLWriter(bufferedWriter, OutputFormat.createPrettyPrint());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + output, e);
		}

		this.root = DocumentFactory.getInstance().createElement("Peptide_taxonomy");
		this.document.setRootElement(root);

		root.addAttribute("searchEngine", searchEngine);
		root.addAttribute("quantitativeMethodId", quanType);

		StringBuilder sb = new StringBuilder();
		for (String name : fileNames) {
			sb.append(name).append(";");
		}
		sb.deleteCharAt(sb.length() - 1);

		root.addAttribute("fileNames", sb.toString());

		peptideRoot = DocumentFactory.getInstance().createElement("Peptides");
		root.add(peptideRoot);

		taxonRoot = DocumentFactory.getInstance().createElement("Taxons");
		root.add(taxonRoot);

		this.taxonIdSet = new HashSet<Integer>();
	}

	public void addPeptide(MetaPeptide pep) {

		Element ePep = pep.getXmlPepElement();
		peptideRoot.add(ePep);
		peptideCount++;
	}

	public boolean addTaxon(Taxon taxon) {
		int id = taxon.getId();
		if (taxonIdSet.contains(id)) {
			return false;
		} else {
			taxonIdSet.add(id);

			String name = taxon.getName();
			int parentId = taxon.getParentId();
			int rankId = taxon.getRankId();
			int rootTypeId = taxon.getRootType().getId();
			Element eTaxon = DocumentFactory.getInstance().createElement("Taxon");
			eTaxon.addAttribute("id", String.valueOf(id));
			eTaxon.addAttribute("name", name);
			eTaxon.addAttribute("parentId", String.valueOf(parentId));
			eTaxon.addAttribute("rankId", String.valueOf(rankId));
			eTaxon.addAttribute("rootTypeId", String.valueOf(rootTypeId));

			int[] mainParentIds = taxon.getMainParentIds();
			if (mainParentIds != null) {
				StringBuilder mpsb = new StringBuilder();
				for (int mpid : mainParentIds) {
					mpsb.append(mpid).append(",");
				}
				if (mpsb.length() > 0) {
					mpsb.deleteCharAt(mpsb.length() - 1);
				}
				eTaxon.addAttribute("mainParentIds", mpsb.toString());
			}

			taxonRoot.add(eTaxon);
			taxonCount++;
			return true;
		}
	}

	public void close() {
		root.addAttribute("peptideCount", String.valueOf(peptideCount));
		root.addAttribute("taxonCount", String.valueOf(taxonCount));
		try {
			writer.write(document);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + output, e);
		}
	}

	public static void main(String[] args) {
		MaxquantPepReader reader = new MaxquantPepReader("Z:\\Kai\\20230526_big_maxquant\\"
				+ "peptides.txt");
		System.out.println(Arrays.toString(reader.getIntensityTitle()));
		MetaPeptide[] peps = reader.getMetaPeptides();
		System.out.println(peps.length);
		MetaPeptideXMLWriter writer = new MetaPeptideXMLWriter("Z:\\Kai\\20230526_big_maxquant\\peps.xml", 
				"MSFragger", "Label free", reader.getIntensityTitle());
		
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < peps.length; i++) {
			if (!peps[i].isUse()) {
				continue;
			}
			set.add(peps[i].getSequence());
		}
		System.out.println(set.size());
		
		TaxonomyDatabase taxDb = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		Pep2TaxaDatabase pep2TaxDb = new Pep2TaxaDatabase("D:\\Exported\\Resources\\pep2taxa\\pep2taxa.tab", taxDb);

		MetaPep2TaxaPar mptp = MetaPep2TaxaPar.getDefault();
		HashMap<String, ArrayList<Taxon>> taxaMap = pep2TaxDb.pept2TaxaList(set);
		HashMap<String, Integer> lcaIdMap = pep2TaxDb.taxa2Lca(taxaMap, mptp.getIgnoreBlankRank(),
				mptp.getUsedRootTypes(), mptp.getExcludeTaxa());

		for (MetaPeptide metapep : peps) {
			if ( !metapep.isUse()) {
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
}
