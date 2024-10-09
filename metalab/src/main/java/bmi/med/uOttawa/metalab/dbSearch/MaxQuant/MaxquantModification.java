/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.MaxQuant;

/**
 * @author Kai Cheng
 *
 */
public class MaxquantModification {

	private String title;
	private String description;
	private String position;
	private String composition;
	private String[] sites;
	private double monomass;
	private String tmtComp;
	private double tmtMass;

	public MaxquantModification(String title, String description, String position, String composition, String[] sites,
			double monomass) {
		this.title = title;
		this.description = description;
		this.position = position;
		this.composition = composition;
		this.sites = sites;
		this.monomass = monomass;
	}
	
	public MaxquantModification(String title, String description, String position, String composition, String[] sites,
			double monomass, double tmtMass, String tmtComp) {
		this.title = title;
		this.description = description;
		this.position = position;
		this.composition = composition;
		this.sites = sites;
		this.monomass = monomass;
		this.tmtMass = tmtMass;
		this.tmtComp = tmtComp;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getPosition() {
		return position;
	}

	public String getComposition() {
		return composition;
	}

	public String[] getSites() {
		return sites;
	}

	public double getMonomass() {
		return monomass;
	}

	public double getTmtMass() {
		return tmtMass;
	}

	public void setTmtMass(double tmtMass) {
		this.tmtMass = tmtMass;
	}

	public String getTmtComp() {
		return tmtComp;
	}

	public void setTmtComp(String tmtComp) {
		this.tmtComp = tmtComp;
	}

	public String toString() {
		return title;
	}

	public int hashCode() {
		return title.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof MaxquantModification) {
			MaxquantModification mm = (MaxquantModification) obj;
			return mm.title.equals(title);
		} else {
			return false;
		}
	}
	
	public String getTandemFormat() {
		StringBuilder sb = new StringBuilder();
		sb.append(monomass).append("@");
		for (int i = 0; i < sites.length; i++) {
			if (sites[i].equals("-")) {
				if (position.endsWith("Nterm")) {
					sb.append("[");
				} else if (position.endsWith("Cterm")) {
					sb.append("]");
				} else {

				}
			} else {
				sb.append(sites[i]);
			}
		}

		return sb.toString();
	}
}
