package bmi.med.uOttawa.metalab.task.mag.gui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.task.mag.MagDatabase;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParaIOMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesIoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
import net.miginfocom.swing.MigLayout;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class MagDownloadFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1680936521713439451L;
	private JTextField nameTextField;
	private JTextField versionTextField;
	private JTextField countTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

					File resources = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath(),
							"resources_" + MetaParaIOMag.version + ".json");

					MetaSourcesMag msm = MetaSourcesIoMag.parse(resources);

					MagDownloadFrame dialog = new MagDownloadFrame(null, msm, resources.getAbsolutePath());
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the dialog.
	 */
	public MagDownloadFrame(MetaLabMainFrameMag frame, MetaSourcesMag msm, String msmPath) {
		setTitle("Download database");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MagDownloadFrame.class.getResource("/bmi/med/uOttawa/metalab/icon/mag_128.png")));
		setBounds(100, 100, 800, 500);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[100][100][200]"));

		JPanel magPanel = new JPanel();
		magPanel.setBorder(
				new TitledBorder(null, "Select the MAG database", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(magPanel, "cell 0 0,grow");
		magPanel.setLayout(new MigLayout("", "[80][200:250,grow][:60:80][:40:60][:80:100][:40:60]", "[40][40]"));

		MagDatabase[] magDbs = new MagDatabase[MagDatabase.values().length - 1];
		System.arraycopy(MagDatabase.values(), 0, magDbs, 0, magDbs.length);

		JLabel nameLabel = new JLabel("Catalogue name");
		magPanel.add(nameLabel, "cell 0 1,alignx trailing");

		nameTextField = new JTextField(magDbs[0].getNameString());
		nameTextField.setEditable(false);
		magPanel.add(nameTextField, "cell 1 1,grow");
		nameTextField.setColumns(10);

		JLabel versionLabel = new JLabel("Version");
		magPanel.add(versionLabel, "cell 2 1,alignx trailing");

		versionTextField = new JTextField(magDbs[0].getVersionString());
		versionTextField.setEditable(false);
		magPanel.add(versionTextField, "cell 3 1,grow");
		versionTextField.setColumns(10);

		JLabel countLabel = new JLabel("Genomes count");
		magPanel.add(countLabel, "cell 4 1,alignx trailing");

		countTextField = new JTextField(String.valueOf(magDbs[0].getGenomeCount()));
		countTextField.setEditable(false);
		magPanel.add(countTextField, "cell 5 1,grow");
		countTextField.setColumns(10);

		MagDownloadPanel metaLabDownloadPanel = new MagDownloadPanel(magDbs[0], msm, msmPath, this, frame);
		getContentPane().add(metaLabDownloadPanel, "cell 0 1,grow");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 2,grow");

		ConsoleTextArea consoleTextArea;
		try {
			consoleTextArea = new ConsoleTextArea();
			scrollPane.setViewportView(consoleTextArea);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JComboBox<MagDatabase> comboBox = new JComboBox<MagDatabase>();
		comboBox.addActionListener(l -> {

			MagDatabase magDb = comboBox.getItemAt(comboBox.getSelectedIndex());
			nameTextField.setText(magDb.getNameString());
			versionTextField.setText(magDb.getVersionString());
			countTextField.setText(String.valueOf(magDb.getGenomeCount()));

			metaLabDownloadPanel.setMagDb(magDb);
		});

		for (MagDatabase magDb : magDbs) {
			comboBox.addItem(magDb);
		}
		magPanel.add(comboBox, "cell 0 0 2 1,growx");

	}
}
