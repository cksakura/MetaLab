/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.dbSearch.alphapept.AlphapeptTask;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantTask {

	private static final String MaxQuantCmd = "Resources//mq_bin//MaxQuantCmd.exe";
	private static final String MaxQuantPara = "Resources//mq_bin//mqpar.xml";

	private Appendable log;
	private static String taskName = "Alphapept task";
	private static final Logger LOGGER = LogManager.getLogger(AlphapeptTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	
	/**
	 * Be careful with this error!!!
	 * 
	 * Problem in parameter!!!
	 * 
	 * Right <label> <string/> </label>
	 * 
	 * Wrong <label/>
	 * 
	 * @param log
	 */
	public MaxquantTask(Appendable log) {
		this.log = log;
	}

	public boolean run(String mqCmd, String par) {
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}

		try {
			File mqCmdFile = new File(mqCmd);
			StringBuilder sb = new StringBuilder();
			sb.append(mqCmdFile.getName()).append(" ");
			sb.append(par);

			int id = mqCmdFile.getAbsolutePath().indexOf(":");
			if (id < 0) {
				System.out.println(
						format.format(new Date()) + "\t" + taskName + ": cann't find the directory of MaxQuantCmd.exe");
				LOGGER.error(taskName + ": cann't find the directory of MaxQuantCmd.exe");
				return false;
			}

			File batFile = new File(par + ".bat");
			PrintWriter batWriter = new PrintWriter(batFile);

			batWriter.println(mqCmdFile.getAbsolutePath().substring(0, id) + ":");
			batWriter.println("cd " + mqCmdFile.getParent());
			batWriter.println(sb);
			batWriter.println("exit");
			batWriter.close();

			System.out.println(format.format(new Date()) + "\t" + sb);
			LOGGER.info(taskName + ": " + sb);

			String[] args = { "cmd.exe", "/c", "start", batFile.getAbsolutePath() };

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error("Error in database searching by MaxQuant");
				}
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by MaxQuant", e);
			return false;
		}
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
		
		return true;
	}
	
	public void run(String par) {
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
		String cmd = MaxQuantCmd + " " + par;
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error("Error in database searching by MaxQuant");
				}
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by MaxQuant", e);
		}
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}
	
	public void run() {
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
		String cmd = MaxQuantCmd + " " + MaxQuantPara;
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error("Error in database searching by MaxQuant");
				}
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by MaxQuant", e);
		}
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}

	public void run(MaxquantParameter parameter, String paraPath) {
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search started" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
		try {
			parameter.export(paraPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in exporting parameter file to " + paraPath, e);
		}
		String cmd = MaxQuantCmd + " " + paraPath;
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1) {
					LOGGER.error("Error in database searching by MaxQuant");
				}
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			LOGGER.error("Error in database searching by MaxQuant", e);
		}
		try {
			log.append(format.format(new Date()) + "\t" + "Maxquant search finished" + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing log file", e);
		}
	}
	
	public static String getMaxQuantParameter() {
		return MaxQuantPara;
	}

	public static void main(String[] args) throws IOException, DocumentException {
		// TODO Auto-generated method stub

		MaxquantTask task = new MaxquantTask(new StringBuilder());
		task.run("D:\\MetaLab\\Project\\Resources\\mq_bin\\MaxQuantCmd.exe", 
				"D:\\Data\\MetaLab_test\\wash_long_column\\mq_1623\\parameter\\pep_iden_par.xml");
	}

}
