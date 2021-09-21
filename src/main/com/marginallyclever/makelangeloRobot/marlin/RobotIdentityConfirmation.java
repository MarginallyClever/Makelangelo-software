package com.marginallyclever.makelangeloRobot.marlin;

import java.util.ArrayList;

import com.marginallyclever.communications.NetworkSessionListener;

public abstract class RobotIdentityConfirmation implements NetworkSessionListener {
	private boolean identityConfirmed;
	private boolean portConfirmed;
	
	
	abstract public void reset();
	
	abstract public void start();

	protected void setIdentityConfirmed(boolean state) {
		identityConfirmed=state;
	}
	
	public boolean getIdentityConfirmed() {
		return identityConfirmed;
	}

	protected void setPortConfirmed(boolean state) {
		portConfirmed=state;
	}
	
	public boolean getPortConfirmed() {
		return portConfirmed;
	}
	
	// OBSERVER PATTERN

	private ArrayList<RobotIdentityEventListener> listeners = new ArrayList<RobotIdentityEventListener>();
	public void addRobotIdentityEventListener(RobotIdentityEventListener a) {
		listeners.add(a);
	}
	
	public void removeRobotIdentityEventListener(RobotIdentityEventListener a) {
		listeners.remove(a);
	}
	
	protected void notifyListeners(RobotIdentityEvent e) {
		for( RobotIdentityEventListener a : listeners ) {
			a.robotIdentityEvent(e);
		}
	}
}
