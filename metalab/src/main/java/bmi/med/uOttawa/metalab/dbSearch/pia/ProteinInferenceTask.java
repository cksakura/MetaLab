/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.pia;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class ProteinInferenceTask {

	private static String parameter = "Resources\\pia\\parameter.xml";

//	private static String piaJar = "Resources\\pia\\pia-1.3.10.jar";

	private static String jar2 = "D:\\software\\Bio\\pia-1.3.6\\pia.jar";

	private static final Logger LOGGER = LogManager.getLogger(ProteinInferenceTask.class);

	public static void PIAByJAR(String input, String peptideOut, String proteinOut) {

		String cmd = "java -Xmx12g -jar " + jar2 + " -infile " + input + " -paramFile " + parameter + " -peptideExport "
				+ peptideOut + " csv " + " -proteinExport " + proteinOut + " csv";
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
			LOGGER.error("Error in PIA analysis from " + input, e);
		}
	}

	public static void PIA2(String input, String peptideOut, String proteinOut) {

		String cmd = "java -Xmx12g -cp " + "de.mpc.pia.intermediate.compiler.PIACompiler" + " -infile " + input
				+ " -paramFile " + parameter + " -peptideExport " + peptideOut + " csv " + " -proteinExport "
				+ proteinOut + " csv";
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
			LOGGER.error("Error in PIA analysis from " + input, e);
		}
	}

}
