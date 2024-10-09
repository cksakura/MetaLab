package bmi.med.uOttawa.metalab.task.mag.par;

import java.io.File;
import java.util.ArrayList;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.hgm.par.MetaParameterHGM;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem;
import bmi.med.uOttawa.metalab.task.mag.MagDbItem.MagVersion;
import bmi.med.uOttawa.metalab.task.par.MetaData;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;

public class MetaParameterMag extends MetaParameterHGM {

	protected MagDbItem[][] magDbItems;
	protected String[] fragpipeEnzyme;
	protected MagDbItem usedMagDbItem;
	protected String magDb;
	protected String magDbVersion;

	public MetaParameterMag(MetaParameter metaPar) {
		super(metaPar);
	}

	public MetaParameterMag(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[] isobaric,
			IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor, String magDb, String magDbVersion) {
		super(metaPar, fixMods, variMods, enzyme, missCleavages, digestMode, quanMode, isobaric, isobaricTag,
				combineLabel, isoCorFactor);
		this.magDb = magDb;
		this.magDbVersion = magDbVersion;
	}

	public MetaParameterMag(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[] isobaric,
			IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor, String magDb, String magDbVersion,
			MagDbItem[][] magDbItems) {
		super(metaPar, fixMods, variMods, enzyme, missCleavages, digestMode, quanMode, isobaric, isobaricTag,
				combineLabel, isoCorFactor);
		this.magDb = magDb;
		this.magDbVersion = magDbVersion;
		this.magDbItems = magDbItems;
		for (int i = 0; i < magDbItems.length; i++) {
			for (int j = 0; j < magDbItems[i].length; j++) {
				if (magDb.equals(magDbItems[i][j].getCatalogueID())) {
					usedMagDbItem = magDbItems[i][j];

					ArrayList<MagVersion> verlist = magDbItems[i][j].getVersionList();
					for (int k = 0; k < verlist.size(); k++) {
						if (verlist.get(k).getCatalogueVersion().equals(magDbVersion)) {
							usedMagDbItem.setUsedVersion(verlist.get(k));
						}
					}
				}
			}
		}
	}

	public boolean hasRawFiles() {
		MetaData metaData = this.getMetadata();
		if (metaData == null) {
			return false;
		} else {
			String[] raws = metaData.getRawFiles();
			if (raws == null || raws.length == 0) {
				return false;
			} else {
				for (int i = 0; i < raws.length; i++) {
					if (!(new File(raws[i])).exists()) {
						return false;
					}
				}
				return true;
			}
		}
	}

	public String[] getFragpipeEnzyme() {
		return fragpipeEnzyme;
	}

	public void setFragpipeEnzyme(String[] fragpipeEnzyme) {
		this.fragpipeEnzyme = fragpipeEnzyme;
	}

	public MagDbItem[][] getMagDbItems() {
		return magDbItems;
	}

	public void setMagDbItems(MagDbItem[][] magDbItems) {
		this.magDbItems = magDbItems;
	}

	public MagDbItem getUsedMagDbItem() {
		return usedMagDbItem;
	}

	public void setUsedMagDbItem(MagDbItem usedMagDbItem) {
		this.usedMagDbItem = usedMagDbItem;
	}

	public String getMagDb() {
		return magDb;
	}

	public String getMagDbVersion() {
		return magDbVersion;
	}

	public MagDbItem[] getAvailableMagDbItem() {
		ArrayList<MagDbItem> list = new ArrayList<MagDbItem>();
		if (magDbItems != null && magDbItems.length > 0) {
			for (int i = 0; i < magDbItems.length; i++) {
				for (int j = 0; j < magDbItems[i].length; j++) {
					boolean use = false;
					ArrayList<MagVersion> verlist = magDbItems[i][j].getVersionList();
					for (int k = 0; k < verlist.size(); k++) {
						if (verlist.get(k).isLocalAvailable()) {
							use = true;
							break;
						}
					}

					if (use) {
						list.add(magDbItems[i][j]);
					}
				}
			}
		}
		MagDbItem[] items = list.toArray(new MagDbItem[list.size()]);
		return items;
	}
}
