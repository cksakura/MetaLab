/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.gzip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @author Kai Cheng
 *
 */
public class Txt2GzipCompressor {

	public static void compress(String in, String out) throws IOException {

		byte[] buffer = new byte[1024];

		GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(out));

		FileInputStream instream = new FileInputStream(in);

		int line;
		while ((line = instream.read(buffer)) > 0) {
			gzos.write(buffer, 0, line);
		}

		instream.close();

		gzos.finish();
		gzos.close();

	}

}
