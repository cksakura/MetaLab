package bmi.med.uOttawa.metalab.task.mag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

public class MagDbItem {

	private String catalogueID;
	private String identifier;
	private MagVersion version;
	private ArrayList<MagVersion> versionList;
	private HashMap<String, ArrayList<String>> mgygNameMap;
	private MagVersion usedVersion;

	private File repositoryFile;
	private File currentFile;
	private File hostDbFile;
	
	private HashMap<String, MagModel> magModelMap;
	private HashMap<String, MagPepLib> magPepLibMap;

	public MagDbItem(String catalogueID, String identifier) {
		this.catalogueID = catalogueID;
		this.identifier = identifier;
		this.versionList = new ArrayList<MagVersion>();

		this.repositoryFile = null;
		
		this.magModelMap = new HashMap<String, MagModel>();
		this.magPepLibMap = new HashMap<String, MagPepLib>();
	}

	public MagDbItem(String catalogueID, String identifier, File directoryFile) {
		this.catalogueID = catalogueID;
		this.identifier = identifier;
		this.versionList = new ArrayList<MagVersion>();

		this.repositoryFile = directoryFile;
		
		this.magModelMap = new HashMap<String, MagModel>();
		this.magPepLibMap = new HashMap<String, MagPepLib>();
	}

	public void addVersion(String catalogueVersion, int speciesCount, Date date, boolean localAvailable) {
		MagVersion version = new MagVersion(catalogueVersion, speciesCount, date, localAvailable);
		this.versionList.add(version);
		this.version = version;
	}
	
	public void addVersion(String catalogueVersion, int speciesCount, Date date, boolean localAvailable, String dbName,
			String funcName, String taxaName) {
		MagVersion version = new MagVersion(catalogueVersion, speciesCount, date, localAvailable, dbName, funcName,
				taxaName);
		this.versionList.add(version);
		this.version = version;
	}

	public MagModel[] getMagModels() {
		MagModel[] magModels = this.magModelMap.values().toArray(MagModel[]::new);
		return magModels;
	}

	public MagPepLib[] getMagPepLibs() {
		MagPepLib[] magPepLibs = this.magPepLibMap.values().toArray(MagPepLib[]::new);
		return magPepLibs;
	}

	public void addModel(String modelName, int trainSize, int modelSize, double corr, double med) {
		this.magModelMap.put(modelName, new MagModel(modelName, trainSize, modelSize, corr, med));
	}

	public void addPepLib(String pepLibName, int pepCount, String protease, int miss) {
		this.magPepLibMap.put(pepLibName, new MagPepLib(pepLibName, pepCount, protease, miss));
	}
	
	public boolean removeModel(MagModel magModel) {
		String modelName = magModel.getModelName();
		MagModel removedModel = this.magModelMap.remove(modelName);
		return removedModel != null;
	}
	
	public boolean removePepLib(MagPepLib magPepLib) {
		String libName = magPepLib.getLibName();
		MagPepLib removedPepLib = this.magPepLibMap.remove(libName);
		return removedPepLib != null;
	}
	
	public void removeAllModels() {
		this.magModelMap.clear();
	}
	
	public void removeAllPepLibs() {
		this.magPepLibMap.clear();
	}
	
	public void addModelFromFile(File file) {
		String modelName = file.getName();
		File infoFile = new File(file, modelName + "_info.txt");
		if (infoFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {
				String line = reader.readLine();
				String[] cs = line.split("\t");
				int trainSize = Integer.parseInt(cs[1]);

				line = reader.readLine();
				cs = line.split("\t");
				int dataSize = Integer.parseInt(cs[1]);

				line = reader.readLine();
				cs = line.split("\t");
				double corr = Double.parseDouble(cs[1]);

				line = reader.readLine();
				cs = line.split("\t");
				double med = Double.parseDouble(cs[1]);

				addModel(modelName, trainSize, dataSize, corr, med);

				reader.close();
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
	
	public void addPepLibFromFile(File file) {
		String pepLibName = file.getName();
		File infoFile = new File(file, pepLibName + "_info.txt");
		if (infoFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(infoFile))) {

				String line = reader.readLine();
				String[] cs = line.split("\t");
				int pepCount = Integer.parseInt(cs[1]);

				line = reader.readLine();
				cs = line.split("\t");
				String enzyme = cs[1];

				line = reader.readLine();
				cs = line.split("\t");
				int miss = Integer.parseInt(cs[1]);
				
				addPepLib(pepLibName, pepCount, enzyme, miss);
				
				reader.close();
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
	
	public File getPredictedPepFile() {
		if (currentFile == null) {
			return null;
		}

		File predictedPepFile = new File(currentFile, "predicted");
		return predictedPepFile;
	}
	
	public void resetMgygMap() {
		mgygNameMap = new HashMap<String, ArrayList<String>>();
	}
	
	public void addMgygName(String parent, String child) {
		if (mgygNameMap == null) {
			mgygNameMap = new HashMap<String, ArrayList<String>>();
		}

		if (!mgygNameMap.containsKey(parent)) {
			mgygNameMap.put(parent, new ArrayList<String>());
		}

		mgygNameMap.get(parent).add(child);
	}

	public HashMap<String, ArrayList<String>> getMgygNameMap() {
		return mgygNameMap;
	}

	public String getCatalogueID() {
		return catalogueID;
	}

	public void setCatalogueID(String catalogueID) {
		this.catalogueID = catalogueID;
	}

	public ArrayList<MagVersion> getVersionList() {
		return versionList;
	}

	public String getCatalogueVersion() {
		if (version == null) {
			return "";
		}
		return version.getCatalogueVersion();
	}

	public int getSpeciesCount() {
		if (version == null) {
			return 0;
		}
		return version.getSpeciesCount();
	}
	
	public void setAvailable() {
		if (version != null) {
			version.localAvailable = true;
		}
	}
	
	public boolean isAvailable() {
		if (version == null) {
			return false;
		}
		return version.isLocalAvailable();
	}
	
	public String getOldVersion() {
		if (versionList.size() == 0) {
			return "";
		} else if (versionList.size() == 1) {
			return version.getCatalogueVersion();
		} else {
			return versionList.get(versionList.size() - 2).getCatalogueVersion();
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Date getDate() {
		if (version == null) {
			return null;
		}
		return version.getDate();
	}

	public MagVersion getUsedVersion() {
		if (usedVersion == null) {
			for (int i = 0; i < versionList.size(); i++) {
				MagVersion veri = versionList.get(i);
				if (veri.isLocalAvailable()) {
					usedVersion = veri;
				}
			}
		}
		return usedVersion;
	}

	public void setUsedVersion(MagVersion usedVersion) {
		this.usedVersion = usedVersion;
		this.currentFile = new File(repositoryFile, usedVersion.getCatalogueVersion());
	}

	public File getRepositoryFile() {
		return repositoryFile;
	}

	public void setRepositoryFile(File repositoryFile) {
		this.repositoryFile = repositoryFile;
	}

	public boolean updateAvailable() {
		if (repositoryFile == null || !repositoryFile.exists()) {
			return true;
		}

		if (version == null) {
			return true;
		}

		File currentFolder = new File(repositoryFile, this.version.getCatalogueVersion());
		if (!currentFolder.exists()) {
			return true;
		}
		return false;
	}

	public File getHostDbFile() {
		return hostDbFile;
	}

	public void setHostDbFile(File hostDbFile) {
		this.hostDbFile = hostDbFile;
	}

	public File getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(File currentFile) {
		this.currentFile = currentFile;
	}

	public File getHapFastaFile() {
		if (repositoryFile == null || !repositoryFile.exists()) {
			return null;
		}
		if (currentFile == null || !currentFile.exists()) {
			return null;
		}

		return new File(currentFile, "rib_elon.fasta");
	}
	
	public File getHapFastaHdfFile() {
		if (repositoryFile == null || !repositoryFile.exists()) {
			return null;
		}
		if (currentFile == null || !currentFile.exists()) {
			return null;
		}

		return new File(currentFile, "rib_elon.hdf");
	}

	public File getHapFastaTDFile() {

		if (repositoryFile == null || !repositoryFile.exists()) {
			return null;
		}
		if (currentFile == null || !currentFile.exists()) {
			return null;
		}

		return new File(currentFile, "rib_elon_td.fasta");
	}

	public File getOriginalDbFolder() {
		if (repositoryFile == null || !repositoryFile.exists()) {
			return null;
		}
		if (usedVersion == null) {
			return null;
		}

		return new File(currentFile, usedVersion.getDbName());
	}
	
	public class MagPepLib {
		private String libName;
		private int pepCount;
		private String protease;
		private int miss;

		public MagPepLib(String libName, int pepCount, String protease, int miss) {
			this.libName = libName;
			this.pepCount = pepCount;
			this.protease = protease;
			this.miss = miss;
		}

		public String getLibName() {
			return libName;
		}

		public int getPepCount() {
			return pepCount;
		}

		public String getProtease() {
			return protease;
		}

		public int getMiss() {
			return miss;
		}

		public void delete() {
			File modelFolder = new File(new File(currentFile, "libraries"), libName);
			FileUtils.deleteQuietly(modelFolder);
		}

	}
	
	public class MagModel {
		private String modelName;
		private int trainSize;
		private int modelSize;
		private double corr;
		private double med;

		public MagModel(String modelName, int trainSize, int modelSize, double corr, double med) {
			this.modelName = modelName;
			this.trainSize = trainSize;
			this.modelSize = modelSize;
			this.corr = corr;
			this.med = med;
		}

		public String getModelName() {
			return modelName;
		}

		public void setModelName(String modelName) {
			this.modelName = modelName;
		}

		public int getTrainSize() {
			return trainSize;
		}

		public void setTrainSize(int trainSize) {
			this.trainSize = trainSize;
		}

		public int getModelSize() {
			return modelSize;
		}

		public void setModelSize(int modelSize) {
			this.modelSize = modelSize;
		}

		public double getCorr() {
			return corr;
		}

		public void setCorr(double corr) {
			this.corr = corr;
		}

		public double getMed() {
			return med;
		}

		public void setMed(double med) {
			this.med = med;
		}

		public File getModelFile() {
			File modelFolder = new File(new File(currentFile, "models"), modelName);
			if (!modelFolder.exists()) {
				return null;
			}

			File modelFile = new File(modelFolder, modelName + ".h5");
			if (!modelFile.exists()) {
				return null;
			}

			return modelFile;
		}

		public void delete() {
			File modelFolder = new File(new File(currentFile, "models"), modelName);
			FileUtils.deleteQuietly(modelFolder);
		}
	}
	
	public class MagVersion {

		private String catalogueVersion;
		private int speciesCount;
		private Date date;
		private boolean localAvailable;
		
		private String dbName;
		private String funcName;
		private String taxName;

		MagVersion(String catalogueVersion, int speciesCount, Date date, boolean localAvailable) {
			this.catalogueVersion = catalogueVersion;
			this.speciesCount = speciesCount;
			this.date = date;
			this.localAvailable = localAvailable;
			this.dbName = "original_db";
			this.funcName = "eggNOG";
			this.taxName = "genomes-all_metadata.tsv";
		}
		
		MagVersion(String catalogueVersion, int speciesCount, Date date, boolean localAvailable, String dbName, String funcName, String taxName) {
			this.catalogueVersion = catalogueVersion;
			this.speciesCount = speciesCount;
			this.date = date;
			this.localAvailable = localAvailable;
			this.dbName = dbName;
			this.funcName = funcName;
			this.taxName = taxName;
		}

		public String getCatalogueVersion() {
			return catalogueVersion;
		}

		public int getSpeciesCount() {
			return speciesCount;
		}

		public Date getDate() {
			return date;
		}

		public boolean isLocalAvailable() {
			return localAvailable;
		}
		
		public String getDbName() {
			return dbName;
		}

		public String getFuncName() {
			return funcName;
		}

		public String getTaxName() {
			return taxName;
		}

		public String toString() {
			return catalogueVersion;
		}
	}

	public String toString() {
		return catalogueID;
	}

	public int hashCode() {
		return (catalogueID + "_" + this.usedVersion.catalogueVersion).hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof MagDbItem) {
			MagDbItem magDbItem = (MagDbItem) obj;
			if (!magDbItem.catalogueID.equals(this.catalogueID)) {
				return false;
			}

			if (magDbItem.usedVersion == null || this.usedVersion == null) {
				return true;
			}

			if (!magDbItem.usedVersion.catalogueVersion.equals(this.usedVersion.catalogueVersion)) {
				return false;
			}
			return true;
		} else {
			return false;

		}
	}

	public Object[] getTableRow() {
		Object[] objs = new Object[] { isAvailable(), catalogueID, getCatalogueVersion(), getSpeciesCount(),
				updateAvailable() };
		return objs;
	}

	public static Object[] getColumnNames() {
		Object[] objs = new Object[] { "Saved in local", "Catalogue ID", "Version", "Species Count",
				"Update available" };
		return objs;
	}
}
