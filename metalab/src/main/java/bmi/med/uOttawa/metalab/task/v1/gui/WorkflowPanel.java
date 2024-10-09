/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import java.awt.Color;

/**
 * @author Kai Cheng
 *
 */
public class WorkflowPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1704010324919474175L;
	private JCheckBox chckbxStep1;
	private JCheckBox chckbxStep2;
	private JCheckBox chckbxStep3;
	private JCheckBox chckbxStep4;

	/**
	 * Create the panel.
	 */
	public WorkflowPanel() {
		setLayout(new MigLayout("", "[230][230][230][230]", "[120,grow][30]"));

		JPanel panelStep1 = new JPanel();
		panelStep1.setBackground(new Color(135, 206, 255));
		add(panelStep1, "cell 0 0,growx");
		panelStep1.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep1 = new JLabel("<html><b>Step 1:</b><p>Sample specific database construction</p></html>");
		panelStep1.add(lblStep1, "cell 0 0");

		JPanel panelStep2 = new JPanel();
		panelStep2.setBackground(new Color(126, 192, 238));
		add(panelStep2, "cell 1 0,growx");
		panelStep2.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep2 = new JLabel("<html><b>Step 2:</b><p>Peptide identification/quantification</p></html>");
		panelStep2.add(lblStep2, "cell 0 0");

		JPanel panelStep3 = new JPanel();
		panelStep3.setBackground(new Color(108, 166, 205));
		add(panelStep3, "cell 2 0,growx");
		panelStep3.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep3 = new JLabel("<html><b>Step 3-1:</b><p>Post-analysis (taxonomy)</p></html>");
		panelStep3.add(lblStep3, "cell 0 0");

		JPanel panelStep4 = new JPanel();
		panelStep4.setBackground(new Color(108, 166, 205));
		add(panelStep4, "cell 3 0,growx");
		panelStep4.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep4 = new JLabel("<html><b>Step 3-2:</b><p>Post-analysis (function)</p></html>");
		panelStep4.add(lblStep4, "cell 0 0");

		this.chckbxStep1 = new JCheckBox("");
		chckbxStep1.setBackground(new Color(135, 206, 255));
		add(chckbxStep1, "cell 0 1,alignx center");

		this.chckbxStep2 = new JCheckBox("");
		chckbxStep2.setBackground(new Color(126, 192, 238));
		add(chckbxStep2, "cell 1 1,alignx center");

		this.chckbxStep3 = new JCheckBox("");
		chckbxStep3.setBackground(new Color(108, 166, 205));
		add(chckbxStep3, "cell 2 1,alignx center");

		this.chckbxStep4 = new JCheckBox("");
		chckbxStep4.setBackground(new Color(108, 166, 205));
		add(chckbxStep4, "cell 3 1,alignx center");
	}

	public JCheckBox getChckbxStep1() {
		return chckbxStep1;
	}

	public JCheckBox getChckbxStep2() {
		return chckbxStep2;
	}

	public JCheckBox getChckbxStep31() {
		return chckbxStep3;
	}

	public JCheckBox getChckbxStep32() {
		return chckbxStep4;
	}

	public boolean[] getWorkflow() {
		return new boolean[] { chckbxStep1.isSelected(), chckbxStep2.isSelected(), chckbxStep3.isSelected(),
				chckbxStep4.isSelected() };
	}
}
