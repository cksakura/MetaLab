package bmi.med.uOttawa.metalab.task.hgm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.taxonomy.TaxonomyRanks;

public class HMGenomeHtmlWriter {

	private String link = "https://www.ebi.ac.uk/metagenomics/genomes/";

	private static final String head = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
			+ "<title>Genomes</title>\n<style>\nth {background-color: rgb(118, 214, 240);}"
			+ "td {padding: 20px;}\na{display: block;}\n"
			+ "ul li{list-style-type: circle;margin: 0;padding-bottom: 10px;}\n"
			+ "table {border-spacing: 5px;border-collapse: separate; border: 1px solid teal;}\n"
			+ "</style>\n</head>\n<body>\n";

	private PrintWriter writer;

	public HMGenomeHtmlWriter(String output) throws IOException {
		this(new File(output));
	}

	public HMGenomeHtmlWriter(File output) throws IOException {
		this.writer = new PrintWriter(output);
		this.writer.write(head);
	}

	public void write(HashMap<String, String[]> genomeMap) {

		this.writer.println("<table border=\"1\" style=\"width:100%\">");
		this.writer.println("<tr>");

		ArrayList<String> list = new ArrayList<String>();
		list.add("Genome");
		list.add("Taxonomy");
		list.add("Rank");

		for (String title : list) {
			this.writer.println("<th>" + title + "</th>");
		}
		for (TaxonomyRanks rank : TaxonomyRanks.getMainRanks()) {
			this.writer.println("<th>" + rank + "</th>");
		}
		this.writer.println("</tr>");

		String[] genomes = genomeMap.keySet().toArray(new String[genomeMap.size()]);
		Arrays.sort(genomes);

		for (String genome : genomes) {
			String[] taxa = genomeMap.get(genome);
			this.writer.println("<tr>");
			this.writer.println("<td><a href=\"" + link + genome + "\">" + genome + "</a></td>");
			for (int i = 1; i < TaxonomyRanks.getMainRanks().length + 3; i++) {
				this.writer.println("<td>" + taxa[i] + "</td>");
			}
			this.writer.println("</tr>");
		}
	}

	public void close() {
		this.writer.write("\n</table>\n</body>\n</html>");
		this.writer.close();
	}
}
