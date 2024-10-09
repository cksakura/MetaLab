package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagPepLib;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;

public class MagDbPepLibPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField pepResultTextField;
	private JTextField pepDbNameTextField;
	private JComboBox<String> proteaseComboBox;
	private JComboBox<Integer> missComboBox;
	private String lastUsedFolder;
	private HashMap<String, MagPepLib> libMap;
	private MagLibTableModel tableModel;
	private MagDbItem magDbItem;
	private JTable libTable;
	private final String[] columnNames = { "Select", "Name", "Peptide count", "Protease", "Miss cleavages", "Remove library" };
	private Object[][] data;
	private JButton createPepDbButton;
	private JProgressBar progressBar;
	/**
	 * Create the panel.
	 */
	public MagDbPepLibPanel(MagDbItem magDbItem, String[] usedLibs) {
		setBorder(new TitledBorder(null, "Step 1: choose initial libraries", TitledBorder.LEADING, TitledBorder.TOP,
				null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[250:350:450,grow][300:400:480,grow]", "[100:200:300,grow][120:140:160,grow]"));

		lastUsedFolder = System.getProperty("user.home");
		this.magDbItem = magDbItem;
		
		JScrollPane pepLibScrollPane = new JScrollPane();
		add(pepLibScrollPane, "cell 0 0 2 1,grow");

		this.libMap = new HashMap<String, MagPepLib>();
		MagPepLib[] magLibs = magDbItem == null ? new MagPepLib[] {} : magDbItem.getMagPepLibs();

		this.data = new Object[magLibs.length][];

		for (int i = 0; i < magLibs.length; i++) {
			this.libMap.put(magLibs[i].getLibName(), magLibs[i]);
			data[i] = new Object[6];
			boolean use = false;

			if (usedLibs != null && usedLibs.length > 0) {
				for (int j = 0; j < usedLibs.length; j++) {
					if (usedLibs[j].equals(magLibs[i].getLibName())) {
						use = true;
						break;
					}
				}
			}

			data[i][0] = use;
			data[i][1] = magLibs[i].getLibName();
			data[i][2] = magLibs[i].getPepCount();
			data[i][3] = magLibs[i].getProtease();
			data[i][4] = magLibs[i].getMiss();
		}

		this.tableModel = new MagLibTableModel();
		libTable = new JTable(tableModel);
		for (int i = 1; i < 5; i++) {
			libTable.getColumnModel().getColumn(i).setCellRenderer(new CenteredRenderer());
		}
		libTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
		libTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

		pepLibScrollPane.setViewportView(libTable);

		JPanel createPepDbPanel = new JPanel();
		createPepDbPanel.setBorder(new TitledBorder(null, "Create new peptide library", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(createPepDbPanel, "cell 0 1 2 1,grow");
		createPepDbPanel.setLayout(new MigLayout("",
				"[80][80:80][10:100:220,grow][80:80][80:80][10:100:220,grow][80:80][50]", "[30][30][30]"));

		JLabel proDbLabel = new JLabel("Protein database");
		createPepDbPanel.add(proDbLabel, "cell 0 0,alignx left");

		pepResultTextField = new JTextField();
		createPepDbPanel.add(pepResultTextField, "cell 1 0 6 1,growx");
		pepResultTextField.setColumns(10);

		JButton pepResultButton = new JButton("Browse");
		createPepDbPanel.add(pepResultButton, "cell 7 0");
		pepResultButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(lastUsedFolder);

			int result = fileChooser.showOpenDialog(MagDbPepLibPanel.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String filePath = selectedFile.getAbsolutePath();
				pepResultTextField.setText(filePath);
				lastUsedFolder = selectedFile.getParent();
			}
		});

		JLabel proteaseLabel = new JLabel("Protease");
		createPepDbPanel.add(proteaseLabel, "cell 0 1,alignx left");

		proteaseComboBox = new JComboBox<String>();
		createPepDbPanel.add(proteaseComboBox, "cell 1 1,growx");
		proteaseComboBox.addItem("Trypsin/P");
		proteaseComboBox.addItem("Trypsin");
		proteaseComboBox.addItem("Lys-C");
		proteaseComboBox.addItem("Chymotrypsin");
		proteaseComboBox.addItem("AspN");
		proteaseComboBox.addItem("GluC");

		JLabel missLabel = new JLabel("Missed cleavage");
		createPepDbPanel.add(missLabel, "cell 3 1,alignx trailing");

		missComboBox = new JComboBox<Integer>();
		createPepDbPanel.add(missComboBox, "cell 4 1,growx");
		missComboBox.addItem(0);
		missComboBox.addItem(1);
		missComboBox.addItem(2);
		missComboBox.setSelectedIndex(1);
		
		JLabel PepDbNameLabel = new JLabel("Library name");
		createPepDbPanel.add(PepDbNameLabel, "cell 6 1,alignx left");

		pepDbNameTextField = new JTextField();
		createPepDbPanel.add(pepDbNameTextField, "cell 7 1,growx");
		pepDbNameTextField.setColumns(10);

		createPepDbButton = new JButton("Start");
		createPepDbPanel.add(createPepDbButton, "cell 0 2,alignx left");

		progressBar = new JProgressBar();
		createPepDbPanel.add(progressBar, "cell 1 2 7 1,growx");
	}

	public String getPepLibName() {
		return pepDbNameTextField.getText();
	}

	public String getFilePath() {
		return pepResultTextField.getText();
	}

	public JButton getCreatePepDbButton() {
		return createPepDbButton;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public String getProtease() {
		return proteaseComboBox.getItemAt(proteaseComboBox.getSelectedIndex());
	}

	public int getMiss() {
		return missComboBox.getItemAt(missComboBox.getSelectedIndex());
	}

	class MagLibTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5037466165904843364L;

		MagLibTableModel() {
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
				return String.class;
			case 4:
				return Integer.class;
			case 5:
				return JButton.class;
			default:
				return Object.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 0 || col == 5;
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
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    }
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8266811128935303264L;

		public ButtonRenderer() {
			setOpaque(true);
			setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			addActionListener(l -> {
				int row = Integer.valueOf(l.getActionCommand());
				Object[] options = { "Delete the library from the table",
						"Also delete the library files from the hard drive", "cancel" };

				int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to delete it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				String libName = (String) tableModel.getValueAt(row, 1);
				MagPepLib mm = libMap.get(libName);

				switch (choice) {
				case 0:
					magDbItem.removePepLib(mm);
					tableModel.removeRow(row);
					libMap.remove(libName);
					break;
				case 1:
					File modelFolder = new File(new File(magDbItem.getCurrentFile(), "libraries"), libName);
					System.out.println("delete file " + modelFolder);
					mm.delete();
					magDbItem.removePepLib(mm);
					tableModel.removeRow(row);
					libMap.remove(libName);
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
			button.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			button.addActionListener(l -> {
				int row = Integer.valueOf(l.getActionCommand());
				Object[] options = { "Delete the library from the table",
						"Also delete the library files from the hard drive", "cancel" };

				int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to delete it?", "Confirmation",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				String libName = (String) tableModel.getValueAt(row, 1);
				MagPepLib mm = libMap.get(libName);

				switch (choice) {
				case 0:
					magDbItem.removePepLib(mm);
					tableModel.removeRow(row);
					libMap.remove(libName);
					break;
				case 1:
					magDbItem.removePepLib(mm);
					File modelFolder = new File(new File(magDbItem.getCurrentFile(), "libraries"), libName);
					System.out.println("delete file " + modelFolder);
					mm.delete();
					tableModel.removeRow(row);
					libMap.remove(libName);
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
		MagPepLib[] magPepLibs = this.magDbItem.getMagPepLibs();
		for (MagPepLib lib : magPepLibs) {
			String libName = lib.getLibName();
			if (!this.libMap.containsKey(libName)) {
				addNewLib(lib);
				this.libMap.put(libName, lib);
			}
		}
	}

	public void update(MagDbItem magDbItem) {
		if (this.magDbItem==null || !this.magDbItem.equals(magDbItem)) {
			this.magDbItem = magDbItem;
			this.libMap = new HashMap<String, MagPepLib>();
			this.tableModel.setRowCount(0);

			if (magDbItem != null) {
				MagPepLib[] magLibs = this.magDbItem.getMagPepLibs();
				for (MagPepLib ml : magLibs) {
					String libName = ml.getLibName();
					if (!this.libMap.containsKey(libName)) {
						addNewLib(ml);
						this.libMap.put(libName, ml);
					}
				}
			}
		}
	}

	private void addNewLib(MagPepLib pepLib) {

		Object[] objs = new Object[6];
		objs[0] = false;
		objs[1] = pepLib.getLibName();
		objs[2] = pepLib.getPepCount();
		objs[3] = pepLib.getProtease();
		objs[4] = pepLib.getMiss();
		tableModel.addRow(objs);
	}
	
	public String[] getUsedLibs() {
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
