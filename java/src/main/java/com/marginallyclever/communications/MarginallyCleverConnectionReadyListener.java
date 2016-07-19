package com.marginallyclever.communications;


public interface MarginallyCleverConnectionReadyListener {
	/**
	 * transmission of message number 'lineNumber' has failed.
	 * @param arg0
	 * @param lineNumber
	 */
	public void lineError(MarginallyCleverConnection arg0,int lineNumber);
	/**
	 * The outbound data buffer is empty.
	 * @param arg0
	 */
	public void sendBufferEmpty(MarginallyCleverConnection arg0);

	/**
	 * inbound data has arrived. 
	 * @param arg0
	 * @param data
	 */
	public void dataAvailable(MarginallyCleverConnection arg0,String data);
}
