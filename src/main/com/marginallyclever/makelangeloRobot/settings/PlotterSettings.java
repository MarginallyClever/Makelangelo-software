package com.marginallyclever.makelangeloRobot.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.settings.plotterTypes.Makelangelo2;
import com.marginallyclever.makelangeloRobot.settings.plotterTypes.PlotterType;
import com.marginallyclever.makelangeloRobot.settings.plotterTypes.PlotterTypeFactory;
import com.marginallyclever.util.PreferencesHelper;

/**
 * All the hardware settings for a single plotter robot. Does not store state information.
 * 
 * @author Dan Royer 
 */
public class PlotterSettings implements Serializable {
	private static final long serialVersionUID = -4185946661019573192L;

	public static final String COMMAND_DRAW = "G1";
	public static final String COMMAND_TRAVEL = "G0";

	private String[] configsAvailable;

	private ArrayList<PlotterSettingsListener> listeners;

	// Each robot has a global unique identifier
	private long robotUID;
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered;

	private String hardwareVersion;
	
	private PlotterType hardwareProperties;
	
	// machine physical limits, in mm
	private double limitLeft;
	private double limitRight;
	private double limitBottom;
	private double limitTop;
	
	// speed control
	private double travelFeedRate;
	private double drawFeedRate;
	private double maxAcceleration;

	// for a while the robot would sign it's name at the end of a drawing
	@Deprecated
	private boolean shouldSignName;

	private ColorRGB paperColor;
	
	private ColorRGB penDownColorDefault;
	private ColorRGB penDownColor;
	private ColorRGB penUpColor;

	private double penDiameter; // mm, >0
	private double penUpAngle; // servo angle (degrees,0...180)
	private double penDownAngle; // servo angle (degrees,0...180)
	private double penLiftTime; // ms

	/**
	 * top left, bottom center, etc...
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	private String[] startingStrings = { "Top Left", "Top Center", "Top Right", "Left", "Center", "Right",
	 * 			"Bottom Left", "Bottom Center", "Bottom Right" };
	 * }
	 * </pre>
	 */
	private int startingPositionIndex;

	/**
	 * These values should match
	 * https://github.com/marginallyclever/makelangelo-firmware/firmware_rumba/configure.h
	 */
	public PlotterSettings() {
		double mh = 835; // mm
		double mw = 835; // mm

		robotUID = 0;
		isRegistered = false;
		limitTop = mh / 2;
		limitBottom = -mh / 2;
		limitRight = mw / 2;
		limitLeft = -mw / 2;

		paperColor = new ColorRGB(255, 255, 255);

		listeners = new ArrayList<PlotterSettingsListener>();
		shouldSignName = false;

		penDownColor = penDownColorDefault = new ColorRGB(0, 0, 0); // BLACK
		penUpColor = new ColorRGB(0, 255, 0); // blue
		startingPositionIndex = 4;

		setHardwareVersion("5");

		// which configurations are available?
		try {
			Preferences topLevelMachinesPreferenceNode = PreferencesHelper
					.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
			configsAvailable = topLevelMachinesPreferenceNode.childrenNames();
		} catch (Exception e) {
			Log.error(e.getMessage());
			configsAvailable = new String[1];
			configsAvailable[0] = "Default";
		}

		// Load most recent config
		// loadConfig(last_machine_id);
	}

	public void addListener(PlotterSettingsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlotterSettingsListener listener) {
		listeners.remove(listener);
	}

	public void notifyListeners() {
		for (PlotterSettingsListener listener : listeners) {
			listener.settingsChangedEvent(this);
		}
	}

	public void createNewUID(long newUID) {
		// make sure a topLevelMachinesPreferenceNode node is created
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper
				.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		topLevelMachinesPreferenceNode.node(Long.toString(newUID));

		// if this is a new robot UID, update the list of available configurations
		final String[] new_list = new String[configsAvailable.length + 1];
		System.arraycopy(configsAvailable, 0, new_list, 0, configsAvailable.length);
		new_list[configsAvailable.length] = Long.toString(newUID);
		configsAvailable = new_list;
	}

	public double getMaxAcceleration() {
		return maxAcceleration;
	}

	/**
	 * Get the UID of every machine this computer recognizes INCLUDING machine 0,
	 * which is only assigned temporarily when a machine is new or before the first
	 * software connect.
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

	@Deprecated
	public String getGCodeTeleportToHomePosition() {
		return "G92 X" + getHomeX() + " Y" + getHomeY();
	}

	// return the strings that will tell a makelangelo robot its physical limits.
	public String getGCodeConfig() {
		return getHardwareProperties().getGCodeConfig(this);
	}

	@Deprecated
	public String getAbsoluteMode() {
		return "G90\n";
	}

	@Deprecated
	public String getRelativeMode() {
		return "G91\n";
	}

	public int getKnownMachineIndex() {
		String[] list = getKnownMachineNames();
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals("0"))
				continue;
			if (list[i].equals(Long.toString(robotUID))) {
				return i;
			}
		}

		return -1;
	}

	boolean isNumeric(String s) {
		try {
			Integer.parseInt(s);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Get the UID of every machine this computer recognizes EXCEPT machine 0, which
	 * is only assigned temporarily when a machine is new or before the first
	 * software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getKnownMachineNames() {
		List<String> knownMachineList = new LinkedList<String>(Arrays.asList(configsAvailable));
		List<String> keepList = new LinkedList<String>();
		for (String a : knownMachineList) {
			if (a.contentEquals("0"))
				continue;
			if (!isNumeric(a))
				continue;
			keepList.add(a);
		}

		return Arrays.copyOf(keepList.toArray(), keepList.size(), String[].class);
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

		Preferences topLevelMachinesPreferenceNode = PreferencesHelper
				.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		limitTop = Double.valueOf(uniqueMachinePreferencesNode.get("limit_top", Double.toString(limitTop)));
		limitBottom = Double.valueOf(uniqueMachinePreferencesNode.get("limit_bottom", Double.toString(limitBottom)));
		limitLeft = Double.valueOf(uniqueMachinePreferencesNode.get("limit_left", Double.toString(limitLeft)));
		limitRight = Double.valueOf(uniqueMachinePreferencesNode.get("limit_right", Double.toString(limitRight)));

		maxAcceleration = Float
				.valueOf(uniqueMachinePreferencesNode.get("acceleration", Double.toString(maxAcceleration)));

		startingPositionIndex = Integer
				.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex", Integer.toString(startingPositionIndex)));

		int r, g, b;
		r = uniqueMachinePreferencesNode.getInt("paperColorR", paperColor.getRed());
		g = uniqueMachinePreferencesNode.getInt("paperColorG", paperColor.getGreen());
		b = uniqueMachinePreferencesNode.getInt("paperColorB", paperColor.getBlue());
		paperColor = new ColorRGB(r, g, b);

		// setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool",
		// Integer.toString(getCurrentToolNumber()))));
		setRegistered(Boolean.parseBoolean(uniqueMachinePreferencesNode.get("isRegistered", Boolean.toString(isRegistered))));

		loadPenConfig(uniqueMachinePreferencesNode);
		setHardwareVersion(uniqueMachinePreferencesNode.get("hardwareVersion", hardwareVersion));
	}

	protected void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setPenDiameter(Double.valueOf(prefs.get("diameter", Double.toString(getPenDiameter()))));
		setPenLiftTime(Double.valueOf(prefs.get("z_rate", Double.toString(getPenLiftTime()))));
		setPenDownAngle(Double.valueOf(prefs.get("z_on", Double.toString(getPenDownAngle()))));
		setPenUpAngle(Double.valueOf(prefs.get("z_off", Double.toString(getPenUpAngle()))));
		// tool_number =
		// Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
		travelFeedRate = Double.valueOf(prefs.get("feed_rate", Double.toString(travelFeedRate)));
		drawFeedRate = Double.valueOf(prefs.get("feed_rate_current", Double.toString(drawFeedRate)));

		int r, g, b;
		r = prefs.getInt("penDownColorR", penDownColor.getRed());
		g = prefs.getInt("penDownColorG", penDownColor.getGreen());
		b = prefs.getInt("penDownColorB", penDownColor.getBlue());
		penDownColor = penDownColorDefault = new ColorRGB(r, g, b);
		r = prefs.getInt("penUpColorR", penUpColor.getRed());
		g = prefs.getInt("penUpColorG", penUpColor.getGreen());
		b = prefs.getInt("penUpColorB", penUpColor.getBlue());
		penUpColor = new ColorRGB(r, g, b);
	}

	protected void savePenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		prefs.put("diameter", Double.toString(getPenDiameter()));
		prefs.put("z_rate", Double.toString(getPenLiftTime()));
		prefs.put("z_on", Double.toString(getPenDownAngle()));
		prefs.put("z_off", Double.toString(getPenUpAngle()));
		// prefs.put("tool_number", Integer.toString(toolNumber));
		prefs.put("feed_rate", Double.toString(travelFeedRate));
		prefs.put("feed_rate_current", Double.toString(drawFeedRate));
		prefs.putInt("penDownColorR", penDownColorDefault.getRed());
		prefs.putInt("penDownColorG", penDownColorDefault.getGreen());
		prefs.putInt("penDownColorB", penDownColorDefault.getBlue());
		prefs.putInt("penUpColorR", penUpColor.getRed());
		prefs.putInt("penUpColorG", penUpColor.getGreen());
		prefs.putInt("penUpColorB", penUpColor.getBlue());
	}

	public void saveConfig() {
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper
				.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		uniqueMachinePreferencesNode.put("limit_top", Double.toString(limitTop));
		uniqueMachinePreferencesNode.put("limit_bottom", Double.toString(limitBottom));
		uniqueMachinePreferencesNode.put("limit_right", Double.toString(limitRight));
		uniqueMachinePreferencesNode.put("limit_left", Double.toString(limitLeft));
		uniqueMachinePreferencesNode.put("acceleration", Double.toString(maxAcceleration));
		uniqueMachinePreferencesNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

		uniqueMachinePreferencesNode.putInt("paperColorR", paperColor.getRed());
		uniqueMachinePreferencesNode.putInt("paperColorG", paperColor.getGreen());
		uniqueMachinePreferencesNode.putInt("paperColorB", paperColor.getBlue());

		// uniqueMachinePreferencesNode.put("current_tool",
		// Integer.toString(getCurrentToolNumber()));
		uniqueMachinePreferencesNode.put("isRegistered", Boolean.toString(isRegistered()));

		uniqueMachinePreferencesNode.put("hardwareVersion", hardwareVersion);

		savePenConfig(uniqueMachinePreferencesNode);
		notifyListeners();
	}

	public void setAcceleration(double f) {
		maxAcceleration = f;
	}

	public void setTravelFeedRate(double f) {
		if (f < 0.001)
			f = 0.001f;
		travelFeedRate = f;

		if (drawFeedRate > travelFeedRate)
			drawFeedRate = travelFeedRate;
	}

	public double getTravelFeedRate() {
		return travelFeedRate;
	}

	public double getDrawFeedRate() {
		return drawFeedRate;
	}

	public void setDrawFeedRate(double f) {
		if (f < 0.001)
			f = 0.001f;
		if (f > travelFeedRate)
			f = travelFeedRate;
		drawFeedRate = f;
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
		this.limitLeft = -width / 2.0;
		this.limitRight = width / 2.0;
		this.limitBottom = -height / 2.0;
		this.limitTop = height / 2.0;
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

	public PlotterType getHardwareProperties() {
		return hardwareProperties;
	}

	public void setHardwareVersion(String version) {
		String newVersion = "";
		try {
			// get version numbers
			Iterator<PlotterType> i = PlotterTypeFactory.iterator();
			while (i.hasNext()) {
				PlotterType hw = i.next();
				if (hw.getVersion().contentEquals(version)) {
					hardwareProperties = hw.getClass().getDeclaredConstructor().newInstance();
					newVersion = version;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error("Hardware version instance failed. Defaulting to v2");
			hardwareProperties = new Makelangelo2();
			newVersion = "2";
		}
		if (newVersion == "") {
			Log.error("Unknown hardware version requested. Defaulting to v2");
			hardwareProperties = new Makelangelo2();
		}

		hardwareVersion = newVersion;
		if (!hardwareProperties.canChangeMachineSize()) {
			this.setMachineSize(hardwareProperties.getWidth(), hardwareProperties.getHeight());
		}

		// apply default hardware values
		travelFeedRate = hardwareProperties.getFeedrateMax();
		drawFeedRate = hardwareProperties.getFeedrateDefault();
		maxAcceleration = hardwareProperties.getAccelerationMax();

		setPenLiftTime(hardwareProperties.getPenLiftTime());
		setPenDownAngle(hardwareProperties.getZAngleOn());
		setPenUpAngle(hardwareProperties.getZAngleOff());

		// pen
		setPenDiameter(0.8f);
	}

	public ColorRGB getPenDownColorDefault() {
		return penDownColorDefault;
	}

	public ColorRGB getPenDownColor() {
		return penDownColor;
	}

	public void setPenDownColorDefault(ColorRGB arg0) {
		penDownColorDefault = arg0;
	}

	public void setPenDownColor(ColorRGB arg0) {
		penDownColor = arg0;
	}

	public void setPenUpColor(ColorRGB arg0) {
		penUpColor = arg0;
	}

	public ColorRGB getPenUpColor() {
		return penUpColor;
	}

	@Deprecated
	public String getProgramStart() {
		String a = StringHelper.formatDouble(getMaxAcceleration());
		String j = "3";
		return hardwareProperties.getProgramStart() + "M201 X" + a + " Y" + a + "\n" // acceleration
				+ "M205 X" + j + " Y" + j + "\n"; // jerk
	}

	@Deprecated
	public String getProgramEnd() {
		return hardwareProperties.getProgramEnd();
	}

	@Deprecated
	public String getPenDownString() {
		return "M280 P0 T" + getPenLiftTime() + " S" + StringHelper.formatDouble(getPenDownAngle()) + "\n";
	}

	@Deprecated
	public String getPenUpString() {
		return "M280 P0 T100 S" + StringHelper.formatDouble(getPenUpAngle()) + "\n";
	}

	@Deprecated
	public String getTravelFeedrateString() {
		return "F" + StringHelper.formatDouble(travelFeedRate) + "\n";
	}

	@Deprecated
	public String getDrawFeedrateString() {
		return "F" + StringHelper.formatDouble(drawFeedRate) + "\n";
	}

	@Deprecated
	public String getChangeTool(ColorRGB newPenDownColor) {
		penDownColor = newPenDownColor;
		return getChangeToolInternal();
	}

	@Deprecated
	public String getChangeToDefaultColor() {
		penDownColor = penDownColorDefault;
		return getChangeToolInternal();
	}

	@Deprecated
	protected String getChangeToolInternal() {
		int toolNumber = penDownColor.toInt() & 0xffffff; // ignore alpha channel

		String name = "";
		switch (toolNumber) {
		case 0xff0000:			name = "red";			break;
		case 0x00ff00:			name = "green";			break;
		case 0x0000ff:			name = "blue";			break;
		case 0x000000:			name = "black";			break;
		case 0x00ffff:			name = "cyan";			break;
		case 0xff00ff:			name = "magenta";		break;
		case 0xffff00:			name = "yellow";		break;
		case 0xffffff:			name = "white";			break;
		default:
			name = "0x" + Integer.toHexString(toolNumber);
			break; // display unknown RGB value as hex
		}

		return "M06 T" + toolNumber + "\n" + "M0 Ready " + name + " and click\n";
	}

	@Deprecated
	public String getMoveTo(double x, double y, boolean isUp, boolean zMoved) {
		String command = isUp ? COMMAND_TRAVEL : COMMAND_DRAW;
		if (zMoved) {
			command += " " + (isUp ? getTravelFeedrateString() : getDrawFeedrateString());
		}
		return command 
				+ " X" + StringHelper.formatDouble(x) 
				+ " Y" + StringHelper.formatDouble(y) + "\n";
	}

	public void setPenDiameter(double d) {
		penDiameter = d;
	}

	public double getPenDiameter() {
		return penDiameter;
	}

	public double getPenUpAngle() {
		return penUpAngle;
	}

	public void setPenUpAngle(double angle) {
		this.penUpAngle = angle;
	}

	public double getPenDownAngle() {
		return penDownAngle;
	}

	public void setPenDownAngle(double angle) {
		this.penDownAngle = angle;
	}

	public double getPenLiftTime() {
		return penLiftTime;
	}

	public void setPenLiftTime(double ms) {
		this.penLiftTime = ms;
	}
}
