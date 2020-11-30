package com.marginallyclever.makelangeloRobot.settings;

import java.awt.BasicStroke;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo2Properties;
import com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;
import com.marginallyclever.util.PreferencesHelper;


/**
 * All the hardware settings for a single plotter robot.  Does not store state information.  
 * @author Dan Royer
 * TODO move tool names into translations & add a color palette system for quantizing colors.
 */
public final class MakelangeloRobotSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4185946661019573192L;

	public static final String COMMAND_MOVE = "G0";
	public static final String COMMAND_TRAVEL = "G1";
	public static final double INCH_TO_CM = 2.54;
	public static final int FIRMWARE_MAX_SEGMENTS = 32;
	
	private String[] configsAvailable;

	private ArrayList<MakelangeloRobotSettingsListener> listeners;

	// Each robot has a global unique identifier
	private long robotUID;
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered;
	// machine physical limits, in mm
	private double limitLeft;
	private double limitRight;
	private double limitBottom;
	private double limitTop;
	// paper area, in mm
	private double paperLeft;
	private double paperRight;
	private double paperBottom;
	private double paperTop;
	private double rotation;
	private double rotationref;
	// % from edge of paper.
	private double paperMargin;

	// for a while the robot would sign it's name at the end of a drawing
	@Deprecated
	private boolean shouldSignName;
	
	private String hardwareVersion;
	private MakelangeloHardwareProperties hardwareProperties;

	
	private ColorRGB paperColor;
	
	private int lookAheadSegments;  // >0 and power of two.

	protected ColorRGB penDownColorDefault;
	protected ColorRGB penDownColor;
	protected ColorRGB penUpColor;
	
	// speed control
	private float feedRateMax;
	private float feedRateDefault;
	private float accelerationMax;

	private float diameter;  // pen diameter (mm, >0)
	private float zOff;	// pen up servo angle (deg,0...180)
	private float zOn;	// pen down servo angle (deg,0...180)
	private float zRate;	// pen servo movement speed (deg/s)

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

	private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

	
	/**
	 * These values should match https://github.com/marginallyclever/makelangelo-firmware/firmware_rumba/configure.h
	 */
	public MakelangeloRobotSettings() {				
		double mh = 835; // mm
		double mw = 835; // mm
		
		robotUID = 0;
		isRegistered = false;
		limitTop = mh/2;
		limitBottom = -mh/2;
		limitRight = mw/2;
		limitLeft = -mw/2;

		paperColor = new ColorRGB(255,255,255);
		
		listeners = new ArrayList<MakelangeloRobotSettingsListener>();
		shouldSignName = false;
		
		// paper area
		double pw = 420 * 0.1; // cm
		double ph = 594 * 0.1; // cm

		paperTop = ph/2;
		paperBottom = -ph/2;
		paperLeft = -pw/2;
		paperRight = pw/2;
		paperMargin = 0.9;

		penDownColor = penDownColorDefault = new ColorRGB(0,0,0); // BLACK
		penUpColor = new ColorRGB(0,255,0); // blue
		startingPositionIndex = 4;

		setLookAheadSegments(FIRMWARE_MAX_SEGMENTS);  // firmware MAX_SEGMENTS
		
		// default hardware version is 2
		setHardwareVersion("2");

		// which configurations are available?
		try {
			configsAvailable = topLevelMachinesPreferenceNode.childrenNames();
		} catch (Exception e) {
			Log.error( e.getMessage() );
			configsAvailable = new String[1];
			configsAvailable[0] = "Default";
		}
		
		// Load most recent config
		//loadConfig(last_machine_id);
	}
	
	public void addListener(MakelangeloRobotSettingsListener listener) {
		listeners.add(listener);
	}
	
	public void createNewUID(long newUID) {
		// make sure a topLevelMachinesPreferenceNode node is created
		topLevelMachinesPreferenceNode.node(Long.toString(newUID));
		
		// if this is a new robot UID, update the list of available configurations
		final String[] new_list = new String[configsAvailable.length + 1];
		System.arraycopy(configsAvailable, 0, new_list, 0, configsAvailable.length);
		new_list[configsAvailable.length] = Long.toString(newUID);
		configsAvailable = new_list;
	}
	
	
	
	public double getAcceleration() {
		return accelerationMax;
	}

	/**
	 * Get the UID of every machine this computer recognizes INCLUDING machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getAvailableConfigurations() {
		return configsAvailable;
	}

	public Point2D getHome() {
		return getHardwareProperties().getHome(this);
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
		return "G92 X"+getHomeX()+" Y"+getHomeY();
	}

	// return the strings that will tell a makelangelo robot its physical limits.
	public String getGCodeConfig() {
		return getHardwareProperties().getGCodeConfig(this);
	}

	public String getAbsoluteMode() {
		return "G90";
	}

	public String getRelativeMode() {
		return "G91";
	}

	public int getKnownMachineIndex() {
		String [] list = getKnownMachineNames();
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals("0")) continue;
			if (list[i].equals(Long.toString(robotUID))) {
				return i;
			}
		}

		return -1;
	}

	/*
  // TODO finish these cloud storage methods.  Security will be a problem.

   public boolean GetCanUseCloud() {
    return topLevelMachinesPreferenceNode.getBoolean("can_use_cloud", false);
  }


  public void SetCanUseCloud(boolean b) {
    topLevelMachinesPreferenceNode.putBoolean("can_use_cloud", b);
  }

  protected boolean SaveConfigToCloud() {
    return false;
  }



   protected boolean LoadConfigFromCloud() {
     // Ask for credentials: MC login, password.  auto-remember login name.
     //String login = new String();
     //String password = new String();

     //try {
     // Send query
     //URL url = new URL("https://marginallyclever.com/drawbot_getmachineconfig.php?name="+login+"pass="+password+"&id="+robot_uid);
     //URLConnection conn = url.openConnection();
     //BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     // read data

     // close connection
     //rd.close();
     //} catch (Exception e) {}

    return false;
  }
	 */


	/**
	 * Get the UID of every machine this computer recognizes EXCEPT machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getKnownMachineNames() {
		final List<String> knownMachineList = new LinkedList<>(Arrays.asList(configsAvailable));
		if (knownMachineList.contains("0")) {
			knownMachineList.remove("0");
		}
		return Arrays.copyOf(knownMachineList.toArray(), knownMachineList.size(), String[].class);
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

	/**
	 * @return the number of machine configurations that exist on this computer
	 */
	public int getMachineCount() {
		return configsAvailable.length;
	}

	/**
	 * @return paper height in mm.
	 */
	public double getPaperHeight() {
		return paperTop - paperBottom;
	}

	/**
	 * @return paper width in mm.
	 */
	public double getMarginHeight() {
		return getMarginTop() - getMarginBottom();
	}

	/**
	 * @return paper width in mm.
	 */
	public double getMarginWidth() {
		return getMarginRight() - getMarginLeft();
	}

	/**
	 * @return paper width in mm.
	 */
	public double getPaperWidth() {
		return paperRight - paperLeft;
	}

	/**
	 * @return paper left edge in mm.
	 */
	public double getPaperLeft() {
		return paperLeft;
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getPaperRight() {
		return paperRight;
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getPaperTop() {
		return paperTop;
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getPaperBottom() {
		return paperBottom;
	}

	/**
	 * @return paper left edge in mm.
	 */
	public double getMarginLeft() {
		return getPaperLeft() * getPaperMargin();
	}

	/**
	 * @return paper right edge in mm.
	 */
	public double getMarginRight() {
		return getPaperRight() * getPaperMargin();
	}

	/**
	 * @return paper top edge in mm.
	 */
	public double getMarginTop() {
		return getPaperTop() * getPaperMargin();
	}

	/**
	 * @return paper bottom edge in mm.
	 */
	public double getMarginBottom() {
		return getPaperBottom() * getPaperMargin();
	}

	/**
	 * @return paper margin %.
	 */
	public double getPaperMargin() {
		return paperMargin;
	}
	
	public long getUID() {
		return robotUID;
	}

	public boolean isPaperConfigured() {
		return (paperTop > paperBottom && paperRight > paperLeft);
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	/**
	 * Load the machine configuration
	 *
	 * @param uid the unique id of the robot to be loaded
	 */
	public void loadConfig(long uid) {
		robotUID = uid;
		// once cloud logic is finished.
		//if( GetCanUseCloud() && LoadConfigFromCloud() ) return;
		loadConfigFromLocal();
	}

	protected void loadConfigFromLocal() {
		final Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		limitTop    = Double.valueOf(uniqueMachinePreferencesNode.get("limit_top", Double.toString(limitTop)));
		limitBottom = Double.valueOf(uniqueMachinePreferencesNode.get("limit_bottom", Double.toString(limitBottom)));
		limitLeft   = Double.valueOf(uniqueMachinePreferencesNode.get("limit_left", Double.toString(limitLeft)));
		limitRight  = Double.valueOf(uniqueMachinePreferencesNode.get("limit_right", Double.toString(limitRight)));

		paperLeft   = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_left",Double.toString(paperLeft)));
		paperRight  = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_right",Double.toString(paperRight)));
		paperTop    = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_top",Double.toString(paperTop)));
		paperBottom = Double.parseDouble(uniqueMachinePreferencesNode.get("paper_bottom",Double.toString(paperBottom)));
		rotation = Double.parseDouble(uniqueMachinePreferencesNode.get("rotation",Double.toString(rotation)));
		rotationref = 0;

		accelerationMax=Float.valueOf(uniqueMachinePreferencesNode.get("acceleration",Float.toString(accelerationMax)));

		startingPositionIndex = Integer.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex",Integer.toString(startingPositionIndex)));

		int r,g,b;
		r = uniqueMachinePreferencesNode.getInt("paperColorR", paperColor.getRed());
		g = uniqueMachinePreferencesNode.getInt("paperColorG", paperColor.getGreen());
		b = uniqueMachinePreferencesNode.getInt("paperColorB", paperColor.getBlue());
		paperColor = new ColorRGB(r,g,b);

		paperMargin = Double.valueOf(uniqueMachinePreferencesNode.get("paper_margin", Double.toString(paperMargin)));
		//setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool", Integer.toString(getCurrentToolNumber()))));
		setRegistered(Boolean.parseBoolean(uniqueMachinePreferencesNode.get("isRegistered",Boolean.toString(isRegistered))));

		loadPenConfig(uniqueMachinePreferencesNode);

		setHardwareVersion(uniqueMachinePreferencesNode.get("hardwareVersion", hardwareVersion));
	}

	protected void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setDiameter(Float.parseFloat(prefs.get("diameter", Float.toString(getPenDiameter()))));
		setzRate(Float.parseFloat(prefs.get("z_rate", Float.toString(getzRate()))));
		setzOn(Float.parseFloat(prefs.get("z_on", Float.toString(getzOn()))));
		setzOff(Float.parseFloat(prefs.get("z_off", Float.toString(getzOff()))));
		//tool_number = Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
		feedRateMax = Float.parseFloat(prefs.get("feed_rate", Float.toString(feedRateMax)));
		feedRateDefault=Float.valueOf(prefs.get("feed_rate_current",Float.toString(feedRateDefault)));
		
		int r,g,b;
		r = prefs.getInt("penDownColorR", penDownColor.getRed());
		g = prefs.getInt("penDownColorG", penDownColor.getGreen());
		b = prefs.getInt("penDownColorB", penDownColor.getBlue());
		penDownColor = penDownColorDefault = new ColorRGB(r,g,b);
		r = prefs.getInt("penUpColorR", penUpColor.getRed());
		g = prefs.getInt("penUpColorG", penUpColor.getGreen());
		b = prefs.getInt("penUpColorB", penUpColor.getBlue());
		penUpColor = new ColorRGB(r,g,b);
	}

	protected void savePenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		prefs.put("diameter", Float.toString(getPenDiameter()));
		prefs.put("z_rate", Float.toString(getzRate()));
		prefs.put("z_on", Float.toString(getzOn()));
		prefs.put("z_off", Float.toString(getzOff()));
		//prefs.put("tool_number", Integer.toString(toolNumber));
		prefs.put("feed_rate", Float.toString(feedRateMax));
		prefs.put("feed_rate_current", Float.toString(feedRateDefault));
		prefs.putInt("penDownColorR", penDownColorDefault.getRed());
		prefs.putInt("penDownColorG", penDownColorDefault.getGreen());
		prefs.putInt("penDownColorB", penDownColorDefault.getBlue());
		prefs.putInt("penUpColorR", penUpColor.getRed());
		prefs.putInt("penUpColorG", penUpColor.getGreen());
		prefs.putInt("penUpColorB", penUpColor.getBlue());
	}

	public void notifySettingsChanged() {
		for( MakelangeloRobotSettingsListener listener : listeners ) {
			listener.settingsChangedEvent(this);
		}
	}

	public void removeListener(MakelangeloRobotSettingsListener listener) {
		listeners.remove(listener);
	}
	
	// Save the machine configuration
	public void saveConfig() {
		// once cloud logic is finished.
		//if(GetCanUseCloud() && SaveConfigToCloud() ) return;
		saveConfigToLocal();
		notifySettingsChanged();
	}
	
	protected void saveConfigToLocal() {
		final Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		uniqueMachinePreferencesNode.put("limit_top", Double.toString(limitTop));
		uniqueMachinePreferencesNode.put("limit_bottom", Double.toString(limitBottom));
		uniqueMachinePreferencesNode.put("limit_right", Double.toString(limitRight));
		uniqueMachinePreferencesNode.put("limit_left", Double.toString(limitLeft));
		uniqueMachinePreferencesNode.put("acceleration", Double.toString(accelerationMax));
		uniqueMachinePreferencesNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

		uniqueMachinePreferencesNode.putDouble("paper_left", paperLeft);
		uniqueMachinePreferencesNode.putDouble("paper_right", paperRight);
		uniqueMachinePreferencesNode.putDouble("paper_top", paperTop);
		uniqueMachinePreferencesNode.putDouble("paper_bottom", paperBottom);
		uniqueMachinePreferencesNode.putDouble("rotation", rotation);
		
		uniqueMachinePreferencesNode.putInt("paperColorR", paperColor.getRed());
		uniqueMachinePreferencesNode.putInt("paperColorG", paperColor.getGreen());
		uniqueMachinePreferencesNode.putInt("paperColorB", paperColor.getBlue());

		uniqueMachinePreferencesNode.put("paper_margin", Double.toString(paperMargin));
		//uniqueMachinePreferencesNode.put("current_tool", Integer.toString(getCurrentToolNumber()));
		uniqueMachinePreferencesNode.put("isRegistered", Boolean.toString(isRegistered()));
		
		uniqueMachinePreferencesNode.put("hardwareVersion", hardwareVersion);

		savePenConfig(uniqueMachinePreferencesNode);
	}
	
	public void setAcceleration(float f) {
		accelerationMax = f;
	}
	
	public void setMaxFeedRate(float f) {
		feedRateMax = f;
		if(feedRateDefault > feedRateMax) {
			feedRateDefault = feedRateMax;
		}
	}
	
	public float getPenUpFeedRate() {
		return feedRateMax;
	}
	
	public void setCurrentFeedRate(float f) {
		if (f < 0.001) f = 0.001f;
		if( f > feedRateMax) {
			f = feedRateMax;
		}
		feedRateDefault = f;
	}
	
	public float getPenDownFeedRate() {
		return feedRateDefault;
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
		this.limitLeft = -width/2.0;
		this.limitRight = width/2.0;
		this.limitBottom = -height/2.0;
		this.limitTop = height/2.0;
	}
	
	public void setPaperMargin(double paperMargin) {
		this.paperMargin = paperMargin;	
	}

	public void setPaperSize(double width, double height, double shiftx, double shifty) {
		this.paperLeft = -width/2 + shiftx;
		this.paperRight = width/2 + shiftx;
		this.paperTop = height/2 + shifty;
		this.paperBottom = -height/2+shifty;
	}
	
	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
	

	public boolean shouldSignName() {
		return shouldSignName;
	}

	public String getHardwareVersion() {
		return hardwareVersion;
	}

	public MakelangeloHardwareProperties getHardwareProperties() {
		return hardwareProperties;
	}

	public void setHardwareVersion(String version) {
		String newVersion = "";

		try {
			// get version numbers
			ServiceLoader<MakelangeloHardwareProperties> knownHardware = ServiceLoader.load(MakelangeloHardwareProperties.class);
			Iterator<MakelangeloHardwareProperties> i = knownHardware.iterator();
			while(i.hasNext()) {
				MakelangeloHardwareProperties hw = i.next();
				if(hw.getVersion().equals(version)) {
					hardwareProperties = hw.getClass().newInstance();
					newVersion = version;
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.error("Hardware version instance failed. Defaulting to v2");
			hardwareProperties = new Makelangelo2Properties();
			newVersion="2";
		}
		if(newVersion == "") {
			Log.error("Unknown hardware version requested. Defaulting to v2");
			hardwareProperties = new Makelangelo2Properties();
		}
		
		hardwareVersion = newVersion;
		if(!hardwareProperties.canChangeMachineSize()) {
			this.setMachineSize(hardwareProperties.getWidth(), hardwareProperties.getHeight());
		}

		// apply default hardware values
		feedRateMax = hardwareProperties.getFeedrateMax();
		feedRateDefault = hardwareProperties.getFeedrateDefault();
		accelerationMax = hardwareProperties.getAccelerationMax();
		
		setzRate(hardwareProperties.getZRate());
		setzOn(hardwareProperties.getZAngleOn());
		setzOff(hardwareProperties.getZAngleOff());

		// pen
		setDiameter(0.8f);
	}
	
	public ColorRGB getPaperColor() {
		return paperColor;
	}
	
	public void setPaperColor(ColorRGB arg0) {
		paperColor = arg0;
	}
	
	public ColorRGB getPenDownColorDefault() {
		return penDownColorDefault;
	}
	
	public ColorRGB getPenDownColor() {
		return penDownColor;
	}
	
	public void setPenDownColorDefault(ColorRGB arg0) {
		penDownColorDefault=arg0;
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

	public float getZRate() {
		return getzRate();
	}
	
	public void setZRate(float arg0) {
		setzRate(arg0);
	}
	

	public float getPenDownAngle() {
		return getzOn();
	}

	public float getPenUpAngle() {
		return getzOff();
	}

	public void setPenDownAngle(float arg0) {
		setzOn(arg0);
	}

	public void setPenUpAngle(float arg0) {
		setzOff(arg0);
	}

	public BasicStroke getStroke() {
		return new BasicStroke(getPenDiameter() * 10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	public void writeProgramStart(Writer out) throws IOException {	
		hardwareProperties.writeProgramStart(out);
	}
	
	public void writeProgramEnd(Writer out) throws IOException {	
		hardwareProperties.writeProgramEnd(out);
	}
	
	public String getPenDownString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getzRate()) + " Z" + StringHelper.formatDouble(getPenDownAngle());
	}

	public String getPenUpString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getzRate()) + " Z" + StringHelper.formatDouble(getPenUpAngle());
	}
	
	public String getPenUpFeedrateString() {
		return COMMAND_TRAVEL+" F" + StringHelper.formatDouble(getPenUpFeedRate());
	}
	
	public String getPenDownFeedrateString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getPenDownFeedRate());
	}

	public void writeChangeTo(Writer out,ColorRGB newPenDownColor) throws IOException {
		//if(penDownColor == newPenDownColor) return;
		
		penDownColor = newPenDownColor;
		writeChangeToInternal(out);
	}

	public void writeChangeToDefaultColor(Writer out) throws IOException {
		penDownColor = penDownColorDefault;
		writeChangeToInternal(out);
	}
	
	protected void writeChangeToInternal(Writer out) throws IOException {
		int toolNumber = penDownColor.toInt() & 0xffffff;  // ignore alpha channel

		String name="";
		switch(toolNumber) {
		case 0xff0000: name="red";		break;
		case 0x00ff00: name="green";	break;
		case 0x0000ff: name="blue";		break;
		case 0x000000: name="black";	break;
		case 0x00ffff: name="cyan";		break;
		case 0xff00ff: name="magenta";	break;
		case 0xffff00: name="yellow";	break;
		case 0xffffff: name="white";	break;
		default: name= "0x"+Integer.toHexString(toolNumber);  break;  // display unknown RGB value as hex
		}
		
		String changeString = String.format("%-20s", "Change to "+name);
		String continueString = String.format("%-20s", "Click to continue");

		out.write("M06 T" + toolNumber + "\n");
		out.write("M117 "+changeString+continueString+"\n");
		out.write("M300 S60 P250\n");  // beep
		out.write("M226\n");  // pause for user input
		out.write("M117\n");  // clear message
		out.write("G00 F" + StringHelper.formatDouble(getPenUpFeedRate()) + 
					 " A" + StringHelper.formatDouble(getAcceleration()) + "\n");
	}
	
	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s);  
	}

	// 7.22.6: feedrate changes here
	public void writeMoveTo(Writer out, double x, double y,boolean isUp,boolean zMoved) throws IOException {
		String command = isUp?COMMAND_TRAVEL:COMMAND_MOVE;
		if(zMoved) {
			command = isUp 
					? getPenUpFeedrateString() 
					: getPenDownFeedrateString();
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
		// this dwell forces the firmware to stop path-planning through the lift action.
		// we want to stop is so that it doesn't take off at a crazy speed after.
		// G04 S[milliseconds] P[seconds]
		//out.write("G04 S1\n");
		
		// moved the feedrate adjust to writeMoveTo() in 7.22.6
		//out.write(COMMAND_TRAVEL+" F" + StringHelper.formatDouble(getPenUpFeedRate()) + "\n");
	}
	
	// lower the pen
	public void writePenDown(Writer out) throws IOException {
		out.write(getPenDownString()+"\n");
		// this dwell forces the firmware to stop path-planning through the lift action.
		// we want to stop is so that it doesn't take off at a crazy speed after.
		// G04 S[milliseconds] P[seconds]
		//out.write("G04 S1\n");

		// moved the feedrate adjust to writeMoveTo() in 7.22.6
		//out.write(COMMAND_MOVE+" F" + StringHelper.formatDouble(getPenDownFeedRate()) + "\n");
	}
	
	public void writeAbsoluteMode(Writer out) throws IOException {
		out.write(getAbsoluteMode() + "\n");
	}

	public void writeRelativeMode(Writer out) throws IOException {
		out.write(getRelativeMode() + "\n");
	}

	public int getLookAheadSegments() {
		return lookAheadSegments;
	}

	public void setLookAheadSegments(int arg0) {
		this.lookAheadSegments = arg0;
	}


	public void setDiameter(float d) {
		diameter = d;
	}
	
	public float getPenDiameter() {
		return diameter;
	}

	protected float getzOff() {
		return zOff;
	}

	protected void setzOff(float zOff) {
		this.zOff = zOff;
	}

	protected float getzOn() {
		return zOn;
	}

	protected void setzOn(float zOn) {
		this.zOn = zOn;
	}

	protected float getzRate() {
		return zRate;
	}

	protected void setzRate(float zRate) {
		this.zRate = zRate;
	}

	public double getRotation() {
		return this.rotation;
	}

	public void setRotation(double rot) {
		this.rotation=rot;
	}

	public void setRotationRef(double ang) {
		this.rotationref=ang;
		
	}

	public double getRotationRef() {
		return this.rotationref;
	}

}
