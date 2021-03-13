package com.marginallyclever.makelangelo.robot;

import java.beans.PropertyChangeListener;

/**
 * Implemented by anyone listening to the {@link RobotController}
 * @author Dan Royer
 * @since before 7.25.0
 *
 */
public abstract interface PlotterListener extends PropertyChangeListener {
	// called when live robot is ready to receive more commands.
	public void sendBufferEmpty(Plotter r);
	// called whenever data arrives from serial connection, regardless of confirmation.
	public void dataAvailable(Plotter r,String data);
	// called when robot connection is disconnected
	public void disconnected(Plotter r);
	// called when live robot has detected an error in the transmission
	public void lineError(Plotter r,int lineNumber);
	// called when the firmware on the robot is detected as out of date
	public void firmwareVersionBad(Plotter r,long versionFound);
	// called when robot connection is confirmed.  connection is open to a valid robot with good hardware and firmware.
	public void connectionConfirmed(Plotter r);
}
