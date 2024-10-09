package bmi.med.uOttawa.metalab.dbSearch.deepDetect;

import java.io.File;

public class DeepDetectParameter {

	private File input;
	private File output;
	private String protease = "Trypsin";
	private int missCleavage = 1;
	private int minLength = 7;
	private int maxLength = 30;
	
	private double threshold = 0.1;
	
	public DeepDetectParameter() {

	}
	
	public DeepDetectParameter(DeepDetectParameter par) {
		this.protease = par.getProtease();
		this.missCleavage = par.getMissCleavage();
		this.minLength = par.getMinLength();
		this.maxLength = par.getMaxLength();
	}

	public DeepDetectParameter(File input, File output, String protease, int missCleavage, int minLength,
			int maxLength) {
		this.input = input;
		this.output = output;
		this.protease = protease;
		this.missCleavage = missCleavage;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	public File getInput() {
		return input;
	}

	public void setInput(File input) {
		this.input = input;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public String getProtease() {
		return protease;
	}

	public void setProtease(String protease) {
		this.protease = protease;
	}

	public int getMissCleavage() {
		return missCleavage;
	}

	public void setMissCleavage(int missCleavage) {
		this.missCleavage = missCleavage;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	public double getThreshold() {
		return this.threshold;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--input=").append(input).append(" ");
		sb.append("--output=").append(output).append(" ");
		sb.append("--protease=").append(protease).append(" ");
		sb.append("--missed_cleavages=").append(missCleavage).append(" ");
		sb.append("--min_len=").append(minLength).append(" ");
		sb.append("--max_len=").append(maxLength);
		return sb.toString();
	}

}
