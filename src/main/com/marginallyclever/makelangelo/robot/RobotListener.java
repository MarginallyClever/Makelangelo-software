package com.marginallyclever.makelangelo.robot;

import java.beans.PropertyChangeListener;

public abstract interface RobotListener extends PropertyChangeListener {
	// called when live robot is ready to receive more commands.
	public void sendBufferEmpty(RobotController r);
	// called whenever data arrives from serial connection, regardless of confirmation.
	public void dataAvailable(RobotController r,String data);
	// called when robot connection is disconnected
	public void disconnected(RobotController r);
	// called when live robot has detected an error in the transmission
	public void lineError(RobotController r,int lineNumber);
	// called when the firmware on the robot is detected as out of date
	public void firmwareVersionBad(RobotController r,long versionFound);
	// called when robot connection is confirmed.  connection is open to a valid robot with good hardware and firmware.
	public void connectionConfirmed(RobotController r);
}
