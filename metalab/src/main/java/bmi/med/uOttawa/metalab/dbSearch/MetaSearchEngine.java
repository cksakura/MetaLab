/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch;

/**
 * 
 * The database search engine used in MetaLab
 * 
 * @author Kai Cheng
 *
 */
public enum MetaSearchEngine {

	maxquant("MaxQuant", 0),

	xtandem("X!Tandem", 1),

	open("Open", 2),

	pFind("pFind", 3),

	FragPipe("FragPipe", 4),

	AlphaPept("AlphaPept", 5),

	Sage("Sage", 6);

	private final String name;
	private final int id;

	MetaSearchEngine(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}
