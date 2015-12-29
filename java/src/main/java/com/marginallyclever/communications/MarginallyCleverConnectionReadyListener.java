package com.marginallyclever.communications;


public interface MarginallyCleverConnectionReadyListener {
	public void lineError(MarginallyCleverConnection arg0,int lineNumber);
	public void connectionReady(MarginallyCleverConnection arg0);
	public void dataAvailable(MarginallyCleverConnection arg0,String data);
}
