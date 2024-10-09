package bmi.med.uOttawa.metalab.task.hgm.gui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import bmi.med.uOttawa.metalab.task.hgm.UHGGDownloadTask;
import net.miginfocom.swing.MigLayout;

public class HGMDownloadPanel extends JPanel implements ActionListener, PropertyChangeListener {

	protected JButton btnDownload;
	protected JTextField textField;
	protected JProgressBar progressBar;
	protected JLabel lblTitle;
	protected JButton btnBrowse;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3977845946887012637L;

	public HGMDownloadPanel(String description) {

		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[50][15][grow][15][50]", "[30][30][30]"));

		lblTitle = new JLabel(description);
		add(lblTitle, "cell 0 0 3 1,alignx left,aligny center");

		JLabel lblPath = new JLabel("Saved path");
		add(lblPath, "cell 0 1,alignx left,aligny center");

		textField = new JTextField();
		add(textField, "cell 2 1,growx");
		textField.setColumns(10);

		this.btnBrowse = new JButton("Browse");
		add(btnBrowse, "cell 4 1");
		this.setBrowseButton();

		progressBar = new JProgressBar();
		add(progressBar, "cell 2 2 3 1,growx");

		btnDownload = new JButton("Download");
		btnDownload.setActionCommand("Start");
		btnDownload.addActionListener(this);
		add(btnDownload, "cell 0 2,alignx left");
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

			int returnValue = fileChooser.showOpenDialog(HGMDownloadPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();

				textField.setText(file.getAbsolutePath());

			}
		});
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		progressBar.setStringPainted(true);
		btnDownload.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		UHGGDownloadTask task = new UHGGDownloadTask(textField.getText()) {
			@Override
			protected void done() {
				// TODO Auto-generated method stub
				btnDownload.setEnabled(true);
				setCursor(Cursor.getDefaultCursor());

				try {
					String info = get();
					boolean finish = isFinish();
					if (finish) {
						JOptionPane.showMessageDialog(HGMDownloadPanel.this, "Database download finished");
					} else {
						JOptionPane.showMessageDialog(HGMDownloadPanel.this, info, "Warning",
								JOptionPane.WARNING_MESSAGE);
					}

				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		task.addPropertyChangeListener(this);
		task.execute();
	}
}
