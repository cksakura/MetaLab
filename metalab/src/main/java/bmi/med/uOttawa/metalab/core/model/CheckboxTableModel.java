/**
 * 
 */
package bmi.med.uOttawa.metalab.core.model;

import javax.swing.table.AbstractTableModel;

/**
 * @author Kai Cheng
 *
 */
public class CheckboxTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5000733993613495241L;

	private String[] title;
	private RowObject[] rows;
	private boolean[] selected;

	public CheckboxTableModel(String[] title, RowObject[] rows, boolean[] selected) {
		this.title = title;
		this.rows = rows;
		this.selected = selected;
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return "Select";
		else
			return title[columnIndex - 1];
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return title.length + 1;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return rows.length;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0)
			return Boolean.class;
		return String.class;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		if (arg1 == 0) {
			return selected[arg0];
		} else {
			return rows[arg0].getObject(arg1 - 1);
		}
	}

	@Override
	public void setValueAt(Object object, int row, int column) {
		if (column == 0) {
			selected[row] = (Boolean) object;
		}
		fireTableCellUpdated(row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 0)
			return true;
		return false;
	}
}
