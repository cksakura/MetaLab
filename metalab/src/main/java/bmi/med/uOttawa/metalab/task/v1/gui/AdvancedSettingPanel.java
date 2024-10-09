/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;

/**
 * @author Kai Cheng
 *
 */
public class AdvancedSettingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1161708898423037367L;
	private JTextField resourceTextField;
	private JComboBox<String> comboBoxReso;
	private String[] instru = { "HCD in Orbitrap fusion/Orbitrap Elite/Q Exactive", "CID in Orbitrap" };
	private JTextField instruTextField;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton resourceBtnBrowse;
	private JCheckBox resourceCheckBxDefault;
	private JCheckBox chckbxEukaryota;
	private JCheckBox chckbxBacteria;
	private JCheckBox chckbxArchaea;
	private JCheckBox chckbxViruses;
	private JCheckBox chckbxViroids;
	private JCheckBox chckbxUnclassified;
	private JCheckBox chckbxOther;
	private JComboBox<TaxonomyRanks> rankComboBox;
	private JCheckBox chckbxEnviomentalSamples;
	private JComboBox<Integer> threadComboBox;
	private WorkflowPanel2 workflowPanel2;
	private JRadioButton rdbtnClusterYes;
	private JRadioButton rdbtnClusterNo;
	private JCheckBox chckbxBuiltinDb;
	private JCheckBox chckbxUnipept;
	private JComboBox<String> funcComboBox;

	/**
	 * Create the panel.
	 */
	public AdvancedSettingPanel() {

		int threadCount = 1;
		int instruType = 1;
		String pep2taxa = "";
		boolean useBuild = true;
		boolean useUnipept = true;
		boolean clusterMS2 = true;
		boolean[] workflow = new boolean[] { true, true, true, true };
		int sampleid = 1;
		HashSet<RootType> usedTypes = new HashSet<RootType>();

		setBorder(new TitledBorder(null, "Advanced Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[150,grow 180][10,grow 20][500,grow 1000]",
				"[150,grow][150,grow][250,grow 300][150,grow 200]"));

		WorkflowPanel2 workflowPanel2 = new WorkflowPanel2();
		add(workflowPanel2, "cell 0 0 1 4,grow");

		JPanel processPanel = new JPanel();
		processPanel.setBorder(
				new TitledBorder(null, "Processing settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(processPanel, "cell 2 0,grow");
		processPanel.setLayout(
				new MigLayout("", "[50,grow][30][50,grow][300,grow 500][][30,grow]", "[30,grow 50][30,grow 50]"));

		JLabel lblNumberOfThreads = new JLabel("Number of threads");
		processPanel.add(lblNumberOfThreads, "cell 0 0");

		JComboBox<Integer> threadComboBox = new JComboBox<Integer>();
		processPanel.add(threadComboBox, "cell 2 0,growx");

		JLabel lblResource = new JLabel("Resource");
		processPanel.add(lblResource, "cell 0 1");

		resourceTextField = new JTextField();
		processPanel.add(resourceTextField, "cell 2 1 2 1,growx");
		resourceTextField.setColumns(10);

		JButton resourceBtnBrowse = new JButton("Browse");
		resourceBtnBrowse.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(resourceTextField.getText());
			int returnValue = fileChooser.showOpenDialog(AdvancedSettingPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				resourceTextField.setText(file.getAbsolutePath());
			}
		});
		processPanel.add(resourceBtnBrowse, "cell 4 1");

		int totalCount = Runtime.getRuntime().availableProcessors();
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
			threadComboBox.addItem(i);
		}

		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}

		JPanel step12Panel = new JPanel();
		step12Panel.setBorder(new TitledBorder(null, "Step 1 & 2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step12Panel.setBackground(new Color(135, 206, 255));
		add(step12Panel, "cell 2 1,grow");
		step12Panel.setLayout(new MigLayout("", "[135][30][100][30][360]", "[40,grow 60][40,grow 60]"));

		JLabel lblInstruReso = new JLabel("Instrument resolution");
		step12Panel.add(lblInstruReso, "cell 0 1");

		this.comboBoxReso = new JComboBox<String>();
		step12Panel.add(comboBoxReso, "cell 2 1,growx");

		instruTextField = new JTextField(instru[0]);
		step12Panel.add(instruTextField, "cell 4 1,growx");
		instruTextField.setEnabled(false);
		instruTextField.setEditable(false);
		instruTextField.setColumns(10);

		comboBoxReso.addItem("High-High");
		comboBoxReso.addItem("High-Low");
		comboBoxReso.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				int id = comboBoxReso.getSelectedIndex();
				if (id == 0) {
					instruTextField.setText(instru[0]);
				} else if (id == 1) {
					instruTextField.setText(instru[1]);
				}
			}
		});

		if (instruType >= 0 && instruType < 2) {
			comboBoxReso.setSelectedIndex(instruType);
		}

		if (!(workflow[0] || workflow[1])) {
			lblInstruReso.setEnabled(false);
			comboBoxReso.setEnabled(false);
		}

		JLabel lblClustering = new JLabel("Clustering MS/MS in step 1");
		step12Panel.add(lblClustering, "cell 0 0");

		JRadioButton rdbtnClusterYes = new JRadioButton("Yes");
		rdbtnClusterYes.setSelected(true);
		rdbtnClusterYes.setBackground(new Color(135, 206, 255));
		step12Panel.add(rdbtnClusterYes, "flowx,cell 2 0");

		buttonGroup.add(rdbtnClusterYes);
		JRadioButton rdbtnClusterNo = new JRadioButton("No");
		rdbtnClusterNo.setBackground(new Color(135, 206, 255));
		step12Panel.add(rdbtnClusterNo, "cell 2 0");
		buttonGroup.add(rdbtnClusterNo);
		if (clusterMS2) {
			rdbtnClusterYes.setSelected(true);
		} else {
			rdbtnClusterNo.setSelected(true);
		}

		if (!workflow[0]) {
			lblClustering.setEnabled(false);
			rdbtnClusterYes.setEnabled(false);
			rdbtnClusterNo.setEnabled(false);
		}

		JPanel step3Panel = new JPanel();
		step3Panel.setBorder(new TitledBorder(null, "Step 3", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step3Panel.setBackground(new Color(108, 166, 205));
		add(step3Panel, "cell 2 2,grow");
		step3Panel.setLayout(new MigLayout("", "[150,grow 200][30][300,grow 500][30,grow]",
				"[30,grow 50][30,grow 50][30,grow 50][30,grow 50]"));

		JLabel lblBy = new JLabel("Taxonomy analysis by");
		step3Panel.add(lblBy, "cell 0 0");

		JPanel lcaPanel = new JPanel();
		lcaPanel.setBorder(
				new TitledBorder(null, "LCA calculation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step3Panel.add(lcaPanel, "cell 2 0 1 4,grow");
		lcaPanel.setLayout(new MigLayout("", "[]", "[50,grow][50,grow][50,grow][50,grow]"));

		JLabel lblTaxaFromWhich = new JLabel("Taxa from which top nodes showed in result");
		lcaPanel.add(lblTaxaFromWhich, "cell 0 0");

		JCheckBox chckbxEukaryota = new JCheckBox("Eukaryota");
		chckbxEukaryota.setSelected(usedTypes.contains(RootType.Eukaryota));
		lcaPanel.add(chckbxEukaryota, "cell 1 0");

		JCheckBox chckbxBacteria = new JCheckBox("Bacteria");
		chckbxBacteria.setSelected(usedTypes.contains(RootType.Bacteria));
		lcaPanel.add(chckbxBacteria, "cell 2 0");

		JCheckBox chckbxArchaea = new JCheckBox("Archaea");
		chckbxArchaea.setSelected(usedTypes.contains(RootType.Archaea));
		lcaPanel.add(chckbxArchaea, "cell 3 0");

		JCheckBox chckbxViruses = new JCheckBox("Viruses");
		chckbxViruses.setSelected(usedTypes.contains(RootType.Viruses));
		lcaPanel.add(chckbxViruses, "cell 4 0");

		JCheckBox chckbxViroids = new JCheckBox("Viroids");
		chckbxViroids.setSelected(usedTypes.contains(RootType.Viroids));
		lcaPanel.add(chckbxViroids, "cell 1 1");

		JCheckBox chckbxUnclassified = new JCheckBox("unclassified sequences");
		chckbxUnclassified.setSelected(usedTypes.contains(RootType.unclassified));
		lcaPanel.add(chckbxUnclassified, "cell 2 1");

		JCheckBox chckbxOther = new JCheckBox("other sequences");
		chckbxOther.setSelected(usedTypes.contains(RootType.other));
		lcaPanel.add(chckbxOther, "cell 3 1");

		JLabel lblIgnoreBlanksBelow = new JLabel("Ignore blanks below rank");
		lcaPanel.add(lblIgnoreBlanksBelow, "cell 0 2");

		JComboBox<TaxonomyRanks> rankComboBox = new JComboBox<TaxonomyRanks>();
		lcaPanel.add(rankComboBox, "cell 1 2,growx");
		rankComboBox.addItem(TaxonomyRanks.Superkingdom);
		rankComboBox.addItem(TaxonomyRanks.Phylum);
		rankComboBox.addItem(TaxonomyRanks.Class);
		rankComboBox.addItem(TaxonomyRanks.Order);
		rankComboBox.addItem(TaxonomyRanks.Family);
		rankComboBox.addItem(TaxonomyRanks.Genus);
		rankComboBox.addItem(TaxonomyRanks.Species);
		rankComboBox.setSelectedItem(TaxonomyRanks.Superkingdom);

		JLabel lblIgnoreTaxaBelow = new JLabel("Ignore taxa below parents");
		lcaPanel.add(lblIgnoreTaxaBelow, "cell 0 3");

		JCheckBox chckbxEnviomentalSamples = new JCheckBox("environmental samples");
		lcaPanel.add(chckbxEnviomentalSamples, "cell 1 3");

		JButton btnDefault = new JButton("Restore defaults");
		lcaPanel.add(btnDefault, "cell 3 3 2 1,grow");

		JCheckBox chckbxBuiltinDb = new JCheckBox("Built-in database");
		chckbxBuiltinDb.setBackground(new Color(108, 166, 205));
		step3Panel.add(chckbxBuiltinDb, "cell 0 1");

		JCheckBox chckbxUnipept = new JCheckBox("Unipept (http://unipept.ugent.be/)");
		chckbxUnipept.setBackground(new Color(108, 166, 205));
		step3Panel.add(chckbxUnipept, "cell 0 2");

		JPanel step4Panel = new JPanel();
		step4Panel.setBorder(new TitledBorder(null, "Step 4", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step4Panel.setBackground(new Color(108, 166, 205));
		add(step4Panel, "cell 2 3,grow");
		step4Panel.setLayout(new MigLayout("", "[30,grow 50][50,grow][50,grow][50,grow][30,grow 300]", "[]"));

		JLabel lblSample = new JLabel("Functional annotation");
		step4Panel.add(lblSample, "cell 0 0,alignx left");
		
		JComboBox<String> funcComboBox = new JComboBox<String>();
		funcComboBox.setEditable(true);
		step4Panel.add(funcComboBox, "cell 1 0,growx");
	}

	/**
	 * Create the panel.
	 */
	public AdvancedSettingPanel(MetaParameterV1 par) {

		int threadCount = par.getThreadCount();
		int instruType = par.getInstruType();
		String resource = par.getResource();
		boolean useBuild = par.isBuildIn();
		boolean useUnipept = par.isUnipept();
		boolean clusterMS2 = par.isCluster();
		boolean[] workflow = par.getWorkflow();
		int sampleid = par.getFunctionSampleId();
		HashSet<RootType> usedTypes = par.getUsedRootTypes();
		TaxonomyRanks ignoreRank = par.getIgnoreBlankRank();
		boolean exEnviron = par.getExcludeTaxa().contains("environmental samples");

		setBorder(new TitledBorder(null, "Advanced Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[150,grow 180][10,grow 20][500,grow 1000]",
				"[150,grow][150,grow][250,grow 300][150,grow 200]"));

		workflowPanel2 = new WorkflowPanel2(workflow);
		add(workflowPanel2, "cell 0 0 1 4,grow");

		JPanel processPanel = new JPanel();
		processPanel.setBorder(
				new TitledBorder(null, "Processing settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(processPanel, "cell 2 0,grow");
		processPanel.setLayout(
				new MigLayout("", "[40,grow 60][10][30,grow 50][200,grow 300][30,grow 50][10][30,grow 50]", "[30,grow 50][30,grow 50]"));

		JLabel lblNumberOfThreads = new JLabel("Number of threads");
		processPanel.add(lblNumberOfThreads, "cell 0 0");

		threadComboBox = new JComboBox<Integer>();
		processPanel.add(threadComboBox, "cell 2 0,growx");

		JLabel lblResource = new JLabel("Resource");
		processPanel.add(lblResource, "cell 0 1");

		resourceTextField = new JTextField();
		resourceTextField.setText(resource);
		processPanel.add(resourceTextField, "cell 2 1 2 1,growx");
		resourceTextField.setColumns(10);

		resourceBtnBrowse = new JButton("Browse");
		processPanel.add(resourceBtnBrowse, "cell 4 1");

		resourceCheckBxDefault = new JCheckBox("Default");
		resourceCheckBxDefault.setSelected(resource.equals("Resources\\"));
		resourceCheckBxDefault.addItemListener(l -> {
			if(resourceCheckBxDefault.isSelected()) {
				resourceTextField.setText("Resources\\");
			}else {
				resourceTextField.setText("");
			}
			checkResource();
		});
		processPanel.add(resourceCheckBxDefault, "cell 5 1");

		int totalCount = Runtime.getRuntime().availableProcessors();
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
			threadComboBox.addItem(i);
		}

		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}

		JPanel step12Panel = new JPanel();
		step12Panel.setBorder(new TitledBorder(null, "Step 1 & 2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step12Panel.setBackground(new Color(135, 206, 255));
		add(step12Panel, "cell 2 1,grow");
		step12Panel.setLayout(new MigLayout("", "[135][30][100][30][360]", "[40,grow 60][40,grow 60]"));

		JLabel lblInstruReso = new JLabel("Instrument resolution");
		step12Panel.add(lblInstruReso, "cell 0 1");

		this.comboBoxReso = new JComboBox<String>();
		step12Panel.add(comboBoxReso, "cell 2 1,growx");

		instruTextField = new JTextField(instru[0]);
		step12Panel.add(instruTextField, "cell 4 1,growx");
		instruTextField.setEnabled(false);
		instruTextField.setEditable(false);
		instruTextField.setColumns(10);

		comboBoxReso.addItem("High-High");
		comboBoxReso.addItem("High-Low");
		comboBoxReso.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				int id = comboBoxReso.getSelectedIndex();
				if (id == 0) {
					instruTextField.setText(instru[0]);
				} else if (id == 1) {
					instruTextField.setText(instru[1]);
				}
			}
		});

		if (instruType >= 0 && instruType < 2) {
			comboBoxReso.setSelectedIndex(instruType);
		}

		if (!(workflow[0] || workflow[1])) {
			lblInstruReso.setEnabled(false);
			comboBoxReso.setEnabled(false);
		}

		JLabel lblClustering = new JLabel("Clustering MS/MS in step 1");
		step12Panel.add(lblClustering, "cell 0 0");

		rdbtnClusterYes = new JRadioButton("Yes");
		rdbtnClusterYes.setBackground(new Color(135, 206, 255));
		step12Panel.add(rdbtnClusterYes, "flowx,cell 2 0");
		buttonGroup.add(rdbtnClusterYes);

		rdbtnClusterNo = new JRadioButton("No");
		rdbtnClusterNo.setBackground(new Color(135, 206, 255));
		step12Panel.add(rdbtnClusterNo, "cell 2 0");
		buttonGroup.add(rdbtnClusterNo);

		if (clusterMS2) {
			rdbtnClusterYes.setSelected(true);
		} else {
			rdbtnClusterNo.setSelected(true);
		}

		if (!workflow[0]) {
			lblClustering.setEnabled(false);
			rdbtnClusterYes.setEnabled(false);
			rdbtnClusterNo.setEnabled(false);
		}

		JPanel step3Panel = new JPanel();
		step3Panel.setBorder(new TitledBorder(null, "Step 3", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step3Panel.setBackground(new Color(108, 166, 205));
		add(step3Panel, "cell 2 2,grow");
		step3Panel.setLayout(new MigLayout("", "[150,grow 200][30][300,grow 500][30,grow]",
				"[30,grow 50][30,grow 50][30,grow 50][30,grow 50]"));

		JLabel lblBy = new JLabel("Taxonomy analysis by");
		step3Panel.add(lblBy, "cell 0 0");

		JPanel lcaPanel = new JPanel();
		lcaPanel.setBorder(
				new TitledBorder(null, "LCA calculation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step3Panel.add(lcaPanel, "cell 2 0 1 4,grow");
		lcaPanel.setLayout(new MigLayout("", "[]", "[50,grow][50,grow][50,grow][50,grow]"));

		JLabel lblTaxaFromWhich = new JLabel("Taxa from which top nodes showed in result");
		lcaPanel.add(lblTaxaFromWhich, "cell 0 0");

		chckbxEukaryota = new JCheckBox("Eukaryota");
		chckbxEukaryota.setSelected(usedTypes.contains(RootType.Eukaryota));
		lcaPanel.add(chckbxEukaryota, "cell 1 0");

		chckbxBacteria = new JCheckBox("Bacteria");
		chckbxBacteria.setSelected(usedTypes.contains(RootType.Bacteria));
		lcaPanel.add(chckbxBacteria, "cell 2 0");

		chckbxArchaea = new JCheckBox("Archaea");
		chckbxArchaea.setSelected(usedTypes.contains(RootType.Archaea));
		lcaPanel.add(chckbxArchaea, "cell 3 0");

		chckbxViruses = new JCheckBox("Viruses");
		chckbxViruses.setSelected(usedTypes.contains(RootType.Viruses));
		lcaPanel.add(chckbxViruses, "cell 4 0");

		chckbxViroids = new JCheckBox("Viroids");
		chckbxViroids.setSelected(usedTypes.contains(RootType.Viroids));
		lcaPanel.add(chckbxViroids, "cell 1 1");

		chckbxUnclassified = new JCheckBox("unclassified sequences");
		chckbxUnclassified.setSelected(usedTypes.contains(RootType.unclassified));
		lcaPanel.add(chckbxUnclassified, "cell 2 1");

		chckbxOther = new JCheckBox("other sequences");
		chckbxOther.setSelected(usedTypes.contains(RootType.other));
		lcaPanel.add(chckbxOther, "cell 3 1");

		JLabel lblIgnoreBlanksBelow = new JLabel("Ignore blanks below rank");
		lcaPanel.add(lblIgnoreBlanksBelow, "cell 0 2");

		rankComboBox = new JComboBox<TaxonomyRanks>();
		lcaPanel.add(rankComboBox, "cell 1 2,growx");
		rankComboBox.addItem(TaxonomyRanks.Superkingdom);
		rankComboBox.addItem(TaxonomyRanks.Phylum);
		rankComboBox.addItem(TaxonomyRanks.Class);
		rankComboBox.addItem(TaxonomyRanks.Order);
		rankComboBox.addItem(TaxonomyRanks.Family);
		rankComboBox.addItem(TaxonomyRanks.Genus);
		rankComboBox.addItem(TaxonomyRanks.Species);
		rankComboBox.setSelectedItem(ignoreRank);

		JLabel lblIgnoreTaxaBelow = new JLabel("Ignore taxa below parents");
		lcaPanel.add(lblIgnoreTaxaBelow, "cell 0 3");

		chckbxEnviomentalSamples = new JCheckBox("environmental samples");
		chckbxEnviomentalSamples.setSelected(exEnviron);
		lcaPanel.add(chckbxEnviomentalSamples, "cell 1 3");

		JButton btnDefault = new JButton("Restore defaults");
		btnDefault.addActionListener(l -> {
			rankComboBox.setSelectedItem(TaxonomyRanks.Family);
			chckbxEukaryota.setSelected(true);
			chckbxBacteria.setSelected(true);
			chckbxArchaea.setSelected(true);
			chckbxViruses.setSelected(true);
			chckbxViroids.setSelected(false);
			chckbxUnclassified.setSelected(false);
			chckbxOther.setSelected(false);
			chckbxEnviomentalSamples.setSelected(true);
		});
		lcaPanel.add(btnDefault, "cell 3 3 2 1,grow");

		chckbxBuiltinDb = new JCheckBox("Built-in database");
		chckbxBuiltinDb.setBackground(new Color(108, 166, 205));
		chckbxBuiltinDb.setSelected(useBuild);
		step3Panel.add(chckbxBuiltinDb, "cell 0 1");

		chckbxUnipept = new JCheckBox("Unipept (http://unipept.ugent.be/)");
		chckbxUnipept.setBackground(new Color(108, 166, 205));
		chckbxUnipept.setSelected(useUnipept);
		step3Panel.add(chckbxUnipept, "cell 0 2");

		JPanel step4Panel = new JPanel();
		step4Panel.setBorder(new TitledBorder(null, "Step 4", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		step4Panel.setBackground(new Color(108, 166, 205));
		add(step4Panel, "cell 2 3,grow");
		step4Panel.setLayout(new MigLayout("", "[30,grow 50][50,grow][50,grow][50,grow][30,grow 300]", "[]"));

		JLabel lblSample = new JLabel("Functional annotation");
		step4Panel.add(lblSample, "cell 0 0");
		
		funcComboBox = new JComboBox<String>();
		step4Panel.add(funcComboBox, "cell 1 0,growx");

		JCheckBox chckbxStep1 = workflowPanel2.getChckbxStep1();
		JCheckBox chckbxStep2 = workflowPanel2.getChckbxStep2();
		JCheckBox chckbxStep3 = workflowPanel2.getChckbxStep3();
		JCheckBox chckbxStep4 = workflowPanel2.getChckbxStep4();

		chckbxStep1.addItemListener(l -> {
			if (!chckbxStep1.isSelected()) {
				if (!(chckbxStep2.isSelected() || chckbxStep3.isSelected() || chckbxStep4.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep1.setSelected(true);
				} else {
					lblClustering.setEnabled(false);
					rdbtnClusterYes.setEnabled(false);
					rdbtnClusterNo.setEnabled(false);

					if (!chckbxStep2.isSelected()) {
						lblInstruReso.setEnabled(false);
						comboBoxReso.setEnabled(false);
					}
				}
			} else {
				lblClustering.setEnabled(true);
				rdbtnClusterYes.setEnabled(true);
				rdbtnClusterNo.setEnabled(true);

				lblInstruReso.setEnabled(true);
				comboBoxReso.setEnabled(true);
			}
		});

		chckbxStep1.setSelected(workflow[0]);

		chckbxStep2.addItemListener(l -> {
			if (!chckbxStep2.isSelected()) {
				if (!(chckbxStep1.isSelected() || chckbxStep3.isSelected() || chckbxStep4.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep2.setSelected(true);
				} else if (chckbxStep1.isSelected() && chckbxStep3.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
									"Since step 1 was selected and step 2 was skipped, taxonomy analysis will based on the peptide identification result from step 1.",
									"information", JOptionPane.WARNING_MESSAGE);
						}
					});
				} else {
					lblInstruReso.setEnabled(false);
					comboBoxReso.setEnabled(false);
				}
			} else {
				lblInstruReso.setEnabled(true);
				comboBoxReso.setEnabled(true);
			}
		});

		chckbxStep2.setSelected(workflow[1]);

		chckbxStep3.addItemListener(l -> {
			if (!chckbxStep3.isSelected()) {
				if (!(chckbxStep1.isSelected() || chckbxStep2.isSelected() || chckbxStep4.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep3.setSelected(true);
				} else {
					step3Panel.setEnabled(false);
					Component[] components = step3Panel.getComponents();
					for (Component component : components) {
						component.setEnabled(false);
					}

					Component[] components1 = lcaPanel.getComponents();
					for (Component component : components1) {
						component.setEnabled(false);
					}
				}
			} else {
				if (chckbxStep1.isSelected() && !chckbxStep2.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
									"Since step 1 was selected and step 2 was skipped, taxonomy analysis will based on the peptide identification result from step 1.",
									"information", JOptionPane.WARNING_MESSAGE);
						}
					});
				} else {
					step3Panel.setEnabled(true);
					Component[] components = step3Panel.getComponents();
					for (Component component : components) {
						component.setEnabled(true);
					}

					Component[] components1 = lcaPanel.getComponents();
					for (Component component : components1) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxStep3.setSelected(workflow[2]);

		chckbxStep4.addItemListener(l -> {
			if (!chckbxStep4.isSelected()) {
				if (!(chckbxStep1.isSelected() || chckbxStep2.isSelected() || chckbxStep3.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep4.setSelected(true);
				} else {
					step4Panel.setEnabled(false);
					Component[] components = step4Panel.getComponents();
					for (Component component : components) {
						component.setEnabled(false);
					}
				}
			} else {
				if (chckbxStep1.isSelected() && !chckbxStep2.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedSettingPanel.this,
									"Since step 1 was selected and step 2 was skipped, taxonomy analysis will based on the peptide identification result from step 1.",
									"information", JOptionPane.WARNING_MESSAGE);
						}
					});
				} else {
					step4Panel.setEnabled(true);
					Component[] components = step4Panel.getComponents();
					for (Component component : components) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxStep4.setSelected(workflow[3]);

		if (chckbxStep3.isSelected()) {
			step3Panel.setEnabled(true);
			Component[] components = step3Panel.getComponents();
			for (Component component : components) {
				component.setEnabled(true);
			}

			Component[] components1 = lcaPanel.getComponents();
			for (Component component : components1) {
				component.setEnabled(true);
			}
		} else {
			step3Panel.setEnabled(false);
			Component[] components = step3Panel.getComponents();
			for (Component component : components) {
				component.setEnabled(false);
			}

			Component[] components1 = lcaPanel.getComponents();
			for (Component component : components1) {
				component.setEnabled(false);
			}
		}

		if (chckbxStep4.isSelected()) {
			step4Panel.setEnabled(true);
			Component[] components = step4Panel.getComponents();
			for (Component component : components) {
				component.setEnabled(true);
			}
		} else {
			step4Panel.setEnabled(false);
			Component[] components = step4Panel.getComponents();
			for (Component component : components) {
				component.setEnabled(false);
			}
		}

		this.checkResource();
		
		if (sampleid < funcComboBox.getItemCount()) {
			this.funcComboBox.setSelectedIndex(sampleid);
		}
	}

	public JButton getResourceBtnBrowse() {
		return resourceBtnBrowse;
	}

	public JCheckBox getResourceCheckBxDefault() {
		return resourceCheckBxDefault;
	}

	public JTextField getResourceTextField() {
		return resourceTextField;
	}

	public int getThreadCount() {
		return (int) threadComboBox.getSelectedItem();
	}

	public int getInstruType() {
		return comboBoxReso.getSelectedIndex();
	}

	public boolean useBuiltin() {
		return chckbxBuiltinDb.isSelected();
	}

	public boolean useUnipept() {
		return chckbxUnipept.isSelected();
	}

	public boolean clusterMS2() {
		return rdbtnClusterYes.isSelected();
	}

	public boolean[] getWrokflow() {
		return workflowPanel2.getWorkflow();
	}

	public TaxonomyRanks getIgnoreRank() {
		return rankComboBox.getItemAt(rankComboBox.getSelectedIndex());
	}

	public HashSet<RootType> getRootSet() {
		HashSet<RootType> rootSet = new HashSet<RootType>();
		if (chckbxEukaryota.isSelected()) {
			rootSet.add(RootType.Eukaryota);
		}
		if (chckbxBacteria.isSelected()) {
			rootSet.add(RootType.Bacteria);
		}
		if (chckbxArchaea.isSelected()) {
			rootSet.add(RootType.Archaea);
		}
		if (chckbxViruses.isSelected()) {
			rootSet.add(RootType.Viruses);
		}
		if (chckbxViroids.isSelected()) {
			rootSet.add(RootType.Viroids);
		}
		if (chckbxUnclassified.isSelected()) {
			rootSet.add(RootType.unclassified);
		}
		if (chckbxOther.isSelected()) {
			rootSet.add(RootType.other);
		}
		return rootSet;
	}

	public HashSet<String> getIgnoreTaxaSet() {
		HashSet<String> ignoreTaxaSet = new HashSet<String>();
		if (chckbxEnviomentalSamples.isSelected()) {
			ignoreTaxaSet.add("environmental samples");
		}
		return ignoreTaxaSet;
	}

	public int getFunctionId() {
		return funcComboBox.getSelectedIndex();
	}
	
	public String getSampleName() {
		return (String) funcComboBox.getSelectedItem();
	}

	public String getResource() {
		return resourceTextField.getText();
	}

	public boolean checkResource() {

		System.out.println("Checking configurations...");
		boolean ok = true;

		String resource = resourceTextField.getText();
		if (resource == null || resource.length() == 0) {
			System.out.println(
					"Notice: resource folder is not found, please config it in \"Advanced settings -> Processing settings\"");
			return false;
		}

		File resourceFile = new File(resource);
		if (!resourceFile.exists()) {
			System.out
					.println("Notice: resource folder " + resource + " is not found, please check the configurations.");
			return false;
		}

		if (!resourceFile.isDirectory()) {
			System.out.println("Notice: resource folder " + resource
					+ " seems not a valid folder, please check the configurations.");
			return false;
		}
		
		funcComboBox.removeAllItems();
		funcComboBox.addItem("Unknown");
		
		File[] funcDbs = (new File(resource + "\\function")).listFiles();
		HashSet<String> sampleSet = new HashSet<String>();
		for (int i = 0; i < funcDbs.length; i++) {
			String name = funcDbs[i].getName();
			if (name.endsWith("gc")) {
				int sampleBegin = name.indexOf("_");
				int sampleEnd = name.indexOf(".");
				if (sampleBegin > 0 && sampleEnd > sampleBegin) {
					String sample = name.substring(sampleBegin + 1, sampleEnd);
					if (!sample.endsWith("COG") && !sample.endsWith("NOG")) {
						sampleSet.add(sample);
					}
				}
			}
		}
		String[] samples = sampleSet.toArray(new String[sampleSet.size()]);
		Arrays.sort(samples);
		for (int i = 0; i < samples.length; i++) {
			funcComboBox.addItem(samples[i]);
		}

		String[] checkList = new String[] { "db\\pep2taxa.gz", "db\\pep2taxa.tab", "function\\Category_COG.gc",
				"function\\Category_NOG.gc", "ProteomicsTools", "tandem", "tandem\\bin\\tandem.exe",
				"tandem\\bin\\default_input.xml", "tandem\\bin\\input.xml", "tandem\\bin\\taxonomy.xml", "mq_bin",
				"mq_bin\\MaxQuantCmd.exe", "mq_bin\\mqpar.xml", "mq_bin\\conf\\modifications.xml",
				"mq_bin\\conf\\enzymes.xml" };

		int checkId = 0;
		File pep2taxGz = new File(resource + "\\" + checkList[checkId++]);
		File pep2taxTab = new File(resource + "\\" + checkList[checkId++]);

		if (pep2taxTab.exists()) {

		} else {
			if (pep2taxGz.exists()) {

			} else {
				System.out.println("Notice: pep2tax database " + "\"pep2taxa.gz\" or \"pep2taxa.tab\""
						+ "is not found in folder " + resource + "\\db, please check the configurations.");
				ok = false;
			}
		}

		for (int i = 0; i < 2; i++) {
			File functionFile = new File(resource + "\\" + checkList[checkId++]);
			if (!functionFile.exists()) {
				String name = functionFile.getName();
				System.out.println("Notice: functional annotation database \"" + name + "\" is not found in folder "
						+ resource + "\\function, please check the configurations.");
				ok = false;
			}
		}

		File ptFile = new File(resource + "\\" + checkList[checkId++]);
		if (!ptFile.exists()) {
			System.out.println("Notice: spectra converting tool \"" + ptFile + "\" is not found in folder " + resource
					+ ", please check the configurations.");
			ok = false;
		}

		File tandemFile = new File(resource + "\\" + checkList[checkId++]);
		if (!tandemFile.exists()) {
			System.out.println("Notice: X!Tandem folder \"" + tandemFile.getName() + "\" is not found in folder "
					+ tandemFile.getParent() + ", please check the configurations.");
			ok = false;
		}

		File tandemExeFile = new File(resource + "\\" + checkList[checkId++]);
		if (!tandemExeFile.exists()) {
			System.out.println("Notice: X!Tandem runnable program \"" + tandemExeFile.getName()
					+ "\" is not found in folder " + tandemExeFile.getParent() + ", please check the configurations.");
			ok = false;
		}

		for (int i = 0; i < 3; i++) {
			File tandemParaFile = new File(resource + "\\" + checkList[checkId++]);
			if (!tandemParaFile.exists()) {
				System.out.println(
						"Notice: X!Tandem parameter file \"" + tandemParaFile.getName() + "\" is not found in folder "
								+ tandemParaFile.getParent() + ", please check the configurations.");
				ok = false;
			}
		}

		File mqFile = new File(resource + "\\" + checkList[checkId++]);
		if (!mqFile.exists()) {
			System.out.println("Notice: MaxQuant folder \"" + mqFile.getName() + "\" is not found in folder "
					+ mqFile.getParent() + ", please check the configurations.");
			ok = false;
		}

		File mqExeFile = new File(resource + "\\" + checkList[checkId++]);
		if (!mqExeFile.exists()) {
			System.out.println("Notice: MaxQuant runnable program \"" + mqExeFile.getName()
					+ "\" is not found in folder " + mqExeFile.getParent() + ", please check the configurations.");
			ok = false;
		}

		File mqParFile = new File(resource + "\\" + checkList[checkId++]);
		if (!mqParFile.exists()) {
			System.out.println("Notice: MaxQuant parameter file \"" + mqParFile.getName() + "\" is not found in folder "
					+ mqParFile.getParent() + ", please generate \"mqpar.xml\" in " + mqParFile.getParent()
					+ " by running MaxQuantGui.exe (just load a raw file in the MaxQuant GUI and then save the parameter file).");
			ok = false;
		}

		for (int i = 0; i < 2; i++) {
			File mqConfFile = new File(resource + "\\" + checkList[checkId++]);
			if (!mqConfFile.exists()) {
				System.out.println("Notice: MaxQuant configuration file \"" + mqConfFile.getName()
						+ "\" is not found in folder " + mqConfFile.getParent() + ", please check the configurations.");
				ok = false;
			}
		}

		if (ok) {
			System.out.println("Perfect! MetaLab is ready for use.");
		} else {
			System.out.println("Configuration is not complete, some modules will not work properly");
		}
		
		return ok;
	}
}
