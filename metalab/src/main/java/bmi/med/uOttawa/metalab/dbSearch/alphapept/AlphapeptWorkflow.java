package bmi.med.uOttawa.metalab.dbSearch.alphapept;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;

public class AlphapeptWorkflow {

	public static final String templeteWorkflow = "settings_template.yaml";
	private ArrayList<String> parentList;
	private HashMap<String, AlphapeptWorkflowItem> valueMap;

	private AlphapeptWorkflow(HashMap<String, AlphapeptWorkflowItem> valueMap, ArrayList<String> parentList) {
		this.valueMap = valueMap;
		this.parentList = parentList;
	}
	
	public static AlphapeptWorkflow parse() throws IOException {
		InputStream stream = ResourceLoader.load(AlphapeptWorkflow.templeteWorkflow);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		return parse(reader);
	}

	public static AlphapeptWorkflow parse(String params) throws IOException {
		return parse(new File(params));
	}

	public static AlphapeptWorkflow parse(File params) throws IOException {
		return parse(new BufferedReader(new FileReader(params)));
	}

	public static AlphapeptWorkflow parse(BufferedReader reader) throws IOException {

		HashMap<String, AlphapeptWorkflowItem> valueMap = new HashMap<String, AlphapeptWorkflowItem>();
		ArrayList<String> parentList = new ArrayList<String>();

		int id = 1;
		String line = null;
		String parent = null;
		String key = null;
		String value = null;

		while ((line = reader.readLine()) != null) {
			if (line.startsWith("    ")) {
				if (line.startsWith("    default:")) {
					value = line.substring("    default:".length());
					if (value.length() == 0) {
						value = reader.readLine();
						AlphapeptWorkflowItem item = new AlphapeptWorkflowItem(id++, true, parent, key, value);
						if (!valueMap.containsKey(key)) {
							valueMap.put(key, item);
						}
					} else {
						if (value.equals(" []")) {
							AlphapeptWorkflowItem item = new AlphapeptWorkflowItem(id++, true, parent, key, value);
							if (!valueMap.containsKey(key)) {
								valueMap.put(key, item);
							}
						} else {
							AlphapeptWorkflowItem item = new AlphapeptWorkflowItem(id++, false, parent, key, value);
							if (!valueMap.containsKey(key)) {
								valueMap.put(key, item);
							}
						}
					}
				}
			} else if (line.startsWith("  ")) {
				key = line;
			} else {
				parent = line;
				parentList.add(parent);
			}
		}
		reader.close();

		AlphapeptWorkflow workflow = new AlphapeptWorkflow(valueMap, parentList);
		return workflow;
	}
	
	public void config(MetaParameterMag metaPar, File output, File resultHdfFile) throws IOException {

		this.valueMap.get("  results_path:").setValue(resultHdfFile.getAbsolutePath());
		this.valueMap.get("  fasta_paths:").setValue(metaPar.getCurrentDb());
		this.valueMap.get("  n_processes:").setValue(String.valueOf(metaPar.getThreadCount()));
		
		File fastaFile = new File(metaPar.getCurrentDb());
		String fileName = fastaFile.getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		File fastaHdfFile = new File(fastaFile.getParent(), fileName + ".hdf");
		this.valueMap.get("  database_path:").setValue(fastaHdfFile.getAbsolutePath());

		MetaData metaData = metaPar.getMetadata();
		String[] rawFiles = metaData.getRawFiles();
		boolean continueSearch = true;
		for (int i = 0; i < rawFiles.length; i++) {
			File rawFileIFile = new File(rawFiles[i]);
			String name = rawFileIFile.getName();
			name = name.substring(0, name.lastIndexOf("."));

			File hdfFile = new File(rawFileIFile.getParent(), name + ".ms_data.hdf");
			if (!hdfFile.exists()) {
				continueSearch = false;
				break;
			}
		}
		
		this.valueMap.get("  continue_runs:").setValue(String.valueOf(continueSearch));
		String[] expNames = metaData.getExpNames();
		int[] fraction = metaData.getFractions();
		String[] fragctionStrings = new String[fraction.length];
		for (int i = 0; i < fraction.length; i++) {
			fragctionStrings[i] = String.valueOf(fraction[i]);
		}

		this.valueMap.get("  shortnames:").setValue(expNames);
		this.valueMap.get("  file_paths:").setValue(rawFiles);
		this.valueMap.get("  fraction:").setValue(fragctionStrings);

		HashMap<String, Integer> metaIdMap = new HashMap<String, Integer>();
		String[][] metaInfo = metaData.getMetaInfo();
		for (int i = 0; i < metaInfo.length; i++) {
			if (metaInfo[i].length > 0) {
				if (!metaIdMap.containsKey(metaInfo[i][0])) {
					metaIdMap.put(metaInfo[i][0], metaIdMap.size());
				}
			}
		}

		if (metaIdMap.size() > 0) {
			String[] sampleGroup = metaIdMap.keySet().toArray(new String[metaIdMap.size()]);
			Arrays.sort(sampleGroup, (g1, g2) -> {
				int s1 = metaIdMap.get(g1);
				int s2 = metaIdMap.get(g2);
				return s1 - s2;
			});

			this.valueMap.get("  sample_group:").setValue(sampleGroup);
			String[] matchingGroup = new String[metaInfo.length];

			for (int i = 0; i < matchingGroup.length; i++) {
				matchingGroup[i] = String.valueOf(metaIdMap.get(metaInfo[i][0]));
			}
			this.valueMap.get("  matching_group:").setValue(matchingGroup);
		}

		export(output);
	}

	public void config(MetaParameterMag metaPar, File singleRawFile, String expName, int fraction, File output,
			File resultHdfFile) throws IOException {
		this.valueMap.get("  create_database:").setValue("false");
		this.valueMap.get("  lfq_quantification:").setValue("false");
		this.valueMap.get("  results_path:").setValue(resultHdfFile.getAbsolutePath());
		this.valueMap.get("  fasta_paths:").setValue(metaPar.getCurrentDb());
		this.valueMap.get("  n_processes:").setValue(String.valueOf(metaPar.getThreadCount()));

		File fastaFile = new File(metaPar.getCurrentDb());
		String fileName = fastaFile.getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		File fastaHdfFile = new File(fastaFile.getParent(), fileName + ".hdf");
		this.valueMap.get("  database_path:").setValue(fastaHdfFile.getAbsolutePath());

		String name = singleRawFile.getName();
		name = name.substring(0, name.lastIndexOf("."));

		File hdfFile = new File(singleRawFile.getParent(), name + ".ms_data.hdf");
		if (hdfFile.exists()) {
			this.valueMap.get("  continue_runs:").setValue("true");
		} else {
			this.valueMap.get("  continue_runs:").setValue("false");
		}

		this.valueMap.get("  shortnames:").setValue(expName);
		this.valueMap.get("  file_paths:").setValue(singleRawFile.getAbsolutePath());
		this.valueMap.get("  fraction:").setValue(String.valueOf(fraction));

		this.valueMap.get("  sample_group:").setValue("0");
		this.valueMap.get("  matching_group:").setValue("0");
		this.valueMap.get("  max_lfq:").setValue("false");
		
		export(output);
	}
	
	private static class AlphapeptWorkflowItem implements Comparable<AlphapeptWorkflowItem> {

		private int id;
		private boolean isList;
		private String parent;
		private String key;
		private String[] value;

		AlphapeptWorkflowItem(int id, boolean isList, String parent, String key, String... value) {
			this.id = id;
			this.isList = isList;
			this.parent = parent;
			this.key = key;
			this.value = value;
		}

		void setValue(String... value) {
			this.value = value;
		}

		@Override
		public int compareTo(AlphapeptWorkflowItem o) {
			// TODO Auto-generated method stub
			return this.id - o.id;
		}
	}

	public void export(String file) throws IOException {
		export(new File(file));
	}

	public void export(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		AlphapeptWorkflowItem[] items = this.valueMap.values().toArray(new AlphapeptWorkflowItem[this.valueMap.size()]);
		Arrays.sort(items);

		HashMap<String, StringBuilder> sbMap = new HashMap<String, StringBuilder>();
		for (int i = 0; i < parentList.size(); i++) {
			StringBuilder sb = new StringBuilder();
			String parent = parentList.get(i);
			sb.append(parent).append("\n");
			sbMap.put(parent, sb);
		}

		for (int id = 0; id < items.length; id++) {

			String parent = items[id].parent;
			StringBuilder sb = sbMap.get(parent);
			if (items[id].isList) {
				if (items[id].value[0].equals(" []")) {
					sb.append(items[id].key).append(items[id].value[0]).append("\n");
				} else {
					sb.append(items[id].key).append("\n");
					for (int j = 0; j < items[id].value.length; j++) {
						if (items[id].value[j].startsWith("    - ")) {
							sb.append(items[id].value[j].substring(2)).append("\n");
						} else if (items[id].value[j].startsWith("  - ")) {
							sb.append(items[id].value[j]).append("\n");
						} else {
							sb.append("  - ").append(items[id].value[j]).append("\n");
						}
					}
				}
			} else {
				if (items[id].value[0].startsWith("    - ")) {
					sb.append(items[id].key).append("\n");
					sb.append(items[id].value[0].substring(2)).append("\n");
				} else {
					if (items[id].value[0].charAt(0) == ' ') {
						sb.append(items[id].key).append(items[id].value[0]).append("\n");
					} else {
						sb.append(items[id].key).append(" ").append(items[id].value[0]).append("\n");
					}
				}
			}
		}

		for (int i = 0; i < parentList.size(); i++) {
			String parent = parentList.get(i);
			StringBuilder sb = sbMap.get(parent);
			writer.print(sb);
		}

		writer.close();
	}

}
