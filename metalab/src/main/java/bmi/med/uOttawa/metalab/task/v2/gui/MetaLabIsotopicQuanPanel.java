/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.v2.par.MaxQuantModIO;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import net.miginfocom.swing.MigLayout;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabIsotopicQuanPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4092378436214959548L;

	private HashSet<MaxquantModification>[] labelSets;
	private JList<MaxquantModification> labelList0;
	private JList<MaxquantModification> labelList1;
	private JList<MaxquantModification> labelList2;
	private JList<MaxquantModification> labelList3;

	private JScrollPane labelScrollPane1;
	private JScrollPane labelScrollPane2;
	private JScrollPane labelScrollPane3;

	/**
	 * Create the panel.
	 */
	@SuppressWarnings("unchecked")
	public MetaLabIsotopicQuanPanel(MetaParameterMQ par) {

		setLayout(new MigLayout("", "[350][50][350]", "[][100,grow 250][][100,grow 250][][100,grow 250]"));

		this.labelSets = new HashSet[3];
		for (int i = 0; i < this.labelSets.length; i++) {
			labelSets[i] = new HashSet<MaxquantModification>();
		}

		JLabel lblIsotopeLabels = new JLabel("Isotope labels");
		add(lblIsotopeLabels, "cell 0 0,alignx left,aligny bottom");

		JLabel lblLightLabels = new JLabel("Light labels");
		add(lblLightLabels, "cell 2 0,alignx left,aligny bottom");

		JLabel lblMediemLabels = new JLabel("Mediem labels");
		add(lblMediemLabels, "cell 2 2,alignx left,aligny bottom");

		JLabel lblHeavyLabels = new JLabel("Heavy labels");
		add(lblHeavyLabels, "cell 2 4,alignx left,aligny bottom");

		JScrollPane quanScrollPane = new JScrollPane();
		add(quanScrollPane, "cell 0 1 1 5,grow");

		labelScrollPane1 = new JScrollPane();
		add(labelScrollPane1, "cell 2 1,grow");

		labelScrollPane2 = new JScrollPane();
		add(labelScrollPane2, "cell 2 3,grow");

		labelScrollPane3 = new JScrollPane();
		add(labelScrollPane3, "cell 2 5,grow");

		JButton buttonLightAdd = new JButton(">");
		add(buttonLightAdd, "flowy,cell 1 1,alignx center");

		JButton buttonLightRemove = new JButton("<");
		add(buttonLightRemove, "cell 1 1,alignx center");

		JButton buttonMediumAdd = new JButton(">");
		add(buttonMediumAdd, "flowy,cell 1 3,alignx center");

		JButton buttonMediumRemove = new JButton("<");
		add(buttonMediumRemove, "cell 1 3,alignx center");

		JButton buttonHeavyAdd = new JButton(">");
		add(buttonHeavyAdd, "flowy,cell 1 5,alignx center");

		JButton buttonHeavyRemove = new JButton("<");
		add(buttonHeavyRemove, "cell 1 5,alignx center");

		buttonLightAdd.setEnabled(false);
		buttonLightAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[0].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[0]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList1.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonLightAdd.setEnabled(false);
		});

		buttonMediumAdd.setEnabled(false);
		buttonMediumAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[1].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[1]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList2.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonMediumAdd.setEnabled(false);
		});

		buttonHeavyAdd.setEnabled(false);
		buttonHeavyAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[2].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[2]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList3.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonHeavyAdd.setEnabled(false);
		});

		buttonLightRemove.setEnabled(false);
		buttonLightRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList1.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[0].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[0]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList1.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList1.clearSelection();
			buttonLightRemove.setEnabled(false);
		});

		buttonMediumRemove.setEnabled(false);
		buttonMediumRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList2.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[1].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[1]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList2.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList2.clearSelection();
			buttonMediumRemove.setEnabled(false);
		});

		buttonHeavyRemove.setEnabled(false);
		buttonHeavyRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList3.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[2].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[2]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList3.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList3.clearSelection();
			buttonHeavyRemove.setEnabled(false);
		});

		MaxquantModification[] mods = MaxQuantModIO.getIsotopicLabels();
		if (mods != null) {
			labelList0 = new JList<MaxquantModification>(mods);
		} else {
			labelList0 = new JList<MaxquantModification>();
		}

		quanScrollPane.setViewportView(labelList0);
		labelList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList0.getSelectedIndex() == -1) {
						buttonLightAdd.setEnabled(false);
						buttonMediumAdd.setEnabled(false);
						buttonHeavyAdd.setEnabled(false);
					} else {
						buttonLightAdd.setEnabled(true);
						buttonMediumAdd.setEnabled(true);
						buttonHeavyAdd.setEnabled(true);
					}
				}
			}
		});

		MaxquantModification[][] labels = par.getLabels();

		this.labelList1 = new JList<MaxquantModification>(labels[0]);
		labelScrollPane1.setViewportView(labelList1);
		labelList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList1.getSelectedIndex() == -1) {
						buttonLightRemove.setEnabled(false);
					} else {
						buttonLightRemove.setEnabled(true);
					}
				}
			}
		});

		this.labelList2 = new JList<MaxquantModification>(labels[1]);
		labelScrollPane2.setViewportView(labelList2);
		labelList2.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList2.getSelectedIndex() == -1) {
						buttonMediumRemove.setEnabled(false);
					} else {
						buttonMediumRemove.setEnabled(true);
					}
				}
			}
		});

		this.labelList3 = new JList<MaxquantModification>(labels[2]);
		labelScrollPane3.setViewportView(labelList3);
		labelList3.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList3.getSelectedIndex() == -1) {
						buttonHeavyRemove.setEnabled(false);
					} else {
						buttonHeavyRemove.setEnabled(true);
					}
				}
			}
		});
	}

	/**
	 * Create the panel.
	 */
	@SuppressWarnings("unchecked")
	public MetaLabIsotopicQuanPanel() {

		setLayout(new MigLayout("", "[350][50][350]", "[][100,grow 250][][100,grow 250][][100,grow 250]"));

		this.labelSets = new HashSet[3];
		for (int i = 0; i < this.labelSets.length; i++) {
			labelSets[i] = new HashSet<MaxquantModification>();
		}

		JLabel lblIsotopeLabels = new JLabel("Isotope labels");
		add(lblIsotopeLabels, "cell 0 0,alignx left,aligny bottom");

		JLabel lblLightLabels = new JLabel("Light labels");
		add(lblLightLabels, "cell 2 0,alignx left,aligny bottom");

		JLabel lblMediemLabels = new JLabel("Mediem labels");
		add(lblMediemLabels, "cell 2 2,alignx left,aligny bottom");

		JLabel lblHeavyLabels = new JLabel("Heavy labels");
		add(lblHeavyLabels, "cell 2 4,alignx left,aligny bottom");

		JScrollPane quanScrollPane = new JScrollPane();
		add(quanScrollPane, "cell 0 1 1 5,grow");

		labelScrollPane1 = new JScrollPane();
		add(labelScrollPane1, "cell 2 1,grow");

		labelScrollPane2 = new JScrollPane();
		add(labelScrollPane2, "cell 2 3,grow");

		labelScrollPane3 = new JScrollPane();
		add(labelScrollPane3, "cell 2 5,grow");

		JButton buttonLightAdd = new JButton(">");
		add(buttonLightAdd, "flowy,cell 1 1,alignx center");

		JButton buttonLightRemove = new JButton("<");
		add(buttonLightRemove, "cell 1 1,alignx center");

		JButton buttonMediumAdd = new JButton(">");
		add(buttonMediumAdd, "flowy,cell 1 3,alignx center");

		JButton buttonMediumRemove = new JButton("<");
		add(buttonMediumRemove, "cell 1 3,alignx center");

		JButton buttonHeavyAdd = new JButton(">");
		add(buttonHeavyAdd, "flowy,cell 1 5,alignx center");

		JButton buttonHeavyRemove = new JButton("<");
		add(buttonHeavyRemove, "cell 1 5,alignx center");

		buttonLightAdd.setEnabled(false);
		buttonLightAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[0].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[0]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList1.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonLightAdd.setEnabled(false);
		});

		buttonMediumAdd.setEnabled(false);
		buttonMediumAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[1].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[1]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList2.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonMediumAdd.setEnabled(false);
		});

		buttonHeavyAdd.setEnabled(false);
		buttonHeavyAdd.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList0.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[2].add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[2]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList3.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList0.clearSelection();
			buttonHeavyAdd.setEnabled(false);
		});

		buttonLightRemove.setEnabled(false);
		buttonLightRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList1.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[0].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[0]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList1.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList1.clearSelection();
			buttonLightRemove.setEnabled(false);
		});

		buttonMediumRemove.setEnabled(false);
		buttonMediumRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList2.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[1].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[1]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList2.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList2.clearSelection();
			buttonMediumRemove.setEnabled(false);
		});

		buttonHeavyRemove.setEnabled(false);
		buttonHeavyRemove.addActionListener(l -> {
			MaxquantModification value = MetaLabIsotopicQuanPanel.this.labelList3.getSelectedValue();
			MetaLabIsotopicQuanPanel.this.labelSets[2].remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification mod : MetaLabIsotopicQuanPanel.this.labelSets[2]) {
				model.addElement(mod);
			}
			MetaLabIsotopicQuanPanel.this.labelList3.setModel(model);
			MetaLabIsotopicQuanPanel.this.labelList3.clearSelection();
			buttonHeavyRemove.setEnabled(false);
		});

		MaxquantModification[] mods = MaxQuantModIO.getIsotopicLabels();
		if (mods != null) {
			labelList0 = new JList<MaxquantModification>(mods);
		} else {
			labelList0 = new JList<MaxquantModification>();
		}

		quanScrollPane.setViewportView(labelList0);
		labelList0.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList0.getSelectedIndex() == -1) {
						buttonLightAdd.setEnabled(false);
						buttonMediumAdd.setEnabled(false);
						buttonHeavyAdd.setEnabled(false);
					} else {
						buttonLightAdd.setEnabled(true);
						buttonMediumAdd.setEnabled(true);
						buttonHeavyAdd.setEnabled(true);
					}
				}
			}
		});

		this.labelList1 = new JList<MaxquantModification>();
		labelScrollPane1.setViewportView(labelList1);
		labelList1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList1.getSelectedIndex() == -1) {
						buttonLightRemove.setEnabled(false);
					} else {
						buttonLightRemove.setEnabled(true);
					}
				}
			}
		});

		this.labelList2 = new JList<MaxquantModification>();
		labelScrollPane2.setViewportView(labelList2);
		labelList2.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList2.getSelectedIndex() == -1) {
						buttonMediumRemove.setEnabled(false);
					} else {
						buttonMediumRemove.setEnabled(true);
					}
				}
			}
		});

		this.labelList3 = new JList<MaxquantModification>();
		labelScrollPane3.setViewportView(labelList3);
		labelList3.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (labelList3.getSelectedIndex() == -1) {
						buttonHeavyRemove.setEnabled(false);
					} else {
						buttonHeavyRemove.setEnabled(true);
					}
				}
			}
		});
	}

	public MaxquantModification[][] getLabels() {
		MaxquantModification[][] labels = new MaxquantModification[this.labelSets.length][];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = this.labelSets[i].toArray(new MaxquantModification[labelSets[i].size()]);
		}
		return labels;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] components = getComponents();
		for (Component comp : components) {
			comp.setEnabled(enabled);
		}
		this.labelList0.setEnabled(enabled);
		this.labelList1.setEnabled(enabled);
		this.labelList2.setEnabled(enabled);
		this.labelList3.setEnabled(enabled);
	}

	public String[] getLabelTitle() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < this.labelSets.length; i++) {
			if (labelSets[i].size() > 0) {
				if (i == 0) {
					list.add("light");
				} else if (i == 1) {
					list.add("medium");
				} else if (i == 2) {
					list.add("heavy");
				}
			}
		}
		String[] title = list.toArray(new String[list.size()]);
		return title;
	}
	
	public void removeAllLabels() {

		for (int i = 0; i < this.labelSets.length; i++) {

			if (labelSets[i].size() > 0) {
				if (i == 0) {
					((DefaultListModel<MaxquantModification>) this.labelList1.getModel()).removeAllElements();
				} else if (i == 1) {
					((DefaultListModel<MaxquantModification>) this.labelList2.getModel()).removeAllElements();
				} else if (i == 2) {
					((DefaultListModel<MaxquantModification>) this.labelList3.getModel()).removeAllElements();
				}
				labelSets[i] = new HashSet<MaxquantModification>();
			}
		}

		this.updateUI();
	}

}
