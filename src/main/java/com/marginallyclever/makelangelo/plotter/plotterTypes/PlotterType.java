package com.marginallyclever.makelangelo.plotter.plotterTypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.plotter.Plotter;

/**
 * Each {@link PlotterType} renders a {@link PlotterType} with custom graphics.
 * They get all the information they need from the {@link Plotter} passed to the
 * {@code render()} method.
 * 
 * @author Dan Royer
 */
public abstract interface PlotterType {
	public String getName();

	/**
	 * Custom look and feel for each version
	 * 
	 * @param gl2   the render context
	 * @param robot the machine to draw.
	 */
	public void render(GL2 gl2, Plotter robot);
}
