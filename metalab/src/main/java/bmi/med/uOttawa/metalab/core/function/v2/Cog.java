package bmi.med.uOttawa.metalab.core.function.v2;

public class Cog {

	private String id;
	private char category;
	private String name;

	public Cog(String id, char category, String name) {
		super();
		this.id = id;
		this.category = category;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public char getCategory() {
		return category;
	}

	public void setCategory(char category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
