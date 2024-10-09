package bmi.med.uOttawa.metalab.glycan.henghui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

import java.io.File;
import java.util.regex.Pattern;

public class GlycoSelectFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4290173614077078021L;
	private JPanel contentPane;
	private JTextField textFieldInput;
	private JTextField textFieldOutput;
	private JTextField textFieldDatabase;

	private Pattern pattern0 = Pattern.compile("N[A-Z][ST]");
	private Pattern pattern1 = Pattern.compile("N[A-Z][STC]");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					GlycoSelectFrame frame = new GlycoSelectFrame();
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
	public GlycoSelectFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[50][150,grow][50]", "[30][30][30][30][30][][]"));

		JLabel lblInput = new JLabel("Input");
		contentPane.add(lblInput, "cell 0 0,alignx leading");

		textFieldInput = new JTextField();
		contentPane.add(textFieldInput, "cell 1 0,growx");
		textFieldInput.setColumns(10);

		JButton buttonInput = new JButton("...");
		buttonInput.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(GlycoSelectFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				textFieldInput.setText(file.getAbsolutePath());
				String path = file.getAbsolutePath();
				String outpath = path.substring(0, path.length() - 4) + "_glycopeptide.txt";
				textFieldOutput.setText(outpath);
			}
		});
		contentPane.add(buttonInput, "cell 2 0,alignx trailing");

		JLabel lblOutput = new JLabel("Output");
		contentPane.add(lblOutput, "cell 0 1,alignx leading");

		textFieldOutput = new JTextField();
		contentPane.add(textFieldOutput, "cell 1 1,growx");
		textFieldOutput.setColumns(10);

		JButton buttonOutput = new JButton("...");
		buttonOutput.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(GlycoSelectFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				textFieldOutput.setText(file.getAbsolutePath());
			}
		});
		contentPane.add(buttonOutput, "cell 2 1,alignx trailing");

		JLabel lblDatabase = new JLabel("Database");
		contentPane.add(lblDatabase, "cell 0 2,alignx leading");

		textFieldDatabase = new JTextField();
		contentPane.add(textFieldDatabase, "cell 1 2,growx");
		textFieldDatabase.setColumns(10);

		JButton buttonDatabase = new JButton("...");
		buttonDatabase.addActionListener(l -> {

			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(GlycoSelectFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				textFieldDatabase.setText(file.getAbsolutePath());
			}
		});
		contentPane.add(buttonDatabase, "cell 2 2,alignx trailing");

		JLabel lblPattern = new JLabel("Pattern");
		contentPane.add(lblPattern, "cell 0 4,alignx leading");

		JComboBox<Pattern> comboBoxPattern = new JComboBox<Pattern>();
		comboBoxPattern.addItem(pattern0);
		comboBoxPattern.addItem(pattern1);
		contentPane.add(comboBoxPattern, "cell 1 4,growx");

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(l -> {
			String input = textFieldInput.getText();
			String output = textFieldOutput.getText();
			String database = textFieldDatabase.getText();
			Pattern pattern = comboBoxPattern.getItemAt(comboBoxPattern.getSelectedIndex());

			GlycoSelectTask task = new GlycoSelectTask(input, output, database, pattern);
			task.run();
		});
		contentPane.add(btnStart, "cell 2 6");
	}

}
