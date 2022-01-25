package com.marginallyclever.makelangelo.turtle.turtleRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

public interface TurtleRenderer {
	void start(GL2 gl2);

	void draw(TurtleMove p0, TurtleMove p1);
	
	void travel(TurtleMove p0, TurtleMove p1);

	void end();

	void setPenDownColor(ColorRGB color);
	
	void setPenDiameter(double d);
}
