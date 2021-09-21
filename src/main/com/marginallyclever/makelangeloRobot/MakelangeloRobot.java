package com.marginallyclever.makelangeloRobot;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
public class MakelangeloRobot implements NetworkSessionListener, PreviewListener, MarlinEventListener {	
	private MakelangeloRobotSettings settings = new MakelangeloRobotSettings();
	private MarlinFirmware2 marlin = new MarlinFirmware2();
	
	// rendering stuff
	private TurtleRenderer turtleRenderer;
	private Turtle turtleToRender = new Turtle();
	private MakelangeloRobotDecorator decorator = null;

	// misc state
	private boolean areMotorsEngaged;
	private boolean isRunning;
	private boolean penIsUp;
	private boolean penJustMoved;
	private boolean penIsUpBeforePause;
	private boolean didFindHome;
	private double penX;
	private double penY;

	// this list of gcode commands is stored separate from the Turtle.
	private ArrayList<String> gcodeCommands = new ArrayList<String>();
	// what line in drawingCommands is going to be sent next?
	protected int nextLineNumber;

	private ArrayList<MakelangeloRobotEventListener> listeners = new ArrayList<MakelangeloRobotEventListener>();

	
	public MakelangeloRobot() {
		super();
		
		areMotorsEngaged = true;
		isRunning = false;
		penIsUp = false;
		penJustMoved = false;
		penIsUpBeforePause = false;
		didFindHome = false;
		setPenX(0);
		setPenY(0);
		setLineNumber(0);

		marlin.addRobotIdentityEventListener((evt)->{
			switch(evt.flag) {
				case RobotIdentityEvent.BAD_HARDWARE:
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.BAD_HARDWARE, this, evt.data));
					break;
				case RobotIdentityEvent.BAD_FIRMWARE:
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.BAD_FIRMWARE, this, evt.data));
					break;
				case RobotIdentityEvent.IDENTITY_CONFIRMED:
					Log.message("Identity confirmed.");
					getSettings().setHardwareVersion(marlin.getVersion());
					notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.CONNECTION_READY,this));
					break;
				default:
					Log.error("Unexpected RobotIdentityEvent "+evt.flag);
					break;
			}
		});
	}
	
	// OBSERVER PATTERN

	public void addListener(MakelangeloRobotEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MakelangeloRobotEventListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(MakelangeloRobotEvent e) {
		for (MakelangeloRobotEventListener listener : listeners) listener.makelangeloRobotEvent(e);
	}

	// OBSERVER PATTERN ENDS
	

	public void setNetworkSession(NetworkSession c) {
		didFindHome = false;
		if(c != null) c.addListener(this);
		marlin.setNetworkSession(c);
	}

	private void closeConnection(NetworkSession connection) {
		if(connection == null) return;

		connection.removeListener(this);
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.DISCONNECT,this));
	}
	
	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.CONNECTION_CLOSED) {
			halt();
			closeConnection((NetworkSession)evt.getSource());
		}
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
				Log.error("UID parsing failed. line="+lines[0]+", error="+e.getLocalizedMessage());
			}
		}

		// new robots have UID<=0
		if(newUID <= 0) newUID = getNewRobotUID();
		
		return newUID;
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
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = rd.readLine();
			Log.message("Server says: '" + line + "'");
			newUID = Long.parseLong(line);
			if (newUID != 0) {
				settings.createNewUID(newUID);
				marlin.sendNewUID(newUID);
			}
			
		} catch (Exception e) {
			Log.error("UID from server: " + e.getMessage());
			return 0;
		}

		return newUID;
	}

	// Send the machine configuration to the robot.
	public void sendConfig() {
		if(!marlin.getIdentityConfirmed()) return;

		Log.message("Sending config.");
		String config = settings.getGCodeConfig();
		String [] lines = config.split("\n");
		for( String line : lines ) send(line + "\n");
		setHome();
	}

	public boolean isRunning() {
		return isRunning;
	}
		
	public void halt() {
		isRunning = false;
		penIsUpBeforePause = penIsUp;
		if(!penIsUp) raisePen();
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.STOP,this));
	}

	public void start() {
		isRunning = true;
		if(!penIsUpBeforePause) lowerPen();
		sendFileCommand();
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.START,this));
	}
	
	public void raisePen() {
		send(settings.getPenUpString());
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
		send(settings.getPenDownString());
		rememberLoweredPen();
	}
	
	public void testPenAngle(double testAngle) {
		send(MakelangeloRobotSettings.COMMAND_DRAW + " Z" + StringHelper.formatDouble(testAngle));
	}
	
	// Take the next line from the file and send it to the robot, if permitted.
	private void sendFileCommand() {
		if( !marlin.getIdentityConfirmed() || !isRunning() ) return;
		
		// are there any more commands?
		if(nextLineNumber >= gcodeCommands.size()) {
			halt();
			SoundSystem.playDrawingFinishedSound();
		} else {
			String line = gcodeCommands.get(nextLineNumber);
			marlin.sendLineWithNumberAndChecksum(line, nextLineNumber);
			setLineNumber(nextLineNumber+1);
		}
	}
	
	private void setLineNumber(int count) {
		nextLineNumber=count;
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.PROGRESS_SOFAR, this, nextLineNumber));
	}
	
	public void send(String line) {
		line = line.trim();
		if(line.isEmpty()) return;

		updateStateFromGCode(line);
		marlin.send(line);
	}
	
	// update the simulated position to match the real robot
	private void updateStateFromGCode(String line) {
		if (line.contains("G0") || line.contains("G1")) {
			double px = this.getPenX();
			double py = this.getPenY();

			int star=line.indexOf("*");
			if(star>=0) line = line.substring(0,star);

			int semiColon=line.indexOf(";");
			if(semiColon>=0) line = line.substring(0,semiColon);
			
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

		setLineNumber(lineNumber);
		sendLineNumber(lineNumber);
		start();
	}
	
	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	@SuppressWarnings("unused")
	private void requestUserChangeTool(int toolNumber) {
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.TOOL_CHANGE, this, toolNumber));
	}

	public void setDrawFeedRate(float feedRate) {
		// remember it
		settings.setDrawFeedRate(feedRate);
		// get it again in case it was capped.
		feedRate = settings.getDrawFeedRate();
		// tell the robot
		send("G0 F" + StringHelper.formatDouble(feedRate));
	}
	
	public void setTravelFeedRate(float feedRate) {
		// remember it
		settings.setTravelFeedRate(feedRate);
		// get it again in case it was capped.
		feedRate = settings.getTravelFeedRate();
		// tell the robot
		send("G1 F" + StringHelper.formatDouble(feedRate));
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
		send("G28 XY");
		setPenX((float) settings.getHomeX());
		setPenY((float) settings.getHomeY());
		notifyListeners(new MakelangeloRobotEvent(MakelangeloRobotEvent.HOME_FOUND,this));
		didFindHome = true;
	}
	
	public void setHome() {
		send(settings.getGCodeTeleportToHomePosition());
		// save home position
		send("D6"
						+" X" + StringHelper.formatDouble(settings.getHomeX())
						+" Y" + StringHelper.formatDouble(settings.getHomeY()));
		setPenX((float) settings.getHomeX());
		setPenY((float) settings.getHomeY());
	}
	
	public boolean didFindHome() {
		return didFindHome;
	}
	
	/**
	 * @param x position in mm
	 * @param y position in mm
	 */
	public void movePenAbsolute(double x, double y) {
		String go = (penJustMoved ? "" : getTravelOrMoveWithFeedrate());	
		
		send(go
			+ " X" + StringHelper.formatDouble(x)
			+ " Y" + StringHelper.formatDouble(y));
		
		penX = x;
		penY = y;
		penJustMoved=true;
	}
	
	/**
	 * @param dx distance in mm
	 * @param dy distance in mm
	 */
	public void movePenRelative(double dx, double dy) {
		penJustMoved=false;

		String go = (penJustMoved ? "" : getTravelOrMoveWithFeedrate());
		
		send("G91"); // set relative mode
		send(go
			+ " X" + StringHelper.formatDouble(dx)
			+ " Y" + StringHelper.formatDouble(dy));
		send("G90"); // return to absolute mode
		
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
		send("M18");
		areMotorsEngaged = false;
	}

	public void engageMotors() {
		send("M17");
		areMotorsEngaged = true;
	}
	
	@Deprecated
	public void jogLeftMotorOut() {
		send("D00 L400");
	}

	@Deprecated
	public void jogLeftMotorIn() {
		send("D00 L-400");
	}

	@Deprecated
	public void jogRightMotorOut() {
		send("D00 R400");
	}

	@Deprecated
	public void jogRightMotorIn() {
		send("D00 R-400");
	}

	private void sendLineNumber(int newLineNumber) {
		send("M110 N" + newLineNumber);
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
		return marlin.getIdentityConfirmed();
	}

	public void sendPenDown() {
		send(getSettings().getPenDownString());
	}

	@Override
	public void marlinEvent(MarlinEvent evt) {
		if(evt.flag == MarlinEvent.TRANSPORT_ERROR) setLineNumber((int)evt.data);
		if(evt.flag == MarlinEvent.SEND_BUFFER_EMPTY) sendFileCommand();
	}
}
