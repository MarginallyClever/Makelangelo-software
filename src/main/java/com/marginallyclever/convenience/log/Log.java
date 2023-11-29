package com.marginallyclever.convenience.log;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

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
		logger.info("Properties:");
		Properties p = System.getProperties();
		List<String> names = new ArrayList<>(p.stringPropertyNames());
		Collections.sort(names);
		names.forEach(name -> logger.info(name + " = " + p.get(name)));
		logger.info("------------------------------------------------");
		logger.info("Environment:");
		Map<String,String> env = System.getenv();
		names = new ArrayList<>(env.keySet());
		Collections.sort(names);
		names.forEach(name -> logger.info(name + " = " + env.get(name)));
		logger.info("------------------------------------------------");
	}

	public static File getLogLocation() {
		return new File(logDir, LOG_FILE_NAME_TXT);
	}
}
