package com.marginallyclever.convenience.turtle;

import com.marginallyclever.convenience.ColorRGB;

/**
 * TurtleMoves describe the path of turtle motion.
 * @author Dan Royer
 *
 */
public class TurtleMove {
	public boolean isUp;
	public double x,y;  // destination
	
	public TurtleMove(double x,double y,boolean isUp) {
		this.x=x;
		this.y=y;
		this.isUp=isUp;
	}
	
	public TurtleMove(TurtleMove m) {
		this.x=m.x;
		this.y=m.y;
		this.isUp=m.isUp;
	}

	public ColorRGB getColor() {
		return new ColorRGB((int)x);
	}
}