package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;
import net.miginfocom.swing.MigLayout;

public class DiaNNSearchLibPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3937649923872682739L;

	/**
	 * Create the panel.
	 */
	public DiaNNSearchLibPanel(DiaLibSearchPar par) {

		setLayout(new MigLayout("", "[200:320:450,grow][200:320:450,grow]", "[30][30][30][30][30][30][30][15][30]"));

		JLabel lblMassAccu = new JLabel("Mass accuracy (PPM)");
		add(lblMassAccu, "cell 0 0,alignx center");

		JComboBox<Double> comboBoxMassAccu = new JComboBox<Double>();
		add(comboBoxMassAccu, "cell 1 0,grow");
		for (int i = 0; i < 100; i++) {
			comboBoxMassAccu.addItem((double) i);
		}
		comboBoxMassAccu.addActionListener(l -> {
			double massAccu = comboBoxMassAccu.getItemAt(comboBoxMassAccu.getSelectedIndex());
			par.setMassAccu(massAccu);
		});
		for (int i = 1; i < comboBoxMassAccu.getItemCount(); i++) {
			double itemI = comboBoxMassAccu.getItemAt(i);
			if (itemI > par.getMassAccu()) {
				comboBoxMassAccu.setSelectedIndex(i - 1);
				break;
			}
		}

		JLabel lblMS1Accu = new JLabel("MS1 accuracy (PPM)");
		add(lblMS1Accu, "cell 0 1,alignx center");

		JComboBox<Double> comboBoxMS1Accu = new JComboBox<Double>();
		add(comboBoxMS1Accu, "cell 1 1,grow");
		for (int i = 0; i < 100; i++) {
			comboBoxMS1Accu.addItem((double) i);
		}
		comboBoxMS1Accu.addActionListener(l -> {
			double ms1Accu = comboBoxMS1Accu.getItemAt(comboBoxMS1Accu.getSelectedIndex());
			par.setMs1Accu((int) ms1Accu);
		});
		for (int i = 1; i < comboBoxMS1Accu.getItemCount(); i++) {
			double itemI = comboBoxMS1Accu.getItemAt(i);
			if (itemI > par.getMs1Accu()) {
				comboBoxMS1Accu.setSelectedIndex(i - 1);
				break;
			}
		}

		JLabel lblScanWin = new JLabel("Scan window");
		add(lblScanWin, "cell 0 2,alignx center");

		JComboBox<Double> comboBoxScanWin = new JComboBox<Double>();
		add(comboBoxScanWin, "cell 1 2,grow");
		for (int i = 0; i < 100; i++) {
			comboBoxScanWin.addItem((double) i);
		}
		comboBoxScanWin.addActionListener(l -> {
			double scanWin = comboBoxScanWin.getItemAt(comboBoxScanWin.getSelectedIndex());
			par.setScanWin((int) scanWin);
		});
		for (int i = 1; i < comboBoxScanWin.getItemCount(); i++) {
			double itemI = comboBoxScanWin.getItemAt(i);
			if (itemI > par.getScanWin()) {
				comboBoxScanWin.setSelectedIndex(i - 1);
				break;
			}
		}

		JLabel lblMBR = new JLabel("MBR (match-between-runs)");
		add(lblMBR, "cell 0 3,alignx center");

		JCheckBox chckbxMBR = new JCheckBox("Yes");
		add(chckbxMBR, "cell 1 3");
		chckbxMBR.addActionListener(l -> {
			par.setMbr(chckbxMBR.isSelected());
		});
		chckbxMBR.setSelected(par.isMbr());

		JLabel lblQuanStra = new JLabel("Quantification strategy");
		add(lblQuanStra, "cell 0 4,alignx center");

		JComboBox<String> comboBoxQuanStra = new JComboBox<String>();
		add(comboBoxQuanStra, "cell 1 4,grow");
		comboBoxQuanStra.addItem("Any LC (high accuracy)");
		comboBoxQuanStra.addItem("Any LC (high precision)");
		comboBoxQuanStra.addItem("Robust LC (high accuracy)");
		comboBoxQuanStra.addItem("Robust LC (high precision)");
		comboBoxQuanStra.addActionListener(l -> {
			par.setQuanStrategyId(comboBoxQuanStra.getSelectedIndex());
		});
		if (par.getQuanStrategyId() >= 0 && par.getQuanStrategyId() < 4) {
			comboBoxQuanStra.setSelectedIndex(par.getQuanStrategyId());
		}

		JLabel lblThread = new JLabel("Number of threads");
		add(lblThread, "cell 0 5,alignx center");
		lblThread.setBackground(new Color(190, 109, 183));
		lblThread.setOpaque(true);

		int totalCount = Runtime.getRuntime().availableProcessors();
		JComboBox<Integer> threadComboBox = new JComboBox<Integer>();
		add(threadComboBox, "cell 1 5,grow");
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
			threadComboBox.addItem(i);
		}

		int threadCount = par.getThreadCount();
		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}
		threadComboBox.addActionListener(l -> {
			par.setThreadCount(threadComboBox.getItemAt(threadComboBox.getSelectedIndex()));
		});

		JButton btnDefault = new JButton("Restore default settings");
		add(btnDefault, "cell 1 7,grow");
		btnDefault.setOpaque(true);
		btnDefault.setBackground(new Color(253, 211, 106));
		btnDefault.addActionListener(l -> {
			comboBoxMassAccu.setSelectedIndex(10);
			par.setMassAccu(10);

			comboBoxMS1Accu.setSelectedIndex(5);
			par.setMs1Accu(5);

			comboBoxScanWin.setSelectedIndex(10);
			par.setScanWin(10);

			chckbxMBR.setSelected(true);
			par.setMbr(true);

			comboBoxQuanStra.setSelectedIndex(0);
			par.setQuanStrategyId(0);
		});
	}
}
