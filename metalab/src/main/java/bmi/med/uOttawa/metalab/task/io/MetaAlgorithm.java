/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io;

/**
 * Two peptide-to-taxon database are available in MetaLab.
 * 
 * @author Kai Cheng
 *
 */
public enum MetaAlgorithm {

	Builtin("BuiltIn", 0),

	Unipept("Unipept", 1);

	private final String name;
	private final int id;

	MetaAlgorithm(String name, int id) {
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
