package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;

public class MicroMonosaccharides {

	private boolean nGlycan;
	private Glycosyl[] glycosyls;
	private int[] ids;
	private double[] sortedMasses;
	private HashMap<Double, Glycosyl[]> OxiniumMap;
	private double[] oxinuimMasses;
	private boolean hasNeuAc;

	private int combineCount = 4;
	private double massLimit = 1000;
	private double[] combineGlycosylMasses;
	private HashMap<Double, int[]> combineCompositions;

	/**
	 * 204, 366, 569, 731, 893
	 */
	private static final double oxoUpperLimit = 900;
	private static final DecimalFormat df4 = new DecimalFormat("#.####");

	public MicroMonosaccharides(Glycosyl[] glycosyls) {

		this.glycosyls = glycosyls;
		this.sortedMasses = new double[glycosyls.length];
		this.ids = new int[glycosyls.length];

		for (int i = 0; i < glycosyls.length; i++) {
			sortedMasses[i] = glycosyls[i].getMonoMass();
		}

		Arrays.sort(sortedMasses);
		for (int i = 0; i < sortedMasses.length; i++) {
			for (int j = 0; j < glycosyls.length; j++) {
				if (this.sortedMasses[i] == glycosyls[j].getMonoMass()) {
					ids[i] = j;
					sortedMasses[i] = Double.parseDouble(df4.format(sortedMasses[i]));
				}
			}
		}

		boolean hasNexNAc = false;
		boolean hasNex = false;
		this.OxiniumMap = new HashMap<Double, Glycosyl[]>();
		for (int i = 0; i < glycosyls.length; i++) {

			if (glycosyls[i] == Glycosyl.HexNAc) {
				hasNexNAc = true;
			} else if (glycosyls[i] == Glycosyl.Hex) {
				hasNex = true;
			}

			if (glycosyls[i].getMonoMass() < 200)
				continue;
			if (glycosyls[i] == Glycosyl.NeuAc) {
				OxiniumMap.put(
						Double.parseDouble(df4.format(Glycosyl.NeuAc.getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { Glycosyl.NeuAc });
				OxiniumMap.put(
						Double.parseDouble(df4.format(Glycosyl.NeuAc_H2O.getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { Glycosyl.NeuAc });
				hasNeuAc = true;
			} else {
				OxiniumMap.put(Double.parseDouble(df4.format(glycosyls[i].getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { glycosyls[i] });
			}
		}

		for (int i = 0; i < glycosyls.length; i++) {
			for (int j = i; j < glycosyls.length; j++) {
				double combine = glycosyls[i].getMonoMass() + glycosyls[j].getMonoMass() + AminoAcidProperty.PROTON_W;
				if (combine < oxoUpperLimit) {
					OxiniumMap.put(Double.parseDouble(df4.format(combine)),
							new Glycosyl[] { glycosyls[i], glycosyls[j] });
				}
			}
		}

		if (hasNexNAc && hasNex) {
			double monoHexNAc = Glycosyl.HexNAc.getMonoMass();
			double monoHex = Glycosyl.Hex.getMonoMass();
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex });
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex * 2 + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.Hex });
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex * 3 + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.Hex, Glycosyl.Hex });

			if (hasNeuAc) {
				OxiniumMap.put(
						Double.parseDouble(df4.format(
								monoHexNAc + monoHex + Glycosyl.NeuAc.getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc });
			}
		}

		this.oxinuimMasses = new double[this.OxiniumMap.size()];
		int id = 0;
		for (Double mass : this.OxiniumMap.keySet()) {
			this.oxinuimMasses[id++] = mass;
		}
		Arrays.sort(this.oxinuimMasses);

		this.initialCombineCount();
	}
	
	public MicroMonosaccharides(Glycosyl[] glycosyls, boolean nGlycan) {

		this.glycosyls = glycosyls;
		this.sortedMasses = new double[glycosyls.length];
		this.ids = new int[glycosyls.length];

		for (int i = 0; i < glycosyls.length; i++) {
			sortedMasses[i] = glycosyls[i].getMonoMass();
		}

		Arrays.sort(sortedMasses);
		for (int i = 0; i < sortedMasses.length; i++) {
			for (int j = 0; j < glycosyls.length; j++) {
				if (this.sortedMasses[i] == glycosyls[j].getMonoMass()) {
					ids[i] = j;
					sortedMasses[i] = Double.parseDouble(df4.format(sortedMasses[i]));
				}
			}
		}

		boolean hasNexNAc = false;
		boolean hasNex = false;
		this.OxiniumMap = new HashMap<Double, Glycosyl[]>();
		for (int i = 0; i < glycosyls.length; i++) {

			if (glycosyls[i] == Glycosyl.HexNAc) {
				hasNexNAc = true;
			} else if (glycosyls[i] == Glycosyl.Hex) {
				hasNex = true;
			}

			if (glycosyls[i].getMonoMass() < 200)
				continue;
			if (glycosyls[i] == Glycosyl.NeuAc) {
				OxiniumMap.put(
						Double.parseDouble(df4.format(Glycosyl.NeuAc.getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { Glycosyl.NeuAc });
				OxiniumMap.put(
						Double.parseDouble(df4.format(Glycosyl.NeuAc_H2O.getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { Glycosyl.NeuAc });
				hasNeuAc = true;
			} else {
				OxiniumMap.put(Double.parseDouble(df4.format(glycosyls[i].getMonoMass() + AminoAcidProperty.PROTON_W)),
						new Glycosyl[] { glycosyls[i] });
			}
		}

		for (int i = 0; i < glycosyls.length; i++) {
			for (int j = i; j < glycosyls.length; j++) {
				double combine = glycosyls[i].getMonoMass() + glycosyls[j].getMonoMass() + AminoAcidProperty.PROTON_W;
				if (combine < oxoUpperLimit) {
					OxiniumMap.put(Double.parseDouble(df4.format(combine)),
							new Glycosyl[] { glycosyls[i], glycosyls[j] });
				}
			}
		}

		if (hasNexNAc && hasNex) {
			double monoHexNAc = Glycosyl.HexNAc.getMonoMass();
			double monoHex = Glycosyl.Hex.getMonoMass();
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex });
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex * 2 + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.Hex });
			OxiniumMap.put(Double.parseDouble(df4.format(monoHexNAc * 2 + monoHex * 3 + AminoAcidProperty.PROTON_W)),
					new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.Hex, Glycosyl.Hex });
		}

		this.oxinuimMasses = new double[this.OxiniumMap.size()];
		int id = 0;
		for (Double mass : this.OxiniumMap.keySet()) {
			this.oxinuimMasses[id++] = mass;
		}
		Arrays.sort(this.oxinuimMasses);

		this.initialCombineCount();
	}

	public int getGlycoTypeCount() {
		return glycosyls.length;
	}

	public double[] getSortedMasses() {
		return sortedMasses;
	}

	public int[] getOriginalComposition(int[] comp) {
		int[] originalComp = new int[comp.length];
		for (int i = 0; i < originalComp.length; i++) {
			originalComp[ids[i]] = comp[i];
		}
		return originalComp;
	}

	public String getGlycanName(int[] comp) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < comp.length; i++) {
			if (comp[i] > 0) {
				String unit = glycosyls[i].getTitle();
				sb.append(unit);
				sb.append("(").append(comp[i]).append(")");
			}
		}
		return sb.toString();
	}

	public double getGlycanMass(int[] comp) {
		double total = 0;
		for (int i = 0; i < comp.length; i++) {
			total += comp[i] * this.sortedMasses[i];
		}
		return total;
	}

	public double[] getOxoniumMasses() {
		return this.oxinuimMasses;
	}

	public Glycosyl[] getGlycoFromOxo(double oxoniumMass) {
		return this.OxiniumMap.get(oxoniumMass);
	}

	public HashMap<Double, Glycosyl[]> getOxiniumMap() {
		return OxiniumMap;
	}

	public void setCombineCount(int combineCount) {
		this.combineCount = combineCount;
		this.initialCombineCount();
	}

	private void initialCombineCount() {
		this.combineCompositions = new HashMap<Double, int[]>();
		this.add(combineCompositions, combineCount);

		this.combineGlycosylMasses = new double[this.combineCompositions.size()];
		int id = 0;
		for (Double mass : this.combineCompositions.keySet()) {
			this.combineGlycosylMasses[id++] = mass;
		}
		Arrays.sort(this.combineGlycosylMasses);
	}

	private HashMap<Double, int[]> add(HashMap<Double, int[]> combineMap, int combineCount) {

		HashMap<Double, int[]> newCombineMap = new HashMap<Double, int[]>();

		if (combineCount == 1) {
			for (int i = 0; i < sortedMasses.length; i++) {
				int[] composition = new int[this.sortedMasses.length];
				composition[i] = 1;
				newCombineMap.put(this.sortedMasses[i], composition);
			}
		} else {
			HashMap<Double, int[]> tempMap = this.add(combineMap, combineCount - 1);
			Double[] masses = tempMap.keySet().toArray(new Double[tempMap.size()]);
			for (int i = 0; i < masses.length; i++) {
				int[] composition = tempMap.get(masses[i]);
				for (int j = 0; j < this.sortedMasses.length; j++) {
					int[] newcomposition = composition.clone();
					newcomposition[j]++;
					double commass = masses[i] + sortedMasses[j];
					if (commass > massLimit)
						continue;
					newCombineMap.put(Double.parseDouble(df4.format(commass)), newcomposition);
				}
			}

		}
		combineMap.putAll(newCombineMap);
		return newCombineMap;
	}

	public int getCombineCount() {
		return combineCount;
	}

	public double[] getCombineGlycosylMasses() {
		return combineGlycosylMasses;
	}

	public HashMap<Double, int[]> getCombineCompositions() {
		return combineCompositions;
	}

	public Glycosyl[] getGlycosyls() {
		return glycosyls;
	}

	public Glycosyl getGlycosylFromId(int id) {
		return this.glycosyls[this.ids[id]];
	}

	public int[] getIds() {
		return ids;
	}

	public boolean hasNeuAc() {
		return hasNeuAc;
	}

}
