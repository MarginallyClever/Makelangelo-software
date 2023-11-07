package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Draws a border around the paper.  Uses current paper myPaper.
 * @author Dan Royer
 *
 */
public class Generator_Border extends TurtleGenerator {
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		Turtle turtle = new Turtle();
		turtle.penUp();
		turtle.moveTo(xMin,yMax);
		turtle.penDown();
		turtle.moveTo(xMin,yMax);
		turtle.moveTo(xMax,yMax);
		turtle.moveTo(xMax,yMin);
		turtle.moveTo(xMin,yMin);
		turtle.moveTo(xMin,yMax);
		
		notifyListeners(turtle);
	}
}
