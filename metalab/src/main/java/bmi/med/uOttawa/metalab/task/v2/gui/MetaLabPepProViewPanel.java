package bmi.med.uOttawa.metalab.task.v2.gui;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantPepReader;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantProReader;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannPgMatrixReader;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannPrMatrixReader;
import bmi.med.uOttawa.metalab.dbSearch.fragpipe.FragpipePepReader;
import bmi.med.uOttawa.metalab.dbSearch.fragpipe.FragpipeProReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanPepReader;
import bmi.med.uOttawa.metalab.quant.flashLFQ.FlashLfqQuanProReader;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class MetaLabPepProViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7264280448338599499L;
	private JTextField textFieldPath;
	private JTable ppTable;
	private DefaultTableModel model;
	private JTextField textFieldOutput;
	private File inputFile;

	private MetaPeptide[] mpeps;
	private MetaProtein[] mpros;
	private String[] intensityTitle;
	private String quanMode;
	private String quanResultType;

	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the panel.
	 */
	public MetaLabPepProViewPanel(MetaLabWorkflowType workflowType) {

		setLayout(new MigLayout("", "[1000,grow][20][100]", "[600,grow 200][100][100]"));

		JScrollPane scrollPanePep = new JScrollPane();
		scrollPanePep.setEnabled(false);
		scrollPanePep.setViewportBorder(null);
		add(scrollPanePep, "cell 0 0 3 1,grow");

		ppTable = new JTable();
		scrollPanePep.setViewportView(ppTable);

		JPanel panelControl = new JPanel();
		add(panelControl, "cell 0 1,grow");
		panelControl.setLayout(new MigLayout("", "[60][15][60][60][60][60][15:600:1600,grow][60]", "[30][5][30][5][30]"));

		JLabel lblResultType = new JLabel("Result type");
		panelControl.add(lblResultType, "cell 0 0");

		JRadioButton rdbtnMaxquant = new JRadioButton("MaxQuant");
		buttonGroup.add(rdbtnMaxquant);
		panelControl.add(rdbtnMaxquant, "cell 2 0");
		rdbtnMaxquant.setSelected(true);

		JRadioButton rdbtnFlashlfq = new JRadioButton("FlashLFQ");
		buttonGroup.add(rdbtnFlashlfq);
		panelControl.add(rdbtnFlashlfq, "cell 3 0");
		
		JRadioButton rdbtnDiann = new JRadioButton("Dia-NN");
		buttonGroup.add(rdbtnDiann);
		panelControl.add(rdbtnDiann, "cell 4 0");
		
		JRadioButton rdbtnFragpipe = new JRadioButton("Fragpipe");
		buttonGroup.add(rdbtnFragpipe);
		panelControl.add(rdbtnFragpipe, "cell 5 0");

		JLabel lblFilePath = new JLabel("File path");
		panelControl.add(lblFilePath, "cell 0 2,alignx left");

		textFieldPath = new JTextField();
		panelControl.add(textFieldPath, "cell 2 2 5 1,growx");
		textFieldPath.setColumns(10);

		JButton buttonAddPep = new JButton("Browse");
		panelControl.add(buttonAddPep, "cell 7 2");
		buttonAddPep.addActionListener(l -> {
			String path = textFieldPath.getText();
			JFileChooser fileChooser;
			if (path != null) {
				fileChooser = new JFileChooser(new File(path));
			} else {
				fileChooser = new JFileChooser();
			}
			if (workflowType == MetaLabWorkflowType.TaxonomyAnalysis) {
				fileChooser.setFileFilter(new FileNameExtensionFilter("Peptide identification result",
						new String[] { "txt", "tsv", "csv" }));
			} else if (workflowType == MetaLabWorkflowType.FunctionalAnnotation) {
				fileChooser.setFileFilter(new FileNameExtensionFilter("Protein identification result",
						new String[] { "txt", "tsv", "csv" }));
			}

			int returnValue = fileChooser.showOpenDialog(MetaLabPepProViewPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				this.inputFile = fileChooser.getSelectedFile();
				textFieldPath.setText(inputFile.getAbsolutePath());

				if (workflowType == MetaLabWorkflowType.TaxonomyAnalysis) {
					File output = new File(inputFile.getParent(), "taxonomy_analysis");
					textFieldOutput.setText(output.getAbsolutePath());
				} else if (workflowType == MetaLabWorkflowType.FunctionalAnnotation) {
					File output = new File(inputFile.getParent(), "functional_annotation");
					textFieldOutput.setText(output.getAbsolutePath());
				}
			}
		});

		JLabel lblOutPut = new JLabel("Output");
		panelControl.add(lblOutPut, "cell 0 4,alignx left");

		textFieldOutput = new JTextField();
		panelControl.add(textFieldOutput, "cell 2 4 5 1,growx");
		textFieldOutput.setColumns(10);

		JButton btnOutput = new JButton("Browse");
		panelControl.add(btnOutput, "cell 7 4");
		btnOutput.addActionListener(l -> {
			JFileChooser fileChooser;
			File resultDir = new File(textFieldOutput.getText());
			if (resultDir.exists()) {
				fileChooser = new JFileChooser(resultDir);
			} else {
				if (inputFile != null) {
					fileChooser = new JFileChooser(inputFile);
				} else {
					fileChooser = new JFileChooser(resultDir.getParent());
				}
			}

			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fileChooser.showSaveDialog(MetaLabPepProViewPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				textFieldOutput.setText(file.getAbsolutePath());
			}
		});

		JButton btnLoad = new JButton("Load");
		add(btnLoad, "cell 2 1,grow");
		btnLoad.addActionListener(l -> {

			if (workflowType == MetaLabWorkflowType.TaxonomyAnalysis) {
				File peptides = new File(this.textFieldPath.getText());

				if (peptides.exists()) {
					if (rdbtnMaxquant.isSelected()) {
						MaxquantPepReader pepReader = new MaxquantPepReader(peptides);
						Object[] titleObjs = pepReader.getTitleObjs();
						mpeps = pepReader.getMetaPeptides();
						Object[][] objs = new Object[mpeps.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpeps[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.maxQuant;
						this.quanMode = pepReader.getQuanMode();
						this.intensityTitle = pepReader.getIntensityTitle();

					} else if (rdbtnFlashlfq.isSelected()) {
						FlashLfqQuanPepReader pepReader = new FlashLfqQuanPepReader(peptides);
						Object[] titleObjs = pepReader.getTitleObjs();
						mpeps = pepReader.getMetaPeptides();
						Object[][] objs = new Object[mpeps.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpeps[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.flashLFQ;
						this.quanMode = pepReader.getQuanMode();
						this.intensityTitle = pepReader.getIntensityTitle();
					} else if(rdbtnDiann.isSelected()) {
						DiannPrMatrixReader pepReader = new DiannPrMatrixReader(peptides);
						Object[] titleObjs = pepReader.getTitleObjs();
						mpeps = pepReader.getMetaPeptides();
						Object[][] objs = new Object[mpeps.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpeps[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.diaNN;
						this.quanMode = pepReader.getQuanMode();
						this.intensityTitle = pepReader.getIntensityTitle();
					}else if (rdbtnFragpipe.isSelected()) {
						FragpipePepReader pepReader = new FragpipePepReader(peptides);
						Object[] titleObjs = pepReader.getTitleObjs();
						mpeps = pepReader.getMetaPeptides();
						Object[][] objs = new Object[mpeps.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpeps[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.fragpipeIGC;
						this.quanMode = pepReader.getQuanMode();
						this.intensityTitle = pepReader.getIntensityTitle();
					}
				}
			} else if (workflowType == MetaLabWorkflowType.FunctionalAnnotation) {
				File proteins = new File(this.textFieldPath.getText());

				if (proteins.exists()) {
					File output = new File(proteins.getParent(), "functional_annotation");
					textFieldOutput.setText(output.getAbsolutePath());

					if (rdbtnMaxquant.isSelected()) {
						MaxquantProReader proReader = new MaxquantProReader(proteins);
						Object[] titleObjs = proReader.getTitleObjs();
						mpros = proReader.getMetaProteins();
						Object[][] objs = new Object[mpros.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpros[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.maxQuant;
						this.quanMode = proReader.getQuanMode();
						this.intensityTitle = proReader.getIntensityTitle();

					} else if (rdbtnFlashlfq.isSelected()) {
						FlashLfqQuanProReader proReader = new FlashLfqQuanProReader(proteins);
						Object[] titleObjs = proReader.getTitleObjs();
						mpros = proReader.getMetaProteins();
						Object[][] objs = new Object[mpros.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpros[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.flashLFQ;
						this.quanMode = proReader.getQuanMode();
						this.intensityTitle = proReader.getIntensityTitle();
					} else if (rdbtnDiann.isSelected()) {
						DiannPgMatrixReader proReader = new DiannPgMatrixReader(proteins);
						Object[] titleObjs = proReader.getTitleObjs();
						mpros = proReader.getMetaProteins();
						Object[][] objs = new Object[mpros.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpros[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.diaNN;
						this.quanMode = proReader.getQuanMode();
						this.intensityTitle = proReader.getIntensityTitle();
					} else if (rdbtnFragpipe.isSelected()) {
						FragpipeProReader proReader = new FragpipeProReader(proteins);
						Object[] titleObjs = proReader.getTitleObjs();
						mpros = proReader.getMetaProteins();
						Object[][] objs = new Object[mpros.length][];
						for (int i = 0; i < objs.length; i++) {
							objs[i] = mpros[i].getTableObjects();
						}
						model = new DefaultTableModel(objs, titleObjs);
						ppTable.setModel(model);

						this.quanResultType = MetaConstants.fragpipeIGC;
						this.quanMode = proReader.getQuanMode();
						this.intensityTitle = proReader.getIntensityTitle();
					} 
				}
			}

		});
	}

	public MetaPeptide[] getMetaPeptides() {
		return this.mpeps;
	}

	public MetaProtein[] getMetaProteins() {
		return this.mpros;
	}

	public String getOutput() {
		return textFieldOutput.getText();
	}

	public String[] getIntensityTitle() {
		return intensityTitle;
	}

	public String getQuanMode() {
		return quanMode;
	}

	public String getQuanResultType() {
		return quanResultType;
	}

}
