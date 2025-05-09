package bmi.med.uOttawa.metalab.task.dia.target.gui;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import net.miginfocom.swing.MigLayout;

public class MetaTaxaSelectPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6278610486298034335L;
	private JTable table;
	private JComboBox<String> comboBox;
	private JScrollPane scrollPane;

	/**
	 * Create the panel.
	 */
	public MetaTaxaSelectPanel(MetaParameterDia metaPar, MetaSourcesDia msd) {
		setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Taxa",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[380:590:840,grow]", "[30][][180:300:420,grow][100]"));

		comboBox = new JComboBox<String>();
		add(comboBox, "cell 0 1,growx");
		comboBox.addItem("Choose the taxonomic level");
		TaxonomyRanks[] ranks = TaxonomyRanks.getMainRanks7();
		for (int i = 0; i < ranks.length; i++) {
			comboBox.addItem(ranks[i].getName());
		}
		comboBox.addItem("Genome");

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

	public HashSet<String> getSelectedTaxa() {
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
