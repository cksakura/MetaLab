/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzyme;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzymeHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModHandler;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantParameter;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

import javax.swing.event.ListSelectionEvent;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;

import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

/**
 * @author Kai Cheng
 *
 */
public class ParameterPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7033562003758597160L;

	private JList<String> varList0;
	private JList<MaxquantModification> varList1;
	private JList<String> fixList0;
	private JList<MaxquantModification> fixList1;
	private JList<String> enzymeList0;
	private JList<MaxquantEnzyme> enzymeList1;
	private JList<MaxquantModification> labelList0;
	private JList<String> labelList1;
	private JList<String> labelList2;
	private JList<String> labelList3;

	private JTable isobaricTable1;
	private DefaultTableModel isobaricModel1;

	private JTable isobaricTable2;
	private DefaultTableModel isobaricModel2;
	private int totalTagCount;

	private HashSet<String> vmUsed;
	private HashSet<String> fmUsed;
	private HashSet<String> enUsed;
	private HashSet<String>[] labelSets;
	private JTextField textField;

	private JTextArea textArea;

	private JCheckBox chckbxCombine;

	private Color left = new Color(255, 204, 204);
	private Color middle = new Color(204, 255, 204);
	private Color right = new Color(204, 255, 255);

	private boolean quanPanelUpdate;
	private boolean enzymePanelUpdate;

	private JScrollPane quanScrollPane;
	private JScrollPane labelScrollPane1;
	private JScrollPane labelScrollPane2;
	private JScrollPane labelScrollPane3;
	private JScrollPane isobaricScrollPane_1;
	private JScrollPane isobaricScrollPane_2;

	private JButton label1InButton;
	private JButton label2InButton;
	private JButton label3InButton;
	private JButton label1OutButton;
	private JButton label2OutButton;
	private JButton label3OutButton;

	private JRadioButton rdbtnFull;
	private JRadioButton rdbtnSemispecific;
	private JRadioButton rdbtnUnspecific;
	private JRadioButton rdbtnSemiNTerm;
	private JRadioButton rdbtnSemiCTerm;
	private JRadioButton rdbtnNone;

	private JRadioButton rdbtnLabelFree;
	private JRadioButton rdbtnIsotopeLabeling;
	private JRadioButton rdbtnIsobaricLabeling;

	private JLabel lblIsobaricLabels;
	private JLabel lblIsotopeLabels;
	private JLabel lblLightLabels;
	private JLabel lblMediemLabels;
	private JLabel lblHeavyLabels;

	private JScrollPane enzymeScrollPane0;
	private JScrollPane enzymeScrollPane1;

	private JButton enInButton;

	private JButton enOutButton;

	/**
	 * Create the panel.
	 * 
	 * @throws DocumentException
	 */
	@SuppressWarnings("rawtypes")
	public ParameterPanel() {
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));

		JLabel valLabel = new JLabel("Variable Modifications");
		valLabel.setBackground(new Color(255, 153, 153));

		JScrollPane valScrollPane0 = new JScrollPane();

		JScrollPane valScrollPane1 = new JScrollPane();

		JScrollPane fixScrollPane0 = new JScrollPane();

		JScrollPane fixScrollPane1 = new JScrollPane();

		JLabel fixLabel = new JLabel("Fixed Modifications");

		JLabel lblEnzyme = new JLabel("Enzyme");

		JScrollPane enzymeScrollPane0 = new JScrollPane();

		JScrollPane enzymeScrollPane1 = new JScrollPane();

		JList<MaxquantEnzyme> enzymeList1 = new JList<MaxquantEnzyme>();
		enzymeScrollPane1.setViewportView(enzymeList1);
		enzymeList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

			}
		});

		this.enzymeList0 = new JList<String>();
		enzymeScrollPane0.setViewportView(enzymeList0);

		JList<String> fixList1 = new JList<String>();
		fixScrollPane1.setViewportView(fixList1);

		JList<String> fixList0 = new JList<String>();
		fixScrollPane0.setViewportView(fixList0);

		JList<String> valList1 = new JList<String>();
		valScrollPane1.setColumnHeaderView(valList1);

		JList<String> valList0 = new JList<String>();
		valList0.setBackground(new Color(204, 255, 204));
		valList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			}
		});

		valScrollPane0.setViewportView(valList0);

		JButton vmInButton = new JButton(">");
		vmInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		setLayout(new MigLayout("",
				"[180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240]",
				"[20px][100px,grow 250][20px][100px,grow 250][20px][100px,grow 250][50,grow][135px:180px]"));

		JLabel lblIsotopeLabels = new JLabel("Isotope labels");
		add(lblIsotopeLabels, "cell 4 0,alignx left,aligny top");

		JLabel lblLightLabels = new JLabel("Light labels");
		add(lblLightLabels, "cell 6 0,alignx left,aligny top");

		JLabel lblIsobaricLabels = new JLabel("Isobaric labels");
		add(lblIsobaricLabels, "cell 8 0");
		add(vmInButton, "flowy,cell 1 1,alignx center,aligny center");

		JScrollPane quanScrollPane = new JScrollPane();
		add(quanScrollPane, "cell 4 1 1 5,grow");

		JList quanList = new JList();
		quanScrollPane.setViewportView(quanList);

		JButton label1InButton = new JButton(">");
		add(label1InButton, "flowy,cell 5 1,alignx center,aligny center");

		JScrollPane labelScrollPane1 = new JScrollPane();
		add(labelScrollPane1, "cell 6 1,grow");

		JList labelList1 = new JList();
		labelScrollPane1.setViewportView(labelList1);

		JScrollPane isobaricScrollPane_1 = new JScrollPane();
		add(isobaricScrollPane_1, "cell 8 1 1 5,grow");
		JList isobaricList1 = new JList();
		isobaricScrollPane_1.setViewportView(isobaricList1);

		JScrollPane isobaricScrollPane_2 = new JScrollPane();
		add(isobaricScrollPane_2, "cell 10 1 1 5,grow");
		JList isobaricList2 = new JList();
		isobaricScrollPane_2.setViewportView(isobaricList2);

		add(fixLabel, "cell 0 2,alignx left,aligny top");
		add(valLabel, "cell 0 0,alignx left,aligny bottom");

		JLabel lblMediumLabels = new JLabel("Medium labels");
		add(lblMediumLabels, "cell 6 2,alignx left,aligny top");

		JButton fmInButton = new JButton(">");
		fmInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		add(fmInButton, "flowy,cell 1 3,alignx center,aligny center");

		JButton label2InButton = new JButton(">");
		add(label2InButton, "flowy,cell 5 3,alignx center,aligny center");

		JScrollPane labelScrollPane2 = new JScrollPane();
		add(labelScrollPane2, "cell 6 3,grow");

		JList labelList2 = new JList();
		labelScrollPane2.setViewportView(labelList2);

		JLabel lblHeavyLabels = new JLabel("Heavy labels");
		add(lblHeavyLabels, "cell 6 4,alignx left,aligny top");
		add(enzymeScrollPane0, "cell 0 5,grow");
		add(fixScrollPane0, "cell 0 3,grow");
		add(valScrollPane0, "cell 0 1,grow");
		add(valScrollPane1, "cell 2 1,grow");
		add(fixScrollPane1, "cell 2 3,grow");

		JButton enInButton = new JButton(">");
		add(enInButton, "flowy,cell 1 5,alignx center,aligny center");
		add(enzymeScrollPane1, "cell 2 5,grow");
		add(lblEnzyme, "cell 0 4,alignx left,aligny top");

		JButton vmOutButton = new JButton("<");
		add(vmOutButton, "cell 1 1,alignx center,aligny center");

		JButton fmOutButton = new JButton("<");
		add(fmOutButton, "cell 1 3,alignx center,aligny center");

		JButton enOutButton = new JButton("<");
		add(enOutButton, "cell 1 5,alignx center,aligny center");

		JButton label3InButton = new JButton(">");
		add(label3InButton, "flowy,cell 5 5,alignx center,aligny center");

		JScrollPane labelScrollPane3 = new JScrollPane();
		add(labelScrollPane3, "cell 6 5,grow");

		JList labelList3 = new JList();
		labelScrollPane3.setViewportView(labelList3);

		JPanel digestPanel = new JPanel();
		digestPanel.setBorder(new TitledBorder(null, "Digestion mode", TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		add(digestPanel, "cell 0 6 5 1,grow");
		digestPanel.setLayout(new MigLayout("", "[][][]", "[][]"));

		JRadioButton rdbtnFull = new JRadioButton("Specific");
		rdbtnFull.setSelected(true);
		digestPanel.add(rdbtnFull, "cell 0 0");

		JRadioButton rdbtnSemispecific = new JRadioButton("Semi-specific");
		digestPanel.add(rdbtnSemispecific, "cell 1 0");

		JRadioButton rdbtnUnspecific = new JRadioButton("Unspecific");
		digestPanel.add(rdbtnUnspecific, "cell 2 0");

		JPanel quanMethodPanel = new JPanel();
		quanMethodPanel.setBorder(
				new TitledBorder(null, "Quantitative mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(quanMethodPanel, "cell 6 6 5 1,grow");
		quanMethodPanel.setLayout(new MigLayout("", "[50][50][50]", "[]"));

		JRadioButton rdbtnLabelFree = new JRadioButton("Label free");
		rdbtnLabelFree.setSelected(true);
		quanMethodPanel.add(rdbtnLabelFree, "cell 0 0");

		JRadioButton rdbtnIsotopeLabeling = new JRadioButton("Isotopic labeling");
		quanMethodPanel.add(rdbtnIsotopeLabeling, "cell 1 0");

		JRadioButton rdbtnIsobaricLabeling = new JRadioButton("Isobaric labeling");
		quanMethodPanel.add(rdbtnIsobaricLabeling, "cell 2 0");

		JPanel dbPanel = new JPanel();
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(dbPanel, "cell 0 7 11 1,growx 1920,growy");

		textField = new JTextField((String) null);
		textField.setColumns(10);

		JButton btnMicroDb = new JButton("Browse");
		btnMicroDb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		dbPanel.setLayout(new MigLayout("", "[120][20][660][20][50]", "[30][30][50,grow]"));

		JLabel lblDatabase = new JLabel("Microbiome database");
		dbPanel.add(lblDatabase, "cell 0 0,grow");
		dbPanel.add(textField, "cell 2 0,grow");
		dbPanel.add(btnMicroDb, "cell 4 0,alignx right,growy");

		JCheckBox chckbxCombine = new JCheckBox("Append host database to the generated sample-specific database");
		dbPanel.add(chckbxCombine, "cell 0 1 3 1");

		JLabel lblHostDatabase = new JLabel("Host database");
		dbPanel.add(lblHostDatabase, "cell 0 2");

		JScrollPane hostDbScrollPane = new JScrollPane();
		dbPanel.add(hostDbScrollPane, "flowx,cell 2 2,grow");

		JTextArea textArea = new JTextArea();
		hostDbScrollPane.setViewportView(textArea);

		JButton btnHostDB = new JButton("Browse");
		dbPanel.add(btnHostDB, "cell 4 2");

		JButton label1OutButton = new JButton("<");
		add(label1OutButton, "cell 5 1,alignx center,aligny center");

		JButton label2OutButton = new JButton("<");
		add(label2OutButton, "cell 5 3,alignx center,aligny center");

		JButton label3OutButton = new JButton("<");
		add(label3OutButton, "cell 5 5,alignx center,aligny center");

	}

	/**
	 * Create the panel.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ParameterPanel(MetaParameterV1 par) {

		this.vmUsed = new HashSet<String>();
		this.fmUsed = new HashSet<String>();
		this.enUsed = new HashSet<String>();
		this.labelSets = new HashSet[3];
		for (int i = 0; i < this.labelSets.length; i++) {
			labelSets[i] = new HashSet<String>();
		}

		for (String vm : par.getVariMods()) {
			this.vmUsed.add(vm);
		}

		for (String fm : par.getFixMods()) {
			this.fmUsed.add(fm);
		}

		for (String en : par.getEnzymes()) {
			this.enUsed.add(en);
		}

		String[][] labels = par.getLabels();
		for (int i = 0; i < labels.length; i++) {
			for (String l : labels[i]) {
				this.labelSets[i].add(l);
			}
		}

		setBorder(new TitledBorder(null, "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("",
				"[180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][180px,grow 240][20,grow 40][300px,grow 480]",
				"[20px][150px,grow 250][20px][150px,grow 250][20px][150px,grow 250][50,grow][135px:180px]"));

		JLabel valLabel = new JLabel("Variable Modifications");
		add(valLabel, "cell 0 0,alignx left,aligny bottom");

		JLabel fixLabel = new JLabel("Fixed Modifications");
		add(fixLabel, "cell 0 2,alignx left,aligny bottom");

		JLabel lblEnzyme = new JLabel("Enzyme");
		add(lblEnzyme, "cell 0 4,alignx left,aligny bottom");

		lblIsotopeLabels = new JLabel("Isotope labels");
		add(lblIsotopeLabels, "cell 4 0,alignx left,aligny bottom");

		lblLightLabels = new JLabel("Light labels");
		add(lblLightLabels, "cell 6 0,alignx left,aligny bottom");

		lblMediemLabels = new JLabel("Mediem labels");
		add(lblMediemLabels, "cell 6 2,alignx left,aligny bottom");

		lblHeavyLabels = new JLabel("Heavy labels");
		add(lblHeavyLabels, "cell 6 4,alignx left,aligny bottom");

		lblIsobaricLabels = new JLabel("Isobaric labels");
		add(lblIsobaricLabels, "cell 8 0,alignx left,aligny bottom");

		JScrollPane valScrollPane0 = new JScrollPane();
		add(valScrollPane0, "cell 0 1,grow");

		JScrollPane valScrollPane1 = new JScrollPane();
		add(valScrollPane1, "cell 2 1,grow");

		JScrollPane fixScrollPane0 = new JScrollPane();
		add(fixScrollPane0, "cell 0 3,grow");

		JScrollPane fixScrollPane1 = new JScrollPane();
		add(fixScrollPane1, "cell 2 3,grow");

		enzymeScrollPane0 = new JScrollPane();
		add(enzymeScrollPane0, "cell 0 5,grow");

		enzymeScrollPane1 = new JScrollPane();
		add(enzymeScrollPane1, "cell 2 5,grow");

		quanScrollPane = new JScrollPane();
		add(quanScrollPane, "cell 4 1 1 5,grow");

		labelScrollPane1 = new JScrollPane();
		add(labelScrollPane1, "cell 6 1,grow");

		labelScrollPane2 = new JScrollPane();
		add(labelScrollPane2, "cell 6 3,grow");

		labelScrollPane3 = new JScrollPane();
		add(labelScrollPane3, "cell 6 5,grow");

		isobaricScrollPane_1 = new JScrollPane();
		add(isobaricScrollPane_1, "cell 8 1 1 5,grow");

		isobaricScrollPane_2 = new JScrollPane();
		add(isobaricScrollPane_2, "cell 10 1 1 5,grow");

		JButton vmInButton = new JButton(">");
		add(vmInButton, "flowy,cell 1 1,alignx center,aligny center");

		JButton vmOutButton = new JButton("<");
		add(vmOutButton, "cell 1 1,alignx center,aligny center");

		JButton fmInButton = new JButton(">");
		add(fmInButton, "flowy,cell 1 3,alignx center,aligny center");

		JButton fmOutButton = new JButton("<");
		add(fmOutButton, "cell 1 3,alignx center,aligny center");

		enInButton = new JButton(">");
		add(enInButton, "flowy,cell 1 5,alignx center,aligny center");

		enOutButton = new JButton("<");
		add(enOutButton, "cell 1 5,alignx center,aligny center");

		label1InButton = new JButton(">");
		add(label1InButton, "flowy,cell 5 1,alignx center,aligny center");

		label2InButton = new JButton(">");
		add(label2InButton, "flowy,cell 5 3,alignx center,aligny center");

		label3InButton = new JButton(">");
		add(label3InButton, "flowy,cell 5 5,alignx center,aligny center");

		label1OutButton = new JButton("<");
		add(label1OutButton, "cell 5 1,alignx center,aligny center");

		label2OutButton = new JButton("<");
		add(label2OutButton, "cell 5 3,alignx center,aligny center");

		label3OutButton = new JButton("<");
		add(label3OutButton, "cell 5 5,alignx center,aligny center");

		vmInButton.setEnabled(false);
		vmInButton.addActionListener(l -> {
			String value = ParameterPanel.this.varList0.getSelectedValue();
			ParameterPanel.this.vmUsed.remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String vm : ParameterPanel.this.vmUsed) {
				model.addElement(vm);
			}
			ParameterPanel.this.varList0.setModel(model);
			ParameterPanel.this.varList0.clearSelection();
			vmInButton.setEnabled(false);
		});

		vmOutButton.setEnabled(false);
		vmOutButton.addActionListener(l -> {
			String value = ParameterPanel.this.varList1.getSelectedValue().getTitle();
			ParameterPanel.this.vmUsed.add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String vm : ParameterPanel.this.vmUsed) {
				model.addElement(vm);
			}
			ParameterPanel.this.varList0.setModel(model);
			ParameterPanel.this.varList1.clearSelection();
			vmOutButton.setEnabled(false);
		});

		fmInButton.setEnabled(false);
		fmInButton.addActionListener(l -> {
			String value = ParameterPanel.this.fixList0.getSelectedValue();
			ParameterPanel.this.fmUsed.remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String fm : ParameterPanel.this.fmUsed) {
				model.addElement(fm);
			}
			ParameterPanel.this.fixList0.setModel(model);
			ParameterPanel.this.fixList0.clearSelection();
			fmInButton.setEnabled(false);
		});

		fmOutButton.setEnabled(false);
		fmOutButton.addActionListener(l -> {
			String value = ParameterPanel.this.fixList1.getSelectedValue().getTitle();
			ParameterPanel.this.fmUsed.add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String fm : ParameterPanel.this.fmUsed) {
				model.addElement(fm);
			}
			ParameterPanel.this.fixList0.setModel(model);
			ParameterPanel.this.fixList1.clearSelection();
			fmOutButton.setEnabled(false);
		});

		enInButton.setEnabled(false);
		enInButton.addActionListener(l -> {
			String value = ParameterPanel.this.enzymeList0.getSelectedValue();
			ParameterPanel.this.enUsed.remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.enUsed) {
				model.addElement(en);
			}
			ParameterPanel.this.enzymeList0.setModel(model);
			ParameterPanel.this.enzymeList0.clearSelection();
			enInButton.setEnabled(false);
		});

		enOutButton.setEnabled(false);
		enOutButton.addActionListener(l -> {
			String value = ParameterPanel.this.enzymeList1.getSelectedValue().getTitle();
			ParameterPanel.this.enUsed.add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.enUsed) {
				model.addElement(en);
			}
			ParameterPanel.this.enzymeList0.setModel(model);
			ParameterPanel.this.enzymeList1.clearSelection();
			enOutButton.setEnabled(false);
		});

		label1InButton.setEnabled(false);
		label1InButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList0.getSelectedValue().getTitle();
			ParameterPanel.this.labelSets[0].add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[0]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList1.setModel(model);
			ParameterPanel.this.labelList0.clearSelection();
			label1InButton.setEnabled(false);
		});

		label2InButton.setEnabled(false);
		label2InButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList0.getSelectedValue().getTitle();
			ParameterPanel.this.labelSets[1].add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[1]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList2.setModel(model);
			ParameterPanel.this.labelList0.clearSelection();
			label2InButton.setEnabled(false);
		});

		label3InButton.setEnabled(false);
		label3InButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList0.getSelectedValue().getTitle();
			ParameterPanel.this.labelSets[2].add(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[2]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList3.setModel(model);
			ParameterPanel.this.labelList0.clearSelection();
			label3InButton.setEnabled(false);
		});

		label1OutButton.setEnabled(false);
		label1OutButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList1.getSelectedValue();
			ParameterPanel.this.labelSets[0].remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[0]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList1.setModel(model);
			ParameterPanel.this.labelList1.clearSelection();
			label1OutButton.setEnabled(false);
		});

		label2OutButton.setEnabled(false);
		label2OutButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList2.getSelectedValue();
			ParameterPanel.this.labelSets[1].remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[1]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList2.setModel(model);
			ParameterPanel.this.labelList2.clearSelection();
			label2OutButton.setEnabled(false);
		});

		label3OutButton.setEnabled(false);
		label3OutButton.addActionListener(l -> {
			String value = ParameterPanel.this.labelList3.getSelectedValue();
			ParameterPanel.this.labelSets[2].remove(value);
			DefaultListModel<String> model = new DefaultListModel<String>();
			for (String en : ParameterPanel.this.labelSets[2]) {
				model.addElement(en);
			}
			ParameterPanel.this.labelList3.setModel(model);
			ParameterPanel.this.labelList3.clearSelection();
			label3OutButton.setEnabled(false);
		});

		MaxquantEnzymeHandler enzymeHandler = new MaxquantEnzymeHandler(par.getMaxQuantEnzyme());
		MaxquantEnzyme[] enzymes = enzymeHandler.getEnzymes();
		this.enzymeList1 = new JList<MaxquantEnzyme>(enzymes);
		enzymeList1.setBackground(left);
		enzymeScrollPane1.setViewportView(enzymeList1);
		enzymeList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (enzymeList1.getSelectedIndex() == -1) {
						enOutButton.setEnabled(false);
					} else {
						enOutButton.setEnabled(true);
					}
				}
			}
		});

		this.enzymeList0 = new JList<String>(par.getEnzymes());
		enzymeList0.setBackground(left);
		enzymeScrollPane0.setViewportView(enzymeList0);
		enzymeList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (enzymeList0.getSelectedIndex() == -1) {
						enInButton.setEnabled(false);
					} else {
						enInButton.setEnabled(true);
					}
				}
			}
		});

		MaxquantModHandler modHandler = new MaxquantModHandler(par.getMaxQuantMod());
		MaxquantModification[][] mods = modHandler.getModifications();
		this.fixList1 = new JList<MaxquantModification>(mods[0]);
		fixList1.setBackground(left);
		fixScrollPane1.setViewportView(fixList1);
		fixList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (fixList1.getSelectedIndex() == -1) {
						fmOutButton.setEnabled(false);
					} else {
						fmOutButton.setEnabled(true);
					}
				}
			}
		});

		this.fixList0 = new JList<String>(par.getFixMods());
		fixList0.setBackground(left);
		fixScrollPane0.setViewportView(fixList0);
		fixList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (fixList0.getSelectedIndex() == -1) {
						fmInButton.setEnabled(false);
					} else {
						fmInButton.setEnabled(true);
					}
				}
			}
		});

		this.varList1 = new JList<MaxquantModification>(mods[0]);
		varList1.setBackground(left);
		valScrollPane1.setViewportView(varList1);
		varList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (varList1.getSelectedIndex() == -1) {
						vmOutButton.setEnabled(false);
					} else {
						vmOutButton.setEnabled(true);
					}
				}
			}
		});

		this.varList0 = new JList<String>(par.getVariMods());
		varList0.setBackground(left);
		valScrollPane0.setViewportView(varList0);
		varList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (varList0.getSelectedIndex() == -1) {
						vmInButton.setEnabled(false);
					} else {
						vmInButton.setEnabled(true);
					}
				}
			}
		});

		this.labelList0 = new JList<MaxquantModification>(mods[1]);
		labelList0.setBackground(middle);
		quanScrollPane.setViewportView(labelList0);
		labelList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList0.getSelectedIndex() == -1) {
						label1InButton.setEnabled(false);
						label2InButton.setEnabled(false);
						label3InButton.setEnabled(false);
					} else {
						label1InButton.setEnabled(true);
						label2InButton.setEnabled(true);
						label3InButton.setEnabled(true);
					}
				}
			}
		});

		this.labelList1 = new JList<String>(labels[0]);
		labelList1.setBackground(middle);
		labelScrollPane1.setViewportView(labelList1);
		labelList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList1.getSelectedIndex() == -1) {
						label1OutButton.setEnabled(false);
					} else {
						label1OutButton.setEnabled(true);
					}
				}
			}
		});

		this.labelList2 = new JList<String>(labels[1]);
		labelList2.setBackground(middle);
		labelScrollPane2.setViewportView(labelList2);
		labelList2.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList2.getSelectedIndex() == -1) {
						label2OutButton.setEnabled(false);
					} else {
						label2OutButton.setEnabled(true);
					}
				}
			}
		});

		this.labelList3 = new JList<String>(labels[2]);
		labelList3.setBackground(middle);
		labelScrollPane3.setViewportView(labelList3);
		labelList3.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList3.getSelectedIndex() == -1) {
						label3OutButton.setEnabled(false);
					} else {
						label3OutButton.setEnabled(true);
					}
				}
			}
		});

		String[] isobarics = par.getIsobaric();
		double[][] isoCorFactor = par.getIsoCorFactor();
		HashMap<String, double[]> isomap = new HashMap<String, double[]>();
		for (int i = 0; i < isobarics.length; i++) {
			isomap.put(isobarics[i], isoCorFactor[i]);
		}

		this.isobaricTable1 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2993376790352324890L;

			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable1.setBackground(right);

		this.isobaricTable2 = new JTable() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3334820538694443913L;

			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		isobaricTable2.setBackground(right);

		IsobaricTag[] isobaricTages = IsobaricTag.values();
		Object[][] isobaricExps = new Object[isobaricTages.length][2];
		String[][] isobraicTagNames = new String[isobaricTages.length][];

		for (int i = 0; i < isobaricExps.length; i++) {
			isobaricExps[i][0] = false;
			isobaricExps[i][1] = isobaricTages[i].getName();

			ArrayList<String> list = new ArrayList<String>();
			for (int j = 0; j < mods[2].length; j++) {
				String title = mods[2][j].getTitle();
				if (title.startsWith(isobaricTages[i].getName())) {
					list.add(title);
					if (isomap.containsKey(title)) {
						isobaricExps[i][0] = true;
					}
				}
			}
			isobraicTagNames[i] = list.toArray(new String[list.size()]);
			totalTagCount += isobraicTagNames[i].length;
		}

		isobaricModel1 = new DefaultTableModel(isobaricExps, new Object[] { "", "Method" });
		isobaricTable1.setModel(isobaricModel1);
		isobaricTable1.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable1.getColumnModel().getColumn(1).setPreferredWidth(150);

		Object[] isobaricTable2Title = new Object[] { "", "Tags", "% of -2", "% of -1", "% of 1", "% of 2" };
		isobaricModel2 = new DefaultTableModel(new Object[][] {}, isobaricTable2Title);

		isobaricModel1.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				int row = event.getFirstRow();
				if (((boolean) isobaricModel1.getValueAt(row, 0)) == true) {
					for (int i = 0; i < isobaricExps.length; i++) {
						if (i != row) {
							isobaricModel1.setValueAt(false, i, 0);
						}
					}
					Object[][] usedIsobaricLabels = new Object[isobraicTagNames[row].length][6];
					for (int i = 0; i < usedIsobaricLabels.length; i++) {
						usedIsobaricLabels[i][0] = true;
						usedIsobaricLabels[i][1] = isobraicTagNames[row][i];
						double[] corFactor = isomap.containsKey(isobraicTagNames[row][i])
								? isomap.get(isobraicTagNames[row][i])
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							usedIsobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(usedIsobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);

				} else {
					Object[][] isobaricLabels = new Object[mods[2].length][6];
					for (int i = 0; i < isobaricLabels.length; i++) {
						isobaricLabels[i][0] = false;
						isobaricLabels[i][1] = mods[2][i].getTitle();
						double[] corFactor = isomap.containsKey(mods[2][i].getTitle())
								? isomap.get(mods[2][i].getTitle())
								: new double[] { 0.0, 0.0, 0.0, 0.0 };
						for (int j = 0; j < corFactor.length; j++) {
							isobaricLabels[i][j + 2] = corFactor[j];
						}
					}
					isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
					isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
					isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
				}
			}
		});
		isobaricScrollPane_1.setViewportView(isobaricTable1);

		Object[][] isobaricLabels = null;
		if (isomap.size() > 0) {
			isobaricLabels = new Object[isomap.size()][6];
			int id = 0;
			for (int i = 0; i < mods[2].length; i++) {
				String title = mods[2][i].getTitle();
				if (isomap.containsKey(title)) {
					isobaricLabels[id][0] = true;
					isobaricLabels[id][1] = title;

					double[] corFactor = isomap.get(title);
					for (int j = 0; j < corFactor.length; j++) {
						isobaricLabels[id][j + 2] = corFactor[j];
					}
					id++;
				}
			}
		} else {
			isobaricLabels = new Object[mods[2].length][6];
			for (int i = 0; i < mods[2].length; i++) {
				String title = mods[2][i].getTitle();
				isobaricLabels[i][0] = false;
				isobaricLabels[i][1] = title;
				for (int j = 0; j < 4; j++) {
					isobaricLabels[i][j + 2] = 0.0;
				}
			}
		}

		isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
		isobaricTable2.setModel(isobaricModel2);
		isobaricTable2.getColumnModel().getColumn(0).setMaxWidth(30);
		isobaricTable2.getColumnModel().getColumn(1).setMinWidth(150);
		isobaricModel2.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {
				// TODO Auto-generated method stub
				int row = event.getFirstRow();
				int column = event.getColumn();

				if (column > 1) {
					String obj = (String) isobaricModel2.getValueAt(row, column);
					if (NumberUtils.isNumber(obj)) {
						double value = Double.parseDouble(obj);
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(value, row, column);

						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(value, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(value, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					} else {
						isobaricModel2.removeTableModelListener(this);
						isobaricModel2.setValueAt(0.0, row, column);
						int rowCount = isobaricModel2.getRowCount();
						if (rowCount < totalTagCount) {
							if (row * 2 < rowCount) {
								isobaricModel2.setValueAt(0.0, row + rowCount / 2, column);
							} else {
								isobaricModel2.setValueAt(0.0, row - rowCount / 2, column);
							}
						}
						isobaricModel2.addTableModelListener(this);
					}
				}
			}
		});
		isobaricScrollPane_2.setViewportView(isobaricTable2);

		JPanel digestPanel = new JPanel();
		digestPanel.setBorder(new TitledBorder(null, "Digestion mode", TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		add(digestPanel, "cell 0 6 5 1,grow");
		digestPanel.setLayout(new MigLayout("", "[50][50][50][50][50][50]", "[]"));

		rdbtnFull = new JRadioButton("Specific");
		digestPanel.add(rdbtnFull, "cell 0 0");
		rdbtnFull.addActionListener(l -> {
			if (rdbtnFull.isSelected()) {
				updateEnzyme(true);
			}
		});
		rdbtnFull.setSelected(par.getDigestMode() == MaxquantEnzyme.specific);

		rdbtnSemiNTerm = new JRadioButton("Semi free N-term");
		digestPanel.add(rdbtnSemiNTerm, "cell 1 0");
		rdbtnSemiNTerm.addActionListener(l -> {
			if (rdbtnSemiNTerm.isSelected()) {
				updateEnzyme(true);
			}
		});
		rdbtnSemiNTerm.setSelected(par.getDigestMode() == MaxquantEnzyme.semi_Nterm);

		rdbtnSemiCTerm = new JRadioButton("Semi free C-term");
		digestPanel.add(rdbtnSemiCTerm, "cell 2 0");
		rdbtnSemiCTerm.addActionListener(l -> {
			if (rdbtnSemiCTerm.isSelected()) {
				updateEnzyme(true);
			}
		});
		rdbtnSemiCTerm.setSelected(par.getDigestMode() == MaxquantEnzyme.semi_Cterm);

		rdbtnSemispecific = new JRadioButton("Semi-specific");
		digestPanel.add(rdbtnSemispecific, "cell 3 0");
		rdbtnSemispecific.addActionListener(l -> {
			if (rdbtnSemispecific.isSelected()) {
				updateEnzyme(true);
			}
		});
		rdbtnSemispecific.setSelected(par.getDigestMode() == MaxquantEnzyme.semi_specific);

		rdbtnUnspecific = new JRadioButton("Unspecific");
		digestPanel.add(rdbtnUnspecific, "cell 4 0");
		rdbtnUnspecific.addActionListener(l -> {
			if (rdbtnUnspecific.isSelected()) {
				updateEnzyme(false);
			}
		});
		rdbtnUnspecific.setSelected(par.getDigestMode() == MaxquantEnzyme.unspecific);

		rdbtnNone = new JRadioButton("No digestion");
		digestPanel.add(rdbtnNone, "cell 5 0");
		rdbtnNone.addActionListener(l -> {
			if (rdbtnNone.isSelected()) {
				updateEnzyme(false);
			}
		});
		rdbtnNone.setSelected(par.getDigestMode() == MaxquantEnzyme.no_digest);

		if (par.getDigestMode() == MaxquantEnzyme.unspecific || par.getDigestMode() == MaxquantEnzyme.no_digest) {
			updateEnzyme(false);
		} else {
			updateEnzyme(true);
		}

		ButtonGroup digestGroup = new ButtonGroup();
		digestGroup.add(rdbtnFull);
		digestGroup.add(rdbtnSemiNTerm);
		digestGroup.add(rdbtnSemiCTerm);
		digestGroup.add(rdbtnSemispecific);
		digestGroup.add(rdbtnUnspecific);
		digestGroup.add(rdbtnNone);

		JPanel quanMethodPanel = new JPanel();
		quanMethodPanel.setBorder(
				new TitledBorder(null, "Quantitative mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(quanMethodPanel, "cell 6 6 5 1,grow");
		quanMethodPanel.setLayout(new MigLayout("", "[50][50][50]", "[]"));

		rdbtnLabelFree = new JRadioButton("Label free");
		quanMethodPanel.add(rdbtnLabelFree, "cell 0 0");

		rdbtnIsotopeLabeling = new JRadioButton("Isotopic labeling");
		quanMethodPanel.add(rdbtnIsotopeLabeling, "cell 1 0");

		rdbtnIsobaricLabeling = new JRadioButton("Isobaric labeling");
		quanMethodPanel.add(rdbtnIsobaricLabeling, "cell 2 0");

		ButtonGroup quanGroup = new ButtonGroup();
		quanGroup.add(rdbtnLabelFree);
		quanGroup.add(rdbtnIsotopeLabeling);
		quanGroup.add(rdbtnIsobaricLabeling);

		rdbtnLabelFree.addActionListener(l -> {
			if (rdbtnLabelFree.isSelected()) {
				updateQuanPanel(MetaConstants.labelFree);
			}
		});

		rdbtnIsotopeLabeling.addActionListener(l -> {
			if (rdbtnIsotopeLabeling.isSelected()) {
				updateQuanPanel(MetaConstants.isotopicLabel);
			}
		});

		rdbtnIsobaricLabeling.addActionListener(l -> {
			if (rdbtnIsobaricLabeling.isSelected()) {
				updateQuanPanel(MetaConstants.isobaricLabel);
			}
		});

		if (par.getQuanMode().equals(MetaConstants.labelFree)) {
			rdbtnLabelFree.setSelected(true);
			updateQuanPanel(par.getQuanMode());
		}
		if (par.getQuanMode().equals(MetaConstants.isotopicLabel)) {
			rdbtnIsotopeLabeling.setSelected(true);
			updateQuanPanel(par.getQuanMode());
		}
		if (par.getQuanMode().equals(MetaConstants.isobaricLabel)) {
			rdbtnIsobaricLabeling.setSelected(true);
			updateQuanPanel(par.getQuanMode());
		}

		JPanel dbPanel = new JPanel();
		dbPanel.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(dbPanel, "cell 0 7 11 1,grow 1920");
		dbPanel.setLayout(new MigLayout("", "[100][20,grow 10][660,grow 1700][20,grow 10][50]", "[30][30][50,grow]"));

		textField = new JTextField();
		textField.setColumns(10);
		String database = par.getDatabase();
		textField.setText(database);

		JButton btnMicroDb = new JButton("Browse");
		btnMicroDb.addActionListener(l -> {
			JFileChooser fileChooser;
			if (database != null) {
				fileChooser = new JFileChooser(new File(database));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileFilter(new FileNameExtensionFilter("Protein sequence database (.fasta)", "fasta"));

			int returnValue = fileChooser.showOpenDialog(ParameterPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File fasta = fileChooser.getSelectedFile();
				textField.setText(fasta.getAbsolutePath());
			}
		});

		JLabel lblDatabase = new JLabel("Microbiome database");
		dbPanel.add(lblDatabase, "cell 0 0,grow");
		dbPanel.add(textField, "cell 2 0,growx,aligny top");
		dbPanel.add(btnMicroDb, "cell 4 0,alignx right,growy");

		JLabel lblHostDatabase = new JLabel("Host database");
		dbPanel.add(lblHostDatabase, "cell 0 2");

		JScrollPane hostDbScrollPane = new JScrollPane();
		dbPanel.add(hostDbScrollPane, "flowx,cell 2 2,grow");

		textArea = new JTextArea();
		textArea.setEditable(false);
		String[] hostDBs = par.getHostDBs();
		StringBuilder hostsb = new StringBuilder();
		for (String s : hostDBs) {
			hostsb.append(s).append("\n");
		}
		if (hostsb.length() > 0) {
			hostsb.deleteCharAt(hostsb.length() - 1);
		}
		textArea.setText(hostsb.toString());
		hostDbScrollPane.setViewportView(textArea);

		JButton btnHostDB = new JButton("Browse");
		btnHostDB.addActionListener(l -> {

			JFileChooser fileChooser;
			if (database != null) {
				fileChooser = new JFileChooser(new File(database));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileFilter(new FileNameExtensionFilter("Protein sequence database (.fasta)", "fasta"));
			fileChooser.setMultiSelectionEnabled(true);

			int returnValue = fileChooser.showOpenDialog(ParameterPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File[] fasta = fileChooser.getSelectedFiles();
				StringBuilder sb = new StringBuilder();
				for (File f : fasta) {
					sb.append(f.getAbsolutePath()).append("\n");
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				textArea.setText(sb.toString());
			}

		});
		dbPanel.add(btnHostDB, "cell 4 2");

		boolean appendHostDb = par.isAppendHostDb();

		chckbxCombine = new JCheckBox("Append host database to the generated sample-specific database", appendHostDb);
		dbPanel.add(chckbxCombine, "cell 0 1 3 1");
		chckbxCombine.addActionListener(l -> {
			if (chckbxCombine.isSelected()) {
				lblHostDatabase.setEnabled(true);
				hostDbScrollPane.setEnabled(true);
				textArea.setEnabled(true);
				btnHostDB.setEnabled(true);
			} else {
				lblHostDatabase.setEnabled(false);
				hostDbScrollPane.setEnabled(false);
				textArea.setEnabled(false);
				btnHostDB.setEnabled(false);
			}
		});

		if (appendHostDb) {
			lblHostDatabase.setEnabled(true);
			hostDbScrollPane.setEnabled(true);
			textArea.setEnabled(true);
			btnHostDB.setEnabled(true);
		} else {
			lblHostDatabase.setEnabled(false);
			hostDbScrollPane.setEnabled(false);
			textArea.setEnabled(false);
			btnHostDB.setEnabled(false);
		}
	}

	private void updateEnzyme(boolean enable) {
		synchronized (this) {
			if (this.enzymePanelUpdate) {
				return;
			}
			enzymePanelUpdate = true;

			if (enable) {
				setEnableForComponent(enzymeScrollPane0, enable);
				setEnableForComponent(enzymeScrollPane1, enable);
				setEnableForComponent(enzymeList0, enable);
				setEnableForComponent(enzymeList1, enable);
				enInButton.setEnabled(enable);
				enOutButton.setEnabled(enable);
			} else {
				enUsed.removeAll(enUsed);
				DefaultListModel<String> model = new DefaultListModel<String>();
				enzymeList0.setModel(model);

				setEnableForComponent(enzymeScrollPane0, enable);
				setEnableForComponent(enzymeScrollPane1, enable);
				setEnableForComponent(enzymeList0, enable);
				setEnableForComponent(enzymeList1, enable);
				enInButton.setEnabled(enable);
				enOutButton.setEnabled(enable);
			}

			enzymePanelUpdate = false;
		}
	}

	private void setEnableForComponent(JComponent component, boolean enable) {
		component.setEnabled(enable);
		Component[] components = component.getComponents();
		for (Component comp : components) {
			comp.setEnabled(enable);
		}
	}

	private void updateQuanPanel(String quanMode) {
		synchronized (this) {
			if (quanPanelUpdate) {
				return;
			}
			quanPanelUpdate = true;

			if (quanMode.equals(MetaConstants.labelFree)) {
				setEnableForComponent(quanScrollPane, false);
				setEnableForComponent(labelScrollPane1, false);
				setEnableForComponent(labelScrollPane2, false);
				setEnableForComponent(labelScrollPane3, false);
				setEnableForComponent(isobaricScrollPane_1, false);
				setEnableForComponent(isobaricScrollPane_2, false);

				setEnableForComponent(labelList0, false);
				setEnableForComponent(labelList1, false);
				setEnableForComponent(labelList2, false);
				setEnableForComponent(labelList3, false);

				labelList1.setModel(new DefaultListModel<String>());
				labelList2.setModel(new DefaultListModel<String>());
				labelList3.setModel(new DefaultListModel<String>());
				for (int i = 0; i < labelSets.length; i++) {
					labelSets[i] = new HashSet<String>();
				}

				label1InButton.setEnabled(false);
				label2InButton.setEnabled(false);
				label3InButton.setEnabled(false);
				label1OutButton.setEnabled(false);
				label2OutButton.setEnabled(false);
				label3OutButton.setEnabled(false);

				lblIsotopeLabels.setEnabled(false);
				lblLightLabels.setEnabled(false);
				lblMediemLabels.setEnabled(false);
				lblHeavyLabels.setEnabled(false);
				lblIsobaricLabels.setEnabled(false);

				setEnableForComponent(isobaricTable1, false);
				setEnableForComponent(isobaricTable2, false);

				for (int i = 0; i < isobaricModel1.getRowCount(); i++) {
					isobaricModel1.setValueAt(false, i, 0);
				}

				for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
					isobaricModel2.setValueAt(false, i, 0);
				}
			} else if (quanMode.equals(MetaConstants.isotopicLabel)) {
				setEnableForComponent(quanScrollPane, true);
				setEnableForComponent(labelScrollPane1, true);
				setEnableForComponent(labelScrollPane2, true);
				setEnableForComponent(labelScrollPane3, true);
				setEnableForComponent(isobaricScrollPane_1, false);
				setEnableForComponent(isobaricScrollPane_2, false);

				setEnableForComponent(labelList0, true);
				setEnableForComponent(labelList1, true);
				setEnableForComponent(labelList2, true);
				setEnableForComponent(labelList3, true);

				label1InButton.setEnabled(true);
				label2InButton.setEnabled(true);
				label3InButton.setEnabled(true);
				label1OutButton.setEnabled(true);
				label2OutButton.setEnabled(true);
				label3OutButton.setEnabled(true);

				lblIsotopeLabels.setEnabled(true);
				lblLightLabels.setEnabled(true);
				lblMediemLabels.setEnabled(true);
				lblHeavyLabels.setEnabled(true);
				lblIsobaricLabels.setEnabled(false);

				setEnableForComponent(isobaricTable1, false);
				setEnableForComponent(isobaricTable2, false);

				for (int i = 0; i < isobaricModel1.getRowCount(); i++) {
					isobaricModel1.setValueAt(false, i, 0);
				}

				for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
					isobaricModel2.setValueAt(false, i, 0);
				}
			} else if (quanMode.equals(MetaConstants.isobaricLabel)) {
				setEnableForComponent(quanScrollPane, false);
				setEnableForComponent(labelScrollPane1, false);
				setEnableForComponent(labelScrollPane2, false);
				setEnableForComponent(labelScrollPane3, false);
				setEnableForComponent(isobaricScrollPane_1, true);
				setEnableForComponent(isobaricScrollPane_2, true);

				setEnableForComponent(labelList0, false);
				setEnableForComponent(labelList1, false);
				setEnableForComponent(labelList2, false);
				setEnableForComponent(labelList3, false);

				labelList1.setModel(new DefaultListModel<String>());
				labelList2.setModel(new DefaultListModel<String>());
				labelList3.setModel(new DefaultListModel<String>());
				for (int i = 0; i < labelSets.length; i++) {
					labelSets[i] = new HashSet<String>();
				}

				label1InButton.setEnabled(false);
				label2InButton.setEnabled(false);
				label3InButton.setEnabled(false);
				label1OutButton.setEnabled(false);
				label2OutButton.setEnabled(false);
				label3OutButton.setEnabled(false);

				lblIsotopeLabels.setEnabled(false);
				lblLightLabels.setEnabled(false);
				lblMediemLabels.setEnabled(false);
				lblHeavyLabels.setEnabled(false);
				lblIsobaricLabels.setEnabled(true);

				setEnableForComponent(isobaricTable1, true);
				setEnableForComponent(isobaricTable2, true);
			}
			quanPanelUpdate = false;
		}
	}

	public String[] getVariableMods() {
		String[] mods = this.vmUsed.toArray(new String[this.vmUsed.size()]);
		return mods;
	}

	public String[] getFixedMods() {
		String[] mods = this.fmUsed.toArray(new String[this.fmUsed.size()]);
		return mods;
	}

	public String[] getEnzymes() {
		String[] enzymes = this.enUsed.toArray(new String[this.enUsed.size()]);
		return enzymes;
	}

	public int getDigestMode() {
		if (rdbtnFull.isSelected()) {
			return MaxquantEnzyme.specific;
		}
		if (rdbtnSemiNTerm.isSelected()) {
			return MaxquantEnzyme.semi_Nterm;
		}
		if (rdbtnSemiCTerm.isSelected()) {
			return MaxquantEnzyme.semi_Cterm;
		}
		if (rdbtnSemispecific.isSelected()) {
			return MaxquantEnzyme.semi_specific;
		}
		if (rdbtnUnspecific.isSelected()) {
			return MaxquantEnzyme.unspecific;
		}
		if (rdbtnNone.isSelected()) {
			return MaxquantEnzyme.no_digest;
		}
		return 0;
	}

	public String[][] getLabels() {
		String[][] labels = new String[3][];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = this.labelSets[i].toArray(new String[this.labelSets[i].size()]);
		}
		return labels;
	}

	public String[] getIsobaricTags() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
			if (((boolean) isobaricModel2.getValueAt(i, 0)) == true) {
				list.add((String) isobaricModel2.getValueAt(i, 1));
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public double[][] getIsoCorFactor() {
		ArrayList<double[]> list = new ArrayList<double[]>();
		for (int i = 0; i < isobaricModel2.getRowCount(); i++) {
			if (((boolean) isobaricModel2.getValueAt(i, 0)) == true) {
				double[] factor = new double[4];
				for (int j = 0; j < factor.length; j++) {
					factor[j] = (double) isobaricModel2.getValueAt(i, j + 2);
				}
				list.add(factor);
			}
		}
		return list.toArray(new double[list.size()][]);
	}

	public String getQuanMode() {
		if (rdbtnIsotopeLabeling.isSelected()) {
			return MetaConstants.isotopicLabel;
		}
		if (rdbtnIsobaricLabeling.isSelected()) {
			return MetaConstants.isobaricLabel;
		}
		return MetaConstants.labelFree;
	}

	public String getDatabase() {
		return this.textField.getText();
	}

	public String[] getHostDBs() {
		String text = this.textArea.getText();
		String[] hostDBs = text.split("\n");
		return hostDBs;
	}

	public boolean isAppendHostDb() {
		return this.chckbxCombine.isSelected();
	}

	public void setResource(String resource) {
		File maxMod = new File(resource + "\\mq_bin\\conf\\modifications.xml");
		File maxEnzyme = new File(resource + "\\mq_bin\\conf\\enzymes.xml");

		if (maxMod.exists()) {
			MaxquantModHandler modHandler = new MaxquantModHandler(maxMod.getAbsolutePath());
			MaxquantModification[][] mods = modHandler.getModifications();

			DefaultListModel<MaxquantModification> fixModel = new DefaultListModel<MaxquantModification>();
			DefaultListModel<MaxquantModification> variModel = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : mods[0]) {
				fixModel.addElement(mod);
				variModel.addElement(mod);
			}
			fixList1.setModel(fixModel);
			varList1.setModel(variModel);

			DefaultListModel<MaxquantModification> labelModel = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : mods[1]) {
				labelModel.addElement(mod);
			}
			labelList0.setModel(labelModel);

			Object[] isobaricTable2Title = new Object[] { "", "Tags", "% of -2", "% of -1", "% of 1", "% of 2" };
			Object[][] isobaricLabels = new Object[mods[2].length][6];
			for (int i = 0; i < mods[2].length; i++) {
				String title = mods[2][i].getTitle();
				isobaricLabels[i][0] = false;
				isobaricLabels[i][1] = title;
				for (int j = 0; j < 4; j++) {
					isobaricLabels[i][j + 2] = 0.0;
				}
			}
			isobaricModel2.setDataVector(isobaricLabels, isobaricTable2Title);
		}

		if (maxEnzyme.exists()) {
			MaxquantEnzymeHandler enzymeHandler = new MaxquantEnzymeHandler(maxEnzyme.getAbsolutePath());
			MaxquantEnzyme[] enzymes = enzymeHandler.getEnzymes();
			DefaultListModel<MaxquantEnzyme> model = new DefaultListModel<MaxquantEnzyme>();
			for (MaxquantEnzyme en : enzymes) {
				model.addElement(en);
			}
			this.enzymeList1.setModel(model);
		}
	}
}
