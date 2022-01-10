package com.marginallyclever.communications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkSession {

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

	private List<NetworkSessionListener> listeners = new ArrayList<>();

	public void addListener(NetworkSessionListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(NetworkSessionListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(NetworkSessionEvent evt) {
		for( NetworkSessionListener a : listeners ) {
			a.networkSessionEvent(evt);
		}
	}

	// OBSERVER CONVENIENCE METHODS

	// tell all listeners data has arrived
	protected void notifyDataReceived(String line) {
		notifyListeners(new NetworkSessionEvent(this,NetworkSessionEvent.DATA_RECEIVED,line));
	}

	// tell all listeners data has arrived
	protected void notifyDataSent(String line) {
		notifyListeners(new NetworkSessionEvent(this,NetworkSessionEvent.DATA_SENT,line));
	}
}
