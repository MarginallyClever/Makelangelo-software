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
 * Initialize log file and purge old
 */
public class Log {

	private static Logger logger;

	// must be consistent with logback.xml
	private static final String LOG_FILE_NAME_TXT = "log.txt";
	private static final String PROGRAM_START_STRING = "PROGRAM START";
	private static final String PROGRAM_END_STRING = "PROGRAM END";

	public static void start() {
		boolean hadCrashed = crashReportCheck();
		deletePreviousLog();

		// lazy init to be able to purge old file
		logger = LoggerFactory.getLogger(Log.class);
		// custom logger name to separate a long debug info from useful info
		logger.info(PROGRAM_START_STRING);
		logger.info("------------------------------------------------");
		Properties p = System.getProperties();
		List<String> names = new ArrayList<>(p.stringPropertyNames());
		Collections.sort(names);
		for (String name : names) {
			logger.info("{} = {}", name, p.get(name));
		}
		logger.info("------------------------------------------------");

		if (hadCrashed) {
			logger.warn("Crash detected on previous run");
		}
	}

	public static void end() {
		logger.info(PROGRAM_END_STRING);
	}

	public static File getLogLocation() {
		return new File(FileAccess.getUserDirectory(), LOG_FILE_NAME_TXT);
	}

	private static boolean crashReportCheck() {
		File oldLogFile = getLogLocation();
		if( oldLogFile.exists() ) {
			// read last line of file
			String ending = FileAccess.tail(oldLogFile);
			return !ending.contains(PROGRAM_END_STRING);
		}
		return false;
	}

	/**
	 * wipe the previous log file
	 */
	private static void deletePreviousLog() {
		File toDelete = getLogLocation();
		if (toDelete.exists() && toDelete.isFile()) {
			toDelete.delete();
		}
	}

}
