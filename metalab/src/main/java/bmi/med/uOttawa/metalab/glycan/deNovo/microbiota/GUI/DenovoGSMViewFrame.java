package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;

import bmi.med.uOttawa.basicTools.GUI.PagedTableModel;
import bmi.med.uOttawa.basicTools.GUI.RowObject;
import bmi.med.uOttawa.glyco.deNovo.microbiota.NovoGlycoCompMatch;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoDrawer;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoReader;
import bmi.med.uOttawa.spectra.Peak;

import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.ScrollPaneConstants;
import java.awt.FlowLayout;

public class DenovoGSMViewFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8575916926747132601L;
	private static int rowCount = 30;
	private JTable table;
	private JLabel labelTotal;
	private JLabel labelOne;
	private JPanel spectraPanel;
	
	private int totalPageCount;
	private int totalItemCount;
	private int currentPage;
	
	private JComboBox<Integer> comboBoxPage;
	private JButton btnFirst;
	private JButton btnPrevious;
	private JButton btnNext;
	private JButton btnEnd;
	private PagedTableModel model;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DenovoGSMViewFrame frame = new DenovoGSMViewFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public DenovoGSMViewFrame() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JLabel lblShow = new JLabel("Showing");
		
		JLabel lblOf = new JLabel("of");
		
		this.labelTotal = new JLabel("");
		
		this.labelOne = new JLabel("");
		
		JButton btnFirst = new JButton("<<");
		btnFirst.setVerticalAlignment(SwingConstants.BOTTOM);
		btnFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JButton btnPrevious = new JButton("<");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JButton btnNext = new JButton(">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JButton btnEnd = new JButton(">>");
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JLabel lblGoToPage = new JLabel("Go to page");
		
		JComboBox comboBoxPage = new JComboBox();
		comboBoxPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		comboBoxPage.setModel(new DefaultComboBoxModel(new Integer[] {1}));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab("New tab", null, scrollPane, null);
		
		table = new JTable();
		scrollPane.setColumnHeaderView(table);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 784, GroupLayout.PREFERRED_SIZE)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(btnFirst)
					.addGap(6)
					.addComponent(btnPrevious, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(btnEnd)
					.addGap(26)
					.addComponent(lblGoToPage, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addGap(4)
					.addComponent(comboBoxPage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(293)
							.addComponent(labelOne, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
							.addGap(6))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblShow)
							.addGap(104)))
					.addComponent(lblOf, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addComponent(labelTotal, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 503, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnFirst)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(btnPrevious))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(btnNext))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(btnEnd))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addComponent(lblGoToPage))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addComponent(comboBoxPage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(labelOne)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblOf)
								.addComponent(lblShow)))
						.addComponent(labelTotal)))
		);
		
		this.spectraPanel = new JPanel();
		tabbedPane.addTab("Spectrum", null, spectraPanel, null);
		JFreeChart chart = GlycoMicroNovoDrawer.drawBlank();
		ChartPanel cp = new ChartPanel(chart);
		FlowLayout flowLayout = (FlowLayout) cp.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		GroupLayout gl_spectraPanel = new GroupLayout(spectraPanel);
		gl_spectraPanel.setHorizontalGroup(
			gl_spectraPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_spectraPanel.createSequentialGroup()
					.addComponent(cp, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_spectraPanel.setVerticalGroup(
			gl_spectraPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_spectraPanel.createSequentialGroup()
					.addComponent(cp, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
					.addGap(11))
		);
		spectraPanel.setLayout(gl_spectraPanel);
		
		
		getContentPane().setLayout(groupLayout);
		
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public DenovoGSMViewFrame(String in) throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		GlycoMicroNovoReader reader = new GlycoMicroNovoReader(in);
		reader.read();
		NovoGlycoCompMatch[] matches = reader.getDeterminedMatches();
		RowObject[] objects = new RowObject[matches.length];
		
		for(int i=0;i<matches.length;i++){
			objects[i] = matches[i].getRowObject();
		}	

		this.currentPage = 1;
		this.totalPageCount = objects.length / rowCount + 1;
		this.totalItemCount = objects.length;
		
		this.model = new PagedTableModel(reader.getTitle(), objects, currentPage, rowCount);

		Integer[] pages = new Integer[totalPageCount];
		for (int i = 0; i < pages.length; i++) {
			pages[i] = i + 1;
		}	

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JLabel lblShow = new JLabel("Showing");
		
		JLabel lblOf = new JLabel("of");
		
		this.labelTotal = new JLabel(String.valueOf(matches.length));
		
		this.labelOne = new JLabel("");
		
		this.btnFirst = new JButton("<<");
		btnFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DenovoGSMViewFrame.this.currentPage = 1;
				DenovoGSMViewFrame.this.selectPage(DenovoGSMViewFrame.this.currentPage);
			}
		});
		
		this.btnPrevious = new JButton("<");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DenovoGSMViewFrame.this.selectPage(--DenovoGSMViewFrame.this.currentPage);
			}
		});
		
		this.btnNext = new JButton(">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DenovoGSMViewFrame.this.selectPage(++DenovoGSMViewFrame.this.currentPage);
			}
		});
		
		this.btnEnd = new JButton(">>");
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DenovoGSMViewFrame.this.currentPage = DenovoGSMViewFrame.this.totalPageCount;
				DenovoGSMViewFrame.this.selectPage(DenovoGSMViewFrame.this.currentPage);
			}
		});
		
		JLabel lblGoToPage = new JLabel("Go to page");
		
		this.comboBoxPage = new JComboBox<Integer>();
		comboBoxPage.setModel(new DefaultComboBoxModel<Integer>(pages));
		comboBoxPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DenovoGSMViewFrame.this.currentPage = DenovoGSMViewFrame.this.comboBoxPage.getSelectedIndex() + 1;
				DenovoGSMViewFrame.this.selectPage(DenovoGSMViewFrame.this.currentPage);
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnFirst)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnPrevious, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnEnd, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(26)
					.addComponent(lblGoToPage, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(comboBoxPage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 150, Short.MAX_VALUE)
					.addComponent(lblShow)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelOne, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOf, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelTotal, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addGap(31))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 503, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblShow)
						.addComponent(lblOf)
						.addComponent(labelOne)
						.addComponent(labelTotal)
						.addComponent(btnFirst)
						.addComponent(btnPrevious)
						.addComponent(btnNext)
						.addComponent(btnEnd)
						.addComponent(lblGoToPage)
						.addComponent(comboBoxPage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(26, Short.MAX_VALUE))
		);
		
		this.selectPage(1);
		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Glycan composition", null, scrollPane, null);
		
		this.spectraPanel = new JPanel();
		tabbedPane.addTab("Spectrum", null, spectraPanel, null);
		JFreeChart chart = GlycoMicroNovoDrawer.drawBlank();
		ChartPanel cp = new ChartPanel(chart);
		GroupLayout gl_spectraPanel = new GroupLayout(spectraPanel);
		gl_spectraPanel.setHorizontalGroup(
				gl_spectraPanel.createParallelGroup(Alignment.TRAILING).addGroup(gl_spectraPanel.createSequentialGroup()
						.addComponent(cp, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)));
		gl_spectraPanel.setVerticalGroup(gl_spectraPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spectraPanel
				.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)));
		spectraPanel.setLayout(gl_spectraPanel);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				spectraPanel.remove(0);
				int selectedRow = table.getSelectedRow();
				int id = (currentPage - 1) * rowCount + selectedRow;
				Peak[] peaks = matches[id].getPeaks();
				JFreeChart chart = GlycoMicroNovoDrawer.createXYBarChart(peaks, matches[id].getNodeMap());
				ChartPanel cp = new ChartPanel(chart);
				GroupLayout gl_spectraPanel = new GroupLayout(spectraPanel);
				gl_spectraPanel.setHorizontalGroup(
						gl_spectraPanel.createParallelGroup(Alignment.TRAILING).addGroup(gl_spectraPanel.createSequentialGroup()
								.addComponent(cp, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)));
				gl_spectraPanel.setVerticalGroup(gl_spectraPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spectraPanel
						.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)));
				spectraPanel.setLayout(gl_spectraPanel);
			}});
		setExtendedState(MAXIMIZED_BOTH);
		getContentPane().setLayout(groupLayout);
	}
	
	private void selectPage(int page) {
		this.model.setCurrentPage(page);
		this.comboBoxPage.setSelectedItem(page);
		StringBuilder sb = new StringBuilder();
		if (page == 1) {
			if (page == this.totalPageCount) {
				sb.append("1").append("-").append(this.totalItemCount);
				this.btnFirst.setEnabled(false);
				this.btnPrevious.setEnabled(false);
				this.btnNext.setEnabled(false);
				this.btnEnd.setEnabled(false);
			} else {
				sb.append("1").append("-").append(rowCount);
				this.btnFirst.setEnabled(false);
				this.btnPrevious.setEnabled(false);
				this.btnNext.setEnabled(true);
				this.btnEnd.setEnabled(true);
			}
		} else if (page == this.totalPageCount) {
			sb.append((page - 1) * rowCount + 1).append("-").append(this.totalItemCount);
			this.btnFirst.setEnabled(true);
			this.btnPrevious.setEnabled(true);
			this.btnNext.setEnabled(false);
			this.btnEnd.setEnabled(false);
		} else {
			sb.append((page - 1) * rowCount + 1).append("-").append(page * rowCount);
			this.btnFirst.setEnabled(true);
			this.btnPrevious.setEnabled(true);
			this.btnNext.setEnabled(true);
			this.btnEnd.setEnabled(true);
		}
		this.labelOne.setText(sb.toString());
		this.repaint();
	}
}
