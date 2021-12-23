package com.marginallyclever.convenience.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * static log methods available everywhere
 * @author Dan Royer
 * @since Makelangelo 7.3.0
 */
public class Log {

	private static final Logger logger = LoggerFactory.getLogger(Log.class);
	private static final List<LogListener> listeners = new ArrayList<>();

	/**
	 * Appends a message to the log file
	 * @param msg HTML to put in the log file
	 */
	@Deprecated
	public static void write(String msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final String cleanMsg = sdf.format(Calendar.getInstance().getTime())+" "+msg;

		logger.info(msg);
		notifyListeners(cleanMsg);
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message append text as red HTML
	 */
	@Deprecated
	public static void error(String message) {
		write("ERROR "+message);
	}

	/**
	 * Appends a message to the log file.  Color will be green.
	 * @param str append text as green HTML
	 */
	@Deprecated
	public static void message(String str) {
		write(str);
	}


	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}

	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}

	private static void notifyListeners(String cleanMsg) {
		for( LogListener listener : listeners ) {
			listener.logEvent(cleanMsg);
		}
	}
}
