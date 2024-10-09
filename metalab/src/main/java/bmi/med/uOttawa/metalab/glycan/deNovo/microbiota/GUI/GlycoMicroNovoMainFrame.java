package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.jfree.ui.ExtensionFileFilter;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class GlycoMicroNovoMainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2575229338977445238L;
	
	private JFileChooser inputChooser;

	private GlycoMicroNovoTaskPanel glycoMicroNovoTaskPanel;

	private GlycoMicroNovoViewPanel glycoMicroNovoViewPanel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GlycoMicroNovoMainFrame frame = new GlycoMicroNovoMainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public GlycoMicroNovoMainFrame() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.glycoMicroNovoTaskPanel = new GlycoMicroNovoTaskPanel();
		this.glycoMicroNovoViewPanel = new GlycoMicroNovoViewPanel();
		
		setExtendedState(MAXIMIZED_BOTH);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(glycoMicroNovoTaskPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(glycoMicroNovoViewPanel, GroupLayout.DEFAULT_SIZE, 1383, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(glycoMicroNovoViewPanel, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
						.addComponent(glycoMicroNovoTaskPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 984, Short.MAX_VALUE))
					.addContainerGap(19, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu FileMenu = new JMenu("File");
		menuBar.add(FileMenu);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inputChooser = new JFileChooser();
				inputChooser.setFileFilter(new ExtensionFileFilter("Glycopeptide identification result (.txt)", "txt"));
				if (inputChooser.showOpenDialog(GlycoMicroNovoMainFrame.this) == JFileChooser.APPROVE_OPTION) {
					File file = inputChooser.getSelectedFile();
					try {
						GlycoMicroNovoMainFrame.this.glycoMicroNovoViewPanel.load(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.getJButtonClose().setEnabled(true);
			}
		});
		FileMenu.add(mntmOpen);

		this.glycoMicroNovoTaskPanel.getJButtonLoad().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inputChooser = new JFileChooser();
				inputChooser.setFileFilter(new ExtensionFileFilter("Glycopeptide identification result (.txt)", "txt"));
				if (inputChooser.showOpenDialog(GlycoMicroNovoMainFrame.this) == JFileChooser.APPROVE_OPTION) {
					File file = inputChooser.getSelectedFile();
					try {
						GlycoMicroNovoMainFrame.this.glycoMicroNovoViewPanel.load(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.getJButtonClose().setEnabled(true);
				GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.getJButtonExport().setEnabled(true);
			}
		});
		
		this.glycoMicroNovoTaskPanel.getJButtonExport().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser outputChooser = new JFileChooser();
				outputChooser.setFileFilter(new ExtensionFileFilter("Identification result (.xlsx)", "xlsx"));
				if (outputChooser.showOpenDialog(GlycoMicroNovoMainFrame.this) == JFileChooser.APPROVE_OPTION) {
					String file = outputChooser.getSelectedFile().getAbsolutePath();
					if (!file.endsWith("xlsx")) {
						file += ".xlsx";
					}
					try {
						GlycoMicroNovoMainFrame.this.glycoMicroNovoViewPanel.export(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(GlycoMicroNovoMainFrame.this,
							"Result has been exported to " + file + ".", "Message", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		
		this.glycoMicroNovoTaskPanel.getJButtonClose().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GlycoMicroNovoMainFrame.this.glycoMicroNovoViewPanel.closeFile();
				GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.getJButtonExport().setEnabled(false);
				GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.getJButtonClose().setEnabled(false);
			}
		});
		
		JMenuItem mntmExport = new JMenuItem("Export");
		FileMenu.add(mntmExport);
		
		JMenu dbMenu = new JMenu("Monosaccharides");
		menuBar.add(dbMenu);
		
		JMenuItem mntmView = new JMenuItem("View");
		dbMenu.add(mntmView);
		mntmView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GlycosylViewDialog frame = new GlycosylViewDialog();
				frame.getContentPane().setPreferredSize(frame.getSize());
				frame.pack();
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setLocationRelativeTo(GlycoMicroNovoMainFrame.this);
				frame.setVisible(true);

				frame.getCloseButton().addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						frame.dispose();
						try {
							GlycoMicroNovoMainFrame.this.glycoMicroNovoTaskPanel.refresh();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
			}
		});
	}
}
