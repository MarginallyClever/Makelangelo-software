package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.Clipper2D;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Completely fills the page with ink.
 * @author Dan Royer
 */
public class Generator_FillPage extends TurtleGenerator {
	private static double angle = 0;
	private static double penDiameter = 0.8;// must be greater than zero ! (or else infinit loop)

	public Generator_FillPage() {
		super();
		SelectDouble selectAngle = new SelectDouble("order",Translator.get("HilbertCurveOrder"),angle);
		SelectDouble selectPenDiameter = new SelectDouble("penDiameter",Translator.get("penDiameter"),penDiameter);
		add(selectAngle);
		add(selectPenDiameter);
		selectAngle.addSelectListener(evt->{
			angle = selectAngle.getValue();
			generate();
		});
		selectPenDiameter.addSelectListener(evt->{
			penDiameter = selectPenDiameter.getValue();
			generate();

		});
	}

	@Override
	public String getName() {
		return Translator.get("FillPageName");
	}

	@Override
	public void generate() {
		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();
		double height = rect.getHeight();
		double width = rect.getWidth();
		double radius = Math.sqrt(width*width+height*height)/2;

		Turtle turtle = new Turtle();
		turtle.setStroke(Color.BLACK,penDiameter);
		Point2d P0=new Point2d();
		Point2d P1=new Point2d();

		Point2d rMax = new Point2d(xMax,yMax);
		Point2d rMin = new Point2d(xMin,yMin);
		
		int i=0;
		if ( penDiameter > 0 ){
			for(double a = -radius;a<radius;a+=penDiameter) {
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
		}
		// else throw error message "penDiameter must be greater than zero."

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}
}
