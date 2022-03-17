package com.marginallyclever.makelangelo.plotter.plotterControls;

/**
 * {@link ConversationEvent} describes a single entry in a {@link ConversationHistory}.
 * @author Dan Royer
 * @since 7.28.0
 */
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