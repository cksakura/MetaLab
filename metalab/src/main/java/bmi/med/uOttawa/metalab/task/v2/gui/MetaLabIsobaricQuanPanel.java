/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.math.NumberUtils;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MaxQuantModIO;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import net.miginfocom.swing.MigLayout;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabIsobaricQuanPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7505439065591416288L;

	private JScrollPane isobaricScrollPane_1;
	private JScrollPane isobaricScrollPane_2;

	private JTable isobaricTable1;
	private DefaultTableModel isobaricModel1;

	private JTable isobaricTable2;
	private DefaultTableModel isobaricModel2;
	private int totalTagCount;

	private MaxquantModification[] mods = MaxQuantModIO.getIsobaricLabels();

	/**
	 * Create the panel. MaxQuant
	 */
	public MetaLabIsobaricQuanPanel(MetaParameterMQ par) {

		setLayout(new MigLayout("", "[320][10][420]", "[][100,grow 250][][100,grow 250][][100,grow 250]"));

		isobaricScrollPane_1 = new JScrollPane();
		add(isobaricScrollPane_1, "cell 0 1 1 5,grow");

		isobaricScrollPane_2 = new JScrollPane();
		add(isobaricScrollPane_2, "cell 2 1 1 5,grow");

		HashMap<String, double[]> isomap = new HashMap<String, double[]>();
		if (par.getIsobaric() != null) {
			MaxquantModification[] isobarics = par.getIsobaric();
			double[][] isoCorFactor = par.getIsoCorFactor();
			for (int i = 0; i < isobarics.length; i++) {
				isomap.put(isobarics[i].getTitle(), isoCorFactor[i]);
			}
		}

		this.isobaricTable1 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2993376790352324890L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable1.setBackground(new Color(185, 238, 172));

		this.isobaricTable2 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3334820538694443913L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable2.setBackground(new Color(185, 238, 172));

		IsobaricTag[] isobaricTages = IsobaricTag.getMaxQuantTags();
		Object[][] isobaricExps = new Object[isobaricTages.length][2];
		String[][] isobraicTagNames = new String[isobaricTages.length][];

		for (int i = 0; i < isobaricExps.length; i++) {

			if (par.getIsobaricTag() != null && par.getIsobaricTag() == isobaricTages[i]) {
				isobaricExps[i][0] = true;
			} else {
				isobaricExps[i][0] = false;
			}

			String tagNamei = isobaricTages[i].getName();
			isobaricExps[i][1] = tagNamei;
			ArrayList<String> list = new ArrayList<String>();

			if (isobaricTages[i] == IsobaricTag.tmt11) {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(IsobaricTag.tmt11.getName())) {
						list.add(title);
					} else if (title.startsWith(IsobaricTag.tmt10.getName())) {
						list.add(title);
					}
				}
			} else if (isobaricTages[i] == IsobaricTag.tmtpro18) {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(IsobaricTag.tmtpro18.getName())) {
						list.add(title);
					} else if (title.startsWith(IsobaricTag.tmtpro16.getName())) {
						list.add(title);
					}
				}
			} else {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(tagNamei)) {
						list.add(title);
					}
				}
			}

			isobraicTagNames[i] = list.toArray(new String[list.size()]);
			totalTagCount += isobraicTagNames[i].length;
		}

		isobaricModel1 = new DefaultTableModel(isobaricExps, new Object[] { "", "Method" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4717793077013211578L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};
		isobaricTable1.setModel(isobaricModel1);
		isobaricTable1.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable1.getColumnModel().getColumn(1).setPreferredWidth(150);

		Object[] isobaricTable2Title = new Object[] { "", "Tags", "% of -2", "% of -1", "% of 1", "% of 2" };
		isobaricModel2 = new DefaultTableModel(new Object[][] {}, isobaricTable2Title) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2151115679384395709L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex != 1;
			}
		};

		isobaricModel1.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				
				int row = event.getFirstRow();

				boolean select = false;
				if (((boolean) isobaricModel1.getValueAt(row, 0)) == true) {
					for (int i = 0; i < isobaricExps.length; i++) {
						if (i != row) {
							isobaricModel1.setValueAt(false, i, 0);
						}
					}
					Object[][] usedIsobaricLabels = new Object[isobraicTagNames[row].length][6];
					for (int i = 0; i < usedIsobaricLabels.length; i++) {
						usedIsobaricLabels[i][0] = true;
						usedIsobaricLabels[i][1] = isobraicTagNames[row][i];
						double[] corFactor = isomap.containsKey(isobraicTagNames[row][i])
								? isomap.get(isobraicTagNames[row][i])
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							usedIsobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(usedIsobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);

					select = true;
				}

				if (!select) {
					Object[][] isobaricLabels = new Object[mods.length][6];
					for (int i = 0; i < isobaricLabels.length; i++) {
						isobaricLabels[i][0] = false;
						isobaricLabels[i][1] = mods[i].getTitle();
						double[] corFactor = isomap.containsKey(mods[i].getTitle()) ? isomap.get(mods[i].getTitle())
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							isobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
				}
			}
		});
		isobaricScrollPane_1.setViewportView(isobaricTable1);

		Object[][] isobaricLabels = null;
		if (isomap.size() > 0) {
			isobaricLabels = new Object[isomap.size()][6];
			int id = 0;
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				if (isomap.containsKey(title)) {
					isobaricLabels[id][0] = true;
					isobaricLabels[id][1] = title;

					double[] corFactor = isomap.get(title);
					for (int j = 0; j < corFactor.length; j++) {
						isobaricLabels[id][j + 2] = corFactor[j];
					}
					id++;
				}
			}
		} else {
			isobaricLabels = new Object[mods.length][6];
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				isobaricLabels[i][0] = false;
				isobaricLabels[i][1] = title;
				for (int j = 0; j < 4; j++) {
					isobaricLabels[i][j + 2] = 0.0;
				}
			}
		}

		isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
		isobaricTable2.setModel(isobaricModel2);
		isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
		isobaricModel2.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				int row = event.getFirstRow();
				int column = event.getColumn();

				if (column > 1) {
					String obj = (String) isobaricModel2.getValueAt(row, column);
					if (NumberUtils.isParsable(obj)) {
						double value = Double.parseDouble(obj);
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(value, row, column);

						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(value, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(value, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					} else {
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(0.0, row, column);
						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(0.0, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(0.0, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					}
				}
			}
		});
		isobaricScrollPane_2.setViewportView(isobaricTable2);
	}

	/**
	 * Create the panel. pFind
	 */
	public MetaLabIsobaricQuanPanel(MetaParameterPFind par) {

		setLayout(new MigLayout("", "[320][10][420]", "[][100,grow 250][][100,grow 250][][100,grow 250]"));

		isobaricScrollPane_1 = new JScrollPane();
		add(isobaricScrollPane_1, "cell 0 1 1 5,grow");

		isobaricScrollPane_2 = new JScrollPane();
		add(isobaricScrollPane_2, "cell 2 1 1 5,grow");

		HashMap<String, double[]> isomap = new HashMap<String, double[]>();
		if (par.getIsobaric() != null) {
			MaxquantModification[] isobarics = par.getIsobaric();
			double[][] isoCorFactor = par.getIsoCorFactor();
			for (int i = 0; i < isobarics.length; i++) {
				isomap.put(isobarics[i].getTitle(), isoCorFactor[i]);
			}
		}

		this.isobaricTable1 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2993376790352324890L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable1.setBackground(new Color(185, 238, 172));

		this.isobaricTable2 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3334820538694443913L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable2.setBackground(new Color(185, 238, 172));

		IsobaricTag[] isobaricTages = IsobaricTag.getPFindTags();
		Object[][] isobaricExps = new Object[isobaricTages.length][2];
		String[][] isobraicTagNames = new String[isobaricTages.length][];

		for (int i = 0; i < isobaricExps.length; i++) {

			if (par.getIsobaricTag() != null && par.getIsobaricTag() == isobaricTages[i]) {
				isobaricExps[i][0] = true;
			} else {
				isobaricExps[i][0] = false;
			}

			String tagNamei = isobaricTages[i].getName();
			isobaricExps[i][1] = tagNamei;
			ArrayList<String> list = new ArrayList<String>();

			if (isobaricTages[i] == IsobaricTag.tmt11) {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(IsobaricTag.tmt11.getName())) {
						list.add(title);
					} else if (title.startsWith(IsobaricTag.tmt10.getName())) {
						list.add(title);
					}
				}
			} else if (isobaricTages[i] == IsobaricTag.tmtpro18) {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(IsobaricTag.tmtpro18.getName())) {
						list.add(title);
					} else if (title.startsWith(IsobaricTag.tmtpro16.getName())) {
						list.add(title);
					}
				}
			} else {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(tagNamei)) {
						list.add(title);
					}
				}
			}

			isobraicTagNames[i] = list.toArray(new String[list.size()]);
			totalTagCount += isobraicTagNames[i].length;
		}

		isobaricModel1 = new DefaultTableModel(isobaricExps, new Object[] { "", "Method" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4717793077013211578L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};
		isobaricTable1.setModel(isobaricModel1);
		isobaricTable1.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable1.getColumnModel().getColumn(1).setPreferredWidth(150);

		Object[] isobaricTable2Title = new Object[] { "", "Tags", "% of -2", "% of -1", "% of 1", "% of 2" };
		isobaricModel2 = new DefaultTableModel(new Object[][] {}, isobaricTable2Title) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2151115679384395709L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex != 1;
			}
		};

		isobaricModel1.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				
				int row = event.getFirstRow();

				boolean select = false;
				if (((boolean) isobaricModel1.getValueAt(row, 0)) == true) {
					for (int i = 0; i < isobaricExps.length; i++) {
						if (i != row) {
							isobaricModel1.setValueAt(false, i, 0);
						}
					}
					Object[][] usedIsobaricLabels = new Object[isobraicTagNames[row].length][6];
					for (int i = 0; i < usedIsobaricLabels.length; i++) {
						usedIsobaricLabels[i][0] = true;
						usedIsobaricLabels[i][1] = isobraicTagNames[row][i];
						double[] corFactor = isomap.containsKey(isobraicTagNames[row][i])
								? isomap.get(isobraicTagNames[row][i])
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							usedIsobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(usedIsobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);

					select = true;
				}

				if (!select) {
					Object[][] isobaricLabels = new Object[mods.length][6];
					for (int i = 0; i < isobaricLabels.length; i++) {
						isobaricLabels[i][0] = false;
						isobaricLabels[i][1] = mods[i].getTitle();
						double[] corFactor = isomap.containsKey(mods[i].getTitle()) ? isomap.get(mods[i].getTitle())
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							isobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
				}
			}
		});
		isobaricScrollPane_1.setViewportView(isobaricTable1);

		Object[][] isobaricLabels = null;
		if (isomap.size() > 0) {
			isobaricLabels = new Object[isomap.size()][6];
			int id = 0;
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				if (isomap.containsKey(title)) {
					isobaricLabels[id][0] = true;
					isobaricLabels[id][1] = title;

					double[] corFactor = isomap.get(title);
					for (int j = 0; j < corFactor.length; j++) {
						isobaricLabels[id][j + 2] = corFactor[j];
					}
					id++;
				}
			}
		} else {
			isobaricLabels = new Object[mods.length][6];
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				isobaricLabels[i][0] = false;
				isobaricLabels[i][1] = title;
				for (int j = 0; j < 4; j++) {
					isobaricLabels[i][j + 2] = 0.0;
				}
			}
		}

		isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
		isobaricTable2.setModel(isobaricModel2);
		isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
		isobaricModel2.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				int row = event.getFirstRow();
				int column = event.getColumn();

				if (column > 1) {
					String obj = (String) isobaricModel2.getValueAt(row, column);
					if (NumberUtils.isParsable(obj)) {
						double value = Double.parseDouble(obj);
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(value, row, column);

						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(value, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(value, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					} else {
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(0.0, row, column);
						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(0.0, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(0.0, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					}
				}
			}
		});
		isobaricScrollPane_2.setViewportView(isobaricTable2);
	}

	/**
	 * Create the panel.
	 */
	public MetaLabIsobaricQuanPanel() {

		setLayout(new MigLayout("", "[320][10][420]", "[][100,grow 250][][100,grow 250][][100,grow 250]"));

		isobaricScrollPane_1 = new JScrollPane();
		add(isobaricScrollPane_1, "cell 0 1 1 5,grow");

		isobaricScrollPane_2 = new JScrollPane();
		add(isobaricScrollPane_2, "cell 2 1 1 5,grow");

		HashMap<String, double[]> isomap = new HashMap<String, double[]>();

		this.isobaricTable1 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2993376790352324890L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable1.setBackground(new Color(185, 238, 172));

		this.isobaricTable2 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3334820538694443913L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable2.setBackground(new Color(185, 238, 172));

		IsobaricTag[] isobaricTages = IsobaricTag.values();
		Object[][] isobaricExps = new Object[isobaricTages.length][2];
		String[][] isobraicTagNames = new String[isobaricTages.length][];

		for (int i = 0; i < isobaricExps.length; i++) {

			isobaricExps[i][0] = false;

			String tagNamei = isobaricTages[i].getName();
			isobaricExps[i][1] = tagNamei;
			ArrayList<String> list = new ArrayList<String>();

			if (isobaricTages[i] == IsobaricTag.tmt11) {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(IsobaricTag.tmt11.getName())) {
						list.add(title);
					} else if (title.startsWith(IsobaricTag.tmt10.getName())) {
						list.add(title);
					}
				}
			} else {
				for (int j = 0; j < mods.length; j++) {
					String title = mods[j].getTitle();
					if (title.startsWith(tagNamei)) {
						list.add(title);
					}
				}
			}

			isobraicTagNames[i] = list.toArray(new String[list.size()]);
			totalTagCount += isobraicTagNames[i].length;
		}

		isobaricModel1 = new DefaultTableModel(isobaricExps, new Object[] { "", "Method" }) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4717793077013211578L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
		};
		isobaricTable1.setModel(isobaricModel1);
		isobaricTable1.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable1.getColumnModel().getColumn(1).setPreferredWidth(150);

		Object[] isobaricTable2Title = new Object[] { "", "Tags", "% of -2", "% of -1", "% of 1", "% of 2" };
		isobaricModel2 = new DefaultTableModel(new Object[][] {}, isobaricTable2Title) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2151115679384395709L;

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex != 1;
			}
		};

		isobaricModel1.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				
				int row = event.getFirstRow();

				boolean select = false;
				if (((boolean) isobaricModel1.getValueAt(row, 0)) == true) {
					for (int i = 0; i < isobaricExps.length; i++) {
						if (i != row) {
							isobaricModel1.setValueAt(false, i, 0);
						}
					}
					Object[][] usedIsobaricLabels = new Object[isobraicTagNames[row].length][6];
					for (int i = 0; i < usedIsobaricLabels.length; i++) {
						usedIsobaricLabels[i][0] = true;
						usedIsobaricLabels[i][1] = isobraicTagNames[row][i];
						double[] corFactor = isomap.containsKey(isobraicTagNames[row][i])
								? isomap.get(isobraicTagNames[row][i])
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							usedIsobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(usedIsobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);

					select = true;
				}

				if (!select) {
					Object[][] isobaricLabels = new Object[mods.length][6];
					for (int i = 0; i < isobaricLabels.length; i++) {
						isobaricLabels[i][0] = false;
						isobaricLabels[i][1] = mods[i].getTitle();
						double[] corFactor = isomap.containsKey(mods[i].getTitle()) ? isomap.get(mods[i].getTitle())
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							isobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
				}
			}
		});
		isobaricScrollPane_1.setViewportView(isobaricTable1);

		Object[][] isobaricLabels = null;
		if (isomap.size() > 0) {
			isobaricLabels = new Object[isomap.size()][6];
			int id = 0;
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				if (isomap.containsKey(title)) {
					isobaricLabels[id][0] = true;
					isobaricLabels[id][1] = title;

					double[] corFactor = isomap.get(title);
					for (int j = 0; j < corFactor.length; j++) {
						isobaricLabels[id][j + 2] = corFactor[j];
					}
					id++;
				}
			}
		} else {
			isobaricLabels = new Object[mods.length][6];
			for (int i = 0; i < mods.length; i++) {
				String title = mods[i].getTitle();
				isobaricLabels[i][0] = false;
				isobaricLabels[i][1] = title;
				for (int j = 0; j < 4; j++) {
					isobaricLabels[i][j + 2] = 0.0;
				}
			}
		}

		isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
		isobaricTable2.setModel(isobaricModel2);
		isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
		isobaricModel2.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				int row = event.getFirstRow();
				int column = event.getColumn();

				if (column > 1) {
					String obj = (String) isobaricModel2.getValueAt(row, column);
					if (NumberUtils.isCreatable(obj)) {
						double value = Double.parseDouble(obj);
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(value, row, column);

						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(value, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(value, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					} else {
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(0.0, row, column);
						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(0.0, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(0.0, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					}
				}
			}
		});
		isobaricScrollPane_2.setViewportView(isobaricTable2);
	}

	public IsobaricTag getIsobaricTag() {
		for (int i = 0; i < isobaricModel1.getRowCount(); i++) {
			if (((boolean) isobaricModel1.getValueAt(i, 0)) == true) {
				IsobaricTag[] values = IsobaricTag.values();
				for (int j = 0; j < values.length; j++) {
					String name = (String) isobaricModel1.getValueAt(i, 1);
					if (name.equals(values[j].getName())) {
						return values[j];
					}
				}
			}
		}
		return null;
	}

	public MaxquantModification[] getIsobaricTags() {
		HashMap<String, MaxquantModification> isobaricMap = MaxQuantModIO.getIsobaricMap();
		ArrayList<MaxquantModification> list = new ArrayList<MaxquantModification>();
		for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
			if (((boolean) isobaricModel2.getValueAt(i, 0)) == true) {
				list.add(isobaricMap.get((String) isobaricModel2.getValueAt(i, 1)));
			}
		}
		return list.toArray(new MaxquantModification[list.size()]);
	}

	public String[] getLabelTitle() {
		ArrayList<String> list = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();
		Pattern pattern = Pattern.compile("([\\w]*)-[A-Za-z]*([\\d]*[A-Za-z]?)");
		for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
			if (((boolean) isobaricModel2.getValueAt(i, 0)) == true) {
				String name = (String) isobaricModel2.getValueAt(i, 1);
				Matcher matcher = pattern.matcher(name);
				if (matcher.matches()) {
					String channel = matcher.group(1) + matcher.group(2);
					if (!set.contains(channel)) {
						list.add(channel);
						set.add(channel);
					}
				}
			}
		}

		String[] channels = list.toArray(new String[list.size()]);
		return channels;
	}

	public double[][] getIsoCorFactor() {
		ArrayList<double[]> list = new ArrayList<double[]>();
		for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
			if (((boolean) isobaricModel2.getValueAt(i, 0)) == true) {
				double[] factor = new double[4];
				for (int j = 0; j < factor.length; j++) {
					factor[j] = (double) isobaricModel2.getValueAt(i, j + 2);
				}
				list.add(factor);
			}
		}
		return list.toArray(new double[list.size()][]);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] components = getComponents();
		for (Component comp : components) {
			comp.setEnabled(enabled);
		}

		isobaricScrollPane_1.setEnabled(enabled);
		isobaricScrollPane_2.setEnabled(enabled);
		isobaricTable1.setEnabled(enabled);
		isobaricTable2.setEnabled(enabled);

		if (enabled) {
			isobaricTable1.setForeground(Color.BLACK);
			isobaricTable1.getTableHeader().setForeground(Color.BLACK);
			isobaricTable2.setForeground(Color.BLACK);
			isobaricTable2.getTableHeader().setForeground(Color.BLACK);
		} else {
			isobaricTable1.setForeground(Color.LIGHT_GRAY);
			isobaricTable1.getTableHeader().setForeground(Color.LIGHT_GRAY);
			isobaricTable2.setForeground(Color.LIGHT_GRAY);
			isobaricTable2.getTableHeader().setForeground(Color.LIGHT_GRAY);
		}
	}

	public void removeAllLabels() {
		int rowCount = isobaricModel1.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			isobaricModel1.setValueAt(false, i, 0);
		}
	}

	public DefaultTableModel getIsobaricModel1() {
		return isobaricModel1;
	}
	
	
}
