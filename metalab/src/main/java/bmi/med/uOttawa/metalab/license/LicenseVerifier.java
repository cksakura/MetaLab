package bmi.med.uOttawa.metalab.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;
import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultKeyStoreParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseContentException;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseParam;

public class LicenseVerifier {

	protected Logger LOGGER = LogManager.getLogger(LicenseVerifier.class);
	protected ConfigurationBuilder<BuiltConfiguration> builder;

	private ArrayList<String> infoList;

	public LicenseVerifier() {
		String path = System.getProperty("user.dir");
		configLog(new File(path));
	}

	/**
	 * Install the license
	 */
	public boolean install(File licenseFile) {
		try {
			LicenseManager licenseManager = getLicenseManager();
			licenseManager.install(licenseFile);
			LOGGER.info("License installation succeeded");
			return true;
		} catch (Exception e) {
			LOGGER.error("License installation failed", e);
			return false;
		}
	}

	protected void configLog(File logDir) {
		String pattern = "%d %p %c [%t] %m%n";

		this.builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		builder.setStatusLevel(Level.INFO);
		builder.setConfigurationName("DefaultFileLogger");

		// set the pattern layout and pattern
		LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern", pattern);

		// create a file appender
		AppenderComponentBuilder appenderBuilder = builder.newAppender("LogToFile", "File")
				.addAttribute("fileName", logDir + "\\logging.log").add(layoutBuilder);

		builder.add(appenderBuilder);

		RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.INFO);
		rootLogger.add(builder.newAppenderRef("LogToFile"));
		builder.add(rootLogger);

		Configurator.reconfigure(builder.build());
	}

	/**
	 * Initial the parameters
	 */
	private LicenseParam initLicenseParams() {
		Properties inherentProp = new Properties();
		try (InputStream in = ResourceLoader.load(LicenseConstants.INHERENTPATH_STRING)) {
			inherentProp.load(in);
		} catch (IOException e) {
			LOGGER.error("Error in loading the inherent properties", e);
		}

		Preferences pre = Preferences.userNodeForPackage(LicenseVerifier.class);
		CipherParam cipherParam = new DefaultCipherParam(inherentProp.getProperty(LicenseConstants.STOREPWD_STRING));

		KeyStoreParam pubStoreParam = new DefaultKeyStoreParam(LicenseVerifier.class, LicenseConstants.PUBPATH_STRING,
				inherentProp.getProperty(LicenseConstants.KEYALIAS_STRING),
				inherentProp.getProperty(LicenseConstants.STOREPWD_STRING), null);

		InputStream in = LicenseVerifier.class.getResourceAsStream(LicenseConstants.PUBPATH_STRING);
		if (in == null) {
			in = LicenseVerifier.class.getResourceAsStream("/resources" + LicenseConstants.PUBPATH_STRING);
			if (in == null) {
				LOGGER.info("Can't load resource file " + LicenseConstants.PUBPATH_STRING);
			} else {
				pubStoreParam = new DefaultKeyStoreParam(LicenseVerifier.class,
						"/resources" + LicenseConstants.PUBPATH_STRING,
						inherentProp.getProperty(LicenseConstants.KEYALIAS_STRING),
						inherentProp.getProperty(LicenseConstants.STOREPWD_STRING), null);
			}

		} else {
			pubStoreParam = new DefaultKeyStoreParam(LicenseVerifier.class, LicenseConstants.PUBPATH_STRING,
					inherentProp.getProperty(LicenseConstants.KEYALIAS_STRING),
					inherentProp.getProperty(LicenseConstants.STOREPWD_STRING), null);
		}

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new DefaultLicenseParam(inherentProp.getProperty(LicenseConstants.SUBJECT_STRING), pre, pubStoreParam,
				cipherParam);
	}

	private LicenseManager getLicenseManager() {
		return LicenseManagerHolder.getLicenseManager(initLicenseParams());
	}

	public ArrayList<String> getVerifyInfo() {
		return this.infoList;
	}

	/**
	 * Verify the license
	 */
	@SuppressWarnings("unchecked")
	public boolean verify() {

		boolean verified = true;
		this.infoList = new ArrayList<String>();

		try {
			LicenseManager licenseManager = getLicenseManager();
			LicenseContent verify = licenseManager.verify();

			LOGGER.info("Verification started");
			HashMap<String, String> extra = (HashMap<String, String>) verify.getExtra();

			InetAddress inetAddress = InetAddress.getLocalHost();
			/*
			 * String ip = extra.get(LicenseConstants.IP_STRING); String localIp =
			 * inetAddress.toString(); if (!Objects.equals(ip, localIp)) {
			 * LOGGER.error("IP address verification failed"); return false; }
			 */
			String mac = extra.get(LicenseConstants.MAC_STRING);
			String localMac = getLocalMac(inetAddress);
			if (!Objects.equals(mac, localMac)) {
				LOGGER.error("MAC address verification failed");
				this.infoList.add("MAC address verification failed");

				verified = false;
			}

			String wmic = "C:\\Windows\\System32\\wbem\\WMIC.exe";
			if (!((new File(wmic)).exists())) {
				System.out.println("This Windows version is not supported, "
						+ "minimum supported versions are Windows Vista for client and Windows Server 2008 for server");
				LOGGER.info("This Windows version is not supported, "
						+ "minimum supported versions are Windows Vista for client and Windows Server 2008 for server");

				this.infoList.add("This Windows version is not supported, "
						+ "minimum supported versions are Windows Vista for client and Windows Server 2008 for server");

				verified = false;
			}

			try {
				boolean match = false;
				String baseboard = extra.get(LicenseConstants.BASEBOARD_STRING);
				Process p = Runtime.getRuntime().exec(wmic + " baseboard get serialnumber");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line.equals(baseboard)) {
						match = true;
						break;
					}
				}

				input.close();

				if (!match) {
					System.err.println("Baseboard serial number does not match");
					LOGGER.error("Baseboard serial number does not match");
					this.infoList.add("Baseboard serial number does not match");

					verified = false;
				}

			} catch (Exception e) {
				System.err.println("Baseboard serial number verification failed");
				LOGGER.error("Baseboard serial number verification failed");
				this.infoList.add("Baseboard serial number verification failed");

				verified = false;
			}

			try {
				boolean match = false;
				String diskdrive = extra.get(LicenseConstants.DISKDRIVE_STRING);
				Process p = Runtime.getRuntime().exec(wmic + " diskdrive get serialnumber");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line.equals(diskdrive)) {
						match = true;
						break;
					}
				}

				input.close();

				if (!match) {
					System.err.println("Diskdrive serial number does not match");
					LOGGER.error("Diskdrive serial number does not match");
					this.infoList.add("Diskdrive serial number does not match");

					verified = false;
				}

			} catch (Exception e) {
				System.err.println("Diskdrive serial number verification failed");
				LOGGER.error("Diskdrive serial number verification failed");
				this.infoList.add("Diskdrive serial number verification failed");
				verified = false;
			}

			try {
				boolean match = false;
				String bios = extra.get(LicenseConstants.BIOS_STRING);
				Process p = Runtime.getRuntime().exec(wmic + " bios get serialnumber");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line.equals(bios)) {
						match = true;
						break;
					}
				}

				input.close();

				if (!match) {
					System.err.println("Bios serial number does not match");
					LOGGER.error("Bios serial number does not match");
					this.infoList.add("Bios serial number does not match");
					verified = false;
				}

			} catch (Exception e) {
				System.err.println("Bios serial number verification failed");
				LOGGER.error("Bios serial number verification failed");
				this.infoList.add("Bios serial number verification failed");
				verified = false;
			}

			try {
				boolean match = false;
				String cpu = extra.get(LicenseConstants.CPU_STRING);
				Process p = Runtime.getRuntime().exec(wmic + " cpu get processorId");
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = input.readLine()) != null) {
					if (line.equals(cpu)) {
						match = true;
						break;
					}
				}

				input.close();

				if (!match) {
					System.err.println("CPU serial number does not match");
					LOGGER.error("CPU serial number does not match");
					this.infoList.add("CPU serial number does not match");
					verified = false;
				}

			} catch (Exception e) {
				System.err.println("CPU serial number verification failed");
				LOGGER.error("CPU serial number verification failed");
				this.infoList.add("CPU serial number verification failed");
				verified = false;
			}

			LOGGER.info("Verification succeeded");

			return verified;

		} catch (LicenseContentException ex) {
			LOGGER.error("Certification expired", ex);
			this.infoList.add("Certification expired");
			return false;

		} catch (Exception e) {
			LOGGER.error("Verification failed", e);
			this.infoList.add("Verification failed");

			return false;
		}
	}

	/**
	 * Get mac address
	 *
	 * @param inetAddress
	 * @throws SocketException
	 */
	private String getLocalMac(InetAddress inetAddress) throws SocketException {
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
		return sb.toString();
	}

	public static void propertyTest(String property, String license) {
		LicenseVerifier verifier = new LicenseVerifier();
		verifier.install(new File(license));

		LicenseManager licenseManager = verifier.getLicenseManager();
		LicenseContent verify;
		try {
			verify = licenseManager.verify();
			HashMap<String, String> extra = (HashMap<String, String>) verify.getExtra();

			System.out.println(extra);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		LicenseVerifier verifier = new LicenseVerifier();
		boolean verified = verifier.verify();
		System.out.println("verify\t"+verified);
	}

}
