/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.expasy.mzjava.proteomics.mol.Peptide;
import org.expasy.mzjava.proteomics.mol.Protein;
import org.expasy.mzjava.proteomics.mol.digest.ProteinDigester;

/**
 * @author Kai Cheng
 *
 */
public class Pep2GiDbCallable implements Callable<Object> {

	private Protein protein;
	private ProteinDigester digester;
//	private PrintWriter[][] tempWriters;
	private HashMap<String, HashSet<String>>[][] maps;

	public Pep2GiDbCallable(Protein protein, ProteinDigester digester, PrintWriter[][] tempWriters,
			HashMap<String, HashSet<String>>[][] maps) {
		this.protein = protein;
		this.digester = digester;
//		this.tempWriters = tempWriters;
		this.maps = maps;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub

		try {

			String accession = protein.getAccessionId();
			String[] sp = accession.split("\\|");
			if (sp.length > 1) {
				accession = sp[1];
			}

			List<Peptide> peptideList = digester.digest(protein);

			for (Peptide pep : peptideList) {
				int length = pep.size();
				if (length < 6 || length > 30)
					continue;

				String sequence = pep.toSymbolString();
				int idI = sequence.charAt(0) - 'A';
				int idJ = length - 6;

				if (maps[idI][idJ].containsKey(sequence)) {
					maps[idI][idJ].get(sequence).add(accession);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(accession);
					maps[idI][idJ].put(sequence, set);
				}
			}
			return null;

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}

}
