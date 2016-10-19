package com.marginallyclever.communications;


/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public interface NetworkConnection {
	// close this connection
	public void closeConnection();

	// open a connection to a connection
	public void openConnection(String connectionName) throws Exception;

	public void reconnect() throws Exception;

	public boolean isOpen();

	public String getRecentConnection();

	public void sendMessage(String msg) throws Exception;

	public void addListener(NetworkConnectionListener listener);

	public void removeListener(NetworkConnectionListener listener);
	
	public TransportLayer getTransportLayer();
}
