package com.marginallyclever.communications;

import java.util.ArrayList;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkSession {
	// Listeners which should be notified of a change to the percentage.
	private ArrayList<NetworkSessionListener> listeners = new ArrayList<NetworkSessionListener>();
	
	// close this connection
	public abstract void closeConnection();

	// open a connection to a connection
	public abstract void openConnection(String connectionName) throws Exception;

	public abstract void reconnect() throws Exception;

	public abstract boolean isOpen();

	public abstract String getRecentConnection();

	public abstract void sendMessage(String msg) throws Exception;

	public abstract TransportLayer getTransportLayer();
	

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
