package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.stream.XMLStreamException;

import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.awt.event.ActionEvent;

public class GlycoMicroNovoMDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2730018044930787707L;
	private DenovoParaPanel denovoParaPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			GlycoMicroNovoMDialog dialog = new GlycoMicroNovoMDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public GlycoMicroNovoMDialog() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		setBounds(100, 100, 720, 670);
		{
			denovoParaPanel = new DenovoParaPanel();
		}
		
		JButton btnStartButton = new JButton("Start");
		btnStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					GlycoMicroNovoThread thread = new GlycoMicroNovoThread(denovoParaPanel.getTask(), denovoParaPanel.getProgressBar());
					thread.start();
					Thread.sleep(200);
//					GlycoMicroNovoMDialog.this.dispose();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (XMLStreamException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DataFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		});
		
		JButton btnCloseButton = new JButton("Close");
		btnCloseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoMDialog.this.dispose();
			}
		});
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(denovoParaPanel, GroupLayout.PREFERRED_SIZE, 704, GroupLayout.PREFERRED_SIZE)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(206)
					.addComponent(btnStartButton, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					.addGap(159)
					.addComponent(btnCloseButton, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(denovoParaPanel, GroupLayout.PREFERRED_SIZE, 588, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCloseButton)
						.addComponent(btnStartButton))
					.addGap(19))
		);
		getContentPane().setLayout(groupLayout);
	}
	
	private class GlycoMicroNovoThread extends Thread {

		private GlycoMicroNovoTask task;
		private JProgressBar bar;

		public GlycoMicroNovoThread(GlycoMicroNovoTask task, JProgressBar bar) {
			this.task = task;
			this.bar = bar;
		}

		public void run() {
			while (task.hasNext()) {
				task.processNext();
				bar.setStringPainted(true);
				bar.setValue(task.completePercentage());
			}
			try {
				task.complete();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bar.setString("Finish");
		}
	}
}
