package com.marginallyclever.communications;

import java.util.ArrayList;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkSession {
	private ArrayList<NetworkSessionListener> listeners = new ArrayList<NetworkSessionListener>();
	private NetworkSessionLog log = new NetworkSessionLog();
	private String name = "";
		
	public abstract void closeConnection();

	public abstract void openConnection(String connectionName) throws Exception;

	public abstract boolean isOpen();

	public abstract void sendMessage(String msg) throws Exception;
	
	public NetworkSessionLog getLog() {
		return log;
	}
	
	protected void clearLog() {
		log.clear();
	}
	
	protected void addLog(ConversationEvent e) {
		log.addElement(e);
	}
	
	protected void setName(String s) {
		name=s;
	}
	
	public String getName() {
		return name;
	}
	
	// OBSERVER PATTERN

	public void addListener(NetworkSessionListener listener) {
		listeners.add(listener);
	}
	public void removeListener(NetworkSessionListener listener) {
		listeners.remove(listener);
	}
	protected void notifyListeners(NetworkSessionEvent evt) {
		for( NetworkSessionListener a : listeners ) {
			a.networkSessionEvent(evt);
		}
	}

	// OBSERVER CONVENIENCE METHODS
	
	protected void notifyLineError(int lineNumber) {
		notifyListeners(new NetworkSessionEvent(this,NetworkSessionEvent.TRANSPORT_ERROR,lineNumber));	
	}

	protected void notifySendBufferEmpty() {
		notifyListeners(new NetworkSessionEvent(this,NetworkSessionEvent.SEND_BUFFER_EMPTY,null));	
	}

	// tell all listeners data has arrived
	protected void notifyDataAvailable(String line) {
		notifyListeners(new NetworkSessionEvent(this,NetworkSessionEvent.DATA_AVAILABLE,line));
	}
}
