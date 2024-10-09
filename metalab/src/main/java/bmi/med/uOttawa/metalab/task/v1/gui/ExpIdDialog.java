/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.PlainDocument;

import bmi.med.uOttawa.metalab.core.tools.IntegerTextFilter;
import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBox;

/**
 * @author Kai Cheng
 *
 */
public class ExpIdDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -859972317982018319L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox chckbxIdSuffix;
	private JLabel lblFirst;
	private JTextField textFieldFirst;
	private JLabel lblDifference;
	private JTextField textFieldDiff;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ExpIdDialog dialog = new ExpIdDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ExpIdDialog() {
		setBounds(100, 100, 380, 160);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[75][10][60][60][20][60][60]", "[30][30]"));

		JLabel lblExperiment = new JLabel("Experiment");
		contentPanel.add(lblExperiment, "cell 0 0,alignx left");

		textField = new JTextField();
		contentPanel.add(textField, "cell 2 0 5 1,growx,aligny center");
		textField.setColumns(10);

		chckbxIdSuffix = new JCheckBox("Id suffix");
		chckbxIdSuffix.addItemListener(l -> {
			if (chckbxIdSuffix.isSelected()) {
				lblFirst.setEnabled(true);
				textFieldFirst.setEnabled(true);
				lblDifference.setEnabled(true);
				textFieldDiff.setEnabled(true);
			} else {
				lblFirst.setEnabled(false);
				textFieldFirst.setEnabled(false);
				lblDifference.setEnabled(false);
				textFieldDiff.setEnabled(false);
			}
		});
		contentPanel.add(chckbxIdSuffix, "cell 0 1");

		lblFirst = new JLabel("First term");
		lblFirst.setEnabled(false);
		contentPanel.add(lblFirst, "cell 2 1,alignx left");

		textFieldFirst = new JTextField();
		textFieldFirst.setText("1");
		((PlainDocument) textFieldFirst.getDocument()).setDocumentFilter(new IntegerTextFilter());

		textFieldFirst.setEnabled(false);
		contentPanel.add(textFieldFirst, "cell 3 1,growx");
		textFieldFirst.setColumns(10);

		lblDifference = new JLabel("Difference");
		lblDifference.setEnabled(false);
		contentPanel.add(lblDifference, "cell 5 1,alignx trailing");

		textFieldDiff = new JTextField();
		textFieldDiff.setText("1");
		((PlainDocument) textFieldDiff.getDocument()).setDocumentFilter(new IntegerTextFilter());

		textFieldDiff.setEnabled(false);
		contentPanel.add(textFieldDiff, "cell 6 1,growx");
		textFieldDiff.setColumns(10);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);

		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

	public JButton getOkButton() {
		return okButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public String[] getExpNames(int number) {
		String[] names = new String[number];

		String prefix = this.textField.getText();
		if (chckbxIdSuffix.isSelected()) {
			String firstText = textFieldFirst.getText();
			String diffText = textFieldDiff.getText();

			if (firstText.length() == 0) {
				if (diffText.length() == 0) {
					Arrays.fill(names, prefix);
				} else {
					int difference = Integer.parseInt(diffText);
					for (int i = 0; i < names.length; i++) {
						names[i] = prefix + (difference * (i + 1));
					}
				}
			} else {
				if (diffText.length() == 0) {
					Arrays.fill(names, prefix + firstText);
				} else {
					int first = Integer.parseInt(firstText);
					int difference = Integer.parseInt(diffText);
					for (int i = 0; i < names.length; i++) {
						names[i] = prefix + (first + difference * i);
					}
				}
			}
		} else {
			Arrays.fill(names, prefix);
		}

		return names;
	}
}
