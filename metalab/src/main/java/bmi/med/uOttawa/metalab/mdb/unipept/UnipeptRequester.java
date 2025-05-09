/**
 * 
 */
package bmi.med.uOttawa.metalab.mdb.unipept;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Kai Cheng
 *
 */
public class UnipeptRequester {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) ...";
	private static final String Language = "en-US,en;q=0.5";
	private static final String pept2prot = "http://api.unipept.ugent.be/api/v1/pept2prot";
	private static final String pept2lca = "http://api.unipept.ugent.be/api/v1/pept2lca";
	// private static final String pept2taxa =
	// "http://api.unipept.ugent.be/api/v1/pept2taxa";

	private static Logger LOGGER = LogManager.getLogger(UnipeptRequester.class);

	public static UnipProResult pept2prot(String sequence) {

		URL obj = null;
		try {
			obj = new URL(pept2prot);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to proteins by " + pept2prot, e);
			return null;
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to proteins by " + pept2prot, e);
			return null;
		}

		// add reuqest header
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to proteins by " + pept2prot, e);
		}

		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("connection", "Keep-Alive");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", Language);

		String urlParameters = "input[]=" + sequence;

		// Send post request
		con.setDoOutput(true);
		con.setDoInput(true);

		DataOutputStream wr;
		try {
			wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to proteins by " + pept2prot, e);
		}

		// int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + pept2prot);
		// System.out.println("Post parameters : " + urlParameters);
		// System.out.println("Response Code : " + responseCode);

		String line = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			line = in.readLine();
			in.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to proteins by " + pept2prot, e);
		}

		if (line == null || line.length() <= 2) {
			return null;
		}

		String[] cs = line.substring(2, line.length() - 2).split("\\},\\{");
		String[] uniprotIds = new String[cs.length];
		String[] pronames = new String[cs.length];
		int[] taxonIds = new int[cs.length];

		for (int i = 0; i < cs.length; i++) {
			String[] csi = cs[i].split("\",\"");
			uniprotIds[i] = csi[1].substring(csi[1].lastIndexOf('"') + 1);
			pronames[i] = csi[2].substring(csi[2].lastIndexOf('"') + 1);
			taxonIds[i] = Integer.parseInt(csi[3].substring(csi[3].lastIndexOf(':') + 1));
		}

		UnipProResult result = new UnipProResult(sequence, uniprotIds, pronames, taxonIds);
		return result;
	}

	public static HashMap<String, UnipProResult> pept2prot(String[] sequences) {

		URL obj = null;
		try {
			obj = new URL(pept2prot);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to proteins by " + pept2prot, e);
			return null;
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to proteins by " + pept2prot, e);
			return null;
		}

		// add reuqest header
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to proteins by " + pept2prot, e);
		}
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("connection", "Keep-Alive");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", Language);

		StringBuilder sb = new StringBuilder();
		for (String sequence : sequences) {
			sb.append("input[]=").append(sequence).append("&");
		}
		sb.deleteCharAt(sb.length() - 1);

		String urlParameters = sb.toString();

		// Send post request
		con.setDoOutput(true);
		con.setDoInput(true);

		try {
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to proteins by " + pept2prot, e);
		}

		HashMap<String, UnipProResult> map = new HashMap<String, UnipProResult>();

		String line = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			line = in.readLine();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to proteins by " + pept2prot, e);
		}

		if (line == null || line.length() < 4) {
			return map;
		}

		HashMap<String, HashSet<String>> pepProMap = new HashMap<String, HashSet<String>>();
		HashMap<String, String[]> proTaxonMap = new HashMap<String, String[]>();

		String[] cs = line.substring(2, line.length() - 2).split("\\},\\{");

		for (int i = 0; i < cs.length; i++) {
			System.out.println(cs[i]);
			String[] csi = cs[i].split("\",\"");

			if (csi.length == 4) {

				String sequence = csi[0].substring(csi[0].lastIndexOf('"') + 1);
				String proid = csi[1].substring(csi[1].lastIndexOf('"') + 1);
				String proname = csi[2].substring(csi[2].lastIndexOf('"') + 1);
				String taxid = csi[3].substring(csi[3].lastIndexOf(':') + 1);

				if (pepProMap.containsKey(sequence)) {
					pepProMap.get(sequence).add(proid);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(proid);
					pepProMap.put(sequence, set);
				}

				proTaxonMap.put(proid, new String[] { proname, taxid });
			} else {
				LOGGER.error("Error in parsing " + cs[i]);
			}
		}

		for (String sequence : pepProMap.keySet()) {
			HashSet<String> set = pepProMap.get(sequence);
			String[] proids = set.toArray(new String[set.size()]);
			String[] pronames = new String[proids.length];
			int[] taxids = new int[proids.length];

			for (int i = 0; i < proids.length; i++) {
				pronames[i] = proTaxonMap.get(proids[i])[0];
				taxids[i] = Integer.parseInt(proTaxonMap.get(proids[i])[1]);
			}

			UnipProResult result = new UnipProResult(sequence, proids, pronames, taxids);
			map.put(sequence, result);
		}

		return map;
	}

	public static UnipLCAResult pept2lca(String sequence) {

		URL obj = null;
		try {
			obj = new URL(pept2lca);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to LCA by " + pept2lca, e);
			return null;
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to LCA by " + pept2lca, e);
			return null;
		}

		// add reuqest header
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("connection", "Keep-Alive");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", Language);

		String urlParameters = "input[]=" + sequence;

		// Send post request
		con.setDoOutput(true);
		con.setDoInput(true);

		try {
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to LCA by " + pept2lca, e);
		}

		// int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + pept2prot);
		// System.out.println("Post parameters : " + urlParameters);
		// System.out.println("Response Code : " + responseCode);

		String line = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			line = in.readLine();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptide " + sequence + " to LCA by " + pept2lca, e);
		}

		if (line == null || line.length() == 0) {
			return null;
		}

		String[] cs = line.split("[\\W]+");
		if (cs.length != 9) {
			return null;
		}

		UnipLCAResult result = new UnipLCAResult(cs[2], Integer.parseInt(cs[4]), cs[6], cs[8]);
		return result;
	}

	public static HashMap<String, UnipLCAResult> pept2lca(String[] sequences) {

		URL obj = null;
		try {
			obj = new URL(pept2lca);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to LCA by " + pept2lca, e);
			return null;
		}
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) obj.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to LCA by " + pept2lca, e);
			return null;
		}

		// add reuqest header
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to LCA by " + pept2lca, e);
		}
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("connection", "Keep-Alive");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", Language);

		StringBuilder sb = new StringBuilder();
		for (String sequence : sequences) {
			sb.append("input[]=").append(sequence).append("&");
		}
		sb.deleteCharAt(sb.length() - 1);

		String urlParameters = sb.toString();

		// Send post request
		con.setDoOutput(true);
		con.setDoInput(true);

		try {
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to LCA by " + pept2lca, e);
		}

		HashMap<String, UnipLCAResult> map = new HashMap<String, UnipLCAResult>();

		String line = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			line = in.readLine();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in matching peptides to LCA by " + pept2lca, e);
		}

		if (line == null || line.length() == 0) {
			return map;
		}

		String[] cs = line.split("\\},\\{");
		for (int i = 0; i < cs.length; i++) {
			String[] csi = cs[i].split("[,:\"]+");
			if (csi.length == 9) {
				UnipLCAResult result = new UnipLCAResult(csi[2], Integer.parseInt(csi[4]), csi[6], csi[8]);
				map.put(result.getSequence(), result);
			} else if (csi.length == 10) {
				UnipLCAResult result = new UnipLCAResult(csi[2], Integer.parseInt(csi[4]), csi[6], csi[8]);
				map.put(result.getSequence(), result);
			}
		}

		return map;
	}

	@SuppressWarnings("unused")
	private static void test(String in) {
		try (BufferedReader reader = new BufferedReader(new FileReader(in))) {
			String line = reader.readLine();
			ArrayList<String> list = new ArrayList<String>();

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");
				list.add(cs[0]);
			}
			reader.close();

			String[] sequences = list.toArray(String[]::new);
			HashMap<String, UnipLCAResult> lcaResultMap = UnipeptRequester.pept2lca(sequences);
			System.out.println(sequences.length + "\t" + lcaResultMap.size());

		} catch (IOException e) {

		}
	}
}
