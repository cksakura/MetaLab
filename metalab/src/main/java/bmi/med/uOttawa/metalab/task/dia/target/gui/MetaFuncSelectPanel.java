package bmi.med.uOttawa.metalab.task.dia.target.gui;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.target.MetaDiaTargetDBTask;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MetaFuncSelectPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7053808615064033074L;
	private JTable table;
	private JComboBox<String> comboBox;
	private JScrollPane scrollPane;
	
	public MetaFuncSelectPanel(MetaParameterDia metaPar, MetaSourcesDia msd) {
		setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Functions",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[380:590:840,grow]", "[30][][180:300:420,grow][100]"));

		comboBox = new JComboBox<String>();
		add(comboBox, "cell 0 1,growx");
		comboBox.addItem("Choose the functional level");
		for (int i = 0; i < MetaDiaTargetDBTask.FUNCTIONS.length; i++) {
			comboBox.addItem(MetaDiaTargetDBTask.FUNCTIONS[i]);
		}

		scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 2,grow");

		table = new JTable();
		scrollPane.setViewportView(table);
	}

	public JTable getTable() {
		return table;
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}
	
	public void setTable(JTable funcTable) {
		this.table = funcTable;
		scrollPane.setViewportView(table);
	}

	public HashSet<String> getSelectedFunctions() {
		HashSet<String> set = new HashSet<String>();
		if (this.table == null) {
			return null;
		}
		TableModel model = this.table.getModel();
		if (model == null) {
			return null;
		}

		for (int i = 0; i < model.getRowCount(); i++) {
			boolean select = (boolean) model.getValueAt(i, 0);
			if (select) {
				set.add(model.getValueAt(i, 1).toString());
			}
		}

		return set;
	}
}
