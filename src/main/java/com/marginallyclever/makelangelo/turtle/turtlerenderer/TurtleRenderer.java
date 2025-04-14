package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import javax.vecmath.Point2d;
import java.awt.*;

/**
 * {@link TurtleRenderer} draws a {@link com.marginallyclever.makelangelo.turtle.Turtle} in a specific style.
 */
public interface TurtleRenderer {
	void start(Graphics2D gl2);

	void draw(Point2d p0, Point2d p1);
	
	void travel(Point2d p0, Point2d p1);

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

	void setShowTravel(boolean showTravel);
}
