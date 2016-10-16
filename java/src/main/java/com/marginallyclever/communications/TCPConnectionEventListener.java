package com.marginallyclever.communications;

public interface TCPConnectionEventListener {
	public void dataAvailable(int numBytes,byte [] buffer);
}
