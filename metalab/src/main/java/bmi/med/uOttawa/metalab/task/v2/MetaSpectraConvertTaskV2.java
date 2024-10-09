/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.spectra.io.MSConvertor;
import bmi.med.uOttawa.metalab.spectra.io.ProteomicsTools;
import bmi.med.uOttawa.metalab.task.MetaAbstractTask;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;

/**
 * @author Kai Cheng
 *
 */
public class MetaSpectraConvertTaskV2 extends MetaAbstractTask {

	private static Logger LOGGER = LogManager.getLogger(MetaSpectraConvertTaskV2.class);
	private String taskName = "Spectra format convert";

	public MetaSpectraConvertTaskV2(MetaParameter metaPar, MetaSourcesV2 advPar, JProgressBar bar1, JProgressBar bar2,
			SwingWorker<Boolean, Object> nextWork) {
		super(metaPar, advPar, bar1, bar2, nextWork);
	}

	protected void initial() {
		
	}
	
	/**
	 * Multiple threads with ExecutorService, not used now
	 * 
	 * @deprecated
	 * @return
	 * @throws Exception
	 */
	protected Boolean doInBackgroundNotUsed() throws Exception {

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		String[] raws = metaPar.getMetadata().getRawFiles();
		String spectraDir = metaPar.getSpectraFile().getAbsolutePath();
		String convertCmd = msv.getResource() + "\\ProteoWizard\\msconvert.exe";

		ExecutorService es = Executors.newFixedThreadPool(metaPar.getThreadCount());

		for (int i = 0; i < raws.length; i++) {
			File rawi = new File(raws[i]);

			String rawname = rawi.getName();
			String mgfname = rawname.substring(0, rawname.lastIndexOf(".")) + ".mgf";
			File mgfi = new File(spectraDir, mgfname);

			if (!mgfi.exists()) {
				if (rawname.endsWith("raw") || rawname.endsWith("RAW")) {

					File file = new File(raws[i]);

					String cmd = convertCmd + " " + file + " --mgf " + "-o " + spectraDir
							+ " --zlib --filter \"peakPicking true 1-\"";

					Runnable runable = new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							try {
								Process p = Runtime.getRuntime().exec(cmd);
								BufferedInputStream in = new BufferedInputStream(p.getInputStream());
								BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
								String line0 = "";
								String line1 = "";
								while ((line1 = inBr.readLine()) != null) {
									if (!line1.equals(line0)) {
										System.out.println(format.format(new Date()) + "\t" + line1);
										line0 = line1;
									}
								}

								if (p.waitFor() != 0) {
									if (p.exitValue() == 1)
										LOGGER.error("false");
								}
								inBr.close();
								in.close();

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					es.execute(runable);
				}
			}
		}

		try {
			es.shutdown();

			boolean finish = es.awaitTermination(2, TimeUnit.HOURS);

			if (finish) {

				for (int i = 0; i < raws.length; i++) {
					File rawi = new File(raws[i]);

					String rawname = rawi.getName();
					String mgfname = rawname.substring(0, rawname.lastIndexOf(".")) + ".mgf";
					File mgfi = new File(spectraDir, mgfname);

					if (!mgfi.exists()) {
						return false;
					}
				}

				LOGGER.info(taskName + ": extract spectra from raw file finished");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file finished");
			} else {
				LOGGER.info(taskName + ": extract spectra from raw file failed");
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file failed");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": extract spectra from raw file failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": extract spectra from raw file failed");
		}

		return true;
	}

	/**
	 * Multiple threads by set "ParallelMode" as true in ProteomicsTools parameter
	 */
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		bar2.setString(taskName);

		LOGGER.info(taskName + ": start");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": start");

		ProteomicsTools pt = new ProteomicsTools(metaPar, (MetaSourcesV2) msv);

		String[] raws = metaPar.getMetadata().getRawFiles();
		File spectraDir = metaPar.getSpectraFile();
		if (!spectraDir.exists()) {
			spectraDir.mkdir();
		}

		File[] mgfs = spectraDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if (arg0.getName().endsWith("mgf") || arg0.getName().endsWith("MGF"))
					return true;

				return false;
			}
		});

		boolean isMzML = false;
		ArrayList<String> rawlist = new ArrayList<String>();

		if (mgfs.length > 0) {

			for (int i = 0; i < raws.length; i++) {

				File rawi = new File(raws[i]);
				String rawname = rawi.getName();
				String suffix = rawname.substring(rawname.lastIndexOf(".") + 1);
				rawname = rawname.substring(0, rawname.length() - suffix.length() - 1);

				File mgfi = null;

				for (int j = 0; j < mgfs.length; j++) {
					if (mgfs[j].getName().startsWith(rawname) && mgfs[j].length() > 0) {
						mgfi = mgfs[j];
					}
				}

				if (mgfi == null) {

					mgfi = new File(spectraDir, rawname + ".mgf");

					if (suffix.equalsIgnoreCase("raw")) {

						rawlist.add(rawi.getAbsolutePath());

					} else if (suffix.equalsIgnoreCase("mzML")) {
						isMzML = true;
						rawlist.add(rawi.getAbsolutePath());
					} else {
						LOGGER.error(taskName + ": cannot convert raw file " + rawi
								+ " to mgf file, only .raw, .mzML and .mzXML are supported");
						System.err.println(format.format(new Date()) + "\t" + taskName + ": cannot convert raw file "
								+ rawi + " to mgf file, only .raw, .mzML and .mzXML are supported");
						return false;
					}

				} else {
					LOGGER.info(taskName + ": mgf file " + mgfi.getName() + " already exist in " + spectraDir);
					System.out.println(format.format(new Date()) + "\t" + taskName + ": mgf file " + mgfi.getName()
							+ " already exist in " + spectraDir);
				}
			}
		} else {
			for (int i = 0; i < raws.length; i++) {

				File rawi = new File(raws[i]);
				String rawname = rawi.getName();
				String suffix = rawname.substring(rawname.lastIndexOf(".") + 1);
				rawname = rawname.substring(0, rawname.length() - suffix.length() - 1);

				if (suffix.equalsIgnoreCase("raw")) {

					rawlist.add(rawi.getAbsolutePath());

				} else if (suffix.equalsIgnoreCase("mzML")) {
					isMzML = true;
					rawlist.add(rawi.getAbsolutePath());
				} else {
					LOGGER.error(taskName + ": cannot convert raw file " + rawi
							+ " to mgf file, only .raw and .mzXML are supported");
					System.err.println(format.format(new Date()) + "\t" + taskName + ": cannot convert raw file " + rawi
							+ " to mgf file, only .raw and .mzXML are supported");
					return false;
				}
			}
		}

		if (isMzML) {

			File resourceFile = (new File(((MetaSourcesV2) msv).getFlashlfq())).getParentFile().getParentFile();
			File proteoWizardFile = new File(resourceFile, "ProteoWizard");
			File msconvertFile = new File(proteoWizardFile, "msconvert.exe");

			if (msconvertFile.exists()) {

				ExecutorService es = Executors.newFixedThreadPool(metaPar.getThreadCount());

				for (int i = 0; i < rawlist.size(); i++) {

					String rawi = rawlist.get(i);

					Runnable runable = new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							MSConvertor.mzml2mgf(msconvertFile.getAbsolutePath(), rawi, spectraDir.getAbsolutePath());
						}
					};
					es.submit(runable);
				}

				es.shutdown();

				try {
					boolean finish = es.awaitTermination(rawlist.size() * 4, TimeUnit.HOURS);
					if (finish) {
						System.out.println(format.format(new Date()) + "\t" + taskName + ": msconvert finished");
						LOGGER.info(taskName + ": msconvert finished");
					} else {
						System.out.println(format.format(new Date()) + "\t" + taskName + ": msconvert failed");
						LOGGER.info(taskName + ": msconvert failed");
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println(format.format(new Date()) + "\t" + taskName + ": msconvert was not found, please "
						+ "put \"ProteoWizard\" into the resource folder");
				LOGGER.info(taskName + ": msconvert was not found, please "
						+ "put \"ProteoWizard\" into the resource folder");
			}

		} else {
			pt.raws2Mgfs(rawlist.toArray(new String[rawlist.size()]), spectraDir.getAbsolutePath());
		}

		setProgress(10);

		LOGGER.info(taskName + ": converting raw files finished");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": converting raw files finished");

		return true;
	}

	@Override
	protected String getTaskName() {
		// TODO Auto-generated method stub
		return taskName;
	}

	@Override
	protected Logger getLogger() {
		// TODO Auto-generated method stub
		return LOGGER;
	}

}
