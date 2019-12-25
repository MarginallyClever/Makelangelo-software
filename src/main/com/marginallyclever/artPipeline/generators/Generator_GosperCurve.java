package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_GosperCurve extends ImageGenerator {
	private double turtleStep = 10.0f;
	private double xMax = 0;
	private double xMin = 0;
	private double yMax = 0;
	private double yMin = 0;
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("GosperCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_GosperCurve.order = order;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_GosperCurve_Panel(this);
	}
	
	@Override
	public boolean generate() {
		double v = Math.min(machine.getMarginWidth(),machine.getMarginHeight());

		turtle = new Turtle();
		
		turtleStep = 10;
		
		xMax = 0;
		xMin = 0;
		yMax = 0;
		yMin = 0;
		
		gosperA(order);

		turtle = new Turtle();

		// scale the image to fit on the paper
		double w = xMax-xMin;
		double h = yMax-yMin;
		if(w>h) {
			double f = v/w;
			h*=f;
			turtleStep*=f;
			xMax*=f;
			xMin*=f;
			yMax*=f;
			yMin*=f;
		} else {
			double f = v/h;
			w*=f;
			turtleStep*=f;
			xMax*=f;
			xMin*=f;
			yMax*=f;
			yMin*=f;
		}
		// adjust the start position to center the image
		double x = (xMax+xMin)/-2;
		double y = (yMax+yMin)/-2;
		
		// move to starting position
		turtle.penUp();
		turtle.moveTo(x,y);
		turtle.penDown();
		// do the curve
		gosperA(order);
	    
	    return true;
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void gosperA(int n) {
		if (n == 0) {
			gosperForward();
			return;
		}
		gosperA(n-1);
		turtle.turn(-60);
		gosperB(n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(n-1);
		turtle.turn(60);
		gosperA(n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(n-1);
		gosperA(n-1);
		turtle.turn(60);
		gosperB(n-1);
		turtle.turn(-60);
	}


	// Gosper curve B = +A-BB--B-A++A+B
	public void gosperB(int n) {
		if (n == 0) {
			gosperForward();
			return;
		}
		turtle.turn(60);
		gosperA(n-1);
		turtle.turn(-60);
		gosperB(n-1);
		gosperB(n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(n-1);
		turtle.turn(-60);
		gosperA(n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(n-1);
		turtle.turn(60);
		gosperB(n-1);
	}


	public void gosperForward() {
		turtle.forward(turtleStep);
		if(xMax<turtle.getX()) xMax=turtle.getX();
		if(xMin>turtle.getX()) xMin=turtle.getX();
		if(yMax<turtle.getY()) yMax=turtle.getY();
		if(yMin>turtle.getY()) yMin=turtle.getY();
	}
}
