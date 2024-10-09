package bmi.med.uOttawa.metalab.task.hgm.gui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSourcePathDialog;
import net.miginfocom.swing.MigLayout;

public class HGMDownloadFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1680936521713439451L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

					HGMDownloadFrame dialog = new HGMDownloadFrame();
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
	public HGMDownloadFrame() {
		setTitle("Download database");
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabSourcePathDialog.class.getResource("/bmi/med/uOttawa/metalab/icon/HGM.png")));
		setBounds(100, 100, 800, 400);
		getContentPane().setLayout(new MigLayout("", "[grow]", "[100][200]"));

		HGMDownloadPanel metaLabDownloadPanel = new HGMDownloadPanel("Unified Human Gastrointestinal Genome (UHGG) v2.0");
		getContentPane().add(metaLabDownloadPanel, "cell 0 0,grow");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 1,grow");

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
