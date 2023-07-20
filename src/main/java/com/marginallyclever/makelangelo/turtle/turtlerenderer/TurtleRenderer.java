package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * Interface for a TurtleRenderer.  A {@link TurtleRenderer} is a class that can draw a
 * {@link com.marginallyclever.makelangelo.turtle.Turtle} in a given style.
 */
public interface TurtleRenderer {
	void start(GL2 gl2);

	void draw(TurtleMove p0, TurtleMove p1);
	
	void travel(TurtleMove p0, TurtleMove p1);

	void end();

	void setPenDownColor(ColorRGB color);

	void setPenUpColor(ColorRGB color);
	
	void setPenDiameter(double d);

	String getTranslatedName();
}
