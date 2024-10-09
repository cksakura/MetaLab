package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;

import java.awt.Component;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLStreamException;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JProgressBar;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;

public class DenovoParaPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2449825853443605233L;
	private JTextField preTolTextField;
	private JTextField textFieldInput;
	private JTextField textFieldOutput;
	private JTextField fragTextField;
	private JFileChooser inputChooser;
	private JFileChooser outputChooser;
	private JList<Glycosyl> usedMonoList;
	private DefaultListModel<Glycosyl> usedMonoListModel;
	private Glycosyl[] glycosyls;
	private boolean[] selected;
	
	private JTable glycosylTable;
	private CheckboxTableModel model;
	private JTextField topNTextField;
	private JCheckBox chckbxNewCheckBox;
	private JProgressBar progressBar;
	
	/**
	 * Create the panel.
	 */
	public DenovoParaPanel() {

		JPanel monoPanel = new JPanel();
		monoPanel.setToolTipText("");
		monoPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Monosaccharides",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		JPanel msmsPanel = new JPanel();
		msmsPanel.setBorder(new TitledBorder(null, "MS/MS", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		this.progressBar = new JProgressBar();

		JPanel inputPanel = new JPanel();
		inputPanel.setToolTipText("Glycopeptide spectra files (.mzxml)");
		inputPanel.setBorder(new TitledBorder(null, "Input", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panelOutput = new JPanel();
		panelOutput.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Output",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		JButton buttonOutput = new JButton("...");
		textFieldOutput = new JTextField();
		textFieldOutput.setColumns(10);
		
		buttonOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputChooser = new JFileChooser();
				outputChooser.setFileFilter(new ExtensionFileFilter("", "txt"));
				if (outputChooser.showOpenDialog(DenovoParaPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = outputChooser.getSelectedFile().getAbsolutePath();
					textFieldOutput.setText(file);
				}
			}
		});
		
		GroupLayout gl_panelOutput = new GroupLayout(panelOutput);
		gl_panelOutput.setHorizontalGroup(
			gl_panelOutput.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panelOutput.createSequentialGroup()
					.addComponent(textFieldOutput, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(buttonOutput, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_panelOutput.setVerticalGroup(
			gl_panelOutput.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelOutput.createSequentialGroup()
					.addGroup(gl_panelOutput.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldOutput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonOutput))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panelOutput.setLayout(gl_panelOutput);

		JButton buttonInput = new JButton("...");
		textFieldInput = new JTextField();
		textFieldInput.setColumns(10);

		buttonInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inputChooser = new JFileChooser();
				inputChooser.setFileFilter(new ExtensionFileFilter("Glycopeptide spectra file (.mzxml)", "mzxml"));
				if (inputChooser.showOpenDialog(DenovoParaPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = inputChooser.getSelectedFile().getAbsolutePath();
					String out = file.substring(0, file.length()-5)+"txt";
					textFieldInput.setText(file);
					textFieldOutput.setText(out);
				}
			}
		});

		GroupLayout gl_inputPanel = new GroupLayout(inputPanel);
		gl_inputPanel.setHorizontalGroup(
			gl_inputPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_inputPanel.createSequentialGroup()
					.addComponent(textFieldInput, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(buttonInput, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_inputPanel.setVerticalGroup(
			gl_inputPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_inputPanel.createSequentialGroup()
					.addGroup(gl_inputPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonInput))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		inputPanel.setLayout(gl_inputPanel);

		JLabel lblFragmentTolerance = new JLabel("Fragment tolerance");

		fragTextField = new JTextField();
		fragTextField.setText("10");
		fragTextField.setColumns(10);

		JLabel lblPpm = new JLabel("ppm");

		preTolTextField = new JTextField();
		preTolTextField.setText("10");
		preTolTextField.setColumns(10);

		JLabel lblPrecursorTolerance = new JLabel("Precursor tolerance");

		JLabel label = new JLabel("ppm");
		
		JLabel lblDisplayTop = new JLabel("Display top");
		
		JLabel lblCompositions = new JLabel("compositions");
		
		topNTextField = new JTextField();
		topNTextField.setText("1");
		topNTextField.setColumns(10);
		
		this.chckbxNewCheckBox = new JCheckBox("Only determined compositions");
		chckbxNewCheckBox.setSelected(true);
		GroupLayout gl_msmsPanel = new GroupLayout(msmsPanel);
		gl_msmsPanel.setHorizontalGroup(
			gl_msmsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_msmsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblFragmentTolerance)
						.addComponent(lblPrecursorTolerance, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(preTolTextField, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
						.addComponent(fragTextField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPpm, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
					.addGap(61)
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_msmsPanel.createSequentialGroup()
							.addGap(8)
							.addComponent(lblDisplayTop, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(topNTextField, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(lblCompositions, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
						.addComponent(chckbxNewCheckBox))
					.addContainerGap(176, Short.MAX_VALUE))
		);
		gl_msmsPanel.setVerticalGroup(
			gl_msmsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_msmsPanel.createSequentialGroup()
					.addGap(4)
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(preTolTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPrecursorTolerance)
						.addComponent(label)
						.addComponent(lblCompositions)
						.addComponent(topNTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblDisplayTop))
					.addGap(18)
					.addGroup(gl_msmsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFragmentTolerance)
						.addComponent(fragTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPpm)
						.addComponent(chckbxNewCheckBox))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		msmsPanel.setLayout(gl_msmsPanel);

		this.usedMonoList = new JList<Glycosyl>();
		this.usedMonoListModel = new DefaultListModel<Glycosyl>();
		usedMonoList.setModel(usedMonoListModel);
		
		JScrollPane usedMonoScrollPane = new JScrollPane(usedMonoList);
		
		JScrollPane allScrollPane = new JScrollPane();

		GroupLayout gl_monoPanel = new GroupLayout(monoPanel);
		gl_monoPanel.setHorizontalGroup(
			gl_monoPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_monoPanel.createSequentialGroup()
					.addGap(29)
					.addComponent(usedMonoScrollPane, GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)
					.addGap(38)
					.addComponent(allScrollPane, GroupLayout.PREFERRED_SIZE, 380, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(27, Short.MAX_VALUE))
		);
		gl_monoPanel.setVerticalGroup(
			gl_monoPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_monoPanel.createSequentialGroup()
					.addGap(13)
					.addGroup(gl_monoPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(allScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
						.addComponent(usedMonoScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
					.addGap(11))
		);
		
		this.loadGlycosyl();
		glycosylTable = new JTable(this.model);
		glycosylTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		allScrollPane.setViewportView(glycosylTable);

		monoPanel.setLayout(gl_monoPanel);
		monoPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { usedMonoScrollPane }));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(49)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(panelOutput, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(inputPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(monoPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(msmsPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
					.addGap(50))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(32)
					.addComponent(msmsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(monoPanel, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(inputPanel, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(panelOutput, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(64))
		);
		setLayout(groupLayout);

	}
	
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	private void loadGlycosyl() {

		this.glycosyls = new Glycosyl[] { Glycosyl.dHex, Glycosyl.Hex, Glycosyl.HexNAc, Glycosyl.NeuAc,
				Glycosyl.NeuGc, Glycosyl.Glucuronic_acid, Glycosyl.Pseudaminic_acid};
		
		RowObject[] objs = new RowObject[glycosyls.length];
		for (int i = 0; i < glycosyls.length; i++) {
			objs[i] = glycosyls[i].getRowObject();
		}
		
		String[] title = new String[]{"Title", "Composition", "Mono mass", "Ave mass"};
		this.selected = new boolean[objs.length];
		this.model = new CheckboxTableModel(title, objs, selected);
		this.model.addTableModelListener(new TableModelListener(){

			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				
				int row = e.getFirstRow();
				int column = e.getColumn();
				Object obj = model.getValueAt(row, column);

				if(obj.getClass() == Boolean.class){
					Boolean selected = (Boolean) obj;
					if(selected){
						usedMonoListModel.addElement(glycosyls[row]);
					}else{
						usedMonoListModel.removeElement(glycosyls[row]);
					}
				}
			}
		});
	}
	
	public GlycoMicroNovoTask getTask() throws IOException, XMLStreamException, DataFormatException {

		String in = this.textFieldInput.getText();
		String out = this.textFieldOutput.getText();
		double precursorTol = Double.parseDouble(this.preTolTextField.getText());
		double fragmentTol = Double.parseDouble(this.fragTextField.getText());
		int maxN = Integer.parseInt(this.topNTextField.getText());
		boolean determined = this.chckbxNewCheckBox.isSelected();

		int size = this.usedMonoListModel.size();
		Glycosyl[] glycosyls = new Glycosyl[size];
		for (int i = 0; i < size; i++) {
			glycosyls[i] = this.usedMonoListModel.get(i);
		}
		GlycoMicroNovoTask task = new GlycoMicroNovoTask(in, out, glycosyls, precursorTol, fragmentTol, 0);
		return task;
	}
}
