/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.MetaLabTaskType;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 * @author Kai Cheng
 *
 */
public class InputOutputPanel extends JPanel {

	private static Logger LOGGER = LogManager.getLogger();
	/**
	 * 
	 */
	private static final long serialVersionUID = 5588166584465647743L;
	private JTable table;
	private DefaultTableModel model;
	private DefaultTableModel expModel;
	private JFileChooser rawFileChooser;
	private JFileChooser batchChooser;
	private HashSet<String> fileSet;
	private JTextField resultTextField;
	private JTable patternTable;
	private JTextField textFieldFirst;
	private JTextField textFieldLast;
	private JTextField textFieldOther;
	private JTextField textFieldStart;
	private JTextField textFieldEnd;
	private JComboBox<String> splitSymbolComboBox;
	private String pattern;
	private boolean patternTableUpdate;

	/**
	 * Create the panel.
	 */
	/**
	 * @param rawFiles
	 * @param pFindResult
	 */
	public InputOutputPanel(MetaParameterMQ par) {

		MetaData metadata = par.getMetadata();
		String[] rawFiles = metadata.getRawFiles();
		String[] expNames = metadata.getExpNames();
		String result = par.getResult();

		setBackground(new Color(185, 238, 172));
		setPreferredSize(new Dimension(1000, 560));
		setLayout(new MigLayout("", "[1000,grow 1600]", "[500][40]"));

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "Raw files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.setLayout(
				new MigLayout("", "[240,grow 450][240,grow 450][240,grow 450][240,grow 450]", "[420,grow][40]"));
		add(contentPane, "cell 0 0,grow");

		this.fileSet = new HashSet<String>();
		if (rawFiles != null) {
			for (int i = 0; i < rawFiles.length; i++) {
				this.fileSet.add(rawFiles[i]);
			}
		}

		ExpIdDialog expDialog = new ExpIdDialog();
		expDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		expDialog.getOkButton().addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			String[] names = expDialog.getExpNames(selectedRow.length);

			for (int i = 0; i < selectedRow.length; i++) {
				table.setValueAt(names[i], selectedRow[i], 2);
			}

			expDialog.setVisible(false);
		});

		expDialog.getCancelButton().addActionListener(l -> {
			expDialog.setVisible(false);
		});

		JButton btnClear = new JButton("Clear");

		btnClear.addActionListener(l -> {
			fileSet = new HashSet<String>();
			int count = model.getRowCount();
			for (int i = count - 1; i >= 0; i--) {
				model.removeRow(i);
			}
			setPattern();
			btnClear.setEnabled(false);
		});
		contentPane.add(btnClear, "cell 3 1,alignx center");

		if (rawFiles != null && rawFiles.length > 0) {
			btnClear.setEnabled(true);
		} else {
			btnClear.setEnabled(false);
		}

		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			for (int i = 0; i < selectedRow.length; i++) {
				int first = table.getSelectedRow();
				String raw = (String) table.getValueAt(first, 1);
				fileSet.remove(raw);
				model.removeRow(first);
			}
			setPattern();
			btnRemove.setEnabled(false);
		});

		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(l -> {

			if (rawFiles != null && rawFiles.length > 0) {
				rawFileChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				rawFileChooser = new JFileChooser();
			}
			rawFileChooser.setFileFilter(new FileNameExtensionFilter(
					"raw files (*.raw, *.wiff, *.mzXML) or peptide identification result (.txt for MaxQuant and .xml for X!Tandem)",
					new String[] { "raw", "wiff", "mzXML", "txt", "xml" }));
			rawFileChooser.setMultiSelectionEnabled(true);

			int returnValue = rawFileChooser.showOpenDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = rawFileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					if (!fileSet.contains(file.getAbsolutePath())) {
						String path = file.getAbsolutePath();
						fileSet.add(path);

						String name = file.getName();
						name = name.substring(0, name.lastIndexOf("."));

						model.addRow(new Object[] { file.exists(), path, name });
						resultTextField.setText(file.getParentFile().getAbsolutePath() + "\\MetaLab");
					}
				}
				setPattern();
			}
		});

		JButton btnBatchLoad = new JButton("Batch load");
		btnBatchLoad.addActionListener(l -> {
			if (rawFiles != null && rawFiles.length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv) ",
					new String[] { "txt", "tsv", "csv" }));

			int returnValue = batchChooser.showOpenDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File batchFile = batchChooser.getSelectedFile();
				Object[][] files = read(batchFile);
				for (int i = 0; i < files.length; i++) {
					String filei = (String) files[i][1];
					if (!fileSet.contains(filei)) {

						fileSet.add(filei);

						model.addRow(new Object[] { (boolean) files[i][0], filei, (String) files[i][2] });

						resultTextField.setText(batchFile.getParentFile().getAbsolutePath() + "\\MetaLab");
					}
				}
				setPattern();
			}
		});

		contentPane.add(btnBatchLoad, "cell 0 1,alignx center");
		contentPane.add(btnAdd, "cell 1 1,alignx center");
		contentPane.add(btnRemove, "cell 2 1,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 0 3 1,grow");

		table = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8095709998037461105L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		Object[][] trans;
		if (rawFiles == null) {
			trans = new Object[0][0];
		} else {
			trans = new Object[rawFiles.length][3];
			for (int i = 0; i < trans.length; i++) {
				File rawi = new File(rawFiles[i]);
				trans[i][0] = rawi.exists();
				trans[i][1] = rawFiles[i];
				trans[i][2] = expNames[i];
			}
		}

		model = new DefaultTableModel(trans, new Object[] { "", "File", "Experiment" });
		table.setModel(model);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(480);
		table.getColumnModel().getColumn(2).setPreferredWidth(180);
		table.getSelectionModel().addListSelectionListener(l -> {
			if (table.getSelectedRowCount() > 0) {
				btnRemove.setEnabled(true);
			} else {
				btnRemove.setEnabled(false);
			}

			if (table.getRowCount() > 0) {
				btnClear.setEnabled(true);
			} else {
				btnClear.setEnabled(false);
			}
		});

		scrollPane.setViewportView(table);

		JPanel resultPanel = new JPanel();
		resultPanel.setBackground(new Color(185, 238, 172));
		resultPanel.setBorder(new TitledBorder(null, "Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(resultPanel, "cell 0 1,grow");
		resultPanel.setLayout(new MigLayout("", "[50,left][15][700, grow 1800][15][50,right]", "[]"));

		JLabel lblOutput = new JLabel("Output");
		resultPanel.add(lblOutput, "cell 0 0,alignx left");

		resultTextField = new JTextField();
		resultPanel.add(resultTextField, "flowx,cell 2 0,growx");
		resultTextField.setColumns(10);
		resultTextField.setText(result);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(l -> {
			JFileChooser fileChooser;
			File resultDir = new File(resultTextField.getText());
			if (resultDir.exists()) {
				fileChooser = new JFileChooser(resultDir);
			} else {
				fileChooser = new JFileChooser(resultDir.getParent());
			}

			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fileChooser.showSaveDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				resultTextField.setText(file.getAbsolutePath());
			}
		});
		resultPanel.add(btnOpen, "cell 4 0,alignx right");

		JPanel expNamePanel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2220413402840507619L;

			public void setEnabled(boolean enabled) {
				Component[] components = getComponents();
				for (Component comp : components) {
					comp.setEnabled(enabled);
					if (comp instanceof JPanel) {
						JPanel childComp = (JPanel) comp;

						Component[] childComponents = childComp.getComponents();
						for (Component c : childComponents) {
							c.setEnabled(enabled);
						}
					}
				}

				patternTable.setEnabled(enabled);
				if (enabled) {
					patternTable.setForeground(Color.BLACK);
					patternTable.getTableHeader().setForeground(Color.BLACK);
				} else {
					patternTable.setForeground(Color.LIGHT_GRAY);
					patternTable.getTableHeader().setForeground(Color.LIGHT_GRAY);
				}
			}
		};
		expNamePanel.setBorder(
				new TitledBorder(null, "Experiment name editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(expNamePanel, "cell 3 0,grow");
		expNamePanel.setLayout(new MigLayout("", "[360,grow 600]", "[280,grow 420][140,grow 210][30]"));

		JPanel pattenPanel = new JPanel();
		pattenPanel.setBorder(
				new TitledBorder(null, "Edit by pattern", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		expNamePanel.add(pattenPanel, "cell 0 0,grow");
		pattenPanel.setLayout(new MigLayout("", "[120,grow 200][120,grow 200][120,grow 200]", "[30][220,grow 360]"));

		JLabel lblSplitSymbol = new JLabel("Split symbol");
		pattenPanel.add(lblSplitSymbol, "cell 0 0,alignx center,aligny center");

		splitSymbolComboBox = new JComboBox<String>();
		splitSymbolComboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "_", "-", "_ and -", "Other" }));
		splitSymbolComboBox.addActionListener(l -> {
			// TODO Auto-generated method stub
			String symbol = (String) splitSymbolComboBox.getSelectedItem();
			if (symbol.equals("Other")) {
				textFieldOther.setEditable(true);
			} else {
				textFieldOther.setEditable(false);
			}
			setPattern();
		});
		pattenPanel.add(splitSymbolComboBox, "cell 1 0,alignx center,aligny center");

		textFieldOther = new JTextField();
		textFieldOther.setEditable(false);
		pattenPanel.add(textFieldOther, "cell 2 0,alignx center,aligny center");
		textFieldOther.setColumns(10);
		textFieldOther.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}
		});

		JScrollPane patternScrollPane = new JScrollPane();
		pattenPanel.add(patternScrollPane, "cell 0 1 3 1,grow");

		patternTable = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1105621242076247506L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		expModel = new DefaultTableModel();
		expModel.setDataVector(new Object[0][2], new Object[] { "Keep in experiment name", "Content" });
		patternTable.setModel(expModel);
		expModel.addTableModelListener(l -> {
			// TODO Auto-generated method stub
			generateExp();
		});

		patternScrollPane.setViewportView(patternTable);
		setPattern();

		JPanel numberPanel = new JPanel();
		numberPanel.setBorder(new TitledBorder(null, "Edit by number of characters", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		expNamePanel.add(numberPanel, "cell 0 1,grow");
		numberPanel.setLayout(new MigLayout("", "[120,grow 200][120,grow 200][120,grow 200]", "[30][30][30][30]"));

		JLabel lblDeletePre = new JLabel("Delete the first ");
		numberPanel.add(lblDeletePre, "cell 0 0,alignx center");

		textFieldFirst = new JTextField();
		numberPanel.add(textFieldFirst, "cell 1 0,growx");
		textFieldFirst.setColumns(10);
		textFieldFirst.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

		});

		JLabel lblCharactersPre = new JLabel("characters");
		numberPanel.add(lblCharactersPre, "cell 2 0,alignx center");

		JLabel lblDeleteNex = new JLabel("Delete the last");
		numberPanel.add(lblDeleteNex, "cell 0 1,alignx center");

		textFieldLast = new JTextField();
		numberPanel.add(textFieldLast, "cell 1 1,growx");
		textFieldLast.setColumns(10);
		textFieldLast.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

		});

		JLabel lblCharactersNex = new JLabel("characters");
		numberPanel.add(lblCharactersNex, "cell 2 1,alignx center");

		JLabel lblDeleteCharBetween = new JLabel("Delete characters between");
		numberPanel.add(lblDeleteCharBetween, "cell 0 2,alignx trailing");

		textFieldStart = new JTextField();
		numberPanel.add(textFieldStart, "cell 1 2,growx");
		textFieldStart.setColumns(10);
		textFieldStart.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}
		});

		textFieldEnd = new JTextField();
		numberPanel.add(textFieldEnd, "cell 2 2,growx");
		textFieldEnd.setColumns(10);
		textFieldEnd.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}
		});

		JButton btnRestore = new JButton("Restore");
		expNamePanel.add(btnRestore, "cell 0 2,alignx center,growy");
		btnRestore.addActionListener(l -> {
			restore();
		});

		if (par.getWorkflowType().equals("") || par.getWorkflowType().equals("")) {
			expNamePanel.setEnabled(true);
		} else {
			expNamePanel.setEnabled(false);
		}
	}

	/**
	 * Create the panel.
	 */
	/**
	 * @param rawFiles
	 * @param result
	 */
	public InputOutputPanel(String[][] rawFiles, String result) {

		setBackground(new Color(185, 238, 172));
		setPreferredSize(new Dimension(1000, 560));
		setLayout(new MigLayout("", "[1000,grow 1600]", "[500][40]"));

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "Raw files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.setLayout(
				new MigLayout("", "[240,grow 450][240,grow 450][240,grow 450][240,grow 450]", "[420,grow][40]"));
		add(contentPane, "cell 0 0,grow");

		this.fileSet = new HashSet<String>();
		if (rawFiles != null) {
			for (int i = 0; i < rawFiles[0].length; i++) {
				this.fileSet.add(rawFiles[0][i]);
			}
		}

		ExpIdDialog expDialog = new ExpIdDialog();
		expDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		expDialog.getOkButton().addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			String[] names = expDialog.getExpNames(selectedRow.length);

			for (int i = 0; i < selectedRow.length; i++) {
				table.setValueAt(names[i], selectedRow[i], 2);
			}

			expDialog.setVisible(false);
		});

		expDialog.getCancelButton().addActionListener(l -> {
			expDialog.setVisible(false);
		});

		JButton btnClear = new JButton("Clear");

		btnClear.addActionListener(l -> {
			fileSet = new HashSet<String>();
			int count = model.getRowCount();
			for (int i = count - 1; i >= 0; i--) {
				model.removeRow(i);
			}
			setPattern();
			btnClear.setEnabled(false);
		});
		contentPane.add(btnClear, "cell 3 1,alignx center");

		if (rawFiles != null && rawFiles[0].length > 0) {
			btnClear.setEnabled(true);
		} else {
			btnClear.setEnabled(false);
		}

		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			for (int i = 0; i < selectedRow.length; i++) {
				int first = table.getSelectedRow();
				String raw = (String) table.getValueAt(first, 1);
				fileSet.remove(raw);
				model.removeRow(first);
			}
			setPattern();
			btnRemove.setEnabled(false);
		});

		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(l -> {

			if (rawFiles != null && rawFiles[0].length > 0) {
				rawFileChooser = new JFileChooser(new File(rawFiles[0][0]));
			} else {
				rawFileChooser = new JFileChooser();
			}
			rawFileChooser.setFileFilter(new FileNameExtensionFilter(
					"raw files (*.raw, *.wiff, *.mzXML) or peptide identification result (.txt for MaxQuant and .xml for X!Tandem)",
					new String[] { "raw", "wiff", "mzXML", "txt", "xml" }));
			rawFileChooser.setMultiSelectionEnabled(true);

			int returnValue = rawFileChooser.showOpenDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = rawFileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					if (!fileSet.contains(file.getAbsolutePath())) {
						String path = file.getAbsolutePath();
						fileSet.add(path);

						String name = file.getName();
						name = name.substring(0, name.lastIndexOf("."));

						model.addRow(new Object[] { file.exists(), path, name });
						resultTextField.setText(file.getParentFile().getAbsolutePath() + "\\MetaLab");
					}
				}
				setPattern();
			}
		});

		JButton btnBatchLoad = new JButton("Batch load");
		btnBatchLoad.addActionListener(l -> {
			if (rawFiles != null && rawFiles[0].length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0][0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv) ",
					new String[] { "txt", "tsv", "csv" }));

			int returnValue = batchChooser.showOpenDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File batchFile = batchChooser.getSelectedFile();
				Object[][] files = read(batchFile);
				for (int i = 0; i < files.length; i++) {
					String filei = (String) files[i][1];
					if (!fileSet.contains(filei)) {

						fileSet.add(filei);

						model.addRow(new Object[] { (boolean) files[i][0], filei, (String) files[i][2] });

						resultTextField.setText(batchFile.getParentFile().getAbsolutePath() + "\\MetaLab");
					}
				}
				setPattern();
			}
		});

		contentPane.add(btnBatchLoad, "cell 0 1,alignx center");
		contentPane.add(btnAdd, "cell 1 1,alignx center");
		contentPane.add(btnRemove, "cell 2 1,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 0 3 1,grow");

		table = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8095709998037461105L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		Object[][] trans;
		if (rawFiles == null) {
			trans = new Object[0][0];
		} else {
			trans = new Object[rawFiles[0].length][3];
			for (int i = 0; i < trans.length; i++) {
				File rawi = new File(rawFiles[0][i]);
				trans[i][0] = rawi.exists();
				trans[i][1] = rawFiles[0][i];
				trans[i][2] = rawFiles[1][i];
			}
		}

		model = new DefaultTableModel(trans, new Object[] { "", "File", "Experiment" });
		table.setModel(model);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(480);
		table.getColumnModel().getColumn(2).setPreferredWidth(180);
		table.getSelectionModel().addListSelectionListener(l -> {
			if (table.getSelectedRowCount() > 0) {
				btnRemove.setEnabled(true);
			} else {
				btnRemove.setEnabled(false);
			}

			if (table.getRowCount() > 0) {
				btnClear.setEnabled(true);
			} else {
				btnClear.setEnabled(false);
			}
		});

		scrollPane.setViewportView(table);

		JPanel resultPanel = new JPanel();
		resultPanel.setBackground(new Color(185, 238, 172));
		resultPanel.setBorder(new TitledBorder(null, "Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(resultPanel, "cell 0 1,grow");
		resultPanel.setLayout(new MigLayout("", "[50,left][15][700, grow 1800][15][50,right]", "[]"));

		JLabel lblOutput = new JLabel("Output");
		resultPanel.add(lblOutput, "cell 0 0,alignx left");

		resultTextField = new JTextField();
		resultPanel.add(resultTextField, "flowx,cell 2 0,growx");
		resultTextField.setColumns(10);
		resultTextField.setText(result);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(resultTextField.getText());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fileChooser.showSaveDialog(InputOutputPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				resultTextField.setText(file.getAbsolutePath());
			}
		});
		resultPanel.add(btnOpen, "cell 4 0,alignx right");

		JPanel expNamePanel = new JPanel();
		expNamePanel.setBorder(
				new TitledBorder(null, "Experiment name editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(expNamePanel, "cell 3 0,grow");
		expNamePanel.setLayout(new MigLayout("", "[360,grow 600]", "[280,grow 420][140,grow 210][30]"));

		JPanel pattenPanel = new JPanel();
		pattenPanel.setBorder(
				new TitledBorder(null, "Edit by pattern", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		expNamePanel.add(pattenPanel, "cell 0 0,grow");
		pattenPanel.setLayout(new MigLayout("", "[120,grow 200][120,grow 200][120,grow 200]", "[30][220,grow 360]"));

		JLabel lblSplitSymbol = new JLabel("Split symbol");
		pattenPanel.add(lblSplitSymbol, "cell 0 0,alignx center,aligny center");

		splitSymbolComboBox = new JComboBox<String>();
		splitSymbolComboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "_", "-", "_ and -", "Other" }));
		splitSymbolComboBox.addActionListener(l -> {
			// TODO Auto-generated method stub
			String symbol = (String) splitSymbolComboBox.getSelectedItem();
			if (symbol.equals("Other")) {
				textFieldOther.setEditable(true);
			} else {
				textFieldOther.setEditable(false);
			}
			setPattern();
		});
		pattenPanel.add(splitSymbolComboBox, "cell 1 0,alignx center,aligny center");

		textFieldOther = new JTextField();
		textFieldOther.setEditable(false);
		pattenPanel.add(textFieldOther, "cell 2 0,alignx center,aligny center");
		textFieldOther.setColumns(10);
		textFieldOther.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setPattern();
			}
		});

		JScrollPane patternScrollPane = new JScrollPane();
		pattenPanel.add(patternScrollPane, "cell 0 1 3 1,grow");

		patternTable = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1105621242076247506L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		expModel = new DefaultTableModel();
		expModel.setDataVector(new Object[0][2], new Object[] { "Keep in experiment name", "Content" });
		patternTable.setModel(expModel);
		expModel.addTableModelListener(l -> {
			// TODO Auto-generated method stub
			generateExp();
		});

		patternScrollPane.setViewportView(patternTable);
		setPattern();

		JPanel numberPanel = new JPanel();
		numberPanel.setBorder(new TitledBorder(null, "Edit by number of characters", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		expNamePanel.add(numberPanel, "cell 0 1,grow");
		numberPanel.setLayout(new MigLayout("", "[120,grow 200][120,grow 200][120,grow 200]", "[30][30][30][30]"));

		JLabel lblDeletePre = new JLabel("Delete the first ");
		numberPanel.add(lblDeletePre, "cell 0 0,alignx center");

		textFieldFirst = new JTextField();
		numberPanel.add(textFieldFirst, "cell 1 0,growx");
		textFieldFirst.setColumns(10);
		textFieldFirst.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimFirst();
			}

		});

		JLabel lblCharactersPre = new JLabel("characters");
		numberPanel.add(lblCharactersPre, "cell 2 0,alignx center");

		JLabel lblDeleteNex = new JLabel("Delete the last");
		numberPanel.add(lblDeleteNex, "cell 0 1,alignx center");

		textFieldLast = new JTextField();
		numberPanel.add(textFieldLast, "cell 1 1,growx");
		textFieldLast.setColumns(10);
		textFieldLast.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trimLast();
			}

		});

		JLabel lblCharactersNex = new JLabel("characters");
		numberPanel.add(lblCharactersNex, "cell 2 1,alignx center");

		JLabel lblDeleteCharBetween = new JLabel("Delete characters between");
		numberPanel.add(lblDeleteCharBetween, "cell 0 2,alignx trailing");

		textFieldStart = new JTextField();
		numberPanel.add(textFieldStart, "cell 1 2,growx");
		textFieldStart.setColumns(10);
		textFieldStart.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}
		});

		textFieldEnd = new JTextField();
		numberPanel.add(textFieldEnd, "cell 2 2,growx");
		textFieldEnd.setColumns(10);
		textFieldEnd.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				trim();
			}
		});

		JButton btnRestore = new JButton("Restore");
		expNamePanel.add(btnRestore, "cell 0 2,alignx center,growy");
		btnRestore.addActionListener(l -> {
			restore();
		});
	}

	private void trimFirst() {
		String first = textFieldFirst.getText();
		if (NumberUtils.isNumber(first)) {
			int firstCount = Integer.parseInt(first);
			int rowCount = model.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				String exp = (String) model.getValueAt(i, 2);
				if (firstCount >= 0 && exp.length() > firstCount) {
					exp = exp.substring(firstCount);
				}
				this.model.setValueAt(exp, i, 2);
			}
		}
	}

	private void trimLast() {
		String last = textFieldLast.getText();
		if (NumberUtils.isNumber(last)) {
			int lastCount = Integer.parseInt(last);
			int rowCount = model.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				String exp = (String) model.getValueAt(i, 2);
				if (lastCount >= 0 && exp.length() > lastCount) {
					exp = exp.substring(0, exp.length() - lastCount);
				}
				this.model.setValueAt(exp, i, 2);
			}
		}
	}

	private void trim() {
		String start = textFieldStart.getText();
		String end = textFieldEnd.getText();

		if (NumberUtils.isNumber(start) && NumberUtils.isNumber(end)) {
			int startCount = Integer.parseInt(start);
			int endCount = Integer.parseInt(end);
			int rowCount = model.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				String exp = (String) model.getValueAt(i, 2);
				if (startCount >= 0 && endCount < exp.length() && startCount < endCount) {
					exp = exp.substring(0, startCount) + exp.substring(endCount);
				}
				this.model.setValueAt(exp, i, 2);
			}
		}
	}

	private void setPattern() {

		synchronized (this) {
			if (patternTableUpdate) {
				return;
			}
			patternTableUpdate = true;

			int rowCount = model.getRowCount();
			if (rowCount == 0) {
				Object[][] contents = new Object[0][2];
				expModel.setDataVector(contents, new String[] { "Keep in experiment name", "Content" });
			} else {
				String name = (String) model.getValueAt(0, 2);
				String symbol = (String) splitSymbolComboBox.getSelectedItem();
				if (symbol.equals("Other")) {
					pattern = textFieldOther.getText();
				} else if (symbol.equals("_ and -")) {
					pattern = "[_-]";
				} else {
					pattern = symbol;
				}

				if (pattern.length() > 0) {
					String[] cs = name.split(pattern);
					Object[][] contents = new Object[cs.length][2];
					for (int i = 0; i < contents.length; i++) {
						contents[i][0] = true;
						contents[i][1] = cs[i];
					}
					expModel.setDataVector(contents, new String[] { "Keep in experiment name", "Content" });
				}
			}

			patternTableUpdate = false;
		}
	}

	private void generateExp() {
		int patternRowCount = this.expModel.getRowCount();
		boolean[] combine = new boolean[patternRowCount];
		for (int i = 0; i < patternRowCount; i++) {
			combine[i] = (boolean) this.expModel.getValueAt(i, 0);
		}

		String[] exps = getOriginalRawFiles()[1];
		for (int i = 0; i < exps.length; i++) {
			String[] cs = exps[i].split(pattern);
			if (cs.length == combine.length) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < cs.length; j++) {
					if (combine[j]) {
						sb.append(cs[j]).append("_");
					}
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				this.model.setValueAt(sb.toString(), i, 2);
			}
		}
	}

	private void restore() {
		String[][] originalRawFiles = getOriginalRawFiles();
		for (int i = 0; i < originalRawFiles[0].length; i++) {
			model.setValueAt(true, i, 0);
			model.setValueAt(originalRawFiles[0][i], i, 1);
			model.setValueAt(originalRawFiles[1][i], i, 2);
		}
		setPattern();

		textFieldFirst.setText("");
		textFieldLast.setText("");
		textFieldStart.setText("");
		textFieldEnd.setText("");
	}

	private Object[][] read(File in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading input information file " + in, e);
		}
		String line = null;
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("[,\t]");
				if (cs.length == 1) {
					String path = cs[0];
					if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
						path = path.substring(1, path.length() - 1);
					}
					File filei = new File(path);
					String name = filei.getName();
					name = name.substring(0, name.lastIndexOf("."));

					list.add(new Object[] { filei.exists(), path, name });

				} else if (cs.length >= 2) {

					String path = cs[0];
					if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
						path = path.substring(1, path.length() - 1);
					}
					String name = cs[1];
					if (name.length() > 2 && name.startsWith("\"") && name.endsWith("\"")) {
						name = name.substring(1, name.length() - 1);
					}

					File filei = new File(path);
					list.add(new Object[] { filei.exists(), path, name });
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading input information file " + in, e);
		}

		Object[][] files = list.toArray(new Object[list.size()][]);

		return files;
	}

	public String[][] getRawFiles() {
		int rowCount = table.getRowCount();
		ArrayList<String> rawlist = new ArrayList<String>();
		ArrayList<String> namelist = new ArrayList<String>();
		for (int i = 0; i < rowCount; i++) {
			boolean find = (boolean) table.getValueAt(i, 0);
			if (find) {
				String path = (String) table.getValueAt(i, 1);
				String name = (String) table.getValueAt(i, 2);
				rawlist.add(path);
				namelist.add(name);
			}
		}
		String[][] files = new String[2][];
		files[0] = rawlist.toArray(new String[rawlist.size()]);
		files[1] = namelist.toArray(new String[namelist.size()]);

		return files;
	}

	public String[][] getOriginalRawFiles() {
		int rowCount = table.getRowCount();
		ArrayList<String> rawlist = new ArrayList<String>();
		ArrayList<String> namelist = new ArrayList<String>();
		for (int i = 0; i < rowCount; i++) {
			boolean find = (boolean) table.getValueAt(i, 0);
			if (find) {
				String path = (String) table.getValueAt(i, 1);
				String name = (new File(path)).getName();
				name = name.substring(0, name.lastIndexOf("."));
				rawlist.add(path);
				namelist.add(name);
			}
		}
		String[][] files = new String[2][];
		files[0] = rawlist.toArray(new String[rawlist.size()]);
		files[1] = namelist.toArray(new String[namelist.size()]);

		return files;
	}

	public String getResultFile() {
		return resultTextField.getText();
	}

	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("TableRenderDemo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				RawFilesPanel newContentPane = new RawFilesPanel(new String[][] { {} }, "");
				newContentPane.setOpaque(true); // content panes must be opaque
				frame.setContentPane(newContentPane);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}
