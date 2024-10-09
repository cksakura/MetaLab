package bmi.med.uOttawa.metalab.task.hgm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.Taxon;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyDatabase;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.dbSearch.open.io.OpenPIAXMLWriter;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPSM;
import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindPsmReader;
import bmi.med.uOttawa.metalab.mdb.pep.Pep2TaxaDatabase;
import de.mpc.pia.modeller.PIAModeller;

public class HapPSMFilterTask {

	private File pFindSpectra;
	private File xmloutFile;

	private File[] ms2File;
	private String[] rawNames;
	private HashMap<String, HashMap<Integer, Double>> rtmap;
	private HashMap<String, Integer> ms2CountMap;

	public HapPSMFilterTask(File pFindSpectra, File xmloutFile) {
		this.pFindSpectra = pFindSpectra;
		this.xmloutFile = xmloutFile;
	}

	public HapPSMFilterTask(File pFindSpectra, File spectraFile, String in,
			HashMap<String, PostTransModification> modMap) {
		this.pFindSpectra = pFindSpectra;
		this.ms2File = spectraFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().endsWith("ms2"))
					return true;
				return false;
			}
		});

		this.rawNames = new String[ms2File.length];
		for (int i = 0; i < rawNames.length; i++) {
			rawNames[i] = ms2File[i].getName();
			rawNames[i] = rawNames[i].substring(0, rawNames.length - ".ms2".length());
		}
	}

	private void initial() throws NumberFormatException, IOException {

		this.rtmap = new HashMap<String, HashMap<Integer, Double>>();
		this.ms2CountMap = new HashMap<String, Integer>();

		for (int i = 0; i < ms2File.length; i++) {

			int count = 0;
			HashMap<Integer, Double> rtmapi = new HashMap<Integer, Double>();

			BufferedReader reader = new BufferedReader(new FileReader(ms2File[i]));

			String line = null;
			int scannum = -1;
			double rt = -1;

			while ((line = reader.readLine()) != null) {

				char c0 = line.charAt(0);
				String[] cs = line.split("[\\s+]");

				switch (c0) {
				case 'H':
					break;

				case 'S':

					scannum = Integer.parseInt(cs[1]);
					break;

				case 'I':

					if (cs[1].equals("RetTime")) {
						rt = Double.parseDouble(cs[2]) / 60.0;
						rtmapi.put(scannum, rt);
						count++;
					}

					break;
				case 'Z':

					break;

				default:
					break;
				}

			}

			reader.close();

			this.rtmap.put(rawNames[i], rtmapi);
			this.ms2CountMap.put(rawNames[i], count);
		}

		PFindPsmReader reader = new PFindPsmReader(pFindSpectra);
	}

	private void filter(File fasta, File[] rawFiles) throws IOException {

		PFindPsmReader reader = new PFindPsmReader(this.pFindSpectra);
		PFindPSM[] psms = reader.getPsms();

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(this.xmloutFile);
		writer.initialPFind(fasta, rawFiles);

		int count = 0;
		for (PFindPSM psm : psms) {
			writer.add(psm);
			if (count++ == 10000) {
				break;
			}
		}
		writer.close();
		/*
		 * File piaParameter = new File(par); byte[] buffer = new byte[1024]; int
		 * readBytes;
		 * 
		 * URL fromUrl =
		 * getClass().getResource("/bmi/med/uOttawa/metalab/dbSearch/pia/parameter.xml")
		 * ; URLConnection urlConnection = fromUrl.openConnection(); InputStream is =
		 * urlConnection.getInputStream();
		 * 
		 * OutputStream os = new FileOutputStream(piaParameter); try { while ((readBytes
		 * = is.read(buffer)) != -1) { os.write(buffer, 0, readBytes); } } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * 
		 * } finally { os.close(); is.close(); }
		 * 
		 * String[] args = new String[] { "-infile", "\"" + xmlout + "\"", "-paramFile",
		 * "\"" + piaParameter.getAbsolutePath() + "\"", "-peptideExport", "\"" + pepout
		 * + "\"", "csv", "-proteinExport", "\"" + proout + "\"", "csv" };
		 * 
		 * PIAModeller.main(args);
		 */
	}

	private void filter(File fasta, File[] rawFiles, File piaParameter, String pepout, String proout)
			throws IOException {

		PFindPsmReader reader = new PFindPsmReader(this.pFindSpectra);
		PFindPSM[] psms = reader.getPsms();

		OpenPIAXMLWriter writer = new OpenPIAXMLWriter(this.xmloutFile);
		writer.initialPFind(fasta, rawFiles);

		int count = 0;
		for (PFindPSM psm : psms) {
//			writer.add(psm);
			writer.addSpecies(psm);
		}
		writer.close();

		byte[] buffer = new byte[1024];
		int readBytes;

		URL fromUrl = getClass().getResource("/bmi/med/uOttawa/metalab/dbSearch/pia/parameter.xml");
		URLConnection urlConnection = fromUrl.openConnection();
		InputStream is = urlConnection.getInputStream();

		OutputStream os = new FileOutputStream(piaParameter);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block

		} finally {
			os.close();
			is.close();
		}

		String[] args = new String[] { "-infile", "\"" + this.xmloutFile + "\"", "-paramFile",
				"\"" + piaParameter.getAbsolutePath() + "\"", "-peptideExport", "\"" + pepout + "\"", "csv",
				"-proteinExport", "\"" + proout + "\"", "csv" };

		PIAModeller.main(args);

	}

	private static void getIdRate(String summary, String in) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		HashMap<String, int[]> map = new HashMap<String, int[]>();
		BufferedReader summaryReader = new BufferedReader(new FileReader(summary));
		String line = null;
		while ((line = summaryReader.readLine()) != null) {
			if (line.startsWith("ID Rate:")) {
				while ((line = summaryReader.readLine()) != null) {
					if (line.startsWith("Overall")) {
						break;
					} else {
						String[] cs = line.split("[\\s]");
						list.add(cs[0]);
						map.put(cs[0], new int[] { 0, Integer.parseInt(cs[cs.length - 3]) });
					}
				}
			}
		}
		summaryReader.close();

		BufferedReader psmReader = new BufferedReader(new FileReader(in));
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs[0].endsWith("dta")) {
				String name = cs[0].substring(0, cs[0].indexOf("."));
				map.get(name)[0]++;
			}
		}
		psmReader.close();

		for (int i = 0; i < list.size(); i++) {
			int[] count = map.get(list.get(i));
			double ratio = (double) count[0] / (double) count[1];
			System.out.println(FormatTool.getDFP2().format(ratio));
		}
	}

	private static void toSingleCharge(String in, String out) throws IOException {
		BufferedReader psmReader = new BufferedReader(new FileReader(in));
		String line = psmReader.readLine();
		String[] title = line.split("\t");
		HashMap<String, Double> map = new HashMap<String, Double>();
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				String scan = cs[0];
				String[] ss = scan.split("\\.");
				scan = ss[0] + "_" + ss[1];
				double score = Double.parseDouble(cs[8]);
				if (map.containsKey(scan)) {
					if (score > map.get(scan)) {
						map.put(scan, score);
					}
				} else {
					map.put(scan, score);
				}
			}
		}
		psmReader.close();

		PrintWriter writer = new PrintWriter(out);
		psmReader = new BufferedReader(new FileReader(in));
		line = psmReader.readLine();
		writer.println(line);

		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				String scan = cs[0];
				String[] ss = scan.split("\\.");
				scan = ss[0] + "_" + ss[1];

				double score = Double.parseDouble(cs[8]);
				if (map.containsKey(scan) && score == map.get(scan)) {
					writer.println(line);
				}
			}
		}
		psmReader.close();
		writer.close();
	}

	private static void compareSequence(String compareWith, String filterSpectra, String allSpectra, String dbpath, String out)
			throws IOException, SQLException {
		
		HashMap<String, Double> rawScoreMap = new HashMap<String, Double>();
		HashSet<String> filteredSet = new HashSet<String>();
		BufferedReader psmReader = new BufferedReader(new FileReader(filterSpectra));
		String line = psmReader.readLine();
		String[] title = line.split("\t");
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				filteredSet.add(cs[5]);
				rawScoreMap.put(cs[0], Double.parseDouble(cs[8]));
			}
		}
		psmReader.close();
		System.out.println("Filtered\t" + filteredSet.size());

		HashSet<String> oldMethodSet = new HashSet<String>();
		HashSet<String> missSet = new HashSet<String>();
		
		HashSet<String> bigScoreSet = new HashSet<String>();
		psmReader = new BufferedReader(new FileReader(compareWith));
		line = psmReader.readLine();
		title = line.split("\t");
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				oldMethodSet.add(cs[5]);
				if (!filteredSet.contains(cs[5])) {
					missSet.add(cs[5]);
					
					if(rawScoreMap.containsKey(cs[0])) {
						if(rawScoreMap.get(cs[0])>Double.parseDouble(cs[8])) {
							bigScoreSet.add(cs[5]);
						}
					}
				}
			}
		}
		psmReader.close();
		System.out.println("Missed\t" + missSet.size());
		System.out.println("old\t" + oldMethodSet.size());
		
		System.out.println("Bigger\t" + bigScoreSet.size());

		HashSet<String> recurvedSet = new HashSet<String>();
		psmReader = new BufferedReader(new FileReader(allSpectra));
		line = psmReader.readLine();
		title = line.split("\t");
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
//				if(Double.parseDouble(cs[4])<0.01) {
				if (missSet.contains(cs[5])) {
					recurvedSet.add(cs[5]);
				}
//				}
			}
		}
		psmReader.close();

		System.out.println("recurved\t" + recurvedSet.size());
		
		HashSet<String> tempSet = new HashSet<String>();
		tempSet.addAll(bigScoreSet);
		tempSet.addAll(recurvedSet);
		System.out.println(bigScoreSet.size() + "\t" + recurvedSet.size() + "\t"
				+ (bigScoreSet.size() + recurvedSet.size() - tempSet.size()));
		
		PrintWriter writer = new PrintWriter(out);
		
		int count = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (String seq : missSet) {
			if (!recurvedSet.contains(seq)) {
				sb.append("'").append(seq).append("'").append(",");
				count++;
				
				writer.println(seq);
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		System.out.println(count);
		
		writer.close();
		
		/*
		PrintWriter writer = new PrintWriter(out);

		String url = "jdbc:sqlite:" + dbpath;

		Connection conn = DriverManager.getConnection(url);
		Statement stmt = conn.createStatement();

		DecimalFormat df5 = new DecimalFormat("00000");
		for (int i = 1; i <= 4644; i++) {
			String name = df5.format(i);
			if(i%100==0)
				System.out.println(name);
			String sql = "select * from pep2pro_" + name + " where pep IN " + sb;

			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				StringBuilder ssb = new StringBuilder();
				ssb.append(name).append("\t");
				ssb.append(rs.getString("pep")).append("\t");
				ssb.append(rs.getString("pro"));

				writer.println(ssb);
			}
		}

		writer.close();
*/		
	}
	
	private static void getMissTaxon(String missPep, String missPepPro, String out) throws IOException {
		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(missPep));
		String line = null;
		while((line=reader.readLine())!=null) {
			set.add(line);
		}
		reader.close();
		
		System.out.println("miss all pep\t"+set.size());
		
		HashSet<String> pepproSet = new HashSet<String>();
		reader = new BufferedReader(new FileReader(missPepPro));
		while((line=reader.readLine())!=null) {
			String[] cs = line.split("\t");
			set.remove(cs[1]);
			pepproSet.add(cs[1]);
		}
		reader.close();
		
		System.out.println("miss all pep\t"+set.size()+"\t"+pepproSet.size());
		
		TaxonomyDatabase td = new TaxonomyDatabase("D:\\Exported\\Resources\\taxonomy-all.tab");
		Pep2TaxaDatabase pep2tax = new Pep2TaxaDatabase("D:\\Exported\\Resources\\pep2taxa\\pep2taxa.tab", td);
//		HashMap<String, ArrayList<Taxon>> map = pep2tax.pept2TaxaList(set);

		HashSet<String> excludeSet = new HashSet<String>();
		excludeSet.add("environmental samples");

		TaxonomyRanks rank = TaxonomyRanks.Family;

		HashSet<RootType>rootTypeSet = new HashSet<RootType>();
		rootTypeSet.add(RootType.Bacteria);
		rootTypeSet.add(RootType.Archaea);
		rootTypeSet.add(RootType.cellular_organisms);
		rootTypeSet.add(RootType.Eukaryota);
		rootTypeSet.add(RootType.Viroids);
		rootTypeSet.add(RootType.Viruses);
		
		HashMap<String, Integer> lcaMap = pep2tax.pept2Lca(set, rank, rootTypeSet, excludeSet);

		System.out.println("lca\t"+lcaMap.size());
		
		/*		
		for (String pep : map.keySet()) {
			writer.println("pep\t" + pep);
			ArrayList<Taxon> taxons = map.get(pep);
			for (Taxon taxon : taxons) {
				StringBuilder sb = new StringBuilder();
				String[] names = td.getTaxonArrays(taxon);
				for (String name : names) {
					if (name == null) {
						sb.append("\t");
					} else {
						sb.append(name).append("\t");
					}

				}
				writer.println(sb);
			}
		}
*/
		PrintWriter writer = new PrintWriter(out);
		for (String pep : lcaMap.keySet()) {
			Integer id = lcaMap.get(pep);
			Taxon taxon = td.getTaxonFromId(id);
			StringBuilder sb = new StringBuilder();
			sb.append(pep).append("\t");
			if (taxon != null) {
				String[] names = td.getTaxonArrays(taxon);
				if (names != null) {
					for (String name : names) {
						if (name == null) {
							sb.append("\t");
						} else {
							sb.append(name).append("\t");
						}
					}
				}
			}

			writer.println(sb);
		}
		
		writer.close();
		
		System.out.println(set.size()+"\t"+lcaMap.size());
	}
	
	private static void missPepAnalysis(String in) throws IOException {
		HashSet<String> pepSet = new HashSet<String>();
		HashMap<String, HashSet<String>> genomeMap = new HashMap<String, HashSet<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			pepSet.add(cs[1]);

			if (genomeMap.containsKey(cs[0])) {
				genomeMap.get(cs[0]).add(cs[2]);
			} else {
				HashSet<String> proSet = new HashSet<String>();
				proSet.add(cs[2]);
				genomeMap.put(cs[0], proSet);
			}
		}
		reader.close();
		
		System.out.println(pepSet.size()+"\t"+genomeMap.size());
	}
	
	private static void comparePSM(String igc, String genome, String output) throws IOException {
		BufferedReader psmReader = new BufferedReader(new FileReader(igc));
		String line = psmReader.readLine();
		String[] title = line.split("\t");
		HashMap<String, String> seqMap1 = new HashMap<String, String>();
		HashMap<String, Double> finalMap1 = new HashMap<String, Double>();
		HashMap<String, Boolean> tdMap1 = new HashMap<String, Boolean>();

		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				String scan = cs[0];
				String[] ss = scan.split("\\.");
				scan = ss[0] + "_" + ss[1];
				double score = -Math.log10(Double.parseDouble(cs[9]));
				if (seqMap1.containsKey(scan)) {
					if (score > finalMap1.get(scan)) {
						seqMap1.put(scan, cs[5]);
						finalMap1.put(scan, -Math.log10(Double.parseDouble(cs[9])));
						tdMap1.put(scan, cs[cs.length - 4].equals("target"));
					}
				} else {
					seqMap1.put(scan, cs[5]);
					finalMap1.put(scan, -Math.log10(Double.parseDouble(cs[9])));
					tdMap1.put(scan, cs[cs.length - 4].equals("target"));
				}
			}
		}
		psmReader.close();

		psmReader = new BufferedReader(new FileReader(genome));
		line = psmReader.readLine();
		title = line.split("\t");
		HashMap<String, String> seqMap2 = new HashMap<String, String>();
		HashMap<String, Double> finalMap2 = new HashMap<String, Double>();
		HashMap<String, Boolean> tdMap2 = new HashMap<String, Boolean>();
		
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				String scan = cs[0];
				String[] ss = scan.split("\\.");
				scan = ss[0] + "_" + ss[1];
				
				double score = -Math.log10(Double.parseDouble(cs[9]));
				if (seqMap2.containsKey(scan)) {
					if (score > finalMap2.get(scan)) {
						seqMap2.put(scan, cs[5]);
						finalMap2.put(scan, -Math.log10(Double.parseDouble(cs[9])));
						tdMap2.put(scan, cs[cs.length - 4].equals("target"));
					}
				} else {
					seqMap2.put(scan, cs[5]);
					finalMap2.put(scan, -Math.log10(Double.parseDouble(cs[9])));
					tdMap2.put(scan, cs[cs.length - 4].equals("target"));
				}
			}
		}
		psmReader.close();

		HashSet<String> set = new HashSet<String>();
		set.addAll(seqMap1.keySet());
		set.addAll(seqMap2.keySet());

		HashSet<String> useqSet = new HashSet<String>();
		HashSet<String> useqSet1 = new HashSet<String>();
		HashSet<String> useqSet2 = new HashSet<String>();
		
		useqSet1.addAll(seqMap1.values());
		useqSet2.addAll(seqMap2.values());
		
		useqSet.addAll(useqSet1);
		useqSet.addAll(useqSet2);
		
		System.out.println(useqSet1.size()+"\t"+useqSet2.size()+"\t"+useqSet.size());
		
		HashSet<String> commonSet = new HashSet<String>();

		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String scan = it.next();
			if (seqMap1.containsKey(scan) && seqMap2.containsKey(scan)) {
				String seq1 = seqMap1.get(scan);
				String seq2 = seqMap2.get(scan);

				if (seq1.equals(seq2)) {
					
					
					if(tdMap1.get(scan) == tdMap2.get(scan)) {
						commonSet.add(scan);
//						System.out.println(scan+"\t"+seq1+"\t"+seq2);
					}
				}
			}
		}

		System.out.println(seqMap1.size() + "\t" + seqMap2.size() + "\t" + set.size()+"\t"+commonSet.size());
		
		psmReader = new BufferedReader(new FileReader(igc));
		line = psmReader.readLine();
		title = line.split("\t");

		int[] igcTarget = new int[2];
		int[] igcDecoy = new int[2];
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				if (commonSet.contains(cs[0])) {
					double qvalue = Double.parseDouble(cs[4]);
					if (cs[cs.length - 4].equals("target")) {
						if (qvalue < 0.01) {
							igcTarget[0]++;
						} else {
							igcTarget[1]++;
						}
					} else {
						if (qvalue < 0.01) {
							igcDecoy[0]++;
						} else {
							igcDecoy[1]++;
						}
					}
				}
			}
		}
		psmReader.close();

		psmReader = new BufferedReader(new FileReader(genome));
		line = psmReader.readLine();
		title = line.split("\t");

		int[] genomeTarget = new int[2];
		int[] genomeDecoy = new int[2];
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				if (commonSet.contains(cs[0])) {
					double qvalue = Double.parseDouble(cs[4]);
					if (cs[cs.length - 4].equals("target")) {
						if (qvalue < 0.01) {
							genomeTarget[0]++;
						} else {
							genomeTarget[1]++;
						}
					} else {
						if (qvalue < 0.01) {
							genomeDecoy[0]++;
						} else {
							genomeDecoy[1]++;
						}
					}
				}
			}
		}
		psmReader.close();

		System.out.println("igcTarget q<0.01\t" + igcTarget[0]);
		System.out.println("igcTarget q>0.01\t" + igcTarget[1]);
		System.out.println("igcDecoy q<0.01\t" + igcDecoy[0]);
		System.out.println("igcDecoy q>0.01\t" + igcDecoy[1]);

		System.out.println("genomeTarget q<0.01\t" + genomeTarget[0]);
		System.out.println("genomeTarget q>0.01\t" + genomeTarget[1]);
		System.out.println("genomeDecoy q<0.01\t" + genomeDecoy[0]);
		System.out.println("genomeDecoy q>0.01\t" + genomeDecoy[1]);
	}
	
	private static void getDecoyCount(String in) throws IOException {
		BufferedReader psmReader = new BufferedReader(new FileReader(in));
		String line = psmReader.readLine();
		String[] title = line.split("\t");

		int count = 0;
		while ((line = psmReader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (cs.length == title.length) {
				if (cs[cs.length - 4].equals("target")) {
					if (cs[cs.length - 7].contains("REV_")) {
						count++;
					}
				}
			}
		}
		psmReader.close();
		System.out.println(count);
	}
	
	private static void getGenomeCount(String fasta) throws IOException {
		int count = 0;
		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fasta));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
//				String[] cs = line.split("_");
//				set.add(cs[1]);

				count++;
			}
		}
		reader.close();
		System.out.println(set.size());
		System.out.println(count);
	}
	
	private static void getMissGenome(String db1, String db2, String peppro) throws IOException {
		HashSet<String> set1 = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(db1));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
				String[] cs = line.split("_");
				set1.add(cs[1]);
			}
		}
		reader.close();
		System.out.println(set1.size());

		HashSet<String> set2 = new HashSet<String>();
		reader = new BufferedReader(new FileReader(db2));
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {
				String[] cs = line.split("_");
				set2.add(cs[1]);
			}
		}
		reader.close();
		System.out.println(set2.size());

		HashSet<String> findset1 = new HashSet<String>();
		HashSet<String> findset2 = new HashSet<String>();
		HashSet<String> allSet = new HashSet<String>();

		reader = new BufferedReader(new FileReader(peppro));
		while ((line = reader.readLine()) != null) {
			String[] cs = line.split("\t");
			if (set1.contains("GENOME0" + cs[0])) {
				findset1.add(cs[1]);
			}
			if (set2.contains("GENOME0" + cs[0])) {
				findset2.add(cs[1]);
			}

			allSet.add(cs[1]);
		}
		reader.close();

		int count = 0;
		for (String seq : allSet) {
			if (findset1.contains(seq) || findset2.contains(seq)) {
				continue;
			}
			count++;
		}
		System.out.println(findset1.size() + "\t" + findset2.size() + "\t" + allSet.size() + "\t" + count);
	}
	
	public static void main(String[] args) throws IOException, SQLException {
		// TODO Auto-generated method stub
		
		HapPSMFilterTask.getMissGenome("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\rib_elon\\combined.fasta", 
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\rib_elon\\temp85_90.fasta", 
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_pep_pro.tsv");
		
		/*
		 * HapPSMFilterTask task = new HapPSMFilterTask(new
		 * File("Z:\\Kai\\HAP\\TMT20201020\\pFind.spectra"), new
		 * File("Z:\\Kai\\HAP\\TMT20201020\\psms.xml")); File[] rawsFiles = (new
		 * File("Z:\\Kai\\tmt\\TMT20201020")).listFiles(new FileFilter() {
		 * 
		 * @Override public boolean accept(File pathname) { // TODO Auto-generated
		 * method stub if (pathname.getName().endsWith("raw")) return true; return
		 * false; } }); task.filter(new
		 * File("Z:\\Microbiome\\db\\fasta\\human_IGC.pep.fasta"), rawsFiles, new
		 * File("Z:\\Kai\\HAP\\TMT20201020\\pia.xml"),
		 * "Z:\\Kai\\HAP\\TMT20201020\\pep.csv", "Z:\\Kai\\HAP\\TMT20201020\\pro.csv");
		 * 
		 * // task.filter(new File("Z:\\Microbiome\\db\\fasta\\human_IGC.pep.fasta"),
		 * rawsFiles);
		 * 
		 */
		/*
		 * HapPSMFilterTask task = new HapPSMFilterTask(new
		 * File("Z:\\Kai\\HAP\\Metalab_test\\pFind.spectra"), new
		 * File("Z:\\Kai\\HAP\\Metalab_test\\psms.xml")); File[] rawsFiles = (new
		 * File("Y:\\Kai\\Metalab_test")).listFiles(new FileFilter() {
		 * 
		 * @Override public boolean accept(File pathname) { // TODO Auto-generated
		 * method stub if (pathname.getName().endsWith("raw")) return true; return
		 * false; } }); task.filter(new
		 * File("Z:\\Microbiome\\db\\fasta\\human_IGC.pep.fasta"), rawsFiles, new
		 * File("Z:\\Kai\\HAP\\Metalab_test\\pia.xml"),
		 * "Z:\\Kai\\HAP\\Metalab_test\\pep.csv",
		 * "Z:\\Kai\\HAP\\Metalab_test\\pro.csv");
		 */

//		HapPSMFilterTask.getIdRate("Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind.summary",
//				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\pFind.filterQ.spectra");
		
//		HapPSMFilterTask.toSingleCharge("Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind.spectra", 
//				"Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind.singleCharge.spectra");
/*
		HapPSMFilterTask.comparePSM(
				"Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\finalScore.compare.csv");
*/
//		HapPSMFilterTask.getDecoyCount("Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind-Filtered.spectra");
//		HapPSMFilterTask.getDecoyCount("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\pFind-Filtered.spectra");
		
//		HapPSMFilterTask.getGenomeCount("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\hap\\hap.fasta");
		
		HapPSMFilterTask.comparePSM(
				"Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\finalScore.compare.csv");
/*		
		HapPSMFilterTask.comparePSM(
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\genome_80_85\\pFind-Filtered.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\pFind.filterQ.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\genome_80_85\\finalScore.compare.csv");
		
		HapPSMFilterTask.comparePSM(
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\genome_80_85\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\pFind.spectra",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\genome_80_85\\finalScore.compare.csv");
*/		
//		HapPSMFilterTask.getGenomeCount("Z:\\Kai\\tmt\\TMT20201020_labelFree\\db_generation_msCluster\\sample_specific_db.fasta");
//		HapPSMFilterTask.getGenomeCount("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\rib_elon\\combined.fasta");
/*		
		HapPSMFilterTask.compareSequence("Z:\\Kai\\tmt\\TMT20201020_labelFree\\pfind_open_search_new\\pFind-Filtered.spectra", 
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\pFind-Filtered.spectra", 
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\pFind.spectra", 
				"D:\\Data\\new_db_4\\catalog.db",
				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_all_pep.tsv");
*/		
//		HapPSMFilterTask.missPepAnalysis("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_pep_pro.tsv");
		
//		HapPSMFilterTask.getMissTaxon("Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_all_pep.tsv", 
//				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_pep_pro.tsv", 
//				"Z:\\Kai\\HAP\\Krystal_LSARP_09112020\\combined_80\\miss_lca.tsv");
				
	}

}
