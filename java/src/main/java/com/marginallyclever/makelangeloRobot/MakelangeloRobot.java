package com.marginallyclever.makelangeloRobot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.GCodeFile;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
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
	private final String robotTypeName = "DRAWBOT";
	private final String hello = "HELLO WORLD! I AM " + robotTypeName + " #";

	// Firmware check
	private final String versionCheck = new String("Firmware v");
	private final long expectedFirmwareVersion = 5;
	private boolean firmwareVersionChecked = false;

	private DecimalFormat df;
	
	private MakelangeloRobotSettings settings = null;
	private MakelangeloRobotPanel myPanel = null;
	
	// Connection state
	private MarginallyCleverConnection connection = null;
	private boolean portConfirmed;

	// misc state
	private boolean areMotorsEngaged;
	private boolean isRunning;
	private boolean isPaused;
	private boolean penIsUp;
	private boolean penIsUpBeforePause;
	private boolean hasSetHome;
	public float gondolaX,gondolaY;

	// rendering stuff
	public boolean showPenUpMoves=false;
	private DrawPanelDecorator drawDecorator=null;
	private final float PEN_HOLDER_RADIUS=6; //cm

	// Listeners which should be notified of a change to the percentage.
    private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();

	public GCodeFile gCode;
	
	
	public MakelangeloRobot() {
		// set up number format
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		df = new DecimalFormat("#.###",otherSymbols);
		df.setGroupingUsed(false);
		
		settings = new MakelangeloRobotSettings(this);
		portConfirmed = false;
		areMotorsEngaged = true;
		isRunning = false;
		isPaused = false;
		penIsUp = false;
		penIsUpBeforePause = false;
		hasSetHome = false;
		gondolaX = 0;
		gondolaY = 0;
	}
	
	public MarginallyCleverConnection getConnection() {
		return connection;
	}

	/**
	 * TODO this is not great.  openConnection(id) and closeConnection() would be better.
	 * @param c the connection.  Use null to close the connection. 
	 */
	public void setConnection(MarginallyCleverConnection c) {
		if( this.connection != null ) {
			this.connection.closeConnection();
			this.connection.removeListener(this);
			notifyDisconnected();
		}
		
		if( this.connection != c ) {
			portConfirmed = false;
			hasSetHome = false;
			firmwareVersionChecked = false;
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
	public void sendBufferEmpty(MarginallyCleverConnection arg0) {
		sendFileCommand();
		
		notifyConnectionReady();
	}

	@Override
	public void dataAvailable(MarginallyCleverConnection arg0, String data) {
		notifyDataAvailable(data);
		
		boolean justNow = false;
		
		// is port confirmed?
		if (!portConfirmed && data.lastIndexOf(hello) >= 0) {
			portConfirmed = true;
			// which machine is this?
			String afterHello = data.substring(data.lastIndexOf(hello) + hello.length());
			parseRobotUID(afterHello);
			justNow=true;
		}
		
		if( !firmwareVersionChecked && data.lastIndexOf(versionCheck)>=0 ) {
			String afterV = data.substring(versionCheck.length()).trim();
			long versionFound = Long.parseLong(afterV);
			
			if( versionFound == expectedFirmwareVersion ) {
				firmwareVersionChecked=true;
				justNow=true;
			} else {
				notifyFirmwareBad(versionFound);
			}
		}
		
		if(justNow && firmwareVersionChecked && portConfirmed) {
			// send whatever config settings I have for this machine.
			sendConfig();
			
			if(myPanel!=null) myPanel.onConnect();
			
			// tell everyone I've confirmed connection.
			notifyPortConfirmed();
		}
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

	// Notify when unknown robot connected so that Makelangelo GUI can respond.
	private void notifyFirmwareBad(long versionFound) {
		for (MakelangeloRobotListener listener : listeners) {
			listener.firmwareBad(this,versionFound);
		}
	}
	
	private void notifyDataAvailable(String data) {
		for(MakelangeloRobotListener listener : listeners) {
			listener.dataAvailable(this,data);
		}
	}
	
	private void notifyConnectionReady() {
		for(MakelangeloRobotListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}
	
	public void lineError(MarginallyCleverConnection arg0,int lineNumber) {
        if(gCode!=null) {
    		gCode.setLinesProcessed(lineNumber);
        }
        
		notifyLineError(lineNumber);
	}
	
	private void notifyLineError(int lineNumber) {
		for(MakelangeloRobotListener listener : listeners) {
			listener.lineError(this,lineNumber);
		}
	}

	public void notifyDisconnected() {
		for(MakelangeloRobotListener listener : listeners) {
			listener.disconnected(this);
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

		boolean pleaseGetAGUID = !CommandLineOptions.hasOption("-noguid");
		if(pleaseGetAGUID) {
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

		return "*" + Integer.toString(checksum);
	}


	/**
	 * Send the machine configuration to the robot.
	 * @author danroyer
	 */
	public void sendConfig() {
		if (getConnection() != null && !isPortConfirmed()) return;

		// Send  new configuration values to the robot.
		try {
			sendLineToRobot(settings.getGCodeConfig() + "\n");
			sendLineToRobot(settings.getGCodePulleyDiameter() + "\n");
			setHome();
			sendLineToRobot("G0 F"+ df.format(settings.getFeedRate()) + " A" + df.format(settings.getAcceleration()) + "\n");
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
	
	public void halt() {
		isRunning = false;
		if(isPaused) isPaused=false;  // do not lower pen
		if(myPanel != null) myPanel.updateButtonAccess();
	}
	
	public void setRunning() {
		isRunning = true;
		if(myPanel != null) myPanel.statusBar.start();
		if(myPanel != null) myPanel.updateButtonAccess();  // disables all the manual driving buttons
	}
	
	public void raisePen() {
		sendLineToRobot(settings.getPenUpString());
	}
	
	public void lowerPen() {
		sendLineToRobot(settings.getPenDownString());
	}
	
	public void testPenAngle(double testAngle) {
		sendLineToRobot("G00 Z" + df.format(testAngle));
	}


	/**
	 * removes comments, processes commands robot doesn't handle, add checksum information.
	 *
	 * @param line command to send
	 */
	public void tweakAndSendLine(String line, int lineNumber) {
		if (getConnection() == null || !isPortConfirmed() || !isRunning()) return;

		// tool change request?
		String[] tokens = line.split("(\\s|;)");

		// tool change?
		if (Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			int toolNumber=0;
			boolean nextTokenIsColorName=false;
			String colorName ="";
			for (String token : tokens) {
				if (token.startsWith("T")) {
					toolNumber = Integer.decode(token.substring(1));
				}
				if (token.startsWith("//")) {
					nextTokenIsColorName=true;
				}
				if(nextTokenIsColorName) {
					nextTokenIsColorName=false;
					colorName = token;
				}
			}
			
			changeToTool(toolNumber,colorName);
		}

		// checksums for commands with a line number
		if (line.length() > 3) {
			line = "N" + lineNumber + " " + line;
			String checksum = generateChecksum(line); 
			line += checksum; 
		}
		
		// send relevant part of line to the robot
		sendLineToRobot(line);
	}


	/**
	 * Take the next line from the file and send it to the robot, if permitted.
	 */
	public void sendFileCommand() {
		if (isRunning() == false 
				|| isPaused() == true 
				|| gCode==null
				|| gCode.isFileOpened() == false 
				|| (getConnection() != null && isPortConfirmed() == false) )
			return;

		// are there any more commands?
		if( gCode.moreLinesAvailable() == false )  {
			// end of file
			halt();
			// bask in the glory
			int x = gCode.getLinesTotal();
			if(myPanel!=null) myPanel.statusBar.setProgress(x, x);
			
			SoundSystem.playDrawingFinishedSound();
		} else {
			int lineNumber = gCode.getLinesProcessed();
			String line = gCode.nextLine();
			tweakAndSendLine( line, lineNumber );
	
			if(myPanel!=null) myPanel.statusBar.setProgress(lineNumber, gCode.getLinesTotal());
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		}
	}

	public void startAt(int lineNumber) {
		if(gCode==null) return;
		
		int lineBefore = gCode.findLastPenUpBefore(lineNumber,getSettings().getPenUpString());
		gCode.setLinesProcessed(lineBefore);
		setLineNumber(gCode.getLinesProcessed());
		setRunning();
		sendFileCommand();
	}

	private void changeToTool(int newToolID,String colorName) {
		String[] toolNames = settings.getToolNames();

		if (newToolID < 0 || newToolID > toolNames.length) {
			Log.error( Translator.get("InvalidTool") + newToolID );
			newToolID = 0;
		}
		
		if(!colorName.trim().equals("")) {
			colorName+=" ";
		}
		
		JOptionPane.showMessageDialog(null, Translator.get("ChangeToolPrefix") + colorName + toolNames[newToolID] + Translator.get("ChangeToolPostfix"));
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
		if(reportedline.trim().isEmpty()) return false;

		// catch pen up/down status here
		if (line.equals(settings.getPenUpString())) {
			penIsUp=true;
		}
		if (line.equals(settings.getPenDownString())) {
			penIsUp=false;
		}
		if( line.equals("M17") ) {
			// engage motors
			myPanel.motorsHaveBeenEngaged();
		}
		if( line.equals("M18")) {
			// disengage motors
			myPanel.motorsHaveBeenDisengaged();
		}

		Log.write("white", line );
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
		sendLineToRobot("G00 F" + df.format(parsedFeedRate));
	}
	
	
	public void goHome() {
		sendLineToRobot("G00 X"+df.format(settings.getHomeX())+" Y"+df.format(settings.getHomeY()));
		gondolaX=(float)settings.getHomeX();
		gondolaY=(float)settings.getHomeY();
	}
	
	
	public void setHome() {
		sendLineToRobot(settings.getGCodeSetPositionAtHome());
		sendLineToRobot("D6 X"+df.format(settings.getHomeX())+" Y"+df.format(settings.getHomeY()));  // save home position
		hasSetHome=true;
		gondolaX=(float)settings.getHomeX();
		gondolaY=(float)settings.getHomeY();
	}
	
	
	public boolean hasSetHome() {
		return hasSetHome;
	}
	
	/**
	 * @param x absolute position in mm
	 * @param y absolute position in mm
	 */
	public void movePenAbsolute(float x,float y) {
		sendLineToRobot("G00 X" + df.format(x) + " Y" + df.format(y));
		gondolaX = x;
		gondolaY = y;
	}

	/**
	 * @param x relative position in mm
	 * @param y relative position in mm
	 */
	public void movePenRelative(float dx,float dy) {
		sendLineToRobot("G91");  // set relative mode
		sendLineToRobot("G00 X" + df.format(dx) + " Y" + df.format(dy));
		sendLineToRobot("G90");  // return to absolute mode
		gondolaX += dx;
		gondolaY += dy;
	}
	
	public boolean areMotorsEngaged() { return areMotorsEngaged; }
	
	public void movePenToEdgeLeft()   {		movePenAbsolute((float)settings.getPaperLeft()*10,gondolaY);	}
	public void movePenToEdgeRight()  {		movePenAbsolute((float)settings.getPaperRight()*10,gondolaY);	}
	public void movePenToEdgeTop()    {		movePenAbsolute(gondolaX,(float)settings.getPaperTop()   *10);  }
	public void movePenToEdgeBottom() {		movePenAbsolute(gondolaX,(float)settings.getPaperBottom()*10);  }
	
	public void disengageMotors() {		sendLineToRobot("M18");		areMotorsEngaged=false; }
	public void engageMotors()    {		sendLineToRobot("M17");		areMotorsEngaged=true; }
	
	public void jogLeftMotorOut()  {		sendLineToRobot("D00 L400");	}
	public void jogLeftMotorIn()   {		sendLineToRobot("D00 L-400");	}
	public void jogRightMotorOut() {		sendLineToRobot("D00 R400");	}
	public void jogRightMotorIn()  {		sendLineToRobot("D00 R-400");	}
		
	public void setLineNumber(int newLineNumber) {		sendLineToRobot("M110 N" + newLineNumber);	}
	

	public MakelangeloRobotSettings getSettings() {
		return settings;
	}
	
	public MakelangeloRobotPanel createControlPanel(Makelangelo gui) {
		myPanel = new MakelangeloRobotPanel(gui, this);
		return myPanel;
	}
	
	public MakelangeloRobotPanel getControlPanel() {		
		return myPanel;
	}


	public void setGCode(GCodeFile gcode) {
		gCode = gcode;
		if(gCode!=null) gCode.emptyNodeBuffer();
	}


	public void setDecorator(DrawPanelDecorator dd) {
		drawDecorator = dd;
		if(gCode!=null) gCode.emptyNodeBuffer();
	}
	
	
	public void render(GL2 gl2) {
		paintLimits(gl2);
		paintCalibrationPoint(gl2);
		paintMotors(gl2);
		paintControlBox(gl2);
		paintPenHolderAndCounterweights(gl2);

		if(drawDecorator!=null) {
			// filters can also draw WYSIWYG previews while converting.
			drawDecorator.render(gl2,settings);
			return;
		}

		if(gCode!=null) gCode.render(gl2,this);
	}

	// draw left motor, right motor
	private void paintMotors( GL2 gl2 ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();
		
		gl2.glColor3f(1,0.8f,0.5f);
		// left frame
		gl2.glPushMatrix();
		gl2.glTranslatef(-2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left-5f, top+5f);
		gl2.glVertex2d(left+5f, top+5f);
		gl2.glVertex2d(left+5f, top);
		gl2.glVertex2d(left   , top-5f);
		gl2.glVertex2d(left-5f, top-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		gl2.glTranslatef(2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(right+5f, top+5f);
		gl2.glVertex2d(right-5f, top+5f);
		gl2.glVertex2d(right-5f, top);
		gl2.glVertex2d(right   , top-5f);
		gl2.glVertex2d(right+5f, top-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// left motor
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left-4.2f, top+4.2f);
		gl2.glVertex2d(left     , top+4.2f);
		gl2.glVertex2d(left     , top);
		gl2.glVertex2d(left-4.2f, top);
		// right motor
		gl2.glVertex2d(right     , top+4.2f);
		gl2.glVertex2d(right+4.2f, top+4.2f);
		gl2.glVertex2d(right+4.2f, top);
		gl2.glVertex2d(right     , top);
		gl2.glEnd();
	}

	private void paintControlBox(GL2 gl2) {
		double cy = settings.getLimitTop();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy+2.1f, 0);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();
		
		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(left,-0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(left,-0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(left, 0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(left, 0.3);
		

		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(right, 0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(right, 0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(right,-0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(right,-0.3);
		gl2.glEnd();
		
		// UNO in v2
		// @TODO draw correct version based on settings or machine connected
		gl2.glColor3d(0,0,0.6);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-4, 3);
		gl2.glVertex2d(+4, 3);
		gl2.glVertex2d(+4, -3);
		gl2.glVertex2d(-4, -3);
		gl2.glEnd();

		gl2.glPopMatrix();
	}
	
	private void paintPenHolderAndCounterweights( GL2 gl2 ) {
		double dx,dy;
		double gx = gondolaX / 10;
		double gy = gondolaY / 10;
		
		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		
		double mw = right-left;
		double mh = top-settings.getLimitBottom();
		double suggested_length = Math.sqrt(mw*mw+mh*mh)+5;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggested_length - left_a)/2;

		dx = gx - right;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggested_length - right_a)/2;

		if(gx<left) return;
		if(gx>right) return;
		if(gy>top) return;
		if(gy<bottom) return;
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		
		// belt from motor to gondola left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx,gy);
		// belt from motor to gondola right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx,gy);
		
		float bottleCenter = 2.1f+0.75f;
		
		// belt from motor to counterweight left
		gl2.glVertex2d(left-bottleCenter-0.2, top);
		gl2.glVertex2d(left-bottleCenter-0.2, top-left_b);
		gl2.glVertex2d(left-bottleCenter+0.2, top);
		gl2.glVertex2d(left-bottleCenter+0.2, top-left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right+bottleCenter-0.2, top);
		gl2.glVertex2d(right+bottleCenter-0.2, top-right_b);
		gl2.glVertex2d(right+bottleCenter+0.2, top);
		gl2.glVertex2d(right+bottleCenter+0.2, top-right_b);
		gl2.glEnd();
		
		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(gx+Math.cos(f)*PEN_HOLDER_RADIUS,gy+Math.sin(f)*PEN_HOLDER_RADIUS);
		}
		gl2.glEnd();
		
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b-15);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b-15);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b-15);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b-15);
		gl2.glEnd();
		
		/*
		// bottom clearance arcs
		// right
		gl2.glColor3d(0.6, 0.6, 0.6);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		double w = machine.getSettings().getLimitRight() - machine.getSettings().getLimitLeft()+2.1;
		double h = machine.getSettings().getLimitTop() - machine.getSettings().getLimitBottom() + 2.1;
		r=(float)Math.sqrt(h*h + w*w); // circle radius
		gx = machine.getSettings().getLimitLeft() - 2.1;
		gy = machine.getSettings().getLimitTop() + 2.1;
		double start = (float)1.5*(float)Math.PI;
		double end = (2*Math.PI-Math.atan(h/w));
		double v;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		
		// left
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gx = machine.getSettings().getLimitRight() + 2.1;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		*/
	}



	/**
	 * draw the machine edges and paper edges
	 *
	 * @param gl2
	 */
	private void paintLimits(GL2 gl2) {
		gl2.glColor3f(0.7f, 0.7f, 0.7f);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitBottom());
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitBottom());
		gl2.glEnd();
		
		if (!isPortConfirmed()) {
			gl2.glColor3f(194.0f / 255.0f, 133.0f / 255.0f, 71.0f / 255.0f);
			gl2.glColor3f(1, 1, 1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperTop());
			gl2.glVertex2d(settings.getPaperRight(), settings.getPaperTop());
			gl2.glVertex2d(settings.getPaperRight(), settings.getPaperBottom());
			gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperBottom());
			gl2.glEnd();
		} else {
			gl2.glColor3f(194.0f / 255.0f, 133.0f / 255.0f, 71.0f / 255.0f);
			gl2.glColor3f(1, 1, 1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperTop());
			gl2.glVertex2d(settings.getPaperRight(), settings.getPaperTop());
			gl2.glVertex2d(settings.getPaperRight(), settings.getPaperBottom());
			gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperBottom());
			gl2.glEnd();
		}
		// margin settings
		gl2.glPushMatrix();
		gl2.glColor3f(0.9f,0.9f,0.9f);
		gl2.glLineWidth(1);
		gl2.glScaled(settings.getPaperMargin(),settings.getPaperMargin(),1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperBottom());
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperBottom());
		gl2.glEnd();
		gl2.glPopMatrix();
	}


	/**
	 * draw calibration point
	 * @param gl2
	 */
	private void paintCalibrationPoint(GL2 gl2) {
		gl2.glColor3f(0.8f,0.8f,0.8f);
		gl2.glPushMatrix();
		gl2.glTranslated(settings.getHomeX(), settings.getHomeY(), 0);

		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(	Math.cos(f)*(PEN_HOLDER_RADIUS+0.1),
							Math.sin(f)*(PEN_HOLDER_RADIUS+0.1)
							);
		}
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f,0.0f);
		gl2.glVertex2f( 0.25f,0.0f);
		gl2.glVertex2f(0.0f,-0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();
		
		gl2.glPopMatrix();
	}


	/**
	 * Toggle pen up moves.
	 * @param state if <strong>true</strong> the pen up moves will be drawn.  if <strong>false</strong> they will be hidden.
 	 * FIXME setShowPenUp(false) does not refresh the WYSIWYG preview.  It should. 
	 */
	public void setShowPenUp(boolean state) {
		showPenUpMoves = state;
		if(gCode!=null) {
			gCode.changed = true;
			gCode.emptyNodeBuffer();
		}
	}

	
	/**
	 * @return the "show pen up" flag
	 */
	public boolean getShowPenUp() {
		return showPenUpMoves;
	}

}
