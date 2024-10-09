package bmi.med.uOttawa.metalab.core.function.v2;

public class GoObo {

	private String id;
	private String name;
	private String namespace;

	public GoObo(String id, String name, String namespace) {
		super();
		this.id = id;
		this.name = name;
		this.namespace = namespace;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
