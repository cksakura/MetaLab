/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.open.quant.OpenQuanPeptide;
import bmi.med.uOttawa.metalab.dbSearch.open.quant.OpenQuanProtein;
import bmi.med.uOttawa.metalab.dbSearch.open.quant.OpenQuanReader;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

/**
 * @author Kai Cheng
 *
 */
public class OpenSearchPepLcaTask extends SwingWorker<File, Void> {
	
	/** logger for this class */
	private static final Logger LOGGER = LogManager.getLogger(OpenSearchPepLcaTask.class);
	
	private File result;
	
	private File final_mods_file;
	private File final_peps_file;
	private File final_pros_file;
	
	private File quan_peps_file;
	private File quan_mod_peps_file;
	private File quan_pros_file;
	private File quan_psms_file;
	
	private File pep_tax_all;
	private File tax_all;
	
	private File tax_pep_file;
	private File tax_pro_file;
	private File tax_pep_lca_file;
	private File tax_pro_lca_file;

	private OpenSearchPepLcaTask(String resultDir){
		this(new File(resultDir));
	}
	
	private OpenSearchPepLcaTask(File resultDir) {
		this.result = resultDir;
		this.final_mods_file = new File(this.result, "final_mods.xml");
		this.final_peps_file = new File(this.result, "final_peps.csv");
		this.final_pros_file = new File(this.result, "final_pros.csv");

		this.quan_peps_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedBaseSequences.tsv");
		this.quan_mod_peps_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedModifiedSequences.tsv");
		this.quan_pros_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedProteins.tsv");
		this.quan_psms_file = new File(this.result, "quant_psms_FlashLFQ_QuantifiedPeaks.tsv");
		
		this.pep_tax_all = new File(this.result, "pep_tax_all.tsv");
		this.tax_all = new File(this.result, "tax_all.tsv");

		this.tax_pep_file = new File(this.result, "pep_taxa.csv");
		this.tax_pro_file = new File(this.result, "pro_taxa.csv");
		this.tax_pep_lca_file = new File(this.result, "LCA_by_pep.csv");
		this.tax_pro_lca_file = new File(this.result, "LCA_by_pro.csv");
	}
	
	public OpenSearchPepLcaTask(MetaParameterV1 parameter){
		
	}
	
	private void pep2Tax() {
		BufferedReader quanPepReader = null;
		try {
			quanPepReader = new BufferedReader(new FileReader(this.quan_peps_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptides from " + this.quan_peps_file, e);
		}

		HashSet<String> set = new HashSet<String>();
		try {
			String line = quanPepReader.readLine();
			String[] title = line.split(",");
			int seqId = -1;
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					seqId = i;
				}
			}

			while ((line = quanPepReader.readLine()) != null) {
				String[] cs = line.split("\t");
				set.add(cs[seqId]);
			}
			quanPepReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptides from " + this.quan_peps_file, e);
		}

		TaxonomyDatabase td = new TaxonomyDatabase();
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase(td);

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		HashMap<String, Integer> taxmap = pep2tax.pept2Lca(set, TaxonomyRanks.Family, rootSet, exclude);
		
	}
	
	private void pep2Tax(OpenQuanPeptide[] quanPeptides, String[] quanFileNames, TaxonomyDatabase td) {

		HashMap<String, Boolean> pepTdMap = new HashMap<String, Boolean>();
		HashMap<String, Double> pepIntenMap = new HashMap<String, Double>();
		for (OpenQuanPeptide op : quanPeptides) {
			String seq = op.getSequence();
			double[] intensity = op.getIntensity();
			double total = 0;
			for (int i = 0; i < intensity.length; i++) {
				if (intensity[i] > 0) {
					total += intensity[i];
				}
			}
			if (pepIntenMap.containsKey(seq)) {
				pepIntenMap.put(seq, pepIntenMap.get(seq) + total);
			} else {
				pepIntenMap.put(seq, total);
			}
			pepTdMap.put(seq, op.isTarget());
		}

		HashSet<String> seqset = new HashSet<String>();
		seqset.addAll(pepIntenMap.keySet());
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase(td);

		HashMap<String, ArrayList<Taxon>> pep2taxMap = pep2tax.pept2TaxaList(seqset);

		String[] keys = pep2taxMap.keySet().toArray(new String[pep2taxMap.size()]);
		Arrays.sort(keys);

		PrintWriter pep2taxWriter = null;
		try {
			pep2taxWriter = new PrintWriter(this.pep_tax_all);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing peptide taxon list to " + pep_tax_all, e);
		}

		HashMap<String, Integer> psmCountMap = OpenQuanReader.getQuanPSMCount(this.quan_psms_file);
		
		for (String sequence : keys) {
			double intensity = pepIntenMap.get(sequence);
			int psmCount = 0;
			if (psmCountMap.containsKey(sequence)) {
				psmCount = psmCountMap.get(sequence);
			}
			boolean isTarget = pepTdMap.get(sequence);

			StringBuilder sb = new StringBuilder();
			sb.append(sequence).append("\t");
			sb.append(psmCount).append("\t");
			sb.append(intensity).append("\t");
			sb.append(isTarget).append("\t");
			
			ArrayList<Taxon> list = pep2taxMap.get(sequence);
			for (Taxon taxon : list) {
				sb.append(taxon.getId()).append("\t");
			}
			
			/*ArrayList<Taxon> list = pep2taxMap.get(sequence);
			for (Taxon taxon : list) {
				int id = taxon.getId();
				sb.append(id).append("\t");

				if (tdmap.containsKey(id)) {
					if (tdmap.get(id)) {
						tdmap.put(id, isTarget);
					}
				} else {
					tdmap.put(id, isTarget);
				}

				int[] parents = parentMap.get(id);
				if (parents == null) {
					parents = td.getMainParentTaxonIds(taxon);
					parentMap.put(id, parents);
				}

				for (int i = 0; i < parents.length; i++) {
					if (parents[i] <= 0) {
						continue;
					}
					double[] pc = pcmap.get(parents[i]);
					if (pc == null) {
						pc = new double[2];
					}

					double[] sc = scmap.get(parents[i]);
					if (sc == null) {
						sc = new double[2];
					}

					double[] in = inmap.get(parents[i]);
					if (in == null) {
						in = new double[2];
					}

					if (list.size() == 1) {
						pc[0]++;
						sc[0] += psmCount;
						in[0] += intensity;
					} else {
						pc[1]++;
						sc[1] += psmCount;
						in[1] += intensity;
					}

					pcmap.put(parents[i], pc);
					scmap.put(parents[i], sc);
					inmap.put(parents[i], in);
				}
			}*/
			pep2taxWriter.println(sb);
		}
		pep2taxWriter.close();

		/*PrintWriter taxAllWriter = null;
		try {
			taxAllWriter = new PrintWriter(this.tax_all);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxon list to " + tax_all, e);
		}

		for (Integer id : tdmap.keySet()) {
			int[] parents = parentMap.get(id);
			double[][] speciesPc = new double[2][parents.length];
			double[][] speciesSc = new double[2][parents.length];
			double[][] speciesIn = new double[2][parents.length];

			for (int i = 0; i < parents.length; i++) {
				if (pcmap.containsKey(parents[i])) {
					double[] pc = pcmap.get(parents[i]);
					speciesPc[0][i] += pc[0];
					speciesPc[1][i] += pc[1];
				}
				if (scmap.containsKey(parents[i])) {
					double[] sc = scmap.get(parents[i]);
					speciesSc[0][i] += sc[0];
					speciesSc[1][i] += sc[1];
				}
				if (inmap.containsKey(parents[i])) {
					double[] in = inmap.get(parents[i]);
					speciesIn[0][i] += in[0];
					speciesIn[1][i] += in[1];
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append(id).append("\t");
			sb.append(tdmap.get(id)).append("\t");
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesPc[0][i]).append("\t");
			}
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesPc[1][i]).append("\t");
			}
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesSc[0][i]).append("\t");
			}
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesSc[1][i]).append("\t");
			}
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesIn[0][i]).append("\t");
			}
			for (int i = 0; i < parents.length; i++) {
				sb.append(speciesIn[1][i]).append("\t");
			}

			taxAllWriter.println(sb);
		}
		
		taxAllWriter.close();*/
	}
	
	/**
	 * @deprecated
	 * @param td
	 * not used 
	 */
	private void validateTax(TaxonomyDatabase td) {

		HashMap<Integer, Double> pcmap = new HashMap<Integer, Double>();
		HashMap<Integer, Double> scmap = new HashMap<Integer, Double>();
		HashMap<Integer, Double> ncmap = new HashMap<Integer, Double>();
		HashMap<Integer, Double> inmap = new HashMap<Integer, Double>();
		HashMap<Integer, Boolean> tdmap = new HashMap<Integer, Boolean>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(this.pep_tax_all));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide taxon list from " + pep_tax_all, e);
		}

		HashMap<Integer, int[]> parentMap = new HashMap<Integer, int[]>();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				int psmCount = Integer.parseInt(cs[1]);
				double intensity = Double.parseDouble(cs[2]);
				boolean isTarget = Boolean.parseBoolean(cs[3]);
				int taxaCount = cs.length - 4;

				double pc = 1.0 / (double) taxaCount;
				double sc = psmCount * pc;
				double nc = cs[0].length() * pc;
				double in = intensity * pc;

				for (int i = 4; i < cs.length; i++) {
					int id = Integer.parseInt(cs[i]);
					int[] parent = parentMap.get(id);
					if (parent == null) {
						parent = td.getMainParentTaxonIds(id);
						if (parent == null) {
							continue;
						}
						parentMap.put(id, parent);
					}

					if (tdmap.containsKey(id)) {
						if (tdmap.get(id)) {
							tdmap.put(id, isTarget);
						}
					} else {
						tdmap.put(id, isTarget);
					}
					
					for (int j = 0; j < parent.length; j++) {
						if (parent[j] > 0) {
							if (pcmap.containsKey(parent[j])) {
								pcmap.put(parent[j], pcmap.get(parent[j]) + pc);
							} else {
								pcmap.put(parent[j], pc);
							}
							if (scmap.containsKey(parent[j])) {
								scmap.put(parent[j], scmap.get(parent[j]) + sc);
							} else {
								scmap.put(parent[j], sc);
							}
							if (ncmap.containsKey(parent[j])) {
								ncmap.put(parent[j], ncmap.get(parent[j]) + nc);
							} else {
								ncmap.put(parent[j], nc);
							}
							if (inmap.containsKey(parent[j])) {
								inmap.put(parent[j], inmap.get(parent[j]) + in);
							} else {
								inmap.put(parent[j], in);
							}
						}
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide taxon list from " + pep_tax_all, e);
		}
		
		PrintWriter tempwriter = null;
		try {
			tempwriter = new PrintWriter(new File(result, "temp.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<Integer, double[]> speciesPcMap = new HashMap<Integer, double[]>();
		HashMap<Integer, double[]> speciesScMap = new HashMap<Integer, double[]>();
		HashMap<Integer, double[]> speciesNcMap = new HashMap<Integer, double[]>();
		HashMap<Integer, double[]> speciesInMap = new HashMap<Integer, double[]>();

		for (Integer id : parentMap.keySet()) {
			int[] parent = parentMap.get(id);
			double[] pc = new double[parent.length];
			double[] sc = new double[parent.length];
			double[] nc = new double[parent.length];
			double[] in = new double[parent.length];

			for (int i = 0; i < parent.length; i++) {
				if (pcmap.containsKey(parent[i])) {
					pc[i] = pcmap.get(parent[i]);
				}
				if (pcmap.containsKey(parent[i])) {
					sc[i] = scmap.get(parent[i]);
				}
				if (pcmap.containsKey(parent[i])) {
					nc[i] = ncmap.get(parent[i]);
				}
				if (pcmap.containsKey(parent[i])) {
					in[i] = inmap.get(parent[i]);
				}
			}

			speciesPcMap.put(id, pc);
			speciesScMap.put(id, sc);
			speciesNcMap.put(id, nc);
			speciesInMap.put(id, in);

			StringBuilder sb = new StringBuilder();
			sb.append(id).append("\t");
			sb.append(tdmap.get(id)).append("\t");
			for (int i = 0; i < pc.length; i++) {
				sb.append(pc[i]).append("\t");
			}
			for (int i = 0; i < pc.length; i++) {
				sb.append(sc[i]).append("\t");
			}
			for (int i = 0; i < pc.length; i++) {
				sb.append(nc[i]).append("\t");
			}
			for (int i = 0; i < pc.length; i++) {
				sb.append(in[i]).append("\t");
			}
			tempwriter.println(sb);
		}

//		TaxonLearning tl = new TaxonLearning();
//		HashMap<Integer, Double> taxScoreMap = tl.validate(speciesPcMap, speciesScMap, speciesNcMap, speciesInMap,
//				tdmap);
		
		tempwriter.close();
	}
	
	private void pep2Lca(TaxonomyDatabase td) {

		OpenQuanReader quanReader = new OpenQuanReader(this.quan_peps_file, this.quan_pros_file);
		String[] fileNames = quanReader.getFileNames();
		String[] sortedNames = new String[fileNames.length];
		System.arraycopy(fileNames, 0, sortedNames, 0, fileNames.length);
		Arrays.sort(sortedNames);
		int[] ids = new int[fileNames.length];
		for (int i = 0; i < ids.length; i++) {
			for (int j = 0; j < fileNames.length; j++) {
				if (sortedNames[i].equals(fileNames[j])) {
					ids[i] = j;
					break;
				}
			}
		}

		OpenQuanPeptide[] qpeps = quanReader.getPeptides();

		HashMap<String, double[]> pepmap = new HashMap<String, double[]>();
		for (OpenQuanPeptide op : qpeps) {
			String seq = op.getSequence();
			if (pepmap.containsKey(seq)) {
				double[] intensity = pepmap.get(seq);
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] += op.getIntensity()[i];
				}
				pepmap.put(seq, intensity);
			} else {
				pepmap.put(seq, op.getIntensity());
			}
		}

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		HashSet<String> seqset = new HashSet<String>();
		seqset.addAll(pepmap.keySet());
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase(td);

		HashMap<String, Integer> pep2taxMap = pep2tax.pept2Lca(seqset, TaxonomyRanks.Family, rootSet, exclude);
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();

		PrintWriter tax_pep_writer = null;
		try {
			tax_pep_writer = new PrintWriter(this.tax_pep_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + tax_pep_file, e);
		}

		StringBuilder tax_pep_title = new StringBuilder();
		tax_pep_title.append("\"Sequence\",");
		tax_pep_title.append("\"Taxon ID\",");
		tax_pep_title.append("\"Taxon rank\",");
		tax_pep_title.append("\"Taxon name\",");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_title.append("\"").append(mainRanks[i].getName()).append("\",");
		}
		for (int i = 0; i < sortedNames.length; i++) {
			tax_pep_title.append("\"").append(sortedNames[i]).append("\"").append(",");
		}
		tax_pep_writer.println(tax_pep_title);

		String[] sequences = pep2taxMap.keySet().toArray(new String[pep2taxMap.size()]);
		Arrays.sort(sequences);
		HashMap<Integer, double[]> pepLcaIntenMap = new HashMap<Integer, double[]>();
		HashMap<Integer, String[]> pepLcaParentMap = new HashMap<Integer, String[]>();

		for (String sequence : sequences) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(sequence).append("\",");

			Integer id = pep2taxMap.get(sequence);
			sb.append("\"").append(id).append("\",");

			Taxon taxon = td.getTaxonFromId(id);
			if (taxon == null) {
				continue;
			}
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");

			int[] parents = td.getMainParentTaxonIds(taxon);
			String[] pnames = new String[parents.length];
			for (int pid = 0; pid < parents.length; pid++) {
				Taxon ptaxon = td.getTaxonFromId(parents[pid]);
				if (ptaxon == null) {
					sb.append("\"\",");
				} else {
					pnames[pid] = ptaxon.getName();
					sb.append("\"").append(pnames[pid]).append("\"").append(",");
				}
			}

			double[] intensity = pepmap.get(sequence);
			for (int i = 0; i < intensity.length; i++) {
				if (intensity[ids[i]] == 0) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(intensity[ids[i]]).append("\",");
				}
			}

			if (pepLcaIntenMap.containsKey(id)) {
				double[] tintensity = pepLcaIntenMap.get(id);
				for (int i = 0; i < tintensity.length; i++) {
					tintensity[i] += intensity[i];
				}
				pepLcaIntenMap.put(id, tintensity);
			} else {
				pepLcaIntenMap.put(id, intensity);
			}

			pepLcaParentMap.put(id, pnames);

			tax_pep_writer.println(sb);
		}
		tax_pep_writer.close();
		
		PrintWriter tax_pep_lca_writer = null;
		try {
			tax_pep_lca_writer = new PrintWriter(this.tax_pep_lca_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + tax_pep_lca_file, e);
		}
		StringBuilder tax_pep_lca_sb = new StringBuilder();
		tax_pep_lca_sb.append("\"Taxon ID\",");
		tax_pep_lca_sb.append("\"Taxon rank\",");
		tax_pep_lca_sb.append("\"Taxon name\",");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_lca_sb.append("\"").append(mainRanks[i].getName()).append("\",");
		}
		for (int i = 0; i < sortedNames.length; i++) {
			tax_pep_lca_sb.append("\"").append(sortedNames[i]).append("\"").append(",");
		}
		tax_pep_lca_writer.println(tax_pep_lca_sb);

		Integer[] tax_pep_lca_ids = pepLcaIntenMap.keySet().toArray(new Integer[pepLcaIntenMap.size()]);
		Arrays.sort(tax_pep_lca_ids);
		
		for (Integer id : tax_pep_lca_ids) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(id).append("\",");
			
			Taxon taxon = td.getTaxonFromId(id);
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");
			
			String[] parents = pepLcaParentMap.get(id);
			for (String parent : parents) {
				if (parent == null) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(parent).append("\"").append(",");
				}
			}
			double[] intensity = pepLcaIntenMap.get(id);
			for (int i = 0; i < intensity.length; i++) {
				if (intensity[ids[i]] == 0) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(intensity[ids[i]]).append("\",");
				}
			}
			tax_pep_lca_writer.println(sb);
		}
		tax_pep_lca_writer.close();	
	}
	
	private void pep2Lca(OpenQuanPeptide[] quanPeptides, String[] quanFileNames, TaxonomyDatabase td){
		
		HashMap<String, double[]> pepmap = new HashMap<String, double[]>();
		for (OpenQuanPeptide op : quanPeptides) {
			String seq = op.getSequence();
			if (pepmap.containsKey(seq)) {
				double[] intensity = pepmap.get(seq);
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] += op.getIntensity()[i];
				}
				pepmap.put(seq, intensity);
			} else {
				pepmap.put(seq, op.getIntensity());
			}
		}

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		HashSet<String> seqset = new HashSet<String>();
		seqset.addAll(pepmap.keySet());
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase(td);

		HashMap<String, Integer> pep2taxMap = pep2tax.pept2Lca(seqset, TaxonomyRanks.Family, rootSet, exclude);
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();

		PrintWriter tax_pep_writer = null;
		try {
			tax_pep_writer = new PrintWriter(this.tax_pep_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + tax_pep_file, e);
		}
		StringBuilder tax_pep_title = new StringBuilder();
		tax_pep_title.append("Sequence,");
		tax_pep_title.append("Taxon ID,");
		tax_pep_title.append("Taxon rank,");
		tax_pep_title.append("Taxon name,");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_title.append(mainRanks[i].getName()).append(",");
		}
		for (int i = 0; i < quanFileNames.length; i++) {
			tax_pep_title.append("\"").append("Intensity_").append(quanFileNames[i]).append("\"").append(",");
		}
		tax_pep_writer.println(tax_pep_title);
		String[] sequences = pep2taxMap.keySet().toArray(new String[pep2taxMap.size()]);
		Arrays.sort(sequences);
		HashMap<Integer, double[]> pepLcaIntenMap = new HashMap<Integer, double[]>();
		HashMap<Integer, String[]> pepLcaParentMap = new HashMap<Integer, String[]>();

		for (String sequence : sequences) {
			StringBuilder sb = new StringBuilder();
			sb.append(sequence).append(",");
			Integer id = pep2taxMap.get(sequence);
			sb.append(id).append(",");
			Taxon taxon = td.getTaxonFromId(id);
			if (taxon == null) {
				continue;
			}
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");

			int[] parents = td.getMainParentTaxonIds(taxon);
			String[] pnames = new String[parents.length];
			for (int pid = 0; pid < parents.length; pid++) {
				Taxon ptaxon = td.getTaxonFromId(parents[pid]);
				if (ptaxon == null) {
					sb.append("\"\",");
				} else {
					pnames[pid] = ptaxon.getName();
					sb.append("\"").append(pnames[pid]).append("\"").append(",");
				}
			}

			double[] intensity = pepmap.get(sequence);
			for (double inten : intensity) {
				sb.append(inten).append(",");
			}

			if (pepLcaIntenMap.containsKey(id)) {
				double[] tintensity = pepLcaIntenMap.get(id);
				for (int i = 0; i < tintensity.length; i++) {
					tintensity[i] += intensity[i];
				}
				pepLcaIntenMap.put(id, tintensity);
			} else {
				pepLcaIntenMap.put(id, intensity);
			}

			pepLcaParentMap.put(id, pnames);

			tax_pep_writer.println(sb);
		}
		tax_pep_writer.close();

		PrintWriter tax_pep_lca_writer = null;
		try {
			tax_pep_lca_writer = new PrintWriter(this.tax_pep_lca_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + tax_pep_lca_file, e);
		}
		StringBuilder tax_pep_lca_sb = new StringBuilder();
		tax_pep_lca_sb.append("Taxon ID,");
		tax_pep_lca_sb.append("Taxon rank,");
		tax_pep_lca_sb.append("Taxon name,");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_lca_sb.append(mainRanks[i].getName()).append(",");
		}
		for (int i = 0; i < quanFileNames.length; i++) {
			tax_pep_lca_sb.append("\"").append("Intensity_").append(quanFileNames[i]).append("\"").append(",");
		}
		tax_pep_lca_writer.println(tax_pep_lca_sb);

		Integer[] tax_pep_lca_ids = pepLcaIntenMap.keySet().toArray(new Integer[pepLcaIntenMap.size()]);
		Arrays.sort(tax_pep_lca_ids);
		for (Integer id : tax_pep_lca_ids) {
			StringBuilder sb = new StringBuilder();
			sb.append(id).append(",");
			Taxon taxon = td.getTaxonFromId(id);
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");
			String[] parents = pepLcaParentMap.get(id);
			for (String parent : parents) {
				if (parent == null) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(parent).append("\"").append(",");
				}
			}
			double[] intensity = pepLcaIntenMap.get(id);
			for (double inten : intensity) {
				sb.append("\"").append(inten).append("\"").append(",");
			}
			tax_pep_lca_writer.println(sb);
		}
		tax_pep_lca_writer.close();	
	}
	
	private static void combine(String[] inputs, String out, TaxonomyDatabase td) {

		OpenSearchPepLcaTask task = new OpenSearchPepLcaTask(out);
		int[][] ids = new int[inputs.length][];
		String[][] sortedNames = new String[inputs.length][];
		HashMap<String, double[]>[] pepmap = new HashMap[inputs.length];
		int totalColumnCount = 0;

		for (int i = 0; i < inputs.length; i++) {

			OpenSearchPepLcaTask taski = new OpenSearchPepLcaTask(inputs[i]);
			OpenQuanReader quanReader = new OpenQuanReader(taski.quan_peps_file, taski.quan_pros_file);

			String[] fileNames = quanReader.getFileNames();
			sortedNames[i] = new String[fileNames.length];
			System.arraycopy(fileNames, 0, sortedNames[i], 0, fileNames.length);
			Arrays.sort(sortedNames[i]);
			totalColumnCount += fileNames.length;

			ids[i] = new int[fileNames.length];

			for (int j = 0; j < fileNames.length; j++) {
				for (int k = 0; k < fileNames.length; k++) {
					if (sortedNames[i][j].equals(fileNames[k])) {
						ids[i][j] = k;
						break;
					}
				}
			}

			OpenQuanPeptide[] qpeps = quanReader.getPeptides();

			pepmap[i] = new HashMap<String, double[]>();
			for (OpenQuanPeptide op : qpeps) {
				String seq = op.getSequence();
				if (pepmap[i].containsKey(seq)) {
					double[] intensity = pepmap[i].get(seq);
					for (int j = 0; j < intensity.length; j++) {
						intensity[j] += op.getIntensity()[j];
					}
					pepmap[i].put(seq, intensity);
				} else {
					pepmap[i].put(seq, op.getIntensity());
				}
			}
		}

		HashSet<String> exclude = new HashSet<String>();
		exclude.add("environmental samples");

		HashSet<RootType> rootSet = new HashSet<RootType>();
		rootSet.add(RootType.Bacteria);
		rootSet.add(RootType.Archaea);
		rootSet.add(RootType.cellular_organisms);
		rootSet.add(RootType.Eukaryota);
		rootSet.add(RootType.Viroids);
		rootSet.add(RootType.Viruses);

		HashSet<String> seqset = new HashSet<String>();
		for (int i = 0; i < pepmap.length; i++) {
			seqset.addAll(pepmap[i].keySet());
		}

		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase(td);

		HashMap<String, Integer> pep2taxMap = pep2tax.pept2Lca(seqset, TaxonomyRanks.Family, rootSet, exclude);
		TaxonomyRanks[] mainRanks = TaxonomyRanks.getMainRanks();

		PrintWriter tax_pep_writer = null;
		try {
			tax_pep_writer = new PrintWriter(task.tax_pep_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + task.tax_pep_file, e);
		}

		StringBuilder tax_pep_title = new StringBuilder();
		tax_pep_title.append("\"Sequence\",");
		tax_pep_title.append("\"Taxon ID\",");
		tax_pep_title.append("\"Taxon rank\",");
		tax_pep_title.append("\"Taxon name\",");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_title.append("\"").append(mainRanks[i].getName()).append("\",");
		}
		for (int i = 0; i < sortedNames.length; i++) {
			for (int j = 0; j < sortedNames[i].length; j++) {
				tax_pep_title.append("\"").append(sortedNames[i][j]).append("\"").append(",");
			}
		}
		tax_pep_writer.println(tax_pep_title);

		String[] sequences = pep2taxMap.keySet().toArray(new String[pep2taxMap.size()]);
		Arrays.sort(sequences);
		HashMap<Integer, double[]> pepLcaIntenMap = new HashMap<Integer, double[]>();
		HashMap<Integer, String[]> pepLcaParentMap = new HashMap<Integer, String[]>();

		for (String sequence : sequences) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(sequence).append("\",");

			Integer id = pep2taxMap.get(sequence);
			sb.append("\"").append(id).append("\",");

			Taxon taxon = td.getTaxonFromId(id);
			if (taxon == null) {
				continue;
			}
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");

			int[] parents = td.getMainParentTaxonIds(taxon);
			String[] pnames = new String[parents.length];
			for (int pid = 0; pid < parents.length; pid++) {
				Taxon ptaxon = td.getTaxonFromId(parents[pid]);
				if (ptaxon == null) {
					sb.append("\"\",");
				} else {
					pnames[pid] = ptaxon.getName();
					sb.append("\"").append(pnames[pid]).append("\"").append(",");
				}
			}

			double[] intensity = new double[totalColumnCount];
			int columnId = 0;

			for (int i = 0; i < pepmap.length; i++) {
				double[] inteni = null;
				if (pepmap[i].containsKey(sequence)) {
					inteni = pepmap[i].get(sequence);
				} else {
					inteni = new double[sortedNames[i].length];
				}
				for (int j = 0; j < inteni.length; j++) {
					intensity[columnId++] = inteni[ids[i][j]];
				}
			}

			for (int i = 0; i < intensity.length; i++) {
				if (intensity[i] == 0) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(intensity[i]).append("\",");
				}
			}

			if (pepLcaIntenMap.containsKey(id)) {
				double[] tintensity = pepLcaIntenMap.get(id);
				for (int i = 0; i < tintensity.length; i++) {
					tintensity[i] += intensity[i];
				}
				pepLcaIntenMap.put(id, tintensity);
			} else {
				pepLcaIntenMap.put(id, intensity);
			}

			pepLcaParentMap.put(id, pnames);

			tax_pep_writer.println(sb);
		}
		tax_pep_writer.close();

		PrintWriter tax_pep_lca_writer = null;
		try {
			tax_pep_lca_writer = new PrintWriter(task.tax_pep_lca_file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing taxonomy information to " + task.tax_pep_lca_file, e);
		}
		StringBuilder tax_pep_lca_sb = new StringBuilder();
		tax_pep_lca_sb.append("\"Taxon ID\",");
		tax_pep_lca_sb.append("\"Taxon rank\",");
		tax_pep_lca_sb.append("\"Taxon name\",");
		for (int i = 0; i < mainRanks.length; i++) {
			tax_pep_lca_sb.append("\"").append(mainRanks[i].getName()).append("\",");
		}
		for (int i = 0; i < sortedNames.length; i++) {
			for (int j = 0; j < sortedNames[i].length; j++) {
				tax_pep_lca_sb.append("\"").append(sortedNames[i][j]).append("\"").append(",");
			}
		}
		tax_pep_lca_writer.println(tax_pep_lca_sb);

		Integer[] tax_pep_lca_ids = pepLcaIntenMap.keySet().toArray(new Integer[pepLcaIntenMap.size()]);
		Arrays.sort(tax_pep_lca_ids);

		for (Integer id : tax_pep_lca_ids) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(id).append("\",");

			Taxon taxon = td.getTaxonFromId(id);
			sb.append("\"").append(taxon.getRank()).append("\"").append(",");
			sb.append("\"").append(taxon.getName()).append("\"").append(",");

			String[] parents = pepLcaParentMap.get(id);
			for (String parent : parents) {
				if (parent == null) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(parent).append("\"").append(",");
				}
			}
			double[] intensity = pepLcaIntenMap.get(id);
			for (int i = 0; i < intensity.length; i++) {
				if (intensity[i] == 0) {
					sb.append("\"\",");
				} else {
					sb.append("\"").append(intensity[i]).append("\",");
				}
			}
			tax_pep_lca_writer.println(sb);
		}
		tax_pep_lca_writer.close();
	}
	
	private static void select(String pep, String lca, String output, int leastNum) throws IOException {
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();
		BufferedReader pepReader = new BufferedReader(new FileReader(pep));
		String line = pepReader.readLine();
		while ((line = pepReader.readLine()) != null) {
			String[] cs = line.split(",");
			HashSet<String> set = map.get(cs[1]);
			if (set == null) {
				set = new HashSet<String>();
			}
			set.add(cs[0]);
			map.put(cs[1], set);
		}
		pepReader.close();

		PrintWriter writer = new PrintWriter(output);
		BufferedReader lcaReader = new BufferedReader(new FileReader(lca));
		writer.println(lcaReader.readLine());

		while ((line = lcaReader.readLine()) != null) {
			String[] cs = line.split(",");
			if (map.containsKey(cs[0])) {
				if (map.get(cs[0]).size() >= leastNum) {
					writer.println(line);
				}
			}
		}
		lcaReader.close();
		writer.close();
	}
	
	private static void combineProtein(String pro1, String quanPro1, String pro2, String quanPro2, String output)
			throws IOException {

		BufferedReader pro1Reader = new BufferedReader(new FileReader(pro1));
		String line = pro1Reader.readLine();
		HashMap<String, String[]> pro1Map = new HashMap<String, String[]>();
		while ((line = pro1Reader.readLine()) != null) {
			String[] cs = line.split(",");
			pro1Map.put(cs[0], cs);
		}
		pro1Reader.close();

		BufferedReader quanPro1Reader = new BufferedReader(new FileReader(quanPro1));
		line = quanPro1Reader.readLine();
		String[] cs1 = line.split("\t");
		String[] title1 = new String[cs1.length - 1];
		int[] id1 = new int[title1.length];
		System.arraycopy(cs1, 1, title1, 0, title1.length);
		Arrays.sort(title1);
		for (int i = 0; i < title1.length; i++) {
			for (int j = 0; j < cs1.length; j++) {
				if (title1[i].equals(cs1[j])) {
					id1[i] = j;
				}
			}
		}

		HashMap<String, String[]> quanPro1Map = new HashMap<String, String[]>();
		while ((line = quanPro1Reader.readLine()) != null) {
			String[] cs = line.split("\t");
			quanPro1Map.put(cs[0], cs);
		}
		quanPro1Reader.close();

		BufferedReader pro2Reader = new BufferedReader(new FileReader(pro2));
		line = pro2Reader.readLine();
		HashMap<String, String[]> pro2Map = new HashMap<String, String[]>();
		while ((line = pro2Reader.readLine()) != null) {
			String[] cs = line.split(",");
			pro2Map.put(cs[0], cs);
		}
		pro2Reader.close();

		BufferedReader quanPro2Reader = new BufferedReader(new FileReader(quanPro2));
		line = quanPro2Reader.readLine();
		String[] cs2 = line.split("\t");
		String[] title2 = new String[cs2.length - 1];
		int[] id2 = new int[title2.length];
		System.arraycopy(cs2, 1, title2, 0, title2.length);
		Arrays.sort(title1);
		for (int i = 0; i < title2.length; i++) {
			for (int j = 0; j < cs2.length; j++) {
				if (title2[i].equals(cs2[j])) {
					id2[i] = j;
				}
			}
		}
		HashMap<String, String[]> quanPro2Map = new HashMap<String, String[]>();
		while ((line = quanPro2Reader.readLine()) != null) {
			String[] cs = line.split("\t");
			quanPro2Map.put(cs[0], cs);
		}
		quanPro2Reader.close();

		PrintWriter writer = new PrintWriter(output);
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Protein,");
		titlesb.append("Score,");
		titlesb.append("Peptide count,");
		titlesb.append("PSM count,");
		for (int i = 0; i < title1.length; i++) {
			titlesb.append(title1[i]).append(",");
		}
		for (int i = 0; i < title2.length; i++) {
			titlesb.append(title2[i]).append(",");
		}
		titlesb.deleteCharAt(titlesb.length() - 1);
		writer.println(titlesb);

		HashSet<String> totalset = new HashSet<String>();
		totalset.addAll(pro1Map.keySet());
		totalset.addAll(quanPro1Map.keySet());
		totalset.addAll(pro2Map.keySet());
		totalset.addAll(quanPro2Map.keySet());

		for (String key : totalset) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"").append(key).append("\",");

			double score = 0;
			int pepcount = 0;
			int psmcount = 0;
			if (pro1Map.containsKey(key)) {
				String[] cs = pro1Map.get(key);
				double s = Double.parseDouble(cs[1]);
				if (s > score) {
					score = s;
				}
				int pepc = Integer.parseInt(cs[2]);
				pepcount += pepc;

				int psmc = Integer.parseInt(cs[3]);
				psmcount += psmc;
			}
			if (pro2Map.containsKey(key)) {
				String[] cs = pro2Map.get(key);
				double s = Double.parseDouble(cs[1]);
				if (s > score) {
					score = s;
				}
				int pepc = Integer.parseInt(cs[2]);
				pepcount += pepc;

				int psmc = Integer.parseInt(cs[3]);
				psmcount += psmc;
			}
			sb.append(score).append(",");
			sb.append(pepcount).append(",");
			sb.append(psmcount).append(",");

			boolean quan = false;
			if (quanPro1Map.containsKey(key)) {
				quan = true;
				String[] cs = quanPro1Map.get(key);
				for (int i = 0; i < id1.length; i++) {
					if (cs[id1[i]].length() > 1) {
						double intensity = Double.parseDouble(cs[id1[i]]);
						sb.append(intensity).append(",");
					} else {
						sb.append("0,");
					}
				}
			} else {
				for (int i = 0; i < id1.length; i++) {
					sb.append("0,");
				}
			}

			if (quanPro2Map.containsKey(key)) {
				quan = true;
				String[] cs = quanPro2Map.get(key);
				for (int i = 0; i < id2.length; i++) {
					if (cs[id2[i]].length() > 1) {
						double intensity = Double.parseDouble(cs[id2[i]]);
						sb.append(intensity).append(",");
					} else {
						sb.append("0,");
					}
				}
			} else {
				for (int i = 0; i < id2.length; i++) {
					sb.append("0,");
				}
			}

			if (quan) {
				sb.deleteCharAt(sb.length() - 1);
				writer.println(sb);
			}
		}
		writer.close();
	}

	@Override
	protected File doInBackground() {
		// TODO Auto-generated method stub

		OpenQuanReader opreader = new OpenQuanReader(this.quan_peps_file, this.quan_pros_file);
		String[] quanFileNames = opreader.getFileNames();
		OpenQuanPeptide[] quanPeptides = opreader.getPeptides();
		OpenQuanProtein[] quanProteins = opreader.getProteins();

		TaxonomyDatabase td = new TaxonomyDatabase();

//		this.pep2Tax(quanPeptides, quanFileNames, td);
		
		this.validateTax(td);
		
//		this.validate();
/*
		HashSet<String> set = new HashSet<String>();
		for (OpenQuanPeptide qp : quanPeptides) {
			if (!qp.isTarget()) {
				set.add(qp.getSequence());
			}
		}

		Pep2TaxaDatabase pep2tax = null;
		try {
			pep2tax = new Pep2TaxaDatabase(td);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Can not load pep2tax database", e);
		}

		HashMap<String, ArrayList<Taxon>> pep2taxMap = null;
		try {
			pep2taxMap = pep2tax.pept2TaxaList(set);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to taxa", e);
		}

		PrintWriter decoyWriter = null;
		try {
			decoyWriter = new PrintWriter(new File(result, "decoy_taxa.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String key : pep2taxMap.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(key).append("\t");
			ArrayList<Taxon> list = pep2taxMap.get(key);
			for (Taxon taxon : list) {
				sb.append(taxon.getId()).append("\t");
			}
			decoyWriter.println(sb);
		}
		decoyWriter.close();*/

		return null;
	}
}
