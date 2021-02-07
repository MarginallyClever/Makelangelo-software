package com.marginallyclever.artPipeline.nodes.fractals;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Gosper curve fractal.
 * @author Dan Royer
 */
public class Generator_GosperCurve extends Node {
	// controls complexity of curve
	private NodeConnectorInt inputOrder = new NodeConnectorInt("Generator_GosperCurve.inputOrder",4);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");
	
	private double turtleStep = 10.0f;
	private double xMax = 0;
	private double xMin = 0;
	private double yMax = 0;
	private double yMin = 0;
	
	public Generator_GosperCurve() {
		super();
		inputs.add(inputOrder);
		outputs.add(outputTurtle);
	}

	@Override
	public String getName() {
		return Translator.get("GosperCurveName");
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();

		double v = 100;
		
		turtleStep = 10;
		
		xMax = 0;
		xMin = 0;
		yMax = 0;
		yMin = 0;
		
		gosperA(turtle,inputOrder.getValue());

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
		gosperA(turtle,inputOrder.getValue());

		outputTurtle.setValue(turtle);
	    return false;
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void gosperA(Turtle turtle,int n) {
		if (n == 0) {
			gosperForward(turtle);
			return;
		}
		gosperA(turtle,n-1);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(turtle,n-1);
		gosperA(turtle,n-1);
		turtle.turn(60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
	}


	// Gosper curve B = +A-BB--B-A++A+B
	public void gosperB(Turtle turtle,int n) {
		if (n == 0) {
			gosperForward(turtle);
			return;
		}
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		gosperB(turtle,n-1);
	}


	public void gosperForward(Turtle turtle) {
		turtle.forward(turtleStep);
		if(xMax<turtle.getX()) xMax=turtle.getX();
		if(xMin>turtle.getX()) xMin=turtle.getX();
		if(yMax<turtle.getY()) yMax=turtle.getY();
		if(yMin>turtle.getY()) yMin=turtle.getY();
	}
}
