/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;

import org.expasy.mzjava.proteomics.io.mol.FastaProteinReader;
import org.expasy.mzjava.proteomics.mol.Protein;
import org.expasy.mzjava.proteomics.mol.digest.Protease;
import org.expasy.mzjava.proteomics.mol.digest.ProteinDigester;

import bmi.med.uOttawa.metalab.core.prodb.Pep2GiDbCallable;
import bmi.med.uOttawa.metalab.core.tools.FileHandler;

import javax.swing.JComboBox;

/**
 * @author Kai Cheng
 *
 */
public class DBConstructorDialog extends JDialog implements ActionListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2798031452328334051L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldFasta;
	private JTextField textFieldDb;
	private JProgressBar progressBar;
	private File dbfile;
	private JComboBox<Protease> comboBoxEnzyme;
	private JComboBox<Integer> comboBoxMiss;

	private Pep2GiDbCreateTask task;
	private JButton btnStart;
	private JButton btnClose;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DBConstructorDialog dialog = new DBConstructorDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DBConstructorDialog() {
		setBounds(100, 100, 600, 280);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[80,grow][130,grow][130][130][80,grow][20]",
				"[30][15][30][10][30][10][20][10][30]"));
		{
			JLabel lblProteinDatabase = new JLabel("Protein database");
			contentPanel.add(lblProteinDatabase, "cell 0 0,alignx trailing");
		}
		{
			textFieldFasta = new JTextField();
			contentPanel.add(textFieldFasta, "cell 1 0 3 1,growx");
			textFieldFasta.setColumns(10);
		}
		{
			JButton prodbBtnBrowse = new JButton("Browse");
			prodbBtnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser
							.setFileFilter(new FileNameExtensionFilter("Protein sequence database (*.fasta)", "fasta"));
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnValue = fileChooser.showOpenDialog(DBConstructorDialog.this);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						textFieldFasta.setText(file.getAbsolutePath());
						dbfile = file;
					}
				}
			});
			contentPanel.add(prodbBtnBrowse, "cell 4 0,growx");
		}
		{
			JLabel lblNotice = new JLabel("Notice: make sure enough disk space");
			lblNotice.setForeground(Color.GRAY);
			contentPanel.add(lblNotice, "cell 1 1 3 1");
		}
		{
			JLabel lblBuildinDatabase = new JLabel("Output directory");
			contentPanel.add(lblBuildinDatabase, "cell 0 2,alignx trailing");
		}
		{
			textFieldDb = new JTextField();
			contentPanel.add(textFieldDb, "cell 1 2 3 1,growx");
			textFieldDb.setColumns(10);
		}
		{
			JButton pep2GiDbBtnBrowse = new JButton("Browse");
			pep2GiDbBtnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser(dbfile);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnValue = fileChooser.showSaveDialog(DBConstructorDialog.this);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						textFieldDb.setText(file.getAbsolutePath());
					}
				}
			});
			contentPanel.add(pep2GiDbBtnBrowse, "cell 4 2,growx");
		}
		{
			JLabel lblEnzyme = new JLabel("Enzyme");
			contentPanel.add(lblEnzyme, "cell 0 4,alignx left");
		}
		{
			this.comboBoxEnzyme = new JComboBox<Protease>();
			comboBoxEnzyme.addItem(Protease.TRYPSIN);
			comboBoxEnzyme.addItem(Protease.CASPASE_1);
			comboBoxEnzyme.addItem(Protease.CASPASE_2);
			comboBoxEnzyme.addItem(Protease.CASPASE_3);
			comboBoxEnzyme.addItem(Protease.CASPASE_4);
			comboBoxEnzyme.addItem(Protease.CASPASE_5);
			comboBoxEnzyme.addItem(Protease.CASPASE_6);
			comboBoxEnzyme.addItem(Protease.CASPASE_7);
			comboBoxEnzyme.addItem(Protease.CASPASE_8);
			comboBoxEnzyme.addItem(Protease.CASPASE_9);
			comboBoxEnzyme.addItem(Protease.CASPASE_10);
			comboBoxEnzyme.addItem(Protease.CHYMOTRYPSIN_FYLW);
			comboBoxEnzyme.addItem(Protease.CHYMOTRYPSIN_FYL);
			comboBoxEnzyme.addItem(Protease.CHYMOTRYPSIN_HIGH_SPEC);
			comboBoxEnzyme.addItem(Protease.CHYMOTRYPSIN_LOW_SPEC);
			comboBoxEnzyme.addItem(Protease.ENTEROKINASE);
			comboBoxEnzyme.addItem(Protease.LYS_C);
			comboBoxEnzyme.addItem(Protease.ARG_C);
			comboBoxEnzyme.addItem(Protease.CNBR);
			comboBoxEnzyme.addItem(Protease.ASP_N);
			comboBoxEnzyme.addItem(Protease.BNPS_SKATOLE);
			comboBoxEnzyme.addItem(Protease.GLU_C_BICARBONATE);
			comboBoxEnzyme.addItem(Protease.GLU_C_PHOSPHATE);
			comboBoxEnzyme.addItem(Protease.PEPSINE_PH_1_3);
			comboBoxEnzyme.addItem(Protease.PEPSINE_PH_GT_2);
			comboBoxEnzyme.addItem(Protease.PROTEINASE_K);
			comboBoxEnzyme.addItem(Protease.THERMOLYSINE);
			contentPanel.add(comboBoxEnzyme, "cell 1 4,growx");
		}
		{
			JLabel lblMissCleavages = new JLabel("Miss cleavages");
			contentPanel.add(lblMissCleavages, "cell 3 4,alignx center");
		}
		{
			this.comboBoxMiss = new JComboBox<Integer>();
			comboBoxMiss.addItem(0);
			comboBoxMiss.addItem(1);
			comboBoxMiss.addItem(2);
			comboBoxMiss.addItem(3);
			comboBoxMiss.addItem(4);
			contentPanel.add(comboBoxMiss, "cell 4 4,growx");
		}
		{
			this.progressBar = new JProgressBar();
			contentPanel.add(progressBar, "cell 0 6 5 1,growx,aligny center");
		}
		{
			this.btnClose = new JButton("Close");
			btnClose.addActionListener(this);

			this.btnStart = new JButton("Start");
			btnStart.addActionListener(this);

			contentPanel.add(btnStart, "flowx,cell 3 8,alignx center");
			contentPanel.add(btnClose, "cell 4 8,alignx center,aligny center");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		Object obj = e.getSource();

		if (obj == btnStart) {
			btnStart.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			String fasta = this.textFieldFasta.getText();
			String dbDir = this.textFieldDb.getText();

			if (fasta == null || fasta.length() == 0) {
				JOptionPane.showMessageDialog(this, "Protein sequence database was not set", "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
			} else if (dbDir == null || dbDir.length() == 0) {
				JOptionPane.showMessageDialog(this, "Output path was not set", "Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
			} else {

				Protease enzyme = this.comboBoxEnzyme.getItemAt(comboBoxEnzyme.getSelectedIndex());
				int missCount = this.comboBoxMiss.getItemAt(comboBoxMiss.getSelectedIndex());
//				int totalCount = Runtime.getRuntime().availableProcessors();
//				int useCount = totalCount > 2 ? totalCount - 2 : 1;

				try {
					task = new Pep2GiDbCreateTask(1, fasta, dbDir, enzyme, missCount);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				task.addPropertyChangeListener(this);
				task.execute();
			}
		} else if (obj == btnClose) {

			if (task != null) {
				task.cancel(true);
			}

			dispose();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	class Pep2GiDbCreateTask extends SwingWorker<Void, Void> {

		private final int threads;
		private ExecutorService executeService;

		private final File prodbFile;
		private final File pep2giFile;

		private ProteinDigester digester;
		private FastaProteinReader reader;
		private PrintWriter[][] tempWriters;
		private HashMap<String, HashSet<String>>[][] maps;
		private long readBytes;
		private long totalBytes;

		public Pep2GiDbCreateTask(int threads, String prodb, String pep2gi, Protease enzyme, int miss)
				throws IOException {
			this.threads = threads;
			this.prodbFile = new File(prodb);
			this.pep2giFile = new File(pep2gi);
			this.totalBytes = prodbFile.length();

			this.digester = new ProteinDigester.Builder(enzyme).missedCleavageMax(miss).build();
			this.reader = new FastaProteinReader(prodbFile);

			if (!pep2giFile.exists()) {
				pep2giFile.mkdirs();
			}
		}

		private void initial() throws IOException {
			this.tempWriters = new PrintWriter[26][25];
			this.maps = new HashMap[26][25];
			for (int i = 0; i < 26; i++) {
				char c = (char) ('A' + i);
				for (int j = 0; j < 25; j++) {
					tempWriters[i][j] = new PrintWriter(new File(pep2giFile, c + "" + (j + 6)));
					maps[i][j] = new HashMap<String, HashSet<String>>();
				}
			}
		}

		private void write() throws IOException {
			for (int i = 0; i < tempWriters.length; i++) {
				char c = (char) ('A' + i);
				FileWriter fw = new FileWriter(new File(pep2giFile, c + "_temp"), true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter writer = new PrintWriter(bw);

				for (int j = 0; j < tempWriters[i].length; j++) {
					tempWriters[i][j].close();
					BufferedReader reader = new BufferedReader(new FileReader(new File(pep2giFile, c + "" + (j + 6))));
					String line = null;
					while ((line = reader.readLine()) != null) {
						String[] cs = line.split("\t");
						if (maps[i][j].containsKey(cs[0])) {
							for (int k = 1; k < cs.length; k++) {
								maps[i][j].get(cs[0]).add(cs[k]);
							}
						} else {
							HashSet<String> set = new HashSet<String>();
							for (int k = 1; k < cs.length; k++) {
								set.add(cs[k]);
							}
							maps[i][j].put(cs[0], set);
						}
					}
					reader.close();

					for (String key : maps[i][j].keySet()) {
						StringBuilder sb = new StringBuilder(key);
						HashSet<String> set = maps[i][j].get(key);
						for (String s : set) {
							sb.append("\t").append(s);
						}
						writer.println(sb);
					}
				}

				writer.close();
			}
		}

		private void delete() throws IOException {
			for (int i = 0; i < maps.length; i++) {
				for (int j = 0; j < maps[i].length; j++) {
					char c = (char) ('A' + i);
					FileHandler.deleteFileOrFolder(new File(pep2giFile, c + "" + (j + 6)));
				}
			}
		}

		private void writeFinal() throws IOException {
			for (char i = 'A'; i <= 'Z'; i++) {
				HashMap<String, HashSet<String>>[] maps = new HashMap[25];
				for (int j = 0; j < maps.length; j++) {
					maps[j] = new HashMap<String, HashSet<String>>();
				}

				BufferedReader reader = new BufferedReader(new FileReader(new File(pep2giFile, i + "_temp")));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] cs = line.split("\t");
					int id = cs[0].length() - 6;
					if (maps[id].containsKey(cs[0])) {
						for (int k = 1; k < cs.length; k++) {
							maps[id].get(cs[0]).add(cs[k]);
						}
					} else {
						HashSet<String> set = new HashSet<String>();
						for (int k = 1; k < cs.length; k++) {
							set.add(cs[k]);
						}
						maps[id].put(cs[0], set);
					}
				}
				reader.close();

				PrintWriter writer = new PrintWriter(new File(pep2giFile, i + ""));
				for (HashMap<String, HashSet<String>> map : maps) {
					for (String key : map.keySet()) {
						StringBuilder sb = new StringBuilder(key);
						HashSet<String> set = map.get(key);
						for (String s : set) {
							sb.append("\t").append(s);
						}
						writer.println(sb);
					}
				}
				writer.close();

				FileHandler.deleteFileOrFolder(new File(pep2giFile, i + "_temp"));
			}
		}

		public long getTotalBytes() {
			return totalBytes;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub

			executeService = Executors.newFixedThreadPool(threads);
			progressBar.setStringPainted(true);
//			progressBar.setString("Processing...");

			initial();

			int proCount = 0;
			ArrayList<Future<Object>> noDoneList = new ArrayList<Future<Object>>();

			while (reader.hasNext()) {

				if (Thread.currentThread().isInterrupted()) {
					executeService.shutdownNow();
					return null;
				}

				Protein protein = reader.next();
				Pep2GiDbCallable callable = new Pep2GiDbCallable(protein, digester, tempWriters, maps);
				Future<Object> future = executeService.submit(callable);

				readBytes += protein.getAccessionId().length() + protein.toString().length();
				int progress = (int) (readBytes * 100 / totalBytes);
				setProgress(progress);

				if (!future.isDone()) {
					noDoneList.add(future);
				}

				proCount++;

				if (proCount % 100000 == 0) {

					while (true) {
						if (noDoneList.size() == 0) {
							break;
						}

						Iterator<Future<Object>> it = noDoneList.iterator();
						while (it.hasNext()) {
							Future<Object> futureNoDone = it.next();
							if (futureNoDone.isDone()) {
								it.remove();
							}
						}

						Thread.sleep(1000);
					}

					write();
					delete();

					initial();
				}
			}

			executeService.shutdown();

			while (true) {
				if (noDoneList.size() == 0) {
					break;
				}

				Iterator<Future<Object>> it = noDoneList.iterator();
				while (it.hasNext()) {
					Future<Object> future = it.next();
					if (future.isDone()) {
						it.remove();
					}
				}

				Thread.sleep(1000);
			}

			while (true) {
				if (executeService.awaitTermination(2, TimeUnit.SECONDS)) {
					break;
				}
			}

			write();
			delete();

			progressBar.setString("Writing...");

			writeFinal();

			setProgress(100);

			return null;
		}

		public void done() {

			if (this.getProgress() == 100) {
				progressBar.setString("Finished.");
				btnStart.setEnabled(true);

				JOptionPane.showMessageDialog(DBConstructorDialog.this, "Database generated successfully.", "Finished",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				btnStart.setEnabled(true);

				JOptionPane.showMessageDialog(DBConstructorDialog.this, "Task canceled.", "Cancel",
						JOptionPane.INFORMATION_MESSAGE);
			}

			setCursor(null);
		}
	}
}
