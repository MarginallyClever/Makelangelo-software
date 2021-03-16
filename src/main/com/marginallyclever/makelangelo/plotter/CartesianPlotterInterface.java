package com.marginallyclever.makelangelo.plotter;

import javax.vecmath.Point3d;

/**
 * The interface all Cartesian (xyz) plotters share.
 * @author Dan Royer
 * @since 7.25.0
 */
public abstract interface CartesianPlotterInterface {
	public void setPosition(Point3d p);
	
	public Point3d getPosition();
	
	public void lineTo(Point3d destination);
	
	// not shared by lame plotters
	//public void findHome();
	
	// could be implemented on top of any Cartesian plotter.
	//public void arcTo(Point3d destination);
}
