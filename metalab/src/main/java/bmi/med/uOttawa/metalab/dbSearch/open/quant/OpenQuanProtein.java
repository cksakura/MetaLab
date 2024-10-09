/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.open.quant;

/**
 * @author Kai Cheng
 *
 */
public class OpenQuanProtein {
	
	private String name;
	private double[] intensity;
	
	public OpenQuanProtein(String name, double[] intensity) {
		this.name = name;
		this.intensity = intensity;
	}

	public String getName() {
		return name;
	}

	public double[] getIntensity() {
		return intensity;
	}
	
	

}
