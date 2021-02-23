package com.marginallyclever.makelangelo.nodes.fractals;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;

/**
 * Hilbert Curve fractal.
 * @author Dan Royer
 */
public class Generator_HilbertCurve extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorInteger inputOrder = new NodeConnectorBoundedInt("Generator_HilbertCurve.inputOrder",15,1,4);
	
	private float turtleStep = 10.0f;
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	
	public Generator_HilbertCurve() {
		super();
		inputs.add(inputOrder);
	}

	@Override
	public String getName() {
		return Translator.get("Generator_HilbertCurve.name");
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
