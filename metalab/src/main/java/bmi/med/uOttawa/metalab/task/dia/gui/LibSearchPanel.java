package bmi.med.uOttawa.metalab.task.dia.gui;

import java.awt.Color;
import java.io.File;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class LibSearchPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private DefaultListModel<String> listModel;
	private HashSet<String> libSet;
	private String lastUsedFolder;

	/**
	 * Create the panel.
	 */
	public LibSearchPanel(String[] libraries) {
		setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Library",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new MigLayout("", "[60][grow][60]", "[30][100:120:150,grow][30]"));

		this.libSet = new HashSet<String>();
		JLabel libLabel = new JLabel("Library");
		add(libLabel, "cell 0 0,alignx left");

		lastUsedFolder = System.getProperty("user.home");

		textField = new JTextField();
		add(textField, "cell 1 0,growx");
		textField.setColumns(10);
		textField.setEditable(false);

		JButton libButton = new JButton("Browse");
		add(libButton, "cell 2 0,alignx center");
		libButton.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(lastUsedFolder);

			// Show the file dialog
			int result = fileChooser.showOpenDialog(LibSearchPanel.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String filePath = selectedFile.getAbsolutePath();
				textField.setText(filePath);
				lastUsedFolder = selectedFile.getParent();
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 1 3 1,grow");

		listModel = new DefaultListModel<String>();
		JList<String> fileList = new JList<String>(listModel);

		scrollPane.setViewportView(fileList);

		if (libraries.length > 0) {
			lastUsedFolder = System.getProperty((new File(libraries[0])).getParent());
			for (String lib : libraries) {
				this.libSet.add(lib);
				this.listModel.addElement(lib);
			}
		}

		JButton addButton = new JButton("Add");
		add(addButton, "cell 0 2");
		addButton.addActionListener(l -> {
			String filePath = textField.getText();
			if (!filePath.isEmpty() && !libSet.contains(filePath)) {
				libSet.add(filePath);
				listModel.addElement(filePath);
				textField.setText("");
			}
		});

		JButton removeButton = new JButton("Remove");
		add(removeButton, "cell 1 2,alignx center");
		removeButton.addActionListener(l -> {
			int selectedIndex = fileList.getSelectedIndex();
			if (selectedIndex != -1) {
				String filePathToRemove = listModel.getElementAt(selectedIndex);
				libSet.remove(filePathToRemove);
				listModel.remove(selectedIndex);
			}
		});

		JButton clearButton = new JButton("Clear");
		add(clearButton, "cell 2 2");
		clearButton.addActionListener(l -> {
			listModel.clear();
			libSet.clear();
		});

		fileList.addListSelectionListener(e -> {
			removeButton.setEnabled(!listModel.isEmpty() && fileList.getSelectedIndex() != -1);
		});
	}

	public String[] getLibs() {
		String[] libs = this.libSet.toArray(new String[this.libSet.size()]);
		return libs;
	}
}
