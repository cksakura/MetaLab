package bmi.med.uOttawa.metalab.task.pfind.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.MetaLabTask;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.gui.MetaLabDBPanel;
import bmi.med.uOttawa.metalab.task.gui.MetaLabTaxaPanel;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;
import bmi.med.uOttawa.metalab.task.pfind.MetaLabMagTask;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindEnzymeIO;
import bmi.med.uOttawa.metalab.task.pfind.par.PFindModIO;
import bmi.med.uOttawa.metalab.task.v1.gui.StatusPanel;
import bmi.med.uOttawa.metalab.task.v2.MetaLabTaskV2;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMaxQuantPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabEnzymePanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIOPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIsobaricQuanPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabIsotopicQuanPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMetaPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabModPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabPFindPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMSFraggerPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabParViewPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabPepProViewPanel;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSsdbPanel;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParaIOMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import net.miginfocom.swing.MigLayout;

public class MetaLabPFindMainPanel extends JPanel {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -7287656906294747672L;

	protected final ButtonGroup quanButtonGroup = new ButtonGroup();
	protected StatusPanel statusPanel;
	protected MetaLabIsotopicQuanPanel metaLabIsotopicQuanPanel;
	protected MetaLabIsobaricQuanPanel metaLabIsobaricQuanPanel;
	protected MetaLabIOPanel inputOutputPanel;
	protected MetaLabDBPanel dbPanel;
	protected MetaLabModPanel metaLabModPanel;
	protected MetaLabEnzymePanel metaLabEnzymePanel;
	protected JButton btnReportView;
	protected JRadioButton rdbtnIsobaricLabeling;
	protected JRadioButton rdbtnIsotopicLabeling;
	protected JRadioButton rdbtnLabelFree;
	protected MetaLabParViewPanel metaLabParViewPanel;

	protected MetaLabWorkflowType workflowType;
	protected MetaParameter par;
	protected MetaSourcesV2 msv;
	protected JLabel lblLabelQuantificationResult;
	protected JRadioButton rdbtnCombine;
	protected JRadioButton rdbtnNotCombine;
	protected final ButtonGroup combineButtonGroup = new ButtonGroup();
	protected JPanel instruPanel;
	protected JLabel lblNumberOfThreads;
	protected JComboBox<Integer> threadComboBox;
	protected JComboBox<String> ms2ComboBox;
	protected JPanel settingPanel;
	protected JPanel perPanel;
	protected MetaLabTaxaPanel metaLabTaxaPanel;
	protected MetaLabMaxQuantPanel metaLabClosedPanel;
	protected MetaLabMSFraggerPanel metaLabMSFraggerPanel;
	protected MetaLabPFindPanel metaLabPFindPanel;
	protected MetaLabSsdbPanel metaLabSsdbPanel;
	protected JScrollPane scrollPaneParCheck;
	protected JTextPane textAreaParCheck;

	protected JTabbedPane tabbedPane;
	protected int previousTabIndex;
	
	protected String[] warnings;
	protected MetaLabMetaPanel metaLabMetaPanel;

	protected ConsoleTextArea consoleTextArea = null;

	protected MetaLabPepProViewPanel pepPanel;
	protected MetaLabPepProViewPanel proPanel;
	protected JRadioButton rdbtnMetaSearchYes;
	protected final ButtonGroup metaButtonGroup = new ButtonGroup();
	
	protected JCheckBox pfindCheckBox;
	protected JCheckBox fragpipeBox;
	protected JCheckBox alphapeptBox;
	protected JCheckBox sageBox;

	protected MetaLabTask task;

	protected final String helpInfo = "Task failed :(\nplease contact us to get a solution\ntechteam.metalab@gmail.com\n(attaching the files"
			+ "\"log.txt\" and \"logging.log\" in the result folder could be helpful)";

	/**
	 * Create the panel.
	 */
	public MetaLabPFindMainPanel(MetaParameter par, MetaSourcesV2 msv) {

		this.par = par;
		this.msv = msv;

		setLayout(new MigLayout("", "[1000:1600:1920,grow]", "[800:900:1080,grow]"));

		this.initial();
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		workflowType = par.getWorkflowType();

		switch (workflowType) {
		case TaxonomyAnalysis: {
			this.initialTaxaPepViewPanel();
			this.initialSettingPanel();
			this.initialRunPanel();

			tabbedPane.setEnabledAt(2, false);
			break;
		}
		case FunctionalAnnotation: {
			this.initialFuncProViewPanel();
			this.initialRunPanel();
			break;
		}
		default: {
			this.initialIOPanel();
			this.initialParPanel();
			this.initialSettingPanel();
			this.initialRunPanel();

			tabbedPane.setEnabledAt(2, false);
			tabbedPane.setEnabledAt(3, false);
			break;
		}
		}

		// this is used for "Design"
//		this.initialIOPanel();
//		this.initialParPanel();
//		this.initialSettingPanel();
//		this.initialRunPanel();
		previousTabIndex = tabbedPane.getSelectedIndex();
		
		tabbedPane.addChangeListener(l -> {

			int selectedIndex = tabbedPane.getSelectedIndex();
			switch (selectedIndex) {
			case 0:
				if (tabbedPane.getTabCount() == 4) {
					tabbedPane.setEnabledAt(1, true);
					tabbedPane.setEnabledAt(2, false);
					tabbedPane.setEnabledAt(3, false);
				} else if (tabbedPane.getTabCount() == 3) {
					tabbedPane.setEnabledAt(1, true);
					tabbedPane.setEnabledAt(2, false);
				}
				break;
			case 1:
				if (tabbedPane.getTabCount() == 4) {
					tabbedPane.setEnabledAt(2, true);
					tabbedPane.setEnabledAt(3, false);
					update1();
				} else if (tabbedPane.getTabCount() == 3) {
					tabbedPane.setEnabledAt(2, true);
				} else if (tabbedPane.getTabCount() == 2) {
					update3();
				}
				break;
			case 2:
				if (tabbedPane.getTabCount() == 4) {
					tabbedPane.setEnabledAt(3, true);
					update2();
				} else if (tabbedPane.getTabCount() == 3) {
					update3();
				}
				break;
			case 3:
				update3();
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
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Add16.gif")),
				ioPanel, null);
		tabbedPane.setEnabledAt(0, true);
		ioPanel.setLayout(new MigLayout("", "[800:1600:1920,grow]", "[450:750:930,grow][120]"));

		inputOutputPanel = new MetaLabIOPanel(par);
		ioPanel.add(inputOutputPanel, "cell 0 0,grow");

		dbPanel = new MetaLabDBPanel(par);
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ioPanel.add(dbPanel, "cell 0 1,grow");
	}

	protected void initialTaxaPepViewPanel() {
		pepPanel = new MetaLabPepProViewPanel(MetaLabWorkflowType.TaxonomyAnalysis);
		tabbedPane.addTab("Input data",
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Add16.gif")),
				pepPanel, null);
		tabbedPane.setEnabledAt(0, true);
	}

	protected void initialFuncProViewPanel() {
		proPanel = new MetaLabPepProViewPanel(MetaLabWorkflowType.FunctionalAnnotation);
		tabbedPane.addTab("Input data",
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Add16.gif")),
				proPanel, null);
		tabbedPane.setEnabledAt(0, true);
	}

	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(
				new MigLayout("", "[400:800:960,grow][400:800:960,grow]", "[300,400,500,grow][300,400,500,grow]"));

		JPanel quantPanel = new JPanel();
		quantPanel.setBorder(
				new TitledBorder(null, "Quantification", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(quantPanel, "cell 1 0 1 2,grow");
		quantPanel.setLayout(new MigLayout("", "[100:500:660,grow][20][80][20][80][20][80]",
				"[40][40][360:450:480,grow][360:450:480,grow]"));

		JLabel lblQuantificationMode = new JLabel("Quantification mode");
		quantPanel.add(lblQuantificationMode, "cell 0 0");

		lblLabelQuantificationResult = new JLabel("Combine same label in different samples");
		quantPanel.add(lblLabelQuantificationResult, "cell 0 1");

		rdbtnCombine = new JRadioButton("Yes");
		combineButtonGroup.add(rdbtnCombine);
		quantPanel.add(rdbtnCombine, "cell 2 1");

		rdbtnNotCombine = new JRadioButton("No");
		combineButtonGroup.add(rdbtnNotCombine);
		quantPanel.add(rdbtnNotCombine, "cell 4 1");

		rdbtnLabelFree = new JRadioButton(MetaConstants.labelFree);
		quanButtonGroup.add(rdbtnLabelFree);
		quantPanel.add(rdbtnLabelFree, "cell 2 0");
		rdbtnLabelFree.addActionListener(l -> {
			if (rdbtnLabelFree.isSelected()) {
				metaLabIsotopicQuanPanel.removeAllLabels();
				metaLabIsotopicQuanPanel.setEnabled(false);

				metaLabIsobaricQuanPanel.removeAllLabels();
				metaLabIsobaricQuanPanel.setEnabled(false);

				lblLabelQuantificationResult.setEnabled(false);
				rdbtnCombine.setEnabled(false);
				rdbtnNotCombine.setEnabled(false);
			}
		});

		rdbtnIsotopicLabeling = new JRadioButton(MetaConstants.isotopicLabel);
		quanButtonGroup.add(rdbtnIsotopicLabeling);
		quantPanel.add(rdbtnIsotopicLabeling, "cell 4 0");
		rdbtnIsotopicLabeling.addActionListener(l -> {
			if (rdbtnIsotopicLabeling.isSelected()) {
				metaLabIsotopicQuanPanel.setEnabled(true);
				metaLabIsobaricQuanPanel.removeAllLabels();
				metaLabIsobaricQuanPanel.setEnabled(false);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			}
		});

		rdbtnIsobaricLabeling = new JRadioButton(MetaConstants.isobaricLabel);
		quanButtonGroup.add(rdbtnIsobaricLabeling);
		quantPanel.add(rdbtnIsobaricLabeling, "cell 6 0");
		rdbtnIsobaricLabeling.addActionListener(l -> {
			if (rdbtnIsobaricLabeling.isSelected()) {
				metaLabIsotopicQuanPanel.removeAllLabels();
				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(true);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			}
		});

		if (this.workflowType == MetaLabWorkflowType.pFindWorkflow) {
			MetaParameterPFind parV3 = (MetaParameterPFind) par;

			metaLabModPanel = new MetaLabModPanel(parV3, PFindModIO.getMaxQuantMods(msv.getMod()));
			parameterPanel.add(metaLabModPanel, "cell 0 0,grow");

			metaLabEnzymePanel = new MetaLabEnzymePanel(parV3, PFindEnzymeIO.getEnzymes(msv.getEnzyme()));
			parameterPanel.add(metaLabEnzymePanel, "cell 0 1,grow");

			rdbtnCombine.setSelected(false);
			rdbtnNotCombine.setSelected(true);

			metaLabIsotopicQuanPanel = new MetaLabIsotopicQuanPanel();

			metaLabIsobaricQuanPanel = new MetaLabIsobaricQuanPanel(parV3);

			rdbtnLabelFree.setSelected(true);
			rdbtnIsotopicLabeling.setEnabled(false);
			rdbtnIsobaricLabeling.setEnabled(true);

			if (parV3.getQuanMode().equals(MetaConstants.labelFree)) {
				rdbtnLabelFree.setSelected(true);
				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(false);
				lblLabelQuantificationResult.setEnabled(false);
				rdbtnCombine.setEnabled(false);
				rdbtnNotCombine.setEnabled(false);
			} else if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
				rdbtnIsobaricLabeling.setSelected(true);
				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(true);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			}

		} else {
			MetaParameterMQ parV2 = (MetaParameterMQ) par;

			metaLabModPanel = new MetaLabModPanel(parV2);
			parameterPanel.add(metaLabModPanel, "cell 0 0,grow");

			metaLabEnzymePanel = new MetaLabEnzymePanel(parV2);
			parameterPanel.add(metaLabEnzymePanel, "cell 0 1,grow");

			rdbtnCombine.setSelected(parV2.isCombineLabel());
			rdbtnNotCombine.setSelected(!parV2.isCombineLabel());

			metaLabIsotopicQuanPanel = new MetaLabIsotopicQuanPanel(parV2);

			metaLabIsobaricQuanPanel = new MetaLabIsobaricQuanPanel(parV2);

			if (parV2.getQuanMode().equals(MetaConstants.labelFree)) {
				rdbtnLabelFree.setSelected(true);
				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(false);
				lblLabelQuantificationResult.setEnabled(false);
				rdbtnCombine.setEnabled(false);
				rdbtnNotCombine.setEnabled(false);
			} else if (parV2.getQuanMode().equals(MetaConstants.isotopicLabel)) {
				rdbtnIsotopicLabeling.setSelected(true);
				metaLabIsotopicQuanPanel.setEnabled(true);
				metaLabIsobaricQuanPanel.setEnabled(false);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			} else if (parV2.getQuanMode().equals(MetaConstants.isobaricLabel)) {
				rdbtnIsobaricLabeling.setSelected(true);
				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(true);
				lblLabelQuantificationResult.setEnabled(true);
				rdbtnCombine.setEnabled(true);
				rdbtnNotCombine.setEnabled(true);
			}

			if (this.workflowType != MetaLabWorkflowType.MaxQuantWorkflow) {
				rdbtnLabelFree.setSelected(true);
				rdbtnIsotopicLabeling.setEnabled(false);
				rdbtnIsobaricLabeling.setEnabled(false);

				metaLabIsotopicQuanPanel.setEnabled(false);
				metaLabIsobaricQuanPanel.setEnabled(false);
			}
		}

		metaLabIsotopicQuanPanel.setBorder(new TitledBorder(null, MetaConstants.isotopicLabel, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		quantPanel.add(metaLabIsotopicQuanPanel, "cell 0 2 7 1,grow");

		metaLabIsobaricQuanPanel.setBorder(new TitledBorder(null, MetaConstants.isobaricLabel, TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		quantPanel.add(metaLabIsobaricQuanPanel, "cell 0 3 7 1,grow");
	}

	protected void initialSettingPanel() {
		settingPanel = new JPanel();
		tabbedPane.addTab("Setting",
				new ImageIcon(
						MetaLabPFindMainPanel.class.getResource("/toolbarButtonGraphics/general/Properties16.gif")),
				settingPanel, null);
		settingPanel.setLayout(new MigLayout("", "[200:400:450,grow][200:400:450,grow][400:800:900,grow]",
				"[50:50][50:50][10][320][240][:280:400,grow]"));

		if (workflowType == MetaLabWorkflowType.TaxonomyAnalysis) {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;

			metaLabTaxaPanel = new MetaLabTaxaPanel(parV2.getPtp());
			metaLabTaxaPanel.setBorder(new TitledBorder(null, "Peptide to taxonomy analysis settings",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			settingPanel.add(metaLabTaxaPanel, "cell 0 0 1 3,grow");

		} else {

			perPanel = new JPanel();
			perPanel.setBorder(
					new TitledBorder(null, "Performance setting", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			settingPanel.add(perPanel, "cell 0 0,grow");
			perPanel.setLayout(new MigLayout("", "[100:100:100][20:40:160,grow]", "[40]"));

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
			instruPanel.setLayout(new MigLayout("", "[60:60:60][10:10:10][40:150:180][20]", "[40]"));

			JLabel lblMsReso = new JLabel("MS2 scan mode");
			instruPanel.add(lblMsReso, "cell 0 0");

			ms2ComboBox = new JComboBox<String>();
			instruPanel.add(ms2ComboBox, "cell 2 0,growx");

			JPanel metaPanel = new JPanel();
			metaPanel.setBorder(new TitledBorder(null, "Metaproteomics setting", TitledBorder.LEADING, TitledBorder.TOP,
					null, null));
			settingPanel.add(metaPanel, "cell 0 1 2 1,grow");
			metaPanel.setLayout(new MigLayout("", "[120][20][50][][50]", "[40]"));

			JLabel metaLabel = new JLabel("Perform taxonomy analysis and functional annotation");
			metaPanel.add(metaLabel, "cell 0 0");

			rdbtnMetaSearchYes = new JRadioButton("Yes");
			metaButtonGroup.add(rdbtnMetaSearchYes);
			rdbtnMetaSearchYes.setSelected(par.isMetaWorkflow());
			metaPanel.add(rdbtnMetaSearchYes, "cell 2 0");
			rdbtnMetaSearchYes.addActionListener(l -> {
				if (metaLabTaxaPanel != null) {
					metaLabTaxaPanel.setEnabled(rdbtnMetaSearchYes.isSelected());
				}
			});

			JRadioButton rdbtnMetaSearchNo = new JRadioButton("No");
			metaButtonGroup.add(rdbtnMetaSearchNo);
			rdbtnMetaSearchNo.setSelected(!par.isMetaWorkflow());
			metaPanel.add(rdbtnMetaSearchNo, "cell 3 0");
			rdbtnMetaSearchNo.addActionListener(l -> {
				if (metaLabTaxaPanel != null) {
					metaLabTaxaPanel.setEnabled(!rdbtnMetaSearchNo.isSelected());
				}
			});

			switch (workflowType) {
			case MaxQuantWorkflow: {

				ms2ComboBox.addItem(MetaConstants.FTMS);
				ms2ComboBox.addItem(MetaConstants.ITMS);

				String ms2ScanMode = par.getMs2ScanMode();
				if (ms2ScanMode.equals(MetaConstants.FTMS)) {
					ms2ComboBox.setSelectedIndex(0);
				} else {
					ms2ComboBox.setSelectedIndex(1);
				}
				rdbtnMetaSearchYes.setSelected(par.isMetaWorkflow());

				MetaParameterMQ parV2 = (MetaParameterMQ) par;

				metaLabSsdbPanel = new MetaLabSsdbPanel(parV2.getMscp());
				metaLabSsdbPanel.setBorder(new TitledBorder(null, "Sample-specific database generation settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabSsdbPanel, "cell 0 3 2 1,grow");

				metaLabClosedPanel = new MetaLabMaxQuantPanel(parV2.getCsp());
				metaLabClosedPanel.setBorder(new TitledBorder(null, "MaxQuant search settings", TitledBorder.LEADING,
						TitledBorder.TOP, null, null));
				settingPanel.add(metaLabClosedPanel, "cell 0 4 2 2,grow");

				metaLabTaxaPanel = new MetaLabTaxaPanel(parV2.getPtp());
				metaLabTaxaPanel.setBorder(new TitledBorder(null, "Peptide to taxonomy analysis settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabTaxaPanel, "cell 2 0 1 4,grow");

				metaLabMetaPanel = new MetaLabMetaPanel(par.getMetadata(), false);
				metaLabMetaPanel.setBorder(new TitledBorder(null, "Metadata settings", TitledBorder.LEADING,
						TitledBorder.TOP, null, null));
				settingPanel.add(metaLabMetaPanel, "cell 2 4 1 2,grow");

				break;
			}
			case MSFraggerWorkflow: {

				ms2ComboBox.addItem(MetaConstants.FTMS);
				ms2ComboBox.addItem(MetaConstants.ITMS);

				String ms2ScanMode = par.getMs2ScanMode();
				if (ms2ScanMode.equals(MetaConstants.FTMS)) {
					ms2ComboBox.setSelectedIndex(0);
				} else {
					ms2ComboBox.setSelectedIndex(1);
				}
				rdbtnMetaSearchYes.setSelected(par.isMetaWorkflow());

				MetaParameterMQ parV2 = (MetaParameterMQ) par;

				metaLabSsdbPanel = new MetaLabSsdbPanel(parV2.getMscp());
				metaLabSsdbPanel.setBorder(new TitledBorder(null, "Sample-specific database generation settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabSsdbPanel, "cell 0 3 2 1,grow");

				metaLabMSFraggerPanel = new MetaLabMSFraggerPanel(parV2.getOsp());
				metaLabMSFraggerPanel.setBorder(new TitledBorder(null, "MSFragger search settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabMSFraggerPanel, "cell 0 4 2 2,grow");

				metaLabTaxaPanel = new MetaLabTaxaPanel(parV2.getPtp());
				metaLabTaxaPanel.setBorder(new TitledBorder(null, "Peptide to taxonomy analysis settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabTaxaPanel, "cell 2 0 1 4,grow");

				metaLabMetaPanel = new MetaLabMetaPanel(par.getMetadata(), false);
				metaLabMetaPanel.setBorder(new TitledBorder(null, "Metadata settings", TitledBorder.LEADING,
						TitledBorder.TOP, null, null));
				settingPanel.add(metaLabMetaPanel, "cell 2 4 1 2,grow");

				break;
			}
			case pFindWorkflow: {

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
				rdbtnMetaSearchYes.setSelected(par.isMetaWorkflow());

				MetaParameterPFind parV3 = (MetaParameterPFind) par;

				metaLabSsdbPanel = new MetaLabSsdbPanel(parV3.getMscp(), true);
				metaLabSsdbPanel.setBorder(new TitledBorder(null, "Sample-specific database generation settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabSsdbPanel, "cell 0 3 2 1,grow");

				metaLabPFindPanel = new MetaLabPFindPanel(parV3.getMop());
				metaLabPFindPanel.setBorder(new TitledBorder(null, "pFind search settings", TitledBorder.LEADING,
						TitledBorder.TOP, null, null));
				settingPanel.add(metaLabPFindPanel, "cell 0 4 2 2,grow");

				metaLabTaxaPanel = new MetaLabTaxaPanel(parV3.getPtp());
				metaLabTaxaPanel.setBorder(new TitledBorder(null, "Peptide to taxonomy analysis settings",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				settingPanel.add(metaLabTaxaPanel, "cell 2 0 1 4,grow");

				metaLabMetaPanel = new MetaLabMetaPanel(parV3.getMetadata(), parV3.getMetadata().isIsobaricQuan());

				metaLabMetaPanel.setBorder(new TitledBorder(null, "Metadata settings", TitledBorder.LEADING,
						TitledBorder.TOP, null, null));
				settingPanel.add(metaLabMetaPanel, "cell 2 4 1 2,grow");

				break;
			}
			default: {
				break;
			}
			}
		}
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
/*
			LicenseVerifier verifier = new LicenseVerifier();
			boolean verified = verifier.verify();
			if (!verified) {

				StringBuilder sb = new StringBuilder();
				sb.append("Thank you for using MetaLab. A license is needed to run MetaLab, ");
				sb.append("please go to Tools -> Activation for the license.").append("\n");
				ArrayList<String> infoList = verifier.getVerifyInfo();
				for (String info : infoList) {
					sb.append("\n").append(info);
				}

				JOptionPane.showMessageDialog(this, sb.toString(), "Warning", JOptionPane.WARNING_MESSAGE);

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

			System.out.println("------Welcome to MetaLab " + MetaParaIOMQ.version + "------");

			btnStart_1.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			statusPanel.start();

			if (workflowType == MetaLabWorkflowType.pFindWorkflow) {

				task = new MetaLabMagTask((MetaParameterPFind) par, msv, progressBar, progressBar_1) {

					public void done() {

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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
						MetaLabPFindMainPanel.this.repaint();

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

								int reply = JOptionPane.showConfirmDialog(MetaLabPFindMainPanel.this,
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
								JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
										JOptionPane.ERROR_MESSAGE);
							}

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);

						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
			} else {

				task = new MetaLabTaskV2((MetaParameterMQ) par, msv, progressBar, progressBar_1) {

					public void done() {

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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
						MetaLabPFindMainPanel.this.repaint();

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

								int reply = JOptionPane.showConfirmDialog(MetaLabPFindMainPanel.this,
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
								JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
										JOptionPane.ERROR_MESSAGE);
							}

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);

						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this, helpInfo, "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				};
			}

			try {

				statusPanel.start();

				btnStop.setEnabled(true);

				task.execute();

			} catch (Exception e) {

				JOptionPane.showMessageDialog(MetaLabPFindMainPanel.this,
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

		this.statusPanel = new StatusPanel("Version: " + MetaParaIOMQ.version);
		consolePanel.add(statusPanel, "cell 0 3 3 1");
	}

	protected void update1() {
		MetaLabWorkflowType workflowType = par.getWorkflowType();
		switch (workflowType) {
		case pFindWorkflow: {
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
			parV3.setMetadata(metadata);

			break;
		}
		case MSFraggerWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			String result = inputOutputPanel.getResultFile();
			parV2.setResult(result);

			String microdb = dbPanel.getMicroDb();
			String hostdb = dbPanel.getHostDB();
			boolean isappend = dbPanel.isAppendHostDb();

			parV2.setMicroDb(microdb);
			parV2.setHostDB(hostdb);
			parV2.setAppendHostDb(isappend);

			MetaData metadata = inputOutputPanel.getMetaData();
			parV2.setMetadata(metadata);

			break;
		}
		case MaxQuantWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;

			String result = inputOutputPanel.getResultFile();
			parV2.setResult(result);

			String microdb = dbPanel.getMicroDb();
			String hostdb = dbPanel.getHostDB();
			boolean isappend = dbPanel.isAppendHostDb();

			parV2.setMicroDb(microdb);
			parV2.setHostDB(hostdb);
			parV2.setAppendHostDb(isappend);

			MetaData metadata = inputOutputPanel.getMetaData();
			parV2.setMetadata(metadata);

			break;
		}
		default:
			break;
		}
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
		} else if (rdbtnIsotopicLabeling.isSelected()) {
			boolean has = false;
			MaxquantModification[][] labels = metaLabIsotopicQuanPanel.getLabels();
			for (int i = 0; i < labels.length; i++) {
				for (int j = 0; j < labels[i].length; j++) {
					if (labels[i][j] != null) {
						has = true;
					}
				}
			}

			if (!has) {
				JOptionPane.showMessageDialog(this,
						"Please choose the isotopic labeling method when \"Isotopic labeling\" "
								+ "has been selected in the \"Quantification mode.\"",
						"Warning", JOptionPane.WARNING_MESSAGE);

				tabbedPane.setSelectedIndex(1);

				return;
			}
		}

		MetaLabWorkflowType workflowType = par.getWorkflowType();
		switch (workflowType) {
		case pFindWorkflow: {
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

			metaLabMetaPanel.update(parV3.getMetadata());
			break;
		}

		case MSFraggerWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			MaxquantModification[] fixMods = metaLabModPanel.getFixMods();
			MaxquantModification[] variMods = metaLabModPanel.getVariMods();

			parV2.setFixMods(fixMods);
			parV2.setVariMods(variMods);

			Enzyme enzyme = metaLabEnzymePanel.getEnzyme();
			int digestMode = metaLabEnzymePanel.getDigestMode();
			int miss = metaLabEnzymePanel.getMissCleavages();

			parV2.setEnzyme(enzyme);
			parV2.setDigestMode(digestMode);
			parV2.setMissCleavages(miss);

			if (rdbtnCombine.isSelected()) {
				parV2.setCombineLabel(true);
			} else {
				parV2.setCombineLabel(false);
			}

			if (rdbtnLabelFree.isSelected()) {
				parV2.setQuanMode(MetaConstants.labelFree);
			}

			metaLabMetaPanel.update(parV2.getMetadata());

			break;
		}
		case MaxQuantWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			MaxquantModification[] fixMods = metaLabModPanel.getFixMods();
			MaxquantModification[] variMods = metaLabModPanel.getVariMods();

			parV2.setFixMods(fixMods);
			parV2.setVariMods(variMods);

			Enzyme enzyme = metaLabEnzymePanel.getEnzyme();
			int digestMode = metaLabEnzymePanel.getDigestMode();
			int miss = metaLabEnzymePanel.getMissCleavages();

			parV2.setEnzyme(enzyme);
			parV2.setDigestMode(digestMode);
			parV2.setMissCleavages(miss);

			if (rdbtnCombine.isSelected()) {
				parV2.setCombineLabel(true);
			} else {
				parV2.setCombineLabel(false);
			}

			if (rdbtnLabelFree.isSelected()) {
				parV2.setQuanMode(MetaConstants.labelFree);
				parV2.setLabels(new MaxquantModification[][] {});
				parV2.setIsobaric(new MaxquantModification[] {});
				parV2.getMetadata().setLabelTitle(new String[] {});
				parV2.setIsoCorFactor(new double[][] {});
				parV2.setIsobaricTag(null);
			}

			if (rdbtnIsotopicLabeling.isSelected()) {
				parV2.setQuanMode(MetaConstants.isotopicLabel);
				MaxquantModification[][] labels = metaLabIsotopicQuanPanel.getLabels();
				parV2.setLabels(labels);
				parV2.getMetadata().setLabelTitle(metaLabIsotopicQuanPanel.getLabelTitle());
			}

			if (rdbtnIsobaricLabeling.isSelected()) {
				parV2.setQuanMode(MetaConstants.isobaricLabel);
				MaxquantModification[] isobaric = metaLabIsobaricQuanPanel.getIsobaricTags();
				parV2.setIsobaric(isobaric);

				double[][] factor = metaLabIsobaricQuanPanel.getIsoCorFactor();
				parV2.setIsoCorFactor(factor);

				IsobaricTag tag = metaLabIsobaricQuanPanel.getIsobaricTag();
				parV2.setIsobaricTag(tag);

				parV2.getMetadata().setLabelTitle(metaLabIsobaricQuanPanel.getLabelTitle());
			}

			metaLabMetaPanel.update(parV2.getMetadata());

			break;
		}
		default:
			break;
		}
	}

	protected void update3() {

		MetaLabWorkflowType workflowType = par.getWorkflowType();
		switch (workflowType) {
		case pFindWorkflow: {

			MetaParameterPFind parV3 = (MetaParameterPFind) par;

			int threadCount = (int) this.threadComboBox.getSelectedItem();
			parV3.setThreadCount(threadCount);

			String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
			parV3.setMs2ScanMode(ms2ScanMode);

			boolean isMetaWorkflow = this.rdbtnMetaSearchYes.isSelected();
			parV3.setMetaWorkflow(isMetaWorkflow);

			parV3.setMscp(this.metaLabSsdbPanel.getSsdbPar());
			parV3.setMop(this.metaLabPFindPanel.getOpenPar());
			parV3.setPtp(this.metaLabTaxaPanel.getPep2TaxaPar());

			metaLabMetaPanel.update();

			break;
		}

		case MSFraggerWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;

			int threadCount = (int) this.threadComboBox.getSelectedItem();
			parV2.setThreadCount(threadCount);

			String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
			parV2.setMs2ScanMode(ms2ScanMode);

			boolean isMetaWorkflow = this.rdbtnMetaSearchYes.isSelected();
			parV2.setMetaWorkflow(isMetaWorkflow);

			parV2.setMscp(this.metaLabSsdbPanel.getSsdbPar());
			parV2.setOsp(this.metaLabMSFraggerPanel.getOpenPar());
			parV2.setPtp(this.metaLabTaxaPanel.getPep2TaxaPar());

			metaLabMetaPanel.update(parV2.getMetadata());

			break;
		}
		case MaxQuantWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;

			int threadCount = (int) this.threadComboBox.getSelectedItem();
			parV2.setThreadCount(threadCount);

			String ms2ScanMode = this.ms2ComboBox.getSelectedItem().toString();
			parV2.setMs2ScanMode(ms2ScanMode);

			boolean isMetaWorkflow = this.rdbtnMetaSearchYes.isSelected();
			parV2.setMetaWorkflow(isMetaWorkflow);

			parV2.setMscp(this.metaLabSsdbPanel.getSsdbPar());
			parV2.setCsp(this.metaLabClosedPanel.getClosedPar());
			parV2.setPtp(this.metaLabTaxaPanel.getPep2TaxaPar());

			metaLabMetaPanel.update();

			break;
		}
		case TaxonomyAnalysis: {
			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			parV2.setPeps(pepPanel.getMetaPeptides());
			parV2.setIntensityTitles(pepPanel.getIntensityTitle());
			parV2.setQuanMode(pepPanel.getQuanMode());
			parV2.setQuanResultType(pepPanel.getQuanResultType());
			parV2.setQuickOutput(pepPanel.getOutput());
			parV2.setResult(pepPanel.getOutput());

			parV2.setPtp(this.metaLabTaxaPanel.getPep2TaxaPar());

			break;
		}

		case FunctionalAnnotation: {
			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			parV2.setPros(proPanel.getMetaProteins());
			parV2.setIntensityTitles(proPanel.getIntensityTitle());
			parV2.setQuanMode(proPanel.getQuanMode());
			parV2.setQuanResultType(proPanel.getQuanResultType());
			parV2.setQuickOutput(proPanel.getOutput());
			parV2.setResult(proPanel.getOutput());

			break;
		}

		default:
			break;
		}

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

	/**
	 * This method is only used in MetaLabMainFrameV21, for the export of the
	 * parameter file
	 */
	public void updateParameter() {
		MetaLabWorkflowType workflowType = par.getWorkflowType();
		switch (workflowType) {
		case TaxonomyAnalysis: {

			break;
		}

		case FunctionalAnnotation: {

			break;
		}

		default:
			int selectedIndex = tabbedPane.getSelectedIndex();
			switch (selectedIndex) {
			case 0:
				update1();
				break;
			case 1:
				update2();
				break;
			case 2:
				update3();
				break;
			default:
				break;
			}

			break;
		}
	}

	public MetaParameter getParameter() {
		return par;
	}

	protected String[] checkParameter() {

		ArrayList<String> list = new ArrayList<String>();

		MetaLabWorkflowType workflowType = par.getWorkflowType();
		switch (workflowType) {
		case MaxQuantWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			MetaSsdbCreatePar mscp = parV2.getMscp();
			MetaPep2TaxaPar ptp = parV2.getPtp();

			if (!msv.findPT()) {
				list.add("ProteomicsTools is not found, please check the Setting -> Resource.");
			}
			if (mscp.isSsdb()) {
				if (!msv.findXTandem()) {
					list.add("X!Tandem is not found, please check the Setting -> Resource.");
				}
			}
			if (!msv.findMaxQuant()) {
				list.add("MaxQuant is not found, please check the Setting -> Resource.");
			}
			if (parV2.isMetaWorkflow() && ptp.isBuildIn()) {
				if (!msv.findPep2tax()) {
					list.add("Peptide and taxa mapping database is not found, please check the Setting -> Resource.");
				}
				if (!msv.findTaxonAll()) {
					list.add("Taxonomic tree file is not found, please check the Setting -> Resource.");
				}
			}

			if (parV2.getMetadata() == null || parV2.getMetadata().getRawFiles().length == 0) {
				list.add("Raw files are not found.");
			}

			String[] rawfiles = parV2.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}

			if (parV2.getMicroDb() == null || parV2.getMicroDb().length() == 0) {
				list.add("Microbiome database is not found");
			} else {
				File dbfile = new File(parV2.getMicroDb());
				if (!dbfile.exists()) {
					list.add("Microbiome database " + dbfile + " is not found.");
				}
			}

			if (parV2.isAppendHostDb()) {
				if (parV2.getHostDB() == null || parV2.getHostDB().length() == 0) {
					list.add("Host database is not found");
				} else {
					File dbfile = new File(parV2.getHostDB());
					if (!dbfile.exists()) {
						list.add("Host database " + dbfile + " is not found.");
					}
				}
			}

			if (parV2.getResult() == null || parV2.getResult().length() == 0) {
				list.add("Result file is not found");
			}

			if (parV2.getEnzyme() == null) {
				list.add("Enzyme is not set");
			}

			break;
		}
		case MSFraggerWorkflow: {

			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			MetaSsdbCreatePar mscp = parV2.getMscp();
			MetaPep2TaxaPar ptp = parV2.getPtp();

			if (!msv.findPT()) {
				list.add("ProteomicsTools is not found, please check the Setting -> Resource.");
			}
			if (mscp.isSsdb()) {
				if (!msv.findXTandem()) {
					list.add("X!Tandem is not found, please check the Setting -> Resource.");
				}
			}
			if (!msv.findMSFragger()) {
				list.add("MSFragger is not found, please check the Setting -> Resource.");
			}
			if (parV2.isMetaWorkflow() && ptp.isBuildIn()) {
				if (!msv.findPep2tax()) {
					list.add("Peptide and taxa mapping database is not found, please check the Setting -> Resource.");
				}
				if (!msv.findTaxonAll()) {
					list.add("Taxonomic tree file is not found, please check the Setting -> Resource.");
				}
			}

			if (parV2.getMetadata() == null || parV2.getMetadata().getRawFiles().length == 0) {
				list.add("Raw files are not found.");
			}

			String[] rawfiles = parV2.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}

			if (parV2.getMicroDb() == null || parV2.getMicroDb().length() == 0) {
				list.add("Microbiome database is not found");
			} else {
				File dbfile = new File(parV2.getMicroDb());
				if (!dbfile.exists()) {
					list.add("Microbiome database " + dbfile + " is not found.");
				}
			}

			if (parV2.isAppendHostDb()) {
				if (parV2.getHostDB() == null || parV2.getHostDB().length() == 0) {
					list.add("Host database is not found");
				} else {
					File dbfile = new File(parV2.getHostDB());
					if (!dbfile.exists()) {
						list.add("Host database " + dbfile + " is not found.");
					}
				}
			}

			if (parV2.getResult() == null || parV2.getResult().length() == 0) {
				list.add("Result file is not found");
			}

			if (parV2.getEnzyme() == null) {
				list.add("Enzyme is not set");
			}

			String metaCheck = parV2.getMetadata().checkFlashLFQExpDesign()[0];

			if (metaCheck.length() > 0) {
				list.add(metaCheck);
			}

			break;
		}

		case pFindWorkflow: {
			MetaParameterPFind parV3 = (MetaParameterPFind) par;
			MetaPep2TaxaPar ptp = parV3.getPtp();

			if (!msv.findPFind()) {
				list.add("pFind is not found, please check the Setting -> Resource.");
			}

			if (parV3.isMetaWorkflow() && ptp.isBuildIn()) {
				File microDbFile = new File(parV3.getMicroDb());
				if (microDbFile.exists() && microDbFile.isDirectory()) {
					File catelogFile = new File(microDbFile, "catalog.db");
					if (!catelogFile.exists()) {
						list.add("Gene catelog database is not found");
					}
				} else {
					if (!msv.findPep2tax()) {
						list.add(
								"Peptide and taxa mapping database is not found, please check the Setting -> Resource.");
					}
				}

				if (!msv.findTaxonAll()) {
					list.add("Taxonomic tree file is not found, please check the Setting -> Resource.");
				}
			}

			if (parV3.getMetadata() == null || parV3.getMetadata().getRawFiles().length == 0) {
				list.add("Raw files are not found.");
			}

			String[] rawfiles = parV3.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}

			if (parV3.getMicroDb() == null || parV3.getMicroDb().length() == 0) {
				list.add("Microbiome database is not found");
			} else {
				File dbfile = new File(parV3.getMicroDb());
				if (!dbfile.exists()) {
					list.add("Microbiome database " + dbfile + " is not found.");
				}
			}

			if (parV3.isAppendHostDb()) {
				if (parV3.getHostDB() == null || parV3.getHostDB().length() == 0) {
					File dbfile = new File(parV3.getMicroDb());
					if (dbfile.isFile()) {
						list.add("Host database is not found");
					} else {
						if (!dbfile.exists()) {
							list.add("Host database is not found");
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
				list.add("Result file is not found");
			}

			if (parV3.getEnzyme() == null || parV3.getEnzyme().length() == 0) {
				list.add("Enzyme is not set");
			}

			String metaCheck = parV3.getMetadata().checkFlashLFQExpDesign()[0];

			if (metaCheck.length() > 0) {
				list.add(metaCheck);
			}

			break;
		}
		case TaxonomyAnalysis: {
			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			MetaPep2TaxaPar ptp = parV2.getPtp();
			if (ptp.isBuildIn()) {
				if (!msv.findPep2tax()) {
					list.add("Peptide and taxa mapping database is not found, please check the Setting -> Resource.");
				}
				if (!msv.findTaxonAll()) {
					list.add("Taxonomic tree file is not found, please check the Setting -> Resource.");
				}
			}
			MetaPeptide[] peps = parV2.getPeps();
			if (peps == null) {
				list.add("No peptides were loaded.");
			} else {
				String[] titles = parV2.getIntensityTitles();
				if (titles == null) {
					list.add("The experimental names were unknown.");
				}
			}

			break;
		}

		case FunctionalAnnotation: {
			MetaParameterMQ parV2 = (MetaParameterMQ) par;
			if (!msv.findFunction()) {
				list.add("Functional database directory is not found, please check the Setting -> Resource.");
			}

			MetaProtein[] pros = parV2.getPros();
			if (pros == null) {
				list.add("No protein groups were loaded.");
			} else {
				String[] titles = parV2.getIntensityTitles();
				if (titles == null) {
					list.add("The experimental names were unknown.");
				}
			}

			break;
		}

		default:
			break;
		}

		String[] warnings = list.toArray(new String[list.size()]);

		return warnings;
	}
	
	protected void setComponentsEnabled(Container container, boolean enabled) {
		container.setEnabled(enabled);
		for (Component component : container.getComponents()) {
			if (component instanceof JTable) {
				JTable table = (JTable) component;
				table.setEnabled(enabled);
				table.getTableHeader().setEnabled(enabled);
				table.setForeground(enabled ? Color.BLACK : Color.GRAY);
				table.getTableHeader().setForeground(enabled ? Color.BLACK : Color.GRAY);
			} else {
				component.setEnabled(enabled);
			}
			
			if (component instanceof Container) {
				setComponentsEnabled((Container) component, enabled);
			}
		}
	}
	
	protected int ProcessorCount() {
		int total = 0;
		try {
			String[] args = { "wmic", "cpu", "get", "NumberOfLogicalProcessors" };
			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = pb.start();
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr = null;
			L: while ((lineStr = inBr.readLine()) != null) {
				String trimLine = lineStr.trim();
				if (trimLine.length() > 0) {
					int oneCore = 0;
					for (int i = 0; i < trimLine.length(); i++) {
						char aa = trimLine.charAt(i);
						if (aa >= '0' && aa <= '9') {
							oneCore += ((int) (aa - '0')) * Math.pow(10, (trimLine.length() - i - 1));
						} else {
							continue L;
						}
					}
					total += oneCore;
				}
			}
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					System.err.println("false");
			}
			inBr.close();
			in.close();
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to get logical processors using wmic: " + e.getMessage());
		}

		if (total == 0) {
			total = Runtime.getRuntime().availableProcessors();
		}
		return total;
	}
}
