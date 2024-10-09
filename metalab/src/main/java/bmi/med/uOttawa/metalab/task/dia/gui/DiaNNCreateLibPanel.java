package bmi.med.uOttawa.metalab.task.dia.gui;

import javax.swing.JPanel;

import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.dia.par.MetaDiaParGenLib;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;

import javax.swing.JCheckBox;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.JButton;

public class DiaNNCreateLibPanel extends JPanel {

	private MetaDiaParGenLib diaParGenLib;

	private JComboBox<String> comboBoxEnzyme;
	private JComboBox<String> comboBoxMaxMiss;
	private JCheckBox chckbxFixCarbami;
	private JCheckBox chckbxMetExci;
	private JComboBox<String> comboBoxMaxVariMod;
	private JCheckBox chckbxVariOxi;
	private JCheckBox chckbxVariAce;
	private JCheckBox chckbxVariPhos;
	private JCheckBox chckbxVariUbi;
	private JComboBox<String> comboBoxPepLen1;
	private JComboBox<String> comboBoxPepLen2;
	private JComboBox<String> comboBoxPreChar1;
	private JComboBox<String> comboBoxPreChar2;
	private JComboBox<String> comboBoxPreMz1;
	private JComboBox<String> comboBoxPreMz2;
	private JComboBox<String> comboBoxFragMz1;
	private JComboBox<String> comboBoxFragMz2;
	private JComboBox<String> comboBoxPreFdr;
	private JComboBox<String> comboBoxPepDetect;

	private JComboBox<String> threadComboBox;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8643605478852461748L;

	/**
	 * Create the panel.
	 */
	public DiaNNCreateLibPanel(MetaDiaParGenLib diaParGenLib) {

		this.diaParGenLib = diaParGenLib;
		DiannParameter par = diaParGenLib.getDiannPar();

		setLayout(new MigLayout("", "[180:320:360,grow][100:240:320,grow][100:240:320,grow]",
				"[30][30][30][30][30][30][30][30][30][30][30][30][30][30][30][30][15][30]"));

		JLabel lblEnzyme = new JLabel("Enzyme");
		add(lblEnzyme, "cell 0 0,alignx center");

		JComboBox<String> comboBoxEnzyme = new JComboBox<String>();
		add(comboBoxEnzyme, "cell 1 0,grow");
		comboBoxEnzyme.addItem("Trypsin/P");
		comboBoxEnzyme.addItem("Trypsin");
		comboBoxEnzyme.addItem("Lys-C");
		comboBoxEnzyme.addItem("ChymoTrypsin");
		comboBoxEnzyme.addItem("AspN");
		comboBoxEnzyme.addItem("GluC");
		comboBoxEnzyme.setSelectedItem(par.getEnzyme());
		comboBoxEnzyme.addActionListener(l -> {
			par.setEnzyme(comboBoxEnzyme.getItemAt(comboBoxEnzyme.getSelectedIndex()));
		});

		JLabel lblMaxMiss = new JLabel("Max missed cleavages");
		add(lblMaxMiss, "cell 0 1,alignx center");

		JComboBox<Integer> comboBoxMaxMiss = new JComboBox<Integer>();
		add(comboBoxMaxMiss, "cell 1 1,grow");
		for (int i = 0; i <= 5; i++) {
			comboBoxMaxMiss.addItem(i);
		}
		comboBoxMaxMiss.setSelectedItem(par.getMissed_cleavages());
		comboBoxMaxMiss.addActionListener(l -> {
			par.setMissed_cleavages(comboBoxMaxMiss.getItemAt(comboBoxMaxMiss.getSelectedIndex()));
		});

		JLabel lblFixCarbami = new JLabel("Fixed Mod. Carbamidomethyl (C)");
		add(lblFixCarbami, "cell 0 2,alignx center");

		JCheckBox chckbxFixCarbami = new JCheckBox("Yes");
		add(chckbxFixCarbami, "cell 1 2");
		chckbxFixCarbami.setSelected(par.isCarbami_C());
		chckbxFixCarbami.addActionListener(l -> {
			par.setCarbami_C(chckbxFixCarbami.isSelected());
		});

		JLabel lblMetExci = new JLabel("N-term Met excision");
		add(lblMetExci, "cell 0 3,alignx center");

		JCheckBox chckbxMetExci = new JCheckBox("Yes");
		add(chckbxMetExci, "cell 1 3");
		chckbxMetExci.setSelected(par.isM_excision());
		chckbxMetExci.addActionListener(l -> {
			par.setM_excision(chckbxMetExci.isSelected());
		});

		JLabel lblMaxVariMod = new JLabel("Max variable Mod. count");
		add(lblMaxVariMod, "cell 0 4,alignx center");

		JComboBox<Integer> comboBoxMaxVariMod = new JComboBox<Integer>();
		add(comboBoxMaxVariMod, "cell 1 4,grow");
		for (int i = 0; i <= 5; i++) {
			comboBoxMaxVariMod.addItem(i);
		}
		comboBoxMaxVariMod.setSelectedItem(par.getMaxMod());
		comboBoxMaxVariMod.addActionListener(l -> {
			par.setMaxMod(comboBoxMaxVariMod.getItemAt(comboBoxMaxVariMod.getSelectedIndex()));
		});

		JLabel lblVariOxi = new JLabel("Variable Mod. Oxidation (M)");
		add(lblVariOxi, "cell 0 5,alignx center");

		JCheckBox chckbxVariOxi = new JCheckBox("Yes");
		add(chckbxVariOxi, "cell 1 5");
		chckbxVariOxi.setSelected(par.isOxidation_M());
		chckbxVariOxi.addActionListener(l -> {
			par.setOxidation_M(chckbxVariOxi.isSelected());
			if (chckbxVariOxi.isSelected()) {
				if (comboBoxMaxVariMod.getSelectedIndex() == 0) {
					comboBoxMaxVariMod.setSelectedIndex(1);
					par.setMaxMod(1);
				}
			}
		});

		JLabel lblVariAc = new JLabel("Variable Mod. Acetylation (Protein N-term)");
		add(lblVariAc, "cell 0 6,alignx center");

		JCheckBox chckbxVariAce = new JCheckBox("Yes");
		add(chckbxVariAce, "cell 1 6");
		chckbxVariAce.setSelected(par.isAcety_proN());
		chckbxVariAce.addActionListener(l -> {
			par.setAcety_proN(chckbxVariAce.isSelected());
			if (chckbxVariAce.isSelected()) {
				if (comboBoxMaxVariMod.getSelectedIndex() == 0) {
					comboBoxMaxVariMod.setSelectedIndex(1);
					par.setMaxMod(1);
				}
			}
		});

		JLabel lblVariPhos = new JLabel("Variable Mod. Phosphorylation (STY)");
		add(lblVariPhos, "cell 0 7,alignx center");

		JCheckBox chckbxVariPhos = new JCheckBox("Yes");
		add(chckbxVariPhos, "cell 1 7");
		chckbxVariPhos.setSelected(par.isPhospho_STY());
		chckbxVariPhos.addActionListener(l -> {
			par.setPhospho_STY(chckbxVariPhos.isSelected());
			if (chckbxVariPhos.isSelected()) {
				if (comboBoxMaxVariMod.getSelectedIndex() == 0) {
					comboBoxMaxVariMod.setSelectedIndex(1);
					par.setMaxMod(1);
				}
			}
		});

		JLabel lblVariGG = new JLabel("Variable Mod. Ubiquitinylation/GlyGly (K)");
		add(lblVariGG, "cell 0 8,alignx center");

		JCheckBox chckbxVariUbi = new JCheckBox("Yes");
		add(chckbxVariUbi, "cell 1 8");
		chckbxVariUbi.setSelected(par.isK_GG());
		chckbxVariUbi.addActionListener(l -> {
			par.setK_GG(chckbxVariUbi.isSelected());
			if (chckbxVariUbi.isSelected()) {
				if (comboBoxMaxVariMod.getSelectedIndex() == 0) {
					comboBoxMaxVariMod.setSelectedIndex(1);
					par.setMaxMod(1);
				}
			}
		});

		JLabel lblPepLen = new JLabel("Peptide length range");
		add(lblPepLen, "cell 0 9,alignx center");

		JComboBox<Integer> comboBoxPepLen1 = new JComboBox<Integer>();
		add(comboBoxPepLen1, "cell 1 9,grow");
		for (int i = 5; i <= 100; i++) {
			comboBoxPepLen1.addItem(i);
		}
		comboBoxPepLen1.setSelectedItem(par.getMin_pep_len());

		JComboBox<Integer> comboBoxPepLen2 = new JComboBox<Integer>();
		add(comboBoxPepLen2, "cell 2 9,grow");
		for (int i = 5; i <= 100; i++) {
			comboBoxPepLen2.addItem(i);
		}
		comboBoxPepLen2.setSelectedItem(par.getMax_pep_len());

		comboBoxPepLen1.addActionListener(l -> {
			Integer len1 = comboBoxPepLen1.getItemAt(comboBoxPepLen1.getSelectedIndex());
			Integer len2 = comboBoxPepLen2.getItemAt(comboBoxPepLen2.getSelectedIndex());
			par.setMin_pep_len(len1);
			if (len1 > len2) {
				comboBoxPepLen2.setSelectedItem(len1);
				par.setMax_pep_len(len1);
			}
		});

		comboBoxPepLen2.addActionListener(l -> {
			Integer len1 = comboBoxPepLen1.getItemAt(comboBoxPepLen1.getSelectedIndex());
			Integer len2 = comboBoxPepLen2.getItemAt(comboBoxPepLen2.getSelectedIndex());
			par.setMax_pep_len(len2);
			if (len2 < len1) {
				comboBoxPepLen1.setSelectedItem(len2);
				par.setMin_pep_len(len2);
			}
		});

		JLabel lblPreChar = new JLabel("Precursor charge range");
		add(lblPreChar, "cell 0 10,alignx center");

		JComboBox<Integer> comboBoxPreChar1 = new JComboBox<Integer>();
		add(comboBoxPreChar1, "cell 1 10,grow");
		for (int i = 1; i <= 10; i++) {
			comboBoxPreChar1.addItem(i);
		}
		comboBoxPreChar1.setSelectedItem(par.getMin_pr_charge());

		JComboBox<Integer> comboBoxPreChar2 = new JComboBox<Integer>();
		add(comboBoxPreChar2, "cell 2 10,grow");
		for (int i = 1; i <= 10; i++) {
			comboBoxPreChar2.addItem(i);
		}
		comboBoxPreChar2.setSelectedItem(par.getMax_pr_charge());

		comboBoxPreChar1.addActionListener(l -> {
			Integer char1 = comboBoxPreChar1.getItemAt(comboBoxPreChar1.getSelectedIndex());
			Integer char2 = comboBoxPreChar2.getItemAt(comboBoxPreChar2.getSelectedIndex());
			if (char1 > char2) {
				comboBoxPreChar2.setSelectedItem(char1);
				par.setMax_pr_charge(char1);
			}
			par.setMin_pr_charge(char1);
		});

		comboBoxPreChar2.addActionListener(l -> {
			Integer char1 = comboBoxPreChar1.getItemAt(comboBoxPreChar1.getSelectedIndex());
			Integer char2 = comboBoxPreChar2.getItemAt(comboBoxPreChar2.getSelectedIndex());
			if (char2 < char1) {
				comboBoxPreChar1.setSelectedItem(char2);
				par.setMin_pr_charge(char2);
			}
			par.setMax_pr_charge(char2);
		});

		JLabel lblPreMzRange = new JLabel("Precursor m/z range");
		add(lblPreMzRange, "cell 0 11,alignx center");

		JComboBox<Integer> comboBoxPreMz1 = new JComboBox<Integer>();
		add(comboBoxPreMz1, "cell 1 11,grow");
		for (int i = 100; i <= 2000; i++) {
			comboBoxPreMz1.addItem(i);
		}
		comboBoxPreMz1.setSelectedItem(par.getMin_pr_mz());

		JComboBox<Integer> comboBoxPreMz2 = new JComboBox<Integer>();
		add(comboBoxPreMz2, "cell 2 11,grow");
		for (int i = 100; i <= 2000; i++) {
			comboBoxPreMz2.addItem(i);
		}
		comboBoxPreMz2.setSelectedItem(par.getMax_pr_mz());

		comboBoxPreMz1.addActionListener(l -> {
			Integer mz1 = comboBoxPreMz1.getItemAt(comboBoxPreMz1.getSelectedIndex());
			Integer mz2 = comboBoxPreMz2.getItemAt(comboBoxPreMz2.getSelectedIndex());
			if (mz1 > mz2) {
				comboBoxPreMz2.setSelectedItem(mz1);
				par.setMax_pr_mz(mz1);
			}
			par.setMin_pr_mz(mz1);
		});

		comboBoxPreMz2.addActionListener(l -> {
			Integer mz1 = comboBoxPreMz1.getItemAt(comboBoxPreMz1.getSelectedIndex());
			Integer mz2 = comboBoxPreMz2.getItemAt(comboBoxPreMz2.getSelectedIndex());
			if (mz2 < mz1) {
				comboBoxPreMz1.setSelectedItem(mz2);
				par.setMin_pr_mz(mz2);
			}
			par.setMax_pr_mz(mz2);
		});

		JLabel lblFragMzRange = new JLabel("Fragment ion m/z Range");
		add(lblFragMzRange, "cell 0 12,alignx center");

		JComboBox<Integer> comboBoxFragMz1 = new JComboBox<Integer>();
		add(comboBoxFragMz1, "cell 1 12,grow");
		for (int i = 1; i <= 2000; i++) {
			comboBoxFragMz1.addItem(i);
		}
		comboBoxFragMz1.setSelectedItem(par.getMin_fr_mz());

		JComboBox<Integer> comboBoxFragMz2 = new JComboBox<Integer>();
		add(comboBoxFragMz2, "cell 2 12,grow");
		for (int i = 1; i <= 2000; i++) {
			comboBoxFragMz2.addItem(i);
		}
		comboBoxFragMz2.setSelectedItem(par.getMax_fr_mz());

		comboBoxFragMz1.addActionListener(l -> {
			Integer mz1 = comboBoxFragMz1.getItemAt(comboBoxFragMz1.getSelectedIndex());
			Integer mz2 = comboBoxFragMz2.getItemAt(comboBoxFragMz2.getSelectedIndex());
			if (mz1 > mz2) {
				comboBoxFragMz2.setSelectedItem(mz1);
				par.setMax_fr_mz(mz1);
			}
			par.setMin_fr_mz(mz1);
		});

		comboBoxFragMz2.addActionListener(l -> {
			Integer mz1 = comboBoxFragMz1.getItemAt(comboBoxFragMz1.getSelectedIndex());
			Integer mz2 = comboBoxFragMz2.getItemAt(comboBoxFragMz2.getSelectedIndex());
			if (mz2 < mz1) {
				comboBoxFragMz1.setSelectedItem(mz2);
				par.setMin_fr_mz(mz2);
			}
			par.setMax_fr_mz(mz2);
		});

		JLabel lblPreFdr = new JLabel("Precursor FDR (%)");
		add(lblPreFdr, "cell 0 13,alignx center");

		JComboBox<Integer> comboBoxPreFdr = new JComboBox<Integer>();
		add(comboBoxPreFdr, "cell 1 13,grow");

		int fdrSelectId = 0;
		for (int i = 1; i < 100; i++) {
			comboBoxPreFdr.addItem(i);
			if ((double) i <= par.getQvalue() * 100.0) {
				fdrSelectId = i;
			}
		}
		comboBoxPreFdr.setSelectedIndex(fdrSelectId);
		comboBoxPreFdr.addActionListener(l -> {
			double qvalue = (double) comboBoxPreFdr.getItemAt(comboBoxPreFdr.getSelectedIndex()) / 100.0;
			par.setQvalue(qvalue);
		});

		JLabel lblPepDetect = new JLabel("Peptide detectability threshold");
		add(lblPepDetect, "cell 0 14,alignx center");

		DecimalFormat df = new DecimalFormat("0.0");
		JComboBox<String> comboBoxPepDetect = new JComboBox<String>();
		add(comboBoxPepDetect, "cell 1 14,grow");
		int pepDetectId = 0;
		for (int i = 1; i < 10; i++) {
			comboBoxPepDetect.addItem(df.format((double) i * 0.1));
			if ((double) i <= diaParGenLib.getPepDetectThreshold() * 10.0 - 1.0) {
				pepDetectId = i;
			}
		}
		comboBoxPepDetect.setSelectedIndex(pepDetectId);
		comboBoxPepDetect.addActionListener(l -> {
			double threshold = Double.parseDouble(comboBoxPepDetect.getItemAt(comboBoxPepDetect.getSelectedIndex()));
			this.diaParGenLib.setPepDetectThreshold(threshold);
		});

		int totalCount = Runtime.getRuntime().availableProcessors();

		JLabel lblNumberOfThreads = new JLabel("Number of threads");
		lblNumberOfThreads.setBackground(new Color(190, 109, 183));
		lblNumberOfThreads.setOpaque(true);
		add(lblNumberOfThreads, "cell 0 15,alignx center");

		JComboBox<Integer> threadComboBox = new JComboBox<Integer>();
		add(threadComboBox, "cell 1 15,grow");
		for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++) {
			threadComboBox.addItem(i);
		}

		int threadCount = par.getThreads();
		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}
		threadComboBox.addActionListener(l -> {
			int thread = threadComboBox.getItemAt(threadComboBox.getSelectedIndex());
			diaParGenLib.setThread(thread);
		});

		JButton btnDefault = new JButton("Restore default settings");
		add(btnDefault, "cell 1 17,grow");
		btnDefault.setOpaque(true);
		btnDefault.setBackground(new Color(253, 211, 106));
		btnDefault.addActionListener(l -> {
			comboBoxEnzyme.setSelectedItem("Trypsin/P");
			par.setEnzyme("Trypsin/P");
			comboBoxMaxMiss.setSelectedItem(1);
			par.setMissed_cleavages(1);
			chckbxFixCarbami.setSelected(true);
			par.setCarbami_C(true);
			chckbxMetExci.setSelected(true);
			par.setM_excision(true);
			comboBoxMaxVariMod.setSelectedItem(1);
			par.setMaxMod(1);
			chckbxVariOxi.setSelected(false);
			par.setOxidation_M(false);
			chckbxVariAce.setSelected(false);
			par.setAcety_proN(false);
			chckbxVariPhos.setSelected(false);
			par.setPhospho_STY(false);
			chckbxVariUbi.setSelected(false);
			par.setK_GG(false);
			comboBoxPepLen1.setSelectedItem(7);
			par.setMin_pep_len(7);
			comboBoxPepLen2.setSelectedItem(30);
			par.setMax_pep_len(30);
			comboBoxPreChar1.setSelectedItem(2);
			par.setMin_pr_charge(2);
			comboBoxPreChar2.setSelectedItem(3);
			par.setMax_pr_charge(3);
			comboBoxPreMz1.setSelectedItem(350);
			par.setMin_pr_mz(350);
			comboBoxPreMz2.setSelectedItem(1000);
			par.setMax_pr_mz(1000);
			comboBoxFragMz1.setSelectedItem(200);
			par.setMin_fr_mz(200);
			comboBoxFragMz2.setSelectedItem(1800);
			par.setMax_fr_mz(1800);
			comboBoxPreFdr.setSelectedItem(1);
			par.setQvalue(0.01);
			comboBoxPepDetect.setSelectedIndex(0);
			this.diaParGenLib.setPepDetectThreshold(0.1);
			par.setThreads(1);
			this.diaParGenLib.setThread(1);
			threadComboBox.setSelectedIndex(0);
		});
	}

	public MetaDiaParGenLib getParameter() {
		return this.diaParGenLib;
	}

	public void setParameter(MetaDiaParGenLib diaParGenLib) {
		this.diaParGenLib = diaParGenLib;

		DiannParameter par = diaParGenLib.getDiannPar();
		comboBoxEnzyme.setSelectedItem("Trypsin/P");
		comboBoxMaxMiss.setSelectedItem(1);
		chckbxFixCarbami.setSelected(par.isCarbami_C());
		chckbxMetExci.setSelected(par.isM_excision());
		comboBoxMaxVariMod.setSelectedItem(1);
		chckbxVariOxi.setSelected(par.isOxidation_M());
		chckbxVariAce.setSelected(par.isAcety_proN());
		chckbxVariPhos.setSelected(par.isPhospho_STY());
		chckbxVariUbi.setSelected(par.isK_GG());
		comboBoxPepLen1.setSelectedItem(par.getMin_pep_len());
		comboBoxPepLen2.setSelectedItem(par.getMax_pep_len());
		comboBoxPreChar1.setSelectedItem(par.getMin_pr_charge());
		comboBoxPreChar2.setSelectedItem(par.getMax_pr_charge());
		comboBoxPreMz1.setSelectedItem(par.getMin_pr_mz());
		comboBoxPreMz2.setSelectedItem(par.getMax_pr_mz());
		comboBoxFragMz1.setSelectedItem(par.getMin_fr_mz());
		comboBoxFragMz2.setSelectedItem(par.getMax_fr_mz());

		int fdrSelectId = 0;
		for (int i = 1; i < 100; i++) {
			if ((double) i <= par.getQvalue() * 100.0) {
				fdrSelectId = i;
			}
		}
		comboBoxPreFdr.setSelectedIndex(fdrSelectId);

		int pepDetectId = 0;
		for (int i = 1; i < 10; i++) {
			if ((double) i <= diaParGenLib.getPepDetectThreshold() * 10.0 - 1.0) {
				pepDetectId = i;
			}
		}
		comboBoxPepDetect.setSelectedIndex(pepDetectId);

		int totalCount = Runtime.getRuntime().availableProcessors();
		int threadCount = diaParGenLib.getThread();
		if (threadCount > 0 && threadCount <= totalCount) {
			threadComboBox.setSelectedIndex(threadCount - 1);
		}
	}

}
