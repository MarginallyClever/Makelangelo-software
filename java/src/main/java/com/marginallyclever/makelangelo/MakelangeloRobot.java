package com.marginallyclever.makelangelo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;

/**
 * @author Admin
 * @since 7.2.10
 *
 */
public class MakelangeloRobot implements MarginallyCleverConnectionReadyListener {
	// Constants
	final String robotTypeName = "DRAWBOT";
	final String hello = "HELLO WORLD! I AM " + robotTypeName + " #";
		
	// Settings go here
	public MakelangeloRobotSettings settings = null;
	
	// Current state goes here
	private MarginallyCleverConnection connection = null;
	private boolean portConfirmed = false;


	// Listeners which should be notified of a change to the percentage.
    private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();

	// reading file
	private boolean isRunning = false;
	private boolean isPaused = true;
	
	// current pen state
	private boolean penIsUp = false;
	private boolean penIsUpBeforePause = false;
	
	
	public MakelangeloRobot(Translator translator) {
		settings = new MakelangeloRobotSettings(translator, this);
	}
	
	public MarginallyCleverConnection getConnection() {
		return connection;
	}

	public void setConnection(MarginallyCleverConnection c) {
		if( this.connection != null ) {
			this.connection.removeListener(this);
		}
		
		if( this.connection != c ) {
			portConfirmed = false;
		}
		
		this.connection = c;
		
		if( this.connection != null ) {
			this.connection.addListener(this);
		}
	}

	@Override
	public void finalize() {
		if( this.connection != null ) {
			this.connection.removeListener(this);
		}
	}
	
	@Override
	public void connectionReady(MarginallyCleverConnection arg0) {
		notifyConnectionReady();
	}

	@Override
	public void dataAvailable(MarginallyCleverConnection arg0, String data) {
		notifyDataAvailable(data);
		
		if (portConfirmed == true) return;
		if (data.lastIndexOf(hello) < 0) return;

		portConfirmed = true;
		// which machine is this?
		String after_hello = data.substring(data.lastIndexOf(hello) + hello.length());
		parseRobotUID(after_hello);
		// send whatever config settings I have for this machine.
		sendConfig();
		// tell everyone I've confirmed connection.
		notifyPortConfirmed();
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
				Log.error( e.getMessage() );
			}
		}

		// new robots have UID=0
		if (new_uid == 0) {
			new_uid = getNewRobotUID();
		}
		
		// load machine specific config
		settings.loadConfig(new_uid);
	}

	// Notify when unknown robot connected so that Makelangelo GUI can respond.
	private void notifyPortConfirmed() {
		for (MakelangeloRobotListener listener : listeners) {
			listener.portConfirmed(this);
		}
	}
	
	private void notifyDataAvailable(String data) {
		for(MakelangeloRobotListener listener : listeners) {
			listener.dataAvailable(this,data);
		}
	}
	
	private void notifyConnectionReady() {
		for(MakelangeloRobotListener listener : listeners) {
			listener.connectionReady(this);
		}
	}
	
	public void lineError(MarginallyCleverConnection arg0,int lineNumber) {
		notifyLineError(lineNumber);
	}
	
	private void notifyLineError(int lineNumber) {
		for(MakelangeloRobotListener listener : listeners) {
			listener.lineError(this,lineNumber);
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
			Log.error( e.getMessage() );
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


	protected String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + ((int) checksum);
	}


	/**
	 * Send the machine configuration to the robot.
	 * @author danroyer
	 */
	public void sendConfig() {
		if (getConnection() != null && !isPortConfirmed()) return;

		// Send  new configuration values to the robot.
		try {
			connection.sendMessage(settings.getConfigLine() + "\n");
			connection.sendMessage(settings.getBobbinLine() + "\n");
			connection.sendMessage("G0 F"+ settings.getFeedRate() + " A" + settings.getAcceleration() + "\n");
		} catch(Exception e) {}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void pause() {
		isPaused = true;
		// remember for later if the pen is down
		penIsUpBeforePause = penIsUp;
		// raise it if needed.
		raisePen();
	}

	public void unPause() {
		// if pen was down before pause, lower it
		if (!penIsUpBeforePause) {
			lowerPen();
		}
		
		isPaused = false;
	}
	
	public void setRunning(boolean running) {
		isRunning = running;
	}
	
	public void raisePen() {
		sendLineToRobot("G00 Z" + settings.getPenUpString());
	}
	public void lowerPen() {
		sendLineToRobot("G00 Z" + settings.getPenDownString());
	}


	/**
	 * removes comments, processes commands drawbot shouldn't have to handle.
	 *
	 * @param line command to send
	 */
	public void tweakAndSendLine(String line,Translator translator) {
		if (getConnection() == null || !isPortConfirmed() || !isRunning()) return;

		// tool change request?
		String[] tokens = line.split("(\\s|;)");

		// tool change?
		if (Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for (String token : tokens) {
				if (token.startsWith("T")) {
					changeToTool(token.substring(1),translator);
				}
			}
		}

		// send relevant part of line to the robot
		sendLineToRobot(line);
	}


	private void changeToTool(String changeToolString,Translator translator) {
		int i = Integer.decode(changeToolString);

		String[] toolNames = settings.getToolNames();

		if (i < 0 || i > toolNames.length) {
			Log.error( Translator.get("InvalidTool") + i );
			i = 0;
		}
		JOptionPane.showMessageDialog(null, Translator.get("ChangeToolPrefix") + toolNames[i] + Translator.get("ChangeToolPostfix"));
	}


	/**
	 * Sends a single command the robot.  Could be anything.
	 *
	 * @param line command to send.
	 * @return <code>true</code> if command was sent to the robot; <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if (getConnection() == null || !isPortConfirmed()) return false;

		if (line.trim().equals("")) return false;
		String reportedline = line;
		// does it have a checksum?  hide it in the log
		if (reportedline.contains(";")) {
			String[] lines = line.split(";");
			reportedline = lines[0];
		}
		if(reportedline.trim().equals("")) return false;

		// catch pen up/down status here
		if (line.contains("Z" + settings.getPenUpString())) {
			penIsUp=true;
		}
		if (line.contains("Z" + settings.getPenDownString())) {
			penIsUp=false;
		}

		Log.write("white", reportedline );
		line += "\n";

		// send unmodified line
		try {
			getConnection().sendMessage(line);
		} catch (Exception e) {
			Log.error( e.getMessage() );
			return false;
		}
		return true;
	}

	public void setFeedRate(double parsedFeedRate) {
		// remember it
		settings.setFeedRate(parsedFeedRate);
		// tell the robot
		sendLineToRobot("G00 F" + parsedFeedRate);
	}
}
