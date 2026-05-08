package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Center the input {@link Turtle} to the origin.
 * @author Dan Royer
 */
public class CenterTurtleToPaperAction extends TurtleTool {
	private final Paper paper;

	public CenterTurtleToPaperAction(Paper paper, String name) {
		super(name);
		this.paper = paper;
	}
	
	@Override
	public Turtle run(Turtle turtle) {
		Rectangle2D.Double turtleBounds = turtle.getBounds(); // image bounds

		double ix = paper.getCenterX() - turtleBounds.getCenterX();
		double iy = paper.getCenterY() - turtleBounds.getCenterY();

		// apply
		Turtle result = new Turtle(turtle);
		result.translate(ix,iy);
		
		return result;
	}
}
