package bmi.med.uOttawa.metalab.dbSearch.fragpipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;

public class FragpipeWorkflow {

	private HashMap<String, FragpipeWorkflowItem> valueMap;
	private int workflowId;

	public static final int defaultWorkflow = 0;
	public static final int LFQ_MBRWorkflow = 1;
	public static final int openWorkflow = 2;
	public static final int silacWorkflow = 3;
	public static final int TMTWorkflow = 4;
	public static final int TMTProWorkflow = 5;
	
	protected static String[] workflowTypes = new String[] {"Default.workflow", "LFQ-MBR.workflow", "Open.workflow", 
			"SILAC3.workflow", "TMT10.workflow", "TMT16.workflow"};

	private FragpipeWorkflow(HashMap<String, FragpipeWorkflowItem> valueMap) {
		this.valueMap = valueMap;
	}
	
	public static FragpipeWorkflow parse(File workflowFolder, int workflowType) throws IOException {
		if (workflowType < 0 || workflowType > workflowTypes.length) {
			return null;
		}
		File workflowFile = new File(workflowFolder, workflowTypes[workflowType]);
		FragpipeWorkflow fragpipeWorkflow = parse(workflowFile);
		fragpipeWorkflow.workflowId = workflowType;
		return fragpipeWorkflow;
	}

	public static FragpipeWorkflow parse(String params) throws IOException {
		return parse(new File(params));
	}

	public static FragpipeWorkflow parse(File params) throws IOException {
		HashMap<String, FragpipeWorkflowItem> valueMap = new HashMap<String, FragpipeWorkflowItem>();

		int id = 1;
		BufferedReader reader = new BufferedReader(new FileReader(params));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}

			int l1 = line.indexOf("=");
			if (l1 < 0) {
				continue;
			}

			String key = line.substring(0, l1).trim();
			String value = line.substring(l1 + 1).trim();

			FragpipeWorkflowItem item = new FragpipeWorkflowItem(id++, key, value);
			valueMap.put(key, item);
		}
		reader.close();

		FragpipeWorkflow parameter = new FragpipeWorkflow(valueMap);
		return parameter;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public void config(MetaParameterMag metaPar, File output) throws IOException {

		setDatabase(metaPar.getCurrentDb());
		setIonMobility(metaPar.getMetadata().isTimsTof());
		
		/*
		int termini = 2;
		String[] enzyme = metaPar.getFragpipeEnzyme();
		int digestMode = metaPar.getDigestMode();
		int missCleavage = metaPar.getMissCleavages();

		if (digestMode == 0) {
			termini = 2;
		} else if (digestMode == 1 || digestMode == 2 || digestMode == 3) {
			termini = 1;
		} else if (digestMode == 4) {
			termini = 0;
		}

		setEnzyme(enzyme[0], enzyme[1], enzyme[2], termini, missCleavage);

		switch (workflowId) {
		case silacWorkflow:
			IsobaricTag tag = metaPar.getIsobaricTag();

			break;
		default:
			break;
		}
		 */
		export(output);
	}

	public void setEnzyme(String enzyme, String cutAfter, String notCut, int termini, int miss) {
		valueMap.get("msfragger.misc.fragger.enzyme-dropdown-1").setValue(enzyme);
		valueMap.get("msfragger.search_enzyme_name_1").setValue(enzyme);
		valueMap.get("msfragger.search_enzyme_cut_1").setValue(cutAfter);
		valueMap.get("msfragger.search_enzyme_nocut_1").setValue(notCut);
		valueMap.get("msfragger.num_enzyme_termini").setValue(String.valueOf(termini));
		valueMap.get("msfragger.allowed_missed_cleavage_1").setValue(String.valueOf(miss));
	}

	public void setDatabase(String database) {
		if (valueMap.containsKey("database.db-path")) {
			valueMap.get("database.db-path").setValue(database);
		} else {
			valueMap.put("database.db-path", new FragpipeWorkflowItem(0, "database.db-path", database));
		}
	}
	
	public void setIonMobility(boolean ionMobility) {
		if (ionMobility) {
			valueMap.get("workflow.input.data-type.im-ms").setValue("true");
			valueMap.get("workflow.input.data-type.regular-ms").setValue("false");
		} else {
			valueMap.get("workflow.input.data-type.im-ms").setValue("false");
			valueMap.get("workflow.input.data-type.regular-ms").setValue("true");
		}
	}

	/**
	 * for the unit, 0=Daltons, 1=ppm
	 * 
	 * @param massTol
	 * @param unit
	 * @param trueTol
	 * @param trueUnit
	 */
	public void setPrecursorMassTol(double massTol, int unit, double trueTol, int trueUnit) {
		this.valueMap.get("msfragger.precursor_mass_lower").setValue(String.valueOf(-massTol));
		this.valueMap.get("msfragger.precursor_mass_upper").setValue(String.valueOf(massTol));
		this.valueMap.get("msfragger.precursor_true_tolerance").setValue(String.valueOf(trueTol));
		this.valueMap.get("msfragger.precursor_mass_units").setValue(String.valueOf(unit));
		this.valueMap.get("msfragger.precursor_true_units").setValue(String.valueOf(trueUnit));
	}

	public void setFragmentMassTol(double massTol, int unit) {
		this.valueMap.get("msfragger.fragment_mass_tolerance").setValue(String.valueOf(massTol));
		this.valueMap.get("msfragger.fragment_mass_units").setValue(String.valueOf(unit));
	}

	public void export(String file) throws IOException {
		export(new File(file));
	}

	public void export(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		FragpipeWorkflowItem[] items = this.valueMap.values().toArray(new FragpipeWorkflowItem[this.valueMap.size()]);
		Arrays.sort(items);

		for (int id = 0; id < items.length; id++) {
			writer.println(items[id]);
		}

		writer.close();
	}

	private static class FragpipeWorkflowItem implements Comparable<FragpipeWorkflowItem> {

		private int id;
		private String key;
		private String value;

		FragpipeWorkflowItem(int id, String key, String value) {
			this.id = id;
			this.key = key;
			this.value = value;
		}

		void setValue(String value) {
			this.value = value;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(key).append("=").append(value.replaceAll("\\\\", "\\\\\\\\"));
			return sb.toString();
		}

		@Override
		public int compareTo(FragpipeWorkflowItem o) {
			// TODO Auto-generated method stub
			return this.id - o.id;
		}
	}
}
