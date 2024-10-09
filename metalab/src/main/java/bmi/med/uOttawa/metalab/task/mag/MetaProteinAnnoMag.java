package bmi.med.uOttawa.metalab.task.mag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;

public class MetaProteinAnnoMag extends MetaProteinAnnoEggNog {

	private String pro_name;
	private String seed;
	private String description;
	private String pfam;

	public static String[] funcNames = { "query_name", "pro_name", "seed_ortholog", "COG",
			"eggNOG_OGs", "max_annot_lvl", "COG_category", "Description", "Preferred_name", "GOs", "EC", "KEGG_ko",
			"KEGG_Pathway", "KEGG_Module", "KEGG_Reaction", "KEGG_rclass", "BRITE", "KEGG_TC", "CAZy", "BiGG_Reaction",
			"PFAMs" };

	public static int seedID = 1;
	public static int nogID = 4;
	public static int taxonID = 5;
	public static int cogCatID = 6;
	public static int desID = 7;
	public static int prefnameID = 8;
	public static int goID = 9;
	public static int ecID = 10;
	public static int koID = 11;
	public static int pathwayID = 12;
	public static int moduleID = 13;
	public static int reactionID = 14;
	public static int rclassID = 15;
	public static int briteID = 16;
	public static int tcID = 17;
	public static int cazyID = 18;
	public static int biggID = 19;
	public static int pfamID = 20;

	public MetaProteinAnnoMag(MetaProtein pro) {
		super(pro);
		// TODO Auto-generated constructor stub
	}

	public MetaProteinAnnoMag(MetaProtein pro, String[] content, String pro_name, String cog, String nog) {
		super(pro, content[taxonID].split("\\|")[0], content[taxonID].split("\\|")[1], content[prefnameID],
				content[goID], content[ecID], content[koID], content[pathwayID], content[moduleID], content[reactionID],
				content[rclassID], content[briteID], content[tcID], content[cazyID], content[biggID], cog, nog);
		this.pro_name = pro_name;
		this.seed = content[seedID];
		this.description = content[desID];
		this.pfam = content[pfamID];
	}

	public MetaProteinAnnoMag(MetaProtein pro, String pro_name, String seed, String cog, String nog, String tax_id,
			String best_tax_level, String descrption, String prefered_name, String gene_Ontology, String eC,
			String kEGG_ko, String kEGG_Pathway, String kEGG_Module, String kEGG_Reaction, String kEGG_rclass,
			String bRITE, String kEGG_TC, String cAZy, String biGG_Reaction, String pfam) {

		super(pro, tax_id, best_tax_level, prefered_name, gene_Ontology, eC, kEGG_ko, kEGG_Pathway, kEGG_Module,
				kEGG_Reaction, kEGG_rclass, bRITE, kEGG_TC, cAZy, biGG_Reaction, cog, nog);
		this.pro_name = pro_name;
		this.seed = seed;
		this.description = descrption;
		this.pfam = pfam;
	}

	public String[] getFuncArrays(HashMap<String, String[]> usedCogMap, HashMap<String, String[]> usedNogMap,
			HashMap<String, String> usedKeggMap, HashMap<String, GoObo> usedGoMap,
			HashMap<String, EnzymeCommission> usedEcMap) {

		ArrayList<String> list = new ArrayList<String>();

		list.add(pro_name);
		list.add(seed);
		list.add(description);

		list.add(tax_id == null ? "" : tax_id);
		list.add(best_tax_level == null ? "" : best_tax_level);
		list.add(prefered_name == null ? "" : prefered_name);

		if (Gene_Ontology != null && Gene_Ontology.length() > 0) {

			list.add(Gene_Ontology);

			String[] goStrings = Gene_Ontology.split(",");
			StringBuilder namesb = new StringBuilder();
			StringBuilder namespacesb = new StringBuilder();
			for (int i = 0; i < goStrings.length; i++) {
				if (usedGoMap.containsKey(goStrings[i])) {
					namesb.append(usedGoMap.get(goStrings[i]).getName()).append(";");
					namespacesb.append(usedGoMap.get(goStrings[i]).getNamespace()).append(";");
				}
			}

			if (namesb.length() > 0) {
				list.add(namesb.substring(0, namesb.length() - 1));
			} else {
				list.add("");
			}

			if (namespacesb.length() > 0) {
				list.add(namespacesb.substring(0, namespacesb.length() - 1));
			} else {
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (EC != null && EC.length() > 0) {

			list.add(EC);

			String[] ecStrings = EC.split(",");
			StringBuilder desb = new StringBuilder();
			HashSet<String> caset = new HashSet<String>();
			HashSet<String> anset = new HashSet<String>();

			for (int i = 0; i < ecStrings.length; i++) {
				if (usedEcMap.containsKey(ecStrings[i])) {
					EnzymeCommission ec = usedEcMap.get(ecStrings[i]);
					desb.append(ec.getDe()).append(";");

					caset.add(ec.getCa());
					anset.addAll(ec.getAn());
				}
			}

			StringBuilder casb = new StringBuilder();
			StringBuilder ansb = new StringBuilder();

			for (String ca : caset) {
				casb.append(ca).append(";");
			}
			for (String an : anset) {
				ansb.append(an).append(";");
			}

			if (desb.length() > 0) {
				list.add(desb.substring(0, desb.length() - 1));
			} else {
				list.add("");
			}
			if (casb.length() > 0) {
				list.add(casb.substring(0, casb.length() - 1));
			} else {
				list.add("");
			}
			if (ansb.length() > 0) {
				list.add(ansb.substring(0, ansb.length() - 1));
			} else {
				list.add("");
			}

		} else {
			list.add("");
			list.add("");
			list.add("");
			list.add("");
		}

		list.add(KEGG_ko == null ? "" : KEGG_ko);

		if (KEGG_Pathway != null && KEGG_Pathway.length() > 0) {

			list.add(KEGG_Pathway);

			String[] keggStrings = KEGG_Pathway.split(",");
			StringBuilder dessb = new StringBuilder();
			for (int i = 0; i < keggStrings.length; i++) {
				if (usedKeggMap.containsKey(keggStrings[i])) {
					dessb.append(usedKeggMap.get(keggStrings[i])).append(";");
				}
			}

			if (dessb.length() > 0) {
				list.add(dessb.substring(0, dessb.length() - 1));
			} else {
				list.add("");
			}

		} else {
			list.add("");
			list.add("");
		}

		list.add(KEGG_Module == null ? "" : KEGG_Module);
		list.add(KEGG_Reaction == null ? "" : KEGG_Reaction);
		list.add(KEGG_rclass == null ? "" : KEGG_rclass);
		list.add(BRITE == null ? "" : BRITE);
		list.add(KEGG_TC == null ? "" : KEGG_TC);
		list.add(CAZy == null ? "" : CAZy);
		list.add(BiGG_Reaction == null ? "" : BiGG_Reaction);
		list.add(pfam == null ? "" : pfam);

		if (cog != null && cog.length() > 0) {

			list.add(cog);

			if (usedCogMap.containsKey(cog)) {
				list.add(usedCogMap.get(cog)[0]);
				list.add(usedCogMap.get(cog)[1]);
			} else {
				list.add("");
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (nog != null && nog.length() > 0) {
			String[] nogStrings = nog.split(",");

			for (int i = 0; i < nogStrings.length; i++) {
				list.add(nogStrings[i]);
				String nogName = nogStrings[i].substring(0, nogStrings[i].indexOf("|"));
				if (usedNogMap.containsKey(nogName)) {
					list.add(usedNogMap.get(nogName)[0]);
					list.add(usedNogMap.get(nogName)[1]);
				} else {
					list.add("");
					list.add("");
				}
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		return list.toArray(new String[list.size()]);
	}

	public String[] getFuncReportArrays(HashMap<String, String[]> usedCogMap, HashMap<String, String[]> usedNogMap,
			HashMap<String, String> usedKeggMap, HashMap<String, GoObo> usedGoMap,
			HashMap<String, EnzymeCommission> usedEcMap) {

		ArrayList<String> list = new ArrayList<String>();

		if (Gene_Ontology != null && Gene_Ontology.length() > 0) {

			list.add(Gene_Ontology);

			String[] goStrings = Gene_Ontology.split(",");
			StringBuilder namesb = new StringBuilder();
			StringBuilder namespacesb = new StringBuilder();
			for (int i = 0; i < goStrings.length; i++) {
				if (usedGoMap.containsKey(goStrings[i])) {
					namesb.append(usedGoMap.get(goStrings[i]).getName()).append(";");
					namespacesb.append(usedGoMap.get(goStrings[i]).getNamespace()).append(";");
				}
			}

			if (namesb.length() > 0) {
				list.add(namesb.substring(0, namesb.length() - 1));
			} else {
				list.add("");
			}

			if (namespacesb.length() > 0) {
				list.add(namespacesb.substring(0, namespacesb.length() - 1));
			} else {
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (EC != null && EC.length() > 0) {

			list.add(EC);

			String[] ecStrings = EC.split(",");
			StringBuilder desb = new StringBuilder();
			HashSet<String> caset = new HashSet<String>();
			HashSet<String> anset = new HashSet<String>();

			for (int i = 0; i < ecStrings.length; i++) {
				if (usedEcMap.containsKey(ecStrings[i])) {
					EnzymeCommission ec = usedEcMap.get(ecStrings[i]);
					desb.append(ec.getDe()).append(";");

					caset.add(ec.getCa());
					anset.addAll(ec.getAn());
				}
			}

			StringBuilder casb = new StringBuilder();
			StringBuilder ansb = new StringBuilder();

			for (String ca : caset) {
				casb.append(ca).append(";");
			}
			for (String an : anset) {
				ansb.append(an).append(";");
			}

			if (desb.length() > 0) {
				list.add(desb.substring(0, desb.length() - 1));
			} else {
				list.add("");
			}
			if (casb.length() > 0) {
				list.add(casb.substring(0, casb.length() - 1));
			} else {
				list.add("");
			}
			if (ansb.length() > 0) {
				list.add(ansb.substring(0, ansb.length() - 1));
			} else {
				list.add("");
			}

		} else {
			list.add("");
			list.add("");
			list.add("");
			list.add("");
		}

		list.add(KEGG_ko == null ? "" : KEGG_ko);

		if (KEGG_Pathway != null && KEGG_Pathway.length() > 0) {

			list.add(KEGG_Pathway);

			String[] keggStrings = KEGG_Pathway.split(",");
			StringBuilder dessb = new StringBuilder();
			for (int i = 0; i < keggStrings.length; i++) {
				if (usedKeggMap.containsKey(keggStrings[i])) {
					dessb.append(usedKeggMap.get(keggStrings[i])).append(";");
				}
			}

			if (dessb.length() > 0) {
				list.add(dessb.substring(0, dessb.length() - 1));
			} else {
				list.add("");
			}

		} else {
			list.add("");
			list.add("");
		}

		list.add(KEGG_Module == null ? "" : KEGG_Module);
		list.add(KEGG_Reaction == null ? "" : KEGG_Reaction);
		list.add(KEGG_rclass == null ? "" : KEGG_rclass);
		list.add(BRITE == null ? "" : BRITE);
		list.add(KEGG_TC == null ? "" : KEGG_TC);
		list.add(CAZy == null ? "" : CAZy);
		list.add(BiGG_Reaction == null ? "" : BiGG_Reaction);
		list.add(pfam == null ? "" : pfam);

		if (cog != null && cog.length() > 0) {

			list.add(cog);

			if (usedCogMap.containsKey(cog)) {
				list.add(usedCogMap.get(cog)[0]);
				list.add(usedCogMap.get(cog)[1]);
			} else {
				list.add("");
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (nog != null && nog.length() > 0) {
			String[] nogStrings = nog.split(",");
			String repNOG = nogStrings[0];

			list.add(repNOG);
			String nogName = repNOG.substring(0, repNOG.indexOf("|"));
			if (usedNogMap.containsKey(nogName)) {
				list.add(usedNogMap.get(nogName)[0]);
				list.add(usedNogMap.get(nogName)[1]);
			} else {
				list.add("");
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		return list.toArray(new String[list.size()]);
	}

	public String getPro_name() {
		return pro_name;
	}

	public String getSeed() {
		return seed;
	}

	public String getDescription() {
		return description;
	}

	public String getPfam() {
		return pfam;
	}

}
