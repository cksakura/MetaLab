package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import bmi.med.uOttawa.basicTools.FormatTool;
import bmi.med.uOttawa.basicTools.GUI.CheckboxTableModel;
import bmi.med.uOttawa.basicTools.GUI.RowObject;
import bmi.med.uOttawa.glyco.Glycosyl;
import bmi.med.uOttawa.glyco.GlycosylDBManager;
import bmi.med.uOttawa.probasic.aminoacid.AminoAcidProperty;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

public class GlycosylViewDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3363729556413642324L;
	
	private JTextField textField_S;
	private JTextField textField_P;
	private JTextField textField_N;
	private JTextField textField_O;
	private JTextField textField_H;
	private JTextField textField_C;
	private JTextField textField_AM;
	private JTextField textField_MM;
	private JTextField textField_SN;
	private JTextField textField_FN;
	private int[] count;
	private double monoMass;
	private double aveMass;
	private double[] mono = new double[] { AminoAcidProperty.MONOW_C, AminoAcidProperty.MONOW_H,
			AminoAcidProperty.MONOW_O, AminoAcidProperty.MONOW_N, AminoAcidProperty.MONOW_P,
			AminoAcidProperty.MONOW_S32};
	private double[] ave = new double[]{ AminoAcidProperty.AVERAGEW_C, AminoAcidProperty.AVERAGEW_H,
			AminoAcidProperty.AVERAGEW_O, AminoAcidProperty.AVERAGEW_N, AminoAcidProperty.MONOW_P,
			AminoAcidProperty.AVERAGEW_S};

	private JTable glycosylTable;
	private CheckboxTableModel model;
	private Glycosyl[] glycosyls;
	private boolean[] selected;

	private JButton btnDelete;

	private JScrollPane scrollPane;

	private JButton btnClose;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GlycosylViewDialog dialog = new GlycosylViewDialog();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public GlycosylViewDialog() {
		setBounds(100, 100, 677, 510);
		this.count = new int[6];
		
		JPanel listPanel = new JPanel();
		listPanel.setBorder(new TitledBorder(null, "Monosaccharide list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JPanel addPanel = new JPanel();
		addPanel.setBorder(new TitledBorder(null, "Add new monosaccharide", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		this.btnClose = new JButton("Close");

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(listPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(addPanel, GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(534, Short.MAX_VALUE)
					.addComponent(btnClose)
					.addGap(38))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(addPanel, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
						.addComponent(listPanel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 424, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
					.addComponent(btnClose)
					.addContainerGap())
		);
		
		JLabel label = new JLabel("S");
		
		textField_S = new JTextField();
		textField_S.setText("0");
		textField_S.setColumns(10);
		textField_S.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_S.getText(), 5);
			}
		});
		
		JLabel label_1 = new JLabel("P");
		
		textField_P = new JTextField();
		textField_P.setText("0");
		textField_P.setColumns(10);
		textField_P.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_P.getText(), 4);
			}
		});
		
		JLabel label_2 = new JLabel("N");
		
		textField_N = new JTextField();
		textField_N.setText("0");
		textField_N.setColumns(10);
		textField_N.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_N.getText(), 3);
			}
		});
		
		JLabel label_3 = new JLabel("O");
		
		textField_O = new JTextField();
		textField_O.setText("0");
		textField_O.setColumns(10);
		textField_O.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_O.getText(), 2);
			}
		});
		
		JLabel label_4 = new JLabel("H");
		
		textField_H = new JTextField();
		textField_H.setText("0");
		textField_H.setColumns(10);
		textField_H.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_H.getText(), 1);
			}
		});
		
		JLabel label_5 = new JLabel("C");
		
		textField_C = new JTextField();
		textField_C.setText("0");
		textField_C.setColumns(10);
		textField_C.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				update();
			}
			
			public void update(){
				GlycosylViewDialog.this.setValue(textField_C.getText(), 0);
			}
		});
		
		JLabel label_6 = new JLabel("Full name");
		
		JLabel label_7 = new JLabel("Short name");
		
		JLabel label_8 = new JLabel("Mono mass");
		
		JLabel label_9 = new JLabel("Avg mass");
		
		JButton buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String shortName = GlycosylViewDialog.this.textField_SN.getText();
				String fullName = GlycosylViewDialog.this.textField_FN.getText();
				
				if (shortName.length() == 0) {
					JOptionPane.showMessageDialog(GlycosylViewDialog.this, "Short name cannot be null.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (fullName.length() == 0) {
					JOptionPane.showMessageDialog(GlycosylViewDialog.this, "Full name cannot be null.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (monoMass == 0 || aveMass == 0) {
					JOptionPane.showMessageDialog(GlycosylViewDialog.this, "Mass cannot be 0.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				int result = JOptionPane.showConfirmDialog(null,
						"Do you want to add \"" + shortName + "\" to the monosaccharide list? ", null,
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					Glycosyl glycosyl = new Glycosyl(shortName, fullName, count, monoMass, aveMass, "", -1);
					try {
						GlycosylDBManager.add(glycosyl);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						JOptionPane.showMessageDialog(GlycosylViewDialog.this, e1.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					try {
						GlycosylViewDialog.this.loadGlycosyl();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					GlycosylViewDialog.this.repaint();
				} else {
					return;
				}
			}
		});
		
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycosylViewDialog.this.textField_C.setText("0");
				GlycosylViewDialog.this.textField_H.setText("0");
				GlycosylViewDialog.this.textField_O.setText("0");
				GlycosylViewDialog.this.textField_N.setText("0");
				GlycosylViewDialog.this.textField_P.setText("0");
				GlycosylViewDialog.this.textField_S.setText("0");
				GlycosylViewDialog.this.textField_MM.setText("0");
				GlycosylViewDialog.this.textField_AM.setText("0");
				GlycosylViewDialog.this.textField_SN.setText("");
				GlycosylViewDialog.this.textField_FN.setText("");
			}
		});
		
		textField_AM = new JTextField();
		textField_AM.setEnabled(false);
		textField_AM.setEditable(false);
		textField_AM.setColumns(10);
		
		textField_MM = new JTextField();
		textField_MM.setEnabled(false);
		textField_MM.setEditable(false);
		textField_MM.setColumns(10);
		
		textField_SN = new JTextField();
		textField_SN.setColumns(10);
		
		textField_FN = new JTextField();
		textField_FN.setColumns(10);
		GroupLayout gl_addPanel = new GroupLayout(addPanel);
		gl_addPanel.setHorizontalGroup(
			gl_addPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_addPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 7, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_C, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 7, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_H, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 8, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_O, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 7, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_N, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 6, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_P, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 6, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(textField_S, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
							.addGap(38)
							.addComponent(textField_FN, 0, 0, Short.MAX_VALUE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
							.addGap(28)
							.addComponent(textField_SN, 0, 0, Short.MAX_VALUE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
							.addGap(30)
							.addComponent(textField_MM, 0, 0, Short.MAX_VALUE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(label_9)
							.addGap(37)
							.addComponent(textField_AM, 0, 0, Short.MAX_VALUE))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addComponent(buttonAdd, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(buttonReset, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(28, Short.MAX_VALUE))
		);
		gl_addPanel.setVerticalGroup(
			gl_addPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_addPanel.createSequentialGroup()
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_addPanel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_addPanel.createSequentialGroup()
									.addGap(3)
									.addComponent(label_5))
								.addComponent(textField_C, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(label_4)
								.addComponent(textField_H, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(label_3)
								.addComponent(textField_O, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(label_2)
								.addComponent(textField_N, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(label_1)
								.addComponent(textField_P, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18)
							.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(label)
								.addComponent(textField_S, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(21)
							.addComponent(label_6))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(239)
							.addComponent(textField_FN, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(14)
							.addComponent(label_7))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(textField_SN, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(14)
							.addComponent(label_8))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(textField_MM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(14)
							.addComponent(label_9))
						.addGroup(gl_addPanel.createSequentialGroup()
							.addGap(11)
							.addComponent(textField_AM, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(18)
					.addGroup(gl_addPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(buttonAdd)
						.addComponent(buttonReset))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		addPanel.setLayout(gl_addPanel);
		
		this.scrollPane = new JScrollPane();
		try {
			loadGlycosyl();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<Glycosyl> list = new ArrayList<Glycosyl>();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < glycosyls.length; i++) {
					if (selected[i]) {
						sb.append("\"").append(glycosyls[i].getTitle()).append("\"");
						list.add(glycosyls[i]);
					}
				}

				int result = JOptionPane.showConfirmDialog(null,
						"Do you want to delete the following monosaccharide(s) " + sb + "? ", null,
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					for (int i = 0; i < glycosyls.length; i++) {
						if (selected[i]) {
							sb.append("\"").append(glycosyls[i].getTitle()).append("\"");
						}
					}
					Glycosyl[] removed = list.toArray(new Glycosyl[list.size()]);
					GlycosylDBManager.remove(removed);
					try {
						GlycosylViewDialog.this.loadGlycosyl();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					GlycosylViewDialog.this.repaint();
				} else {
					return;
				}
			}
		});
		btnDelete.setEnabled(false);
		
		GroupLayout gl_listPanel = new GroupLayout(listPanel);
		gl_listPanel.setHorizontalGroup(
			gl_listPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_listPanel.createSequentialGroup()
					.addContainerGap(367, Short.MAX_VALUE)
					.addComponent(btnDelete, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(Alignment.LEADING, gl_listPanel.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 430, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_listPanel.setVerticalGroup(
			gl_listPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_listPanel.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 353, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnDelete)
					.addContainerGap(44, Short.MAX_VALUE))
		);
		listPanel.setLayout(gl_listPanel);
		getContentPane().setLayout(groupLayout);

	}
	
	private void setValue(String text, int id) {
		if (text.length() == 0)
			return;
		for (int i = 0; i < text.length(); i++) {
			if (Character.digit(text.charAt(i), 10) < 0)
				return;
		}

		this.count[id] = Integer.parseInt(text);
		this.monoMass = 0;
		this.aveMass = 0;
		for(int i=0;i<count.length;i++){
			monoMass += this.mono[i] * count[i];
			aveMass += this.ave[i] * count[i];
		}
		
		this.textField_MM.setText(FormatTool.df4.format(monoMass));
		this.textField_AM.setText(FormatTool.df4.format(aveMass));
	}
	
	private void loadGlycosyl() throws IOException {

		this.glycosyls = GlycosylDBManager.load();

		RowObject[] objs = new RowObject[glycosyls.length];
		for (int i = 0; i < glycosyls.length; i++) {
			objs[i] = glycosyls[i].getRowObject();
		}

		String[] title = new String[] { "Title", "Composition", "Mono mass", "Ave mass" };
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
					GlycosylViewDialog.this.selected[row] = selected;
					boolean se = false;
					for (int i = 0; i < GlycosylViewDialog.this.selected.length; i++) {
						if (GlycosylViewDialog.this.selected[i]) {
							se = true;
							break;
						}
					}
					if (se) {
						GlycosylViewDialog.this.btnDelete.setEnabled(true);
					} else {
						GlycosylViewDialog.this.btnDelete.setEnabled(false);
					}
				}
			}
		});
		
		this.glycosylTable = new JTable(this.model);
		this.scrollPane.setViewportView(glycosylTable);
	}
	
	public JButton getCloseButton() {
		return btnClose;
	}
}
