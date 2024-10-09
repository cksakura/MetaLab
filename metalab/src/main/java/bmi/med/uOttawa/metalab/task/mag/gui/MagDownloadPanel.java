package bmi.med.uOttawa.metalab.task.mag.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import bmi.med.uOttawa.metalab.task.dia.gui.MetaLabMainFrameDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesIoDia;
import bmi.med.uOttawa.metalab.task.hgm.gui.HGMDownloadPanel;
import bmi.med.uOttawa.metalab.task.mag.MagDatabase;
import bmi.med.uOttawa.metalab.task.mag.MagDownloadTask;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesIoMag;
import bmi.med.uOttawa.metalab.task.mag.par.MetaSourcesMag;
@Deprecated
public class MagDownloadPanel extends HGMDownloadPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6976147450640594882L;

	private MagDatabase magdb;
	private MetaSourcesMag msm;
	MagDownloadFrame downloadFrame;
	private MetaLabMainFrameMag mainframe;
	private String msmPath;

	public MagDownloadPanel(MagDatabase magdb, MetaSourcesMag msm, String msmPath, MagDownloadFrame downloadFrame,
			MetaLabMainFrameMag mainFrame) {
		super(magdb.getIdString());
		// TODO Auto-generated constructor stub

		this.magdb = magdb;
		this.msm = msm;
		this.downloadFrame = downloadFrame;
		this.mainframe = mainFrame;
		this.msmPath = msmPath;
	}

	public void setMagDb(MagDatabase magdb) {
		this.magdb = magdb;
		this.lblTitle.setText(magdb.getIdString());
		String text = textField.getText();
		if (text.length() > 0) {
			File file = (new File(text)).getParentFile();
			if (file.exists()) {
				textField.setText((new File(file, magdb.name())).getAbsolutePath());
			}
		}
	}

	protected void setBrowseButton() {
		btnBrowse.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.addChoosableFileFilter(new FileFilter() {

				public String getDescription() {
					return "Save the database file in this folder";
				}

				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					} else {
						return false;
					}
				}
			});

			int returnValue = fileChooser.showOpenDialog(MagDownloadPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File file = new File(fileChooser.getSelectedFile(), magdb.name());
				textField.setText(file.getAbsolutePath());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		progressBar.setStringPainted(true);
		btnDownload.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		MagDownloadTask task = new MagDownloadTask(magdb, textField.getText()) {
			@Override
			protected void done() {
				// TODO Auto-generated method stub

				String info = "";
				try {
					info = get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				boolean finish = isFinish();

				if (finish) {


					JOptionPane optionPane = new JOptionPane();
					JDialog dialog = optionPane.createDialog(downloadFrame, "Message");
					dialog.setSize(300, 150);

					optionPane.setMessage(
							"<html><body><p>Database download has finished, and MetaLab will restart to load the database.</p></body></html>");
					optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);

					JButton jButton = getButton(optionPane, dialog, "OK");
					optionPane.setOptions(new Object[] { jButton });

					dialog.setVisible(true);

				} else {
					JOptionPane.showMessageDialog(MagDownloadPanel.this, info, "Warning", JOptionPane.WARNING_MESSAGE);
				}

				btnDownload.setEnabled(true);
				setCursor(Cursor.getDefaultCursor());
			}
		};
		task.addPropertyChangeListener(this);
		task.execute();
	}

	public JButton getButton(JOptionPane optionPane, JDialog dialog, String text) {
		final JButton button = new JButton(text);
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				// Return current text label, instead of argument to method

				dialog.dispose();

				if (downloadFrame != null && mainframe != null) {

					downloadFrame.dispose();

					String version = msm.getVersion();

					if (version.startsWith("MAG")) {
						MetaSourcesIoMag.export(msm, msmPath);
					} else if (version.startsWith("DIA")) {
						MetaSourcesIoDia.export((MetaSourcesDia) msm, msmPath);
					}

					mainframe.dispose();

					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {

								if (version.startsWith("MAG")) {
									MetaLabMainFrameMag frame = new MetaLabMainFrameMag();
									frame.setVisible(true);
								} else if (version.startsWith("DIA")) {
									MetaLabMainFrameDia frame = new MetaLabMainFrameDia();
									frame.setVisible(true);
								}

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		};
		button.addActionListener(actionListener);
		return button;
	}
}
