package com.marginallyclever.communications;

import java.security.InvalidParameterException;

import javax.swing.DefaultListModel;

public class NetworkSessionLog extends DefaultListModel<ConversationEvent> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4060801871711497751L;
	private String name;

	public NetworkSessionLog(Communication s) {
		if(s==null) throw new InvalidParameterException("Communication cannot be null.");
		
		name = s.getName();
		
		s.addListener((e)->{
			if(e.flag== CommunicationEvent.DATA_RECEIVED) {
				String message = (String)e.data;
				addElement(new ConversationEvent("in",message,System.currentTimeMillis()));
			} else if(e.flag== CommunicationEvent.DATA_SENT) {
				String message = (String)e.data;
				addElement(new ConversationEvent("out",message,System.currentTimeMillis()));
			}
		});
	}
	
	public String getName() {
		return name;
	}
}
