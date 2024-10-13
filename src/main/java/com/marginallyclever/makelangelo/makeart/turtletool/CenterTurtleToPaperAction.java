package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Center the input {@link Turtle} to the origin.
 * @author Dan Royer
 */
public class CenterTurtleToPaperAction extends TurtleTool {

	public CenterTurtleToPaperAction(String name) {
		super(name);
	}
	
	@Override
	public Turtle run(Turtle turtle) {
		Rectangle2D.Double turtleBounds = turtle.getBounds(); // image bounds

		double ix = turtleBounds.getCenterX();
		double iy = turtleBounds.getCenterY();
		
		// apply
		Turtle result = new Turtle(turtle);
		result.translate(-ix,-iy);
		
		return result;
	}
}
