package bmi.med.uOttawa.metalab.task.dia.target.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.sun.management.OperatingSystemMXBean;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectParameter;
import bmi.med.uOttawa.metalab.dbSearch.deepDetect.DeepDetectTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiaNNTask;
import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.dia.gui.MetaLabMainPanelDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaParameterDia;
import bmi.med.uOttawa.metalab.task.dia.par.MetaSourcesDia;
import bmi.med.uOttawa.metalab.task.dia.target.MetaDiaTargetDBTask;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.gui.MetaLabMainPanelMag;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabMetaPanel;
import net.miginfocom.swing.MigLayout;

public class MetaLabMainPanelDiaTg extends MetaLabMainPanelDia {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2297795724561016397L;

	private MagDbItem magDbItem;
	private DBStatusPanel dbStatusPanel;
	protected SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	public MetaLabMainPanelDiaTg(MetaParameterDia metaPar, MetaSourcesDia msv) {
		super(metaPar, msv);
		// TODO Auto-generated constructor stub
	}

	protected void initial() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, "cell 0 0,grow");

		MetaParameterDia diaPar = (MetaParameterDia) par;
		this.workflowType = MetaLabWorkflowType.DiaNNMAGTG;
		this.magDbItem = diaPar.getUsedMagDbItem();
		if (magDbItem == null) {
			MagDbItem[] items = diaPar.getAvailableMagDbItem();
			if (items != null && items.length > 0) {
				magDbItem = items[0];
			} else {
				magDbItem = new MagDbItem("", "");
			}
		}

		this.initialMagDbPanel();
		this.initialIOPanel();
		this.initialParPanel();
		this.initialRunPanel();

		tabbedPane.setSelectedIndex(1);
		tabbedPane.setEnabledAt(3, false);
		previousTabIndex = tabbedPane.getSelectedIndex();

		tabbedPane.addChangeListener(l -> {

			int selectedIndex = tabbedPane.getSelectedIndex();
			switch (selectedIndex) {
			case 0:
				tabbedPane.setEnabledAt(2, false);
				tabbedPane.setEnabledAt(3, false);
				break;
			case 1:
				tabbedPane.setEnabledAt(2, true);
				tabbedPane.setEnabledAt(3, false);
				if (previousTabIndex == 0) {
					update0();
				}
				break;
			case 2:
				tabbedPane.setEnabledAt(3, true);
				if (previousTabIndex == 1) {
					update1();
				}
				break;
			case 3:
				update2();
				break;
			default:
				break;
			}

			previousTabIndex = selectedIndex;
		});
	}

	class DBStatusPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JLabel predictLabel;
		private JLabel sqlLabel;
		private JButton predictButton;
		private JButton sqlButton;

		private void update() {
			File currentFile = magDbItem == null ? new File("") : magDbItem.getCurrentFile();
			File predictDirFile = new File(currentFile, "predicted");

			if (predictDirFile.exists() && predictDirFile.listFiles().length > magDbItem.getSpeciesCount()) {
				predictLabel.setText("Peptide detectability files existed");
				predictButton.setEnabled(false);
			} else {
				predictLabel.setText("Peptide detectability files are missing");
				predictButton.setEnabled(true);
			}

			File sqlDbFile = new File(currentFile, "pro_func.db");
			if (sqlDbFile.exists()) {
				sqlLabel.setText("pro-func SQL database existed");
				sqlButton.setEnabled(false);
			} else {
				sqlLabel.setText("pro-func SQL database not found");
				sqlButton.setEnabled(true);
			}
		}

		DBStatusPanel(MetaSourcesDia msd, JProgressBar progressBar) {
			setBorder(new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
					"MAG library status", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			setLayout(new MigLayout("", "[100][50][100][120][10][80][50][100][80][10][80][50]", "[50]"));

			int totalCount = ProcessorCount();

			lblNumberOfThreads = new JLabel("Number of threads");
			add(lblNumberOfThreads, "cell 0 0");

			threadComboBox = new JComboBox<Integer>();
			add(threadComboBox, "cell 1 0,growx");
			for (int i = 1; i <= totalCount; i++) {
				threadComboBox.addItem(i);
			}

			int threadCount = par.getThreadCount();
			if (threadCount > 0 && threadCount <= totalCount) {
				threadComboBox.setSelectedIndex(threadCount - 1);
			}

			File currentFile = magDbItem == null ? new File("") : magDbItem.getCurrentFile();
			File predictDirFile = new File(currentFile, "predicted");

			if (predictDirFile.exists() && predictDirFile.listFiles().length > magDbItem.getSpeciesCount()) {
				predictLabel = new JLabel("Peptide detectability files existed");
				add(predictLabel, "cell 3 0");

				predictButton = new JButton("Predict");
				add(predictButton, "cell 5 0");
				predictButton.setEnabled(false);
			} else {
				predictLabel = new JLabel("Peptide detectability files are missing");
				add(predictLabel, "cell 3 0");

				JTextField predicTextField = new JTextField();
				predicTextField.setEditable(false);
				predicTextField.setColumns(40);
				add(predicTextField, "cell 6 0");

				predictButton = new JButton("Predict");
				add(predictButton, "cell 5 0");
				predictButton.setEnabled(true);
				predictButton.addActionListener(l -> {

					if (!msd.findDeepDetect()) {
						JOptionPane.showMessageDialog(this,
								"DeepDetect is not found, please check the Setting -> Resource.", "Warning",
								JOptionPane.WARNING_MESSAGE);
						setCursor(null);
						return;
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					int thread = threadComboBox.getItemAt(threadComboBox.getSelectedIndex());

					if (thread == 1) {

						Object[] options = { "Yes, continue with 1 thread.", "No, add more threads." };

						int choice = JOptionPane.showOptionDialog(this,
								"Only 1 thread will be used in this task, which will take a very long time. Do you want to continue?",
								"Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);

						if (choice == 1) {
							return;
						}
					}

					predictButton.setEnabled(false);
					progressBar.setValue(0);
					progressBar.setStringPainted(true);

					File deepDetectFile = new File(msd.getDeepDetect());

					DeepDetectTask deepDetectTask = new DeepDetectTask(deepDetectFile, new DeepDetectParameter(),
							magDbItem, thread, progressBar, predicTextField) {
						public void done() {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							setCursor(null);

							if (isCancelled()) {
								progressBar.setString("Task stopped");
								return;
							}

							boolean finish = false;

							try {

								finish = get();

								if (finish) {
									progressBar.setString("finished");
									predictLabel.setText("Peptide detectability files existed");
									JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Task finished", "Finish",
											JOptionPane.INFORMATION_MESSAGE);
								} else {
									JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Task failed", "Error",
											JOptionPane.ERROR_MESSAGE);
								}

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);

							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					};
					deepDetectTask.execute();
				});
			}

			File sqlDbFile = new File(currentFile, "pro_func.db");
			if (sqlDbFile.exists()) {
				sqlLabel = new JLabel("pro-func SQL database existed");
				add(sqlLabel, "cell 8 0");

				sqlButton = new JButton("Generate pro-func SQL database");
				add(sqlButton, "cell 10 0");
				sqlButton.setEnabled(false);
			} else {
				sqlLabel = new JLabel("pro-func SQL database not found");
				add(sqlLabel, "cell 8 0");

				sqlButton = new JButton("Generate pro-func SQL database");
				add(sqlButton, "cell 10 0");
				sqlButton.setEnabled(true);
				sqlButton.addActionListener(l -> {
					if (!msd.findPython()) {
						JOptionPane.showMessageDialog(this,
								"Python is not found, please check the Setting -> Resource.", "Warning",
								JOptionPane.WARNING_MESSAGE);
						setCursor(null);
						return;
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					progressBar.setIndeterminate(true);
					sqlButton.setEnabled(false);

					MetaDiaTargetDBTask task = new MetaDiaTargetDBTask((MetaParameterDia) par, (MetaSourcesDia) msv,
							progressBar, new JProgressBar(), null) {
						public void done() {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							setCursor(null);
							progressBar.setIndeterminate(false);

							try {
								if (!isCancelled()) {
									boolean finish = get();
									if (finish) {
										sqlLabel.setText("pro-func SQL database existed");
										JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Task finished",
												"Finish", JOptionPane.INFORMATION_MESSAGE);
									} else {
										JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Task failed",
												"Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							} catch (Exception e) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					};

					try {
						task.execute();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Task failed", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	protected void initialParPanel() {
		JPanel parameterPanel = new JPanel();
		tabbedPane.addTab("Parameters",
				new ImageIcon(MetaLabMainPanelMag.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")),
				parameterPanel, null);

		parameterPanel.setLayout(new MigLayout("", "[400:600:980,grow][400:600:980,grow]",
				"[50][300:350:400,grow][50][280:320:360,grow][20]"));

		MetaParameterDia diaPar = (MetaParameterDia) par;
		MetaSourcesDia msd = (MetaSourcesDia) msv;

		JProgressBar progressBar = new JProgressBar();
		parameterPanel.add(progressBar, "cell 0 4 2 1,growx");

		this.dbStatusPanel = new DBStatusPanel(msd, progressBar);
		parameterPanel.add(dbStatusPanel, "cell 0 0 2 0,grow");

		MetaTaxaSelectPanel taxaSelectPanel = new MetaTaxaSelectPanel(diaPar, (MetaSourcesDia) msv);
		parameterPanel.add(taxaSelectPanel, "cell 0 1,grow");
		taxaSelectPanel.getComboBox().addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				SwingWorker<?, ?> task = new SwingWorker<Object, Object>() {
					protected void done() {
						try {
							if (!isCancelled()) {
								get();
								progressBar.setIndeterminate(false);
								setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					protected Object doInBackground() throws Exception {
						// TODO Auto-generated method stub
						progressBar.setIndeterminate(true);
						int taxaId = taxaSelectPanel.getComboBox().getSelectedIndex();
						File currentFile = magDbItem.getCurrentFile();
						File sqlDbFile = new File(currentFile, "pro_func.db");
						if (!sqlDbFile.exists()) {

							JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
									"Please generate the pro-func SQL functional database first.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						String[] columns = new String[] { "genome_name", "superkingdom", "phylum", "class",
								"order_name", "family", "genus", "species" };
						HashSet<String> set = new HashSet<String>();
						String url = "jdbc:sqlite:" + sqlDbFile.getAbsolutePath().replaceAll("\\\\", "/");
						if (taxaId < columns.length) {
							try (Connection conn = DriverManager.getConnection(url);
									PreparedStatement pstmt = conn
											.prepareStatement("SELECT " + columns[taxaId] + " FROM taxonomy")) {
								try (ResultSet rs = pstmt.executeQuery()) {
									while (rs.next()) {
										set.add(rs.getString(columns[taxaId]));
									}
								}
							} catch (SQLException e) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
										"Error in connecting to the SQL functional database.", "Error",
										JOptionPane.ERROR_MESSAGE);
								return null;
							}
						} else {
							try (Connection conn = DriverManager.getConnection(url);
									PreparedStatement pstmt = conn
											.prepareStatement("SELECT genome_name FROM taxonomy")) {
								try (ResultSet rs = pstmt.executeQuery()) {
									while (rs.next()) {
										set.add(rs.getString("genome_name"));
									}
								}
							} catch (SQLException e) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
										"Error in connecting to the SQL functional database.", "Error",
										JOptionPane.ERROR_MESSAGE);
								return null;
							}
						}

						Object[] combineColumnName = new Object[] { "Deselect all", "Taxonomic name" };
						DefaultTableModel taxaTableModel = new DefaultTableModel(combineColumnName, 0);

						final JTable taxaTable = new JTable(taxaTableModel) {
							/**
							 * 
							 */
							private static final long serialVersionUID = 1191634849070116819L;

							@Override
							public Class<?> getColumnClass(int column) {
								if (column == 0) {
									return Boolean.class;
								}
								return String.class;
							}

							public boolean isCellEditable(int row, int column) {
								return column == 0;
							}
						};

						taxaTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxCellRenderer());
						taxaTable.getColumnModel().getColumn(0).setMaxWidth(90);
						taxaTable.getColumnModel().getColumn(1).setPreferredWidth(120);
						if (taxaId == 8) {
							taxaTable.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseClicked(MouseEvent e) {
									int row = taxaTable.rowAtPoint(e.getPoint());
									int col = taxaTable.columnAtPoint(e.getPoint());
									if (col == 1 && row >= 0) {
										String taxaName = taxaTable.getValueAt(row, col).toString();
										String url = "https://www.ebi.ac.uk/metagenomics/genomes/" + taxaName
												+ "#overview";
										if (url != null) {
											try {
												Desktop.getDesktop().browse(new URI(url));
											} catch (Exception ex) {
												ex.printStackTrace();
												JOptionPane.showMessageDialog(null, "Failed to open URL: " + url);
											}
										}
									}
								}
							});
						}

						taxaTable.getTableHeader().addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								int column = taxaTable.columnAtPoint(e.getPoint());
								if (column == 0) {
									TableColumn selectColumn = taxaTable.getColumnModel().getColumn(0);
									String headerValue = (String) selectColumn.getHeaderValue();
									if (headerValue.equals("Select all")) {
										selectColumn.setHeaderValue("Deselect all");
										for (int row = 0; row < taxaTableModel.getRowCount(); row++) {
											taxaTableModel.setValueAt(true, row, 0);
										}
									} else {
										selectColumn.setHeaderValue("Select all");
										for (int row = 0; row < taxaTableModel.getRowCount(); row++) {
											taxaTableModel.setValueAt(false, row, 0);
										}
									}
								}
							}
						});

						String[] taxa = set.toArray(String[]::new);
						Arrays.sort(taxa);
						for (String taxon : taxa) {
							if (taxon != null && taxon.length() > 0) {
								taxaTableModel.addRow(new Object[] { true, taxon });
							}
						}
						taxaSelectPanel.setTable(taxaTable);

						return null;
					}
				};

				try {
					task.execute();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
							"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		MetaFuncSelectPanel funcSelectPanel = new MetaFuncSelectPanel(diaPar, (MetaSourcesDia) msv);
		parameterPanel.add(funcSelectPanel, "cell 1 1,grow");
		funcSelectPanel.getComboBox().addItemListener(l -> {
			if (l.getStateChange() == ItemEvent.SELECTED) {
				SwingWorker<?, ?> task = new SwingWorker<Object, Object>() {
					protected void done() {
						try {
							if (!isCancelled()) {
								get();
								progressBar.setIndeterminate(false);
								setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					protected Object doInBackground() throws Exception {
						// TODO Auto-generated method stub
						progressBar.setIndeterminate(true);
						String funcName = funcSelectPanel.getComboBox()
								.getItemAt(funcSelectPanel.getComboBox().getSelectedIndex());
						File currentFile = magDbItem.getCurrentFile();
						File sqlDbFile = new File(currentFile, "pro_func.db");
						if (!sqlDbFile.exists()) {

							JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
									"Please generate the pro-func SQL functional database first.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						HashSet<String> set = new HashSet<String>();
						String url = "jdbc:sqlite:" + sqlDbFile.getAbsolutePath().replaceAll("\\\\", "/");
						try (Connection conn = DriverManager.getConnection(url);
								PreparedStatement pstmt = conn.prepareStatement("SELECT func_name FROM " + funcName)) {
							try (ResultSet rs = pstmt.executeQuery()) {
								while (rs.next()) {
									set.add(rs.getString("func_name"));
								}
							}
						} catch (SQLException e) {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
									"Error in connecting to the SQL functional database.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						Object[] combineColumnName = new Object[] { "Select all", "Function name" };
						DefaultTableModel funcTableModel = new DefaultTableModel(combineColumnName, 0);

						final JTable funcTable = new JTable(funcTableModel) {
							/**
							 * 
							 */
							private static final long serialVersionUID = 1L;

							@Override
							public Class<?> getColumnClass(int column) {
								if (column == 0) {
									return Boolean.class;
								}
								return String.class;
							}

							public boolean isCellEditable(int row, int column) {
								return column == 0;
							}
						};

						funcTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxCellRenderer());
						funcTable.getColumnModel().getColumn(0).setMaxWidth(90);
						funcTable.getColumnModel().getColumn(1).setPreferredWidth(120);
						funcTable.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								int row = funcTable.rowAtPoint(e.getPoint());
								int col = funcTable.columnAtPoint(e.getPoint());
								if (col == 1 && row >= 0) {
									String funcName = funcTable.getValueAt(row, col).toString();
									String url = getFunURL(funcSelectPanel.getComboBox().getSelectedIndex(), funcName);
									if (url != null) {
										try {
											Desktop.getDesktop().browse(new URI(url));
										} catch (Exception ex) {
											ex.printStackTrace();
											JOptionPane.showMessageDialog(null, "Failed to open URL: " + url);
										}
									}
								}
							}
						});

						funcTable.getTableHeader().addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								int column = funcTable.columnAtPoint(e.getPoint());
								if (column == 0) {
									TableColumn selectColumn = funcTable.getColumnModel().getColumn(0);
									String headerValue = (String) selectColumn.getHeaderValue();
									if (headerValue.equals("Select all")) {
										selectColumn.setHeaderValue("Deselect all");
										for (int row = 0; row < funcTableModel.getRowCount(); row++) {
											funcTableModel.setValueAt(true, row, 0);
										}
									} else {
										selectColumn.setHeaderValue("Select all");
										for (int row = 0; row < funcTableModel.getRowCount(); row++) {
											funcTableModel.setValueAt(false, row, 0);
										}
									}
								}
							}
						});

						String[] funcs = set.toArray(String[]::new);
						Arrays.sort(funcs);
						for (String func : funcs) {
							funcTableModel.addRow(new Object[] { false, func });
						}
						funcSelectPanel.setTable(funcTable);

						return null;
					}
				};

				try {
					task.execute();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
							"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		metaLabMetaPanel = new MetaLabMetaPanel(diaPar.getMetadata());

		metaLabMetaPanel.setBorder(
				new TitledBorder(null, "Metadata settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(metaLabMetaPanel, "cell 0 2 1 2,grow");

		JPanel createLibPanel = new JPanel();
		parameterPanel.add(createLibPanel, "cell 1 2,grow");
		createLibPanel.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Create library", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		createLibPanel.setLayout(new MigLayout("", "[120][20][80][20][80][20][80]", "[50][50]"));
		JCheckBox pepLibCheckBox = new JCheckBox("Library path");
		createLibPanel.add(pepLibCheckBox, "cell 0 1,grow");
		JTextField pepLibTextField = new JTextField();
		createLibPanel.add(pepLibTextField, "cell 2 1 5 1,grow");
		File finalLibFile = new File(new File(par.getResult()), "selectedPep.predicted.speclib");
		pepLibTextField.setText(finalLibFile.getAbsolutePath());
		pepLibCheckBox.setSelected(finalLibFile.exists());
		pepLibCheckBox.setEnabled(false);
		pepLibTextField.setEnabled(false);

		JLabel pepProbLabel = new JLabel("Peptide detectability threshold");
		createLibPanel.add(pepProbLabel, "cell 0 0,grow");
		JComboBox<Float> pepProbComboBox = new JComboBox<Float>();
		for (int i = 0; i <= 100; i++) {
			float threshold = (float) (i / 100.0);
			pepProbComboBox.addItem(threshold);
		}
		createLibPanel.add(pepProbComboBox, "cell 2 0,grow");

		JButton testButton = new JButton("Get a proper threshold");
		createLibPanel.add(testButton, "cell 4 0,grow");

		JButton createButton = new JButton("Create library");
		createLibPanel.add(createButton, "cell 6 0,grow");

		testButton.addActionListener(l -> {

			SwingWorker<?, ?> task = new SwingWorker<Object, Object>() {
				protected void done() {
					try {
						if (!isCancelled()) {
							get();
							progressBar.setIndeterminate(false);
							testButton.setEnabled(true);
							createButton.setEnabled(true);
							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub
					progressBar.setIndeterminate(true);
					testButton.setEnabled(false);
					createButton.setEnabled(false);

					File currentFile = magDbItem.getCurrentFile();
					File sqlDbFile = new File(currentFile, "pro_func.db");
					if (!sqlDbFile.exists()) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please generate the SQL functional database first.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					int taxaId = taxaSelectPanel.getComboBox().getSelectedIndex();
					if (taxaId == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please specify a taxonomic level and select the taxa.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					int funcId = funcSelectPanel.getComboBox().getSelectedIndex();
					if (funcId == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please specify a functional type and select the functions.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					HashSet<String> taxaSet = taxaSelectPanel.getSelectedTaxa();
					HashSet<String> funcSet = funcSelectPanel.getSelectedFunctions();
					if (taxaSet == null || taxaSet.size() == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Please select the taxa.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					if (funcSet == null || funcSet.size() == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Please select the functions.",
								"Error", JOptionPane.ERROR_MESSAGE);
						return null;
					}

					HashSet<String> taxaGenomeSet = new HashSet<String>();
					String[] columns = new String[] { "genome_name", "superkingdom", "phylum", "class", "order_name",
							"family", "genus", "species" };
					String url = "jdbc:sqlite:" + sqlDbFile.getAbsolutePath().replaceAll("\\\\", "/");
					if (taxaId < columns.length) {
						StringBuilder sql = new StringBuilder(
								"SELECT genome_name FROM taxonomy WHERE " + columns[taxaId] + " IN (");
						boolean first = true;
						for (String taxaName : taxaSet) {
							if (!first) {
								sql.append(", ");
							} else {
								first = false;
							}
							sql.append("'").append(taxaName).append("'");
						}
						sql.append(")");

						try (Connection conn = DriverManager.getConnection(url);
								PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
							try (ResultSet rs = pstmt.executeQuery()) {
								while (rs.next()) {
									taxaGenomeSet.add(rs.getString("genome_name"));
								}
							}
						} catch (SQLException e) {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
									"Error in connecting to the SQL functional database.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						System.out.println(format.format(new Date()) + ":\t" + taxaSet.size() + " taxa are selected in "
								+ columns[taxaId] + " level, " + taxaGenomeSet.size()
								+ " genomes will be selected for the following analysis");
					} else {
						taxaGenomeSet = taxaSet;
						System.out.println(format.format(new Date()) + ":\t" + taxaSet.size()
								+ " genomes will be selected for the following analysis");
					}

					HashSet<String> funcProSet = new HashSet<String>();
					String funcName = funcSelectPanel.getComboBox().getItemAt(funcId);
					StringBuilder sql = new StringBuilder(
							"SELECT protein_name FROM " + funcName + " WHERE func_name IN (");
					boolean first = true;
					for (String fName : funcSet) {
						if (!first) {
							sql.append(", ");
						} else {
							first = false;
						}
						sql.append("'").append(fName).append("'");
					}
					sql.append(")");

					try (Connection conn = DriverManager.getConnection(url);
							PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								String proName = rs.getString("protein_name");
								String genomeName = proName.substring(0, proName.indexOf("_"));
								if (taxaGenomeSet.contains(genomeName)) {
									funcProSet.add(proName);
								}
							}
						}

						System.out.println(format.format(new Date()) + ":\t" + funcSet.size()
								+ " functions are selected from the " + funcName + " database, " + funcProSet.size()
								+ " proteins will be selected for the following analysis");
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error in connecting to the SQL functional database.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					File predictedFile = new File(currentFile, "predicted");
					if (!predictedFile.exists() || predictedFile.listFiles().length == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please predict the peptide detectability first.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
							.getTotalMemorySize() / 1024 / 1024 / 1024;
					ExecutorService es = Executors.newFixedThreadPool((int) totalMemorySize / 2);

					ConcurrentHashMap<String, Float> pepMap = new ConcurrentHashMap<String, Float>();
					for (String genome : taxaGenomeSet) {
						es.submit(() -> {
							try (BufferedReader reader = new BufferedReader(
									new FileReader(new File(predictedFile, genome + ".predicted.tsv")))) {
								String line = reader.readLine();
								while ((line = reader.readLine()) != null) {
									String[] cs = line.split("\t");
									if (funcProSet.contains(cs[0])) {
										if (pepMap.containsKey(cs[1])) {
											float score = Float.parseFloat(cs[2]);
											if (score > pepMap.get(cs[1])) {
												pepMap.put(cs[1], score);
											}
										} else {
											pepMap.put(cs[1], Float.parseFloat(cs[2]));
										}
									}
								}
							} catch (Exception e) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
										"Error processing file for genome " + genome + " - " + e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								return null;
							}
							return null;
						});
					}
					es.shutdown();
					try {
						es.awaitTermination(2, TimeUnit.HOURS);
					} catch (InterruptedException e) {

						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error waiting for file processing threads: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					int[] detectabilities = new int[101];
					for (String pep : pepMap.keySet()) {
						float score = pepMap.get(pep);
						int id = (int) (score * 100);
						for (int i = 0; i <= id; i++) {
							detectabilities[i]++;
						}
					}

					int lowPepThresCount = -1;
					int highPepThresCount = -1;
					for (int i = 0; i < detectabilities.length; i++) {
						System.out.println(format.format(new Date()) + ":\t" + "Peptide detectability: "
								+ (double) i / 100 + "; peptide count: " + detectabilities[i]);

						if (detectabilities[i] < 3000000 && lowPepThresCount == -1) {
							lowPepThresCount = i;
						}

						if (detectabilities[i] < 1000000 && highPepThresCount == -1) {
							highPepThresCount = i;
						}
					}

					System.out.println(format.format(new Date()) + ":\t"
							+ "The proper size of a peptide library is from 1,000,000 to 3,000,000 peptides");
					System.out.println(
							format.format(new Date()) + ":\t" + "The recommended peptide detectability threshold is:");

					if (lowPepThresCount == -1) {
						if (highPepThresCount == -1) {
							System.out
									.println(format.format(new Date()) + ":\t" + "Peptide detectability threshold=1.0");
							pepProbComboBox.setSelectedIndex(100);
						}
					} else {
						if (highPepThresCount == -1) {
							System.out.println(format.format(new Date()) + ":\t" + "Peptide detectability threshold>"
									+ (double) lowPepThresCount / 100 + " and <=1.0");
							pepProbComboBox.setSelectedIndex(lowPepThresCount);
						} else {
							if (highPepThresCount > lowPepThresCount) {
								System.out.println(format.format(new Date()) + ":\t"
										+ "Peptide detectability threshold>=" + (double) lowPepThresCount / 100
										+ " and <=" + (double) highPepThresCount / 100);
							} else {
								System.out.println(format.format(new Date()) + ":\t"
										+ "Peptide detectability threshold>=" + (double) lowPepThresCount / 100);
							}
							pepProbComboBox.setSelectedIndex(lowPepThresCount);
						}
					}

					return null;
				}
			};

			try {
				task.execute();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
						"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		createButton.addActionListener(l -> {

			if (pepLibCheckBox.isSelected()) {
				Object[] options = { "Yes, overwrite the original file.", "No." };

				int choice = JOptionPane.showOptionDialog(this,
						"A selected peptide library already exists in " + pepLibTextField.getText()
								+ ", do you want to overwrite it?",
						"Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
						options[0]);

				if (choice == 1) {
					return;
				}
			}

			SwingWorker<?, ?> task = new SwingWorker<Object, Object>() {
				protected void done() {
					try {
						if (!isCancelled()) {
							get();
							progressBar.setIndeterminate(false);
							testButton.setEnabled(true);
							createButton.setEnabled(true);
							setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				protected Object doInBackground() throws Exception {
					// TODO Auto-generated method stub
					progressBar.setIndeterminate(true);
					testButton.setEnabled(false);
					createButton.setEnabled(false);

					File currentFile = magDbItem.getCurrentFile();
					File sqlDbFile = new File(currentFile, "pro_func.db");
					if (!sqlDbFile.exists()) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please generate the SQL functional database first.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					int taxaId = taxaSelectPanel.getComboBox().getSelectedIndex();
					if (taxaId == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please specify a taxonomic level and select the taxa.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					int funcId = funcSelectPanel.getComboBox().getSelectedIndex();
					if (funcId == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please specify a functional type and select the functions.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					HashSet<String> taxaSet = taxaSelectPanel.getSelectedTaxa();
					HashSet<String> funcSet = funcSelectPanel.getSelectedFunctions();
					if (taxaSet == null || taxaSet.size() == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Please select the taxa.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					if (funcSet == null || funcSet.size() == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this, "Please select the functions.",
								"Error", JOptionPane.ERROR_MESSAGE);
						return null;
					}

					HashSet<String> taxaGenomeSet = new HashSet<String>();
					String[] columns = new String[] { "genome_name", "superkingdom", "phylum", "class", "order_name",
							"family", "genus", "species" };
					String url = "jdbc:sqlite:" + sqlDbFile.getAbsolutePath().replaceAll("\\\\", "/");
					if (taxaId < columns.length) {
						StringBuilder sql = new StringBuilder(
								"SELECT genome_name FROM taxonomy WHERE " + columns[taxaId] + " IN (");
						boolean first = true;
						for (String taxaName : taxaSet) {
							if (!first) {
								sql.append(", ");
							} else {
								first = false;
							}
							sql.append("'").append(taxaName).append("'");
						}
						sql.append(")");

						try (Connection conn = DriverManager.getConnection(url);
								PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
							try (ResultSet rs = pstmt.executeQuery()) {
								while (rs.next()) {
									taxaGenomeSet.add(rs.getString("genome_name"));
								}
							}
						} catch (SQLException e) {
							JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
									"Error in connecting to the SQL functional database.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						System.out.println(format.format(new Date()) + ":\t" + taxaSet.size() + " taxa are selected in "
								+ columns[taxaId] + " level, " + taxaGenomeSet.size()
								+ " genomes will be selected for the following analysis");
					} else {
						taxaGenomeSet = taxaSet;
						System.out.println(format.format(new Date()) + ":\t" + taxaSet.size()
								+ " genomes will be selected for the following analysis");
					}

					HashSet<String> funcProSet = new HashSet<String>();
					String funcName = funcSelectPanel.getComboBox().getItemAt(funcId);
					StringBuilder sql = new StringBuilder(
							"SELECT protein_name FROM " + funcName + " WHERE func_name IN (");
					boolean first = true;
					for (String fName : funcSet) {
						if (!first) {
							sql.append(", ");
						} else {
							first = false;
						}
						sql.append("'").append(fName).append("'");
					}
					sql.append(")");

					try (Connection conn = DriverManager.getConnection(url);
							PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								String proName = rs.getString("protein_name");
								String genomeName = proName.substring(0, proName.indexOf("_"));
								if (taxaGenomeSet.contains(genomeName)) {
									funcProSet.add(proName);
								}
							}
						}

						System.out.println(format.format(new Date()) + ":\t" + funcSet.size()
								+ " functions are selected from the " + funcName + " database, " + funcProSet.size()
								+ " proteins will be selected for the following analysis");
					} catch (SQLException e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error in connecting to the SQL functional database.", "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					File predictedFile = new File(currentFile, "predicted");
					if (!predictedFile.exists() || predictedFile.listFiles().length == 0) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Please predict the peptide detectability first.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					long totalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
							.getTotalMemorySize() / 1024 / 1024 / 1024;
					ExecutorService es = Executors.newFixedThreadPool((int) totalMemorySize / 2);

					float threshold = pepProbComboBox.getItemAt(pepProbComboBox.getSelectedIndex());
					HashSet<String> pepSet = new HashSet<String>();
					for (String genome : taxaGenomeSet) {
						es.submit(() -> {
							try (BufferedReader reader = new BufferedReader(
									new FileReader(new File(predictedFile, genome + ".predicted.tsv")))) {
								String line = reader.readLine();
								while ((line = reader.readLine()) != null) {
									String[] cs = line.split("\t");
									if (funcProSet.contains(cs[0])) {
										float score = Float.parseFloat(cs[2]);
										if (score > threshold) {
											pepSet.add(cs[1]);
										}
									}
								}
							} catch (Exception e) {
								JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
										"Error processing file for genome " + genome + " - " + e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
								return null;
							}
							return null;
						});
					}
					es.shutdown();
					try {
						es.awaitTermination(2, TimeUnit.HOURS);
					} catch (InterruptedException e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error waiting for file processing threads: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					File resultFile = new File(par.getResult());
					if (!resultFile.exists()) {
						resultFile.mkdirs();
					}
					File fastaFile = new File(resultFile, "selectedPep.fasta");
					File infoFile = new File(resultFile, "selectedPep.info");
					File libFile = new File(resultFile, "selectedPep.lib");
					try (PrintWriter writer = new PrintWriter(fastaFile)) {
						int count = 0;
						for (String pep : pepSet) {
							writer.println(">pro" + count + " pro" + count);
							writer.println(pep);
							count++;
						}
						writer.close();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error writing the peptide fasta file: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}
					try (PrintWriter writer = new PrintWriter(infoFile)) {
						writer.println("Genomes:");
						for (String genome : taxaGenomeSet) {
							writer.println(genome);
						}
						writer.println("Functions:");
						for (String func : funcSet) {
							writer.println(func);
						}
						writer.println("Proteins:");
						for (String pro : funcProSet) {
							writer.println(pro);
						}
						writer.println("Peptide detectability threshold:");
						writer.print(threshold);
						writer.close();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
								"Error writing the peptide information file: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
						return null;
					}

					DiaNNTask diaNNTask = new DiaNNTask(msd.getDiann());
					DiannParameter diannPar = new DiannParameter();
					diannPar.setThreads(diaPar.getThreadCount());
					diaNNTask.addTask(diannPar, fastaFile.getAbsolutePath(), libFile.getAbsolutePath(), false);
					diaNNTask.run(2);

					File finalLibFile = new File(resultFile, "selectedPep.predicted.speclib");
					diaPar.setLibrary(new String[] { finalLibFile.getAbsolutePath() });

					pepLibCheckBox.setSelected(true);
					pepLibTextField.setText(finalLibFile.getAbsolutePath());

					return null;
				}
			};

			try {
				task.execute();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MetaLabMainPanelDiaTg.this,
						"Task failed :(\nplease contact us to get a solution", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		JScrollPane scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBorder(BorderFactory.createEmptyBorder());
		scrollPaneConsole.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parameterPanel.add(scrollPaneConsole, "cell 1 3,grow");

		ConsoleTextArea consoleTextArea = null;
		try {
			consoleTextArea = new ConsoleTextArea();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scrollPaneConsole.setViewportView(consoleTextArea);
	}

	public void updateParameter() {
		int selectedIndex = tabbedPane.getSelectedIndex();
		switch (selectedIndex) {
		case 1:
			update1();
			break;
		case 2:
			update2();
			break;
		default:
			break;
		}
	}

	protected void update1() {
		MetaData metadata = inputOutputPanel.getMetaData();
		String result = inputOutputPanel.getResultFile();
		par.setResult(result);

		String microdb = dbPanel.getMicroDb();

		par.setMicroDb(microdb);
		par.setMetadata(metadata);

		metaLabMetaPanel.update(metadata);
		metaLabMetaPanel.update();

		MetaParameterDia diaPar = (MetaParameterDia) par;
		this.magDbItem = diaPar.getUsedMagDbItem();

		this.dbStatusPanel.update();
	}

	protected void update2() {

		MetaParameterDia diaPar = (MetaParameterDia) par;

		String result = inputOutputPanel.getResultFile();
		diaPar.setResult(result);

		MetaData metadata = inputOutputPanel.getMetaData();
		diaPar.setMetadata(metadata);

		int threadCount = (int) this.threadComboBox.getSelectedItem();
		par.setThreadCount(threadCount);

		metaLabMetaPanel.update(metadata);
		metaLabMetaPanel.update();

		diaPar.setLibrarySearch(true);

		metaLabParViewPanel.update();

		this.warnings = this.checkParameter();

		if (warnings.length > 0) {
			StringBuilder sb = new StringBuilder("<b>Warnings</b><p>");
			for (int i = 0; i < warnings.length; i++) {
				sb.append(warnings[i]).append("<p>");
			}
			sb.append("<b>").append(warnings.length).append("</b> ")
					.append("warnings are found, please check the parameter settings before start the task.");
			this.textAreaParCheck.setText(sb.toString());
		} else {
			this.textAreaParCheck.setText("Perfect! Ready to start.");
		}
	}

	private String getFunURL(int funcType, String funcName) {
		switch (funcType) {
		case 1:
			return "https://www.ebi.ac.uk/QuickGO/term/" + funcName;
		case 2:
			return "https://www.genome.jp/dbget-bin/www_bget?ec:" + funcName;
		case 3:
			return "https://www.genome.jp/dbget-bin/www_bget?" + funcName;
		case 4:
			return "https://www.genome.jp/dbget-bin/www_bget?path:" + funcName;
		case 5:
			return "https://www.genome.jp/module/ath_" + funcName;
		case 6:
			return "https://www.genome.jp/dbget-bin/www_bget?rn:" + funcName;
		case 7:
			return "https://www.genome.jp/entry/" + funcName;
		default:
			return null;
		}
	}

	protected String[] checkParameter() {
		ArrayList<String> list = new ArrayList<String>();

		MetaParameterDia diaPar = (MetaParameterDia) par;

		if (!((MetaSourcesDia) msv).findDiaNN()) {
			list.add("DIA-NN is not found, please check the Setting -> Resource.");
		}

		if (diaPar.getMetadata() == null || diaPar.getMetadata().getRawFiles() == null
				|| diaPar.getMetadata().getRawFiles().length == 0) {
			list.add("Raw files are not found.");
		} else {
			String[] rawfiles = diaPar.getMetadata().getRawFiles();
			for (int i = 0; i < rawfiles.length; i++) {
				File filei = new File(rawfiles[i]);
				if (!filei.exists()) {
					list.add("Raw file " + rawfiles[i] + " is not found.");
				}
			}
		}

		File microDbFile = new File(diaPar.getMicroDb());
		if (!microDbFile.exists()) {
			list.add("Microbiome database " + microDbFile + " is not found.");
		} else {

		}

		if (diaPar.getResult() == null || diaPar.getResult().length() == 0) {
			list.add("Result file is not found.");
		}

		String[] usedLibs = diaPar.getLibrary();
		if (usedLibs.length == 0) {
			list.add("Peptide libraries are not set.");
		}

		String[] warnings = list.toArray(new String[list.size()]);

		return warnings;
	}

	private class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4465353594278093657L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setSelected((Boolean) value);
			return this;
		}
	}
}
