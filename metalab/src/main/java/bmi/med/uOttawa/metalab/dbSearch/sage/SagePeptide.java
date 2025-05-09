package bmi.med.uOttawa.metalab.dbSearch.sage;

import java.util.ArrayList;

import org.dom4j.Element;

import bmi.med.uOttawa.metalab.task.io.pep.MetaPeptide;

public class SagePeptide extends MetaPeptide {

	private ArrayList<SagePSM> psmlist;

	public SagePeptide(String sequence, String modSeq, String[] proteins, double[] intensity, int[] idenType,
			double qvalue, double score) {
		super(sequence, modSeq, proteins, intensity, idenType);
		this.PEP = qvalue;
		this.score = score;
		this.psmlist = new ArrayList<SagePSM>();
	}

	public void addPSM(SagePSM psm) {
		this.psmlist.add(psm);
	}

	public SagePSM[] getPSMs() {
		SagePSM[] psms = psmlist.toArray(SagePSM[]::new);
		return psms;
	}

	@Override
	public Object[] getTableObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getXmlPepElement() {
		// TODO Auto-generated method stub
		return null;
	}
}
