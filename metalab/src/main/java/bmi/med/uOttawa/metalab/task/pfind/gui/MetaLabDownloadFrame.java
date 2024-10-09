package bmi.med.uOttawa.metalab.task.pfind.gui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;

import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSourcePathDialog;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;

public class MetaLabDownloadFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8164333427577918756L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

					MetaLabDownloadFrame dialog = new MetaLabDownloadFrame();
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
	public MetaLabDownloadFrame() {
		setTitle("Download databases");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabSourcePathDialog.class.getResource("/bmi/med/uOttawa/metalab/icon/metalab_128.png")));
		setBounds(100, 100, 800, 600);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[100][100][grow][grow][][200]"));

		MetaLabDownloadPanel metaLabDownloadPanel = new MetaLabDownloadPanel(MetaConstants.humanGutProDb,
				MetaConstants.humanGutProDbLink);
		getContentPane().add(metaLabDownloadPanel, "cell 0 0,grow");

		MetaLabDownloadPanel metaLabDownloadPanel_1 = new MetaLabDownloadPanel(MetaConstants.mouseGutProDb,
				MetaConstants.mouseGutProDbLink);
		getContentPane().add(metaLabDownloadPanel_1, "cell 0 1,grow");

		MetaLabDownloadPanel metaLabDownloadPanel_2 = new MetaLabDownloadPanel(MetaConstants.pep2TaxDb,
				MetaConstants.pep2TaxDbLink);
		getContentPane().add(metaLabDownloadPanel_2, "cell 0 2,grow");

		MetaLabDownloadPanel metaLabDownloadPanel_3 = new MetaLabDownloadPanel(MetaConstants.pro2FuncDb,
				MetaConstants.pro2FuncDbLink);
		getContentPane().add(metaLabDownloadPanel_3, "cell 0 3,grow");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 5,grow");

		ConsoleTextArea consoleTextArea;
		try {
			consoleTextArea = new ConsoleTextArea();
			scrollPane.setViewportView(consoleTextArea);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
