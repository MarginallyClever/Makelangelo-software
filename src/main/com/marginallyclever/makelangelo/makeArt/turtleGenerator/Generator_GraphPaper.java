package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.PlotterPanel;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_GraphPaper extends TurtleGenerator {
	private static double angle = 0;

	PlotterPanel robotPanel;
	

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
		turtle = new Turtle();
		turtle.setColor(new ColorRGB(255,0,0));
		lines(10,0);
		lines(10,90);
		turtle.setColor(new ColorRGB(0,0,0));
		lines(100,0);
		lines(100,90);

		notifyListeners(turtle);
	}

	protected void lines(float stepSize_mm,int angle_deg) {
		double majorX = Math.cos(Math.toRadians(angle_deg));
		double majorY = Math.sin(Math.toRadians(angle_deg));

		// from top to bottom of the margin area...
		double yBottom = settings.getMarginBottom();
		double yTop    = settings.getMarginTop()   ;
		double xLeft   = settings.getMarginLeft()  ;
		double xRight  = settings.getMarginRight() ;
		double dy = (yTop - yBottom)/2;
		double dx = (xRight - xLeft)/2;
		double radius = Math.sqrt(dx*dx+dy*dy);

		Point2D P0=new Point2D();
		Point2D P1=new Point2D();

		Point2D rMax = new Point2D(settings.getMarginRight(),settings.getMarginTop());
		Point2D rMin = new Point2D(settings.getMarginLeft(),settings.getMarginBottom());
		
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
