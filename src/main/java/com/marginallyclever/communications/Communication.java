package com.marginallyclever.communications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class Communication {

	private String name = "";
		
	public abstract void closeConnection();

	public abstract void openConnection(String connectionName) throws Exception;

	public abstract boolean isOpen();

	public abstract void sendMessage(String msg) throws Exception;
		
	protected void setName(String s) {
		name=s;
	}
	
	public String getName() {
		return name;
	}
	
	// OBSERVER PATTERN

	private List<CommunicationListener> listeners = new ArrayList<>();

	public void addListener(CommunicationListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(CommunicationListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(CommunicationEvent evt) {
		for( CommunicationListener a : listeners ) {
			a.networkSessionEvent(evt);
		}
	}

	// OBSERVER CONVENIENCE METHODS

	// tell all listeners data has arrived
	protected void notifyDataReceived(String line) {
		notifyListeners(new CommunicationEvent(this, CommunicationEvent.DATA_RECEIVED,line));
	}

	// tell all listeners data has arrived
	protected void notifyDataSent(String line) {
		notifyListeners(new CommunicationEvent(this, CommunicationEvent.DATA_SENT,line));
	}
}
