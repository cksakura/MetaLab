package bmi.med.uOttawa.metalab.dbSearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PepProGroup {

	private int id;
	private HashSet<String> pepset;
	private HashMap<String, Integer> proCountMap;

	public PepProGroup() {
		this.pepset = new HashSet<String>(100000);
		this.proCountMap = new HashMap<String, Integer>(100000);
	}
	
	public PepProGroup(int id) {
		this.id = id;
		this.pepset = new HashSet<String>(100000);
		this.proCountMap = new HashMap<String, Integer>(100000);
	}

	public PepProGroup(HashSet<String> pepset, HashMap<String, Integer> proCountMap) {
		this.pepset = pepset;
		this.proCountMap = proCountMap;
	}

	public HashSet<String> getPepset() {
		return pepset;
	}

	public HashMap<String, Integer> getProCountMap() {
		return proCountMap;
	}

	public void setPepset(HashSet<String> pepset) {
		this.pepset = pepset;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addPep(String pep) {

	}

	public boolean containsPep(String pep) {
		return this.pepset.contains(pep);
	}

	public void addPep(String pep, HashSet<String> pros) {
		this.pepset.add(pep);
		for (String pro : pros) {
			if (proCountMap.containsKey(pro)) {
				proCountMap.put(pro, proCountMap.get(pro) + 1);
			} else {
				proCountMap.put(pro, 1);
			}
		}
	}

	public void refine(HashMap<String, HashSet<String>> proPepMap) {

		HashSet<String> refindSet = new HashSet<String>();
		for (String pro : proCountMap.keySet()) {
			int count = proCountMap.get(pro);
			if (count == pepset.size()) {
				refindSet.add(pro);
			}
		}

		if (refindSet.size() > 0) {
			Iterator<String> it = this.proCountMap.keySet().iterator();
			while (it.hasNext()) {
				String pro = it.next();
				if (!refindSet.contains(pro)) {
					it.remove();
				}
			}
			return;
		}

		for (int i = pepset.size() - 1; i > 0; i--) {
			for (String pro : proCountMap.keySet()) {
				int count = proCountMap.get(pro);
				if (count == i) {
					refindSet.add(pro);
				}
			}

			HashSet<String> pepSet = new HashSet<String>();
			for (String pro : refindSet) {
				pepSet.addAll(proPepMap.get(pro));
			}

			if (pepSet.size() == this.pepset.size()) {
				Iterator<String> it = this.proCountMap.keySet().iterator();
				while (it.hasNext()) {
					String pro = it.next();
					if (!refindSet.contains(pro)) {
						it.remove();
					}
				}
				return;
			}
		}
	}
}
