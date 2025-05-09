package bmi.med.uOttawa.metalab.core.prosit.irt;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Predict the retention time by Prosit
 * @see https://koina.wilhelmlab.org/
 * @author Kai Cheng
 * @since 2025
 */
public class PrositCurlRequest {

	private static final String requestUrl = "https://koina.wilhelmlab.org:443/v2/models/Prosit_2024_irt_cit/infer";
	private static final String jsonInputStart = """
			{"id": "0", "inputs": [{"name": "peptide_sequences","shape": [""";
	private static final String jsonInputMid = """
			, 1],"datatype": "BYTES","data": [""";
	private static final String jsonInputEnd = """
			]}]}""";

	public static String[] sendPostRequest(String[] peptides) {
		StringBuilder sb = new StringBuilder();
		sb.append(jsonInputStart).append(peptides.length).append(jsonInputMid);
		for (String pep : peptides) {
			sb.append("\"").append(pep).append("\",");
		}
		if (peptides.length > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(jsonInputEnd);

		try {
			URL url = new URL(requestUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; utf-8");
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = sb.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			System.out.println("Response Code: " + responseCode);

			if (responseCode == HttpsURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				int id1 = response.lastIndexOf("[");
				if (id1 > 0) {
					int id2 = response.indexOf("]", id1);
					if (id2 > id1) {
						String[] cs = response.subSequence(id1 + 1, id2).toString().split(",");
						if (cs.length == peptides.length) {
							return cs;
						}
					}
				}
			} else {
				System.out.println("Request failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static void batchPredict(String in) {
		File[] files = (new File(in)).listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if (name.endsWith(".predicted.tsv")) {
				File irtFile = new File(in, name.replace("tsv", "iRT.tsv"));
				if (!irtFile.exists()) {
					HashSet<String> set = new HashSet<String>();
					try (BufferedReader reader = new BufferedReader(new FileReader(files[i]))) {
						String line = reader.readLine();
						while ((line = reader.readLine()) != null) {
							String[] cs = line.split("\t");
							set.add(cs[1]);
						}
						reader.close();
					} catch (IOException e) {

					}

					System.out.println(name + "\t" + set.size());

					int chunkSize = 1000;
					List<ArrayList<String>> chunks = new ArrayList<>();
					ArrayList<String> currentChunk = new ArrayList<String>();
					int count = 0;

					for (String peptide : set) {
						currentChunk.add(peptide);
						count++;
						if (count == chunkSize) {
							chunks.add(new ArrayList<>(currentChunk));
							currentChunk.clear();
							count = 0;
						}
					}

					if (!currentChunk.isEmpty()) {
						chunks.add(currentChunk);
					}

					try (PrintWriter writer = new PrintWriter(irtFile)) {
						writer.println("Sequence\tiRT");

						for (ArrayList<String> chunk : chunks) {
							String[] peptides = chunk.toArray(String[]::new);
							String[] rts = sendPostRequest(peptides);
							if (rts != null) {
								for (int j = 0; j < peptides.length; j++) {
									writer.println(peptides[j] + "\t" + rts[j]);
								}
							} else {
								System.out.println(name + "\tNULL");
							}
						}
						writer.close();
					} catch (IOException e) {

					}
				}
			}
		}
	}
}

