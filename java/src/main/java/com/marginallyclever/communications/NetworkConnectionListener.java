package com.marginallyclever.communications;


public interface NetworkConnectionListener {
	/**
	 * transmission of message number 'lineNumber' has failed.
	 * @param arg0
	 * @param lineNumber
	 */
	public void lineError(NetworkConnection arg0,int lineNumber);
	/**
	 * The outbound data buffer is empty.
	 * @param arg0
	 */
	public void sendBufferEmpty(NetworkConnection arg0);

	/**
	 * inbound data has arrived. 
	 * @param arg0
	 * @param data
	 */
	public void dataAvailable(NetworkConnection arg0,String data);
}
