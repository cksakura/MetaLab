/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import bmi.med.uOttawa.metalab.core.model.AutoDismissDialog;
import bmi.med.uOttawa.metalab.task.dia.gui.MetaLabMainFrameDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesIoDia;
import bmi.med.uOttawa.metalab.task.hgm.gui.MetaLabMainFrameHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesHGM;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaSourcesIoHGM;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMainFrameMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesIoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import bmi.med.uOttawa.metalab.task.pfind.gui.MetaLabPFindMainFrame;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesIOV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabSourcePathDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8248829828277909619L;
	protected final JPanel contentPanel = new JPanel();
	protected JTextField textField;
	protected JTable table;
	protected Object[][] objs;

	protected Color red = new Color(255, 167, 166);
	protected Color green = new Color(166, 255, 172);
	protected DefaultTableModel model;
	protected JCheckBox chckbxDefault;
	protected MetaSourcesV2 msv;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			MetaLabSourcePathDialog dialog = new MetaLabSourcePathDialog(MetaSourcesIOV2.parse(args[0]), new File(args[0]));
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
	public MetaLabSourcePathDialog(MetaSourcesV2 msv, File path) {
		this(msv, path, null);
	}
	
	/**
	 * Create the dialog.
	 */
	public MetaLabSourcePathDialog(MetaSourcesV2 msv, File path, MetaLabPFindMainFrame mainFrame) {
		this.msv = msv;
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabSourcePathDialog.class.getResource("/bmi/med/uOttawa/metalab/icon/metalab_128.png")));
		setBounds(100, 100, 800, 600);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[30][30][180,grow][30][30]", "[20][30][30][grow]"));
		{
			JLabel lblResource = new JLabel("Resource");
			contentPanel.add(lblResource, "cell 1 1,alignx trailing");
		}
		{
			textField = new JTextField(msv.getResource());
			contentPanel.add(textField, "cell 2 1,growx");
			textField.setColumns(10);
		}
		{
			JButton btnBrowse = new JButton("Browse");
			contentPanel.add(btnBrowse, "cell 3 1");
			btnBrowse.addActionListener(l -> {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.addChoosableFileFilter(new FileFilter() {
					public String getDescription() {
						return "The resources folder containing the following documents and executable programs";
					}

					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						} else {
							return false;
						}
					}
				});

				int returnValue = fileChooser.showOpenDialog(MetaLabSourcePathDialog.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
					chckbxDefault.setSelected(false);

					setSources(file.getAbsolutePath());

					// model.fireTableDataChanged();
				}
			});
		}
		{
			chckbxDefault = new JCheckBox("Default");
			chckbxDefault.setSelected(msv.getResource().equals("Resources\\"));
			contentPanel.add(chckbxDefault, "cell 2 2");
			chckbxDefault.addItemListener(l -> {
				if (chckbxDefault.isSelected()) {
					textField.setText("Resources\\");
					setSources("Resources\\");
				}
			});
		}

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(l -> {

					AutoDismissDialog.showMessageDialog(this, "The parameters are updated successfully.", "Notice",
							2500);

					this.dispose();

					if (mainFrame != null) {

						String version = msv.getVersion();

						if (version.startsWith("HGM")) {
							MetaSourcesIoHGM.export((MetaSourcesHGM) this.msv, path);
						} else if (version.startsWith("MAG")) {
							MetaSourcesIoMag.export((MetaSourcesMag) this.msv, path);
						} else if (version.startsWith("DIA")) {
							MetaSourcesIoDia.export((MetaSourcesDia) this.msv, path);
						} else {
							MetaSourcesIOV2.export(this.msv, path);
						}

						mainFrame.dispose();

						try {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
								| UnsupportedLookAndFeelException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						javax.swing.SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {

									if (version.startsWith("HGM")) {
										MetaLabMainFrameHGM frame = new MetaLabMainFrameHGM();
										frame.setVisible(true);
									} else if (version.startsWith("MAG")) {
										MetaLabMainFrameMag frame = new MetaLabMainFrameMag();
										frame.setVisible(true);
									} else if (version.startsWith("DIA")) {
										MetaLabMainFrameDia frame = new MetaLabMainFrameDia();
										frame.setVisible(true);
									} else {
										MetaLabPFindMainFrame frame = new MetaLabPFindMainFrame();
										frame.setVisible(true);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}

				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(l -> {
					AutoDismissDialog.showMessageDialog(this, "The parameters are not updated.", "Notice", 2500);
					this.dispose();
				});
				buttonPane.add(cancelButton);
			}
		}

		initialTable();
	}
	
	protected void initialTable() {

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(scrollPane, "cell 1 3 3 1,grow");
		{
			table = new JTable();
			scrollPane.setViewportView(table);

			objs = new Object[12][];

			String pep2tax;
			File pep2taxFile = new File(msv.getPep2tax());
			if (pep2taxFile.exists()) {
				pep2tax = pep2taxFile.getAbsolutePath();
			} else {
				File pep2taxaFolder = pep2taxFile.getParentFile();
				File tab = new File(pep2taxaFolder, "pep2taxa.tab");
				if (tab.exists()) {
					pep2tax = tab.getAbsolutePath();
				} else {
					File gz = new File(pep2taxaFolder, "pep2taxa.gz");
					if (gz.exists()) {
						pep2tax = gz.getAbsolutePath();
					} else {
						pep2tax = msv.getResource() + "\\pep2taxa\\pep2taxa.tab";
					}
				}
			}
			msv.setPep2tax(pep2tax);
			pep2taxFile = new File(msv.getPep2tax());

			String function;
			File funcFile = new File(msv.getFunction());
			if (funcFile.exists()) {
				function = funcFile.getAbsolutePath();
			} else {
				function = msv.getResource() + "\\function\\func.db";
			}
			msv.setFunction(function);
			funcFile = new File(msv.getFunction());

			String funcDef;
			File funcDefFile = new File(msv.getFuncDef());
			if (funcDefFile.exists()) {
				funcDef = funcDefFile.getAbsolutePath();
			} else {
				funcDef = msv.getResource() + "\\function\\func_def.db";
			}
			msv.setFuncDef(funcDef);
			funcDefFile = new File(msv.getFuncDef());

			String taxonAll;
			File taxonFile = new File(msv.getTaxonAll());
			if (taxonFile.exists()) {
				taxonAll = taxonFile.getAbsolutePath();
			} else {
				taxonAll = msv.getResource() + "\\taxonomy-all.tab";
			}
			msv.setTaxonAll(taxonAll);
			taxonFile = new File(msv.getTaxonAll());

			String unimod;
			File unimodFile = new File(msv.getUnimod());
			if (unimodFile.exists()) {
				unimod = unimodFile.getAbsolutePath();
			} else {
				unimod = msv.getResource() + "\\unimod.xml";
			}
			msv.setUnimod(unimod);
			unimodFile = new File(msv.getUnimod());

			String maxquant;
			File maxquantFile = new File(msv.getMaxquant());
			if (maxquantFile.exists()) {
				maxquant = maxquantFile.getAbsolutePath();
			} else {
				maxquant = msv.getResource() + "\\mq_bin\\MaxQuantCmd.exe";
			}
			msv.setMaxquant(maxquant);
			maxquantFile = new File(msv.getMaxquant());

			String xtandem;
			File tandemFile = new File(msv.getXtandem());
			if (tandemFile.exists()) {
				xtandem = tandemFile.getAbsolutePath();
			} else {
				xtandem = msv.getResource() + "\\tandem\\bin\\tandem.exe";
			}
			msv.setXtandem(xtandem);
			tandemFile = new File(msv.getXtandem());

			String ProteomicsTools;
			File ptFile = new File(msv.getProteomicsTools());
			if (ptFile.exists()) {
				ProteomicsTools = ptFile.getAbsolutePath();
			} else {
				ProteomicsTools = msv.getResource() + "\\ProteomicsTools\\ProteomicsTools.exe";
			}
			msv.setProteomicsTools(ProteomicsTools);
			ptFile = new File(msv.getProteomicsTools());

			String msconvert;
			File msconvertFile = new File(msv.getMsconvert());
			if (msconvertFile.exists()) {
				msconvert = msconvertFile.getAbsolutePath();
			} else {
				msconvert = msv.getResource() + "\\ProteoWizard\\msconvert.exe";
			}
			msv.setMsconvert(msconvert);
			msconvertFile = new File(msv.getMsconvert());

			String pfind;
			File pfindFile = new File(msv.getpFind());
			if (pfindFile.exists()) {
				pfind = pfindFile.getAbsolutePath();
			} else {
				pfind = msv.getResource() + "\\DBReducer\\pFind3\\bin\\pFind.exe";
			}
			msv.setpFind(pfind);
			pfindFile = new File(msv.getpFind());
			
			String dbReducer;
			File dbReducerFile = new File(msv.getDbReducer());
			if (dbReducerFile.exists()) {
				dbReducer = dbReducerFile.getAbsolutePath();
			} else {
				dbReducer = msv.getResource() + "\\DBReducer\\DBReducer\\DBReducer.exe";
			}
			msv.setDbReducer(dbReducer);
			dbReducerFile = new File(msv.getDbReducer());

			String flashlfq;
			File lfqFile = new File(msv.getFlashlfq());
			if (lfqFile.exists()) {
				flashlfq = lfqFile.getAbsolutePath();
			} else {
				flashlfq = msv.getResource() + "\\FlashLFQ\\CMD.exe";
			}
			msv.setFlashlfq(flashlfq);
			lfqFile = new File(msv.getFlashlfq());

			objs[0] = new Object[] { "Peptide and taxa mapping database (pep2taxa)", pep2tax, pep2taxFile.exists(),
					"..." };

			objs[1] = new Object[] { "Function database", function, funcFile.exists(), "..." };

			objs[2] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

			objs[3] = new Object[] { "Taxonomic tree", taxonAll, taxonFile.exists(), "..." };

			objs[4] = new Object[] { "Protein modifications", unimod, unimodFile.exists(), "..." };

			objs[5] = new Object[] { "MaxQuant", maxquant, maxquantFile.exists(), "..." };

			objs[6] = new Object[] { "X!Tandem", xtandem, tandemFile.exists(), "..." };

			objs[7] = new Object[] { "ProteomicsTools", ProteomicsTools, ptFile.exists(), "..." };

			objs[8] = new Object[] { "MSConvert", msconvert, msconvertFile.exists(), "..." };

			objs[9] = new Object[] { "pFind", pfind, pfindFile.exists(), "..." };
			
			objs[10] = new Object[] { "DBReducer", dbReducer, dbReducerFile.exists(), "..." };

			objs[11] = new Object[] { "FlashLFQ", flashlfq, lfqFile.exists(), "..." };

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

		objs = new Object[12][];

		msv.setResource(sources);

		String pep2tax = sources + "\\pep2taxa\\pep2taxa.tab";
		File pep2taxFile = new File(pep2tax);
		if (pep2taxFile.exists()) {
			pep2tax = pep2taxFile.getAbsolutePath();
		} else {
			File pep2taxaFolder = pep2taxFile.getParentFile();
			File tab = new File(pep2taxaFolder, "pep2taxa.tab");
			if (tab.exists()) {
				pep2tax = tab.getAbsolutePath();
			} else {
				File gz = new File(pep2taxaFolder, "pep2taxa.gz");
				if (gz.exists()) {
					pep2tax = gz.getAbsolutePath();
				}
			}
		}
		msv.setPep2tax(pep2tax);
		pep2taxFile = new File(msv.getPep2tax());

		String function = sources + "\\function\\func.db";
		File funcFile = new File(function);
		if (funcFile.exists()) {
			function = funcFile.getAbsolutePath();
		}
		msv.setFunction(function);
		funcFile = new File(msv.getFunction());

		String funcDef = sources + "\\function\\func_def.db";
		File funcDefFile = new File(funcDef);
		if (funcDefFile.exists()) {
			funcDef = funcDefFile.getAbsolutePath();
		}
		msv.setFuncDef(funcDef);
		funcDefFile = new File(msv.getFuncDef());

		String taxonAll = sources + "\\taxonomy-all.tab";
		File taxonFile = new File(taxonAll);
		if (taxonFile.exists()) {
			taxonAll = taxonFile.getAbsolutePath();
		}
		msv.setTaxonAll(taxonAll);
		taxonFile = new File(msv.getTaxonAll());

		String unimod = sources + "\\unimod.xml";
		File unimodFile = new File(unimod);
		if (unimodFile.exists()) {
			unimod = unimodFile.getAbsolutePath();
		}
		msv.setUnimod(unimod);
		unimodFile = new File(msv.getUnimod());

		String maxquant = sources + "\\mq_bin\\MaxQuantCmd.exe";
		File maxquantFile = new File(maxquant);
		if (maxquantFile.exists()) {
			maxquant = maxquantFile.getAbsolutePath();
		}
		msv.setMaxquant(maxquant);
		maxquantFile = new File(msv.getMaxquant());

		String xtandem = sources + "\\tandem\\bin\\tandem.exe";
		File tandemFile = new File(xtandem);
		if (tandemFile.exists()) {
			xtandem = tandemFile.getAbsolutePath();
		}
		msv.setXtandem(xtandem);
		tandemFile = new File(msv.getXtandem());

		String ProteomicsTools = sources + "\\ProteomicsTools\\ProteomicsTools.exe";
		File ptFile = new File(ProteomicsTools);
		if (ptFile.exists()) {
			ProteomicsTools = ptFile.getAbsolutePath();
		}
		msv.setProteomicsTools(ProteomicsTools);
		ptFile = new File(msv.getProteomicsTools());

		String msconvert = sources + "\\ProteoWizard\\msconvert.exe";
		File msconvertFile = new File(msconvert);
		if (msconvertFile.exists()) {
			msconvert = msconvertFile.getAbsolutePath();
		}
		msv.setMsconvert(msconvert);
		msconvertFile = new File(msv.getMsconvert());

		String pfind = sources + "\\DBReducer\\pFind3\\bin\\pFind.exe";
		File pfindFile = new File(pfind);
		if (pfindFile.exists()) {
			pfind = pfindFile.getAbsolutePath();
		}
		msv.setpFind(pfind);
		pfindFile = new File(msv.getpFind());
		
		String dbReducer = sources + "\\DBReducer\\DBReducer\\DBReducer.exe";
		File dbReducerFile = new File(dbReducer);
		if (dbReducerFile.exists()) {
			dbReducer = dbReducerFile.getAbsolutePath();
		}
		msv.setDbReducer(dbReducer);
		dbReducerFile = new File(msv.getDbReducer());

		String flashlfq = sources + "\\FlashLFQ\\CMD.exe";
		File lfqFile = new File(flashlfq);
		if (lfqFile.exists()) {
			flashlfq = lfqFile.getAbsolutePath();
		}
		msv.setFlashlfq(flashlfq);
		lfqFile = new File(msv.getFlashlfq());

		objs[0] = new Object[] { "Peptide and taxa mapping database (pep2taxa)", pep2tax, pep2taxFile.exists(), "..." };

		objs[1] = new Object[] { "Function database", function, funcFile.exists(), "..." };
		
		objs[2] = new Object[] { "Function definition database", funcDef, funcDefFile.exists(), "..." };

		objs[3] = new Object[] { "Taxonomic tree", taxonAll, taxonFile.exists(), "..." };

		objs[4] = new Object[] { "Protein modifications", unimod, unimodFile.exists(), "..." };

		objs[5] = new Object[] { "MaxQuant", maxquant, maxquantFile.exists(), "..." };

		objs[6] = new Object[] { "X!Tandem", xtandem, tandemFile.exists(), "..." };

		objs[7] = new Object[] { "ProteomicsTools", ProteomicsTools, ptFile.exists(), "..." };

		objs[8] = new Object[] { "MSConvert", msconvert, msconvertFile.exists(), "..." };

		objs[9] = new Object[] { "pFind", pfind, pfindFile.exists(), "..." };
		
		objs[10] = new Object[] { "DBReducer", dbReducer, dbReducerFile.exists(), "..." };

		objs[11] = new Object[] { "FlashLFQ", flashlfq, lfqFile.exists(), "..." };

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
							return "Peptide and taxa mapping database (pep2taxa)";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return false;
							} else {
								String name = f.getName();
								if (name.equals("pep2taxa.gz") || name.equals("pep2taxa.tab")) {
									return true;
								} else {
									return false;
								}
							}
						}
					});
					break;

				case 1:
					fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "Function database";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("func.db"))
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

				case 3:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "Taxonomic tree";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("taxonomy-all.tab"))
									return true;
								return false;
							}
						}
					});
					break;

				case 4:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "Protein modifications from Unimod";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("unimod.xml"))
									return true;
								return false;
							}
						}
					});
					break;

				case 5:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "MaxQuant running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("MaxQuantCmd.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 6:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "X!Tandem running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("tandem.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 7:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "ProteomicsTools for converting the format of spectra file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("ProteomicsTools.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 8:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "MSConvert for mzML file conversion";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("msconvert.exe"))
									return true;
								return false;
							}
						}
					});
					break;

				case 9:
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
					
				case 10:
					fileChooser = new JFileChooser();
					fileChooser.addChoosableFileFilter(new FileFilter() {
						public String getDescription() {
							return "DBReducer running file";
						}

						public boolean accept(File f) {
							if (f.isDirectory()) {
								return true;
							} else {
								if (f.getName().equals("DBReducer.exe"))
									return true;
								return false;
							}
						}
					});
					break;
					
				case 11:
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

				int returnValue = fileChooser.showOpenDialog(MetaLabSourcePathDialog.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();
					model.setValueAt(file.getAbsolutePath(), row, 1);
					model.setValueAt(file.exists(), row, 2);

					model.fireTableDataChanged();

					for (int i = 0; i < model.getRowCount(); i++) {
						switch (i) {
						case 0: {
							msv.setPep2tax((String) model.getValueAt(i, 1));
							break;
						}
						case 1: {
							msv.setFunction((String) model.getValueAt(i, 1));
							break;
						}
						case 2: {
							msv.setFuncDef((String) model.getValueAt(i, 1));
							break;
						}
						case 3: {
							msv.setTaxonAll((String) model.getValueAt(i, 1));
							break;
						}
						case 4: {
							msv.setUnimod((String) model.getValueAt(i, 1));
							break;
						}
						case 5: {
							msv.setMaxquant((String) model.getValueAt(i, 1));
							break;
						}
						case 6: {
							msv.setXtandem((String) model.getValueAt(i, 1));
							break;
						}
						case 7: {
							msv.setProteomicsTools((String) model.getValueAt(i, 1));
							break;
						}
						case 8: {
							msv.setMsconvert((String) model.getValueAt(i, 1));
							break;
						}
						case 9: {
							msv.setpFind((String) model.getValueAt(i, 1));
							break;
						}
						case 10: {
							msv.setDbReducer((String) model.getValueAt(i, 1));
							break;
						}
						case 11: {
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
