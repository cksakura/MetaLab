package bmi.med.uOttawa.metalab.core.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

public class ResumableDownloadTask extends SwingWorker<Boolean, Void> {

	private String downloadUrl;
	private URLConnection downloadFileConnection;
	private File saveAsFile;
	private long fileLength;
	private int loopCount = 0;
	private boolean resume = false;

	public ResumableDownloadTask(String downloadUrl, String saveAsFileName) {
		this.downloadUrl = downloadUrl;
		this.saveAsFile = new File(saveAsFileName);
	}

	public ResumableDownloadTask(String downloadUrl, String saveAsFileName, boolean resume) {
		this.downloadUrl = downloadUrl;
		this.saveAsFile = new File(saveAsFileName);
		this.resume = resume;
	}

	public ResumableDownloadTask(String downloadUrl, String saveAsFileName, long fileLength) {
		this.downloadUrl = downloadUrl;
		this.saveAsFile = new File(saveAsFileName);
		this.fileLength = fileLength;
	}

	public ResumableDownloadTask(String downloadUrl, String saveAsFileName, long fileLength, boolean resume) {
		this.downloadUrl = downloadUrl;
		this.saveAsFile = new File(saveAsFileName);
		this.fileLength = fileLength;
		this.resume = resume;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		// TODO Auto-generated method stub

		System.out.println("Downloading " + downloadUrl + " ...");

		long bytesDownloaded = 0;

		try {

			URLConnection connection = new URI(downloadUrl).toURL().openConnection();
			if (connection instanceof HttpURLConnection) {
				HttpURLConnection tmpFileConn = (HttpURLConnection) new URI(downloadUrl).toURL().openConnection();
				tmpFileConn.setRequestMethod("HEAD");
				this.fileLength = tmpFileConn.getContentLengthLong();

				if (fileLength > 1000) {
					System.out.println("Total size: " + fileLength);

					this.downloadFileConnection = new URI(downloadUrl).toURL().openConnection();

					if (resume) {
						bytesDownloaded = this.downloadFileWithResume();
					} else {
						bytesDownloaded = this.downloadFile();
					}

					if (bytesDownloaded == fileLength) {
						setProgress(100);
						System.out.println(saveAsFile.getName() + " finished...");
						return true;
					} else {
						System.out.println(saveAsFile.getName() + " failed...");
						return false;
					}
				} else {

					try {
						FileUtils.copyURLToFile(new URL(downloadUrl), saveAsFile, 10000, 10000);
						setProgress(100);
						System.out.println(saveAsFile.getName() + " finished...");
						return true;
					} catch (Exception e) {
						System.out.println("Connecting timeout, please try again after the current work finish");
						return false;
					}
				}

			} else {

				FileUtils.copyURLToFile(new URL(downloadUrl), saveAsFile, 10000, 10000);

				setProgress(100);
				System.out.println(saveAsFile.getName() + " finished...");
				return true;
			}

		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}
	}

	public void done() {
		try {
			boolean finish = get();
			if (finish) {

			} else {
				System.out.println("Download task failed");
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long transferDataAndGetBytesDownloaded(URLConnection downloadFileConnection, File outputFile)
			throws IOException {
		long bytesDownloaded = 0;
		try (BufferedInputStream bis = new BufferedInputStream(downloadFileConnection.getInputStream());
				BufferedOutputStream bos = resume ? new BufferedOutputStream(new FileOutputStream(outputFile, true))
						: new BufferedOutputStream(new FileOutputStream(outputFile, false))) {

			byte[] buffer = new byte[1024 * 1024 * 8];

			int bytesCount;
			int progress = 0; // Keep track of progress to reduce frequent updates
			long lastUpdateTime = System.currentTimeMillis(); // Keep track of last update time

			while ((bytesCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, bytesCount);
				bytesDownloaded += bytesCount;

				// Update progress only after a certain amount of data has been downloaded
				if (bytesDownloaded * 100 / fileLength > progress) {
					progress = (int) (bytesDownloaded * 100 / fileLength);
					long currentTime = System.currentTimeMillis();
					// Update progress only once every 500 milliseconds
					if (currentTime - lastUpdateTime > 500) {
						setProgress(progress);
						lastUpdateTime = currentTime;
					}
				}
			}
		} catch (IOException e) {
			// Handle any IO exceptions that may occur during download
			e.printStackTrace();
		} finally {
			// Ensure proper cleanup of resources even in case of exceptions
			// Close the streams using try-with-resources
		}

		// Set progress to 100% after download is complete
		setProgress(100);

		return bytesDownloaded;
	}

	public long downloadFile() throws IOException, URISyntaxException {

		long bytesDownloaded = transferDataAndGetBytesDownloaded(downloadFileConnection, saveAsFile);
		return bytesDownloaded;
	}

	public long downloadFileWithResume() throws IOException, URISyntaxException {

		URLConnection downloadFileConnection = addFileResumeFunctionality(saveAsFile);

		long bytesDownloaded = transferDataAndGetBytesDownloaded(downloadFileConnection, saveAsFile);

		if (bytesDownloaded == fileLength) {
			return bytesDownloaded;
		} else {
			loopCount++;
			if (loopCount == 5) {
				return bytesDownloaded;
			} else {
				return downloadFileWithResume();
			}
		}
	}

	private URLConnection addFileResumeFunctionality(File outputFile)
			throws IOException, URISyntaxException, ProtocolException, ProtocolException {

		long existingFileSize = 0L;

		if (outputFile.exists() && downloadFileConnection instanceof HttpURLConnection) {

			HttpURLConnection httpFileConnection = (HttpURLConnection) downloadFileConnection;

			existingFileSize = outputFile.length();

			if (existingFileSize < fileLength) {
				httpFileConnection.setRequestProperty("Range", "bytes=" + existingFileSize + "-" + fileLength);
			} else {
//				throw new IOException("File Download already completed.");
			}
		}
		return downloadFileConnection;
	}

}
