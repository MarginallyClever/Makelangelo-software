package com.marginallyclever.artPipeline.nodes.fractals;

import com.marginallyclever.artPipeline.Node;
import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorInt;
import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_SierpinskiTriangle_Panel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * @see <a href='https://en.wikipedia.org/wiki/Sierpi%C5%84ski_arrowhead_curve'>Wikipedia</a>
 * @author Dan Royer 
 * @since 2016-12-12
 *
 */
public class Generator_SierpinskiTriangle extends Node {
	// controls complexity of curve
	private NodeConnectorInt inputOrder = new NodeConnectorInt(4);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	private double xMax, xMin, yMax, yMin;
	private double maxSize;
		
	public Generator_SierpinskiTriangle() {
		super();
		inputs.add(inputOrder);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("SierpinskiTriangleName");
	}
	
	@Override
	public NodePanel getPanel() {
		return new Generator_SierpinskiTriangle_Panel(this);
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		xMax = 100;
		yMax = -100;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		maxSize = Math.tan(Math.toRadians(30))*(xx < yy ? xx : yy)*2;
		double jj = Math.asin(Math.toRadians(30))*(xx < yy ? xx : yy);

		// move to starting position
		if(xMax>yMax) {
			turtle.moveTo(-jj,yMin);
		} else {
			turtle.moveTo(xMax,-jj);
			turtle.turn(90);
		}
		turtle.penDown();
		// do the curve
		int order = inputOrder.getValue();
		
		if( (order&1) == 0 ) {
			drawCurve(turtle,order, maxSize,-60);
		} else {
			turtle.turn(60);
			drawCurve(turtle,order, maxSize,-60);
		}

		outputTurtle.setValue(turtle);
		
	    return false;
	}


	private void drawCurve(Turtle turtle,int n, double distance,double angle) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		
		drawCurve(turtle,n-1,distance/2.0f,-angle);
		turtle.turn(angle);
		drawCurve(turtle,n-1,distance/2.0f,angle);
		turtle.turn(angle);
		drawCurve(turtle,n-1,distance/2.0f,-angle);
	}
}
