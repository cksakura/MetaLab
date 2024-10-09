/**
 * 
 */
package bmi.med.uOttawa.metalab.dbSearch.philosopher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * @author Kai Cheng
 *
 */
public class PhilosopherPepReader {
	
	public PhilosopherPepReader(String in) {
		this(new File(in));
	}
	
	public PhilosopherPepReader(File in) {
		this.read(in);
	}
	
	private void read(File in) {
		HashSet<String> set = new HashSet<String>();
		BufferedReader reader = null;
		String line = null;
		try {
			int count = 0;
			reader = new BufferedReader(new FileReader(in));
			String[] title = reader.readLine().split("\t");
			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				set.add(cs[0]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(set.size());
	}
	
}
