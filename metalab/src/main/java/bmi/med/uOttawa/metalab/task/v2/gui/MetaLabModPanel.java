/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MaxQuantModIO;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

import javax.swing.JList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabModPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7975648323922016447L;

	private JList<MaxquantModification> modlist;
	private JList<MaxquantModification> fixlist;
	private JList<MaxquantModification> varilist;

	private HashSet<MaxquantModification> vmUsed;
	private HashSet<MaxquantModification> fmUsed;

	/**
	 * Create the panel.
	 * 
	 * @wbp.parser.constructor
	 */
	public MetaLabModPanel(MetaParameterMQ par) {

		setLayout(new MigLayout("", "[125:375:455,grow][50][125:375:455,grow]",
				"[25:50:75,grow][50][50][25:50:75,grow][25:50:75,grow][50][50][25:50:75,grow]"));

		MaxquantModification[] fixMods = par.getFixMods();
		MaxquantModification[] variMods = par.getVariMods();

		this.fmUsed = new HashSet<MaxquantModification>();
		this.vmUsed = new HashSet<MaxquantModification>();

		for (int i = 0; i < fixMods.length; i++) {
			this.fmUsed.add(fixMods[i]);
		}

		for (int i = 0; i < variMods.length; i++) {
			this.vmUsed.add(variMods[i]);
		}

		JButton buttonFixAdd = new JButton(">");
		buttonFixAdd.setEnabled(false);
		add(buttonFixAdd, "cell 1 1,alignx center");
		buttonFixAdd.addActionListener(l -> {
			MaxquantModification value = modlist.getSelectedValue();
			fmUsed.add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : fmUsed) {
				model.addElement(vm);
			}
			fixlist.setModel(model);
			modlist.clearSelection();
			buttonFixAdd.setEnabled(false);
		});

		JButton buttonFixRemove = new JButton("<");
		buttonFixRemove.setEnabled(false);
		add(buttonFixRemove, "cell 1 2,alignx center");
		buttonFixRemove.addActionListener(l -> {
			MaxquantModification value = fixlist.getSelectedValue();
			fmUsed.remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : fmUsed) {
				model.addElement(vm);
			}
			fixlist.setModel(model);
			fixlist.clearSelection();
			buttonFixAdd.setEnabled(false);
		});

		JButton buttonVariAdd = new JButton(">");
		buttonVariAdd.setEnabled(false);
		add(buttonVariAdd, "cell 1 5,alignx center");
		buttonVariAdd.addActionListener(l -> {
			MaxquantModification value = modlist.getSelectedValue();
			vmUsed.add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : vmUsed) {
				model.addElement(vm);
			}
			varilist.setModel(model);
			modlist.clearSelection();
			buttonVariAdd.setEnabled(false);
		});

		JButton buttonVariRemove = new JButton("<");
		buttonVariRemove.setEnabled(false);
		add(buttonVariRemove, "cell 1 6,alignx center");
		buttonVariRemove.addActionListener(l -> {
			MaxquantModification value = varilist.getSelectedValue();
			vmUsed.remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : vmUsed) {
				model.addElement(vm);
			}
			varilist.setModel(model);
			varilist.clearSelection();
			buttonVariAdd.setEnabled(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(
				new TitledBorder(null, "Modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setToolTipText("");
		add(scrollPane, "cell 0 0 1 8,grow");

		MaxquantModification[] mods = MaxQuantModIO.getMods();
		if (mods != null) {
			modlist = new JList<MaxquantModification>(mods);
		} else {
			modlist = new JList<MaxquantModification>();
		}

		modlist.setBackground(new Color(255, 208, 199));
		scrollPane.setViewportView(modlist);
		modlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (modlist.getSelectedIndex() == -1) {
						buttonFixAdd.setEnabled(false);
						buttonVariAdd.setEnabled(false);
					} else {
						buttonFixAdd.setEnabled(true);
						buttonVariAdd.setEnabled(true);
						fixlist.clearSelection();
						varilist.clearSelection();
					}
				}
			}
		});

		JScrollPane scrollPaneFix = new JScrollPane();
		scrollPaneFix.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneFix.setViewportBorder(
				new TitledBorder(null, "Fixed modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPaneFix.setToolTipText("Fixed modifications");
		add(scrollPaneFix, "cell 2 0 1 4,grow");

		fixlist = new JList<MaxquantModification>();
		fixlist.setBackground(new Color(255, 181, 166));
		scrollPaneFix.setViewportView(fixlist);
		fixlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (fixlist.getSelectedIndex() == -1) {
						buttonFixRemove.setEnabled(false);
					} else {
						buttonFixRemove.setEnabled(true);
						modlist.clearSelection();
						varilist.clearSelection();
					}
				}
			}
		});

		DefaultListModel<MaxquantModification> fixModel = new DefaultListModel<MaxquantModification>();
		for (MaxquantModification fm : fmUsed) {
			fixModel.addElement(fm);
		}
		fixlist.setModel(fixModel);

		JScrollPane scrollPaneVari = new JScrollPane();
		scrollPaneVari.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneVari.setViewportBorder(
				new TitledBorder(null, "Variable modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPaneVari.setToolTipText("Variable modifications");
		add(scrollPaneVari, "cell 2 4 1 4,grow");

		varilist = new JList<MaxquantModification>();
		varilist.setBackground(new Color(255, 181, 166));
		scrollPaneVari.setViewportView(varilist);
		varilist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (varilist.getSelectedIndex() == -1) {
						buttonVariRemove.setEnabled(false);
					} else {
						buttonVariRemove.setEnabled(true);
						fixlist.clearSelection();
						modlist.clearSelection();
					}
				}
			}
		});

		DefaultListModel<MaxquantModification> variModel = new DefaultListModel<MaxquantModification>();
		for (MaxquantModification vm : vmUsed) {
			variModel.addElement(vm);
		}
		varilist.setModel(variModel);
	}

	public MaxquantModification[] getFixMods() {
		MaxquantModification[] fixMods = this.fmUsed.toArray(new MaxquantModification[fmUsed.size()]);
		return fixMods;
	}

	public MaxquantModification[] getVariMods() {
		MaxquantModification[] variMods = this.vmUsed.toArray(new MaxquantModification[vmUsed.size()]);
		return variMods;
	}

	private JList<MaxquantModification> pfindModlist;
	private JList<MaxquantModification> pfindFixlist;
	private JList<MaxquantModification> pfindVarilist;

	private HashSet<MaxquantModification> pfindVmUsed;
	private HashSet<MaxquantModification> pfindFmUsed;

	/**
	 * Create the panel.
	 */
	public MetaLabModPanel(MetaParameterPFind par, MaxquantModification[] pfindMods) {

		setLayout(new MigLayout("", "[125:275:455,grow][50][125:275:455,grow]",
				"[25:60:90,grow][50][50][25:60:90,grow][25:60:90,grow][50][50][25:60:90,grow]"));

		String[] fixMods = par.getFixMods();
		String[] variMods = par.getVariMods();

		this.pfindFmUsed = new HashSet<MaxquantModification>();
		this.pfindVmUsed = new HashSet<MaxquantModification>();

		for (int i = 0; i < fixMods.length; i++) {
			for (int j = 0; j < pfindMods.length; j++) {
				if (fixMods[i].equals(pfindMods[j].getTitle())) {
					this.pfindFmUsed.add(pfindMods[j]);
					break;
				}
			}
		}

		for (int i = 0; i < variMods.length; i++) {
			for (int j = 0; j < pfindMods.length; j++) {
				if (variMods[i].equals(pfindMods[j].getTitle())) {
					this.pfindVmUsed.add(pfindMods[j]);
					break;
				}
			}
		}

		JButton buttonFixAdd = new JButton(">");
		buttonFixAdd.setEnabled(false);
		add(buttonFixAdd, "cell 1 1,alignx center");
		buttonFixAdd.addActionListener(l -> {
			MaxquantModification value = pfindModlist.getSelectedValue();
			pfindFmUsed.add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : pfindFmUsed) {
				model.addElement(vm);
			}
			pfindFixlist.setModel(model);
			pfindModlist.clearSelection();
			buttonFixAdd.setEnabled(false);
		});

		JButton buttonFixRemove = new JButton("<");
		buttonFixRemove.setEnabled(false);
		add(buttonFixRemove, "cell 1 2,alignx center");
		buttonFixRemove.addActionListener(l -> {
			MaxquantModification value = pfindFixlist.getSelectedValue();
			pfindFmUsed.remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : pfindFmUsed) {
				model.addElement(vm);
			}
			pfindFixlist.setModel(model);
			pfindFixlist.clearSelection();
			buttonFixAdd.setEnabled(false);
		});

		JButton buttonVariAdd = new JButton(">");
		buttonVariAdd.setEnabled(false);
		add(buttonVariAdd, "cell 1 5,alignx center");
		buttonVariAdd.addActionListener(l -> {
			MaxquantModification value = pfindModlist.getSelectedValue();
			pfindVmUsed.add(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : pfindVmUsed) {
				model.addElement(vm);
			}
			pfindVarilist.setModel(model);
			pfindModlist.clearSelection();
			buttonVariAdd.setEnabled(false);
		});

		JButton buttonVariRemove = new JButton("<");
		buttonVariRemove.setEnabled(false);
		add(buttonVariRemove, "cell 1 6,alignx center");
		buttonVariRemove.addActionListener(l -> {
			MaxquantModification value = pfindVarilist.getSelectedValue();
			pfindVmUsed.remove(value);
			DefaultListModel<MaxquantModification> model = new DefaultListModel<MaxquantModification>();
			for (MaxquantModification vm : pfindVmUsed) {
				model.addElement(vm);
			}
			pfindVarilist.setModel(model);
			pfindVarilist.clearSelection();
			buttonVariAdd.setEnabled(false);
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(
				new TitledBorder(null, "Modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setToolTipText("");
		add(scrollPane, "cell 0 0 1 8,grow");

		if (pfindMods != null) {
			pfindModlist = new JList<MaxquantModification>(pfindMods);
		} else {
			pfindModlist = new JList<MaxquantModification>();
		}

		pfindModlist.setBackground(new Color(255, 208, 199));
		scrollPane.setViewportView(pfindModlist);
		pfindModlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (pfindModlist.getSelectedIndex() == -1) {
						buttonFixAdd.setEnabled(false);
						buttonVariAdd.setEnabled(false);
					} else {
						buttonFixAdd.setEnabled(true);
						buttonVariAdd.setEnabled(true);
						pfindFixlist.clearSelection();
						pfindVarilist.clearSelection();
					}
				}
			}
		});

		JScrollPane scrollPaneFix = new JScrollPane();
		scrollPaneFix.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneFix.setViewportBorder(
				new TitledBorder(null, "Fixed modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPaneFix.setToolTipText("Fixed modifications");
		add(scrollPaneFix, "cell 2 0 1 4,grow");

		pfindFixlist = new JList<MaxquantModification>();
		pfindFixlist.setBackground(new Color(255, 181, 166));
		scrollPaneFix.setViewportView(pfindFixlist);
		pfindFixlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (pfindFixlist.getSelectedIndex() == -1) {
						buttonFixRemove.setEnabled(false);
					} else {
						buttonFixRemove.setEnabled(true);
						pfindModlist.clearSelection();
						pfindVarilist.clearSelection();
					}
				}
			}
		});

		DefaultListModel<MaxquantModification> fixModel = new DefaultListModel<MaxquantModification>();
		for (MaxquantModification fm : pfindFmUsed) {
			fixModel.addElement(fm);
		}
		pfindFixlist.setModel(fixModel);

		JScrollPane scrollPaneVari = new JScrollPane();
		scrollPaneVari.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneVari.setViewportBorder(
				new TitledBorder(null, "Variable modifications", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPaneVari.setToolTipText("Variable modifications");
		add(scrollPaneVari, "cell 2 4 1 4,grow");

		pfindVarilist = new JList<MaxquantModification>();
		pfindVarilist.setBackground(new Color(255, 181, 166));
		scrollPaneVari.setViewportView(pfindVarilist);
		pfindVarilist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					if (pfindVarilist.getSelectedIndex() == -1) {
						buttonVariRemove.setEnabled(false);
					} else {
						buttonVariRemove.setEnabled(true);
						pfindFixlist.clearSelection();
						pfindModlist.clearSelection();
					}
				}
			}
		});

		DefaultListModel<MaxquantModification> variModel = new DefaultListModel<MaxquantModification>();
		for (MaxquantModification vm : pfindVmUsed) {
			variModel.addElement(vm);
		}
		pfindVarilist.setModel(variModel);
	}

	public String[] getPFindFixMods() {
		MaxquantModification[] fixMods = this.pfindFmUsed.toArray(new MaxquantModification[pfindFmUsed.size()]);
		String[] fixModNames = new String[fixMods.length];
		for (int i = 0; i < fixModNames.length; i++) {
			fixModNames[i] = fixMods[i].getTitle();
		}
		return fixModNames;
	}

	public String[] getPFindVariMods() {
		MaxquantModification[] variMods = this.pfindVmUsed.toArray(new MaxquantModification[pfindVmUsed.size()]);
		String[] variModNames = new String[variMods.length];
		for (int i = 0; i < variModNames.length; i++) {
			variModNames[i] = variMods[i].getTitle();
		}
		return variModNames;
	}

	public MaxquantModification[] getPFindFullFixMods() {
		MaxquantModification[] fixMods = this.pfindFmUsed.toArray(new MaxquantModification[pfindFmUsed.size()]);
		return fixMods;
	}

	public MaxquantModification[] getPFindFullVariMods() {
		MaxquantModification[] variMods = this.pfindVmUsed.toArray(new MaxquantModification[pfindVmUsed.size()]);
		return variMods;
	}

	public void setEnabledAll(boolean enable) {
		Component[] components = getComponents();
		for (Component component : components) {
			component.setEnabled(enable);
		}
	}
}
