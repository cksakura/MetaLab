package bmi.med.uOttawa.metalab.task.par;

import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.taxonomy.RootType;
import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

public class MetaPep2TaxaPar {

	private boolean buildIn;
	private boolean unipept;
	private HashSet<RootType> usedRootTypes;
	private TaxonomyRanks ignoreBlankRank;
	private HashSet<String> excludeTaxa;
	private int leastPepCount;

	public MetaPep2TaxaPar(boolean buildIn, boolean unipept, HashSet<RootType> usedRootTypes,
			TaxonomyRanks ignoreBlankRank, HashSet<String> excludeTaxa, int leastPepCount) {
		this.buildIn = buildIn;
		this.unipept = unipept;
		this.usedRootTypes = usedRootTypes;
		this.ignoreBlankRank = ignoreBlankRank;
		this.excludeTaxa = excludeTaxa;
		this.leastPepCount = leastPepCount;
	}

	public boolean isBuildIn() {
		return buildIn;
	}

	public void setBuildIn(boolean buildIn) {
		this.buildIn = buildIn;
	}

	public boolean isUnipept() {
		return unipept;
	}

	public void setUnipept(boolean unipept) {
		this.unipept = unipept;
	}

	public HashSet<RootType> getUsedRootTypes() {
		return usedRootTypes;
	}

	public void setUsedRootTypes(HashSet<RootType> usedRootTypes) {
		this.usedRootTypes = usedRootTypes;
	}

	public TaxonomyRanks getIgnoreBlankRank() {
		return ignoreBlankRank;
	}

	public void setIgnoreBlankRank(TaxonomyRanks ignoreBlankRank) {
		this.ignoreBlankRank = ignoreBlankRank;
	}

	public HashSet<String> getExcludeTaxa() {
		return excludeTaxa;
	}

	public void setExcludeTaxa(HashSet<String> excludeTaxa) {
		this.excludeTaxa = excludeTaxa;
	}

	public int getLeastPepCount() {
		return leastPepCount;
	}

	public void setLeastPepCount(int leastPepCount) {
		this.leastPepCount = leastPepCount;
	}

	public static MetaPep2TaxaPar getDefault() {

		HashSet<RootType> usedRootTypes = new HashSet<RootType>();
		usedRootTypes.add(RootType.Archaea);
		usedRootTypes.add(RootType.Bacteria);
		usedRootTypes.add(RootType.Eukaryota);
		usedRootTypes.add(RootType.Viruses);

		TaxonomyRanks ignoreBlankRank = TaxonomyRanks.Family;

		HashSet<String> excludeTaxa = new HashSet<String>();
		excludeTaxa.add("environmental samples");

		MetaPep2TaxaPar mptp = new MetaPep2TaxaPar(true, false, usedRootTypes, ignoreBlankRank, excludeTaxa, 3);
		return mptp;
	}
}
