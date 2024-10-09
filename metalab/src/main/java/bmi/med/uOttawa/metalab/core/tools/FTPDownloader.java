package bmi.med.uOttawa.metalab.core.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPDownloader {

	public static void main(String[] args) {
		// Create an instance of FTPClient
		FTPClient ftpClient = new FTPClient();
		try {
			// Connect to the FTP server
			ftpClient.connect("https://ftp.pride.ebi.ac.uk/pride/data/archive/2022/12/PXD036445/", 21);
			// Login with username and password
//			ftpClient.login("ftpUser", "ftpPassword");
			// Enter passive mode
			ftpClient.enterLocalPassiveMode();
			// Set file type to binary
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			// The remote file name on the FTP server
			String remoteFile = "/20181112_QX8_PhGe_SA_EasyLC12-4_A_a1_158_TP96hrs_control_rep2_181116081537.raw";

			// The local file name where the downloaded file will be saved
			File downloadFile = new File("Z:\\Kai\\Raw_files\\PXD036445\\"
					+ "20181112_QX8_PhGe_SA_EasyLC12-4_A_a1_158_TP96hrs_control_rep2_181116081537.raw");

			// Create an output stream for writing the downloaded file
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));

			// Download the file from the FTP server
			boolean success = ftpClient.retrieveFile(remoteFile, outputStream);

			// Close the output stream
			outputStream.close();

			// Check if the download was successful
			if (success) {
				System.out.println("File has been downloaded successfully.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
