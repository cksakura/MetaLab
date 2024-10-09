package bmi.med.uOttawa.metalab.task.pfind.gui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.license.LicenseConstants;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.license.ParameterCreateTask;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSourcePathDialog;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JProgressBar;

import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;

public class MetaLabLicenseFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7666982682654184423L;

	private JTextField propertyTextField;
	private JTextField licenseTextField;
	private JProgressBar propertyBar;
	private JProgressBar licenseBar;
	private JTextField nameTextField;
	private JTextField affiliaTextField;
	private JTextField countryTextField;
	private JTextField emailTextField;

	private File propertyFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MetaLabLicenseFrame frame = new MetaLabLicenseFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public MetaLabLicenseFrame() throws URISyntaxException, IOException {

		this.propertyFile = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath(),
				"user.properties");

		String nameString = "";
		String affiliationString = "";
		String countryString = "";
		String emailString = "";
		if (propertyFile.exists() && propertyFile.isFile()) {
			BufferedReader reader = new BufferedReader(new FileReader(this.propertyFile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(LicenseConstants.NAME_STRING)
						&& line.length() > LicenseConstants.NAME_STRING.length() + 1) {
					nameString = line.substring(LicenseConstants.NAME_STRING.length() + 1);
				} else if (line.startsWith(LicenseConstants.AFFILIATION_STRING)
						&& line.length() > LicenseConstants.AFFILIATION_STRING.length() + 1) {
					affiliationString = line.substring(LicenseConstants.AFFILIATION_STRING.length() + 1);
				} else if (line.startsWith(LicenseConstants.COUNTRY_STRING)
						&& line.length() > LicenseConstants.COUNTRY_STRING.length() + 1) {
					countryString = line.substring(LicenseConstants.COUNTRY_STRING.length() + 1);
				} else if (line.startsWith(LicenseConstants.EMAIL_STRING)
						&& line.length() > LicenseConstants.EMAIL_STRING.length() + 1) {
					emailString = line.substring(LicenseConstants.EMAIL_STRING.length() + 1);
				}
			}
			reader.close();
		}

		setTitle("Activation");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabSourcePathDialog.class.getResource("/bmi/med/uOttawa/metalab/icon/metalab_128.png")));
		setBounds(100, 100, 800, 600);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[180][100][grow][200]"));

		JPanel exportParPanel = new JPanel();
		exportParPanel.setBorder(new TitledBorder(null, "Generate a license property file for the verification",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(exportParPanel, "cell 0 0,grow");
		exportParPanel.setLayout(new MigLayout("", "[80][15][50,grow][50][80][50,grow][15][50]", "[30][30][30][30]"));

		JLabel nameLabel = new JLabel("Name");
		exportParPanel.add(nameLabel, "cell 0 0");

		nameTextField = new JTextField();
		exportParPanel.add(nameTextField, "cell 2 0,growx");
		nameTextField.setColumns(10);
		nameTextField.setText(nameString);

		JLabel affiliationLabel = new JLabel("Affiliation");
		exportParPanel.add(affiliationLabel, "cell 4 0");

		affiliaTextField = new JTextField();
		exportParPanel.add(affiliaTextField, "cell 5 0,growx");
		affiliaTextField.setColumns(10);
		affiliaTextField.setText(affiliationString);

		JLabel countryLabel = new JLabel("Country");
		exportParPanel.add(countryLabel, "cell 0 1");

		countryTextField = new JTextField();
		exportParPanel.add(countryTextField, "cell 2 1,growx");
		countryTextField.setColumns(10);
		countryTextField.setText(countryString);

		JLabel emailLabel = new JLabel("Email");
		exportParPanel.add(emailLabel, "cell 4 1");

		emailTextField = new JTextField();
		exportParPanel.add(emailTextField, "cell 5 1,growx");
		emailTextField.setColumns(10);
		emailTextField.setText(emailString);

		JLabel lblNewLabel_2 = new JLabel("Save to path");
		exportParPanel.add(lblNewLabel_2, "cell 0 2");

		propertyTextField = new JTextField();
		exportParPanel.add(propertyTextField, "cell 2 2 4 1,growx");
		propertyTextField.setColumns(10);

		JButton generateBrowseButton = new JButton("Browse");
		generateBrowseButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save license property file");

			int value = fileChooser.showSaveDialog(MetaLabLicenseFrame.this);

			if (value == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				if (!fileToSave.getName().endsWith(".properties")) {
					fileToSave = new File(fileToSave.getParent(), fileToSave.getName() + ".properties");
				}

				propertyTextField.setText(fileToSave.getAbsolutePath());
			}
		});
		exportParPanel.add(generateBrowseButton, "cell 7 2,growx");

		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(l -> {

			String name = nameTextField.getText();
			if (name == null || name.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please input your name", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String affiliation = affiliaTextField.getText();
			if (affiliation == null || affiliation.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please input your affiliation", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			String country = countryTextField.getText();
			if (country == null || country.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please input your country", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			String email = emailTextField.getText();
			if (email == null || email.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please input your email", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String path = propertyTextField.getText();
			if (path == null || path.length() == 0) {
				JOptionPane.showMessageDialog(this, "Please specify an output path", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			ParameterCreateTask task = new ParameterCreateTask(path, name, affiliation, country, email, propertyBar);

			task.execute();
		});
		exportParPanel.add(generateButton, "cell 0 3,growx");

		propertyBar = new JProgressBar();
		exportParPanel.add(propertyBar, "cell 2 3 6 1,growx");

		JPanel verifyPanel = new JPanel();
		verifyPanel.setBorder(
				new TitledBorder(null, "Install the license", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(verifyPanel, "cell 0 1,grow");
		verifyPanel.setLayout(new MigLayout("", "[80][15][200,grow][15][50]", "[30][30]"));

		JLabel lblNewLabel_4 = new JLabel("Path");
		verifyPanel.add(lblNewLabel_4, "cell 0 0");

		licenseTextField = new JTextField();
		verifyPanel.add(licenseTextField, "cell 2 0,growx");
		licenseTextField.setColumns(10);

		JButton installBrowseButton = new JButton("Browse");
		verifyPanel.add(installBrowseButton, "cell 4 0,growx");
		installBrowseButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load license file");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.addChoosableFileFilter(new FileFilter() {
				public String getDescription() {
					return "MetaLab license file (.lic)";
				}

				public boolean accept(File f) {
					if (f.getName().endsWith("lic")) {
						return true;
					} else {
						return false;
					}
				}
			});

			int returnValue = fileChooser.showOpenDialog(MetaLabLicenseFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				licenseTextField.setText(file.getAbsolutePath());
			}
		});

		JButton installButton = new JButton("Install");
		installButton.addActionListener(l -> {

			File licenseFile = new File(licenseTextField.getText());
			if (!licenseFile.exists()) {
				JOptionPane.showMessageDialog(this, "Please load the license file", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			try {
				FileUtils.copyFile(licenseFile, new File(
						ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath(), licenseFile.getName()));
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SwingWorker<Boolean, Object> task = new SwingWorker<Boolean, Object>() {

				@Override
				protected Boolean doInBackground() throws Exception {
					// TODO Auto-generated method stub

					licenseBar.setStringPainted(true);
					licenseBar.setIndeterminate(true);

					LicenseVerifier verifier = new LicenseVerifier();
					boolean finish = verifier.install(licenseFile);

					licenseBar.setIndeterminate(false);

					if (finish) {
						licenseBar.setValue(100);
						licenseBar.setString("Finished");
					} else {
						licenseBar.setString("Failed");
					}

					return finish;
				}
			};

			task.execute();

			try {
				boolean successed = task.get();
				if (successed) {

					JOptionPane.showMessageDialog(this, "License installation successed", "Info",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "License installation failed", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}

			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(this, e, "Warning", JOptionPane.WARNING_MESSAGE);
			}
		});
		verifyPanel.add(installButton, "cell 0 1,growx");

		licenseBar = new JProgressBar();
		verifyPanel.add(licenseBar, "cell 2 1 3 1,growx");

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel, "cell 0 2,grow");
		panel.setLayout(new MigLayout("", "[300,grow]", "[150]"));

		JTextArea txtrInfo = new JTextArea();
		txtrInfo.setText(
				"Please following these steps to activate the MetaLab:\r\n1. Generate a license property file from the above panel;\r\n2. Go to https://imetalab.ca/license, use the online activation tool to create a\r\nlicense file;\r\n3. Install the license.\r\n\r\nThank you for using MetaLab and enjoy the data analysis！");
		txtrInfo.setLineWrap(true);
		txtrInfo.setEditable(false);
		panel.add(txtrInfo, "cell 0 0,grow");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 3,grow");

		ConsoleTextArea consoleTextArea;
		try {
			consoleTextArea = new ConsoleTextArea();
			scrollPane.setViewportView(consoleTextArea);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ParameterCreateTask task = new ParameterCreateTask(propertyFile, nameTextField.getText(),
						affiliaTextField.getText(), countryTextField.getText(), emailTextField.getText(),
						new JProgressBar());
				task.execute();

				e.getWindow().dispose();
			}
		});
	}

}
