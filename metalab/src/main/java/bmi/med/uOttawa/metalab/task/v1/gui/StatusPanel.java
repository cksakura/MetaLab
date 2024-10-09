/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.DecimalFormat;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import bmi.med.uOttawa.metalab.core.tools.FormatTool;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.border.TitledBorder;

/**
 * @author Kai Cheng
 *
 */
public class StatusPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4849303268653275625L;

	private DecimalFormat dfTime = new DecimalFormat("00");
	private DecimalFormat dfper = FormatTool.getDFP2();

	private Timer timer;
	// private int counter = 0; // the duration
	private static final int delay = 1000; // every 1 second

	private JLabel lblTime;
	private JLabel labelCpuUsed;
	private JLabel labelMemoryUsed;

	/**
	 * Create the panel.
	 */
	public StatusPanel(String version) {
		setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JLabel lblRunTime = new JLabel("Running time:");

		lblTime = new JLabel(" ");

		JLabel lblCpu = new JLabel("CPU state:");

		labelCpuUsed = new JLabel(" ");

		JLabel lblMemory = new JLabel("Memory (used/max):");

		labelMemoryUsed = new JLabel(" ");

		setLayout(new MigLayout("", "[100:100][150:250][100:100][150:250][100:100][150:250][50:250:1220,grow][100]",
				"[40:40:40]"));
		JLabel lblVersion = new JLabel(version);
		lblVersion.setForeground(Color.GRAY);
		add(lblVersion, "cell 7 0,alignx right,growy");

		add(lblRunTime, "cell 0 0,alignx left,aligny center");
		add(lblTime, "cell 1 0,growx,aligny center");
		add(lblCpu, "cell 2 0,alignx left,aligny center");
		add(labelCpuUsed, "cell 3 0,growx,aligny center");
		add(lblMemory, "cell 4 0,alignx left,aligny center");
		add(labelMemoryUsed, "cell 5 0,growx,aligny center");
	}

	public void start() {
		
		ActionListener action = new ActionListener() {
			int counter = 0;

			@Override
			public void actionPerformed(ActionEvent event) {

				int h = counter / 3600;
				int m = (counter - h * 3600) / 60;
				int s = counter - h * 3600 - m * 60;

				lblTime.setText(dfTime.format(h) + ":" + dfTime.format(m) + ":" + dfTime.format(s));
				counter++;

				MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
				labelMemoryUsed.setText((memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024) + " MB/"
						+ memoryMXBean.getHeapMemoryUsage().getMax() / 1024 / 1024 + " MB");

				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				ObjectName name = null;
				AttributeList list = null;
				try {
					name = ObjectName.getInstance("java.lang:type=OperatingSystem");
					list = mbs.getAttributes(name, new String[] { "SystemCpuLoad" });

				} catch (MalformedObjectNameException | NullPointerException | InstanceNotFoundException
						| ReflectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!list.isEmpty()) {
					javax.management.Attribute att = (javax.management.Attribute) list.get(0);
					Double value = (Double) att.getValue();
					if (value > 0) {
						labelCpuUsed.setText(dfper.format(value));
					}
				}
			}
		};

		timer = new Timer(delay, action);
		timer.setInitialDelay(0);
		timer.start();
	}

	public void stop() {
		timer.stop();
		ActionListener[] als = timer.getActionListeners();
		for (int i = 0; i < als.length; i++) {
			timer.removeActionListener(als[i]);
		}

		String lblTimeString = lblTime.getText();
		String memory = labelMemoryUsed.getText();
		String cpu = labelCpuUsed.getText();

		lblTime = new JLabel();
		lblTime.setText(lblTimeString);
		lblTime.repaint();

		labelMemoryUsed = new JLabel();
		labelMemoryUsed.setText(memory);
		labelMemoryUsed.repaint();

		labelCpuUsed = new JLabel();
		labelCpuUsed.setText(cpu);
		labelCpuUsed.repaint();

		this.repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame();
				frame.setSize(600, 200);
				StatusPanel panel = new StatusPanel("2.0.0");
				frame.getContentPane().add(panel, BorderLayout.CENTER);
				panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				frame.setVisible(true);
				panel.start();
			}
		});
	}

}
