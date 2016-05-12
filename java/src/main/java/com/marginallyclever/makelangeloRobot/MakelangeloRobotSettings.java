package com.marginallyclever.makelangeloRobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.drawingtools.DrawingTool_Pen;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.PreferencesHelper;
import com.marginallyclever.makelangelo.Translator;


/**
 * All the hardware settings for a single Makelangelo robot.
 * @author dan royer
 */
public final class MakelangeloRobotSettings {
	public static final double INCH_TO_CM = 2.54;
	/**
	 * measured from "m4 calibration.pdf"
	 * @since 7.5.0
	 */
	public static final float CALIBRATION_CM_FROM_TOP = 21.7f;
	
	private String[] configsAvailable;
	
	private int currentToolIndex;

	// pulleys turning backwards?
	private boolean isLeftMotorInverted;
	private boolean isRegistered;
	private boolean isRightMotorInverted;
	private double limitBottom;

	private double limitLeft;
	private double limitRight;
	// machine physical limits, in cm
	private double limitTop;
	private ArrayList<MakelangeloRobotSettingsListener> listeners;
	private double maxAcceleration;

	// maximum speed
	private double maxFeedRate;
	private double paperBottom;

	private double paperLeft;
	private double paperMargin;  // % from edge of paper.
	
	private double paperRight;
	// paper area, in cm
	private double paperTop;
	
	// pulley diameter
	private double pulleyDiameterLeft;
	
	private double pulleyDiameterRight;
	private boolean reverseForGlass;
	

	/**
	 * Each robot has a global unique identifier
	 */
	private long robotUID;
	
	// TODO leave the origin at the center of the paper and make a G92 (teleport) call when at the starting position

	private boolean shouldSignName;

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

	// TODO a way for users to create different tools for each machine
	private List<DrawingTool> tools;

	/**
	 *
	 */
	private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

	
	
	/**
	 * TODO move tool names into translations & add a color palette system for quantizing colors
	 *
	 * @param translator
	 * @param robot
	 */
	protected MakelangeloRobotSettings(Translator translator, MakelangeloRobot robot) {
		double mh = 835 * 0.1; // mm > cm
		double mw = 835 * 0.1; // mm > cm
		
		robotUID = 0;
		isRegistered = false;
		limitTop = mh/2;
		limitBottom = -mh/2;
		limitRight = mw/2;
		limitLeft = -mw/2;

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

		maxFeedRate = 7500;
		maxAcceleration = 20;

		// diameter = circumference/pi
		// circumference is 20 teeth @ 2mm/tooth
		pulleyDiameterLeft  = 20.0 * 0.2 / Math.PI;  // 20 teeth on the pulley, 2mm per tooth.
		pulleyDiameterRight = 20.0 * 0.2 / Math.PI;  // 20 teeth on the pulley, 2mm per tooth.

		isLeftMotorInverted = false;
		isRightMotorInverted = true;
		reverseForGlass = false;

		startingPositionIndex = 4;
		
		tools = new ArrayList<>();
		tools.add(new DrawingTool_Pen(translator, robot));
		currentToolIndex = 0;

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
		return maxAcceleration;
	}

	/**
	 * Get the UID of every machine this computer recognizes INCLUDING machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getAvailableConfigurations() {
		return configsAvailable;
	}
	
	
	public String getBobbinLine() {
		String left = String.format("%.4f", pulleyDiameterLeft);
		String right = String.format("%.4f", pulleyDiameterRight);
		return "D1 L" + left + " R" + right;
	}

	
	public double getHomeX() {
		return 0;
	}
	
	public double getHomeY() {
		float limitTop = (float)getLimitTop();
		float homeY = limitTop - MakelangeloRobotSettings.CALIBRATION_CM_FROM_TOP;
		homeY = (float)Math.floor(homeY*1000.0f)/1000.0f;
		return homeY;
	}
	
	public String getSetStartAtHomeLine() {
		return "G92 X"+getHomeX()+" Y"+getHomeY();
	}

	public String getConfigLine() {
		return "M101 T" + limitTop
				+ " B" + limitBottom
				+ " L" + limitLeft
				+ " R" + limitRight
				+ " I" + (isLeftMotorInverted ? "-1" : "1")
				+ " J" + (isRightMotorInverted ? "-1" : "1");
	}


	public DrawingTool getCurrentTool() {
		return getTool(currentToolIndex);
	}


	public int getCurrentToolNumber() {
		return currentToolIndex;
	}

	public double getFeedRate() {
		return maxFeedRate;
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
		final List<String> thissAvailableArrayAsList = new LinkedList<>(Arrays.asList(configsAvailable));
		if (thissAvailableArrayAsList.contains("0")) {
			thissAvailableArrayAsList.remove("0");
		}
		return Arrays.copyOf(thissAvailableArrayAsList.toArray(), thissAvailableArrayAsList.size(), String[].class);
	}


	public double getLimitBottom() {
		return limitBottom;
	}


	public double getLimitLeft() {
		return limitLeft;
	}


	public double getLimitRight() {
		return limitRight;
	}

	public double getLimitTop() {
		return limitTop;
	}

	/**
	 * @return the number of machine configurations that exist on this computer
	 */
	public int getMachineCount() {
		return configsAvailable.length;
	}


	public double getPaperBottom() {
		return paperBottom;
	}


	/**
	 * @return paper height, in cm.
	 */
	public double getPaperHeight() {
		return paperTop - paperBottom;
	}

	public double getPaperLeft() {
		return paperLeft;
	}


	public double getPaperMargin() {
		return paperMargin;
	}


	public double getPaperRight() {
		return paperRight;
	}


	public double getPaperTop() {
		return paperTop;
	}


	/**
	 * @return paper width, in cm.
	 */
	public double getPaperWidth() {
		return paperRight - paperLeft;
	}

	public String getPenDownString() {
		return Float.toString(getCurrentTool().getPenDownAngle());
	}
	
	public String getPenUpString() {
		return Float.toString(getCurrentTool().getPenUpAngle());
	}

	public double getPulleyDiameterLeft()  {
		return pulleyDiameterLeft;
	}

	public double getPulleyDiameterRight() {
		return pulleyDiameterRight;
	}

	public DrawingTool getTool(int tool_id) {
		return tools.get(tool_id);
	}
	
	public String[] getToolNames() {
		String[] toolNames = new String[tools.size()];
		Iterator<DrawingTool> i = tools.iterator();
		int c = 0;
		while (i.hasNext()) {
			DrawingTool t = i.next();
			toolNames[c++] = t.getName();
		}
		return toolNames;
	}

	public long getUID() {
		return robotUID;
	}

	public void invertLeftMotor(boolean backwards)  {
		isLeftMotorInverted = backwards;
	}

	public void invertRightMotor(boolean backwards) {
		isRightMotorInverted = backwards;
	}

	public boolean isLeftMotorInverted()   {
		return isLeftMotorInverted;
	}

	public boolean isPaperConfigured() {
		return (paperTop > paperBottom && paperRight > paperLeft);
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	public boolean isReverseForGlass() {
		return reverseForGlass;
	}

	public boolean isRightMotorInverted() {
		return isRightMotorInverted;
	}

	/**
	 * Load the machine configuration
	 *
	 * @param uid the unique id of the robot to be loaded
	 */
	protected void loadConfig(long uid) {
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

		isLeftMotorInverted=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m1invert", Boolean.toString(isLeftMotorInverted)));
		isRightMotorInverted=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m2invert", Boolean.toString(isRightMotorInverted)));

		pulleyDiameterLeft=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_left_diameter", Double.toString(pulleyDiameterLeft)));
		pulleyDiameterRight=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_right_diameter", Double.toString(pulleyDiameterRight)));

		maxFeedRate=Double.valueOf(uniqueMachinePreferencesNode.get("feed_rate",Double.toString(maxFeedRate)));
		maxAcceleration=Double.valueOf(uniqueMachinePreferencesNode.get("acceleration",Double.toString(maxAcceleration)));

		startingPositionIndex = Integer.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex",Integer.toString(startingPositionIndex)));

		// load each tool's settings
		for (DrawingTool tool : tools) {
			tool.loadConfig(uniqueMachinePreferencesNode);
		}

		paperMargin = Double.valueOf(uniqueMachinePreferencesNode.get("paper_margin", Double.toString(paperMargin)));
		reverseForGlass = Boolean.parseBoolean(uniqueMachinePreferencesNode.get("reverseForGlass", Boolean.toString(reverseForGlass)));
		setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool", Integer.toString(getCurrentToolNumber()))));
		setRegistered(Boolean.parseBoolean(uniqueMachinePreferencesNode.get("isRegistered",Boolean.toString(isRegistered))));
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
		uniqueMachinePreferencesNode.put("m1invert", Boolean.toString(isLeftMotorInverted));
		uniqueMachinePreferencesNode.put("m2invert", Boolean.toString(isRightMotorInverted));
		uniqueMachinePreferencesNode.put("bobbin_left_diameter", Double.toString(pulleyDiameterLeft));
		uniqueMachinePreferencesNode.put("bobbin_right_diameter", Double.toString(pulleyDiameterRight));
		uniqueMachinePreferencesNode.put("feed_rate", Double.toString(maxFeedRate));
		uniqueMachinePreferencesNode.put("acceleration", Double.toString(maxAcceleration));
		uniqueMachinePreferencesNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

		uniqueMachinePreferencesNode.putDouble("paper_left", paperLeft);
		uniqueMachinePreferencesNode.putDouble("paper_right", paperRight);
		uniqueMachinePreferencesNode.putDouble("paper_top", paperTop);
		uniqueMachinePreferencesNode.putDouble("paper_bottom", paperBottom);

		// save each tool's settings
		for (DrawingTool tool : tools) {
			tool.saveConfig(uniqueMachinePreferencesNode);
		}

		uniqueMachinePreferencesNode.put("paper_margin", Double.toString(paperMargin));
		uniqueMachinePreferencesNode.put("reverseForGlass", Boolean.toString(reverseForGlass));
		uniqueMachinePreferencesNode.put("current_tool", Integer.toString(getCurrentToolNumber()));
		uniqueMachinePreferencesNode.put("isRegistered", Boolean.toString(isRegistered));
		
	}
	
	public void setAcceleration(double f) {
		maxAcceleration = f;
		saveConfig();
	}
	
	public void setCurrentToolNumber(int current_tool) {
		this.currentToolIndex = current_tool;
	}
	
	public void setFeedRate(double f) {
		maxFeedRate = f;
		saveConfig();
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

	public void setPaperSize(double width, double height) {
		this.paperLeft = -width/2;
		this.paperRight = width/2;
		this.paperTop = height/2;
		this.paperBottom = -height/2;
	}
	
	public void setPulleyDiameter(double left,double right) {
		pulleyDiameterLeft = left;
		pulleyDiameterRight = right;
	}
	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
	public void setReverseForGlass(boolean reverseForGlass) {
		this.reverseForGlass = reverseForGlass;
	}
	public boolean shouldSignName() {
		return shouldSignName;
	}
}
