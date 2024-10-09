/**
 * 
 */
package bmi.med.uOttawa.metalab.task.v1.gui;

import java.awt.Cursor;
import java.io.File;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.model.ConsoleTextArea;
import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantEnzyme;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;
import bmi.med.uOttawa.metalab.task.v1.MetaMainTask;
import bmi.med.uOttawa.metalab.task.v1.par.JsonParaHandler;
import bmi.med.uOttawa.metalab.task.v1.par.MetaParameterV1;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

/**
 * @author Kai Cheng
 *
 */
public class MainFrame extends JFrame implements ActionListener {

//	private static final String tempParameter = "src\\bmi\\med\\uOttawa\\metaLabPro\\Task\\temp.json";
	private static final String tempParameter = "conf\\temp.json";
	private static final String icon = "conf\\icon.png";

	/**
	 * 
	 */
	private static final long serialVersionUID = 882496033652981328L;

	private JPanel contentPane;
	private JProgressBar progressBar1;
	private JProgressBar progressBar2;

	private JButton btnRun;
	private ParameterPanel parameterPanel;
	private StatusPanel statusPanel;
	private InputOutputPanel rawFilesPanel;
	private AdvancedSettingPanel advancedSettingPanel;

	private MetaParameterV1 parameter;

	private static Logger LOGGER = LogManager.getLogger();

	/**
	 * Launch the application.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					LOGGER.error("Error in creating the GUI", e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws Exception
	 */
	public MainFrame() throws Exception {

		setTitle("MetaLab 1.2.1");

		setIconImage((new ImageIcon(icon)).getImage());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(MainFrame.this, "Exiting MetaLab?", "Really Closing?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

					JsonParaHandler.export(getParameter(), tempParameter);
					System.exit(0);
				}
			}
		});

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 1600, 900);

		if ((new File(tempParameter)).exists()) {
			parameter = JsonParaHandler.parse(tempParameter);
			if (parameter == null) {
				parameter = new MetaParameterV1();
			}
		} else {
			parameter = new MetaParameterV1();
		}

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmLoadParameter = new JMenuItem("Load parameter");
		mntmLoadParameter.addActionListener(ActionListener -> {
			JFileChooser fileChooser = new JFileChooser(parameter.getMetalabOutput());
			fileChooser.setFileFilter(new FileNameExtensionFilter("Parameter file (*.json)", "json"));
			int returnValue = fileChooser.showOpenDialog(MainFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				MetaParameterV1 loadPar = JsonParaHandler.parse(selectedFile.getAbsolutePath());

				if (loadPar != null) {
					parameter = loadPar;
					JsonParaHandler.export(loadPar, tempParameter);

					MainFrame.this.parameterPanel = new ParameterPanel(loadPar);
					MainFrame.this.rawFilesPanel = new InputOutputPanel(loadPar.getRawFiles(),
							loadPar.getMetalabOutput());
					MainFrame.this.advancedSettingPanel = new AdvancedSettingPanel(loadPar);
				}
			}

		});
		mnFile.add(mntmLoadParameter);

		JMenuItem mntmSaveParameter = new JMenuItem("Save parameter");
		mntmSaveParameter.addActionListener(l -> {

			MetaParameterV1 savePar = getParameter();

			if (savePar != null) {
				JFileChooser fileChooser = new JFileChooser(savePar.getMetalabOutput());
				int returnValue = fileChooser.showSaveDialog(MainFrame.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					File parent = file.getParentFile();
					String name = file.getName();
					if (!name.endsWith("json")) {
						name = name += ".json";
						file = new File(parent, name);
					}

					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(MainFrame.this,
								file.getAbsolutePath() + " already exists. Replace it?", "Confirm Save",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

							JsonParaHandler.export(savePar, file.getAbsolutePath());
						}
					} else {
						JsonParaHandler.export(savePar, file.getAbsolutePath());
					}
				}
			}
		});
		mnFile.add(mntmSaveParameter);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(l -> {
			if (JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure to close this window?", "Really Closing?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

				JsonParaHandler.export(getParameter(), tempParameter);
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);

		/*
		 * JMenu mnTools = new JMenu("Tools"); menuBar.add(mnTools);
		 * 
		 * JMenuItem mntmSetting = new JMenuItem("Settings");
		 * mntmSetting.addActionListener(ActionListener -> { AdvancedParameterDialog
		 * psDialog = new AdvancedParameterDialog(parameter);
		 * psDialog.setAlwaysOnTop(true); psDialog.setVisible(true); });
		 * 
		 * mnTools.add(mntmSetting);
		 */

		/*
		 * JMenuItem mntmConstructPeptideDatabase = new
		 * JMenuItem("Construct peptide database");
		 * mntmConstructPeptideDatabase.addActionListener(ActionListener -> {
		 * DBConstructorDialog dbDialog = new DBConstructorDialog();
		 * dbDialog.setAlwaysOnTop(true); dbDialog.setVisible(true); });
		 * mnTools.add(mntmConstructPeptideDatabase);
		 */

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new MigLayout("", "[950px,grow 1920]", "[600][90][150,grow 300][20]"));
		setContentPane(contentPane);

		JScrollPane consoleScrollPane = new JScrollPane();
		consoleScrollPane.setViewportBorder(
				new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		ConsoleTextArea consoleTextArea = new ConsoleTextArea();
		consoleTextArea.setEditable(false);
		consoleScrollPane.setViewportView(consoleTextArea);

		System.out.println("Welcome to MetaLab " + MetaLabVersions.getCurrentVersion().getVersion());

		statusPanel = new StatusPanel("1.0.0");
		statusPanel.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		rawFilesPanel = new InputOutputPanel(parameter.getRawFiles(), parameter.getMetalabOutput());
		tabbedPane.addTab("Input & output files", null, rawFilesPanel, null);

		parameterPanel = new ParameterPanel(parameter);
		tabbedPane.addTab("Parameters", null, parameterPanel, null);

		advancedSettingPanel = new AdvancedSettingPanel(parameter);
		tabbedPane.addTab("Advanced settings", null, advancedSettingPanel, null);
		advancedSettingPanel.getResourceBtnBrowse().addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser(advancedSettingPanel.getResourceTextField().getText());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fileChooser.showOpenDialog(MainFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				advancedSettingPanel.getResourceTextField().setText(file.getAbsolutePath());
				if (advancedSettingPanel.checkResource()) {
					parameterPanel.setResource(file.getAbsolutePath());
				}
			}
		});
		advancedSettingPanel.getResourceCheckBxDefault().addActionListener(l -> {
			if (advancedSettingPanel.getResourceCheckBxDefault().isSelected()) {
				advancedSettingPanel.getResourceTextField().setText("Resources\\");
				File file = new File("Resources\\");
				if (advancedSettingPanel.checkResource()) {
					parameterPanel.setResource(file.getAbsolutePath());
				}
			} else {
				advancedSettingPanel.getResourceTextField().setText("");
			}
		});

		JPanel taskPanel = new JPanel();
		taskPanel.setBorder(new TitledBorder(null, "Task", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		this.progressBar1 = new JProgressBar();
		this.progressBar2 = new JProgressBar();
		taskPanel.setLayout(new MigLayout("", "[750, grow 1800][20][100][10]", "[15px][15px][20px]"));
		taskPanel.add(progressBar1, "cell 0 0,growx");
		taskPanel.add(progressBar2, "cell 0 1,growx");

		btnRun = new JButton("Run");
		btnRun.setActionCommand("start");
		btnRun.addActionListener(this);
		taskPanel.add(btnRun, "cell 2 0 1 2,grow");

		contentPane.add(statusPanel, "cell 0 3,growx,aligny bottom");
		contentPane.add(consoleScrollPane, "cell 0 2,grow");
		contentPane.add(taskPanel, "cell 0 1,grow");
		contentPane.add(tabbedPane, "cell 0 0,grow 1920");
	}

	private MetaParameterV1 getParameter() {

		String[] vms = this.parameterPanel.getVariableMods();
		parameter.setVariMods(vms);

		String[] fms = this.parameterPanel.getFixedMods();
		parameter.setFixMods(fms);

		String[] ens = this.parameterPanel.getEnzymes();
		parameter.setEnzymes(ens);

		int digestMode = this.parameterPanel.getDigestMode();
		parameter.setDigestMode(digestMode);

		String quanMode = this.parameterPanel.getQuanMode();
		parameter.setQuanMode(quanMode);

		String[][] labels = this.parameterPanel.getLabels();
		parameter.setLabels(labels);

		String[] isobaric = this.parameterPanel.getIsobaricTags();
		parameter.setIsobaric(isobaric);

		double[][] isoCorFactor = this.parameterPanel.getIsoCorFactor();
		parameter.setIsoCorFactor(isoCorFactor);

		String database = this.parameterPanel.getDatabase();
		parameter.setDatabase(database);

		String[] hostDBs = this.parameterPanel.getHostDBs();
		parameter.setHostDBs(hostDBs);

		boolean isAppendHostDb = this.parameterPanel.isAppendHostDb();
		parameter.setAppendHostDb(isAppendHostDb);

		String[][] raws = this.rawFilesPanel.getRawFiles();
		parameter.setRawFiles(raws);

		String result = this.rawFilesPanel.getResultFile();
		parameter.setResult(result);

		int threadCount = this.advancedSettingPanel.getThreadCount();
		parameter.setThreadCount(threadCount);

		int instruType = this.advancedSettingPanel.getInstruType();
		parameter.setInstruType(instruType);

		String resource = this.advancedSettingPanel.getResource();
		parameter.setResource(resource);

		boolean useBuildin = this.advancedSettingPanel.useBuiltin();
		parameter.setBuildIn(useBuildin);

		boolean useUnipept = this.advancedSettingPanel.useUnipept();
		parameter.setUnipept(useUnipept);

		boolean clusterMS2 = this.advancedSettingPanel.clusterMS2();
		parameter.setCluster(clusterMS2);

		boolean[] workflow = this.advancedSettingPanel.getWrokflow();
		parameter.setWorkflow(workflow);

		TaxonomyRanks ingoreRank = this.advancedSettingPanel.getIgnoreRank();
		parameter.setIgnoreBlankRank(ingoreRank);

		HashSet<RootType> rootSet = this.advancedSettingPanel.getRootSet();
		parameter.setUsedRootTypes(rootSet);

		HashSet<String> ignoreTaxaSet = this.advancedSettingPanel.getIgnoreTaxaSet();
		parameter.setExcludeTaxa(ignoreTaxaSet);

		int functionId = this.advancedSettingPanel.getFunctionId();
		parameter.setFunctionSampleId(functionId);

		String sampleName = this.advancedSettingPanel.getSampleName();
		parameter.setSampleName(sampleName);

		File resultDir = new File(result);
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}

		return parameter;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		MetaParameterV1 parameter = getParameter();

		/*
		 * if (parameter.isBuildIn() == false && parameter.isUnipept() == false) {
		 * 
		 * JOptionPane.showMessageDialog(this,
		 * "Taxonomy analysis method was not set, see Tools->Settings", "Warning",
		 * JOptionPane.WARNING_MESSAGE); setCursor(null); return; }
		 */

		if (parameter.getRawFiles()[0].length == 0) {
			JOptionPane.showMessageDialog(this, "No input files.", "Warning", JOptionPane.WARNING_MESSAGE);
			setCursor(null);
			return;
		}

		if (parameter.getDatabase().length() == 0) {
			JOptionPane.showMessageDialog(this, "No database.", "Warning", JOptionPane.WARNING_MESSAGE);
			setCursor(null);
			return;
		}

		if (parameter.getQuanMode().equals(MetaConstants.labelFree)) {
			String[][] labels = parameter.getLabels();
			boolean hasLabel = false;
			L: for (int i = 0; i < labels.length; i++) {
				for (int j = 0; j < labels[i].length; j++) {
					if (labels[i][j].length() > 0) {
						hasLabel = true;
						break L;
					}
				}
			}

			if (hasLabel) {
				JOptionPane.showMessageDialog(this,
						"Quantitative mode is set as \"label free\", but isotopic labels are selected, quantification will still use \"label free\" mode",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}

			String[] isobaric = parameter.getIsobaric();
			boolean hasIsobaric = false;
			for (int i = 0; i < isobaric.length; i++) {
				if (isobaric[i].length() > 0) {
					hasIsobaric = true;
					break;
				}
			}
			if (hasIsobaric) {
				JOptionPane.showMessageDialog(this,
						"Quantitative mode is set as \"label free\", but isobaric labels are selected, quantification will still use \"label free\" mode",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		}

		if (parameter.getQuanMode().equals(MetaConstants.isotopicLabel)) {
			String[][] labels = parameter.getLabels();
			boolean hasLabel = false;
			L: for (int i = 0; i < labels.length; i++) {
				for (int j = 0; j < labels[i].length; j++) {
					if (labels[i][j].length() > 0) {
						hasLabel = true;
						break L;
					}
				}
			}

			if (!hasLabel) {
				JOptionPane.showMessageDialog(this,
						"Quantitative mode is set as \"Isotopic labeling\", but no isotopic labels are selected",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		}

		if (parameter.getQuanMode().equals(MetaConstants.isobaricLabel)) {
			String[] isobaric = parameter.getIsobaric();
			boolean hasIsobaric = false;
			for (int i = 0; i < isobaric.length; i++) {
				if (isobaric[i].length() > 0) {
					hasIsobaric = true;
					break;
				}
			}
			if (!hasIsobaric) {
				JOptionPane.showMessageDialog(this,
						"Quantitative mode is set as \"Isobaric labeling\", but no isobaric labels are found",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		}

		if (parameter.getDigestMode() != MaxquantEnzyme.unspecific
				&& parameter.getDigestMode() != MaxquantEnzyme.no_digest) {

			String[] enzymes = parameter.getEnzymes();
			if (enzymes.length == 0) {
				JOptionPane.showMessageDialog(this, "Digestion mode is set as \""
						+ MaxquantEnzyme.enzymeModes[parameter.getDigestMode()] + "\"" + ", but no enzyme is selected",
						"Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		}

		int rawCount = 0;
		int txtCount = 0;
		int xmlCount = 0;
		String[] raws = parameter.getRawFiles()[0];
		for (int i = 0; i < raws.length; i++) {
			if (raws[i].endsWith("raw") || raws[i].endsWith("RAW")) {
				rawCount++;
			} else if (raws[i].endsWith("mzxml") || raws[i].endsWith("mzXML")) {
				rawCount++;
			} else if (raws[i].endsWith("wiff") || raws[i].endsWith("WIFF")) {
				rawCount++;
			} else if (raws[i].endsWith("txt") || raws[i].endsWith("TXT")) {
				txtCount++;
			} else if (raws[i].endsWith("xml") || raws[i].endsWith("XML")) {
				xmlCount++;
			}
		}

		if (parameter.getWorkflow()[0] || parameter.getWorkflow()[1]) {
			if (rawCount == 0) {
				JOptionPane.showMessageDialog(this, "No raw files.", "Warning", JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		} else {
			if (txtCount == 0 && xmlCount == 0) {
				JOptionPane.showMessageDialog(this, "No peptide identification result files.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				setCursor(null);
				return;
			}
		}

		btnRun.setEnabled(false);
		progressBar1.setValue(0);
		progressBar2.setValue(0);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		this.statusPanel.start();

		MetaMainTask task = new MetaMainTask(parameter, progressBar1, progressBar2) {
			public void done() {

				taskDone();

				if (progressBar1.isIndeterminate()) {
					progressBar1.setIndeterminate(false);
				}

				if (isFinish()) {
					progressBar1.setValue(100);
					progressBar2.setValue(100);
					System.out.println("Task finished!");
				} else {
					System.out.println("Task failed.");
				}

				btnRun.setEnabled(true);
				setCursor(null);
				statusPanel.stop();
			}
		};
		task.execute();
	}

	public JProgressBar getProgressBar1() {
		return progressBar1;
	}

	public JProgressBar getProgressBar2() {
		return progressBar2;
	}

	public JButton getBtnRun() {
		return btnRun;
	}

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

}
