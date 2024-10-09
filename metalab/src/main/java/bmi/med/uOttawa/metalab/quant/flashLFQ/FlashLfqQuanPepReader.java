package bmi.med.uOttawa.metalab.quant.flashLFQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.dbSearch.pfind.PFindResultReader;
import bmi.med.uOttawa.metalab.task.io.pep.AbstractMetaPeptideReader;
import bmi.med.uOttawa.metalab.task.par.MetaConstants;

public class FlashLfqQuanPepReader extends AbstractMetaPeptideReader {

	protected BufferedReader reader;
	protected static int sequenceId = -1;
	protected static int modSeqId = -1;
	protected static int proteinId = -1;

	protected String[] intensityTitles;
	protected HashMap<String, Integer> intensityTitleIdMap;
	protected String[] idenTypeTitles;
	protected HashMap<String, Integer> idenTypeTitleIdMap;
	protected String[] title;
	protected String line;

	private static Logger LOGGER = LogManager.getLogger(FlashLfqQuanPepReader.class);

	public FlashLfqQuanPepReader(String quanPep) {
		this(new File(quanPep));
	}

	public FlashLfqQuanPepReader(File quanPep) {
		super(quanPep);
		try {
			this.reader = new BufferedReader(new FileReader(quanPep));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ search result file " + quanPep, e);
		}
		this.parseTitle();
	}
	
	public FlashLfqQuanPepReader(String quanPep, ArrayList<String> fileList) {
		this(new File(quanPep), fileList);
	}

	public FlashLfqQuanPepReader(File quanPep, ArrayList<String> fileList) {
		super(quanPep);
		try {
			this.reader = new BufferedReader(new FileReader(quanPep));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ search result file " + quanPep, e);
		}
		this.parseTitle(fileList);
	}
	
	public FlashLfqQuanPepReader(String quanPep, ArrayList<String> fileList, String[] reportTitle) {
		this(new File(quanPep), fileList, reportTitle);
	}

	public FlashLfqQuanPepReader(File quanPep, ArrayList<String> fileList, String[] reportTitle) {
		super(quanPep);
		try {
			this.reader = new BufferedReader(new FileReader(quanPep));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ search result file " + quanPep, e);
		}
		this.parseTitle(fileList, reportTitle);
	}

	protected void parseTitle() {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> intensityTitleList = new ArrayList<String>();
		idenTypeTitleIdMap = new HashMap<String, Integer>();
		ArrayList<String> idenTypeList = new ArrayList<String>();

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}

		this.title = line.split("\t");
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				modSeqId = i;
			} else if (title[i].equals("Base Sequence")) {
				sequenceId = i;
			} else if (title[i].equals("Protein Groups")) {
				proteinId = i;
			} else if (title[i].startsWith("Intensity_")) {
				intensityTitleIdMap.put(title[i], i);
				intensityTitleList.add(title[i]);
			} else if (title[i].startsWith("Detection Type_")) {
				idenTypeTitleIdMap.put(title[i], i);
				idenTypeList.add(title[i]);
			}
		}

		if (sequenceId == -1) {
			sequenceId = modSeqId;
		}

		this.intensityTitles = intensityTitleList.toArray(String[]::new);
		this.idenTypeTitles = idenTypeList.toArray(String[]::new);
	}
	
	protected void parseTitle(ArrayList<String> fileList) {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		idenTypeTitleIdMap = new HashMap<String, Integer>();
		this.intensityTitles = new String[fileList.size()];
		this.idenTypeTitles = new String[fileList.size()];

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}

		this.title = line.split("\t");
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				modSeqId = i;
			} else if (title[i].equals("Base Sequence")) {
				sequenceId = i;
			} else if (title[i].equals("Protein Groups")) {
				proteinId = i;
			} else if (title[i].startsWith("Intensity_")) {
				intensityTitleIdMap.put(title[i], i);
				for (int j = 0; j < fileList.size(); j++) {
					if (title[i].equals("Intensity_" + fileList.get(j))) {
						this.intensityTitles[j] = title[i];
					}
				}
			} else if (title[i].startsWith("Detection Type_")) {
				idenTypeTitleIdMap.put(title[i], i);
				for (int j = 0; j < fileList.size(); j++) {
					if (title[i].equals("Detection Type_" + fileList.get(j))) {
						this.idenTypeTitles[j] = title[i];
					}
				}
			}
		}

		if (sequenceId == -1) {
			sequenceId = modSeqId;
		}
	}

	protected void parseTitle(ArrayList<String> fileList, String[] reportTitle) {
		// TODO Auto-generated method stub
		intensityTitleIdMap = new HashMap<String, Integer>();
		idenTypeTitleIdMap = new HashMap<String, Integer>();
		this.intensityTitles = new String[fileList.size() * reportTitle.length];
		this.idenTypeTitles = new String[fileList.size()];

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading FlashLFQ quantification result file " + super.getFile(), e);
		}

		this.title = line.split("\t");
		for (int i = 0; i < title.length; i++) {
			if (title[i].equals("Sequence")) {
				modSeqId = i;
			} else if (title[i].equals("Base Sequence")) {
				sequenceId = i;
			} else if (title[i].equals("Protein Groups")) {
				proteinId = i;
			} else if (title[i].startsWith("Intensity_")) {
				intensityTitleIdMap.put(title[i], i);
				for (int j = 0; j < fileList.size(); j++) {
					String fileJ = fileList.get(j);
					for (int k = 0; k < reportTitle.length; k++) {
						if (title[i].equals("Intensity_" + fileJ + "_" + reportTitle[k])) {
							this.intensityTitles[j * reportTitle.length + k] = title[i];
						}
					}
				}
			} else if (title[i].startsWith("Detection Type_")) {
				idenTypeTitleIdMap.put(title[i], i);
				for (int j = 0; j < fileList.size(); j++) {
					if (title[i].equals("Detection Type_" + fileList.get(j))) {
						this.idenTypeTitles[j] = title[i];
					}
				}
			}
		}

		if (sequenceId == -1) {
			sequenceId = modSeqId;
		}
	}
	
	protected FlashLfqQuanPeptide parse() {
		String[] cs = line.split("\t");
		String[] ps = proteinId == -1 ? new String[] {} : cs[proteinId].split(";");
		String[] proteins = new String[ps.length];
		boolean decoy = false;
		for (int i = 0; i < proteins.length; i++) {
			proteins[i] = ps[i];
			if (proteins[i].startsWith(PFindResultReader.REV)) {
				decoy = true;
			}
		}

		double[] intensity = new double[intensityTitles.length];
		for (int i = 0; i < intensity.length; i++) {
			int id = this.intensityTitleIdMap.get(intensityTitles[i]);
			if (id > -1 && id < cs.length) {
				intensity[i] = Double.parseDouble(cs[id]);
			}
		}

		int[] idenType = new int[idenTypeTitles.length];
		for (int i = 0; i < idenType.length; i++) {
			int id = this.idenTypeTitleIdMap.get(idenTypeTitles[i]);
			if (id > -1 && id < cs.length) {
				if (cs[id].equals("MSMS")) {
					idenType[i] = 0;
				} else if (cs[id].equals("MBR")) {
					idenType[i] = 1;
				} else if (cs[id].equals("NotDetected")) {
					idenType[i] = 2;
				}
			}
		}

		FlashLfqQuanPeptide peptide = new FlashLfqQuanPeptide(cs[sequenceId], cs[modSeqId], proteins, intensity,
				idenType, decoy);

		return peptide;
	}

	@Override
	public String getQuanMode() {
		// TODO Auto-generated method stub
		return MetaConstants.labelFree;
	}

	@Override
	public FlashLfqQuanPeptide[] getMetaPeptides() {
		// TODO Auto-generated method stub
		ArrayList<FlashLfqQuanPeptide> list = new ArrayList<FlashLfqQuanPeptide>();
		try {
			while ((line = reader.readLine()) != null) {
				FlashLfqQuanPeptide pep = parse();
				if (pep != null) {
					list.add(pep);
				}
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading peptide from " + this.getFile(), e);
		}

		FlashLfqQuanPeptide[] peps = list.toArray(new FlashLfqQuanPeptide[list.size()]);
		return peps;
	}

	@Override
	public Object[] getTitleObjs() {
		// TODO Auto-generated method stub
		Object[] titleObjs;
		if (proteinId > -1) {
			titleObjs = new Object[3 + intensityTitles.length];
			titleObjs[0] = this.title[sequenceId];
			titleObjs[1] = this.title[proteinId];
			titleObjs[2] = "Intensity";
			for (int i = 0; i < intensityTitles.length; i++) {
				titleObjs[i + 3] = intensityTitles[i];
			}
		} else {
			titleObjs = new Object[2 + intensityTitles.length];
			titleObjs[0] = this.title[sequenceId];
			titleObjs[1] = "Intensity";
			for (int i = 0; i < intensityTitles.length; i++) {
				titleObjs[i + 2] = intensityTitles[i];
			}
		}

		return titleObjs;
	}

	@Override
	public String[] getIntensityTitle() {
		// TODO Auto-generated method stub
		return this.intensityTitles;
	}
	
	public static void main(String[] args) {
		
	}
}
