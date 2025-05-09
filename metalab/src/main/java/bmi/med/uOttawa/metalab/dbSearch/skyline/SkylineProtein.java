package bmi.med.uOttawa.metalab.dbSearch.skyline;

import java.util.ArrayList;
import java.util.List;

public class SkylineProtein {
	private String name;
    private String description;
    private List<SkylinePeptide> peptides;

    public SkylineProtein(String name, String description) {
        this.name = name;
        this.description = description;
        this.peptides = new ArrayList<>();
    }

    public void addPeptide(SkylinePeptide peptide) {
        this.peptides.add(peptide);
    }

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<SkylinePeptide> getPeptides() {
		return peptides;
	}
    
    
}
