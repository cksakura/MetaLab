package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;
import net.miginfocom.swing.MigLayout;

public class MetaLabPFindPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5087797470901962063L;
	private JTextField textFieldPsmFDR;
	private JTextField textFieldProFDR;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JRadioButton rdbtnOpenSearch;
	private AbstractButton rdbtnYes;

	/**
	 * Create the panel.
	 */
	public MetaLabPFindPanel(MetaOpenPar osp) {
		setLayout(new MigLayout("", "[10][100][30][100][30][100]", "[40][40][40][40]"));

		JLabel lblSearchType = new JLabel("Search type");
		add(lblSearchType, "cell 1 0");

		rdbtnOpenSearch = new JRadioButton("Open search");
		buttonGroup.add(rdbtnOpenSearch);
		rdbtnOpenSearch.setSelected(true);
		add(rdbtnOpenSearch, "cell 3 0");

		JRadioButton rdbtnClosedSearch = new JRadioButton("Closed search");
		buttonGroup.add(rdbtnClosedSearch);
		add(rdbtnClosedSearch, "cell 5 0");

		JLabel lblPsmFdrThreshold = new JLabel("PSM FDR threshold");
		add(lblPsmFdrThreshold, "cell 1 1");

		textFieldPsmFDR = new JTextField(String.valueOf(osp.getOpenPsmFDR()));
		add(textFieldPsmFDR, "cell 3 1,growx");
		textFieldPsmFDR.setColumns(10);

		JLabel lblProteinFdrThreshold = new JLabel("Protein FDR threshold");
		add(lblProteinFdrThreshold, "cell 1 2");

		textFieldProFDR = new JTextField(String.valueOf(osp.getOpenProFDR()));
		add(textFieldProFDR, "cell 3 2,growx");
		textFieldProFDR.setColumns(10);

		JLabel lblMatchBetweenRuns = new JLabel("Match between runs");
		add(lblMatchBetweenRuns, "cell 1 3");

		rdbtnYes = new JRadioButton("Yes");
		buttonGroup_1.add(rdbtnYes);
		rdbtnYes.setSelected(true);
		add(rdbtnYes, "cell 3 3");

		JRadioButton rdbtnNo = new JRadioButton("No");
		buttonGroup_1.add(rdbtnNo);
		add(rdbtnNo, "cell 5 3");
	}

	public MetaOpenPar getOpenPar() {
		boolean isOpenSearch = rdbtnOpenSearch.isSelected();
		double psmfdr = Double.parseDouble(this.textFieldPsmFDR.getText());
		double pepfdr = 0.01;
		double profdr = Double.parseDouble(this.textFieldProFDR.getText());
		double r2 = 0.9;
		boolean matchBetweenRuns = rdbtnYes.isSelected();
		MetaOpenPar mop = new MetaOpenPar(isOpenSearch, psmfdr, pepfdr, profdr, r2, matchBetweenRuns);
		return mop;
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
}
