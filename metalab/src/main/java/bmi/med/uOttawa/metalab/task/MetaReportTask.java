/**
 * 
 */
package bmi.med.uOttawa.metalab.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generate the reports thought submitting the result files to ocpu.
 * 
 * @author Kai Cheng
 *
 */
public class MetaReportTask extends SwingWorker<Boolean, Object> {

	private static final Logger LOGGER = LogManager.getLogger(MetaReportTask.class);
	private static final String taskName = "Report generation task";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static final String mainName = "http://206.12.91.148";
	private static final String server1 = "http://206.12.91.148";
	private static final String server2 = "http://44.241.51.105";

	private static final String summary_api = "/ocpu/library/rmdocpu/R/render_MQsummary_file";
	private static final String peptide_api = "/ocpu/library/rmdocpu/R/render_peptides_file";
	private static final String protein_api = "/ocpu/library/rmdocpu/R/render_proteinGroups_file";
	private static final String taxon_api = "/ocpu/library/rmdocpu/R/render_taxon_file";
	private static final String function_api = "/ocpu/library/rmdocpu/R/render_function_file";
	private static final String output = "output.html";

	public static final String summary = "summary";
	public static final String peptide = "peptide";
	public static final String protein = "protein";
	public static final String taxon = "taxon";
	public static final String function = "function";
	
	private static String localRExe = "R_metalab/bin/R.exe";
	private static String localRLib = "R_metalab/library";
	private static final String[] localCMD = new String[] { "R","CMD","BATCH","--no-save","--no-restore"};

	private String taskType;
	private File inputPath;
	private File outputPath;
	private File metaPath;
	
	private ExecutorService executor;
	private ArrayList<File> outputList;
	
	public MetaReportTask(int threadCount) {
		this.executor = Executors.newFixedThreadPool(threadCount);
		this.outputList = new ArrayList<File>();
	}
	
	public boolean addTask(String taskType, File inputPath, File outputPath) {
		return addTask(taskType, inputPath, outputPath, null);
	}
	
	public boolean addTask(String taskType, File inputPath, File outputPath, File metaPath) {

		boolean useLocalR = true;
		File localRExeFile = new File(localRExe);
		if (!localRExeFile.exists()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": local R.exe was not found");
			LOGGER.info(taskName + ": local R.exe was not found");
			useLocalR = false;
		}
		
		File localRLibFile = new File(localRLib);
		if (!localRLibFile.exists()) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": local R library was not found");
			LOGGER.info(taskName + ": local R library was not found");
			useLocalR = false;
		}
		
		if (!useLocalR) {
			return this.runHttpClient(taskType, inputPath, outputPath, metaPath);
		}

		File outputDir = outputPath.getParentFile();
		File rscriptsFile = new File(outputDir.getParent(), "rscripts");
		if (!rscriptsFile.exists()) {
			rscriptsFile.mkdirs();
		}

		File htmlFile = new File(outputDir.getParent(), "rmd_html");
		if (!htmlFile.exists()) {
			htmlFile.mkdirs();
		}

		System.out.println(format.format(new Date()) + "\t" + taskName + ": adding " + taskType + " report task");
		LOGGER.info(taskName + ": adding " + taskType + " report task");

		File rFile = new File(rscriptsFile, taskType + ".r");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(rFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": writing R script to " + rFile + " failed");
			LOGGER.error(taskName + ": writing R script to " + rFile + " failed");
			return false;
		}

		writer.println("rmarkdown::find_pandoc(dir =  .libPaths())");
		writer.println("library(rmetalab)");
		writer.println("library(metareport)");
		writer.println("metareport(type = \"" + taskType + "\",");
		writer.println("data_file = \"" + inputPath.getAbsolutePath().replaceAll("\\\\", "//") + "\",");

		if (metaPath != null && metaPath.exists()) {
			writer.println("meta_file = \"" + metaPath.getAbsolutePath().replaceAll("\\\\", "//") + "\",");
		}
		writer.println("output_dir = \"" + outputDir.getAbsolutePath().replaceAll("\\\\", "//") + "//\",");
		writer.println("output_file = \"" + outputPath.getName() + "\")");

		writer.close();

		outputList.add(outputPath);

		executor.submit(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {

					ArrayList<String> commandAndArgs = new ArrayList<String>();
					commandAndArgs.add(localRExeFile.getAbsolutePath());
					commandAndArgs.addAll(Arrays.asList(localCMD));
					commandAndArgs.add(rFile.getAbsolutePath());

					ProcessBuilder pb = new ProcessBuilder(commandAndArgs);

					Process p = pb.start();
					BufferedInputStream in = new BufferedInputStream(p.getInputStream());
					BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
					String lineStr;
					while ((lineStr = inBr.readLine()) != null) {
						LOGGER.info(lineStr);
						System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
					}

					if (p.waitFor() != 0) {
						if (p.exitValue() == 1)
							LOGGER.error("false");
					}
					inBr.close();
					in.close();

				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					LOGGER.error(e);
				}
			}
		});

		return true;
	}

	public boolean runCLI() {
		return runHttpClient();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		executor.shutdown();

		boolean finish = executor.awaitTermination(30, TimeUnit.MINUTES);

		if (finish) {
			for (int i = 0; i < this.outputList.size(); i++) {
				if (!this.outputList.get(i).exists()) {
					return false;
				}
			}
		} else {
			return false;
		}

		return true;
	}

	
	@SuppressWarnings("unused")
	private boolean localRun() {

		File localRExeFile = new File(localRExe);
		if (!localRExeFile.exists()) {
			return false;
		}

		File localRLibFile = new File(localRLib);
		if (!localRLibFile.exists()) {
			return false;
		}

		File outputDir = outputPath.getParentFile();
		File rscriptsFile = new File(outputDir.getParent(), "rscripts");
		if (!rscriptsFile.exists()) {
			rscriptsFile.mkdirs();
		}

		File htmlFile = new File(outputDir.getParent(), "rmd_html");
		if (!htmlFile.exists()) {
			htmlFile.mkdirs();
		}

		File rFile = new File(rscriptsFile, taskType + ".r");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(rFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": writing R script to " + rFile + " failed");
			LOGGER.error(taskName + ": writing R script to " + rFile + " failed");
			return false;
		}

		writer.println(
				"rmarkdown::find_pandoc(dir = \"" + localRLibFile.getAbsolutePath().replaceAll("\\\\", "//") + "\")");

		writer.println("library(rmetalab)");
		writer.println("library(metareport)");
		writer.println("metareport(type = \"" + taskType + "\",");
		writer.println("data_file = \"" + inputPath.getAbsolutePath().replaceAll("\\\\", "//") + "\",");

		if (metaPath != null && metaPath.exists()) {
			writer.println("meta_file = \"" + metaPath.getAbsolutePath().replaceAll("\\\\", "//") + "\",");
		}
		writer.println("output_dir = \"" + outputDir.getAbsolutePath().replaceAll("\\\\", "//") + "//\",");
		writer.println("output_file = \"" + outputPath.getName() + "\")");

		writer.close();

		Runtime run = Runtime.getRuntime();
		try {
			
			ArrayList<String> commandAndArgs = new ArrayList<String>();
			commandAndArgs.add(localRExeFile.getAbsolutePath());
			commandAndArgs.addAll(Arrays.asList(localCMD));
			commandAndArgs.add(rFile.getAbsolutePath());

			ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
			
			Process p = pb.start();

			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				LOGGER.info(lineStr);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
			}

			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)
					LOGGER.error("false");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			System.err.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
					+ outputPath.getName() + " failed");
			LOGGER.error(taskName + ": exporting report to " + outputPath.getName() + " failed");

			return false;
		}

		return outputPath.exists();
	}

	private String getServer() {
		int timeout = 10;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpGet httpGet = new HttpGet(server1 + "/ocpu/test/");
		boolean good = true;
		try {
			client.execute(httpGet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": cann't connect to server 1, switch to another");
			LOGGER.error(taskName + ": cann't connect to server 1", e);
			good = false;
		}

		if (good) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": connected to server 1");
			LOGGER.info(taskName + ": connected to server 1");
			return server1;
		}

		httpGet = new HttpGet(server2 + "/ocpu/test/");
		good = true;

		try {
			client.execute(httpGet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(format.format(new Date()) + "\t" + taskName + ": cann't connect to server 2");
			LOGGER.error(taskName + ": cann't connect to server 2", e);
			good = false;
		}

		if (good) {
			System.out.println(format.format(new Date()) + "\t" + taskName + ": connected to server 2");
			LOGGER.info(taskName + ": connected to server 2");
			return server2;
		}

		return "";
	}

	/**
	 * Send the request by Apache API.
	 * 
	 * @return
	 */
	private boolean runHttpClient() {

		String server = this.getServer();
		if (server.length() == 0) {
			return false;
		}

		int timeout = 300;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpPost post;

		if (this.taskType.equals(summary)) {
			post = new HttpPost(server + summary_api);
		} else if (this.taskType.equals(peptide)) {
			post = new HttpPost(server + peptide_api);
		} else if (this.taskType.equals(protein)) {
			post = new HttpPost(server + protein_api);
		} else if (this.taskType.equals(taxon)) {
			post = new HttpPost(server + taxon_api);
		} else if (this.taskType.equals(function)) {
			post = new HttpPost(server + function_api);
		} else {
			return false;
		}

		LOGGER.info(taskName + ": exporting " + taskType + " report");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting " + taskType + " report");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", inputPath, ContentType.MULTIPART_FORM_DATA, inputPath.getName());

		if (metaPath != null && metaPath.exists()) {
			builder.addBinaryBody("meta", metaPath, ContentType.MULTIPART_FORM_DATA, metaPath.getName());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		CloseableHttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			return false;
		}

		int code = response.getStatusLine().getStatusCode();
		if (code != 201) {
			LOGGER.error(taskName + ": error in exporting " + taskType + " report, error code: " + code);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			return false;
		}

		HttpEntity entity = response.getEntity();

		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		ContentType contentType = ContentType.getOrDefault(entity);

		Charset charset = contentType.getCharset();

		if (charset == null) {
			charset = Charset.defaultCharset();
		}

		String url = "";
		boolean find = false;

		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.endsWith(output)) {
					url = mainName + line;
					System.out.println(format.format(new Date()) + "\t" + taskName + ": find url " + url);
					find = true;
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		if (find) {
			try {
				FileUtils.copyURLToFile(new URL(url), outputPath);

				LOGGER.info(taskName + ": exporting report to " + outputPath.getName() + " finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
						+ outputPath.getName() + " finished");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			}
		} else {
			System.err.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
					+ outputPath.getName() + " failed");
			LOGGER.error(taskName + ": exporting report to " + outputPath.getName() + " failed");
		}

		return find;
	}
	
	/**
	 * Send the request by Apache API.
	 * 
	 * @return
	 */
	private boolean runHttpClient(String taskType, File inputPath, File outputPath, File metaPath) {

		String server = this.getServer();
		if (server.length() == 0) {
			return false;
		}

		int timeout = 300;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpPost post;

		if (taskType.equals(summary)) {
			post = new HttpPost(server + summary_api);
		} else if (taskType.equals(peptide)) {
			post = new HttpPost(server + peptide_api);
		} else if (taskType.equals(protein)) {
			post = new HttpPost(server + protein_api);
		} else if (taskType.equals(taxon)) {
			post = new HttpPost(server + taxon_api);
		} else if (taskType.equals(function)) {
			post = new HttpPost(server + function_api);
		} else {
			return false;
		}

		LOGGER.info(taskName + ": exporting " + taskType + " report");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting " + taskType + " report");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", inputPath, ContentType.MULTIPART_FORM_DATA, inputPath.getName());

		if (metaPath != null && metaPath.exists()) {
			builder.addBinaryBody("meta", metaPath, ContentType.MULTIPART_FORM_DATA, metaPath.getName());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		CloseableHttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			return false;
		}

		int code = response.getStatusLine().getStatusCode();
		if (code != 201) {
			LOGGER.error(taskName + ": error in exporting " + taskType + " report, error code: " + code);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			return false;
		}

		HttpEntity entity = response.getEntity();

		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		ContentType contentType = ContentType.getOrDefault(entity);

		Charset charset = contentType.getCharset();

		if (charset == null) {
			charset = Charset.defaultCharset();
		}

		String url = "";
		boolean find = false;

		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.endsWith(output)) {
					url = mainName + line;
					System.out.println(format.format(new Date()) + "\t" + taskName + ": find url " + url);
					find = true;
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
		}

		if (find) {
			try {
				FileUtils.copyURLToFile(new URL(url), outputPath);

				LOGGER.info(taskName + ": exporting report to " + outputPath.getName() + " finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
						+ outputPath.getName() + " finished");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in exporting " + taskType + " report", e);
				System.err.println(
						format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType + " report");
			}
		} else {
			System.err.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
					+ outputPath.getName() + " failed");
			LOGGER.error(taskName + ": exporting report to " + outputPath.getName() + " failed");
		}

		return find;
	}

	/**
	 * Send the request by Apache API.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean runHttpClient(String server) {

		int timeout = 300;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		HttpPost post;

		if (this.taskType.equals(summary)) {
			post = new HttpPost(server + summary_api);
		} else if (this.taskType.equals(peptide)) {
			post = new HttpPost(server + peptide_api);
		} else if (this.taskType.equals(protein)) {
			post = new HttpPost(server + protein_api);
		} else if (this.taskType.equals(taxon)) {
			post = new HttpPost(server + taxon_api);
		} else if (this.taskType.equals(function)) {
			post = new HttpPost(server + function_api);
		} else {
			return false;
		}

		LOGGER.info(taskName + ": exporting " + taskType + " report");
		System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting " + taskType + " report");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", inputPath, ContentType.MULTIPART_FORM_DATA, inputPath.getName());

		if (metaPath != null && metaPath.exists()) {
			builder.addBinaryBody("meta", metaPath, ContentType.MULTIPART_FORM_DATA, metaPath.getName());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		CloseableHttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
			LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
			LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
			return false;
		}

		int code = response.getStatusLine().getStatusCode();
		if (code != 201) {
			LOGGER.error(taskName + ": error in exporting " + taskType + " report, error code: " + code);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
			return false;
		}

		HttpEntity entity = response.getEntity();

		InputStream instream = null;
		try {
			instream = entity.getContent();
		} catch (UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
		}

		ContentType contentType = ContentType.getOrDefault(entity);

		Charset charset = contentType.getCharset();

		if (charset == null) {
			charset = Charset.defaultCharset();
		}

		String url = "";
		boolean find = false;

		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.endsWith(output)) {
					url = server + line;
					System.out.println(format.format(new Date()) + "\t" + taskName + ": find url " + url);
					find = true;
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
		}

		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
			System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
					+ " report from server " + server);
		}

		if (find) {
			try {
				FileUtils.copyURLToFile(new URL(url), outputPath);

				LOGGER.info(taskName + ": exporting report to " + outputPath.getName() + " finished");
				System.out.println(format.format(new Date()) + "\t" + taskName + ": exporting report to "
						+ outputPath.getName() + " finished");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(taskName + ": error in exporting " + taskType + " report from server " + server, e);
				System.out.println(format.format(new Date()) + "\t" + taskName + ": error in exporting " + taskType
						+ " report from server " + server);
			}
		}

		return find;
	}
	
	/**
	 * Send the request by CURL. Currently not used.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void curl() throws MalformedURLException, IOException {

		String command;

		if (this.taskType.equals(summary)) {
			command = "curl -v " + summary_api + " -F \"file=@" + inputPath.getAbsolutePath() + "\"";
		} else if (this.taskType.equals(peptide)) {
			command = "curl -v " + peptide_api + " -F \"file=@" + inputPath.getAbsolutePath() + "\"";
		} else if (this.taskType.equals(protein)) {
			command = "curl -v " + protein_api + " -F \"file=@" + inputPath.getAbsolutePath() + "\"";
		} else if (this.taskType.equals(taxon)) {
			command = "curl -v " + taxon_api + " -F \"file=@" + inputPath.getAbsolutePath() + "\"";
		} else if (this.taskType.equals(function)) {
			command = "curl -v " + function_api + " -F \"file=@" + inputPath.getAbsolutePath() + "\"";
		} else {
			return;
		}

		if (metaPath != null) {
			command += " -F \"meta=@";
			command += metaPath.getAbsolutePath() + "\"";
		}

		System.out.println(format.format(new Date()) + "\t" + taskName + ": " + command);

		String url = "";
		boolean find = false;
		Runtime run = Runtime.getRuntime();

		try {

			Process process = run.exec(command);

			// InputStream stream = process.getInputStream();
			InputStream stream = process.getErrorStream();

			String line = null;

			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(stream, Charset.defaultCharset()))) {
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
					if (line.endsWith(output)) {
						url = mainName + line;
						System.out.println(format.format(new Date()) + "\t" + taskName + ": find url " + url);
						find = true;
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error in gerenating report", e);
		}

		if (find) {
			FileUtils.copyURLToFile(new URL(url), outputPath);
		} else {
			System.out.println(
					format.format(new Date()) + "\t" + taskName + ": exporting report to " + outputPath + " is failed");
		}
	}

	/**
	 * Send the request by HTTP methods. Currently not used.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings({ "unused", "resource" })
	private void htmlTest() throws IOException {
		URL serverUrl = new URL(summary_api);
		HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

		String boundaryString = Long.toHexString(System.currentTimeMillis());
		;

		// Indicate that we want to write to the HTTP request body
		urlConnection.setDoOutput(true);
		urlConnection.setRequestMethod("POST");
		urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

		OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
		BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

		// Include value from the myFileDescription text area in the post data
		httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
		httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
		httpRequestBodyWriter.write("\n\n");
		httpRequestBodyWriter.write("Log file for 20150208");

		// Include the section to describe the file
		httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
		httpRequestBodyWriter.write("Content-Disposition: form-data;" + "name=\"myFile\";" + "filename=\""
				+ this.inputPath.getName() + "\"" + "\nContent-Type: text/plain\n\n");
		httpRequestBodyWriter.flush();

		// Write the actual file contents
		FileInputStream inputStreamToLogFile = new FileInputStream(inputPath);

		int bytesRead;
		byte[] dataBuffer = new byte[1024];
		while ((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
			outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
		}

		outputStreamToRequestBody.flush();

		// Mark the end of the multipart http request
		httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
		httpRequestBodyWriter.flush();

		// Close the streams
		outputStreamToRequestBody.close();
		httpRequestBodyWriter.close();
	}
	
	protected static void localTest(String taskType, File inputPath) {
		File localRExeFile = new File("E:/Exported/exe/R_metalab/bin/R.exe");
		File htmlFile = new File(inputPath.getParent(), taskType + ".html");
		File rFile = new File(inputPath.getParent(), taskType + ".r");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(rFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(
					format.format(new Date()) + "\t" + taskName + ": writing R script to " + rFile + " failed");
			LOGGER.error(taskName + ": writing R script to " + rFile + " failed");
			return;
		}

		writer.println("rmarkdown::find_pandoc(dir =  .libPaths())");
		writer.println("library(rmetalab)");
		writer.println("library(metareport)");
		writer.println("metareport(type = \"" + taskType + "\",");
		writer.println("data_file = \"" + inputPath.getAbsolutePath().replaceAll("\\\\", "//") + "\",");
		writer.println("output_dir = \"" + inputPath.getParent().replaceAll("\\\\", "//") + "//\",");
		writer.println("output_file = \"" + htmlFile.getName() + "\")");
		writer.close();

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {

					ArrayList<String> commandAndArgs = new ArrayList<String>();
					commandAndArgs.add(localRExeFile.getAbsolutePath());
					commandAndArgs.addAll(Arrays.asList(localCMD));
					commandAndArgs.add(rFile.getAbsolutePath());

					ProcessBuilder pb = new ProcessBuilder(commandAndArgs);

					Process p = pb.start();
					BufferedInputStream in = new BufferedInputStream(p.getInputStream());
					BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
					String lineStr;
					while ((lineStr = inBr.readLine()) != null) {
						LOGGER.info(lineStr);
						System.out.println(format.format(new Date()) + "\t" + taskName + ": " + lineStr);
					}

					if (p.waitFor() != 0) {
						if (p.exitValue() == 1)
							LOGGER.error("false");
					}
					inBr.close();
					in.close();

				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					LOGGER.error(e);
				}
			}
		});

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
