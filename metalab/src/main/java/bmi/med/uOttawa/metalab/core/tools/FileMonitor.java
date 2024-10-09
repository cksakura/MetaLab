package bmi.med.uOttawa.metalab.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileMonitor {

	// Method 1: Using a marker file
	public static void monitorWithMarker(String folderPath, String fileName) {
		// The name of the marker file
		String markerName = fileName + ".marker";

		// The path of the folder to monitor
		File folder = new File(folderPath);

		// A flag to indicate if the file is copied completely
		boolean done = false;

		// Loop until the file is copied completely
		while (!done) {
			// List the files in the folder
			File[] files = folder.listFiles();

			// Check if the marker file exists
			for (File file : files) {
				if (file.getName().equals(markerName)) {
					// The marker file exists, so the file is copied completely
					done = true;
					break;
				}
			}

			// Wait for a while before checking again
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Do some analysis on the file
		System.out.println("The file " + fileName + " is copied completely.");
		// ...
	}

	// Method 2: Using the Watch Service API
	public static void monitorWithWatchService(String folderPath, String fileName) throws IOException {
		// The path of the folder to monitor
		Path folder = Paths.get(folderPath);

		// Create a watch service and register the folder for creation events
		WatchService watchService = FileSystems.getDefault().newWatchService();
		folder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

		// A flag to indicate if the file is copied completely
		boolean done = false;

		// Loop until the file is copied completely
		while (!done) {
			// Wait for a key to be available
			WatchKey key;
			try {
				key = watchService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			// Process the events for the key
			for (WatchEvent<?> event : key.pollEvents()) {
				// Get the kind of event and the file name
				WatchEvent.Kind<?> kind = event.kind();
				WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
				Path file = pathEvent.context();

				// Check if the event is a creation event and the file name matches
				if (kind == StandardWatchEventKinds.ENTRY_CREATE && file.toString().equals(fileName)) {
					// The file is created, but it may not be copied completely yet
					// To check if the file is copied completely, we can compare its size at
					// different times
					// If the size does not change, then the file is copied completely

					// The path of the file to monitor
					Path filePath = folder.resolve(file);

					// The initial size of the file
					long initialSize = Files.size(filePath);

					// Wait for a while before checking the size again
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// The final size of the file
					long finalSize = Files.size(filePath);

					// Compare the initial and final sizes
					if (initialSize == finalSize) {
						// The file is copied completely
						done = true;
						break;
					}
				}
			}

			// Reset the key to receive further events
			key.reset();
		}

		// Do some analysis on the file
		System.out.println("The file " + fileName + " is copied completely.");
		// ...
	}

	public static void main(String[] args) throws IOException {
		// The path of the folder to monitor
		String folderPath = "C:\\Users\\User\\Documents\\Test";

		// The name of the file to monitor
		String fileName = "data.txt";

		// Choose one of the methods to monitor the file
		// monitorWithMarker(folderPath, fileName);
		monitorWithWatchService(folderPath, fileName);
	}
}
