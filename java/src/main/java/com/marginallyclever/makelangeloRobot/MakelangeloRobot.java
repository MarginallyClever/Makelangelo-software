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

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;
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
	final String robotTypeName = "DRAWBOT";
	final String hello = "HELLO WORLD! I AM " + robotTypeName + " #";

	static public final float PEN_HOLDER_RADIUS=6; //cm
	
	// TODO make please_get_a_guid a runtime parameter
	// set to false when I'm building robots @ marginallyclever.com.  
	static boolean please_get_a_guid=false;
	
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

	// Listeners which should be notified of a change to the percentage.
    private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();

	public GCodeFile gCode;
	
	
	public MakelangeloRobot(Translator translator) {
		settings = new MakelangeloRobotSettings(translator, this);
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

	public void setConnection(MarginallyCleverConnection c) {
		if( this.connection != null ) {
			this.connection.closeConnection();
			this.connection.removeListener(this);
			notifyDisconnected();
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
	public void sendBufferEmpty(MarginallyCleverConnection arg0) {
		sendFileCommand();
		
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
		
		if(myPanel!=null) {
			myPanel.updateMachineNumberPanel();
			myPanel.updateButtonAccess();
		}
		
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

		if(please_get_a_guid) {
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
			sendLineToRobot(settings.getConfigLine() + "\n");
			sendLineToRobot(settings.getBobbinLine() + "\n");
			setHome();
			sendLineToRobot("G0 F"+ settings.getFeedRate() + " A" + settings.getAcceleration() + "\n");
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
		if(myPanel != null) myPanel.updateButtonAccess();
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
	public void tweakAndSendLine(String line, int lineNumber) {
		if (getConnection() == null || !isPortConfirmed() || !isRunning()) return;

		// tool change request?
		String[] tokens = line.split("(\\s|;)");

		// tool change?
		if (Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for (String token : tokens) {
				if (token.startsWith("T")) {
					changeToTool(token.substring(1));
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
		
		gCode.setLinesProcessed(gCode.findLastPenUpBefore(lineNumber,getSettings().getPenUpString()));
		setLineNumber(gCode.getLinesProcessed());
		setRunning();
		sendFileCommand();
	}

	private void changeToTool(String changeToolString) {
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
		sendLineToRobot("G00 X"+settings.getHomeX()+" Y"+(settings.getHomeY()*10));
		gondolaX=(float)settings.getHomeX();
		gondolaY=(float)settings.getHomeY();
	}
	
	
	public void setHome() {
		sendLineToRobot(settings.getSetStartAtHomeLine());
		hasSetHome=true;
		gondolaX=(float)settings.getHomeX();
		gondolaY=(float)settings.getHomeY();
	}
	
	
	public boolean hasSetHome() {
		return hasSetHome;
	}
	
	
	public void movePenAbsolute(float x,float y) {
		sendLineToRobot("G00"+
						" X" + x +
						" Y" + y);
		gondolaX = x * 0.1f;
		gondolaY = y * 0.1f;
	}
	
	public void movePenRelative(float dx,float dy) {
		sendLineToRobot("G91");  // set relative mode
		sendLineToRobot("G00"+
						" X" + dx +
						" Y" + dy);
		sendLineToRobot("G90");  // return to absolute mode
		gondolaX += dx * 0.1f;
		gondolaY += dy * 0.1f;
	}
	
	public boolean areMotorsEngaged() { return areMotorsEngaged; }
	
	public void movePenToEdgeLeft()   {		movePenAbsolute((float)settings.getPaperLeft()*10,gondolaY*10);	}
	public void movePenToEdgeRight()  {		movePenAbsolute((float)settings.getPaperRight()*10,gondolaY*10);	}
	public void movePenToEdgeTop()    {		movePenAbsolute(gondolaX*10,(float)settings.getPaperTop()   *10);  }
	public void movePenToEdgeBottom() {		movePenAbsolute(gondolaX*10,(float)settings.getPaperBottom()*10);  }
	
	public void disengageMotors() {		sendLineToRobot("M17");	areMotorsEngaged=false; }
	public void engageMotors()    {		sendLineToRobot("M18");	areMotorsEngaged=true; }
	
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
		paintPenHolderAndCounterweights(gl2);
		// TODO draw control box?

		if(drawDecorator!=null) {
			// filters can also draw WYSIWYG previews while converting.
			drawDecorator.render(gl2,settings);
			return;
		}

		if(gCode!=null) gCode.render(gl2,this);
	}

	// draw left motor, right motor
	private void paintMotors( GL2 gl2 ) {
		gl2.glColor3f(1,0.8f,0.5f);
		// left frame
		gl2.glPushMatrix();
		gl2.glTranslatef(-2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getLimitLeft()-5f, settings.getLimitTop()+5f);
		gl2.glVertex2d(settings.getLimitLeft()+5f, settings.getLimitTop()+5f);
		gl2.glVertex2d(settings.getLimitLeft()+5f, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitLeft()   , settings.getLimitTop()-5f);
		gl2.glVertex2d(settings.getLimitLeft()-5f, settings.getLimitTop()-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		gl2.glTranslatef(2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getLimitRight()+5f, settings.getLimitTop()+5f);
		gl2.glVertex2d(settings.getLimitRight()-5f, settings.getLimitTop()+5f);
		gl2.glVertex2d(settings.getLimitRight()-5f, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight()   , settings.getLimitTop()-5f);
		gl2.glVertex2d(settings.getLimitRight()+5f, settings.getLimitTop()-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// left motor
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(settings.getLimitLeft()-4.2f, settings.getLimitTop()+4.2f);
		gl2.glVertex2d(settings.getLimitLeft()     , settings.getLimitTop()+4.2f);
		gl2.glVertex2d(settings.getLimitLeft()     , settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitLeft()-4.2f, settings.getLimitTop());
		// right motor
		gl2.glVertex2d(settings.getLimitRight()     , settings.getLimitTop()+4.2f);
		gl2.glVertex2d(settings.getLimitRight()+4.2f, settings.getLimitTop()+4.2f);
		gl2.glVertex2d(settings.getLimitRight()+4.2f, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight()     , settings.getLimitTop());
		gl2.glEnd();
	}


	private void paintPenHolderAndCounterweights( GL2 gl2 ) {
		double dx,dy;

		double mw = settings.getLimitRight()-settings.getLimitLeft();
		double mh = settings.getLimitTop()-settings.getLimitBottom();
		double suggested_length = Math.sqrt(mw*mw+mh*mh)+5;

		dx = gondolaX - settings.getLimitLeft();
		dy = gondolaY - settings.getLimitTop();
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggested_length - left_a)/2;

		dx = gondolaX - settings.getLimitRight();
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggested_length - right_a)/2;

		if(gondolaX<settings.getLimitLeft()) return;
		if(gondolaX>settings.getLimitRight()) return;
		if(gondolaY>settings.getLimitTop()) return;
		if(gondolaY<settings.getLimitBottom()) return;
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		
		// motor to gondola left
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(gondolaX,gondolaY);
		// motor to gondola right
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(gondolaX,gondolaY);
		
		float bottleCenter = 2.1f+0.75f;
		
		// motor to counterweight left
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter-0.1, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter-0.1, settings.getLimitTop()-left_b);
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter+0.1, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter+0.1, settings.getLimitTop()-left_b);
		// motor to counterweight right
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter-0.1, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter-0.1, settings.getLimitTop()-right_b);
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter+0.1, settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter+0.1, settings.getLimitTop()-right_b);
		gl2.glEnd();
		
		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(gondolaX+Math.cos(f)*PEN_HOLDER_RADIUS,gondolaY+Math.sin(f)*PEN_HOLDER_RADIUS);
		}
		gl2.glEnd();
		
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter-1.5,settings.getLimitTop()-left_b);
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter+1.5,settings.getLimitTop()-left_b);
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter+1.5,settings.getLimitTop()-left_b-15);
		gl2.glVertex2d(settings.getLimitLeft()-bottleCenter-1.5,settings.getLimitTop()-left_b-15);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter-1.5,settings.getLimitTop()-right_b);
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter+1.5,settings.getLimitTop()-right_b);
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter+1.5,settings.getLimitTop()-right_b-15);
		gl2.glVertex2d(settings.getLimitRight()+bottleCenter-1.5,settings.getLimitTop()-right_b-15);
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
