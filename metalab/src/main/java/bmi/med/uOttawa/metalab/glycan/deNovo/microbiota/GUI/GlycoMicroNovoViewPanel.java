package bmi.med.uOttawa.metalab.glycan.deNovo.microbiota.GUI;

import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import bmi.med.uOttawa.basicTools.FormatTool;
import bmi.med.uOttawa.basicTools.GUI.PagedTableModel;
import bmi.med.uOttawa.basicTools.GUI.RowObject;
import bmi.med.uOttawa.glyco.Glycosyl;
import bmi.med.uOttawa.glyco.deNovo.GlycoPeakNode;
import bmi.med.uOttawa.glyco.deNovo.microbiota.GlycoMicroNovoStat;
import bmi.med.uOttawa.glyco.deNovo.microbiota.NovoGlycoCompMatch;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoDrawer;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoReader;
import bmi.med.uOttawa.glyco.deNovo.microbiota.IO.GlycoMicroNovoXlsWriter;
import bmi.med.uOttawa.spectra.Peak;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;

public class GlycoMicroNovoViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8588695524332278597L;
	
	private static int rowCount = 25;
	private JScrollPane gsmScrollPane;
	private JLabel labelTotal;
	private JLabel labelOne;
	private JPanel spectrumPanel;
	
	private int totalPageCount;
	private int totalItemCount;
	private int currentPage;
	
	private JComboBox<Integer> comboBoxPage;
	private JButton btnFirst;
	private JButton btnPrevious;
	private JButton btnNext;
	private JButton btnEnd;
	private PagedTableModel gsmModel;
	private JTable glycoTable;
	private JTable gsmTable;
	private JTable ionTable;
	private JTable statTable;
	private JScrollPane glycoScrollPane;
	private JScrollPane statScrollPane;
	private JScrollPane ionScrollPane;
	
	private Glycosyl[] glycosyls;
	private NovoGlycoCompMatch[] determinedMatches;
	private NovoGlycoCompMatch[] undeterminedMatches;
	
	/**
	 * Create the panel.
	 */
	public GlycoMicroNovoViewPanel() {

		JPanel ionPanel = new JPanel();
		ionPanel.setBorder(new TitledBorder(null, "Ions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.ionScrollPane = new JScrollPane();

		this.spectrumPanel = new JPanel();
		spectrumPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Spectrum", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

		JPanel viewPanel = new JPanel();
		
		JPanel glycoPanel = new JPanel();
		glycoPanel.setBorder(new TitledBorder(null, "Monosaccharide list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.glycoScrollPane = new JScrollPane();
		
		JPanel statPanel = new JPanel();
		statPanel.setBorder(new TitledBorder(null, "Statistic information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(1)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(statPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(glycoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spectrumPanel, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(ionPanel, GroupLayout.PREFERRED_SIZE, 301, Short.MAX_VALUE))
				.addComponent(viewPanel, 0, 1000, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(viewPanel, GroupLayout.PREFERRED_SIZE, 540, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
							.addComponent(glycoPanel, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(statPanel, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
						.addComponent(spectrumPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
						.addComponent(ionPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
					.addGap(0))
		);
		
		this.statScrollPane = new JScrollPane();
		GroupLayout gl_statPanel = new GroupLayout(statPanel);
		gl_statPanel.setHorizontalGroup(
			gl_statPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(statScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
		);
		gl_statPanel.setVerticalGroup(
			gl_statPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(statScrollPane, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
		);
		
		statTable = new JTable();
		statScrollPane.setViewportView(statTable);
		statPanel.setLayout(gl_statPanel);
		GroupLayout gl_spectrumPanel = new GroupLayout(spectrumPanel);
		gl_spectrumPanel.setHorizontalGroup(
			gl_spectrumPanel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 84, Short.MAX_VALUE)
		);
		gl_spectrumPanel.setVerticalGroup(
			gl_spectrumPanel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 27, Short.MAX_VALUE)
		);
		spectrumPanel.setLayout(gl_spectrumPanel);

		this.gsmScrollPane = new JScrollPane();

		JLabel lblShow = new JLabel("Showing");

		JLabel lblOf = new JLabel("of");

		this.labelTotal = new JLabel();

		this.labelOne = new JLabel("");

		this.btnFirst = new JButton("<<");
		btnFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoViewPanel.this.currentPage = 1;
				GlycoMicroNovoViewPanel.this.selectPage(GlycoMicroNovoViewPanel.this.currentPage);
			}
		});

		this.btnPrevious = new JButton("<");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoViewPanel.this.selectPage(--GlycoMicroNovoViewPanel.this.currentPage);
			}
		});

		this.btnNext = new JButton(">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoViewPanel.this.selectPage(++GlycoMicroNovoViewPanel.this.currentPage);
			}
		});

		this.btnEnd = new JButton(">>");
		btnEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoViewPanel.this.currentPage = GlycoMicroNovoViewPanel.this.totalPageCount;
				GlycoMicroNovoViewPanel.this.selectPage(GlycoMicroNovoViewPanel.this.currentPage);
			}
		});

		JLabel lblGoToPage = new JLabel("Go to page");

		this.comboBoxPage = new JComboBox<Integer>();
		comboBoxPage.setModel(new DefaultComboBoxModel<Integer>(new Integer[] { 1 }));

		GroupLayout gl_viewPanel = new GroupLayout(viewPanel);
		gl_viewPanel.setHorizontalGroup(
			gl_viewPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_viewPanel.createSequentialGroup()
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
					.addPreferredGap(ComponentPlacement.RELATED, 463, Short.MAX_VALUE)
					.addComponent(lblShow)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelOne, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblOf, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labelTotal, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addGap(31))
				.addGroup(gl_viewPanel.createSequentialGroup()
					.addComponent(gsmScrollPane, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
					.addGap(0))
		);
		gl_viewPanel.setVerticalGroup(
			gl_viewPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_viewPanel.createSequentialGroup()
					.addComponent(gsmScrollPane, GroupLayout.PREFERRED_SIZE, 500, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_viewPanel.createParallelGroup(Alignment.BASELINE)
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
					.addGap(6))
		);

		glycoTable = new JTable();
		glycoScrollPane.setViewportView(glycoTable);
		GroupLayout gl_glycoPanel = new GroupLayout(glycoPanel);
		gl_glycoPanel.setHorizontalGroup(
			gl_glycoPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(glycoScrollPane, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
		);
		gl_glycoPanel.setVerticalGroup(
			gl_glycoPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_glycoPanel.createSequentialGroup()
					.addGap(5)
					.addComponent(glycoScrollPane, GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
					.addContainerGap())
		);
		glycoPanel.setLayout(gl_glycoPanel);
		
		
		gsmTable = new JTable();
		gsmScrollPane.setViewportView(gsmTable);
		viewPanel.setLayout(gl_viewPanel);

		ionTable = new JTable();
		ionScrollPane.setViewportView(ionTable);
		GroupLayout gl_ionPanel = new GroupLayout(ionPanel);
		gl_ionPanel.setHorizontalGroup(
			gl_ionPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ionPanel.createSequentialGroup()
					.addComponent(ionScrollPane, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
					.addGap(2))
		);
		gl_ionPanel.setVerticalGroup(
			gl_ionPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ionPanel.createSequentialGroup()
					.addGap(5)
					.addComponent(ionScrollPane, GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
					.addGap(6))
		);
		ionPanel.setLayout(gl_ionPanel);
		setLayout(groupLayout);
		setSize(1000, 950);
	}
	
	public void closeFile() {
		
		glycoTable = new JTable();
		glycoScrollPane.setViewportView(glycoTable);
		
		gsmModel = null;
		gsmTable = new JTable();
		gsmScrollPane.setViewportView(gsmTable);
		
		ionTable = new JTable();
		ionScrollPane.setViewportView(ionTable);
		
		statTable = new JTable();
		statScrollPane.setViewportView(statTable);
		
		spectrumPanel.remove(0);
		repaint();
	}
	
	public void export(String out) throws IOException {
		export(new File(out));
	}
	
	public void export(File out) throws IOException {
		GlycoMicroNovoXlsWriter writer = new GlycoMicroNovoXlsWriter(out, glycosyls);
		writer.write(determinedMatches, undeterminedMatches);
		writer.close();
	}
	
	public void load(File in) throws IOException {

		GlycoMicroNovoReader reader = new GlycoMicroNovoReader(in);
		reader.read();
		this.determinedMatches = reader.getDeterminedMatches();
		this.undeterminedMatches = reader.getUndeterminedMatches();

		GlycoMicroNovoStat stat = reader.getStat();
		
		HashMap<Integer, Integer> idmap = new HashMap<Integer, Integer>();

		this.glycosyls = reader.getGlycosyls();
		int[] glycoIds = new int[this.glycosyls.length];
		double[] glycoMasses = new double[this.glycosyls.length];
		for (int i = 0; i < glycoMasses.length; i++) {
			glycoMasses[i] = this.glycosyls[i].getMonoMass();
		}
		Arrays.sort(glycoMasses);
		for (int i = 0; i < glycoMasses.length; i++) {
			for (int j = 0; j < this.glycosyls.length; j++) {
				if (glycoMasses[i] == this.glycosyls[j].getMonoMass()) {
					glycoIds[i] = j;
				}
			}
		}

		Object[][] glycosylObjs = new Object[glycosyls.length][];
		for (int i = 0; i < glycosylObjs.length; i++) {
			glycosylObjs[i] = glycosyls[i].getRowObject().getObjs();
		}
		Object[] glycosylTitle = new Object[] { "Title", "Composition", "Mono mass", "Ave mass" };
		DefaultTableModel glycoModel = new DefaultTableModel(glycosylObjs, glycosylTitle) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -4980296784548330360L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		glycoTable.setModel(glycoModel);

		Object[][] statObjs = new Object[8][2];
		Object[] statTitle = new Object[] { "Item", "Value" };
		statObjs[0] = new Object[] { "Determined composition", determinedMatches.length };
		statObjs[1] = new Object[] { "Partially determined compositions", undeterminedMatches.length };
		statObjs[2] = new Object[] { "Q-value < 0.05", stat.getQValue5() };
		statObjs[3] = new Object[] { "Q-value < 0.01", stat.getQValue1() };
		statObjs[4] = new Object[] { "Classification true positive", stat.getClassTP() };
		statObjs[5] = new Object[] { "Classification FDR", FormatTool.dfP2.format(stat.getClassFDR()) };
		statObjs[6] = new Object[] { "Classification sensitivity", FormatTool.dfP2.format(stat.getClassSensitivity()) };
		statObjs[7] = new Object[] { "Classification specificity", FormatTool.dfP2.format(stat.getClassSpecificity()) };

		DefaultTableModel statModel = new DefaultTableModel(statObjs, statTitle) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5104883154806863006L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		statTable.setModel(statModel);

		ArrayList<RowObject> list = new ArrayList<RowObject>();
		for (int i = 0; i < determinedMatches.length; i++) {
			if (determinedMatches[i].getRank() > 1) {
				continue;
			}
			idmap.put(list.size(), i);
			list.add(determinedMatches[i].getRowObject());
		}

		RowObject[] objects = list.toArray(new RowObject[list.size()]);

		this.currentPage = 1;
		this.totalPageCount = objects.length / rowCount + 1;
		this.totalItemCount = objects.length;

		this.gsmModel = new PagedTableModel(reader.getTitle(), objects, currentPage, rowCount);

		Integer[] pages = new Integer[totalPageCount];
		for (int i = 0; i < pages.length; i++) {
			pages[i] = i + 1;
		}

		comboBoxPage.setModel(new DefaultComboBoxModel<Integer>(pages));
		comboBoxPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GlycoMicroNovoViewPanel.this.currentPage = GlycoMicroNovoViewPanel.this.comboBoxPage.getSelectedIndex()
						+ 1;
				GlycoMicroNovoViewPanel.this.selectPage(GlycoMicroNovoViewPanel.this.currentPage);
			}
		});

		gsmTable.setModel(gsmModel);
		gsmTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub
				spectrumPanel.remove(0);
				int selectedRow = gsmTable.getSelectedRow();
				int id = idmap.get((currentPage - 1) * rowCount + selectedRow);
				Peak[] peaks = determinedMatches[id].getPeaks();
				JFreeChart chart = GlycoMicroNovoDrawer.createXYBarChart(peaks, determinedMatches[id].getNodeMap());
				ChartPanel cp = new ChartPanel(chart);
				GroupLayout gl_spectraPanel = new GroupLayout(spectrumPanel);
				gl_spectraPanel.setHorizontalGroup(gl_spectraPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_spectraPanel.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE,
								780, Short.MAX_VALUE)));
				gl_spectraPanel.setVerticalGroup(gl_spectraPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_spectraPanel.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE,
								453, Short.MAX_VALUE)));
				spectrumPanel.setLayout(gl_spectraPanel);

				HashMap<Integer, GlycoPeakNode> nodemap = determinedMatches[id].getNodeMap();
				HashMap<Integer, GlycoPeakNode> oxoniumMap = determinedMatches[id].getOxoniumMap();
				Integer[] nodeIds = nodemap.keySet().toArray(new Integer[nodemap.size()]);
				Arrays.sort(nodeIds);
				Integer[] oxoIds = oxoniumMap.keySet().toArray(new Integer[oxoniumMap.size()]);
				Arrays.sort(oxoIds);

				Object[] title = new Object[5];
				Object[][] objs = new Object[oxoniumMap.size() + nodemap.size()][title.length];

				title[0] = "Type";
				title[1] = "Mz";
				title[2] = "Intensity";
				title[3] = "Charge";
				title[4] = "Detail";

				for (int i = 0; i < oxoIds.length; i++) {
					GlycoPeakNode node = oxoniumMap.get(oxoIds[i]);
					Peak peak = peaks[node.getPeakId()];
					objs[i][0] = "Oxonium ion";
					objs[i][1] = peak.getMz();
					objs[i][2] = peak.getIntensity();
					objs[i][3] = node.getCharge();
					int[] comp = node.getComposition();
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < comp.length; j++) {
						if (comp[j] > 0) {
							sb.append(glycosyls[glycoIds[j]].getTitle()).append("(").append(comp[j]).append(")");
						}
					}
					objs[i][4] = sb;
				}

				for (int i = 0; i < nodeIds.length; i++) {
					GlycoPeakNode node = nodemap.get(nodeIds[i]);
					String name = "Y" + node.getSid();
					Peak peak = peaks[node.getPeakId()];
					objs[oxoniumMap.size() + i][0] = name;
					objs[oxoniumMap.size() + i][1] = peak.getMz();
					objs[oxoniumMap.size() + i][2] = peak.getIntensity();
					objs[oxoniumMap.size() + i][3] = node.getCharge();

					int[] comp = node.getComposition();
					StringBuilder sb = new StringBuilder("pep+");
					for (int j = 0; j < comp.length; j++) {
						if (comp[j] > 0) {
							sb.append(glycosyls[glycoIds[j]].getTitle()).append("(").append(comp[j]).append(")");
						}
					}
					if (sb.charAt(sb.length() - 1) == '+') {
						sb.deleteCharAt(sb.length() - 1);
					}
					objs[oxoniumMap.size() + i][4] = sb;
				}

				DefaultTableModel model = new DefaultTableModel(objs, title) {

					/**
					 * 
					 */
					private static final long serialVersionUID = -8878952144333294000L;

					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};

				ionTable.setModel(model);
			}
		});

		this.selectPage(1);
		
		JFreeChart chart = GlycoMicroNovoDrawer.drawBlank();
		ChartPanel cp = new ChartPanel(chart);
		GroupLayout gl_spectraPanel = new GroupLayout(spectrumPanel);
		gl_spectraPanel
				.setHorizontalGroup(gl_spectraPanel.createParallelGroup(Alignment.TRAILING).addGroup(gl_spectraPanel
						.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)));
		gl_spectraPanel.setVerticalGroup(gl_spectraPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spectraPanel
				.createSequentialGroup().addComponent(cp, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)));
		spectrumPanel.setLayout(gl_spectraPanel);

		repaint();
	}
	
	private void selectPage(int page) {
		this.gsmModel.setCurrentPage(page);
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
