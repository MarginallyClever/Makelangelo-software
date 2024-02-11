package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * {@link TurtleRenderer} is an interface class that can draw a
 * {@link com.marginallyclever.makelangelo.turtle.Turtle} in a specific style.
 */
public interface TurtleRenderer {
	void start(GL2 gl2);

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
