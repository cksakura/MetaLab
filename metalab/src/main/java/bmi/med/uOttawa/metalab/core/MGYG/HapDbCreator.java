package bmi.med.uOttawa.metalab.core.MGYG;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;

import bmi.med.uOttawa.metalab.core.prodb.FastaManager;
import bmi.med.uOttawa.metalab.dbSearch.xtandem.TandemFastaGetter;

public class HapDbCreator {
	
	private static void create(String xmlin, String dbin, String out, double fdr, double evalue) {
		HashSet<String> totalSet = new HashSet<String>();
		File[] xmlFiles = (new File(xmlin)).listFiles();
		for (int i = 0; i < xmlFiles.length; i++) {
			if (xmlFiles[i].getName().endsWith("xml")) {
				TandemFastaGetter getter = new TandemFastaGetter(xmlFiles[i]);
				HashSet<String> refset = getter.parseProtein(fdr, evalue);

				for (String refString : refset) {
					String species = refString.split("\\s+")[0];
					species = species.substring(0, species.lastIndexOf("_"));
					totalSet.add(species);
				}
			}
		}
		
		System.out.println(totalSet.size());
		
		for(String spString : totalSet) {
			System.out.println(spString);
		}
	}
	
	private static void fileCopy(String in, String out) {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				File dbFile = new File(files[i], "MGYG" + files[i].getName() + ".faa");
				System.out.println(dbFile);
				if (dbFile.exists()) {
					try {
						FileUtils.copyFile(dbFile, new File(out, dbFile.getName()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		// TODO Auto-generated method stub
/*		
		HapDbCreator.create("D:\\Data\\MetaLab_test\\human\\cluster", 
				"D:\\Data\\new_db_4\\original_db", "", 0.01, 0.05);
		
		HapDbCreator.create("D:\\Data\\MetaLab_test\\human\\cluster\\only_target", 
				"D:\\Data\\new_db_4\\original_db", "", 0.01, 0.05);
*/		
//		HapDbCreator.create("D:\\Data\\MetaLab_test\\human\\spectra", 
//				"D:\\Data\\new_db_4\\original_db", "", 0.01, 0.05);
		
//		FastaManager.addDecoy("D:\\Data\\new_db_4\\rib_elon.fasta", 
//				"D:\\Data\\new_db_4\\final_rib_elon.fasta");
		
		HapDbCreator.fileCopy("Z:\\Kai\\20211220_newdb", 
				"Z:\\Kai\\ttt\\MGYG\\original_db");
	}

}
