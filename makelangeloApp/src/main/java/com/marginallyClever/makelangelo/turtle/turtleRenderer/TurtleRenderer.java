package com.marginallyClever.makelangelo.turtle.turtleRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.makelangelo.turtle.TurtleMove;

public interface TurtleRenderer {
	void start(GL2 gl2);

	void draw(TurtleMove p0, TurtleMove p1);
	
	void travel(TurtleMove p0, TurtleMove p1);

	void end();

	void setPenDownColor(ColorRGB color);

	void setPenUpColor(ColorRGB color);
	
	void setPenDiameter(double d);
}
