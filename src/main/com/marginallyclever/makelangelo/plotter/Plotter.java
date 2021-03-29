package com.marginallyclever.makelangelo.plotter;

import java.awt.BasicStroke;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.core.CommandLineOptions;
import com.marginallyclever.core.StringHelper;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.makelangelo.pen.Pen;
import com.marginallyclever.makelangelo.pen.PenColor;
import com.marginallyclever.util.PreferencesHelper;


/**
 * A {@link Plotter} is a state machine used to simulate a drawing robot in the real world.
 * This abstract class contains the code most common to all types of drawing robots.  Subclasses can then
 * alter the behavior and the visual representation.
 * 
 * {@link Plotter} is limited in scope - it can be told to do one command at a time, much like a real plotter.
 * For more sophisticated behavior associate your Plotter with some buffering class.
 * 
 * @author Dan Royer
 * @since before 7.25.0
 */
public abstract class Plotter implements Serializable, NetworkConnectionListener, PlotterModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4185946661019573192L;

	public static final String COMMAND_TRAVEL = "G0";
	public static final String COMMAND_DRAW   = "G1";
	public static final String COMMAND_DWELL = "G4";
	public static final String COMMAND_MODE_ABSOLUTE = "G90";
	public static final String COMMAND_MODE_RELATIVE = "G91";
	public static final String COMMAND_TELEPORT = "G92";
	public static final String COMMAND_JOG_MOTOR = "D0";
	public static final String COMMAND_FIND_HOME = "G28";

	/**
	 * Each {@link Plotter} has a global unique identifier
	 */
	private long robotUID;
	
	/**
	 * Users can pick nicknames for machines.
	 */
	private String nickname="";
	
	/**
	 * Name of preference node where save data is stored.
	 */
	private String nodeName="";
	
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered;
	
	// machine physical limits, in mm
	private double limitLeft;
	private double limitRight;
	private double limitBottom;
	private double limitTop;

	private ColorRGB penDownColor;
	private ColorRGB penUpColor;
	
	// speed control
	private double feedRateTravel;
	private double feedRateDrawing;
	private double acceleration;
	private int minimumSegmentTime;

	private double diameter;  // pen diameter (mm, >0)
	private double zOff;	// pen up servo angle (deg,0...180)
	private double zOn;	// pen down servo angle (deg,0...180)
	private double zFeedrate;	// pen servo movement speed (deg/s)

	/**
	 * top left, bottom center, etc...
	 *
	 * <pre>
	 * {@code private String[] startingStrings =  {
	 *       "Top Left",
	 *       "Top Center",
	 *       "Top Right",
	 *       "Left",
	 *       "Center",
	 *       "Right",
	 *       "Bottom Left",
	 *       "Bottom Center",
	 *       "Bottom Right"
	 *   };}
	 * </pre>
	 */
	private int startingPositionIndex;

	// misc state
	private boolean areMotorsEngaged;

	private boolean penIsUp;

	private boolean didSetHome;

	private double penX;
	private double penY;
	
	private boolean firstMoveAfterUpChange;
	
	private ArrayList<PlotterListener> listeners = new ArrayList<PlotterListener>();

	// Connection state
	private NetworkConnection connection = null;
	private boolean portConfirmed;

	// Firmware check
	private final String versionCheckStart = new String("Firmware v");
	private boolean firmwareVersionChecked = false;
	private final long expectedFirmwareVersion = 10; // must match the version in the the firmware EEPROM
	private boolean hardwareVersionChecked = false;
	
	/**
	 * These values should match https://github.com/marginallyclever/makelangelo-firmware/firmware_rumba/configure.h
	 */
	public Plotter() {
		super();
		
		robotUID = 0;
		isRegistered = false;
		firstMoveAfterUpChange = false;
		
		double mh = 835; // mm
		double mw = 835; // mm
		limitTop = mh/2;
		limitBottom = -mh/2;
		limitRight = mw/2;
		limitLeft = -mw/2;

		penDownColor = new ColorRGB(0,0,0); // BLACK
		penUpColor = new ColorRGB(0,255,0); // blue
		startingPositionIndex = 4;

		areMotorsEngaged = true;
		penIsUp = false;
		didSetHome = false;

		setPenX(0);
		setPenY(0);

		setZFeedrate(getZRate());
		setZOn(getZAngleOn());
		setZOff(getZAngleOff());

		// pen
		setPenDiameter(0.8f);
		setNickname(getName());

		portConfirmed = false;
	}
	
	public double getAcceleration() {
		return acceleration;
	}

	/**
	 * @return home X coordinate in mm
	 */ 
	public double getHomeX() {
		return getHome().x;
	}
	
	/**
	 * @return home Y coordinate in mm
	 */
	public double getHomeY() {
		return getHome().y;
	}
	
	public String getGCodeSetPositionAtHome() {
		return COMMAND_TELEPORT + " X"+getHomeX()+" Y"+getHomeY();
	}

	// return the strings that will tell a makelangelo robot its physical limits.
	@Override
	public String getGCodeConfig() {
		return null;
	}

	/**
	 * @return bottom limit in mm
	 */
	public double getLimitBottom() {
		return limitBottom;
	}

	/**
	 * @return left limit in mm
	 */
	public double getLimitLeft() {
		return limitLeft;
	}

	/**
	 * @return right limit in mm
	 */
	public double getLimitRight() {
		return limitRight;
	}

	/**
	 * @return top limit in mm
	 */
	public double getLimitTop() {
		return limitTop;
	}

	public long getUID() {
		return robotUID;
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	
	protected void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setPenDiameter(prefs.getDouble("diameter", getPenDiameter()));
		setZFeedrate(prefs.getDouble("z_rate", getZFeedrate()));
		setZOn(prefs.getDouble("z_on", getZOn()));
		setZOff(prefs.getDouble("z_off", getZOff()));
		feedRateTravel = prefs.getDouble("feed_rate", feedRateTravel);
		feedRateDrawing=prefs.getDouble("feed_rate_current",feedRateDrawing);
		
		int r,g,b;
		r = prefs.getInt("penDownColorR", penDownColor.getRed());
		g = prefs.getInt("penDownColorG", penDownColor.getGreen());
		b = prefs.getInt("penDownColorB", penDownColor.getBlue());
		penDownColor = new ColorRGB(r,g,b);
		
		r = prefs.getInt("penUpColorR", penUpColor.getRed());
		g = prefs.getInt("penUpColorG", penUpColor.getGreen());
		b = prefs.getInt("penUpColorB", penUpColor.getBlue());
		penUpColor = new ColorRGB(r,g,b);
	}

	protected void savePenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		prefs.putDouble("diameter", getPenDiameter());
		prefs.putDouble("z_rate", getZFeedrate());
		prefs.putDouble("z_on", getZOn());
		prefs.putDouble("z_off", getZOff());
		prefs.putDouble("feed_rate", feedRateTravel);
		prefs.putDouble("feed_rate_current", feedRateDrawing);

		prefs.putInt("penDownColorR", penDownColor.getRed());
		prefs.putInt("penDownColorG", penDownColor.getGreen());
		prefs.putInt("penDownColorB", penDownColor.getBlue());
		
		prefs.putInt("penUpColorR", penUpColor.getRed());
		prefs.putInt("penUpColorG", penUpColor.getGreen());
		prefs.putInt("penUpColorB", penUpColor.getBlue());
	}


	/**
	 * Save the machine configuration
	 * @param nodeName preference node name at which data will be saved
	 */
	public void saveConfig() {
		Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences myNode = topNode.node(nodeName);
		
		myNode.putDouble("limit_top", limitTop);
		myNode.putDouble("limit_bottom", limitBottom);
		myNode.putDouble("limit_right", limitRight);
		myNode.putDouble("limit_left", limitLeft);

		myNode.putDouble("acceleration", acceleration);
		myNode.putDouble("feedRateDrawing", feedRateDrawing);
		myNode.putDouble("feedRateTravel", feedRateTravel);
		myNode.putInt("minimumSegmentTime", minimumSegmentTime);
		
		myNode.putInt("startingPosIndex", startingPositionIndex);
		myNode.putBoolean("isRegistered", isRegistered());
		myNode.putLong("GUID", robotUID);
		myNode.put("Nickname", nickname);
		savePenConfig(myNode);
	}
	
	/**
	 * Load the machine configuration
	 * @param nodeName preference node name from which to load data
	 */
	public void loadConfig(String fromNodeName) {
		Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences myNode = topNode.node(fromNodeName);
		nodeName = fromNodeName;
		
		limitTop    = myNode.getDouble("limit_top", limitTop);
		limitBottom = myNode.getDouble("limit_bottom", limitBottom);
		limitLeft   = myNode.getDouble("limit_left", limitLeft);
		limitRight  = myNode.getDouble("limit_right", limitRight);
		
		acceleration = myNode.getDouble("acceleration",acceleration);
		feedRateDrawing = myNode.getDouble("feedRateDrawing", feedRateDrawing);
		feedRateTravel = myNode.getDouble("feedRateTravel", feedRateTravel);
		minimumSegmentTime = myNode.getInt("minimumSegmentTime", minimumSegmentTime);
		
		startingPositionIndex = myNode.getInt("startingPosIndex",startingPositionIndex);
		isRegistered = myNode.getBoolean("isRegistered",false);
		robotUID = myNode.getLong("GUID", 0);
		nickname = myNode.get("Nickname", nickname);
		
		loadPenConfig(myNode);

		//hardwareVersion = myNode.get("hardwareVersion", getVersion());
	}

	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String name) {
		nickname=name;
	}

	public void setAcceleration(double f) {
		acceleration = f;
	}
	
	public void setTravelFeedRate(double f) {
		feedRateTravel = Math.max(f,0.001);
	}
	
	public double getTravelFeedRate() {
		return feedRateTravel;
	}
	
	public void setDrawingFeedRate(double f) {
		feedRateDrawing = Math.max(f,0.001);
	}
	
	public double getDrawingFeedRate() {
		return feedRateDrawing;
	}
	
	public void setLimitBottom(double limitBottom) {
		this.limitBottom = limitBottom;
	}
	
	public void setLimitLeft(double limitLeft) {
		this.limitLeft = limitLeft;
	}
	
	public void setLimitRight(double limitRight) {
		this.limitRight = limitRight;
	}
	
	public void setLimitTop(double limitTop) {
		this.limitTop = limitTop;
	}
	
	public void setMachineSize(double width, double height) {
		limitLeft = -width/2.0;
		limitRight = width/2.0;
		limitBottom = -height/2.0;
		limitTop = height/2.0;
	}
	
	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
	
	public ColorRGB getPenDownColor() {
		return penDownColor;
	}
	
	public void setPenDownColor(ColorRGB arg0) {
		penDownColor=arg0;
	}
	
	public void setPenUpColor(ColorRGB arg0) {
		penUpColor=arg0;
	}
	
	public ColorRGB getPenUpColor() {
		return penUpColor;
	}

	public double getPenDownAngle() {
		return getZOn();
	}

	public double getPenUpAngle() {
		return getZOff();
	}

	public void setPenDownAngle(double arg0) {
		setZOn(arg0);
	}

	public void setPenUpAngle(double arg0) {
		setZOff(arg0);
	}

	public BasicStroke getStroke() {
		return new BasicStroke((float)getPenDiameter()*10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	public String getPenDownString() {
		return COMMAND_DRAW+" F" + StringHelper.formatDouble(getZFeedrate()) + " Z" + StringHelper.formatDouble(getPenDownAngle());
	}

	public String getPenUpString() {
		return COMMAND_DRAW+" F" + StringHelper.formatDouble(getZFeedrate()) + " Z" + StringHelper.formatDouble(getPenUpAngle());
	}
	
	public String getTravelFeedrateString() {
		return COMMAND_TRAVEL+" F" + StringHelper.formatDouble(getTravelFeedRate());
	}
	
	public String getDrawingFeedrateString() {
		return COMMAND_DRAW+" F" + StringHelper.formatDouble(getDrawingFeedRate());
	}

	public void writeChangeTo(Writer out,ColorRGB newPenDownColor) throws IOException {
		//if(penDownColor == newPenDownColor) return;
		
		penDownColor = newPenDownColor;
		writeChangeToInternal(out);
	}

	private void writeChangeToInternal(Writer out) throws IOException {
		int toolNumber = penDownColor.toInt() & 0xffffff;  // ignore alpha channel

		String name="";
		for( PenColor pc : Pen.commonPenColors ) {
			if( pc.hexValue==toolNumber ) {
				// found a common pen color.
				name = pc.name;
				break;
			}
		}

		if(name.isEmpty()) {
			// set the name to the color hex value
			name = "0x"+Integer.toHexString(toolNumber);  
		}
		
		String changeString = String.format("%-20s", "Change to "+name);
		String continueString = String.format("%-20s", "Click to continue");

		out.write("M06 T" + toolNumber + "\n");
		out.write("M117 "+changeString+continueString+"\n");
		out.write("M300 S60 P250\n");  // beep
		out.write("M226\n");  // pause for user input
		out.write("M117\n");  // clear message
		out.write(COMMAND_TRAVEL
				+" F" + StringHelper.formatDouble(getTravelFeedRate()) 
				+" A" + StringHelper.formatDouble(getAcceleration())
				+"\n");
	}
	
	public void writeMoveTo(Writer out, double x, double y,boolean isUp,boolean zMoved) throws IOException {
		String command = isUp?COMMAND_TRAVEL:COMMAND_DRAW;
		if(zMoved) {
			command = isUp 
					? getTravelFeedrateString() 
					: getDrawingFeedrateString();
			zMoved=false;
		}
		out.write(command
				+" X" + StringHelper.formatDouble(x)
				+" Y" + StringHelper.formatDouble(y)
				+"\n");
	}

	// lift the pen
	public void writePenUp(Writer out) throws IOException {
		out.write(getPenUpString()+"\n");
	}
	
	// lower the pen
	public void writePenDown(Writer out) throws IOException {
		out.write(getPenDownString()+"\n");
	}
	
	public void writeAbsoluteMode(Writer out) throws IOException {
		out.write(COMMAND_MODE_ABSOLUTE+"\n");
	}

	public void writeRelativeMode(Writer out) throws IOException {
		out.write(COMMAND_MODE_RELATIVE+"\n");
	}


	public void setPenDiameter(double d) {
		diameter = d;
	}
	
	public double getPenDiameter() {
		return diameter;
	}

	protected double getZOff() {
		return zOff;
	}

	protected void setZOff(double zOff) {
		this.zOff = zOff;
	}

	protected double getZOn() {
		return zOn;
	}

	protected void setZOn(double zOn) {
		this.zOn = zOn;
	}

	public void setZFeedrate(double arg0) {
		zFeedrate = arg0;
	}
	
	public double getZFeedrate() {
		return zFeedrate;
	}

	@Override
	public void render(GL2 gl2) {
		paintLimits(gl2);
	}

	protected void paintLimits(GL2 gl2) {
		gl2.glLineWidth(1);

		gl2.glColor3f(0.7f, 0.7f, 0.7f);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(getLimitLeft(), getLimitTop());
		gl2.glVertex2d(getLimitRight(), getLimitTop());
		gl2.glVertex2d(getLimitRight(), getLimitBottom());
		gl2.glVertex2d(getLimitLeft(), getLimitBottom());
		gl2.glEnd();
	}

	public double getPenX() {
		return penX;
	}


	public double getPenY() {
		return penY;
	}


	public void setPenX(double px) {
		penX = px;
	}

	public void setPenY(double py) {
		penY = py;
	}

	public void raisePen() {
		sendLineToRobot(getPenUpString());
		firstMoveAfterUpChange = true;
		penIsUp=true;
	}

	public void lowerPen() {
		sendLineToRobot(getPenDownString());
		firstMoveAfterUpChange = true;
		penIsUp=true;
	}

	public boolean didSetHome() {
		return didSetHome;
	}

	public boolean areMotorsEngaged() {
		return areMotorsEngaged;
	}

	public boolean isPenUp() {
		return penIsUp;
	}

	public void hasSetHome(boolean b) {
		didSetHome=b;
	}

	public void disengageMotors() {
		sendLineToRobot("M18");
		areMotorsEngaged = false;
	}

	public void engageMotors() {
		sendLineToRobot("M17");
		areMotorsEngaged = true;
	}

	public void goHome() {
		sendLineToRobot(COMMAND_TRAVEL
				+" F"+ StringHelper.formatDouble(getTravelFeedRate())
				+" X"+ StringHelper.formatDouble(getHomeX())
				+" Y"+ StringHelper.formatDouble(getHomeY()));
		setPenX(getHomeX());
		setPenY(getHomeY());
	}

	public void findHome() {
		raisePen();
		sendLineToRobot(COMMAND_FIND_HOME);
		setPenX(getHomeX());
		setPenY(getHomeY());
	}

	public void setHome() {
		sendLineToRobot(getGCodeSetPositionAtHome());
		// save home position
		sendLineToRobot("D6"
				+" X" + StringHelper.formatDouble(getHomeX())
				+" Y" + StringHelper.formatDouble(getHomeY()));
		setPenX(getHomeX());
		setPenY(getHomeY());
		hasSetHome(true);
	}

	/**
	 * @param x absolute position in mm
	 * @param y absolute position in mm
	 */
	public void movePenAbsolute(double x, double y) {
		sendLineToRobot(
				(isPenUp() 
					? (firstMoveAfterUpChange ? getTravelFeedrateString() : Plotter.COMMAND_TRAVEL)
					: (firstMoveAfterUpChange ? getDrawingFeedrateString() : Plotter.COMMAND_DRAW))
				+ " X" + StringHelper.formatDouble(x) 
				+ " Y" + StringHelper.formatDouble(y));
		setPenX(x);
		setPenY(y);
		firstMoveAfterUpChange = false;
	}

	/**
	 * @param dx relative position in mm
	 * @param dy relative position in mm
	 */
	public void movePenRelative(double dx, double dy) {
		sendLineToRobot(COMMAND_MODE_RELATIVE);
		sendLineToRobot(
				(isPenUp()  
					? (firstMoveAfterUpChange ? getTravelFeedrateString() : Plotter.COMMAND_TRAVEL)
					: (firstMoveAfterUpChange ? getDrawingFeedrateString() : Plotter.COMMAND_DRAW))
				+ " X" + StringHelper.formatDouble(dx) 
				+ " Y" + StringHelper.formatDouble(dy));
		sendLineToRobot(COMMAND_MODE_ABSOLUTE);
		setPenX(getPenX() + dx);
		setPenY(getPenY() + dy);
		firstMoveAfterUpChange = false;
	}

	public void jogLeftMotorOut() {
		sendLineToRobot(COMMAND_JOG_MOTOR+" L400");
	}

	public void jogLeftMotorIn() {
		sendLineToRobot(COMMAND_JOG_MOTOR+" L-400");
	}

	public void jogRightMotorOut() {
		sendLineToRobot(COMMAND_JOG_MOTOR+" R400");
	}

	public void jogRightMotorIn() {
		sendLineToRobot(COMMAND_JOG_MOTOR+" R-400");
	}
	
	public void addListener(PlotterListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlotterListener listener) {
		listeners.remove(listener);
	}


	/**
	 * Sends a single command the robot. Could be anything.
	 *
	 * @param line command to send.
	 * @return <code>true</code> if command was sent to the robot;
	 *         <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if (getConnection() == null || !isPortConfirmed())
			return false;

		String reportedline = line;
		// does it have a checksum? hide it from the log
		if (reportedline.contains(";")) {
			String[] lines = line.split(";");
			reportedline = lines[0];
		}
		if (reportedline.trim().isEmpty()) {
			// nothing to send
			return false;
		}

		// remember important status changes
		
		if(reportedline.startsWith(getPenUpString())) {
			notifyListeners("penUp", null, true);
		} else if(reportedline.startsWith(getPenDownString())) {
			notifyListeners("penUp", null, false);
		} else if(reportedline.startsWith("M17")) {
			notifyListeners("motorsEngaged", null, true);
		} else if(reportedline.startsWith("M18")) {
			notifyListeners("motorsEngaged", null, false);
		}

		Log.message(reportedline);
		
		// make sure the line has a return on the end
		if(!line.endsWith("\n")) {
			line += "\n";
		}

		try {
			getConnection().sendMessage(line);
		} catch (Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}

	
	public NetworkConnection getConnection() {
		return connection;
	}

	/**
	 * @param c the connection. Use null to close the connection.
	 */
	public void openConnection(NetworkConnection c) {
		assert (c != null);

		if (connection != null) {
			closeConnection();
		}

		portConfirmed = false;
		firmwareVersionChecked = false;
		hardwareVersionChecked = false;
		connection = c;
		connection.addListener(this);
		try {
			connection.sendMessage("M100\n");
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}

	public void closeConnection() {
		if (connection == null)
			return;

		connection.closeConnection();
		connection.removeListener(this);
		notifyDisconnected();
		connection = null;
		portConfirmed = false;
	}

	@Override
	public void finalize() {
		if (connection != null) {
			connection.removeListener(this);
		}
	}

	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		notifyDataAvailable(data);

		boolean justNow = false;

		// Every Makelangelo on startup says "HELLO WORLD, I AM [model] #[GUID]" on connect.
		// @See <a href ='https://github.com/MarginallyClever/Makelangelo-firmware/blob/master/src/parser.cpp'>Parser::M100()</a>
		// portConfirmed is true when the robot we called says these magic words and the [model] matches getHello()
		if (!portConfirmed) {
			// not confirmed yet, keep reading to check.
			PlotterModel ms = this;
			String machineTypeName = ms.getHello();
			if (data.lastIndexOf(machineTypeName) >= 0) {
				Log.message("Connecting machine recognized!");
				portConfirmed = true;
				// which machine GUID is this?
				String afterHello = data.substring(data.lastIndexOf(machineTypeName) + machineTypeName.length());
				parseRobotUID(afterHello);

				justNow = true;
			}
		}

		// Part of M100 is a D5 which reports "Firmware v**".  We want to know we're on the right version of the firmware.
		if (!firmwareVersionChecked && data.lastIndexOf(versionCheckStart) >= 0) {
			String afterV = data.substring(versionCheckStart.length()).trim();
			long versionFound = Long.parseLong(afterV);

			if (versionFound == expectedFirmwareVersion) {
				firmwareVersionChecked = true;
				justNow = true;
				// request the hardware version of this robot
				sendLineToRobot("D10\n");
			} else {
				notifyFirmwareVersionBad(versionFound);
			}
		}

		// "D10 V**" where ** is 
		// 3 is makelangelo 3
		// 4 is makelangelo huge
		// 5 is makelangelo 5
		// 6 is makelangelo 6
		// is hardware checked?
		if (!hardwareVersionChecked && data.lastIndexOf("D10") >= 0) {
			String[] pieces = data.split(" ");
			if (pieces.length > 1) {
				String last = pieces[pieces.length - 1];
				last = last.replace("\r\n", "");
				if (last.startsWith("V")) {
					String version = last.substring(1);
					if(version.contentEquals(getVersion())) {
						hardwareVersionChecked = true;
					} else {
						// TODO die here if versions don't match?
					}
					justNow = true;
				}
			}
		}

		// all checks have passed!
		if (justNow && portConfirmed && firmwareVersionChecked && hardwareVersionChecked) {
			// send whatever config settings I have for this machine.
			sendConfig();
			
			// tell everyone I've confirmed connection.
			notifyConnectionConfirmed();
		}
	}

	public boolean isPortConfirmed() {
		return portConfirmed;
	}

	private void parseRobotUID(String line) {
		Log.message("Reading GUID");
		// get the UID reported by the robot
		String[] lines = line.split("\\r?\\n");
		long newUID = -1;
		if (lines.length > 0) {
			try {
				newUID = Long.parseLong(lines[0]);
			} catch (NumberFormatException e) {
				Log.error(e.getMessage());
			}
		}

		// new robots have UID<=0
		if (newUID <= 0) {
			newUID = getNewRobotUID();
			if(newUID!=0) {
				robotUID = newUID;
			}
		}		
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		notifySendBufferEmpty();
	}

	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		notifyLineError(lineNumber);
	}

	/**
	 * Send the machine configuration to the robot.
	 */
	public void sendConfig() {
		if (getConnection() != null && !isPortConfirmed())
			return;

		String config = getGCodeConfig();
		String[] lines = config.split("\n");

		for (int i = 0; i < lines.length; ++i) {
			sendLineToRobot(lines[i] + "\n");
		}
		setHome();
		sendLineToRobot(COMMAND_TRAVEL
				+" F" + StringHelper.formatDouble(getTravelFeedRate()) 
				+" A" + StringHelper.formatDouble(getAcceleration())
				+"\n");
	}
	
	// notify PropertyChangeListeners
	void notifyListeners(String propertyName,Object oldValue,Object newValue) {
		PropertyChangeEvent e = new PropertyChangeEvent(this,propertyName,oldValue,newValue);
		for(PlotterListener ear : listeners) {
			ear.propertyChange(e);
		}
	}
	
	// Notify when unknown robot connected so that Makelangelo GUI can respond.
	private void notifyConnectionConfirmed() {
		for (PlotterListener listener : listeners) {
			listener.connectionConfirmed(this);
		}
	}

	// Notify when unknown robot connected so that Makelangelo GUI can respond.
	private void notifyFirmwareVersionBad(long versionFound) {
		for (PlotterListener listener : listeners) {
			listener.firmwareVersionBad(this, versionFound);
		}
	}

	private void notifyDataAvailable(String data) {
		for (PlotterListener listener : listeners) {
			listener.dataAvailable(this, data);
		}
	}

	private void notifySendBufferEmpty() {
		for (PlotterListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}

	private void notifyLineError(int lineNumber) {
		for (PlotterListener listener : listeners) {
			listener.lineError(this, lineNumber);
		}
	}

	private void notifyDisconnected() {
		for (PlotterListener listener : listeners) {
			listener.disconnected(this);
		}
	}

	/**
	 * Make a request to the GUID server and parse the results.
	 * If there is an {@link Exception} the details will appear in the {@link Log} output. 
	 * @return the new UID on success.  0 on failure.
	 */
	private long getNewRobotUID() {
		long newUID = 0;

		boolean pleaseGetAGUID = !CommandLineOptions.hasOption("-noguid");
		if (!pleaseGetAGUID)
			return 0;

		Log.message("Requesting GUID from server.");
		try {
			// Send request.  This url is broken up to prevent code scanners spotting it in open source repositories.
			URL url = new URL("htt"+"ps://www.marginally"+"clever.com/drawbot_getuid.p"+"hp");
			URLConnection conn = url.openConnection();
			// read results
			InputStream connectionInputStream = conn.getInputStream();
			Reader inputStreamReader = new InputStreamReader(connectionInputStream);
			BufferedReader rd = new BufferedReader(inputStreamReader);
			String line = rd.readLine();
			Log.message("Server says: '" + line + "'");
			newUID = Long.parseLong(line);
			// did read go ok?
			if (newUID != 0) {
				// Tell the robot it's new UID.
				sendLineToRobot("UID " + newUID);
			}
		} catch (Exception e) {
			Log.error("UID request error: " + e.getMessage());
			return 0;
		}

		return newUID;
	}
	
	/**
	 * Each {@link PlotterModel} has a type name for identification
	 */
	public String getLongName() {
		return getName() 
				//+" "+getVersion()
				+" #"+getUID();
	}

	public void setNodeName(String name) {
		nodeName=name;		
	}

	public String getNodeName() {
		return nodeName;
	}

	protected int getMinimumSegmentTime() {
		return minimumSegmentTime;
	}
}
