/**
 * 
 */
package bmi.med.uOttawa.metalab.core.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * @author Kai Cheng
 *
 */
public class ConsoleTextArea extends JTextArea {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6003628266173146889L;

	public ConsoleTextArea(InputStream[] inStreams) {
		this.setEditable(false);
		for (int i = 0; i < inStreams.length; ++i)
			startConsoleReaderThread(inStreams[i]);
	} // ConsoleTextArea()

	public ConsoleTextArea() throws IOException {
		LoopedStreams ls = new LoopedStreams();
		this.setEditable(false);
		// System.out System.err
		PrintStream ps = new PrintStream(ls.getOutputStream());
		System.setOut(ps);
		System.setErr(ps);

		startConsoleReaderThread(ls.getInputStream());
	} // ConsoleTextArea()

	private void startConsoleReaderThread(InputStream inStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		new Thread(new Runnable() {
			public void run() {
				StringBuffer sb = new StringBuffer();
				try {
					String s;
					Document doc = getDocument();
					while ((s = br.readLine()) != null) {
						boolean caretAtEnd = false;
						caretAtEnd = getCaretPosition() == doc.getLength() ? true : false;
						sb.setLength(0);
						append(sb.append(s).append('\n').toString());
						if (caretAtEnd)
							setCaretPosition(doc.getLength());
					}
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Read error from BufferedReader:" + e);
//					System.exit(1);
				}
			}
		}).start();
	} // startConsoleReaderThread()

	// Test
	public static void main(String[] args) {
		JFrame f = new JFrame("ConsoleTextArea Test");
		ConsoleTextArea consoleTextArea = null;

		try {
			consoleTextArea = new ConsoleTextArea();
		} catch (IOException e) {
			System.err.println("Cannot construct LoopedStreams:" + e);
			System.exit(1);
			return;
		}

		consoleTextArea.setFont(java.awt.Font.decode("monospaced"));
		f.getContentPane().add(new JScrollPane(consoleTextArea), java.awt.BorderLayout.CENTER);
		f.setBounds(50, 50, 300, 300);
		f.setVisible(true);

		f.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
//				System.exit(0);
			}
		});

		// System.out and System.err out
		startWriterTestThread("Write thread #1", System.err, 920, 50);
		startWriterTestThread("Write thread #2", System.out, 500, 50);
		startWriterTestThread("Write thread #3", System.out, 200, 50);
		startWriterTestThread("Write thread #4", System.out, 1000, 50);
		startWriterTestThread("Write thread #5", System.err, 850, 50);
	} // main()

	private static void startWriterTestThread(final String name, final PrintStream ps, final int delay,
			final int count) {
		new Thread(new Runnable() {
			public void run() {
				for (int i = 1; i <= count; ++i) {
					ps.println("***" + name + ", hello !, i=" + i);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	} // startWriterTestThread()

}
