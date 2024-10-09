/**
 * 
 */
package bmi.med.uOttawa.metalab.core.model;

import javax.swing.table.AbstractTableModel;

/**
 * @author Kai Cheng
 *
 */
public class PagedTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7717838253202777756L;

	private String[] title;
	private RowObject[] rows;
	private int pageRowCount;
	private int currentPageRowCount;
	private int currentPage;

	public PagedTableModel(String[] title, RowObject[] rows, int currentPage, int pageRowCount) {
		this.title = title;
		this.rows = rows;
		this.currentPage = currentPage;
		this.pageRowCount = pageRowCount;

		int count = rows.length - (currentPage - 1) * pageRowCount;
		if (count <= pageRowCount) {
			this.currentPageRowCount = count;
		} else {
			this.currentPageRowCount = pageRowCount;
		}
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
		int count = rows.length - (currentPage - 1) * pageRowCount;
		if (count <= pageRowCount) {
			this.currentPageRowCount = count;
		} else {
			this.currentPageRowCount = pageRowCount;
		}
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return "ID";
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
		return currentPageRowCount;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		int id = (currentPage - 1) * pageRowCount + arg0 + 1;
		if (arg1 == 0) {
			return id;
		} else {
			return rows[id - 1].getObject(arg1 - 1);
		}
	}

}
