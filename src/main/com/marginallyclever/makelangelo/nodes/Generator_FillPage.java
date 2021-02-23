package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.Clipper2D;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorAngle;
import com.marginallyclever.core.turtle.Turtle;

/**
 * Completely fills the page with ink.
 * @author Dan Royer
 */
public class Generator_FillPage extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorAngle inputAngle = new NodeConnectorAngle("Generator_FillPage.inputAngle",0.0);
	
	private double stepSize = 2.0;
	private double yBottom = -100;
	private double yTop    = 100;
	private double xLeft   = -100;
	private double xRight  = 100;

	public Generator_FillPage() {
		super();
		inputs.add(inputAngle);
	}

	@Override
	public String getName() {
		return Translator.get("Generator_FillPage.name");
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		double r = Math.toRadians(inputAngle.getValue());
		double majorX = Math.cos(r);
		double majorY = Math.sin(r);

		// figure out how many lines we're going to have on this image.
		// from top to bottom of the margin area...
		double dy = (yTop - yBottom)/2;
		double dx = (xRight - xLeft)/2;
		double radius = Math.sqrt(dx*dx+dy*dy);

		turtle = new Turtle();
		Point2D P0=new Point2D();
		Point2D P1=new Point2D();

		Point2D rMax = new Point2D(xRight,yTop);
		Point2D rMin = new Point2D(xLeft,yBottom);
		
		int i=0;
		for(double a = -radius;a<radius;a+=stepSize) {
			double majorPX = majorX * a;
			double majorPY = majorY * a;
			P0.set( majorPX - majorY * radius,
					majorPY + majorX * radius);
			P1.set( majorPX + majorY * radius,
					majorPY - majorX * radius);
			if(Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
				if ((i % 2) == 0) 	{
					turtle.moveTo(P0.x,P0.y);
					turtle.penDown();
					turtle.moveTo(P1.x,P1.y);
				} else {
					turtle.moveTo(P1.x,P1.y);
					turtle.penDown();
					turtle.moveTo(P0.x,P0.y);
				}
			}
			++i;
		}

		outputTurtle.setValue(turtle);
	    return false;
	}
}
