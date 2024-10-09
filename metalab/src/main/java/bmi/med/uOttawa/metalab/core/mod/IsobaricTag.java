/**
 * 
 */
package bmi.med.uOttawa.metalab.core.mod;

/**
 * @author Kai Cheng
 *
 */
public enum IsobaricTag {

	itraq4("iTRAQ4plex", "iTRAQ (Representative mass and accurate mass for 116 & 117)", 144.102063, 4),

	itraq8("iTRAQ8plex", "iTRAQ8plex (Representative mass and accurate mass for 113, 114, 116 & 117)", 304.205360, 8),

	tmt2("TMT2plex", "Duplex Tandem Mass Tag", 225.155833, 2),

	tmt6("TMT6plex", "Sixplex Tandem Mass Tag", 229.162932, 6),

	tmt8("TMT8plex", "TMT8plex", 229.162932, 8),

	tmt10("TMT10plex", "TMT10plex", 229.162932, 10),

	tmt11("TMT11plex", "TMT11plex", 229.162932, 11),

	iodo6("iodoTMT6plex", "Sixplex iodoacetyl Tandem Mass Tag", 329.226595, 6),

	tmtpro16("TMTpro16plex", "TMTpro 16plex", 304.207146, 16),

	tmtpro18("TMTpro18plex", "TMTpro 18plex", 304.207146, 18);

	private String name;
	private String unimodTitle;
	private double mass;
	private int nPlex;

	IsobaricTag(String name, String unimodTitle, double mass, int nPlex) {
		this.name = name;
		this.unimodTitle = unimodTitle;
		this.mass = mass;
		this.nPlex = nPlex;
	}

	public String getName() {
		return name;
	}

	public String getUnimodTitle() {
		return unimodTitle;
	}

	public double getMass() {
		return mass;
	}

	public int getnPlex() {
		return nPlex;
	}

	public String[] getReportTitle() {
		switch (this) {
		case itraq4: {
			String[] ions = new String[4];
			String[] itaq = itraqIons();
			System.arraycopy(itaq, 1, ions, 0, ions.length);
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case itraq8: {
			String[] ions = itraqIons();
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmt2: {
			String[] ions = new String[2];
			String[] tmt = tmtIons();
			System.arraycopy(tmt, 1, ions, 0, ions.length);
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmt6: {
			String[] ions = tmtIons();
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmt8: {
			String[] tmt = tmtIonsNC();
			String[] ions = new String[] { tmt[0], tmt[1], tmt[2], tmt[4], tmt[5], tmt[6], tmt[8], tmt[9] };
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmt10: {
			String[] ions = new String[10];
			String[] tmt = tmtIonsNC();
			System.arraycopy(tmt, 0, ions, 0, ions.length);
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmt11: {
			String[] ions = tmtIonsNC();
			for (int i = 0; i < ions.length - 1; i++) {
				ions[i] = tmt10.getName() + ions[i];
			}
			ions[ions.length - 1] = this.getName() + ions[ions.length - 1];
			return ions;
		}
		case iodo6: {
			String[] ions = iodoTmtIons();
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmtpro16: {
			String[] ions = new String[16];
			String[] tmt = tmtProIonsNC();
			System.arraycopy(tmt, 0, ions, 0, ions.length);
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		case tmtpro18: {
			String[] ions = tmtProIonsNC();
			for (int i = 0; i < ions.length; i++) {
				ions[i] = this.getName() + ions[i];
			}
			return ions;
		}
		default: {
			return new String[] {};
		}
		}
	}

	public static IsobaricTag getTagFromReportTitle(String[] reportTitle) {
		L: for (IsobaricTag tag : IsobaricTag.values()) {
			String[] tagTitle = tag.getReportTitle();
			for (int i = 0; i < reportTitle.length; i++) {
				boolean find = false;
				for (int j = 0; j < tagTitle.length; j++) {
					if (reportTitle[i].equals(tagTitle[j])) {
						find = true;
						break;
					}
				}
				if (!find) {
					continue L;
				}
			}
			return tag;
		}
		return null;
	}

	private static String[] itraqIons() {
		String[] ions = new String[8];
		for (int i = 0; i < 7; i++) {
			ions[i] = String.valueOf(i + 113);
		}
		ions[7] = "121";
		return ions;
	}

	private static String[] tmtIons() {
		String[] ions = new String[6];
		for (int i = 0; i < 6; i++) {
			ions[i] = String.valueOf(i + 126);
		}
		return ions;
	}

	private static String[] tmtIonsNC() {
		String[] ions = new String[11];
		ions[0] = "126C";
		for (int i = 0; i < 5; i++) {
			ions[i * 2 + 1] = (i + 127) + "N";
			ions[i * 2 + 2] = (i + 127) + "C";
		}
		return ions;
	}

	private static String[] tmtProIonsNC() {
		String[] ions = new String[18];
		ions[0] = "126C";
		for (int i = 0; i < 8; i++) {
			ions[i * 2 + 1] = (i + 127) + "N";
			ions[i * 2 + 2] = (i + 127) + "C";
		}
		ions[ions.length - 1] = "135N";
		return ions;
	}

	private static String[] iodoTmtIons() {
		String[] ions = new String[6];
		for (int i = 0; i < 6; i++) {
			ions[i] = "Cys" + (i + 126);
		}
		return ions;
	}

	public static IsobaricTag[] getMaxQuantTags() {
		return new IsobaricTag[] { itraq4, itraq8, tmt2, tmt6, tmt8, tmt10, tmt11, iodo6 };
	}

	public static IsobaricTag[] getPFindTags() {
		return new IsobaricTag[] { itraq4, itraq8, tmt2, tmt6, tmt8, tmt10, tmt11, iodo6, tmtpro16, tmtpro18 };
	}
	
	public static IsobaricTag[] getFragpipeTags() {
		return new IsobaricTag[] { itraq4, itraq8, tmt6, tmt10, tmt11, tmtpro16, tmtpro18 };
	}
}
