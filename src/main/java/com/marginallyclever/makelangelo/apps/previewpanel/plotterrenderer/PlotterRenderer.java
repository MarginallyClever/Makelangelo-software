package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
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
	 * @param gl      the render context
	 * @param context
	 * @param robot   the machine to draw.
	 */
	void render(RenderContext context, Plotter robot);

	/**
	 * Update the settings for the plotter.
	 *
	 * @param settings the new settings
	 */
	void updatePlotterSettings(PlotterSettings settings);
}
