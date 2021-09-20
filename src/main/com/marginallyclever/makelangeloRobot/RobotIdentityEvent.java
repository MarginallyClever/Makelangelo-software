package com.marginallyclever.makelangeloRobot;

import java.util.EventObject;

public class RobotIdentityEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4740144769141897500L;
	public static final int IDENTITY_CONFIRMED = 0;
	public static final int BAD_FIRMWARE = 1;
	public static final int BAD_HARDWARE = 2;
	
	public int flag;
	public Object data;
	
	public RobotIdentityEvent(Object source,int eventType,Object extra) {
		super(source);
		flag = eventType;
		data = extra;
	}

}
