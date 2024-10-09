package bmi.med.uOttawa.metalab.quant.flashLFQ;

import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;

public class FlashLfqQuanProtein extends MetaProtein {

	public FlashLfqQuanProtein(String name, double[] intensities) {
		super(name, intensities);
		// TODO Auto-generated constructor stub
	}

	public Object[] getTableObjects() {

		Object[] objs = new Object[2 + intensities.length];
		objs[0] = name;
		int total = 0;
		for (int i = 0; i < intensities.length; i++) {
			objs[i + 2] = intensities[i];
			total += intensities[i];
		}
		objs[1] = total;

		return objs;
	}
}
