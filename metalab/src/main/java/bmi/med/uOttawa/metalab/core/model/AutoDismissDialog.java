/**
 * 
 */
package bmi.med.uOttawa.metalab.core.model;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * @author Kai Cheng
 *
 */
public class AutoDismissDialog implements ActionListener {

	private JDialog dialog;

	private AutoDismissDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		dialog.dispose();
	}

	public static void showMessageDialog(Component parentComponent, String message, String title,
			int delayInMilliseconds) {
		final JOptionPane optionPane = new JOptionPane(message);
		final JDialog dialog = optionPane.createDialog(parentComponent, title);
		dialog.setTitle(title);
		Timer timer = new Timer(delayInMilliseconds, new AutoDismissDialog(dialog));
		timer.setRepeats(false);
		timer.start();
		if (dialog.isDisplayable()) {
			dialog.setVisible(true);
		}
	}
}
