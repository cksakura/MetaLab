/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.awt.Dimension;
import javax.swing.border.TitledBorder;

/**
 * @author Kai Cheng
 *
 */
public class RawFilesPanel extends JPanel {

	private static Logger LOGGER = LogManager.getLogger();
	/**
	 * 
	 */
	private static final long serialVersionUID = 5588166584465647743L;
	private JTable table;
	private DefaultTableModel model;
	private DefaultTableModel expModel;
	private JFileChooser rawFileChooser;
	private JFileChooser batchChooser;
	private HashSet<String> fileSet;
	private String pattern;
	private boolean patternTableUpdate;
	private String[][] originalRawFiles;
	
	/**
	 * Create the panel.
	 */
	public RawFilesPanel(String[][] rawFiles, String result) {

		setPreferredSize(new Dimension(880, 560));
		setLayout(new MigLayout("", "[1000, grow 1920]", "[500][40]"));

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "Raw files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.setLayout(new MigLayout("", "[170,grow 400][170,grow 400][170,grow 400][100,grow 150]", "[420,grow][40]"));
		add(contentPane, "cell 0 0,grow");

		this.originalRawFiles = rawFiles;
		this.fileSet = new HashSet<String>();
		for (int i = 0; i < rawFiles[0].length; i++) {
			this.fileSet.add(rawFiles[0][i]);
		}

		ExpIdDialog expDialog = new ExpIdDialog();
		expDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		expDialog.getOkButton().addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			String[] names = expDialog.getExpNames(selectedRow.length);

			for (int i = 0; i < selectedRow.length; i++) {
				table.setValueAt(names[i], selectedRow[i], 2);
			}

			expDialog.setVisible(false);
		});

		expDialog.getCancelButton().addActionListener(l -> {
			expDialog.setVisible(false);
		});

		JButton btnClear = new JButton("Clear");
		if (rawFiles != null && rawFiles[0].length > 0) {
			btnClear.setEnabled(true);
		} else {
			btnClear.setEnabled(false);
		}

		btnClear.addActionListener(l -> {
			fileSet = new HashSet<String>();
			int count = model.getRowCount();
			for (int i = count - 1; i >= 0; i--) {
				model.removeRow(i);
			}
			originalRawFiles = getOriginalRawFiles();
			btnClear.setEnabled(false);
		});

		/*JButton btnSetExperiment = new JButton("Set experiment");
		btnSetExperiment.setEnabled(false);
		btnSetExperiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expDialog.setVisible(true);
			}
		});*/

		JButton btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(l -> {
			int[] selectedRow = table.getSelectedRows();
			for (int i = 0; i < selectedRow.length; i++) {
				int first = table.getSelectedRow();
				String raw = (String) table.getValueAt(first, 1);
				fileSet.remove(raw);
				model.removeRow(first);
			}
			btnRemove.setEnabled(false);
		});

		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(l -> {

			if (rawFiles != null && rawFiles[0].length > 0) {
				rawFileChooser = new JFileChooser(new File(rawFiles[0][0]));
			} else {
				rawFileChooser = new JFileChooser();
			}
			rawFileChooser.setFileFilter(new FileNameExtensionFilter(
					"raw files (*.raw, *.wiff, *.mzXML) or peptide identification result (.txt for MaxQuant and .xml for X!Tandem)",
					new String[] { "raw", "wiff", "mzXML", "txt", "xml" }));
			rawFileChooser.setMultiSelectionEnabled(true);

			int returnValue = rawFileChooser.showOpenDialog(RawFilesPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File[] selectedFiles = rawFileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					if (!fileSet.contains(file.getAbsolutePath())) {
						String path = file.getAbsolutePath();
						fileSet.add(path);

						String name = file.getName();
						name = name.substring(0, name.lastIndexOf("."));

						model.addRow(new Object[] { file.exists(), path, name });
					}
				}
			}
		});

		JButton btnBatchLoad = new JButton("Batch load");
		btnBatchLoad.addActionListener(l -> {
			if (rawFiles != null && rawFiles[0].length > 0) {
				batchChooser = new JFileChooser(new File(rawFiles[0][0]));
			} else {
				batchChooser = new JFileChooser();
			}
			batchChooser.setFileFilter(new FileNameExtensionFilter("text file (*.txt, *.tsv, *.csv) ",
					new String[] { "txt", "tsv", "csv" }));

			int returnValue = batchChooser.showOpenDialog(RawFilesPanel.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File batchFile = batchChooser.getSelectedFile();
				Object[][] files = read(batchFile);
				for (int i = 0; i < files.length; i++) {
					String filei = (String) files[i][1];
					if (!fileSet.contains(filei)) {

						fileSet.add(filei);

						model.addRow(new Object[] { (boolean) files[i][0], filei, (String) files[i][2] });
					}
				}
			}
		});
		
		expModel = new DefaultTableModel();
		expModel.setDataVector(new Object[0][2], new Object[] { "Keep in experiment name", "Content" });
		expModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
				generateExp();
			}
		});

		contentPane.add(btnBatchLoad, "cell 0 1,alignx center");
		contentPane.add(btnAdd, "cell 1 1,alignx center");
		contentPane.add(btnRemove, "cell 2 1,alignx center");
//		contentPane.add(btnSetExperiment, "cell 3 1,alignx center");
		contentPane.add(btnClear, "cell 3 1,alignx center");

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 0 4 1,grow");

		table = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8095709998037461105L;

			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};

		Object[][] trans = new Object[rawFiles[0].length][3];
		for (int i = 0; i < trans.length; i++) {
			File rawi = new File(rawFiles[0][i]);
			trans[i][0] = rawi.exists();
			trans[i][1] = rawFiles[0][i];
			trans[i][2] = rawFiles[1][i];
		}

		model = new DefaultTableModel(trans, new Object[] { "", "File", "Experiment" });
		table.setModel(model);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(480);
		table.getColumnModel().getColumn(2).setPreferredWidth(180);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (table.getSelectedRowCount() > 0) {
					btnRemove.setEnabled(true);
//					btnSetExperiment.setEnabled(true);
				} else {
					btnRemove.setEnabled(false);
//					btnSetExperiment.setEnabled(false);
				}

				if (table.getRowCount() > 0) {
					btnClear.setEnabled(true);
				} else {
					btnClear.setEnabled(false);
				}
			}
		});

		scrollPane.setViewportView(table);
	}

	private void generateExp() {
		int patternRowCount = this.expModel.getRowCount();
		boolean[] combine = new boolean[patternRowCount];
		for (int i = 0; i < patternRowCount; i++) {
			combine[i] = (boolean) this.expModel.getValueAt(i, 0);
		}
		int rawRowCount = this.model.getRowCount();
		for (int i = 0; i < rawRowCount; i++) {
			String[] cs = ((String) this.model.getValueAt(i, 2)).split(pattern);
			if (cs.length == combine.length) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < cs.length; j++) {
					if (combine[j]) {
						sb.append(cs[j]).append("_");
					}
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
				this.model.setValueAt(sb.toString(), i, 2);
			}
		}
	}
	
	private void restore() {
		
	}

	private Object[][] read(File in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(in));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading input information file " + in, e);
		}
		String line = null;
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("[,\t]");
				if (cs.length == 1) {
					String path = cs[0];
					if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
						path = path.substring(1, path.length() - 1);
					}
					File filei = new File(path);
					String name = filei.getName();
					name = name.substring(0, name.lastIndexOf("."));

					list.add(new Object[] { filei.exists(), path, name });

				} else if (cs.length >= 2) {

					String path = cs[0];
					if (path.length() > 2 && path.startsWith("\"") && path.endsWith("\"")) {
						path = path.substring(1, path.length() - 1);
					}
					String name = cs[1];
					if (name.length() > 2 && name.startsWith("\"") && name.endsWith("\"")) {
						name = name.substring(1, name.length() - 1);
					}

					File filei = new File(path);
					list.add(new Object[] { filei.exists(), path, name });
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading input information file " + in, e);
		}

		Object[][] files = list.toArray(new Object[list.size()][]);
		
		return files;
	}
	
	public String[][] getRawFiles() {
		int rowCount = table.getRowCount();
		ArrayList<String> rawlist = new ArrayList<String>();
		ArrayList<String> namelist = new ArrayList<String>();
		for (int i = 0; i < rowCount; i++) {
			boolean find = (boolean) table.getValueAt(i, 0);
			if (find) {
				String path = (String) table.getValueAt(i, 1);
				String name = (String) table.getValueAt(i, 2);
				rawlist.add(path);
				namelist.add(name);
			}
		}
		String[][] files = new String[2][];
		files[0] = rawlist.toArray(new String[rawlist.size()]);
		files[1] = namelist.toArray(new String[namelist.size()]);

		return files;
	}
	
	public String[][] getOriginalRawFiles() {
		int rowCount = table.getRowCount();
		ArrayList<String> rawlist = new ArrayList<String>();
		ArrayList<String> namelist = new ArrayList<String>();
		for (int i = 0; i < rowCount; i++) {
			boolean find = (boolean) table.getValueAt(i, 0);
			if (find) {
				String path = (String) table.getValueAt(i, 1);
				String name = (new File(path)).getName();
				rawlist.add(path);
				namelist.add(name);
			}
		}
		String[][] files = new String[2][];
		files[0] = rawlist.toArray(new String[rawlist.size()]);
		files[1] = namelist.toArray(new String[namelist.size()]);

		return files;
	}

	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("TableRenderDemo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				RawFilesPanel newContentPane = new RawFilesPanel(new String[][] {}, "");
				newContentPane.setOpaque(true); // content panes must be opaque
				frame.setContentPane(newContentPane);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	/*class ExpDialog extends JDialog {

		private static final long serialVersionUID = -7222658578948715284L;
		private final JPanel contentPanel = new JPanel();
		private JTextField textField;
		private JButton okButton;
		private JButton cancelButton;

		public ExpDialog() {
			setBounds(100, 100, 300, 120);
			getContentPane().setLayout(new BorderLayout());
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(contentPanel, BorderLayout.CENTER);
			contentPanel.setLayout(new MigLayout("", "[60px][240px]", "[50]"));

			JLabel lblExperiment = new JLabel("Experiment");
			contentPanel.add(lblExperiment, "cell 0 0,alignx center");

			textField = new JTextField();
			contentPanel.add(textField, "cell 1 0,growx,aligny center");
			textField.setColumns(10);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);

			okButton = new JButton("OK");
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

			cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
		}

		JButton getOkButton() {
			return okButton;
		}

		JButton getCancelButton() {
			return cancelButton;
		}
	}*/
	
}
