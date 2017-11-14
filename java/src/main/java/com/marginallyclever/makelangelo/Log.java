package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.marginallyclever.makelangelo.preferences.MetricsPreferences;

/**
 * static log methods available everywhere
 * @author danroyer
 * @since 7.3.0
 */
public class Log {
	public static String LOG_FILE_SHARE_URL = "https://www.marginallyclever.com/shareLog.php";
	public static String LOG_FILE_PATH;
	public static String LOG_FILE_NAME_HTML = "log.html";
	public static String LOG_FILE_NAME_TXT = "log.txt";
	public static Logger logger;
	//private static FileHandler fileTXT;
	private static FileHandler fileHTML;
	
	public static void start() {
		crashReportCheck();
		deleteOldLog();
		
		LOG_FILE_PATH = System.getProperty("java.io.tmpdir");
		
		try {
			logger = Logger.getLogger("");
			
			//fileTXT=new FileHandler(LOG_FILE_NAME_TXT);
			//fileTXT.setFormatter(new SimpleFormatter());
			//logger.addHandler(fileTXT);
			
			fileHTML=new FileHandler(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
			fileHTML.setFormatter(new LogFormatterHTML());
			logger.addHandler(fileHTML);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("START");
		logger.info("OS="+System.getProperty("os.name").toLowerCase());
	}
	
	public static void end() {
		fileHTML.flush();
		logger.info("END");
		sendLog();
		deleteOldLog();
	}
	
	private static void crashReportCheck() {
		File oldLogFile = new File(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
		if( oldLogFile.exists() ) {
			// add a crashed message
			try {
				OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(oldLogFile));
				osw.write("</table>\n<h1>**CRASHED**</h1>\n</body>\n</html>");
				osw.flush();
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			sendLog();
		}
	}
	
	private static void sendLog() {
		boolean canShare = MetricsPreferences.areAllowedToShare();
		if(!canShare) return;
		
		fileHTML.close();
		File f= new File(LOG_FILE_PATH+LOG_FILE_NAME_HTML);
		if(!f.exists()) return;
		
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
		logger.log(Level.INFO, msg);
	}

	public static void info(String msg) {
		assert( logger != null );
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
		assert( logger != null );
		logger.log(Level.WARNING, msg );
	}

	/**
	 * Appends a message to the log file.  Color will be red.
	 * @param message
	 */
	public static void error(String msg) {
		assert( logger != null );
		logger.log(Level.SEVERE, msg );
	}
}
