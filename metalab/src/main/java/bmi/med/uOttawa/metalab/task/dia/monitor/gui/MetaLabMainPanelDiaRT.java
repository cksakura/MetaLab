package bmi.med.uOttawa.metalab.task.dia.monitor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.io.FileUtils;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.dia.DiaLibCreateTask;
import bmi.med.uOttawa.metalab.task.dia.DiaPyTorchModelTask;
import bmi.med.uOttawa.metalab.task.dia.gui.MagDbModelPanel;
import bmi.med.uOttawa.metalab.task.dia.gui.MagDbPepLibPanel;
import bmi.med.uOttawa.metalab.task.dia.gui.MagDbStatPanel;
import bmi.med.uOttawa.metalab.task.dia.gui.MetaLabMainPanelDia;
import bmi.med.uOttawa.metalab.task.dia.monitor.MetaDiaCombineTask;
import bmi.med.uOttawa.metalab.task.dia.monitor.MetaDiaMonitorTask;
import bmi.med.uOttawa.metalab.task.dia.monitor.par.MetaParIODiaRT;
import bmi.med.uOttawa.metalab.task.dia.monitor.par.MetaParameterDiaRT;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagModel;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagPepLib;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMagDbPanel;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMainPanelMag;
import bmi.med.uOttawa.metalab.task.v1.gui.StatusPanel;
import net.miginfocom.swing.MigLayout;

public class MetaLabMainPanelDiaRT extends MetaLabMainPanelDia {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6690900527070030161L;
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private DeepDetectTask deepDetectTask;
	private DiaPyTorchModelTask diaModelTask;
	private DiaLibCreateTask diaLibCreateTask;

	private MagDbStatPanel magDbStatPanel;
	private MagDbPepLibPanel pepLibPanel;
	private MagDbModelPanel modelPanel;

	public MetaLabMainPanelDiaRT(MetaParameterDiaRT par, MetaSourcesDia msv) {
		super(par, msv);
		// TODO Auto-generated constructor stub
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		this.workflowType = MetaLabWorkflowType.DiaNNMAGRT;

		this.initialMagDbPanel();
		this.initialParPanel();
		this.initialRunPanel();

		tabbedPane.setSelectedIndex(1);
	}

	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(
				new MigLayout("", "[400:600:980,grow][400:600:980,grow]", "[40][300:350:400,grow][380:420:460,grow]"));

		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					update1();
				}
			}
		};

		dbPanel = new MetaLabMagDbPanel(par, false, itemListener);
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(dbPanel, "cell 0 0 2 1,grow");

		MetaParameterDiaRT diaPar = (MetaParameterDiaRT) par;
		MetaSourcesDia msd = (MetaSourcesDia) msv;
		MagDbItem magDbItem = diaPar.getUsedMagDbItem();
		String[] libs = diaPar.getLibrary();
		String[] usedModels = diaPar.getModels();

		magDbStatPanel = new MagDbStatPanel(diaPar, (MetaSourcesDia) msv);
		parameterPanel.add(magDbStatPanel, "cell 0 1,grow");
		JButton predictedButton = magDbStatPanel.getPredictButton();
		predictedButton.addActionListener(l -> {

			if (!msd.findDeepDetect()) {
				JOptionPane.showMessageDialog(this, "DeepDetect is not found, please check the Setting -> Resource.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			predictedButton.setEnabled(false);
			magDbStatPanel.getProgressBar().setValue(0);
			magDbStatPanel.getProgressBar().setStringPainted(true);

			File deepDetectFile = new File(msd.getDeepDetect());

			deepDetectTask = new DeepDetectTask(deepDetectFile, new DeepDetectParameter(), diaPar.getUsedMagDbItem(),
					magDbStatPanel.getProgressBar(), magDbStatPanel.getTimeTextField()) {
				public void done() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (deepDetectTask.isCancelled()) {
						magDbStatPanel.getProgressBar().setString("Task stopped");
						return;
					}

					boolean finish = false;

					try {

						finish = get();

						if (finish) {
							/*
							 * File repoFile = diaPar.getUsedMagDbItem().getRepositoryFile(); File
							 * currentFile = diaPar.getUsedMagDbItem().getCurrentFile(); File sqlDbFile =
							 * new File(currentFile, repoFile.getName() + ".db");
							 * 
							 * MagSqliteTask.create(sqlDbFile); try { MagSqliteTask.createTaxaTable(repoFile
							 * + "\\genomes-all_metadata.tsv", sqlDbFile);
							 * MagSqliteTask.createFuncTable(repoFile + "\\eggNOG", sqlDbFile);
							 * 
							 * } catch (NumberFormatException | IOException e) { // TODO Auto-generated
							 * catch block LOGGER.error("SQL database: error in creating the SQL database",
							 * e); System.err.println(format.format(new Date()) + "\t" +
							 * "SQL database: error in creating the SQL database"); }
							 */
							magDbStatPanel.update();
							magDbStatPanel.getProgressBar().setString("finished");

							setCursor(null);
							setEnabled(true);
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, "Task failed", "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			deepDetectTask.execute();
		});

		JButton updateButton = magDbStatPanel.getUpdateButton();
		updateButton.addActionListener(l -> {
			SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
				@Override
				protected Void doInBackground() throws Exception {

					updateButton.setEnabled(false);
					magDbStatPanel.getProgressBar().setValue(0);
					magDbStatPanel.getProgressBar().setStringPainted(true);

					MagDbItem magdb = diaPar.getUsedMagDbItem();
					File currentFile = magdb.getCurrentFile();
					if (currentFile == null || !currentFile.exists()) {
						publish("The local directory of the MAG database was not found.");
						return null;
					}

					File originalDbFile = new File(currentFile, "original_db");
					if (!originalDbFile.exists()) {
						publish("The protein sequence databases are not found from " + originalDbFile);
						return null;
					}

					int genomeCount = originalDbFile.listFiles().length;

					File eggnogFile = new File(currentFile, "eggNOG");
					if (!eggnogFile.exists()) {
						publish("The protein annotation files are not found from " + eggnogFile);
						return null;
					}

					int eggnogCount = eggnogFile.listFiles().length;
					if (genomeCount != eggnogCount) {
						publish("The number of the protein sequence database was not equal with the number of the annotation files, "
								+ "please try to download the database again.");
						return null;
					}

					magDbStatPanel.getProgressBar().setValue(30);

					File modelFile = new File(currentFile, "models");
					if (modelFile.exists()) {
						File[] modelFiles = modelFile.listFiles();
						for (File mf : modelFiles) {
							if (mf.isDirectory()) {
								magdb.addModelFromFile(mf);
							}
						}
					}

					magDbStatPanel.getProgressBar().setValue(70);

					File libFile = new File(currentFile, "libraries");
					if (libFile.exists()) {
						File[] libFiles = libFile.listFiles();
						for (File lf : libFiles) {
							if (lf.isDirectory()) {
								magdb.addPepLibFromFile(lf);
							}
						}
					}

					magDbStatPanel.getProgressBar().setValue(100);

					return null;
				}

				@Override
				protected void process(List<String> chunks) {
					// Update UI with intermediate progress messages
					for (String message : chunks) {
						JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
						setCursor(null);
					}
				}

				@Override
				protected void done() {
					JOptionPane.showMessageDialog(null, "Update completed", "Warning", JOptionPane.INFORMATION_MESSAGE);
					setCursor(null);

					modelPanel.update();
					pepLibPanel.update();

					updateButton.setEnabled(true);
				}
			};
			worker.execute();
		});

		pepLibPanel = new MagDbPepLibPanel(magDbItem, libs);
		parameterPanel.add(pepLibPanel, "cell 1 1,grow");
		JButton createLibButton = pepLibPanel.getCreatePepDbButton();
		createLibButton.addActionListener(l -> {

			File pepFile = new File(pepLibPanel.getFilePath());
			if (!pepFile.exists() || !pepFile.isFile() || pepFile.length() == 0) {
				JOptionPane.showMessageDialog(this, "Input file is not found from " + pepFile, "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			int taskType;
			String fileName = pepFile.getName();
			if (fileName.endsWith(".speclib")) {
				taskType = DiaLibCreateTask.fromLibrary;
			} else if (fileName.endsWith(".faa") || fileName.endsWith(".fasta")) {
				if (!msd.findDiaNN()) {
					JOptionPane.showMessageDialog(this, "DiaNN.exe is not found, please check the Setting -> Resource.",
							"Warning", JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}

				if (!msd.findDeepDetect()) {
					JOptionPane.showMessageDialog(this,
							"DeepDetect.exe is not found, please check the Setting -> Resource.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}

				taskType = DiaLibCreateTask.fromProtein;

			} else if (fileName.endsWith(".tsv") || fileName.endsWith(".csv")) {
				if (!msd.findDiaNN()) {
					JOptionPane.showMessageDialog(this, "DiaNN.exe is not found, please check the Setting -> Resource.",
							"Warning", JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}

				taskType = DiaLibCreateTask.fromPeptide;

			} else {
				JOptionPane.showMessageDialog(this, "The type of input file is unknown", "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			String libName = pepLibPanel.getPepLibName();
			if (libName.length() == 0) {
				JOptionPane.showMessageDialog(this, "the library name is not set", "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			MagPepLib[] magLibs = diaPar.getUsedMagDbItem().getMagPepLibs();
			for (MagPepLib magPepLib : magLibs) {
				if (magPepLib.getLibName().equals(libName)) {
					JOptionPane.showMessageDialog(this,
							"library named " + libName + " already exist, please use another name", "Warning",
							JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}
			}

			File currentFile = diaPar.getUsedMagDbItem().getCurrentFile();
			File libFile = new File(new File(currentFile, "libraries"), libName);
			if (libFile.exists()) {

				Object[] options = { "Yes, overwrite them.", "No, use existing files.", "Cancel" };

				int choice = JOptionPane.showOptionDialog(this,
						"Library files has been found from " + libName + ", overwrite it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				switch (choice) {
				case 0:
					FileUtils.deleteQuietly(libFile);
					break;
				case 1:
					break;
				case 2:
					setCursor(null);
					return;
				default:
					setCursor(null);
					return;
				}
			}

			if (!libFile.exists()) {
				libFile.mkdir();
			}

			createLibButton.setEnabled(false);
			pepLibPanel.getProgressBar().setIndeterminate(true);

			diaPar.setResult(libFile.getAbsolutePath());
			diaPar.setThreadCount(threadComboBox.getItemAt(threadComboBox.getSelectedIndex()));

			diaLibCreateTask = new DiaLibCreateTask(diaPar, msd, pepLibPanel.getProgressBar(),
					pepFile.getAbsolutePath(), pepLibPanel.getProtease(), pepLibPanel.getMiss(),
					pepLibPanel.getPepLibName(), taskType) {

				public void done() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					setCursor(null);
					createLibButton.setEnabled(true);
					pepLibPanel.getProgressBar().setIndeterminate(false);

					if (diaLibCreateTask.isCancelled()) {
						pepLibPanel.getProgressBar().setString("Task stopped");
						return;
					}

					boolean finish = false;

					try {

						finish = get();

						if (finish) {
							pepLibPanel.getProgressBar().setValue(100);
							pepLibPanel.update();
							pepLibPanel.getProgressBar().setString("finished");
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			diaLibCreateTask.execute();
		});

		modelPanel = new MagDbModelPanel(magDbItem, usedModels);
		parameterPanel.add(modelPanel, "cell 1 2,grow");
		JButton createModelButton = modelPanel.getCreateModelButton();
		createModelButton.addActionListener(l -> {

			if (!msd.findPython()) {
				JOptionPane.showMessageDialog(this, "python is not found, please check the Setting -> Resource.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			File pepFile = new File(modelPanel.getFilePath());
			if (!pepFile.exists() || !pepFile.isFile() || pepFile.length() == 0) {
				JOptionPane.showMessageDialog(this, "peptide quantification result is not found from " + pepFile,
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			String modelName = modelPanel.getModelName();
			if (modelName.length() == 0) {
				JOptionPane.showMessageDialog(this, "the model name is not set", "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			MagModel[] magModels = diaPar.getUsedMagDbItem().getMagModels();
			for (MagModel model : magModels) {
				if (model.getModelName().equals(modelName)) {
					JOptionPane.showMessageDialog(this,
							"model named " + modelName + " already exist, please use another name", "Warning",
							JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}
			}

			File modelFile = new File(new File(diaPar.getUsedMagDbItem().getCurrentFile(), "models"), modelName);
			if (modelFile.exists()) {

				Object[] options = { "Yes, overwrite them.", "No, use existing files.", "Cancel" };

				int choice = JOptionPane.showOptionDialog(this,
						"Model files has been found from " + modelFile + ", overwrite it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				switch (choice) {
				case 0:
					FileUtils.deleteQuietly(modelFile);
					break;
				case 1:
					break;
				case 2:
					setCursor(null);
					return;
				default:
					setCursor(null);
					return;
				}
			}

			if (!modelFile.exists()) {
				modelFile.mkdir();
			}

			createModelButton.setEnabled(false);
			modelPanel.getProgressBar().setIndeterminate(true);

			diaPar.setThreadCount(threadComboBox.getItemAt(threadComboBox.getSelectedIndex()));
			diaPar.setResult(modelFile.getAbsolutePath());

			diaModelTask = new DiaPyTorchModelTask((MetaParameterDia) par, msd, modelPanel.getProgressBar(),
					pepFile.getAbsolutePath(), modelFile) {
				public void done() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					setCursor(null);
					createModelButton.setEnabled(true);
					modelPanel.getProgressBar().setIndeterminate(false);

					if (diaModelTask.isCancelled()) {
						modelPanel.getProgressBar().setString("Task stopped");
						return;
					}

					boolean finish = false;

					try {

						finish = get();

						if (finish) {
							modelPanel.getProgressBar().setValue(100);
							modelPanel.update();
							modelPanel.getProgressBar().setString("finished");
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			diaModelTask.execute();
		});

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(scrollPaneConsole, "cell 0 2,grow");

		ConsoleTextArea consoleTextArea = null;
		try {
			consoleTextArea = new ConsoleTextArea();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollPaneConsole.setViewportView(consoleTextArea);
	}

	protected void initialRunPanel() {
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new MigLayout("", "[450:600:1120,grow][450:600:800,grow]", "[520:740:940,grow][60]"));
		tabbedPane.addTab("Run",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
				consolePanel, null);

		JPanel monitorJPanel = new JPanel();
		monitorJPanel.setLayout(new MigLayout("", "[450:600:1120,grow]", "[520:740:940,grow][60]"));

		JPanel combineJPanel = new JPanel();
		combineJPanel.setLayout(new MigLayout("", "[450:600:1120,grow]", "[520:740:940,grow][60]"));

		JTabbedPane runTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		runTabbedPane.addTab("Monitor",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
				monitorJPanel, null);
		runTabbedPane.addTab("Combine",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
				combineJPanel, null);

		consolePanel.add(runTabbedPane, "cell 0 0,grow");

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		consolePanel.add(scrollPaneConsole, "cell 1 0,grow");

		this.statusPanel = new StatusPanel("Version: " + MetaParIODiaRT.version);
		consolePanel.add(statusPanel, "cell 0 1 2 1,grow");

		MetaParameterDiaRT diaRTPar = (MetaParameterDiaRT) par;
		JPanel monitorIOPanel = new JPanel();
		monitorJPanel.add(monitorIOPanel, "cell 0 0,grow");
		monitorIOPanel.setLayout(
				new MigLayout("", "[100][300:680:920,grow][50]", "[30][30][240:300:400,grow][240:300:400,grow][120]"));

		JLabel lblMonFolder = new JLabel("Monitor folder");
		monitorIOPanel.add(lblMonFolder, "cell 0 0,alignx left");

		JTextField textFieldMonFolder = new JTextField();
		monitorIOPanel.add(textFieldMonFolder, "flowx,cell 1 0,growx");
		textFieldMonFolder.setColumns(10);

		JButton btnMonFolder = new JButton("Browse");
		monitorIOPanel.add(btnMonFolder, "cell 2 0");
		btnMonFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldMonFolder.setText(selectedPath);
			}
		});

		JLabel lblResultFolder = new JLabel("Result folder");
		monitorIOPanel.add(lblResultFolder, "cell 0 1,alignx left");

		JTextField textFieldResFolder = new JTextField();
		monitorIOPanel.add(textFieldResFolder, "cell 1 1,growx");
		textFieldResFolder.setColumns(10);

		JButton btnResFolder = new JButton("Browse");
		monitorIOPanel.add(btnResFolder, "cell 2 1");
		btnResFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldResFolder.setText(selectedPath);
			}
		});

		JPanel monitorFolderPanel = new JPanel(new BorderLayout());
		monitorFolderPanel.setBorder(
				new TitledBorder(null, "Monitor folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JScrollPane scrollPaneMonitor = new JScrollPane();
		scrollPaneMonitor.setToolTipText("Monitor folder");
		monitorFolderPanel.add(scrollPaneMonitor);
		monitorIOPanel.add(monitorFolderPanel, "cell 0 2 3 1,grow");

		JList<String> monitorList = new JList<String>();
		scrollPaneMonitor.setViewportView(monitorList);
		DefaultListModel<String> rawListModel = new DefaultListModel<String>();
		monitorList.setModel(rawListModel);

		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel
				.setBorder(new TitledBorder(null, "Result folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JScrollPane scrollPaneResult = new JScrollPane();
		resultPanel.add(scrollPaneResult);
		monitorIOPanel.add(resultPanel, "cell 0 3 3 1,grow");

		JTable resultTable = new JTable();
		Object[] columnName = new Object[] { "File", "Round 1", "Model", "Round 2", "Round 3", "Round 4" };
		DefaultTableModel resultTableModel = new DefaultTableModel(columnName, 0);
		resultTable.setModel(resultTableModel);
		scrollPaneResult.setViewportView(resultTable);

		JPanel runPanel = new JPanel();
		runPanel.setBorder(new TitledBorder(null, "Control panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		monitorIOPanel.add(runPanel, "cell 0 4 3 1,grow");
		runPanel.setLayout(new MigLayout("", "[100][300:680:920,grow][50]", "[30][30][30][30]"));

		JButton btnStart = new JButton("Start monitor");
		JButton btnEnd = new JButton("End monitor");
		JButton btnEnd2 = new JButton("End monitor after the total number of processed files reached:");
		JButton btnReset = new JButton("Reset");
		JComboBox<Integer> comboBoxFileCount = new JComboBox<Integer>();

		JProgressBar progressBar = new JProgressBar();
		runPanel.add(progressBar, "cell 0 2 3 1,growx");

		JProgressBar progressBar2 = new JProgressBar();
		runPanel.add(progressBar2, "cell 0 3 3 1,growx");

		int processorCount = ProcessorCount();
		diaRTPar.setThreadCount(processorCount);

		runPanel.add(btnStart, "cell 0 0,growx");
		btnStart.addActionListener(l -> {

			LicenseVerifier verifier = new LicenseVerifier();
			boolean verified = verifier.verify();
			if (!verified) {

				JOptionPane.showMessageDialog(this,
						"Thank you for using MetaLab. A license is needed to run MetaLab, "
								+ "please go to Tools -> Activation for the license.",
						"Warning", JOptionPane.WARNING_MESSAGE);

				setCursor(null);
				return;
			}

			try {
				consoleTextArea = new ConsoleTextArea();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scrollPaneConsole.setViewportView(consoleTextArea);

			System.out.println("------Welcome to MetaLab " + MetaParIODiaRT.version + "------");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();
			progressBar2.setIndeterminate(true);

			btnStart.setEnabled(false);
			btnEnd.setEnabled(true);
			btnEnd2.setEnabled(true);
			btnReset.setEnabled(true);

			File monitorFolder = new File(textFieldMonFolder.getText());
			File destinationFolder = new File(textFieldResFolder.getText());

			diaRTPar.setMonitorFolder(monitorFolder);
			diaRTPar.setResultFolder(destinationFolder);
			diaRTPar.setResult(destinationFolder.getAbsolutePath());

			rawListModel.clear();
			resultTableModel.setRowCount(0);

			MetaDiaMonitorTask task = new MetaDiaMonitorTask(diaRTPar, (MetaSourcesDia) msv, progressBar, null, null,
					monitorFolder, destinationFolder, resultTableModel, rawListModel) {
				protected void done() {
					try {
						if (!isCancelled()) {
							get(); // Trigger any exceptions thrown in doInBackground
							// Update GUI with completion status
							System.out.println(
									"Monitoring completed. Total number of files processed: " + rawListModel.size());

							btnStart.setEnabled(true);
							btnEnd.setEnabled(false);
							btnEnd2.setEnabled(false);
							btnReset.setEnabled(false);
							progressBar2.setIndeterminate(false);
							setCursor(null);

							int rawFileCount = resultTableModel.getRowCount();

							int unfinished = 0;
							for (int i = 0; i < rawFileCount; i++) {
								for (int j = 1; j < resultTableModel.getColumnCount(); j++) {
									String value = (String) resultTableModel.getValueAt(i, j);
									if (!value.equals("Done")) {
										unfinished++;
										break;
									}
								}
							}

							if (unfinished == 0) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this,
										"Task finished, the total number of processed files is " + rawFileCount,
										"Information", JOptionPane.INFORMATION_MESSAGE);
							} else {

								Object[] options = { "Yes", "No" };

								int choice = JOptionPane.showOptionDialog(MetaLabMainPanelDiaRT.this,
										"The monitoring finished with " + rawFileCount + " raw files detected, but "
												+ unfinished
												+ " raw files were not processed completely, do you want to continue to processing these files?",
										"Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
										options, options[0]);

								switch (choice) {
								case 0:
									btnStart.doClick();
									break;
								case 1:
									break;
								default:
									return;
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			try {

				statusPanel.start();
				task.execute();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this,
						"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		JCheckBox chckbxCombine = new JCheckBox(
				"Perform taxonomic and functional analysis after monitoring is completed");
		runPanel.add(chckbxCombine, "cell 1 0");
		chckbxCombine.setSelected(diaRTPar.isCombineResult());
		chckbxCombine.addActionListener(l -> {
			diaRTPar.setCombineResult(chckbxCombine.isSelected());
		});

		runPanel.add(btnEnd, "cell 0 1,growx");
		btnEnd.setEnabled(false);
		btnEnd.addActionListener(l -> {
			btnEnd.setEnabled(false);
			SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub
					diaRTPar.setStopNow(true);
					diaRTPar.setStopComplete(false);

					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
					String formattedDateTime = now.format(formatter);
					String fileName = "file_" + formattedDateTime + ".txt";

					try (PrintWriter writer = new PrintWriter(new File(diaRTPar.getMonitorFolder(), fileName))) {
						writer.println("The monitor will end after all current files were processed.");
						writer.close();
					} catch (IOException e) {
						System.out.println("An error occurred.");
						e.printStackTrace();
					}
					return null;
				}
			};

			worker.execute();
			try {
				worker.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		runPanel.add(btnEnd2, "flowx,cell 1 1");
		btnEnd2.setEnabled(false);
		btnEnd2.addActionListener(l -> {
			btnEnd2.setEnabled(false);

			SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub

					diaRTPar.setStopNow(false);
					diaRTPar.setStopComplete(true);
					int taskCount = comboBoxFileCount.getItemAt(comboBoxFileCount.getSelectedIndex());
					diaRTPar.setTaskCount(taskCount);

					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
					String formattedDateTime = now.format(formatter);
					String fileName = "file_" + formattedDateTime + ".txt";

					try (PrintWriter writer = new PrintWriter(new File(diaRTPar.getMonitorFolder(), fileName))) {
						writer.println("The monitor will end after " + taskCount + " files were processed.");
						writer.close();
					} catch (IOException e) {
						System.out.println("An error occurred.");
						e.printStackTrace();
					}
					return null;
				}
			};

			worker.execute();

			try {
				worker.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		runPanel.add(comboBoxFileCount, "cell 1 1");
		for (int i = 1; i < 1000; i++) {
			comboBoxFileCount.addItem(i);
		}

		runPanel.add(btnReset, "cell 1 1");
		btnReset.setEnabled(false);

		btnReset.addActionListener(l -> {
			btnEnd.setEnabled(true);
			btnEnd2.setEnabled(true);
			comboBoxFileCount.setSelectedIndex(0);
			diaRTPar.setStopNow(false);
			diaRTPar.setStopComplete(false);
		});

		JProgressBar comProgressBar = new JProgressBar();
		JProgressBar comProgressBar2 = new JProgressBar();

		// combine panel
		JPanel rawIOPanel = new JPanel();
		combineJPanel.add(rawIOPanel, "cell 0 0,grow");
		rawIOPanel.setLayout(new MigLayout("", "[100][300:680:920,grow][50]",
				"[30][30][30][240:300:400,grow][240:300:400,grow][120]"));

		JLabel lblRawFolder = new JLabel("Raw file folder");
		rawIOPanel.add(lblRawFolder, "cell 0 0,alignx left");

		JTextField textFieldRawFolder = new JTextField();
		rawIOPanel.add(textFieldRawFolder, "flowx,cell 1 0,growx");
		textFieldRawFolder.setColumns(10);

		JButton btnRawFolder = new JButton("Browse");
		rawIOPanel.add(btnRawFolder, "cell 2 0");
		btnRawFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldRawFolder.setText(selectedPath);
			}
		});

		JLabel lblCombineResultFolder = new JLabel("Result folder");
		rawIOPanel.add(lblCombineResultFolder, "cell 0 1,alignx left");

		JTextField textFieldCombResFolder = new JTextField();
		rawIOPanel.add(textFieldCombResFolder, "cell 1 1,growx");
		textFieldCombResFolder.setColumns(10);

		JButton btnCombResFolder = new JButton("Browse");
		rawIOPanel.add(btnCombResFolder, "cell 2 1");
		btnCombResFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldCombResFolder.setText(selectedPath);
			}
		});

		JLabel lblOutputFolder = new JLabel("Output folder");
		rawIOPanel.add(lblOutputFolder, "cell 0 2,alignx left");

		JTextField textFieldOutputFolder = new JTextField();
		rawIOPanel.add(textFieldOutputFolder, "cell 1 2,growx");
		textFieldOutputFolder.setColumns(10);

		JButton btnOutputFolder = new JButton("Browse");
		rawIOPanel.add(btnOutputFolder, "cell 2 2");
		btnOutputFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldOutputFolder.setText(selectedPath);
			}
		});

		JPanel combineRawFolderPanel = new JPanel(new BorderLayout());
		combineRawFolderPanel.setBorder(
				new TitledBorder(null, "Raw file folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JScrollPane scrollPaneCombineRaw = new JScrollPane();
		scrollPaneCombineRaw.setToolTipText("Raw file folder");
		combineRawFolderPanel.add(scrollPaneCombineRaw);
		rawIOPanel.add(combineRawFolderPanel, "cell 0 3 3 1,grow");

		Object[] combineColumnName = new Object[] { "Select all", "File name", "File path" };
		DefaultTableModel combineRawTableModel = new DefaultTableModel(combineColumnName, 0);
		final JTable combineRawTable = new JTable(combineRawTableModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -738077452527789490L;

			@Override
			public Class<?> getColumnClass(int column) {
				if (column == 0) {
					return Boolean.class;
				}
				return String.class;
			}

			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}
		};

		combineRawTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxCellRenderer());
		combineRawTable.getColumnModel().getColumn(0).setMaxWidth(90);
		combineRawTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		combineRawTable.getColumnModel().getColumn(2).setPreferredWidth(480);
		combineRawTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = combineRawTable.columnAtPoint(e.getPoint());
				if (column == 0) {

					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					comProgressBar2.setIndeterminate(true);
					SwingWorker<Object, Object> task = new SwingWorker<Object, Object>() {
						protected void done() {
							try {
								if (!isCancelled()) {
									get();
									comProgressBar2.setIndeterminate(false);
									setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								}
								repaint();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						protected Object doInBackground() throws Exception {
							// TODO Auto-generated method stub

							TableColumn selectColumn = combineRawTable.getColumnModel().getColumn(0);
							String headerValue = (String) selectColumn.getHeaderValue();
							if (headerValue.equals("Select all")) {
								selectColumn.setHeaderValue("Deselect all");
								for (int row = 0; row < combineRawTableModel.getRowCount(); row++) {
									combineRawTableModel.setValueAt(true, row, 0);
									comProgressBar.setValue(
											(int) ((double) row / (double) combineRawTableModel.getRowCount() * 100.0));
								}
							} else {
								selectColumn.setHeaderValue("Select all");
								for (int row = 0; row < combineRawTableModel.getRowCount(); row++) {
									combineRawTableModel.setValueAt(false, row, 0);
									comProgressBar.setValue(
											(int) ((double) row / (double) combineRawTableModel.getRowCount() * 100.0));
								}
							}

							return null;
						}
					};

					try {
						task.execute();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this,
								"Task failed :(\nplease contact us to get a solution", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		scrollPaneCombineRaw.setViewportView(combineRawTable);

		JPanel combineResultPanel = new JPanel(new BorderLayout());
		combineResultPanel
				.setBorder(new TitledBorder(null, "Result folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JScrollPane scrollPaneCombineResult = new JScrollPane();
		combineResultPanel.add(scrollPaneCombineResult);
		rawIOPanel.add(combineResultPanel, "cell 0 4 3 1,grow");

		JTable combineResultTable = new JTable();
		DefaultTableModel combineResultTableModel = new DefaultTableModel(new Object[] { "File name", "File path" }, 0);
		combineResultTable.setModel(combineResultTableModel);
		scrollPaneCombineResult.setViewportView(combineResultTable);
		combineResultTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		combineResultTable.getColumnModel().getColumn(1).setPreferredWidth(480);

		combineRawTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				if (column == 0) {
					Boolean isChecked = (Boolean) combineRawTableModel.getValueAt(row, column);
					String fileName = (String) combineRawTableModel.getValueAt(row, 1);
					String resultFolder = textFieldCombResFolder.getText();
					if (isChecked) {
						File resultFile = findResultFile(fileName, new File(resultFolder));
						if (resultFile == null) {
							System.out.println(format.format(new Date()) + "\t" + "The result folder for " + fileName
									+ " was not found from " + resultFolder + ", or the search of " + fileName
									+ " was not completed. Please go to the Monitor panel to finish the processing of the raw files.");
						} else {
							combineResultTableModel.addRow(new Object[] { fileName, resultFile.getAbsolutePath() });
						}
					} else {
						for (int i = 0; i < combineResultTableModel.getRowCount(); i++) {
							if (((String) combineResultTableModel.getValueAt(i, 0)).equals(fileName)) {
								combineResultTableModel.removeRow(i);
								break;
							}
						}
					}
				}
			}
		});

		JPanel combineRunPanel = new JPanel();
		combineRunPanel
				.setBorder(new TitledBorder(null, "Control panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		rawIOPanel.add(combineRunPanel, "cell 0 5 3 1,grow");
		combineRunPanel.setLayout(new MigLayout("", "[100][300:680:920,grow][50]", "[30][30][30][30]"));

		JButton btnCheck = new JButton("Check folder");
		JButton btnCombine = new JButton("Combine the result");

		combineRunPanel.add(comProgressBar, "cell 0 2 3 1,growx");
		combineRunPanel.add(comProgressBar2, "cell 0 3 3 1,growx");

		diaRTPar.setThreadCount(processorCount);

		combineRunPanel.add(btnCheck, "cell 0 0,growx");
		btnCheck.addActionListener(l -> {

			LicenseVerifier verifier = new LicenseVerifier();
			boolean verified = verifier.verify();
			if (!verified) {

				JOptionPane.showMessageDialog(this,
						"Thank you for using MetaLab. A license is needed to run MetaLab, "
								+ "please go to Tools -> Activation for the license.",
						"Warning", JOptionPane.WARNING_MESSAGE);

				setCursor(null);
				return;
			}

			try {
				consoleTextArea = new ConsoleTextArea();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scrollPaneConsole.setViewportView(consoleTextArea);

			System.out.println("------Welcome to MetaLab " + MetaParIODiaRT.version + "------");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			comProgressBar2.setIndeterminate(true);
			btnCheck.setEnabled(false);

			SwingWorker<Object, Object> task = new SwingWorker<Object, Object>() {
				protected void done() {
					try {
						if (!isCancelled()) {
							get();

							System.out.println(format.format(new Date()) + "\t" + "Checking completed. "
									+ combineRawTableModel.getRowCount() + " raw files have been found from "
									+ textFieldRawFolder.getText());

							comProgressBar2.setIndeterminate(false);
							btnCheck.setEnabled(true);
							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub

					File checkFile = new File(textFieldRawFolder.getText());
					HashMap<String, String> rawFileMap = new HashMap<String, String>();
					collectRawFiles(checkFile, rawFileMap);

					for (String file : rawFileMap.keySet()) {
						combineRawTableModel.addRow(new Object[] { false, file, rawFileMap.get(file) });
					}

					return null;
				}

				void collectRawFiles(File directory, HashMap<String, String> rawFileMap) {
					File[] files = directory.listFiles();
					if (files != null) {
						for (File file : files) {
							if (file.isDirectory()) {
								collectRawFiles(file, rawFileMap);
							} else if (file.isFile()) {
								String fileName = file.getName();
								if (fileName.endsWith(".raw") || fileName.endsWith(".RAW")) {
									fileName = fileName.substring(0, fileName.length() - 4);
									if (!rawFileMap.containsKey(fileName)) {
										rawFileMap.put(fileName, file.getAbsolutePath());
									}
								} else if (fileName.endsWith(".d") || fileName.endsWith(".D")) {
									fileName = fileName.substring(0, fileName.length() - 2);
									if (!rawFileMap.containsKey(fileName)) {
										rawFileMap.put(fileName, file.getAbsolutePath());
									}
								} else if (fileName.endsWith(".dia") || fileName.endsWith(".DIA")) {
									if (fileName.endsWith(".raw.dia") || fileName.endsWith(".RAW.DIA")) {
										fileName = fileName.substring(0, fileName.length() - 8);
									} else if (fileName.endsWith(".d.dia") || fileName.endsWith(".D.DIA")) {
										fileName = fileName.substring(0, fileName.length() - 6);
									} else {
										fileName = fileName.substring(0, fileName.length() - 4);
									}
									rawFileMap.put(fileName, file.getAbsolutePath());
								}
							}
						}
					}
				}
			};

			try {
				task.execute();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this,
						"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		combineRunPanel.add(btnCombine, "cell 1 0,growx");
		btnCombine.addActionListener(l -> {

			LicenseVerifier verifier = new LicenseVerifier();
			boolean verified = verifier.verify();
			if (!verified) {

				JOptionPane.showMessageDialog(this,
						"Thank you for using MetaLab. A license is needed to run MetaLab, "
								+ "please go to Tools -> Activation for the license.",
						"Warning", JOptionPane.WARNING_MESSAGE);

				setCursor(null);
				return;
			}

			try {
				consoleTextArea = new ConsoleTextArea();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scrollPaneConsole.setViewportView(consoleTextArea);

			System.out.println("------Welcome to MetaLab " + MetaParIODiaRT.version + "------");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();
			comProgressBar2.setIndeterminate(true);

			btnCombine.setEnabled(false);

			File resultFolder = new File(textFieldOutputFolder.getText());
			diaRTPar.setResultFolder(resultFolder);
			diaRTPar.setResult(resultFolder.getAbsolutePath());

			HashMap<String, String[]> rawResultFileMap = new HashMap<String, String[]>();
			for (int i = 0; i < combineResultTableModel.getRowCount(); i++) {
				String fileName = (String) combineResultTableModel.getValueAt(i, 0);
				String resultFolderI = (String) combineResultTableModel.getValueAt(i, 1);
				for (int j = 0; j < combineRawTableModel.getRowCount(); j++) {
					if (fileName.equals((String) combineRawTableModel.getValueAt(j, 1))) {
						String rawFileI = (String) combineRawTableModel.getValueAt(j, 2);
						rawResultFileMap.put(fileName, new String[] { rawFileI, resultFolderI });
						break;
					}
				}
			}

			if (rawResultFileMap.size() == 0) {
				JOptionPane.showMessageDialog(this,
						"No raw files were selected. Please select the raw files for the processing first.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				btnCombine.setEnabled(true);
				comProgressBar2.setIndeterminate(false);
				setCursor(null);

			} else {

				MetaDiaCombineTask task = new MetaDiaCombineTask(diaRTPar, (MetaSourcesDia) msv, comProgressBar,
						comProgressBar2, null, rawResultFileMap) {
					protected void done() {
						try {
							if (!isCancelled()) {
								boolean finish = get();

								btnCombine.setEnabled(true);
								comProgressBar2.setIndeterminate(false);
								setCursor(null);

								String log = consoleTextArea.getText();
								try {
									PrintWriter logwriter = new PrintWriter(new File(par.getResult(), "log.txt"));
									logwriter.print(log);
									logwriter.close();

								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								if (finish) {
									comProgressBar.setString("finished");

									int reply = JOptionPane.showConfirmDialog(MetaLabMainPanelDiaRT.this,
											"Task finished :)\nView the report?", "Finished",
											JOptionPane.YES_NO_OPTION);

									if (reply == JOptionPane.YES_OPTION) {
										try {
											File report = new File(par.getReportDir(), "index.html");
											java.awt.Desktop.getDesktop().browse(report.toURI());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								} else {
									JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this, helpInfo, "Error",
											JOptionPane.ERROR_MESSAGE);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				try {

					statusPanel.start();
					task.execute();

				} catch (Exception e) {

					JOptionPane.showMessageDialog(MetaLabMainPanelDiaRT.this,
							"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private File findResultFile(String fileName, File resultDir) {
		File[] listFiles = resultDir.listFiles();
		for (File file : listFiles) {
			if (file.isDirectory()) {
				if (file.getName().equals(fileName)) {

					File firstFile = new File(new File(file, "firstSearch"), "firstSearch.tsv");
					File secondFile = new File(new File(file, "secondSearch"), "secondSearch.tsv");
					File thirdFile = new File(new File(file, "thirdSearch"), "thirdSearch.tsv");
					File lastFile = new File(file, "lastSearch.tsv");

					if (firstFile.exists() && firstFile.length() > 0 && secondFile.exists() && secondFile.length() > 0
							&& thirdFile.exists() && thirdFile.length() > 0 && lastFile.exists()
							&& lastFile.length() > 0) {
						return file;
					}
				} else {
					File subFile = findResultFile(fileName, file);
					if (subFile != null) {
						return subFile;
					}
				}
			}
		}
		return null;
	}

	// Custom cell renderer to display checkboxes in the first column
	private class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3409337273879152519L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setSelected((Boolean) value);
			return this;
		}
	}

	public void updateParameter() {
		int selectedIndex = tabbedPane.getSelectedIndex();
		switch (selectedIndex) {
		case 1:
			update1();
			break;
		case 2:
			update2();
			break;
		default:
			break;
		}
	}

	protected void update1() {
		String microdb = dbPanel.getMicroDb();
		par.setMicroDb(microdb);
		magDbStatPanel.update();

		MetaParameterDia diaPar = (MetaParameterDia) par;
		MagDbItem magDbItem = diaPar.getUsedMagDbItem();

		if (magDbItem != null) {
			this.pepLibPanel.update(magDbItem);
			this.modelPanel.update(magDbItem);
		}
	}

	protected void update2() {

		MetaParameterDia diaPar = (MetaParameterDia) par;

		String[] usedLibs = this.pepLibPanel.getUsedLibs();
		String[] usedModels = this.modelPanel.getUsedModels();

		diaPar.setLibrary(usedLibs);
		diaPar.setModels(usedModels);
	}

	protected String[] checkParameter() {
		ArrayList<String> list = new ArrayList<String>();

		MetaParameterDia diaPar = (MetaParameterDia) par;

		if (!((MetaSourcesDia) msv).findDiaNN()) {
			list.add("DIA-NN is not found, please check the Setting -> Resource.");
		}

		if (diaPar.getMetadata() == null || diaPar.getMetadata().getRawFiles() == null
				|| diaPar.getMetadata().getRawFiles().length == 0) {
			list.add("Raw files are not found.");
		} else {
			String[] rawfiles = diaPar.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}
		}

		File microDbFile = new File(diaPar.getMicroDb());
		if (!microDbFile.exists()) {
			list.add("Microbiome database " + microDbFile + " is not found.");
		} else {

		}

		if (diaPar.getResult() == null || diaPar.getResult().length() == 0) {
			list.add("Result file is not found.");
		}

		String[] usedLibs = diaPar.getLibrary();
		if (usedLibs.length == 0) {
			list.add("Peptide libraries are not set.");
		}

		String[] warnings = list.toArray(new String[list.size()]);

		return warnings;
	}
}
