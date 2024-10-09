package bmi.med.uOttawa.metalab.task.pfind.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.task.MetaLabWorkflowType;
import bmi.med.uOttawa.metalab.task.mag.MagHapPFindTask;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaParameterIO;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParaIOPFind;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.gui.MetaLabSourcePathDialog;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParaIOMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaParameterMQ;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesIOV2;
import bmi.med.uOttawa.metalab.task.v2.par.MetaSourcesV2;
import net.miginfocom.swing.MigLayout;

public class MetaLabPFindMainFrame extends JFrame {

	protected Logger LOGGER = LogManager.getLogger(MagHapPFindTask.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4049164771443540698L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				MetaLabPFindMainFrame frame = null;
				try {
					frame = new MetaLabPFindMainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					frame.LOGGER.error(e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public MetaLabPFindMainFrame() throws IOException, URISyntaxException {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 20, 1440, 1024);
		setMinimumSize(new Dimension(1280, 1024));
		initial();
	}

	protected void initial() throws IOException, URISyntaxException {
		this.getContentPane().setLayout(new MigLayout("", "[1200:1600:1920,grow]", "[800:900:1080,grow]"));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		tabbedPane.setFont(new Font("Tahoma", Font.PLAIN, 14));

		this.getContentPane().add(tabbedPane, "cell 0 0,grow");

		File parameter = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath(),
				"parameters" + MetaParaIOMQ.versionFile);

		File resources = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath(),
				"resources" + MetaParaIOMQ.versionFile);

		MetaSourcesV2 msv2 = MetaSourcesIOV2.parse(resources);

		{
			MetaParameter par = MetaParameterIO.parse(parameter);
			MetaParameterMQ parV2 = par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow
					? MetaParaIOMQ.parse(parameter)
					: new MetaParameterMQ();
			parV2.setWorkflowType(MetaLabWorkflowType.MaxQuantWorkflow.name());
			MetaLabPFindMainPanel maxquantPanel = new MetaLabPFindMainPanel(parV2, msv2);

			tabbedPane.addTab("<html><br/>MaxQuant<br/>workflow<br/></html>", null, maxquantPanel, null);
			if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow) {
				tabbedPane.setSelectedIndex(0);
			}
		}
/*
		{
			MetaParameter par = MetaParameterIO.parse(parameter);
			MetaParameterV2 parV2 = par.getWorkflowType() == MetaLabWorkflowType.MSFraggerWorkflow
					? MetaParaIOV2.parse(parameter)
					: new MetaParameterV2();
			parV2.setWorkflowType(MetaLabWorkflowType.MSFraggerWorkflow.name());
			MetaLabPFindMainPanel msfraggerPanel = new MetaLabPFindMainPanel(parV2, msv2);

			tabbedPane.addTab("<html><br/>MSFragger<br/>workflow<br/></html>", null, msfraggerPanel, null);
			if (par.getWorkflowType() == MetaLabWorkflowType.MSFraggerWorkflow) {
				tabbedPane.setSelectedIndex(1);
			}
		}
*/
		{
			MetaParameter par = MetaParameterIO.parse(parameter);
			MetaParameterPFind parV3 = par.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow
					? MetaParaIOPFind.parse(parameter)
					: new MetaParameterPFind();
			parV3.setWorkflowType(MetaLabWorkflowType.pFindWorkflow.name());
			MetaLabPFindMainPanel pfindPanel = new MetaLabPFindMainPanel(parV3, msv2);

			tabbedPane.addTab("<html><br/>pFind<br/>workflow<br/></html>", null, pfindPanel, null);
			if (par.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow) {
				tabbedPane.setSelectedIndex(1);
			}
		}

		{
			MetaParameter par = MetaParameterIO.parse(parameter);
			MetaParameterMQ parV2 = par.getWorkflowType() == MetaLabWorkflowType.TaxonomyAnalysis
					? MetaParaIOMQ.parse(parameter)
					: new MetaParameterMQ(par);
			par.setMetadata(new MetaData());
			parV2.setWorkflowType(MetaLabWorkflowType.TaxonomyAnalysis.name());
			MetaLabPFindMainPanel taxaPanel = new MetaLabPFindMainPanel(parV2, msv2);
			tabbedPane.addTab("<html><br/>Taxonomy<br/>analysis<br/></html>", null, taxaPanel, null);
			if (par.getWorkflowType() == MetaLabWorkflowType.TaxonomyAnalysis) {
				tabbedPane.setSelectedIndex(2);
			}
		}

		{
			MetaParameter par = MetaParameterIO.parse(parameter);
			MetaParameterMQ parV2 = par.getWorkflowType() == MetaLabWorkflowType.FunctionalAnnotation
					? MetaParaIOMQ.parse(parameter)
					: new MetaParameterMQ(par);
			par.setMetadata(new MetaData());
			parV2.setWorkflowType(MetaLabWorkflowType.FunctionalAnnotation.name());
			MetaLabPFindMainPanel funcPanel = new MetaLabPFindMainPanel(parV2, msv2);
			tabbedPane.addTab("<html><br/>Functional<br/>annotation<br/></html>", null, funcPanel, null);
			if (par.getWorkflowType() == MetaLabWorkflowType.FunctionalAnnotation) {
				tabbedPane.setSelectedIndex(3);
			}
		}

		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MetaLabPFindMainFrame.class.getResource("/bmi/med/uOttawa/metalab/icon/metalab_128.png")));

		setTitle("MetaLab " + MetaParaIOMQ.version);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmLoadParameter = new JMenuItem("Load parameter");
		mntmLoadParameter.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));

		mntmLoadParameter.addActionListener(l -> {

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load parameter file");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.addChoosableFileFilter(new FileFilter() {
				public String getDescription() {
					return "MetaLab parameter file (.json)";
				}

				public boolean accept(File f) {
					if (f.getName().endsWith("json")) {
						return true;
					} else {
						return false;
					}
				}
			});

			int returnValue = fileChooser.showOpenDialog(MetaLabPFindMainFrame.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				MetaParameter loadPar = MetaParameterIO.parse(file);

				MetaLabWorkflowType type = loadPar.getWorkflowType();

				switch (type) {
				case MaxQuantWorkflow:

					tabbedPane.remove(0);
					MetaLabPFindMainPanel maxquantPanel = new MetaLabPFindMainPanel((MetaParameterMQ) loadPar, msv2);
					tabbedPane.insertTab("<html><br/>MaxQuant<br/>workflow<br/></html>", null, maxquantPanel, null, 0);
					tabbedPane.setSelectedIndex(0);

					break;
/*
				case MSFraggerWorkflow:

					tabbedPane.remove(1);
					MetaLabPFindMainPanel msfraggerPanel = new MetaLabPFindMainPanel((MetaParameterV2) loadPar, msv2);
					tabbedPane.insertTab("<html><br/>MSFragger<br/>workflow<br/></html>", null, msfraggerPanel, null,
							1);
					tabbedPane.setSelectedIndex(1);

					break;
*/
				case pFindWorkflow:

					tabbedPane.remove(1);
					MetaLabPFindMainPanel pfindPanel = new MetaLabPFindMainPanel((MetaParameterPFind) loadPar, msv2);
					tabbedPane.insertTab("<html><br/>pFind<br/>workflow<br/></html>", null, pfindPanel, null, 1);
					tabbedPane.setSelectedIndex(1);

					break;

				case TaxonomyAnalysis:

					tabbedPane.remove(2);
					MetaLabPFindMainPanel taxonPanel = new MetaLabPFindMainPanel((MetaParameterMQ) loadPar, msv2);
					tabbedPane.insertTab("<html><br/>Taxonomy<br/>analysis<br/></html>", null, taxonPanel, null, 2);
					tabbedPane.setSelectedIndex(2);

					break;

				case FunctionalAnnotation:

					tabbedPane.remove(3);
					MetaLabPFindMainPanel funcPanel = new MetaLabPFindMainPanel((MetaParameterMQ) loadPar, msv2);
					tabbedPane.insertTab("<html><br/>Functional<br/>annotation<br/></html>", null, funcPanel, null, 3);
					tabbedPane.setSelectedIndex(3);

					break;

				default:
					break;
				}
			}

		});
		mnFile.add(mntmLoadParameter);

		JMenuItem mntmSaveParameter = new JMenuItem("Save parameter");
		mntmSaveParameter.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
		mnFile.add(mntmSaveParameter);
		mntmSaveParameter.addActionListener(l -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save parameter file");

			int value = fileChooser.showSaveDialog(MetaLabPFindMainFrame.this);

			if (value == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				if (!fileToSave.getName().endsWith(".json")) {
					fileToSave = new File(fileToSave.getParent(), fileToSave.getName() + ".json");
				}

				MetaLabPFindMainPanel panel = (MetaLabPFindMainPanel) tabbedPane.getSelectedComponent();
				panel.updateParameter();
				MetaParameter par = panel.getParameter();

				if (par.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow) {
					MetaParaIOPFind.export((MetaParameterPFind) par, fileToSave);
				} else if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow
						|| par.getWorkflowType() == MetaLabWorkflowType.MSFraggerWorkflow) {
					MetaParaIOMQ.export((MetaParameterMQ) par, fileToSave);
				}
			}
		});

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Stop16.gif")));
		mntmExit.addActionListener(l -> {
			this.dispose();
		});
		mnFile.add(mntmExit);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		JMenuItem mntmVerify = new JMenuItem("Activation");
		mntmVerify.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Add16.gif")));
		mntmVerify.addActionListener(l -> {
			MetaLabLicenseFrame frame;
			try {
				frame = new MetaLabLicenseFrame();
				frame.setAlwaysOnTop(true);
				frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			} catch (URISyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		mnTools.add(mntmVerify);

		JMenuItem mntmDownloadDb = new JMenuItem("Download database");
		mntmDownloadDb.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Import16.gif")));
		mntmDownloadDb.addActionListener(l -> {
			MetaLabDownloadFrame frame = new MetaLabDownloadFrame();
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		});
		mnTools.add(mntmDownloadDb);

		JMenu mnSetting = new JMenu("Setting");
		menuBar.add(mnSetting);

		JMenuItem mntmResource = new JMenuItem("Resource");
		mntmResource.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Bookmarks16.gif")));
		mntmResource.addActionListener(l -> {
			MetaLabSourcePathDialog dialog = new MetaLabSourcePathDialog(msv2, resources, this);
			dialog.setAlwaysOnTop(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		});
		mnSetting.add(mntmResource);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmHelp = new JMenuItem("Help");
		mntmHelp.setIcon(
				new ImageIcon(MetaLabPFindMainFrame.class.getResource("/toolbarButtonGraphics/general/Help16.gif")));
		mntmHelp.addActionListener(l -> {
			try {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://wiki.imetalab.ca/"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		mnHelp.add(mntmHelp);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				MetaLabPFindMainPanel panel = (MetaLabPFindMainPanel) tabbedPane.getSelectedComponent();
				panel.updateParameter();
				MetaParameter par = panel.getParameter();

				if (par.getWorkflowType() == MetaLabWorkflowType.pFindWorkflow) {
					MetaParaIOPFind.export((MetaParameterPFind) par, parameter);
				} else if (par.getWorkflowType() == MetaLabWorkflowType.MaxQuantWorkflow
						|| par.getWorkflowType() == MetaLabWorkflowType.MSFraggerWorkflow) {
					MetaParaIOMQ.export((MetaParameterMQ) par, parameter);
				} else if (par.getWorkflowType() == MetaLabWorkflowType.TaxonomyAnalysis
						|| par.getWorkflowType() == MetaLabWorkflowType.FunctionalAnnotation) {
					MetaParaIOMQ.export((MetaParameterMQ) par, parameter);
				}

				e.getWindow().dispose();
			}
		});
	}
}
