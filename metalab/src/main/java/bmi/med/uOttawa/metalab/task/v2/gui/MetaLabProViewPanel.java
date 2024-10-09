package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.JPanel;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantProReader;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MetaLabProViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8900592071852044703L;
	private JTextField textFieldPath;
	private JTable proteinTable;
	private DefaultTableModel model;
	private JTextField textFieldMeta;
	private JTextField textFieldOutput;

	private MetaProtein[] mps;

	/**
	 * Create the panel.
	 */
	public MetaLabProViewPanel() {

		setLayout(new MigLayout("", "[1000,grow][20][100]", "[600,grow 200][100][100]"));

		JScrollPane scrollPanePro = new JScrollPane();
		add(scrollPanePro, "cell 0 0 3 1,grow");

		proteinTable = new JTable();
		scrollPanePro.setViewportView(proteinTable);

		JPanel panelControl = new JPanel();
		add(panelControl, "cell 0 1,grow");
		panelControl.setLayout(new MigLayout("", "[60][15][60][grow][15][]", "[30][30][30]"));

		JLabel lblFilePath = new JLabel("File path");
		panelControl.add(lblFilePath, "cell 0 0");

		textFieldPath = new JTextField();
		panelControl.add(textFieldPath, "cell 2 0 2 1,growx");
		textFieldPath.setColumns(10);

		JButton buttonAddPro = new JButton("Browse");
		panelControl.add(buttonAddPro, "cell 5 0");
		buttonAddPro.addActionListener(l -> {
			String path = textFieldPath.getText();
			JFileChooser fileChooser;
			if (path != null) {
				fileChooser = new JFileChooser(new File(path));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileFilter(new FileNameExtensionFilter("Protein identification result (.txt)", "txt"));

			int returnValue = fileChooser.showOpenDialog(MetaLabProViewPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File proteins = fileChooser.getSelectedFile();
				textFieldPath.setText(proteins.getAbsolutePath());
			}
		});

		JLabel lblMetadata = new JLabel("Metadata");
		panelControl.add(lblMetadata, "cell 0 1");

		textFieldMeta = new JTextField();
		panelControl.add(textFieldMeta, "cell 2 1 2 1,growx");
		textFieldMeta.setColumns(10);

		JButton buttonAddMeta = new JButton("Browse");
		buttonAddMeta.addActionListener(l -> {
			String path = textFieldMeta.getText();
			JFileChooser fileChooser;
			if (path != null) {
				fileChooser = new JFileChooser(new File(path));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileFilter(
					new FileNameExtensionFilter("Metadata (.xlsx, .txt)", new String[] { "xlsx", "txt" }));

			int returnValue = fileChooser.showOpenDialog(MetaLabProViewPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File proteins = fileChooser.getSelectedFile();
				textFieldMeta.setText(proteins.getAbsolutePath());
			}
		});
		panelControl.add(buttonAddMeta, "cell 5 1");

		JLabel lblOutput = new JLabel("Output");
		panelControl.add(lblOutput, "cell 0 2");

		textFieldOutput = new JTextField();
		panelControl.add(textFieldOutput, "cell 2 2 2 1,growx");
		textFieldOutput.setColumns(10);

		JButton btnOutput = new JButton("Browse");
		panelControl.add(btnOutput, "cell 5 2");

		JButton btnLoad = new JButton("Load");
		add(btnLoad, "cell 2 1,grow");
		btnLoad.addActionListener(l -> {
			File proteins = new File(this.textFieldPath.getText());
			File meta = new File(this.textFieldMeta.getText());

			if (proteins.exists()) {
				if (meta.exists()) {
					MaxquantProReader proReader = new MaxquantProReader(proteins);
					Object[] titleObjs = proReader.getTitleObjs();
					mps = proReader.getMetaProteins();
					Object[][] objs = new Object[mps.length][];
					for (int i = 0; i < objs.length; i++) {
						objs[i] = mps[i].getTableObjects();
					}
					model = new DefaultTableModel(objs, titleObjs);
					proteinTable.setModel(model);
				} else {
					MaxquantProReader proReader = new MaxquantProReader(proteins);
					Object[] titleObjs = proReader.getTitleObjs();
					mps = proReader.getMetaProteins();
					Object[][] objs = new Object[mps.length][];
					for (int i = 0; i < objs.length; i++) {
						objs[i] = mps[i].getTableObjects();
					}
					model = new DefaultTableModel(objs, titleObjs);
					proteinTable.setModel(model);
				}
			}
		});
	}

	public MetaProtein[] getMetaProteins() {
		return mps;
	}
}
