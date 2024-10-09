package bmi.med.uOttawa.metalab.core.tools;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
	
	private static final String RESOURCE_PREFIX = "/resources/";

	public static InputStream load(String fileName) throws IOException {
		InputStream inputStream = ResourceLoader.class.getClassLoader().getResourceAsStream(fileName);

		if (inputStream == null) {
			inputStream = ResourceLoader.class.getResourceAsStream(RESOURCE_PREFIX + fileName);
		}

		if (inputStream == null) {
			throw new IOException("Unable to load resource: " + fileName);
		}
/*
		// check if running from a JAR file
		if (isRunningFromJar()) {
			inputStream = ResourceLoader.class.getResourceAsStream(RESOURCE_PREFIX + fileName);
		} else {
			inputStream = ResourceLoader.class.getClassLoader().getResourceAsStream(fileName);
		}

		if (inputStream == null) {
			throw new IOException("Unable to load resource: " + fileName);
		}
*/
		return inputStream;
	}

	private static boolean isRunningFromJar() {
		return ResourceLoader.class.getResource(RESOURCE_PREFIX) != null;
	}
}
