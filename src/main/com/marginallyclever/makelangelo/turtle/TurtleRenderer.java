package com.marginallyclever.makelangelo.turtle;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;

public abstract interface TurtleRenderer {
	abstract void start(GL2 gl2);

	abstract void draw(TurtleMove p0, TurtleMove p1);
	
	abstract void travel(TurtleMove p0, TurtleMove p1);

	abstract void end();

	abstract void setPenDownColor(ColorRGB color);
}
