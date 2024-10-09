package bmi.med.uOttawa.metalab.task.v2.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import bmi.med.uOttawa.metalab.task.v2.par.MetaMaxQuantPar;
import net.miginfocom.swing.MigLayout;

public class MetaLabMaxQuantPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2120489243583997833L;
	private JTextField textFieldPepFDR;
	private JTextField textFieldProFDR;
	private JRadioButton rdbtnMbrYes;
	private JTextField mbrTimeTextField;
	private JTextField ionMobiTextField;
	private JTextField alignTimeTextField;
	private JTextField alignIonTextField;
	private JComboBox<Boolean> matchUnidenComboBox;
	private JComboBox<Integer> comboBoxLabelMin;
	private JComboBox<Integer> comboBoxLfqMin;

	/**
	 * Create the panel.
	 */
	public MetaLabMaxQuantPanel(MetaMaxQuantPar csp) {

		setLayout(new MigLayout("", "[200][20][70][70]", "[35][35][35][10][250][35][35]"));

		JLabel lblPeptideFdrThreshold = new JLabel("PSM FDR");
		add(lblPeptideFdrThreshold, "cell 0 0");

		textFieldPepFDR = new JTextField();
		add(textFieldPepFDR, "cell 2 0 2 1,growx,aligny center");
		textFieldPepFDR.setColumns(10);
		textFieldPepFDR.setText(String.valueOf(csp.getClosedPsmFdr()));

		JLabel lblProteinFdrThreshold = new JLabel("Protein FDR");
		add(lblProteinFdrThreshold, "cell 0 1");

		textFieldProFDR = new JTextField();
		add(textFieldProFDR, "cell 2 1 2 1,growx,aligny center");
		textFieldProFDR.setColumns(10);
		textFieldProFDR.setText(String.valueOf(csp.getClosedProFdr()));

		JLabel lblMatchBetweenRuns = new JLabel("Match between runs");
		add(lblMatchBetweenRuns, "cell 0 2");

		ButtonGroup buttonGroupMbr = new ButtonGroup();
		rdbtnMbrYes = new JRadioButton("Yes");
		buttonGroupMbr.add(rdbtnMbrYes);
		add(rdbtnMbrYes, "flowx,cell 2 2");
		rdbtnMbrYes.setSelected(csp.isMbr());

		JRadioButton rdbtnMbrNo = new JRadioButton("No");
		buttonGroupMbr.add(rdbtnMbrNo);
		add(rdbtnMbrNo, "cell 3 2");
		rdbtnMbrNo.setSelected(!csp.isMbr());

		JPanel mbrPanel = new JPanel();
		mbrPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Match between runs parameters",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(mbrPanel, "cell 0 4 4 1,grow");
		mbrPanel.setLayout(new MigLayout("", "[200,grow][50][150]", "[30][30][30][30][30]"));

		JLabel lblMatchTimeWindow = new JLabel("Match time window [min]");
		mbrPanel.add(lblMatchTimeWindow, "cell 0 0");

		mbrTimeTextField = new JTextField();
		mbrTimeTextField.setText(String.valueOf(csp.getMatchTimeWin()));
		mbrPanel.add(mbrTimeTextField, "cell 2 0,growx");
		mbrTimeTextField.setColumns(10);

		JLabel lblMatchIonMobility = new JLabel("Match ion mobility window");
		mbrPanel.add(lblMatchIonMobility, "cell 0 1");

		ionMobiTextField = new JTextField();
		ionMobiTextField.setText(String.valueOf(csp.getMatchIonWin()));
		mbrPanel.add(ionMobiTextField, "cell 2 1,growx");
		ionMobiTextField.setColumns(10);

		JLabel lblAlignmentTimeWindow = new JLabel("Alignment time window [min]");
		mbrPanel.add(lblAlignmentTimeWindow, "cell 0 2");

		alignTimeTextField = new JTextField();
		alignTimeTextField.setText(String.valueOf(csp.getAlignTimeWin()));
		mbrPanel.add(alignTimeTextField, "cell 2 2,growx");
		alignTimeTextField.setColumns(10);

		JLabel lblAlignmentIonMobility = new JLabel("Alignment ion mobility");
		mbrPanel.add(lblAlignmentIonMobility, "cell 0 3");

		alignIonTextField = new JTextField();
		alignIonTextField.setText(String.valueOf(csp.getAlignIonMobility()));
		mbrPanel.add(alignIonTextField, "cell 2 3,growx");
		alignIonTextField.setColumns(10);

		JLabel lblMatchUnidentifiedFeatures = new JLabel("Match unidentified features");
		mbrPanel.add(lblMatchUnidentifiedFeatures, "cell 0 4");

		matchUnidenComboBox = new JComboBox<Boolean>();
		mbrPanel.add(matchUnidenComboBox, "cell 2 4,growx");
		matchUnidenComboBox.addItem(false);
		matchUnidenComboBox.addItem(true);
		if (csp.isMatchUnidenFeature()) {
			matchUnidenComboBox.setSelectedIndex(1);
		} else {
			matchUnidenComboBox.setSelectedIndex(0);
		}

		JLabel lblLabelMinRatio = new JLabel("Label min. ratio count");
		add(lblLabelMinRatio, "cell 0 5");

		comboBoxLabelMin = new JComboBox<Integer>();
		add(comboBoxLabelMin, "cell 2 5 2 1,growx,aligny center");
		for (int i = 1; i <= 5; i++) {
			comboBoxLabelMin.addItem(i);
		}
		if (csp.getMinRatioCount() <= 5) {
			comboBoxLabelMin.setSelectedItem(csp.getMinRatioCount());
		} else {
			comboBoxLabelMin.setSelectedItem(1);
		}

		JLabel lblLfqMinRatio = new JLabel("LFQ min. ratio count");
		add(lblLfqMinRatio, "cell 0 6");

		comboBoxLfqMin = new JComboBox<Integer>();
		add(comboBoxLfqMin, "cell 2 6 2 1,growx,aligny center");
		for (int i = 1; i <= 5; i++) {
			comboBoxLfqMin.addItem(i);
		}
		if (csp.getLfqMinRatioCount() <= 5) {
			comboBoxLfqMin.setSelectedItem(csp.getLfqMinRatioCount());
		} else {
			comboBoxLfqMin.setSelectedItem(1);
		}
	}

	public MetaMaxQuantPar getClosedPar() {

		double closedPsmFdr = Double.parseDouble(textFieldPepFDR.getText());
		double closedProFdr = Double.parseDouble(textFieldProFDR.getText());
		boolean mbr = rdbtnMbrYes.isSelected();

		double matchTimeWin = Double.parseDouble(mbrTimeTextField.getText());
		double matchIonWin = Double.parseDouble(ionMobiTextField.getText());
		double alignTimeWin = Double.parseDouble(alignTimeTextField.getText());
		double alignIonMobility = Double.parseDouble(alignIonTextField.getText());
		boolean matchUnidenFeature = matchUnidenComboBox.getItemAt(matchUnidenComboBox.getSelectedIndex());

		int minRatioCount = comboBoxLabelMin.getItemAt(comboBoxLabelMin.getSelectedIndex());
		int lfqMinRatioCount = comboBoxLfqMin.getItemAt(comboBoxLabelMin.getSelectedIndex());

		MetaMaxQuantPar par = new MetaMaxQuantPar(closedPsmFdr, closedProFdr, mbr, matchTimeWin, matchIonWin,
				alignTimeWin, alignIonMobility, matchUnidenFeature, minRatioCount, lfqMinRatioCount);
		return par;
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
