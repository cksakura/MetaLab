package bmi.med.uOttawa.metalab.task.mag.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import bmi.med.uOttawa.metalab.core.MGYG.HapExtractor;
import bmi.med.uOttawa.metalab.core.MGYG.MGYGFtpTask;
import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagVersion;

import javax.swing.JProgressBar;

public class MagDbConfigPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField latestVerTextField;
	private JTextField mgnifyOldVerTextField;
	private JTextField spCountTextField;
	private JTextField mgnifyRepoTextField;
	private JTextField localNameTextField;
	private JTextField identifierTextField;
	private JTextField localRepoTextField;
	
	private JComboBox<MagDbItem> mgnifyComboBox;

	private MagDbItem[][] magDbItems;
	private JTable mgnifyTable;
	private JTable localTable;
	private DefaultTableModel mgnifyModel;
	private DefaultTableModel localModel;
	private JTextField localDbTextField;
	private JTextField localFuncTextField;
	private JTextField localTaxaTextField;
	
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	/**
	 * Create the panel.
	 */
	public MagDbConfigPanel(MagDbItem[][] magDbItems) {
		setLayout(new MigLayout("", "[grow]", "[grow]"));

		this.magDbItems = magDbItems;

		JPanel panel = new JPanel();
		panel.setBorder(null);
		add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]"));

		JPanel mgnifyPanel = new JPanel();
		mgnifyPanel.setBorder(
				new TitledBorder(null, "MGnify database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(mgnifyPanel, "cell 0 0 2 1,grow");
		mgnifyPanel.setLayout(new MigLayout("", "[300:500:810,grow][300:500:810,grow]", "[250:300:400,grow]"));

		JPanel mgnifyInfoPanel = new JPanel();
		mgnifyInfoPanel.setBorder(
				new TitledBorder(null, "MGnify database list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		mgnifyPanel.add(mgnifyInfoPanel, "cell 0 0,grow");
		mgnifyInfoPanel.setLayout(new MigLayout("", "[80][200:500:700,grow]", "[200:250:350,grow][50]"));

		JScrollPane mgnifyScrollPane = new JScrollPane();
		mgnifyInfoPanel.add(mgnifyScrollPane, "cell 0 0 2 1,grow");
		mgnifyTable = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6813826941243937368L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				case 3:
					return Integer.class;
				case 4:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		mgnifyScrollPane.setViewportView(mgnifyTable);
		Object[][] mgnifyObjs = new Object[magDbItems[0].length][];
		for (int i = 0; i < mgnifyObjs.length; i++) {
			mgnifyObjs[i] = magDbItems[0][i].getTableRow();
		}
		this.mgnifyModel = new DefaultTableModel(mgnifyObjs, MagDbItem.getColumnNames()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8289806264323250089L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};
		mgnifyTable.setModel(mgnifyModel);

		JProgressBar checkMGnifyBar = new JProgressBar();
		mgnifyInfoPanel.add(checkMGnifyBar, "cell 1 1,growx");

		JButton mgnifyButton = new JButton("Check MGnify");
		mgnifyInfoPanel.add(mgnifyButton, "cell 0 1");

		JPanel mgnifyDLPanel = new JPanel();
		mgnifyPanel.add(mgnifyDLPanel, "cell 1 0,grow");
		mgnifyDLPanel.setBorder(
				new TitledBorder(null, "Download MGnify database", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		mgnifyDLPanel.setLayout(new MigLayout("", "[80][100:150:200,grow][20:50:200,grow][80][100:150:200,grow]",
				"[30][30][30][30][200,grow]"));

		JLabel genomeLabel = new JLabel("Name");
		mgnifyDLPanel.add(genomeLabel, "cell 0 0,alignx left");
	
		mgnifyComboBox = new JComboBox<MagDbItem>();
		for (int i = 0; i < magDbItems[0].length; i++) {
			mgnifyComboBox.addItem(magDbItems[0][i]);
		}
		mgnifyDLPanel.add(mgnifyComboBox, "cell 1 0,growx");
		
		JLabel genomeCountLabel = new JLabel("Species count");
		mgnifyDLPanel.add(genomeCountLabel, "cell 3 0,alignx left");

		spCountTextField = new JTextField();
		mgnifyDLPanel.add(spCountTextField, "cell 4 0,growx,aligny center");
		spCountTextField.setColumns(10);
		spCountTextField.setEnabled(false);

		JLabel mgfniyLatestVerLabel = new JLabel("Latest version");
		mgnifyDLPanel.add(mgfniyLatestVerLabel, "cell 0 1,alignx left");

		latestVerTextField = new JTextField();
		mgnifyDLPanel.add(latestVerTextField, "cell 1 1,growx,aligny center");
		latestVerTextField.setColumns(10);
		latestVerTextField.setEnabled(false);

		JLabel localVerLabel = new JLabel("Local version");
		mgnifyDLPanel.add(localVerLabel, "cell 3 1,alignx left");

		mgnifyOldVerTextField = new JTextField();
		mgnifyDLPanel.add(mgnifyOldVerTextField, "cell 4 1,growx,aligny center");
		mgnifyOldVerTextField.setColumns(10);
		mgnifyOldVerTextField.setEnabled(false);

		JLabel mgnifyRepoLabel = new JLabel("Local repository");
		mgnifyDLPanel.add(mgnifyRepoLabel, "cell 0 2,alignx left");

		mgnifyRepoTextField = new JTextField();
		mgnifyDLPanel.add(mgnifyRepoTextField, "cell 1 2 3 1,growx");
		mgnifyRepoTextField.setColumns(10);
		
		JButton mgnifyLocalRepoButton = new JButton("Browse");
		mgnifyDLPanel.add(mgnifyLocalRepoButton, "cell 4 2");
		mgnifyLocalRepoButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int result = fileChooser.showOpenDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String fileName = selectFile.getName();

				MagDbItem dbItem = mgnifyComboBox.getItemAt(mgnifyComboBox.getSelectedIndex());
				if (dbItem != null) {
					if (!fileName.equals(dbItem.getCatalogueID())) {
						selectFile = new File(selectFile, dbItem.getCatalogueID());
					}
				}

				String selectedPath = selectFile.getAbsolutePath();
				mgnifyRepoTextField.setText(selectedPath);
			}
		});

		JButton downloadButton = new JButton("Download/Update");
		mgnifyDLPanel.add(downloadButton, "cell 0 3");

		JProgressBar mgnifyDLBar = new JProgressBar();
		mgnifyDLPanel.add(mgnifyDLBar, "cell 1 3 4 1,growx");

		JScrollPane mgnifyRunScrollPane = new JScrollPane();
		mgnifyDLPanel.add(mgnifyRunScrollPane, "cell 0 4 5 1,grow");

		if (magDbItems[0].length > 0) {
			int spCount = magDbItems[0][0].getSpeciesCount();
			spCountTextField.setText(String.valueOf(spCount));

			String catalogueVersion = magDbItems[0][0].getCatalogueVersion();
			latestVerTextField.setText(catalogueVersion);

			String oldVersion = magDbItems[0][0].getOldVersion();
			if (oldVersion != null && oldVersion.length() > 0) {
				mgnifyOldVerTextField.setText(oldVersion);
			}

			File localRepoFile = magDbItems[0][0].getRepositoryFile();
			if (localRepoFile != null && localRepoFile.exists()) {
				mgnifyRepoTextField.setText(localRepoFile.getAbsolutePath());
			}

			boolean updateAvailable = magDbItems[0][0].updateAvailable();
			if (updateAvailable) {
				downloadButton.setEnabled(true);
			} else {
				downloadButton.setEnabled(false);
			}
		}

		JPanel localPanel = new JPanel();
		localPanel.setBorder(
				new TitledBorder(null, "Local MAG database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(localPanel, "cell 0 1 2 1,grow");
		localPanel.setLayout(new MigLayout("", "[300:500:810,grow][300:500:810,grow]", "[250:300:500,grow]"));

		JPanel localInfoPanel = new JPanel();
		localInfoPanel.setBorder(
				new TitledBorder(null, "Local database list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		localPanel.add(localInfoPanel, "cell 0 0,grow");
		localInfoPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

		JScrollPane localScrollPane = new JScrollPane();
		localInfoPanel.add(localScrollPane, "cell 0 0,grow");
		localTable = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6813826941243937368L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				case 3:
					return Integer.class;
				case 4:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		localScrollPane.setViewportView(localTable);
		Object[][] localObjs = new Object[magDbItems[1].length][];
		for (int i = 0; i < localObjs.length; i++) {
			localObjs[i] = magDbItems[1][i].getTableRow();
		}

		this.localModel = new DefaultTableModel(localObjs, MagDbItem.getColumnNames()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8289806264323250089L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};
		localTable.setModel(localModel);

		JPanel localConfigPanel = new JPanel();
		localPanel.add(localConfigPanel, "cell 1 0,grow");
		localConfigPanel.setBorder(
				new TitledBorder(null, "Config local database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		localConfigPanel.setLayout(new MigLayout("", "[100:120:150,grow][100:150:200,grow][50:100px:150][100:150:200,grow][100:150:200,grow]", "[30][30][30][30][30][30][100px:200:300,grow]"));

		JLabel localNameLabel = new JLabel("Name");
		localConfigPanel.add(localNameLabel, "cell 0 0,alignx left");

		localNameTextField = new JTextField();
		localConfigPanel.add(localNameTextField, "cell 1 0,growx");
		localNameTextField.setColumns(10);

		JLabel identifierLabel = new JLabel("Identifier");
		localConfigPanel.add(identifierLabel, "cell 3 0,alignx left");

		identifierTextField = new JTextField();
		localConfigPanel.add(identifierTextField, "cell 4 0,growx");
		identifierTextField.setColumns(10);

		JLabel localRepoLabel = new JLabel("Local repository");
		localConfigPanel.add(localRepoLabel, "cell 0 1,alignx left");

		localRepoTextField = new JTextField();
		localConfigPanel.add(localRepoTextField, "cell 1 1 3 1,growx");
		localRepoTextField.setColumns(10);

		JButton localRepoButton = new JButton("Browse");
		localConfigPanel.add(localRepoButton, "cell 4 1");
		localRepoButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int result = fileChooser.showOpenDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				localRepoTextField.setText(selectedPath);

				File localDbFile = new File(selectFile, "original_db");
				localDbTextField.setText(localDbFile.getAbsolutePath());

				File localTaxaFile = new File(selectFile, "genomes-all_metadata.tsv");
				localTaxaTextField.setText(localTaxaFile.getAbsolutePath());

				File localFuncFile = new File(selectFile, "eggNOG");
				localFuncTextField.setText(localFuncFile.getAbsolutePath());
			}
		});

		JLabel localDbLabel = new JLabel("Genome database");
		localConfigPanel.add(localDbLabel, "cell 0 2,alignx left");

		localDbTextField = new JTextField();
		localDbTextField.setEditable(false);
		localConfigPanel.add(localDbTextField, "cell 1 2 4 1,growx");
		localDbTextField.setColumns(10);

		JLabel localTaxaLabel = new JLabel("Taxonomic database");
		localConfigPanel.add(localTaxaLabel, "cell 0 3,alignx left");

		localTaxaTextField = new JTextField();
		localTaxaTextField.setEditable(false);
		localConfigPanel.add(localTaxaTextField, "cell 1 3 4 1,growx");
		localTaxaTextField.setColumns(10);

		JLabel localFunLabel = new JLabel("Functional database");
		localConfigPanel.add(localFunLabel, "cell 0 4,alignx left");

		localFuncTextField = new JTextField();
		localFuncTextField.setEditable(false);
		localConfigPanel.add(localFuncTextField, "cell 1 4 4 1,growx");
		localFuncTextField.setColumns(10);

		JButton addButton = new JButton("Add");
		localConfigPanel.add(addButton, "cell 0 5,growx");

		JButton localDeleteButton = new JButton("Delete");
		localConfigPanel.add(localDeleteButton, "cell 1 5,growx");
		localDeleteButton.setEnabled(false);
		
		JProgressBar localBar = new JProgressBar();
		localConfigPanel.add(localBar, "cell 2 5 3 1,growx");

		JScrollPane localRunScrollPane = new JScrollPane();
		localConfigPanel.add(localRunScrollPane, "cell 0 6 5 1,grow");

		JTextArea localConsoleTextArea = new JTextArea();
		localConsoleTextArea.setWrapStyleWord(true);
		localConsoleTextArea.setLineWrap(true);

		localRunScrollPane.setViewportView(localConsoleTextArea);

		mgnifyButton.addActionListener(l -> {

			ConsoleTextArea mgnifyConsoleTextArea= null;
			try {
				mgnifyConsoleTextArea = new ConsoleTextArea();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mgnifyRunScrollPane.setViewportView(mgnifyConsoleTextArea);
			
			mgnifyButton.setEnabled(false);

			checkMGnifyBar.setValue(0);
			checkMGnifyBar.setIndeterminate(false);
			checkMGnifyBar.setString("Processing will take 2~3 minutes...");
			checkMGnifyBar.setStringPainted(true);

			MGYGFtpTask mgygFtp = new MGYGFtpTask(magDbItems[0], checkMGnifyBar) {
				@Override
				protected void done() {
					// This method is executed on the EDT after the task is done
					// Update GUI components here
					magDbItems[0] = getRepositoryItems();

					Object[][] newMgnifyObjs = new Object[magDbItems[0].length][];
					for (int i = 0; i < newMgnifyObjs.length; i++) {
						newMgnifyObjs[i] = magDbItems[0][i].getTableRow();
					}
					mgnifyModel = new DefaultTableModel(newMgnifyObjs, MagDbItem.getColumnNames()) {
						/**
						 * 
						 */
						private static final long serialVersionUID = -8289806264323250089L;

						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return false;
						}
					};
					mgnifyTable.setModel(mgnifyModel);

					mgnifyComboBox.removeAll();
					for (int i = 0; i < magDbItems[0].length; i++) {
						mgnifyComboBox.addItem(magDbItems[0][i]);
					}

					if (mgnifyComboBox.getItemCount() > 0) {
						mgnifyComboBox.setSelectedIndex(0);
					}

					checkMGnifyBar.setString("Finished");
					checkMGnifyBar.setValue(100);

					mgnifyButton.setEnabled(true);
				}
			};
			mgygFtp.execute();
		});

		downloadButton.addActionListener(l -> {
			MagDbItem magDbItem = mgnifyComboBox.getItemAt(mgnifyComboBox.getSelectedIndex());
			String path = mgnifyRepoTextField.getText();
			magDbItem.setRepositoryFile(new File(path));

			downloadButton.setEnabled(false);
			mgnifyDLBar.setValue(0);
			mgnifyDLBar.setIndeterminate(false);
			mgnifyDLBar.setStringPainted(true);

			MGYGFtpTask mgygFtp = new MGYGFtpTask(magDbItem, mgnifyDLBar) {
				@Override
				protected void done() {

					mgnifyDLBar.setString("Finished");
					mgnifyDLBar.setValue(100);
				}
			};
			mgygFtp.execute();

		});

		mgnifyComboBox.addItemListener(l -> {
			MagDbItem dbItem = mgnifyComboBox.getItemAt(mgnifyComboBox.getSelectedIndex());
			if (dbItem != null) {

				int spCount = dbItem.getSpeciesCount();
				spCountTextField.setText(String.valueOf(spCount));

				String catalogueVersion = dbItem.getCatalogueVersion();
				latestVerTextField.setText(catalogueVersion);

				String oldVersion = dbItem.getOldVersion();
				if (oldVersion != null && oldVersion.length() > 0) {
					mgnifyOldVerTextField.setText(oldVersion);
				}

				File localRepoFile = dbItem.getRepositoryFile();
				if (localRepoFile != null && localRepoFile.exists()) {
					mgnifyRepoTextField.setText(localRepoFile.getAbsolutePath());
				}

				boolean updateAvailable = dbItem.updateAvailable();
				if (updateAvailable) {
					downloadButton.setEnabled(true);
				} else {
					downloadButton.setEnabled(false);
				}
			}
		});

		addButton.addActionListener(l -> {

			addButton.setEnabled(false);
			localBar.setIndeterminate(true);
			localConsoleTextArea.append(format.format(new Date()) + "\tAnalyzing...\n");

			ArrayList<String> errorList = new ArrayList<String>();
			String localName = localNameTextField.getText();
			if (localName == null || localName.length() == 0) {
				errorList.add("Please enter a name of the MAG database.");
			}

			String identifier = identifierTextField.getText();
			if (identifier == null || identifier.length() == 0) {
				errorList.add("Please enter an identifier of the MAG database.");
			}

			String localRepo = localRepoTextField.getText();
			if (localRepo == null || localRepo.length() == 0) {
				errorList.add("Please set the repository of the MAG database.");
			} else {
				File localRepoFile = new File(localRepo);
				if (localRepoFile == null || !localRepoFile.exists()) {
					errorList.add("The repository " + localRepoFile + " doesn't exist.");
				}
			}

			int[] speciesCount = new int[1];
			String localProDb = localDbTextField.getText();
			if (localProDb == null || localProDb.length() == 0) {
				errorList.add("The protein sequence database folder is null.");
			} else {
				File localProDbFile = new File(localProDb);
				if (localProDbFile == null || !localProDbFile.exists()) {
					errorList.add("The protein sequence database folder " + localProDbFile + " doesn't exist.");
				} else {
					int errorCount = 0;
					File[] proDbFiles = localProDbFile.listFiles();
					speciesCount[0] = proDbFiles.length;

					for (int i = 0; i < proDbFiles.length; i++) {
						if (!proDbFiles[i].getName().startsWith(identifier)) {
							errorCount++;
						}
					}
					if (errorCount > 0) {
						errorList.add("The names of " + errorCount + " files in " + localProDbFile
								+ " doesn't start with the given identifier " + identifier + ".");
					}
				}
			}

			String localFunc = localFuncTextField.getText();
			if (localFunc == null || localFunc.length() == 0) {
				errorList.add("The functional annotation folder is null.");
			} else {
				File localFuncFile = new File(localFunc);
				if (localFuncFile == null || !localFuncFile.exists()) {
					errorList.add("The functional annotation folder " + localFuncFile + " doesn't exist.");
				} else {
					int errorCount = 0;
					File[] funcFiles = localFuncFile.listFiles();

					if (speciesCount[0] != funcFiles.length) {
						errorList.add("The number of files in " + localProDb
								+ " doesn't equal with the number of files in" + localFuncFile + ".");
					}

					for (int i = 0; i < funcFiles.length; i++) {
						if (!funcFiles[i].getName().startsWith(identifier)) {
							errorCount++;
						}
					}
					if (errorCount > 0) {
						errorList.add("The names of " + errorCount + " files in " + localFuncFile
								+ " doesn't start with the given identifier " + identifier + ".");
					}
				}
			}

			String localTaxa = localTaxaTextField.getText();
			if (localTaxa == null || localTaxa.length() == 0) {
				errorList.add("The taxonomic annotation file is null.");
			} else {
				File localTaxaFile = new File(localTaxa);
				if (localTaxaFile == null || !localTaxaFile.exists()) {
					errorList.add("The taxonomic annotation file " + localTaxaFile + " doesn't exist.");
				}
			}

			for (int i = 0; i < magDbItems[1].length; i++) {
				if (magDbItems[1][i].getCatalogueID().equals(localName)) {
					errorList.add("MAG database with name " + localName + " already exists.");
				}
			}
			
			if (errorList.size() > 0) {

				for (String message : errorList) {
					localConsoleTextArea.append(message + "\n");
				}

				JOptionPane.showMessageDialog(this, errorList.size() + " warnings exist, please check the settings.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				addButton.setEnabled(true);

			} else {

				localConsoleTextArea
						.append(format.format(new Date()) + "\tAdding " + localName + " to local MAG database...\n");

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						// Perform your time-consuming task here
						// Don't update GUI components in this method

						MagDbItem magDbItem = new MagDbItem(localName, identifier, new File(localRepo));
						magDbItem.addVersion("v1.0", speciesCount[0], new Date(), true);

						File hapFile = new File(new File(localRepo), "rib_elon.fasta");
						HapExtractor extractor = new HapExtractor(true);
						try {
							extractor.extract(new File(localProDb), hapFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							localConsoleTextArea
									.append(format.format(new Date()) + "\t" + e.getLocalizedMessage() + "\n");
						}

						ArrayList<MagDbItem> magDbItemList = new ArrayList<MagDbItem>();
						for (int i = 0; i < magDbItems[1].length; i++) {
							if (magDbItems[1][i].isAvailable()) {
								magDbItemList.add(magDbItems[1][i]);
							}
						}
						magDbItemList.add(magDbItem);

						magDbItems[1] = magDbItemList.toArray(new MagDbItem[magDbItemList.size()]);

						Object[][] newMgnifyObjs = new Object[magDbItems[1].length][];
						for (int i = 0; i < newMgnifyObjs.length; i++) {
							newMgnifyObjs[i] = magDbItems[1][i].getTableRow();
						}
						localModel = new DefaultTableModel(newMgnifyObjs, MagDbItem.getColumnNames()) {
							/**
							 * 
							 */
							private static final long serialVersionUID = -8289806264323250089L;

							public boolean isCellEditable(int rowIndex, int columnIndex) {
								return false;
							}
						};
						localTable.setModel(localModel);

						return null;
					}

					@Override
					protected void done() {
						// This method is executed on the EDT after the task is done
						// Update GUI components here
						localConsoleTextArea.append(format.format(new Date()) + "\tFinished.\n");
						localBar.setIndeterminate(false);
						addButton.setEnabled(true);
					}
				};
				
				worker.execute();
			}
		});

		ListSelectionModel selectionModel = localTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(l -> {
			if (!l.getValueIsAdjusting()) {
				int selectedRow = localTable.getSelectedRow();
				if (selectedRow != -1) {
					localDeleteButton.setEnabled(true);
					String magDbName = (String) localTable.getValueAt(selectedRow, 1);

					for (int i = 0; i < magDbItems[1].length; i++) {
						if (magDbItems[1][i].getCatalogueID().equals(magDbName)) {
							localNameTextField.setText(magDbName);
							identifierTextField.setText(magDbItems[1][i].getIdentifier());

							String repoPath = magDbItems[1][i].getRepositoryFile().getAbsolutePath();
							localRepoTextField.setText(repoPath);

							MagVersion magVer = magDbItems[1][i].getUsedVersion();
							localDbTextField.setText(repoPath + "\\" + magVer.getDbName());
							localTaxaTextField.setText(repoPath + "\\" + magVer.getTaxName());
							localFuncTextField.setText(repoPath + "\\" + magVer.getFuncName());
						}
					}
				}
			}
		});

		localDeleteButton.addActionListener(l -> {
			String magDbName = localNameTextField.getText();
			ArrayList<MagDbItem> magDbItemList = new ArrayList<MagDbItem>();
			for (int i = 0; i < magDbItems[1].length; i++) {
				if (magDbItems[1][i].isAvailable() && !magDbItems[1][i].getCatalogueID().equals(magDbName)) {
					magDbItemList.add(magDbItems[1][i]);
				}
			}

			magDbItems[1] = magDbItemList.toArray(new MagDbItem[magDbItemList.size()]);

			Object[][] newMgnifyObjs = new Object[magDbItems[1].length][];
			for (int i = 0; i < newMgnifyObjs.length; i++) {
				newMgnifyObjs[i] = magDbItems[1][i].getTableRow();
			}
			localModel = new DefaultTableModel(newMgnifyObjs, MagDbItem.getColumnNames()) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -8289806264323250089L;

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			};
			localTable.setModel(localModel);

			localConsoleTextArea
					.append(format.format(new Date()) + "\tMAG database " + magDbName + " was deleted.\n");
			localDeleteButton.setEnabled(false);
		});
	}

	public MagDbItem[][] getMagDbItems() {
		return magDbItems;
	}

	
}
