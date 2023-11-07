package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Draws a spiral.
 * @author Dan Royer
 *
 */
public class Generator_Spiral extends TurtleGenerator {
	private static double radius=10;
	private static boolean toCorners=false;

	public Generator_Spiral() {
		super();
		SelectDouble chooseRadius = new SelectDouble("radius", Translator.get("Generator_Spiral.radius"), radius);
		add(chooseRadius);
		chooseRadius.addSelectListener(e->{
			radius = Math.max(0,chooseRadius.getValue());
			generate();
		});
		SelectBoolean chooseToCorners = new SelectBoolean("toCorners", Translator.get("Spiral.toCorners"), toCorners);
		add(chooseToCorners);
		chooseToCorners.addSelectListener(e->{
			toCorners = chooseToCorners.isSelected();
			generate();
		});
	}

	@Override
	public String getName() {
		return Translator.get("Generator_Spiral.name");
	}

	@Override
	public void generate() {
		Turtle turtle = new Turtle();
		double cx = Math.cos(Math.toRadians(0));
		double cy = Math.sin(Math.toRadians(0));

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double maxr;
		if(toCorners) {
			// go right to the corners
			double h = rect.getHeight();
			double w = rect.getWidth();
			maxr = Math.sqrt(h * h + w * w)/2 + 1.0;
		} else {
			// do the largest circle that still fits in the image.
			double w = rect.getWidth()/2.0f;
			double h = rect.getHeight()/2.0f;
			maxr = Math.min(h, w);
		}

		int i;

		double r = maxr;
		double fx, fy;
		while (r > radius/2) {
			// find circumference of current circle
			double c1 = Math.floor((2.0f * r - radius) * Math.PI);

			for (i = 0; i < c1; ++i) {
				double p = (double)i / c1;
				double f = Math.PI * 2.0 * p;
				double r1 = r - radius * p;
				fx = cx + Math.cos(f) * r1;
				fy = cy + Math.sin(f) * r1;

				turtle.moveTo(fx, fy);
				if(rect.contains(fx, fy)) turtle.penDown();
				else turtle.penUp();
			}
			r -= radius;
		}

		notifyListeners(turtle);
	}
}
