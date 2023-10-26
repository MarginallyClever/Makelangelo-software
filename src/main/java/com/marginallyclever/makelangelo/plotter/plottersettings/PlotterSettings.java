package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.util.PreferencesHelper;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * {@link PlotterSettings} stores the physical settings for a single {@link com.marginallyclever.makelangelo.plotter.Plotter}.
 * Not to be confused with the dynamic state of a {@link com.marginallyclever.makelangelo.plotter.Plotter}.
 * @author Dan Royer
 */
public class PlotterSettings {
	/**
	 * mm/s/s
	 */
	public static final String ACCELERATION = "acceleration";
	/**
	 * int
	 */
	public static final String BLOCK_BUFFER_SIZE = "blockBufferSize";
	/**
	 * double
	 */
	public static final String DIAMETER = "diameter";
	/**
	 * double
	 */
	public static final String FEED_RATE_TRAVEL = "feed_rate";
	/**
	 * double
	 */
	public static final String FEED_RATE_DRAW = "feed_rate_current";
	/**
	 * boolean
	 */
	public static final String HANDLE_SMALL_SEGMENTS = "handleSmallSegments";
	/**
	 * String
	 */
	public static final String HARDWARE_VERSION = "hardwareVersion";
	/**
	 * boolean.  if we wanted to test for Marginally Clever brand Makelangelo robots
	 */
	public static final String IS_REGISTERED = "isRegistered";
	/**
	 * double
	 */
	public static final String LIMIT_BOTTOM = "limit_bottom";
	/**
	 * double
	 */
	public static final String LIMIT_LEFT = "limit_left";
	/**
	 * double
	 */
	public static final String LIMIT_RIGHT = "limit_right";
	/**
	 * double
	 */
	public static final String LIMIT_TOP = "limit_top";
	/**
	 * array of doubles
	 */
	public static final String MAX_JERK = "maxJerk";
	/**
	 * double
	 */
	public static final String MINIMUM_PLANNER_SPEED = "minimumPlannerSpeed";
	/**
	 * double
	 */
	public static final String MIN_ACCELERATION = "minAcceleration";
	/**
	 * double
	 */
	public static final String MIN_SEGMENT_LENGTH = "minSegmentLength";
	public static final String MIN_SEG_TIME = "minSegTime";
	/**
	 * color
	 */
	public static final String PAPER_COLOR = "paperColor";
	/**
	 * color
	 */
	public static final String PEN_DOWN_COLOR = "penDownColor";
	/**
	 * color
	 */
	public static final String PEN_DOWN_COLOR_DEFAULT = "penDownColorDefault";
	/**
	 * color
	 */
	public static final String PEN_UP_COLOR = "penUpColor";
	/**
	 * integer
	 */
	public static final String SEGMENTS_PER_SECOND = "segmentsPerSecond";
	/**
	 * integer.
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	private String[] startingStrings = { "Top Left", "Top Center", "Top Right", "Left", "Center", "Right",
	 * 			"Bottom Left", "Bottom Center", "Bottom Right" };
	 * }
	 * </pre>
	 */
	public static final String STARTING_POS_INDEX = "startingPosIndex";
	/**
	 * double
	 */
	public static final String PEN_ANGLE_UP = "z_off";
	/**
	 * double
	 */
	public static final String PEN_ANGLE_DOWN = "z_on";
	/**
	 * double.
	 * The milliseconds delay to raise the pen.  Marlin firmware will send intermediate values to the servo
	 * to approximate the slow movement, but it may cause the servo to jitter.  Use with care.
	 */
	public static final String PEN_ANGLE_UP_TIME = "z_rate_up";
	/**
	 * double.
	 * The milliseconds delay to lower the pen.  Marlin firmware will send intermediate values to the servo
	 * to approximate the slow movement, but it may cause the servo to jitter.  Use with care.
	 */
	public static final String PEN_ANGLE_DOWN_TIME = "z_rate";
	/**
	 * String
	 */
	public static final String USER_GENERAL_END_GCODE = "userGeneralEndGcode";
	/**
	 * String
	 */
	public static final String USER_GENERAL_START_GCODE = "userGeneralStartGcode";
	/**
	 * integer
	 */
	public static final String Z_MOTOR_TYPE = "zMotorType";
	/**
	 * String
	 */
	public static final String STYLE = "style";

	public static final int Z_MOTOR_TYPE_SERVO = 1;
	public static final int Z_MOTOR_TYPE_STEPPER = 2;

	private String robotUID = "0";
	private double [] maxJerk = { 10, 10, 0.3 };

	public PlotterSettings() {
		super();
	}

	public PlotterSettings(String UID) {
		super();
		load(UID);
	}

	public String getUID() {
		return robotUID;
	}

	protected void setRobotUID(String robotUID) {
		this.robotUID = robotUID;
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public double getDouble(String key) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		return thisMachineNode.getDouble(key, 0);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public int getInteger(String key) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		return thisMachineNode.getInt(key, 0);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public String getString(String key) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		return thisMachineNode.get(key, "");
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public boolean getBoolean(String key) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		return thisMachineNode.getBoolean(key, false);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public ColorRGB getColor(String key) throws NullPointerException, IllegalStateException {
		int v = getInteger(key);
		return new ColorRGB(v);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setDouble(String key,double value) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		thisMachineNode.putDouble(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setInteger(String key,int value) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		thisMachineNode.putInt(key,value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setString(String key,String value) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		thisMachineNode.put(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setBoolean(String key,boolean value) throws NullPointerException, IllegalStateException {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		thisMachineNode.putBoolean(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setColor(String key,ColorRGB value) throws NullPointerException, IllegalStateException {
		setInteger(key,value.toInt());
	}

	public Point2D getHome() {
		return new Point2D(0,0);
	}

	/**
	 * Load the machine configuration from {@link Preferences}.
	 * @param uid the unique id of the robot to be loaded
	 */
	public void load(String uid) {
		robotUID = uid;
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		Preferences jerkNode = thisMachineNode.node(PlotterSettings.MAX_JERK);
		for(int i=0;i<maxJerk.length;i++) {
			maxJerk[i] = jerkNode.getDouble(Integer.toString(i), maxJerk[i]);
		}
	}

	/**
	 * Save the machine configuration to {@link Preferences}.  The preference node will be the unique id of the robot.
	 */
	public void save() {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);
		Preferences jerkNode = thisMachineNode.node(PlotterSettings.MAX_JERK);
		for(int i=0;i<maxJerk.length;i++) {
			jerkNode.putDouble(Integer.toString(i), maxJerk[i] );
		}
	}

	// TODO reset to what, exactly?
	public void reset() {
		PlotterSettings ps = new PlotterSettings();
		ps.save();
		load(getUID());
	}

	/**
	 * @param width mm
	 * @param height mm
	 */
	public void setMachineSize(double width, double height) {
		setDouble(PlotterSettings.LIMIT_LEFT,-width / 2.0);
		setDouble(PlotterSettings.LIMIT_RIGHT,width / 2.0);
		setDouble(PlotterSettings.LIMIT_BOTTOM,-height / 2.0);
		setDouble(PlotterSettings.LIMIT_TOP,height / 2.0);
	}

	/**
	 * @param maxJerk the maxJerk to set
	 */
	public void setMaxJerk(double[] maxJerk) {
		this.maxJerk = maxJerk;
	}

	/**
	 * @return the maxJerk
	 */
	public double[] getMaxJerk() {
		return maxJerk;
	}

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("robotUID", robotUID);		
		json.put("isRegistered", getBoolean(IS_REGISTERED));
		json.put("hardwareName", getString(HARDWARE_VERSION));
		json.put("limitLeft", getDouble(PlotterSettings.LIMIT_LEFT));	// mm
		json.put("limitRight", getDouble(PlotterSettings.LIMIT_RIGHT));	// mm
		json.put("limitBottom", getDouble(PlotterSettings.LIMIT_BOTTOM));	// mm
		json.put("limitTop", getDouble(PlotterSettings.LIMIT_TOP));		// mm
		json.put("blockBufferSize", getInteger(PlotterSettings.BLOCK_BUFFER_SIZE));
		json.put("segmentsPerSecond", getInteger(PlotterSettings.SEGMENTS_PER_SECOND));
		json.put("minSegmentLength", getDouble(PlotterSettings.MIN_SEGMENT_LENGTH));	// mm
		json.put("minSegTime", getInteger(PlotterSettings.MIN_SEG_TIME));		// us
		json.put("handleSmallSegments", getBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS));
		json.put("maxAcceleration", getDouble(PlotterSettings.ACCELERATION));	// mm/s/s
		json.put("minAcceleration", getDouble(PlotterSettings.MIN_ACCELERATION));	// mm/s/s
		json.put("minimumPlannerSpeed", getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED));	// mm/s
		json.put("maxJerk", Arrays.toString(maxJerk));
		json.put("paperColor", getColor(PlotterSettings.PAPER_COLOR));
		json.put("penDownColorDefault", getColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT));
		json.put("penDownColor", getColor(PlotterSettings.PEN_DOWN_COLOR));
		json.put("penUpColor", getColor(PlotterSettings.PEN_UP_COLOR));
		json.put("penDiameter", getDouble(PlotterSettings.DIAMETER));	// mm, >0
		json.put("penUpAngle", getDouble(PlotterSettings.PEN_ANGLE_UP));	// servo angle (degrees,0...180)
		json.put("penDownAngle", getDouble(PlotterSettings.PEN_ANGLE_DOWN));	// servo angle (degrees,0...180)
		json.put("penLiftTime", getDouble(PlotterSettings.PEN_ANGLE_UP_TIME));
		json.put("penLowerTime", getDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME));
		json.put("startingPositionIndex", getInteger(STARTING_POS_INDEX));
		json.put("userGeneralStartGcode", getString(USER_GENERAL_START_GCODE));
		json.put("userGeneralEndGcode", getString(USER_GENERAL_END_GCODE));
		json.put("zMotorType", getInteger(Z_MOTOR_TYPE));
		json.put("style", getString(STYLE));
		json.put("travelFeedRate", getDouble(PlotterSettings.FEED_RATE_TRAVEL));	// mm/min.  3000 = 50 mm/s
		json.put("drawFeedRate", getDouble(PlotterSettings.FEED_RATE_DRAW));	// mm/min.  3000 = 50 mm/s
		return json.toString();
	}
}