package bmi.med.uOttawa.metalab.task.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import bmi.med.uOttawa.metalab.task.MetaReportCopyTask;
import net.miginfocom.swing.MigLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.event.ActionEvent;

public class MetaLabTestFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2589523445316249449L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MetaLabTestFrame frame = new MetaLabTestFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MetaLabTestFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][][][]", "[][][][]"));

		JButton btnTestButton = new JButton("Test");
		btnTestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/*
				try {
					MetaReportCopyTask.copyJarFolder(new File("D:\\Exported\\cmd_test\\cmd_test"), "beta 1.0");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
*/
				
				MetaReportCopyTask.copy(new File("D:\\Exported\\cmd_test\\report"), "beta 1.0");
			}
		});
		contentPane.add(btnTestButton, "cell 3 3");
	}

}
