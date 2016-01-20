package com.marginallyclever.makelangelo;

public interface MakelangeloRobotListener {
	// only called when robot connection is confirmed
	public void portConfirmed(MakelangeloRobot r);
	// called whenever data arrives from serial connection, regardless of confirmation.
	public void dataAvailable(MakelangeloRobot r,String data);
	// called when live robot is ready to receive more commands.
	public void connectionReady(MakelangeloRobot r);
	// called when live robot has detected an error in the transmission
	public void lineError(MakelangeloRobot r,int lineNumber);
}
