package bmi.med.uOttawa.metalab.license;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParameterCreateTask extends SwingWorker<Boolean, Object> {

	private String name;
	private String affiliation;
	private String country;
	private String email;

	private File destFile;
	private JProgressBar bar;

	protected Logger LOGGER = LogManager.getLogger(ParameterCreateTask.class);

	/**
	 * Used in CMD mode
	 */
	public ParameterCreateTask(String file) {
		this(file, "", "", "", "", new JProgressBar());
	}

	public ParameterCreateTask(String file, String name, String affiliation, String country, String email,
			JProgressBar bar) {
		this(new File(file), name, affiliation, country, email, bar);
	}

	public ParameterCreateTask(File file, String name, String affiliation, String country, String email,
			JProgressBar bar) {

		this.destFile = file;
		this.name = name;
		this.affiliation = affiliation;
		this.country = country;
		this.email = email;
		this.bar = bar;

		addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				if ("progress".equals(evt.getPropertyName())) {
					bar.setValue((Integer) evt.getNewValue());
				}
			}
		});
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		boolean finished = false;

		bar.setStringPainted(true);

		PrintWriter writer = new PrintWriter(destFile);

		writer.println(LicenseConstants.NAME_STRING + "=" + this.name);

		writer.println(LicenseConstants.AFFILIATION_STRING + "=" + this.affiliation);

		writer.println(LicenseConstants.COUNTRY_STRING + "=" + this.country);

		writer.println(LicenseConstants.EMAIL_STRING + "=" + this.email);

		InetAddress inetAddress = InetAddress.getLocalHost();
		String localIp = inetAddress.toString();
		writer.println(LicenseConstants.IP_STRING + "=" + localIp);

		setProgress(10);

		byte[] mac = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}

			String str = Integer.toHexString(mac[i] & 0xff);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}
		writer.println(LicenseConstants.MAC_STRING + "=" + sb);
		setProgress(20);

		String wmic = "C:\\Windows\\System32\\wbem\\WMIC.exe";
		if (!((new File(wmic)).exists())) {
			System.out.println("This Windows version is not supported, "
					+ "minimum supported versions are Windows Vista for client and Windows Server 2008 for server");
			LOGGER.info("This Windows version is not supported, "
					+ "minimum supported versions are Windows Vista for client and Windows Server 2008 for server");
			writer.close();

			return false;
		}

		try {

			Process p = Runtime.getRuntime().exec(wmic + " baseboard get serialnumber");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (line.length() > 0 && !line.contains("SerialNumber")) {
					break;
				}
			}
			if (line == null) {
				System.out.println("Error in getting the baseboard serial number.");
			} else {
				writer.println(LicenseConstants.BASEBOARD_STRING + "=" + line);
			}
			input.close();

		} catch (IOException e) {
			LOGGER.error("Error in getting the baseboard serial number.", e);
		}

		try {
			Process p = Runtime.getRuntime().exec(wmic + " diskdrive get serialnumber");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (line.length() > 0 && !line.contains("SerialNumber")) {
					break;
				}
			}
			if (line == null) {
				System.out.println("Error in getting the diskdrive serial number.");
			} else {
				writer.println(LicenseConstants.DISKDRIVE_STRING + "=" + line);
			}
			input.close();

		} catch (IOException e) {
			LOGGER.error("Error in getting the diskdrive serial number.", e);
		}

		try {
			Process p = Runtime.getRuntime().exec(wmic + " bios get serialnumber");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (line.length() > 0 && !line.contains("SerialNumber")) {
					break;
				}
			}
			if (line == null) {
				System.out.println("Error in getting the bios serial number.");
			} else {
				writer.println(LicenseConstants.BIOS_STRING + "=" + line);
			}
			input.close();

		} catch (IOException e) {
			LOGGER.error("Error in getting the bios serial number.", e);
		}

		try {
			Process p = Runtime.getRuntime().exec(wmic + " cpu get processorId");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = input.readLine()) != null) {
				if (line.length() > 0 && !line.contains("ProcessorId")) {
					break;
				}
			}
			if (line == null) {
				System.out.println("Error in getting the ProcessorId number.");
			} else {
				writer.println(LicenseConstants.CPU_STRING + "=" + line);
			}
			input.close();

		} catch (IOException e) {
			LOGGER.error("Error in getting the cpu serial number.", e);
		}

		writer.close();

		setProgress(100);
		bar.setString("Finished");

		finished = true;

		return finished;
	}


}
