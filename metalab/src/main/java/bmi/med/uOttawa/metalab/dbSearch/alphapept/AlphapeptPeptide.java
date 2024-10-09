package bmi.med.uOttawa.metalab.dbSearch.alphapept;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

public class AlphapeptPeptide extends MetaPeptide {

	private String modseq;
	private String[] proteins;

	public AlphapeptPeptide(String sequence, int length, int missCleave, double mass, double score, int[] charges,
			String[] proteins, int[] spCount, double[] intensity) {
		super(sequence, length, charges, missCleave, mass, score, intensity);
		// TODO Auto-generated constructor stub
		this.modseq = sequence;
		this.proteins = proteins;
		this.ms2Counts = spCount;
		this.intensity = intensity;
	}

	public AlphapeptPeptide(String sequence, String modSeq, int length, int missCleave, double mass, double score,
			int[] charges, String[] proteins, double[] intensity, int[] spCount) {
		super(sequence, length, charges, missCleave, mass, score, intensity);
		// TODO Auto-generated constructor stub
		this.modseq = modSeq;
		this.proteins = proteins;
		this.ms2Counts = spCount;
	}

	public String getProteinString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < proteins.length; i++) {
			sb.append(proteins[i]).append(";");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}
	}

	@Override
	public Object[] getTableObjects() {
		// TODO Auto-generated method stub
		Object[] objs;
		if (proteins != null && proteins.length > 0) {
			objs = new Object[3 + intensity.length * 2];
			objs[0] = sequence;
			objs[1] = getProteinString();
			double total = 0;
			for (int i = 0; i < intensity.length; i++) {
				objs[i + 3] = intensity[i];
				total += intensity[i];
			}
			objs[2] = total;

			for (int i = 0; i < ms2Counts.length; i++) {
				objs[i + 3 + intensity.length] = ms2Counts[i];
			}
		} else {
			objs = new Object[2 + intensity.length * 2];
			objs[0] = sequence;
			double total = 0;
			for (int i = 0; i < intensity.length; i++) {
				objs[i + 2] = intensity[i];
				total += intensity[i];
			}
			objs[1] = total;

			for (int i = 0; i < ms2Counts.length; i++) {
				objs[i + 2 + intensity.length] = ms2Counts[i];
			}
		}

		return objs;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub

		Element ePep = DocumentFactory.getInstance().createElement("Peptide");

		ePep.addAttribute("sequence", sequence);

		StringBuilder prosb = new StringBuilder();
		if (proteins != null && proteins.length > 0) {
			for (String protein : proteins) {
				prosb.append(protein).append(">");
			}
			if (prosb.length() > 0)
				prosb.deleteCharAt(prosb.length() - 1);
		}

		ePep.addAttribute("proteins", prosb.toString());

		StringBuilder intensitySb = new StringBuilder();
		for (double inten : intensity) {
			intensitySb.append(inten).append("_");
		}
		if (intensitySb.length() > 0)
			intensitySb.deleteCharAt(intensitySb.length() - 1);

		ePep.addAttribute("intensities", intensitySb.toString());

		StringBuilder spCountSb = new StringBuilder();
		for (int count : ms2Counts) {
			spCountSb.append(count).append("_");
		}
		if (spCountSb.length() > 0)
			spCountSb.deleteCharAt(spCountSb.length() - 1);
		ePep.addAttribute("spCounts", spCountSb.toString());

		ePep.addAttribute("lcaId", String.valueOf(lcaId));
		if (taxonIds != null) {
			StringBuilder taxonIdSb = new StringBuilder();
			for (int i = 0; i < taxonIds.length; i++) {
				taxonIdSb.append(taxonIds[i]).append("_");
			}
			if (taxonIdSb.length() > 0)
				taxonIdSb.deleteCharAt(taxonIdSb.length() - 1);

			ePep.addAttribute("taxonIds", taxonIdSb.toString());
		}

		return ePep;
	}

	public String[] getProteins() {
		return proteins;
	}

	public String getModSeq() {
		return modseq;
	}

}
