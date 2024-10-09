package bmi.med.uOttawa.metalab.task.mag;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bmi.med.uOttawa.metalab.core.MGYG.HapExtractor;
import bmi.med.uOttawa.metalab.core.tools.ResourceLoader;
import bmi.med.uOttawa.metalab.core.tools.ResumableDownloadTask;
@Deprecated
public class MagDownloadTask extends SwingWorker<String, Void> {
	
	private static String taskName = "MAG database download task";
	private static final Logger LOGGER = LogManager.getLogger(MagDownloadTask.class);
	private SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private MagDatabase magDb;
	
	private long missLength = 146;
	private int waitHours = 4;
	private int thread;
	private File outputDirFile;
	private boolean finish = false;
	
	public MagDownloadTask(MagDatabase magDb, String output) {
		this.outputDirFile = new File(output);
		this.thread = Runtime.getRuntime().availableProcessors() / 2;
		this.magDb = magDb;
	}
	
	public MagDownloadTask(MagDatabase magDb, String output, int thread) {
		this.outputDirFile = new File(output);
		this.thread = Runtime.getRuntime().availableProcessors() / 2;
		this.magDb = magDb;
	}
	
	@Override
	protected String doInBackground() throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info(taskName + ": started");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": started");

		if (!outputDirFile.exists()) {
			outputDirFile.mkdirs();
		}

		File originalDbFile = new File(outputDirFile, "original_db");
		if (!originalDbFile.exists()) {
			originalDbFile.mkdirs();
		}

		File eggnogFile = new File(outputDirFile, "eggNOG");
		if (!eggnogFile.exists()) {
			eggnogFile.mkdirs();
		}

		ExecutorService executor = Executors.newFixedThreadPool(thread);

		ArrayList<Future<?>> flist = new ArrayList<Future<?>>();
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(ResourceLoader.load(magDb.idString)));
			if (magDb.hasHost) {
				line = reader.readLine();
				String host = magDb.hostNameString;
				File hostFile = new File(outputDirFile, host);

				if (!hostFile.exists() || hostFile.length() == 0) {
					ResumableDownloadTask task = new ResumableDownloadTask(line, hostFile.getAbsolutePath());
					Future<?> f = executor.submit(task);
					flist.add(f);
				}
			}

			// metadata
			line = reader.readLine();
			String meta = line.substring(line.lastIndexOf("/") + 1, line.length());
			File metaFile = new File(outputDirFile, meta);

			if (!metaFile.exists() || metaFile.length() == 0) {
				ResumableDownloadTask task = new ResumableDownloadTask(line, metaFile.getAbsolutePath());
				Future<?> f = executor.submit(task);
				flist.add(f);
			}

			while ((line = reader.readLine()) != null) {
				String[] cs = line.split("\t");

				String link = cs[0].substring(0, cs[0].lastIndexOf("/"));
				String faa = cs[0].substring(cs[0].lastIndexOf("/") + 1, cs[0].length());
				long fileLength = Long.parseLong(cs[1]);

				File faaFile = new File(originalDbFile, faa);

				if (!faaFile.exists() || faaFile.length() != fileLength) {
					ResumableDownloadTask task = new ResumableDownloadTask(cs[0], faaFile.getAbsolutePath());
					Future<?> f = executor.submit(task);
					flist.add(f);
				}

				String eggnog = faa.replace(".faa", "_eggNOG.tsv");
				File eggFile = new File(eggnogFile, eggnog);
				if (!eggFile.exists() || eggFile.length() == 0) {
					ResumableDownloadTask task = new ResumableDownloadTask(link + "/" + eggnog,
							eggFile.getAbsolutePath());
					Future<?> f = executor.submit(task);
					flist.add(f);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.info(taskName + ": unable to download the UHGG database");
			System.out.println(format.format(new Date()) + "\t" + taskName + ": unable to download the UHGG database");
		}

		for (int i = 0; i < flist.size(); i++) {
			Future<?> f = flist.get(i);
			f.get();
			setProgress((int) (i * 95.0 / (double) flist.size()));
		}

		try {

			executor.shutdown();

			boolean finish = executor.awaitTermination(waitHours, TimeUnit.HOURS);

			setProgress(98);

			String info = this.check();

			if (finish) {

				if (info.length() == 0) {
					LOGGER.info(taskName + ": extracting high abundant proteins...");
					System.out.println(
							format.format(new Date()) + "\t" + taskName + ": extracting  high abundant proteins...");

					File hapFile = new File(outputDirFile, "rib_elon.fasta");
					HapExtractor extractor = new HapExtractor(true);
					extractor.extract(originalDbFile, hapFile);

					LOGGER.info(taskName + ": finished");
					System.out.println(format.format(new Date()) + "\t" + taskName + ": finished");

					setProgress(100);

					this.finish = true;

					return "Task finished";

				} else {
					LOGGER.info(taskName + ": " + info + ", please try again");
					System.out
							.println(format.format(new Date()) + "\t" + taskName + ": " + info + ", please try again");
					return info;
				}
			} else {
				LOGGER.info(taskName + ": task dosen't finish in a long time, please try again");
				System.out.println(format.format(new Date()) + "\t" + taskName
						+ ": task dosen't finish in a long time, please try again");
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": failed", e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": failed");
		}

		return "Task failed";
	}
	
	private String check() {

		File originalDbFile = new File(outputDirFile, "original_db");
		if (originalDbFile.listFiles().length < magDb.genomeCount) {
			return "The database has not been downloaded completely, "
					+ (magDb.genomeCount - originalDbFile.listFiles().length)
					+ " genomes were missed, please try again.";
		}

		File eggnogFile = new File(outputDirFile, "eggNOG");
		if (eggnogFile.listFiles().length < magDb.genomeCount) {
			return "The database has not been downloaded completely, "
					+ (magDb.genomeCount - originalDbFile.listFiles().length)
					+ " genome annotations were missed, please try again.";
		}

		if (magDb.hasHost) {
			File hostFile = new File(outputDirFile, magDb.hostNameString);

			if (!hostFile.exists() || hostFile.length() == 0) {
				return "The database has not been downloaded completely, host database was missed, please try again.";
			}
		}
		
		return "";
	}
	
	protected void createDownloadLinks() throws IOException, URISyntaxException {

		DecimalFormat df1 = new DecimalFormat("0000000");
		DecimalFormat df2 = new DecimalFormat("000000000");

		PrintWriter writer = new PrintWriter(outputDirFile);
		writer.println(this.magDb.baseLinkString + "genomes-all_metadata.tsv");

		String species_catalogue = this.magDb.baseLinkString + "species_catalogue/";

		int count = 0;

		for (int i = this.magDb.startId; i <= this.magDb.endId; i++) {

			for (int j = 0; j <= 99; j++) {

				int idj = i * 100 + j;
				if (idj == 0) {
					continue;
				}

				StringBuilder sb = new StringBuilder(species_catalogue);

				sb.append("MGYG").append(df1.format(i)).append("/");

				String geneId = df2.format(idj);

				sb.append("MGYG").append(df2.format(idj));

				StringBuilder sb1 = new StringBuilder(sb);

				sb.append("/").append("genome/");
				sb1.append(".1/").append("genome/");

				sb.append("MGYG").append(geneId).append(".faa");
				sb1.append("MGYG").append(geneId).append(".1.faa");

				String lineString = sb.toString();
				String lineString1 = sb1.toString();

				URLConnection connection = new URI(lineString).toURL().openConnection();

				if (connection instanceof HttpURLConnection) {
					HttpURLConnection tmpFileConn = (HttpURLConnection) new URI(lineString).toURL().openConnection();
					tmpFileConn.setRequestMethod("HEAD");
					long fileLength = tmpFileConn.getContentLengthLong();
					if (fileLength > missLength) {
						writer.println(lineString + "\t" + fileLength);
						count++;

					} else {

						connection = new URI(lineString1).toURL().openConnection();

						if (connection instanceof HttpURLConnection) {
							tmpFileConn = (HttpURLConnection) new URI(lineString1).toURL().openConnection();
							tmpFileConn.setRequestMethod("HEAD");
							fileLength = tmpFileConn.getContentLengthLong();
							if (fileLength > missLength) {
								writer.println(lineString1 + "\t" + fileLength);
								count++;
							}
						}
					}
				}
			}
		}
		writer.close();

		System.out.println(this.magDb.genomeCount + " in database");
		System.out.println(count + " genomes found");
	}
	
	public boolean isFinish() {
		return finish;
	}

	public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException {
		// TODO Auto-generated method stub

//		MagDownloadTask.createHumanGutLinks("Z:\\Kai\\HGM_DB\\human_gut_link.txt");
		
//		MagDownloadTask.createMarineLinks("");
		
		MagDownloadTask task = new MagDownloadTask(MagDatabase.nm_fish_gut, "Z:\\Kai\\Database\\non-model-fish-gut-v2-02");
		task.createDownloadLinks();
//		task.run();
/*
		FileUtils.copyURLToFile(new URL(
				"http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-gut/v2.0/species_catalogue/"
						+ "MGYG0000000/MGYG000000001/genome/MGYG000000001.faa"),
				new File("Z:\\Kai\\Database\\human_gut\\original_db\\MGYG000000001.faa"), 1000, 1000);
*/		
//		ResumableDownloadTask task = new ResumableDownloadTask("http://ftp.ebi.ac.uk/pub/databases/metagenomics/mgnify_genomes/human-gut/v2.0/species_catalogue/"
//				+ "MGYG0000000/MGYG000000009/genome/MGYG000000009.faa", "Z:\\Kai\\Database\\human_gut\\original_db\\MGYG000000009.faa");
//		task.run();
		
	}

}
