package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.marginallyclever.makelangelo.preferences.MetricsPreferences;

import ch.qos.logback.classic.BasicConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * static log methods available everywhere
 * @author dan royer
 * @since 7.3.0
 */
public class Log {
	public static String LOG_FILE_SHARE_URL = "https://www.marginallyclever.com/shareLog.php";
	public static String LOG_FILE_PATH;
	public static String LOG_FILE_NAME_HTML = "log.html";
	public static String LOG_FILE_NAME_TXT = "log.txt";
	public final static String PROGRAM_START_STRING = "PROGRAM START";
	public final static String PROGRAM_END_STRING = "PROGRAM END";
	
	//private static FileHandler fileTXT;
	
	private static Logger logger = null;
	private static ArrayList<LogListener> listeners = new ArrayList<LogListener>();

	public static void addListener(LogListener listener) {
		listeners.add(listener);
	}
	public static void removeListener(LogListener listener) {
		listeners.remove(listener);
	}
	
	
	/**
	 * wipe the log file
	 */
	public static void clear() {
		Path p = FileSystems.getDefault().getPath(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
		try {
			Files.deleteIfExists(p);
		} catch (IOException e1) {
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
		logger.info(msg);
		try (Writer fileWriter = new FileWriter(LOG_FILE_PATH+LOG_FILE_NAME_HTML, true)) {
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
	public static void write(String color, String message) {
		write("<div color='"+color+"'>"+message+"</div>\n");
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void error(String message) {
		write("red",message);
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void warning(String message) {
		write("ffdd00",message);		
	}
	
	public static void start() {
		LOG_FILE_PATH = System.getProperty("java.io.tmpdir");
		if(!LOG_FILE_PATH.endsWith(File.separator)) {
			LOG_FILE_PATH+=File.separator;
		}
			
		System.out.println("java.io.tmpdir="+LOG_FILE_PATH);

		
		crashReportCheck();
		deleteOldLog();
		
		logger = LoggerFactory.getLogger(Makelangelo.class);
		BasicConfigurator.configureDefaultContext();
		
		info(PROGRAM_START_STRING);
		info("OS="+System.getProperty("os.name").toLowerCase());
	}
	
	public static void end() {
		info(PROGRAM_END_STRING);
	}
	
	private static void crashReportCheck() {
		File oldLogFile = new File(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
		if( oldLogFile.exists() ) {
			// read last line of file
			String ending = tail(oldLogFile);
			
			if(!ending.contains(PROGRAM_END_STRING)) {
				// add a crashed message
				//sendLog();
			} // else no problem
		}
	}
	
	/**
	 * https://stackoverflow.com/a/7322581
	 * @param file
	 * @return
	 */
	public static String tail( File file ) {
	    RandomAccessFile fileHandler = null;
	    try {
	        fileHandler = new RandomAccessFile( file, "r" );
	        long fileLength = fileHandler.length() - 1;
	        StringBuilder sb = new StringBuilder();

	        for(long filePointer = fileLength; filePointer != -1; filePointer--){
	            fileHandler.seek( filePointer );
	            int readByte = fileHandler.readByte();

	            if( readByte == 0xA ) {
	                if( filePointer == fileLength ) {
	                    continue;
	                }
	                break;

	            } else if( readByte == 0xD ) {
	                if( filePointer == fileLength - 1 ) {
	                    continue;
	                }
	                break;
	            }

	            sb.append( ( char ) readByte );
	        }

	        String lastLine = sb.reverse().toString();
	        return lastLine;
	    } catch( java.io.FileNotFoundException e ) {
	        e.printStackTrace();
	        return null;
	    } catch( java.io.IOException e ) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (fileHandler != null )
	            try {
	                fileHandler.close();
	            } catch (IOException e) {
	                /* ignore */
	            }
	    }
	}
	
	@SuppressWarnings("unused")
	private static void sendLog() {
		boolean canShare = MetricsPreferences.areAllowedToShare();
		if(!canShare) return;
		
		File f= new File(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
		if(f.exists()) {
			// make an http request with the log file attached
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		    entityBuilder.addPart("upfile", new FileBody(f));
		    
		    HttpPost request = new HttpPost(LOG_FILE_SHARE_URL);
		    request.setEntity(entityBuilder.build());
	
		    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		    CloseableHttpClient client = clientBuilder.build();
		    try {
				HttpResponse response = client.execute(request);
				System.out.println(response);
			    StringWriter writer=new StringWriter();
			    IOUtils.copy(response.getEntity().getContent(),writer);
	            System.out.println(writer.toString());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// log file has mysteriously disappeared?!
		}
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
		assert( logger != null );
		write(color,msg);
	}

	public static void info(String msg) {
		assert( logger != null );
		write("00ff00",msg);
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
}
