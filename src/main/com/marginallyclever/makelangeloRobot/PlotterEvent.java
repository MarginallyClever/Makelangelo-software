package com.marginallyclever.makelangeloRobot;

public class PlotterEvent {
	/*
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
	// extra is the line number last sent to the robot.
	public static final int PROGRESS_SOFAR = 6;
	*/
	// requests tool change. extra is a 24 bit RGB value.
	public static final int TOOL_CHANGE = 7;
	// new GCode commands have been given to the robot.
	public static final int NEW_GCODE = 8;
	// motor engaged status has changed.  extra is a boolean with the new state.
	public static final int MOTORS_ENGAGED = 9;
	// robot has homed and is safe to drive.
	public static final int HOME_FOUND = 10;
	// robot has moved.
	public static final int POSITION = 11;
	// robot has lifted pen.  extra is true if pen is up, false if pen is down.
	public static final int PEN_UP = 12;
	
	public Plotter subject;
	public int type;
	public Object extra;

	public PlotterEvent(int eventType, Plotter robot) {
		type = eventType;
		subject = robot;
		extra = null;
	}

	public PlotterEvent(int eventType, Plotter robot, Object extraData) {
		this(eventType, robot);
		extra = extraData;
	}
}
