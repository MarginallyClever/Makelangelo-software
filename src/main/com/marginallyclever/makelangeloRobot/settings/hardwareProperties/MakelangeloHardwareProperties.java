package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * Properties of each Makelangelo version. These are non-configurable features
 * unique to each machine.
 * 
 * @author Dan Royer
 *
 */
public interface MakelangeloHardwareProperties {
	public String getVersion();

	public String getName();

	public Point2D getHome(MakelangeloRobotSettings settings);

	public boolean canChangeMachineSize();

	public boolean canAccelerate();

	public boolean canInvertMotors();

	/**
	 * @return true if the machine has limit switches and can find home on its own.
	 */
	public boolean canAutoHome();

	/**
	 * Can this machine's home position be adjusted via gcode?
	 * 
	 * @return true if it can
	 */
	public boolean canChangeHome();

	/**
	 * @return default machine size, in mm
	 */
	public float getWidth();

	/**
	 * @return default machine size, in mm
	 */
	public float getHeight();

	/**
	 * custom look & feel for each version
	 * 
	 * @param gl2
	 */
	public void render(GL2 gl2, MakelangeloRobot robot);

	/**
	 * hardware specific gcode included at the start of each program
	 * @param out
	 * @throws IOException
	 */
	public void writeProgramStart(Writer out) throws IOException;

	/**
	 * Hardware specific gcode sent on connect
	 * @param out
	 * @throws IOException
	 */
	public String getGCodeConfig(MakelangeloRobotSettings settings);

	public void writeProgramEnd(Writer out) throws IOException;

	// @since hardware m2
	public float getFeedrateMax();

	// @since hardware m2
	public float getFeedrateDefault();

	// @since hardware m2
	public float getAccelerationMax();

	// @since hardware m2
	public float getZRate();

	// @since hardware m2
	public float getZAngleOn();

	// @since hardware m2
	public float getZAngleOff();
}
