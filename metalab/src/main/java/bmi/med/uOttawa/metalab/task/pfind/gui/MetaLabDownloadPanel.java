package bmi.med.uOttawa.metalab.task.pfind.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import bmi.med.uOttawa.metalab.core.tools.ResumableDownloadTask;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.border.BevelBorder;

public class MetaLabDownloadPanel extends JPanel implements ActionListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5692895723246668356L;

	private JButton btnDownload;
	private JTextField textField;
	private JProgressBar progressBar;

	private String description;
	private String downloadLink;

	/**
	 * Create the panel.
	 */
	public MetaLabDownloadPanel(String description, String downloadLink) {
		setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setLayout(new MigLayout("", "[50][15][grow][15][50]", "[30][30][30]"));

		JLabel lblTitle = new JLabel(description);
		add(lblTitle, "cell 0 0 3 1,alignx left,aligny center");

		JLabel lblPath = new JLabel("Saved path");
		add(lblPath, "cell 0 1,alignx left,aligny center");

		textField = new JTextField();
		add(textField, "cell 2 1,growx");
		textField.setColumns(10);

		JButton btnBrowse = new JButton("Browse");
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

			int returnValue = fileChooser.showOpenDialog(MetaLabDownloadPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();

				try {
					URL url = new URL(downloadLink);
					String fileName = FilenameUtils.getName(url.getPath());
					textField.setText(file.getAbsolutePath() + "\\" + fileName);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		add(btnBrowse, "cell 4 1");

		progressBar = new JProgressBar();
		add(progressBar, "cell 2 2 3 1,growx");

		btnDownload = new JButton("Download");
		btnDownload.setActionCommand("Start");
		btnDownload.addActionListener(this);
		add(btnDownload, "cell 0 2,alignx left");

		this.downloadLink = downloadLink;
		this.description = description;
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

		ResumableDownloadTask task;

		if (this.description.startsWith("Human")) {
			task = new ResumableDownloadTask(downloadLink, textField.getText(), MetaConstants.humanGutProDbLength) {
				/*
				 * Executed in event dispatching thread
				 */
				@Override
				public void done() {
					btnDownload.setEnabled(true);
					setCursor(null);
				}
			};
		} else if (this.description.startsWith("Mouse")) {
			task = new ResumableDownloadTask(downloadLink, textField.getText(), MetaConstants.mouseGutProDbLength) {
				/*
				 * Executed in event dispatching thread
				 */
				@Override
				public void done() {
					btnDownload.setEnabled(true);
					setCursor(null);
				}
			};
		} else if (this.description.startsWith("Taxonomy")) {
			task = new ResumableDownloadTask(downloadLink, textField.getText(), MetaConstants.pep2TaxDbLength) {
				/*
				 * Executed in event dispatching thread
				 */
				@Override
				public void done() {
					btnDownload.setEnabled(true);
					setCursor(null);
				}
			};
		} else if (this.description.startsWith("Functional")) {
			task = new ResumableDownloadTask(downloadLink, textField.getText(), MetaConstants.pro2FuncDbLength) {
				/*
				 * Executed in event dispatching thread
				 */
				@Override
				public void done() {
					btnDownload.setEnabled(true);
					setCursor(null);
				}
			};
		} else {
			
			
			
			return;
		}

		task.addPropertyChangeListener(this);
		task.execute();
	}

}
