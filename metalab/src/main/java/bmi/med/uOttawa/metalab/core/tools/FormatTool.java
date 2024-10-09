/**
 * 
 */
package bmi.med.uOttawa.metalab.core.tools;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Kai Cheng
 *
 */
public class FormatTool {

	public static String CR = System.getProperty("line.separator");

	public static DecimalFormat getDF2() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.##", dfs);
		return df;
	}

	public static DecimalFormat getDF3() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.###", dfs);
		return df;
	}

	public static DecimalFormat getDF4() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.####", dfs);
		return df;
	}

	public static DecimalFormat getDF6() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.######", dfs);
		return df;
	}

	public static DecimalFormat getDFE2() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.##E0", dfs);
		return df;
	}

	public static DecimalFormat getDFE4() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.####E0", dfs);
		return df;
	}

	public static DecimalFormat getDFP2() {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.##%", dfs);
		return df;
	}

	public static SimpleDateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		return sdf;
	}

}
