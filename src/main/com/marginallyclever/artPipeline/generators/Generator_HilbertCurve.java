package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Hilbert Curve fractal.
 * @author Dan Royer
 */
public class Generator_HilbertCurve extends TurtleGenerator {
	private float turtleStep = 10.0f;
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("HilbertCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_HilbertCurve.order = order;
	}
	
	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_HilbertCurve_Panel(this);
	}
	
	@Override
	public Turtle generate() {
		Turtle turtle = new Turtle();
		
		double v = 100;
		xMax = v;
		yMax = v;
		xMin = -v;

		turtle.reset();
		turtleStep = (float) ((xMax - xMin) / (Math.pow(2, order)));

		// move to starting position
		turtle.moveTo(
				-xMax + turtleStep / 2,
				-yMax + turtleStep / 2);
		turtle.penDown();
		hilbert(turtle,order);
	    
	    return turtle;
	}


	// Hilbert curve
	private void hilbert(Turtle turtle,int n) {
		if (n == 0) return;
		turtle.turn(90);
		treblih(turtle, n - 1);
		turtle.forward(turtleStep);
		turtle.turn(-90);
		hilbert(turtle, n - 1);
		turtle.forward(turtleStep);
		hilbert(turtle, n - 1);
		turtle.turn(-90);
		turtle.forward(turtleStep);
		treblih(turtle, n - 1);
		turtle.turn(90);
	}


	// evruc trebliH
	public void treblih(Turtle turtle,int n) {
		if (n == 0) return;
		turtle.turn(-90);
		hilbert(turtle, n - 1);
		turtle.forward(turtleStep);
		turtle.turn(90);
		treblih(turtle, n - 1);
		turtle.forward(turtleStep);
		treblih(turtle, n - 1);
		turtle.turn(90);
		turtle.forward(turtleStep);
		hilbert(turtle, n - 1);
		turtle.turn(-90);
	}
}
