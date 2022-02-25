package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.util.PreferencesHelper;
import java.io.Serializable;
import java.util.*;
import java.util.prefs.Preferences;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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

	private String userGeneralStartGcode = "G28";// TODO Should containe a G28 or a G28 is added befor any move...
	private String userGeneralEndGcode = "M300";// TODO Should containe a disble motors or added at the end //  I think I just parked it 10cm from the left edge and 15cm from the top edge.
	private String userToolChangeStartGcode ="M300"; // Should go in relative moves befor and in absolut moves after ??? a save position ?
	private String userToolChangeEndGcode = ""; // a resort position ?
	/**
	 * These values should match
	 * https://github.com/marginallyclever/makelangelo-firmware/firmware_rumba/configure.h
	 */
	public PlotterSettings() {
		double mh = 1000; // mm
		double mw = 650; // mm

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

		hardwareName = "Makelangelo 5";
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

		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(robotUID));

		limitTop 				= thisMachineNode.getDouble("limit_top", limitTop);
		limitBottom 			= thisMachineNode.getDouble("limit_bottom", limitBottom);
		limitLeft 				= thisMachineNode.getDouble("limit_left", limitLeft);
		limitRight 				= thisMachineNode.getDouble("limit_right", limitRight);
		maxAcceleration 		= thisMachineNode.getDouble("acceleration", maxAcceleration);
		startingPositionIndex 	= thisMachineNode.getInt("startingPosIndex", startingPositionIndex);

		int r = thisMachineNode.getInt("paperColorR", paperColor.getRed());
		int g = thisMachineNode.getInt("paperColorG", paperColor.getGreen());
		int b = thisMachineNode.getInt("paperColorB", paperColor.getBlue());
		paperColor = new ColorRGB(r, g, b);

		// setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool",
		// Integer.toString(getCurrentToolNumber()))));
		isRegistered = Boolean.parseBoolean(thisMachineNode.get("isRegistered", Boolean.toString(isRegistered)));
		hardwareName = thisMachineNode.get("hardwareVersion", hardwareName);

		blockBufferSize 		= thisMachineNode.getInt("blockBufferSize", blockBufferSize);
		segmentsPerSecond 		= thisMachineNode.getInt("segmentsPerSecond", segmentsPerSecond);
		minSegmentLength 		= thisMachineNode.getDouble("minSegmentLength", minSegmentLength);
		minSegTime 				= thisMachineNode.getLong("minSegTime", minSegTime);
		handleSmallSegments 	= thisMachineNode.getBoolean("handleSmallSegments", handleSmallSegments);
		minAcceleration			= thisMachineNode.getDouble("minAcceleration", minAcceleration);
		minimumPlannerSpeed 	= thisMachineNode.getDouble("minimumPlannerSpeed", minimumPlannerSpeed);
		
		userGeneralStartGcode 	= thisMachineNode.get("userGeneralStartGcode", userGeneralStartGcode);
		userGeneralEndGcode 	= thisMachineNode.get("userGeneralEndGcode", userGeneralEndGcode);
		userToolChangeStartGcode 	= thisMachineNode.get("userToolChangeStartGcode", userToolChangeStartGcode);
		userToolChangeEndGcode 	= thisMachineNode.get("userToolChangeEndGcode", userToolChangeEndGcode);
		
		loadJerkConfig(thisMachineNode);
		loadPenConfig(thisMachineNode);
	}
	
	private void loadJerkConfig(Preferences thisMachineNode) {
		Preferences jerkNode = thisMachineNode.node("maxJerk");
		for(int i=0;i<maxJerk.length;i++) {
			maxJerk[i] = jerkNode.getDouble(Integer.toString(i), maxJerk[i] );
		}
	}

	private void loadPenConfig(Preferences prefs) {
		prefs = prefs.node("Pen");
		setPenDiameter(		prefs.getDouble("diameter"			, penDiameter	));
		setPenLiftTime(		prefs.getDouble("z_rate"			, penLiftTime	));
		setPenDownAngle(	prefs.getDouble("z_on"				, penDownAngle	));
		setPenUpAngle(		prefs.getDouble("z_off"				, penUpAngle	));
		setTravelFeedRate(	prefs.getDouble("feed_rate"			, travelFeedRate));
		setDrawFeedRate(	prefs.getDouble("feed_rate_current"	, drawFeedRate	));
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

	public void saveConfig() {
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(robotUID));
		
		thisMachineNode.put("limit_top", Double.toString(limitTop));
		thisMachineNode.put("limit_bottom", Double.toString(limitBottom));
		thisMachineNode.put("limit_right", Double.toString(limitRight));
		thisMachineNode.put("limit_left", Double.toString(limitLeft));
		thisMachineNode.put("acceleration", Double.toString(maxAcceleration));
		thisMachineNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

		thisMachineNode.putInt("paperColorR", paperColor.getRed());
		thisMachineNode.putInt("paperColorG", paperColor.getGreen());
		thisMachineNode.putInt("paperColorB", paperColor.getBlue());

		thisMachineNode.put("isRegistered", Boolean.toString(isRegistered()));
		thisMachineNode.put("hardwareVersion", hardwareName);

		thisMachineNode.putInt("blockBufferSize", blockBufferSize);
		thisMachineNode.putInt("segmentsPerSecond", segmentsPerSecond);
		thisMachineNode.putDouble("minSegmentLength", minSegmentLength);
		thisMachineNode.putLong("minSegTime", minSegTime);
		thisMachineNode.putBoolean("handleSmallSegments", handleSmallSegments);
		thisMachineNode.putDouble("minAcceleration", minAcceleration);
		thisMachineNode.putDouble("minimumPlannerSpeed", minimumPlannerSpeed);
		
		thisMachineNode.put("userGeneralStartGcode", userGeneralStartGcode);
		thisMachineNode.put("userGeneralEndGcode", userGeneralEndGcode);
		thisMachineNode.put("userToolChangeStartGcode", userToolChangeStartGcode);
		thisMachineNode.put("userToolChangeEndGcode", userToolChangeEndGcode);
		
		saveJerkConfig(thisMachineNode);
		savePenConfig(thisMachineNode);
		notifyListeners();
	}

	private void saveJerkConfig(Preferences thisMachineNode) {
		Preferences jerkNode = thisMachineNode.node("maxJerk");
		for(int i=0;i<maxJerk.length;i++) {
			jerkNode.putDouble(Integer.toString(i), maxJerk[i] );
		}
	}

	private void savePenConfig(Preferences prefs) {
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

	public String getUserGeneralStartGcode() {
		return userGeneralStartGcode;
	}

	public void setUserGeneralStartGcode(String userGeneralStartGcode) {
		this.userGeneralStartGcode = userGeneralStartGcode;
	}

	public String getUserGeneralEndGcode() {
		return userGeneralEndGcode;
	}

	public void setUserGeneralEndGcode(String userGeneralEndGcode) {
		this.userGeneralEndGcode = userGeneralEndGcode;
	}

	public String getUserToolChangeStartGcode() {
		return userToolChangeStartGcode;
	}

	public void setUserToolChangeStartGcode(String userToolChangeStartGcode) {
		this.userToolChangeStartGcode = userToolChangeStartGcode;
	}

	public String getUserToolChangeEndGcode() {
		return userToolChangeEndGcode;
	}

	public void setUserToolChangeEndGcode(String userToolChangeEndGcode) {
		this.userToolChangeEndGcode = userToolChangeEndGcode;
	}

	
	/**
	 * Suppose the prefrence is Uptodate ...
	 * Expression evaluation Thanks to https://stackoverflow.com/questions/2605032/is-there-an-eval-function-in-java :: https://stackoverflow.com/users/4244130/vincelomba : https://stackoverflow.com/posts/48251395/revisions
	 * @param in
	 * @return 
	 */
	public String resolvePlaceHolder(String in){
		StringBuilder res = new StringBuilder();
		
		// build a simple map PlaceHolderName to current value ...
		Preferences allMachinesNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences thisMachineNode = allMachinesNode.node(Long.toString(robotUID));	

		System.out.println(""+thisMachineNode);
		System.out.println(""+thisMachineNode.toString());
		
		try{
			String[] childrenNames = thisMachineNode.childrenNames();
			if (childrenNames != null ){
			System.out.println(" childrenNames: "+childrenNames.length);
			}
		}catch(Exception e ){
			e.printStackTrace();
		}
		
		TreeMap<String,String> paramNameToValue = new TreeMap<>();
		try{
			String[] keys = thisMachineNode.keys();
			if (keys != null ){
			System.out.println(" keys: "+keys.length);
			for ( int i = 0 ; i < keys.length ; i++){
				System.out.println("  "+keys[i]+": "+thisMachineNode.get(keys[i], "?"));
				paramNameToValue.put(keys[i], thisMachineNode.get(keys[i], "?"));
			}
			/*
			
 childrenNames: 2
 keys: 22
  acceleration: 100.0
  blockBufferSize: 16
  handleSmallSegments: false
  hardwareVersion: Makelangelo 5
  isRegistered: false
  limit_bottom: -500.0
  limit_left: -325.0
  limit_right: 325.0
  limit_top: 500.0
  minAcceleration: 0.0
  minSegTime: 20000
  minSegmentLength: 0.5
  minimumPlannerSpeed: 0.05
  paperColorB: 255
  paperColorG: 255
  paperColorR: 255
  segmentsPerSecond: 5
  startingPosIndex: 4
  userGeneralEndGcode: 
  userGeneralStartGcode: G28;
G0 X{limit_left+10} Y0.00
M300;
  userToolChangeEndGcode: 
  userToolChangeStartGcode: 
			*/
			}
		}catch(Exception e ){
			e.printStackTrace();
		}
		// TODO a parser like to replace placeHolder with ther value and to performe expression evalutation 
		//// cf https://github.com/MarginallyClever/Makelangelo-software/issues/552#issuecomment-1041907446 : I think I just parked it 10cm from the left edge and 15cm from the top edge.
		// like "G0 X{${limit_left}+10} Y{${limit_top}-15} ; note the '+' as normaly left are negalive value." -> G0 X-
		// with limit_left = -width / 2.0
		// todo evalutation prioryty cf parentesis ...
		// TODO what if array [0] ... ?
		// todo what if conditional ?
		//
		// basic way replace ? // do not help for evaluation
		// ? the lasy way use something like groovy or an existing interprétor to evaluate ... 
		// Comme il n'y a pas besoin d'un vrai parser (je ne vais pas vérifier que l'ensemble respecte une grammaire) 
		int readPos = 0;
		final char toResolvExpressionStartDelimitor = '{';
		final char toResolvExpressionEndDelimitor = '}';
		int level = 0;
		
		StringBuilder tokenToEvaluateReadBuffer = new StringBuilder();
		while (readPos < in.length()){
			char currentCharAtReadPos = in.charAt(readPos);
			switch (currentCharAtReadPos) {
				case toResolvExpressionStartDelimitor:
					level++;
					break;
				case toResolvExpressionEndDelimitor:
					level--;
//					res.append("<TODO resolve \"");
//					res.append(tokenToEvaluateReadBuffer);
//					
//					res.append("\">");
					for (String k : paramNameToValue.keySet() ){
						String tmp = tokenToEvaluateReadBuffer.toString().replaceAll(k, paramNameToValue.get(k));
						tokenToEvaluateReadBuffer.setLength(0);
						tokenToEvaluateReadBuffer.append(tmp);
					}
					
//					res.append("<TODO evaluate \"");
//					res.append(tokenToEvaluateReadBuffer);
//					
//					res.append("\">");
					
					//https://stackoverflow.com/questions/2605032/is-there-an-eval-function-in-java
					try{
					double resTmp = FunctionSolver.solveNumericExpression(tokenToEvaluateReadBuffer.toString());
					tokenToEvaluateReadBuffer.setLength(0);
						tokenToEvaluateReadBuffer.append(resTmp);
						}catch (Exception e ){
						e.printStackTrace();
					}
//					res.append("<TODO checkIfWellEvaluated \"");
//					res.append(tokenToEvaluateReadBuffer);
//					
//					res.append("\">");
					res.append(tokenToEvaluateReadBuffer);
					tokenToEvaluateReadBuffer.setLength(0);
					break;
				default:
					if (level > 0) {
							tokenToEvaluateReadBuffer.append(currentCharAtReadPos);
					} else {
						res.append(currentCharAtReadPos);
					}
			}
			//tokenReadBuffer.a
			if (level <= 0) {

			} else {

			}
			readPos++;
		}
		
		
		return res.toString();
	}
	
}
