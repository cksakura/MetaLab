package bmi.med.uOttawa.metalab.task.io.pro.pfind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import bmi.med.uOttawa.metalab.core.function.v2.EnzymeCommission;
import bmi.med.uOttawa.metalab.core.function.v2.GoObo;
import bmi.med.uOttawa.metalab.task.io.pro.MetaProtein;

public class MetaProteinAnnoEggNog {

	protected MetaProtein pro;
	protected String tax_id;
	protected String best_tax_level;
	protected String prefered_name;
	protected String Gene_Ontology;
	protected String EC;
	protected String KEGG_ko;
	protected String KEGG_Pathway;
	protected String KEGG_Module;
	protected String KEGG_Reaction;
	protected String KEGG_rclass;
	protected String BRITE;
	protected String KEGG_TC;
	protected String CAZy;
	protected String BiGG_Reaction;
	protected String cog;
	protected String nog;

	public static String[] funcNames = { "Gene_Ontology", "EC", "KEGG_ko", "KEGG_Pathway", "KEGG_Module",
			"KEGG_Reaction", "KEGG_rclass", "BRITE", "KEGG_TC", "CAZy", "BiGG_Reaction", "COG",
			"COG_Functional_Category", "COG_description", "NOG", "NOG_Functional_Category", "NOG_description" };

	public MetaProteinAnnoEggNog(MetaProtein pro) {
		this(pro, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
	}

	public MetaProteinAnnoEggNog(MetaProtein pro, String tax_id, String best_tax_level, String prefered_name,
			String gene_Ontology, String eC, String kEGG_ko, String kEGG_Pathway, String kEGG_Module,
			String kEGG_Reaction, String kEGG_rclass, String bRITE, String kEGG_TC, String cAZy, String biGG_Reaction,
			String cog, String nog) {
		this.pro = pro;
		this.tax_id = tax_id;
		this.best_tax_level = best_tax_level;
		this.prefered_name = prefered_name;
		this.Gene_Ontology = gene_Ontology;
		this.EC = eC;
		this.KEGG_ko = kEGG_ko;
		this.KEGG_Pathway = kEGG_Pathway;
		this.KEGG_Module = kEGG_Module;
		this.KEGG_Reaction = kEGG_Reaction;
		this.KEGG_rclass = kEGG_rclass;
		this.BRITE = bRITE;
		this.KEGG_TC = kEGG_TC;
		this.CAZy = cAZy;
		this.BiGG_Reaction = biGG_Reaction;
		this.cog = cog;
		this.nog = nog;
	}

	public String getTax_id() {
		return tax_id;
	}

	public String getBest_tax_level() {
		return best_tax_level;
	}

	public String getPrefered_name() {
		return prefered_name;
	}
	
	public String getGene_Ontology() {
		return Gene_Ontology;
	}

	public void setGene_Ontology(String gene_Ontology) {
		Gene_Ontology = gene_Ontology;
	}

	public String getEC() {
		return EC;
	}

	public void setEC(String eC) {
		EC = eC;
	}

	public String getKEGG_ko() {
		return KEGG_ko;
	}

	public void setKEGG_ko(String kEGG_ko) {
		KEGG_ko = kEGG_ko;
	}

	public String getKEGG_Pathway() {
		return KEGG_Pathway;
	}

	public void setKEGG_Pathway(String kEGG_Pathway) {
		KEGG_Pathway = kEGG_Pathway;
	}

	public String getKEGG_Module() {
		return KEGG_Module;
	}

	public void setKEGG_Module(String kEGG_Module) {
		KEGG_Module = kEGG_Module;
	}

	public String getKEGG_Reaction() {
		return KEGG_Reaction;
	}

	public void setKEGG_Reaction(String kEGG_Reaction) {
		KEGG_Reaction = kEGG_Reaction;
	}

	public String getKEGG_rclass() {
		return KEGG_rclass;
	}

	public void setKEGG_rclass(String kEGG_rclass) {
		KEGG_rclass = kEGG_rclass;
	}

	public String getBRITE() {
		return BRITE;
	}

	public void setBRITE(String bRITE) {
		BRITE = bRITE;
	}

	public String getKEGG_TC() {
		return KEGG_TC;
	}

	public void setKEGG_TC(String kEGG_TC) {
		KEGG_TC = kEGG_TC;
	}

	public String getCAZy() {
		return CAZy;
	}

	public void setCAZy(String cAZy) {
		CAZy = cAZy;
	}

	public String getBiGG_Reaction() {
		return BiGG_Reaction;
	}

	public void setBiGG_Reaction(String biGG_Reaction) {
		BiGG_Reaction = biGG_Reaction;
	}

	public String getCog() {
		return cog;
	}

	public void setCog(String cog) {
		this.cog = cog;
	}

	public String getNog() {
		return nog;
	}

	public void setNog(String nog) {
		this.nog = nog;
	}

	public MetaProtein getPro() {
		return pro;
	}

	public String[] getFuncArrays() {

		ArrayList<String> list = new ArrayList<String>();
		list.add(tax_id == null ? "" : tax_id);
		list.add(best_tax_level == null ? "" : best_tax_level);
		list.add(prefered_name == null ? "" : prefered_name);
		list.add(Gene_Ontology == null ? "" : Gene_Ontology);
		list.add(EC == null ? "" : EC);
		list.add(KEGG_ko == null ? "" : KEGG_ko);
		list.add(KEGG_Pathway == null ? "" : KEGG_Pathway);
		list.add(KEGG_Module == null ? "" : KEGG_Module);
		list.add(KEGG_Reaction == null ? "" : KEGG_Reaction);
		list.add(KEGG_rclass == null ? "" : KEGG_rclass);
		list.add(BRITE == null ? "" : BRITE);
		list.add(KEGG_TC == null ? "" : KEGG_TC);
		list.add(CAZy == null ? "" : CAZy);
		list.add(BiGG_Reaction == null ? "" : BiGG_Reaction);
		list.add(cog == null ? "" : cog);
		list.add(nog == null ? "" : nog);

		String[] funcs = list.toArray(new String[list.size()]);

		return funcs;
	}

	public String[] getFuncArrays(HashMap<String, String[]> usedCogMap, HashMap<String, String[]> usedNogMap,
			HashMap<String, String> usedKeggMap, HashMap<String, GoObo> usedGoMap,
			HashMap<String, EnzymeCommission> usedEcMap) {

		ArrayList<String> list = new ArrayList<String>();

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

		if (cog != null && cog.length() > 0) {

			list.add(cog);

			String[] cogStrings = cog.split(",");
			StringBuilder catesb = new StringBuilder();
			StringBuilder dessb = new StringBuilder();
			for (int i = 0; i < cogStrings.length; i++) {
				if (usedCogMap.containsKey(cogStrings[i])) {
					catesb.append(usedCogMap.get(cogStrings[i])[0]).append(";");
					dessb.append(usedCogMap.get(cogStrings[i])[1]).append(";");
				}
			}

			if (catesb.length() > 0) {
				list.add(catesb.substring(0, catesb.length() - 1));
			} else {
				list.add("");
			}

			if (dessb.length() > 0) {
				list.add(dessb.substring(0, dessb.length() - 1));
			} else {
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (nog != null && nog.length() > 0) {

			list.add(nog);

			if (usedNogMap.containsKey(nog + "@" + tax_id)) {
				list.add(usedNogMap.get(nog + "@" + tax_id)[0]);
				list.add(usedNogMap.get(nog + "@" + tax_id)[1]);
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

		if (cog != null && cog.length() > 0) {

			list.add(cog);

			String[] cogStrings = cog.split(",");
			StringBuilder catesb = new StringBuilder();
			StringBuilder dessb = new StringBuilder();
			for (int i = 0; i < cogStrings.length; i++) {
				if (usedCogMap.containsKey(cogStrings[i])) {
					catesb.append(usedCogMap.get(cogStrings[i])[0]).append(";");
					dessb.append(usedCogMap.get(cogStrings[i])[1]).append(";");
				}
			}

			if (catesb.length() > 0) {
				list.add(catesb.substring(0, catesb.length() - 1));
			} else {
				list.add("");
			}

			if (dessb.length() > 0) {
				list.add(dessb.substring(0, dessb.length() - 1));
			} else {
				list.add("");
			}
		} else {
			list.add("");
			list.add("");
			list.add("");
		}

		if (nog != null && nog.length() > 0) {

			list.add(nog);

			if (usedNogMap.containsKey(nog + "@" + tax_id)) {
				list.add(usedNogMap.get(nog + "@" + tax_id)[0]);
				list.add(usedNogMap.get(nog + "@" + tax_id)[1]);
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
	
}
