package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;

/**
 * Each {@link PlotterRenderer} renders a {@link Plotter} with custom graphics.
 * {@link PlotterRenderer}s do not store any state data.  They must rely on {@link Plotter} and {@link PlotterSettings}.
 * {@code render()} method.
 * 
 * @author Dan Royer
 */
public interface PlotterRenderer {
	/**
	 * Custom look and feel for each version
	 * 
	 * @param gl2   the render context
	 * @param robot the machine to draw.
	 */
	public void render(GL2 gl2, Plotter robot);
}
