/**
 * 
 */
package bmi.med.uOttawa.metalab.task.gui;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import java.awt.Component;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabDBPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3504492781670189732L;
	protected JTextField textFieldMicroDb;
	protected JTextField textFieldHostDB;
	protected JCheckBox chckbxCombine;

	/**
	 * Create the panel.
	 */
	public MetaLabDBPanel() {
		textFieldMicroDb = new JTextField();
		textFieldMicroDb.setColumns(10);

		textFieldHostDB = new JTextField();
		textFieldHostDB.setColumns(10);

		chckbxCombine = new JCheckBox("Append host database to the generated sample-specific database");
		chckbxCombine.setBackground(new Color(255, 208, 199));
	}
	
	/**
	 * Create the panel.
	 */
	public MetaLabDBPanel(MetaParameter par) {
		setBackground(new Color(255, 208, 199));

		setLayout(new MigLayout("", "[120][20][590:1390:1710,grow][20][50]", "[30][30][30]"));

		textFieldMicroDb = new JTextField(par.getMicroDb());
		textFieldMicroDb.setColumns(10);

		String microDatabase = getMicroDb();

		JButton btnMicroDb = new JButton("Browse");
		btnMicroDb.addActionListener(l -> {
			JFileChooser fileChooser;
			if (microDatabase != null) {
				fileChooser = new JFileChooser(new File(microDatabase));
			} else {
				fileChooser = new JFileChooser();
			}

			fileChooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					// TODO Auto-generated method stub

					if (f.isDirectory()) {
						return true;
					}

					if (f.getName().endsWith("fasta") || f.getName().endsWith("faa")) {
						return true;
					}

					return false;
				}

				@Override
				public String getDescription() {
					// TODO Auto-generated method stub
					return "Protein sequence database (.fasta, .faa) or directory";
				}
			});

			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			int returnValue = fileChooser.showOpenDialog(MetaLabDBPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File fasta = fileChooser.getSelectedFile();
				textFieldMicroDb.setText(fasta.getAbsolutePath());
				par.setMicroDb(fasta.getAbsolutePath());
			}
		});

		JLabel lblDatabase = new JLabel("Microbiome database");
		add(lblDatabase, "cell 0 0,grow");
		add(textFieldMicroDb, "cell 2 0,growx");
		add(btnMicroDb, "cell 4 0,alignx right,growy");

		JLabel lblHostDatabase = new JLabel("Host database");
		add(lblHostDatabase, "cell 0 2");

		textFieldHostDB = new JTextField(par.getHostDB());
		add(textFieldHostDB, "cell 2 2,growx");
		textFieldHostDB.setColumns(10);

		String hostDatabase = this.getHostDB();

		JButton btnHostDb = new JButton("Browse");
		btnHostDb.addActionListener(l -> {
			JFileChooser fileChooser;
			if (hostDatabase != null) {
				fileChooser = new JFileChooser(new File(hostDatabase));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileFilter(new FileNameExtensionFilter("Protein sequence database (.fasta)", "fasta"));

			int returnValue = fileChooser.showOpenDialog(MetaLabDBPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File fasta = fileChooser.getSelectedFile();
				textFieldHostDB.setText(fasta.getAbsolutePath());
				par.setHostDB(fasta.getAbsolutePath());
			}

		});
		add(btnHostDb, "cell 4 2");

		chckbxCombine = new JCheckBox("Append host database to the generated sample-specific database");
		chckbxCombine.setBackground(new Color(255, 208, 199));

		add(chckbxCombine, "cell 0 1 3 1");
		chckbxCombine.addActionListener(l -> {
			if (chckbxCombine.isSelected()) {
				lblHostDatabase.setEnabled(true);
				textFieldHostDB.setEnabled(true);
				btnHostDb.setEnabled(true);
				par.setAppendHostDb(true);
				String dbString = textFieldMicroDb.getText();
				if (dbString.length() > 0) {
					File dbFile = new File(dbString);
					if (dbFile.isDirectory() && dbFile.exists()) {
						File hostFile = new File(dbFile, "uniprot.Homo_sapiens.fasta");
						if (hostFile.exists()) {
							textFieldHostDB.setText(hostFile.getAbsolutePath());
						}
					}
				}
			} else {
				lblHostDatabase.setEnabled(false);
				textFieldHostDB.setText("");
				textFieldHostDB.setEnabled(false);
				btnHostDb.setEnabled(false);
				par.setAppendHostDb(false);
			}
		});
		chckbxCombine.setSelected(par.isAppendHostDb());

		if (par.isAppendHostDb()) {
			lblHostDatabase.setEnabled(true);
			textFieldHostDB.setEnabled(true);
			btnHostDb.setEnabled(true);
		} else {
			lblHostDatabase.setEnabled(false);
			textFieldHostDB.setEnabled(false);
			btnHostDb.setEnabled(false);
		}
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
}
