package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import bmi.med.uOttawa.metalab.glycan.Glycosyl;
import bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.NovoGlycoCompMatch;
import bmi.med.uOttawa.metalab.quant.Features;

public class GlycoMicroNovoWriter {

	private PrintWriter writer;

	private static final Logger LOGGER = LogManager.getLogger(GlycoMicroNovoWriter.class);

	public GlycoMicroNovoWriter(String out) {
		try {
			this.writer = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing results to " + out, e);
		}
	}

	public GlycoMicroNovoWriter(File outputFile) {
		// TODO Auto-generated constructor stub
		try {
			this.writer = new PrintWriter(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing results to " + outputFile, e);
		}
	}

	public void write(NovoGlycoCompMatch[] determinedMatches, NovoGlycoCompMatch[] undeterminedMatches,
			HashMap<Integer, Integer> feasIdMap, ArrayList<Features> featuresList, Glycosyl[] glycosyls) {

		StringBuilder title = new StringBuilder();
		title.append(glycosyls.length).append(";");
		title.append(determinedMatches.length).append(";");
		title.append(undeterminedMatches.length).append(";");
		title.append(featuresList.size()).append(FormatTool.CR);

		writer.write(title.toString());

		for (Glycosyl glycosyl : glycosyls) {
			writer.write(glycosyl.getFullString() + FormatTool.CR);
		}

		for (NovoGlycoCompMatch match : determinedMatches) {
			writer.write(match.toString() + FormatTool.CR);
		}

		for (NovoGlycoCompMatch match : undeterminedMatches) {
			writer.write(match.toString() + FormatTool.CR);
		}

		for (Features feas : featuresList) {
			writer.write(feas.toStringOneLine() + FormatTool.CR);
		}

		for (Integer scannum : feasIdMap.keySet()) {
			writer.write(scannum + ";" + feasIdMap.get(scannum) + FormatTool.CR);
		}

		writer.close();
	}

}
