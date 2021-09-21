package com.marginallyclever.communications;

public class ConversationEvent {
	public String who;
	public String what;
	public long   when;
	
	public ConversationEvent(String src,String msg,long time) {
		who=src;
		what=msg;
		when=time;
	}
	
	@Override
	public String toString() {
		return when + ": " + who + ": " + what;
	}
}
