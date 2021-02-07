package com.marginallyclever.artPipeline.nodes.fractals;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_HilbertCurve_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Hilbert Curve fractal.
 * @author Dan Royer
 */
public class Generator_HilbertCurve extends Node {
	// controls complexity of curve
	private NodeConnectorInt inputOrder = new NodeConnectorInt("Generator_HilbertCurve.inputOrder",4);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");
	
	private float turtleStep = 10.0f;
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	
	public Generator_HilbertCurve() {
		super();
		inputs.add(inputOrder);
		outputs.add(outputTurtle);
	}

	@Override
	public String getName() {
		return Translator.get("HilbertCurveName");
	}
	
	@Override
	public NodePanel getPanel() {
		return new Generator_HilbertCurve_Panel(this);
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		double v = 100;
		xMax = v;
		yMax = v;
		xMin = -v;

		turtle.reset();
		turtleStep = (float) ((xMax - xMin) / (Math.pow(2, inputOrder.getValue())));

		// move to starting position
		turtle.moveTo(
				-xMax + turtleStep / 2,
				-yMax + turtleStep / 2);
		turtle.penDown();
		hilbert(turtle,inputOrder.getValue());

		outputTurtle.setValue(turtle);
	    return false;
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
