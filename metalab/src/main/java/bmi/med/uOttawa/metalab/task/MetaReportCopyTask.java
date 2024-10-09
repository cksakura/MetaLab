package bmi.med.uOttawa.metalab.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetaReportCopyTask {

	private static final Logger LOGGER = LogManager.getLogger(MetaReportCopyTask.class);
	private static final String taskName = "Report file copy task";
	private static final SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

	private static String reportPath = "/bmi/med/uOttawa/metalab/report";

	public MetaReportCopyTask() {
		
	}
	
	/**
     * Copies the contents of the specified source folder to the specified destination folder.
     *
     * @param sourceFolder the relative path of the source folder to copy from
     * @param destinationFolder the destination folder to copy to
     * @throws IOException if an I/O error occurs during the copy operation
	 * @throws URISyntaxException 
     */
	public static void copyJarFolder(File destinationFolder, String version) throws IOException, URISyntaxException {
		
		URL url = MetaReportCopyTask.class.getResource(reportPath);
		LOGGER.info("copy from "+url);
		Path sourceFolderPath = Paths.get(url.toURI()).toAbsolutePath();
		
		Path destinationFolderPath = destinationFolder.toPath();

		if (!Files.isDirectory(sourceFolderPath)) {
			throw new IllegalArgumentException("Source folder must be a directory");
		}
		if (!Files.exists(destinationFolderPath)) {
			Files.createDirectories(destinationFolderPath);
		}

		Files.walk(sourceFolderPath).forEach(sourcePath -> {
			try {
				Path destinationPath = destinationFolderPath.resolve(sourceFolderPath.relativize(sourcePath));
				Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		File indexFile = new File(destinationFolder, "index.html");
		if (indexFile.exists()) {
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = null;
			String line = null;
			try {
				reader = new BufferedReader(new FileReader(indexFile));
				while ((line = reader.readLine()) != null) {
					line = line.replace("version", version);
					sb.append(line).append("\t");
				}
				reader.close();

				PrintWriter writer = new PrintWriter(indexFile);
				writer.print(sb);
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void copy(File toPath, String version) {
		MetaReportCopyTask task = new MetaReportCopyTask();
		try {
			task.loadRecourseFromJarByFolder(reportPath, toPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error(taskName + ": error in creating the report folder", e);
			System.err.println(format.format(new Date()) + "\t" + taskName + ": error in creating the report folder");
		}

		File indexFile = new File(toPath, "index.html");
		if (indexFile.exists()) {
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = null;
			String line = null;
			try {
				reader = new BufferedReader(new FileReader(indexFile));
				while ((line = reader.readLine()) != null) {
					line = line.replace("version", version);
					sb.append(line).append("\t");
				}
				reader.close();

				PrintWriter writer = new PrintWriter(indexFile);
				writer.print(sb);
				writer.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadRecourseFromJarByFolder(String fromPath, File toPath) throws IOException {

		URL url = getClass().getResource(reportPath);

		LOGGER.info(taskName + ": copying report folder from " + url + " started");
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": copying report folder from " + url + " started");

		URLConnection urlConnection = url.openConnection();

		if (url.getProtocol().equals("file")) {
			copyFileResources(url, toPath);
		} else {
			copyJarResources((JarURLConnection) urlConnection, toPath);
		}

		LOGGER.info(taskName + ": copying report folder from " + url + " finished");
		System.out.println(
				format.format(new Date()) + "\t" + taskName + ": copying report folder from " + url + " finished");
	}

	private void copyFileResources(URL url, File toPath) throws IOException {
		FileUtils.copyDirectory(new File(url.getPath()), toPath);
	}

	private void copyJarResources(JarURLConnection jarURLConnection, File toPath) throws IOException {
		JarFile jarFile = jarURLConnection.getJarFile();
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			if (entry.getName().startsWith(jarURLConnection.getEntryName()) && !entry.getName().endsWith("/")) {
				copyFromJar("/" + entry.getName(), toPath);
			}
		}
		jarFile.close();
	}

	private void copyFromFile(String fromPath, File toPath) throws IOException {

		int index = fromPath.lastIndexOf('/');

		String filename = fromPath.substring(index + 1);
		File file = new File(toPath, filename);

		if (!file.exists() && !file.createNewFile()) {
			return;
		}

		byte[] buffer = new byte[1024];
		int readBytes;

		URL fromUrl = getClass().getResource(fromPath);
		URLConnection urlConnection = fromUrl.openConnection();
		InputStream is = urlConnection.getInputStream();

		if (is == null) {
			throw new FileNotFoundException("File " + fromPath + " was not found inside JAR.");
		}

		OutputStream os = new FileOutputStream(file);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing report file to " + file, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing report file to " + file);
		} finally {
			os.close();
			is.close();
		}
	}

	private void copyFromJar(String fromPath, File toPath) throws IOException {

		String toPathName = toPath.getName();
		int index = fromPath.indexOf("/" + toPathName + "/");

		File file = new File(toPath.getParent() + fromPath.substring(index));

		File parent = file.getParentFile();

		if (!parent.exists()) {
			parent.mkdirs();
		}

		byte[] buffer = new byte[1024];
		int readBytes;

		URL fromUrl = getClass().getResource(fromPath);
		URLConnection urlConnection = fromUrl.openConnection();
		InputStream is = urlConnection.getInputStream();

		if (is == null) {
			throw new FileNotFoundException("File " + fromPath + " was not found inside JAR.");
		}

		OutputStream os = new FileOutputStream(file);
		try {
			while ((readBytes = is.read(buffer)) != -1) {
				os.write(buffer, 0, readBytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in writing report file to " + file, e);
			System.err.println(format.format(new Date()) + "\t" + "Error in writing report file to " + file);
		} finally {
			os.close();
			is.close();
		}
	}

	/**
     * Copies the contents of the specified source folder to the specified destination folder.
     *
     * @param sourceFolder the source folder to copy from
     * @param destinationFolder the destination folder to copy to
     * @throws IOException if an I/O error occurs during the copy operation
     */
	public static void copyFolder(Path sourceFolder, Path destinationFolder) throws IOException {
		if (!Files.isDirectory(sourceFolder)) {
			throw new IllegalArgumentException("Source folder must be a directory");
		}
		if (!Files.exists(destinationFolder)) {
			Files.createDirectories(destinationFolder);
		}

		Files.walk(sourceFolder).forEach(sourcePath -> {
			try {
				Path destinationPath = destinationFolder.resolve(sourceFolder.relativize(sourcePath));
				Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
    
	public static void main(String[] args) throws URISyntaxException {
		// TODO Auto-generated method stub

		MetaReportCopyTask.copy(
				new File("D:\\Exported\\cmd_test\\cmd_test"),
				"betabeta");
		
//		Path path = Paths.get(MetaReportCopyTask.class.getResource(reportPath).toURI());
//		System.out.println(path);
	}

}
