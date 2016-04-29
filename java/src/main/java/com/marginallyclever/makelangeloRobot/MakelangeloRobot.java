package com.marginallyclever.makelangeloRobot;

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
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;

/**
 * MakelangeloRobot is the Controller for a physical robot, following a Model-View-Controller design pattern.  It also contains non-persistent Model data.  
 * MakelangeloRobotPanel is one of the Views.
 * MakelangeloRobotSettings is the persistent Model data (machine configuration).
 * @author dan
 * @since 7.2.10
 *
 */
public class MakelangeloRobot implements MarginallyCleverConnectionReadyListener {
	// Constants
	final String robotTypeName = "DRAWBOT";
	final String hello = "HELLO WORLD! I AM " + robotTypeName + " #";

	static boolean please_get_a_guid=true;  // set to true when I'm building robots @ marginallyclever.com.
		
	// Settings go here
	public MakelangeloRobotSettings settings = null;
	
	// control panel
	private MakelangeloRobotPanel myPanel = null;
	
	// Current state goes here
	private MarginallyCleverConnection connection = null;
	private boolean portConfirmed = false;


	// Listeners which should be notified of a change to the percentage.
    private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();

    private boolean areMotorsEngaged = true;
	private boolean isRunning = false;
	private boolean isPaused = true;
	
	// current pen state
	private boolean penIsUp = false;
	private boolean penIsUpBeforePause = false;
	
	// current location
	private boolean hasSetHome;
	
	
	public MakelangeloRobot(Translator translator) {
		settings = new MakelangeloRobotSettings(translator, this);
		hasSetHome=false;
	}
	
	public MarginallyCleverConnection getConnection() {
		return connection;
	}

	public void setConnection(MarginallyCleverConnection c) {
		if( this.connection != null ) {
			this.connection.closeConnection();
			this.connection.removeListener(this);
		}
		
		if( this.connection != c ) {
			portConfirmed = false;
			hasSetHome = false;
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
		long newUID = 0;
		if (lines.length > 0) {
			try {
				newUID = Long.parseLong(lines[0]);
			} catch (NumberFormatException e) {
				Log.error( "UID parsing: "+e.getMessage() );
			}
		}

		// new robots have UID=0
		if (newUID == 0) {
			newUID = getNewRobotUID();
		}
		
		// load machine specific config
		settings.loadConfig(newUID);
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

		if(please_get_a_guid==false) {
			Log.error("Developers have made a stupid mistake.");
		} else {
			Log.message("obtaining UID from server.");
			try {
				// Send data
				URL url = new URL("https://www.marginallyclever.com/drawbot_getuid.php");
				URLConnection conn = url.openConnection();
				try (	final InputStream connectionInputStream = conn.getInputStream();
						final Reader inputStreamReader = new InputStreamReader(connectionInputStream);
						final BufferedReader rd = new BufferedReader(inputStreamReader)
						) {
					String line = rd.readLine();
					Log.message("Server says: '"+line+"'");
					newUID = Long.parseLong(line);
				} catch (Exception e) {
					Log.error( "UID from server: "+e.getMessage() );
					return 0;
				}
			} catch (Exception e) {
				Log.error( "UID from server: "+e.getMessage() );
				return 0;
			}
		}
		// did read go ok?
		if (newUID != 0) {
			settings.createNewUID(newUID);

			try {
				// Tell the robot it's new UID.
				connection.sendMessage("UID " + newUID);
			} catch(Exception e) {
				//FIXME deal with this rare and smelly problem.
				Log.error( "UID to robot: "+e.getMessage() );
			}
		}
		return newUID;
	}


	public String generateChecksum(String line) {
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
		if(isPaused) return;
		
		isPaused = true;
		// remember for later if the pen is down
		penIsUpBeforePause = penIsUp;
		// raise it if needed.
		raisePen();
	}

	public void unPause() {
		if(!isPaused) return;
		
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
	public void testPenAngle(String testAngle) {
		sendLineToRobot("G00 Z" + testAngle);
	}


	/**
	 * removes comments, processes commands robot doesn't handle, add checksum information.
	 *
	 * @param line command to send
	 */
	public void tweakAndSendLine(String line, int lineNumber, Translator translator) {
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

		if (line.length() > 3) {
			line = "N" + lineNumber + " " + line;
			line += generateChecksum(line);
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
	
	
	public void goHome() {
		sendLineToRobot("G00 X0 Y0");
	}
	
	
	public void setHome() {
		sendLineToRobot("G92 X0 Y0");
		hasSetHome=true;
	}
	
	
	public boolean hasSetHome() {
		return hasSetHome;
	}
	
	
	public void movePenAbsolute(float x,float y) {
		sendLineToRobot("G00"+
						" X" + x +
						" Y" + y);
	}
	
	public void movePenRelative(float dx,float dy) {
		sendLineToRobot("G91");  // set relative mode
		sendLineToRobot("G00"+
				" X" + dx +
				" Y" + dy);
		sendLineToRobot("G90");  // return to absolute mode
	}
	
	public boolean areMotorsEngaged() { return areMotorsEngaged; }
	
	public void movePenToEdgeLeft()   {		sendLineToRobot("G00 X" + settings.getPaperLeft()   * 10);	}
	public void movePenToEdgeRight()  {		sendLineToRobot("G00 X" + settings.getPaperRight()  * 10);	}
	public void movePenToEdgeTop()    {		sendLineToRobot("G00 Y" + settings.getPaperTop()    * 10);	}
	public void movePenToEdgeBottom() {		sendLineToRobot("G00 Y" + settings.getPaperBottom() * 10);	}
	
	public void disengageMotors() {		sendLineToRobot("M17");	areMotorsEngaged=false; }
	public void engageMotors()    {		sendLineToRobot("M18");	areMotorsEngaged=true; }
	
	public void jogLeftMotorOut()  {		sendLineToRobot("D00 L400");	}
	public void jogLeftMotorIn()   {		sendLineToRobot("D00 L-400");	}
	public void jogRightMotorOut() {		sendLineToRobot("D00 R400");	}
	public void jogRightMotorIn()  {		sendLineToRobot("D00 R-400");	}
		
	public void setLineNumber(int newLineNumber) {		sendLineToRobot("M110 N" + newLineNumber);	}
	

	public MakelangeloRobotPanel getControlPanel(Makelangelo gui) {
		myPanel = new MakelangeloRobotPanel(gui, this);
		
		return myPanel;
	}
}
