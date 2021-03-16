package com.marginallyclever.makelangelo.plotter;

import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.makelangelo.preview.RendersInOpenGL;

/**
 * Properties of each plotter style. These are non-configurable features
 * unique to each machine.
 * @author Dan Royer
 */
public abstract interface PlotterModel extends RendersInOpenGL {
	public String getHello();

	/**
	 * Each {@link PlotterModel} has a hardware version for class identification
	 */
	public String getVersion();

	public String getName();

	public Point2D getHome();

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
	 * custom look and feel for each version
	 * 
	 * @param gl2
	 * @param plotter
	 */
	public void render(GL2 gl2);

	/**
	 * hardware specific gcode included at the start of each program
	 * @param out
	 * @throws IOException
	 */
	public void writeProgramStart(Writer out) throws IOException;

	/**
	 * Hardware specific gcode sent on connect
	 * @param settings
	 * @return
	 */
	public String getGCodeConfig();

	/**
	 * hardware specific gcode at the end of each program
	 * @param out
	 * @throws IOException
	 */
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
