package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

import com.marginallyclever.makelangelo.preferences.MetricsPreferences;

/**
 * static log methods available everywhere
 * @author danroyer
 * @since 7.3.0
 */
public class Log {
	public static String LOG_FILE_NAME_HTML = "log.html";
	public static String LOG_FILE_NAME_TXT = "log.txt";
	public static Logger logger;
	private static FileHandler fileTXT, fileHTML;
	

	public static void start() {
		crashReportCheck();
		deleteOldLog();
		
		logger = Logger.getLogger("");
		
		try {
			fileTXT=new FileHandler(LOG_FILE_NAME_TXT);
			fileTXT.setFormatter(new SimpleFormatter());
			logger.addHandler(fileTXT);
			
			fileHTML=new FileHandler(LOG_FILE_NAME_HTML);
			fileHTML.setFormatter(new LogFormatterHTML());
			logger.addHandler(fileHTML);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("START");
	}
	
	public static void end() {
		if(MetricsPreferences.areAllowedToShare()) {
			logger.info("END");
			sendLog();
			// delete the log file
		}
	}
	
	private static void crashReportCheck() {
		boolean oldLogExists = false;
		boolean canShare = MetricsPreferences.areAllowedToShare(); 
		if( oldLogExists && canShare ) {
			// Add line "** CRASHED **"
			// send it!
			sendLog();
		}
	}
	
	private static void sendLog() {
		File f = new File(LOG_FILE_NAME_HTML);
		if(!f.exists()) return;
	}
	
	/**
	 * wipe the log file
	 * @author danroyer
	 */
	protected static void deleteOldLog() {
		Path p = FileSystems.getDefault().getPath(LOG_FILE_NAME_HTML);
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	/**
	 * Appends a message to the log file
	 * @param color the hex code or HTML name of the color for this message
	 * @param msg the text
	 */
	public static void info(String color, String msg) {
		assert(logger!=null);
		logger.log(Level.INFO, msg);
	}

	public static void info(String msg) {
		assert(logger!=null);
		logger.log(Level.INFO, msg);
	}

	/**
	 * turns milliseconds into h:m:s
	 * @param millis
	 * @return
	 */
	public static String millisecondsToHumanReadable(long millis) {
		long s = millis / 1000;
		long m = s / 60;
		long h = m / 60;
		m %= 60;
		s %= 60;

		String elapsed = "";
		if (h > 0) elapsed += h + "h";
		if (h > 0 || m > 0) elapsed += m + "m";
		elapsed += s + "s ";

		return elapsed;
	}
	

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void warning(String msg) {
		assert(logger!=null);
		logger.log(Level.WARNING, msg );
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void error(String msg) {
		assert(logger!=null);
		logger.log(Level.SEVERE, msg );
	}
}
