package bmi.med.uOttawa.metalab.dbSearch.pfind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PFindResReader {
	
	public PFindResReader(String in) {
		
	}
	
	public PFindResReader(File in) throws IOException {
		int count = 0;
		BufferedReader reader = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=reader.readLine())!=null) {
			System.out.println(line);
			if(count++==20) {
				break;
			}
		}
		reader.close();
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		PFindResReader reader = new PFindResReader(new File("Z:\\Kai\\tmt\\TMT20201020\\MetaLab\\pfind_open_search_original_not_used"
				+ "\\pFindTask.F1.L1.qry.res"));
	}

}
