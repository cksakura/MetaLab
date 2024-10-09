/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.task;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.aminoacid.AminoAcidProperty;
import bmi.med.uOttawa.metalab.core.math.MathTool;
import bmi.med.uOttawa.metalab.core.mod.PostTransModification;
import bmi.med.uOttawa.metalab.core.mod.UmModification;
import bmi.med.uOttawa.metalab.core.mod.UnimodReader;
import bmi.med.uOttawa.metalab.dbSearch.msFragger.MSFraggerPsm;
import bmi.med.uOttawa.metalab.dbSearch.open.OpenMod;
import mr.go.sgfilter.SGFilter;

/**
 * @author Kai Cheng
 *
 */
public class OpenSearchGaussianFitting {

	private static int openSearchWindow = 500;
	private static int binCount = 1000000;
	private static int filterWindow = 6;
	private static int halfWindow = 3;

	private static int countThreshold = 3;
	private static double r2Threshold = 0.9;
	private static double modMassTolerance = 0.01;
	
	private static double calWindow = 0.2;
	private static int calBinCount = 400;

	private ArrayList<int[]> rangeList;
	private UmModification[] ummods;
	private OpenMod[] openMods;
	private HashMap<Double, PostTransModification> setModMap;
	
	private double[] x;
	private double[] y;
	private double[] sg_y;
	
	public OpenSearchGaussianFitting() throws DocumentException {
		this.rangeList = new ArrayList<int[]>();

		UnimodReader umreader = new UnimodReader();
		this.ummods = umreader.getUmModifications();
	}
	
	public OpenSearchGaussianFitting(String unimod) throws DocumentException {
		this.rangeList = new ArrayList<int[]>();

		UnimodReader umreader = new UnimodReader(unimod);
		this.ummods = umreader.getUmModifications();
	}
	
	public double calibrate(MSFraggerPsm[] psms) throws Exception {
		double[] calx = new double[calBinCount];
		double[] caly = new double[calBinCount];

		for (int i = 0; i < calBinCount; i++) {
			calx[i] = i * (calWindow * 2) / (double) calBinCount - calWindow;
		}

		for (int i = 0; i < psms.length; i++) {
			double massDiff = psms[i].getMassDiff();
			int id = (int) ((massDiff + calWindow) / (calWindow * 2) * calBinCount);
			if (id >= 0 && id < calBinCount) {
				psms[i].setModBinId(id);
				if (psms[i].isTarget()) {
					caly[id]++;
				}
			}
		}

		SGFilter sgf = new SGFilter(halfWindow, halfWindow);
		double[] coeffs = SGFilter.computeSGCoefficients(halfWindow, halfWindow, 2);
		double[] cal_sg_y = sgf.smooth(caly, coeffs);

		GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(5000);
		double[] noModPara = null;
		boolean contain0 = false;

		int beginId = -1;
		boolean start = false;

		for (int i = 0; i < calBinCount; i++) {

			if (cal_sg_y[i] > countThreshold) {
				if (!start) {
					beginId = i;
					start = true;
				}
			} else {
				if (start) {

					start = false;

					ArrayList<int[]> usedBins = new ArrayList<int[]>();

					double localMax = 0;
					double localMin = 0;
					int leftCount = 0;
					int rightCount = 0;
					for (int j = beginId; j < i; j++) {

						if (cal_sg_y[j] > localMax) {
							if (localMin == 0) {
								localMax = cal_sg_y[j];
								leftCount++;
							} else {
								boolean pass = false;
								if (leftCount > rightCount) {
									if (leftCount >= filterWindow && rightCount >= halfWindow) {
										pass = true;
									}
								} else {
									if (rightCount >= filterWindow && leftCount >= halfWindow) {
										pass = true;
									}
								}
								if (pass) {
									int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
									usedBins.add(sub);
								}
								leftCount = 1;
								rightCount = 0;
								localMax = caly[j];
								localMin = 0;
							}

						} else {
							if (localMin == 0) {
								localMin = cal_sg_y[j];
								rightCount++;
							} else {
								if (cal_sg_y[j] > localMin) {
									boolean pass = false;
									if (leftCount > rightCount) {
										if (leftCount >= filterWindow && rightCount >= halfWindow) {
											pass = true;
										}
									} else {
										if (rightCount >= filterWindow && leftCount >= halfWindow) {
											pass = true;
										}
									}
									if (pass) {
										int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
										usedBins.add(sub);
									}
									leftCount = 1;
									rightCount = 0;
									localMax = caly[j];
									localMin = 0;
								} else {
									localMin = cal_sg_y[j];
									rightCount++;
								}
							}
						}
					}

					boolean pass = false;
					if (leftCount > rightCount) {
						if (leftCount >= filterWindow && rightCount >= halfWindow) {
							pass = true;
						}
					} else {
						if (rightCount >= filterWindow && leftCount >= halfWindow) {
							pass = true;
						}
					}
					if (pass) {
						int[] sub = new int[] { i - leftCount - rightCount, i - 1 };
						usedBins.add(sub);
					}

					boolean find = false;
					for (int j = 0; j < usedBins.size(); j++) {
						int[] subBin = usedBins.get(j);
						double[] parameter = this.getGuassian(fitter, calx, cal_sg_y, subBin[0], subBin[1]);
						if (parameter != null) {
							if (subBin[0] <= calBinCount / 2 && subBin[1] >= calBinCount / 2) {
								noModPara = parameter;
								contain0 = true;
							}
						}
					}

					if (!find) {
						double[] parameter = this.getGuassian(fitter, calx, cal_sg_y, beginId, i - 1);
						if (parameter != null) {
							if (beginId <= calBinCount / 2 && i - 1 >= calBinCount / 2) {
								noModPara = parameter;
								contain0 = true;
							}
						}
					}
				}
			}
		}

		if (contain0) {
			return noModPara[1];
		} else {
			return 0;
		}
	}

	public MSFraggerPsm[] fit(MSFraggerPsm[] psms, HashMap<Double, PostTransModification> ptmmap, int ppm, Appendable aa)
			throws Exception {

		this.setModMap = new HashMap<Double, PostTransModification>();
		aa.append("Start bin\t").append("End bin\t").append("Start mass\t").append("End mass\t")
				.append("Mass range\t");
		aa.append("Parameter a\t").append("Parameter b\t").append("Parameter c\t").append("Average intensity\t").append("R2\n");

		for (Double mass : ptmmap.keySet()) {
			PostTransModification ptm = ptmmap.get(mass);
			double modMass = ptm.getMass();
			String setSite = ptm.getSite();

			ArrayList<UmModification> umlist = new ArrayList<UmModification>();
			for (UmModification um : ummods) {
				boolean match = false;
				double umMass = um.getMono_mass();
				if (Math.abs(umMass - modMass) < modMassTolerance) {
					String[] sites = um.getSpecificity();
					for (String site : sites) {
						if (site.equals(setSite)) {
							match = true;
							break;
						}
					}
				}
				if (match) {
					umlist.add(um);
				}
			}
			UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
			PostTransModification newPTM = new PostTransModification(ums, setSite, ptm.isVariable());

			this.setModMap.put(mass, newPTM);
		}

		this.x = new double[binCount];
		this.y = new double[binCount];

		for (int i = 0; i < binCount; i++) {
			x[i] = i * (openSearchWindow * 2) / (double) binCount - openSearchWindow;
		}

		for (int i = 0; i < psms.length; i++) {
			double massDiff = psms[i].getMassDiff();
			int id = (int) ((massDiff + openSearchWindow) / (openSearchWindow * 2) * binCount);
			if (id >= 0 && id < binCount) {
				psms[i].setModBinId(id);
				if (psms[i].isTarget()) {
					y[id]++;
				}
			}
		}

		SGFilter sgf = new SGFilter(halfWindow, halfWindow);
		double[] coeffs = SGFilter.computeSGCoefficients(halfWindow, halfWindow, 2);
		this.sg_y = sgf.smooth(y, coeffs);

		GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(5000);
		HashMap<Double, double[]> massmap = new HashMap<Double, double[]>();
		double[] noModPara = null;
		boolean contain0 = false;

		int beginId = -1;
		boolean start = false;

		double[] idModMass = new double[binCount];
		Arrays.fill(idModMass, -Double.MAX_VALUE);

		for (int i = 0; i < binCount; i++) {

			if (sg_y[i] > countThreshold) {
				if (!start) {
					beginId = i;
					start = true;
				}
			} else {
				if (start) {

					start = false;

					ArrayList<int[]> usedBins = new ArrayList<int[]>();

					double localMax = 0;
					double localMin = 0;
					int leftCount = 0;
					int rightCount = 0;
					for (int j = beginId; j < i; j++) {

						if (sg_y[j] > localMax) {
							if (localMin == 0) {
								localMax = sg_y[j];
								leftCount++;
							} else {
								boolean pass = false;
								if (leftCount > rightCount) {
									if (leftCount >= filterWindow && rightCount >= halfWindow) {
										pass = true;
									}
								} else {
									if (rightCount >= filterWindow && leftCount >= halfWindow) {
										pass = true;
									}
								}
								if (pass) {
									int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
									usedBins.add(sub);
								}
								leftCount = 1;
								rightCount = 0;
								localMax = y[j];
								localMin = 0;
							}

						} else {
							if (localMin == 0) {
								localMin = sg_y[j];
								rightCount++;
							} else {
								if (sg_y[j] > localMin) {
									boolean pass = false;
									if (leftCount > rightCount) {
										if (leftCount >= filterWindow && rightCount >= halfWindow) {
											pass = true;
										}
									} else {
										if (rightCount >= filterWindow && leftCount >= halfWindow) {
											pass = true;
										}
									}
									if (pass) {
										int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
										usedBins.add(sub);
									}
									leftCount = 1;
									rightCount = 0;
									localMax = y[j];
									localMin = 0;
								} else {
									localMin = sg_y[j];
									rightCount++;
								}
							}
						}
					}

					boolean pass = false;
					if (leftCount > rightCount) {
						if (leftCount >= filterWindow && rightCount >= halfWindow) {
							pass = true;
						}
					} else {
						if (rightCount >= filterWindow && leftCount >= halfWindow) {
							pass = true;
						}
					}
					if (pass) {
						int[] sub = new int[] { i - leftCount - rightCount, i - 1 };
						usedBins.add(sub);
					}

					boolean find = false;
					for (int j = 0; j < usedBins.size(); j++) {
						int[] subBin = usedBins.get(j);
						double[] parameter = this.getGuassian(fitter, x, sg_y, subBin[0], subBin[1], aa);
						if (parameter != null) {
							if (subBin[0] <= binCount / 2 && subBin[1] >= binCount / 2) {
								noModPara = parameter;
								contain0 = true;
							} else {
								massmap.put(parameter[1], parameter);
							}

							for (int k = subBin[0]; k <= subBin[1]; k++) {
								idModMass[k] = parameter[1];
							}

							find = true;
						}
					}

					if (!find) {
						double[] parameter = this.getGuassian(fitter, x, sg_y, beginId, i - 1, aa);
						if (parameter != null) {
							if (beginId <= binCount / 2 && i - 1 >= binCount / 2) {
								noModPara = parameter;
								contain0 = true;
							} else {
								massmap.put(parameter[1], parameter);
							}

							for (int k = beginId; k < i; k++) {
								idModMass[k] = parameter[1];
							}
						}
					}
				}
			}
		}

		filterGuassian(massmap);
		
		Double[] deltaMasses = massmap.keySet().toArray(new Double[massmap.size()]);
		Arrays.sort(deltaMasses);

		HashMap<Double, Integer> openModMap = new HashMap<Double, Integer>();
		double adjustment = 0;

		if (noModPara == null) {
			this.openMods = new OpenMod[deltaMasses.length];

			for (int i = 0; i < deltaMasses.length; i++) {
				double[] para = massmap.get(deltaMasses[i]);
				OpenMod om = new OpenMod(i, para, para[1], -1);
				om.setMonoIsotope(true);
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(om.getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					om.setUmmod(ums);
				}
				openModMap.put(deltaMasses[i], om.getId());
				openMods[om.getId()] = om;
			}

			for (int i = 1; i < openMods.length; i++) {
				double abundantI = openMods[i].getGaussianParameters()[0];
				for (int j = i - 1; j >= 0; j--) {
					double deltaMass = openMods[i].getExpModMass() - openMods[j].getExpModMass();
					double abundantJ = openMods[j].getGaussianParameters()[0];
					if (Math.abs(deltaMass - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
						if (abundantI < abundantJ) {
							openMods[i].setMonoIsotope(false);
							if (openMods[j].isMonoIsotope()) {
								openMods[i].setMonoId(j);
							} else {
								openMods[i].setMonoId(openMods[j].getMonoId());
							}
						}

					} else if (deltaMass - AminoAcidProperty.isotope_deltaM > modMassTolerance) {
						break;
					}
				}
			}

		} else {
			this.openMods = new OpenMod[deltaMasses.length + 1];
			this.openMods[0] = new OpenMod(0, noModPara, 0.0, -1);
			this.openMods[0].setMonoIsotope(true);
			openModMap.put(noModPara[1], 0);

			adjustment = noModPara[1];

			for (int i = 0; i < deltaMasses.length; i++) {
				double[] para = massmap.get(deltaMasses[i]);
				OpenMod om = new OpenMod(i + 1, para, para[1] - adjustment, -1);
				om.setMonoIsotope(true);
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(om.getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					om.setUmmod(ums);
				}
				openModMap.put(deltaMasses[i], om.getId());
				openMods[om.getId()] = om;
			}

			for (int i = 1; i < openMods.length; i++) {
				double abundantI = openMods[i].getGaussianParameters()[0];

				if (Math.abs(openMods[i].getExpModMass() - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
					if (abundantI < openMods[0].getGaussianParameters()[0]) {
						openMods[i].setMonoIsotope(false);
						openMods[i].setMonoId(0);
					}
					continue;
				}
				for (int j = i - 1; j >= 0; j--) {
					double deltaMass = openMods[i].getExpModMass() - openMods[j].getExpModMass();
					double abundantJ = openMods[j].getGaussianParameters()[0];
					if (Math.abs(deltaMass - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
						if (abundantI < abundantJ) {
							openMods[i].setMonoIsotope(false);
							if (openMods[j].isMonoIsotope()) {
								openMods[i].setMonoId(j);
							} else {
								openMods[i].setMonoId(openMods[j].getMonoId());
							}
						}

					} else if (deltaMass - AminoAcidProperty.isotope_deltaM > modMassTolerance) {
						break;
					}
				}
			}
		}

		ArrayList<MSFraggerPsm> filteredList = new ArrayList<MSFraggerPsm>();

		for (int i = 0; i < psms.length; i++) {
			int modBinId = psms[i].getModBinId();
			if (modBinId >= 0 && modBinId < binCount) {
				if (openModMap.containsKey(idModMass[modBinId])) {
					int modid = openModMap.get(idModMass[modBinId]);
					if (contain0 && modid == 0) {

						psms[i].setOpenModId(modid);
						psms[i].setOpenModMass(0);
						psms[i].setMassDiff(psms[i].getMassDiff() - adjustment);

						if (ppm == 0) {
							filteredList.add(psms[i]);
						} else {
							if (Math.abs(psms[i].getMassDiff() / psms[i].getPrecursorMr() * 1E6) < ppm) {
								filteredList.add(psms[i]);
							}
						}
					} else {
						psms[i].setOpenModId(modid);
						psms[i].setOpenModMass(openMods[modid].getExpModMass());
						psms[i].setMassDiff(psms[i].getMassDiff() - adjustment - openMods[modid].getExpModMass());

						if (ppm == 0) {
							filteredList.add(psms[i]);
						} else {
							boolean filterppm = false;
							if (Math.abs(psms[i].getMassDiff() / psms[i].getPrecursorMr() * 1E6) < ppm) {
								filterppm = true;
							} else {
								UmModification[] ums = openMods[modid].getUmmod();
								if (ums != null) {
									for (int j = 0; j < ums.length; j++) {
										if (Math.abs((psms[i].getMassDiff() + openMods[modid].getExpModMass()
												- ums[j].getMono_mass()) / psms[i].getPrecursorMr() * 1E6) < ppm) {
											filterppm = true;
										}
									}
								}
							}
							if (filterppm) {
								filteredList.add(psms[i]);
							}
						}
					}
				}
			}
		}

		MSFraggerPsm[] filteredPSMs = filteredList.toArray(new MSFraggerPsm[filteredList.size()]);
		return filteredPSMs;
	}

	public MSFraggerPsm[] fit(MSFraggerPsm[][] psms, HashMap<Double, PostTransModification> ptmmap, int ppm, Appendable aa)
			throws Exception {

		this.setModMap = new HashMap<Double, PostTransModification>();
		aa.append("Start bin\t").append("End bin\t").append("Start mass\t").append("End mass\t").append("Mass range\t");
		aa.append("Parameter a\t").append("Parameter b\t").append("Parameter c\t").append("Average intensity\t")
				.append("R2\n");

		for (Double mass : ptmmap.keySet()) {
			PostTransModification ptm = ptmmap.get(mass);
			double modMass = ptm.getMass();
			String setSite = ptm.getSite();

			ArrayList<UmModification> umlist = new ArrayList<UmModification>();
			for (UmModification um : ummods) {
				boolean match = false;
				double umMass = um.getMono_mass();
				if (Math.abs(umMass - modMass) < modMassTolerance) {
					String[] sites = um.getSpecificity();
					for (String site : sites) {
						if (site.equals(setSite)) {
							match = true;
							break;
						}
					}
				}
				if (match) {
					umlist.add(um);
				}
			}
			UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
			PostTransModification newPTM = new PostTransModification(ums, setSite, ptm.isVariable());

			this.setModMap.put(mass, newPTM);
		}

		this.x = new double[binCount];
		this.y = new double[binCount];

		for (int i = 0; i < binCount; i++) {
			x[i] = i * (openSearchWindow * 2) / (double) binCount - openSearchWindow;
		}

		for (int i = 0; i < psms.length; i++) {
			double calbration = this.calibrate(psms[i]);
			for (int j = 0; j < psms[i].length; j++) {
				double massDiff = psms[i][j].getMassDiff() - calbration;
				int id = (int) ((massDiff + openSearchWindow) / (openSearchWindow * 2) * binCount);
				if (id >= 0 && id < binCount) {
					psms[i][j].setModBinId(id);
					if (psms[i][j].isTarget()) {
						y[id]++;
					}
				}
			}
		}

		SGFilter sgf = new SGFilter(halfWindow, halfWindow);
		double[] coeffs = SGFilter.computeSGCoefficients(halfWindow, halfWindow, 2);
		this.sg_y = sgf.smooth(y, coeffs);

		GaussianCurveFitter fitter = GaussianCurveFitter.create().withMaxIterations(5000);
		HashMap<Double, double[]> massmap = new HashMap<Double, double[]>();
		double[] noModPara = null;
		boolean contain0 = false;

		int beginId = -1;
		boolean start = false;

		double[] idModMass = new double[binCount];
		Arrays.fill(idModMass, -Double.MAX_VALUE);

		for (int i = 0; i < binCount; i++) {

			if (sg_y[i] > countThreshold) {
				if (!start) {
					beginId = i;
					start = true;
				}
			} else {
				if (start) {

					start = false;

					ArrayList<int[]> usedBins = new ArrayList<int[]>();

					double localMax = 0;
					double localMin = 0;
					int leftCount = 0;
					int rightCount = 0;
					for (int j = beginId; j < i; j++) {

						if (sg_y[j] > localMax) {
							if (localMin == 0) {
								localMax = sg_y[j];
								leftCount++;
							} else {
								boolean pass = false;
								if (leftCount > rightCount) {
									if (leftCount >= filterWindow && rightCount >= halfWindow) {
										pass = true;
									}
								} else {
									if (rightCount >= filterWindow && leftCount >= halfWindow) {
										pass = true;
									}
								}
								if (pass) {
									int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
									usedBins.add(sub);
								}
								leftCount = 1;
								rightCount = 0;
								localMax = y[j];
								localMin = 0;
							}

						} else {
							if (localMin == 0) {
								localMin = sg_y[j];
								rightCount++;
							} else {
								if (sg_y[j] > localMin) {
									boolean pass = false;
									if (leftCount > rightCount) {
										if (leftCount >= filterWindow && rightCount >= halfWindow) {
											pass = true;
										}
									} else {
										if (rightCount >= filterWindow && leftCount >= halfWindow) {
											pass = true;
										}
									}
									if (pass) {
										int[] sub = new int[] { j - leftCount - rightCount, j - 1 };
										usedBins.add(sub);
									}
									leftCount = 1;
									rightCount = 0;
									localMax = y[j];
									localMin = 0;
								} else {
									localMin = sg_y[j];
									rightCount++;
								}
							}
						}
					}

					boolean pass = false;
					if (leftCount > rightCount) {
						if (leftCount >= filterWindow && rightCount >= halfWindow) {
							pass = true;
						}
					} else {
						if (rightCount >= filterWindow && leftCount >= halfWindow) {
							pass = true;
						}
					}
					if (pass) {
						int[] sub = new int[] { i - leftCount - rightCount, i - 1 };
						usedBins.add(sub);
					}

					boolean find = false;
					for (int j = 0; j < usedBins.size(); j++) {
						int[] subBin = usedBins.get(j);
						double[] parameter = this.getGuassian(fitter, x, sg_y, subBin[0], subBin[1], aa);

						if (parameter != null) {
							if (subBin[0] <= binCount / 2 && subBin[1] >= binCount / 2) {
								noModPara = parameter;
								contain0 = true;
							} else {
								massmap.put(parameter[1], parameter);
							}

							for (int k = subBin[0]; k <= subBin[1]; k++) {
								idModMass[k] = parameter[1];
							}

							find = true;
						}
					}

					if (!find) {
						double[] parameter = this.getGuassian(fitter, x, sg_y, beginId, i - 1, aa);

						if (parameter != null) {
							if (beginId <= binCount / 2 && i - 1 >= binCount / 2) {
								noModPara = parameter;
								contain0 = true;
							} else {
								massmap.put(parameter[1], parameter);
							}

							for (int k = beginId; k < i; k++) {
								idModMass[k] = parameter[1];
							}
						}
					}
				}
			}
		}

		Double[] deltaMasses = massmap.keySet().toArray(new Double[massmap.size()]);
		Arrays.sort(deltaMasses);

		double adjustment = 0;
		OpenMod[] unFilteredMods = null;

		if (noModPara == null) {

			unFilteredMods = new OpenMod[deltaMasses.length];

			for (int i = 0; i < deltaMasses.length; i++) {
				double[] para = massmap.get(deltaMasses[i]);
				OpenMod om = new OpenMod(i, para, para[1], -1);
				om.setMonoIsotope(true);
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(om.getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					om.setUmmod(ums);
				}
				unFilteredMods[om.getId()] = om;
			}

			for (int i = 1; i < unFilteredMods.length; i++) {
				double abundantI = unFilteredMods[i].getGaussianParameters()[0];
				for (int j = i - 1; j >= 0; j--) {
					double deltaMass = unFilteredMods[i].getExpModMass() - unFilteredMods[j].getExpModMass();
					double abundantJ = unFilteredMods[j].getGaussianParameters()[0];
					if (Math.abs(deltaMass - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
						if (abundantI < abundantJ) {
							unFilteredMods[i].setMonoIsotope(false);
							if (unFilteredMods[j].isMonoIsotope()) {
								unFilteredMods[i].setMonoId(j);
							} else {
								unFilteredMods[i].setMonoId(unFilteredMods[j].getMonoId());
							}
						}

					} else if (deltaMass - AminoAcidProperty.isotope_deltaM > modMassTolerance) {
						break;
					}
				}
			}

		} else {

			unFilteredMods = new OpenMod[deltaMasses.length + 1];
			unFilteredMods[0] = new OpenMod(0, noModPara, 0.0, -1);
			unFilteredMods[0].setMonoIsotope(true);

			adjustment = noModPara[1];

			for (int i = 0; i < deltaMasses.length; i++) {
				double[] para = massmap.get(deltaMasses[i]);
				OpenMod om = new OpenMod(i + 1, para, para[1] - adjustment, -1);
				om.setMonoIsotope(true);
				ArrayList<UmModification> umlist = new ArrayList<UmModification>();
				for (int j = 0; j < ummods.length; j++) {
					double dm = Math.abs(om.getExpModMass() - ummods[j].getMono_mass());
					if (dm < modMassTolerance) {
						umlist.add(ummods[j]);
					}
				}
				if (umlist.size() > 0) {
					UmModification[] ums = umlist.toArray(new UmModification[umlist.size()]);
					om.setUmmod(ums);
				}
				unFilteredMods[om.getId()] = om;
			}

			for (int i = 1; i < unFilteredMods.length; i++) {
				double abundantI = unFilteredMods[i].getGaussianParameters()[0];

				if (Math.abs(unFilteredMods[i].getExpModMass() - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
					if (abundantI < unFilteredMods[0].getGaussianParameters()[0]) {
						unFilteredMods[i].setMonoIsotope(false);
						unFilteredMods[i].setMonoId(0);
					}
					continue;
				}
				for (int j = i - 1; j >= 0; j--) {
					double deltaMass = unFilteredMods[i].getExpModMass() - unFilteredMods[j].getExpModMass();
					double abundantJ = unFilteredMods[j].getGaussianParameters()[0];
					if (Math.abs(deltaMass - AminoAcidProperty.isotope_deltaM) < modMassTolerance) {
						if (abundantI < abundantJ) {
							unFilteredMods[i].setMonoIsotope(false);
							if (unFilteredMods[j].isMonoIsotope()) {
								unFilteredMods[i].setMonoId(j);
							} else {
								unFilteredMods[i].setMonoId(unFilteredMods[j].getMonoId());
							}
						}

					} else if (deltaMass - AminoAcidProperty.isotope_deltaM > modMassTolerance) {
						break;
					}
				}
			}
		}

		HashMap<Double, Integer> openModMap = new HashMap<Double, Integer>();
		this.openMods = this.filterGuassian(unFilteredMods);

		for (int i = 0; i < this.openMods.length; i++) {
			double deltaMass = openMods[i].getGaussianParameters()[1];
			int id = openMods[i].getId();
			openModMap.put(deltaMass, id);
		}

		ArrayList<MSFraggerPsm> filteredList = new ArrayList<MSFraggerPsm>();

		for (int i = 0; i < psms.length; i++) {
			for (int j = 0; j < psms[i].length; j++) {
				int modBinId = psms[i][j].getModBinId();
				if (modBinId >= 0 && modBinId < binCount) {
					if (openModMap.containsKey(idModMass[modBinId])) {
						int modid = openModMap.get(idModMass[modBinId]);
						if (contain0 && modid == 0) {

							psms[i][j].setOpenModId(modid);
							psms[i][j].setOpenModMass(0);
							psms[i][j].setMassDiff(psms[i][j].getMassDiff() - adjustment);

							if (ppm == 0) {
								filteredList.add(psms[i][j]);
							} else {
								if (Math.abs(psms[i][j].getMassDiff() / psms[i][j].getPrecursorMr() * 1E6) < ppm) {
									filteredList.add(psms[i][j]);
								}
							}
						} else {
							psms[i][j].setOpenModId(modid);
							psms[i][j].setOpenModMass(openMods[modid].getExpModMass());
							psms[i][j].setMassDiff(
									psms[i][j].getMassDiff() - adjustment - openMods[modid].getExpModMass());

							if (ppm == 0) {
								filteredList.add(psms[i][j]);
							} else {
								boolean filterppm = false;
								if (Math.abs(psms[i][j].getMassDiff() / psms[i][j].getPrecursorMr() * 1E6) < ppm) {
									filterppm = true;
								} else {
									UmModification[] ums = openMods[modid].getUmmod();
									if (ums != null) {
										for (int k = 0; k < ums.length; k++) {
											if (Math.abs((psms[i][k].getMassDiff() + openMods[modid].getExpModMass()
													- ums[k].getMono_mass()) / psms[i][k].getPrecursorMr()
													* 1E6) < ppm) {
												filterppm = true;
											}
										}
									}
								}
								if (filterppm) {
									filteredList.add(psms[i][j]);
								}
							}
						}
					}
				}
			}
		}

		MSFraggerPsm[] filteredPSMs = filteredList.toArray(new MSFraggerPsm[filteredList.size()]);

		return filteredPSMs;
	}
	
	private void filterGuassian(HashMap<Double, double[]> massmap) {
		ArrayList<Double> parameterA = new ArrayList<Double>();
		ArrayList<Double> parameterC = new ArrayList<Double>();
		ArrayList<Double> parameterAvsAve = new ArrayList<Double>();
		for (Double deltaMass : massmap.keySet()) {
			double[] parameters = massmap.get(deltaMass);
			parameterA.add(parameters[0]);
			parameterC.add(parameters[2]);
			parameterAvsAve.add(parameters[0] / parameters[3]);
		}

		double medianA = MathTool.getMedian(parameterA);
		double medianC = MathTool.getMedian(parameterC);
		double medianAvsAve = MathTool.getMedian(parameterAvsAve);

		Iterator<Double> it = massmap.keySet().iterator();
		while (it.hasNext()) {
			Double deltaMass = it.next();
			double[] parameters = massmap.get(deltaMass);

			boolean pass = false;
			if (parameters[0] > medianA * 10) {
				pass = true;
			} else if (parameters[0] / parameters[3] > medianAvsAve) {
				pass = true;
			} else if (parameters[0] > medianA) {
				if (Math.abs(parameters[1]) < 5) {
					if (parameters[2] < medianC * 2) {
						pass = true;
					}
				} else {
					if (parameters[2] < medianC * 3) {
						pass = true;
					}
				}
			} else {
				if (Math.abs(parameters[1]) < 5) {
					if (parameters[2] < medianC) {
						pass = true;
					}
				} else {
					if (parameters[2] < medianC * 2) {
						pass = true;
					}
				}
			}

			if (!pass) {
				it.remove();
			}
		}
	}
	
	private OpenMod[] filterGuassian(OpenMod[] unFilteredMods) {
		ArrayList<Double> parameterA = new ArrayList<Double>();
		ArrayList<Double> parameterC = new ArrayList<Double>();
		ArrayList<Double> parameterAvsAve = new ArrayList<Double>();
		for (OpenMod om : unFilteredMods) {
			double[] parameters = om.getGaussianParameters();
			parameterA.add(parameters[0]);
			parameterC.add(parameters[2]);
			parameterAvsAve.add(parameters[0] / parameters[3]);
		}

		double medianA = MathTool.getMedian(parameterA);
		double medianC = MathTool.getMedian(parameterC);
		double medianAvsAve = MathTool.getMedian(parameterAvsAve);
		double factor = medianA / medianC;

		boolean[] passes = new boolean[unFilteredMods.length];
		Arrays.fill(passes, false);

		for (int i = 0; i < unFilteredMods.length; i++) {
			double[] parameters = unFilteredMods[i].getGaussianParameters();
			if (parameters[0] > medianA * 10) {
				passes[i] = true;
				continue;
			}

			if (parameters[0] / parameters[3] > medianAvsAve) {
				passes[i] = true;
				continue;
			}

			int monoId = unFilteredMods[i].getMonoId();
			if (parameters[0] > medianA * 2) {
				if (Math.abs(parameters[1]) < 5) {
					if (parameters[2] < medianC * 2) {
						passes[i] = true;
					}
				} else {
					if (parameters[2] < medianC * 3) {
						passes[i] = true;
					}
				}
			} else if (parameters[0] < medianA) {
				if (Math.abs(parameters[1]) < 5) {
					if (parameters[0] / parameters[2] > factor) {
						passes[i] = true;
					}
				} else {
					if (monoId < 0) {
						if (parameters[2] < medianC) {
							passes[i] = true;
						}
					} else {
						if (parameters[2] < medianC * 2) {
							passes[i] = true;
						}
					}
				}
			} else {
				if (Math.abs(parameters[1]) < 5) {
					if (parameters[2] < medianC) {
						passes[i] = true;
					}
				} else {
					if (monoId < 0) {
						if (parameters[2] < medianC * 2) {
							passes[i] = true;
						}
					} else {
						if (parameters[2] < medianC * 3) {
							passes[i] = true;
						}
					}
				}

			}
		}

		int[] newIds = new int[unFilteredMods.length];
		int id = 0;
		for (int i = 0; i < unFilteredMods.length; i++) {
			int monoid = unFilteredMods[i].getMonoId();
			if (monoid >= 0) {
				if (!passes[monoid]) {
					passes[i] = false;
				}
			}

			if (passes[i]) {
				newIds[i] = id;
				id++;
			} else {
				newIds[i] = -1;
			}
		}

		ArrayList<OpenMod> list = new ArrayList<OpenMod>();
		for (int i = 0; i < unFilteredMods.length; i++) {
			if (passes[i]) {
				if (unFilteredMods[i].getMonoId() < 0) {
					unFilteredMods[i].setId(newIds[i]);
					list.add(unFilteredMods[i]);
				} else {
					unFilteredMods[i].setId(newIds[i]);
					unFilteredMods[i].setMonoId(newIds[unFilteredMods[i].getMonoId()]);
					list.add(unFilteredMods[i]);
				}
			}
		}

		OpenMod[] filteredOpenMods = list.toArray(new OpenMod[list.size()]);
		return filteredOpenMods;
	}
	
	public OpenMod[] getModInfo() {
		return this.openMods;
	}

	public HashMap<Double, PostTransModification> getSetModMap() {
		return setModMap;
	}

	private double[] getGuassian(GaussianCurveFitter fitter, double[] x, double[] y, int beginId, int endId)
			throws Exception {

		WeightedObservedPoints obs = new WeightedObservedPoints();

		int length = endId - beginId + 1;

		double totaly = 0;
		double maxy = 0;
		int maxid = -1;
		int count = 0;
		for (int i = beginId; i <= endId; i++) {

			if (y[i] > 0) {
				totaly += y[i];
				obs.add(x[i], y[i]);
				count++;

				if (y[i] > maxy) {
					maxy = y[i];
					maxid = i;
				}
			}
		}

		if (count < filterWindow) {
			return null;
		}

		double avey = totaly / (double) count;

		List<WeightedObservedPoint> list = obs.toList();
		double[] parameters = null;

		try {
			parameters = fitter.fit(list);
		} catch (Exception e) {
			return null;
		}

		if (parameters == null) {

			if (length > filterWindow * 2) {

				int newBeginId = maxid - filterWindow >= beginId ? maxid - filterWindow : beginId;
				int newEndId = maxid + filterWindow < endId ? maxid + filterWindow : endId;

				length = newEndId - newBeginId + 1;

				obs = new WeightedObservedPoints();
				totaly = 0;
				count = 0;

				for (int i = newBeginId; i < newEndId; i++) {

					if (y[i] > 0) {
						totaly += y[i];
						obs.add(x[i], y[i]);
						count++;
					}
				}

				if (count < filterWindow) {
					return null;
				}

				avey = totaly / (double) count;
				list = obs.toList();

				try {
					parameters = fitter.fit(list);
				} catch (Exception e) {
					return null;
				}
			}
		}

		if (parameters == null) {
			return null;
		}

		double SStot = 0;
		double SSres = 0;

		for (WeightedObservedPoint wop : list) {
			double px = wop.getX();
			double py = wop.getY();

			SStot += (py - avey) * (py - avey);
			double caly = parameters[0]
					* Math.exp(-0.5 * (px - parameters[1]) * (px - parameters[1]) / (parameters[2] * parameters[2]));
			SSres += (caly - py) * (caly - py);
		}

		double r2 = 1 - SSres / SStot;

		if (r2 < r2Threshold || SStot == 0) {
			
			if (list.size() > filterWindow * 2) {

				int newBeginId = maxid - filterWindow >= beginId ? maxid - filterWindow : beginId;
				int newEndId = maxid + filterWindow < endId ? maxid + filterWindow : endId;
				length = newEndId - newBeginId + 1;
				
				obs = new WeightedObservedPoints();
				totaly = 0;
				count = 0;

				for (int i = newBeginId; i < newEndId; i++) {

					if (y[i] > 0) {
						totaly += y[i];
						obs.add(x[i], y[i]);
						count++;
					}
				}
				
				if (count < filterWindow) {
					return null;
				}

				avey = totaly / (double) count;
				list = obs.toList();

				try {
					parameters = fitter.fit(list);
				} catch (Exception e) {
					return null;
				}

				if (parameters == null) {
					return null;
				} else {
					SStot = 0;
					SSres = 0;

					for (WeightedObservedPoint wop : list) {
						double px = wop.getX();
						double py = wop.getY();

						SStot += (py - avey) * (py - avey);
						double caly = parameters[0] * Math.exp(
								-0.5 * (px - parameters[1]) * (px - parameters[1]) / (parameters[2] * parameters[2]));
						SSres += (caly - py) * (caly - py);
					}

					r2 = 1 - SSres / SStot;

					if (r2 < r2Threshold || SStot == 0) {
						return null;
					}
				}
			} else {
				return null;

			}
		}

		if ((parameters[1] < x[beginId] || parameters[1] > x[endId])) {
			return null;
		}
		
		if (length < 8 && parameters[2] > 0.0035) {
			return null;
		}

		double[] result = new double[5];
		result[0] = parameters[0];
		result[1] = parameters[1];
		result[2] = parameters[2];
		result[3] = avey;
		result[4] = r2;

		return result;
	}
	
	private double[] getGuassian(GaussianCurveFitter fitter, double[] x, double[] y, int beginId, int endId, Appendable aa)
			throws Exception {

		WeightedObservedPoints obs = new WeightedObservedPoints();

		int length = endId - beginId + 1;

		double totaly = 0;
		double maxy = 0;
		int maxid = -1;
		int count = 0;
		for (int i = beginId; i <= endId; i++) {

			if (y[i] > 0) {
				totaly += y[i];
				obs.add(x[i], y[i]);
				count++;

				if (y[i] > maxy) {
					maxy = y[i];
					maxid = i;
				}
			}
		}

		if (count < filterWindow) {
			return null;
		}

		double avey = totaly / (double) count;

		List<WeightedObservedPoint> list = obs.toList();
		double[] parameters = null;

		try {
			parameters = fitter.fit(list);
		} catch (Exception e) {
			return null;
		}

		if (parameters == null) {

			if (length > filterWindow * 2) {

				int newBeginId = maxid - filterWindow >= beginId ? maxid - filterWindow : beginId;
				int newEndId = maxid + filterWindow < endId ? maxid + filterWindow : endId;

				if (newEndId - maxid > maxid - newBeginId) {
					newEndId = maxid + (maxid - newBeginId);
				} else if (newEndId - maxid < maxid - newBeginId) {
					newBeginId = maxid + (maxid - newEndId);
				}

				length = newEndId - newBeginId + 1;

				obs = new WeightedObservedPoints();
				totaly = 0;
				count = 0;

				for (int i = newBeginId; i <= newEndId; i++) {

					if (y[i] > 0) {
						totaly += y[i];
						obs.add(x[i], y[i]);
						count++;
					}
				}

				if (count < filterWindow) {
					return null;
				}

				avey = totaly / (double) count;
				list = obs.toList();

				try {
					parameters = fitter.fit(list);
				} catch (Exception e) {
					return null;
				}
			}
		}

		if (parameters == null) {
			return null;
		}

		double SStot = 0;
		double SSres = 0;

		for (WeightedObservedPoint wop : list) {
			double px = wop.getX();
			double py = wop.getY();

			SStot += (py - avey) * (py - avey);
			double caly = parameters[0]
					* Math.exp(-0.5 * (px - parameters[1]) * (px - parameters[1]) / (parameters[2] * parameters[2]));
			SSres += (caly - py) * (caly - py);
		}

		double r2 = 1 - SSres / SStot;

		if (r2 < r2Threshold || SStot == 0) {

			if (list.size() > filterWindow * 2) {

				int newBeginId = maxid - filterWindow >= beginId ? maxid - filterWindow : beginId;
				int newEndId = maxid + filterWindow < endId ? maxid + filterWindow : endId;

				if (newEndId - maxid > maxid - newBeginId) {
					newEndId = maxid + (maxid - newBeginId);
				} else if (newEndId - maxid < maxid - newBeginId) {
					newBeginId = maxid + (maxid - newEndId);
				}

				length = newEndId - newBeginId + 1;

				obs = new WeightedObservedPoints();
				totaly = 0;
				count = 0;

				for (int i = newBeginId; i <= newEndId; i++) {

					if (y[i] > 0) {
						totaly += y[i];
						obs.add(x[i], y[i]);
						count++;
					}
				}

				if (count < filterWindow) {
					return null;
				}

				avey = totaly / (double) count;
				list = obs.toList();

				try {
					parameters = fitter.fit(list);
				} catch (Exception e) {
					return null;
				}

				if (parameters == null) {
					return null;
				} else {
					SStot = 0;
					SSres = 0;

					for (WeightedObservedPoint wop : list) {
						double px = wop.getX();
						double py = wop.getY();

						SStot += (py - avey) * (py - avey);
						double caly = parameters[0] * Math.exp(
								-0.5 * (px - parameters[1]) * (px - parameters[1]) / (parameters[2] * parameters[2]));
						SSres += (caly - py) * (caly - py);
					}

					r2 = 1 - SSres / SStot;

					if (r2 < r2Threshold || SStot == 0) {
						return null;
					}
				}
			} else {
				return null;

			}
		}

		if ((parameters[1] < x[beginId] || parameters[1] > x[endId])) {
			return null;
		}

		aa.append(beginId + "\t" + endId + "\t" + x[beginId] + "\t" + x[endId] + "\t" + (x[endId] - x[beginId]) + "\t"
				+ parameters[0] + "\t" + parameters[1] + "\t" + parameters[2] + "\t" + avey + "\t" + r2 + "\n");

		double[] result = new double[5];
		result[0] = parameters[0];
		result[1] = parameters[1];
		result[2] = parameters[2];
		result[3] = avey;
		result[4] = r2;

		return result;
	}

	public ArrayList<int[]> getRangeList() {
		return this.rangeList;
	}

	public void outputBins(File output) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		StringBuilder title = new StringBuilder();
		title.append("Bin id\t");
		title.append("Delta mass\t");
		title.append("PSM count\t");
		title.append("Smooth value\t");
		writer.println(title);

		for (int i = 0; i < x.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i + 1).append("\t");
			sb.append(x[i]).append("\t");
			sb.append(y[i]).append("\t");
			sb.append(sg_y[i]).append("\t");
			writer.println(sb);
		}

		writer.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SGFilter sgf = new SGFilter(halfWindow, halfWindow);
		double[] coeffs = SGFilter.computeSGCoefficients(halfWindow, halfWindow, 2);
	}
	
	
}
