package com.marginallyclever.makelangelo.makeart.turtlegenerator.grid;

import com.marginallyclever.convenience.Clipper2D;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_GraphPaper extends TurtleGenerator {
	private static double angle = 0;

	public Generator_GraphPaper() {
		super();

		SelectDouble angle;

		add(angle = new SelectDouble("order",Translator.get("HilbertCurveOrder"),Generator_GraphPaper.getAngle()));

		angle.addSelectListener(evt->{
			setAngle(angle.getValue());
			generate();
		});
	}

	@Override
	public String getName() {
		return Translator.get("GraphPaperName");
	}

	static public double getAngle() {
		return angle;
	}
	static public void setAngle(double value) {
		angle = value;
	}

	@Override
	public void generate() {
		Turtle turtle = new Turtle();
		turtle.setColor(Color.RED);
		lines(turtle,10,0);
		lines(turtle,10,90);
		turtle.setColor(Color.BLACK);
		lines(turtle,100,0);
		lines(turtle,100,90);

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}

	protected void lines(Turtle turtle,float stepSize_mm,int angle_deg) {
		double majorX = Math.cos(Math.toRadians(angle_deg));
		double majorY = Math.sin(Math.toRadians(angle_deg));

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		double dy = (yMax - yMin)/2;
		double dx = (xMax - xMin)/2;
		double radius = Math.sqrt(dx*dx+dy*dy);

		Point2d P0=new Point2d();
		Point2d P1=new Point2d();

		Point2d rMax = new Point2d(xMax,yMax);
		Point2d rMin = new Point2d(xMin,yMin);
		
		int i=0;
		for(double a = -radius;a<radius;a+=stepSize_mm) {
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
					turtle.penUp();
				} else {
					turtle.moveTo(P1.x,P1.y);
					turtle.penDown();
					turtle.moveTo(P0.x,P0.y);
					turtle.penUp();
				}
			}
			++i;
		}
	}
}
