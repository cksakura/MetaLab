/**
 * 
 */
package bmi.med.uOttawa.metalab.core.taxonomy;

/**
 * @author Kai Cheng
 *
 */
public class Taxon {

	private int id;
	private int parentId;
	private int rankId;
	private String name;
	private RootType rootType;
	private String[] lineage;

	private int[] mainParentIds;
	private Taxon kindom;

	public static final Taxon root = new Taxon(0, 0, "root");
	public static final Taxon cellular_organisms = new Taxon(131567, 0, 0, "cellular organisms",
			RootType.cellular_organisms);
	public static final Taxon unclassified = new Taxon(12908, 0, 0, "unclassified sequences", RootType.unclassified);
	public static final Taxon other = new Taxon(28384, 0, 0, "other sequences", RootType.other);

	public Taxon(int id, int rankId, String name) {
		this.id = id;
		this.rankId = rankId;
		this.name = name;
	}

	public Taxon(int id, int parentId, int rankId, String name, RootType rootType) {
		this.id = id;
		this.parentId = parentId;
		this.rankId = rankId;
		this.name = name;
		this.rootType = rootType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getRankId() {
		return rankId;
	}

	public void setRankId(int rankId) {
		this.rankId = rankId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getLineage() {
		return lineage;
	}

	public void setLineage(String[] lineage) {
		this.lineage = lineage;
	}

	public RootType getRootType() {
		return rootType;
	}

	public void setRootType(RootType rootType) {
		this.rootType = rootType;
	}

	public Taxon getKindom() {
		return kindom;
	}

	public void setKindom(Taxon kindom) {
		this.kindom = kindom;
	}

	/**
	 * length==8
	 * @return
	 */
	public int[] getMainParentIds() {
		return mainParentIds;
	}

	public void setMainParentIds(int[] mainParentIds) {
		this.mainParentIds = mainParentIds;
	}

	public boolean isMainRank() {
		if (rankId == -1) {
			if (id == root.id) {
				return true;
			} else {
				return false;
			}
		}
		return TaxonomyRanks.getRankFromId(rankId).isMainRank();
	}

	public String getRank() {

		switch (id) {
		case 0: {
			return "root";
		}
		case 131567: {
			return "cellular organisms";
		}
		case 12908: {
			return "unclassified sequences";
		}
		case 28384: {
			return "other sequences";
		}
		default: {
			return TaxonomyRanks.getRankNameFromId(rankId);
		}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id).append(";");
		sb.append(parentId).append(";");
		sb.append(rankId).append(";");
		sb.append(name);
		return sb.toString();
	}
	
	public boolean equals(Object obj) {

		if (obj instanceof Taxon) {
			Taxon taxon = (Taxon) obj;
			if (taxon.getId() == this.getId() && taxon.getRankId() == this.getRankId()
					&& taxon.getName().equals(this.getName())) {
				return true;
			}
		}

		return false;
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
}
