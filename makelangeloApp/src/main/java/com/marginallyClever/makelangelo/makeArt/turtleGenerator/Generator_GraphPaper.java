package com.marginallyClever.makelangelo.makeArt.turtleGenerator;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.convenience.Clipper2D;
import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.convenience.Point2D;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_GraphPaper extends TurtleGenerator {
	private static double angle = 0;

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
	public TurtleGeneratorPanel getPanel() {
		return new Generator_GraphPaper_Panel(this);
	}
	
	@Override
	public void generate() {
		Turtle turtle = new Turtle();
		turtle.setColor(new ColorRGB(255,0,0));
		lines(turtle,10,0);
		lines(turtle,10,90);
		turtle.setColor(new ColorRGB(0,0,0));
		lines(turtle,100,0);
		lines(turtle,100,90);

		notifyListeners(turtle);
	}

	protected void lines(Turtle turtle,float stepSize_mm,int angle_deg) {
		double majorX = Math.cos(Math.toRadians(angle_deg));
		double majorY = Math.sin(Math.toRadians(angle_deg));

		// from top to bottom of the margin area...
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop()   ;
		double xLeft   = myPaper.getMarginLeft()  ;
		double xRight  = myPaper.getMarginRight() ;
		double dy = (yTop - yBottom)/2;
		double dx = (xRight - xLeft)/2;
		double radius = Math.sqrt(dx*dx+dy*dy);

		Point2D P0=new Point2D();
		Point2D P1=new Point2D();

		Point2D rMax = new Point2D(xRight,yTop);
		Point2D rMin = new Point2D(xLeft,yBottom);
		
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
