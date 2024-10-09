/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import javax.swing.border.TitledBorder;

/**
 * @author Kai Cheng
 *
 */
public class WorkflowPanel2 extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6657991377378191913L;
	
	private JCheckBox chckbxStep1;
	private JCheckBox chckbxStep2;
	private JCheckBox chckbxStep3;
	private JCheckBox chckbxStep4;
	
	/**
	 * Create the panel.
	 */
	public WorkflowPanel2() {
		setBorder(new TitledBorder(null, "Workflow", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[120,grow 150][30]", "[120][120][120][120]"));

		JPanel panelStep1 = new JPanel();
		panelStep1.setBackground(new Color(135, 206, 255));
		add(panelStep1, "cell 0 0,growx");
		panelStep1.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep1 = new JLabel("<html><b>Step 1:</b><p>Sample specific database construction</p></html>");
		panelStep1.add(lblStep1, "cell 0 0");

		JPanel panelStep2 = new JPanel();
		panelStep2.setBackground(new Color(126, 192, 238));
		add(panelStep2, "cell 0 1,growx");
		panelStep2.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep2 = new JLabel("<html><b>Step 2:</b><p>Peptide identification/quantification</p></html>");
		panelStep2.add(lblStep2, "cell 0 0");

		JPanel panelStep3 = new JPanel();
		panelStep3.setBackground(new Color(108, 166, 205));
		add(panelStep3, "cell 0 2,growx");
		panelStep3.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep3 = new JLabel("<html><b>Step 3:</b><p>Taxonomy analysis</p></html>");
		panelStep3.add(lblStep3, "cell 0 0");

		JPanel panelStep4 = new JPanel();
		panelStep4.setBackground(new Color(108, 166, 205));
		add(panelStep4, "cell 0 3,growx");
		panelStep4.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep4 = new JLabel("<html><b>Step 4:</b><p>Functional annotation</p></html>");
		panelStep4.add(lblStep4, "cell 0 0");

		this.chckbxStep1 = new JCheckBox("");
		chckbxStep1.setBackground(new Color(135, 206, 255));
		add(chckbxStep1, "cell 1 0,alignx center");

		this.chckbxStep2 = new JCheckBox("");
		chckbxStep2.setBackground(new Color(126, 192, 238));
		add(chckbxStep2, "cell 1 1,alignx center");

		this.chckbxStep3 = new JCheckBox("");
		chckbxStep3.setBackground(new Color(108, 166, 205));
		add(chckbxStep3, "cell 1 2,alignx center");

		this.chckbxStep4 = new JCheckBox("");
		chckbxStep4.setBackground(new Color(108, 166, 205));
		add(chckbxStep4, "cell 1 3,alignx center");
	}
	
	/**
	 * Create the panel.
	 */
	public WorkflowPanel2(boolean[] workflow) {
		setBorder(new TitledBorder(null, "Workflow", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new MigLayout("", "[120,grow 150][30]", "[120][120][120][120]"));

		JPanel panelStep1 = new JPanel();
		panelStep1.setBackground(new Color(135, 206, 255));
		add(panelStep1, "cell 0 0,growx");
		panelStep1.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep1 = new JLabel("<html><b>Step 1:</b><p>Sample specific database construction</p></html>");
		panelStep1.add(lblStep1, "cell 0 0");

		JPanel panelStep2 = new JPanel();
		panelStep2.setBackground(new Color(126, 192, 238));
		add(panelStep2, "cell 0 1,growx");
		panelStep2.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep2 = new JLabel("<html><b>Step 2:</b><p>Peptide identification/quantification</p></html>");
		panelStep2.add(lblStep2, "cell 0 0");

		JPanel panelStep3 = new JPanel();
		panelStep3.setBackground(new Color(108, 166, 205));
		add(panelStep3, "cell 0 2,growx");
		panelStep3.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep3 = new JLabel("<html><b>Step 3:</b><p>Taxonomy analysis</p></html>");
		panelStep3.add(lblStep3, "cell 0 0");

		JPanel panelStep4 = new JPanel();
		panelStep4.setBackground(new Color(108, 166, 205));
		add(panelStep4, "cell 0 3,growx");
		panelStep4.setLayout(new MigLayout("", "[150]", "[120]"));

		JLabel lblStep4 = new JLabel("<html><b>Step 4:</b><p>Functional annotation</p></html>");
		panelStep4.add(lblStep4, "cell 0 0");

		this.chckbxStep1 = new JCheckBox("");
		chckbxStep1.setBackground(new Color(135, 206, 255));
		chckbxStep1.setSelected(workflow[0]);
		add(chckbxStep1, "cell 1 0,alignx center");

		this.chckbxStep2 = new JCheckBox("");
		chckbxStep2.setBackground(new Color(126, 192, 238));
		chckbxStep2.setSelected(workflow[1]);
		add(chckbxStep2, "cell 1 1,alignx center");

		this.chckbxStep3 = new JCheckBox("");
		chckbxStep3.setBackground(new Color(108, 166, 205));
		chckbxStep3.setSelected(workflow[2]);
		add(chckbxStep3, "cell 1 2,alignx center");

		this.chckbxStep4 = new JCheckBox("");
		chckbxStep4.setBackground(new Color(108, 166, 205));
		chckbxStep4.setSelected(workflow[3]);
		add(chckbxStep4, "cell 1 3,alignx center");
	}
	
	public JCheckBox getChckbxStep1() {
		return chckbxStep1;
	}

	public JCheckBox getChckbxStep2() {
		return chckbxStep2;
	}

	public JCheckBox getChckbxStep3() {
		return chckbxStep3;
	}

	public JCheckBox getChckbxStep4() {
		return chckbxStep4;
	}

	public boolean[] getWorkflow() {
		return new boolean[] { chckbxStep1.isSelected(), chckbxStep2.isSelected(), chckbxStep3.isSelected(),
				chckbxStep4.isSelected() };
	}

}
