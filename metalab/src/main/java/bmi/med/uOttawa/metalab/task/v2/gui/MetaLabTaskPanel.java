/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v2.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JProgressBar;

import javax.swing.JButton;

/**
 * @author Kai Cheng
 *
 */
public class MetaLabTaskPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2319517031728955276L;

	private JProgressBar progressBar;
	private JProgressBar progressBar_1;
	private JButton btnRun;

	/**
	 * Create the panel.
	 */
	public MetaLabTaskPanel() {
		setLayout(new MigLayout("", "[600]", "[30][30][30]"));

		progressBar = new JProgressBar();
		add(progressBar, "cell 0 0,growx,aligny center");

		progressBar_1 = new JProgressBar();
		add(progressBar_1, "cell 0 1,growx,aligny center");

		btnRun = new JButton("Run");
		add(btnRun, "cell 0 2,alignx center,aligny center");
	}

	public JButton getRunButton() {
		return btnRun;
	}

	public JProgressBar getProgressBar1() {
		return progressBar;
	}

	public JProgressBar getProgressBar2() {
		return progressBar_1;
	}
}
