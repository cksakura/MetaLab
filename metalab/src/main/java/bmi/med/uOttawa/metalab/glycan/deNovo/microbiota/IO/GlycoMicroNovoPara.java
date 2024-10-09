package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.GlycosylDBManager;

public class GlycoMicroNovoPara {

	private static final Logger LOGGER = LogManager.getLogger(GlycoMicroNovoPara.class);

	private Glycosyl[] glycosyls;
	private double precursorPPM;
	private double fragmentPPM;
	private String parserType;

	public GlycoMicroNovoPara(Glycosyl[] glycosyls, double precursorPPM, double fragmentPPM, String parserType) {
		this.glycosyls = glycosyls;
		this.precursorPPM = precursorPPM;
		this.fragmentPPM = fragmentPPM;
		this.parserType = parserType;
	}

	public Glycosyl[] getGlycosyls() {
		return glycosyls;
	}

	public double getPrecursorPPM() {
		return precursorPPM;
	}

	public double getFragmentPPM() {
		return fragmentPPM;
	}

	public String getParserType() {
		return parserType;
	}

	public static GlycoMicroNovoPara getDefault() {
		Glycosyl[] glycosyls = new Glycosyl[] { Glycosyl.HexNAc, Glycosyl.Hex, Glycosyl.NeuAc, Glycosyl.Fuc,
				Glycosyl.Xylose, Glycosyl.NeuGc };

		GlycoMicroNovoPara para = new GlycoMicroNovoPara(glycosyls, 20, 10, MetaGlycanConstants.standardMode);
		return para;
	}

	public static void write(double precursorPPM, double fragmentPPM, int parserType, Glycosyl[] glycosyls,
			String output) {

		StringBuilder sb = new StringBuilder();
		sb.append("PTOL=").append(precursorPPM).append("\n");
		sb.append("FTOL=").append(fragmentPPM).append("\n");
		sb.append("TYPE=").append(parserType).append("\n");
		sb.append("GLY=").append(glycosyls.length).append("\n");
		for (int i = 0; i < glycosyls.length; i++) {
			sb.append("GLY").append(i + 1).append("=").append(glycosyls[i].getTitle()).append("\n");
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing parameter to file " + output, e);
		}
		pw.write(sb.toString());
		pw.close();
	}

	public static void write(GlycoMicroNovoPara parameter, String output) {
		StringBuilder sb = new StringBuilder();
		sb.append("PTOL=").append(parameter.precursorPPM).append("\n");
		sb.append("FTOL=").append(parameter.fragmentPPM).append("\n");
		sb.append("TYPE=").append(parameter.parserType).append("\n");
		sb.append("GLY=").append(parameter.glycosyls.length).append("\n");
		for (int i = 0; i < parameter.glycosyls.length; i++) {
			sb.append("GLY").append(i + 1).append("=").append(parameter.glycosyls[i].getTitle()).append("\n");
		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing parameter to file " + output, e);
		}
		pw.write(sb.toString());
		pw.close();
	}

	public static GlycoMicroNovoPara read(String file) {

		HashMap<String, Glycosyl> totalGlycoMap = GlycosylDBManager.loadGlycoMap();
		Glycosyl[] glycosyls = null;
		double precursorPPM = 0, fragmentPPM = 0;
		String parserType = MetaGlycanConstants.standardMode;
		int glycoId = 0;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("=");
				if (cs[0].equals("PTOL")) {
					precursorPPM = Double.parseDouble(cs[1]);
				} else if (cs[0].equals("FTOL")) {
					fragmentPPM = Double.parseDouble(cs[1]);
				} else if (cs[0].equals("TYPE")) {
					parserType = cs[1];
				} else if (cs[0].equals("GLY")) {
					glycosyls = new Glycosyl[Integer.parseInt(cs[1])];
				} else {
					glycosyls[glycoId++] = totalGlycoMap.get(cs[1]);
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading parameter from file " + file, e);
		}

		GlycoMicroNovoPara parameter = new GlycoMicroNovoPara(glycosyls, precursorPPM, fragmentPPM, parserType);
		return parameter;
	}

	public static GlycoMicroNovoPara read(String file, HashMap<String, Glycosyl> totalGlycoMap) {

		Glycosyl[] glycosyls = null;
		double precursorPPM = 0, fragmentPPM = 0;
		String parserType = MetaGlycanConstants.standardMode;
		int glycoId = 0;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("=");
				if (cs[0].equals("PTOL")) {
					precursorPPM = Double.parseDouble(cs[1]);
				} else if (cs[0].equals("FTOL")) {
					fragmentPPM = Double.parseDouble(cs[1]);
				} else if (cs[0].equals("TYPE")) {
					parserType = cs[1];
				} else if (cs[0].equals("GLY")) {
					glycosyls = new Glycosyl[Integer.parseInt(cs[1])];
				} else {
					glycosyls[glycoId++] = totalGlycoMap.get(cs[1]);
				}
			}
			reader.close();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading parameter from file " + file, e);
		}

		GlycoMicroNovoPara parameter = new GlycoMicroNovoPara(glycosyls, precursorPPM, fragmentPPM, parserType);
		return parameter;
	}

}
