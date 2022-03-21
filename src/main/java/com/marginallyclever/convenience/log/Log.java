package com.marginallyclever.convenience.log;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Convenience methods for logging.
 * @author Dan Royer
 *
 */
public class Log {
	private static final Logger logger = LoggerFactory.getLogger(Log.class);

	// must be consistent with logback.xml
	public static final String LOG_FILE_NAME_TXT = "makelangelo.log";
	public static final String LOG_FILE_NAME_PATTERN = "makelangelo\\..*log";
	public static final File logDir = new File(FileAccess.getHomeDirectory(), ".makelangelo");

	/**
	 * Initialize log file
	 */
	public static void start() {
		logger.info("------------------------------------------------");
		Properties p = System.getProperties();
		List<String> names = new ArrayList<>(p.stringPropertyNames());
		Collections.sort(names);
		for (String name : names) {
			logger.info("{} = {}", name, p.get(name));
		}
		logger.info("------------------------------------------------");
	}

	public static File getLogLocation() {
		return new File(logDir, LOG_FILE_NAME_TXT);
	}
}
