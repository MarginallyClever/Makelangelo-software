package com.marginallyclever.makelangelo.robot;

import java.awt.BasicStroke;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.StringHelper;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.makelangelo.robot.hardwareProperties.Makelangelo2Properties;
import com.marginallyclever.makelangelo.robot.hardwareProperties.HardwareProperties;
import com.marginallyclever.util.PreferencesHelper;


/**
 * All the hardware settings for a single plotter robot.  Does not store state information.  
 * @author Dan Royer
 * @since before 7.25.0
 */
public final class Plotter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4185946661019573192L;

	public static final String COMMAND_MOVE = "G0";
	public static final String COMMAND_TRAVEL = "G1";
	
	private String[] configsAvailable;

	// Each robot has a global unique identifier
	private long robotUID;
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered;
	// machine physical limits, in mm
	private double limitLeft;
	private double limitRight;
	private double limitBottom;
	private double limitTop;
	
	private String hardwareVersion;
	private HardwareProperties hardwareProperties;

	private ColorRGB penDownColor;
	private ColorRGB penUpColor;
	
	// speed control
	private double feedRateMax;
	private double feedRateDefault;
	private double accelerationMax;

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

	
	/**
	 * These values should match https://github.com/marginallyclever/makelangelo-firmware/firmware_rumba/configure.h
	 */
	public Plotter() {				
		super();
		
		robotUID = 0;
		isRegistered = false;
		
		double mh = 835; // mm
		double mw = 835; // mm
		limitTop = mh/2;
		limitBottom = -mh/2;
		limitRight = mw/2;
		limitLeft = -mw/2;

		penDownColor = new ColorRGB(0,0,0); // BLACK
		penUpColor = new ColorRGB(0,255,0); // blue
		startingPositionIndex = 4;

		// default hardware version
		setHardwareVersion("5");

		// which configurations are available?
		try {
			Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
			configsAvailable = topLevelMachinesPreferenceNode.childrenNames();
		} catch (Exception e) {
			Log.error( e.getMessage() );
			configsAvailable = new String[1];
			configsAvailable[0] = "Default";
		}
		
		// Load most recent config
		//loadConfig(last_machine_id);
	}
		
	public void createNewUID(long newUID) {
		// make sure a topLevelMachinesPreferenceNode node is created
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
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

	private Point2D getHome() {
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
	
	public long getUID() {
		return robotUID;
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
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		limitTop    = Double.valueOf(uniqueMachinePreferencesNode.get("limit_top", Double.toString(limitTop)));
		limitBottom = Double.valueOf(uniqueMachinePreferencesNode.get("limit_bottom", Double.toString(limitBottom)));
		limitLeft   = Double.valueOf(uniqueMachinePreferencesNode.get("limit_left", Double.toString(limitLeft)));
		limitRight  = Double.valueOf(uniqueMachinePreferencesNode.get("limit_right", Double.toString(limitRight)));

		accelerationMax=Double.valueOf(uniqueMachinePreferencesNode.get("acceleration",Double.toString(accelerationMax)));

		startingPositionIndex = Integer.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex",Integer.toString(startingPositionIndex)));

		//setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool", Integer.toString(getCurrentToolNumber()))));
		setRegistered(Boolean.parseBoolean(uniqueMachinePreferencesNode.get("isRegistered",Boolean.toString(isRegistered))));

		loadPenConfig(uniqueMachinePreferencesNode);

		setHardwareVersion(uniqueMachinePreferencesNode.get("hardwareVersion", hardwareVersion));
	}

	protected void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setPenDiameter(Double.parseDouble(prefs.get("diameter", Double.toString(getPenDiameter()))));
		setZFeedrate(Double.parseDouble(prefs.get("z_rate", Double.toString(getZFeedrate()))));
		setZOn(Double.parseDouble(prefs.get("z_on", Double.toString(getZOn()))));
		setZOff(Double.parseDouble(prefs.get("z_off", Double.toString(getZOff()))));
		//tool_number = Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
		feedRateMax = Double.parseDouble(prefs.get("feed_rate", Double.toString(feedRateMax)));
		feedRateDefault=Double.valueOf(prefs.get("feed_rate_current",Double.toString(feedRateDefault)));
		
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
		prefs.put("diameter", Double.toString(getPenDiameter()));
		prefs.put("z_rate", Double.toString(getZFeedrate()));
		prefs.put("z_on", Double.toString(getZOn()));
		prefs.put("z_off", Double.toString(getZOff()));
		//prefs.put("tool_number", Integer.toString(toolNumber));
		prefs.put("feed_rate", Double.toString(feedRateMax));
		prefs.put("feed_rate_current", Double.toString(feedRateDefault));
		prefs.putInt("penUpColorR", penUpColor.getRed());
		prefs.putInt("penUpColorG", penUpColor.getGreen());
		prefs.putInt("penUpColorB", penUpColor.getBlue());
	}
	
	// Save the machine configuration
	public void saveConfig() {
		// once cloud logic is finished.
		//if(GetCanUseCloud() && SaveConfigToCloud() ) return;
		saveConfigToLocal();
	}
	
	protected void saveConfigToLocal() {
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		uniqueMachinePreferencesNode.put("limit_top", Double.toString(limitTop));
		uniqueMachinePreferencesNode.put("limit_bottom", Double.toString(limitBottom));
		uniqueMachinePreferencesNode.put("limit_right", Double.toString(limitRight));
		uniqueMachinePreferencesNode.put("limit_left", Double.toString(limitLeft));
		uniqueMachinePreferencesNode.put("acceleration", Double.toString(accelerationMax));
		uniqueMachinePreferencesNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

		//uniqueMachinePreferencesNode.put("current_tool", Integer.toString(getCurrentToolNumber()));
		uniqueMachinePreferencesNode.put("isRegistered", Boolean.toString(isRegistered()));
		
		uniqueMachinePreferencesNode.put("hardwareVersion", hardwareVersion);

		savePenConfig(uniqueMachinePreferencesNode);
	}
	
	public void setAcceleration(double f) {
		accelerationMax = f;
	}
	
	public void setTravelFeedRate(double f) {
		feedRateMax = Math.max(f,0.001);
	}
	
	public double getTravelFeedRate() {
		return feedRateMax;
	}
	
	public void setDrawingFeedRate(double f) {
		feedRateDefault = Math.max(f,0.001);
	}
	
	public double getDrawingFeedRate() {
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
	
	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
	

	public String getHardwareVersion() {
		return hardwareVersion;
	}

	public HardwareProperties getHardwareProperties() {
		return hardwareProperties;
	}

	public void setHardwareVersion(String version) {
		String newVersion = "";

		try {
			// get version numbers
			ServiceLoader<HardwareProperties> knownHardware = ServiceLoader.load(HardwareProperties.class);
			Iterator<HardwareProperties> i = knownHardware.iterator();
			while(i.hasNext()) {
				HardwareProperties hw = i.next();
				if(hw.getVersion().equals(version)) {
					hardwareProperties = hw.getClass().getDeclaredConstructor().newInstance();
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
		
		setZFeedrate(hardwareProperties.getZRate());
		setZOn(hardwareProperties.getZAngleOn());
		setZOff(hardwareProperties.getZAngleOff());

		// pen
		setPenDiameter(0.8f);
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

	public void writeProgramStart(Writer out) throws IOException {	
		hardwareProperties.writeProgramStart(out);
	}
	
	public void writeProgramEnd(Writer out) throws IOException {	
		hardwareProperties.writeProgramEnd(out);
	}
	
	public String getPenDownString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getZFeedrate()) + " Z" + StringHelper.formatDouble(getPenDownAngle());
	}

	public String getPenUpString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getZFeedrate()) + " Z" + StringHelper.formatDouble(getPenUpAngle());
	}
	
	public String getTravelFeedrateString() {
		return COMMAND_TRAVEL+" F" + StringHelper.formatDouble(getTravelFeedRate());
	}
	
	public String getDrawingFeedrateString() {
		return COMMAND_MOVE+" F" + StringHelper.formatDouble(getDrawingFeedRate());
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
		out.write("G00 F" + StringHelper.formatDouble(getTravelFeedRate()) + 
					 " A" + StringHelper.formatDouble(getAcceleration()) + "\n");
	}
	
	// 7.22.6: feedrate changes here
	public void writeMoveTo(Writer out, double x, double y,boolean isUp,boolean zMoved) throws IOException {
		String command = isUp?COMMAND_TRAVEL:COMMAND_MOVE;
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
		this.zFeedrate = arg0;
	}
	
	public double getZFeedrate() {
		return zFeedrate;
	}
}
