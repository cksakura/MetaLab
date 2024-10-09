/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.math.NumberUtils;

import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaDataIO;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.v1.gui.ExpIdDialog;
import net.miginfocom.swing.MigLayout;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabIOPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4587747218653997671L;

	private JTable table;
	private DefaultTableModel model;
	private DefaultTableModel expModel;
	private JFileChooser rawFileChooser;
	private JFileChooser batchChooser;
	private HashSet<String> fileSet;
	private JTextField resultTextField;
	private JTable patternTable;
	
	private JTextField textFieldOther;
	
	private JComboBox<String> splitSymbolComboBox;
	private String pattern;
	private boolean patternTableUpdate;
	
	// Currently not used
	private JTextField textFieldFirst;
	private JTextField textFieldLast;
	private JTextField textFieldStart;
	private JTextField textFieldEnd;
	
	/**
	 * Create the panel.
	 */
	/**
	 * @param rawFiles
	 * @param pFindResult
	 */
	public MetaLabIOPanel(MetaParameter par) {

		MetaData metadata = par.getMetadata();

		String[] rawFiles = metadata.getRawFiles();

		String result = par.getResult();

		setBackground(new Color(185, 238, 172));
		setLayout(new MigLayout("", "[600:1000:1920,grow]", "[580:720:860,grow][40]"));

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "Raw files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.setLayout(new MigLayout("",
				"[50:125:270,grow][50:125:270,grow][50:125:270,grow][50:125:270,grow][50:125:270,grow][50:125:270,grow][200:240:300,grow]",
				"[200,grow 220][200,grow 220][50]"));
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
		});

		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(l -> {

			if (rawFiles != null && rawFiles.length > 0) {
				rawFileChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				rawFileChooser = new JFileChooser();
			}
			rawFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			rawFileChooser.setFileFilter(new FileNameExtensionFilter(
					"raw files (*.raw, *.wiff, *.mzXML, *mzML, *dia, *d) or peptide identification result (.txt for MaxQuant and .xml for X!Tandem)",
					new String[] { "raw", "wiff", "mzML", "mzXML", "txt", "xml", "dia", "d" }));
			rawFileChooser.setMultiSelectionEnabled(true);

			int returnValue = rawFileChooser.showOpenDialog(MetaLabIOPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				
				File[] selectedFiles = rawFileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					if (!fileSet.contains(file.getAbsolutePath())) {
						String path = file.getAbsolutePath();
						fileSet.add(path);

						String name = file.getName();
						name = name.substring(0, name.lastIndexOf("."));

						Object[] objs = new Object[model.getColumnCount()];

						objs[0] = file.exists();
						objs[1] = fileSet.size();
						objs[2] = path;
						objs[3] = name;

						if (objs.length == 5) {
							objs[4] = 0;
						} else if (objs.length == 6) {
							objs[4] = 1;
							objs[5] = 1;
						}

						model.addRow(objs);

						resultTextField.setText(file.getParentFile().getAbsolutePath() + "\\MetaLab");
					}
				}
				setPattern();
			}
		});
		contentPane.add(btnAdd, "cell 0 2,alignx center");

		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			for (int i = 0; i < selectedRow.length; i++) {
				int first = table.getSelectedRow();
				String raw = (String) table.getValueAt(first, 2);
				fileSet.remove(raw);
				model.removeRow(first);
			}

			for (int i = 0; i < model.getRowCount(); i++) {
				model.setValueAt(i + 1, i, 1);
			}

			setPattern();
			btnRemove.setEnabled(false);
		});
		contentPane.add(btnRemove, "cell 1 2,alignx center");

		contentPane.add(btnClear, "cell 2 2,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 0 6 2,grow");

		table = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8095709998037461105L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				case 1:
					return Integer.class;
				case 2:
					return String.class;
				case 3:
					return String.class;
				case 4:
					return Integer.class;
				case 5:
					return Integer.class;
				default:
					return String.class;
				}
			}
		};

		if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow) {
			model = new DefaultTableModel(metadata.getRawExpObjectsMaxquant(), metadata.getRawExpTableTitleMaxquant()) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2271755975878728955L;

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return columnIndex > 2;
				}
			};

			table.setModel(model);
			table.getColumnModel().getColumn(0).setMaxWidth(60);
			table.getColumnModel().getColumn(1).setMaxWidth(60);
			table.getColumnModel().getColumn(2).setPreferredWidth(480);
			table.getColumnModel().getColumn(3).setPreferredWidth(180);
			table.getColumnModel().getColumn(4).setMaxWidth(60);
		} else {
			model = new DefaultTableModel(metadata.getRawExpObjectsFlashLFQ(), metadata.getRawExpTableTitleFlashLFQ()) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2271755975878728955L;

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return columnIndex > 2;
				}
			};

			table.setModel(model);
			table.getColumnModel().getColumn(0).setMaxWidth(60);
			table.getColumnModel().getColumn(1).setMaxWidth(60);
			table.getColumnModel().getColumn(2).setPreferredWidth(480);
			table.getColumnModel().getColumn(3).setPreferredWidth(180);
			table.getColumnModel().getColumn(4).setMaxWidth(60);
			table.getColumnModel().getColumn(5).setMaxWidth(60);
		}

		table.getSelectionModel().addListSelectionListener(l -> {
			if (table.getSelectedRowCount() > 0) {
				btnRemove.setEnabled(true);
			} else {
				btnRemove.setEnabled(false);
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
			int returnValue = fileChooser.showSaveDialog(MetaLabIOPanel.this);
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
//					patternTable.getTableHeader().setForeground(Color.BLACK);
				} else {
					patternTable.setForeground(Color.LIGHT_GRAY);
//					patternTable.getTableHeader().setForeground(Color.LIGHT_GRAY);
				}
			}
		};
		expNamePanel.setBorder(
				new TitledBorder(null, "Experiment name editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(expNamePanel, "cell 6 0 1 3,grow");
		expNamePanel.setLayout(new MigLayout("", "[50px][15px][50px,grow][15px][50:50px]", "[30][150,grow][30]"));

		JLabel lblSplitSymbol = new JLabel("Split symbol");
		expNamePanel.add(lblSplitSymbol, "cell 0 0,alignx center,aligny center");

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
		expNamePanel.add(splitSymbolComboBox, "cell 2 0,alignx center,aligny center");

		textFieldOther = new JTextField();
		textFieldOther.setEditable(false);
		expNamePanel.add(textFieldOther, "cell 4 0,alignx center,aligny center");
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
		expNamePanel.add(patternScrollPane, "cell 0 1 7 1,grow");

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

		patternTable.getColumnModel().getColumn(0).setMinWidth(200);
		patternTable.getColumnModel().getColumn(1).setMaxWidth(40);

		patternScrollPane.setViewportView(patternTable);
		setPattern();

		JButton btnRestore = new JButton("Reset");
		expNamePanel.add(btnRestore, "cell 2 2,alignx center,growy");
		btnRestore.addActionListener(l -> {
			restoreExpNames();
		});

		if (par.getWorkflowType() == MetaLabWorkflowType.TaxonomyAnalysis
				|| par.getWorkflowType() == MetaLabWorkflowType.FunctionalAnnotation) {
			expNamePanel.setEnabled(false);
		} else {
			expNamePanel.setEnabled(true);
		}

		JButton btnLoadExp = new JButton("Load exp info");
		btnLoadExp.addActionListener(l -> {
			if (rawFiles != null && rawFiles.length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv, *xls, *xlsx) ",
					new String[] { "txt", "tsv", "csv", "xls", "xlsx" }));

			int returnValue = batchChooser.showOpenDialog(MetaLabIOPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				MetaData loadMetadata = new MetaData();
				File metaFile = batchChooser.getSelectedFile();
				if (metaFile.getName().endsWith("xlsx")) {
					try {
						loadMetadata = MetaDataIO.readRawExpNameXlsx(metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (metaFile.getName().endsWith("txt") || metaFile.getName().endsWith("csv")) {
					try {
						loadMetadata = MetaDataIO.readRawExpNameTxt(metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Object[][] tableObjs;
				Object[] newTitleObj;

				if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow) {
					tableObjs = loadMetadata.getRawExpObjectsMaxquant();
					newTitleObj = loadMetadata.getRawExpTableTitleMaxquant();
				} else {
					tableObjs = loadMetadata.getRawExpObjectsFlashLFQ();
					newTitleObj = loadMetadata.getRawExpTableTitleFlashLFQ();
				}

				if (tableObjs.length > 0) {

					model = new DefaultTableModel(tableObjs, newTitleObj) {
						/**
						 * 
						 */
						private static final long serialVersionUID = 2271755975878728955L;

						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnIndex > 2;
						}
					};
					table.setModel(model);

					if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow) {
						table.getColumnModel().getColumn(0).setMaxWidth(60);
						table.getColumnModel().getColumn(1).setMaxWidth(60);
						table.getColumnModel().getColumn(2).setPreferredWidth(480);
						table.getColumnModel().getColumn(3).setPreferredWidth(180);
						table.getColumnModel().getColumn(4).setMaxWidth(60);
					} else {
						table.getColumnModel().getColumn(0).setMaxWidth(60);
						table.getColumnModel().getColumn(1).setMaxWidth(60);
						table.getColumnModel().getColumn(2).setPreferredWidth(480);
						table.getColumnModel().getColumn(3).setPreferredWidth(180);
						table.getColumnModel().getColumn(4).setMaxWidth(60);
						table.getColumnModel().getColumn(5).setMaxWidth(60);
					}

					fileSet = new HashSet<String>();
					String[] files = metadata.getRawFiles();
					for (int i = 0; i < files.length; i++) {
						fileSet.add(files[i]);
					}

					setPattern();
				}
			}
		});

		JButton btnExpTsv = new JButton("Edit exp in tsv");
		btnExpTsv.addActionListener(l -> {
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*tsv) ", new String[] { "tsv" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabIOPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("tsv")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".tsv");
				}

				try {
					MetaDataIO.exportRawExpNameTsv(getMetaData(), metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JButton btnExpXlsx = new JButton("Edit exp in xlsx");
		btnExpXlsx.addActionListener(l -> {
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*xlsx) ", new String[] { "xlsx" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabIOPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("xlsx")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".xlsx");
				}

				try {
					MetaDataIO.exportRawExpNameXlsx(getMetaData(), metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		contentPane.add(btnExpXlsx, "cell 3 2,alignx center");
		contentPane.add(btnExpTsv, "cell 4 2,alignx center");
		contentPane.add(btnLoadExp, "cell 5 2,alignx center");
	}

	@SuppressWarnings("unused")
	private void trimFirst() {
		String first = textFieldFirst.getText();
		int firstCount = NumberUtils.toInt(first, -1);
		int rowCount = model.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String exp = (String) model.getValueAt(i, 2);
			if (firstCount >= 0 && exp.length() > firstCount) {
				exp = exp.substring(firstCount);
			}
			this.model.setValueAt(exp, i, 2);
		}
	}

	@SuppressWarnings("unused")
	private void trimLast() {
		String last = textFieldLast.getText();
		int lastCount = NumberUtils.toInt(last, -1);
		int rowCount = model.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String exp = (String) model.getValueAt(i, 2);
			if (lastCount >= 0 && exp.length() > lastCount) {
				exp = exp.substring(0, exp.length() - lastCount);
			}
			this.model.setValueAt(exp, i, 2);
		}
	}

	@SuppressWarnings("unused")
	private void trim() {
		String start = textFieldStart.getText();
		String end = textFieldEnd.getText();

		int startCount = NumberUtils.toInt(start, -1);
		int endCount = NumberUtils.toInt(end, -1);
		int rowCount = model.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String exp = (String) model.getValueAt(i, 2);
			if (startCount >= 0 && endCount < exp.length() && startCount < endCount) {
				exp = exp.substring(0, startCount) + exp.substring(endCount);
			}
			this.model.setValueAt(exp, i, 2);
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
				String name = (String) model.getValueAt(0, 3);
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

		String[] exps = this.getExpFromRawName();
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
				this.model.setValueAt(sb.toString(), i, 3);
			}
		}
	}

	private void restoreExpNames() {
		String[] originalExpNames = this.getExpFromRawName();
		for (int i = 0; i < originalExpNames.length; i++) {
			model.setValueAt(originalExpNames[i], i, 3);
		}
		setPattern();
	}

	public MetaData getMetaData() {

		int rowCount = model.getRowCount();
		String[] rawFiles = new String[rowCount];
		String[] expNames = new String[rowCount];
		int[] fractions = new int[rowCount];
		int[] replicates = new int[rowCount];

		for (int i = 0; i < rowCount; i++) {
			rawFiles[i] = (String) model.getValueAt(i, 2);
			expNames[i] = (String) model.getValueAt(i, 3);
			fractions[i] = (Integer) model.getValueAt(i, 4);
			if (model.getColumnCount() == 6) {
				replicates[i] = (Integer) model.getValueAt(i, 5);
			}
		}

		MetaData md = new MetaData(rawFiles, expNames, fractions, replicates);

		return md;
	}

	public String[] getExpFromRawName() {
		int rowCount = model.getRowCount();
		String[] expNames = new String[rowCount];
		for (int i = 0; i < rowCount; i++) {
			String path = (String) model.getValueAt(i, 2);
			String name = (new File(path)).getName();
			name = name.substring(0, name.lastIndexOf("."));
			expNames[i] = name;
		}

		return expNames;
	}

	public String getResultFile() {
		return resultTextField.getText();
	}

}
