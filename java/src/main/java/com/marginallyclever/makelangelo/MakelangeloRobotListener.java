package com.marginallyclever.makelangelo;

public interface MakelangeloRobotListener {
	// only called when robot connection is confirmed
	public void portConfirmed(MakelangeloRobot r);
	// called whenever data arrives from serial connection, regardless of confirmation.
	public void dataAvailable(MakelangeloRobot r,String data);
}
