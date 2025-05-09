package bmi.med.uOttawa.metalab.dbSearch.sage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONWriter;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;

public class SageParameter {
	
	private MaxquantModification[] fixMods;
	private MaxquantModification[] variMods;

	private int missCleavages = 2;
	private String[] enzyme;
	private int digestMode = 0;
	private String fasta;
	
	private boolean quant;
	private IsobaricTag isobaricTag;
	
	private String[] mzMLFiles;
	
	public void config(MetaParameterMag metaPar, boolean quant, String[] mzMLFiles, File output) throws IOException {

		this.fixMods = metaPar.getFullFixMods();
		this.variMods = metaPar.getFullVariMods();
		
		this.enzyme = metaPar.getFragpipeEnzyme();
		this.digestMode = metaPar.getDigestMode();
		
		this.fasta = metaPar.getCurrentDb();
		
		this.quant = quant;
		this.isobaricTag = metaPar.getIsobaricTag();
		this.mzMLFiles = mzMLFiles;
		
		export(output);
	}

	public void export(String out) throws IOException {
		export(new File(out));
	}

	public void export(File out) throws IOException {

		PrintWriter writer = new PrintWriter(out);
		JSONWriter jw = new JSONWriter(writer);
		jw.object();

		// database
		jw.key("database").object();
		jw.key("bucket_size").value(8192);

		jw.key("enzyme").object();
		jw.key("missed_cleavages").value(missCleavages);
		jw.key("min_len").value(7);
		jw.key("max_len").value(35);
		if (enzyme == null) {
			jw.key("cleave_at").value("$");
		} else {
			if (digestMode == 2) {
				jw.key("cleave_at").value("");
			} else {
				jw.key("cleave_at").value(enzyme[1]);
				if (!enzyme[2].equals("_")) {
					jw.key("restrict").value(enzyme[2]);
				}
				if (enzyme[3].equals("C")) {
					jw.key("c_terminal").value(true);
				} else {
					jw.key("c_terminal").value(false);
				}
				if (digestMode == 1) {
					jw.key("semi_enzymatic").value(true);
				}
			}
		}

		jw.endObject();

		jw.key("peptide_min_mass").value(500.0);
		jw.key("peptide_max_mass").value(5000.0);

		jw.key("ion_kinds").array();
		jw.value("b").value("y");
		jw.endArray();

		jw.key("min_ion_index").value(2);
		jw.key("max_variable_mods").value(3);

		jw.key("static_mods").object();
		if (fixMods != null) {
			for (int i = 0; i < fixMods.length; i++) {
				String position = fixMods[i].getPosition();
				double mono = fixMods[i].getMonomass();
				if (position.equals("NORMAL")) {
					String[] sites = fixMods[i].getSites();
					jw.key(sites[0]).value(mono);
				} else if (position.equals("PEP_N")) {
					jw.key("^").value(mono);
				} else if (position.equals("PEP_C")) {
					jw.key("$").value(mono);
				} else if (position.equals("PRO_N")) {
					jw.key("[").value(mono);
				} else if (position.equals("PRO_C")) {
					jw.key("]").value(mono);
				}
			}
		}
		jw.endObject();

		jw.key("variable_mods").object();
		if (variMods != null) {
			for (int i = 0; i < variMods.length; i++) {
				String position = variMods[i].getPosition();
				double mono = variMods[i].getMonomass();
				if (position.equals("NORMAL")) {
					String[] sites = variMods[i].getSites();
					jw.key(sites[0]).value(mono);
				} else if (position.equals("PEP_N")) {
					jw.key("^").value(mono);
				} else if (position.equals("PEP_C")) {
					jw.key("$").value(mono);
				} else if (position.equals("PRO_N")) {
					jw.key("[").value(mono);
				} else if (position.equals("PRO_C")) {
					jw.key("]").value(mono);
				}
			}
		}
		jw.endObject();

		jw.key("decoy_tag").value("REV_");
		jw.key("generate_decoys").value(true);
		jw.key("fasta").value(fasta);
		jw.endObject();

		// precursor_tol
		jw.key("precursor_tol").object();
		jw.key("ppm").array();
		jw.value(-10.0).value(10.0);
		jw.endArray();
		jw.endObject();

		// fragment_tol
		jw.key("fragment_tol").object();
		jw.key("ppm").array();
		jw.value(-10.0).value(10.0);
		jw.endArray();
		jw.endObject();

		// isotope_errors
		jw.key("isotope_errors").array();
		jw.value(0).value(2);
		jw.endArray();

		jw.key("deisotope").value(true);
		jw.key("min_peaks").value(15);
		jw.key("max_peaks").value(150);
		jw.key("max_fragment_charge").value(1);
		jw.key("min_matched_peaks").value(4);
		jw.key("predict_rt").value(true);

		// quant
		if (quant) {
			if (isobaricTag == null) {

				jw.key("quant").object();
				jw.key("lfq").value(true);

				jw.key("lfq_settings").object();
				jw.key("peak_scoring").value("Hybrid");
				jw.key("integration").value("Sum");
				jw.key("spectral_angle").value(0.7);
				jw.key("ppm_tolerance").value(5.0);
				jw.endObject();

				jw.endObject();
			} else {
				switch (isobaricTag) {
				case tmt6:
					jw.key("quant").object();
					jw.key("tmt").value("Tmt6");

					jw.key("tmt_settings").object();
					jw.key("level").value(3);
					jw.key("sn").value(false);
					jw.endObject();

					jw.endObject();
				case tmt10:
					jw.key("quant").object();
					jw.key("tmt").value("Tmt10");

					jw.key("tmt_settings").object();
					jw.key("level").value(3);
					jw.key("sn").value(false);
					jw.endObject();

					jw.endObject();
					break;
				case tmt11:
					jw.key("quant").object();
					jw.key("tmt").value("Tmt11");

					jw.key("tmt_settings").object();
					jw.key("level").value(3);
					jw.key("sn").value(false);
					jw.endObject();

					jw.endObject();
				case tmtpro16:
					jw.key("quant").object();
					jw.key("tmt").value("Tmt16");

					jw.key("tmt_settings").object();
					jw.key("level").value(3);
					jw.key("sn").value(false);
					jw.endObject();

					jw.endObject();
				case tmtpro18:
					jw.key("quant").object();
					jw.key("tmt").value("Tmt18");

					jw.key("tmt_settings").object();
					jw.key("level").value(3);
					jw.key("sn").value(false);
					jw.endObject();

					jw.endObject();
				default:
					break;
				}
			}
		}

		jw.key("mzml_paths").array();
		for (String mzMLFile : this.mzMLFiles) {
			jw.value(mzMLFile);
		}
		jw.endArray();
		
		jw.key("output_directory").value(out.getParent());
		jw.endObject();

		writer.close();
	}

	public static void main(String[] args) throws IOException {

	}
}
