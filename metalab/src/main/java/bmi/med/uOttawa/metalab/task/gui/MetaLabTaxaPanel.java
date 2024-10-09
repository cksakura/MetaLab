package bmi.med.uOttawa.metalab.task.gui;

import java.awt.Component;
import java.util.HashSet;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import net.miginfocom.swing.MigLayout;
import javax.swing.ButtonGroup;

public class MetaLabTaxaPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1520239989013158352L;
	private JRadioButton radioBtnBuiltin;
	private JRadioButton radioBtUnipept;
	private JCheckBox chckbxEukaryota;
	private JCheckBox chckbxBacteria;
	private JCheckBox chckbxArchaea;
	private JCheckBox chckbxViruses;
	private JCheckBox chckbxViroids;
	private JCheckBox chckbxUnclassified;
	private AbstractButton chckbxOther;
	private JComboBox<TaxonomyRanks> rankComboBox;
	private JComboBox<Integer> comboBox;
	private JCheckBox chckbxEnviomentalSamples;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the panel.
	 */
	public MetaLabTaxaPanel(MetaPep2TaxaPar par) {

		setLayout(new MigLayout("", "[20][100:160:225,grow][100:160:225,grow][100:160:225,grow][100:160:225,grow]",
				"[15][40][15][200][15][40]"));

		JPanel pep2TaxParPanel = new JPanel();
		pep2TaxParPanel.setBorder(
				new TitledBorder(null, "LCA calculation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(pep2TaxParPanel, "cell 1 3 5 1,grow");
		pep2TaxParPanel.setLayout(new MigLayout("", "[80:150:180,grow][80:150:180][80:150:180][80:150:180][80:150:180]", "[35][35][35][35]"));

		radioBtnBuiltin = new JRadioButton("<html>Built-in database</html>");
		buttonGroup.add(radioBtnBuiltin);
		add(radioBtnBuiltin, "cell 2 1");

		radioBtnBuiltin.addItemListener(l -> {
			if (radioBtnBuiltin.isSelected()) {
				pep2TaxParPanel.setEnabled(true);
				Component[] components = pep2TaxParPanel.getComponents();
				for (Component component : components) {
					component.setEnabled(true);
				}
			} else {
				pep2TaxParPanel.setEnabled(false);
				Component[] components = pep2TaxParPanel.getComponents();
				for (Component component : components) {
					component.setEnabled(false);
				}
			}
		});

		radioBtnBuiltin.setSelected(par.isBuildIn());

		JLabel lblTaxoxomyAnalysisBy = new JLabel("Taxonomy analysis by");
		add(lblTaxoxomyAnalysisBy, "cell 1 1");

		radioBtUnipept = new JRadioButton("<html>Unipept (http://unipept.ugent.be/)</html>", par.isUnipept());
		buttonGroup.add(radioBtUnipept);
		add(radioBtUnipept, "cell 3 1");

		JLabel topNodeLabel = new JLabel("<html>Taxa from which top nodes showed in result</html>");
		pep2TaxParPanel.add(topNodeLabel, "cell 0 0,alignx left,aligny center");

		HashSet<RootType> usedTypes = par.getUsedRootTypes();

		chckbxEukaryota = new JCheckBox("Eukaryota");
		chckbxEukaryota.setSelected(usedTypes.contains(RootType.Eukaryota));
		pep2TaxParPanel.add(chckbxEukaryota, "cell 1 0");

		chckbxBacteria = new JCheckBox("Bacteria");
		chckbxBacteria.setSelected(usedTypes.contains(RootType.Bacteria));
		pep2TaxParPanel.add(chckbxBacteria, "cell 2 0");

		chckbxArchaea = new JCheckBox("Archaea");
		chckbxArchaea.setSelected(usedTypes.contains(RootType.Archaea));
		pep2TaxParPanel.add(chckbxArchaea, "cell 3 0");

		chckbxViruses = new JCheckBox("Viruses");
		chckbxViruses.setSelected(usedTypes.contains(RootType.Viruses));
		pep2TaxParPanel.add(chckbxViruses, "cell 4 0");

		chckbxViroids = new JCheckBox("Viroids");
		chckbxViroids.setSelected(usedTypes.contains(RootType.Viroids));
		pep2TaxParPanel.add(chckbxViroids, "cell 1 1");

		chckbxUnclassified = new JCheckBox("<html>unclassified sequences</html>");
		chckbxUnclassified.setSelected(usedTypes.contains(RootType.unclassified));
		pep2TaxParPanel.add(chckbxUnclassified, "cell 2 1");

		chckbxOther = new JCheckBox("other sequences");
		chckbxOther.setSelected(usedTypes.contains(RootType.other));
		pep2TaxParPanel.add(chckbxOther, "cell 3 1");

		JLabel ignoreBlanksBelowLabel = new JLabel("<html>Ignore blanks below rank</html>");
		pep2TaxParPanel.add(ignoreBlanksBelowLabel, "cell 0 2,alignx left,aligny center");

		rankComboBox = new JComboBox<TaxonomyRanks>();
		pep2TaxParPanel.add(rankComboBox, "cell 1 2,growx");
		rankComboBox.addItem(TaxonomyRanks.Superkingdom);
		rankComboBox.addItem(TaxonomyRanks.Phylum);
		rankComboBox.addItem(TaxonomyRanks.Class);
		rankComboBox.addItem(TaxonomyRanks.Order);
		rankComboBox.addItem(TaxonomyRanks.Family);
		rankComboBox.addItem(TaxonomyRanks.Genus);
		rankComboBox.addItem(TaxonomyRanks.Species);

		rankComboBox.setSelectedItem(par.getIgnoreBlankRank());

		JLabel ignoreTaxaBelowLabel = new JLabel("<html>Ignore taxa below parents</html>");
		pep2TaxParPanel.add(ignoreTaxaBelowLabel, "cell 0 3,alignx left,aligny center");

		chckbxEnviomentalSamples = new JCheckBox("<html>environmental samples</html>");
		chckbxEnviomentalSamples.setSelected(par.getExcludeTaxa().contains("environmental samples"));
		pep2TaxParPanel.add(chckbxEnviomentalSamples, "cell 1 3");

		JButton btnDefault = new JButton("Restore defaults");
		btnDefault.addActionListener(l -> {
			rankComboBox.setSelectedItem(TaxonomyRanks.Family);
			chckbxEukaryota.setSelected(true);
			chckbxBacteria.setSelected(true);
			chckbxArchaea.setSelected(true);
			chckbxViruses.setSelected(true);
			chckbxViroids.setSelected(false);
			chckbxUnclassified.setSelected(false);
			chckbxOther.setSelected(false);
			chckbxEnviomentalSamples.setSelected(true);
		});
		pep2TaxParPanel.add(btnDefault, "cell 3 3");

		JLabel lblUniquePeptideCount = new JLabel("Unique peptide count>=");
		add(lblUniquePeptideCount, "cell 1 5");

		comboBox = new JComboBox<Integer>();
		add(comboBox, "cell 2 5,growx");
		for (int i = 1; i < 6; i++) {
			comboBox.addItem(i);
		}
		int leastPepCount = par.getLeastPepCount();
		if (leastPepCount > 0 && leastPepCount <= 5) {
			comboBox.setSelectedIndex(leastPepCount - 1);
		}

		if (radioBtnBuiltin.isSelected()) {
			pep2TaxParPanel.setEnabled(true);
			Component[] components = pep2TaxParPanel.getComponents();
			for (Component component : components) {
				component.setEnabled(true);
			}
		} else {
			pep2TaxParPanel.setEnabled(false);
			Component[] components = pep2TaxParPanel.getComponents();
			for (Component component : components) {
				component.setEnabled(false);
			}
		}
	}

	public MetaPep2TaxaPar getPep2TaxaPar() {

		boolean builtIn = radioBtnBuiltin.isSelected();
		boolean unipept = radioBtUnipept.isSelected();

		HashSet<RootType> rootSet = new HashSet<RootType>();
		if (chckbxEukaryota.isSelected()) {
			rootSet.add(RootType.Eukaryota);
		}
		if (chckbxBacteria.isSelected()) {
			rootSet.add(RootType.Bacteria);
		}
		if (chckbxArchaea.isSelected()) {
			rootSet.add(RootType.Archaea);
		}
		if (chckbxViruses.isSelected()) {
			rootSet.add(RootType.Viruses);
		}
		if (chckbxViroids.isSelected()) {
			rootSet.add(RootType.Viroids);
		}
		if (chckbxUnclassified.isSelected()) {
			rootSet.add(RootType.unclassified);
		}
		if (chckbxOther.isSelected()) {
			rootSet.add(RootType.other);
		}

		TaxonomyRanks ignoreRank = rankComboBox.getItemAt(rankComboBox.getSelectedIndex());
		HashSet<String> ignoreTaxaSet = new HashSet<String>();
		if (chckbxEnviomentalSamples.isSelected()) {
			ignoreTaxaSet.add("environmental samples");
		}

		int leastPepCount = comboBox.getItemAt(comboBox.getSelectedIndex());

		MetaPep2TaxaPar par = new MetaPep2TaxaPar(builtIn, unipept, rootSet, ignoreRank, ignoreTaxaSet, leastPepCount);

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
