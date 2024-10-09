package bmi.med.uOttawa.metalab.task.io.pep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPep4Meta;

public class MetaPeptideTsvReader extends AbstractMetaPeptideReader {

	private MetaPeptide[] peptides;
	private String[] intensityTitle;

	public MetaPeptideTsvReader(String in) {
		super(in);
		// TODO Auto-generated constructor stub
		this.read();
	}

	public MetaPeptideTsvReader(File in) {
		super(in);
		// TODO Auto-generated constructor stub
		this.read();
	}

	private void read() {
		ArrayList<MetaPeptide> list = new ArrayList<MetaPeptide>();
		try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
			String line = reader.readLine();
			String[] title = line.split("\t");
			int seqId = -1;
			int modSeqId = -1;
			int lengthId = -1;
			int prosId = -1;
			int chargeId = -1;
			int scoreId = -1;
			int ms2Id = -1;
			HashMap<Integer, String> intenIdMap = new HashMap<Integer, String>();

			for (int i = 0; i < title.length; i++) {
				if (title[i].equals("Sequence")) {
					modSeqId = i;
				} else if (title[i].equals("Base sequence")) {
					seqId = i;
				} else if (title[i].equals("Length")) {
					lengthId = i;
				} else if (title[i].equals("Proteins")) {
					prosId = i;
				} else if (title[i].equals("Charges")) {
					chargeId = i;
				} else if (title[i].equals("PEP")) {
					scoreId = i;
				} else if (title[i].equals("MS/MS Count")) {
					ms2Id = i;
				} else if (title[i].startsWith("Intensity ")) {
					intenIdMap.put(i, title[i]);
				}
			}
			Integer[] ids = intenIdMap.keySet().toArray(Integer[]::new);
			Arrays.sort(ids);

			this.intensityTitle = new String[ids.length];
			for (int i = 0; i < ids.length; i++) {
				intensityTitle[i] = intenIdMap.get(ids[i]);
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				String sequence = "";
				if (modSeqId > -1) {
					sequence = cs[modSeqId];
				}

				String baseSeqString = sequence;
				if (seqId > -1) {
					baseSeqString = cs[seqId];
				}

				int length = lengthId > -1 ? Integer.parseInt(cs[lengthId]) : baseSeqString.length();
				String[] pros = prosId > -1 ? cs[prosId].split(";") : new String[] {};
				int[] charges;
				if (chargeId > -1) {
					String[] chargeStrings = cs[chargeId].split(";");
					charges = new int[chargeStrings.length];
					for (int i = 0; i < charges.length; i++) {
						charges[i] = Integer.parseInt(chargeStrings[i]);
					}
				} else {
					charges = new int[] {};
				}

				double[] intensity = new double[intenIdMap.size()];
				for (int i = 0; i < intensity.length; i++) {
					intensity[i] = Double.parseDouble(cs[ids[i]]);
				}

				double score = scoreId > -1 ? Double.parseDouble(cs[scoreId]) : 0.0;
				int ms2Count = ms2Id > -1 ? Integer.parseInt(cs[ms2Id]) : 0;

				MaxquantPep4Meta peptide = new MaxquantPep4Meta(baseSeqString, length, charges, 0, 0, score, ms2Count,
						score, pros, false, false, intensity);
				list.add(peptide);
			}
			reader.close();
		} catch (IOException e) {

		}

		peptides = list.toArray(new MetaPeptide[list.size()]);
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		return peptides;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return intensityTitle;
	}
	
	public static void main(String[] args) {
		MetaPeptideTsvReader reader = new MetaPeptideTsvReader("Z:\\Kai\\Raw_files\\run21\\DDA\\MetaLab\\final_peptides.tsv");
		System.out.println(reader.getMetaPeptides().length);
	}

}
