package com.marginallyclever.communications;


/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public interface MarginallyCleverConnection {
	void closeConnection();

	void openConnection(String portName) throws Exception;

	void reconnect() throws Exception;

	boolean isOpen();

	String getRecentConnection();

	void sendMessage(String msg) throws Exception;

	public void addListener(MarginallyCleverConnectionReadyListener listener);

	public void removeListener(MarginallyCleverConnectionReadyListener listener);
}
