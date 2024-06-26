package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.apps.previewpanel.ShaderProgram;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

/**
 * Each {@link PlotterRenderer} renders a {@link Plotter} with custom graphics.  That is to say it draws the machine,
 * not the drawing made by the machine.
 * {@link PlotterRenderer}s do not store any state data.  They must rely on {@link Plotter} and {@link PlotterSettings}.
 * {@code render()} method.
 * 
 * @author Dan Royer
 */
public interface PlotterRenderer {
	/**
	 * Custom look and feel for each version
	 * 
	 * @param gl   the render context
	 * @param robot the machine to draw.
	 */
	void render(GL3 gl, Plotter robot, ShaderProgram shaderProgram);

	/**
	 * Update the settings for the plotter.
	 *
	 * @param settings the new settings
	 */
	void updatePlotterSettings(PlotterSettings settings);
}
