package bmi.med.uOttawa.metalab.task.dia.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JRadioButton;

public class MagDbModelPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable modelTable;
	private JTextField pepResultTextField;
	private JTextField modelNameTextField;
	private String lastUsedFolder;
	private MagModelTableModel tableModel;
	private JButton createModelButton;
	private JProgressBar progressBar;

	private HashMap<String, MagModel> modelMap;
	private MagDbItem magDbItem;
	private final String[] columnNames = { "Select", "Name", "Training set", "Predicting set", "Correlation", "Median",
			"Remove model" };
	private Object[][] data;
	private JLabel lblNNType;
	private JRadioButton rdbtnRNN;
	private JRadioButton rdbtnDFNN;

	/**
	 * Create the panel.
	 */
	public MagDbModelPanel(MagDbItem magDbItem, String[] usedModels) {
		setBorder(new TitledBorder(null, "Step 2: choose models (optional)", TitledBorder.LEADING, TitledBorder.TOP,
				null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[250:350:450,grow][300:400:480,grow]", "[100:200:300,grow][120:140:160,grow]"));

		lastUsedFolder = System.getProperty("user.home");
		this.magDbItem = magDbItem;

		JScrollPane modelScrollPane = new JScrollPane();
		add(modelScrollPane, "cell 0 0 2 1,grow");

		this.modelMap = new HashMap<String, MagModel>();
		MagModel[] magModels = magDbItem == null ? new MagModel[] {} : magDbItem.getMagModels();
		this.data = new Object[magModels.length][];

		for (int i = 0; i < magModels.length; i++) {
			this.modelMap.put(magModels[i].getModelName(), magModels[i]);
			data[i] = new Object[7];
			boolean use = false;

			if (usedModels != null && usedModels.length > 0) {
				for (int j = 0; j < usedModels.length; j++) {
					if (usedModels[j].equals(magModels[i].getModelName())) {
						use = true;
						break;
					}
				}
			}

			data[i][0] = use;
			data[i][1] = magModels[i].getModelName();
			data[i][2] = magModels[i].getTrainSize();
			data[i][3] = magModels[i].getModelSize();
			data[i][4] = magModels[i].getCorr();
			data[i][5] = magModels[i].getMed();
		}

		this.tableModel = new MagModelTableModel();
		modelTable = new JTable(tableModel);
		for (int i = 1; i < 6; i++) {
			modelTable.getColumnModel().getColumn(i).setCellRenderer(new CenteredRenderer());
		}
		modelTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
		modelTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
		modelScrollPane.setViewportView(modelTable);

		JPanel createModelPanel = new JPanel();
		createModelPanel.setBorder(
				new TitledBorder(null, "Create new model", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(createModelPanel, "cell 0 1 2 1,grow");
		createModelPanel.setLayout(new MigLayout("", "[120][180:400:800,grow][50]", "[30][30][30][30]"));

		JLabel pepIdenLabel = new JLabel("Peptide result");
		createModelPanel.add(pepIdenLabel, "cell 0 0,alignx left");

		pepResultTextField = new JTextField();
		createModelPanel.add(pepResultTextField, "cell 1 0,growx");
		pepResultTextField.setColumns(10);

		JButton pepResultButton = new JButton("Browse");
		createModelPanel.add(pepResultButton, "cell 2 0");
		pepResultButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(lastUsedFolder);

			int result = fileChooser.showOpenDialog(MagDbModelPanel.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String filePath = selectedFile.getAbsolutePath();
				pepResultTextField.setText(filePath);
				lastUsedFolder = selectedFile.getParent();
			}
		});

		JLabel modelNameLabel = new JLabel("Model name");
		createModelPanel.add(modelNameLabel, "cell 0 1,alignx left");

		modelNameTextField = new JTextField();
		createModelPanel.add(modelNameTextField, "cell 1 1,growx");
		modelNameTextField.setColumns(10);

		lblNNType = new JLabel("Neural Network type");
		createModelPanel.add(lblNNType, "cell 0 2");

		rdbtnRNN = new JRadioButton("RNN");
		rdbtnRNN.setSelected(true);
		createModelPanel.add(rdbtnRNN, "flowx,cell 1 2");

		rdbtnDFNN = new JRadioButton("DFNN");
		createModelPanel.add(rdbtnDFNN, "cell 1 2");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdbtnRNN);
		buttonGroup.add(rdbtnDFNN);

		createModelButton = new JButton("Start");
		createModelPanel.add(createModelButton, "cell 0 3,alignx left");

		progressBar = new JProgressBar();
		createModelPanel.add(progressBar, "cell 1 3 2 1,growx");
	}

	public String getModelName() {
		return modelNameTextField.getText();
	}

	public String getFilePath() {
		return pepResultTextField.getText();
	}

	public JButton getCreateModelButton() {
		return createModelButton;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	class MagModelTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5037466165904843364L;

		MagModelTableModel() {
			super(data, columnNames);
		}

		@Override
		public Class<?> getColumnClass(int col) {
			switch (col) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			case 2:
				return Integer.class;
			case 3:
				return Integer.class;
			case 4:
				return Double.class;
			case 5:
				return Double.class;
			case 6:
				return JButton.class;
			default:
				return Object.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 0 || col == 6;
		}
	}

	class CenteredRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1755085907847890501L;

		public CenteredRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5806334761538883427L;

		ButtonRenderer() {
			setOpaque(true);
			setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			addActionListener(l -> {
				int row = Integer.valueOf(l.getActionCommand());
				Object[] options = { "Delete the model from the table",
						"Also delete the model files from the hard drive", "cancel" };

				int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to delete it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				String modelName = (String) tableModel.getValueAt(row, 1);
				switch (choice) {
				case 0:
					tableModel.removeRow(row);
					magDbItem.removeModel(modelMap.get(modelName));
					modelMap.remove(modelName);
					break;
				case 1:
					MagModel mm = modelMap.get(modelName);
					mm.delete();
					magDbItem.removeModel(mm);
					tableModel.removeRow(row);
					modelMap.remove(modelName);
					break;
				case 2:
					break;
				default:
					break;
				}
			});
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
			setActionCommand(String.valueOf(row));
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7668273797953453790L;
		private JButton button;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(l -> {
				Object[] options = { "Delete the model from the table",
						"Also delete the model files from the hard drive", "cancel" };
				int row = Integer.valueOf(l.getActionCommand());

				int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to delete it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				String modelName = (String) tableModel.getValueAt(row, 1);
				switch (choice) {
				case 0:
					tableModel.removeRow(row);
					magDbItem.removeModel(modelMap.get(modelName));
					modelMap.remove(modelName);
					break;
				case 1:
					MagModel mm = modelMap.get(modelName);
					mm.delete();
					tableModel.removeRow(row);
					magDbItem.removeModel(mm);
					modelMap.remove(modelName);
					break;
				case 2:
					break;
				default:
					break;
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			button.setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
			button.setActionCommand(String.valueOf(row));
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return Boolean.TRUE;
		}
	}

	public void update() {
		if (this.magDbItem != null) {
			MagModel[] magModels = this.magDbItem.getMagModels();
			for (MagModel mm : magModels) {
				String modelName = mm.getModelName();
				if (!this.modelMap.containsKey(modelName)) {
					addNewModel(mm);
					this.modelMap.put(modelName, mm);
				}
			}
		}
	}

	public void update(MagDbItem magDbItem) {
		if (this.magDbItem == null || !this.magDbItem.equals(magDbItem)) {
			this.magDbItem = magDbItem;
			this.modelMap = new HashMap<String, MagModel>();
			this.tableModel.setRowCount(0);

			if (magDbItem != null) {
				MagModel[] magModels = this.magDbItem.getMagModels();
				for (MagModel mm : magModels) {
					String modelName = mm.getModelName();
					if (!this.modelMap.containsKey(modelName)) {
						addNewModel(mm);
						this.modelMap.put(modelName, mm);
					}
				}
			}
		}
	}

	private void addNewModel(MagModel model) {

		Object[] objs = new Object[6];
		objs[0] = false;
		objs[1] = model.getModelName();
		objs[2] = model.getTrainSize();
		objs[3] = model.getModelSize();
		objs[4] = model.getCorr();
		objs[5] = model.getMed();
		tableModel.addRow(objs);
	}

	public String[] getUsedModels() {
		ArrayList<String> list = new ArrayList<String>();
		int rowCount = this.tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			boolean select = (boolean) this.tableModel.getValueAt(i, 0);
			if (select) {
				list.add((String) this.tableModel.getValueAt(i, 1));
			}
		}

		return list.toArray(String[]::new);
	}
}
