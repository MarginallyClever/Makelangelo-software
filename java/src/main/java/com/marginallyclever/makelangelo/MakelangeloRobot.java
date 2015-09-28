package com.marginallyclever.makelangelo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.drawingtools.DrawingTool_LED;
import com.marginallyclever.drawingtools.DrawingTool_Pen;
import com.marginallyclever.drawingtools.DrawingTool_Spraypaint;


/**
 * All the hardware settings for a single Makelangelo robot.
 * @author dan royer
 */
public final class MakelangeloRobot {
	/**
	 *
	 */
	private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

	/**
	 * Each robot has a global unique identifier
	 */
	private long robotUID = 0;

	public final static double INCH_TO_CM = 2.54;

	public final String commonPaperSizes [] = { "",
			"4A0 (1682 x 2378)",
			"2A0 (1189 x 1682)",
			"A0 (841 x 1189)",
			"A1 (594 x 841)",
			"A2 (420 x 594)",
			"A3 (297 x 420)",
			"A4 (210 x 297)",
			"A5 (148 x 210)",
			"A6 (105 x 148)",
			"A7 (74 x 105)",};

	// machine physical limits
	protected double limitTop;
	protected double limitBottom;
	protected double limitLeft;
	protected double limitRight;

	// paper area
	protected double paperTop;
	protected double paperBottom;
	protected double paperLeft;
	protected double paperRight;
	protected double paperMargin;

	// pulleys turning backwards?
	protected boolean invertMotor1;
	protected boolean invertMotor2;

	// pulley diameter
	protected double bobbinDiameterLeft;
	protected double bobbinDiameterRight;
	
	// maximum speed
	protected double maxFeedRate;

	protected boolean reverseForGlass;
	protected boolean areMotorsBackwards;

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
	protected int startingPositionIndex;

	// TODO a way for users to create different tools for each machine
	private List<DrawingTool> tools;

	private int currentToolIndex;

	private String[] machineConfigurationsAvailable = null;

	private MainGUI gui = null;
	private MultilingualSupport translator = null;

	private final Logger logger = LoggerFactory.getLogger(MakelangeloRobot.class);


	/**
	 * TODO move tool names into translations & add a color palette system for quantizing colors
	 *
	 * @param gui
	 * @param translator
	 */
	protected MakelangeloRobot(MainGUI _gui, MultilingualSupport _translator) {
		gui = _gui;
		translator = _translator;

		commonPaperSizes[0] = translator.get("Other");

		limitTop = 18 * INCH_TO_CM;
		limitBottom = -18 * INCH_TO_CM;
		limitLeft = -18 * INCH_TO_CM;
		limitRight = 18 * INCH_TO_CM;

		// paper area
		paperTop = 12 * INCH_TO_CM;
		paperBottom = -12 * INCH_TO_CM;
		paperLeft = -9 * INCH_TO_CM;
		paperRight = 9 * INCH_TO_CM;
		paperMargin = 0.9;

		maxFeedRate=11000;
		startingPositionIndex = 4;
		currentToolIndex = 0;
		maxFeedRate = 11000;
		bobbinDiameterLeft=1.5;
		bobbinDiameterRight=1.5;

		invertMotor1 = false;
		invertMotor2 = false;

		reverseForGlass=false;
		areMotorsBackwards=false;
		
		tools = new ArrayList<>();
		tools.add(new DrawingTool_Pen("Pen (black)", 0, gui, translator, this));
		tools.add(new DrawingTool_Pen("Pen (red)", 1, gui, translator, this));
		tools.add(new DrawingTool_Pen("Pen (green)", 2, gui, translator, this));
		tools.add(new DrawingTool_Pen("Pen (blue)", 3, gui, translator, this));
		tools.add(new DrawingTool_LED(gui, translator, this));
		tools.add(new DrawingTool_Spraypaint(gui, translator, this));

		// which configurations are available?
		try {
			machineConfigurationsAvailable = topLevelMachinesPreferenceNode.childrenNames();
		} catch (Exception e) {
			logger.error("{}", e);
			machineConfigurationsAvailable = new String[1];
		}
		
		// Load most recent config
		loadConfig(0);
	}

	/**
	 * Must match commonPaperSizes
	 * @return
	 */
	public int getCurrentPaperSizeChoice(double pw,double ph) {
		if( pw == 1682 && ph == 2378 ) return 1;
		if( pw == 1189 && ph == 1682 ) return 2;
		if( pw == 841 && ph == 1189 ) return 3;
		if( pw == 594 && ph == 841 ) return 4;
		if( pw == 420 && ph == 594 ) return 5;
		if( pw == 297 && ph == 420 ) return 6;
		if( pw == 210 && ph == 297 ) return 7;
		if( pw == 148 && ph == 210 ) return 8;
		if( pw == 105 && ph == 148 ) return 9;
		if( pw == 74 && ph == 105 ) return 10;

		return 0;
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


	// dialog to adjust the pen up & pen down values
	protected void adjustTool() {
		getCurrentTool().adjust();
	}


	public DrawingTool getTool(int tool_id) {
		return tools.get(tool_id);
	}


	public DrawingTool getCurrentTool() {
		return getTool(currentToolIndex);
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
		limitTop = Double.valueOf(uniqueMachinePreferencesNode.get("limit_top", Double.toString(limitTop)));
		limitBottom = Double.valueOf(uniqueMachinePreferencesNode.get("limit_bottom", Double.toString(limitBottom)));
		limitLeft = Double.valueOf(uniqueMachinePreferencesNode.get("limit_left", Double.toString(limitLeft)));
		limitRight = Double.valueOf(uniqueMachinePreferencesNode.get("limit_right", Double.toString(limitRight)));

		paperLeft=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_left",Double.toString(paperLeft)));
		paperRight=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_right",Double.toString(paperRight)));
		paperTop=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_top",Double.toString(paperTop)));
		paperBottom=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_bottom",Double.toString(paperBottom)));

		invertMotor1=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m1invert", invertMotor1?"true":"false"));
		invertMotor2=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m2invert", invertMotor2?"true":"false"));

		bobbinDiameterLeft=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_left_diameter", Double.toString(bobbinDiameterLeft)));
		bobbinDiameterRight=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_right_diameter", Double.toString(bobbinDiameterRight)));

		maxFeedRate=Double.valueOf(uniqueMachinePreferencesNode.get("feed_rate",Double.toString(maxFeedRate)));

		startingPositionIndex=Integer.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex",Integer.toString(startingPositionIndex)));

		// load each tool's settings
		for (DrawingTool tool : tools) {
			tool.loadConfig(uniqueMachinePreferencesNode);
		}

		paperMargin = Double.valueOf(uniqueMachinePreferencesNode.get("paper_margin", Double.toString(paperMargin)));
		reverseForGlass = Boolean.parseBoolean(uniqueMachinePreferencesNode.get("reverseForGlass", reverseForGlass ? "true" : "false"));
		setCurrentToolNumber(Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool", Integer.toString(getCurrentToolNumber()))));
	}


	// Save the machine configuration
	public void saveConfig() {
		// once cloud logic is finished.
		//if(GetCanUseCloud() && SaveConfigToCloud() ) return;
		saveConfigToLocal();
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


	protected void saveConfigToLocal() {
		final Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robotUID));
		uniqueMachinePreferencesNode.put("limit_top", Double.toString(limitTop));
		uniqueMachinePreferencesNode.put("limit_bottom", Double.toString(limitBottom));
		uniqueMachinePreferencesNode.put("limit_right", Double.toString(limitRight));
		uniqueMachinePreferencesNode.put("limit_left", Double.toString(limitLeft));
		uniqueMachinePreferencesNode.put("m1invert", Boolean.toString(invertMotor1));
		uniqueMachinePreferencesNode.put("m2invert", Boolean.toString(invertMotor2));
		uniqueMachinePreferencesNode.put("bobbin_left_diameter", Double.toString(bobbinDiameterLeft));
		uniqueMachinePreferencesNode.put("bobbin_right_diameter", Double.toString(bobbinDiameterRight));
		uniqueMachinePreferencesNode.put("feed_rate", Double.toString(maxFeedRate));
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
	}


	public String getBobbinLine() {
		return "D1 L" + bobbinDiameterLeft + " R" + bobbinDiameterRight;
	}


	public String getConfigLine() {
		return "M101 T" + limitTop
				+ " B" + limitBottom
				+ " L" + limitLeft
				+ " R" + limitRight
				+ " I" + (invertMotor1 ? "-1" : "1")
				+ " J" + (invertMotor2 ? "-1" : "1");
	}


	public String getPenUpString() {
		return Float.toString(getCurrentTool().getZOff());
	}

	public String getPenDownString() {
		return Float.toString(getCurrentTool().getZOn());
	}

	public boolean isPaperConfigured() {
		return (paperTop > paperBottom && paperRight > paperLeft);
	}
	
	public void parseRobotUID(String line) {
		saveConfig();

		// get the UID reported by the robot
		String[] lines = line.split("\\r?\\n");
		long new_uid = 0;
		if (lines.length > 0) {
			try {
				new_uid = Long.parseLong(lines[0]);
			} catch (NumberFormatException e) {
				logger.error("{}", e);
			}
		}

		// new robots have UID=0
		if (new_uid == 0) {
			new_uid = getNewRobotUID();
		}

		// load machine specific config
		loadConfig(new_uid);

		if (limitTop == 0 && limitBottom == 0 && limitLeft == 0 && limitRight == 0) {
			// probably first time turning on, adjust the machine size
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(gui,translator,this);
			m.run();
		}
	}

	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/drawbot_getuid.php");
			URLConnection conn = url.openConnection();
			try (
					final InputStream connectionInputStream = conn.getInputStream();
					final Reader inputStreamReader = new InputStreamReader(connectionInputStream);
					final BufferedReader rd = new BufferedReader(inputStreamReader)
					) {
				String line = rd.readLine();
				new_uid = Long.parseLong(line);
			}
		} catch (Exception e) {
			logger.error("{}", e);
			return 0;
		}

		// did read go ok?
		if (new_uid != 0) {
			// make sure a topLevelMachinesPreferenceNode node is created
			topLevelMachinesPreferenceNode.node(Long.toString(new_uid));
			// tell the robot it's new UID.
			gui.sendLineToRobot("UID " + new_uid);

			// if this is a new robot UID, update the list of available configurations
			final String[] new_list = new String[machineConfigurationsAvailable.length + 1];
			System.arraycopy(machineConfigurationsAvailable, 0, new_list, 0, machineConfigurationsAvailable.length);
			new_list[machineConfigurationsAvailable.length] = Long.toString(new_uid);
			machineConfigurationsAvailable = new_list;
		}
		return new_uid;
	}


	/**
	 * @return the number of machine configurations that exist on this computer
	 */
	public int getMachineCount() {
		return machineConfigurationsAvailable.length;
	}


	/**
	 * Get the UID of every machine this computer recognizes EXCEPT machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getKnownMachineNames() {
		final List<String> machineConfigurationsAvailableArrayAsList = new LinkedList<>(Arrays.asList(machineConfigurationsAvailable));
		if (machineConfigurationsAvailableArrayAsList.contains("0")) {
			machineConfigurationsAvailableArrayAsList.remove("0");
		}
		return Arrays.copyOf(machineConfigurationsAvailableArrayAsList.toArray(), machineConfigurationsAvailableArrayAsList.size(), String[].class);
	}

	/**
	 * Get the UID of every machine this computer recognizes INCLUDING machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 *
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getAvailableConfigurations() {
		return machineConfigurationsAvailable;
	}


	public int getCurrentMachineIndex() {
		for (int i = 0; i < machineConfigurationsAvailable.length; i++) {
			if (machineConfigurationsAvailable[i].equals("0")) continue;
			if (machineConfigurationsAvailable[i].equals(Long.toString(robotUID))) {
				return i;
			}
		}

		return 0;
	}


	public double getPaperWidth() {
		return paperRight - paperLeft;
	}


	public double getPaperHeight() {
		return paperTop - paperBottom;
	}


	public double getPaperScale() {
		double paper_w = getPaperWidth();
		double paper_h = getPaperHeight();

		if (paper_w > paper_h) {
			return paper_h / paper_w;
		} else {
			return paper_w / paper_h;
		}
	}

	public double getFeedRate() {
		return maxFeedRate;
	}

	public void setFeedRate(double f) {
		maxFeedRate = f;
		saveConfig();
	}

	public long getUID() {
		return robotUID;
	}

	public void setCurrentToolNumber(int current_tool) {
		this.currentToolIndex = current_tool;
	}
	public int getCurrentToolNumber() {
		return currentToolIndex;
	}

	public boolean isReverseForGlass() {
		return reverseForGlass;
	}

	public void setReverseForGlass(boolean reverseForGlass) {
		this.reverseForGlass = reverseForGlass;
	}

	public double getLimitTop() {
		return limitTop;
	}

	public void setLimitTop(double limitTop) {
		this.limitTop = limitTop;
	}

	public double getLimitBottom() {
		return limitBottom;
	}

	public void setLimitBottom(double limitBottom) {
		this.limitBottom = limitBottom;
	}

	public double getLimitLeft() {
		return limitLeft;
	}

	public void setLimitLeft(double limitLeft) {
		this.limitLeft = limitLeft;
	}

	public double getLimitRight() {
		return limitRight;
	}

	public void setLimitRight(double limitRight) {
		this.limitRight = limitRight;
	}

	public double getPaperMargin() {
		return paperMargin;
	}

	public void setPaperMargin(double paperMargin) {
		this.paperMargin = paperMargin;	
	}
}
