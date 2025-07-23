package com.marginallyclever.makelangelo.plotter.plottersettings;


import com.marginallyclever.convenience.W3CColorNames;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.util.PreferencesHelper;
import org.json.JSONObject;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * {@link PlotterSettings} stores the physical settings for a single {@link com.marginallyclever.makelangelo.plotter.Plotter}.
 * Not to be confused with the dynamic state of a {@link com.marginallyclever.makelangelo.plotter.Plotter}.
 * @author Dan Royer
 */
public class PlotterSettings {
	public static final String ANCESTOR = "ancestor";
	/**
	 * mm/s/s
	 */
	public static final String MAX_ACCELERATION = "acceleration";
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
	 * <pre>{@code
	 * 	private String[] startingStrings = { "Top Left", "Top Center", "Top Right", "Left", "Center", "Right",
	 * 			"Bottom Left", "Bottom Center", "Bottom Right" };
	 * }</pre>
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
	public static final String END_GCODE = "userGeneralEndGcode";
	/**
	 * String
	 */
	public static final String START_GCODE = "userGeneralStartGcode";
	/**
	 * String
	 */
	public static final String STYLE = "style";

	public static final String FIND_HOME_GCODE = "FIND_HOME";
	public static final String DEFAULT_FIND_HOME_GCODE = "G28 X Y";

	public static final String PEN_UP_GCODE = "PEN_UP";
	public static final String DEFAULT_PEN_UP_GCODE = "M280 P0 S%1 T%2";

	public static final String PEN_DOWN_GCODE = "PEN_DOWN";
	public static final String DEFAULT_PEN_DOWN_GCODE = "M280 P0 S%1 T%2";

	private final JSONObject json = new JSONObject();
	private String robotUID = "0";

	public PlotterSettings() {
		super();
		setDefaults();
	}

	private void setDefaults() {
		json.put(IS_REGISTERED, 			false);
		json.put(HANDLE_SMALL_SEGMENTS, 	false);
		json.put(MIN_SEGMENT_LENGTH, 		0.5);	// mm
		json.put(MAX_ACCELERATION, 			100);	// mm/s/s
		json.put(MIN_ACCELERATION, 			0.0);	// mm/s/s
		json.put(MINIMUM_PLANNER_SPEED, 	0.05);	// mm/s
		json.put(LIMIT_LEFT, 				-325);	// mm
		json.put(LIMIT_RIGHT, 				325);	// mm
		json.put(LIMIT_BOTTOM,				-500);	// mm
		json.put(LIMIT_TOP, 				500);		// mm
		json.put(DIAMETER, 					0.8);	// mm, >0
		json.put(PEN_ANGLE_UP, 				90);	// servo angle (degrees,0...180)
		json.put(PEN_ANGLE_DOWN, 			25);	// servo angle (degrees,0...180)
		json.put(PEN_ANGLE_UP_TIME, 		250);
		json.put(PEN_ANGLE_DOWN_TIME, 		150);
		json.put(FEED_RATE_TRAVEL, 			3000);	// mm/min.  3000 = 50 mm/s
		json.put(FEED_RATE_DRAW, 			3000);	// mm/min.  3000 = 50 mm/s
		json.put(BLOCK_BUFFER_SIZE, 		16);
		json.put(SEGMENTS_PER_SECOND, 		5);
		json.put(MIN_SEG_TIME, 				20000);		// us
		json.put(STARTING_POS_INDEX, 		4);
		json.put(ANCESTOR,					"");
		json.put(START_GCODE, 				"");
		json.put(END_GCODE, 				"");
		json.put(FIND_HOME_GCODE, 			DEFAULT_FIND_HOME_GCODE);
		json.put(STYLE,         			PlotterRendererFactory.MAKELANGELO_5.getName());
		json.put(PAPER_COLOR,		 		(Color.WHITE.hashCode()));
		json.put(PEN_DOWN_COLOR_DEFAULT, 	(Color.BLACK.hashCode()));
		json.put(PEN_DOWN_COLOR, 			(Color.BLACK.hashCode()));
		json.put(PEN_UP_COLOR, 				(Color.GREEN.hashCode()));
		json.put(MAX_JERK,           		"[10,10,3]");
		json.put(PEN_UP_GCODE, 				DEFAULT_PEN_UP_GCODE);
		json.put(PEN_DOWN_GCODE, 			DEFAULT_PEN_DOWN_GCODE);
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
		json.put("robotUID", robotUID);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public double getDouble(String key) throws NullPointerException, IllegalStateException {
		return json.getDouble(key);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public int getInteger(String key) throws NullPointerException, IllegalStateException {
		return json.getInt(key);
	}

	public float getFloat(String key) throws NullPointerException, IllegalStateException {
		return (float) json.getDouble(key);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public String getString(String key) throws NullPointerException, IllegalStateException {
		return json.getString(key);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public boolean getBoolean(String key) throws NullPointerException, IllegalStateException {
		return json.getBoolean(key);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public Color getColor(String key) throws NullPointerException, IllegalStateException {
		int v = getInteger(key);
		return new Color(v);
	}

	/**
	 * @param key the key to look up
	 * @return the value of the key
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public double [] getDoubleArray(String key) throws NullPointerException, IllegalStateException {
		String s = getString(key);
		String [] parts = s.substring(1,s.length()-1).split(",");
		if(parts.length==0) return new double[0];
		try {
			double[] result = new double[parts.length];
			for (int i = 0; i < parts.length; ++i) {
				result[i] = Double.parseDouble(parts[i].trim());
			}
			return result;
		} catch(Exception e) {
			return new double[0];
		}
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setDouble(String key,double value) throws NullPointerException, IllegalStateException {
		json.put(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setInteger(String key,int value) throws NullPointerException, IllegalStateException {
		json.put(key,value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setString(String key,String value) throws NullPointerException, IllegalStateException {
		json.put(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setBoolean(String key,boolean value) throws NullPointerException, IllegalStateException {
		json.put(key, value);
	}

	/**
	 * @param key the key to look up
	 * @param value the value to set
	 * @throws NullPointerException key does not exist
	 * @throws IllegalStateException profile does not exist.
	 */
	public void setColor(String key,Color value) throws NullPointerException, IllegalStateException {
		setInteger(key,value.hashCode());
	}

	public void setDoubleArray(String key,double [] values) throws NullPointerException, IllegalStateException {
		setString(key,Arrays.toString(values));
	}

	public Point2d getHome() {
		return new Point2d();
	}

	/**
	 * Load the machine configuration from {@link Preferences}.
	 * @param uid the unique id of the robot to be loaded
	 */
	public void load(String uid) {
		robotUID = uid;
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);

		json.put("robotUID", robotUID);
		json.put(IS_REGISTERED, 			thisMachineNode.getBoolean(IS_REGISTERED,false));
		json.put(HANDLE_SMALL_SEGMENTS, 	thisMachineNode.getBoolean(HANDLE_SMALL_SEGMENTS,false));
		json.put(MIN_SEGMENT_LENGTH, 		thisMachineNode.getDouble(MIN_SEGMENT_LENGTH,0.5));	// mm
		json.put(MAX_ACCELERATION, 			thisMachineNode.getDouble(MAX_ACCELERATION,100));	// mm/s/s
		json.put(MIN_ACCELERATION, 			thisMachineNode.getDouble(MIN_ACCELERATION,0.0));	// mm/s/s
		json.put(MINIMUM_PLANNER_SPEED, 	thisMachineNode.getDouble(MINIMUM_PLANNER_SPEED,0.05));	// mm/s
		json.put(LIMIT_LEFT, 				thisMachineNode.getDouble(LIMIT_LEFT,-325));	// mm
		json.put(LIMIT_RIGHT, 				thisMachineNode.getDouble(LIMIT_RIGHT,325));	// mm
		json.put(LIMIT_BOTTOM,				thisMachineNode.getDouble(LIMIT_BOTTOM,-500));	// mm
		json.put(LIMIT_TOP, 				thisMachineNode.getDouble(LIMIT_TOP,500));		// mm
		json.put(DIAMETER, 					thisMachineNode.getDouble(DIAMETER,0.8));	// mm, >0
		json.put(PEN_ANGLE_UP, 				thisMachineNode.getDouble(PEN_ANGLE_UP,90));	// servo angle (degrees,0...180)
		json.put(PEN_ANGLE_UP_TIME, 		thisMachineNode.getDouble(PEN_ANGLE_UP_TIME,250));
		json.put(PEN_ANGLE_DOWN, 			thisMachineNode.getDouble(PEN_ANGLE_DOWN,25));	// servo angle (degrees,0...180)
		json.put(PEN_ANGLE_DOWN_TIME, 		thisMachineNode.getDouble(PEN_ANGLE_DOWN_TIME,150));
		json.put(FEED_RATE_TRAVEL, 			thisMachineNode.getDouble(FEED_RATE_TRAVEL,3000));	// mm/min.  3000 = 50 mm/s
		json.put(FEED_RATE_DRAW, 			thisMachineNode.getDouble(FEED_RATE_DRAW,3000));	// mm/min.  3000 = 50 mm/s
		json.put(BLOCK_BUFFER_SIZE, 		thisMachineNode.getInt(BLOCK_BUFFER_SIZE,16));
		json.put(SEGMENTS_PER_SECOND, 		thisMachineNode.getInt(SEGMENTS_PER_SECOND,5));
		json.put(MIN_SEG_TIME, 				thisMachineNode.getInt(MIN_SEG_TIME,20000));		// us
		json.put(STARTING_POS_INDEX, 		thisMachineNode.getInt(STARTING_POS_INDEX,4));
		json.put(ANCESTOR,					thisMachineNode.get(ANCESTOR,""));
		json.put(START_GCODE, 				thisMachineNode.get(START_GCODE,""));
		json.put(END_GCODE, 				thisMachineNode.get(END_GCODE,""));
		json.put(FIND_HOME_GCODE, 			thisMachineNode.get(FIND_HOME_GCODE, DEFAULT_FIND_HOME_GCODE));
		json.put(STYLE,         			thisMachineNode.get(STYLE, PlotterRendererFactory.MAKELANGELO_5.getName()));
		json.put(PAPER_COLOR,		 		thisMachineNode.getInt(PAPER_COLOR,(Color.WHITE.hashCode())));
		json.put(PEN_DOWN_COLOR_DEFAULT, 	thisMachineNode.getInt(PEN_DOWN_COLOR_DEFAULT,(Color.BLACK.hashCode())));
		json.put(PEN_DOWN_COLOR, 			thisMachineNode.getInt(PEN_DOWN_COLOR,(Color.BLACK.hashCode())));
		json.put(PEN_UP_COLOR, 				thisMachineNode.getInt(PEN_UP_COLOR,(Color.GREEN.hashCode())));
		json.put(MAX_JERK,           		thisMachineNode.get(MAX_JERK,"[10,10,3]"));
		json.put(PEN_UP_GCODE, 				thisMachineNode.get(PEN_UP_GCODE, DEFAULT_PEN_UP_GCODE));
		json.put(PEN_DOWN_GCODE, 			thisMachineNode.get(PEN_DOWN_GCODE, DEFAULT_PEN_DOWN_GCODE));
	}

	/**
	 * Save the machine configuration to {@link Preferences}.  The preference node will be the unique id of the robot.
	 * Preferences save every time a value is changed, so save() does nothing.
	 */
	public void save() {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(robotUID);

		thisMachineNode.put("robotUID", robotUID);
		thisMachineNode.putBoolean(IS_REGISTERED, 			json.getBoolean(IS_REGISTERED));
		thisMachineNode.putBoolean(HANDLE_SMALL_SEGMENTS, 	json.getBoolean(HANDLE_SMALL_SEGMENTS));
		thisMachineNode.putDouble(MIN_SEGMENT_LENGTH, 		json.getDouble(MIN_SEGMENT_LENGTH));
		thisMachineNode.putDouble(MAX_ACCELERATION, 		json.getDouble(MAX_ACCELERATION));
		thisMachineNode.putDouble(MIN_ACCELERATION, 		json.getDouble(MIN_ACCELERATION));
		thisMachineNode.putDouble(MINIMUM_PLANNER_SPEED, 	json.getDouble(MINIMUM_PLANNER_SPEED));
		thisMachineNode.putDouble(LIMIT_LEFT, 				json.getDouble(LIMIT_LEFT));
		thisMachineNode.putDouble(LIMIT_RIGHT, 				json.getDouble(LIMIT_RIGHT));
		thisMachineNode.putDouble(LIMIT_BOTTOM, 			json.getDouble(LIMIT_BOTTOM));
		thisMachineNode.putDouble(LIMIT_TOP, 				json.getDouble(LIMIT_TOP));
		thisMachineNode.putDouble(DIAMETER, 				json.getDouble(DIAMETER));
		thisMachineNode.putDouble(PEN_ANGLE_UP, 			json.getDouble(PEN_ANGLE_UP));
		thisMachineNode.putDouble(PEN_ANGLE_DOWN, 			json.getDouble(PEN_ANGLE_DOWN));
		thisMachineNode.putDouble(PEN_ANGLE_UP_TIME, 		json.getDouble(PEN_ANGLE_UP_TIME));
		thisMachineNode.putDouble(PEN_ANGLE_DOWN_TIME, 		json.getDouble(PEN_ANGLE_DOWN_TIME));
		thisMachineNode.putDouble(FEED_RATE_TRAVEL, 		json.getDouble(FEED_RATE_TRAVEL));
		thisMachineNode.putDouble(FEED_RATE_DRAW, 			json.getDouble(FEED_RATE_DRAW));
		thisMachineNode.putInt(BLOCK_BUFFER_SIZE, 			json.getInt(BLOCK_BUFFER_SIZE));
		thisMachineNode.putInt(SEGMENTS_PER_SECOND, 		json.getInt(SEGMENTS_PER_SECOND));
		thisMachineNode.putInt(MIN_SEG_TIME, 				json.getInt(MIN_SEG_TIME));
		thisMachineNode.putInt(STARTING_POS_INDEX, 			json.getInt(STARTING_POS_INDEX));
		thisMachineNode.put(ANCESTOR, 						json.getString(ANCESTOR));
		thisMachineNode.put(START_GCODE, 					json.getString(START_GCODE));
		thisMachineNode.put(END_GCODE, 						json.getString(END_GCODE));
		thisMachineNode.put(FIND_HOME_GCODE, 				json.getString(FIND_HOME_GCODE));
		thisMachineNode.put(STYLE, 							json.getString(STYLE));
		thisMachineNode.putInt(PAPER_COLOR, 				json.getInt(PAPER_COLOR));
		thisMachineNode.putInt(PEN_DOWN_COLOR_DEFAULT, 		json.getInt(PEN_DOWN_COLOR_DEFAULT));
		thisMachineNode.putInt(PEN_DOWN_COLOR, 				json.getInt(PEN_DOWN_COLOR));
		thisMachineNode.putInt(PEN_UP_COLOR, 				json.getInt(PEN_UP_COLOR));
		thisMachineNode.put(MAX_JERK, 						json.getString(MAX_JERK));
		thisMachineNode.put(PEN_UP_GCODE, 					json.getString(PEN_UP_GCODE));
		thisMachineNode.put(PEN_DOWN_GCODE, 				json.getString(PEN_DOWN_GCODE));
	}

	/**
	 * Reset the machine configuration to the value of the progenitor (original ancestor).
	 */
	public void reset() {
		// TODO finish me?
	}

	/**
	 * Sets the machine limits.
	 * @param width mm
	 * @param height mm
	 */
	public void setMachineSize(double width, double height) {
		setDouble(PlotterSettings.LIMIT_LEFT,-width / 2.0);
		setDouble(PlotterSettings.LIMIT_RIGHT,width / 2.0);
		setDouble(PlotterSettings.LIMIT_BOTTOM,-height / 2.0);
		setDouble(PlotterSettings.LIMIT_TOP,height / 2.0);
	}

	@Override
	public String toString() {
		return json.toString();
	}

	/**
	 * @param key the key to look up
	 * @return true if the value of this key is the same as the value of the previous ancestor.
	 */
	public boolean isDefaultValue(String key) {
		String progenitor = getProgenitor();
		if(progenitor.equals(getUID())) return true;
		PlotterSettings ancestor = new PlotterSettings(progenitor);
		return ancestor.getString(key).equals(getString(key));
	}

	/**
	 * Walk up the chain of ancestors to find the progenitor, the original.
	 * @return the name of the progenitor.
	 */
	public String getProgenitor() {
		PlotterSettings trace = this;
		while(!trace.isMostAncestral()) {
			trace = new PlotterSettings(trace.getString(ANCESTOR));
		}
		return trace.getUID();
	}

	/**
	 * @return true if this profile has no ancestors.
	 */
	public boolean isMostAncestral() {
		String ancestorName = getString(ANCESTOR);
		return ancestorName==null || ancestorName.isEmpty();
	}

	/**
	 * <a href="https://marlinfw.org/docs/gcode/G000-G001.html">By convention, most G-code generators use G0 for non-extrusion movements</a>
	 * @param x destination point
	 * @param y destination point
	 * @return the formatted string
	 */
	public String getTravelToString(double x, double y) {
		return "G0 " + getPosition(x, y)
				+ " F" + getDouble(FEED_RATE_TRAVEL);
	}

	/**
	 * <a href="https://marlinfw.org/docs/gcode/G000-G001.html">By convention, most G-code generators use G0 for non-extrusion movements</a>
	 * @param x destination point
	 * @param y destination point
	 * @return the formatted string
	 */
	public String getDrawToString(double x, double y) {
		return "G1 " + getPosition(x, y)
				+ " F" + getDouble(FEED_RATE_DRAW);
	}

	private static String getPosition(double x, double y) {
		return "X" + StringHelper.formatDouble(x)
				+ " Y" + StringHelper.formatDouble(y);
	}

	public String getPenUpString() {
		return getString2(PEN_UP_GCODE,new String[] {
				String.valueOf(getDouble(PEN_ANGLE_UP)),
				String.valueOf(getDouble(PEN_ANGLE_UP_TIME))
		});
	}

	public String getPenDownString() {
		return getString2(PEN_DOWN_GCODE,new String[] {
				String.valueOf(getDouble(PEN_ANGLE_DOWN)),
				String.valueOf(getDouble(PEN_ANGLE_DOWN_TIME))
		});
	}

	/**
	 * Get a string with parameters replaced.
	 * @param key the key to look up
	 * @param params the parameters to replace in the string
	 * @return the formatted string
	 */
	private String getString2(String key,String [] params) {
		String modified = getString(key);
		int n=1;
		for(String p : params) {
			modified = modified.replaceAll("%"+n, p);
			++n;
		}
		return modified;
	}

	public String getToolChangeString(int toolNumber) {
		String colorName = getColorName(toolNumber & 0xFFFFFF);
		return "M0 Ready " + colorName + " and click";
	}

	private static String getColorName(int toolNumber) {
		String name = W3CColorNames.get(new Color(toolNumber));
		if(name==null) name = "0x" + StringHelper.paddedHex(toolNumber); // display unknown RGB value as hex
		return name;
	}

	/**
	 * @return the string to send to the plotter to find home.
	 */
	public String getFindHomeString() {
		return getString(FIND_HOME_GCODE);
	}
}