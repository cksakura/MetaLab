package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.stream.XMLStreamException;

import org.jfree.ui.ExtensionFileFilter;

import bmi.med.uOttawa.basicTools.GUI.CheckboxTableModel;
import bmi.med.uOttawa.basicTools.GUI.RowObject;
import bmi.med.uOttawa.glyco.Glycosyl;
import bmi.med.uOttawa.glyco.GlycosylDBManager;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoPara;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoTask;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GlycoMicroNovoTaskPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2020598006062497938L;
	
	private DefaultListModel<Glycosyl> usedMonoListModel;
	private Glycosyl[] glycosyls;
	private boolean[] selected;
	
	private JTable glycosylTable;
	private CheckboxTableModel model;
	private JList<Glycosyl> usedMonoList;
	private JTextField preTolTextField;
	private JTextField FraTolTextField;
	private JTextField paraTextField;
	private JTextField inputTextField;
	private JTextField outputTextField;
	private JList<GlycoMicroNovoTask> taskList;
	private DefaultListModel<GlycoMicroNovoTask> taskListModel;
	private JRadioButton rdbtnStandardMode;
	private JRadioButton rdbtnDiscoverMode;
	private String openedParameter;

	private JButton inputButton;
	private JButton outputButton;
	private JButton btnAddToTask;
	private JButton btnLoadButton;
	private JButton btnDelete;
	private JButton paraButton;
	private JButton btnRun;

	private JScrollPane allScrollPane;

	private JButton btnCloseButton;
	private JButton btnExport;
	
	/**
	 * Create the panel.
	 */
	public GlycoMicroNovoTaskPanel() {
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Parameter editor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Task editor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Task list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Load result", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(panel, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(panel_2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
								.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addContainerGap())))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
					.addGap(0))
		);
		
		this.btnLoadButton = new JButton("Load");
		this.btnCloseButton = new JButton("Close");
		this.btnCloseButton.setEnabled(false);
		
		this.btnExport = new JButton("Export");
		this.btnExport.setEnabled(false);

		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
			gl_panel_3.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnLoadButton, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
					.addGap(34)
					.addComponent(btnExport, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
					.addGap(34)
					.addComponent(btnCloseButton, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_panel_3.setVerticalGroup(
			gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnLoadButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnCloseButton)
						.addComponent(btnExport))
					.addGap(8))
		);
		panel_3.setLayout(gl_panel_3);
		
		JScrollPane taskScrollPane = new JScrollPane();
		
		JProgressBar progressBar = new JProgressBar();
		
		this.btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<GlycoMicroNovoTask> selectedList = GlycoMicroNovoTaskPanel.this.taskList.getSelectedValuesList();
				for (int i = 0; i < selectedList.size(); i++) {
					GlycoMicroNovoTaskPanel.this.taskListModel.removeElement(selectedList.get(i));
				}
			}
		});
		
		this.btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = GlycoMicroNovoTaskPanel.this.taskListModel.size();
				if (count == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "No task.", "Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					GlycoMicroNovoTaskPanel.this.inputButton.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.outputButton.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.btnLoadButton.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.btnAddToTask.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.btnRun.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.btnDelete.setEnabled(false);
					GlycoMicroNovoTaskPanel.this.paraButton.setEnabled(false);
					
					GlycoMicroNovoTask[] tasks = new GlycoMicroNovoTask[count];
					for (int i = 0; i < tasks.length; i++) {
						tasks[i] = GlycoMicroNovoTaskPanel.this.taskListModel.getElementAt(i);
						GlycoMicroNovoTaskPanel.this.taskList.setSelectedIndex(i);
					}

					GlycoMicroNovoThread thread = new GlycoMicroNovoThread(tasks, progressBar);
					thread.start();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		this.taskList = new JList<GlycoMicroNovoTask>();
		this.taskListModel = new DefaultListModel<GlycoMicroNovoTask>();
		this.taskList.setModel(taskListModel);
		taskScrollPane.setViewportView(taskList);
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
						.addComponent(taskScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_panel_2.createSequentialGroup()
							.addComponent(btnDelete)
							.addPreferredGap(ComponentPlacement.RELATED, 302, Short.MAX_VALUE)
							.addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
						.addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(taskScrollPane, GroupLayout.PREFERRED_SIZE, 277, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnDelete)
						.addComponent(btnRun))
					.addGap(12))
		);
		panel_2.setLayout(gl_panel_2);
		
		JLabel lblParameter = new JLabel("Parameter");
		
		JLabel lblInput = new JLabel("Input");
		
		JLabel lblOutput = new JLabel("Output");
		
		paraTextField = new JTextField();
		paraTextField.setColumns(10);
		
		inputTextField = new JTextField();
		inputTextField.setColumns(10);
		
		outputTextField = new JTextField();
		outputTextField.setColumns(10);
		
		this.paraButton = new JButton("...");
		paraButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser paraChooser = new JFileChooser();
				paraChooser.setFileFilter(new ExtensionFileFilter("Parameter file (.par)", "par"));
				if (paraChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = paraChooser.getSelectedFile().getAbsolutePath();
					GlycoMicroNovoTaskPanel.this.paraTextField.setText(file);
				}
			}
		});
		
		this.inputButton = new JButton("...");
		inputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser inputChooser = new JFileChooser();
				inputChooser.setFileFilter(new ExtensionFileFilter("Glycopeptide spectra file (.mzxml)", "mzxml"));
				if (inputChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = inputChooser.getSelectedFile().getAbsolutePath();
					String out = file.substring(0, file.length() - 5) + "txt";
					inputTextField.setText(file);
					outputTextField.setText(out);
				}
			}
		});
				
		this.outputButton = new JButton("...");
		outputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser outputChooser = new JFileChooser();
				outputChooser.setFileFilter(new ExtensionFileFilter("Result file (.txt)", "txt"));
				if (outputChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = outputChooser.getSelectedFile().getAbsolutePath();
					outputTextField.setText(file);
				}
			}
		});
				
		this.btnAddToTask = new JButton("Add to task list");
		btnAddToTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String in = inputTextField.getText();
				if (in == null || in.length() == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Input file is missing.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String out = outputTextField.getText();
				if (out == null || out.length() == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Output file is missing.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String para = paraTextField.getText();
				if (para == null || para.length() == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Parameter file is missing.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				GlycoMicroNovoTask task = new GlycoMicroNovoTask(in, out, para);
				GlycoMicroNovoTaskPanel.this.taskListModel.addElement(task);
			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblParameter)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(paraTextField, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(paraButton))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblOutput)
									.addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
									.addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, 323, GroupLayout.PREFERRED_SIZE)
									.addGap(6)
									.addComponent(outputButton, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addComponent(lblInput)
									.addPreferredGap(ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
									.addComponent(inputTextField, GroupLayout.PREFERRED_SIZE, 323, GroupLayout.PREFERRED_SIZE)
									.addGap(6)
									.addComponent(inputButton, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)))
							.addContainerGap())
						.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
							.addComponent(btnAddToTask)
							.addGap(169))))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblParameter)
						.addComponent(paraTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(paraButton))
					.addGap(18)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(lblInput)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGap(1)
							.addComponent(inputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(inputButton))
					.addGap(18)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGap(1)
							.addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGap(1)
							.addComponent(outputButton))
						.addComponent(lblOutput))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnAddToTask)
					.addContainerGap(18, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser openChooser = new JFileChooser();
				openChooser.setFileFilter(new ExtensionFileFilter("Parameter file (.par)", "par"));
				if (openChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = openChooser.getSelectedFile().getAbsolutePath();
					GlycoMicroNovoTaskPanel.this.openedParameter = file;
					GlycoMicroNovoPara parameter = GlycoMicroNovoPara.read(file);
					GlycoMicroNovoTaskPanel.this.preTolTextField
							.setText(String.valueOf(parameter.getPrecursorPPM()));
					GlycoMicroNovoTaskPanel.this.FraTolTextField
							.setText(String.valueOf(parameter.getFragmentPPM()));
					if (parameter.getParserType() == 0) {
						GlycoMicroNovoTaskPanel.this.rdbtnStandardMode.setSelected(true);
					} else {
						GlycoMicroNovoTaskPanel.this.rdbtnDiscoverMode.setSelected(true);
					}
					
					Glycosyl[] used = parameter.getGlycosyls();
					for (int i = 0; i < GlycoMicroNovoTaskPanel.this.model.getRowCount(); i++) {
						String shortName = (String) GlycoMicroNovoTaskPanel.this.model.getValueAt(i, 1);
						boolean found = false;
						for (int j = 0; j < used.length; j++) {
							if (used[j].getTitle().equals(shortName)) {
								if (!GlycoMicroNovoTaskPanel.this.selected[i]) {
									GlycoMicroNovoTaskPanel.this.selected[i] = true;
									GlycoMicroNovoTaskPanel.this.model.setValueAt(true, i, 0);
								}
								found = true;
								break;
							}
						}
						if (!found && GlycoMicroNovoTaskPanel.this.selected[i]) {
							GlycoMicroNovoTaskPanel.this.selected[i] = false;
							GlycoMicroNovoTaskPanel.this.model.setValueAt(false, i, 0);
						}
					}
					GlycoMicroNovoTaskPanel.this.openedParameter = file;
				}
			}
		});
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = GlycoMicroNovoTaskPanel.this.usedMonoListModel.size();
				if (count == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Monosaccharide list was not set.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (GlycoMicroNovoTaskPanel.this.preTolTextField.getText().length() == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Precursor mass tolerance was not set.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (GlycoMicroNovoTaskPanel.this.FraTolTextField.getText().length() == 0) {
					JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Fragment mass tolerance was not set.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (GlycoMicroNovoTaskPanel.this.openedParameter == null) {
					JFileChooser openChooser = new JFileChooser();
					openChooser.setFileFilter(new ExtensionFileFilter("Parameter file (.par)", "par"));
					if (openChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
						String file = openChooser.getSelectedFile().getAbsolutePath();
						if (!file.endsWith("par")) {
							file += ".par";
						}
						Glycosyl[] used = new Glycosyl[count];
						for (int i = 0; i < count; i++) {
							used[i] = GlycoMicroNovoTaskPanel.this.usedMonoListModel.getElementAt(i);
						}
						int parserType = GlycoMicroNovoTaskPanel.this.rdbtnStandardMode.isSelected() ? 0 : 1;

						try {
							GlycoMicroNovoPara.write(
									Double.parseDouble(GlycoMicroNovoTaskPanel.this.preTolTextField.getText()),
									Double.parseDouble(GlycoMicroNovoTaskPanel.this.FraTolTextField.getText()),
									parserType, used, file);
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				} else {
					int result = JOptionPane.showConfirmDialog(null,
							GlycoMicroNovoTaskPanel.this.openedParameter + " already existed. Overwrite it?", null,
							JOptionPane.YES_NO_OPTION);

					if (result == JOptionPane.YES_OPTION) {
						Glycosyl[] used = new Glycosyl[count];
						for (int i = 0; i < count; i++) {
							used[i] = GlycoMicroNovoTaskPanel.this.usedMonoListModel.getElementAt(i);
						}
						int parserType = GlycoMicroNovoTaskPanel.this.rdbtnStandardMode.isSelected() ? 0 : 1;

						try {
							GlycoMicroNovoPara.write(
									Double.parseDouble(GlycoMicroNovoTaskPanel.this.preTolTextField.getText()),
									Double.parseDouble(GlycoMicroNovoTaskPanel.this.FraTolTextField.getText()),
									parserType, used, GlycoMicroNovoTaskPanel.this.openedParameter);
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						return;
					}
				}
			}
		});
		
		JButton btnSaveAs = new JButton("Save as");
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser openChooser = new JFileChooser();
				openChooser.setFileFilter(new ExtensionFileFilter("Parameter file (.par)", "par"));
				if (openChooser.showOpenDialog(GlycoMicroNovoTaskPanel.this) == JFileChooser.APPROVE_OPTION) {
					String file = openChooser.getSelectedFile().getAbsolutePath();
					if (!file.endsWith("par")) {
						file += ".par";
					}
					int count = GlycoMicroNovoTaskPanel.this.usedMonoListModel.size();
					if (count == 0) {
						JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this, "Monosaccharide list was not set.",
								"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (GlycoMicroNovoTaskPanel.this.preTolTextField.getText().length() == 0) {
						JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this,
								"Precursor mass tolerance was not set.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (GlycoMicroNovoTaskPanel.this.FraTolTextField.getText().length() == 0) {
						JOptionPane.showMessageDialog(GlycoMicroNovoTaskPanel.this,
								"Fragment mass tolerance was not set.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Glycosyl[] used = new Glycosyl[count];
					for (int i = 0; i < count; i++) {
						used[i] = GlycoMicroNovoTaskPanel.this.usedMonoListModel.getElementAt(i);
					}
					int parserType = GlycoMicroNovoTaskPanel.this.rdbtnStandardMode.isSelected() ? 0 : 1;

					try {
						GlycoMicroNovoPara.write(
								Double.parseDouble(GlycoMicroNovoTaskPanel.this.preTolTextField.getText()),
								Double.parseDouble(GlycoMicroNovoTaskPanel.this.FraTolTextField.getText()), parserType,
								used, file);
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					GlycoMicroNovoTaskPanel.this.openedParameter = file;
				}
			}
		});
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GlycoMicroNovoTaskPanel.this.preTolTextField.setText(String.valueOf(10.0));
				GlycoMicroNovoTaskPanel.this.FraTolTextField.setText(String.valueOf(10.0));
				GlycoMicroNovoTaskPanel.this.rdbtnStandardMode.setSelected(true);
				for (int i = 0; i < GlycoMicroNovoTaskPanel.this.selected.length; i++) {
					GlycoMicroNovoTaskPanel.this.selected[i] = false;
					GlycoMicroNovoTaskPanel.this.model.setValueAt(false, i, 0);
				}
			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(btnOpen, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
								.addComponent(btnSave, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
								.addComponent(btnSaveAs, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
							.addGap(20))
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(btnNew, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(18)
							.addComponent(btnNew)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnOpen)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnSave)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnSaveAs)))
					.addContainerGap())
		);
		
		JScrollPane usedScrollPane = new JScrollPane();
		
		this.allScrollPane = new JScrollPane();
		
		JLabel lblPrecursorTolerance = new JLabel("Precursor tolerance");
		
		JLabel lblFragmentTolerance = new JLabel("Fragment tolerance");
		
		preTolTextField = new JTextField();
		preTolTextField.setText("10");
		preTolTextField.setColumns(10);
		
		FraTolTextField = new JTextField();
		FraTolTextField.setText("10");
		FraTolTextField.setColumns(10);
		
		JLabel lblPpm = new JLabel("ppm");
		
		JLabel label = new JLabel("ppm");
		
		this.rdbtnStandardMode = new JRadioButton("Standard mode");
		rdbtnStandardMode.setSelected(true);
		
		this.rdbtnDiscoverMode = new JRadioButton("Discover mode");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdbtnStandardMode);
		buttonGroup.add(rdbtnDiscoverMode);
		
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_panel_4.createSequentialGroup()
							.addComponent(lblFragmentTolerance)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(FraTolTextField, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_4.createSequentialGroup()
							.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING, false)
								.addGroup(Alignment.LEADING, gl_panel_4.createSequentialGroup()
									.addComponent(lblPrecursorTolerance)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(preTolTextField, 0, 0, Short.MAX_VALUE))
								.addComponent(usedScrollPane, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING, false)
								.addGroup(gl_panel_4.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_panel_4.createSequentialGroup()
											.addComponent(lblPpm)
											.addGap(20)
											.addComponent(rdbtnStandardMode, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
										.addComponent(rdbtnDiscoverMode, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)))
								.addGroup(gl_panel_4.createSequentialGroup()
									.addGap(10)
									.addComponent(allScrollPane, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 101, Short.MAX_VALUE)))))
					.addGap(0))
		);
		gl_panel_4.setVerticalGroup(
			gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addComponent(allScrollPane, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
						.addComponent(usedScrollPane, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
						.addComponent(lblPpm)
						.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
								.addComponent(rdbtnStandardMode)
								.addGroup(gl_panel_4.createSequentialGroup()
									.addGap(30)
									.addComponent(rdbtnDiscoverMode)))
							.addGroup(gl_panel_4.createSequentialGroup()
								.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblPrecursorTolerance)
									.addComponent(preTolTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblFragmentTolerance)
									.addComponent(FraTolTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(label)))))
					.addContainerGap(12, Short.MAX_VALUE))
		);
		
		this.usedMonoList = new JList<Glycosyl>();
		this.usedMonoListModel = new DefaultListModel<Glycosyl>();
		this.usedMonoList.setModel(usedMonoListModel);
		
		usedScrollPane.setViewportView(usedMonoList);
		
		try {
			this.loadGlycosyl();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		panel_4.setLayout(gl_panel_4);
		panel.setLayout(gl_panel);
		setLayout(groupLayout);
	}
	
	public JButton getJButtonLoad() {
		return this.btnLoadButton;
	}
	
	public JButton getJButtonExport() {
		return this.btnExport;
	}
	
	public JButton getJButtonClose() {
		return this.btnCloseButton;
	}
	
	public void refresh() throws IOException{
		this.loadGlycosyl();
		this.usedMonoListModel.removeAllElements();
	}
	
	private void loadGlycosyl() throws IOException {

		this.glycosyls = GlycosylDBManager.load();

		RowObject[] objs = new RowObject[glycosyls.length];
		for (int i = 0; i < glycosyls.length; i++) {
			objs[i] = new RowObject(new Object[] { glycosyls[i].getTitle() });
		}

		String[] title = new String[] { "Monosaccharide" };
		this.selected = new boolean[objs.length];
		this.model = new CheckboxTableModel(title, objs, selected);
		this.model.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub

				int row = e.getFirstRow();
				int column = e.getColumn();
				Object obj = model.getValueAt(row, column);

				if (obj.getClass() == Boolean.class) {
					Boolean selected = (Boolean) obj;
					if (selected) {
						usedMonoListModel.addElement(glycosyls[row]);
					} else {
						usedMonoListModel.removeElement(glycosyls[row]);
					}
				}
			}
		});
		glycosylTable = new JTable(this.model);
		glycosylTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		allScrollPane.setViewportView(glycosylTable);
	}
	
	private class GlycoMicroNovoThread extends Thread {

		private GlycoMicroNovoTask[] tasks;
		private JProgressBar bar;

		public GlycoMicroNovoThread(GlycoMicroNovoTask[] tasks, JProgressBar bar) {
			
			this.tasks = tasks;
			this.bar = bar;
		}

		public void run() {
			bar.setStringPainted(true);
			bar.setString("Processing...");
			for (int i = 0; i < tasks.length; i++) {
				tasks[i].initial();
				while (tasks[i].hasNext()) {
					tasks[i].processNext();
					bar.setString((i + 1) + "/" + tasks.length);
					bar.setValue(tasks[i].completePercentage());
				}
				tasks[i].complete();
			}
			bar.setValue(0);
			bar.setString("Finish");
			
			GlycoMicroNovoTaskPanel.this.inputButton.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.outputButton.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.btnLoadButton.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.btnAddToTask.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.btnRun.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.btnDelete.setEnabled(true);
			GlycoMicroNovoTaskPanel.this.paraButton.setEnabled(true);
		}
	}
}
