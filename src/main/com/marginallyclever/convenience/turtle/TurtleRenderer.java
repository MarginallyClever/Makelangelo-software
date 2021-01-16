package com.marginallyclever.convenience.turtle;

import com.marginallyclever.convenience.ColorRGB;

public abstract interface TurtleRenderer {
	abstract void start();

	abstract void draw(TurtleMove p0, TurtleMove p1);
	
	abstract void travel(TurtleMove p0, TurtleMove p1);

	abstract void end();

	abstract void setPenDownColor(ColorRGB color);
}
