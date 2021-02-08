package com.marginallyclever.core.turtle;

import com.marginallyclever.core.ColorRGB;

public abstract interface TurtleRenderer {
	abstract void start();

	abstract void draw(TurtleMove p0, TurtleMove p1);
	
	abstract void travel(TurtleMove p0, TurtleMove p1);

	abstract void end();

	abstract void setPenDownColor(ColorRGB color);
}
