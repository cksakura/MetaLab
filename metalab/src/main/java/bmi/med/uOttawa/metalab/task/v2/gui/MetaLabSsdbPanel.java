package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;
import net.miginfocom.swing.MigLayout;

import java.awt.Component;

import javax.swing.ButtonGroup;

public class MetaLabSsdbPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2951333826015460087L;

	private JRadioButton rdbtnYesSsdb;
	private JRadioButton rdbtnNoSsdb;
	private JLabel lblSpectraClustering;
	private JRadioButton rdbtnYesCluster;
	private JRadioButton rdbtnNoCluster;
	private JLabel lblEvalueThreshold;
	private JTextField textFieldEvalue;
	private JLabel lblPsmFdrThreshold;
	private JTextField textFieldFdr;
	private JLabel lblUseOpenSearch;
	private JRadioButton rdbtnYesOpen;
	private JRadioButton rdbtnNoOpen;
	private final ButtonGroup buttonGroupOpen = new ButtonGroup();

	/**
	 * Create the panel.
	 * 
	 * @wbp.parser.constructor
	 */
	public MetaLabSsdbPanel(MetaSsdbCreatePar scp) {
		this(scp, false);
	}

	/**
	 * Create the panel.
	 */
	public MetaLabSsdbPanel(MetaSsdbCreatePar scp, boolean isPFind) {

		setLayout(new MigLayout("", "[120][20][100]", "[35][35][35][35][35]"));

		JLabel lblGeneSsdb = new JLabel("Generate sample specific database");
		add(lblGeneSsdb, "cell 0 0");

		ButtonGroup buttonGroupSsdb = new ButtonGroup();

		rdbtnYesSsdb = new JRadioButton("Yes");
		buttonGroupSsdb.add(rdbtnYesSsdb);
		add(rdbtnYesSsdb, "flowx,cell 2 0");
		rdbtnYesSsdb.setSelected(scp.isSsdb());
		rdbtnYesSsdb.addActionListener(l -> {
			if (rdbtnYesSsdb.isSelected()) {
				lblSpectraClustering.setEnabled(true);
				rdbtnYesCluster.setEnabled(true);
				rdbtnNoCluster.setEnabled(true);
				if (isPFind) {
					lblUseOpenSearch.setEnabled(true);
					rdbtnYesOpen.setEnabled(true);
					rdbtnNoOpen.setEnabled(true);
				}
				if (rdbtnYesCluster.isSelected()) {
					lblEvalueThreshold.setEnabled(true);
					textFieldEvalue.setEnabled(true);
					lblPsmFdrThreshold.setEnabled(false);
					textFieldFdr.setEnabled(false);
				} else {
					lblEvalueThreshold.setEnabled(false);
					textFieldEvalue.setEnabled(false);
					lblPsmFdrThreshold.setEnabled(true);
					textFieldFdr.setEnabled(true);
				}
			} else {
				lblSpectraClustering.setEnabled(false);
				rdbtnYesCluster.setEnabled(false);
				rdbtnNoCluster.setEnabled(false);
				lblUseOpenSearch.setEnabled(false);
				rdbtnYesOpen.setEnabled(false);
				rdbtnNoOpen.setEnabled(false);
				lblEvalueThreshold.setEnabled(false);
				textFieldEvalue.setEnabled(false);
				lblPsmFdrThreshold.setEnabled(false);
				textFieldFdr.setEnabled(false);
			}
		});

		rdbtnNoSsdb = new JRadioButton("No");
		buttonGroupSsdb.add(rdbtnNoSsdb);
		add(rdbtnNoSsdb, "cell 2 0");
		rdbtnNoSsdb.setSelected(!scp.isSsdb());
		rdbtnNoSsdb.addActionListener(l -> {
			if (!rdbtnNoSsdb.isSelected()) {
				lblSpectraClustering.setEnabled(true);
				rdbtnYesCluster.setEnabled(true);
				rdbtnNoCluster.setEnabled(true);
				if (isPFind) {
					lblUseOpenSearch.setEnabled(true);
					rdbtnYesOpen.setEnabled(true);
					rdbtnNoOpen.setEnabled(true);
				}
				if (rdbtnYesCluster.isSelected()) {
					lblEvalueThreshold.setEnabled(true);
					textFieldEvalue.setEnabled(true);
					lblPsmFdrThreshold.setEnabled(false);
					textFieldFdr.setEnabled(false);
				} else {
					lblEvalueThreshold.setEnabled(false);
					textFieldEvalue.setEnabled(false);
					lblPsmFdrThreshold.setEnabled(true);
					textFieldFdr.setEnabled(true);
				}
			} else {
				lblSpectraClustering.setEnabled(false);
				rdbtnYesCluster.setEnabled(false);
				rdbtnNoCluster.setEnabled(false);
				lblUseOpenSearch.setEnabled(false);
				rdbtnYesOpen.setEnabled(false);
				rdbtnNoOpen.setEnabled(false);
				lblEvalueThreshold.setEnabled(false);
				textFieldEvalue.setEnabled(false);
				lblPsmFdrThreshold.setEnabled(false);
				textFieldFdr.setEnabled(false);
			}
		});

		lblSpectraClustering = new JLabel("Spectra clustering in first search");
		add(lblSpectraClustering, "cell 0 1");

		ButtonGroup buttonGroupCluster = new ButtonGroup();
		rdbtnYesCluster = new JRadioButton("Yes");
		buttonGroupCluster.add(rdbtnYesCluster);
		add(rdbtnYesCluster, "flowx,cell 2 1");
		rdbtnYesCluster.setSelected(scp.isCluster());
		rdbtnYesCluster.addActionListener(l -> {
			if (rdbtnYesCluster.isSelected()) {
				lblEvalueThreshold.setEnabled(true);
				textFieldEvalue.setEnabled(true);
				lblPsmFdrThreshold.setEnabled(false);
				textFieldFdr.setEnabled(false);

			} else {
				lblEvalueThreshold.setEnabled(false);
				textFieldEvalue.setEnabled(false);
				lblPsmFdrThreshold.setEnabled(true);
				textFieldFdr.setEnabled(true);
			}
		});

		rdbtnNoCluster = new JRadioButton("No");
		buttonGroupCluster.add(rdbtnNoCluster);
		add(rdbtnNoCluster, "cell 2 1");
		rdbtnNoCluster.setSelected(!scp.isCluster());
		rdbtnNoCluster.addActionListener(l -> {
			if (!rdbtnNoCluster.isSelected()) {
				lblEvalueThreshold.setEnabled(true);
				textFieldEvalue.setEnabled(true);
				lblPsmFdrThreshold.setEnabled(false);
				textFieldFdr.setEnabled(false);
			} else {
				lblEvalueThreshold.setEnabled(false);
				textFieldEvalue.setEnabled(false);
				lblPsmFdrThreshold.setEnabled(true);
				textFieldFdr.setEnabled(true);
			}
		});

		lblUseOpenSearch = new JLabel("Use open search");
		add(lblUseOpenSearch, "cell 0 2");

		rdbtnYesOpen = new JRadioButton("Yes");
		rdbtnYesOpen.setSelected(scp.isOpen());
		buttonGroupOpen.add(rdbtnYesOpen);
		add(rdbtnYesOpen, "flowx,cell 2 2");

		rdbtnNoOpen = new JRadioButton("No");
		rdbtnNoOpen.setSelected(!scp.isOpen());
		buttonGroupOpen.add(rdbtnNoOpen);
		add(rdbtnNoOpen, "cell 2 2");

		if (!isPFind) {
			rdbtnYesOpen.setSelected(false);
			rdbtnNoOpen.setSelected(true);
			rdbtnYesOpen.setEnabled(false);
			rdbtnNoOpen.setEnabled(false);
		}

		lblEvalueThreshold = new JLabel("PSM E-value threshold");
		add(lblEvalueThreshold, "cell 0 3");

		textFieldEvalue = new JTextField();
		textFieldEvalue.setToolTipText("0.0 represent no filtering is applied");
		add(textFieldEvalue, "cell 2 3,growx");
		textFieldEvalue.setColumns(10);
		textFieldEvalue.setText(String.valueOf(scp.getXtandemEvalue()));

		lblPsmFdrThreshold = new JLabel("PSM FDR threshold");
		add(lblPsmFdrThreshold, "cell 0 4");

		textFieldFdr = new JTextField();
		add(textFieldFdr, "cell 2 4,growx");
		textFieldFdr.setColumns(10);
		textFieldFdr.setText(String.valueOf(scp.getXtandemFDR()));

		if (scp.isSsdb()) {
			lblSpectraClustering.setEnabled(true);
			rdbtnYesCluster.setEnabled(true);
			rdbtnNoCluster.setEnabled(true);
			lblEvalueThreshold.setEnabled(true);
			textFieldEvalue.setEnabled(true);
			lblPsmFdrThreshold.setEnabled(true);
			textFieldFdr.setEnabled(true);
		} else {
			lblSpectraClustering.setEnabled(false);
			rdbtnYesCluster.setEnabled(false);
			rdbtnNoCluster.setEnabled(false);
			lblEvalueThreshold.setEnabled(false);
			textFieldEvalue.setEnabled(false);
			lblPsmFdrThreshold.setEnabled(false);
			textFieldFdr.setEnabled(false);
		}

		if (scp.isCluster()) {
			lblEvalueThreshold.setEnabled(true);
			textFieldEvalue.setEnabled(true);
			lblPsmFdrThreshold.setEnabled(false);
			textFieldFdr.setEnabled(false);
		} else {
			lblEvalueThreshold.setEnabled(false);
			textFieldEvalue.setEnabled(false);
			lblPsmFdrThreshold.setEnabled(true);
			textFieldFdr.setEnabled(true);
		}
	}

	public MetaSsdbCreatePar getSsdbPar() {

		boolean ssdb = this.rdbtnYesSsdb.isSelected();
		boolean cluster = this.rdbtnYesCluster.isSelected();
		boolean isopen = this.rdbtnYesOpen.isSelected();
		double xtandemFDR = 0.01;
		double xtandemEvalue = 0.05;

		try {
			xtandemFDR = Double.parseDouble(this.textFieldFdr.getText());
		} catch (NumberFormatException e) {
			System.err.println("Error in parsing number from PSM FDR threshold");
			xtandemFDR = 0.01;
		}

		try {
			xtandemEvalue = Double.parseDouble(this.textFieldEvalue.getText());
		} catch (NumberFormatException e) {
			System.err.println("Error in parsing number from PSM E-value threshold");
			xtandemEvalue = 0.05;
		}

		MetaSsdbCreatePar mscp = new MetaSsdbCreatePar(ssdb, cluster, isopen, xtandemFDR, xtandemEvalue);
		return mscp;
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
