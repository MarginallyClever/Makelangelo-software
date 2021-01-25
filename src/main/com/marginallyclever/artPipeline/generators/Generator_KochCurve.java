package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Koch Curve fractal
 * @author Dan Royer
 */
public class Generator_KochCurve extends ImageGenerator {
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	private double yMin = -7;
	private static int order = 4; // controls complexity of curve

	private double maxSize;
	
	@Override
	public String getName() {
		return Translator.get("KochTreeName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_KochCurve.order = order;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_KochCurve_Panel(this);
	}
	
	@Override
	public boolean generate() {
		double v = Math.min(machine.getMarginWidth(),machine.getMarginHeight());
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;

		turtle = new Turtle();
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		// move to starting position
		if(machine.getPaperWidth() > machine.getPaperHeight()) {
			turtle.moveTo(-xMax,0);
		} else {
			turtle.moveTo(0,-yMax);
			turtle.turn(90);
		}
		
		turtle.penDown();
		drawTriangle(order, maxSize);
	    
	    return true;
	}


	// L System tree
	private void drawTriangle(int n, double distance) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		drawTriangle(n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangle(n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangle(n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtle.forward(distance/3.0f);
		}
		drawTriangle(n-1,distance/3.0f);
	}
}
