/**
 * 
 */
package bmi.med.uOttawa.metalab.core.prodb;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.awt.event.ActionEvent;

/**
 * @author Kai Cheng
 *
 */
public class FastaExtractFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	
	private File dir;
	
	private JButton btnStart;
	private JProgressBar progressBar;
	private AbstractButton rdbtnUniprot;
	private AbstractButton rdbtnDnaSequencing;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FastaExtractFrame frame = new FastaExtractFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FastaExtractFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow][]", "[30][30][30][30][30][30][30]"));

		JLabel lblDatabaseInput = new JLabel("Database input");
		contentPane.add(lblDatabaseInput, "cell 0 0,alignx trailing");

		textField = new JTextField();
		contentPane.add(textField, "cell 1 0,growx");
		textField.setColumns(10);

		JButton button1 = new JButton("...");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = dir == null ? new JFileChooser() : new JFileChooser(dir);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Protein sequence database (*.fasta)", "fasta"));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnValue = fileChooser.showOpenDialog(FastaExtractFrame.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
					dir = file.getParentFile();
				}
			}
		});
		contentPane.add(button1, "cell 2 0");

		JLabel lblDatabaseOutput = new JLabel("Database output");
		contentPane.add(lblDatabaseOutput, "cell 0 1,alignx trailing");

		textField_1 = new JTextField();
		contentPane.add(textField_1, "cell 1 1,growx");
		textField_1.setColumns(10);

		JButton button2 = new JButton("...");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = dir == null ? new JFileChooser() : new JFileChooser(dir);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Protein sequence database (*.fasta)", "fasta"));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnValue = fileChooser.showOpenDialog(FastaExtractFrame.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					textField_1.setText(file.getAbsolutePath());
				}
			}
		});
		contentPane.add(button2, "cell 2 1");

		JLabel lblProteinList = new JLabel("Protein list");
		contentPane.add(lblProteinList, "cell 0 2,alignx trailing");

		textField_2 = new JTextField();
		contentPane.add(textField_2, "cell 1 2,growx");
		textField_2.setColumns(10);

		JButton button3 = new JButton("...");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = dir == null ? new JFileChooser() : new JFileChooser(dir);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Protein list (*.txt)", "txt"));
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnValue = fileChooser.showOpenDialog(FastaExtractFrame.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					textField_2.setText(file.getAbsolutePath());
				}
			}
		});
		contentPane.add(button3, "cell 2 2");

		rdbtnUniprot = new JRadioButton("Uniprot");
		rdbtnUniprot.setSelected(true);
		contentPane.add(rdbtnUniprot, "flowx,cell 1 3");

		rdbtnDnaSequencing = new JRadioButton("DNA sequencing");
		contentPane.add(rdbtnDnaSequencing, "cell 1 3");

		ButtonGroup bg = new ButtonGroup();
		bg.add(rdbtnUniprot);
		bg.add(rdbtnDnaSequencing);

		progressBar = new JProgressBar();
		contentPane.add(progressBar, "cell 0 4 3 1,growx");

		btnStart = new JButton("Start");
		btnStart.setActionCommand("start");
		btnStart.addActionListener(this);
		contentPane.add(btnStart, "flowx,cell 1 6,growx");

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		contentPane.add(btnClose, "cell 1 6,growx");

	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		btnStart.setEnabled(false);
		progressBar.setIndeterminate(true);

		String fastain = textField.getText();
		String fastaout = textField_1.getText();
		String protein = textField_2.getText();

		int type = -1;
		if (rdbtnUniprot.isSelected()) {
			type = 0;
		}
		if (rdbtnDnaSequencing.isSelected()) {
			type = 1;
		}

		FastaExtractTask task = new FastaExtractTask(fastain, fastaout, protein, type) {
			public void done() {
				if (progressBar.isIndeterminate()) {
					progressBar.setIndeterminate(false);
				}

				btnStart.setEnabled(true);
				setCursor(null);
			}
		};
		task.execute();
		try {
			task.get();
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
