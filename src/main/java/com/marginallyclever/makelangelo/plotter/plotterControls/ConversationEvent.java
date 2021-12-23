package com.marginallyclever.makelangelo.plotter.plotterControls;

public class ConversationEvent {
	public String whoSpoke;
	public String whatWasSaid;
	
	public ConversationEvent(String src,String msg) {
		whoSpoke=src;
		whatWasSaid=msg;
	}
	
	@Override
	public String toString() {
		return whoSpoke +": "+whatWasSaid;
	}
}