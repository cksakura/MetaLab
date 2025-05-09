package bmi.med.uOttawa.metalab.dbSearch.alphapept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class AlphapeptPepReader extends AbstractMetaPeptideReader {

	protected BufferedReader reader;
	protected int sequenceId = -1;
	protected int modSeqId = -1;
	protected int missId = -1;
	protected int massId = -1;
	protected int scoreId = -1;
	protected int proteinId = -1;
	protected int chargeId = -1;
	protected int lengthId = -1;
	protected int intensityId = -1;
	protected int fileSNameId = -1;
	protected int targetId = -1;

	protected HashMap<String, Integer> fileNameIdMap;
	protected String[] fileNames;
	protected String[] title;
	protected String line;

	private static Logger LOGGER = LogManager.getLogger(AlphapeptPepReader.class);

	public AlphapeptPepReader(String in, String[] fileNames) {
		this(new File(in), fileNames);
		// TODO Auto-generated constructor stub
	}

	public AlphapeptPepReader(File in, String[] fileNames) {
		super(in);
		// TODO Auto-generated constructor stub
		try {
			this.reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + in, e);
		}
		this.fileNames = fileNames;
		this.fileNameIdMap = new HashMap<String, Integer>();
		for (int i = 0; i < fileNames.length; i++) {
			this.fileNameIdMap.put(fileNames[i], i);
		}
		this.parseTitle();
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		String line = null;
		try {
			line = reader.readLine();
			this.title = line.split(AlphapeptProReader.delimiter);
			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("sequence_naked")) {
					sequenceId = i;
				} else if (title[i].equals("sequence")) {
					modSeqId = i;
				} else if (title[i].equals("n_missed")) {
					missId = i;
				} else if (title[i].equals("mass_db")) {
					massId = i;
				} else if (title[i].equals("score")) {
					scoreId = i;
				} else if (title[i].equals("protein")) {
					proteinId = i;
				} else if (title[i].equals("charge")) {
					chargeId = i;
				} else if (title[i].equals("n_AA")) {
					lengthId = i;
				} else if (title[i].equals("ms1_int_sum_apex_dn")) {
					intensityId = i;
				} else if (title[i].equals("shortname")) {
					fileSNameId = i;
				} else if (title[i].equals("target_precursor")) {
					targetId = i;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading Fragpipe search result file " + super.getFile(), e);
		}
	}

	protected void parse(HashMap<String, AlphapeptPeptide> pepMap) {
		String[] cs = line.split(AlphapeptProReader.delimiter);
		if (cs[targetId].equals("FALSE")) {
			return;
		}
		String seqString = cs[sequenceId];
		String modSeq = modSeqId > -1 ? cs[modSeqId] : seqString;
		double intensity = Double.parseDouble(cs[intensityId]);
		int fileId = fileNameIdMap.get(cs[fileSNameId]);
		int charge = Integer.parseInt(cs[chargeId].split("\\.")[0]);

		if (pepMap.containsKey(modSeq)) {
			AlphapeptPeptide alphaPep = pepMap.get(modSeq);
			double[] intensities = alphaPep.getIntensity();
			intensities[fileId] += intensity;
			int[] spCounts = alphaPep.getMs2Counts();
			spCounts[fileId]++;
			int[] charges = alphaPep.getCharges();

			boolean findCharge = false;
			ArrayList<Integer> chargeList = new ArrayList<Integer>();
			for (int i = 0; i < charges.length; i++) {
				if (charges[i] == charge) {
					findCharge = true;
					break;
				}
				chargeList.add(charges[i]);
			}
			if (!findCharge) {
				chargeList.add(charge);
				charges = new int[chargeList.size()];
				for (int i = 0; i < charges.length; i++) {
					charges[i] = chargeList.get(i);
				}

				Arrays.sort(charges);
				alphaPep.setCharges(charges);
			}
		} else {
			int length = Integer.parseInt(cs[lengthId]);
			int miss = Integer.parseInt(cs[missId]);
			double mass = Double.parseDouble(cs[massId]);
			double score = Double.parseDouble(cs[scoreId]);
			double[] intensities = new double[fileNames.length];
			intensities[fileId] += intensity;
			int[] spCounts = new int[fileNames.length];
			spCounts[fileId]++;
			int[] charges = new int[] { charge };

			String[] pros;
			if (cs[proteinId].charAt(0) == '"') {
				pros = cs[proteinId].substring(1, cs[proteinId].length() - 1).split(",");
			} else {
				pros = new String[] { cs[proteinId] };
			}

			AlphapeptPeptide alphaPep = new AlphapeptPeptide(seqString, modSeq, length, miss, mass, score, charges,
					pros, intensities, spCounts);
			pepMap.put(modSeq, alphaPep);
		}
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return MetaConstants.labelFree;
	}

	@Override
	public AlphapeptPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		HashMap<String, AlphapeptPeptide> pepMap = new HashMap<String, AlphapeptPeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				parse(pepMap);
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		AlphapeptPeptide[] peps = pepMap.values().toArray(new AlphapeptPeptide[pepMap.size()]);
		return peps;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		Object[] titleObjs = new Object[3 + fileNames.length + fileNames.length];
		titleObjs[0] = this.title[sequenceId];
		titleObjs[1] = this.title[proteinId];
		titleObjs[2] = "Intensity";
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + 3] = "Intensity " + fileNames[i];
		}
		for (int i = 0; i < fileNames.length; i++) {
			titleObjs[i + fileNames.length + 3] = "Spectral Count" + fileNames[i];
		}
		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		String[] intensityTitles = new String[this.fileNames.length];
		for (int i = 0; i < intensityTitles.length; i++) {
			intensityTitles[i] = "Intensity " + fileNames[i];
		}
		return intensityTitles;
	}

	@SuppressWarnings("unused")
	private static void test(String in, String[] names) {
		AlphapeptPepReader apReader = new AlphapeptPepReader(in, names);
		try {
			HashMap<String, Integer> countMap = new HashMap<String, Integer>();
			HashMap<String, HashSet<String>> pepMap = new HashMap<String, HashSet<String>>();
			for (int i = 0; i < names.length; i++) {
				countMap.put(names[i], 0);
				pepMap.put(names[i], new HashSet<String>());
			}

			int totalCount = 0;
			while ((apReader.line = apReader.reader.readLine()) != null) {
				String[] cs = apReader.line.split(AlphapeptProReader.delimiter);
				if (cs[apReader.targetId].equals("FALSE")) {
					return;
				}
				String seqString = cs[apReader.sequenceId];
				String fileName = cs[apReader.fileSNameId];
				countMap.put(fileName, countMap.get(fileName) + 1);
				pepMap.get(fileName).add(seqString);
				totalCount++;
			}
			apReader.reader.close();

			System.out.println(totalCount);
			for (int i = 0; i < names.length; i++) {
				System.out.println(names[i] + "\t" + countMap.get(names[i]) + "\t" + pepMap.get(names[i]).size());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
	}

	@SuppressWarnings("unused")
	private static void compare(String in, String fragpipe, String alphadb) {
		AlphapeptPepReader apReader = new AlphapeptPepReader(in, new String[] {});
		try {

			HashSet<String> pepSet = new HashSet<String>();
			int totalCount = 0;
			while ((apReader.line = apReader.reader.readLine()) != null) {
				String[] cs = apReader.line.split(AlphapeptProReader.delimiter);
				if (cs[apReader.targetId].equals("FALSE")) {
					return;
				}
				String seqString = cs[apReader.sequenceId];
				pepSet.add(seqString);
				totalCount++;
			}
			apReader.reader.close();

			HashSet<String> fpSet = new HashSet<String>();
			BufferedReader fpReader = new BufferedReader(new FileReader(fragpipe));
			String fpLine = fpReader.readLine();
			while ((fpLine = fpReader.readLine()) != null) {
				String[] cs = fpLine.split("\t");
				if (!pepSet.contains(cs[0])) {
					fpSet.add(cs[7]);
				}
			}
			fpReader.close();

			int findCount = 0;
			BufferedReader fastaReader = new BufferedReader(new FileReader(alphadb));
			String line = fastaReader.readLine();
			while ((line = fastaReader.readLine()) != null) {
				if (line.startsWith(">")) {
					String proname = line.substring(1, line.indexOf(" "));
					if (fpSet.contains(proname)) {
						findCount++;
					}
				}
			}
			fastaReader.close();
			System.out.println(totalCount + "\t" + pepSet.size() + "\t" + fpSet.size() + "\t" + findCount);
		} catch (IOException e) {
			// TODO Auto-generated catch block

		}
	}
}
