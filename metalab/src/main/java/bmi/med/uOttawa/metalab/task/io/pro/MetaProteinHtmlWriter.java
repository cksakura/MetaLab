/**
 * 
 */
package bmi.med.uOttawa.metalab.task.io.pro;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import bmi.med.uOttawa.metalab.core.function.COGFinder;
import bmi.med.uOttawa.metalab.core.function.GOBPFinder;
import bmi.med.uOttawa.metalab.core.function.GOCCFinder;
import bmi.med.uOttawa.metalab.core.function.GOMFFinder;
import bmi.med.uOttawa.metalab.core.function.KEGGFinder;
import bmi.med.uOttawa.metalab.task.io.pro.pfind.MetaProteinAnnoEggNog;

/**
 * @author Kai Cheng
 *
 */
public class MetaProteinHtmlWriter {

	private String Gene_Ontology_link = "https://www.ebi.ac.uk/QuickGO/term/";
	private String EC_link = "https://www.genome.jp/dbget-bin/www_bget?ec:";
	private String KEGG_link = "https://www.genome.jp/dbget-bin/www_bget?";
	private String BRITE_link = "https://www.genome.jp/kegg/annotation/";
	private String KEGG_TC_link = "http://www.tcdb.org/search/result.php?tc=";
	private String CAZy_link = "http://www.cazy.org/";
	private String BiGG_Reaction_link = "http://bigg.ucsd.edu/models/";

	private static final String head = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
			+ "<title>functional analysis</title>\n<style>\nth {background-color: rgb(118, 214, 240);}"
			+ "td {padding: 20px;}\na{display: block;}\n"
			+ "ul li{list-style-type: circle;margin: 0;padding-bottom: 10px;}\n"
			+ "table {border-spacing: 5px;border-collapse: separate; border: 1px solid teal;}\n"
			+ "</style>\n</head>\n<body>\n";

	private PrintWriter writer;

	public MetaProteinHtmlWriter(String output) throws IOException {
		this(new File(output));
	}

	public MetaProteinHtmlWriter(File output) throws IOException {
		this.writer = new PrintWriter(output);
		this.writer.write(head);
	}

	public void write(MetaProteinAnnoEggNog[] proteins, HashMap<String, String[]> cogMap,
			HashMap<String, String[]> nogMap) {
		this.writer.println("<table border=\"1\" style=\"width:100%\">");
		this.writer.println("<tr>");

		ArrayList<String> list = new ArrayList<String>();
		list.add("Group ID");
		list.add("Protein names");

		for (int i = 0; i < MetaProteinAnnoEggNog.funcNames.length; i++) {
			list.add(MetaProteinAnnoEggNog.funcNames[i]);
		}

		for (String title : list) {
			this.writer.println("<th>" + title + "</th>");
		}
		this.writer.println("</tr>");

		MetaProteinAnnoEggNog delegrate = null;
		ArrayList<String> proNameList = new ArrayList<String>();

		for (int i = 0; i < proteins.length; i++) {

			MetaProtein pro = proteins[i].getPro();

			if (delegrate == null) {
				delegrate = proteins[i];
				proNameList.add(pro.getName());
			} else {

				if (delegrate.getPro().getGroupId() != -1
						&& delegrate.getPro().getGroupId() == proteins[i].getPro().getGroupId()) {

					proNameList.add(proteins[i].getPro().getName());

				} else {

					this.writer.println("<tr>");
					this.writer.println("<td>" + (delegrate.getPro().getGroupId()) + "</td>");

					StringBuilder namesb = new StringBuilder("<td>");
					for (String name : proNameList) {
						namesb.append(name).append(";").append("<br>");
					}

					namesb.append("</td>");
					this.writer.println(namesb);

					String Gene_Ontology = delegrate.getGene_Ontology();
					if (Gene_Ontology == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] gos = Gene_Ontology.split(",");
						if (gos != null && gos.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String go : gos) {
								sb.append("<li><a href=\"").append(this.Gene_Ontology_link + go).append("\">")
										.append(go).append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String EC = delegrate.getEC();
					if (EC == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] ecs = EC.split(",");
						if (ecs != null && ecs.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : ecs) {
								sb.append("<li><a href=\"").append(this.EC_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_ko = delegrate.getKEGG_ko();
					if (KEGG_ko == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] kos = KEGG_ko.split(",");
						if (kos != null && kos.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : kos) {
								sb.append("<li><a href=\"").append(this.KEGG_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_Pathway = delegrate.getKEGG_Pathway();
					if (KEGG_Pathway == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] pathways = KEGG_Pathway.split(",");
						if (pathways != null && pathways.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : pathways) {
								sb.append("<li><a href=\"").append(this.KEGG_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_Module = delegrate.getKEGG_Module();
					if (KEGG_Module == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] modules = KEGG_Module.split(",");
						if (modules != null && modules.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : modules) {
								sb.append("<li><a href=\"").append(this.KEGG_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_Reaction = delegrate.getKEGG_Reaction();
					if (KEGG_Reaction == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] reactions = KEGG_Reaction.split(",");
						if (reactions != null && reactions.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : reactions) {
								String webname = name.indexOf(".") > 0 ? name.substring(0, name.indexOf(".")) : name;
								sb.append("<li><a href=\"").append(this.KEGG_link + webname).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_rclass = delegrate.getKEGG_rclass();
					if (KEGG_rclass == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] rclass = KEGG_rclass.split(",");
						if (rclass != null && rclass.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : rclass) {
								sb.append("<li><a href=\"").append(this.KEGG_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String BRITE = delegrate.getBRITE();
					if (BRITE == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] brites = BRITE.split(",");
						if (brites != null && brites.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : brites) {
								if (name.startsWith("br")) {
									sb.append("<li><a href=\"").append(this.BRITE_link + name + ".html").append("\">")
											.append(name).append("</a></li>");
								} else {
									sb.append("<li>").append(name).append("</li>");
								}

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String KEGG_TC = delegrate.getKEGG_TC();
					if (KEGG_TC == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] tcs = KEGG_TC.split(",");
						if (tcs != null && tcs.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : tcs) {
								sb.append("<li><a href=\"").append(this.KEGG_TC_link + name).append("\">").append(name)
										.append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String CAZy = delegrate.getCAZy();
					if (CAZy == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] cazys = CAZy.split(",");
						if (cazys != null && cazys.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : cazys) {
								sb.append("<li><a href=\"").append(this.CAZy_link + name + ".html").append("\">")
										.append(name).append("</a></li>");

							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String BiGG_Reaction = delegrate.getBiGG_Reaction();
					if (BiGG_Reaction == null) {
						this.writer.println("<td>-</td>");
					} else {
						String[] biggs = BiGG_Reaction.split(",");
						if (biggs != null && biggs.length > 0) {
							StringBuilder sb = new StringBuilder("<td><ul>");
							for (String name : biggs) {
								if (name.indexOf(".") > 0) {
									sb.append("<li><a href=\"")
											.append(this.BiGG_Reaction_link + name.substring(0, name.indexOf(".")))
											.append("\">").append(name).append("</a></li>");
								} else {
									sb.append("<li><a href=\"").append(this.BiGG_Reaction_link + name).append("\">")
											.append(name).append("</a></li>");
								}
							}
							sb.append("</ul></td>");
							this.writer.println(sb);

						} else {
							this.writer.println("<td>-</td>");
						}
					}

					String cog = delegrate.getCog();
					if (cog == null) {
						this.writer.println("<td>-</td>");
						this.writer.println("<td>-</td>");
						this.writer.println("<td>-</td>");
					} else {
						this.writer.println("<td><a href=\"" + COGFinder.getLinks(cog) + "\">" + cog + "</a></td>");
						if (cogMap.containsKey(cog)) {
							this.writer.println("<td>" + cogMap.get(cog)[0] + "</td>");
							this.writer.println("<td>" + cogMap.get(cog)[1] + "</td>");
						} else {
							this.writer.println("<td>-</td>");
							this.writer.println("<td>-</td>");
						}
					}

					String nog = delegrate.getNog();
					if (nog == null) {
						this.writer.println("<td>-</td>");
						this.writer.println("<td>-</td>");
						this.writer.println("<td>-</td>");
					} else {
						this.writer.println("<td>" + nog + "</td>");
						if (nogMap.containsKey(nog)) {
							this.writer.println("<td>" + nogMap.get(nog)[0] + "</td>");
							this.writer.println("<td>" + nogMap.get(nog)[1] + "</td>");
						} else {
							this.writer.println("<td>-</td>");
							this.writer.println("<td>-</td>");
						}
					}

					this.writer.println("</tr>");

					delegrate = proteins[i];
					proNameList = new ArrayList<String>();
					proNameList.add(proteins[i].getPro().getName());
				}
			}
		}
	}

	public void write(MetaProteinAnno1[] proteins, HashMap<String, HashMap<String, String>> functionMap) {

		this.writer.println("<table border=\"1\" style=\"width:100%\">");
		this.writer.println("<tr>");

		ArrayList<String> list = new ArrayList<String>();
		list.add("Group ID");
		list.add("Protein names");

		HashMap<String, String> cogMap = functionMap.containsKey("COG") ? functionMap.get("COG") : null;
		HashMap<String, String> cogCateMap = functionMap.containsKey("COG category") ? functionMap.get("COG category")
				: null;

		if (cogMap != null && cogCateMap != null) {
			list.add("COG accession");
			list.add("COG name");
			list.add("COG category");
		}

		HashMap<String, String> nogMap = functionMap.containsKey("NOG") ? functionMap.get("NOG") : null;
		HashMap<String, String> nogCateMap = functionMap.containsKey("NOG category") ? functionMap.get("NOG category")
				: null;

		if (nogMap != null && nogCateMap != null) {
			list.add("NOG accession");
			list.add("NOG name");
			list.add("NOG category");
		}

		HashMap<String, String> keggMap = functionMap.containsKey("KEGG") ? functionMap.get("KEGG") : null;

		if (keggMap != null) {
			list.add("KEGG accession");
			list.add("KEGG name");
		}

		HashMap<String, String> koMap = functionMap.containsKey("KO") ? functionMap.get("KO") : null;
		if (koMap != null) {
			list.add("KO accession");
			list.add("KO name");
		}

		HashMap<String, String> goMap = functionMap.containsKey("GO") ? functionMap.get("GO") : null;
		if (goMap != null) {
			list.add("GOBP accession");
			list.add("GOBP name");

			list.add("GOCC accession");
			list.add("GOCC name");

			list.add("GOMF accession");
			list.add("GOMF name");
		}

		for (String title : list) {
			this.writer.println("<th>" + title + "</th>");
		}
		this.writer.println("</tr>");

		MetaProteinAnno1 delegrate = null;
		ArrayList<String> proNameList = new ArrayList<String>();

		for (MetaProteinAnno1 mp : proteins) {

			if (delegrate == null) {
				delegrate = mp;
				proNameList.add(mp.getPro().getName());
			} else {
				if (delegrate.getPro().getGroupId() == mp.getPro().getGroupId()) {
					proNameList.add(mp.getPro().getName());

				} else {

					this.writer.println("<tr>");

					this.writer.println("<td>" + (delegrate.getPro().getGroupId()) + "</td>");

					StringBuilder namesb = new StringBuilder("<td>");
					for (String name : proNameList) {
						namesb.append(name).append(";").append("<br>");
					}

					namesb.append("</td>");
					this.writer.println(namesb);

					if (cogMap != null && cogCateMap != null) {
						String cog = delegrate.getCOG();
						String cogName = cogMap.get(cog);
						String cogCate = cogCateMap.get(cog);

						if (cog != null) {
							this.writer.println("<td><a href=\"" + COGFinder.getLinks(cog) + "\">" + cog + "</a></td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (cogName != null) {
							this.writer.println("<td>" + cogName + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (cogCate != null) {
							this.writer.println("<td>" + cogCate + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}
					}

					if (nogMap != null && nogCateMap != null) {
						String nog = delegrate.getNOG();
						String nogName = nogMap.get(nog);
						String nogCate = nogCateMap.get(nog);

						if (nog != null) {
							this.writer.println("<td>" + nog + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (nogName != null) {
							this.writer.println("<td>" + nogName + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (nogCate != null) {
							this.writer.println("<td>" + nogCate + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}
					}

					if (keggMap != null) {
						String kegg = delegrate.getKEGG();
						String keggName = keggMap.get(kegg);

						if (kegg != null) {
							this.writer
									.println("<td><a href=\"" + KEGGFinder.getLinks(kegg) + "\">" + kegg + "</a></td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (keggName != null) {
							this.writer.println("<td>" + keggName + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}
					}

					if (koMap != null) {
						String ko = delegrate.getKO();
						String koName = koMap.get(ko);

						if (ko != null) {
							this.writer.println("<td><a href=\"" + KEGGFinder.getLinks(ko) + "\">" + ko + "</a></td>");
						} else {
							this.writer.println("<td>-</td>");
						}

						if (koName != null) {
							this.writer.println("<td>" + koName + "</td>");
						} else {
							this.writer.println("<td>-</td>");
						}
					}

					if (goMap != null) {

						String[] gobp = delegrate.getGOBP();
						String[] gocc = delegrate.getGOCC();
						String[] gomf = delegrate.getGOMF();

						if (gobp != null && gobp.length > 0) {
							StringBuilder sb1 = new StringBuilder("<td><ul>");
							StringBuilder sb2 = new StringBuilder("<td><ul>");
							for (String go : gobp) {
								sb1.append("<li><a href=\"").append(GOBPFinder.getQuickGo(go)).append("\">").append(go)
										.append("</a></li>");
								if (goMap.containsKey(go)) {
									sb2.append("<li><a href=\"").append(GOBPFinder.getAmiGO(go)).append("\">")
											.append(goMap.get(go)).append("</a></li>");
								}
							}
							sb1.append("</ul></td>");
							sb2.append("</ul></td>");

							this.writer.println(sb1);
							this.writer.println(sb2);

						} else {
							this.writer.println("<td>-</td>");
							this.writer.println("<td>-</td>");
						}

						if (gocc != null && gocc.length > 0) {
							StringBuilder sb1 = new StringBuilder("<td><ul>");
							StringBuilder sb2 = new StringBuilder("<td><ul>");
							for (String go : gocc) {
								sb1.append("<li><a href=\"").append(GOCCFinder.getQuickGo(go)).append("\">").append(go)
										.append("</a></li>");
								if (goMap.containsKey(go)) {
									sb2.append("<li><a href=\"").append(GOCCFinder.getAmiGO(go)).append("\">")
											.append(goMap.get(go)).append("</a></li>");
								}
							}
							sb1.append("</ul></td>");
							sb2.append("</ul></td>");

							this.writer.println(sb1);
							this.writer.println(sb2);

						} else {
							this.writer.println("<td>-</td>");
							this.writer.println("<td>-</td>");
						}

						if (gomf != null && gomf.length > 0) {
							StringBuilder sb1 = new StringBuilder("<td><ul>");
							StringBuilder sb2 = new StringBuilder("<td><ul>");
							for (String go : gomf) {
								sb1.append("<li><a href=\"").append(GOMFFinder.getQuickGo(go)).append("\">").append(go)
										.append("</a></li>");
								if (goMap.containsKey(go)) {
									sb2.append("<li><a href=\"").append(GOMFFinder.getAmiGO(go)).append("\">")
											.append(goMap.get(go)).append("</a></li>");
								}
							}
							sb1.append("</ul></td>");
							sb2.append("</ul></td>");

							this.writer.println(sb1);
							this.writer.println(sb2);

						} else {
							this.writer.println("<td>-</td>");
							this.writer.println("<td>-</td>");
						}
					}

					this.writer.println("</tr>");

					delegrate = mp;
					proNameList = new ArrayList<String>();
					proNameList.add(mp.getPro().getName());
				}
			}
		}
	}

	public void close() {
		this.writer.write("\n</table>\n</body>\n</html>");
		this.writer.close();
	}
}
