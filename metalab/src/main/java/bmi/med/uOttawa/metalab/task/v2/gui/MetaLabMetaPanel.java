package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaDataIO;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class MetaLabMetaPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3226087533053347296L;

	private boolean metaDataUpdate;
//	private boolean isobaricRefUpdate;

	private JComboBox<Integer> comboBoxMetaType;
	private JComboBox<String> comboBoxColumn;
	private JComboBox<Integer> comboBoxStart;
	private JComboBox<Integer> comboBoxEnd;
	private JComboBox<String> comboBoxChannel;
	private JTextField metaTextField;
	private JTable tableMeta;
	private DefaultTableModel modelMeta;
	private JTable tableRef;
	private DefaultTableModel modelRef;

	private MetaData metadata;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private JRadioButton rdbtnByName;
	private JRadioButton rdbtnByChannel;

	private JPanel isoRefPanel;

	/**
	 * Create the panel.
	 */
	public MetaLabMetaPanel(MetaData iniMetadata, boolean showReference) {

		this.metadata = new MetaData(iniMetadata.getRawFiles(), iniMetadata.getExpNames(), iniMetadata.getFractions(),
				iniMetadata.getReplicates(), iniMetadata.getLabelTitle());

		int metaTypeCount = iniMetadata.getMetaTypeCount();
		int sampleCount = iniMetadata.getSampleCount();
		
		this.metadata.setMetaTypeCount(metaTypeCount);

		setLayout(new MigLayout("", "[100:200:300,grow][100:200:300,grow][240:250:300,grow]", "[200:200:250,grow][200:200:270,grow][40]"));

		JScrollPane scrollPaneMeta = new JScrollPane() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 9085941827256276212L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				Component[] components = getComponents();
				for (Component comp : components) {
					comp.setEnabled(enabled);
				}
			}
		};
		add(scrollPaneMeta, "cell 0 0 2 2,grow");

		tableMeta = new JTable();
		modelMeta = new DefaultTableModel(iniMetadata.getMetaObjects(), iniMetadata.getMetaTableTitleObjects()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8831159216556546811L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex > 1;
			}
		};
		tableMeta.setModel(modelMeta);
		tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
		tableMeta.getColumnModel().getColumn(1).setMinWidth(50);
		scrollPaneMeta.setViewportView(tableMeta);

		JPanel metaPanel = new JPanel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1544038356670125286L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
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
			}
		};
		metaPanel.setBorder(
				new TitledBorder(null, "Metadata editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(metaPanel, "cell 2 0,grow");
		metaPanel.setLayout(new MigLayout("", "[90,grow][][90,grow]", "[30][150]"));

		JLabel lblNumberOfTypes = new JLabel("Number of types");
		metaPanel.add(lblNumberOfTypes, "cell 0 0");

		comboBoxMetaType = new JComboBox<Integer>();
		metaPanel.add(comboBoxMetaType, "cell 2 0,growx");
		for (int i = 0; i <= 5; i++) {
			comboBoxMetaType.addItem(i);
		}
		if (metaTypeCount <= 5) {
			comboBoxMetaType.setSelectedItem(metaTypeCount);
		}

		comboBoxMetaType.addActionListener(l -> {
			Integer changedCount = (Integer) comboBoxMetaType.getSelectedItem();
			if (changedCount != this.metadata.getMetaTypeCount()) {
				this.metadata.setMetaTypeCount(changedCount);
				Object[][] blankObjects = this.metadata.getBlankMetaObjects();
				modelMeta = new DefaultTableModel(blankObjects, this.metadata.getMetaTableTitleObjects()) {
					/**
					 * 
					 */
					private static final long serialVersionUID = -8831159216556546811L;

					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnIndex > 1;
					}
				};

				tableMeta.setModel(modelMeta);
				tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
				tableMeta.getColumnModel().getColumn(1).setMinWidth(50);

				comboBoxStart.removeAllItems();
				for (int i = 0; i < blankObjects.length; i++) {
					comboBoxStart.addItem((i + 1));
				}
				if (comboBoxStart.getItemCount() > 0) {
					comboBoxStart.setSelectedIndex(0);
				}

				comboBoxEnd.removeAllItems();
				for (int i = 0; i < blankObjects.length; i++) {
					comboBoxEnd.addItem((i + 1));
				}
				if (comboBoxEnd.getItemCount() > 0) {
					comboBoxEnd.setSelectedIndex(comboBoxEnd.getItemCount() - 1);
				}

				if (this.metadata.getMetaTypeCount() <= 5) {
					comboBoxColumn.removeAllItems();
					for (int i = 0; i < changedCount; i++) {
						comboBoxColumn.addItem("Meta " + (i + 1));
					}

				} else {
					comboBoxColumn.removeAllItems();
					for (int i = 0; i < 5; i++) {
						comboBoxColumn.addItem("Meta " + (i + 1));
					}
				}

				update();
			}
		});

		JPanel metaSetPanel = new JPanel();
		metaSetPanel.setBorder(new TitledBorder(null, "Batch set", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		metaPanel.add(metaSetPanel, "cell 0 1 3 1,grow");
		metaSetPanel.setLayout(new MigLayout("", "[60][15][90,grow]", "[30][30][30][]"));

		JLabel lblColumn = new JLabel("Column");
		metaSetPanel.add(lblColumn, "cell 0 0");

		comboBoxColumn = new JComboBox<String>();
		metaSetPanel.add(comboBoxColumn, "cell 2 0,growx");
		for (int i = 0; i < metaTypeCount; i++) {
			comboBoxColumn.addItem("Meta " + (i + 1));
		}

		JLabel lblStartIndex = new JLabel("Start index");
		metaSetPanel.add(lblStartIndex, "cell 0 1");

		comboBoxStart = new JComboBox<Integer>();
		for (int i = 0; i < sampleCount; i++) {
			comboBoxStart.addItem((i + 1));
		}
		metaSetPanel.add(comboBoxStart, "cell 2 1,growx");

		JLabel lblEndIndex = new JLabel("End index");
		metaSetPanel.add(lblEndIndex, "cell 0 2");

		comboBoxEnd = new JComboBox<Integer>();
		for (int i = 0; i < sampleCount; i++) {
			comboBoxEnd.addItem((i + 1));
		}
		metaSetPanel.add(comboBoxEnd, "cell 2 2,growx");

		JLabel lblData = new JLabel("Data");
		metaSetPanel.add(lblData, "cell 0 3");

		metaTextField = new JTextField();
		metaTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

		});
		metaSetPanel.add(metaTextField, "cell 2 3,growx");
		metaTextField.setColumns(10);

		this.isoRefPanel = new JPanel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6928786425338982416L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				Component[] components = getComponents();
				for (Component comp : components) {

					if (comp instanceof JPanel) {
						Component[] subcomps = ((JPanel) comp).getComponents();
						for (Component sbc : subcomps) {
							sbc.setEnabled(enabled);
						}
					}

					comp.setEnabled(enabled);
				}
			}
		};
		isoRefPanel.setBorder(
				new TitledBorder(null, "Isobaric reference", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(isoRefPanel, "cell 2 1,grow");
		isoRefPanel.setLayout(new MigLayout("", "[45][10][50,grow]", "[][][][]"));

		comboBoxChannel = new JComboBox<String>();
		isoRefPanel.add(comboBoxChannel, "cell 2 0,growx");
		if (metadata.isIsobaricQuan()) {
			for (int i = 1; i <= metadata.getLabelCount(); i++) {
				comboBoxChannel.addItem("Channel " + i);
			}
		}

		JScrollPane scrollPaneRef = new JScrollPane() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6928786425338982416L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				Component[] components = getComponents();
				for (Component comp : components) {

					if (comp instanceof JPanel) {
						Component[] subcomps = ((JPanel) comp).getComponents();
						for (Component sbc : subcomps) {
							sbc.setEnabled(enabled);
						}
					}

					comp.setEnabled(enabled);
				}
			}
		};
		isoRefPanel.add(scrollPaneRef, "cell 0 3 3 1,growx");
		scrollPaneRef.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Select by name", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		modelRef = new DefaultTableModel(iniMetadata.getRefObjects(),
				new Object[] { "Select as reference", "Exp name" });

		tableRef = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5924229233855919804L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		scrollPaneRef.setViewportView(tableRef);
		tableRef.setModel(modelRef);

		scrollPaneRef.setEnabled(showReference);
		tableRef.setEnabled(showReference);
		
		this.rdbtnByName = new JRadioButton("By name");
		rdbtnByName.addActionListener(l -> {
			if (rdbtnByName.isSelected()) {
				comboBoxChannel.setEnabled(false);
				scrollPaneRef.setEnabled(true);
			} else {
				comboBoxChannel.setEnabled(true);
				scrollPaneRef.setEnabled(false);
			}
		});
		buttonGroup.add(rdbtnByName);
		isoRefPanel.add(rdbtnByName, "cell 0 1,growx");

		this.rdbtnByChannel = new JRadioButton("By channel");
		rdbtnByChannel.addActionListener(l -> {
			if (rdbtnByChannel.isSelected()) {
				comboBoxChannel.setEnabled(true);
				scrollPaneRef.setEnabled(false);
			} else {
				comboBoxChannel.setEnabled(false);
				scrollPaneRef.setEnabled(true);
			}
		});

		buttonGroup.add(rdbtnByChannel);
		
		isoRefPanel.add(rdbtnByChannel, "cell 0 0");

		int iniChannelId = iniMetadata.getRefChannelId();
		if (iniChannelId == -1) {
			rdbtnByChannel.setSelected(false);
			rdbtnByName.setSelected(true);
			comboBoxChannel.setEnabled(false);
			scrollPaneRef.setEnabled(true);
		} else {
			rdbtnByChannel.setSelected(true);
			rdbtnByName.setSelected(false);
			comboBoxChannel.setEnabled(true);
			scrollPaneRef.setEnabled(false);

			if (iniChannelId >= 0 && iniChannelId < comboBoxChannel.getItemCount()) {
				comboBoxChannel.setSelectedIndex(iniChannelId);
			}
		}

		if (this.metadata.isIsobaricQuan()) {
			isoRefPanel.setEnabled(true);
		} else {
			isoRefPanel.setEnabled(false);
		}

		JButton btnEditInXlsx = new JButton("Edit in xlsx");
		add(btnEditInXlsx, "cell 0 2,alignx center");
		btnEditInXlsx.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*xlsx) ", new String[] { "xlsx" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("xlsx")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".xlsx");
				}

				update();

				try {
					MetaDataIO.exportMetaXlsx(metadata, metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JButton btnEditInTsv = new JButton("Edit in tsv");
		add(btnEditInTsv, "cell 1 2,alignx center");
		btnEditInTsv.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*tsv) ", new String[] { "tsv" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("tsv")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".tsv");
				}

				update();

				try {
					MetaDataIO.exportMetaTsv(metadata, metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JButton btnLoadMetaInfo = new JButton("Load meta info");
		add(btnLoadMetaInfo, "cell 2 2,alignx center");
		btnLoadMetaInfo.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser batchChooser;
			if (rawFiles != null && rawFiles.length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv, *xlsx) ",
					new String[] { "txt", "tsv", "csv", "xlsx" }));

			int returnValue = batchChooser.showOpenDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File metaFile = batchChooser.getSelectedFile();
				if (metaFile.getName().endsWith("xlsx")) {
					try {
						MetaDataIO.readMetaFileXlsx(this.metadata, metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
					}
				} else if (metaFile.getName().endsWith("txt") || metaFile.getName().endsWith("csv")
						|| metaFile.getName().endsWith("tsv")) {
					try {
						MetaDataIO.readMetaFileTxt(this.metadata, metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
					}
				}

				Object[][] tableObjs = this.metadata.getMetaObjects();
				Object[] newTitleObj = this.metadata.getMetaTableTitleObjects();

				if (tableObjs.length > 0) {
					modelMeta = new DefaultTableModel(tableObjs, newTitleObj) {
						/**
						 * 
						 */
						private static final long serialVersionUID = -8831159216556546811L;

						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnIndex > 1;
						}
					};
					tableMeta.setModel(modelMeta);
					tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
					tableMeta.getColumnModel().getColumn(1).setMinWidth(50);

					comboBoxStart.removeAllItems();
					for (int i = 0; i < tableObjs.length; i++) {
						comboBoxStart.addItem((i + 1));
					}
					if (comboBoxStart.getItemCount() > 0) {
						comboBoxStart.setSelectedIndex(0);
					}

					comboBoxEnd.removeAllItems();
					for (int i = 0; i < tableObjs.length; i++) {
						comboBoxEnd.addItem((i + 1));
					}
					if (comboBoxEnd.getItemCount() > 0) {
						comboBoxEnd.setSelectedIndex(comboBoxEnd.getItemCount() - 1);
					}

					if (this.metadata.getMetaTypeCount() <= 5) {
						comboBoxMetaType.setSelectedItem(this.metadata.getMetaTypeCount());

						comboBoxColumn.removeAllItems();
						for (int i = 0; i < this.metadata.getMetaTypeCount(); i++) {
							comboBoxColumn.addItem("Meta " + (i + 1));
						}

					} else {
						comboBoxMetaType.setSelectedItem(5);

						comboBoxColumn.removeAllItems();
						for (int i = 0; i < 5; i++) {
							comboBoxColumn.addItem("Meta " + (i + 1));
						}
					}

					parseRef();

					update();
				}
			}
		});
	}
	
	/**
	 * Create the panel.
	 */
	public MetaLabMetaPanel(MetaData iniMetadata) {

		this.metadata = new MetaData(iniMetadata.getRawFiles(), iniMetadata.getExpNames(), iniMetadata.getFractions(),
				iniMetadata.getReplicates(), iniMetadata.getLabelTitle());

		int metaTypeCount = iniMetadata.getMetaTypeCount();
		int sampleCount = iniMetadata.getSampleCount();

		this.metadata.setMetaTypeCount(metaTypeCount);

		setLayout(
				new MigLayout("", "[100:200:300,grow][100:200:300,grow][240:250:300,grow]", "[200:300:400,grow][40]"));

		JScrollPane scrollPaneMeta = new JScrollPane() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 9085941827256276212L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
				Component[] components = getComponents();
				for (Component comp : components) {
					comp.setEnabled(enabled);
				}
			}
		};
		add(scrollPaneMeta, "cell 0 0 2 1,grow");

		tableMeta = new JTable();
		modelMeta = new DefaultTableModel(iniMetadata.getMetaObjects(), iniMetadata.getMetaTableTitleObjects()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8831159216556546811L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex > 1;
			}
		};
		tableMeta.setModel(modelMeta);
		tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
		tableMeta.getColumnModel().getColumn(1).setMinWidth(50);
		scrollPaneMeta.setViewportView(tableMeta);

		JPanel metaPanel = new JPanel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1544038356670125286L;

			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
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
			}
		};
		metaPanel.setBorder(
				new TitledBorder(null, "Metadata editing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(metaPanel, "cell 2 0,grow");
		metaPanel.setLayout(new MigLayout("", "[90,grow][][90,grow]", "[30][150]"));

		JLabel lblNumberOfTypes = new JLabel("Number of types");
		metaPanel.add(lblNumberOfTypes, "cell 0 0");

		comboBoxMetaType = new JComboBox<Integer>();
		metaPanel.add(comboBoxMetaType, "cell 2 0,growx");
		for (int i = 0; i <= 5; i++) {
			comboBoxMetaType.addItem(i);
		}
		if (metaTypeCount <= 5) {
			comboBoxMetaType.setSelectedItem(metaTypeCount);
		}

		comboBoxMetaType.addActionListener(l -> {
			Integer changedCount = (Integer) comboBoxMetaType.getSelectedItem();
			if (changedCount != this.metadata.getMetaTypeCount()) {
				this.metadata.setMetaTypeCount(changedCount);
				Object[][] blankObjects = this.metadata.getBlankMetaObjects();
				modelMeta = new DefaultTableModel(blankObjects, this.metadata.getMetaTableTitleObjects()) {
					/**
					 * 
					 */
					private static final long serialVersionUID = -8831159216556546811L;

					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnIndex > 1;
					}
				};

				tableMeta.setModel(modelMeta);
				tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
				tableMeta.getColumnModel().getColumn(1).setMinWidth(50);

				comboBoxStart.removeAllItems();
				for (int i = 0; i < blankObjects.length; i++) {
					comboBoxStart.addItem((i + 1));
				}
				if (comboBoxStart.getItemCount() > 0) {
					comboBoxStart.setSelectedIndex(0);
				}

				comboBoxEnd.removeAllItems();
				for (int i = 0; i < blankObjects.length; i++) {
					comboBoxEnd.addItem((i + 1));
				}
				if (comboBoxEnd.getItemCount() > 0) {
					comboBoxEnd.setSelectedIndex(comboBoxEnd.getItemCount() - 1);
				}

				if (this.metadata.getMetaTypeCount() <= 5) {
					comboBoxColumn.removeAllItems();
					for (int i = 0; i < changedCount; i++) {
						comboBoxColumn.addItem("Meta " + (i + 1));
					}

				} else {
					comboBoxColumn.removeAllItems();
					for (int i = 0; i < 5; i++) {
						comboBoxColumn.addItem("Meta " + (i + 1));
					}
				}

				update();
			}
		});

		JPanel metaSetPanel = new JPanel();
		metaSetPanel.setBorder(new TitledBorder(null, "Batch set", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		metaPanel.add(metaSetPanel, "cell 0 1 3 1,grow");
		metaSetPanel.setLayout(new MigLayout("", "[60][15][90,grow]", "[30][30][30][]"));

		JLabel lblColumn = new JLabel("Column");
		metaSetPanel.add(lblColumn, "cell 0 0");

		comboBoxColumn = new JComboBox<String>();
		metaSetPanel.add(comboBoxColumn, "cell 2 0,growx");
		for (int i = 0; i < metaTypeCount; i++) {
			comboBoxColumn.addItem("Meta " + (i + 1));
		}

		JLabel lblStartIndex = new JLabel("Start index");
		metaSetPanel.add(lblStartIndex, "cell 0 1");

		comboBoxStart = new JComboBox<Integer>();
		for (int i = 0; i < sampleCount; i++) {
			comboBoxStart.addItem((i + 1));
		}
		metaSetPanel.add(comboBoxStart, "cell 2 1,growx");

		JLabel lblEndIndex = new JLabel("End index");
		metaSetPanel.add(lblEndIndex, "cell 0 2");

		comboBoxEnd = new JComboBox<Integer>();
		for (int i = 0; i < sampleCount; i++) {
			comboBoxEnd.addItem((i + 1));
		}
		metaSetPanel.add(comboBoxEnd, "cell 2 2,growx");

		JLabel lblData = new JLabel("Data");
		metaSetPanel.add(lblData, "cell 0 3");

		metaTextField = new JTextField();
		metaTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				setMetaData();
			}

		});
		metaSetPanel.add(metaTextField, "cell 2 3,growx");
		metaTextField.setColumns(10);

		JButton btnEditInXlsx = new JButton("Edit in xlsx");
		add(btnEditInXlsx, "cell 0 2,alignx center");
		btnEditInXlsx.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*xlsx) ", new String[] { "xlsx" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("xlsx")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".xlsx");
				}

				update();

				try {
					MetaDataIO.exportMetaXlsx(metadata, metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JButton btnEditInTsv = new JButton("Edit in tsv");
		add(btnEditInTsv, "cell 1 2,alignx center");
		btnEditInTsv.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser metaXlsxChooser = new JFileChooser();
			if (rawFiles != null && rawFiles.length > 0) {
				metaXlsxChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				metaXlsxChooser = new JFileChooser();
			}

			metaXlsxChooser.setFileFilter(new FileNameExtensionFilter("metadata (*tsv) ", new String[] { "tsv" }));

			int returnValue = metaXlsxChooser.showSaveDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File metaFile = metaXlsxChooser.getSelectedFile();
				if (!metaFile.getName().endsWith("tsv")) {
					metaFile = new File(metaFile.getAbsolutePath() + ".tsv");
				}

				update();

				try {
					MetaDataIO.exportMetaTsv(metadata, metaFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		JButton btnLoadMetaInfo = new JButton("Load meta info");
		add(btnLoadMetaInfo, "cell 2 2,alignx center");
		btnLoadMetaInfo.addActionListener(l -> {
			String[] rawFiles = this.metadata.getRawFiles();
			JFileChooser batchChooser;
			if (rawFiles != null && rawFiles.length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv, *xlsx) ",
					new String[] { "txt", "tsv", "csv", "xlsx" }));

			int returnValue = batchChooser.showOpenDialog(MetaLabMetaPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File metaFile = batchChooser.getSelectedFile();
				if (metaFile.getName().endsWith("xlsx")) {
					try {
						MetaDataIO.readMetaFileXlsx(this.metadata, metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
					}
				} else if (metaFile.getName().endsWith("txt") || metaFile.getName().endsWith("csv")
						|| metaFile.getName().endsWith("tsv")) {
					try {
						MetaDataIO.readMetaFileTxt(this.metadata, metaFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(this, e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
					}
				}

				Object[][] tableObjs = this.metadata.getMetaObjects();
				Object[] newTitleObj = this.metadata.getMetaTableTitleObjects();

				if (tableObjs.length > 0) {
					modelMeta = new DefaultTableModel(tableObjs, newTitleObj) {
						/**
						 * 
						 */
						private static final long serialVersionUID = -8831159216556546811L;

						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnIndex > 1;
						}
					};
					tableMeta.setModel(modelMeta);
					tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
					tableMeta.getColumnModel().getColumn(1).setMinWidth(50);

					comboBoxStart.removeAllItems();
					for (int i = 0; i < tableObjs.length; i++) {
						comboBoxStart.addItem((i + 1));
					}
					if (comboBoxStart.getItemCount() > 0) {
						comboBoxStart.setSelectedIndex(0);
					}

					comboBoxEnd.removeAllItems();
					for (int i = 0; i < tableObjs.length; i++) {
						comboBoxEnd.addItem((i + 1));
					}
					if (comboBoxEnd.getItemCount() > 0) {
						comboBoxEnd.setSelectedIndex(comboBoxEnd.getItemCount() - 1);
					}

					if (this.metadata.getMetaTypeCount() <= 5) {
						comboBoxMetaType.setSelectedItem(this.metadata.getMetaTypeCount());

						comboBoxColumn.removeAllItems();
						for (int i = 0; i < this.metadata.getMetaTypeCount(); i++) {
							comboBoxColumn.addItem("Meta " + (i + 1));
						}

					} else {
						comboBoxMetaType.setSelectedItem(5);

						comboBoxColumn.removeAllItems();
						for (int i = 0; i < 5; i++) {
							comboBoxColumn.addItem("Meta " + (i + 1));
						}
					}

					parseRef();

					update();
				}
			}
		});
	}
/*
	private void setIsobaricRef() {

		synchronized (this) {
			if (isobaricRefUpdate) {
				return;
			}
			isobaricRefUpdate = true;

			String text = this.metaTextField.getText();

			int columnCount = this.modelMeta.getColumnCount();
			if (columnCount > 0) {
				String metaTitle = (String) this.comboBoxColumn.getSelectedItem();
				int start = (Integer) this.comboBoxStart.getSelectedItem();
				int end = (Integer) this.comboBoxEnd.getSelectedItem();

				for (int i = 0; i < columnCount; i++) {
					if (this.modelMeta.getColumnName(i).equals(metaTitle)) {
						for (int j = start - 1; j < end; j++) {
							this.modelMeta.setValueAt(text, j, i);
						}
					}
				}
			}

			isobaricRefUpdate = false;
		}
	}
*/
	private void setMetaData() {
		synchronized (this) {
			if (metaDataUpdate) {
				return;
			}
			metaDataUpdate = true;

			String text = this.metaTextField.getText();

			int columnCount = this.modelMeta.getColumnCount();
			if (columnCount > 0) {
				String metaTitle = (String) this.comboBoxColumn.getSelectedItem();
				int start = (Integer) this.comboBoxStart.getSelectedItem();
				int end = (Integer) this.comboBoxEnd.getSelectedItem();

				for (int i = 0; i < columnCount; i++) {
					if (this.modelMeta.getColumnName(i).equals(metaTitle)) {
						for (int j = start - 1; j < end; j++) {
							this.modelMeta.setValueAt(text, j, i);
						}
					}
				}
			}

			modelRef = new DefaultTableModel(metadata.getRefObjects(),
					new Object[] { "Select as reference", "Exp name" });
			tableRef.setModel(modelRef);

			metaDataUpdate = false;
		}
	}

	private void parseRef() {
		String[] labelExpNames = metadata.getLabelExpNames();
		metadata.getLabelCount();
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < metadata.getLabelCount(); i++) {
			set.add(labelExpNames[i]);
		}

		for (int i = 1; i < metadata.getExpNames().length; i++) {
			HashSet<String> seti = new HashSet<String>();
			for (int j = 0; j < metadata.getLabelTitle().length; j++) {
				seti.add(labelExpNames[i * metadata.getLabelTitle().length + j]);
			}
			Iterator<String> it = seti.iterator();
			while (it.hasNext()) {
				String name = it.next();
				if (!set.contains(name)) {
					it.remove();
				}
			}
			set = seti;
		}

		String[] isobaricRef = set.size() == 0 ? new String[] { "" } : set.toArray(new String[set.size()]);
		boolean[] selectRef = new boolean[isobaricRef.length];

		metadata.setSelectRef(selectRef);
		metadata.setIsobaricReference(isobaricRef);

		modelRef = new DefaultTableModel(metadata.getRefObjects(), new Object[] { "Select as reference", "Exp name" });
		tableRef.setModel(modelRef);
	}

	public void update(MetaData newMetadata) {

		if (!this.metadata.equalRawExpName(newMetadata) || !this.metadata.equalLabelTitle(newMetadata)) {

			this.metadata = newMetadata;

			this.metadata.setMetaTypeCount((Integer) this.comboBoxMetaType.getSelectedItem());

			Object[] newTitleObj = newMetadata.getMetaTableTitleObjects();
			Object[][] newObjs = newMetadata.getBlankMetaObjects();

			newMetadata.setMetaInfo(new String[newMetadata.getRawFileCount()][newMetadata.getMetaTypeCount()]);

			modelMeta = new DefaultTableModel(newObjs, newTitleObj) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -8831159216556546811L;

				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return columnIndex > 1;
				}
			};
			tableMeta.setModel(modelMeta);
			tableMeta.getColumnModel().getColumn(0).setMaxWidth(50);
			tableMeta.getColumnModel().getColumn(1).setMinWidth(150);

			comboBoxColumn.removeAllItems();
			for (int i = 0; i < newMetadata.getMetaTypeCount(); i++) {
				comboBoxColumn.addItem("Meta" + (i + 1));
			}

			if (newObjs.length != comboBoxStart.getItemCount()) {
				comboBoxStart.removeAllItems();
				for (int i = 0; i < newObjs.length; i++) {
					comboBoxStart.addItem((i + 1));
				}
				if (comboBoxStart.getItemCount() > 0) {
					comboBoxStart.setSelectedIndex(0);
				}

				comboBoxEnd.removeAllItems();
				for (int i = 0; i < newObjs.length; i++) {
					comboBoxEnd.addItem((i + 1));
				}
				if (comboBoxEnd.getItemCount() > 0) {
					comboBoxEnd.setSelectedIndex(comboBoxEnd.getItemCount() - 1);
				}
			}

			if(comboBoxChannel!=null) {
				comboBoxChannel.removeAllItems();
				modelRef = new DefaultTableModel(newMetadata.getRefObjects(),
						new Object[] { "Select as reference", "Exp name" });
				tableRef.setModel(modelRef);

				if (newMetadata.isIsobaricQuan()) {
					isoRefPanel.setEnabled(true);
					rdbtnByChannel.setSelected(true);
					for (int i = 1; i <= newMetadata.getLabelCount(); i++) {
						comboBoxChannel.addItem("Channel " + i);
					}
				} else {
					isoRefPanel.setEnabled(false);
				}
			}

		} else {

			this.metadata = newMetadata;

			this.update();
		}
	}

	/**
	 * This method is used to write the information from the panel to the class MetaData, please be sure to edit the information
	 * in the panel correctly before using this method
	 */
	public void update() {

		int metaTypeCount = this.comboBoxMetaType.getItemAt(comboBoxMetaType.getSelectedIndex());
		int rowCount = this.modelMeta.getRowCount();
		int columnCount = this.modelMeta.getColumnCount();
		String[][] metainfo = new String[rowCount][metaTypeCount];
		if (columnCount == metaTypeCount + 2) {
			for (int i = 0; i < metainfo.length; i++) {
				for (int j = 0; j < metainfo[i].length; j++) {
					metainfo[i][j] = (String) this.modelMeta.getValueAt(i, j + 2);
				}
			}
			metadata.setMetaTypeCount(metaTypeCount);
			metadata.setMetaInfo(metainfo);
		} else if (columnCount == metaTypeCount + 3) {
			for (int i = 0; i < metainfo.length; i++) {
				for (int j = 0; j < metainfo[i].length; j++) {
					metainfo[i][j] = (String) this.modelMeta.getValueAt(i, j + 3);
				}
			}
			metadata.setMetaTypeCount(metaTypeCount);
			metadata.setMetaInfo(metainfo);

			HashMap<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < rowCount; i++) {
				map.put((String) this.modelMeta.getValueAt(i, 1), (String) this.modelMeta.getValueAt(i, 2));
			}

			String[] labelTitle = metadata.getLabelTitle();
			String[] expName = metadata.getExpNames();
			int[] fraction = metadata.getFractions();
			int[] replicate = metadata.getReplicates();
			String[] labelExpName = new String[expName.length * labelTitle.length];
			for (int i = 0; i < expName.length; i++) {
				for (int j = 0; j < labelTitle.length; j++) {
					String key = expName[i] + "_" + fraction[i] + "_" + replicate[i] + " " + labelTitle[j];
					if (map.containsKey(key)) {
						labelExpName[i * labelTitle.length + j] = map.get(key);
					} else {
						labelExpName[i * labelTitle.length + j] = "";
					}
				}
			}
			metadata.setLabelExpNames(labelExpName);
		}

		if (rdbtnByName!=null && rdbtnByName.isSelected()) {
			String[] isobaricRef = new String[this.modelRef.getRowCount()];
			boolean[] selectRef = new boolean[this.modelRef.getRowCount()];

			for (int i = 0; i < selectRef.length; i++) {
				selectRef[i] = (boolean) this.modelRef.getValueAt(i, 0);
				isobaricRef[i] = (String) this.modelRef.getValueAt(i, 1);
			}

			metadata.setSelectRef(selectRef);
			metadata.setIsobaricReference(isobaricRef);
			metadata.setRefChannelId(-1);
		}

		if (rdbtnByChannel!=null && rdbtnByChannel.isSelected()) {
			metadata.setRefChannelId(comboBoxChannel.getSelectedIndex());
		}
	}

	public MetaData getMetadata() {
		return metadata;
	}

	public int getMetaTypeCount() {
		return comboBoxMetaType.getItemAt(comboBoxMetaType.getSelectedIndex());
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] components = getComponents();
		for (Component comp : components) {

			if (comp instanceof JPanel) {
				Component[] subcomps = ((JPanel) comp).getComponents();
				for (Component sbc : subcomps) {
					sbc.setEnabled(enabled);
				}
			}

			comp.setEnabled(enabled);
		}
	}
	/*
	 * public void setMetaData(MetaData metadata) { this.metadata = new
	 * MetaData(metadata.getRawFiles(), metadata.getExpNames(),
	 * metadata.getFractions(), metadata.getReplicates(), metadata.getLabelTitle());
	 * this.comboBoxMetaType.setSelectedIndex(0); }
	 * 
	 * public MetaData getMetaData() {
	 * 
	 * int metaTypeCount =
	 * this.comboBoxMetaType.getItemAt(comboBoxMetaType.getSelectedIndex()); int
	 * rowCount = this.modelMeta.getRowCount(); int columnCount =
	 * this.modelMeta.getColumnCount(); String[][] metainfo = new
	 * String[rowCount][metaTypeCount]; if (columnCount == metaTypeCount + 2) { for
	 * (int i = 0; i < metainfo.length; i++) { for (int j = 0; j <
	 * metainfo[i].length; j++) { metainfo[i][j] = (String)
	 * this.modelMeta.getValueAt(i, j + 2); } }
	 * metadata.setMetaTypeCount(metaTypeCount); metadata.setMetaInfo(metainfo); }
	 * else if (columnCount == metaTypeCount + 3) { for (int i = 0; i <
	 * metainfo.length; i++) { for (int j = 0; j < metainfo[i].length; j++) {
	 * metainfo[i][j] = (String) this.modelMeta.getValueAt(i, j + 3); } }
	 * metadata.setMetaTypeCount(metaTypeCount); metadata.setMetaInfo(metainfo);
	 * 
	 * HashMap<String, String> map = new HashMap<String, String>(); for (int i = 0;
	 * i < rowCount; i++) { map.put((String) this.modelMeta.getValueAt(i, 1),
	 * (String) this.modelMeta.getValueAt(i, 2)); } String[] labelExpName = new
	 * String[metadata.getExpNames().length * metadata.getLabelTitle().length]; for
	 * (int i = 0; i < metadata.getExpNames().length; i++) { for (int j = 0; j <
	 * metadata.getLabelTitle().length; j++) { String key =
	 * metadata.getExpNames()[i] + " " + metadata.getLabelTitle()[j]; if
	 * (map.containsKey(key)) { labelExpName[i * metadata.getLabelTitle().length +
	 * j] = map.get(key); } else { labelExpName[i * metadata.getLabelTitle().length
	 * + j] = ""; } } } metadata.setLabelExpNames(labelExpName); }
	 * 
	 * String[] isobaricRef = new String[this.modelRef.getRowCount()]; boolean[]
	 * selectRef = new boolean[this.modelRef.getRowCount()];
	 * 
	 * for (int i = 0; i < selectRef.length; i++) { selectRef[i] = (boolean)
	 * this.modelRef.getValueAt(i, 0); isobaricRef[i] = (String)
	 * this.modelRef.getValueAt(i, 1); }
	 * 
	 * metadata.setSelectRef(selectRef); metadata.setIsobaricReference(isobaricRef);
	 * 
	 * return metadata; }
	 */
}
