package com.marginallyclever.communications;


public interface MarginallyCleverConnectionReadyListener {
	public void connectionReady(MarginallyCleverConnection arg0);
	public void dataAvailable(MarginallyCleverConnection arg0,String data);
}
