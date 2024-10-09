package bmi.med.uOttawa.metalab.task.hgm.gui;

import java.awt.Component;
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

import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesIoHGM;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSourcePathDialog;

public class MetaLabSourceDialogHGM extends MetaLabSourcePathDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -122504279827839583L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			MetaLabSourceDialogHGM dialog = new MetaLabSourceDialogHGM(MetaSourcesIoHGM.parse(args[0]),
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
	public MetaLabSourceDialogHGM(MetaSourcesHGM msv, File path) {
		this(msv, path, null);
	}

	/**
	 * Create the dialog.
	 */
	public MetaLabSourceDialogHGM(MetaSourcesHGM msv, File path, MetaLabMainFrameHGM mainFrame) {
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
			
			String function;
			File functionFile = new File(msv.getFunction());
			if (functionFile.exists()) {
				function = functionFile.getAbsolutePath();
			} else {
				function = msv.getResource() + "\\function\\UHGG_func_anno.db";
			}
			msv.setFunction(function);
			functionFile = new File(msv.getFunction());

			String funcDef;
			File funcDefFile = new File(msv.getFuncDef());
			if (funcDefFile.exists()) {
				funcDef = funcDefFile.getAbsolutePath();
			} else {
				funcDef = msv.getResource() + "\\function\\func_def.db";
			}
			msv.setFuncDef(funcDef);
			funcDefFile = new File(msv.getFuncDef());

			String pfind;
			File pfindFile = new File(msv.getpFind());
			if (pfindFile.exists()) {
				pfind = pfindFile.getAbsolutePath();
			} else {
				pfind = msv.getResource() + "\\pFind3\\bin\\pFind.exe";
			}
			msv.setpFind(pfind);
			pfindFile = new File(msv.getpFind());

			String flashlfq;
			File lfqFile = new File(msv.getFlashlfq());
			if (lfqFile.exists()) {
				flashlfq = lfqFile.getAbsolutePath();
			} else {
				flashlfq = msv.getResource() + "\\FlashLFQ\\CMD.exe";
			}
			msv.setFlashlfq(flashlfq);
			lfqFile = new File(msv.getFlashlfq());
			
			objs[0] = new Object[] { "Functional annotation database", function, functionFile.exists(), "..." };

			objs[1] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

			objs[2] = new Object[] { "pFind", pfind, pfindFile.exists(), "..." };

			objs[3] = new Object[] { "FlashLFQ", flashlfq, lfqFile.exists(), "..." };

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
		
		String function = sources + "\\function\\UHGG_func_anno.db";
		File functionFile = new File(function);
		if (functionFile.exists()) {
			function = functionFile.getAbsolutePath();
		}
		msv.setFunction(function);
		functionFile = new File(msv.getFunction());

		String funcDef = sources + "\\function\\func_def.db";
		File funcDefFile = new File(funcDef);
		if (funcDefFile.exists()) {
			funcDef = funcDefFile.getAbsolutePath();
		}
		msv.setFuncDef(funcDef);
		funcDefFile = new File(msv.getFuncDef());

		String pfind = sources + "\\pFind3\\bin\\pFind.exe";
		File pfindFile = new File(pfind);
		if (pfindFile.exists()) {
			pfind = pfindFile.getAbsolutePath();
		}
		msv.setpFind(pfind);
		pfindFile = new File(msv.getpFind());

		String flashlfq = sources + "\\FlashLFQ\\CMD.exe";
		File lfqFile = new File(flashlfq);
		if (lfqFile.exists()) {
			flashlfq = lfqFile.getAbsolutePath();
		}
		msv.setFlashlfq(flashlfq);
		lfqFile = new File(msv.getFlashlfq());

		objs[0] = new Object[] { "Functional annotation database", function, functionFile.exists(), "..." };
		
		objs[1] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

		objs[2] = new Object[] { "pFind", pfind, pfindFile.exists(), "..." };

		objs[3] = new Object[] { "FlashLFQ", flashlfq, lfqFile.exists(), "..." };

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
							return "Functional annotation database";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("UHGG_func_anno.db"))
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

				case 2:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "pFind running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("pFind.exe"))
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
							return "FlashLFQ running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("CMD.exe"))
									return true;
								return false;
							}
						}
					});
					break;
				}

				int returnValue = fileChooser.showOpenDialog(MetaLabSourceDialogHGM.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();
					model.setValueAt(file.getAbsolutePath(), row, 1);
					model.setValueAt(file.exists(), row, 2);

					model.fireTableDataChanged();

					for (int i = 0; i < model.getRowCount(); i++) {
						switch (i) {
						case 0: {
							msv.setFunction((String) model.getValueAt(i, 1));
							break;
						}
						case 1: {
							msv.setFuncDef((String) model.getValueAt(i, 1));
							break;
						}
						case 2: {
							msv.setpFind((String) model.getValueAt(i, 1));
							break;
						}
						case 3: {
							msv.setFlashlfq((String) model.getValueAt(i, 1));
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
