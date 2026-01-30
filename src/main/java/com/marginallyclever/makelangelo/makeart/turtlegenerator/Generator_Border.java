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
		return Translator.get("Generator_Border.Name");
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();
		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();

		Turtle turtle = new Turtle();
		turtle.penUp();
		turtle.moveTo(cx+xMin,cy+yMax);
		turtle.penDown();
		turtle.moveTo(cx+xMin,cy+yMax);
		turtle.moveTo(cx+xMax,cy+yMax);
		turtle.moveTo(cx+xMax,cy+yMin);
		turtle.moveTo(cx+xMin,cy+yMin);
		turtle.moveTo(cx+xMin,cy+yMax);
		
		notifyListeners(turtle);
	}
}
