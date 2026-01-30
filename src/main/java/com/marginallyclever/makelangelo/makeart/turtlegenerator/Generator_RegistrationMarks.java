package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Draws registration marks intended for cutting the margins off and aligning multiple sheets into a single large image.
 * @author Dan Royer
 */
public class Generator_RegistrationMarks extends TurtleGenerator {
	public final static double DEFAULT_SIZE = 10.0;
	private double size = DEFAULT_SIZE;

	@Override
	public String getName() {
		return Translator.get("Generator_RegistrationMarks.Name");
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		var w = myPaper.getPaperWidth();
		var h = myPaper.getPaperHeight();
		var margin = myPaper.getPaperMargin();
		var halfWidth = (w - (w*margin)) / 4.0;
		var halfHeight = (h - (h*margin)) / 4.0;
		var minSize = Math.min(halfWidth, halfHeight) * 0.8;
		size = Math.min(DEFAULT_SIZE, minSize);

		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		Turtle turtle = new Turtle();
		drawMark(turtle, xMin-halfWidth, yMin);
		drawMark(turtle, xMin, yMin-halfHeight);
		drawMark(turtle, xMax, yMin-halfHeight);
		drawMark(turtle, xMax+halfWidth, yMin);
		drawMark(turtle, xMax+halfWidth, yMax);
		drawMark(turtle, xMax, yMax+halfHeight);
		drawMark(turtle, xMin, yMax+halfHeight);
		drawMark(turtle, xMin-halfWidth, yMax);

		notifyListeners(turtle);
	}

	private void drawMark(Turtle turtle,double cx, double cy) {
		turtle.jumpTo(cx-size,cy);
		turtle.moveTo(cx+size,cy);
		turtle.jumpTo(cx,cy-size);
		turtle.moveTo(cx,cy+size);
	}
}
