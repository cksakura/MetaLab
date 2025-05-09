package bmi.med.uOttawa.metalab.task.mag.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParaIOMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.pfind.MetaLabMagTask;
import bmi.med.uOttawa.metalab.task.pfind.gui.MetaLabPFindMainPanel;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindEnzymeIO;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindModIO;
import bmi.med.uOttawa.metalab.task.v1.gui.StatusPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabEnzymePanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIOPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIsobaricQuanPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMetaPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabModPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabParViewPanel;
import net.miginfocom.swing.MigLayout;

public class MetaLabMainPanelMag extends MetaLabPFindMainPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7604890275066535978L;
	@SuppressWarnings("unused")
	private static Logger LOGGER = LogManager.getLogger(MetaLabMainPanelMag.class);

	/**
	 * Create the panel.
	 */
	public MetaLabMainPanelMag(MetaParameter par, MetaSourcesMag msv) {
		super(par, msv);
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		this.workflowType = par.getWorkflowType();

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

	protected void initialMagDbPanel() {
		MagDbConfigPanel magDbConfigPanel = new MagDbConfigPanel(((MetaParameterMag) this.par).getMagDbItems());
		tabbedPane.addTab("MAG database",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/general/Import16.gif")),
				magDbConfigPanel, null);
		tabbedPane.setEnabledAt(0, true);
	}

	protected void initialIOPanel() {
		JPanel ioPanel = new JPanel();
		tabbedPane.addTab("Input data",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/general/Add16.gif")),
				ioPanel, null);
		tabbedPane.setEnabledAt(1, true);
		ioPanel.setLayout(new MigLayout("", "[800:1600:1920,grow]", "[450:750:930,grow][120]"));

		inputOutputPanel = new MetaLabIOPanel(par);
		ioPanel.add(inputOutputPanel, "cell 0 0,grow");

		dbPanel = new MetaLabMagDbPanel(par);
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ioPanel.add(dbPanel, "cell 0 1,grow");
	}

	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(new MigLayout("", "[600:800:960,grow][600:800:960,grow]",
				"[60][60][400,500,600,grow][250,350,450,grow]"));

		JPanel searchEnginePanel = new JPanel();
		searchEnginePanel
				.setBorder(new TitledBorder(null, "Search engine", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(searchEnginePanel, "cell 0 0,grow");
		searchEnginePanel.setLayout(
				new MigLayout("", "[150:200:240,grow][150:200:240,grow][150:200:240,grow][150:200:240,grow]", "[80]"));

		pfindCheckBox = new JCheckBox("pFind");
		searchEnginePanel.add(pfindCheckBox, "cell 0 0,grow");

		fragpipeBox = new JCheckBox("FragPipe");
		searchEnginePanel.add(fragpipeBox, "cell 1 0,grow");

		alphapeptBox = new JCheckBox("Alphapept");
		searchEnginePanel.add(alphapeptBox, "cell 2 0,grow");

		sageBox = new JCheckBox("Sage");
		searchEnginePanel.add(sageBox, "cell 3 0,grow");

		ButtonGroup searchEngineGroup = new ButtonGroup();
		searchEngineGroup.add(pfindCheckBox);
		searchEngineGroup.add(fragpipeBox);
		searchEngineGroup.add(alphapeptBox);
		searchEngineGroup.add(sageBox);

		if (this.workflowType == MetaLabWorkflowType.FragpipeIMMag) {
			pfindCheckBox.setSelected(false);
			pfindCheckBox.setEnabled(false);
			fragpipeBox.setSelected(true);
		} else if (this.workflowType == MetaLabWorkflowType.AlphapeptIMMag) {
			pfindCheckBox.setSelected(false);
			pfindCheckBox.setEnabled(false);
			alphapeptBox.setSelected(true);
		} else if (this.workflowType == MetaLabWorkflowType.SageMAG) {
			pfindCheckBox.setSelected(false);
			fragpipeBox.setSelected(false);
			alphapeptBox.setSelected(false);
			sageBox.setSelected(true);
		} else {
			pfindCheckBox.setSelected(true);
		}

		settingPanel = new JPanel();
		settingPanel.setLayout(new MigLayout("", "[300:400:480,grow][300:400:480,grow]", "[80]"));
		parameterPanel.add(settingPanel, "cell 0 1,grow");

		perPanel = new JPanel();
		perPanel.setBorder(
				new TitledBorder(null, "Performance setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingPanel.add(perPanel, "cell 0 0,grow");
		perPanel.setLayout(new MigLayout("", "[100][100]", "[40]"));

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

		instruPanel = new JPanel() {

			/**
			 * 
			 */
			protected static final long serialVersionUID = 9195455881699288360L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				Component[] components = getComponents();
				for (Component comp : components) {
					comp.setEnabled(enabled);
				}
			}
		};
		instruPanel.setBorder(
				new TitledBorder(null, "Instrument setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingPanel.add(instruPanel, "cell 1 0,grow");
		instruPanel.setLayout(new MigLayout("", "[100][20][100][20]", "[40]"));

		JLabel lblMsReso = new JLabel("MS2 scan mode");
		instruPanel.add(lblMsReso, "cell 0 0");

		ms2ComboBox = new JComboBox<String>();
		instruPanel.add(ms2ComboBox, "cell 2 0,growx");

		ms2ComboBox.addItem(MetaConstants.HCD_FTMS);
		ms2ComboBox.addItem(MetaConstants.HCD_ITMS);
		ms2ComboBox.addItem(MetaConstants.CID_FTMS);
		ms2ComboBox.addItem(MetaConstants.CID_ITMS);

		if (this.workflowType == MetaLabWorkflowType.FragpipeIMMag
				|| this.workflowType == MetaLabWorkflowType.AlphapeptIMMag) {
			setComponentsEnabled(instruPanel, false);
		}

		String ms2ScanMode = par.getMs2ScanMode();
		if (ms2ScanMode.equals(MetaConstants.HCD_FTMS)) {
			ms2ComboBox.setSelectedIndex(0);
		} else if (ms2ScanMode.equals(MetaConstants.HCD_ITMS)) {
			ms2ComboBox.setSelectedIndex(1);
		} else if (ms2ScanMode.equals(MetaConstants.CID_FTMS)) {
			ms2ComboBox.setSelectedIndex(2);
		} else if (ms2ScanMode.equals(MetaConstants.CID_ITMS)) {
			ms2ComboBox.setSelectedIndex(3);
		}

		JPanel quantPanel = new JPanel();
		quantPanel.setBorder(
				new TitledBorder(null, "Quantification", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(quantPanel, "cell 1 0 1 3,grow");
		quantPanel.setLayout(new MigLayout("", "[100:500:660,grow][20][80][20][80][20][80]", "[40][40][500,grow]"));

		JLabel lblQuantificationMode = new JLabel("Quantification mode");
		quantPanel.add(lblQuantificationMode, "cell 0 0");

		lblLabelQuantificationResult = new JLabel("Combine same label in different samples");
		quantPanel.add(lblLabelQuantificationResult, "cell 0 1");

		rdbtnCombine = new JRadioButton("Yes");
		combineButtonGroup.add(rdbtnCombine);
		quantPanel.add(rdbtnCombine, "cell 2 1");

		rdbtnNotCombine = new JRadioButton("No");
		combineButtonGroup.add(rdbtnNotCombine);
		quantPanel.add(rdbtnNotCombine, "cell 6 1");

		rdbtnLabelFree = new JRadioButton(MetaConstants.labelFree);
		quanButtonGroup.add(rdbtnLabelFree);
		quantPanel.add(rdbtnLabelFree, "cell 2 0");
		rdbtnLabelFree.addActionListener(l -> {
			if (rdbtnLabelFree.isSelected()) {
				metaLabIsobaricQuanPanel.removeAllLabels();
				metaLabIsobaricQuanPanel.setEnabled(false);

				lblLabelQuantificationResult.setEnabled(false);
				rdbtnCombine.setEnabled(false);
				rdbtnNotCombine.setEnabled(false);

				update1();
			}
		});

		rdbtnIsobaricLabeling = new JRadioButton(MetaConstants.isobaricLabel);
		quanButtonGroup.add(rdbtnIsobaricLabeling);
		quantPanel.add(rdbtnIsobaricLabeling, "cell 6 0");
		rdbtnIsobaricLabeling.addActionListener(l -> {
			if (rdbtnIsobaricLabeling.isSelected()) {
				metaLabIsobaricQuanPanel.setEnabled(true);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);

				update1();
			}
		});

		MetaParameterMag magPar = (MetaParameterMag) par;

		metaLabModPanel = new MetaLabModPanel(magPar, PFindModIO.getMaxQuantMods(msv.getMod()));
		parameterPanel.add(metaLabModPanel, "cell 0 2,grow");

		metaLabEnzymePanel = new MetaLabEnzymePanel(magPar, PFindEnzymeIO.getEnzymes(msv.getEnzyme()));
		parameterPanel.add(metaLabEnzymePanel, "cell 0 3,grow");

		rdbtnCombine.setSelected(false);
		rdbtnNotCombine.setSelected(true);

		metaLabIsobaricQuanPanel = new MetaLabIsobaricQuanPanel(magPar);

		TableModelListener[] listeners = metaLabIsobaricQuanPanel.getIsobaricModel1().getTableModelListeners();
		if (listeners.length == 2) {
			metaLabIsobaricQuanPanel.getIsobaricModel1().removeTableModelListener(listeners[0]);
			metaLabIsobaricQuanPanel.getIsobaricModel1().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {
					// TODO Auto-generated method stub

					int row = e.getFirstRow();
					if (((boolean) metaLabIsobaricQuanPanel.getIsobaricModel1().getValueAt(row, 0)) == true) {
						update1();
					}
				}
			});
// change the order of the listeners
			metaLabIsobaricQuanPanel.getIsobaricModel1().addTableModelListener(listeners[0]);
		}

		rdbtnLabelFree.setSelected(true);
		rdbtnIsobaricLabeling.setEnabled(true);

		metaLabIsobaricQuanPanel.setBorder(new TitledBorder(null, MetaConstants.isobaricLabel, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		quantPanel.add(metaLabIsobaricQuanPanel, "cell 0 2 7 2,grow");
		if (this.workflowType == MetaLabWorkflowType.FragpipeIMMag
				|| this.workflowType == MetaLabWorkflowType.AlphapeptIMMag) {
			metaLabIsobaricQuanPanel.setEnabled(false);
		}

		if (this.workflowType == MetaLabWorkflowType.FragpipeIMMag
				|| this.workflowType == MetaLabWorkflowType.AlphapeptIMMag) {
			setComponentsEnabled(metaLabModPanel, false);
			setComponentsEnabled(metaLabEnzymePanel, false);
			rdbtnLabelFree.setSelected(true);
			rdbtnIsobaricLabeling.setSelected(false);
			setComponentsEnabled(quantPanel, false);
		} else if (this.workflowType == MetaLabWorkflowType.pFindMAG) {
			setComponentsEnabled(metaLabModPanel, true);
			setComponentsEnabled(metaLabEnzymePanel, true);

			if (magPar.getQuanMode().equals(MetaConstants.labelFree)) {
				rdbtnLabelFree.setSelected(true);
				metaLabIsobaricQuanPanel.setEnabled(false);
				lblLabelQuantificationResult.setEnabled(false);
				rdbtnCombine.setEnabled(false);
				rdbtnNotCombine.setEnabled(false);
			} else if (magPar.getQuanMode().equals(MetaConstants.isobaricLabel)) {
				rdbtnIsobaricLabeling.setSelected(true);
				metaLabIsobaricQuanPanel.setEnabled(true);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			}
		}

		metaLabMetaPanel = new MetaLabMetaPanel(magPar.getMetadata(), magPar.getMetadata().isIsobaricQuan());

		metaLabMetaPanel.setBorder(
				new TitledBorder(null, "Metadata settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(metaLabMetaPanel, "cell 1 3 1 1,grow");

		pfindCheckBox.addActionListener(l -> {
			if (pfindCheckBox.isSelected()) {
				setComponentsEnabled(metaLabModPanel, true);
				setComponentsEnabled(metaLabEnzymePanel, true);
				setComponentsEnabled(quantPanel, true);
				this.workflowType = MetaLabWorkflowType.pFindMAG;
				this.par.setWorkflowType(this.workflowType.name());
			}
		});

		alphapeptBox.addActionListener(l -> {
			if (alphapeptBox.isSelected()) {
				setComponentsEnabled(metaLabModPanel, false);
				setComponentsEnabled(metaLabEnzymePanel, false);
				setComponentsEnabled(instruPanel, false);
				rdbtnLabelFree.setSelected(true);
				rdbtnIsobaricLabeling.setSelected(false);
				setComponentsEnabled(quantPanel, false);
				this.workflowType = MetaLabWorkflowType.AlphapeptIMMag;
				this.par.setWorkflowType(this.workflowType.name());
			}
		});

		fragpipeBox.addActionListener(l -> {
			if (fragpipeBox.isSelected()) {
				setComponentsEnabled(metaLabModPanel, false);
				setComponentsEnabled(metaLabEnzymePanel, false);
				setComponentsEnabled(instruPanel, false);
				rdbtnLabelFree.setSelected(true);
				rdbtnIsobaricLabeling.setSelected(false);
				setComponentsEnabled(quantPanel, false);
				this.workflowType = MetaLabWorkflowType.FragpipeIMMag;
				this.par.setWorkflowType(this.workflowType.name());
			}
		});

		sageBox.addActionListener(l -> {
			if (sageBox.isSelected()) {
				setComponentsEnabled(metaLabModPanel, true);
				setComponentsEnabled(metaLabEnzymePanel, true);
				setComponentsEnabled(quantPanel, true);
				this.workflowType = MetaLabWorkflowType.SageMAG;
				this.par.setWorkflowType(this.workflowType.name());
			}
		});
	}

	protected void initialRunPanel() {
		JPanel consolePanel = new JPanel();
		tabbedPane.addTab("Run",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
				consolePanel, null);

		consolePanel.setLayout(new MigLayout("", "[200:300:400,grow][200:300:400,grow][400:1000:1120,grow]",
				"[300:480:640,grow][120:160:200,grow][100:100:100][60:60:60]"));

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		metaLabParViewPanel = new MetaLabParViewPanel(par);
		consolePanel.add(metaLabParViewPanel, "cell 0 0 2 1,grow");

		consolePanel.add(scrollPaneConsole, "cell 2 0 1 3,grow");

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
/*
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
*/
			try {
				consoleTextArea = new ConsoleTextArea();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scrollPaneConsole.setViewportView(consoleTextArea);

			System.out.println("------Welcome to MetaLab " + MetaParaIOMag.version + "------");

			btnStart_1.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();

			task = new MetaLabMagTask(par, (MetaSourcesMag) msv, progressBar, progressBar_1) {

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
					MetaLabMainPanelMag.this.repaint();

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

							int reply = JOptionPane.showConfirmDialog(MetaLabMainPanelMag.this,
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
							JOptionPane.showMessageDialog(MetaLabMainPanelMag.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelMag.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelMag.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};

			try {

				statusPanel.start();

				btnStop.setEnabled(true);

				task.execute();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(MetaLabMainPanelMag.this,
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

		this.statusPanel = new StatusPanel("Version: " + MetaParaIOMag.version);
		consolePanel.add(statusPanel, "cell 0 3 3 1");
	}

	protected void update0() {
		((MetaLabMagDbPanel) dbPanel).update(par);
	}

	protected void update1() {
		MetaData metadata = inputOutputPanel.getMetaData();
		if (rdbtnLabelFree.isSelected()) {
			metadata.setLabelTitle(new String[] {});
		}
		if (rdbtnIsobaricLabeling.isSelected() && metaLabIsobaricQuanPanel.getIsobaricTag() != null) {
			metadata.setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
		}
		boolean isTimsTof = metadata.isTimsTof();

		if (isTimsTof) {
			if (this.workflowType == MetaLabWorkflowType.pFindMAG) {
				this.workflowType = MetaLabWorkflowType.FragpipeIMMag;
			}

			pfindCheckBox.setEnabled(false);

			setComponentsEnabled(metaLabModPanel, false);
			setComponentsEnabled(metaLabEnzymePanel, false);

		} else {
			pfindCheckBox.setEnabled(true);
			if (this.workflowType == MetaLabWorkflowType.pFindMAG) {
				setComponentsEnabled(metaLabModPanel, true);
				setComponentsEnabled(metaLabEnzymePanel, true);
			} else {
				setComponentsEnabled(metaLabModPanel, false);
				setComponentsEnabled(metaLabEnzymePanel, false);
			}
		}

		par.setWorkflowType(this.workflowType.name());

		String result = inputOutputPanel.getResultFile();
		par.setResult(result);

		String microdb = dbPanel.getMicroDb();
		String hostdb = dbPanel.getHostDB();
		boolean isappend = dbPanel.isAppendHostDb();

		par.setMicroDb(microdb);
		par.setHostDB(hostdb);
		par.setAppendHostDb(isappend);
		par.setMetadata(metadata);

		metaLabMetaPanel.update(metadata);
		metaLabMetaPanel.update();
	}

	protected void update2() {

		if (rdbtnIsobaricLabeling.isSelected()) {
			IsobaricTag tag = metaLabIsobaricQuanPanel.getIsobaricTag();
			if (tag == null) {

				JOptionPane.showMessageDialog(this,
						"Please choose the isobaric labeling method when \"Isobaric labeling\" "
								+ "has been selected in the \"Quantification mode.\"",
						"Warning", JOptionPane.WARNING_MESSAGE);

				tabbedPane.setSelectedIndex(2);

				return;
			}
		}

		if (par.getWorkflowType() == MetaLabWorkflowType.FragpipeIMMag) {

			String[] fragEnzyme = metaLabEnzymePanel.getFragpipeEnzyme();
			((MetaParameterMag) par).setFragpipeEnzyme(fragEnzyme);

		} else if (par.getWorkflowType() == MetaLabWorkflowType.pFindMAG) {

			MetaParameterMag magPar = (MetaParameterMag) par;

			String[] fixMods = metaLabModPanel.getPFindFixMods();
			String[] variMods = metaLabModPanel.getPFindVariMods();

			magPar.setFixMods(fixMods);
			magPar.setVariMods(variMods);

			String enzyme = metaLabEnzymePanel.getPFindEnzyme();
			int digestMode = metaLabEnzymePanel.getDigestMode();
			int miss = metaLabEnzymePanel.getMissCleavages();

			magPar.setEnzyme(enzyme);
			magPar.setDigestMode(digestMode);
			magPar.setMissCleavages(miss);

			if (rdbtnLabelFree.isSelected()) {
				magPar.setQuanMode(MetaConstants.labelFree);
				magPar.setIsobaric(new MaxquantModification[] {});
				magPar.getMetadata().setLabelTitle(new String[] {});
				magPar.setIsoCorFactor(new double[][] {});
				magPar.setIsobaricTag(null);
			}

			if (rdbtnIsobaricLabeling.isSelected()) {
				magPar.setQuanMode(MetaConstants.isobaricLabel);
				MaxquantModification[] isobaric = metaLabIsobaricQuanPanel.getIsobaricTags();
				magPar.setIsobaric(isobaric);

				double[][] factor = metaLabIsobaricQuanPanel.getIsoCorFactor();
				magPar.setIsoCorFactor(factor);

				IsobaricTag tag = metaLabIsobaricQuanPanel.getIsobaricTag();
				magPar.setIsobaricTag(tag);

				magPar.getMetadata().setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
			}

			String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
			magPar.setMs2ScanMode(ms2ScanMode);
		} else if (par.getWorkflowType() == MetaLabWorkflowType.SageMAG) {

			MetaParameterMag magPar = (MetaParameterMag) par;

			MaxquantModification[] fixMods = metaLabModPanel.getPFindFullFixMods();
			MaxquantModification[] variMods = metaLabModPanel.getPFindFullVariMods();

			magPar.setFullFixMods(fixMods);
			magPar.setFullVariMods(variMods);

			String[] enzyme = metaLabEnzymePanel.getFragpipeEnzyme();
			int digestMode = metaLabEnzymePanel.getDigestMode();
			int miss = metaLabEnzymePanel.getMissCleavages();

			magPar.setFragpipeEnzyme(enzyme);
			magPar.setDigestMode(digestMode);
			magPar.setMissCleavages(miss);

			if (rdbtnLabelFree.isSelected()) {
				magPar.setQuanMode(MetaConstants.labelFree);
				magPar.setIsobaric(new MaxquantModification[] {});
				magPar.getMetadata().setLabelTitle(new String[] {});
				magPar.setIsoCorFactor(new double[][] {});
				magPar.setIsobaricTag(null);
			}

			if (rdbtnIsobaricLabeling.isSelected()) {
				magPar.setQuanMode(MetaConstants.isobaricLabel);
				MaxquantModification[] isobaric = metaLabIsobaricQuanPanel.getIsobaricTags();
				magPar.setIsobaric(isobaric);

				double[][] factor = metaLabIsobaricQuanPanel.getIsoCorFactor();
				magPar.setIsoCorFactor(factor);

				IsobaricTag tag = metaLabIsobaricQuanPanel.getIsobaricTag();
				magPar.setIsobaricTag(tag);

				magPar.getMetadata().setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
			}

			String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
			magPar.setMs2ScanMode(ms2ScanMode);
		}

		int threadCount = (int) this.threadComboBox.getSelectedItem();
		par.setThreadCount(threadCount);

		metaLabMetaPanel.update(par.getMetadata());

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

	protected String[] checkParameter() {
		ArrayList<String> list = new ArrayList<String>();

		MetaData metaData = par.getMetadata();
		if (metaData == null || metaData.getRawFiles() == null || metaData.getRawFiles().length == 0) {
			list.add("Raw files are not found.");
		} else {
			boolean timsTofWorkflow = false;
			boolean thermoWorkflow = false;
			String[] rawfiles = metaData.getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				} else {
					String name = filei.getName();
					if (name.endsWith(".d")) {
						timsTofWorkflow = true;
					} else if (name.endsWith(".raw")) {
						thermoWorkflow = true;
					}
				}
			}

			if (thermoWorkflow && timsTofWorkflow) {
				list.add("Only accept one type of raw file in one task, both .raw and .d files are detected");
			}

			String metaCheck = metaData.checkFlashLFQExpDesign()[0];

			if (metaCheck.length() > 0) {
				list.add(metaCheck);
			}
		}

		MetaParameterMag magPar = (MetaParameterMag) par;
		MagDbItem magDbItem = magPar.getUsedMagDbItem();
		if (magDbItem == null) {
			list.add("MAG database is not found.");
		} else {
			if (!magDbItem.isAvailable()) {
				list.add("MAG database" + magDbItem.getCatalogueID() + " is not available.");
			} else {
				File hapFile = magDbItem.getHapFastaFile();
				if (!hapFile.exists() || hapFile.length() == 0) {
					list.add("High protein database is not found.");
				}
			}
		}

		if (magPar.isAppendHostDb()) {
			if (magPar.getHostDB() == null || magPar.getHostDB().length() == 0) {
				list.add("Host database is not found.");
			} else {
				File dbfile = new File(magPar.getHostDB());
				if (!dbfile.exists()) {
					list.add("Host database " + dbfile + " didn't exist.");
				}
			}
		}

		if (par.getWorkflowType() == MetaLabWorkflowType.FragpipeIMMag) {

			if (!((MetaSourcesMag) msv).findFragpipe()) {
				list.add("Fragpipe is not found, please check the Setting -> Resource.");
			}

			if (magPar.getFragpipeEnzyme() == null || magPar.getFragpipeEnzyme().length == 0) {
				list.add("Enzyme is not set.");
			}

		} else {

			if (!msv.findPFind()) {
				list.add("pFind is not found, please check the Setting -> Resource.");
			}

			if (!msv.findFlashLFQ()) {
				list.add("FlashLFQ is not found, please check the Setting -> Resource.");
			}

			if (magPar.getEnzyme() == null || magPar.getEnzyme().length() == 0) {
				list.add("Enzyme is not set.");
			}
		}

		if (par.getResult() == null || par.getResult().length() == 0) {
			list.add("Result file is not found.");
		}

		String[] warnings = list.toArray(new String[list.size()]);

		return warnings;
	}
}
