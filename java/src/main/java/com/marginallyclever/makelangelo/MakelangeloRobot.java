package com.marginallyclever.makelangelo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;

/**
 * @author Admin
 * @since 7.2.10
 *
 */
public class MakelangeloRobot implements MarginallyCleverConnectionReadyListener {
	// constants
	private String robotTypeName = "DRAWBOT";
	private String hello = "HELLO WORLD! I AM " + robotTypeName + " #";

	// god object!
	private final Makelangelo mainGUI=null;
		
	// settings go here
	public MakelangeloRobotSettings settings = null;
	
	// current state goes here
	private MarginallyCleverConnection connection = null;
	private boolean portConfirmed = false;


	// Listeners which should be notified of a change to the percentage.
    private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();


	private final Logger logger = LoggerFactory.getLogger(MakelangeloRobot.class);

	
	
	public MarginallyCleverConnection getConnection() {
		return connection;
	}

	public void setConnection(MarginallyCleverConnection c) {
		this.connection = c;
		
		this.connection.addListener(this);
	}

	@Override
	public void serialConnectionReady(MarginallyCleverConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serialDataAvailable(MarginallyCleverConnection arg0, String data) {
		if (portConfirmed == true) return;
		if (data.lastIndexOf(hello) < 0) return;

		portConfirmed = true;
		String after_hello = data.substring(data.lastIndexOf(hello) + hello.length());
		parseRobotUID(after_hello);
		notifyPortConfirmed();

		if(mainGUI != null) mainGUI.confirmConnected();
	}
	
	public boolean isPortConfirmed() {
		return portConfirmed;
	}
	
	public void parseRobotUID(String line) {
		settings.saveConfig();

		// get the UID reported by the robot
		String[] lines = line.split("\\r?\\n");
		long new_uid = 0;
		if (lines.length > 0) {
			try {
				new_uid = Long.parseLong(lines[0]);
			} catch (NumberFormatException e) {
				logger.error("{}", e);
			}
		}

		// new robots have UID=0
		if (new_uid == 0) {
			new_uid = getNewRobotUID();
		}
		
		// load machine specific config
		settings.loadConfig(new_uid);
	}

	void notifyPortConfirmed() {
		for (MakelangeloRobotListener listener : listeners) {
			listener.portConfirmed(this);
		}
	}

	public void addListener(MakelangeloRobotListener listener) {
		listeners.add(listener);
		
	}

	public void removeListener(MakelangeloRobotListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long newUID = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/drawbot_getuid.php");
			URLConnection conn = url.openConnection();
			try (
					final InputStream connectionInputStream = conn.getInputStream();
					final Reader inputStreamReader = new InputStreamReader(connectionInputStream);
					final BufferedReader rd = new BufferedReader(inputStreamReader)
					) {
				String line = rd.readLine();
				newUID = Long.parseLong(line);
			}
		} catch (Exception e) {
			logger.error("{}", e);
			return 0;
		}

		// did read go ok?
		if (newUID != 0) {
			settings.createNewUID(newUID);

			try {
				// Tell the robot it's new UID.
				connection.sendMessage("UID " + newUID);
			} catch(Exception e) {
				//FIXME deal with this rare and smelly problem.
			}
		}
		return newUID;
	}
}
