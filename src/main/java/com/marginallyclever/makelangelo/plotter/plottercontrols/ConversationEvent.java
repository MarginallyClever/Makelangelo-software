package com.marginallyclever.makelangelo.plotter.plottercontrols;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link ConversationEvent} describes a single entry in a {@link ConversationHistory}.
 * @author Dan Royer
 * @since 7.28.0
 */
public class ConversationEvent {
	public String whoSpoke;
	public String whatWasSaid;
	public Date when;
	
	public ConversationEvent(String src,String msg) {
		whoSpoke=src;
		whatWasSaid=msg;
		when = new Date();
	}
	
	@Override
	public String toString() {
		String time = new SimpleDateFormat("HH:mm:ss.SSS").format(when);
		return "["+time+"] " + whoSpoke +": "+whatWasSaid;
	}
}