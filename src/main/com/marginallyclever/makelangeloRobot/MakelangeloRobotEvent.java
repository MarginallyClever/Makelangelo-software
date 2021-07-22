package com.marginallyclever.makelangeloRobot;

public class MakelangeloRobotEvent {
	public MakelangeloRobot subject;
	public int type;
	public Object extra;
	
	public MakelangeloRobotEvent(int eventType, MakelangeloRobot robot) {
		type=eventType;
		subject = robot;
		extra=null;
	}

	public MakelangeloRobotEvent(int eventType, MakelangeloRobot robot, Object extraData) {
		this(eventType,robot);
		extra=extraData;
	}

	public static final int DISCONNECT = 0;
	public static final int CONNECTION_READY = 1;
	public static final int BAD_FIRMWARE = 2;
	public static final int BAD_HARDWARE = 3;
	public static final int START = 4;
	public static final int STOP = 5;
	public static final int TOOL_CHANGE = 6;
}
