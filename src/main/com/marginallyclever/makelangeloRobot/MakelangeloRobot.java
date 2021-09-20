package com.marginallyclever.makelangeloRobot;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.io.gcode.SaveGCode;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.DefaultTurtleRenderer;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleRenderer;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * {@link MakelangeloRobot} is the Controller for a physical robot, following a
 * Model-View-Controller design pattern.  It also contains non-persistent model data. 
 * {@link MakelangeloRobotPanel} is one of the Views. 
 * {@link MakelangeloRobotSettings} is the persistent Model data (machine configuration).
 * 
 * @author dan
 * @since 7.2.10
 */
public class MakelangeloRobot implements NetworkSessionListener, PreviewListener {	
	private MakelangeloRobotSettings settings = new MakelangeloRobotSettings();
	private NetworkSession connection = null;
	private RobotIdentityConfirmation ric = new RobotIdentityConfirmationAfterMarlin(this);
	
	// rendering stuff
	private TurtleRenderer turtleRenderer;
	private Turtle turtleToRender = new Turtle();
	private MakelangeloRobotDecorator decorator = null;

	// misc state
	private boolean areMotorsEngaged;
	private boolean isRunning;
	private boolean isPaused;
	private boolean penIsUp;
	private boolean penJustMoved;
	private boolean penIsUpBeforePause;
	private boolean didSetHome;
	private double penX;
	private double penY;

	// this list of gcode commands is stored separate from the Turtle.
	private ArrayList<String> gcodeCommands = new ArrayList<String>();
	// what line in drawingCommands is going to be sent next?
	protected int nextLineNumber;

	// Listeners which should be notified of a change to the percentage.
	private ArrayList<MakelangeloRobotEventListener> listeners = new ArrayList<MakelangeloRobotEventListener>();

	
	public MakelangeloRobot() {
		super();
		
		areMotorsEngaged = true;
		isRunning = false;
		isPaused = false;
		penIsUp = false;
		penJustMoved = false;
		penIsUpBeforePause = false;
		didSetHome = false;
		setPenX(0);
		setPenY(0);
		setNextLineNumber(0);

		ric.addRobotIdentityEventListener((evt)->{
			switch(evt.flag) {
				case RobotIdentityEvent.BAD_HARDWARE:
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.BAD_HARDWARE, this, evt.data));
					break;
				case RobotIdentityEvent.BAD_FIRMWARE:
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.BAD_HARDWARE, this, evt.data));
				case RobotIdentityEvent.IDENTITY_CONFIRMED:
					Log.message("Identity confirmed.");
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.CONNECTION_READY,this));
					break;
				default:
					Log.error("Unexpected RobotIdentityEvent "+evt.flag);
					break;
			}
		});
	}
	
	public NetworkSession getConnection() {
		return connection;
	}

	// @param c the connection.
	public void openConnection(NetworkSession c) {
		if (c == null) return;

		Log.message("Opening connection...");
		didSetHome = false;
		
		ric.reset();

		connection = c;
		connection.addListener(this);
		connection.addListener(ric);
		
		ric.start();
	}

	public void closeConnection() {
		if (this.connection == null)
			return;

		connection.closeConnection();
		connection.removeListener(this);
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.DISCONNECT,this));
		connection = null;
	}
	
	@Override
	public void finalize() {
		if(connection != null) connection.removeListener(this);
	}

	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) dataAvailable((NetworkSession)evt.getSource(),(String)evt.data);
		if(evt.flag == NetworkSessionEvent.TRANSPORT_ERROR) setNextLineNumber((int)evt.data);
		if(evt.flag == NetworkSessionEvent.SEND_BUFFER_EMPTY) sendFileCommand();
	}
	
	private void dataAvailable(NetworkSession arg0, String data) {
		if (data.endsWith("\n")) data = data.substring(0, data.length() - 1);
		Log.message("Recv: "+data);
	}
	
	public long findOrCreateUID(String line) {
		long newUID = -1;
		
		// try to get the UID in the line
		String[] lines = line.split("\\r?\\n");
		if (lines.length > 0) {
			try {
				newUID = Long.parseLong(lines[0]);
				Log.message("UID found: "+newUID);
			} catch (NumberFormatException e) {
				Log.error("UID parsing failed.");
				Log.error("line="+lines[0]);
				Log.error(e.getMessage());
			}
		}

		// new robots have UID<=0
		if(newUID <= 0) newUID = getNewRobotUID();
		
		return newUID;
	}

	public void addListener(MakelangeloRobotEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MakelangeloRobotEventListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(MakelangeloRobotEvent e) {
		for (MakelangeloRobotEventListener listener : listeners) listener.makelangeloRobotEvent(e);
	}

	// based on http://www.exampledepot.com/egs/java.net/Post.html
	private long getNewRobotUID() {
		long newUID = 0;

		boolean pleaseGetAGUID = !CommandLineOptions.hasOption("-noguid");
		if (!pleaseGetAGUID)
			return 0;

		Log.message("obtaining UID from server.");
		try {
			// Send request
			URL url = new URL("https://www.marginallyclever.com/drawbot_getuid.php");
			URLConnection conn = url.openConnection();
			// get results
			InputStream connectionInputStream = conn.getInputStream();
			Reader inputStreamReader = new InputStreamReader(connectionInputStream);
			BufferedReader rd = new BufferedReader(inputStreamReader);
			String line = rd.readLine();
			Log.message("Server says: '" + line + "'");
			newUID = Long.parseLong(line);
			// did read go ok?
			if (newUID != 0) {
				settings.createNewUID(newUID);

				try {
					// Tell the robot it's new UID.
					connection.sendMessage("UID " + newUID);
				} catch (Exception e) {
					// FIXME has this ever happened? Deal with it better?
					Log.error("UID to robot: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			Log.error("UID from server: " + e.getMessage());
			return 0;
		}

		return newUID;
	}

	public String generateChecksum(String line) {
		byte checksum = 0;

		for (int i = 0; i < line.length(); ++i) {
			checksum ^= line.charAt(i);
		}

		return "*" + Integer.toString(checksum);
	}

	// Send the machine configuration to the robot.
	public void sendConfig() {
		if(!ric.getIdentityConfirmed()) return;

		Log.message("Sending config.");
		String config = settings.getGCodeConfig();
		String[] lines = config.split("\n");
		for( String line : lines ) sendLineToRobot(line + "\n");
		setHome();
		sendLineToRobot("G0"
				+" F" + StringHelper.formatDouble(settings.getTravelFeedRate()) 
				+" A" + StringHelper.formatDouble(settings.getAcceleration())
				+"\n");
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
		
		// if we're drawing, raise the pen so it doesn't make an ink blot.
		penIsUpBeforePause = penIsUp;
		if(!penIsUp) raisePen();
	}
	
	public void unPause() {
		if(!isPaused) return;
		isPaused = false;
		
		if(!penIsUpBeforePause) lowerPen();
		sendFileCommand();
	}
	
	public void halt() {
		isRunning = false;
		isPaused = false;
		raisePen();
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.STOP,this));
	}

	public void setRunning() {
		isRunning = true;
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.START,this));
	}
	
	public void raisePen() {
		sendLineToRobot(settings.getPenUpString());
		rememberRaisedPen();
	}
	
	private void rememberRaisedPen() {
		penJustMoved = !penIsUp;
		penIsUp = true;
	}
	
	private void rememberLoweredPen() {
		penJustMoved = penIsUp;
		penIsUp = false;
	}
	
	public void lowerPen() {
		sendLineToRobot(settings.getPenDownString());
		rememberLoweredPen();
	}
	
	public void testPenAngle(double testAngle) {
		sendLineToRobot(MakelangeloRobotSettings.COMMAND_DRAW + " Z" + StringHelper.formatDouble(testAngle));
	}

	/**
	 * Removes comments, processes commands robot doesn't handle, add checksum information.
	 *
	 * @param line command to send
	 */
	private void sendLineWithNumberAndChecksum(String line, int lineNumber) {
		if(!ric.getIdentityConfirmed() || !isRunning()) return;

		line = "N" + lineNumber + " " + line;
		if(!line.endsWith(";")) line += ';';
		line += generateChecksum(line);
		sendLineToRobot(line);
	}
	
	// Take the next line from the file and send it to the robot, if permitted.
	private void sendFileCommand() {
		if(!ric.getIdentityConfirmed() || !isRunning() || isPaused()) return;
		
		// are there any more commands?
		if(nextLineNumber >= gcodeCommands.size()) {
			halt();
			SoundSystem.playDrawingFinishedSound();
		} else {
			String line = gcodeCommands.get(nextLineNumber);
			sendLineWithNumberAndChecksum(line, nextLineNumber);
			setNextLineNumber(nextLineNumber+1);
		}
	}
	
	private void setNextLineNumber(int count) {
		nextLineNumber=count;
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.PROGRESS_SOFAR, this, nextLineNumber));
	}
	
	// update the simulated position to match the real robot
	private void updateStateFromGCode(String line) {
		if (line.contains("G0") || line.contains("G1")) {
			double px = this.getPenX();
			double py = this.getPenY();

			String[] tokens = line.split(" ");
			for (String t : tokens) {
				if (t.startsWith("X")) px = Float.parseFloat(t.substring(1));
				if (t.startsWith("Y")) py = Float.parseFloat(t.substring(1));
			}
			this.setPenX(px);
			this.setPenY(py);
		}
		
		if(line.startsWith(settings.getPenUpString())) rememberRaisedPen();
		if(line.startsWith(settings.getPenDownString())) rememberLoweredPen();
		if(line.startsWith("M17")) rememberMotorsEngaged(true);
		if(line.startsWith("M18")) rememberMotorsEngaged(false);
	}
	
	private void rememberMotorsEngaged(boolean b) {
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.MOTORS_ENGAGED,this,b));
		areMotorsEngaged=b;
	}

	public void startAt(int lineNumber) {
		if(lineNumber>=gcodeCommands.size()) lineNumber = gcodeCommands.size();
		if(lineNumber<0) lineNumber=0;

		setNextLineNumber(lineNumber);
		sendLineNumber(lineNumber);
		setRunning();
		sendFileCommand();
	}
	
	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	@SuppressWarnings("unused")
	private void requestUserChangeTool(int toolNumber) {
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.TOOL_CHANGE, this, toolNumber));
	}

	/**
	 * Sends a single command the robot. Could be anything.
	 *
	 * @param line command to send.
	 * @return <code>true</code> if command was sent to the robot;
	 *         <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if(!ric.getPortConfirmed() || getConnection()==null ) return false;

		// does it have a checksum? hide it from the log
		String reportedLine = line;
		int n = reportedLine.indexOf(";");
		if(n>=0) reportedLine = reportedLine.substring(n);
		if(reportedLine.trim().isEmpty()) return false;
		Log.message("Send: "+reportedLine);

		updateStateFromGCode(reportedLine);
		
		// make sure the line has a return on the end
		if(!line.endsWith("\n")) line += "\n";

		try {
			getConnection().sendMessage(line);
		} catch (Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	public void setDrawFeedRate(float feedRate) {
		// remember it
		settings.setDrawFeedRate(feedRate);
		// get it again in case it was capped.
		feedRate = settings.getDrawFeedRate();
		// tell the robot
		sendLineToRobot("G0 F" + StringHelper.formatDouble(feedRate));
	}
	
	public void setTravelFeedRate(float feedRate) {
		// remember it
		settings.setTravelFeedRate(feedRate);
		// get it again in case it was capped.
		feedRate = settings.getTravelFeedRate();
		// tell the robot
		sendLineToRobot("G1 F" + StringHelper.formatDouble(feedRate));
	}
	
	/**
	 * @return travel or draw feed rate, depending on pen state.
	 */
	public double getCurrentFeedRate() {
		return penIsUp
				? settings.getTravelFeedRate()
				: settings.getDrawFeedRate();
	}

	public void goHome() {
		movePenAbsolute(settings.getHomeX(),settings.getHomeY());
		penX=settings.getHomeX();
		penY=settings.getHomeY();
	}
	
	public void findHome() {
		this.raisePen();
		sendLineToRobot("G28 XY");
		setPenX((float) settings.getHomeX());
		setPenY((float) settings.getHomeY());
	}
	
	public void setHome() {
		sendLineToRobot(settings.getGCodeTeleportToHomePosition());
		// save home position
		sendLineToRobot("D6"
						+" X" + StringHelper.formatDouble(settings.getHomeX())
						+" Y" + StringHelper.formatDouble(settings.getHomeY()));
		setPenX((float) settings.getHomeX());
		setPenY((float) settings.getHomeY());
		didSetHome = true;
	}
	
	public boolean didSetHome() {
		return didSetHome;
	}
	
	/**
	 * @param x absolute position in mm
	 * @param y absolute position in mm
	 */
	public void movePenAbsolute(double x, double y) {
		String go = (penJustMoved ? "" : getTravelOrMoveWithFeedrate());	
		sendLineToRobot(go
						+ " X" + StringHelper.formatDouble(x)
						+ " Y" + StringHelper.formatDouble(y));
		penX = x;
		penY = y;
		penJustMoved=true;
	}
	
	/**
	 * @param dx relative position in mm
	 * @param dy relative position in mm
	 */
	public void movePenRelative(float dx, float dy) {
		sendLineToRobot("G91"); // set relative mode
		penJustMoved=false;

		String go = (penJustMoved ? "" : getTravelOrMoveWithFeedrate());
		sendLineToRobot(go
						+ " X" + StringHelper.formatDouble(dx)
						+ " Y" + StringHelper.formatDouble(dy));
		
		sendLineToRobot("G90"); // return to absolute mode
		penJustMoved=false;
		penX += dx;
		penY += dy;
	}
	
	private String getTravelOrMoveWithFeedrate() {
		if(penIsUp) return MakelangeloRobotSettings.COMMAND_TRAVEL +" "+ settings.getTravelFeedrateString();
		else        return MakelangeloRobotSettings.COMMAND_DRAW   +" "+ settings.getDrawFeedrateString();
	}
	
	public boolean areMotorsEngaged() {
		return areMotorsEngaged;
	}
	
	public void movePenToEdgeLeft() {
		movePenAbsolute(settings.getPaperLeft(),penY);
	}

	public void movePenToEdgeRight() {
		movePenAbsolute(settings.getPaperRight(),penY);
	}
	
	public void movePenToEdgeTop() {
		movePenAbsolute(getPenX(),settings.getPaperTop());
	}
	
	public void movePenToEdgeBottom() {
		movePenAbsolute(getPenX(),settings.getPaperBottom());
	}
	
	public void disengageMotors() {
		sendLineToRobot("M18");
		areMotorsEngaged = false;
	}

	public void engageMotors() {
		sendLineToRobot("M17");
		areMotorsEngaged = true;
	}
	
	public void jogLeftMotorOut() {
		sendLineToRobot("D00 L400");
	}

	public void jogLeftMotorIn() {
		sendLineToRobot("D00 L-400");
	}

	public void jogRightMotorOut() {
		sendLineToRobot("D00 R400");
	}

	public void jogRightMotorIn() {
		sendLineToRobot("D00 R-400");
	}

	public void sendLineNumber(int newLineNumber) {
		sendLineToRobot("M110 N" + newLineNumber);
	}
	
	public MakelangeloRobotSettings getSettings() {
		return settings;
	}

	public int getGCodeCommandsCount() {
		return gcodeCommands.size();
	}
	
	public void setTurtle(Turtle turtle) {
		turtleToRender = turtle;
		
		try(final OutputStream fileOutputStream = new FileOutputStream("currentDrawing.ngc")) {
			SaveGCode exportForDrawing = new SaveGCode();
			exportForDrawing.save(fileOutputStream, this, null);

			gcodeCommands.clear();
			
			BufferedReader reader = new BufferedReader(new FileReader("currentDrawing.ngc"));
			String line;
			while((line = reader.readLine()) != null) {
				gcodeCommands.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.NEW_GCODE,this));
	}

	public Turtle getTurtle() {
		return turtleToRender;
	}

	public void setDecorator(MakelangeloRobotDecorator arg0) {
		decorator = arg0;
	}

	@Override
	public void render(GL2 gl2) {
		float[] lineWidthBuf = new float[1];
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		
		// outside physical limits
		paintLimits(gl2);
		paintPaper(gl2);
		paintMargins(gl2);
		
		// hardware features
		settings.getHardwareProperties().render(gl2, this);

		gl2.glLineWidth(lineWidthBuf[0]);
		
		if (decorator != null) {
			// filters can also draw WYSIWYG previews while converting.
			decorator.render(gl2);
		} else if (turtleToRender != null) {
			if(turtleRenderer==null) {
				turtleRenderer = new DefaultTurtleRenderer(gl2);
				//turtleRenderer = new BarberPoleTurtleRenderer(gl2);
			}
			if(turtleRenderer!=null) {
				turtleToRender.render(turtleRenderer);
			}
			
			//TODO
			//MakelangeloFirmwareVisualizer viz = new MakelangeloFirmwareVisualizer(); 
			//viz.render(gl2, turtleToRender, settings);
		}
	}
	
	
	public void setTurtleRenderer(TurtleRenderer r) {
		turtleRenderer = r;
	}
	
	
	public TurtleRenderer getTurtleRenderer() {
		return turtleRenderer;
	}
		
	private void paintLimits(GL2 gl2) {
		gl2.glLineWidth(1);

		gl2.glColor3f(0.7f, 0.7f, 0.7f);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitBottom());
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitBottom());
		gl2.glEnd();
	}

	private void paintPaper(GL2 gl2) {
		ColorRGB c = settings.getPaperColor();
		gl2.glColor3d(
				(double)c.getRed() / 255.0, 
				(double)c.getGreen() / 255.0, 
				(double)c.getBlue() / 255.0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperBottom());
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperBottom());
		gl2.glEnd();
	}
	
	private void paintMargins(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginBottom());
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginBottom());
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	// in mm
	public double getPenX() {
		return penX;
	}
	
	// in mm
	public void setPenX(double px) {
		this.penX = px;
	}

	// in mm
	public double getPenY() {
		return penY;
	}

	// in mm
	public void setPenY(double py) {
		this.penY = py;
	}

	public int findLastPenUpBefore(int startAtLine) {
		int total = gcodeCommands.size();
		if (total == 0)
			return 0;

		String toMatch = settings.getPenUpString();

		int x = startAtLine;
		if (x >= total)
			x = total - 1;

		toMatch = toMatch.trim();
		while (x > 1) {
			String line = gcodeCommands.get(x).trim();
			if (line.equals(toMatch)) {
				return x;
			}
			--x;
		}

		return x;
	}

	public boolean isPenIsUp() {
		return penIsUp;
	}

	public boolean getIdentityConfirmed() {
		return ric.getIdentityConfirmed();
	}
}
