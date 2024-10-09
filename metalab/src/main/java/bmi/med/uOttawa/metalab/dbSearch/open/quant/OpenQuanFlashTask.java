/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.quant;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class OpenQuanFlashTask extends SwingWorker<Object, Object> {

	private static final Logger LOGGER = LogManager.getLogger(OpenQuanFlashTask.class);
	private static final String flashLFQ = "Resources\\FlashLFQ\\CMD.exe";

	@Override
	protected Object doInBackground() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static void help() {
		String cmd = flashLFQ + " --help";
		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
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

	public static void batchQuan(String msms, String rawDir) {
		String cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"" + " --pro true";
		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				LOGGER.info(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in quantifying PSMs from " + msms, e);
		}
	}
	
	public static void batchQuan(File msms, File rawDir) {
		String cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"";
		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				LOGGER.info(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in quantifying PSMs from " + msms, e);
		}
	}

	public static void batchQuan(String flashLFQ, File msms, File rawDir) {
//		String cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"" + " --pro true";
		String cmd = flashLFQ + " --idt " + "\"" + msms + "\"" + " --rep " + "\"" + rawDir + "\"";
		LOGGER.info(cmd);

		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				LOGGER.info(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in quantifying PSMs from " + msms, e);
		}
	}
}
