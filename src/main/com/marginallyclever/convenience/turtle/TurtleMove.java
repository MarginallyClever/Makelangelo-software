package com.marginallyclever.convenience.turtle;

import com.marginallyclever.convenience.ColorRGB;

public class TurtleMove {
	public TurtleMoveType type;
	public double x,y;  // destination
	
	public TurtleMove(double x0,double y0,TurtleMoveType type0) {
		x=x0;
		y=y0;
		type=type0;
	}
	
	public TurtleMove(TurtleMove m) {
		this.x=m.x;
		this.y=m.y;
		this.type=m.type;
	}

	public ColorRGB getColor() {
		return new ColorRGB((int)x);
	}
}