package bmi.med.uOttawa.metalab.task.dia.monitor.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.license.LicenseVerifier;
import bmi.med.uOttawa.metalab.task.dia.gui.MetaLabMainPanelDia;
import bmi.med.uOttawa.metalab.task.dia.monitor.MetaDiaMonitorTask;
import bmi.med.uOttawa.metalab.task.dia.monitor.par.MetaParIODiaRT;
import bmi.med.uOttawa.metalab.task.dia.monitor.par.MetaParameterDiaRT;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParaIoDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;

public class MetaLabDiaMonitorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField textFieldMonFolder;
	private JTextField textFieldResFolder;
	private JTable resultTable;

	private MetaParameterDiaRT par;

	/**
	 * Create the panel.
	 */
	public MetaLabDiaMonitorPanel(MetaParameterDiaRT par, MetaSourcesDia msv) {
		setLayout(new MigLayout("", "[100][300:680:920,grow][50]", "[30][30][200:300:400,grow][200:300:400,grow][100]"));
		this.par = par;
		JLabel lblMonFolder = new JLabel("Monitor folder");
		add(lblMonFolder, "cell 0 0,alignx left");

		textFieldMonFolder = new JTextField();
		add(textFieldMonFolder, "flowx,cell 1 0,growx");
		textFieldMonFolder.setColumns(10);

		JButton btnMonFolder = new JButton("Browse");
		add(btnMonFolder, "cell 2 0");
		btnMonFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldMonFolder.setText(selectedPath);
			}
		});

		JLabel lblResultFolder = new JLabel("Result folder");
		add(lblResultFolder, "cell 0 1,alignx left");

		textFieldResFolder = new JTextField();
		add(textFieldResFolder, "cell 1 1,growx");
		textFieldResFolder.setColumns(10);

		JButton btnResFolder = new JButton("Browse");
		add(btnResFolder, "cell 2 1");
		btnResFolder.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectFile = fileChooser.getSelectedFile();
				String selectedPath = selectFile.getAbsolutePath();
				textFieldResFolder.setText(selectedPath);
			}
		});

		JScrollPane scrollPaneMonitor = new JScrollPane();
		scrollPaneMonitor.setToolTipText("Monitor folder");
		scrollPaneMonitor.setViewportBorder(
				new TitledBorder(null, "Monitor folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(scrollPaneMonitor, "cell 0 2 3 1,grow");

		JList<String> monitorList = new JList<String>();
		scrollPaneMonitor.setViewportView(monitorList);

		Object[] columnName = new Object[] {"File","Round 1", "Model", "Round 2", "Round 3", "Round 4"};
		DefaultTableModel resultTableModel = new DefaultTableModel(columnName, 0);
		resultTable = new JTable(resultTableModel);
		
		JScrollPane scrollPane = new JScrollPane(resultTable);
//		scrollPane.setViewportBorder(
//				new TitledBorder(null, "Result folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel titledPanel = new JPanel(new BorderLayout());
		titledPanel.setBorder(new TitledBorder(null, "Result folder", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		titledPanel.add(scrollPane, BorderLayout.CENTER);
		add(titledPanel, "cell 0 3 3 1,grow");
//		add(scrollPane, "cell 0 3 3 1,grow");
		
		resultTable.setTableHeader(resultTable.getTableHeader());
		scrollPane.setColumnHeaderView(resultTable.getTableHeader());

		JPanel runPanel = new JPanel();
		runPanel.setBorder(new TitledBorder(null, "Control panel", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(runPanel, "cell 0 4 3 1,grow");
		runPanel.setLayout(new MigLayout("", "[100][300:680:920,grow][50]", "[30][30][30]"));

		JButton btnStart = new JButton("Start monitor");
		JButton btnEnd = new JButton("End monitor");
		JButton btnEnd2 = new JButton("End monitor after the total number of processed files reached:");
		JButton btnReset = new JButton("Reset");
		JComboBox<Integer> comboBoxFileCount = new JComboBox<Integer>();

		runPanel.add(btnStart, "cell 0 0,growx");
		btnStart.addActionListener(l -> {

		});

		JCheckBox chckbxCombine = new JCheckBox(
				"Perform taxonomic and functional analysis after monitoring is completed");
		runPanel.add(chckbxCombine, "cell 1 0");
		chckbxCombine.setSelected(par.isCombineResult());
		chckbxCombine.addActionListener(l -> {
			par.setCombineResult(chckbxCombine.isSelected());
		});

		runPanel.add(btnEnd, "cell 0 1,growx");
		btnEnd.setEnabled(false);
		btnEnd.addActionListener(l -> {
			btnEnd.setEnabled(false);
			par.setStopNow(true);
			par.setStopComplete(false);
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String formattedDateTime = now.format(formatter);
			String fileName = "file_" + formattedDateTime + ".txt";

			try (PrintWriter writer = new PrintWriter(new File(par.getMonitorFolder(), fileName))) {
				writer.println("The monitor will end after all current files were processed.");
				writer.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		});

		runPanel.add(btnEnd2, "flowx,cell 1 1");
		btnEnd2.setEnabled(false);
		btnEnd2.addActionListener(l -> {
			btnEnd2.setEnabled(false);
			par.setStopNow(false);
			par.setStopComplete(true);
			int taskCount = comboBoxFileCount.getItemAt(comboBoxFileCount.getSelectedIndex());
			par.setTaskCount(taskCount);

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String formattedDateTime = now.format(formatter);
			String fileName = "file_" + formattedDateTime + ".txt";

			try (PrintWriter writer = new PrintWriter(new File(par.getMonitorFolder(), fileName))) {
				writer.println("The monitor will end after " + taskCount + " files were processed.");
				writer.close();
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		});

		runPanel.add(comboBoxFileCount, "cell 1 1");
		for (int i = 1; i < 1000; i++) {
			comboBoxFileCount.addItem(i);
		}

		runPanel.add(btnReset, "cell 1 1");
		btnReset.setEnabled(false);

		JProgressBar progressBar = new JProgressBar();
		runPanel.add(progressBar, "cell 0 2 3 1,growx");
		btnReset.addActionListener(l -> {
			btnEnd.setEnabled(true);
			btnEnd2.setEnabled(true);
			comboBoxFileCount.setSelectedIndex(0);
			par.setStopNow(false);
			par.setStopComplete(false);
		});
	}

}
