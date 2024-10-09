package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

public class MetaLabMSFraggerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4767285300977358357L;
	private JTextField textFieldPsmFDR;
	private JTextField textFieldProFDR;
	private JTextField textFieldPepFDR;
	private JTextField textFieldR2;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JRadioButton rdbtnOpenSearch;
	private AbstractButton rdbtnYes;

	/**
	 * Create the panel.
	 */
	public MetaLabMSFraggerPanel(MetaOpenPar osp) {
		setLayout(new MigLayout("", "[10][100][30][100][30][100]", "[40][40][40][40][40][40]"));

		JLabel lblSearchType = new JLabel("Search type");
		add(lblSearchType, "cell 1 0");

		rdbtnOpenSearch = new JRadioButton("Open search");
		buttonGroup.add(rdbtnOpenSearch);
		rdbtnOpenSearch.setSelected(osp.isOpenSearch());
		add(rdbtnOpenSearch, "cell 3 0");

		JRadioButton rdbtnClosedSearch = new JRadioButton("Closed search");
		buttonGroup.add(rdbtnClosedSearch);
		rdbtnClosedSearch.setSelected(!osp.isOpenSearch());
		add(rdbtnClosedSearch, "cell 5 0");

		JLabel lblPsmFdrThreshold = new JLabel("PSM FDR threshold");
		add(lblPsmFdrThreshold, "cell 1 1");

		textFieldPsmFDR = new JTextField(String.valueOf(osp.getOpenPsmFDR()));
		add(textFieldPsmFDR, "cell 3 1,growx");
		textFieldPsmFDR.setColumns(10);

		JLabel lblPeptideFdrThreshold = new JLabel("Peptide FDR threshold");
		add(lblPeptideFdrThreshold, "cell 1 2");

		textFieldPepFDR = new JTextField(String.valueOf(osp.getOpenPepFDR()));
		add(textFieldPepFDR, "cell 3 2,growx");
		textFieldPepFDR.setColumns(10);

		JLabel lblProteinFdrThreshold = new JLabel("Protein FDR threshold");
		add(lblProteinFdrThreshold, "cell 1 3");

		textFieldProFDR = new JTextField(String.valueOf(osp.getOpenProFDR()));
		add(textFieldProFDR, "cell 3 3,growx");
		textFieldProFDR.setColumns(10);

		JLabel lblGaussianRThreshold = new JLabel("Gaussian R2 threshold");
		add(lblGaussianRThreshold, "cell 1 4");

		textFieldR2 = new JTextField();
		textFieldR2.setText("0.9");
		add(textFieldR2, "cell 3 4,growx");
		textFieldR2.setColumns(10);

		JLabel lblMatchBetweenRuns = new JLabel("Match between runs");
		add(lblMatchBetweenRuns, "cell 1 5");

		rdbtnYes = new JRadioButton("Yes");
		buttonGroup_1.add(rdbtnYes);
		rdbtnYes.setSelected(osp.isMatchBetweenRuns());
		add(rdbtnYes, "cell 3 5");

		JRadioButton rdbtnNo = new JRadioButton("No");
		buttonGroup_1.add(rdbtnNo);
		rdbtnNo.setSelected(!osp.isMatchBetweenRuns());
		add(rdbtnNo, "cell 5 5");

	}

	public MetaOpenPar getOpenPar() {
		boolean isOpenSearch = rdbtnOpenSearch.isSelected();
		double psmfdr = Double.parseDouble(this.textFieldPsmFDR.getText());
		double pepfdr = Double.parseDouble(this.textFieldPepFDR.getText());
		double profdr = Double.parseDouble(this.textFieldProFDR.getText());
		double r2 = Double.parseDouble(this.textFieldR2.getText());
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
