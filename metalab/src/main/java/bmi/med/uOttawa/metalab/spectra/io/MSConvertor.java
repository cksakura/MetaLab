/**
 * 
 */
package bmi.med.uOttawa.metalab.spectra.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Kai Cheng
 *
 */
public class MSConvertor {
	
	private static String tofString = "--filter \"analyzer TOF\"";
	private static String polarityString = "--filter \"polarity  positive\"";
	private static String scanSumString = "--filter \"scanSumming precursorTol=0.05 scanTimeTol=5 ionMobilityTol=0.1\"";
	private static String titleMakerString = "--filter \"titleMaker <RunId>.<ScanNumber>.<ScanNumber>.<ChargeState>\"";

	private static final String convertCmd = "Resources//ProteoWizard//msconvert.exe";
	
	public static void d2mgf(String convertCmd, String raw, String resultDir) {

		File file = new File(raw);
		String rawName = file.getName();
		rawName = rawName.substring(0, rawName.lastIndexOf("."));

		File batFile = new File(resultDir, rawName + ".bat");
		File msconvertFile = new File(convertCmd);
		int id = convertCmd.indexOf(":");
		if (id < 0) {
			return;
		}

		try {

			PrintWriter writer = new PrintWriter(batFile);
			writer.println(convertCmd.substring(0, id) + ":");
			writer.println("cd " + msconvertFile.getParent());
			writer.println("msconvert.exe " + "\"" + file + "\"" + " --mgf " + "-o " + "\"" + resultDir + "\" "
					+ tofString + " " + polarityString + " " + scanSumString + " " + titleMakerString);
			writer.print("exit");
			writer.close();

			ArrayList<String> list = new ArrayList<String>();
			list.add("cmd.exe");
			list.add("/c");
			list.add("start");
			list.add(batFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(list.toArray(new String[list.size()]));

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
	
	public static void mzml2mgf(String convertCmd, String raw, String resultDir) {

		File file = new File(raw);
		String rawName = file.getName();
		rawName = rawName.substring(0, rawName.lastIndexOf("."));

		File batFile = new File(resultDir, rawName + ".bat");
		File msconvertFile = new File(convertCmd);
		int id = convertCmd.indexOf(":");
		if (id < 0) {
			return;
		}

		try {

			PrintWriter writer = new PrintWriter(batFile);
			writer.println(convertCmd.substring(0, id) + ":");
			writer.println("cd " + msconvertFile.getParent());
			writer.println("msconvert.exe " + "\"" + file + "\"" + " --mgf " + "-o " + "\"" + resultDir + "\" "
					+ tofString + " " + polarityString + " " + scanSumString + " " + titleMakerString);
			writer.print("exit");
			writer.close();

			ArrayList<String> list = new ArrayList<String>();
			list.add("cmd.exe");
			list.add("/c");
			list.add("start");
			list.add(batFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(list.toArray(new String[list.size()]));

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

	public static void raw2mgf(String convertCmd, String raw, String resultDir) {

		File file = new File(raw);
		String cmd = convertCmd + " " + file + " --mgf " + "-o " + resultDir
				+ " --zlib --filter \"peakPicking true 1-\"";

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

	public static File raw2mgf(String raw) {
		return raw2mgf(new File(raw));
	}

	public static File raw2mgf(File raw) {
		String parent = raw.getParent();
		String cmd = convertCmd + " " + raw + " --mgf " + "-o " + parent + " --zlib --filter \"peakPicking true 1-\"";

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

		String name = raw.getName();
		name = name.substring(0, name.lastIndexOf(".")) + ".mgf";
		return new File(parent, name);
	}

	public static String raw2Mzxml(String raw) {
		File file = new File(raw);
		String parent = file.getParent();
		String cmd = convertCmd + " " + file + " --mzXML " + "-o " + parent
				+ " --zlib --filter \"peakPicking true 1-\"";

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

		String name = file.getName();
		name = name.substring(0, name.lastIndexOf(".")) + ".mzXML";
		return parent + "\\" + name;
	}

	public static void help() {
		String cmd = convertCmd + " --help";
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

	public static void main(String[] args) {
		MSConvertor.raw2mgf("E:\\Exported\\Resources\\ProteoWizard\\msconvert.exe", 
				"Z:\\Kai\\Raw_files\\test\\S11_Fraction2.raw", 
				"Z:\\Kai\\Raw_files\\test\\MetaLab_maxquant\\spectra");
	}
}
