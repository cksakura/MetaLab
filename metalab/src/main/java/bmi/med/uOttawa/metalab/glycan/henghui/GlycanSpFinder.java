package bmi.med.uOttawa.metalab.glycan.henghui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.stat.inference.TTest;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.tools.MwCalculator;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.spectra.Peak;
import bmi.med.uOttawa.metalab.spectra.Spectrum;
import bmi.med.uOttawa.metalab.spectra.io.MzxmlReader;

public class GlycanSpFinder {

	private static double ppm = 10;
	private static double cationNH4 = 17.026549;
	private static double cationK = 37.955882;
	private static double cationNa = 21.981943;

	private MwCalculator mwc;
	private double methyl;
	private HashMap<Double, int[]> map;

	public GlycanSpFinder() {
		this.map = this.getGlycanList();
		this.mwc = new MwCalculator();
	}

	private HashMap<Double, int[]> getGlycanList() {

		HashMap<Double, int[]> map = new HashMap<Double, int[]>();
		this.methyl = this.getMethylMod();

		for (int i = 2; i <= 8; i++) {
			double massHexNAc = Glycosyl.HexNAc.getMonoMass() * i;
			for (int j = 1; j <= 10; j++) {
				double massHex = Glycosyl.Hex.getMonoMass() * j;
				for (int k = 0; k <= 3; k++) {
					double massdHex = Glycosyl.dHex.getMonoMass() * k;
					for (int l = 0; l <= 4; l++) {
						double massNeuAc = (Glycosyl.NeuAc.getMonoMass() + methyl) * l;
						for (int m = 0; m <= 1; m++) {
							double massNeuGc = (Glycosyl.NeuGc.getMonoMass() + methyl) * m;

							if (j < 3 && l > 0) {
								continue;
							}

							if (i < 3 && l > 0) {
								continue;
							}

							if (i < 3 && m > 0) {
								continue;
							}

							if (j < 3 && m > 0) {
								continue;
							}

							if (l > j) {
								continue;
							}

							if (k > i) {
								continue;
							}

							if (j == 2 && i > j) {
								continue;
							}

							if (i - j > 2) {
								continue;
							}

							if (j > 1 && i > j) {

								double total = massHexNAc + massHex + massdHex + massNeuAc + massNeuGc
										+ AminoAcidProperty.MONOW_O + AminoAcidProperty.MONOW_H * 2;
								int[] comp = new int[] { i, j, k, l, m };
								map.put(total, comp);

								continue;
							}
						}
					}
				}
			}
		}

		return map;
	}

	private double getMethylMod() {
		double mass = AminoAcidProperty.MONOW_C + AminoAcidProperty.MONOW_H * 3 + AminoAcidProperty.MONOW_N
				- AminoAcidProperty.MONOW_O;
		return mass;
	}

	private void process(String in) throws IOException, XMLStreamException, DataFormatException {

		String out = in.substring(0, in.lastIndexOf(".")) + ".txt";
		PrintWriter writer = new PrintWriter(out);

		StringBuilder title = new StringBuilder();
		title.append("Scan").append("\t");
		title.append("Charge").append("\t");
		title.append("NH4").append("\t");
		title.append("Na").append("\t");
		title.append("K").append("\t");
		title.append("Retention time").append("\t");
		title.append("Precursor mz").append("\t");
		title.append("Glycan mass").append("\t");
		title.append(Glycosyl.HexNAc.getTitle()).append("\t");
		title.append(Glycosyl.Hex.getTitle()).append("\t");
		title.append(Glycosyl.dHex.getTitle()).append("\t");
		title.append(Glycosyl.NeuAc.getTitle()).append("\t");
		title.append(Glycosyl.NeuGc.getTitle());
		writer.println(title);

		Double[] masses = map.keySet().toArray(new Double[map.size()]);
		Arrays.sort(masses);

		MzxmlReader reader = new MzxmlReader(in);
		Spectrum spectrum;
		L: while ((spectrum = reader.getNextSpectrum()) != null) {
			int level = spectrum.getMslevel();
			if (level == 1) {

			} else if (level == 2) {

				int scan = spectrum.getScannum();
				double rt = spectrum.getRt();
				double premz = spectrum.getPrecursorMz();
				int charge = spectrum.getPrecursorCharge();

				StringBuilder sb = new StringBuilder();
				sb.append(scan).append("\t");
				sb.append(charge).append("\t");

				if (charge == 1) {

					if (premz > 1800) {
						continue;
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] - AminoAcidProperty.PROTON_W) / premz * 1E6 <= ppm) {

							sb.append("-\t-\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}

							writer.println(sb);
							continue L;
						}
					}
				} else if (charge == 2) {

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W) / premz * 1E6 <= ppm) {

							sb.append("-\t-\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationNH4 / 2.0) / premz
								* 1E6 <= ppm) {

							sb.append("+\t-\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationNa / 2.0) / premz
								* 1E6 <= ppm) {

							sb.append("-\t+\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationK / 2.0) / premz
								* 1E6 <= ppm) {

							sb.append("-\t-\t+\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

				} else if (charge == 3) {

					if (premz < 1800) {
						continue;
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W) / premz * 1E6 <= ppm) {

							sb.append("-\t-\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationNH4 / 3.0) / premz
								* 1E6 <= ppm) {

							sb.append("+\t-\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationNa / 3.0) / premz
								* 1E6 <= ppm) {

							sb.append("-\t+\t-\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}

					for (int i = 0; i < masses.length; i++) {
						if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationK / 3.0) / premz
								* 1E6 <= ppm) {

							sb.append("-\t-\t+\t");
							sb.append(rt).append("\t");
							sb.append(premz).append("\t");
							sb.append(masses[i]);

							int[] comp = map.get(masses[i]);
							for (int j = 0; j < comp.length; j++) {
								sb.append("\t").append(comp[j]);
							}
							writer.println(sb);
							continue L;
						}
					}
				}
			}
		}
		reader.close();
		writer.close();
	}

	private boolean judge(Spectrum spectrum) {
		boolean find204 = false;
		Peak[] peaks = spectrum.getPeaks();

		Arrays.sort(peaks);
		for (int i = 0; i < peaks.length; i++) {
			if (Math.abs(peaks[i].getMz() - Glycosyl.HexNAc.getMonoMass() - AminoAcidProperty.PROTON_W) < 0.1) {
				find204 = true;
			} else if (Math.abs(peaks[i].getMz() - Glycosyl.HexNAc.getMonoMass() - Glycosyl.Hex.getMonoMass()
					- AminoAcidProperty.PROTON_W) < 0.1) {
				if (find204) {
					return true;
				}
			} else if (peaks[i].getMz() - Glycosyl.HexNAc.getMonoMass() - Glycosyl.Hex.getMonoMass()
					- AminoAcidProperty.PROTON_W > 0.1) {
				return false;
			}
		}
		return false;
	}

	private void batchProcess(String in) throws IOException, XMLStreamException, DataFormatException {

		File dir = new File(in);
		File[] files = dir.listFiles();

		PrintWriter psmswriter = new PrintWriter(new File(in, "psms.tsv"));
		StringBuilder psmtitle = new StringBuilder();
		psmtitle.append("Raw file").append("\t");
		psmtitle.append("Sequence").append("\t");
		psmtitle.append("Modified sequence").append("\t");
		psmtitle.append("Mass").append("\t");
		psmtitle.append("Retention time").append("\t");
		psmtitle.append("Charge").append("\t");
		psmtitle.append("Proteins").append("\t");
		psmtitle.append("Scan").append("\t");
		psmtitle.append("Premz").append("\t");
		psmtitle.append(Glycosyl.HexNAc.getTitle()).append("\t");
		psmtitle.append(Glycosyl.Hex.getTitle()).append("\t");
		psmtitle.append(Glycosyl.dHex.getTitle()).append("\t");
		psmtitle.append(Glycosyl.NeuAc.getTitle()).append("\t");
		psmtitle.append(Glycosyl.NeuGc.getTitle());

		psmswriter.println(psmtitle);

		for (int fid = 0; fid < files.length; fid++) {
			String name = files[fid].getName();
			if (name.endsWith("mzXML")) {
				System.out.println(name);
				String fileName = name.substring(0, name.lastIndexOf("."));
				File out = new File(dir, fileName + ".txt");
				PrintWriter writer = new PrintWriter(out);

				StringBuilder title = new StringBuilder();
				title.append("Scan").append("\t");
				title.append("Charge").append("\t");
				title.append("NH4").append("\t");
				title.append("Na").append("\t");
				title.append("K").append("\t");
				title.append("Retention time").append("\t");
				title.append("Precursor mz").append("\t");
				title.append("Glycan mass").append("\t");
				title.append(Glycosyl.HexNAc.getTitle()).append("\t");
				title.append(Glycosyl.Hex.getTitle()).append("\t");
				title.append(Glycosyl.dHex.getTitle()).append("\t");
				title.append(Glycosyl.NeuAc.getTitle()).append("\t");
				title.append(Glycosyl.NeuGc.getTitle());
				writer.println(title);

				Double[] masses = map.keySet().toArray(new Double[map.size()]);
				Arrays.sort(masses);

				MzxmlReader reader = new MzxmlReader(files[fid]);
				Spectrum spectrum;
				L: while ((spectrum = reader.getNextSpectrum()) != null) {
					int level = spectrum.getMslevel();
					if (level == 1) {

					} else if (level == 2) {

						if (!this.judge(spectrum)) {
							continue;
						}

						int scan = spectrum.getScannum();
						double rt = spectrum.getRt();
						double premz = spectrum.getPrecursorMz();
						int charge = spectrum.getPrecursorCharge();
						double premass = (premz - AminoAcidProperty.PROTON_W) * charge;

						StringBuilder sb = new StringBuilder();
						sb.append(scan).append("\t");
						sb.append(charge).append("\t");

						if (charge == 1) {

							if (premz > 1800) {
								continue;
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] - AminoAcidProperty.PROTON_W) / premz * 1E6 <= ppm) {

									sb.append("-\t-\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}

									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 0);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}
						} else if (charge == 2) {

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W) / premz
										* 1E6 <= ppm) {

									sb.append("-\t-\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 0);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationNH4 / 2.0)
										/ premz * 1E6 <= ppm) {

									sb.append("+\t-\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 1);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationNa / 2.0)
										/ premz * 1E6 <= ppm) {

									sb.append("-\t+\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 2);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 2.0 - AminoAcidProperty.PROTON_W - cationK / 2.0)
										/ premz * 1E6 <= ppm) {

									sb.append("-\t-\t+\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 3);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

						} else if (charge == 3) {

							if (premz < 1800) {
								continue;
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W) / premz
										* 1E6 <= ppm) {

									sb.append("-\t-\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 0);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationNH4 / 3.0)
										/ premz * 1E6 <= ppm) {

									sb.append("+\t-\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 1);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationNa / 3.0)
										/ premz * 1E6 <= ppm) {

									sb.append("-\t+\t-\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 2);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}

							for (int i = 0; i < masses.length; i++) {
								if (Math.abs(premz - masses[i] / 3.0 - AminoAcidProperty.PROTON_W - cationK / 3.0)
										/ premz * 1E6 <= ppm) {

									sb.append("-\t-\t+\t");
									sb.append(rt).append("\t");
									sb.append(premz).append("\t");
									sb.append(masses[i]);

									int[] comp = map.get(masses[i]);
									for (int j = 0; j < comp.length; j++) {
										sb.append("\t").append(comp[j]);
									}
									writer.println(sb);

									FPeptide fp = this.translate(masses[i], comp, 3);
									StringBuilder pepsb = new StringBuilder();
									pepsb.append(fileName).append("\t");
									pepsb.append(fp.getSequence()).append("\t");
									pepsb.append(fp.getModseq()).append("\t");

									pepsb.append(fp.getTheoryMass()).append("\t");
									pepsb.append(rt).append("\t");
									pepsb.append(charge).append("\t");
									pepsb.append(fp.getProtein()).append("\t");
									pepsb.append(scan).append("\t");
									pepsb.append(premz);
									for (int j = 0; j < comp.length; j++) {
										pepsb.append("\t").append(comp[j]);
									}

									psmswriter.println(pepsb);

									continue L;
								}
							}
						}
					}
				}
				reader.close();
				writer.close();
			}
		}
		psmswriter.close();
	}

	private FPeptide translate(double glycomass, int[] comp, int cationId) {

		StringBuilder sb = new StringBuilder();
		double theoryMass = 0;
		for (int i = 0; i < comp[0]; i++) {
			sb.append("T");
		}
		theoryMass += Glycosyl.HexNAc.getMonoMass() * comp[0];

		for (int i = 0; i < comp[1]; i++) {
			sb.append("S");
		}
		theoryMass += Glycosyl.Hex.getMonoMass() * comp[1];

		for (int i = 0; i < comp[2]; i++) {
			sb.append("A");
		}
		theoryMass += Glycosyl.dHex.getMonoMass() * comp[2];

		for (int i = 0; i < comp[3]; i++) {
			sb.append("R");
		}
		theoryMass += (Glycosyl.NeuAc.getMonoMass() + methyl) * comp[3];

		for (int i = 0; i < comp[4]; i++) {
			sb.append("Y");
		}
		theoryMass += (Glycosyl.NeuGc.getMonoMass() + methyl) * comp[4];

		theoryMass = theoryMass + AminoAcidProperty.MONOW_O + AminoAcidProperty.MONOW_H * 2;

		switch (cationId) {
		case 1:
			theoryMass += cationNH4;
			break;
		case 2:
			theoryMass += cationNa;
			break;
		case 3:
			theoryMass += cationK;
			break;
		default:
			break;
		}

		String sequence = sb.toString();
		double mass = this.mwc.getMonoIsotopeMZ(sequence);
		double deltaMass = mass - glycomass;

		FPeptide fp = new FPeptide(sequence, mass, deltaMass, theoryMass, comp, cationId);
		return fp;
	}

	private void convert(String quanpro, String out, String meta) throws IOException {
		PrintWriter writer = new PrintWriter(out);
		BufferedReader quanProReader = new BufferedReader(new FileReader(quanpro));
		String line = quanProReader.readLine();
		String[] title = line.split("\t");
		StringBuilder titlesb = new StringBuilder();
		titlesb.append("Id").append("\t");
		titlesb.append("Compostion").append("\t");
		titlesb.append("Mass").append("\t");
		titlesb.append(Glycosyl.HexNAc.getTitle()).append("\t");
		titlesb.append(Glycosyl.Hex.getTitle()).append("\t");
		titlesb.append(Glycosyl.dHex.getTitle()).append("\t");
		titlesb.append(Glycosyl.NeuAc.getTitle()).append("\t");
		titlesb.append(Glycosyl.NeuGc.getTitle());
		for (int i = 3; i < title.length; i++) {
			titlesb.append("\t").append(title[i]);
		}

		writer.println(titlesb);

		PrintWriter metaWriter = new PrintWriter(meta);
		metaWriter.println("Id\tExperiment\tGroup");
		for (int i = 3; i < title.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i - 2).append("\t");
			sb.append(title[i]).append("\t");
			if (title[i].contains("UC")) {
				sb.append("UC");
			} else if (title[i].contains("Control")) {
				sb.append("Control");
			} else if (title[i].contains("Post")) {
				sb.append("Post");
			}
			metaWriter.println(sb);
		}
		metaWriter.close();

		TTest ttest = new TTest();

		int id = 1;
		while ((line = quanProReader.readLine()) != null) {
			String[] cs = line.split("\t");
			double[] intensity = new double[cs.length - 3];
			int count = 0;
			for (int i = 0; i < intensity.length; i++) {
				intensity[i] = Double.parseDouble(cs[i + 3]);
				if (intensity[i] > 0) {
					count++;
				}
			}
			if (count * 2 < intensity.length) {
				continue;
			}

			String[] pro = cs[0].split("_");
			StringBuilder sb = new StringBuilder();
			double mass = 0;

			sb.append(id).append("\t");

			StringBuilder compsb = new StringBuilder();
			int c1 = Integer.parseInt(pro[1]);
			mass += Glycosyl.HexNAc.getMonoMass() * c1;
			if (c1 > 0)
				compsb.append(Glycosyl.HexNAc.getTitle()).append("(").append(c1).append(")");

			int c2 = Integer.parseInt(pro[2]);
			mass += Glycosyl.Hex.getMonoMass() * c2;
			if (c2 > 0)
				compsb.append(Glycosyl.Hex.getTitle()).append("(").append(c2).append(")");

			int c3 = Integer.parseInt(pro[3]);
			mass += Glycosyl.dHex.getMonoMass() * c3;
			if (c3 > 0)
				compsb.append(Glycosyl.dHex.getTitle()).append("(").append(c3).append(")");

			int c4 = Integer.parseInt(pro[4]);
			mass += (Glycosyl.NeuAc.getMonoMass() + methyl) * c4;
			if (c4 > 0)
				compsb.append(Glycosyl.NeuAc.getTitle()).append("(").append(c4).append(")");

			int c5 = Integer.parseInt(pro[5]);
			mass += (Glycosyl.NeuGc.getMonoMass() + methyl) * c5;
			if (c5 > 0)
				compsb.append(Glycosyl.NeuGc.getTitle()).append("(").append(c5).append(")");

			mass = mass + AminoAcidProperty.MONOW_O + AminoAcidProperty.MONOW_H * 2;

			sb.append(compsb);
			sb.append("\t").append(mass);
			sb.append("\t").append(c1);
			sb.append("\t").append(c2);
			sb.append("\t").append(c3);
			sb.append("\t").append(c4);
			sb.append("\t").append(c5);

			ArrayList<Double> controlList = new ArrayList<Double>();
			ArrayList<Double> ucList = new ArrayList<Double>();
			ArrayList<Double> postList = new ArrayList<Double>();

			for (int i = 0; i < intensity.length; i++) {
				sb.append("\t").append(intensity[i]);

				if (intensity[i] > 0) {
					if (title[i + 3].contains("UC")) {
						ucList.add(intensity[i]);
					} else if (title[i].contains("Control")) {
						controlList.add(intensity[i]);
					} else if (title[i].contains("Post")) {
						postList.add(intensity[i]);
					}
				}
			}
			writer.println(sb);
			id++;

			double[] controlIntensities = new double[controlList.size()];
			for (int i = 0; i < controlIntensities.length; i++) {
				controlIntensities[i] = controlList.get(i);
			}

			double[] ucIntensities = new double[ucList.size()];
			for (int i = 0; i < ucIntensities.length; i++) {
				ucIntensities[i] = ucList.get(i);
			}

			double[] postIntensities = new double[postList.size()];
			for (int i = 0; i < postIntensities.length; i++) {
				postIntensities[i] = postList.get(i);
			}

			double value = ttest.homoscedasticT(controlIntensities, ucIntensities);
			if (Math.abs(value) < 0.01) {
				System.out.println(
						compsb + "\t" + value + "\t" + controlIntensities.length + "\t" + ucIntensities.length);
			}
		}
		quanProReader.close();
		writer.close();
	}

	private void convertPeak(String peakin, String peakout) throws IOException {
		PrintWriter writer = new PrintWriter(peakout);
		BufferedReader quanPeakReader = new BufferedReader(new FileReader(peakin));
		String line = quanPeakReader.readLine();
		String[] title = line.split("\t");
		StringBuilder titlesb = new StringBuilder();
		titlesb.append(title[0]).append("\t");
		titlesb.append("Compostion").append("\t");
		titlesb.append("Glycan mass").append("\t");
		titlesb.append(Glycosyl.HexNAc.getTitle()).append("\t");
		titlesb.append(Glycosyl.Hex.getTitle()).append("\t");
		titlesb.append(Glycosyl.dHex.getTitle()).append("\t");
		titlesb.append(Glycosyl.NeuAc.getTitle()).append("\t");
		titlesb.append(Glycosyl.NeuGc.getTitle()).append("\t");
		titlesb.append("NH4").append("\t");
		titlesb.append("Na").append("\t");
		titlesb.append("K").append("\t");
		titlesb.append("Monoisotopic Mass");
		for (int i = 5; i < title.length; i++) {
			titlesb.append("\t").append(title[i]);
		}

		writer.println(titlesb);

		while ((line = quanPeakReader.readLine()) != null) {
			String[] cs = line.split("\t");
			StringBuilder sb = new StringBuilder();
			sb.append(cs[0]).append("\t");
			String[] glycom = cs[2].split("[)(_]");
			if (glycom.length == 7) {
				int ion = Integer.parseInt(glycom[1]);

				StringBuilder compsb = new StringBuilder();
				int c1 = Integer.parseInt(glycom[2]);
				if (c1 > 0)
					compsb.append(Glycosyl.HexNAc.getTitle()).append("(").append(c1).append(")");

				int c2 = Integer.parseInt(glycom[3]);
				if (c2 > 0)
					compsb.append(Glycosyl.Hex.getTitle()).append("(").append(c2).append(")");

				int c3 = Integer.parseInt(glycom[4]);
				if (c3 > 0)
					compsb.append(Glycosyl.dHex.getTitle()).append("(").append(c3).append(")");

				int c4 = Integer.parseInt(glycom[5]);
				if (c4 > 0)
					compsb.append(Glycosyl.NeuAc.getTitle()).append("(").append(c4).append(")");

				int c5 = Integer.parseInt(glycom[6]);
				if (c5 > 0)
					compsb.append(Glycosyl.NeuGc.getTitle()).append("(").append(c5).append(")");

				double mass = 0;
				mass += Glycosyl.HexNAc.getMonoMass() * c1;

				mass += Glycosyl.Hex.getMonoMass() * c2;

				mass += Glycosyl.dHex.getMonoMass() * c3;

				mass += (Glycosyl.NeuAc.getMonoMass() + methyl) * c4;

				mass += (Glycosyl.NeuGc.getMonoMass() + methyl) * c5;

				mass = mass + AminoAcidProperty.MONOW_O + AminoAcidProperty.MONOW_H * 2;

				sb.append(compsb);
				sb.append("\t").append(mass);
				sb.append("\t").append(c1);
				sb.append("\t").append(c2);
				sb.append("\t").append(c3);
				sb.append("\t").append(c4);
				sb.append("\t").append(c5);

				if (ion == 0) {
					sb.append("\t-\t-\t-");
				} else if (ion == 1) {
					sb.append("\t+\t-\t-");
				} else if (ion == 2) {
					sb.append("\t-\t+\t-");
				} else if (ion == 3) {
					sb.append("\t-\t-\t+");
				}

				for (int i = 4; i < cs.length; i++) {
					sb.append("\t").append(cs[i]);
				}
				writer.println(sb);
			}
		}
		quanPeakReader.close();
		writer.close();
	}

	class FPeptide {

		String sequence;
		double mass;
		double deltaMass;
		double theoryMass;
		int[] comp;
		int cationId;

		FPeptide(String sequence, double mass, double deltaMass, double theoryMass, int[] comp, int cationId) {
			this.sequence = sequence;
			this.mass = mass;
			this.deltaMass = deltaMass;
			this.theoryMass = theoryMass;
			this.comp = comp;
			this.cationId = cationId;
		}

		public String getSequence() {
			return sequence;
		}

		public double getMass() {
			return mass;
		}

		public double getDeltaMass() {
			return deltaMass;
		}

		public double getTheoryMass() {
			return theoryMass;
		}

		public String getModseq() {
			StringBuilder sb = new StringBuilder(sequence);
			sb.append("(");
			sb.append(cationId).append("_");
			for (int i = 0; i < comp.length; i++) {
				sb.append(comp[i]).append("_");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");

			return sb.toString();
		}

		public String getProtein() {
			StringBuilder sb = new StringBuilder("Protein");
			sb.append("_");
			for (int i = 0; i < comp.length; i++) {
				sb.append(comp[i]).append("_");
			}
			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
	}

	private static void maldi(String glycans, String in, String out) throws IOException {

		HashMap<Double, String[]> glycanMap = new HashMap<Double, String[]>();
		BufferedReader greader = new BufferedReader(new FileReader(glycans));
		String line = greader.readLine();
		while ((line = greader.readLine()) != null) {
			String[] cs = line.split("\t");
			Double mass = Double.parseDouble(cs[2]);
			glycanMap.put(mass, cs);
		}
		greader.close();

		Double[] gmasses = glycanMap.keySet().toArray(new Double[glycanMap.size()]);
		Arrays.parallelSort(gmasses);

		HashMap<Double, HashMap<String, Double>> intenmap = new HashMap<Double, HashMap<String, Double>>();
		ArrayList<String> namelist = new ArrayList<String>();
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.endsWith("txt")) {
				String[] cs = name.split("_");
				if (cs.length == 3) {
					String key = cs[0] + "_" + cs[1] + "_" + cs[2].substring(0, cs[2].lastIndexOf("."));
					namelist.add(key);

					BufferedReader reader = new BufferedReader(new FileReader(files[i]));
					line = reader.readLine();
					while ((line = reader.readLine()) != null) {
						String[] gcs = line.split("\t");
						Double mass = Double.parseDouble(gcs[1]);
						double sn = Double.parseDouble(gcs[gcs.length - 3]);
						if (sn < 3) {
							continue;
						}

						Double value = Double.parseDouble(gcs[gcs.length - 1]);

						for (int j = 0; j < gmasses.length; j++) {
							if (Math.abs(mass - gmasses[j]) < 1) {
								HashMap<String, Double> gmap;
								if (intenmap.containsKey(gmasses[j])) {
									gmap = intenmap.get(gmasses[j]);
								} else {
									gmap = new HashMap<String, Double>();
									intenmap.put(gmasses[j], gmap);
								}

								if (gmap.containsKey(key)) {
									if (value > gmap.get(key)) {
										gmap.put(key, value);
									}
								} else {
									gmap.put(key, value);
								}
							}
						}
					}
					reader.close();
				}
			}
		}

		String[] names = namelist.toArray(new String[namelist.size()]);
		Arrays.parallelSort(names);

		PrintWriter writer = new PrintWriter(out);
		StringBuilder title = new StringBuilder();
		title.append("Composition").append("\t");
		title.append("Mass").append("\t");
		title.append("Mass+TMPP");
		for (int i = 0; i < names.length; i++) {
			title.append("\t").append(names[i]);
		}
		writer.println(title);

		for (int i = 0; i < gmasses.length; i++) {
			StringBuilder sb = new StringBuilder();
			String[] contents = glycanMap.get(gmasses[i]);
			for (int j = 0; j < contents.length; j++) {
				sb.append(contents[j]).append("\t");
			}

			HashMap<String, Double> imap = intenmap.get(gmasses[i]);
			for (int j = 0; j < names.length; j++) {
				if (imap.containsKey(names[j])) {
					sb.append(imap.get(names[j])).append("\t");
				} else {
					sb.append("0.0\t");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			writer.println(sb);
		}

		writer.close();
	}

	public static void main(String[] args) throws IOException, XMLStreamException, DataFormatException {
		// TODO Auto-generated method stub

		GlycanSpFinder finder = new GlycanSpFinder();
//		finder.batchProcess("Z:\\Kai\\henghui_glyco_IBD");
		/*
		 * finder.convert("Z:\\Kai\\henghui_glyco_IBD\\QuantifiedProteins.tsv",
		 * "Z:\\Kai\\henghui_glyco_IBD\\QuantifiedGlycans.tsv",
		 * "Z:\\Kai\\henghui_glyco_IBD\\metadata.tsv");
		 * 
		 * finder.convertPeak("Z:\\Kai\\henghui_glyco_IBD\\QuantifiedPeaks.tsv",
		 * "Z:\\Kai\\henghui_glyco_IBD\\QuantifiedGlycanPeaks.tsv");
		 */

		GlycanSpFinder.maldi("Z:\\Kai\\henghui_glyco_IBD\\IBD MALDI_all samples_20191124\\glycan.txt",
				"Z:\\Kai\\henghui_glyco_IBD\\IBD MALDI_all samples_20191124",
				"Z:\\Kai\\henghui_glyco_IBD\\IBD MALDI_all samples_20191124\\result.txt");

	}

}
