package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.marlinSimulation.MarlinSimulation;
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

	// Each robot has a global unique identifier
	private long robotUID;
	// if we wanted to test for Marginally Clever brand Makelangelo robots
	private boolean isRegistered;

	private String hardwareName;
		
	// machine physical limits, in mm
	private double limitLeft;
	private double limitRight;
	private double limitBottom;
	private double limitTop;
	
	// speed control
	private double travelFeedRate = MarlinSimulation.MAX_FEEDRATE;
	private double drawFeedRate = MarlinSimulation.MAX_FEEDRATE;
	private double maxAcceleration = MarlinSimulation.MAX_ACCELERATION;

	private ColorRGB paperColor;

	private ColorRGB penDownColorDefault;
	private ColorRGB penDownColor;
	private ColorRGB penUpColor;

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

		penDownColor = penDownColorDefault = new ColorRGB(0, 0, 0); // BLACK
		penUpColor = new ColorRGB(0, 255, 0); // blue
		startingPositionIndex = 4;

		setHardwareVersion("Makelangelo 5");

		// Load most recent config
		// loadConfig(last_machine_id);
	}

	// OBSERVER PATTERN START

	private List<PlotterSettingsListener> listeners = new ArrayList<PlotterSettingsListener>();

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
		setHardwareVersion(uniqueMachinePreferencesNode.get("hardwareVersion", hardwareName));
	}

	protected void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setPenDiameter(Double.valueOf(prefs.get("diameter", Double.toString(penDiameter))));
		setPenLiftTime(Double.valueOf(prefs.get("z_rate", Double.toString(penLiftTime))));
		setPenDownAngle(Double.valueOf(prefs.get("z_on", Double.toString(penDownAngle))));
		setPenUpAngle(Double.valueOf(prefs.get("z_off", Double.toString(penUpAngle))));
		setTravelFeedRate(Double.valueOf(prefs.get("feed_rate", Double.toString(travelFeedRate))));
		setDrawFeedRate(Double.valueOf(prefs.get("feed_rate_current", Double.toString(drawFeedRate))));
		// tool_number = Integer.valueOf(prefs.get("tool_number",Integer.toString(tool_number)));

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

		uniqueMachinePreferencesNode.put("hardwareVersion", hardwareName);

		savePenConfig(uniqueMachinePreferencesNode);
		notifyListeners();
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

	public String getHardwareName() {
		return hardwareName;
	}

	public void setHardwareVersion(String name) {
		hardwareName = name;
		if (!canChangeMachineSize()) {
			this.setMachineSize(getWidth(), getHeight());
		}
	}

	/**
	 * @return height of machine's drawing area, in mm.
	 */
	private double getHeight() {
		return 1000; // mm
	}

	/**
	 * @return width of machine's drawing area, in mm.
	 */
	private double getWidth() {
		return 650; // mm
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
}
