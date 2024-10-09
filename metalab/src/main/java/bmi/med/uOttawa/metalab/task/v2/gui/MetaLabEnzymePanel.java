/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;

import bmi.med.uOttawa.metalab.core.enzyme.Enzyme;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzyme;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

import javax.swing.border.TitledBorder;
import java.awt.Color;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabEnzymePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8679994585968793160L;

	private DefaultTableModel modelEnzyme;
	private DefaultTableModel modelDismode;
	private JComboBox<Integer> comboBox;

	private String[][] pfindEnzymes;
	private DefaultTableModel pfindModelEnzyme;

	/**
	 * Create the panel.
	 */
	public MetaLabEnzymePanel(MetaParameterMQ par) {

		Enzyme enzyme = par.getEnzyme();

		int missCleavages = par.getMissCleavages();

		int digestMode = par.getDigestMode();

		JTable tableEnzyme = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5816216718271314374L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		Enzyme[] enzymes = Enzyme.values();
		Object[][] enzymeObjs = new Object[enzymes.length][];
		for (int i = 0; i < enzymeObjs.length; i++) {
			enzymeObjs[i] = enzymes[i].toTableRawObjects();
			if (enzyme.equals(enzymes[i])) {
				enzymeObjs[i][0] = true;
			} else {
				enzymeObjs[i][0] = false;
			}
		}

		modelEnzyme = new DefaultTableModel(enzymeObjs,
				new Object[] { "", "Name", "cleaveAt", "notCleaveAt", "sense_C" }) {
			/**
					 * 
					 */
			private static final long serialVersionUID = 5168531670219208095L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};

		tableEnzyme.setModel(modelEnzyme);
		tableEnzyme.getColumnModel().getColumn(0).setMaxWidth(30);

		tableEnzyme.setBackground(new Color(185, 238, 172));
		tableEnzyme.setGridColor(new Color(0, 0, 0));

		modelEnzyme.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub

				int row = event.getFirstRow();
				if (((boolean) modelEnzyme.getValueAt(row, 0)) == true) {
					for (int i = 0; i < modelEnzyme.getRowCount(); i++) {
						if (i != row) {
							modelEnzyme.setValueAt(false, i, 0);
						}
					}
				} else {
					boolean allFalse = true;
					for (int i = 0; i < modelEnzyme.getRowCount(); i++) {
						if (((boolean) modelEnzyme.getValueAt(i, 0)) == true) {
							allFalse = false;
						}
					}
					if (allFalse) {
						modelEnzyme.setValueAt(true, row, 0);
					}
				}
			}
		});

		setLayout(new MigLayout("", "[150:400:480,grow][150:400:480,grow]", "[250:350:450,grow][60:60:60]"));

		JScrollPane scrollPaneEnzyme = new JScrollPane();
		scrollPaneEnzyme.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneEnzyme.setViewportView(tableEnzyme);

		JPanel panelEnzyme = new JPanel();
		panelEnzyme.setBorder(new TitledBorder(null, "Enzymes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelEnzyme.setLayout(new MigLayout("", "[]", "[]"));
		panelEnzyme.add(scrollPaneEnzyme, "cell 0 0");
		add(panelEnzyme, "cell 0 0 1 2,grow");

		JScrollPane scrollPaneMode = new JScrollPane();
		scrollPaneMode.setBorder(BorderFactory.createEmptyBorder());

		JTable tableMode = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -802709812109265880L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}

		};
		scrollPaneMode.setViewportView(tableMode);

		JPanel panelMode = new JPanel();
		panelMode.setBorder(
				new TitledBorder(null, "Digestion mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMode.setLayout(new MigLayout("", "[200:400:480,grow]", "[250:350:450,grow]"));
		panelMode.add(scrollPaneMode, "cell 0 0,grow");
		add(panelMode, "cell 1 0,grow");

		String[] disModes = MaxquantEnzyme.enzymeModes;
		Object[][] modeObjs = new Object[disModes.length][2];
		for (int i = 0; i < modeObjs.length; i++) {
			modeObjs[i][1] = disModes[i];
			if (digestMode == i) {
				modeObjs[i][0] = true;
			} else {
				modeObjs[i][0] = false;
			}
		}

		modelDismode = new DefaultTableModel(modeObjs, new Object[] { "", "Digestion mode" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6422182669339243789L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};

		tableMode.setModel(modelDismode);
		tableMode.getColumnModel().getColumn(0).setMaxWidth(30);

		tableMode.setBackground(new Color(185, 238, 172));
		tableMode.setGridColor(new Color(0, 0, 0));

		modelDismode.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub

				int row = event.getFirstRow();
				if (((boolean) modelDismode.getValueAt(row, 0)) == true) {
					for (int i = 0; i < modelDismode.getRowCount(); i++) {
						if (i != row) {
							modelDismode.setValueAt(false, i, 0);
						}
					}
				} else {
					boolean allFalse = true;
					for (int i = 0; i < modelDismode.getRowCount(); i++) {
						if (((boolean) modelDismode.getValueAt(i, 0)) == true) {
							allFalse = false;
						}
					}
					if (allFalse) {
						modelDismode.setValueAt(true, row, 0);
					}
				}
			}
		});

		JPanel panelMiss = new JPanel();
		panelMiss.setBorder(
				new TitledBorder(null, "Max missed Cleavages", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelMiss, "cell 1 1,grow");
		panelMiss.setLayout(new MigLayout("", "[30][80]", "[20px][20]"));

		comboBox = new JComboBox<Integer>();
		panelMiss.add(comboBox, "cell 1 0,growx,aligny top");
		for (int i = 0; i <= 5; i++) {
			comboBox.addItem(i);
		}

		if (missCleavages <= 5) {
			comboBox.setSelectedIndex(missCleavages);
		}

		tableMode.getSelectionModel().addListSelectionListener(l -> {

		});
	}

	/**
	 * Create the panel.
	 * @wbp.parser.constructor
	 */
	public MetaLabEnzymePanel(MetaParameterPFind par, String[][] pfindEnzymes) {

		String enzyme = par.getEnzyme();

		int missCleavages = par.getMissCleavages();

		int digestMode = par.getDigestMode();

		JTable tableString = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5816216718271314374L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		this.pfindEnzymes = pfindEnzymes;
		boolean findEnzyme = false;
		
		Object[][] enzymeObjs = new Object[pfindEnzymes.length][];
		for (int i = 0; i < enzymeObjs.length; i++) {
			enzymeObjs[i] = new Object[pfindEnzymes[i].length + 1];
			enzymeObjs[i][0] = false;
			for (int j = 0; j < pfindEnzymes[i].length; j++) {
				enzymeObjs[i][j + 1] = pfindEnzymes[i][j];
			}
			if (enzyme.equals(pfindEnzymes[i][0])) {
				enzymeObjs[i][0] = true;
				findEnzyme = true;
			} else {
				enzymeObjs[i][0] = false;
			}
		}

		if (!findEnzyme && pfindEnzymes.length > 0) {
			enzymeObjs[0][0] = true;
		}
		
		pfindModelEnzyme = new DefaultTableModel(enzymeObjs,
				new Object[] { "", "Name", "cleaveAt", "notCleaveAt", "sense" }) {
			/**
					 * 
					 */
			private static final long serialVersionUID = 5168531670219208095L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};

		tableString.setModel(pfindModelEnzyme);
		tableString.getColumnModel().getColumn(0).setMaxWidth(30);

		tableString.setBackground(new Color(185, 238, 172));
		tableString.setGridColor(new Color(0, 0, 0));

		pfindModelEnzyme.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub

				int row = event.getFirstRow();
				if (((boolean) pfindModelEnzyme.getValueAt(row, 0)) == true) {
					for (int i = 0; i < pfindModelEnzyme.getRowCount(); i++) {
						if (i != row) {
							pfindModelEnzyme.setValueAt(false, i, 0);
						}
					}
				} else {
					boolean allFalse = true;
					for (int i = 0; i < pfindModelEnzyme.getRowCount(); i++) {
						if (((boolean) pfindModelEnzyme.getValueAt(i, 0)) == true) {
							allFalse = false;
						}
					}
					if (allFalse) {
						pfindModelEnzyme.setValueAt(true, row, 0);
					}
				}
			}
		});

		setLayout(new MigLayout("", "[150:400:480,grow][150:400:480,grow]", "[250:350:450,grow][60:60:60]"));

		JScrollPane scrollPaneString = new JScrollPane();
		scrollPaneString.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneString.setViewportView(tableString);

		JPanel panelString = new JPanel();
		panelString.setBorder(new TitledBorder(null, "Enzymes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelString.setLayout(new MigLayout("", "[]", "[]"));
		panelString.add(scrollPaneString, "cell 0 0");
		add(panelString, "cell 0 0 1 2,grow");

		JScrollPane scrollPaneMode = new JScrollPane();
		scrollPaneMode.setBorder(BorderFactory.createEmptyBorder());

		JTable tableMode = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -802709812109265880L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}

		};
		scrollPaneMode.setViewportView(tableMode);

		JPanel panelMode = new JPanel();
		panelMode.setBorder(
				new TitledBorder(null, "Digestion mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelMode.setLayout(new MigLayout("", "[200:400:480,grow]", "[200:300:400,grow]"));
		panelMode.add(scrollPaneMode, "cell 0 0,grow");
		add(panelMode, "cell 1 0,grow");

		String[] disModes = MaxquantEnzyme.simpleEnzymeModes;
		Object[][] modeObjs = new Object[disModes.length][2];
		for (int i = 0; i < modeObjs.length; i++) {
			modeObjs[i][1] = disModes[i];
			if (digestMode == i) {
				modeObjs[i][0] = true;
			} else {
				modeObjs[i][0] = false;
			}
		}

		modelDismode = new DefaultTableModel(modeObjs, new Object[] { "", "Digestion mode" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6422182669339243789L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};

		tableMode.setModel(modelDismode);
		tableMode.getColumnModel().getColumn(0).setMaxWidth(30);

		tableMode.setBackground(new Color(185, 238, 172));
		tableMode.setGridColor(new Color(0, 0, 0));

		modelDismode.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub

				int row = event.getFirstRow();
				if (((boolean) modelDismode.getValueAt(row, 0)) == true) {
					for (int i = 0; i < modelDismode.getRowCount(); i++) {
						if (i != row) {
							modelDismode.setValueAt(false, i, 0);
						}
					}
				} else {
					boolean allFalse = true;
					for (int i = 0; i < modelDismode.getRowCount(); i++) {
						if (((boolean) modelDismode.getValueAt(i, 0)) == true) {
							allFalse = false;
						}
					}
					if (allFalse) {
						modelDismode.setValueAt(true, row, 0);
					}
				}
			}
		});

		JPanel panelMiss = new JPanel();
		panelMiss.setBorder(
				new TitledBorder(null, "Max missed Cleavages", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelMiss, "cell 1 1,grow");
		panelMiss.setLayout(new MigLayout("", "[20][80]", "[20px][20]"));

		comboBox = new JComboBox<Integer>();
		panelMiss.add(comboBox, "cell 1 0,growx,aligny top");
		for (int i = 0; i <= 5; i++) {
			comboBox.addItem(i);
		}

		if (missCleavages <= 5) {
			comboBox.setSelectedIndex(missCleavages);
		}

		tableMode.getSelectionModel().addListSelectionListener(l -> {

		});
	}

	public Enzyme getEnzyme() {
		for (int i = 0; i < this.modelEnzyme.getRowCount(); i++) {
			if (((boolean) modelEnzyme.getValueAt(i, 0)) == true) {
				return Enzyme.values()[i];
			}
		}
		return Enzyme.Trypsin;
	}

	public String getPFindEnzyme() {
		for (int i = 0; i < this.pfindModelEnzyme.getRowCount(); i++) {
			if (((boolean) pfindModelEnzyme.getValueAt(i, 0)) == true) {
				return this.pfindEnzymes[i][0];
			}
		}
		return Enzyme.Trypsin.getName();
	}

	public String[] getFragpipeEnzyme() {
		for (int i = 0; i < this.pfindModelEnzyme.getRowCount(); i++) {
			if (((boolean) pfindModelEnzyme.getValueAt(i, 0)) == true) {
				return this.pfindEnzymes[i];
			}
		}
		return new String[] {Enzyme.Trypsin.getName(), "KR", ""};
	}

	public int getDigestMode() {
		for (int i = 0; i < this.modelDismode.getRowCount(); i++) {
			if (((boolean) modelDismode.getValueAt(i, 0)) == true) {
				return i;
			}
		}
		return 0;
	}

	public int getMissCleavages() {
		return comboBox.getSelectedIndex();
	}

}
