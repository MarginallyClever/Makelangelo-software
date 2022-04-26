package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.util.PreferencesHelper;
import java.io.Serializable;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * {@link PlotterSettings} stores the customized settings for a single plotter robot.
 * {@link com.marginallyclever.makelangelo.plotter.Plotter} stores the rapidly changing state information (while drawing).
 * @author Dan Royer
 */
public class PlotterSettings implements Serializable {
	private static final long serialVersionUID = -4185946661019573192L;

	private static final String PREF_KEY_ACCELERATION = "acceleration";
	private static final String PREF_KEY_BLOCK_BUFFER_SIZE = "blockBufferSize";
	private static final String PREF_KEY_DIAMETER = "diameter";
	private static final String PREF_KEY_FEED_RATE = "feed_rate";
	private static final String PREF_KEY_FEED_RATE_CURRENT = "feed_rate_current";
	private static final String PREF_KEY_HANDLE_SMALL_SEGMENTS = "handleSmallSegments";
	private static final String PREF_KEY_HARDWARE_VERSION = "hardwareVersion";
	private static final String PREF_KEY_IS_REGISTERED = "isRegistered";
	private static final String PREF_KEY_LIMIT_BOTTOM = "limit_bottom";
	private static final String PREF_KEY_LIMIT_LEFT = "limit_left";
	private static final String PREF_KEY_LIMIT_RIGHT = "limit_right";
	private static final String PREF_KEY_LIMIT_TOP = "limit_top";
	private static final String PREF_KEY_MAX_JERK = "maxJerk";
	private static final String PREF_KEY_MINIMUM_PLANNER_SPEED = "minimumPlannerSpeed";
	private static final String PREF_KEY_MIN_ACCELERATION = "minAcceleration";
	private static final String PREF_KEY_MIN_SEGMENT_LENGTH = "minSegmentLength";
	private static final String PREF_KEY_MIN_SEG_TIME = "minSegTime";
	private static final String PREF_KEY_PAPER_COLOR_B = "paperColorB";
	private static final String PREF_KEY_PAPER_COLOR_G = "paperColorG";
	private static final String PREF_KEY_PAPER_COLOR_R = "paperColorR";
	private static final String PREF_KEY_PEN = "Pen";
	private static final String PREF_KEY_PEN_DOWN_COLOR_B = "penDownColorB";
	private static final String PREF_KEY_PEN_DOWN_COLOR_G = "penDownColorG";
	private static final String PREF_KEY_PEN_DOWN_COLOR_R = "penDownColorR";
	private static final String PREF_KEY_PEN_UP_COLOR_B = "penUpColorB";
	private static final String PREF_KEY_PEN_UP_COLOR_G = "penUpColorG";
	private static final String PREF_KEY_PEN_UP_COLOR_R = "penUpColorR";
	private static final String PREF_KEY_SEGMENTS_PER_SECOND = "segmentsPerSecond";
	private static final String PREF_KEY_STARTING_POS_INDEX = "startingPosIndex";
	private static final String PREF_KEY_Z_OFF = "z_off";
	private static final String PREF_KEY_Z_ON = "z_on";
	private static final String PREF_KEY_Z_RATE = "z_rate";

	// Each robot has a global unique identifier
	private long robotUID = 0;
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered = false;

	private String hardwareName = "Makelangelo 5";

	// machine physical limits, in mm
	private final double machineHeight = 1000; // mm
	private final double machineWidth = 650; // mm

	private double limitLeft = - machineWidth / 2;
	private double limitRight = machineWidth / 2;
	private double limitBottom = - machineHeight / 2;
	private double limitTop = machineHeight / 2;

	// speed control

	// values for {@link MarlinSimulation} that cannot be tweaked in firmware at run time.
	private int blockBufferSize = 16;

	private int segmentsPerSecond = 5;
	private double minSegmentLength = 0.5;  // mm
	private long minSegTime = 20000;  // us
	private boolean handleSmallSegments = false;

	// values for {@link MarlinSimulation} that can be tweaked in firmware at run time.
	private double travelFeedRate = 3000;  // 5400 = 90*60 mm/s
	private double drawFeedRate = 3000;  // 5400 = 90*60 mm/s
	private double maxAcceleration = 100;  // 2400=40*60 mm/s/s
	private double minAcceleration = 0.0;  // mm/s/s
	private double minimumPlannerSpeed = 0.05;  // mm/s
	private double [] maxJerk = { 10, 10, 0.3 };

	private ColorRGB paperColor = new ColorRGB(255, 255, 255);

	private ColorRGB penDownColorDefault = new ColorRGB(0, 0, 0);
	private ColorRGB penDownColor = new ColorRGB(0, 0, 0);
	private ColorRGB penUpColor = new ColorRGB(0, 255, 0);

	private double penDiameter=0.8; // mm, >0
	private double penUpAngle=90; // servo angle (degrees,0...180)
	private double penDownAngle=25; // servo angle (degrees,0...180)
	private double penLiftTime=50; // ms

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
	private int startingPositionIndex = 4;

	public PlotterSettings() {
	}

	// OBSERVER PATTERN START

	private final List<PlotterSettingsListener> listeners = new ArrayList<>();

	public void addPlotterSettingsListener(PlotterSettingsListener listener) {
		listeners.add(listener);
	}

	public void removePlotterSettingsListener(PlotterSettingsListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners() {
		for (PlotterSettingsListener listener : listeners) {
			listener.settingsChangedEvent(this);
		}
	}

	// OBSERVER PATTERN END

	public double getMaxAcceleration() {
		return maxAcceleration;
	}

	public Point2D getHome() {
		return new Point2D(0,0);
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

	protected void setRobotUID(long robotUID) {
		this.robotUID = robotUID;
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	/**
	 * Load the machine configuration from {@link Preferences}.
	 * @param uid the unique id of the robot to be loaded
	 */
	public void loadConfig(long uid) {
		robotUID = uid;

		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(robotUID));

		limitTop 				= thisMachineNode.getDouble(PREF_KEY_LIMIT_TOP, limitTop);
		limitBottom 			= thisMachineNode.getDouble(PREF_KEY_LIMIT_BOTTOM, limitBottom);
		limitLeft 				= thisMachineNode.getDouble(PREF_KEY_LIMIT_LEFT, limitLeft);
		limitRight 				= thisMachineNode.getDouble(PREF_KEY_LIMIT_RIGHT, limitRight);
		maxAcceleration 		= thisMachineNode.getDouble(PREF_KEY_ACCELERATION, maxAcceleration);
		startingPositionIndex 	= thisMachineNode.getInt(PREF_KEY_STARTING_POS_INDEX, startingPositionIndex);

		int r = thisMachineNode.getInt(PREF_KEY_PAPER_COLOR_R, paperColor.getRed());
		int g = thisMachineNode.getInt(PREF_KEY_PAPER_COLOR_G, paperColor.getGreen());
		int b = thisMachineNode.getInt(PREF_KEY_PAPER_COLOR_B, paperColor.getBlue());
		paperColor = new ColorRGB(r, g, b);

		// setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool",
		// Integer.toString(getCurrentToolNumber()))));
		isRegistered = Boolean.parseBoolean(thisMachineNode.get(PREF_KEY_IS_REGISTERED, Boolean.toString(isRegistered)));
		hardwareName = thisMachineNode.get(PREF_KEY_HARDWARE_VERSION, hardwareName);

		blockBufferSize 		= thisMachineNode.getInt(PREF_KEY_BLOCK_BUFFER_SIZE, blockBufferSize);
		segmentsPerSecond 		= thisMachineNode.getInt(PREF_KEY_SEGMENTS_PER_SECOND, segmentsPerSecond);
		minSegmentLength 		= thisMachineNode.getDouble(PREF_KEY_MIN_SEGMENT_LENGTH, minSegmentLength);
		minSegTime 				= thisMachineNode.getLong(PREF_KEY_MIN_SEG_TIME, minSegTime);
		handleSmallSegments 	= thisMachineNode.getBoolean(PREF_KEY_HANDLE_SMALL_SEGMENTS, handleSmallSegments);
		minAcceleration			= thisMachineNode.getDouble(PREF_KEY_MIN_ACCELERATION, minAcceleration);
		minimumPlannerSpeed 	= thisMachineNode.getDouble(PREF_KEY_MINIMUM_PLANNER_SPEED, minimumPlannerSpeed);

		loadJerkConfig(thisMachineNode);
		loadPenConfig(thisMachineNode);
	}

	private void loadJerkConfig(Preferences thisMachineNode) {
		Preferences jerkNode = thisMachineNode.node(PREF_KEY_MAX_JERK);
		for(int i=0;i<maxJerk.length;i++) {
			maxJerk[i] = jerkNode.getDouble(Integer.toString(i), maxJerk[i]);
		}
	}

	private void loadPenConfig(Preferences prefs) {
		prefs = prefs.node(PREF_KEY_PEN);
		setPenDiameter(		prefs.getDouble(PREF_KEY_DIAMETER, penDiameter	));
		setPenLiftTime(		prefs.getDouble(PREF_KEY_Z_RATE, penLiftTime	));
		setPenDownAngle(	prefs.getDouble(PREF_KEY_Z_ON, penDownAngle	));
		setPenUpAngle(		prefs.getDouble(PREF_KEY_Z_OFF, penUpAngle	));
		setTravelFeedRate(	prefs.getDouble(PREF_KEY_FEED_RATE, travelFeedRate));
		setDrawFeedRate(	prefs.getDouble(PREF_KEY_FEED_RATE_CURRENT, drawFeedRate	));
		// tool_number = Integer.valueOf(prefs.get("tool_number",Integer.toString(tool_number)));

		int r, g, b;
		r = prefs.getInt(PREF_KEY_PEN_DOWN_COLOR_R, penDownColor.getRed());
		g = prefs.getInt(PREF_KEY_PEN_DOWN_COLOR_G, penDownColor.getGreen());
		b = prefs.getInt(PREF_KEY_PEN_DOWN_COLOR_B, penDownColor.getBlue());
		penDownColor = penDownColorDefault = new ColorRGB(r, g, b);
		r = prefs.getInt(PREF_KEY_PEN_UP_COLOR_R, penUpColor.getRed());
		g = prefs.getInt(PREF_KEY_PEN_UP_COLOR_G, penUpColor.getGreen());
		b = prefs.getInt(PREF_KEY_PEN_UP_COLOR_B, penUpColor.getBlue());
		penUpColor = new ColorRGB(r, g, b);
	}

	public void saveConfig() {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(robotUID));

		thisMachineNode.put(PREF_KEY_LIMIT_TOP, Double.toString(limitTop));
		thisMachineNode.put(PREF_KEY_LIMIT_BOTTOM, Double.toString(limitBottom));
		thisMachineNode.put(PREF_KEY_LIMIT_RIGHT, Double.toString(limitRight));
		thisMachineNode.put(PREF_KEY_LIMIT_LEFT, Double.toString(limitLeft));
		thisMachineNode.put(PREF_KEY_ACCELERATION, Double.toString(maxAcceleration));
		thisMachineNode.put(PREF_KEY_STARTING_POS_INDEX, Integer.toString(startingPositionIndex));

		thisMachineNode.putInt(PREF_KEY_PAPER_COLOR_R, paperColor.getRed());
		thisMachineNode.putInt(PREF_KEY_PAPER_COLOR_G, paperColor.getGreen());
		thisMachineNode.putInt(PREF_KEY_PAPER_COLOR_B, paperColor.getBlue());

		thisMachineNode.put(PREF_KEY_IS_REGISTERED, Boolean.toString(isRegistered()));
		thisMachineNode.put(PREF_KEY_HARDWARE_VERSION, hardwareName);

		thisMachineNode.putInt(PREF_KEY_BLOCK_BUFFER_SIZE, blockBufferSize);
		thisMachineNode.putInt(PREF_KEY_SEGMENTS_PER_SECOND, segmentsPerSecond);
		thisMachineNode.putDouble(PREF_KEY_MIN_SEGMENT_LENGTH, minSegmentLength);
		thisMachineNode.putLong(PREF_KEY_MIN_SEG_TIME, minSegTime);
		thisMachineNode.putBoolean(PREF_KEY_HANDLE_SMALL_SEGMENTS, handleSmallSegments);
		thisMachineNode.putDouble(PREF_KEY_MIN_ACCELERATION, minAcceleration);
		thisMachineNode.putDouble(PREF_KEY_MINIMUM_PLANNER_SPEED, minimumPlannerSpeed);

		saveJerkConfig(thisMachineNode);
		savePenConfig(thisMachineNode);
		notifyListeners();
	}

	private void saveJerkConfig(Preferences thisMachineNode) {
		Preferences jerkNode = thisMachineNode.node(PREF_KEY_MAX_JERK);
		for(int i=0;i<maxJerk.length;i++) {
			jerkNode.putDouble(Integer.toString(i), maxJerk[i] );
		}
	}

	private void savePenConfig(Preferences prefs) {
		prefs = prefs.node(PREF_KEY_PEN);
		prefs.put(PREF_KEY_DIAMETER, Double.toString(getPenDiameter()));
		prefs.put(PREF_KEY_Z_RATE, Double.toString(getPenLiftTime()));
		prefs.put(PREF_KEY_Z_ON, Double.toString(getPenDownAngle()));
		prefs.put(PREF_KEY_Z_OFF, Double.toString(getPenUpAngle()));
		prefs.put(PREF_KEY_FEED_RATE, Double.toString(travelFeedRate));
		prefs.put(PREF_KEY_FEED_RATE_CURRENT, Double.toString(drawFeedRate));
		prefs.putInt(PREF_KEY_PEN_DOWN_COLOR_R, penDownColorDefault.getRed());
		prefs.putInt(PREF_KEY_PEN_DOWN_COLOR_G, penDownColorDefault.getGreen());
		prefs.putInt(PREF_KEY_PEN_DOWN_COLOR_B, penDownColorDefault.getBlue());
		prefs.putInt(PREF_KEY_PEN_UP_COLOR_R, penUpColor.getRed());
		prefs.putInt(PREF_KEY_PEN_UP_COLOR_G, penUpColor.getGreen());
		prefs.putInt(PREF_KEY_PEN_UP_COLOR_B, penUpColor.getBlue());
	}

	public void reset() {
		PlotterSettings ps = new PlotterSettings();
		ps.saveConfig();
		loadConfig(getUID());
	}

	public void setAcceleration(double f) {
		maxAcceleration = f;
	}

	public void setTravelFeedRate(double f) {
		if(f < 0.001) f = 0.001f;
		travelFeedRate = f;
	}

	public double getTravelFeedRate() {
		return travelFeedRate;
	}

	public double getDrawFeedRate() {
		return drawFeedRate;
	}

	public void setDrawFeedRate(double f) {
		if(f < 0.001) f = 0.001f;
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

	public void setHardwareName(String hardwareName) {
		this.hardwareName = hardwareName;
	}

	public String getHardwareName() {
		return hardwareName;
	}

	public ColorRGB getPaperColor() {
		return paperColor;
	}

	public void setPaperColor(ColorRGB paperColor) {
		this.paperColor = paperColor;
	}

	public ColorRGB getPenDownColorDefault() {
		return penDownColorDefault;
	}

	public ColorRGB getPenDownColor() {
		return penDownColor;
	}

	public void setPenDownColorDefault(ColorRGB color) {
		penDownColorDefault = color;
	}

	public void setPenDownColor(ColorRGB color) {
		penDownColor = color;
	}

	public void setPenUpColor(ColorRGB color) {
		penUpColor = color;
	}

	public ColorRGB getPenUpColor() {
		return penUpColor;
	}

	public void setPenDiameter(double diameter) {
		penDiameter = diameter;
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

	public boolean canChangeMachineSize() {
		return false;
	}

	public boolean canAccelerate() {
		return false;
	}

	public void setStartingPositionIndex(int startingPositionIndex) {
		this.startingPositionIndex = startingPositionIndex;
	}

	public int getStartingPositionIndex() {
		return this.startingPositionIndex;
	}
	/**
	 * @return the blockBufferSize
	 */
	public int getBlockBufferSize() {
		return blockBufferSize;
	}

	/**
	 * @param blockBufferSize the blockBufferSize to set
	 */
	public void setBlockBufferSize(int blockBufferSize) {
		this.blockBufferSize = blockBufferSize;
	}

	/**
	 * @return the segmentsPerSecond
	 */
	public int getSegmentsPerSecond() {
		return segmentsPerSecond;
	}

	/**
	 * @param segmentsPerSecond the segmentsPerSecond to set
	 */
	public void setSegmentsPerSecond(int segmentsPerSecond) {
		this.segmentsPerSecond = segmentsPerSecond;
	}

	/**
	 * @return the minSegmentLength
	 */
	public double getMinSegmentLength() {
		return minSegmentLength;
	}

	/**
	 * @param minSegmentLength the minSegmentLength to set
	 */
	public void setMinSegmentLength(double minSegmentLength) {
		this.minSegmentLength = minSegmentLength;
	}

	/**
	 * @return the minSegTime
	 */
	public long getMinSegmentTime() {
		return minSegTime;
	}

	/**
	 * @param minSegTime the minSegTime to set
	 */
	public void setMinSegmentTime(long minSegTime) {
		this.minSegTime = minSegTime;
	}

	/**
	 * @return the handleSmallSegments
	 */
	public boolean isHandleSmallSegments() {
		return handleSmallSegments;
	}

	/**
	 * @param handleSmallSegments the handleSmallSegments to set
	 */
	public void setHandleSmallSegments(boolean handleSmallSegments) {
		this.handleSmallSegments = handleSmallSegments;
	}

	/**
	 * @return the minAcceleration
	 */
	public double getMinAcceleration() {
		return minAcceleration;
	}

	/**
	 * @param minAcceleration the minAcceleration to set
	 */
	public void setMinAcceleration(double minAcceleration) {
		this.minAcceleration = minAcceleration;
	}

	/**
	 * @return the minimumPlannerSpeed
	 */
	public double getMinPlannerSpeed() {
		return minimumPlannerSpeed;
	}

	/**
	 * @param minimumPlannerSpeed the minimumPlannerSpeed to set
	 */
	public void setMinPlannerSpeed(double minimumPlannerSpeed) {
		this.minimumPlannerSpeed = minimumPlannerSpeed;
	}

	/**
	 * @return the maxJerk
	 */
	public double[] getMaxJerk() {
		return maxJerk;
	}

	/**
	 * @param maxJerk the maxJerk to set
	 */
	public void setMaxJerk(double[] maxJerk) {
		this.maxJerk = maxJerk;
	}
}
