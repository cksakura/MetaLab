/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzyme;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.dia.par.MetaDiaParSearchLib;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParameterHGM;
import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.par.MetaSsdbCreatePar;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaMaxQuantPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This panel provides a tree view of the parameters.
 * 
 * @author Kai Cheng
 *
 */
public class MetaLabParViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8154819987609293129L;

	private JScrollPane scrollPane;
	private JTree tree;
	private MetaParameter par;

	/**
	 * Create the panel.
	 */
	public MetaLabParViewPanel(MetaParameter par) {

		setLayout(new MigLayout("", "[400:600:800,grow]", "[300:480:640,grow]"));

		this.par = par;

		scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 0,grow");

		tree = new JTree();

		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setClosedIcon(new ImageIcon(
				MetaLabParViewPanel.class.getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
		renderer.setOpenIcon(
				new ImageIcon(MetaLabParViewPanel.class.getResource("/toolbarButtonGraphics/navigation/Down16.gif")));

		update();
	}

	public void update() {
		tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Parameters") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4562255177222978283L;

			{

				DefaultMutableTreeNode workflowNode = new DefaultMutableTreeNode("Work flow");
				MetaLabWorkflowType workflowType = par.getWorkflowType();

				workflowNode.add(new DefaultMutableTreeNode(workflowType.getDescription()));
				add(workflowNode);

				if (workflowType == MetaLabWorkflowType.TaxonomyAnalysis) {

					DefaultMutableTreeNode resultNode = new DefaultMutableTreeNode("Result");
					resultNode.add(new DefaultMutableTreeNode(((MetaParameterMQ) par).getQuickOutput()));
					add(resultNode);

					MetaPeptide[] mps = ((MetaParameterMQ) par).getPeps();
					if (mps == null) {
						DefaultMutableTreeNode pepCountNode = new DefaultMutableTreeNode("Peptide count");
						pepCountNode.add(new DefaultMutableTreeNode("No peptide"));
						add(pepCountNode);
					} else {
						int pepCount = mps.length;
						DefaultMutableTreeNode pepCountNode = new DefaultMutableTreeNode("Peptide count");
						pepCountNode.add(new DefaultMutableTreeNode(pepCount));
						add(pepCountNode);
					}

					String[] titles = ((MetaParameterMQ) par).getIntensityTitles();
					if (titles == null) {
						DefaultMutableTreeNode titleNode = new DefaultMutableTreeNode("Experimental names");
						titleNode.add(new DefaultMutableTreeNode("No experimental names"));
						add(titleNode);
					} else {
						StringBuilder titleSb = new StringBuilder();
						for (int i = 0; i < titles.length; i++) {
							titleSb.append(titles[i]).append(";");
						}
						DefaultMutableTreeNode titleNode = new DefaultMutableTreeNode("Experimental names");
						titleNode.add(new DefaultMutableTreeNode("<html>" + titleSb.toString() + "</html>"));
						add(titleNode);
					}

					MetaPep2TaxaPar mptp = ((MetaParameterMQ) par).getPtp();
					DefaultMutableTreeNode builtinNode = new DefaultMutableTreeNode(
							"Taxonomy analysis by Built-in database");
					builtinNode.add(new DefaultMutableTreeNode(mptp.isBuildIn()));
					add(builtinNode);

					DefaultMutableTreeNode unipeptNode = new DefaultMutableTreeNode(
							"Taxonomy analysis by Unipept database");
					unipeptNode.add(new DefaultMutableTreeNode(mptp.isUnipept()));
					add(unipeptNode);

					HashSet<RootType> usedRoots = mptp.getUsedRootTypes();
					DefaultMutableTreeNode usedRootsNode = new DefaultMutableTreeNode("Used root nodes");
					for (RootType rt : usedRoots) {
						usedRootsNode.add(new DefaultMutableTreeNode(rt.getName()));
					}
					add(usedRootsNode);

					TaxonomyRanks blank = mptp.getIgnoreBlankRank();
					DefaultMutableTreeNode blankNode = new DefaultMutableTreeNode("Ignore blands below");
					blankNode.add(new DefaultMutableTreeNode(blank.getName()));
					add(blankNode);

					HashSet<String> exclude = mptp.getExcludeTaxa();
					DefaultMutableTreeNode excludeNode = new DefaultMutableTreeNode("Ignore taxa below");
					for (String ex : exclude) {
						excludeNode.add(new DefaultMutableTreeNode(ex));
					}
					add(excludeNode);

				} else if (workflowType == MetaLabWorkflowType.FunctionalAnnotation) {

					DefaultMutableTreeNode resultNode = new DefaultMutableTreeNode("Result");
					resultNode.add(new DefaultMutableTreeNode(((MetaParameterMQ) par).getQuickOutput()));
					add(resultNode);

					MetaProtein[] mps = ((MetaParameterMQ) par).getPros();
					if (mps == null) {
						DefaultMutableTreeNode proCountNode = new DefaultMutableTreeNode("Protein group count");
						proCountNode.add(new DefaultMutableTreeNode("No protein"));
						add(proCountNode);
					} else {
						int proCount = ((MetaParameterMQ) par).getPros().length;
						DefaultMutableTreeNode proCountNode = new DefaultMutableTreeNode("Protein group count");
						proCountNode.add(new DefaultMutableTreeNode(proCount));
						add(proCountNode);
					}

					String[] titles = ((MetaParameterMQ) par).getIntensityTitles();
					if (titles == null) {
						DefaultMutableTreeNode titleNode = new DefaultMutableTreeNode("Experimental names");
						titleNode.add(new DefaultMutableTreeNode("No experimental names"));
						add(titleNode);
					} else {
						StringBuilder titleSb = new StringBuilder();
						for (int i = 0; i < titles.length; i++) {
							titleSb.append(titles[i]).append(";");
						}
						DefaultMutableTreeNode titleNode = new DefaultMutableTreeNode("Experimental names");
						titleNode.add(new DefaultMutableTreeNode("<html>" + titleSb.toString() + "</html>"));
						add(titleNode);
					}

				} else {

					DefaultMutableTreeNode resultNode = new DefaultMutableTreeNode("Result");
					resultNode.add(new DefaultMutableTreeNode(par.getResult()));
					add(resultNode);

					MetaData metadata = par.getMetadata();
					DefaultMutableTreeNode rawFileNode = new DefaultMutableTreeNode("Raw files");
					String[] rawfiles = metadata.getRawFiles();
					for (int i = 0; i < rawfiles.length; i++) {
						rawFileNode.add(new DefaultMutableTreeNode(rawfiles[i]));
					}
					add(rawFileNode);

					DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode("Protein database");
					databaseNode.add(new DefaultMutableTreeNode(par.getMicroDb()));
					add(databaseNode);

					DefaultMutableTreeNode appendNode = new DefaultMutableTreeNode("Append host database");
					appendNode.add(new DefaultMutableTreeNode(par.isAppendHostDb()));
					add(appendNode);

					if (par.isAppendHostDb()) {
						DefaultMutableTreeNode hostDbNode = new DefaultMutableTreeNode("Host database");
						hostDbNode.add(new DefaultMutableTreeNode(par.getHostDB()));
						add(hostDbNode);
					}

					DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode("Thread count");
					threadNode.add(new DefaultMutableTreeNode(par.getThreadCount()));
					add(threadNode);

					if (workflowType == MetaLabWorkflowType.pFindWorkflow) {

						DefaultMutableTreeNode instruNode = new DefaultMutableTreeNode("MS2 scan mode");
						instruNode.add(new DefaultMutableTreeNode(par.getMs2ScanMode()));
						add(instruNode);

						MetaParameterPFind parV3 = (MetaParameterPFind) par;
						DefaultMutableTreeNode fixModNode = new DefaultMutableTreeNode("Fixed modifications");
						String[] fixedMods = parV3.getFixMods();
						for (int i = 0; i < fixedMods.length; i++) {
							fixModNode.add(new DefaultMutableTreeNode(fixedMods[i]));
						}
						add(fixModNode);

						DefaultMutableTreeNode variModNode = new DefaultMutableTreeNode("Variable modifications");
						String[] variMods = parV3.getVariMods();
						for (int i = 0; i < variMods.length; i++) {
							variModNode.add(new DefaultMutableTreeNode(variMods[i]));
						}
						add(variModNode);

						DefaultMutableTreeNode enzymeNode = new DefaultMutableTreeNode("Enzyme");
						enzymeNode.add(new DefaultMutableTreeNode(parV3.getEnzyme()));
						add(enzymeNode);

						DefaultMutableTreeNode digestionNode = new DefaultMutableTreeNode("Digestion mode");
						digestionNode
								.add(new DefaultMutableTreeNode(MaxquantEnzyme.enzymeModes[parV3.getDigestMode()]));
						add(digestionNode);

						DefaultMutableTreeNode missCleavageNode = new DefaultMutableTreeNode("Max missed cleavaged");
						missCleavageNode.add(new DefaultMutableTreeNode(parV3.getMissCleavages()));
						add(missCleavageNode);

						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV3.getQuanMode()));
						add(quanNode);

						if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV3.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						MetaSsdbCreatePar mscp = parV3.getMscp();

						DefaultMutableTreeNode ssdbNode = new DefaultMutableTreeNode(
								"Generate sample-specific database");
						ssdbNode.add(new DefaultMutableTreeNode(mscp.isSsdb()));
						add(ssdbNode);

						if (mscp.isSsdb()) {
							DefaultMutableTreeNode clusterNode = new DefaultMutableTreeNode("Database generation mode");
							if (mscp.isCluster()) {
								clusterNode.add(new DefaultMutableTreeNode("Spectra cluster"));
								add(clusterNode);
							} else {
								clusterNode.add(new DefaultMutableTreeNode("MetaProIq"));
								add(clusterNode);
							}

							DefaultMutableTreeNode evalueNode = new DefaultMutableTreeNode("E-value threshold");
							evalueNode.add(new DefaultMutableTreeNode(mscp.getXtandemEvalue()));
							add(evalueNode);
						}

						MetaOpenPar mop = parV3.getMop();

						DefaultMutableTreeNode openNode = new DefaultMutableTreeNode("Open search");
						openNode.add(new DefaultMutableTreeNode(mop.isOpenSearch()));
						add(openNode);

						DefaultMutableTreeNode psmfdrNode = new DefaultMutableTreeNode("Open search PSM FDR");
						psmfdrNode.add(new DefaultMutableTreeNode(mop.getOpenPsmFDR()));
						add(psmfdrNode);

						DefaultMutableTreeNode profdrNode = new DefaultMutableTreeNode("Open search protein FDR");
						profdrNode.add(new DefaultMutableTreeNode(mop.getOpenProFDR()));
						add(profdrNode);

						DefaultMutableTreeNode mbrNode = new DefaultMutableTreeNode("Match between runs");
						mbrNode.add(new DefaultMutableTreeNode(mop.isMatchBetweenRuns()));
						add(mbrNode);

						if (par.isMetaWorkflow()) {

							MetaPep2TaxaPar mptp = parV3.getPtp();
							DefaultMutableTreeNode builtinNode = new DefaultMutableTreeNode(
									"Taxonomy analysis by Built-in database");
							builtinNode.add(new DefaultMutableTreeNode(mptp.isBuildIn()));
							add(builtinNode);

							DefaultMutableTreeNode unipeptNode = new DefaultMutableTreeNode(
									"Taxonomy analysis by Unipept database");
							unipeptNode.add(new DefaultMutableTreeNode(mptp.isUnipept()));
							add(unipeptNode);

							HashSet<RootType> usedRoots = mptp.getUsedRootTypes();
							DefaultMutableTreeNode usedRootsNode = new DefaultMutableTreeNode("Used root nodes");
							for (RootType rt : usedRoots) {
								usedRootsNode.add(new DefaultMutableTreeNode(rt.getName()));
							}
							add(usedRootsNode);

							TaxonomyRanks blank = mptp.getIgnoreBlankRank();
							DefaultMutableTreeNode blankNode = new DefaultMutableTreeNode("Ignore blands below");
							blankNode.add(new DefaultMutableTreeNode(blank.getName()));
							add(blankNode);

							HashSet<String> exclude = mptp.getExcludeTaxa();
							DefaultMutableTreeNode excludeNode = new DefaultMutableTreeNode("Ignore taxa below");
							for (String ex : exclude) {
								excludeNode.add(new DefaultMutableTreeNode(ex));
							}
							add(excludeNode);

						}

					} else if (workflowType == MetaLabWorkflowType.pFindHGM
							|| workflowType == MetaLabWorkflowType.pFindMAG) {

						DefaultMutableTreeNode instruNode = new DefaultMutableTreeNode("MS2 scan mode");
						instruNode.add(new DefaultMutableTreeNode(par.getMs2ScanMode()));
						add(instruNode);

						MetaParameterHGM parV3 = (MetaParameterHGM) par;
						DefaultMutableTreeNode fixModNode = new DefaultMutableTreeNode("Fixed modifications");
						String[] fixedMods = parV3.getFixMods();
						for (int i = 0; i < fixedMods.length; i++) {
							fixModNode.add(new DefaultMutableTreeNode(fixedMods[i]));
						}
						add(fixModNode);

						DefaultMutableTreeNode variModNode = new DefaultMutableTreeNode("Variable modifications");
						String[] variMods = parV3.getVariMods();
						for (int i = 0; i < variMods.length; i++) {
							variModNode.add(new DefaultMutableTreeNode(variMods[i]));
						}
						add(variModNode);

						DefaultMutableTreeNode enzymeNode = new DefaultMutableTreeNode("Enzyme");
						enzymeNode.add(new DefaultMutableTreeNode(parV3.getEnzyme()));
						add(enzymeNode);

						DefaultMutableTreeNode digestionNode = new DefaultMutableTreeNode("Digestion mode");
						digestionNode
								.add(new DefaultMutableTreeNode(MaxquantEnzyme.enzymeModes[parV3.getDigestMode()]));
						add(digestionNode);

						DefaultMutableTreeNode missCleavageNode = new DefaultMutableTreeNode("Max missed cleavaged");
						missCleavageNode.add(new DefaultMutableTreeNode(parV3.getMissCleavages()));
						add(missCleavageNode);

						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV3.getQuanMode()));
						add(quanNode);

						if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV3.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						if (workflowType == MetaLabWorkflowType.pFindMAG) {
							MagDbItem magDbItem = ((MetaParameterMag) parV3).getUsedMagDbItem();
							if (magDbItem != null) {
								DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
								magDbItemNode.add(new DefaultMutableTreeNode(magDbItem.getCatalogueID()));
								add(magDbItemNode);

								DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode(
										"MAG database version");
								magDbVerNode.add(new DefaultMutableTreeNode(magDbItem.getUsedVersion()));
								add(magDbVerNode);
							} else {
								DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
								magDbItemNode.add(new DefaultMutableTreeNode(""));
								add(magDbItemNode);

								DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode(
										"MAG database version");
								magDbVerNode.add(new DefaultMutableTreeNode(""));
								add(magDbVerNode);
							}
						}

					} else if (workflowType == MetaLabWorkflowType.DiaNNMAG) {
						MetaParameterDia diaPar = (MetaParameterDia) par;
						MagDbItem magDbItem = diaPar.getUsedMagDbItem();
						if (magDbItem != null) {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(magDbItem.getCatalogueID()));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(magDbItem.getUsedVersion()));
							add(magDbVerNode);
						} else {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(""));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(""));
							add(magDbVerNode);
						}
						
						String[] libs = diaPar.getLibrary();
						DefaultMutableTreeNode libNode = new DefaultMutableTreeNode("Peptide library");
						for (String lib : libs) {
							libNode.add(new DefaultMutableTreeNode(lib));
						}
						add(libNode);

						DefaultMutableTreeNode iterNode = new DefaultMutableTreeNode("Iterative search");
						iterNode.add(new DefaultMutableTreeNode(!diaPar.isLibrarySearch()));
						add(iterNode);
						
						if (!diaPar.isLibrarySearch()) {
							String[] models = diaPar.getModels();
							if (models.length > 0) {
								DefaultMutableTreeNode useModelNode = new DefaultMutableTreeNode(
										"Use model prediction");
								useModelNode.add(new DefaultMutableTreeNode("Yes"));
								add(useModelNode);

								DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode("Models");
								for (String model : models) {
									modelNode.add(new DefaultMutableTreeNode(model));
								}
								add(modelNode);

							} else {
								DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode("Use model prediction");
								modelNode.add(new DefaultMutableTreeNode("No"));
								add(modelNode);
							}
						}

					} else if (workflowType == MetaLabWorkflowType.DdaDiaMAG) {

						MetaDiaParSearchLib parDia = (MetaDiaParSearchLib) par;

						DiannParameter diannPar = parDia.getDiannPar();

						DefaultMutableTreeNode enzymeNode = new DefaultMutableTreeNode("Enzyme");
						enzymeNode.add(new DefaultMutableTreeNode(diannPar.getEnzyme()));
						add(enzymeNode);

						DefaultMutableTreeNode missCleavageNode = new DefaultMutableTreeNode("Max missed cleavaged");
						missCleavageNode.add(new DefaultMutableTreeNode(diannPar.getMissed_cleavages()));
						add(missCleavageNode);

						DefaultMutableTreeNode Carbami_CNode = new DefaultMutableTreeNode(
								"Fixed Mod. Carbamidomethyl (C)");
						Carbami_CNode.add(new DefaultMutableTreeNode(diannPar.isCarbami_C()));
						add(Carbami_CNode);

						DefaultMutableTreeNode M_excisionNode = new DefaultMutableTreeNode("N-term Met excision");
						M_excisionNode.add(new DefaultMutableTreeNode(diannPar.isM_excision()));
						add(M_excisionNode);

						DefaultMutableTreeNode maxModNode = new DefaultMutableTreeNode("Max variable Mod. count");
						maxModNode.add(new DefaultMutableTreeNode(diannPar.getMaxMod()));
						add(maxModNode);

						DefaultMutableTreeNode oxiNode = new DefaultMutableTreeNode("Variable Mod. Oxidation (M)");
						oxiNode.add(new DefaultMutableTreeNode(diannPar.isOxidation_M()));
						add(oxiNode);

						DefaultMutableTreeNode aceNode = new DefaultMutableTreeNode(
								"Variable Mod. Acetylation (Protein N-term)");
						aceNode.add(new DefaultMutableTreeNode(diannPar.isAcety_proN()));
						add(aceNode);

						DefaultMutableTreeNode phosNode = new DefaultMutableTreeNode(
								"Variable Mod. Phosphorylation (STY)");
						phosNode.add(new DefaultMutableTreeNode(diannPar.isPhospho_STY()));
						add(phosNode);

						DefaultMutableTreeNode kggNode = new DefaultMutableTreeNode(
								"Variable Mod. Ubiquitinylation/GlyGly (K)");
						kggNode.add(new DefaultMutableTreeNode(diannPar.isK_GG()));
						add(kggNode);

						DefaultMutableTreeNode minPepLenNode = new DefaultMutableTreeNode("Min peptide length");
						minPepLenNode.add(new DefaultMutableTreeNode(diannPar.getMin_pep_len()));
						add(minPepLenNode);

						DefaultMutableTreeNode maxPepLenNode = new DefaultMutableTreeNode("Max peptide length");
						maxPepLenNode.add(new DefaultMutableTreeNode(diannPar.getMax_pep_len()));
						add(maxPepLenNode);

						DefaultMutableTreeNode minPrCharNode = new DefaultMutableTreeNode("Min precursor charge");
						minPrCharNode.add(new DefaultMutableTreeNode(diannPar.getMin_pr_charge()));
						add(minPrCharNode);

						DefaultMutableTreeNode maxPrCharNode = new DefaultMutableTreeNode("Max precursor charge");
						maxPrCharNode.add(new DefaultMutableTreeNode(diannPar.getMax_pr_charge()));
						add(maxPrCharNode);

						DefaultMutableTreeNode minPrMzNode = new DefaultMutableTreeNode("Min precursor mz");
						minPrMzNode.add(new DefaultMutableTreeNode(diannPar.getMin_pr_mz()));
						add(minPrMzNode);

						DefaultMutableTreeNode maxPrMzNode = new DefaultMutableTreeNode("Max precursor mz");
						maxPrMzNode.add(new DefaultMutableTreeNode(diannPar.getMax_pr_mz()));
						add(maxPrMzNode);

						DefaultMutableTreeNode minFrMzNode = new DefaultMutableTreeNode("Min fragment mz");
						minFrMzNode.add(new DefaultMutableTreeNode(diannPar.getMin_fr_mz()));
						add(minFrMzNode);

						DefaultMutableTreeNode maxFrMzNode = new DefaultMutableTreeNode("Max fragment mz");
						maxFrMzNode.add(new DefaultMutableTreeNode(diannPar.getMax_fr_mz()));
						add(maxFrMzNode);

						DefaultMutableTreeNode qvalueNode = new DefaultMutableTreeNode("Qvalue");
						qvalueNode.add(new DefaultMutableTreeNode(diannPar.getQvalue()));
						add(qvalueNode);

					} else if (workflowType == MetaLabWorkflowType.FragpipeIMMag) {
						MetaParameterMag parV3 = (MetaParameterMag) par;
						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV3.getQuanMode()));
						add(quanNode);

						if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV3.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						MagDbItem magDbItem = parV3.getUsedMagDbItem();
						if (magDbItem != null) {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(magDbItem.getCatalogueID()));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(magDbItem.getUsedVersion()));
							add(magDbVerNode);
						} else {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(""));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(""));
							add(magDbVerNode);
						}

					} else if (workflowType == MetaLabWorkflowType.AlphapeptIMMag) {
						MetaParameterMag parV3 = (MetaParameterMag) par;
						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV3.getQuanMode()));
						add(quanNode);

						if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV3.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						MagDbItem magDbItem = parV3.getUsedMagDbItem();
						if (magDbItem != null) {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(magDbItem.getCatalogueID()));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(magDbItem.getUsedVersion()));
							add(magDbVerNode);
						} else {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(""));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(""));
							add(magDbVerNode);
						}

					} else if (workflowType == MetaLabWorkflowType.SageMAG) {
						MetaParameterMag parV3 = (MetaParameterMag) par;
						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV3.getQuanMode()));
						add(quanNode);

						if (parV3.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV3.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						MagDbItem magDbItem = parV3.getUsedMagDbItem();
						if (magDbItem != null) {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(magDbItem.getCatalogueID()));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(magDbItem.getUsedVersion()));
							add(magDbVerNode);
						} else {
							DefaultMutableTreeNode magDbItemNode = new DefaultMutableTreeNode("MAG database");
							magDbItemNode.add(new DefaultMutableTreeNode(""));
							add(magDbItemNode);

							DefaultMutableTreeNode magDbVerNode = new DefaultMutableTreeNode("MAG database version");
							magDbVerNode.add(new DefaultMutableTreeNode(""));
							add(magDbVerNode);
						}

					} else {
						MetaParameterMQ parV2 = (MetaParameterMQ) par;
						DefaultMutableTreeNode fixModNode = new DefaultMutableTreeNode("Fixed modifications");
						MaxquantModification[] fixedMods = parV2.getFixMods();
						for (int i = 0; i < fixedMods.length; i++) {
							fixModNode.add(new DefaultMutableTreeNode(fixedMods[i].getTitle()));
						}
						add(fixModNode);

						DefaultMutableTreeNode variModNode = new DefaultMutableTreeNode("Variable modifications");
						MaxquantModification[] variMods = parV2.getVariMods();
						for (int i = 0; i < variMods.length; i++) {
							variModNode.add(new DefaultMutableTreeNode(variMods[i].getTitle()));
						}
						add(variModNode);

						DefaultMutableTreeNode enzymeNode = new DefaultMutableTreeNode("Enzyme");
						enzymeNode.add(new DefaultMutableTreeNode(parV2.getEnzyme().getName()));
						add(enzymeNode);

						DefaultMutableTreeNode digestionNode = new DefaultMutableTreeNode("Digestion mode");
						digestionNode
								.add(new DefaultMutableTreeNode(MaxquantEnzyme.enzymeModes[parV2.getDigestMode()]));
						add(digestionNode);

						DefaultMutableTreeNode missCleavageNode = new DefaultMutableTreeNode("Max missed cleavaged");
						missCleavageNode.add(new DefaultMutableTreeNode(parV2.getMissCleavages()));
						add(missCleavageNode);

						DefaultMutableTreeNode quanNode = new DefaultMutableTreeNode("Quantification mode");
						quanNode.add(new DefaultMutableTreeNode(parV2.getQuanMode()));
						add(quanNode);

						if (parV2.getQuanMode().equals(MetaConstants.isotopicLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[][] labels = parV2.getLabels();
							for (int i = 0; i < labels.length; i++) {
								StringBuilder sb = new StringBuilder();
								for (int j = 0; j < labels[i].length; j++) {
									sb.append(labels[i][j]).append(";");
								}

								if (sb.length() > 0) {
									labelNode.add(new DefaultMutableTreeNode(sb.substring(1)));
								}
							}
							add(labelNode);
						} else if (parV2.getQuanMode().equals(MetaConstants.isobaricLabel)) {
							DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode("Labels");
							MaxquantModification[] labels = parV2.getIsobaric();
							for (int i = 0; i < labels.length; i++) {
								labelNode.add(new DefaultMutableTreeNode(labels[i]));
							}
							add(labelNode);
						}

						MetaSsdbCreatePar mscp = parV2.getMscp();
						DefaultMutableTreeNode ssdbNode = new DefaultMutableTreeNode(
								"Generate sample-specific database");
						ssdbNode.add(new DefaultMutableTreeNode(mscp.isSsdb()));
						add(ssdbNode);

						if (mscp.isSsdb()) {
							DefaultMutableTreeNode clusterNode = new DefaultMutableTreeNode("Database generation mode");
							if (mscp.isCluster()) {
								clusterNode.add(new DefaultMutableTreeNode("Spectra cluster"));
								add(clusterNode);
							} else {
								clusterNode.add(new DefaultMutableTreeNode("MetaProIq"));
								add(clusterNode);
							}

							DefaultMutableTreeNode evalueNode = new DefaultMutableTreeNode("E-value threshold");
							evalueNode.add(new DefaultMutableTreeNode(mscp.getXtandemEvalue()));
							add(evalueNode);
						}

						if (parV2.isOpenSearch()) {
							MetaOpenPar mop = parV2.getOsp();

							DefaultMutableTreeNode openNode = new DefaultMutableTreeNode("Open search");
							openNode.add(new DefaultMutableTreeNode(mop.isOpenSearch()));
							add(openNode);

							DefaultMutableTreeNode psmfdrNode = new DefaultMutableTreeNode("Open search PSM FDR");
							psmfdrNode.add(new DefaultMutableTreeNode(mop.getOpenPsmFDR()));
							add(psmfdrNode);

							DefaultMutableTreeNode pepfdrNode = new DefaultMutableTreeNode("Open search peptide FDR");
							pepfdrNode.add(new DefaultMutableTreeNode(mop.getOpenPepFDR()));
							add(pepfdrNode);

							DefaultMutableTreeNode profdrNode = new DefaultMutableTreeNode("Open search protein FDR");
							profdrNode.add(new DefaultMutableTreeNode(mop.getOpenProFDR()));
							add(profdrNode);

							DefaultMutableTreeNode r2Node = new DefaultMutableTreeNode("Gaussian fitting R2 threshold");
							r2Node.add(new DefaultMutableTreeNode(mop.getGaussianR2()));
							add(r2Node);

							DefaultMutableTreeNode mbrNode = new DefaultMutableTreeNode("Match between runs");
							mbrNode.add(new DefaultMutableTreeNode(mop.isMatchBetweenRuns()));
							add(mbrNode);

						} else {

							MetaMaxQuantPar mcp = parV2.getCsp();

							DefaultMutableTreeNode pepfdrNode = new DefaultMutableTreeNode("Closed search peptide FDR");
							pepfdrNode.add(new DefaultMutableTreeNode(mcp.getClosedPsmFdr()));
							add(pepfdrNode);

							DefaultMutableTreeNode profdrNode = new DefaultMutableTreeNode("Closed search protein FDR");
							profdrNode.add(new DefaultMutableTreeNode(mcp.getClosedProFdr()));
							add(profdrNode);

							DefaultMutableTreeNode minRatioCountNode = new DefaultMutableTreeNode("Min ratio count");
							minRatioCountNode.add(new DefaultMutableTreeNode(mcp.getMinRatioCount()));
							add(minRatioCountNode);

							DefaultMutableTreeNode lfqMinRatioCountNode = new DefaultMutableTreeNode(
									"LFQ min ratio count");
							lfqMinRatioCountNode.add(new DefaultMutableTreeNode(mcp.getLfqMinRatioCount()));
							add(lfqMinRatioCountNode);

						}

						if (par.isMetaWorkflow()) {

							MetaPep2TaxaPar mptp = parV2.getPtp();
							DefaultMutableTreeNode builtinNode = new DefaultMutableTreeNode(
									"Taxonomy analysis by Built-in database");
							builtinNode.add(new DefaultMutableTreeNode(mptp.isBuildIn()));
							add(builtinNode);

							DefaultMutableTreeNode unipeptNode = new DefaultMutableTreeNode(
									"Taxonomy analysis by Unipept database");
							unipeptNode.add(new DefaultMutableTreeNode(mptp.isUnipept()));
							add(unipeptNode);

							HashSet<RootType> usedRoots = mptp.getUsedRootTypes();
							DefaultMutableTreeNode usedRootsNode = new DefaultMutableTreeNode("Used root nodes");
							for (RootType rt : usedRoots) {
								usedRootsNode.add(new DefaultMutableTreeNode(rt.getName()));
							}
							add(usedRootsNode);

							TaxonomyRanks blank = mptp.getIgnoreBlankRank();
							DefaultMutableTreeNode blankNode = new DefaultMutableTreeNode("Ignore blands below");
							blankNode.add(new DefaultMutableTreeNode(blank.getName()));
							add(blankNode);

							HashSet<String> exclude = mptp.getExcludeTaxa();
							DefaultMutableTreeNode excludeNode = new DefaultMutableTreeNode("Ignore taxa below");
							for (String ex : exclude) {
								excludeNode.add(new DefaultMutableTreeNode(ex));
							}
							add(excludeNode);

						}
					}
				}
			}
		}));

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		scrollPane.setViewportView(tree);
	}
}
