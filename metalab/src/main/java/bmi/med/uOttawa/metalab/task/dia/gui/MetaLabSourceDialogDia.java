package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesIoDia;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabSourceDialogMag;

public class MetaLabSourceDialogDia extends MetaLabSourceDialogMag {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569837193042757220L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			MetaLabSourceDialogDia dialog = new MetaLabSourceDialogDia(MetaSourcesIoDia.parse(args[0]),
					new File(args[0]));
			dialog.setAlwaysOnTop(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MetaLabSourceDialogDia(MetaSourcesDia msv, File path) {
		super(msv, path, null);
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabSourceDialogDia.class.getResource("/bmi/med/uOttawa/metalab/icon/dia_128.png")));
	}

	/**
	 * Create the dialog.
	 */
	public MetaLabSourceDialogDia(MetaSourcesDia msv, File path, MetaLabMainFrameDia mainFrame) {
		super(msv, path, mainFrame);
	}

	protected void initialTable() {

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollPane, "cell 1 3 3 1,grow");
		{
			table = new JTable();
			scrollPane.setViewportView(table);

			objs = new Object[4][];

			String funcDef;
			File funcDefFile = new File(msv.getFuncDef());
			if (funcDefFile.exists()) {
				funcDef = funcDefFile.getAbsolutePath();
			} else {
				funcDef = msv.getResource() + "\\function\\func_def.db";
			}
			msv.setFuncDef(funcDef);
			funcDefFile = new File(msv.getFuncDef());

			String diaNN;
			File diaNNFile = new File(((MetaSourcesDia) msv).getDiann());
			if (diaNNFile.exists()) {
				diaNN = diaNNFile.getAbsolutePath();
			} else {
				diaNN = msv.getResource() + "\\fragpipe\\tools\\diann\\1.8.2_beta_8\\win\\DiaNN.exe";
			}
			((MetaSourcesDia) msv).setDiann(diaNN);
			diaNNFile = new File(((MetaSourcesDia) msv).getDiann());

			String deepDetect;
			File deepDetectFile = new File(((MetaSourcesDia) msv).getDeepDetect());
			if (deepDetectFile.exists()) {
				deepDetect = deepDetectFile.getAbsolutePath();
			} else {
				deepDetect = msv.getResource() + "\\DeepDetect\\DeepDetect.exe";
			}
			((MetaSourcesDia) msv).setDeepDetect(deepDetect);
			deepDetectFile = new File(((MetaSourcesDia) msv).getDeepDetect());

			String python;
			File pythonFile = new File(((MetaSourcesDia) msv).getPython());
			if (pythonFile.exists()) {
				python = pythonFile.getAbsolutePath();
			} else {
				python = msv.getResource() + "\\tf\\python.exe";
			}
			((MetaSourcesDia) msv).setPython(python);
			pythonFile = new File(((MetaSourcesDia) msv).getPython());

			objs[0] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

			objs[1] = new Object[] { "DIA-NN command line DiaNN.exe", diaNN, diaNNFile.exists(), "..." };

			objs[2] = new Object[] { "DeepDetect command line DeepDetect.exe", deepDetect, deepDetectFile.exists(),
					"..." };

			objs[3] = new Object[] { "Python command line python.exe, Tensorflow is required", python,
					pythonFile.exists(), "..." };

			model = new DefaultTableModel(objs, new Object[] { "Name", "Path", "Status", "Browse" }) {

				/**
				 * 
				 */
				private static final long serialVersionUID = -2923585948369719422L;

				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 0:
						return String.class;
					case 1:
						return String.class;
					case 2:
						return Boolean.class;
					case 3:
						return JButton.class;
					default:
						return Object.class;
					}
				}

				public boolean isCellEditable(int row, int column) {
					if (column == 3) {
						return true;
					} else {
//						return false;
						return true;
					}
				}
			};

			table.setModel(model);

			TableColumn nameColumn = table.getColumn("Name");
			nameColumn.setMaxWidth(400);
			nameColumn.setPreferredWidth(300);
			nameColumn.setCellRenderer(new JLabelRenderer01());

			TableColumn pathColumn = table.getColumn("Path");
			pathColumn.setMaxWidth(500);
			pathColumn.setPreferredWidth(400);
			pathColumn.setCellRenderer(new JLabelRenderer01());

			TableColumn statusColumn = table.getColumn("Status");
			statusColumn.setMaxWidth(75);
			statusColumn.setPreferredWidth(50);
			statusColumn.setCellRenderer(new JCheckBoxRenderer2());

			TableColumn buttonColumn = table.getColumn("Browse");
			buttonColumn.setMaxWidth(75);
			buttonColumn.setPreferredWidth(50);
			buttonColumn.setCellRenderer(new ButtonRenderer());
			buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox(), model));
		}
	}

	protected void setSources(String sources) {

		objs = new Object[4][];

		msv.setResource(sources);

		String funcDef = sources + "\\function\\func_def.db";
		File funcDefFile = new File(funcDef);
		if (funcDefFile.exists()) {
			funcDef = funcDefFile.getAbsolutePath();
		}
		msv.setFuncDef(funcDef);
		funcDefFile = new File(msv.getFuncDef());

		String diaNN = sources + "\\fragpipe\\tools\\diann\\1.8.2_beta_8\\win\\DiaNN.exe";
		File diaNNFile = new File(diaNN);
		if (diaNNFile.exists()) {
			diaNN = diaNNFile.getAbsolutePath();
		}
		((MetaSourcesDia) msv).setDiann(diaNN);
		diaNNFile = new File(((MetaSourcesDia) msv).getDiann());

		String deepDetect = sources + "\\DeepDetect\\DeepDetect.exe";
		File deepDetectFile = new File(deepDetect);
		if (deepDetectFile.exists()) {
			deepDetect = deepDetectFile.getAbsolutePath();
		}
		((MetaSourcesDia) msv).setDeepDetect(deepDetect);
		deepDetectFile = new File(((MetaSourcesDia) msv).getDeepDetect());

		String python = sources + "\\tf\\python.exe";
		File pythonFile = new File(python);
		if (pythonFile.exists()) {
			python = pythonFile.getAbsolutePath();
		}
		((MetaSourcesDia) msv).setPython(python);
		pythonFile = new File(((MetaSourcesDia) msv).getPython());

		objs[0] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

		objs[1] = new Object[] { "DIA-NN", diaNN, diaNNFile.exists(), "..." };

		objs[2] = new Object[] { "DeepDetect", deepDetect, deepDetectFile.exists(), "..." };

		objs[3] = new Object[] { "python", python, pythonFile.exists(), "..." };

		model = new DefaultTableModel(objs, new Object[] { "Name", "Path", "Status", "Browse" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2923585948369719422L;

			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return String.class;
				case 1:
					return String.class;
				case 2:
					return Boolean.class;
				case 3:
					return JButton.class;
				default:
					return Object.class;
				}
			}

			public boolean isCellEditable(int row, int column) {
				if (column == 3) {
					return true;
				} else {
					// return false;
					return true;
				}
			}
		};

		model.setDataVector(objs, new Object[] { "Name", "Path", "Status", "Browse" });
		table.setModel(model);

		TableColumn nameColumn = table.getColumn("Name");
		nameColumn.setMaxWidth(400);
		nameColumn.setPreferredWidth(300);
		nameColumn.setCellRenderer(new JLabelRenderer01());

		TableColumn pathColumn = table.getColumn("Path");
		pathColumn.setMaxWidth(500);
		pathColumn.setPreferredWidth(400);
		pathColumn.setCellRenderer(new JLabelRenderer01());

		TableColumn statusColumn = table.getColumn("Status");
		statusColumn.setMaxWidth(75);
		statusColumn.setPreferredWidth(50);
		statusColumn.setCellRenderer(new JCheckBoxRenderer2());

		TableColumn buttonColumn = table.getColumn("Browse");
		buttonColumn.setMaxWidth(75);
		buttonColumn.setPreferredWidth(50);
		buttonColumn.setCellRenderer(new ButtonRenderer());
		buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox(), model));
	}

	class JLabelRenderer01 extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9073527193009963446L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// TODO Auto-generated method stub

			boolean find = (Boolean) table.getValueAt(row, 2);

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);

			if (find) {
				label.setBackground(green);
				label.setOpaque(true);
			} else {
				label.setBackground(red);
				label.setOpaque(true);
			}
			return label;
		}
	}

	class JCheckBoxRenderer2 extends JCheckBox implements TableCellRenderer, ItemListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3225963073830560746L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// TODO Auto-generated method stub
			boolean find = (Boolean) table.getValueAt(row, 2);
			if (find) {
				((JCheckBox) this).setBackground(green);
				((JCheckBox) this).setOpaque(true);
			} else {
				((JCheckBox) this).setBackground(red);
				((JCheckBox) this).setOpaque(true);
			}

			setSelected((value != null && ((Boolean) value).booleanValue()));

			return this;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub

		}
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5521092823006780486L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			boolean find = (Boolean) table.getValueAt(row, 2);
			if (find) {
				((JButton) this).setBackground(green);
				((JButton) this).setOpaque(true);
			} else {
				((JButton) this).setBackground(red);
				((JButton) this).setOpaque(true);
			}
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1128088293810062433L;

		protected JButton button;

		public ButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);

			button.addActionListener(l -> {

				int row = table.getSelectedRow();
				JFileChooser fileChooser = new JFileChooser();

				switch (row) {

				case 0:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "Function definition database";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("func_def.db"))
									return true;
								return false;
							}
						}
					});
					break;

				case 1:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "DIA-NN running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("DiaNN.exe") || f.getName().equals("diann.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 2:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "DeepDetect running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("DeepDetect.exe") || f.getName().equals("deepdetect.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 3:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "python running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("python.exe"))
									return true;
								return false;
							}
						}
					});
					break;
				}

				int returnValue = fileChooser.showOpenDialog(MetaLabSourceDialogDia.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();
					model.setValueAt(file.getAbsolutePath(), row, 1);
					model.setValueAt(file.exists(), row, 2);

					model.fireTableDataChanged();

					for (int i = 0; i < model.getRowCount(); i++) {
						String path = (String) model.getValueAt(i, 1);
						switch (i) {
						case 0: {
							msv.setFuncDef(path);
							break;
						}
						case 1: {
							((MetaSourcesDia) msv).setDiann(path);
							break;
						}
						case 2: {
							((MetaSourcesDia) msv).setDeepDetect(path);
							break;
						}
						case 3: {
							((MetaSourcesDia) msv).setPython(path);
							break;
						}
						default: {
							break;
						}
						}
					}
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			/*
			 * if (isSelected) { button.setForeground(table.getSelectionForeground());
			 * button.setBackground(table.getSelectionBackground()); } else {
			 * button.setForeground(table.getForeground());
			 * button.setBackground(table.getBackground()); } String label = (value == null)
			 * ? "" : value.toString(); button.setText(label);
			 */
			return button;
		}

	}
}
