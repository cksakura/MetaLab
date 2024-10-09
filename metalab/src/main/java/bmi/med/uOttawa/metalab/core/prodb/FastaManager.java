/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;

/**
 * @author Kai Cheng
 *
 */
public class FastaManager {
	
	public static String shuffle = "shuf_";

	public static void split(String in, int part) throws IOException {

		File fastaFile = new File(in);
		String name = fastaFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		PrintWriter[] writers = new PrintWriter[part];
		for (int i = 0; i < writers.length; i++) {
			File outputFile = new File(fastaFile.getParent(), name + "_" + (i + 1) + ".fasta");
			writers[i] = new PrintWriter(outputFile);
		}

		int currentId = -1;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if(line.trim().length()==0) {
				continue;
			}
			if (line.startsWith(">")) {
				currentId++;
				if (currentId == part) {
					currentId = 0;
				}
				writers[currentId].println(line);
			} else {
				writers[currentId].println(line);
			}
		}
		reader.close();

		for (int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
	}
	
	public static void combine(String input, String output) throws IOException {
		File[] files = (new File(input)).listFiles();
		PrintWriter writer = new PrintWriter(output);
		for (File in : files) {
			BufferedReader reader = new BufferedReader(new FileReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			reader.close();
		}
		writer.close();
	}
	
	public static void combine(String[] input, String output) throws IOException {
		HashSet<String> set = new HashSet<String>();
		PrintWriter writer = new PrintWriter(output);
		for (String in : input) {
			BufferedReader reader = new BufferedReader(new FileReader(in));
			String line = null;
			boolean use = false;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					String ref = line.substring(1);
					if (set.contains(ref)) {
						use = false;
					} else {
						use = true;
						set.add(ref);
						writer.println(line);
					}
				} else {
					if (use) {
						writer.println(line);
					}
				}
			}
			reader.close();
		}
		writer.close();
	}

	public static void addDecoy(String in, String out) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		StringBuilder sb = null;
		String rev = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {

				if (rev != null) {
					writer.println(rev);
					writer.println(sb.reverse());
				}

				sb = new StringBuilder();
				rev = ">rev_" + line.substring(1);
				writer.println(line);

			} else {
				sb.append(line);
				writer.println(line);
			}
		}

		if (rev != null) {
			writer.println(rev);
			writer.println(sb.reverse());
		}

		reader.close();
		writer.close();
	}

	public static void generateDecoy(String in, String out) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		StringBuilder sb = null;
		String rev = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {

				if (rev != null) {
					writer.println(rev);
					writer.println(sb.reverse());
				}

				sb = new StringBuilder();
				rev = ">REV_" + line.substring(1);

			} else {
				sb.append(line);
			}
		}

		if (rev != null) {
			writer.println(rev);
			writer.println(sb.reverse());
		}

		reader.close();
		writer.close();
	}
	
	public static void addShuffle(String in, String out) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);
		String line = null;
		StringBuilder sb = null;
		String shuf = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith(">")) {

				if (shuf != null) {
					writer.println(shuf);
					writer.println(generateTrypticShuffledSequence(sb.toString()));
				}

				sb = new StringBuilder();
				shuf = ">" + shuffle + line.substring(1);
				writer.println(line);

			} else {
				sb.append(line);
				writer.println(line);
			}
		}

		if (shuf != null) {
			writer.println(shuf);
			writer.println(generateTrypticShuffledSequence(sb.toString()));
		}

		reader.close();
		writer.close();
	}
	
	private static void digest(String in, String out) throws IOException {

		File[] files = new File[26];
		for (char amino = 'A'; amino <= 'Z'; amino++) {
			HashSet<String>[] sets = new HashSet[26];
			for (int i = 0; i < sets.length; i++) {
				sets[i] = new HashSet<String>(10000000);
			}
			int proCount = 0;

			File file = new File(in);
			FastaReader fReader = new FastaReader(in);
			ProteinItem[] items = fReader.getItems();
			for (int i = 0; i < items.length; i++) {
				String sequence = items[i].getSequence();
				String[] peps = Enzyme.Trypsin.digest(sequence, 2, 6);
				for (int j = 0; j < peps.length; j++) {
					char aa = peps[j].charAt(0);
					int length = peps[j].length();
					if (aa == amino) {
						if (length <= 6) {
							sets[0].add(peps[j]);
						} else if (length > 30) {
							sets[25].add(peps[j]);
						} else {
							sets[length - 6].add(peps[j]);
						}
					}
				}

				if (proCount++ % 10000 == 0) {
					System.out.println(proCount);
				}
			}

			System.out.println(amino + "_finish");

			files[amino - 'A'] = new File(file.getParent(), file.getName() + "_" + amino + ".fasta");
			PrintWriter writer = new PrintWriter(files[amino - 'A']);
			int count = 0;

			for (int i = 0; i < sets.length; i++) {
				for (String pep : sets[i]) {
					writer.println(">" + amino + "_" + (++count));
					writer.println(pep);
				}
			}
			writer.close();
		}

		PrintWriter writer = new PrintWriter(out);
		for (int i = 0; i < files.length; i++) {
			BufferedReader reader = new BufferedReader(new FileReader(files[i]));
			String line = null;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
			reader.close();
		}
		writer.close();
	}

	private static final Random random = new Random();

	public static String generateTrypticShuffledSequence(String sequence) {
		// Shuffle the entire sequence
		List<Character> sequenceChars = new ArrayList<>();
		for (char c : sequence.toCharArray()) {
			if (c != 'K' && c != 'R') {
				sequenceChars.add(c);
			}
		}
		Collections.shuffle(sequenceChars, random);

        // Convert shuffled characters back to a string
        StringBuilder shuffledSequence = new StringBuilder(sequenceChars.size());
        for (Character c : sequenceChars) {
            shuffledSequence.append(c);
        }

        // Find positions of lysine (K) and arginine (R) residues in the original sequence
        List<Integer> cleavagePositions = new ArrayList<>();
        for (int i = 0; i < sequence.length(); i++) {
            char residue = sequence.charAt(i);
            if (residue == 'K' || residue == 'R') {
                cleavagePositions.add(i);
            }
        }

        // Insert K and R residues into the shuffled sequence at the same positions as in the original sequence
        for (int originalPos : cleavagePositions) {
            char residue = sequence.charAt(originalPos);
            shuffledSequence.insert(originalPos, residue);
        }

        return shuffledSequence.toString();
    }
    
	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
//		String originalSequence = "MAITYQTEGIKMPDIKKRETTEWIKAVAATYEKRIGEIAYIFCSDEKILEVNRQYLQHDYYTDIITFDYCEGNRLSGDLFISLETVKTNSEQFNTPYEEELHRTIIHGILHLCGINDKGPGEREIMEAAENKALAMRKQ";
//        String shuffledSequence = generateTrypticShuffledSequence(originalSequence);
//        System.out.println("Original Sequence: " + originalSequence);
//        System.out.println("Shuffled Sequence: " + shuffledSequence);
    
        
		FastaManager.addDecoy("Z:\\Kai\\Raw_files\\single_species\\8482\\uniprotkb_taxonomy_id_821_2024_09_06.fasta", 
				"Z:\\Kai\\Raw_files\\single_species\\8482\\uniprotkb_taxonomy_id_821_2024_09_06_TD.fasta");
		
//		FastaManager.addShuffle("Z:\\Kai\\Raw_files\\PXD008738\\fecal\\dia\\MetaLab_pep_lib_pfind\\mag_result\\combined.fasta", 
//				"Z:\\Kai\\Raw_files\\PXD008738\\fecal\\dia\\MetaLab_pep_lib_pfind\\mag_result\\shuf.combined.fasta");

//		FastaManager.split("Z:\\Kai\\Raw_files\\multiomics\\db\\prodigal-prot-HM454PCN.fa", 5);
		
//		FastaManager.digest("Z:\\Haonan\\DIA_eva\\database_overlap between DSM10507 and microbiome\\DSM10507.fasta", 
//				"Z:\\Haonan\\DIA_eva\\database_overlap between DSM10507 and microbiome\\DSM10507_peps.fasta");
		
//		FastaManager.combine("Z:\\Haonan\\DIA_eva\\database_overlap between DSM10507 and microbiome\\temp", 
//				"Z:\\Haonan\\DIA_eva\\database_overlap between DSM10507 and microbiome\\sample_specific_db_for48_peps.fasta");
	}
	
}
