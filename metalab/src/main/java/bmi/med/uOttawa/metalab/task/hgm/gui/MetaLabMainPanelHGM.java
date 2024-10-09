package bmi.med.uOttawa.metalab.task.hgm.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParaIOHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.pfind.MetaLabMagTask;
import bmi.med.uOttawa.metalab.task.pfind.gui.MetaLabPFindMainPanel;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindEnzymeIO;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindModIO;
import bmi.med.uOttawa.metalab.task.v1.gui.StatusPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabEnzymePanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIsobaricQuanPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMetaPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabModPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabParViewPanel;
import net.miginfocom.swing.MigLayout;

public class MetaLabMainPanelHGM extends MetaLabPFindMainPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7604890275066535978L;

	private int totalGenomeDbCount = 4744;

	/**
	 * Create the panel.
	 */
	public MetaLabMainPanelHGM(MetaParameter par, MetaSourcesHGM msv) {
		super(par, msv);
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		this.workflowType = MetaLabWorkflowType.pFindHGM;

		this.initialIOPanel();
		this.initialParPanel();
		this.initialRunPanel();

		tabbedPane.setEnabledAt(2, false);

		tabbedPane.addChangeListener(l -> {

			int selectedIndex = tabbedPane.getSelectedIndex();
			switch (selectedIndex) {
			case 0:
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, false);
				break;
			case 1:
				tabbedPane.setEnabledAt(2, true);
				update1();
				break;
			case 2:
				update2();
				break;
			default:
				break;
			}
		});
	}

	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(
				new MigLayout("", "[600:800:960,grow][600:800:960,grow]", "[300,400,500,grow][300,400,500,grow][80]"));

		JPanel quantPanel = new JPanel();
		quantPanel.setBorder(
				new TitledBorder(null, "Quantification", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(quantPanel, "cell 1 0,grow");
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

		MetaParameterPFind parV3 = (MetaParameterPFind) par;

		metaLabModPanel = new MetaLabModPanel(parV3, PFindModIO.getMods(msv.getMod()));
		parameterPanel.add(metaLabModPanel, "cell 0 0,grow");

		metaLabEnzymePanel = new MetaLabEnzymePanel(parV3, PFindEnzymeIO.getEnzymes(msv.getEnzyme()));
		parameterPanel.add(metaLabEnzymePanel, "cell 0 1,grow");

		rdbtnCombine.setSelected(false);
		rdbtnNotCombine.setSelected(true);

		metaLabIsobaricQuanPanel = new MetaLabIsobaricQuanPanel(parV3);

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

		if (parV3.getQuanMode().equals(MetaConstants.labelFree)) {
			rdbtnLabelFree.setSelected(true);
			metaLabIsobaricQuanPanel.setEnabled(false);
			lblLabelQuantificationResult.setEnabled(false);
			rdbtnCombine.setEnabled(false);
			rdbtnNotCombine.setEnabled(false);
		} else if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
			rdbtnIsobaricLabeling.setSelected(true);
			metaLabIsobaricQuanPanel.setEnabled(true);
			lblLabelQuantificationResult.setEnabled(true);
			rdbtnCombine.setEnabled(true);
			rdbtnNotCombine.setEnabled(true);
		}

		metaLabIsobaricQuanPanel.setBorder(new TitledBorder(null, MetaConstants.isobaricLabel, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		quantPanel.add(metaLabIsobaricQuanPanel, "cell 0 2 7 2,grow");

		settingPanel = new JPanel();
		settingPanel.setLayout(new MigLayout("", "[300:400:480,grow][300:400:480,grow]", "[50:60:70,grow]"));
		parameterPanel.add(settingPanel, "cell 0 2,grow");

		perPanel = new JPanel();
		perPanel.setBorder(
				new TitledBorder(null, "Performance setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		settingPanel.add(perPanel, "cell 0 0,grow");
		perPanel.setLayout(new MigLayout("", "[100][100]", "[40]"));

		int totalCount = Runtime.getRuntime().availableProcessors();

		lblNumberOfThreads = new JLabel("Number of threads");
		perPanel.add(lblNumberOfThreads, "cell 0 0");

		threadComboBox = new JComboBox<Integer>();
		perPanel.add(threadComboBox, "cell 1 0,growx");
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
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

		metaLabMetaPanel = new MetaLabMetaPanel(parV3.getMetadata(), parV3.getMetadata().isIsobaricQuan());

		metaLabMetaPanel.setBorder(
				new TitledBorder(null, "Metadata settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(metaLabMetaPanel, "cell 1 1 1 2,grow");
	}

	protected void initialRunPanel() {
		JPanel consolePanel = new JPanel();
		tabbedPane.addTab("Run",
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/development/Host16.gif")),
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

			System.out.println("------Welcome to MetaLab " + MetaParaIOHGM.version + "------");

			btnStart_1.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();

			task = new MetaLabMagTask((MetaParameterPFind) par, msv, progressBar, progressBar_1) {

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
					MetaLabMainPanelHGM.this.repaint();

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

							int reply = JOptionPane.showConfirmDialog(MetaLabMainPanelHGM.this,
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
							JOptionPane.showMessageDialog(MetaLabMainPanelHGM.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelHGM.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);

					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MetaLabMainPanelHGM.this, helpInfo, "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			};

			try {

				statusPanel.start();

				btnStop.setEnabled(true);

				task.execute();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(MetaLabMainPanelHGM.this,
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

		this.statusPanel = new StatusPanel("Version: " + MetaParaIOHGM.version);
		consolePanel.add(statusPanel, "cell 0 3 3 1");
	}

	protected void update1() {
		MetaParameterPFind parV3 = (MetaParameterPFind) par;

		String result = inputOutputPanel.getResultFile();
		parV3.setResult(result);

		String microdb = dbPanel.getMicroDb();
		String hostdb = dbPanel.getHostDB();
		boolean isappend = dbPanel.isAppendHostDb();

		parV3.setMicroDb(microdb);
		parV3.setHostDB(hostdb);
		parV3.setAppendHostDb(isappend);

		MetaData metadata = inputOutputPanel.getMetaData();
		if (rdbtnLabelFree.isSelected()) {
			metadata.setLabelTitle(new String[] {});
		}
		if (rdbtnIsobaricLabeling.isSelected() && metaLabIsobaricQuanPanel.getIsobaricTag() != null) {

			metadata.setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
		}
		parV3.setMetadata(metadata);

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

				tabbedPane.setSelectedIndex(1);

				return;
			}
		}

		MetaParameterPFind parV3 = (MetaParameterPFind) par;

		String[] fixMods = metaLabModPanel.getPFindFixMods();
		String[] variMods = metaLabModPanel.getPFindVariMods();

		parV3.setFixMods(fixMods);
		parV3.setVariMods(variMods);

		String enzyme = metaLabEnzymePanel.getPFindEnzyme();
		int digestMode = metaLabEnzymePanel.getDigestMode();
		int miss = metaLabEnzymePanel.getMissCleavages();

		parV3.setEnzyme(enzyme);
		parV3.setDigestMode(digestMode);
		parV3.setMissCleavages(miss);

		if (rdbtnLabelFree.isSelected()) {
			parV3.setQuanMode(MetaConstants.labelFree);
			parV3.setIsobaric(new MaxquantModification[] {});
			parV3.getMetadata().setLabelTitle(new String[] {});
			parV3.setIsoCorFactor(new double[][] {});
			parV3.setIsobaricTag(null);
		}

		if (rdbtnIsobaricLabeling.isSelected()) {
			parV3.setQuanMode(MetaConstants.isobaricLabel);
			MaxquantModification[] isobaric = metaLabIsobaricQuanPanel.getIsobaricTags();
			parV3.setIsobaric(isobaric);

			double[][] factor = metaLabIsobaricQuanPanel.getIsoCorFactor();
			parV3.setIsoCorFactor(factor);

			IsobaricTag tag = metaLabIsobaricQuanPanel.getIsobaricTag();
			parV3.setIsobaricTag(tag);

			parV3.getMetadata().setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
		}

		int threadCount = (int) this.threadComboBox.getSelectedItem();
		parV3.setThreadCount(threadCount);

		String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
		parV3.setMs2ScanMode(ms2ScanMode);

		metaLabMetaPanel.update(parV3.getMetadata());

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
		case 0:
			update1();
			break;
		case 1:
			update2();
			break;
		default:
			break;
		}
	}

	protected String[] checkParameter() {
		ArrayList<String> list = new ArrayList<String>();

		MetaParameterPFind parV3 = (MetaParameterPFind) par;

		if (!msv.findPFind()) {
			list.add("pFind is not found, please check the Setting -> Resource.");
		}

		if (!msv.findFlashLFQ()) {
			list.add("FlashLFQ is not found, please check the Setting -> Resource.");
		}

		if (parV3.getMetadata() == null || parV3.getMetadata().getRawFiles() == null
				|| parV3.getMetadata().getRawFiles().length == 0) {
			list.add("Raw files are not found.");
		} else {
			String[] rawfiles = parV3.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}

			String metaCheck = parV3.getMetadata().checkFlashLFQExpDesign()[0];

			if (metaCheck.length() > 0) {
				list.add(metaCheck);
			}
		}

		File microDbFile = new File(parV3.getMicroDb());
		if (!microDbFile.exists()) {
			list.add("Microbiome database " + microDbFile + " is not found.");
		} else {
			if (microDbFile.exists() && microDbFile.isDirectory()) {
				File originalDbFile = new File(microDbFile, "original_db");
				if (!originalDbFile.exists() || !originalDbFile.isDirectory()) {
					list.add("Genome protein databases are not found in " + microDbFile);
				} else {
					File[] genomeFiles = originalDbFile.listFiles(new FileFilter() {

						@Override
						public boolean accept(File pathname) {
							// TODO Auto-generated method stub
							if (pathname.getName().endsWith(".faa"))
								return true;
							return false;
						}
					});

					if (genomeFiles.length < this.totalGenomeDbCount) {
						list.add("The genome protein databases is not downloaded completely.");
					}
				}
				File hapFile = new File(microDbFile, "rib_elon.fasta");
				if (!hapFile.exists() || hapFile.length() == 0) {
					list.add("High protein database is not found.");
				}
			} else {
				list.add(
						"Only human gut microbiome gene catelog database folder is accepted in \"Microbiome database\".");
			}
		}

		if (parV3.isAppendHostDb()) {
			if (parV3.getHostDB() == null || parV3.getHostDB().length() == 0) {
				File dbfile = new File(parV3.getMicroDb());
				if (dbfile.isFile()) {
					list.add("Host database is not found.");
				} else {
					if (!dbfile.exists()) {
						list.add("Host database is not found.");
					}
				}

			} else {
				File dbfile = new File(parV3.getHostDB());
				if (!dbfile.exists()) {
					list.add("Host database " + dbfile + " is not found.");
				}
			}
		}

		if (parV3.getResult() == null || parV3.getResult().length() == 0) {
			list.add("Result file is not found.");
		}

		if (parV3.getEnzyme() == null || parV3.getEnzyme().length() == 0) {
			list.add("Enzyme is not set.");
		}

		String[] warnings = list.toArray(new String[list.size()]);

		return warnings;
	}
}
