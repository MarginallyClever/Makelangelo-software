package com.marginallyclever.makelangeloRobot;

import java.util.EventObject;

public class MarlinEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7301345381201547866L;

	public static final int SEND_BUFFER_EMPTY = 0;

	public static final int TRANSPORT_ERROR = 1;

	public static final int DATA_RECEIVED = 2;
	
	public int flag;
	public Object data;
	
	public MarlinEvent(Object source,int flag,Object data) {
		super(source);
		this.flag = flag;
		this.data = data;
	}
}
