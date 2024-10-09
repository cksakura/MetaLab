/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.border.TitledBorder;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.v1.par.JsonParaHandler;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

import java.awt.Color;
import java.awt.Component;

import javax.swing.UIManager;

/**
 * @author Kai Cheng
 *
 */
public class AdvancedParameterDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9174679030565403642L;
	private final JPanel contentPanel = new JPanel();

	private JTextField textField1;

	private JCheckBox chckbxBuildinDatabase;
	private JCheckBox chckbxUnipept;

	private static final String defaultPep2taxa = "Resources//db//pep2taxa.gz";
	private static final String defaultPep2taxaTab = "Resources//db//pep2taxa.tab";
	protected static final String tempParameter = "Resources//temp.json";
	
	private JTextField textFieldReso;
	private String[] instru = {"HCD in Orbitrap fusion/Orbitrap Elite/Q Exactive", "CID in Orbitrap"};
	private JComboBox<String> comboBoxReso;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AdvancedParameterDialog dialog = new AdvancedParameterDialog(new MetaParameterV1());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * 
	 * @throws IOException
	 */
	public AdvancedParameterDialog(MetaParameterV1 par) {
		setTitle("Settings");

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(AdvancedParameterDialog.this, "Close this window?", "Really Closing?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

					AdvancedParameterDialog.this.dispose();
				}
			}
		});

		int threadCount = par.getThreadCount();
		int instruType = par.getInstruType();
		String pep2taxa = par.getPep2taxa();
		boolean useBuild = par.isBuildIn();
		boolean useUnipept = par.isUnipept();
		boolean clusterMS2 = par.isCluster();
		boolean[] workflow = par.getWorkflow();
		int sampleid = par.getFunctionSampleId();

		setBounds(100, 100, 760, 740);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[700,grow]", "[180][30][40][70][340][grow][30][]"));

		/**
		 * 
		 */
		JPanel panelOverall = new JPanel();
		panelOverall.setBackground(new Color(255, 255, 255));
		panelOverall.setBorder(new TitledBorder(null, "Overall", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(panelOverall, "cell 0 2,grow");
		panelOverall.setLayout(new MigLayout("", "[120][30][45]", "[]"));
		
		JLabel lblNumberOfThreads = new JLabel("Number of threads");
		panelOverall.add(lblNumberOfThreads, "cell 0 0");
		
		JComboBox<Integer> threadComboBox = new JComboBox<Integer>();
		panelOverall.add(threadComboBox, "cell 2 0,growx");
		
		int totalCount = Runtime.getRuntime().availableProcessors();
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
			threadComboBox.addItem(i);
		}
		
		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}
		
		/**
		 * 
		 */
		JLabel lblParameters = new JLabel("Parameters");
		contentPanel.add(lblParameters, "cell 0 1");
		
		/**
		 * 
		 */
		JPanel panelStep1_2 = new JPanel();
		panelStep1_2.setBackground(new Color(135, 206, 255));
		panelStep1_2
				.setBorder(new TitledBorder(null, "Step 1 & 2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(panelStep1_2, "cell 0 3,grow");
		panelStep1_2.setLayout(new MigLayout("", "[135][30][100][30][360]", "[][]"));

		JLabel lblInstruReso = new JLabel("Instrument resolution");
		panelStep1_2.add(lblInstruReso, "cell 0 1");

		this.comboBoxReso = new JComboBox<String>();
		panelStep1_2.add(comboBoxReso, "cell 2 1,growx");

		textFieldReso = new JTextField(instru[0]);
		panelStep1_2.add(textFieldReso, "cell 4 1,growx");
		textFieldReso.setEnabled(false);
		textFieldReso.setEditable(false);
		textFieldReso.setColumns(10);
		
		comboBoxReso.addItem("High-High");
		comboBoxReso.addItem("High-Low");
		comboBoxReso.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				int id = comboBoxReso.getSelectedIndex();
				if (id == 0) {
					textFieldReso.setText(instru[0]);
				} else if (id == 1) {
					textFieldReso.setText(instru[1]);
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
		panelStep1_2.add(lblClustering, "cell 0 0");

		JRadioButton rdbtnClusterYes = new JRadioButton("Yes");
		rdbtnClusterYes.setSelected(true);
		rdbtnClusterYes.setBackground(new Color(135, 206, 255));
		panelStep1_2.add(rdbtnClusterYes, "flowx,cell 2 0");

		buttonGroup.add(rdbtnClusterYes);
		JRadioButton rdbtnClusterNo = new JRadioButton("No");
		rdbtnClusterNo.setBackground(new Color(135, 206, 255));
		panelStep1_2.add(rdbtnClusterNo, "cell 2 0");
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
		
		/**
		 * 
		 */
		JPanel panelStep3 = new JPanel();
		panelStep3.setBackground(new Color(108, 166, 205));
		panelStep3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Step 3-1", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contentPanel.add(panelStep3, "cell 0 4,grow");
		panelStep3.setLayout(new MigLayout("", "[150,grow][450][80]", "[][][][grow][]"));

		JLabel lblPep2taxa = new JLabel("Peptide to Taxa");
		panelStep3.add(lblPep2taxa, "cell 0 1");
		lblPep2taxa.setEnabled(false);

		textField1 = new JTextField();
		panelStep3.add(textField1, "cell 1 1,growx");
		textField1.setEnabled(false);
		textField1.setColumns(10);
		textField1.setText(pep2taxa);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setBackground(new Color(108, 166, 205));
		panelStep3.add(btnBrowse, "cell 2 1");
		btnBrowse.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(textField1.getText());
			int returnValue = fileChooser.showOpenDialog(AdvancedParameterDialog.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				textField1.setText(file.getAbsolutePath());
			}
		});

		JLabel lblTaxoxomyAnalysisBy = new JLabel("Taxonomy analysis by");
		panelStep3.add(lblTaxoxomyAnalysisBy, "cell 0 0");

		this.chckbxBuildinDatabase = new JCheckBox("Built-in database");
		chckbxBuildinDatabase.setBackground(new Color(108, 166, 205));
		panelStep3.add(chckbxBuildinDatabase, "flowx,cell 1 0");

		JCheckBox chckbxDefault = new JCheckBox("Default");
		chckbxDefault.setBackground(new Color(108, 166, 205));
		panelStep3.add(chckbxDefault, "cell 1 2");
		chckbxDefault.addItemListener(l -> {
			if (chckbxDefault.isSelected()) {

				boolean validate0 = validateDatabase(defaultPep2taxaTab);

				if (validate0) {
					lblPep2taxa.setEnabled(false);
					textField1.setText(defaultPep2taxaTab);
					textField1.setEnabled(false);
					btnBrowse.setEnabled(false);
				} else {

					validate0 = validateDatabase(defaultPep2taxa);

					if (validate0) {
						lblPep2taxa.setEnabled(false);
						textField1.setText(defaultPep2taxa);
						textField1.setEnabled(false);
						btnBrowse.setEnabled(false);
					} else {
						chckbxDefault.setSelected(false);
					}
				}

			} else {
				lblPep2taxa.setEnabled(true);
				textField1.setEnabled(true);
				btnBrowse.setEnabled(true);
			}
		});
		chckbxDefault.setSelected(true);
		
		this.chckbxUnipept = new JCheckBox("Unipept (http://unipept.ugent.be/)", useUnipept);
		chckbxUnipept.setBackground(new Color(108, 166, 205));
		panelStep3.add(chckbxUnipept, "cell 1 0");
		
		JPanel pep2TaxParPanel = new JPanel();
		pep2TaxParPanel.setBorder(new TitledBorder(null, "LCA calculation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelStep3.add(pep2TaxParPanel, "cell 0 3 3 1,grow");
		pep2TaxParPanel.setLayout(new MigLayout("", "[211px][120][120][120][80]", "[35][35][35][35]"));
		
		JLabel topNodeLabel = new JLabel("Taxa from which top nodes showed in result");
		pep2TaxParPanel.add(topNodeLabel, "cell 0 0,alignx left,aligny center");
		
		HashSet<RootType> usedTypes = par.getUsedRootTypes();

		JCheckBox chckbxEukaryota = new JCheckBox("Eukaryota");
		chckbxEukaryota.setSelected(usedTypes.contains(RootType.Eukaryota));
		pep2TaxParPanel.add(chckbxEukaryota, "cell 1 0");
		
		JCheckBox chckbxBacteria = new JCheckBox("Bacteria");
		chckbxBacteria.setSelected(usedTypes.contains(RootType.Bacteria));
		pep2TaxParPanel.add(chckbxBacteria, "cell 2 0");
		
		JCheckBox chckbxArchaea = new JCheckBox("Archaea");
		chckbxArchaea.setSelected(usedTypes.contains(RootType.Archaea));
		pep2TaxParPanel.add(chckbxArchaea, "cell 3 0");
		
		JCheckBox chckbxViruses = new JCheckBox("Viruses");
		chckbxViruses.setSelected(usedTypes.contains(RootType.Viruses));
		pep2TaxParPanel.add(chckbxViruses, "cell 4 0");
		
		JCheckBox chckbxViroids = new JCheckBox("Viroids");
		chckbxViroids.setSelected(usedTypes.contains(RootType.Viroids));
		pep2TaxParPanel.add(chckbxViroids, "cell 1 1");
		
		JCheckBox chckbxUnclassified = new JCheckBox("unclassified sequences");
		chckbxUnclassified.setSelected(usedTypes.contains(RootType.unclassified));
		pep2TaxParPanel.add(chckbxUnclassified, "cell 2 1");
		
		JCheckBox chckbxOther = new JCheckBox("other sequences");
		chckbxOther.setSelected(usedTypes.contains(RootType.other));
		pep2TaxParPanel.add(chckbxOther, "cell 3 1");
		
		JLabel ignoreBlanksBelowLabel = new JLabel("Ignore blanks below rank ");
		pep2TaxParPanel.add(ignoreBlanksBelowLabel, "cell 0 2,alignx left,aligny center");
		
		JComboBox<TaxonomyRanks> rankComboBox = new JComboBox<TaxonomyRanks>();
		pep2TaxParPanel.add(rankComboBox, "cell 1 2,growx");
		rankComboBox.addItem(TaxonomyRanks.Superkingdom);
		rankComboBox.addItem(TaxonomyRanks.Phylum);
		rankComboBox.addItem(TaxonomyRanks.Class);
		rankComboBox.addItem(TaxonomyRanks.Order);
		rankComboBox.addItem(TaxonomyRanks.Family);
		rankComboBox.addItem(TaxonomyRanks.Genus);
		rankComboBox.addItem(TaxonomyRanks.Species);
		rankComboBox.setSelectedItem(par.getIgnoreBlankRank());
		
		JLabel ignoreTaxaBelowLabel = new JLabel("Ignore taxa below parents");
		pep2TaxParPanel.add(ignoreTaxaBelowLabel, "cell 0 3,alignx left,aligny center");
		
		JCheckBox chckbxEnviomentalSamples = new JCheckBox("environmental samples");
		chckbxEnviomentalSamples.setSelected(par.getExcludeTaxa().contains("environmental samples"));
		pep2TaxParPanel.add(chckbxEnviomentalSamples, "cell 1 3");
		
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
		pep2TaxParPanel.add(btnDefault, "cell 3 3 2 1,grow");

		chckbxBuildinDatabase.addItemListener(l -> {
			if (chckbxBuildinDatabase.isSelected()) {
				boolean validate0 = validateDatabase(textField1.getText());
				if (!validate0) {
					JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
							"Peptide database was not found in " + textField1.getText(), "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxBuildinDatabase.setSelected(false);
				} else {
					pep2TaxParPanel.setEnabled(true);
					Component[] components = pep2TaxParPanel.getComponents();
					for (Component component : components) {
						component.setEnabled(true);
					}
				}
			} else {
				pep2TaxParPanel.setEnabled(false);
				Component[] components = pep2TaxParPanel.getComponents();
				for (Component component : components) {
					component.setEnabled(false);
				}
			}
		});
		chckbxBuildinDatabase.setSelected(useBuild);
		
		JPanel panelStep3_2 = new JPanel();
		panelStep3_2.setBackground(new Color(108, 166, 205));
		panelStep3_2.setBorder(new TitledBorder(null, "Step 3-2", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(panelStep3_2, "cell 0 5,grow");
		panelStep3_2.setLayout(new MigLayout("", "[150][150][150][150]", "[][]"));
		
		JLabel lblSample = new JLabel("Sample");
		panelStep3_2.add(lblSample, "cell 0 0");
		
		ButtonGroup sampleBtnGroup = new ButtonGroup();
		
		JRadioButton rdbtnHumanGutMicrobiome = new JRadioButton("Human gut microbiome");
		rdbtnHumanGutMicrobiome.setBackground(new Color(108, 166, 205));
		panelStep3_2.add(rdbtnHumanGutMicrobiome, "cell 1 0");
		sampleBtnGroup.add(rdbtnHumanGutMicrobiome);
		if (sampleid == 1) {
			rdbtnHumanGutMicrobiome.setSelected(true);
		}
		
		JRadioButton rdbtnMouseGutMicrobiome = new JRadioButton("Mouse gut microbiome");
		rdbtnMouseGutMicrobiome.setBackground(new Color(108, 166, 205));
		panelStep3_2.add(rdbtnMouseGutMicrobiome, "cell 2 0");
		sampleBtnGroup.add(rdbtnMouseGutMicrobiome);
		if (sampleid == 2) {
			rdbtnMouseGutMicrobiome.setSelected(true);
		}
		
		JRadioButton rdbtnUnknown = new JRadioButton("Unknown");
		rdbtnUnknown.setBackground(new Color(108, 166, 205));
		panelStep3_2.add(rdbtnUnknown, "cell 3 0");
		sampleBtnGroup.add(rdbtnUnknown);
		if (sampleid == 0) {
			rdbtnUnknown.setSelected(true);
		}
		
		/**
		 * 
		 */
		WorkflowPanel workflowPanel = new WorkflowPanel();
		workflowPanel.setBorder(new TitledBorder(null, "Workflow", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(workflowPanel, "cell 0 0,grow");
		
		JCheckBox chckbxStep1 = workflowPanel.getChckbxStep1();
		JCheckBox chckbxStep2 = workflowPanel.getChckbxStep2();
		JCheckBox chckbxStep31 = workflowPanel.getChckbxStep31();
		JCheckBox chckbxStep32 = workflowPanel.getChckbxStep32();
		
		chckbxStep1.addItemListener(l -> {
			if (!chckbxStep1.isSelected()) {
				if (!(chckbxStep2.isSelected() || chckbxStep31.isSelected() || chckbxStep32.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
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
				if (!(chckbxStep1.isSelected() || chckbxStep31.isSelected() || chckbxStep32.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep2.setSelected(true);
				} else if (chckbxStep1.isSelected() && chckbxStep31.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
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
		
		chckbxStep31.addItemListener(l -> {
			if (!chckbxStep31.isSelected()) {
				if (!(chckbxStep1.isSelected() || chckbxStep2.isSelected() || chckbxStep32.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep31.setSelected(true);
				} else {
					panelStep3.setEnabled(false);
					Component[] components = panelStep3.getComponents();
					for (Component component : components) {
						component.setEnabled(false);
					}
					
					Component[] components1 = pep2TaxParPanel.getComponents();
					for (Component component : components1) {
						component.setEnabled(false);
					}
				}
			} else {
				if (chckbxStep1.isSelected() && !chckbxStep2.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
									"Since step 1 was selected and step 2 was skipped, taxonomy analysis will based on the peptide identification result from step 1.",
									"information", JOptionPane.WARNING_MESSAGE);
						}
					});
				} else {
					panelStep3.setEnabled(true);
					Component[] components = panelStep3.getComponents();
					for (Component component : components) {
						component.setEnabled(true);
					}
					
					Component[] components1 = pep2TaxParPanel.getComponents();
					for (Component component : components1) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxStep31.setSelected(workflow[2]);
		
		chckbxStep32.addItemListener(l -> {
			if (!chckbxStep32.isSelected()) {
				if (!(chckbxStep1.isSelected() || chckbxStep2.isSelected() || chckbxStep31.isSelected())) {
					JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
							"At least one step should be selected in workflow.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					chckbxStep32.setSelected(true);
				} else {
					panelStep3_2.setEnabled(false);
					Component[] components = panelStep3_2.getComponents();
					for (Component component : components) {
						component.setEnabled(false);
					}
				}
			} else {
				if (chckbxStep1.isSelected() && !chckbxStep2.isSelected()) {
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(AdvancedParameterDialog.this,
									"Since step 1 was selected and step 2 was skipped, taxonomy analysis will based on the peptide identification result from step 1.",
									"information", JOptionPane.WARNING_MESSAGE);
						}
					});
				} else {
					panelStep3_2.setEnabled(true);
					Component[] components = panelStep3_2.getComponents();
					for (Component component : components) {
						component.setEnabled(true);
					}
				}
			}
		});
		chckbxStep32.setSelected(workflow[3]);
		
		if (chckbxStep31.isSelected()) {
			panelStep3.setEnabled(true);
			Component[] components = panelStep3.getComponents();
			for (Component component : components) {
				component.setEnabled(true);
			}
			
			Component[] components1 = pep2TaxParPanel.getComponents();
			for (Component component : components1) {
				component.setEnabled(true);
			}
		} else {
			panelStep3.setEnabled(false);
			Component[] components = panelStep3.getComponents();
			for (Component component : components) {
				component.setEnabled(false);
			}
			
			Component[] components1 = pep2TaxParPanel.getComponents();
			for (Component component : components1) {
				component.setEnabled(false);
			}
		}
		
		if (chckbxStep32.isSelected()) {
			panelStep3_2.setEnabled(true);
			Component[] components = panelStep3_2.getComponents();
			for (Component component : components) {
				component.setEnabled(true);
			}
		} else {
			panelStep3_2.setEnabled(false);
			Component[] components = panelStep3_2.getComponents();
			for (Component component : components) {
				component.setEnabled(false);
			}
		}
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton saveButton = new JButton("Save");
				// don't change to lamda
				saveButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						if (JOptionPane.showConfirmDialog(AdvancedParameterDialog.this, "Save the changes?", "Save",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

							int threadCount = (int) threadComboBox.getSelectedItem();
							par.setThreadCount(threadCount);

							int instruType = comboBoxReso.getSelectedIndex();
							par.setInstruType(instruType);

							String pep2taxa = textField1.getText();
							par.setResource(pep2taxa);

							boolean useBuildin = chckbxBuildinDatabase.isSelected();
							par.setBuildIn(useBuildin);

							boolean useUnipept = chckbxUnipept.isSelected();
							par.setUnipept(useUnipept);

							boolean clusterMS2 = rdbtnClusterYes.isSelected();
							par.setCluster(clusterMS2);

							boolean[] workflow = workflowPanel.getWorkflow();
							par.setWorkflow(workflow);
							
							TaxonomyRanks ingoreRank = rankComboBox.getItemAt(rankComboBox.getSelectedIndex());
							par.setIgnoreBlankRank(ingoreRank);

							HashSet<RootType> rootSet = new HashSet<RootType>();
							if(chckbxEukaryota.isSelected()){
								rootSet.add(RootType.Eukaryota);
							}
							if(chckbxBacteria.isSelected()){
								rootSet.add(RootType.Bacteria);
							}
							if(chckbxArchaea.isSelected()){
								rootSet.add(RootType.Archaea);
							}
							if(chckbxViruses.isSelected()){
								rootSet.add(RootType.Viruses);
							}
							if(chckbxViroids.isSelected()){
								rootSet.add(RootType.Viroids);
							}
							if(chckbxUnclassified.isSelected()){
								rootSet.add(RootType.unclassified);
							}
							if(chckbxOther.isSelected()){
								rootSet.add(RootType.other);
							}
							par.setUsedRootTypes(rootSet);
							
							HashSet<String> ignoreTaxaSet = new HashSet<String>();
							if(chckbxEnviomentalSamples.isSelected()){
								ignoreTaxaSet.add("environmental samples");
							}
							par.setExcludeTaxa(ignoreTaxaSet);
							
							if (rdbtnHumanGutMicrobiome.isSelected()) {
								par.setFunctionSampleId(1);
							} else if (rdbtnMouseGutMicrobiome.isSelected()) {
								par.setFunctionSampleId(2);
							} else if (rdbtnUnknown.isSelected()) {
								par.setFunctionSampleId(0);
							}
							
							JsonParaHandler.export(par, tempParameter);

//							MaxquantParaHandler.setThreadCount(threadCount);

//							XTandemParameterHandler.setThreadCount(threadCount);

//							XTandemParameterHandler.setInstrumentType(instruType);
							
							AdvancedParameterDialog.this.dispose();
						}
					}
				});
				saveButton.setActionCommand("Save");
				buttonPane.add(saveButton);
				getRootPane().setDefaultButton(saveButton);
			}
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (JOptionPane.showConfirmDialog(AdvancedParameterDialog.this, "Close this window?",
								"Really Closing?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							AdvancedParameterDialog.this.dispose();
						}
					}
				});
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}
	
	private boolean validateDatabase(String pep2taxa) {

		File pep2taxaFile = new File(pep2taxa);
		if (!pep2taxaFile.exists()) {
			return false;
		}

		return true;
	}

	public String getPep2taxa() {
		return this.textField1.getText();
	}
}
