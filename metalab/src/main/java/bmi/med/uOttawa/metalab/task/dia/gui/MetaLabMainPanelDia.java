package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.dia.DiaLibCreateTask;
import bmi.med.uOttawa.metalab.task.dia.DiaModelTask;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParaIoDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagModel;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagPepLib;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMagDbPanel;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMainPanelMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.pfind.MetaLabMagTask;
import bmi.med.uOttawa.metalab.task.v1.gui.StatusPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIOPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMetaPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabParViewPanel;
import net.miginfocom.swing.MigLayout;

public class MetaLabMainPanelDia extends MetaLabMainPanelMag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6690900527070030161L;

	private JCheckBox libSearchCheckBox;
	private JCheckBox iterSearchCheckBox;
	private JCheckBox largeScaleSearchCheckBox;
	
	private DeepDetectTask deepDetectTask;
	private DiaModelTask diaModelTask;
	private DiaLibCreateTask diaLibCreateTask;

	private MagDbStatPanel magDbStatPanel;
	private MagDbPepLibPanel pepLibPanel;
	private MagDbModelPanel modelPanel;
	
	private static Logger LOGGER = LogManager.getLogger(MetaLabMainPanelDia.class);
	private static SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
	
	public MetaLabMainPanelDia(MetaParameterDia par, MetaSourcesDia msv) {
		super(par, msv);
		// TODO Auto-generated constructor stub
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		this.workflowType = MetaLabWorkflowType.DiaNNMAG;

		this.initialMagDbPanel();
		this.initialIOPanel();
		this.initialParPanel();
		this.initialRunPanel();

		tabbedPane.setSelectedIndex(1);
		tabbedPane.setEnabledAt(3, false);
		previousTabIndex = tabbedPane.getSelectedIndex();

		tabbedPane.addChangeListener(l -> {

			int selectedIndex = tabbedPane.getSelectedIndex();
			switch (selectedIndex) {
			case 0:
				tabbedPane.setEnabledAt(2, false);
				tabbedPane.setEnabledAt(3, false);
				break;
			case 1:
				tabbedPane.setEnabledAt(2, true);
				tabbedPane.setEnabledAt(3, false);
				if (previousTabIndex == 0) {
					update0();
				}
				break;
			case 2:
				tabbedPane.setEnabledAt(3, true);
				if (previousTabIndex == 1) {
					update1();
				}
				break;
			case 3:
				update2();
				break;
			default:
				break;
			}

			previousTabIndex = selectedIndex;
		});
	}

	protected void initialIOPanel() {
		JPanel ioPanel = new JPanel();
		tabbedPane.addTab("Input data",
				new ImageIcon(MetaLabMainPanelDia.class.getResource("/toolbarButtonGraphics/general/Add16.gif")),
				ioPanel, null);
		tabbedPane.setEnabledAt(1, true);
		ioPanel.setLayout(new MigLayout("", "[800:1600:1920,grow]", "[450:750:930,grow][120]"));

		inputOutputPanel = new MetaLabIOPanel(par);
		ioPanel.add(inputOutputPanel, "cell 0 0,grow");

		dbPanel = new MetaLabMagDbPanel(par, false);
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ioPanel.add(dbPanel, "cell 0 1,grow");
	}
	
	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabMainPanelDia.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(new MigLayout("", "[400:500:560,grow][200:300:400,grow][560:760:1000,grow]",
				"[40][240:270:300,grow][240:270:300,grow][200:230:260,grow]"));

		JPanel strategyPanel = new JPanel();
		strategyPanel.setBorder(new TitledBorder(null, "DIA data analysis strategy", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		parameterPanel.add(strategyPanel, "cell 0 0,grow");
		strategyPanel.setLayout(new MigLayout("", "[120:180:240,grow][120:180:240,grow][120:180:240,grow]", "[80]"));

		MetaParameterDia diaPar = (MetaParameterDia) par;
		MetaSourcesDia msd = (MetaSourcesDia) msv;
		MagDbItem magDbItem = diaPar.getUsedMagDbItem();
		String[] libs = diaPar.getLibrary();
		String[] usedModels = diaPar.getModels();
		
		libSearchCheckBox = new JCheckBox("Library search");
		strategyPanel.add(libSearchCheckBox, "cell 0 0,grow");

		iterSearchCheckBox = new JCheckBox("Iterative search");
		strategyPanel.add(iterSearchCheckBox, "cell 1 0,grow");
		
		largeScaleSearchCheckBox= new JCheckBox("Self-model");
		strategyPanel.add(largeScaleSearchCheckBox, "cell 2 0,grow");

		ButtonGroup searchEngineGroup = new ButtonGroup();
		searchEngineGroup.add(libSearchCheckBox);
		searchEngineGroup.add(iterSearchCheckBox);
		searchEngineGroup.add(largeScaleSearchCheckBox);

		perPanel = new JPanel();
		perPanel.setBorder(
				new TitledBorder(null, "Performance setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		perPanel.setLayout(new MigLayout("", "[100][100]", "[40]"));
		parameterPanel.add(perPanel, "cell 1 0,grow");

		int totalCount = ProcessorCount();

		lblNumberOfThreads = new JLabel("Number of threads");
		perPanel.add(lblNumberOfThreads, "cell 0 0");

		threadComboBox = new JComboBox<Integer>();
		perPanel.add(threadComboBox, "cell 1 0,growx");
		for (int i = 1; i <= totalCount; i++) {
			threadComboBox.addItem(i);
		}

		int threadCount = par.getThreadCount();
		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}
		
		magDbStatPanel = new MagDbStatPanel(diaPar, (MetaSourcesDia) msv);
		parameterPanel.add(magDbStatPanel, "cell 0 1 2 1,grow");
		JButton predictedButton = magDbStatPanel.getPredictButton();
		predictedButton.addActionListener(l -> {

			if (!msd.findDeepDetect()) {
				JOptionPane.showMessageDialog(this, "DeepDetect is not found, please check the Setting -> Resource.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
			
			int thread = threadComboBox.getItemAt(threadComboBox.getSelectedIndex());

			if (thread == 1) {

				Object[] options = { "Yes, continue with 1 thread.", "No, add more threads." };

				int choice = JOptionPane.showOptionDialog(this,
						"Only 1 thread will be used in this task, which will take a very long time. Do you want to continue?",
						"Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				if (choice == 1) {
					return;
				}
			}

			predictedButton.setEnabled(false);
			magDbStatPanel.getProgressBar().setValue(0);
			magDbStatPanel.getProgressBar().setStringPainted(true);

			File deepDetectFile = new File(msd.getDeepDetect());

			deepDetectTask = new DeepDetectTask(deepDetectFile, new DeepDetectParameter(), diaPar.getUsedMagDbItem(),
					thread, magDbStatPanel.getProgressBar(), magDbStatPanel.getTimeTextField()) {
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
							File repoFile = diaPar.getUsedMagDbItem().getRepositoryFile();
							File currentFile = diaPar.getUsedMagDbItem().getCurrentFile();
							File sqlDbFile = new File(currentFile, repoFile.getName() + ".db");

							MagSqliteTask.create(sqlDbFile);
							try {
								MagSqliteTask.createTaxaTable(repoFile + "\\genomes-all_metadata.tsv", sqlDbFile);
								MagSqliteTask.createFuncTable(repoFile + "\\eggNOG", sqlDbFile);

							} catch (NumberFormatException | IOException e) {
								// TODO Auto-generated catch block
								LOGGER.error("SQL database: error in creating the SQL database", e);
								System.err.println(format.format(new Date()) + "\t"
										+ "SQL database: error in creating the SQL database");
							}
*/
							magDbStatPanel.update();
							magDbStatPanel.getProgressBar().setString("finished");

							setCursor(null);
							setEnabled(true);
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, "Task failed", "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, e.getMessage(), "Error",
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
		parameterPanel.add(pepLibPanel, "cell 2 0 1 2,grow");
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
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			diaLibCreateTask.execute();
		});

		modelPanel = new MagDbModelPanel(magDbItem, usedModels);
		parameterPanel.add(modelPanel, "cell 2 2 1 2,grow");
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

			diaModelTask = new DiaModelTask(diaPar, msd, modelPanel.getProgressBar(), pepFile.getAbsolutePath(),
					modelPanel.getModelName(), modelPanel.getModelType()) {
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
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, "Task finished", "Finish",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			diaModelTask.execute();
		});

		metaLabMetaPanel = new MetaLabMetaPanel(diaPar.getMetadata());

		metaLabMetaPanel.setBorder(
				new TitledBorder(null, "Metadata settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(metaLabMetaPanel, "cell 0 2 2 1,grow");

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(scrollPaneConsole, "cell 0 3 2 1,grow");

		ConsoleTextArea consoleTextArea = null;
		try {
			consoleTextArea = new ConsoleTextArea();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollPaneConsole.setViewportView(consoleTextArea);

		if (diaPar.isLibrarySearch()) {
			libSearchCheckBox.setSelected(true);
			iterSearchCheckBox.setSelected(false);
			largeScaleSearchCheckBox.setSelected(false);
			setComponentsEnabled(modelPanel, false);
		} else {

			if (diaPar.isSelfModelSearch()) {
				libSearchCheckBox.setSelected(false);
				iterSearchCheckBox.setSelected(false);
				largeScaleSearchCheckBox.setSelected(true);
			} else {
				libSearchCheckBox.setSelected(false);
				iterSearchCheckBox.setSelected(true);
				largeScaleSearchCheckBox.setSelected(false);
			}

			setComponentsEnabled(modelPanel, true);
		}

		libSearchCheckBox.addActionListener(l -> {
			if (libSearchCheckBox.isSelected()) {
				setComponentsEnabled(modelPanel, false);
			}
		});

		iterSearchCheckBox.addActionListener(l -> {
			if (iterSearchCheckBox.isSelected()) {
				setComponentsEnabled(modelPanel, true);
			}
		});
		
		largeScaleSearchCheckBox.addActionListener(l -> {
			if (largeScaleSearchCheckBox.isSelected()) {
				setComponentsEnabled(modelPanel, true);
			}
		});
	}

	protected void initialRunPanel() {
		JPanel consolePanel = new JPanel();
		tabbedPane.addTab("Run",
				new ImageIcon(MetaLabMainPanelDia.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
				consolePanel, null);

		consolePanel.setLayout(new MigLayout("", "[200:300:400,grow][200:300:400,grow][400:1000:1120,grow]",
				"[300:480:640,grow][120:160:200,grow][100:100:100][60:60:60]"));

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		consolePanel.add(scrollPaneConsole, "cell 2 0 1 3,grow");
		
		metaLabParViewPanel = new MetaLabParViewPanel(par);
		consolePanel.add(metaLabParViewPanel, "cell 0 0 2 1,grow");

		scrollPaneParCheck = new JScrollPane();
		consolePanel.add(scrollPaneParCheck, "cell 0 1 2 1,grow");

		textAreaParCheck = new JTextPane();
		textAreaParCheck.setForeground(Color.WHITE);
		textAreaParCheck.setContentType("text/html");
		textAreaParCheck.setEnabled(false);
		textAreaParCheck.setEditable(false);
		scrollPaneParCheck.setViewportView(textAreaParCheck);

		JPanel metaLabTaskPanel = new JPanel();

		consolePanel.add(metaLabTaskPanel, "cell 0 2 2 1,grow");
		metaLabTaskPanel
				.setLayout(new MigLayout("", "[120:180:250,grow][160:240:300,grow][120:180:250,grow]", "[25][25][30]"));

		JProgressBar progressBar = new JProgressBar();
		metaLabTaskPanel.add(progressBar, "cell 0 0 3 1,growx");

		JProgressBar progressBar_1 = new JProgressBar();
		metaLabTaskPanel.add(progressBar_1, "cell 0 1 3 1,growx");

		JButton btnStart_1 = new JButton("Start");

		JButton btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.addActionListener(l -> {
			if (task != null) {
				Thread thread = new Thread() {
					public void run() {
						task.forceStop();
					}
				};
				thread.start();
			}
		});
		metaLabTaskPanel.add(btnStop, "cell 2 2,alignx center");

		btnStart_1.addActionListener(l -> {

			if (this.warnings == null) {
				this.warnings = this.checkParameter();
				if (warnings.length > 0) {
					JOptionPane.showMessageDialog(this,
							warnings.length + " warnings exist, please check the parameters and settings.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}
			} else {
				if (warnings.length > 0) {
					JOptionPane.showMessageDialog(this,
							warnings.length + " warnings exist, please check the parameters and settings.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					setCursor(null);
					return;
				}
			}

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

			System.out.println("------Welcome to MetaLab " + MetaParaIoDia.version + "------");

			btnStart_1.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();

			task = new MetaLabMagTask((MetaParameterDia) par, (MetaSourcesDia) msv, progressBar, progressBar_1) {

				public void done() {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					progressBar_1.setIndeterminate(false);

					String log = consoleTextArea.getText();
					try {
						PrintWriter logwriter = new PrintWriter(new File(par.getResult(), "log.txt"));
						logwriter.print(log);
						logwriter.close();

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					setCursor(null);

					btnStart_1.setEnabled(true);
					btnStop.setEnabled(false);
					statusPanel.stop();
					MetaLabMainPanelDia.this.repaint();

					if (task.isCancelled()) {
						progressBar_1.setIndeterminate(false);
						progressBar_1.setString("Task stopped");

						return;
					}

					boolean finish = false;

					try {

						finish = get();

						if (finish) {

							progressBar_1.setString("finished");

							btnReportView.setEnabled(true);

							int reply = JOptionPane.showConfirmDialog(MetaLabMainPanelDia.this,
									"Task finished :)\nView the report?", "Finished", JOptionPane.YES_NO_OPTION);

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
							JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelDia.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};

			try {

				statusPanel.start();

				btnStop.setEnabled(true);

				task.execute();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(MetaLabMainPanelDia.this,
						"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);

			}
		});

		metaLabTaskPanel.add(btnStart_1, "cell 0 2,alignx center");

		btnReportView = new JButton("Report view");
		btnReportView.setEnabled(false);
		btnReportView.addActionListener(l -> {
			try {
				File report = new File(par.getReportDir(), "index.html");
				java.awt.Desktop.getDesktop().browse(report.toURI());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		metaLabTaskPanel.add(btnReportView, "cell 1 2,alignx center");

		this.statusPanel = new StatusPanel("Version: " + MetaParaIoDia.version);
		consolePanel.add(statusPanel, "cell 0 3 3 1");
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
		MetaData metadata = inputOutputPanel.getMetaData();
		String result = inputOutputPanel.getResultFile();
		par.setResult(result);

		String microdb = dbPanel.getMicroDb();

		par.setMicroDb(microdb);
		par.setMetadata(metadata);

		metaLabMetaPanel.update(metadata);
		metaLabMetaPanel.update();

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

		String result = inputOutputPanel.getResultFile();
		diaPar.setResult(result);

		MetaData metadata = inputOutputPanel.getMetaData();
		diaPar.setMetadata(metadata);
		
		int threadCount = (int) this.threadComboBox.getSelectedItem();
		par.setThreadCount(threadCount);
		
		metaLabMetaPanel.update(metadata);
		metaLabMetaPanel.update();
		
		boolean isLibSearch = libSearchCheckBox.isSelected();
		diaPar.setLibrarySearch(isLibSearch);
		
		boolean isLargeScaleSearch = largeScaleSearchCheckBox.isSelected();
		diaPar.setSelfModelSearch(isLargeScaleSearch);

		String[] usedLibs = this.pepLibPanel.getUsedLibs();
		String[] usedModels = this.modelPanel.getUsedModels();
		
		diaPar.setLibrary(usedLibs);
		diaPar.setModels(usedModels);
		
		metaLabParViewPanel.update();

		this.warnings = this.checkParameter();

		if (warnings.length > 0) {
			StringBuilder sb = new StringBuilder("<b>Warnings</b><p>");
			for (int i = 0; i < warnings.length; i++) {
				sb.append(warnings[i]).append("<p>");
			}
			sb.append("<b>").append(warnings.length).append("</b> ")
					.append("warnings are found, please check the parameter settings before start the task.");
			this.textAreaParCheck.setText(sb.toString());
		} else {
			this.textAreaParCheck.setText("Perfect! Ready to start.");
		}
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
