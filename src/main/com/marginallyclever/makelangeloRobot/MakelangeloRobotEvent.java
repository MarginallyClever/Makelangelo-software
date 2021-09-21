package com.marginallyclever.makelangeloRobot;

public class MakelangeloRobotEvent {
	// robot has disconnected
	public static final int DISCONNECT = 0;
	// robot is connected and identity confirmed.
	public static final int CONNECTION_READY = 1;
	// bad firmware detected. extra is a String with the version found.
	public static final int BAD_FIRMWARE = 2;
	// bad hardware detected. extra is a String with the version found.
	public static final int BAD_HARDWARE = 3;
	// starting to send gcode to robot
	public static final int START = 4;
	// stopping
	public static final int STOP = 5;
	// requests tool change. extra is a 24 bit RGB value.
	public static final int TOOL_CHANGE = 6;
	// extra is the line number last sent to the robot.
	public static final int PROGRESS_SOFAR = 7;
	// new GCode commands have been given to the robot.
	public static final int NEW_GCODE = 8;
	// motor engaged status has changed.  extra is a boolean with the new state.
	public static final int MOTORS_ENGAGED = 9;
	// robot has homed and is safe to drive.
	public static final int HOME_FOUND = 10;
	
	public MakelangeloRobot subject;
	public int type;
	public Object extra;

	public MakelangeloRobotEvent(int eventType, MakelangeloRobot robot) {
		type = eventType;
		subject = robot;
		extra = null;
	}

	public MakelangeloRobotEvent(int eventType, MakelangeloRobot robot, Object extraData) {
		this(eventType, robot);
		extra = extraData;
	}
}
