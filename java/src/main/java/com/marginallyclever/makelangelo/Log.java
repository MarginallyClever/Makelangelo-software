package com.marginallyclever.makelangelo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * static log methods available everywhere
 * @author danroyer
 * @since 7.3.0
 */
public class Log {
	/**
	 * logging
	 * @see org.slf4j.Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(Makelangelo.class);
	private static ArrayList<LogListener> listeners = new ArrayList<LogListener>();

	
	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}
	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * wipe the log file
	 * @author danroyer
	 */
	public static void clear() {
		Path p = FileSystems.getDefault().getPath("log.html");
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// print starting time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		write("<h3>" + sdf.format(cal.getTime()) + "</h3>\n");
	}


	/**
	 * Appends a message to the log file
	 * @param msg
	 */
	public static void write(String msg) {
		try (Writer fileWriter = new FileWriter("log.html", true)) {
			PrintWriter logToFile = new PrintWriter(fileWriter);
			logToFile.write(msg);
			logToFile.flush();
		} catch (IOException e) {
			logger.error("{}", e);
		}
		
		for( LogListener listener : listeners ) {
			listener.logEvent(msg);
		}
	}


	/**
	 * Appends a message to the log file
	 * @param color the hex code or HTML name of the color for this message
	 * @param msg the text
	 */
	public static void write(String color, String msg) {
		write("<font color='"+color+"'>"+msg+"</font>\n");
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void error(String message) {
		write("red",message);
	}

	/**
	 * Appends a message to the log file.  Color will be green.
	 * @param message
	 */
	public static void message(String message) {
		write("green",message);		
	}
}
