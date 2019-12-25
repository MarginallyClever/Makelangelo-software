package com.marginallyclever.makelangeloRobot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveGCode;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.Turtle.Movement;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.preferences.GFXPreferences;
import com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * MakelangeloRobot is the Controller for a physical robot, following a
 * Model-View-Controller design pattern. It also contains non-persistent Model
 * data. MakelangeloRobotPanel is one of the Views. MakelangeloRobotSettings is
 * the persistent Model data (machine configuration).
 * 
 * @author dan
 * @since 7.2.10
 */
public class MakelangeloRobot implements NetworkConnectionListener {
	// Firmware check
	private final String versionCheckStart = new String("Firmware v");
	private boolean firmwareVersionChecked = false;
	private final long expectedFirmwareVersion = 10; // must match the version in the the firmware EEPROM
	private boolean hardwareVersionChecked = false;

	private DecimalFormat df;

	private MakelangeloRobotSettings settings = null;
	private MakelangeloRobotPanel myPanel = null;

	// Connection state
	private NetworkConnection connection = null;
	private boolean portConfirmed;

	// misc state
	private boolean areMotorsEngaged;
	private boolean isRunning;
	private boolean isPaused;
	private boolean penIsUp;
	private boolean penIsUpBeforePause;
	private boolean didSetHome;
	private float gondolaX;
	private float gondolaY;

	protected Turtle turtle;
	private ReentrantLock turtleLock;

	private ArrayList<String> drawingCommands;
	int drawingProgress;
	
	// rendering stuff
	private MakelangeloRobotDecorator decorator = null;

	// Listeners which should be notified of a change to the percentage.
	private ArrayList<MakelangeloRobotListener> listeners = new ArrayList<MakelangeloRobotListener>();


	public MakelangeloRobot() {
		// set up number format
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		df = new DecimalFormat("#.###", otherSymbols);
		df.setGroupingUsed(false);

		settings = new MakelangeloRobotSettings();
		portConfirmed = false;
		areMotorsEngaged = true;
		isRunning = false;
		isPaused = false;
		penIsUp = false;
		penIsUpBeforePause = false;
		didSetHome = false;
		setGondolaX(0);
		setGondolaY(0);
		turtle = new Turtle();
		turtleLock = new ReentrantLock();
		drawingCommands=new ArrayList<String>();
		drawingProgress=0;
	}

	public NetworkConnection getConnection() {
		return connection;
	}

	/**
	 * @param c
	 *            the connection. Use null to close the connection.
	 */
	public void openConnection(NetworkConnection c) {
		assert (c != null);

		if (this.connection != null) {
			closeConnection();
		}

		portConfirmed = false;
		didSetHome = false;
		firmwareVersionChecked = false;
		hardwareVersionChecked = false;
		this.connection = c;
		this.connection.addListener(this);
		try {
			this.connection.sendMessage("M100\n");
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}

	public void closeConnection() {
		if (this.connection == null)
			return;

		this.connection.closeConnection();
		this.connection.removeListener(this);
		notifyDisconnected();
		this.connection = null;
		this.portConfirmed = false;
	}

	@Override
	public void finalize() {
		if (this.connection != null) {
			this.connection.removeListener(this);
		}
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		sendFileCommand();

		notifyConnectionReady();
	}

	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		notifyDataAvailable(data);

		boolean justNow = false;

		// is port confirmed?
		if (!portConfirmed) {
			// machine names
			ServiceLoader<MachineStyle> knownHardware = ServiceLoader.load(MachineStyle.class);
			Iterator<MachineStyle> i = knownHardware.iterator();
			while (i.hasNext()) {
				MachineStyle ms = i.next();
				String machineTypeName = ms.getHello();
				if (data.lastIndexOf(machineTypeName) >= 0) {
					portConfirmed = true;
					// which machine GUID is this?
					String afterHello = data.substring(data.lastIndexOf(machineTypeName) + machineTypeName.length());
					parseRobotUID(afterHello);

					justNow = true;
					break;
				}
			}
		}

		// is firmware checked?
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

		// is hardware checked?
		if (!hardwareVersionChecked && data.lastIndexOf("D10") >= 0) {
			String[] pieces = data.split(" ");
			if (pieces.length > 1) {
				String last = pieces[pieces.length - 1];
				last = last.replace("\r\n", "");
				if (last.startsWith("V")) {
					String hardwareVersion = last.substring(1);
					
					this.settings.setHardwareVersion(hardwareVersion);
					hardwareVersionChecked = true;
					justNow = true;
				}
			}
		}

		if (justNow && portConfirmed && firmwareVersionChecked && hardwareVersionChecked) {
			// send whatever config settings I have for this machine.
			sendConfig();

			if (myPanel != null) {
				String hardwareVersion = this.settings.getHardwareVersion();
				myPanel.onConnect();
				this.settings.setHardwareVersion(hardwareVersion);
			}

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
				Log.error("UID parsing: " + e.getMessage());
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
	private void notifyFirmwareVersionBad(long versionFound) {
		for (MakelangeloRobotListener listener : listeners) {
			listener.firmwareVersionBad(this, versionFound);
		}
	}

	private void notifyDataAvailable(String data) {
		for (MakelangeloRobotListener listener : listeners) {
			listener.dataAvailable(this, data);
		}
	}

	private void notifyConnectionReady() {
		for (MakelangeloRobotListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}

	public void lineError(NetworkConnection arg0, int lineNumber) {
		drawingProgress=lineNumber;

		notifyLineError(lineNumber);
	}

	private void notifyLineError(int lineNumber) {
		for (MakelangeloRobotListener listener : listeners) {
			listener.lineError(this, lineNumber);
		}
	}

	public void notifyDisconnected() {
		for (MakelangeloRobotListener listener : listeners) {
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
		if (!pleaseGetAGUID)
			return 0;

		Log.info("obtaining UID from server.");
		try {
			// Send request
			URL url = new URL("https://www.marginallyclever.com/drawbot_getuid.php");
			URLConnection conn = url.openConnection();
			// get results
			InputStream connectionInputStream = conn.getInputStream();
			Reader inputStreamReader = new InputStreamReader(connectionInputStream);
			BufferedReader rd = new BufferedReader(inputStreamReader);
			String line = rd.readLine();
			Log.info("Server says: '" + line + "'");
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

	/**
	 * Send the machine configuration to the robot.
	 * 
	 * @author danroyer
	 */
	public void sendConfig() {
		if (getConnection() != null && !isPortConfirmed())
			return;

		String config = settings.getGCodeConfig();
		String[] lines = config.split("\n");

		try {
			for (int i = 0; i < lines.length; ++i) {
				sendLineToRobot(lines[i] + "\n");
			}
			setHome();
			sendLineToRobot("G0 F" + df.format(settings.getPenUpFeedRate()) + " A"
					+ df.format(settings.getAcceleration()) + "\n");
		} catch (Exception e) {
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void pause() {
		if (isPaused)
			return;

		isPaused = true;
		// remember for later if the pen is down
		penIsUpBeforePause = penIsUp;
		// raise it if needed.
		raisePen();
	}

	public void unPause() {
		if (!isPaused)
			return;

		// if pen was down before pause, lower it
		if (!penIsUpBeforePause) {
			lowerPen();
		}

		isPaused = false;
	}

	public void halt() {
		isRunning = false;
		isPaused = false;
		raisePen();
		if (myPanel != null)
			myPanel.updateButtonAccess();
	}

	public void setRunning() {
		isRunning = true;
		if (myPanel != null)
			myPanel.statusBar.start();
		if (myPanel != null)
			myPanel.updateButtonAccess(); // disables all the manual driving
											// buttons
	}

	public void raisePen() {
		sendLineToRobot(settings.getPenUpString());
		sendLineToRobot(settings.getPenUpFeedrateString());
	}

	public void lowerPen() {
		sendLineToRobot(settings.getPenDownString());
		sendLineToRobot(settings.getPenDownFeedrateString());
	}

	public void testPenAngle(double testAngle) {
		sendLineToRobot("G00 Z" + df.format(testAngle));
	}

	/**
	 * removes comments, processes commands robot doesn't handle, add checksum
	 * information.
	 *
	 * @param line
	 *            command to send
	 */
	public void sendLineWithNumberAndChecksum(String line, int lineNumber) {
		if (getConnection() == null || !isPortConfirmed() || !isRunning())
			return;
		
		line = "N" + lineNumber + " " + line;
		if(!line.endsWith(";")) line+=';';
		String checksum = generateChecksum(line);
		line += checksum;

		// send relevant part of line to the robot
		sendLineToRobot(line);
	}

	/**
	 * Take the next line from the file and send it to the robot, if permitted.
	 */
	public void sendFileCommand() {
		int total=drawingCommands.size();

		if (!isRunning() || isPaused() || total==0
			|| (getConnection() != null && isPortConfirmed() == false)
			)
			return;

		// are there any more commands?
		if(drawingProgress == total) {
			// no!
			halt();
			// bask in the glory
			if (myPanel != null)
				myPanel.statusBar.setProgress(total, total);

			SoundSystem.playDrawingFinishedSound();
		} else {
			String line = drawingCommands.get(drawingProgress);
			sendLineWithNumberAndChecksum(line, drawingProgress);
			drawingProgress++;
			
			if (myPanel != null)
				myPanel.statusBar.setProgress(drawingProgress, total);
			// loop until we find a line that gets sent to the robot, at which
			// point we'll
			// pause for the robot to respond. Also stop at end of file.
		}
	}

	public void startAt(int lineNumber) {
		if(drawingCommands.size()==0) return;

		drawingProgress = lineNumber;
		setLineNumber(lineNumber);
		setRunning();
		sendFileCommand();
	}

	/**
	 * display a dialog asking the user to change the pen
	 * 
	 * @param toolNumber
	 *            a 24 bit RGB color of the new pen.
	 */
	public void requestUserChangeTool(int toolNumber) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);

		JLabel fieldValue = new JLabel("");
		fieldValue.setOpaque(true);
		fieldValue.setMinimumSize(new Dimension(80, 20));
		fieldValue.setMaximumSize(fieldValue.getMinimumSize());
		fieldValue.setPreferredSize(fieldValue.getMinimumSize());
		fieldValue.setSize(fieldValue.getMinimumSize());
		fieldValue.setBackground(new Color(toolNumber));
		fieldValue.setBorder(new LineBorder(Color.BLACK));
		panel.add(fieldValue, c);

		JLabel message = new JLabel(Translator.get("ChangeToolMessage"));
		c.gridx = 1;
		c.gridwidth = 3;
		panel.add(message, c);

		Component root = null;
		MakelangeloRobotPanel p = this.getControlPanel();
		if (p != null)
			root = p.getRootPane();
		JOptionPane.showMessageDialog(root, panel, Translator.get("ChangeToolTitle"), JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Sends a single command the robot. Could be anything.
	 *
	 * @param line
	 *            command to send.
	 * @return <code>true</code> if command was sent to the robot;
	 *         <code>false</code> otherwise.
	 */
	public boolean sendLineToRobot(String line) {
		if (getConnection() == null || !isPortConfirmed())
			return false;

		String reportedline = line;
		// does it have a checksum? hide it in the log
		if (reportedline.contains(";")) {
			String[] lines = line.split(";");
			reportedline = lines[0];
		}
		if (reportedline.trim().isEmpty())
			return false;

		// catch pen up/down status here
		if (reportedline.startsWith(settings.getPenUpString())) {
			penIsUp = true;
		}
		if (reportedline.startsWith(settings.getPenDownString())) {
			penIsUp = false;
		}
		if (reportedline.startsWith("M17")) {
			// engage motors
			myPanel.motorsHaveBeenEngaged();
		}
		if (reportedline.startsWith("M18")) {
			// disengage motors
			myPanel.motorsHaveBeenDisengaged();
		}

		Log.info("black", line);
		line += "\n";

		// send unmodified line
		try {
			getConnection().sendMessage(line);
		} catch (Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		return true;
	}

	public void setCurrentFeedRate(float feedRate) {
		// remember it
		settings.setCurrentFeedRate(feedRate);
		// get it again in case it was capped.
		feedRate = settings.getPenDownFeedRate();
		// tell the robot
		sendLineToRobot("G00 F" + df.format(feedRate));
	}

	public double getCurrentFeedRate() {
		return settings.getPenDownFeedRate();
	}

	public void goHome() {
		sendLineToRobot("G00 X" + df.format(settings.getHomeX()) + " Y" + df.format(settings.getHomeY()));
		setGondolaX((float) settings.getHomeX());
		gondolaY = (float) settings.getHomeY();
	}

	public void findHome() {
		this.raisePen();
		sendLineToRobot("G28");
		setGondolaX((float) settings.getHomeX());
		setGondolaY((float) settings.getHomeY());
	}

	public void setHome() {
		sendLineToRobot(settings.getGCodeSetPositionAtHome());
		// save home position
		sendLineToRobot("D6 X" + df.format(settings.getHomeX()) + " Y" + df.format(settings.getHomeY()));
		setGondolaX((float) settings.getHomeX());
		setGondolaY((float) settings.getHomeY());
		didSetHome = true;
	}

	public boolean didSetHome() {
		return didSetHome;
	}

	/**
	 * @param x absolute position in mm
	 * @param y absolute position in mm
	 */
	public void movePenAbsolute(float x, float y) {
		sendLineToRobot("G00 X" + df.format(x) + " Y" + df.format(y));
		setGondolaX(x);
		gondolaY = y;
	}

	/**
	 * @param x
	 *            relative position in mm
	 * @param y
	 *            relative position in mm
	 */
	public void movePenRelative(float dx, float dy) {
		sendLineToRobot("G91"); // set relative mode
		sendLineToRobot("G00 X" + df.format(dx) + " Y" + df.format(dy));
		sendLineToRobot("G90"); // return to absolute mode
		setGondolaX(getGondolaX() + dx);
		gondolaY += dy;
	}

	public boolean areMotorsEngaged() {
		return areMotorsEngaged;
	}

	public void movePenToEdgeLeft() {
		movePenAbsolute((float) settings.getPaperLeft(), gondolaY);
	}

	public void movePenToEdgeRight() {
		movePenAbsolute((float) settings.getPaperRight(), gondolaY);
	}

	public void movePenToEdgeTop() {
		movePenAbsolute(getGondolaX(), (float) settings.getPaperTop());
	}

	public void movePenToEdgeBottom() {
		movePenAbsolute(getGondolaX(), (float) settings.getPaperBottom());
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

	public void setLineNumber(int newLineNumber) {
		sendLineToRobot("M110 N" + newLineNumber);
	}

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
	
	
	protected void checkResize() {	
		{
			int result = JOptionPane.showConfirmDialog(myPanel, "Resize to fit inside margins?", "Resize", JOptionPane.YES_NO_OPTION);
			System.out.println(result);
			if(result == JOptionPane.YES_OPTION) {
				Point2D top = new Point2D();
				Point2D bottom = new Point2D();
				turtle.getBounds(top, bottom);
				
				double tw = top.x-bottom.x;
				double th = top.y-bottom.y;
				double nh=th;
				double nw=tw;
				double w = settings.getMarginWidth();
				double h = settings.getMarginHeight();
				double ratioW=1,ratioH=1;
				ratioH = h/nh;
				ratioW = w/nw;
				// use < to fit in the page.
				double ratio = ratioW<ratioH?ratioW:ratioH;
				turtle.scale(ratio,ratio);
			}
		}
		{
			int result = JOptionPane.showConfirmDialog(myPanel, "Resize to fill margins?", "Resize", JOptionPane.YES_NO_OPTION);
			System.out.println(result);
			if(result == JOptionPane.YES_OPTION) {
				Point2D top = new Point2D();
				Point2D bottom = new Point2D();
				turtle.getBounds(top, bottom);
				
				double tw = top.x-bottom.x;
				double th = top.y-bottom.y;
				double nh=th;
				double nw=tw;
				double w = settings.getMarginWidth();
				double h = settings.getMarginHeight();
				double ratioW=1,ratioH=1;
				ratioH = h/nh;
				ratioW = w/nw;
				// use > to fill the page.
				double ratio = ratioW>ratioH?ratioW:ratioH;
				turtle.scale(ratio,ratio);
			}
		}
	}

	protected void checkOptimize() {
		int result = JOptionPane.showConfirmDialog(myPanel, "Turn many short moves into fewer longer moves?", "Optimize", JOptionPane.YES_NO_OPTION);
		System.out.println(result);
		if(result == JOptionPane.YES_OPTION) {
			ArrayList<Movement> toKeep = new ArrayList<Movement>();
			
			// The goal is to find many pen down moves in a row that are very short
			// and reduce them to 
			boolean isUp=true;
			double ox=settings.getHomeX();
			double oy=settings.getHomeY();
			double sum=0;
			double minimumStepSize=1;
			
			for( Movement m : turtle.history ) {
				switch(m.type) {
				case DRAW:
					if(isUp) {
						isUp=false;
						sum=0;
						toKeep.add(m);
					} else {
						double dx=m.x-ox;
						double dy=m.y-oy;
						sum+=Math.sqrt(dx*dx+dy*dy);
						if(sum>minimumStepSize) {
							toKeep.add(m);
							sum=0;
						}
					}
					ox=m.x;
					oy=m.y;
					break;
				case TRAVEL:
					toKeep.add(m);
				default:
					toKeep.add(m);
					break;
				}
			}
			int os = turtle.history.size();
			int ns = toKeep.size();
			Log.info("was "+os+" is now "+ns);
			turtle.history = toKeep;
		}
	}
	
	public void setTurtle(Turtle t) {
		turtle=t;
		
		checkResize();
		checkOptimize();
		
		try (final OutputStream fileOutputStream = new FileOutputStream("currentDrawing.ngc")) {
			LoadAndSaveGCode exportForDrawing = new LoadAndSaveGCode();
			exportForDrawing.save(fileOutputStream, this);
			
			drawingCommands.clear();
			BufferedReader reader = new BufferedReader(new FileReader("currentDrawing.ngc"));
		    String line;
		     
		    while ((line = reader.readLine()) != null) {
		        drawingCommands.add(line.trim());
		    }
		    reader.close();
		    
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		estimateTime();
	}
	
	public Turtle getTurtle() {
		return turtle;
	}

	protected void estimateTime() {
		if(turtleLock.isLocked()) return;
		turtleLock.lock();
		
		try {
			double totalTime=0;
			
			boolean isUp=true;
			double ox=this.settings.getHomeX();
			double oy=this.settings.getHomeY();
			double oz=this.settings.getPenUpAngle();
			
			for( Turtle.Movement m : turtle.history ) {
				double nx=ox;
				double ny=oy;
				double nz=oz;
				
				switch(m.type) {
				case TRAVEL:
					if(!isUp) {
						nz = this.settings.getPenUpAngle();
						isUp=true;
					}
					break;
				case DRAW:
					if(isUp) {
						nz = this.settings.getPenDownAngle();
						isUp=false;
					}
					nx=m.x;
					ny=m.y;
					break;
				case TOOL_CHANGE:
					// n remains unchanged, so length is zero.
					break;
				}

				double dx=nx-ox;
				double dy=ny-oy;
				double dz=nz-oz;
				double length=Math.sqrt(dx*dx+dy*dy+dz*dz);
				if(length>0) {
					double accel = settings.getAcceleration();
					double maxV;
					if(oz!=nz) {
						maxV = settings.getZRate();
					} else if(nz==settings.getPenDownAngle()) {
						maxV = settings.getPenDownFeedRate();
					} else {
						maxV = settings.getPenUpFeedRate();
					}
					totalTime+=estimateSingleBlock(length,0,0,maxV,accel);
				}
				ox=nx;
				oy=ny;
				oz=nz;
			}
			
			double seconds = totalTime % 60;
			totalTime-=seconds;
			totalTime/=60;
			int minutes = (int)(totalTime % 60);
			totalTime-=minutes;
			totalTime/=60;
			int hours = (int)totalTime;
			
			Log.info("Worst case draw time="+hours+"h"+minutes+"m"+(int)(seconds)+"s.");	
		}
		finally {
			turtleLock.unlock();
		}
	}
	
	/**
	 * calculate seconds to move a given length.  Also uses globals feedRate and acceleration 
	 * See http://zonalandeducation.com/mstm/physics/mechanics/kinematics/EquationsForAcceleratedMotion/AlgebraRearrangements/Displacement/DisplacementAccelerationAlgebra.htm
	 * @param length mm distance to travel.
	 * @param startRate mm/s at start of move
	 * @param endRate mm/s at end of move
	 * @return time to execute move
	 */
	protected double estimateSingleBlock(double length,double startRate,double endRate,double maxV,double accel) {
		double distanceToAccelerate = ( maxV*maxV - startRate*startRate ) / (2.0 *  accel);
		double distanceToDecelerate = ( endRate*endRate   - maxV*maxV   ) / (2.0 * -accel);
		double distanceAtTopSpeed = length - distanceToAccelerate - distanceToDecelerate;
		if(distanceAtTopSpeed<0) {
			// we never reach feedRate.
			double intersection = (2.0 * accel * length - startRate*startRate + endRate*endRate) / (4.0*accel);
			distanceToAccelerate = intersection;
			distanceToDecelerate = length-intersection;
			distanceAtTopSpeed = 0;
		}
		// time at maxV
		double time = distanceAtTopSpeed / maxV;
		
		// time accelerating (v=start vel;a=acceleration;d=distance;t=time)
		// 0.5att+vt-d=0
		// att+2vt=2d
		// using quadratic to solve for t,
		// t = (-v +/- sqrt(vv+2ad))/a
		double s;
		s = Math.sqrt(startRate*startRate + 2.0*accel*distanceToAccelerate);
		double a = (-startRate + s)/accel;
		double b = (-startRate - s)/accel;
		double accelTime = a>b? a:b;
		if(accelTime<0) {
			accelTime=0;
		}
		
		// time decelerating (v=end vel;a=acceleration;d=distance;t=time)
		s = Math.sqrt(endRate*endRate + 2.0*accel*distanceToDecelerate);
		double c = (-endRate + s)/accel;
		double d = (-endRate - s)/accel;
		double decelTime = c>d? c:d;
		if(decelTime<0) {
			decelTime=0;
		}
		
		// sum total
		return time+accelTime+decelTime;
	}
	
	public void setDecorator(MakelangeloRobotDecorator arg0) {
		decorator = arg0;
	}

	public void render(GL2 gl2) {
		paintLimits(gl2);

		settings.getHardwareProperties().render(gl2, this);

		if (decorator != null) {
			// filters can also draw WYSIWYG previews while converting.
			decorator.render(gl2);
		} else if(turtle != null) {
			if(turtleLock.isLocked()) return;
			turtleLock.lock();
			try {
				boolean showPenUp = GFXPreferences.getShowPenUp();
				ColorRGB penUpColor = this.settings.getPenUpColor();
				ColorRGB penDownColor = this.settings.getPenDownColorDefault();
				
				boolean isUp=true;
				double ox=this.settings.getHomeX();
				double oy=this.settings.getHomeY();
				Movement previousMove=turtle.new Movement(ox,oy,Turtle.MoveType.TRAVEL);				
	
				gl2.glBegin(GL2.GL_LINE_STRIP);
				
				gl2.glColor4d(
						(double)penUpColor.getRed() / 255.0,
						(double)penUpColor.getGreen() / 255.0,
						(double)penUpColor.getBlue() / 255.0,
						showPenUp?1:0);
				gl2.glVertex2d(ox,oy);
				
				for( Turtle.Movement m : turtle.history ) {
					switch(m.type) {
					case TRAVEL:
						if(!isUp) {
							isUp=true;
							gl2.glColor4d(
									(double)penUpColor.getRed() / 255.0,
									(double)penUpColor.getGreen() / 255.0,
									(double)penUpColor.getBlue() / 255.0,
									showPenUp?1:0);
							if(previousMove!=null) {
								gl2.glVertex2d(previousMove.x,previousMove.y);
							}
							gl2.glVertex2d(m.x,m.y);
						}
						previousMove=m;
						break;
					case DRAW:
						if(isUp) {
							gl2.glVertex2d(m.x,m.y);
							gl2.glColor4d(
									(double)penDownColor.getRed() / 255.0,
									(double)penDownColor.getGreen() / 255.0,
									(double)penDownColor.getBlue() / 255.0,
									1);
							if(previousMove!=null) {
								gl2.glVertex2d(previousMove.x,previousMove.y);
							}
							isUp=false;
						}
						gl2.glVertex2d(m.x,m.y);
						previousMove=m;
						break;
					case TOOL_CHANGE:
						penDownColor = m.getColor();
						gl2.glColor4d(
								(double)penDownColor.getRed() / 255.0,
								(double)penDownColor.getGreen() / 255.0,
								(double)penDownColor.getBlue() / 255.0,
								1);
						break;
					}
				}
				
				gl2.glEnd();
			}
			finally {
				turtleLock.unlock();
			}
		}
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

		// margin settings
		gl2.glPushMatrix();
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glLineWidth(1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginBottom());
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginBottom());
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	// in mm
	public float getGondolaX() {
		return gondolaX;
	}

	// in mm
	public void setGondolaX(float gondolaX) {
		this.gondolaX = gondolaX;
	}

	// in mm
	public float getGondolaY() {
		return gondolaY;
	}

	// in mm
	public void setGondolaY(float gondolaY) {
		this.gondolaY = gondolaY;
	}

	public int findLastPenUpBefore(int startAtLine) {
		int total = drawingCommands.size();
		if( total==0 ) return 0;

		String toMatch = settings.getPenUpString();
		
		int x = startAtLine;
		if(x >= total) x = total-1;
		
		toMatch = toMatch.trim();
		while(x>1) {
			String line = drawingCommands.get(x).trim();
			if(line.equals(toMatch)) {
				return x;
			}
			--x;
		}
				
		return x;
	}
}
