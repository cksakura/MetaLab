package bmi.med.uOttawa.metalab.task.hgm.par;

import bmi.med.uOttawa.metalab.core.mod.IsobaricTag;
import bmi.med.uOttawa.metalab.dbSearch.MaxQuant.MaxquantModification;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;
import bmi.med.uOttawa.metalab.task.par.MetaPep2TaxaPar;
import bmi.med.uOttawa.metalab.task.pfind.par.MetaParameterPFind;
import bmi.med.uOttawa.metalab.task.v2.par.MetaOpenPar;

public class MetaParameterHGM extends MetaParameterPFind {

	public MetaParameterHGM(MetaParameter metaPar) {
		super(metaPar);
	}

	public MetaParameterHGM(MetaParameter metaPar, String[] fixMods, String[] variMods, String enzyme,
			int missCleavages, int digestMode, String quanMode, MaxquantModification[] isobaric,
			IsobaricTag isobaricTag, boolean combineLabel, double[][] isoCorFactor) {
		super(metaPar, fixMods, variMods, enzyme, missCleavages, digestMode, quanMode, isobaric, isobaricTag,
				combineLabel, isoCorFactor);
		this.osp = MetaOpenPar.getDefault();
		this.ptp = MetaPep2TaxaPar.getDefault();
	}

}
