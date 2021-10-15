package com.marginallyclever.makelangelo.plotter.plotterTypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;

/**
 * Each unique plotter derives {@link PlotterType} and extends it with custom features and graphics.
 * @author Dan Royer
 */
public abstract interface PlotterType {
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
	 * @return true if this machine's home position be adjusted via gcode
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
	 * @param gl2
	 * @param robot the machine to draw.  TODO wtf?
	 */
	public void render(GL2 gl2, Plotter robot);

	/**
	 * @return Hardware specific gcode sent on connect
	 */
	@Deprecated
	public String getGCodeConfig(Plotter robot);

	public float getFeedrateMax();

	public float getFeedrateDefault();
	
	public float getAccelerationMax();
	
	public float getPenLiftTime();
	
	public float getZAngleOn();
	
	public float getZAngleOff();
}
