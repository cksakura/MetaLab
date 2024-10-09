package bmi.med.uOttawa.metalab.task.dia.par;

import bmi.med.uOttawa.metalab.dbSearch.diann.DiannParameter;
import bmi.med.uOttawa.metalab.task.dia.DiaLibSearchPar;
import bmi.med.uOttawa.metalab.task.mag.par.MetaParameterMag;
import bmi.med.uOttawa.metalab.task.par.MetaParameter;

public class MetaDiaParSearchLib extends MetaParameterMag {

	private DiaLibSearchPar diaLibSearchPar;
	private MetaDiaParGenLib diaParGenLib;
	private boolean isTimsTof;

	public MetaDiaParSearchLib(MetaParameter metaPar, String magDb, String magDbVersion) {
		super(metaPar);
		// TODO Auto-generated constructor stub
		this.magDb = magDb;
		this.magDbVersion = magDbVersion;
		this.diaLibSearchPar = new DiaLibSearchPar();
	}

	public MetaDiaParSearchLib(MetaParameter metaPar, String magDb, String magDbVersion,
			DiaLibSearchPar diaLibSearchPar) {
		super(metaPar);
		// TODO Auto-generated constructor stub
		this.magDb = magDb;
		this.magDbVersion = magDbVersion;
		this.diaLibSearchPar = diaLibSearchPar;
	}

	public MetaDiaParSearchLib(MetaParameter metaPar, String magDb, String magDbVersion,
			DiaLibSearchPar diaLibSearchPar, MetaDiaParGenLib diaParGenLib) {
		super(metaPar);
		// TODO Auto-generated constructor stub
		this.magDb = magDb;
		this.magDbVersion = magDbVersion;
		this.diaLibSearchPar = diaLibSearchPar;
		this.diaParGenLib = diaParGenLib;
	}

	public DiaLibSearchPar getDiaLibSearchPar() {
		return diaLibSearchPar;
	}

	public void setDiaLibSearchPar(DiaLibSearchPar diaLibSearchPar) {
		this.diaLibSearchPar = diaLibSearchPar;
	}

	public DiannParameter getDiannPar() {
		return diaParGenLib.getDiannPar();
	}

	public MetaDiaParGenLib getDiaParGenLib() {
		return diaParGenLib;
	}

	public void setDiaParGenLib(MetaDiaParGenLib diaParGenLib) {
		this.diaParGenLib = diaParGenLib;
	}

	public boolean isTimsTof() {
		return isTimsTof;
	}

	public void setTimsTof(boolean isTimsTof) {
		this.isTimsTof = isTimsTof;
	}

}
