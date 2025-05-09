package bmi.med.uOttawa.metalab.task.mag.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import bmi.med.uOttawa.metalab.task.gui.MetaLabDBPanel;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagVersion;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JButton;

public class MetaLabMagDbPanel extends MetaLabDBPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1097144083754906609L;
	private JTextField textFieldMicroDb;
	private JComboBox<MagDbItem> magComboBox;
	private JComboBox<MagVersion> verComboBox;
	private JLabel lblTaxDb;
	private JTextField textFieldTaxDb;
	private JLabel lblFuncDb;
	private JTextField textFieldFuncDb;
	private JLabel lblAnnInfo;
	private JButton btnHostDb;
	private JLabel verLabel;
	private MagDbItem usedMagDbItem;

	public MetaLabMagDbPanel(MetaParameter par) {
		this(par, true);
	}

	public MetaLabMagDbPanel(MetaParameter par, boolean withHost) {
		super();
		if (withHost) {
			setLayout(new MigLayout("",
					"[120][70:90:120][80][70:90:120][20][260:400:720,grow][60:60:60][20:20][150:150:150][10:10][260:400:720,grow][60]",
					"[30][30][30]"));
		} else {
			setLayout(new MigLayout("",
					"[120][70:90:120][80][70:90:120][20][260:400:720,grow][60:60:60][20:20][150:150:150][10:10][260:400:720,grow][60]",
					"[30][30]"));
		}

		JLabel lblDatabase = new JLabel("MAG database");
		add(lblDatabase, "cell 0 0,grow");

		MetaParameterMag magPar = (MetaParameterMag) par;
		MagDbItem[] magDbItems = magPar.getAvailableMagDbItem();
		String magDb = magPar.getMagDb();
		String magDbVersion = magPar.getMagDbVersion();

		magComboBox = new JComboBox<MagDbItem>();
		add(magComboBox, "cell 1 0,growx");
		for (int i = 0; i < magDbItems.length; i++) {
			magComboBox.addItem(magDbItems[i]);
		}

		verLabel = new JLabel("Version");
		add(verLabel, "cell 2 0,alignx trailing");

		verComboBox = new JComboBox<MagVersion>();
		verComboBox.setEditable(false);
		add(verComboBox, "cell 3 0,growx");

		textFieldMicroDb = new JTextField();
		textFieldMicroDb.setEditable(false);
		textFieldMicroDb.setColumns(10);
		add(textFieldMicroDb, "cell 5 0 7 1,growx");

		textFieldTaxDb = new JTextField();
		textFieldTaxDb.setEditable(false);
		add(textFieldTaxDb, "cell 5 1 2 1,growx");
		textFieldTaxDb.setColumns(10);

		textFieldFuncDb = new JTextField();
		textFieldFuncDb.setEditable(false);
		add(textFieldFuncDb, "cell 10 1 2 1,growx");
		textFieldFuncDb.setColumns(10);

		if (magComboBox.getItemCount() > 0) {
			if (magDb != null && magDb.length() > 0) {
				for (int i = 0; i < magComboBox.getItemCount(); i++) {
					MagDbItem magDbItemI = magComboBox.getItemAt(i);
					if (magDbItemI.getCatalogueID().equals(magDb)) {
						magComboBox.setSelectedIndex(i);
						usedMagDbItem = magDbItemI;
						magPar.setUsedMagDbItem(magDbItemI);
					}
				}
			} else {
				magComboBox.setSelectedIndex(0);
				usedMagDbItem = magComboBox.getItemAt(0);
				magPar.setUsedMagDbItem(usedMagDbItem);
			}

			ArrayList<MagVersion> verlist = usedMagDbItem.getVersionList();
			for (MagVersion magver : verlist) {
				if (magver.isLocalAvailable()) {
					verComboBox.addItem(magver);
				}
			}

			if (verComboBox.getItemCount() > 0) {

				for (int i = 0; i < verComboBox.getItemCount(); i++) {
					MagVersion magVersion = verComboBox.getItemAt(i);
					if (magVersion.getCatalogueVersion().equals(magDbVersion)) {
						verComboBox.setSelectedIndex(i);
						usedMagDbItem.setUsedVersion(magVersion);
					}
				}

				MagVersion latestVersion = verComboBox.getItemAt(verComboBox.getSelectedIndex());
				String versionString = latestVersion.getCatalogueVersion();
				File repoFile = usedMagDbItem.getRepositoryFile();
				File currentFile = new File(repoFile, versionString);
				if (currentFile.exists()) {
					File metaFile = new File(currentFile, latestVersion.getTaxName());
					File eggnogFile = new File(currentFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(currentFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(currentFile);
				} else {
					File metaFile = new File(repoFile, latestVersion.getTaxName());
					File eggnogFile = new File(repoFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(repoFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(repoFile);
				}

				File hostDbFile = usedMagDbItem.getHostDbFile();
				if (hostDbFile != null && hostDbFile.exists()) {
					textFieldHostDB.setText(hostDbFile.getAbsolutePath());
				}
			}
			magPar.setUsedMagDbItem(usedMagDbItem);
		}

		magComboBox.addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				usedMagDbItem = magComboBox.getItemAt(magComboBox.getSelectedIndex());
				verComboBox.removeAllItems();
				ArrayList<MagVersion> verlist = usedMagDbItem.getVersionList();
				MagVersion latestVersion = null;
				for (MagVersion magver : verlist) {
					if (magver.isLocalAvailable()) {
						verComboBox.addItem(magver);
						latestVersion = magver;
					}
				}
				if (latestVersion != null) {
					verComboBox.setSelectedItem(latestVersion);
					usedMagDbItem.setUsedVersion(latestVersion);
					String versionString = latestVersion.getCatalogueVersion();
					File repoFile = usedMagDbItem.getRepositoryFile();
					File currentFile = new File(repoFile, versionString);
					if (currentFile.exists()) {
						File metaFile = new File(currentFile, latestVersion.getTaxName());
						File eggnogFile = new File(currentFile, latestVersion.getFuncName());

						textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
						textFieldMicroDb.setText(currentFile.getAbsolutePath());
						textFieldTaxDb.setText(metaFile.getAbsolutePath());

						usedMagDbItem.setCurrentFile(currentFile);
					} else {
						File metaFile = new File(repoFile, latestVersion.getTaxName());
						File eggnogFile = new File(repoFile, latestVersion.getFuncName());

						textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
						textFieldMicroDb.setText(repoFile.getAbsolutePath());
						textFieldTaxDb.setText(metaFile.getAbsolutePath());

						usedMagDbItem.setCurrentFile(repoFile);
					}

					File hostDbFile = usedMagDbItem.getHostDbFile();
					if (hostDbFile != null && hostDbFile.exists()) {
						textFieldHostDB.setText(hostDbFile.getAbsolutePath());
					}
					magPar.setUsedMagDbItem(usedMagDbItem);
				}
			}
		});

		verComboBox.addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				MagVersion latestVersion = verComboBox.getItemAt(verComboBox.getSelectedIndex());
				usedMagDbItem.setUsedVersion(latestVersion);
				String versionString = latestVersion.getCatalogueVersion();
				File repoFile = usedMagDbItem.getRepositoryFile();
				File currentFile = new File(repoFile, versionString);
				if (currentFile.exists()) {
					File metaFile = new File(currentFile, latestVersion.getTaxName());
					File eggnogFile = new File(currentFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(currentFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(currentFile);
				} else {
					File metaFile = new File(repoFile, latestVersion.getTaxName());
					File eggnogFile = new File(repoFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(repoFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(repoFile);
				}

				File hostDbFile = usedMagDbItem.getHostDbFile();
				if (hostDbFile != null && hostDbFile.exists()) {
					textFieldHostDB.setText(hostDbFile.getAbsolutePath());
				}
				((MetaParameterMag) par).setUsedMagDbItem(usedMagDbItem);
			}
		});

		lblAnnInfo = new JLabel("Annotation information");
		add(lblAnnInfo, "cell 0 1");

		lblTaxDb = new JLabel("Genome Taxonomy Database");
		add(lblTaxDb, "cell 2 1 2 1,alignx right,growy");

		lblFuncDb = new JLabel("Functional annotation database");
		add(lblFuncDb, "cell 8 1");

		btnHostDb = new JButton("Browse");

		if (withHost) {

			add(textFieldHostDB, "cell 5 2 6 1,growx");
			textFieldHostDB.setColumns(10);
			if (par.isAppendHostDb()) {
				textFieldHostDB.setText(par.getHostDB());
				textFieldHostDB.setEditable(true);
			} else {
				textFieldHostDB.setEditable(false);
			}

			add(btnHostDb, "cell 11 2");
			btnHostDb.addActionListener(l -> {
				JFileChooser fileChooser = new JFileChooser();

				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return "Protein sequence database (.fasta or .faa)";
					}

					@Override
					public boolean accept(File f) {
						// TODO Auto-generated method stub
						if (f.isDirectory()) {
							// Allow directories to be visible
							return true;
						}
						if (f.getName().endsWith(".fasta") || f.getName().endsWith(".faa"))
							return true;
						return false;
					}
				});

				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectFile = fileChooser.getSelectedFile();
					String selectedPath = selectFile.getAbsolutePath();
					textFieldHostDB.setText(selectedPath);
				}
			});
			btnHostDb.setEnabled(par.isAppendHostDb());

			chckbxCombine.setBackground(new Color(255, 208, 199));

			add(chckbxCombine, "cell 0 2 4 1");
			chckbxCombine.addActionListener(l -> {
				if (chckbxCombine.isSelected()) {
					textFieldHostDB.setEnabled(true);
					btnHostDb.setEnabled(true);
					par.setAppendHostDb(true);
				} else {
					textFieldHostDB.setText("");
					textFieldHostDB.setEnabled(false);
					btnHostDb.setEnabled(false);
					par.setAppendHostDb(false);
				}
			});
			chckbxCombine.setSelected(par.isAppendHostDb());
		}
	}

	public MetaLabMagDbPanel(MetaParameter par, boolean withHost, ItemListener itemListener) {
		super();
		if (withHost) {
			setLayout(new MigLayout("",
					"[120][70:90:120][80][70:90:120][20][260:400:720,grow][60:60:60][20:20][150:150:150][10:10][260:400:720,grow][60]",
					"[30][30][30]"));
		} else {
			setLayout(new MigLayout("",
					"[120][70:90:120][80][70:90:120][20][260:400:720,grow][60:60:60][20:20][150:150:150][10:10][260:400:720,grow][60]",
					"[30][30]"));
		}

		JLabel lblDatabase = new JLabel("MAG database");
		add(lblDatabase, "cell 0 0,grow");

		MetaParameterMag magPar = (MetaParameterMag) par;
		MagDbItem[] magDbItems = magPar.getAvailableMagDbItem();
		String magDb = magPar.getMagDb();
		String magDbVersion = magPar.getMagDbVersion();

		magComboBox = new JComboBox<MagDbItem>();
		add(magComboBox, "cell 1 0,growx");
		for (int i = 0; i < magDbItems.length; i++) {
			magComboBox.addItem(magDbItems[i]);
		}

		verLabel = new JLabel("Version");
		add(verLabel, "cell 2 0,alignx trailing");

		verComboBox = new JComboBox<MagVersion>();
		verComboBox.setEditable(false);
		add(verComboBox, "cell 3 0,growx");

		textFieldMicroDb = new JTextField();
		textFieldMicroDb.setEditable(false);
		textFieldMicroDb.setColumns(10);
		add(textFieldMicroDb, "cell 5 0 7 1,growx");

		textFieldTaxDb = new JTextField();
		textFieldTaxDb.setEditable(false);
		add(textFieldTaxDb, "cell 5 1 2 1,growx");
		textFieldTaxDb.setColumns(10);

		textFieldFuncDb = new JTextField();
		textFieldFuncDb.setEditable(false);
		add(textFieldFuncDb, "cell 10 1 2 1,growx");
		textFieldFuncDb.setColumns(10);

		if (magComboBox.getItemCount() > 0) {
			if (magDb != null && magDb.length() > 0) {
				for (int i = 0; i < magComboBox.getItemCount(); i++) {
					MagDbItem magDbItemI = magComboBox.getItemAt(i);
					if (magDbItemI.getCatalogueID().equals(magDb)) {
						magComboBox.setSelectedIndex(i);
						usedMagDbItem = magDbItemI;
						magPar.setUsedMagDbItem(magDbItemI);
					}
				}
			} else {
				magComboBox.setSelectedIndex(0);
				usedMagDbItem = magComboBox.getItemAt(0);
				magPar.setUsedMagDbItem(usedMagDbItem);
			}

			ArrayList<MagVersion> verlist = usedMagDbItem.getVersionList();
			for (MagVersion magver : verlist) {
				if (magver.isLocalAvailable()) {
					verComboBox.addItem(magver);
				}
			}

			if (verComboBox.getItemCount() > 0) {

				for (int i = 0; i < verComboBox.getItemCount(); i++) {
					MagVersion magVersion = verComboBox.getItemAt(i);
					if (magVersion.getCatalogueVersion().equals(magDbVersion)) {
						verComboBox.setSelectedIndex(i);
						usedMagDbItem.setUsedVersion(magVersion);
					}
				}

				MagVersion latestVersion = verComboBox.getItemAt(verComboBox.getSelectedIndex());
				String versionString = latestVersion.getCatalogueVersion();
				File repoFile = usedMagDbItem.getRepositoryFile();
				File currentFile = new File(repoFile, versionString);
				if (currentFile.exists()) {
					File metaFile = new File(currentFile, latestVersion.getTaxName());
					File eggnogFile = new File(currentFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(currentFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(currentFile);
				} else {
					File metaFile = new File(repoFile, latestVersion.getTaxName());
					File eggnogFile = new File(repoFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(repoFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(repoFile);
				}

				File hostDbFile = usedMagDbItem.getHostDbFile();
				if (hostDbFile != null && hostDbFile.exists()) {
					textFieldHostDB.setText(hostDbFile.getAbsolutePath());
				}
			}
			magPar.setUsedMagDbItem(usedMagDbItem);
		}

		magComboBox.addItemListener(itemListener);
		magComboBox.addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				usedMagDbItem = magComboBox.getItemAt(magComboBox.getSelectedIndex());
				verComboBox.removeAllItems();
				ArrayList<MagVersion> verlist = usedMagDbItem.getVersionList();
				MagVersion latestVersion = null;
				for (MagVersion magver : verlist) {
					if (magver.isLocalAvailable()) {
						verComboBox.addItem(magver);
						latestVersion = magver;
					}
				}
				if (latestVersion != null) {

					verComboBox.setSelectedItem(latestVersion);
					usedMagDbItem.setUsedVersion(latestVersion);
					String versionString = latestVersion.getCatalogueVersion();
					File repoFile = usedMagDbItem.getRepositoryFile();
					File currentFile = new File(repoFile, versionString);
					if (currentFile.exists()) {
						File metaFile = new File(currentFile, latestVersion.getTaxName());
						File eggnogFile = new File(currentFile, latestVersion.getFuncName());

						textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
						textFieldMicroDb.setText(currentFile.getAbsolutePath());
						textFieldTaxDb.setText(metaFile.getAbsolutePath());

						usedMagDbItem.setCurrentFile(currentFile);
					} else {
						File metaFile = new File(repoFile, latestVersion.getTaxName());
						File eggnogFile = new File(repoFile, latestVersion.getFuncName());

						textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
						textFieldMicroDb.setText(repoFile.getAbsolutePath());
						textFieldTaxDb.setText(metaFile.getAbsolutePath());

						usedMagDbItem.setCurrentFile(repoFile);
					}

					File hostDbFile = usedMagDbItem.getHostDbFile();
					if (hostDbFile != null && hostDbFile.exists()) {
						textFieldHostDB.setText(hostDbFile.getAbsolutePath());
					}
					magPar.setUsedMagDbItem(usedMagDbItem);
				}
			}
		});
		verComboBox.addItemListener(itemListener);
		verComboBox.addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				MagVersion latestVersion = verComboBox.getItemAt(verComboBox.getSelectedIndex());
				usedMagDbItem.setUsedVersion(latestVersion);
				String versionString = latestVersion.getCatalogueVersion();
				File repoFile = usedMagDbItem.getRepositoryFile();
				File currentFile = new File(repoFile, versionString);
				if (currentFile.exists()) {
					File metaFile = new File(currentFile, latestVersion.getTaxName());
					File eggnogFile = new File(currentFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(currentFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(currentFile);
				} else {
					File metaFile = new File(repoFile, latestVersion.getTaxName());
					File eggnogFile = new File(repoFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(repoFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(repoFile);
				}

				File hostDbFile = usedMagDbItem.getHostDbFile();
				if (hostDbFile != null && hostDbFile.exists()) {
					textFieldHostDB.setText(hostDbFile.getAbsolutePath());
				}
				((MetaParameterMag) par).setUsedMagDbItem(usedMagDbItem);
			}
		});

		lblAnnInfo = new JLabel("Annotation information");
		add(lblAnnInfo, "cell 0 1");

		lblTaxDb = new JLabel("Genome Taxonomy Database");
		add(lblTaxDb, "cell 2 1 2 1,alignx right,growy");

		lblFuncDb = new JLabel("Functional annotation database");
		add(lblFuncDb, "cell 8 1");

		btnHostDb = new JButton("Browse");

		if (withHost) {
			textFieldHostDB.setEditable(false);
			add(textFieldHostDB, "cell 5 2 6 1,growx");
			textFieldHostDB.setColumns(10);

			add(btnHostDb, "cell 11 2");
			btnHostDb.addActionListener(l -> {
				JFileChooser fileChooser = new JFileChooser();

				fileChooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return "Protein sequence database (.fasta or .faa)";
					}

					@Override
					public boolean accept(File f) {
						// TODO Auto-generated method stub
						if (f.isDirectory()) {
							// Allow directories to be visible
							return true;
						}
						if (f.getName().endsWith(".fasta") || f.getName().endsWith(".faa"))
							return true;
						return false;
					}
				});

				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectFile = fileChooser.getSelectedFile();
					String selectedPath = selectFile.getAbsolutePath();
					textFieldHostDB.setText(selectedPath);
				}
			});
			btnHostDb.setEnabled(par.isAppendHostDb());

			chckbxCombine.setBackground(new Color(255, 208, 199));

			add(chckbxCombine, "cell 0 2 4 1");
			chckbxCombine.addActionListener(l -> {
				if (chckbxCombine.isSelected()) {
					textFieldHostDB.setEnabled(true);
					btnHostDb.setEnabled(true);
					par.setAppendHostDb(true);
				} else {
					textFieldHostDB.setText("");
					textFieldHostDB.setEnabled(false);
					btnHostDb.setEnabled(false);
					par.setAppendHostDb(false);
				}
			});
			chckbxCombine.setSelected(par.isAppendHostDb());
		}
	}

	public void update(MetaParameter par) {
		MagDbItem[] magDbItems = ((MetaParameterMag) par).getAvailableMagDbItem();
		usedMagDbItem = ((MetaParameterMag) par).getUsedMagDbItem();

		magComboBox.removeAllItems();
		for (int i = 0; i < magDbItems.length; i++) {
			magComboBox.addItem(magDbItems[i]);
		}

		if (magComboBox.getItemCount() > 0) {
			if (usedMagDbItem != null) {
				for (int i = 0; i < magComboBox.getItemCount(); i++) {
					MagDbItem magDbItemI = magComboBox.getItemAt(i);
					if (magDbItemI.getCatalogueID().equals(usedMagDbItem.getCatalogueID())) {
						magComboBox.setSelectedIndex(i);
					}
				}
			} else {
				magComboBox.setSelectedIndex(0);
				usedMagDbItem = magComboBox.getItemAt(0);
			}

			ArrayList<MagVersion> verlist = usedMagDbItem.getVersionList();
			for (MagVersion magver : verlist) {
				if (magver.isLocalAvailable()) {
					verComboBox.addItem(magver);
				}
			}

			if (verComboBox.getItemCount() > 0) {
				verComboBox.setSelectedIndex(verComboBox.getItemCount() - 1);
				MagVersion latestVersion = verComboBox.getItemAt(verComboBox.getSelectedIndex());
				String versionString = latestVersion.getCatalogueVersion();
				File repoFile = usedMagDbItem.getRepositoryFile();
				File currentFile = new File(repoFile, versionString);
				if (currentFile.exists()) {
					File metaFile = new File(currentFile, latestVersion.getTaxName());
					File eggnogFile = new File(currentFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(currentFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(currentFile);
				} else {
					File metaFile = new File(repoFile, latestVersion.getTaxName());
					File eggnogFile = new File(repoFile, latestVersion.getFuncName());

					textFieldFuncDb.setText(eggnogFile.getAbsolutePath());
					textFieldMicroDb.setText(repoFile.getAbsolutePath());
					textFieldTaxDb.setText(metaFile.getAbsolutePath());

					usedMagDbItem.setCurrentFile(repoFile);
				}

				File hostDbFile = usedMagDbItem.getHostDbFile();
				if (hostDbFile != null && hostDbFile.exists()) {
					textFieldHostDB.setText(hostDbFile.getAbsolutePath());
				}
			}
		}

		btnHostDb.setEnabled(par.isAppendHostDb());
		chckbxCombine.setSelected(par.isAppendHostDb());
	}

	public String getMicroDb() {
		return textFieldMicroDb.getText();
	}

	public boolean isAppendHostDb() {
		return chckbxCombine.isSelected();
	}

	public String getHostDB() {
		return textFieldHostDB.getText();
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		Component[] comps = getComponents();
		for (Component c : comps) {
			c.setEnabled(enabled);
		}
	}

	public JComboBox<MagDbItem> getMagComboBox() {
		return magComboBox;
	}

	public JComboBox<MagVersion> getVerComboBox() {
		return verComboBox;
	}

	public MagDbItem getUsedMagDbItem() {
		return usedMagDbItem;
	}
}
