package bmi.med.uOttawa.metalab.task.pfind;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;

import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.alphapept.MetaLabMagAlphapeptTask;
import bmi.med.uOttawa.metalab.task.dia.MetaDiaTask;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParaIoDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.fragpipe.MetaLabMagFragpipeTask;
import bmi.med.uOttawa.metalab.task.hgm.HgmHapPFindTask;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParaIOHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParameterHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;
import bmi.med.uOttawa.metalab.task.mag.MagHapPFindTask;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParaIOMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParaIOPFind;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.sage.MetaLabMagSageTask;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesIOV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

/**
 * pFind workflow
 * 
 * @author Kai Cheng
 *
 */
public class MetaLabMagTask extends MetaLabTask {

	private MetaAbstractTask idenTask;
	private MetaDbCreatePFindTask dbcTask;

	public MetaLabMagTask(MetaParameter metaPar, MetaSourcesV2 msv, JProgressBar bar1, JProgressBar bar2) {
		super(metaPar, msv, bar1, bar2);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		MetaSourcesV2 msv = (MetaSourcesV2) this.msv;

		this.bar1.setValue(0);
		this.bar1.setStringPainted(true);
		this.bar2.setStringPainted(true);
		this.bar2.setIndeterminate(true);

		File result = new File(metaPar.getResult());
		if (!result.exists()) {
			result.mkdirs();
		}
		File searchresultFile = null;
		if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow) {
			MetaParaIOPFind.export((MetaParameterPFind) metaPar, new File(result, "parameter.json"));
			if (((MetaParameterPFind) metaPar).isOpenSearch()) {
				searchresultFile = new File(result, "open_search");
			} else {
				searchresultFile = new File(result, "closed_search");
			}
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindHGM) {
			MetaParaIOHGM.export((MetaParameterHGM) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "open_search");
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindMAG) {
			MetaParaIOMag.export((MetaParameterMag) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "mag_result");
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.DiaNNMAG) {
			MetaParaIoDia.export((MetaParameterDia) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "mag_result");
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.FragpipeIMMag) {
			MetaParaIOMag.export((MetaParameterMag) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "mag_result");
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.AlphapeptIMMag) {
			MetaParaIOMag.export((MetaParameterMag) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "mag_result");
		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.SageMAG) {
			MetaParaIOMag.export((MetaParameterMag) metaPar, new File(result, "parameter.json"));
			searchresultFile = new File(result, "mag_result");
		} else {
			return false;
		}

		if (!searchresultFile.exists()) {
			searchresultFile.mkdirs();
		}

		metaPar.setDbSearchResultFile(searchresultFile);

		if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow) {

			idenTask = new MetaIdenPFindTask((MetaParameterPFind) metaPar, msv, bar1, bar2, null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindHGM) {

			idenTask = new HgmHapPFindTask((MetaParameterHGM) metaPar, (MetaSourcesHGM) msv, bar1, bar2, null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.pFindMAG) {

			MetaParameterMag magPar = (MetaParameterMag) metaPar;
			idenTask = new MagHapPFindTask(magPar, (MetaSourcesMag) msv, bar1, bar2, null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.DiaNNMAG) {

			MetaParameterDia diaPar = (MetaParameterDia) metaPar;
			idenTask = new MetaDiaTask(diaPar, (MetaSourcesDia) msv, bar1, bar2, null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.FragpipeIMMag) {

			idenTask = new MetaLabMagFragpipeTask((MetaParameterMag) this.metaPar, (MetaSourcesMag) msv, bar1, bar2,
					null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.AlphapeptIMMag) {

			idenTask = new MetaLabMagAlphapeptTask((MetaParameterMag) this.metaPar, (MetaSourcesMag) msv, bar1, bar2,
					null);

		} else if (metaPar.getWorkflowType() == MetaLabWorkflowType.SageMAG) {

			idenTask = new MetaLabMagSageTask((MetaParameterMag) this.metaPar, (MetaSourcesMag) msv, bar1, bar2, null);

		} else {

			System.out.println("Unknown workflow type.");

			return false;
		}

		idenTask.setLogBuilder(this.builder);

		idenTask.execute();

		return idenTask.get();
	}

	/**
	 * Stop all the pFind works
	 */
	@Override
	public void forceStop() {
		// TODO Auto-generated method stub

		this.cancel(true);

		if (this.dbcTask != null && !this.dbcTask.isDone()) {
			this.dbcTask.cancel(true);
		}

		if (this.idenTask != null && !this.idenTask.isDone()) {
			this.idenTask.cancel(true);
		}

		MetaData metadata = metaPar.getMetadata();
		String[] rawFiles = metadata.getRawFiles();

		HashSet<String> rawSet = new HashSet<String>();
		for (int i = 0; i < rawFiles.length; i++) {
			rawSet.add((new File(rawFiles[i])).getName());
		}

		String resultDir = metaPar.getResult();
		File spectraFile = new File(resultDir, "spectra");
		File[] mgfFiles = spectraFile.listFiles();
		for (int i = 0; i < mgfFiles.length; i++) {
			String name = mgfFiles[i].getName();
			if (name.endsWith("mgf")) {
				name = name.substring(0, name.length() - "mgf".length()) + "raw";
				if (rawSet.contains(name)) {
					rawSet.remove(name);
				}
			}
		}

		if (rawSet.size() > 0) {
			String name = "pParse.exe";
			Runtime run1 = Runtime.getRuntime();
			try {
				Process p = run1.exec(new String[] { "taskkill", "/F", "/IM", name });
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					if (lineStr.startsWith("SUCCESS")) {
						System.out.println(lineStr);
					}
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			name = "xtract_raw.exe";
			Runtime run2 = Runtime.getRuntime();
			try {
				Process p = run2.exec(new String[] { "taskkill", "/F", "/IM", name });
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					if (lineStr.startsWith("SUCCESS")) {
						System.out.println(lineStr);
					}
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			name = "pParse.exe";
			Runtime run3 = Runtime.getRuntime();
			try {
				Process p = run3.exec(new String[] { "taskkill", "/F", "/IM", name });
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					if (lineStr.startsWith("SUCCESS")) {
						System.out.println(lineStr);
					}
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}

		File pFindSpectra = new File(metaPar.getDbSearchResultFile(), "pFind-Filtered.spectra");
		if (pFindSpectra.exists()) {
			String name = "CMD.exe";
			Runtime run1 = Runtime.getRuntime();
			try {
				Process p = run1.exec(new String[] { "taskkill", "/F", "/IM", name });
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null) {
					if (lineStr.startsWith("SUCCESS")) {
						System.out.println(lineStr);
					}
				}
				if (p.waitFor() != 0) {
					if (p.exitValue() == 1)
						System.err.println("false");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			ArrayList<String> pfindExeList = new ArrayList<String>();
			pfindExeList.add("Group.exe");
			pfindExeList.add("PSSearcher.exe");
			pfindExeList.add("ReRanker.exe");
			pfindExeList.add("Searcher.exe");
			pfindExeList.add("TagSearcher.exe");

			for (int i = 0; i < pfindExeList.size(); i++) {

				String name = pfindExeList.get(i);
				Runtime run1 = Runtime.getRuntime();
				try {
					Process p = run1.exec(new String[] { "taskkill", "/F", "/IM", name });
					BufferedInputStream in = new BufferedInputStream(p.getInputStream());
					BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
					String lineStr;
					while ((lineStr = inBr.readLine()) != null) {
						if (lineStr.startsWith("SUCCESS")) {
							System.out.println(lineStr);
						}
					}
					if (p.waitFor() != 0) {
						if (p.exitValue() == 1)
							System.err.println("false");
					}
					inBr.close();
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
//		MetaSourcesDia msv2 = MetaSourcesIoDia.parse("D:\\Exported\\exe\\resources_DIA_1_0.json");
//		MetaDiaParSearchLib parV3 = MetaParIODiaSearchLib
//				.parse("Z:\\Kai\\Raw_files\\single_strain_dia\\29098\\MetaLab\\parameter.json");

		MetaParameterPFind par = MetaParaIOPFind
				.parse("C:\\Users\\kchen2\\.git\\repository\\metalab\\target\\classes\\parameters_MAG_1_1.json");
		MetaSourcesV2 msv2 = MetaSourcesIOV2
				.parse("C:\\Users\\kchen2\\.git\\repository\\metalab\\target\\classes\\resources_MAG_1_1.json");
		MetaLabMagTask task = new MetaLabMagTask(par, msv2, new JProgressBar(), new JProgressBar());

		task.execute();

		try {
			task.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
