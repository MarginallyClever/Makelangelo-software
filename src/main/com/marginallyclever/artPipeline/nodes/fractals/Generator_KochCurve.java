package com.marginallyclever.artPipeline.nodes.fractals;

import com.marginallyclever.artPipeline.nodes.TurtleGenerator;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Koch Curve fractal
 * @author Dan Royer
 */
public class Generator_KochCurve extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorInteger inputOrder = new NodeConnectorBoundedInt("Generator_KochCurve.inputOrder",15,1,4);
	
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	private double yMin = -7;
	private double maxSize;
	
	public Generator_KochCurve() {
		super();
		inputs.add(inputOrder);
	}
	
	@Override
	public String getName() {
		return Translator.get("Generator_KochCurve.name");
	}

	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		double v = 100;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		// move to starting position
		turtle.moveTo(0,-yMax);
		turtle.turn(90);
		
		turtle.penDown();
		drawTriangle(turtle,inputOrder.getValue(), maxSize);

		outputTurtle.setValue(turtle);
		
	    return false;
	}


	// L System tree
	private void drawTriangle(Turtle turtle,int n, double distance) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		drawTriangle(turtle,n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtle.forward(distance/3.0f);
		}
		drawTriangle(turtle,n-1,distance/3.0f);
	}
}
