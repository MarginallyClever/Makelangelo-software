package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * {@link TurtleRenderer} draws a {@link com.marginallyclever.makelangelo.turtle.Turtle} in a specific style.
 */
public interface TurtleRenderer {
	void start(RenderContext context);

	void draw(TurtleMove p0, TurtleMove p1);
	
	void travel(TurtleMove p0, TurtleMove p1);

	void end();

	void setPenDownColor(Color color);

	void setPenUpColor(Color color);
	
	void setPenDiameter(double d);

	/**
	 * @return the name of this renderer, translated into the current language.
	 */
	String getTranslatedName();

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	void reset();
}
