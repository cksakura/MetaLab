package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Color;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagModel;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagPepLib;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagVersion;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabParViewPanel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class MagDbStatPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField timeTextField;
	
	private MetaParameterDia metaPar;
	private JProgressBar progressBar;
	private JButton predictButton;
	private JScrollPane treeScrollPane;
	private JTree tree;
	private DefaultTreeModel treeModel;
	private JButton btnUpdate;
	/**
	 * Create the panel.
	 */
	public MagDbStatPanel(MetaParameterDia metaPar, MetaSourcesDia msd) {
		setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"MAG library status", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[380:590:840,grow][60]", "[180:300:420,grow][30]"));

		this.metaPar = metaPar;
		MagDbItem magDbItem = metaPar.getUsedMagDbItem();

		if (magDbItem != null) {
			MagVersion usedVersion = magDbItem.getUsedVersion();
			int speciesCount = usedVersion.getSpeciesCount();

			File predictedPepFile = magDbItem.getPredictedPepFile();

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(
					magDbItem.getCatalogueID() + " " + usedVersion.getCatalogueVersion());

			DefaultMutableTreeNode spCountNode = new DefaultMutableTreeNode("Species count: " + speciesCount);
			DefaultMutableTreeNode prePepNode = new DefaultMutableTreeNode("Predicted peptides");
			if (predictedPepFile.exists()) {
				File[] files = predictedPepFile.listFiles();
				if (files.length == speciesCount) {
					prePepNode.add(new DefaultMutableTreeNode("Predicted peptides are available now"));
				} else {
					prePepNode.add(new DefaultMutableTreeNode("Peptide prediction is not finished yet"));
				}
			} else {
				prePepNode.add(new DefaultMutableTreeNode("Please predict the peptide detectability first"));
			}

			DefaultMutableTreeNode libNode = new DefaultMutableTreeNode("Peptide libraries");
			MagPepLib[] magLibs = magDbItem.getMagPepLibs();
			if (magLibs.length == 0) {
				libNode.add(new DefaultMutableTreeNode("Please create a peptide library first"));
			} else {
				libNode.add(new DefaultMutableTreeNode(magLibs.length + " libraries are available"));
			}

			DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode("Peptide models");
			MagModel[] magModels = magDbItem.getMagModels();
			if (magModels.length == 0) {
				modelNode.add(new DefaultMutableTreeNode("Please build a model first"));
			} else {
				modelNode.add(new DefaultMutableTreeNode(magModels.length + " models are available"));
			}

			root.add(spCountNode);
			root.add(prePepNode);
			root.add(libNode);
			root.add(modelNode);

			this.treeModel = new DefaultTreeModel(root);
		} else {

			this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("null"));
		}

		this.tree = new JTree(treeModel);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setClosedIcon(new ImageIcon(
				MetaLabParViewPanel.class.getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
		renderer.setOpenIcon(
				new ImageIcon(MetaLabParViewPanel.class.getResource("/toolbarButtonGraphics/navigation/Down16.gif")));

		expandAllNodes(tree, 0, tree.getRowCount());

		treeScrollPane = new JScrollPane(tree);
		add(treeScrollPane, "cell 0 0 2 1,grow");

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Predict peptide detectability", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));
		add(panel, "cell 0 1,grow");
		panel.setLayout(new MigLayout("", "[60][60:260:460,grow][120:120]", "[30]"));

		progressBar = new JProgressBar();
		panel.add(progressBar, "cell 1 0,growx");

		predictButton = new JButton("Predict");
		panel.add(predictButton, "cell 0 0");

		timeTextField = new JTextField();
		timeTextField.setEditable(false);
		panel.add(timeTextField, "cell 2 0,growx");
		timeTextField.setColumns(10);

		btnUpdate = new JButton("Update");
		add(btnUpdate, "cell 1 1,grow");
	}

	private static void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
		for (int i = startingIndex; i < rowCount; i++) {
			tree.expandRow(i);
		}

		if (tree.getRowCount() != rowCount) {
			expandAllNodes(tree, rowCount, tree.getRowCount());
		}
	}

	public JTextField getTimeTextField() {
		return timeTextField;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JButton getPredictButton() {
		return predictButton;
	}
	
	public JButton getUpdateButton() {
		return btnUpdate;
	}
	
	public void update() {
		MagDbItem magDbItem = metaPar.getUsedMagDbItem();
		if (magDbItem == null) {
			return;
		}
		MagVersion usedVersion = magDbItem.getUsedVersion();
		int speciesCount = usedVersion.getSpeciesCount();

		File predictedPepFile = magDbItem.getPredictedPepFile();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(
				magDbItem.getCatalogueID() + " " + usedVersion.getCatalogueVersion());

		DefaultMutableTreeNode spCountNode = new DefaultMutableTreeNode("Species count: " + speciesCount);
		DefaultMutableTreeNode prePepNode = new DefaultMutableTreeNode("Predicted peptides");
		if (predictedPepFile.exists()) {
			File[] files = predictedPepFile.listFiles();
			if (files.length == speciesCount) {
				prePepNode.add(new DefaultMutableTreeNode("Predicted peptides are available now"));
				progressBar.setValue(100);
				timeTextField.setText("Complete");
			} else {
				prePepNode.add(new DefaultMutableTreeNode("Peptide prediction is not finished yet"));
				progressBar.setValue(0);
				timeTextField.setText("");
			}
		} else {
			prePepNode.add(new DefaultMutableTreeNode("Please predict the peptide detectability first"));
			progressBar.setValue(0);
			timeTextField.setText("");
		}

		DefaultMutableTreeNode libNode = new DefaultMutableTreeNode("Peptide libraries");
		MagPepLib[] magLibs = magDbItem.getMagPepLibs();
		if (magLibs.length == 0) {
			libNode.add(new DefaultMutableTreeNode("Please create a peptide library first"));
		} else {
			libNode.add(new DefaultMutableTreeNode(magLibs.length + " libraries are available"));
		}

		DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode("Peptide models");
		MagModel[] magModels = magDbItem.getMagModels();
		if (magModels.length == 0) {
			modelNode.add(new DefaultMutableTreeNode("Please build a model first"));
		} else {
			modelNode.add(new DefaultMutableTreeNode(magModels.length + " models are available"));
		}

		root.add(spCountNode);
		root.add(prePepNode);
		root.add(libNode);
		root.add(modelNode);

		treeModel.setRoot(root);
		treeModel.reload();

		expandAllNodes(tree, 0, tree.getRowCount());
	}

}
